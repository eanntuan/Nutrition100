package edu.mit.csail.sls.nut;

//Query to be stored in log
public class NutritionSearchQuery {
	private String foodItem;
	private String brand;
	private String quantity;
	private String description;
	private String result; // Nutritionix ID

	public NutritionSearchQuery(String foodItem, String brand, String quantity,
			String description, String result) {
		this.foodItem = foodItem;
		this.brand = brand;
		this.quantity = quantity;
		this.description = description;
		this.result = result;
	}

	/**
	 * @return the foodItem
	 */
	public String getFoodItem() {
		return foodItem;
	}

	/**
	 * @param foodItem
	 *            the foodItem to set
	 */
	public void setFoodItem(String foodItem) {
		this.foodItem = foodItem;
	}

	/**
	 * @return the brand
	 */
	public String getBrand() {
		return brand;
	}

	/**
	 * @param brand
	 *            the brand to set
	 */
	public void setBrand(String brand) {
		this.brand = brand;
	}

	/**
	 * @return the quantity
	 */
	public String getQuantity() {
		return quantity;
	}

	/**
	 * @param quantity
	 *            the quantity to set
	 */
	public void setQuantity(String quantity) {
		this.quantity = quantity;
	}

	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @param description
	 *            the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * @return the result
	 */
	public String getResult() {
		return result;
	}

	/**
	 * @param result
	 *            the result to set
	 */
	public void setResult(String result) {
		this.result = result;
	}
	
	public String toString () {
		return "Food Item:" + foodItem + "Brand:" + brand + "Quantity:"+ quantity
				+ "Description:" + description + "Result:" + result;
	}

}
