package edu.mit.csail.sls.nut.databaseLookup.nutritionix;

import java.util.ArrayList;

import edu.mit.csail.sls.nut.NutritionContext;

//Queries to be sent via POST to Nutritionix
interface NutritionixQuery {

}

abstract class NutritionixItemQuery implements NutritionixQuery {
//	String appKey = NutritionContext.getNutritionixAppKey();
//	String appId = NutritionContext.getNutritionixAppID();
	static String appKey = "fa6c29b2dd2a10e654ed207aa017d657";
	static String appId = "cdc2f3cf";

	ArrayList<String> fields = new ArrayList<>();

	// String results = "0:20";

	NutritionixItemQuery() {
		fields.add("item_name");
		fields.add("brand_name");
		fields.add("item_id");
		fields.add("brand_id");
		fields.add("nf_serving_size_qty");
		fields.add("nf_serving_size_unit");
		fields.add("nf_calories");
	}

}

class SimpleItemQuery extends NutritionixItemQuery implements NutritionixQuery {

	String query;
//	notTypeFilter filters;
	String appKey = NutritionixLookup.appKey;
	String appId = NutritionixLookup.appId;

	public SimpleItemQuery(String p_query) {
		super();
		query = p_query;
//		filters=new notTypeFilter();

	}
}

//class notTypeFilter implements NutritionixQuery {
//	itemTypeFilter not;
//
//	public notTypeFilter() {
//		not=new itemTypeFilter();
//	}
//
//}

//class itemTypeFilter implements NutritionixQuery {
//	int item_type;
//
//	public itemTypeFilter() {
//		item_type=2;
//	}
//
//}

class brandItemQuery implements NutritionixQuery {
	String brand_id;
//	int item_type;
	

	public brandItemQuery(String brand) {
		brand_id = brand;
//		item_type=1;
	}

}

class ItemQuerywithBrand extends NutritionixItemQuery implements
		NutritionixQuery {

	String query;
	String appKey = NutritionixLookup.appKey;
	String appId = NutritionixLookup.appId;
	brandItemQuery filters;

	public ItemQuerywithBrand(String p_query, String brand) {
		super();
		query = p_query;
		filters = new brandItemQuery(brand);

	}
}
