 package org.monkey.web.controller;
 
import org.monkey.common.utils.config.ApplicationStartupUtils;
 import org.restlet.data.MediaType;
 import org.restlet.representation.FileRepresentation;
 import org.restlet.representation.Representation;
 import org.restlet.representation.StringRepresentation;
 import org.restlet.resource.Get;
 import org.restlet.resource.ServerResource;
 import org.springframework.context.annotation.Scope;
 import org.springframework.stereotype.Controller;
 
 import java.util.Map;
 import java.util.TreeMap;
 
 @Scope("prototype")
 @Controller
 public class StatusController extends ServerResource {
 
     @Get("html")
     public FileRepresentation view() {
 //        final Map<String, Object> dataModel = new TreeMap<String, Object>();
 //            dataModel.put("user", this.user);
 //            dataModel.put("contacts", this.contacts);
 //            dataModel.put("resourceRef", getRequest().getResourceRef());
 //            dataModel.put("rootRef", getRequest().getRootRef());
 //
 //            return getTemplateRepresentation("user.html", dataModel, MediaType.TEXT_HTML);
 
 //        return "<html><body>Status Page</body></html>";
        return new FileRepresentation(ApplicationStartupUtils.getWarPath() + "/status.html", MediaType.TEXT_HTML);
     }
 
 //    protected Representation getTemplateRepresentation(String templateName,
 //            Map<String, Object> dataModel, MediaType mediaType) {
 //        // The template representation is based on Freemarker.
 //        return new TemplateRepresentation(templateName, getFmcConfiguration(),
 //                dataModel, mediaType);
 //    }
 }
