 package com.threewks.analytics.filter;
 
 import java.io.IOException;
 import java.io.OutputStream;
 import java.io.OutputStreamWriter;
 import java.io.PrintWriter;
 import java.net.HttpURLConnection;
 import java.net.URL;
 import java.util.logging.Logger;
 
 import javax.servlet.Filter;
 import javax.servlet.FilterChain;
 import javax.servlet.FilterConfig;
 import javax.servlet.ServletException;
 import javax.servlet.ServletRequest;
 import javax.servlet.ServletResponse;
 import javax.servlet.http.HttpServletRequest;
 
 import org.apache.commons.io.IOUtils;
 import org.apache.commons.lang3.StringUtils;
 
 import com.google.gson.Gson;
 import com.google.gson.GsonBuilder;
 import com.threewks.analytics.model.HttpRequest;
 
 /**
  * <p>
  * Servlet filter for capturing HTTP request data and posting it to thundr-analytics. To use this filter add the following to your web.xml
  * <p>
  * 
  * <pre>
  * &lt;filter&gt;
  *     &lt;filter-name&gt;HttpRequestCaptureFilter&lt;/filter-name&gt;
  *     &lt;filter-class&gt;com.threewks.analytics.filter.HttpRequestCaptureFilter&lt;/filter-class&gt;
  *     &lt;init-param&gt;
  *         &lt;param-name&gt;endpoint&lt;/param-name&gt;
  *         &lt;param-value&gt;http://analytics.3wks.com/request&lt;/param-value&gt;
  *     &lt;/init-param&gt;
  * &lt;/filter&gt;
  * &lt;filter-mapping&gt;
  *     &lt;filter-name&gt;HttpRequestCaptureFilter&lt;/filter-name&gt;
  *     &lt;url-pattern&gt;/*&lt;/url-pattern&gt;
  * &lt;/filter-mapping&gt;
  * </pre>
  */
 public class HttpRequestCaptureFilter implements Filter {
 
 	private static final Logger logger = Logger.getLogger("3wks-analytics");
 
 	private static final String APPLICATION_JSON = "application/json";
 	private static final String UTF8 = "UTF-8";
 
 	private Gson gson;
 	private String endpoint;
 
 	@Override
 	public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
 
 		try {
 			postJson(endpoint, gson.toJson(new HttpRequest((HttpServletRequest) req)));
 		} catch (Throwable t) {
 			logger.severe(String.format("Error posting request data to %s, error: %s", endpoint, t.getMessage()));
 		}
 
 		// carry on regardless
 		chain.doFilter(req, res);
 	}
 
 	@Override
 	public void init(FilterConfig config) throws ServletException {
 		this.endpoint = config.getInitParameter("endpoint");
 		if (StringUtils.isBlank(endpoint)) {
 			throw new ServletException(String.format("Missing mandatory init param 'endpoint' for %s", this.getClass().getCanonicalName()));
 		}
 		this.gson = new GsonBuilder().create();
 	}
 
 	@Override
 	public void destroy() {
 	}
 
 	/**
 	 * Post JSON to the given endpoint.
 	 * 
 	 * @param url the URL to post the data to.
 	 * @param json the JSON data to post.
 	 * @throws Exception if anything goes wrong.
 	 */
 	private static void postJson(String url, String json) throws Exception {
 		HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
 		connection.setConnectTimeout(5000);
 		connection.setReadTimeout(5000);
 		connection.setDoOutput(true);
 		connection.setRequestProperty("Content-Type", APPLICATION_JSON);
 		PrintWriter writer = null;
 		try {
 			OutputStream output = connection.getOutputStream();
 			writer = new PrintWriter(new OutputStreamWriter(output, UTF8), true);
 			writer.append(json);
 		} finally {
 			IOUtils.closeQuietly(writer);
 		}
 	}
 
 }
