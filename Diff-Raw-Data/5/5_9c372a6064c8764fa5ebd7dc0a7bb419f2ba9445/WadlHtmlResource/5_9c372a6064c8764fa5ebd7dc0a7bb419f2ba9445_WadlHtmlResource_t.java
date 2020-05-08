 package edu.northwestern.bioinformatics.studycalendar.restlets;
 
 import org.restlet.resource.Resource;
 import org.restlet.resource.TransformRepresentation;
 import org.restlet.resource.Representation;
 import org.restlet.resource.ResourceException;
 import org.restlet.resource.Variant;
 import org.restlet.Context;
 import org.restlet.data.Request;
 import org.restlet.data.Response;
 import org.restlet.data.MediaType;
 import freemarker.template.Configuration;
 
 /**
  * @author Rhett Sutphin
  */
 public class WadlHtmlResource extends Resource {
     private static final String WSDL_DOC_XSLT = "/edu/northwestern/bioinformatics/studycalendar/restlets/wadl_documentation.xsl";
 
     private Configuration freemarkerConfiguration;
 
     @Override
     public void init(Context context, Request request, Response response) {
         super.init(context, request, response);
         getVariants().add(new Variant(MediaType.TEXT_HTML));
     }
 
     @Override
     public Representation represent(Variant variant) throws ResourceException {
         if (MediaType.TEXT_HTML.includes(variant.getMediaType())) {
            TransformRepresentation transform = new TransformRepresentation(null,
                 PscWadlRepresentation.create(freemarkerConfiguration, getRequest()),
                 new ClasspathResourceRepresentation(MediaType.TEXT_XML, WSDL_DOC_XSLT)
             );
            transform.setMediaType(MediaType.TEXT_HTML);
            return transform;
         } else {
             return null;
         }
     }
 
     public void setFreemarkerConfiguration(Configuration freemarkerConfiguration) {
         this.freemarkerConfiguration = freemarkerConfiguration;
     }
 
 }
