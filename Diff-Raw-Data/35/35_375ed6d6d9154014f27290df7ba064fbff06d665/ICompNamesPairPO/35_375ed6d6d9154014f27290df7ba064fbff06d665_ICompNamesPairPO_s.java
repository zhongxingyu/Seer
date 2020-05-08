 /*******************************************************************************
  * Copyright (c) 2004, 2010 BREDEX GmbH.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     BREDEX GmbH - initial API and implementation and/or initial documentation
  *******************************************************************************/
 package org.eclipse.jubula.client.core.model;
 
 
 /**
  * @author BREDEX GmbH
  * @created 20.12.2005
  */
 public interface ICompNamesPairPO 
         extends IPersistentObject, IComponentNameReuser {
     /**
      * @return Returns the propagated property.
      */
     public abstract boolean isPropagated();
 
     /**
      * @param propagated The propagated property to set.
      */
     public abstract void setPropagated(boolean propagated);
 
     /**
      * @param firstName the GUID of the CompnamesPairPO of the first name.
      */
     public void setFirstName(String firstName);
     
     /**
      * @return Returns the GUID of the CompnamesPairPO of the first name.
      */
     public abstract String getFirstName();
 
     /**
      * @return Returns the GUID of the CompnamesPairPO of the second name.
      */
     public abstract String getSecondName();
 
     /**
      * @param secondName the GUID of the CompnamesPairPO of the second name.
      */
     public abstract void setSecondName(String secondName);
 
     /**
      * @return <code>true</code> if the first and the second name are equal,
      *         <code>false</code> otherwise
      */
     public abstract boolean areNamesEqual();
     
     /**
      * @return the current component type
      */
     public abstract String getType();
     
     /**
      * @param type the current type to set
     * FIXME Clemens: make private, when persisting in DB
      */
     public abstract void setType(String type);
 }
