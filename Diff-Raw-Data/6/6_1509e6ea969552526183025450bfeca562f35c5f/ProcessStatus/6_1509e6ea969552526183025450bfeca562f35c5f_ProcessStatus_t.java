 package gov.usgs.cida.gdp.wps.completion;
 
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 import javax.xml.namespace.NamespaceContext;
 import javax.xml.xpath.XPath;
 import javax.xml.xpath.XPathConstants;
 import javax.xml.xpath.XPathExpression;
 import javax.xml.xpath.XPathExpressionException;
 import javax.xml.xpath.XPathFactory;
 import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
 
 /**
  *
  * @author jwalker
  */
 public class ProcessStatus {
 
     private Document document;
     private XPath xpath;
 
     public ProcessStatus(Document document) {
         this.document = document;
         XPathFactory xpathFactory = XPathFactory.newInstance();
         xpath = xpathFactory.newXPath();
         xpath.setNamespaceContext(new GetObservationNamespaceContext());
     }
 
     public boolean isStarted() throws XPathExpressionException {
         String XPATH_started = "/wps:ExecuteResponse/wps:Status/wps:ProcessStarted";
         XPathExpression startedExpression = xpath.compile(XPATH_started);
         return (null != startedExpression.evaluate(document, XPathConstants.NODE));
     }
 
     public boolean isAccepted() throws XPathExpressionException {
         String XPATH_accepted = "/wps:ExecuteResponse/wps:Status/wps:ProcessAccepted";
         XPathExpression acceptedExpression = xpath.compile(XPATH_accepted);
         return (null != acceptedExpression.evaluate(document, XPathConstants.NODE));
     }
 
     public boolean isPaused() throws XPathExpressionException {
         String XPATH_paused = "/wps:ExecuteResponse/wps:Status/wps:ProcessPaused";
         XPathExpression pausedExpression = xpath.compile(XPATH_paused);
         return (null != pausedExpression.evaluate(document, XPathConstants.NODE));
     }
 
     public boolean isSuccess() throws XPathExpressionException {
         String XPATH_succeeded = "/wps:ExecuteResponse/wps:Status/wps:ProcessSucceeded";
         XPathExpression succeededExpression = xpath.compile(XPATH_succeeded);
         return (null != succeededExpression.evaluate(document, XPathConstants.NODE));
     }
 
     public boolean isFailed() throws XPathExpressionException {
         String XPATH_failed = "/wps:ExecuteResponse/wps:Status/wps:ProcessFailed";
         XPathExpression failedExpression = xpath.compile(XPATH_failed);
         return (null != failedExpression.evaluate(document, XPathConstants.NODE));
     }
 
     /**
      * @return Will only return value when process is successful
      */
     public String getOutputReference() throws XPathExpressionException {
         String XPATH_output = "/wps:ExecuteResponse/wps:ProcessOutputs/wps:Output/wps:Reference/@href";
         XPathExpression outputExpression = xpath.compile(XPATH_output);
         return outputExpression.evaluate(document);
     }
 
     public String getSuccessMessage() throws XPathExpressionException {
         String XPATH_succeeded = "/wps:ExecuteResponse/wps:Status/wps:ProcessSucceeded/text()";
         XPathExpression succeededExpression = xpath.compile(XPATH_succeeded);
         return succeededExpression.evaluate(document);
     }
 
     /**
      * @return Will only return value when process fails
      * @throws XPathExpressionException
      */
     public String getFailureMessage() throws XPathExpressionException {
         String XPATH_failed = "/wps:ExecuteResponse/wps:Status/wps:ProcessFailed/ows:ExceptionReport//ows:Exception/ows:ExceptionText";
         XPathExpression failedExpression = xpath.compile(XPATH_failed);
        NodeList exceptions = (NodeList) failedExpression.evaluate(document, XPathConstants.NODESET);
         StringBuilder builder = new StringBuilder();
         String newline = System.getProperty("line.separator");
         for (int i = 0; i < exceptions.getLength(); i++) {
             builder.append(newline);
             builder.append(exceptions.item(i).getTextContent());
         }
         return builder.toString();
     }
 
     public String getPercentComplete() throws XPathExpressionException {
         String percentComplete = "0";
         if (isAccepted()) {
             percentComplete = "0";
         } else if (isStarted()) {
             String XPATH_started = "/wps:ExecuteResponse/wps:Status/wps:ProcessStarted/@percentCompleted";
             XPathExpression startedExpression = xpath.compile(XPATH_started);
             percentComplete = startedExpression.evaluate(document);
         } else if (isPaused()) {
             String XPATH_paused = "/wps:ExecuteResponse/wps:Status/wps:ProcessPaused/@percentCompleted";
             XPathExpression pausedExpression = xpath.compile(XPATH_paused);
             percentComplete = pausedExpression.evaluate(document);
         } else if (isSuccess() || isFailed()) {
             percentComplete = "100";
         }
         return (percentComplete.equals("")) ? "0" : percentComplete;
     }
 }
 
 class GetObservationNamespaceContext implements NamespaceContext {
 
     public final static Map<String, String> namespaceMap;
 
     static {
         namespaceMap = new HashMap<String, String>();
         namespaceMap.put("", "http://www.opengis.net/wps/1.0.0");
         namespaceMap.put("wps", "http://www.opengis.net/wps/1.0.0");
         namespaceMap.put("ows", "http://www.opengis.net/ows/1.1");
         namespaceMap.put("xlink", "http://www.w3.org/1999/xlink");
     }
 
     @Override
     public String getNamespaceURI(String prefix) {
         if (prefix == null) {
             throw new NullPointerException("prefix is null");
         }
         String namespaceURI = namespaceMap.get(prefix);
 
         return namespaceURI;
     }
 
     @Override
     public String getPrefix(String namespaceURI) {
         throw new UnsupportedOperationException();
     }
 
     @Override
     public Iterator<String> getPrefixes(String namespaceURI) {
         throw new UnsupportedOperationException();
     }
 }
