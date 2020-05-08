 
 package injector;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 /**
  * A simple servlet which takes a parameter from a POST call and wraps each line
  * in "out.println();". This is intended to be used when you need to inject
  * multiple HTML lines in a Java servlet source file. With this, you can just
  * write the HTML as you want it, then send it to this servlet in order to
  * inject it in its Java source code form.
  * 
  * @author Aaron Foltz
  * 
  */
 
 @SuppressWarnings("serial")
 public class Java_HTML_InjectorServlet extends HttpServlet {
 
 	@Override
 	public void doGet(HttpServletRequest req, HttpServletResponse resp)
 			throws IOException {
 
 		// Set the response type
 		resp.setContentType("text/plain");
 
 		// Get the writer to write the HTML
 		PrintWriter out = resp.getWriter();
 
 		// HTML code for the presentation layer
 		String param = req.getParameter("code");
 
 		// If there is no param, then just return;
 		if (param == null) {
 			return;
 		}
 
 		// Split at the end of each line. This needs to go in a separate
 		// out.println
 		String[] output = param.split("\n");
 
 		// Print out writer initialization
 		out.println("PrintWriter out = resp.getWriter()");
 
 		// Iterate through each string in the split array
 		for (String line : output) {
 
			// If the line isn't just a newline, then we need to keep it
			if (!line.equals("\n")) {

 				// Trim the whitespace off the beginning and end of the line
 				line = line.trim();
 
 				// Print out the line wrapped in out.println()
 				out.println("out.println(\"" + line + "\");");
 			}
 		}
 	}
 
 	@Override
 	public void doPost(HttpServletRequest req, HttpServletResponse resp)
 			throws IOException {
 
 		doGet(req, resp);
 	}
 }
