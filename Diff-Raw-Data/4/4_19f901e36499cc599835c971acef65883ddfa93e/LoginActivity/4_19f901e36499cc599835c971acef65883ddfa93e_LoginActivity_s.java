 /*******************************************************************************
  * Copyright 2011 Google Inc. All Rights Reserved.
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  *******************************************************************************/
 package nl.sense_os.commonsense.login.client.login;
 
 import java.util.List;
 import java.util.Map.Entry;
 import java.util.logging.Logger;
 
 import nl.sense_os.commonsense.common.client.communication.CommonSenseApi;
 import nl.sense_os.commonsense.common.client.communication.SessionManager;
 import nl.sense_os.commonsense.common.client.communication.httpresponse.LoginResponse;
 import nl.sense_os.commonsense.common.client.component.AlertDialogContent;
 import nl.sense_os.commonsense.login.client.LoginClientFactory;
 import nl.sense_os.commonsense.login.client.forgotpassword.ForgotPasswordPlace;
 
 import com.google.gwt.activity.shared.AbstractActivity;
 import com.google.gwt.core.client.JsonUtils;
 import com.google.gwt.event.shared.EventBus;
 import com.google.gwt.http.client.Request;
 import com.google.gwt.http.client.RequestCallback;
 import com.google.gwt.http.client.Response;
 import com.google.gwt.http.client.UrlBuilder;
 import com.google.gwt.safehtml.shared.SafeHtmlUtils;
 import com.google.gwt.user.client.Window.Location;
 import com.google.gwt.user.client.ui.AcceptsOneWidget;
 import com.google.gwt.user.client.ui.DialogBox;
 
 /**
  * Activities are started and stopped by an ActivityManager associated with a container Widget.
  */
 public class LoginActivity extends AbstractActivity implements LoginView.Presenter,
 		AlertDialogContent.Presenter {
 
 	private static final Logger LOG = Logger.getLogger(LoginActivity.class.getName());
 
 	/**
 	 * Used to obtain views, eventBus, placeController. Alternatively, could be injected via GIN.
 	 */
 	private LoginClientFactory clientFactory;
 
 	private LoginView view;
 
 	private DialogBox alertDialog;
 
 	public LoginActivity(LoginPlace place, LoginClientFactory clientFactory) {
 		this.clientFactory = clientFactory;
 	}
 
 	@Override
 	public void dismissAlert() {
 		if (null != alertDialog) {
 			alertDialog.hide();
 		}
 	}
 
 	@Override
 	public void forgotPassword() {
 		// TODO put the username in the token so it can be pre-filled in
 		clientFactory.getPlaceController().goTo(new ForgotPasswordPlace(""));
 	}
 
 	@Override
 	public void googleLogin() {
 		CommonSenseApi.googleLogin();
 	}
 
 	private void goToMainPage() {
 
 		UrlBuilder builder = new UrlBuilder();
 		builder.setProtocol(Location.getProtocol());
 		builder.setHost(Location.getHost());
 		String path = Location.getPath().contains("login.html") ? Location.getPath().replace(
 				"login.html", "index.html") : Location.getPath() + "index.html";
 		builder.setPath(path);
 		for (Entry<String, List<String>> entry : Location.getParameterMap().entrySet()) {
 			if ("session_id".equals(entry.getKey())) {
 				// do not copy the session id parameter
 			} else {
 				builder.setParameter(entry.getKey(), entry.getValue().toArray(new String[0]));
 			}
 		}
 		Location.replace(builder.buildString().replace("127.0.0.1%3A", "127.0.0.1:"));
 	}
 
 	/**
 	 * Sends a login request to the CommonSense API.
 	 * 
 	 * @param username
 	 *            The username to use for log in.
 	 * @param password
 	 *            The password to user for log in. Will be hashed before submission.
 	 */
 	@Override
 	public void login(String username, String password) {
 
 		// prepare request callback
 		RequestCallback callback = new RequestCallback() {
 
 			@Override
 			public void onError(Request request, Throwable exception) {
 				LOG.warning("login error: " + exception.getMessage());
 				onLoginFailure(0, exception);
 			}
 
 			@Override
 			public void onResponseReceived(Request request, Response response) {
 				onLoginResponse(response);
 			}
 		};
 
 		// send request
		CommonSenseApi.login(callback, username, password);
 	}
 
 	private void onAuthenticationFailure() {
 
 		// enable view
 		view.setBusy(false);
 
 		// show alert
 		alertDialog = new DialogBox();
 		alertDialog.setHTML(SafeHtmlUtils.fromSafeConstant("<b>Login failed</b>"));
 
 		AlertDialogContent content = new AlertDialogContent();
 		content.setMessage("Login failed! Invalid username or password.");
 		content.setPresenter(this);
 
 		alertDialog.setWidget(content);
 		alertDialog.center();
 	}
 
 	private void onLoginFailure(int code, Throwable error) {
 
 		// enable view
 		view.setBusy(false);
 
 		// show alert
 		alertDialog = new DialogBox();
 		alertDialog.setHTML(SafeHtmlUtils.fromSafeConstant("<b>Login failed</b>"));
 
 		AlertDialogContent content = new AlertDialogContent();
 		content.setMessage("Login failed! Error: " + code + " (" + error.getMessage() + ")");
 		content.setPresenter(this);
 
 		alertDialog.setWidget(content);
 		alertDialog.center();
 	}
 
 	private void onLoginResponse(Response response) {
 		LOG.finest("POST login response received: " + response.getStatusText());
 		final int statusCode = response.getStatusCode();
 		if (Response.SC_OK == statusCode) {
 			onLoginSuccess(response.getText());
 		} else if (Response.SC_FORBIDDEN == statusCode) {
 			onAuthenticationFailure();
 		} else {
 			LOG.warning("POST login returned incorrect status: " + statusCode);
 			onLoginFailure(statusCode, new Exception("Incorrect response status: " + statusCode));
 		}
 	}
 
 	private void onLoginSuccess(String response) {
 
 		if (response != null) {
 			LOG.fine("LOGIN Success...");
 
 			// try to get "session_id" object
 			String sessionId = null;
 			if (response != null && response.length() > 0 && JsonUtils.safeToEval(response)) {
 				LoginResponse jso = JsonUtils.unsafeEval(response);
 				sessionId = jso.getSessionId();
 			}
 
 			LOG.fine("sessionId is " + sessionId);
 			if (null != sessionId) {
 
 				SessionManager.setSessionId(sessionId);
 
 				goToMainPage();
 
 			} else {
 				onLoginFailure(0, new Exception("Did not receive session ID"));
 			}
 
 		} else {
 			LOG.severe("Error parsing login response: response=null");
 			onLoginFailure(0, new Exception("No response content"));
 		}
 	}
 
 	@Override
 	public void start(AcceptsOneWidget containerWidget, EventBus eventBus) {
 		LOG.info("Start activity: Login");
 
 		view = clientFactory.getLoginView();
 		view.setPresenter(this);
 		containerWidget.setWidget(view.asWidget());
 
         view.setFocus(true);
 	}
 }
