 package com.example.formidable;
 
 import java.util.*;
 
 import com.couchbase.touchdb.TDView;
 import com.couchbase.touchdb.TDViewMapBlock;
 import com.couchbase.touchdb.TDViewMapEmitBlock;
 import com.couchbase.touchdb.TDViewReduceBlock;
 
 public class CurrentState {
 
     public void setMapReduceBlocksFor(TDView view) {
        view.setMapReduceBlocks(map(), reduce(), "0.1");
     }
 
     private TDViewMapBlock map() {
         return new TDViewMapBlock() {
             @Override
             public void map(Map<String, Object> document, TDViewMapEmitBlock emitter) {
                 List<Object> recordTimeKey = Arrays.asList(document.get("recordId"), document.get("epoch"));
 				emitter.emit(recordTimeKey, document);
             }
         };
     }
 
     private TDViewReduceBlock reduce() {
         return new TDViewReduceBlock() {
             @Override
             public Object reduce(List<Object> keys, List<Object> values, boolean rereduce) {
                 List<Event> events = hydrateEvents(values);
                 return new EventAggregation(events).replay();
             }
 
             private List<Event> hydrateEvents(List<Object> values) {
                 List<Event> events = new ArrayList<Event>();
                 for (Object document : values) {
                     events.add(hydrateEvent(document));
                 }
                 return events;
             }
 
             private Event hydrateEvent(Object document) {
                 Map<String, Object> documentMap = (Map<String, Object>) document;
                 return new Event((Integer) documentMap.get("epoch"), (String) documentMap.get("recordId"),
                         (Map<String, String>) documentMap.get("data"));
             }
         };
     }
 }
