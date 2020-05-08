 package ar.edu.itba.paw.grupo1.controller;
 
 import java.io.IOException;
 import java.util.List;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import ar.edu.itba.paw.grupo1.ApplicationContainer;
 import ar.edu.itba.paw.grupo1.controller.BaseServlet;
 import ar.edu.itba.paw.grupo1.model.Property;
 import ar.edu.itba.paw.grupo1.service.PropertyService;
 
 @SuppressWarnings("serial")
 public class QueryServlet extends BaseServlet {
 
 	@Override
 	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
 			throws ServletException, IOException {
 
 		String operation = req.getParameter("operation");
 		String property = req.getParameter("property");
 		String rangeFrom = req.getParameter("rangeFrom");
 		String rangeTo = req.getParameter("rangeTo");
 
 		if (operation == null && property == null && rangeFrom == null
 				&& rangeTo == null) {
 
 			render(req, resp, "query.jsp", "Query");
 			return;
 		}
 
 		if (operation == null) {
 			operation = "any";
 		}
 		if (property == null) {
 			property = "any";
 		}
 
 		double parsedRangeFrom = 0;
		double parsedRangeTo = 0;
 		boolean badParse = false;
 
 		try {
			if (rangeFrom != null) {
 				parsedRangeFrom = Double.parseDouble(rangeFrom);
 			}
			if (rangeTo != null) {
 				parsedRangeTo = Double.parseDouble(rangeTo);
 			}
 		} catch (NumberFormatException e) {
 			badParse = true;
 		}
 
 		if (badParse
 				|| (rangeFrom != null && parsedRangeFrom < 0)
 				|| (rangeTo != null && parsedRangeTo < 0)
 				|| (rangeFrom != null && rangeTo != null && parsedRangeTo < parsedRangeFrom)) {
 
 			req.setAttribute("invalidRange", true);
 			render(req, resp, "query.jsp", "Query");
 			return;
 		}
 
 		if (rangeFrom == null) {
 			parsedRangeFrom = 0;
 		}
 		if (rangeTo == null) {
 			parsedRangeTo = Double.MAX_VALUE;
 		}
 
 		List<Property> query = ApplicationContainer.get(PropertyService.class)
 				.query(operation, property, parsedRangeFrom, parsedRangeTo);
 
 		req.setAttribute("queryResults", query);
 		render(req, resp, "query.jsp", "Query");
 	}
 
 }
