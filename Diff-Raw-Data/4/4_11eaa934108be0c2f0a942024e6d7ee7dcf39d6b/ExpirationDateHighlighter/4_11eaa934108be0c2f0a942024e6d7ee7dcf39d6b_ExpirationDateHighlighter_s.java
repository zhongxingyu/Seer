 /*
  * Copyright 2012 Danylo Vashchilenko
  *
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not
  * use this file except in compliance with the License. You may obtain a copy of
  * the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
  * License for the specific language governing permissions and limitations under
  * the License.
  */
 package org.key2gym.presentation.highlighters;
 
 import java.awt.Color;
 import javax.swing.JTextField;
 import org.joda.time.DateMidnight;
 import org.joda.time.Days;
 import org.joda.time.format.DateTimeFormat;
 import org.key2gym.presentation.colors.Palette;
 
 /**
  * 
  * @author Danylo Vashchilenko
  */
 public class ExpirationDateHighlighter extends AbstractHighlighter {
     
     public ExpirationDateHighlighter(JTextField textField) {
         super(textField);
     }
 
   
     /**
      * Updates the component's highlight.
      */
     @Override
     protected ColorScheme getHighlightModelFor(String text) {
         
         DateMidnight value;
         
         try {
             value = DateMidnight.parse(text, DateTimeFormat.forPattern("dd-MM-yyyy"));
         } catch(IllegalArgumentException ex) {
             value = null;
         }
 
         if (value == null) {
             return NULL_SCHEME;
         }
 
         int daysTillExpiration = Days.daysBetween(DateMidnight.now(), value).getDays();
 
 
         if (daysTillExpiration > 2) {
             return NOT_SOON_SCHEME;
         } else if (daysTillExpiration > 0) {
             return SOON_SCHEME;
         } else {
             return PASSED_SCHEME;
         }
     }
     
     private static final ColorScheme SOON_SCHEME = new ColorScheme(
             Palette.EXPIRATION_DATE_SOON_BACKGROUND, 
             Palette.EXPIRATION_DATE_SOON_FOREGROUND);
     
     private static final ColorScheme PASSED_SCHEME = new ColorScheme(
            Palette.EXPIRATION_DATE_SOON_BACKGROUND, 
            Palette.EXPIRATION_DATE_SOON_FOREGROUND);
     
     private static final ColorScheme NOT_SOON_SCHEME = new ColorScheme(
             Palette.EXPIRATION_DATE_NOT_SOON_BACKGROUND,
             Palette.EXPIRATION_DATE_NOT_SOON_FOREGROUND);
 }
