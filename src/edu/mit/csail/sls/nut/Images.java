package edu.mit.csail.sls.nut;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.SerializationUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.util.JSONPObject;

import edu.mit.csail.asgard.syntax.CRFToken;
import edu.mit.csail.asgard.syntax.Features;
import edu.mit.csail.asgard.syntax.Sentence;
import edu.mit.csail.sls.nut.databaseLookup.usda.CacheGenerator;
//import edu.mit.csail.sls.nut.databaseLookup.usda.QuantityTester;
//import edu.mit.csail.sls.nut.databaseLookup.semantics3.Semantics3Lookup;
import edu.mit.csail.sls.nut.databaseLookup.usda.USDALookup;

/**
 * Servlet implementation class Images
 */
@WebServlet("/Images")
public class Images extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public Images() {
        super();
    }
    


	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		System.out.println(request.toString());
		long starttime = System.currentTimeMillis();
		System.out.println("Start time: " + starttime);
		// get the type of approach for associating foods with attributes
		String segment_type = request.getParameter("segment_type"); 
		String labelType = request.getParameter("labelRep");
		String tag_type = request.getParameter("tag_type");
		// TODO: re-use CRF labeling from Tag.java
		/*
		Object data = request.getParameter("data"); 
		JSONObject jObject = new JSONObject(data);
		JSONArray jarray = (JSONArray) jObject.get("bytes");
		byte[] bytes = new byte[jarray.length()];
		for (int i=0;i<jarray.length();i++){ 
		    bytes[i] = (byte) jarray.get(i);
		} 
		Object deserializedData = SerializationUtils.deserialize(bytes);
		//Object deserializedData = objectMapper.readValue(jObject);
		System.out.println("data: "+deserializedData);
		*/
		// re-computes CRF labeling, then gets images and db data
		String jsonp = request.getParameter("jsonp");
		String textWithPunc = request.getParameter("text");
		System.out.println(textWithPunc);
				
		// run CRF and get food-attribute dependencies
		PrintWriter FSTwriter = null;
		Segmentation segmentation = new Segmentation();

		try {
			NLPData NLPresult = Tag.runCRF(FSTwriter, textWithPunc, segment_type, NutritionContext.sentenceTagger, false, labelType, tag_type);
			
			segmentation.text = NLPresult.text;
			segmentation.tokens = NLPresult.tokens;
			segmentation.labels = NLPresult.labels;
			segmentation.tags = NLPresult.tags;
			segmentation.segments = NLPresult.segments;
			segmentation.parse = NLPresult.parse;
			segmentation.deps = NLPresult.deps;
			segmentation.foods = NLPresult.foods;
			segmentation.attributes = NLPresult.attributes;
		} catch (ClassNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		//Add calls to get database lookups for food item
		System.out.println("");
		
		

		segmentation.results=USDALookup.foodItemInitialLookup(segmentation.attributes, segmentation.tokens);
//		segmentation.semantic3results=Semantics3Lookup.foodItemLookup(segmentation.attributes, segmentation.tokens);

long beforeImages = System.currentTimeMillis();
		//Map<String, String> foodImages = GetImages.getImagesDB(segmentation.foods);
		segmentation.images = GetImages.getImageEncodings();
		
		/*
		System.out.println ("Printing segmentation images value 1");
		
		for (Map.Entry<String, String> entry : segmentation.images.entrySet()) {
		    String key = entry.getKey();
		    String value = entry.getValue();

		    System.out.println ("Food: " + key + ", Image Path: " + value);
		}    
		*/
		
		//segmentation.images = GetImages.getImages(segmentation.foods, segmentation.attributes, segmentation.tokens);
		
		//System.out.println ("Printing segmentation images value 2");
		/*
		for (Map.Entry<String, String> entry : segmentation.images.entrySet()) {
		    String key = entry.getKey();
		    String value = entry.getValue();

		    System.out.println ("Food: " + key + " Image Path: " + value);
		}   */
		
		long endTime = System.currentTimeMillis();
		System.out.println("Total time: "+(endTime-starttime)+" images time: "+(endTime-beforeImages));
				
		Object result = segmentation;
		if (null != jsonp)
			result = new JSONPObject(jsonp, result);
		response.setContentType("application/javascript");
		PrintWriter writer = response.getWriter();
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.writeValue(writer, result);
		
//		QuantityTester.performQuantityTest();;
//		CacheGenerator.processSentences();
//		GetImages.loadCacheImages();
		
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	}

}
