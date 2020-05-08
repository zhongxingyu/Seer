 /**
  * Sencha GXT 3.0.1 - Sencha for GWT Copyright(c) 2007-2012, Sencha, Inc. licensing@sencha.com
  * 
  * http://www.sencha.com/products/gxt/license/
  */
 package org.iplantc.de.client.desktop.widget;
 
 import org.iplantc.de.client.DeResources;
 import org.iplantc.de.client.models.ShortcutDesc;
 import org.iplantc.de.client.views.windows.configs.WindowConfig;
 
 import com.google.gwt.core.shared.GWT;
 import com.google.gwt.event.dom.client.MouseOutEvent;
 import com.google.gwt.event.dom.client.MouseOutHandler;
 import com.google.gwt.event.dom.client.MouseOverEvent;
 import com.google.gwt.event.dom.client.MouseOverHandler;
import com.sencha.gxt.core.client.util.Padding;
 import com.sencha.gxt.widget.core.client.button.IconButton;
 import com.sencha.gxt.widget.core.client.event.SelectEvent.SelectHandler;
 
 /**
  * A desktop shortcut.
  */
 public class Shortcut extends IconButton {
 
     private final String action;
     private final String tag;
     private final WindowConfig windowConfig;
     DeResources res = GWT.create(DeResources.class);
 
     /**
      * Creates a new shortcut.
      */
     public Shortcut(final ShortcutDesc desc, SelectHandler handler) {
         super(desc.getStyle());
         setId(desc.getId());
         setSize("68px", "68px");
         setToolTip(desc.getCaption());
 
         this.windowConfig = desc.getWindowConfig();
 
         this.action = desc.getAction();
         this.tag = desc.getTag();
 
         this.addSelectHandler(handler);
         getElement().setAttribute("data-step", desc.getIndex());
         getElement().setAttribute("data-intro", desc.getHelp());
         getElement().setAttribute("data-position", "bottom");
         setBorders(false);
         addHandler(new MouseOverHandler() {
 
             @Override
             public void onMouseOver(MouseOverEvent event) {
                 changeStyle(desc.getHoverStyle());
             }
         }, MouseOverEvent.getType());
 
         addHandler(new MouseOutHandler() {
 
             @Override
             public void onMouseOut(MouseOutEvent event) {
                 changeStyle(desc.getStyle());
             }
         }, MouseOutEvent.getType());
 
     }
 
     public WindowConfig getWindowConfig() {
         return windowConfig;
     }
 
     /**
      * Retrieve the action for dispatch.
      * 
      * @return the action associated with this shortcut.
      */
     public String getAction() {
         return action;
     }
 
     /**
      * Retrieve the tag for dispatch.
      * 
      * @return the tag associated with this shortcut.
      */
     public String getTag() {
         return tag;
     }
 }
