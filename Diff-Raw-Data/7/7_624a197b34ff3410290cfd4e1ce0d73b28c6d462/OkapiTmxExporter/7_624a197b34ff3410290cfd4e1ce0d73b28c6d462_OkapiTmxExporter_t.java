 /*===========================================================================
 Copyright (C) 2008-2009 by the Okapi Framework contributors
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
 package net.sf.okapi.tm.pensieve.tmx;
 
 import net.sf.okapi.common.filterwriter.TMXWriter;
 import net.sf.okapi.tm.pensieve.common.PensieveUtil;
 import net.sf.okapi.tm.pensieve.common.TranslationUnit;
 import net.sf.okapi.tm.pensieve.seeker.TmSeeker;
 
 import java.io.IOException;
 import java.util.List;
 
 /**
  * Used to interact with the Okapi Standards for TMX. For example, the property names and default fields stored.
  */
 public class OkapiTmxExporter implements TmxExporter {
 
     /**
      * Exports all target langs in Pensieve to TMX
      * @param sourceLang The source language of desired tran
      * @param tmSeeker The TMSeeker to use when reading from the TM
      * @param tmxWriter The TMXWriter to use when writing out the TMX
      */
     public void exportTmx(String sourceLang, TmSeeker tmSeeker, TMXWriter tmxWriter) throws IOException {
         exportTmx(sourceLang, null, tmSeeker, tmxWriter);
     }
 
     /**
      * Exports only specific target langs Pensieve to TMX
      * @param sourceLang The source language of desired tran
      * @param targetLang The target language of desired tran (or null for all target languages)
      * @param tmSeeker The TMSeeker to use when reading from the TM
      * @param tmxWriter The TMXWriter to use when writing out the TMX
      */
     public void exportTmx(String sourceLang, String targetLang, TmSeeker tmSeeker, TMXWriter tmxWriter) throws IOException {
         checkExportTmxParams(sourceLang, tmSeeker, tmxWriter);
         try {
             tmxWriter.writeStartDocument(sourceLang, targetLang, "pensieve", "0.0.1", "sentence", "pensieve", "unknown");
             //TODO might eat up too much memory for large TMs
             List<TranslationUnit> tus = tmSeeker.getAllTranslationUnits();
             for (TranslationUnit tu : tus) {
                 if (isWriteTextUnit(sourceLang, targetLang, tu)) {
                     tmxWriter.writeTUFull(PensieveUtil.convertToTextUnit(tu));
                 }
             }
             tmxWriter.writeEndDocument();
         } finally {
             tmxWriter.close();
         }
     }
 
     private static boolean isWriteTextUnit(String sourceLang, String targetLang, TranslationUnit tu) {
         return sourceLang.equals(tu.getSource().getLang()) && (targetLang == null || targetLang.equals(tu.getTarget().getLang()));
     }
 
     private void checkExportTmxParams(String sourceLang, TmSeeker tmSeeker, TMXWriter tmxWriter) {
         if (sourceLang == null) {
             throw new IllegalArgumentException("sourceLang was not set");
         }
         if (tmSeeker == null) {
             throw new IllegalArgumentException("tmSeeker was not set");
         }
         if (tmxWriter == null) {
             throw new IllegalArgumentException("tmxWriter was not set");
         }
     }
 }
