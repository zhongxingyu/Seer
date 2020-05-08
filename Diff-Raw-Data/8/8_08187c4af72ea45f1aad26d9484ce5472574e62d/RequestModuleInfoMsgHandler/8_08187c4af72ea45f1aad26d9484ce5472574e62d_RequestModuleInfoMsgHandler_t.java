 package edu.teco.dnd.module.messages.infoReq;
 
 import java.util.UUID;
 
 import edu.teco.dnd.module.config.ConfigReader;
 import edu.teco.dnd.network.ConnectionManager;
 import edu.teco.dnd.network.MessageHandler;
 import edu.teco.dnd.network.messages.Response;
 
 public class RequestModuleInfoMsgHandler implements MessageHandler<RequestModuleInfoMessage> {
 	private final ConfigReader conf;
 
 	public RequestModuleInfoMsgHandler(ConfigReader conf) {
 		this.conf = conf;
 	}
 
 	@Override
 	public Response handleMessage(ConnectionManager connectionManager, UUID remoteUUID, RequestModuleInfoMessage message) {
 		return new ModuleInfoMessage(conf);
 	}
 
 }
