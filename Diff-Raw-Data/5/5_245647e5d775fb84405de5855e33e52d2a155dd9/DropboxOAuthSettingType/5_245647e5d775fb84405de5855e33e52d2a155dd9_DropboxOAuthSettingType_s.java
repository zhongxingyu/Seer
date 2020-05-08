 package org.sleeksnap.dropbox;
 
 import java.awt.Dimension;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.net.URL;
 import java.util.HashMap;
 import java.util.Map;
 
 import javax.swing.JButton;
 import javax.swing.JComponent;
 import javax.swing.JOptionPane;
 
 import org.json.JSONObject;
 import org.nikkii.embedhttp.HttpServer;
 import org.nikkii.embedhttp.handler.HttpRequestHandler;
 import org.nikkii.embedhttp.impl.HttpRequest;
 import org.nikkii.embedhttp.impl.HttpResponse;
 import org.nikkii.embedhttp.impl.HttpStatus;
 import org.sleeksnap.uploaders.settings.UploaderSettingType;
 import org.sleeksnap.util.Util;
 
 import com.dropbox.core.DbxAccountInfo;
 import com.dropbox.core.DbxAppInfo;
 import com.dropbox.core.DbxAuthFinish;
 import com.dropbox.core.DbxClient;
 import com.dropbox.core.DbxException;
 import com.dropbox.core.DbxSessionStore;
 import com.dropbox.core.DbxWebAuth;
 
 public class DropboxOAuthSettingType implements UploaderSettingType {
 
 	private JSONObject obj = null;
 
 	@Override
 	public JComponent constructComponent(String defaultValue) {
 		final JButton button = new JButton("Click to authorize");
 		button.setPreferredSize(new Dimension(200, 20));
 		button.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				try {
 					dropboxAuthentication(button);
 				} catch (Exception e1) {
 					e1.printStackTrace();
 				}
 			}
 		});
 		button.updateUI();
 		return button;
 	}
 
 	protected void dropboxAuthentication(final JComponent component) throws Exception {
 		DbxAppInfo appInfo = new DbxAppInfo(DropboxUploader.APP_KEY, DropboxUploader.APP_SECRET);
 
 		String redirectUri = "http://localhost:8581/dropbox-auth-finish";
 		String sessionKey = "dropbox-auth-csrf-token";
 
 		DbxSessionStore csrfStore = new DbxSessionStore() {
 
 			private String csrfToken = null;
 
 			@Override
 			public void clear() {
 				csrfToken = null;
 			}
 
 			@Override
 			public String get() {
 				return csrfToken;
 			}
 
 			@Override
 			public void set(String arg0) {
 				this.csrfToken = arg0;
 			}
 		};
 
 		csrfStore.set(sessionKey);
 
 		final DbxWebAuth webAuth = new DbxWebAuth(DropboxUploader.REQUEST_CONFIG, appInfo, redirectUri, csrfStore);
 
 		String authUrl = webAuth.start();
 
 		Util.openURL(new URL(authUrl));
 
 		final HttpServer server = new HttpServer(8581);
 		server.addRequestHandler(new HttpRequestHandler() {
 			@Override
 			public HttpResponse handleRequest(HttpRequest request) {
 				if (request.getUri().equals("/dropbox-auth-finish")) {
 					Map<String, Object> requestData = request.getGetData();
 
 					Map<String, String[]> queryParams = new HashMap<String, String[]>();
 					queryParams.put("state", new String[] { (String) requestData.get("state") });
 					queryParams.put("code", new String[] { (String) requestData.get("code") });
 
 					try {
 						DbxAuthFinish authFinish = webAuth.finish(queryParams);
 
 						authorized(component, authFinish);
 					} catch (Exception e) {
 						e.printStackTrace();
 					}
 				}
 				try {
					return new HttpResponse(HttpStatus.OK, "Thank you for authenticating! You may now close this window and return to Sleeksnap.");
 				} finally {
 					server.stop();
 				}
 			}
 		});
 		server.start();
 	}
 
 	public void authorized(final JComponent component, DbxAuthFinish authFinish) {
 		try {
 			DbxClient client = new DbxClient(DropboxUploader.REQUEST_CONFIG, authFinish.accessToken);
 
 			final DbxAccountInfo info = client.getAccountInfo();
 
 			obj = new JSONObject();
 
 			obj.put("displayName", info.displayName);
 			obj.put("accessToken", authFinish.accessToken);
 
 			new Thread(new Runnable() {
 				public void run() {
					component.requestFocus();
 					JOptionPane.showMessageDialog(component, "Thank you for authorizing " + info.displayName + "! You can now use Dropbox to upload Text, Files, and Screenshots after you confirm the settings!", "Thank you", JOptionPane.INFORMATION_MESSAGE);
 				}
 			}).start();
 		} catch (DbxException e) {
 			e.printStackTrace();
 		}
 	}
 
 	@Override
 	public void setValue(JComponent component, Object value) {
 		if (value instanceof JSONObject) {
 			JSONObject obj = (JSONObject) value;
 
 			((JButton) component).setText(obj.getString("displayName"));
 
 			this.obj = obj;
 		}
 	}
 
 	@Override
 	public Object getValue(JComponent component) {
 		return obj;
 	}
 }
