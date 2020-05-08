 package net.sf.okapi.common.pipeline.tests;
 
 import java.io.ByteArrayOutputStream;
 import java.io.InputStream;
 import java.io.UnsupportedEncodingException;
 import java.net.URISyntaxException;
 import java.net.URL;
 
 import net.sf.okapi.common.filters.IFilter;
 import net.sf.okapi.common.filterwriter.IFilterWriter;
 import net.sf.okapi.common.pipeline.FilterPipelineStepAdaptor;
 import net.sf.okapi.common.pipeline.FilterWriterPipelineStepAdaptor;
 import net.sf.okapi.common.pipeline.IPipeline;
 import net.sf.okapi.common.pipeline.IPipelineStep;
 import net.sf.okapi.common.pipeline.Pipeline;
 import net.sf.okapi.common.resource.RawDocument;
 import net.sf.okapi.filters.xml.XMLFilter;
 
 import static org.junit.Assert.*;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 public class XsltPipelineTest {
 
 	@Before
 	public void setUp() throws Exception {
 	}
 
 	@After
 	public void tearDown() throws Exception {
 	}
 
 	@Test
 	public void runXsltPipeline() throws URISyntaxException,
 			UnsupportedEncodingException {
 		IPipeline pipeline = new Pipeline();
 
 		// input resource
 		URL inputXml = XsltPipelineTest.class.getResource("test.xml");
 
 		// make copy of input
 		InputStream in = XsltPipelineTest.class.getResourceAsStream("identity.xsl");
 		pipeline.addStep(new XsltTransformStep(in));
 
 		// remove b tags from input
 		in = XsltPipelineTest.class.getResourceAsStream("remove_b_tags.xsl");
 		pipeline.addStep(new XsltTransformStep(in));
 
 		// filtering step - converts resource to events
 		IFilter filter = new XMLFilter();
 		IPipelineStep filterStep = new FilterPipelineStepAdaptor(filter);
 		pipeline.addStep(filterStep);
 
 		// writer step - converts events to a resource
 		IFilterWriter writer = filter.createFilterWriter();
 		writer.setOptions("en", "UTF-8");
 		pipeline.addStep(new FilterWriterPipelineStepAdaptor(writer));
 
 		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
 		writer.setOutput(outStream);
 
 		pipeline.process(new RawDocument(inputXml.toURI(), "UTF-8", "en"));
 
 		assertEquals(
				"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n<start fileID=\"02286_000_000\"><para id=\"1\">This is a test with .</para></start>".replaceAll("\\r\\n", "\n"),
				new String(outStream.toByteArray(), "UTF-8").replaceAll("\\r\\n", "\n"));
 
 		pipeline.destroy();
 	}
 }
