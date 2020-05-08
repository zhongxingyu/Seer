 /**
  * 
  */
 package cytoscape.plugin;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 
 import javax.xml.transform.OutputKeys;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerException;
 import javax.xml.transform.TransformerFactory;
 import javax.xml.transform.stream.StreamResult;
 import javax.xml.transform.stream.StreamSource;
 
 import cytoscape.CytoscapeVersion;
 
 /**
  * @author skillcoy
  *
  */
 public class PluginTestXML {
 
 	
 	private static String testFileDir() {
 		String FS = "/";
		return System.getProperty("user.dir") + FS + "testData" + FS + "plugins" + FS;
 	}
 
 	// get the xsl file as a stream
 	private static  StreamSource getXSL() throws IOException {
 		String XSLFile = testFileDir() + "test_plugins.xsl";
 		InputStream instream = new FileInputStream(  XSLFile );
 
 		if (instream == null || instream.available() == 0) {
 			// throw an error!
 			String Msg = "";
 			if (instream == null)
 				Msg = "input stream is null";
 			else if (instream.available() == 0)
 				Msg = "0 bytes in input stream";
 
 			IOException Error = new IOException(
 					"Unable to load test_plugins.xsl: " + Msg);
 			throw Error;
 		}
 		return new StreamSource(instream);
 	}
 
 	public static File transformXML(String FileName, String FileUrl) throws IOException {
 		StreamSource xsltSource = getXSL();
 		String XmlFile = testFileDir() + FileName;
 		StreamSource testFile = new StreamSource( XmlFile );
 
 		File TempTestFile = File.createTempFile("test_plugins", ".xml");
 		StreamResult resultFile = new StreamResult(TempTestFile.getAbsolutePath());
 
 		TransformerFactory transFact = javax.xml.transform.TransformerFactory
 				.newInstance();
 		try {
 			Transformer trans = transFact.newTransformer(xsltSource);
 			trans.setOutputProperty(OutputKeys.METHOD, "xml");
 			trans.setOutputProperty(OutputKeys.INDENT, "yes");
 			trans.setParameter("cytoscapeVersion", CytoscapeVersion.version);
 			trans.setParameter("fileUrl", FileUrl);
 
 			trans.transform(testFile, resultFile);
 		} catch (TransformerException te) {
 			te.printStackTrace();
 		}
 		return TempTestFile;
 	}
 
 	
 	
 	
 }
