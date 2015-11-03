package edu.mit.csail.sls.nut.databaseLookup.nutritionix;

public class NutritionixResponse {
	String total;
	String max_score;
	NutritionixResponseHit[] hits;

	public NutritionixResponse() {

	}

	public String toString() {

		String toreturn = "Total: " + total + " max_score:" + max_score
				+ " hits:";
		for (int i = 0; i < hits.length; i++) {
			toreturn += hits[i].toString();
		}
		return toreturn;
	}

	public NutritionixResponse(String p_total, String p_max_score,
			NutritionixResponseHit[] p_hits) {
		total = p_total;
		max_score = p_max_score;
		hits = p_hits;
	}

	public String getTotal() {
		return total;
	}

	public void setTotal(String total) {
		this.total = total;
	}

	public String getMax_score() {
		return max_score;
	}

	public void setMax_score(String max_score) {
		this.max_score = max_score;
	}

	public NutritionixResponseHit[] getHits() {
		return hits;
	}

	public void setHits(NutritionixResponseHit[] hits) {
		this.hits = hits;
	}

}
