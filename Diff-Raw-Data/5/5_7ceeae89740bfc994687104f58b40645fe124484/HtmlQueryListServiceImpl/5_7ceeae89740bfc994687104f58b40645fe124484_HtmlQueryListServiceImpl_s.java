 package com.abudko.reseller.huuto.query.service.list.html;
 
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.util.Collection;
 import java.util.LinkedHashSet;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Qualifier;
 import org.springframework.stereotype.Component;
 import org.springframework.web.client.RestTemplate;
 
 import com.abudko.reseller.huuto.query.service.list.AbstractQueryListService;
 import com.abudko.reseller.huuto.query.service.list.ListResponse;
 
 @Component
 public class HtmlQueryListServiceImpl extends AbstractQueryListService {
     
     private static final int MAX_ITEMS_ON_PAGE = 50;
     
     private static final String PAGE_PARAM = "/page/";
 
     @Autowired
     private HtmlListParser htmlListParser;
 
     @Autowired
     @Qualifier("restTemplateHtml")
     private RestTemplate restTemplate;
 
     @Override
     public Collection<ListResponse> callAndParse(String query) throws URISyntaxException {
         Collection<ListResponse> queryAllResponses = new LinkedHashSet<ListResponse>();
         boolean morePages = false;
         
         int page = 1;
         do {
             URI pagedURI = getPagedURI(query, page++);
             String responseList = restTemplate.getForObject(pagedURI, String.class);
             Collection<ListResponse> queryPageResponses = htmlListParser.parse(responseList);            
             queryAllResponses.addAll(queryPageResponses);
             
            morePages = morePages(queryPageResponses, page);
         }
         while (morePages);
 
         return queryAllResponses;
     }
     
     private URI getPagedURI(String query, int page) throws URISyntaxException {
         StringBuilder sb = new StringBuilder(query);
         sb.append(PAGE_PARAM);
         sb.append(page);
         URI uri = new URI(sb.toString());
         
         return uri;
     }
     
    private boolean morePages(Collection<ListResponse> queryPageResponses, int page) {
         if (queryPageResponses.size() < MAX_ITEMS_ON_PAGE) {
             return false;
         }
         
         return true;
     }
 }
