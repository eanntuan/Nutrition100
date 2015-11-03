package edu.mit.csail.sls.nut.databaseLookup.usda;

import java.util.ArrayList;

public class NutritionixItem implements ReturnableItem {

	private String foodID;
	private String longDesc;
	private double calories;
	private ArrayList<String> features = new ArrayList<String>();
	private String quantityAmount;
	private String quantityUnit;
	private String nutID;
	
	public NutritionixItem (String nutId, String p_longDesc, double p_calories, String quantityAmount,
			String quantityUnit){
		super();
		System.out.println(p_calories);
		this.foodID = "-1";
		this.setNutID(nutId);
		this.longDesc = p_longDesc;
		this.calories = p_calories;
		this.setQuantityAmount(quantityAmount);
		this.setQuantityUnit(quantityUnit);
	}

	/**
	 * @return the foodID
	 */
	public String getFoodID() {
		return foodID;
	}

	/**
	 * @return the longDesc
	 */
	public String getLongDesc() {
		return longDesc;
	}

	
	public String toString (){
		return foodID+"("+calories+")";
//		return longDesc+": Calories = "+calories;
	
	}
	
	public boolean hasFeature (String feature) {
		return false;
	}
	
	public boolean hasSimilarFeature(String feature) {
		return false;
	}
	
	public boolean hasIdenticalFeature(String feature) {
		return false;
	}

	@Override
	public ArrayList<String> getAllFoodIDs() {
		ArrayList<String> toReturn = new ArrayList<String>();
		toReturn.add(foodID);
		return toReturn;
	}

	@Override
	public double getCalories() {
		// TODO Auto-generated method stub
		return calories;
	}

	@Override
	public String getItemName() {
		// TODO Auto-generated method stub
		return longDesc;
	}

	@Override
	public ArrayList<String> getFeatures() {
		// TODO Auto-generated method stub
		return features;
	}

	/**
	 * @return the quantityAmount
	 */
	public String getQuantityAmount() {
		return quantityAmount;
	}

	/**
	 * @param quantityAmount the quantityAmount to set
	 */
	public void setQuantityAmount(String quantityAmount) {
		this.quantityAmount = quantityAmount;
	}

	/**
	 * @return the quantityUnit
	 */
	public String getQuantityUnit() {
		return quantityUnit;
	}

	/**
	 * @param quantityUnit the quantityUnit to set
	 */
	public void setQuantityUnit(String quantityUnit) {
		this.quantityUnit = quantityUnit;
	}

	/**
	 * @return the nutID
	 */
	public String getNutID() {
		return nutID;
	}

	/**
	 * @param nutID the nutID to set
	 */
	public void setNutID(String nutID) {
		this.nutID = nutID;
	}
}
