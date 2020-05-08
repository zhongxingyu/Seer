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
 
     }
 }
