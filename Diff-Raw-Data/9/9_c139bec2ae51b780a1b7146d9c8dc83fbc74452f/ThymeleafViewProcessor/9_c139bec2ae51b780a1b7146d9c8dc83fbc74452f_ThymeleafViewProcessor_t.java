 package de.deepamehta.plugins.webactivator.provider;
 
 import de.deepamehta.plugins.webactivator.WebActivatorPlugin;
 import de.deepamehta.core.util.JavaUtils;
 
 import com.sun.jersey.api.view.Viewable;
 import com.sun.jersey.spi.template.ViewProcessor;
 
 import org.thymeleaf.TemplateEngine;
 import org.thymeleaf.context.IContext;
 
 import javax.ws.rs.core.Context;
 import javax.ws.rs.core.UriInfo;
 import javax.ws.rs.ext.Provider;
 
 import java.io.BufferedWriter;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.io.OutputStreamWriter;
 import java.io.Writer;
 import java.util.List;
 import java.util.logging.Logger;
 
 
 
 @Provider
 public class ThymeleafViewProcessor implements ViewProcessor<String> {
 
     // ---------------------------------------------------------------------------------------------- Instance Variables
 
     @Context
     private UriInfo uriInfo;
 
     private Logger logger = Logger.getLogger(getClass().getName());
 
     // -------------------------------------------------------------------------------------------------- Public Methods
 
     @Override
     public String resolve(String templateName) {
         // Note: By default the Viewable constructor resolves relative template names (as returned by the
         // webapp's resource methods) against the package path of the request's matching resource object.
         // JavaUtils.getFilename() strips that path.
         return "/views/" + JavaUtils.getFilename(templateName) + ".html";
     }
 
     @Override
     public void writeTo(String templateName, Viewable viewable, OutputStream out) throws IOException {
         WebActivatorPlugin plugin = matchedPlugin();
         logger.info("Processing template \"" + templateName + "\" of " + plugin);
         processTemplate(plugin.getTemplateEngine(), templateName, (IContext) viewable.getModel(), out);
     }
 
     // -------------------------------------------------------------------------------------------------- Public Methods
 
     /**
      * Returns the plugin that matches the current request.
      */
     private WebActivatorPlugin matchedPlugin() {
         List<Object> resources = uriInfo.getMatchedResources();
         //
        // Note: sub-resource methods match 2 times. Both with the same resource object.
        // ### TODO: support sub-resource locators
        /* if (resources.size() != 1) {
             throw new RuntimeException("Request path \"" + uriInfo.getPath() + "\" matches " + resources.size() +
                " resource objects " + resources);
        } */
         //
         return (WebActivatorPlugin) resources.get(0);
     }
 
     private void processTemplate(TemplateEngine templateEngine, String templateName, IContext context, OutputStream out)
                                                                                                     throws IOException {
         Writer writer = new BufferedWriter(new OutputStreamWriter(out , "UTF-8"));
         templateEngine.process(templateName, context, writer);
         writer.flush();
     }
 }
