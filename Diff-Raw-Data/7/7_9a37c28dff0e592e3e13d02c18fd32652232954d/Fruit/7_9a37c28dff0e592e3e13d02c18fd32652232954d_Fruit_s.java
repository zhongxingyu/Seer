 package fruit.sim;
 
 // general utilities
 import java.io.*;
 import java.util.List;
 import java.util.*;
 import java.net.*;
 import javax.tools.*;
 
 
 public class Fruit
 {
     private static String ROOT_DIR = "fruit";
 
     // recompile .class file?
     private static boolean recompile = true;
 
     // enable gui
     private static boolean gui = true;
 
     // Step by step trace
     private static boolean trace = true;
 
     // default parameters
     private static final String DEFAULT_PLAYERLIST = "players.list";
     private static final String DEFAULT_DISTRIBUTION = "uniform.txt";
     private static int DEFAULT_BOWL_SIZE = 10;
 
 
 	// list files below a certain directory
 	// can filter those having a specific extension constraint
     //
 	private static List <File> directoryFiles(String path, String extension) {
 		List <File> allFiles = new ArrayList <File> ();
 		allFiles.add(new File(path));
 		int index = 0;
 		while (index != allFiles.size()) {
 			File currentFile = allFiles.get(index);
 			if (currentFile.isDirectory()) {
 				allFiles.remove(index);
 				for (File newFile : currentFile.listFiles())
 					allFiles.add(newFile);
 			} else if (!currentFile.getPath().endsWith(extension))
 				allFiles.remove(index);
 			else index++;
 		}
 		return allFiles;
 	}
 
   	// compile and load players dynamically
     //
     static Player[] loadPlayers(String txtPath) {
 		// list of players
         List <Player> playersList = new LinkedList <Player> ();
 
         try {
             // get file of players
             BufferedReader in = new BufferedReader(new FileReader(new File(txtPath)));
             // get tools
             URL url = Fruit.class.getProtectionDomain().getCodeSource().getLocation();
             // Create a new class loader to load the players between each game
             // so that no static fields will be carried to the next game
             ClassLoader loader = new ClassReloader(url, Fruit.class.getClassLoader());
 
             if (loader == null) throw new Exception("Cannot load class loader");
             JavaCompiler compiler = null;
             StandardJavaFileManager fileManager = null;
             // get separator
             String sep = File.separator;
             // load players
             String group;
             while ((group = in.readLine()) != null) {
                 System.err.println("Group: " + group);
                 // search for compiled files
                 File classFile = new File(ROOT_DIR + sep + group + sep + "Player.class");
                 System.err.println(classFile.getAbsolutePath());
                 if (!classFile.exists() || recompile) {
                     // delete all class files
                     List <File> classFiles = directoryFiles(ROOT_DIR + sep + group, ".class");
                     System.err.print("Deleting " + classFiles.size() + " class files...   ");
                     for (File file : classFiles)
                         file.delete();
                     System.err.println("OK");
                     if (compiler == null) compiler = ToolProvider.getSystemJavaCompiler();
                     if (compiler == null) throw new Exception("Cannot load compiler");
                     if (fileManager == null) fileManager = compiler.getStandardFileManager(null, null, null);
                     if (fileManager == null) throw new Exception("Cannot load file manager");
                     // compile all files
                     List <File> javaFiles = directoryFiles(ROOT_DIR + sep + group, ".java");
                     System.err.print("Compiling " + javaFiles.size() + " source files...   ");
                     Iterable<? extends JavaFileObject> units = fileManager.getJavaFileObjectsFromFiles(javaFiles);
                     boolean ok = compiler.getTask(null, fileManager, null, null, null, units).call();
                     if (!ok) throw new Exception("Compile error");
                     System.err.println("OK");
                 }
                 // load class
                 System.err.print("Loading player class...   ");
                 String className = ROOT_DIR + "." + group + ".Player";
                 Class playerClass = loader.loadClass(className);
                 System.err.println("OK");
                 // set name of player and append on list
 
                 Player player = (Player) playerClass.newInstance();
                 // set player id
                 if (Character.isDigit(group.charAt(1))) {
                     player.id = group.charAt(1) - '0';
                     
                     //PX**
                     if(group.equals("g4"))  {
                     	System.out.println("g4's idx: "+player.id);
                     	g4handle = player;
                     }
                     //**PX
                 }
                 else
                     player.id = -1;
 
                 if (player == null)
                     throw new Exception("Load error");
                 playersList.add(player);
             }
             in.close();
         } catch (Exception e) {
             e.printStackTrace(System.err);
             return null;
         }
 
 		return playersList.toArray(new Player[0]);
 	}
 
     private void roundToHtml(StringBuffer buf, int round) {
 		// array of results below buttons
         buf.append("<div>");
 
         buf.append("<div style=\"width: 200px; height: 40px; text-align: center;font-size: 25px; font-weight: bold; font-family: 'Comic Sans MS', cursive, sans-serif\">" + "Round #" + round + "</div>\n");
         
 		// empty up left square
 		buf.append("    <div style=\"width: 34px; height: 40px; float:left;\"></div>\n");
 
 		// color squares
         for (int c = 0; c < FRUIT_NAMES.length; c++) {
             String color = FRUIT_COLORS[c];
             String cname = Character.toString((char)(65+c));
 			buf.append("<div style=\"width: 34px; height: 40px; font-size:20px; font-weight:bold;font-family:'Comic Sans MS', cursive, sans-serif;text-align:center;float:left; border: 1px solid black; background-color: " + color + "\">" + cname + "</div>\n");
         }
 		// empty up right square
 		buf.append("    <div style=\"width: 34px; height: 40px; float:left;\"></div>\n");
 		buf.append("    <div style=\"clear:both;\"></div>\n");
 		// result lines
 		for (int p = 0 ; p != players.length ; ++p) {
 			// player name
 			buf.append("    <div style=\"width: 34px; height: 40px; float:left; border: 1px solid black; text-align: center;");
             if (round == this.round && p == currentPlayer)
                 buf.append("background-color:green;"); // indicate current player is moving
 
             buf.append("\n");
 			buf.append("                font-size: 20px; font-weight: bold; font-family: 'Comic Sans MS', cursive, sans-serif\">" + (p + 1) + "</div>\n");
 			int total = 0;
 
 			buf.append("    <div style=\"width: 432px; height: 40px; float: left; border: 1px solid black;\">\n");
 			for (int r = 0 ; r != FRUIT_NAMES.length ; ++r) {
                 String s;
                 if (bowlOfPlayer[round][p] == null)
                     s = "-";
                 else {
                     s = Integer.toString(bowlOfPlayer[round][p][r]);
                     total += bowlOfPlayer[round][p][r] * preference[p][r];
                 }
 				buf.append("     <div style=\"width: 36px; height: 40px; float:left; text-align: center; font-size: 20px;\n");
 				buf.append("                 font-weight: bold; font-family: 'Comic Sans MS', cursive, sans-serif\">" + s + "</div>\n");
 
 			}
 			buf.append("    </div>\n");
 			// score
             buf.append("    <div style=\"width: 36px; height: 40px; float:left; border: 1px solid black; text-align: center;\n");
             buf.append("                font-size: 20px; font-weight: bold; font-family: 'Comic Sans MS', cursive, sans-serif\">" + total + "</div>\n");
             buf.append("    <div style=\"clear:both;\"></div>\n");
 		}
 		// close result array
 		buf.append("   </div>\n");
     }
 
     private void preferenceToHtml(StringBuffer buf) {
 		// array of results below buttons
         buf.append("<div>");
 
         buf.append("<div style=\"width: 400px; height: 40px; text-align: center;font-size: 25px; font-weight: bold; font-family: 'Comic Sans MS', cursive, sans-serif\">" + "Player preference" + "</div>\n");
 
 
 		// empty up left square
 		buf.append("    <div style=\"width: 34px; height: 40px; float:left;\"></div>\n");
 		// color squares
         for (int c = 0; c < FRUIT_NAMES.length; c++) {
             String color = FRUIT_COLORS[c];
             String cname = Character.toString((char)(65+c));
 			buf.append("<div style=\"width: 34px; height: 40px; font-size:20px; font-weight:bold;font-family:'Comic Sans MS', cursive, sans-serif;text-align:center;float:left; border: 1px solid black; background-color: " + color + "\">" + cname + "</div>\n");
         }
 
         buf.append("    <div style=\"clear:both;\"></div>\n");
 		// result lines
 		for (int p = 0 ; p != players.length ; ++p) {
 			// player name
 			buf.append("    <div style=\"width: 34px; height: 40px; float:left; border: 1px solid black; text-align: center;");
             buf.append("\n");
 			buf.append("                font-size: 20px; font-weight: bold; font-family: 'Comic Sans MS', cursive, sans-serif\">" + players[p].id + "</div>\n");
 
 			buf.append("    <div style=\"width: 432px; height: 40px; float: left; border: 1px solid black;\">\n");
 			for (int r = 0 ; r != FRUIT_NAMES.length ; ++r) {
 				String s = Integer.toString(preference[p][r]);
 				buf.append("     <div style=\"width: 36px; height: 40px; float:left; text-align: center; font-size: 20px;\n");
 				buf.append("                 font-weight: bold; font-family: 'Comic Sans MS', cursive, sans-serif\">" + s + "</div>\n");
 			}
 
 			buf.append("    </div>\n");
             // expectation
             buf.append("    <div style=\"width: 36px; height: 40px; float:left; border: 1px solid black; text-align: center;\n");
             buf.append("                font-size: 20px; font-weight: bold; font-family: 'Comic Sans MS', cursive, sans-serif\">" + expectation[p] + "</div>\n");
             buf.append("    <div style=\"clear:both;\"></div>\n");
 
 			// score
             if (scores != null) {
                 buf.append("    <div style=\"width: 36px; height: 40px; float:left; border: 1px solid black; text-align: center;\n");
                 buf.append("                font-size: 20px; font-weight: bold; font-family: 'Comic Sans MS', cursive, sans-serif\">" + scores[p] + "</div>\n");
                 buf.append("    <div style=\"clear:both;\"></div>\n");
             }
             buf.append("   <div style=\"clear:both;\"></div>\n");
 		}
 		// close result array
 		buf.append("   </div>\n");
     }
 
     private void bowlToHtml(StringBuffer buf) {
         buf.append("<div>");
 
         // Text information about bowl id
         buf.append("<div style=\"width: 200px; height: 40px; text-align: center;font-size: 25px; font-weight: bold; font-family: 'Comic Sans MS', cursive, sans-serif\">" + "Bowl #" + round + "," + bowlId + "</div>\n");
 
 		// empty up left square
 		buf.append("    <div style=\"width: 34px; height: 80px; float:left;\"></div>\n");
 
         for (int c = 0; c < FRUIT_NAMES.length; c++) {
             String color = FRUIT_COLORS[c];
             String cname = Character.toString((char)(65+c));
 			buf.append("<div style=\"width: 34px; height: 40px; font-size:20px; font-weight:bold;font-family:'Comic Sans MS', cursive, sans-serif;text-align:center;float:left; border: 1px solid black; background-color: " + color + "\">" + cname + "</div>\n");
         }
 
         // initial distribution
         buf.append("  <div style=\"width: 432px; height: 40px; float: left; border: 1px solid black;\">\n");
         for (int r = 0 ; r != FRUIT_NAMES.length ; ++r) {
             String s = "-";
             if (currentBowl != null)
                 s = Integer.toString(currentBowl[r]); 
             buf.append("     <div style=\"width: 36px; height: 40px; float:left; text-align: center; font-size: 20px;\n");
             buf.append("                 font-weight: bold; font-family: 'Comic Sans MS', cursive, sans-serif\">" + s + "</div>\n");
         }
         buf.append(" </div>\n");
         
         int score = 0;
         if (currentPlayer != -1) {
         	
             for (int c = 0; c < 12; c++) {
                 score += preference[currentPlayer][c] * currentBowl[c];
             }
             
 
         }
         buf.append("    <div style=\"width: 36px; height: 40px; float:left; border: 1px solid black; text-align: center;\n");
         buf.append("                font-size: 20px; font-weight: bold; font-family: 'Comic Sans MS', cursive, sans-serif\">" + score + "</div>\n");
         buf.append("    <div style=\"clear:both;\"></div>\n");
 
         // Print player action
         if (action != null)
             buf.append("<div style=\"width: 400px; height: 40px; text-align: center;font-size: 25px; font-weight: bold; font-family: 'Comic Sans MS', cursive, sans-serif\">" + action + "</div>\n");
 
         buf.append("</div>");
 
     }
 
 
     private void fruitDistToHtml(StringBuffer buf) {
         //		buf.append("   <div style=\"width: 800px; float: left;\">\n");
         buf.append("<div>");
 
         buf.append("<div style=\"width: 500px; height: 40px; text-align: center;font-size: 25px; font-weight: bold; font-family: 'Comic Sans MS', cursive, sans-serif\">" + "Init and current distribution" + "</div>\n");
 
 		// empty up left square
 		buf.append("    <div style=\"width: 34px; height: 40px; float:left;\"></div>\n");
 
 		// color squares
         for (int c = 0; c < FRUIT_NAMES.length; c++) {
             String color = FRUIT_COLORS[c];
             String cname = Character.toString((char)(65+c));
 			buf.append("<div style=\"width: 34px; height: 40px; font-size:20px; font-weight:bold;font-family:'Comic Sans MS', cursive, sans-serif;text-align:center;float:left; border: 1px solid black; background-color: " + color + "\">" + cname + "</div>\n");
         }
 		buf.append("   <div style=\"clear:both;\"></div>\n");
 
         // Init I
         buf.append("    <div style=\"width: 34px; height: 40px; float:left; border: 1px solid black; text-align: center;");
         buf.append("\n");
         buf.append("                font-size: 25px; font-weight: bold; font-family: 'Comic Sans MS', cursive, sans-serif\">I</div>\n");
         
         // initial distribution
         buf.append("  <div style=\"width: 432px; height: 40px; float: left; border: 1px solid black;\">\n");
         for (int r = 0 ; r != FRUIT_NAMES.length ; ++r) {
             String s = Integer.toString(fruitDist[r]); 
             buf.append("     <div style=\"width: 36px; height: 40px; float:left; text-align: center; font-size: 20px;\n");
             buf.append("                 font-weight: bold; font-family: 'Comic Sans MS', cursive, sans-serif\">" + s + "</div>\n");
         }
         buf.append(" </div>\n");
         buf.append("    <div style=\"clear:both;\"></div>\n");
 
         // Current C
         buf.append("    <div style=\"width: 34px; height: 40px; float:left; border: 1px solid black; text-align: center;");
         buf.append("\n");
         buf.append("                font-size: 25px; font-weight: bold; font-family: 'Comic Sans MS', cursive, sans-serif\">C</div>\n");
 
 
         // current distribution
         buf.append("  <div style=\"width: 432px; height: 40px; float: left; border: 1px solid black;\">\n");
         for (int r = 0 ; r != FRUIT_NAMES.length ; ++r) {
             String s = "-";
             if (currentFruits != null)
                 s = Integer.toString(currentFruits[r]); 
             buf.append("     <div style=\"width: 36px; height: 40px; float:left; text-align: center; font-size: 20px;\n");
             buf.append("                 font-weight: bold; font-family: 'Comic Sans MS', cursive, sans-serif\">" + s + "</div>\n");
         }
         buf.append("    </div>\n");
         buf.append("    <div style=\"clear:both;\"></div>\n");
         
         buf.append("</div>");
     }
 
     private String state() {
 
 		int pixels = 1300;
 		String title = "Fruit";
 		StringBuffer buf = new StringBuffer("");
 		buf.append("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">\n");
 		buf.append("<html xmlns=\"http://www.w3.org/1999/xhtml\" dir=\"ltr\" lang=\"en-US\" xml:lang=\"en\">\n");
 		buf.append("<head>\n");
 		buf.append(" <meta http-equiv=\"Content-Type\" content=\"text/html; charset=iso-8859-7\" />\n");
 		buf.append(" <link rel=\"shortcut icon\" href=\"cell/icon.ico\" />\n");
 		buf.append(" <title>" + title + "</title>\n");
 		buf.append(" <style type=\"text/css\">\n");
 		buf.append("  a:link {text-decoration: none; color: blue;}\n");
 		buf.append("  a:visited {text-decoration: none; color: blue;}\n");
 		buf.append("  a:hover {text-decoration: none; color: red;}\n");
 		buf.append("  a:active {text-decoration: none; color: blue;}\n");
 		buf.append(" </style>\n");
 		buf.append("</head>\n");
 		buf.append("<body>\n");
 		// general part
 		buf.append(" <div style=\"width:" + pixels + "px; margin-left:auto; margin-right: auto;\">\n");
 		// left part
 		buf.append("  <div style=\"width: 600px; float: left;\">\n");
         
         // initial and current distribution
         fruitDistToHtml(buf);
 
 		// space above
 		buf.append("   <div style=\"width: 600px; height: 50px;\"></div>\n");
 		buf.append("   <div style=\"clear:both;\"></div>\n");
 
         // player preference
         preferenceToHtml(buf);
 
 		// space above
 		buf.append("   <div style=\"width: 600px; height: 50px;\"></div>\n");
 		buf.append("   <div style=\"clear:both;\"></div>\n");
 		// space between buttons and array
 		buf.append("   <div style=\"width: 400px; height: 50px; float:left;\"></div>\n");
 		buf.append("   <div style=\"clear:both;\"></div>\n");
 		// button 1
 		buf.append("   <div style=\"width: 150px; height: 70px; float:left; cursor: pointer; text-align: center; font-size: 40px;\n");
 		buf.append("               font-weight: bold; font-family: 'Comic Sans MS', cursive, sans-serif\"><a href=\"play\">Play</a></div>\n");
         //		buf.append("   <div style=\"clear:both;\"></div>\n");
 		// button 2
 		buf.append("   <div style=\"width: 150px; height: 70px; float:left; cursor: pointer; text-align: center; font-size: 40px;\n");
 		buf.append("               font-weight: bold; font-family: 'Comic Sans MS', cursive, sans-serif\"><a href=\"stop\">Stop</a></div>\n");
         //		buf.append("   <div style=\"clear:both;\"></div>\n");
 		// button 3
 		buf.append("   <div style=\"width: 150px; height: 70px; float:left; cursor: pointer; text-align: center; font-size: 40px;\n");
 		buf.append("               font-weight: bold; font-family: 'Comic Sans MS', cursive, sans-serif\"><a href=\"step\">Step</a></div>\n");
         //		buf.append("   <div style=\"clear:both;\"></div>\n");
 		// close left part of page
 		buf.append("  </div>\n");
 
 		// right part
 		buf.append("<div style=\"width: 600px; float: left;\">\n");
 
         // show current bowl
         bowlToHtml(buf);
 
 		// space above
 		buf.append("   <div style=\"width: 600px; height: 50px;\"></div>\n");
 		buf.append("   <div style=\"clear:both;\"></div>\n");
 
         // show current round first
         roundToHtml(buf, round);
 
 		// space above
 		buf.append("   <div style=\"width: 600px; height: 50px;\"></div>\n");
 		buf.append("   <div style=\"clear:both;\"></div>\n");
         
         // show the other round later
         roundToHtml(buf, 1-round);
         
         buf.append("</div");
 
         // close page
 		buf.append(" </div>\n");
 		buf.append("</body>\n");
 		buf.append("</html>\n");
 		return buf.toString();
 
     }
 
 	// shuffle player
 	private static void shufflePlayer(Player[] arr)
 	{
 		for (int i = 0 ; i != arr.length ; ++i) {
 			int j = random.nextInt(arr.length - i) + i;
 			Player t = arr[i];
 			arr[i] = arr[j];
 			arr[j] = t;
 		}
         
         // set index
         for (int i = 0; i != arr.length; ++i)
             arr[i].index = i;
 	}
 
 
 	private static void shuffle(int[] arr)
 	{
 		for (int i = 0 ; i != arr.length ; ++i) {
 			int j = random.nextInt(arr.length - i) + i;
 			int t = arr[i];
 			arr[i] = arr[j];
 			arr[j] = t;
 		}
 	}
 
 
     // generate a random perm for a player
     private static int[] genPref()
     {
         int[] pref = new int[FRUIT_NAMES.length];
         for (int i = 0; i != pref.length; ++i)
             pref[i] = i + 1;
 
         shuffle(pref);
         return pref;
     }
 
     private int pickFruit()
     {
         // generate a prefix sum
         int[] prefixsum = new int[FRUIT_NAMES.length];
         prefixsum[0] = currentFruits[0];
         for (int i = 1; i != FRUIT_NAMES.length; ++i)
             prefixsum[i] = prefixsum[i-1] + currentFruits[i];
 
         int currentFruitCount = prefixsum[FRUIT_NAMES.length-1];
         // roll a dice [0, currentFruitCount)
         int rnd = random.nextInt(currentFruitCount);
         
         for (int i = 0; i != FRUIT_NAMES.length; ++i)
             if (rnd < prefixsum[i])
                 return i;
 
         assert false;
 
         return -1;
     }
 
     private int[] createBowl()
     {
         int[] bowl = new int[FRUIT_NAMES.length];
         int sz = 0;
         while (sz < bowlsize) {
             // pick a fruit according to current fruit distribution
             int fruit = pickFruit(); 
             int c = 1 + random.nextInt(3);
             c = Math.min(c, bowlsize - sz);
             c = Math.min(c, currentFruits[fruit]);
 
             bowl[fruit] += c;
             sz += c;
             currentFruits[fruit] -= c;
         }
         return bowl;
     }
 
 
     private void play(boolean gui) throws Exception{
         BufferedReader buffer = null;
 
         HTTPServer server = null;
 		int refresh = 0;
 		char req = 'X';
         if (gui) {
 			server = new HTTPServer();
 			int port = server.port();
 			System.err.println("Port: " + port);
 			while ((req = server.nextRequest(0)) == 'I');
 			if (req != 'B')
 				throw new Exception("Invalid first request");
         }
         else {
             buffer = new BufferedReader(new InputStreamReader(System.in));
         }
 
 		for (File f : directoryFiles(ROOT_DIR + "/sim/webpages", ".html"))
 			f.delete();
 		FileOutputStream out = new FileOutputStream(ROOT_DIR + "/sim/webpages/index.html");
 		out.write(state().getBytes());
 		out.close();
 
         boolean f = true;
         if (server != null) do {
                 if (!f) refresh = 0;
                 server.replyState(state(), refresh);
                 while ((req = server.nextRequest(0)) == 'I' || req == 'X');
                 if (req == 'S') refresh = 0;
                 else if (req == 'P') refresh = 1;
                 f = false;
             } while (req == 'B');
 
 
         int t = 0;
         for (int r = 0; r <= 1; r++) {
             System.err.println("###### ROUND " + r + " ######");
             resetRound(r);
             for (int i = 0; i < players.length; i++) {
                 bowlId = i;
                 System.err.println();
                 System.err.println("====== BOWL " + r + "." + i + " ======");
 
                 if (!gui && trace) {
                     try {
                         System.err.print("$");
                         buffer.readLine();
                     } catch (Exception e) {}
                 }
 
                 int[] bowl = createBowl();
                 System.err.println(Arrays.toString(bowl));
                 currentBowl = bowl;
                 
                 int [] range = new int[players.length];
                 if (r == 0) {
                     for (int k = 0; k < range.length; k++)
                         range[k] = k;
                 }
                 else {
                     for (int k = 0; k < range.length; k++)
                         range[k] = players.length - k - 1;
                 }
                 
 
                 for (int k = 0; k < range.length; k++) {
                     t++;
 
                     int j = range[k];
                     currentPlayer = j;
 
                     boolean canPick = !hasBowl[j];
                     boolean mustTake = canPick && (choices[j] == 0); 
                     boolean take = players[j].pass(bowl, i, round,
                                                    canPick,
                                                    mustTake);
                     System.err.println("Bowl " + i + " is shown to player " + j);
 
                     // only process the return value from qualified player
                     if (canPick) {
                         if (take || mustTake) {
                             hasBowl[j] = true;
                             bowlOfPlayer[round][j] = bowl.clone();
                             action = "Player " + (j+1) + " TAKES bowl (" + round + "," + i + ")";
                             System.err.println(action);
                         }
                         else {
                             action = "Player " + (j+1) + " PASSES bowl (" + round + "," + i + ")";
                             System.err.println(action);
                             choices[j]--;
                         }
 
                         f = true;
                         if (server != null) do {
                             if (!f) refresh = 0;
                             server.replyState(state(), refresh);
                             while ((req = server.nextRequest(0)) == 'I' || req == 'X');
                             if (req == 'S') refresh = 0;
                             else if (req == 'P') refresh = 1;
                             f = false;
                         } while (req == 'B');
                         // update the html file
                         out = new FileOutputStream(ROOT_DIR + "/sim/webpages/" + t + ".html");
                         out.write(state().getBytes());
                         out.close();
 
                         // break the loop
                         if (take || mustTake)
                             break;
                     }
 
                 }
             }        
         }
 
 
 
         computeScores();
         
         System.err.println(Arrays.toString(scores));
         
         System.out.println("g4idx: " + g4handle.getIndex() + ", g4's score: " + scores[g4handle.getIndex()]);
         
         
         // clean up the server
 		if (server != null) {
 			server.replyState(state(), 0);
 			while ((req = server.nextRequest(2000)) == 'I');
             server.close();
 		}
     }
 
     private int[] computeScores()
     {
         scores = new int[players.length];
         
         for (int i = 0; i < players.length; ++i) {
             int score  = 0;
             int[] pref = preference[i];
 
             for (int r = 0; r <= 1; r++) {
                 int[] bowl = bowlOfPlayer[r][i];
                 for (int k = 0; k != bowl.length; ++k)
                     score += pref[k] * bowl[k];
             }
             
             scores[i] = score;
             
             
         }
         
         return scores;
     }
 
     private void printConfig() {
         System.err.println("###### Config ######");
         System.err.println("N players: " + players.length);
         System.err.println("Bowl size: " + bowlsize);
         System.err.println("Distribution:");
         System.err.println(Arrays.toString(fruitDist));
         System.err.println();
     }
 
 
     private int computeExpectation(int[] pref, int[] dist, int nfruits, int bowlsz) {
         double exp = 0;
         for (int i = 0; i != 12; ++i) {
             double p = 1.0 * dist[i] / nfruits;
             exp += (p * pref[i] * bowlsz);
         }
         return (int)exp;
     }
 
     public Fruit(Player[] players, int bowlsize, int[] dist)
     {
         this.players = players;
         this.bowlsize = bowlsize;
         this.nfruits = bowlsize * players.length;
         this.fruitDist = dist;
 
         // generate the preference for each player
         preference = new int[players.length][];
         for (int i = 0; i != players.length; ++i) {
             preference[i] = genPref();
             players[i].init(players.length, preference[i].clone());
         }
 
         // compute expectation
         expectation = new int[players.length];
         for (int p = 0; p != players.length; ++p) {
             expectation[p] = computeExpectation(preference[p], dist, nfruits, bowlsize);
         }
 
         // bowl of player of both rounds
         bowlOfPlayer = new int[2][players.length][];
 
         printConfig();
     }
 
 
     private static int[] createServingBowl(String distPath, int nfruits) throws Exception {
         double[] dist = new double[12];
         int[] bowl = new int[12];
         
         Scanner sc = new Scanner(new File(distPath));
         // read the first 11 possibilities
         for (int i = 0; i < 11; ++i)
             dist[i] = sc.nextDouble();
         sc.close();
         
         // prefix sum
         for (int i = 1; i < 11; ++i) {
             dist[i] = dist[i-1] + dist[i];
         }
 
         if (dist[10] > 1)
             throw new Exception("Distribution does not sum to 1!");
         dist[11] = 1; // the last is always 1
 
         for (int i = 0; i < nfruits; ++i) {
             double r = random.nextDouble();
             int f = -1;
             for (int j = 0; j < 12; ++j) {
                 if (r < dist[j]) {
                     f = j;
                     break;
                 }
                     
             }
             assert f != -1;
             bowl[f]++;
         }
 
         int total = 0;
         for (int i = 0; i < 12; ++i)
             total += bowl[i];
 
         assert total == nfruits;
 
         return bowl;
     }
 
     // each round has a different fruit distribution and preference
     public static void main(String[] args) throws Exception
     {
         Random random = new Random();
         String playerPath = DEFAULT_PLAYERLIST;
         int bowlsize = DEFAULT_BOWL_SIZE;
         String distPath = DEFAULT_DISTRIBUTION;
        
         //PX**
         //String distrHomepath = "C:\\Users\\Peter\\workspace v1\\FruitSalad\\src\\fruit\\distrib";
        String filepath = new File("").getAbsolutePath();
        String distrHomepath = filepath.concat("\\fruit\\distrib\\");
 
         // player list
         if (args.length > 0)
             playerPath = args[0];
         // bowl size
         if (args.length > 1)
             bowlsize = Integer.parseInt(args[1]);
         // distribution
         if (args.length > 2) {
             distPath = args[2];
         }
         // enable gui?
         if (args.length > 3)
             gui = Boolean.parseBoolean(args[3]);
         if (args.length > 4)
             trace = Boolean.parseBoolean(args[4]);
         
       //PX**
         File distrdir = new File(distrHomepath);
         File[] distrList = distrdir.listFiles();
         
         Vector<Result> results = new Vector<Result>();
         
         int numruns = 2;
        
         for(int i=0; i < distrList.length; i++) {
         	Player[] players = null;
         	double avgScore = 0.0;
         	for(int x=0; x < numruns; x++) {
         		players = loadPlayers(playerPath);
         		shufflePlayer(players);
         		distPath = distrList[i].getAbsolutePath();
         		// read a fruit distribution
         		//        FruitGenerator fruitgen = (FruitGenerator)Class.forName(distgen).newInstance();
         		//        FruitGenerator fruitgen = new fruit.dumb.FruitGenerator();
 
         		int[] dist = createServingBowl(distPath, bowlsize * players.length);
 
         		Fruit game = new Fruit(players, bowlsize, dist);
         		game.play(gui);
         		avgScore += scores[g4handle.getIndex()];
 
         	}
         	avgScore /= numruns;
         	results.add(new Result(distrList[i].getName(), avgScore, bowlsize, players.length));
         }
         
         for(Result r : results) {
         	System.out.println(r.toString());
         }
         //**PX
     }
 
     
     // reset each round
     void resetRound(int rnd) {
         round = rnd;
         bowlId = 0;
 
         // reset choices
         choices = new int[players.length];
         hasBowl = new boolean[players.length];
         for (int i = 0; i < players.length; i++) {
             hasBowl[i] = false;
             choices[i] = rnd == 0 ? players.length - i - 1 : i;
         }
 
         // create a new copy of the fruit
         currentFruits = fruitDist.clone();
     }
 
     // names of all fruits
     private static String[] FRUIT_NAMES = {
         "Apples", "Bananas", "Cherries", "Dates",
         "Elderberries", "Figs", "Grapes", "Honeydew",
         "Ilama", "Jackfruit", "Kiwi", "Lychee"};
 
     private static String[] FRUIT_COLORS = {
         "red", "yellow", "purple", "brown",
         "crimson", "khaki", "darkred", "palegreen",
         "yellowgreen", "greenyellow", "darkgreen", "indianred"};
 
     
     // all players
     private Player[] players;
 
     // total fruits
     private int nfruits;
     // the size of a bowl
     private int bowlsize;
     // fruit repositories
     private int[] fruitDist;
 
     // round number 0 or 1
     private int round = 0;
     private int bowlId = 0;
     private int currentPlayer = -1;
     
     // current fruit
     private int[] currentFruits;
 
     // current bowl
     private int[] currentBowl;
     
     // the preference of each player
     private int[][] preference;
     // number of choices of player
     private int[] choices;
     // whether player has taken a bowl
     private boolean[] hasBowl;
     // bowl of player
     private int[][][] bowlOfPlayer;
     // expectation
     private int[] expectation;
     // score
     private static int[] scores; //PX**
 
     private String action;
 
     private static Random random = new Random();
     
     private static Player g4handle; //PX**
 }
