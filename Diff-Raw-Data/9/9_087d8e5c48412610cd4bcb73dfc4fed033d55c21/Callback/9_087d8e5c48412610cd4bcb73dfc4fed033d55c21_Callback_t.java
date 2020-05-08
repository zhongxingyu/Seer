 package kornell.api.client;
 
 import static com.google.gwt.http.client.Response.SC_FORBIDDEN;
 import static com.google.gwt.http.client.Response.SC_NOT_FOUND;
 import static com.google.gwt.http.client.Response.SC_OK;
 import static com.google.gwt.http.client.Response.SC_UNAUTHORIZED;
 import kornell.core.shared.data.BeanFactory;
 import kornell.core.shared.to.TOFactory;
 
 import com.google.gwt.core.shared.GWT;
 import com.google.gwt.http.client.Request;
 import com.google.gwt.http.client.RequestCallback;
 import com.google.gwt.http.client.Response;
 import com.google.gwt.json.client.JSONParser;
 import com.google.gwt.json.client.JSONValue;
 import com.google.web.bindery.autobean.shared.AutoBean;
 import com.google.web.bindery.autobean.shared.AutoBeanCodex;
 
 public class Callback<T> implements RequestCallback {
 	private static final BeanFactory beans = GWT.create(BeanFactory.class);
 	private static final TOFactory tos = GWT.create(TOFactory.class);
 	private MediaTypes mimeTypes = new MediaTypes();
 
 	@Override
 	public void onResponseReceived(Request request, Response response) {
 		if (!isTrusted(response))
 			throw new RuntimeException("Won't touch untrusted response");
 		int statusCode = response.getStatusCode();
 		switch (statusCode) {
 		case SC_OK:
 			ok(response);
 			break;
 		case SC_FORBIDDEN:
 			forbidden();
 			break;
 		case SC_UNAUTHORIZED:
 			unauthorized();
 			break;
 		case SC_NOT_FOUND:
 			notFound();
 			break;
 		case 0:
 			cancelled();
 			break;
 		default:
 			GWT.log("Got a response, but don't know what to do about it");
 			break;
 		}
 	}
 
 	protected void notFound() {
 		
 	}
 
 	protected void ok(Response response) {
 		dispatchByMimeType(response);
 	}
 
 	private void dispatchByMimeType(Response response) {
 		String contentType = response.getHeader("Content-Type").toLowerCase();
 		String responseText = response.getText();
 
 		if (contentType.contains("json")) {
 			if (mimeTypes.containsKey(contentType)) {
 				@SuppressWarnings("unchecked")
 				Class<T> clazz = (Class<T>) mimeTypes.get(contentType);
 
 				AutoBean<T> bean = null;
 				if (contentType.contains(".to.")) {
 					bean = AutoBeanCodex.decode(tos, clazz, responseText);
 				} else {
 					bean = AutoBeanCodex.decode(beans, clazz, responseText);
 				}
				T unwrapped = bean.as();
				ok(unwrapped);
 			} else
 				ok(Callback.parseJson(responseText));
 
 		} else
 			ok();
 	}
 
 	protected void ok(T to) {
 	}
 
 	protected void ok(JSONValue json) {
 	}
 
 	private static JSONValue parseJson(String jsonStr) {
 		return JSONParser.parseStrict(jsonStr);
 	}
 
 	protected boolean isTrusted(Response response) {
 		return true;
 	}
 
 	protected void cancelled() {
 		GWT.log("Your request was cancelled, probably for the same origin policy, check your cross origin resourse sharing configuration.");
 	}
 
 	protected void unauthorized() {
 	}
 
 	protected void forbidden() {
 	}
 
 	protected void ok() {
 	}
 
 	@Override
 	public void onError(Request request, Throwable exception) {
 		error(request, exception);
 
 	}
 
 	private void error(Request request, Throwable exception) {
 		GWT.log("Error!", exception);
 	}
 
 }
