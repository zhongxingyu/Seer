 package it.polimi.chansonnier.servlet;
 
 import java.io.IOException;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.eclipse.smila.lucene.LuceneSearchService;
 import org.eclipse.smila.processing.parameters.SearchParameters;
import org.eclipse.smila.search.api.helper.QueryBuilder;
 import org.eclipse.smila.datamodel.record.Annotation;
import org.eclipse.smila.datamodel.record.Attribute;
import org.eclipse.smila.datamodel.record.Literal;
 import org.eclipse.smila.datamodel.record.Record;
import org.eclipse.smila.datamodel.record.RecordFactory;
import org.eclipse.smila.datamodel.record.impl.AttributeImpl;
import org.eclipse.smila.datamodel.record.impl.LiteralImpl;;
 
 public class SearchServlet extends HttpServlet {
 	public static final String DEFAULT_PIPELINE = "SearchPipeline";
 	
 	public void doGet(HttpServletRequest request, HttpServletResponse response) 
 		throws ServletException, IOException {
 	    String lyrics = request.getParameter("lyrics");
 	    if (lyrics != null) {
 	    	Record queryRecord = RecordFactory.DEFAULT_INSTANCE.createRecord();
 	    	queryRecord.setMetadata(RecordFactory.DEFAULT_INSTANCE.createMetadataObject());
 	        final Annotation annotation = RecordFactory.DEFAULT_INSTANCE.createAnnotation();
 	        queryRecord.getMetadata().addAnnotation(SearchParameters.PARAMETERS, annotation);
 	        annotation.setNamedValue(SearchParameters.QUERY, lyrics);
 	        annotation.setNamedValue(LuceneSearchService.SEARCH_ANNOTATION_QUERY_ATTRIBUTE, "Lyrics");
 	    	try {
 				String result = Activator.getSearchService().searchAsXmlString(DEFAULT_PIPELINE, queryRecord);
 		        response.setContentType("text/html;charset=UTF-8");
 				response.getWriter().write(result);
 			} catch (ParserConfigurationException e) {
 				// TODO Auto-generated catch block
 				throw new ServletException(e);
 			}
 	    }
 	    
 	}
 }
