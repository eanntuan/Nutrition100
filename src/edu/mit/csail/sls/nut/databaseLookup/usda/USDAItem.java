package edu.mit.csail.sls.nut.databaseLookup.usda;

import java.util.ArrayList;

public class USDAItem extends TreeNode implements Comparable<USDAItem>, FSTNode, ReturnableItem {

	private String foodID;
	private String longDesc;
	private String itemName;
	private double calories;
	private ArrayList<String> features;
	private double protein;
	private double fat;
	private double cholesterol;
	private double sodium;
	private double carbohydrates;
	private double fiber;
	private double sugars;
	private String image;
	
	public USDAItem (String p_foodID, String p_longDesc, double p_calories){
		super();
		this.foodID = p_foodID; 
		this.longDesc = p_longDesc.replaceAll("\"", "");
		this.calories = p_calories;
		type=nodeType.LEAF;
		String[] segments = p_longDesc.split(",");
		this.itemName = segments[0];
		features = new ArrayList<String>();
		for (int i=1; i<segments.length; i++) {
			features.add(segments[i]);
		}
	}

	public USDAItem() {
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
		return features.contains(feature);
	}
	
	public boolean hasSimilarFeature(String feature) {
		for (String current : features) {
			if (!current.contains("NS")) {
				if (current.contains(feature)) {
					return true;
				} else if (current.replaceAll("\\W", "").contains(
						feature.replaceAll("\\W", ""))) {
					return true;
				}
			}
		}
		return false;
	}
	
	public boolean hasIdenticalFeature(String feature) {
		for (String current : features) {
			if (!current.contains("NS")) {
				if (current.contains(feature)) {
					return true;
				} else if (current.replaceAll("\\W", "").equalsIgnoreCase(
						feature.replaceAll("\\W", ""))) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * @return the itemName
	 */
	public String getItemName() {
		return itemName;
	}

	/**
	 * @param itemName the itemName to set
	 */
	public void setItemName(String itemName) {
		this.itemName = itemName;
	}

	public ArrayList<String> getFeatures() {
		return features;
	}

	public void setFeatures(ArrayList<String> features) {
		this.features = features;
	}

	/**
	 * @return the calories
	 */
	public double getCalories() {
		return calories;
	}

	/**
	 * @param calories the calories to set
	 */
	public void setCalories(double calories) {
		this.calories = calories;
	}

	@Override
	public int compareTo(USDAItem o) {
		if (calories < o.getCalories()) {
			return -1;
		} else if (calories == o.getCalories()) {
			return 0;
		}
		return 1;
	}
	
	public FSTNode.type getFSTType() {
		return FSTNode.type.Item;
	}
	
	public void compressTree(ArrayList<String> attributesKept, ArrayList<String> attributesRemoved, boolean first) {
		
	}
	
	public ArrayList<ReturnableItem> itemsUpstream () {
		ArrayList<ReturnableItem> toreturn = new ArrayList<ReturnableItem>();
		toreturn.add(this);
		return toreturn;
	}

	@Override
	public int depth() {
		return 1;
	}

	@Override
	public ArrayList<String> attributesUpstream() {
		return new ArrayList<String>();
	}

	@Override
	public void hide() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public ArrayList<String> getAllFoodIDs() {
		ArrayList<String> toReturn = new ArrayList<String>();
		toReturn.add(foodID);
		return toReturn;
	}

	/**
	 * @return the protein
	 */
	public double getProtein() {
		return protein;
	}

	/**
	 * @param protein the protein to set
	 */
	public void setProtein(double protein) {
		this.protein = protein;
	}

	/**
	 * @return the fat
	 */
	public double getFat() {
		return fat;
	}

	/**
	 * @param fat the fat to set
	 */
	public void setFat(double fat) {
		this.fat = fat;
	}

	/**
	 * @return the cholesterol
	 */
	public double getCholesterol() {
		return cholesterol;
	}

	/**
	 * @param cholesterol the cholesterol to set
	 */
	public void setCholesterol(double cholesterol) {
		this.cholesterol = cholesterol;
	}

	/**
	 * @return the sodium
	 */
	public double getSodium() {
		return sodium;
	}

	/**
	 * @param sodium the sodium to set
	 */
	public void setSodium(double sodium) {
		this.sodium = sodium;
	}

	/**
	 * @return the carbohydrates
	 */
	public double getCarbohydrates() {
		return carbohydrates;
	}

	/**
	 * @param carbohydrates the carbohydrates to set
	 */
	public void setCarbohydrates(double carbohydrates) {
		this.carbohydrates = carbohydrates;
	}

	/**
	 * @return the fiber
	 */
	public double getFiber() {
		return fiber;
	}

	/**
	 * @param fiber the fiber to set
	 */
	public void setFiber(double fiber) {
		this.fiber = fiber;
	}

	/**
	 * @return the sugars
	 */
	public double getSugars() {
		return sugars;
	}

	/**
	 * @param sugars the sugars to set
	 */
	public void setSugars(double sugars) {
		this.sugars = sugars;
	}
	
	/**
	 * @return the image
	 */
	public String getImage() {
		return image;
	}
	
	/**
	 * @param image the image to set
	 */
	public void setImage(String image) {
		this.image = image;
	}
	
}
