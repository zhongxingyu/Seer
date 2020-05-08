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
 import net.sf.okapi.tm.pensieve.seeker.TmSeeker;
 
 import java.io.IOException;
 
 /**
  * The TMX interface to Pensieve.
  */
 public interface TmxExporter {
 
 
     /**
      * Exports all Pensieve contents matching source and target language to TMX
      * @param sourceLang The source language to export
      * @param targetLang The target language to export
      * @param tmSeeker The Seeker to use when reading from the TM
      * @param tmxWriter The TMXWriter to use when writing to the TMX file
      * @throws IOException if there was a problem with the TMX export
      */
     void exportTmx(String sourceLang, String targetLang, TmSeeker tmSeeker, TMXWriter tmxWriter) throws IOException;
 
     /**
      * Exports all Pensieve contents matching source languages to TMX
      * @param sourceLang The source language to export
      * @param tmSeeker The Seeker to use when reading from the TM
      * @param tmxWriter The TMXWriter to use when writing to the TMX file
      * @throws IOException if there was a problem with the TMX export
      */
     void exportTmx(String sourceLang, TmSeeker tmSeeker, TMXWriter tmxWriter) throws IOException;
 }
