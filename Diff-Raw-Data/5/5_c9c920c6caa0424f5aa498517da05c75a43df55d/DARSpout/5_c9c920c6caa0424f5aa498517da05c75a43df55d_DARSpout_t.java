 package muCkk.DeathAndRebirth;
 
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.Plugin;
 import org.getspout.spoutapi.SpoutManager;
 import org.getspout.spoutapi.player.AppearanceManager;
 import org.getspout.spoutapi.player.SpoutPlayer;
 import org.getspout.spoutapi.sound.SoundManager;
 
 public final class DARSpout {
 
 	private static String sound_death = "http://dl.dropbox.com/u/12769915/minecraft/plugins/DAR/sounds/death.wav";
 	private static String sound_res = "http://dl.dropbox.com/u/12769915/minecraft/plugins/DAR/sounds/res.wav";
 	
 	private static String skin_ghost = "http://dl.dropbox.com/u/12769915/minecraft/plugins/DAR/skins/ghost1.png";
 	
 	/**
 	 * Called when a player dies
 	 * Sets the skin to a ghost skin and plays a sound
 	 * @param player which dies
 	 */
 	public static void playerDied(Player player) {
		SpoutPlayer sp = SpoutManager.getPlayer(player);
 		Plugin spoutPlugin = player.getServer().getPluginManager().getPlugin("SpoutTester");
 		
 		// *** Sound effect ***
 		SoundManager soundM = SpoutManager.getSoundManager();
 		soundM.playCustomSoundEffect(spoutPlugin, sp, sound_death, false);		
 		
 		// TODO !!! sky effect when spout releases it
 	}
 	
 	/**
 	 * Called when a player is resurrected
 	 * Resets the skin and plays a sound
 	 * @param player who gets resurrected
 	 */
 	public static void playerRes(Player player) {
		SpoutPlayer sp = SpoutManager.getPlayer(player);
 		
 		// *** Skin ***
 		AppearanceManager appearanceM = SpoutManager.getAppearanceManager();
 		appearanceM.resetGlobalSkin(sp);
 		playResSound(player);
 		// TODO !!! sky effect when spout releases it		
 	}
 	
 	public static void playResSound(Player player) {
 		Plugin spoutPlugin = player.getServer().getPluginManager().getPlugin("SpoutTester");
 		SoundManager soundM = SpoutManager.getSoundManager();
 		SpoutPlayer sPlayer = SpoutManager.getPlayer(player);
 		soundM.playCustomSoundEffect(spoutPlugin, sPlayer, sound_res, false);
 	}
 
 	/**
 	 * Changes the skin ofthe player to a ghost skin.
 	 * The thread is needed because minecraft resets the players skin on respawn.
 	 * @param player which gets a new skin
 	 */
 	public static void setGhostSkin(final Player player) {
 		// wait for the player to spawn		
 		new Thread() {
 			@Override
 			public void run() {				
 				try {
 					Thread.sleep(1500);
 				} catch (InterruptedException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 				// *** Skin ***
 				SpoutPlayer sPlayer = SpoutManager.getPlayer(player);
 				AppearanceManager appearanceM = SpoutManager.getAppearanceManager();
 				appearanceM.setGlobalSkin(sPlayer, skin_ghost);
 			}
 		}.start();
 	}
 }
