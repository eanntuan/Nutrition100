package edu.mit.csail.sls.nut;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.ServletContext;

import org.apache.commons.configuration.Configuration;
import org.json.JSONArray;
import org.json.JSONObject;

import edu.mit.csail.asgard.syntax.CRFToken;
import edu.mit.csail.asgard.syntax.Feature;
import edu.mit.csail.asgard.syntax.Features;
import edu.mit.csail.asgard.syntax.Sentence;
import edu.mit.csail.asgard.syntax.SentenceSegment;
import edu.mit.csail.asgard.syntax.SentenceTagger;
import edu.mit.csail.asgard.util.Configure;

/**
 * Runs the CRF on given diary/diaries & prints labels that don't match AMT.
 * @param data file to evaluate (e.g. /afs/csail.mit.edu/u/k/korpusik/nutrition/nikki/results.test).
 * @param output file path (e.g. /afs/csail.mit.edu/u/k/korpusik/nutrition/CRFmisses.txt)
 * @author korpusik
 */

public class EvalCRF {
	
	static final String[] namePath = {"Nutrition", "Asgard", ""};
	static final String domain = "Nutrition";
	static final String confFile = "conf/semlab";

	static ServletContext servletContext;
	static Configuration configuration;
	static File crfBase;
	static Feature segmenter;
	static SentenceTagger sentenceTagger;
	static PrintWriter writer;

	public static void main(String[] args) throws IOException {
		writer = new PrintWriter(args[1], "UTF-8");
		
		// run what gets run for a web servlet (NutritionContext.initialize)
		ArrayList<String> names = new ArrayList<String>(namePath.length+2);  	
		names.add(domain);
		for(String name : namePath)
			names.add(name);
		    configuration = Configure.getConfiguration(names);
			configuration.setProperty("domain", domain);	
			crfBase = new File("WebContent/WEB-INF/CRF/samples");
				
			try {
				Features.initializeFeatureComputers(configuration.getString("/tagger/@url"), true);
				segmenter = Features.whitespace_tokenizer;
				sentenceTagger = new SentenceSegment(crfBase.getPath(), new File(crfBase, confFile).toString());
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			// load JSON object of labeled food-attribute data from file
			Charset encoding = StandardCharsets.UTF_8;
			byte[] encoded = Files.readAllBytes(Paths.get(args[0]));
			String jsonstr = new String(encoded, encoding);
			
			JSONObject json = new JSONObject(jsonstr);
	        Iterator<?> keys = json.keys();

	        String text = "";
			//text = "\u200bI had jerk pork sweet potatoes rice  beans and salad with honey mustard dressing I had a blush wine as well as two Bud Lights to drink We were at a party at our wedding venue so we also had cake smores strawberry cheesecake and chocolate mousse";
	        JSONObject val = null;
	    	
	    	// loop through each query in test data file
	    	while( keys.hasNext() ){
	    		
	            text = (String)keys.next();
	    		writer.println("\n"+text);
	            
	            // map of AMT labels (per token index) from JSON
	    		Map <String, String> goldStandard = new HashMap<>();
	            
	            // get gold standard from labeled data
	            val = (JSONObject) json.get(text);
	            String[] words = text.split("\\s+");
	            Iterator<?> foodIndices = val.keys();
	            
	            String index = "";
	            JSONObject attrs = null;
	            while( foodIndices.hasNext() ){
	            	
	            	ArrayList<Segment> attrList = new ArrayList<Segment>();
	                index = (String)foodIndices.next();
	                if (index.equals("labelIndex") || index.equals("reverseLabelIndex")){
	            		continue;
	            	}
	                // assign food label to current index
	                goldStandard.put(index, "food");
	                
	                attrs = (JSONObject) val.get(index);
	                Iterator<?> attributes = attrs.keys();
                	
	                // assign property labels
	                while( attributes.hasNext() ){
	                	String label = (String)attributes.next();
	                	JSONArray attrIndices = (JSONArray) attrs.get(label);
	                	for (int i = 0; i < attrIndices.length(); i++) {
	                		  goldStandard.put((attrIndices.get(i).toString()), label);
	                	}
	                }
	            }
				
		    	// run CRF on given food diary to get tags
				Sentence sentence = new Sentence();
				sentence.originalText = text;
				sentence.isNutrition = true;
				Features.nlparser_pos_english.compute(sentence, text);
				sentenceTagger.addCRFClasses(sentence);
				
				// print tokens, CRF labels, and AMT labels
				int pos = 0;
				for(CRFToken token : sentence.tokens){
					// skip empty string (don't update index position variable)
					if (token.text.equals("")){
						continue;
					}
					//System.out.println("Token: "+token.text);
					//System.out.println("Word: "+words[pos]);
					String CRFlabel = token.crfClass.getName().toLowerCase();
					String AMTlabel = goldStandard.get(Integer.toString(pos));
					
					// assign "other" to indices with null value for AMT label
					if (AMTlabel==null){
						goldStandard.put(Integer.toString(pos), "other");
						AMTlabel = "other";
					}
					
					// print CRF misses
					if (!CRFlabel.equals(AMTlabel) && CRFlabel.equals("other")){
			    		//System.out.println(words[pos]+" "+CRFlabel+" "+AMTlabel);
			    		writer.println(words[pos]+" "+CRFlabel+" "+AMTlabel);
					}
					pos++;
		    	}
	    	}
			writer.close();
	}

}
