 package org.sakaiproject.clog.tool;
 
 import java.io.IOException;
 import java.net.URLEncoder;
 import java.util.List;
 import java.util.Locale;
 import java.util.Map;
 import java.util.Set;
 
 import javax.servlet.RequestDispatcher;
 import javax.servlet.ServletConfig;
 import javax.servlet.ServletException;
 import javax.servlet.http.Cookie;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import net.sf.json.JSONArray;
 import net.sf.json.JSONObject;
 
 import org.apache.log4j.Logger;
 import org.sakaiproject.clog.api.ClogManager;
 import org.sakaiproject.clog.api.SakaiProxy;
 import org.sakaiproject.component.api.ComponentManager;
 import org.sakaiproject.search.api.InvalidSearchQueryException;
 import org.sakaiproject.search.api.SearchResult;
 import org.sakaiproject.util.ResourceLoader;
 
 /**
  * @author Adrian Fish (a.fish@lancaster.ac.uk)
  */
 public class ClogTool extends HttpServlet {
     private Logger logger = Logger.getLogger(getClass());
 
     private SakaiProxy sakaiProxy;
     private ClogManager clogManager = null;
 
     public void init(ServletConfig config) throws ServletException {
 	super.init(config);
 
 	if (logger.isDebugEnabled())
 	    logger.debug("init");
 
 	ComponentManager componentManager = org.sakaiproject.component.cover.ComponentManager.getInstance();
 	sakaiProxy = (SakaiProxy) componentManager.get(SakaiProxy.class);
 	clogManager = (ClogManager) componentManager.get(ClogManager.class);
     }
 
     protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 	if (logger.isDebugEnabled())
 	    logger.debug("doGet()");
 
 	if (sakaiProxy == null)
 	    throw new ServletException("sakaiProxy MUST be initialised.");
 
 	String state = request.getParameter("state");
 	String postId = request.getParameter("postId");
 
 	if (state == null)
 	    state = "viewAllPosts";
 
 	if (postId == null)
 	    postId = "none";
 
 	String siteId = sakaiProxy.getCurrentSiteId();
 
 	String userId = sakaiProxy.getCurrentUserId();
 
 	if (userId == null) {
 	    // We are not logged in. Could be the gateway placement.
 	    if (!"!gateway".equals(siteId)) {
 		// There should be an authenticated user at this point.
 		throw new ServletException("getCurrentUser returned null.");
 	    }
 	}
 
 	String toolId = sakaiProxy.getCurrentToolId();
 
 	// We need to pass the language code to the JQuery code in the pages.
 	Locale locale = (new ResourceLoader(userId)).getLocale();
 	String languageCode = locale.getLanguage();
	
	// CLOG-44
	if("".equals(languageCode)) {
	    languageCode = "en";
	}
 
 	String pathInfo = request.getPathInfo();
 
 	boolean publicAllowed = sakaiProxy.isPublicAllowed();
 
 	if (pathInfo == null || pathInfo.length() < 1) {
 	    String uri = request.getRequestURI();
 
 	    // There's no path info, so this is the initial state
 	    if (uri.contains("/portal/pda/")) {
 		// The PDA portal is frameless for redirects don't work. It also
 		// means that we can't pass url parameters to the page.We can
 		// use a cookie and the JS will pull the initial state from that
 		// instead.
 		Cookie params = new Cookie("sakai-tool-params", "state=" + URLEncoder.encode(state,"UTF-8") + "&siteId=" + siteId + "&placementId=" + toolId + "&postId=" + URLEncoder.encode(postId,"UTF-8") + "&langage=" + languageCode + "&publicAllowed=" + publicAllowed);
 		response.addCookie(params);
 
 		RequestDispatcher dispatcher = this.getServletContext().getRequestDispatcher("/clog.html");
 		dispatcher.include(request, response);
 		return;
 	    } else {
 		String url = "/clog-tool/clog.html?state=" + URLEncoder.encode(state,"UTF-8") + "&siteId=" + siteId + "&placementId=" + toolId + "&postId=" + URLEncoder.encode(postId,"UTF-8") + "&language=" + languageCode + "&publicAllowed=" + publicAllowed;
 		response.sendRedirect(url);
 		return;
 	    }
 	} else {
 	    String[] parts = pathInfo.substring(1).split("/");
 
 	    if (parts.length >= 1) {
 		String part1 = parts[0];
 
 		if ("perms".equals(part1)) {
 		    doPermsGet(response);
 		}
 
 		else if ("userPerms".equals(part1)) {
 		    doUserPermsGet(response);
 		}
 	    }
 	}
     }
 
     private void doUserPermsGet(HttpServletResponse response) throws ServletException, IOException {
 	Set<String> perms = sakaiProxy.getPermissionsForCurrentUserAndSite();
 	JSONArray data = JSONArray.fromObject(perms);
 	response.setStatus(HttpServletResponse.SC_OK);
 	response.setContentType("application/json");
 	response.getWriter().write(data.toString());
 	response.getWriter().close();
 	return;
 
     }
 
     private void doPermsGet(HttpServletResponse response) throws ServletException, IOException {
 	Map<String, Set<String>> perms = sakaiProxy.getPermsForCurrentSite();
 	JSONObject data = JSONObject.fromObject(perms);
 	response.setStatus(HttpServletResponse.SC_OK);
 	response.setContentType("application/json");
 	response.getWriter().write(data.toString());
 	response.getWriter().close();
 	return;
     }
 
     protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 	logger.info("doPost()");
 
 	String pathInfo = request.getPathInfo();
 
 	String[] parts = new String[] {};
 
 	if (pathInfo != null)
 	    parts = pathInfo.substring(1).split("/");
 
 	if (parts.length >= 1) {
 	    String part1 = parts[0];
 
 	    if ("search".equals(part1))
 		doSearchPost(request, response);
 	    else if ("setPerms".equals(part1))
 		doPermsPost(request, response);
 	}
     }
 
     private void doPermsPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 	if (sakaiProxy.setPermsForCurrentSite(request.getParameterMap())) {
 	    response.setStatus(HttpServletResponse.SC_OK);
 	    response.setContentType("text/plain");
 	    response.getWriter().write("success");
 	    response.getWriter().close();
 	    return;
 	} else {
 	    response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
 	    return;
 	}
     }
 
     private void doSearchPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 	String searchTerms = request.getParameter("searchTerms");
 
 	if (searchTerms == null || searchTerms.length() == 0)
 	    throw new ServletException("No search terms supplied.");
 
 	try {
 	    List<SearchResult> results = sakaiProxy.searchInCurrentSite(searchTerms);
 	
 	    JSONArray data = JSONArray.fromObject(results);
 	    response.setStatus(HttpServletResponse.SC_OK);
 	    response.setContentType("application/json");
 	    response.getWriter().write(data.toString());
 	    response.getWriter().close();
 	    return;
 	} catch(InvalidSearchQueryException isqe) {
 	    response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
 	    response.getWriter().write("Your search terms were invalid");
 	    response.getWriter().close();
 	    return;
 	}
     }
 }
