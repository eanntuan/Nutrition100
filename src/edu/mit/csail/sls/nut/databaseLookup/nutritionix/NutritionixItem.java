package edu.mit.csail.sls.nut.databaseLookup.nutritionix;

//Classes to decode responses from Nutritionix

class NutritionixResponseHit {
	String _index;
	String _type;
	String _id;
	String _score;
	NutritionixItem fields;

	public String toString() {
		return "\n" + fields;
	}

	public String get_index() {
		return _index;
	}

	public void set_index(String _index) {
		this._index = _index;
	}

	public String get_type() {
		return _type;
	}

	public void set_type(String _type) {
		this._type = _type;
	}

	public String get_id() {
		return _id;
	}

	public void set_id(String _id) {
		this._id = _id;
	}

	public String get_score() {
		return _score;
	}

	public void set_score(String _score) {
		this._score = _score;
	}

	public NutritionixItem getFields() {
		return fields;
	}

	public void setFields(NutritionixItem fields) {
		this.fields = fields;
	}

}

public class NutritionixItem {

	private String item_id;
	private String item_name;
	private String brand_id;
	private String brand_name;
	private String nf_serving_size_qty;
	private String nf_serving_size_unit;
	private String nf_calories;

	public String getNf_serving_size_qty() {
		return nf_serving_size_qty.replaceAll(",", "");
	}

	public void setNf_serving_size_qty(String nf_serving_size_qty) {
		if (nf_serving_size_qty == null) {
			this.nf_serving_size_qty = "-1";
		} else {
			this.nf_serving_size_qty = nf_serving_size_qty;
		}
	}

	public String getNf_serving_size_unit() {
		return nf_serving_size_unit.replaceAll(",", "");
	}

	public void setNf_serving_size_unit(String nf_serving_size_unit) {
		if (nf_serving_size_unit == null) {
			this.nf_serving_size_unit = "NULL";
		} else {
			this.nf_serving_size_unit = nf_serving_size_unit;
		}
	}

	public String toString() {
		return "item: " + item_name + " brand: " + brand_name;
	}

	public String getItem_id() {
		return item_id;
	}

	public void setItem_id(String item_id) {
		this.item_id = item_id;
	}

	public String getItem_name() {
		return item_name.replaceAll(",", "");
	}

	public void setItem_name(String item_name) {
		this.item_name = item_name;
	}

	public String getBrand_id() {
		return brand_id;
	}

	public void setBrand_id(String brand_id) {
		this.brand_id = brand_id;
	}

	public String getBrand_name() {
		return brand_name.replaceAll(",", "");
	}

	public void setBrand_name(String brand_name) {
		this.brand_name = brand_name;
	}

	/**
	 * @return the nf_calories
	 */
	public String getNf_calories() {
		return nf_calories;
	}

	/**
	 * @param nf_calories the nf_calories to set
	 */
	public void setNf_calories(String nf_calories) {
		this.nf_calories = nf_calories;
	}


}

class NutritionixBrandResponse {
	String total;
	String max_score;
	NutritionixBrandResponseHit[] hits;

	public NutritionixBrandResponse() {

	}

	public String toString() {

		String toreturn = "Total: " + total + " max_score:" + max_score
				+ " hits:";
		for (int i = 0; i < hits.length; i++) {
			toreturn += hits[i].toString();
		}
		return toreturn;
	}

	public String getTotal() {
		return total;
	}

	public String getMax_score() {
		return max_score;
	}

	public NutritionixBrandResponseHit[] getHits() {
		return hits;
	}

}

class NutritionixBrandResponseHit {
	String _index;
	String _type;
	String _id;
	String _score;
	NutritionixBrand fields;

	public String toString() {
		return "\n" + fields;
	}

	public String get_index() {
		return _index;
	}

	public void set_index(String _index) {
		this._index = _index;
	}

	public String get_type() {
		return _type;
	}

	public void set_type(String _type) {
		this._type = _type;
	}

	public String get_id() {
		return _id;
	}

	public void set_id(String _id) {
		this._id = _id;
	}

	public String get_score() {
		return _score;
	}

	public void set_score(String _score) {
		this._score = _score;
	}

	public NutritionixBrand getFields() {
		return fields;
	}

	public void setFields(NutritionixBrand fields) {
		this.fields = fields;
	}

}

class NutritionixBrand {

	private String name;
	private String website;
	private String type;
	private String _id;
	private String total_items;

	public String toString() {
		return "name: " + name + " id: " + _id;
	}

	public String getName() {
		return name;
	}

	public String getWebsite() {
		return website;
	}

	public String getType() {
		return type;
	}

	public String get_id() {
		return _id;
	}

	public String getTotal_items() {
		return total_items;
	}

}
