 package cz.muni.fi.pb138.log4jconverter;
 
 import cz.muni.fi.pb138.log4jconverter.configuration.Appender;
 import cz.muni.fi.pb138.log4jconverter.configuration.Configuration;
 import cz.muni.fi.pb138.log4jconverter.configuration.ErrorHandler;
 import java.io.File;
 import java.net.URI;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerConfigurationException;
 import javax.xml.transform.TransformerException;
 import javax.xml.transform.TransformerFactory;
 import javax.xml.transform.dom.DOMSource;
 import javax.xml.transform.stream.StreamResult;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.NodeList;
 
 public class XMLParser implements Parser {
 	
 	private Document doc;
         private Configuration configuration;
 	
	public XMLParser(Document document){
             this.doc = document;
             this.configuration = null;
 	}
         
         public boolean validateXML() {
             // TODO
             return false;
         }
         
         public void parseXML() {
             if (!validateXML()) {
                 // XML not valid, throw exception, write error message etc. and quit
                 return;
             }
             
             int i;
             
             // Load appenders
             Appender appender;
             NodeList appenderList = doc.getElementsByTagName("appender");
             for (i = 0; i < appenderList.getLength(); i++) {
                 appender = parseAppender((Element) appenderList.item(i));
                 configuration.addAppender(appender);
             }
             
             // TODO: Load additional attributes
         }
         
         public Appender parseAppender(Element appenderElement) {
             Appender appender = null;
             
             // Required attributes
             appender.setAppenderName(appenderElement.getAttribute("name"));
             appender.setClassName(appenderElement.getAttribute("class"));
 
             // ErrorHandler, optional
             NodeList errorHandlerList = appenderElement.getElementsByTagName("errorHandler");
             if (errorHandlerList.getLength() == 1) {
                 ErrorHandler errorHandler = parseErrorHandler((Element) errorHandlerList.item(0));
                 appender.setErrorhandler(errorHandler);
             }
             
             // TODO: Load additional attributes
             
             return appender;
         }
         
         public ErrorHandler parseErrorHandler(Element errorHandlerElement) {
             ErrorHandler errorHandler = null;
             int i;
             
             // Required attribute
             errorHandler.setClassName(errorHandlerElement.getAttribute("class"));
 
             // param
             NodeList paramList = errorHandlerElement.getElementsByTagName("param");
             for (i = 0; i < paramList.getLength(); i++) {
                 Element paramElement = (Element) paramList.item(i);
                 errorHandler.addParam(paramElement.getAttribute("name"), paramElement.getAttribute("value"));
             }
             
             // root-ref
             NodeList rootRefList = errorHandlerElement.getElementsByTagName("root-ref");
             if (rootRefList.getLength() == 1) {
                 errorHandler.setRootRef(true);
             }
 
             // logger-ref
             NodeList loggerRefList = errorHandlerElement.getElementsByTagName("logger-ref");
             for (i = 0; i < loggerRefList.getLength(); i++) {
                 Element loggerRefElement = (Element) loggerRefList.item(i);
                 errorHandler.addLoggerRef(loggerRefElement.getAttribute("ref"));
             }
 
             // appender-ref
             NodeList appenderRefList = errorHandlerElement.getElementsByTagName("appender-ref");
             if (appenderRefList.getLength() == 1) {
                 Element appenderRefElement = (Element) appenderRefList.item(0);
                 errorHandler.setAppenderRef(appenderRefElement.getAttribute("ref"));
             }
             
             return errorHandler;
         }
         
         /*
          * This method just prints the document, not the configuration (for testing purposes only)
          */
         public void writeAllXML(URI output) throws TransformerConfigurationException, TransformerException {
             TransformerFactory factory = TransformerFactory.newInstance();
             Transformer transformer = factory.newTransformer();
             DOMSource source = new DOMSource(doc);
             StreamResult result = new StreamResult(output.toString());
             transformer.transform(source, result);
         }
         
         public void writeAllXML(File output) throws TransformerConfigurationException, TransformerException {
             writeAllXML(output.toURI());
         }
 
 	@Override
 	public Configuration parse() {
             if(configuration == null){
                 configuration = new Configuration();
                 parseXML();
             }
             return configuration;
 	}
 
 }
