 package com.anotherbrick.inthewall;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 
 public class VizNotificationCenter {
 
   private static VizNotificationCenter instance = new VizNotificationCenter();
 
   public enum EventName {
    SCROLLING_TWEETS_UPDATED, GRAPH_YEAR_CHANGED, BUTTON_TOUCHED, FILTER_LIST_CLOSE, FILTER_LIST_OPEN, CURRENT_FILTER_UPDATED, MAP_COORDINATES_UPDATED, CRASHES_UPDATED, CRASHES_COUNT_BY_VALUE_UPDATED, SELECTOR_PANEL_CLOSE, SELECTOR_PANEL_OPEN, CHANGED_MAP_COLOR_ATTRIBUTE, SCATTER_PLOT_AXIS_UPDATED, EVENT_CHANGED, DATA_UPDATED, MAP_ZOOMED_OR_PANNED
   }
 
   private HashMap<EventName, ArrayList<EventSubscriber>> subscribers;
 
   public static VizNotificationCenter getInstance() {
     return instance;
   }
 
   private VizNotificationCenter() {
     subscribers = new HashMap<VizNotificationCenter.EventName, ArrayList<EventSubscriber>>();
     for (EventName eventName : EventName.values()) {
       subscribers.put(eventName, new ArrayList<EventSubscriber>());
     }
   }
 
   public void registerToEvent(EventName eventName, EventSubscriber eventSubscriber) {
     if (!subscribers.get(eventName).contains(eventSubscriber)) {
       subscribers.get(eventName).add(eventSubscriber);
     }
   }
 
   public void notifyEvent(EventName eventName) {
     notifyEvent(eventName, null);
   }
 
   public void notifyEvent(EventName eventName, Object data) {
     System.out.println("=== EVENT: " + eventName.toString() + "("
         + (data == null ? "" : data.toString()) + ")");
     if (subscribers.get(eventName) == null) return;
     for (EventSubscriber es : subscribers.get(eventName)) {
       System.out.println("    --> " + es.getClass().getSimpleName().toString());
       es.eventReceived(eventName, data);
     }
   }
 }
