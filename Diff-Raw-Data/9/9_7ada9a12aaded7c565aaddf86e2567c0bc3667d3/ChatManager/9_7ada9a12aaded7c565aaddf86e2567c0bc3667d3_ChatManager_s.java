 package com.infrno.multiplayer;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import com.infrno.multiplayer.commands.ChatExpression;
 import com.infrno.multiplayer.commands.MessageEvaluator;
 import com.wowza.wms.amf.AMFDataList;
 import com.wowza.wms.client.IClient;
 
 public class ChatManager 
 {
 	private Application main_app;
 	
 	public ChatManager(Application app) 
 	{
 		main_app = app;
 		
 	}
 	
 	public void chatToServer( IClient client, AMFDataList msgObj )
 	{
 		String msgIn = msgObj.getString( 3 );
 		//4th param may one day contain targetd users to recieve the chat
 		
 		String user_name = main_app.userManager.getClientInfo(Integer.toString(client.getClientId())).getString("user_name");
 		
 		MessageEvaluator messageEvaluator = new MessageEvaluator( msgIn );
 		ChatExpression chatExpression = messageEvaluator.getChatExpression( );
 		
 		Map <String, String> context = new HashMap<String, String>();
 		context.put("user_name", user_name);
 		String interpretedMessage = chatExpression.interpret( context );
 		
 		main_app.log( "ChatManager.chatToServer() chat in: <"+ user_name +"> "+ msgIn );
 		main_app.log( "ChatManager.chatToServer() chat out: <"+ user_name +"> "+ interpretedMessage );
 		
		main_app.app_instance.broadcastMsg( "chatToUser", user_name + ": " + interpretedMessage );
 	}
 	
 }
