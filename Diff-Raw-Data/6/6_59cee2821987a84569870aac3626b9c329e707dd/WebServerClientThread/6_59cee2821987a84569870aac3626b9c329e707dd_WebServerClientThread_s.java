 package com.nexus.webserver;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.LinkedHashMap;
 import java.util.StringTokenizer;
 import java.util.logging.Level;
 
 import com.nexus.logging.NexusLog;
 import com.nexus.utils.MultipartObject;
 import com.nexus.utils.Utils;
 
 public class WebServerClientThread implements Runnable{
 	
 	private final WebServerSession Session;
 	
 	private WebServerMethod Method;
 	private String Request;
 	private String HTTPVersion;
 	private String RequestingIP;
 	
 	private String URLEncodingCharset;
 	
 	private ArrayList<MultipartObject> PostMultipart = new ArrayList<MultipartObject>();
 	private String PostBoundary;
 	private final StringBuilder PostData = new StringBuilder();
 	private PostRequestType PostType;
 	private final HashMap<String, String> PostParameters = new LinkedHashMap<String, String>();
 	
 	private final HashMap<String, String> Headers = new LinkedHashMap<String, String>();
 	private final HashMap<String, String> RequestParameters = new LinkedHashMap<String, String>();
 	
 	public static void LaunchNewThread(WebServerSession session){
 		Thread t = new Thread(new WebServerClientThread(session));
 		t.setDaemon(true);
 		t.setName("WebServer Client Worker");
 		t.start();
 	}
 	
 	public WebServerClientThread(WebServerSession session){
 		this.Session = session;
 	}
 	
 	@Override
 	public void run(){
 		try{
 			this.ReadClientIP();
 			this.ParseRequest();
 			
 			if(this.HTTPVersion.equalsIgnoreCase("HTTP/1.1") && !this.Headers.containsKey("Host")){
 				this.SendError(WebServerStatus.BadRequest, "A host header is required in HTTP 1.1!");
 				NexusLog.log("WebServer", Level.WARNING, "Received an invalid HTTP request from %s! Host header is required in HTTP 1.1, but was not provided", this.RequestingIP);
 				return;
 			}
 			
 			if(this.Method == WebServerMethod.POST){
 				this.ParsePOSTRequest();
 				if(this.PostMultipart.size() == 0 && this.PostType == PostRequestType.Multipart){
 					return;
 				}
 			}
 			
 			WebServerRequest Request = new WebServerRequest();
 			Request.Address = this.RequestingIP;
 			Request.Headers = this.Headers;
 			Request.Method = this.Method;
 			Request.Parameters = this.RequestParameters;
 			Request.Path = this.Request.split("\\?")[0];
 			Request.PostData = this.PostParameters;
 			Request.RawPostData = this.GetIncomingData();
 			Request.PostMultipart = this.PostMultipart;
 			
 			WebServerHandlerFactory.HandleRequest(Request, this);
 		}catch(Exception e){
 			this.SendError(WebServerStatus.InternalServerError, "Internal server error!");
 			NexusLog.log("WebServer", Level.SEVERE, e, "Error in WebServerClientThread!");
 		}
 	}
 	
 	private void ParsePOSTRequest(){
 		String[] lines = this.GetIncomingData().split("\r\n");
 		boolean FoundData = false;
 		boolean FirstRun = true;
 		for(String line : lines){
 			if(!FoundData){
 				if(line.isEmpty()){
 					FoundData = true;
 				}else{
 					continue;
 				}
 			}
 			if(FirstRun){
 				FirstRun = false;
 			}else{
 				this.PostData.append("\r\n");
 			}
 			this.PostData.append(line.trim());
 		}
 		
 		if(this.Headers.containsKey("Content-Type")){
 			String ContentType = this.Headers.get("Content-Type");
 			if(ContentType.contains("multipart/form-data")){
 				this.PostType = PostRequestType.Multipart;
 				this.PostBoundary = ContentType.split("boundary=", 2)[1];
 				
 				final ArrayList<MultipartObject> objects = new ArrayList<MultipartObject>();
 				String[] Parts = this.PostData.toString().split("--" + this.PostBoundary);
 				for(String s : Parts){
 					if(s.isEmpty()) continue;
 					MultipartObject o = this.ParseMultipart(s);
 					if(o.isEmpty()) continue;
 					objects.add(this.ParseMultipart(s));
 				}
 				if(this.PostData.length() != 0){
 					if(objects.size() > this.PostMultipart.size()){
 						this.PostMultipart.clear();
 						this.PostMultipart = objects;
 					}
 				}
 			}else if(ContentType.contains("application/x-www-form-urlencoded")){
 				this.PostType = PostRequestType.FormData;
 				this.URLEncodingCharset = ContentType.split("charset=", 2)[1];
 				
 				String PostDataArray[] = Utils.URLDecode(this.PostData.toString(), this.URLEncodingCharset).split("&");
 				for(String parameter : PostDataArray){
 					String[] Values = parameter.split("=", 2);
 					if(Values.length == 1) continue;
 					
 					this.PostParameters.put(Values[0].trim(), Values[1].trim());
 				}
 			}
 		}
 	}
 	
 	private MultipartObject ParseMultipart(String s){
 		MultipartObject mo = new MultipartObject();
 		
 		String[] lines = s.split("\r\n");
 		boolean ReadingHeaders = true;
 		boolean FirstRun = true;
 		boolean FirstEmptyLine = true;
 		for(String line : lines){
 			if(FirstEmptyLine){
 				FirstEmptyLine = false;
 				continue;
 			}
 			if(ReadingHeaders){
 				if(line.isEmpty()){
 					ReadingHeaders = false;
 				}else{
 					String[] data = line.split(":", 2);
 					if(data.length != 2) continue;
 					mo.Headers.put(data[0].trim(), data[1].trim());
 				}
 				continue;
 			}
 			if(FirstRun){
 				FirstRun = false;
 			}else{
 				mo.Content.append("\r\n");
 			}
 			mo.Content.append(line.trim());
 		}
 		
 		return mo;
 	}
 	
 	private void ParseRequest(){
 		StringTokenizer tokenizer = new StringTokenizer(this.GetIncomingData());
 		this.Method = WebServerMethod.valueOf(tokenizer.nextToken().toUpperCase());
 		this.Request = tokenizer.nextToken();
 		this.HTTPVersion = tokenizer.nextToken();
 		
 		this.ParseHTTPParameters();
 		
 		String[] lines = this.GetIncomingData().split("\r\n");
 		boolean IsFirst = true;
 		for(String line : lines){
 			if(IsFirst){
 				IsFirst = false;
 				continue;
 			}
 			if(line.isEmpty()) break;
 			
 			String[] data = line.split(":", 2);
 			this.Headers.put(data[0].trim(), data[1].trim());
 		}
 	}
 	
 	private void ParseHTTPParameters(){
 		String SplittedURL[] = this.Request.split("\\?", 2);
 		if(SplittedURL.length == 1) return;
 		String ParameterArray[] = SplittedURL[1].split("&");
 		for(String Parameter : ParameterArray){
 			String[] Values = Parameter.split("=", 2);
			this.RequestParameters.put(Values[0], Values[1]);
 		}
 	}
 	
 	private String GetIncomingData(){
 		return this.Session.readLines.toString();
 	}
 	
 	public String ReadClientIP(){
 		if(this.RequestingIP != null) return this.RequestingIP;
 		this.RequestingIP = this.Session.Channel.socket().getInetAddress().toString().split("/")[1];
 		return this.RequestingIP;
 	}
 	
 	private void SendError(WebServerStatus status, String error){
 		WebServerResponse Response = new WebServerResponse(this);
 		Response.SendHeaders(status);
 		Response.SendError(error);
 		Response.Close();
 	}
 	
 	public WebServerSession GetSession(){
 		return this.Session;
 	}
 
 	public WebServerMethod GetMethod(){
 		return this.Method;
 	}
 
 	public String GetVersion(){
 		return this.HTTPVersion;
 	}
 }
