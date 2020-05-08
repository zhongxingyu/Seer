 import java.io.IOException;
 import java.util.Calendar;
 import java.util.Properties;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 import java.text.DateFormat;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.ArrayList;
 
 
 public class GamePlayer extends NanoHTTPD {
 	
	private OurAgent agent;
 
 	public GamePlayer(int port, Agent agent) throws IOException {
 		super(port);
 		this.agent = agent;
 	}
 
 	/**
 	 * this method is called when a new match begins
 	 */
 	protected void commandStart(String msg) {
 		// msg="(START <MATCH ID> <ROLE> <GAME DESCRIPTION> <STARTCLOCK> <PLAYCLOCK>)
 		// e.g. msg="(START tictactoe1 white ((role white) (role black) ...) 1800 120)" means:
 		//       - the current match is called "match0815"
 		//       - your role is "white",
 		//       - after at most 1800 seconds, you have to return from the commandStart method
 		//       - for each move you have 120 seconds
 		agent.init(getInitialState(msg));
 	}
 
 	/**
 	 * this method is called once for each move
 	 * @return the move of this player
 	 */
 	protected String commandPlay(String msg){
 		// msg="(PLAY <MATCHID> <TURN> <LASTMOVE> <PERCEPTS>)
 		return agent.nextAction(getPercepts(msg));
 	}
 
 	/**
 	 * this method is called if the match is over
 	 */
 	protected void commandStop(String msg){
 		// msg="(STOP <MATCH ID> <JOINT MOVE>)
 		
 		// TODO:
 		//    - clean up the GamePlayer for the next match
 		//    - be happy if you have won, think about what went wrong if you have lost ;-)
 	}
 
 	public Response serve( String uri, String method, Properties header, Properties parms, String data )
 	{
 		try{
 			String response_string=null;
 			if(data!=null){
 				System.out.println(DateFormat.getTimeInstance(DateFormat.FULL).format(Calendar.getInstance().getTime()));
 				System.out.println("Command: " + data);
 				String command=getCommand(data);
 				if(command==null){
 					throw(new IllegalArgumentException("Unknown message format"));
 				}else if(command.equals("START")){
 					response_string="READY";
 					commandStart(data);
 				}else if(command.equals("PLAY")){
 					response_string=commandPlay(data);
 /*				}else if(command.equals("replay")){
 					response_string=commandReplay(data);*/
 				}else if(command.equals("STOP")){
 					response_string="DONE";
 					commandStop(data);
 				}else{
 					throw(new IllegalArgumentException("Unknown command:"+command));
 				}
 			}else{
 				throw(new IllegalArgumentException("Message is empty!"));
 			}
 			System.out.println(DateFormat.getTimeInstance(DateFormat.FULL).format(Calendar.getInstance().getTime()));
 			System.out.println("Response:"+response_string);
 			if(response_string!=null && response_string.equals("")) response_string=null;
 			return new Response( HTTP_OK, "text/acl", response_string );
 		}catch(IllegalArgumentException ex){
 			System.err.println(ex);
 			ex.printStackTrace();
 			return new Response( HTTP_BADREQUEST, "text/acl", "NIL" );
 		}
 	}
 
 	private Collection<String> getInitialState(String msg){
 			Collection<String> result = new ArrayList<String>();
 			Matcher m=Pattern.compile("\\(INIT\\s*([^\\s)]*|\\((AT|ORIENTATION|HOME)[^)]*\\))\\)").matcher(msg);
 			String fluent;
 			while (m.find()) {
 				fluent = m.group(1);
 				// System.err.println("found: " + m.group(0) + " fluent: " + fluent);
 				result.add(fluent);
 			}
 			m=Pattern.compile("\\(SIZE\\s+([0-9]+)\\s+([0-9]+)\\s*\\)").matcher(msg);
 			while (m.find()) {
 				fluent = m.group(0);
 				// System.err.println("found: " + fluent);
 				result.add(fluent);
 			}
 			return result;
 	}
 
 
 	private Collection<String> getPercepts(String msg){
 		// TODO: only works for atomic percepts and actions so far
 		if (msg.endsWith("NIL)")) {
 			return new ArrayList<String>();
 		} else {
 			String perceptString="";
 			try{
 				int openIdx = msg.lastIndexOf("(");
 				int closeIdx = msg.indexOf(")", openIdx);
 				perceptString = msg.substring(openIdx, closeIdx+1);
 	// 			Matcher m=Pattern.compile("\\(\\s*[^\\s]+\\s+[^\\s]+\\s+[^\\s]+\\s+(\\([^)]*\\))\\s*\\)").matcher(msg);
 	// 			if(m.lookingAt()){
 	// 				perceptString=m.group(1);
 	// 			}
 				perceptString = perceptString.toUpperCase();
 				perceptString = perceptString.substring(1, perceptString.length()-1);
 				// System.out.println("percepts: " + perceptString);
 			}catch(Exception ex){
 				System.err.println("Pattern to extract percepts did not match!");
 				ex.printStackTrace();
 			}
 			if (!perceptString.equals("")) {
 				String[] perceptArray = perceptString.split("\\s+");
 				return Arrays.asList(perceptArray);
 			} else {
 				return new ArrayList<String>();
 			}
 		}
 	}
 
 	private String getCommand(String msg){
 		String cmd=null;
 		try{
 			Matcher m=Pattern.compile("\\(\\s*([^\\s]*)\\s").matcher(msg);
 			if(m.lookingAt()){
 				cmd=m.group(1);
 			}
 			cmd=cmd.toUpperCase();
 		}catch(Exception ex){
 			System.err.println("Pattern to extract command did not match!");
 			ex.printStackTrace();
 		}
 		return cmd;
 	}
 
 	public void waitForExit(){
 		try {
 			server_thread.join(); // wait for server thread to exit
 		}catch(Exception ex){
 			ex.printStackTrace();
 		}
 	}
 }
