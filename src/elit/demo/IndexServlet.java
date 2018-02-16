package elit.demo;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;


@WebServlet("/indexServlet")
public class IndexServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException  {

		HttpSession session = request.getSession(false);

		String inputText = "";

		if (session == null) {
			session = request.getSession();
		} else {
			inputText=(String)session.getAttribute("inputText");

			if(inputText == null) {
				inputText = "";
			} else {
				inputText = inputText.replaceAll("<br>", "\\\n");
			}
		}

		String question_text = "Write a letter to organise a surprise birthday party";
		session.setAttribute("question_text", question_text);

		PrintWriter writer = response.getWriter();

		String response_str = "<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\""
				+ "\"http://www.w3.org/TR/html4/loose.dtd\">"
				+ "<html lang=\"en\">"
				+ "<head>"
				+ "<meta charset=\"utf-8\">"
				+ "<meta http-equiv=\"X-UA-Compatible\" content=\"IE=edge\">"
				+ "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">"
				+ "<link rel=\"stylesheet\" href=\"https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap.min.css\">"
				+ "<link rel=\"stylesheet\" type=\"text/css\" "
				+ "href=\"" // + request.getContextPath() - if running in tomcat
				+ "/style.css\" />"   
				+ "<title>ELiT API example</title>"
				+ "</head>"
				+ "<body>";

		String account_id = System.getenv("WI_ACCOUNT_ID");
		String account_token = System.getenv("WI_ACCOUNT_TOKEN");

		boolean static_eg = account_id == null || account_id.trim().equals("")
				|| account_token == null || account_token.trim().equals("");

		response_str += "<div class=\"container\" id=\"page-input\">";

		if(static_eg) {
			response_str += "<p>API credentials unavailable - please set to use live API.</p>";
			response_str += "<p>Text is not editable. This example will not connect to the API "
					+ "but instead use example JSON file returned from the API. "
					+ "Please contact ELiT to apply for free trial API access.</p>";
		}

		response_str += "<h3>" + question_text + "</h3>";
		response_str += "<form method=\"post\" action=\"submitServlet\">";
		response_str += "<div class=\"form-group\">";

		if(static_eg) {
			inputText = "Dear Mrs Brown,\n\n" 
					+ "I am writing in connection with a surprise birthday party for your husband, "
					+ "Mr. Brown. We are writing to invite you and to give you some information "
					+ "about the party. All our class love Mr Brown very much, so we decided to "
					+ "organise a surprise party for him. The party in on Tuesday 16 of June. "
					+ "You should come on 3 pm in college Canteen . "
					+ "We have bought some snaks to eat and three students will sing for him, also . "
					+ "Besides this, we have invited all other teachers and the Principal of our school. "
					+ "Of course all the class will take party to this party. "
					+ "Furthermore , we don't know what present buying for him. "
					+ "So we would appreciate if you help us with this matter. "
					+ "We have thought to buy a cd or a book. He loves to read books. "
					+ "What do you believe ? If he needs something else, we are happy to buy this. "
					+ "I am looking forward to hearing from you soon especially as I am concerned "
					+ "about this matter.\n\n"
					+ "Yours sincerely,\n\n"
					+ "John Smith";

			response_str += "<textarea id=\"input_text\" class=\"form-control\" "
					+ "name=\"inputText\" readonly>" 
					+ inputText
					+ "</textarea>";

			inputText = inputText.replaceAll("\\n", "<br>");
			inputText = inputText.replaceAll("\\s+", " ");

			session.setAttribute("inputText", inputText);

		} else {
			response_str += "<textarea id=\"input_text\" class=\"form-control\" name=\"inputText\">" 
					+ inputText
					+ "</textarea>";
		}

		response_str += "</div>"
				+ "<button type=\"submit\" class=\"btn btn-default\" id=\"submit\">Submit</button>";	

		response_str += "</form></div>";

		response_str += "</body></html>";

		writer.println(response_str);
	}

}
