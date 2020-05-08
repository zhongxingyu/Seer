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
 
 package org.icefaces.samples.showcase.example.ace.tab;
 
 import org.icefaces.samples.showcase.metadata.annotation.ComponentExample;
 import org.icefaces.samples.showcase.metadata.annotation.ExampleResource;
 import org.icefaces.samples.showcase.metadata.annotation.ExampleResources;
 import org.icefaces.samples.showcase.metadata.annotation.ResourceType;
 import org.icefaces.samples.showcase.metadata.context.ComponentExampleImpl;
 
 import javax.faces.bean.CustomScoped;
 import javax.faces.bean.ManagedBean;
 import java.io.Serializable;
 import java.util.HashMap;
 import javax.faces.application.FacesMessage;
 import javax.faces.component.UIComponent;
 import javax.faces.context.FacesContext;
 import javax.faces.validator.ValidatorException;
 
 @ComponentExample(
         parent = TabSetBean.BEAN_NAME,
         title = "example.ace.tabSet.proxy.title",
         description = "example.ace.tabSet.proxy.description",
         example = "/resources/examples/ace/tab/tabset-proxy.xhtml"
 )
 @ExampleResources(
         resources ={
             // xhtml
             @ExampleResource(type = ResourceType.xhtml,
                     title="tabset-proxy_side.xhtml",
                     resource = "/resources/examples/ace/tab/tabset-proxy.xhtml"),
             @ExampleResource(type = ResourceType.java,
                     title="TabProxyBean.java",
                     resource = "/WEB-INF/classes/org/icefaces/samples/showcase/example/ace/tab/TabProxyBean.java")
         }
 )
 @ManagedBean(name = TabProxyBean.BEAN_NAME)
 @CustomScoped(value = "#{window}")
 public class TabProxyBean extends ComponentExampleImpl<TabProxyBean>
         implements Serializable {
 
     public static final String BEAN_NAME = "tabProxy";
 
     private String exampleText = "";
     private boolean invalidSwitch = false;
     private int secondTabSelection;
     HashMap<String, Integer> options;
     
     
     public void validateSelection(FacesContext context, UIComponent component, Object value) throws ValidatorException{
         Integer submitedValue = (Integer)value;
         System.out.println("Submited value: " +submitedValue);
         if(submitedValue<0){
              throw new ValidatorException(new FacesMessage("You must select one of the options"));
         }
     }
 
     public boolean isInvalidSwitch() {
         return invalidSwitch;
     }
 
     public void setInvalidSwitch(boolean invalidSwitch) {
         this.invalidSwitch = invalidSwitch;
     }
 
     public String getExampleText() {
         return exampleText;
     }
 
     public void setExampleText(String exampleText) {
         this.exampleText = exampleText;
     }
 
     public int getSecondTabSelection() {
         return secondTabSelection;
     }
 
     public void setSecondTabSelection(int secondTabSelection) {
         this.secondTabSelection = secondTabSelection;
     }
 
     public TabProxyBean() {
         super(TabProxyBean.class);
         options = new HashMap<String, Integer>();
         options.put("yes", 1);
        options.put("no", 0);
     }
 
     public HashMap<String, Integer> getOptions() {
         return options;
     }
 
     public void setOptions(HashMap<String, Integer> options) {
         this.options = options;
     }
 }
