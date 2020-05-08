 package ufly.frs;
 
 import java.io.IOException;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 @SuppressWarnings("serial")
 public class Includer extends HttpServlet {
 	public void doGet(HttpServletRequest req, HttpServletResponse resp)
 	throws ServletException,IOException
 	{
 		String pageToInclude= getServletConfig().getInitParameter("page");
 		if(pageToInclude.equals("footer"))
 		{
 			req.getRequestDispatcher("foot.jsp")
 				.include(req,resp);
 		}else if (pageToInclude.equals("header"))
 		{
 			req.getRequestDispatcher("head.jsp")
 				.include(req,resp);
 		}
 			
 	}
	public void doPost(HttpServletRequest req, HttpServletResponse resp)
	throws ServletException,IOException		
	{
		doGet(req,  resp);
	}
 }
