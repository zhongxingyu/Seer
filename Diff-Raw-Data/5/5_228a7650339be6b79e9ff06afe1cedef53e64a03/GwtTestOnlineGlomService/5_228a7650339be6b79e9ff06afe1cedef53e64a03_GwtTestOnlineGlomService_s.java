 /*
  * Copyright (C) 2012 Openismus GmbH
  *
  * This file is part of GWT-Glom.
  *
  * GWT-Glom is free software: you can redistribute it and/or modify it
  * under the terms of the GNU Lesser General Public License as published by the
  * Free Software Foundation, either version 3 of the License, or (at your
  * option) any later version.
  *
  * GWT-Glom is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
  * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
  * for more details.
  *
  * You should have received a copy of the GNU Lesser General Public License
  * along with GWT-Glom.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package org.glom.web.client;
 
 import org.glom.web.shared.Documents;
 import org.junit.Test;
 
 import com.google.gwt.junit.client.GWTTestCase;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 
 /**
  * @author Murray Cumming <murrayc@openismus.com>
  *
  */
 public class GwtTestOnlineGlomService extends GWTTestCase {
 
 	@Test
 	public void test() {
 		// Setup an asynchronous event handler.
 		final AsyncCallback<Documents> callback = new AsyncCallback<Documents>() {
 			@Override
 			public void onFailure(final Throwable caught) {
				// Try to get an error message. Most likely this won't work but it's worth a try.
				//getAndSetErrorMessage();
				
				finishTest();
 			}
 
 			@Override
 			public void onSuccess(final Documents documents) {
 				if (documents.getCount() > 0) {
 					for (int i = 0; i < documents.getCount(); i++) {
 						String id = documents.getDocumentID(i);
 						String title = documents.getTitle(i);
 					}
 				} else {
 					//getAndSetErrorMessage();
 				}
 				
 				finishTest();
 			}
 		};
 		
 		delayTestFinish(500);
 
 		OnlineGlomServiceAsync service = OnlineGlomServiceAsync.Util.getInstance();
 		assertNotNull(service);
 		service.getDocuments(callback);
 	}
 	
 	@Override
 	public String getModuleName() {
 		return "org.glom.web.OnlineGlom";
 	}
 
 }
