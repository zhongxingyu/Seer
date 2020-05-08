 package txtfnnl.uima.resource;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.net.URI;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import org.apache.uima.resource.DataResource;
 import org.apache.uima.resource.ExternalResourceDescription;
 import org.apache.uima.resource.ResourceInitializationException;
 
 import org.uimafit.component.ExternalResourceAware;
 import org.uimafit.component.initialize.ConfigurationParameterInitializer;
 import org.uimafit.descriptor.ConfigurationParameter;
 import org.uimafit.factory.ExternalResourceFactory;
 
 /**
  * A generic implementation of a StringArrayResource that reads data from a line-based stream with
  * the data separated by some separator. By default, tab is used as separator, and acts like a
  * tab-separated value file reader.
  * 
  * @author Florian Leitner
  */
 public class LineBasedStringArrayResource implements StringArrayResource, ExternalResourceAware {
   @ConfigurationParameter(name = ExternalResourceFactory.PARAM_RESOURCE_NAME)
   private String resourceName;
   protected String resourceUrl;
   /**
    * The field separator to use. May be a regular expression and defaults to a tab. Input lines are
    * split on this regular expression.
    */
   public static final String PARAM_SEPARATOR = "FieldSeparator";
   @ConfigurationParameter(name = PARAM_SEPARATOR, mandatory = false, defaultValue = "(?<!\\\\)\t")
   protected String separator;
   protected List<String[]> resource = new ArrayList<String[]>();
 
   /**
    * Configure a new resource. In the case of a file, make sure that the (data) resource URL is
    * prefixed with the "file:" schema prefix.
    * 
    * @param resourceUrl the URL where this resource is located
    * @param separator the regex used to separate the values (if <code>null</code> or empty, use
    *        default - see {@link #PARAM_SEPARATOR} )
    */
   public static ExternalResourceDescription configure(String resourceUrl, String separator) {
     if (separator == null || separator.length() == 0) return ExternalResourceFactory
         .createExternalResourceDescription(LineBasedStringArrayResource.class, resourceUrl);
     else return ExternalResourceFactory.createExternalResourceDescription(
         LineBasedStringArrayResource.class, resourceUrl, PARAM_SEPARATOR, separator);
   }
 
   /**
    * Configure a new resource using the default separator.
    * 
    * @param resourceUrl the URL where this resource is located
    * @return a configured resource description
    */
   public static ExternalResourceDescription configure(String resourceUrl) {
     return LineBasedStringArrayResource.configure(resourceUrl, null);
   }
 
   public void load(DataResource data) throws ResourceInitializationException {
     ConfigurationParameterInitializer.initialize(this, data);
     final URI uri = data.getUri();
     if (uri != null) {
       resourceUrl = uri.toString();
     }
     InputStream inStr = null;
     try {
       // open input stream to data
      inStr = data.getInputStream();
       // read each line
       final BufferedReader reader = new BufferedReader(new InputStreamReader(inStr));
       String line;
       while ((line = reader.readLine()) != null) {
         line = line.trim();
         if (line.length() > 0) resource.add(line.split(separator));
       }
     } catch (final IOException e) {
       throw new ResourceInitializationException(e);
     } finally {
       if (inStr != null) {
         try {
           inStr.close();
         } catch (final IOException e) {}
       }
     }
   }
 
   /** Fetch a particular row. */
   public String[] get(int row) {
     return resource.get(row);
   }
 
   /** Return the number of rows this resource holds. */
   public int size() {
     return resource.size();
   }
 
   /**
    * Return an iterator over a copy of the resource (i.e., <code>iter.remove()</code> has no effect
    * on the underlying resource).
    */
   public Iterator<String[]> iterator() {
     return new ArrayList<String[]>(resource).iterator();
   }
 
   public String getResourceName() {
     return resourceName;
   }
 
   public String getResourceUrl() {
     return resourceUrl;
   }
 
   public void afterResourcesInitialized() {
     // nothing to do here...
   }
 }
