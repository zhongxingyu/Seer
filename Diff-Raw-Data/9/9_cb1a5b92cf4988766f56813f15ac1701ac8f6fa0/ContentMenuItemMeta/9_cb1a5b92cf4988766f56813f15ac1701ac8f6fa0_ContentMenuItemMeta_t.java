 /*
  * Copyright 2004-2012 ICEsoft Technologies Canada Corp.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the
  * License. You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an "AS
  * IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
  * express or implied. See the License for the specific language
  * governing permissions and limitations under the License.
  */
 package org.icefaces.mobi.component.contentmenuitem;
 
 import org.icefaces.ace.meta.annotation.*;
 import org.icefaces.ace.meta.baseMeta.UICommandMeta;
 
 
 @Component(
         tagName = "contentMenuItem",
         componentClass = "org.icefaces.mobi.component.contentmenuitem.ContentMenuItem",
         rendererClass = "org.icefaces.mobi.component.contentmenuitem.ContentMenuItemRenderer",
         generatedClass = "org.icefaces.mobi.component.contentmenuitem.ContentMenuItemBase",
         extendsClass = "javax.faces.component.UICommand",
         componentType = "org.icefaces.ContentMenuItem",
         rendererType = "org.icefaces.ContentMenuItemRenderer",
         componentFamily = "org.icefaces.ContentMenuItem",
        tlddoc = "This component fires an actionListener from a menu item.  The value can be a url, " +
                " or, more typically, an id to a panel in the contentStack that is controlled by its" +
                " parent, which must be either a ContentStackMenu or a ContentNavBar. If it's a child of" +
                 " the ContentStackMenu, then it's rendered as a list item.  If a child of the  " +
                 " ContentNavBar, it is rendered as a button on a tool bar.  If the menuHeader attribute "+
                 " is true, then it becomes a menu heading and no selection of the contentStack is done. " +
                " A contentNavBar does not have menu headings, so the menuHeading attribute is only "+
                 " applicable for a ContentStackMenu."
 )
 
 public class ContentMenuItemMeta extends UICommandMeta {
      @Property(defaultValue = "true",
             tlddoc = "When singleSubmit is true, triggering an action on this component will submit" +
                     " and execute this component only. Equivalent to <f:ajax execute='@this' render='@all'>." +
                     " When singleSubmit is false, triggering an action on this component will submit and execute " +
                     " the full form that this component is contained within." +
                     " The default value is false.")
      private boolean singleSubmit;
 
      @Property(tlddoc = "Url to be navigated when menuitem is clicked. Outside of the ContentStackMenu contentStack.")
      private String url;
 
      @Property(defaultValue = "false",
                 tlddoc = "disabled property. If true nothing will be submitted via this" +
                         "component.  ")
      private boolean disabled;
 
      @Property(tlddoc="label of layoutMenuItem", required=Required.yes)
      private String label;
 
      @Property(defaultValue="false",
              tlddoc="true if the item is not a link but a header for a group of menu items")
      private boolean menuHeader;
 
      @Property(tlddoc="id of panel in contentStack that will be displayed when selecting this item." +
              "  default value will be first contentPane in the stack as long as url is blank as well")
      private String value;
 
      @Property(tlddoc = "style class of the component, rendered on the div root of the component")
      private String styleClass;
 
      @Property(tlddoc = "style of the component, rendered on the div root of the component")
      private String style;
 
      @Property(tlddoc = "Path of the menuitem image.")
      private String icon;
 
 }
