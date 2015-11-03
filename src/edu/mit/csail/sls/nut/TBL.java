package edu.mit.csail.sls.nut;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.xmlrpc.XmlRpcException;
import org.json.JSONArray;
import org.json.JSONObject;

import edu.mit.csail.asgard.syntax.CRFClass;
import edu.mit.csail.asgard.syntax.CRFToken;
import edu.mit.csail.asgard.syntax.Sentence;
import edu.mit.csail.asgard.syntax.SentenceSegment;
import edu.mit.csail.asgard.syntax.SentenceTagger;

/*
 * Transformation-based learning for segmenting foods and their properties.
 * Use train.dat and test.dat for automatically predicting most likely chunk.
 * Use train.init and test.init for using simple rule predictions.
 * Type can be "automatic" or "AMT" (no predicted BIO -> can be used to train CRF)
 * Or type can be "Simple" (use simple rule to predict BIO) or "FST" or "CRF"
 * Or type "POS" which adds POS tags or "semiCRF" which uses semi-CRF labels
 */

public class TBL {
	static String trainFile = "src/edu/mit/csail/sls/nut/results+deployed.train"; 
	static String testFile = "src/edu/mit/csail/sls/nut/results+deployed.test"; 

	public static void main(String[] args) throws IOException {
        String confFile = "conf/semlab";
		File crfBase = new File("WebContent/WEB-INF/CRF/samples");
		SentenceTagger sentenceTagger = null;
		try {
			sentenceTagger = new SentenceSegment(crfBase.getPath(), new File(crfBase, confFile).toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
		
        // TODO: remove POS tags for punctuation
		String type = "automatic"; // TODO: make this an argument
		boolean isOracle = true;
		String labelType = "IOE"; // IOB, IOE, or IOBES
		String tag_type = "semicrf";
		
		// make two training files if using CRF (have to train CRF and TBL)
		PrintWriter trainWriter = new PrintWriter(args[0], "UTF-8");
		makeDataFiles(trainFile, args[0], trainWriter, type, sentenceTagger, labelType, isOracle, tag_type);
		
		PrintWriter testWriter = new PrintWriter(args[1], "UTF-8");
		makeDataFiles(testFile, args[1], testWriter, type, sentenceTagger, labelType, isOracle, tag_type);
	}
	
	public static void makeDataFiles(String file, String writerName, PrintWriter writer, String type, SentenceTagger sentenceTagger, String labelType, boolean isOracle, String tag_type) throws IOException{
		// load JSON object of labeled food-attribute data from file
		Charset encoding = StandardCharsets.UTF_8;
		byte[] encoded = Files.readAllBytes(Paths.get(file));
		String jsonstr = new String(encoded, encoding);
		Segmentation segmentation = null;
		JSONObject json = new JSONObject(jsonstr);
		int numQueries = json.length();
		int half = numQueries / 2;
		System.out.println("half "+half);
		System.out.println(writerName.substring(writerName.length() - 1).equals("2"));
		Iterator<?> keys = json.keys();
		String text = "";
        JSONObject val = null;
        int j = 0;	

		// loop through each query in AMT results file
    	while( keys.hasNext() ){
            text = (String)keys.next();
        	
            // get semiCRF predicted segmentation
            try {
            	segmentation = Tag.runCRF(null, text, type, sentenceTagger, true, labelType, tag_type);
            } catch  (Exception e) {
            		System.out.println("could not run CRF on "+text);
            		continue;
            }
            
            val = (JSONObject) json.get(text);
            // for some reason, this word has a non-whitespace whitespace
            // requires manually replacing non-whitespace with a space
        	if (text.contains("in a")){
        		text = text.replaceAll("in a", "in a");
        	}
			String[] words = text.split("\\s+");
            Iterator<?> foodIndices = val.keys();
            
            // maps word index to BIO Features object
            Map <Integer, BIOFeatures> wordInfo = new HashMap<>();
                       
            // get AMT BIO labels from results file and put into wordInfo
            goldStandardBIO(type, words, val, foodIndices, wordInfo, labelType, segmentation, isOracle);
            
            // get predicted BIO labels from segmentation and put into wordInfo
            predictBIO(words, wordInfo, segmentation, labelType);

            // write samples to file
            writeToFile(writer, words, wordInfo, segmentation, type, isOracle);
            
            j++;
            /*
            if (j>5){
    	    	break;
    	    }
    	    */
    	}
    	writer.close();
	}
	
	/*
	 * Loop through AMT foods to get gold standard token labels and BIO labels.
	 */
	public static void goldStandardBIO(String type, String[] words, JSONObject val, Iterator<?> foodIndices, Map<Integer, BIOFeatures> wordInfo, String labelType, Segmentation segmentation, boolean isOracle) throws IOException{
		ArrayList<Segment> attrSegments = new ArrayList<>();
		ArrayList<CRFToken> foodItems = new ArrayList<>();
		ArrayList<String> foods = new ArrayList<>();
		Map<Integer, String> labels = new HashMap<Integer, String>();
		Map<Integer, CRFToken> tokens = new HashMap<Integer, CRFToken>();
		
		JSONObject attrs = null;
        while( foodIndices.hasNext() ){
        	String foodIndex = (String)foodIndices.next();
        	
        	// skip non-foods
        	if (foodIndex.equals("labelIndex") || foodIndex.equals("reverseLabelIndex")){
        		continue;
        	}
        	
        	// add AMT "food" label
        	BIOFeatures foodInfo = getInfo(Integer.parseInt(foodIndex), wordInfo);
        	foodInfo.setAMTLabel("food");
        	wordInfo.put(Integer.parseInt(foodIndex), foodInfo);
        	
        	// add new foodItem to foodItems and foods
        	String food = words[Integer.parseInt(foodIndex)];
        	foods.add(food+foodIndex);
        	CRFToken foodItem = new CRFToken(null, food, null);
        	foodItem.position = Integer.parseInt(foodIndex);
        	foodItems.add(foodItem);
        	
        	// create new food token
        	CRFToken token = new CRFToken(null, food, null);
        	token.crfClass = CRFClass.define("Food");
        	tokens.put(Integer.parseInt(foodIndex), token);
        	labels.put(Integer.parseInt(foodIndex), "food");
        	
        	// track smallest & largest indices associated w/ food
        	int lowIndex = Integer.parseInt(foodIndex);
        	int highIndex = Integer.parseInt(foodIndex);
        	
        	//System.out.println("food: "+index+" "+words[Integer.parseInt(index)]);
        	attrs = (JSONObject) val.get(foodIndex);
            Iterator<?> attributes = attrs.keys();
            
            // loop through all attributes of food
            while( attributes.hasNext() ){
            	String label = (String)attributes.next();
            	JSONArray attrIndices = (JSONArray) attrs.get(label);
            	
            	// start new attribute segment
            	Segment attrSeg = new Segment();
            	if (attrIndices.length()!=0){
	            	int attrIndex = (int)attrIndices.get(0);
	            	attrSeg.label = label;
	    			attrSeg.start = attrIndex;
	    			attrSeg.end = attrIndex+1;
	    			
            		// update lowest & highest indices
            		if (attrIndex < lowIndex){
            			lowIndex = attrIndex;
            		} else if (attrIndex > highIndex){
            			highIndex = attrIndex;
            		}
            		
            		// create new attribute token
                	token = new CRFToken(null, words[attrIndex], null);
                	token.crfClass = CRFClass.define(label.substring(0, 1).toUpperCase()+label.substring(1, label.length()));
                	tokens.put(attrIndex, token);
                	labels.put(attrIndex, label.toLowerCase());
            		
            		// add AMT attribute labels
            		BIOFeatures info = getInfo(attrIndex, wordInfo);
            		info.setAMTLabel(label);
                	wordInfo.put(attrIndex, info); 
            	}
    			
            	for (int i = 1; i < attrIndices.length(); i++) {
            		int attrIndex = (int)attrIndices.get(i);
            		// skip food indices
            		if (attrIndex==Integer.parseInt(foodIndex)){
            			continue;
            		}
            		
            		// create new attribute token
                	token = new CRFToken(null, words[attrIndex], null);
                	token.crfClass = CRFClass.define(label.substring(0, 1).toUpperCase()+label.substring(1, label.length()));
                	tokens.put(attrIndex, token);
                	labels.put(attrIndex, label.toLowerCase());

        			// update existing segment
            		if (attrSeg.end==attrIndex){
            			attrSeg.end = attrIndex+1;
            		} else if (attrIndex > attrSeg.end){
            			// add existing segment and start new segment
                		if (attrSeg.label!=null){
                    		//System.out.println("added new attribute segment: "+attrSeg.label+" "+attrSeg.start+" "+attrSeg.end);
                			attrSegments.add(attrSeg);
                		}
            			attrSeg = new Segment();
            			attrSeg.label = label;
            			attrSeg.start = attrIndex;
            			attrSeg.end = attrIndex+1;
            		}
            		          		
            		// add AMT attribute labels
            		BIOFeatures info = getInfo(attrIndex, wordInfo);
            		info.setAMTLabel(label);
                	wordInfo.put(attrIndex, info); 
            		
            		// update lowest & highest indices
            		if (attrIndex < lowIndex){
            			lowIndex = attrIndex;
            		} else if (attrIndex > highIndex){
            			highIndex = attrIndex;
            		}
            	}
            	
            	// add last segment
            	if (attrSeg.label!=null){
            		//System.out.println("added new attribute segment: "+attrSeg.label+" "+attrSeg.start+" "+attrSeg.end);
            		attrSegments.add(attrSeg);
            	}
            }
            
            // assigns gold standard BIO labels between lowIndex and highIndex
            //System.out.println(food+foodIndex);
            //System.out.println("lowIndex: "+lowIndex);
            //System.out.println("highIndex: "+highIndex);
            assignChunks(lowIndex, highIndex, wordInfo, labelType, false);
        }
        
        if (isOracle) {
        	// change segmentation.foods and segmentation.attributes
        	segmentation.foods = foods;
        	ArrayList<CRFToken> allTokens = new ArrayList<>();
    		ArrayList<String> allLabels = new ArrayList<>();
    		
        	// add food/attribute tokens in map, as well as others, to list
        	for (int i=0; i < words.length; i++){
        		// add token to tokens list
        		if (tokens.keySet().contains(i)){
        			allTokens.add(tokens.get(i));
        		} else {
        			// create new Other token
        			CRFToken token = new CRFToken(null, words[i], null);
                	token.crfClass = CRFClass.define("Other");
                	allTokens.add(token);
        		}
        		// add label to labels list
        		if (labels.keySet().contains(i)){
        			allLabels.add(labels.get(i));
        		} else {
        			// create new Other label
        			allLabels.add("other");
        		}
        	}
        	// change segmentation.sentence.tokens and segmentation.labels
        	Sentence sentence = new Sentence();
        	sentence.tokens = allTokens;
        	segmentation.labels = allLabels;
        	
        	// get predicted attributes
        	if (type.equals("Simple")) {    	    	
    			segmentation.attributes = GetAttributesSimple.getAttributeDeps(attrSegments, foodItems);
    	    } else if (type.equals("FST")) {
            	PrintWriter FSTWriter = null;
    	    	segmentation.attributes = GetAttributesFST.getAttributeDeps(sentence, FSTWriter, segmentation, foodItems);
    	    } else {
    	    	segmentation.attributes = GetAttributesCRF.getAttributeDeps(sentence, segmentation, foodItems, labelType);
        	}
        	
        	for (String food : segmentation.attributes.keySet()) {
        		//System.out.println(food);
        		for (Segment seg : segmentation.attributes.get(food)) {
        			//System.out.println("	"+seg.label+" "+seg.start+" "+seg.end);
        		}
        	}
        	// TODO: add FST and CRF too
        }
	}
	
	/*
	 * Loop through predicted foods to get predicted BIO labels.
	 */
	public static void predictBIO(String[] words, Map<Integer, BIOFeatures> wordInfo, Segmentation segmentation, String labelType){
		int wordIndex = 0;
        for (String word : words) {
        	String food = word+wordIndex;
        	int predictLowIndex = wordIndex;
        	int predictHighIndex = wordIndex;
        	
        	// skip words that aren't predicted foods
        	if (!segmentation.foods.contains(food)){
                wordIndex++;
        		continue;
        	}
  	
        	// get predicted attributes and low/high indices
        	if (segmentation.attributes!=null){
        		ArrayList<Segment> predictAttrs = segmentation.attributes.get(food);
        		if (predictAttrs!=null) {
        			for(Segment seg : predictAttrs){
            			// update predicted low index
        				if (seg.start < predictLowIndex){
        					predictLowIndex = seg.start;
        				}
        				if (seg.end > predictHighIndex){
        					predictHighIndex = seg.end-1;
        				}
        			}
        		}
        	}

        	// assigns predicted BIO labels to predictLowIndex thru predictHighIndex
            //System.out.println(food);
            //System.out.println("predictLowIndex: "+predictLowIndex);
            //System.out.println("predictHighIndex: "+predictHighIndex);
        	assignChunks(predictLowIndex, predictHighIndex, wordInfo, labelType, true);
            
            wordIndex++;
        }
	}
	
	/*
	 * Write each token and its associated features to given writer file.
	 * Type determines which features get written.
	 */
	public static void writeToFile(PrintWriter writer, String[] words, Map<Integer, BIOFeatures> wordInfo, Segmentation segmentation, String type, boolean isOracle) {

        int i = 0;
        for (String word : words) {
        	// skip if token is empty string
        	if (i>0 && words[i-1].equals("water") && word.length()==1 && words[3].equals("stryfry")){
        		continue;
        	}
        	
        	// assign index if not in wordInfo already
        	BIOFeatures info = getInfo(i, wordInfo);
        	wordInfo.put(i, info);
        	        		
        	String AMTLabel = wordInfo.get(i).getAMTLabel();
        	String semiCRFLabel = segmentation.labels.get(i);
        	String POS = segmentation.tags[i];
        	
        	String predictedBIO = wordInfo.get(i).getpredictedBIO();
        	String AMTBIO = wordInfo.get(i).getAMTBIO();
        	
        	// set default token label to "Other"
        	if (AMTLabel==null) {
        		AMTLabel = "other";
        	}
        	// set default BIO labels to "O"
        	if (predictedBIO==null) {
        		predictedBIO = "O";
        	}
        	if (AMTBIO==null) {
        		AMTBIO = "O";
        	}
        	
        	// write token and features to file
            if(type.equals("semiCRF")){
            	writer.println(word+" "+AMTLabel+" "+semiCRFLabel);
            } else if (type.equals("AMT")){
            	writer.println(word+" "+AMTLabel);
            } else if (type.equals("POS")){
            	writer.println(word+" "+POS+" "+AMTLabel+" "+predictedBIO);
            } else if (type.equals("Simple") || type.equals("FST") || type.equals("CRF")){
            	// use AMT labels if doing oracle experiment
            	if (isOracle){
        			writer.println(word+" "+AMTLabel+" "+predictedBIO+" "+AMTBIO);
        		} else {
        			writer.println(word+" "+semiCRFLabel+" "+predictedBIO+" "+AMTBIO);
        		}
        	} else if (type.equals("automatic")){
        		if (isOracle){
        			writer.println(word+" "+AMTLabel+" "+AMTBIO);
        		} else {
        			writer.println(word+" "+semiCRFLabel+" "+AMTBIO);
        		}
        	}
                   
        	// since not actually two tokens due to strange non-whitespace,
        	// must manually decrement index
        	if (segmentation!=null && segmentation.tokens.get(i).contains("in a")){
        		System.out.println(segmentation.tokens.get(i));
        		i--;
        	}
        	i++;
        }
        writer.println(); // separate sentences by spaces

	}
	
	public static BIOFeatures getInfo(int index, Map<Integer, BIOFeatures> wordInfo) {
		BIOFeatures info = wordInfo.get(index);
		if (info==null) {
    		info = new BIOFeatures();
    	}
		return info;
	}
	
	/*
	 * Assign chunk labels from lowIndex through highIndex
	 */
	public static void assignChunks(int lowIndex, int highIndex, Map<Integer, BIOFeatures> wordInfo, String labelType, boolean isPrediction) {
		
		// if IOB or IOBES and not a single index, assign 'B' to smallest index
		if (labelType.equals("IOB") || (labelType.equals("IOBES") && lowIndex!=highIndex)){
			addChunk(lowIndex, "B", wordInfo, isPrediction);
		}
		
		// if IOE, assign 'I' to smallest index
		if (labelType.equals("IOE")){
			addChunk(lowIndex, "I", wordInfo, isPrediction);
		}
				
		// if IOBES and a single index, assign 'S' to smallest index
		if (labelType.equals("IOBES") && lowIndex==highIndex){
			addChunk(lowIndex, "S", wordInfo, isPrediction);
		}

        // assign 'I' to every word in range (smallest index, largest index)
        for (int i =lowIndex+1; i <highIndex; i++){
    		addChunk(i, "I", wordInfo, isPrediction);
        }
        
        // if IOB, assign 'I' to last index
     	if (labelType.equals("IOB")){
     		addChunk(highIndex, "I", wordInfo, isPrediction);
     	}
     		
     	// if IOE, assign 'I' to last index
     	if (labelType.equals("IOE") || (labelType.equals("IOBES") && lowIndex!=highIndex)){
     		addChunk(highIndex, "E", wordInfo, isPrediction);
     	}
	}
	
	/*
	 * Insert BIO label for word i into infoIndex in wordInfo BIOFeatures object.
	 */
	public static void addChunk(int i, String label, Map<Integer, BIOFeatures> wordInfo, boolean isPrediction) {
		// if index i not already in wordInfo, add it
		if (!wordInfo.keySet().contains(i)) {
			BIOFeatures info = new BIOFeatures();
			wordInfo.put(i, info);
    	}
		
		BIOFeatures info = wordInfo.get(i);
		
		// check whether label is predicted or AMT gold standard before setting it
		if (isPrediction) {
			// if predictedBIO filled with O or empty, set label
			if (info.getpredictedBIO()==null || info.getpredictedBIO().equals("O")){
				info.setpredictedBIO(label);
			}
		} else {
			// if predictedBIO filled with O or empty, set label
			if (info.getAMTBIO()==null || info.getAMTBIO().equals("O")){
				info.setAMTBIO(label);
			}
		}
	}
}
