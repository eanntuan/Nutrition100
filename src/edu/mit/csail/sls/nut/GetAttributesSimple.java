package edu.mit.csail.sls.nut;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import edu.mit.csail.asgard.syntax.CRFToken;
import edu.mit.csail.asgard.syntax.Sentence;

public class GetAttributesSimple {

	/**
     * Get all food-attribute associations, using a simple rule:
     * attributes are assigned to the food that comes right after.
     */
	static Map<String, ArrayList<Segment>> getAttributeDeps(ArrayList<Segment> attrSegments, ArrayList<CRFToken> foodItems) {		
		Map<String, ArrayList<Segment>> segmentDeps = Tag.initializeSegmentDeps(foodItems);
		
		// determine food-attribute dependencies for entire attribute segments
		for(Segment segment : attrSegments){
			// segments have multiple words, so check if segment was added yet
			boolean segmentAdded = false; 
			int i = segment.start;
			// skip if this is not an attribute segment
			if (segment.label.toLowerCase().contains("food") || segment.label.toLowerCase().contains("other")) {
				continue;
			}
			// this is an attribute segment
			while (i < segment.end) {
				// if only one food, use that
				if (foodItems.size()==1) {
					//System.out.println("Assign to only food available");
					CRFToken foodToken = foodItems.get(0);
					ArrayList<Segment> attrList = segmentDeps.get(foodToken.text+foodToken.position);
					if (!segmentAdded){
						attrList.add(segment);
						segmentAdded = true;
					}
					i++;
					continue;
				}

				// TODO: add food matching code (i.e. ignore indices)
				// TODO: add attributes from current food to prev food matches
				// as default, assign attribute to next food item
				CRFToken match = null;
				for (CRFToken foodToken : foodItems) {
					// choose food right after attribute
					if (foodToken.position > i && (match==null || match.position > foodToken.position)){
						match = foodToken;
					}
				}
				if (match!=null) {
					// foods with same name get same attributes (i.e. foods with/without indices share attrList)
					ArrayList<Segment> attrList = segmentDeps.get(match.text+match.position);
					if (!segmentAdded){
						// add attributes of matching food items
						if (!attrList.contains(segment)){
							attrList.add(segment);
							segmentAdded = true;
						}
					}
				}
				i++;
			}
			// if attribute still unassigned, assign to last food
			if (!segmentAdded && foodItems.size()>0){
				// find food token with largest index
				CRFToken match = null;
				for (CRFToken foodToken : foodItems) {
					// choose food right after attribute
					if (match==null || foodToken.position > match.position){
						match = foodToken;
					}
				}
				// foods with same name get same attributes (i.e. foods with/without indices share attrList)
				ArrayList<Segment> attrList = segmentDeps.get(match.text+match.position);
				attrList.add(segment);
			}
		}
		return segmentDeps;
	}
}
