 package hunternif.mc.dota2items.config;
 
 import hunternif.mc.dota2items.Dota2Items;
 import hunternif.mc.dota2items.core.buff.Buff;
 import hunternif.mc.dota2items.item.Dota2Item;
 
 import java.lang.annotation.Retention;
 import java.lang.annotation.RetentionPolicy;
 import java.lang.reflect.Field;
 import java.util.ArrayList;
 import java.util.List;
 
 import net.minecraft.client.Minecraft;
 import net.minecraft.client.gui.FontRenderer;
 import net.minecraft.util.EnumChatFormatting;
 
 public class DescriptionBuilder {
 	/** Example: "+{%s%%} Mana Regeneration" <br>
 	 * Text in {} will be marked gold.
 	 * Text in [] will be marked dark gray. */
 	@Retention(RetentionPolicy.RUNTIME)
 	public static @interface BuffLineFormat {
 		String value();
 	}
 	
 	public static void build(CfgInfo<? extends Dota2Item> config) {
 		List<String> lines = new ArrayList<String>();
 		Dota2Item item = (Dota2Item) config.instance;
 		if (config.description != null && !config.description.isEmpty()) {
 			// Build list of strings from the description string
 			String description = applyColorFormatting(config.description.replace("\n", " \n "));
 			FontRenderer font = Minecraft.getMinecraft().fontRenderer;
 			String[] descrWords = description.split(" ");
 			int curLineWidth = 0;
 			StringBuilder sb = new StringBuilder();
 			for (int i = 0; i < descrWords.length; i++) {
 				int wordWidth = font.getStringWidth(descrWords[i]);
 				// At least one word will always fit:
 				if ((curLineWidth > 0 && curLineWidth + wordWidth > Dota2Item.maxTooltipWidth) || descrWords[i].equals("\n")) {
 					curLineWidth = 0;
 					lines.add(sb.toString());
 					sb = new StringBuilder();
 				}
 				if (!descrWords[i].equals("\n")) {
 					curLineWidth += font.getStringWidth(descrWords[i] + " ");
 					sb.append(descrWords[i]).append(" ");
 				}
 			}
 			if (sb.length() > 0) {
 				lines.add(sb.toString());
 			}
 		}
 		Buff buff = config.passiveBuff;
 		if (buff != null) {
 			lines.addAll(buffDescription(buff));
 		}
 		item.descriptionLines = lines;
		Dota2Items.logger.info(String.format("Built description lines for item %s", item.getLocalizedName(null)));
 	}
 	
 	public static List<String> buffDescription(Buff buff) {
 		List<String> lines = new ArrayList<String>();
 		try {
 			Field[] fields = Buff.class.getFields();
 			for (Field field : fields) {
 				if (field.isAnnotationPresent(BuffLineFormat.class)) {
 					String format = field.getAnnotation(BuffLineFormat.class).value();
 					format = applyColorFormatting(format);
 					Number value = (Number)field.get(buff);
 					if (value.doubleValue() > 0) {
 						String line = String.format(format, value);
 						lines.add(line);
 					}
 				}
 			}
 		} catch (Exception e) {
			Dota2Items.logger.warning(String.format("Failed to build description for buff %s", buff.name));
 		}
 		return lines;
 	}
 	
 	private static String applyColorFormatting(String str) {
 		return str.replace("{", EnumChatFormatting.GOLD.toString())
 				.replace("}", EnumChatFormatting.GRAY.toString())
 				.replace("[", EnumChatFormatting.DARK_GRAY.toString())
 				.replace("]", EnumChatFormatting.GRAY.toString());
 	}
 }
