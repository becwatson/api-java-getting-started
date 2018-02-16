package elit.demo;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.HashMap;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

@WebServlet("/submitServlet")
public class SubmitServlet extends HttpServlet {

	public static void main(String[] args) throws InterruptedException {
		SubmitServlet apis = new SubmitServlet();

		String session_id = Long.toString(System.currentTimeMillis());
		String text_id = session_id; // for now just use same unique ID for text

		JSONObject json = new JSONObject();
		json.put("author_id", "APIDemoExampleAuthor");
		json.put("task_id", "APIDemoExampleTask");
		json.put("session_id", session_id);
		json.put("question_text", "Do you like this example?");
		json.put("text", "It's great, I can see how to the use the API now!");
		json.put("test", 1);

		String url = api_url + "/v2.0.0/account/" + apis.account_id + "/text/" + text_id;

		String response_str = "";

		try {
			response_str = apis.executeHttpRequest(json.toString()
					, url);
		} catch (Exception e) {
			e.printStackTrace();
			response_str = e.getMessage();
		}

		// wait 10 seconds to make sure results are ready:
		Thread.sleep(10000);

		String url_results = api_url + "/v2.0.0/account/" + apis.account_id + "/text/" + text_id + "/results";

		try {
			response_str = apis.executeHttpRequest(null, url_results);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			response_str = e.getMessage();
		}

		System.out.println("Test results = " + response_str);
	}

	private static final long serialVersionUID = 1L;

	private static String api_url = "https://api-staging.englishlanguageitutoring.com";

	public static boolean print_api_response = true;

	private static double sentence_threshold_high = 0.33;

	// Score threshold for low quality sentences
	private static double sentence_threshold_low = -0.33;

	private static String color_sentence_low = "#ffbc99";

	// Colour for medium quality sentences
	private static String color_sentence_med = "#ffee99";

	// Colour for high quality sentences
	private static String color_sentence_high = "#ffffff";

	// Colour for the box around suspect tokens
	private static String color_token_suspect = "#d24a00";

	// Colour for the box around error tokens
	private static String color_token_error = "#d24aff";

	private String account_id;
	private String account_token;

	public SubmitServlet (){
		this.account_id = System.getenv("WI_ACCOUNT_ID");
		this.account_token = System.getenv("WI_ACCOUNT_TOKEN");
	}

	public String getLoadingBox() {
		return "<div class=\"container\" id=\"page-loader\">"
				+ "<div class=\"loader\"></div>"
				+ "</div>";
	}

	public String getDebugBox(String debug_output) {
		return "<div class=\"container\">"
				+ "<div id=\"page-debug\"><pre>"
				+ debug_output 
				+ "</pre>"
				+ "</div>"
				+ "</div>";
	}

	// called when indexServlet submits the form with the input text:
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) {

		// session started on indexServlet (main) page:
		HttpSession session = request.getSession(false);

		String inputText = request.getParameter("inputText");
		inputText = inputText.replaceAll("\\n", "<br>");
		inputText = inputText.replaceAll("\\s+", " ");
		session.setAttribute("inputText", inputText);
		
		String question_text = (String) session.getAttribute("question_text");
		
		String session_id = Long.toString(System.currentTimeMillis());
		String text_id = session_id; // for now just use same unique ID for text
		session.setAttribute("text_id", text_id); 

		String response_str = "<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\""
				+ "\"http://www.w3.org/TR/html4/loose.dtd\">"
				+ "<html lang=\"en\">"
				+ "<head>"
				+ "<meta charset=\"utf-8\">"
				+ "<meta http-equiv=\"X-UA-Compatible\" content=\"IE=edge\">"
				+ "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">"
				+ "<link rel=\"stylesheet\" href=\"https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap.min.css\">"
				+ "<link rel=\"stylesheet\" type=\"text/css\" "
				+ "href=\"" // + request.getContextPath() // if running in tomcat
				+ "/style.css\" />"   
				+ "<title>ELiT API example</title>"
				+ "</head>"
				+ "<body>";

		String debug_output = "";

		String account_id = System.getenv("WI_ACCOUNT_ID");
		String account_token = System.getenv("WI_ACCOUNT_TOKEN");

		boolean static_eg = account_id == null || account_id.trim().equals("")
				|| account_token == null || account_token.trim().equals("");

		// if API credentials aren't available use the static example:
		if(static_eg) {
			
			// just show loading box and go to the GET request
			response_str += getLoadingBox();

			// will create a GET request i.e. to poll for result!
			response.addHeader("Refresh", "2");

			debug_output = "Loading static API example - no API credentials available";
			
		} else {

			JSONObject json = new JSONObject();
			json.put("author_id", "APIDemoExampleAuthor");
			json.put("task_id", "APIDemoExampleTask");
			json.put("session_id", session_id);
			json.put("question_text", question_text);
			json.put("text", inputText);
			json.put("test", 1);

			String url = api_url + "/v2.0.0/account/" + account_id + "/text/" + text_id;

			System.out.println("url=" + url);
			System.out.println("json=" + json.toString());

			String response_api = "";

			try {
				response_api = this.executeHttpRequest(json.toString()
						, url);

				System.out.println("submission response:" + response_api);

				JSONObject json_obj = new JSONObject(response_api);
				if(json_obj.getString("type").equals("success")) {
					// will switch to GET method (below) once refreshed.
					response.addHeader("Refresh", "0");
				}

				debug_output = json_obj.toString(4);

				response_str += getLoadingBox();			

			} catch (Exception e) {
				e.printStackTrace();
				response_str += "An error has occurred: " + e.getMessage();
				response_str += "<br><br>Response from server (if received): "  + response_api;
			}

		}
		
		if(print_api_response && debug_output != "") {
			response_str += getDebugBox(debug_output);		
		}

		response_str +="</body></html>";


		PrintWriter writer;
		try {
			writer = response.getWriter();
			writer.println(response_str);
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// called when page is refreshed - already submitted text to the API for processing:
	// use session to retrieve the input text and submission text id:
	public void doGet(HttpServletRequest request, HttpServletResponse response)  {

		// session started if required on indexServlet (main) page:
		HttpSession session = request.getSession(false);

		String inputText = (String) session.getAttribute("inputText");
		String question_text = (String) session.getAttribute("question_text");
		String text_id=(String)session.getAttribute("text_id");  
		String response_api = ""; 

		// if results ready to display this is updated 
		JSONObject results_json_obj = null;

		String debug_output = "";

		String response_str = "<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\""
				+ "\"http://www.w3.org/TR/html4/loose.dtd\">"
				+ "<html lang=\"en\">"
				+ "<head>"
				+ "<meta charset=\"utf-8\">"
				+ "<meta http-equiv=\"X-UA-Compatible\" content=\"IE=edge\">"
				+ "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">"
				+ "<link rel=\"stylesheet\" href=\"https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap.min.css\">"
				+ "<link rel=\"stylesheet\" type=\"text/css\" "
				+ "href=\"" // + request.getContextPath() if running in tomcat
				+ "/style.css\" />"   
				+ "<title>ELiT API example</title>"
				+ "</head>"
				+ "<body>";

		String account_id = System.getenv("WI_ACCOUNT_ID");
		String account_token = System.getenv("WI_ACCOUNT_TOKEN");

		boolean static_eg = account_id == null || account_id.trim().equals("")
				|| account_token == null || account_token.trim().equals("");

		// if no API credentials are available we use a static example:
		if(static_eg) {
			try {
				// get the s3 JSON file results for the static text example:
				results_json_obj = new JSONObject(IOUtils.toString(
						new URL("https://s3-eu-west-1.amazonaws.com/elit-website-media/results-example-api.json"), 
						Charset.forName("UTF-8")));
				
			} catch (JSONException | IOException e) {
				e.printStackTrace();
				debug_output = "An Error has occurred in the static example: " + e.getMessage();
			}		
		} else {
			try {

				String url = api_url + "/v2.0.0/account/" + account_id + "/text/" + text_id + "/results";

				response_api = this.executeHttpRequest(null, url);

				System.out.println("submission response:" + response_api);

				JSONObject json_obj = new JSONObject(response_api);
				
				String status_type = json_obj.getString("type");

				if(status_type.equals("results_not_ready")) {
					// reload page again in the suggested timeframe:
					// estimated seconds to complete may reduce to 0 before completion if we have 
					// underestimated the time to complete the task therefore, make sure we wait at
					// least 1 second before refreshing:
					double reLoadTime = json_obj.getDouble("estimated_seconds_to_completion");
					reLoadTime = Math.max(reLoadTime, 1.0);
					response.addHeader("Refresh", "" + reLoadTime);

					// loading page:
					response_str += getLoadingBox();	
				} else if(status_type.equals("success")) {	
					// results ready:
					results_json_obj = json_obj;	
				}  else {
					response_str += "Unknown status_type:" + status_type;
				}
			} catch (Exception e) {
				e.printStackTrace();
				response_str += "An error has occurred:" + e.getMessage();
			}
		}

		if(results_json_obj != null) {
			
			debug_output = results_json_obj.toString(4);

			// map from character positions to html tags:
			HashMap<Integer, String> tags = new HashMap<Integer, String>();

			JSONArray sentence_scores = results_json_obj.getJSONArray("sentence_scores");
			JSONArray suspect_tokens = results_json_obj.getJSONArray("suspect_tokens");
			JSONArray textual_errors = results_json_obj.getJSONArray("textual_errors");

			JSONArray sent_score, sus_token, text_error;
			Double score;
			int start, end;
			String sentence_color;

			for(int i=0; i < sentence_scores.length(); i++) {
				sent_score = sentence_scores.getJSONArray(i);
				score = sent_score.getDouble(2);
				start = sent_score.getInt(0);
				end = sent_score.getInt(1);

				if(score < sentence_threshold_high && score > sentence_threshold_low){
					sentence_color = color_sentence_med;
				}
				else if(score < sentence_threshold_low){
					sentence_color = color_sentence_low;
				}
				else{
					sentence_color = color_sentence_high;
				}
				tags.put(start, "<span style=\"background-color:" + sentence_color +
						"\" data-sentence-score=\"" + score + "\">");
				tags.put(end, "</span>");
			}

			for(int i=0; i < suspect_tokens.length(); i++) {
				sus_token = suspect_tokens.getJSONArray(i);
				start = sus_token.getInt(0);
				end = sus_token.getInt(1);
				tags.put(start, "<span style=\"border:2px solid " + color_token_suspect + ";\">");
				tags.put(end, "</span>");
			}

			for(int i=0; i < textual_errors.length(); i++) {
				text_error = textual_errors.getJSONArray(i);
				start = text_error.getInt(0);
				end = text_error.getInt(1);
				tags.put(start, "<span style=\"border:2px solid " + color_token_error + ";\">");
				tags.put(end, "</span>");
			}

			String processed_text = "";

			for(int i=0; i <= inputText.length(); i++){
				if(tags.containsKey(i)){
					processed_text += tags.get(i);
				}
				if(i < inputText.length())
					processed_text += inputText.charAt(i);
			}

			// display the HTML:
			response_str +=  "<div class=\"container\" id=\"page-output\">"
					+ "<div id=\"output\">"
					+ "<h3>" + question_text + "</h3>"
					+ "<div class=\"overall_score\">"
					+ "<strong>Overall score:</strong> " + results_json_obj.getDouble("overall_score") +"</div>"
					+ "<div id=\"analysis\" style=\"line-height:160%;\">" + processed_text
					+ "</div></div>"
					+ "<form action=\"" + "/" // + request.getContextPath() if running in tomcat - use instead of "/"
					+ "\">"
					+ "<button type=\"submit\" class=\"btn btn-default\" id=\"submit\">Try again</button>"
					+ "</form>"
					+ "</div>";
		}

		if(print_api_response && debug_output != "") {
			response_str += getDebugBox(debug_output);		
		}
		response_str +="</body></html>";
		
		PrintWriter writer;
		try {
			writer = response.getWriter();
			writer.println(response_str);	
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

	private String executeHttpRequest(String postData, String url) throws Exception {
		CloseableHttpClient httpClient = HttpClientBuilder.create().build();
		HttpResponse httpResponse = null;

		try {
			HttpRequestBase request;
			if (postData == null) {
				request = new HttpGet(url);
			} else {
				// post any data that needs to go with http request.
				request = new HttpPut(url);
				((HttpPut) request).setEntity(new StringEntity(postData, "UTF-8"));
			}

			// Set http headers
			request.addHeader("Accept", "*/*");
			request.addHeader("Accept-Charset", "UTF-8");
			request.addHeader("Content-Type", "application/json");

			// Add apiKey to the http header
			request.addHeader("Authorization", "Token token=" + account_token );

			// execute http request
			httpResponse = httpClient.execute(request);

			System.out.println("http response = " + httpResponse.toString());

			if (httpResponse.getStatusLine().getStatusCode() != 200) {
				System.err.println(httpResponse);
				throw new RuntimeException(httpResponse.getStatusLine().getReasonPhrase());
			}
			// return JSON results as String
			ResponseHandler<String> responseHandler = new BasicResponseHandler();
			String responseBody = responseHandler.handleResponse(httpResponse);
			return responseBody;

		}
		catch (Exception e) {
			e.printStackTrace();
			//throw new RuntimeException("unable to execute json call:" + e);
			return e.getMessage();
		} finally {
			// close http connection
			if (httpResponse != null) {
				HttpEntity entity = httpResponse.getEntity();
				if (entity != null) {
					EntityUtils.consume(entity);
				}
			}
			if (httpClient != null) {
				httpClient.getConnectionManager().shutdown();
			}
		}
	}

}