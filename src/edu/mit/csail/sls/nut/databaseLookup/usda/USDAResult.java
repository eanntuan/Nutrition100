package edu.mit.csail.sls.nut.databaseLookup.usda;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class USDAResult {

	private ArrayList<String> features;
	private ArrayList<ReturnableItem> results;
	private ArrayList<USDAWeight> weights;
	private int levelUsed;
	private ArrayList<String> originalDescription;
	private String originalBrand;
	private String quantity; //Full string given
	private String foodID;
	private String imagePath;
	private double quantityAmount=1;
	boolean quantityMatchFound=false;
	private boolean adjectivesRelevant=true;
	
	
	public USDAResult(ArrayList<String> features,
			ArrayList<ReturnableItem> results, ArrayList<USDAWeight> weights, int level, 
			ArrayList<String> originalDescription, String originalBrand, String quantity, boolean adjectivesRelevant) {
		this.features = features;
		this.results = results;
		this.setWeights(weights);
		this.setLevelUsed(level);
		this.setOriginalBrand(originalBrand);
		this.setOriginalDescription(originalDescription);
		this.quantity=quantity.toLowerCase();
		updateQuantityAmount();
		updateQuantityWeight();
		this.setAdjectivesRelevant(adjectivesRelevant);
	}
	
	private void updateQuantityAmount () {
		System.out.println("Quantity being parsed:"+quantity);
		if (quantity.length()>0 && !quantity.equals("a")) {
			//First check for fraction
		Matcher matcher = Pattern.compile("(\\d+)?[/]\\d+").matcher(this.quantity);
		if(matcher.find()) {
			String[] fraction=matcher.group().split("/");
			quantityAmount=Double.parseDouble(fraction[0])/Double.parseDouble(fraction[1]);
		}  else {
			//Otherwise check for plain digits
			matcher = Pattern.compile("\\d+").matcher(this.quantity);
			if(matcher.find()) {
				quantityAmount=Double.parseDouble(matcher.group());
			} else {
				if  (quantity.contains("two thirds")) {
					quantityAmount=2.0/3;
				} else if  (quantity.contains("three quarters")) {
					quantityAmount=.75;
				} else if (quantity.contains("two")) {
					quantityAmount=2;
				} else if  (quantity.contains("three")) {
					quantityAmount=3;
				} else if  (quantity.contains("four")) {
					quantityAmount=4;
				}else if  (quantity.contains("five")) {
					quantityAmount=5;
				}else if  (quantity.contains("six")) {
					quantityAmount=6;
				}else if  (quantity.contains("seven")) {
					quantityAmount=7;
				}else if  (quantity.contains("eight")) {
					quantityAmount=8;
				}else if  (quantity.contains("nine")) {
					quantityAmount=9;
				}else if  (quantity.contains("ten")) {
					quantityAmount=10;
				}else if  (quantity.contains("half")) {
					quantityAmount=.5;
				}else if  (quantity.contains("quarter")) {
					quantityAmount=.25;
				}else if  (quantity.contains("third")) {
					quantityAmount=1.0/3;
				}
			}
		}
		}
	}
	
	private void updateQuantityWeight() {
		System.out.println("WEIGHTS: " + weights);
		if (weights==null) {
			weights=new ArrayList<USDAWeight>();
		}
		int relevantWeight = -1;
		int similarWeight = -1;
		for (int i=0; i<weights.size();i++) {
			if (quantity.contains(weights.get(i).getMsre_Desc())) {
				relevantWeight=i;
			} else if ((weights.get(i).getMsre_Desc().equals("tbsp") && quantity.contains("tablespoon")) ||
					(weights.get(i).getMsre_Desc().equals("tsp") && quantity.contains("teaspoon")) ||
					(weights.get(i).getMsre_Desc().equals("oz") && quantity.contains("ounce"))) {
				relevantWeight=i;
			}
			if (relevantWeight<0 && similarWeight<0) {
				String firstWord = weights.get(i).getMsre_Desc().split(" ")[0];
				if (quantity.contains(firstWord) || (firstWord.equals("cup") && quantity.contains("glass"))) {
					similarWeight = i;
				}
			}
		}
		
		if (relevantWeight<0 && similarWeight>=0) {
			relevantWeight=similarWeight;
		}
		
		if (relevantWeight <0 && ((quantity.contains("gram") || quantity.contains(" g ")))) {
			weights.add(new USDAWeight("", "g", 1, 1));
			relevantWeight= weights.size()-1;
		}
		
		if (relevantWeight>=0) {
			USDAWeight tofront=weights.remove(relevantWeight);
			weights.add(0, tofront);
			quantityMatchFound=true;
		} else {
			
			String conversionStartingItem=UnitConverter.containsConversionItem(quantity);
			boolean relevantQuantityFound=false;
			if (conversionStartingItem!=null) {
				for (int i=0; i<weights.size();i++) {
					String conversionFinalItem=UnitConverter.containsConversionItem(weights.get(i).getMsre_Desc());
					if (conversionFinalItem!=null) {
						quantityAmount=UnitConverter.convertUnit(quantityAmount, conversionStartingItem, conversionFinalItem);
						USDAWeight tofront=weights.remove(i);
						weights.add(0, tofront);
						quantityMatchFound=true;
						relevantQuantityFound=true;
						relevantWeight=0;
						break;
					}
				}
			}
			if (!relevantQuantityFound && weights.size()>0) {
				//Default to most reasonable serving size (defined as either 1 NLEA, 1 medium or 1 of smallest in that order)
				int nleaFound=-1;
				int mediumFound=-1;
				int smallest=0;
				double smallestAmount=weights.get(0).getGmwgt();
				for (int i=0; i<weights.size();i++) {
					if (weights.get(i).getMsre_Desc().contains("NLEA")) {
						nleaFound=i;
//						break;
					} else if (weights.get(i).getMsre_Desc().contains("medium")) {
						mediumFound=i;
					} else if (weights.get(i).getGmwgt()<smallestAmount) {
						smallest=i;
					}
				}
//				if (nleaFound>=0) {
////					quantityAmount=1;
//					USDAWeight tofront=weights.remove(nleaFound);
//					weights.add(0, tofront);
//					
//				} else 
					if (mediumFound>=0) {
//					quantityAmount=1;
					USDAWeight tofront=weights.remove(mediumFound);
					weights.add(0, tofront);
					
				} else if (smallest>0) {
//					quantityAmount=1;
					USDAWeight tofront=weights.remove(smallest);
					weights.add(0, tofront);
					
				}
			}
		}
		
		if (weights.get(0).getMsre_Desc().equals("serving 3 pancakes")) {
			quantityAmount=quantityAmount/3.0;
		}
	}
	
	
	
	public ArrayList<String> getFeatures() {
		return features;
	}
	public void setFeatures(ArrayList<String> features) {
		this.features = features;
	}
	public ArrayList<ReturnableItem> getResults() {
		return results;
	}
	public void setResults(ArrayList<ReturnableItem> results) {
		this.results = results;
	}
	/**
	 * @return the weights
	 */
	public ArrayList<USDAWeight> getWeights() {
		return weights;
	}
	/**
	 * @param weights the weights to set
	 */
	public void setWeights(ArrayList<USDAWeight> weights) {
		this.weights = weights;
	}
	/**
	 * @return the levelUsed
	 */
	public int getLevelUsed() {
		return levelUsed;
	}
	/**
	 * @param levelUsed the levelUsed to set
	 */
	public void setLevelUsed(int levelUsed) {
		this.levelUsed = levelUsed;
	}
	/**
	 * @return the originalDescription
	 */
	public ArrayList<String> getOriginalDescription() {
		return originalDescription;
	}
	/**
	 * @param originalDescription the originalDescription to set
	 */
	public void setOriginalDescription(ArrayList<String> originalDescription) {
		this.originalDescription = originalDescription;
	}
	/**
	 * @return the originalBrand
	 */
	public String getOriginalBrand() {
		return originalBrand;
	}
	/**
	 * @param originalBrand the originalBrand to set
	 */
	public void setOriginalBrand(String originalBrand) {
		this.originalBrand = originalBrand;
	}
	/**
	 * @return the quantity
	 */
	public String getQuantity() {
		return quantity;
	}
	/**
	 * @param quantity the quantity to set
	 */
	public void setQuantity(String quantity) {
		this.quantity = quantity;
	}

	/**
	 * @return the quantityAmount
	 */
	public double getQuantityAmount() {
		return quantityAmount;
	}

	/**
	 * @param quantityAmount the quantityAmount to set
	 */
	public void setQuantityAmount(double quantityAmount) {
		this.quantityAmount = quantityAmount;
	}

	/**
	 * @return the adjectivesRelevant
	 */
	public boolean isAdjectivesRelevant() {
		return adjectivesRelevant;
	}

	/**
	 * @param adjectivesRelevant the adjectivesRelevant to set
	 */
	public void setAdjectivesRelevant(boolean adjectivesRelevant) {
		this.adjectivesRelevant = adjectivesRelevant;
	}
	
	
}
