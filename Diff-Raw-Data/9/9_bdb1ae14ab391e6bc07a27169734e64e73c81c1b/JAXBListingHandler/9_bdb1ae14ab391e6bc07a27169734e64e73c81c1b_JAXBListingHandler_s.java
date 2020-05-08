 /**
  * 
  */
 package cz.cuni.mff.peckam.java.origamist.services;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.URI;
 import java.net.URL;
 
 import javax.xml.bind.JAXBContext;
 import javax.xml.bind.JAXBException;
 import javax.xml.bind.MarshalException;
 import javax.xml.bind.Marshaller;
 
 import cz.cuni.mff.peckam.java.origamist.exceptions.UnsupportedDataFormatException;
 import cz.cuni.mff.peckam.java.origamist.files.Listing;
 import cz.cuni.mff.peckam.java.origamist.files.ObjectFactory;
 import cz.cuni.mff.peckam.java.origamist.jaxb.BindingsController;
 import cz.cuni.mff.peckam.java.origamist.jaxb.BindingsManager;
 import cz.cuni.mff.peckam.java.origamist.services.interfaces.ListingHandler;
 import cz.cuni.mff.peckam.java.origamist.utils.URIAdapter;
 
 /**
  * Handles loading and exporting a listing.xml file using JAXB.
  * 
  * @author Martin Pecka
  */
 public class JAXBListingHandler extends Service implements ListingHandler
 {
 
     /** The namespace of the newest schema. */
     public static final String LATEST_SCHEMA_NAMESPACE = "http://www.mff.cuni.cz/~peckam/java/origamist/files/v1";
 
     /** The listing to return. */
     protected Listing          listing                 = null;
 
     @Override
     public void export(Listing listing, File file) throws IOException, MarshalException, JAXBException
     {
         export(listing, file, null, null);
     }
 
     @Override
     public void export(Listing listing, File file, URI relativeBase, URI currentBase) throws IOException,
             MarshalException, JAXBException
     {
         if (!file.exists())
             file.createNewFile();
         if (!file.isFile())
             throw new IOException("Cannot save listing.xml in a directory or a non-file object: "
                     + file.getAbsolutePath() + ".");
 
         JAXBContext context = JAXBContext.newInstance("cz.cuni.mff.peckam.java.origamist.files.jaxb", getClass()
                 .getClassLoader());
         Marshaller m = context.createMarshaller();
 
         // enable indenting and newline generation
         m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
 
         // make URLs in the listing relative to the location we save the listing to
         m.setAdapter(new URIAdapter());
         m.getAdapter(URIAdapter.class).setCurrentBase(currentBase);
         if (relativeBase == null) {
             m.getAdapter(URIAdapter.class).setRelativeBase(file.getParentFile().toURI());
         } else {
             m.getAdapter(URIAdapter.class).setRelativeBase(relativeBase);
         }
 
         // do the Java class->XML conversion
         m.marshal(new ObjectFactory().createListing(listing), file);
     }
 
     @Override
     public Listing load(final URL path) throws IOException, UnsupportedDataFormatException
     {
         try {
             BindingsManager manager = ServiceLocator.get(BindingsManager.class);
             BindingsController<Listing> controller = new BindingsController<Listing>(manager, LATEST_SCHEMA_NAMESPACE);
            return controller.unmarshal(new InputStreamReader(path.openStream()));
         } catch (JAXBException e) {
             throw new UnsupportedDataFormatException();
         }
     }
 }
