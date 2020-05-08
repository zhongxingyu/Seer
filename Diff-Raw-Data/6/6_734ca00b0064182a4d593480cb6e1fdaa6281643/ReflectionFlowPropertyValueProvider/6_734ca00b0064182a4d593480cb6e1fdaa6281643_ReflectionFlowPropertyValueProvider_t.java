 /*
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not
  * use this file except in compliance with the License. You may obtain a copy
  * of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software distributed
  * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
  * OR CONDITIONS OF ANY KIND, either express or implied. See the License for
  * the specific language governing permissions and limitations under the
  * License.
  */
 
 package org.amplafi.flow.flowproperty;
 
 import org.amplafi.flow.FlowPropertyValueProvider;
 import org.amplafi.flow.FlowPropertyDefinition;
 
 import com.sworddance.beans.BeanWorker;
 
 /**
  * Uses reflection to trace to find the property value.
  * if at any point a null is returned then null is returned (no {@link NullPointerException} will be thrown)
  *
  * The root object can be either the flowPropertyProvider parameter in the {@link #get(FlowPropertyProvider, FlowPropertyDefinition)} call or another object
  * supplied in the constructor.
  *
  * TODO need to be able to set base as a String and that is the property name that will act as the base.
  * example: "messagePoint" means:
  * 1) retrieve "messagePoint"
  * 2) do reflection using the propertyNames to get the value.
  *
  * TODO: Create ability to define the {@link FlowPropertyDefinitionImpl} Default property name would be the last property in the list. So "httpManager.cachedUris" would define
  * the "cachedUris" property (default)
  *
  * @author patmoore
  *
  */
 public class ReflectionFlowPropertyValueProvider extends BeanWorker implements FlowPropertyValueProvider<FlowPropertyProvider> {
 
     private Object object;
 
     /**
      * Use the {@link FlowPropertyProvider} that is passed in the {@link #get(FlowPropertyProvider, FlowPropertyDefinition)} as the starting object to trace for
      * using propertyNames.
      *
      * @param propertyNames
      */
     public ReflectionFlowPropertyValueProvider(String propertyNames) {
         super(propertyNames);
     }
     public ReflectionFlowPropertyValueProvider(Object object, String... propertyNames) {
         super(propertyNames);
         this.object = object;
     }
 
     /**
      * Used when an object other than the flowPropertyProvider passed in the {@link #get(FlowPropertyProvider, FlowPropertyDefinition)} should be used
      * as the root for tracing out properties.
      * @param object
      */
     public void setObject(Object object) {
         this.object = object;
     }
     /**
      * @return the object
      */
     public Object getObject() {
         return object;
     }
     /**
      *
      * @see org.amplafi.flow.FlowPropertyValueProvider#get(org.amplafi.flow.flowproperty.FlowPropertyProvider, org.amplafi.flow.FlowPropertyDefinition)
      */
     @SuppressWarnings("unchecked")
     @Override
     public <T> T get(FlowPropertyProvider flowPropertyProvider, FlowPropertyDefinition flowPropertyDefinition) {
         final Object base = this.object==null?flowPropertyProvider:this.object;
         return (T) getValue(base, this.getPropertyName(0));
     }
     /**
      * @see org.amplafi.flow.FlowPropertyValueProvider#getFlowPropertyProviderClass()
      */
     @Override
     public Class<FlowPropertyProvider> getFlowPropertyProviderClass() {
         return FlowPropertyProvider.class;
     }
     @Override
     public boolean isHandling(FlowPropertyDefinition flowPropertyDefinition) {
        for(String complexPropertyName:this.getPropertyNames()) {
            int beginIndex = complexPropertyName.lastIndexOf('.');
            String simplePropertyName = beginIndex < 0?complexPropertyName:complexPropertyName.substring(beginIndex+1);
            if ( flowPropertyDefinition.isNamed(simplePropertyName)) {
                 return true;
             }
         }
         return false;
     }
 
 
 }
