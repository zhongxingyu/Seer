 package fedora.server.validation;
 
 // JAXP imports
 import javax.xml.parsers.*;
 import org.xml.sax.*;
 
 import java.io.File;
 import java.io.InputStream;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.net.URI;
 
 import fedora.server.errors.ObjectValidityException;
 import fedora.server.errors.GeneralException;
 
 /**
  *
  * <p><b>Title:</b> DOValidatorXMLSchema.java</p>
  * <p><b>Description:</b> XML Schema validation for Digital Objects</p>
  *
  * @author payette@cs.cornell.edu
  * @version $Id$
  */
 public class DOValidatorXMLSchema implements EntityResolver
 {
     /** Constants used for JAXP 1.2 */
     private static final String JAXP_SCHEMA_LANGUAGE =
         "http://java.sun.com/xml/jaxp/properties/schemaLanguage";
     private static final String W3C_XML_SCHEMA =
         "http://www.w3.org/2001/XMLSchema";
     private static final String JAXP_SCHEMA_SOURCE =
         "http://java.sun.com/xml/jaxp/properties/schemaSource";
 
     private URI schemaURI = null;
 
     public DOValidatorXMLSchema(String schemaPath) throws GeneralException
     {
       try
       {
         schemaURI = (new File(schemaPath)).toURI();
       }
       catch (Exception e)
       {
         System.err.println("DOValidatorXMLSchema caught ERROR in Constructor: "
           + e.getMessage());
         throw new GeneralException(e.getMessage());
       }
     }
 
     public void validate(File objectAsFile)
       throws ObjectValidityException, GeneralException
     {
       try
       {
 		validate(new InputSource(new FileInputStream(objectAsFile)));
       }
       catch (IOException e)
       {
         String msg = "DOValidatorXMLSchema returned error.\n"
                   + "The underlying exception was a " + e.getClass().getName() + ".\n"
                   + "The message was "  + "\"" + e.getMessage() + "\"";
         throw new GeneralException(msg);
       }
     }
 
 
 
     public void validate(InputStream objectAsStream)
       throws ObjectValidityException, GeneralException
     {
 	  validate(new InputSource(objectAsStream));
     }
 
     private void validate(InputSource objectAsSource)
       throws ObjectValidityException, GeneralException
     {
       InputSource doXML = objectAsSource;
       try
       {
       // XMLSchema validation via SAX parser
       SAXParserFactory spf = SAXParserFactory.newInstance();
       spf.setNamespaceAware(true);
       spf.setValidating(true);
       SAXParser sp = spf.newSAXParser();
       sp.setProperty(JAXP_SCHEMA_LANGUAGE, W3C_XML_SCHEMA);
 
       // JAXP property for schema location
       sp.setProperty(
         "http://java.sun.com/xml/jaxp/properties/schemaSource",
         schemaURI.toString());
 
       XMLReader xmlreader = sp.getXMLReader();
       xmlreader.setErrorHandler(new DOValidatorXMLErrorHandler());
       xmlreader.setEntityResolver(this);
       xmlreader.parse(doXML);
       }
       catch (ParserConfigurationException e)
       {
         String msg = "DOValidatorXMLSchema returned parser error.\n"
                   + "The underlying exception was a " + e.getClass().getName() + ".\n"
                   + "The message was "  + "\"" + e.getMessage() + "\"";
         throw new GeneralException(msg, e);
       }
       catch (SAXException e)
       {
         String msg = "DOValidatorXMLSchema returned validation exception.\n"
                   + "The underlying exception was a " + e.getClass().getName() + ".\n"
                   + "The message was "  + "\"" + e.getMessage() + "\"";
         throw new ObjectValidityException(msg, e);
       }
       catch (Exception e)
       {
         String msg = "DOValidatorXMLSchema returned error.\n"
                   + "The underlying error was a " + e.getClass().getName() + ".\n"
                   + "The message was "  + "\"" + e.getMessage() + "\"";
         throw new GeneralException(msg, e);
       }
     }
 
     /**
     * Resolve the entity if it's referring to a local schema.
      * Otherwise, return an empty InputSource.
      *
      * This behavior is required in order to ensure that Xerces never
      * attempts to load external schemas specified with xsi:schemaLocation.
      * It is not enough that we specify processContents="skip" in our own
      * schema.
      */
     public InputSource resolveEntity(String publicId, String systemId) {
        if (systemId != null && systemId.startsWith("file:")) {
             return null;
         } else {
             return new InputSource();
         }
     }
 }
