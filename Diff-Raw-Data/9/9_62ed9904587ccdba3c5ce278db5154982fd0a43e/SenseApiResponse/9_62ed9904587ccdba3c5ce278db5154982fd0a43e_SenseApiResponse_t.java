 package nl.sense_os.commonsense.lib.client.model.httpresponse;
 
 import com.google.gwt.core.client.JavaScriptObject;
 import com.google.gwt.core.client.JsonUtils;
 import com.google.gwt.safehtml.shared.SafeHtmlUtils;
 
/**
 * Base class for JavaScriptObject overlays on response data from the CommonSense API. Use the
 * {@link #create(String)} method to safely escape the content before evaluating the JSON.
 * 
 * @author Steven Mulder <steven@sense-os.nl>
 */
 public class SenseApiResponse extends JavaScriptObject {
 
 	/**
 	 * Creates a SenseApiResponse from the JSON response from CommonSense.
 	 * 
 	 * @param source
 	 *            Raw response from CommonSense.
 	 * @return JavaScriptObject representing the response.
 	 */
 	public static final SenseApiResponse create(String source) {
		String escapedResponse = SafeHtmlUtils.htmlEscape("" + source);
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
