package edu.mit.csail.sls.nut.databaseLookup.usda;

import java.util.ArrayList;

public class FeatureStatistics implements Comparable<FeatureStatistics>{

	private int count;
	private double mean;
	private double variance;
	private String feature;
	
	public FeatureStatistics (String feature, ArrayList<ReturnableItem> arrayList) {
		this.feature = feature;
		count= arrayList.size();
		int total=0;
		for (ReturnableItem currentItem: arrayList) {
			total+= currentItem.getCalories();
		}
		mean= total*1.0/arrayList.size();
		variance = calculateVariance(arrayList);
		
	}
	
	@Override
	public int compareTo(FeatureStatistics o) {
		if (variance < o.getVariance()) {
			return -1;
		} else if (variance == o.getVariance()) {
			return 0;
		}
		return 1;
	}
	
	private double calculateVariance (ArrayList<ReturnableItem> arrayList) {
	        double temp = 0;
	        for(ReturnableItem currentItem: arrayList) {
	        	temp += (mean-currentItem.getCalories())*(mean-currentItem.getCalories());
	        }
	        return temp/arrayList.size();
	}

	public int getCount() {
		return count;
	}

	public double getMean() {
		return (int)mean;
	}

	public double getVariance() {
		return (int)variance;
	}

	/**
	 * @return the feature
	 */
	public String getFeature() {
		return feature;
	}

	/**
	 * @param feature the feature to set
	 */
	public void setFeature(String feature) {
		this.feature = feature;
	}
}
