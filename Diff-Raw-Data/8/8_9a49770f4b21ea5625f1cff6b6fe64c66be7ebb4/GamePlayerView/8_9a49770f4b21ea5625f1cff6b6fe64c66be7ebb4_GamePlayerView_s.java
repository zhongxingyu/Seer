 package theresistance.server.view;
 
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.TreeMap;
 
 import theresistance.core.Game;
 import theresistance.core.Player;
 import theresistance.core.Role;
 
 /**
  * A view of other players in the game
  */
 public class GamePlayerView
 {
 	private final Map<String, String> roles = new TreeMap<>();
 	private final List<String> order = new LinkedList<>();
 	private final String leader;
 
 	public GamePlayerView(Game game, String player)
 	{
 		Role myRole = game.getPlayer(player).getRole();
 
 		for (Player p : game.getPlayers())
 		{
 			order.add(p.getName());
			if (p.getName().equals(player)) {
 				roles.put(p.getName(), myRole.getClass().getSimpleName().toUpperCase());
			} else {
 				roles.put(p.getName(), myRole.identify(p.getRole()));
 			}
 		}
 
 		leader = game.getCurrentLeader().getName();
 	}
 
 	public Map<String, String> getRoles()
 	{
 		return roles;
 	}
 
 	public List<String> getOrder()
 	{
 		return order;
 	}
 
 	public String getLeader()
 	{
 		return leader;
 	}
 }
