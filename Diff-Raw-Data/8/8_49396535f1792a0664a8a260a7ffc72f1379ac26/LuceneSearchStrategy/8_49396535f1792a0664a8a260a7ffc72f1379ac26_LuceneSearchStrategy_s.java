 /*
  * cilla - Blog Management System
  *
  * Copyright (C) 2012 Richard "Shred" Körber
  *   http://cilla.shredzone.org
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Affero General Public License as published
  * by the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU Affero General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package org.shredzone.cilla.service.search.strategy;
 
 import java.io.StringReader;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.annotation.Resource;
 
 import org.apache.lucene.analysis.SimpleAnalyzer;
 import org.apache.lucene.analysis.TokenStream;
 import org.apache.lucene.queryParser.ParseException;
 import org.apache.lucene.search.Query;
 import org.apache.lucene.search.highlight.Formatter;
 import org.apache.lucene.search.highlight.Fragmenter;
 import org.apache.lucene.search.highlight.Highlighter;
 import org.apache.lucene.search.highlight.QueryScorer;
 import org.apache.lucene.util.Version;
 import org.hibernate.Criteria;
 import org.shredzone.cilla.core.model.Page;
 import org.shredzone.cilla.core.repository.SearchDao;
 import org.shredzone.cilla.core.search.PageBridge;
 import org.shredzone.cilla.service.search.FilterModel;
 import org.shredzone.cilla.service.search.impl.SearchResultImpl;
 import org.shredzone.cilla.service.search.renderer.SearchResultRenderer;
 import org.shredzone.cilla.ws.exception.CillaServiceException;
 import org.springframework.stereotype.Component;
 
 /**
  * A {@link SearchStrategy} that uses Lucene for finding. This strategy is used when a
  * query string is set in the {@link FilterModel}.
  *
  * @author Richard "Shred" Körber
  */
 @Component
 public class LuceneSearchStrategy extends AbstractSearchStrategy {
 
     private @Resource SearchDao searchDao;
     private @Resource SearchResultRenderer searchResultRenderer;
 
     @Override
     public void count(SearchResultImpl result) throws CillaServiceException {
         Query query = createQuery(result);
         Criteria crit = createCriteria(result);
 
         result.setCount(searchDao.count(query, crit));
     }
 
     @Override
     public void search(SearchResultImpl result) throws CillaServiceException {
         Query query = createQuery(result);
         Criteria crit = createPaginatedCriteria(result);
 
         List<Page> resultset = searchDao.fetch(query, crit);
         result.setPages(resultset);
         result.setHighlighted(createHighlights(query, resultset));
     }
 
     /**
      * Creates a {@link Query} for the search result.
      *
      * @param result
      *            {@link SearchResultImpl} to create a {@link Query} for
      * @return {@link Query} that was created
      */
     private Query createQuery(SearchResultImpl result) throws CillaServiceException {
         FilterModel filter = result.getFilter();
         try {
             return searchDao.parseQuery(filter.getQuery(), filter.getLocale());
         } catch (ParseException ex) {
             throw new CillaServiceException("Could not parse query '" + filter.getQuery() + "'" , ex);
         }
     }
 
     /**
      * Creates a list of highlights for a search result.
      *
      * @param pq
      *            {@link Query} that was used
      * @param result
      *            List of {@link Page} results
      * @return matching list of text extracts with highlights
      */
     private List<String> createHighlights(Query pq, List<Page> result) {
         QueryScorer scorer = new QueryScorer(pq, "text");
         Fragmenter fragmenter = searchResultRenderer.createFragmenter(scorer);
         Formatter formatter = searchResultRenderer.createFormatter();
 
         Highlighter hilighter = new Highlighter(formatter, scorer);
         hilighter.setTextFragmenter(fragmenter);
         PageBridge bridge = new PageBridge();
 
         List<String> highlighted = new ArrayList<String>(result.size());
         for (Page page : result) {
             String plain = bridge.objectToString(page);
            TokenStream tokenStream = new SimpleAnalyzer(Version.LUCENE_31).tokenStream("text", new StringReader(plain));
 
             try {
                 StringBuilder sb = new StringBuilder();
                 sb.append(searchResultRenderer.getHeader());
                 sb.append(hilighter.getBestFragments(
                                 tokenStream,
                                 plain,
                                 searchResultRenderer.getMaxResults(),
                                 searchResultRenderer.getSeparator()
                 ));
                 sb.append(searchResultRenderer.getFooter());
                 highlighted.add(sb.toString());
             } catch (Exception ex) {
                 highlighted.add(plain);
             }
         }
 
         return highlighted;
     }
 
 }
