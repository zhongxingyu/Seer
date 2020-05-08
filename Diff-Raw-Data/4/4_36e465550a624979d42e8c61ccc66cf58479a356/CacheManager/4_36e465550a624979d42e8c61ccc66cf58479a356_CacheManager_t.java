 /**
  * @author Nigel Cook
  *
  * (C) Copyright 2010-2012. Nigel Cook. All rights reserved.
  * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
  * 
  * Licensed under the terms described in LICENSE file that accompanied this code, (the "License"); you may not use this file
  * except in compliance with the License. 
  * 
  *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on 
  *  an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the 
  *  specific language governing permissions and limitations under the License.
  */
 package n3phele.client;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import n3phele.client.model.Change;
 import n3phele.client.model.ChangeGroup;
 import n3phele.client.model.Cloud;
 import n3phele.client.model.Collection;
 import n3phele.client.model.Root;
 import n3phele.client.presenter.helpers.AuthenticatedRequestFactory;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.event.shared.EventBus;
 import com.google.gwt.event.shared.EventHandler;
 import com.google.gwt.event.shared.GwtEvent;
 import com.google.gwt.http.client.Request;
 import com.google.gwt.http.client.RequestBuilder;
 import com.google.gwt.http.client.RequestCallback;
 import com.google.gwt.http.client.RequestException;
 import com.google.gwt.http.client.Response;
 import com.google.gwt.http.client.URL;
 import com.google.gwt.user.client.Timer;
 
 
 public class CacheManager {
 	Map<String, List<Registration>> cache = new HashMap<String, List<Registration>>();
 	EventBus eventBus;
 	private long stamp = 0;
 	private boolean gotClouds = false;
 	private int backoff = 0;
 	private int counter = 0;
 	private Timer refreshTimer = null;
	final public String ServiceAddress = "https://n3phele-dev.appspot.com/resources/";
	//final public String ServiceAddress = "http://127.0.0.1:8888/resources/";   // uncomment for local access
 	//final public String ServiceAddress = "http://192.168.100.73:8888/resources/";   // uncomment for local access
 	
 	public CacheManager(EventBus eventBus) {
 		this.eventBus = eventBus;
 	}
 	
 	public void start() {
 		if(refreshTimer == null) {
 			if(!gotClouds)
 				refreshClouds();
 			refreshTimer = new Timer() {
 				public void run()
 				{
 					if(AuthenticatedRequestFactory.isAuthenticated()) {
 						if(!gotClouds) {
 							refreshClouds();
 						}
 						int recentRequests = AuthenticatedRequestFactory.incrementalRequests();
 						counter += 1;
 						boolean expired = counter >= backoff;
 						if(recentRequests > 0) {
 							counter = 0;
 							backoff = 0;
 						} else if(expired) {
 							counter = 0;
 							if(backoff <= 0) {
 								backoff = 1;
 							} else {
 								backoff = backoff >= 32 ? 32 : backoff + 1 ;
 							}
 						}
 						GWT.log("recentRequests="+recentRequests+" counter="+counter+" backoff="+backoff );
 						if(backoff <= 2 || expired) {	
 							refresh();
 						}
 					}
 				}
 			};
 			refreshTimer.scheduleRepeating(5000);
 		}
 	}
 	public void refresh() {
 	    RequestBuilder builder = AuthenticatedRequestFactory.newCacheManagerRequest(RequestBuilder.GET, 
 	    		ServiceAddress+"?summary=false&changeOnly=true&since="+stamp);
 	    if(builder == null) return;
 	    try {
 	    	  GWT.log("Sending request");
 	          @SuppressWarnings("unused")
 			Request request = builder.sendRequest(null, new RequestCallback() {
 	          public void onError(Request request, Throwable exception) {
 	          // displayError("Couldn't retrieve JSON "+exception.getMessage());
 	        }
 
 	        public void onResponseReceived(Request request, Response response) {
 	          if (200 == response.getStatusCode()) {
 	        	Root root = Root.parse(response.getText());
 	        	GWT.log(response.getText());
 	        	GWT.log(root.toString());
 	        	stamp = root.getStamp();
 	        	ChangeGroup changes = root.getChangeGroup();
 	    		if (changes != null) {
 	    			if (changes.getChange() != null) {
 	    				for (Change x : changes.getChange()) {
 	    					List<Registration> item;
 	    					item = cache.get(x.getUri());
 	    					GWT.log("Got change URI "+x.getUri()+" item "+item);
 	    					if(x.getUri().equals(cloudUrl)) {
 	    						refreshClouds();
 	    					}
 	    					if (item != null) {
 	    						fireAll(x.getUri(), item);
 	    					}
 	    				}
 	    				if(changes.getChange().size() != 0) {
 	    					backoff = 0;
 	    				}
 	    			}
 	    		} else {
 	    			for(Entry<String, List<Registration>> entry : cache.entrySet()) {
 	    				fireAll(entry.getKey(), entry.getValue());
 	    			}
 	    		}
 	        	
 	          } else {
 	            // displayError("Couldn't retrieve JSON (" + response.getStatusText() + ")");
 	          }
 	        }
 
 	      });
 	    } catch (RequestException e) {
 	      // displayError("Couldn't retrieve JSON "+e.getMessage());
 	    }
 	}
 	
 	private void fireAll(String key, List<Registration> l) {
 		if (l != null) {
 			for (Registration r : l) {
 				GWT.log("Firing "+r.registrant+" "+key);
 				eventBus.fireEvent(r.constructor.newInstance(key));
 			}
 		}
 	}
 
 	/**
 	 * Register a class of event to be sent when modifications to key occur.
 	 * 
 	 * @param key
 	 * @param registrant
 	 * @param event
 	 */
 	public void register(String key, String registrant,
 			EventConstructor constructor) {
 		Registration registration = new Registration(registrant, constructor);
 		List<Registration> l = cache.get(key);
 		if (l == null) {
 			l = new ArrayList<Registration>();
 			cache.put(key, l);
 		} else {
 			for (int i = 0; i < l.size(); i++) {
 				Registration r = l.get(i);
 				if (r.registrant.equals(registration.registrant)) {
 					l.set(i, registration);
 					return;
 				}
 			}
 		}
 		l.add(registration);
 	}
 
 	public void unregister(String key, String registrant) {
 		List<Registration> l = cache.get(key);
 		if (l != null) {
 			for (int i = 0; i < l.size(); i++) {
 				Registration r = l.get(i);
 				if (r.registrant.equals(registrant)) {
 					l.remove(i);
 					if(l.size() == 0) {
 						cache.remove(key);
 					}
 					return;
 				}
 			}
 		}
 	}
 
 	public void unregisterAll(String registrant) {
 		String[] keys = cache.keySet().toArray(new String[cache.keySet().size()]);
 		for(int i=0; i < keys.length; i++) {
 			String key = keys[i];
 			List<Registration> list = cache.get(key);
 			if (list != null) {
 				for (int l = 0; l < list.size(); l++) {
 					Registration r = list.get(l);
 					if (r.registrant.equals(registrant)) {
 						list.remove(l);
 						if(list.size() == 0) {
 							cache.remove(key);
 						}
 						break;
 					}
 				}
 			}
 		}
 	}
 
 	public interface EventConstructor {
 		public GwtEvent<?> newInstance(String key);
 	}
 
 	private static class Registration {
 		public final EventConstructor constructor;
 		public final String registrant;
 
 		public Registration(String registrant, EventConstructor constructor) {
 			this.constructor = constructor;
 			this.registrant = registrant;
 		}
 
 		/* (non-Javadoc)
 		 * @see java.lang.Object#hashCode()
 		 */
 		@Override
 		public int hashCode() {
 			final int prime = 31;
 			int result = 1;
 			result = prime * result
 					+ ((constructor == null) ? 0 : constructor.hashCode());
 			result = prime * result
 					+ ((registrant == null) ? 0 : registrant.hashCode());
 			return result;
 		}
 
 		/* (non-Javadoc)
 		 * @see java.lang.Object#equals(java.lang.Object)
 		 */
 		@Override
 		public boolean equals(Object obj) {
 			if (this == obj)
 				return true;
 			if (obj == null)
 				return false;
 			if (getClass() != obj.getClass())
 				return false;
 			Registration other = (Registration) obj;
 			if (constructor == null) {
 				if (other.constructor != null)
 					return false;
 			} else if (!constructor.equals(other.constructor))
 				return false;
 			if (registrant == null) {
 				if (other.registrant != null)
 					return false;
 			} else if (!registrant.equals(other.registrant))
 				return false;
 			return true;
 		}
 	}
 
 
 	public void test(String username, String password,
 			RequestCallback requestCallback) {
 
 		// Send request to server and catch any errors.
 		RequestBuilder builder = AuthenticatedRequestFactory.request(RequestBuilder.GET, ServiceAddress+"user/byName?id="+URL.encodeQueryString(username),
 				username, password);
 		GWT.log("Sending authentication test request");
 		Request request=null;
 		try {
 			request = builder.sendRequest(null, requestCallback);
 		} catch (RequestException e) {
 			requestCallback.onError(request, e);
 		}
 
 	}
 	
 	private final String cloudUrl = ServiceAddress+"cloud";
 	private final List<Cloud> clouds = new ArrayList<Cloud>();
 	public List<Cloud> getClouds() {
 		return clouds;
 	}
 	
 	protected void refreshClouds() {
 		
 
 		RequestBuilder builder = AuthenticatedRequestFactory.newCacheManagerRequest(RequestBuilder.GET, cloudUrl);
 		try {
 			builder.sendRequest(null, new RequestCallback() {
 				public void onError(Request request, Throwable exception) {
 					GWT.log("Couldn't retrieve JSON " + exception.getMessage());
 				}
 	
 				public void onResponseReceived(Request request, Response response) {
 					if (200 == response.getStatusCode()) {
 						Collection<Cloud> p = Cloud.asCollection(response.getText());
 						clouds.clear();
 						clouds.addAll(p.getElements());
 						GWT.log("Got "+clouds.size()+" clouds.");
 						gotClouds = true;
 						eventBus.fireEvent(new CloudListUpdate(cloudUrl));
 					} else {
 						GWT.log("Couldn't retrieve JSON ("
 								+ response.getStatusText() + ")");
 					}
 				}
 			});
 			gotClouds = false;
 		} catch (RequestException e) {
 			GWT.log("Couldn't retrieve JSON " + e.getMessage());
 		}
 	}
 	
 	public CacheManager.EventConstructor cloudListConstructor = new CacheManager.EventConstructor() {
 		@Override
 		public CloudListUpdate newInstance(String key) {
 			return new CloudListUpdate(key);
 		}
 	};
 	public interface CloudListUpdateEventHandler extends EventHandler {
 		void onMessageReceived(CloudListUpdate event);
 	}
 	public static class CloudListUpdate extends GwtEvent<CloudListUpdateEventHandler> {
 		public static Type<CloudListUpdateEventHandler> TYPE = new Type<CloudListUpdateEventHandler>();
 		private final String key;
 		public CloudListUpdate(String key) {this.key = key;}
 		@Override
 		public com.google.gwt.event.shared.GwtEvent.Type<CloudListUpdateEventHandler> getAssociatedType() {
 			return TYPE;
 		}
 		@Override
 		protected void dispatch(CloudListUpdateEventHandler handler) {
 			handler.onMessageReceived(this);
 		}
 		public String getKey() { return this.key; }
 	}
 
 }
