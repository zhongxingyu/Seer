 package dcll.grp3.morphoQuizz;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
import java.io.InputStreamReader;
 import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
 
 import javax.xml.stream.FactoryConfigurationError;
 import javax.xml.stream.XMLEventReader;
 import javax.xml.stream.XMLEventWriter;
 import javax.xml.stream.XMLInputFactory;
 import javax.xml.stream.XMLOutputFactory;
 import javax.xml.stream.XMLStreamException;
 
 import de.odysseus.staxon.json.JsonXMLConfig;
 import de.odysseus.staxon.json.JsonXMLConfigBuilder;
 import de.odysseus.staxon.json.JsonXMLInputFactory;
 import de.odysseus.staxon.json.JsonXMLOutputFactory;
 import de.odysseus.staxon.xml.util.PrettyXMLEventWriter;
 
 @SuppressWarnings("restriction")
 public class QuizzConverter {
 
 	/**
 	 * Lit le fichier de quizz XML xmlInput, ecrit le test convertit en JSON
 	 * dans le fichier jsonOutput
 	 * 
 	 * @param xmlInput
 	 *            Ficher de Quizz XML
 	 * @param jsonOutput
 	 *            Fichier de sortie JSON
 	 * @throws IOException
 	 * @throws javax.xml.stream.FactoryConfigurationError
 	 * @throws XMLStreamException
 	 */
 	public static void xmlQuizzToJson(File xmlInput, File jsonOutput)
 			throws IOException, XMLStreamException,
 			javax.xml.stream.FactoryConfigurationError {
		
 		InputStream is = new FileInputStream(xmlInput);
 		OutputStream os = new FileOutputStream(jsonOutput);
		
		Reader r = new InputStreamReader(is, "UTF-8");
		Writer w = new OutputStreamWriter(os, "UTF-8");
 
 		JsonXMLConfig config = new JsonXMLConfigBuilder().autoArray(true)
 				.autoPrimitive(true).prettyPrint(true).build();
 		try {
 			/*
 			 * Create reader (XML).
 			 */
 			XMLEventReader reader = XMLInputFactory.newInstance()
					.createXMLEventReader(r);
 
 			/*
 			 * Create writer (JSON).
 			 */
 			XMLEventWriter writer = new JsonXMLOutputFactory(config)
					.createXMLEventWriter(w);
 
 			/*
 			 * Copy events from reader to writer.
 			 */
 			writer.add(reader);
 
 			/*
 			 * Close reader/writer.
 			 */
 			reader.close();
 			writer.close();
 		} finally {
 			/*
 			 * As per StAX specification, XMLEventReader/Writer.close() doesn't
 			 * close the underlying stream.
 			 */
			r.close();
			w.close();
 			os.close();
 			is.close();
 		}
 	}
 
 	/**
 	 * Lit le fichier de quizz json jsonInput, ecrit le test convertit en XML
 	 * dans le fichier xmlOutput
 	 * 
 	 * @param jsonInput
 	 *            Ficher de Quizz Json
 	 * @param xmlOutput
 	 *            Fichier de sortie XML
 	 * @throws IOException
 	 * @throws FactoryConfigurationError 
 	 * @throws XMLStreamException 
 	 */
 	public static void jsonQuizzToXML(File jsonInput, File xmlOutput)
 			throws IOException, XMLStreamException, FactoryConfigurationError {
		
 		InputStream is = new FileInputStream(jsonInput);
 		OutputStream os = new FileOutputStream(xmlOutput);
		
		Reader r = new InputStreamReader(is, "UTF-8");
		Writer w = new OutputStreamWriter(os, "UTF-8");
 
 		JsonXMLConfig config = new JsonXMLConfigBuilder().multiplePI(false).build();
 		try {
 			/*
 			 * Create reader (JSON).
 			 */
			XMLEventReader reader = new JsonXMLInputFactory(config).createXMLEventReader(r);
 			
 			/*
 			 * Create writer (XML).
 			 */
			XMLEventWriter writer = XMLOutputFactory.newInstance().createXMLEventWriter(w);
 			writer = new PrettyXMLEventWriter(writer); // format output
 			
 			/*
 			 * Copy events from reader to writer.
 			 */
 			writer.add(reader);
 			
 			/*
 			 * Close reader/writer.
 			 */
 			reader.close();
 			writer.close();
 		} finally {
 			/*
 			 * As per StAX specification, XMLEventReader/Writer.close() doesn't close
 			 * the underlying stream.
 			 */
			r.close();
			w.close();
 			os.close();
 			is.close();
 		}
 	}
 }
