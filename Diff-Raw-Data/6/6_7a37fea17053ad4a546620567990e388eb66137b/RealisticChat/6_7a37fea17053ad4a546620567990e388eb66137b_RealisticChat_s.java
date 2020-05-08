 package me.exphc.RealisticChat;
 
 
 import java.util.logging.Logger;
 import java.util.Set;
 import java.util.HashSet;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Iterator;
 import java.util.Random;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.ConcurrentSkipListSet;
 import java.util.Date;
 
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.plugin.*;
 import org.bukkit.entity.*;
 import org.bukkit.event.*;
 import org.bukkit.event.player.*;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.ShapedRecipe;
 import org.bukkit.enchantments.*;
 import org.bukkit.block.*;
 import org.bukkit.*;
 
 class SmartphoneCall {
     public static RealisticChat plugin;
     public static ConcurrentHashMap<Player, SmartphoneCall> calls = new ConcurrentHashMap<Player, SmartphoneCall>();
     public static ConcurrentHashMap<Player, SmartphoneCall> ringingCall = new ConcurrentHashMap<Player, SmartphoneCall>();
 
     //ConcurrentSkipListSet<Player> members; // not Comparable
     public HashSet<Player> members;
     public Date whenStarted, whenEnded;
 
     // set of all ringtones for each member, when call()ing someone new
     public HashSet<SmartphoneRinger> ringers;
 
     /** Create a new call, with only one player. */
     public SmartphoneCall(Player caller) {
         //members = new ConcurrentSkipListSet<Player>();
         members = new HashSet<Player>();
         members.add(caller);
 
         // unfortunately, this is the time started calling not answered..
         // will be reset in answer()
         whenStarted = new Date();
 
         // for lookup
         calls.put(caller, this);
 
         ringers = new HashSet<SmartphoneRinger>();
     }
 
     /** Try to establish a call with a new callee.
      */
     public void call(Player callee) {
         stopRinging();
 
         // If they're already holding the phone, they answer immediately!
         if (plugin.listener.holdingSmartphone(callee)) {
             answer(callee);
             return;
         }
         // Otherwise, we need to ring them until they answer
 
         // Ringing tone for everyone already online
         for (Player member: members) {
             ringers.add(new SmartphoneRinger(plugin, member));
         }
 
         SmartphoneRinger calleeRinger = new SmartphoneRinger(plugin, callee);
         // Track everyone hearing the ringing for this callee, including callee itself
         ringers.add(calleeRinger);
 
         callee.sendMessage("Incoming call from "+getNames()+", pickup a smartphone to answer");
 
         // Call which will be established if they answer!
         ringingCall.put(callee, this);
     }
 
     /** Answer (pickup) a ringing call. */
     public void answer(Player callee) {
         // TODO: what timestamp to record for 3-way (or greater) calling?
         whenStarted = new Date();
 
         ringingCall.remove(callee);
 
         stopRinging();
         members.add(callee);
 
         // for lookup
         calls.put(callee, this);
 
         // Tell everyone about the new call
         String names = getNames();
         for (Player member: members) {
             member.sendMessage("Call established: " + names);
         }
     }
 
     /** Get string of a list of names in the call. */
     public String getNames() {
         ArrayList<String> names = new ArrayList<String>();
         for (Player member: members) {
             names.add(member.getDisplayName());
         }
         return RealisticChatListener.joinList(names, ", ");
     }
 
     
     private void stopRinging() {
         for (SmartphoneRinger ringer: ringers) {
             ringer.stop();
         }
         ringers.clear();
     }
 
     /** Get the call ringing to a player, or null if no one is calling. */
     public static SmartphoneCall getRingingCall(Player callee) {
         return ringingCall.get(callee);
     }
 
     /** Find the call a player is participating in.
     */
     public static SmartphoneCall lookup(Player member) {
         return calls.get(member);
     }
 
     /** Send a message to all members on a call.
     */
     public void say(Player speaker, String message) {
         ArrayList<String> recvInfo = new ArrayList<String>();
         recvInfo.add("smartphone");
 
         for (Player member: members) {
             if (speaker != member) {
                 RealisticChatListener.deliverMessage(member, speaker, "cell" + RealisticChatListener.getDeviceTag() + message, recvInfo);
             }
         }
     }
 
     /** Destroy a call.
     */
     public void hangup() {
         whenEnded = new Date();
 
         long durationMsec = whenEnded.getTime() - whenStarted.getTime();
         long durationSeconds = (durationMsec / 1000) % 60;
         long durationMinutes = (durationMsec / 1000) / 60;   // TODO: better duration formatting?
         String duration = durationMinutes + " m " + durationSeconds + " s";
 
         for (Player member: members) {
             member.sendMessage("Hung up call, lasted " + duration);
             calls.remove(member);
         }
 
         // TODO: log calls
     }
 }
 
 /** Play a sequence of tones representing a telephone ringing. */
 class SmartphoneRinger implements Runnable {
     private final Location noteblockLocation;
     private final Iterator<Note> notesIterator;
     private final int taskId;
     private final Player player;
     private final RealisticChat plugin;
 
     public SmartphoneRinger(RealisticChat plugin, Player player) {
         this.plugin = plugin;
         this.player = player;
 
         // Place a temporary noteblock above the player's head, required by the client to play a note
         // For why we do this see http://forums.bukkit.org/threads/player-playnote-is-it-broken.65404/#post-1023374
         // TODO: what if player is at build limit?
         noteblockLocation  = player.getLocation().add(0, 2, 0);
        player.sendBlockChange(noteblockLocation, Material.NOTE_BLOCK, (byte)0);
 
         List<Note> notes = new ArrayList<Note>();
         // TODO: more phone-like ringing!
         for (int i = 0; i < plugin.getConfig().getInt("smartphoneRings", 4); i += 1) {
             notes.add(new Note(1, Note.Tone.A, false));
             notes.add(new Note(1, Note.Tone.A, true));
             notes.add(new Note(1, Note.Tone.A, false));
             notes.add(new Note(1, Note.Tone.A, false));
             notes.add(null);
         }
 
         notesIterator = notes.iterator();
 
         long delay = 0;
         long period = 20/2;
 
         taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, this, delay, period);
     }
 
     public void run() {
         if (notesIterator.hasNext()) {
             Note note = notesIterator.next();
 
             if (note != null) {
                 player.playNote(noteblockLocation, Instrument.PIANO, note);
             }
         } else {
             stop();
         }
     }
 
     /** Stop ringing, either because call was established or did not answer. */
     public void stop() {
         Bukkit.getScheduler().cancelTask(taskId);
 
         // Revert temporary noteblock
         Block lastBlock = noteblockLocation.getBlock();
         player.sendBlockChange(noteblockLocation, lastBlock.getType(), lastBlock.getData());
 
         // No longer eligible to answer
         SmartphoneCall.ringingCall.remove(player);
     }
 }
 
 class RealisticChatListener implements Listener {
     static RealisticChat plugin;
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
 
         if (message.startsWith(getDeviceTag())) {
             // we made this event.. don't recurse! let it pass through to other plugins
             return;
         }
 
         ArrayList<String> sendInfo = new ArrayList<String>();
 
         // First, global prefix overrides all
         if (message.startsWith(getGlobalPrefix())) {
             globallyDeliverMessage(sender, message.substring(getGlobalPrefix().length()));
             
             event.setCancelled(true);
             return;
         }
 
 
         // TODO: change to sound pressure level instead of distance based
         // see http://en.wikipedia.org/wiki/Sound_pressure#Examples_of_sound_pressure_and_sound_pressure_levels
         // for added realism, also to allow 130 dB = threshold of pain (player damage), nausea, http://en.wikipedia.org/wiki/Sound_cannon
         double speakingRange = plugin.getConfig().getDouble("speakingRangeMeters", 50.0);
         double garbleRangeDivisor = plugin.getConfig().getDouble("garbleRangeDivisor", 2.0); // so last 1/2.0 (half) of distance, speech is unclear
 
         // Yelling costs hunger and increases range
         int yell = countExclamationMarks(message);
         yell = Math.min(yell, plugin.getConfig().getInt("yellMax", 4));
         if (yell > 0 && sender.hasPermission("realisticchat.yell")) {
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
         if (whisper > 0 && sender.hasPermission("realisticchat.whisper")) {
             sendInfo.add("whisper="+whisper);
 
             speakingRange -= plugin.getConfig().getDouble("whisperRangeDecrease", 40.0) * whisper;
             if (speakingRange < 1) {
                 speakingRange = 1;
             }
         }
 
         // Speaking into certain devices
         if (holdingBullhorn(sender)) {
             sendInfo.add("send-mega");
             // calculated in recipient
         } else if (holdingSmartphone(sender)) {
             sendInfo.add("send-phone");
 
       
             SmartphoneCall call = SmartphoneCall.lookup(sender);
             if (call != null) {
                 // We're on a call!
                 call.say(sender, message);
 
                 if (!plugin.getConfig().getBoolean("smartphoneHearLocally", true)) {
                     return;
                 }
                 // fall through and say locally too
             } else if (!sender.hasPermission("realisticchat.smartphone.call")) {
                 //sender.sendMessage("You do not have permission to make calls");
             } else {
                 // Talking to voice-activated phone
                 // TODO: natural language processing
                 // TODO: detect commands, like voice-activated proximity-activated state-of-the-art smartphone
 
                 String calleeName = message.replaceAll("[()!]", "");    // strip punctuation to try to understand better
 
                 // Call player name if exists
                 Player caller = sender;
                 Player callee = Bukkit.getPlayer(calleeName); // interestingly, this does prefix matching..
                 // TODO: also check for offline player, to leave offline voicemail
                 if (callee == null) {
                     // TODO: ring but have no one pick up? Your call cannot be completed as dialed.
                     sender.sendMessage("Sorry, I don't understand '"+calleeName+"'");
                     sender.sendMessage("Say a player name to place a call.");
                 } else if (callee.equals(caller)) {
                     // Tried to call self..
                     sender.sendMessage("Busy signal");
                 } else if (!callee.hasPermission("realisticchat.smartphone.answer")) {   // TODO: move to call()?
                     sender.sendMessage(callee.getDisplayName() + " does not have permission to answer calls");
                 } else {
                     sender.sendMessage("Ringing "+callee.getDisplayName()+"...");
 
                     // TODO: ring until player picks up! Keep ringing, then the callee should have to
                     // switch their held item to the smartphone in order to pickup. If they don't pick up
                     // in time, or deny the call (shift-switch?) switch to their voicemail.
 
                     call = new SmartphoneCall(caller);
                     call.call(callee);
                     // will be picked up on item change, if any
 
                     /*
 
                     if (isSmartphone(callee.getItemInHand())) {
                         new SmartphoneCall(caller, callee);
                     } else {
                         sender.sendMessage(callee.getDisplayName() + " did not answer");
                     }
                     */
                 }
             }
 
             if (!plugin.getConfig().getBoolean("smartphoneWaterproof", false)) {
                 // Trying to use a phone underwater? Not the best idea
                 // TODO: can we feasibly detect once player enters water? rather not listen to player move event though
                 Block in = sender.getLocation().getBlock().getRelative(BlockFace.DOWN);
                 if (in != null && (in.getType() == Material.WATER || in.getType() == Material.STATIONARY_WATER)) {
                     if (call != null) {
                         call.hangup();
                     }
 
                     Item item = sender.getWorld().dropItemNaturally(sender.getLocation(), sender.getItemInHand());
                     item.setPickupDelay(plugin.getConfig().getInt("smartphoneWaterPickupDelay", 10000));     // can't pickup, will despawn before
                     sender.setItemInHand(null);
                     sender.sendMessage("Your smartphone was destroyed by water damage");
                 }
             }
         }
 
         // Log that the player tried to talk
         sendInfo.add("r="+speakingRange);
         plugin.log.info("<" + sender.getName() + ": "+joinList(sendInfo,",")+"> "+message);
 
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
                     deliverMessage(recipient, sender, "walkie" + getDeviceTag() + message, recvInfoWalkie);
                 } else if (distance < walkieMaxRange) {
                     // Can't quite make you out..
                     double walkieClarity = 1 - ((distance - walkieClearRange) / walkieMaxRange);
 
                     recvInfoWalkie.add("wc="+walkieClarity);
 
                 	deliverMessage(recipient, sender, "walkie" + getDeviceTag() + garbleMessage(message, walkieClarity), recvInfoWalkie);
                 }
                 if (!plugin.getConfig().getBoolean("walkieHearLocally", true)) {
                     continue;
                 }
                 // also fall through and deliver message locally
             }
 
             double hearingRange = speakingRange;
             
             if (holdingBullhorn(sender)) {
             	/*
             	 * actSlop is measured in yaw, which itself is measured in 256 degrees, instead of
             	 * 360. actSlop is the degrees yaw of the angle from sender to rec. 
             	 * micSlop is the yaw angle that the sender is facing. the equation in the if
             	 * statement on the left hand side of the "less than" symbol gives the difference between the 
             	 * two slopes and the right hand side of the if statement checks to make sure the difference
             	 * is less than 35 degrees by default. This makes sure the receiving player is in the "sound cone".
             	 * If so, the bullhorn multipier is applied to the hearingRange of this receiving player.
             	 */
             	double deltaZ = recipient.getLocation().getZ() - sender.getLocation().getZ();
             	double deltaX = recipient.getLocation().getX() - sender.getLocation().getX();
             	double actSlop = 0;
             	if (deltaZ <= 0 && deltaX >= 0)
             		actSlop = Math.toDegrees(Math.tan(deltaX/(-1*deltaZ)))+180;
             	if (deltaZ >= 0 && deltaX > 0)
             		actSlop = Math.toDegrees(Math.tan(deltaZ/(deltaX)))+270;
             	if (deltaZ > 0 && deltaX <= 0)
             		actSlop = Math.toDegrees(Math.tan((-1*deltaX)/(deltaZ)));
             	if (deltaZ <= 0 && deltaX < 0)
             		actSlop = Math.toDegrees(Math.tan((-1*deltaZ)/(-1*deltaX)))+90;
 
                 actSlop %= 360;
 
             	double micSlop = sender.getLocation().getYaw() * (360/256);
             	
             	double dMicSlop = (micSlop +180)%360;
             	double dActSlop = (actSlop +180)%360;
 
                 double degree = Math.abs(micSlop - actSlop);
                 
                 double ddegree = Math.abs(dMicSlop = dActSlop);
 
                 recvInfo.add("mega-actSlop=" + actSlop);
                 recvInfo.add("mega-micSlop=" + micSlop);
                 recvInfo.add("mega-deltaZ=" + deltaZ);
                 recvInfo.add("mega-deltaX=" + deltaX);
                 recvInfo.add("mega-degree=" + degree);
 
                 // If within 70 degrees (the default Minecraft FOV), heard bullhorn
                 
             	if (degree < plugin.getConfig().getDouble("bullhornWidthDegrees", 70.0) || ddegree < plugin.getConfig().getDouble("bullhornWidthDegrees", 70.0)){
                     recvInfo.add("mega-HEARD");
             		hearingRange *= plugin.getConfig().getDouble("bullhornFactor", 2.0);
             	}
             }
 
             // Ear trumpets increase hearing range only
             double earTrumpetRange = getEarTrumpetRange(recipient);
             if (earTrumpetRange != 0 && sender.hasPermission("realisticchat.eartrumpet")) {
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
         // TODO: we really should NOT cancel the event, instead let it through and use setFormat() 
         // to customize the display. This will increase compatibility with other chat programs.
         // The problem is how to deliver the messages twice.. (sendMessage for one of them?)
         // See discussion at http://forums.bukkit.org/threads/help-how-to-stop-other-players-from-seeing-chat.66537/#post-1035424
         event.setCancelled(true);
     }
 
     /** Get whether the player has a walkie-talkie ready to talk into.
     */
     private static boolean hasWalkieTalking(Player player) {
         if (!plugin.getConfig().getBoolean("walkieEnable", true)) {
             return false;
         }
         if (!player.hasPermission("realisticchat.walkie.talk")) {
             return false;
         }
 
         ItemStack held = player.getItemInHand();
         
         return held != null && held.getTypeId() == plugin.walkieItemId;
     }
 
     /** Get whether the player has a walkie-talkie ready for listening.
     */
     private static boolean hasWalkieListening(Player player) {
         if (!plugin.getConfig().getBoolean("walkieEnable", true)) {
             return false;
         }
         if (!player.hasPermission("realisticchat.walkie.hear")) {
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
 
     /** Get whether the player has a bullhorn to talk into.
     */
     private static boolean holdingBullhorn(Player player) {
         if (!plugin.getConfig().getBoolean("bullhornEnable", true)) {
             return false;
         }
         if (!player.hasPermission("realisticchat.bullhorn")) {
             return false;
         }
 
         ItemStack held = player.getItemInHand();
 
         return held != null && held.getTypeId() == plugin.bullhornItemId;
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
 
 
     public static String joinList(ArrayList<String> list, String sep) {
         StringBuilder sb = new StringBuilder();
         for (String item: list) {
             sb.append(item + sep);
         }
         String s = sb.toString();
         if (s.length() == 0) {
             return "";
         } else {
             return s.substring(0, s.length() - sep.length());
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
 
     /** Get a special character used to separate device name from chat message.
     Should never appear in normal chat.
     */
     public static String getDeviceTag() {
         return "\u0001"; // C0 Start of Heading (SOH)
     }
 
     /** Send a message to everyone on the server. */
     public void globallyDeliverMessage(Player sender, String message) {
         ArrayList<String> sendInfo = new ArrayList<String>();
         sendInfo.add("global");
 
         for (Player recipient: Bukkit.getOnlinePlayers()) {
             deliverMessage(recipient, sender, "global" + getDeviceTag() + message, sendInfo);
         }
 
     }
 
     /** Send a properly-formatted chat message, to the user and to other plugins. 
     The message can be prefixed with a device name + getDeviceTag() for special formatting.
     */
     public static void deliverMessage(Player recipient, Player sender, String message, ArrayList<String> info) {
         PlayerChatEvent event = createChatEvent(recipient, sender, message, info);
 
         callChatEvent(event);
     }
 
     private static void callChatEvent(PlayerChatEvent event) {
         // Tag the message so we know it is ours..
         event.setMessage(getDeviceTag() + event.getMessage());
 
         // Allow other plugins to process event
         Bukkit.getServer().getPluginManager().callEvent(event);
 
         if (event.isCancelled()) {
             plugin.log.info(" ignoring chat event cancelled by other plugin: " + event);
             return;
         }
 
         // Deliver (possibly modified) chat to player as message
         // based on src/main/java/net/minecraft/server/NetServerHandler.java : chat()
         String s = String.format(event.getFormat(), event.getPlayer().getDisplayName(), event.getMessage());
 
         // Strip the tag completely from the message, wherever it might be
         s = s.replace(getDeviceTag(), "");
 
         //minecraftServer.console.sendMessage(s);   // TODO
         for (Player recipient : event.getRecipients()) {
             recipient.sendMessage(s);
         }
     }
 
     /** Synthesize a new chat event between two players, with customized formatting.
     */
     private static PlayerChatEvent createChatEvent(Player recipient, Player sender, String message, ArrayList<String> info) {
         ChatColor senderColor = (sender.equals(recipient) ? plugin.spokenPlayerColor : plugin.heardPlayerColor);
 
         String device = null;
 
         // Tagged message, from a device
         int divider = message.indexOf(getDeviceTag());
         if (divider != -1) {
             device = message.substring(0, divider);
             message = message.substring(divider + 1);
         }
 
         // Get the message format accordingly depending on how it was received
         String defaultFormat = plugin.getConfig().getString("chatLineFormat", "%1$s: %2$s");
         String format = defaultFormat;
 
         if (holdingBullhorn(sender)) {      // TODO: move to device tagging on message like walkie/cell
             String direction = bullhornDirection(recipient, sender);
 
             if (direction != null && !direction.equals("")) {
                 format = String.format(plugin.getConfig().getString("bullhornChatLineFormat", "%1$s [%3$s]: %2$s"),
                         "%1$s", // player, replaced below
                         "%2$s", // message, replaced below
                         direction);
             }
         }
 
         if (device != null) {
             if (device.equals("walkie")) {
                 format = plugin.getConfig().getString("walkieChatLineFormat", "[walkie] %1$s: %2$s");
             } else if (device.equals("cell")) {
                 format = plugin.getConfig().getString("smartphoneChatLineFormat", "[cell] %1$s: %2$s");
             } else if (device.equals("global")) {
                 format = plugin.getConfig().getString("globalChatLineFormat", "[global] %1$s: %2$s");
             }
         }
 
         if (format == null) {
             format = defaultFormat;
         }
 
         // Add player name color to format string
         format = String.format(format, senderColor + "%1$s" + plugin.messageColor, "%2$s");
 
         // Format the message
         //String formattedMessage = String.format(format, senderColor + sender.getDisplayName() + plugin.messageColor, message);
         //plugin.log.info(formattedMessage);
         //recipient.sendMessage(formattedMessage);
 
         // TODO: instead, use recipient.chat() and PlayerChatEvent setFormat(), so other plugins see the chat, too
         // or, construct a PlayerChatEvent and send it ourselves so other plugins can format messages from cells/walkies
         // .. but the challenge is avoiding infinite recursion (TODO: tagged messages, don't resend if tagged)
 
 
         plugin.log.info("[RealisticChat] ("+joinList(info, ",")+") "+sender.getName() + " -> " + recipient.getName() + ": " + message);
 
         // Output is the message format and message itself, in the event object
         PlayerChatEvent newEvent = new PlayerChatEvent(sender, message);
 
         newEvent.getRecipients().clear();
         newEvent.getRecipients().add(recipient);
 
         newEvent.setFormat(format);
 
         return newEvent;
     }
    
     /** Get the direction a bullhorn-amplified message came from, if possible.
     */
     private static String bullhornDirection(Player recipient, Player sender){
         if (!plugin.getConfig().getBoolean("bullhornEnable", true) || !holdingBullhorn(sender)) {
             return "";
         }
 
         String addition = "";
         double recX = recipient.getLocation().getX();
         double recZ = recipient.getLocation().getZ();
         double senX = sender.getLocation().getX();
         double senZ = sender.getLocation().getZ();
 
         if (recZ > senZ)
             addition += "North";
         if (recZ < senZ)
             addition += "South";
         if (recX > senX)
             addition += "West";
         if (recX < senX)
             addition += "East";
 
     	return addition;
     }
 
     @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled=true)
     public void onPlayerItemHeld(PlayerItemHeldEvent event) {
         Player player = event.getPlayer();
         int slot = event.getNewSlot();
 
         ItemStack held = player.getInventory().getItem(slot);
 
         if (isSmartphone(held)) {
             SmartphoneCall pendingCall = SmartphoneCall.getRingingCall(player);
             if (pendingCall != null) {
                 player.sendMessage("Answering call");
                 pendingCall.answer(player);
             }
         } else {
             // Switching off of phone hangs it up
             // TODO: other ways to hangup.. dropped item?
             SmartphoneCall call = SmartphoneCall.lookup(player);
             if (call != null) {
                 call.hangup();
             }
         }
     }
 
     @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled=true) 
     public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
         if (getGlobalPrefix() == null) {
             return;
         }
 
         if (event.getMessage().startsWith(getGlobalPrefix())) {
             globallyDeliverMessage(event.getPlayer(), event.getMessage().substring(getGlobalPrefix().length()));
 
             event.setCancelled(true);
         }
     }
 
     /** Get prefix for messages to be sent globally.
     May be a command (beginning with '/'), normal chat message, or null (if disabled)
     */
     private String getGlobalPrefix() {
         return plugin.getConfig().getString("globalPrefix", "/g ");
     }
 
     /** Get whether the item is a smart phone device.
     */
     private boolean isSmartphone(ItemStack item) {
         if (!plugin.getConfig().getBoolean("smartphoneEnable", true)) {
             return false;
         }
 
         return item != null && item.getTypeId() == plugin.smartphoneItemId;
     }
 
     /** Get whether the player is holding their smartphone, possibly speaking into it.
     */
     public boolean holdingSmartphone(Player player) {
         return isSmartphone(player.getItemInHand());
     }
 }
 
 public class RealisticChat extends JavaPlugin { 
     Logger log = Logger.getLogger("Minecraft");
     RealisticChatListener listener;
 
     int walkieItemId, bullhornItemId, smartphoneItemId;
     ChatColor spokenPlayerColor, heardPlayerColor, messageColor, dimMessageColor;
 
     public void onEnable() {
         // Copy default config
         getConfig().options().copyDefaults(true);
         saveConfig();
         reloadConfig();
 
         walkieItemId = getConfigItemId("walkieItem", Material.COMPASS.getId());
         bullhornItemId = getConfigItemId("bullhornItem", Material.DIAMOND.getId());
         smartphoneItemId = getConfigItemId("smartphoneItem", Material.WATCH.getId());
 
         spokenPlayerColor = getConfigColor("chatSpokenPlayerColor", ChatColor.YELLOW);
         heardPlayerColor = getConfigColor("chatHeardPlayerColor", ChatColor.GREEN);
         messageColor = getConfigColor("chatMessageColor", ChatColor.WHITE);
         dimMessageColor = getConfigColor("chatDimMessageColor", ChatColor.DARK_GRAY);
 
         if (getConfig().getBoolean("earTrumpetEnable", true) && getConfig().getBoolean("earTrumpetEnableCrafting", true)) {
             loadRecipes();
         }
 
         listener = new RealisticChatListener(this);
         SmartphoneCall.plugin = this;
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
 
         // TODO: can we use ItemStack ConfigurationSerializable? might be easier..
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
