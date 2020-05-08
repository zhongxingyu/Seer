 package hudson.plugins.googlecode;
 
 import hudson.model.Descriptor;
 import hudson.scm.EditType;
 import hudson.scm.RepositoryBrowser;
 import hudson.scm.SubversionChangeLogSet.LogEntry;
 import hudson.scm.SubversionChangeLogSet.Path;
 import hudson.scm.SubversionRepositoryBrowser;
 
import net.sf.json.JSONObject;

 import org.kohsuke.stapler.DataBoundConstructor;
 import org.kohsuke.stapler.StaplerRequest;
 
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.URL;
 
 /**
  * {@link SubversionRepositoryBrowser} that produces Google Code links.
  * 
  * @author Kohsuke Kawaguchi
  * @author Erik Ramfelt
  */
 public class GoogleCodeRepositoryBrowser extends SubversionRepositoryBrowser {
 
     private static final long serialVersionUID = 1L;
 
     private transient GoogleCodeProjectProperty.PropertyRetriever propertyRetriever;
     
     @DataBoundConstructor
     public GoogleCodeRepositoryBrowser(GoogleCodeProjectProperty.PropertyRetriever retriever) {
         propertyRetriever = retriever;
     }
 
     /**
      * Gets a URL for the {@link GoogleCodeProjectProperty#googlecodeWebsite} value
      * configured for the current project.
      */
     private URL getGoogleCodeWebURL(LogEntry cs) throws MalformedURLException {
         if (propertyRetriever == null) {
             propertyRetriever = PluginImpl.PROJECT_PROPERTY_DESCRIPTOR;
         }
         GoogleCodeProjectProperty property = propertyRetriever.getProperty(cs);
         if ((property == null) || (property.googlecodeWebsite == null))
             return null;
         else
             return new URL(property.googlecodeWebsite);
     }
 
     @Override
     public URL getDiffLink(Path path) throws IOException {
         if(path.getEditType()!= EditType.EDIT)
             return null;    // no diff if this is not an edit change
         URL baseUrl = getGoogleCodeWebURL(path.getLogEntry());
         int revision = path.getLogEntry().getRevision();
         return new URL(baseUrl, "source/diff?r=" + revision + "&format=side&path=" + path.getValue());
     }
 
     @Override
     public URL getFileLink(Path path) throws IOException {
         URL baseUrl = getGoogleCodeWebURL(path.getLogEntry());
         int revision = path.getLogEntry().getRevision();
         return baseUrl == null ? null : new URL(baseUrl, "source/browse" + path.getValue() + "?r=" + revision + "#1");
     }
 
     @Override
     public URL getChangeSetLink(LogEntry changeSet) throws IOException {
         URL baseUrl = getGoogleCodeWebURL(changeSet);
         return baseUrl == null ? null : new URL(baseUrl, "source/detail?r=" + changeSet.getRevision());
     }
 
     public DescriptorImpl getDescriptor() {
         return PluginImpl.REPOSITORY_BROWSER_DESCRIPTOR;
     }
 
     public static final class DescriptorImpl extends Descriptor<RepositoryBrowser<?>> {
         public DescriptorImpl() {
             super(GoogleCodeRepositoryBrowser.class);
         }
 
         @Override
         public String getDisplayName() {
             return "Google Code";
         }
 
         @Override
        public GoogleCodeRepositoryBrowser newInstance(StaplerRequest req, JSONObject formData) throws FormException {
            return new GoogleCodeRepositoryBrowser(PluginImpl.PROJECT_PROPERTY_DESCRIPTOR);        
         }
     }
 }
