package edu.mit.csail.sls.nut;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.mit.csail.asgard.syntax.Features;
import edu.mit.csail.asgard.syntax.Sentence;
import edu.mit.csail.asgard.syntax.SentenceTagger;

public class FoodConflicts {

	/**
	 * For every diary with round 1 food votes, check whether multiple foods
	 * are in same segment using CRF++ BIO labels.
	 * Select the food with majority votes and write to file "foodConflicts."
	 * Also uses POS tags to determine which foods are actually descriptions.
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
			PrintWriter writer = new PrintWriter("foodConflicts", "UTF-8");
			SentenceTagger sentenceTagger = Eval.initialize();
			
			// initialize variables
			ArrayList<String> output = null; // CRF++ output
			ArrayList<String> tokens = new ArrayList<String>();
			List<String> labels = new ArrayList<String>();
			
			// maps food index to number of votes
			Map <Integer, Integer> foodVotes = new HashMap<>(); 
			
			// get tokens and labels from allVotes file
			String path = "src/edu/mit/csail/sls/nut/allVotes_round1";
			BufferedReader br = new BufferedReader(new FileReader(path));
			String line;
			int index = 0;
			int numDiaries = 0;
			String text = "";
			// loop through every token in each diary
			while ((line = br.readLine()) != null) {
				// reached the end of one diary, move onto the next
				if (line.equals("")){
					System.out.println("\nVotes: "+foodVotes);
					output = CRF.getOutputFST(tokens, labels, writer, "IOE"); // get CRF segmentation
					List<Integer> multFoods = checkForConflictFoods(labels, tokens, output, foodVotes, writer);
					getPOSTags(text, sentenceTagger, tokens, labels, writer);
					numDiaries += 1;
					
					// re-initialize variables
					output = null; // CRF++ output
					tokens = new ArrayList<String>();
					labels = new ArrayList<String>();
					foodVotes = new HashMap<>(); 
					index = 0;
					text = "";
					continue;
				}
			   // remove parentheses
			   line = line.replace("(", "");
			   line = line.replace(")", "");
			   
			   // count food votes
			   if (line.contains(" ")){
				   labels.add("food");
				   tokens.add(line.split(" ")[0]);
				   text += line.split(" ")[0]+" ";
				   Integer votes = line.split(" ").length-1;
				   foodVotes.put(index, votes);
			   } else {
				   labels.add("other");
				   tokens.add(line);
				   text += line+" ";
			   }
			   index += 1;
			   
			   /*
			   if (numDiaries > 100){
				   break;
			   }
			   */
			   
			}
			br.close();		
			writer.close();
			System.out.println("numDiaries: "+numDiaries);
		}
	
	/*
	 * Determines POS of each token, then writes foods that aren't nouns.
	 */
	public static void getPOSTags(String text, SentenceTagger tagger, ArrayList<String> tokens, List<String> labels, PrintWriter writer){
		Sentence sentence = new Sentence();
		sentence.originalText = text;
		sentence.isNutrition = true;
		Features.nlparser_pos_english.compute(sentence, text);
		//System.out.println(text);
		int indexTokens = 0;
		for (int indexSent = 0; indexSent < sentence.tags.length; indexSent++){
			String tag = sentence.tags[indexSent];
			boolean tokensMatch = true;
			// if last tokens are only punctuation, move on
			if (indexTokens >= tokens.size()){
				break;
			}
			// ensure tagged token matches whitespace-tokenized token
			if (!tokens.get(indexTokens).toLowerCase().equals(sentence.tokens.get(indexSent).text) && !sentence.tokens.get(indexSent).text.replace("\\/", "/").equals(tokens.get(indexTokens))){
				// check if actually didn't split into tokens
				if (tokens.get(indexTokens).toLowerCase().equals("1") && sentence.tokens.get(indexSent).text.equals("1 1\\/2")){
					indexSent = indexSent - 2;
				}
				
				// skip if length is 2, but token is just "i"
				// check if replaced "grey" with "gray" (i.e. same lengths)
				else if (!(tokens.get(indexTokens).toLowerCase().length()==2 && sentence.tokens.get(indexSent).text.equals("i")) && !(tokens.get(indexTokens).toLowerCase().equals("grey"))) {
					tokensMatch = false;
				}
			}
			System.out.println(tokens.get(indexTokens).toLowerCase()+" "+sentence.tokens.get(indexSent).text);
			// determine POS of food tokens
			if (labels.get(indexTokens).equals("food")) {
				String food = tokens.get(indexTokens);
				
				// check for non-noun foods
				if (!tag.contains("NN")){
					System.out.println("Not noun! "+food+" "+tag);
					writer.write("\nNot noun! "+food+" "+tag);
				}
			} 
			if (!tokensMatch){
				indexSent++;
			}
			
			indexTokens++;
		}
	}
	
	/*
	 * Find foods that are in the same segment.
	 * Only include foods that are adjacent to each other.
	 */
	public static List<Integer> checkForConflictFoods(List<String> labels, ArrayList<String> tokens, ArrayList<String> output, Map <Integer, Integer> foodVotes, PrintWriter writer){

		// check for multiple foods in same segment
		int numFoodsSameSeg = 0;
		int i = 0;
		List<Integer> multFoods = new ArrayList<Integer>();
		for (String label : output) {
			// reset num foods in same segment to 0 
			if (label.equals("#")){
				numFoodsSameSeg = 0;
				multFoods = new ArrayList<Integer>();
				continue;
			}
			// increment count
			else {

				// loop through labels until find a match
				while (!labels.get(i).substring(0, 1).toUpperCase().equals("F")){
					//System.out.println("inside loop "+labels.get(i)+" "+i);
					i += 1;
				}
				// add food if none added yet
				if (multFoods.size() == 0) {
					multFoods.add(i);
					numFoodsSameSeg += 1;
				} 
				// add food if adjacent to prev food
				else if (multFoods.get(multFoods.size()-1)==i-1){
					multFoods.add(i);
					numFoodsSameSeg += 1;
				}
				// start over if prev multFoods > 2, but current food not adjacent
				else if (multFoods.size() > 1){
					getMajority(multFoods, tokens, foodVotes, writer);
					multFoods = new ArrayList<Integer>();
					multFoods.add(i);
					numFoodsSameSeg = 1;
				}
				
				//System.out.println("added food "+tokens.get(i)+" "+i+" "+multFoods);
				i+=1;
			}
			// break if >1 foods in same segment
			if (numFoodsSameSeg > 1){
				getMajority(multFoods, tokens, foodVotes, writer);
			}
		}
		return multFoods;
	}
	
	/*
	 * Of the foods in the same segment, find that with the most Turker votes.
	 * Break ties by choosing food that comes last.
	 */
	public static void getMajority(List<Integer> multFoods, ArrayList<String> tokens, Map <Integer, Integer> foodVotes, PrintWriter writer){
		int majorityVote = 0;
		int majorityIndex = 0;
		System.out.println("conflicting foods! "+multFoods);
		writer.write("\n\nconflicting foods! "+multFoods);
		for (int j : multFoods){
			System.out.println(tokens.get(j));
			writer.write("\n\t"+tokens.get(j)+", votes: "+foodVotes.get(j));
			// select majority vote food (if tie, choose last)
			if (foodVotes.get(j) >= majorityVote){
				majorityVote = foodVotes.get(j);
				majorityIndex = j;
			}
		}
		System.out.println("majority: "+tokens.get(majorityIndex));
		writer.write("\nmajority: "+tokens.get(majorityIndex));
	}

}
