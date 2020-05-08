 package facebook;
 
 import nodes.DistNode;
 import transactions.Command;
 import transactions.Transaction;
 
 public class PostMessage extends FacebookOperation {
 	private static final String[] COMMANDS = {	"txstart",
 												"get logged_in", //check that curUser is logged in
 												"get [username]_friends",
 												"append [friend]_wall \"[message]\n\"", //execute for each line in [username]_friends file
 												"txcommit"};
 	private String message;
 	private int numFriends;
 	
 	public PostMessage(User u, String message, DistNode n){
 		super(COMMANDS, n, u);
 		this.n.onFacebookCommand( this.nextCommand());
 		this.message = message;
 		this.numFriends = 0;
 	}
 	
 	@Override
 	public void onCommandFinish(Command c) {
 		String newCommand;
 		
 		switch( this.cmds.size() ) {
 		case 3:
 			if( FacebookOperation.isUserLoggedIn(this.user, this.n.addr, c.getContents())) {
 				newCommand = FacebookOperation.replaceField(this.nextCommand(), "username", this.user.getUsername());
 				this.n.onFacebookCommand( newCommand );
 			} else {
 				this.notLoggedIn();
 			}
 			break;
 		case 2:
 			String replaceMessage = FacebookOperation.replaceField(this.nextCommand(), "message", FacebookOperation.boxify(this.user, this.message));
 			String[] friends = c.getContents().split("\n");
 			this.numFriends = friends.length;
 			for( String friend : c.getContents().split("\n") ) {
				newCommand = FacebookOperation.replaceField(replaceMessage, "friend", friend);
				this.n.onFacebookCommand( newCommand );
 			}
 			break;
 		case 1:
 			this.numFriends--;
 			if( this.numFriends <= 0 ) {
 				this.n.onFacebookCommand(this.nextCommand());
 			}
 			break;
 		}
 	}
 
 	@Override
 	public void onAbort(Transaction txn) {
 		System.out.println("Error: Node " + this.n.addr + ": Cannot execute command: Please try again");
 	}
 
 	@Override
 	public void onCommit(Transaction txn) {
 		System.out.println("Success: Node " + this.n.addr + ": Successfully posted message.");
 
 	}
 
 	@Override
 	public void onStart(int txId) {
 		this.commandId = txId;
 		this.n.onFacebookCommand( this.nextCommand());
 	}
 
 }
