 package autoeq.eq;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.OutputStreamWriter;
 import java.io.PrintWriter;
 import java.net.Socket;
 import java.net.UnknownHostException;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.LinkedHashMap;
 import java.util.LinkedHashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.logging.FileHandler;
 import java.util.logging.Formatter;
 import java.util.logging.Handler;
 import java.util.logging.LogRecord;
 import java.util.logging.Logger;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 
 import autoeq.EverquestModule;
 import autoeq.ExpressionEvaluator;
 import autoeq.SpellData;
 import autoeq.effects.AlternateAbilityEffect;
 import autoeq.effects.DisciplineEffect;
 import autoeq.effects.Effect;
 import autoeq.effects.ItemEffect;
 import autoeq.effects.SongEffect;
 import autoeq.effects.SpellEffect;
 import autoeq.ini.Ini2;
 import autoeq.ini.Section;
 import autoeq.modules.pull.MoveModule;
 
 import com.google.inject.Guice;
 import com.google.inject.Injector;
 
 public class EverquestSession {
   private final MyThread thread;
   private final List<Module> modules = new ArrayList<>();
   private final String sessionName;
   private final Map<Integer, SpellData> rawSpellData;
   private final Ini2 globalIni;
 
   private final Map<Integer, Spawn> spawns = new HashMap<>();
   private final Map<Integer, Spell> spells = new HashMap<>();
   private final Set<String> groupMemberNames = new HashSet<>();
   private final Set<String> botNames = new HashSet<>();
   private final Set<ProfileSet> profileSets = new LinkedHashSet<>();
   private final Set<String> ignoreList = new HashSet<>();
 
   private Ini2 ini;
   private File iniFile;
   private long iniLastModified;
   private long castLockEndMillis = Long.MIN_VALUE;
 
   private String charName;
   private String alternateName;
   private int charId;
   private int zoneId;
   private boolean zoning = true;
   private Logger logger;
 
   private Module activeModule;
   private boolean debug = false;
 
   public EverquestSession(Map<Integer, SpellData> rawSpellData, Ini2 globalIni, String host, int port, String username, String password) throws UnknownHostException, IOException, InterruptedException {
     this.rawSpellData = rawSpellData;
     this.globalIni = globalIni;
 
     @SuppressWarnings("resource")
     Socket socket = new Socket(host, port);
 
     socket.setSoTimeout(1000);
 
     BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
     PrintWriter writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
 
     System.out.println("Logging in");
 
     while(!(reader.read() == ':')) {
     }
 
     System.out.println("Sending username");
 
     Thread.sleep(300);
 
     writer.println(username);
     writer.flush();
 
     while(!(reader.read() == ':')) {
     }
 
     Thread.sleep(300);
 
     System.out.println("Sending password");
 
     writer.println(password);
     writer.flush();
 
     System.out.println(">Waiting for lines");
 
     if(!reader.readLine().startsWith("Succe")) {
       throw new RuntimeException("Unable to connect, no welcome mesage");
     }
 
     socket.setSoTimeout(0);
 
     thread = new MyThread(reader, writer);
     thread.start();
 
     //onFinishedZoning();
     sessionName = "EverquestSession(" + port + ")";
   }
 
   public void addModule(Module module) {
     modules.add(module);
   }
 
   private Lock<Module> moveLock;
 
   private Lock<Module> getMoveLock() {
     return moveLock;
   }
 
   public void lockMovement() {
     getMoveLock().lock(activeModule);
   }
 
   public boolean tryLockMovement() {
     return getMoveLock().tryLock(activeModule);
   }
 
   public void unlockMovement() {
     getMoveLock().unlock(activeModule);
   }
 
 
   /**
    * @return a set of spawns with all group members in this zone
    */
   public Set<Spawn> getGroupMembers() {
     HashSet<Spawn> members = new HashSet<>();
 
     for(String name : groupMemberNames) {
       Spawn spawn = getSpawn(name);
 
       if(spawn != null) {
         members.add(spawn);
       }
     }
 
     return members;
   }
 
   /**
    * @return a set of spawns with all bots in this zone
    */
   public Set<Spawn> getBots() {
     HashSet<Spawn> bots = new HashSet<>();
 
     for(String name : botNames) {
       Spawn spawn = getSpawn(name);
 
       if(spawn != null) {
         bots.add(spawn);
       }
     }
 
     return bots;
   }
 
   public void setBotNames(Set<String> names) {
     botNames.clear();
     botNames.addAll(names);
   }
 
   public Set<String> getBotNames() {
     return botNames;
   }
 
   public int getZoneId() {
     return zoneId;
   }
 
   public void setCastLockOut(long ms) {
     castLockEndMillis = System.currentTimeMillis() + ms;
   }
 
   private final Map<String, UserCommandWrapper> userCommands = new HashMap<>();
 
   public void addUserCommand(String name, Pattern parameters, String helpText, UserCommand command) {
     System.out.println("Added user command : " + name);
     userCommands.put(name, new UserCommandWrapper(command, parameters, helpText));
   }
 
   private final Map<String, Long> timers = new LinkedHashMap<>();
   private final Map<String, Long> startedTimers = new LinkedHashMap<>();
 
   private void startTimer(String name) {
     long millis = System.currentTimeMillis();
     startedTimers.put(name, millis);
   }
 
   private void endTimer(String name) {
     long millis = System.currentTimeMillis();
     Long startTime = startedTimers.remove(name);
     if(startTime == null) {
       System.err.println("Timer nesting problem with: " + name);
       return;
     }
 
     Long accumulatedTime = timers.get(name);
     if(accumulatedTime == null) {
       accumulatedTime = 0L;
     }
     timers.put(name, accumulatedTime + (millis - startTime));
   }
 
   private long lastUpdate = 0;
 
   public void pulse() {
     try {
 //      if(zoning) {
 //        onFinishedZoning();
 //        // if we make it to here without ZoningException, zoning is over.
 //        zoning = false;
 //      }
 
       if(System.currentTimeMillis() - lastUpdate > 10000) {
         lastUpdate = System.currentTimeMillis();
         if(getMe() != null) {
           //System.err.println("Timers for " + getMe().getName() + " : " + timers);
         }
       }
 
       /*
        * Process chat lines
        */
 
       startTimer("General Pulse Processing");
 
       for(;;) {
         String line;
 
         synchronized(chatLines) {
           if(chatLines.size() == 0) {
             break;
           }
 
           if(chatLines.getFirst().startsWith("#")) {
             // Encountered a data burst.  This should be processed before other chat lines.
             break;
           }
 
           line = chatLines.removeFirst();
         }
 
         if(line.startsWith("[MQ2] jb-")) {
           String cmd = line.substring(10);
 
           log("USERCMD: " + cmd);
 
          // TODO concurrentmod exception here
           for(String s : userCommands.keySet()) {
             if(cmd.equals(s) || cmd.startsWith(s + " ")) {
               UserCommandWrapper userCommandWrapper = userCommands.get(s);
               Matcher m = userCommandWrapper.pattern.matcher(cmd.substring(s.length()).trim());
 
               if(m.matches()) {
                 userCommandWrapper.userCommand.onCommand(m);
               }
               else {
                 doCommand("/echo JB: syntax error, use: " + s + " " + userCommandWrapper.helpText);
               }
             }
           }
         }
 
         for(ChatListener listener : chatListeners) {
           Matcher matcher = listener.getFilter().matcher(line);
 
           if(matcher.matches()) {
             listener.match(matcher);
           }
         }
       }
 
       endTimer("General Pulse Processing");
 
 
       /*
        * Process data burst & modules
        */
 
       if(unprocessedDataBurstCount > 0) {
         startTimer("processDataBursts()");
         boolean fullUpdate = processDataBursts();
         endTimer("processDataBursts()");
 
         if(fullUpdate) {
           if(iniFile.exists() && iniLastModified != iniFile.lastModified()) {
             reloadIni();
             reloadModules();
           }
         }
 
 //        TreeSet<Command> commands = new TreeSet<Command>(new Comparator<Command>() {
 //          public int compare(Command o1, Command o2) {
 //            int result = o1.getPriority() - o2.getPriority();
 //
 //            if(result == 0) {
 //              result = o1.hashCode() - o2.hashCode();
 //            }
 //
 //            return result;
 //          }
 //        });
 
         if(getMe() != null) {
           List<Command> commands = new ArrayList<>();
 
           getMe().unlockAllSpellSlots();
 
           /*
            * Run modules
            */
           for(Module module : modules) {
             activeModule = module;
 
             if(module.isLowLatency() || fullUpdate) {
               startTimer("Module " + module.getClass().getSimpleName());
               List<Command> newCommands = module.pulse();
               endTimer("Module " + module.getClass().getSimpleName());
 
               if(newCommands != null) {
                 commands.addAll(newCommands);
               }
             }
           }
 
           startTimer("Commands");
 
           /*
            * Sorts the commands on priority, but respects the original order of the Commands that
            * have the same priorities.
            */
 
           Collections.sort(commands, new Comparator<Command>() {
             @Override
             public int compare(Command o1, Command o2) {
               double d = o1.getPriority() - o2.getPriority();
 
               if(d < 0) {
                 return -1;
               }
               else if(d > 0) {
                 return 1;
               }
 
               return 0;
             }
           });
 
           // This locks out all commands, since commands can be anything this might be a bit too much.
           if(castLockEndMillis < System.currentTimeMillis()) {
   //          for(Command command : commands) {
   //            log(command.getPriority() + " : " + command);
   //          }
 
             for(Command command : commands) {
               command.execute(this);
               break;
             }
           }
 
           endTimer("Commands");
         }
       }
     }
     catch(ZoningException e) {
       try {
         if(!zoning) {
           System.out.println("ZONING DETECTED");
         }
         zoning = true;
         Thread.sleep(2000);
       }
       catch(InterruptedException e2) {
         throw new RuntimeException(e2);
       }
     }
   }
 
   private static final Pattern BOT_PATTERN = Pattern.compile("#B ([A-Za-z]+) ([-0-9]+) ([-0-9]+) ([-0-9]+) ([-0-9]+) ([-0-9]+) ([-0-9]+) B\\[([-0-9 ]*)\\] D\\[([-0-9 ]*)\\] SB\\[([-0-9 ]*)\\]");
   private static final Pattern ME_PATTERN = Pattern.compile("#M ([-0-9]+) ([-0-9]+) ([A-Za-z]+) .+");
 
   private boolean processDataBursts() {
     boolean fullUpdate = false;
 
     while(unprocessedDataBurstCount > 0) {
       List<String> dataBurst = new ArrayList<>();
       boolean containsAllSpawns = false;
       boolean zoning = false;
 
       startTimer("PDB1 collect burst");
 
       synchronized(chatLines) {
         Iterator<String> chatLineIterator = chatLines.iterator();
 
         while(chatLineIterator.hasNext()) {
           String line = chatLineIterator.next();
 
           if(line.startsWith("#")) {
             if(line.startsWith("#F")) {
               fullUpdate = true;
               containsAllSpawns = true;
             }
             if(line.startsWith("#Z")) {
               // System.err.println("-------ZONING-------");
               zoning = true;
             }
             dataBurst.add(line);
             chatLineIterator.remove();
             if(line.equals("##")) {
               unprocessedDataBurstCount--;
               break;
             }
           }
         }
       }
 
       endTimer("PDB1 collect burst");
 
       if(dataBurst.size() < 1) {
         throw new RuntimeException("Assertion failed");
       }
 
       if(!(dataBurst.get(0).startsWith("#F") || dataBurst.get(0).startsWith("#M"))) {
         // Discard this burst as it contains no valid start
         //logErr("Discarding a databurst, starts with: " + dataBurst.get(0));
         continue;
       }
 
       if(!zoning) {
         startTimer("PDB2 first pass");
 
         //botNames.clear();
         groupMemberNames.clear();
 
         for(String line : dataBurst) {
           if(line.startsWith("#B ")) {
             Matcher matcher = BOT_PATTERN.matcher(line);
 
             if(matcher.matches()) {
               //botNames.add(matcher.group(1));
             }
           }
           else if(line.startsWith("#G ")) {
             groupMemberNames.add(line.substring(3).replaceAll("[0-9]+", ""));
           }
           else if(line.startsWith("#A")) {
             String[] results = line.substring(2).split(";");
             if(Integer.parseInt(results[0]) == expressionsVersion) {
               int i = 1;
 
               for(List<ExpressionListener> listeners : expressionListeners.values()) {
                 for(ExpressionListener listener : listeners) {
                   listener.stateUpdated(results[i]);
                 }
                 i++;
               }
             }
           }
         }
 
       //    System.out.println("Processing data burst");
         Set<Integer> foundSpawnIDs = new HashSet<>();
         int spawnCountCheck = 0;
         String previousLine = "(empty)";
         String lastSpawnLine = "(empty)";
 
         endTimer("PDB2 first pass");
         startTimer("PDB3 second pass");
 
         for(String line : dataBurst) {
           if(line.startsWith("#M ")) {
             startTimer("PDB #M");
 
             Matcher matcher = ME_PATTERN.matcher(line);
 
             if(matcher.matches()) {
               int zoneId = Integer.parseInt(matcher.group(1));
               int charId = Integer.parseInt(matcher.group(2));
               String charName = matcher.group(3);
 
               if(charId != this.charId) {
                 System.out.println("SESSION: Main Character ID changed: " + this.charId + " -> " + charId);
                 this.charId = charId;
               }
               if(zoneId != this.zoneId || !charName.equals(this.charName)) {
                 boolean characterChanged = false;
 
                 if(!charName.equals(this.charName)) {
                   System.out.println("SESSION: Changed Characters: " + this.charName + " -> " + charName);
                   characterChanged = true;
                 }
                 if(zoneId != this.zoneId) {
                   System.out.println("SESSION: Zoned: " + this.zoneId + " -> " + zoneId);
                 }
                 this.charName = charName;
                 this.zoneId = zoneId;
                 onFinishedZoning(characterChanged);
               }
 
               ((Me)getSpawnInternal(charId)).updateMe(line);
 
               foundSpawnIDs.add(charId);
       //        System.out.println("CHAT: " + line);
             }
 
             endTimer("PDB #M");
           }
           else if(line.startsWith("#S-")) {
             startTimer("PDB #S");
 
             int firstSpace = line.indexOf(' ');
             int secondSpace = line.indexOf(' ', firstSpace + 1);
             int spawnID = Integer.parseInt(line.substring(firstSpace + 1, secondSpace));
 
             if(!line.substring(3, firstSpace).equals("" + spawnCountCheck++)) {
               System.err.println("Missing spawn line, #S-" + (spawnCountCheck - 1) + "; got: " + line);
               System.err.println("lastspawnline was: " + lastSpawnLine);
               System.err.println("previous line was: " + previousLine);
               spawnCountCheck++;
             }
 
             foundSpawnIDs.add(spawnID);
             getSpawnInternal(spawnID).updateSpawn(line);
 
             lastSpawnLine = line;
 
             endTimer("PDB #S");
           }
 
           previousLine = line;
         }
 
         endTimer("PDB3 second pass");
 
         startTimer("PDB4 third pass");
 
         groupMemberNames.add(getMe().getName());
 
         for(String line : dataBurst) {
           if(line.startsWith("#B ")) {
             Matcher matcher = BOT_PATTERN.matcher(line);
 
             if(matcher.matches()) {
   //            botNames.add(matcher.group(1));
   //              System.err.println(line);
               Spawn spawn = getSpawn(matcher.group(1));
 
               if(spawn != null) {  // Bot is in the zone
                 int currentHPs = Integer.parseInt(matcher.group(2));
                 int maxHPs = Integer.parseInt(matcher.group(3));
 //                int currentMana = Integer.parseInt(matcher.group(4));
 //                int maxMana = Integer.parseInt(matcher.group(5));
 
                 if(!spawn.isMe()) {
                   spawn.updateHealth(maxHPs == 0 ? 100 : currentHPs * 100 / maxHPs);
                 }
 
                 spawn.updateBuffs(matcher.group(8).trim() + " " + matcher.group(10).trim());
 
                 int targetId = Integer.parseInt(matcher.group(6));
                 int targetPct = Integer.parseInt(matcher.group(7));
 
                 spawn.updateTarget(targetId);
 
                 Spawn target = getSpawn(targetId);
 
                 if(target != null && !target.isBot() && !target.isMe() && !target.isGroupMember()) {
                   target.updateHealth(targetPct);
                 }
               }
             }
           }
         }
 
         if(containsAllSpawns) {
           spawns.keySet().retainAll(foundSpawnIDs);
         }
 
         endTimer("PDB4 third pass");
 
         startTimer("PDB5 post-processing 2");
 
         for(Spawn spawn : spawns.values()) {
           spawn.updateTTL();
         }
 
         endTimer("PDB5 post-processing 2");
 
   //      for(String line : dataBurst) {
   //        if(line.startsWith("#L")) {
   //          Me me = getMe();
   //
   //          if(me != null) {
   //            me.updateMeTypeL(line);
   //          }
   //        }
   //      }
       }
     }
 
     return fullUpdate;
   }
 
   private Spawn getSpawnInternal(int spawnID) {
     Spawn spawn = spawns.get(spawnID);
 
     if(spawn == null) {
       if(spawnID == charId) {
         spawn = new Me(this, spawnID);
       }
       else {
         spawn = new Spawn(this, spawnID);
       }
 
       spawns.put(spawnID, spawn);
     }
 
     return spawn;
   }
 
   @Override
   public String toString() {
     return sessionName;
   }
 
   public Ini2 getIni() {
     return ini;
   }
 
   public Ini2 getGlobalIni() {
     return globalIni;
   }
 
   public Logger getLogger() {
     return logger;
   }
 
   public void echo(String text) {
     doCommand("/echo " + text);
   }
 
   public void log(String text) {
     System.out.printf("%-10s " + text + "%n", "<" + charName + ">");
   }
 
   public void logErr(String text) {
     System.err.printf("%-10s " + text + "%n", "<" + charName + ">");
   }
 
   public void setDebug(boolean debug) {
     this.debug = debug;
   }
 
   /**
    * Evaluates a MacroQuest script expression to <code>true</code> or <code>false</code>.
    */
   public boolean evaluate(String expr) {
     return translate("${If[" + expr + ",TRUE,FALSE]}").equals("TRUE");
   }
 
   /**
    * Translates a MacroQuest script expression and replaces all ${} constructs with their actual values.  It
    * does not attempt to evaluate the resulting expression.
    */
   public String translate(String s) {
     String result = thread.waitForResponse(s);
     if(debug) {
       System.out.println("                " + result + " << " + s);
     }
 //    System.out.println("                " + result + " << " + s);
     if(result == null) {
       // We're zoning it seems
       System.out.println(this + " : ZONING DETECTED");
       throw new ZoningException();
     }
     return result;
   }
 
   public void doCommand(String s) {
     thread.doCommand(s);
     //log("CMD: " + s);
     logger.info("CMD: " + s);
     if(debug) {
     }
   }
 
   public void delayUntilUpdate() {
     processDataBursts();
 
     while(unprocessedDataBurstCount == 0) {
       try {
         Thread.sleep(10);
       }
       catch(InterruptedException e) {
         throw new RuntimeException(e);
       }
     }
 
     processDataBursts();
   }
 
   public void delay(int timeOut) {
     delay(timeOut, (Condition)null);
   }
 
   /**
    * @return <code>true</code> if we exited early
    */
   public boolean delay(int timeOut, Condition condition) {
     long millis = System.currentTimeMillis();
 
     while(System.currentTimeMillis() - millis < timeOut) {
       if(condition != null && condition.isValid()) {
         return true;
       }
 
       try {
         processDataBursts();
         Thread.sleep(condition != null ? 100 : 10);
       }
       catch(InterruptedException e) {
         throw new RuntimeException(e);
       }
     }
 
     return false;
   }
 
   /**
    * @return <code>true</code> if we exited early
    */
   public boolean delay(int timeOut, final String expr) {
     return delay(timeOut, new Condition() {
       @Override
       public boolean isValid() {
         boolean result = evaluate(expr);
         // System.out.println("${If[" + expr + ",TRUE,FALSE]} -> " + result);
         return result;
       }
     });
   }
 
   private final List<ChatListener> chatListeners = new ArrayList<>();
   private final LinkedList<String> chatLines = new LinkedList<>();
 
   public void addChatListener(ChatListener listener) {
     chatListeners.add(listener);
   }
 
   private int unprocessedDataBurstCount;
 
   public void addChatLine(String line) {
     synchronized(chatLines) {
       chatLines.add(line);
       if(line.equals("##")) {
         unprocessedDataBurstCount++;
       }
       if(chatLines.size() >= 1000 && chatLines.size() % 1000 == 0) {
         System.err.println("WARNING: Chat line buffer contains " + chatLines.size() + " lines (unprocessed bursts = " + unprocessedDataBurstCount +", + " + getMe() + ")!");
         if(chatLines.size() >= 100000) {
           System.err.println("WARNING: Discarding all lines to prevent running out of memory");
           chatLines.clear();
         }
       }
     }
   }
 
   private Map<Class<?>, ResourceProvider<?>> resourceProviders = new HashMap<>();
 
   public <T> boolean testResource(Class<T> cls) {
     T t = obtainResource(cls);
 
     if(t == null) {
       return false;
     }
     else {
       releaseResource(t);
       return true;
     }
   }
 
   @SuppressWarnings("unchecked")
   public <T> T obtainResource(Class<T> cls) {
     ResourceProvider<T> resourceProvider = (ResourceProvider<T>)resourceProviders.get(cls);
 
     return resourceProvider.obtainResource();
   }
 
   @SuppressWarnings("unchecked")
   public <T> void releaseResource(T resource) {
     ResourceProvider<T> resourceProvider = (ResourceProvider<T>)resourceProviders.get(resource.getClass());
 
     resourceProvider.releaseResource(resource);
   }
 
   public void registerResourceProvider(Class<?> cls, ResourceProvider<?> provider) {
     resourceProviders.put(cls, provider);
   }
 
   private final Event<EverquestSession> onZoned = new Event<>(this);
 
   public Event<EverquestSession>.Interface onZoned() {
     return onZoned.getInterface();
   }
 
   private void reloadIni() {
     log("SESSION: Reloading " + iniFile);
 
     try {
       if(iniFile.exists()) {
         ini = new Ini2(iniFile);
         iniLastModified = iniFile.lastModified();
       }
       else {
         ini = new Ini2();
       }
 
       profileSets.clear();
 
       if(ini.getSection("Modules") != null) {
         for(String profileSet : ini.getSection("Modules").getDefault("Profiles", "").split("\\|")) {
           String[] profiles = profileSet.split(",");
 
           profileSets.add(new ProfileSet(profiles));
         }
 
         for(String ignoreName : ini.getSection("General").getDefault("Ignore", "").split(";")) {
           ignoreList.add(ignoreName);
         }
       }
     }
     catch(IOException e) {
       throw new RuntimeException(e);
     }
   }
 
   private synchronized void unloadModules() {
     modules.clear();
     resourceProviders.clear();
     userCommands.clear();
     chatListeners.clear();
     expressionListeners.clear();
     effects.clear();
     onZoned.clear();
 
     addUserCommand("modules", Pattern.compile("(load|unload)"), "(load|unload)", new UserCommand() {
       @Override
       public void onCommand(Matcher matcher) {
         if(matcher.group(1).equals("load")) {
           reloadModules();
           echo("==> Reloaded modules.");
         }
         else {
           unloadModules();
           echo("==> All modules unloaded.");
         }
       }
     });
 
     addUserCommand("altini", Pattern.compile("(.*)"), "name", new UserCommand() {
       @Override
       public void onCommand(Matcher matcher) {
         if(matcher.group(1).equals("")) {
           alternateName = null;
           echo("==> Using default ini: " + charName);
           onFinishedZoning(true);
         }
         else {
           alternateName = matcher.group(1);
           echo("==> Using alternate ini: " + matcher.group(1));
           onFinishedZoning(true);
         }
       }
     });
 
     addUserCommand("profile", Pattern.compile("(.+)"), "(help|<profile name>)", new UserCommand() {
       @Override
       public void onCommand(Matcher matcher) {
         String profileName = matcher.group(1).toLowerCase();
         boolean noSuchProfile = true;
 
         if(!profileName.equals("help")) {
           for(ProfileSet set : profileSets) {
             if(set.contains(profileName)) {
               set.toggle(profileName);
               noSuchProfile = false;
               break;
             }
           }
         }
 
         String availableProfiles = "";
 
         for(ProfileSet set : profileSets) {
           if(availableProfiles.length() > 0) {
             availableProfiles += " -- ";
           }
           availableProfiles += set.toString();
         }
 
         if(profileName.equals("help")) {
           echo("==> Status: " + availableProfiles);
         }
         else if(noSuchProfile) {
           echo("==> No such profile exists: " + profileName);
           echo("==> Status: " + availableProfiles);
         }
         else {
           echo("==> Toggled profile: " + profileName);
           echo("==> Status: " + availableProfiles);
         }
       }
     });
   }
 
   private synchronized void reloadModules() {
     log("SESSION: Reloading Modules");
 
     unloadModules();
 
     moveLock = new Lock<>();
 
     Section section = globalIni.getSection("Modules");
     Injector injector = Guice.createInjector(new EverquestModule(this));
 
     if(section != null) {
       for(String className : globalIni.getSection("Modules").getAll("Class")) {
         try {
           Class<?> cls = Class.forName(className);
 
           addModule((Module)injector.getInstance(cls));
 
 //          Constructor<?> constructor = cls.getConstructor(EverquestSession.class);
 //
 //          addModule((Module)constructor.newInstance(this));
           System.out.println("Loaded " + cls.getSimpleName());
           log("Loaded " + cls.getSimpleName());
         }
         catch(ClassNotFoundException e) {
           System.err.println("Class not found: " + className);
         }
 //        catch(NoSuchMethodException e) {
 //          System.err.println("Incorrect or missing constructor: " + className);
 //        }
         catch(Exception e) {
           System.err.println("Couldn't construct: " + className);
           e.printStackTrace();
         }
       }
 
       addModule(new MoveModule(this));
     }
     else {
       System.err.println("No Modules section in global.ini");
     }
   }
 
   private void createLogger() {
     logger = Logger.getLogger(charName);
 
     for(Handler handler : logger.getHandlers()) {
       logger.removeHandler(handler);
     }
 
     try {
       FileHandler fileHandler = new FileHandler("logs/" + charName + ".%g.txt", 1048576, 10, true);
 
       final DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss.SSS");
 
       fileHandler.setFormatter(new Formatter() {
 
         @Override
         public String format(LogRecord record) {
           StringBuilder sb = new StringBuilder();
 
           sb.append("[");
           sb.append(record.getLevel().getName());
           sb.append(" ");
           sb.append(dateFormat.format(new Date(record.getMillis())));
           sb.append("] ");
           sb.append(record.getMessage());
 
           while(sb.length() < 80 || sb.length() % 10 != 0) {
             sb.append(" ");
           }
 
           sb.append("-- " + record.getSourceClassName() + "." + record.getSourceMethodName() + "()\n");
           return sb.toString();
         }
       });
 
       logger.setUseParentHandlers(false);
       logger.addHandler(fileHandler);
     }
     catch(IOException e) {
       throw new RuntimeException(e);
     }
   }
 
   public void onFinishedZoning(boolean characterChanged) {
     spawns.clear();
 
     if(characterChanged) {
       iniFile = new File(globalIni.getValue("Global", "Path") + "/JB_" + (alternateName == null ? charName : alternateName) + ".ini");
 
       if(!iniFile.exists() && alternateName == null) {
         iniFile = new File(globalIni.getValue("Global", "Path") + "/JB_" + translate("${Me.Class}").toLowerCase() + ".ini");
       }
 
       createLogger();
       reloadIni();
       reloadModules();
       echo("Using ini: " + iniFile.getName());
     }
     else {
       onZoned.trigger();
     }
 
     System.out.println("SESSION: Zoning Finished: " + charName + " (" + charId + ") is in zone " + zoneId);
 
     logger.info("ZONE: Zoned to " + zoneId);
   }
 
   public Me getMe() {
     return (Me)getSpawn(charId);
   }
 
   public Set<Spawn> getSpawns() {
     Set<Spawn> set = new HashSet<>();
 
     set.addAll(spawns.values());
 
     return set;
   }
 
   public Spawn getSpawn(int id) {
     return spawns.get(id);
   }
 
   public Spawn getSpawn(String name) {
     for(Spawn spawn : spawns.values()) {
       if(spawn.getName() != null) {   // TODO happend somehow that we got a null spawn in here...
         if(spawn.getName().equals(name)) {
           return spawn;
         }
       }
     }
 
     return null;
   }
 
   public Spell getSpellFromBook(String name) {
     String result = translate("${Me.Book[" + name + "]} ${Me.Book[" + name + " Rk. II]} ${Me.Book[" + name + " Rk.II]} ${Me.Book[" + name + " Rk. III]} ${Me.Book[" + name + " Rk.III]}");
 
     for(String s : result.split(" ")) {
       if(!s.equals("NULL")) {
         return getSpell(Integer.parseInt(translate("${Me.Book[" + s + "].ID}")));
       }
     }
 
     return null;
   }
 
   public Spell getCombatAbilityFromList(String name) {
     String result = translate("${Me.CombatAbility[${Me.CombatAbility[" + name + "]}].ID} ${Me.CombatAbility[${Me.CombatAbility[" + name + " Rk. II]}].ID} ${Me.CombatAbility[${Me.CombatAbility[" + name + " Rk.II]}].ID} ${Me.CombatAbility[${Me.CombatAbility[" + name + " Rk. III]}].ID} ${Me.CombatAbility[${Me.CombatAbility[" + name + " Rk.III]}].ID}");
 
     for(String s : result.split(" ")) {
       if(!s.equals("NULL")) {
         return getSpell(Integer.parseInt(translate("${Spell[" + s + "].ID}")));
       }
     }
 
     return null;
   }
 
   public Spell getSpell(int id) {
     Spell spell = spells.get(id);
 
     if(spell == null) {
       spell = new Spell(this, id);
       spells.put(id, spell);
     }
 
     return spell;
   }
 
   public SpellData getRawSpellData(int id) {
     return rawSpellData.get(id);
   }
 
   public Set<String> getIgnoreList() {
     return ignoreList;
   }
 
   public Set<String> getActiveProfiles() {
     Set<String> activeProfiles = new HashSet<>();
 
     for(ProfileSet set : profileSets) {
       String activeProfile = set.getActiveProfile();
 
       if(activeProfile != null) {
         activeProfiles.add(activeProfile);
       }
     }
 
     return activeProfiles;
   }
 
   public Set<String> getAuras() {
     Set<String> auras = new HashSet<>();
 
     for(Spawn spawn : spawns.values()) {
       if(spawn.getType() == SpawnType.AURA) {
         auras.add(spawn.getName());
       }
     }
 
     return auras;
   }
 
   public Module getModule(String name) {
     for(Module module : modules) {
       String clsName = module.getClass().getSimpleName();
 
       clsName = clsName.substring(clsName.lastIndexOf('.') + 1);
 
       if(clsName.equals(name)) {
         return module;
       }
     }
 
     return null;
   }
 
   private final Map<String, Effect> effects = new HashMap<>();
 
   /**
    * Gets an Effect based on a description string.<br>
    *
    * This only creates effects available to the character at the time, ie. spells must be scribed and clickeys
    * must be on the character.
    */
   public Effect getEffect(String effectDescription, int agro) {
     boolean hasEffect = effects.containsKey(effectDescription);
 
     if(!hasEffect) {
       Effect effect = null;
       int colon = effectDescription.indexOf(':');
       String type = effectDescription.substring(0, colon);
       String name = effectDescription.substring(colon + 1);
 
       if(type.equals("Spell")) {
         Spell spell = getSpellFromBook(name);
         if(spell != null) {
           effect = new SpellEffect(this, spell, agro);
         }
       }
       else if(type.equals("Song")) {
         Spell spell = getSpellFromBook(name);
         if(spell != null) {
           effect = new SongEffect(this, spell, agro);
         }
       }
       else if(type.equals("Item")) {
         String result = translate("${FindItem[=" + name + "].Spell.ID}");
         if(!result.equals("NULL")) {
           effect = new ItemEffect(this, name, getSpell(Integer.parseInt(result)), agro);
         }
       }
       else if(type.equals("Alt")) {
         String result = translate("${Me.AltAbility[" + name + "].Spell.ID}");
         if(!result.equals("NULL")) {
           effect = new AlternateAbilityEffect(this, name, getSpell(Integer.parseInt(result)), agro);
         }
       }
       else if(type.equals("Discipline")) {
         Spell spell = getCombatAbilityFromList(name);
         if(spell != null) {
           effect = new DisciplineEffect(this, name, spell, agro);
         }
       }
 
       effects.put(effectDescription, effect);
     }
 
     return effects.get(effectDescription);
   }
 
   public boolean hasClass(String className) {
     for(Spawn spawn : getGroupMembers()) {
       if(spawn.getClassShortName().equalsIgnoreCase(className)) {
         return true;
       }
     }
 
     for(Spawn spawn : getBots()) {
       if(spawn.getClassShortName().equalsIgnoreCase(className)) {
         return true;
       }
     }
 
     return false;
   }
 
   public boolean isProfileActive(String profiles) {
     if(profiles == null || profiles.trim().length() == 0 || profileSets.isEmpty()) {
       return true;
     }
 
     String expr = profiles.replaceAll(",", " || ").replaceAll("([a-zA-Z]+)", "isActive(\"$1\")");
 
     return ExpressionEvaluator.evaluate(Arrays.asList(expr), new ProfileExpressionRoot(profileSets), profiles);
   }
 
   private final LinkedHashMap<String, List<ExpressionListener>> expressionListeners = new LinkedHashMap<>();
 
   private int expressionsVersion;
 
   public synchronized void registerExpression(String expression, ExpressionListener listener) {
     List<ExpressionListener> listeners = expressionListeners.get(expression);
 
     if(listeners == null) {
       listeners = new ArrayList<>();
       expressionListeners.put(expression, listeners);
     }
 
     listeners.add(listener);
 
     String stateString = "";
 
     for(String s : expressionListeners.keySet()) {
       if(stateString.length() > 0) {
         stateString += ";";
       }
 
       stateString += s;
     }
 
     expressionsVersion++;
     doCommand("##A" + expressionsVersion + ";" + stateString);
   }
 
   private class MyThread extends Thread {
     private final Map<Integer, String> responses = new HashMap<>();
     private final Map<Integer, String> waiters = new HashMap<>();
     private final BufferedReader reader;
     private final PrintWriter writer;
 
     private int responseNumber = 1;
 
     public MyThread(BufferedReader reader, PrintWriter writer) {
       this.reader = reader;
       this.writer = writer;
 
       setPriority(Thread.NORM_PRIORITY + 2);
     }
 
     public void doCommand(String s) {
       synchronized(waiters) {
         writer.println(s);
         writer.flush();
       }
     }
 
     private long lastProcessedMillis;
     private int lastResponseNumber;
 
     public String waitForResponse(String s) {
       synchronized(waiters) {
         int n = responseNumber++;
 
         lastProcessedMillis = System.currentTimeMillis();
         lastResponseNumber = responseNumber;
 
         writer.println("$" + n + "-" + s);
         writer.flush();
         waiters.put(n, s);
 
         while(!responses.containsKey(n)) {
           try {
             waiters.wait();
           }
           catch(InterruptedException e) {
             throw new RuntimeException(e);
           }
         }
 
         return responses.remove(n);
       }
     }
 
 
     private final List<String> debugBuffer = new LinkedList<>();
     private final DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss.SSS");
 
     @SuppressWarnings("unused")
     private void debug(String line) {
       synchronized(debugBuffer) {
         debugBuffer.add("[" + dateFormat.format(new Date()) + "] " + line);
         if(debugBuffer.size() > 1500) {
           debugBuffer.remove(0);
         }
 
         synchronized(waiters) {
           if(waiters.size() > 0 && lastResponseNumber == responseNumber && System.currentTimeMillis() - lastProcessedMillis > 1500) {
             getLogger().warning("RESPONSE_DEBUG: No response: " + EverquestSession.this.getMe());
             getLogger().warning("RESPONSE_DEBUG: waiters.size() = " + waiters.size());
             getLogger().warning("RESPONSE_DEBUG: waiters = " + waiters);
             getLogger().warning("RESPONSE_DEBUG: responseNumber = " + responseNumber);
 
             System.err.println("DUMPING RESPONSE : " + EverquestSession.this.getMe());
             System.err.println("waiters.size() = " + waiters.size());
             System.err.println("waiters = " + waiters);
             System.err.println("responseNumber = " + responseNumber);
             for(String s : debugBuffer) {
               getLogger().warning("RESPONSE_DEBUG: " + s);
               System.err.println(s);
             }
             waiters.remove(responseNumber - 1);
             responses.put(responseNumber - 1, "NULL");
             waiters.notifyAll();
           }
         }
       }
     }
 
     /**
      * Resends data if no response was received in due time.  This is to work around what I think is
      * a bug in the Loopback TCP/IP stack of Windows, which seems to be triggered when data is being
      * simultaneously send and received on the Server side.
      */
     private void resendWorkaround() {
       synchronized(waiters) {
         if(waiters.size() > 0 && lastResponseNumber == responseNumber && System.currentTimeMillis() - lastProcessedMillis > 450) {
           // Resend last request(s)
           for(Integer i : waiters.keySet()) {
             System.err.println("Resending: $" + i + "-" + waiters.get(i));
             writer.println("$" + i + "-" + waiters.get(i));
             writer.flush();
             lastProcessedMillis = System.currentTimeMillis();
           }
         }
       }
     }
 
     @Override
     public void run() {
       boolean bufSkip = false;
 
       try {
         for(;;) {
           String line = reader.readLine();
 
           startTimer("TCP processing");
 //          debug(line);  // Stores and Activates the Debug Buffer code
           resendWorkaround();
 
           if(line.startsWith("@BUF=")) {
             bufSkip = true;
           }
 
           if(line.startsWith("@ENDBUF")) {
             bufSkip = false;
           }
 
           if(!bufSkip) {
             if(line.startsWith("$")) {
               synchronized(waiters) {
   //              System.out.println("Received response: " + line);
                 int dash = line.indexOf('-');
 
                 if(dash > 0) {
                   int responseNo = Integer.parseInt(line.substring(1, dash));
                   if(waiters.remove(responseNo) == null) {
                     getLogger().warning("RESPONSE_DEBUG2: Unexpected response: " + line);
                     System.err.println("Unexpected response: " + line);
                   }
                   else {
                     responses.put(responseNo, line.substring(dash + 1));
                     waiters.notifyAll();
                   }
                 }
                 else if(line.equals("$ZONING")) {
                   // Zoning detected.  Terminate all responses.
                   for(int i : waiters.keySet()) {
                     responses.put(i, null);
                   }
                   waiters.clear();
                   waiters.notifyAll();
                 }
 
 
   //              for(String s : waiters.keySet()) {
   //                if(line.startsWith(s)) {
   //                  int n = waiters.get(s);
   //                  responses.put(n, line.substring(line.indexOf('-') + 1));
   //                  waiters.remove(s);
   //                  waiters.notifyAll();
   //                  break;
   //                }
   //              }
               }
             }
             else {
               addChatLine(line);
             }
           }
 
           endTimer("TCP processing");
         }
       }
       catch(IOException e) {
         logger.severe("Exception while communicating with MQ.  Commands in queue:");
         System.err.println("Exception while communicating with MQ.  Commands in queue:");
 
         for(int key : waiters.keySet()) {
           logger.severe(" " + key + ": " + waiters.get(key));
           System.err.println(" " + key + ": " + waiters.get(key));
         }
 
         throw new RuntimeException(e);
       }
     }
   }
 
   private static class UserCommandWrapper {
     private final UserCommand userCommand;
     private final Pattern pattern;
     private final String helpText;
 
     public UserCommandWrapper(UserCommand userCommand, Pattern pattern, String helpText) {
       this.userCommand = userCommand;
       this.pattern = pattern;
       this.helpText = helpText;
     }
   }
 }
