 package us.fitzpatricksr.cownet.commands;
 
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.entity.Player;
 import org.bukkit.util.Vector;
 import us.fitzpatricksr.cownet.CowNetThingy;
 
 import java.util.List;
 
 public class Bounce extends CowNetThingy {
 	private static final int MAX_RADIUS = 100;
 	private static final int MAX_VELOCITY = 5;
 	@Setting
 	private int radius = 5;
 	@Setting
 	private int velocity = 1;
 
 	@Override
 	protected String getHelpString(CommandSender sender) {
 		return "usage: bounce [radius] [velocity]";
 	}
 
 	@CowCommand
 	protected boolean doBounce(Player player) {
 		return doBounce(player, String.valueOf(radius), String.valueOf(velocity));
 	}
 
 	@CowCommand
 	protected boolean doBounce(Player player, String radius) {
        return doBounce(player, radius, String.valueOf(velocity));
 	}
 
 	@CowCommand
 	protected boolean doBounce(Player player, String radiusStr, String velocityStr) {
 		int radius;
 		try {
 			radius = Integer.parseInt(radiusStr);
 		} catch (Exception e) {
 			player.sendMessage("usage: radius must be a number between 1 and " + MAX_RADIUS);
 			return true;
 		}
 		if (radius < 1 || radius > MAX_RADIUS) {
 			player.sendMessage("usage: radius must be a number between 1 and " + MAX_RADIUS);
 			return true;
 		}
 		int velocity;
 		try {
 			velocity = Integer.parseInt(velocityStr);
 		} catch (Exception e) {
 			player.sendMessage("usage: velocity must be a number between 1 and " + MAX_VELOCITY);
 			return true;
 		}
 		if (velocity < 1 || velocity > MAX_RADIUS) {
 			player.sendMessage("usage: velocity must be a number between 1 and " + MAX_VELOCITY);
 			return true;
 		}
 		final Vector vector = new Vector(0, velocity, 0);
 		List<Entity> entities = player.getNearbyEntities(radius, radius, radius);
 		if (entities.size() == 0) {
 			player.sendMessage("Nothing to bounce within a radius of " + radius);
 		} else {
 			player.sendMessage("Bouncing creatures " + velocity + " within a radius of " + radius);
 			for (Entity entity : entities) {
 				if (entity instanceof LivingEntity) {
 					LivingEntity livingEntity = (LivingEntity) entity;
 					livingEntity.setVelocity(vector);
 				}
 			}
 		}
 		return true;
 	}
 }
 
