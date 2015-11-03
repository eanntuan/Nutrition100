package edu.mit.csail.sls.nut;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Arrays;

import org.apache.commons.lang.StringUtils;

public class FST {

	public static void main(String[] args) {
		try {
	    	PrintWriter writer = new PrintWriter("fst_segmentations.txt", "UTF-8");
			//getOutput("Q B D F", 5, writer);
	    	getOutput("Q D F B D F D F", 5, writer);
	    	writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static String[] getOutput(String input, int n, PrintWriter writer) throws IOException{
		System.out.println("input: "+input);
		String inputNoSpaces = input.replaceAll("\\s+","");
		String filename = inputNoSpaces+".fst";
		File dir = new File("fst_lab");
		// call fst_from_string on input, output out.fst
		Process p = Runtime.getRuntime().exec("fst_from_string out.fst "+input, null, dir);
		
		// compose with foods.fst, output out2.fst
		Process q = Runtime.getRuntime().exec("fst_compose -t out.fst foods.fst out2.fst", null, dir);

		// output nbest paths through final fst, output results to standard output
		Process r = Runtime.getRuntime().exec("fst_nbest -o -n "+n+" -s -p out2.fst out3.fst", null, dir);
		BufferedReader in = new BufferedReader( 
				new FileReader("fst_lab/out3.fst"));
				
		String line = null;  
		String firstLine = "";
		// retry if output file is empty
		if ((firstLine = in.readLine()) == null){
			return getOutput(input, n, writer);
		} 
		// print results
		System.out.println(firstLine);

		String[] firstLineArr = firstLine.split("\\s+");
		// retry if first line is not long enough
		if (firstLineArr.length < 2){
			return getOutput(input, n, writer);
		}
		firstLineArr = Arrays.copyOfRange(firstLineArr, 2, firstLineArr.length);
		
		// make sure this is the output for the correct input
		String firstLineStr = "";
		for (String s : firstLineArr){
			if (!s.equals("#")){
				firstLineStr += s + " ";
			}
		}
		firstLineStr = firstLineStr.substring(0, firstLineStr.length() - 1);
		
		// if first line without '#' doesn't match input, try again
		if (!firstLineStr.equals(input)){
			//System.out.println("input mismatch "+firstLineStr);
			return getOutput(input, n, writer);
		}
		
		// if first line starts with "null" instead of tab, retry
		if (!firstLine.substring(0, 2).equals("  ")){
			System.out.println(firstLine.substring(0, 2));
			return getOutput(input, n, writer);
		}
		
		// write top n outputs to file
		if (writer != null) {
			writer.println("\nInput: "+input);
			writer.println("Output: ");
			writer.println(firstLine);
			while ((line = in.readLine()) != null) {  
				System.out.println(line);  
				writer.println(line);
			} 
		}
		in.close();
				
		// return first line of file
		return firstLineArr;
	
	}

}
