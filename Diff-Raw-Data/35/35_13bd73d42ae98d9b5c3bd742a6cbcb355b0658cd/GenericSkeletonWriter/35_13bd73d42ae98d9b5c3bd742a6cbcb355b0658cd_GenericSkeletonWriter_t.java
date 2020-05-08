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
 
 import net.sf.okapi.common.IResource;
 import net.sf.okapi.common.ISkeleton;
 import net.sf.okapi.common.encoder.EncoderManager;
 import net.sf.okapi.common.filters.IEncoder;
 import net.sf.okapi.common.resource.Code;
 import net.sf.okapi.common.resource.DocumentPart;
 import net.sf.okapi.common.resource.Ending;
 import net.sf.okapi.common.resource.INameable;
 import net.sf.okapi.common.resource.IReferenceable;
 import net.sf.okapi.common.resource.Property;
 import net.sf.okapi.common.resource.StartDocument;
 import net.sf.okapi.common.resource.StartGroup;
 import net.sf.okapi.common.resource.StartSubDocument;
 import net.sf.okapi.common.resource.TextContainer;
 import net.sf.okapi.common.resource.TextFragment;
 import net.sf.okapi.common.resource.TextUnit;
 import net.sf.okapi.common.writer.ILayerProvider;
 
 /**
  * Implements ISkeletonWriter for the GenericSkeleton skeleton. 
  */
 public class GenericSkeletonWriter implements ISkeletonWriter {
 
 	private Stack<StorageList> storageStack;
 	private LinkedHashMap<String, Referent> referents;
 	private String outputLang;
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
 	
 	public String processStartDocument (String outputLanguage,
 		String outputEncoding,
 		ILayerProvider layer,
 		EncoderManager encoderManager,
 		StartDocument resource)
 	{
 		referents = new LinkedHashMap<String, Referent>();
 		storageStack = new Stack<StorageList>();
 
 		this.outputLang = outputLanguage;
 		this.encoderManager = encoderManager;
 		this.outputEncoding = outputEncoding;
 		this.layer = layer;
 		isMultilingual = resource.isMultilingual();
 		if ( this.encoderManager != null ) {
 			this.encoderManager.setDefaultOptions(null, outputEncoding);
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
 		return getString(resource, outputLang, 1);
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
 			return getString((INameable)part.parent, propName, part.language, context);
 		}
 
 		// Set the langToUse and the contextToUse parameters
 		// If lang==null: it's source, so use output language for monolingual
 		String langToUse = (part.language==null) ? outputLang : part.language;
 		int contextToUse = context;
 		if ( isMultilingual ) {
 			langToUse = part.language;
 			// If lang==null: it's source, so not text in multilingual
 			contextToUse = (langToUse==null) ? 0 : context;
 		}
 		
 		// If a parent if set, it's a reference to the content of the resource
 		// holding this skeleton. And it's always a TextUnit
 		if ( part.parent != null ) {
 			if ( part.parent instanceof TextUnit ) {
 				return getContent((TextUnit)part.parent, langToUse, contextToUse);
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
 			return getString((TextUnit)ref, langToUse, contextToUse); //TODO: Test langToUse
 		}
 		if ( ref instanceof GenericSkeletonPart ) {
 			return getString((GenericSkeletonPart)ref, contextToUse);
 		}
 		if ( ref instanceof StorageList ) { // == StartGroup
 			return getString((StorageList)ref, langToUse, contextToUse); //TODO: Test langToUse
 		}
 		// Else: DocumentPart, StartDocument, StartSubDocument 
 		return getString((GenericSkeleton)((IResource)ref).getSkeleton(), context);
 	}
 
 	private String getString (INameable ref,
 		String propName,
 		String langToUse,
 		int context)
 	{
 		if ( ref == null ) {
 			return "-ERR:NULL-REF-";
 		}
 		if ( propName != null ) {
 			return getPropertyValue((INameable)ref, propName, langToUse);
 		}
 		if ( ref instanceof TextUnit ) {
 			return getString((TextUnit)ref, langToUse, context);
 		}
 		if ( ref instanceof DocumentPart ) {
 			return getString((GenericSkeleton)((IResource)ref).getSkeleton(), context);
 		}
 		if ( ref instanceof StorageList ) {
 			return getString((StorageList)ref, langToUse, context);
 		}
 		return "-ERR:INVALID-REFTYPE-";
 	}
 
 	/**
 	 * Gets the skeleton and the original content of a given text unit.
 	 * @param tu The text unit to process.
 	 * @param langToUse Language to output. Use null for the source, or the language
 	 * code for the target languages.
 	 * @param content Context flag: 0=text, 1=skeleton, 2=in-line.
 	 * @return The string representation of the text unit. 
 	 */
 	private String getString (TextUnit tu,
 		String langToUse,
 		int context)
 	{
 		GenericSkeleton skel = (GenericSkeleton)tu.getSkeleton();
 		if ( skel == null ) { // No skeleton
 			return getContent(tu, langToUse, context);
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
 	 * @param langToUse Language to output. Use null for the source, or the language
 	 * code for the target languages.
 	 * @param content Context flag: 0=text, 1=skeleton, 2=inline.
 	 * @return The string representation of the text unit content.
 	 */
 	private String getContent (TextUnit tu,
 		String langToUse,
 		int context) 
 	{
 		// Update the encoder from the TU's MIME type
 		if ( encoderManager != null ) {
 			encoderManager.updateEncoder(tu.getMimeType());
 		}
 		
 		// Get the right text container
 		TextContainer srcCont = tu.getSource();
 		TextContainer trgCont = null;
 		if ( langToUse != null ) {
 			if ( (trgCont = tu.getTarget(langToUse)) == null ) {
 				if ( !srcCont.isSegmented() ) {
 					// Fall back to source, except when the source is segmented
 					trgCont = tu.getSource();
 				}
 			}
 		}
 		// Now trgCont is null only if we have segments and no target is available
 		// Otherwise trgCont is either the available target or the source (fall-back case)
 
 //TODO: Case of unsegmented with target!!!		
 		
 		// Check for segmentation
 		if ( srcCont.isSegmented() ) {
 			// Special case of segmented entry: source + target
 			return getSegmentedText(tu.getSource(), trgCont, langToUse, context);
 		}
 		else { // Normal case: use the calculated target
 			TextContainer cont;
 			if ( langToUse == null ) cont = srcCont;
 			else cont = trgCont;
 			
 			// Apply the layer if there is one
 			if ( layer == null ) {
 				return getContent(cont, langToUse, context);
 			}
 			else {
 				switch ( context ) {
 				case 1:
 					return layer.endCode()
 						+ getContent(cont, langToUse, 0)
 						+ layer.startCode();
 				case 2:
 					return layer.endInline()
 						+ getContent(cont, langToUse, 0)
 						+ layer.startInline();
 				default:
 					return getContent(cont, langToUse, context);
 				}
 			}
 		}
 	}
 	
 	private String getSegmentedText (TextContainer srcCont,
 		TextContainer trgCont,
 		String langToUse,
 		int context)
 	{
 		StringBuilder tmp = new StringBuilder();
 		List<TextFragment> srcSegs = srcCont.getSegments();
 		List<TextFragment> trgSegs = null;
 		if ( trgCont != null ) trgSegs = trgCont.getSegments();
 		String text = srcCont.getCodedText();
 		Code code;
 		for ( int i=0; i<text.length(); i++ ) {
 			switch ( text.charAt(i) ) {
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
 					trgFrag = trgSegs.get(n);
 					lev = 100;
 				}
 				//TODO: Else, source/target segments do not match
 				// Write it
 				if ( layer == null ) {
 					if ( trgFrag == null ) {
 						tmp.append(getContent(srcSegs.get(n), null, context));
 					}
 					else { // No target available: use the source
 						tmp.append(getContent(trgFrag, langToUse, context));
 					}
 				}
 				else {
 					switch ( context ) {
 					case 1:
 						tmp.append(layer.endCode()
 							+ layer.startSegment()
 							+ getContent(srcSegs.get(n), null, 0)
 							+ layer.midSegment(lev)
 							+ ((trgFrag==null) ? "" : getContent(trgFrag, langToUse, 0))
 							+ layer.endSegment()
 							+ layer.startCode());
 						break;
 					case 2:
 						tmp.append(layer.endInline()
 							+ layer.startSegment()
 							+ getContent(srcSegs.get(n), null, 0)
 							+ layer.midSegment(lev)
 							+ ((trgFrag==null) ? "" : getContent(trgFrag, langToUse, 0))
 							+ layer.endSegment()
 							+ layer.startInline());
 						break;
 					default:
 						tmp.append(layer.startSegment()
 							+ getContent(srcSegs.get(n), null, context)
 							+ layer.midSegment(lev)
 							+ ((trgFrag==null) ? "" : getContent(trgFrag, langToUse, context))
 							+ layer.endSegment());
 						break;
 					}
 				}
 				break;
 			default:
 				if ( encoderManager == null ) {
 					if ( layer == null ) {
 						tmp.append(text.charAt(i));
 					}
 					else {
 						tmp.append(layer.encode(text.charAt(i), context));
 					}
 				}
 				else {
 					if ( layer == null ) {
 						tmp.append(encoderManager.encode(text.charAt(i), context));
 					}
 					else {
 						tmp.append(layer.encode(
 							encoderManager.encode(text.charAt(i), context),
 							context));
 					}
 				}
 				break;
 			}
 		}
 		return tmp.toString();
 	}
 
 	private String getContent (TextFragment tf,
 		String langToUse,
 		int context)
 	{
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
 		for ( int i=0; i<text.length(); i++ ) {
 			switch ( text.charAt(i) ) {
 			case TextFragment.MARKER_OPENING:
 				code = codes.get(TextFragment.toIndex(text.charAt(++i)));
 				tmp.append(expandCodeContent(code, langToUse, context));
 				break;
 			case TextFragment.MARKER_CLOSING:
 				code = codes.get(TextFragment.toIndex(text.charAt(++i)));
 				tmp.append(expandCodeContent(code, langToUse, context));
 				break;
 			case TextFragment.MARKER_ISOLATED:
 			case TextFragment.MARKER_SEGMENT:
 				code = codes.get(TextFragment.toIndex(text.charAt(++i)));
 				tmp.append(expandCodeContent(code, langToUse, context));
 				break;
 			default:
 				if ( encoderManager == null ) {
 					if ( layer == null ) {
 						tmp.append(text.charAt(i));
 					}
 					else {
 						tmp.append(layer.encode(text.charAt(i), context));
 					}
 				}
 				else {
 					if ( layer == null ) {
 						tmp.append(encoderManager.encode(text.charAt(i), context));
 					}
 					else {
 						tmp.append(layer.encode(
 								encoderManager.encode(text.charAt(i), context),
 							context));
 					}
 				}
 				break;
 			}
 		}
 		return tmp.toString();
 	}
 	
 	private String expandCodeContent (Code code,
 		String langToUse,
 		int context)
 	{
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
 					getPropertyValue((INameable)ref, propName, langToUse));
 			}
 			else if ( ref instanceof TextUnit ) {
 				tmp.replace(start, end, getString((TextUnit)ref, langToUse, 2));
 			}
 			else if ( ref instanceof GenericSkeletonPart ) {
 				tmp.replace(start, end, getString((GenericSkeletonPart)ref, 2));
 			}
 			else if ( ref instanceof StorageList ) { // == StartGroup
 				tmp.replace(start, end, getString((StorageList)ref, langToUse, 2));
 			}
 			else { // DocumentPart, StartDocument, StartSubDocument 
 				tmp.replace(start, end, getString((GenericSkeleton)((IResource)ref).getSkeleton(), 2));
 			}
 		}
 		return tmp.toString();
 	}
 	
 	private String getString (StorageList list,
 		String langToUse,
 		int context)
 	{
 		StringBuilder tmp = new StringBuilder();
 		// Treat the skeleton of this list
 		tmp.append(getString((GenericSkeleton)list.getSkeleton(), context));		
 		// Then treat the list itself
 		for ( IResource res : list ) {
 			if ( res instanceof TextUnit ) {
 				tmp.append(getString((TextUnit)res, langToUse, context));
 			}
 			else if ( res instanceof StorageList ) {
 				tmp.append(getString((StorageList)res, langToUse, context));
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
 		String langToUse)
 	{
 		// Get the value based on the output language
 		Property prop;
 		if ( langToUse == null ) { // Use the source
 			prop = resource.getSourceProperty(name);
 		}
 		else if ( langToUse.length() == 0 ) { // Use the resource-level properties
 			prop = resource.getProperty(name);
 		}
 		else { // Use the given target language if possible
 			if ( resource.hasTargetProperty(langToUse, name) ) {
 				prop = resource.getTargetProperty(langToUse, name);
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
 		if ( IEncoder.PROP_LANGUAGE.equals(name) ) {
 			value = outputLang;
 		}
 		else if ( IEncoder.PROP_ENCODING.equals(name) ) {
 			value = outputEncoding;
 		}
 		// Return the native value if possible
 		if ( encoderManager == null ) return value;
 		else return encoderManager.toNative(name, value);
 	}
 
 }
