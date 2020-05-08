 package algorithms.params;
 
 import java.io.FileNotFoundException;
 
 import javax.xml.stream.FactoryConfigurationError;
 import javax.xml.stream.XMLStreamException;
 
 import org.junit.Assert;
 import org.junit.Test;
 
 public class XMLProtocolInfoTests {
 		
 	@Test
	public void parseXML_allArgsTest() throws FileNotFoundException, XMLStreamException, FactoryConfigurationError {
 		Assert.assertNotNull("res is not in the classpath - ask Daniel", getClass().getClassLoader().getResource("protInfo.xml"));
 		ProtocolInfo protInfo = new XMLProtocolInfo(getClass().getClassLoader().getResource("protInfo.xml").getFile());
 		Assert.assertEquals("", protInfo.getVersion());
 		Assert.assertEquals("MyDemo", protInfo.getSessionId());
 		Assert.assertEquals(3, protInfo.getNumOfParties());
 		Assert.assertEquals(2, protInfo.getThreshold());
 		Assert.assertEquals(256, protInfo.getNe());
 		Assert.assertEquals(100, protInfo.getNr());
 		Assert.assertEquals(256, protInfo.getNv());
 		Assert.assertEquals("SHA-256", protInfo.getHashFunction());
 		Assert.assertEquals("SHA-256", protInfo.getPrg());
 		Assert.assertEquals("ECqPGroup(P-256)::0000000002010000001c766572696669636174756d2e61726974686d2e4543715047726f75700100000005502d323536", protInfo.getGq());
 		Assert.assertEquals(1, protInfo.getWidth());
 	}
 	
 	@Test (expected = IllegalXmlFormatException.class)
	public void parseXML_withMissingArgTest() throws FileNotFoundException, XMLStreamException, FactoryConfigurationError {
 		Assert.assertNotNull("res is not in the classpath - ask Daniel", getClass().getClassLoader().getResource("protInfoMissingArg.xml"));
 		new XMLProtocolInfo(getClass().getClassLoader().getResource("protInfoMissingArg.xml").getFile());
 	}
 }
