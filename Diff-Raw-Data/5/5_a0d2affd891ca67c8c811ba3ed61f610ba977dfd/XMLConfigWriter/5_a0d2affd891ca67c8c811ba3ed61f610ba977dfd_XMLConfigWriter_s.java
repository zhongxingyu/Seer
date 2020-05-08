 /* Copyright (c) 2001, 2003 TOPP - www.openplans.org.  All rights reserved.
  * This code is licensed under the GPL 2.0 license, availible at the root
  * application directory.
  */
 package org.vfny.geoserver.global.xml;
 
 import com.vividsolutions.jts.geom.Envelope;
 import org.vfny.geoserver.global.ConfigurationException;
 import org.vfny.geoserver.global.dto.AttributeTypeInfoDTO;
 import org.vfny.geoserver.global.dto.ContactDTO;
 import org.vfny.geoserver.global.dto.DataDTO;
 import org.vfny.geoserver.global.dto.DataStoreInfoDTO;
 import org.vfny.geoserver.global.dto.FeatureTypeInfoDTO;
 import org.vfny.geoserver.global.dto.GeoServerDTO;
 import org.vfny.geoserver.global.dto.NameSpaceInfoDTO;
 import org.vfny.geoserver.global.dto.ServiceDTO;
 import org.vfny.geoserver.global.dto.StyleDTO;
 import org.vfny.geoserver.global.dto.WFSDTO;
 import org.vfny.geoserver.global.dto.WMSDTO;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.StringWriter;
 import java.io.Writer;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.logging.Logger;
 
 
 /**
  * XMLConfigWriter purpose.
  * 
  * <p>
  * This class is intended to store a configuration to be written and complete
  * the output to XML.
  * </p>
  * 
  * <p></p>
  *
  * @author dzwiers, Refractions Research, Inc.
  * @version $Id: XMLConfigWriter.java,v 1.32 2004/09/20 20:43:37 cholmesny Exp $
  */
 public class XMLConfigWriter {
     /** Used internally to create log information to detect errors. */
     private static final Logger LOGGER = Logger.getLogger(
             "org.vfny.geoserver.global");
 
     /**
      * XMLConfigWriter constructor.
      * 
      * <p>
      * Should never be called.
      * </p>
      */
     private XMLConfigWriter() {
     }
 
     public static void store(DataDTO data, File root)
         throws ConfigurationException {
         LOGGER.fine("In method store DataDTO");
 
         if (data == null) {
             throw new ConfigurationException("DataDTO is null: cannot write.");
         }
 
         WriterUtils.initFile(root, true);
 
         File configDir = WriterUtils.initFile(new File(root, "WEB-INF/"), true);
 
         File catalogFile = WriterUtils.initWriteFile(new File(configDir,
                     "catalog.xml"), false);
 
         try {
             FileWriter fw = new FileWriter(catalogFile);
             storeCatalog(new WriterHelper(fw), data);
             fw.close();
         } catch (IOException e) {
             throw new ConfigurationException("Store" + root, e);
         }
 
         File dataDir = WriterUtils.initFile(new File(root, "data/"), true);
         File featureTypeDir = WriterUtils.initFile(new File(dataDir,
                     "featureTypes/"), true);
         storeFeatures(featureTypeDir, data);
     }
 
     public static void store(WMSDTO wms, WFSDTO wfs, GeoServerDTO geoServer,
         File root) throws ConfigurationException {
         LOGGER.fine("In method store WMSDTO,WFSDTO, GeoServerDTO");
 
         if (geoServer == null) {
             throw new ConfigurationException(
                 "null parameter in store(WFSDTO,WMSDTO, GeoServerDTO): cannot write.");
         }
 
         WriterUtils.initFile(root, true);
 
         File configDir = WriterUtils.initFile(new File(root, "WEB-INF/"), true);
         File configFile = WriterUtils.initWriteFile(new File(configDir,
                     "services.xml"), false);
 
         try {
             FileWriter fw = new FileWriter(configFile);
             storeServices(new WriterHelper(fw), wms, wfs, geoServer);
             fw.close();
         } catch (IOException e) {
             throw new ConfigurationException("Store" + root, e);
         }
     }
 
     public static void store(WMSDTO wms, WFSDTO wfs, GeoServerDTO geoServer,
         DataDTO data, File root) throws ConfigurationException {
         store(wms, wfs, geoServer, root);
         store(data, root);
     }
 
     /**
      * storeServices purpose.
      * 
      * <p>
      * Writes the services.xml file from the model in memory.
      * </p>
      *
      * @param cw The Configuration Writer
      * @param wms DOCUMENT ME!
      * @param wfs DOCUMENT ME!
      * @param geoServer DOCUMENT ME!
      *
      * @throws ConfigurationException When an IO exception occurs.
      */
     protected static void storeServices(WriterHelper cw, WMSDTO wms,
         WFSDTO wfs, GeoServerDTO geoServer) throws ConfigurationException {
         LOGGER.fine("In method storeServices");
         cw.writeln("<?config.xml version=\"1.0\" encoding=\"UTF-8\"?>");
         cw.comment("Service level configuration");
         cw.openTag("serverConfiguration");
 
         GeoServerDTO g = geoServer;
 
         if (g != null) {
             cw.openTag("global");
 
             if (g.getLoggingLevel() != null) {
                 cw.comment(
                     "Defines the logging level.  Common options are SEVERE,\n"
                     + "WARNING, INFO, CONFIG, FINER, FINEST, in order of\n"
                     + "Increasing statements logged.");
                 cw.textTag("loggingLevel", g.getLoggingLevel().getName());
             }
 
             /*if(g.getBaseUrl()!=null && g.getBaseUrl()!=""){
                cw.comment("The base URL where this servlet will run.  If running locally\n"+
                "then http://localhost:8080 (or whatever port you're running on)\n"+
                "should work.  If you are serving to the world then this must be\n"+
                "the location where the geoserver servlets appear");
                cw.textTag("URL",g.getBaseUrl());
                }*/
             cw.comment("Sets the max number of Features returned by GetFeature");
             cw.valueTag("maxFeatures", "" + g.getMaxFeatures());
             cw.comment("Whether newlines and indents should be returned in \n"
                 + "XML responses.  Default is false");
             cw.valueTag("verbose", "" + g.isVerbose());
 			cw.comment("Whether the Service Exceptions returned to clients should contain\n"
 							+ "full java stack traces (useful for debugging). ");
 			cw.valueTag("verboseExceptions", "" + g.isVerboseExceptions());
             cw.comment(
                 "Sets the max number of decimal places past the zero returned in\n"
                 + "a GetFeature response.  Default is 4");
             cw.valueTag("numDecimals", "" + g.getNumDecimals());
 
             if (g.getCharSet() != null) {
                 cw.comment(
                     "Sets the global character set.  This could use some more testing\n"
                     + "from international users, but what it does is sets the encoding\n"
                     + "globally for all postgis database connections (the charset tag\n"
                     + "in FeatureTypeConfig), as well as specifying the encoding in the return\n"
                     + "config.xml header and mime type.  The default is UTF-8.  Also be warned\n"
                     + "that GeoServer does not check if the CharSet is valid before\n"
                     + "attempting to use it, so it will fail miserably if a bad charset\n"
                     + "is used.");
                 cw.valueTag("charSet", g.getCharSet().toString());
             }
 
             if ((g.getSchemaBaseUrl() != null) && (g.getSchemaBaseUrl() != "")) {
                 cw.comment(
                     "Define a base url for the location of the wfs schemas.\n"
                     + "By default GeoServer loads and references its own at\n"
                     + "<URL>/data/capabilities. Uncomment to enable.  The\n"
                     + "standalone Tomcat server needs SchemaBaseUrl defined\n"
                     + "for validation.");
                 cw.textTag("SchemaBaseUrl", g.getSchemaBaseUrl());
             }
 
             if ((g.getAdminUserName() != null) && (g.getAdminUserName() != "")) {
                 cw.comment(
                     "Defines the user name of the administrator for log in\n"
                     + "to the web based administration tool.");
                 cw.textTag("adminUserName", g.getAdminUserName());
             }
 
             if ((g.getAdminPassword() != null) && (g.getAdminPassword() != "")) {
                 cw.comment(
                     "Defines the password of the administrator for log in\n"
                     + "to the web based administration tool.");
                 cw.textTag("adminPassword", g.getAdminPassword());
             }
 
             if (g.getContact() != null) {
                 storeContact(g.getContact(), cw);
             }
 
             cw.closeTag("global");
         }
 
         if (!((wfs == null) && (wms == null))) {
             cw.openTag("services");
 
             if (wfs != null) {
                 storeService(wfs, cw);
             }
 
             if (wms != null) {
                 storeService(wms, cw);
             }
 
             // Z39.50 is not used in the current system.
             cw.closeTag("services");
         }
 
         cw.closeTag("serverConfiguration");
     }
 
     /**
      * storeContact purpose.
      * 
      * <p>
      * Writes a contact into the WriterUtils provided from the ContactConfig
      * provided.
      * </p>
      *
      * @param c The ContactConfig to write.
      * @param cw The Configuration Writer
      *
      * @throws ConfigurationException When an IO exception occurs.
      */
     protected static void storeContact(ContactDTO c, WriterHelper cw)
         throws ConfigurationException {
         LOGGER.fine("In method storeContact");
 
         if ((c != null) && !c.equals(new ContactDTO())) {
             cw.openTag("ContactInformation");
             cw.openTag("ContactPersonPrimary");
             cw.textTag("ContactPerson", c.getContactPerson());
             cw.textTag("ContactOrganization", c.getContactOrganization());
             cw.closeTag("ContactPersonPrimary");
             cw.textTag("ContactPosition", c.getContactPosition());
             cw.openTag("ContactAddress");
             cw.textTag("AddressType", c.getAddressType());
             cw.textTag("Address", c.getAddress());
             cw.textTag("City", c.getAddressCity());
             cw.textTag("StateOrProvince", c.getAddressState());
             cw.textTag("PostCode", c.getAddressPostalCode());
             cw.textTag("Country", c.getAddressCountry());
             cw.closeTag("ContactAddress");
             cw.textTag("ContactVoiceTelephone", c.getContactVoice());
             cw.textTag("ContactFacsimileTelephone", c.getContactFacsimile());
             cw.textTag("ContactElectronicMailAddress", c.getContactEmail());
             cw.closeTag("ContactInformation");
         }
     }
 
     /**
      * storeService purpose.
      * 
      * <p>
      * Writes a service into the WriterUtils provided from the WFS or WMS
      * object provided.
      * </p>
      *
      * @param obj either a WFS or WMS object.
      * @param cw The Configuration Writer
      *
      * @throws ConfigurationException When an IO exception occurs or the object
      *         provided is not of the correct type.
      */
     protected static void storeService(Object obj, WriterHelper cw)
         throws ConfigurationException {
         LOGGER.fine("In method storeService");
 
         ServiceDTO s = null;
         String u = null;
         String t = "";
         boolean gml = false;
         boolean srsXmlStyle = false;
         int serviceLevel = 0;
 
         if (obj instanceof WFSDTO) {
             WFSDTO w = (WFSDTO) obj;
             s = w.getService();
             t = "WFS";
             gml = w.isGmlPrefixing();
             srsXmlStyle = w.isSrsXmlStyle();
             serviceLevel = w.getServiceLevel();
         } else if (obj instanceof WMSDTO) {
             WMSDTO w = (WMSDTO) obj;
             s = w.getService();
             t = "WMS";
         } else {
             throw new ConfigurationException("Invalid object: not WMS of WFS");
         }
 
         Map atrs = new HashMap();
         atrs.put("type", t);
         atrs.put("enabled", s.isEnabled() + "");
         cw.openTag("service", atrs);
         cw.comment(
             "ServiceDTO elements, needed for the capabilities document\n"
             + "Title and OnlineResource are the two required");
 
         if ((s.getName() != null) && (s.getName() != "")) {
             cw.textTag("name", s.getName());
         }
 
         if ((s.getTitle() != null) && (s.getTitle() != "")) {
             cw.textTag("title", s.getTitle());
         }
 
         if ((s.getAbstract() != null) && (s.getAbstract() != "")) {
             cw.textTag("abstract", s.getAbstract());
         }
 
         if (s.getKeywords().length != 0) {
             cw.openTag("keywords");
 
             for (int i = 0; i < s.getKeywords().length; i++) {
                 cw.textTag("keyword", (s.getKeywords())[i].toString());
             }
 
             cw.closeTag("keywords");
         }
 
         if (s.getOnlineResource() != null) {
             cw.textTag("onlineResource", s.getOnlineResource().toString());
         }
 
         if ((s.getFees() != null) && (s.getFees() != "")) {
             cw.textTag("fees", s.getFees());
         }
 
         if ((s.getAccessConstraints() != null)
                 && (s.getAccessConstraints() != "")) {
             cw.textTag("accessConstraints", s.getAccessConstraints());
         }
 
         if (gml) {
 			cw.valueTag("gmlPrefixing", gml + "");
         }
         
        if (srsXmlStyle) {
 			cw.valueTag("srsXmlStyle", srsXmlStyle + "");
        }
 
         if (serviceLevel != 0) {
             cw.valueTag("serviceLevel", serviceLevel + "");
         }
 
         if ((s.getMaintainer() != null) && (s.getMaintainer() != "")) {
             cw.textTag("maintainer", s.getMaintainer());
         }
 
         cw.closeTag("service");
     }
 
     /**
      * storeCatalog purpose.
      * 
      * <p>
      * Writes a catalog into the WriterUtils provided from Data provided in
      * memory.
      * </p>
      *
      * @param cw The Configuration Writer
      * @param data DOCUMENT ME!
      *
      * @throws ConfigurationException When an IO exception occurs.
      */
     protected static void storeCatalog(WriterHelper cw, DataDTO data)
         throws ConfigurationException {
         LOGGER.fine("In method storeCatalog");
         cw.writeln("<?config.xml version=\"1.0\" encoding=\"UTF-8\"?>");
         cw.openTag("catalog");
 
         if (data.getDataStores().size() != 0) {
             cw.openTag("datastores");
             cw.comment(
                 "a datastore configuration element serves as a common data source connection\n"
                 + "parameters repository for all featuretypes it holds.");
 
             Iterator i = data.getDataStores().keySet().iterator();
 
             while (i.hasNext()) {
                 String s = (String) i.next();
                 DataStoreInfoDTO ds = (DataStoreInfoDTO) data.getDataStores()
                                                              .get(s);
 
                 if (ds != null) {
                     storeDataStore(cw, ds);
                 }
             }
 
             cw.closeTag("datastores");
         }
 
         if (data.getNameSpaces().size() != 0) {
             cw.comment("Defines namespaces to be used by the datastores.");
             cw.openTag("namespaces");
 
             Iterator i = data.getNameSpaces().keySet().iterator();
 
             while (i.hasNext()) {
                 String s = (String) i.next();
                 NameSpaceInfoDTO ns = (NameSpaceInfoDTO) data.getNameSpaces()
                                                              .get(s);
 
                 if (ns != null) {
                     storeNameSpace(cw, ns);
                 }
             }
 
             cw.closeTag("namespaces");
         }
 
         if (data.getStyles().size() != 0) {
             cw.comment(
                 "Defines the style ids to be used by the wms.  The files must be\n"
                 + "contained in geoserver/misc/wms/styles.  We're working on finding\n"
                 + "a better place for them, but for now that's where you must put them\n"
                 + "if you want them on the server.");
             cw.openTag("styles");
 
             Iterator i = data.getStyles().keySet().iterator();
 
             while (i.hasNext()) {
                 String s = (String) i.next();
                 StyleDTO st = (StyleDTO) data.getStyles().get(s);
 
                 if (st != null) {
                     storeStyle(cw, st);
                 }
             }
 
             cw.closeTag("styles");
         }
 
         cw.closeTag("catalog");
     }
 
     /**
      * storeDataStore purpose.
      * 
      * <p>
      * Writes a DataStoreInfo into the WriterUtils provided.
      * </p>
      *
      * @param cw The Configuration Writer
      * @param ds The Datastore.
      *
      * @throws ConfigurationException When an IO exception occurs.
      */
     protected static void storeDataStore(WriterHelper cw, DataStoreInfoDTO ds)
         throws ConfigurationException {
         LOGGER.fine("In method storeDataStore");
 
         Map temp = new HashMap();
 
         if (ds.getId() != null) {
             temp.put("id", ds.getId());
         }
 
         temp.put("enabled", ds.isEnabled() + "");
 
         if (ds.getNameSpaceId() != null) {
             temp.put("namespace", ds.getNameSpaceId());
         }
 
         cw.openTag("datastore", temp);
 
         if ((ds.getAbstract() != null) && (ds.getAbstract() != "")) {
             cw.textTag("abstract", ds.getAbstract());
         }
 
         if ((ds.getTitle() != null) && (ds.getTitle() != "")) {
             cw.textTag("title", ds.getTitle());
         }
 
         if (ds.getConnectionParams().size() != 0) {
             cw.openTag("connectionParams");
 
             Iterator i = ds.getConnectionParams().keySet().iterator();
             temp = new HashMap();
 
             while (i.hasNext()) {
                 String key = (String) i.next();
                 temp.put("name", key);
                 temp.put("value", ds.getConnectionParams().get(key).toString());
                 cw.attrTag("parameter", temp);
             }
 
             cw.closeTag("connectionParams");
         }
 
         cw.closeTag("datastore");
     }
 
     /**
      * storeNameSpace purpose.
      * 
      * <p>
      * Writes a NameSpaceInfoDTO into the WriterUtils provided.
      * </p>
      *
      * @param cw The Configuration Writer
      * @param ns The NameSpaceInfo.
      *
      * @throws ConfigurationException When an IO exception occurs.
      */
     protected static void storeNameSpace(WriterHelper cw, NameSpaceInfoDTO ns)
         throws ConfigurationException {
         LOGGER.fine("In method storeNameSpace");
 
         Map attr = new HashMap();
 
         if ((ns.getUri() != null) && (ns.getUri() != "")) {
             attr.put("uri", ns.getUri());
         }
 
         if ((ns.getPrefix() != null) && (ns.getPrefix() != "")) {
             attr.put("prefix", ns.getPrefix());
         }
 
         if (ns.isDefault()) {
             attr.put("default", "true");
         }
 
         if (attr.size() != 0) {
             cw.attrTag("namespace", attr);
         }
     }
 
     /**
      * storeStyle purpose.
      * 
      * <p>
      * Writes a StyleDTO into the WriterUtils provided.
      * </p>
      *
      * @param cw The Configuration Writer
      * @param s The StyleDTO.
      *
      * @throws ConfigurationException When an IO exception occurs.
      */
     protected static void storeStyle(WriterHelper cw, StyleDTO s)
         throws ConfigurationException {
         LOGGER.fine("In method storeStyle: " + s);
 
         Map attr = new HashMap();
 
         if ((s.getId() != null) && (s.getId() != "")) {
             attr.put("id", s.getId());
         }
 
         if (s.getFilename() != null) {
             attr.put("filename", s.getFilename().getName());
         }
 
         if (s.isDefault()) {
             attr.put("default", "true");
         }
 
 		LOGGER.fine("storing style " + attr);
         if (attr.size() != 0) {
             cw.attrTag("style", attr);
         }
     }
 
     /**
      * storeStyle purpose.
      * 
      * <p>
      * Sets up writing FeatureTypes into their Directories.
      * </p>
      *
      * @param dir The FeatureTypes directory
      * @param data DOCUMENT ME!
      *
      * @throws ConfigurationException When an IO exception occurs.
      *
      * @see storeFeature(FeatureTypeInfo,File)
      */
     protected static void storeFeatures(File dir, DataDTO data)
         throws ConfigurationException {
         LOGGER.fine("In method storeFeatures");
 
 		// write them
         Iterator i = data.getFeaturesTypes().keySet().iterator();
         while (i.hasNext()) {
             String s = (String) i.next();
             FeatureTypeInfoDTO ft = (FeatureTypeInfoDTO) data.getFeaturesTypes()
                                                              .get(s);
 
             if (ft != null) {
                 File dir2 = WriterUtils.initWriteFile(new File(dir,
                             ft.getDirName()), true);
                 
                 storeFeature(ft, dir2);
                 
                 if (ft.getSchemaAttributes() != null) {
                     LOGGER.fine( ft.getKey() +" writing schema.xml w/ "+ft.getSchemaAttributes().size() );
                     storeFeatureSchema(ft, dir2);
                 }
             }
         }
         
         // delete old ones that are not overwritten
 		File[] fa = dir.listFiles();
 		for(int j=0;j<fa.length;j++){
 			// find dir name
 			i = data.getFeaturesTypes().values().iterator();
 			FeatureTypeInfoDTO fti = null;
 			while(fti==null && i.hasNext()){
 				FeatureTypeInfoDTO ft = (FeatureTypeInfoDTO)i.next();
 				if(ft.getDirName().equals(fa[j].getName())){
 					fti = ft;
 				}
 			}
 			if(fti == null){
 				//delete it
 				File[] t = fa[j].listFiles();
 				if (t != null) {
 				    for(int x=0;x<t.length;x++)
 					t[x].delete();
 				}
 				fa[j].delete();
 			}
 		}
     }
 
     /**
      * storeStyle purpose.
      * 
      * <p>
      * Writes a FeatureTypes into it's Directory.
      * </p>
      *
      * @param ft DOCUMENT ME!
      * @param dir The particular FeatureTypeInfo directory
      *
      * @throws ConfigurationException When an IO exception occurs.
      *
      * @see storeFeatures(File)
      */
     protected static void storeFeature(FeatureTypeInfoDTO ft, File dir)
         throws ConfigurationException {
         LOGGER.fine("In method storeFeature");
 
         File f = WriterUtils.initWriteFile(new File(dir, "info.xml"), false);
 
         try {
             FileWriter fw = new FileWriter(f);
             WriterHelper cw = new WriterHelper(fw);
             Map m = new HashMap();
 
             if ((ft.getDataStoreId() != null) && (ft.getDataStoreId() != "")) {
                 m.put("datastore", ft.getDataStoreId());
             }
 
             cw.openTag("featureType", m);
 
             if ((ft.getName() != null) && (ft.getName() != "")) {
                 cw.textTag("name", ft.getName());
             }
 
             cw.comment("native wich EPGS code for the FeatureTypeInfoDTO");
             cw.textTag("SRS", ft.getSRS() + "");
 
             if ((ft.getTitle() != null) && (ft.getTitle() != "")) {
                 cw.textTag("title", ft.getTitle());
             }
 
             if ((ft.getAbstract() != null) && (ft.getAbstract() != "")) {
                 cw.textTag("abstract", ft.getAbstract());
             }
 
             cw.valueTag("numDecimals", ft.getNumDecimals() + "");
 
             if ((ft.getKeywords() != null) && (ft.getKeywords().size() != 0)) {
                 String s = "";
                 Iterator i = ft.getKeywords().iterator();
 
                 if (i.hasNext()) {
                     s = i.next().toString();
 
                     while (i.hasNext()) {
                         s = s + ", " + i.next().toString();
                     }
                 }
 
                 cw.textTag("keywords", s);
             }
 
             if (ft.getLatLongBBox() != null) {
                 m = new HashMap();
 
                 Envelope e = ft.getLatLongBBox();
 
                 // from creation, isn't stored otherwise
                 if (!e.isNull()) {
                     m.put("dynamic", "false");
                     m.put("minx", e.getMinX() + "");
                     m.put("miny", e.getMinY() + "");
                     m.put("maxx", e.getMaxX() + "");
                     m.put("maxy", e.getMaxY() + "");
                 } else {
                     m.put("dynamic", "true");
                 }
 
                 cw.attrTag("latLonBoundingBox", m);
             }
 
             if ((ft.getDefaultStyle() != null) && (ft.getDefaultStyle() != "")) {
                 cw.comment(
                     "the default style this FeatureTypeInfoDTO can be represented by.\n"
                     + "at least must contain the \"default\" attribute ");
                 m = new HashMap();
                 m.put("default", ft.getDefaultStyle());
                 cw.attrTag("styles", m);
             }
 
             if (ft.getDefinitionQuery() != null) {
                 cw.openTag("definitionQuery");
 
                 StringWriter sw = new StringWriter();
 
                 /*
                  * @TODO replace with current implementation.
                  */
                 org.geotools.filter.XMLEncoder xe = new org.geotools.filter.XMLEncoder(sw);
                 xe.encode(ft.getDefinitionQuery());
                 cw.writeln(sw.toString());
                 cw.closeTag("definitionQuery");
             }
 
             cw.closeTag("featureType");
             fw.close();
         } catch (IOException e) {
             throw new ConfigurationException(e);
         }
     }
 
     protected static void storeFeatureSchema(FeatureTypeInfoDTO fs, File dir)
         throws ConfigurationException {
 
         if ((fs.getSchemaBase() == null) || (fs.getSchemaBase() == "")) {
             //LOGGER.info( "No schema base" );
             LOGGER.fine( fs.getKey() + " has not schemaBase");            
             return;
         }
         
         if ((fs.getSchemaName() == null) || (fs.getSchemaName() == "")) {                   
             // Should assume Null?
             //LOGGER.info( "No schema name" ); // Do we even have a field for this?
             LOGGER.fine( fs.getKey() + " has not schemaName");
             return;
         }
         
         File f = WriterUtils.initWriteFile(new File(dir, "schema.xml"), false);
         try {
             FileWriter fw = new FileWriter(f);
             storeFeatureSchema(fs, fw);
             fw.close();
         } catch (IOException e) {
             throw new ConfigurationException(e);
         }
     }
 
     public static void storeFeatureSchema(FeatureTypeInfoDTO fs, Writer w)
         throws ConfigurationException {
         WriterHelper cw = new WriterHelper(w);
         HashMap m = new HashMap();
         String t = fs.getSchemaName();
 
         if (t != null) {
             if (!"_Type".equals(t.substring(t.length() - 5))) {
                 t = t + "_Type";
             }
 
             m.put("name", t);
         }
 
         cw.openTag("xs:complexType", m);
         cw.openTag("xs:complexContent");
         m = new HashMap();
         t = fs.getSchemaBase();
 
         if (t != null) {
             m.put("base", t);
         }
 
         cw.openTag("xs:extension", m);
         cw.openTag("xs:sequence");
 
         for (int i = 0; i < fs.getSchemaAttributes().size(); i++) {
             AttributeTypeInfoDTO ati = (AttributeTypeInfoDTO) fs.getSchemaAttributes()
                                                                 .get(i);
             m = new HashMap();
             m.put("nillable", "" + ati.isNillable());
             m.put("minOccurs", "" + ati.getMinOccurs());
             m.put("maxOccurs", "" + ati.getMaxOccurs());
 
             NameSpaceTranslator nst1 = NameSpaceTranslatorFactory.getInstance()
                                                                  .getNameSpaceTranslator("xs");
             NameSpaceTranslator nst2 = NameSpaceTranslatorFactory.getInstance()
                                                                  .getNameSpaceTranslator("gml");
 
             if (!ati.isComplex()) {
                 if (ati.getName() == ati.getType()) {
                     String r = "";
                     NameSpaceElement nse = nst1.getElement(ati.getType());
 
                     if (nse == null) {
                         nse = nst2.getElement(ati.getType());
                     }
 
                     r = nse.getQualifiedTypeRefName();
                     m.put("ref", r);
                 } else {
                     m.put("name", ati.getName());
 
                     String r = "";
                     NameSpaceElement nse = nst1.getElement(ati.getType());
 
                     if (nse == null) {
                         nse = nst2.getElement(ati.getType());
                     }
 
                     r = nse.getQualifiedTypeRefName();
 
                     m.put("type", r);
                 }
 
                 cw.attrTag("xs:element", m);
             } else {
                 m.put("name", ati.getName());
                 cw.openTag("xs:element", m);
                 cw.writeln(ati.getType());
                 cw.closeTag("xs:element");
             }
         }
 
         cw.closeTag("xs:sequence");
         cw.closeTag("xs:extension");
         cw.closeTag("xs:complexContent");
         cw.closeTag("xs:complexType");
     }
 }
 
 
 /**
  * WriterUtils purpose.
  * 
  * <p>
  * This is a static class which is used by XMLConfigWriter for File IO
  * validation tests.
  * </p>
  * 
  * <p></p>
  *
  * @author dzwiers, Refractions Research, Inc.
  * @version $Id: XMLConfigWriter.java,v 1.32 2004/09/20 20:43:37 cholmesny Exp $
  */
 class WriterUtils {
     /** Used internally to create log information to detect errors. */
     private static final Logger LOGGER = Logger.getLogger(
             "org.vfny.geoserver.global");
 
     /**
      * WriterUtils constructor.
      * 
      * <p>
      * Static class, should never be used.
      * </p>
      */
     private WriterUtils() {
     }
 
     /**
      * initFile purpose.
      * 
      * <p>
      * Checks to ensure the handle exists. If the handle is a directory and not
      * created, it is created
      * </p>
      *
      * @param f the File handle
      * @param isDir true when the handle is intended to be a directory.
      *
      * @return The file passed in.
      *
      * @throws ConfigurationException When an IO error occurs or the handle is
      *         invalid.
      */
     public static File initFile(File f, boolean isDir)
         throws ConfigurationException {
         if (!f.exists()) {
             LOGGER.fine("Creating File: " + f.toString());
 
             if (isDir) {
                 if (!f.mkdir()) {
                     throw new ConfigurationException(
                         "Path specified does not have a valid file.\n" + f
                         + "\n\n");
                 }
             } else {
                 try {
                     if (!f.createNewFile()) {
                         throw new ConfigurationException(
                             "Path specified does not have a valid file.\n" + f
                             + "\n\n");
                     }
                 } catch (IOException e) {
                     throw new ConfigurationException(e);
                 }
             }
         }
 
         if (isDir && !f.isDirectory()) {
             throw new ConfigurationException(
                 "Path specified does not have a valid file.\n" + f + "\n\n");
         }
 
         if (!isDir && !f.isFile()) {
             throw new ConfigurationException(
                 "Path specified does not have a valid file.\n" + f + "\n\n");
         }
 
         LOGGER.fine("File is valid: " + f);
 
         return f;
     }
 
     /**
      * initFile purpose.
      * 
      * <p>
      * Checks to ensure the handle exists and can be writen to. If the handle
      * is a directory and not created, it is created
      * </p>
      *
      * @param f the File handle
      * @param isDir true when the handle is intended to be a directory.
      *
      * @return The file passed in.
      *
      * @throws ConfigurationException When an IO error occurs or the handle is
      *         invalid.
      */
     public static File initWriteFile(File f, boolean isDir)
         throws ConfigurationException {
         initFile(f, isDir);
 
         if (!f.canWrite()) {
             throw new ConfigurationException("Cannot Write to file: "
                 + f.toString());
         }
 
         return f;
     }
 }
