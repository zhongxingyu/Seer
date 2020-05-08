 package ca.ubc.cpsc304.r3.web.responder;
 
 
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.servlet.http.HttpServletRequest;
 
 import ca.ubc.cpsc304.r3.db.BorrowingDao;
 import ca.ubc.cpsc304.r3.db.ConnectionService;
 import ca.ubc.cpsc304.r3.db.OverdueDao;
 import ca.ubc.cpsc304.r3.dto.BookCheckoutReportDto;
 import ca.ubc.cpsc304.r3.dto.CheckedOutBookDto;
 import ca.ubc.cpsc304.r3.dto.OverdueDto;
 import ca.ubc.cpsc304.r3.web.DirectorServlet.ViewAndParams;
 
 public class ReportController {
 
 	public ViewAndParams getCheckedOutBooksForm() {
 		ViewAndParams vp = new ViewAndParams("/jsp/librarian/reportCheckedOutBooksForm.jsp");
 		return vp;
 	}
 
 	/**
 	 * From requirements:
 	 * 
 	 * Generate a report with all the books that have been checked out.
 	 * For each book the report shows the date it was checked out and the
 	 * due date. The system flags the items that are overdue. The items are
 	 * ordered by the book call number.  If a subject is provided the report
 	 * lists only books related to that subject, otherwise all the books that
 	 * are out are listed by the report.
 	 * 
 	 * 
 	 */
 	public ViewAndParams getCheckedOutBooksReport(HttpServletRequest request) {
 		ViewAndParams vp = new ViewAndParams("/jsp/librarian/reportCheckedOutBooksDisplay.jsp");
 		try{
 
 			@SuppressWarnings("unchecked")
 			Map<String, String[]> reqParams = request.getParameterMap();
 
 			String subject = reqParams.get("subject")[0];
 			BorrowingDao dao = new BorrowingDao(ConnectionService.getInstance());
 			List<CheckedOutBookDto> checkedOutBooks = dao.generateCheckedOutBooksReport(subject);
 
 			vp.putViewParam("checkedOutBooks", checkedOutBooks);
 			vp.putViewParam("now", new Date());
 
 			return vp;
 
 		} catch (Exception e){
 
 			vp.putViewParam("hasError", true);
 			vp.putViewParam("errorMsg", BookController.generateFriendlyError(e));
 			return vp;
 
 		}
 
 
 	}
 
 	public ViewAndParams getMostPopularBooksReportForm() {
 		ViewAndParams vp = new ViewAndParams("/jsp/librarian/reportMostPopularBooksForm.jsp");
 		return vp;
 	}
 
 	/**
 	 * 
 	 * Generate a report with the most popular items
 	 * in a given year.  The librarian provides a year
 	 * and a number n. The system lists out the top n
 	 * books that where borrowed the most times during
 	 * that year. The books are ordered by the number
 	 * of times they were borrowed.
 	 * 
 	 */
 	public ViewAndParams getMostPopularBooksReport(HttpServletRequest request) {
 		ViewAndParams vp = new ViewAndParams("/jsp/librarian/reportMostPopularBooksDisplay.jsp");
 
 		try {
 
 			@SuppressWarnings("unchecked")
 			Map<String, String[]> reqParams = request.getParameterMap();
 			
 			int year = Integer.parseInt(reqParams.get("year")[0]);
 			int limit = Integer.parseInt(reqParams.get("limit")[0]);
 
 			BorrowingDao dao = new BorrowingDao(ConnectionService.getInstance());
 			List<BookCheckoutReportDto> mostPopularBooks = dao.generateMostPopularBooksReport(year, limit);
 			
 			vp.putViewParam("mostPopularBooks", mostPopularBooks);
 			return vp;
 			
 		} catch (Exception e) {
 			vp.putViewParam("hasError", true);
 			vp.putViewParam("errorMsg", BookController.generateFriendlyError(e));
 			return vp;
 		}
 	}
 
 	public ViewAndParams getOverdueReportForm() {
 		ViewAndParams vp = new ViewAndParams("/jsp/clerk/checkOverdueForm.jsp");
 		return vp;
 	}
 
 	public ViewAndParams getOverdueReportResults(HttpServletRequest request) {
 		ViewAndParams vp = new ViewAndParams("/jsp/clerk/checkOverdueDisplay.jsp");
 		Map<String, String[]> outPut = new HashMap<String, String[]>();
 		OverdueDao odao = new OverdueDao(ConnectionService.getInstance());
 		List<OverdueDto> dtos = new ArrayList();;
 		try {
 			dtos = odao.checkOverdue(request.getParameter("bid"));
 			int size = dtos.size();
 			if(size <= 0){
 				vp.putViewParam("noOverdue", "Yay! There are no overdue books!");
 				vp.putViewParam("overdues", dtos);
 				return vp;
 			}
 			String[] titles = new String[size];
 			String[] borrowers = new String[size];
 			String[] emails = new String[size];
 
 			for(int i=0; i<size; i++){
 				titles[i] = dtos.get(i).getTitle();
 				borrowers[i] = dtos.get(i).getName();
 				emails[i] = dtos.get(i).getEmail();
 			}
 
 			outPut.put("Title", titles);
 			outPut.put("Name", borrowers);
 			outPut.put("Email", emails);
 
 
 		} catch (SQLException e) {
 			// TODO UNABLE TO GET OVERDUE REPORT
 			e.printStackTrace();
 		} catch (Exception e){
 			//bad exception
 			e.printStackTrace();
 		}
 
 		vp.putViewParam("overdue", outPut);
 		return vp;
 	}
 
 
 }
