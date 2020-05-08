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
 
 package org.icefaces.ace.component.menu;
 
 import org.icefaces.ace.meta.annotation.Component;
 import org.icefaces.ace.meta.annotation.Property;
 import org.icefaces.ace.meta.baseMeta.UIComponentBaseMeta;
 import org.icefaces.ace.model.MenuModel;
 
 import javax.faces.application.ResourceDependencies;
 import javax.faces.application.ResourceDependency;
 
 @Component(
         tagName = "menu",
         componentClass = "org.icefaces.ace.component.menu.Menu",
         rendererClass = "org.icefaces.ace.component.menu.MenuRenderer",
         generatedClass = "org.icefaces.ace.component.menu.MenuBase",
         extendsClass = "org.icefaces.ace.component.menu.AbstractMenu",
         componentFamily = "org.icefaces.ace.component.Menu",
         componentType = "org.icefaces.ace.component.Menu",
         rendererType = "org.icefaces.ace.component.MenuRenderer",
         tlddoc = "Menu is a navigation component with various customized modes like multi tiers, overlay " +
                 "and nested menus." +
                  "<p>For more information, see the " +
                  "<a href=\"http://wiki.icefaces.org/display/ICE/Menu+and+Menubar\">Menu Wiki Documentation</a>."
 )
 @ResourceDependencies({
 		@ResourceDependency(library="icefaces.ace", name="util/combined.css"),
         @ResourceDependency(library = "icefaces.ace", name = "util/ace-jquery.js"),
         @ResourceDependency(library = "icefaces.ace", name = "util/ace-menu.js")
 })
 public class MenuMeta extends UIComponentBaseMeta {
     @Property(tlddoc = "Javascript variable name of the wrapped widget.")
     private String widgetVar;
 
     @Property(tlddoc = "MenuModel instance to create menus programmatically. " +
             "For the menuitem and submenu components, use explicit ids, and " +
             "avoid long processing in the getter method for this property, " +
             "as it will be called multiple times, in every lifecycle.")
     private MenuModel model;
 
     @Property(tlddoc="Server side id of the component whose triggerEvent will show the menu, when position is dynamic.")
     private String trigger;
 
    @Property(tlddoc="When position is dynamic, this attribute specifies the corner of the menu to align with the trigger element. The format is \"&lt;left|right&gt; &lt;top|bottom&gt;\" (examples: \"left top\", \"right bottom\").", defaultValue="left top")
     private String my;
 
    @Property(tlddoc="When position is dynamic, this attribute specifies the corner of trigger element to align with the menu. The format is \"&lt;left|right&gt; &lt;top|bottom&gt;\" (examples: \"left top\", \"right bottom\").", defaultValue="left bottom")
     private String at;
 
     @Property(tlddoc = "Sets the way menu is placed on the page, when \"static\" menu is displayed in the normal flow," +
             " when set to \"dynamic\" menu is not on the normal flow allowing overlaying. Default value is \"static\".",
             defaultValue = "static")
     private String position;
 
     @Property(tlddoc = "Sets the tiered mode, when set to true menu will be rendered in different tiers.")
     private boolean tiered;
 
     @Property(tlddoc = "Type of menu, valid values are \"plain\", tiered and \"sliding\".", defaultValue = "plain")
     private String type;
 
     @Property(tlddoc = "Sets the effect for the menu display, default value is FADE. Possible values are" +
             " \"FADE\", \"SLIDE\", \"NONE\". Use \"NONE\" to disable animation at all.", defaultValue = "fade")
     private String effect;
 
     @Property(tlddoc = "Sets the effect duration in milliseconds.", defaultValue = "400")
     private int effectDuration;
 
     @Property(tlddoc = "Style of the main container element.")
     private String style;
 
     @Property(tlddoc = "Style class of the main container element.")
     private String styleClass;
 
     @Property(tlddoc = "zindex property to control overlapping with other elements.", defaultValue = "1")
     private int zindex;
 
     @Property(tlddoc="Label for the 'back' link (only applies to sliding menus).", defaultValue = "Back")
     private String backLabel;
 
     @Property(tlddoc="Maximum height (in pixels) for the menu (only applies to sliding menus).", defaultValue = "200")
     private int maxHeight;
 
    @Property(tlddoc="Event that will trigger the menu to show, when position is dynamic. The possible values are \"click\", \"mouseenter\", \"dblclick\", and \"rtclick\".", defaultValue = "click")
     private String triggerEvent;
 
     private AbstractMenu am; // need this for solving dependence on AbstractMenu when compiling MenuBase
 }
