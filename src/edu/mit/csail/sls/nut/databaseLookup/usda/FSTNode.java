package edu.mit.csail.sls.nut.databaseLookup.usda;

import java.util.ArrayList;

public interface FSTNode {
	public static enum type {
		Attribute, Item
	}

	public type getFSTType ();
	
	public void compressTree (ArrayList<String> attributesKept, ArrayList<String> attributesRemoved, boolean first);
	public ArrayList<ReturnableItem> itemsUpstream ();
	public int depth ();
	public ArrayList<String> attributesUpstream ();

	public void hide();
}
