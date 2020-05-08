 package com.nexus.webserver.handlers;
 
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import com.nexus.NexusServer;
 import com.nexus.client.NexusClient;
 import com.nexus.logging.NexusLog;
 import com.nexus.network.ProtocolType;
 import com.nexus.network.exception.InvalidPacketException;
 import com.nexus.network.exception.InvalidPacketImplementationException;
 import com.nexus.network.exception.UnauthorizedException;
 import com.nexus.network.packets.Packet;
 import com.nexus.utils.JSONPacket;
 import com.nexus.utils.Utils;
 import com.nexus.webserver.IWebServerHandler;
 import com.nexus.webserver.WebServerMethod;
 import com.nexus.webserver.WebServerRequest;
 import com.nexus.webserver.WebServerResponse;
 import com.nexus.webserver.WebServerStatus;
 
 public class WebServerHandlerPostPacket implements IWebServerHandler{
 	
 	public static final Logger log = NexusLog.MakeLogger("PacketReceiver");
 	
 	@Override
 	public void Handle(WebServerRequest Request, WebServerResponse Response, String... Data){
 		if(Request.Method != WebServerMethod.POST){
 			Response.SendHeaders(WebServerStatus.MethodNotAllowed);
 			Response.SendError("PostPacket must use an POST request!");
 			Response.Close();
 			return;
 		}
 		try{
 			String Token = Request.GetParameter("token");
 			if(!NexusServer.Instance.AuthenticationManager.isTokenValid(Token, Request.Address)){
 				throw new UnauthorizedException("Expired session token");
 			}
 			NexusClient SenderClient = NexusServer.Instance.ClientManager.GetClientFromToken(Token);
 			if(SenderClient == null){
 				throw new UnauthorizedException("Invalid session token");
 			}
 
 			String IncomingJson = Utils.URLDecode(Request.GetPostObject("data"));
 			JSONPacket Json = Utils.Gson.fromJson(IncomingJson, JSONPacket.class);
			
			Packet.HandlePacket(Json, SenderClient, ProtocolType.FETCH);
 			
 			JSONPacket p = new JSONPacket();
 			p.addErrorPayload("none");
 			Response.SendHeaders(WebServerStatus.OK);
 			Response.SendData(p);
 			Response.Close();
 		}catch(InvalidPacketImplementationException e){
 			Response.SendHeaders(WebServerStatus.InternalServerError);
 			Response.SendError("Internal server error");
 			Response.Close();
 			new InvalidPacketImplementationException("Something is wrong with your Packet implementation!", e).printStackTrace();
 		}catch(InvalidPacketException e){
 			Response.SendHeaders(WebServerStatus.BadRequest);
 			Response.SendError(e.getMessage());
 			Response.Close();
 		}catch(Exception e){
 			Response.SendHeaders(WebServerStatus.InternalServerError);
 			Response.SendError("Internal server error");
 			Response.Close();
 			NexusLog.log(Level.SEVERE, e, "Exception while reading packet");
 		}
 	}
 }
