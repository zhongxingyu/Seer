 package com.archmageinc.BlacklistCheck;
 
 import java.io.File;
 import java.net.Inet4Address;
 import java.net.InetAddress;
 import java.util.Iterator;
 import java.util.List;
 import java.util.logging.Logger;
 
 import org.apache.commons.net.util.SubnetUtils;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class BlacklistCheck extends JavaPlugin {
 
	private Logger log				=	getLogger();
 	private BlacklistLookup BLL;
 	
 	@Override
 	public void onEnable(){
 		initialConfigCheck();
 		getDNSBLServers();
		getServer().getPluginManager().registerEvents(new BlacklistListener(this), this);
 		BLL	=	new BlacklistLookup(this);
 		logMessage("Enabled");
 	}
 	
 	@Override
 	public void onDisable(){
 		logMessage("Disabled");
 	}
 	
 	public String[] getDNSBLServers(){
 		if(!getConfig().isList("DNSBLServers"))
 			return null;
 		
 		return	getConfig().getStringList("DNSBLServers").toArray(new String[0]);
 	}
 	
 	private void initialConfigCheck(){
 		getConfig().options().copyDefaults(true);
 		if(!(new File(this.getDataFolder(),"config.yml").exists())){
 			this.logMessage("Saving default configuration file.");
 			this.saveDefaultConfig();
 		}
 	}
 	
 	public void logMessage(String msg){
 		PluginDescriptionFile pdFile	=	this.getDescription();
 		log.info("["+pdFile.getName()+" "+pdFile.getVersion()+"]: "+msg);
 	}
 	
 	public void logWarning(String msg){
 		PluginDescriptionFile pdFile	=	this.getDescription();
 		log.warning("["+pdFile.getName()+" "+pdFile.getVersion()+"]: "+msg);
 	}
 	
 	public boolean isWhitelisted(InetAddress ip){
 		reloadConfig();
 		List<String> whitelist	=	getConfig().getStringList("Whitelist");
 		if((ip instanceof Inet4Address)){
 			String ips	=	((Inet4Address) ip).toString().replaceAll("/","");
 			if(getConfig().getBoolean("Debug"))
 				logMessage("Checking "+ips+" against the whitelist.");
 			
 			Iterator<String> itr	=	whitelist.iterator();
 			while(itr.hasNext()){
 				String subnet	=	itr.next();
 				try{
 					if(getConfig().getBoolean("Debug"))
 						logMessage("Checking "+ips+" against whitelist entry "+subnet);
 					
 					SubnetUtils util	=	new SubnetUtils(subnet);
 					if(util.getInfo().getAddressCount()==0 && util.getInfo().getNetworkAddress().equals(ips)){
 						if(getConfig().getBoolean("Debug"))
 							logMessage("Address "+ips+" is whitelisted.");
 						
 						return true;
 					}
 					if(util.getInfo().isInRange(ips)){
 						if(getConfig().getBoolean("Debug"))
 							logMessage("Address "+ips+" is in a whitelisted subnet: "+subnet);
 						
 						return true;
 					}
 				}catch(IllegalArgumentException e){
 					logWarning("Misconfiguration for whitelisted subnet "+subnet+". Invalid CIDR Notation");
 				}
 			}
 			if(getConfig().getBoolean("Debug"))
 				logMessage(ips+" is not whitelisted.");
 			
 		}
 		
 		return false;
 	}
 	
 	public boolean isBlacklisted(InetAddress ip){
 		return BLL.isBlacklisted(ip);
 	}
 	
 }
