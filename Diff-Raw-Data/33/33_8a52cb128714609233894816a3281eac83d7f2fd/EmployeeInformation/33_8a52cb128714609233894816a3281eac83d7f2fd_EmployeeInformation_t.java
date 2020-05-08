 package sc10dw.distributed.cw2.web;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.rmi.RemoteException;
 import java.rmi.registry.LocateRegistry;
 import java.rmi.registry.Registry;
 import java.util.List;
 
 import javax.servlet.http.*;
 import javax.servlet.ServletException;
 
 import sc10dw.distributed.cw2.*;
 
 public class EmployeeInformation extends HttpServlet {
 
 	@Override
 	public void doGet(HttpServletRequest request, HttpServletResponse response)
 		throws ServletException, IOException {
 		response.setContentType(Config.HTTP_CONTENT_TYPE);
 		PrintWriter out = response.getWriter();
 		
 		String employeeObjectName = request.getParameter("id");
 		Employee employee = getEmployee(employeeObjectName);
 		if (employee != null) {
 			out.println("<html>\n\n<head>\n\t<title>Employee " + employee.getForename() + " " + employee.getSurname() + "</title>\n</head>\n\n");
 			out.println("\t<body>");
 			out.println("\t\t<h1>Employee Information</h1>");
 			out.println("\t\t<table>");
 			out.println("\t\t<tr><th>Name</th><td>" + employee.getForename() + " " + employee.getSurname() + "</td></tr>");
 			out.println("\t\t<tr><th>Hourly Rate</th><td>" + employee.getHourlyRate() + "</td></tr>");
 			out.println("\t\t<tr><th>Hours per Week</th><td>" + employee.getNumberHours() + "</td></tr>");
 			out.println("\t\t<tr><th>Total Earnings per Week</th><td>" + employee.getWeeklyEarning() + "</td></tr>");
 			out.println("\t\t</table>");
			out.println("\t\t<a href='" + Config.ROOT_URL + "'><button type='button'>Back to Main Page</button></a>");
			out.println("\t</body>\n\n</html>"); 
 		} else {
 			out.println("<html>\n\n<head>\n\t<title>Employee Not Found</title>\n</head>\n\n");
 			out.println("\t<body>\n\t\t<h1>Employee Not Found</h1>");
 			out.println("\t\t<p>Employee with object name '" + employeeObjectName + "' not found.</p>");
			out.println("\t\t<a href='" + Config.ROOT_URL + "'><button type='button'>Back to Main Page</button></a>");
 			out.println("\t</body>\n\n</html>");
 		}
 		
 		out.close();
 	}
 	
 	private Employee getEmployee(String objectName) {
 		// Try and retrieve the RMI object with the given object name
 		try {
 			Registry registry;
 			registry = LocateRegistry.getRegistry();
 			return (Employee)registry.lookup(objectName);
 		} catch (Exception e) {
 			return null;
 		}
 		
 	}
 	
 }
