 package chat;
 import org.red5.server.adapter.ApplicationAdapter;
 import org.red5.server.api.IConnection;
 import org.red5.server.api.IClient;
 import org.red5.server.api.IScope;
 import org.red5.server.api.Red5;
 import org.red5.server.api.service.IServiceCapableConnection;
 import org.red5.server.api.so.ISharedObject;
 import org.red5.server.api.ScopeUtils;
 import org.red5.server.api.so.ISharedObjectService;
 import org.red5.server.api.*;
 import org.red5.server.api.scheduling.*;
import org.red5.components.ClientManager;
 import org.red5.server.api.service.*;
 
 //
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import java.util.*;
 
 public class Application extends ApplicationAdapter{	
 	public HashMap<String,User> users = new HashMap<String,User>();
 	public class User {
 		public String id = null;
 		public String pseudo = null;
 		public String webcam = null;
 		public String role = null;
 		public String sex = null;
 		public String onlineStatus = null;
 		public String room=null;
 		public String world=null;
 		public String isWatching=null;
 		public String hasWebcam=null;
 
 		public User (String id,String  pseudo,String webcam, String role,String sex,String onlineStatus,String room,String world,String isWatching,String hasWebcam) {
 		      this.id = id;
 		      this.pseudo = pseudo;
 		      this.webcam = webcam;
 		      this.role = role;
 		      this.sex = sex;
 		      this.onlineStatus = "true";	
 		      this.room = room;	
 		      this.world = world;
 		      this.isWatching = "false";	
 		      this.world = world;		      
 		   }
 		} 
 	
 	   
 	
 	
 	// ici le callback ! ! !
 	/*if (conn instanceof IServiceCapableConnection) {
 	    IServiceCapableConnection sc = (IServiceCapableConnection) conn;
 	    sc.invoke("the_method", new Object[]{"One", 1}, new MyCallback());
 	}
 	sc.invoke("echo", new Object[]{"Hello world!"},
 			 new IPendingServiceCallback() {
 			 	public void resultReceived(IPendingServiceCall call) {
 			 	System.err.println("Received: " + call.getResult());
 			 	}
 			  });
 	*/
 
 	
 	class MyCallback implements IPendingServiceCallback {
 	    public void resultReceived(IPendingServiceCall call) { 
 	        // Do something with "call.getResult()"
 	    }
 	}
 	//private ClientManager2 clientMgr = new ClientManager2("users_so","online",true);
 	private ClientManager clientMgr = new ClientManager("users_so",true);	
 	private static final Log log = LogFactory.getLog( Application.class );
 
 	//ISharedObject users_so;
 	//= getSharedObject(scope);
 
 	//ArrayList listeUsers = new ArrayList();
 	//ArrayList listeObjectUsers = new ArrayList();	
 	
 	/** Manager for the clients. */
 
 	public void disconnectUser(String connectionID)
       {   //code to inform other clients that this client with this connection ID has left below...
       }
 	/*
 	 * (non-Javadoc)
 	 * @see org.red5.server.adapter.ApplicationAdapter#connect(org.red5.server.api.IConnection, org.red5.server.api.IScope, java.lang.Object[])
 	 * connect an users with 7 parameters: username,webcam,onlineStatus, role, room, world 
 	 */
 	public boolean connect(IConnection conn, IScope scope, Object[] params) {
 		log.info("connect IConnection nombre params="+ params.length);
 		// Check if the user passed valid parameters.
 		//params = 	 pseudo onlineStatus role sexe  room world
 		if (params.length==1) {			
 			log.info("getNumberUsersConnected");
 			((IServiceCapableConnection) conn).invoke("getNumberUsersConnected", new Object[]{users.size()});
 			//rejectClient("getNumberUsersConnected !.");
 			conn.close();
 			return false;
 		}
 		if (params == null || params.length != 7) {
 			log.info("Client must pass 7 params !");
 			rejectClient("Client must pass 7 params !.");
 			return false;
 		}
 		String id = conn.getClient().getId();
 		String pseudo = params[0].toString();
 		String webcam = params[1].toString();
 		String onlineStatus = params[2].toString();
 		String role=params[3].toString();
 		String sex = params[4].toString();
 		String room = params[5].toString();
 		String world=params[6].toString();
 		String isWatching="false";
 		String hasWebcam="true";
 //		 test if username is already used
 		if (users.containsKey(pseudo)) {		     
 		    	  rejectClient("Error: pseudo "+pseudo+" already used.");
 		    	  return false;
 		      }   		
 
 		// Call original method of parent class.
 		if (!super.connect(conn, scope, params)) {
 			return false;
 		}		
 		log.info("ici2");
         users.put(pseudo,new User(id,pseudo,webcam, role,sex,onlineStatus,room,world,isWatching,hasWebcam));
         log.info("ici3");
         log.info("connected as :"+pseudo+"webcam="+webcam+" status="+onlineStatus+"role="+role+"sexe="+sex+"room="+room+"world="+world);
 		clientMgr.addClient(scope, pseudo, id);
 		ServiceUtils.invokeOnAllConnections (scope, "joinuser", new Object[] {id,pseudo,webcam, onlineStatus,role,sex,room,world} );
 		return true;
 	}
 	/*
 	 * (non-Javadoc)
 	 * @see org.red5.server.adapter.ApplicationAdapter#disconnect(org.red5.server.api.IConnection, org.red5.server.api.IScope)
 	 * disconnect an user form the chat and notify all others users 
 	 */
 	public void disconnect(IConnection conn, IScope scope) {       
 		// Get the previously stored username.
 		String id = conn.getClient().getId();			
 		// Unregister user.
 		String pseudo = clientMgr.removeClient(scope, id);
 		User user=users.get(pseudo);
 		users.remove(pseudo);
 		log.info("removeuser invoked id:"+user.id+" pseudo="+user.pseudo+"sexe="+user.sex);
 		ServiceUtils.invokeOnAllConnections (scope, "removeuser", new Object[] {user.id,user.pseudo,user.room,user.world} );
 		super.disconnect(conn, scope);
 		
 	}	
 	public boolean appJoin(IClient client, IScope scope) {
 		log.info("Client joined app " + client.getId());
 		// If you need the connecion object you can access it via.
 		//IConnection conn = Red5.getConnectionLocal();
 		return true;
 	}
 	public void send_to_all_iterate() {
 		IConnection current = Red5.getConnectionLocal();
		Iterator<IConnection> it = (Iterator<IConnection>) scope.getConnections();
 		
 		while (it.hasNext()) {
 		IConnection conn = it.next();
 		if (conn.equals(current)) continue;
 			if (conn instanceof IServiceCapableConnection) {
 				((IServiceCapableConnection) conn).invoke("someClientMethod", new Object[]{"Hello World"});
 				log.debug("sending notification to "+conn);
 			}	
 		}
 	}	
 	/*
 	 * get all the already connected users
 	 */
 	public HashMap<String,User> getUserList() {
 		/*IConnection conn = Red5.getConnectionLocal();		
 		if (conn instanceof IServiceCapableConnection) {
 			((IServiceCapableConnection) conn).invoke("receive_private", new Object[]{"zaza"});
 			log.info("getUserList"+conn.getClient().getId());
 		}*/
 		//listeObjectUsers.toString()
 		log.info("getUserList called");		
 		return users;		
 	}	
 	/*
 	 * send a public message msg from from_pseudo to ALL users
 	 */
 	public void send_public(String from_pseudo, String msg) {
 		//Iterator<IConnection> it = scope.getConnections();
 		log.info("send_public from_pseudo"+from_pseudo+" msg:"+msg);
 		ServiceUtils.invokeOnAllConnections (scope, "receivePublicMsg", new Object[] {from_pseudo,msg} );
 	}
 	/*
 	 * start Talk invoked on all users by whoTalks
 	 */
 	public void startTalk(String whoTalks) {
 		log.info("startTalk: "+whoTalks);
 		ServiceUtils.invokeOnAllConnections (scope, "talkStarted", new Object[] {whoTalks} );
 	}
 	/*
 	 * stop Talk invoked on all users by whoTalks
 	 */
 	public void stopTalk(String whoTalks) {
 		log.info("stopTalk: "+whoTalks);
 		ServiceUtils.invokeOnAllConnections (scope, "talkEnded", new Object[] {whoTalks} );
 	}
 	/*
 	 * gett the number of connected users to the chat
 	 */
 	public int getNumberUsersConnected(){
 		return users.size();
 
 	}
 	/*
 	 * start requestHand invoked on all users by whoRequests
 	 */
 	public void requestHand(String whoRequests) {
 		log.info("send_public from_pseudo"+whoRequests);
 		ServiceUtils.invokeOnAllConnections (scope, "handRequested", new Object[] {whoRequests} );
 	}
 	
 	/*
 	 * send a privateMessage msg from fromPseudo to sendToId user
 	 */
 	public void send_private_id(String sendToId,String fromPseudo, String msg) {
 		//IConnection current = Red5.getConnectionLocal();
		Iterator<IConnection> it = (Iterator<IConnection>) scope.getConnections();
 		log.debug("send_private to "+sendToId+" "+msg);
 		//String uid = scope.getClient().getId();
 		while (it.hasNext()) {
 		IConnection conn = it.next();
 		String id=conn.getClient().getId();
 		if (!(sendToId.equals(id))) continue;
 		log.info("receive_private "+sendToId+" "+msg);
 			if (conn instanceof IServiceCapableConnection) {
 				((IServiceCapableConnection) conn).invoke("receive_private", new Object[]{msg});
 				log.info("received_private "+sendToId+" "+msg);
 				return;
 			}	
 		}
 	}	
 	/*
 	 * who is wathing who function
 	 */
 	public void watches(String UserId) {
         IConnection conn2 = Red5.getConnectionLocal();
 	    String uid = conn2.getClient().getId();
 	    
		Iterator<IConnection> it = (Iterator<IConnection>) scope.getConnections();
 		log.debug("watches "+UserId);
 		while (it.hasNext()) {
 		IConnection conn = it.next();
 		String id=conn.getClient().getId();
 	
 		if (!(UserId.equals(id))) continue;
 			if (conn instanceof IServiceCapableConnection) {
 				((IServiceCapableConnection) conn).invoke("watches", new Object[]{uid});
 				log.info(uid+" is watching "+UserId);
 				return;
 			}	
 		}
 	}	
 	/*
 	 * ban the user _id
 	 */
 	public void ban(String _id) {
		Iterator<IConnection> it = (Iterator<IConnection>) scope.getConnections();
 		//log.debug("ban called: "+_id);
 		while (it.hasNext()) {
 		IConnection conn = it.next();
 		String id=conn.getClient().getId();
 		if (!(_id.equals(id))) continue;
 			if (conn instanceof IServiceCapableConnection) {
 				((IServiceCapableConnection) conn).invoke("IhaveBeenBanned", new Object[]{_id});
 				log.info("ban "+_id);
 			}	
 		}
 	}			
 	/*
 	 * kick the user _id
 	 */
 	public void kick(String _id) {
		Iterator<IConnection> it = (Iterator<IConnection>) scope.getConnections();
 		//log.debug("kick called: "+_id);
 		while (it.hasNext()) {
 		IConnection conn = it.next();
 		String id=conn.getClient().getId();
 		if (!(_id.equals(id))) continue;
 			if (conn instanceof IServiceCapableConnection) {
 				((IServiceCapableConnection) conn).invoke("IhaveBeenKicked", new Object[]{_id});
 				log.info("kick "+_id);
 			}	
 		}
 	}	
 	/*
 	 * send a private message msg from user fromPseudo to detsination user DestinationID
 	 */
 	public void send_private(String fromPseudo, String DestinationID,String msg) {
 		//IConnection current = Red5.getConnectionLocal();
		Iterator<IConnection> it = (Iterator<IConnection>) scope.getConnections();
 		log.debug("send_private to "+DestinationID+" "+msg);
 		//String uid = scope.getClient().getId();
 		while (it.hasNext()) {
 		IConnection conn = it.next();
 		String id=conn.getClient().getId();
 		log.debug("id="+id+ " senTO="+DestinationID);
 		//if (sendTo.equals(id)) log.info("PAREIL"); else log.info("differents");
 		
 		if (!(DestinationID.equals(id))) continue;
 		log.info("receive_private "+DestinationID+" "+msg);
 			if (conn instanceof IServiceCapableConnection) {
 				((IServiceCapableConnection) conn).invoke("receivePrivateMsg", new Object[]{fromPseudo, msg});
 				log.info("received_private "+DestinationID+" "+msg);
 			}	
 		}
 	}	
 	/*
 	 * change the status of the webcam: can be "on" or "off"
 	 */
 	public void change_webcam(String webcam) {
 		//
         IScope appScope = Red5.getConnectionLocal().getScope();
         IConnection conn = Red5.getConnectionLocal();
 	    String uid = conn.getClient().getId();
 	    log.debug("change_webcam called form id:"+uid+" :"+webcam);
         ServiceUtils.invokeOnAllConnections (appScope, "change_webcam", new Object[]{uid,webcam} );
 	}
 	/*
 	 * change the status of the user to "status"
 	 */
 	//
 	public void changeProfil(String webcam,String role, String onlineStatus) {
 		//
         IScope appScope = Red5.getConnectionLocal().getScope();
         IConnection conn = Red5.getConnectionLocal();
 	    String _id = conn.getClient().getId();
 	    log.debug("changeProfil called form id:"+_id+" :"+webcam);
         ServiceUtils.invokeOnAllConnections (appScope, "changeProfil", new Object[]{_id,webcam,role,onlineStatus} );
        // return (new Object[]{uid,status});
 	}
 
 	public Double add(Double a, Double b){
         return a + b;
     }
 	public Boolean appStart() {
 		//users_so = SharedObject.get("users_so", false);
 	     createSharedObject(Red5.getConnectionLocal().getScope(), "users_so", false);
 	     log.debug("application START!");
 		return true;
 		
 	}
 	public boolean roomStart(IScope room) {
 		log.info( "Red5First.room start " + room);
 	      if (!super.roomStart(room))
 	          return false;	      
 	      //createSharedObject(room, "sampleSO", true);
 	      //ISharedObject so = getSharedObject(room, "sampleSO");
 	      // Now you could do something with the shared object...
 	    
 	      return true;            
 	  }	
 	public String whoami() {
 	    IConnection conn = Red5.getConnectionLocal();
 	    IClient client = conn.getClient();
 	    IScope scope = conn.getScope();
 	    return client.getId();
 	    // ...
 	}
 	public void callclient() {
 		log.info("callclient called");
 		IConnection conn = Red5.getConnectionLocal();
 		if (conn instanceof IServiceCapableConnection) {
 		    IServiceCapableConnection sc = (IServiceCapableConnection) conn;
 		    log.info("flashmethod called");
 		    sc.invoke("flashmethod", new Object[]{"One", 1});
 		}
 	}
 	public boolean appConnect( IConnection conn , Object[] params )
 	{	
 		//String id=conn.getClient().getId();
 		//String username=(String)params[1];
 		//String password=(String)params[2];
 		//String sexe=(String)params[3];
 		//String status=(String)params[4];
		// cherche si user d�j� dans la liste 
 		//Cuser user=new Cuser(id,username,password,sexe,status);
 		/*if (this.list[UserName]) {
			application.rejectConnection(newClient, {msg:"Nom d�j� utilis�, essayez de nouveau."});
 			trace("### name taken");
 			return;
 		} 
 		*/
 		
 		
 	    log.info( "appConnect " + conn.getClient().getId() );
 	    //boolean accept = (Boolean)params[0];
 	    //String user=(String)params[1];
 	   // if ( !accept ) rejectClient( "you passed false..." );
 	    return true;
 	}
 	public void appDisconnect( IConnection conn){	    
 		log.info( "Red5First.appDisconnect " + conn.getClient().getId() );
        // IScope appScope = Red5.getConnectionLocal().getScope();
        // ServiceUtils.invokeOnAllConnections (appScope, "removeuser", new Object[] { "zaza" } );
 		
 	}	
 	public void appStop() {
 		log.info( "Red5First.appStop" );
 
 	}
 }
