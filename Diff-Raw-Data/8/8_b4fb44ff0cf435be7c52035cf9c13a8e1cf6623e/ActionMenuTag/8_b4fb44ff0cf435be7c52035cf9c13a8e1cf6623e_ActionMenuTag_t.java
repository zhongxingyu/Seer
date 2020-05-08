 package org.gridlab.gridsphere.provider.portletui.tags;
 
 import org.gridlab.gridsphere.portlet.PortletResponse;
 import org.gridlab.gridsphere.portlet.PortletURI;
 import org.gridlab.gridsphere.provider.portletui.beans.ActionMenuBean;
 
 import javax.servlet.jsp.JspException;
 import javax.servlet.jsp.JspWriter;
 import javax.servlet.jsp.PageContext;
 import javax.servlet.jsp.tagext.Tag;
 
 /*
  * @author <a href="mailto:oliver.wehrens@aei.mpg.de">Oliver Wehrens</a>
  * @version $Id$
  */
 
 public class ActionMenuTag extends ContainerTag {
 
     protected ActionMenuBean actionMenuBean = null;
     protected String layout = null;
     protected String title = null;
     protected String menuType = null;
     protected boolean collapsible = false;
     protected boolean collapsed = false;
     protected String key = null;
 
 
     public String getMenuType() {
         return menuType;
     }
 
     public void setMenuType(String menuType) {
         this.menuType = menuType;
     }
 
     public boolean isCollapsed() {
         return collapsed;
     }
 
     public void setCollapsed(boolean collapsed) {
         this.collapsed = collapsed;
     }
 
     public boolean isCollapsible() {
         return collapsible;
     }
 
     public void setCollapsible(boolean collapsible) {
         this.collapsible = collapsible;
     }
 
     public String getTitle() {
         return title;
     }
 
     public void setTitle(String title) {
         this.title = title;
     }
 
     public String getLayout() {
         return layout;
     }
 
     public void setLayout(String layout) {
         this.layout = layout;
     }
 
     /**
      * Returns the key used to identify localized text
      *
      * @return the key used to identify localized text
      */
     public String getKey() {
         return key;
     }
 
     /**
      * Sets the key used to identify localized text
      *
      * @param key the key used to identify localized text
      */
     public void setKey(String key) {
         this.key = key;
     }
 
     public int doStartTag() throws JspException {
 
         // get the bean and the values
         if (!beanId.equals("")) {
             actionMenuBean = (ActionMenuBean) pageContext.getAttribute(getBeanKey(), PageContext.REQUEST_SCOPE);
         }
         if (actionMenuBean == null) {
             actionMenuBean = new ActionMenuBean();
         }
        if (this.layout != null) actionMenuBean.setAlign(layout);
        if (this.title != null) actionMenuBean.setTitle(title);
        if (this.menuType != null) actionMenuBean.setMenuType(this.menuType);

 
         Tag parent = getParent();
         if (parent instanceof ActionMenuTag) {
             actionMenuBean.setHasParentMenu(true);
         }
 
         // TODO not working so far
         actionMenuBean.setCollapsible(this.collapsible);
         actionMenuBean.setCollapsed(this.collapsed);
         PortletResponse res = (PortletResponse) pageContext.getAttribute("portletResponse");
         PortletURI uri = res.createURI();
         actionMenuBean.setPortletURI(uri);
 
         // locale stuff
         if (key != null) {
             actionMenuBean.setTitle(getLocalizedText(key, "ActionMenu"));
         }
 
         try {
             JspWriter out = pageContext.getOut();
             out.print(actionMenuBean.toStartString());
         } catch (Exception e) {
             throw new JspException(e.getMessage());
         }
         return EVAL_BODY_INCLUDE;
 
     }
 
     public int doEndTag() throws JspException {
 
         try {
             JspWriter out = pageContext.getOut();
             out.print(actionMenuBean.toEndString());
         } catch (Exception e) {
             throw new JspException(e.getMessage());
         }
 
         return EVAL_PAGE;
     }
 }
