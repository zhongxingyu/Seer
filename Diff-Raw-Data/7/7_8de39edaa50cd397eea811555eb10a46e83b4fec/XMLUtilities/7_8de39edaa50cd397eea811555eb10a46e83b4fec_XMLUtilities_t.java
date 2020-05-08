 package com.qeevee.gq.xml;
 
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import org.dom4j.Element;
 import org.dom4j.Node;
 
 import android.util.Log;
 import android.webkit.WebView;
 
 import com.qeevee.ui.WebViewUtil;
 
 import edu.bonn.mobilegaming.geoquest.GeoQuestApp;
 import edu.bonn.mobilegaming.geoquest.Variables;
 import edu.ubonn.gq.extmissionhelper.ExternalMissionHelper;
 
 public class XMLUtilities {
 
 	/**
 	 * Used to extract the parameters for external missions.
 	 * 
 	 * Reads the given XML node element, which must represent the
 	 * <code>parameters</code> tag. All found arguments (i.e. input parameters
 	 * as well as result declarations with their default values which are send
 	 * to the Mission) are stored in the map returned.
 	 * 
 	 * @param parametersElement
 	 *            a <code>parameters</code> xml tag from the game specification
 	 *            (e.g. in a StartExternelMission action or in an
 	 *            ExternalMission itself.
 	 * @return a map containing all arguments (aka input parameters) with
 	 *         <code>name</code> (preceded by "arg:") as key and
 	 *         <code>value</code> as value and all result declarations with with
 	 *         <code>name</code> (preceded by "res:") as key and
 	 *         <code>default</code> as value.
 	 */
 	@SuppressWarnings("unchecked")
 	public static Map<String, String> extractParameters(
 			Element parametersElement) {
 		Map<String, String> inputParameters = null;
 		// Arguments:
 		List<Element> paramElements = parametersElement.selectNodes("argument");
 		if (paramElements != null && paramElements.size() > 0) {
 			inputParameters = new HashMap<String, String>();
 			for (Iterator<Element> iterator = paramElements.iterator(); iterator
 					.hasNext();) {
 				Element currentParamElement = iterator.next();
 				String attributeValue = currentParamElement
 						.attributeValue("value");
 				if (attributeValue.startsWith("$")) {
 					attributeValue = (String) Variables
 							.getValue(attributeValue).toString();
 				}
 				inputParameters.put(ExternalMissionHelper.ARG_PREFIX
 						+ currentParamElement.attributeValue("name"),
 						attributeValue);
 			}
 		}
 		paramElements = parametersElement.selectNodes("result");
 		if (paramElements != null && paramElements.size() > 0) {
 			for (Iterator<Element> iterator = paramElements.iterator(); iterator
 					.hasNext();) {
 				Element currentParamElement = iterator.next();
 				inputParameters.put(ExternalMissionHelper.RES_PREFIX
 						+ currentParamElement.attributeValue("name"),
 						currentParamElement.attributeValue("default"));
 			}
 		}
 		return inputParameters;
 	}
 
 	@SuppressWarnings("rawtypes")
 	public static String getXMLContent(Element element) {
 		StringBuilder builder = new StringBuilder();
 		List contentParts = element.content();
 		for (Iterator iterator = contentParts.iterator(); iterator.hasNext();) {
 			Node node = (Node) iterator.next();
 			builder.append(node.asXML());
 		}
 		return builder.toString().replaceAll("\\s+", " ");
 	}
 
 	public static final int NECESSARY_ATTRIBUTE = 0;
 	public static final int OPTIONAL_ATTRIBUTE = 1;
 	private static final String TAG = XMLUtilities.class.getSimpleName();
 
 	/**
 	 * 
 	 * @param attributeName
 	 * @param defaultAsResourceID
 	 *            either NECESSARY_ATTRIBUTE, OPTIONAL_ATTRIBUTE or a valid
 	 *            resource ID which points to the default value for this
 	 *            attribute as a string resource.
 	 * @param xmlElement
 	 *            the XML element which defines the attribute, e.g. representing
 	 *            a mission or an item in game.xml
 	 * 
 	 * @return the corresponding attribute value as specified in the game.xml or
 	 *         null if the attribute is optional and not specified
 	 * @throws IllegalArgumentException
 	 *             if the attribute is necessary but not given in the game.xml
 	 */
 	public static CharSequence getAttribute(String attributeName,
 			int defaultAsResourceID, Element xmlElement) {
		if (xmlElement == null
				|| xmlElement.attributeValue(attributeName) == null)
 			if (defaultAsResourceID == NECESSARY_ATTRIBUTE) {
 				// attribute needed but not found => error in game.xml:
 				IllegalArgumentException e = new IllegalArgumentException(
 						"Necessary attribute \"" + attributeName
 								+ "\" missing. Rework game specification.");
 				Log.e(TAG, e.toString());
 				throw e;
 			} else if (defaultAsResourceID == OPTIONAL_ATTRIBUTE) {
 				// optional attribute not set in game.xml => return null:
 				return null;
 			} else
 				// attribute not set in game.xml but given as parameter => use
 				// referenced resource as default and return its value:
 				return GeoQuestApp.getInstance().getText(defaultAsResourceID);
 		else
			return (CharSequence) xmlElement.attributeValue(attributeName);
 	}
 
 	/**
 	 * Loads the content (including html tags) of the given xml element into the
 	 * given webview.
 	 * 
 	 * @param view
 	 * @param element
 	 */
 	public static void loadElementContentForWebView(WebView view,
 			Element element) {
 		WebViewUtil.showTextInWebView(view, getXMLContent(element));
 	}
 
 }
