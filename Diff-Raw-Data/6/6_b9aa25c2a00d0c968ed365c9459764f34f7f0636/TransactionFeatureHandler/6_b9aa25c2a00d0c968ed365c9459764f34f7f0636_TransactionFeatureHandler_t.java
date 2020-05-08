 /* Copyright (c) 2001, 2003 TOPP - www.openplans.org.  All rights reserved.
  * This code is licensed under the GPL 2.0 license, availible at the root
  * application directory.
  */
 package org.vfny.geoserver.requests.wfs;
 
 import com.vividsolutions.jts.geom.Geometry;
 import org.geotools.feature.AttributeType;
 import org.geotools.feature.Feature;
 import org.geotools.feature.FeatureType;
 import org.geotools.feature.FeatureTypeFactory;
 import org.geotools.gml.GMLFilterFeature;
 import org.vfny.geoserver.global.Data;
 import org.vfny.geoserver.global.FeatureTypeInfo;
 import org.vfny.geoserver.requests.Requests;
 import org.xml.sax.Attributes;
 import org.xml.sax.SAXException;
 import java.util.List;
 import java.util.Vector;
 import java.util.logging.Logger;
 import javax.servlet.http.HttpServletRequest;
 
 
 /**
  * Uses SAX to extact a Transactional request from and incoming XML stream. It
  * now makes use of the FeatureType of the typename to parse the attribute
  * values correctly.  Right now this leads to a bit of loose parsing, for
  * example if the attributes passed in are in the wrong order we correct for
  * that.  If an attribute is passed in twice for some reason the code right
  * now will just use the second
  *
  * @author Chris Holmes, TOPP
 * @version $Id: TransactionFeatureHandler.java,v 1.20 2004/07/27 20:55:12 cholmesny Exp $
  */
 public class TransactionFeatureHandler extends GMLFilterFeature {
     //    implements ContentHandler, FilterHandler, GMLHandlerFeature {
 
     /** Class logger */
     private static Logger LOGGER = Logger.getLogger(
             "org.vfny.geoserver.requests");
 
     /** Stores current feature attributes. */
     private Object[] attributes;
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
 
     //private TypeRepository typeRepo = TypeRepository.getInstance();
     private Data catalog = null;
     private FeatureType curFeatureType;
     private AttributeType curAttributeType;
 
     /**
      * Constructor with parent, which must implement GMLHandlerJTS.
      *
      * @param parent The parent of this filter.
      * @param r DOCUMENT ME!
      */
     public TransactionFeatureHandler(TransactionFilterHandler parent,
         HttpServletRequest r) {
         super(parent);
         this.parent = parent;
         catalog = Requests.getWFS(r).getData();
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
             //REVISIT: more filtering here?  So we don't have to check as many
             //featureTypes?  Like add a 
             //!namespaceURI.equals("http://www.opengis.net/gml"); 
             //(not sure if that'd work, but something to that effect
             FeatureTypeInfo fType = catalog.getFeatureTypeInfo(localName,
                     namespaceURI);
             String internalTypeName = null;
 
             if (fType != null) {
                 internalTypeName = fType.getName();
             }
 
             if (!insideFeature) {
                 if ((internalTypeName == null) || (fType == null)) {
                     throw new SAXException(
                         "Could not find featureType with name " + localName
                         + ", and uri: " + namespaceURI);
                 }
 
                 //fix for insert ...
                 typeName = internalTypeName;
 
                 try {
                     curFeatureType = fType.getFeatureType();
                 } catch (java.io.IOException ioe) {
                     throw new SAXException(ioe);
                 }
 
                 int numAtts = curFeatureType.getAttributeTypes().length;
                 attributes = new Object[numAtts];
                 attributeNames = new Vector(numAtts);
 
                 //currentFeature = new FeatureFlat();
                 insideFeature = true;
                 tempValue = null;
                 LOGGER.finer("Starting a feature " + typeName);
             }
 
             //HACK: the local name stuff should be handled in geotools.
             if (insideFeature
                     && !((localName.equals("lineStringMember"))
                     || (localName.equals("polygonMember"))
                     || (localName.equals("pointMember")))) {
                 LOGGER.fine("inside feature " + internalTypeName);
 
                 //This is for feature attributes as xml attributes.  Is this
                 //valid in the xmlspec?  Commenting out for now as its messing
                 //up fids.  Change it back if for some reason we allow clients
                 //to specify their fid, but to do that we'd need to change the
                 //code anyways (store the fid and create the feature with it).
 
                 /* for (int i = 0; i < atts.getLength(); i++) {
                    String name = atts.getLocalName(i);
                    String attString = atts.getValue(i);
                    LOGGER.info("getting attribute type for " + name);
                    AttributeType type = curFeatureType.getAttributeType(name);
                    Object value = type.parse(attString);
                    attributes[curFeatureType.find(type)] = value;
                    attributeNames.add(name);
                    }*/
                 if (!typeName.equalsIgnoreCase(internalTypeName)) {
                     if (attName.equals("")) {
                         LOGGER.finest("setting attName to " + localName);
                         attName = localName;
                     } else {
                         LOGGER.finest("adding " + localName + " to " + attName);
                         attName = attName + "/" + localName;
                     }
 
                     //this may not work with nested attributes, but the rest of our
                     //code doesn't handle them either, so this can be revisited.
                     curAttributeType = curFeatureType.getAttributeType(attName);
 
                     if (curAttributeType == null) {
                         throw new SAXException(
                             "Could not find attributeType named " + attName
                             + "in featureType " + curFeatureType);
                     }
 
                     LOGGER.fine("attName now equals " + attName);
                     insideAttribute = true;
                 }
 
                 return;
             } else if (insideAttribute) {
                 LOGGER.finer("inside attribute");
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
         String rawAttribute = new String(ch, start, length);
         LOGGER.fine("we are inside attribute: " + insideAttribute
             + ", curAttType is " + curAttributeType + " curFeatureT: "
             + curFeatureType + " attName " + attName);
 
         if (insideAttribute && !rawAttribute.trim().equals("")) {
             tempValue = curAttributeType.parse(rawAttribute);
 
             //try {
             //    tempValue = new Integer(rawAttribute);
             //} catch (NumberFormatException e1) {
             //    try {
             //        tempValue = new Double(rawAttribute);
             //    } catch (NumberFormatException e2) {
             //        tempValue = new String(rawAttribute);
             //    }
             //}
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
      * @param namespaceURI NameSpaceInfo of the element.
      * @param localName Local name of the element.
      * @param qName Full name of the element, including namespace prefix.
      *
      * @throws SAXException Parsing error occurred while reading coordinates.
      * @throws RuntimeException DOCUMENT ME!
      */
     public void endElement(String namespaceURI, String localName, String qName)
         throws SAXException {
         if (localName.equals("Insert")) {
             insideInsert = false;
         }
 
         FeatureTypeInfo fType = catalog.getFeatureTypeInfo(localName,
                 namespaceURI);
         String internalTypeName = null;
 
         if (fType != null) {
             internalTypeName = fType.getName();
         }
 
         if (typeName.equals(internalTypeName)) {
             //AttributeType[] attDef = new AttributeType[attributes.size()];
             //for (int i = 0; i < attributes.size(); i++) {
             //    attDef[i] = AttributeTypeFactory.newAttributeType((String) attributeNames
             //            .get(i), attributes.get(i).getClass());
             //}
             try {
                 //TODO: This is a hack, setting the namespace here.  What
                 //should be done is that our datastores should be constructed
                 //with namespaces.  I just coded up some stuff that should do
                 //that, check DataStoreInfo, we add it to the connection params, which 
                 //should create it with Postgis and Oracle, but it doesn't seem to be
                 //working.  This should be good enough, it'd just be nice to be cleaner.
                 FeatureTypeFactory ftFactory = FeatureTypeFactory
                     .createTemplate(curFeatureType);
                 ftFactory.setNamespace(namespaceURI);
 
                 FeatureType schema = ftFactory.getFeatureType();
                 Feature feature = schema.create(attributes);
 
                 //currentFeature.setAttributes((Object []) attributes.toArray());
                 parent.feature(feature);
                 LOGGER.finest("resetting attName at end of feature");
                 attName = "";
                 LOGGER.finer("created feature: " + feature);
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
                 int insertPosition = curFeatureType.find(curAttributeType);
                 Object curAtt = attributes[insertPosition];
 
                 //REVISIT: If we ever support complex attributes then this can
                 //just create a list and add to it.
                 if (curAtt != null) {
                     throw new SAXException("Attempted to set attribute "
                         + attName + " twice, first with " + curAtt
                         + ", and then with " + tempValue + ".\n  Future "
                         + "versions of GeoServer may support complex attributes");
                 }
 
                 attributes[insertPosition] = tempValue;
                 tempValue = null;
                 attributeNames.add(attName);
             }
 
             int index = attName.lastIndexOf('/');
 
             if (index > -1) {
                 LOGGER.finest("removing " + attName.substring(index + 1));
                 attName = attName.substring(0, index);
             } else {
                 attName = "";
             }
 
             LOGGER.finer("attName now equals " + attName);
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
	    LOGGER.fine("geometry recieved is " + geometry);
            //curAttributeType = curFeatureType.getDefaultGeometry();
 
             if (attName.equals("")) {
                 attributeNames.add("geometry");
             } else {
                 attributeNames.add(attName);
             }
 
             int position = curFeatureType.find(curAttributeType);
             attributes[position] = geometry;
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
