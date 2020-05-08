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
 package org.icefaces.mobi.component.tabset;
 
 import javax.el.MethodExpression;
 import org.icefaces.ace.meta.annotation.Component;
 import org.icefaces.ace.meta.annotation.Property;
 import org.icefaces.ace.meta.annotation.Expression;
 import org.icefaces.ace.meta.baseMeta.UIPanelMeta;
 import org.icefaces.ace.meta.annotation.ClientBehaviorHolder;
 import org.icefaces.ace.meta.annotation.ClientEvent;
 import org.icefaces.ace.meta.annotation.Required;
 
 @Component(
         tagName = "tabSet",
         componentClass = "org.icefaces.mobi.component.tabset.TabSet",
         rendererClass = "org.icefaces.mobi.component.tabset.TabSetRenderer",
         generatedClass = "org.icefaces.mobi.component.tabset.TabSetBase",
         componentType = "org.icefaces.TabSet",
         rendererType = "org.icefaces.TabSetRenderer",
         extendsClass = "javax.faces.component.UIPanel",
         componentFamily = "org.icefaces.TabSet",
         tlddoc = "This mobility component allows contentPane's to be placed within tabset controls in  " +
                 "a stacked manner.  Only a single contentPane may be active at a time."
 )

 public class TabSetMeta extends UIPanelMeta {
     @Property(tlddoc="Int of the active pane.", defaultValue="0")
     private int tabIndex;
 
     @Property(tlddoc="Inline style of the container element.")
     private String style;
 
     @Property(tlddoc="Style class of the container element.")
     private String styleClass;
 
  /*    @Property(tlddoc = "If true then all tabs except the active one will " +
         "be disabled and can not be selected.")
      private boolean disabled; */
 
     @Property(
          tlddoc="fixeHeight can be used to fix the height of the container. Must be valid height for element.style.height, " +
                  "for example: 200px.  If this attribute it not used, a calculation will be made to fix the height of the" +
                  " container to the largest height of its children")
     private String fixedHeight;
 
     @Property(tlddoc="MethodExpression representing a method that will be " +
         "invoked when the selected TabPane has changed. The expression " +
         "must evaluate to a public method that takes a ValueChangeEvent " +
         "parameter, with a return type of void. NOT IMPLEMENTED YET!!!",
         expression= Expression.METHOD_EXPRESSION,
         methodExpressionArgument="javax.faces.event.ValueChangeEvent")
     private MethodExpression tabChangeListener;
 
    @Property(defaultValue = "false", tlddoc = "The default value of this attribute is false. If true then value change event will happen in APPLY_REQUEST_VALUES phase and if the value of this attribute is false then event change will happen in INVOKE_APPLICATION phase")
     private boolean immediate;
 
     @Property(defaultValue = "true",
             tlddoc = "When singleSubmit is true, triggering an action on this component will submit" +
                     " and execute this component only. Equivalent to <f:ajax execute='@this' render='@all'>." +
                     " When singleSubmit is false, triggering an action on this component will submit and execute " +
                     " the full form that this component is contained within." +
                     " The default value is true in case this component is not placed within a form.")
      private boolean singleSubmit;
 }
