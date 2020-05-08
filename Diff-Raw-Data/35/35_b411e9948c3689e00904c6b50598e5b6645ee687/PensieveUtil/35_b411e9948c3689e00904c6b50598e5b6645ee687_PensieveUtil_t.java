 /*===========================================================================
 Copyright (C) 2008-2010 by the Okapi Framework contributors
 -----------------------------------------------------------------------------
 This library is free software; you can redistribute it and/or modify it
 under the terms of the GNU Lesser General Public License as published by
 the Free Software Foundation; either version 2.1 of the License, or (at
 your option) any later version.
 
 This library is distributed in the hope that it will be useful, but
 WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser
 General Public License for more details.
 
 You should have received a copy of the GNU Lesser General Public License
 along with this library; if not, write to the Free Software Foundation,
 Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 
 See also the full LGPL text here: http://www.gnu.org/copyleft/lesser.html
 ===========================================================================*/
 
 package net.sf.okapi.tm.pensieve.common;
 
 import net.sf.okapi.common.LocaleId;
 import net.sf.okapi.common.resource.TextContainer;
 import net.sf.okapi.common.resource.TextUnit;
 import net.sf.okapi.common.resource.Property;
 
 /**
  * A helper class
  */
 public final class PensieveUtil {
 
     private PensieveUtil(){}
 
     /**
      * Converts an un-segmented TextUnit into a TranslationUnit. The target may not exist, in that case
      * its content is stored as a null.
      * @param sourceLoc the source locale to transform.
      * @param targetLoc the target locale to transform.
      * @param textUnit the text unit to convert.
      * @return a TranslationUnit that represents the TextUnit
      */
     public static TranslationUnit convertToTranslationUnit (LocaleId sourceLoc,
     	LocaleId targetLoc,
     	TextUnit textUnit)
     {
         TranslationUnitVariant source = new TranslationUnitVariant(sourceLoc, textUnit.getSource().getFirstPartContent());
         TextContainer tc = textUnit.getTarget(targetLoc); // Allow null target content
         TranslationUnitVariant target = new TranslationUnitVariant(targetLoc, (( tc==null ) ? null : tc.getFirstPartContent()));
         TranslationUnit tu = new TranslationUnit(source, target);
         populateMetaDataFromProperties(textUnit, tu);
         return tu;
     }
 
     /**
      * Converts a TranslationUnit to a TextUnit
      * @param tu The TranslationUnit to convert.
      * @return The converted TextUnit
      */
     public static TextUnit convertToTextUnit (TranslationUnit tu) {
         TextUnit textUnit;
         String tuid = tu.getMetadata().get(MetadataType.ID);
 
         textUnit = new TextUnit(tuid);
         if (tuid != null) {
             textUnit.setName(tuid);
         }
         textUnit.setSourceContent(tu.getSource().getContent());
         textUnit.setTargetContent(tu.getTarget().getLanguage(), tu.getTarget().getContent());
         for (MetadataType type : tu.getMetadata().keySet()) {
             if (type != MetadataType.ID) {
                 textUnit.setProperty(new Property(type.fieldName(), tu.getMetadata().get(type)));
             }
         }
         return textUnit;
     }
 
 
 
     private static void populateMetaDataFromProperties (TextUnit textUnit,
     	TranslationUnit tu)
     {
         MetadataType mdt;
         for (String key : textUnit.getPropertyNames()) {
            mdt = MetadataType.findMetadataType(key);
             if (mdt != null) {
                 tu.getMetadata().put(mdt, textUnit.getProperty(key).getValue());
             } else {
                //TODO: What do we do if mapping for metadata doesn't exist?  It is enough for simpleTM replacement to
                 //support a finite set.
             }
         }
     }
 }
