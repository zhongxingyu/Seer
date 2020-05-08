 /*
  * Copyright 2004-2007 the Seasar Foundation and the Others.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, 
  * either express or implied. See the License for the specific language
  * governing permissions and limitations under the License.
  */
 package org.seasar.teeda.extension.taglib;
 
import javax.faces.webapp.UIComponentTag;

 import org.seasar.teeda.extension.component.html.THtmlForm;
 
 /**
  * @author koichik
  */
public class TFormTag extends UIComponentTag {
 
     public TFormTag() {
     }
 
     public String getComponentType() {
         return THtmlForm.COMPONENT_TYPE;
     }
 
     public String getRendererType() {
         return THtmlForm.DEFAULT_RENDERER_TYPE;
     }
 }
