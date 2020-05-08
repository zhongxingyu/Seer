 package net.cyklotron.cms.documents.internal;
 
 import java.util.Date;
 import java.util.Locale;
 
 import org.apache.lucene.analysis.Analyzer;
 import org.apache.lucene.document.DateTools;
 import org.apache.lucene.index.Term;
 import org.apache.lucene.queryParser.MultiFieldQueryParser;
 import org.apache.lucene.queryParser.QueryParser;
 import org.apache.lucene.search.BooleanClause;
 import org.apache.lucene.search.BooleanQuery;
 import org.apache.lucene.search.Query;
 import org.apache.lucene.search.SortField;
 import org.apache.lucene.search.TermQuery;
 import org.apache.lucene.search.TermRangeQuery;
 import org.apache.lucene.util.Version;
 import org.jcontainer.dna.Logger;
 import org.objectledge.coral.session.CoralSession;
 import org.objectledge.coral.store.Resource;
 import org.objectledge.parameters.Parameters;
 import org.objectledge.table.TableState;
 
 import net.cyklotron.cms.documents.DocumentNodeResource;
 import net.cyklotron.cms.search.SearchConstants;
 import net.cyklotron.cms.search.SearchService;
 import net.cyklotron.cms.search.SearchUtil;
 import net.cyklotron.cms.search.searching.PageableResultsSearchMethod;
 
 /**
  * Calendar search method implementation.
  *
  * @author <a href="mailto:dgajda@caltha.pl">Damian Gajda</a>
  * @version $Id: CalendarSearchMethod.java,v 1.8 2005-08-10 05:31:05 rafal Exp $
  */
 public class CalendarSearchMethod extends PageableResultsSearchMethod
 {
     private Logger log;
     private Date startDate;
     private Date endDate;
     
     private String[] fieldNames;
     private Query query;
     private String textQuery;
     
     public CalendarSearchMethod(
         SearchService searchService,
         Parameters parameters,
         Locale locale,
         Logger log,
         Date startDate,
         Date endDate)
     {
         super(searchService, parameters, locale);
         this.startDate = startDate;
         this.endDate = endDate;
         this.log = log;
     }
     
     public CalendarSearchMethod(
         SearchService searchService,
         Parameters parameters,
         Locale locale,
         Logger log,
         Date startDate,
         Date endDate, 
         String textQuery)
     {
         this(searchService, parameters, locale, log, startDate, endDate);
         this.textQuery = textQuery;
     }
 
     @Override
     public Query getQuery(CoralSession coralSession)
     throws Exception
     {
         return getQuery(coralSession, getFieldNames());
     }
 
     @Override
     public String getQueryString(CoralSession coralSession)
     {
         try
         {
             Query query = getQuery(coralSession, getFieldNames());
             return query.toString();
         }
         catch(Exception e)
         {
             return "";
         }
     }
 
     private String[] getFieldNames()
     {
         if(fieldNames == null)
         {
             fieldNames = DEFAULT_FIELD_NAMES;
             String qField = parameters.get("field","any");
             if(!qField.equals("any"))
             {
                 fieldNames = new String[1];
                 fieldNames[0] = qField;
             }
         }
         return fieldNames;
     }
 
     @Override
     public void setupTableState(TableState state)
     {
         super.setupTableState(state);
         // block page changes ??
         //state.setCurrentPage(1);
     }
 
     private Query getQuery(CoralSession coralSession, String[] fieldNames)
        throws Exception
     {
         if(query == null)
         {
             long firstCatId = parameters.getLong("category_id_1",-1);
             long secondCatId = parameters.getLong("category_id_2",-1);
             long[] categoriesIds = parameters.getLongs("categories");
 
             long[] categories = new long[categoriesIds.length+2]; 
 
             System.arraycopy(categoriesIds, 0, categories, 0, categoriesIds.length);
             categories[categoriesIds.length] = firstCatId;
             categories[categoriesIds.length+1] = secondCatId;
 
             String range = parameters.get("range","all");
             query = getQuery(coralSession, startDate, endDate, range, categories, textQuery);
         }
         return query;
     }
 
     
     private Query getQuery(CoralSession coralSession, Date startDate, Date endDate, String range, long[] categoriesIds, String textQuery)
         throws Exception
     {
         Analyzer analyzer = searchService.getAnalyzer(locale);
         BooleanQuery aQuery = new BooleanQuery();
 
         if(textQuery.length() > 0)
         {
             QueryParser parser = new MultiFieldQueryParser(Version.LUCENE_30, DEFAULT_FIELD_NAMES,
                 analyzer);
             parser.setDefaultOperator(QueryParser.AND_OPERATOR);
             parser.setDateResolution(DateTools.Resolution.SECOND);
 
             Query q = parser.parse(textQuery);
             aQuery.add(q, BooleanClause.Occur.MUST);
         }
 
         Term lowerEndDate = new Term("eventEnd", SearchUtil.dateToString(startDate));
         Term upperStartDate = new Term("eventStart", SearchUtil.dateToString(endDate));
         Term lowerStartDate = new Term("eventStart", SearchUtil.dateToString(startDate));
         Term upperEndDate = new Term("eventEnd", SearchUtil.dateToString(endDate));
 
         if(range.equals("all"))
         {
             CalendarAllRangeQuery calQuery = new CalendarAllRangeQuery(log, startDate, endDate);
             aQuery.add(new BooleanClause(calQuery, BooleanClause.Occur.MUST));
         }
         else if(range.equals("in"))
         {
             TermRangeQuery dateRange = new TermRangeQuery(lowerEndDate.field(),
                 lowerEndDate.text(), upperEndDate.text(), true, true);
             TermRangeQuery dateRange2 = new TermRangeQuery(lowerStartDate.field(), lowerStartDate
                 .text(), upperStartDate.text(), true, true);
 
             aQuery.add(new BooleanClause(dateRange, BooleanClause.Occur.MUST));
             aQuery.add(new BooleanClause(dateRange2, BooleanClause.Occur.MUST));
         }
         else if(range.equals("ending"))
         {
             TermRangeQuery dateRange = new TermRangeQuery(lowerEndDate.field(),
                 lowerEndDate.text(), upperEndDate
                 .text(), true, true);
             aQuery.add(new BooleanClause(dateRange, BooleanClause.Occur.MUST));
         }
         else if(range.equals("starting"))
         {
            TermRangeQuery dateRange2 = new TermRangeQuery(lowerEndDate.field(), lowerEndDate
                .text(), upperEndDate.text(), true, true);
             aQuery.add(new BooleanClause(dateRange2, BooleanClause.Occur.MUST));
         }
 
         for (int i = 0; i < categoriesIds.length; i++)
         {
             if(categoriesIds[i] != -1)
             {
                 Resource category = coralSession.getStore().getResource(categoriesIds[i]);
                 Query categoryQuery = getQueryForCategory(coralSession, category);
                 aQuery.add(new BooleanClause(categoryQuery, BooleanClause.Occur.MUST));
             }
         }
         aQuery.add(new BooleanClause(new TermQuery(new Term("titleCalendar",
             DocumentNodeResource.EMPTY_TITLE)), BooleanClause.Occur.MUST_NOT));
         return aQuery;
     }
 
     @Override
     public String getErrorQueryString()
     {
         return "";
     }
     
     private Query getQueryForCategory(CoralSession coralSession, Resource category)
     {
         BooleanQuery query = new BooleanQuery();
         addQueriesForCategories(coralSession, query, category);
         return query;
     }
 
     private void addQueriesForCategories(CoralSession coralSession, BooleanQuery query, Resource parentCategory)
     {
         TermQuery oneCategoryQuery = new TermQuery(new Term(SearchConstants.FIELD_CATEGORY,
             parentCategory.getPath()));
         query.add(new BooleanClause(oneCategoryQuery, BooleanClause.Occur.SHOULD));
 
         Resource[] children = coralSession.getStore().getResource(parentCategory);
         for (int i = 0; i < children.length; i++)
         {
             addQueriesForCategories(coralSession, query, children[i]);
         }
     }
 
     @Override 
     public SortField[] getSortFields()
     {
         if(parameters.isDefined("sort_field") && 
            parameters.isDefined("sort_order"))
         {
             return super.getSortFields();
         }
         else
         {
             //SortField field = new SortField("eventStart", "desc".equals("desc"));
             SortField field2 = new SortField(SearchConstants.FIELD_ID, SortField.LONG, "desc"
                 .equals("desc"));
             return new SortField[] { field2};
         }
     }
 }
