 /**
  * 
  */
 package org.jasig.portal.web.skin;
 
 import java.io.File;
 
 import org.apache.commons.io.FileUtils;
 import org.junit.After;
 import org.junit.Assert;
 import org.junit.Test;
 import org.springframework.core.io.ClassPathResource;
 
 /**
  * Test harness for {@link ResourcesAggregatorImpl}.
  * 
  * @author Nicholas Blair, npblair@wisc.edu
  *
  */
 public class ResourcesAggregatorImplTest {
 
 	@Test
 	public void testControl() throws Exception {
 		String tempPath = getTestOutputRoot() + "/skin-test1";
 
 		File outputDirectory = new File(tempPath);
 		outputDirectory.mkdirs();
 		Assert.assertTrue(outputDirectory.exists());
 
 		File skinXml = new ClassPathResource("skin-test1/skin.xml").getFile();
 		Assert.assertTrue(skinXml.exists());
 
 		ResourcesAggregatorImpl impl = new ResourcesAggregatorImpl();
 		Resources result = impl.aggregate(skinXml, outputDirectory);
 		Assert.assertEquals(1, result.getCss().size());
 		Assert.assertEquals(1, result.getJs().size());
 		Css aggrCss = result.getCss().get(0);
 		Assert.assertTrue(aggrCss.getValue().startsWith("uportal3_aggr1"));
 		Js aggrJs = result.getJs().get(0);
 		Assert.assertTrue(aggrJs.getValue().startsWith("uportal3_aggr2"));
 
 		File outputCss = new File(outputDirectory, aggrCss.getValue());
 		Assert.assertTrue(outputCss.exists());
 		String outputCssContent = FileUtils.readFileToString(outputCss);
 		Assert.assertEquals(".selector{color:red;}.otherselector{color:green;}", outputCssContent);
 		
 		File outputJs = new File(outputDirectory, aggrJs.getValue());
 		Assert.assertTrue(outputJs.exists());
 	}
 
 	/**
 	 * 
 	 * @throws Exception
 	 */
 	@Test
 	public void testAllAbsolute() throws Exception {
 		String tempPath = getTestOutputRoot() + "/skin-testAllAbsolute";
 
 		File outputDirectory = new File(tempPath);
 		outputDirectory.mkdirs();
 		Assert.assertTrue(outputDirectory.exists());
 
 		File skinXml = new ClassPathResource("skin-testAllAbsolute/skin.xml").getFile();
 		Assert.assertTrue(skinXml.exists());
 
 		ResourcesAggregatorImpl impl = new ResourcesAggregatorImpl();
 
 		Resources result = impl.aggregate(skinXml, outputDirectory);
 		Assert.assertEquals(2, result.getCss().size());
 		Assert.assertEquals(2, result.getJs().size());
 		Assert.assertEquals("/a.css", result.getCss().get(0).getValue());
 		Assert.assertEquals("/b.css", result.getCss().get(1).getValue());
 		Assert.assertEquals("/a.js", result.getJs().get(0).getValue());
 		Assert.assertEquals("/b.js", result.getJs().get(1).getValue());
 
 		// there should only be one output file - the skin-aggr.xml 
 		Assert.assertEquals(1, outputDirectory.list().length);
 		Assert.assertEquals("uportal3_aggr.skin.xml", outputDirectory.list()[0]);
 	}
 	
 	/**
 	 * 
 	 * @throws Exception
 	 */
 	@Test
 	public void testComplex() throws Exception {
 		String tempPath = getTestOutputRoot() + "/skin-complex/superskin";
 		
 		File outputDirectory = new File(tempPath);
 		outputDirectory.mkdirs();
 		Assert.assertTrue(outputDirectory.exists());
 
 		File skinXml = new ClassPathResource("skin-complex/superskin/skin.xml").getFile();
 		Assert.assertTrue(skinXml.exists());
 
 		ResourcesAggregatorImpl impl = new ResourcesAggregatorImpl();
 		Resources result = impl.aggregate(skinXml, outputDirectory);
 		
 		Assert.assertEquals(5, result.getCss().size());
 		// 1st element was aggregated
 		Assert.assertTrue(result.getCss().get(0).getValue().startsWith("../common/css/"));
 		Assert.assertTrue(result.getCss().get(0).getValue().contains("uportal3_aggr"));
 		
 		// 2nd is conditional
 		Assert.assertTrue(result.getCss().get(1).isConditional());
 		Assert.assertEquals("condition!", result.getCss().get(1).getConditional());
 		
 		// 3rd and 4th are absolutes
 		Assert.assertTrue(result.getCss().get(2).isAbsolute());
 		Assert.assertEquals("/ResourceServingWebapp/rs/1.css", result.getCss().get(2).getValue());
 		Assert.assertTrue(result.getCss().get(3).isAbsolute());
 		Assert.assertEquals("/ResourceServingWebapp/rs/2.css", result.getCss().get(3).getValue());
 		
 		// 5th element was aggregated and has media set
 		Assert.assertTrue(result.getCss().get(4).getValue().startsWith("css/"));
 		Assert.assertTrue(result.getCss().get(4).getValue().contains("uportal3_aggr"));
 		Assert.assertEquals("alternate", result.getCss().get(4).getMedia());
 		
 		Assert.assertEquals(4, result.getJs().size());
 		// 1st aggregated
 		Assert.assertTrue(result.getJs().get(0).getValue().startsWith("../common/js/"));
 		Assert.assertTrue(result.getJs().get(0).getValue().contains("uportal3_aggr"));
 		// 2nd absolute
 		Assert.assertTrue(result.getJs().get(1).isAbsolute());
 		Assert.assertEquals("/universal.js", result.getJs().get(1).getValue());
 		// 3rd aggregated with conditional
 		Assert.assertTrue(result.getJs().get(2).getValue().startsWith("js/"));
 		Assert.assertTrue(result.getJs().get(2).getValue().contains("uportal3_aggr"));
 		// 4th is compressed
 		Assert.assertTrue(result.getJs().get(3).getValue().contains("js/c-compressed.js"));
 		Assert.assertTrue(result.getJs().get(3).isCompressed());
 	}
 	
 
 	/**
 	 * Delete our temp directory after test execution.
 	 * @throws Exception
 	 */
 	@After
 	public void cleanupTempDir() throws Exception {
 		File testOutputDirectory = new File(getTestOutputRoot());
 		FileUtils.forceDelete(testOutputDirectory);
 	}
 
 	/**
 	 * Shortcut to get a temporary directory underneath java.io.tmpdir.
	 * Includes special handling for Mac OS X JVM.
 	 * 
 	 * @return
 	 */
 	private String getTestOutputRoot() {
 		String tempPath = System.getProperty("java.io.tmpdir");
		// Mac JVM java.io.tmpdir is odd, replace it with /tmp
		if (tempPath.startsWith("/var/folders/")) {
			tempPath = "/tmp/";
 		}

 		tempPath = tempPath + "resources-aggregator-impl-test-output";
 		return tempPath;
 	}
 }
