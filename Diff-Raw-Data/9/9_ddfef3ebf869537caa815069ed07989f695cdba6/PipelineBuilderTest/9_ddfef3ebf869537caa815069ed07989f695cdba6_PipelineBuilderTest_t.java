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
 
 package net.sf.okapi.common.pipeline.integration;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNotNull;
 
 import java.io.File;
 
 import org.junit.Test;
 
 import net.sf.okapi.common.LocaleId;
 import net.sf.okapi.common.pipelinedriver.PipelineDriver;
 import net.sf.okapi.lib.extra.pipelinebuilder.XBatch;
 import net.sf.okapi.lib.extra.pipelinebuilder.XBatchItem;
 import net.sf.okapi.lib.extra.pipelinebuilder.XPipeline;
 import net.sf.okapi.lib.extra.pipelinebuilder.XPipelineType;
 import net.sf.okapi.steps.common.FilterEventsWriterStep;
 import net.sf.okapi.steps.common.RawDocumentToFilterEventsStep;
import net.sf.okapi.steps.sentencealigner.SentenceAlignerStep;
 
 
 public class PipelineBuilderTest {
 	
 	@Test
 	public void test() {
 		
 	}
 	
 	// DEBUG @Test
 //	public void testPipeline() {
 //		
 //		//----------------------------------------------------------------
 //		XPipeline p1 = 
 //			new XPipeline(					
 //					"Pipeline template example.",
 //					new RawDocumentToFilterEventsStep(),
 //					new TextModificationStep(),
 //					new SearchAndReplaceStep(),
 //					new WordCountStep(),
 //					new FilterEventsWriterStep());
 //		
 //		//----------------------------------------------------------------
 //		XPipeline p2 = 
 //			new XPipeline(					
 //					"Pipeline initialized to process 2 input documents with TextModificationStep.",
 //					new XBatch(
 //							new XBatchItem(
 //								(new File("input1.html")).toURI(),
 //								"UTF-8",
 //								"okf_html",
 //								(new File("output1.html")).toURI(),
 //								"UTF-8",
 //								LocaleId.ENGLISH,
 //								LocaleId.FRENCH),
 //								
 //							new XBatchItem(
 //								(new File("input2.html")).toURI(),
 //								"UTF-8",
 //								"okf_html",
 //								(new File("output2.html")).toURI(),
 //								"UTF-16",
 //								LocaleId.ENGLISH,
 //								LocaleId.CHINA_CHINESE)),
 //								
 //					new RawDocumentToFilterEventsStep(),
 //					new TextModificationStep(),
 //					new FilterEventsWriterStep());
 //		
 //		//----------------------------------------------------------------		
 //		XPipeline p3 = 
 //			new XPipeline(					
 //					"Pipeline initialized to process a batch defined in another pipeline. " +
 //					"TextModificationStep is created and initialized wit parameters from an external resource. " +
 //					"The p1 pipeline is inserted as a step, and a parallel pipeline with 2 steps inserted as a step.",
 //					p1.getBatch(),
 //					new RawDocumentToFilterEventsStep(),
 //					new XPipelineStep(
 //							TextModificationStep.class,
 //							this.getClass().getResource("test.txt"), 
 //							true),
 //					p1,
 //					new XPipeline(
 //							"Parallel pipeline with SearchAndReplaceStep and WordCountStep steps.",
 //							XPipelineType.PARALLEL,
 //							new SearchAndReplaceStep(),
 //							new WordCountStep()),
 //					new FilterEventsWriterStep());
 //		
 //		//----------------------------------------------------------------				
 //		XPipeline p4 = 
 //			new XPipeline(
 //					"Pipeline initialized to process nested batches.",
 //					new XBatch(
 //							// bic 1 and bic 2
 //							p2.getBatch(),
 //							
 //							// bic 3 with 1 document
 //							new XBatchItem(									
 //									(new File("input3.html")).toURI(),
 //									"UTF-8",
 //									"okf_html",
 //									(new File("output3.html")).toURI(),
 //									"UTF-8",
 //									LocaleId.ENGLISH,
 //									LocaleId.FRENCH),
 //							
 //							// bic 4 with 2 documents	
 //							new XBatchItem(
 //									new XDocument(
 //											(new File("input4.html")).toURI(),
 //											"UTF-8",
 //											"okf_html",
 //											(new File("output4.html")).toURI(),
 //											"UTF-8",
 //											LocaleId.ENGLISH,
 //											LocaleId.FRENCH),
 //									new XDocument(
 //											(new File("input5.html")).toURI(),
 //											"UTF-8",
 //											"okf_html",
 //											(new File("output5.html")).toURI(),
 //											"UTF-8",
 //											LocaleId.ENGLISH,
 //											LocaleId.FRENCH)),
 //							// bic 5
 //							new XBatchItem(
 //									(new File("input6.html")).toURI(),
 //									"UTF-8",
 //									"okf_html",
 //									(new File("output6.html")).toURI(),
 //									"UTF-16",
 //									LocaleId.ENGLISH,
 //									LocaleId.CHINA_CHINESE),
 //							
 //							// bic 6
 //							new XBatchItem(
 //									(new File("input7.html")).toURI(),
 //									"UTF-8",
 //									"okf_html",
 //									(new File("output7.html")).toURI(),
 //									"UTF-16",
 //									LocaleId.ENGLISH,
 //									LocaleId.CHINA_CHINESE)),
 //								
 //					new RawDocumentToFilterEventsStep(),
 //					new TextModificationStep(),
 //					new FilterEventsWriterStep());
 //		
 //		//----------------------------------------------------------------
 //		XPipeline p5 =
 //			new XPipeline(
 //					"Alignment pipeline. Source and target documents are processed by separate " +
 //					"pipelines connected in parallel. Events from both pipelines are anilized in TestAlignerStep.",
 //					new XPipeline(
 //							"Parallel pipeline for parrallel handling of source and target documents.",
 //							XPipelineType.PARALLEL,
 //							
 //							new XPipeline(
 //									"Source document translatable text extraction",
 //									new XBatch(
 //											new XBatchItem(
 //													(new File("source.html")).toURI(),
 //													 "UTF-8",
 //													 LocaleId.ENGLISH)),
 //									new RawDocumentToFilterEventsStep()),
 //									
 //							new XPipeline(
 //									"Target document translatable text extraction",
 //									new XBatch(
 //											new XBatchItem(
 //													(new File("target.doc")).toURI(),
 //													 "UTF-16",
 //													 LocaleId.CHINA_CHINESE)),
 //									new RawDocumentToFilterEventsStep())),
 //									
 //					new SentenceAlignerStep(),
 //					
 //					new FilterEventsWriterStep());
 //					
 //		//----------------------------------------------------------------
 //		XPipeline p6 =
 //			new XPipeline(
 //					"Test pipeline with step parameters",
 //					new XBatch(
 //							new XBatchItem(
 //									this.getClass().getResource("test.txt"),
 //									"UTF-8",
 //									LocaleId.ENGLISH,
 //									LocaleId.FRENCH)),
 //									
 //					new RawDocumentToFilterEventsStep(),
 //					
 //					new XPipelineStep(new LeveragingStep(), 
 //							new XParameter("resourceClassName", net.sf.okapi.connectors.google.GoogleMTConnector.class.getName()),
 //							new XParameter("threshold", 80),
 //							new XParameter("fillTarget", true)),
 //							
 //					new XPipelineStep(TextModificationStep.class, 
 //							new XParameter("type", 0),
 //							new XParameter("addPrefix", true),
 //							new XParameter("prefix", "{START_"),
 //							new XParameter("addSuffix", true),
 //							new XParameter("suffix", "_END}"),
 //							new XParameter("applyToExistingTarget", false),
 //							new XParameter("addName", false),
 //							new XParameter("addID", true),
 //							new XParameter("markSegments", false)),
 //							
 //					new XLIFFKitWriterStep());
 //		
 //		// DEBUG
 ////		p1.execute();
 ////		p2.execute();
 ////		p3.execute();
 ////		p4.execute();
 ////		p5.execute();
 ////		p6.execute();
 //		
 //		// Tests of PipelineDriver.setPipeline()
 //		PipelineDriver pd = new PipelineDriver();
 //		assertNotNull(pd.getPipeline());
 //		assertEquals(0, pd.getPipeline().getSteps().size());
 //		
 //		pd.setPipeline(null);
 //		assertNull(pd.getPipeline());
 //		
 //		pd.setPipeline(p3);
 //		assertNotNull(pd.getPipeline());
 //		assertEquals(5, pd.getPipeline().getSteps().size());
 //		
 //		pd.setPipeline(p4);
 //		assertNotNull(pd.getPipeline());
 //		assertEquals(3, pd.getPipeline().getSteps().size());
 //		
 //		pd.setPipeline(p5);
 //		assertNotNull(pd.getPipeline());
 //		assertEquals(3, pd.getPipeline().getSteps().size());
 //		
 //		pd.setPipeline(p6);
 //		assertNotNull(pd.getPipeline());
 //		assertEquals(4, pd.getPipeline().getSteps().size());
 //	}
 
 	// DEBUG 
 	@Test
 	public void testParallelPipeline() {
 		XPipeline p5 =
 			new XPipeline(
 					"Alignment pipeline. Source and target documents are processed by separate " +
 					"pipelines connected in parallel. Events from both pipelines are anilized in TestAlignerStep.",
 					new XPipeline(
 							"Parallel pipeline for parrallel handling of source and target documents.",
 							XPipelineType.PARALLEL,
 							
 							new XPipeline(
 									"Source document translatable text extraction",
 									new XBatch(
 											new XBatchItem(
 													(new File("source.html")).toURI(),
 													 "UTF-8",
 													 LocaleId.ENGLISH)),
 									new RawDocumentToFilterEventsStep()),
 									
 							new XPipeline(
 									"Target document translatable text extraction",
 									new XBatch(
 											new XBatchItem(
 													(new File("target.doc")).toURI(),
 													 "UTF-16",
 													 LocaleId.CHINA_CHINESE)),
 									new RawDocumentToFilterEventsStep())),
 									
 					new SentenceAlignerStep(),
 					
 					new FilterEventsWriterStep());
 
 		PipelineDriver pd = new PipelineDriver();
 		pd.setPipeline(p5);
 		assertNotNull(pd.getPipeline());
 		assertEquals(3, pd.getPipeline().getSteps().size());
 		
 		p5.execute();
 	}		
 }
