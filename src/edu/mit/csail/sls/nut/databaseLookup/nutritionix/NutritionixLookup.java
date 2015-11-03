package edu.mit.csail.sls.nut.databaseLookup.nutritionix;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import edu.mit.csail.sls.nut.NutritionContext;
import edu.mit.csail.sls.nut.NutritionSearchQuery;
import edu.mit.csail.sls.nut.Segment;

//import com.google.gson.JsonObject;

public class NutritionixLookup {

//	static String appKey = NutritionContext.getNutritionixAppKey();
//	static String appId = NutritionContext.getNutritionixAppID();
	
//	static String appKey = "fa6c29b2dd2a10e654ed207aa017d657";
//	static String appId = "cdc2f3cf";
	
	static String appKey = "9994f27eb487d3bb304136cb811ea771";
	static String appId = "9268f6b4";
	
	static final String DB_URL = "jdbc:mysql://mysql.csail.mit.edu/nutritionApplicationLog";

	// Database credentials
	static final String USER = "slsNutrition";
	static final String PASS = "slsNutrition";
	
	/**
	 * Perform nutritionix lookup for a food item
	 */
	static Map<String, NutritionixResponse> foodItemLookup(
			Map<String, ArrayList<Segment>> dependencies,
			ArrayList<String> tokens) {
		System.out.println("Food item lookup:" + dependencies);
		Map<String, NutritionixResponse> foodItems = new HashMap<String, NutritionixResponse>();
		System.out.println("Keyset: " + dependencies.keySet());
		for (String item : dependencies.keySet()) {
			// Get associated values
			String brand = "";
			String description = "";
			String quantity = "";
			for (Segment s : dependencies.get(item)) {
				System.out.println(s.label);
				if (s.label.equals("Brand")) {
					for (int i = s.start; i < s.end; i++) {
						// don't include "from" as part of brand name
						if (tokens.get(i).equals("from")) {
							continue;
						}
						brand += " " + tokens.get(i);
					}

				}
				if (s.label.equals("Quantity")) {
					for (int i = s.start; i < s.end; i++) {
						quantity += " " + tokens.get(i);
					}
				}
				// if brand labeled "Trader", make it "Trader Joe's"
				if (brand.equals("Trader") || brand.equals(" Trader")) {
					brand += " Joe's";
				}
				if (s.label.equals("Description")) {
					for (int i = s.start; i < s.end; i++) {
						description += " " + tokens.get(i);
					}

				}
			}
			System.out.println("Brand in lookup: " + brand);

			// remove numeric values (i.e. indices) from food item
			String itemWithDescription = item.replaceAll("[^A-Za-z]", "");
			if (!description.equals("")) {
				System.out.println("Found description" + description);
				itemWithDescription = description + " " + itemWithDescription;
			}

			NutritionixItemQuery query = new SimpleItemQuery(
					itemWithDescription);

			if (!brand.equals("")) {
				query = new ItemQuerywithBrand(itemWithDescription,
						brandDatabaseLookup(brand));
			}
			NutritionixResponse response = executeSearchQuery(query);
			foodItems.put(item, response);

			// Add query to log
			String resultId = "";
			if (response.getHits().length > 0) {
				resultId = response.getHits()[0].get_id();
				for (NutritionixResponseHit currentItem: response.getHits()) {
//					addNutritionixItemToDB(currentItem.getFields());
				}
				
			}
			NutritionSearchQuery searchToLog = new NutritionSearchQuery(item.replaceAll("[^A-Za-z]", ""),
					brand, quantity, description, resultId);
			System.out.println(searchToLog);
//			addQueryToLog(searchToLog, "defaultUser");

		}

		return foodItems;

	}

	/**
	 * Look up brand in the Nutritionix database (to obtain id)
	 */
	public static String brandDatabaseLookup(String brand) {
		try {
			// appId = "9268f6b4";
			// appKey = "9994f27eb487d3bb304136cb811ea771";
			String url = "https://api.nutritionix.com/v1_1/brand/search/";
			String charset = "UTF-8";
			String query = String.format("appKey=%s&appId=%s&query=%s",
					URLEncoder.encode(appKey, charset),
					URLEncoder.encode(appId, charset),
					URLEncoder.encode(brand.trim(), charset));
			System.out.println(query);

			URLConnection connection = new URL(url + "?" + query)
					.openConnection();
			connection.setRequestProperty("Accept-Charset", charset);
			InputStream responseStream = connection.getInputStream();

			ObjectMapper mapper = new ObjectMapper();

			NutritionixBrandResponse response;
			response = mapper.readValue(responseStream,
					NutritionixBrandResponse.class);
			if (response.getHits().length > 0) {
				System.out.println(response.getHits().length);
				return response.getHits()[0]._id;
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}

	public static NutritionixResponse executeSearchQuery(
			NutritionixItemQuery query) {

		HttpClient httpclient = HttpClients.createDefault();
		HttpPost httppost = new HttpPost(
				"https://api.nutritionix.com/v1_1/search/");

		// Make query object
		Gson gson = new GsonBuilder().create();
		String gsonstring = gson.toJson(query);
		System.out.println(gsonstring);
		NutritionixResponse toreturn = null;

		// Set entity
		StringEntity stringentity;
		try {
			stringentity = new StringEntity(gsonstring);
			stringentity.setContentType("application/json");
			httppost.setEntity(stringentity);

		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		// Execute and get the response.
		HttpResponse response;
		try {
			response = httpclient.execute(httppost);
			HttpEntity entity = response.getEntity();
			if (entity != null) {

				InputStream instream = entity.getContent();

				try {
					toreturn = NutritionixLookup.parseNutritionixData(instream);

				} finally {
					instream.close();

				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		return toreturn;
	}

	

	public static NutritionixResponse parseNutritionixData(InputStream instream) {

		ObjectMapper mapper = new ObjectMapper();

		// File jsonFile = new File("nutritionixOutputData.json");
		NutritionixResponse response;
		try {
			response = mapper.readValue(instream, NutritionixResponse.class);
			System.out.println(response);
			return response;
		} catch (JsonParseException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return null;

	}

	/**
	 * Adds a given nutritionix search to the log
	 */
	public static void addQueryToLog (NutritionSearchQuery searchQuery, String username){
		   Connection conn = null;
		   Statement stmt = null;
		   
		   //Create query
		   String query = "INSERT INTO `nutritionApplicationLog`.`Searches` ( `id` ,"
		   		+ "`username` ,"
		   		+ "`foodItem` ,"
		   		+ " `brand` ,"
		   		+ "`quantity` ,"
		   		+ " `description` ,"
		   		+ " `result` ,"
		   		+ "`time` )"
		   		+ " VALUES ( NULL,'"+
				   username+"',";
		   if (!searchQuery.getFoodItem().isEmpty()) {
			   query+= "'" + searchQuery.getFoodItem()+ "',";
		   } else {
			   query+="NULL,";
		   }
		   if (!searchQuery.getBrand().isEmpty()) {
			   query+= "'" + searchQuery.getBrand()+ "',";
		   } else {
			   query+="NULL,";
		   }
		   if (!searchQuery.getQuantity().isEmpty() || searchQuery.getQuantity() == null) {
			   query+= "'" + searchQuery.getQuantity()+ "',";
		   } else {
			   query+="NULL,";
		   }
		   if (!searchQuery.getDescription().isEmpty() || searchQuery.getDescription() == null) {
			   query+= "'" + searchQuery.getDescription()+ "',";
		   } else {
			   query+="NULL,";
		   }
		   if (!searchQuery.getResult().isEmpty()) {
			   query+= "'" + searchQuery.getResult() + "',";
		   } else {
			   query+="NULL,";
		   }
		   Date now = new Date(System.currentTimeMillis());
		   	query += "'" + now +"')";
		try {
			// Register JDBC driver
			Class.forName("com.mysql.jdbc.Driver");

			// Open a connection
			conn = DriverManager.getConnection(DB_URL, USER, PASS);

			stmt = conn.createStatement();

			stmt.execute(query);

			stmt.close();
			conn.close();

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
	}
	
	/**
	 * Adds a given nutritionix item to the database
	 */
	public static void addNutritionixItemToDB(NutritionixItem item) {
		Connection conn = null;
		Statement stmt = null;

		// Create query
		String query = "INSERT INTO `nutritionApplicationLog`.`nutritionixItems` ("
				+ "`item_id` ,"
				+ "`item_name` ,"
				+ " `brand_id` ,"
				+ "`brand_name` ,"
				+ " `nf_serving_size_qty` ,"
				+ " `nf_serving_size_unit` ,"
				+ "`lastUpdate` )" + " VALUES ('"+item.getItem_id()+"',\"" 
				+ item.getItem_name().replace("\"", "'") + "\",\""
				+ item.getBrand_id().replace("\"", "'") + "\",\""
				+ item.getBrand_name().replace("\"", "'") + "\",\""
				+ item.getNf_serving_size_qty().replace("\"", "'") + "\",\""
				+ item.getNf_serving_size_unit().replace("\"", "'") + "\",";
		Date now = new Date(System.currentTimeMillis());
		query += "'" + now + "') ON DUPLICATE KEY UPDATE `item_id` = `item_id`";

		System.out.println("Adding to db:" + item);
		try {
			// Register JDBC driver
			Class.forName("com.mysql.jdbc.Driver");

			// Open a connection
			conn = DriverManager.getConnection(DB_URL, USER, PASS);

			stmt = conn.createStatement();

			stmt.execute(query);

			stmt.close();
			conn.close();

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
		      //STEP 4: Execute a query
		      //System.out.println("Creating statement...");
		      try {
				stmt = conn.createStatement();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		      
		      try {
				stmt.execute(query);
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
//		      while(rs.next()){
//		    	  
//		         //Retrieve by column name
//		         String ndb_no  = rs.getString("NDB_No");
//		         String longDesc = rs.getString("Long_Desc");
//
//		         returnedItems.add(new USDAItem(ndb_no, longDesc));
//		         
//		         
//		      }
//		      rs.close();
		      
		      try {
				stmt.close();
				conn.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		      
		      
		    //  rs.close();
		     
		      
	   }

	public static void main(String[] args) {
		// System.out.println(brandDatabaseLookup("Kellogg's"));
		// System.out.println("Hi");
		NutritionSearchQuery searchToLog = new NutritionSearchQuery("apple",
				"mott", "5", "red", "nutrID");
		addQueryToLog(searchToLog, "default");
	}
}
