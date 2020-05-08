 /*
  * apertium-translator-java-api
  * 
  * Copyright 2011 Jonathan Griggs <jonathan.griggs at gmail.com>.
  * Copyright 2011 Robert Theis
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package com.robtheis.aptr.language;
 
 /**
  * Language - an enum of language codes supported by the Apertium API
  */
 public enum Language {
   ASTURIAN("ast"),
  BASQUE("EU"),
   BRETONA("br"),
   BULGARIAN("bg"),
  CATALAN("CA"),
   DANISH("da"),
   ENGLISH("en"),
   ESPERANTO("eo"),
   FRENCH("fr"),
   GALICIAN("gl"),
   ICELANDIC("is"),
   ITALIAN("it"),
   MACEDONIAN("mk"),
   NORWEGIAN_BOKMAL("nb"),
   NORWEGIAN_NYNORSK("nn"),
   OCCITAN("oc"),
   PORTUGUESE("pt"),
   ROMANIAN("ro"),
   SPANISH("es"),
   SWEDISH("sv"),
   WELSH("cy");
 
   /**
    * String representation of this language.
    */
   private final String language;
 
   /**
    * Enum constructor.
    * @param pLanguage The language identifier.
    */
   private Language(final String pLanguage) {
     language = pLanguage;
   }
 
   public static Language fromString(final String pLanguage) {
     for (Language l : values()) {
       if (l.toString().equals(pLanguage)) {
         return l;
       }
     }
     return null;
   }
 
   /**
    * Returns the String representation of this language.
    * @return The String representation of this language.
    */
   @Override
   public String toString() {
     return language;
   }
 
 }
