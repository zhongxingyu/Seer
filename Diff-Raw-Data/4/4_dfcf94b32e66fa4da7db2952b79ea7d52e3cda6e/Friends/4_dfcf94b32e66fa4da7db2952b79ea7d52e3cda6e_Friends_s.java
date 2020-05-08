 /*
  * @(#)Friends.java	1.0   Feb 19, 2008
  *
  * Copyright 2000-2008 ETH Zurich. All Rights Reserved.
  *
  * This software is the proprietary information of ETH Zurich.
  * Use is subject to license terms.
  *
  */
 
 package ch.ethz.globis.web.facebook;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.net.URL;
 import java.net.URLEncoder;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.transform.dom.DOMSource;
 import javax.xml.transform.stream.StreamResult;
 import javax.xml.transform.stream.StreamSource;
 
 import org.w3c.dom.Document;
 
 import com.restfb.DefaultFacebookClient;
 
 /**
  * Facebook Application Friends Servlet
  * 
  * @author Stefania Leone <leone@inf.ethz.ch>
  * @author Michael Nebeling <nebeling@inf.ethz.ch>
  * @author Matthias Geel <geel@inf.ethz.ch>
  * @version 3.0
  */
 public class Friends extends HttpServlet {
 
 	private static final long serialVersionUID = 1L;
 
 	private static final int MAX_FRIENDS = 15;
 
 	@Override
 	public void doGet(final HttpServletRequest request,
 			final HttpServletResponse response) throws ServletException,
 			IOException {
 
 		// set MIME type and encoding
 		response.setContentType("text/html");
 		response.setCharacterEncoding("UTF-8");
 
 		// get writer for output
 		final PrintWriter p = response.getWriter();
 
 		// make sure that we have obtained an access token, otherwise redirect
 		// to login
 		final String accessToken = request.getParameter("access_token");
 		if (accessToken == null) {
 			response.sendRedirect(Config.getValue("LOGIN_URL"));
 			return;
 		} else {
 			// Store the token in a session
 			HttpSession session = request.getSession();
 			session.setAttribute(Config.getValue("ACCESS_TOKEN_SESSION"),
 					accessToken);
 		}
 
 		// get client
 		final DefaultFacebookClient client = new DefaultFacebookClient(
 				accessToken);
 
 		// retrieve the document with all friend user ids
 		try {
 
 			final URL url = new URL(
 					"https://api.facebook.com/method/fql.query?access_token="
 							+ accessToken
 							+ "&query="
 							+ URLEncoder
 									.encode("SELECT name,uid,pic_square FROM user WHERE uid IN ( SELECT uid2 FROM friend WHERE uid1=me() )",
 											"UTF-8"));
 
 			final DocumentBuilderFactory factory = DocumentBuilderFactory
 					.newInstance();
 			final DocumentBuilder builder = factory.newDocumentBuilder();
 
 			final Document doc = builder.parse(url.openStream());
 
 			// returns an XML tree
 			// OutputGenerator.transformToXML(new DOMSource(doc), new
 			// StreamResult(p));
 
 			// transform the XML to HTML
 			final StreamSource source = new StreamSource(new File(this
 					.getServletContext().getRealPath(
							Config.getValue("XSL_FILE"))));
 			OutputGenerator.transformWithStyle(new DOMSource(doc), source,
 					new StreamResult(p));
 		} catch (final Exception e) {
 			System.out.println(e.getMessage());
 			e.printStackTrace();
 		}
 
 		p.flush();
 		p.close();
 	}
 
 	@Override
 	public void doPost(final HttpServletRequest request,
 			final HttpServletResponse response) throws ServletException,
 			IOException {
 
 		this.doGet(request, response);
 	}
 
 }
