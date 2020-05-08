 /*
  * Copyright 2013 The MITRE Corporation, All Rights Reserved.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this work except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *    http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  *
  */
 
 /**
  * Single client test proxy for the SVMP protocol
  */
 package org.mitre.svmp.TestProxy;
 
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.net.UnknownHostException;
 import java.security.KeyStore;
 import java.security.SecureRandom;
 
 import javax.net.ServerSocketFactory;
 import javax.net.ssl.KeyManagerFactory;
 import javax.net.ssl.SSLContext;
 
 import org.mitre.svmp.protocol.SVMPProtocol;
 import org.mitre.svmp.protocol.SVMPProtocol.Request;
 import org.mitre.svmp.protocol.SVMPProtocol.Response;
 import org.mitre.svmp.protocol.SVMPProtocol.Response.ResponseType;
 import org.mitre.svmp.protocol.SVMPProtocol.ScreenInfo;
 
 import com.google.protobuf.ByteString;
 import com.google.protobuf.TextFormat;
 
 /**
  * @author David Keppler <dkeppler@mitre.org>
  *
  */
 public class TestProxy {
 
 	// Set this to the IP address of the SVMP VM.
 	public static String VM_ADDRESS = "192.168.42.100";
 
 	// To use SSL:
 	// 1) use keytool to generate a self signed cert in a new keystore
 	// 2) set the keystore file name and password here and set USE_SSL to true
 	private static final boolean USE_SSL = true;
 	private static final String KEYSTORE_FILE = "test.keystore.jks";
 	private static final String KEYSTORE_PASS = "changeme";
 
 	public static int INPUT_SERVICE_PORT = 8001;
 
 	public static final int LISTEN_PORT = 8002;
 
 	private static final int UNAUTHENTICATED = 0;
 	private static final int AUTHENTICATED = 1;
 	private static final int PROXYING = 2;
 
 	private Socket inputService = null;
 	private InputStream  inputServiceIn = null;
 	private OutputStream inputServiceOut = null;
 	private Thread inputServiceThread;
 
 	private int state = UNAUTHENTICATED;
 	
 	Socket session;
 	
 	public TestProxy(Socket s) {
 		session = s;
 	}
 	
 	/**
 	 * @param args
 	 * @throws IOException 
 	 * @throws InterruptedException 
 	 */
 	public static void main(String[] args) throws Exception {
 		ServerSocket daemon = getServerSocket().createServerSocket(LISTEN_PORT);
 		System.out.println("Listen socket opened on port " + LISTEN_PORT);
 
 		while (true) {
 			TestProxy session = null;
 			try {
 				Socket s = daemon.accept();
 				System.out.println("Client connection received from " + s.getInetAddress());
 				session = new TestProxy(s);
 				session.run();
 			} catch (Exception e) {
 				e.printStackTrace();
 				if (session != null)
 					session.cleanup();
 			}
 		}
 	}
 
 	private static ServerSocketFactory getServerSocket() throws Exception {
 		if (USE_SSL) {
 			FileInputStream keyFile = new FileInputStream(KEYSTORE_FILE);
 			char[] keyPass = KEYSTORE_PASS.toCharArray();
 			KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
 			ks.load(keyFile, keyPass);
 
 			KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
 			kmf.init(ks, keyPass);
 
 			// server authentication only, no client auth here so no TrustManager needed
 			SSLContext sslcontext = SSLContext.getInstance("TLS");
 			sslcontext.init(kmf.getKeyManagers(), null, new SecureRandom());
 
 			return sslcontext.getServerSocketFactory();
 		} else {
 			return ServerSocketFactory.getDefault();
 		}
 	}
 
 	private void run() throws IOException, InterruptedException {
 		InputStream in = session.getInputStream();
 		OutputStream out = session.getOutputStream();
		
 		System.out.println("Starting listen loop");
		
		while (!session.isClosed()) {
			Request req = Request.parseDelimitedFrom(in);
 			//System.out.println("Request received: " + req.getType().name());
 			Response.Builder response = SVMPProtocol.Response.newBuilder();
 			switch (state) {
 			case UNAUTHENTICATED:
 				if (doAuthentication(req)) {
 					response.setType(ResponseType.AUTHOK);
 					synchronized(out) {
 						response.build().writeDelimitedTo(out);
 						System.out.println("AUTHOK sent");
 					}
 					connectToVM(out);
 					response.clear();
 					System.out.println("Waiting for \"VM to start\"");
 					Thread.sleep(2000); // pretend we have to wait a bit for the VM to connect
 					response.setType(ResponseType.VMREADY);
 					response.setMessage(VM_ADDRESS);
 					state = PROXYING;
 					synchronized(out) {
 						response.build().writeDelimitedTo(out);
 						System.out.println("VMREADY sent");
 					}
 				} else {
 					response.setType(ResponseType.ERROR);
 					response.setMessage("Authentication failed.");
 					synchronized(out) {
 						response.build().writeDelimitedTo(out);
 						System.out.println("Authentication ERROR sent");
 					}
 					// failed or unexpected message type
 				}
 				break;
 			case AUTHENTICATED:
 				break;
 			case PROXYING:
 /*
 // Debug printouts
 				switch(req.getType()) {
 				case SCREENINFO:
 					response.clear();
 		        	ScreenInfo.Builder scr = SVMPProtocol.ScreenInfo.newBuilder();
 		        	scr.setX(360);
 		        	scr.setY(480);
 		        	response.setType(ResponseType.SCREENINFO);
 		        	response.setScreenInfo(scr);
 		        	response.build().writeDelimitedTo(out);
 		        	break;
 				case SENSOREVENT:
 					if (req.hasSensor()) {
 						System.out.println("Sensor type = " + req.getSensor().getType().name());
 						System.out.println("   Accuracy = " + req.getSensor().getAccuracy());
 						System.out.println("   Timestamp = " + req.getSensor().getTimestamp());
 						System.out.print("   Values = [");
 						for (float v : req.getSensor().getValuesList()) {
 							System.out.print(" "+v);
 						}
 						System.out.println("]");
 					}
 					break;
 				case TOUCHEVENT:
 					if (req.hasTouch()) {
 						System.out.println("Action = " + req.getTouch().getAction());
 						for (SVMPProtocol.TouchEvent.PointerCoords p : req.getTouch().getItemsList()) {
 							System.out.println("    id = " + p.getId() + " ; x = " + p.getX() + " ; y = " + p.getY());
 						}
 					}
 					break;
 				case INTENT:
 					break;
 				case LOCATION:
 					break;
 				}
 				System.out.println("Request forwarded to input server");
 // End debug printouts
 */
 
 				req.writeDelimitedTo(inputServiceOut);
 				break;	// PROXYING state
 			}
 		}
 	}
	
 	private boolean doAuthentication(SVMPProtocol.Request r) {
 		if (r.hasAuthentication()) {
 			System.out.println("Got username = '" + r.getAuthentication().getUn() +
 					"' ; password = '" + r.getAuthentication().getPw() + "'");
 			return true;
 		} else
 			return false;
 	}
 
 	private void connectToVM(OutputStream client) throws UnknownHostException, IOException {
 		System.out.println("Connecting to Input service daemon");
 		inputService = new Socket(VM_ADDRESS, INPUT_SERVICE_PORT);
 		inputServiceOut = inputService.getOutputStream();
 		inputServiceIn  = inputService.getInputStream();
 		inputServiceThread = new InputServiceResponseHandler(client, inputServiceIn);
 		System.out.println("Input service daemon connected. Starting listen thread.");
 		inputServiceThread.start();
 	}
 
 	public void cleanup() throws IOException {
 		inputServiceThread.stop();
 		//intentServiceThread.stop();
 		inputService.close();
 		//intentService.close();
 	}
 		
 	private class InputServiceResponseHandler extends Thread {
 		private OutputStream toClient;
 		private InputStream fromService;
 
 		private InputServiceResponseHandler(OutputStream client, InputStream service) {
 			toClient = client;
 			fromService = service;
 		}
 		
 		@Override
 		public void run() {
 			try {
 				while (true) {
 					Response r = Response.parseDelimitedFrom(fromService);
 					System.out.println("Sending response back to client: " + r.getType().name());
 					synchronized (toClient) {
 						r.writeDelimitedTo(toClient);
 					}
 				}
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 	}
 }
