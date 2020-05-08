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
 ============================================================================*/
 
 package net.sf.okapi.steps.simplekit.common;
 
 import net.sf.okapi.common.IParameters;
 import net.sf.okapi.common.Util;
 import net.sf.okapi.common.annotation.AltTranslation;
 import net.sf.okapi.common.annotation.AltTranslationsAnnotation;
import net.sf.okapi.common.annotation.ScoresAnnotation;
 import net.sf.okapi.common.encoder.EncoderManager;
 import net.sf.okapi.common.exceptions.OkapiIOException;
 import net.sf.okapi.common.filterwriter.TMXWriter;
 import net.sf.okapi.common.LocaleId;
 import net.sf.okapi.common.resource.Property;
 import net.sf.okapi.common.resource.Segment;
 import net.sf.okapi.common.resource.ISegments;
 import net.sf.okapi.common.resource.TextContainer;
 import net.sf.okapi.common.resource.TextFragment;
 import net.sf.okapi.common.resource.TextUnit;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipOutputStream;
 
 public abstract class BasePackageWriter implements IPackageWriter {
 	
 	protected Manifest manifest;
 	protected boolean useManifest;
 	protected int docID;
 	protected String inputRoot;
 	protected String relativeWorkPath;
 	protected String relativeSourcePath;
 	protected String relativeTargetPath;
 	protected String sourceEncoding;
 	protected String targetEncoding;
 	protected String filterId;
 	protected IParameters filterParams;
 	protected TMXWriter tmxWriterApproved;
 	protected String tmxPathApproved;
 	protected TMXWriter tmxWriterUnApproved;
 	protected String tmxPathUnApproved;
 	protected TMXWriter tmxWriterAlternate;
 	protected String tmxPathAlternate;
 	protected TMXWriter tmxWriterLeverage;
 	protected String tmxPathLeverage;
 	protected LocaleId trgLoc;
 	protected String encoding;
 	protected String outputPath;
 	protected String creationTool;
 	protected EncoderManager encoderManager;
 	
 	public BasePackageWriter () {
 		manifest = new Manifest();
 		manifest.setReaderClass(getReaderClass());
 	}
 	
 	public void setUseManifest (boolean useManifest) {
 		this.useManifest = useManifest;
 	}
 
 	public void cancel () {
 		//TODO: implement cancel()
 	}
 	
 	public void setInformation (LocaleId sourceLocale,
 		LocaleId targetLocale,
 		String projectID,
 		String outputFolder,
 		String packageID,
 		String sourceRoot,
 		String creationTool)
 	{
 		manifest.setSourceLanguage(sourceLocale);
 		manifest.setTargetLanguage(targetLocale);
 		manifest.setProjectID(projectID);
 		manifest.setRoot(outputFolder);
 		manifest.setPackageID(packageID);
 		manifest.setPackageType(getPackageType());
 		this.trgLoc = targetLocale;
 		this.inputRoot = sourceRoot;
 		this.creationTool = creationTool;
 	}
 
 	public void setEncoderManager (EncoderManager encoderManager) {
 		this.encoderManager = encoderManager;
 	}
 	
 	@Override
 	public EncoderManager getEncoderManager () {
 		return encoderManager;
 	}
 	
 	@Override
 	public void writeStartPackage () {
 		// Create the root directory
 		Util.createDirectories(manifest.getRoot());
 		
 		String tmp = manifest.getSourceLocation();
 		if ( !Util.isEmpty(tmp) ) {
 			Util.createDirectories(manifest.getRoot() + File.separator + tmp + File.separator);
 		}
 		
 		tmp = manifest.getTargetLocation();
 		if ( !Util.isEmpty(tmp) ) {
 			Util.createDirectories(manifest.getRoot() + File.separator + tmp + File.separator);
 		}
 
 		tmp = manifest.getSkeletonLocation();
 		if ( !Util.isEmpty(tmp) ) {
 			Util.createDirectories(manifest.getRoot() + File.separator + tmp + File.separator);
 		}
 
 		// No need to create the folder structure for the 'done' folder
 		// It will be done when merging
 		
 		// Create the reference TMX (approved pre-translations found in the source files)
 		if ( tmxPathApproved == null ) {
 			tmxPathApproved = manifest.getRoot() + File.separator + "approved.tmx";
 		}
 		tmxWriterApproved = new TMXWriter(tmxPathApproved);
 		tmxWriterApproved.writeStartDocument(manifest.getSourceLanguage(),
 			manifest.getTargetLanguage(), creationTool, null, null, null, null);
 
 		// Create the reference TMX (un-approved pre-translations found in the source files)
 		if ( tmxPathUnApproved == null ) {
 			tmxPathUnApproved = manifest.getRoot() + File.separator + "unapproved.tmx";
 		}
 		tmxWriterUnApproved = new TMXWriter(tmxPathUnApproved);
 		tmxWriterUnApproved.writeStartDocument(manifest.getSourceLanguage(),
 			manifest.getTargetLanguage(), creationTool, null, null, null, null);
 
 		// Create the reference TMX (alternate found in the source files)
 		if ( tmxPathAlternate == null ) {
 			tmxPathAlternate = manifest.getRoot() + File.separator + "alternate.tmx";
 		}
 		tmxWriterAlternate = new TMXWriter(tmxPathAlternate);
 		tmxWriterAlternate.writeStartDocument(manifest.getSourceLanguage(),
 			manifest.getTargetLanguage(), creationTool, null, null, null, null);
 		// Make sure we output only alt-translation annotation coming from the source itself
 		tmxWriterAlternate.setAltTranslationOption(AltTranslation.ORIGIN_SOURCEDOC);
 
 		// Create the reference TMX (pre-translation TM)
 		if ( tmxPathLeverage == null ) {
 			tmxPathLeverage = manifest.getRoot() + File.separator + "leverage.tmx";
 		}
 		tmxWriterLeverage = new TMXWriter(tmxPathLeverage);
 		tmxWriterLeverage.writeStartDocument(manifest.getSourceLanguage(),
 			manifest.getTargetLanguage(), creationTool, null, null, null, null);
 	}
 
 	@Override
 	public void writeEndPackage (boolean createZip) {
 		try {
 			// Save the manifest
 			if ( manifest != null ) {
 				manifest.Save();
 			}
 	
 			tmxWriterApproved.writeEndDocument();
 			tmxWriterApproved.close();
 			if ( tmxWriterApproved.getItemCount() == 0 ) {
 				File file = new File(tmxPathApproved);
 				file.delete();
 			}
 
 			tmxWriterUnApproved.writeEndDocument();
 			tmxWriterUnApproved.close();
 			if ( tmxWriterUnApproved.getItemCount() == 0 ) {
 				File file = new File(tmxPathUnApproved);
 				file.delete();
 			}
 
 			tmxWriterAlternate.writeEndDocument();
 			tmxWriterAlternate.close();
 			if ( tmxWriterAlternate.getItemCount() == 0 ) {
 				File file = new File(tmxPathAlternate);
 				file.delete();
 			}
 
 			tmxWriterLeverage.writeEndDocument();
 			tmxWriterLeverage.close();
 			if ( tmxWriterLeverage.getItemCount() == 0 ) {
 				File file = new File(tmxPathLeverage);
 				file.delete();
 			}
 
 			// Zip the package if needed
 			if ( createZip && ( manifest != null )) {
 				Compression.zipDirectory(manifest.getRoot(), manifest.getRoot() + ".zip");
 			}
 		}
 		catch ( IOException e ) {
 			throw new RuntimeException(e);
 		}
 	}
 
 	@Override
 	public void createOutput (int docID,
 		String relativeSourcePath,
 		String relativeTargetPath,
 		String sourceEncoding,
 		String targetEncoding,
 		String filterId,
 		IParameters filterParams,
 		EncoderManager encoderManager)
 	{
 		if ( relativeSourcePath == null ) throw new NullPointerException();
 		if ( relativeTargetPath == null ) throw new NullPointerException();
 		if ( sourceEncoding == null ) throw new NullPointerException();
 		if ( targetEncoding == null ) throw new NullPointerException();
 		if ( filterId == null ) throw new NullPointerException();
 
 		this.docID = docID;
 		this.relativeSourcePath = relativeSourcePath;
 		this.relativeTargetPath = relativeTargetPath;
 		this.sourceEncoding = sourceEncoding;
 		this.targetEncoding = targetEncoding;
 		this.filterId = filterId;
 		this.filterParams = filterParams;
 		this.encoderManager = encoderManager;
 		
 		// If needed copy the original input to the package
 		String subFolder = manifest.getSkeletonLocation();
 		if ( Util.isEmpty(subFolder) ) return;
 				
 //		String inputPath = inputRoot + File.separator + relativeSourcePath;
 //		String docPrefix = String.format("%d.", docID);
 //			
 //		String destination = manifest.getRoot() + File.separator + subFolder
 //			+ File.separator + docPrefix + "ori"; // docPrefix has a dot
 //		Util.copyFile(inputPath, destination, false);
 //			
 //		String paramsCopy = manifest.getRoot() + File.separator + subFolder
 //			+ File.separator + docPrefix + "fprm";
 //		if ( filterParams != null ) {
 //			filterParams.save(paramsCopy);
 //		}
 		
 		// Set the options for the actual writer
 		String outputPath = manifest.getRoot() + File.separator
 			+ ((manifest.getSourceLocation().length() == 0 ) ? "" : (manifest.getSourceLocation() + File.separator)) 
 			+ relativeWorkPath;
 		setOptions(trgLoc, targetEncoding);
 		setOutput(outputPath);
 	}
 
 	@Override
 	public void createCopies (int docID,
 		String relativeSourcePath)
 	{
 		if ( relativeSourcePath == null ) {
 			throw new NullPointerException("Argument relativeSourcePath must not be null.");
 		}
 
 		String inputPath = inputRoot + File.separator + relativeSourcePath;
 		String outputPath = manifest.getRoot() + File.separator
 			+ ((manifest.getSourceLocation().length() == 0 ) ? "" : (manifest.getSourceLocation() + File.separator)) 
 			+ relativeSourcePath;
 
 		// If needed copy the original input to the package
 		String subFolder = manifest.getSkeletonLocation();
 		if (( subFolder != null ) && ( subFolder.length() > 0 )) {
 			String docPrefix = String.format("%d.", docID);
 			String destination = manifest.getRoot() + File.separator + subFolder
 				+ File.separator + docPrefix + "ori"; // docPrefix has a dot
 			Util.copyFile(inputPath, destination, false);
 		}
 			
 		// Copy to the work folder
 		Util.createDirectories(outputPath);
 		Util.copyFile(inputPath, outputPath, false);
 	}
 
 	@Override
 	public void setOptions (LocaleId locale,
 		String defaultEncoding)
 	{
 		trgLoc = locale;
 		encoding = defaultEncoding;
 	}
 	
 	@Override
 	public void setOutput (String path) {
 		outputPath = path;
 	}
 
 	@Override
 	public void setOutput (OutputStream output) {
 		throw new UnsupportedOperationException();
 	}
 
 	@Override
 	public void writeTMXEntries (TextUnit tu) {
 		// Write the items in the TM if needed
 		TextContainer tc = tu.getTarget(trgLoc);
 		if (( tc != null ) && ( !tc.isEmpty() )) {
 			if ( tu.getSource().isEmpty() ||
 				( tu.getSource().hasText(false) && !tc.hasText(false) )) {
 				return; // Target has code and/or spaces only
 			}
 			boolean done = false;
 			if ( tu.hasTargetProperty(trgLoc, Property.APPROVED) ) {
 				if ( tu.getTargetProperty(trgLoc, Property.APPROVED).getValue().equals("yes") ) {
 					tmxWriterApproved.writeItem(tu, null);
 					done = true;
 				}
 			}
 //TODO			
 //			if ( !done ) {
 //				ScoresAnnotation scores = tu.getTarget(trgLoc).getAnnotation(ScoresAnnotation.class);
 //				if ( scores != null ) {
 //					writeScoredItem(tu);
 //				}
 //				else { // Has some text in target, but not approved
 //					// Output to un-approved TMX only it's different from source
 //					if ( tc.compareTo(tu.getSource(), true) != 0 ) {
 //						tmxWriterUnApproved.writeItem(tu, null);
 //					}
 //				}
 //			}
 		}
 
 		// Check for alternates
 		TextContainer altCont = tu.getTarget(trgLoc);
 		if ( altCont != null ) {
 			// From the segments
 			ISegments srcSegs = tu.getSource().getSegments();
 			for ( Segment seg : altCont.getSegments() ) {
 				Segment srcSeg = srcSegs.get(seg.id);
 				if ( srcSeg == null ) continue;
 				writeAltTranslations(seg.getAnnotation(AltTranslationsAnnotation.class), srcSeg.text);
 			}
 			// From the target container
 			TextFragment srcOriginal;
 			if ( tu.getSource().contentIsOneSegment() ) {
 				srcOriginal = tu.getSource().getFirstContent();
 			}
 			else {
 				srcOriginal = tu.getSource().getUnSegmentedContentCopy();
 			}
 			writeAltTranslations(altCont.getAnnotation(AltTranslationsAnnotation.class), srcOriginal);
 		}
 	}
 	
 	private void writeAltTranslations (AltTranslationsAnnotation ann,
 		TextFragment srcOriginal)
 	{
 		if ( ann == null ) {
 			return;
 		}
 		for ( AltTranslation alt : ann ) {
 			tmxWriterAlternate.writeAlternate(alt, srcOriginal);
 		}
 	}
 
 	@Override
 	public void writeScoredItem (TextUnit item) {
 		// By default, matches go in the leverage TM
 		tmxWriterLeverage.writeItem(item, null);
 	}
 
 	public void createFilesForMerging () {
 		try {
 			if ( useManifest ){
 				createNonZippedSkeleton();
 			}
 			else {
 				createZippedSkeleton();
 			}
 		}
 		catch ( IOException e ) {
 			throw new OkapiIOException("Error when creating the skeleton data.");
 		}
 	}
 
 	private void createNonZippedSkeleton ()
 		throws IOException
 	{
 		FileOutputStream fos = null;
 		try {
 			// Copy original to skeleton directory
 			String inputPath = inputRoot + File.separator + relativeSourcePath;
 			String outputPath = manifest.getRoot() + File.separator
 				+ (Util.isEmpty(manifest.getSkeletonLocation()) ? "" : (manifest.getSkeletonLocation() + File.separator)) 
 				+ relativeSourcePath;
 			// Copy to the work folder
 			Util.copyFile(inputPath, outputPath, false);
 	
 			// Copy merging information to skeleton dircetory
     		String paramsData = null;
     		if ( filterParams != null ) {
     			paramsData = filterParams.toString();
     		}
     		MergingInfoFile mi = new MergingInfoFile(getReaderClass(), filterId, paramsData,
     			sourceEncoding, relativeTargetPath, targetEncoding);
 			outputPath += ".mrginfo";
 			fos = new FileOutputStream(outputPath);
 			fos.write(mi.save().toByteArray());
 		}
 		finally {
 			if ( fos != null ) {
 				fos.close();
 			}
 		}
 	}
 	
 	private void createZippedSkeleton ()
 		throws IOException
 	{
 		ZipOutputStream os = null;
 		FileInputStream input = null;
 		try {
 			// Create the ZIP file
 			String skeletonPath = manifest.getRoot() + File.separator
 				+ (Util.isEmpty(manifest.getSkeletonLocation()) ? "" : (manifest.getSkeletonLocation() + File.separator)) 
 				+ relativeSourcePath + ".skl";
 			Util.createDirectories(skeletonPath);
 			os = new ZipOutputStream(new FileOutputStream(skeletonPath));
 
 			// Add the original document 
 			String inputPath = inputRoot + File.separator + relativeSourcePath;
 			input = new FileInputStream(inputPath); 
        		os.putNextEntry(new ZipEntry("original.dta"));
        		int nCount; 
        		byte[] aBuf = new byte[1024];
        		while( (nCount = input.read(aBuf)) > 0 ) { 
        			os.write(aBuf, 0, nCount); 
        		}
        		os.closeEntry();
        		input.close();
        		
        		// Add the merging information file
     		String paramsData = null;
     		if ( filterParams != null ) {
     			paramsData = filterParams.toString();
     		}
     		MergingInfoFile mi = new MergingInfoFile(getReaderClass(), filterId, paramsData,
     			sourceEncoding, relativeTargetPath, targetEncoding);
        		os.putNextEntry(new ZipEntry("settings.dta"));
        		os.write(mi.save().toByteArray());
        		os.closeEntry();
         }
 		finally {
 			try {
 				if ( input != null ) input.close();
 				if ( os != null ) os.close();
 			}
 			catch ( IOException e ) {
 				throw new OkapiIOException("Error when creating the skeleton file.");
 			}
 		}
 	}
 
 }
