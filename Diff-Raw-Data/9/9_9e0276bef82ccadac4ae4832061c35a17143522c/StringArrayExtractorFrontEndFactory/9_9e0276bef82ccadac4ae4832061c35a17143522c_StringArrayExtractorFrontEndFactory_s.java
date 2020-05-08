 /**
  * Licensed under MPL / LGPL dual-license.
  *
  * Copyright (c) 2010 Tamas Eppel <Tamas.Eppel@gmail.com>
  *
  * You should have received a copy of the licenses
  * along with this program.
  * If not, see:
  *
  *    <http://www.gnu.org/licenses/>
  *    <http://www.mozilla.org/MPL/MPL-1.1.html>
  */
 package org.pcosta.vax.impl;
 
 import java.util.Map;
 
 import org.pcosta.vax.ExtractorFrontEnd;
 import org.pcosta.vax.FrontEndFactory;
 
 /**
  *
  * @author Tamas.Eppel@gmail.com
  *
  */
public class StringArrayExtractorFrontEndFactory implements FrontEndFactory<Map<String, String[]>> {
 
     private String keySeparator;
 
     private Boolean skipBlanks;
 
     private Boolean qualified;
 
     private StringArrayExtractorFrontEndFactory() {
 
     }
 
     public static StringArrayExtractorFrontEndFactory instance() {
         return new StringArrayExtractorFrontEndFactory();
     }
 
     @Override
     public ExtractorFrontEnd<Map<String, String[]>> create() {
         final StringArrayExtractorFrontEnd frontEnd = new StringArrayExtractorFrontEnd();
         if (keySeparator != null) {
             frontEnd.setKeySeparator(keySeparator);
         }
         if (skipBlanks != null) {
             frontEnd.setSkipBlanks(skipBlanks);
         }
         if (qualified != null) {
             frontEnd.setQualified(qualified);
         }
         return frontEnd;
     }
 
     public StringArrayExtractorFrontEndFactory keySeparator(final String keySeparator) {
         this.keySeparator = keySeparator;
         return this;
     }
 
     public StringArrayExtractorFrontEndFactory skipBlanks(final boolean skipBlanks) {
         this.skipBlanks = skipBlanks;
         return this;
     }
 
     public StringArrayExtractorFrontEndFactory qualified(final boolean qualified) {
         this.qualified = qualified;
         return this;
     }
 
 }
