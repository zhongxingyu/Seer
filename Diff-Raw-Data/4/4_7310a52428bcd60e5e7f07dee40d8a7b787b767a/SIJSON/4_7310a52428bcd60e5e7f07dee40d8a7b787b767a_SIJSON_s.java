 package net.loadingchunks.plugins.SheepInfo;
 
 import java.io.IOException;
 import java.io.OutputStream;
 import java.net.InetSocketAddress;
 import java.util.concurrent.Executor;
 
 import org.bukkit.World;
 import org.bukkit.entity.Player;
 import org.json.simple.*;
 
 import com.sun.net.httpserver.HttpExchange;
 import com.sun.net.httpserver.HttpHandler;
 import com.sun.net.httpserver.HttpServer;
 
 import net.loadingchunks.plugins.SheepInfo.SheepInfo;
 
 
 public class SIJSON {
     private final SheepInfo plugin;
 
     public SIJSON (SheepInfo plugin) {
         this.plugin = plugin;
     }
     
     public JSONObject Info(World w)
     {
     	JSONObject object = new JSONObject();
     	object.put("name", w.getName());
     	object.put("players", new Integer(w.getPlayers().size()));
     	object.put("chunks", new Integer(w.getLoadedChunks().length));
     	object.put("entities", new Integer(w.getLivingEntities().size()));
     	object.put("max_mem", new Long( Runtime.getRuntime().maxMemory()));
     	object.put("free_mem", new Long(Runtime.getRuntime().freeMemory()));
     	object.put("time", new Long(w.getTime()));
     	return object;
     }
     
     public JSONObject Players(World w)
     {
     	JSONObject object = new JSONObject();
     	JSONObject player = new JSONObject();
     	
     	for ( Player p : w.getPlayers())
     	{
     		player.clear();
     		player.put("name", p.getName());
    		player.put("nickname", p.getDisplayName());
     		player.put("ip", p.getAddress().getHostName());
     		player.put("x", p.getLocation().getX());
     		player.put("y", p.getLocation().getY());
     		player.put("z", p.getLocation().getZ());
     		player.put("holding", p.getItemInHand().getTypeId());
     		player.put("health", p.getHealth());
     		player.put("air", p.getRemainingAir());
     		player.put("dead", p.isDead());
     		
     		object.put(p.getName(), player);
     	}
     	return object;
     }
 }
