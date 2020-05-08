 package web;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.persistence.EntityManager;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import master.AbstractMaster;
 import model.BSScrimmageSet;
 import model.STATUS;
 import model.TEAM;
 
 import common.BSUtil;
 import common.HibernateUtil;
 
 public class ScrimmagesServlet extends HttpServlet {
 	private static final long serialVersionUID = -4741371113331532230L;
 	public static final String NAME = "/scrimmages.html";
 	
 	private void warn(HttpServletResponse response, String warning) throws IOException {
 		response.getWriter().println("<p class='ui-state-error' style='padding:10px'>" + warning + "</p>");
 	}
 	
 	private void highlight(HttpServletResponse response, String msg) throws IOException {
 		response.getWriter().println("<p class='ui-state-highlight' style='padding:10px'>" + msg + "</p>");
 	}
 
 	@Override
 	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		// Send them to the upload servlet if they haven't uploaded battlecode files yet
 		if (!BSUtil.initializedBattlecode()) {
 			response.sendRedirect(UploadServlet.NAME);
 			return;
 		}
 		if (!request.getRequestURI().equals("/") && !request.getRequestURI().equals(NAME)) {
 			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
 			return;
 		}
 		response.setContentType("text/html");
 		response.setStatus(HttpServletResponse.SC_OK);
 		PrintWriter out = response.getWriter();
 		out.println("<html><head>");
 		out.println("<title>Battlecode Tester</title>");
 		out.println("<link rel=\"stylesheet\" href=\"/css/table.css\" />");
 		out.println("</head>");
 		out.println("<body>");
 
 		WebUtil.writeTabs(request, response, NAME);
 		out.println("<script src='js/jquery.dataTables.min.js'></script>");
 		
 		if (request.getParameter("submit-scrimmage") != null) {
 			File scrimmage = (File) request.getAttribute("scrimmage");
 			String scrimmageName = request.getParameter("scrimmageName").trim().replaceAll("\\s", "_");
 			if (scrimmage == null || !scrimmage.exists() || scrimmage.isDirectory()) {
 				warn(response, "Please select a valid map file");
 			} else if (scrimmageName == null || scrimmageName.isEmpty() || scrimmageName.contains("/")) {
 				warn(response, "Scrimmage file has invalid name"); 
 			} else {
 				BSScrimmageSet scrim = new BSScrimmageSet();
 				scrim.setFileName(scrimmageName);
 				scrim.setStatus(STATUS.QUEUED);
 				scrim.setPlayerA("");
 				scrim.setPlayerB("");
 				if (new File(scrim.toPath()).exists()) {
 					warn(response, "Scrimmage " + scrimmageName + " already exists!");
 				} else {
 					BSUtil.writeFileData(scrimmage, scrim.toPath());
 					highlight(response, "Successfully uploaded map: " + scrimmageName);
 					// Don't spawn a new thread because we *want* the new web thread to block
 					AbstractMaster.getMaster().analyzeScrimmageMatch(scrim);
 				}
 			}
 		}
 		
 		EntityManager em = HibernateUtil.getEntityManager();
 		// Begin the upload form
 		out.println("<form id='scrimmageForm' action='" + response.encodeURL(NAME) + "' method='post' enctype='multipart/form-data'>");
 		out.println("<table>");
 		out.println("<tr>" +
 				"<td style='text-align:right'>Scrimmage:</td>" +
 				"<td><input type='file' name='scrimmage' id='scrimFile' /></td>" +
 				"</tr>");
 		out.println("<tr><td></td>" +
 				"<td><input type='submit' name='submit-scrimmage' value='Upload'/></td>" +
 				"</tr>");
 		out.println("</table>");
 		out.println("<input id='scrimmageName' type='hidden' name='scrimmageName' />");
 		out.println("</form>");
 
 		out.println("<table id=\"scrim_table\" class='datatable datatable-clickable'>" +
 				"<thead>" + 
 				"<tr>" +
 				"<th>Scrimmage ID</th>" +
 				"<th>Name</th>" +
 				"<th>Team A</th>" +
 				"<th>Team B</th>" +
 				"<th>Status</th>" +
 				"<th>Control</th>" +
 				"</tr>" +
 				"</thead>" +
 		"<tbody>");
 
 		List<BSScrimmageSet> scrimmages = em.createQuery("from BSScrimmageSet", BSScrimmageSet.class).getResultList();
 		String myTeam = null;
 		// Try to find out what team we are
 		ArrayList<String> possibleTeams = new ArrayList<String>();
 		if (scrimmages.size() > 0) {
 			possibleTeams.add(scrimmages.get(0).getPlayerA());
 			possibleTeams.add(scrimmages.get(0).getPlayerB());
 		}
 		for (BSScrimmageSet s: scrimmages) {
 			if (possibleTeams.isEmpty())
 				break;
 			for (int i = 0; i < possibleTeams.size(); i++) {
 				boolean remove = true;
				if (s.getPlayerA().equals("") || s.getPlayerA().equals(possibleTeams.get(i))) {
 					remove = false;
 				}
				if (s.getPlayerB().equals("") || s.getPlayerB().equals(possibleTeams.get(i))) {
 					remove = false;
 				}
 				if (remove) {
 					possibleTeams.remove(i);
 					i--;
 				}
 			}
 		}
 		if (possibleTeams.size() == 1) {
 			myTeam = possibleTeams.get(0);
 		}
 		
 		for (BSScrimmageSet s: scrimmages) {
 			String td;
 			// Make scrimmages with data clickable
 			//TODO: move this into js
 			if (s.getStatus() == STATUS.COMPLETE || s.getStatus() == STATUS.CANCELED)
 				td = "<td onClick='doNavMatches(" + s.getId() + ")'>";
 			else
 				td = "<td>";
 			if (myTeam == null || s.getPlayerA().equals("")) {
 				out.println("<tr>");
 			} else {
 				boolean won = (s.getPlayerA().equals(myTeam) && s.getWinner() == TEAM.A) ||
 				(s.getPlayerB().equals(myTeam) && s.getWinner() == TEAM.B);
 				out.println("<tr class='" + (won ? "win" : "loss") + "'>");
 			}
 			
 			out.println(td + s.getId() + "</td>" + 
 					td + s.getFileName() + "</td>" +
 					td + s.getPlayerA() + "</td>" +
 					td + s.getPlayerB() + "</td>" +				
 					td + s.getStatus() + "</td>");
 			switch (s.getStatus()){
 			case QUEUED:
 				out.println("<td><input type=\"button\" value=\"dequeue\" onclick=\"delScrimmage(" + s.getId() + ")\"></td>");
 				break;
 			case RUNNING:
 				out.println("<td><input type=\"button\" value=\"cancel\" onclick=\"delScrimmage(" + s.getId() + ")\"></td>");
 				break;
 			case COMPLETE:
 			case CANCELED:
 				out.println("<td><input type=\"button\" value=\"delete\" onclick=\"delScrimmage(" + s.getId() + ")\"></td>");
 				break;
 			default:
 				out.println("<td><input type=\"button\" value=\"delete\" onclick=\"delScrimmage(" + s.getId() + ")\"></td>");
 			}
 			out.println("</tr>");
 		}
 		out.println("</tbody>");
 		out.println("</table>");
 		out.println("</div>");
 		
 		out.println("<script type=\"text/javascript\">var myTeam = '" + myTeam + "'</script>");
 		out.println("<script type=\"text/javascript\" src=\"js/bsUtil.js\"></script>");
 		out.println("<script type=\"text/javascript\" src=\"js/scrimmage.js\"></script>");
 		out.println("</body></html>");
 		em.close();
 	}
 	
 	@Override
 	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		doGet(request, response);
 	}
 }
