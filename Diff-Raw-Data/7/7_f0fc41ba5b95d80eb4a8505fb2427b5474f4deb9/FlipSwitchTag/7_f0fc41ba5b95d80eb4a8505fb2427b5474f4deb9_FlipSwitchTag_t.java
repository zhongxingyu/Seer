 package org.icemobile.jsp.tags;
 
 import javax.servlet.ServletRequest;
 import javax.servlet.jsp.PageContext;
 import javax.servlet.jsp.tagext.SimpleTagSupport;
 import java.io.IOException;
 import java.io.Writer;
 
 /**
  *
  */
 public class FlipSwitchTag extends SimpleTagSupport {
 
     public static final String FLIPSWITCH_ON_CLASS = "mobi-flip-switch mobi-flip-switch-on ";
     public static final String FLIPSWITCH_OFF_CLASS = "mobi-flip-switch mobi-flip-switch-off ";
    public static final String FLIPSWITCH_TEXT_CLASS = "mobi-flip-switch-txt";
 
     private String id;
     private String style;
     private String styleClass;
 
     private boolean disabled;
     private String value;
     private boolean readOnly;
     private String labelOn;
     private String labelOff;
 
 
     public void doTag() throws IOException {
 
 
         PageContext pageContext = (PageContext) getJspContext();
         Writer out = pageContext.getOut();
         ServletRequest sr = pageContext.getRequest();
 
 
 //        writeJavascriptFile(pageContext, , , );
 
 
         out.write("<a id=\"" + getId() + "\" name=\"" + getId() + "\"");
 
         String styleClass = FLIPSWITCH_OFF_CLASS;
         boolean isChecked = isChecked(getValue());
         if (isChecked) {
             styleClass = FLIPSWITCH_ON_CLASS;
         }
        out.write(" class=\"" + styleClass + " " + this.styleClass +"\"");
        out.write(" style=\"" + this.style +"\"");
         if (isDisabled()) {
             out.write(" disabled=\"true\"");
         }
 
         // write javascript
         StringBuilder builder = new StringBuilder(255);
         builder.append("ice.mobi.flipvalue('").append(getId()).append("', { event: event, elVal: this  }); ");
 
         if (!isDisabled() | !isReadOnly()) {
             out.write(" onclick=\"" + builder.toString() + "\"");
         }
 
         out.write(">");
 
         // write the nested child objects containing both on/off labels.
         out.write("<span class=\"" + FLIPSWITCH_TEXT_CLASS + "\">");
         out.write(getLabelOn());
         out.write("</span>");
 
         // Concat _hidden to the id for uniqueness, but keep the name stock
         // to make the MVC mapping easier
         out.write("<input id=\"" + getId() + "_hidden\" name=\"" + getId() + "\"");
         out.write(" value=\"" + getValue() + "\" type=\"hidden\">");
 
         out.write("<span class=\"" + FLIPSWITCH_TEXT_CLASS + "\">");
         out.write(getLabelOff());
         out.write("</span>");
 
         out.write("</input>");
         out.write("</a>");
 
     }
 
 
     private boolean isChecked(String hiddenValue) {
         if (hiddenValue == null) {
             return false;
         }
         return hiddenValue.equalsIgnoreCase("on") ||
                        hiddenValue.equalsIgnoreCase("yes") ||
                        hiddenValue.equalsIgnoreCase("true");
     }
 
 
     public String getId() {
         return id;
     }
 
     public void setId(String id) {
         this.id = id;
     }
 
     public String getStyle() {
         return style;
     }
 
     public void setStyle(String style) {
         this.style = style;
     }
 
     public String getStyleClass() {
         return styleClass;
     }
 
     public void setStyleClass(String styleClass) {
         this.styleClass = styleClass;
     }
 
     public boolean isDisabled() {
         return disabled;
     }
 
     public void setDisabled(boolean disabled) {
         this.disabled = disabled;
     }
 
     public String getValue() {
         return value;
     }
 
     public void setValue(String value) {
         this.value = value;
     }
 
     public boolean isReadOnly() {
         return readOnly;
     }
 
     public void setReadOnly(boolean readOnly) {
         this.readOnly = readOnly;
     }
 
     public String getLabelOn() {
         return labelOn;
     }
 
     public void setLabelOn(String labelOn) {
         this.labelOn = labelOn;
     }
 
     public String getLabelOff() {
         return labelOff;
     }
 
     public void setLabelOff(String labelOff) {
         this.labelOff = labelOff;
     }
 }
