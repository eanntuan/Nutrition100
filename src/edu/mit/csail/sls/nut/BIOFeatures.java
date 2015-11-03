package edu.mit.csail.sls.nut;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

import edu.mit.csail.asgard.syntax.CRFSegment;
import edu.mit.csail.asgard.syntax.CRFToken;
import edu.mit.csail.asgard.syntax.Sentence;
import edu.mit.csail.sls.nut.databaseLookup.*;
import edu.mit.csail.sls.nut.databaseLookup.nutritionix.NutritionixResponse;
import edu.mit.csail.sls.nut.databaseLookup.semantics3.Semantics3NutritionObject;
import edu.mit.csail.sls.nut.databaseLookup.usda.ReturnableItem;
import edu.mit.csail.sls.nut.databaseLookup.usda.USDAItem;
import edu.mit.csail.sls.nut.databaseLookup.usda.USDAResult;

public class BIOFeatures {
	@JsonProperty
	String POS;
	@JsonProperty
	String semiCRFLabel;
	@JsonProperty
	String AMTLabel;
	@JsonProperty
	String predictedBIO;
	@JsonProperty
	String AMTBIO;
	
	public BIOFeatures() {
		// TODO Auto-generated constructor stub
	}
	
	
	// setters
	public void setPOS(String input) {
		POS = input;
	}
	
	public void setsemiCRFLabel(String input) {
		semiCRFLabel = input;
	}
	
	public void setAMTLabel(String input) {
		AMTLabel = input;
	}
	
	public void setpredictedBIO(String input) {
		predictedBIO = input;
	}
	
	public void setAMTBIO(String input) {
		AMTBIO = input;
	}
	
	// getters
	public String getPOS() {
		return POS;
	}
	
	public String getsemiCRFLabel() {
		return semiCRFLabel;
	}
	
	public String getAMTLabel() {
		return AMTLabel;
	}
	
	public String getpredictedBIO() {
		return predictedBIO;
	}
	
	public String getAMTBIO() {
		return AMTBIO;
	}
}
