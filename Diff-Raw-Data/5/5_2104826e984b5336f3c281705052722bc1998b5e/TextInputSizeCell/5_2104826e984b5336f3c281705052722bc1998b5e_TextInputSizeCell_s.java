 package ch.cern.atlas.apvs.client.widget;
 
 import com.google.gwt.cell.client.TextInputCell;
 import com.google.gwt.cell.client.ValueUpdater;
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.dom.client.Element;
 import com.google.gwt.dom.client.NativeEvent;
 import com.google.gwt.safehtml.client.SafeHtmlTemplates;
 import com.google.gwt.safehtml.shared.SafeHtml;
 import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
 
 public class TextInputSizeCell extends TextInputCell {
 
 	/* for running inside the plugin needs to be public */
     interface MyTextInputCellTemplate extends SafeHtmlTemplates {
		@Template("<input type=\"text\" value=\"{0}\" tabindex=\"-1\" size=\"{1}\"></input>")
 		SafeHtml input(String value, Integer size);
	}
 
 	private static MyTextInputCellTemplate textInputCellTemplate;
 	private int size;
 	
 	public TextInputSizeCell(int size) {
 		this.size = size;
 		
 		if (textInputCellTemplate == null) {
 			textInputCellTemplate = GWT
 					.create(MyTextInputCellTemplate.class);
 		}
 	}
 
 	@Override
 	protected void onEnterKeyDown(
 			com.google.gwt.cell.client.Cell.Context context,
 			Element parent, String value, NativeEvent event,
 			ValueUpdater<String> valueUpdater) {
 		super.onEnterKeyDown(context, parent, value, event, valueUpdater);
 	}
 
 	@Override
 	public void render(Context context, String value, SafeHtmlBuilder sb) {
 		// Get the view data.
 		Object key = context.getKey();
 		ViewData viewData = getViewData(key);
 		if (viewData != null && viewData.getCurrentValue().equals(value)) {
 			clearViewData(key);
 			viewData = null;
 		}
 
 		String s = (viewData != null) ? viewData.getCurrentValue() : value;
 		if (s != null) {
 			sb.append(textInputCellTemplate.input(s, size));
 		} else {
 			sb.appendHtmlConstant("<input type=\"text\" tabindex=\"-1\" size=\""+size+"\"></input>");
 		}
 	}
 }
