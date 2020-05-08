 package com.mitsugaru.KarmicLotto;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import org.bukkit.configuration.ConfigurationSection;
 
 public class Config {
 	private KarmicLotto plugin;
 	public boolean debug, effects;
 	public int amount;
 
 	public Config(KarmicLotto karmiclottery)
 	{
 		plugin = karmiclottery;
 		ConfigurationSection config = plugin.getConfig();
 		//Hashmap of defaults
 		final Map<String, Object> defaults = new HashMap<String, Object>();
 		defaults.put("amount", 1);
 		defaults.put("effects", true);
 		defaults.put("list", new ArrayList<String>());
 		defaults.put("version", plugin.getDescription().getVersion());
 		boolean gen = false;
 		for(final Entry<String, Object> e : defaults.entrySet())
 		{
 			if(!config.contains(e.getKey()))
 			{
 				config.set(e.getKey(), e.getValue());
 				gen = true;
 			}
 		}
 		if(gen)
 		{
 			plugin.syslog.info(KarmicLotto.prefix + " No KarmicLotto config file found. Creating config file.");
 		}
 		debug = config.getBoolean("debug", false);
 		amount = config.getInt("amount");
 		effects = config.getBoolean("effects", true);
 		//Check for update
 		checkUpdate();
 	}
 
 	private void checkUpdate() {
 		// TODO Auto-generated method stub
 
 	}
 
 	public Map<Item, Double> getLotto(String lotto)
 	{
 		final Map<Item,Double> list = new HashMap<Item, Double>();
 		final ConfigurationSection section = plugin.getConfig().getConfigurationSection(lotto);
 		for(final String entry : section.getKeys(false))
 		{
 			try
 			{
 				int key = Integer.parseInt(entry);
 				//If it has child nodes, parse those as well
 				if (section.isConfigurationSection(entry))
 				{
 					final ConfigurationSection sec = section
 							.getConfigurationSection(entry);
 					for (final String dataValue : sec.getKeys(false))
 					{
 						int secondKey = Integer.parseInt(dataValue);
 						double secondValue = sec.getDouble(dataValue);
						if(key != 373)
						{
 						list.put(
 								new Item(key, Byte.parseByte("" + secondKey), (short) secondKey),
 								secondValue);
						}
						else
						{
							list.put(new Item(key, Byte.parseByte("" + 0), (short) secondKey),
									secondValue);
						}
 					}
 				}
 				else
 				{
 					double value = section.getDouble(entry);
 					list.put(new Item(key, Byte.valueOf("" + 0), (short) 0), value);
 				}
 			}
 			catch (final NumberFormatException ex)
 			{
 				plugin.syslog.warning("Non-integer value in karma list");
 				ex.printStackTrace();
 			}
 		}
 		return list;
 	}
 
 	public void save()
 	{
 		plugin.saveConfig();
 	}
 
 	public void setProperty(String path, Object o) {
 		plugin.getConfig().set(path, o);
 	}
 
 	public Object getProperty(String path)
 	{
 		return plugin.getConfig().get(path);
 	}
 
 	public List<String> getStringList(String path) {
 		List<String> list = plugin.getConfig().getStringList(path);
 		if(list != null)
 		{
 			return list;
 		}
 		return new ArrayList<String>();
 	}
 
 	public void removeProperty(String path) {
 		plugin.getConfig().set(path, null);
 	}
 
 	public boolean getBoolean(String path) {
 		return (Boolean)(this.getProperty(path));
 	}
 
 	public double getDouble(String path) {
 		return (Double) (this.getProperty(path));
 	}
 
 	public int getInteger(String path) {
 		return (Integer) (this.getProperty(path));
 	}
 
 	public String getString(String path) {
 		return (String) this.getProperty(path);
 	}
 }
