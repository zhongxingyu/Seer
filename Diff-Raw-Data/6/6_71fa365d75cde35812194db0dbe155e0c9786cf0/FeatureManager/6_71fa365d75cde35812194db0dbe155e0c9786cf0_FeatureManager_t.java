 package com.turt2live.antishare.manager;
 
 import java.io.File;
 
 import com.feildmaster.lib.configuration.EnhancedConfiguration;
 
 public class FeatureManager extends AntiShareManager {
 
 	public static enum Feature{
 		INVENTORIES, REGIONS, BLOCKS, MONEY, SELF, ALWAYS_ON;
 	}
 
 	private EnhancedConfiguration config;
 
 	@Override
 	public boolean load(){
 		config = new EnhancedConfiguration(new File(plugin.getDataFolder(), "features.yml"), plugin);
 		config.loadDefaults(plugin.getResource("resources/features.yml"));
		if(!config.fileExists() || !config.checkDefaults()){
			config.saveDefaults();
		}
		config.load();
 		return true;
 	}
 
 	@Override
 	public boolean save(){
 		return true;
 	}
 
 	public boolean isEnabled(Feature feature){
 		if(feature == Feature.SELF || feature == Feature.ALWAYS_ON){
 			return true;
 		}
 		return config.getBoolean(feature.name().toLowerCase().trim());
 	}
 
 }
