 package {{name}};
 
import juzu.Path;
import juzu.View;
 import juzu.template.Template;

 import javax.inject.Inject;
 import javax.portlet.PortletPreferences;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
 
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
   public void index() throws IOException
    {

       String size = portletPreferences.getValue("size", "128");
        
      Map<String, Object> parameters = new HashMap<String, Object>();
      parameters.put("size", size);
      indexTemplate.render(parameters);
    }
 
 }
