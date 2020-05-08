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
 
 /**
  * @author patmoore
  *
  */
 public interface FlowPropertyProviderWithValues extends FlowPropertyProvider {
 
     /**
      * override to treat some properties as special. This method is called by
      * FlowPropertyBinding.
      *
      * Will trigger property initialization including possible call to {@link org.amplafi.flow.FlowPropertyValueProvider}
      *
     * TO_PAT: The method is no longer invoked by FlowState.
     * Seems that now getProperty(String key, Class<? extends T> expected)
     * has to be overriden to get the special properties through binding.
     *   
      * @param key
      * @param <T> type of property.
      * @return property
      */
     <T> T getProperty(String key);
     <T> T getProperty(String key, Class<? extends T> expected);
 
     /**
      * override to treat some properties as special. This method is called by
      * FlowPropertyBinding. Default behavior caches value and sets the property
      * to value.
      *
      * TODO: If called on a definition have it set the default object on the definition ( if there is no other {@link org.amplafi.flow.FlowPropertyValueProvider} )
      *
      * @param key
      * @param value
      * @param <T> value's type
      * @throws UnsupportedOperationException if property modification is not supported.
      * @throws IllegalStateException if the property cannot be modified ( but other properties maybe could be modified )
      */
     <T> void setProperty(String key, T value) throws UnsupportedOperationException, IllegalStateException;
 
     /**
      * checks to see if {@link #isPropertyValueSet(String)} or if the property has a {@link org.amplafi.flow.FlowPropertyValueProvider}.
      *
      * This method is NOT {@link #getProperty(String)} != null. Implementors check the actual storage to see if the value
      * is set. This method is intended to avoid initialization overhead.
      *
      * FlowPropertyValueProvider
      * are assumed to always supply a default. For complex {@link org.amplafi.flow.FlowPropertyValueProvider}s,
      * this may not always be the case.
      *
      * @param key
      * @return true if the property is set or if the property has a FlowPropertyValueProvider.
      */
     boolean isPropertySet(String key);
     /**
      * checks to see if the property is set.
      *
      * This method is NOT {@link #getProperty(String)} != null. Implementors check the actual storage to see if the value
      * is set. This method is intended to avoid initialization overhead.
      * @param key
      * @return true if the property is set
      */
     boolean isPropertyValueSet(String key);
 
     /**
      * Used when dealing with Boolean properties. Allows nulls to be handled when converting to a boolean.
      * @param key
      * @return false if property value is null or {@link Boolean#FALSE}
      */
     boolean isTrue(String key);
 }
