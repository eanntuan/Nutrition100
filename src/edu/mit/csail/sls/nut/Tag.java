package edu.mit.csail.sls.nut;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.io.StringReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cc.mallet.fst.CRF;
import cc.mallet.fst.SimpleTagger;
import cc.mallet.fst.TransducerEvaluator;
import cc.mallet.pipe.Pipe;
import cc.mallet.pipe.iterator.LineGroupIterator;
import cc.mallet.types.InstanceList;
import cc.mallet.types.Sequence;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.util.JSONPObject;

import edu.mit.csail.asgard.syntax.CRFClass;
import edu.mit.csail.asgard.syntax.CRFToken;
import edu.mit.csail.asgard.syntax.Features;
import edu.mit.csail.asgard.syntax.ParentNode;
import edu.mit.csail.asgard.syntax.ParseNode;
import edu.mit.csail.asgard.syntax.Sentence;
import edu.mit.csail.asgard.syntax.SentenceTagger;

/**
 * Servlet implementation class Tag
 */
@WebServlet(name = "tag", description = "Add semantic classes to a sentence", urlPatterns = { "/tag" })
public class Tag extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public Tag() {
        super();
    }
	
    // prints the parse tree and finds foods/attributes that are in same NP
    static void traverseTree(ParseNode node, int indent, ArrayList<ParseNode> callers){
    	// maps NP nodes to list of children tokens
    	Map <ParentNode, ArrayList<CRFToken>> NPChildren = new HashMap<>();
    	
    	for(int i=0; i<indent; i++){
			System.out.print(' ');
		}
		if (node.isLeaf()){
			System.out.print(node.text);
			CRFToken crfToken = (CRFToken)node;
			System.out.print(" | ");
			System.out.print(crfToken.crfClass.toString());
		} else {
			System.out.print('(');
			System.out.print(node.text);
			ParentNode parent = ((ParentNode)node);
			if (parent.children.size() > 1 || (parent.children.size() == 1 && !parent.children.get(0).isLeaf())){
				System.out.println();
				for(ParseNode child : parent.children){
					ArrayList<ParseNode> newCallers = new ArrayList<>(callers);
					newCallers.add(parent);
					traverseTree(child, indent+3, newCallers);
				}
				for(int i=0; i<indent; i++){
					System.out.print(' ');
				}
				System.out.print(')');
			} else {
				System.out.print(' ');
				ArrayList<ParseNode> newCallers = new ArrayList<>(callers);
				newCallers.add(parent);
				for(ParseNode child : parent.children){
					CRFToken crfToken = (CRFToken)child;
					for(ParseNode ancestor: newCallers) {
						// if any ancestor is NP, add child to descendants list
						if (ancestor.text.equals("NP")) {
							ArrayList<CRFToken> children = NPChildren.get(ancestor);
							if (children == null) {
								children = new ArrayList<CRFToken>();
								NPChildren.put((ParentNode) ancestor, children);
						    }
							children.add(crfToken);
						} 
					}
					System.out.print(child.text);
					System.out.print(" | ");
					System.out.print(crfToken.crfClass.toString());
				}
				System.out.print(')');
			}
		}
		System.out.println();
	}
    
	/*
	 * Run CRF on given tokens (i.e. single sentence), rather than input file.
	 */
	public static String[] runMalletTagger(String text) throws FileNotFoundException, IOException, ClassNotFoundException{
		String[] labels = null;
		InstanceList testData = null;
		Pipe p = null;
		CRF crf = null;
		TransducerEvaluator eval = null;
		int nBest = 1;
		
		// use crf model saved to file "MalletModel"
		//ObjectInputStream s = new ObjectInputStream(new FileInputStream("/usr/users/korpusik/workspace/Nutrition/WebContent/WEB-INF/models/MalletModel"));
		ObjectInputStream s = new ObjectInputStream(NutritionContext.getResourceAsStream("WEB-INF/models/MalletModel"));
		crf = (CRF) s.readObject();
		s.close();
		p = crf.getInputPipe();
		p.setTargetProcessing(false);
		
		testData = new InstanceList(p);
		//Reader testFile = new FileReader(new File("testSingleMealNoLabels"));
		//testData.addThruPipe(new LineGroupIterator(testFile, Pattern.compile("^\\s*$"), true));
		text = text.replaceAll("\\s", "\n"); // replace spaces with new line characters
		testData.addThruPipe(new LineGroupIterator(new StringReader(text), Pattern.compile("^\\s*$"), true));

		for (int i = 0; i < testData.size(); i++) {
			Sequence input = (Sequence)testData.get(i).getData();
			//System.out.println("input: "+input);
			Sequence[] outputs = SimpleTagger.apply(crf, input, nBest);
			System.out.println("applied Mallet CRF");
			int k = outputs.length;
			boolean error = false;
			for (int a = 0; a < k; a++) {
				if (outputs[a].size() != input.size()) {
					error = true;
				}
			}
			if (! error) {
				//System.out.println("input size "+input.size());
				labels = new String[input.size()];
				for (int j = 0; j < input.size(); j++) {
					StringBuffer buf = new StringBuffer();
					for (int a = 0; a < k; a++) {
						buf.append(outputs[a].get(j).toString());
					}
					// add label to list of labels
					String label = buf.toString();
					labels[j] = label;
				}
			}
		}
		
		//testFile.close();
		return labels;
	}    
	
	/*
	 * Run PyNutrition CRFsuite on input meal description, return label strings.
	 */
	public static String[] runPyNutritionTagger(String text) throws IOException {
		 // TODO: pre-load vectors (i.e. try to make it FAST!)
		 // TODO: re-use results in db/img search instead of re-doing tagging

		 String[] labels = null;

		 ProcessBuilder pb = new ProcessBuilder("python","/scratch/PyNutrition/scripts/predict.py", "/scratch/PyNutrition-data/semlab.train.tagged.sents", "/scratch/PyNutrition-data/GoogleNews-vectors-negative300.bin", text);
		 Process p = pb.start();
		 		  
		 BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
		 String line = "";
		 // get labels from output
		 while ((line = in.readLine()) != null) {
			 labels = line.split("\t");
		 }
		 
		 // map integer labels to strings
		 int i = 0;
		 for (String label : labels){
			 if (label.equals("1")){
				 labels[i]="food";
			 } else if(label.equals("2")){
				 labels[i]="brand";
			 } else if(label.equals("3")){
				 labels[i]="quantity";
			 } else if(label.equals("4")){
				 labels[i]="description";
			 } else if(label.equals("5")){
				 labels[i]="other";
			 }
			 i++;
		 }
		return labels;
	}
   
    /**
     * Tokenizes, parses, and uses CRF to label given text string.
     * Then finds the food-attribute dependencies using given method type.
     * @return 
     * @throws IOException 
     * @throws ClassNotFoundException 
     */
    public static Segmentation runCRF(PrintWriter FSTWriter, String text, String type, SentenceTagger tagger, boolean eval, String labelRep, String tag_type) throws IOException, ClassNotFoundException {
    	Sentence sentence = new Sentence();
		sentence.originalText = text;
		sentence.isNutrition = true;
		if (tag_type.equals("semicrf")){
			Features.nlparser_pos_english.compute(sentence, text);
			tagger.addCRFClasses(sentence);
		} else {
			// run Mallet SimpleTagger to get classes
			String[] labels = null;
			if (tag_type.equals("mallet")){
				labels = runMalletTagger(sentence.originalText);				
			} else if (tag_type.equals("crfsuite")){
				labels = runPyNutritionTagger(sentence.originalText);
			}
			
			// add crfClasses to each token (split on spaces)
			String[] seq = sentence.originalText.split(" ");
			sentence.tokens = new ArrayList<CRFToken>();			
			for (int i = 0; i<seq.length; i++){
				CRFToken token = new CRFToken(null, seq[i], null);
				token.position = i;
				String label = labels[i];
				System.out.println(label);
				String uppercaseLabel = label.substring(0, 1).toUpperCase()+label.substring(1, label.length());
				token.crfClass = CRFClass.define(uppercaseLabel);
				sentence.tokens.add(token);
			}
			
			// populate sentence.segments
	        int segmentStart = 0;
	        while(segmentStart < seq.length){
	        	// get the end of the segment
	        	int segmentEnd = -1;
	        	for (int i = segmentStart + 1; i < seq.length; i++) {
	        		// segment has ended if label changes
	    			if (!labels[i].equals(labels[segmentStart])){
	    				segmentEnd = i-1;
	    				break;
	    			}
	    		}
	        	if (segmentEnd==-1){
	        		segmentEnd = seq.length-1;
	        	}
	        	// assign new segment and re-initialize segment
	        	sentence.setSegment(segmentStart, segmentEnd);
	        	segmentStart = segmentEnd+1;
	        }
		}
		Segmentation segmentation = new Segmentation(sentence);
		segmentation.parse = sentence.parse;
		segmentation.deps = sentence.deps;
		/*
		System.out.println("dependencies: ");
		for (Object dep : segmentation.deps){
			System.out.println(dep);
		}
		*/
		
		/*
		if (!eval) {
			// traverse the parse tree and get list of tokens in each NP
			ParentNode root = sentence.getRoot();
			ArrayList<ParseNode> callers = new ArrayList<ParseNode>();
			callers.add(root);
			traverseTree(root, 0, callers);
		
			// print all children within each NP
			Iterator it = NPChildren.entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry pairs = (Map.Entry)it.next();
				ParentNode key = (ParentNode) pairs.getKey();
				System.out.println(key.text);
				for (CRFToken child: (ArrayList<CRFToken>) pairs.getValue())
					System.out.println(child.text+" "+child.crfClass.toString());
				it.remove(); // avoids a ConcurrentModificationException
			}
		}
		*/
	    			
		// add all food tokens to foodItems ArrayList and segmentDeps map
		ArrayList<CRFToken> foodItems = new ArrayList<>();
		ArrayList<String> foods = new ArrayList<>();
		for(CRFToken token : sentence.tokens){
        	if (token.crfClass!=null) {
        		// check if food and add to food list
        		if (token.crfClass.toString().contains("Food")) {
        			foodItems.add(token);
        			foods.add(token.text+token.position);
        			ArrayList<Segment> attrList = new ArrayList<Segment>();
        		} 
        	}
        }
		segmentation.foods = foods;

	    // get all food-attribute dependencies (method type is an argument)
		System.out.println("Type:"+type);
		if (type.equals("Simple")) {    	    	
			segmentation.attributes = GetAttributesSimple.getAttributeDeps(segmentation.segments, foodItems);
	    } else if (type.equals("Dependency")) {
	    	segmentation.attributes = GetAttributesDependency.getAttributeDeps(sentence, segmentation, foodItems);    	    	
	    } else if (type.equals("FST")) {
	    	segmentation.attributes = GetAttributesFST.getAttributeDeps(sentence, FSTWriter, segmentation, foodItems);
	    } else {
	    	segmentation.attributes = GetAttributesCRF.getAttributeDeps(sentence, segmentation, foodItems, labelRep);
    	}
		/*
		System.out.println("tokens");
		for (String token: segmentation.tokens){
			System.out.println(token);
		}
		System.out.println("segments");
		for (Segment seg: segmentation.segments){
			System.out.println(seg.start+" "+seg.end+" "+seg.label);
		}
		*/
	    return segmentation;
    }
    
    public static Map<String, ArrayList<Segment>> initializeSegmentDeps(ArrayList<CRFToken> foodItems){
		// maps food tokens to list of attribute segments
		Map <String, ArrayList<Segment>> segmentDeps = new HashMap<>();
		
		for(CRFToken food : foodItems){
        	ArrayList<Segment> attrList = new ArrayList<Segment>();
			// append index to food text to avoid overwriting repeats
        	segmentDeps.put(food.text+food.position, attrList);
        }
    	return segmentDeps;
    }

	/**
	 * @param useMallet 
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String jsonp = request.getParameter("jsonp");
		String text = request.getParameter("text");
		// get the type of approach for associating foods with attributes
		String segment_type = request.getParameter("segment_type"); 
		String labelRep = request.getParameter("labelRep");
		String tag_type = request.getParameter("tag_type");
		System.out.println("labelRep: "+labelRep);
		System.out.println(text);
		
		// run CRF and get food-attribute dependencies
		PrintWriter FSTWriter = null;
		Object result = null;
		try {
			result = runCRF(FSTWriter, text, segment_type, NutritionContext.sentenceTagger, false, labelRep, tag_type);
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (null != jsonp)
			result = new JSONPObject(jsonp, result);
		ObjectMapper objectMapper = new ObjectMapper();
		response.setContentType("application/javascript");
		PrintWriter writer = response.getWriter();
		objectMapper.writeValue(writer, result);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	}

}
