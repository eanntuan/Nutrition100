package edu.mit.csail.sls.nut;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import edu.mit.csail.sls.nut.databaseLookup.usda.ReturnableItem;
import edu.mit.csail.sls.nut.databaseLookup.usda.USDAItem;

public class DatabaseQueryExecutor {


	// JDBC driver name and database URL
//	static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
//	static final String DB_URL = "jdbc:mysql://mysql.csail.mit.edu/FNDDS";
	private String DB_URL = "jdbc:mysql://mysql.csail.mit.edu/";

	// Database credentials
	static final String USER = "slsNutrition";
	static final String PASS = "slsNutrition";
	
	public DatabaseQueryExecutor (String databaseName) {
		DB_URL+=databaseName;
	}


	

	public ArrayList<ReturnableItem> executeFoodItemQuery(String query) {
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
				String ndb_no = rs.getString("NDB_No");
				String longDesc = rs.getString("Long_Desc");
				double calories = rs.getDouble("Calories");
				System.out.println(longDesc+" Calories: "+calories);

				returnedItems.add(new USDAItem(ndb_no, longDesc, calories));

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
	
	public void executeInsertImageQuery(String query) {
		Connection conn = null;
		Statement stmt = null;

		try {
			// STEP 2: Register JDBC driver
			Class.forName("com.mysql.jdbc.Driver");

			// STEP 3: Open a connection
			conn = DriverManager.getConnection(DB_URL, USER, PASS);

			// STEP 4: Execute a query
			stmt = conn.createStatement();

			ResultSet rs = stmt.executeQuery(query);
//			while (rs.next()) {
//
//				// Retrieve by column name
//				String ndb_no = rs.getString("NDB_No");
//				String longDesc = rs.getString("Long_Desc");
//				double calories = rs.getDouble("Calories");
//				System.out.println(longDesc+" Calories: "+calories);
//
//				returnedItems.add(new USDAItem(ndb_no, longDesc, calories));
//
//			}
			rs.close();

			stmt.close();
			conn.close();

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
	}
	
	public ArrayList<ReturnableItem> executeImageQuery(String query) {
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
				String ndb_no = rs.getString("NDB_No");
				String longDesc = rs.getString("Long_Desc");
				double calories = rs.getDouble("Calories");
				System.out.println(longDesc+" Calories: "+calories);

				returnedItems.add(new USDAItem(ndb_no, longDesc, calories));

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
}