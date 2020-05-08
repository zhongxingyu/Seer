 package core.distributor;
 
 import java.io.File;
 import java.util.Properties;
 
 import org.junit.Assert;
 import org.junit.Before;
 import org.junit.Test;
 
 import core.packet.Packet;
 import core.packet.Packets;
 import core.process.write.SimpleXmlFileWriter;
 
 public class XmlFileDistributorTest {
 
 	private String baseDirectory;	
 	private XmlFileDistributor distributor;
 	
 	@Before
 	public void setUp() {
 		baseDirectory = new File(".").getAbsolutePath() + "src" + File.separator + "test" + File.separator + "output";
 		distributor = new XmlFileDistributor(new SimpleXmlFileWriter(baseDirectory));
 	}
 	
 	@Test
 	public void testWriteFile() {
 		String xml = "<persons><person><name>sean</name><age>24</age></person></persons>";
 		Properties properties = new Properties();
 		properties.put("name", "sean");
 		properties.put("age", "24");
 		
 		Packet packet = Packets.getInstance("000", "xml", xml, properties);
 		
 		distributor.distribute(packet);
 		
 		File xmlFile = new File(baseDirectory + File.separator + "000.xml");
 		File propertiesFile = new File(baseDirectory + File.separator + "000.properties");
 		
 		Assert.assertTrue(xmlFile.exists());
 		Assert.assertTrue(propertiesFile.exists());
 		
 		xmlFile.delete();
 		propertiesFile.delete();
 	}
 }
