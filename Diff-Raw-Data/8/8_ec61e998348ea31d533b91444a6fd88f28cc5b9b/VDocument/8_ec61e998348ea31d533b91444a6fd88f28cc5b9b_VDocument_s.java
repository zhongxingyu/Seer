 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.anosym.vjax.xml;
 
 import java.io.*;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import org.htmlcleaner.CleanerProperties;
 import org.htmlcleaner.HtmlCleaner;
 import org.htmlcleaner.SimpleXmlSerializer;
 import org.htmlcleaner.TagNode;
 import org.xml.sax.DTDHandler;
 import org.xml.sax.InputSource;
 import org.xml.sax.SAXException;
 import org.xml.sax.XMLReader;
 import org.xml.sax.helpers.XMLReaderFactory;
 
 /**
  *
  * @author Marembo
  */
 public class VDocument {
 
   private VElement rootElement;
   private URL documentUrl;
   private File documentName;
   private String documentType;
   private String schemaUrl;
   private VElement notationDeclaration;
   private boolean standalone;
   private List<VDocument> includes;
   private transient boolean debug;
 
   public VDocument() {
     this.includes = new ArrayList<VDocument>();
     this.standalone = true;
   }
 
   public VDocument(String name) {
     this(new File(name));
   }
 
   public VDocument(File documentFile) {
     this();
     this.documentName = documentFile;
     if (!this.documentName.exists()) {
       try {
         //check if the folders exist
         if (this.documentName.getParentFile() != null) {
           if (!this.documentName.getParentFile().exists()) {
             this.documentName.getParentFile().mkdirs();
           }
         }
         this.documentName.getAbsoluteFile().createNewFile();
       } catch (IOException ex) {
         try {
           Logger.getLogger(VDocument.class.getName()).log(Level.SEVERE, documentName.getCanonicalPath(), ex);
         } catch (IOException ex1) {
           Logger.getLogger(VDocument.class.getName()).log(Level.SEVERE, null, ex1);
         }
       }
     }
     this.doGetURL();
   }
 
   public VDocument(URL documentUrl, boolean standalone) {
     this();
     this.documentUrl = documentUrl;
     this.standalone = standalone;
   }
 
   public VDocument(URL documentUrl) {
     this();
     this.documentUrl = documentUrl;
   }
 
   public VDocument(File documentFile, String documentType, String schemaUrl) {
     this(documentFile);
     this.documentType = documentType;
     this.schemaUrl = schemaUrl;
   }
 
   public void setDebug(boolean debug) {
     this.debug = debug;
   }
 
   public boolean isDebug() {
     return debug;
   }
 
   public File getDocumentName() {
     return documentName;
   }
 
   public void setDocumentName(File documentName) {
     this.documentName = documentName;
   }
 
   private void doGetURL() {
     try {
       this.documentUrl = this.documentName.toURI().toURL();
     } catch (Exception e) {
     }
   }
 
   public String getName() {
     return documentName.getName();
   }
 
   public VElement getRootElement() {
     return rootElement;
   }
 
   public void setRootElement(VElement rootElement) {
     this.rootElement = rootElement;
   }
 
   @Override
   public String toString() {
     if (debug) {
       return toXmlString();
     }
     return (this.notationDeclaration == null) ? "" : this.notationDeclaration.elemString() + this.rootElement.elemString().trim();
   }
 
   public String toXmlString() {
     VNamespace vn = this.rootElement.getAssociatedNamespace();
     standalone = vn == null || !vn.isHasSchema();
     return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=" + ((standalone) ? "\"yes\"" : "\"no\"") + " ?>\n"
             + this.rootElement.elemString().trim();
   }
 
   public void parse() {
     BufferedInputStream reader = null;
     try {
       reader = new BufferedInputStream(this.documentUrl.openStream());
       parse(reader);
     } catch (IOException ex) {
       throw new RuntimeException(ex);
     } catch (SAXException ex) {
       throw new RuntimeException(ex);
     } finally {
       if (reader != null) {
         try {
           reader.close();
         } catch (IOException ex) {
           Logger.getLogger(VDocument.class.getName()).log(Level.SEVERE, null, ex);
         }
       }
     }
   }
 
   private void parse(InputStream inn, VXMLHandler handler) throws SAXException, IOException {
     try {
       XMLReader xmlreader = XMLReaderFactory.createXMLReader();
       xmlreader.setContentHandler(handler);
       InputSource s = new InputSource(new InputStreamReader(inn));
       xmlreader.setProperty("http://xml.org/sax/properties/lexical-handler", handler);
       xmlreader.setProperty("http://xml.org/sax/properties/declaration-handler", handler);
       xmlreader.setDTDHandler(new DTDHandler() {
         @Override
         public void notationDecl(String string, String string1, String string2) throws SAXException {
           //do nothing
         }
 
         @Override
         public void unparsedEntityDecl(String string, String string1, String string2, String string3) throws SAXException {
           //do nothing
         }
       });
       xmlreader.setFeature("http://xml.org/sax/features/validation", false);
       xmlreader.setFeature("http://apache.org/xml/features/nonvalidating/load-dtd-grammar", false);
       xmlreader.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
       xmlreader.setFeature("http://xml.org/sax/features/external-general-entities", false);
       xmlreader.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
       xmlreader.parse(s);
       this.setRootElement(handler.getRootElement());
       List<String[]> decls = handler.getNotationDeclaration();
       for (String[] d : decls) {
         VElement decl = null;
         if (notationDeclaration == null) {
           notationDeclaration = new VElement(d[0]);
           decl = notationDeclaration;
         } else {
           decl = new VElement(d[0], notationDeclaration);
         }
         decl.addAttribute(new VAttribute("publicid", d[1]));
         decl.addAttribute(new VAttribute("systemid", d[2]));
       }
       //find if we have includes, read then in
       for (VElement elem : rootElement.getChildren()) {
         VNamespace xinclude = elem.getAssociatedNamespace();
         if (xinclude != null) { //Should this be null?
           if (xinclude.equals(VNamespace.XINCLUDE_NAMESPACE)) {
             String name = elem.getAttribute("href").getValue();
             URL url = null;
             //consider file or http or ftp
             if (!name.startsWith("file") && !name.startsWith("http") && !name.startsWith("ftp")) {
               File file = new File(name);
               url = file.toURI().toURL();
             } else {
               url = new URL(name);
             }
             VDocument doc = new VDocument(name);
             doc.parse(url.openStream(), handler);
             this.includes.add(doc);
           }
         }
       }
     } catch (SAXException ee) {
       System.out.println(ee.getMessage());
       throw ee;
     }
   }
 
   private void parse(InputStream inn) throws SAXException, IOException {
     parse(inn, new VXMLHandler());
   }
 
   public VElement getNotationDeclaration() {
     return notationDeclaration;
   }
 
   public void removeElement(VElement elem) {
     if (this.rootElement != null) {
       this.rootElement.removeChild(elem);
     }
   }
 
   public static VDocument parseDocument(InputStream inn) {
     return parseDocument(inn, false);
   }
 
   public static VDocument parseDocument(File file) {
     try {
       return parseDocument(new FileInputStream(file), false);
     } catch (FileNotFoundException ex) {
       throw new RuntimeException(ex);
     }
   }
 
   /**
    * if this document is an html, it may not be well formed, and therefore, it needs to be cleaned
    * first.
    *
    * @param inn
    * @param isHtml
    * @return
    */
   public static VDocument parseDocument(InputStream inn, boolean isHtml) {
     try {
       VDocument doc = new VDocument();
       if (isHtml) {
         //we do not know what type of document we are handling, so we do some cleaning first.
         HtmlCleaner htmlCleaner = new HtmlCleaner();
         CleanerProperties cp = htmlCleaner.getProperties();
         TagNode node = htmlCleaner
                 .clean(inn);
         ByteArrayOutputStream out = new ByteArrayOutputStream();
         new SimpleXmlSerializer(cp).writeToStream(node, out);
         byte[] buffer = out.toByteArray();
         inn = new ByteArrayInputStream(buffer);
       }
       doc.parse(inn);
       return doc;
     } catch (IOException ex) {
       throw new RuntimeException(ex);
     } catch (SAXException ex) {
       throw new RuntimeException(ex);
     }
   }
 
   public static VDocument parseDocument(String filePath) {
     try {
       File file = new File(filePath);
       return parseDocument(new FileInputStream(file));
     } catch (FileNotFoundException ex) {
       throw new RuntimeException(ex);
     }
   }
 
   public static VDocument parseDocumentFromString(String xml) {
     if (!xml.contains("<?xml")) {
       xml = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"" + " ?>\n" + xml;
     }
     return VDocument.parseDocument(new ByteArrayInputStream(xml.getBytes()));
   }
 
   public void writeDocument() {
     FileOutputStream out = null;
     try {
      if (documentName.exists() && documentName.length() > 0) {
        return;
      }
       out = new FileOutputStream(documentName);
       out.write(this.toXmlString().getBytes());
       out.close();
       //if the includes file do not exist, write them too
       for (VDocument include : this.includes) {
         include.writeDocument();
       }
     } catch (IOException ex) {
       throw new RuntimeException(ex);
     } finally {
       if (out != null) {
         try {
           out.close();
         } catch (IOException ex) {
           Logger.getLogger(VDocument.class.getName()).log(Level.SEVERE, null, ex);
         }
       }
     }
    //write the includes
    for (VDocument doc : includes) {
      doc.writeDocument();
    }
   }
 
   public void addInclude(VDocument doc) {
     this.includes.add(doc);
     VNamespace namespace = VNamespace.XINCLUDE_NAMESPACE;
     VElement elem = new VElement("include");
     elem.addNamespace(namespace);
     elem.addAttribute(new VAttribute("href", doc.documentName.getPath()));
     rootElement.addChildAsFirst(elem);
   }
 
   public void setIncludes(List<VDocument> includes) {
     this.includes = new ArrayList<VDocument>(includes);
     if (!this.includes.isEmpty()) {
       //add the schema namespace
       VNamespace namespace = VNamespace.XINCLUDE_NAMESPACE;
       for (VDocument doc : includes) {
         VElement elem = new VElement("include");
         elem.addNamespace(namespace);
         elem.addAttribute(new VAttribute("href", doc.documentName.getAbsolutePath()));
         rootElement.addChildAsFirst(elem);
       }
     }
   }
 
   public List<VDocument> getIncludes() {
     return includes;
   }
 
   @Override
   public int hashCode() {
     int hash = 3;
     hash = 37 * hash + (this.rootElement != null ? this.rootElement.hashCode() : 0);
     hash = 37 * hash + (this.documentUrl != null ? this.documentUrl.hashCode() : 0);
     hash = 37 * hash + (this.documentName != null ? this.documentName.hashCode() : 0);
     hash = 37 * hash + (this.documentType != null ? this.documentType.hashCode() : 0);
     hash = 37 * hash + (this.schemaUrl != null ? this.schemaUrl.hashCode() : 0);
     hash = 37 * hash + (this.notationDeclaration != null ? this.notationDeclaration.hashCode() : 0);
     hash = 37 * hash + (this.standalone ? 1 : 0);
     hash = 37 * hash + (this.includes != null ? this.includes.hashCode() : 0);
     hash = 37 * hash + (this.debug ? 1 : 0);
     return hash;
   }
 
   @Override
   public boolean equals(Object obj) {
     if (obj == null) {
       return false;
     }
     if (getClass() != obj.getClass()) {
       return false;
     }
     final VDocument other = (VDocument) obj;
     if (this.rootElement != other.rootElement && (this.rootElement == null || !this.rootElement.equals(other.rootElement))) {
       return false;
     }
     if (this.documentUrl != other.documentUrl && (this.documentUrl == null || !this.documentUrl.equals(other.documentUrl))) {
       return false;
     }
     if (this.documentName != other.documentName && (this.documentName == null || !this.documentName.equals(other.documentName))) {
       return false;
     }
     if ((this.documentType == null) ? (other.documentType != null) : !this.documentType.equals(other.documentType)) {
       return false;
     }
     if ((this.schemaUrl == null) ? (other.schemaUrl != null) : !this.schemaUrl.equals(other.schemaUrl)) {
       return false;
     }
     if (this.notationDeclaration != other.notationDeclaration && (this.notationDeclaration == null || !this.notationDeclaration.equals(other.notationDeclaration))) {
       return false;
     }
     if (this.standalone != other.standalone) {
       return false;
     }
     if (this.includes != other.includes && (this.includes == null || !this.includes.equals(other.includes))) {
       return false;
     }
     if (this.debug != other.debug) {
       return false;
     }
     return true;
   }
 }
