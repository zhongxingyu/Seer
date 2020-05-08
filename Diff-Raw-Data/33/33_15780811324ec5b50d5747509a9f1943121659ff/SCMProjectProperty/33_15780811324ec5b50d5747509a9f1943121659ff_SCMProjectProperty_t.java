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
  *    Anton Kozak
  *
  *******************************************************************************/
 package org.eclipse.hudson.model.project.property;
 
 import hudson.scm.NullSCM;
 import hudson.scm.SCM;
 import org.eclipse.hudson.api.model.ICascadingJob;
 
 /**
  * Represents {@link SCM} property.
  * <p/>
  * Date: 10/11/11
  *
  * @author Anton Kozak
  */
 public class SCMProjectProperty extends BaseProjectProperty<SCM> {
 
     public SCMProjectProperty(ICascadingJob job) {
         super(job);
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public SCM getDefaultValue() {
         return new NullSCM();
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     protected boolean returnOriginalValue() {
         return isOverridden() || (null != getOriginalValue() && !getDefaultValue().equals(getOriginalValue()));
     }
    
    @Override
    protected boolean updateOriginalValue(SCM value, SCM cascadingValue) {
        if ((value instanceof NullSCM) && !(cascadingValue instanceof NullSCM)) {
            value = null;
            clearOriginalValue(value);
            return false;
        } else {
            return super.updateOriginalValue(value, cascadingValue);
        }
    }
 }
