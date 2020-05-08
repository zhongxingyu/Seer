 /* Copyright (c) 2001, 2003 TOPP - www.openplans.org.  All rights reserved.
  * This code is licensed under the GPL 2.0 license, availible at the root
  * application directory.
  */
 package org.vfny.geoserver.requests;
 
 import com.vividsolutions.jts.geom.Geometry;
 import org.geotools.feature.AttributeType;
 import org.geotools.feature.AttributeTypeFactory;
 import org.geotools.feature.Feature;
 import org.geotools.feature.FeatureType;
 import org.geotools.feature.FeatureTypeFactory;
 import org.geotools.gml.GMLFilterFeature;
 import org.vfny.geoserver.config.TypeRepository;
 import org.xml.sax.Attributes;
 import org.xml.sax.SAXException;
 import java.util.List;
 import java.util.Vector;
 import java.util.logging.Logger;
 
 
 /**
  * Uses SAX to extact a Transactional request from and incoming XML stream.
  *
  * @author Chris Holmes, TOPP
 * @version $Id: TransactionFeatureHandler.java,v 1.9 2003/12/10 17:30:40 cholmesny Exp $
  */
 public class TransactionFeatureHandler extends GMLFilterFeature {
     //    implements ContentHandler, FilterHandler, GMLHandlerFeature {
 
     /** Class logger */
     private static Logger LOGGER = Logger.getLogger(
             "org.vfny.geoserver.requests");
 
     /** Stores current feature attributes. */
     private List attributes = new Vector();
     private List attributeNames = new Vector();
 
     /** Stores current feature attributes. */
     private boolean insideAttribute = false;
 
     /** Stores current feature attributes. */
     private boolean insideFeature = false;
     private boolean insideInsert = false;
 
     /** Stores current feature attributes. */
     private Object tempValue = null;
     private String attName = "";
 
     //private FeatureSchema metadata = new FeatureSchema();
     private String typeName = "GenericFeature";
     private TransactionFilterHandler parent;
     private TypeRepository typeRepo = TypeRepository.getInstance();
 
     /**
      * Constructor with parent, which must implement GMLHandlerJTS.
      *
      * @param parent The parent of this filter.
      */
     public TransactionFeatureHandler(TransactionFilterHandler parent) {
         super(parent);
         this.parent = parent;
     }
 
     /**
      * Checks for GML element start and - if not a coordinates element - sends
      * it directly on down the chain to the appropriate parent handler.  If it
      * is a coordinates (or coord) element, it uses internal methods to set
      * the current state of the coordinates reader appropriately.
      *
      * @param namespaceURI The namespace of the element.
      * @param localName The local name of the element.
      * @param qName The full name of the element, including namespace prefix.
      * @param atts The element attributes.
      *
      * @throws SAXException Some parsing error occured while reading
      *         coordinates.
      */
     public void startElement(String namespaceURI, String localName,
         String qName, Attributes atts) throws SAXException {
         if (localName.equals("Insert")) {
             insideInsert = true;
         }
 
         LOGGER.finest("checking out " + namespaceURI + ", " + localName);
 
         // if it ends with Member we'll assume it's a feature for the time being
         if (insideInsert && !(localName.equals("Insert"))) {
             String internalTypeName = typeRepo.getInternalTypeName(localName,
                     namespaceURI);
 
             if (!insideFeature) {
                 if (internalTypeName != null) {
                     typeName = internalTypeName;
                     attributes = new Vector();
                     attributeNames = new Vector();
 
                     //currentFeature = new FeatureFlat();
                     insideFeature = true;
                     tempValue = null;
                     LOGGER.finer("Starting a feature " + typeName);
                 } else {
                     throw new SAXException(
                         "Could not find featureType with name " + localName
                         + ", and uri: " + namespaceURI);
                 }
             }
 
             //HACK: the local name stuff should be handled in geotools.
             if (insideFeature
                     && !((localName.equals("lineStringMember"))
                     || (localName.equals("polygonMember"))
                     || (localName.equals("pointMember")))) {
                 LOGGER.finest("inside feature " + internalTypeName);
 
                 for (int i = 0; i < atts.getLength(); i++) {
                     String name = atts.getLocalName(i);
                     attributes.add(atts.getValue(i));
                     attributeNames.add(name);
                 }
 
                 if (!typeName.equalsIgnoreCase(internalTypeName)) {
                     if (attName.equals("")) {
                         LOGGER.finest("setting attName to " + localName);
                         attName = localName;
                     } else {
                         LOGGER.finest("adding " + localName + " to " + attName);
                         attName = attName + "/" + localName;
                     }
 
                     LOGGER.finest("attName now equals " + attName);
                 }
 
                 insideAttribute = true;
 
                 return;
             } else if (insideAttribute) {
                 LOGGER.finest("inside attribute");
             }
         } else {
             parent.startElement(namespaceURI, localName, qName, atts);
         }
     }
 
     /**
      * Reads the only internal characters read by pure GML parsers, which are
      * coordinates.  These coordinates are sent to the coordinates reader
      * class which interprets them appropriately, depending on the its current
      * state.
      *
      * @param ch Raw coordinate string from the GML document.
      * @param start Beginning character position of raw coordinate string.
      * @param length Length of the character string.
      *
      * @throws SAXException Some parsing error occurred while reading
      *         coordinates.
      */
     public void characters(char[] ch, int start, int length)
         throws SAXException {
         // the methods here read in both coordinates and coords and take the
         // grunt-work out of this task for geometry handlers.
         // See the documentation for CoordinatesReader to see what this entails
         String rawAttribute = new String(ch, start, length);
 
         if (insideAttribute) {
             try {
                 tempValue = new Integer(rawAttribute);
             } catch (NumberFormatException e1) {
                 try {
                     tempValue = new Double(rawAttribute);
                 } catch (NumberFormatException e2) {
                     tempValue = new String(rawAttribute);
                 }
             }
         } else {
             parent.characters(ch, start, length);
         }
     }
 
     /**
      * Checks for GML element end and - if not a coordinates element - sends it
      * directly on down the chain to the appropriate parent handler.  If it is
      * a coordinates (or coord) element, it uses internal methods to set the
      * current state of the coordinates reader appropriately.
      *
      * @param namespaceURI Namespace of the element.
      * @param localName Local name of the element.
      * @param qName Full name of the element, including namespace prefix.
      *
      * @throws SAXException Parsing error occurred while reading coordinates.
      */
     public void endElement(String namespaceURI, String localName, String qName)
         throws SAXException {
         if (localName.equals("Insert")) {
             insideInsert = false;
         }
 
         String internalTypeName = typeRepo.getInternalTypeName(localName,
                 namespaceURI);
 
         if (typeName.equals(internalTypeName)) {
             AttributeType[] attDef = new AttributeType[attributes.size()];
 
             for (int i = 0; i < attributes.size(); i++) {
                 attDef[i] = AttributeTypeFactory.newAttributeType((String) attributeNames
                         .get(i), attributes.get(i).getClass());
             }
 
             try {
		//Note localName is used here instead of typeName as typeName's
		//meaning is with the prefix for internal GeoServer types,
		//which we don't want passed to our datasources.
                 FeatureType schema = FeatureTypeFactory.newFeatureType(attDef,
                        localName, namespaceURI);
                 Feature feature = schema.create(attributes.toArray());
 
                 //currentFeature.setAttributes((Object []) attributes.toArray());
                 parent.feature(feature);
                 LOGGER.finest("resetting attName at end of feature");
                 attName = "";
             } catch (org.geotools.feature.SchemaException sve) {
                 throw new RuntimeException("problem creating schema", sve);
             } catch (org.geotools.feature.IllegalAttributeException ife) {
                 throw new RuntimeException("problem creating feature", ife);
             }
             insideFeature = false;
 
             //HACK: the local name stuff should be handled in geotools.
         } else if (insideAttribute
                 && !(localName.equals("lineStringMember")
                 || localName.equals("polygonMember")
                 || localName.equals("pointMember"))) {
             LOGGER.finest("end - inside attribute [" + tempValue + "]");
 
             if ((tempValue != null) && !tempValue.toString().trim().equals("")) {
                 attributes.add(tempValue);
                 attributeNames.add(attName);
 		tempValue = null;
             }
 
             int index = attName.lastIndexOf('/');
 
             if (index > -1) {
                 LOGGER.finest("removing " + attName.substring(index + 1));
                 attName = attName.substring(0, index);
             } else {
                 attName = "";
             }
 
             LOGGER.finest("attName now equals " + attName);
             insideAttribute = false;
         } else {
             parent.endElement(namespaceURI, localName, qName);
             LOGGER.finest("end - inside feature");
 
             //insideFeature = false;
         }
     }
 
     /**
      * Manages the start of a new main or sub geometry.  This method looks at
      * the status of the current handler and either returns a new sub-handler
      * (if the last one was successfully returned already) or passes the
      * element start notification along to the current handler as a sub
      * geometry notice.
      *
      * @param geometry The geometry from the child.
      */
     public void geometry(Geometry geometry) {
         if (insideFeature) {
             if (attName.equals("")) {
                 attributeNames.add("geometry");
             } else {
                 attributeNames.add(attName);
             }
 
             attributes.add(geometry);
             insideAttribute = false;
 
             int index = attName.lastIndexOf('/');
 
             if (index > -1) {
                 LOGGER.finest("removing " + attName.substring(index + 1));
                 attName = attName.substring(0, index);
             } else {
                 attName = "";
             }
 
             attName = "";
             LOGGER.finer("calling gmlFeatureFilter for geom " + geometry);
         } else {
             parent.geometry(geometry);
         }
     }
 }
