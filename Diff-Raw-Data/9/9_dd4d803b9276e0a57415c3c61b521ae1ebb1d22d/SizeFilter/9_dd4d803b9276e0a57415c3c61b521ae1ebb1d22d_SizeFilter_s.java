 package com.abudko.reseller.huuto.query.filter;
 
 import java.util.ArrayList;
 import java.util.Collection;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.stereotype.Component;
 
 import com.abudko.reseller.huuto.query.html.list.ListResponse;
 import com.abudko.reseller.huuto.query.params.SearchParams;
 
 @Component
 public class SizeFilter extends RangeFilter {
 
     private Logger log = LoggerFactory.getLogger(getClass());
 
     public Collection<ListResponse> apply(Collection<ListResponse> queryResponses, SearchParams searchParams) {
         double sizeMin = parseMin(searchParams.getSizeMin());
         double sizeMax = parseMax(searchParams.getSizeMax());
         if (sizeMin > 0 || sizeMax > 0) {
             Collection<ListResponse> filtered = filterInternal(queryResponses, sizeMin, sizeMax);
             return filtered;
         }
 
         return queryResponses;
     }
 
     private Collection<ListResponse> filterInternal(Collection<ListResponse> queryResponses, double sizeMin,
             double sizeMax) {
         Collection<ListResponse> filtered = new ArrayList<ListResponse>();
         for (ListResponse queryListResponse : queryResponses) {
             String sizeStr = queryListResponse.getSize();
             try {
                 double size = Double.parseDouble(sizeStr);
                 if (size >= sizeMin && size <= sizeMax) {
                     filtered.add(queryListResponse);
                 }
             } catch (RuntimeException e) {
                log.error(String.format("Unable to parse %s", sizeStr), e);
             }
         }
         return filtered;
     }
 
 }
