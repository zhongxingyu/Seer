 package com.smartbear.edp.api;
 
 import java.util.EventObject;
 
 /**
  * User: Renato
  */
 public interface EventManager {
 
 	/**
 	 * Subscribes a subscriber to events of the given type.
 	 * @param subscriber to be subscribed
 	 * @param eventType of interest to the subscriber
 	 * @param <K> Type of the Event
 	 */
 	<K extends EventObject> void subscribe( EventSubscriber<K> subscriber, Class<K> eventType );
 
 	/**
 	 * Subscribes a subscriber to events of the given type. Unlike the simple subscribe method,
 	 * with this method only a WeakReference is kept to the subscriber, so that if the subscriber
 	 * is not referenced by any other 'live' Objects, which may be garbage-collected.
 	 * @param subscriber to be subscribed
 	 * @param eventType of interest to the subscriber
 	 * @param <K> Type of the Event
 	 */
 	<K extends EventObject> void subscribeWeakly( EventSubscriber<K> subscriber, Class<K> eventType );
 
 	/**
	 * Unsubscribe the given subscriber to listening to events. Notice that when using 'subscribeWeakly'
 	 * to subscribe, it is NOT necessary to call this method to avoid memory-leaks.
 	 * @param subscriber to be unsubscribed
 	 * @param <K> Type of the Event
 	 */
 	<K extends EventObject> void unSubscribe( EventSubscriber<K> subscriber );
 
 	/**
 	 * Any Object with access to this EventManager may post an Event through this method.
 	 * @param event to be posted. Any existing Subscribers will be notified through their
 	 *              handle method.
 	 */
 	void post( EventObject event);
 
 }
