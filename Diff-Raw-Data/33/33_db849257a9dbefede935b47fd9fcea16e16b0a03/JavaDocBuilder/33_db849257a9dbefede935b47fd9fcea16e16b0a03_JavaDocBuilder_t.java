 package org.arachna.netweaver.javadoc;
 
 import hudson.Extension;
 import hudson.FilePath;
 import hudson.Launcher;
 import hudson.Util;
 import hudson.model.BuildListener;
 import hudson.model.AbstractBuild;
 import hudson.model.AbstractProject;
 import hudson.tasks.BuildStepDescriptor;
 import hudson.tasks.Builder;
 
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.StringWriter;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.List;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerConfigurationException;
 import javax.xml.transform.TransformerException;
 import javax.xml.transform.TransformerFactory;
 import javax.xml.transform.TransformerFactoryConfigurationError;
 import javax.xml.transform.dom.DOMSource;
 import javax.xml.transform.stream.StreamResult;
 import javax.xml.transform.stream.StreamSource;
 
 import net.sf.json.JSONObject;
 
 import org.apache.tools.ant.BuildException;
 import org.arachna.netweaver.dc.types.Compartment;
 import org.arachna.netweaver.dc.types.CompartmentByNameComparator;
 import org.arachna.netweaver.dc.types.CompartmentState;
 import org.arachna.netweaver.dc.types.DevelopmentComponent;
 import org.arachna.netweaver.dc.types.DevelopmentConfiguration;
 import org.arachna.netweaver.hudson.nwdi.DCWithJavaSourceAcceptingFilter;
 import org.arachna.netweaver.hudson.nwdi.NWDIBuild;
 import org.arachna.netweaver.hudson.nwdi.NWDIProject;
 import org.kohsuke.stapler.DataBoundConstructor;
 import org.kohsuke.stapler.StaplerRequest;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 
 /**
  * {@link Builder} for JavaDoc documentation for SAP NetWeaver development
  * components.
  *
  * @author Dirk Weigenand
  */
 public class JavaDocBuilder extends Builder {
 
     /**
      * URLs to JavaDoc generated elsewhere that should be linked into the
      * generated javadoc documentation.
      */
     private Collection<URL> links = new HashSet<URL>();
 
     /**
      * @return the links
      */
     public String getLinks() {
         StringBuilder links = new StringBuilder();
 
         for (URL link : this.links) {
             links.append(link.toString()).append('\n');
         }
 
         return links.toString();
     }
 
     // Fields in config.jelly must match the parameter names in the
     // "DataBoundConstructor"
     @DataBoundConstructor
     public JavaDocBuilder(String links) {
         setLinks(links);
     }
 
     private void setLinks(String links) {
         if (links != null) {
             for (String link : links.split("\n\r")) {
                 String trimmedLink = link.trim();
 
                 if (trimmedLink.length() > 0) {
                     try {
                         this.links.add(new URL(trimmedLink));
                     }
                     catch (MalformedURLException mfue) {
                         // FIXME: Should say something about malformed package
                         // list urls
                     }
                 }
             }
         }
     }
 
     @Override
     public boolean perform(final AbstractBuild build, final Launcher launcher, final BuildListener listener) {
         final NWDIBuild nwdiBuild = (NWDIBuild)build;
         final JavaDocExecutor executor =
             new JavaDocExecutor(nwdiBuild.getDevelopmentConfiguration(), nwdiBuild.getAntHelper(listener.getLogger()),
                 nwdiBuild.getDevelopmentComponentFactory(), this.links);
 
         for (final DevelopmentComponent component : nwdiBuild
             .getAffectedDevelopmentComponents(new DCWithJavaSourceAcceptingFilter())) {
             long start = System.currentTimeMillis();
 
             try {
                 listener.getLogger().append(
                     String.format("Running JavaDoc on %s:%s...", component.getVendor(), component.getName()));
                 executor.execute(component);
             }
             catch (BuildException be) {
                 listener.getLogger().append(
                     String.format("\n%s: %s\n", be.getLocalizedMessage().trim(), component.getSourceFolders()));
             }
             finally {
                 listener.getLogger().append(
                     String.format(" (%f sec.).\n", (System.currentTimeMillis() - start) / 1000f));
             }
         }
 
         FilePath javaDocFolder = nwdiBuild.getWorkspace().child("javadoc");
 
         try {
             FilePath indexHtml = javaDocFolder.child("index.html");
             indexHtml.write(createIndexHtml(nwdiBuild.getDevelopmentConfiguration()), "UTF-8");
             StringWriter style = new StringWriter();
             Util.copyStream(new InputStreamReader(this.getClass().getResourceAsStream("style.css")), style);
             javaDocFolder.child("style.css").write(style.toString(), "UTF-8");
         }
         catch (IOException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         }
         catch (InterruptedException e) {
             // ignore
         }
 
         return true;
     }
 
     /**
     * Generate <code>index.html</code> as an entry point for all available
     * JavaDoc documentation.
     *
     * @param configuration
     *            {@link DevelopmentConfiguration} to generate entry page for.
      */
     protected String createIndexHtml(DevelopmentConfiguration configuration) {
         StringWriter result = new StringWriter();
 
         try {
             Transformer transformer =
                 TransformerFactory.newInstance().newTransformer(
                     new StreamSource(this.getClass().getResourceAsStream("JavaDocIndex.xsl")));
             transformer.setParameter("track", configuration.getCaption());
             transformer.transform(createIndexDom(configuration), new StreamResult(result));
         }
         catch (IOException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         }
         catch (TransformerConfigurationException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         }
         catch (TransformerFactoryConfigurationError e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         }
         catch (TransformerException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         }
         catch (ParserConfigurationException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         }
 
         return result.toString();
     }
 
     /**
      * @param javaDocFolder
      * @return
      */
     public DOMSource createIndexDom(DevelopmentConfiguration configuration) throws ParserConfigurationException,
         IOException {
         DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
         Document document = builder.newDocument();
         Element compartments = document.createElement("compartments");
         document.appendChild(compartments);
 
         for (Compartment compartment : getNonEmptyCompartmentsSortedAlphabetically(configuration)) {
             Collection<DevelopmentComponent> components = this.getDevelopmentComponentsWithJavaSources(compartment);
 
             Element compartmentElement = document.createElement("compartment");
             compartmentElement.setAttribute("name", compartment.getSoftwareComponent());
             compartments.appendChild(compartmentElement);
 
             for (DevelopmentComponent component : components) {
                 Element dc = document.createElement("dc");
                 compartmentElement.appendChild(dc);
 
                 dc.setAttribute("vendor", component.getVendor());
                 dc.setAttribute("name", component.getName());
                 dc.setAttribute("folder", component.getVendor() + "~" + component.getName().replace('/', '~'));
 
                 dc.appendChild(document.createTextNode(component.getDescription()));
             }
         }
 
         return new DOMSource(document);
     }
 
     /**
      * @param configuration
      * @return
      */
     private List<Compartment> getNonEmptyCompartmentsSortedAlphabetically(DevelopmentConfiguration configuration) {
         List<Compartment> compartments = new ArrayList<Compartment>();
 
         for (Compartment compartment : configuration.getCompartments(CompartmentState.Source)) {
             if (!compartment.getDevelopmentComponents().isEmpty()) {
                 compartments.add(compartment);
             }
         }
 
         Collections.sort(compartments, new CompartmentByNameComparator());
 
         return compartments;
     }
 
     private Collection<DevelopmentComponent> getDevelopmentComponentsWithJavaSources(Compartment compartment) {
         Collection<DevelopmentComponent> components = new ArrayList<DevelopmentComponent>();
         DCWithJavaSourceAcceptingFilter filter = new DCWithJavaSourceAcceptingFilter();
 
         for (DevelopmentComponent component : compartment.getDevelopmentComponents()) {
             if (filter.accept(component)) {
                 components.add(component);
             }
         }
 
         return components;
     }
 
     // Overridden for better type safety.
     // If your plugin doesn't really define any property on Descriptor,
     // you don't have to do this.
     @Override
     public DescriptorImpl getDescriptor() {
         return (DescriptorImpl)super.getDescriptor();
     }
 
     /**
      * Descriptor for {@link JavaDocBuilder}. Used as a singleton. The class is
      * marked as public so that it can be accessed from views.
      */
     @Extension
     // This indicates to Jenkins that this is an implementation of an extension
     // point.
     public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {
         @Override
         public boolean isApplicable(final Class<? extends AbstractProject> aClass) {
             return NWDIProject.class.equals(aClass);
         }
 
         /**
          * This human readable name is used in the configuration screen.
          */
         @Override
         public String getDisplayName() {
            return "NWDI JavaDoc Builder";
         }
 
         @Override
         public boolean configure(final StaplerRequest req, final JSONObject formData) throws FormException {
             save();
             return super.configure(req, formData);
         }
     }
 }
