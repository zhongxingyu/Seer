 /*******************************************************************************
  *
  * Copyright (c) 2011 Oracle Corporation.
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *
  *    Nikita Levyankov
  *
  *******************************************************************************/
 package org.eclipse.hudson.model.project.property;
 
 import hudson.util.DeepEquals;
 import hudson.util.DescribableList;
 import java.util.List;
 import org.apache.commons.collections.CollectionUtils;
 import org.eclipse.hudson.api.model.ICascadingJob;
 
 /**
  * Property represents DescribableList object.
  * <p/>
  * Date: 10/3/11
  *
  * @author Nikita Levyankov
  */
 public class DescribableListProjectProperty extends BaseProjectProperty<DescribableList> {
 
     public DescribableListProjectProperty(ICascadingJob job) {
         super(job);
     }
 
     @Override
     public DescribableList getDefaultValue() {
         DescribableList result = new DescribableList(getJob());
         return result;
     }
 
     @Override
     public boolean allowOverrideValue(DescribableList cascadingValue, DescribableList candidateValue) {
         if (null == cascadingValue && null == candidateValue) {
             return false;
         }
         if (null != cascadingValue && null != candidateValue) {
             List cascadingList = cascadingValue.toList();
             List candidateList = candidateValue.toList();
             return !(CollectionUtils.isEqualCollection(cascadingList, candidateList) || DeepEquals.deepEquals(cascadingList, candidateList));
 
         }
         return true;
     }
 
     @Override
     protected boolean returnOriginalValue() {
         return isOverridden() || !getOriginalValue().isEmpty();
     }
 
     @Override
     public DescribableList getOriginalValue() {
         DescribableList result = super.getOriginalValue();
         return null != result ? result : getDefaultValue();
     }
 }
