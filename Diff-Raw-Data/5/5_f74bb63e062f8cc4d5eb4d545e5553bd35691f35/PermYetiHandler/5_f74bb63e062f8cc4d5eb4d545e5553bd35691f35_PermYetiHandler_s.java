 package com.rcjrrjcr.bukkitplugins.util.permissionsinterface;
 
 import java.util.LinkedHashSet;
 
 import org.bukkit.plugin.Plugin;
 
 import com.nijiko.permissions.Control;
 import com.nijikokun.bukkit.Permissions.Permissions;
 import com.rcjrrjcr.bukkitplugins.util.RcjrPlugin;
 
 
 //TODO: Find way to add permissions dynamically.
 public class PermYetiHandler implements IPermHandler {
 	private Permissions plugin;
 	private RcjrPlugin origin;
 	private Control permHandle;
 	private LinkedHashSet<PermissionData> permAddCache;
 	private LinkedHashSet<PermissionData> permRemCache;
 	
 	public PermYetiHandler(Plugin plugin,RcjrPlugin origin)
 	{
 		setPlugin(plugin);
 		this.origin = origin;
 		permAddCache = new LinkedHashSet<PermissionData>();
 		permRemCache = new LinkedHashSet<PermissionData>();
 	}
 	@Override
 	public boolean hasPerm(String world,String playerName, String perm) {
 		if(origin.active==null||!(origin.active.isPermActive())) return false;
 		if(origin.getServer().getPlayer(playerName)==null)return false;
 		return permHandle.has(origin.getServer().getPlayer(playerName), perm);
 	}
 
 	@Override
 	public void setPerm(String world, String playerName, String perm, boolean hasPerm) {
 		if(hasPerm)
 		{
 			addPerm(world,playerName,perm);
 		}
 		else
 		{
 			removePerm(world,playerName,perm);
 		}
 	}
 
 	@Override
 	public void addPerm(String world,String playerName, String perm) {
 		if(origin.active==null||!(origin.active.isPermActive()))
 		{
 			PermissionData pData = new PermissionData();
 			pData.setWorld(world);
 			pData.setPlayerName(playerName);
 			pData.setNode(perm);
 			permAddCache.add(pData);
 			return;
 		}
		permHandle.addUserPermission("world",playerName,perm);
 		return;
 	}
 
 	@Override
 	public void removePerm(String world,String playerName, String perm) {
 		if(origin.active==null||!(origin.active.isPermActive()))
 		{
 			PermissionData pData = new PermissionData();
 			pData.setWorld(world);
 			pData.setPlayerName(playerName);
 			pData.setNode(perm);
 			permRemCache.add(pData);
 			return;
 		}
		permHandle.removeUserPermission(world,playerName, perm);
 	}
 
 	@Override
 	public boolean isInGroup(String world,String playerName, String group) {
 		if(origin.active==null||!(origin.active.isPermActive())) return false;
 		return permHandle.inGroup(world, playerName, group);
 	}
 
 
 	@Override
 	public void setPlugin(Plugin plugin) {
 		if(plugin instanceof Permissions)
 		{
 			this.plugin=(Permissions) plugin;
 			this.permHandle = (Control) this.plugin.getHandler();
 		}
 		return;		
 	}
 
 	@Override
 	public String[] listGroups(String world, String playerName) {
 		if(origin.active==null||!(origin.active.isPermActive())) return null;
 		return permHandle.getGroups(world,playerName);
 	}
 	
 	@Override
 	public void flushCache() {
 		if(origin.active==null||!(origin.active.isPermActive())) return;
 		for(PermissionData pAdd : permAddCache)
 		{
 			addPerm(pAdd.getWorld(),pAdd.getPlayerName(),pAdd.getNode());
 			permAddCache.remove(pAdd);
 		}
 		for(PermissionData pAdd : permRemCache)
 		{
 			removePerm(pAdd.getWorld(),pAdd.getPlayerName(),pAdd.getNode());
 			permRemCache.remove(pAdd);
 		}
 		return;
 	}
 	@Override
 	public LinkedHashSet<PermissionData> getAddCache() {
 		return permAddCache;
 	}
 	@Override
 	public LinkedHashSet<PermissionData> getRemCache() {
 		return permRemCache;
 	}
 	@Override
 	public void setCache(LinkedHashSet<PermissionData> addCache,
 			LinkedHashSet<PermissionData> remCache) {
 		permAddCache = addCache;
 		permRemCache = remCache;
 		
 	}
 	
 	
 }
