 import java.util.Random;
 import java.util.logging.Logger;
 
 public class Behead extends Plugin {
     public static final String  NAME    = Behead.class.getSimpleName();
    public static final String  VERSION = "0.1";
     public static final String  AUTHOR  = "14mRh4X0r";
     public static final Logger  LOG     = Logger.getLogger("Minecraft.Behead");
     public static final Random  RANDOM  = new Random();
     public final PropertiesFile PROPS   = this.getPropertiesFile();
     public PluginRegisteredListener prl;
     public final PluginListener PL      = new PluginListener() {
         @Override
         public void onDeath(LivingEntity living) {
             if (!(living instanceof Player)) {
                 return;
             }
             Player player = (Player) living;
            if (RANDOM.nextInt(101) <= PROPS.getInt("dropchance")) {
                 Item item = new Item(Item.Type.Skull);
                 NBTTagCompound comp = new NBTTagCompound("tag");
                 comp.add("SkullOwner", player.getName());
                 item.setDataTag(comp);
                 player.giveItemDrop(item);
             }
         }
     };
 
     static {
         // Set prefix (the fancy hackish way)
         LOG.setFilter(new java.util.logging.Filter() {
             @Override
             public boolean isLoggable(java.util.logging.LogRecord record) {
                 record.setMessage("[" + NAME + "] " + record.getMessage());
                 return true;
             }
         });
     }
 
     public void enable() {
         prl = etc.getLoader().addListener(PluginLoader.Hook.DEATH,
                                           this.PL, this,
                                           PluginListener.Priority.MEDIUM);
         LOG.info(String.format("Version %s by %s loaded.", VERSION, AUTHOR));
     }
 
     public void disable() {
         etc.getLoader().removeListener(prl);
         LOG.info("Disabled.");
     }
 
 }
