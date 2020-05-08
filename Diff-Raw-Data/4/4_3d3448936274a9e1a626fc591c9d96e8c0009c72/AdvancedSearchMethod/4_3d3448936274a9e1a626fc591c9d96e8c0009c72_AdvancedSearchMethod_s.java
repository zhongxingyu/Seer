 package net.cyklotron.cms.search.searching;
 
 import java.util.Date;
 import java.util.Locale;
 
 import org.apache.lucene.analysis.Analyzer;
 import org.apache.lucene.document.DateTools;
 import org.apache.lucene.index.Term;
 import org.apache.lucene.queries.TermsFilter;
 import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
 import org.apache.lucene.queryparser.classic.QueryParser;
 import org.apache.lucene.search.BooleanClause;
 import org.apache.lucene.search.BooleanQuery;
 import org.apache.lucene.search.FilteredQuery;
 import org.apache.lucene.search.MatchAllDocsQuery;
 import org.apache.lucene.search.Query;
 import org.apache.lucene.search.TermRangeQuery;
 import org.apache.lucene.util.BytesRef;
 import org.objectledge.coral.session.CoralSession;
 import org.objectledge.parameters.Parameters;
 import org.objectledge.templating.TemplatingContext;
 
 import bak.pcj.LongIterator;
 import bak.pcj.set.LongSet;
 import net.cyklotron.cms.search.SearchConstants;
 import net.cyklotron.cms.search.SearchService;
 import net.cyklotron.cms.search.SearchUtil;
 
 /**
  * Advanced search method implementation.
  *
  * @author <a href="mailto:dgajda@caltha.pl">Damian Gajda</a>
  * @version $Id: AdvancedSearchMethod.java,v 1.8 2007-01-31 00:18:44 rafal Exp $
  */
 public class AdvancedSearchMethod extends PageableResultsSearchMethod
 {
     /**
      * Maximum number of clauses in a query. Date range queries use quite a few.
      */
     private static final int MAX_CALUSE_COUNT = 10240;
     
     private LongSet docIds;
 
     public AdvancedSearchMethod(
         SearchService searchService,
         Parameters parameters,
         Locale locale)
     {
         super(searchService, parameters, locale);
         docIds = null;
     }
     
     public LongSet getDocIds()
     {
         return docIds;
     }
 
     public void setDocIds(LongSet docIds)
     {
         this.docIds = docIds;
     }
 
     @Override
     public Query getQuery(CoralSession coralSession)
     throws Exception
     {
         String[] fieldNames;
         
         String qField = parameters.get("field","standard");
         if("standard".equals(qField))
         {
             fieldNames = DEFAULT_FIELD_NAMES; 
         }
         else if("extended".equals(qField))
         {
             fieldNames = EXTENDED_FIELD_NAMES;
         }
         else
         {
             fieldNames = new String[] { qField };
         }
 
         return getQuery(fieldNames);
     }
     
     @Override
     public String getQueryString(CoralSession coralSession)
     {
         String[] fieldNames = new String[1];
         fieldNames[0] = "";
         String qField = parameters.get("field","any");
         if(!qField.equals("any"))
         {
             fieldNames = new String[1];
             fieldNames[0] = qField;
         }
         try
         {
             Query query = getQuery(fieldNames);
             return query.toString();
         }
         catch(Exception e)
         {
             return "";
         }
     }
 
     private Query getQuery(String[] fieldNames)
     throws Exception
     {
         Analyzer analyzer = searchService.getAnalyzer(locale);
         BooleanQuery.setMaxClauseCount(MAX_CALUSE_COUNT);
         BooleanQuery aQuery = new BooleanQuery();
 
         String qAnd = parameters.get("q_and","");
         if(qAnd.length() > 0)
         {
             for(String fieldName : fieldNames)
             {
                 QueryParser parser = new QueryParser(SearchConstants.LUCENE_VERSION, fieldName, analyzer);
                 parser.setDateResolution(DateTools.Resolution.SECOND);
                 Query q = parser.parse(qAnd);
                 makeAllRequired(q);
                 aQuery.add(q, BooleanClause.Occur.SHOULD);
             }
         }
 
         QueryParser parser = new MultiFieldQueryParser(SearchConstants.LUCENE_VERSION, fieldNames, analyzer);
         parser.setDateResolution(DateTools.Resolution.SECOND);
         String qExpr = parameters.get("q_expr","");
         if(qExpr.length() > 0)
         {
             aQuery.add(parser.parse("\""+qExpr+"\""), BooleanClause.Occur.MUST);
         }
 
         String qOr = parameters.get("q_or","");
         if(qOr.length() > 0)
         {
             aQuery.add(parser.parse(qOr), BooleanClause.Occur.MUST);
         }
         
         String qNot = parameters.get("q_not","");
         if(qNot.length() > 0)
         {
             aQuery.add(parser.parse(qNot), BooleanClause.Occur.MUST_NOT);
         }
         
         String q_org = parameters.get("q_org","");
         if(q_org.length() > 0)
         {
             QueryParser orgParser = new QueryParser(SearchConstants.LUCENE_VERSION,
                 SearchConstants.FIELD_ORGANIZATION_NAME, analyzer);
             orgParser.setDateResolution(DateTools.Resolution.SECOND);
             aQuery.add(orgParser.parse("\""+q_org+"\""), BooleanClause.Occur.MUST);
         }
         
         BooleanQuery outQuery = new BooleanQuery();
         if(aQuery.clauses().size() > 0) {
             outQuery.add(aQuery, BooleanClause.Occur.MUST);
         }
         
         String qTime = parameters.get("q_time","all");
         BooleanClause clause = getDateRangeClause(SearchConstants.FIELD_MODIFICATION_TIME, qTime);
         if(clause != null)
         {
             outQuery.add(clause);
         }
 
         String vTime = parameters.get("v_time","all");
         clause = getDateRangeClause("validityStart", vTime);
         if(clause != null)
         {
             outQuery.add(clause);
         }
 
         String fiendTime = parameters.get("f_time", "");
         String startTime = parameters.get("s_time", "");
         String endTime = parameters.get("e_time", "");
         clause = getDateRangeClause(fiendTime, startTime, endTime);
         if(clause != null)
         {
             outQuery.add(clause);
         }
 
         clause = getDocIdsFilterQuery(docIds);
         if(clause != null)
         {
             outQuery.add(clause);
         }
 
         return outQuery;
     }
 
     private void makeAllRequired(Query query)
     {
         if(query instanceof BooleanQuery)
         {
             BooleanQuery bQuery = (BooleanQuery)query;
             for(BooleanClause clause : bQuery.getClauses())
             {
                 clause.setOccur(BooleanClause.Occur.MUST);
                 makeAllRequired(clause.getQuery());
             }
         }
     }
 
     private BooleanClause getDateRangeClause(String fieldName, String paramValue)
     {
         BooleanClause clause = null;
         if(!paramValue.equals("all"))
         {
             long days = Long.parseLong(paramValue);
             Date date = new Date(System.currentTimeMillis() - (days * 1000L * 60L * 60L * 24L));
             Term lowerDate = new Term(fieldName, SearchUtil.dateToString(date));
             
             TermRangeQuery dateRange = TermRangeQuery.newStringRange(fieldName, lowerDate.text(),
                 null, true, false);
             clause = new BooleanClause(dateRange, BooleanClause.Occur.MUST);
         }
         return clause;
     }
     
     private BooleanClause getDateRangeClause(String fieldName, String startTime, String endTime)
     {
         BooleanClause clause = null;
         if((SearchConstants.FIELD_EVENT_END.equals(fieldName)
             || SearchConstants.FIELD_EVENT_START.equals(fieldName) || SearchConstants.FIELD_MODIFICATION_TIME
                 .equals(fieldName)) && !(startTime.isEmpty() && endTime.isEmpty()))
         {
             String lowerDateToText = null;
             String upperDateToText = null;
             if(!startTime.isEmpty())
             {
                 Date startDate = new Date(Long.parseLong(startTime));
                 Term lowerDate = new Term(fieldName, SearchUtil.dateToString(startDate));
                 lowerDateToText = lowerDate.text();
             }
             if(!endTime.isEmpty())
             {
                 Date endDate = new Date(Long.parseLong(endTime));
                 Term upperDate = new Term(fieldName, SearchUtil.dateToString(endDate));
                 upperDateToText = upperDate.text();
             }
 
             TermRangeQuery dateRange = TermRangeQuery.newStringRange(fieldName, lowerDateToText,
                 upperDateToText, lowerDateToText != null, upperDateToText != null);
             clause = new BooleanClause(dateRange, BooleanClause.Occur.MUST);
         }
         return clause;
     }
     
     private BooleanClause getDocIdsFilterQuery(LongSet docIds)
     {
         BooleanClause clause = null;
         if(docIds != null)
         {
            BytesRef[] terms = new BytesRef[docIds.size()];
             LongIterator id = docIds.iterator();
             int i = 0;
             while(id.hasNext())
             {
                 terms[i++] = new BytesRef(((Long)id.next()).toString().getBytes());
             }
             TermsFilter tf = new TermsFilter(SearchConstants.FIELD_ID, terms);
             FilteredQuery filteredQuery = new FilteredQuery(new MatchAllDocsQuery(), tf);
             clause = new BooleanClause(filteredQuery, BooleanClause.Occur.MUST);
         }
         return clause;
     }
     
     @Override
     public String getErrorQueryString()
     {
         return "";
     }
     
     public void storeQueryParameters(TemplatingContext templatingContext)
     {
         super.storeQueryParameters(templatingContext);
         storeQueryParameter("field", templatingContext);
         storeQueryParameter("q_and", templatingContext);
         storeQueryParameter("q_expr", templatingContext);
         storeQueryParameter("q_or", templatingContext);
         storeQueryParameter("q_not", templatingContext);
         storeQueryParameter("q_org", templatingContext);
         storeQueryParameter("q_time", templatingContext);
         storeQueryParameter("v_time", templatingContext);
         storeQueryParameter("f_time", templatingContext);
         storeQueryParameter("s_time", templatingContext);
         storeQueryParameter("e_time", templatingContext);
     }
 }
