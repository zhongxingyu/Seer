 package eu.vilaca.services;
 
 import java.io.IOException;
 import java.net.URI;
 import java.util.Collections;
 import java.util.Map;
 import java.util.Properties;
 
 import com.sun.net.httpserver.HttpExchange;
 import com.sun.net.httpserver.HttpHandler;
 
 import eu.vilaca.pagelets.PageLet;
 import eu.vilaca.pagelets.RedirectPageLet;
 
 class RequestHandler implements HttpHandler {
 
 	private final Map<String, PageLet> resources;
 	private final String version;
 
 	/**
 	 * private c'tor to avoid external instantiation
 	 * 
 	 * @param properties
 	 * 
 	 * @param resources
 	 *            mapping of URI to static content
 	 */
 	RequestHandler(final Map<String, PageLet> pages, Properties properties) {
 
 		// server will not at any case modify this structure
 		this.resources = Collections.unmodifiableMap(pages);
 		this.version = properties.getProperty("server.version", "unversioned");
 	}
	
 
 	/**
 	 * Handle request, parse URI filename from request into page resource
 	 * 
 	 * @param
 	 * 
 	 * @exception IOException
 	 */
 	@Override
 	public void handle(final HttpExchange exchange) throws IOException {
 
 		final PageLet resource = getPageContents(exchange);
 
 		resource.execute(exchange);
 
		exchange.getResponseHeaders().set("Server", version);
 		
 		Server.printLogMessage(exchange, resource.getResponseCode());
 	}
 
 	/**
 	 * Resolve URI to correct page/resource or use 404
 	 * 
 	 * @param exchange
 	 *            .getRequestURI()
 	 * @return
 	 */
 	private PageLet getPageContents(final HttpExchange exchange) {
 
 		final String filename = getRequestedFilename(exchange.getRequestURI());
 
 		if (filename.length() == 6) {
 			return new RedirectPageLet();
 		}
 
 		final PageLet page = resources.get(filename);
 		
 		return page != null ? page : resources.get("404");
 	}
 
 	/**
 	 * Parse requested filename from URI
 	 * 
 	 * @param request
 	 * 
 	 * @return Requested filename
 	 */
 	private String getRequestedFilename(final URI request) {
 
 		// split into tokens
 
 		final String[] tokens = request.getRawPath().split("/");
 
 		if (tokens.length > 0) {
 			return tokens[1];
 		}
 
 		// empty URI (no tokens) means front page
 
 		return "/";
 	}
 
 }
