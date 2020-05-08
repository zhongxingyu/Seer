 package net.cyklotron.cms.search.searching;
 
 import java.util.Locale;
 
 import org.apache.lucene.analysis.Analyzer;
 import org.apache.lucene.document.DateTools;
import org.apache.lucene.queryParser.MultiFieldQueryParser;
import org.apache.lucene.queryParser.QueryParser;
 import org.apache.lucene.search.Query;
 import org.apache.lucene.util.Version;
 import org.objectledge.coral.session.CoralSession;
 import org.objectledge.parameters.Parameters;
 import org.objectledge.templating.TemplatingContext;
 
 import net.cyklotron.cms.search.SearchService;
 
 /**
  * Simple search method implementation.
  *
  * @author <a href="mailto:dgajda@caltha.pl">Damian Gajda</a>
  * @version $Id: SimpleSearchMethod.java,v 1.4 2007-01-30 23:55:02 rafal Exp $
  */
 public class SimpleSearchMethod extends BaseSearchMethod
 {
     protected String query;
     
     public SimpleSearchMethod(
         SearchService searchService,
         Parameters parameters,
         Locale locale)
     {
         super(searchService, parameters, locale);
     }
                             
     @Override
     public Query getQuery(CoralSession coralSession)
     throws Exception
     {
         query = parameters.get("query","");
         if(query.length() > 0)
         {
             Analyzer analyzer = searchService.getAnalyzer(locale);
            QueryParser parser = new MultiFieldQueryParser(Version.LUCENE_30, DEFAULT_FIELD_NAMES,
                 analyzer);
             parser.setDateResolution(DateTools.Resolution.SECOND);
             return parser.parse(query);
         }
         else
         {
             return null;
         }
     }
     
     @Override
     public String getQueryString(CoralSession coralSessio)
     {
         return query;
     }
     
     @Override
     public String getErrorQueryString()
     {
         return query;
     }
     
     public void storeQueryParameters(TemplatingContext templatingContext)
     {
         super.storeQueryParameters(templatingContext);
         storeQueryParameter("query", templatingContext);
     }
 }
