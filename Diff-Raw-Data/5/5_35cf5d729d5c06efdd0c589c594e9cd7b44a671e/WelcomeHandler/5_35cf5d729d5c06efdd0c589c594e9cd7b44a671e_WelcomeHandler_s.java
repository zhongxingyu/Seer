 package $package;
 
 import agave.AbstractHandler;
 import agave.HandlesRequestsTo;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import java.io.PrintWriter;
 
 public class WelcomeHandler extends AbstractHandler {
 
     @HandlesRequestsTo("/")
     public void welcome() throws Exception {
         response.setContentType("application/xhtml+xml");
         
         String context = request.getContextPath().substring(1);
         
         PrintWriter out = response.getWriter();
         out.println("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\"");
         out.println("  \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">");
         out.println("<html xmlns=\"http://www.w3.org/1999/xhtml\" xml:lang=\"en\" lang=\"en\">");
         out.println("  <head>");
         out.println("    <meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" />");
        out.format("    <title>Hello, {0}!</title>", context);
         out.println("  </head>");
         out.println("  <body>");
        out.format("    <p>Hello, {0}!</p>", context);
         out.println("  </body>");
         out.println("</html>");
         
         out.close();
     }
     
 }
