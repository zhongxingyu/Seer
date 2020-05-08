 package net.sf.okapi.filters.versifiedtxt;
 
 import java.io.BufferedOutputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.io.OutputStreamWriter;
 import java.nio.charset.Charset;
 
 import net.sf.okapi.common.Event;
 import net.sf.okapi.common.IParameters;
 import net.sf.okapi.common.LocaleId;
 import net.sf.okapi.common.Util;
 import net.sf.okapi.common.BOMNewlineEncodingDetector.NewlineType;
 import net.sf.okapi.common.encoder.EncoderManager;
 import net.sf.okapi.common.exceptions.OkapiFileNotFoundException;
 import net.sf.okapi.common.exceptions.OkapiIOException;
 import net.sf.okapi.common.filterwriter.GenericContent;
 import net.sf.okapi.common.filterwriter.IFilterWriter;
 import net.sf.okapi.common.resource.ITextUnit;
 import net.sf.okapi.common.resource.StartDocument;
 import net.sf.okapi.common.resource.StartSubDocument;
 import net.sf.okapi.common.resource.TextContainer;
 import net.sf.okapi.common.skeleton.ISkeletonWriter;
 
 public class VersifiedTextWriter implements IFilterWriter {
 	private OutputStream output;
 	private String outputPath;
 	private OutputStreamWriter writer;
 	private LocaleId language;
 	private String encoding;
 	private String linebreak;
 	private File tempFile;
 	private GenericContent formatter;
 	private int verseCount;
 
 	public VersifiedTextWriter() {
 		verseCount = 0;
 		formatter = new GenericContent();
 	}
 
 	@Override
 	public String getName() {
 		return "VersifiedTextWriter";
 	}
 
 	@Override
 	public void setOptions(LocaleId language, String defaultEncoding) {
 		this.language = language;
 		this.encoding = defaultEncoding;
 	}
 
 	@Override
 	public void setOutput(String path) {
 		close(); // Make sure previous is closed
 		this.outputPath = path;
 	}
 
 	@Override
 	public void setOutput(OutputStream output) {
 		close(); // Make sure previous is closed
 		this.outputPath = null; // If we use the stream, we can't use the path
 		this.output = output; // then assign the new stream
 	}
 
 	@Override
 	public Event handleEvent(Event event) {
 		switch (event.getEventType()) {
 		case START_DOCUMENT:
 			processStartDocument(event);
 			break;
 		case END_DOCUMENT:
 			processEndDocument();
 			break;
 		case START_SUBDOCUMENT:
 			processStartSubDocument(event);
 			break;
 		case TEXT_UNIT:
 			processTextUnit(event);
 			break;
 		default:
 			event = Event.NOOP_EVENT;
 			break;
 		}
 		return event;
 	}
 
 	@Override
 	public void close() {
 		if (writer == null) {
 			return;
 		}
 		IOException err = null;
 		InputStream orig = null;
 		OutputStream dest = null;
 		try {
 			// Close the output
 			writer.close();
 			writer = null;
 			output.close();
 			output = null;
 
 			// If it was in a temporary file, copy it over the existing one
 			// If the IFilter.close() is called before IFilterWriter.close()
 			// this should allow to overwrite the input.
 			if (tempFile != null) {
 				dest = new FileOutputStream(outputPath);
 				orig = new FileInputStream(tempFile);
 				byte[] buffer = new byte[2048];
 				int len;
 				while ((len = orig.read(buffer)) > 0) {
 					dest.write(buffer, 0, len);
 				}
 			}
 		} catch (IOException e) {
 			err = e;
 		} finally {
 			// Make sure we close both files
 			if (dest != null) {
 				try {
 					dest.close();
 				} catch (IOException e) {
 					err = e;
 				}
 				dest = null;
 			}
 			if (orig != null) {
 				try {
 					orig.close();
 				} catch (IOException e) {
 					err = e;
 				}
 				orig = null;
 				if (err != null)
 					throw new RuntimeException(err);
 				else {
 					if (tempFile != null) {
 						tempFile.delete();
 						tempFile = null;
 					}
 				}
 			}
 		}
 	}
 
 	@Override
 	public IParameters getParameters() {
 		return null;
 	}
 
 	@Override
 	public void setParameters(IParameters params) {		
 	}
 
 	@Override
 	public void cancel() {
 		// TODO
 	}
 
 	@Override
 	public EncoderManager getEncoderManager() {
 		// This writer does not use skeleton
 		return null;
 	}
 
 	@Override
 	public ISkeletonWriter getSkeletonWriter() {
 		return null;
 	}
 	
 	private void createWriter(StartDocument startDoc) {
 		linebreak = startDoc.getLineBreak();
 		try {
 			tempFile = null;
 			// If needed, create the output stream from the path provided
 			if (output == null) {
 				boolean useTemp = false;
 				File f = new File(outputPath);
 				if (f.exists()) {
 					// If the file exists, try to remove
 					useTemp = !f.delete();
 				}
 				if (useTemp) {
 					// Use a temporary output if we can overwrite for now
 					// If it's the input file, IFilter.close() will free it before we
 					// call close() here (that is if IFilter.close() is called correctly
 					tempFile = File.createTempFile("vrszwTmp", null);
 					output = new BufferedOutputStream(new FileOutputStream(
 							tempFile.getAbsolutePath()));
 				} else { // Make sure the directory exists
 					Util.createDirectories(outputPath);
 					output = new BufferedOutputStream(new FileOutputStream(outputPath));
 				}
 			}
 
 			// Get the encoding of the original document
 			String originalEnc = startDoc.getEncoding();
 			// If it's undefined, assume it's the default of the system
 			if (originalEnc == null) {
 				originalEnc = Charset.defaultCharset().name();
 			}
 			// Check if the output encoding is defined
 			if (encoding == null) {
 				// if not: Fall back on the encoding of the original
 				encoding = originalEnc;
 			}
 			// Create the output
 			writer = new OutputStreamWriter(output, encoding);
 			// Set default UTF-8 BOM usage
 			boolean useUTF8BOM = false; // On all platforms
 			// Check if the output encoding is UTF-8
 			if ("utf-8".equalsIgnoreCase(encoding)) {
 				// If the original was UTF-8 too
 				if ("utf-8".equalsIgnoreCase(originalEnc)) {
 					// Check whether it had a BOM or not
 					// Linux tools do not like BOM
 					useUTF8BOM = false; // startDoc.hasUTF8BOM();
 				}
 			}
 			// Write out the BOM if needed
 			Util.writeBOMIfNeeded(writer, useUTF8BOM, encoding);
 		} catch (FileNotFoundException e) {
 			throw new OkapiFileNotFoundException(e);
 		} catch (IOException e) {
 			throw new OkapiIOException(e);
 		}
 	}
 
 	private void processStartDocument(Event event) {
 		try {
 			verseCount = 0;
 			StartDocument sd = event.getStartDocument();
 			// Create the output
 			createWriter(sd);
 
 			// Writer header
 			writer.write("|b" + sd.getName() + linebreak);
 		} catch (IOException e) {
 			throw new OkapiIOException("Error writing the versified text file header.", e);
 		}
 	}
 
 	private void processEndDocument() {
 		close();
 	}
 
 	private void processStartSubDocument(Event event) {
 		try {
 			verseCount = 0;
 			StartSubDocument ssd = (StartSubDocument) event.getResource();
 			writer.write("|c" + ssd.getName() + linebreak);
 		} catch (IOException e) {
 			throw new OkapiIOException("Error writing subdocument (|cName).", e);
 		}
 	}
 
 	private void processTextUnit(Event event) {
 		try {
 			ITextUnit tu = event.getTextUnit();
 			if (tu.isEmpty()) {
 				return; // Do not write out entries with empty source
 			}
 
 			// |v
 			// the numeric verse id is not unique but we inherit the 
 			// |c chapter name which should be unique per document
 			// here we split out the verse id from the chapter id
			String verseId = tu.getId().split(":")[1];
 			writer.write("|v" + verseId + linebreak);
 
 			// source
 			boolean lastCharWasNewline = writeTextContainer(tu.getSource());
 			// <TARGET> tag
 			String targetTag = (lastCharWasNewline ? "" : linebreak) + "<TARGET>" + linebreak; 
 			writer.write(targetTag);
 			writeTextContainer(tu.getTarget(language));
 		} catch (IOException e) {
 			throw new OkapiIOException("Error writing a versified text unit.", e);
 		}
 	}
 
 	private boolean writeTextContainer(TextContainer tc) {
 		try {
 			if (tc == null) {
 				writer.write("\n");
 				return true;
 			}
 
 			String tmp;
 			tmp = formatter.printSegmentedContent(tc, false, false);
 			writer.write(tmp); // No wrapping needed
 			if (tmp.endsWith("\n")) {
 				return true;
 			}
 			return false;
 		} catch (IOException e) {
 			throw new OkapiIOException("Error writing TextContainer.", e);
 		}
 	}
 }
