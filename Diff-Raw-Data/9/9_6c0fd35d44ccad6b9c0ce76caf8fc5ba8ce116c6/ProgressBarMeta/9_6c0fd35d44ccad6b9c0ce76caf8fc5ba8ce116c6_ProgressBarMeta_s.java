 /*
  * Copyright 2010-2011 ICEsoft Technologies Canada Corp.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package org.icefaces.ace.component.progressbar;
 
 import org.icefaces.ace.meta.annotation.Component;
 import org.icefaces.ace.meta.annotation.Expression;
 import org.icefaces.ace.meta.annotation.Property;
 import org.icefaces.ace.meta.baseMeta.UIComponentBaseMeta;
 import javax.el.MethodExpression;
 import javax.faces.application.ResourceDependencies;
 import javax.faces.application.ResourceDependency;
 import org.icefaces.ace.meta.annotation.ClientBehaviorHolder;
 import org.icefaces.ace.meta.annotation.ClientEvent;
 import org.icefaces.ace.api.IceClientBehaviorHolder;
 
 @Component(
         tagName = "progressBar",
         componentClass = "org.icefaces.ace.component.progressbar.ProgressBar",
         generatedClass = "org.icefaces.ace.component.progressbar.ProgressBarBase",
         extendsClass = "javax.faces.component.UIComponentBase",
         rendererClass = "org.icefaces.ace.component.progressbar.ProgressBarRenderer",
         componentFamily = "org.icefaces.ace.component.ProgressBar",
         componentType = "org.icefaces.ace.component.ProgressBar",
         rendererType = "org.icefaces.ace.component.ProgressBarRenderer",
         tlddoc = "ProgressBar is a process status indicator that can either work purely on client side or " +
                  "interact with server side using ajax.")
 @ResourceDependencies({
         @ResourceDependency(library="icefaces.ace", name="jquery/ui/jquery-ui.css"),
         @ResourceDependency(library="icefaces.ace", name="jquery/jquery.js"),
         @ResourceDependency(library="icefaces.ace", name="jquery/ui/jquery-ui.js"),
         @ResourceDependency(library="icefaces.ace", name="core/core.js"),
         @ResourceDependency(library="icefaces.ace", name="progressbar/progressbar.js")
 })
 @ClientBehaviorHolder(events = {
 	@ClientEvent(name="complete", javadoc="...", tlddoc="...", defaultRender="@all", defaultExecute="@all"),
 	@ClientEvent(name="cancel", javadoc="...", tlddoc="...", defaultRender="@all", defaultExecute="@all"),
 	@ClientEvent(name="change", javadoc="...", tlddoc="...", defaultRender="@all", defaultExecute="@all")
 })
 public class ProgressBarMeta extends UIComponentBaseMeta {
     @Property(tlddoc = "Name of the client side widget.")
     private String widgetVar;
 
     @Property(defaultValue = "0", tlddoc = "Value of the progress bar.")
     private int value;
 
     @Property(tlddoc = "Disables or enables the progressbar.")
     private boolean disabled;
 
     @Property(tlddoc = "If true, you can use start() to start polling sever for progress value.")
     private boolean usePolling;
 
     @Property(defaultValue = "3000", tlddoc = "Interval in milliseconds to do polling.")
     private int pollingInterval;
 
     @Property(tlddoc = "Inline style of the main container element.")
     private String style;
 
     @Property(tlddoc = "Style class of the main container element.")
     private String styleClass;
 
     @Property(tlddoc = "")
     private String oncomplete;
 
     @Property(tlddoc = "Specifies component(s) to update with ajax when progress is completed")
     private String onCompleteUpdate;
 
     @Property(expression = Expression.METHOD_EXPRESSION,
               tlddoc = "A server side listener to be invoked when a progress is completed.")
     private MethodExpression completeListener;
 
     @Property(expression = Expression.METHOD_EXPRESSION, methodExpressionArgument = "org.icefaces.ace.event.ProgressBarChangeEvent",
             tlddoc = "Listener to be called when the value of the progressbar changes.")
     private MethodExpression changeListener;
 
     @Property(tlddoc = "Specifies component(s) to update with ajax when progress is cancelled")
     private String onCancelUpdate;
 
     @Property(expression = Expression.METHOD_EXPRESSION,
               tlddoc = "A server side listener to be invoked when a progress is cancelled.")
     private MethodExpression cancelListener;
 }
