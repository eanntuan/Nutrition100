package edu.mit.csail.sls.nut.databaseLookup.usda;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.atteo.evo.inflector.English;

import com.sun.org.apache.bcel.internal.generic.NEWARRAY;

import edu.mit.csail.sls.nut.GetImages;
import edu.mit.csail.sls.nut.Segment;

public class USDALookup {

	// JDBC driver name and database URL
	// static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
	// static final String DB_URL = "jdbc:mysql://mysql.csail.mit.edu/FNDDS";
	static final String DB_URL = "jdbc:mysql://mysql.csail.mit.edu/nutritionData";

	// Database credentials
	static final String USER = "slsNutrition";
	static final String PASS = "slsNutrition";

	/**
	 * Overall food item lookup with levels as specified for widening search
	 * parameters
	 * @param foodItem
	 * @param brand
	 * @param descriptions
	 * @param startingLevel
	 * @param quantity
	 * @param addedAdjectives
	 * @return
	 */

	public static USDAResult leveledFoodItemLookup(String foodItem,
			String brand, ArrayList<String> descriptions, int startingLevel, String quantity, ArrayList<String> addedAdjectives) {

		System.out.println("Lookup is called on "+foodItem+" at level "+startingLevel);
		System.out.println("Description: "+descriptions);
		System.out.println("Added adjectives: "+addedAdjectives);
		int currentLevel = startingLevel;
		ArrayList<ReturnableItem> results = new ArrayList<ReturnableItem>();
		Stemmer stemmer = new Stemmer();
		for (char ch : foodItem.toCharArray()) {
			stemmer.add(ch);
		}
		stemmer.stem();
		String singularItem = stemmer.toString();
		String pluralItem = English.plural(foodItem);

		// Create description string for use later
		String descriptionString = "";
		for (String i : descriptions) {
			descriptionString += " " + i;
		}
		if (descriptionString.length() > 0) {
			descriptionString = descriptionString.substring(1);
		}
		
		boolean adjectivesrelevant=true;

		while (results.isEmpty() && currentLevel<14) {

			switch (currentLevel) {
			// To begin, check the cache for matching options
			case 0: {
				//System.out.println("case 0");
				// First, check if the full brand and description are found, or
				// move on if brand or description are empty
				if (!(descriptions.isEmpty() || brand.equals(""))) {
					results = findFreeBaseEquiv(brand + " " + descriptionString
							+ " " + foodItem, brand + " " + descriptionString
							+ " " + singularItem, brand + " "
							+ descriptionString + " " + pluralItem);
					if (results.size()==0) {
						results = findNutritionixEquiv(brand + " " + descriptionString
								+ " " + foodItem, brand + " " + descriptionString
								+ " " + singularItem, brand + " "
								+ descriptionString + " " + pluralItem);
					}
				} else if (! brand.equals("")) {
					results = findFreeBaseEquiv(brand 
							+ " " + foodItem, brand 
							+ " " + singularItem, brand + " "
							+ descriptionString + " " + pluralItem);
					if (results.size()==0) {
						results = findNutritionixEquiv(brand 
								+ " " + foodItem, brand 
								+ " " + singularItem, brand + " "
								+ descriptionString + " " + pluralItem);
					}
				}
				break;
			}
			case 1: {
				//System.out.println("case 1");
				// Next check for the full description and food item
				if (!descriptions.isEmpty()) {
					results = findFreeBaseEquiv(descriptionString + " "
							+ foodItem, descriptionString + " "
							+ singularItem, brand + " "
							+ descriptionString + " " + pluralItem);
					if (results.size()==0) {
						results = findNutritionixEquiv(descriptionString + " "
								+ foodItem, descriptionString + " "
								+ singularItem, brand + " "
								+ descriptionString + " " + pluralItem);
					}
				}
				break;
			}
			
			case 2: {
				//System.out.println("case 2");
				//Check if a cached result is found with just the first description
				if (!descriptions.isEmpty()) {
					 results =findFreeBaseEquiv(descriptions.get(0)+" "+foodItem,
					 descriptions.get(0)+" "+singularItem,
					 descriptions.get(0)+" "+pluralItem);
					 if (results.size()==0) {
						 results =findNutritionixEquiv(descriptions.get(0)+" "+foodItem,
								 descriptions.get(0)+" "+singularItem,
								 descriptions.get(0)+" "+pluralItem);
						}
				}
				break;
			}
			
			case 3: {
				//System.out.println("case 3");
				//Check if a cached result for just the food item exists
				if (descriptions.isEmpty()) {
				results =findFreeBaseEquiv(foodItem, singularItem, pluralItem);
				if (results.size()==0) {
					results =findNutritionixEquiv(foodItem, singularItem, pluralItem);
					}
				}
				break;
			}
			
			//If no result is found in the cache, move onto the USDA SR database
			case 4: {
				System.out.println("case 4");
				if (!descriptions.isEmpty() && !brand.isEmpty()) {
				// Food item and description begins entry, brand exact feature in adjectives
				ArrayList<ReturnableItem> tempResults = executeItemFirstPartialQuery(
						descriptionString + " " + foodItem, descriptionString
								+ " " + singularItem,
						descriptionString + " " + pluralItem);
				for (ReturnableItem currentItem : tempResults) {
					if (currentItem.hasIdenticalFeature(brand)) {
						results.add(currentItem);
					}
				}
				}
				break;
			}
			
			case 5: {
				System.out.println("case 5");
				// Food item and description begins entry, brand similar feature in adjectives
				if (!descriptions.isEmpty() && !brand.isEmpty()) {
				ArrayList<ReturnableItem> tempResults = executeItemFirstPartialQuery(
						descriptionString + " " + foodItem, descriptionString
								+ " " + singularItem,
						descriptionString + " " + pluralItem);
				for (ReturnableItem currentItem : tempResults) {
					if (currentItem.hasSimilarFeature(brand)) {
						results.add(currentItem);
					}
				}
				}
				break;
			}
			
			case 6: {
				System.out.println("case 6");
				//Food item and description begins entry,  brand not in entry
				if (!descriptions.isEmpty()) {
				results = executeItemFirstPartialQuery(
						descriptionString + " " + foodItem, descriptionString
								+ " " + singularItem,
						descriptionString + " " + pluralItem);
				}
				break;
			}
			
			case 7: {
				System.out.println("case 7");
				//Food item and description found exactly in entry
				if (!descriptions.isEmpty()) {
				results = executeItemPartialQuery(
						descriptionString + " " + foodItem, descriptionString
								+ " " + singularItem,
						descriptionString + " " + pluralItem);
				}
				adjectivesrelevant=false;
				break;
			}
			
			case 8: {
				System.out.println("case 8");
				//Food item begins entry, exact brand and description in adjectives
				ArrayList<ReturnableItem> tempResults = executeItemFirstPartialQuery(foodItem, singularItem, pluralItem);
				for (ReturnableItem currentItem : tempResults) {
					if (currentItem.hasIdenticalFeature(brand)) {
						for (String description: descriptions) {
							if (currentItem.hasIdenticalFeature(description)) {
								results.add(currentItem);
								break;
							}
						}
					}
				}
				adjectivesrelevant=true;
				break;
			}
			
			case 9: {
				System.out.println("Case 9");
				// Food item begins entry, exact brand in adjectives
				if (!brand.equals("")) {
					ArrayList<ReturnableItem> tempResults = executeItemFirstPartialQuery(
							foodItem, singularItem, pluralItem);
					for (ReturnableItem currentItem : tempResults) {
						if (currentItem.hasIdenticalFeature(brand)) {
							results.add(currentItem);
						}
					}
				}
				adjectivesrelevant=true;
				break;
			}

			case 10: {
				System.out.println("Case 10");
				//Food item begins entry, exact description in adjectives
				ArrayList<ReturnableItem> tempResults = executeItemFirstPartialQuery(foodItem, singularItem, pluralItem);
				for (ReturnableItem currentItem : tempResults) {
						for (String description: descriptions) {
							if (currentItem.hasIdenticalFeature(description)) {
								results.add(currentItem);
								System.out.println("Item with feature "+description+": "+currentItem);
								break;
							} else {
								System.out.println("Item without feature "+description+": "+currentItem);
							}
					}
				}
				adjectivesrelevant=true;
				break;
			}
			
			case 11: {
				System.out.println("case 11");
				//Food item begins entry, partial match to brand or description in adjectives
				ArrayList<ReturnableItem> tempResults = executeItemFirstPartialQuery(foodItem, singularItem, pluralItem);
				for (ReturnableItem currentItem : tempResults) {
					if ((!brand.isEmpty())&&currentItem.hasSimilarFeature(brand)) {
						results.add(currentItem);
					} else {
						for (String description: descriptions) {
							if (currentItem.hasSimilarFeature(description)) {
								System.out.println("Item with feature "+description+": "+currentItem);
								results.add(currentItem);
								break;
							} else {
								System.out.println("Item without feature "+description+": "+currentItem);
							}
						}
					}
				}
				break;
			}
			
			case 12: {
				System.out.println("case 12");
				//Food item begins entry
				results = executeItemFirstPartialQuery(foodItem, singularItem, pluralItem);
				break;
			}
			
			case 13: {
				System.out.println("case 13");
				//Food item anywhere in entry
				results = executeItemPartialQuery(foodItem, singularItem, pluralItem);
				adjectivesrelevant=false;
				break;
			}			
			}
			
			//At each given level, do filter for specific adjectives specified
			if (addedAdjectives.size()>0 && results.size()>0) {
				ArrayList<ReturnableItem> tempResults = new ArrayList<ReturnableItem>();
				for (ReturnableItem currentItem : results) {
					boolean found=true;
						for (String description: addedAdjectives) {
							if (!currentItem.hasIdenticalFeature(description)) {
								found=false;
								break;
							} 
						}
						if (found) {
							tempResults.add(currentItem);
					}
				}
				if (!tempResults.isEmpty()) {
					results=tempResults;
				}
			}
			currentLevel++;
		}
		return generateAdjectives(foodItem, descriptions, results, brand, currentLevel-1, quantity, adjectivesrelevant);
	}

	/**
	 * Generates adjectives for narrowing down from list of options as well as
	 * final usda result
	 */

	public static USDAResult generateAdjectives(String itemName,
			ArrayList<String> adjectivesSpecified,
			ArrayList<ReturnableItem> results, String brand, int level, String quantity, boolean adjectivesRelevant) {
		if (results.isEmpty()) {
			return new USDAResult(new ArrayList<String>(), results,
					new ArrayList<USDAWeight>(), level, adjectivesSpecified, brand, quantity, true);
		}
		
		if (results.size()==1 && results.get(0).getFoodID().equals("-1")) {
			//COrrect item is from Nutritonix
			ArrayList<USDAWeight> weights = new ArrayList<USDAWeight> ();
			weights.add(getNutritionixWeight((NutritionixItem) results.get(0)));
			System.out.println("Calories at current: "+results.get(0).getCalories());
			return new USDAResult(new ArrayList<String>(), results,
					weights, level, adjectivesSpecified, brand, quantity, true);
			
		}
		
		if (results.size()==1) {
			return new USDAResult(new ArrayList<String>(), results,
					getRelevantQuantities(results), level, adjectivesSpecified, brand, quantity, true);
		}
		
		ArrayList<FSTNode> fstTree = FSTGenerator.makeTree(results);
		AttributeNode root = new AttributeNode(itemName);

		for (FSTNode node : fstTree) {
			root.addNext(node);
		}

		ArrayList<FSTNode> rootList = new ArrayList<FSTNode>();
		rootList.add(root);

		ArrayList<String> attributesKept = new ArrayList<String>();
		ArrayList<String> attributesRemoved = new ArrayList<String>();
		root.compressTree(attributesKept, attributesRemoved, true);

		attributesKept.remove(itemName);
		Set<String> keptSet = new HashSet<String>();
		for (String currentAttribute : attributesKept) {
			if (!currentAttribute.contains("NS")
					&& !currentAttribute.contains("NFS")
					&& !adjectivesSpecified.contains(currentAttribute)) {
				boolean matchfound = false;
				for (String adjective : adjectivesSpecified) {
					if (adjective.replaceAll("\\W", "").equals(
							currentAttribute.replaceAll("\\W", ""))) {
						matchfound = true;
						break;
					}
				}
				if (!matchfound) {
					// keptSet.add(currentAttribute.replaceAll("from ", ""));
					keptSet.add(currentAttribute);
				}
			}
		}

		ArrayList<String> keptAttributes = new ArrayList<String>(keptSet);

		Collections.sort(keptAttributes);
		System.out.println(keptAttributes);
		// //Sort by length
		// Collections.sort(keptAttributes, new Comparator<String>() {
		//
		// @Override
		// public int compare(String o1, String o2) {
		// return o1.length()-o2.length();
		// }
		// });
		ArrayList<ReturnableItem> finalitems = root.itemsUpstream();
		return new USDAResult(keptAttributes, finalitems,
				getRelevantQuantities(finalitems), level, adjectivesSpecified, brand, quantity, adjectivesRelevant);

	}

	/**
	 * Perform lookup for a food item, used for initial lookup
	 */
	public static Map<String, USDAResult> foodItemInitialLookup(
			Map<String, ArrayList<Segment>> dependencies,
			ArrayList<String> tokens) {
		System.out.println("Food item initial lookup dependencies:" + dependencies);
		
		Map<String, USDAResult> foodItems = new HashMap<String, USDAResult>();
		
		//System.out.println("Testing if there is an image: " + image);
		
		//Map<String, String> foodImages = new HashMap<String, String>();
		
		System.out.println("Keyset: " + dependencies.keySet());
		//GetImages.setImageName(imageLink);
		
		Set<String> keyset = dependencies.keySet();
		
		for (String item : dependencies.keySet()) {

			foodItems.put(
					item,
					foodItemLookup(item, dependencies.get(item), tokens,
							new ArrayList<String>()));
			//foodImages.put(item, "image here");

		}

		return foodItems;

	}

	/**
	 * Do lookup for a specific food item with specific given adjectives
	 * (initial lookup)
	 */
	public static USDAResult foodItemLookup(String item,
			ArrayList<Segment> dependencies, ArrayList<String> tokens,
			ArrayList<String> adjectivesSpecified) {
		// Get associated values
		ArrayList<String> brand = new ArrayList<String>();
		ArrayList<String> description = new ArrayList<String>();
		 String quantity = "";
		for (Segment s : dependencies) {
			System.out.println("label: " + s.label);
			if (s.label.equals("Brand")) {
				for (int i = s.start; i < s.end; i++) {
					// don't include "from" as part of brand name
					if (tokens.get(i).equals("from")) {
						continue;
					}
					if (tokens.get(i).equals("Trader")
							|| tokens.get(i).equals(" Trader")) {
						brand.add(tokens.get(i) + "Joes");
					} else {
						brand.add(tokens.get(i));
					}
				}

			}
			if (s.label.equals("Quantity")) {
				for (int i = s.start; i < s.end; i++) {
					 quantity += " " + tokens.get(i);
				}
			}
			if (s.label.equals("Description")) {
				for (int i = s.start; i < s.end; i++) {
					description.add(tokens.get(i).replaceAll("[^A-Za-z1-9% ]", "").replaceAll("%", "/%"));
				}

			}
		}
		System.out.println("Description: " + description);
		System.out.println("Brand: " + brand);

		// remove numeric values (i.e. indices) from food item
		String formattedItem = item.replaceAll("[^A-Za-z ]", "");
		
		String brandString = "";
		for (String i : brand) {
			brandString += " " + i;
		}
		if (brandString.length() > 0) {
			brandString = brandString.substring(1);
		}
		String formattedBrand = brandString.replaceAll("[^A-Za-z1-9% ]", "");

//		ArrayList<ReturnableItem> response = new ArrayList<>();
//		Stemmer stemmer = new Stemmer();
//		for (char ch : formattedItem.toCharArray()) {
//			stemmer.add(ch);
//		}
//		stemmer.stem();
//		String singularFormattedItem = stemmer.toString();
//		String pluralFormattedItem = English.plural(formattedItem);
//		String descriptionString = "";
//		for (String i : description) {
//			descriptionString += " " + i;
//		}
//		if (descriptionString.length() > 0) {
//			descriptionString = descriptionString.substring(1);
//		}
//
//		// First check if it is found in freebase database
//		if (description.size() > 0) {
//			response = findFreeBaseEquiv(descriptionString + " "
//					+ formattedItem, descriptionString + " "
//					+ singularFormattedItem, descriptionString + " "
//					+ pluralFormattedItem);
//			if (response.isEmpty()) {
//				response = findFreeBaseEquiv(description.get(0) + " "
//						+ formattedItem, description.get(0) + " "
//						+ singularFormattedItem, description.get(0) + " "
//						+ pluralFormattedItem);
//			}
//		}
//
//		if (response.isEmpty()) {
//			response = findFreeBaseEquiv(formattedItem, singularFormattedItem,
//					pluralFormattedItem);
//		}
//		if (response.isEmpty()) {
//			response = executeItemFirstPartialQuery(formattedItem,
//					singularFormattedItem, pluralFormattedItem);
//		}
//
//		// if (brand.size() > 0) {
//		// if (brand.size() > 0) {
//		// response = executeQuerywithBrandAndDescription(
//		// formattedItem, brand, description);
//		// } else {
//		// response = executeQuerywithBrand(formattedItem, brand);
//		// }
//		// } else {
//		// response=executeItemFirstPartialQuery(formattedItem);
//		// }
//		//
//		System.out.println("Description: " + description);
//		USDAResult usdaResponse = getUSDAResult(item, response, brand,
//				description, true);
//		if (usdaResponse.getResults().size() == 0) {
//			System.out.println("No results using description");
//			usdaResponse = getUSDAResult(item, response,
//					new ArrayList<String>(), new ArrayList<String>(), true);
//		}

		return leveledFoodItemLookup(formattedItem, formattedBrand, description, 0, quantity, new ArrayList<String>());

	}

	/**
	 * Do lookup for a specific food item with specific given adjectives
	 */
	public static USDAResult foodItemAdjectiveLookup(String item, ArrayList<String> description,
			ArrayList<String> adjectivesSpecified, String brand, int level, String quantity) {

//		System.out.println("Searching for " + item + " with adjectives"
//				+ adjectivesSpecified);
//		// remove numeric values (i.e. indices) from food item
		String formattedItem = item.replaceAll("[^A-Za-z ]", "");
			formattedItem=formattedItem.trim();
			System.out.println("formatted string:" +formattedItem);
			
			String formattedBrand = brand.replaceAll("[^A-Za-z1-9% ]", "");
			formattedBrand=formattedBrand.trim();
//
//		ArrayList<ReturnableItem> response = new ArrayList<>();
//		Stemmer stemmer = new Stemmer();
//		for (char ch : formattedItem.toCharArray()) {
//			stemmer.add(ch);
//		}
//		stemmer.stem();
//		String singularFormattedItem = stemmer.toString();
//		String pluralFormattedItem = English.plural(formattedItem);
//
//		response = executeItemFirstPartialQuery(formattedItem,
//				singularFormattedItem, pluralFormattedItem);
//
//		USDAResult usdaResponse = getUSDAResult(item, response,
//				new ArrayList<String>(), adjectivesSpecified, defaultRaw);
//		if (usdaResponse.getResults().size() == 0) {
//			System.out.println("No results using description");
//			usdaResponse = getUSDAResult(item, response,
//					new ArrayList<String>(), new ArrayList<String>(),
//					defaultRaw);
//		}
//
//		// return usdaResponse;
//		// USDAResult usdaResponse = getUSDAResult(item, response,
//		// adjectivesSpecified, defaultRaw);
//
//		return usdaResponse;
		return leveledFoodItemLookup(formattedItem, formattedBrand, description, level, quantity, adjectivesSpecified);

	}

	private static ArrayList<ReturnableItem> executeQuery(String query) {
		System.out.println("Execute query");
		
		ArrayList<ReturnableItem> returnedItems = new ArrayList<ReturnableItem>();
		
		Connection conn = null;
		Statement stmt = null;

		try {
			// STEP 2: Register JDBC driver
			Class.forName("com.mysql.jdbc.Driver");

			// STEP 3: Open a connection
			// System.out.println("Connecting to database...");

			conn = DriverManager.getConnection(DB_URL, USER, PASS);
			//Map<String, String> foodImages = new HashMap<String, String>();
			
			// STEP 4: Execute a query
			// System.out.println("Creating statement...");
			stmt = conn.createStatement();

			ResultSet rs = stmt.executeQuery(query);
			while (rs.next()) {
				// Retrieve by column name
				String ndb_no = rs.getString("NDB_No");
				String longDesc = rs.getString("Long_Desc");
				double calories = rs.getDouble("Calories");
				double protein=rs.getDouble("Protein");
				double fat=rs.getDouble("fat");
				double cholesterol=rs.getDouble("cholesterol");
				double sodium=rs.getDouble("sodium");
				double carbohydrates =rs.getDouble("carbohydrates");
				double fiber =rs.getDouble("fiber");
				double sugars=rs.getDouble("sugars");
				String image = rs.getString("image");
				System.out.println(longDesc + ", Calories: " + calories+", NDB_No: "+ndb_no);
				System.out.println("USDA Lookup image name: " + image);

				
				//Adding image path to hash table
				//String foodDesc[] = longDesc.split(" ", 2);
				//String firstWord = foodDesc[0];
				//System.out.println("first word: " + firstWord);
				
				//foodImages.put(firstWord, image);
				
				
				USDAItem toAdd=new USDAItem(ndb_no, longDesc, calories);
				toAdd.setProtein(protein);
				toAdd.setFat(fat);
				toAdd.setCholesterol(cholesterol);
				toAdd.setSodium(sodium);
				toAdd.setCarbohydrates(carbohydrates);
				toAdd.setFiber(fiber);
				toAdd.setSugars(sugars);
				toAdd.setImage(image);
				
				GetImages.imageName(longDesc, image);
				
				returnedItems.add(toAdd);

			}
			rs.close();

			stmt.close();
			conn.close();
			return returnedItems;

			// rs.close();

			// STEP 6: Clean-up environment

		} catch (SQLException se) {
			// Handle errors for JDBC
			System.err.println("Misformed query: " + query);
			se.printStackTrace();
		} catch (Exception e) {
			// Handle errors for Class.forName
			e.printStackTrace();
		} finally {
			// finally block used to close resources
			try {
				if (stmt != null)
					stmt.close();
			} catch (SQLException se2) {
			}// nothing we can do
			try {
				if (conn != null)
					conn.close();
			} catch (SQLException se) {
				se.printStackTrace();
			}// end finally try
		}// end try
		return null;
	}

	/**
	 * This function executes the query on the given food item and returns what
	 * it thinks is the best result
	 * 
	 * @param item
	 *            the item to search for
	 **/

	public static ArrayList<ReturnableItem> executeItemFirstPartialQuery(
			String item, String singular, String plural) {
		
		System.out.println("Execute item first partial query for: " + item);

		String sql;
		sql="SELECT NDB_No, Long_Desc, Calories, "
				+ "Protein, fat, cholesterol, "
				+ "sodium, carbohydrates, fiber, "
				//+ "sugars FROM foodsWithNutrients "
				+ "sugars, image FROM foodsWithNutrientsTufts "
				+ "WHERE ";

		sql += "(Long_Desc LIKE '"
				+ item
				+ "\\,%' OR Long_Desc LIKE '"
				+ singular
				+ "\\,%' OR Long_Desc LIKE '"
				+ plural
				+ "\\,%' OR Long_Desc LIKE '"
				+ item
				+ "' OR Long_Desc LIKE '"
				+ singular + "' OR Long_Desc LIKE '" + plural + "')";
		//
		System.out.println("Query (1):" + sql);

		ArrayList<ReturnableItem> returnedItems = executeQuery(sql);

		System.out.println("Finished (1) with " + returnedItems.size()
				+ " results.");
		
		

		return returnedItems;

	}

	public static ArrayList<ReturnableItem> executeItemPartialQuery(
			String item, String singular, String plural) {

		String sql;
		// sql = "SELECT NDB_No, Long_Desc FROM FOOD_DES WHERE Long_Desc LIKE '"
		// + item + " %' OR Long_Desc LIKE '" + item+",%'";

		// sql = "SELECT NDB_No, Long_Desc FROM FOOD_DES WHERE Long_Desc LIKE ;
		sql="SELECT NDB_No, Long_Desc, Calories, "
				+ "Protein, fat, cholesterol, "
				+ "sodium, carbohydrates, fiber, "
				//+ "sugars FROM foodsWithNutrients "
				+ "sugars, image FROM foodsWithNutrientsTufts "
				+ "WHERE ";
		sql += "(Long_Desc LIKE '%"
				+ item
				+ "\\,%' OR Long_Desc LIKE '%"
				+ singular
				+ "\\,%' OR Long_Desc LIKE '%"
				+ plural
				+ "\\,%' OR Long_Desc LIKE '%"
				+ item
				+ "' OR Long_Desc LIKE '%"
				+ singular
				+ "' OR Long_Desc LIKE '%" + plural + "')";

		System.out.println("Query (2):" + sql);
		ArrayList<ReturnableItem> returnedItems = executeQuery(sql);

		System.out.println("Finished (2) with " + returnedItems.size()
				+ " results.");
		//System.out.println("Returned items: " + returnedItems);

		return returnedItems;

	}


	public static ArrayList<USDAWeight> getRelevantQuantities(
			ArrayList<ReturnableItem> usdaIds) {
		String sql;
		if (usdaIds.size() < 1) {
			System.err.println("Too few ids sent");
		} else {
			ArrayList<String> allIds = new ArrayList<String>();
			for (ReturnableItem id : usdaIds) {
				allIds.addAll(id.getAllFoodIDs());
			}
			sql = "SELECT Msre_Desc, NDB_No, Gm_Wgt, Amount FROM WEIGHT"
			//sql = "SELECT Msre_Desc, NDB_No, Gm_Wgt, Amount FROM WEIGHTtufts"
					+ " WHERE";
			for (String currentID : allIds) {
				sql += " NDB_No=" + currentID + " OR";
			}
			sql = sql.substring(0, sql.length() - 2);
			//
			System.out.println("Query (3):" + sql);

			ArrayList<USDAWeight> returnedItems = executeWeightQuery(sql);

			Collections.sort(returnedItems);
			return returnedItems;
		}
		return null;

	}

	private static ArrayList<USDAWeight> executeWeightQuery(String query) {
		// ArrayList<USDAWeight> returnedItems = new ArrayList<USDAWeight>();
		HashMap<String, USDAWeight> currentWeights = new HashMap<String, USDAWeight>();
		Connection conn = null;
		Statement stmt = null;

		try {
			// STEP 2: Register JDBC driver
			Class.forName("com.mysql.jdbc.Driver");

			// STEP 3: Open a connection
			// System.out.println("Connecting to database...");

			conn = DriverManager.getConnection(DB_URL, USER, PASS);

			// STEP 4: Execute a query
			// System.out.println("Creating statement...");
			stmt = conn.createStatement();

			ResultSet rs = stmt.executeQuery(query);
			while (rs.next()) {

				// Retrieve by column name
				String ndb_no = rs.getString("NDB_No");
				String desc = rs.getString("Msre_Desc");
				double gmwgt = rs.getDouble("Gm_Wgt");
				double amount = rs.getDouble("Amount");

				if (currentWeights.containsKey(desc)) {
					currentWeights.get(desc).addFoodID(ndb_no, gmwgt);

				} else {
					currentWeights.put(desc, new USDAWeight(ndb_no, desc,
							gmwgt, amount));
				}

			}
			rs.close();

			stmt.close();
			conn.close();
			return new ArrayList<USDAWeight>(currentWeights.values());

			// rs.close();

			// STEP 6: Clean-up environment

		} catch (SQLException se) {
			// Handle errors for JDBC
			System.err.println("Misformed query: " + query);
			se.printStackTrace();
		} catch (Exception e) {
			// Handle errors for Class.forName
			e.printStackTrace();
		} finally {
			// finally block used to close resources
			try {
				if (stmt != null)
					stmt.close();
			} catch (SQLException se2) {
			}// nothing we can do
			try {
				if (conn != null)
					conn.close();
			} catch (SQLException se) {
				se.printStackTrace();
			}// end finally try
		}// end try
		return null;
	}

	/**
	 * Get corresponding freebase usda_equiv item if it exists
	 **/

	public static ArrayList<ReturnableItem> findFreeBaseEquiv(String item,
			String singular, String plural) {
		
		System.out.println("Find freebase equiv");

		String sql;
		
		sql="SELECT fd.NDB_No, Long_Desc, Calories, "
				+ "Protein, fat, cholesterol, "
				+ "sodium, carbohydrates, fiber, "
				//+ "sugars FROM foodsWithNutrients as fd, freebaseEquiv as free "
				+ "sugars, image FROM foodsWithNutrientsTufts as fd, freebaseEquiv as free "
				+ "WHERE fd.NDB_NO = free.srid AND ";
		sql += "(free.name LIKE '"
				+ item
				+ "' OR free.name LIKE '"
				+ singular
				+ "' OR free.name LIKE '" + plural + "')";
		
		System.out.println("Query:" + sql);

		ArrayList<ReturnableItem> returnedItems = executeQuery(sql);

		System.out.println("Finished (3) with " + returnedItems.size()
				+ " results.");

		return returnedItems;

	}
	
	/**
	 * Get corresponding freebase usda_equiv item if it exists
	 **/

	public static ArrayList<ReturnableItem> findNutritionixEquiv(String item,
			String singular, String plural) {
		
		System.out.println("Find nutritionix equiv");
		String sql;
		sql = "SELECT itemName, calories, nutritionixID, servingQuant, servingAmount FROM nutritionixCache"
				+ " WHERE name LIKE '"
				+ item
				+ "' OR name LIKE '"
				+ singular
				+ "' OR name LIKE '" + plural + "'";
		//
		System.out.println("Query:" + sql);

		ArrayList<ReturnableItem> returnedItems = executeNutritionixCacheQuery(sql);

		System.out.println("Finished (4) with " + returnedItems.size()
				+ " results.");

		return returnedItems;

	}
	
	public static USDAWeight getNutritionixWeight (NutritionixItem item) {
		return new USDAWeight("-1", item.getQuantityAmount(), -1, Double.parseDouble(item.getQuantityUnit()));
	}
	
	private static ArrayList<ReturnableItem> executeNutritionixCacheQuery(String query) {
		ArrayList<ReturnableItem> returnedItems = new ArrayList<ReturnableItem>();
		Connection conn = null;
		Statement stmt = null;

		try {
			// STEP 2: Register JDBC driver
			Class.forName("com.mysql.jdbc.Driver");

			// STEP 3: Open a connection
			// System.out.println("Connecting to database...");

			conn = DriverManager.getConnection(DB_URL, USER, PASS);

			// STEP 4: Execute a query
			// System.out.println("Creating statement...");
			stmt = conn.createStatement();

			ResultSet rs = stmt.executeQuery(query);
			while (rs.next()) {
				// Retrieve by column name
				String itemName = rs.getString("itemName");
				double calories = rs.getDouble("calories");
				String servingAmount = rs.getString("servingAmount");
				String servingQuant = rs.getString("servingQuant");
				String nutid = rs.getString("nutritionixID");
				System.out.println(itemName + ", Calories: " + calories);

				returnedItems.add(new NutritionixItem(nutid, itemName, calories, servingAmount, servingQuant));

			}
			rs.close();

			stmt.close();
			conn.close();
			return returnedItems;

			// rs.close();

			// STEP 6: Clean-up environment

		} catch (SQLException se) {
			// Handle errors for JDBC
			System.err.println("Misformed query: " + query);
			se.printStackTrace();
		} catch (Exception e) {
			// Handle errors for Class.forName
			e.printStackTrace();
		} finally {
			// finally block used to close resources
			try {
				if (stmt != null)
					stmt.close();
			} catch (SQLException se2) {
			}// nothing we can do
			try {
				if (conn != null)
					conn.close();
			} catch (SQLException se) {
				se.printStackTrace();
			}// end finally try
		}// end try
		return null;
	}

	// @SuppressWarnings("unchecked")
	public static void main(String[] args) {
		findFreeBaseEquiv("waffle", "waffle", "waffles");
//		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
//		Date date = new Date();
//		System.out.println(dateFormat.format(date)); //2014/08/06 15:59:48
//		executeItemPartialQuery("butter", "butter", "butters");
//		
//		date = new Date();
//		System.out.println(dateFormat.format(date)); //2014/08/06 15:59:48
//		
//		System.out.println(executeItemFirstPartialQuery("butter", "butter", "butters"));
//		
//		date = new Date();
//		System.out.println(dateFormat.format(date)); //2014/08/06 15:59:48

	}
}
