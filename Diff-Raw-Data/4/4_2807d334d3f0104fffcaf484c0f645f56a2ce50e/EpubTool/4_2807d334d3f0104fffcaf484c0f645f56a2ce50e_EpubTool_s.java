 package name.vysoky.epub;
 
 import nl.siegmann.epublib.domain.Book;
 import nl.siegmann.epublib.domain.Resource;
 import nl.siegmann.epublib.epub.EpubReader;
 import nl.siegmann.epublib.epub.NCXDocument;
 import nl.siegmann.epublib.util.IOUtil;
 import org.apache.commons.io.FileUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.NodeList;
 import org.xml.sax.SAXException;
 
 import javax.management.modelmbean.XMLParseException;
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.transform.OutputKeys;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerConfigurationException;
 import javax.xml.transform.TransformerFactory;
 import javax.xml.transform.dom.DOMSource;
 import javax.xml.transform.stream.StreamResult;
 import java.io.*;
 import java.net.MalformedURLException;
 
 /**
  * EPUB tool overriding EPUB library functionality.
  * @author Jiri Vysoky
  */
 public class EpubTool {
 
     final Logger logger = LoggerFactory.getLogger(EpubTool.class);
 
     private DocumentBuilder xhtmlBuilder;
     private Transformer xhtmlTransformer;
 
     private File directory;
     private Book book;
 
     /**
      * Parametric constructor.
      * @param directory extracted EPUB directory.
      */
     public EpubTool(File directory)
             throws IOException, ParserConfigurationException, TransformerConfigurationException {
         this.directory = directory;
         initFactories();
         initBook();
     }
 
     /**
      * Accessor for XHTML document builder.
      * @return document builder
      */
     public DocumentBuilder getXhtmlBuilder() {
         return xhtmlBuilder;
     }
 
     public Book getBook() {
         return book;
     }
 
     private void initFactories() throws ParserConfigurationException, TransformerConfigurationException {
         DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
         documentBuilderFactory.setValidating(true);
         documentBuilderFactory.setNamespaceAware(false);
         //documentBuilderFactory.setXIncludeAware(true);
         documentBuilderFactory.setExpandEntityReferences(false); // it true removes &nbqp; and others !!
         xhtmlBuilder = documentBuilderFactory.newDocumentBuilder();
         xhtmlBuilder.setEntityResolver(new EpubEntityResolver());
         xhtmlBuilder.setErrorHandler(new EpubErrorHandler());
 
         TransformerFactory transformerFactory = TransformerFactory.newInstance();
         xhtmlTransformer = transformerFactory.newTransformer();
         xhtmlTransformer.setErrorListener(new EpubErrorListener());
         xhtmlTransformer.setOutputProperty(OutputKeys.INDENT, "no");
         xhtmlTransformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
         xhtmlTransformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
         xhtmlTransformer.setOutputProperty(OutputKeys.STANDALONE, "no");
         xhtmlTransformer.setOutputProperty(OutputKeys.DOCTYPE_PUBLIC, "-//W3C//DTD XHTML 1.1//EN");
         xhtmlTransformer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, "http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd");
     }
 
     private void initBook() throws IOException {
         File file = File.createTempFile("temporary", ".epub");
         FileUtils.deleteQuietly(file);
         EpubZip.compress(directory, file);
         EpubReader reader = new EpubReader();
         book = reader.readEpub(new FileInputStream(file));
     }
 
     public Document readDocument(Resource resource) throws IOException, SAXException {
         //TODO: Loading from zip is not functional?!
         //return epubTool.getXhtmlBuilder().parse(resource.getInputStream());
         // bad fix - load from original file
         return getXhtmlBuilder().parse(convertResourceToFile(resource));
     }
 
     public void writeXhtmlDocument(Resource resource, Document document) {
         try {
             document.setXmlStandalone(false);
             File file = convertResourceToFile(resource);
             //xhtmlTransformer.transform(new DOMSource(document), new StreamResult(file));
             StringWriter stringWriter = new StringWriter();
             xhtmlTransformer.transform(new DOMSource(document), new StreamResult(stringWriter));
             String string = stringWriter.getBuffer().toString();
             string = string.replace(Entity.toCharacterAsString("&nbsp;"), "&nbsp;");
             string = string.replace(" version=\"-//W3C//DTD XHTML 1.1//EN\"", "");
             string = string.replace(" profile=\"\"", "");
             FileUtils.write(file, string);
             logger.info("Successfully saved file: " + file);
         } catch (Exception e) {
             logger.error("Unable to save file!", e);
         }
     }
 
         public void writeResourceAsString(Resource resource, String string) {
         try {
             File file = convertResourceToFile(resource);
             FileUtils.write(file, string);
             logger.info("Successfully saved file: " + file);
         } catch (Exception e) {
             logger.error("Unable to save file!", e);
         }
     }
 
 //    public void writeFile(Resource resource, File inputFile) {
 //        try {
 //            File outputFile = convertResourceToFile(resource);
 //            logger.info("Writing document to file: " + outputFile);
 //            FileUtils.copyFile(inputFile, outputFile);
 //            FileUtils.deleteQuietly(inputFile);
 //        } catch (Exception e) {
 //            logger.error("Unable write document!", e);
 //        }
 //    }
 
     public void writeTableOfContents(Book book) {
         InputStream is = null;
         OutputStream os = null;
         try {
             File file = getTableOfContentsFile();
             logger.info("Writing document to file: " + file);
             os = new BufferedOutputStream(new FileOutputStream(file));
             Resource resource = NCXDocument.createNCXResource(book);
             is = resource.getInputStream();
             IOUtil.copy(is, os);
         } catch (Exception e) {
             logger.error("Unable to write TOC!", e);
         } finally {
             try {
                 if (os != null) os.close();
             } catch (Exception e) {
                 logger.error("Unable to close output stream!", e);
             }
             try {
                 if (is != null) is.close();
             } catch (Exception e) {
                 logger.error("Unable to close input stream!", e);
             }
         }
     }
 
     public Element getBodyElement(Document document) throws XMLParseException {
         NodeList nodeList = document.getElementsByTagName("body");
         if (nodeList.getLength() != 1)
             throw new XMLParseException("Unable to find body element!");
         return (Element) nodeList.item(0);
     }
 
     private File convertResourceToFile(Resource resource) throws MalformedURLException {
         String dir = directory.getAbsolutePath();
         String href = resource.getHref();
         String relativePath = "OEBPS/" + href;
         String path = (dir.endsWith("/")) ? dir + relativePath : dir + "/" + relativePath;
         return new File(path);
     }
 
     private File getTableOfContentsFile() {
         String dir = directory.getAbsolutePath();
         String relativePath = "OEBPS/toc.ncx";
         String path = (dir.endsWith("/")) ? dir + relativePath : dir + "/" + relativePath;
         return new File(path);
     }
 }
