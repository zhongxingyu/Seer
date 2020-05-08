 package net.catharos.clib.util;
 
 import org.bukkit.Effect;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.Sound;
 import org.bukkit.World;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.entity.Player;
 import org.bukkit.potion.PotionEffect;
 import org.bukkit.potion.PotionEffectType;
 
 public class FxUtil {
 	/* --------------- General effects --------------- */
 
 	/**
 	 * Plays a generic effect at a given location with a given data
 	 * 
	 * @see <a href="http://www.wiki.vg/Protocol">www.wiki.vg/Protocol</a>
 	 * 
 	 * @param player
 	 *            The player to apply the effect on
 	 * @param loc
 	 *            The location to play the effect
 	 * @param effect
 	 *            The effect to play
 	 * @param data
 	 *            The data given to the effect
 	 */
 	public static void playEffect( Player player, Location loc, Effect effect, int data ) {
 		player.getWorld().playEffect( player.getLocation(), effect, data );
 	}
 
 	/**
 	 * Plays a generic effect at a given location with a given data in a
 	 * specific radius
 	 * 
	 * @see <a href="http://www.wiki.vg/Protocol">www.wiki.vg/Protocol</a>
 	 * 
 	 * @param player
 	 *            The player to apply the effect on
 	 * @param loc
 	 *            The location to play the effect
 	 * @param effect
 	 *            The effect to play
 	 * @param data
 	 *            The data given to the effect
 	 * @param radius
 	 *            The radius of the effect
 	 */
 	public static void playEffect( Player player, Location loc, Effect effect, int data, int radius ) {
 		player.getWorld().playEffect( player.getLocation(), effect, data, radius );
 	}
 
 	/* ---------------- Potion effects --------------- */
 
 	/**
 	 * Applies a potion effect to an entity
 	 * 
 	 * @param entity
 	 *            The entity to apply the effect on
 	 * @param effect
 	 *            The effect to apply
 	 * @param duration
 	 *            The effect duration
 	 */
 	public static void applyPotionEffect( LivingEntity entity, PotionEffectType effect, int duration ) {
 		applyPotionEffect( entity, effect, duration, 1 );
 	}
 
 	/**
 	 * Applies a potion effect to an entity
 	 * 
 	 * @param entity
 	 *            The entity to apply the effect on
 	 * @param effect
 	 *            The effect to apply
 	 * @param duration
 	 *            The effect duration
 	 * @param amp
 	 *            The effect amplitude
 	 */
 	public static void applyPotionEffect( LivingEntity entity, PotionEffectType effect, int duration, int amp ) {
 		entity.addPotionEffect( new PotionEffect( effect, duration, amp ), true );
 	}
 
 	/* ---------------- Sound effects ---------------- */
 
 	public static void playSound( Player player, Sound sound ) {
 		FxUtil.playSound( player, sound, 1, 0 );
 	}
 
 	public static void playSound( Player player, Sound sound, float vol ) {
 		FxUtil.playSound( player, sound, vol, 0 );
 	}
 
 	public static void playSound( Player player, Sound sound, float vol, float pitch ) {
 		FxUtil.playSound( player, player.getLocation(), sound, vol, pitch );
 	}
 
 	public static void playSound( Player player, Location loc, Sound sound, float vol, float pitch ) {
 		player.getWorld().playSound( loc, sound, vol, pitch );
 	}
 
 	/* ---------------- Special effects --------------- */
 
 	/**
 	 * Spawns particles of a specific material.<br />
 	 * <b>Important:</b> The following materials will not get spawned because of
 	 * a client crash: <code>REDSTONE</code>.
 	 * 
 	 * @see Material
 	 * 
 	 * @param world
 	 *            The world where the particles should be spawned in
 	 * @param loc
 	 *            The location to spawn the particles
 	 * @param mat
 	 *            The particle material
 	 */
 	public static void spawnParticles( World world, Location loc, Material mat ) {
 		switch (mat) {
 		case REDSTONE:
 			return;
 		default:
 			world.playEffect( loc, Effect.STEP_SOUND, mat.getId() );
 		}
 	}
 }
