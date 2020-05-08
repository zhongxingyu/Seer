 /*  
  * RestrictedQuery.java  
  *   
  * Copyright (C) 2009 LWsystems GmbH & Co. KG  
  * This program is free software; you can redistribute it and/or 
  * modify it under the terms of the GNU General Public License as 
  * published by the Free Software Foundation; either version 2 of 
  * the License, or (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,  
  * but WITHOUT ANY WARRANTY; without even the implied warranty of  
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the  
  * GNU General Public License for more details.  
  *   
  * You should have received a copy of the GNU General Public License  
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.  
  */ 
 package de.lwsystems.mailarchive.web;
 
 import de.lwsystems.mailarchive.parser.MetaDocument;
 import de.lwsystems.utils.Base64;
 import java.util.Collection;
 import java.util.Date;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import org.apache.lucene.analysis.standard.StandardAnalyzer;
 import org.apache.lucene.index.Term;
 import org.apache.lucene.misc.ChainedFilter;
 import org.apache.lucene.queryParser.MultiFieldQueryParser;
 import org.apache.lucene.queryParser.ParseException;
 import org.apache.lucene.queryParser.QueryParser;
 import org.apache.lucene.search.BooleanClause;
 import org.apache.lucene.search.BooleanQuery;
 import org.apache.lucene.search.Filter;
 import org.apache.lucene.search.FilteredQuery;
 import org.apache.lucene.search.MatchAllDocsQuery;
 import org.apache.lucene.search.Query;
 import org.apache.lucene.search.TermQuery;
 import org.apache.lucene.search.TermRangeQuery;
 import org.apache.lucene.util.Version;
 import org.springframework.security.Authentication;
 import org.springframework.security.GrantedAuthority;
 import org.springframework.security.context.SecurityContextHolder;
 
 /**
  *
  * @author rene
  */
 public class RestrictedQuery {
 
     private static String[] defaultfields = {"from", "to", "text", "title"};
     public static Query getRestrictedQuery() {
         Authentication auth = SecurityContextHolder.getContext().getAuthentication();
         BooleanQuery q = new BooleanQuery();
         for (GrantedAuthority ga : auth.getAuthorities()) {
             String str = ga.getAuthority();
             if (str.startsWith("ROLE_ADMIN") || str.startsWith("ROLE_AUDITOR")) {
                 return null;
             }
             if (str.startsWith("ROLE_MAIL_")) {
                 String email = str.substring(10).trim().toLowerCase();
                 q.add(new TermQuery(new Term("to", email)), BooleanClause.Occur.SHOULD);
                 q.add(new TermQuery(new Term("from", email)), BooleanClause.Occur.SHOULD);
             }
             if (str.startsWith("ROLE_QUERY_")) {
                 String allowedquery;
                 allowedquery = new String(Base64.decode(str.substring(11).trim()));
                 QueryParser qp = new QueryParser(Version.LUCENE_24,"text", new StandardAnalyzer(Version.LUCENE_24));
                 try {
                     q.add(qp.parse(allowedquery), BooleanClause.Occur.SHOULD);
                 } catch (ParseException ex) {
                     Logger.getLogger(SearchController.class.getName()).log(Level.SEVERE, null, ex);
                 }
             }
         }
         return q;
     }
     
     public static Query getParsedQueryWithRestrictions(String query) throws ParseException {
         return getParsedQueryWithRestrictions(query, null,null,null);
     }
     
     public static Query getParsedQueryWithRestrictions(String query,Date begin,Date end,Collection<Filter> excludefilters) throws ParseException {
                 Query querywithrestrictions;
 
         QueryParser qp = new MultiFieldQueryParser(Version.LUCENE_24,defaultfields, new StandardAnalyzer(Version.LUCENE_24));
         Query userquery;
 
             if (query.trim().equals("")) {
                 userquery = new MatchAllDocsQuery();
             } else {
                 userquery = qp.parse(query);
             }
 
             //User restrictions
             Query restquery;
             if ((restquery = getRestrictedQuery()) == null) {
                 querywithrestrictions = userquery;
             } else {
                 querywithrestrictions = new BooleanQuery();
                 ((BooleanQuery) querywithrestrictions).add(getRestrictedQuery(), BooleanClause.Occur.MUST);
                 ((BooleanQuery) querywithrestrictions).add(userquery, BooleanClause.Occur.MUST);
             }
 
             //Date restrictions
             if (begin != null || end != null) {
                 BooleanQuery daterestricted = new BooleanQuery();
                 String lowerDateString = null;
                 String upperDateString = null;
                 if (begin != null) {
                     lowerDateString = MetaDocument.getDateFormat().format(begin);
                 }
                 if (end != null) {
                     upperDateString = MetaDocument.getDateFormat().format(end);
                 }
                 TermRangeQuery rq = new TermRangeQuery("sent", lowerDateString, upperDateString, true, false);
                 daterestricted.add(rq, BooleanClause.Occur.MUST);
                 daterestricted.add(querywithrestrictions, BooleanClause.Occur.MUST);
                 querywithrestrictions = daterestricted;
             }
             //Exclude pattern (e.g. Spam)
  Logger.getLogger(SearchController.class.getName()).log(Level.SEVERE, null, excludefilters);
             if (excludefilters!=null && !excludefilters.isEmpty()) {
                 Filter[] filters=new Filter[excludefilters.size()];
                 int i=0;
                 System.out.println(excludefilters.toString());
                 for (Filter f:excludefilters) {
                     System.out.println(f);
                     filters[i++]=f;
                 }
                 querywithrestrictions=new FilteredQuery(querywithrestrictions, new ChainedFilter(filters,ChainedFilter.ANDNOT));
                 //querywithrestrictions=new FilteredQuery(querywithrestrictions, new ChainedFilter(cfa.toArray(new Filter[1]), ChainedFilter.AND));
             }
             System.out.println(querywithrestrictions.toString());
 
            return querywithrestrictions;
     }
 }
