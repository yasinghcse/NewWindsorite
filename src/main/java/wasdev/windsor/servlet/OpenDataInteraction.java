package wasdev.windsor.servlet;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import wasdev.windsor.resources.WindsorOpenDataApi;

/**
 * Servlet implementation class OpenDataInteraction
 */
@WebServlet("/OpenDataInteraction")
public class OpenDataInteraction extends HttpServlet {
	//private variables
	private static WindsorOpenDataApi obj=  new WindsorOpenDataApi();
	
	private static final long serialVersionUID = 1L;

    public OpenDataInteraction() {
        super();
    }


	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		System.out.println("input" + request.getParameter("call"));
		if (request.getParameter("call") == null) {
			System.out.println("insufficient parameters");
			response.getWriter().print("insufficient parameters");
		}
		else if (request.getParameter("call") == "garbage") {
			System.out.println("Calling the Wastage Collection API");
			response.getWriter().print(obj.getGarabageCollDetails());
		}
		else if (request.getParameter("call") == "events") {
			System.out.println("Calling the Event Info API");
			response.getWriter().print(obj.getEventDetails());
		}
		else if (request.getParameter("call") == "construction") {
			System.out.println("Calling the Construction Info API");
			response.getWriter().print(obj.getConstructionDetails());
		}
		else{
			System.out.println("invalid paramter");
			response.getWriter().print("invalid paramter");
		}	
	}
}
