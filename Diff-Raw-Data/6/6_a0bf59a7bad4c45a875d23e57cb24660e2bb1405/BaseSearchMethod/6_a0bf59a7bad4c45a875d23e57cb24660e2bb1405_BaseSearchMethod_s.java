 package net.cyklotron.cms.search.searching;
 
 import java.util.Locale;
 
 import org.apache.lucene.search.Query;
 import org.apache.lucene.search.SortField;
 import org.objectledge.coral.session.CoralSession;
 import org.objectledge.parameters.Parameters;
 import org.objectledge.table.TableConstants;
 import org.objectledge.table.TableState;
 import org.objectledge.templating.TemplatingContext;
 
 import net.cyklotron.cms.search.SearchConstants;
 import net.cyklotron.cms.search.SearchService;
 
 /**
  * Base search method implementation.
  *
  * @author <a href="mailto:dgajda@caltha.pl">Damian Gajda</a>
  * @version $Id: BaseSearchMethod.java,v 1.4 2005-05-30 00:18:51 zwierzem Exp $
  */
 public abstract class BaseSearchMethod implements SearchMethod
 {
     public static final String[] DEFAULT_FIELD_NAMES = { SearchConstants.FIELD_INDEX_TITLE,
                             SearchConstants.FIELD_INDEX_ABBREVIATION,
                             SearchConstants.FIELD_INDEX_CONTENT };
     
     public static final String[] EXTENDED_FIELD_NAMES = { SearchConstants.FIELD_INDEX_TITLE,
                     SearchConstants.FIELD_INDEX_ABBREVIATION, SearchConstants.FIELD_INDEX_CONTENT,
                     "keywords", "titleCalendar", "authors", "sources",
                     SearchConstants.FIELD_ORGANIZATION_NAME };
     
     protected SearchService searchService;
     protected Parameters parameters;
     protected Locale locale;
                            
     public BaseSearchMethod(
         SearchService searchService,
         Parameters parameters,
         Locale locale)
     {
         this.searchService = searchService;
         this.parameters = parameters;
         this.locale = locale;
     }
                             
     public abstract Query getQuery(CoralSession coralSession)
     throws Exception;
     
     public abstract String getQueryString(CoralSession coralSession);
     
     public abstract String getErrorQueryString();
     
     public void setupTableState(TableState state)
     {
         if(state.isNew())
         {
             state.setRootId(null);
             state.setTreeView(false);
             state.setPageSize(10);
         }
 
         // WARN: duplicate setPage action
         if(parameters.isDefined(TableConstants.TABLE_ID_PARAM_KEY) &&
            parameters.getInt(TableConstants.TABLE_ID_PARAM_KEY) == state.getId())
         {
             state.setCurrentPage( parameters.getInt(TableConstants.PAGE_NO_PARAM_KEY,1) );
         }
         else
         {
             state.setCurrentPage(1);
         }
     }
     
     public SortField[] getSortFields()
     {
         String sortField = parameters.get("sort_field","score");
         String sortOrder = parameters.get("sort_order","desc");
         if(!sortField.equals("score"))
         {
            SortField field = new SortField(sortField, SortField.STRING, sortOrder.equals("desc"));
             return new SortField[] { field };
         }
         else if(sortOrder.equals("asc"))
         {
            SortField field = new SortField((String)null, SortField.SCORE, true);
             return new SortField[] { field };
         }
         return null;
     }
     
     protected void storeQueryParameter(String parameterName, TemplatingContext templatingContext)
     {
         String parameterValue = parameters.get(parameterName, null);
         if(parameterValue != null)
         {
             templatingContext.put(parameterName, parameterValue);
         }
     }
     
     public void storeQueryParameters(TemplatingContext templatingContext)
     {
         storeQueryParameter("sort_field", templatingContext);
         storeQueryParameter("sort_order", templatingContext);
     }
 }
