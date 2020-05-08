 package ch.zhaw.i11b.pwork.sem2.clTools;
 
 import java.util.Date;
 
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.UriBuilder;
 
 import ch.zhaw.i11b.pwork.sem2.beans.Message;
 import ch.zhaw.i11b.pwork.sem2.beans.config.JAXBContextResolver;
 
 import com.sun.jersey.api.client.Client;
 import com.sun.jersey.api.client.WebResource;
 import com.sun.jersey.api.client.config.ClientConfig;
 import com.sun.jersey.api.client.config.DefaultClientConfig;
 
 
 public class McConnection {
 
 	private WebResource r;
 	
 	static private McConnection _instance = null;
 	
 	static public McConnection instance() {
 		if (_instance == null) {
 			_instance = new McConnection();
 		}
 		return _instance;
 	}
 	
 	protected McConnection() { 
 		ClientConfig cc = new DefaultClientConfig();
 		// use the following jaxb context resolver
     	cc.getClasses().add(JAXBContextResolver.class);
     	Client c = Client.create(cc);
     	r = c.resource(UriBuilder.fromUri("http://localhost/").port(9998).build());
 	}
 	
 	protected boolean sendMess(Message m){
 		if(m.from.isEmpty()){
 			IO.mcNotify("Cannot send message, sender miising");
 			return false;
 		}
 		else if(m.message.isEmpty()){
 			IO.mcNotify("Cannot send message, content missing");
 			return false;
 		}
 		else if(m.targets.isEmpty()){
 			IO.mcNotify("Cannot send message, target missing");
 			return false;
 		}
 		else if(m.sendtime.before(new Date())){
 			IO.mcNotify("Cannot send message in the past");
 			return false;
 		}
 		else{
 			boolean succ;
 			try{
 				m = r.path("messages").accept(MediaType.APPLICATION_JSON).type(MediaType.APPLICATION_JSON).put(Message.class, m);
 				IO.mcNotify("Message sent, id = "+ m.id);
 				succ = true;
 			
 			} catch (Exception e){
 				IO.mcNotify("Message not sent");
 				succ = false;
 			}
 			return succ;
 			
 		}
 	}
 	
 }
