 /*
  * Licensed to the Apache Software Foundation (ASF) under one or more
  * contributor license agreements. See the NOTICE file distributed with this
  * work for additional information regarding copyright ownership. The ASF
  * licenses this file to You under the Apache License, Version 2.0 (the
  * "License"); you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  * http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
  * License for the specific language governing permissions and limitations under
  * the License.
  */
 
 package org.jboss.cluster.proxy.container;
 
 import java.net.DatagramPacket;
 import java.net.InetAddress;
 import java.net.InetSocketAddress;
 import java.net.MulticastSocket;
 import java.security.MessageDigest;
 import java.sql.Date;
 import java.util.Arrays;
 import java.util.Enumeration;
 import java.util.UUID;
 
 import org.apache.catalina.connector.Connector;
 import org.apache.coyote.ActionCode;
 import org.apache.coyote.Adapter;
 import org.apache.coyote.Request;
 import org.apache.coyote.Response;
 import org.apache.coyote.http11.Constants;
 import org.apache.tomcat.util.buf.ByteChunk;
 import org.apache.tomcat.util.buf.MessageBytes;
 import org.apache.tomcat.util.http.Parameters;
 import org.apache.tomcat.util.net.SocketStatus;
 import org.jboss.cluster.proxy.container.Context.Status;
 
 /**
  * Adapter. This represents the entry point in a coyote-based servlet container.
  * reads the MCM element
  * 
  * @author Jean-Frederic Clere
  * 
  */
 public class MCMPAdapter implements Adapter {
 
 	private static final String VERSION_PROTOCOL = "0.2.1";
 	private static final String TYPESYNTAX = "SYNTAX";
 	private static final String TYPEMEM = "MEM";
 
 	/* the syntax error messages */
 	private static final String SMESPAR = "SYNTAX: Can't parse message";
 	private static final String SBALBIG = "SYNTAX: Balancer field too big";
 	private static final String SBAFBIG = "SYNTAX: A field is too big";
 	private static final String SROUBIG = "SYNTAX: JVMRoute field too big";
 	private static final String SROUBAD = "SYNTAX: JVMRoute can't be empty";
 	private static final String SDOMBIG = "SYNTAX: LBGroup field too big";
 	private static final String SHOSBIG = "SYNTAX: Host field too big";
 	private static final String SPORBIG = "SYNTAX: Port field too big";
 	private static final String STYPBIG = "SYNTAX: Type field too big";
 	private static final String SALIBAD = "SYNTAX: Alias without Context";
 	private static final String SCONBAD = "SYNTAX: Context without Alias";
 	private static final String SBADFLD = "SYNTAX: Invalid field ";
 	private static final String SBADFLD1 = " in message";
 	private static final String SMISFLD = "SYNTAX: Mandatory field(s) missing in message";
 	private static final String SCMDUNS = "SYNTAX: Command is not supported";
 	private static final String SMULALB = "SYNTAX: Only one Alias in APP command";
 	private static final String SMULCTB = "SYNTAX: Only one Context in APP command";
 	private static final String SREADER = "SYNTAX: %s can't read POST data";
 
 	/* the mem error messages */
 	private static final String MNODEUI = "MEM: Can't update or insert node";
 	private static final String MNODERM = "MEM: Old node still exist";
 	private static final String MBALAUI = "MEM: Can't update or insert balancer";
 	private static final String MNODERD = "MEM: Can't read node";
 	private static final String MHOSTRD = "MEM: Can't read host alias";
 	private static final String MHOSTUI = "MEM: Can't update or insert host alias";
 	private static final String MCONTUI = "MEM: Can't update or insert context";
 
 	static final byte[] CRLF = "\r\n".getBytes();
 
 	private Connector connector;
 
 	protected Thread thread = null;
 	private String sgroup = "224.0.1.105";
 	private int sport = 23364;
 	private String slocal = "127.0.0.1";
 	private MessageDigest md = null;
 	private String chost = "127.0.0.1"; // System.getProperty("org.jboss.cluster.proxy.net.ADDRESS",
 										// "127.0.0.1");
 	private int cport = Integer.parseInt(System.getProperty("org.jboss.cluster.proxy.net.PORT",
 			"6666"));
 	private String scheme = "http";
 	private String securityKey = System
 			.getProperty("org.jboss.cluster.proxy.securityKey", "secret");
 
 	/**
 	 * Create a new instance of {@code MCMPaddapter}
 	 * 
 	 * @param connector
 	 */
 	public MCMPAdapter(Connector connector) {
 		this.connector = connector;
 		this.scheme = connector.getScheme();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.apache.coyote.Adapter#init()
 	 */
 	public void init() throws Exception {
 		System.out.println("init: " + connector);
 		if (md == null)
 			md = MessageDigest.getInstance("MD5");
 		if (thread == null) {
 			thread = new Thread(new MCMAdapterBackgroundProcessor(),
 					"MCMAdapaterBackgroundProcessor");
 			thread.setDaemon(true);
 			thread.start();
 
 		}
 	}
 
 	protected class MCMAdapterBackgroundProcessor implements Runnable {
 
 		/*
 		 * the messages to send are something like:
 		 * 
 		 * HTTP/1.0 200 OK
 		 * Date: Thu, 13 Sep 2012 09:24:02 GMT
 		 * Sequence: 5
 		 * Digest: ae8e7feb7cd85be346134657de3b0661
 		 * Server: b58743ba-fd84-11e1-bd12-ad866be2b4cc
 		 * X-Manager-Address: 127.0.0.1:6666
 		 * X-Manager-Url: /b58743ba-fd84-11e1-bd12-ad866be2b4cc
 		 * X-Manager-Protocol: http
 		 * X-Manager-Host: 10.33.144.3
 		 * non-Javadoc)
 		 */
 		@Override
 		public void run() {
 			try {
 				InetAddress group = InetAddress.getByName(sgroup);
 				InetAddress addr = InetAddress.getByName(slocal);
 				InetSocketAddress addrs = new InetSocketAddress(sport);
 
 				MulticastSocket s = new MulticastSocket(addrs);
 				s.setTimeToLive(29);
 				s.joinGroup(group);
 
 				int seq = 0;
 				/*
 				 * apr_uuid_get(&magd->suuid);
 				 * magd->srvid[0] = '/';
 				 * apr_uuid_format(&magd->srvid[1], &magd->suuid);
 				 * In fact we use the srvid on the 2 second byte [1]
 				 */
 				String server = UUID.randomUUID().toString();
 				boolean ok = true;
 				while (ok) {
 					Date date = new Date(System.currentTimeMillis());
 					md.reset();
 					digestString(md, securityKey);
 					byte[] ssalt = md.digest();
 					md.update(ssalt);
 					digestString(md, date);
 					digestString(md, seq);
 					digestString(md, server);
 					byte[] digest = md.digest();
 					StringBuilder str = new StringBuilder();
 					for (int i = 0; i < digest.length; i++)
 						str.append(String.format("%x", digest[i]));
 
 					String sbuf = "HTTP/1.0 200 OK\r\n" + "Date: " + date + "\r\n" + "Sequence: "
 							+ seq + "\r\n" + "Digest: " + str.toString() + "\r\n" + "Server: "
 							+ server + "\r\n" + "X-Manager-Address: " + chost + ":" + cport
 							+ "\r\n" + "X-Manager-Url: /" + server + "\r\n"
 							+ "X-Manager-Protocol: " + scheme + "\r\n" + "X-Manager-Host: " + chost
 							+ "\r\n";
 
 					byte[] buf = sbuf.getBytes();
 					DatagramPacket data = new DatagramPacket(buf, buf.length, group, sport);
 					s.send(data);
 					Thread.sleep(1000);
 					seq++;
 				}
 				s.leaveGroup(group);
 			} catch (Exception Ex) {
 				Ex.printStackTrace(System.out);
 			}
 		}
 
 		private void digestString(MessageDigest md, int seq) {
 			String sseq = "" + seq;
 			digestString(md, sseq);
 		}
 
 		private void digestString(MessageDigest md, Date date) {
 			String sdate = date.toString();
 			digestString(md, sdate);
 		}
 
 		private void digestString(MessageDigest md, String securityKey) {
 			byte buf[] = securityKey.getBytes();
 			md.update(buf);
 
 		}
 
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.apache.coyote.Adapter#event(org.apache.coyote.Request,
 	 * org.apache.coyote.Response, org.apache.tomcat.util.net.SocketStatus)
 	 */
 	public boolean event(Request req, Response res, SocketStatus status) throws Exception {
 		return false;
 	}
 
 	static MCMConfig conf = new MCMConfig();
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.apache.coyote.Adapter#service(org.apache.coyote.Request,
 	 * org.apache.coyote.Response)
 	 */
 	public void service(Request req, Response res) throws Exception {
 
 		System.out.println("service...");
 		MessageBytes methodMB = req.method();
 		if (methodMB.equals(Constants.GET)) {
 			// In fact that is /mod_cluster_manager
 		} else if (methodMB.equals(Constants.CONFIG)) {
 			process_config(req, res);
 		} else if (methodMB.equals(Constants.ENABLE_APP)) {
 			try {
 				process_enable(req, res);
 			} catch (Exception Ex) {
 				Ex.printStackTrace(System.out);
 			}
 		} else if (methodMB.equals(Constants.DISABLE_APP)) {
 			process_disable(req, res);
 		} else if (methodMB.equals(Constants.STOP_APP)) {
 			process_stop(req, res);
 		} else if (methodMB.equals(Constants.REMOVE_APP)) {
 			try {
 				process_remove(req, res);
 			} catch (Exception Ex) {
 				Ex.printStackTrace(System.out);
 			}
 		} else if (methodMB.equals(Constants.STATUS)) {
 			process_status(req, res);
 		} else if (methodMB.equals(Constants.DUMP)) {
 			process_dump(req, res);
 		} else if (methodMB.equals(Constants.INFO)) {
 			try {
 				process_info(req, res);
 			} catch (Exception Ex) {
 				Ex.printStackTrace(System.out);
 			}
 		} else if (methodMB.equals(Constants.PING)) {
 			process_ping(req, res);
 		}
 
 		res.sendHeaders();
 		if (!res.isCommitted()) {
 			// If the response is not committed, then commit
 			res.action(ActionCode.ACTION_COMMIT, res);
 		}
 		// Flush buffers
 		res.action(ActionCode.ACTION_CLIENT_FLUSH, res);
 	}
 
 	/**
 	 * Process <tt>PING</tt> request
 	 * 
 	 * @param req
 	 * @param res
 	 * @throws Exception
 	 */
 	private void process_ping(Request req, Response res) throws Exception {
 		System.out.println("process_ping");
 		Parameters params = req.getParameters();
 		if (params == null) {
 			process_error(TYPESYNTAX, SMESPAR, res);
 			return;
 		}
 		String jvmRoute = null;
 		String scheme = null;
 		String host = null;
 		String port = null;
 
 		Enumeration<String> names = params.getParameterNames();
 		while (names.hasMoreElements()) {
 			String name = (String) names.nextElement();
 			String[] value = params.getParameterValues(name);
 			if (name.equalsIgnoreCase("JVMRoute")) {
 				jvmRoute = value[0];
 			} else if (name.equalsIgnoreCase("Scheme")) {
 				scheme = value[0];
 			} else if (name.equalsIgnoreCase("Port")) {
 				port = value[0];
 			} else if (name.equalsIgnoreCase("Host")) {
 				host = value[0];
 			} else {
 				process_error(TYPESYNTAX, SBADFLD + name + SBADFLD1, res);
 				return;
 			}
 		}
 		if (jvmRoute == null) {
 			if (scheme == null && host == null && port == null) {
 				res.addHeader("Content-Type", "text/plain");
 				String data = "Type=PING-RSP&State=OK";
 				res.setContentLength(data.length());
 				ByteChunk chunk = new ByteChunk();
 				chunk.append(data.getBytes(), 0, data.length());
 				res.doWrite(chunk);
 				return;
 			} else {
 				if (scheme == null || host == null || port == null) {
 					process_error(TYPESYNTAX, SMISFLD, res);
 					return;
 				}
 				res.addHeader("Content-Type", "text/plain");
				String data = "Type=PING-RSP";
 				if (ishost_up(scheme, host, port))
 					data = data.concat("&State=OK");
 				else
 					data = data.concat("&State=NOTOK");
 
 				res.setContentLength(data.length());
 				ByteChunk chunk = new ByteChunk();
 				chunk.append(data.getBytes(), 0, data.length());
 				res.doWrite(chunk);
 			}
 		} else {
 			// ping the corresponding node.
 			Node node = conf.getNode(jvmRoute);
 			if (node == null) {
 				process_error(TYPEMEM, MNODERD, res);
 				return;
 			}
 			res.addHeader("Content-Type", "text/plain");
			String data = "Type=PING-RSP";
 			if (isnode_up(node))
 				data = data.concat("&State=OK");
 			else
 				data = data.concat("&State=NOTOK");
 
 			res.setContentLength(data.length());
 			ByteChunk chunk = new ByteChunk();
 			chunk.append(data.getBytes(), 0, data.length());
 			res.doWrite(chunk);
 		}
 	}
 
 	private boolean isnode_up(Node node) {
 		System.out.println("process_ping: " + node);
 		return false;
 	}
 
 	private boolean ishost_up(String scheme, String host, String port) {
 		System.out.println("process_ping: " + scheme + "://" + host + ":" + port);
 		return false;
 	}
 
 	/*
 	 * Something like:
 	 * 
 	 * Node: [1],Name: 368e2e5c-d3f7-3812-9fc6-f96d124dcf79,Balancer:
 	 * cluster-prod-01,LBGroup: ,Host: 127.0.0.1,Port: 8443,Type:
 	 * https,Flushpackets: Off,Flushwait: 10,Ping: 10,Smax: 21,Ttl: 60,Elected:
 	 * 0,Read: 0,Transfered: 0,Connected: 0,Load: 1 Vhost: [1:1:1], Alias:
 	 * default-host Vhost: [1:1:2], Alias: localhost Vhost: [1:1:3], Alias:
 	 * example.com Context: [1:1:1], Context: /myapp, Status: ENABLED
 	 */
 
 	/**
 	 * Process <tt>INFO</tt> request
 	 * 
 	 * @param req
 	 * @param res
 	 * @throws Exception
 	 */
 	private void process_info(Request req, Response res) throws Exception {
 
 		String data = process_info_string();
 		process_OK(res);
 		res.addHeader("Content-Type", "text/plain");
 		res.addHeader("Server", "Mod_CLuster/0.0.0");
 		if (data.length() > 0) {
 			res.setContentLength(data.length());
 		}
 
 		ByteChunk chunk = new ByteChunk();
 		chunk.append(data.getBytes(), 0, data.length());
 		res.doWrite(chunk);
 	}
 
 	private String process_info_string() {
 		int i = 1;
 		StringBuilder data = new StringBuilder();
 
 		for (Node node : conf.getNodes()) {
 			data.append("Node: [").append(i).append("],Name: ").append(node.getJvmRoute())
 					.append(",Balancer: ").append(node.getBalancer()).append(",LBGroup: ")
 					.append(node.getDomain()).append(",Host: ").append(node.getHostname())
 					.append(",Port: ").append(node.getPort()).append(",Type: ")
 					.append(node.getType()).append(",Flushpackets: ")
 					.append((node.isFlushpackets() ? "On" : "Off")).append(",Flushwait: ")
 					.append(node.getFlushwait()).append(",Ping: ").append(node.getPing())
 					.append(",Smax: ").append(node.getSmax()).append(",Ttl: ")
 					.append(node.getTtl()).append(",Elected: ").append(node.getElected())
 					.append(",Read: ").append(node.getRead()).append(",Transfered: ")
 					.append(node.getTransfered()).append(",Connected: ")
 					.append(node.getConnected()).append(",Load: ").append(node.getLoad() + "\n");
 			i++;
 		}
 
 		for (VHost host : conf.getHosts()) {
 			int j = 1;
 			long node = conf.getNodeId(host.getJVMRoute());
 			for (String alias : host.getAliases()) {
 				data.append("Vhost: [").append(node).append(":").append(host.getId()).append(":")
 						.append(j).append("], Alias: ").append(alias).append("\n");
 
 				j++;
 			}
 		}
 
 		i = 1;
 		for (Context context : conf.getContexts()) {
 			data.append("Context: [").append(conf.getNodeId(context.getJVMRoute())).append(":")
 					.append(context.getHostId()).append(":").append(i).append("], Context: ")
 					.append(context.getPath()).append(", Status: ").append(context.getStatus())
 					.append("\n");
 
 			// TODO do we need to increment i ?
 			// i++;
 		}
 		return data.toString();
 	}
 
 	/*
 	 * something like:
 	 * 
 	 * balancer: [1] Name: cluster-prod-01 Sticky: 1 [JSESSIONID]/[jsessionid]
 	 * remove: 0 force: 0 Timeout: 0 maxAttempts: 1 node: [1:1],Balancer:
 	 * cluster-prod-01,JVMRoute: 368e2e5c-d3f7-3812-9fc6-f96d124dcf79,LBGroup:
 	 * [],Host: 127.0.0.1,Port: 8443,Type: https,flushpackets: 0,flushwait:
 	 * 10,ping: 10,smax: 21,ttl: 60,timeout: 0 host: 1 [default-host] vhost: 1
 	 * node: 1 host: 2 [localhost] vhost: 1 node: 1 host: 3 [example.com] vhost:
 	 * 1 node: 1 context: 1 [/myapp] vhost: 1 node: 1 status: 1
 	 */
 
 	/**
 	 * Process <tt>DUMP</tt> request
 	 * 
 	 * @param req
 	 * @param res
 	 */
 	private void process_dump(Request req, Response res) {
 		String data = "";
 		int i = 1;
 		for (Balancer balancer : conf.getBalancers()) {
 			String bal = "balancer: [" + i + "] Name: " + balancer.getName() + " Sticky: "
 					+ (balancer.isStickySession() ? "1" : "0") + " ["
 					+ balancer.getStickySessionCookie() + "]/[" + balancer.getStickySessionPath()
 					+ "] remove: " + (balancer.isStickySessionRemove() ? "1" : "0") + " force: "
 					+ (balancer.isStickySessionForce() ? "1" : "0") + " Timeout: "
 					+ balancer.getWaitWorker() + " maxAttempts: " + balancer.getMaxattempts()
 					+ "\n";
 			data = data.concat(bal);
 			i++;
 		}
 		// TODO Add more...
 
 		System.out.println("process_dump");
 	}
 
 	/**
 	 * Process <tt>STATUS</tt> request
 	 * 
 	 * @param req
 	 * @param res
 	 * @throws Exception
 	 */
 	private void process_status(Request req, Response res) throws Exception {
 		Parameters params = req.getParameters();
 		if (params == null) {
 			process_error(TYPESYNTAX, SMESPAR, res);
 			return;
 		}
 		String jvmRoute = null;
 		String load = null;
 		Enumeration<String> names = params.getParameterNames();
 		while (names.hasMoreElements()) {
 			String name = (String) names.nextElement();
 			String[] value = params.getParameterValues(name);
 			if (name.equalsIgnoreCase("JVMRoute")) {
 				jvmRoute = value[0];
 			} else if (name.equalsIgnoreCase("Load")) {
 				load = value[0];
 			} else {
 				process_error(TYPESYNTAX, SBADFLD + value[0] + SBADFLD1, res);
 				return;
 			}
 		}
 		if (load == null || jvmRoute == null) {
 			process_error(TYPESYNTAX, SMISFLD, res);
 			return;
 		}
 
 		Node node = conf.getNode(jvmRoute);
 		if (node == null) {
 			process_error(TYPEMEM, MNODERD, res);
 			return;
 		}
 		node.setLoad(Integer.parseInt(load));
 		/* TODO we need to check the node here */
 		node.setStatus(Node.NodeStatus.NODE_UP);
 		process_OK(res);
 	}
 
 	/**
 	 * Process <tt>REMOVE-APP</tt> request
 	 * 
 	 * @param req
 	 * @param res
 	 * @throws Exception
 	 */
 	private void process_remove(Request req, Response res) throws Exception {
 		Parameters params = req.getParameters();
 		if (params == null) {
 			process_error(TYPESYNTAX, SMESPAR, res);
 			return;
 		}
 
 		boolean global = false;
 		if (req.unparsedURI().toString().equals("*") || req.unparsedURI().toString().endsWith("/*")) {
 			global = true;
 		}
 		Context context = new Context();
 		VHost host = new VHost();
 		Enumeration<String> names = params.getParameterNames();
 		while (names.hasMoreElements()) {
 			String name = (String) names.nextElement();
 			String[] value = params.getParameterValues(name);
 			if (name.equalsIgnoreCase("JVMRoute")) {
 				if (conf.getNodeId(value[0]) == -1) {
 					process_error(TYPEMEM, MNODERD, res);
 					return;
 				}
 				host.setJVMRoute(value[0]);
 				context.setJVMRoute(value[0]);
 			} else if (name.equalsIgnoreCase("Alias")) {
 				// Alias is something like =default-host,localhost,example.com
 				String aliases[] = value[0].split(",");
 				host.setAliases(Arrays.asList(aliases));
 			} else if (name.equalsIgnoreCase("Context")) {
 				context.setPath(value[0]);
 			}
 
 		}
 		if (context.getJVMRoute() == null) {
 			process_error(TYPESYNTAX, SROUBAD, res);
 			return;
 		}
 
 		if (global)
 			conf.removeNode(context.getJVMRoute());
 		else
 			conf.remove(context, host);
 		process_OK(res);
 	}
 
 	/**
 	 * Process <tt>STOP-APP</tt> request
 	 * 
 	 * @param req
 	 * @param res
 	 * @throws Exception
 	 */
 	private void process_stop(Request req, Response res) throws Exception {
 		process_cmd(req, res, Context.Status.STOPPED);
 	}
 
 	/**
 	 * Process <tt>DISABLE-APP</tt> request
 	 * 
 	 * @param req
 	 * @param res
 	 * @throws Exception
 	 */
 	private void process_disable(Request req, Response res) throws Exception {
 		process_cmd(req, res, Context.Status.DISABLED);
 	}
 
 	/**
 	 * Process <tt>ENABLE-APP</tt> request
 	 * 
 	 * @param req
 	 * @param res
 	 * @throws Exception
 	 */
 	private void process_enable(Request req, Response res) throws Exception {
 		process_cmd(req, res, Context.Status.ENABLED);
 	}
 
 	private void process_cmd(Request req, Response res, Context.Status status) throws Exception {
 		Parameters params = req.getParameters();
 		if (params == null) {
 			process_error(TYPESYNTAX, SMESPAR, res);
 			return;
 		}
 
 		if (req.unparsedURI().toString().equals("*") || req.unparsedURI().toString().endsWith("/*")) {
 			process_node_cmd(req, res, status);
 			return;
 		}
 
 		Context context = new Context();
 		VHost host = new VHost();
 		Enumeration<String> names = params.getParameterNames();
 		while (names.hasMoreElements()) {
 			String name = (String) names.nextElement();
 			String[] value = params.getParameterValues(name);
 			if (name.equalsIgnoreCase("JVMRoute")) {
 				if (conf.getNodeId(value[0]) == -1) {
 					process_error(TYPEMEM, MNODERD, res);
 					return;
 				}
 				host.setJVMRoute(value[0]);
 				context.setJVMRoute(value[0]);
 			} else if (name.equalsIgnoreCase("Alias")) {
 				// Alias is something like =default-host,localhost,example.com
 				String aliases[] = value[0].split(",");
 				host.setAliases(Arrays.asList(aliases));
 			} else if (name.equalsIgnoreCase("Context")) {
 				context.setPath(value[0]);
 			}
 
 		}
 		if (context.getJVMRoute() == null) {
 			process_error(TYPESYNTAX, SROUBAD, res);
 			return;
 		}
 		context.setStatus(status);
 		long id = conf.insertupdate(host);
 		context.setHostid(id);
 		conf.insertupdate(context);
 		process_OK(res);
 	}
 
 	private void process_node_cmd(Request req, Response res, Status enabled) {
 		System.out.println("process_node_cmd:" + process_info_string());
 	}
 
 	/**
 	 * Process <tt>CONFIG</tt> request
 	 * 
 	 * @param req
 	 * @param res
 	 * @throws Exception
 	 */
 	private void process_config(Request req, Response res) throws Exception {
 		Parameters params = req.getParameters();
 		if (params == null) {
 			process_error(TYPESYNTAX, SMESPAR, res);
 			return;
 		}
 
 		Balancer balancer = new Balancer();
 		Node node = new Node();
 
 		Enumeration<String> names = params.getParameterNames();
 		while (names.hasMoreElements()) {
 			String name = (String) names.nextElement();
 			String[] value = params.getParameterValues(name);
 			if (name.equalsIgnoreCase("Balancer")) {
 				balancer.setName(value[0]);
 				node.setBalancer(value[0]);
 			} else if (name.equalsIgnoreCase("StickySession")) {
 				if (value[0].equalsIgnoreCase("No"))
 					balancer.setStickySession(false);
 			} else if (name.equalsIgnoreCase("StickySessionCookie")) {
 				balancer.setStickySessionCookie(value[0]);
 			} else if (name.equalsIgnoreCase("StickySessionPath")) {
 				balancer.setStickySessionPath(value[0]);
 			} else if (name.equalsIgnoreCase("StickySessionRemove")) {
 				if (value[0].equalsIgnoreCase("Yes"))
 					balancer.setStickySessionRemove(true);
 			} else if (name.equalsIgnoreCase("StickySessionForce")) {
 				if (value[0].equalsIgnoreCase("no"))
 					balancer.setStickySessionForce(false);
 			} else if (name.equalsIgnoreCase("WaitWorker")) {
 				balancer.setWaitWorker(Integer.valueOf(value[0]));
 			} else if (name.equalsIgnoreCase("Maxattempts")) {
 				balancer.setMaxattempts(Integer.valueOf(value[0]));
 			} else if (name.equalsIgnoreCase("JVMRoute")) {
 				node.setJvmRoute(value[0]);
 			} else if (name.equalsIgnoreCase("Domain")) {
 				node.setDomain(value[0]);
 			} else if (name.equalsIgnoreCase("Host")) {
 				node.setHostname(value[0]);
 			} else if (name.equalsIgnoreCase("Port")) {
 				node.setPort(Integer.valueOf(value[0]));
 			} else if (name.equalsIgnoreCase("Type")) {
 				node.setType(value[0]);
 			} else if (name.equalsIgnoreCase("Reversed")) {
 				continue; // ignore it.
 			} else if (name.equalsIgnoreCase("flushpacket")) {
 				if (value[0].equalsIgnoreCase("on"))
 					node.setFlushpackets(true);
 				if (value[0].equalsIgnoreCase("auto"))
 					node.setFlushpackets(true);
 			} else if (name.equalsIgnoreCase("flushwait")) {
 				node.setFlushwait(Integer.valueOf(value[0]));
 			} else if (name.equalsIgnoreCase("ping")) {
 				node.setPing(Integer.valueOf(value[0]));
 			} else if (name.equalsIgnoreCase("smax")) {
 				node.setSmax(Integer.valueOf(value[0]));
 			} else if (name.equalsIgnoreCase("ttl")) {
 				node.setTtl(Integer.valueOf(value[0]));
 			} else if (name.equalsIgnoreCase("Timeout")) {
 				node.setTimeout(Integer.valueOf(value[0]));
 			} else {
 				process_error(TYPESYNTAX, SBADFLD + name + SBADFLD1, res);
 				return;
 			}
 		}
 
 		conf.insertupdate(balancer);
 		conf.insertupdate(node);
 		process_OK(res);
 	}
 
 	/**
 	 * If the process is OK, then add 200 HTTP status and its "OK" phrase
 	 * 
 	 * @param res
 	 * @throws Exception
 	 */
 	private void process_OK(Response res) throws Exception {
 		res.setStatus(200);
 		res.setMessage("OK");
 		res.addHeader("Content-type", "plain/text");
 	}
 
 	/**
 	 * If any error occurs,
 	 * 
 	 * @param type
 	 * @param errstring
 	 * @param res
 	 * @throws Exception
 	 */
 	private void process_error(String type, String errstring, Response res) throws Exception {
 		res.setStatus(500);
 		res.setMessage("ERROR");
 		res.addHeader("Version", VERSION_PROTOCOL);
 		res.addHeader("Type", type);
 		res.addHeader("Mess", errstring);
 	}
 }
