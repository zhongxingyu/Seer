 /*
  * Copyright 2004-2013 ICEsoft Technologies Canada Corp.
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
 
 package org.icefaces.ace.component.textentry;
 
 import org.icefaces.ace.meta.annotation.*;
 import org.icefaces.ace.meta.baseMeta.HtmlInputTextMeta;
 
 import org.icefaces.ace.resources.ACEResourceNames;
 import org.icefaces.resources.ICEResourceDependencies;
 import org.icefaces.resources.ICEResourceDependency;
 import org.icefaces.resources.ICEResourceLibrary;
import javax.faces.application.ResourceDependencies;
import javax.faces.application.ResourceDependency;
 
 @Component(
         tagName = "textEntry",
         componentClass = "org.icefaces.ace.component.textentry.TextEntry",
         rendererClass = "org.icefaces.ace.component.textentry.TextEntryRenderer",
         generatedClass = "org.icefaces.ace.component.textentry.TextEntryBase",
         extendsClass = "javax.faces.component.html.HtmlInputText",
         componentType = "org.icefaces.ace.component.TextEntry",
         rendererType = "org.icefaces.ace.component.TextEntryRenderer",
         componentFamily = "org.icefaces.ace.TextEntry",
         tlddoc = "TextEntry is a text input component that can display some placeholder text inside the input field when the component doesn't have a value and is not focussed." +
                 " It also has custom styling for invalid state and required status." +
                 "<p>For more information, see the <a href=\"http://wiki.icefaces.org/display/ICE/TextEntry\">TextEntry Wiki Documentation</a>."
 )
 
 @ICEResourceLibrary(ACEResourceNames.ACE_LIBRARY)
 @ICEResourceDependencies({
      @ICEResourceDependency(name = ACEResourceNames.COMPONENTS_JS)
 })
@ResourceDependencies({
        @ResourceDependency(library = "org.icefaces.component.util", name = "component.js")
})
 @ClientBehaviorHolder(events = {
         @ClientEvent(name = "blur", javadoc = "Fired when the text input field loses focus (default event).",
                 tlddoc = "Fired when the text input field loses focus (default event).", defaultRender = "@all", defaultExecute = "@this"),
 	@ClientEvent(name="change", javadoc="Fired when the component detects value is changed.",
             tlddoc="Fired when the component detects value is changed.",
             defaultRender="@this", defaultExecute="@all")
 }, defaultEvent = "blur")
 public class TextEntryMeta extends HtmlInputTextMeta {
 
     @Property(tlddoc = "Name of the widget variable to access client-side API.")
     private String widgetVar;
 
     @Property(tlddoc = "Indicator indicating that the user is required to provide a submitted value for this input component.")
     private String requiredIndicator;
 
     @Property(tlddoc = "Indicator indicating that the user is NOT required to provide a submitted value for this input component.")
     private String optionalIndicator;
 
     @Property(tlddoc = "Position of label relative to input field. Supported values are \"left/right/top/bottom/inField/none\". Default is \"none\".")
     private String labelPosition;
 
     @Property(tlddoc = "Position of input-required or input-optional indicator relative to input field or label. " +
             "Supported values are \"left/right/top/bottom/labelLeft/labelRight/none\". " +
             "Default is \"labelRight\" if labelPosition is \"inField\", \"right\" otherwise.")
     private String indicatorPosition;
 
     @Property(tlddoc = "When true the component will automatically tab to the next component once the maxLength number of characters have been entered.")
     private boolean autoTab;
 	
 	// ----------------------------------------
 	// ----- imported from mobi:inputText -----
 	// ----------------------------------------
 
     @Property(defaultValue = "text", tlddoc = "The type attribute for the input element. " +
     		"Currently supports text, phone, url, email, number, date, time, datetime.  Depending " +
     		"on device capability, a type-specific keyboard may be displayed. ")
     private String type;
 
     @Property(tlddoc = "The HTML5 placeholder attribute represents a short hint" +
     		" (a word or short phrase) intended to aid the user with data entry " +
     		"when the input element has no value.")
     private String placeholder;
 
     @Property(tlddoc = "The pattern attribute specifies a regular expression against which " +
     		"the control's value, or, when the multiple attribute applies and is set, " +
     		"the control's values, are to be checked. ")
     private String pattern;
 
     @Property(defaultValue = "off",
             tlddoc = "Capitalize the first character of the field.")
     private String autocapitalize;
 
     @Property(defaultValue = "off",
             tlddoc = "Correct spelling errors in the field.")
     private String autocorrect;
 
     @Property(defaultValue = "3", tlddoc = "Magnifying glass for webkit used to show last three searches on a search field.")
     private int results;
     
     @Property(defaultValue = "Integer.MIN_VALUE", tlddoc="Minimum value, only applicable to type number.")
     private int min;
 
     @Property(defaultValue = "Integer.MIN_VALUE", tlddoc="Maximum value, only applicable to type number.")
     private int max;
     
     @Property(defaultValue = "Integer.MIN_VALUE", tlddoc="The step to increase/decrease the value of the number input. " +
             "Applicable only to type \"number\". ")
     private int step;
 }
