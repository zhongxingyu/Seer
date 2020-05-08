 package jipdbs.web;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import jipdbs.AliasResult;
 import jipdbs.JIPDBS;
 
 public class AliasServlet extends HttpServlet {
 
 	private static final long serialVersionUID = 8123604074174537109L;
 
 	private JIPDBS app;
 
	private final int DEFAULT_OFFSET = 10;
 	
 	@Override
 	public void init() throws ServletException {
 		app = (JIPDBS) getServletContext().getAttribute("jipdbs");
 	}
 
 	@Override
 	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
 			throws ServletException, IOException {
 
 		String id = req.getParameter("id");
 		
 		int offset = 0;
 		
 		try {
 			offset = Integer.parseInt(req.getParameter("o"));
 		} catch (NumberFormatException e) {
 			// Ignore.
 		}
 
 		List<AliasResult> list = new ArrayList<AliasResult>();
 
 		int[] count = new int[1];
		list = app.alias(id, offset, DEFAULT_OFFSET, count);
 
		req.setAttribute("hasMore", new Boolean((offset + DEFAULT_OFFSET) < count[0]));
 		req.setAttribute("list", list);
 		req.setAttribute("count", count);
 	}
 }
