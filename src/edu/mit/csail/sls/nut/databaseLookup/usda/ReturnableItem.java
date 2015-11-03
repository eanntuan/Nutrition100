package edu.mit.csail.sls.nut.databaseLookup.usda;

import java.util.ArrayList;

public interface ReturnableItem {
	public double getCalories();

	public String getItemName();

	public ArrayList<String> getFeatures();

	public boolean hasFeature(String currentFeature);
	
	public boolean hasSimilarFeature (String currentFeature);
	
	public boolean hasIdenticalFeature (String currentFeature);
	
	public String getFoodID();
	
	public ArrayList<String> getAllFoodIDs ();

}
