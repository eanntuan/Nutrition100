package edu.mit.csail.sls.nut.databaseLookup.usda;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import cern.colt.Arrays;
import edu.mit.csail.sls.nut.NutritionContext;
import edu.mit.csail.sls.nut.Segment;
import edu.mit.csail.sls.nut.Segmentation;
import edu.mit.csail.sls.nut.Tag;

public class QuantityTester {
	
	public static void performQuantityTest() {
		// Map<String, USDAResult> is what is needed to do lookup
		String path = "/afs/csail.mit.edu/u/k/korpusik/Documents/";
		String file = path+"DataForAMTQuantityTest.csv";
		
		BufferedReader br;
		try {
			br = new BufferedReader(new FileReader(file));
			PrintWriter resultWriter = new PrintWriter(path+"quantityResults.csv");

			String line;
			try {
				int current=0;
			while ((line = br.readLine()) != null) {
				try {
				current++;
//				System.out.println(line);
//				break;
				
				String segment_type = "CRF"; 
				String labelType = "IOE";
				String tag_type = "mallet";
//				String jsonp = request.getParameter("jsonp");
				String textWithPunc = line;
//				System.out.println(textWithPunc);
						
				// run CRF and get food-attribute dependencies
				PrintWriter FSTwriter = null;
			    Segmentation segmentation = null;
				try {
					segmentation = Tag.runCRF(FSTwriter, textWithPunc, segment_type, NutritionContext.sentenceTagger, false, labelType, tag_type);
				} catch (ClassNotFoundException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				//Add calls to get database lookups for food item
				Map<String, USDAResult> result=USDALookup.foodItemInitialLookup(segmentation.attributes, segmentation.tokens);
				for (String currentItem: result.keySet()) {
					USDAResult currentResult = result.get(currentItem);
					if (currentResult.getWeights().size()>0) {
						resultWriter.println(line.replaceAll(",", ";")+","+currentItem.replaceAll("[^A-Za-z ]", "")+","+currentResult.getQuantity().replaceAll(",", ";")+
								","+currentResult.getQuantityAmount()+","+currentResult.getWeights().get(0).getMsre_Desc().replaceAll(",", ";"));
					}
				}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			} finally {
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
//		performQuantityTest();
		System.out.println("Beef,  plate,  outside skirt steak,  trimmed to 0\" fat,  all grades,  cooked,  broiled".replaceAll("\"", ""));
	}

}
