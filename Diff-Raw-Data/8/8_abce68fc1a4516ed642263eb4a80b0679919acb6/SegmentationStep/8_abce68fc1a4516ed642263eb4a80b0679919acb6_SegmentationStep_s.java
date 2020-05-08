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
 
 package net.sf.okapi.steps.segmentation;
 
 import java.util.logging.Logger;
 
 import net.sf.okapi.common.Event;
 import net.sf.okapi.common.IParameters;
 import net.sf.okapi.common.IResource;
 import net.sf.okapi.common.pipeline.BasePipelineStep;
 import net.sf.okapi.common.resource.TextUnit;
 import net.sf.okapi.lib.segmentation.ISegmenter;
 import net.sf.okapi.lib.segmentation.SRXDocument;
 
 public class SegmentationStep extends BasePipelineStep {
 
 	private final Logger logger = Logger.getLogger(getClass().getName());
 
 	private Parameters params;
 	private ISegmenter srcSeg;
 	private ISegmenter trgSeg;
 	private String trgLang;
 
 	public SegmentationStep () {
 		params = new Parameters();
 		srcSeg = null;
 	}
 	
 	public String getName () {
 		return "SRX Segmentation";
 	}
 
 	public String getDescription () {
 		return "Apply SRX segmentation to a document.";
 	}
 
 	@Override
 	public IParameters getParameters () {
 		return params;
 	}
 
 	@Override
 	protected void handleStartBatch (Event event) {
 		String src = params.sourceSrxPath; //.replace(VAR_PROJDIR, projectDir);
 		String trg = params.targetSrxPath; //.replace(VAR_PROJDIR, projectDir);
 		SRXDocument srxDoc = new SRXDocument();
 		srxDoc.loadRules(src);
 		if ( srxDoc.hasWarning() ) logger.warning(srxDoc.getWarning());
 		srcSeg = srxDoc.compileLanguageRules(getContext().getSourceLanguage(0), null);
 		if ( !src.equals(trg) ) {
 			srxDoc.loadRules(trg);
 			if ( srxDoc.hasWarning() ) logger.warning(srxDoc.getWarning());
 		}
 		trgLang = getContext().getTargetLanguage(0);
 		trgSeg = srxDoc.compileLanguageRules(trgLang, null);
 	}
 	
 	@Override
 	protected void handleTextUnit (Event event) {
 		TextUnit tu = (TextUnit)event.getResource();
 		// Skip non-translatable
 		if ( !tu.isTranslatable() ) return;
 
 		//TODO: Decide what to do with target, is target real target or copy of source?
 		if ( tu.hasTarget(trgLang) ) {
 			trgSeg.computeSegments(tu.getTarget(trgLang));
 			tu.getTarget(trgLang).createSegments(trgSeg.getRanges());
 		}
 		else {
 			srcSeg.computeSegments(tu.getSource());
 			tu.getSource().createSegments(srcSeg.getRanges());
 		}
 		
 		// Make sure we have target content
 		tu.createTarget(trgLang, false, IResource.COPY_ALL);
 	}
 
 }
