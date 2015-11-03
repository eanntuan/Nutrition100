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
//import edu.mit.csail.sls.nut.databaseLookup.semantics3.Semantics3Lookup;
import edu.mit.csail.sls.nut.databaseLookup.usda.USDALookup;

/**
 * Servlet implementation class Images
 */
@WebServlet("/FeatureLookup")
public class FeatureLookup extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public FeatureLookup() {
		super();
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
//		System.out.println(request.getParameterNames());
		String jsonp = request.getParameter("jsonp");
		String items = request.getParameter("item");
		String brand = request.getParameter("brand");
		if (brand==null) {
			brand="";
		}
		int level = Integer.parseInt(request.getParameter("level"));
		String quantity = request.getParameter("quantity");
		if (quantity==null) {
			quantity="";
		}
		String featureChosen = request.getParameter("featureChosen");
		System.out.println("57" +featureChosen);
		ArrayList<String> adjectivesSpecified = new ArrayList<String>();
		 String formattedString = featureChosen.substring(1,featureChosen.length()-1);
		 String[] features=formattedString.split(",");
		for (String currentFeature: features) {
			System.out.println(currentFeature);
			if (!currentFeature.equals("")){
				adjectivesSpecified.add(currentFeature.replaceAll("\"", "").trim());
			}
			
		}
		if (adjectivesSpecified.size()>0) {
		System.out.println(64+" "+adjectivesSpecified.get(0));
		}
		System.out.println(adjectivesSpecified);
		String description = request.getParameter("description");
		ArrayList<String > descriptionSpecified = new ArrayList<String>();
		 String formattedDesString = description.substring(1,description.length()-1);
		 String[] desfeatures=formattedDesString.split(",");
		for (String currentFeature: desfeatures) {
			System.out.println(currentFeature);
			if (!currentFeature.equals("")){
				descriptionSpecified.add(currentFeature.replaceAll("\"", "").trim());
			}
			
		}
		System.out.println(descriptionSpecified);

		Object result = USDALookup
				.foodItemAdjectiveLookup(items, descriptionSpecified,adjectivesSpecified ,brand, level, quantity);
		if (null != jsonp)
			result = new JSONPObject(jsonp, result);
		response.setContentType("application/javascript");
		PrintWriter writer = response.getWriter();
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.writeValue(writer, result);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

	}

}
