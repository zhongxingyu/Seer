 package com.nexus.webserver;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.nio.ByteBuffer;
 import java.nio.channels.FileChannel;
 import java.nio.file.Files;
 
 public class WebServerHandlerHtml extends WebServerHandler {
 	private WebServerRequest request;
 	private WebServerResponse response = new WebServerResponse();
 	private OutputStream socket;
 	
 	public WebServerHandlerHtml(WebServerRequest request, OutputStream socket) {
 		super(request, socket);
 		this.request = request;
 		this.socket = socket;
 	}
 	
 	public void handle() {
 		WebServerResponse response = new WebServerResponse();
 		
 		try {
 			
			String path = "htdocs" + request.path();
			if (path.endsWith("/"))
				path += "index.html";
 			
 			FileChannel file;
 			file = new FileInputStream(path).getChannel();
 			
 			ByteBuffer buffer = ByteBuffer.allocate((int) file.size());
 			
 			file.read(buffer);
 			
 			char[] data = new char[(int) file.size()];
 			
 			buffer.rewind();
 			
 			while (buffer.hasRemaining()) {
 				data[buffer.position()] = (char) buffer.get();
 			}
 			
 			this.response.setHeader("Content-Type", Files.probeContentType(new File(path).toPath()));
 			this.response.setHeader("Content-Length", Long.toString(file.size()));
 
 			this.sendHeaders(WebServerStatus.OK);
 			
 			for (char c : data) {
 				this.socket.write((byte) c);
 			}
 			
 			this.close();
 			
 		} catch (FileNotFoundException e) {
 			this.sendHeaders(WebServerStatus.NotFound);
 		} catch (IOException e) {
 			this.sendHeaders(WebServerStatus.InternalServerError);
 		}
 	}
 }
