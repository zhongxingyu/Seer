 import java.io.IOException;
 import java.io.PrintWriter;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.xml.security.utils.Base64;
 import org.nchelp.meteor.security.SecurityToken;
 
 /**
  *   Class SampleBump.java
  *
  *   @author  timb
  *   @version $Revision$ $Date$
  *   @since   Jun 17, 2002
  */
 public class SampleBump extends HttpServlet {
 
 	/**
 	 * Constructor for SampleBump.
 	 */
 	public SampleBump() {
 		super();
 	}
 
 	/**
 	 * @see javax.servlet.http.HttpServlet#doGet(HttpServletRequest, HttpServletResponse)
 	 */
 	protected void doGet(HttpServletRequest req, HttpServletResponse res)
 		throws ServletException, IOException {
 		
 		// Send the response
 		res.setContentType("text/html");
 		PrintWriter out = res.getWriter();
 
 		out.println("<html>");
 		out.println("<head><title>Meteor Bump</title></head>");
 		out.println("<body>");
 		out.println(
 			"<p>HTTP GET not supported.  You must use HTTP POST</p>");
 		out.println("</body></html>");
 
 		out.close(); 
 			
 	}
 
 	/**
 	 * @see javax.servlet.http.HttpServlet#doPost(HttpServletRequest, HttpServletResponse)
 	 */
 	protected void doPost(HttpServletRequest req, HttpServletResponse res)
 		throws ServletException, IOException {
 		res.setContentType("text/html");
 		PrintWriter out = res.getWriter();
 		
 		out.println("<html><head><title>Bump Response</title></head><body>");
 		
 		out.println("Parameters: <ul>");
 
 		String successful = req.getParameter("successful");
 		String unsuccessful = req.getParameter("unsuccessful");
 		String accessprovider = req.getParameter("accessprovider");
 		String minimumlevel = req.getParameter("minimumlevel");
 
 		out.println("<li>" + successful + "</li>");
 		out.println("<li>" + unsuccessful + "</li>");
 		out.println("<li>" + accessprovider + "</li>");
 		out.println("<li>" + minimumlevel + "</li>");
 		out.println("</ul>");
 		
 		
 		String userid = req.getParameter("userid");
 		String procid = req.getParameter("authprocid");
 		SecurityToken token = new SecurityToken();
 		try {
 			token.setUserid(userid);
 			token.setAuthenticationProcessID(procid);
 			token.setCurrentAuthLevel(Integer.parseInt(minimumlevel));
			token.setRole(SecurityToken.roleFAA);
 		} catch (Exception e) {
 			out.println(e.getMessage());
 		}
 		
 		String encodedAssertion = Base64.encode(token.toString().getBytes());
 		out.println("Assertion:");
 		out.println("<pre>" + encodedAssertion +"</pre>");
 		out.println("<form action=\"" + successful + "\" method=\"post\">");
 		out.println("<input type=\"hidden\" name=\"assertion\" value=\"" + encodedAssertion + "\">");
 		out.println("<input type=\"submit\"></form>");
 		out.println("</body></html>");
 		out.close();
 	}
 
 }
