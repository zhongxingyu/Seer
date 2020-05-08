 package cudl.w3c;
 
 import static org.junit.Assert.assertEquals;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.ArrayList;
 
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerConfigurationException;
 import javax.xml.transform.TransformerException;
 import javax.xml.transform.TransformerFactory;
 import javax.xml.transform.TransformerFactoryConfigurationError;
 import javax.xml.transform.stream.StreamResult;
 import javax.xml.transform.stream.StreamSource;
 
 import org.junit.AfterClass;
 import org.junit.BeforeClass;
 import org.junit.Test;
 import org.mockito.internal.util.reflection.FieldInitializationReport;
 import org.xml.sax.SAXException;
 
 import cudl.Interpreter;
 import cudl.Prompt;
 
 public class InterpreterTestW3C {
 	private static Prompt prompt;
 	private static String url;
 	private Interpreter interpreter;
 	private final static ArrayList<String> filenames = new ArrayList<String>() {
 		{
 			add("a11.txml");
 			add("a12a.txml");// need file to add("a12b.txml");
 			add("a18.txml");
 			add("a19.txml");
 			
 			//goto test
 			add("526.txml");
 			add("527a1.txml");
 			add("528a1.txml");
 			add("529a1.txml");
 			add("530a1.txml");
 			add("531a1.txml");
 			add("532a1.txml");
 			add("533.txml");
 			add("534.txml");
 			
 		}
 	};
 
 	@BeforeClass
 	public static void setUp() throws IOException, TransformerConfigurationException, TransformerFactoryConfigurationError, TransformerException {
 		url =  new File(".").getCanonicalPath() + "/test/docVxml/w3c/";
 		new File(url+ "xslt_output/").mkdirs();
 		
 		for (String name : filenames) {
 			if (name.endsWith(".txml")) {
 				txmlToVxml(name, name.replace(".txml", ".vxml"));
 				System.err.println(name);
 			}
 		}
 		txmlToVxml("a12b.txml", "a12b.vxml");
 		txmlToVxml("526b.txml", "526b.vxml");
 		txmlToVxml("527a2.txml", "527a2.vxml");
 		txmlToVxml("529a2.txml", "529a2.vxml");
 		txmlToVxml("534App.txml", "534App.vxml");
 		txmlToVxml("534Doc.txml", "534Doc.vxml");
 		prompt = new Prompt();
 		prompt.tts = "pass";
 	}
 
 	@Test
 	public void testSimpleTxmlToVxml() throws IOException, ParserConfigurationException, SAXException,
 			TransformerConfigurationException, TransformerFactoryConfigurationError, TransformerException {
 		for (String fileName : filenames) {
 			System.err.println();
 			interpreter = new Interpreter("file://" + url + "xslt_output/" + fileName.replace(".txml", ".vxml"));
 			interpreter.start();
 			if (fileName.equals("a18.txml")) {
 				interpreter.talk("Chicago");
 			}
			System.err.println(interpreter.getLogs());
 			assertEquals(prompt, interpreter.getPrompts().get(interpreter.getPrompts().size() - 1));
 			System.err.println(fileName+"\ttest OK");
 		}
 
 	}
 
 	private static void txmlToVxml(String fromFile, String toFile) throws TransformerFactoryConfigurationError,
 			TransformerConfigurationException, TransformerException, FileNotFoundException {
 		TransformerFactory tFactory = TransformerFactory.newInstance();
 		Transformer transformer = tFactory.newTransformer(new StreamSource(url + "/irtest.xslt"));
 		
 		String outputFileName = (url + "/xslt_output/").replaceAll("file:/*", "/");
 		System.err.println(outputFileName+toFile);
 		transformer.transform(new StreamSource(url + fromFile), new StreamResult(new FileOutputStream(outputFileName + toFile)));
 
 	}
 	
 	@AfterClass
 	public static void afterAllTest() {
 		File f = new File(url + "/xslt_output/");
 		
 		for (String filename : f.list()) {
 			new File(f, filename).deleteOnExit();
 		}
 		System.err.println(f.delete());
 	}
 
 }
