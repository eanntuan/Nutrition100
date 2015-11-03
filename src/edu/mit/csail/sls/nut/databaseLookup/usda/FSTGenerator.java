package edu.mit.csail.sls.nut.databaseLookup.usda;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FSTGenerator {

	static int numMostCommon = 4;
	
	public static ArrayList<FSTNode> makeTree(ArrayList<ReturnableItem> items) {
		Map<String, AttributeNode> firstNodes = new HashMap<String, AttributeNode>();
		for (ReturnableItem currentItem : items) {
			ArrayList<String> features = currentItem.getFeatures();
			AttributeNode currentNode;
			if (features.size()>0) {
			if (firstNodes.containsKey(features.get(0))) {
				currentNode = firstNodes.get(features.get(0));
			} else {
				currentNode = new AttributeNode(features.get(0));
				firstNodes.put(features.get(0), currentNode);
			}
			for (int i = 1; i < features.size(); i++) {
				String currentAttribute = features.get(i);
				if (currentNode.nextNode(currentAttribute) != null) {
					currentNode = (AttributeNode) currentNode
							.nextNode(currentAttribute);
				} else {
					AttributeNode nextNode = new AttributeNode(currentAttribute);
					currentNode.addNext(nextNode);
					currentNode = nextNode;
				}

			}
			currentNode.addNext((FSTNode) currentItem); //TODO check this cast
			}
		}
		return new ArrayList<FSTNode>(firstNodes.values());
	}

	public static String drawTree(ArrayList<FSTNode> nodes) {
		String toreturn = "";
		for (FSTNode currentMainNode : nodes) {
			int currentLevelNumber=1;
			toreturn += "\r"+currentMainNode + "\r\t";
			if (currentMainNode.getFSTType().equals(FSTNode.type.Attribute)) {
				boolean hasNextLevel = true;
				ArrayList<FSTNode> currentLevel = new ArrayList<FSTNode>();
				ArrayList<FSTNode> nextLevel = new ArrayList<FSTNode>();
				for (FSTNode nextNode : ((AttributeNode) currentMainNode)
						.getNext()) {
						currentLevel.add(nextNode);
//					}
				}

				while (hasNextLevel) {

//					 System.out.println(currentLevel.size());

					hasNextLevel = false;
					for (int i = 0; i < currentLevel.size(); i++) {
						FSTNode currentNode = currentLevel.get(i);
						if (currentNode.getFSTType().equals(
								FSTNode.type.Item)) {
							toreturn += currentNode + "; ";
						} else if (currentNode.getFSTType().equals(
								FSTNode.type.Attribute)) {
							hasNextLevel = true;
							
							ArrayList<FSTNode> children = ((AttributeNode) currentNode).getNext();
//							System.out.println("Children:" + children.size());
							for (FSTNode nextNode : children) {
//								if (((AttributeNode) currentMainNode).shown()) {
//								System.out.println("Adding" + nextNode+ ((AttributeNode) currentMainNode)
//										.getNext().size());
									nextLevel.add(nextNode);
//								}
							}
							if (!((AttributeNode)currentNode).shown()) {
//								toreturn+="hidden:";
							} else {
								toreturn += currentNode + "; ";
							}
							
						}
					}

					currentLevel = nextLevel;
					nextLevel = new ArrayList<FSTNode>();
					toreturn += "\r";
					for (int i=0; i<=currentLevelNumber; i++) {
						toreturn +="\t";
					}
					currentLevelNumber++;

				}
			}

		}
		return toreturn;

	}
	
	static ArrayList<ArrayList<ReturnableItem>> generateClusters (ArrayList<USDAItem> items) {
		ArrayList<ArrayList<ReturnableItem>> toreturn= new ArrayList<ArrayList<ReturnableItem>>();
		
		ArrayList<ReturnableItem> currentList = new ArrayList<ReturnableItem>();
		if (items.size() == 1) {
			return toreturn;
		}
		
		double currentLowest= items.get(0).getCalories();
		double currentTopBoundary= Math.max(currentLowest+AttributeNode.numberClose, 
				currentLowest*(1+AttributeNode.percentClose));
		for (int i=0; i<items.size(); i++) {
			USDAItem currentItem = items.get(i);
			if (currentItem.getCalories()<=currentTopBoundary) {
				currentList.add(currentItem);
			} else {
				toreturn.add(currentList);
				currentList = new ArrayList<ReturnableItem>();
				currentList.add(currentItem);
				currentLowest= items.get(i).getCalories();
				currentTopBoundary= Math.max(currentLowest+AttributeNode.numberClose, 
						currentLowest*(1+AttributeNode.percentClose));
			}
		}
		toreturn.add(currentList);
		
		return toreturn;
	}
	
	static ArrayList<String> getClusterFeatures (ArrayList<USDAItem> items, String item) {
		ArrayList<ArrayList<ReturnableItem>> clusters = generateClusters(items);
		ArrayList<String> successfulFeatures = new ArrayList<String>();
		System.out.println(clusters);
		try {
			PrintWriter clusteredWriter = new PrintWriter("databaseTrees/"+item+"_clusters.txt", "UTF-8");
				
		for (ArrayList<ReturnableItem> currentCluster: clusters) {
			clusteredWriter.println("Cluster:"+currentCluster);
				
			
			
			//Find words in common for the cluster
			Map<String, Integer> featureCounter = new HashMap<String,Integer>();
			for (ReturnableItem currentItem: currentCluster) {
				for (String currentToAdd: currentItem.getFeatures()) {
					if (featureCounter.containsKey(currentToAdd)) {
						featureCounter.put(currentToAdd, featureCounter.get(currentToAdd)+1);
					} else {
						featureCounter.put(currentToAdd, 1);
					}
				}
			}
			List<Integer> topCounts = new ArrayList<Integer>(featureCounter.values());
			Collections.sort(topCounts, Collections.reverseOrder());
			System.out.println("topcounts:"+topCounts);
			topCounts=topCounts.subList(0, Math.min(topCounts.size(), numMostCommon));
			System.out.println("topcounts:"+topCounts);
		   ArrayList<String> currentFeatures = new ArrayList<String>();
		   for (String featureToCheck: featureCounter.keySet()) {
			   if (topCounts.contains(featureCounter.get(featureToCheck)) && featureCounter.get(featureToCheck)>0) {//TODO 0 or 1?
				   currentFeatures.add(featureToCheck);
			   }
		   }
		   System.out.println("Features of cluster:"+currentFeatures);
					
			//See if any of them are not found somewhere else
			for (String currentFeatureToCheck: currentFeatures) {
//				System.out.println("checking for feature" + currentFeatureToCheck);
				boolean found = false;
				for (USDAItem currentItem: items) {
					if (!currentCluster.contains(currentItem) 
							&& currentItem.hasFeature(currentFeatureToCheck)) {
						found = true;
//						System.out.println("found feature" + currentFeatureToCheck+ 
//								currentItem+currentCluster.contains(currentItem)+currentItem.hasFeature(currentFeatureToCheck));
						break;
					}
				}
				if (!found) {
					successfulFeatures.add(currentFeatureToCheck);
					clusteredWriter.println("Features found in cluster: "+currentFeatureToCheck);
					break;
				}
			}
			
		}
		clusteredWriter.close();
		} catch (FileNotFoundException | UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return successfulFeatures;
	}
	
	/**
	 * Check if any of the features define items that are clusterable
	 * @param items
	 * @return
	 */
	public static ArrayList<String> clusterableFeatures (ArrayList<USDAItem> items) {
		//Add all features to a map
		Map<String,ArrayList<ReturnableItem>> featureMap = new HashMap<String,ArrayList<ReturnableItem>>();
		for (USDAItem currentItem: items) {
			for (String currentFeature: currentItem.getFeatures()) {
				if (!featureMap.containsKey(currentFeature)) {
					featureMap.put(currentFeature, new ArrayList<ReturnableItem>());
				}
				featureMap.get(currentFeature).add(currentItem);
			}
		}
		
		//Loop through to see if any work
		ArrayList<String> successfulFeatures = new ArrayList<String>();
		for (String currentFeature: featureMap.keySet()) {
			if (AttributeNode.itemsSimilar(featureMap.get(currentFeature)) && featureMap.get(currentFeature).size() >1) {
				System.out.println("Feature found: "+currentFeature+" with "+featureMap.get(currentFeature).size()+" items");
				successfulFeatures.add(currentFeature);
			}
		}
		return successfulFeatures;
	}
	
	/**
	 * Generate statistics for each feature
	 */
	public static ArrayList<FeatureStatistics> generateStatistics (ArrayList<ReturnableItem> results) {
		//Add all features to a map
		Map<String,ArrayList<ReturnableItem>> featureMap = new HashMap<String,ArrayList<ReturnableItem>>();
		for (ReturnableItem currentItem: results) {
			for (String currentFeature: currentItem.getFeatures()) {
				if (!featureMap.containsKey(currentFeature)) {
					featureMap.put(currentFeature, new ArrayList<ReturnableItem>());
				}
				featureMap.get(currentFeature).add(currentItem);
			}
		}
		
		//Loop through to see if any work
		ArrayList<FeatureStatistics> statistics = new ArrayList<FeatureStatistics>();
		for (String currentFeature: featureMap.keySet()) {
			statistics.add(new FeatureStatistics(currentFeature, featureMap.get(currentFeature)));
		}
		return statistics;
	}
	
}
