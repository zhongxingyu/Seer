 package me.oceanor.OceManaBar;
 
 import java.io.File;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.logging.Logger;
 
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.getspout.spoutapi.gui.Color;
 import org.getspout.spoutapi.gui.GenericGradient;
 import org.getspout.spoutapi.gui.GenericLabel;
 
 
 
 public class OceManaBar extends JavaPlugin
 {
     public static OceManaBar plugin;
     public OceListener ManaChangeListener = new OceListener(this);
     public OceManaBarSpoutListener SpoutListener = new OceManaBarSpoutListener(this);
     public OceCommandHandler CommandExecutor;
     
     public final static Logger logger = Logger.getLogger("Minecraft");
     
     public static boolean enabled;
     public static int manabarType; 
     public static boolean useTexture;
     public static boolean useAscii;
     public static boolean showNumeric;
     public static int posX;
     public static int posY;
     public static int height;
     public static int width;
     public static int size;
     public static int maxMana;
     public static String segmentChar;
     public static Color gradient1;
     public static Color gradient2;
     public static Color bgcolor1;
     public static Color bgcolor2;
 
     public static Boolean hasSpout, hasMagicSpells;
     
     public static FileConfiguration PlayerConfig = null;
     public static File PlayerConfigurationFile = null;
     
     public static Map<Player, GenericLabel> asciibars = new HashMap<Player, GenericLabel>();
     public static Map<Player, GenericGradient> gradientbars = new HashMap<Player, GenericGradient>();
     public static Map<Player, GenericGradient> backgrounds = new HashMap<Player, GenericGradient>();
     public static Map<Player, GenericLabel> numericmanas = new HashMap<Player, GenericLabel>();
 
     @Override
     public void onDisable() 
     {
         this.getServer().getScheduler().cancelTasks(this);
         logger.info("[Ocemanabar] disabled");
     }
 
     public void onEnable() 
     {
         plugin = this;
         PluginManager pm = getServer().getPluginManager();
         
         CommandExecutor = new OceCommandHandler(this);
         getCommand("ocemanabar").setExecutor(CommandExecutor);
         getCommand("manabar").setExecutor(CommandExecutor);
         
         loadConfiguration();
         hasSpout = this.getServer().getPluginManager().isPluginEnabled("Spout");
         hasMagicSpells = this.getServer().getPluginManager().isPluginEnabled("MagicSpells");
 
         if (hasSpout) 
         {
             pm.registerEvents(this.SpoutListener, this);
             logger.info("[Ocemanabar] Spout found");
         }
         else
             logger.info("[Ocemanabar] ERROR: you need spout to get this working!");
 
         if (hasMagicSpells) 
         {
             pm.registerEvents(this.ManaChangeListener, this);
             logger.info("[Ocemanabar] MagicSpells found");
         }
         else
             logger.info("[Ocemanabar] ERROR: you need MagicSpells to have this working!");
         
         logger.info("[Ocemanabar] enabled");
     }
     
 
     public void loadConfiguration() 
     {
         getConfig().options().copyDefaults(true);
 
         String headertext;
         headertext = "Default OceManaBar Config file\r\n\r\n";
         headertext += "enabled: [true|false] - easy enable or disable this plugin without remove it.\r\n";
         headertext += "manabarType: [1|2]\r\n";
         headertext += "    1: ascii.  \r\n";
         headertext += "    2: textures.\r\n";
         headertext += "maxMana: [number] - Copy here the value of max mana of your MagicSpells config file (default 100).\r\n";
         headertext += "showNumeric: [true|false] - choose if add a number after the mana bar with [CurrentMana]/[TotalMana].\r\n";
         headertext += "posX: [number] - Set X position of mana bar.\r\n";
         headertext += "posY: [number] - Set Y position of mana bar.\r\n";
         headertext += "height: [number] - Set height of mana bar. WARNING: if you are using textures, do not use a number less than 8\r\n";
         headertext += "width: [number] - Set widht of mana bar.  WARNING: if you are using textures, do not use a number less than 4\r\n";
         headertext += "segmentChar: [number] - For ascii mana bar, set wich characters it have to use.\r\n";
         headertext += "size: [number] - For ascii mana bar, set how many characters it have to print.\r\n";
         headertext += "\r\n";
 
         getConfig().options().header(headertext);
         getConfig().options().copyHeader(true);
 
         enabled = getConfig().getBoolean("enabled");
         manabarType = getConfig().getInt("manabarType");
         if(manabarType == 1)
         {
             useAscii = true;
             useTexture = false;
         }
         else
         {
             useAscii = false;
             useTexture = true;    
         }
         maxMana = getConfig().getInt("maxMana");
         showNumeric = getConfig().getBoolean("showNumeric");
         posX = getConfig().getInt("posX");
         posY = getConfig().getInt("posY");
         height = getConfig().getInt("height");
         width = getConfig().getInt("width");
         gradient1 = hexToRgb(getConfig().getString("textureColor1"));
         gradient2 = hexToRgb(getConfig().getString("textureColor2"));
         bgcolor1 = hexToRgb(getConfig().getString("backgroundColor1"));
         bgcolor2 = hexToRgb(getConfig().getString("backgroundColor2"));
         segmentChar = getConfig().getString("segmentChar");
         size = getConfig().getInt("size");
 
         if(manabarType == 2 && height < 8)
             height = 8;
         if(manabarType == 2 && width < 4)
             width = 4;
 
         saveConfig();
     }
 
     public static Color hexToRgb(String hexcolor)
     {
        return new Color(Integer.valueOf(hexcolor.substring( 0, 2 ), 16 ),
                         Integer.valueOf(hexcolor.substring( 2, 4 ), 16 ),
                         Integer.valueOf(hexcolor.substring( 4, 6 ), 16 ));
     }
 }
