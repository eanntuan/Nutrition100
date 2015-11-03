package edu.mit.csail.sls.nut.databaseLookup.usda;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.atteo.evo.inflector.English;

import cern.colt.Arrays;
import edu.mit.csail.sls.nut.Segment;

public class RegressionTester {

	public static void parseTestList() {
		// Map<String, USDAResult> is what is needed to do lookup
		String path = "/Users/rnaphtal/Documents/Classes/NutritionProject/RegressionTesting/";
		String file = path+"regressionData.csv";
		
		BufferedReader br;
		try {
			br = new BufferedReader(new FileReader(file));
			PrintWriter resultWriter = new PrintWriter(path+"results.csv");

			String line;
			while ((line = br.readLine()) != null) {
				String[] values = line.split(",");
				Map<String, ArrayList<Segment>> dependencies = new HashMap<String, ArrayList<Segment>>();
				ArrayList<Segment> segments = new ArrayList<Segment>();
				
				ArrayList<String> tokens = new  ArrayList<String>();
				tokens.add(values[1]);
				if (values[2].length()>1) {
					tokens.add(values[2]);
					Segment descriptionSegment = new Segment();
					descriptionSegment.label="Description";
					descriptionSegment.start = tokens.size()-1;
					descriptionSegment.end = tokens.size();
					segments.add(descriptionSegment);
					System.out.println(descriptionSegment.start+" "+ descriptionSegment.end);
				}
				if (values[3].length()>0) {
					tokens.add(values[3]);
					Segment brandSegment = new Segment();
					brandSegment.label="Brand";
					brandSegment.start = tokens.size()-1;
					brandSegment.end = tokens.size();
					segments.add(brandSegment);
				}
				dependencies.put(values[1], segments);
				System.out.println(Arrays.toString(values));
				System.out.println(tokens);
				System.out.println(segments);
				Map<String, USDAResult> usdaresult= USDALookup.foodItemInitialLookup(dependencies, tokens);
				ArrayList<ReturnableItem> result = usdaresult.get(values[1]).getResults();
				String correctAnswer = values[4];
				boolean foundAnswer = false;
				for (ReturnableItem item: result) {
					if (item.getAllFoodIDs().contains(correctAnswer)) {
						foundAnswer = true;
					}
				}
//				resultWriter.println(values[0]+","+values[4]+","+ foundAnswer+","+result.size()+","+ result);
				
				System.out.println(values[0]+","+values[4]+","+ foundAnswer+","+result.size()+","+ result);
				break;
			}
			br.close();
			resultWriter.close();

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void main (String[] args) {
//		System.out.println(English.plural("avocado"));
		parseTestList();
	}
	


}
