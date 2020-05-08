 package web;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.ArrayList;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import master.AbstractMaster;
 
 import org.apache.log4j.Logger;
 
 public class ActionServlet extends HttpServlet {
 	private static final long serialVersionUID = -4569590875419487616L;
 	private static final Logger _log = Logger.getLogger(ActionServlet.class);
 	public static final String NAME = "/action.html";
 	
 	@Override
 	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		String cmd = request.getParameter("cmd");
 		if (cmd == null) {
 			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
 			return;
 		}
 		response.setContentType("text/plain");
 		response.setStatus(HttpServletResponse.SC_OK);
 		if (cmd.equals("run")) {
 			queueRun(request, response);
 		} else if (cmd.equals("delete")) {
 			deleteRun(request, response);
 		} else if (cmd.equals("dequeue")) {
 			dequeueRun(request, response);
 		} else if (cmd.equals("cancel")) {
 			cancelRun(request, response);
 		} else if (cmd.equals("deleteScrim")) {
 			deleteScrimmage(request, response);
 		}
 		
 	}
 	
 	private void queueRun(HttpServletRequest request, HttpServletResponse response) throws IOException {
 		PrintWriter out = response.getWriter();
 		String teamAId = request.getParameter("team_a");
 		String teamBId = request.getParameter("team_b");
 		String seeds = request.getParameter("seeds");
 		String maps = request.getParameter("maps");
 		if (teamAId == null || !teamAId.matches("-?\\d+")) {
 			_log.warn("Error team A: " + teamAId);
 			out.print("err team_a");
 			return;
 		} else if (teamBId == null || !teamBId.matches("-?\\d+")) {
 			_log.warn("Error team B: " + teamBId);
 			out.print("err team_b");
 			return;
 		} else if (!maps.matches("\\d+(,\\d+)*")) {
 			_log.warn("Error map format: " + maps);
 			out.print("err maps");
 			return;
 		} else if (!seeds.matches("\\d+(,\\d+)*")) {
 			_log.warn("Error seed format: " + seeds);
 			out.print("err seed");
 			return;
 		}
 		Long teamAIdLong = new Long(Integer.parseInt(teamAId));
 		Long teamBIdLong = new Long(Integer.parseInt(teamBId));
 		ArrayList<Long> seedLongs = new ArrayList<Long>();
 		for (String seed: seeds.split(",")) {
 			seedLongs.add(new Long(Integer.parseInt(seed)));
 		}
 		ArrayList<Long> mapIds = new ArrayList<Long>();
 		for (String mapId: maps.split(",")) {
 			mapIds.add(new Long(Integer.parseInt(mapId)));
 		}
 		try {
 			AbstractMaster.kickoffQueueRun(teamAIdLong, teamBIdLong, seedLongs, mapIds);
 			out.print("success");
 		} catch (Exception e) {
 			e.printStackTrace(out);
 		}
 	}
 	
 	private void deleteRun(HttpServletRequest request, HttpServletResponse response) throws IOException {
 		PrintWriter out = response.getWriter();
 		String strId = request.getParameter("id");
 		if (strId == null || !strId.matches("\\d+")) {
 			out.println("Invalid id: " + strId);
 		}
 
 		AbstractMaster.getMaster().deleteRun(new Long(Integer.parseInt(strId)));
 		out.print("success");
 	}
 	
 	private void cancelRun(HttpServletRequest request, HttpServletResponse response) throws IOException {
 		PrintWriter out = response.getWriter();
 		String strId = request.getParameter("id");
 		if (strId == null || !strId.matches("\\d+")) {
 			out.println("Invalid id: " + strId);
 		}
 
 		AbstractMaster.getMaster().cancelRun(new Long(Integer.parseInt(strId)));
 		out.print("success");
 	}
 	private void dequeueRun(HttpServletRequest request, HttpServletResponse response) throws IOException {
 		PrintWriter out = response.getWriter();
 		String strId = request.getParameter("id");
 		if (strId == null || !strId.matches("\\d+")) {
 			out.println("Invalid id: " + strId);
 		}
 
 		AbstractMaster.getMaster().dequeueRun(new Long(Integer.parseInt(strId)));
 		out.print("success");
 	}
 	
 	private void deleteScrimmage(HttpServletRequest request, HttpServletResponse response) throws IOException {
 		PrintWriter out = response.getWriter();
 		String strId = request.getParameter("id");
 		if (strId == null || !strId.matches("\\d+")) {
 			out.println("Invalid id: " + strId);
 		}
 
 		AbstractMaster.getMaster().deleteScrimmage(new Long(Integer.parseInt(strId)));
 		out.print("success");
 	}
 }
