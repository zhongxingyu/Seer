 package com.nexus.webserver;
 
 import java.io.IOException;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.HashMap;
 import java.util.Locale;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.TimeZone;
 import java.util.logging.Level;
 
 import com.nexus.NexusVersion;
 import com.nexus.event.Event;
 import com.nexus.json.JSONPacket;
 import com.nexus.logging.NexusLog;
 
 public class WebServerResponse{
 	
 	private final Map<String, String> Headers = new HashMap<String, String>();
 	private final WebServerSession Session;
 	private final WebServerClientThread Client;
 	
 	private StringBuffer HeaderData = new StringBuffer();
 	private boolean HeadersReadyToSend = false;
 	private boolean HeadersSent = false;
 	private boolean ContinueSent = false;
 	
	private boolean ResponseClosed = false;
	
 	public WebServerResponse(WebServerClientThread client){
 		this.Client = client;
 		this.Session = client.GetSession();
 	}
 	
 	public void SetHeader(String Name, String Value){
 		this.Headers.put(Name, Value);
 	}
 	
 	public Map<String, String> GetHeaders(){
 		return this.Headers;
 	}
 	
 	public void SendHeaders(int Status){
 		this.SendHeaders(WebServerStatus.GetByCode(Status));
 	}
 	
 	public void SendHeaders(WebServerStatus Status){
 		if(!Status.IsCompatible(this.Client.GetVersion())){
 			NexusLog.log("WebServer", Level.WARNING, "Sending incompatible http header (%s) to %s! Please avoid things like this", Status.GetCode(), this.Client.ReadClientIP());
 		}
 		if(Status == WebServerStatus.Continue){
 			if(!this.ContinueSent){
 				this.ContinueSent = true;
 				this.HeaderData = new StringBuffer();
 				this.HeaderData.append("HTTP/1.1 ");
 				this.HeaderData.append(Status.GetHeader());
 				this.HeaderData.append("\r\n");
 				try{
 					Session.write(this.HeaderData.toString());
 					this.HeaderData = new StringBuffer();
 				}catch(Exception e){
 				}
 			}
 		}
 		Locale locale = Locale.US;
 		Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"), locale);
 		SimpleDateFormat format = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", locale);
 		format.setTimeZone(TimeZone.getTimeZone("GMT"));
 		String GMTTime = format.format(calendar.getTime());
 		
 		this.SetHeader("Server", "Nexus/" + NexusVersion.getVersion());
 		this.SetHeader("Date", GMTTime);
 		this.SetHeader("Connection", "close");
 		
 		this.HeaderData = new StringBuffer();
 		this.HeaderData.append("HTTP/1.1 ");
 		this.HeaderData.append(Status.GetHeader());
 		this.HeaderData.append("\r\n");
 		
 		for(Entry<String, String> e : this.Headers.entrySet()){
 			this.HeaderData.append(e.getKey() + ": " + e.getValue() + "\r\n");
 		}
 		
 		this.HeadersReadyToSend = true;
 	}
 	
 	private void SendHeadersToSession(){
 		try{
 			this.HeaderData.append("\r\n");
 			this.Session.write(this.HeaderData.toString());
 			this.HeadersSent = true;
 		}catch(IOException e){
 		}
 	}
 	
 	public void ForceSendHeaders(){
 		if(!this.HeadersReadyToSend){
 			throw new IllegalStateException("WebServerStatus and headers are not set!");
 		}
 		this.SendHeadersToSession();
 	}
 	
 	public void ForceSendHeaders(WebServerStatus Status){
 		if(!this.HeadersReadyToSend){
 			this.SendHeaders(Status);
 		}
 		this.SendHeadersToSession();
 	}
 	
 	public void SendData(IWebServerResponse p){
 		if(!this.HeadersReadyToSend) throw new IllegalStateException("WebServerStatus and headers are not set!");
 		
 		String data = p.toString();
 		int length = p.toString().length();
 		if(length == 0){
 			this.SetHeader("Content-Length", "0");
 			this.ForceSendHeaders(WebServerStatus.NoContent);
 			this.Close();
 			return;
 		}
 
 		this.HeaderData.append("Content-Length: " + Integer.toString(length));
 		this.HeaderData.append("\r\n");
 		this.HeaderData.append("Content-Type: " + p.getMimeType());
 		this.HeaderData.append("\r\n");
 		
 		if(!this.HeadersSent) this.ForceSendHeaders();
 		if(this.Client.GetMethod() == WebServerMethod.HEAD) return;
 		try{
 			Session.write(data);
 		}catch(Exception e){
 		}
 	}
 	
 	public void SendError(String s){
 		JSONPacket p = new JSONPacket();
 		p.addErrorPayload(s);
 		this.SendData(p);
 	}
 	
 	public void SendEventResult(Event e){
 		JSONPacket p = new JSONPacket();
 		p.addEventResult(e);
 		this.SendData(p);
 	}
 	
 	public WebServerSession GetSession(){
 		if(!this.HeadersSent) this.ForceSendHeaders();
 		return this.Session;
 	}
 	
 	public void Close(){
 		this.Session.reset();
 		this.Session.Close();
		this.ResponseClosed = true;
	}
	
	public boolean isOpen(){
		return !this.ResponseClosed;
 	}
 }
