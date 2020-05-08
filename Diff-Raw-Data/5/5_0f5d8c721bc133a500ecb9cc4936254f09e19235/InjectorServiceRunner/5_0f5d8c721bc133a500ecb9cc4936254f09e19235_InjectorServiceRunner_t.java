 
 package axirassa.services.runners;
 
 import org.hibernate.Session;
 import org.hornetq.api.core.client.ClientSession;
 
import axirassa.services.InjectorService;
 import axirassa.services.Service;
 import axirassa.util.HibernateTools;
 import axirassa.util.MessagingTools;
 
 public class InjectorServiceRunner {
 	public static void main(String[] args) throws Exception {
 		Thread.sleep(10000);
 
 		ClientSession msgsession = MessagingTools.getEmbeddedSession();
 		Session dbsession = HibernateTools.getSession();
 
		Service service = new InjectorService(msgsession, dbsession);
 
 		System.out.println("Executing injector");
 		service.execute();
 		System.out.println("Finished executing");
 
 		return;
 	}
 }
