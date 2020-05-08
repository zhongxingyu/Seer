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
 
 package net.sf.okapi.applications.rainbow.packages.rtf;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.io.UnsupportedEncodingException;
 
 import net.sf.okapi.applications.rainbow.packages.BaseWriter;
 import net.sf.okapi.common.Event;
 import net.sf.okapi.common.IParameters;
 import net.sf.okapi.common.Util;
 import net.sf.okapi.common.encoder.EncoderManager;
 import net.sf.okapi.common.resource.DocumentPart;
 import net.sf.okapi.common.resource.Ending;
 import net.sf.okapi.common.resource.StartDocument;
 import net.sf.okapi.common.resource.StartGroup;
 import net.sf.okapi.common.resource.StartSubDocument;
 import net.sf.okapi.common.resource.TextUnit;
 import net.sf.okapi.common.skeleton.ISkeletonWriter;
 import net.sf.okapi.common.writer.ILayerProvider;
 
 public class Writer extends BaseWriter {
 	
 	private static final String   EXTENSION = ".rtf";
 
 	private ISkeletonWriter skelWriter;
 	private ILayerProvider layer;
 	private PrintWriter writer;
 	private EncoderManager encoderManager;
 	
 	public Writer (ISkeletonWriter skelWriter) {
 		super();
 		this.skelWriter = skelWriter;
 		encoderManager = new EncoderManager();
 	}
 	
 	public String getPackageType () {
 		return "rtf";
 	}
 	
 	public String getReaderClass () {
 		//TODO: Use dynamic name
 		return "net.sf.okapi.applications.rainbow.packages.rtf.Reader";
 	}
 	
 	@Override
 	public void writeStartPackage () {
 		manifest.setSourceLocation("work");
 		manifest.setTargetLocation("work");
 		manifest.setOriginalLocation("original");
 		manifest.setDoneLocation("done");
 		super.writeStartPackage();
 	}
 
 	@Override
 	public void createOutput (int docID,
 		String relativeSourcePath,
 		String relativeTargetPath,
 		String sourceEncoding,
 		String targetEncoding,
 		String filtersettings,
 		IParameters filterParams)
 	{
 		relativeWorkPath = relativeSourcePath;
 		relativeWorkPath += EXTENSION;
 
 		super.createOutput(docID, relativeSourcePath, relativeTargetPath,
 			sourceEncoding, targetEncoding, filtersettings, filterParams);
 
 		String path = manifest.getRoot() + File.separator
 			+ ((manifest.getSourceLocation().length() == 0 ) ? "" : (manifest.getSourceLocation() + File.separator)) 
 			+ relativeWorkPath;
 		Util.createDirectories(path);
 	}
 
 	public void close () {
 		if ( skelWriter != null ) {
 			skelWriter.close();
 			skelWriter = null;
 		}
 		if ( writer != null ) {
 			writer.close();
 			writer = null;
 		}
 	}
 
 	public String getName () {
 		return getClass().getName();
 	}
 
 	public IParameters getParameters () {
 		return null;
 	}
 
 	@Override
 	public void setOptions (String language,
 		String defaultEncoding)
 	{
 		//TODO: Fix encoding so we use a windows-able one (especially for utf-16)
 		super.setOptions(language, defaultEncoding);
 		layer = new LayerProvider();
 		layer.setOptions(null, encoding);
 	}
 
 	public void setParameters (IParameters params) {
 		// Nothing to do
 	}
 
 	public Event handleEvent (Event event) {
 		try {
 			switch ( event.getEventType() ) {
 			case START_DOCUMENT:
 				processStartDocument((StartDocument)event.getResource());
 				break;
 			case END_DOCUMENT:
 				processEndDocument((Ending)event.getResource());
 				close();
 				break;
 			case START_SUBDOCUMENT:
 				processStartSubDocument((StartSubDocument)event.getResource());
 				break;
 			case END_SUBDOCUMENT:
 				processEndSubDocument((Ending)event.getResource());
 				break;
 			case START_GROUP:
 				processStartGroup((StartGroup)event.getResource());
 				break;
 			case END_GROUP:
 				processEndGroup((Ending)event.getResource());
 				break;
 			case TEXT_UNIT:
 				processTextUnit((TextUnit)event.getResource());
 				break;
 			case DOCUMENT_PART:
 				processDocumentPart((DocumentPart)event.getResource());
 				break;
 			}
 		}
 		catch ( FileNotFoundException e ) {
 			throw new RuntimeException(e);
 		}
 		catch ( UnsupportedEncodingException e ) {
 			throw new RuntimeException(e);
 		}
 		catch ( IOException e ) {
 			throw new RuntimeException(e);
 		}
 		return event;
 	}
 
 	private void processStartDocument (StartDocument resource) {
 		try {
 			Util.createDirectories(outputPath);
 			writer = new PrintWriter(outputPath, encoding);
 		}
 		catch ( FileNotFoundException e ) {
 			throw new RuntimeException(e);
 		}
 		catch ( UnsupportedEncodingException e ) {
 			throw new RuntimeException(e);
 		}
 
 		//TODO: change codepage
 		writer.write("{\\rtf1\\ansi\\ansicpg" + "1252" + "\\uc1\\deff1 \n"+
 			"{\\fonttbl \n"+
 			"{\\f1 \\fmodern\\fcharset0\\fprq1 Courier New;}\n"+
 			"{\\f2 \\fswiss\\fcharset0\\fprq2 Arial;}\n"+
 			"{\\f3 \\froman\\fcharset0\\fprq2 Times New Roman;}}\n"+
 			"{\\colortbl \\red0\\green0\\blue0;\\red0\\green0\\blue0;\\red0\\green0\\blue255;"+
 			"\\red0\\green255\\blue255;\\red0\\green255\\blue0;\\red255\\green0\\blue255;"+
 			"\\red255\\green0\\blue0;\\red255\\green255\\blue0;\\red255\\green255\\blue255;"+
 			"\\red0\\green0\\blue128;\\red0\\green128\\blue128;\\red0\\green128\\blue0;"+
 			"\\red128\\green0\\blue128;\\red128\\green0\\blue0;\\red128\\green128\\blue0;"+
 			"\\red128\\green128\\blue128;\\red192\\green192\\blue192;}\n"+
 			"{\\stylesheet \n"+
 			"{\\s0 \\sb80\\slmult1\\widctlpar\\fs20\\f1 \\snext0 Normal;}\n"+
 			"{\\cs1 \\additive \\v\\cf12\\sub\\f1 tw4winMark;}\n"+
 			"{\\cs2 \\additive \\cf4\\fs40\\f1 tw4winError;}\n"+
 			"{\\cs3 \\additive \\f1\\cf11 tw4winPopup;}\n"+
 			"{\\cs4 \\additive \\f1\\cf10 tw4winJump;}\n"+
 			"{\\cs5 \\additive \\cf15\\f1\\lang1024\\noproof tw4winExternal;}\n"+
 			"{\\cs6 \\additive \\cf6\\f1\\lang1024\\noproof tw4winInternal;}\n"+
 			"{\\cs7 \\additive \\cf2 tw4winTerm;}\n"+
 			"{\\cs8 \\additive \\cf13\\f1\\lang1024\\noproof DO_NOT_TRANSLATE;}\n"+
 			"{\\cs9 \\additive Default Paragraph Font;}"+
 			"{\\cs15 \\additive \\v\\f1\\cf12\\sub tw4winMark;}"+
 			"}\n"+
 			"\\paperw11907\\paperh16840\\viewkind4\\viewscale100\\pard\\plain\\s0\\sb80\\slmult1\\widctlpar\\fs20\\f1 \n"+
 			Util.RTF_STARTCODE);
 
 		// Write the skeleton
 		writer.write(skelWriter.processStartDocument(trgLang, encoding, layer, encoderManager, resource));
 	}
 
 	private void processEndDocument (Ending resource) { 
 		writer.write(skelWriter.processEndDocument(resource));
 		writer.write(Util.RTF_ENDCODE+"}\n");
 		writer.close();
 		if ( manifest != null ) {
 			manifest.addDocument(docID, relativeWorkPath, relativeSourcePath,
 				relativeTargetPath, sourceEncoding, targetEncoding, filterID);
 		}
 	}
 
 	private void processStartSubDocument (StartSubDocument resource) throws IOException {
 		writer.write(skelWriter.processStartSubDocument(resource));
 	}
 
 	private void processEndSubDocument (Ending resource) throws IOException {
 		writer.write(skelWriter.processEndSubDocument(resource));
 	}
 
 	private void processStartGroup (StartGroup resource) throws IOException {
 		writer.write(skelWriter.processStartGroup(resource));
 	}
 
 	private void processEndGroup (Ending resource) throws IOException {
 		writer.write(skelWriter.processEndGroup(resource));
 	}
 
 	private void processTextUnit (TextUnit resource) {
 		// Write the items in the TM if needed
 		if (( tmxWriter != null ) && resource.hasTarget(trgLang) ) {
 			tmxWriter.writeItem(resource, null);
 		}
 		// Write skeleton and its content
 		writer.write(skelWriter.processTextUnit(resource));
 	}
 
 	private void processDocumentPart (DocumentPart resource) throws IOException {
 		writer.write(skelWriter.processDocumentPart(resource));
 	}
 
 }
