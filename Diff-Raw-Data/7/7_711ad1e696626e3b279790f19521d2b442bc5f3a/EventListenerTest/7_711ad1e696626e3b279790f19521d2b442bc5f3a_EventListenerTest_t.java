 /*******************************************************************************
  * Copyright (c) 2008, 2009 SOPERA GmbH.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     SOPERA GmbH - initial API and implementation
  *******************************************************************************/
 package org.eclipse.swordfish.core.event.test;
 
 import java.util.Dictionary;
 import java.util.Hashtable;
 
 import org.eclipse.swordfish.core.event.EventFilter;
 import org.eclipse.swordfish.core.event.EventHandler;
 import org.eclipse.swordfish.core.event.EventService;
 import org.eclipse.swordfish.core.event.Severity;
 import org.eclipse.swordfish.core.event.TrackingEvent;
 import org.eclipse.swordfish.core.test.util.OsgiSupport;
 import org.eclipse.swordfish.core.test.util.SwordfishTestCase;
 import org.eclipse.swordfish.internal.core.event.EventHandlerRegistry;
 import org.eclipse.swordfish.internal.core.event.EventServiceImpl;
 import org.eclipse.swordfish.internal.core.event.SeverityEventFilter;
 import org.eclipse.swordfish.internal.core.event.TrackingEventImpl;
 import org.osgi.framework.ServiceRegistration;
 import org.osgi.service.event.EventAdmin;
 
 
 public class EventListenerTest extends SwordfishTestCase {
 
     public static final String TEST_TOPIC = "org/eclipse/swordfish/event/test";
 
     public void test1GenericEventAdminService() throws Exception {
         EventAdmin eventAdmin = OsgiSupport.getReference(bundleContext, EventAdmin.class);
         assertNotNull(eventAdmin);
 
         final int[] handledCountArray = new int[]{0};
         org.osgi.service.event.EventHandler simpleHandle = new org.osgi.service.event.EventHandler(){
             public void handleEvent(org.osgi.service.event.Event event) {
                 handledCountArray[0]++;
             }
         };
 
         Dictionary<String, Object> properties = new Hashtable<String, Object>();
         properties.put(org.osgi.service.event.EventConstants.EVENT_TOPIC, TEST_TOPIC);
 
         addRegistrationToCancel(bundleContext.registerService(org.osgi.service.event.EventHandler.class.getName(), simpleHandle, properties));
 
 
         org.osgi.service.event.Event event = new org.osgi.service.event.Event(TEST_TOPIC, null);
         eventAdmin.postEvent(event);
         eventAdmin.postEvent(event);
 
         Thread.sleep(500);
         assertEquals(2, handledCountArray[0]);
     }
 
    @SuppressWarnings({ "unchecked" })
	public void test2SworfishEventServices() throws Exception {
        EventHandlerRegistry eventListenerRegistry = OsgiSupport.getReference(bundleContext, EventHandlerRegistry.class);
        assertNotNull(eventListenerRegistry);
 
        EventServiceImpl eventSender = (EventServiceImpl)OsgiSupport.getReference(bundleContext, EventService.class);
        assertNotNull(eventSender);
        assertNotNull(eventSender.getEventAdmin());
 
        final int[] receiveCount = new int[]{0};
        EventHandler genericListener = new EventHandler<TrackingEvent>() {
             public String getSubscribedTopic() {
                 return SimpleTrackingEvent.TOPIC;
             }
 
             public void handleEvent(TrackingEvent arg0) {
                 receiveCount[0]++;
             }
 			public EventFilter getEventFilter() {
 				return null;
 			}
        };
        // adding listener to registry
        int intitialSize = eventListenerRegistry.getKeySet().size();
        ServiceRegistration eventListenerSegistration =
            bundleContext.registerService(EventHandler.class.getName(), genericListener, null);
        addRegistrationToCancel(eventListenerSegistration);
        Thread.sleep(500);
        assertEquals(intitialSize + 1, eventListenerRegistry.getKeySet().size());
 
        // send test event
        TrackingEvent event = new SimpleTrackingEvent(null);
        eventSender.postEvent(event);
        Thread.sleep(500);
        assertEquals(1, receiveCount[0]);
 
        // removing listener from registry
        intitialSize = eventListenerRegistry.getKeySet().size();
        eventListenerSegistration.unregister();
        Thread.sleep(500);
        assertEquals(intitialSize - 1, eventListenerRegistry.getKeySet().size());
 
        // sending event that shouldn't received anymore
        eventSender.postEvent(event);
        Thread.sleep(500);
        assertEquals(1, receiveCount[0]);
     }
 
    @SuppressWarnings("unchecked")
	public void test3SeverityAwareEvents() throws Exception {
         EventHandlerRegistry eventListenerRegistry = OsgiSupport.getReference(bundleContext, EventHandlerRegistry.class);
         assertNotNull(eventListenerRegistry);
 
         EventServiceImpl eventSender = (EventServiceImpl)OsgiSupport.getReference(bundleContext, EventService.class);
         assertNotNull(eventSender);
         assertNotNull(eventSender.getEventAdmin());
 
         final int[] receiveCount = new int[]{0};
         EventHandler genericListener = new EventHandler<TrackingEvent>() {
              public String getSubscribedTopic() {
                  return SimpleTrackingEvent.TOPIC;
              }
              public void handleEvent(TrackingEvent arg0) {
                  receiveCount[0]++;
              }
              
              public EventFilter getEventFilter(){
             	 return new SeverityEventFilter(Severity.INFO);
              }
         };
         
 
         
         
         // adding listener to registry
         ServiceRegistration eventListenerSegistration =
             bundleContext.registerService(EventHandler.class.getName(), genericListener, null);
         addRegistrationToCancel(eventListenerSegistration);
 
         // send test event with similar severity
         TrackingEventImpl event = new SimpleTrackingEvent(null);
         event.setSeverity(Severity.INFO);
         eventSender.postEvent(event);
         Thread.sleep(500);
         assertEquals(1, receiveCount[0]);
 
         // send event with LOWER severity
         event = new SimpleTrackingEvent(null);
         event.setSeverity(Severity.DEBUG);
         eventSender.postEvent(event);
         Thread.sleep(500);
         assertEquals(1, receiveCount[0]);
         
         // send event with GRATER severity
         event = new SimpleTrackingEvent(null);
         event.setSeverity(Severity.ERROR);
         eventSender.postEvent(event);
         Thread.sleep(500);
         assertEquals(2, receiveCount[0]);
 
         // removing listener from registry
         eventListenerSegistration.unregister();
         Thread.sleep(500);
 
         // sending event that shouldn't received anywere
         eventSender.postEvent(event);
         Thread.sleep(500);
         assertEquals(2, receiveCount[0]);
      }
 }
