 /**
  * <copyright>
  * 
  * Copyright (c) 2010-2012 Thales Global Services S.A.S.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    Thales Global Services S.A.S. - initial API and implementation
  * 
  * </copyright>
  */
 package org.eclipse.emf.diffmerge.api.diff;
 
 import org.eclipse.emf.ecore.EStructuralFeature;
 
 
 /**
  * A difference which is due to the unmatched presence of a value on some feature
  * in a given comparison role.
  * @author Olivier Constant
  */
 public interface IValuePresence extends IElementRelativeDifference,
 IPresenceDifference, IMergeableDifference {
   
   /**
    * Return the feature holding the value (null stands for root containment)
    */
   EStructuralFeature getFeature();
   
   /**
   * Return the difference, if any, which is symmetrical to this one. 
    * @see IValuePresence#isSymmetricalTo(IValuePresence)
    * @return a potentially null value presence (always null if upper bound is not 1)
    */
   IValuePresence getSymmetrical();
   
   /**
    * Return the non-null value being held
    * @return a non-null object
    */
   Object getValue();
   
   /**
    * Return whether the unmatched presence is solely due to a different ordering.
    * If true, then [getFeature() == null || getFeature().isMany()] and
    * getSymmetrical() returns the opposite ordering difference.
    */
   boolean isOrder();
   
   /**
    * Return whether the given value presence corresponds to this one in the opposite role.
    * True may only be returned if the setting (element and feature) is the same.
    * If the feature is of upper bound 1, then true is returned when the given value
    * presence describes a different value in the same setting.
   * If the feature is many, then false is always returned.
    * @param peer_p a non-null value presence
    */
   boolean isSymmetricalTo(IValuePresence peer_p);
   
 }
