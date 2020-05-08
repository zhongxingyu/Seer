 package com.ftwinston.Killer.CopyExistingWorld;
 
 import java.util.Iterator;
 import java.util.LinkedHashMap;
 import java.util.Map;
 
import org.bukkit.configuration.ConfigurationSection;

 import com.ftwinston.Killer.Killer;
 import com.ftwinston.Killer.WorldConfig;
 import com.ftwinston.Killer.WorldOption;
 import com.ftwinston.Killer.WorldOptionPlugin;
 
 public class Plugin extends WorldOptionPlugin
 {
 	public void onEnable()
 	{
 		loadOptions();
 		Killer.registerWorldOption(this);
 	}
 	
 	@Override
 	public WorldOption createInstance()
 	{
 		return new CopyExistingWorld();
 	}
 	
 	private static Option[] options = null;
 	
 	private void loadOptions()
 	{
		ConfigurationSection section = getConfig().getConfigurationSection("worlds");
		Map<String, Object> optionMap = section == null ? null : section.getValues(false);
 		
 		// if we don't have anything specified, add some defaults
 		if ( optionMap == null || optionMap.size() == 0 )
 		{
 			optionMap = new LinkedHashMap<String, Object>();
 			optionMap.put("Ice island", "90296315345015956");
 			optionMap.put("Tropical island", "Artomix");
 			optionMap.put("Jungle temple island", "-6123363264721061161");
 			optionMap.put("One tree", "-7202837769721758374");
 			optionMap.put("Two trees", "1114752357");
 			optionMap.put("No trees", "4");
 			optionMap.put("Island village", "-2089467411");
 			optionMap.put("Mountain island", "7619013540131867193");
 			optionMap.put("Village and cliffs", "-2056320673894174252");
 			optionMap.put("Pyramid and temple", "-2866479247629964952");
 			optionMap.put("Peninsula", "The Election");
 			optionMap.put("Nearby mushroom biome", "5596653170798224322");
 			
 			getConfig().createSection("worlds", optionMap);
 			saveConfig();
 		}
 		
 		options = new Option[optionMap.size()];
 		Iterator<Map.Entry<String, Object>> it = optionMap.entrySet().iterator();
 		int num = 0;
 		while ( it.hasNext() )
 		{
 			Map.Entry<String, Object> entry = (Map.Entry<String, Object>)it.next();
 			options[num++] = new Option(entry.getKey(), (String)entry.getValue());
 		}
 	}
 	
 	static int getNumOptions() { return options.length; }
 	
 	static String getOptionName(int num)
 	{
 		return options[num].getName();
 	}
 	
 	static long getOptionSeed(int num)
 	{
 		return WorldConfig.getSeedFromString(options[num].getSeed());
 	}
 	
 	private class Option
 	{
 		public Option(String name, String seed)
 		{
 			this.name = name;
 			this.seed = seed;
 		}
 		
 		private String name, seed;
 		String getName() { return name; }
 		String getSeed() { return seed; }
 	}
 }
