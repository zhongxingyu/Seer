 package com.example.newsfeed;
 
 import java.io.IOException;
 
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.jsoup.Connection;
 import org.jsoup.Connection.Method;
 import org.jsoup.Jsoup;
 
 public class FeedValidatorServlet extends HttpServlet {
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 
 	public void doGet(HttpServletRequest req, HttpServletResponse resp)
 			throws IOException {
 		String feedurl = req.getParameter("feed");
 		Connection.Response res2Jsoup = Jsoup
 				.connect(feedurl)
 				.timeout(60 * 10000)
 				.header("User-Agent",
 						"Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10.4; en-US; rv:1.9.2.2) Gecko/20100316 Firefox/3.6.2")
 				.method(Method.GET).execute();
 
 		String xml = res2Jsoup.body();
		System.out.print(xml);
 		resp.setContentType(res2Jsoup.contentType());
 		resp.getWriter().print(xml);
 	}
 }
