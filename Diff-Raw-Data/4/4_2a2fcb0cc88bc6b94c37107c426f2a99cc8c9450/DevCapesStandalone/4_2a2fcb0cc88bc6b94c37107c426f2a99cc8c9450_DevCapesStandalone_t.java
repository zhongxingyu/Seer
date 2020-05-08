 /**
 * DeveloperCapes by Jadar
 * License: MIT License (https://raw.github.com/jadar/DeveloperCapes/master/LICENSE)
  * version 2.0
  */
 package com.jadarstudios.developercapes.standalone;
 
 import static argo.jdom.JsonNodeBuilders.aStringBuilder;
 
 import java.io.InputStream;
 import java.io.InputStreamReader;
 
 import argo.jdom.JdomParser;
 import argo.jdom.JsonRootNode;
 import argo.saj.InvalidSyntaxException;
 
 import com.google.common.base.Strings;
 import com.google.common.base.Throwables;
 import com.jadarstudios.developercapes.DevCapesUtil;
 
 import cpw.mods.fml.common.Mod;
 import cpw.mods.fml.common.Mod.EventHandler;
 import cpw.mods.fml.common.Mod.Instance;
 import cpw.mods.fml.common.event.FMLInitializationEvent;
 import cpw.mods.fml.common.event.FMLPreInitializationEvent;
 
 @Mod(name="DeveloperCapes", modid="devcapesstandalone", version="2.0")
 public class DevCapesStandalone {
 
 	@Instance
 	public static DevCapesStandalone instance;
 	
 	private static JdomParser parser = new JdomParser();
 	
 	private String capeTextUrl = "";
 	
 	/**
 	 * Loads information from jar JSON file.
 	 * 
 	 * @param event
 	 */
 	@EventHandler
 	public void preLoad(FMLPreInitializationEvent event) {
 		InputStream is = getClass().getResourceAsStream("/capeInfo.json");
         InputStreamReader reader = new InputStreamReader(is);
         try {
             JsonRootNode root = parser.parse(reader);
 
             capeTextUrl = Strings.nullToEmpty(root.getFields().get(aStringBuilder("capeTxtUrl")).getStringValue());
 
         } catch (InvalidSyntaxException e) {
             System.out.println("[DevCapes] Failed to parse capeInfo.json. This means you could have a corrupt mod jar.");
         } catch (Exception e) {
             throw Throwables.propagate(e);
         }
 	}
 	
 	/**
 	 * Sets up DeveloperCapes.
 	 * 
 	 * @param event
 	 */
 	@EventHandler
 	public void load(FMLInitializationEvent event) {
 		if(!Strings.isNullOrEmpty(capeTextUrl))
 			DevCapesUtil.getInstance().addFileUrl(capeTextUrl);
 	}
 	
 }
