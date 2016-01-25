package edu.mit.csail.sls.nut.databaseLookup.usda;

import java.util.ArrayList;


/**
 * Node for use in constructing FST 
 */
public class AttributeNode implements FSTNode {
	
	private String attribute;
	ArrayList<FSTNode> next;
	private boolean needed=true;
	
	static double percentClose = .15;
	static double numberClose = 20;
	
	public AttributeNode(String attribute) {
//		System.out.println("Attribute node made:" + attribute);
		this.attribute = attribute;
		this.next = new ArrayList<FSTNode>();
	}
	
	public FSTNode.type getFSTType() {
		return FSTNode.type.Attribute;
	}

	/**
	 * @return the attribute
	 */
	public String getAttribute() {
		return attribute;
	}

	/**
	 * Returns node directly after this one with the required attribute or null if it is not found
	 */
	public FSTNode nextNode(String attribute) {
		for (int i=0; i<next.size(); i++) {
			FSTNode currentNode = next.get(i);
//			System.out.println("searching" + currentNode+ " "+currentNode.getFSTType());
			if (currentNode.getFSTType().equals(FSTNode.type.Attribute)
					&& ((AttributeNode)currentNode).getAttribute().equals(attribute)) {
				return next.get(i);
			}
		}
		return null;
	}
	
	public void addNext (FSTNode node) {
		next.add(node);
	}
	
	public String toString () {
		return attribute;
	}
	
	/**
	 * If all the final items are the same, reduces this next to a composite item. 
	 * Otherwise, calls compress on all its next items. 
	 * First = whether this is the first that has 1 ancestor (so it is false if the node before it only had 1 child).
	 */
	public void compressTree (ArrayList<String> attributesKept, ArrayList<String> attributesRemoved, boolean first) {
		if (next.size()==0) {
			System.err.println("Node has no children.");
			return;
		} 
		
		//Remove full nodes that are similar to NFS
		USDAItem nfsNode = getNFSNode();
		if (nfsNode !=null) {
			ArrayList<FSTNode> nextCopy = new ArrayList<FSTNode>(next);
			for (FSTNode currentNode: nextCopy) {
				if (itemsSimilarNFS(currentNode.itemsUpstream(), nfsNode)) {
					if (!next.remove(currentNode)) {
						System.err.println("Current node is not in next " + currentNode);
					};
					currentNode.hide();
					System.out.println("Removed from "+attribute+" because similar to NFS/NS" + currentNode);
					attributesRemoved.addAll(currentNode.attributesUpstream());
				}
			}
			next.add(nfsNode);
		}
		
		ArrayList<ReturnableItem> allItems = itemsUpstream();
		
		if (next.size() == 1) {
			if (!first) {
				System.out.println("Removed bc not first: " + attribute);
				this.needed = false;
				attributesRemoved.add(attribute);
			} else {
				System.out.println("First with 1 option: " + attribute);
				this.needed = true;
				attributesKept.add(attribute);
			}
			
				next.get(0).compressTree(attributesKept, attributesRemoved, false);
			
		} else {
			
			
			//If all items upstream are similar, this node is not needed and all can be compressed
			if (itemsSimilar(allItems)) {
				this.needed = false;
				System.out.println("Added bc final then all upstream similar: " + attribute);
				attributesKept.add(attribute);
				attributesRemoved.addAll(attributesUpstream());
				CompositeItem item = new CompositeItem(allItems);
				next.clear();
				next.add(item);
			} else if (allItems.size() ==2) {
				//Add only if 1 is a food item
				if (next.get(0).getFSTType().equals(FSTNode.type.Item) || next.get(1).getFSTType().equals(FSTNode.type.Item)) {
					System.out.println("Adding attribute bc 2 items: " + attribute);
					attributesKept.add(attribute);
				}
				
				next.get(0).compressTree(attributesKept, attributesRemoved, true);
				next.get(1).compressTree(attributesKept, attributesRemoved, true);
			} else {
			
				//Loop through and see if any combos can be compressed
				boolean nodeeliminated = false;
				
//				for (int i=0; i<next.size(); i++) {
//					ArrayList<FSTNode> tocheck = new ArrayList<FSTNode>(next);
//					FSTNode removed = tocheck.remove(i);
//					if (removed.getFSTType().equals(FSTNode.type.Attribute)) {
//					ArrayList<ReturnableItem> returnable = AttributeNode.itemsUpstream(tocheck);
//					if (itemsSimilar(returnable)) {
////						System.out.println("Isolated:" + attribute);
//						
//						nodeeliminated = true;
////						FSTNode different = next.get(i);
////						System.out.println("Isolated:" + different);
//						attributesKept.add(removed.toString());
//						attributesKept.add(attribute);
//						next.clear();
//						next.add(removed);
//						next.add(new CompositeItem(returnable));
//						attributesRemoved.addAll(attributesUpstream(tocheck));
//						System.out.println("Removed all others bc one of " + removed);
//						break;
//					}
//				}
//				}
				
				if (nodeeliminated) {
					this.needed = false;
					attributesRemoved.add(attribute);
				} else {
//					System.out.println("None isolated:" + attribute);
					attributesKept.add(attribute);
				}
				
				//Compress all nodes left
				for (int i = 0; i < next.size(); i++) {
					next.get(i).compressTree(attributesKept,attributesRemoved, true);
				}
			}
		}
	}
	
	public void setAttribute(String attribute) {
		this.attribute = attribute;
	}

	public static ArrayList<ReturnableItem> itemsUpstream(ArrayList<FSTNode> items) {
		ArrayList<ReturnableItem> toreturn = new ArrayList<ReturnableItem>();
		for (FSTNode currentNode: items) {
			toreturn.addAll(currentNode.itemsUpstream());
		}
		return toreturn;
	}
	
	public ArrayList<ReturnableItem> itemsUpstream() {
		return AttributeNode.itemsUpstream(next);
	}
	
	/**
	 * Returns whether a list of greater than 2 items are similar enough to be combined
	 * Similar currently means the low and the high they are within 10% or 10 calories of eachother
	 */
	public static boolean itemsSimilar (ArrayList<ReturnableItem> items) {
		if (items.size() <2) {
			return true;
		}
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
		
		return (1.0*(higher-lower)/higher < percentClose || (higher-lower) <= numberClose);
	}
	
	public ArrayList<FSTNode> getNext() {
		return next;
	}
	
	/**
	 * returns whether or not this attribute matters (i.e. whether or not it has a branch)
	 */
	public boolean shown () {
		return needed;
	}

	@Override
	public int depth() {
		if (shown()) {
			return 1+ next.get(0).depth();
		}
		return next.get(0).depth();	
	}

	@Override
	public ArrayList<String> attributesUpstream() {
		return attributesUpstream(next);
	}
	
	public ArrayList<String> attributesUpstream(ArrayList<FSTNode> nodes) {
		ArrayList<String> toreturn = new ArrayList<String>();
		toreturn.add(attribute);
		for (FSTNode currentNode: nodes) {
			toreturn.addAll(currentNode.attributesUpstream());
		}
		return toreturn;
	}
	
	public static boolean itemsSimilar (ReturnableItem item1, ReturnableItem item2) {
		double higher = item1.getCalories();
		double lower = item2.getCalories();
		if (item2.getCalories()>item1.getCalories()) {
			higher = item2.getCalories();
			lower = item1.getCalories();
		}
		return (1.0*(higher-lower)/higher < percentClose || (higher-lower) <= numberClose);
	}
	
	public static boolean itemsSimilarNFS (ArrayList<ReturnableItem> items, USDAItem nfsNode) {
		for (ReturnableItem item: items) {
			if (!itemsSimilar(nfsNode, item)) {
				return false;
			}
		}
		return true;
	}
	
	private USDAItem getNFSNode () {
		for (FSTNode node: next) {
			if (node.getFSTType().equals(FSTNode.type.Attribute)) {
				if (((AttributeNode)node).getAttribute().equals("NFS") || ((AttributeNode)node).getAttribute().contains("NS")) {
					return (USDAItem) node.itemsUpstream().get(0);
				}
			}
		}
		return null;
	}

	@Override
	public void hide() {
		needed=false;
	}
}
