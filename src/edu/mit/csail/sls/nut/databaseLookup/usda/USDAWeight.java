package edu.mit.csail.sls.nut.databaseLookup.usda;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;


public class USDAWeight implements Comparable<USDAWeight> {

	private HashMap<String, Double> foodIDs;
	private String Msre_Desc;
	private double gmwgt;
	private double amount;
	
	public USDAWeight (String p_foodID, String p_msreDesc, double p_gmwgt, double amount){
		foodIDs = new HashMap<String, Double>();
		foodIDs.put(p_foodID, p_gmwgt);
		this.setMsre_Desc(p_msreDesc);
		this.setGmwgt(p_gmwgt);
		this.setAmount(amount);
	}

	public USDAWeight() {
	}

	/**
	 * @return the foodID
	 */
	public ArrayList<String> getFoodIDs() {
		return new ArrayList<String> (foodIDs.keySet());
	}

	/**
	 * @param foodID the foodID to set
	 */
	public void addFoodID(String foodID, double gmwgt) {
		this.foodIDs.put(foodID, gmwgt);
	}

	/**
	 * @return the msre_Desc
	 */
	public String getMsre_Desc() {
		return Msre_Desc;
	}

	/**
	 * @param msre_Desc the msre_Desc to set
	 */
	public void setMsre_Desc(String msre_Desc) {
		Msre_Desc = msre_Desc;
	}


	public String toString () {
		return Msre_Desc;
	}

	@Override
	public int compareTo(USDAWeight o) {
		// TODO Auto-generated method stub
		return Msre_Desc.compareTo(o.getMsre_Desc());
	}

	/**
	 * @return the gmwgt
	 */
	public double getGmwgt() {
		return gmwgt;
	}

	/**
	 * @param gmwgt the gmwgt to set
	 */
	public void setGmwgt(double gmwgt) {
		this.gmwgt = gmwgt;
	}

	/**
	 * @return the amount
	 */
	public double getAmount() {
		return amount;
	}

	/**
	 * @param amount the amount to set
	 */
	public void setAmount(double amount) {
		this.amount = amount;
	}
}
