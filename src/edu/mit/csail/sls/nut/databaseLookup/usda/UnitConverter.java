package edu.mit.csail.sls.nut.databaseLookup.usda;

/**
 * Used to assist with unit conversions between common measurements
 */
public class UnitConverter {
	
	public static String[] supportedConversions = new String[] {"tablespoon", "teaspoon", "cup",
		"tsp","tbsp", "fluid ounce", "fl oz"};
	
	/**
	 * This method currently handles a limited amount of conversions, but can be expanded.
	 * @param amount 
	 * @param startingUnit
	 * @param finalUnit
	 * @return
	 */
	public static double convertUnit (double amount, String startingUnit, String finalUnit) {
		System.out.println("Converting from "+startingUnit+" to "+finalUnit);
		if (startingUnit.equals(finalUnit)) {
			return amount;
		}
		if (startingUnit.equals("tbsp") || startingUnit.equals("tablespoon")) {
			if (finalUnit.equals("tsp")) {
				//1 Tablespoon = 3 teaspoon
				return amount*3;
			} else if (finalUnit.equals("cup")) {
				//1 Tablespoon = 1/16 cup
				return amount/16.0;
			} else if (finalUnit.equals(startingUnit.equals("fl oz") || startingUnit.equals("fluid ounce"))) {
				//1 Tablespoon = 1/16 cup
				return amount/2.0;
			} 
		} else if (startingUnit.equals("tsp") || startingUnit.equals("teaspoon")) {
			if (finalUnit.equals("tbsp")) {
				//1 Tablespoon = 3 teaspoon
				return amount/3.0;
			} else if (finalUnit.equals("cup")) {
				//1 Teaspoon = 1/48 cup
				return amount*1.0/48;
			} else if (finalUnit.equals(startingUnit.equals("fl oz") || startingUnit.equals("fluid ounce"))) {
				//1 Tablespoon = 1/16 cup
				return amount/6.0;
			} 
		} else if (startingUnit.equals("cup")){
			if (finalUnit.equals("tbsp")) {
				//1 Tablespoon = 1/16 cup
				return amount*16;
			} else if (finalUnit.equals("tsp")) {
				//1 cup = 48 teaspoon
				return amount*48;
			} else if (finalUnit.equals(startingUnit.equals("fl oz") || startingUnit.equals("fluid ounce"))) {
				//1 Tablespoon = 1/16 cup
				return amount*8;
			} 
		} else if (finalUnit.equals(startingUnit.equals("fl oz") || startingUnit.equals("fluid ounce"))){
			if (finalUnit.equals("tbsp")) {
				//1 Tablespoon = 1/16 cup
				return amount*2.0;
			} else if (finalUnit.equals("tsp")) {
				//1 cup = 48 teaspoon
				return amount*6.0;
			} else if (finalUnit.equals(startingUnit.equals("cup"))) {
				//1 Tablespoon = 1/16 cup
				return amount/8;
			} 
		}
		return -1;
	}
	
	public static String containsConversionItem(String inputString) {
	
	    for(int i =0; i < supportedConversions.length; i++)
	    {
	        if(inputString.contains(supportedConversions[i]))
	        {
	            return supportedConversions[i];
	        }
	    }
	    return null;
	}
}
