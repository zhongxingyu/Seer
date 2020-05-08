 package edu.uw.cs.cse461.Net.DDNS;
 
 import java.util.HashMap;
 import java.util.Map;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import edu.uw.cs.cse461.HTTP.HTTPProviderInterface;
 import edu.uw.cs.cse461.Net.Base.NetBase;
 import edu.uw.cs.cse461.Net.Base.NetLoadable.NetLoadableService;
 import edu.uw.cs.cse461.Net.DDNS.DDNSException.DDNSAuthorizationException;
 import edu.uw.cs.cse461.Net.DDNS.DDNSException.DDNSNoAddressException;
 import edu.uw.cs.cse461.Net.DDNS.DDNSException.DDNSNoSuchNameException;
 import edu.uw.cs.cse461.Net.DDNS.DDNSException.DDNSRuntimeException;
 import edu.uw.cs.cse461.Net.DDNS.DDNSException.DDNSTTLExpiredException;
 import edu.uw.cs.cse461.Net.DDNS.DDNSException.DDNSZoneException;
 import edu.uw.cs.cse461.Net.DDNS.DDNSRRecord.*;
 import edu.uw.cs.cse461.Net.RPC.RPCCallableMethod;
 import edu.uw.cs.cse461.Net.RPC.RPCService;
 import edu.uw.cs.cse461.util.Log;
 
 /**
  * Protocol: Based on RPC.  The calls:
  * <p>
  * Request:  method: "register" 
  *           args: 
  * <br>Response:  void
  * <p>
  * Fetch all records (for all apps) for a specific host.
  * Request:  method: "fetchall"
  *           args:  {host: hostname}
  * <br>Response:  [ [appname, port, authoritative], ...]
  *
  * <pre>
  * app:"ddns" supports RPC calls:
  *     register( {host: hostname,  ip: ipaddr,   port: portnum} ) => { status: "OK" } or errormsg
  *     resolve( { host: hostname } ) => { host: repeats hostname, ip: ip address, authoritative: boolean } ) or errormsg
  * </pre>
  * 
  *  * @author zahorjan
  *
  */
 public class DDNSService extends NetLoadableService implements HTTPProviderInterface, DDNSServiceInterface {
 	private static String TAG="DDNSService";
 
 	private RPCCallableMethod resolve;
 	private RPCCallableMethod register;
 	private RPCCallableMethod unregister;
 
 	private DDNSNode mRoot = null;
 
 	private static final int CNAMERECORD_CONFIG_PARTS = 4;
 	private static final int ARECORD_CONFIG_PARTS = 3;
 
 	private static final String SOA_PREFIX = "SOA";
 	private static final String A_PREFIX = "A";
 	private static final String CNAME_PREFIX = "CNAME";
 	private static final String NS_PREFIX = "NS";
 
 	/**
 	 * Called to end execution.  Specifically, need to terminate any threads we've created.
 	 */
 	@Override
 	public void shutdown() {
 		super.shutdown();
 	}
 
 	@Override
 	public String httpServe(String[] uriArray) { return toString();	}
 
 	/**
 	 * Constructor.  Registers the system RPCServerSocket with the parent as
 	 * this host's ip address.  Registers the root server and itself in the
 	 * local name cache.
 	 * @throws DDNSException
 	 */
 	public DDNSService() throws DDNSException {
 		super("ddns", true);
 
 		try {
 			//--------------------------------------------------------------
 			// set up RPC callable methods
 			//--------------------------------------------------------------
 
 			// export methods via the rpc service
 			resolve = new RPCCallableMethod(this, "_rpcResolve");
 			register = new RPCCallableMethod(this, "_rpcRegister");
 			unregister = new RPCCallableMethod(this, "_rpcUnregister");
 
 			RPCService rpcService = (RPCService)NetBase.theNetBase().getService("rpc");
 			rpcService.registerHandler(loadablename(), "register", register );
 			rpcService.registerHandler(loadablename(), "unregister", unregister );
 			rpcService.registerHandler(loadablename(), "resolve", resolve );
 
 			buildRecordTree();
 		} catch (Exception e) {
 			String msg = "DDNSService constructor caught exception: " + e.getMessage();
 			Log.e(TAG, msg);
 			e.printStackTrace();
 			throw new DDNSRuntimeException(msg);
 		}
 	}
 
 	private void buildRecordTree() throws DDNSException {
 		String[] nodes = NetBase.theNetBase().config().getAsStringVec("ddns.nodes");
 		if(nodes == null || nodes.length == 0) {
 			throw new DDNSRuntimeException("no nodes found in config file");
 		}
 
 		for(int i = 0; i < nodes.length; i++) {
 			nodes[i] = nodes[i].trim();
 			checkValidNode(nodes[i]);
 		}
 
 		String[] soaNode = nodes[0].split(":");
 		if(!soaNode[0].toUpperCase().equals(SOA_PREFIX)) {
 			throw new DDNSRuntimeException("first node in list must be an SOA node");
 		} else {
 			String hostname = NetBase.theNetBase().config().getProperty("net.hostname", null);
 			if(hostname == null) {
 				throw new DDNSRuntimeException("net.hostname cannot be null");
 			} else if(!soaNode[1].equals(hostname)) {
 				throw new DDNSRuntimeException("SOA node must have same name as the hostname");
 			}
 		}
 
 		mRoot = new DDNSNode(soaNode[1], soaNode[2], new SOARecord());
 
 		for(int i = 1; i < nodes.length; i++) {
 			String[] nodeInfo = nodes[i].split(":");
 			String parentName = nodeInfo[1];
 			parentName = parentName.substring(parentName.indexOf('.')+1);
 			if(parentName.equals(nodeInfo[1])) {
 				throw new DDNSRuntimeException(nodeInfo[1] + " is already the address of the SOA");
 			}
 			
 			DDNSNode parentNode = nodeLookup(parentName, true);
 			DDNSRRecord record = parentNode.getRecord();
 			if(record.type() == RRType.RRTYPE_CNAME || record.type() == RRType.RRTYPE_NS) {
 				throw new DDNSRuntimeException(nodeInfo[1] + " has a parent that is either a CNAME or NS");
 			}
 			
 			if(parentNode.hasChild(nodeInfo[1])) {
 				throw new DDNSRuntimeException(nodeInfo[1] + " has already been created");
 			} else if(nodeInfo[0].toUpperCase().equals(SOA_PREFIX)) {
 				throw new DDNSRuntimeException(nodeInfo[1] + " cannot be another SOA node");
 			} else if(nodeInfo[0].toUpperCase().equals(CNAME_PREFIX)) {
 				record = new CNAMERecord(nodeInfo[2]);  
 			} else if(nodeInfo[0].toUpperCase().equals(A_PREFIX)) {
 				record = new ARecord();
 			} else if(nodeInfo[0].toUpperCase().equals(NS_PREFIX)) {
 				record = new NSRecord();
 			}
 			
 			String pw = nodeInfo[nodeInfo.length-1];
 			DDNSNode newNode = new DDNSNode(nodeInfo[1], pw, record);
 			parentNode.addChild(newNode);
 		}
 	}
 	
 	//check config file node string is of correct format
 	private void checkValidNode(String node) throws DDNSException { 
 		String[] nodeInfo = node.split(":");
 		boolean valid = false;
 		if(nodeInfo.length == ARECORD_CONFIG_PARTS && 
 			(nodeInfo[0].toUpperCase().equals(A_PREFIX) ||
 			nodeInfo[0].toUpperCase().equals(NS_PREFIX) ||
 			nodeInfo[0].toUpperCase().equals(SOA_PREFIX))) {
 			
 			valid = true;
 			
 		} else if(nodeInfo.length == CNAMERECORD_CONFIG_PARTS &&
 				nodeInfo[0].toUpperCase().equals(CNAME_PREFIX)) {
 			valid = true;
 		}
 			
 		if(!valid) {
 			throw new DDNSRuntimeException("Invalid node definition format: " + node);
 		}
 		
 		String hostname = NetBase.theNetBase().config().getProperty("net.hostname", null);
 		if(hostname == null) {
 			throw new DDNSRuntimeException("hostname cannot be null");
 		} else if(valid && !nodeInfo[1].endsWith(hostname)) {
 			throw new DDNSRuntimeException("cannot create node that is not in the zone: " + hostname);
 		}
 	}
 	
 	//---------------------------------------------------------------------------
 	// RPC callable routines
 
 	/**
 	 * Indicates host is going offline.
 	 *      unregister( {name: name, password: password} ) => { status: "OK" } or errormsg
 	 * @param args
 	 * @return
 	 * @throws JSONException
 	 * @throws DDNSException
 	 */
 	public JSONObject _rpcUnregister(JSONObject args) throws JSONException, DDNSException {
 		JSONObject resultJSON = new JSONObject();
 		String name = args.getString("name");
 		try {
 			synchronized(this) {
 				DDNSNode node = nodeLookup(name);
 				DDNSRRecord record = node.getRecord();
 				
 				if(record.type() == RRType.RRTYPE_CNAME) {
 					JSONObject nodeJSON = record.marshall();
 					nodeJSON.put("name", node.getName());
 					
 					resultJSON.put("node", nodeJSON);
 					resultJSON.put("done", false);
 				} else { 
 					String pw = args.getString("password");
 					
 					if(record.type() == RRType.RRTYPE_NS){
 						if(node.getName().equals(name))
 							node.unregister(pw);
 						
 						JSONObject nodeJSON = record.marshall();
 						nodeJSON.put("name", node.getName());
 						
 						resultJSON.put("node", nodeJSON);
 						resultJSON.put("done", false);
 					} else { //A or SOA
 						node.unregister(pw);
 						resultJSON.put("done", true);
 					}
 				}
 			}
			resultJSON.put("resulttype", "unregisterresult");
 		} catch(DDNSException e) {
 			resultJSON = ddnsexceptionToJSON(e);
 		}
 			
 		return resultJSON;
 	}
 
 	/**
 	 *   register( {name: <string>, password: <string>, ip: <string>,  port: <int>} ) => { DDNSNode } or errormsg
 	 *<p>
 	 * We accept only requests for names stored on this server.
 	 * 
 	 * @param args
 	 * @return
 	 * @throws JSONException 
 	 * @throws DDNSException 
 	 */
 	public JSONObject _rpcRegister(JSONObject args) throws JSONException {
 		JSONObject resultJSON = new JSONObject();
 		String name = args.getString("name");
 		
 		try {
 			synchronized(this) {
 				DDNSNode node = nodeLookup(name);
 				DDNSRRecord record = node.getRecord();
 				
 				if(record.type() == RRType.RRTYPE_CNAME) {
 					JSONObject nodeJSON = record.marshall();
 					nodeJSON.put("name", node.getName());
 					
 					resultJSON.put("node", nodeJSON);
 					resultJSON.put("done", false);
 				} else { 
 					String ip = args.getString("ip");
 					int port = args.getInt("port");
 					String pw = args.getString("password");
 					
 					if(record.type() == RRType.RRTYPE_NS){
 						if(node.getName().equals(name))
 							node.register(ip, port, pw);
 						
 						JSONObject nodeJSON = record.marshall();
 						nodeJSON.put("name", node.getName());
 						
 						resultJSON.put("node", nodeJSON);
 						resultJSON.put("done", false);
 						resultJSON.put("lifetime", DDNSNode.REG_LIFETIME);
 					} else { //A or SOA
 						node.register(ip, port, pw);
 						
 						JSONObject nodeJSON = record.marshall();
 						nodeJSON.put("name", node.getName());
 						
 						resultJSON.put("node", nodeJSON);
 						resultJSON.put("done", true);
 						resultJSON.put("lifetime", DDNSNode.REG_LIFETIME);
 					}
 				}
 			}
			resultJSON.put("resulttype", "registerresult");
 		} catch(DDNSException e) {
 			resultJSON = ddnsexceptionToJSON(e);
 		}
 		return resultJSON;
 	}
 
 	/**
 	 * This version is invoked via RPC.  It's simply a wrapper that extracts the call arguments
 	 * and invokes resolve(host).
 	 * @param callArgs
 	 * @return
 	 * @throws DDNSException 
 	 * @throws JSONException 
 	 */
 	public JSONObject _rpcResolve(JSONObject args) throws JSONException {
 		JSONObject resultJSON = new JSONObject();
 		String name = args.getString("name");
 		try {
 			synchronized(this) {
 				DDNSNode node = nodeLookup(name);
 				DDNSRRecord record = node.getRecord();
 				
 				JSONObject nodeJSON = record.marshall();
 				nodeJSON.put("name", node.getName());
 				resultJSON.put("node", nodeJSON);
 				if(record.type() == RRType.RRTYPE_CNAME) {
 					resultJSON.put("done", false);
 				} else if(record.type() == RRType.RRTYPE_NS){
 					if(!node.isValid())
 						throw new DDNSNoAddressException(new DDNSFullName(name));
 					resultJSON.put("done", false);
 				} else { //A or SOA
 					if(!node.isValid())
 						throw new DDNSNoAddressException(new DDNSFullName(name));
 					resultJSON.put("done", true);
 				}	
 			}
			resultJSON.put("resulttype", "resolveresult");
 		} catch(DDNSException e) {
 			resultJSON = ddnsexceptionToJSON(e);
 		}
 		return resultJSON;
 	}
 
 	private DDNSNode nodeLookup(String name) throws DDNSException {
 		return nodeLookup(name, false);
 	}
 	
 	private DDNSNode nodeLookup(String name, boolean suppressNoAddressErrors) throws DDNSException {
 		if(!name.endsWith(mRoot.getName())) {
 			DDNSFullNameInterface nodeName = new DDNSFullName(name);
 			DDNSFullNameInterface zoneName = new DDNSFullName(mRoot.getName());
 			throw new DDNSZoneException(nodeName, zoneName);
 		}
 
 		
 		DDNSNode curNode = mRoot;
 		while(curNode != null) {
 			DDNSRRecord curRecord = curNode.getRecord();
 			if(curNode.getName().equals(name) &&
 					(curRecord.type() == RRType.RRTYPE_A ||
 					curRecord.type() == RRType.RRTYPE_SOA)) { //resolved to A or SOA
 				if(!suppressNoAddressErrors && !curNode.isValid()) {
 					throw new DDNSNoAddressException(new DDNSFullName(name));
 				}
 				return curNode;
 
 			} else if(curRecord.type() == RRType.RRTYPE_CNAME) { //reached CNAME
 				return curNode;
 
 			} else if(curRecord.type() == RRType.RRTYPE_NS) { //reached NS
 				if(!suppressNoAddressErrors && !curNode.isValid()) {
 					throw new DDNSNoAddressException(new DDNSFullName(name));
 				}
 				return curNode;
 			}
 			
 			int decimalIndex = name.lastIndexOf('.', name.length() - curNode.getName().length() - 2)+1;
 			String childName = name.substring(decimalIndex);
 			curNode = curNode.getChild(childName);
 		}
 
 		throw new DDNSNoSuchNameException(new DDNSFullName(name));
 	}
 
 	private JSONObject ddnsexceptionToJSON(DDNSException ex) {
 		JSONObject result = new JSONObject();
 		try {
 			result.put("resulttype", "ddnsexception");
 			if(ex instanceof DDNSNoSuchNameException) {
 				result.put("exceptionnum", 1);
 			} else if(ex instanceof DDNSNoAddressException) {
 				result.put("exceptionnum", 2);
 			} else if(ex instanceof DDNSAuthorizationException) {
 				result.put("exceptionnum", 3);
 			} else if(ex instanceof DDNSRuntimeException) {
 				result.put("exceptionnum", 4);
 			} else if(ex instanceof DDNSTTLExpiredException) {
 				result.put("exceptionnum", 5);
 			} else if(ex instanceof DDNSZoneException) {
 				result.put("exceptionnum", 6);
 			} else {
 				throw new RuntimeException("Invalid DDNSException type");
 			}
 			result.put("message", ex.getMessage());
 			int i = 1;
 			for(String arg : ex.args) {
 				if(i == 1) {
 					result.put("name", arg);
 				} else if(i == 2) {
 					result.put("zone", arg);
 				} else {
 					result.put("other arg " + i, arg);
 				}
 				i++;
 			}
 		} catch(JSONException e) {
 			//shouldn't happen
 		}
 		return result;
 	}
 	
 	// RPC callable routines
 	//---------------------------------------------------------------------------
 
 	@Override
 	public String dumpState() {
 		return "whatever you'd like";
 	}
 
 
 	// private classes for DDNS Tree and unregistering cleanup 
 	//---------------------------------------------------------------------------
 	private static class DDNSNode {
 		public static final int REG_LIFETIME = NetBase.theNetBase().config().getAsInt("ddns.registerlifetime", 15, TAG); //seconds
 		private String nFullname;
 		private String nPassword;
 		private DDNSRRecord nRecord;
 		private long nDieAt; //die at this time, time in millis
 		private Map<String, DDNSNode> nChildren = new HashMap<String, DDNSNode>();
 
 		public DDNSNode(String fullname, String pw, DDNSRRecord record) { 
 			nFullname = fullname;
 			nRecord = record;
 			nDieAt =  System.currentTimeMillis() + REG_LIFETIME*1000;
 			nPassword = pw;
 		}
 
 		public void addChild(DDNSNode child) {
 			nChildren.put(child.getName(), child);
 		}
 
 		public String getName() {
 			return nFullname;
 		}
 
 		public boolean hasChild(String childName) {
 			return nChildren.containsKey(childName);
 		}
 
 		public DDNSNode getChild(String childName) {
 			return nChildren.get(childName);
 		}
 
 		public DDNSRRecord getRecord() {
 			if(!this.isValid() && nRecord instanceof ARecord)
 				((ARecord) nRecord).updateAddress(null, -1);
 			return nRecord;
 		}
 
 		public void register(String ip, int port, String pw) throws DDNSException {
 			if(!pw.equals(nPassword))
 				throw new DDNSAuthorizationException(new DDNSFullName(nFullname));
 			else if(!(nRecord instanceof ARecord))
 				throw new DDNSRuntimeException("Cannot register CNAME Record");
 			((ARecord) nRecord).updateAddress(ip, port);
 			nDieAt = System.currentTimeMillis() + REG_LIFETIME*1000;
 		}
 		
 		public void unregister(String pw) throws DDNSException {
 			if(!pw.equals(nPassword))
 				throw new DDNSAuthorizationException(new DDNSFullName(nFullname));
 			else if(!(nRecord instanceof ARecord))
 				throw new DDNSRuntimeException("Cannot unregister CNAME Record");
 			((ARecord) nRecord).updateAddress(null, -1);
 		}
 		
 		public boolean isValid() {
 			if(nRecord instanceof ARecord) {
 				return nDieAt > System.currentTimeMillis() &&
 						((ARecord) nRecord).mIP != null;
 			} else {
 				return true;
 			}
 			
 		}
 		
 		public String toString() {
 			try {
 				JSONObject result = new JSONObject();
 				result.put("Fullname", this.getName());
 				result.put("Password", this.nPassword);
 				result.put("Record", this.getRecord());
 				if(this.nChildren.size() > 0) {
 					int i = 1;
 					for(String child : nChildren.keySet()) {
 						result.append("Children", child);
 					}
 				} else {
 					result.put("Children", "None");
 				}
 				return result.toString();
 			} catch(JSONException e) {
 				//shouldn't happen
 			}
 			return null;
 		}
 	}
 }
