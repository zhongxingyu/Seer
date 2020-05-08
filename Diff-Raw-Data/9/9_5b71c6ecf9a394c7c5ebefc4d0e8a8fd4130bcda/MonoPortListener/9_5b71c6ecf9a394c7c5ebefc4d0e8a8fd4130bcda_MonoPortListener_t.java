 
 /**
  * Listens for Events
  * @author Reebdoog
  */
 
 import java.util.Date;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.io.File;
 import java.io.FileWriter;
 import java.util.Scanner;
 import java.util.Random;
 import java.io.IOException;
 import java.util.ArrayList;
 
 public class MonoPortListener extends PluginListener {
 
     public static PropertiesFile properties;
     private static ArrayList<String> playerList = new ArrayList<String>();
     private static ArrayList<Block> playerCube = new ArrayList<Block>();
     private static ArrayList<Block> portalCube = new ArrayList<Block>();
     
     public String getDateTime(){
         DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
         Date date = new Date();
         return dateFormat.format(date);
     }
 
     public boolean onCommand(Player player, String[] split) {
         if  (split[0].equalsIgnoreCase("/setdest") && player.canUseCommand("/setdest")){
              if (split.length > 1){
                  if (setDestination(player, split[1])){
                      player.sendMessage(Colors.Rose + "Destination set");
                  }
                  else {
                     player.sendMessage(Colors.Rose + "Destination failed to be set");
                  }
              }
              else {
                 player.sendMessage(Colors.Rose + "Correct usage is: /setdest [nameForDestination]");
              }
              return true;
         }
         else if (split[0].equalsIgnoreCase("/makeportal") && player.canUseCommand("/makeportal")){
             if (split.length > 2){
                 int index = getPlayerIndex(player.getName());
                 Block block = playerCube.get(index);
                 String destination = split[2];
                 String portalName = split[1];
                 String usePayment = "free";
                 if (split.length == 4){
                     if (split[3].equals("paid")){
                         usePayment = split[3];
                     }
                 }
                 if (createPortal(player, block, destination, portalName, usePayment)){
                     player.sendMessage(Colors.Rose + "Portal " + portalName + " created to " + destination);
                     if (!loadPortalsIntoArray())
                     {
                         System.out.println(getDateTime()+" [ERROR] MonoPort plugin : Couldn't load all portals");
                     }
                 }
                  else {
                     player.sendMessage(Colors.Rose + "Failed to create portal");
                  }
                  return true;
             }
             else{
                 player.sendMessage(Colors.Rose + "Correct usage is: /makeportal [nameForPortal] [nameOfDestination]");
                 return true;
             }
         }
         else if (split[0].equalsIgnoreCase("/delportal") && player.canUseCommand("/delportal")){
             if (split.length > 1){
                 String portalName = split[1];
                 if (removePortal(player, portalName)){
                     player.sendMessage(Colors.Rose + "Portal " + portalName + " removed");
                     if (!loadPortalsIntoArray())
                     {
                         System.out.println(getDateTime()+" [ERROR] MonoPort plugin : Couldn't load all portals");
                     }
                 }
             }
             else {
                 player.sendMessage(Colors.Rose + "Correct usage is: /delportal [nameOfPortal]");
             }
             return true;
         }
         else if (split[0].equalsIgnoreCase("/showportallinks") && player.canUseCommand("/showportallinks")){
             showPortalLinks(player);
              return true;
         }
         else if (split[0].equalsIgnoreCase("/showdestinations") && player.canUseCommand("/showdestinations")){
             showDestinations(player);
              return true;
         }
         else if (split[0].equalsIgnoreCase("/addportaldest") && player.canUseCommand("/addportaldest")){
             if (split.length > 2){
                 String portalName = split[1];
                 String destName = split[2];
                 addPortalDestination(player, portalName, destName);
             }
             else {
                 player.sendMessage(Colors.Rose + "Correct usage is: /addportaldest [nameOfPortal] [nameOfDest]");
             }
              return true;
         }
         else if (split[0].equalsIgnoreCase("/remportaldest") && player.canUseCommand("/remportaldest")){
              if (split.length > 2){
                 String portalName = split[1];
                 String destName = split[2];
                 remPortalDestination(player, portalName, destName);
             }
             else {
                 player.sendMessage(Colors.Rose + "Correct usage is: /remportaldest [nameOfPortal] [nameOfDest]");
             }
              return true;
         }
         //Must return false, or else no other commands will be checked!
         return false;
     }
     public boolean onBlockCreate(Player player, Block blockPlaced, Block blockClicked, int itemInHand){
         int handItemType = properties.getInt("itemInHand", 290);
         int adminHandItemType = properties.getInt("adminItemInHand", 270);
         int portalBlockType = properties.getInt("portalBlockType", 7);
         String playerName = player.getName();
         //If admin then set warp
         if (blockClicked.getType() == portalBlockType && player.canUseCommand("/monoport") && itemInHand == adminHandItemType){
             //scan to see if block location exists
             if (findPortalName(blockClicked) == null)
             {
                 int playerIndex = getPlayerIndex(playerName);
                 playerCube.set(playerIndex, blockClicked);
                 player.sendMessage(Colors.Rose + "Portal Entrance Set. Use /makeportal to create the portal");
             }
              else {
                 player.sendMessage(Colors.Rose + "Portal already Exists.");
              }
             return true;
         }
         //If user then goto warp location
         else if (blockClicked.getType() == portalBlockType && player.canUseCommand("/useportal")
                 && (itemInHand == handItemType || handItemType == 0)){
             String portalName = findPortalName(blockClicked);
             if ( portalName != null)
             {
                 ArrayList<String> destinations = getDestinationsFromPortal(portalName);
                 if (destinations != null){
                     String destName = getRandomPortalName(destinations);
                     portPlayer(player,destName,portalName);
                 }
             }
              else {
                 return false;
              }
         }
         //Must return false, or else no other commands will be checked!
         return false;
     }
 
     public void onPlayerMove(Player player, Location from, Location to) {
         if (player.canUseCommand("/useportal")) {
             boolean isPortal = false;
             Block portalBlock = new Block();
             int portalHeight = properties.getInt("portalHeight", 2);
             for (Block b : portalCube) {
                 if (b.getWorld().equals(player.getWorld())) {
                     if (b.getX() == (int) to.x) {
                         if (b.getZ() == (int) to.z) {
                             // Y is height in Minecraft
                             if ((b.getY() + portalHeight >= (int) to.y && b.getY() <= (int) to.y)
                                     || (b.getY() + portalHeight <= (int) to.y && b.getY() >= (int) to.y)) {
                                 isPortal = true;
                                 portalBlock = b;
                             }
                         }
                     }
                 }
             }
             if (isPortal) {
                 String portalName = findPortalName(portalBlock);
                 if (portalName != null) {
                     ArrayList<String> destinations = getDestinationsFromPortal(portalName);
                     if (destinations != null) {
                         String destName = getRandomPortalName(destinations);
                         portPlayer(player, destName, portalName);
                     }
                 }
             }
         }
     }
     private void portPlayer(Player player, String destName, String portalName){
         boolean canPortal = false;
         int paymentItemType = properties.getInt("paymentItemType", 266);
         int paymentAmount = properties.getInt("paymentAmount", 1);
         boolean doesCost = doesPortalCost(portalName);
         if (paymentItemType != 0 && doesCost){
             if (player.getInventory().hasItem(paymentItemType, paymentAmount, 6400))
             {
                 Item paymentItem = player.getInventory().getItemFromId(paymentItemType);
                 int amount = paymentItem.getAmount();
                 paymentItem.setAmount(amount - paymentAmount);
                 player.getInventory().removeItem(player.getInventory().getItemFromId(paymentItemType));
                 player.getInventory().addItem(paymentItem);
                 player.getInventory().update();
                 player.sendMessage(Colors.Rose + "Collecting Payment");
                 canPortal = true;
             }
              else {
                 player.sendMessage(Colors.Rose + "You do not have enough to use the portal");
                 canPortal = false;
              }
         }
         else {
             canPortal = true;
         }
         if (canPortal){
             player.sendMessage(Colors.Rose + "Using Portal " + portalName + " to " + destName);
             String destCoords = getDestinationCoords(destName);
             Location destination = new Location();
             String[] tempdata = destCoords.split(",");
             destination.dimension = destCoords.startsWith("d") ? 0:-1;
             destination.x = Double.parseDouble(tempdata[2]);
             destination.y = Double.parseDouble(tempdata[3]);
             destination.z = Double.parseDouble(tempdata[4]);
             destination.rotX = Float.parseFloat(tempdata[6]);
             destination.rotY = Float.parseFloat(tempdata[5]);
             player.teleportTo(destination);
         }
     }
     public void checkDataFile(){
         File dataSource = new File("monoPortWarps.txt");
         if (!dataSource.exists()) {
             FileWriter writer = null;
             try {
                 writer = new FileWriter(dataSource);
                 writer.write("#Portal locations are stored in here\r\n");
                 writer.write("#d signifies destination\r\n");
                 writer.write("#e signifies entrance\r\n");
 	    }
             catch (Exception e) {
                 System.out.println(getDateTime()+" [ERROR] Exception while creating monoPortWarps.txt");
             }
             finally {
                 try {
                     if (writer != null) {
                         writer.close();
                     }
                 } catch (IOException e) {
                     System.out.println(getDateTime()+" [ERROR] Exception while closing writer for monoPortWarps.txt");
                 }
             }
         }
     }
     public boolean loadPortalsIntoArray(){
         File dataSource = new File("monoPortWarps.txt");
         if (dataSource.exists()) {
             Scanner scanner = null;
             try {
                 scanner = new Scanner(dataSource);
                 String[] tempdata = {""};
                 portalCube.clear();
                 while (scanner.hasNextLine()) {
                     Block portalBlock = new Block();
                     String line = scanner.nextLine();
                     if (!(line.startsWith("e") || line.startsWith("Ne")) || line.equals("")) {
                         continue;
                     }
                     if (line.startsWith("e")) {
                         portalBlock.setWorld(etc.getServer().getWorld(0));
                     } else {
                         portalBlock.setWorld(etc.getServer().getWorld(-1));                        
                     }
                     tempdata = line.split(",");
                     portalBlock.setX(Integer.parseInt(tempdata[2]));
                     portalBlock.setY(Integer.parseInt(tempdata[3]));
                     portalBlock.setZ(Integer.parseInt(tempdata[4]));
                     portalCube.add(portalBlock);
                 }
             }
             catch (Exception e) {
                 System.out.println(getDateTime()+" [ERROR] : MonoPort plugin : Exception while loading destinations");
                 return false;
             }
             finally {
                 if (scanner != null) {
                     scanner.close();
                 }
             }
             return true;
         }
         return false;
     }
     private boolean setDestination(Player player, String location){
         if (!doesDestinationExist(location))
         {
             File dataSource = new File("monoPortWarps.txt");
             if (dataSource.exists()) {
                 FileWriter writer = null;
                 String key = "d";
                 try {
                     String playerLocation = player.getX() + "," + player.getY() +  "," + player.getZ();
                     String playerView = player.getPitch() + "," + player.getRotation();
                     writer = new FileWriter(dataSource,true);
                     if (player.getWorld().getType().getId() == -1) {
                        key = "Nd";
                     }
                     writer.write(key+"," + location + "," + playerLocation + "," + playerView + "\r\n");
                 }
                 catch (Exception e) {
                     System.out.println(getDateTime()+" [ERROR] Exception while writing to monoPortWarps.txt");
                 }
                 finally {
                     try {
                         if (writer != null) {
                             writer.close();
                         }
                         return true;
                     } catch (IOException e) {
                         System.out.println(getDateTime()+" [ERROR] Exception while closing writer for monoPortWarps.txt");
                     }
                 }
             }
         }
         else {
             player.sendMessage(Colors.Rose + "Destination name already exists");
         }
         return false;
     }
     private void showDestinations(Player player){
         File dataSource = new File("monoPortWarps.txt");
         if (dataSource.exists()) {
             Scanner scanner = null;
             try {
                 scanner = new Scanner(dataSource);
                 String[] tempdata = {""};
                 while (scanner.hasNextLine()) {
                     String line = scanner.nextLine();
                    if (!(line.startsWith("d") || line.startsWith("Nd")) || line.equals("")) {
                         continue;
                     }
                     tempdata = line.split(",");
                     player.sendMessage(Colors.Rose + tempdata[1]);
                 }
             }
             catch (Exception e) {
                 System.out.println(getDateTime()+" [ERROR] : MonoPort plugin : Exception while loading destinations");
             }
             finally {
                 if (scanner != null) {
                     scanner.close();
                 }
             }
         }
     }
     private void showPortalLinks(Player player){
         File dataSource = new File("monoPortWarps.txt");
         if (dataSource.exists()) {
             Scanner scanner = null;
             try {
                 scanner = new Scanner(dataSource);
                 String[] tempdata = {""};
                 while (scanner.hasNextLine()) {
                     String line = scanner.nextLine();
                     if (line.startsWith("Ne")) {
                         line = line.substring(1);
                     }
                     if (!line.startsWith("e") || line.equals("")) {
                         continue;
                     }
                     tempdata = line.split(",");
                     player.sendMessage(Colors.Rose + tempdata[1] + " - " + tempdata[8]);
                 }
                 scanner.close();
             }
             catch (Exception e) {
                 System.out.println(getDateTime()+" [ERROR] : MonoPort plugin : Exception while loading portal links");
             }
             finally {
                 if (scanner != null) {
                     scanner.close();
                 }
             }
         }
     }
     private boolean doesDestinationExist(String destination){
         File dataSource = new File("monoPortWarps.txt");
         if (dataSource.exists()) {
             Scanner scanner = null;
             try {
                 scanner = new Scanner(dataSource);
                 String[] tempdata = {""};
                 while (scanner.hasNextLine()) {
                     String line = scanner.nextLine();
                     if (line.startsWith("N")) {
                         line = line.substring(1);
                     }
                     if (!line.startsWith("d") || line.equals("")) {
                         continue;
                     }
                     tempdata = line.split(",");
                     if (tempdata[1].equals(destination))
                     {
                         scanner.close();
                         return true;
                     }
                 }
                 scanner.close();
             }
             catch (Exception e) {
                 System.out.println(getDateTime()+" [ERROR] : MonoPort plugin : Exception while loading destinations");
                 return true;
             }
             finally {
                 if (scanner != null) {
                     scanner.close();
                 }
             }
             return false;
         }
         System.out.println(getDateTime()+" [ERROR] : MonoPort plugin : Failed to Load text file");
         return true;
     }
     private static int getPlayerIndex(String playerName){
             boolean inList = false;
             for (String p : playerList){
                     if (p==playerName)
                             inList = true;
             }
             if (!inList){
                     playerList.add(playerName);
                     playerCube.add(null);
             }
             return playerList.indexOf(playerName);
     }
     private String findPortalName(Block block){
         File dataSource = new File("monoPortWarps.txt");
         if (dataSource.exists()) {
             try {
                 Scanner scanner = new Scanner(dataSource);
                 String[] tempdata = {""};
                 while (scanner.hasNextLine()) {
                     String line = scanner.nextLine();
                     if (line.startsWith("Ne")) {
                         if (block.getWorld().getType().getId() != -1) {
                             continue;
                         } else {
                             line = line.substring(1);
                         }
                     }
                     if (!line.startsWith("e") || line.equals("")) {
                         continue;
                     }
                     tempdata = line.split(",");
                     if (Integer.toString(block.getX()).equals(tempdata[2])
                             && Integer.toString(block.getY()).equals(tempdata[3])
                             && Integer.toString(block.getZ()).equals(tempdata[4])){
                         scanner.close();
                         return tempdata[1];
                     }
                 }
                 scanner.close();
             }
             catch (Exception e) {
                 System.out.println(getDateTime()+" [ERROR] : MonoPort plugin : Exception while loading destinations");
                 return null;
             }
             return null;
         }
         return null;
     }
     private String getDestinationCoords(String destination){
         File dataSource = new File("monoPortWarps.txt");
         if (dataSource.exists()) {
             try {
                 Scanner scanner = new Scanner(dataSource);
                 String[] tempdata = {""};
                 while (scanner.hasNextLine()) {
                     String line = scanner.nextLine();
                     if (!(line.startsWith("d") || line.startsWith("Nd")) || line.equals("")) {
                         continue;
                     }
                     tempdata = line.split(",");
                     if (tempdata[1].equals(destination)){
                         scanner.close();
                         return line;
                     }
                 }
                 scanner.close();
             }
             catch (Exception e) {
                 System.out.println(getDateTime()+" [ERROR] : MonoPort plugin : Exception while loading destinations");
                 return null;
             }
             return null;
         }
         return null;
     }
     private ArrayList<String> getDestinationsFromPortal(String portalName){
         File dataSource = new File("monoPortWarps.txt");
         ArrayList<String> destinations = new ArrayList<String>();
         if (dataSource.exists()) {
             try {
                 Scanner scanner = new Scanner(dataSource);
                 String[] tempdata = {""};
                 while (scanner.hasNextLine()) {
                     String line = scanner.nextLine();
                     if (!(line.startsWith("e") || line.startsWith("Ne")) || line.equals("")) {
                         continue;
                     }
                     tempdata = line.split(",");
                     if (tempdata[1].equals(portalName)){
                         destinations.add(tempdata[8].toString());
                     }
                 }
                 scanner.close();
                 return destinations;
             }
             catch (Exception e) {
                 System.out.println(getDateTime()+" [ERROR] : MonoPort plugin : Exception while loading destinations");
                 return null;
             }
         }
         return null;
     }
     private boolean createPortal(Player player, Block block, String destination, String name, String usePayment){
         File dataSource = new File("monoPortWarps.txt");
         Boolean destExists = false;
         Boolean portalExists = false;
         String portalCode = block.getWorld().getType().getId() == -1 ? "Ne":"e";
         if (dataSource.exists()) {
             try {
                 Scanner scanner = new Scanner(dataSource);
                 String[] tempdata = {""};
                 while (scanner.hasNextLine()) {
                     String line = scanner.nextLine();
                     if (line.startsWith("d") || line.startsWith("Nd")){
                         tempdata = line.split(",");
                         if (tempdata[1].equals(destination))
                         {
                             destExists = true;
                         }
                     }
                     else if (line.startsWith(portalCode)){
                         tempdata = line.split(",");
                         if (tempdata[1].equals(name))
                         {
                             portalExists = true;
                         }
                     }
                     else
                     {
                         continue;
                     }
                 }
                 scanner.close();
             }
             catch (Exception e) {
                 System.out.println(getDateTime()+" [ERROR] : MonoPort plugin : Exception while loading destinations");
                 destExists = false;
             }
         }
         else
         {
             return false;
         }
         if (destExists && !portalExists)
         {
             FileWriter writer = null;
             try {
                 String blockLocation = block.getX() + "," + block.getY() +  "," + block.getZ();
                 writer = new FileWriter(dataSource,true);
                 writer.write(portalCode+"," + name + "," + blockLocation + ",0,0," + usePayment + "," + destination + "\r\n");
             }
             catch (Exception e) {
                 System.out.println(getDateTime()+" [ERROR] Exception while writing to monoPortWarps.txt " + e.getMessage());
                 return false;
             }
             finally {
                 try {
                     if (writer != null) {
                         writer.close();
                     }
                     return true;
                 } catch (IOException e) {
                     System.out.println(getDateTime()+" [ERROR] Exception while closing writer for monoPortWarps.txt");
                 }
             }
         }
          else if (portalExists) {
             player.sendMessage(Colors.Rose + "Portal name already exists");
          }
         else {
             player.sendMessage(Colors.Rose + "Destination name doesn't exist");
         }
         return false;
     }
     private boolean removePortal(Player player, String portal){
         File dataSource = new File("monoPortWarps.txt");
         if (getDestinationsFromPortal(portal) != null)
         {
             ArrayList<String> portalFile = new ArrayList<String>();
             if (dataSource.exists()) {
                 try {
                     String[] tempdata = {""};
                     Scanner scanner = new Scanner(dataSource);
                     while (scanner.hasNextLine()) {
                         String line = scanner.nextLine();
                         if (line.equals("")) {
                             continue;
                         }
                         tempdata = line.split(",");
                         if (tempdata.length > 2)
                             {
                             if (tempdata[1].equals(portal) && (tempdata[0].equals("e") || tempdata[0].equals("Ne")))
                             {
                                 continue;
                             }
                             else {
                                 portalFile.add(line);
                             }
                         }
                         else {
                             portalFile.add(line);
                         }
                     }
                     scanner.close();
                 }
                 catch (Exception e) {
                     System.out.println(getDateTime()+" [ERROR] : MonoPort plugin : Exception while loading destinations");
                     return false;
                 }
                 return writeFile(portalFile, player);
             }
         }
         return false;
     }
     private void addPortalDestination(Player player, String portalName, String destName){
         File dataSource = new File("monoPortWarps.txt");
         Boolean destExists = false;
         Boolean portalExists = false;
         String portalInfo = "";
         //ToDo: Add free and paid portals.
         if (dataSource.exists()) {
             try {
                 Scanner scanner = new Scanner(dataSource);
                 String[] tempdata = {""};
                 while (scanner.hasNextLine()) {
                     String line = scanner.nextLine();
                     if (line.startsWith("d") || line.startsWith("Nd")){
                         tempdata = line.split(",");
                         if (tempdata[1].equals(destName))
                         {
                             destExists = true;
                         }
                     }
                     else if (line.startsWith("e") || line.startsWith("Ne")){
                         tempdata = line.split(",");
                         if (tempdata[1].equals(portalName))
                         {
                             portalExists = true;
                             portalInfo = line;
                         }
                     }
                     else
                     {
                         continue;
                     }
                 }
                 scanner.close();
             }
             catch (Exception e) {
                 System.out.println(getDateTime()+" [ERROR] : MonoPort plugin : Exception while loading portal information");
                 destExists = false;
             }
         }
         else
         {
             //return false;
         }
         if (destExists && portalExists)
         {
             FileWriter writer = null;
             try {
                 int lastSplit = portalInfo.lastIndexOf(",");
                 String newPortal = portalInfo.substring(0, lastSplit + 1) + destName + "\r\n";
                 writer = new FileWriter(dataSource,true);
                 writer.write(newPortal);
             }
             catch (Exception e) {
                 System.out.println(getDateTime()+" [ERROR] Exception while writing to monoPortWarps.txt");
                 //return false;
             }
             finally {
                 try {
                     if (writer != null) {
                         writer.close();
                         player.sendMessage(Colors.Rose + "Portal destination added");
                     }
                     //return true;
                 } catch (IOException e) {
                     System.out.println(getDateTime()+" [ERROR] Exception while closing writer for monoPortWarps.txt");
                 }
             }
         }
          else if (!portalExists) {
             player.sendMessage(Colors.Rose + "Portal name doesn't exist");
          }
         else {
             player.sendMessage(Colors.Rose + "Destination name doesn't exist");
         }
         //return false;
     }
     private String getRandomPortalName(ArrayList<String> destName){
         //Gets a random index
         Random r = new Random();
         int randomIndex = r.nextInt(destName.size());
         return destName.get(randomIndex);
     }
     private boolean doesPortalCost(String portalName){
         File dataSource = new File("monoPortWarps.txt");
         if (dataSource.exists()) {
             try {
                 Scanner scanner = new Scanner(dataSource);
                 String[] tempdata = {""};
                 while (scanner.hasNextLine()) {
                     String line = scanner.nextLine();
                     if (!(line.startsWith("e") || line.startsWith("Ne")) || line.equals("")) {
                         continue;
                     }
                     tempdata = line.split(",");
                     if (tempdata[1].equals(portalName) && tempdata[7].equals("paid")){
                         scanner.close();
                         return true;
                     }
                 }
                 scanner.close();
             }
             catch (Exception e) {
                 System.out.println(getDateTime()+" [ERROR] : MonoPort plugin : Exception while loading destinations");
             }
         }
         return false;
     }
     private boolean remPortalDestination(Player player, String portal, String dest){
         File dataSource = new File("monoPortWarps.txt");
         if (getDestinationsFromPortal(portal) != null)
         {
             ArrayList<String> portalFile = new ArrayList<String>();
             if (dataSource.exists()) {
                 try {
                     String[] tempdata = {""};
                     Scanner scanner = new Scanner(dataSource);
                     while (scanner.hasNextLine()) {
                         String line = scanner.nextLine();
                         if (line.equals("")) {
                             continue;
                         }
                         tempdata = line.split(",");
                         if (tempdata.length > 7)
                         {
                             if (tempdata[1].equals(portal) && (tempdata[0].equals("e")|| tempdata[0].equals("Ne")) && tempdata[8].equals(dest))
                             {
                                 continue;
                             }
                             else {
                                 portalFile.add(line);
                             }
                         }
                         else {
                             portalFile.add(line);
                         }
                     }
                     scanner.close();
                 }
                 catch (Exception e) {
                     System.out.println(getDateTime()+" [ERROR] : MonoPort plugin : Exception while loading destinations");
                     return false;
                 }
                 return writeFile(portalFile, player);
             }
         }
         return false;
     }
     private boolean writeFile(ArrayList<String> portalFile , Player player){
         File dataSource = new File("monoPortWarps.txt");
         FileWriter writer = null;
         try {
             writer = new FileWriter(dataSource);
             for (String s : portalFile)
             {
                 writer.write(s + "\r\n");
             }
         }
         catch (Exception e) {
             System.out.println(getDateTime()+" [ERROR] Exception while writing to monoPortWarps.txt");
         }
         finally {
             try {
                 if (writer != null) {
                     player.sendMessage(Colors.Rose + "Portal destination removed");
                     writer.close();
                 }
                 return true;
             } catch (IOException e) {
                 System.out.println(getDateTime()+" [ERROR] Exception while closing writer for monoPortWarps.txt");
             }
         }
         return false;
     }
 }
