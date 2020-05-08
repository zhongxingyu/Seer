 /*
  * Copyright 2009 Jeroen Steenbeeke
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at 
  *
  * 		http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License. 
  */
 package com.fortuityframework.wicket;
 
 import java.io.Serializable;
 import java.lang.ref.WeakReference;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.apache.wicket.Component;
 import org.apache.wicket.Page;
 import org.apache.wicket.PageReference;
 import org.apache.wicket.Session;
 import org.apache.wicket.behavior.Behavior;
 import org.apache.wicket.page.IManageablePage;
 import org.apache.wicket.page.IPageManager;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.fortuityframework.core.annotation.ioc.OnFortuityEvent;
 import com.fortuityframework.core.dispatch.EventContext;
 import com.fortuityframework.core.dispatch.EventException;
 import com.fortuityframework.core.dispatch.EventListener;
 import com.fortuityframework.core.dispatch.EventListenerLocator;
 import com.fortuityframework.core.dispatch.NullEventListenerLocator;
 import com.fortuityframework.core.event.Event;
 
 /**
  * Fortuity Event Listeners locator for use with Wicket components. When used in
  * combination with a {@link FortuityComponentInstantiationListener}, this
  * locator will delegate Fortuity events to Wicket components. It will also
  * delegate events to other event listeners if need be (for example, if using
  * Fortuity with Spring).
  * 
  * To use this locator, you should use the following code in your
  * WicketApplication's init() method.
  * 
  * <pre>
  * {
  * 	&#064;code
  * 	WicketEventListenerLocator locator = new WicketEventListenerLocator(
  * 			Duration.minutes(15));
  * 	FortuityComponentInstantiationListener listener = new FortuityComponentInstantiationListener(
  * 			locator);
  * }
  * </pre>
  * 
  * 
  * @author Jeroen Steenbeeke
  * 
  */
 public class WicketEventListenerLocator implements EventListenerLocator {
 	private EventListenerLocator chainedLocator;
 
 	Map<Class<?>, Set<ComponentEventListener>> listeners = new HashMap<Class<?>, Set<ComponentEventListener>>();
 
 	private static Map<String, WeakReference<Session>> sessionReferences = new HashMap<String, WeakReference<Session>>();
 
 	private static Map<String, Integer> lastPageIdReferences = new HashMap<String, Integer>();
 
 	private static Logger log = LoggerFactory
 			.getLogger(WicketEventListenerLocator.class);
 
 	/**
 	 * Creates a new event listener locator that does not chain, and expires
 	 * components after one hour
 	 */
 	public WicketEventListenerLocator() {
 		this.chainedLocator = new NullEventListenerLocator();
 	}
 
 	/**
 	 * Creates a new event listener locator that chains to the given locator,
 	 * and expires after the specified duration has passed
 	 * 
 	 * @param chainedLocator
 	 *            The locator that requests should be chained to if this one
 	 *            cannot find the reference
 	 */
 	public WicketEventListenerLocator(EventListenerLocator chainedLocator) {
 		this.chainedLocator = chainedLocator;
 	}
 
 	static Session getSession(String id) {
 		return sessionReferences.containsKey(id) ? sessionReferences.get(id)
 				.get() : null;
 	}
 
 	static void addSession(Session session) {
 		if (!sessionReferences.containsKey(session)) {
 			sessionReferences.put(session.getId(), new WeakReference<Session>(
 					session));
 		}
 	}
 
 	/**
 	 * Called by the FortuityComponentInstantiationListener when a new component
 	 * has been created. Scans the component's class structure and adds handlers
 	 * for any event response method in the component
 	 * 
 	 * @param component
 	 *            The component that was added
 	 */
 	synchronized void onComponentAdded(Component component) {
 		Class<?> componentClass = component.getClass();
 
 		for (Method m : componentClass.getMethods()) {
 			OnFortuityEvent metadata = m.getAnnotation(OnFortuityEvent.class);
 			if (metadata != null) {
 				if (m.getParameterTypes().length == 1
 						&& m.getParameterTypes()[0] == EventContext.class) {
 					Class<? extends Event<?>>[] events = getEvents(metadata);
 					for (Class<? extends Event<?>> event : events) {
 						if (!listeners.containsKey(event)) {
 							listeners.put(event,
 									new HashSet<ComponentEventListener>());
 						}
 
 						listeners.get(event).add(
 								new ComponentEventListener(component, m));
 					}
 				} else {
 					log.error("Fortuity event listener has incorrect parameter sequence for method "
 							+ m.toGenericString()
 							+ " on component "
 							+ component.getPath()
 							+ " of page "
 							+ component.getPage().toString());
 				}
 			}
 		}
 	}
 
 	private Class<? extends Event<?>>[] getEvents(OnFortuityEvent metadata) {
 		Class<? extends Event<?>>[] events = (Class<? extends Event<?>>[]) metadata
 				.value();
 		return events;
 	}
 
 	/**
 	 * @see com.fortuityframework.core.dispatch.EventListenerLocator#getEventListeners(java.lang.Class)
 	 */
 	@Override
 	public List<EventListener> getEventListeners(
 			Class<? extends Event<?>> eventClass) {
 		List<EventListener> result = new LinkedList<EventListener>();
 
 		// Dispatch to any chained listeners. We assume that Wicket components
 		// will be updated last,
 		// and any even listeners given by the chained locator are to ensure
 		// data integrity
 		result.addAll(chainedLocator.getEventListeners(eventClass));
 
 		Class<?> next = eventClass;
 
 		while (Event.class.isAssignableFrom(next)) {
 			// If we have listeners for this event then dispatch the event
 			if (listeners.containsKey(next)) {
 				// Set for removing inactive listeners. Components expire when
 				// either they have been
 				// garbage collected, or they have expired according to their
 				// duration. However,
 				// we must prevent a ConcurrentModificationException so we
 				// simply put those that should be removed
 				// in a set, and remove it outside the loop
 				Set<ComponentEventListener> removeListeners = new HashSet<ComponentEventListener>();
 
 				for (ComponentEventListener listener : listeners.get(next)) {
 					// Only dispatch to active listeners
 					if (listener.isActive()) {
 						result.add(listener);
 					} else {
 						// Remove inactive ones
 						removeListeners.add(listener);
 					}
 				}
 
 				// Perform the remove
 				listeners.get(next).removeAll(removeListeners);
 			}
 
 			// Check superclass to ensure polymorphic event handling
 			next = next.getSuperclass();
 		}
 
 		return result;
 	}
 
 	/**
 	 * Event listener tied to a Wicket Component
 	 * 
 	 * @author Jeroen Steenbeeke
 	 */
 	private static class ComponentEventListener implements EventListener,
 			Serializable {
 		private static final long serialVersionUID = 1L;
 
 		private boolean isPage;
 
 		private String componentPath;
 		private PageReference ref;
 		private String methodName;
 
 		private String sessionRef;
 
 		/**
 		 * Create a new event listener for the given method of the given
 		 * component
 		 * 
 		 * @param component
 		 *            The component that contains the method
 		 * @param eventMethod
 		 *            The method to invoke when it's event occurs
 		 */
 		public ComponentEventListener(Component component, Method eventMethod) {
 
 			this.componentPath = component.getPath();
 
 			this.isPage = component instanceof Page;
 
 			this.methodName = eventMethod.getName();
 
 			Session session = Session.get();
 
 			addSession(session);
 
 			this.sessionRef = session.getId();
 
 			// Add a callback to report the reference before render, since
 			// we cannot get a reliable PageReference at this stage
 			component.add(new Behavior() {
 
 				private static final long serialVersionUID = 1L;
 
 				/**
 				 * @see org.apache.wicket.behavior.AbstractBehavior#beforeRender(org.apache.wicket.Component)
 				 */
 				@Override
 				public void beforeRender(Component component) {
 					super.beforeRender(component);
 
 					if (ComponentEventListener.this.ref == null) {
 						Page pg = component.getPage();
 
 						ComponentEventListener.this.ref = pg.getPageReference();
 
 						Session sess = pg.getSession();
 						if (sess != null) {
 							Integer lastId = lastPageIdReferences.get(sess
 									.getId());
 
 							if (lastId != null) {
 								if (lastId < ComponentEventListener.this.ref
 										.getPageId()) {
 									lastPageIdReferences.put(sess.getId(),
 											ComponentEventListener.this.ref
 													.getPageId());
 								}
 							} else {

 							}
 						}
 					}
 
 				}
 			});
 		}
 
 		public boolean isActive() {
 			Session session = getSession(sessionRef);
 
 			if (session != null && !session.isSessionInvalidated()) {
 				return new Integer(ref.getPageId()).equals(lastPageIdReferences
 						.get(session.getId()));
 			}
 
 			return false;
 		}
 
 		/**
 		 * @see com.fortuityframework.core.dispatch.EventListener#dispatchEvent(com.fortuityframework.core.dispatch.EventContext)
 		 */
 		@Override
 		public void dispatchEvent(EventContext<?> context)
 				throws EventException {
 			try {
 				Session session = getSession(sessionRef);
 
 				if (session != null && !session.isSessionInvalidated()) {
 					IPageManager pageManager = session.getPageManager();
 
 					if (isActive()) {
 						IManageablePage pg = pageManager.getPage(ref
 								.getPageId());
 
 						if (pg instanceof Page) {
 							Page page = (Page) pg;
 
 							// Check if the component still exists
 							Component component = isPage ? page : page
 									.get(componentPath);
 
 							Method eventMethod = component.getClass()
 									.getMethod(methodName, EventContext.class);
 
 							if (component != null) {
 								// Invoke the event
 								eventMethod.invoke(component, context);
 								// Detach the component
 								if (component.hasBeenRendered()) {
 									component.detach();
 								}
 							}
 						}
 					}
 				}
 			} catch (IllegalArgumentException e) {
 				throw new EventException(e.getMessage());
 			} catch (IllegalAccessException e) {
 				throw new EventException(e.getMessage());
 			} catch (InvocationTargetException e) {
 				throw new EventException(e.getMessage());
 			} catch (SecurityException e) {
 				throw new EventException(e.getMessage());
 			} catch (NoSuchMethodException e) {
 				throw new EventException(e.getMessage());
 			}
 		}
 	}
 }
