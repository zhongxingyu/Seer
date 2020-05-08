 package dk.statsbiblioteket.newspaper.metadatachecker;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.xml.sax.SAXException;
 import org.xml.sax.SAXParseException;
 
 import dk.statsbiblioteket.medieplatform.autonomous.ResultCollector;
 import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.AttributeParsingEvent;
 import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.NodeBeginsParsingEvent;
 import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.NodeEndParsingEvent;
 import dk.statsbiblioteket.medieplatform.autonomous.iterator.eventhandlers.TreeEventHandler;
 
 import javax.xml.XMLConstants;
 import javax.xml.transform.stream.StreamSource;
 import javax.xml.validation.Schema;
 import javax.xml.validation.SchemaFactory;
 import javax.xml.validation.Validator;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.PrintWriter;
 import java.io.StringWriter;
 import java.net.URL;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Map;
 
 /**
  * Check xml data of known file postfixes against xsd schemas.
  */
 public class SchemaValidatorEventHandler implements TreeEventHandler {
     /** Logger */
     private final Logger log = LoggerFactory.getLogger(getClass());
     /** A map from file postfix to a known schema for that file. */
     private static final Map<String, String> POSTFIX_TO_XSD;
     static {
         Map<String, String> postfixToXsd = new HashMap<>(5);
         postfixToXsd.put(".alto.xml", "alto-v2.0.xsd");
         postfixToXsd.put(".mix.xml", "mix.xsd");
         postfixToXsd.put(".mods.xml", "mods-3-1.xsd");
         postfixToXsd.put(".edition.xml", "mods-3-1.xsd");
         postfixToXsd.put(".film.xml", "film.xsd");
         POSTFIX_TO_XSD = Collections.unmodifiableMap(postfixToXsd);
     }
     /** The result collector results are collected in. */
     private final ResultCollector resultCollector;
     /** A map of parsed schemas for a given schema file name. */
     private Map<String, Schema> schemas = new HashMap<>();
 
     /**
      * Initialise the event handler with the collector to collect results in.
      * @param resultCollector The collector to collect results in.
      */
     public SchemaValidatorEventHandler(ResultCollector resultCollector) {
         log.debug("Initialising {}", getClass().getName());
         this.resultCollector = resultCollector;
     }
 
     @Override
     public void handleNodeBegin(NodeBeginsParsingEvent event) {
         // Do nothing
     }
 
     @Override
     public void handleNodeEnd(NodeEndParsingEvent event) {
         // Do nothing
     }
 
     @Override
     /**
      * For each attribute, if this is a known XML file postfix, check the appropriate schema for that XML file.
      * @event The attribute parsing event that is to be checked.
      */
     public void handleAttribute(AttributeParsingEvent event) {
         for (Map.Entry<String, String> entry : POSTFIX_TO_XSD.entrySet()) {
             if (event.getName().endsWith(entry.getKey())) {
                 checkSchema(event, entry.getValue());
                 break;
             }
         }
     }
 
     /**
      * Given an attribute parsing event and a schema file name, extract the data from the event, and validate it against
      * the schema.
      * @param event The attribute parsing event containing the data.
      * @param schemaFile The file name of the schema to check the data against.
      */
     private void checkSchema(AttributeParsingEvent event, String schemaFile) {
         log.debug("Checking '{}' with schema '{}'", event.getName(), schemaFile);
         try {
             InputStream data = event.getData();
             Validator validator = createValidator(schemaFile);
             validator.validate(new StreamSource(data));
         } catch (SAXParseException e) {
             resultCollector.addFailure(event.getName(), "metadata", "Metadata_checker_component",
                                        "Failure validating XML data from '" + event.getName() + "': Line "
                                                + e.getLineNumber() + " Column " + e.getColumnNumber() + ": "
                                                + e.getMessage());
             log.debug("Error validating '{}' with schema '{}': Line {} Column {}: {}", event.getName(), schemaFile,
                       e.getLineNumber(), e.getColumnNumber(), e.getMessage(), e);
         } catch (SAXException e) {
             resultCollector.addFailure(event.getName(), "metadata", "Metadata_checker_component",
                                        "Failure validating XML data from '" + event.getName() + "': " + e.getMessage());
             log.debug("Error validating '{}' with schema '{}': {}", event.getName(), schemaFile, e.getMessage(), e);
         } catch (IOException e) {
             resultCollector.addFailure(event.getName(), "metadata", "Metadata_checker_component",
                                        "Failure reading data from '" + event.getName() + "': " + e.toString());
             log.debug("IO error reading '{}' while validating with schema '{}'", event.getName(), schemaFile, e);
         } catch (Exception e) {
             StringWriter sw = new StringWriter();
             e.printStackTrace(new PrintWriter(sw));
             resultCollector.addFailure(event.getName(), "metadata", "Metadata_checker_component",
                                        "Unexpected failure processing data from '" + event.getName() + "': " + e
                                                .toString(), sw.toString());
             log.error("Unexpected error while validating '{}' with schema '{}'", event.getName(), schemaFile, e);
         }
     }
 
     /**
      * Create a new validator for the schema in the given schema file. Note: Validators are not thread safe!
      * @param schemaFile The file name of the schema to get a validator for.
      * @return A validator for the given schema.
      * @throws SAXException If the schema fails to parse.
      */
     private Validator createValidator(String schemaFile) throws SAXException {
         Schema schema = getSchema(schemaFile);
         return schema.newValidator();
     }
 
     /**
      * Given a schema file name, get a parsed version of the schema from the classpath. Note that parsed schemas are
      * cached.
      * @param schemaFile The filename of the schema.
      * @return The parsed schema.
      * @throws SAXException If the schema fails to parse.
      */
     private synchronized Schema getSchema(String schemaFile) throws SAXException {
         if (schemas.get(schemaFile) == null) {
             URL schemaUrl = getClass().getClassLoader().getResource(schemaFile);
             Schema schema = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI).newSchema(schemaUrl);
             schemas.put(schemaFile, schema);
         }
         return schemas.get(schemaFile);
     }
 
     @Override
     public void handleFinish() {
         // Do nothing
     }
 }
