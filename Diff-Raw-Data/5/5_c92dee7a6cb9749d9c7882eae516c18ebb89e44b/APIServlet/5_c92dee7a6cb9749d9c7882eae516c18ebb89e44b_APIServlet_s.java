 /*
  *
  */
 
 package com.luntsys.luntbuild;
 
 import com.luntsys.luntbuild.repliers.*;
 import com.luntsys.luntbuild.security.SecurityHelper;
 import com.luntsys.luntbuild.utility.Luntbuild;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import javax.servlet.ServletException;
 import javax.servlet.ServletOutputStream;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import java.io.IOException;
 
 /**
  * Servlet to handle requests for the public API functions.
  *
  * @author Jason Archer
  * @see Replier
  */
 public class APIServlet extends HttpServlet {
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 8592724920337836814L;
 	private static Log logger = LogFactory.getLog(APIServlet.class);
 
 	/**
 	 * Handles a requests.
 	 * 
 	 * @param httpServletRequest the HTTP request object
 	 * @param httpServletResponse the HTTP response object
 	 * @throws ServletException if the request could not be handled
 	 * @throws IOException if detected when handling the request
 	 */
 	protected void doGet(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws ServletException, IOException {
 		if (isValid(httpServletRequest.getPathInfo())) {
 			Replier replier = null;
 			String source = "";
 			String method = "";
 			String notify = httpServletRequest.getParameter("notify");
 
 			if (httpServletRequest.getPathInfo().startsWith("/atom")) {
 				replier = new AtomReplier();
 				source = httpServletRequest.getPathInfo().replaceFirst("/atom/?","");
 				httpServletResponse.setContentType("application/atom+xml");
 
 			} else if (httpServletRequest.getPathInfo().startsWith("/json")) {
 				replier = new JSONReplier();
 				source = httpServletRequest.getPathInfo().replaceFirst("/json/?","");
 
 				String callback = httpServletRequest.getParameter("callback");
 				((JSONReplier) replier).setCallback(callback);
 
 				httpServletResponse.setHeader("Content-disposition", "inline;filename=json");
 				httpServletResponse.setContentType("application/json");
 
 			} else if (httpServletRequest.getPathInfo().startsWith("/opml")){
 				replier = new OPMLReplier();
 				source = httpServletRequest.getPathInfo().replaceFirst("/opml/?","");
 				httpServletResponse.setContentType("text/xml");
 
 			} else if (httpServletRequest.getPathInfo().startsWith("/rss")) {
 				replier = new RSSReplier();
 				source = httpServletRequest.getPathInfo().replaceFirst("/rss/?","");
 				httpServletResponse.setContentType("application/rss+xml");
 
 			} else if (httpServletRequest.getPathInfo().startsWith("/xml")){
 				replier = new XMLReplier();
 				source = httpServletRequest.getPathInfo().replaceFirst("/xml/?","");
 				httpServletResponse.setContentType("text/xml");
 			}
 
 			if (hasMethod(source)) {
 				method = source.split("/")[0];
 				source = source.replaceFirst(method + "/?","");
 			}
 			if (replier == null) {
 				logger.error("Unable to get replier");
 				return;
 			}
 			replier.setSource(source);
 			replier.setMethod(method);
 			replier.setNotify(notify);
 
 			SecurityHelper.runAsSiteAdmin();
 
 			ServletOutputStream out = httpServletResponse.getOutputStream();
 			String reply = replier.getReply() + Luntbuild.getEol(httpServletRequest);
 
 			httpServletResponse.setContentLength(reply.getBytes().length);
 			out.write(reply.getBytes());
             httpServletResponse.flushBuffer();
 		} else if (httpServletRequest.getPathInfo().startsWith("/help")) {
 			String source = httpServletRequest.getPathInfo().replaceFirst("/help","");
 			String help = "";
			String url = "/luntbuild/docs/api/index.html";
 
 			if (source.startsWith("/atom")) {
 				help = source.replaceFirst("/atom/?","");
 				url += "#atom" + help;
 			} else if (source.startsWith("/json")) {
 				help = source.replaceFirst("/json/?","");
 				url += "#json" + help;
 			} else if (source.startsWith("/rss")) {
 				help = source.replaceFirst("/rss/?","");
 				url += "#rss" + help;
 			} else if (source.startsWith("/xml")) {
 				help = source.replaceFirst("/xml/?","");
 				url += "#xml" + help;
 			}
 
 	        try {
 	        	httpServletResponse.sendRedirect(url);
 			} catch (IllegalStateException ise) {
 				throw new ServletException(ise);
 			}
 		} else if (httpServletRequest.getPathInfo().matches("^/$|^$")) {
 			try {
	        	httpServletResponse.sendRedirect("/luntbuild/docs/api/index.html");
 			} catch (IllegalStateException ise) {
 				throw new ServletException(ise);
 			}
 		} else {
 			throw new ServletException("APIServlet rejects path: " + httpServletRequest.getPathInfo());
 		}
 	}
 
 	/**
 	 * Checks of the request path is valid.
 	 * 
 	 * @param path the requested path
 	 * @return <code>true</code> if the path is valid
 	 */
 	private boolean isValid(String path) {
 		if (path.startsWith("/atom")) {
 			return true;
 		} else if (path.startsWith("/json")) {
 			return true;
 		} else if (path.startsWith("/opml")) {
 			return true;
 		} else if (path.startsWith("/rss")) {
 			return true;
 		} else if (path.startsWith("/xml")) {
 			return true;
 		}
 
 		return false;
 	}
 
 	/**
 	 * Checks of the request path contains a method/function.
 	 * 
 	 * @param path the requested path
 	 * @return <code>true</code> if the path contains a method
 	 */
 	private boolean hasMethod(String path) {
 		if (path.startsWith("projects")) {
 			return true;
 		} else if (path.startsWith("schedules")) {
 			return true;
 		} else if (path.startsWith("builds")) {
 			return true;
 		} else if (path.startsWith("build")) {
 			return true;
 		} else if (path.startsWith("users")) {
 			return true;
 		} else if (path.startsWith("user")) {
 			return true;
 		}
 
 		return false;
 	}
 }
