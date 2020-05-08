 package edu.gwu.raminfar.wiki;
 
 
 import org.jsoup.Jsoup;
 import org.jsoup.nodes.Document;
 import org.jsoup.nodes.Element;
 import org.jsoup.select.Elements;
 
 import java.io.IOException;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.LinkedHashSet;
 import java.util.Set;
 
 /**
  * Author: Amir Raminfar
  * Date: Oct 21, 2010
  */
 public class WikiSearch {
     private final static String SEARCH_URL = "http://en.wikipedia.org/w/index.php?search=%s&fulltext=Search";
     private final String query;
 
 
     public WikiSearch(String query) {
         this.query = query.replaceAll(" ", "+").replaceAll("&", "%26");
     }
 
     public Collection<WikiPage> parseResults() throws IOException {
         return parseResults(10);
     }
 
    public Set<WikiPage> parseResults(int max) throws IOException {
         Set<WikiPage> results = new LinkedHashSet<WikiPage>();
 
         String url = String.format(SEARCH_URL, query);
         Document doc = Jsoup.connect(url).get();
         Elements elements = doc.select("ul.mw-search-results a");
        for (int i = 0, elementsSize = elements.size(); i < elementsSize && results.size() < max; i++) {
             Element e = elements.get(i);
             WikiPage page = new WikiPage(e.absUrl("href"));
             if (!results.contains(page)) {
                 results.add(page);
             }            
         }
 
         return results;
     }
 
     public String getQuery() {
         return query;
     }
 }
