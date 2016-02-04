package edu.mit.csail.sls.nut;

import java.util.ArrayList;
import java.util.Map;

import edu.mit.csail.asgard.syntax.CRFToken;
import edu.mit.csail.asgard.syntax.Sentence;

public class GetAttributesDependency {

	/**
     * Get all food-attribute associations, using Stanford dependency parse:
     * associate a food and attribute if there's a dependency between them.
     */
	static Map<String, ArrayList<Segment>> getAttributeDeps(Sentence sentence, NLPData segmentation, ArrayList<CRFToken> foodItems) {		
		Map<String, ArrayList<Segment>> segmentDeps = Tag.initializeSegmentDeps(foodItems);
		
		// determine food-attribute dependencies for entire attribute segments
		int pos = 0; // keeps track of current token index
		for(Segment segment : segmentation.segments){
			boolean segmentAdded = false;
			int i = segment.start;
			// only continue if this is an attribute segment
			System.out.println(segment.label);
			if (segment.label.contains("Food") || segment.label.contains("Other")) {
				while (i < segment.end) {
					pos+=1;
					if (segmentation.tokens.get(i).contains("'")) {
						pos+=1;
					}
					i++; // increment the token index if contains apostrophe
				}
				continue;
			}
			while (i < segment.end) {
				pos += 1;
				String attribute;
				// use the part of the token before the apostrophe
				if (segmentation.tokens.get(i).contains("'") && !segmentation.tokens.get(i).equals("'")) {
					String[] tokens = segmentation.tokens.get(i).split("'");
					attribute = tokens[0]+"-"+pos;
					pos+=1; // increment the token index if contains apostrophe
				} else {
					attribute = segmentation.tokens.get(i).replaceAll("[^a-zA-Z ]", "")+"-"+pos;
				}
				// if only one food, use that
				if (foodItems.size()==1) {
					CRFToken foodToken = foodItems.get(0);
					ArrayList<Segment> attrList = segmentDeps.get(foodToken.text+foodToken.position);
					if (!segmentAdded) {
						attrList.add(segment);
						segmentAdded = true;
					}
					i++;
					continue;
				}
						
				// loop thru foods to find direct match btw food & attribute
				for (CRFToken foodToken : foodItems) {
					int foodPos = foodToken.position+1;
					//String food = foodToken.text+"-"+foodPos;
					String food = foodToken.text.replaceAll("[^a-zA-Z ]", ""+"-"+foodPos);
					System.out.println("food "+food);
					System.out.println("attribute "+attribute);
					// loop through dependencies
					for(Object dependency : sentence.deps){
						String dep = (String)dependency;
						// if food-attribute dependency, add entire segment
						if (dep.contains(food) && dep.contains(attribute)) {
							System.out.println("food-attribute dep: "+dep);
							ArrayList<Segment> attrList = segmentDeps.get(foodToken.text+foodToken.position);
						    if (!segmentAdded) {
								attrList.add(segment);
								segmentAdded = true;
							}
						    break;
						}
					}
				}				
				i++;
			}
		}
		return segmentDeps;
	}

}
