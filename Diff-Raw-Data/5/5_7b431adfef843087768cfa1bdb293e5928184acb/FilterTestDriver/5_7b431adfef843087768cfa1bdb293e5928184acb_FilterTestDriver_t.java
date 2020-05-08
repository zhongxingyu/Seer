 /*===========================================================================
   Copyright (C) 2009 by the Okapi Framework contributors
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
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertTrue;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 
 import net.sf.okapi.common.Event;
 import net.sf.okapi.common.EventType;
 import net.sf.okapi.common.IParameters;
 import net.sf.okapi.common.IResource;
 import net.sf.okapi.common.ISkeleton;
 import net.sf.okapi.common.Util;
 import net.sf.okapi.common.encoder.EncoderManager;
 import net.sf.okapi.common.filters.IFilter;
 import net.sf.okapi.common.LocaleId;
 import net.sf.okapi.common.resource.Code;
 import net.sf.okapi.common.resource.DocumentPart;
 import net.sf.okapi.common.resource.Ending;
 import net.sf.okapi.common.resource.INameable;
 import net.sf.okapi.common.resource.Property;
 import net.sf.okapi.common.resource.RawDocument;
 import net.sf.okapi.common.resource.Segment;
 import net.sf.okapi.common.resource.StartDocument;
 import net.sf.okapi.common.resource.StartGroup;
 import net.sf.okapi.common.resource.StartSubDocument;
 import net.sf.okapi.common.resource.TextContainer;
 import net.sf.okapi.common.resource.TextFragment;
 import net.sf.okapi.common.resource.TextUnit;
 import net.sf.okapi.common.resource.TextFragment.TagType;
 import net.sf.okapi.common.skeleton.GenericSkeletonWriter;
 import net.sf.okapi.common.skeleton.ISkeletonWriter;
 
 /**
  * Driver to test filter output.
  */
 public class FilterTestDriver {
 
 	private boolean showSkeleton = true;
 	private int displayLevel = 0;
 	private int warnings;
 	private boolean ok;
 
 	static public boolean laxCompareEvent(Event manual, Event generated) {
 		if (generated.getEventType() != manual.getEventType()) {
 			return false;
 		}
 		IResource mr = manual.getResource();
 		IResource gr = generated.getResource();
 
 		if (mr != null && gr != null && mr.getSkeleton() != null && gr.getSkeleton() != null) {
 			if (!(mr.getSkeleton().toString().equals(gr.getSkeleton().toString()))) {
 				return false;
 			}
 		}
 
 		switch (generated.getEventType()) {
 		case DOCUMENT_PART:
 			DocumentPart mdp = (DocumentPart) mr;
 			DocumentPart gdp = (DocumentPart) gr;
 			if (mdp.isReferent() != gdp.isReferent()) {
 				return false;
 			}
 			if (mdp.isTranslatable() != gdp.isTranslatable()) {
 				return false;
 			}
 			if ( !(mdp.getPropertyNames().equals(gdp.getPropertyNames())) ) {
 				return false;
 			}
 			for ( String propName : gdp.getPropertyNames() ) {
 				Property gdpProp = gdp.getProperty(propName);
 				Property mdpProp = mdp.getProperty(propName);
 				if ( gdpProp.isReadOnly() != mdpProp.isReadOnly() ) {
 					return false;
 				}
 				if ( !gdpProp.getValue().equals(mdpProp.getValue()) ) {
 					return false;
 				}
 			}
 			
 			if ( !(mdp.getSourcePropertyNames().equals(gdp.getSourcePropertyNames())) ) {
 				return false;
 			}
 			for ( String propName : gdp.getSourcePropertyNames() ) {
 				Property gdpProp = gdp.getSourceProperty(propName);
 				Property mdpProp = mdp.getSourceProperty(propName);
 				if ( gdpProp.isReadOnly() != mdpProp.isReadOnly() ) {
 					return false;
 				}
 				if ( !gdpProp.getValue().equals(mdpProp.getValue()) ) {
 					return false;
 				}
 			}
 			break;
 			
 		case TEXT_UNIT:
 			TextUnit mtu = (TextUnit) mr;
 			TextUnit gtu = (TextUnit) gr;
 
 			// Resource-level properties
 			if ( !(mtu.getPropertyNames().equals(gtu.getPropertyNames())) ) {
 				return false;
 			}
 			for ( String propName : gtu.getPropertyNames() ) {
 				Property gtuProp = gtu.getProperty(propName);
 				Property mtuProp = mtu.getProperty(propName);
 				if ( gtuProp.isReadOnly() != mtuProp.isReadOnly() ) {
 					return false;
 				}
 				if ( !gtuProp.getValue().equals(mtuProp.getValue()) ) {
 					return false;
 				}
 			}
 			
 			// Source properties
 			if ( !(mtu.getSourcePropertyNames().equals(gtu.getSourcePropertyNames())) ) {
 				return false;
 			}
 			for ( String propName : gtu.getSourcePropertyNames() ) {
 				Property gtuProp = gtu.getSourceProperty(propName);
 				Property mtuProp = mtu.getSourceProperty(propName);
 				if ( gtuProp.isReadOnly() != mtuProp.isReadOnly() ) {
 					return false;
 				}
 				if ( !gtuProp.getValue().equals(mtuProp.getValue()) ) {
 					return false;
 				}
 			}
 
 			String tmp = mtu.getName();
 			if ( tmp == null ) {
 				if ( gtu.getName() != null ) {
 					return false;
 				}
 			}
			else if ( !tmp.equals(gtu.getName()) ) {
 				return false;
 			}
 			
 			tmp = mtu.getType();
 			if ( tmp == null ) {
 				if ( gtu.getType() != null ) {
 					return false;
 				}
 			}
			else if ( !tmp.equals(gtu.getType()) ) {
 				return false;
 			}
 			
 			if (mtu.isTranslatable() != gtu.isTranslatable()) {
 				return false;
 			}
 			if ( mtu.isReferent() != gtu.isReferent() ) {
 				return false;
 			}
 			if ( mtu.preserveWhitespaces() != gtu.preserveWhitespaces() ) {
 				return false;
 			}
 			if ( !(mtu.toString().equals(gtu.toString())) ) {
 				return false;
 			}
 			
 			if ( mtu.getSource().getCodes().size() != gtu.getSource().getCodes().size() ) {
 				return false;
 			}
 			int i = -1;
 			for (Code c : mtu.getSource().getCodes()) {
 				i++;
 				if (c.getType() != null) {
 					if (!c.getType().equals(gtu.getSource().getCode(i).getType())) {
 						return false;
 					}
 				}
 			}
 			break;
 		}
 
 		return true;
 	}
 
 	static public boolean laxCompareEvents(ArrayList<Event> manual, ArrayList<Event> generated) {
 		if (manual.size() != generated.size()) {
 			return false;
 		}
 
 		Iterator<Event> manualIt = manual.iterator();
 		for (Event ge : generated) {
 			Event me = manualIt.next();
 			if (!laxCompareEvent(me, ge)) {
 				return false;
 			}
 		}
 		return true;
 	}
 
 	/**
 	 * Compares the codes of two text fragments so in-lines codes that have the same
 	 * IDs have also the same content (getData()), except for opening/closing cases. 
 	 * @param tf1 the base fragment.
 	 * @param tf2 the fragment to compare with the base fragment.
 	 */
 	static public void checkCodeData (TextFragment tf1,
 		TextFragment tf2)
 	{
 		List<Code> srcCodes = tf1.getCodes();
 		List<Code> trgCodes = tf2.getCodes();
 		for ( Code srcCode : srcCodes ) {
 			for ( Code trgCode : trgCodes ) {
 				// Same ID must have the same content, except for open/close
 				if ( srcCode.getId() == trgCode.getId() ) {
 					switch ( srcCode.getTagType() ) {
 					case OPENING:
 						if ( trgCode.getTagType() == TagType.CLOSING ) break;
 						assertEquals(srcCode.getData(), trgCode.getData());
 						break;
 					case CLOSING:
 						if ( trgCode.getTagType() == TagType.OPENING ) break;
 						assertEquals(srcCode.getData(), trgCode.getData());
 						break;
 					default:
 						assertEquals(srcCode.getData(), trgCode.getData());
 						break;
 					}
 				}
 			}
 		}
 	}
 		
 
 	static public boolean compareEvent(Event manual,
 		Event generated)
 	{
 		if ( generated.getEventType() != manual.getEventType() ) {
 			System.err.println("Event type difference: "
 				+ generated.getEventType().toString()
 				+ " and "
 				+ manual.getEventType().toString());
 			return false;
 		}
 		
 		switch (generated.getEventType()) {
 		case DOCUMENT_PART:
 			DocumentPart mdp = (DocumentPart)manual.getResource();
 			DocumentPart gdp = (DocumentPart)generated.getResource();
 			if ( !compareIResource(mdp, gdp) ) {
 				return false;
 			}
 			if ( !compareINameable(mdp, gdp) ) {
 				return false;
 			}
 			if (mdp.isReferent() != gdp.isReferent()) {
 				return false;
 			}
 			break;
 			
 		case START_GROUP:
 			StartGroup sg1 = (StartGroup)manual.getResource();
 			StartGroup sg2 = (StartGroup)generated.getResource();
 			if ( !compareIResource(sg1, sg2) ) {
 				return false;
 			}
 			if ( !compareINameable(sg1, sg2) ) {
 				return false;
 			}
 			if (sg1.isReferent() != sg2.isReferent()) {
 				return false;
 			}
 			break;
 
 		case END_GROUP:
 			if ( !compareIResource(manual.getResource(), generated.getResource()) ) {
 				return false;
 			}
 			break;
 			
 			
 		case TEXT_UNIT:
 			if ( !compareTextUnit((TextUnit)manual.getResource(), (TextUnit)generated.getResource()) ) {
 				System.err.println("Text unit difference");
 				return false;
 			}
 			break;
 		}
 
 		return true;
 	}
 
 	static public boolean compareEvents(ArrayList<Event> list1,
 		ArrayList<Event> list2)
 	{
 		int i = 0;
 		Event event1, event2;
 		while ( i<list1.size() ) {
 			event1 = list1.get(i);
 			if ( i >= list2.size() ) {
 				System.err.println("Less events in second list");
 				return false;
 			}
 			event2 = list2.get(i);
 			if ( !compareEvent(event1, event2) ) {
 				return false;
 			}
 			i++;
 		}
 		
 		if ( list1.size() != list2.size() ) {
 			System.err.println("Less events in first list");
 			return false;
 		}
 
 		return true;
 	}
 	
 	static public boolean compareEventTypesOnly(ArrayList<Event> manual, ArrayList<Event> generated) {
 		if (manual.size() != generated.size()) {
 			return false;
 		}
 
 		Iterator<Event> manualIt = manual.iterator();
 		for (Event ge : generated) {
 			Event me = manualIt.next();
 			if (ge.getEventType() != me.getEventType()) {
 				return false;
 			}
 		}
 		return true;
 	}
 
 	/**
 	 * Indicates to this driver to display the skeleton data.
 	 * 
 	 * @param value
 	 *            True to display the skeleton, false to not display the
 	 *            skeleton.
 	 */
 	public void setShowSkeleton(boolean value) {
 		showSkeleton = value;
 	}
 
 	/**
 	 * Indicates what to display.
 	 * 
 	 * @param value
 	 *            0=display nothing, 1=display TU only, >1=display all.
 	 */
 	public void setDisplayLevel(int value) {
 		displayLevel = value;
 
 	}
 
 	/**
 	 * Process the input document. You must have called the setOptions() and
 	 * open() methods of the filter before calling this method.
 	 * 
 	 * @param filter
 	 *            Filter to process.
 	 * @return False if an error occurred, true if all was OK.
 	 */
 	public boolean process(IFilter filter) {
 		ok = true;
 		warnings = 0;
 		int startDoc = 0;
 		int endDoc = 0;
 		int startGroup = 0;
 		int endGroup = 0;
 		int startSubDoc = 0;
 		int endSubDoc = 0;
 
 		Event event;
 		while (filter.hasNext()) {
 			event = filter.next();
 			switch (event.getEventType()) {
 			case START_DOCUMENT:
 				startDoc++;
 				checkStartDocument((StartDocument) event.getResource());
 				if (displayLevel < 2)
 					break;
 				System.out.println("---Start Document");
 				printSkeleton(event.getResource());
 				break;
 			case END_DOCUMENT:
 				endDoc++;
 				if (displayLevel < 2)
 					break;
 				System.out.println("---End Document");
 				printSkeleton(event.getResource());
 				break;
 			case START_SUBDOCUMENT:
 				startSubDoc++;
 				if (displayLevel < 2)
 					break;
 				System.out.println("---Start Sub Document");
 				printSkeleton(event.getResource());
 				break;
 			case END_SUBDOCUMENT:
 				endSubDoc++;
 				if (displayLevel < 2)
 					break;
 				System.out.println("---End Sub Document");
 				printSkeleton(event.getResource());
 				break;
 			case START_GROUP:
 				startGroup++;
 				if (displayLevel < 2)
 					break;
 				System.out.println("---Start Group");
 				printSkeleton(event.getResource());
 				break;
 			case END_GROUP:
 				endGroup++;
 				if (displayLevel < 2)
 					break;
 				System.out.println("---End Group");
 				printSkeleton(event.getResource());
 				break;
 			case TEXT_UNIT:
 				TextUnit tu = (TextUnit) event.getResource();
 				if (displayLevel < 1)
 					break;
 				printTU(tu);
 				if (displayLevel < 2)
 					break;
 				printResource(tu);
 				printSkeleton(tu);
 				break;
 			case DOCUMENT_PART:
 				if (displayLevel < 2)
 					break;
 				System.out.println("---Document Part");
 				printResource((INameable) event.getResource());
 				printSkeleton(event.getResource());
 				break;
 			}
 		}
 
 		if (startDoc != 1) {
 			System.err.println(String.format("ERROR: START_DOCUMENT = %d", startDoc));
 			ok = false;
 		}
 		if (endDoc != 1) {
 			System.err.println(String.format("ERROR: END_DOCUMENT = %d", endDoc));
 			ok = false;
 		}
 		if (startSubDoc != endSubDoc) {
 			System.err
 					.println(String.format("ERROR: START_SUBDOCUMENT=%d, END_SUBDOCUMENT=%d", startSubDoc, endSubDoc));
 			ok = false;
 		}
 		if (startGroup != endGroup) {
 			System.out.println(String.format("ERROR: START_GROUP=%d, END_GROUP=%d", startGroup, endGroup));
 			ok = false;
 		}
 		return ok;
 	}
 
 	private void printTU(TextUnit tu) {
 		System.out.println("---Text Unit");
 		System.out.println("S=[" + tu.toString() + "]");
 		for (LocaleId lang : tu.getTargetLocales()) {
 			System.out.println("T(" + lang + ")=[" + tu.getTarget(lang).toString() + "]");
 		}
 	}
 
 	private void printResource(INameable res) {
 		if (res == null) {
 			System.out.println("NULL resource.");
 			ok = false;
 		}
 		System.out.print("  id='" + res.getId() + "'");
 		System.out.print(" name='" + res.getName() + "'");
 		System.out.print(" type='" + res.getType() + "'");
 		System.out.println(" mimeType='" + res.getMimeType() + "'");
 	}
 
 	private void printSkeleton(IResource res) {
 		if (!showSkeleton)
 			return;
 		ISkeleton skel = res.getSkeleton();
 		if (skel != null) {
 			System.out.println("---");
 			System.out.println(skel.toString());
 			System.out.println("---");
 		}
 	}
 
 	private void checkStartDocument(StartDocument startDoc) {
 		if ( displayLevel < 1 ) return; 
 		String tmp = startDoc.getEncoding();
 		if ((tmp == null) || (tmp.length() == 0)) {
 			System.err.println("WARNING: No encoding specified in StartDocument.");
 			warnings++;
 		} else if (displayLevel > 1)
 			System.out.println("StartDocument encoding = " + tmp);
 
 		LocaleId locId = startDoc.getLocale();
 		if ( Util.isNullOrEmpty(locId) ) {
 			System.err.println("WARNING: No language specified in StartDocument.");
 			warnings++;
 		} else if (displayLevel > 1)
 			System.out.println("StartDocument language = " + locId.toString());
 
 		tmp = startDoc.getName();
 		if ((tmp == null) || (tmp.length() == 0)) {
 			System.err.println("WARNING: No name specified in StartDocument.");
 			warnings++;
 		} else if (displayLevel > 1)
 			System.out.println("StartDocument name = " + tmp);
 
 		if (displayLevel < 2)
 			return;
 		System.err.println("StartDocument MIME type = " + startDoc.getMimeType());
 		System.err.println("StartDocument Type = " + startDoc.getType());
 	}
 
 	/**
 	 * Creates a string output from a list of events.
 	 * 
 	 * @param list
 	 *            The list of events.
 	 * @param trgLang
 	 *            Code of the target (output) language.
 	 * @return The generated output string
 	 */
 	public static String generateOutput (ArrayList<Event> list,
 		LocaleId trgLang)
 	{
 		GenericSkeletonWriter writer = new GenericSkeletonWriter();
 		StringBuilder tmp = new StringBuilder();
 		for (Event event : list) {
 			switch (event.getEventType()) {
 			case START_DOCUMENT:
 				tmp.append(writer.processStartDocument(trgLang, "UTF-8", null, new EncoderManager(),
 					(StartDocument) event.getResource()));
 				break;
 			case END_DOCUMENT:
 				tmp.append(writer.processEndDocument((Ending)event.getResource()));
 				break;
 			case START_SUBDOCUMENT:
 				tmp.append(writer.processStartSubDocument((StartSubDocument)event.getResource()));
 				break;
 			case END_SUBDOCUMENT:
 				tmp.append(writer.processEndSubDocument((Ending)event.getResource()));
 				break;
 			case TEXT_UNIT:
 				TextUnit tu = (TextUnit)event.getResource();
 				tmp.append(writer.processTextUnit(tu));
 				break;
 			case DOCUMENT_PART:
 				DocumentPart dp = (DocumentPart)event.getResource();
 				tmp.append(writer.processDocumentPart(dp));
 				break;
 			case START_GROUP:
 				StartGroup startGroup = (StartGroup)event.getResource();
 				tmp.append(writer.processStartGroup(startGroup));
 				break;
 			case END_GROUP:
 				tmp.append(writer.processEndGroup((Ending) event.getResource()));
 				break;
 			}
 		}
 		writer.close();
 		return tmp.toString();
 	}
 
 	/**
 	 * Creates a string output from a list of events, using a given ISkeletonWriter.
 	 * @param list the list of events.
 	 * @param trgLang code of the target (output) language.
 	 * @param skelWriter the ISkeletonWriter to use. 
 	 * @return The generated output string.
 	 */
 	public static String generateOutput (ArrayList<Event> list,
 		LocaleId trgLang,
 		ISkeletonWriter skelWriter)
 	{
 		StringBuilder tmp = new StringBuilder();
 		for (Event event : list) {
 			switch (event.getEventType()) {
 			case START_DOCUMENT:
 				tmp.append(skelWriter.processStartDocument(trgLang, "UTF-8", null, new EncoderManager(),
 					(StartDocument) event.getResource()));
 				break;
 			case END_DOCUMENT:
 				tmp.append(skelWriter.processEndDocument((Ending)event.getResource()));
 				break;
 			case START_SUBDOCUMENT:
 				tmp.append(skelWriter.processStartSubDocument((StartSubDocument)event.getResource()));
 				break;
 			case END_SUBDOCUMENT:
 				tmp.append(skelWriter.processEndSubDocument((Ending)event.getResource()));
 				break;
 			case TEXT_UNIT:
 				TextUnit tu = (TextUnit)event.getResource();
 				tmp.append(skelWriter.processTextUnit(tu));
 				break;
 			case DOCUMENT_PART:
 				DocumentPart dp = (DocumentPart)event.getResource();
 				tmp.append(skelWriter.processDocumentPart(dp));
 				break;
 			case START_GROUP:
 				StartGroup startGroup = (StartGroup)event.getResource();
 				tmp.append(skelWriter.processStartGroup(startGroup));
 				break;
 			case END_GROUP:
 				tmp.append(skelWriter.processEndGroup((Ending) event.getResource()));
 				break;
 			}
 		}
 		skelWriter.close();
 		return tmp.toString();
 	}
 
 	public static TextUnit getTextUnit (IFilter filter,
 		InputDocument doc,
 		String defaultEncoding,
 		LocaleId srcLang,
 		LocaleId trgLang,
 		int tuNumber)
 	{
 		try {
 			// Load parameters if needed
 			if  (doc.paramFile == null  || doc.paramFile == "")  {
 				IParameters params = filter.getParameters();
 				if ( params != null ) params.reset();
 			}
 			else {
 				String root = Util.getDirectoryName(doc.path);
 				IParameters params = filter.getParameters();
 				if ( params != null ) params.load(Util.toURI(root+"/"+doc.paramFile), false);
 			}
 			
 			// Open the input
 			int num = 0;
 			filter.open(new RawDocument((new File(doc.path)).toURI(), defaultEncoding, srcLang, trgLang));
 			// Process the document
 			Event event;
 			while ( filter.hasNext() ) {
 				event = filter.next();
 				switch ( event.getEventType() ) {
 				case TEXT_UNIT:
 					if ( ++num == tuNumber ) {
 						return (TextUnit)event.getResource();
 					}
 					break;
 				}
 			}
 		}
 		catch ( Throwable e ) {
 			System.err.println(e.getMessage());
 		}
 		finally {
 			if ( filter != null ) filter.close();
 		}
 		return null;
 	}
 
 	public static boolean testStartDocument (IFilter filter,
 		InputDocument doc,
 		String defaultEncoding,
 		LocaleId srcLang,
 		LocaleId trgLang)
 	{
 		try {
 			// Load parameters if needed
 			if  (doc.paramFile == null  || doc.paramFile == "")  {
 				IParameters params = filter.getParameters();
 				if ( params != null ) params.reset();
 			}
 			else {
 				String root = Util.getDirectoryName(doc.path);
 				IParameters params = filter.getParameters();
 				if ( params != null ) params.load(Util.toURI(root+File.separator+doc.paramFile), false);
 			}
 			
 			// Open the input
 			filter.open(new RawDocument((new File(doc.path)).toURI(), defaultEncoding, srcLang, trgLang));
 			// Process the document
 			Event event;
 			while ( filter.hasNext() ) {
 				event = filter.next();
 				assertTrue("First event is not a StartDocument event.", event.getEventType()==EventType.START_DOCUMENT);
 				StartDocument sd = (StartDocument)event.getResource();
 				assertNotNull("No StartDocument", sd);
 				assertNotNull("Name is null", sd.getName());
 				assertNotNull("Encoding is null", sd.getEncoding());
 				assertNotNull("ID is null", sd.getId());
 				assertNotNull("Language is null", sd.getLocale());
 				assertNotNull("Linebreak is null", sd.getLineBreak());
 				assertNotNull("Filter Parameters is null", sd.getFilterParameters());
 				assertNotNull("FilterWriter is null", sd.getFilterWriter());
 				assertNotNull("Mime type is null", sd.getMimeType());
 				return true;
 			}
 		}
 		catch ( Throwable e ) {
 			System.err.println(e.getMessage());
 		}
 		finally {
 			if ( filter != null ) filter.close();
 		}
 		return false;
 	}
 
 	/**
 	 * Gets the Nth text unit found in the given list of events.
 	 * 
 	 * @param list
 	 *            The list of events
 	 * @param tuNumber
 	 *            The number of the unit to return: 1 for the first one, 2 for
 	 *            the second, etc.
 	 * @return The text unit found, or null.
 	 */
 	public static TextUnit getTextUnit(ArrayList<Event> list, int tuNumber) {
 		int n = 0;
 		for (Event event : list) {
 			if (event.getEventType() == EventType.TEXT_UNIT) {
 				if (++n == tuNumber) {
 					return (TextUnit) event.getResource();
 				}
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * Gets the Nth group found in the given list of events.
 	 * 
 	 * @param list
 	 *            The list of events
 	 * @param tuNumber
 	 *            The number of the group to return: 1 for the first one, 2 for
 	 *            the second, etc.
 	 * @return The group found, or null.
 	 */
 	public static StartGroup getGroup(ArrayList<Event> list, int tuNumber) {
 		int n = 0;
 		for (Event event : list) {
 			if (event.getEventType() == EventType.START_GROUP) {
 				if (++n == tuNumber) {
 					return (StartGroup) event.getResource();
 				}
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * Gets the start document in the given list of events.
 	 * 
 	 * @param list
 	 *            The list of events
 	 * @return The start document found, or null.
 	 */
 	public static StartDocument getStartDocument(ArrayList<Event> list) {
 		for (Event event : list) {
 			if (event.getEventType() == EventType.START_DOCUMENT) {
 				return (StartDocument) event.getResource();
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * Gets the Nth document part found in the given list of events.
 	 * 
 	 * @param list
 	 *            The list of events
 	 * @param tuNumber
 	 *            The number of the document part to return: 1 for the first one, 2 for
 	 *            the second, etc.
 	 * @return The document part found, or null.
 	 */
 	public static DocumentPart getDocumentPart(ArrayList<Event> list, int dpNumber) {
 		int n = 0;
 		for (Event event : list) {
 			if (event.getEventType() == EventType.DOCUMENT_PART) {
 				if (++n == dpNumber) {
 					return (DocumentPart)event.getResource();
 				}
 			}
 		}
 		return null;
 	}
 
 	public static boolean compareTextUnit (TextUnit tu1,
 		TextUnit tu2)
 	{
 		if ( !compareINameable(tu1, tu2) ) {
 			System.err.println("Difference in INameable");
 			return false;
 		}
 		if ( tu1.isReferent() != tu2.isReferent() ) {
 			System.err.println("isReferent difference");
 			return false;
 		}
 		// TextUnit tests
 		if ( tu1.preserveWhitespaces() != tu2.preserveWhitespaces() ) {
 			System.err.println("preserveWhitespaces difference");
 			return false;
 		}
 		if ( !compareTextContainer(tu1.getSource(), tu2.getSource()) ) {
 			System.err.println("TextContainer difference");
 			return false;
 		}
 		//TODO: target, but we have to take re-writing of source as target in account
 		return true;
 	}
 	
 	public static boolean compareIResource (IResource item1,
 		IResource item2)
 	{
 		if ( item1 == null ) {
 			return (item2 == null);
 		}
 		if ( item2 == null ) return false;
 		
 		// ID
 		String tmp1 = item1.getId();
 		String tmp2 = item2.getId();
 		if ( tmp1 == null ) {
 			if ( tmp2 != null ) return false;
 		}
 		else {
 			if ( tmp2 == null ) return false;
 			if ( !tmp1.equals(tmp2) ) return false;
 		}
 
 		// Skeleton
 		ISkeleton skl1 = item1.getSkeleton();
 		ISkeleton skl2 = item2.getSkeleton();
 		if ( skl1 == null ) {
 			if ( skl2 != null ) return false;
 		}
 		else {
 			if ( skl2 == null ) return false;
 			tmp1 = skl1.toString();
 			tmp2 = skl2.toString();
 			if ( tmp1 == null ) {
 				if ( tmp2 != null ) return false;
 			}
 			else {
 				if ( tmp2 == null ) return false;
 				if ( !tmp1.equals(tmp2) ) {
 					System.err.println("Skeleton differences: 1='"+tmp1+"'\n2='"+tmp2+"'");
 					return false;
 				}
 			}
 		}
 
 		return true;
 	}
 	
 	public static boolean compareINameable (INameable item1,
 		INameable item2)
 	{
 		if ( item1 == null ) return (item2 == null);
 		if ( item2 == null ) return false;
 		
 		// Resource-level properties
 		Set<String> names1 = item1.getPropertyNames();
 		Set<String> names2 = item2.getPropertyNames();
 		if ( names1.size() != names2.size() ) {
 			System.err.println("Number of resource-level properties difference");
 			return false;
 		}
 		for ( String name : item1.getPropertyNames() ) {
 			Property p1 = item1.getProperty(name);
 			Property p2 = item2.getProperty(name);
 			if ( !compareProperty(p1, p2) ) {
 				return false;
 			}
 		}
 		
 		// Source properties
 		names1 = item1.getSourcePropertyNames();
 		names2 = item2.getSourcePropertyNames();
 		if ( names1.size() != names2.size() ) {
 			System.err.println("Number of source properties difference");
 			return false;
 		}
 		for ( String name : item1.getSourcePropertyNames() ) {
 			Property p1 = item1.getSourceProperty(name);
 			Property p2 = item2.getSourceProperty(name);
 			if ( !compareProperty(p1, p2) ) {
 				return false;
 			}
 		}
 		
 		// Target properties
 		//TODO: Target properties
 		
 		// Name
 		String tmp1 = item1.getName();
 		String tmp2 = item2.getName();
 		if ( tmp1 == null ) {
 			if ( tmp2 != null ) {
 				System.err.println("Name null difference");
 				return false;
 			}
 		}
 		else {
 			if ( tmp2 == null ) {
 				System.err.println("Name null difference");
 				return false;
 			}
 			if ( !tmp1.equals(tmp2) ) {
 				System.err.println("Name difference");
 				return false;
 			}
 		}
 		
 		// Type
 		tmp1 = item1.getType();
 		tmp2 = item2.getType();
 		if ( tmp1 == null ) {
 			if ( tmp2 != null ) {
 				System.err.println("Type null difference");
 				return false;
 			}
 		}
 		else {
 			if ( tmp2 == null ) {
 				System.err.println("Type null difference");
 				return false;
 			}
 			if ( !tmp1.equals(tmp2) ) {
 				System.err.println("Type difference");
 				return false;
 			}
 		}
 		
 		// MIME type
 		tmp1 = item1.getMimeType();
 		tmp2 = item2.getMimeType();
 		if ( tmp1 == null ) {
 			if ( tmp2 != null ) {
 				System.err.println("Mime-type null difference");
 				return false;
 			}
 		}
 		else {
 			if ( tmp2 == null ) {
 				System.err.println("Mime-type null difference");
 				return false;
 			}
 			if ( !tmp1.equals(tmp2) ) {
 				System.err.println("Mime-type difference");
 				return false;
 			}
 		}
 
 		// Is translatable
 		if ( item1.isTranslatable() != item2.isTranslatable() ) {
 			System.err.println("isTranslatable difference");
 			return false;
 		}
 		
 		return true;
 	}
 	
 	public static boolean compareProperty (Property p1,
 		Property p2)
 	{
 		if ( p1 == null ) {
 			if ( p2 != null ) {
 				System.err.println("Property name null difference");
 				return false;
 			}
 			return true;
 		}
 		if ( p2 == null ) {
 			System.err.println("Property name null difference");
 			return false;
 		}
 		
 		if ( !p1.getName().equals(p2.getName()) ) {
 			System.err.println("Property name difference");
 			return false;
 		}
 		if ( p1.isReadOnly() != p2.isReadOnly() ) {
 			System.err.println("Property isReadOnly difference");
 			return false;
 		}
 		if ( p1.getValue() == null ) {
 			if ( p2.getValue() != null ) {
 				System.err.println("Property value null difference");
 				return false;
 			}
 			return true;
 		}
 		if ( !p1.getValue().equals(p2.getValue()) ) {
 			if ( !p1.getName().equals("start") ) { // In double-extraction 'start' can be different
 				System.err.println("Property value difference");
 				return false;
 			}
 		}
 		return true;
 	}
 
 	public static boolean compareTextContainer (TextContainer t1,
 		TextContainer t2)
 	{
 		if ( t1 == null ) {
 			System.err.println("Text container null difference");
 			return (t2 == null);
 		}
 		if ( t2 == null ) {
 			System.err.println("Text container null difference");
 			return false;
 		}
 		
 		if ( !compareTextFragment(t1.getContent(), t2.getContent()) ) {
 			System.err.println("Fragment difference");
 			return false;
 		}
 		
 		if ( t1.isSegmented() ) {
 			if ( !t2.isSegmented() ) {
 				System.err.println("isSegmented difference");
 				return false;
 			}
 			List<Segment> segs1 = t1.getSegments();
 			List<Segment> segs2 = t2.getSegments();
 			if ( segs1.size() != segs2.size() ) {
 				System.err.println("Number of segments difference");
 				return false;
 			}
 			for ( int i=0; i<segs1.size(); i++ ) {
 				Segment seg1 = segs1.get(i);
 				Segment seg2 = segs1.get(i);
 				if ( seg1.id == null ) {
 					if ( seg2.id != null ) return false;
 				}
 				else {
 					if ( seg2.id == null ) return false;
 					if ( !seg1.id.equals(seg2.id) ) return false;
 				}
 				if ( !compareTextFragment(seg1.text, seg2.text) ) {
 					System.err.println("Text fragment difference");
 					return false;
 				}
 			}
 		}
 		else {
 			if ( t2.isSegmented() ) {
 				System.err.println("Segmentation difference");
 				return false;
 			}
 		}
 		
 		return true;
 	}
 	
 	public static boolean compareTextFragment (TextFragment tf1,
 		TextFragment tf2)
 	{
 		if ( tf1 == null ) {
 			if ( tf2 != null ) {
 				System.err.println("Fragment null difference");
 				return false;
 			}
 			return true;
 		}
 		if ( tf2 == null ) {
 			System.err.println("Fragment null difference");
 			return false;
 		}
 		
 		List<Code> codes1 = tf1.getCodes();
 		List<Code> codes2 = tf2.getCodes();
 		if ( codes1.size() != codes2.size() ) {
 			System.err.println("Number of codes difference");
 			return false;
 		}
 		for ( int i=0; i<codes1.size(); i++ ) {
 			Code code1 = codes1.get(i);
 			Code code2 = codes2.get(i);
 			if ( code1.getId() != code2.getId() ) {
 				System.err.println("ID difference");
 				return false;
 			}
 			// Data
 			String tmp1 = code1.getData();
 			String tmp2 = code2.getData();
 			if ( tmp1 == null ) {
 				if ( tmp2 != null ) {
 					System.err.println("Data null difference");
 					return false;
 				}
 			}
 			else {
 				if ( tmp2 == null ) {
 					System.err.println("Data null difference");
 					return false;
 				}
 				if ( !tmp1.equals(tmp2) ) {
 					System.err.println("Data difference: '"+tmp1+"' and '"+tmp2+"'");
 					return false;
 				}
 			}
 			// Outer data
 			tmp1 = code1.getOuterData();
 			tmp2 = code2.getOuterData();
 			if ( tmp1 == null ) {
 				if ( tmp2 != null ) {
 					System.err.println("Outer data null difference");
 					return false;
 				}
 			}
 			else {
 				if ( tmp2 == null ) {
 					System.err.println("Outer data null difference");
 					return false;
 				}
 				if ( !tmp1.equals(tmp2) ) {
 					System.err.println("Outer data difference");
 					return false;
 				}
 			}
 			// Type
 			tmp1 = code1.getType();
 			tmp2 = code2.getType();
 			if ( tmp1 == null ) {
 				if ( tmp2 != null ) {
 					System.err.println("Type null difference");
 					return false;
 				}
 			}
 			else {
 				if ( tmp2 == null ) {
 					System.err.println("Type null difference");
 					return false;
 				}
 				if ( !tmp1.equals(tmp2) ) {
 					System.err.println("Type difference");
 					return false;
 				}
 			}
 			// Tag type
 			if ( code1.getTagType() != code2.getTagType() ) {
 				System.err.println("Tag-type difference");
 				return false;
 			}
 			if ( code1.hasReference() != code2.hasReference() ) {
 				System.err.println("hasReference difference");
 				return false;
 			}
 			if ( code1.isCloneable() != code2.isCloneable() ) {
 				System.err.println("isCloenable difference");
 				return false;
 			}
 			if ( code1.isDeleteable() != code2.isDeleteable() ) {
 				System.err.println("isDeleteable difference");
 				return false;
 			}
 			if ( code1.hasAnnotation() != code2.hasAnnotation() ) {
 				System.err.println("annotation difference");
 				return false;
 			}
 			//TODO: compare annotations
 		}
 		
 		// Coded text
 		if ( !tf1.getCodedText().equals(tf2.getCodedText()) ) {
 			System.err.println("Coded text difference:\n1=\""+tf1.getCodedText()+"\"\n2=\""+tf2.getCodedText()+"\"");
 			return false;
 		}
 		return true;
 	}
 }
