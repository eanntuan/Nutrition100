package edu.mit.csail.sls.nut;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
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
import com.google.gson.Gson;

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
		System.out.println("servlet request: " + request.toString());
		long starttime = System.currentTimeMillis();
		System.out.println("Start time: " + starttime);
		
		// get the type of approach for associating foods with attributes
		String segment_type = request.getParameter("segment_type"); 
		String labelType = request.getParameter("labelRep");
		String tag_type = request.getParameter("tag_type");
		String jsonp = request.getParameter("jsonp");
		String textWithPunc = request.getParameter("text");
		//String data = request.getParameter("data");
		
		//NLPData NLPresult = new NLPData(null);
		
		System.out.println("text with punc: " + textWithPunc);
		//System.out.println("data: " + data);
		
		//attempt at deserializing object
		//Gson gson = new Gson();
	    //Segmentation testData = gson.fromJson(data, Segmentation.class);
	    //System.out.println("testing test data");
	    //System.out.println(testData);
		
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
		long beforeImages = System.currentTimeMillis();
		
		segmentation.images = GetImages.getImageEncodings();
	

		long endTime = System.currentTimeMillis();
		System.out.println("Total time: "+(endTime-starttime)+" images time: "+(endTime-beforeImages));
				
		Object result = segmentation;
		if (null != jsonp)
			result = new JSONPObject(jsonp, result);
		response.setContentType("application/javascript");
		PrintWriter writer = response.getWriter();
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.writeValue(writer, result);
	
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	}

}
