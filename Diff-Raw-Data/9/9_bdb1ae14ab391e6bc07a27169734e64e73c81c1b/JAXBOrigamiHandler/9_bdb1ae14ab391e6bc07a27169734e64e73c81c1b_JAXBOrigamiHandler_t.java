 /**
  * 
  */
 package cz.cuni.mff.peckam.java.origamist.services;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.net.URI;
 import java.net.URL;
 import java.util.concurrent.Callable;
 
 import javax.xml.bind.JAXBContext;
 import javax.xml.bind.JAXBException;
 import javax.xml.bind.MarshalException;
 import javax.xml.bind.Marshaller;
 import javax.xml.bind.UnmarshalException;
 
 import org.apache.log4j.Logger;
 import org.xml.sax.Attributes;
 import org.xml.sax.SAXException;
 
 import cz.cuni.mff.peckam.java.origamist.exceptions.UnsupportedDataFormatException;
 import cz.cuni.mff.peckam.java.origamist.jaxb.AdditionalTransforms.TransformLocation;
 import cz.cuni.mff.peckam.java.origamist.jaxb.BindingsController;
 import cz.cuni.mff.peckam.java.origamist.jaxb.BindingsManager;
 import cz.cuni.mff.peckam.java.origamist.jaxb.ParseAbortedException;
 import cz.cuni.mff.peckam.java.origamist.jaxb.ResultDelegatingDefaultHandler;
 import cz.cuni.mff.peckam.java.origamist.jaxb.TransformInfo;
 import cz.cuni.mff.peckam.java.origamist.model.ObjectFactory;
 import cz.cuni.mff.peckam.java.origamist.model.Origami;
 import cz.cuni.mff.peckam.java.origamist.model.jaxb.Model;
 import cz.cuni.mff.peckam.java.origamist.services.interfaces.OrigamiHandler;
 import cz.cuni.mff.peckam.java.origamist.utils.ExportFormat;
 import cz.cuni.mff.peckam.java.origamist.utils.URIAdapter;
 
 /**
  * Loads an origami model from XML file using JAXB and vice versa.
  * 
  * The code is inspired by partial-unmarshalling example in JAXB section of JWSDP.
  * 
  * @author Martin Pecka
  */
 public class JAXBOrigamiHandler extends Service implements OrigamiHandler
 {
 
     /** The namespace of the newest schema. */
     public static final String LATEST_SCHEMA_NAMESPACE = "http://www.mff.cuni.cz/~peckam/java/origamist/diagram/v1";
 
     /** The model to return. */
     protected Origami          model                   = null;
 
     /** The base path for resolving relative URIs. */
     protected URL              documentBase            = null;
 
     public JAXBOrigamiHandler(URL documentBase)
     {
         this.documentBase = documentBase;
     }
 
     @Override
     public void save(Origami origami, File file) throws IOException, MarshalException, JAXBException
     {
         if (!file.exists())
             file.createNewFile();
         if (!file.isFile())
             throw new IOException("Cannot save the model in a directory or a non-file object: "
                     + file.getAbsolutePath() + ".");
 
         JAXBContext context = JAXBContext.newInstance("cz.cuni.mff.peckam.java.origamist.model.jaxb", getClass()
                 .getClassLoader());
         Marshaller m = context.createMarshaller();
 
         // enable indenting and newline generation
         m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
 
         // make URLs in the listing relative to the location we save the model to
         m.setAdapter(new URIAdapter());
         m.getAdapter(URIAdapter.class).setRelativeBase(file.getParentFile().toURI());
 
         // do the Java class->XML conversion
         m.marshal(new ObjectFactory().createOrigami(origami), file);
     }
 
     @Override
     public void export(Origami origami, File file, ExportFormat format) throws IOException
     {
         if (ExportFormat.XML.equals(format)) {
             try {
                 save(origami, file);
             } catch (MarshalException e) {
                 throw new IOException(e);
             } catch (JAXBException e) {
                 throw new IOException(e);
             }
         } else {
             // TODO other export formats
             Logger.getLogger("application").error("Unsupported export format: " + format);
             throw new IOException("Unsupported export format: " + format);
         }
     }
 
     @Override
     public Origami loadModel(final URI path, boolean onlyMetadata) throws IOException, UnsupportedDataFormatException
     {
         URL url = null;
         if (path.isAbsolute()) {
             url = path.toURL();
         } else {
             url = new URL(documentBase, path.toString());
         }
         return loadModel(url, onlyMetadata);
     }
 
     @Override
     public Origami loadModel(final URL path, boolean onlyMetadata) throws IOException, UnsupportedDataFormatException
     {
         try {
 
             BindingsManager manager = ServiceLocator.get(BindingsManager.class);
             BindingsController<Origami> controller = new BindingsController<Origami>(manager, LATEST_SCHEMA_NAMESPACE);
 
             InputStream input = path.openStream();
 
             if (onlyMetadata) {
                 TransformInfo transform = new TransformInfo(null, null, null, new MetadataLoadingHandler(input));
                 controller.addAdditionalTransform(transform, TransformLocation.BEFORE_UNMARSHALLER, true);
             }
 
            model = controller.unmarshal(new InputStreamReader(input, "UTF8"));
 
             if (onlyMetadata) {
                 // if only metadata are loaded, we need to provide a method to lazily load the rest of the diagram
                 model.setLoadModelCallable(new Callable<Model>() {
                     @Override
                     public Model call() throws Exception
                     {
                         BindingsManager manager = ServiceLocator.get(BindingsManager.class);
                         BindingsController<Origami> controller = new BindingsController<Origami>(manager,
                                 LATEST_SCHEMA_NAMESPACE);
                         return controller.unmarshal(new InputStreamReader(path.openStream())).getModel();
                     }
                 });
             }
 
             model.setSrc(path);
             return (Origami) new ObjectFactory().createOrigami(model).getValue();
         } catch (UnmarshalException e) {
             throw new UnsupportedDataFormatException(e);
         } catch (JAXBException e) {
             throw new UnsupportedDataFormatException(e);
         }
     }
 
     /**
      * @return the documentBase
      */
     public URL getDocumentBase()
     {
         return documentBase;
     }
 
     /**
      * @param documentBase the documentBase to set
      */
     public void setDocumentBase(URL documentBase)
     {
         this.documentBase = documentBase;
     }
 
     /**
      * This XML filter first reads the version of the used schema, then decides, which JAXB unmarshaller it will use,
      * and, if desired, can stop reading when the model's metadata are read (in particular it will close the URL
      * connection immediately after the metadata are loaded).
      * 
      * @author Martin Pecka
      */
     protected class MetadataLoadingHandler extends ResultDelegatingDefaultHandler
     {
         /**
          * The input stream we are reading from. It is needed here in order to be able to prematurely close the
          * connection.
          */
         protected InputStream is;
 
         /** The QName of the root element. */
         protected String      rootElementQName = null;
 
         /**
          * @param is The input stream we are reading from.
          * @param onlyMetadata If true, close the <code>is</code> when the <code>model</code> tag is encountered.
          */
         protected MetadataLoadingHandler(InputStream is)
         {
             this.is = is;
         }
 
         @Override
         public void startElement(String namespaceURI, String localName, String qName, Attributes atts)
                 throws SAXException
         {
             if (namespaceURI.equals(LATEST_SCHEMA_NAMESPACE) && localName.equals("origami")) {
                 rootElementQName = qName;
             } else if (localName.equals("model")) {
                 try {
                     // metadate are loaded, so abort reading from the URL, close the connection
                     is.close();
                 } catch (IOException e) {}
 
                 super.startElement(namespaceURI, localName, qName, atts);
                 // this will process the rest of the unmarshalling with the <model> tag empty
                 endElement(namespaceURI, localName, qName);
                 // this will end the marshalling at all
                 endElement(LATEST_SCHEMA_NAMESPACE, "origami", rootElementQName);
                 endDocument();
 
                 // abort parsing
                 throw new ParseAbortedException();
             }
             super.startElement(namespaceURI, localName, qName, atts);
         }
     }
 }
