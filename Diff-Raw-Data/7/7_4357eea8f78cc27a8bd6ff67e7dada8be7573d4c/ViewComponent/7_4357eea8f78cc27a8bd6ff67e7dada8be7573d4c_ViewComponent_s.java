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
 package com.google.gwt.chrome.crx.client;
 
 /**
  * A component of a chrome extension that has its own document and window
  * context. Its window context is accessible as a {@link View} via extensions
  * API.
  */
 public abstract class ViewComponent implements Component {
 	private View view;
 
 	public View getView() {
 		return view;
 	}
 
 	/**
 	 * Implement this method to be called when the extension is ready to use.
 	 */
 	public abstract void onLoad();
 
 	protected native void connect(String name) /*-{
 												var self = this;
 												window[name] = function(view) {
 												self.
 												@com.google.gwt.chrome.crx.client.ViewComponent::connectImpl(Lcom/google/gwt/chrome/crx/client/View;)
 												(view);
 												};
 												}-*/;
 
	@SuppressWarnings("unused")
 	private void connectImpl(View view) {
 		this.view = view.cast();
 		onLoad();
 	}
 }
