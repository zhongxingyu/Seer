 /*
  Copyright (c) 2000-2005 University of Washington.  All rights reserved.
 
  Redistribution and use of this distribution in source and binary forms,
  with or without modification, are permitted provided that:
 
    The above copyright notice and this permission notice appear in
    all copies and supporting documentation;
 
    The name, identifiers, and trademarks of the University of Washington
    are not used in advertising or publicity without the express prior
    written permission of the University of Washington;
 
    Recipients acknowledge that this distribution is made available as a
    research courtesy, "as is", potentially with defects, without
    any obligation on the part of the University of Washington to
    provide support, services, or repair;
 
    THE UNIVERSITY OF WASHINGTON DISCLAIMS ALL WARRANTIES, EXPRESS OR
    IMPLIED, WITH REGARD TO THIS SOFTWARE, INCLUDING WITHOUT LIMITATION
    ALL IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
    PARTICULAR PURPOSE, AND IN NO EVENT SHALL THE UNIVERSITY OF
    WASHINGTON BE LIABLE FOR ANY SPECIAL, INDIRECT OR CONSEQUENTIAL
    DAMAGES OR ANY DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR
    PROFITS, WHETHER IN AN ACTION OF CONTRACT, TORT (INCLUDING
    NEGLIGENCE) OR STRICT LIABILITY, ARISING OUT OF OR IN CONNECTION WITH
    THE USE OR PERFORMANCE OF THIS SOFTWARE.
  */
 /* **********************************************************************
     Copyright 2005 Rensselaer Polytechnic Institute. All worldwide rights reserved.
 
     Redistribution and use of this distribution in source and binary forms,
     with or without modification, are permitted provided that:
        The above copyright notice and this permission notice appear in all
         copies and supporting documentation;
 
         The name, identifiers, and trademarks of Rensselaer Polytechnic
         Institute are not used in advertising or publicity without the
         express prior written permission of Rensselaer Polytechnic Institute;
 
     DISCLAIMER: The software is distributed" AS IS" without any express or
     implied warranty, including but not limited to, any implied warranties
     of merchantability or fitness for a particular purpose or any warrant)'
     of non-infringement of any current or pending patent rights. The authors
     of the software make no representations about the suitability of this
     software for any particular purpose. The entire risk as to the quality
     and performance of the software is with the user. Should the software
     prove defective, the user assumes the cost of all necessary servicing,
     repair or correction. In particular, neither Rensselaer Polytechnic
     Institute, nor the authors of the software are liable for any indirect,
     special, consequential, or incidental damages related to the software,
     to the maximum extent the law permits.
 */
 
 package org.bedework.appcommon;
 
 import java.io.Serializable;
 
 /** This class is used by the clients to determine which of two incoming
  * request parameters will set the final value. It is typically used when we
  * have preferred and all select boxes and we get an id from each.
  *
  * <p>We have two values, A and B
  *
  * @author  Mike Douglass douglm @ rpi.edu
  */
 public class IntSelectId implements Serializable {
  /** Neither A nor B has precedence
    */
   public static final int NoneHasPrecedence = 0;
 
   /** A has precedence
    */
   public static final int AHasPrecedence = 1;
 
   /** B has precedence
    */
   public static final int BHasPrecedence = 2;
 
   private int precedence;
 
   private int originalValue;
   private int newValue;
 
   private boolean changed;
 
   /** Create an object with the given original value in which neither A nor B
    * have preference.
    *
    * @param originalValue int
    */
   public IntSelectId(int originalValue) {
     this.originalValue = originalValue;
     this.newValue = originalValue;
   }
 
   /** Create an object with the given original value indicating which of A
    * or B have preference.
    *
    * @param originalValue
    * @param preferred
    */
   public IntSelectId(int originalValue, int preferred) {
     reset(originalValue, preferred);
   }
 
   /** Reset an object with the given original value indicating which of A
    * or B have preference.
    *
    * @param originalValue
    * @param preferred
    */
   public void reset(int originalValue, int preferred) {
     this.originalValue = originalValue;
     this.newValue = originalValue;
     precedence = preferred;
   }
 
   /** Set the A value
    *
    * @param val
    */
   public void setA(int val) {
     if (val == originalValue) {
       return;
     }
 
     if (!changed ||
         (precedence == AHasPrecedence) ||
         (precedence == NoneHasPrecedence)) {
       newValue = val;
       changed = true;
     }
   }
 
   /** Set the B value
    *
    * @param val
    */
   public void setB(int val) {
     if (val == originalValue) {
       return;
     }
 
     if (!changed ||
         (precedence == BHasPrecedence) ||
         (precedence == NoneHasPrecedence)) {
       newValue = val;
       changed = true;
     }
   }
 
   /** Return true if value changed
    *
    * @return boolean
    */
   public boolean getChanged() {
     return changed;
   }
 
   /**
    * @return int original value
    */
   public int getOriginalVal() {
     return originalValue;
   }
 
   /**
    * @return int new value
    */
   public int getVal() {
     return newValue;
   }
 }
 
