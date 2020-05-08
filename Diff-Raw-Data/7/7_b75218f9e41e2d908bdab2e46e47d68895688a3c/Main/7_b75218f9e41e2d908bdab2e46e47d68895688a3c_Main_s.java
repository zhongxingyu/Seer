 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package ircrpg2;
 import ircrpg2.core.*;
 import ircrpg2.messaging.irc.*;
 import ircrpg2.messaging.*;
 import ircrpg2.entities.ilandor.*;
 import ircrpg2.persistence.Landscape;
 import ircrpg2.persistence.NPCLoader;
 import ircrpg2.persistence.PersistedMap;
 import java.io.*;
 import java.util.ArrayList;
 /**
  *
  * @author testi
  */
 public class Main {
     
     public static void main(String[] args) {
 
     File persistenceDir = new File(new File(System.getProperty("user.home")),".ircrpg");
     if (!persistenceDir.exists()){
     persistenceDir.mkdirs();
     }
     File settingsFile = new File(persistenceDir,"settings");
 
     PersistedMap settings = new PersistedMap();
     try {
     if (!settingsFile.exists()) {
     FirstRun(settings);
     settings.write(settingsFile);
     }
     else {
     settings.read(settingsFile);
     }
     
     String network = settings.get("network");
     if (network == null) {throw new Exception("No network entry found");}
     String portString = settings.get("port");
     if (portString == null) {throw new Exception("No port entry found");}
     int port = Integer.parseInt(portString);
     
     String nickname = settings.get("nickname");
     if (nickname == null) {throw new Exception("No nickname entry found");}
 
 
     ArrayList<String> nickNames = new ArrayList<String>();
     String otherNick = null;
     int nickId = 2;
     while ((otherNick = settings.get("nickname" + nickId)) != null) {
     nickNames.add(otherNick);
     nickId++;
     }
 
     
 
     String password = settings.get("password");
     if (password == null) {throw new Exception("No password entry found");}
 
     String name = settings.get("name");
     if (name == null) {throw new Exception("No name entry found");}
 
     String email = settings.get("email");
     if (email == null) {throw new Exception("No email entry found");}
 
     String channel = settings.get("channel");
     if (channel == null) {throw new Exception("No channel entry found");}
 
 
     File landscapeDir = new File(persistenceDir,"landscape");
     String pathDir = new File(landscapeDir,"paths").getAbsolutePath();
     String localityDir = new File(landscapeDir,"localities").getAbsolutePath();
     Landscape landscape = null;
 
     if (landscapeDir.isDirectory()) {
     landscape = new Landscape(pathDir, localityDir);
     //landscape.load();
     }
 
     String npcDir = new File(persistenceDir,"npcs").getAbsolutePath();
     NPCLoader npcLoader = new NPCLoader(npcDir);
 
 
 
    World defaultWorld = new DefaultWorld();
 
     Bot mainBot = new Bot(network, port, nickname, password, name, email, channel, new CommandHandler(defaultWorld));
 
     Bot[] otherBots = new Bot[nickNames.size()+1];
     otherBots[0] = mainBot;
     for (int i = 0; i < nickNames.size(); i++) {
     otherBots[i+1] = new Bot(network, port, nickNames.get(i), password, name, email, channel, null);
     }
    LoadBalancer rpgBot = new LoadBalancer(otherBots);
 
 
     defaultWorld.setMessagingService(rpgBot);
     if (landscape != null) {
     //landscape.connect(defaultWorld);
     defaultWorld.init(landscape,npcLoader);
     } else {
     defaultWorld.init();
     }
     rpgBot.connect();
     String line;
 
     Runtime.getRuntime().addShutdownHook(new Thread() {
                 
                
             public void run() {
         synchronized (defaultWorld) {
 	System.out.println("Shutdown sequence");
 	synchronized (defaultWorld) {
         System.out.print("Persisting..");
         defaultWorld.getLibrary().persistPlayers();
         System.out.println(" done.");
         rpgBot.teardown("Shutdown signal");
                 }
                 }
             });
 
     while (!"quit".equals(line = System.console().readLine())) {
 
     }
     System.out.println("Shutdown sequence");
     synchronized (defaultWorld) {
         System.out.print("Persisting..");
         defaultWorld.getLibrary().persistPlayers();
         System.out.println(" done.");
         rpgBot.teardown("Normal shutdown");
         System.exit(0);
     }
     } catch (IOException ex) {
     System.out.println(ex);
     }
     catch (NumberFormatException ex) {
     System.err.println("Invalid port number entry");
     System.exit(1);
     }
     catch (Exception ex) {
     System.err.println(ex.getMessage());
     System.exit(1);
     }
     }
 
     public static void FirstRun(PersistedMap settings) {
         System.out.println("Erste Ausführung von IRC-RPG");
         System.out.print("Addresse des IRC-Netzwerks: ");
         settings.put("network", System.console().readLine());
 
         int port;
         while(true) {
         System.out.print("Port (normalerweise 6667): ");
         try {
         port = Integer.parseInt(System.console().readLine());
         } catch (NumberFormatException ex) {continue;}
         break;
         }
 
         settings.put("port", String.valueOf(port));
 
         System.out.print("Nickname: ");
         settings.put("nickname", System.console().readLine());
 
         System.out.print("Nickname-Password: ");
         settings.put("password", String.valueOf(System.console().readPassword()));
 
         System.out.print("Name: ");
         settings.put("name", System.console().readLine());
 
         System.out.print("E-Mail: ");
         settings.put("email", System.console().readLine());
 
         System.out.print("IRC-RPG Channel: ");
         settings.put("channel", System.console().readLine());
 
     }
 
 }
