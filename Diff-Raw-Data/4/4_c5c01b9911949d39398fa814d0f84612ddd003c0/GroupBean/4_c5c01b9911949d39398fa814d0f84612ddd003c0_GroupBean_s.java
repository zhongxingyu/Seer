 package org.gridsphere.provider.portletui.beans;
 
 /**
  * The <code>GroupBean</code> provides a way to visually group elements with an optional label.
  */
 public class GroupBean extends BaseComponentBean implements TagBean {
 
     private String label = null;
     private String height = null;
     private String width = null;
 
     public String getLabel() {
         return label;
     }
 
     public void setLabel(String label) {
         this.label = label;
     }
 
     public String getHeight() {
         return height;
     }
 
     public void setHeight(String height) {
         this.height = height;
     }
 
     public String getWidth() {
         return width;
     }
 
     public void setWidth(String width) {
         this.width = width;
     }
 
 
     public String toStartString() {
 
        if (width != null) this.addCssStyle(" width=\"" + width + "\" ");
        if (height != null) this.addCssStyle(" height=\"" + height + "\" ");
 
         StringBuffer sb = new StringBuffer();
         sb.append("<fieldset");
         sb.append(getFormattedCss());
         sb.append(">");
         if (this.label != null) {
             sb.append("<legend>");
             sb.append(label);
             sb.append("</legend>");
         }
         return sb.toString();
     }
 
     public String toEndString() {
         return "</fieldset>";
     }
 
 }
