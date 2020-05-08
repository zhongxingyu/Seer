 /* Copyright (c) 2006-2007 Jan S. Rellermeyer
  * Information and Communication Systems Research Group (IKS),
  * Department of Computer Science, ETH Zurich.
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met:
  *    - Redistributions of source code must retain the above copyright notice,
  *      this list of conditions and the following disclaimer.
  *    - Redistributions in binary form must reproduce the above copyright
  *      notice, this list of conditions and the following disclaimer in the
  *      documentation and/or other materials provided with the distribution.
  *    - Neither the name of ETH Zurich nor the names of its contributors may be
  *      used to endorse or promote products derived from this software without
  *      specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
  * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
  * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
  * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
  * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
  * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
  * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
  * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
  * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
  * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
  * POSSIBILITY OF SUCH DAMAGE.
  */
 package ch.ethz.iks.r_osgi.http.acceptor;
 
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.net.Socket;
 import java.util.HashMap;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import ch.ethz.iks.r_osgi.RemoteOSGiMessage;
 
 /**
  * 
  * @author Jan S. Rellermeyer, ETH Zurich
  */
 public class HttpAcceptorServlet extends HttpServlet {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 
 	private static final int R_OSGi_PORT = 9278;
 
 	private static Socket socket;
 
 	private static HashMap bridges = new HashMap();
 
 	protected void service(HttpServletRequest req, HttpServletResponse resp)
 			throws ServletException, IOException {
 		//System.out.println();
 		//System.out.println("Servlet called");
 		super.service(req, resp);
 	}
 
 	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
 			throws ServletException, IOException {
 		// System.out.println("GOT GET REQUEST");
 		// Writer writer = resp.getWriter();
 		// writer.write("<h1>R-OSGi HTTP Channel Acceptor Servlet</h1>");
 		// resp.setStatus(HttpServletResponse.SC_OK);
 		doPost(req, resp);
 	}
 
 	/**
 	 * 
 	 * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest,
 	 *      javax.servlet.http.HttpServletResponse)
 	 */
 	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
 			throws ServletException, IOException {
 		final String host = req.getProtocol() + req.getRemoteAddr() + req.getHeader("channel");		
 		System.out.println("getting " + host);
 
 		ChannelBridge bridge = (ChannelBridge) bridges.get(host);
 		if (bridge == null) {
 			bridge = new ChannelBridge();
 			bridges.put(host, bridge);
 		}
 
 		bridge.forwardRequest(req, resp);
 	}
 
 	private static class ChannelBridge {
 		private final ObjectInputStream localIn;
 
 		private final ObjectOutputStream localOut;
 
 		private final HashMap waitMap = new HashMap();
 
 		private static final Object WAITING = new Object();
 
		private boolean firstLease = true;
		
 		private ChannelBridge() throws IOException {
 			socket = new Socket("localhost", R_OSGi_PORT);
 			localIn = new ObjectInputStream(socket.getInputStream());
 			localOut = new ObjectOutputStream(socket.getOutputStream());
 			localOut.flush();
 		}
 
 		private void forwardRequest(HttpServletRequest req,
 				HttpServletResponse resp) throws IOException {
 			final ObjectInputStream remoteIn = new ObjectInputStream(req
 					.getInputStream());
 
 			final RemoteOSGiMessage msg = RemoteOSGiMessage.parse(remoteIn);
 			System.out.println("{REMOTE -> LOCAL}: " + msg);
 
 			final Integer xid = new Integer(msg.getXID());
 			
 			synchronized (waitMap) {
 				waitMap.put(xid, WAITING);
 			}
 			msg.send(localOut);
 			localOut.flush();
 
 			if (msg.getFuncID() == RemoteOSGiMessage.LEASE && firstLease) {
 				ObjectOutputStream baseOut = new ObjectOutputStream(
 						new ChunkedEncoderOutputStream(resp.getOutputStream()));
 				baseOut = new ObjectOutputStream(
 						new ChunkedEncoderOutputStream(resp.getOutputStream()));
 				resp.setHeader("Transfer-Encoding", "chunked");
 				resp.setContentType("multipart/x-r_osgi");
 				firstLease = false;
 
 				// intentionally, the request that carried the lease does not
 				// terminate (as long as the connection is open). It is used to
 				// ship remote events.
 				try {
 					while (!Thread.interrupted()) {
 						RemoteOSGiMessage response = RemoteOSGiMessage
 								.parse(localIn);
 						System.out.println("{Servlet Bridge} received "
 								+ response);
 						switch (response.getFuncID()) {
 						case RemoteOSGiMessage.LEASE:
 							try {
 
 								response.rewrite(req.isSecure() ? "https"
 										: "http", req.getServerName(), req
 										.getServerPort());
 							} catch (IllegalArgumentException e) {
 								e.printStackTrace();
 							}
 						case RemoteOSGiMessage.REMOTE_EVENT:
 							System.out.println("{LOCAL -> REMOTE (ASYNC)}: "
 									+ response);
 
 							// deliver remote event as response of the lease
 							// request
 							response.send(baseOut);
 							resp.flushBuffer();
 							continue;
 						default:
 							// put into wait queue
 							synchronized (waitMap) {
 								waitMap.put(new Integer(response.getXID()),
 										response);
 								waitMap.notifyAll();
 							}
 						}
 					}
 				} catch (IOException ioe) {
 					ioe.printStackTrace();
 				}
 			} else {
 				Object response = null;
 
 				try {
 					synchronized (waitMap) {
 						while (waitMap.get(xid) == WAITING) {
 							waitMap.wait();
 						}
 						response = waitMap.remove(xid);
 					}
 				} catch (InterruptedException e) {
 					e.printStackTrace();
 				}
 
 				resp.setContentType("application/x-r_osgi");
 				ObjectOutputStream remoteOut = new ObjectOutputStream(resp
 						.getOutputStream());
 
 				System.out.println("{LOCAL -> REMOTE}: " + msg);
 				((RemoteOSGiMessage) response).send(remoteOut);
 			}
 		}
 	}
 
 }
