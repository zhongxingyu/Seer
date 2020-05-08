 package net.fluxo.updater.processor;
 
 import net.fluxo.updater.dbo.KMLSchema;
 import net.fluxo.updater.dbo.KMLSchemaField;
 import org.apache.log4j.Logger;
 
 import javax.xml.stream.XMLEventReader;
 import javax.xml.stream.XMLInputFactory;
 import javax.xml.stream.XMLStreamException;
 import javax.xml.stream.events.Characters;
 import javax.xml.stream.events.XMLEvent;
 import java.io.InputStream;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Ronald Kurniawan (viper)
  * Date: 14/07/13
  * Time: 12:51 PM
  */
 public class KMLReader {
 
 	private XMLInputFactory _inputFactory;
 	private XMLEventReader _eventReader;
 	private Logger _logger = Logger.getLogger("net.fluxo");
 
 	public KMLReader(InputStream input) throws XMLStreamException {
 		_inputFactory = XMLInputFactory.newInstance();
 		_eventReader = _inputFactory.createXMLEventReader(input);
 
 		KMLSchema schema = readKMLSchema();
 		// DEBUG
 		if (schema != null) {
 			System.out.println("name: " + schema.getName());
 			System.out.println("parent: " + schema.getParent());
 			for (KMLSchemaField field : schema.getSchema()) {
 				System.out.println("field: " + field.getName() + "->" + field.getType());
 			}
 		}
 	}
 
 	public KMLSchema readKMLSchema() throws XMLStreamException {
 		boolean insideDocument = false;
 		boolean insideSchema = false;
 		boolean insideSimpleField = false;
 		KMLSchema kmlSchema = null;
 		String sfName = null;
 		String sfType = null;
 
 		while (_eventReader.hasNext()) {
 			XMLEvent xEvent = _eventReader.nextEvent();
 			if (xEvent.isStartElement()) {
 				String localPart = xEvent.asStartElement().getName().getLocalPart();
 				if (localPart.equalsIgnoreCase("Document")) {
 					insideDocument = true;
 				} else if (localPart.equalsIgnoreCase("Schema")) {
 					insideSchema = true;
 					kmlSchema = new KMLSchema();
 				} else if (localPart.equalsIgnoreCase("SimpleField")) {
 					insideSimpleField = true;
				} else if (localPart.equalsIgnoreCase("name") && insideDocument && insideSchema) {
 					xEvent = _eventReader.nextEvent();
 					String data = (xEvent instanceof Characters ? xEvent.asCharacters().getData() : null);
 					if (data != null && kmlSchema != null) {
 						kmlSchema.setName(data);
 					}
 				} else if (localPart.equalsIgnoreCase("name") && insideDocument && insideSchema && insideSimpleField) {
 					xEvent = _eventReader.nextEvent();
 					String data = (xEvent instanceof Characters ? xEvent.asCharacters().getData() : null);
 					if (data != null && kmlSchema != null) {
 						sfName = data;
 					}
				} else if (localPart.equalsIgnoreCase("parent") && insideDocument && insideSchema) {
 					xEvent = _eventReader.nextEvent();
 					String data = (xEvent instanceof Characters ? xEvent.asCharacters().getData() : null);
 					if (data != null && kmlSchema != null) {
 						kmlSchema.setParent(data);
 					}
 				} else if (localPart.equalsIgnoreCase("type") && insideDocument && insideSchema && insideSimpleField) {
 					xEvent = _eventReader.nextEvent();
 					String data = (xEvent instanceof Characters ? xEvent.asCharacters().getData() : null);
 					if (data != null && kmlSchema != null) {
 						sfType = data;
 					}
 				}
 			} else if (xEvent.isEndElement()) {
 				String localPart = xEvent.asEndElement().getName().getLocalPart();
 				if (localPart.equalsIgnoreCase("Document")) {
 					insideDocument = false;
 				} else if (localPart.equalsIgnoreCase("Schema")) {
 					insideSchema = false;
 					break;
 				} else if (localPart.equalsIgnoreCase("SimpleField")) {
 					insideSimpleField = false;
 					if (sfName != null && sfType != null) {
 						KMLSchemaField field = new KMLSchemaField(sfName, sfType);
 						kmlSchema.addSchema(field);
 						sfName = sfType = null;
 					}
 				}
 			}
 		}
 		return kmlSchema;
 	}
 }
