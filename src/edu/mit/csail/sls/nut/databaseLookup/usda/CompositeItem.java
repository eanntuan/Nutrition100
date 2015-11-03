package edu.mit.csail.sls.nut.databaseLookup.usda;

import java.util.ArrayList;


public class CompositeItem extends TreeNode implements FSTNode, ReturnableItem {

	private ArrayList<ReturnableItem> items;
	private String itemName;
	private double calories;
	private ArrayList<String> features;
	private String foodID;
	ArrayList<String> allFoodIds;
	private String longDesc;
	
	public CompositeItem (ArrayList<ReturnableItem> items){
		super();
		this.setItems(items);
		type=nodeType.LEAF;
		this.itemName = items.get(0).getItemName();
		this.allFoodIds = new ArrayList<String>();
		double totalCalories = 0;
		for (ReturnableItem currentItem: items) {
			totalCalories += currentItem.getCalories();
			allFoodIds.add(currentItem.getFoodID());
		}
		this.calories = totalCalories*1.0/items.size();
		
		//Get common features
		this.features = new ArrayList<String>();
		ArrayList<String> firstFeatures = items.get(0).getFeatures();
		for (int i=0; i<firstFeatures.size(); i++) {
			boolean inall = true;
			String currentFeature = firstFeatures.get(i);
			for (int j=1; j<items.size(); j++) {
				if (!items.get(j).hasFeature(currentFeature)) {
					inall = false;
					break;
				}
			}
			if (inall) {
				this.features.add(currentFeature);
			}
		}
		System.out.println("Composite item made" + features);
		this.longDesc = itemName+",";
		for (String currentFeature: features) {
			longDesc+= " "+currentFeature+",";
		}
		this.longDesc = this.longDesc.substring(0, this.longDesc.length()-1);
		this.longDesc=this.longDesc.replaceAll("\"", "");
		this.foodID = items.get(0).getFoodID();
	}


	public String toString (){
		return itemName + features.toString()+"("+allFoodIds+")";
//		return itemName + features.toString()+"("+calories+")";
	
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
	
	public double getCalories () {
		return calories;
	}




	@Override
	public boolean hasFeature(String currentFeature) {
		return features.contains(currentFeature);
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


	/**
	 * @return the items
	 */
	public ArrayList<ReturnableItem> getItems() {
		return items;
	}


	/**
	 * @param items the items to set
	 */
	public void setItems(ArrayList<ReturnableItem> items) {
		this.items = items;
	}


	@Override
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
	
	@Override
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
	 * @return the foodID
	 */
	public String getFoodID() {
		return foodID;
	}


	/**
	 * @param foodID the foodID to set
	 */
	public void setFoodID(String foodID) {
		this.foodID = foodID;
	}


	/**
	 * @return the longDesc
	 */
	public String getLongDesc() {
		return longDesc;
	}


	/**
	 * @param longDesc the longDesc to set
	 */
	public void setLongDesc(String longDesc) {
		this.longDesc = longDesc;
	}
	
	public ArrayList<String> getAllFoodIDs () {
		return allFoodIds;
	}
}
