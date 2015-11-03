package edu.mit.csail.sls.nut.databaseLookup.usda;

public class TreeNode {

	public static enum nodeType {
		LEAF, INTERNAL, FILLER
	};
	protected nodeType type = nodeType.INTERNAL;
	
	private String featureName;
	
	private TreeNode yesChild;
	private TreeNode noChild;
	
	public TreeNode () {
		
	}
	
	public TreeNode (nodeType type) {
		this.type = type;
	}

	/**
	 * @return the featureName
	 */
	public String getFeatureName() {
		return featureName;
	}

	/**
	 * @param featureName the featureName to set
	 */
	public void setFeatureName(String featureName) {
		this.featureName = featureName;
	}

	public nodeType getType() {
		return type;
	}
	
	public void setType (nodeType type) {
		this.type=type;
	}

	public TreeNode getYesChild() {
		return yesChild;
	}

	public void setYesChild(TreeNode yesChild) {
		this.yesChild = yesChild;
	}

	public TreeNode getNoChild() {
		return noChild;
	}

	public void setNoChild(TreeNode noChild) {
		this.noChild = noChild;
	}

	public String toString () {
		return featureName;
	}
	
}
