 package sc10dw.distributed.cw2.web;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.*;
 import java.io.*;
 
 public class UserForms extends HttpServlet {
 
 	@Override
 	public void doGet(HttpServletRequest request, HttpServletResponse response)
 		throws ServletException, IOException {
 		response.setContentType(Config.HTTP_CONTENT_TYPE);
 		PrintWriter out = response.getWriter();
 
 		out.println("<html>\n\n<head>\n\t<title>Employee Payroll System</title>\n</head>\n\n");
 		out.println("\t<body>");
 		out.println("\t\t<h1>Search Employees</h1>");
		out.println("\t\t<form action='" + Config.ROOT_URL + "/employee' method='GET'>");
 		out.println("\t\tEmployee ID: <input type='text' name='id' /><br />");
 		out.println("\t\t\t<input type='submit' value='Search' />");
 		out.println("\t\t</form>");
 		out.println("\t\t<h1>Create Employee</h1>");
		out.println("\t\t<form action='" + Config.ROOT_URL + "/create' method='POST'>");
		out.println("\t\tEmployee ID: <input type='text' name='id' /><br />");
 		out.println("\t\t\tForename: <input type='text' name='forename' /><br />");
 		out.println("\t\t\tSurname: <input type='text' name='surname' /><br />");
 		out.println("\t\t\tHourly Pay Rate: <input type='text' name='hourlyRate' /><br />");
 		out.println("\t\t\tNumber of Hours per Week: <input type='text' name='hoursPerWeek' /><br />");
 		out.println("\t\t\t<input type='submit' value='Create' />");
 		out.println("\t\t</form>");
 		out.println("</body>\n\n</html>");
 		
 		out.close();
 	}
 	
 }
