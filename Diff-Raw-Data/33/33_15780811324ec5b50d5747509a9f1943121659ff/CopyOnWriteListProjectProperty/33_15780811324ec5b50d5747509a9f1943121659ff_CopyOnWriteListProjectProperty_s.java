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
 
 import hudson.util.CopyOnWriteList;
 import org.eclipse.hudson.api.model.ICascadingJob;
 
 /**
  * Project property for {@link CopyOnWriteList}
  * <p/>
  * Date: 11/1/11
  *
  * @author Nikita Levyankov
  */
 public class CopyOnWriteListProjectProperty extends BaseProjectProperty<CopyOnWriteList> {
 
     public CopyOnWriteListProjectProperty(ICascadingJob job) {
         super(job);
     }
 
     @Override
     public CopyOnWriteList getDefaultValue() {
         CopyOnWriteList result = new CopyOnWriteList();
        setOriginalValue(result, false);
         return result;
     }
 
     @Override
     protected boolean returnOriginalValue() {
         return isOverridden() || !getOriginalValue().isEmpty();
     }
 
     @Override
     public CopyOnWriteList getOriginalValue() {
         CopyOnWriteList result = super.getOriginalValue();
         return null != result ? result : getDefaultValue();
     }
 
     @Override
     protected void clearOriginalValue(CopyOnWriteList originalValue) {
         setOriginalValue(originalValue, false);
     }
 }
