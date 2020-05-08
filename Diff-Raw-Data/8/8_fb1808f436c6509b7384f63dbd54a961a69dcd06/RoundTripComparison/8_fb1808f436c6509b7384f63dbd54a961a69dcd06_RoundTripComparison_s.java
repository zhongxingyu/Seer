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
 
 package net.sf.okapi.common.filters;
 
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.UnsupportedEncodingException;
 import java.util.ArrayList;
 import java.util.List;
 
 import net.sf.okapi.common.Event;
 import net.sf.okapi.common.IParameters;
 import net.sf.okapi.common.Util;
 import net.sf.okapi.common.filters.IFilter;
 import net.sf.okapi.common.filterwriter.IFilterWriter;
 import net.sf.okapi.common.LocaleId;
 import net.sf.okapi.common.resource.RawDocument;
 
 public class RoundTripComparison {
 
 	private IFilter filter;
 	private ArrayList<Event> extraction1Events;
 	private ArrayList<Event> extraction2Events;
 	private IFilterWriter writer;
 	private ByteArrayOutputStream writerBuffer;
 	private String defaultEncoding;
 	private LocaleId srcLoc;
 	private LocaleId trgLoc;
 
 	public RoundTripComparison() {
 		extraction1Events = new ArrayList<Event>();
 		extraction2Events = new ArrayList<Event>();
 	}
 
 	public boolean executeCompare(IFilter filter, List<InputDocument> inputDocs,
 			String defaultEncoding, LocaleId srcLoc, LocaleId trgLoc) {
 		String path = null;
 
 		this.filter = filter;
 		this.defaultEncoding = defaultEncoding;
 		this.srcLoc = srcLoc;
 		this.trgLoc = trgLoc;
 
 		// Create the filter-writer for the provided filter
 		writer = filter.createFilterWriter();
 
 		for (InputDocument doc : inputDocs) {
 			path = doc.path;
 			// DEBUG: System.out.println(doc.path);
 			// Reset the event lists
 			extraction1Events.clear();
 			extraction2Events.clear();
 			// Load parameters if needed
 			if (doc.paramFile != null && !doc.paramFile.equals("")) {
 				String root = Util.getDirectoryName(doc.path);
 				IParameters params = filter.getParameters();
 				if (params != null) {
 					params.load(Util.toURI(root + File.separator + doc.paramFile), false);
 				}
 			}
 			// Execute the first extraction and the re-writing
 			executeFirstExtraction(doc);
 			// Execute the second extraction from the output of the first
 			executeSecondExtraction();
 			// Compare the events
 			if (!FilterTestDriver.compareEvents(extraction1Events, extraction2Events)) {
 				throw new RuntimeException("Events are different for " + doc.path);
 			}
 		}
 		return true;
 	}
 
 	public boolean executeCompare(IFilter filter, List<InputDocument> inputDocs,
 			String defaultEncoding, LocaleId srcLoc, LocaleId trgLoc, String outputDir) {
 
 		this.filter = filter;
 		this.defaultEncoding = defaultEncoding;
 		this.srcLoc = srcLoc;
 		this.trgLoc = trgLoc;
 
 		// Create the filter-writer for the provided filter
 		writer = filter.createFilterWriter();
 
 		for (InputDocument doc : inputDocs) {
 			// Reset the event lists
 			extraction1Events.clear();
 			extraction2Events.clear();
 			// Load parameters if needed
 			if (doc.paramFile == null) {
 				IParameters params = filter.getParameters();
 				if (params != null)
 					params.reset();
 			} else {
 				String root = Util.getDirectoryName(doc.path);
 				IParameters params = filter.getParameters();
 				if (params != null)
 					params.load(Util.toURI(root + File.separator + doc.paramFile), false);
 			}
 			// Execute the first extraction and the re-writing
 			String outPath = executeFirstExtractionToFile(doc, outputDir);
 			// Execute the second extraction from the output of the first
 			executeSecondExtractionFromFile(outPath);
 			// Compare the events
 			if (!FilterTestDriver.compareEvents(extraction1Events, extraction2Events)) {
 				throw new RuntimeException("Events are different for " + doc.path);
 			}
 		}
 		return true;
 	}
 
 	private void executeFirstExtraction(InputDocument doc) {
 		try {
 			// Open the input
 			filter.open(new RawDocument(Util.toURI(doc.path), defaultEncoding, srcLoc,
 					trgLoc));
 
 			// Prepare the output
 			writer.setOptions(trgLoc, "UTF-16");
 			writerBuffer = new ByteArrayOutputStream();
 			writer.setOutput(writerBuffer);
 
 			// Process the document
 			Event event;
 			while (filter.hasNext()) {
 				event = filter.next();
 				switch (event.getEventType()) {
 				case START_DOCUMENT:
 				case END_DOCUMENT:
 				case START_SUBDOCUMENT:
 				case END_SUBDOCUMENT:
 					break;
 				case START_GROUP:
 				case END_GROUP:
 				case TEXT_UNIT:
 					extraction1Events.add(event);
 					break;
 				}
 				writer.handleEvent(event);
 		}
 		} finally {
 			if (filter != null)
 				filter.close();
 			if (writer != null)
 				writer.close();
 		}
 	}
 
 	private void executeSecondExtraction() {
 		try {
 			// Set the input (from the output of first extraction)
 			String input;
 			try {
 				input = new String(writerBuffer.toByteArray(), "UTF-16");
 			} catch (UnsupportedEncodingException e) {
 				throw new RuntimeException(e);
 			}
 			filter.open(new RawDocument(input, srcLoc, trgLoc));
 
 			// Process the document
 			Event event;
 			while (filter.hasNext()) {
 				event = filter.next();
 				switch (event.getEventType()) {
 				case START_DOCUMENT:
 				case END_DOCUMENT:
 				case START_SUBDOCUMENT:
 				case END_SUBDOCUMENT:
 					break;
 				case START_GROUP:
 				case END_GROUP:
 				case TEXT_UNIT:
 					extraction2Events.add(event);
 					break;
 				}
 			}
 		} finally {
 			if (filter != null)
 				filter.close();
 		}
 	}
 
 	private String executeFirstExtractionToFile(InputDocument doc, String outputDir) {
 		String outPath = null;
 		try {
 			// Open the input
 			filter.open(new RawDocument(Util.toURI(doc.path), defaultEncoding, srcLoc,
 					trgLoc));
 
 			// Prepare the output
 			writer.setOptions(trgLoc, "UTF-8");
 			outPath = Util.getDirectoryName(doc.path);
			outPath += (File.separator + outputDir + File.separator + Util.getFilename(doc.path,
					true));
 			writer.setOutput(outPath);
 
 			// Process the document
 			Event event;
 			while (filter.hasNext()) {
 				event = filter.next();
 				switch (event.getEventType()) {
 				case START_DOCUMENT:
 				case END_DOCUMENT:
 				case START_SUBDOCUMENT:
 				case END_SUBDOCUMENT:
 					break;
 				case START_GROUP:
 				case END_GROUP:
 				case TEXT_UNIT:
 					extraction1Events.add(event);
 					break;
 				}
 				writer.handleEvent(event);
 			}
 		} finally {
 			if (filter != null)
 				filter.close();
 			if (writer != null)
 				writer.close();
 		}
 		return outPath;
 	}
 
 	private void executeSecondExtractionFromFile(String input) {
 		try {
 			// Set the input (from the output of first extraction)
 			filter.open(new RawDocument(Util.toURI(input), "UTF-8", srcLoc, trgLoc));
 
 			// Process the document
 			Event event;
 			while (filter.hasNext()) {
 				event = filter.next();
 				switch (event.getEventType()) {
 				case START_DOCUMENT:
 				case END_DOCUMENT:
 				case START_SUBDOCUMENT:
 				case END_SUBDOCUMENT:
 					break;
 				case START_GROUP:
 				case END_GROUP:
 				case TEXT_UNIT:
 					extraction2Events.add(event);
 					break;
 				}
 			}
 		} finally {
 			if (filter != null)
 				filter.close();
 		}
 	}
 }
