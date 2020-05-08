 //	---------------------------------------------------------------------------
 //	jWebSocket - Copyright (c) 2010 jwebsocket.org
 //	---------------------------------------------------------------------------
 //	This program is free software; you can redistribute it and/or modify it
 //	under the terms of the GNU Lesser General Public License as published by the
 //	Free Software Foundation; either version 3 of the License, or (at your
 //	option) any later version.
 //	This program is distributed in the hope that it will be useful, but WITHOUT
 //	ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 //	FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
 //	more details.
 //	You should have received a copy of the GNU Lesser General Public License along
 //	with this program; if not, see <http://www.gnu.org/licenses/lgpl.html>.
 //	---------------------------------------------------------------------------
 package org.jwebsocket.console;
 
 import java.io.ByteArrayOutputStream;
 import java.io.OutputStream;
 import java.io.UnsupportedEncodingException;
 
 
 import org.jwebsocket.api.WebSocketPacket;
 import org.jwebsocket.api.WebSocketServerListener;
 import org.jwebsocket.kit.WebSocketServerEvent;
 import org.jwebsocket.logging.Logging;
 
 //import ecologylab.oodss.distributed.server.DoubleThreadedAIOServer;
 import ecologylab.oodss.distributed.server.ServerMessages;
 import ecologylab.oodss.messages.Ping;
 import ecologylab.oodss.messages.PingRequest;
 import ecologylab.serialization.ElementState.FORMAT;
 import ecologylab.serialization.SIMPLTranslationException;
 
 
 
 
 ///////////////////Comment out this from JWebSocketServer.java and replace it with....
 /*
 // get the token server
 TokenServer lTS0 = (TokenServer) JWebSocketFactory.getServer("ts0");
 if (lTS0 != null) {
 	// and add the sample listener to the server's listener chain
 	lTS0.addListener(new JWebSocketTokenListenerSample());
 }
 *//////////////////this down here.
 //TokenServer lTS0 = (TokenServer) JWebSocketFactory.getServer("ts0");
 //if (lTS0 != null) {
 //	lTS0.addListener(new MyListener());
 //}
 
 /**
  * This shows an example of a simple WebSocket listener
  * @author aschulze
  */
 public class JWebSocketListenerBridge implements WebSocketServerListener {
 
 	//put some object that has threaded events here....
 	//private static Logger log = Logging.getLogger(MyListener.class);
 	
 	public ServerMessages oodssServer = null;
 	//this will be a generic thing
 
 	/**
 	 *
 	 * @param aEvent
 	 */
 	@Override
 	public void processOpened(WebSocketServerEvent aEvent) {
 		System.out.println("NEWSTUFFTHATIWROTE:processOpened");
 	//	if (log.isDebugEnabled()) {
 	//		log.debug("Client '" + aEvent.getSessionId() + "' connected.");
 		//}
		//aEvent.getSessionId() .. need to add id's here.
		if(oodssServer != null)
		{
			oodssServer.newClientAdded(aEvent.getSessionId());
		}
		
 		System.out.println( "Client '" + aEvent.getSessionId() + "' connected.");
 	}
 
 	/**
 	 *
 	 * @param aEvent
 	 * @param aPacket
 	 */
 	@Override
 	public void processPacket(WebSocketServerEvent aEvent, WebSocketPacket aPacket) {
 		System.out.println("NEWSTUFFTHATIWROTE:processPacket");
 		//if (log.isDebugEnabled()) {
 		//	log.debug("Processing data packet '" + aPacket.getUTF8() + "'...");
 		//}
 		System.out.println( "Processing data packet '" + aPacket.getUTF8() + "'...");
 		//aPacket.
 		
 		
 		
 		PingRequest p = new PingRequest();
 		
 		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
 		try {
 			p.serialize(outStream, FORMAT.JSON);
 		} catch (SIMPLTranslationException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		String pJSON = new String(outStream.toByteArray());
 		System.out.println("pJSON:"+pJSON);
 		
 		String response = "";
 		if(oodssServer != null)
 		{
 			
 			
 			response = oodssServer.getAPushFromWebSocket(aPacket.getUTF8(),aEvent.getConnector().getSession().getSessionId());//a.getAPushFromWebSocket(pJSON);
 		}
 			//a.getAPushFromWebSocket(aPacket.getUTF8());
 		
 		
 		//aPacket.setUTF8("[echo from jWebSocket v" + JWebSocketServerConstants.VERSION_STR + "] " + aPacket.getUTF8());
 		
 		//aPacket.setUTF8(aPacket.getUTF8());
 		//aPacket.setUTF8(new String("<meta>This is meta!</meta>"));
 		//worked->aPacket.setUTF8(new String("{\"ns\":\"org.jWebSocket.plugins.system\",\"utid\":25,\"data\":\"LALAlaaaalaLAAL\",\"targetId\":\"*\",\"type\":\"send\",\"sourceId\":\"51262\"}"));		
 		aPacket.setUTF8(response);//new String("{\"data\":\"whatever\"}"));//SOON: replace with a get response message.. use translation scope.
 		System.out.println("THIS is the response that the server is about to send:"+response);
 		
 		
 		System.out.println("REALSTART:"+aPacket.getUTF8());
 		
 		//aPacket.
 		
 /*		StringBuilder lStrBuf = new StringBuilder();
 		for (int i = 0; i < 10000; i++) {
 			lStrBuf.append("winning");
 		}
 		aPacket.setUTF8(lStrBuf.toString());
 	*/	 
 		aEvent.sendPacket(aPacket);
 		
 	}
 
 	/**
 	 *
 	 * @param aEvent
 	 */
 	@Override
 	public void processClosed(WebSocketServerEvent aEvent) {
 		System.out.println("NEWSTUFFTHATIWROTE:aEvent");
 		//if (log.isDebugEnabled()) {
 			
 		//	log.debug("Client '" + aEvent.getSessionId() + "' disconnected.");
 	//	}
 		System.out.println("Client '" + aEvent.getSessionId() + "' disconnected.");
 	}
 }
