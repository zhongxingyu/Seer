 /**
  *  odt2daisy - OpenDocument to DAISY XML/Audio
  *
  *  (c) Copyright 2008 - 2009 by Vincent Spiewak, All Rights Reserved.
  *
  *  This program is free software; you can redistribute it and/or modify
  *  it under the terms of the GNU General Lesser Public License as published by
  *  the Free Software Foundation; either version 3 of the License, or
  *  (at your option) any later version.
  *
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU Lesser General Public License for more details.
  *
  *  You should have received a copy of the GNU Lesser General Public License along
  *  with this program; if not, write to the Free Software Foundation, Inc.,
  *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
  */
 package com.versusoft.packages.jodl;
 
 import com.sun.org.apache.xpath.internal.XPathAPI;
 import java.io.ByteArrayInputStream;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.util.ArrayList;
 import java.util.Enumeration;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipFile;
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.transform.TransformerConfigurationException;
 import javax.xml.transform.TransformerException;
 import org.w3c.dom.Element;
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.transform.OutputKeys;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerFactory;
 import javax.xml.transform.dom.DOMSource;
 import javax.xml.transform.stream.StreamResult;
 import org.w3c.dom.Document;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import org.xml.sax.EntityResolver;
 import org.xml.sax.InputSource;
 import org.xml.sax.SAXException;
 
 /**
  * OdtUtils.java: convert zipped odt to flat odt xml. It provide also some nice fonctions
  * 
  * @author Vincent Spiewak
  */
 public class OdtUtils {
 
     public static final String PICTURE_FOLDER = "Pictures/";
     private static final Logger logger = Logger.getLogger("com.versusoft.packages.jodl.odtutils");
     //
     private String odtFile;
     private DocumentBuilderFactory docFactory;
     private DocumentBuilder docBuilder;
     private Document doc;
     private Element root;
     private ZipFile zf;
 
     public void open(String odtFile) {
         try {
 
             logger.fine("entering");
 
             zf = null;
             this.odtFile = odtFile;
 
 
             zf = new ZipFile(odtFile);
 
             ZipEntry metaEntry = zf.getEntry("meta.xml");
             ZipEntry stylesEntry = zf.getEntry("styles.xml");
             ZipEntry contentEntry = zf.getEntry("content.xml");
             ZipEntry settingsEntry = zf.getEntry("settings.xml");
 
             docFactory = DocumentBuilderFactory.newInstance();
             docFactory.setValidating(false);
 
 
             docBuilder = docFactory.newDocumentBuilder();
             docBuilder.setEntityResolver(new EntityResolver() {
 
                 public InputSource resolveEntity(java.lang.String publicId, java.lang.String systemId) throws SAXException, java.io.IOException {
 
                     return new InputSource(new ByteArrayInputStream("<?xml version='1.0' encoding='UTF-8'?>".getBytes()));
                 }
             });
 
             doc = docBuilder.newDocument();
 
             Element racine = doc.createElement("office:document");
 
 
             Document metaDoc = docBuilder.parse(zf.getInputStream(metaEntry));
             Document stylesDoc = docBuilder.parse(zf.getInputStream(stylesEntry));
             Document contentDoc = docBuilder.parse(zf.getInputStream(contentEntry));
             Document settingsDoc = docBuilder.parse(zf.getInputStream(settingsEntry));
 
             replaceObjectContent(docBuilder, contentDoc, zf);
 
             racine.setAttribute("xmlns:meta", "urn:oasis:names:tc:opendocument:xmlns:meta:1.0");
             racine.setAttribute("xmlns:xsl", "http://www.w3.org/1999/XSL/Transform");
             racine.setAttribute("xmlns:office", "urn:oasis:names:tc:opendocument:xmlns:office:1.0");
             racine.setAttribute("xmlns:style", "urn:oasis:names:tc:opendocument:xmlns:style:1.0");
             racine.setAttribute("xmlns:text", "urn:oasis:names:tc:opendocument:xmlns:text:1.0");
             racine.setAttribute("xmlns:table", "urn:oasis:names:tc:opendocument:xmlns:table:1.0");
             racine.setAttribute("xmlns:draw", "urn:oasis:names:tc:opendocument:xmlns:drawing:1.0");
             racine.setAttribute("xmlns:fo", "urn:oasis:names:tc:opendocument:xmlns:xsl-fo-compatible:1.0");
             racine.setAttribute("xmlns:meta", "urn:oasis:names:tc:opendocument:xmlns:meta:1.0");
             racine.setAttribute("xmlns:number", "urn:oasis:names:tc:opendocument:xmlns:datastyle:1.0");
             racine.setAttribute("xmlns:svg", "urn:oasis:names:tc:opendocument:xmlns:svg-compatible:1.0");
             racine.setAttribute("xmlns:chart", "urn:oasis:names:tc:opendocument:xmlns:chart:1.0");
             racine.setAttribute("xmlns:dr3d", "urn:oasis:names:tc:opendocument:xmlns:dr3d:1.0");
             racine.setAttribute("xmlns:form", "urn:oasis:names:tc:opendocument:xmlns:form:1.0");
             racine.setAttribute("xmlns:script", "urn:oasis:names:tc:opendocument:xmlns:script:1.0");
             racine.setAttribute("xmlns:xlink", "http://www.w3.org/1999/xlink");
             racine.setAttribute("xmlns:dc", "http://purl.org/dc/elements/1.1/");
             racine.setAttribute("xmlns:math", "http://www.w3.org/1998/Math/MathML");
             racine.setAttribute("xmlns:dom", "http://www.w3.org/2001/xml-events");
             racine.setAttribute("xmlns:xforms", "http://www.w3.org/2002/xforms");
             racine.setAttribute("xmlns:xsd", "http://www.w3.org/2001/XMLSchema");
             racine.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
             racine.setAttribute("xmlns:config", "urn:oasis:names:tc:opendocument:xmlns:config:1.0");
 
             NodeList nodelist = metaDoc.getDocumentElement().getChildNodes();
             for (int i = 0; i < nodelist.getLength(); i++) {
                 racine.appendChild(doc.importNode(nodelist.item(i), true));
             }
 
             nodelist = settingsDoc.getDocumentElement().getChildNodes();
             for (int i = 0; i < nodelist.getLength(); i++) {
                 racine.appendChild(doc.importNode(nodelist.item(i), true));
             }
 
 
             nodelist = stylesDoc.getDocumentElement().getChildNodes();
             for (int i = 0; i < nodelist.getLength(); i++) {
                 racine.appendChild(doc.importNode(nodelist.item(i), true));
             }
 
             nodelist = contentDoc.getDocumentElement().getChildNodes();
             for (int i = 0; i < nodelist.getLength(); i++) {
                 racine.appendChild(doc.importNode(nodelist.item(i), true));
             }
 
 
             doc.appendChild(racine);
             root = doc.getDocumentElement();
 
         } catch (SAXException ex) {
             logger.log(Level.SEVERE, null, ex);
         } catch (IOException ex) {
             logger.log(Level.SEVERE, null, ex);
         } catch (ParserConfigurationException ex) {
             logger.log(Level.SEVERE, null, ex);
         }
 
     }
 
     public boolean saveXML(String fileOut) {
 
         return saveDOM(doc, fileOut);
 
     }
 
     public static void correctionProcessing(String xmlFile)
             throws ParserConfigurationException, SAXException, SAXException,
             IOException, IOException, TransformerConfigurationException,
             TransformerConfigurationException, TransformerConfigurationException,
             TransformerConfigurationException, TransformerConfigurationException,
             TransformerException {
 
         DocumentBuilderFactory docFactory;
         DocumentBuilder docBuilder;
 
         Document contentDoc;
 
         // Node pointer
         Node currentNode = null;
 
         docFactory =
                 DocumentBuilderFactory.newInstance();
         docFactory.setValidating(false);
 
         docBuilder =
                 docFactory.newDocumentBuilder();
         docBuilder.setEntityResolver(new EntityResolver() {
 
             public InputSource resolveEntity(
                     java.lang.String publicId, java.lang.String systemId)
                     throws SAXException, java.io.IOException {
 
                 return new InputSource(new ByteArrayInputStream("<?xml version='1.0' encoding='UTF-8'?>".getBytes()));
 
             }
         });
 
         contentDoc = docBuilder.parse(xmlFile);
         Element root = contentDoc.getDocumentElement();
 
         // Select first element after text:sequence-decls
         currentNode =
                 XPathAPI.selectSingleNode(root, "/document/body/text/sequence-decls/following-sibling::*[1]");
 
         if (currentNode == null) {
             System.out.println("XPath select failed");
         }
 
         removeEmptyHeadings(root);
         normalizeTextS(contentDoc,root);
         removeEmptyParagraphs(root);
         
         saveDOM(contentDoc, xmlFile);
 
     }
 
     public static void paginationProcessing(String xmlFile)
             throws ParserConfigurationException, SAXException, SAXException,
             IOException, IOException, TransformerConfigurationException,
             TransformerConfigurationException, TransformerConfigurationException,
             TransformerConfigurationException, TransformerConfigurationException,
             TransformerException {
 
         DocumentBuilderFactory docFactory;
         DocumentBuilder docBuilder;
 
         Document contentDoc;
 
         // Node pointer
         Node currentNode = null;
 
         docFactory =
                 DocumentBuilderFactory.newInstance();
         docFactory.setValidating(false);
 
         docBuilder =
                 docFactory.newDocumentBuilder();
         docBuilder.setEntityResolver(new EntityResolver() {
 
             public InputSource resolveEntity(
                     java.lang.String publicId, java.lang.String systemId)
                     throws SAXException, java.io.IOException {
 
                 return new InputSource(new ByteArrayInputStream("<?xml version='1.0' encoding='UTF-8'?>".getBytes()));
 
             }
         });
 
         contentDoc = docBuilder.parse(xmlFile);
         Element root = contentDoc.getDocumentElement();
 
         // Select first element after text:sequence-decls
         currentNode =
                 XPathAPI.selectSingleNode(root, "/document/body/text/sequence-decls/following-sibling::*[1]");
 
         if (currentNode == null) {
             System.out.println("XPath select failed");
         }
 
         insertPagination(root, currentNode, 0, false, "", "Standard", true, true);
         //correctionProcessing(root);
         saveDOM(contentDoc, xmlFile);
 
     }
 
     /**
      * return an ArrayList of image(s) path(s) included in ODT file
      * 
      * @param odtFile 
      * @return ArrayList of image(s) path(s)
      * @throws java.io.IOException
      */
     public static ArrayList<String> getPictures(String odtFile) throws IOException {
         ArrayList<String> ret = new ArrayList<String>();
         ZipFile zf = null;
         Enumeration entries = null;
 
         zf =
                 new ZipFile(odtFile);
         entries =
                 zf.entries();
 
         while (entries.hasMoreElements()) {
 
             ZipEntry entry = (ZipEntry) entries.nextElement();
 
             if (entry.getName().startsWith(PICTURE_FOLDER)) {
                 if (!entry.isDirectory()) {
                     ret.add(entry.getName());
                 }
 
             }
 
         }
 
         return ret;
 
     }
 
     /**
      * Extract pictures included inside an ODT file<br />
      * outDir can be: images, images/, pics/, book/pics/, ...
      * 
      * @param odtFile
      * @param outDir
      * @throws java.io.IOException
      */
     public static void extractPictures(String odtFile, String outDir) throws IOException {
 
         ZipFile zip = new ZipFile(odtFile);
         File dir = new File(outDir);
 
         ArrayList<String> pics = getPictures(odtFile);
 
         if (pics.size() < 1) {
             return;
         }
 
         if (dir.isFile()) {
             throw new IOException("Wrong argument: outDir is a file");
         }
 
         if (!dir.exists()) {
             dir.mkdirs();
         }
 
         for (int i = 0; i <
                 pics.size(); i++) {
             copyInputStream(
                     zip.getInputStream(zip.getEntry(pics.get(i))),
                     new FileOutputStream(outDir + pics.get(i).substring(PICTURE_FOLDER.length())));
 
         }
 
     }
 
     /**
      * Extract and normalize pictures names.
      * 
      * @param xmlFile
      * @param odtFile
      * @param parentDir
      * @param imgBaseDir
      * @throws org.xml.sax.SAXException
      * @throws org.xml.sax.SAXException
      * @throws java.io.IOException
      * @throws javax.xml.parsers.ParserConfigurationException
      * @throws javax.xml.transform.TransformerConfigurationException
      * @throws javax.xml.transform.TransformerException
      */
     public static void extractAndNormalizedEmbedPictures(String xmlFile, String odtFile, String parentDir, String imgBaseDir) throws SAXException, SAXException, IOException, ParserConfigurationException, TransformerConfigurationException, TransformerException {
 
         logger.fine("entering");
 
         ZipFile zip;
 
         File imgDir;
 
         ArrayList<String> pics;
 
         DocumentBuilderFactory docFactory;
 
         DocumentBuilder docBuilder;
 
         Document contentDoc;
 
         pics = getPictures(odtFile);
         zip = new ZipFile(odtFile);
 
         if (pics.size() < 1) {
             return;
         }
 
         imgDir = new File(parentDir + imgBaseDir);
 
         logger.fine("imgBaseDir: " + imgBaseDir + "\n");
         logger.fine("parentDir: " + parentDir + "\n");
 
         if (imgDir.isFile()) {
             throw new IOException("Wrong argument: parentDir is a file");
         }
 
         if (!imgDir.exists()) {
             imgDir.mkdirs();
         }
 
         docFactory = DocumentBuilderFactory.newInstance();
         docFactory.setValidating(false);
 
         docBuilder =
                 docFactory.newDocumentBuilder();
         docBuilder.setEntityResolver(new EntityResolver() {
 
             public InputSource resolveEntity(
                     java.lang.String publicId, java.lang.String systemId)
                     throws SAXException, java.io.IOException {
 
                 return new InputSource(new ByteArrayInputStream("<?xml version='1.0' encoding='UTF-8'?>".getBytes()));
 
             }
         });
 
 
         contentDoc = docBuilder.parse(xmlFile);
 
         Element root = contentDoc.getDocumentElement();
         NodeList nodelist = root.getElementsByTagName("draw:image");
 
 
         for (int i = 0; i < nodelist.getLength(); i++) {
 
             Node objectNode = nodelist.item(i);
             Node hrefNode = objectNode.getAttributes().getNamedItem("xlink:href");
 
             String imagePath = hrefNode.getTextContent();
             logger.fine("image path:" + imagePath);
 
             if (pics.contains(imagePath)) {
 
                 int id = pics.indexOf(imagePath);
                 String ext = imagePath.substring(imagePath.lastIndexOf(".")).toLowerCase();
 
                 //String newImageName = id + ext;
                 //String newImagePath = parentDir + imgBaseDir + newImageName;
 
 //                if (ext.endsWith("gif") || ext.endsWith("bmp") || ext.endsWith("wbmp")) {
 //
 //                    hrefNode.setTextContent(imgBaseDir + id + ".png");
 //                    logger.fine("extract image\n");
 //                    copyInputStream(zip.getInputStream(zip.getEntry(imagePath)), new FileOutputStream(parentDir + imgBaseDir + id + ext));
 //                    logger.fine("convert to png\n");
 //                    toPNG(parentDir + imgBaseDir + id + ext, parentDir + imgBaseDir + id + ".png");
 //                    logger.fine("delete old image\n");
 //                    new File(parentDir + imgBaseDir + id + ext).delete();
 //
 //                } else {
                 hrefNode.setTextContent(imgBaseDir + id + ext);
                 logger.fine("extract image\n");
                 copyInputStream(zip.getInputStream(zip.getEntry(imagePath)), new FileOutputStream(parentDir + imgBaseDir + id + ext));
 //                }
 
                 logger.fine("done\n");
 
             }
 
         }
 
         saveDOM(contentDoc, xmlFile);
         logger.fine("done");
     }
 
     /**
      * Replace embed pictures base dir.
      * 
      * @param xmlFile
      * @param imgBaseDir
      * @throws javax.xml.parsers.ParserConfigurationException
      * @throws org.xml.sax.SAXException
      * @throws java.io.IOException
      * @throws javax.xml.transform.TransformerConfigurationException
      * @throws javax.xml.transform.TransformerException
      */
     public static void replaceEmbedPicturesBaseDir(String xmlFile, String imgBaseDir) throws ParserConfigurationException, SAXException, IOException, TransformerConfigurationException, TransformerException {
         DocumentBuilderFactory docFactory;
         DocumentBuilder docBuilder;
 
         Document contentDoc;
 
         docFactory =
                 DocumentBuilderFactory.newInstance();
         docFactory.setValidating(false);
 
         docBuilder =
                 docFactory.newDocumentBuilder();
         docBuilder.setEntityResolver(new EntityResolver() {
 
             public InputSource resolveEntity(
                     java.lang.String publicId, java.lang.String systemId)
                     throws SAXException, java.io.IOException {
 
                 return new InputSource(new ByteArrayInputStream("<?xml version='1.0' encoding='UTF-8'?>".getBytes()));
 
             }
         });
 
 
         contentDoc =
                 docBuilder.parse(xmlFile);
         replaceEmbedPicturesBaseDir(contentDoc, imgBaseDir);
         saveDOM(contentDoc, xmlFile);
     }
 
     private static void replaceEmbedPicturesBaseDir(Document contentDoc, String imgBaseDir) throws IOException, SAXException {
 
         Element root = contentDoc.getDocumentElement();
         NodeList nodelist = root.getElementsByTagName("draw:image");
 
         for (int i = 0; i <
                 nodelist.getLength(); i++) {
 
             Node objectNode = nodelist.item(i);
             Node hrefNode = objectNode.getAttributes().getNamedItem("xlink:href");
 
             String imagePath = hrefNode.getTextContent();
             logger.fine("image path=" + imagePath);
 
             if (imagePath.startsWith(PICTURE_FOLDER)) {
 
                 String newImagePath = imgBaseDir + imagePath.substring(PICTURE_FOLDER.length());
                 hrefNode.setTextContent(newImagePath);
 
             }
 
         }
 
     }
 
 
     private static void removeEmptyHeadings(Node root){
 
         // for each text:h
         // remove empty headings
         NodeList hNodes = ((Element) root).getElementsByTagName("text:h");
         for (int i = 0; i < hNodes.getLength(); i++) {
 
             Node node = hNodes.item(i);
 
             if (node.getChildNodes().getLength() > 0) {
 
                 boolean empty = true;
 
                 for (int j = 0; j < node.getChildNodes().getLength(); j++) {
                     if (!node.getChildNodes().item(j).getTextContent().trim().equals("")) {
                         empty = false;
                     }
                 }
 
                 if (empty) {
                     node.getParentNode().removeChild(node);
                     i--;
                 }
 
             } else {
                 if (node.getTextContent().trim().equals("")) {
                     node.getParentNode().removeChild(node);
                     i--;
                 }
 
             }
         }
 
     }
 
     private static void normalizeTextS(Document doc, Node root){
 
         NodeList sNodes = ((Element) root).getElementsByTagName("text:s");
 
         for (int i = 0; i < sNodes.getLength(); i++) {
 
             Element elem = (Element) sNodes.item(i);
 
             int c = 1;
             String s = "";
             
             if(elem.hasAttribute("text:c")){
                 c = Integer.parseInt(elem.getAttribute("text:c"));
             }
             
             for(int j=0; j<c; j++){
                 s += " ";
             }
 
             Node textNode = doc.createTextNode(s);
             elem.getParentNode().replaceChild(textNode, elem);
             i--;
         }
     }
 
     private static void removeEmptyParagraphs(Node root){
 
         // for each text:p
         NodeList pNodes = ((Element) root).getElementsByTagName("text:p");
         for (int i = 0; i < pNodes.getLength(); i++) {
 
             Node node = pNodes.item(i);
 
             // if no text
             if (node.getTextContent().trim().equals("")){
 
                // if no childs
                 if(!node.hasChildNodes()){
 
                    // then remove
                    node.getParentNode().removeChild(node);
                    i--;
 
                // if childs
                 } else {
 
                     boolean empty = true;
 
                     // don't remove if an element is present (like image...)
                     for(int j=0; j<node.getChildNodes().getLength(); j++){
                        if(node.getChildNodes().item(j).getNodeType() == node.ELEMENT_NODE){
                             empty = false;
                        }
                     }
 
                     if(empty){
                         node.getParentNode().removeChild(node);
                         i--;
                     }
                 }
             }
 
         }
     }
 
     private static int insertPagination(Node root, Node node, int pagenum, boolean incPageNum, String enumType, String masterPageName, boolean isFirst, boolean recCall) throws TransformerException {
 
         Node next = null;
         boolean append = false;
         String xpath = null;
         String xpath1 = null;
         String xpath2 = null;
         String xpath3 = null;
 
         String styleName = null;
 
         // Select next sibling element
         next = node.getNextSibling();
         while (next != null && next.getNodeType() != Node.ELEMENT_NODE) {
             next = next.getNextSibling();
         }
 
         //if(next!=null)
         //logger.log(Level.SEVERE, "PageProcessing Next Sibling: "+next.getNodeName()+" "+next.getNodeValue());
 
         // if first element in doc
         if (isFirst) {
             append = true;
         }
 
         // text:p or text:h 
         if (node.getNodeName().equals("text:p") || node.getNodeName().equals("text:h")) {
 
             styleName = node.getAttributes().getNamedItem("text:style-name").getNodeValue();
 
             // text:p break-before='page'
             xpath1 =
                     "/document/automatic-styles/style[@name='" + styleName + "']/paragraph-properties[@break-before='page']";
             // text:p page-number='auto'
             xpath2 =
                     "/document/automatic-styles/style[@name='" + styleName + "']/paragraph-properties[@page-number='auto']";
             // text:p page-number="value"
             xpath3 =
                     "/document/automatic-styles/style[@name='" + styleName + "']/paragraph-properties[@page-number>0]";
 
             if (XPathAPI.eval(root, xpath1).bool()) {
                 append = true;
             } else if (XPathAPI.eval(root, xpath2).bool()) {
                 append = true;
             } else if (XPathAPI.eval(root, xpath3).bool()) {
 
                 xpath = "/document/automatic-styles/style[@name='" + styleName + "']/paragraph-properties/@page-number";
                 pagenum = Integer.parseInt(XPathAPI.eval(root, xpath).str());
                 pagenum--;
 
                 append = true;
             }
 
             // update masterPageName
             if (append) {
 
                 xpath = "/document/automatic-styles/style[@name='" + styleName + "']/@master-page-name";
                 boolean hasMasterPage = XPathAPI.eval(root, xpath).bool();
                 if (hasMasterPage) {
                     xpath = "/document/automatic-styles/style[@name='" + styleName + "']/@master-page-name";
                     masterPageName =
                             XPathAPI.eval(root, xpath).str();
                 }
 
             }
 
 
         } // text:list
         else if (node.getNodeName().equals("text:list")) {
 
             styleName = node.getAttributes().getNamedItem("text:style-name").getNodeValue();
 
             // text:list break-before='page'
             xpath1 = "/document/automatic-styles/style[@list-style-name='" + styleName + "']/paragraph-properties[@break-before='page']";
             // text:list page-number='auto'
             xpath2 = "/document/automatic-styles/style[@list-style-name='" + styleName + "']/paragraph-properties[@page-number='auto']";
             // text:list page-number="value"
             xpath3 = "/document/automatic-styles/style[@list-style-name='" + styleName + "']/paragraph-properties[@page-number>0]";
 
             if (XPathAPI.eval(root, xpath1).bool()) {
                 append = true;
             } else if (XPathAPI.eval(root, xpath2).bool()) {
                 append = true;
             } else if (XPathAPI.eval(root, xpath3).bool()) {
                 xpath = "/document/automatic-styles/style[@list-style-name='" + styleName + "']/paragraph-properties/@page-number";
                 pagenum = Integer.parseInt(XPathAPI.eval(root, xpath).str());
                 pagenum--;
 
                 append = true;
             }
 
             // update masterPageName
             if (append) {
 
                 xpath = "/document/automatic-styles/style[@list-style-name='" + styleName + "']/@master-page-name";
                 boolean hasMasterPage = XPathAPI.eval(root, xpath).bool();
                 if (hasMasterPage) {
                     xpath = "/document/automatic-styles/style[@list-style-name='" + styleName + "']/@master-page-name";
                     masterPageName = XPathAPI.eval(root, xpath).str();
                 }
 
             }
 
         } // table:table
         else if (node.getNodeName().equals("table:table")) {
 
             styleName = node.getAttributes().getNamedItem("table:style-name").getNodeValue();
 
             // table:table break-before='page'
             xpath1 = "/document/automatic-styles/style[@name='" + styleName + "']/table-properties[@break-before='page']";
             // table:table page-number='auto'
             xpath2 = "/document/automatic-styles/style[@name='" + styleName + "']/table-properties[@page-number='0']";
             // table:table page-number="value"
             xpath3 = "/document/automatic-styles/style[@name='" + styleName + "']/table-properties[@page-number>0]";
 
             if (XPathAPI.eval(root, xpath1).bool()) {
                 append = true;
             } else if (XPathAPI.eval(root, xpath2).bool()) {
                 append = true;
             } else if (XPathAPI.eval(root, xpath3).bool()) {
 
                 xpath = "/document/automatic-styles/style[@name='" + styleName + "']/table-properties/@page-number";
                 pagenum = Integer.parseInt(XPathAPI.eval(root, xpath).str());
                 pagenum--;
 
                 append = true;
             }
 
             // update masterPageName
             if (append) {
 
                 xpath = "/document/automatic-styles/style[@name='" + styleName + "']/@master-page-name";
                 boolean hasMasterPage = XPathAPI.eval(root, xpath).bool();
                 if (hasMasterPage) {
                     xpath = "/document/automatic-styles/style[@name='" + styleName + "']/@master-page-name";
                     masterPageName =
                             XPathAPI.eval(root, xpath).str();
                 }
 
             }
 
         } // text:table-of-content
         else if (node.getNodeName().equals("text:table-of-content") ||
                 node.getNodeName().equals("text:alphabetical-index") ||
                 node.getNodeName().equals("text:illustration-index") ||
                 node.getNodeName().equals("text:table-index") ||
                 node.getNodeName().equals("text:user-index") ||
                 node.getNodeName().equals("text:object-index") ||
                 node.getNodeName().equals("text:bibliography")) {
 
             styleName = ((org.w3c.dom.Element) ((org.w3c.dom.Element) ((org.w3c.dom.Element) node).getElementsByTagName("text:index-body").item(0)).getElementsByTagName("text:index-title").item(0)).getElementsByTagName("text:p").item(0).getAttributes().getNamedItem("text:style-name").getNodeValue();
 
             // text:table-of-content break-before='page'
             xpath1 = "/document/automatic-styles/style[@name='" + styleName + "']/paragraph-properties[@break-before='page']";
             // text:table-of-content page-number='auto'
             xpath2 = "/document/automatic-styles/style[@name='" + styleName + "']/paragraph-properties[@page-number='auto']";
             // text:table-of-content page-number="value"
             xpath3 = "/document/automatic-styles/style[@name='" + styleName + "']/paragraph-properties[@page-number>0]";
 
             if (XPathAPI.eval(root, xpath1).bool()) {
                 append = true;
             } else if (XPathAPI.eval(root, xpath2).bool()) {
                 append = true;
             } else if (XPathAPI.eval(root, xpath3).bool()) {
 
                 xpath = "/document/automatic-styles/style[@name='" + styleName + "']/paragraph-properties/@page-number";
                 pagenum = Integer.parseInt(XPathAPI.eval(root, xpath).str());
                 pagenum--;
 
                 append = true;
             }
 
             // update masterPageName
             if (append) {
 
                 xpath = "/document/automatic-styles/style[@name='" + styleName + "']/@master-page-name";
                 boolean hasMasterPage = XPathAPI.eval(root, xpath).bool();
                 if (hasMasterPage) {
                     xpath = "/document/automatic-styles/style[@name='" + styleName + "']/@master-page-name";
                     masterPageName =
                             XPathAPI.eval(root, xpath).str();
                 }
 
             }
 
         } // text:section
         else if (node.getNodeName().equals("text:section")) {
 
             for (int i = 0; i <
                     node.getChildNodes().getLength(); i++) {
 
                 if (node.getChildNodes().item(i).getNodeType() == Node.ELEMENT_NODE) {
 
                     //System.out.println("child: "+node.getChildNodes().item(i).getNodeName());
                     //System.out.println("child: "+node.getChildNodes().item(i).getNodeValue());
 
                     int oldlength = node.getChildNodes().getLength();
 
                     pagenum = insertPagination(root, node.getChildNodes().item(i), pagenum, incPageNum, enumType, masterPageName, false, false);
 
                     //adding offset for inserted childs
                     i += node.getChildNodes().getLength() - oldlength;
                 }
 
             //System.out.println("child(" + i + "): " + node.getChildNodes().item(i).getNodeName() + " Type: " + node.getChildNodes().item(i).getNodeType());
             }
         }
 
 
         // append pagenum node
         if (append) {
 
             pagenum++;
 
             // update incPageNum
             xpath = "(count(/document/master-styles/master-page[@name='" + masterPageName + "']/header/p/page-number)" +
                     "+" + "count(/document/master-styles/master-page[@name='" + masterPageName + "']/footer/p/page-number))>0";
             incPageNum = XPathAPI.eval(root, xpath).bool();
 
             // update enumType
             xpath =
                     "/document/automatic-styles/page-layout[@name=(/document/master-styles/master-page[@name='" + masterPageName + "']/@page-layout-name)]/page-layout-properties/@num-format";
             enumType =
                     XPathAPI.eval(root, xpath).str();
 
             Element pageNode = root.getOwnerDocument().createElement("pagenum");
             pageNode.setAttribute("num", Integer.toString(pagenum));
             pageNode.setAttribute("enum", enumType);
             pageNode.setAttribute("render", Boolean.toString(incPageNum));
 
             if (enumType.equals("i")) {
                 pageNode.setAttribute("value", Numbering.toRoman(pagenum));
             } else if (enumType.equals("I")) {
                 pageNode.setAttribute("value", Numbering.toRoman(pagenum).toUpperCase());
             }
 
             if (enumType.equals("a")) {
                 pageNode.setAttribute("value", Numbering.toLetter(pagenum));
 
             } else if (enumType.equals("A")) {
                 pageNode.setAttribute("value", Numbering.toLetter(pagenum).toUpperCase());
             }
             node.getParentNode().insertBefore(pageNode, node);
 
         }
 
         String nName = node.getNodeName();
         NodeList pageBreaks = ((Element) node).getElementsByTagName("text:soft-page-break");
 
         if (pageBreaks.getLength() > 0) {
 
             NodeList childs = node.getChildNodes();
 
             // text:p and text:h with text:soft-page-break (assert: only one text:soft-page-break !)
             if (nName.equals("text:p") || nName.equals("text:h")) {
 
 
                 Node pageBreakNode = pageBreaks.item(0);
 
                 Node p1 = node.cloneNode(false);
                 Node p2 = node.cloneNode(false);
 
                 while (!childs.item(0).isSameNode(pageBreakNode)) {
                     p1.appendChild(node.getChildNodes().item(0));
                 }
 
                 while (node.getChildNodes().getLength() > 1) {
                     p2.appendChild(node.getChildNodes().item(1));
                 }
 
                 pagenum++;
 
                 Element pageNode = root.getOwnerDocument().createElement("pagenum");
                 pageNode.setAttribute("num", Integer.toString(pagenum));
                 pageNode.setAttribute("enum", enumType);
                 pageNode.setAttribute("render", Boolean.toString(incPageNum));
 
                 node.getParentNode().insertBefore(p1, node);
                 node.getParentNode().insertBefore(pageNode, node);
                 node.getParentNode().insertBefore(p2, node);
                 node.getParentNode().removeChild(node);
 
             // text:list with text:soft-page-break
             } else if (nName.equals("text:list")) {
 
                 for (int i = 0; i <
                         pageBreaks.getLength(); i++) {
 
                     pagenum++;
 
                     Element pageNode = root.getOwnerDocument().createElement("pagenum");
                     pageNode.setAttribute("num", Integer.toString(pagenum));
                     pageNode.setAttribute("enum", enumType);
                     pageNode.setAttribute("render", Boolean.toString(incPageNum));
 
                     Node n = pageBreaks.item(0).getParentNode();
                     while (!n.getNodeName().equals("text:list-item")) {
                         n = n.getParentNode();
                     }
 
                     n.getParentNode().insertBefore(pageNode, n);
 
                 }
 
             // text:table-of-content with text:soft-page-break
             } else if (node.getNodeName().equals("text:table-of-content") ||
                     node.getNodeName().equals("text:alphabetical-index") ||
                     node.getNodeName().equals("text:illustration-index") ||
                     node.getNodeName().equals("text:table-index") ||
                     node.getNodeName().equals("text:user-index") ||
                     node.getNodeName().equals("text:object-index") ||
                     node.getNodeName().equals("text:bibliography")) {
 
                 for (int i = 0; i < pageBreaks.getLength(); i++) {
 
                     // adding tableNode and pageNode
                     pagenum++;
 
                     Element pageNode = root.getOwnerDocument().createElement("pagenum");
                     pageNode.setAttribute("num", Integer.toString(pagenum));
                     pageNode.setAttribute("enum", enumType);
                     pageNode.setAttribute("render", Boolean.toString(incPageNum));
 
                     pageBreaks.item(i).getParentNode().getParentNode().insertBefore(pageNode, pageBreaks.item(i).getParentNode());
                 }
 
             } // table:table with text:soft-page-break (assert: soft-page-break always between <table:row>) 
             else if (nName.equals("table:table")) {
 
                 for (int i = 0; i < pageBreaks.getLength(); i++) {
 
                     pagenum++;
 
                     Element pageNode = root.getOwnerDocument().createElement("pagenum");
                     pageNode.setAttribute("num", Integer.toString(pagenum));
                     pageNode.setAttribute("enum", enumType);
                     pageNode.setAttribute("render", Boolean.toString(incPageNum));
 
                     pageBreaks.item(i).getParentNode().getParentNode().insertBefore(pageNode, pageBreaks.item(i).getParentNode());
                 //node.replaceChild(pageNode, pageBreaks.item(0));
 
                 }
 
             }
 
         }
 
 
         // recursif call on next sibling
         if (next != null && recCall) {
             //System.out.println("next: " + next.getNodeName() + " value: " + next.getTextContent());
             pagenum = insertPagination(root, next, pagenum, incPageNum, enumType, masterPageName, false, true);
         }
 
         return pagenum;
     }
 
     /**
      * Insert MathML separated files into Flat ODT XML
      * @param docBuilder
      * @param contentDoc
      * @param zf
      * @param parentPath
      * @throws java.io.IOException
      * @throws org.xml.sax.SAXException
      */
     private static void replaceObjectContent(
             DocumentBuilder docBuilder, Document contentDoc, ZipFile zf) throws IOException, SAXException {
 
         logger.fine("entering");
 
         Element root = contentDoc.getDocumentElement();
         NodeList nodelist = root.getElementsByTagName("draw:object");
 
         for (int i = 0; i < nodelist.getLength(); i++) {
 
             Node objectNode = nodelist.item(i);
             Node hrefNode = objectNode.getAttributes().getNamedItem("xlink:href");
 
             String objectPath = hrefNode.getTextContent();
             logger.fine("object path=" + objectPath);
 
             Document objectDoc = docBuilder.parse(zf.getInputStream(zf.getEntry(objectPath.substring(2) + "/" + "content.xml")));
             Node objectContentNode = objectDoc.getDocumentElement();
 
             String tagName = objectContentNode.getNodeName();
             logger.fine(tagName);
 
             if (tagName.equals("math:math")) {
                 logger.fine("replacing math");
 
                 Node newObjectNode = contentDoc.createElement("draw:object");
                 newObjectNode.appendChild(contentDoc.importNode(objectContentNode, true));
                 objectNode.getParentNode().replaceChild(newObjectNode, objectNode);
             }
 
         }
 
         logger.fine("done");
     }
 
     private static final void copyInputStream(InputStream in, OutputStream out)
             throws IOException {
         byte[] buffer = new byte[1024];
         int len;
 
         while ((len = in.read(buffer)) >= 0) {
             out.write(buffer, 0, len);
         }
 
         in.close();
         out.close();
     }
 
     public static boolean saveDOM(Document doc, String filename) {
         boolean save = false;
         try {
 
             Transformer transformer = TransformerFactory.newInstance().newTransformer();
             transformer.setOutputProperty(OutputKeys.METHOD, "xml");
             transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
             transformer.setOutputProperty(OutputKeys.INDENT, "yes");
             transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "3");
 
             StreamResult result = new StreamResult(filename);
             DOMSource source = new DOMSource(doc);
             transformer.transform(source, result);
 
             save = true;
         } catch (TransformerConfigurationException ex) {
             logger.log(Level.SEVERE, null, ex);
         } catch (TransformerException ex) {
             logger.log(Level.SEVERE, null, ex);
         } finally {
 
             return save;
 
         }
     }
 }
