 package debugging;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.net.Socket;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Scanner;
 import java.util.Set;
 
 import org.jibble.pircbot.Colors;
 
 import main.NoiseBot;
 
 /**
  * Client
  *
  * @author Michael Mrozek
  *         Created Dec 19, 2010.
  */
 public class Client {
 	private Socket socket;
 	private Scanner in;
 	private PrintWriter out;
 	
 	private String authentication;
 	private String nick;
 	
 	private Map<String, Set<Level>> activeLevels;
 	
 	public Client(Socket socket) throws IOException {
 		this.socket = socket;
 		this.in = new Scanner(this.socket.getInputStream());
 		this.out = new PrintWriter(this.socket.getOutputStream(), true);
 		this.authentication = null;
 		this.nick = null;
 		this.activeLevels = new HashMap<String, Set<Level>>();
 
 		new Thread(new Runnable() {
 			@Override public void run() {
 				final Scanner in = Client.this.in;
 				
				while(!Client.this.socket.isClosed()) {
					if(in.hasNextLine()) {
						Debugger.me.in(Client.this, in.nextLine());
					}
 				}
 				
 				System.out.println("Removed debugging client: " + Client.this);
 				Debugger.me.clients.remove(Client.this);
 			}
 		}).start();
 	}
 	
 	public Socket getSocket() {return this.socket;}
 	public boolean isAuthenticated() {return this.authentication != null && this.authentication.isEmpty();}
 	public String getNick() {return this.nick;}
 	public void setLevels(Map<String, Set<Level>> levels) {this.activeLevels = levels;}
 	
 	public void send(Object object) {this.out.println(object.toString());}
 	public void send(Event event) {
 		final String fullName = event.getClassName() + "." + event.getMethodName();
 		final Level level = event.getLevel();
 		
 		for(Map.Entry<String, Set<Level>> entry : this.activeLevels.entrySet()) {
 			final String name = entry.getKey();
 			final Set<Level> levels = entry.getValue();
 			if((name.equals("*") || fullName.contains(name)) && levels.contains(level)) {
 				send((Object)event.toString()); //TODO
 				break;
 			}
 		}
 	}
 	
 	public void send(Iterable iter) {
 		for(Object o : iter) {
 			if(o instanceof Event) {
 				this.send((Event)o);
 			} else {
 				this.send(o);
 			}
 		}
 	}
 	
 	public void startAuthentication(String nick, String code) {
 		this.nick = nick;
 		this.authentication = code;
 	}
 	
 	public void authenticate(String code) {
 		if(this.authentication == null) {
 			this.send("No authentication in progress");
 		} else if(!this.authentication.equals(code)) {
 			this.send("Invalid authentication code");
 		} else {
 			this.authentication = "";
 			NoiseBot.me.sendNotice("Debugger connected by " + Colors.BLUE + this.nick);
 			this.send("Authentication complete");
 		}
 	}
 }
