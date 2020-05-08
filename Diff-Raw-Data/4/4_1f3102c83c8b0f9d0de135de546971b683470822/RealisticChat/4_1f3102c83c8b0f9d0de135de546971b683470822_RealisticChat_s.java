 package me.exphc.RealisticChat;
 
 
 import java.util.logging.Logger;
 import java.util.Set;
 import java.util.HashSet;
 import java.util.ArrayList;
 import java.util.Random;
 
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.plugin.*;
 import org.bukkit.entity.*;
 import org.bukkit.event.*;
 import org.bukkit.event.player.*;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.ShapedRecipe;
 import org.bukkit.enchantments.*;
 import org.bukkit.*;
 
 class RealisticChatListener implements Listener {
     RealisticChat plugin;
     Random random;
 
     public RealisticChatListener(RealisticChat plugin) {
         this.random = new Random();
 
         this.plugin = plugin;
         Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
     }
 
     @EventHandler(priority = EventPriority.NORMAL)
     public void onPlayerChat(PlayerChatEvent event) {
         Player sender = event.getPlayer();
         String message = event.getMessage();
         ArrayList<String> sendInfo = new ArrayList<String>();
 
         // TODO: change to sound pressure level instead of distance based
         // see http://en.wikipedia.org/wiki/Sound_pressure#Examples_of_sound_pressure_and_sound_pressure_levels
         // for added realism, also to allow 130 dB = threshold of pain (player damage), nausea, http://en.wikipedia.org/wiki/Sound_cannon
         double speakingRange = plugin.getConfig().getDouble("speakingRangeMeters", 50.0);
         double garbleRangeDivisor = plugin.getConfig().getDouble("garbleRangeDivisor", 2.0); // so last 1/2.0 (half) of distance, speech is unclear
 
         // Yelling costs hunger and increases range
         int yell = countExclamationMarks(message);
         if (yell > 0) {
             yell = Math.min(yell, plugin.getConfig().getInt("yellMax", 4));
 
             sendInfo.add("yell="+yell);
 
             final int defaultHunger[] = { 1, 2, 4, 20 };
             final double defaultRangeIncrease[] = { 10.0, 50.0, 100.0, 500.0 };
 
             int hunger = plugin.getConfig().getInt("yell."+yell+".hunger", defaultHunger[yell - 1]);
             // TODO: check food level first, and clamp yell if insufficient (or take away health hearts too?)
             sender.setFoodLevel(sender.getFoodLevel() - hunger);
 
             speakingRange += plugin.getConfig().getDouble("yell."+yell+".rangeIncrease", defaultRangeIncrease[yell - 1]);
         }
 
         // Whispering decreases range
         int whisper = countParenthesizeNests(message);
         if (whisper > 0) {
             sendInfo.add("whisper="+whisper);
 
             speakingRange -= plugin.getConfig().getDouble("whisperRangeDecrease", 40.0) * whisper;
             if (speakingRange < 1) {
                 speakingRange = 1;
             }
         }
 
         // Megaphone
         if (hasMegaphone(sender)) {
             sendInfo.add("send-mega");
             // calculated in recipient
         }
 
         // Log that the player tried to talk
         sendInfo.add("r="+speakingRange);
         plugin.log.info("<" + sender.getName() + ": "+joinList(sendInfo)+"> "+message);
 
         // Send to recipients
         for (Player recipient: event.getRecipients()) {
             ArrayList<String> recvInfo = new ArrayList<String>();
 
             // TODO: special item to hold, to receive all or send to all (infinity compass?)
 
             if (sender.equals(recipient)) {
                 // Talking to ourselves
                 // TODO: still garble? if talking through something
                 deliverMessage(recipient, sender, message, recvInfo);
                 continue;
             }
 
             if (!sender.getWorld().equals(recipient.getWorld())) {
                 // Not in this world!
                 // TODO: cross-world communication device?
                 continue;
             }
 
             double distance = sender.getLocation().distance(recipient.getLocation());
             recvInfo.add("d="+distance);
 
             // Talking into walkie-talkie device
             if (hasWalkieTalking(sender) && hasWalkieListening(recipient)) {
                 ArrayList<String> recvInfoWalkie = new ArrayList<String>(recvInfo);
 
                 double walkieMaxRange = plugin.getConfig().getDouble("walkieRangeMeters", 2000.0);
                 double walkieGarbleDivisor = plugin.getConfig().getDouble("walkieGarbleDivisor", 2.0);
                 double walkieClearRange = walkieMaxRange / walkieGarbleDivisor;
 
                 recvInfoWalkie.add("walkie="+walkieMaxRange+"/"+walkieClearRange);
 
                 if (distance < walkieClearRange){
                     // Coming in loud and clear!
                     // TODO: show as from walkie-talkie, but with callsign of sender? (instead of "foo:[via walkie]" "walkie:foo")
                     deliverMessage(recipient, sender, "[via walkie] " + message, recvInfoWalkie);
                 } else if (distance < walkieMaxRange) {
                     // Can't quite make you out..
                     double walkieClarity = 1 - ((distance - walkieClearRange) / walkieMaxRange);
 
                     recvInfoWalkie.add("wc="+walkieClarity);
 
                 	deliverMessage(recipient, sender, "[via walkie] " + garbleMessage(message, walkieClarity), recvInfoWalkie);
                 }
                 // also fall through and deliver message locally
             }
 
             double hearingRange = speakingRange;
             
             if (hasMegaphone(sender)) {
             	/*
             	 * actSlop is measured in yaw, which itself is measured in 256 degrees, instead of
             	 * 360. actSlop is the degrees yaw of the angle from sender to rec. 
             	 * micSlop is the yaw angle that the sender is facing. the equation in the if
             	 * statement on the left hand side of the "less than" symbol gives the difference between the 
             	 * two slopes and the right hand side of the if statement checks to make sure the difference
             	 * is less than 35 degrees by default. This makes sure the receiving player is in the "sound cone".
             	 * If so, the megaphone multipier is applied to the hearingRange of this receiving player.
             	 */
             	double deltaZ = recipient.getLocation().getZ() - sender.getLocation().getZ();
             	double deltaX = recipient.getLocation().getX() - sender.getLocation().getX();
             	double actSlop = 0;
             	if (deltaZ <= 0 && deltaX >= 0)
             		actSlop = Math.tan(deltaX/(-1*deltaZ))*(360/(2*3.14159));
             	if (deltaZ >= 0 && deltaX > 0)
             		actSlop = Math.tan(deltaZ/(deltaX))*(360/(2*3.14159))+90;
             	if (deltaZ > 0 && deltaX <= 0)
             		actSlop = Math.tan((-1*deltaX)/(deltaZ))*(360/(2*3.14159))+180;
             	if (deltaZ <= 0 && deltaX < 0)
             		actSlop = Math.tan((-1*deltaZ)/(-1*deltaX))*(360/(2*3.14159))+270;
             	double micSlop = sender.getLocation().getYaw() * (360/256);
 
                 recvInfo.add("mega-actSlop=" + actSlop);
                 recvInfo.add("mega-micSlop=" + micSlop);
                 recvInfo.add("mega-deltaZ=" + deltaZ);
                 recvInfo.add("mega-deltaX=" + deltaX);
 
                 // 70 degrees is the default Minecraft FOV; divide it in half to get 35 degree width on each side.
             	if (Math.abs(micSlop - actSlop) < (plugin.getConfig().getDouble("megaphoneWidthDegrees", 70.0))){
                     recvInfo.add("heard-mega");
             		hearingRange *= plugin.getConfig().getDouble("megaphoneFactor", 2.0);
             	}
             }
 
             // Ear trumpets increase hearing range only
             double earTrumpetRange = getEarTrumpetRange(recipient);
             if (earTrumpetRange != 0) {
                 recvInfo.add("ear="+earTrumpetRange);
                 hearingRange += earTrumpetRange;
             }
             recvInfo.add("hr="+hearingRange);
 
             // Limit distance
             if (distance > hearingRange) {
                 continue;
             }
 
             double clearRange = hearingRange / garbleRangeDivisor;
 
             if (distance > clearRange) {
                 // At distances speakingRangeMeters..garbleRangeMeters (50..25), break up
                 // with increasing probability the further away they are.
                 // 24 = perfectly clear
                 // 25 = slightly garbled
                 // (distance-25)/50
                 // 50 = half clear
                 // TODO: different easing function than linear?
                 double noise = (distance - clearRange) / hearingRange;
                 double clarity = 1 - noise;
 
                 recvInfo.add("clarity="+clarity);
 
                 deliverMessage(recipient, sender, garbleMessage(message, clarity), recvInfo);
             } else {
                 deliverMessage(recipient, sender, message, recvInfo);
             }
         }
 
         // Deliver the message manually, so we can customize the chat display 
         event.setCancelled(true);
     }
 
     /** Get whether the player has a walkie-talkie ready to talk into.
     */
     private boolean hasWalkieTalking(Player player) {
         if (!plugin.getConfig().getBoolean("walkieEnable", true)) {
             return false;
         }
 
         ItemStack held = player.getItemInHand();
         
         return held != null && held.getTypeId() == plugin.walkieItemId;
     }
 
     /** Get whether the player has a walkie-talkie ready for listening.
     */
     private boolean hasWalkieListening(Player player) {
         if (!plugin.getConfig().getBoolean("walkieEnable", true)) {
             return false;
         }
 
         ItemStack[] contents = player.getInventory().getContents();
 
         final int HOTBAR_SIZE = 9;
         // Player can hear walkie if placed anywhere within their hotbar slots (not elsewhere)
         for (int i = 0; i < HOTBAR_SIZE; i += 1) {
             ItemStack item = contents[i];
 
             if (item != null && item.getTypeId() == plugin.walkieItemId) {
                 return true;
             }
         }
 
         return false;
     }
 
     /** Get whether the player has a megaphone to talk into.
     */
     private boolean hasMegaphone(Player player) {
         if (!plugin.getConfig().getBoolean("megaphoneEnable", true)) {
             return false;
         }
 
         ItemStack held = player.getItemInHand();
 
         return held != null && held.getTypeId() == plugin.megaphoneItemId;
     }
 
     /** Get the range increase of the ear trumpet the player is wearing, or 0 if none.
      Inspired by http://en.wikipedia.org/wiki/Ear_trumpet - 1600s precursor to modern electric hearing aid
     */
     private double getEarTrumpetRange(Player player) {
         ItemStack ear = getEarTrumpet(player);
 
         if (ear == null) {
             return 0;
         }
 
         int level = ear.getEnchantmentLevel(EFFICIENCY);
         if (level > 3) {
             level = 3;
         }
 
         final double[] defaultRanges = { 100.0, 150.0, 400.0 };
         double range = plugin.getConfig().getDouble("earTrumpet."+level+".rangeIncrease", defaultRanges[level - 1]);
 
         return range;
     }
 
     final private static Enchantment EFFICIENCY = Enchantment.DIG_SPEED;
 
     /** Get the ear trumpet item the player is wearing, or null.
     */
     private ItemStack getEarTrumpet(Player player) {
         if (!plugin.getConfig().getBoolean("earTrumpetEnable", true)) {
             return null;
         }
 
         ItemStack helmet = player.getInventory().getHelmet();
 
         if (helmet != null && helmet.getType() == Material.GOLD_HELMET && helmet.containsEnchantment(EFFICIENCY)) {
             return helmet;
         } else {
             return null;
         }
     }
 
 
     private String joinList(ArrayList<String> list) {
         StringBuilder sb = new StringBuilder();
         for (String item: list) {
             sb.append(item + ",");
         }
         String s = sb.toString();
         if (s.length() == 0) {
             return "";
         } else {
             return s.substring(0, s.length() - 1);
         } 
     }
 
     /** Count number of trailing exclamation marks
      */
     private int countExclamationMarks(String message) {
         int yell = 0;
         while (message.length() > 1 && message.endsWith("!")) {
             message = message.substring(0, message.length() - 1);
             yell += 1;
         }
 
         return yell;
     }
   
     /** Count number of nested surrounding parenthesizes
      */
     private int countParenthesizeNests(String message) {
         int whisper = 0;
         while (message.length() > 2 && message.startsWith("(") && message.endsWith(")")) {
             message = message.substring(1, message.length() - 1);
             whisper += 1;
         }
         return whisper;
     }
 
     /** Randomly garble a message (drop letters) as if it was incompletely heard.
       *
       * @param message The clear message
       * @param clarity Probability of getting through
       * @return The broken up message
       */
     private String garbleMessage(String message, double clarity) {
         // Delete random letters
         StringBuilder newMessage = new StringBuilder();
 
         // This string character iteration method is cumbersome, but it is
         // the most correct, especially if players are using plane 1 characters
         // see http://mindprod.com/jgloss/codepoint.html
         int i = 0;
         int drops = 0;
         while (i < message.length()) {
             int c = message.codePointAt(i);
             i += Character.charCount(c);
 
             if (random.nextDouble() < clarity) {
                 newMessage.appendCodePoint(c);
             } else {
                 // can't hear you..
                 if (random.nextDouble() < plugin.getConfig().getDouble("garblePartialChance", 0.10)) {
                     // barely got through (dimmed)
                     newMessage.append(plugin.dimMessageColor);
                     newMessage.appendCodePoint(c);
                     newMessage.append(plugin.messageColor);
                 } else {
                     newMessage.append(' ');
                     drops += 1;
                 }
             }
         }
         if (drops == message.length()) {
             // bad luck, message was completely obscured
             // TODO: improve conditional?; might have replaced all letters but not spaces..
             String noise = plugin.getConfig().getString("garbleAllDroppedMessage", "~~~");
             if (noise != null) {
                 return noise;
             }
         }
 
         return new String(newMessage);
     }
 
     private void deliverMessage(Player recipient, Player sender, String message, ArrayList<String> info) {
         // TODO: all configurable
         ChatColor senderColor = (sender.equals(recipient) ? plugin.spokenPlayerColor : plugin.heardPlayerColor);
         String prefix = "";
 
         if (hasMegaphone(sender)) {
             prefix = megaphoneDirection(recipient, sender);
         }
 
         String format = plugin.getConfig().getString("chatLineFormat", "player: message");
        String formattedMessage = format.replace("player", senderColor + sender.getDisplayName()).replace("message", prefix + plugin.messageColor + message);
 
         recipient.sendMessage(formattedMessage);
 
         plugin.log.info("[RealisticChat] ("+joinList(info)+") "+sender.getName() + " -> " + recipient.getName() + ": " + message);
     }
    
     /** Get the direction a megaphone-amplified message came from, if possible.
     */
     private String megaphoneDirection(Player recipient, Player sender){
         if (!plugin.getConfig().getBoolean("megaphoneEnable", true) || !hasMegaphone(sender)) {
             return "";
         }
 
         String addition = "";
         double recX = recipient.getLocation().getX();
         double recZ = recipient.getLocation().getZ();
         double senX = sender.getLocation().getX();
         double senZ = sender.getLocation().getZ();
 
         if (recZ > senZ)
             addition = addition + "[North";
         if (recZ < senZ)
             addition = addition + "[South";
         if (recX > senX)
             addition = addition + "West]";
         if (recX < senX)
             addition = addition + "East]";
 
     	return addition;
     }
 }
 
 public class RealisticChat extends JavaPlugin { 
     Logger log = Logger.getLogger("Minecraft");
     RealisticChatListener listener;
 
     int walkieItemId;
     int megaphoneItemId;
     ChatColor spokenPlayerColor, heardPlayerColor, messageColor, dimMessageColor;
 
     public void onEnable() {
         // Copy default config
         getConfig().options().copyDefaults(true);
         saveConfig();
         reloadConfig();
 
         walkieItemId = getConfigItemId("walkieItem", Material.COMPASS.getId());
         megaphoneItemId = getConfigItemId("megaphoneItem", Material.DIAMOND.getId());
 
         spokenPlayerColor = getConfigColor("chatSpokenPlayerColor", ChatColor.YELLOW);
         heardPlayerColor = getConfigColor("chatHeardPlayerColor", ChatColor.GREEN);
         messageColor = getConfigColor("chatMessageColor", ChatColor.WHITE);
         dimMessageColor = getConfigColor("chatDimMessageColor", ChatColor.DARK_GRAY);
 
         if (getConfig().getBoolean("earTrumpetEnable", true) && getConfig().getBoolean("earTrumpetEnableCrafting", true)) {
             loadRecipes();
         }
 
         listener = new RealisticChatListener(this);
     }
 
     /** Get a chat color from a configuration setting.
     */
     private ChatColor getConfigColor(String name, ChatColor defaultColor) {
         String s = getConfig().getString(name);
         if (s == null) {
             return defaultColor;
         }
 
         try {
             return ChatColor.valueOf(s);
         } catch (IllegalArgumentException e) {
             log.warning("Bad color name: " + s + ", using default: " + defaultColor + ": " + e);
             return defaultColor;
         }
     } 
 
     /** Get an item id from a configuration setting (name or numeric id) 
     */
     private int getConfigItemId(String name, int defaultId) {
         String s = getConfig().getString(name);
         if (s == null) {
             return defaultId;
         }
 
         Material material = Material.matchMaterial(s);
         if (material != null) {
             return material.getId();
         }
         try {
             return Integer.parseInt(s, 10);
         } catch (NumberFormatException e) {
             log.warning("Bad item id: " + s + ", using default: " + defaultId);
             return defaultId;
         }
     }
 
 
     public void onDisable() {
         // TODO: new recipe API to remove..but won't work in 1.1-R4 so I can't use it on ExpHC yet :(
     }
 
     final private static Enchantment EFFICIENCY = Enchantment.DIG_SPEED;
 
     private void loadRecipes() {
         ItemStack earTrumpetWoodItem = new ItemStack(Material.GOLD_HELMET, 1);
         ItemStack earTrumpetLeatherItem = new ItemStack(Material.GOLD_HELMET, 1);
         ItemStack earTrumpetIronItem = new ItemStack(Material.GOLD_HELMET, 1);
 
         // TODO: add workaround BUKKIT-602 for 1.1-R4 
         // see https://github.com/mushroomhostage/SilkSpawners/commit/0763f29f217662c97a0b4a155649e14e8beb92c9
         // https://bukkit.atlassian.net/browse/BUKKIT-602 Enchantments lost on crafting recipe output
         earTrumpetWoodItem.addUnsafeEnchantment(EFFICIENCY, 1);
         earTrumpetLeatherItem.addUnsafeEnchantment(EFFICIENCY, 2);
         earTrumpetIronItem.addUnsafeEnchantment(EFFICIENCY, 3);
 
         ShapedRecipe earTrumpetWood = new ShapedRecipe(earTrumpetWoodItem);
         ShapedRecipe earTrumpetLeather = new ShapedRecipe(earTrumpetLeatherItem);
         ShapedRecipe earTrumpetIron = new ShapedRecipe(earTrumpetIronItem);
 
          earTrumpetWood.shape(
             "WWW",
             "WDW");
         earTrumpetWood.setIngredient('W', Material.WOOD);   // planks
         earTrumpetWood.setIngredient('D', Material.DIAMOND);
         Bukkit.addRecipe(earTrumpetWood);
 
         earTrumpetLeather.shape(
             "LLL",
             "LDL");
         earTrumpetLeather.setIngredient('L', Material.LEATHER);
         earTrumpetLeather.setIngredient('D', Material.DIAMOND);
         Bukkit.addRecipe(earTrumpetLeather);
 
         earTrumpetIron.shape(
             "III",
             "IDI");
         earTrumpetIron.setIngredient('I', Material.IRON_INGOT);
         earTrumpetIron.setIngredient('D', Material.DIAMOND);
         Bukkit.addRecipe(earTrumpetIron);
     }
 }
