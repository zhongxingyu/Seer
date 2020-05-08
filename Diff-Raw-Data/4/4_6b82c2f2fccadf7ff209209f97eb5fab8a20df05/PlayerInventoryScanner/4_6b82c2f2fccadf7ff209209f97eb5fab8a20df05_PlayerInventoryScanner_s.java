 package com.aim.wjcrouse913.NetherBan;
 
 import java.io.File;
 import java.io.IOException;
 
 import org.bukkit.Material;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.PlayerInventory;
 import org.bukkit.util.config.Configuration;
 
 public class PlayerInventoryScanner{
 	private String invdir;
 	
 	private Player player;
 	private PlayerInventory inv;
 	
 	public PlayerInventoryScanner(){
 	}
 	
 	public void use(Player p){
 		this.inv=p.getInventory();
 		this.player=p;
 		this.invdir = NetherBan.mainDirectory+File.separator+"PlayerINV"+File.separator+p.getName()+".yml";
 	}
 	
 	public void save(){
 		ItemStack[] i = this.inv.getContents();
 		
 		File main = new File(invdir);
 		
 		if(!main.exists()){
 			try{
 				main.createNewFile();
 			}catch(IOException e){
 				e.printStackTrace();
 			}
 		}
 		
 		Configuration conf = new Configuration(main);
 		conf.load();
 		
 		for(int a=0;a<i.length;a++){
 			
 			if(i[a]!=null){
 				conf.setProperty(a+".material", i[a].getType().name());
 				conf.setProperty(a+".amount", String.valueOf(i[a].getAmount()));
 			}else{
 				conf.setProperty(a+".material", "null");
 				conf.setProperty(a+".amount", "0");
 			}
 		}
 		conf.save();
 	}
 	
 	public ItemStack[] load(){
 		ItemStack[] ret = new ItemStack[36];
 		
 		File main = new File(invdir);
 		
 		if(!main.exists()){
 			return player.getInventory().getContents();
 		}
 		
 		Configuration conf = new Configuration(main);
 		conf.load();
 		
 		int i=0;
 		
 		for(String s : conf.getKeys("")){
 			
 			if(i<36){
 				if(!conf.getString(s+".material").equalsIgnoreCase("null")){
 					Material m = Material.valueOf(conf.getString(s+".material"));
 					int am = Integer.parseInt(conf.getString(s+".amount"));
 					
 					ItemStack item = new ItemStack(m, am);
 					
 					ret[i] = item;
 					
 					i++;
 				}else{
 					ret[i]=null;
 				}
 			}else{
 				break;
 			}
 		}
 		return ret;
 	}
 }
