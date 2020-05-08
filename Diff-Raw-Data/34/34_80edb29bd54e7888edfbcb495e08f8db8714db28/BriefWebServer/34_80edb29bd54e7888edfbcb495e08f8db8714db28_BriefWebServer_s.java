 package org.denevell.briefwebserver;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.HttpURLConnection;
 import java.net.InetSocketAddress;
 import java.net.URL;
 
 import com.sun.net.httpserver.*;
 
 public class BriefWebServer {
 
 	private HttpServer mServer;
 
 	/**
 	 * @param hostname
 	 * @param port
 	 * @throws RuntimeException If we have a problem opening the http server
 	 */
 	public BriefWebServer(String hostname, int port) {
 		try {
 			mServer = HttpServer.create(new InetSocketAddress(hostname, port), 0);
 		} catch (IOException e) {
 			throw new RuntimeException(e);
 		}
 	}
 
	public void start() {
		mServer.start();
	}

 	public void stop() {
 		mServer.stop(0);
 	}
 
 	/**
 	 * Add some sub data that will be found at the path
 	 * @param path
 	 * @param stubData 
 	 */
 	public void addStub(final String path, final String stubData) {
 		mServer.createContext(path, new HttpHandler() {
 			@Override
 			public void handle(HttpExchange arg0) throws IOException {
 				if(!path.equals(arg0.getRequestURI().getPath())) {
 					throw new IOException("The request wasn't exactly the same as the registered path.");
 				}
 				arg0.sendResponseHeaders(HttpURLConnection.HTTP_OK, stubData.getBytes().length);
 				arg0.getResponseHeaders().add("Content-Type", "text/html");
 				arg0.getResponseBody().write(stubData.getBytes());
 				arg0.close();
 			}
 		});
 	}
 
 	/**
 	 * Utility method to skip all the url.openStream, BufferedReader.readLine(), etc rubbish
 	 * 
 	 * @param path Minus the first slash, i.e. "the/url/here"
 	 * @return The entire result of calling hostname:port/path
 	 * @throws IOException If there was a problem getting this data i.e couldn't connect, or bad path
 	 */
 	public String getStringFromContext(String path) throws IOException {
 		URL url = new URL("http://"+mServer.getAddress().getHostName()+":"+mServer.getAddress().getPort()+"/"+path);
 		BufferedReader result = new BufferedReader(new InputStreamReader(url.openStream()));
 		String stringResult = "";
 		String s = "";
 		while((s = result.readLine())!=null) {
 			stringResult += s + "\n";
 		}
 		return stringResult.substring(0, stringResult.length()-1); //get rid of the final \n
 	}
 }
