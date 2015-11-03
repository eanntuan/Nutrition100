package edu.mit.csail.sls.nut.databaseLookup.nutritionix;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;


public class NutritionixFreebaseLoader {

	public static void parseTestList() {
		// Map<String, USDAResult> is what is needed to do lookup
		String path = "/Users/rnaphtal/Documents/Classes/NutritionProject/AMTData/CacheGeneration/4_23/";
		String file = path+"nutritionixNoValues.csv";
		
		BufferedReader br;
		try {
			br = new BufferedReader(new FileReader(file));
			PrintWriter resultWriter = new PrintWriter(path+"nutritionixToAdd.csv");

			String line;
			try{
			while ((line = br.readLine()) != null) {
				
				String[] values = line.split(",");
				System.out.println(Arrays.toString(values));
				//Try to get brand
//				String brandid = NutritionixLookup.brandDatabaseLookup(values[2]);
				NutritionixItemQuery query = new SimpleItemQuery(
						values[1].replaceAll("[^A-Za-z\\d ]", ""));

//				if (!brandid.equals("")) {
//					query = new ItemQuerywithBrand(values[1],
//							brandid);
//				} 
				
				NutritionixResponse response = NutritionixLookup.executeSearchQuery(query);
				//Resulting file rows are item name, nutritionixid, calories
				NutritionixResponseHit[] hits=response.hits;
				if (hits.length>0) {
					System.out.println(values[0]+","+values[1]+","+values[2]+","+
							hits[0].fields.getItem_id()+","+ hits[0].fields.getItem_name()+","+
							hits[0].fields.getBrand_name()+","+
							hits[0].fields.getNf_serving_size_qty() +","+
							hits[0].fields.getNf_serving_size_unit()+","+
							hits[0].fields.getNf_calories());
					resultWriter.println(values[0]+","+values[1]+","+values[2]+","+
							hits[0].fields.getItem_id()+","+ hits[0].fields.getItem_name()+","+
							hits[0].fields.getBrand_name()+","+
							hits[0].fields.getNf_serving_size_qty() +","+
							hits[0].fields.getNf_serving_size_unit()+","+
							hits[0].fields.getNf_calories());
				}
//				break;
				
			}}
			finally{
			br.close();
			resultWriter.close();
			}

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void main (String[] args) {
		parseTestList();
//		System.out.println("Hibachi Chateaubriand w/ Mushrooms".replaceAll("[^A-Za-z\\d ]", ""));
	}
	

}
