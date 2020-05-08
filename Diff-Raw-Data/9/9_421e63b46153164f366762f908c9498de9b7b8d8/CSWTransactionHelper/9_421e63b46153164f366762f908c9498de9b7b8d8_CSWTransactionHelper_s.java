 package gov.usgs.cida.watersmart.csw;
 
 import gov.usgs.cida.config.DynamicReadOnlyProperties;
 import gov.usgs.cida.watersmart.common.ContextConstants;
 import gov.usgs.cida.watersmart.common.JNDISingleton;
 import gov.usgs.cida.watersmart.common.RunMetadata;
 import gov.usgs.cida.watersmart.iso.ISOServiceIdentification;
 import java.io.*;
 import java.net.URISyntaxException;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.logging.Level;
 import javax.xml.namespace.NamespaceContext;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.stream.XMLStreamWriter;
 import javax.xml.transform.OutputKeys;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerException;
 import javax.xml.transform.TransformerFactory;
 import javax.xml.transform.dom.DOMSource;
 import javax.xml.transform.stream.StreamResult;
 import javax.xml.xpath.XPath;
 import javax.xml.xpath.XPathConstants;
 import javax.xml.xpath.XPathExpression;
 import javax.xml.xpath.XPathExpressionException;
 import javax.xml.xpath.XPathFactory;
 import org.apache.commons.io.IOUtils;
 import org.apache.commons.jxpath.JXPathContext;
 import org.apache.http.HttpResponse;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.client.protocol.ClientContext;
 import org.apache.http.entity.StringEntity;
 import org.apache.http.impl.NoConnectionReuseStrategy;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.protocol.BasicHttpContext;
 import org.apache.http.protocol.HttpContext;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.w3c.dom.Document;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import org.xml.sax.SAXException;
 
 /**
  *
  * @author Jordan Walker <jiwalker@usgs.gov>
  */
 public class CSWTransactionHelper {
 
     private static final Logger LOG = LoggerFactory.getLogger(CSWTransactionHelper.class);
     private static DynamicReadOnlyProperties props = JNDISingleton.getInstance();
     private final String TRANSACTION_HEADER = "<csw:Transaction service=\"CSW\" version=\"2.0.2\" xmlns:csw=\"http://www.opengis.net/cat/csw/2.0.2\" xmlns:ogc=\"http://www.opengis.net/ogc\" xmlns:dc=\"http://www.purl.org/dc/elements/1.1/\">";
     private final String TRANSACTION_FOOTER = "</csw:Transaction>";
     public static final String NAMESPACE_GMD = "http://www.isotc211.org/2005/gmd";
     public static final String NAMESPACE_SRV = "http://www.isotc211.org/2005/srv";
     public static final String NAMESPACE_GCO = "http://www.isotc211.org/2005/gco";
     public static final String NAMESPACE_XLINK = "http://www.w3.org/1999/xlink";
     private RunMetadata metadataBean;
     private String sosEndpoint;
     private GeonetworkSession cswSession;
     private Map<String, String> algorithmOutputMap;
 
     public CSWTransactionHelper(RunMetadata runMeta, String sosEndpoint, Map<String, String> algorithmOutputMap) {
         this.metadataBean = runMeta;
         this.sosEndpoint = sosEndpoint;
         this.algorithmOutputMap = algorithmOutputMap;
         this.cswSession = new GeonetworkSession();
     }
 
     /*
      * Only used for updating CSW records
      */
     public CSWTransactionHelper(RunMetadata runMeta) {
         this(runMeta, null, null);
     }
 
     public String addServiceIdentification() throws IOException, UnsupportedEncodingException, URISyntaxException, ParserConfigurationException, SAXException, TransformerException {
         Document getRecordsDoc = getRecordsCall();
        String sosRepo = props.getProperty("watersmart.sos.model.repo");
         NodeList nodes = getRecordsDoc.getElementsByTagNameNS(NAMESPACE_GMD, "MD_Metadata");
 
         if (nodes.getLength() != 1) {
             throw new RuntimeException("Record not inserted, could not narrow search down to one record");
         }
         if (sosRepo == null) {
             throw new RuntimeException("Record not inserted, must specify thredds location");
         }
 
         Node recordNode = nodes.item(0);
 
         // Try to find an existing modelrun scenario - We do this when a user initiates an upload. The first client's first call executes PrepareRecord. 
         // The client's second call will work it's way into this function and if there's already a model run record, we need to replace the record instead of 
         // adding to the root. So here, we find whether the record with the model/run already exists
         Node existingNode;
         String scenario = this.metadataBean.getScenario();
         String version = this.metadataBean.getModelVersion() + "." + this.metadataBean.getRunIdent();
         String xpathString = "//gmd:identificationInfo/srv:SV_ServiceIdentification/gmd:citation/gmd:CI_Citation/gmd:title/gco:CharacterString[normalize-space(text())='" + scenario + "']/../../gmd:edition/gco:CharacterString[text()='" + version + "']/../../../../..";
         JXPathContext ctx = JXPathContext.newContext(getRecordsDoc.getDocumentElement());
         ctx.registerNamespace("gmd", NAMESPACE_GMD);
         ctx.registerNamespace("srv", NAMESPACE_SRV);
         ctx.registerNamespace("gco", NAMESPACE_GCO);
         // Use the xpath to try and find the node of an already existing record
         existingNode = (Node) ctx.selectSingleNode(xpathString);
 
         Node serviceIdNode = buildServiceIdentificationNode(getRecordsDoc);
         if (existingNode != null) { 
             // We found an existing node and will replace the existing node with the new serviceId node
             recordNode.replaceChild(serviceIdNode, existingNode);
         } else {
             // The record does not yet exist, so create one - this probably won't have SOS info in it and will be
             // replaced by the secondary call to this function but is used as a placeholder for the client
             recordNode.appendChild(buildServiceIdentificationNode(getRecordsDoc));
         }
 
         String insertXml = buildUpdateEnvelope(nodeToString(recordNode), metadataBean.getModelId());
         LOG.debug(insertXml);
         return performCSWPost(insertXml);
     }
 
     public String updateRunMetadata(RunMetadata oldInfo) throws IOException, URISyntaxException {
         Map<String, String> updateMap = this.metadataBean.getUpdateMap(oldInfo);
 
         String updateXml = buildUpdateEnvelope(updateMap, metadataBean.getModelId());
         LOG.debug(updateXml);
         String postResponse = performCSWPost(updateXml);
         return postResponse;
     }
 
     public void delete(RunMetadata info) {
     }
 
     private Document getRecordsCall() throws IOException, UnsupportedEncodingException, URISyntaxException, ParserConfigurationException, SAXException {
         String identifier = this.metadataBean.getModelId();
 
         String cswGetRecordsSearch = buildSearchXML(identifier);
         LOG.debug(cswGetRecordsSearch);
 
         String response = performCSWPost(cswGetRecordsSearch);
 
         DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
         factory.setNamespaceAware(true);
         Document doc = factory.newDocumentBuilder().parse(new ByteArrayInputStream(response.getBytes()));
 
         return doc;
     }
 
     private String performCSWPost(String postData) throws IOException, UnsupportedEncodingException, URISyntaxException {
         cswSession.login();
         HttpPost post = new HttpPost(cswSession.GEONETWORK_CSW);
         post.setEntity(new StringEntity(postData, "application/xml", "UTF-8"));
 
         HttpContext localContext = new BasicHttpContext();
         DefaultHttpClient httpClient = new DefaultHttpClient();
         httpClient.setReuseStrategy(new NoConnectionReuseStrategy());
         localContext.setAttribute(ClientContext.COOKIE_STORE, cswSession.getCookieJar());
         HttpResponse methodResponse = null;
         try {
             methodResponse = httpClient.execute(post, localContext);
             if (methodResponse.getStatusLine().getStatusCode() != 200) {
                 throw new IOException(
                         methodResponse.getStatusLine().getReasonPhrase());
             }
         } finally {
             if (httpClient.getConnectionManager() != null) {
                 httpClient.getConnectionManager().closeExpiredConnections();
             }
             cswSession.logout();
             InputStream is = methodResponse.getEntity().getContent();
             String responseText = IOUtils.toString(is);
             LOG.debug(responseText);
             IOUtils.closeQuietly(is);
             return responseText;
         }
     }
 
     private void fullRecordTransaction(XMLStreamWriter record) {
     }
 
     private void updateFieldsTransaction(Map<String, String> propValueMap) {
     }
 
     private String buildInsertEnvelope(String record) {
         StringBuilder insertXml = new StringBuilder();
 
         insertXml.append(this.TRANSACTION_HEADER)
                 .append("<csw:Insert>")
                 .append(record)
                 .append("</csw:Insert>")
                 .append(this.TRANSACTION_FOOTER);
 
         return insertXml.toString();
     }
 
     private String buildDeleteEnvelope(String identifier) {
         StringBuilder deleteXml = new StringBuilder();
 
         deleteXml.append(this.TRANSACTION_HEADER)
                 .append("<csw:Delete typeName=\"csw:Record\">")
                 .append(this.buildIdentifierFilter(identifier))
                 .append("</csw:Delete>")
                 .append(this.TRANSACTION_FOOTER);
         return deleteXml.toString();
     }
 
     private String buildUpdateEnvelope(Map<String, String> propValueMap, String identifier) {
         StringBuilder recordXml = new StringBuilder();
         Iterator<String> recordIter = propValueMap.keySet().iterator();
         while (recordIter.hasNext()) {
             String recordName = recordIter.next();
             recordXml.append("<csw:RecordProperty>")
                     .append("<csw:Name>").append(recordName).append("</csw:Name>")
                     .append("<csw:Value>").append(propValueMap.get(recordName)).append("</csw:Value>")
                     .append("</csw:RecordProperty>");
         }
 
         Iterator<String> algorithmIter = algorithmOutputMap.keySet().iterator();
         while (algorithmIter.hasNext()) {
             String algorithmName = algorithmIter.next();
             recordXml.append("<csw:RecordProperty>")
                     .append("<csw:Name>").append(RunMetadata.updateCoupledResourceXPath(this.metadataBean.getScenario(), this.metadataBean.getModelVersion(), this.metadataBean.getRunIdent(), algorithmName)).append("</csw:Name>")
                     .append("<csw:Value>").append(algorithmOutputMap.get(algorithmName)).append("</csw:Value>")
                     .append("</csw:RecordProperty>");
         }
 
         return buildUpdateEnvelope(recordXml.toString(), identifier);
     }
 
     private String buildUpdateEnvelope(String recordXml, String identifier) {
         StringBuilder updateXml = new StringBuilder();
 
         updateXml.append(this.TRANSACTION_HEADER)
                 .append("<csw:Update>")
                 .append(recordXml)
                 .append(this.buildIdentifierFilter(identifier))
                 .append("</csw:Update>")
                 .append(this.TRANSACTION_FOOTER);
 
         return updateXml.toString();
     }
 
     private String buildSearchXML(String identifier) {
         StringBuilder searchXml = new StringBuilder();
 
         searchXml.append("<csw:GetRecords xmlns:csw=\"http://www.opengis.net/cat/csw/2.0.2\" service=\"CSW\" version=\"2.0.2\" resultType=\"results\" outputSchema=\"http://www.isotc211.org/2005/gmd\">")
                 .append("<csw:Query typeNames=\"csw:Record\">")
                 .append("<csw:ElementSetName>full</csw:ElementSetName>")
                 .append(this.buildIdentifierFilter(identifier))
                 .append("</csw:Query>")
                 .append("</csw:GetRecords>");
 
         return searchXml.toString();
     }
 
     private String buildIdentifierFilter(String identifier) {
         StringBuilder filterXml = new StringBuilder();
 
         filterXml.append("<csw:Constraint version=\"1.1.0\">")
                 .append("<ogc:Filter xmlns:ogc=\"http://www.opengis.net/ogc\">")
                 .append("<ogc:PropertyIsEqualTo>")
                 .append("<ogc:PropertyName>Identifier</ogc:PropertyName>")
                 .append("<ogc:Literal>").append(identifier).append("</ogc:Literal>")
                 .append("</ogc:PropertyIsEqualTo>")
                 .append("</ogc:Filter>")
                 .append("</csw:Constraint>");
 
         return filterXml.toString();
     }
 
     private Node buildServiceIdentificationNode(Document doc) {
         ISOServiceIdentification iso = new ISOServiceIdentification(metadataBean, sosEndpoint, algorithmOutputMap, doc);
         Node idNode = iso.makeIdentificationInfo();
         return idNode;
     }
 
     public static String nodeToString(Node node) throws TransformerException {
         StringWriter writer = new StringWriter();
         Transformer t = TransformerFactory.newInstance().newTransformer();
         t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
         t.transform(new DOMSource(node), new StreamResult(writer));
         return writer.toString();
     }
 }
