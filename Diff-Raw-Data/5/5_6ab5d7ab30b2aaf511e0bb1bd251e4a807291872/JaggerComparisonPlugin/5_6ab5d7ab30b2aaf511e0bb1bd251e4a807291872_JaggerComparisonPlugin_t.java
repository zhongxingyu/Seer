 package com.griddynamics.jagger.jenkins.plugin;
 
 import hudson.Extension;
 import hudson.Launcher;
 import hudson.model.AbstractBuild;
 import hudson.model.AbstractProject;
 import hudson.model.BuildListener;
 import hudson.model.Result;
 import hudson.tasks.BuildStepDescriptor;
 import hudson.tasks.Builder;
 import hudson.util.FormValidation;
 import org.kohsuke.stapler.DataBoundConstructor;
 import org.kohsuke.stapler.QueryParameter;
 import org.w3c.dom.Document;
 import org.xml.sax.SAXException;
 
 import javax.servlet.ServletException;
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.xpath.*;
import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.PrintStream;
 
 
 public class JaggerComparisonPlugin extends Builder {
 
     protected enum Decision{FATAL, OK, WARNING}
 
     protected final String XPATH_DECISION="/report/decision/text()";
 
     private final String path;
     private final boolean ignoreErrors;
 
     @DataBoundConstructor
     public JaggerComparisonPlugin(String path,boolean ignoreErrors) {
         this.path = path;
         this.ignoreErrors = ignoreErrors;
     }
 
     public String getPath() {
         return path;
     }
 
     public boolean getIgnoreErrors() {
         return ignoreErrors;
     }
 
     protected Decision getDicision() throws ParserConfigurationException, IOException, SAXException, IllegalArgumentException, XPathExpressionException {
         DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
         factory.setValidating(true);
         DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(new FileInputStream(getPath()));
         XPathFactory xPathFactory = XPathFactory.newInstance();
         XPath xpath = xPathFactory.newXPath();
         XPathExpression expr = xpath.compile(XPATH_DECISION);
         String decision= expr.evaluate(doc, XPathConstants.STRING).toString();
         return Decision.valueOf(decision);
     }
     @Override
     public boolean perform(AbstractBuild build, Launcher launcher, BuildListener listener) {
         PrintStream logger=listener.getLogger();
         Decision decision;
         try{
             decision=getDicision();
             log(logger,"Result of comparison: " + decision);
             switch (decision){
                 case FATAL:{
                     build.setResult(Result.FAILURE);
                     break;
                 }
                 case OK:{
                     build.setResult(Result.SUCCESS);
                     break;
                 }
                 case WARNING:{
                     build.setResult(Result.UNSTABLE);
                     break;
                 }
             }
             return true;
         } catch (SAXException e) {
             log(logger, "Error while parsing file: " + path + ": " + e);
         } catch (XPathExpressionException e) {
             log(logger, "Error while compiling Xpath: " + XPATH_DECISION + ": " + e);
         } catch (IOException e) {
             log(logger, "Error while reading file: " + path + ": " + e);
         } catch (ParserConfigurationException e) {
             log(logger, "Error while parsing file: " + path + ": " + e);
         } catch (IllegalArgumentException e) {
             log(logger, "Error while getting decision: " + path + ": " + e);
         } catch (Exception e){
             log(logger,"Plugin error: "+e);
             return false;
         }
         if(getIgnoreErrors()){
             return true;
         }
         return false;
 
     }
 
     private void log(java.io.PrintStream stream, String message){
         stream.println(message);
     }
 
     @Override
     public DescriptorImpl getDescriptor() {
         return (DescriptorImpl)super.getDescriptor();
     }
 
     @Extension
     public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {
         public FormValidation doCheckPath(@QueryParameter String value)
                 throws IOException, ServletException {
             if (value.length() == 0){
                 return FormValidation.error("Please set a path");
             }
             return FormValidation.ok();
         }
 
         public boolean isApplicable(Class<? extends AbstractProject> aClass) {
             return true;
         }
 
         public String getDisplayName() {
             return "Jagger session comparison";
         }
     }
 }
 
