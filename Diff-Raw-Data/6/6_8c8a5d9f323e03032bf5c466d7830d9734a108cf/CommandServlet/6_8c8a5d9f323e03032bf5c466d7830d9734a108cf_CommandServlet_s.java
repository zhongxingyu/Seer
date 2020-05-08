 package com.github.davidmoten.et;
 
 import java.io.IOException;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.Map;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import com.google.common.collect.Maps;
 
 /**
  * General purpose servlet. Doesn't really offer the richness of a formal REST
  * interface but is ok for something simple.
  * 
  * @author dxm
  * 
  */
 public class CommandServlet extends HttpServlet {
 
 	private static final String COMMAND_SAVE_REPORT = "saveReport";
 	private static final String COMMAND_GET_VERSION = "getVersion";
 	private static final String COMMAND_GET_REPORTS = "getReports";
 
 	private static final long serialVersionUID = 8026282588720357161L;
 
 	private final Database db = new Database();
 
 	@Override
 	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
 			throws ServletException, IOException {
 
 		String command = req.getParameter("command");
 		if (COMMAND_SAVE_REPORT.equals(command))
 			saveReport(req);
 		else if (COMMAND_GET_VERSION.equals(command))
 			getVersion(resp);
 		else if (COMMAND_GET_REPORTS.equals(command))
 			getReports(req, resp);
 		else
 			throw new RuntimeException("unknown command: " + command);
 	}
 
 	private void getReports(HttpServletRequest req, HttpServletResponse resp) {
 		double topLeftLat = Double.parseDouble(req.getParameter("topLeftLat"));
 		double topLeftLon = Double.parseDouble(req.getParameter("topLeftLon"));
 		double bottomRightLat = Double.parseDouble(req
 				.getParameter("bottomRightLat"));
 		double bottomRightLon = Double.parseDouble(req
 				.getParameter("bottomRightLon"));
 		Date start = parseDate(req.getParameter("start"));
 		Date finish = parseDate(req.getParameter("finish"));
 		String idName = req.getParameter("idName");
 		String idValue = req.getParameter("idValue");
 		resp.setContentType("application/json");
 		try {
 			db.writeReportsAsJson(topLeftLat, topLeftLon, bottomRightLat,
 					bottomRightLon, start, finish, idName, idValue,
 					resp.getWriter());
 		} catch (IOException e) {
 			throw new RuntimeException(e);
 		}
 
 	}
 
 	private void saveReport(HttpServletRequest req) {
 		Date time = parseDate(req.getParameter("time"));
 		double lat = Double.parseDouble(req.getParameter("lat"));
 		double lon = Double.parseDouble(req.getParameter("lon"));
 		int idCount = Integer.parseInt(req.getParameter("idc"));
 		Map<String, String> ids = Maps.newHashMap();
 		for (int i = 1; i <= idCount; i++) {
 			ids.put(req.getParameter("idn" + i), req.getParameter("idv" + i));
 		}
 		db.saveReport(time, lat, lon, ids);
 	}
 
 	private void getVersion(HttpServletResponse resp) {
 		try {
 			resp.getWriter().print(System.getProperty("application.version"));
 		} catch (IOException e) {
 			throw new RuntimeException(e);
 		}
 	}
 
 	@Override
 	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
 			throws ServletException, IOException {
 
 	}
 
 	/**
 	 * Returns the {@link Date} from a date string in format yyyy-MM-dd-HH-mm.
 	 * Date string is assumed to be in UTC time zone.
 	 * 
 	 * @param date
 	 * @return
 	 */
 	private Date parseDate(String date) {
 
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss.SSS-Z");
 		try {
			return sdf.parse(date + "-UTC");
 		} catch (ParseException e) {
 			throw new RuntimeException(e);
 		}
 
 	}
 
 }
