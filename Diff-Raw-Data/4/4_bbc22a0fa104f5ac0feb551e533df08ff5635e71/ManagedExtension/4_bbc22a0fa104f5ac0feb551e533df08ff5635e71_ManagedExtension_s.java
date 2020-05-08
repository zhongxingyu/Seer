 package li.rudin.arduino.managed.cdi;
 
 import java.util.Set;
 
 import javax.enterprise.event.Observes;
 import javax.enterprise.inject.spi.AfterBeanDiscovery;
 import javax.enterprise.inject.spi.BeanManager;
 import javax.enterprise.inject.spi.Extension;
 
 import li.rudin.arduino.api.ethernet.Connection;
 import li.rudin.arduino.core.ethernet.ArduinoEthernetImpl;
 
 import org.scannotation.AnnotationDB;
 import org.scannotation.ClasspathUrlFinder;
 
 public class ManagedExtension implements Extension
 {
 	private final AnnotationDB db = new AnnotationDB();
 
 	void afterBeanDiscovery(@Observes AfterBeanDiscovery abd, BeanManager bm)
 	{
 		try
 		{
 			db.scanArchives( ClasspathUrlFinder.findClassPaths() );
 			
 			Set<String> index = db.getAnnotationIndex().get(Connection.class.getName());
 			
 			for (String s: index)
 			{
 				Class<?> type = Class.forName(s);
 				Connection connection = type.getAnnotation(Connection.class);
 				
 				if (connection != null)
 					abd.addBean(new ManagedDeviceBean(type, new ArduinoEthernetImpl(connection.host(), connection.port())));
 			}
 			
 		}
 		catch (Exception e)
 		{
 			abd.addDefinitionError(e);
 		}
 
 	}
 }
