package edu.mit.csail.sls.nut;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import edu.mit.csail.asgard.syntax.CRFToken;
import edu.mit.csail.asgard.syntax.Sentence;

public class GetAttributesFST {

	/**
     * Get all food-attribute associations, using a simple rule:
     * attributes are assigned to the food that comes right after.
	 * @param sentence 
	 * @throws IOException 
     */
	static Map<String, ArrayList<Segment>> getAttributeDeps(Sentence sentence, PrintWriter writer, Segmentation segmentation, ArrayList<CRFToken> foodItems) throws IOException {		
		Map<String, ArrayList<Segment>> segmentDeps = Tag.initializeSegmentDeps(foodItems);
		
		if (writer != null){
			writer.println("\n\n"+segmentation.text);
		}
		Object[] segsAndLabels = getSegments(sentence, segmentation);
		String labels = (String) segsAndLabels[0];
		ArrayList<Segment> segs = (ArrayList<Segment>) segsAndLabels[1];
		
		// run the FST
		String[] FSTout = null;
		if (!labels.equals("")){
			FSTout = FST.getOutput(labels, 3, writer);
		} else {
			return segmentDeps;
		}
		System.out.println("output:");
		
		return getAttributesFromFST(FSTout, segmentDeps, segs, segmentation);
	}
	
	static Object[] getSegments(Sentence sentence, Segmentation segmentation){

		// use FST to separate CRF tags into food segments
		String labels = ""; 
		char currLabel = '\0';
		int i = 0;
		String[] words = segmentation.text.split(" ");
		Segment seg = new Segment();
		ArrayList<String> foods = new ArrayList<>(); // redo foods with adjusted indices
		ArrayList<Segment> segs = new ArrayList<Segment>(); 
		// turn tokens into sequence of labels and segments (ignore other)
		for(CRFToken token : sentence.tokens){
			// skip empty lines
			if (token.text.equals("")){
				continue;
			}
			// adjust tokens to match original
			String prevChar = "";
			String prevWord = "";
			if (i > 0 && i < words.length && words[i-1].length()>0) {
				prevChar = words[i-1].substring(words[i-1].length()-1);
				prevWord = words[i-1];
				//System.out.println(token.text+" "+index+" "+words[index]);
			}
			// adjust for periods, percents, and apostrophes
			if ((token.text.equals(".") && prevChar.equals(".")) ||
					(token.text.equals("%") && prevChar.equals("%")) ||
					(prevWord.equals("%"+token.text)) ||
					(token.text.charAt(0)=='\'' && prevWord.length()>0 && prevWord.charAt(prevWord.length()-token.text.length())=='\'')) {
				i--;
			}
			// check if food and add to food list
    		if (token.crfClass.toString().contains("Food")) {
    			foods.add(token.text+i);
    		}
			char newLabel = token.crfClass.getName().charAt(0);
			// new food label or any non-repeating, non-other label
			if (newLabel=='F' || (newLabel != 'O' && newLabel != currLabel)){
				// add prev seg to segs list
				if (seg.label!=null){
					segs.add(seg);
				}
				seg = new Segment();
				seg.label = token.crfClass.getName();
				seg.start = i;
				seg.end = i+1;
				if (labels.length()>0){
					labels += " ";
				}
				labels += newLabel;
			} else if (newLabel != 'O' && newLabel == currLabel){
				// continue current segment
				seg.end = i+1;
			}
			currLabel = newLabel;
			i++;
			//System.out.println(token.text+" "+token.crfClass.getName());
		}
		segmentation.foods = foods;
		// add final seg to segs list
		if (seg.label!=null){
			segs.add(seg);
		}
		System.out.println("originalText: "+segmentation.text);
		System.out.println("segmentation text: "+segmentation.text);
		System.out.println("segmentation tokens: "+segmentation.tokens);
		for(Segment seg1 : segmentation.segments){
			System.out.println(seg1.label+" "+seg1.start+" "+seg1.end);
		}
		Object[] segsAndLabels = new Object[2];
		segsAndLabels[0] = labels;
		segsAndLabels[1] = segs;
		return segsAndLabels;

	}
	
	static Map<String, ArrayList<Segment>> getAttributesFromFST(String[] FSTout, Map<String, ArrayList<Segment>> segmentDeps, ArrayList<Segment> segs, Segmentation segmentation){
		// use FST output to assign attributes to foods in a segment
		int index = 0; // index that is decremented at every # sign
		int actualIndex = 0; // actual index that is not changed
		segmentDeps = new HashMap<>();
		ArrayList<Segment> segMatches = new ArrayList<Segment>();
		
		// keep map of foods without indices in order to find repeat foods
		Map<String, ArrayList<Segment>> segmentDepsNoIndices = new HashMap<>();
		
		// map from foods without indices to corresponding foods with indices
		Map<String, ArrayList<String>> foodMatches = new HashMap<>();

		// TODO: uncomment food matching code
		// TODO: debug matching foods :(
		String food = "";
		String foodNoIndex = "";
		for(String label : FSTout){
			System.out.println(index+" "+label);
			if (label.equals("#")){
				// add segMatches to segmentDeps
				if (!food.equals("")){
					/*
					// if food already in foodsNoIndices, combine attributes
					if (segmentDepsNoIndices.keySet().contains(foodNoIndex) && segMatches!=segmentDepsNoIndices.get(foodNoIndex)) {
						System.out.println("food exists! "+food);
						
						// take union of attributes
						segMatches.addAll(segmentDepsNoIndices.get(food));

						// add union attrs to segmentDeps with/without index
						for (String foodWithIndex : foodMatches.get(food)){
							segmentDeps.put(foodWithIndex, segMatches);
						}
					} 
					*/
					// add new segmatches to existing segmatches
					if (segmentDeps.keySet().contains(food)) {
						segMatches.addAll(segmentDeps.get(food));
					} 
					segmentDeps.put(food, segMatches);
					segmentDepsNoIndices.put(foodNoIndex, segMatches);
				}
				// decrement index due to '#'
				index--; 
				// re-initialize segMatches and food
				segMatches = new ArrayList<Segment>();
				food="";
				foodNoIndex="";
			} else {
				// add corresponding segment to list
				Segment segMatch = segs.get(index);
				if (label.equals("F")){
					
					//food = segmentation.tokens.get(segMatch.start)+segMatch.start;
					//foodNoIndex = segmentation.tokens.get(segMatch.start);
					
					// get entire food segment
					int i = segMatch.start;
					food = segmentation.tokens.get(i);
					while (i+1 < segMatch.end){
						food += " "+segmentation.tokens.get(i);
						foodNoIndex += " "+segmentation.tokens.get(i);
						i += 1;
					}
					food += segMatch.start;
					
					// add food with/without index to foodMatches map
					ArrayList<String> foodsWithIndex = foodMatches.get(foodNoIndex);
					if (foodsWithIndex==null){
						foodsWithIndex = new ArrayList<String>();
					}
					foodsWithIndex.add(food);
					foodMatches.put(foodNoIndex, foodsWithIndex);
					System.out.println("food: "+food);
					
					// if >1 food in segment, assign prior attrs (simple rule)
					if (actualIndex+1 < FSTout.length && !FSTout[actualIndex+1].equals("#")){
						segmentDeps.put(food, segMatches);
						segmentDepsNoIndices.put(foodNoIndex, segMatches);
						// re-initialize segMatches and food
						//segMatches = new ArrayList<Segment>();
					}
					
				} else {
					segMatches.add(segMatch);
					System.out.println("attr segment: "+segMatch.label+" "+segMatch.start+" "+segMatch.end);
				}
			}
			index++;
			actualIndex++;
		}
		// add final segMatches to segmentDeps
		if (!food.equals("")){
			System.out.println("adding food: "+food);
			segmentDeps.put(food, segMatches);
		}
		return segmentDeps;
	}
	

}
