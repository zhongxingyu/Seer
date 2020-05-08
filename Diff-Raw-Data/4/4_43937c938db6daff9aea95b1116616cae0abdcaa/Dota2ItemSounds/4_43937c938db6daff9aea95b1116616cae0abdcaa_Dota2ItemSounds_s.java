 package hunternif.mc.dota2items;
 
 import net.minecraftforge.client.event.sound.SoundLoadEvent;
 import net.minecraftforge.event.ForgeSubscribe;
 
 public class Dota2ItemSounds {
 	public static final String BLINK_IN = "blink_in";
 	public static final String BLINK_OUT = "blink_out";
 	public static final String DENY_COOLDOWN = "deny_cooldown";
 	public static final String DENY_GENERAL = "deny_general";
 	public static final String MAGIC_IMMUNE = "magic_immune";
 	public static final String TREE_FALL = "tree_fall";
 	public static final String CYCLONE_START = "cyclone_start";
 	//public static final String CYCLONE_LOOP = "cyclone_loop";
 	
	public static final String[] sounds = {BLINK_IN, BLINK_OUT, DENY_COOLDOWN, DENY_GENERAL, MAGIC_IMMUNE, TREE_FALL, CYCLONE_START};
 	
 	@ForgeSubscribe
 	public void onSound(SoundLoadEvent event) {
         try {
         	for (int i = 0; i < sounds.length; i++) {
         		event.manager.soundPoolSounds.addSound(sounds[i]+".wav", Dota2Items.class.getResource("/mods/"+Dota2Items.ID+"/sounds/"+sounds[i]+".wav"));
         	}
         }
         catch (Exception e) {
             System.err.println("Failed to register one or more sounds.");
         }
     }
 }
