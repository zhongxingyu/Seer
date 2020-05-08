 package com.dierkers.schedule.http;
 
 import java.io.IOException;
 import java.net.BindException;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.eclipse.jetty.server.Server;
 import org.eclipse.jetty.servlet.ServletContextHandler;
 import org.eclipse.jetty.servlet.ServletHolder;
 
 import com.dierkers.schedule.ScheduleServer;
 import com.dierkers.schedule.api.ScheduleAPIAdd;
 
 /**
  * main servlet manager
  * @author Matthew
  * 
  */
 public class ScheduleServlet extends HttpServlet implements Runnable {
 
 	private static final long serialVersionUID = 8172536904188802024L;
 	private Server server;
 	private ScheduleServer ss;
 
 	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
 		resp.setContentType("text/html;charset=utf-8");
 
 		resp.getWriter().print("<h2>Hello from Java!</h2>\n");
 
 		ResultSet rs = ss.db().fquery("SELECT id, type, owner, time, data");
 		try {
 			while (rs.next()) {
 				resp.getWriter().print("ID: " + rs.getString("id") + ", Type: " + rs.getInt("type") + ", " + "Owner: "
 						+ rs.getString("owner") + ", Time: " + rs.getInt("time") + ", Data: " + rs.getString("data")
 						+ "<br>\n");
 			}
 		} catch (SQLException e) {
 			System.err.println("Error generating the page");
 			e.printStackTrace();
 		}
 	}
 
 	public ScheduleServlet(ScheduleServer ss) {
 		this.ss = ss;
 		String port = System.getenv("PORT");
 		server = new Server(port != null ? Integer.valueOf(port) : 5000);
 
 		ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
 		context.setContextPath("/");
 		server.setHandler(context);
 		context.addServlet(new ServletHolder(this), "/*");
 		context.addServlet(new ServletHolder(new ScheduleAPIAdd(ss)), "/add/*");
 	}
 
 	public void run() {
 		try {
 			server.start();
 			server.join();
 		} catch (BindException e) {
 			System.out.println("Address already in use - Is another server already running?");
 			System.exit(0);
 		} catch (Exception e) {
 			System.err.println("Error starting the server");
 			e.printStackTrace();
 		}
 	}
 }
