 package org.melonbrew.fe.database.databases;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.List;
 
 import org.bukkit.Material;
 import org.bukkit.configuration.ConfigurationSection;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 import org.melonbrew.fe.Fe;
 import org.melonbrew.fe.database.Account;
 import org.melonbrew.fe.database.Database;
 
 public class ItemDB extends Database {
 	private final Fe plugin;
 
 	public ItemDB(Fe plugin) {
 		super(plugin);
 
 		this.plugin = plugin;
 	}
 
 	public boolean init() {
 		return true;
 	}
 
 	@Override
 	public List<Account> getTopAccounts(int size){
 		List<Account> topAccounts = getAccounts();
 
 		Collections.sort(topAccounts, new Comparator<Account>(){
 			public int compare(Account account1, Account account2){
 				return ((Double) account1.getMoney()).compareTo(account2.getMoney());
 			}});
		
		topAccounts.subList(0, size - 1);
 
 		return topAccounts;
 	}
 
 	@Override
 	public List<Account> getAccounts(){
 		List<Account> accounts = new ArrayList<Account>();
 
 		for (Player player : plugin.getServer().getOnlinePlayers()){
 			accounts.add(plugin.getAPI().getAccount(player.getName()));
 		}
 
 		return accounts;
 	}
 
 	@Override
 	public double loadAccountMoney(String name){
 		Player player = plugin.getServer().getPlayerExact(name);
 
 		if (player == null){
 			return 0;
 		}
 
 		ItemStack[] items = player.getInventory().getContents();
 
 		double money = 0;
 
 		for (ItemStack stack : items){
 			if (stack != null && stack.getType() == Material.GOLD_INGOT){
 				money += stack.getAmount();
 			}
 		}
 
 		return money * 10;
 	}
 
 	@Override
 	protected void saveAccount(String name, double money){
 
 	}
 
 	@Override
 	public void removeAccount(String name){
 
 	}
 
 	@Override
 	public void getConfigDefaults(ConfigurationSection section){
 
 	}
 
 	@Override
 	public void clean(){
 	}
 
 	@Override
 	public void close(){
 	}
 
 	@Override
 	public String getName(){
 		return "Item";
 	}
 }
