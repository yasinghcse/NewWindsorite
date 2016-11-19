package wasdev.sample.servlet;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.ibm.watson.developer_cloud.conversation.v1.ConversationService;
import com.ibm.watson.developer_cloud.conversation.v1.model.MessageRequest;
import com.ibm.watson.developer_cloud.conversation.v1.model.MessageResponse;


/**
 * Servlet implementation class WatsonInteraction
 */
@WebServlet("/WatsonInteraction")
public class WatsonInteraction extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static ConversationService service=null;
    private static MessageResponse lastConversation=null;

    public WatsonInteraction() {
        super();
    }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		MessageRequest newMessage = null;
		MessageResponse response1=null;
		if (request.getParameter("new") != null) {
			service = new ConversationService(ConversationService.VERSION_DATE_2016_07_11);
			service.setUsernameAndPassword("fd8dd41f-2f5f-4bbe-b42e-b519e85f39a4", "jxSvoafD6MYc");
			newMessage = new MessageRequest.Builder().inputText("").build();
			response1 = service.message("cfb066a8-7b85-4e21-8180-7f5f25bd0265", newMessage).execute();
			System.out.println(response1.getText().get(0));
			lastConversation =  response1;
		} 
		else {
			System.out.println("last Contct" + lastConversation.getContext());
			System.out.println("input " + request.getParameter("question"));
			MessageRequest newMessage1 = new MessageRequest.Builder().inputText(request.getParameter("question")).context(lastConversation.getContext())
					.build();
			response1 = service.message("cfb066a8-7b85-4e21-8180-7f5f25bd0265", newMessage1).execute();
			lastConversation =  response1;
			System.out.println(response1.getText());
		}
		for (int i = 0; i < response1.getText().size(); i++) {
			response.getWriter().print(response1.getText().get(i) + "\n");
		}
		
	}	

}
