 package osf.poc.springremote.resources;
 
 import java.util.List;
import javax.jws.WebService;
 import osf.poc.model.Property;
 
@WebService
 public interface IPropertiesHttpInvoker {
 
     public List<Property> getProperties();
     
 }
