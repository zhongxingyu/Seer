 package nz.gen.wellington.guardian.contentapiproxy.servlets;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.Map;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import nz.gen.wellington.guardian.contentapiproxy.datasources.GuardianDataSource;
 import nz.gen.wellington.guardian.contentapiproxy.datasources.rss.RssDataSource;
 import nz.gen.wellington.guardian.contentapiproxy.model.Section;
 
 import org.apache.commons.codec.digest.DigestUtils;
 import org.apache.log4j.Logger;
 
 import com.google.inject.Inject;
 import com.google.inject.Singleton;
 
 @SuppressWarnings("serial")
 @Singleton
 public class SectionProxyServlet extends CacheAwareProxyServlet {
 
 	Logger log = Logger.getLogger(SectionProxyServlet.class);
 
 	private GuardianDataSource dataSource;
 	private SectionsToJSONRenderer sectionsToJSONRenderer;
 
 	
 	@Inject
 	public SectionProxyServlet(RssDataSource dataSource, SectionsToJSONRenderer sectionsToJSONRenderer) {
 		super();
 		this.dataSource = dataSource;
 		this.sectionsToJSONRenderer = sectionsToJSONRenderer;
 	}
 	
 	
 	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		log.info("Handling request for path: " + request.getRequestURI());
 		response.setContentType("text/plain");
 		response.setCharacterEncoding("UTF-8");
 		
 		if (request.getRequestURI().equals("/sections")) {
 			
 			final String queryCacheKey = "sections";
			String output = cacheGet(queryCacheKey);
			if (output != null) {
 	        	log.info("Returning cached results for call url: " + queryCacheKey);
			}
					
 
			Map<String, Section> sections = dataSource.getSections();
			if (sections != null) {
 				
				String content = sectionsToJSONRenderer.outputJSON(sections);				
				cacheContent(queryCacheKey, content);				
 				response.setStatus(HttpServletResponse.SC_OK);
				response.addHeader("Etag", DigestUtils.md5Hex(output));
 				PrintWriter writer = response.getWriter();
 				writer.print(content);
 				writer.flush();
 				return;
			}			
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return;			
 		}
 		
 		response.setStatus(HttpServletResponse.SC_NOT_FOUND);
 		return;
 	}
 
 }
