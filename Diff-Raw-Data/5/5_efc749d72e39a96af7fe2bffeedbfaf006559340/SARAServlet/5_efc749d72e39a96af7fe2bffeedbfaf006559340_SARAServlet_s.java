 package sara;
 
 import java.io.IOException;
 import javax.servlet.http.*;
 import sara.HighlightService;
 
 public class SARAServlet extends HttpServlet {
     public void doGet(HttpServletRequest req, HttpServletResponse resp)
             throws IOException {
         resp.setContentType("text/plain");
         resp.getWriter().println("Hello, world");
 
         HighlightService hs = new HighlightService();
 
        hs.addDocumentsAndHighlights();

        resp.getWriter().println(hs.listDocuments());
        resp.getWriter().println(hs.listHighlights());
        resp.getWriter().println(hs.listSelections());
     }
 }
