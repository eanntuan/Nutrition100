package edu.mit.csail.sls.nut;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.util.JSONPObject;

import edu.mit.csail.asgard.syntax.CRFSegment;
import edu.mit.csail.asgard.syntax.CRFToken;
import edu.mit.csail.asgard.syntax.Features;
import edu.mit.csail.asgard.syntax.ParentNode;
import edu.mit.csail.asgard.syntax.ParseNode;
import edu.mit.csail.asgard.syntax.Sentence;


/**
 * Servlet implementation class Parse
 */
@WebServlet(name = "parse", urlPatterns = { "/parse" })
public class Parse extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	static class JNode {
		JNode(String name){
			this.name = name;
		}
		@JsonProperty
		String name;
	}
	
	static class JTokenNode extends JNode {
		JTokenNode(String name, int position){
			super(name);
			this.position = position;
		}
		@JsonProperty
		int position;
	}
	
	static class JTreeNode extends JNode {
		JTreeNode(String name){
			super(name);
			children = new ArrayList<JNode>();
		}
		@JsonProperty
		ArrayList<JNode> children;
	}
	
	static JNode convertNode(ParseNode node){
    	if (node.isLeaf()){
    		CRFToken token = (CRFToken)node;
    		return new JTokenNode(node.text, token.position);
    	} else {
    		JTreeNode result = new JTreeNode(node.text);
    		for(ParseNode child : ((ParentNode)node).children){
    			result.children.add(convertNode(child));
    		}
    		return result;
    	}
	}

	static class JToken {
		@JsonProperty
		String pos;
		@JsonProperty
		String name;
		
		JToken(String pos, String name){
			this.pos = pos;
			this.name = name;
		}
	}
	
	static class JSegment {
		@JsonProperty
		String name;
		@JsonProperty
		int from;
		@JsonProperty
		int to;
		
		JSegment(String name, int from, int to){
			this.name = name;
			this.from = from;
			this.to = to;
		}
	}
	
	static class JSentence {
		@JsonProperty
		ArrayList<JToken> tokens;
		@JsonProperty
		ArrayList<JSegment> segments;
		@JsonProperty
		JNode stanford;
		@JsonProperty
		Object[] dependencies;
		
		JSentence(){
			tokens = new ArrayList<JToken>();
			segments = new ArrayList<JSegment>();
			stanford = null;
		}
	}
	
	static JSentence convertSentence(Sentence sentence){
    	JSentence result = new JSentence();
    	// Get the tokens
    	for(CRFToken token : sentence.tokens){
    		result.tokens.add(new JToken(token.pos, token.text));
    	}
    	for(CRFSegment segment : sentence.segments){
    		List<CRFToken> crfTokens = segment.tokens;
    		result.segments.add(new JSegment(segment.crfClass.getName(), crfTokens.get(0).position, crfTokens.get(crfTokens.size()-1).position));
    	}
    	result.stanford = convertNode(sentence.getRoot());
    	result.dependencies = sentence.deps;
    	return result;
	}
	
    int getIntegerParameter(HttpServletRequest request, String name, int defaultValue){
    	String param = request.getParameter(name);
    	if (null == param)
    		return defaultValue;
    	
    	return Integer.parseInt(param);
    }

	
	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String jsonp = request.getParameter("jsonp");
		String text = request.getParameter("text");
		Sentence sentence = new Sentence();
		Features.nlparser_pos_english.compute(sentence, text);
		NutritionContext.sentenceTagger.addCRFClasses(sentence);

		Object result = convertSentence(sentence);
		if (null != jsonp)
			result = new JSONPObject(jsonp, result);
		ObjectMapper objectMapper = new ObjectMapper();
		response.setContentType("application/javascript");
		PrintWriter writer = response.getWriter();
		objectMapper.writeValue(writer, result);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

}
