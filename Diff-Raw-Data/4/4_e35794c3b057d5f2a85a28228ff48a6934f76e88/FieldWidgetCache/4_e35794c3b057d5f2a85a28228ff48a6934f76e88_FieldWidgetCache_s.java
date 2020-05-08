 package org.iucn.sis.client.api.caches;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import org.iucn.sis.client.api.container.SISClientBase;
 import org.iucn.sis.client.api.utils.UriBase;
 import org.iucn.sis.shared.api.debug.Debug;
 import org.iucn.sis.shared.api.displays.Display;
 import org.iucn.sis.shared.api.models.Field;
 import org.iucn.sis.shared.api.utils.FieldParser;
 
 import com.solertium.lwxml.shared.GenericCallback;
 import com.solertium.lwxml.shared.NativeDocument;
 import com.solertium.lwxml.shared.NativeElement;
 import com.solertium.lwxml.shared.NativeNode;
 import com.solertium.lwxml.shared.NativeNodeList;
 import com.solertium.util.portable.XMLWritingUtils;
 
 public class FieldWidgetCache {
 	public static final FieldWidgetCache impl = new FieldWidgetCache();
 
 	private final Map<String, Display> widgetMap;
 	private final FieldParser fieldParser;
 
 	private FieldWidgetCache() {
 		fieldParser = new FieldParser();
 		widgetMap = new HashMap<String, Display>();
 	}
 
 	// TODO: THIS IS BEING CALLED TOO MANY TIMES!! Fix it.
 	public void addAssessmentToDisplay(Display display) {
 		if (AssessmentCache.impl.getCurrentAssessment() != null && display != null) {
 			Field field = AssessmentCache.impl.getCurrentAssessment().getField(display.getCanonicalName());
 			if (field == null)
 				field = new Field(display.getCanonicalName(), AssessmentCache.impl.getCurrentAssessment());
 		
 			display.setData(field);
 		}
 	}
 
 	private void doListFetch(Collection<String> names, final GenericCallback<String> wayBack) {
 		final StringBuilder builder = new StringBuilder();
 		builder.append("<fields>");
 		for (String name : names)
 			builder.append(XMLWritingUtils.writeTag("field", name));
 		builder.append("</fields>");
 		
 		final NativeDocument doc = SISClientBase.getHttpBasicNativeDocument();
 		final String uri = UriBase.getInstance().getSISBase() + "/application/schema/" + 
			AssessmentCache.impl.getCurrentAssessment().getSchema() + "/field";
 		doc.post(uri, builder.toString(), new GenericCallback<String>() {
 			public void onFailure(Throwable caught) {
 				wayBack.onFailure(caught);
 			}
 			public void onSuccess(String arg0) {
 				final NativeNodeList displays = doc.getDocumentElement().getChildNodes();
 				for (int i = 0; i < displays.getLength(); i++) {
 					final NativeNode current = displays.item(i);
 					if (current.getNodeType() != NativeNode.TEXT_NODE && current instanceof NativeElement) {
 						Display dis = fieldParser.parseField((NativeElement)current);
 						if (dis != null && dis.getCanonicalName() != null && !dis.getCanonicalName().equals("")) {
 							widgetMap.put(dis.getCanonicalName(), dis);
 						} else
 							Debug.println("Parsed a " + "display with null canonical " + 
 								"name. Description is: {0}", dis.getDescription());
 					}
 				}
 
 				wayBack.onSuccess("OK");
 			}
 		});
 	}
 
 	public void doLogout() {
 		widgetMap.clear();
 	}
 
 	/**
 	 * Gets the Widget from the master list, or returns null if it's not found.
 	 * 
 	 * This DOES NOT attempt to fetch the field description from the server, and
 	 * WILL NOT build the Widget if it has not yet been built (see
 	 * fetchField(...)).
 	 * 
 	 * @param canonicalName
 	 * @return Display object - MAY BE NULL
 	 */
 	public Display get(String canonicalName) {
 		Display cur = widgetMap.get(canonicalName);
 		addAssessmentToDisplay(cur);
 
 		return cur;
 	}
 
 	/**
 	 * Fetches a list of fields in one call. Supplied argument should be the
 	 * canonical names of the fields separated by commas. If the asked-for
 	 * fields are found in the master list already, this function will call the
 	 * wayBack.onSuccess() immediately.
 	 * 
 	 * @param fieldName
 	 * @param wayBack
 	 */
 	public void prefetchList(final Collection<String> names, final GenericCallback<String> wayBack) {
 		final List<String> uncachedNames = new ArrayList<String>();
 		
 		for (String fieldName : names) 
 			if (!widgetMap.containsKey(fieldName)) 
 				uncachedNames.add(fieldName);
 		
 		if (uncachedNames.isEmpty())
 			wayBack.onSuccess("OK");
 		else
 			doListFetch(uncachedNames, wayBack);
 	}
 
 	public void resetWidgetContents() {
 		if (AssessmentCache.impl.getCurrentAssessment() != null)
 			for (Iterator<Display> iter = widgetMap.values().iterator(); iter.hasNext();)
 				addAssessmentToDisplay(iter.next());
 	}
 
 }
