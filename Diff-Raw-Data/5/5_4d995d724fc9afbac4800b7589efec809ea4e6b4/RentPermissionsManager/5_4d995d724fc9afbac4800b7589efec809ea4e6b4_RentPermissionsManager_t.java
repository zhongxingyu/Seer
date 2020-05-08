 package me.krotn.Rent;
 
 import java.util.ArrayList;
 
 import com.nijiko.permissions.PermissionHandler;
 import com.nijikokun.bukkit.Permissions.Permissions;
 
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.Plugin;
 
 /**
  * This class handles various permissions related tasks for the Rent plugin.
  *
  */
 public class RentPermissionsManager {
 	private Rent plugin;
 	private PermissionHandler permHandler;
 	private RentLogManager logMan;
 	private String baseNode = "Rent.";
 	private ArrayList<String> nonOpsPermissions = new ArrayList<String>();
 	
 	/**
 	 * Constructs a RentPermissionsManager using the given Rent plugin.
 	 * @param plugin The plugin to use when getting necessary objects.
 	 */
 	public RentPermissionsManager(Rent plugin){
 		this.plugin = plugin;
 		this.logMan = this.plugin.getLogManager();
 		setupNonOpsPermissions();
 		Plugin permissionsPlugin = this.plugin.getServer().getPluginManager().getPlugin("Permissions");
 		if (this.permHandler == null) {
 	          if (permissionsPlugin != null) {
 	              this.permHandler = ((Permissions) permissionsPlugin).getHandler();
 	          } else {
 	              logMan.info("Permission system not detected, defaulting to OP");
 	              permHandler = null;
 	          }
 	      }
 	}
 	
 	/**
 	 * This method sets up the permissions list for non-opped players.
 	 * It adds various permissions nodes to {@code nonOpsPermissions}.
 	 */
 	private void setupNonOpsPermissions(){
 		
 	}
 	
 	/**
 	 * This method returns whether or not a player has a given permission.
 	 * @param player The {@code Player} in question.
 	 * @param node The {@code String} permissions node to test <b>WITHOUT</b> "Rent." internally "Rent."
      * is added to the start of the node.
 	 * @return {@code true} the player has this permission. {@code false} otherwise.
 	 */
 	public boolean checkPermission(Player player,String node){
 		if(node==null){
 			return false;
 		}
 		String workingNode = baseNode+node;
 		if(permHandler!=null){
 			return permHandler.has(player, workingNode);
 		}
		if(player.isOp() && !(workingNode.equalsIgnoreCase("Rent.untracked"))){
 			return true;
 		}
		return nonOpsPermissions.contains(workingNode);
 	}
 }
