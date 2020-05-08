 package Controller;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.sql.SQLException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 import Beans.Race;
 import Beans.RaceObj;
 import Exceptions.SelectException;
 
 /******************************************************
  * Controller class that deals with handling the * pre game lobby where the use
  * can see the races he * is in. *
  ******************************************************/
 public class PreRaceController extends HttpServlet {
 	private static final long serialVersionUID = 1L;
 	private Race raceModel;
 
 	/**
 	 * Constuctor that sets the race model that will be used to get the races.
 	 */
 	public PreRaceController() {
 		super();
 
 		try {
 			raceModel = new Race();
 		} catch (ClassNotFoundException e) {
 			e.printStackTrace();
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * This is the do get that will build a json object that contains all of the
 	 * user's races that are coming up or currently active.
 	 * 
 	 * @param request
 	 *            Contains the session.
 	 * @param response
 	 *            Json object will be sent back through the response.
 	 * @throws IOException
 	 * @throws ServletException
 	 */
 	protected void doGet(HttpServletRequest request,
 			HttpServletResponse response) throws ServletException, IOException {
 		response.setContentType("text/json");
 		PrintWriter out = response.getWriter();
 
 		// Gets user id using the session.
 		HttpSession session = request.getSession();
 		int userID = (Integer) session.getAttribute("userid");
 
 		try {
 			// Adds list of races to JSON
 			String json = getJSONRaceList(userID);
 
 			System.out.println(json);
 			out.println(json);
 		} catch (SelectException e) {
 			response.sendError(HttpServletResponse.SC_CONFLICT,
 					"Problem occured with the SQL. Reason: " + e.getMessage());
 		} finally {
 			out.close();
 		}
 	}
 
 	/**
 	 * Will just pass request and response on to doGet.
 	 * 
 	 * @param request
 	 *            The request which contains the JSON
 	 * @param response
 	 *            The response that will be passed back.
 	 * @throws ServletException
 	 * @throws IOException
 	 */
 	protected void doPost(HttpServletRequest request,
 			HttpServletResponse response) throws ServletException, IOException {
 		doGet(request, response);
 	}
 
 	/**
 	 * This function does the work of setting up the JSON object from the
 	 * request to the model about the races for a user.
 	 * 
 	 * @param userID
 	 *            The user that the races will be for.
 	 * @return The list of races for the user.
 	 * @throws SelectException
 	 * @throws SQLException
 	 */
 	private String getJSONRaceList(int userID) throws SelectException {
 		// Gets the list of races for the user.
		List<RaceObj> races;
 
 		// Begins creating the json object.
 		StringBuffer json = new StringBuffer("{races:[");
 
 		races = raceModel.getRacesObj(userID, false);
 		Iterator<RaceObj> iterator = races.iterator();
 
 		// All dates must be formatted like this in both view and controller.
 		final SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd hh:mm");
 
 		RaceObj temp;
 
 		// Loops through the list of races and sets the JSON object.
 		while (iterator.hasNext()) {
 			temp = iterator.next();
 
 			json.append("{raceID:\"" + temp.getId() + "\", creatorID:\""
 					+ temp.getCreatorId() + "\", name:\"" + temp.getName()
 					+ "\", time:\""
 					+ sdf.format((java.util.Date) temp.getStart())
 					+ "\", score:\"" + temp.getScore() + "\"},");
 		}
 
 		// Sets the ending characters of the JSON object.
 		if (races.isEmpty()) {
 			json.append("]");
 		} else {
 			// replace last character with ]
 			json.setCharAt(json.length() - 1, ']');
 		}
 
 		json.append("}");
 
 		return json.toString();
 	}
 }
