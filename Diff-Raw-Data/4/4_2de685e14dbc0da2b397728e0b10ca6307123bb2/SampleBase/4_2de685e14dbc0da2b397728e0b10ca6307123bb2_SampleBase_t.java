 /**********************************************
  * Copyright (C) 2009 Lukas Laag
  * This file is part of libgwtsvg-samples.
  * 
  * libgwtsvg-samples is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * libgwtsvg-samples is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with libgwtsvg-samples.  If not, see http://www.gnu.org/licenses/
  **********************************************/
 package org.vectomatic.svg.samples.client;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.http.client.Request;
 import com.google.gwt.http.client.RequestBuilder;
 import com.google.gwt.http.client.RequestCallback;
 import com.google.gwt.http.client.RequestException;
 import com.google.gwt.http.client.Response;
 import com.google.gwt.uibinder.client.UiField;
 import com.google.gwt.user.client.ui.HTML;
 import com.google.gwt.user.client.ui.Panel;
 import com.google.gwt.user.client.ui.SimplePanel;
 import com.google.gwt.user.client.ui.TabPanel;
 
 /**
  * Base class for libgwtsvg samples
  * 
  * @author laaglu
  */
 public abstract class SampleBase {
 
 	public static final String HTML_SRC_DIR = "src/";
 	@UiField
 	public HTML source;
 	@UiField
 	public TabPanel tabPanel;
 	@UiField
 	public SimplePanel panel;
 
 	public abstract Panel getPanel();
 
 	/**
 	 * Load the sample HTML source code
 	 */
 	protected void requestSourceContents(String partialPath) {
 
 		// Request the contents of the file
		// Add a bogus query to bypass the browser cache as advised by:
		// https://developer.mozilla.org/En/Using_XMLHttpRequest#Bypassing_the_cache
		RequestBuilder builder = new RequestBuilder(RequestBuilder.GET, GWT.getModuleBaseURL() + partialPath + "?ts=" + System.currentTimeMillis());
 		builder.setCallback(new RequestCallback() {
 			public void onError(Request request, Throwable exception) {
 				source.setHTML("Cannot find resource");
 			}
 
 			public void onResponseReceived(Request request, Response response) {
 				source.setHTML(response.getText());
 			}
 		});
 
 		// Send the request
 		try {
 			builder.send();
 		} catch (RequestException e) {
 			GWT.log("Cannot fetch HTML source for " + partialPath, e);
 		}
 	}
 }
