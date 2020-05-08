 package com.dalang.fs_cli;
 
 import android.os.Message;
 import android.util.Log;
 
 import java.util.List;
 import java.util.Iterator;
 import org.freeswitch.esl.client.IEslEventListener;
 import org.freeswitch.esl.client.inbound.Client;
 import org.freeswitch.esl.client.inbound.InboundConnectionFailure;
 import org.freeswitch.esl.client.transport.CommandResponse;
 import org.freeswitch.esl.client.transport.event.EslEvent;
 import org.freeswitch.esl.client.transport.message.EslMessage;
 import org.freeswitch.esl.client.transport.message.EslHeaders.Name;
 //import org.junit.Test;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 
 public class EventSocketManager {
 	FsCli parent;
 	private final Logger log = LoggerFactory.getLogger( this.getClass() );
     private String host;
     private int port;
     private String password;
     Client client;
 
 	EventSocketManager(FsCli fscli, FsSetting setting) {
 		parent = fscli;
 		host = setting.getServer();
 		password = setting.getPassword();
 		port = setting.getPort();
 	}
 	
 	public boolean changesetting(FsSetting setting) {
 		boolean status = false;
 		host = setting.getServer();
 		password = setting.getPassword();
 		port = setting.getPort();
 		
 		if(client != null) {
 			if(client.canSend())
 				client.close();			
 		}
 
 		try{
 			Thread.sleep(2000);
 		}
 		catch(InterruptedException e)
 		{	
 			Log.e("Sleep", "waiting for connection closed error");
 		}
 		
 		try{
 			status = do_connect();
 		}
 		catch (InterruptedException e)
 		{
 			Log.e("Connect failed", e.toString() );
 		}
 		
 		return status;
 	}
 	
 	public boolean reset(FsSetting setting) {
 		boolean status = false;		
 		host = "127.0.0.1";
 		password = "ClueCon";
 		port = 8021;
 		
 		if(client.canSend())
 			client.close();
 		
 		try{
 			Thread.sleep(2000);
 		}
 		catch(InterruptedException e)
 		{
 			
 		}
 		
 		try{
 			status = do_connect();
 		}
 		catch (InterruptedException e)
 		{
 			Log.e("Connect failed", e.toString() );
 		}
 		
 		return status;
 	}
 	
     public boolean do_connect() throws InterruptedException
     {
     	if(client == null)
     	{
     		client = new Client();
     		
 	        client.addEventListener( new IEslEventListener()
 	        {
 	            public void eventReceived( EslEvent event )
 	            {
 	                log.info( "Event received [{}]", event );
 	                StringBuilder sb=new StringBuilder();
	                for (Object o:event.getEventHeaders().keySet()){    
	                    sb.append(o.toString() + ": " + event.getEventHeaders().get(o) + "<br/>");	                    
 	                }	                
 	                parent.handle.sendMessage(Message.obtain(parent.handle, 1, sb.toString()));
 	            }
 	            public void backgroundJobResultReceived( EslEvent event )
 	            {
 	                log.info( "Background job result received [{}]", event );
 	                StringBuilder sb=new StringBuilder();
 	                for (Object o:event.getEventHeaders().keySet()){    
 	                    sb.append(o.toString() + ": " + event.getEventHeaders().get(o) + "<br/>");	                    
 	                }
 	                parent.handle.sendMessage(Message.obtain(parent.handle, 2, sb.toString()));
 	            }
 	
 	        } ); 
     	}
     	
     	if(!client.canSend())
     	{
     		log.info( "Client conn" +
     				"{ecting .." );
     		try
     		{   			
     				client.connect( host, port, password, 2 );
     		}
     		catch ( InboundConnectionFailure e )
     		{
     			log.error( "Connect failed", e );
     			return false;
     		}
     		log.info( "Client connected .." );
     	}
     	
     	//client.setEventSubscriptions( "plain", "heartbeat" );
         return true;
         /*
 //      client.setEventSubscriptions( "plain", "heartbeat CHANNEL_CREATE CHANNEL_DESTROY BACKGROUND_JOB" );
         client.setEventSubscriptions( "plain", "all" );
         client.addEventFilter( "Event-Name", "heartbeat" );
         client.cancelEventSubscriptions();
         client.setEventSubscriptions( "plain", "all" );
         client.addEventFilter( "Event-Name", "heartbeat" );
         client.addEventFilter( "Event-Name", "channel_create" );
         client.addEventFilter( "Event-Name", "background_job" );
         client.sendSyncApiCommand( "echo", "Foo foo bar" );
 
 //        client.sendSyncCommand( "originate", "sofia/internal/101@192.168.100.201! sofia/internal/102@192.168.100.201!" );
         
 //        client.sendSyncApiCommand( "sofia status", "" );
         
         String jobId = client.sendAsyncApiCommand( "status", "" );
         log.info( "Job id [{}] for [status]", jobId );
         client.sendSyncApiCommand( "version", "" );
 //        client.sendAsyncApiCommand( "status", "" );
 //        client.sendSyncApiCommand( "sofia status", "" );
 //        client.sendAsyncApiCommand( "status", "" );
         EslMessage response = client.sendSyncApiCommand( "sofia status", "" );
         log.info( "sofia status = [{}]", response.getBodyLines().get( 3 ) );
         
         // wait to see the heartbeat events arrive
          Thread.sleep( 25000 );
         client.close();
          */
         
     }
     public void exit()
     {
 		CommandResponse cresponse;
 		String out = "";
 
 		if (client.canSend()) {
 			cresponse = client.close();
 			for (Iterator<String> i = cresponse.getResponse().getBodyLines()
 					.iterator(); i.hasNext();) {
 				String content = i.next();
 				out += content + "\n";
 			}
 			out +=  "Goodbye!\nSee you at ClueCon http://www.cluecon.com/\n";
 			parent.handle.sendMessage(Message.obtain(parent.handle, 0, out));
 		}
 
 		return;
     }
     public void sub_event(String format, String events)
     {
     	CommandResponse cresponse;
     	String out = "";
 		
 		if (client.canSend()) {
 				cresponse = client.setEventSubscriptions(format, events);
 	    		out += cresponse.getReplyText() + "\n";
 	    		parent.handle.sendMessage(Message.obtain(parent.handle,0,out));			
 		}
 		return;
     }
     public void no_events()
     {
     	CommandResponse cresponse;
     	String out = "";
 
     	if (client.canSend()) {
         	cresponse = client.cancelEventSubscriptions();
     		out += cresponse.getReplyText() + "\n";
     		parent.handle.sendMessage(Message.obtain(parent.handle,0,out));
     	}
     	return;
     }
     public void log(String loglevel)
     {
     	CommandResponse cresponse;
     	String out = "";
 
     	if (client.canSend()) {
         	cresponse = client.setLoggingLevel(loglevel);
     		out += cresponse.getReplyText() + "\n";
     		parent.handle.sendMessage(Message.obtain(parent.handle,0,out));
     	}
     	return;  	
     }
     public void no_log()
     {
     	CommandResponse cresponse;
     	String out = "";
 
     	if (client.canSend()) {
         	cresponse = client.cancelLogging();
         	out += cresponse.getReplyText() + "\n";
     		parent.handle.sendMessage(Message.obtain(parent.handle,0,out));
     	}
     	return;  	
     }
     public void send_apicmd(String cmd)
     {
     	EslMessage response;
     	String out = "";
 
     	if(client.canSend())
     	{
     		log.info( "Api command = [{}]", cmd );
     		response = client.sendSyncApiCommand( cmd, "" );
     		for(Iterator <String> i = response.getBodyLines().iterator(); i.hasNext();)  
     		{
     			String content = i.next();
     			out += content + "\n";
     		} 
     		parent.handle.sendMessage(Message.obtain(parent.handle,0,out));
     	}
     	return ;
     }
     public void send_bgapicmd(String cmd)
     {
         String jobId = client.sendAsyncApiCommand( cmd, "" );
         log.info( "Job id [{}] for [{}]", jobId, cmd );    	
     }
 }
