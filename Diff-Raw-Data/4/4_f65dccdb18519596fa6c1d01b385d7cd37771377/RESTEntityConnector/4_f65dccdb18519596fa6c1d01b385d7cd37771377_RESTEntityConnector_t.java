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
 
 import com.socialcomputing.wps.server.planDictionnary.connectors.WPSConnectorException;
 import com.socialcomputing.wps.server.planDictionnary.connectors.datastore.Attribute;
 import com.socialcomputing.wps.server.planDictionnary.connectors.datastore.Entity;
 import com.socialcomputing.wps.server.planDictionnary.connectors.datastore.PropertyDefinition;
 import com.socialcomputing.wps.server.planDictionnary.connectors.utils.UrlHelper;
 import com.socialcomputing.wps.server.plandictionary.connectors.datastore.file.FileEntityConnector;
 
 public class RESTEntityConnector extends FileEntityConnector {
 
     protected String contentType = null;
     protected String invert = null;
     protected String m_EntityId = null, m_EntityMarkup = null, m_AttributeId = null, m_AttributeMarkup = null;
 
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
         //LOG.info("Reading REST entity connector configuration");
         RESTEntityConnector connector = new RESTEntityConnector(element.getAttributeValue("name"));
 
         connector._readObject(element);
         connector.contentType = element.getAttributeValue("type");
         connector.invert = element.getAttributeValue("invert");
 
         Element entity = element.getChild("REST-entity");
         connector.m_EntityMarkup = entity.getAttributeValue("markup");
         connector.m_EntityId = entity.getAttributeValue("id");
         for (Element property : (List<Element>) entity.getChildren("REST-property")) {
             connector.entityProperties.add(new PropertyDefinition(property.getAttributeValue("id"), property
                     .getAttributeValue("attribute")));
         }
 
         Element attribute = element.getChild("REST-attribute");
         connector.m_AttributeMarkup = attribute.getAttributeValue("markup");
         connector.m_AttributeId = attribute.getAttributeValue("id");
         for (Element property : (List<Element>) attribute.getChildren("REST-property")) {
             connector.attributeProperties.add(new PropertyDefinition(property.getAttributeValue("id"), property
                     .getAttributeValue("entity")));
         }
         /*LOG.debug("(type = {}, invert = {}, entity=({}), attribute=({}))", new Object[] { connector.contentType,
                                                                                          connector.invert,
                                                                                          connector.entityProperties });*/
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
     public void openConnections(int planType, Hashtable<String, Object> wpsparams) throws WPSConnectorException {
         super.openConnections(planType, wpsparams);
 
         // If the content type is specified in the connector configuration,
         // force the mime type to that value instead of relying on the mime type
         // detected when the connection opens
         String contentType = (this.contentType != null) ? this.contentType : urlHelper.getContentType();
 
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
             throw new WPSConnectorException("Unsupported content type: " + this.contentType + ". Only "
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
      * @throws WPSConnectorException
      */
     private void readJSON(Hashtable<String, Object> wpsparams) throws WPSConnectorException {
         ObjectMapper mapper = new ObjectMapper();
         try {
             JsonNode node = mapper.readTree(urlHelper.getStream());
             JsonNode error = (JsonNode) node.get("error");
             if( error != null) {
                 throw new WPSConnectorException( error.get("message").getTextValue());
             }
             else {
                 JsonNode globals = (JsonNode) node.get("globals");
                 if (globals != null) {
                     for (Iterator<String> it = globals.getFieldNames(); it.hasNext();) {
                         String key = it.next();
                         wpsparams.put( key, readJSONValue( globals.get(key)));
                     }
                 }
                 ArrayNode entities = (ArrayNode) node.get(m_EntityMarkup);
                 if (entities != null) {
                     for (JsonNode jsonentity : entities) {
     
                         Entity entity = addEntity(jsonentity.get(m_EntityId).getTextValue());
                         for (PropertyDefinition property : entityProperties) {
                             if (property.isSimple()) {
                                 entity.addProperty(property.getName(), readJSONValue( jsonentity.get(property.getName())));
                             }
                         }
     
                         ArrayNode attributes = (ArrayNode) jsonentity.get(m_AttributeMarkup);
                         if (attributes != null) {
                             for (JsonNode jsonattribute : attributes) {
                                 Attribute attribute = addAttribute( jsonattribute.get(m_AttributeId).getTextValue());
                                 entity.addAttribute(attribute, 1);
                             }
                         }
                     }
                 }
                 ArrayNode attributes = (ArrayNode) node.get(m_AttributeMarkup);
                 if (attributes != null) {
                     for (JsonNode jsonattribute : attributes) {
                         Attribute attribute = addAttribute(jsonattribute.get(m_AttributeId).getTextValue());
                         for (PropertyDefinition property : attributeProperties) {
                             if (property.isSimple()) {
                                 JsonNode p = jsonattribute.get(property.getName());
                                 if (p != null)
                                     attribute.addProperty(property, readJSONValue( p));
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
             throw new WPSConnectorException("REST Reading json error", e);
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
         else if ( node.isDouble()) {
             return node.getDoubleValue();
         }
        return StringEscapeUtils.unescapeHtml(node.getTextValue());
     }
     
     /**
      * <p>
      * Read the entities and attributes to construct the map from XML formatted
      * data
      * <p>
      * 
      * @param wpsparams
      * @throws WPSConnectorException
      */
     private void readXml(Hashtable<String, Object> wpsparams) throws WPSConnectorException {
         Element root;
         try {
             // TODO SAX Parser => faster
             org.jdom.input.SAXBuilder builder = new org.jdom.input.SAXBuilder(false);
             org.jdom.Document doc = builder.build(urlHelper.getStream());
             root = doc.getRootElement();
         }
         catch (Exception e) {
             LOG.error(e.getMessage(), e);
             throw new WPSConnectorException("REST Reading xml error", e);
         }
 
         for (Element el : (List<Element>) root.getChildren("globals")) {
             wpsparams.put(el.getAttributeValue("id"), el.getText());
         }
         for (Element el : (List<Element>) root.getChildren(m_EntityMarkup)) {
             Entity entity = addEntity(el.getAttributeValue(m_EntityId));
             for (PropertyDefinition property : entityProperties) {
                 if (property.isSimple()) {
                     entity.addProperty(property.getName(), el.getAttributeValue(property.getName()));
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
     public void closeConnections() throws WPSConnectorException {
         super.closeConnections();
     }
 }
