/**
 * This class hold the connection logic to watson
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

import wasdev.windsor.resources.UserProfile;
import wasdev.windsor.resources.WindsorOpenDataApi;

/**
 * Servlet implementation class WatsonInteraction
 */
@WebServlet("/WatsonInteraction")
public class WatsonInteraction extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static ConversationService service = null;
	private static MessageResponse lastConversation = null;
	String[] arr = null;
	JSONArray arr1 = null;

	public WatsonInteraction() {
		super();
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		// Get the session user
		HttpSession localSession = request.getSession();
		if (localSession != null) {
			MessageRequest newMessage = null;
			MessageResponse response1 = null;
			if (request.getParameter("new") != null) {
				service = new ConversationService(ConversationService.VERSION_DATE_2016_07_11);
				service.setUsernameAndPassword("fd8dd41f-2f5f-4bbe-b42e-b519e85f39a4", "jxSvoafD6MYc");
				newMessage = new MessageRequest.Builder().inputText("").build();
				response1 = service.message("cfb066a8-7b85-4e21-8180-7f5f25bd0265", newMessage).execute();
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
				response1 = service.message("cfb066a8-7b85-4e21-8180-7f5f25bd0265", newMessage1).execute();
				lastConversation = response1;
				System.out.println(response1.getText());
				ArrayList<String> test = (ArrayList<String>) response1.getText();
				System.out.println("test" + test.get(0));

				if (test.get(0).equals("Let me get it for you.")) {
					String search = response1.getContext().get("search").toString();
					System.out.println(search);
					WindsorOpenDataApi obj = new WindsorOpenDataApi();
					if (search.equals("garbage")) {
						response.getWriter().print("Below are my finding:." + "<br>");
						response.getWriter().print("*********************" + "<br>");
						arr = obj.getGarabageCollDetails();
						// arr1 = new JSONArray();
						for (int i = 0; i < arr.length; i++) {
							response.getWriter().print(i + 1 + ". " + arr[i] + "<br>");
						}
						response.getWriter().print("*********************" + "<br>");
						// System.out.println("======" + arr1);
						response.getWriter().print(response1.getText().get(1));
					} else if (search.equals("events")) {
						response.getWriter().print("Below are my finding:." + "<br>");
						response.getWriter().print("*********************" + "<br>");
						arr = obj.getEventDetails();
						// arr1 = new JSONArray();
						for (int i = 0; i < arr.length; i++) {
							response.getWriter().print(i + 1 + ". " + arr[i] + "<br>");
						}
						response.getWriter().print("*********************" + "<br>");
						// System.out.println("======" + arr1);
						response.getWriter().print(response1.getText().get(1));
					} else if (search.equals("construction")) {
						response.getWriter().print("Below are my finding:." + "<br>");
						response.getWriter().print("*********************" + "<br>");
						arr = obj.getConstructionDetails();
						// arr1 = new JSONArray();
						for (int i = 0; i < arr.length; i++) {
							response.getWriter().print(i + 1 + ". " + arr[i] + "<br>");
						}
						response.getWriter().print("*********************" + "<br>");
						// System.out.println("======" + arr1);
						response.getWriter().print(response1.getText().get(1));
					} else {
						System.out.println("invalid context");
					}
				} else {
					if (test.get(0).equals("Have a good day. Your recommendations has been overwritten.")) {
						// test values
						// UserProfile u1= new
						// UserProfile(request.getParameter("userName"));
						UserProfile u1 = new UserProfile("test");
						u1.setEmail("test@gmail.com");
						u1.setName("Clovis");
						if (response1.getContext().get("p1") != null) {
							u1.setRecomentation1(response1.getContext().get("p1").toString());
							if (response1.getContext().get("p2")!= null) {
								u1.setRecomentation2(response1.getContext().get("p2").toString());
								if (response1.getContext().get("p3") != null) {
									u1.setRecomentation3(response1.getContext().get("p3").toString());
								}
							}
						}
						System.out.println("*****"+u1.getRecomentation1());
						UserProfile userProfile = (UserProfile) localSession.getAttribute("userName");
						System.out.println("u" + userProfile);
						request.setAttribute("userName", "test1");
						request.setAttribute("password", "test");
						System.out.println(request.getAttribute("userName"));
						RequestDispatcher rd = request.getRequestDispatcher("/Controller?act=login");
						rd.include(request, response);
					}
					for (int i = 0; i < response1.getText().size(); i++) {
						response.getWriter().print(response1.getText().get(i) + "<br>");
						System.out.println("***********" + response1.getText().get(i) + "****************");
					}
				}
			}
		} else {
			response.getWriter().print("Please login to see chat with me !!" + "<br>");
		}
	}

}
