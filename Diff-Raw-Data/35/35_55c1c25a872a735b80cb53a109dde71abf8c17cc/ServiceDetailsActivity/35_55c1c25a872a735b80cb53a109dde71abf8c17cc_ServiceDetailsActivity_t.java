 /**
  * @author Nigel Cook
  * @author Douglas Tondin
  * @author Leonardo Amado
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
 package n3phele.client.presenter;
 
import n3phele.client.AppPlaceHistoryMapper;
 import n3phele.client.CacheManager;
 import n3phele.client.ClientFactory;
 import n3phele.client.model.Account;
 import n3phele.client.model.CloudProcess;
 import n3phele.client.model.Collection;
import n3phele.client.model.Stack;
 import n3phele.client.model.StackServiceAction;
 import n3phele.client.presenter.helpers.AuthenticatedRequestFactory;
 import n3phele.client.view.ServiceDetailsView;
 import n3phele.client.model.CommandCloudAccount;
 
 import com.google.gwt.activity.shared.AbstractActivity;
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.event.shared.EventBus;
 import com.google.gwt.http.client.Request;
 import com.google.gwt.http.client.RequestBuilder;
 import com.google.gwt.http.client.RequestCallback;
 import com.google.gwt.http.client.RequestException;
 import com.google.gwt.http.client.Response;
 import com.google.gwt.user.client.History;
 import com.google.gwt.user.client.ui.AcceptsOneWidget;
 
 public class ServiceDetailsActivity extends AbstractActivity {
 	private final String processUri;
	private final AppPlaceHistoryMapper historyMapper;
 	private final ServiceDetailsView display;	
 	private final CacheManager cacheManager;
 	private String serviceUri;
 
 	public ServiceDetailsActivity(String processUri, ClientFactory factory) {
		historyMapper = factory.getHistoryMapper();
 		this.processUri = processUri;
 		this.display = factory.getServiceDetailsView();
 		this.cacheManager = factory.getCacheManager();
 	}
 	
 	@Override
 	public void start(AcceptsOneWidget panel, EventBus eventBus) {
		display.setPresenter(this);
 		panel.setWidget(display);
 		this.getProcess();
 		if(processUri == null || processUri.length()==0 || processUri.equals("null")) {
 			this.getProcess();
 		} 
 	}
 
 	@Override
 	public String mayStop() {
 		return null;
 	}
 	@Override
 	public void onCancel() {
 
 	}
 
 	public void goToPrevious() {
 		History.back();
 	}
 
	public void onSelect(Stack stack){
		History.newItem(historyMapper.getToken(new StackDetailsPlace(serviceUri,stack.getId())));
 	}
 
 	
 
 	/*
 	 * Rest calls
 	 * ----------------
 	 */
 
 	
 	public void getProcess() {
 		// Send request to server and catch any errors.
 		RequestBuilder builder = AuthenticatedRequestFactory.newRequest(RequestBuilder.GET, processUri);
 		try {
 			Request request = builder.sendRequest(null, new RequestCallback() {
 				public void onError(Request request, Throwable exception) {
 					GWT.log("Got error");
 				}
 
 				public void onResponseReceived(Request request, Response response) {
 					GWT.log("Got reply");
 					if (200 == response.getStatusCode()) {
 						CloudProcess cloud = CloudProcess.asCloudProcess(response.getText());
 						serviceUri = cloud.getAction();
 						getAction();
 					} else {
 
 					}
 				}
 
 			});
 		} catch (RequestException e) {
 		}
 	}
 	public void getAction() {
 		// Send request to server and catch any errors.
 		RequestBuilder builder = AuthenticatedRequestFactory.newRequest(RequestBuilder.GET, serviceUri);
 		try {
 			Request request = builder.sendRequest(null, new RequestCallback() {
 				public void onError(Request request, Throwable exception) {
 					GWT.log("Got error");
 				}
 
 				public void onResponseReceived(Request request, Response response) {
 					GWT.log("Got reply");
 					if (200 == response.getStatusCode()) {
 						StackServiceAction stackAction = StackServiceAction.asAction(response.getText());
 						display.setStackAction(stackAction);
 					} 
 				}
 
 			});
 		} catch (RequestException e) {
 			GWT.log("Got error");
 		}
 	}
 	
 
 
 }
