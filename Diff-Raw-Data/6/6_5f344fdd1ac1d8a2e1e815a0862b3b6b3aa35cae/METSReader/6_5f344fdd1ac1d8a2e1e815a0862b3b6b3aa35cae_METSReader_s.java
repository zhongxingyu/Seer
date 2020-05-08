 package fedora.services.diringest.sip2fox;
 
 import java.io.*;
 import java.net.*;
 import java.util.*;
 import javax.xml.parsers.*;
 import org.apache.log4j.*;
 import org.xml.sax.*;
 import org.xml.sax.helpers.*;
 
 import fedora.services.diringest.common.*;
 
 public class METSReader extends DefaultHandler {
 
     public static final String METS = "http://www.loc.gov/METS/";
     public static final String XLINK = "http://www.w3.org/1999/xlink";
     public static final String INLINE_XML_MIMETYPE = "text/xml";
 
     private static final Logger logger =
             Logger.getLogger(METSReader.class.getName());
 
     private TreeNode m_root;
     private DataResolver m_dataResolver;
 
     private DivNode m_currentDiv;
 
     private Map m_contentMap;
 
     private Map m_prefixMap;
     private List m_prefixList;
 
     private String m_currentID;
     private String m_currentLabel;
     private String m_currentMIME;
     private String m_currentLocator;
     private String m_currentLocatorType;
     private File m_xmlDataFile;
     private List m_filesToDelete;
     private Writer m_xmlData;
 
     public METSReader(InputStream xml, DataResolver dataResolver) throws Exception {
         m_dataResolver = dataResolver;
         m_contentMap = new HashMap();
         m_prefixMap = new HashMap();
         m_prefixList = new ArrayList();
         m_filesToDelete = new ArrayList();
         SAXParserFactory spf = SAXParserFactory.newInstance();
         spf.setNamespaceAware(true);
         SAXParser parser = spf.newSAXParser();
         parser.parse(xml, this);
     }
 
     public TreeNode getRoot() {
         return m_root;
     }
 
     /**
      * Clean up temporary storage area used for disk-backed access to inline
      * XML datastreams.
      */
     public void close() throws IOException {
         logger.info("Deleting " + m_filesToDelete.size() + " temporary file(s).");
         for (int i = 0; i < m_filesToDelete.size(); i++) {
             File f = (File) m_filesToDelete.get(i);
             f.delete();
         }
     }
 
     public void startElement(String uri, 
                              String localName, 
                              String qName, 
                              Attributes a) throws SAXException {
         if (uri.equals(METS) && localName.equals("dmdSec") && m_currentID == null) {
             // Starting a dmdSec
             m_currentID = a.getValue("", "ID");
             if (m_currentID == null) {
                 throw new SAXException("dmdSec element must have an ID attribute");
             }
             m_currentLabel = a.getValue("", "LABEL");
             m_currentMIME = a.getValue("", "MIMETYPE");
             logger.info("Started parsing dmdSec (ID = " + m_currentID + ")");
         } else if (m_currentID != null) {
             // Inside a dmdSec or file
             if (uri.equals(METS) && localName.equals("xmlData") && m_xmlData == null) {
                 // Starting an xmlData 
                 try {
                     m_xmlDataFile = File.createTempFile("diringest-xmlData", null);
                     m_xmlDataFile.deleteOnExit();
                     m_xmlData = new OutputStreamWriter(new FileOutputStream(m_xmlDataFile), "UTF-8");
                     logger.info("Started buffering xmlData for " + m_currentID + " to file: " + m_xmlDataFile.getPath());
                 } catch (Exception e) {
                     String msg = "Unable to create temporary file for xmlData";
                     logger.warn(msg, e);
                     throw new SAXException(msg);
                 }
             } else if (uri.equals(METS) && localName.equals("FLocat")) {
                 m_currentLocator = a.getValue(XLINK, "href");
                 logger.info("Parsing FLocat with xlink:href = " + m_currentLocator);
                 if (m_currentLocator == null) {
                     throw new SAXException("FLocat element must have an xlink:href attribute");
                 }
                 m_currentLocatorType = a.getValue("", "LOCTYPE");
             } else if (m_xmlData != null) {
                 // inside xmlData
                 try {
                     writeElementStart(uri, localName, qName, a, m_xmlData);
                 } catch (Exception e) {
                     String msg = "Unable to write element start for xmlData";
                     logger.warn(msg, e);
                     throw new SAXException(msg);
                 }
             }
         } else if (uri.equals(METS) && localName.equals("file")) {
             m_currentID = a.getValue("", "ID");
             m_currentMIME = a.getValue("", "MIMETYPE");
             if (m_currentID == null || m_currentMIME == null) {
                 throw new SAXException("file element must have ID and MIMETYPE attributes");
             }
             logger.info("Parsing file with ID = " + m_currentID);
         } else if (uri.equals(METS) && localName.equals("div")) {
             String label = a.getValue("", "LABEL");
             String type = a.getValue("", "TYPE");
             logger.info("Parsing div with TYPE = " + type + ", LABEL = " + label);
             // if has no parent, this is the root
             if (m_currentDiv == null) {
                 m_currentDiv = new DivNode(null, label, type);
                 m_root = m_currentDiv;
             } else {
                 // the div we're inside is the parent of this one
                 DivNode newDiv = new DivNode(m_currentDiv, label, type);
                 m_currentDiv.addChild(newDiv);
                 m_currentDiv = newDiv;
             }
             // add any DMDID references
             String dmdIDs = a.getValue("DMDID");
             if (dmdIDs != null && dmdIDs.length() > 0) {
                 String[] ids = dmdIDs.split(" ");
                 if (ids.length == 0) {
                     ids = new String[1];
                     ids[0] = dmdIDs;
                 }
                 for (int i = 0; i < ids.length; i++) {
                     // make a sub-div
                     m_currentDiv.addDMD(getSIPContent(ids[i]));
                 }
             }
         } else if (uri.equals(METS) && localName.equals("fptr")) {
             String contentID = a.getValue("", "FILEID");
             if (contentID == null) {
                 throw new SAXException("METS:fptr must have a FILEID attribute");
             }
             if (m_currentDiv == null) {
                 throw new SAXException("METS:fptr must occur inside a div element");
             }
             m_currentDiv.setSIPContent(getSIPContent(contentID));
         }
     }
 
     private SIPContent getSIPContent(String contentID) throws SAXException {
         SIPContent c = (SIPContent) m_contentMap.get(contentID);
         if (c == null) {
             throw new SAXException("SIP content referenced from structMap was not found in dmdSec or fileSec: " + contentID);
         }
         return c;
     }
 
     public void characters(char[] ch, int start, int length) throws SAXException {
         if (m_xmlData != null) {
             try {
                 m_xmlData.write(ch, start, length);
             } catch (Exception e) {
                 String msg = "Unable to write characters for xmlData";
                 logger.warn(msg, e);
                 throw new SAXException(msg);
             }
         }
     }
 
     private void writeElementStart(String uri,
                                     String localName,
                                     String qName,
                                     Attributes a,
                                     Writer out) throws IOException {
         out.write("<" + qName);
         // do we have any newly-mapped namespaces?
         while (m_prefixList.size() > 0) {
             String prefix = (String) m_prefixList.remove(0);
            out.write(" xmlns:" + prefix + "=\"" + enc((String) m_prefixMap.get(prefix)) + "\"");
         }
         for (int i = 0; i < a.getLength(); i++) {
             out.write(" " + a.getQName(i) + "=\"" + enc(a.getValue(i)) + "\"");
         }
         out.write(">");
     }
 
     public void startPrefixMapping(String prefix, String uri) {
         logger.info("Started prefix mapping: " + prefix + " => " + uri);
         m_prefixMap.put(prefix, uri);
         if (m_xmlData != null) {
             m_prefixList.add(prefix);
         }
     }
 
     public void endPrefixMapping(String prefix) {
         logger.info("Finished prefix mapping: " + prefix);
         m_prefixMap.remove(prefix);
     }
 
     private String enc(String in) {
         return StreamUtil.xmlEncode(in);
     }
 
     public void endElement(String uri, 
                            String localName, 
                            String qName) throws SAXException {
         if (uri.equals(METS) && localName.equals("dmdSec") && m_currentID != null) {
             logger.info("Finished parsing dmdSec (ID = " + m_currentID + ")");
             if (m_xmlDataFile == null) {
                 throw new SAXException("dmdSec element must contain xmlData");
             }
             m_filesToDelete.add(m_xmlDataFile);
             String mime = m_currentMIME;
             if (mime == null) mime = INLINE_XML_MIMETYPE;
             FileBasedSIPContent content = new FileBasedSIPContent(m_currentID, true, mime, m_xmlDataFile);
             content.setLabel(m_currentLabel);
             m_contentMap.put(m_currentID, content);
             m_xmlDataFile = null;
             m_currentID = null;
         } else if (m_currentID != null) {
             if (uri.equals(METS) && localName.equals("xmlData") && m_xmlData != null) {
                 try {
                     m_xmlData.close();
                 } catch (Exception e) {
                     String msg = "Unable to close temporary xmlData file";
                     logger.warn(msg, e);
                     throw new SAXException(msg);
                 }
                 logger.info("Finished buffering xmlData for " + m_currentID);
                 m_xmlData = null;
             } else if (m_xmlData != null) {
                 // inside xmlData
                 try {
                     m_xmlData.write("</" + qName + ">");
                 } catch (Exception e) {
                     String msg = "Unable to write element end for xmlData";
                     logger.warn(msg, e);
                     throw new SAXException(msg);
                 }
             } else if (uri.equals(METS) && localName.equals("file")) {
                 // ending a file, could have been xmlData or FLocat
                 logger.info("Finished parsing file (ID = " + m_currentID + ")");
                 if (m_xmlDataFile == null) {
                     // it was a FLocat
                     if (m_currentLocator == null) {
                         throw new SAXException("file element must contain either FLocat or xmlData");
                     }
                     SIPContent content = new ResolverBasedSIPContent(
                                                  m_currentID, 
                                                  false, 
                                                  m_currentMIME, 
                                                  m_dataResolver,
                                                  m_currentLocatorType,
                                                  m_currentLocator);
                     m_contentMap.put(m_currentID, content);
                     m_currentLocator = null;
                     m_currentLocatorType = null;
                     m_currentID = null;
                 } else {
                     // it was an xmlData
                     m_filesToDelete.add(m_xmlDataFile);
                     m_contentMap.put(m_currentID, new FileBasedSIPContent(m_currentID, true, m_currentMIME, m_xmlDataFile));
                     m_xmlDataFile = null;
                     m_currentID = null;
                 }
             }
         } else if (uri.equals(METS) && localName.equals("div")) {
             m_currentDiv = m_currentDiv.getParentDivNode();
         }
     }
 
 }
