 package com.dc2f.frontent.web;
 
 import javax.servlet.ServletConfig;
 import javax.servlet.ServletRequest;
 import javax.servlet.http.HttpServletRequest;
 
 import com.dc2f.datastore.ContentRepository;
 import com.dc2f.datastore.Node;
 import com.dc2f.renderer.url.URLMapper;
 
 public class ServletURLMapper implements URLMapper {
 
 	private ContentRepository contentRepository;
 	
 	private ServletConfig servletConfig;
 	
	private String defaultProjectPath = "/cmsblog/";
 	
 	public ServletURLMapper(ContentRepository cr, ServletConfig config) {
 		contentRepository = cr;
 		servletConfig = config;
 	}
 	/**
 	 * Try to load a node for the given servlet request.
 	 * @param request - request to get the node from.
 	 * @return the node for the request if it can be found.
 	 */
 	public Node getNode(ServletRequest request) {
 		if(request instanceof HttpServletRequest) {
 			HttpServletRequest httpRequest = (HttpServletRequest) request;
 			String pathFromServlet = httpRequest.getRequestURI().toString();
 			String webappPath = httpRequest.getContextPath();
 			if (pathFromServlet.startsWith(webappPath)) {
 				pathFromServlet = pathFromServlet.substring(webappPath.length());
 			}
 			return contentRepository.getNode(defaultProjectPath + pathFromServlet);
 		}
 		return null;
 	}
 
 	@Override
 	public String getRenderURL(Node node) {
 		String servletURL = servletConfig.getServletContext().getContextPath();
 		String nodePath = node.getPath();
 		if (nodePath.startsWith(defaultProjectPath)) {
 			nodePath = nodePath.substring(defaultProjectPath.length());
 		}
 		return servletURL + nodePath;
 	}
 	
 	private String beautifyURL(String url) {
 		return url.replaceAll("/{2,}", "/");
 	}
 
 }
