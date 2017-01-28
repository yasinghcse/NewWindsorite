/**
` * This class hold the connection logic to watson
 * All Watson communication passes through this servlet
 * 
 * @author yadwindersingh
 */

package wasdev.windsor.servlet;

import java.io.IOException;
import java.util.ArrayList;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.json.simple.JSONArray;

import com.ibm.watson.developer_cloud.conversation.v1.ConversationService;
import com.ibm.watson.developer_cloud.conversation.v1.model.MessageRequest;
import com.ibm.watson.developer_cloud.conversation.v1.model.MessageResponse;


/**
 * Servlet implementation class WatsonInteraction
 */
@WebServlet("/WatsonInteraction")
public class WatsonInteraction extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static ConversationService service = null;
	private static MessageResponse lastConversation = null;


	public WatsonInteraction() {
		super();
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		MessageRequest newMessage = null;
		MessageResponse response1 = null;
		if (request.getParameter("new") != null) {

			// dummy function to create the login connection
			// request.setAttribute("userName", "test1");
			// request.setAttribute("password", "test");
			// RequestDispatcher rd = request
			// .getRequestDispatcher("/Controller?act=login&userName=test2&password=test");
			// rd.include(request, response);

			// connecting to the Watson API
			service = new ConversationService(ConversationService.VERSION_DATE_2016_07_11);
			service.setUsernameAndPassword("fd8dd41f-2f5f-4bbe-b42e-b519e85f39a4", "jxSvoafD6MYc");
			newMessage = new MessageRequest.Builder().inputText("").build();
			response1 = service.message("f11c5611-73e2-40b2-92e2-dec31008d783", newMessage).execute();
			System.out.println(response1.getText().get(0));
			lastConversation = response1;
			for (int i = 0; i < response1.getText().size(); i++) {
				response.getWriter().print(response1.getText().get(i) + "<br>");
				System.out.println("***********" + response1.getText().get(i) + "****************");
			}

		} else {
			System.out.println("last Contct" + lastConversation.getContext());
			System.out.println("input " + request.getParameter("question"));
			MessageRequest newMessage1 = new MessageRequest.Builder().inputText(request.getParameter("question"))
					.context(lastConversation.getContext()).build();
			response1 = service.message("f11c5611-73e2-40b2-92e2-dec31008d783", newMessage1).execute();
			lastConversation = response1;
			System.out.println(response1.getText());
			ArrayList<String> test = (ArrayList<String>) response1.getText();
			System.out.println("test" + test.get(0));

			// preparing the response back to the user
			for (int i = 0; i < response1.getText().size(); i++) {
				response.getWriter().print(response1.getText().get(i) + "<br>");
				System.out.println("***********" + response1.getText().get(i) + "****************");
			}

		}

	}

}
