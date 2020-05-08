 package me.Kruithne.WolfHunt;
 
 public class Configuration {
 	
 	private WolfHunt wolfHuntPlugin = null;
 	
 	public int trackingItem;
 	public int trackingRadius;
 	
 	public boolean allowOpOverride;
 	
 	Configuration(WolfHunt parentPlugin)
 	{
 		this.wolfHuntPlugin = parentPlugin;
 	}
 
 	public String getConfigValue(String configKey, String defaultValue)
 	{	
 		if (this.wolfHuntPlugin.getConfig().contains(String.format(Constants.pluginNodePath, configKey)))
 		{
 			return this.wolfHuntPlugin.getConfig().getString(String.format(Constants.pluginNodePath, configKey));
 		}
 		else
 		{
			this.setConfigValue(String.format(Constants.pluginNodePath, configKey), defaultValue);
			return defaultValue;
 		}
 	}
 	
 	public void setConfigValue(String configKey, String configValue)
 	{
 		this.wolfHuntPlugin.getConfig().set(configKey, configValue);
 		this.wolfHuntPlugin.saveConfig();
 	}
 	
 	public void loadConfiguration()
 	{
 		this.trackingItem = Integer.parseInt(this.getConfigValue("trackingItem", Constants.default_trackingItem));
 		this.trackingRadius = Integer.parseInt(this.getConfigValue("trackingRadius", Constants.default_trackingRadius));
 		this.allowOpOverride = Boolean.parseBoolean(this.getConfigValue("allowOpOverride", Constants.default_allowOpOverride));
 	}
 	
 }
