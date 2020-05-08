 /*===========================================================================
   Copyright (C) 2009-2010 by the Okapi Framework contributors
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
 
 package net.sf.okapi.filters.pensieve;
 
 import java.io.File;
 import java.io.OutputStream;
 
 import net.sf.okapi.common.Event;
 import net.sf.okapi.common.IParameters;
 import net.sf.okapi.common.Util;
 import net.sf.okapi.common.encoder.EncoderManager;
 import net.sf.okapi.common.exceptions.OkapiNotImplementedException;
 import net.sf.okapi.common.filterwriter.IFilterWriter;
 import net.sf.okapi.common.LocaleId;
 import net.sf.okapi.common.resource.Segment;
 import net.sf.okapi.common.resource.StartDocument;
 import net.sf.okapi.common.resource.TextContainer;
 import net.sf.okapi.common.resource.TextUnit;
 import net.sf.okapi.tm.pensieve.common.TranslationUnit;
 import net.sf.okapi.tm.pensieve.common.TranslationUnitVariant;
 import net.sf.okapi.tm.pensieve.writer.ITmWriter;
 import net.sf.okapi.tm.pensieve.writer.TmWriterFactory;
 
 /**
  * Implementation of the {@link IFilterWriter} interface for Pensieve TM.
  * The resources are expected to have a target entry for the given target locale.
  */
 public class PensieveFilterWriter implements IFilterWriter {
 
 	private ITmWriter writer;
 	private String directory;
 	private LocaleId srcLoc;
 	private LocaleId trgLoc;
 
 	@Override
 	public void cancel () {
 		// TODO: support cancel
 	}
 
 	@Override
 	public void close () {
 		if ( writer != null ) {
 			writer.close();
 		}
 	}
 
 	@Override
 	public String getName () {
 		return "PensieveFilterWriter";
 	}
 
 	@Override
 	public EncoderManager getEncoderManager () {
 		return null;
 	}
 
 	@Override
 	public IParameters getParameters () {
 		return null;
 	}
 
 	@Override
 	public Event handleEvent (Event event) {
 		switch ( event.getEventType() ) {
 		case START_DOCUMENT:
 			handleStartDocument(event);
 			break;
 		case TEXT_UNIT:
 			handleTextUnit(event);
 			break;
 		case END_DOCUMENT:
 			close();
 			break;
 		}
 		return event;
 	}
 
 	@Override
 	public void setOptions (LocaleId locale,
 		String defaultEncoding)
 	{
 		trgLoc = locale;
 		// Encoding is ignored in this writer
 	}
 
 	@Override
 	public void setOutput (String path) {
 		File f = new File(path);
 		// We need to make sure this is absolute
 		directory = f.getAbsolutePath(); // We assume it is a directory
 	}
 
 	@Override
 	public void setOutput (OutputStream output) {
 		throw new OkapiNotImplementedException("Output type not supported.");
 	}
 
 	@Override
 	public void setParameters (IParameters params) {
 		// No parameters for now.
 	}
 
 	private void handleStartDocument (Event event) {
 		Util.createDirectories(directory + File.separator);
 		// TODO: Move this check at the pensieve package level
 		File file = new File(directory + File.separator + "segments.gen");
 		// Create a new index only if one does not exists yet
 		// If one exists we pass false to append to it
 		writer = TmWriterFactory.createFileBasedTmWriter(directory, !file.exists());
 		StartDocument sd = (StartDocument) event.getResource();
 		srcLoc = sd.getLocale();
 	}
 
 	private void handleTextUnit (Event event) {
 		TextUnit tu = (TextUnit) event.getResource();
 		if ( !tu.hasTarget(trgLoc) ) return;
 
 		TextContainer srcCont = tu.getSource();
 		TextContainer trgCont = tu.getTarget(trgLoc);
 		
 		// Check if we have the same number of segments
 		if ( trgCont.getSegmentCount() != srcCont.getSegmentCount() ) {
 			// Make sure we have at least the full full entry
 			TranslationUnitVariant source = new TranslationUnitVariant(srcLoc, srcCont.getUnSegmentedContentCopy());
 			TranslationUnitVariant target = new TranslationUnitVariant(trgLoc, trgCont.getUnSegmentedContentCopy());
 			TranslationUnit trUnit = new TranslationUnit(source, target);
 			// TODO: what do we do with properties? e.g. tuid should not be used as it
 			// PensieveUtil.populateMetaDataFromProperties(tu, trUnit);
 			writer.indexTranslationUnit(trUnit);
 			// TODO: Log a warning
 			return;
 		}
 
 		// Index each segment
 		for ( Segment srcSeg : srcCont ) {
 			Segment trgSeg = trgCont.getSegment(srcSeg.id);
 			// Skip entries with no target match
 			if ( trgSeg != null ) {
 				TranslationUnitVariant source = new TranslationUnitVariant(srcLoc, srcSeg.text);
 				TranslationUnitVariant target = new TranslationUnitVariant(trgLoc, trgSeg.text);
 				TranslationUnit trUnit = new TranslationUnit(source, target);
 				// TODO: what do we do with properties? e.g. tuid should not be used as it
 				// PensieveUtil.populateMetaDataFromProperties(tu, trUnit);
 				writer.indexTranslationUnit(trUnit);
 			}
 		}
 	}
 
 }
