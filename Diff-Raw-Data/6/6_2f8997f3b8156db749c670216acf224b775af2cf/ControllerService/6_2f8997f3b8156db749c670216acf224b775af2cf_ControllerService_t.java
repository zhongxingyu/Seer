 
 package axirassa.services;
 
 import java.util.List;
 
 import org.apache.tapestry5.ioc.annotations.Inject;
 import org.hibernate.Query;
 import org.hibernate.Session;
 import org.hornetq.api.core.client.ClientMessage;
 import org.hornetq.api.core.client.ClientProducer;
 
 import axirassa.config.Messaging;
 import axirassa.model.PingerEntity;
import axirassa.webapp.services.MessagingSession;
 
 /**
  * The ControllerService executes every minute, creating a message for each
  * triggered pinger.
  * 
  * @author wiktor
  */
 public class ControllerService implements Service {
 
 	@Inject
	private MessagingSession messaging;
 
 	@Inject
 	private Session database;
 
 
 	@Override
 	public void execute () throws Exception {
 		ClientProducer producer = messaging.createProducer(Messaging.PINGER_REQUEST_QUEUE);
 
 		Query query = database.getNamedQuery("pingers_by_frequency");
 		query.setInteger("frequency", 3600);
 
 		List<PingerEntity> pingers = query.list();
 		for (PingerEntity pinger : pingers) {
 			ClientMessage message = messaging.createMessage(false);
 			message.getBodyBuffer().writeBytes(pinger.toBytes());
 			producer.send(message);
 		}
 
 		System.out.println("Populated " + pingers.size() + " pingers");
 
 		producer.close();
 	}
 }
