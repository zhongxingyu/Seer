 package app;
 
 import org.neo4j.graphdb.*;
 import org.neo4j.graphdb.GraphDatabaseService;
 import asg.cliche.Command;
 import asg.cliche.ShellFactory;
 import asg.cliche.InputConverter;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Random;
 import java.util.Iterator;
 import java.util.Queue;
 import java.util.LinkedList;
 import org.neo4j.kernel.Traversal;
 import org.neo4j.graphdb.traversal.*;
 import org.neo4j.graphdb.*;
 
 public class Cli {
 
 	private HashMap<String, Session> session_table = new HashMap<String, Session>();
 
 	private class Result {
 		boolean success = false;
 		Session session = null;
 		String payload = "";
 
 		public Result( boolean ans, String reason ) {
 			success = ans;
 			payload = reason;
 		}
 
 		public Result( boolean ans, Session s ) {
 			success = ans;
 			session = s;
 		}
 
 		public String toString() {
 			if( success )
 				return "success:" + payload;
 			else
 				return "failure:" + payload;
 		}
 	}
 
 	private Result validateSession( String session_id ) {
 		if( !session_table.containsKey( session_id ) )
 			return new Result(false, "Invalid Session");
 
 		Session s = session_table.get(session_id);
 		if( !s.isValid() ) {
 			session_table.remove(session_id);
 			return new Result(false, "Expired Session");
 		}
 
 		return new Result(true, s);
 	}
 
 	@Command
 	public Result login( String email, String pass ) {
 		Session session = Session.createFromLogin(email, pass);
 		if( session == null )
 			return new Result(false, "Invalid Login");
 
 		boolean did = false;
 		String session_id = "";
 		for( int i=0; i < 20; i++ ) {
 			session_id = randomString(4);
 			if( session_table.containsKey( session_id ) ) {
 				continue;
 			}
 
 			session_table.put( session_id, session );
 			did = true;
 		}
 
 		if( !did )
 			return new Result(false, "Couldn't generate unique Session.id");
 
 		return new Result(true, "Session:"+session_id);
 	}
 
 	@Command
 	public Result logout( String session_id ) {
 		Result res = validateSession( session_id );
 		if( !res.success )
 			return res;
 
 		session_table.remove(session_id);
 
 		return res;
 	}
 
 	@Command
 	public Result signup( String email ) {
 		Email e = null;
 		try {
 			//check if email exists
 			e = new Email(Entity.findExistingNode(LabelDef.EMAIL, Email.EMAIL_KEY, email));
 		}catch(IllegalStateException ise) {
 			//email doesn't exist, create and return claimtoken
 			e = new Email(email);
 			return (new Result(true, e.getClaimToken().toString()));
 		}
 
 		//check if email has been registered
 		if(e.getClaimToken() == null)
 			return (new Result(false, "email claimed"));
 		else
 			return (new Result(true, e.getClaimToken().toString()));
 	}
 
 	@Command
 	public Result register( String ct, String name, String pass, String passVer ) {
 		GraphDatabaseService graphDB = GraphDatabase.get();
 		try(Transaction tx = graphDB.beginTx()) {
 			//check if passwords match
 			if(!pass.equals(passVer))
 				return (new Result(false, "passwords do not match"));
 
 			Email e = null;
 			try {
 				//find email by claimtoken
 				e = new Email(Entity.findExistingNode(LabelDef.EMAIL, Email.CLAIM_TOKEN, ct));
 			}catch(IllegalStateException ise) {
 				//no email found, bad claimtoken
 				return (new Result(false, "bad claimtoken"));
 			}
 
 			//create or find user and set password and email
 			User u = new User();
 			u.setPassword(pass);
 			u.addEmail(e);
 
 			//clear claimToken(should do this on its own!!)
 			e.clearClaimToken();
 
 			tx.success();
 			return (new Result(true, "registration complete"));
 		}
 	}
 
 	@Command
 	public RecoveryToken recoverPassword( String email ) {
 		return null;
 	}
 
 	@Command
 	public Result registerEmail( String session_id, String address) {
 		Result res = validateSession( session_id );
 		if( !res.success )
 			return res;
 
 		Email email = new Email(address);
 
 		return new Result(true, "Email Claim Token - " + email.getClaimToken());
 	}
 
 	@Command
 	public Result addEmail( String session_id, String address, String ct) {
 		Result res = validateSession( session_id );
 		if( !res.success )
 			return res;
 
 		Email email = new Email(address);
 
 		if( !(email.getClaimToken().equals(ct)) )
 			return new Result(false, "Invalid Email Claim Token");
 
 		Session session = res.session;
 		session.user.addEmail(email);
 
 		return new Result(true, "Email Added");
 	}
 
 	@Command
 	public Result deleteEmail( String session_id, String email ) {
 		Result res = validateSession( session_id );
 		if( !res.success )
 			return res;
 
 		Session session = res.session;
 		session.user.removeEmail(new Email(email));
 
 		return new Result(true, "Email Removed");
 	}
 
 	@Command
	public Result addToPortfolio( String session_id, String description, String resource ) {
 		Result res = validateSession( session_id );
 		if( !res.success )
 			return res;
 
 		Citation c = new Citation(description, resource);
 
 		Session session = res.session;
 		session.user.addToPortfolio(c);
 
 		return new Result(true, "Citation Added");
 	}
 
 	@Command
	public Result removeFromPortfolio( String session_id, String cit ) {
 		try(Transaction tx = GraphDatabase.get().beginTx()) {
 			Result res = validateSession( session_id );
 			if( !res.success ){
 
 				return res;
 			}
 			Citation c = new Citation(new Token(cit));
 			c.delete();
 			tx.success();
 			return null;
 		}
 	}
 
 	@Command
 	public Result viewPortfolio( String email ) {
 		//find Email for Email. call email.getUser() to get User.
 		//then call user.viewPortfolio which returns an iterator
 		//over the citations, all of which i want to print
 		try(Transaction tx = GraphDatabase.get().beginTx()) {
 			Email e = new Email(email);
 			if (e.getUser() == null) {
 				//not found
 				return new Result(false, "Invalid Email no profile associated");
 			}
 			User user = e.getUser();
 			Iterator<Citation> iterator = user.viewPortfolio().iterator();
 			StringBuilder output = new StringBuilder();
 			while (iterator.hasNext()) {
 				Citation c = iterator.next();
 				output.append(c.toString());
 			}
 
 
 			tx.success();
 			return new Result(true , output.toString());
 		}
 	}
 
 	@Command					//shouldn't this have an entity arg?
 		public Result trust( String session_id, String subject, String... citations ) {
 			Result res = validateSession( session_id );
 			if( !res.success ){
 				return res;
 			}
 			GraphDatabaseService gdb=app.GraphDatabase.get();
 			try(Transaction tx=gdb.beginTx()){
 				//something involved with making a TrustEdge
 
 			}
 
 			return new Result(true,"");
 		}
 
 	@Command
 	public Result untrust( String session_id, String trustEdge ) {
 		Result res = validateSession( session_id );
 		if( !res.success ){
 			return res;
 		}
 		GraphDatabaseService gdb=app.GraphDatabase.get();
 		try(Transaction tx=gdb.beginTx()){
 			//something involved with removing a TrustEdge
 
 		}
 
 		return null;
 	}
 
 	/*
 	 * prints all users who are trusted by the logged in user
 	 * in the subject passed to the function
 	 */
 
 	@Command
 	public Result viewSubjectiveNetwork( String session_id, String subject, int threshold ) {
 		Result res = validateSession( session_id );
 		if( !res.success ){
 			return res;
 		}
 		GraphDatabaseService gdb=app.GraphDatabase.get();
 		try(Transaction tx=gdb.beginTx()){
 			Session s=session_table.get(session_id);
 			User me=s.user;
 			Node start=me.getInternalNode();
 			LinkedList q=new LinkedList();
 			LinkedList mark=new LinkedList();
 			int depth=0;
 
 			//BFS
 			q.addFirst(start);
 			for(Email e:new User(start).viewEmails()){
 				mark.add(e.getAddress());
 				break;
 			}
 			while(!q.isEmpty()){
 				Node temp;
 				temp=(Node)q.removeLast();
 				if(depth==threshold){
 					return new Result(true,"");
 				}
 				depth++;
 				// r is relationship from User to TE
 				for(Relationship r: temp.getRelationships(RelType.FROM)){
 					//r2 is relationship from TE to next User
 					for(Relationship r2:r.getEndNode().getRelationships(RelType.TO)){
 						//accessing Email for identification to print
 						for(Email e:new User(r2.getEndNode()).viewEmails()){
 							//if email has hasnt been added and the subject is correct
 							if(!mark.contains(e.getAddress()) && r2.getStartNode().getProperty("subject").equals(subject)){
 								mark.add(e.getAddress());
 								q.addFirst(r2.getEndNode());
 								for(Email e1:new User(r2.getEndNode()).viewEmails()){
 									System.out.println(e1.getAddress());
 									break;
 								}
 							}
 						}
 						break;
 					}
 				}
 			}
 		}
 		return new Result(true,"");
 	}
 
 	/*
 	 * prints all user's primary emails that
 	 * the user associated with the argument email
 	 * has trusted on any subject
 	 */
 
 	@Command
 	public Result viewTrustNetwork( String email ) {
 		GraphDatabaseService gdb=app.GraphDatabase.get();
 		try(Transaction tx=gdb.beginTx()){
 			Email e2=new Email(email);
 			User me=e2.getUser();
 			Node start=me.getInternalNode();
 			LinkedList q=new LinkedList();
 			LinkedList mark=new LinkedList();
 
 			//BFS
 			q.addFirst(start);
 			for(Email e3:new User(start).viewEmails()){
 				mark.add(e3.getAddress());
 				break;
 			}
 			while(!q.isEmpty()){
 				Node temp;
 				temp=(Node)q.removeLast();
 
 				// r is relationship from User to TE
 				for(Relationship r: temp.getRelationships(RelType.FROM)){
 					//r2 is relationship from TE to next User
 					for(Relationship r2:r.getEndNode().getRelationships(RelType.TO)){
 						//accessing Email for identification to print
 						for(Email e:new User(r2.getEndNode()).viewEmails()){
 							//if email has hasnt been added
 							if(!mark.contains(e.getAddress())){
 								mark.add(e.getAddress());
 								q.addFirst(r2.getEndNode());
 								for(Email e1:new User(r2.getEndNode()).viewEmails()){
 									System.out.println(e1.getAddress());
 									break;
 								}
 							}
 						}
 						break;
 					}
 				}
 			}
 
 		}
 		return new Result(true,"");
 	}
 
 	public static void main(String[] args) throws IOException {
 		ShellFactory.createConsoleShell("app-sh", "", new Cli()).commandLoop();
 	}
 
 	static final String AB = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
 	static Random rnd = new Random();
 
 	private String randomString( int len ) {
 		StringBuilder sb = new StringBuilder( len );
 		for( int i = 0; i < len; i++ )
 			sb.append( AB.charAt( rnd.nextInt(AB.length()) ) );
 		return sb.toString();
 
 	}
 	/*
 	 *returns "Email", "TrustEdge" or "User"
 	 */
 	public String getNodeType(Node n){
 		for(Label s:n.getLabels()){
 			if(s.name().equals("CITATION")){
 				return "Email";
 			}else if(s.name().equals("EMAIL")){
 				return "TrustEdge";
 			}else if(s.name().equals("USER")){
 				return "User";
 			}
 		}
 		return "";
 	}
 }
