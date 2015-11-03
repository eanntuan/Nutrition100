//package edu.mit.csail.sls.nut.databaseLookup.semantics3;
//
//import java.io.IOException;
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.Map;
//
////import oauth.signpost.exception.OAuthCommunicationException;
////import oauth.signpost.exception.OAuthExpectationFailedException;
////import oauth.signpost.exception.OAuthMessageSignerException;
//
//import org.json.JSONArray;
//import org.json.JSONObject;
//
//import com.semantics3.api.*;
//
//import edu.mit.csail.sls.nut.Segment;
//
//public class Semantics3Lookup {
//
//	public final static String semantic3Key = "SEM3518D541CF320BF54734DC42B30C6EE32";
//	public final static String semantic3Secret = "YjkyOTA5NjAyYzg2NjFmMWIzMjBmMmViYzkwZGNlZjU";
//
//	public static Map<String, ArrayList<Semantics3NutritionObject>> foodItemLookup(
//			Map<String, ArrayList<Segment>> dependencies,
//			ArrayList<String> tokens) {
//		Map<String, ArrayList<Semantics3NutritionObject>> foodItems = new HashMap<String, ArrayList<Semantics3NutritionObject>>();
//		for (String item : dependencies.keySet()) {
//			// Get associated values
//			String brand = "";
//			String description = "";
//			String quantity = "";
//			for (Segment s : dependencies.get(item)) {
//				System.out.println(s.label);
//				if (s.label.equals("Brand")) {
//					for (int i = s.start; i < s.end; i++) {
//						// don't include "from" as part of brand name
//						if (tokens.get(i).equals("from")) {
//							continue;
//						}
//						brand += " " + tokens.get(i);
//					}
//
//				}
//				if (s.label.equals("Quantity")) {
//					for (int i = s.start; i < s.end; i++) {
//						quantity += " " + tokens.get(i);
//					}
//				}
//				// if brand labeled "Trader", make it "Trader Joe's"
//				if (brand.equals("Trader") || brand.equals(" Trader")) {
//					brand += " Joe's";
//				}
//				if (s.label.equals("Description")) {
//					for (int i = s.start; i < s.end; i++) {
//						description += " " + tokens.get(i);
//					}
//
//				}
//			}
//
//			// remove numeric values (i.e. indices) from food item
//			String itemNoIndices = item.replaceAll("[^A-Za-z]", "");
//
//			ArrayList<Semantics3NutritionObject> results = lookUpProduct(
//					itemNoIndices, brand, description);
//			foodItems.put(item, results);
//		}
//
//		return foodItems;
//	}
//
//	public static ArrayList<Semantics3NutritionObject> lookUpProduct(
//			String fooditem, String brand, String description) {
//		Products products = new Products(semantic3Key, semantic3Secret);
//
//		products.productsField("cat_id", 18203).productsField("name", fooditem);
//		if (!brand.equals("")) {
//			products.productsField("brand", brand);
//		}
//		if (!description.equals("")) {
//			products.productsField("description", description);
//		}
//
//		System.out.println("Semantics3 query food item:" + fooditem
//				+ " brand: " + brand + " description:" + description);
//		JSONObject results;
//		ArrayList<Semantics3NutritionObject> nutritionResults = new ArrayList<Semantics3NutritionObject>();
//		try {
//			results = products.getProducts();
//			JSONArray items = (JSONArray) results.get("results");
//			for (int i = 0; i < items.length(); i++) {
//				JSONObject currentitem = (JSONObject) items.get(i);
//				String currentName = "";
//				String currentBrand = "";
//				String currentFeatures = "";
//				if (currentitem.has("name")) {
//					currentName = currentitem.get("name").toString();
//				}
//				if (currentitem.has("brand")) {
//					currentBrand = currentitem.get("brand").toString();
//				}
//				if (currentitem.has("features")) {
//					currentFeatures = currentitem.get("features").toString();
//				}
//				Semantics3NutritionObject nutObject = new Semantics3NutritionObject(
//						currentName, currentBrand, currentFeatures);
//				if (currentitem.has("images")) {
//					JSONArray imgArray = currentitem.getJSONArray("images");
//					nutObject.setImgUrl(imgArray.getString(0));
//				}
//				if (currentitem.has("features") && ((JSONObject)currentitem.get("features")).has("Calories")) {
//					nutritionResults.add(nutObject);
//				}
//				
//				// System.out.println(currentitem.get("name"));
//			}
//		} catch (OAuthMessageSignerException | OAuthExpectationFailedException
//				| OAuthCommunicationException | IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//
//		return nutritionResults;
//	}
//
//	public static void main(String[] args) {
//		System.out.println(lookUpProduct("oats", "", "steel cut"));
//	}
//
//	public static Map<String, String> createImgMap(
//			Map<String, ArrayList<Semantics3NutritionObject>> semantic3results) {
//		Map<String, String> imgMap = new HashMap<String, String>();
//		for (String item : semantic3results.keySet()) {
//			if (semantic3results.get(item).size() > 0) {
//				imgMap.put(item, semantic3results.get(item).get(0).getImgUrl());
//			}
//		}
//		return imgMap;
//	}
//
//}
