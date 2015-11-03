package edu.mit.csail.sls.nut.databaseLookup.semantics3;

public class Semantics3NutritionObject {

	private String name;
	private String brand;
	private String features;
	private String imgUrl="";
	
	public Semantics3NutritionObject(String name, String brand,
			String features) {
		this.name = name;
		this.brand = brand;
		this.features = features;
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getBrand() {
		return brand;
	}
	public void setBrand(String brand) {
		this.brand = brand;
	}
	public String getFeatures() {
		return features;
	}
	public void setFeatures(String features) {
		this.features = features;
	}
	
	public String toString () {
		return "Name: " + name + ", Brand:" + brand;
	}

	/**
	 * @return the imgUrl
	 */
	public String getImgUrl() {
		return imgUrl;
	}

	/**
	 * @param imgUrl the imgUrl to set
	 */
	public void setImgUrl(String imgUrl) {
		this.imgUrl = imgUrl;
	}
	
}
