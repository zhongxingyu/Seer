 /*
  * Copyright 2009 Google Inc.
  * 
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not
  * use this file except in compliance with the License. You may obtain a copy of
  * the License at
  * 
  * http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
  * License for the specific language governing permissions and limitations under
  * the License.
  */
 package com.google.gwt.chrome.crx.client.events;
 
 import com.google.gwt.chrome.crx.client.Port;
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.core.client.JavaScriptObject;
 import com.google.gwt.core.client.GWT.UncaughtExceptionHandler;
 
 /**
  * Wraps event from chrome.onConnect.
  * 
  * See documentation at: <a href=
  * "http://dev.chromium.org/developers/design-documents/extensions/content-scripts"
  * >Content Script Messaging</a>
  * 
  */
 public final class ConnectEvent extends Event {
 	/**
 	 * Called when a ContentScript opens a port.
 	 */
 	public interface Listener {
 		void onConnect(Port port);
 	}
 
 	/**
 	 * Takes care of reporting exceptions to the console in hosted mode.
 	 * 
 	 * @param listener
 	 *            the listener object to call back.
 	 * @param port
 	 *            argument from the callback.
 	 */
	@SuppressWarnings("unused")
 	private static void onConnectImpl(Listener listener, Port port) {
 		UncaughtExceptionHandler ueh = GWT.getUncaughtExceptionHandler();
 		if (ueh != null) {
 			try {
 				listener.onConnect(port);
 			} catch (Exception ex) {
 				ueh.onUncaughtException(ex);
 			}
 		} else {
 			listener.onConnect(port);
 		}
 	}
 
 	protected ConnectEvent() {
 	}
 
 	public ListenerHandle addListener(Listener listener) {
 		return new ListenerHandle(this, addListenerImpl(listener));
 	}
 
 	private native JavaScriptObject addListenerImpl(Listener listener) /*-{
 																		var handle = function(port) {
 																		@com.google.gwt.chrome.crx.client.events.ConnectEvent::onConnectImpl(Lcom/google/gwt/chrome/crx/client/events/ConnectEvent$Listener;Lcom/google/gwt/chrome/crx/client/Port;)
 																		(listener, port);
 																		}
 
 																		this.addListener(handle);
 																		return handle;
 																		}-*/;
 }
