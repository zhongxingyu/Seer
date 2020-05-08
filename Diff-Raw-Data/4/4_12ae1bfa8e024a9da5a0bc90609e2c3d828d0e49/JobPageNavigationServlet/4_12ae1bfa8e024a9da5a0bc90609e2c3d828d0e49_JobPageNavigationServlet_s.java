 package com.VolunteerCoordinatorApp;
 import java.io.*;
 import javax.servlet.http.*;
 
 @SuppressWarnings("serial")
 public class JobPageNavigationServlet extends HttpServlet {
 	public void doPost(HttpServletRequest req, HttpServletResponse resp)
 	throws IOException {
 		String name = req.getParameter("name"); 
 		String startRange = req.getParameter("startDate"); 
 		String endRange = req.getParameter("endDate"); 
 		String cat = req.getParameter("category");
 		if (req.getParameter("catCheck") == null) {
 			cat = "null";
 		}
 		if (req.getParameter("date") == null && (startRange.equals("null") || startRange.equals("") || startRange == null) && (endRange.equals("null") || endRange.equals("") ||endRange == null)) {
 			startRange = "null";
 			endRange = "null";
 		}
 		
 		String nav = req.getParameter("navsubmit"); 
 		int pageNumber = Integer.parseInt(req.getParameter("pageNum"));
 		
 		if (nav.equals("Next")){
 			pageNumber++; 
 		} else if (nav.equals("Prev") && pageNumber > 1) {
 			pageNumber--; 
 		}
 		
 		int resultIndex = pageNumber; 
 		
 		if(pageNumber > 1) {
 			resultIndex = (pageNumber - 1) * 10 + 1;
 		}
 		
		resp.sendRedirect("volunteer.jsp?resultIndex=" + resultIndex + "&pageNumber=" + pageNumber
 				+ "&name=" + name + "&startDate=" + startRange + "&endDate=" + endRange + "&category=" + cat); 
 		
 	}
 }
