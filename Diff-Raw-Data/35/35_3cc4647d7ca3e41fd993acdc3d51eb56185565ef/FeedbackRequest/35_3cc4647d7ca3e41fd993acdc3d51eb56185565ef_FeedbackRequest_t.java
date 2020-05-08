 /*-
  * Copyright 2012 Diamond Light Source Ltd.
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *   http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package uk.ac.diamond.scisoft.feedback;
 
 import java.io.File;
 
 import org.apache.http.HttpHost;
 import org.apache.http.HttpResponse;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.conn.params.ConnRoutePNames;
 import org.apache.http.entity.mime.MultipartEntity;
 import org.apache.http.entity.mime.content.FileBody;
 import org.apache.http.entity.mime.content.StringBody;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.ui.PlatformUI;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 
 /**
  * Class that submits a request to the DAWNFeedback Servlet
  */
 public class FeedbackRequest {
 
 	private static Logger logger = LoggerFactory.getLogger(FeedbackRequest.class);
 	//this is the URL of the GAE servlet
 	private static final String SERVLET_URL = "http://dawnsci-feedback.appspot.com/dawnfeedback";
 	//proxy
 	private static String host;
 	private static int port;
 	
 
 	/**
 	 * Method used to submit a form data/file through HTTP to a GAE servlet
 	 * @param email
 	 * @param to
 	 * @param name
 	 * @param subject
 	 * @param messageBody
 	 * @param file
 	 */
 	public static void doRequest(String email, String to, String name, String subject, String messageBody, File file) {
 
 		DefaultHttpClient httpclient = new DefaultHttpClient();
 
 		FeedbackProxy.init();
 		host = FeedbackProxy.getHost();
 		port = FeedbackProxy.getPort();
 
 		// if there is a proxy
 		if(host != null){
 			HttpHost proxy = new HttpHost(host, port, "http");
 			httpclient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);
 		}
 
 		try {
 			HttpPost httpost = new HttpPost(SERVLET_URL);
 
 			MultipartEntity entity = new MultipartEntity();
 			entity.addPart("name", new StringBody(name));
 			entity.addPart("email", new StringBody(email));
 			entity.addPart("to", new StringBody(to));
 			entity.addPart("subject", new StringBody(subject));
 			entity.addPart("message", new StringBody(messageBody));
 			entity.addPart("log.html", new FileBody(file));
 			httpost.setEntity(entity);
 			
 			//HttpPost post = new HttpPost("http://dawnsci-feedback.appspot.com/dawnfeedback?name=baha&email=baha@email.com&subject=thisisasubject&message=thisisthemessage");
 			HttpResponse response = httpclient.execute(httpost);
			final String reasonPhrase = response.getStatusLine().getReasonPhrase();
			int statusCode = response.getStatusLine().getStatusCode();
			if(statusCode==200){
				logger.debug("Feedback Sent");
				Display.getDefault().asyncExec(new Runnable() {
					@Override
					public void run() {
						MessageDialog.openInformation(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
								"Feedback successfully sent", "Thank you for your contribution");
					}
				});
			} else {
				logger.debug("Feedback email not sent - HTTP response: "+ reasonPhrase);
				Display.getDefault().asyncExec(new Runnable() {
					@Override
					public void run() {
						MessageDialog.openWarning(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
								"Feedback not sent!", "The response from the server is the following:\n"+reasonPhrase+"\nPlease submit your feedback using the online feedback form available at http://dawnsci-feedback.appspot.com/");
					}
				});
			}
			
 			logger.debug("HTTP Response: " + response.getStatusLine());
 		} catch (Exception e) {
 			logger.error("Feedback email not sent", e);
 			Display.getDefault().asyncExec(new Runnable() {
 				@Override
 				public void run() {
					MessageDialog.openWarning(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
							"Feedback not sent!", "Please check that you have an Internet connection. If the feedback is still not working, please submit your feedback using the online feedback form available at http://dawnsci-feedback.appspot.com/");
 				}
 			});
 		} finally {
 			// When HttpClient instance is no longer needed,
 			// shut down the connection manager to ensure
 			// immediate deallocation of all system resources
 			httpclient.getConnectionManager().shutdown();
 		}
 	}
 }
