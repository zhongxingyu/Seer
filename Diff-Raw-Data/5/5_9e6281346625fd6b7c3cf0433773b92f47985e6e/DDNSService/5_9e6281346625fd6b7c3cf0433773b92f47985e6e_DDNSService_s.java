 package edu.uw.cs.cse461.Net.DDNS;
 
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Set;
 import java.util.Stack;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import edu.uw.cs.cse461.HTTP.HTTPProviderInterface;
 import edu.uw.cs.cse461.Net.Base.NetBase;
 import edu.uw.cs.cse461.Net.Base.NetLoadable.NetLoadableService;
 import edu.uw.cs.cse461.Net.DDNS.DDNSException.DDNSAuthorizationException;
 import edu.uw.cs.cse461.Net.DDNS.DDNSException.DDNSNoSuchNameException;
 import edu.uw.cs.cse461.Net.DDNS.DDNSException.DDNSNoAddressException;
 import edu.uw.cs.cse461.Net.DDNS.DDNSException.DDNSRuntimeException;
 import edu.uw.cs.cse461.Net.DDNS.DDNSException.DDNSTTLExpiredException;
 import edu.uw.cs.cse461.Net.DDNS.DDNSException.DDNSZoneException;
 import edu.uw.cs.cse461.Net.DDNS.DDNSRRecord.ARecord;
 import edu.uw.cs.cse461.Net.DDNS.DDNSRRecord.NSRecord;
 import edu.uw.cs.cse461.Net.DDNS.DDNSRRecord.SOARecord;
 import edu.uw.cs.cse461.Net.DDNS.DDNSRRecord.CNAMERecord;
 import edu.uw.cs.cse461.Net.DDNS.DDNSRRecord.RRType;
 import edu.uw.cs.cse461.Net.RPC.RPCCallableMethod;
 import edu.uw.cs.cse461.Net.RPC.RPCService;
 import edu.uw.cs.cse461.util.ConfigManager;
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
 
 	private DDNSNode treeRoot;
 	private int resolvelimit;
 	private int registerTimeout;
 	private Map<DDNSFullName, Long> timers;
 	
 	/**
 	 * Called to end execution.  Specifically, need to terminate any threads we've created.
 	 */
 	@Override
 	public void shutdown() {
 		super.shutdown();
 	}
 	
 	@Override
 	public String httpServe(String[] uriArray) {
 		String ans = "";
 		ans += treeRoot.name.toString() + " " + treeRoot.info.toString() + " with children: \n";
 		Set<DDNSFullName> children = treeRoot.children.keySet();
 		Stack<DDNSNode> nodes = new Stack<DDNSNode>();
 		for (DDNSFullName name : children) {
 			try {
 				nodes.push(treeRoot.getChild(name));
 			} catch (DDNSNoSuchNameException e) {
 				Log.e(TAG, "This error should never be reached, obviously your method of getting children sucks");
 			}
 		}
 		// Depth first traversal of the entire structure
 		while (!nodes.isEmpty()) {
 			DDNSNode current = nodes.pop();
 			ans += current.name.toString() + " child of " + current.name.parent() + " has data " + current.info.toString() + "\n";
 			children = current.children.keySet();
 			for (DDNSFullName name : children) {
 				try {
 					nodes.push(current.getChild(name));
 				} catch (DDNSNoSuchNameException e) {
 					Log.e(TAG, "Again, this error should never be reached");
 				}
 			}
 		}
 		return ans;
 	}
 	
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
 			
 		} catch (Exception e) {
 			String msg = "DDNSService constructor caught exception: " + e.getMessage();
 			Log.e(TAG, msg);
 			e.printStackTrace();
 			throw new DDNSRuntimeException(msg);
 		}
 		
 		ConfigManager config = NetBase.theNetBase().config();
 		try {
 			// Gets the config resolve limits and timeout for registers
 			resolvelimit = config.getAsInt("ddns.resolvettl");
			registerTimeout = 1000*config.getAsInt("ddnsresolver.cachettl"); // convert to milliseconds
 		} catch (NoSuchFieldException e) {
 			resolvelimit = 1000; // arbitrary default value
 			registerTimeout = 1000; // arbitrary default value
 		}
 		timers = new HashMap<DDNSFullName, Long>(); // Creates a map of names to timers for easy reaccess.
 		
 		// Set up the tree that this Service is responsible for.
 		String[] nodesList = config.getAsStringVec("ddns.nodes");
 		if (nodesList == null) {
 			throw new DDNSRuntimeException("No nodes are present, please resolve this issue and restart");
 		}
 		for (int i = 0; i < nodesList.length; i++) {
 			String[] args = nodesList[i].split(":");
 			DDNSNode node = null;
 			if (args.length != 3 && args.length != 4) {
 				throw new DDNSRuntimeException("Entry: " + nodesList[i] + " had incorrect length of " + args.length);
 			}
 			DDNSFullName name = new DDNSFullName(args[1]);
 			
 			if (args.length == 3) {
 				if (args[0].equals("a")) {
 					DDNSRRecord cur = new ARecord(); // because shouldn't originally have port
 					node = new DDNSNode(cur, args[2], name);
 				} else if (args[0].equals("ns")) {
 					DDNSRRecord cur = new NSRecord();
 					node = new DDNSNode(cur, args[2], name);
 				} else if (args[0].equals("soa")) {
 					if (treeRoot != null) {
 						throw new DDNSRuntimeException("Config attempted to create multiple SOAs in this namespace");
 					}
 					DDNSRRecord cur = new SOARecord();				
 					treeRoot = new DDNSNode(cur, args[2], name);
 				} else {
 					throw new DDNSRuntimeException("Entry: " + args[0] + " did not match any node type");
 				}
 			} else if (args.length == 4 && args[0].equals("cname")) {
 				DDNSRRecord cur = new CNAMERecord(args[2]);
 				node = new DDNSNode(cur, args[3], name);
 			} else {
 				throw new DDNSRuntimeException("Length 4 arg was not a cname");
 			}
 			// traverse the tree to find where this current node should be added.  Node will be null if we just created the root of the tree
 			if (node != null) {
 				if (treeRoot == null) {
 					throw new DDNSRuntimeException("Attempted to create a node before the first SOA node");
 				}			
 				if (name.equals(treeRoot.name)) {
 					Log.w(TAG, "Duplicate entries found for the root, ignoring second.");
 				} else {
 					if (name.isChildOf(treeRoot.name)) {
 						treeRoot.addChild(name, node);
 					} else if (name.isDescendantOf(treeRoot.name)) {
 						DDNSNode cur = treeRoot.getChild(treeRoot.name.nextAncestor(name)); 
 							// I think this gets the next generation to check
 						while (!cur.name.equals(name)) {
 							if (!name.isDescendantOf(cur.name)) {
 								Log.w(TAG, "should only end up in here if we try to access a ");
 								throw new DDNSNoSuchNameException(name);
 							}
 							DDNSFullName next = cur.name.nextAncestor(name);
 							if (next.equals(name)) {
 								if (!cur.children.containsKey(next)) {
 									cur.addChild(name, node);
 								} else {
 									Log.w(TAG, "Duplicate entries found for " + name + ", ignoring second");
 								}
 							}
 							cur = cur.getChild(next);						
 						}
 					} else {
 						throw new DDNSNoSuchNameException(name);
 					}					
 				}			
 			}
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
 	// Synchronized to prevent race conditions in register/unregister
 	public synchronized JSONObject _rpcUnregister(JSONObject args) {
 		try {
 			DDNSFullName name = new DDNSFullName(args.getString("name"));
 			JSONObject node = new JSONObject();
 			JSONObject result = new JSONObject();
 			result.put("resulttype", "unregisterresult");
 			DDNSNode rec;
 			try {
 				rec = findName(name);
 			} catch (DDNSException e) {
 				node = exceptionTranslation(e);
 				return node;
 			}
 			try {
 				synchronized(rec) { // ought to prevent race conditions between unregister and resolve
 					rec.updatePortIP(args.getString("password"), -1, null);
 					// Changes the port/ip to the null values
 					RRType type = rec.info.type();
 					if (type.equals(RRType.RRTYPE_CNAME)) {
 						CNAMERecord cname = (CNAMERecord)rec.info;
 						node.put("type", "CNAME");
 						node.put("alias", cname.alias());
 						result.put("node", node);
 						node.put("name", rec.name);
 						result.put("done", false);
 					} else if (type.equals(RRType.RRTYPE_NS)) {
 						NSRecord ns = (NSRecord)rec.info;
 						node.put("type", "NS");
 						node.put("ip", ns.ip());
 						node.put("port", ns.port());
 						node.put("name", rec.name);
 						result.put("done", false);
 						timers.remove(name); // removes the entry from the map of names to their last registration time, as we don't need that info
 					} else {
 						result.put("done", true);
 						timers.remove(name); // removes the entry from the map of names to their last registration time, as we don't need that info
 					}
 					return result;
 				}
 			} catch (DDNSException e) {
 				node = exceptionTranslation(e);
 				return node;
 			}
 		} catch (JSONException e) {
 			e.printStackTrace();
 			return null;
 		}
 	}
 	
 	/**
 	*   register( {name: <string>, password: <string>, ip: <string>,  port: <int>} ) => { DDNSNode } or errormsg
 	*<p>
 	* We accept only requests for names stored on this server.
 	* 
 	* @param args
 	* @return
 	*/
 	// Synchronized to prevent race conditions between register and unregister
 	public synchronized JSONObject _rpcRegister(JSONObject args) {
 		try {
 			Log.d(TAG, "rpc register called with args "+args);
 			DDNSFullName name = new DDNSFullName(args.getString("name"));
 			JSONObject node = new JSONObject();
 			JSONObject result = new JSONObject();
 			result.put("resulttype", "registerresult");
 			// result is what we will return if we actually obtain a result instead of an exception
 			// node is what we will return if a ddnsexception is generated, or the node wrapper
 			// for the result we obtain.
 			DDNSNode rec;
 			try {
 				rec = findName(name);
 			} catch (DDNSException e) {
 				node = exceptionTranslation(e);
 				return node;
 			}
 			synchronized(rec) { // Ought to prevent race conditions between register and resolve
 				RRType type = rec.info.type();
 				if (!type.equals(RRType.RRTYPE_CNAME)) {
 					if (timers.containsKey(rec.name)) {
 						timers.put(rec.name, System.currentTimeMillis());
 						//timers.get(rec.name).restart(); // Does this to try and prevent the timer running out just before we synchronize things
 					} else {
 						timers.put(rec.name, System.currentTimeMillis());
 					}
 					try {
 						rec.updatePortIP(args.getString("password"), args.getInt("port"), args.getString("ip"));
 						node.put("name", rec.name.toString());
 						node.put("ip", args.getString("ip"));
 						node.put("port", args.getInt("port"));
 						// Does all the set up things for each individual type, similar to resolve seen below
 						if (type.equals(RRType.RRTYPE_A)) {
 							node.put("type", "A");
 							result.put("done", true);
 						} else if (type.equals(RRType.RRTYPE_SOA)) {
 							node.put("type", "SOA");
 							result.put("done", true);
 						} else {
 							node.put("type", "NS");
 							result.put("done", false);
 						}
 						result.put("node", node);
 					
 						timers.put(rec.name, System.currentTimeMillis()); 
 						// alters the timestamp so that the lifetime they get is as close to accurate as possible
 					} catch (DDNSException e) {
 						node = exceptionTranslation(e);
 						return node;
 					}			
 				} else {
 					CNAMERecord cname = (CNAMERecord)rec.info;
 					node.put("type", "CNAME");
 					node.put("alias", cname.alias().toString());
 					result.put("node", node);
 					result.put("done", false);
 				}
 						
 				result.put("lifetime", registerTimeout); // the lifetime will always be the same
 				return result;
 			}
 		} catch (JSONException e) {
 			Log.e(TAG, "A JSONException occurred during resolution");
 			return null;
 		} 
 	}
 	
 	// Traverses the tree to find the specified name if it exists, or the first CNAME/NS node it encounters.  If it is nowhere in the tree or 
 	// if we run out of time, throw an exception.
 	private DDNSNode findName(DDNSFullName name) throws DDNSZoneException, DDNSNoSuchNameException, DDNSTTLExpiredException {
 		if (!name.isDescendantOf(treeRoot.name)) {
 			throw new DDNSZoneException(name, treeRoot.name);
 		}
 		if (name.equals(treeRoot.name)) {
 			return treeRoot;
 		} else {
 			int recursionCount = 2;
 			DDNSNode current = treeRoot.getChild(treeRoot.name.nextAncestor(name));
 			RRType curType = current.info.type();
 			while (!current.name.equals(name) && recursionCount < resolvelimit) {
 				// Will short circuit naturally if we encounter an NS or CNAME node first, or if the path
 				// does not actually exist.
 				if (curType.equals(RRType.RRTYPE_NS) || curType.equals(RRType.RRTYPE_CNAME)) {
 					// We can't do anything more with it if it is an NS/CNAME, so just return it and let someone else handle it
 					return current;
 				}
 				// Continues down the path if it exists
 				current = current.getChild(current.name.nextAncestor(name));
 				curType = current.info.type();
 				recursionCount++;
 			}
 			// We looped through too many times so the name may or may not exist but it has taken too long to find out
 			if (recursionCount == resolvelimit) {
 				throw new DDNSTTLExpiredException(name);
 			}
 			// We've exited the while loop without timing out, which means we've found the node we were looking for
 			return current;
 		}
 	}
 	
 	/**
 	 * This version is invoked via RPC.  It's simply a wrapper that extracts the call arguments
 	 * and invokes resolve(host).
 	 * @param callArgs
 	 * @return
 	 */
 	public JSONObject _rpcResolve(JSONObject args) {
 		try {
 			DDNSFullName name = new DDNSFullName(args.getString("name"));
 			JSONObject node = new JSONObject();
 			JSONObject result = new JSONObject();
 			result.put("resulttype", "resolveresult");
 			// result is what we will return if we actually obtain a result instead of an exception
 			// node is what we will return if a ddnsexception is generated, or the node wrapper
 			// for the result we obtain.
 			DDNSNode rec;
 			try {
 				rec = findName(name);
 			} catch (DDNSException e) {
 				node = exceptionTranslation(e);
 				return node;
 			}
 			// No errors occurred so handle each type of node after storing the name (which won't change between node types)
 			node.put("name", rec.name.toString());
 			
 			if (rec.info.type().equals(RRType.RRTYPE_CNAME)) {
 				// We have reached a CNAME so we don't know how to proceed and will leave that up to DDNSResolver
 				CNAMERecord cname = (CNAMERecord)rec.info;
 				node.put("type", "CNAME");
 				node.put("alias", cname.alias().toString());
 				result.put("node", node);
 				result.put("done", false);
 			} else {
 				// All other types have a port and ip associated with them and are subclasses of A, so we only need to cast to A
 				ARecord intermed = (ARecord)rec.info;
 				synchronized(rec) { 
 					// ought to prevent some race conditions between looking at a node's timestamp and that node becoming unregistered
 					// although it will not be possible to prevent all (case where unregister occurs shortly after resolve has returned, etc.)
					if (!timers.containsKey(rec.name) || timers.get(rec.name) + registerTimeout < System.currentTimeMillis()) {
 						// If there is no registration time stamp or the timestamp is out of date, then we have found a node 
 						// without a recently updated address and we should pass this on.
 						node = exceptionTranslation(new DDNSNoAddressException(name));
 						return node;
 					}
 					node.put("ip", intermed.ip());
 					node.put("port", intermed.port());
 				}
 				
 				RRType type = intermed.type();
 				// And all other variation can be determined through merely the type itself without having to cast
 				if (type.equals(RRType.RRTYPE_A)) {
 					node.put("type", "A");
 					result.put("done", true);
 				} else if (type.equals(RRType.RRTYPE_SOA)) {
 					node.put("type", "SOA");
 					result.put("done", true);
 				} else {
 					node.put("type", "NS");
 					result.put("done", false);
 				}
 				result.put("node", node);
 			}
 			return result;
 		} catch (JSONException e) {
 			Log.e(TAG, "A JSONException occurred during resolution");
 			return null;
 		} 		
 	}
 	
 	// RPC callable routines
 	//---------------------------------------------------------------------------
 
 	@Override
 	public String dumpState() {
 		return "whatever you'd like";
 	}
 
 	// Private node class for storing the information in this namespace.
 	private class DDNSNode {
 		private String password;
 		private DDNSFullName name;
 		private Map<DDNSFullName, DDNSNode> children;
 		private DDNSRRecord info;
 		
 		public DDNSNode(DDNSRRecord data, String pwd, DDNSFullName name) {
 			children = new HashMap<DDNSFullName, DDNSNode>();
 			info = data;
 			password = pwd;
 			this.name = name;
 		}
 		
 		// Makes sure I don't set a child wrong
 		public void addChild(DDNSFullName name, DDNSNode child) throws DDNSRuntimeException {
 			RRType type = info.type();
 			if (name.isChildOf(this.name) && !type.equals(RRType.RRTYPE_NS) && !type.equals(RRType.RRTYPE_CNAME)) {
 				children.put(name, child);
 			} else {
 				throw new DDNSRuntimeException("Attempted to set a node as child where it cannot exist" + child.name);
 			}
 		}
 		
 		// Probably don't want use the Node class for password check?
 		public void updatePortIP (String pass, int port, String IP) throws DDNSAuthorizationException {
 			if (!pass.equals(password)) {
 				throw new DDNSAuthorizationException(name);
 			} else {
 				// Race conditions should be handled in other methods, so I shouldn't have to worry about it.
 				RRType type = info.type();
 				if (type != RRType.RRTYPE_CNAME) {
 					ARecord me = (ARecord) info;
 					me.updateAddress(IP, port);
 				} 
 			}
 		}
 		
 		public DDNSNode getChild(DDNSFullName name) throws DDNSNoSuchNameException{
 			if (children.containsKey(name)) {
 				return children.get(name);
 			} else {
 				throw new DDNSNoSuchNameException(name);
 			}
 		}
 	}
 	
 	private JSONObject exceptionTranslation (DDNSException e) throws JSONException {
 		JSONObject result = new JSONObject();
 		result.put("resulttype", "ddnsexception");
 		int exceptionnum = e.errorCode.toInt();
 		result.put("exceptionnum", exceptionnum);
 		result.put("message", e.getMessage());
 		if (exceptionnum != 3) { 
 			// The exception is not a DDNSRuntimeException
 			result.put("name", e.args.get(0));
 			if (exceptionnum == 6) {
 				// The exception is a DDNSZoneException
 				result.put("zone", e.args.get(1));
 			}
 		}
 		return result;
 	}
 }
