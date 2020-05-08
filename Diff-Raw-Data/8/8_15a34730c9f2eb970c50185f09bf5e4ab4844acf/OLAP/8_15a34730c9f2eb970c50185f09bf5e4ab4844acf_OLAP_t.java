 import java.io.*;
 import javax.servlet.*;
 import javax.servlet.http.*;
 import java.sql.*;
 
 public class OLAP extends HttpServlet {
 
 	public static String driverName = "oracle.jdbc.driver.OracleDriver";
 	public static String url = "jdbc:oracle:thin:@gwynne.cs.ualberta.ca:1521:CRS";
 
 	// ONE LONG, UGLY METHOD TO RULE THEM ALL
 	public void doGet(HttpServletRequest req, HttpServletResponse res)
 			throws IOException, ServletException {
 
 		HttpSession session = req.getSession();
 		PrintWriter out = res.getWriter();
 		res.setContentType("text/html");
 		String sessionUser = (String) session.getAttribute("userName");
 		String sql = "select USER_NAME from users where USER_NAME = '"
				+ sessionUser + "' and class = 'a'";
 		try {
			Connection conn = DriverManager.getConnection(url, "zturchan", "Pikachu1");
			Statement stmt = conn.createStatement();
			ResultSet rset = stmt.executeQuery(sql);
 			if (!rset.next()) {
 				// If we're not a curently authenticated user, redirect to login
 				javax.swing.JOptionPane
 						.showMessageDialog(
 								null,
 								"You are not currently authenticated as an administrator. Please authenticate first.");
 
 				res.sendRedirect("../proj1/login.html");
 			}
 		} catch (Exception ex) {
 			out.println("<hr>" + ex.getMessage() + "<hr>");
 		}
 
 		// build query params
 		String use_name = "is null";
 		if (req.getParameter("use_name").equals("Yes")) {
 			use_name = "is not null";
 		}
 		String use_type = "is null";
 		if (req.getParameter("use_type").equals("Yes")) {
 			use_type = "is not null";
 		}
 		String granularity = "";
 		if (req.getParameter("granularity").equals("year")) {
 			granularity = "tyear is not null and tmonth is null and tWeek is null";
 		}
 		if (req.getParameter("granularity").equals("month")) {
 			granularity = "tyear is null and tmonth is not null and tWeek is null";
 		}
 		if (req.getParameter("granularity").equals("week")) {
 			granularity = "tyear is null and tmonth is null and tWeek is not null";
 		}
 		if (req.getParameter("granularity").equals("no")) {
 			granularity = "tyear is null and tmonth is null and tWeek is null";
 		}
 
 		// now get records
 		try {
 			Class drvClass = Class.forName(driverName);
 			Connection con = DriverManager.getConnection(url, "zturchan",
 					"Pikachu1");
 			Statement stmt = con.createStatement();
 			ResultSet rset = stmt
 					.executeQuery("SELECT patient_name, test_type, tyear, tmonth, tweek, record_count FROM data_cube where patient_name "
 							+ use_name
 							+ " and test_type "
 							+ use_type
 							+ " and "
 							+ granularity);
 
 			// print html stuff
 			out.println("<HTML><HEAD><TITLE>RIS - OLAP</TITLE>");
 			out.println("<link rel='stylesheet' type='text/css' href='style.css' /><HEAD>");
 			out.println("<BODY><div id='content'><TABLE border=1><TR valign=top align=left>");
 			out.println("<td>Patient Name</td><td>Test Type</td><td>Time Period</td><td>Record Count</td></tr>");
 			while (rset.next()) {
 				out.println("<TR valign=top align=left>");
 				if (req.getParameter("use_name").equals("Yes")) {
 					out.println("<td>" + rset.getString("patient_name")
 							+ "</td>");
 				} else {
 					out.println("<td>All</td>");
 				}
 
 				if (req.getParameter("use_type").equals("Yes")) {
 					out.println("<td>" + rset.getString("test_type") + "</td>");
 				} else {
 					out.println("<td>All</td>");
 				}
 				// now check what time period to print
 				if (req.getParameter("granularity").equals("year")) {
 					out.println("<td>" + rset.getInt("tyear") + "</td>");
 				}
 				if (req.getParameter("granularity").equals("month")) {
 					out.println("<td>" + rset.getInt("tmonth") + "</td>");
 				}
 				if (req.getParameter("granularity").equals("week")) {
 					out.println("<td>" + rset.getString("tweek") + "</td>");
 				}
 				if (req.getParameter("granularity").equals("no")) {
 					out.println("<td>Any</td>");
 				}
 				// and print record count
 				out.println("<td>" + rset.getInt("record_count") + "</td>");
 			}
 			// close your shit
 			stmt.close();
 			con.close();
 		} catch (Exception e) {
 			out.println("<hr>" + e.getMessage() + "<hr>");
 		}
 		out.println("</TABLE></div><div id='footer'><a href='../proj1/home.html'>Home</a><br><a href='../proj1/logout.jsp'>Logout</a></div></BODY></HTML>");
 
 	}
 }
