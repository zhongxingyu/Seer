 package com.volumetricpixels.vitals.main.commands;
 
 import java.util.List;
 
 import org.spout.api.chat.ChatSection;
 import org.spout.api.chat.style.ChatStyle;
 import org.spout.api.command.CommandContext;
 import org.spout.api.command.CommandSource;
 import org.spout.api.command.annotated.Command;
 import org.spout.api.command.annotated.CommandPermissions;
 import org.spout.api.exception.CommandException;
 import org.spout.api.geo.World;
 import org.spout.vanilla.data.Weather;
import org.spout.vanilla.entity.VanillaController;
 import org.spout.vanilla.entity.world.VanillaSky;
 
 public class AdminCommands {
 
 	@Command(aliases = {"weather"}, usage = "<sunny/rainy/storm/check>", desc = "Change or check the weather.", min = 1, max = 1)
 	@CommandPermissions("vitals.weather")
 	public void weather(CommandContext args, CommandSource source) throws CommandException {
		if(source instanceof VanillaController) {
 			//Setting the entity
			VanillaController player = (VanillaController) source;
 			
 			//Setting the world
 			World world = player.getParent().getWorld();
 			
 			//Getting the VanillaSky
 			VanillaSky sky = VanillaSky.getSky(world);
 			
 			//Get all the args from a command
 			List<ChatSection> csl = args.getRawArgs();
 			
 			//Checking all the stuff.
 			if(csl.size() != 1) {
 				source.sendMessage(ChatStyle.GRAY, "Usage: /weather <sunny/rainy/storm/check>");
 			} else {
 				if(csl.get(0).equals("sunny") && source.hasPermission("vitals.weather.set")) {
 					sky.setWeather(Weather.CLEAR);
 					source.sendMessage(ChatStyle.GRAY, "Weather set to sunny!");
 				} else if(csl.get(0).equals("rainy") && source.hasPermission("vitals.weather.set")) {
 					sky.setWeather(Weather.RAIN);
 					source.sendMessage(ChatStyle.GRAY, "Weather set to rainy!");
 				} else if(csl.get(0).equals("storm") && source.hasPermission("vitals.weather.set")) {
 					sky.setWeather(Weather.THUNDERSTORM);
 					source.sendMessage(ChatStyle.GRAY, "Weather set to storm!");
 				} else if(csl.get(0).equals("check") && source.hasPermission("vitals.weather.check")) {
 					Weather weather = sky.getWeather();
 					source.sendMessage(ChatStyle.GRAY, "The current weather is " + weather + ".");
 				} else {
 					source.sendMessage(ChatStyle.GRAY, "Incorrect syntax! Usage: /weather <sunny/rainy/storm/check>");
 				}	
 			}
 		}
 	}
 }
