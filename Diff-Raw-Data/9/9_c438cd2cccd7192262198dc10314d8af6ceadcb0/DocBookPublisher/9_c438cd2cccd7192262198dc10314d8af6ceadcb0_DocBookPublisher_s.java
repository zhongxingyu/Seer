 // ============================================================================
 //   Copyright 2008-2012 Daniel W. Dyer
 //
 //   Licensed under the Apache License, Version 2.0 (the "License");
 //   you may not use this file except in compliance with the License.
 //   You may obtain a copy of the License at
 //
 //       http://www.apache.org/licenses/LICENSE-2.0
 //
 //   Unless required by applicable law or agreed to in writing, software
 //   distributed under the License is distributed on an "AS IS" BASIS,
 //   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 //   See the License for the specific language governing permissions and
 //   limitations under the License.
 // ============================================================================
 package org.uncommons.antlib.tasks.docbook;
 
 import java.io.BufferedOutputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
import java.net.URL;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.util.Map;
 import javax.xml.transform.Source;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerConfigurationException;
 import javax.xml.transform.TransformerException;
 import javax.xml.transform.TransformerFactory;
 import javax.xml.transform.URIResolver;
 import javax.xml.transform.dom.DOMResult;
 import javax.xml.transform.sax.SAXResult;
 import javax.xml.transform.sax.SAXSource;
 import javax.xml.transform.sax.SAXTransformerFactory;
 import javax.xml.transform.sax.TemplatesHandler;
 import javax.xml.transform.stream.StreamResult;
 import javax.xml.transform.stream.StreamSource;
 import org.apache.fop.apps.FOPException;
 import org.apache.fop.apps.Fop;
 import org.apache.fop.apps.FopFactory;
 import org.xml.sax.InputSource;
 import org.xml.sax.SAXException;
 import org.xml.sax.XMLReader;
 import org.xml.sax.helpers.XMLReaderFactory;
 
 /**
  * Utility class for generating formatted output from DocBook source files.
  * @author Daniel Dyer
  */
 public class DocBookPublisher
 {
     /**
      * Path to DocBook FO stylesheet on the classpath.
      */
     private static final String STYLESHEET_PATH = "custom-xsl/";
 
     /**
      * Path to XSLTHL config file on the classpath.
      */
     private static final String HIGHLIGHTER_CONFIG_PATH = "docbook-xsl/highlighting/xslthl-config.xml";
 
     private static final TransformerFactory TRANSFORMER_FACTORY;
     static
     {
         // Process XInclude tags.
         System.setProperty("org.apache.xerces.xni.parser.XMLParserConfiguration",
                            "org.apache.xerces.parsers.XIncludeParserConfiguration");
 
         TRANSFORMER_FACTORY = TransformerFactory.newInstance();
     }
 
     private final OutputFormat format;
     private final boolean chunked;
     private final Transformer docBookTransformer;
 
 
     /**
      * Create a DocBookPublisher that generates output in the specified format.
     * @param outputFormat A String contant representing one of the values of
      * {@link OutputFormat}.
      * @param chunked Whether the output should be divided into multiple files
      * (specifically for HTML output).
      * @throws IOException If there is a problem reading the XSL stylesheet from
      * the classpath.
      * @throws SAXException If there is a problem parsing the XSL stylesheet.
      * @throws TransformerConfigurationException If there is a problem setting up
      * the XSL processor that will be used for transforming the DocBook source.
      */
     public DocBookPublisher(String outputFormat,
                             boolean chunked) throws IOException,
                                                     TransformerConfigurationException,
                                                     SAXException
     {
         // Set-up source-code highlighting.
         URL configURL = getClass().getClassLoader().getResource(HIGHLIGHTER_CONFIG_PATH);
         System.setProperty("xslthl.config", configURL.toExternalForm());
 
         format = OutputFormat.valueOf(outputFormat.toUpperCase());
         this.chunked = chunked;
 
         SAXTransformerFactory saxTransformerFactory = (SAXTransformerFactory) TRANSFORMER_FACTORY;
 
         TemplatesHandler templatesHandler = saxTransformerFactory.newTemplatesHandler();
         XMLReader reader = XMLReaderFactory.createXMLReader();
         reader.setContentHandler(templatesHandler);
 
         URL styleSheetURL = getClass().getClassLoader().getResource(STYLESHEET_PATH + format.getStylesheet(chunked));
         InputSource inputSource = new InputSource(styleSheetURL.openStream());
         inputSource.setSystemId(styleSheetURL.toExternalForm());
         Source styleSheetSource = new SAXSource(reader, inputSource);
         docBookTransformer = TRANSFORMER_FACTORY.newTransformer(styleSheetSource);
     }
 
 
     public void createDocument(File docbookSourceFile,
                                File outputDirectory,
                                Map<String, String> parameters) throws IOException,
                                                                       TransformerException,
                                                                       FOPException
     {
         OutputStream outputStream = null;
         InputStream docbookInputStream = new FileInputStream(docbookSourceFile);
         try
         {
             Source docbookSource = new SAXSource(new InputSource(docbookInputStream));
             docbookSource.setSystemId(docbookSourceFile.getPath());
 
             // If the output file is a directory, that is the base directory.  If it is a file,
             // the base directory is the directory that contains the file.
             docBookTransformer.setParameter("base.dir", outputDirectory.getAbsolutePath() + File.separator);
 
             // Set DocBook XSL parameters.
             for (Map.Entry<String, String> entry : parameters.entrySet())
             {
                 docBookTransformer.setParameter(entry.getKey(), entry.getValue());
             }
 
             if (format.getFopMimeType() == null) // FO is not used as an intermediate representation (e.g. HTML).
             {
                 if (chunked)
                 {
                     docBookTransformer.transform(docbookSource, new DOMResult()); // DOMResult is just a place-holder, it's not used.
                 }
                 else
                 {
                     outputStream = getFileOutputStream(docbookSourceFile, outputDirectory);
                     docBookTransformer.transform(docbookSource, new StreamResult(outputStream));
                 }
             }
             else  // FO is used as an intermediate representation (e.g PDF/RTF).
             {
                 // FOP extensions enable bookmarks in PDF output.
                 docBookTransformer.setParameter("fop1.extensions", 1);
                 
                 FopFactory fopFactory = FopFactory.newInstance();
                 fopFactory.setBaseURL(docbookSourceFile.getParentFile().toURI().toString());
                 fopFactory.setURIResolver(new ImageURIResolver());
 
                 outputStream = getFileOutputStream(docbookSourceFile, outputDirectory);
                 Fop fop = fopFactory.newFop(format.getFopMimeType(), outputStream);
                 docBookTransformer.transform(docbookSource,
                                              new SAXResult(fop.getDefaultHandler()));
             }
         }
         finally
         {
             docbookInputStream.close();
             if (outputStream != null)
             {
                 outputStream.close();
             }
         }
     }
 
 
     /**
      * Determine the name of the target file for non-chunked formats and return a stream for writing to.
      * @param docbookSourceFile The DocBook source (the output file name is derived
      * from the name of this file).
      * @param outputDirectory The directory in which the output will be written.
      * @return The output stream.
      * @throws FileNotFoundException If the output directory does not exist.
      */
     private OutputStream getFileOutputStream(File docbookSourceFile,
                                              File outputDirectory) throws FileNotFoundException
     {
         String root = docbookSourceFile.getName().substring(0, docbookSourceFile.getName().lastIndexOf('.'));
         File outputFile = new File(outputDirectory, root + format.getFileExtension());
         return new BufferedOutputStream(new FileOutputStream(outputFile));
     }
 
 
     
     private static class ImageURIResolver implements URIResolver
     {
         public Source resolve(String href, String base) throws TransformerException
         {
             // If the resource doesn't exist on the file system but is on the
             // classpath, it's probably one of the DocBook images, so we return a
             // stream for accessing it. Otherwise, leave it for something else to deal with.
             try
             {
                 File file = new File(new URI(base + href));
                 if (!file.exists())
                 {
                     URL resource = getClass().getClassLoader().getResource(STYLESHEET_PATH + href);
                     if (resource != null)
                     {
                         return new StreamSource(resource.openStream());
                     }
                 }
                 return null;
             }
             catch (URISyntaxException ex)
             {
                 throw new IllegalStateException(ex);
             }
             catch (IOException ex)
             {
                 ex.printStackTrace();
                 return null;
             }
         }
     }
 }
