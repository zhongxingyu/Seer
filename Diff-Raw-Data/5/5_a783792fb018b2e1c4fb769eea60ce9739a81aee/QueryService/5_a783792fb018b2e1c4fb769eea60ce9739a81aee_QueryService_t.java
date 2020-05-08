 package com.abudko.reseller.huuto.query.service;
 
 import java.io.UnsupportedEncodingException;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.util.List;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Component;
 import org.springframework.web.client.RestTemplate;
 
 import com.abudko.reseller.huuto.query.QueryBuilder;
 import com.abudko.reseller.huuto.query.QueryParams;
 import com.abudko.reseller.huuto.query.html.item.HtmlItemParser;
 import com.abudko.reseller.huuto.query.html.item.ItemResponse;
 import com.abudko.reseller.huuto.query.html.list.HtmlListParser;
 import com.abudko.reseller.huuto.query.html.list.QueryListResponse;
 import com.abudko.reseller.huuto.query.mapper.ParamMapper;
 import com.abudko.reseller.huuto.query.notification.ResponseProcessor;
 
 @Component
 public class QueryService {
 
     private Log log = LogFactory.getLog(getClass());
 
     @Autowired
     private ParamMapper queryParamMapper;
 
     @Autowired
     private QueryBuilder queryBuilder;
 
     @Autowired
     private HtmlListParser htmlListParser;
 
     @Autowired
     private HtmlItemParser htmlItemParser;
 
     @Autowired
     private ResponseProcessor emailSender;
 
     private RestTemplate restTemplate = new RestTemplate();
 
     public void scan() throws UnsupportedEncodingException, URISyntaxException {
 
         List<QueryParams> queryParamsList = queryParamMapper.getQueryParams();
 
         for (QueryParams queryParams : queryParamsList) {
 
             String query = getQuery(queryParams);
             logQuery(query);
 
             URI uri = new URI(query);
             String responseList = restTemplate.getForObject(uri, String.class);
 
             List<QueryListResponse> queryResponses = htmlListParser.parse(responseList);
 
             processResponses(queryResponses);
         }
     }
 
     private String getQuery(QueryParams queryParams) {
         String query = queryBuilder.buildQuery(queryParams);
         return query;
     }
 
     private void logQuery(String query) {
         log.info("------------------------------ QUERY -------------------------------------");
         log.info(query);
     }
 
     private void processResponses(List<QueryListResponse> queryResponses) {
         for (QueryListResponse queryResponse : queryResponses) {
             ItemResponse itemResponse = extractItemInfo(queryResponse);
             queryResponse.setItemResponse(itemResponse);
 
            log.info(String.format("Processing response: %s", queryResponse.toString()));
            
             emailSender.process(queryResponse);
         }
     }
 
     private ItemResponse extractItemInfo(QueryListResponse queryResponse) {
         String html = restTemplate.getForObject(queryResponse.getItemUrl(), String.class);
         ItemResponse itemResponse = htmlItemParser.parse(html);
         queryResponse.setItemResponse(itemResponse);
 
         return itemResponse;
     }
 }
