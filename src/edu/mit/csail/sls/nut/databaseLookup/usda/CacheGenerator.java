package edu.mit.csail.sls.nut.databaseLookup.usda;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Map;

import org.atteo.evo.inflector.English;

import edu.mit.csail.sls.nut.NLPData;
import edu.mit.csail.sls.nut.NutritionContext;
import edu.mit.csail.sls.nut.Segment;
import edu.mit.csail.sls.nut.Segmentation;
import edu.mit.csail.sls.nut.Tag;
import edu.mit.csail.sls.nut.databaseLookup.nutritionix.NutritionixLookup;

public class CacheGenerator {
		
	/**
	 * Takes a csv of food entries, and outputs a file with processed
	 * food items, descriptions and brands
	 */
		public static void processSentences() {
			// Map<String, USDAResult> is what is needed to do lookup
			String path = "/afs/csail.mit.edu/u/k/korpusik/nutrition/";
			String file = path+"rawdiaryEntries.csv";
			
			BufferedReader br;
			try {
				br = new BufferedReader(new FileReader(file));
				PrintWriter resultWriter = new PrintWriter(path+"allPossibleCombosFromDiariesNotincache.csv");

				String line;
				try {
//					int current=0;
				while ((line = br.readLine()) != null) {
					try {
//					current++;
//					System.out.println(line);
//					break;
					
					String segment_type = "CRF"; 
					String labelType = "IOE";
					String tag_type = "mallet";
//					String jsonp = request.getParameter("jsonp");
					String textWithPunc = line;
//					System.out.println(textWithPunc);
							
					// run CRF and get food-attribute dependencies
					PrintWriter FSTwriter = null;
					Segmentation segmentation = new Segmentation();

					
					try {						
						NLPData NLPresult = Tag.runCRF(FSTwriter, textWithPunc, segment_type, NutritionContext.sentenceTagger, false, labelType, tag_type);
						
						segmentation.text = NLPresult.text;
						segmentation.tokens = NLPresult.tokens;
						segmentation.labels = NLPresult.labels;
						segmentation.tags = NLPresult.tags;
						segmentation.segments = NLPresult.segments;
						segmentation.parse = NLPresult.parse;
						segmentation.deps = NLPresult.deps;
						segmentation.foods = NLPresult.foods;
						segmentation.attributes = NLPresult.attributes;
						
					} catch (ClassNotFoundException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					
					for (String item : segmentation.attributes.keySet()) {
						ArrayList<String> brand = new ArrayList<String>();
						ArrayList<String> description = new ArrayList<String>();
						 ArrayList<String> tokens = segmentation.tokens;
						 ArrayList<Segment> relevantdependencies = segmentation.attributes.get(item);
							for (Segment s : relevantdependencies) {
								System.out.println(s.label);
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
								if (s.label.equals("Description")) {
									for (int i = s.start; i < s.end; i++) {
										description.add(tokens.get(i).replaceAll("[^A-Za-z ]", ""));
									}

								}
							}

							// remove numeric values (i.e. indices) from food item
							String formattedItem = item.replaceAll("[^A-Za-z ]", "");
							
							String brandString = "";
							for (String i : brand) {
								brandString += " " + i;
							}
							if (brandString.length() > 0) {
								brandString = brandString.substring(1);
							}
							
							String descriptionString = "";
							for (String i : description) {
								descriptionString += " " + i;
							}
							if (descriptionString.length() > 0) {
								descriptionString = descriptionString.substring(1);
							}
							
							String formattedBrand = brandString.replaceAll("[^A-Za-z ]", "");
							String formattedDescription = descriptionString.replaceAll("[^A-Za-z ]", "");
							
							System.out.println(line.replaceAll(",", ";")+ "," 
									+formattedItem+"," 
							+ formattedDescription+","+formattedBrand);
							
							resultWriter.println(line.replaceAll(",", ";")+"," 
									+ formattedItem+"," 
									+ formattedDescription+","+formattedBrand);
			

					}
//					//Add calls to get database lookups for food item
//					Map<String, USDAResult> result=USDALookup.foodItemInitialLookup(segmentation.attributes, segmentation.tokens);
//					for (String currentItem: result.keySet()) {
//						USDAResult currentResult = result.get(currentItem);
//						if (currentResult.getWeights().size()>0) {
//							resultWriter.println(line.replaceAll(",", ";")+","+currentItem.replaceAll("[^A-Za-z ]", "")+","+currentResult.getQuantity().replaceAll(",", ";")+
//									","+currentResult.getQuantityAmount()+","+currentResult.getWeights().get(0).getMsre_Desc().replaceAll(",", ";"));
//						}
//					}
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
		
		/**
		 * Finds which of the new items already have entries in the cache
		 */
		public static void findNewItemsforAMT() {
			// Map<String, USDAResult> is what is needed to do lookup
			String path = "/Users/rnaphtal/Documents/Classes/NutritionProject/AMTData/CacheGeneration/";
			String file = path+"allPossibleCombosFromDiaries.csv";
			
			BufferedReader br;
			try {
				br = new BufferedReader(new FileReader(file));
				PrintWriter resultWriter = new PrintWriter(path+"newParsedDiaryResults.csv");

				String rawline;
				try {
					resultWriter.println("foodName");
				while ((rawline = br.readLine()) != null) {
					String line= rawline.toLowerCase();
					try {
						String plural = English.plural(line);
						Stemmer stemmer = new Stemmer();
						for (char ch : line.toCharArray()) {
							stemmer.add(ch);
						}
						stemmer.stem();
						String singular = stemmer.toString();
						if (USDALookup.findFreeBaseEquiv(line, singular, plural).isEmpty()) {
							if (USDALookup.findNutritionixEquiv(line, singular, plural).isEmpty()) {
								resultWriter.println(line);
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
			findNewItemsforAMT();
//			performQuantityTest();
//			System.out.println("Beef,  plate,  outside skirt steak,  trimmed to 0\" fat,  all grades,  cooked,  broiled".replaceAll("\"", ""));
		}


}
