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
import edu.mit.csail.sls.nut.GetImages;
import edu.mit.csail.sls.nut.databaseLookup.usda.CacheGenerator;
//import edu.mit.csail.sls.nut.databaseLookup.usda.QuantityTester;
//import edu.mit.csail.sls.nut.databaseLookup.semantics3.Semantics3Lookup;
import edu.mit.csail.sls.nut.databaseLookup.usda.USDALookup;

/**
 * Servlet implementation class Images
 */
@WebServlet("/UpdatePhoto2")
public class UpdatePhoto2 extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public UpdatePhoto2() {
        super();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		System.out.println("in update photo 2");
		String jsonp = request.getParameter("jsonp");
		String foodID = request.getParameter("foodID");
		String image="";
		image = GetImages.getNewImage(foodID);
		Object result = new Photo(image);
		if (null != jsonp)
			result = new JSONPObject(jsonp, result);
		System.out.println(result);
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
