 import net.sf.json.JSONException;
 import net.sf.json.JSONObject;
 import org.apache.commons.io.IOUtils;
 import scalaskel.Change;
 import scalaskel.ChangeService;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.PrintWriter;
 
 public class Main extends HttpServlet {
 
     private static final String SCALASKEL = "/scalaskel/change/";
     private ChangeService service = new ChangeService();
 
     private JSONObject routes;
 
     @Override
     public void init() throws ServletException {
         InputStream stream = getClass().getClassLoader().getResourceAsStream("route.json");
         try {
             routes = JSONObject.fromObject(IOUtils.toString(stream));
         } catch (IOException e) {
             throw new ServletException("failed to load routes definition");
         }
     }
 
     @Override
     protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
         getServletContext().log("POST:" + IOUtils.toString(req.getInputStream()));
         resp.setStatus(HttpServletResponse.SC_CREATED);
     }
 
     @Override
     protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
 
         String path = req.getPathInfo();
 
        // tomcat don't behave like jetty
        if (path == null) path = req.getServletPath();
 
         if (path.equals("/")) {
             String q = req.getParameter("q");
             String r;
             if (q == null) {
                 r = "@see http://code-story.net";
             } else {
                 try {
                     r = routes.getString(q);
                 } catch(JSONException e) {
                     r = "Désole, je ne comprends pas votre question";
                     resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
                 }
             }
             resp.getWriter().print(r);
         }
         else if (path.startsWith(SCALASKEL)) {
             int groDessimal = Integer.parseInt(path.substring(SCALASKEL.length()));
             resp.setContentType("application/json");
             PrintWriter w = resp.getWriter();
             String sep = "[";
             for (Change change : service.getPossibleChanges(groDessimal)) {
                 w.print(sep);
                 sep = ", ";
                 w.print(change.asJson());
             }
             w.print("]");
         }
    }
 }
