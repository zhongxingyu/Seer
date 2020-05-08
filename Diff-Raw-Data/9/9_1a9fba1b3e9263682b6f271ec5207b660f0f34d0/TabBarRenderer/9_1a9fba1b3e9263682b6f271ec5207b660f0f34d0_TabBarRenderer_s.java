 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package org.helix.mobile.component.tabbar;
 
 import java.io.IOException;
 import java.util.LinkedList;
 import java.util.List;
 import javax.faces.FacesException;
 import javax.faces.component.UIComponent;
 import javax.faces.context.FacesContext;
 import javax.faces.context.ResponseWriter;
 import org.helix.mobile.component.tab.Tab;
 import org.helix.mobile.component.view.View;
 import org.primefaces.renderkit.CoreRenderer;
 
 /**
  *
  * @author shallem
  */
 public class TabBarRenderer extends CoreRenderer {
     private List<Tab> tabs;
     
     public TabBarRenderer() {
         this.tabs = new LinkedList<Tab>();
     }
     
     @Override
     public void encodeBegin(FacesContext context, UIComponent component) throws IOException {
         TabBar bar = (TabBar) component;
        if (bar.getFacet("tabs") != null && this.tabs.isEmpty()) {
            collectTabs(context, bar.getFacet("tabs"));
         }
     }
     
     private void collectTabs(FacesContext context, UIComponent tabsParent) {
         for (UIComponent c : tabsParent.getChildren()) {
             if (c instanceof Tab) {
                 tabs.add((Tab)c);
             } else {
                 throw new FacesException("tabs facet can only contain tab components.");
             }
         }
     }
     
     @Override
     public void encodeEnd(FacesContext context, UIComponent component) throws IOException {
         
     }
 
     @Override
     public void encodeChildren(FacesContext context, UIComponent component) throws IOException {
         ResponseWriter writer = context.getResponseWriter();
         TabBar bar = (TabBar)component;
         View page = (View)bar.getAttributes().get("view");
         
         if (page == null) {
             /* We get there the first time through the component tree. */
             this.renderChildren(context, component);
             return;
         }
         
         writer.startElement("div", page);
         writer.writeAttribute("data-role", "navbar", null);
         
         writer.startElement("ul", page);
         for (Tab t : tabs) {
             if (t.getPage().equals(page.getId())) {
                 t.getAttributes().put("active", Boolean.TRUE);
             } else {
                 t.getAttributes().put("active", Boolean.FALSE);
             }
             this.renderChild(context, t);
         }
         writer.endElement("ul");
         writer.endElement("div");
         
         bar.getAttributes().remove("view");
     }
 
     @Override
     public boolean getRendersChildren() {
         return true;
     }
 }
