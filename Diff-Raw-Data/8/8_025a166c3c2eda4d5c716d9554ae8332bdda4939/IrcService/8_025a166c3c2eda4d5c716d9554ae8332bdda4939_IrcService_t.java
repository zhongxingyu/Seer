 package org.touchirc.irc;
 
 
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Set;
 
 import org.jibble.pircbot.IrcException;
 import org.jibble.pircbot.NickAlreadyInUseException;
 import org.touchirc.TouchIrc;
 import org.touchirc.model.Server;
 
 import android.app.Service;
 import android.content.Intent;
 import android.os.IBinder;
 import android.util.SparseArray;
 import android.widget.Toast;
 
 public class IrcService extends Service {
 	
 	private final IrcBinder ircBinder;
 	
 	// Map of Server, IrcBot for connected Bots
 	private HashMap<Server, IrcBot> botsConnected;
 	
 	// Map of idServer, Server for available servers 
 	private SparseArray<Server> availableServers; // SparseArray = Map<Integer, Object>
 	
 	public IrcService(){
 		super();
		
 		this.ircBinder = new IrcBinder(this);
 		this.botsConnected = new HashMap<Server, IrcBot>();
		TouchIrc.getInstance().load(this);
		this.availableServers = TouchIrc.getInstance().getAvailableServers(); 
 	}
 	
 	@Override
 	public void onCreate(){
 	}
 
 	@Override
 	public IBinder onBind(Intent intent) {
 		return this.ircBinder;
 	}
 	
 	@Override
 	public int onStartCommand(Intent intent, int flags, int startId){
 		Toast.makeText(getApplicationContext(), "TouchIrcService Started", Toast.LENGTH_LONG).show(); // TODO Delete it
 		return START_STICKY;
 	
 	}
 	
 	public void reloadAvailableServers(){
		this.availableServers = TouchIrc.getInstance().getAvailableServers();
 	}
 	
 	// return null if the idServer isn't in the Hashmap servers
 	public Server getServerById(int idServer){
 		return this.availableServers.get(idServer);
 	}
 	
 	
 	public Set<Server> getConnectedServers(){
 		return this.botsConnected.keySet();
 	}
 	
 	public synchronized void connect(int idServer){
 		final Server server = availableServers.get(idServer);
 		final IrcBot bot = getBot(server);
 		new Thread("Thread for the server : " + server.getName()){
 			@Override
 			public void run(){
 				System.out.println(" Thread for the server : " + server.getName());
 
 				bot.setNickName("Toto1234");
 				bot.setIdent("BouletIdent");
 				bot.setRealName("BouletRealName");
 				
 				
 				try {
 					bot.setEncoding(server.getEncoding());
 					bot.connect(server.getHost(),server.getPort(),server.getPassword());
 					
 					bot.joinChannel("#Boulet");
 				} catch (NickAlreadyInUseException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				} catch (IOException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				} catch (IrcException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 			}
 		}.start();
 		
 		
 		
 	}
 	
 	/**
 	 * Get the Bot/Connection of the server with this idServer
 	 * @param idServer of the server (= id of the Bot)
 	 * @return the bot
 	 */
 	// If the Bot don't existe it will be created !
 	public synchronized IrcBot getBot(Server server){
 		IrcBot ircBot = this.botsConnected.get(server);
 		if(ircBot == null){
 			ircBot = new IrcBot(server,this);
 			this.botsConnected.put(server, ircBot);
 		}
 		return ircBot;
 	}
 	
 	
    
 
    
 
     
 }
