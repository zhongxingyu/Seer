 package {{name}};
 
import juzu.*;
 import juzu.template.Template;
import juzu.request.RenderContext;
 import javax.inject.Inject;
 import javax.portlet.PortletPreferences;
 
 /** @author <a href="mailto:benjamin.paillereau@exoplatform.com">Benjamin Paillereau</a> */
 public class Controller
 {
    /** . */
    @Inject
    @Path("index.gtmpl")
    Template indexTemplate;
 
    @Inject
    PortletPreferences portletPreferences;
 
    @View
   public Response.Content index(RenderContext renderContext)
    {
       String size = portletPreferences.getValue("size", "128");
        
      return indexTemplate.with().set("size", size).ok();
    }
 
 }
