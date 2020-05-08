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
 
 package net.sf.okapi.common.skeleton;
 
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Stack;
 
 import net.sf.okapi.common.Event;
 import net.sf.okapi.common.IResource;
 import net.sf.okapi.common.ISkeleton;
 import net.sf.okapi.common.annotation.ScoresAnnotation;
 import net.sf.okapi.common.encoder.EncoderManager;
 import net.sf.okapi.common.filterwriter.ILayerProvider;
 import net.sf.okapi.common.LocaleId;
 import net.sf.okapi.common.resource.Code;
 import net.sf.okapi.common.resource.DocumentPart;
 import net.sf.okapi.common.resource.Ending;
 import net.sf.okapi.common.resource.INameable;
 import net.sf.okapi.common.resource.IReferenceable;
 import net.sf.okapi.common.resource.Property;
 import net.sf.okapi.common.resource.Segment;
 import net.sf.okapi.common.resource.StartDocument;
 import net.sf.okapi.common.resource.StartGroup;
 import net.sf.okapi.common.resource.StartSubDocument;
 import net.sf.okapi.common.resource.TextContainer;
 import net.sf.okapi.common.resource.TextFragment;
 import net.sf.okapi.common.resource.TextUnit;
 
 /**
  * Implements ISkeletonWriter for the GenericSkeleton skeleton. 
  */
 public class GenericSkeletonWriter implements ISkeletonWriter {
 
 	private Stack<StorageList> storageStack;
 	private LinkedHashMap<String, Referent> referents;
 	private LocaleId inputLoc;
 	private LocaleId outputLoc;
 	private String outputEncoding;
 	private boolean isMultilingual;
 	private ILayerProvider layer;
 	private EncoderManager encoderManager;
 	
 	private IReferenceable getReference (String id) {
 		if ( referents == null ) return null;
 		//IReferenceable ref = referents.get(id);
 		Referent ref = referents.get(id);
 		// Remove the object found from the list
 		if (( ref != null ) && ( (--ref.count)==0 )) {
 			referents.remove(id);
 		}
 		return ref.ref;
 	}
 
 	public void close () {
 		if ( referents != null ) {
 			referents.clear();
 			referents = null;
 		}
 		if ( storageStack != null ) {
 			storageStack.clear();
 			storageStack = null;
 		}
 	}
 	
 	public String processStartDocument (LocaleId outputLocale,
 		String outputEncoding,
 		ILayerProvider layer,
 		EncoderManager encoderManager,
 		StartDocument resource)
 	{
 		referents = new LinkedHashMap<String, Referent>();
 		storageStack = new Stack<StorageList>();
 
 		this.inputLoc = resource.getLocale();
 		this.outputLoc = outputLocale;
 		this.encoderManager = encoderManager;
 		this.outputEncoding = outputEncoding;
 		this.layer = layer;
 		isMultilingual = resource.isMultilingual();
 		if ( this.encoderManager != null ) {
 			this.encoderManager.setDefaultOptions(resource.getFilterParameters(), outputEncoding,
 				resource.getLineBreak());
 			encoderManager.updateEncoder(resource.getMimeType());
 		}
 		
 		return getString((GenericSkeleton)resource.getSkeleton(), 1);
 	}
 
 	public String processEndDocument (Ending resource) {
 		return getString((GenericSkeleton)resource.getSkeleton(), 1);
 	}
 
 	public String processStartSubDocument (StartSubDocument resource) {
 		if ( storageStack.size() > 0 ) {
 			storageStack.peek().add(resource);
 			return "";
 		}
 		return getString((GenericSkeleton)resource.getSkeleton(), 1);
 	}
 
 	public String processEndSubDocument (Ending resource) {
 		if ( storageStack.size() > 0 ) {
 			storageStack.peek().add(resource);
 			return "";
 		}
 		return getString((GenericSkeleton)resource.getSkeleton(), 1);
 	}
 	
 	public String processStartGroup (StartGroup resource) {
 		if ( resource.isReferent() ) {
 			StorageList sl = new StorageList(resource);
 			referents.put(sl.getId(), new Referent(sl));
 			storageStack.push(sl);
 			return "";
 		}
 		if ( storageStack.size() > 0 ) {
 			StorageList sl = new StorageList(resource);
 			storageStack.peek().add(sl);
 			storageStack.push(sl);
 			return "";
 		}
 		return getString((GenericSkeleton)resource.getSkeleton(), 1);
 	}
 	
 	public String processEndGroup (Ending resource) {
 		if ( storageStack.size() > 0 ) {
 			storageStack.peek().add(resource);
 			storageStack.pop();
 			return "";
 		}
 		return getString((GenericSkeleton)resource.getSkeleton(), 1);
 	}
 	
 	public String processTextUnit (TextUnit resource) {
 		if ( resource.isReferent() ) {
 			referents.put(resource.getId(), new Referent(resource));
 			return "";
 		}
 		if ( storageStack.size() > 0 ) {
 			storageStack.peek().add(resource);
 			return "";
 		}
 		return getString(resource, outputLoc, 1);
 	}
 
 	public String processDocumentPart (DocumentPart resource) {
 		if ( resource.isReferent() ) {
 			referents.put(resource.getId(), new Referent(resource));
 			return "";
 		}
 		if ( storageStack.size() > 0 ) {
 			storageStack.peek().add(resource);
 			return "";
 		}
 		return getString((GenericSkeleton)resource.getSkeleton(), 1);
 	}
 	
 	private String getString (ISkeleton skeleton,
 		int context)
 	{
 		if ( skeleton == null ) return "";
 		StringBuilder tmp = new StringBuilder();
 		for ( GenericSkeletonPart part : ((GenericSkeleton)skeleton).getParts() ) {
 			tmp.append(getString(part, context));
 		}
 		return tmp.toString();
 	}
 	
 	private String getString (GenericSkeletonPart part,
 		int context)
 	{
 		// If it is not a reference marker, just use the data
 		if ( !part.data.toString().startsWith(TextFragment.REFMARKER_START) ) {
 			if ( layer == null ) {
 				return part.data.toString();
 			}
 			else {
 				return layer.encode(part.data.toString(), context);
 			}
 		}
 		// Get the reference info
 		Object[] marker = TextFragment.getRefMarker(part.data);
 		// Check for problem
 		if ( marker == null ) {
 			return "-ERR:INVALID-REF-MARKER-";
 		}
 		String propName = (String)marker[3];
 
 		// If we have a property name: It's a reference to a property of 
 		// the resource holding this skeleton
 		if ( propName != null ) { // Reference to the content of the referent
 			return getString((INameable)part.parent, propName, part.locId, context);
 		}
 
 		// Set the locToUse and the contextToUse parameters
 		// If locToUse==null: it's source, so use output locale for monolingual
 		LocaleId locToUse = (part.locId==null) ? outputLoc : part.locId;
 		int contextToUse = context;
 		if ( isMultilingual ) {
 			locToUse = part.locId;
 			// If locToUse==null: it's source, so not text in multilingual
 			contextToUse = (locToUse==null) ? 0 : context;
 		}
 		
 		// If a parent if set, it's a reference to the content of the resource
 		// holding this skeleton. And it's always a TextUnit
 		if ( part.parent != null ) {
 			if ( part.parent instanceof TextUnit ) {
 				return getContent((TextUnit)part.parent, locToUse, contextToUse);
 			}
 			else {
 				throw new RuntimeException("The self-reference to this skeleton part must be a text-unit.");
 			}
 		}
 		
 		// Else this is a true reference to a referent
 		IReferenceable ref = getReference((String)marker[0]);
 		if ( ref == null ) {
 			return "-ERR:REF-NOT-FOUND-";
 		}
 		if ( ref instanceof TextUnit ) {
 			return getString((TextUnit)ref, locToUse, contextToUse); //TODO: Test locToUse
 		}
 		if ( ref instanceof GenericSkeletonPart ) {
 			return getString((GenericSkeletonPart)ref, contextToUse);
 		}
 		if ( ref instanceof StorageList ) { // == StartGroup
 			return getString((StorageList)ref, locToUse, contextToUse); //TODO: Test locToUse
 		}
 		// Else: DocumentPart, StartDocument, StartSubDocument 
 		return getString((GenericSkeleton)((IResource)ref).getSkeleton(), context);
 	}
 
 	private String getString (INameable ref,
 		String propName,
 		LocaleId locToUse,
 		int context)
 	{
 		if ( ref == null ) {
 			return "-ERR:NULL-REF-";
 		}
 		if ( propName != null ) {
 			return getPropertyValue((INameable)ref, propName, locToUse, context);
 		}
 		if ( ref instanceof TextUnit ) {
 			return getString((TextUnit)ref, locToUse, context);
 		}
 		if ( ref instanceof DocumentPart ) {
 			return getString((GenericSkeleton)((IResource)ref).getSkeleton(), context);
 		}
 		if ( ref instanceof StorageList ) {
 			return getString((StorageList)ref, locToUse, context);
 		}
 		return "-ERR:INVALID-REFTYPE-";
 	}
 
 	/**
 	 * Gets the skeleton and the original content of a given text unit.
 	 * @param tu The text unit to process.
 	 * @param locToUse locale to output. Use null for the source, or a LocaleId
 	 * object for the target locales.
 	 * @param content Context flag: 0=text, 1=skeleton, 2=in-line.
 	 * @return The string representation of the text unit. 
 	 */
 	private String getString (TextUnit tu,
 		LocaleId locToUse,
 		int context)
 	{
 		GenericSkeleton skel = (GenericSkeleton)tu.getSkeleton();
 		if ( skel == null ) { // No skeleton
 			return getContent(tu, locToUse, context);
 		}
 		// Else: process the skeleton parts, one of them should
 		// refer to the text-unit content itself
 		StringBuilder tmp = new StringBuilder();
 		for ( GenericSkeletonPart part : skel.getParts() ) {
 			tmp.append(getString(part, context));
 		}
 		return tmp.toString();
 	}
 
 	/**
 	 * Gets the original content of a given text unit.
 	 * @param tu The text unit to process.
 	 * @param locToUse locale to output. Use null for the source, or the locale
 	 * for the target locales.
 	 * @param content Context flag: 0=text, 1=skeleton, 2=inline.
 	 * @return The string representation of the text unit content.
 	 */
	protected String getContent (TextUnit tu,
 		LocaleId locToUse,
		int context) // protected for OpenXML
 	{
 		// Update the encoder from the TU's MIME type
 		if ( encoderManager != null ) {
 			encoderManager.updateEncoder(tu.getMimeType());
 		}
 		
 		// Get the right text container
 		TextContainer srcCont = tu.getSource();
 		TextContainer trgCont = null;
 		if ( locToUse != null ) {
 			if ( (trgCont = tu.getTarget(locToUse)) == null ) {
 				if ( !srcCont.isSegmented() ) {
 					// Fall back to source, except when the source is segmented
 					trgCont = tu.getSource();
 				}
 			}
 		}
 		// Now trgCont is null only if we have segments and no target is available
 		// Otherwise trgCont is either the available target or the source (fall-back case)
 
 //TODO: Case of unsegmented with target!!!		
 
 		if ( !tu.isTranslatable() ) {
 			context = 0; // Keep skeleton context
 		}
 		// Check for segmentation
 		if ( srcCont.isSegmented() ) {
 			// Special case of segmented entry: source + target
 			return getSegmentedText(tu.getSource(), trgCont, locToUse, context);
 		}
 		else { // Normal case: use the calculated target
 			TextContainer cont;
 			if ( locToUse == null ) cont = srcCont;
 			else cont = trgCont;
 			
 			// Apply the layer if there is one
 			if ( layer == null ) {
 				return getContent(cont, locToUse, context);
 			}
 			else {
 				switch ( context ) {
 				case 1:
 					return layer.endCode()
 						+ getContent(cont, locToUse, 0)
 						+ layer.startCode();
 				case 2:
 					return layer.endInline()
 						+ getContent(cont, locToUse, 0)
 						+ layer.startInline();
 				default:
 					return getContent(cont, locToUse, context);
 				}
 			}
 		}
 	}
 	
 	private String getSegmentedText (TextContainer srcCont,
 		TextContainer trgCont,
 		LocaleId locToUse,
 		int context)
 	{
 		StringBuilder tmp = new StringBuilder();
 		List<Segment> srcSegs = srcCont.getSegments();
 		List<Segment> trgSegs = null;
 		ScoresAnnotation scores = null;
 		if ( trgCont != null ) {
 			trgSegs = trgCont.getSegments();
 			scores = trgCont.getAnnotation(ScoresAnnotation.class);
 		}
 		String text = srcCont.getCodedText();
 		Code code;
 		char ch;
 		for ( int i=0; i<text.length(); i++ ) {
 			ch = text.charAt(i);
 			switch ( ch ) {
 			case TextFragment.MARKER_OPENING:
 			case TextFragment.MARKER_CLOSING:
 			case TextFragment.MARKER_ISOLATED:
 				//TODO: Handle codes outside the segments!!!
 				break;
 			case TextFragment.MARKER_SEGMENT:
 				code = srcCont.getCode(text.charAt(++i));
 				int n = Integer.valueOf(code.getData());
 				// Check segment source/target
 				TextFragment trgFrag = null;
 				int lev = 0;
 				if (( trgSegs != null ) && ( n < trgSegs.size() )) {
 					trgFrag = trgSegs.get(n).text;
 					if ( scores != null ) lev = scores.getScore(n);
 				}
 				if ( trgFrag == null ) { // No target available: use the source
 					trgFrag = srcSegs.get(n).text;
 				}
 				// Write it
 				if ( layer == null ) {
 //TODO: deal with not-in segment leading text
 //TODO: deal with not-in-segment codes
 					// Get the inter-segment characters at the end of the segment
 					// So derived writers can treat all chars in getContent()
 					// i currently points to the index of the segment marker
 					int j; // Move forward until we found a marker or the end of the text
 					for ( j=1; i+j<text.length(); j++ ) {
 						if ( TextFragment.isMarker(text.charAt(i+j)) ) {
 							break;
 						}
 					} // Now j-1 should be the number of characters to add
 					if ( j > 1 ) {
 						trgFrag = trgFrag.clone(); // Make sure we don't change the original
 						trgFrag.append(text.substring(i+1, i+j));
 						i += (j-1); // Move the pointer at the last char we put in the segment
 					}
 					// Now get the content for the segment
 					tmp.append(getContent(trgFrag, locToUse, context));
 				}
 				else {
 					switch ( context ) {
 					case 1:
 						tmp.append(layer.endCode()
 							+ layer.startSegment()
 							+ getContent(srcSegs.get(n).text, null, 0)
 							+ layer.midSegment(lev)
 							+ ((trgFrag==null) ? "" : getContent(trgFrag, locToUse, 0))
 							+ layer.endSegment()
 							+ layer.startCode());
 						break;
 					case 2:
 						tmp.append(layer.endInline()
 							+ layer.startSegment()
 							+ getContent(srcSegs.get(n).text, null, 0)
 							+ layer.midSegment(lev)
 							+ ((trgFrag==null) ? "" : getContent(trgFrag, locToUse, 0))
 							+ layer.endSegment()
 							+ layer.startInline());
 						break;
 					default:
 						tmp.append(layer.startSegment()
 							+ getContent(srcSegs.get(n).text, null, context)
 							+ layer.midSegment(lev)
 							+ ((trgFrag==null) ? "" : getContent(trgFrag, locToUse, context))
 							+ layer.endSegment());
 						break;
 					}
 				}
 				break;
 			default:
 				if ( Character.isHighSurrogate(ch) ) {
 					int cp = text.codePointAt(i);
 					i++; // Skip low-surrogate
 					if ( encoderManager == null ) {
 						if ( layer == null ) {
 							tmp.append(new String(Character.toChars(cp)));
 						}
 						else {
 							tmp.append(layer.encode(cp, context));
 						}
 					}
 					else {
 						if ( layer == null ) {
 							tmp.append(encoderManager.encode(cp, context));
 						}
 						else {
 							tmp.append(layer.encode(
 								encoderManager.encode(cp, context),
 								context));
 						}
 					}
 				}
 				else { // Non-supplemental case
 					if ( encoderManager == null ) {
 						if ( layer == null ) {
 							tmp.append(ch);
 						}
 						else {
 							tmp.append(layer.encode(ch, context));
 						}
 					}
 					else {
 						if ( layer == null ) {
 							tmp.append(encoderManager.encode(ch, context));
 						}
 						else {
 							tmp.append(layer.encode(
 								encoderManager.encode(ch, context),
 								context));
 						}
 					}
 				}
 				break;
 			}
 		}
 		return tmp.toString();
 	}
 
 	public String getContent (TextFragment tf,
 		LocaleId locToUse,
 		int context)
 	{ // this needs to be public for an override in OpenXML
 		// Output simple text
 		if ( !tf.hasCode() ) {
 			if ( encoderManager == null ) {
 				if ( layer == null ) {
 					return tf.toString();
 				}
 				else {
 					return layer.encode(tf.toString(), context);
 				}
 			}
 			else {
 				if ( layer == null ) {
 					return encoderManager.encode(tf.toString(), context);
 				}
 				else {
 					return layer.encode(
 						encoderManager.encode(tf.toString(), context), context);
 				}
 			}
 		}
 
 		// Output text with in-line codes
 		List<Code> codes = tf.getCodes();
 		StringBuilder tmp = new StringBuilder();
 		String text = tf.getCodedText();
 		Code code;
 		char ch;
 		for ( int i=0; i<text.length(); i++ ) {
 			ch = text.charAt(i);
 			switch ( ch ) {
 			case TextFragment.MARKER_OPENING:
 				code = codes.get(TextFragment.toIndex(text.charAt(++i)));
 				tmp.append(expandCodeContent(code, locToUse, context));
 				break;
 			case TextFragment.MARKER_CLOSING:
 				code = codes.get(TextFragment.toIndex(text.charAt(++i)));
 				tmp.append(expandCodeContent(code, locToUse, context));
 				break;
 			case TextFragment.MARKER_ISOLATED:
 			case TextFragment.MARKER_SEGMENT:
 				code = codes.get(TextFragment.toIndex(text.charAt(++i)));
 				tmp.append(expandCodeContent(code, locToUse, context));
 				break;
 			default:
 				if ( Character.isHighSurrogate(ch) ) {
 					int cp = text.codePointAt(i);
 					i++; // Skip low-surrogate
 					if ( encoderManager == null ) {
 						if ( layer == null ) {
 							tmp.append(new String(Character.toChars(cp)));
 						}
 						else {
 							tmp.append(layer.encode(cp, context));
 						}
 					}
 					else {
 						if ( layer == null ) {
 							tmp.append(encoderManager.encode(cp, context));
 						}
 						else {
 							tmp.append(layer.encode(
 								encoderManager.encode(cp, context),
 								context));
 						}
 					}
 				}
 				else { // Non-supplemental case
 					if ( encoderManager == null ) {
 						if ( layer == null ) {
 							tmp.append(ch);
 						}
 						else {
 							tmp.append(layer.encode(ch, context));
 						}
 					}
 					else {
 						if ( layer == null ) {
 							tmp.append(encoderManager.encode(ch, context));
 						}
 						else {
 							tmp.append(layer.encode(
 								encoderManager.encode(ch, context),
 								context));
 						}
 					}
 				}
 				break;
 			}
 		}
 		return tmp.toString();
 	}
 	
 	protected String expandCodeContent (Code code,
 		LocaleId locToUse,
 		int context)
 	{ // this needs to be protected, not private, for OpenXML
 		String codeTmp = code.getOuterData();
 		if ( layer != null ) {
 			codeTmp = layer.startInline() 
 				+ layer.encode(codeTmp, 2)
 				+ layer.endInline();
 		}
 		if ( !code.hasReference() ) {
 			return codeTmp;
 		}
 		// Check for segment
 		if ( code.getType().equals(TextFragment.CODETYPE_SEGMENT) ) {
 			if ( layer == null ) {
 				return "[SEG-"+code.getData()+"]";
 			}
 			else {
 				return layer.startInline()
 					+ layer.encode("[SEG-"+code.getData()+"]", 2)
 					+ layer.endInline();
 			}
 		}
 		// Else: look for place-holders
 		StringBuilder tmp = new StringBuilder(codeTmp);
 		Object[] marker = null;
 		while ( (marker = TextFragment.getRefMarker(tmp)) != null ) {
 			int start = (Integer)marker[1];
 			int end = (Integer)marker[2];
 			String propName = (String)marker[3];
 			IReferenceable ref = getReference((String)marker[0]);
 			if ( ref == null ) {
 				tmp.replace(start, end, "-ERR:REF-NOT-FOUND-");
 			}
 			else if ( propName != null ) {
 				tmp.replace(start, end,
 					getPropertyValue((INameable)ref, propName, locToUse, 2));
 			}
 			else if ( ref instanceof TextUnit ) {
 				tmp.replace(start, end, getString((TextUnit)ref, locToUse, 2));
 			}
 			else if ( ref instanceof GenericSkeletonPart ) {
 				tmp.replace(start, end, getString((GenericSkeletonPart)ref, 2));
 			}
 			else if ( ref instanceof StorageList ) { // == StartGroup
 				tmp.replace(start, end, getString((StorageList)ref, locToUse, 2));
 			}
 			else { // DocumentPart, StartDocument, StartSubDocument 
 				tmp.replace(start, end, getString((GenericSkeleton)((IResource)ref).getSkeleton(), 2));
 			}
 		}
 		return tmp.toString();
 	}
 	
 	private String getString (StorageList list,
 		LocaleId locToUse,
 		int context)
 	{
 		StringBuilder tmp = new StringBuilder();
 		// Treat the skeleton of this list
 		tmp.append(getString((GenericSkeleton)list.getSkeleton(), context));		
 		// Then treat the list itself
 		for ( IResource res : list ) {
 			if ( res instanceof TextUnit ) {
 				tmp.append(getString((TextUnit)res, locToUse, context));
 			}
 			else if ( res instanceof StorageList ) {
 				tmp.append(getString((StorageList)res, locToUse, context));
 			}
 			else if ( res instanceof DocumentPart ) {
 				tmp.append(getString((GenericSkeleton)res.getSkeleton(), context));
 			}
 			else if ( res instanceof Ending ) {
 				tmp.append(getString((GenericSkeleton)res.getSkeleton(), context));
 			}
 		}
 		return tmp.toString();
 	}
 	
 	private String getPropertyValue (INameable resource,
 		String name,
 		LocaleId locToUse,
 		int context)
 	{
 		// Update the encoder from the TU's MIME type
 		if ( encoderManager != null ) {
 			encoderManager.updateEncoder(resource.getMimeType());
 		}
 
 		// Get the value based on the output locale
 		Property prop;
 		if ( locToUse == null ) { // Use the source
 			prop = resource.getSourceProperty(name);
 		}
 		else if ( locToUse.equals(LocaleId.EMPTY) ) { // Use the resource-level properties
 			prop = resource.getProperty(name);
 		}
 		else { // Use the given target locale if possible
 			if ( resource.hasTargetProperty(locToUse, name) ) {
 				prop = resource.getTargetProperty(locToUse, name);
 			}
 			else { // Fall back to source if there is no target
 				prop = resource.getSourceProperty(name);				
 			}
 		}
 		// Check the property we got
 		if ( prop == null ) return "-ERR:PROP-NOT-FOUND-";
 		// Else process the value
 		String value = prop.getValue();
 		if ( value == null ) return "-ERR:PROP-VALUE-NULL-";
 		
 		// Else: We got the property value
 		// Check if it needs to be auto-modified
 		if ( Property.LANGUAGE.equals(name) ) {
 			// If it is the input locale, we change it with the output locale
 			//TODO: Do we need an option to be region-insensitive? (en==en-gb)
 			LocaleId locId = LocaleId.fromString(value);
 			if ( locId.equals(inputLoc) ) {
 				value = outputLoc.toString();
 			}
 		}
 		else if ( Property.ENCODING.equals(name) ) {
 			value = outputEncoding;
 		}
 		// Return the native value if possible
 		if ( encoderManager == null ) {
 			if ( layer == null ) return value;
 			else return layer.encode(value, context); //TODO: context correct??
 		}
 		else {
 			if ( layer == null ) return encoderManager.toNative(name, value);
 			else return layer.encode(encoderManager.toNative(name, value), context);
 		}
 	}
 	public void addToReferents(Event event) // for OpenXML, so referents can stay private
 	{
 		IResource resource;
 		if (event!=null)
 		{
 			if (referents==null)
 			{
 				referents = new LinkedHashMap<String, Referent>();
 				storageStack = new Stack<StorageList>();
 			}
 			resource = event.getResource();
 			if (resource!=null)
 			{
 				switch(event.getEventType())
 				{
 					case TEXT_UNIT:
 						if (((TextUnit)resource).isReferent())
 							referents.put(resource.getId(), new Referent((TextUnit)resource));
 						break;
 					case DOCUMENT_PART:
 						if (((DocumentPart)resource).isReferent())
 							referents.put(resource.getId(), new Referent((DocumentPart)resource));
 						break;
 					case START_GROUP:
 						if (((StartGroup)resource).isReferent())
 						{
 							StorageList sl = new StorageList((StartGroup)resource);
 							referents.put(sl.getId(), new Referent(sl));
 						}
 						break;
 					default:
 						break;
 				}
 			}
 		}
 	}
 }
