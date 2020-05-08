 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package servlets.actions.get.lifestyle.location;
 
 import health.database.DAO.UserDAO;
 import health.database.models.Users;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import server.exception.ReturnParser;
 import util.AllConstants;
 import util.DateUtil;
 
 import com.google.gson.Gson;
 import com.google.gson.JsonElement;
 import com.google.gson.JsonObject;
import com.lifestyle.DAO.LocationsDAO;
 import com.lifestyle.models.Locationlogs;
 import com.lifestyle.output.models.JsonLocationLog;
 
 /**
  * 
  * @author Leon
  */
 public class GetLocations extends HttpServlet {
 
 	/**
 	 * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
 	 * methods.
 	 * 
 	 * @param request
 	 *            servlet request
 	 * @param response
 	 *            servlet response
 	 * @throws ServletException
 	 *             if a servlet-specific error occurs
 	 * @throws IOException
 	 *             if an I/O error occurs
 	 */
 	public void processRequest(HttpServletRequest request,
 			HttpServletResponse response) throws ServletException, IOException {
 		response.setContentType("application/json");
 		response.setCharacterEncoding("UTF-8");
 		request.setCharacterEncoding("UTF-8");
 
 		Users accessUser = null;
 		UserDAO userDao = new UserDAO();
 				accessUser = userDao.getLogin("leoncool");
 	
 		PrintWriter out = response.getWriter();
 
 		try {		
 		
 			  DateUtil dateUtil = new DateUtil();
	            LocationsDAO locDao = new LocationsDAO();
 	            List<Locationlogs> loclogList = locDao.getLocatioonLogs("leoncool", null, null);
 	            List<JsonLocationLog> jsonLogList = new ArrayList<JsonLocationLog>();
 	            for (Locationlogs log : loclogList) {
 	                JsonLocationLog jlog = new JsonLocationLog();
 	                jlog.setTime(dateUtil.format(log.getDatetime(), dateUtil.utcFormat));
 	                jlog.setLat(Double.toString(log.getLat()));
 	                jlog.setLon(Double.toString(log.getLon()));
 	                jlog.setTime_long(Long.toString(log.getDatetime().getTime()));
 	                jlog.setTag("null");
 	                jsonLogList.add(jlog);
 	            }
 	            Gson gson = new Gson();
 	       
 			JsonElement je = gson.toJsonTree(jsonLogList);
 			JsonObject jo = new JsonObject();
 			jo.addProperty(AllConstants.ProgramConts.result,
 					AllConstants.ProgramConts.succeed);
 			jo.add("locationlogs", je);
 			System.out.println(jo.toString());
 			out.println(gson.toJson(jo));
 		} catch (Exception ex) {
 			ex.printStackTrace();
 			ReturnParser.outputErrorException(response,
 					AllConstants.ErrorDictionary.Internal_Fault,
 					null, null);
 			return;
 		} finally {
 			out.close();
 		}
 	}
 
 	// <editor-fold defaultstate="collapsed"
 	// desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
 
 	/**
 	 * Handles the HTTP <code>GET</code> method.
 	 * 
 	 * @param request
 	 *            servlet request
 	 * @param response
 	 *            servlet response
 	 * @throws ServletException
 	 *             if a servlet-specific error occurs
 	 * @throws IOException
 	 *             if an I/O error occurs
 	 */
 	@Override
 	protected void doGet(HttpServletRequest request,
 			HttpServletResponse response) throws ServletException, IOException {
 		processRequest(request, response);
 	}
 
 	/**
 	 * Handles the HTTP <code>POST</code> method.
 	 * 
 	 * @param request
 	 *            servlet request
 	 * @param response
 	 *            servlet response
 	 * @throws ServletException
 	 *             if a servlet-specific error occurs
 	 * @throws IOException
 	 *             if an I/O error occurs
 	 */
 	@Override
 	protected void doPost(HttpServletRequest request,
 			HttpServletResponse response) throws ServletException, IOException {
 		processRequest(request, response);
 	}
 
 	/**
 	 * Returns a short description of the servlet.
 	 * 
 	 * @return a String containing servlet description
 	 */
 	@Override
 	public String getServletInfo() {
 		return "Short description";
 	}// </editor-fold>
 }
