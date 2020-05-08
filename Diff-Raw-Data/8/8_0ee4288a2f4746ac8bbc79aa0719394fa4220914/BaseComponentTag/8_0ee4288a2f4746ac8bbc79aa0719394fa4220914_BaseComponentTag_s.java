 /*
  * @author <a href="mailto:novotny@aei.mpg.de">Jason Novotny</a>
  * @version $Id$
  */
 package org.gridlab.gridsphere.provider.portletui.tags;
 
 import org.gridlab.gridsphere.portlet.PortletRequest;
 import org.gridlab.gridsphere.provider.portletui.beans.BaseComponentBean;
 
 import javax.portlet.RenderRequest;
 import javax.servlet.jsp.JspException;
 import javax.servlet.jsp.JspWriter;
 import javax.servlet.jsp.tagext.Tag;
 import java.util.Locale;
 import java.util.ResourceBundle;
 
 /**
  * The abstract <code>BaseComponentTag</code> is used by all UI tags to provide CSS support and general
  * name, value attributes
  */
 public abstract class BaseComponentTag extends BaseBeanTag {
 
     protected String name = null;
     protected String value = null;
     protected boolean readonly = false;
     protected boolean disabled = false;
     protected String cssStyle = null;
     protected String cssClass = null;
     protected boolean supportsJS = false;
     protected boolean isVisible = true;
     protected Locale locale = null;
 
 
     /**
      * Sets the name of the bean
      *
      * @param name the name of the bean
      */
     public void setName(String name) {
         this.name = name;
     }
 
     /**
      * Gets the name of the bean
      *
      * @return name of the bean
      */
     public String getName() {
         return this.name;
     }
 
     /**
      * Sets the tag value
      *
      * @param value the tag value
      */
     public void setValue(String value) {
         this.value = value;
     }
 
     /**
      * Returns the tag value
      *
      * @return the tag value
      */
     public String getValue() {
         return value;
     }
 
     /**
      * Returns true if bean is in disabled state
      *
      * @return the tag state
      */
     public boolean isDisabled() {
         return disabled;
     }
 
     /**
      * Sets the disabled attribute of the bean to be in the 'flag' state.
      *
      * @param flag is true if this tag/bean is disabled
      */
     public void setDisabled(boolean flag) {
         this.disabled = flag;
     }
 
     /**
      * Returns disabled String if bean is disabled
      *
      * @return a String depending if bean is disabled
      */
     protected String checkDisabled() {
         if (disabled) {
             return " disabled='disabled' ";
         } else {
             return "";
         }
     }
 
     /**
      * Sets the bean to read-only
      *
      * @param flag is true if bean is read-only, false otherwise
      */
     public void setReadonly(boolean flag) {
         this.readonly = flag;
     }
 
     /**
      * Returns the read-only status of the bean
      *
      * @return read-only status of the bean
      */
     public boolean isReadonly() {
         return readonly;
     }
 
     /**
      * Returns a String to use in markup if the bean is read-only
      *
      * @return a String to use in markup if the bean is read-only
      */
     protected String checkReadonly() {
         if (readonly) {
             return " readonly='readonly' ";
         } else {
             return "";
         }
     }
 
     /**
      * Returns the CSS style name of the beans
      *
      * @return the name of the css style
      */
     public String getCssStyle() {
         return cssStyle;
     }
 
     /**
      * Sets the CSS style of the beans
      *
      * @param style css style name to set for the beans
      */
     public void setCssStyle(String style) {
         this.cssStyle = style;
     }
 
     /**
      * Returns the CSS class name of the beans
      *
      * @return the name of the css class
      */
     public String getCssClass() {
         return cssClass;
     }
 
     /**
      * Sets the CSS class of the beans
      *
      * @param cssClass the class name to set for the beans
      */
     public void setCssClass(String cssClass) {
         this.cssClass = cssClass;
     }
 
     /**
      * Sets the base component properties of the bean
      *
      * @param componentBean a ui bean
      */
     protected void setBaseComponentBean(BaseComponentBean componentBean) {
         componentBean.setBeanId(beanId);
         if (cssStyle != null) componentBean.setCssClass(cssStyle);
         componentBean.setDisabled(disabled);
         componentBean.setReadOnly(readonly);
         if (name != null) componentBean.setName(name);
         if (value != null) componentBean.setValue(value);
         if (supportsJavaScript()) {
             supportsJS = true;
         } else {
             supportsJS = false;
         }
         componentBean.setSupportsJS(supportsJS);
         componentBean.setReadOnly(readonly);
     }
 
     /**
      * Updates the base component bean properties
      *
      * @param componentBean a ui bean
      */
     protected void updateBaseComponentBean(BaseComponentBean componentBean) {
         if ((cssClass != null) && componentBean.getCssClass() == null) {
             componentBean.setCssClass(cssClass);
         }
         if ((cssStyle != null) && componentBean.getCssStyle() == null) {
             componentBean.setCssStyle(cssStyle);
         }
         if ((name != null) && (componentBean.getName() == null)) {
             componentBean.setName(name);
         }
         if ((value != null) && (componentBean.getValue() == null)) {
             componentBean.setValue(value);
         }
         if (supportsJavaScript()) {
             supportsJS = true;
         } else {
             supportsJS = false;
         }
     }
 
     protected void overrideBaseComponentBean(BaseComponentBean componentBean) {
         if (cssClass != null) {
             componentBean.setCssClass(cssClass);
         }
         if (cssStyle != null) {
             componentBean.setCssClass(cssStyle);
         }
         if (name != null) {
             componentBean.setName(name);
         }
         if (value != null) {
             componentBean.setValue(value);
         }
         if (supportsJavaScript()) {
             supportsJS = true;
         } else {
             supportsJS = false;
         }
 
     }
 
     protected String getLocalizedText(String key) {
         return getLocalizedText(key, "Portlet");
     }
 
     protected Locale getLocale() {
         Locale locale = Locale.ENGLISH;
         PortletRequest req = (PortletRequest) pageContext.getAttribute("portletRequest");
         if (req != null) {
             locale = req.getLocale();
         } else {
            RenderRequest renderReq = (RenderRequest) pageContext.getAttribute("renderRequest");
            locale = renderReq.getLocale();
         }
         return locale;
     }
 
     protected String getLocalizedText(String key, String base) {
         Locale locale = getLocale();
         try {
             ResourceBundle bundle = ResourceBundle.getBundle(base, locale);
             return bundle.getString(key);
         } catch (Exception e) {
             try {
                 ResourceBundle bundle = ResourceBundle.getBundle(base, Locale.ENGLISH);
                 return bundle.getString(key);
             } catch (Exception ex) {
                 return key;
             }
         }
     }
 
     public int doStartTag() throws JspException {
         Tag parent = getParent();
         if (parent instanceof PanelTag) {
             PanelTag panelTag = (PanelTag) parent;
 
             int numCols = panelTag.getNumCols();
 
             int thiscol = panelTag.getColumnCounter();
             try {
                 JspWriter out = pageContext.getOut();
                 if ((thiscol % numCols) == 0) {
                     out.println("<tr>");
                 }
                 out.println("<td width=\"" + "100%" + "\">");
             } catch (Exception e) {
                 throw new JspException(e.getMessage());
             }
         }
         return EVAL_BODY_INCLUDE;
     }
 
     public int doEndTag() throws JspException {
         Tag parent = getParent();
         if (parent instanceof PanelTag) {
             PanelTag panelTag = (PanelTag) parent;
 
             int numCols = panelTag.getNumCols();
             int thiscol = panelTag.getColumnCounter();
             thiscol++;
             panelTag.setColumnCounter(thiscol);
             try {
                 JspWriter out = pageContext.getOut();
                 out.println("</td>");
                 if ((thiscol % numCols) == 0) {
                     out.println("</tr>");
                 }
             } catch (Exception e) {
                 throw new JspException(e.getMessage());
             }
         }
         return super.doEndTag();
     }
 }
