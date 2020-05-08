 /*
  * Copyright 2004-2006 the Seasar Foundation and the Others.
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
 package javax.faces.render;
 
 import java.io.IOException;
 import java.util.Iterator;
 
 import javax.faces.component.UIComponent;
 import javax.faces.context.FacesContext;
 import javax.faces.convert.ConverterException;
 
 import org.seasar.framework.util.AssertionUtil;
 
 /**
  * @author shot
  * @author manhole
  */
 public abstract class Renderer {
 
     public void decode(FacesContext context, UIComponent component) {
         AssertionUtil.assertNotNull("context", context);
         AssertionUtil.assertNotNull("component", component);
     }
 
     public void encodeBegin(FacesContext context, UIComponent component)
             throws IOException {
         AssertionUtil.assertNotNull("context", context);
         AssertionUtil.assertNotNull("component", component);
     }
 
     public void encodeChildren(FacesContext context, UIComponent component)
             throws IOException {
         AssertionUtil.assertNotNull("context", context);
         AssertionUtil.assertNotNull("component", component);
         for (Iterator itr = component.getChildren().iterator(); itr.hasNext();) {
             UIComponent child = (UIComponent) itr.next();
             child.encodeBegin(context);
             if (child.getRendersChildren()) {
                 child.encodeChildren(context);
             } else {
                encodeChildren(context, child);
             }
             child.encodeEnd(context);
             if (context.getResponseComplete()) {
                 break;
             }
         }
     }
 
     public void encodeEnd(FacesContext context, UIComponent component)
             throws IOException {
         AssertionUtil.assertNotNull("context", context);
         AssertionUtil.assertNotNull("component", component);
     }
 
     public String convertClientId(FacesContext context, String clientId) {
         AssertionUtil.assertNotNull("context", context);
         AssertionUtil.assertNotNull("clientId", clientId);
         return clientId;
     }
 
     public boolean getRendersChildren() {
         return false;
     }
 
     public Object getConvertedValue(FacesContext context,
             UIComponent component, Object submittedValue)
             throws ConverterException {
         AssertionUtil.assertNotNull("context", context);
         AssertionUtil.assertNotNull("component", component);
         return submittedValue;
     }
 
 }
