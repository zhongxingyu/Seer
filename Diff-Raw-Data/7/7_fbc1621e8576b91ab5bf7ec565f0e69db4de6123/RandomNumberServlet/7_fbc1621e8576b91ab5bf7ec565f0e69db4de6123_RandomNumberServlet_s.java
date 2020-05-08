 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.Random;
 
 /* from http://blog.frankel.ch/simplest-push */
 
 /* see also
 http://html5doctor.com/server-sent-events/
 http://today.java.net/article/2010/03/31/html5-server-push-technologies-part-1
 http://www.html5rocks.com/en/tutorials/eventsource/basics/
 http://www.w3.org/TR/eventsource/
 
  */
 
 
 public class RandomNumberServlet extends HttpServlet {
 
 	@Override
 	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
 
		resp.setContentType("text/event-stream");
		resp.setCharacterEncoding("utf-8");
 
 		Random random = new Random();
 
 		PrintWriter writer = resp.getWriter();
 
 		String next = "data: " + String.valueOf(random.nextInt(100) + 1);
 
 		System.out.println(next);
 
		writer.write(next + "\r\n");
 
 		writer.flush();
 	}
 }
