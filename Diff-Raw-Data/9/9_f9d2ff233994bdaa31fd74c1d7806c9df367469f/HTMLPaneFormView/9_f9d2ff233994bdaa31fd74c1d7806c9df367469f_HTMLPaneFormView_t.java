 package net.rptools.maptool.client.ui.htmlframe;
 
 import java.io.UnsupportedEncodingException;
 import java.net.URLDecoder;
import java.net.URLEncoder;
 
 import javax.swing.text.AttributeSet;
 import javax.swing.text.Element;
 import javax.swing.text.StyleConstants;
 import javax.swing.text.html.FormView;
 import javax.swing.text.html.HTML;
 
 import net.sf.json.JSONObject;
 
 public class HTMLPaneFormView extends FormView {
 	private HTMLPane htmlPane;
 	
 	/**
 	 * Creates a new HTMLPaneFormView.
 	 * @param elem The element this is a view for.
 	 * @param pane The HTMLPane this element resides on.
 	 */
 	public HTMLPaneFormView(Element elem, HTMLPane pane) {
 		super(elem);
 		htmlPane = pane;
 	}
 
 	
 	@Override
 	protected void submitData(String data) {
 		// Find the form
 		Element formElement = null;
 		for (Element e = getElement(); e != null; e = e.getParentElement()) {
 			if (e.getAttributes().getAttribute(StyleConstants.NameAttribute) == HTML.Tag.FORM) {
 				formElement = e;
 				break;
 			}
 		}
 		
 		if (formElement != null) {
 			AttributeSet att = formElement.getAttributes();
 			String action = att.getAttribute(HTML.Attribute.ACTION).toString();
 			String method;
 			if (att.getAttribute(HTML.Attribute.METHOD) != null) {
 				method = att.getAttribute(HTML.Attribute.METHOD).toString();
 			} else {
 				method = "get";
 			}
 			action = action == null ? "" : action;
 			method = method == null ? "" : method.toLowerCase();
 			if (method.equals("json")) {
 				JSONObject jobj = new JSONObject();
 				String[] values = data.split("&");
 				for (String v : values) {
 					String[] dataStr = v.split("=");
 					if (dataStr.length == 1) {
 						jobj.put(dataStr[0], "");												
 					} else if (dataStr.length > 2) {
 						jobj.put(dataStr[0], dataStr[1]);												
 					} else {
 						try {
 							jobj.put(dataStr[0], URLDecoder.decode(dataStr[1], "utf8"));
 						} catch (UnsupportedEncodingException e) {
 							// Use the raw data.
 						jobj.put(dataStr[0], dataStr[1]);						
 						}
 					}
 				}
				try {
					data = URLEncoder.encode(jobj.toString(), "utf8");
				} catch (UnsupportedEncodingException e) {
					// Use the raw data.
					data = jobj.toString();
				}
 			}
 			htmlPane.doSubmit(method, action, data);
 		}
 		
 		
 	}
 	
 	@Override
 	protected void imageSubmit(String data) {
 		submitData(data);
 	}
 	
 
 }
