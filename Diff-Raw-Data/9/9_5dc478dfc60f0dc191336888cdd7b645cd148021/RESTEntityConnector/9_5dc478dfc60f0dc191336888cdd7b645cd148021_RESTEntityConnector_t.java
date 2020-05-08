 package com.socialcomputing.wps.server.plandictionary.connectors.datastore.file.rest;
 
 import java.util.ArrayList;
 import java.util.Hashtable;
 import java.util.Iterator;
 import java.util.List;
 
 import javax.ws.rs.core.MediaType;
 
 import org.apache.commons.lang.StringEscapeUtils;
 import org.codehaus.jackson.JsonNode;
 import org.codehaus.jackson.map.ObjectMapper;
 import org.codehaus.jackson.node.ArrayNode;
 import org.jdom.Element;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.socialcomputing.wps.server.planDictionnary.connectors.JMIException;
 import com.socialcomputing.wps.server.planDictionnary.connectors.datastore.Attribute;
 import com.socialcomputing.wps.server.planDictionnary.connectors.datastore.Entity;
 import com.socialcomputing.wps.server.planDictionnary.connectors.datastore.PropertyDefinition;
 import com.socialcomputing.wps.server.planDictionnary.connectors.utils.UrlHelper;
 import com.socialcomputing.wps.server.plandictionary.connectors.datastore.file.FileEntityConnector;
 
 public class RESTEntityConnector extends FileEntityConnector {
 
     protected String contentType = null;
     protected String invert = null;
     protected String data = null;
     protected String m_GlobalMarkup = null, m_EntityId = null, m_EntityMarkup = null, m_PonderationMarkup = null, m_AttributeId = null, m_AttributeMarkup = null;
     protected boolean m_entitiesAllProperties = true, m_atttributesAllProperties = true;
     
     private static final Logger LOG = LoggerFactory.getLogger(RESTEntityConnector.class);
 
     /**
      * <p>
      * Construct a <code>RESTEntityConnector</code> from the XML configuration
      * stored in the database
      * </p>
      * 
      * @param element
      *            a <code>org.jdom.Element</code> containing the root of the
      *            connector configuration
      * @return an initialised instance of <code>RESTEntityConnector</code>
      */
     public static RESTEntityConnector readObject(org.jdom.Element element) {
         LOG.info("Reading REST entity connector configuration");
         RESTEntityConnector connector = new RESTEntityConnector(element.getAttributeValue("name"));
 
         connector._readObject(element);
         connector.contentType = element.getAttributeValue("type");
         connector.invert = element.getAttributeValue("invert");
         connector.data = element.getAttributeValue("data");
         connector.m_GlobalMarkup = element.getAttributeValue("markup");
         if( connector.m_GlobalMarkup == null)
             connector.m_GlobalMarkup = "globals"; // compatibility patch
         connector.m_PonderationMarkup = element.getAttributeValue("ponderation");
         if( connector.m_PonderationMarkup == null)
             connector.m_PonderationMarkup = "p"; // compatibility patch
             
         Element entity = element.getChild("REST-entity");
         connector.m_EntityMarkup = entity.getAttributeValue("markup");
         connector.m_EntityId = entity.getAttributeValue("id");
         String v = entity.getAttributeValue("all-properties");
         connector.m_entitiesAllProperties = v != null ? (v.equalsIgnoreCase("true") ? true : false) : false;
         for (Element property : (List<Element>) entity.getChildren("REST-property")) {
             connector.entityProperties.add(new PropertyDefinition(property.getAttributeValue("id"), 
                                                                   property.getAttributeValue("attribute"),
                                                                   property.getAttributeValue("default")));
         }
 
         Element attribute = element.getChild("REST-attribute");
         connector.m_AttributeMarkup = attribute.getAttributeValue("markup");
         connector.m_AttributeId = attribute.getAttributeValue("id");
         v = attribute.getAttributeValue("all-properties");
         connector.m_atttributesAllProperties = v != null ? (v.equalsIgnoreCase("true") ? true : false) : false;
         for (Element property : (List<Element>) attribute.getChildren("REST-property")) {
             connector.attributeProperties.add(new PropertyDefinition(property.getAttributeValue("id"), 
                                                                      property.getAttributeValue("entity"),
                                                                      property.getAttributeValue("default")));
         }
         return connector;
     }
 
     /**
      * Default constructor
      * 
      * @param name
      *            the name of the connector as specified in the dictionary
      */
     public RESTEntityConnector(String name) {
         super(name);
     }
 
     @Override
     public void openConnections(int planType, Hashtable<String, Object> wpsparams) throws JMIException {
         super.openConnections(planType, wpsparams);
 
         // If the content type is specified in the connector configuration,
         // force the mime type to that value instead of relying on the mime type
         // detected when the connection opens
         String contentType = (this.contentType != null) ? this.contentType : urlHelper.getContentType();
         LOG.debug("content type = {}", contentType);
 
         m_inverted = UrlHelper.ReplaceParameter(invert, wpsparams).equalsIgnoreCase("true");
 
         if (contentType != null
                 && (contentType.equalsIgnoreCase("json") || contentType.equalsIgnoreCase(MediaType.APPLICATION_JSON))) {
             readJSON(wpsparams);
         }
         else if (contentType != null
                 && (contentType.equalsIgnoreCase("xml") || contentType.equalsIgnoreCase(MediaType.APPLICATION_XML))) {
             readXml(wpsparams);
         }
         else {
             throw new JMIException(JMIException.ORIGIN.CONNECTOR,"Unsupported content type: " + this.contentType + ". Only "
                     + MediaType.APPLICATION_JSON + " and " + MediaType.APPLICATION_XML + " are available for now");
         }
     }
 
     /**
      * <p>
      * Read the entities and attributes to construct the map from JSON formatted
      * data
      * <p>
      * 
      * @param wpsparams
      * @throws JMIException
      */
     private void readJSON(Hashtable<String, Object> wpsparams) throws JMIException {
         ObjectMapper mapper = new ObjectMapper();
         JsonNode error = null;
         try {
             JsonNode node = mapper.readTree(urlHelper.getStream());
             error = (JsonNode) node.get("error");
             if( error == null && this.data != null && wpsparams.containsKey(this.data)) {
                 // Data
                 node = mapper.readTree((String)wpsparams.get(this.data));
             }
             if( error == null) {
                 JsonNode globals = (JsonNode) node.get(m_GlobalMarkup);
                 if (globals != null) {
                     for (Iterator<String> it = globals.getFieldNames(); it.hasNext();) {
                         String key = it.next();
                         wpsparams.put( key, readJSONValue( globals.get(key)));
                     }
                 }
                 ArrayNode entities = (ArrayNode) node.get(m_EntityMarkup);
                 if (entities != null) {
                     for (JsonNode jsonentity : entities) {
                         Entity entity = addEntity( readId( jsonentity.get(m_EntityId)));
                         if( m_entitiesAllProperties) {
                             for( Iterator<String> it = jsonentity.getFieldNames(); it.hasNext(); ) {
                                 String field = it.next();
                                entity.addProperty(field, readJSONValue( jsonentity.get(field)));
                             }
                         }
                         else {
                             for (PropertyDefinition property : entityProperties) {
                                 if (property.isSimple()) {
                                     entity.addProperty(property, readJSONValue( jsonentity.get(property.getName())));
                                 }
                             }
                         }
     
                         ArrayNode attributes = (ArrayNode) jsonentity.get(m_AttributeMarkup);
                         if (attributes != null) {
                             for (JsonNode jsonattribute : attributes) {
                                 Attribute attribute = addAttribute( readId( jsonattribute.get(m_AttributeId)));
                                 entity.addAttribute(attribute, readPonderation( jsonattribute.get(m_PonderationMarkup)));
                             }
                         }
                     }
                 }
                 ArrayNode attributes = (ArrayNode) node.get(m_AttributeMarkup);
                 if (attributes != null) {
                     for (JsonNode jsonattribute : attributes) {
                         Attribute attribute = addAttribute( readId( jsonattribute.get(m_AttributeId)));
                         if( m_atttributesAllProperties) {
                             for( Iterator<String> it = jsonattribute.getFieldNames(); it.hasNext(); ) {
                                 String field = it.next();
                                attribute.addProperty(field, readJSONValue( jsonattribute.get(field)));
                             }
                         }
                         else {
                             for (PropertyDefinition property : attributeProperties) {
                                 if (property.isSimple()) 
                                     attribute.addProperty(property, readJSONValue( jsonattribute.get(property.getName())));
                             }
                         }
                         if (!isInverted())
                             addEntityProperties(attribute);
                     }
                 }
                 if (isInverted()) {
                     for (Entity entity : m_Entities.values()) {
                         addAttributeProperties(entity);
                     }
                 }
             }
         }
         catch (Exception e) {
             //LOG.error(e.getMessage(), e);
             throw new JMIException("Reading json error:" + e.getMessage(), e);
         }
         if( error != null) {
             String m = error.get("message") != null ? error.get("message").getTextValue() : "";
             if( m.length() == 0) {
                 m = error.get("trace") != null ? error.get("trace").getTextValue() : "";
                 int p = m.indexOf("\n");
                 if( p != -1)
                     m = m.substring(0,p);
             }
             long code = error.get("code") != null ? error.get("code").getLongValue() : 0;
             throw new JMIException( JMIException.ORIGIN.CONNECTOR, code, m, error.get("trace").getTextValue());
         }
     }
 
     private Object readJSONValue(JsonNode node) {
         if( node == null)
             return null;
         if( node.isArray()) {
             ArrayNode tab = ( ArrayNode) node;
             List<Object> lst = new ArrayList<Object>(tab.size());
             for( JsonNode t : tab) {
                 lst.add( readJSONValue( t));
             }
             return lst.toArray();
         }
         else if( node.isInt()) {
             return node.getIntValue();
         }
         else if( node.isLong()) {
             return node.getLongValue();
         }
         else if ( node.isDouble()) {
             return node.getDoubleValue();
         }
         else if ( node.isBoolean()) {
             return node.getBooleanValue();
         }
         return StringEscapeUtils.unescapeHtml(node.getTextValue());
     }
 
     private String readId(JsonNode node) {
         if( node == null)
             return null;
         if ( node.isTextual()) {
             return node.getTextValue();
         }
         else if( node.isInt()) {
             return String.valueOf(node.getIntValue());
         }
         else if( node.isLong()) {
             return String.valueOf(node.getLongValue());
         }
         else if ( node.isDouble()) {
             return String.valueOf(node.getDoubleValue());
         }
         return null;
     }
     
     private float readPonderation(JsonNode node) {
         if( node == null)
             return 1;
         if( node.isInt()) {
             return node.getIntValue();
         }
         else if( node.isLong()) {
             return node.getLongValue();
         }
         else if ( node.isDouble()) {
             return (float)node.getDoubleValue();
         }
         return 1;
     }
     
     /**
      * <p>
      * Read the entities and attributes to construct the map from XML formatted
      * data
      * <p>
      * 
      * @param wpsparams
      * @throws JMIException
      */
     private void readXml(Hashtable<String, Object> wpsparams) throws JMIException {
         Element root;
         try {
             // TODO SAX Parser => faster
             org.jdom.input.SAXBuilder builder = new org.jdom.input.SAXBuilder(false);
             org.jdom.Document doc = builder.build(urlHelper.getStream());
             root = doc.getRootElement();
         }
         catch (Exception e) {
             LOG.error(e.getMessage(), e);
             throw new JMIException("REST Reading xml error", e);
         }
 
         for (Element el : (List<Element>) root.getChildren("globals")) {
             wpsparams.put(el.getAttributeValue("id"), el.getText());
         }
         for (Element el : (List<Element>) root.getChildren(m_EntityMarkup)) {
             Entity entity = addEntity(el.getAttributeValue(m_EntityId));
             for (PropertyDefinition property : entityProperties) {
                 if (property.isSimple()) {
                     entity.addProperty(property, el.getAttributeValue(property.getName()));
                 }
             }
 
             for (Element el2 : (List<Element>) el.getChildren(m_AttributeMarkup)) {
                 Attribute attribute = addAttribute(el2.getAttributeValue(m_AttributeId));
                 entity.addAttribute(attribute, 1);
             }
         }
         for (Element el : (List<Element>) root.getChildren(m_AttributeMarkup)) {
             Attribute attribute = addAttribute(el.getAttributeValue(m_AttributeId));
             for (PropertyDefinition property : attributeProperties) {
                 if (property.isSimple()) {
                     attribute.addProperty(property, el.getAttributeValue(property.getName()));
                 }
             }
             if (!isInverted())
                 addEntityProperties(attribute);
         }
         if (isInverted()) {
             for (Entity entity : m_Entities.values()) {
                 addAttributeProperties(entity);
             }
         }
     }
 
     @Override
     public void closeConnections() throws JMIException {
         super.closeConnections();
     }
 }
