 /*
  * Copyright (c) 2000-2004 Netspective Communications LLC. All rights reserved.
  *
  * Netspective Communications LLC ("Netspective") permits redistribution, modification and use of this file in source
  * and binary form ("The Software") under the Netspective Source License ("NSL" or "The License"). The following
  * conditions are provided as a summary of the NSL but the NSL remains the canonical license and must be accepted
  * before using The Software. Any use of The Software indicates agreement with the NSL.
  *
  * 1. Each copy or derived work of The Software must preserve the copyright notice and this notice unmodified.
  *
  * 2. Redistribution of The Software is allowed in object code form only (as Java .class files or a .jar file
  *    containing the .class files) and only as part of an application that uses The Software as part of its primary
  *    functionality. No distribution of the package is allowed as part of a software development kit, other library,
  *    or development tool without written consent of Netspective. Any modified form of The Software is bound by these
  *    same restrictions.
  *
  * 3. Redistributions of The Software in any form must include an unmodified copy of The License, normally in a plain
  *    ASCII text file unless otherwise agreed to, in writing, by Netspective.
  *
  * 4. The names "Netspective", "Axiom", "Commons", "Junxion", and "Sparx" are trademarks of Netspective and may not be
  *    used to endorse or appear in products derived from The Software without written consent of Netspective.
  *
  * THE SOFTWARE IS PROVIDED "AS IS" WITHOUT A WARRANTY OF ANY KIND. ALL EXPRESS OR IMPLIED REPRESENTATIONS AND
  * WARRANTIES, INCLUDING ANY IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR NON-INFRINGEMENT,
  * ARE HEREBY DISCLAIMED.
  *
  * NETSPECTIVE AND ITS LICENSORS SHALL NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE OR ANY THIRD PARTY AS A
  * RESULT OF USING OR DISTRIBUTING THE SOFTWARE. IN NO EVENT WILL NETSPECTIVE OR ITS LICENSORS BE LIABLE FOR ANY LOST
  * REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT, SPECIAL, CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER
  * CAUSED AND REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF THE USE OF OR INABILITY TO USE THE SOFTWARE, EVEN
  * IF IT HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
  */
 package com.netspective.sparx.navigate.fts;
 
 import java.io.IOException;
 import java.io.Writer;
 import java.net.URLEncoder;
 import java.util.HashMap;
 import java.util.Map;
 
 import javax.servlet.ServletRequest;
 
 import org.apache.lucene.index.Term;
 import org.apache.lucene.queryParser.ParseException;
 import org.apache.lucene.search.BooleanQuery;
 import org.apache.lucene.search.FuzzyQuery;
 import org.apache.lucene.search.PhraseQuery;
 import org.apache.lucene.search.PrefixQuery;
 import org.apache.lucene.search.Query;
 import org.apache.lucene.search.TermQuery;
 import org.apache.lucene.search.WildcardQuery;
 
 import com.netspective.commons.template.TemplateProcessor;
 import com.netspective.commons.text.TextUtils;
 import com.netspective.commons.xdm.XmlDataModelSchema;
 import com.netspective.sparx.navigate.NavigationContext;
 import com.netspective.sparx.template.freemarker.FreeMarkerTemplateProcessor;
 
 public class SearchHitsTemplateRenderer implements SearchHitsRenderer
 {
     public static final XmlDataModelSchema.Options XML_DATA_MODEL_SCHEMA_OPTIONS = new XmlDataModelSchema.Options().setIgnorePcData(true);
 
     private TemplateProcessor requestTemplate;
     private TemplateProcessor resultsTemplate;
     private TemplateProcessor queryErrorBodyTemplate;
     private TemplateProcessor searchErrorBodyTemplate;
     private TemplateProcessor indexTermsBodyTemplate;
     private String expressionFormFieldName = "expression";
     private String searchWithinSearchResultsFormFieldName = "searchWithinResults";
     private String requestFormFieldTemplateVarName = "formFieldName";
     private String searchWithinCheckboxFormFieldTemplateVarName = "searchWithinFieldName";
     private String rendererTemplateVarName = "renderer";
     private String searchResultsTemplateVarName = "searchResults";
     private String expressionTemplateVarName = "expression";
     private String exceptionTemplateVarName = "exception";
     private String[] hitsMatrixFieldNames;
     private boolean rewriteAdvancedAsSimpleQuery = true;
 
     public SearchHitsTemplateRenderer()
     {
         FreeMarkerTemplateProcessor renderer = (FreeMarkerTemplateProcessor) createRequestBody();
         renderer.addTemplateContent("You have not provided any search request renderer content.");
         renderer.finalizeContents();
         addRequestBody(renderer);
 
         renderer = (FreeMarkerTemplateProcessor) createResultsBody();
         renderer.addTemplateContent("You have not provided any search results renderer content.");
         renderer.finalizeContents();
         addResultsBody(renderer);
     }
 
     protected Map createDefaultTemplateVars(FullTextSearchResults searchResults)
     {
         final Map templateVars = new HashMap();
         templateVars.put(rendererTemplateVarName, this);
         templateVars.put(requestFormFieldTemplateVarName, expressionFormFieldName);
         templateVars.put(searchWithinCheckboxFormFieldTemplateVarName, searchWithinSearchResultsFormFieldName);
         if(searchResults != null)
         {
             templateVars.put(searchResultsTemplateVarName, searchResults);
             templateVars.put(expressionTemplateVarName, searchResults.getExpression().getExprText());
         }
         return templateVars;
     }
 
     public Query getAdvancedSearchQuery(NavigationContext nc)
     {
         final ServletRequest request = nc.getRequest();
         // we create a hidden field in the advanced data dialog telling us we have advanced data
         if(request.getParameter("hasAdvancedData") == null)
             return null;
 
         final FullTextSearchPage searchPage = (FullTextSearchPage) nc.getActivePage();
         final String defaultFieldName = searchPage.getDefaultAdvancedSearchFieldName();
 
         final String allWordsParamValue = request.getParameter("all");
         final String exactPhraseParamValue = request.getParameter("phrase");
         final String atLeastOneWordParamValue = request.getParameter("one");
 
         BooleanQuery advancedQuery = new BooleanQuery();
 
         if(allWordsParamValue != null && allWordsParamValue.trim().length() > 0)
         {
             Query query = getAllWordsQuery(nc, defaultFieldName, allWordsParamValue);
             advancedQuery.add(query, true, false);
         }
 
         if(exactPhraseParamValue != null && exactPhraseParamValue.trim().length() > 0)
         {
             Query query = getExactPhraseQuery(nc, defaultFieldName, exactPhraseParamValue);
             advancedQuery.add(query, true, false);
         }
 
         if(atLeastOneWordParamValue != null && atLeastOneWordParamValue.trim().length() > 0)
         {
             Query query = getAtLeastOneWordQuery(nc, defaultFieldName, atLeastOneWordParamValue);
             advancedQuery.add(query, true, false);
         }
 
         String[] advancedSearchFieldNames = searchPage.getAdvancedSearchFieldNames();
         for(int i = 0; i < advancedSearchFieldNames.length; i++)
         {
             final String fieldName = advancedSearchFieldNames[i];
             final String fieldParamValue = request.getParameter("field_" + fieldName);
             if(fieldParamValue != null && fieldParamValue.trim().length() > 0)
             {
                 final Query fieldQuery = getFieldQuery(nc, fieldName, fieldParamValue);
                 advancedQuery.add(fieldQuery, true, false);
             }
         }
 
         return advancedQuery;
     }
 
     protected Query getFieldQuery(final NavigationContext nc, final String fieldName, String fieldParamValue)
     {
         final FullTextSearchPage.FieldAttribute fieldAttribute = ((FullTextSearchPage) nc.getActivePage()).getFieldAttribute(fieldName);
         if(fieldAttribute.isUppercase())
             fieldParamValue = fieldParamValue.toUpperCase();
         else if(fieldAttribute.isLowercase())
             fieldParamValue = fieldParamValue.toLowerCase();
         final Term term = new Term(fieldName, fieldParamValue);
         final Query fieldQuery;
         if(fieldAttribute != null && fieldAttribute.isTreatAdvancedFieldExprsAsPhrases())
         {
             // This is useful so that fields get rewritten as field:"value" and then a subclassed query
             // parser can handle it using QueryParser.getFieldQuery(field, expr). The idea is that the advanced
             // query will be turned into a "normal" query using toString and sent through a query parser rewritten
             fieldQuery = new PhraseQuery();
             ((PhraseQuery) fieldQuery).add(term);
         }
         else
         {
             if(fieldParamValue.endsWith("*"))
                 fieldQuery = new PrefixQuery(new Term(fieldName, fieldParamValue.substring(0, fieldParamValue.length() - 1)));
             else if(fieldParamValue.indexOf("*") > 0 || fieldParamValue.indexOf('?') > 0)
                 fieldQuery = new WildcardQuery(term);
             else if(fieldParamValue.endsWith("~"))
                 fieldQuery = new FuzzyQuery(new Term(fieldName, fieldParamValue.substring(0, fieldParamValue.length() - 1)));
             else
                 fieldQuery = new TermQuery(term);
         }
         return fieldQuery;
     }
 
     protected Query getAtLeastOneWordQuery(final NavigationContext nc, final String defaultFieldName, final String atLeastOneWordParamValue)
     {
         String[] words = TextUtils.getInstance().split(atLeastOneWordParamValue, " ", true);
         if(words.length == 1)
             return new TermQuery(new Term(defaultFieldName, words[0]));
         else
         {
             BooleanQuery wordGroup = new BooleanQuery();
             for(int i = 0; i < words.length; i++)
                 wordGroup.add(new TermQuery(new Term(defaultFieldName, words[i])), false, false);
             return wordGroup;
         }
     }
 
     protected Query getExactPhraseQuery(final NavigationContext nc, final String defaultFieldName, final String exactPhraseParamValue)
     {
         final PhraseQuery phraseQuery = new PhraseQuery();
         final String[] words = TextUtils.getInstance().split(exactPhraseParamValue, " ", true);
         for(int i = 0; i < words.length; i++)
             phraseQuery.add(new Term(defaultFieldName, words[i]));
         return phraseQuery;
     }
 
     protected Query getAllWordsQuery(final NavigationContext nc, final String defaultFieldName, final String allWordsParamValue)
     {
         final BooleanQuery booleanQuery = new BooleanQuery();
         final String[] words = TextUtils.getInstance().split(allWordsParamValue, " ", true);
         for(int i = 0; i < words.length; i++)
             booleanQuery.add(new TermQuery(new Term(defaultFieldName, words[i])), true, false);
         return booleanQuery;
     }
 
     public SearchExpression getSearchExpression(final NavigationContext nc)
     {
         final ServletRequest request = nc.getRequest();
        final String searchWithinResultFormFieldValue = request.getParameter(searchWithinSearchResultsFormFieldName);
         final Query advancedQuery = getAdvancedSearchQuery(nc);
         final String exprText = advancedQuery != null
                                 ? advancedQuery.toString() : request.getParameter(getExpressionFormFieldName());
         final String thisSortCriteria = ((FullTextSearchPage) nc.getActivePage()).getSortCriteria(nc);
 
         return exprText == null ? null : new SearchExpression()
         {
             public boolean equals(Object obj)
             {
                 if(obj == null)
                     return false;
 
                 if(obj.getClass() != this.getClass())
                     return false;
 
                 SearchExpression se = (SearchExpression) obj;
                 if(!exprText.equals(se.getExprText()))
                     return false;
 
                 final String otherSortCriteria = se.getSortCriteria();
                 if(otherSortCriteria == null && thisSortCriteria == null)
                     return true;
 
                 if(thisSortCriteria != null && thisSortCriteria.equals(otherSortCriteria))
                     return true;
 
                 if(otherSortCriteria != null && otherSortCriteria.equals(thisSortCriteria))
                     return true;
 
                 return false;
             }
 
             public String getExprText()
             {
                 return exprText;
             }
 
             public String getExprTextURLEncoded()
             {
                 return URLEncoder.encode(exprText);
             }
 
             public boolean isEmptyExpression()
             {
                 return exprText.length() == 0;
             }
 
             public boolean isSearchWithinPreviousResults()
             {
                return searchWithinResultFormFieldValue != null;
             }
 
             public boolean isAdvancedQuery()
             {
                 return advancedQuery != null;
             }
 
             public Query getAdvancedQuery()
             {
                 return advancedQuery;
             }
 
             public String getRewrittenExpressionRedirectParams()
             {
                 return isAdvancedQuery() && isRewriteAdvancedAsSimpleQuery()
                        ? getExpressionFormFieldName() + "=" + URLEncoder.encode(exprText) : null;
             }
 
             public String getSortCriteria()
             {
                 return thisSortCriteria;
             }
         };
     }
 
     public boolean isRewriteAdvancedAsSimpleQuery()
     {
         return rewriteAdvancedAsSimpleQuery;
     }
 
     public void setRewriteAdvancedAsSimpleQuery(boolean rewriteAdvancedAsSimpleQuery)
     {
         this.rewriteAdvancedAsSimpleQuery = rewriteAdvancedAsSimpleQuery;
     }
 
     public void renderSearchRequest(Writer writer, NavigationContext nc) throws IOException
     {
         final Map templateVars = createDefaultTemplateVars(null);
         requestTemplate.process(writer, nc, templateVars);
     }
 
     public void renderEmptyQuery(Writer writer, NavigationContext nc) throws IOException
     {
         final Map templateVars = createDefaultTemplateVars(null);
         templateVars.put(expressionTemplateVarName, "");
         requestTemplate.process(writer, nc, templateVars);
     }
 
     public void renderQueryError(Writer writer, NavigationContext nc, SearchExpression expression, ParseException exception) throws IOException
     {
         final Map templateVars = createDefaultTemplateVars(null);
         templateVars.put(expressionTemplateVarName, expression);
         templateVars.put(exceptionTemplateVarName, exception);
         if(queryErrorBodyTemplate != null)
             queryErrorBodyTemplate.process(writer, nc, templateVars);
         else
             requestTemplate.process(writer, nc, templateVars);
     }
 
     public void renderSearchError(Writer writer, NavigationContext nc, SearchExpression expression, Exception exception) throws IOException
     {
         final Map templateVars = createDefaultTemplateVars(null);
         templateVars.put(expressionTemplateVarName, expression);
         templateVars.put(exceptionTemplateVarName, exception);
         if(searchErrorBodyTemplate != null)
             searchErrorBodyTemplate.process(writer, nc, templateVars);
         else if(queryErrorBodyTemplate != null)
             queryErrorBodyTemplate.process(writer, nc, templateVars);
         else
             requestTemplate.process(writer, nc, templateVars);
     }
 
     public void renderSearchResults(Writer writer, NavigationContext nc, FullTextSearchResults searchResults) throws IOException
     {
         final Map templateVars = createDefaultTemplateVars(searchResults);
         resultsTemplate.process(writer, nc, templateVars);
     }
 
     public void renderTerms(Writer writer, NavigationContext nc, Map termsByFieldsMap) throws IOException
     {
         Map templateVars = new HashMap();
         templateVars.put(rendererTemplateVarName, this);
         templateVars.put("termsByFieldsMap", termsByFieldsMap);
         indexTermsBodyTemplate.process(writer, nc, templateVars);
     }
 
     public String getExpressionFormFieldName()
     {
         return expressionFormFieldName;
     }
 
     public void setExpressionFormFieldName(String expressionFormFieldName)
     {
         this.expressionFormFieldName = expressionFormFieldName;
     }
 
     public TemplateProcessor createRequestBody()
     {
         return new FreeMarkerTemplateProcessor();
     }
 
     public void addRequestBody(TemplateProcessor templateProcessor)
     {
         requestTemplate = templateProcessor;
     }
 
     public TemplateProcessor getRequestBody()
     {
         return requestTemplate;
     }
 
     public TemplateProcessor createResultsBody()
     {
         return new FreeMarkerTemplateProcessor();
     }
 
     public void addResultsBody(TemplateProcessor templateProcessor)
     {
         resultsTemplate = templateProcessor;
     }
 
     public TemplateProcessor getResultsBody()
     {
         return resultsTemplate;
     }
 
     public TemplateProcessor createQueryErrorBody()
     {
         return new FreeMarkerTemplateProcessor();
     }
 
     public void addQueryErrorBody(TemplateProcessor templateProcessor)
     {
         queryErrorBodyTemplate = templateProcessor;
     }
 
     public TemplateProcessor getQueryErrorBody()
     {
         return queryErrorBodyTemplate;
     }
 
     public TemplateProcessor createSearchErrorBody()
     {
         return new FreeMarkerTemplateProcessor();
     }
 
     public void addSearchErrorBody(TemplateProcessor templateProcessor)
     {
         searchErrorBodyTemplate = templateProcessor;
     }
 
     public TemplateProcessor getSearchErrorBody()
     {
         return searchErrorBodyTemplate;
     }
 
     public TemplateProcessor createIndexTermsBody()
     {
         return new FreeMarkerTemplateProcessor();
     }
 
     public void addIndexTermsBody(TemplateProcessor templateProcessor)
     {
         indexTermsBodyTemplate = templateProcessor;
     }
 
     public TemplateProcessor getIndexTermsBody()
     {
         return indexTermsBodyTemplate;
     }
 
     public String[] getHitsMatrixFieldNames()
     {
         return hitsMatrixFieldNames;
     }
 
     public void setHitsMatrixFieldNames(String[] hitsMatrixFieldNames)
     {
         this.hitsMatrixFieldNames = hitsMatrixFieldNames;
     }
 
     public String getExpressionTemplateVarName()
     {
         return expressionTemplateVarName;
     }
 
     public void setExpressionTemplateVarName(String expressionTemplateVarName)
     {
         this.expressionTemplateVarName = expressionTemplateVarName;
     }
 
     public String getExceptionTemplateVarName()
     {
         return exceptionTemplateVarName;
     }
 
     public void setExceptionTemplateVarName(String exceptionTemplateVarName)
     {
         this.exceptionTemplateVarName = exceptionTemplateVarName;
     }
 }
