 /***************************************************************
  *  This file is part of the [fleXive](R) framework.
  *
  *  Copyright (c) 1999-2014
  *  UCS - unique computing solutions gmbh (http://www.ucs.at)
  *  All rights reserved
  *
  *  The [fleXive](R) project is free software; you can redistribute
  *  it and/or modify it under the terms of the GNU Lesser General Public
  *  License version 2.1 or higher as published by the Free Software Foundation.
  *
  *  The GNU Lesser General Public License can be found at
  *  http://www.gnu.org/licenses/lgpl.html.
  *  A copy is found in the textfile LGPL.txt and important notices to the
  *  license from the author are found in LICENSE.txt distributed with
  *  these libraries.
  *
  *  This library is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  For further information about UCS - unique computing solutions gmbh,
  *  please see the company website: http://www.ucs.at
  *
  *  For further information about [fleXive](R), please see the
  *  project website: http://www.flexive.org
  *
  *
  *  This copyright notice MUST APPEAR in all copies of the file!
  ***************************************************************/
 package com.flexive.shared.value;
 
 import com.flexive.shared.FxLanguage;
 import com.flexive.shared.FxSharedUtils;
 import com.flexive.shared.security.UserTicket;
 
 /**
  * A replacement for FxValue objects if the calling user has no access to this value
  *
  * @author Markus Plesser (markus.plesser@flexive.com), UCS - unique computing solutions gmbh (http://www.ucs.at)
  */
 public final class FxNoAccess extends FxValue<Object, FxNoAccess> {
 
     private static final long serialVersionUID = 7693998143844598522L;
 
     private String noAccess;
     private String orgXPath;
     private long defaultLanguage;
     private long[] translatedLanguages;
     private boolean empty;
     private boolean multilang;
     private Class valueClass;
 
     /**
      * Constructor
      *
      * @param multiLanguage   multilanguage value?
      * @param defaultLanguage the default language
      */
     public FxNoAccess(long defaultLanguage, boolean multiLanguage) {
         super(defaultLanguage, multiLanguage);
     }
 
     /**
      * Constructor
      *
      * @param ticket       to get the correct tranlsation of the no access text (if null a fallback will be used)
      * @param wrappedValue FxValue to wrap
      */
     public FxNoAccess(UserTicket ticket, FxValue wrappedValue) {
         super(wrappedValue.getDefaultLanguage(), wrappedValue.isMultiLanguage());
         this.orgXPath = wrappedValue.getXPath();
         this.defaultLanguage = wrappedValue.defaultLanguage;
         this.translatedLanguages = wrappedValue.getTranslatedLanguages();
         this.empty = wrappedValue.isEmpty();
         this.multilang = wrappedValue.isMultiLanguage();
         if (ticket != null)
             this.noAccess = FxSharedUtils.getLocalizedMessage(FxSharedUtils.SHARED_BUNDLE, ticket.getLanguage().getId(),
                     ticket.getLanguage().getIso2digit(), "shared.noAccess");
         else
             this.noAccess = "Access denied";
     }
 
     /**
      * Getter for the original XPath
      *
      * @return original xpath
      */
     public String getOriginalXPath() {
         return orgXPath;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public boolean isReadOnly() {
         return true;
     }
 
 
     /**
      * {@inheritDoc}
      */
     @Override
     public long getDefaultLanguage() {
         return defaultLanguage;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public String getDefaultTranslation() {
         return noAccess;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public String getTranslation(FxLanguage lang) {
         return noAccess;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public String getBestTranslation(long lang) {
         return noAccess;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public long[] getTranslatedLanguages() {
         return translatedLanguages.clone();
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public boolean translationExists(long languageId) {
         for (Long check : translatedLanguages)
             if (check == languageId)
                 return true;
         return false;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public boolean isEmpty() {
         return empty;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public boolean isMultiLanguage() {
         return multilang;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public Object fromString(String value) {
         return value;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public FxNoAccess copy() {
         FxNoAccess clone = new FxNoAccess(defaultLanguage, multilang);
         clone.defaultLanguage = this.defaultLanguage;
         clone.translatedLanguages = new long[translatedLanguages.length];
         System.arraycopy(translatedLanguages, 0, clone.translatedLanguages, 0, translatedLanguages.length);
         clone.empty = this.empty;
         clone.multilang = this.multilang;
         clone.valueClass = this.valueClass;
         return clone;
     }
 
     /**
      * {@inheritDoc}
      */
     @SuppressWarnings({"unchecked"})
     @Override
     public Class getValueClass() {
         return valueClass;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public Object getEmptyValue() {
         throw new UnsupportedOperationException();
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public FxNoAccess setValueData(Integer valueData) {
         //not allowed
         return this;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public void clearValueData() {
         //not allowed
     }
 
     /**
      * {@inheritDoc}
      */
     @SuppressWarnings({"EqualsWhichDoesntCheckParameterClass"})
     @Override
     public boolean equals(Object other) {
         // a no-access object should not be compared to other objects
         return this == other;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public int hashCode() {
         return System.identityHashCode(this);   // see equals
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public boolean isImmutableValueType() {
         return true;
     }
 }
