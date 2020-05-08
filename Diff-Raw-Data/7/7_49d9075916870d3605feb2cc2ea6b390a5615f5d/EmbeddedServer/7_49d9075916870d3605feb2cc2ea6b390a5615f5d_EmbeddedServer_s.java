 package com.emo.utils;
 
 import com.caucho.resin.HttpEmbed;
 import com.caucho.resin.ResinEmbed;
 import com.caucho.resin.WebAppEmbed;
 
 public class EmbeddedServer {
 
 	protected ResinEmbed server = null;
 
 	public void startServer() {
 		if (server != null) {
 			throw new IllegalStateException("Server is already started");
 		}
 
 		server = new ResinEmbed();
 
 		HttpEmbed http = new HttpEmbed(8080);
 		server.addPort(http);
 
 		WebAppEmbed webApp = new WebAppEmbed("/", "src/main/webapp");
 		server.addWebApp(webApp);
 
 		server.start();
		server.join();
 	}
 
 	public void stopServer() {
 		server.stop();
 	}
 }
