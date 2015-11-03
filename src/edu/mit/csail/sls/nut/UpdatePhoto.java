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
@WebServlet("/UpdatePhoto")
public class UpdatePhoto extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public UpdatePhoto() {
        super();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String jsonp = request.getParameter("jsonp");
		String food = request.getParameter("item");
		String brand = request.getParameter("brand");
		if (brand==null) {
			brand="";
		}
		String description = request.getParameter("description");
		String descriptionSpecified = "";
		 String formattedDesString = description.substring(1,description.length()-1);
		 String[] desfeatures=formattedDesString.split(",");
		for (String currentFeature: desfeatures) {
			System.out.println(currentFeature);
			if (!currentFeature.equals("")){
				descriptionSpecified+=" "+currentFeature.replaceAll("\"", "").trim();
			}
			
		}
		descriptionSpecified.trim();
		System.out.println(descriptionSpecified);
			String image="";	
		image = GetImages.getUpdatedImage(food, brand, descriptionSpecified);
		Object result = new Photo(image);
		if (null != jsonp)
			result = new JSONPObject(jsonp, result);
		System.out.println(result);
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
