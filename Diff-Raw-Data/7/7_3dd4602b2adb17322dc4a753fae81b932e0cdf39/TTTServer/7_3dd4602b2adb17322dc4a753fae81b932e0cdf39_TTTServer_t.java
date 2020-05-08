 package ErraiLearning.server;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import javax.enterprise.context.ApplicationScoped;
 import javax.enterprise.event.Event;
 import javax.enterprise.event.Observes;
 import javax.inject.Inject;
 
 import com.google.inject.Singleton;
 
 import ErraiLearning.client.shared.Invitation;
 import ErraiLearning.client.shared.LobbyUpdate;
 import ErraiLearning.client.shared.LobbyUpdateRequest;
 import ErraiLearning.client.shared.RegisterRequest;
 import ErraiLearning.client.shared.Player;
 import ErraiLearning.client.shared.TTTGame;
 
 @ApplicationScoped
 public class TTTServer {
 
 	private Map<Integer,TTTGame> games = new HashMap<Integer,TTTGame>();
 	private Map<Integer,Player> players = new HashMap<Integer,Player>();
 	private int curPlayerId = 1;
 	private int curGameId = 1;
 	
	@Inject	private Event<Player> playerRegistration;
	@Inject	private Event<LobbyUpdate> lobbyUpdate;
	@Inject private Event<Invitation> relayInvitation;
 	
 	/* For debugging only. */
 	private int debugId;
 	private static int curDebugId = 1;
 	private static synchronized int nextDebugId() { return curDebugId++; }
 	
 	public TTTServer() {
 		debugId = nextDebugId();
 		System.out.println("Server" + debugId + ": TTTServer object is constructed.");
 	}
 	
 	public synchronized int nextPlayerId() {
 		return curPlayerId++;
 	}
 	
 	public synchronized int nextGameId() {
 		return curGameId++;
 	}
 	
 	public void registerPlayer(@Observes RegisterRequest request) {
 		int id = nextPlayerId();
 		Player player = new Player(id, request.getNickname(), 0);
 		
 		players.put(id, player);
 		System.out.println("Server" + debugId + ": Player registered.");
 		
 		playerRegistration.fire(player);
 	}
 	
 	public void handleLobbyUpdateRequest(@Observes LobbyUpdateRequest lobbyUpdateRequest) {
 		sendLobbyList();
 	}
 	
 	public void handleInvitation(@Observes Invitation invitation) {
 		System.out.println("Server"+debugId+": Invitation received from " 
 				+ invitation.getInviter().getNick() 
 				+" to " + invitation.getInvitee().getNick()
 		);
 	}
 
 	public void sendLobbyList() {
 		lobbyUpdate.fire(new LobbyUpdate(players, games));
 		System.out.println("Server"+debugId+": Lobby list sent.");
 	}
 }
