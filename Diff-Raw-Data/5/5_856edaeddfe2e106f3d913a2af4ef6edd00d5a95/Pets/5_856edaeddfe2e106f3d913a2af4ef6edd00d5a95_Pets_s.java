 package se.DMarby.Pets;
 
 import com.google.common.base.Strings;
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.DyeColor;
 import org.bukkit.Material;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.plugin.java.JavaPlugin;
 import se.DMarby.Pets.menu.*;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 public class Pets extends JavaPlugin {
 
     public PetController controller;
     private Storage storage;
     private final List<String> pets = new ArrayList();
     private static Pets instance;
     private Menu menu;
 
 
     @Override
     public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
         if (!label.equalsIgnoreCase("pet")) {
             return false;
         }
         if (args.length == 1) {
             if (!sender.hasPermission("pet.toggle") && !sender.hasPermission("pet.admin")) {
                 sender.sendMessage(ChatColor.RED + "You don't have permission to do this!");
                 return true;
             }
             if (args[0].equalsIgnoreCase("menu")) {
                 menu.display((Player) sender);
                 return true;
             }
             if (args[0].equalsIgnoreCase("list")) {
                 String list = "";
                 for (String pet : pets) {
                    String the_pet = (sender.hasPermission("pet." + pet.toLowerCase()) ? ChatColor.GREEN : ChatColor.RED) + pet.substring(0, 1).toUpperCase() + pet.substring(1);
                     list += the_pet + ", ";
                 }
                 if (list.length() <= 1) {
                     sender.sendMessage(ChatColor.GRAY + "No available pets.");
                     return true;
                 }
                 sender.sendMessage(ChatColor.GREEN + "Available pets:");
                 sender.sendMessage(ChatColor.GREEN + list.substring(0, list.length() - 2));
                 sender.sendMessage(ChatColor.GREEN + "Do /pet <pettype> to select a pet!");
                 return true;
             }else if(args[0].equalsIgnoreCase("help")){
                 displayHelp(sender);
                 return true;
             }
 
             if (!sender.hasPermission("pet." + args[0].toLowerCase()) && !sender.hasPermission("pet.admin")) {
                 sender.sendMessage(ChatColor.RED + "You don't have permission to do this!");
                 return true;
             }
             if (!pets.contains(args[0].toLowerCase())) {
                 sender.sendMessage(ChatColor.RED
                         + "Invalid pet type! Do /pet list to get a list of all available types.");
                 return true;
             }
 
             if (!(sender instanceof Player)) {
                 sender.sendMessage(ChatColor.GRAY + "Must be ingame.");
                 return true;
             }
             controller.togglePet((Player) sender, args[0]);
             return true;
         } else if (args.length == 0) {
             if (!(sender instanceof Player)) {
                 sender.sendMessage(ChatColor.GRAY + "Must be ingame.");
                 return true;
             }
             if (!sender.hasPermission("pet.toggle") && !sender.hasPermission("pet.admin")) {
                 sender.sendMessage(ChatColor.RED + "You don't have permission to do this!");
                 return true;
             }
             if (Strings.isNullOrEmpty(controller.getType((Player) sender))) {
                 String list = "";
                 for (String pet : pets) {
                    if (sender.hasPermission("pet." + pet.toLowerCase()) && !sender.hasPermission("pet.admin")) {
                         String the_pet = pet.substring(0, 1).toUpperCase() + pet.substring(1);
                         list += the_pet + ", ";
                     }
                 }
                 if (list.length() <= 1) {
                     sender.sendMessage(ChatColor.RED + "No pet available.");
                     return true;
                 }
                 sender.sendMessage(ChatColor.GREEN + "Available pets:");
                 sender.sendMessage(ChatColor.GREEN + list.substring(0, list.length() - 2));
                 sender.sendMessage(ChatColor.GREEN + "Do /pet <petType> to select a pet!");
                 return true;
             }
             controller.togglePet((Player) sender);
             return true;
         }else if (args.length >= 2){
             if(args[0].equalsIgnoreCase("name")){
                 if (!sender.hasPermission("pet.name") && !sender.hasPermission("pet.admin"))  {
                     sender.sendMessage(ChatColor.RED + "You don't have permission to do this!");
                     return true;
                 }
                 if(args.length == 2){
                     if(args[1].equalsIgnoreCase("reset")){
                         controller.setName((Player) sender, null);
                         sender.sendMessage(ChatColor.GREEN + "Pet name removed.");
                         return true;
                     }
                     controller.setName((Player) sender, ChatColor.translateAlternateColorCodes('&', args[1].substring(0, Math.min(args[1].length(), 24))).replace("&s", " "));
                     sender.sendMessage(ChatColor.GREEN + "Pet name changed to " + args[1].substring(0, Math.min(args[1].length(), 24)).replace("&s", " "));
                 }else if(args.length == 3){
                     if(!sender.hasPermission("pet.admin")){
                         displayHelp(sender);
                         return true;
                     }
                     Player player = Bukkit.getPlayer(args[1]);
                     if(player == null){
                         sender.sendMessage(ChatColor.RED + "This player is not online!");
                         return true;
                     }
                     if(args[2].equalsIgnoreCase("reset")){
                         controller.setName(player, null);
                         sender.sendMessage(ChatColor.GREEN + "Pet name removed.");
                         return true;
                     }
                     controller.setName(player, ChatColor.translateAlternateColorCodes('&', args[2].substring(0, Math.min(args[2].length(), 24))).replace("&s", " "));
                     sender.sendMessage(ChatColor.GREEN + "Pet name changed to " + args[2].substring(0, Math.min(args[2].length(), 24)).replace("&s", " "));
 
                 }
                 return true;
             }else if(args[0].equalsIgnoreCase("item")){
                 if (!sender.hasPermission("pet.item") && !sender.hasPermission("pet.admin")) {
                     sender.sendMessage(ChatColor.RED + "You don't have permission to do this!");
                     return true;
                 }
                 if(args.length == 2){
                     if(args[1].equalsIgnoreCase("reset")){
                         controller.setItem((Player) sender, null);
                         sender.sendMessage(ChatColor.GREEN + "Pet item removed.");
                     }else{
                         Material mat;
                         if(Util.isInt(args[1])){
                             mat = Material.getMaterial(Integer.parseInt(args[1]));
                         }else{
                             mat = Material.getMaterial(args[1].toUpperCase());
                         }
 
                         if(mat != null) {
                             controller.setItem((Player) sender, args[1]);
                             sender.sendMessage(ChatColor.GREEN + "Pet item changed to " + mat.name());
                         }
                         else sender.sendMessage(ChatColor.RED + "Invalid item!");
                     }
                 }else if(args.length == 3){
                     if(!sender.hasPermission("pet.admin")){
                         displayHelp(sender);
                         return true;
                     }
                     Player player = Bukkit.getPlayer(args[1]);
                     if(player == null){
                         sender.sendMessage(ChatColor.RED + "This player is not online!");
                         return true;
                     }
                     if(args[2].equalsIgnoreCase("reset")){
                         controller.setItem(player, null);
                         sender.sendMessage(ChatColor.GREEN + "Pet item removed.");
                     }else{
                         Material mat;
                         if(Util.isInt(args[2])){
                             mat = Material.getMaterial(Integer.parseInt(args[2]));
                         }else{
                             mat = Material.getMaterial(args[2].toUpperCase());
                         }
 
                         if(mat != null) {
                             controller.setItem(player, args[2]);
                             sender.sendMessage(ChatColor.GREEN + "Pet item changed to " + mat.name());
                         }
                         else sender.sendMessage(ChatColor.RED + "Invalid item!");
                     }
                 }
                 return true;
             }else if(args[0].equalsIgnoreCase("type") && args.length == 3){
                 if(!sender.hasPermission("pet.admin")){
                     displayHelp(sender);
                     return true;
                 }
                 Player player = Bukkit.getPlayer(args[1]);
                 if(player == null){
                     sender.sendMessage(ChatColor.RED + "This player is not online!");
                     return true;
                 }
                 controller.togglePet(player, args[2]);
                 sender.sendMessage(ChatColor.GREEN + "Pet changed to " + args[2]);
                 return true;
             }else if(args[0].equalsIgnoreCase("toggle") && args.length == 2){
                 if(!sender.hasPermission("pet.admin")){
                     displayHelp(sender);
                     return true;
                 }
                 Player player = Bukkit.getPlayer(args[1]);
                 if(player == null){
                     sender.sendMessage(ChatColor.RED + "This player is not online!");
                     return true;
                 }
                 controller.togglePet(player);
                 sender.sendMessage(ChatColor.GREEN + "Pet toggled");
                 return true;
             }else{
                 displayHelp(sender);
                 return true;
             }
         }else{
             displayHelp(sender);
             return true;
         }
     }
 
     public void displayHelp(CommandSender sender){
         sender.sendMessage(ChatColor.GREEN + "/pet - Toggles your pet");
         sender.sendMessage(ChatColor.GREEN + "/pet <petType> - Selects a pet");
         sender.sendMessage(ChatColor.GREEN + "/pet menu - Brings up a menu to select & toggle pets");
         sender.sendMessage(ChatColor.GREEN + "/pet list - Lists all available pet types");
         sender.sendMessage(ChatColor.GREEN + "/pet name <petName> - Gives your pet a name");
         sender.sendMessage(ChatColor.GREEN + "/pet name reset - Removes your pets name");
         sender.sendMessage(ChatColor.GREEN + "/pet item <itemMaterial> - Gives your pet an item");
         sender.sendMessage(ChatColor.GREEN + "/pet item reset - Removes your pets item");
         sender.sendMessage(ChatColor.GREEN + "/pet help - Displays this helpmenu");
         if(sender.hasPermission("pet.admin")){
             sender.sendMessage(ChatColor.YELLOW + "/pet type <player> <type> - Sets the players pet");
             sender.sendMessage(ChatColor.YELLOW  + "/pet toggle <player> - Toggles a players pet");
             sender.sendMessage(ChatColor.YELLOW  + "/pet name <player> <name> - Set a players pet's name");
             sender.sendMessage(ChatColor.YELLOW  + "/pet name <player> reset - Reset a players pet's name");
             sender.sendMessage(ChatColor.YELLOW  + "/pet item <player> <itemMaterial> - Set a players pet's item");
             sender.sendMessage(ChatColor.YELLOW  + "/pet item <player> reset - Reset a players pet's item");
         }
     }
 
     @Override
     public void onDisable() {
         storage.save();
     }
 
     public static Pets getInstance(){
         return instance;
     }
 
     @Override
     public void onEnable() {
         this.instance = this;
         pets.add("blaze");
         pets.add("cavespider");
         pets.add("chicken");
         pets.add("cow");
         pets.add("magmacube");
         pets.add("mooshroom");
         pets.add("ocelot");
         pets.add("pig");
         pets.add("silverfish");
         pets.add("slime");
         pets.add("snowman");
         pets.add("villager");
         pets.add("wolf");
         pets.add("creeper");
         pets.add("greenvillager");
         pets.add("bat");
         pets.add("squid");
         pets.add("zombie");
         pets.add("zombiepigman");
         pets.add("zombievillager");
         pets.add("irongolem");
         pets.add("dog");
         pets.add("electrocreeper");
         pets.add("blacksheep");
         pets.add("bluesheep");
         pets.add("brownsheep");
         pets.add("cyansheep");
         pets.add("graysheep");
         pets.add("greensheep");
         pets.add("lightbluesheep");
         pets.add("limesheep");
         pets.add("magentasheep");
         pets.add("orangesheep");
         pets.add("pinksheep");
         pets.add("purplesheep");
         pets.add("redsheep");
         pets.add("silversheep");
         pets.add("whitesheep");
         pets.add("yellowsheep");
         pets.add("blackcat");
         pets.add("redcat");
         pets.add("siamesecat");
         pets.add("horse");
         pets.add("donkey");
         pets.add("mule");
         pets.add("undeadhorse");
         pets.add("skeletonhorse");
         pets.add("ridablehorse");
         pets.add("enderman");
         pets.add("spider");
         pets.add("ghast");
         pets.add("witch");
         pets.add("skeleton");
         pets.add("witherskeleton");
         pets.add("wither");
         pets.add("bluewither");
         pets.add("witherskull");
         pets.add("bluewitherskull");
        // pets.add("enderdragon");
 
         Util.load(getConfig());
         File dataFile = new File(getDataFolder(), "saves.yml");
         controller = new PetController(this);
         storage = new Storage(dataFile, controller);
         storage.load();
         Bukkit.getPluginManager().registerEvents(controller, this);
         Bukkit.getPluginManager().registerEvents(storage, this);
         Bukkit.getPluginManager().registerEvents(new MenuManager(), this);
 
         menu = new Menu("Pets", Util.makeItemStack(new ItemStack(Material.BONE), "Pets", Arrays.asList(ChatColor.GRAY + "Select a pet!")), Menu.Size.SIX_LINE);
         Menu sheep = new Menu("Sheep Colors", Util.makeItemStack(new ItemStack(Material.WOOL, 1, DyeColor.WHITE.getWoolData()), "Sheep", Arrays.asList(ChatColor.GRAY + "Select a sheep color!")), Menu.Size.TWO_LINE);
 
 
         int i = 0;
         for (String s : pets) {
             if (s.endsWith("sheep") || s.endsWith("name")) {
                 continue;
             }
             menu.getElements().put(i, new PetMenuElement(new PetMenuItem("pet." + s)));
             i++;
         }
 
         i += 3;
         int x = 0;
         for (String s : pets) {
             if (!s.endsWith("sheep")){
                 continue;
             }
             sheep.getElements().put(x, new PetMenuElement(new PetMenuItem("pet." + s)));
             x++;
         }
 
 
         menu.getElements().put(53, new PetMenuToggleElement());
         sheep.getElements().put(17, menu);
         menu.getElements().put(i, sheep);
 
         saveConfig();
     }
 }
