 package edu.uw.cs.cse461.Net.DDNS;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.json.JSONArray;
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
 		}
 
 		mRoot = new DDNSNode(new DDNSFullName(soaNode[1]), soaNode[2], new SOARecord());
 
 		for(int i = 1; i < nodes.length; i++) {
 			String[] nodeInfo = nodes[i].split(":");
 			DDNSFullNameInterface nodeName = new DDNSFullName(nodeInfo[1]);
 			DDNSFullNameInterface parentName = nodeName.parent();
 			
 			DDNSNode parentNode = nodeLookup(parentName, true);
 			DDNSRRecord record = parentNode.getRecord();
 			if(record.type() == RRType.RRTYPE_CNAME || record.type() == RRType.RRTYPE_NS) {
 				throw new DDNSRuntimeException(nodeInfo[1] + " has a parent that is either a CNAME or NS");
 			}
 			
 			if(parentNode.hasChild(nodeName)) {
 				throw new DDNSRuntimeException(nodeName.toString() + " has already been created");
 			} else if(nodeInfo[0].toUpperCase().equals(SOA_PREFIX)) {
 				throw new DDNSRuntimeException(nodeName.toString() + " cannot be another SOA node");
 			} else if(nodeInfo[0].toUpperCase().equals(CNAME_PREFIX)) {
 				record = new CNAMERecord(nodeInfo[2]);  
 			} else if(nodeInfo[0].toUpperCase().equals(A_PREFIX)) {
 				record = new ARecord();
 			} else if(nodeInfo[0].toUpperCase().equals(NS_PREFIX)) {
 				record = new NSRecord();
 			}
 			
 			String pw = nodeInfo[nodeInfo.length-1];
 			DDNSNode newNode = new DDNSNode(nodeName, pw, record);
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
 	public JSONObject _rpcUnregister(JSONObject args) {
 		JSONObject resultJSON = new JSONObject();
 		try {
 			DDNSFullNameInterface name = new DDNSFullName(args.getString("name"));
 			String pw = args.getString("password");
 			
 			DDNSNode node;
 			RRType recordType;
 			JSONObject nodeJSON;
 			synchronized(this) {
 				node = nodeLookup(name);
 				DDNSRRecord record = node.getRecord();
 				
 				if(node.getName().equals(name))
 					node.unregister(pw);
 				
 				recordType = record.type();
 				
 				nodeJSON = record.marshall();
 				nodeJSON.put("name", node.getName());	
 			}
 			
 			if(recordType == RRType.RRTYPE_CNAME) {
 				resultJSON.put("node", nodeJSON);
 				resultJSON.put("done", false);
 			} else if(recordType == RRType.RRTYPE_NS) {
 				resultJSON.put("node", nodeJSON);
 				resultJSON.put("done", false);
 			} else { //A or SOA
 				resultJSON.put("done", true);
 			}
 			resultJSON.put("resulttype", "unregisterresult");
 		} catch(DDNSException e) {
 			resultJSON = ddnsexceptionToJSON(e);
 		} catch(JSONException e) {
 			resultJSON = ddnsexceptionToJSON(new DDNSRuntimeException(e.getMessage()));
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
 	public JSONObject _rpcRegister(JSONObject args)  {
 		JSONObject resultJSON = new JSONObject();
 		
 		try {
 			DDNSFullNameInterface name = new DDNSFullName(args.getString("name"));
 			String ip = args.getString("ip");
 			int port = args.getInt("port");
 			String pw = args.getString("password");
 			
 			DDNSNode node;
 			RRType recordType;
 			JSONObject nodeJSON;
 			synchronized(this) {
 				node = nodeLookup(name, true);
 				DDNSRRecord record = node.getRecord();
 				if(node.getName().equals(name))
 					node.register(ip, port, pw);
 				
 				nodeJSON = record.marshall();
 				nodeJSON.put("name", node.getName());
 				recordType = record.type();
 			}
 			
 			resultJSON.put("node", nodeJSON);
 			if(recordType == RRType.RRTYPE_CNAME) {
 				resultJSON.put("done", false);
 			} else if(recordType == RRType.RRTYPE_NS) {
 				resultJSON.put("done", false);
 				resultJSON.put("lifetime", DDNSNode.REG_LIFETIME);
 			} else { //A or SOA
 				resultJSON.put("done", true);
 				resultJSON.put("lifetime", DDNSNode.REG_LIFETIME);
 			}
 			resultJSON.put("resulttype", "registerresult");
 		} catch(DDNSException e) {
 			resultJSON = ddnsexceptionToJSON(e);
 		} catch(JSONException e) {
 			resultJSON = ddnsexceptionToJSON(new DDNSRuntimeException(e.getMessage()));
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
 	public JSONObject _rpcResolve(JSONObject args) {
 		JSONObject resultJSON = new JSONObject();
 		try {
			DDNSFullNameInterface name = new DDNSFullName(args.getString("name"));
		
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
 						throw new DDNSNoAddressException(name);
 					resultJSON.put("done", false);
 				} else { //A or SOA
 					if(!node.isValid())
 						throw new DDNSNoAddressException(name);
 					resultJSON.put("done", true);
 				}	
 			}
 			resultJSON.put("resulttype", "resolveresult");
 		} catch(DDNSException e) {
 			resultJSON = ddnsexceptionToJSON(e);
 		} catch(JSONException e) {
 			resultJSON = ddnsexceptionToJSON(new DDNSRuntimeException(e.getMessage()));
 		}
 		
 		return resultJSON;
 	}
 
 	private DDNSNode nodeLookup(DDNSFullNameInterface name) throws DDNSException {
 		return nodeLookup(name, false);
 	}
 	
 	private DDNSNode nodeLookup(DDNSFullNameInterface name, boolean suppressNoAddressErrors) throws DDNSException {
 		
 		DDNSFullNameInterface curAncestor = name;
 		DDNSFullNameInterface emptyName = new DDNSFullName("");
 		while(curAncestor != null && !curAncestor.equals(emptyName)) {
 			if(curAncestor.equals(mRoot.getName()))
 				break;
 			curAncestor = curAncestor.parent();
 		}
 		
 		if(curAncestor == null || curAncestor.equals(emptyName))
 			throw new DDNSZoneException(name, mRoot.getName());
 
 		
 		DDNSNode curNode = mRoot;
 		while(curNode != null) {
 			
 			DDNSRRecord curRecord = curNode.getRecord();
 			if(curNode.getName().equals(name) &&
 					(curRecord.type() == RRType.RRTYPE_A ||
 					curRecord.type() == RRType.RRTYPE_SOA)) { //resolved to A or SOA
 				if(!suppressNoAddressErrors && !curNode.isValid()) {
 					throw new DDNSNoAddressException(name);
 				}
 				return curNode;
 
 			} else if(curRecord.type() == RRType.RRTYPE_CNAME) { //reached CNAME
 				return curNode;
 
 			} else if(curRecord.type() == RRType.RRTYPE_NS) { //reached NS
 				if(!suppressNoAddressErrors && !curNode.isValid()) {
 					throw new DDNSNoAddressException(name);
 				}
 				return curNode;
 			}
 			
 			DDNSFullNameInterface childName = name;
 			while(!childName.parent().equals(curNode.getName())) {
 				childName = childName.parent();
 			}
 			curNode = curNode.getChild(childName);
 		}
 
 		throw new DDNSNoSuchNameException(name);
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
 		private DDNSFullNameInterface nFullname;
 		private String nPassword;
 		private DDNSRRecord nRecord;
 		private long nDieAt; //die at this time, time in millis
 		private Map<DDNSFullNameInterface, DDNSNode> nChildren = new HashMap<DDNSFullNameInterface, DDNSNode>();
 
 		public DDNSNode(DDNSFullNameInterface fullname, String pw, DDNSRRecord record) { 
 			nFullname = fullname;
 			nRecord = record;
 			nDieAt =  System.currentTimeMillis() + REG_LIFETIME*1000;
 			nPassword = pw;
 		}
 
 		public void addChild(DDNSNode child) {
 			nChildren.put(child.getName(), child);
 		}
 
 		public DDNSFullNameInterface getName() {
 			return nFullname;
 		}
 
 		public boolean hasChild(DDNSFullNameInterface childName) {
 			return nChildren.containsKey(childName);
 		}
 
 		public DDNSNode getChild(DDNSFullNameInterface childName) {
 			return nChildren.get(childName);
 		}
 
 		public DDNSRRecord getRecord() {
 			if(!this.isValid() && nRecord instanceof ARecord)
 				((ARecord) nRecord).updateAddress(null, -1);
 			return nRecord;
 		}
 
 		public void register(String ip, int port, String pw) throws DDNSException {
 			if(!pw.equals(nPassword)) {
 				throw new DDNSAuthorizationException(nFullname);
 			} else if(nRecord instanceof ARecord) {
 				((ARecord) nRecord).updateAddress(ip, port);
 				nDieAt = System.currentTimeMillis() + REG_LIFETIME*1000;
 			}
 		}
 		
 		public void unregister(String pw) throws DDNSException {
 			this.register(null, -1, pw);
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
 				result.put("Fullname", this.getName().toString());
 				result.put("Password", this.nPassword);
 				result.put("Record", this.getRecord());
 				if(this.nChildren.size() > 0) {
 					JSONArray children = new JSONArray();
 					for(DDNSFullNameInterface child : nChildren.keySet()) {
 						children.put(child.toString());
 					}
 					result.put("Children", children);
 				} else {
 					result.put("Children", "None");
 				}
 				return result.toString();
 			} catch(JSONException e) {
 				e.printStackTrace();
 			}
 			return null;
 		}
 	}
 }
