 package org.jenkinsci.plugins.sharedobjects.type;
 
 import groovy.lang.GroovyShell;
 import hudson.Extension;
 import hudson.model.AbstractBuild;
 import org.jenkinsci.plugins.sharedobjects.MultipleSharedObjectType;
 import org.jenkinsci.plugins.sharedobjects.SharedObjectException;
 import org.jenkinsci.plugins.sharedobjects.SharedObjectType;
 import org.jenkinsci.plugins.sharedobjects.SharedObjectTypeDescriptor;
 import org.jenkinsci.plugins.sharedobjects.service.SharedObjectLogger;
 import org.kohsuke.stapler.DataBoundConstructor;
 
 import java.util.HashMap;
 import java.util.Map;
 
 /**
  * @author Gregory Boissinot
  */
 public class GroovySharedObjectType extends MultipleSharedObjectType {
 
     private String content;
 
     @DataBoundConstructor
     public GroovySharedObjectType(String name, String profiles, String content) {
         super(name, profiles);
         this.content = content;
     }
 
     @SuppressWarnings("unused")
     public String getContent() {
         return content;
     }
 
     @Override
     public Map<String, String> getEnvVars(AbstractBuild build, SharedObjectLogger logger) throws SharedObjectException {
         if (content == null) {
             return new HashMap<String, String>();
         }
 
         if (content.trim().length() == 0) {
             return new HashMap<String, String>();
         }
 
         logger.info(String.format("Evaluation the following Groovy script content: \n%s\n", content));
        GroovyShell shell = new GroovyShell();
         Object groovyResult = shell.evaluate(content);
         if (groovyResult != null && !(groovyResult instanceof Map)) {
             throw new SharedObjectException("The evaluated Groovy script must return a Map object.");
         }
 
         Map<String, String> result = new HashMap<String, String>();
         if (groovyResult == null) {
             return result;
         }
 
         for (Map.Entry entry : (((Map<Object, Object>) groovyResult).entrySet())) {
             result.put(String.valueOf(entry.getKey()), String.valueOf(entry.getValue()));
         }
         return result;
 
     }
 
     @Extension
     public static class GroovySharedObjectTypeDescriptor extends SharedObjectTypeDescriptor {
 
         @Override
         public String getDisplayName() {
             return "Groovy script";
         }
 
         @Override
         public Class<? extends SharedObjectType> getType() {
             return GroovySharedObjectType.class;
         }
     }
 }
