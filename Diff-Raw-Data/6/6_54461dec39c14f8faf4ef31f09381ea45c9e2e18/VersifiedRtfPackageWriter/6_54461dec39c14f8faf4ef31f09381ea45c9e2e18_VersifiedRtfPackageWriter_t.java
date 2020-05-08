 /*===========================================================================
   Copyright (C) 2011 by the Okapi Framework contributors
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
 
 package net.sf.okapi.steps.rainbowkit.versified;
 
 import java.io.File;
 
 import net.sf.okapi.common.filters.IFilter;
 import net.sf.okapi.common.resource.RawDocument;
 import net.sf.okapi.filters.rainbowkit.Manifest;
 import net.sf.okapi.filters.rainbowkit.MergingInfo;
 import net.sf.okapi.filters.versifiedtxt.VersifiedTextFilter;
 import net.sf.okapi.steps.rainbowkit.rtf.RTFLayerWriter;
 
 public class VersifiedRtfPackageWriter extends VersifiedPackageWriter {
 
 	public VersifiedRtfPackageWriter() {
 		super();
 		extractionType = Manifest.EXTRACTIONTYPE_VERSIFIED_RTF;
 	}
 
 	@Override
 	protected void processEndBatchItem() {
 		// Finish the Versified output
 		super.processEndBatchItem();
 
 		// The Versified output is done.
 		// Now re-write it with the RTF layer.
 		RTFLayerWriter layerWriter = null;
 		IFilter filter = null;
 		File inpFile = null;
 		try {
 			// Prepare the output in RTF from the temporary Versified file
 			MergingInfo info = manifest.getItem(docId);
			
			if ( info.getExtractionType().equals(Manifest.EXTRACTIONTYPE_NONE) ) {
				// This file is not to be extracted
				return;
			}
			
 			inpFile = new File(manifest.getSourceDirectory() + info.getRelativeInputPath() + ".vrsz");
 			String outPath = inpFile.getAbsolutePath() + ".rtf";
 			RawDocument rd = new RawDocument(inpFile.toURI(), "UTF-8", manifest.getSourceLocale());
 			rd.setTargetLocale(manifest.getTargetLocale());
 
 			// Create the Versified filter and open the Versified file
 			filter = new VersifiedTextFilter();
 			filter.open(rd);
 
 			// Prepare the layer writer
 			layerWriter = new RTFLayerWriter(filter.createSkeletonWriter(), outPath,
 					manifest.getTargetLocale(), info.getTargetEncoding());
 
 			// Process the file
 			while (filter.hasNext()) {
 				layerWriter.writeEvent(filter.next());
 			}
 		} finally {
 			if (filter != null)
 				filter.close();
 			if (layerWriter != null)
 				layerWriter.close();
 			if (inpFile != null) {
 				inpFile.delete();
 			}
 		}
 	}
 }
