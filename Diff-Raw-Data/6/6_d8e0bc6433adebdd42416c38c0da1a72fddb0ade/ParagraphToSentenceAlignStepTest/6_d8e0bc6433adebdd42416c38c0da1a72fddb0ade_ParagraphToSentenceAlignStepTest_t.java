 package net.sf.okapi.steps.gcaligner;
 
 import java.net.URISyntaxException;
 import java.net.URL;
 import net.sf.okapi.common.FileCompare;
 import net.sf.okapi.common.LocaleId;
 import net.sf.okapi.common.filters.FilterConfigurationMapper;
 import net.sf.okapi.common.pipeline.Pipeline;
 import net.sf.okapi.common.resource.RawDocument;
 import net.sf.okapi.filters.tmx.TmxFilter;
 import net.sf.okapi.steps.common.RawDocumentToFilterEventsStep;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 public class ParagraphToSentenceAlignStepTest {
 	private Pipeline pipeline;
 	private SentenceAlignerStep aligner;
 	private LocaleId sourceLocale = new LocaleId("EN-US");
 	private LocaleId targetLocale = new LocaleId("PT-BR");
 
 	@Before
 	public void setUp() throws Exception {
 		// create pipeline
 		pipeline = new Pipeline();
 
 		// add filter step
 		TmxFilter filter = new TmxFilter();
 		
 		pipeline.addStep(new RawDocumentToFilterEventsStep(filter));
 
 		// add aligner step
 		aligner = new SentenceAlignerStep();
 
 		Parameters p = new Parameters();
 		p.setGenerateTMX(true);
 		aligner.setParameters(p);
 		FilterConfigurationMapper fcMapper = new FilterConfigurationMapper();
 		fcMapper.addConfigurations("net.sf.okapi.filters.plaintext.PlainTextFilter");
 		pipeline.addStep(aligner);
 
 	}
 
 	@After
 	public void tearDown() throws Exception {
 		pipeline.destroy();
 	}
 
 	@Test
 	public void smallTest() throws URISyntaxException {
 		splitParagraphAlignment("/smallParagraph.tmx");
 	}
 	
 /*
 	@Test
 	public void mediumTest() throws URISyntaxException {
 		splitParagraphAlignment("/fullParagraph.tmx");
 	}
 */
 	public void splitParagraphAlignment(String initialTmx) {
 		URL url = this.getClass().getResource(initialTmx);
 		String sPath = url.getPath();
 		String sOutputPath = sPath+".out";
 		String sGoldPath = sPath+".gold";
 
 		aligner.setSourceLocale(sourceLocale);
   	aligner.setTargetLocale(targetLocale);
 
 		Parameters p = (net.sf.okapi.steps.gcaligner.Parameters) aligner.getParameters();
 		p.setTmxPath(sOutputPath);
 		p.setGenerateTMX(true);
 		aligner.setParameters(p);
 		
 		pipeline.startBatch();
 
 		pipeline.process(new RawDocument(this.getClass().getResourceAsStream(initialTmx),
 				"UTF-8", sourceLocale, targetLocale));
 
 		pipeline.endBatch();
 
		// Test we observed the correct events
		// (per line to avoid line-break differences on each platform)
 		FileCompare fc = new FileCompare();
		assert(fc.compareFilesPerLines(sOutputPath, sGoldPath, "UTF-8"));
 	}
 }
