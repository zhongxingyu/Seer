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
 
 package net.sf.okapi.steps.textmodification;
 
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import net.sf.okapi.common.Event;
 import net.sf.okapi.common.IParameters;
 import net.sf.okapi.common.IResource;
 import net.sf.okapi.common.LocaleId;
 import net.sf.okapi.common.pipeline.BasePipelineStep;
 import net.sf.okapi.common.pipeline.annotations.StepParameterMapping;
 import net.sf.okapi.common.pipeline.annotations.StepParameterType;
 import net.sf.okapi.common.resource.TextContainer;
 import net.sf.okapi.common.resource.TextFragment;
 import net.sf.okapi.common.resource.TextUnit;
 import net.sf.okapi.lib.segmentation.ISegmenter;
 import net.sf.okapi.lib.segmentation.SRXDocument;
 
 public class TextModificationStep extends BasePipelineStep {
 
 	private final Logger logger = Logger.getLogger(getClass().getName());
 
 	private static final char TMPSTARTSEG = '\u0002';
 	private static final char TMPENDSEG = '\u0003';
 	private static final char STARTSEG = '[';
 	private static final char ENDSEG = ']';
 	private static final String OLDCHARS = "AaEeIiOoUuYyCcDdNn";
 	private static final String NEWCHARS = "\u00c2\u00e5\u00c9\u00e8\u00cf\u00ec\u00d8\u00f5\u00db\u00fc\u00dd\u00ff\u00c7\u00e7\u00d0\u00f0\u00d1\u00f1";
 
 	private Parameters params;
 	private ISegmenter srcSeg;
 	private ISegmenter trgSeg;
 	private LocaleId sourceLocale;
 	private LocaleId targetLocale;
 	private boolean initDone;
 	
 
 	public TextModificationStep () {
 		params = new Parameters();
 	}
 	
 	@StepParameterMapping(parameterType = StepParameterType.SOURCE_LOCALE)
 	public void setsourceLocale (LocaleId sourceLocale) {
 		this.sourceLocale = sourceLocale;
 	}
 	
 	@StepParameterMapping(parameterType = StepParameterType.TARGET_LOCALE)
 	public void setTargetLocale (LocaleId targetLocale) {
 		this.targetLocale = targetLocale;
 	}
 	
 	public String getName () {
 		return "Text Modification";
 	}
 
 	public String getDescription () {
 		return "Apply various modifications to the text units content of a document.";
 	}
 
 	@Override
 	public IParameters getParameters () {
 		return params;
 	}
 
 	@Override
 	public void setParameters (IParameters params) {
 		params = (Parameters)params;
 	}
  
 	protected void handleStartBatch (Event event) {
 		initDone = false;
 	}
 	
 	@Override
 	protected void handleStartBatchItem (Event event) {
 		if ( initDone ) return; // Initialize once per batch
 		if ( params.segment ) {
 			String src = params.sourceSrxPath; //.replace(VAR_PROJDIR, projectDir);
 			String trg = params.targetSrxPath; //.replace(VAR_PROJDIR, projectDir);
 			SRXDocument srxDoc = new SRXDocument();
 			srxDoc.loadRules(src);
 			if ( srxDoc.hasWarning() ) logger.warning(srxDoc.getWarning());
 			srcSeg = srxDoc.compileLanguageRules(sourceLocale, null);
 			if ( !src.equals(trg) ) {
 				srxDoc.loadRules(trg);
 				if ( srxDoc.hasWarning() ) logger.warning(srxDoc.getWarning());
 			}
 			trgSeg = srxDoc.compileLanguageRules(targetLocale, null);
 		}
 		initDone = true;
 	}
 	
 	@Override
 	protected void handleTextUnit (Event event) {
 		TextUnit tu = (TextUnit)event.getResource();
 		// Skip non-translatable
 		if ( !tu.isTranslatable() ) return;
 		// Skip if already translate (only if required)
 		if ( !params.applyToExistingTarget && tu.hasTarget(targetLocale) ) return;
 		
 		// Apply the segmentation and/or segment marks if requested
 		if ( params.segment || params.markSegments ) {
 			if ( tu.hasTarget(targetLocale) ) {
 				if ( params.segment ) {
 					trgSeg.computeSegments(tu.getTarget(targetLocale));
 					tu.getTarget(targetLocale).createSegments(trgSeg.getRanges());
 				}
 			}
 			else {
 				if ( params.segment ) {
 					srcSeg.computeSegments(tu.getSource());
 					tu.getSource().createSegments(srcSeg.getRanges());
 				}
 			}
 		}
 		
 		// Else: do the requested modifications
 		// Make sure we have target content
		tu.createTarget(targetLocale, false, IResource.COPY_ALL);
 
 		// Merge all segments if needed
 		if ( params.segment || params.markSegments ) {
 			mergeSegments(tu.getTarget(targetLocale));
 			// Merge also the source to be in synch.
 			tu.getSource().mergeAllSegments();
 		}
 
 		// Other text modification are done after merging all segments
 		switch ( params.type ) {
 		case Parameters.TYPE_XNREPLACE:
 			replaceWithXN(tu);
 			break;
 		case Parameters.TYPE_EXTREPLACE:
 			replaceWithExtendedChars(tu);
 			break;
 		case Parameters.TYPE_KEEPINLINE:
 			removeText(tu);
 			break;
 		}
 	
 		if ( params.addPrefix || params.addSuffix || params.addName || params.addID ) {
 			addText(tu);
 		}
 	}
 
 	/**
 	 * Removes the text but leaves the inline code.
 	 * @param tu the text unit to process.
 	 */
 	private void removeText (TextUnit tu) {
 		String result = tu.getTarget(targetLocale).getCodedText();
 		StringBuilder sb = new StringBuilder();
 		
 		for ( int i=0; i<result.length(); i++ ) {
 			switch ( result.charAt(i) ) {
 			    case TextFragment.MARKER_OPENING:
 				case TextFragment.MARKER_CLOSING:
 				case TextFragment.MARKER_ISOLATED:
 				case TextFragment.MARKER_SEGMENT:
 					sb.append(result.charAt(i));
 					sb.append(result.charAt(++i));
 					break;
 				case TMPSTARTSEG: // Keep segment marks if needed
 					if ( params.markSegments ) sb.append(STARTSEG);
 					break;
 				case TMPENDSEG: // Keep segment marks if needed
 					if ( params.markSegments ) sb.append(ENDSEG);
 					break;					
 				default: // Do not keep other characters
 					break;
 			}
 		}
 		TextContainer cnt = tu.getTarget(targetLocale);
 		cnt.setCodedText(sb.toString());
 	}	
 	
 	/**
 	 * Merges back segments into a single content, while optionally adding
 	 * brackets to denote the segments. If the unit is not segmented, brackets
 	 * are added if required. 
 	 * @param container The TextContainer object to un-segment.
 	 */
 	private void mergeSegments (TextContainer container) {
 		// Set variables
 		StringBuilder text = new StringBuilder(container.getCodedText());
 		char start = STARTSEG;
 		char end = ENDSEG;
 		if ( params.type == Parameters.TYPE_KEEPINLINE ) {
 			// Use temporary marks if we need to remove the text after
 			// This way '[' and ']' is real text get removed too
 			start = TMPSTARTSEG;
 			end = TMPENDSEG;
 		}
 		
 		if ( !container.isSegmented() ) {
 			if ( params.markSegments ) {
 				container.setCodedText(start+text.toString()+end);
 			}
 			return;
 		}
 		
 		// Add the markers if needed
 		if ( params.markSegments ) {
 			// Insert the segment marks if requested
 			for ( int i=0; i<text.length(); i++ ) {
 				switch ( text.charAt(i) ) {
 				case TextContainer.MARKER_OPENING:
 				case TextContainer.MARKER_CLOSING:
 				case TextContainer.MARKER_ISOLATED:
 					i++; // Normal skip
 					break;
 				case TextContainer.MARKER_SEGMENT:
 					text.insert(i, start);
 					i += 3; // The bracket, the marker, the code index
 					text.insert(i, end);
 					// Next i increment will skip over the closing mark
 					break;
 				}
 			}
 			// Replace the original coded text by the one with markers
 			container.setCodedText(text.toString());
 		}
 		// Merge all segments
 		container.mergeAllSegments();
 	}
 
 	/**
 	 * Replaces letters with Xs and digits with Ns.
 	 * @param tu the text unit to process.
 	 */
 	private void replaceWithXN (TextUnit tu) {
 		String tmp = null;
 		try {
 			tmp = tu.getTarget(targetLocale).getCodedText().replaceAll("\\p{Lu}|\\p{Lo}", "X");
 			tmp = tmp.replaceAll("\\p{Ll}", "x");
 			tmp = tmp.replaceAll("\\d", "N");
 			TextContainer cnt = tu.getTarget(targetLocale); 
 			cnt.setCodedText(tmp, tu.getTargetContent(targetLocale).getCodes(), false);
 		}
 		catch ( Throwable e ) {
 			logger.log(Level.WARNING,
 				String.format("Error when updating content: '%s'", tmp.toString()), e);
 		}
 	}
 	
 	private void replaceWithExtendedChars (TextUnit tu) {
 		StringBuilder tmp = new StringBuilder();
 		try {
 			tmp.append(tu.getTarget(targetLocale).getCodedText());
 			int n;
 			for ( int i=0; i<tmp.length(); i++ ) {
 				switch ( tmp.charAt(i) ) {
 				case TextContainer.MARKER_OPENING:
 				case TextContainer.MARKER_CLOSING:
 				case TextContainer.MARKER_ISOLATED:
 				case TextContainer.MARKER_SEGMENT:
 					i++; // Normal skip
 					break;
 				default:
 					if ( (n = OLDCHARS.indexOf(tmp.charAt(i))) > -1 ) {
 						tmp.setCharAt(i, NEWCHARS.charAt(n));
 					}
 					break;
 				}
 			}
 			TextContainer cnt = tu.getTarget(targetLocale); 
 			cnt.setCodedText(tmp.toString(), tu.getTargetContent(targetLocale).getCodes(), false);
 		}
 		catch ( Throwable e ) {
 			logger.log(Level.WARNING,
 				String.format("Error when updating content: '%s'", tmp.toString()), e);
 		}
 	}
 	
 	/**
 	 * Adds prefix and/or suffix to the target. This method assumes that
 	 * the item has gone through the first transformation already.
 	 * @param tu The text unit to process.
 	 */
 	private void addText (TextUnit tu) {
 		String tmp = null;
 		try {
 			// Use the target as the text to change.
 			tmp = tu.getTarget(targetLocale).getCodedText();
 			if ( params.addPrefix ) {
 				tmp = params.prefix + tmp;
 			}
 			if ( params.addName ) {
 				String name = tu.getName();
 				if (( name != null ) && ( name.length() > 0 )) tmp += "_"+name;
 				else tmp += "_"+tu.getId();
 			}
 			if ( params.addID ) {
 				tmp += "_"+tu.getId();
 			}
 			if ( params.addSuffix ) {
 				tmp += params.suffix;
 			}
 			TextContainer cnt = tu.getTarget(targetLocale); 
 			cnt.setCodedText(tmp, tu.getTargetContent(targetLocale).getCodes(), false);
 		}
 		catch ( Throwable e ) {
 			logger.log(Level.WARNING,
 				String.format("Error when adding text to '%s'", tmp), e);
 		}
 	}
 
 }
