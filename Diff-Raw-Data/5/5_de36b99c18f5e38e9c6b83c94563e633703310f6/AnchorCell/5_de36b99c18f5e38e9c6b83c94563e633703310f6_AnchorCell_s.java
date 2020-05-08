 package org.accesointeligente.client;
 
 import com.google.gwt.cell.client.AbstractCell;
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.safehtml.client.SafeHtmlTemplates;
 import com.google.gwt.safehtml.client.SafeHtmlTemplates.Template;
 import com.google.gwt.safehtml.shared.SafeHtml;
 import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
 
 public class AnchorCell extends AbstractCell<AnchorCellParams> {
 
 	interface Template extends SafeHtmlTemplates {
 		@Template("<a href=\"{0}\" title=\"{1}\">{2}</a>")
		SafeHtml anchor(String token, String title, String value);
 	}
 
 	private static Template template;
 
 	/**
 	 * Construct a new ImageCell.
 	 */
 	public AnchorCell() {
 		if (template == null) {
 			template = GWT.create(Template.class);
 		}
 	}
 
 	@Override
 	public void render(Context context, AnchorCellParams value, SafeHtmlBuilder sb) {
 		if (value != null) {
 			// The template will sanitize the URI.
			sb.append(template.anchor(value.getToken(), value.getTitle(), value.getValue()));
 		}
 	}
 }
