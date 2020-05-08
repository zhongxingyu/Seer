 package com.Top_Cat.CODMW.sql;
 
 import java.util.ArrayList;
 
 import org.bukkit.Material;
 
 public enum Achievement {
     KILL_50(0, Stat.KILLS, 50, "Criminal", "Get 50 Kills", Material.BOW),
     KILL_100(1, Stat.KILLS, 100, "Assassin", "Get 100 Kills", Material.BOW),
     KILL_250(2, Stat.KILLS, 250, "Murderer", "Get 250 Kills", Material.BOW),
     KILL_500(3, Stat.KILLS, 500, "Serial Killer", "Get 500 Kills", Material.BOW),
     KILL_1000(4, Stat.KILLS, 1000, "Worse than Hitler", "Get 1000 Kills", Material.BOW),
     KILL_GIG(5, Stat.OTHER, -1, "Gigssassination", "Kill Gig", Material.CAKE),
     WIN_5(6, Stat.WINS, 5, "Winner", "Win 5 games", Material.CHAINMAIL_HELMET),
     WIN_10(7, Stat.WINS, 10, "Victor", "Win 10 games", Material.CHAINMAIL_HELMET),
     WIN_25(8, Stat.WINS, 25, "Champion", "Win 25 games", Material.CHAINMAIL_HELMET),
     WIN_50(9, Stat.WINS, 50, "Conquerer", "Win 50 games", Material.CHAINMAIL_HELMET),
     WIN_100(10, Stat.WINS, 100, "No lifer", "Win 100 games", Material.CHAINMAIL_HELMET),
     FIRE_1000(11, Stat.ARROWS_FIRED, 1000, "Bad aim", "Fire 1000 arrows", Material.ARROW),
     FIRE_2500(12, Stat.ARROWS_FIRED, 2500, "Worse aim", "Fire 2500 arrows", Material.ARROW),
    FIRE_5000(13, Stat.ARROWS_FIRED, 5000, "Even worse aim", "Fire 5000 arrows", Material.ARROW),
    FIRE_10000(14, Stat.ARROWS_FIRED, 10000, "Gig-level aim", "Fire 10000 arrows", Material.ARROW),
     CLAYMORE_GET_5(15, Stat.CLAYMORES_ACHIEVED, 5, "Sparklers", "Get claymores 5 times", Material.WALL_SIGN),
     CLAYMORE_GET_10(16, Stat.CLAYMORES_ACHIEVED, 10, "Catherine Wheels", "Get claymores 10 times", Material.WALL_SIGN),
     CLAYMORE_GET_25(17, Stat.CLAYMORES_ACHIEVED, 25, "Fire Fountain", "Get claymores 25 times", Material.WALL_SIGN),
     CLAYMORE_GET_50(18, Stat.CLAYMORES_ACHIEVED, 50, "Rockets", "Get claymores 50 times", Material.WALL_SIGN),
     CLAYMORE_GET_100(19, Stat.CLAYMORES_ACHIEVED, 100, "4th of July", "Get claymores 100 times", Material.WALL_SIGN),
     APPLE_GET_5(20, Stat.APPLES_ACHIEVED, 5, "5 a day", "Get 5 apples", Material.APPLE),
     APPLE_GET_10(21, Stat.APPLES_ACHIEVED, 10, "Healthy", "Get 10 apples", Material.APPLE),
     APPLE_GET_25(22, Stat.APPLES_ACHIEVED, 25, "Fruit salad", "Get 25 apples", Material.APPLE),
     APPLE_GET_50(23, Stat.APPLES_ACHIEVED, 50, "Adam and/or Eve", "Get 50 apples", Material.APPLE),
     APPLE_GET_100(24, Stat.APPLES_ACHIEVED, 100, "Granny Smith", "Get 100 apples", Material.APPLE),
     DOG_GET_5(25, Stat.DOGS_ACHIEVED, 5, "Puddles the Dog", "Get dogs 5 times", Material.BONE),
     DOG_GET_10(26, Stat.DOGS_ACHIEVED, 10, "Rufus the Dog", "Get dogs 10 times", Material.BONE),
     DOG_GET_25(27, Stat.DOGS_ACHIEVED, 25, "Max the Dog", "Get dogs 25 times", Material.BONE),
     DOG_GET_50(28, Stat.DOGS_ACHIEVED, 50, "Butch the Dog", "Get dogs 50 times", Material.BONE),
     DOG_GET_100(29, Stat.DOGS_ACHIEVED, 100, "Brian Blessed", "Get dogs 100 times", Material.BONE),
     SENTRY_GET_5(30, Stat.SENTRIES_ACHIEVED, 5, "Hello?", "Get 5 sentries", Material.DISPENSER),
     SENTRY_GET_10(31, Stat.SENTRIES_ACHIEVED, 10, "Who's there?", "Get 10 sentries", Material.DISPENSER),
     SENTRY_GET_25(32, Stat.SENTRIES_ACHIEVED, 25, "Activating!", "Get 25 sentries", Material.DISPENSER),
     SENTRY_GET_50(33, Stat.SENTRIES_ACHIEVED, 50, "I see you!", "Get 50 sentries", Material.DISPENSER),
     SENTRY_GET_100(34, Stat.SENTRIES_ACHIEVED, 100, "Are you still there?", "Get 100 sentries", Material.DISPENSER),
     CHOPPER_GET_5(35, Stat.CHOPPERS_ACHIEVED, 5, "Calling the chopper!", "Get 5 choppers", Material.DIAMOND),
     CHOPPER_GET_10(36, Stat.CHOPPERS_ACHIEVED, 10, "Helicopter on the way!", "Get 10 choppers", Material.DIAMOND),
     CHOPPER_GET_25(37, Stat.CHOPPERS_ACHIEVED, 25, "ETA 2 minutes!", "Get 25 choppers", Material.DIAMOND),
     CHOPPER_GET_50(38, Stat.CHOPPERS_ACHIEVED, 50, "STANDBY, MY HAWKMEN!", "Get 50 choppers", Material.DIAMOND),
     CHOPPER_GET_100(39, Stat.CHOPPERS_ACHIEVED, 100, "Get to the chopper!", "Get 100 choppers", Material.DIAMOND),
     CLAYMORE_KILL_5(40, Stat.CLAYMORE_KILLS, 5, "Vandal", "Get 5 claymore kills", Material.WALL_SIGN),
     CLAYMORE_KILL_10(41, Stat.CLAYMORE_KILLS, 10, "Bomber", "Get 10 claymore kills", Material.WALL_SIGN),
     CLAYMORE_KILL_25(42, Stat.CLAYMORE_KILLS, 25, "Demolistionist", "Get 25 claymore kills", Material.WALL_SIGN),
     CLAYMORE_KILL_50(43, Stat.CLAYMORE_KILLS, 50, "Eradicator", "Get 50 claymore kills", Material.WALL_SIGN),
     CLAYMORE_KILL_100(44, Stat.CLAYMORE_KILLS, 100, "Bin Laden", "Get 100 claymore kills", Material.WALL_SIGN),
     APPLE_KILL_5(45, Stat.INVULNERABLE_KILLS, 5, "Invulnerable", "Get 5 invulnerable kills", Material.APPLE),
     APPLE_KILL_10(46, Stat.INVULNERABLE_KILLS, 10, "Steel skin", "Get 10 invulnerable kills", Material.APPLE),
     APPLE_KILL_25(47, Stat.INVULNERABLE_KILLS, 25, "Can't touch this!", "Get 25 invulnerable kills", Material.APPLE),
     APPLE_KILL_50(48, Stat.INVULNERABLE_KILLS, 50, "Diamond skin", "Get 50 invulnerable kills", Material.APPLE),
     APPLE_KILL_100(49, Stat.INVULNERABLE_KILLS, 100, "Invincible", "Get 100 invulnerable kills", Material.APPLE),
     DOG_KILL_5(50, Stat.DOG_KILLS, 5, "Man's best friend!", "Get 5 dog kills", Material.BONE),
     DOG_KILL_10(51, Stat.DOG_KILLS, 10, "New tricks!", "Get 10 dog kills", Material.BONE),
     DOG_KILL_25(52, Stat.DOG_KILLS, 25, "Giving the dog Steve's bones!", "Get 25 dog kills", Material.BONE),
     DOG_KILL_50(53, Stat.DOG_KILLS, 50, "The dog's dinner!", "Get 50 dog kills", Material.BONE),
     DOG_KILL_100(54, Stat.DOG_KILLS, 100, "His bite is worse than his bark!", "Get 100 dog kills", Material.BONE),
     SENTRY_KILL_5(55, Stat.SENTRY_KILLS, 5, "Activating.", "Get 5 sentry kills", Material.DISPENSER),
     SENTRY_KILL_10(56, Stat.SENTRY_KILLS, 10, "Target acquired.", "Get 10 sentry kills", Material.DISPENSER),
     SENTRY_KILL_25(57, Stat.SENTRY_KILLS, 25, "Dispensing product!", "Get 25 sentry kills", Material.DISPENSER),
     SENTRY_KILL_50(58, Stat.SENTRY_KILLS, 50, "Firing!", "Get 50 sentry kills", Material.DISPENSER),
     SENTRY_KILL_100(59, Stat.SENTRY_KILLS, 100, "Sentry mode activated!", "Get 100 sentry kills", Material.DISPENSER),
     CHOPPER_KILL_5(60, Stat.CHOPPER_KILLS, 5, "Enemy down!", "Get 5 chopper kills", Material.DIAMOND),
     CHOPPER_KILL_10(61, Stat.CHOPPER_KILLS, 10, "Target neutralised.", "Get 10 chopper kills", Material.DIAMOND),
     CHOPPER_KILL_25(62, Stat.CHOPPER_KILLS, 25, "Mass murder", "Get 25 chopper kills", Material.DIAMOND),
     CHOPPER_KILL_50(63, Stat.CHOPPER_KILLS, 50, "Overpowered!", "Get 50 chopper kills", Material.DIAMOND),
     CHOPPER_KILL_100(64, Stat.CHOPPER_KILLS, 100, "Ragequit", "Get 100 chopper kills", Material.DIAMOND),
     KILL_NOTCH(65, Stat.OTHER, -1, "The Notch Delusion", "Kill Notch", Material.LEATHER_HELMET),
     ASSISTS_5(66, Stat.ASSISTS, 5, "Every little helps", "Get 5 assists", Material.WOOD_SWORD),
     ASSISTS_10(67, Stat.ASSISTS, 10, "Helping hand", "Get 10 assists", Material.WOOD_SWORD),
     ASSISTS_25(68, Stat.ASSISTS, 25, "Working together", "Get 25 assists", Material.WOOD_SWORD),
     ASSISTS_50(69, Stat.ASSISTS, 50, "Teamwork!", "Get 50 assists", Material.WOOD_SWORD),
     ASSISTS_100(70, Stat.ASSISTS, 100, "Damned kill stealers...", "Get 100 assists", Material.WOOD_SWORD),
     CLAYMORE_MULTI(71, Stat.OTHER, -1, "Explosives master", "Kill more than one person with a single claymore", Material.SULPHUR),
     SNIPER(72, Stat.FURTHEST_KILL, 30, "Sniper", "Kill an enemy who is more than 30 blocks away", Material.COMPASS),
     SAS_SNIPER(73, Stat.FURTHEST_HEADSHOT, 30, "SAS Sniper", "Headshot an enemy who is more than 30 blocks away", Material.COMPASS),
     LAST_RESORT(74, Stat.OTHER, -1, "Last Resort", "Kill someone with a knife when you have no ammo", Material.IRON_SWORD),
     CLOSE_CALL(75, Stat.LIFE_REGENS, 5, "Close Call", "Restore your health 5 times without dying", Material.GRILLED_PORK),
     CLOSE_CHOPPER(76, Stat.OTHER, -1, "So close, but so far", "Kill an enemy who is one kill away from a helicopter", Material.FIRE),
     NEMESIS(77, Stat.OTHER, -1, "My Arch Nemesis", "Kill an enemy team member 3 times in a row", Material.PUMPKIN),
     FALL_HARD(78, Stat.OTHER, -1, "The harder they fall", "Kill the enemy top player 3 times in a row", Material.JACK_O_LANTERN),
     WARGASM(79, Stat.OTHER, -1, "Wargasm", "Get 2 different killstreaks within 20 seconds", Material.SUGAR),
     HOTSHOT(80, Stat.OTHER, -1, "Hotshot", "Get 3 headshots in a row", Material.IRON_HELMET),
     COMMANDO(81, Stat.OTHER, -1, "Commando", "Get 3 melee kills in a row without dying", Material.IRON_SWORD),
     HIDING(82, Stat.OTHER, -1, "Hiding", "Survive for 2 minutes", Material.SLIME_BALL),
     CAMPER(83, Stat.OTHER, -1, "Camper", "Survive for 3 minutes", Material.SLIME_BALL),
     BEAR_GRYLLS(84, Stat.OTHER, -1, "Bear Grylls", "Survive for 5 minutes", Material.SLIME_BALL),
     EXTERMINATION(85, Stat.OTHER, -1, "Extermination", "Kill 10 enemies with a single killstreak", Material.LAVA_BUCKET),
     OVER_9000(86, Stat.BLOCKS_MOVED, 9000000, "Over 9000!", "Walk 9000 kilometers in MineCod", Material.MINECART),
     MILES_500(87, Stat.BLOCKS_MOVED, 804672, "500 Miles", "Walk 500 miles in MineCod", Material.MINECART),
     MARATHON(88, Stat.BLOCKS_MOVED, 42195, "Marathon", "Run a marathon in MineCod", Material.MINECART),
     ONTHEGROUND(89, Stat.ITEMS_THROWN, 100, "Threw it on the Ground", "My dad's not a phone!", Material.FEATHER, 50, "A hidden achievement (1/5)"),
     READINGABOOK(90, Stat.OTHER, -1, "Never interupt me...", "while i'm reading a book!", Material.BOOK, 50, "A hidden achievement (2/5)"),
     FIREARMS(91, Stat.OTHER, -1, "Firearms", "Kill someone while on fire", Material.FIRE, 50, "A hidden achievement (3/5)"),
     TEAMNOSWITCH(92, Stat.OTHER, -1, "Noswitch!", "Don't switch teams", Material.LEVER, 50, "A hidden achievement (4/5)"),
     KONAMI(93, Stat.OTHER, -1, "Konami Code", "Perform the konami code", Material.GREEN_RECORD, 50, "A hidden achievement (5/5)"),
     GIGLOCK_HOLMES(94, Stat.OTHER, -1, "Giglock Holmes", "Get the 5 hidden achievements", Material.MAP),
     FISH_KILLS_10(95, Stat.FISH_KILLS, 10, "Codswallop", "Get 10 fish kills", Material.RAW_FISH),
     FISH_KILLS_50(96, Stat.FISH_KILLS, 50, "Oh my cod!", "Get 50 fish kills", Material.RAW_FISH),
     FISH_KILLS_100(97, Stat.FISH_KILLS, 100, "MineCOD", "Get 100 fish kills", Material.RAW_FISH),
     FALL_DAMAGE_25(98, Stat.FALL_DAMAGE, 25, "A bit tipsy", "Take 25 fall damage", Material.LEATHER_BOOTS),
     FALL_DAMAGE_50(99, Stat.FALL_DAMAGE, 50, "One beer too many", "Take 50 fall damage", Material.LEATHER_BOOTS),
     FALL_DAMAGE_100(100, Stat.FALL_DAMAGE, 100, "Try walking in a straight line", "Take 100 fall damage", Material.LEATHER_BOOTS),
     FALL_DAMAGE_250(101, Stat.FALL_DAMAGE, 250, "Definitely Drunk", "Take 250 fall damage", Material.LEATHER_BOOTS),
     FALL_DAMAGE_500(102, Stat.FALL_DAMAGE, 500, "George Michael", "Take 500 fall damage", Material.LEATHER_BOOTS),
     ;
     
     private final int id;
     private final Stat s;
     private final int c;
     private final int points;
     private final String name;
     private final String desc;
     private final Material mat;
     private final boolean hidden;
     private final String hidden_txt;
     
     Achievement(int id, Stat s, int c, String name, String desc, Material m, int points, String hidden) {
         this.id = id;
         this.s = s;
         this.c = c;
         this.points = points;
         this.name = name;
         this.desc = desc;
         this.mat = m;
         this.hidden = !hidden.isEmpty();
         this.hidden_txt = hidden;
     }
     
     Achievement(int id, Stat s, int c, String name, String desc, Material m, int points) {
         this(id, s, c, name, desc, m, points, "");
     }
     
     Achievement(int id, Stat s, int c, String name, String desc, Material m) {
         this(id, s, c, name, desc, m, 20);
     }
     
     Achievement(int id, Stat s, int c, String name, String desc) {
         this(id, s, c, name, desc, Material.DIAMOND);
     }
 
     public int getId() {
         return this.id;
     }
     
     public Stat getStat() {
         return this.s;
     }
     
     public int getCount() {
         return this.c;
     }
     
     public int getPoints() {
         return this.points;
     }
     
     public String getName() {
         return this.name;
     }
     
     public String getDesc() {
         return this.desc;
     }
     
     public Material getMat() {
         return this.mat;
     }
     
     public boolean getHidden() {
         return this.hidden;
     }
     
     public String getText() {
         if (hidden) {
             return hidden_txt;
         } else {
             return "'" + this.name + "' (" + this.desc + ")";
         }
     }
     
     public static ArrayList<Achievement> table = new ArrayList<Achievement>(); 
     static {
         for (Achievement i : Achievement.values()) {
             table.add(i.getId(), i);
         }
     }
 
     public static Achievement valueOf(int id) {
         return table.get(id);
     }
 }
