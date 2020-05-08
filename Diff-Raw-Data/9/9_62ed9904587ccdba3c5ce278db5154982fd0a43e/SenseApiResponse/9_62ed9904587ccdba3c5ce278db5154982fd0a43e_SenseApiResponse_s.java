 package nl.sense_os.commonsense.lib.client.model.httpresponse;
 
 import com.google.gwt.core.client.JavaScriptObject;
 import com.google.gwt.core.client.JsonUtils;
 import com.google.gwt.safehtml.shared.SafeHtmlUtils;
 
 public class SenseApiResponse extends JavaScriptObject {
 
 	/**
 	 * Creates a SenseApiResponse from the JSON response from CommonSense.
 	 * 
 	 * @param source
 	 *            Raw response from CommonSense.
 	 * @return JavaScriptObject representing the response.
 	 */
 	public static final SenseApiResponse create(String source) {
		String escapedResponse = SafeHtmlUtils.htmlEscape(source);
 		escapedResponse = escapedResponse.replace("&quot;", "\"");
 		if (JsonUtils.safeToEval(escapedResponse)) {
 			return JsonUtils.safeEval(escapedResponse);
 		} else {
 			return null;
 		}
 	}
 
 	protected SenseApiResponse() {
 		// empty protected constructor
 	}
 }
