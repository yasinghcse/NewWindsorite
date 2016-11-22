package wasdev.windsor.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.json.simple.JSONObject;

import newwindsorite.db.UserProfileAlreadyExistentException;
import newwindsorite.db.UserProfileNotFoundException;
import wasdev.windsor.facade.NewWindsoriteFacade;
import wasdev.windsor.resources.UserProfile;
import wasdev.windsor.resources.WindsorOpenDataApi;
import wasdev.windsor.resources.WindsoriteUtil;

/**
 * Servlet Controller for interaction between Web Layer and System Layer
 * 
 */
@WebServlet("/Controller")
public class WindsoriteServletController extends HttpServlet {

	private static final long serialVersionUID = 1L;

	// Reference to the System Facade
	private NewWindsoriteFacade windsoriteFacade = null;

	/**
	 * simply creates or loads some data that will be used throughout the life
	 * of the servlet
	 * 
	 */
	public void init() throws ServletException {
		windsoriteFacade = NewWindsoriteFacade.getFacade();
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 * 
	 * 
	 */
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		response.setContentType("text/html");
		try {
			// response.getWriter().print("Hit the Controller Servlet<BR><BR>");
			String controllerAction = request.getParameter("act");
			System.out.println("contoller action ==" + controllerAction);
			if (controllerAction != null) {
				if (controllerAction.equals("updtProf")) {
					UserProfile userProfile = new UserProfile(request.getParameter("userName"));
					System.out.println("usrename in controller" + userProfile.getUserName());
					// commenting the code so that the calling function set all
					// new user values
					userProfile.setEmail(request.getParameter("email"));
					userProfile.setName(request.getParameter("name"));
					userProfile.setAddress(request.getParameter("address"));
					userProfile.setZipCOde(request.getParameter("zipcode"));

					updateUserProfile(request, response, userProfile);
				} else if (controllerAction.equals("updtRec")) {

					// get the user info from session
					HttpSession s = request.getSession(false);
					UserProfile userProfile = (UserProfile) s.getAttribute("UserProfile");
					System.out.println("username in controller ****" + userProfile.getUserName());
					System.out.println("userProfile1.email" + userProfile.getEmail());
					System.out.println("user email we got = " + userProfile.getEmail());
					System.out.println("p1 in session = " + userProfile.getRecomentation1());
					System.out.println("p2 in the session = " + userProfile.getRecomentation2());
					System.out.println("p3 in the session = " + userProfile.getRecomentation3());

					// calling the update function
					updateUserProfile(request, response, userProfile);
				} else if (controllerAction.equals("getRecommendations")) {

					// get the user info from session
					HttpSession s = request.getSession(false);

					// local test code
					UserProfile userProfile = new UserProfile("test2");
					userProfile.setRecomentation1("TrafficCharacter");
					userProfile.setRecomentation2("forgot things");
					userProfile.setRecomentation3("SocialCharacter");

					// UserProfile userProfile =(UserProfile)
					// s.getAttribute("UserProfile");
					System.out.println("username in controller ****" + userProfile.getUserName());

					// calling the OpenData API to get all the required
					WindsorOpenDataApi obj = new WindsorOpenDataApi();
					response.getWriter().print(obj.getAllRecommendations(userProfile));

				} else if (controllerAction.equals("getRecommendationsProfile")) {

					// get the user info from session
					HttpSession s = request.getSession(false);

					// local test code
					UserProfile userProfile = new UserProfile("test2");

					// UserProfile userProfile =(UserProfile)s.getAttribute("UserProfile");
					System.out.println("username in controller ****" + userProfile.getUserName());

					// calling the OpenData API to get all the required
					response.getWriter().print(getUserProfile(request, response, userProfile.getUserName()));

				} else if (controllerAction.equals("insProf")) {
					UserProfile userProfile = new UserProfile(request.getParameter("userName"));
					userProfile.setEmail(request.getParameter("email"));
					userProfile.setName(request.getParameter("name"));
					userProfile.setAddress(request.getParameter("address"));
					userProfile.setZipCOde(request.getParameter("zipcode"));
					String hashedPassword = WindsoriteUtil.generateStringHash(request.getParameter("password"));
					userProfile.setPasswordHash(hashedPassword);
					insertUserProfile(request, response, userProfile);
				} else if (controllerAction.equals("delProf")) {
					removeUserProfile(request, response, request.getParameter("userName"));
				} else if (controllerAction.equals("finProf")) {
					searchUserProfile(request, response, request.getParameter("userName"));
				} else if (controllerAction.equals("login")) {
					System.out.println("******************");
					System.out.println("username " + request.getParameter("userName"));
					System.out.println("password " + request.getParameter("password"));
					systemLogin(request, response, request.getParameter("userName"), request.getParameter("password"));
				} else if (controllerAction.equals("logout")) {
					systemLogout(request, response);
				} else if (controllerAction.equals("valSession")) {
					// yadi changes
					if (isSessionValid(request)) {
						response.getWriter().print("Valid User.");
					} else {
						response.getWriter().print("Invalid User");
					}
				} else {
					response.getWriter().print("ACT tag not not valid<BR><BR>");
					response.getWriter().print("ACT =  updtProf to UPDATE PROFILE<BR>");
					response.getWriter().print("ACT = insProf to INSERT PROFILE<BR>");
					response.getWriter().print("ACT = delProf to REMOVE PROFILE<BR>");
					response.getWriter().print("ACT = finProf to FIND PROFILE<BR>");
				}
			} else {
				response.getWriter().print("ACT tag not found<BR><BR>");
			}
		} catch (Exception ex) {
			response.flushBuffer();
			ex.printStackTrace(response.getWriter());
		}
	}

	/**
	 * 
	 * 
	 * @param request
	 * @param response
	 * @throws IOException
	 */
	private void systemLogout(HttpServletRequest request, HttpServletResponse response) throws IOException {
		HttpSession session = request.getSession(false);
		if (session != null) {
			session.invalidate();
			// response.sendRedirect("");
			response.getWriter().print("User LOGGED OUT!<BR>");
		} else {
			response.getWriter().print("Login first User LOGGED OUT!<BR>");
		}
	}

	/**
	 * 
	 * 
	 * @param request
	 * @param response
	 * @param parameter
	 * @param parameter2
	 * @return
	 * @throws IOException
	 */
	private void systemLogin(HttpServletRequest request, HttpServletResponse response, String userName,
			String passwordPlain) throws IOException {
		try {
			UserProfile user = windsoriteFacade.systemLogin(userName, passwordPlain);
			if (user != null) {
				HttpSession session = request.getSession(true);
				session.setAttribute("UserProfile", user);
				response.getWriter().print("User LOGGED IN!<BR>");
			} else {
				System.out.println("Password incorrect");
				response.getWriter()
						.print("User Profile by User Name '" + userName + "': INVALID PASSWORD, cannot LOG IN!<BR>");
			}
		} catch (UserProfileNotFoundException ex) {
			response.getWriter().print("User Profile by User Name '" + userName + "' NOT FOUND, cannot LOG IN!<BR>");
		}
	}

	/**
	 * 
	 * 
	 * @param request
	 * @param response
	 * @param userName
	 * @throws IOException
	 */
	private void searchUserProfile(HttpServletRequest request, HttpServletResponse response, String userName)
			throws IOException {
		try {
			response.getWriter().print("Search Result: <BR>");
			UserProfile user = windsoriteFacade.searchUserProfileByUserName(userName);
			response.getWriter().print("User Profile by User Name '" + userName + "' FOUND!<BR><BR>");
			response.getWriter().print("id = " + user.get_id() + "<BR>");
			response.getWriter().print("UserName = " + user.getUserName() + "<BR>");
			response.getWriter().print("Name = " + user.getName() + "<BR>");
			response.getWriter().print("Email = " + user.getEmail() + "<BR>");
			response.getWriter().print("Address = " + user.getAddress() + "<BR>");
			response.getWriter().print("ZipCode = " + user.getZipCOde() + "<BR>");
			response.getWriter().print("Recomendation 1 = " + user.getRecomentation1() + "<BR>");
			response.getWriter().print("Recomendation 2 = " + user.getRecomentation2() + "<BR>");
			response.getWriter().print("Recomendation 3 = " + user.getRecomentation3() + "<BR>");
		} catch (UserProfileNotFoundException ex) {
			response.getWriter().print("User Profile by User Name '" + userName + "' NOT FOUND!<BR>");
		}
	}

	// yadi
	private JSONObject getUserProfile(HttpServletRequest request, HttpServletResponse response, String userName)
			throws IOException {
		
		UserProfile user = null;
		// local variable
		JSONObject obj = new JSONObject();
		try {
			user = windsoriteFacade.searchUserProfileByUserName(userName);
			if(user.getName() != null){
				obj.put("name", user.getName());
			}
			if(user.getEmail() != null){
				obj.put("email", user.getEmail());
			}
			if(user.getAddress() != null){
				obj.put("address", user.getAddress());
			}
			if(user.getZipCOde() != null){
				obj.put("zipcode", user.getZipCOde());
			}
		} catch (UserProfileNotFoundException ex) {
			response.getWriter().print("User Profile by User Name '" + userName + "' NOT FOUND!<BR>");
		}
		return obj;
	}

	/**
	 * 
	 * @param request
	 * @param response
	 * @param userName
	 * @throws IOException
	 */
	private void removeUserProfile(HttpServletRequest request, HttpServletResponse response, String userName)
			throws IOException {
		try {
			response.getWriter().print("Removal Result: <BR>");
			windsoriteFacade.removeUserProfile(userName);
			response.getWriter().print("User Profile by User Name '" + userName + "' REMOVED!<BR>");
		} catch (UserProfileNotFoundException ex) {
			response.getWriter().print("User Profile by User Name '" + userName + "' NOT FOUND!<BR>");
		}
	}

	/**
	 * 
	 * @param request
	 * @param response
	 * @param userProfile
	 * @throws IOException
	 */
	private void insertUserProfile(HttpServletRequest request, HttpServletResponse response, UserProfile user)
			throws IOException {
		try {
			response.getWriter().print("Insertion Result: <BR>");
			windsoriteFacade.insertUserProfile(user);
			response.getWriter().print("User Profile by User Name '" + user.getUserName() + "' INSERTED!<BR><BR>");
			response.getWriter().print("id = " + user.get_id() + "<BR>");
			response.getWriter().print("UserName = " + user.getUserName() + "<BR>");
			response.getWriter().print("Name = " + user.getName() + "<BR>");
			response.getWriter().print("Email = " + user.getEmail() + "<BR>");
			response.getWriter().print("Address = " + user.getAddress() + "<BR>");
			response.getWriter().print("ZipCode = " + user.getZipCOde() + "<BR>");
			response.getWriter().print("Recomendation 1 = " + user.getRecomentation1() + "<BR>");
			response.getWriter().print("Recomendation 2 = " + user.getRecomentation2() + "<BR>");
			response.getWriter().print("Recomendation 3 = " + user.getRecomentation3() + "<BR>");
		} catch (UserProfileAlreadyExistentException ex) {
			response.getWriter().print("User Profile by User Name '" + user.getUserName() + "' ALREADY EXISTS!<BR>");
		}
	}

	/**
	 * 
	 * @param request
	 * @param response
	 * @param user
	 * @throws IOException
	 */
	private void updateUserProfile(HttpServletRequest request, HttpServletResponse response, UserProfile user)
			throws IOException {
		if (isSessionValid(request)) {
			try {
				// yadi changes
				// commenting debugs so that they are not visible on watson chat

				// response.getWriter().print("Update Result: <BR>");
				System.out.println("Inside Update Profile");
				windsoriteFacade.updateUserProfile(user);
				// response.getWriter().print("User Profile by User Name '" +
				// user.getUserName() + "' UPDATED!<BR><BR>");
				// response.getWriter().print("id = " + user.get_id() + "<BR>");
				// response.getWriter().print("UserName = " + user.getUserName()
				// + "<BR>");
				// response.getWriter().print("Name = " + user.getName() +
				// "<BR>");
				// response.getWriter().print("Email = " + user.getEmail() +
				// "<BR>");
				// response.getWriter().print("Address = " + user.getAddress() +
				// "<BR>");
				// response.getWriter().print("ZipCode = " + user.getZipCOde() +
				// "<BR>");
				// response.getWriter().print("Recomendation 1 = " +
				// user.getRecomentation1() + "<BR>");
				// response.getWriter().print("Recomendation 2 = " +
				// user.getRecomentation2() + "<BR>");
				// response.getWriter().print("Recomendation 3 = " +
				// user.getRecomentation3() + "<BR>");
			} catch (UserProfileNotFoundException ex) {
				response.getWriter().print("User Profile by User Name '" + user.getUserName() + "' NOT FOUND!<BR>");
			}
		} else {
			response.getWriter().print(
					"User Profile by User Name '" + user.getUserName() + "' LOGIN IN FIRST to Update Profile!<BR>");
		}
	}

	/**
	 * 
	 * 
	 * @param request
	 * @return
	 */
	private boolean isSessionValid(HttpServletRequest request) {
		boolean isValid = false;
		if (request.getSession() != null) {
			isValid = true;
		}
		return isValid;
	}
}
