 package klim.services;
 
 import klim.orthodox_calendar.Day;
 import klim.orthodox_calendar.PMF;
 import klim.services.Pocket;
 
 import javax.servlet.ServletOutputStream;
 import javax.servlet.http.HttpServlet;
 
 import java.io.IOException;
 import java.util.logging.Logger;
 
 import javax.jdo.PersistenceManager;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import java.util.List;
 
 
 public class PocketServlet extends HttpServlet {
 	Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
 	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
 		String sendFrom = req.getParameter("email") == null ? "" : req.getParameter("email");
 		String url = req.getParameter("url") == null ? "" : req.getParameter("url");
 		Pocket p = new Pocket("add@getpocket.com");
		p.sendEmail("save url", url, Pocket.getServiceEmail(), sendFrom);
 		ServletOutputStream out = resp.getOutputStream();
 		out.println("<title>Email</title>"
 				+ "<body>"
 				+ "<ol>"
				+ "<li>to=" + Pocket.getServiceEmail() + "</li>"
 				+ "<li>from=" + sendFrom + "</li>"
 				+ "<li>url=" + url + "</li>"
 				+ "</ol>"
 				+ "</body>");
 	}
 }
