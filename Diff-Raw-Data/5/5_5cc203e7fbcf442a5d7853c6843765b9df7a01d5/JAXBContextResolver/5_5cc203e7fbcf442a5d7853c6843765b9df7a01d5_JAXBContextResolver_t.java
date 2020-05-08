 package nl.proteon.liferay.surfnet.security.opensocial;
 
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 
 import javax.ws.rs.ext.ContextResolver;
 import javax.ws.rs.ext.Provider;
 import javax.xml.bind.JAXBContext;
 
import nl.proteon.liferay.surfnet.security.opensocial.model.OpenSocialGroup;
 
 import com.sun.jersey.api.json.JSONJAXBContext;
 
 @SuppressWarnings( "deprecation" )
 
 @Provider
 public final class JAXBContextResolver implements ContextResolver<JAXBContext> {
 
     private final JAXBContext context;
 
     private final Set<Class> types;
 
    private final Class[] cTypes = {OpenSocialGroup.class};
 
     public JAXBContextResolver() throws Exception {
         Map<String, Object> props = new HashMap<String, Object>();
         props.put(JSONJAXBContext.JSON_NOTATION, JSONJAXBContext.JSONNotation.MAPPED);
         props.put(JSONJAXBContext.JSON_ROOT_UNWRAPPING, Boolean.TRUE);
         props.put(JSONJAXBContext.JSON_NON_STRINGS, new HashSet<String>(1){{add("number");}});
         this.types = new HashSet<Class>(Arrays.asList(cTypes));
         this.context = new JSONJAXBContext(cTypes, props);
     }
 
     public JAXBContext getContext(Class<?> objectType) {
         return (types.contains(objectType)) ? context : null;
     }
 }
