package edu.mit.csail.sls.nut.databaseLookup.usda;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class TreeGenerator {

	public static TreeNode generateTree(ArrayList<ReturnableItem> items) {
//		System.out.println (items.toString());
		if (items.isEmpty()) {
			return null;
		}
		if (items.size() == 1) {
			return (TreeNode) items.get(0);
		}
		
		//Check if all items are similar to eachother and should be composite node
		if (itemsSimilar(items)) {
			return new CompositeItem(items);
		}
		TreeNode node = new TreeNode();
		String feature = selectBestFeature(items);
		node.setFeatureName(feature);
		
		//Generate items for each side of tree
		ArrayList<ReturnableItem> yesItems = new ArrayList<ReturnableItem>();
		ArrayList<ReturnableItem> noItems = new ArrayList<ReturnableItem>();
		for (int i=0; i<items.size(); i++) {
			if (items.get(i).hasFeature(feature)) {
				yesItems.add(items.get(i));
			} else {
				noItems.add(items.get(i));
			}
		}
		System.out.println("Feature selected: "+feature + " yes:" + yesItems.size()+ " no: "+ noItems.size());
		
			node.setYesChild(generateTree(yesItems));
			node.setNoChild(generateTree(noItems));
		return node;
	}
	
	public static String drawTree (TreeNode root) {
		String toreturn = "";
		toreturn += root.getFeatureName() + "\r";
		if (root.type.equals(TreeNode.nodeType.LEAF)) {
			return root.toString();
		}
		boolean hasNextLevel = true;
		ArrayList<TreeNode> currentLevel = new ArrayList<TreeNode>();
		ArrayList<TreeNode> nextLevel = new ArrayList<TreeNode>();
		currentLevel.add(root.getYesChild());
		currentLevel.add(root.getNoChild());
//		System.out.println("yes"+root.getYesChild() + "no"+root.getNoChild());
		while (hasNextLevel) {
			
//			System.out.println(currentLevel.size());
			
			hasNextLevel = false;
			for (int i=0; i<currentLevel.size(); i++) {
				TreeNode currentNode = currentLevel.get(i);
				if (currentNode.getType().equals(TreeNode.nodeType.LEAF)) {
					toreturn += ((USDAItem)currentNode).getFoodID()+"; ";
					nextLevel.add(new TreeNode(TreeNode.nodeType.FILLER));
					nextLevel.add(new TreeNode(TreeNode.nodeType.FILLER));
					
				} else if (currentNode.getType().equals(TreeNode.nodeType.FILLER)) {
					
					toreturn += "-- ; ";
					nextLevel.add(new TreeNode(TreeNode.nodeType.FILLER));
					nextLevel.add(new TreeNode(TreeNode.nodeType.FILLER));
					
				} else  {
					hasNextLevel= true;
					nextLevel.add(currentNode.getYesChild());
					nextLevel.add(currentNode.getNoChild());
					toreturn += currentNode + "; ";
				}
			}
			
			currentLevel = nextLevel;
			nextLevel= new ArrayList<TreeNode>();
			toreturn += "\r";
		}
		return toreturn;
	}
	
	public static String drawLargeTree (TreeNode root) {
		String toreturn = "";
		toreturn += root.getFeatureName() + "\r";
		if (root.type.equals(TreeNode.nodeType.LEAF)) {
			return root.toString();
		}
		boolean hasNextLevel = true;
		ArrayList<TreeNode> currentLevel = new ArrayList<TreeNode>();
		ArrayList<TreeNode> nextLevel = new ArrayList<TreeNode>();
		currentLevel.add(root.getYesChild());
		currentLevel.add(root.getNoChild());
		while (hasNextLevel) {
			
//			System.out.println(currentLevel.size());
			
			hasNextLevel = false;
			for (int i=0; i<currentLevel.size(); i++) {
				TreeNode currentNode = currentLevel.get(i);
				if (currentNode.getType().equals(TreeNode.nodeType.LEAF)) {
					toreturn += currentNode+"; ";
//					nextLevel.add(new TreeNode(TreeNode.nodeType.FILLER));
//					nextLevel.add(new TreeNode(TreeNode.nodeType.FILLER));
					
				} else if (currentNode.getType().equals(TreeNode.nodeType.FILLER)) {
					
					toreturn += "-- ; ";
//					nextLevel.add(new TreeNode(TreeNode.nodeType.FILLER));
//					nextLevel.add(new TreeNode(TreeNode.nodeType.FILLER));
					
				} else  {
					hasNextLevel= true;
					nextLevel.add(currentNode.getYesChild());
					nextLevel.add(currentNode.getNoChild());
					toreturn += currentNode + "; ";
				}
			}
			
			currentLevel = nextLevel;
			nextLevel= new ArrayList<TreeNode>();
			toreturn += "\r";
		}
		return toreturn;
	}

	public static String selectBestFeature (ArrayList<ReturnableItem> items) {
		Set<String> allFeatures = new HashSet<>();
		for (ReturnableItem currentItem: items) {
			allFeatures.addAll(currentItem.getFeatures());
		}
		String bestFeature ="";
		double lowestEntropy = -1;
		
		for (String currentFeature: allFeatures) {
			double entropy = calculateEntropy(items, currentFeature);
//			System.out.println("Entropy for " + currentFeature +" is" + entropy);
			if (lowestEntropy == -1 || entropy  < lowestEntropy) {
				bestFeature = currentFeature;
				lowestEntropy = entropy;
			}
			
			
		}
//		System.out.println("Selected feature: " + bestFeature);
		return bestFeature;
		
	}
	
	public static double calculateEntropy (ArrayList<ReturnableItem> items, String feature) {
		int countyes = 0;
		for (int i=0; i<items.size(); i++) {
			if (items.get(i).hasFeature(feature)) {
				countyes++;
			}
		}
		
		//entropy is the sum of p*logp
		double probabilityYes = 1.0*countyes/items.size();
		double probabilityNo = 1-probabilityYes;
//		System.out.println("Probabilities for entropy:" + probabilityYes + " " + probabilityNo);
		if (probabilityNo == 1 || probabilityYes == 1) {
			return Double.POSITIVE_INFINITY;
		}
		
		double entropy = -(probabilityYes * (Math.log(probabilityYes) / Math.log(2)) - probabilityNo *
				(Math.log(probabilityNo) / Math.log(2)));
//		System.out.println("Entropy:"+feature+ entropy+" yes:"+probabilityYes+ "no:"+probabilityNo);
		
		return entropy;
	}
	
	/**
	 * Returns whether a list of greater than 2 items are similar enough to be combined
	 * Similar currently means the low and the high they are within 10% or 10 calories of eachother
	 */
	private static boolean itemsSimilar (ArrayList<ReturnableItem> items) {
		double lower=items.get(0).getCalories();
		double higher=items.get(0).getCalories();
		
		for (int i=1; i<items.size();i++) {
			double currentcalories = items.get(i).getCalories();
			if (currentcalories < lower) {
				lower = currentcalories;
			} else if (currentcalories > higher) {
				higher = currentcalories;
			}
		}
		
		return (1.0*(higher-lower)/higher < .1 || (higher-lower) <= 15);
	}
	
}
