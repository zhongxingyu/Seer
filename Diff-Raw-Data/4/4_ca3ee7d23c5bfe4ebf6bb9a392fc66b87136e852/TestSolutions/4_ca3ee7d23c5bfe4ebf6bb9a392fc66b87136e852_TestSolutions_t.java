 package org.spacebar.escape;
 
 import java.io.ByteArrayInputStream;
 import java.io.File;
 import java.io.FileFilter;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 import java.util.*;
 
 import org.spacebar.escape.common.*;
 import org.spacebar.escape.common.hash.MD5;
 import org.spacebar.escape.j2se.EscapeFrame;
 import org.spacebar.escape.j2se.PlayerInfo;
 
 public class TestSolutions {
 
     private static Set<MD5> rejectures = new HashSet<MD5>();
     static {
 //        rejectures.add(new MD5("75b45c8e3fb338ba80cf69352e425508"));
     	rejectures.add(new MD5("a76ee47cd952e5bb05a20ca4738810b1"));
 //    	rejectures.add(new MD5("5b76e676e670fe3395b57553bfacb59a"));
     }
 
     private static FileFilter ff = new FileFilter() {
         public boolean accept(File pathname) {
             if (pathname.isDirectory()) {
                 return true;
             }
 
             String n = pathname.getName().toLowerCase();
             return n.endsWith(".esx");
         }
     };
 
     public static void main(String[] args) {
         if (args.length < 2) {
             System.out.println("Usage: " + TestSolutions.class.getName()
                     + " levels_dir player.esp [player2.esp ... ]");
             System.exit(1);
         }
 
         File f = new File(args[0]);
 
         int good = 0;
         int dubious = 0;
         int bad = 0;
         int unknown = 0;
         int rejecture = 0;
         int failure = 0;
 
         try {
             // given a directory, search all player files and all level
             // files and try the solutions on all of them
             Map<MD5, Level> levels = new HashMap<MD5, Level>();
             Map<Level, MD5> md5s = new HashMap<Level, MD5>();
             Map<Level, File> levelsToFiles = new HashMap<Level, File>();
 
             System.out.print("Loading...");
             System.out.flush();
             getAllStuff(f, levels, md5s, levelsToFiles);
             System.out.println(" " + levels.size() + " levels loaded");
 
             int maxLevelString = 0;
 
             for (int a = 1; a < args.length; a++) {
                 File pf = new File(args[a]);
 
                 // for each solution, verify
                 PlayerInfo pi = new PlayerInfo(new BitInputStream(
                         new FileInputStream(pf)));
 
                 System.out.println("*** Player: " + pi);
                 Map<MD5, List<Solution>> s = pi.getSolutions();
 
                 Map<Level, List<Solution>> levelsToSolutions = new HashMap<Level, List<Solution>>();
 
                 // the levels we have solutions for
                 for (Iterator<MD5> iterator = s.keySet().iterator(); iterator
                         .hasNext();) {
                     MD5 md5 = iterator.next();
                     Level l = levels.get(md5);
 
                     if (l == null) {
                         System.out.println(" " + md5 + " ?");
                         unknown++;
                         continue; // solution for unknown level?
                     }
 
                     List<Solution> sols = s.get(md5);
                     levelsToSolutions.put(l, sols);
                     String str = getStringForLevel(l, levelsToFiles);
                     maxLevelString = Math.max(maxLevelString, str.length());
                 }
 
                 final int mls = maxLevelString;
 
                 // the solutions for this level
                 for (Iterator<Level> i = levelsToSolutions.keySet().iterator(); i
                         .hasNext();) {
                     Level l = i.next();
                     List sols = levelsToSolutions.get(l);
 
                     for (Iterator iter = sols.iterator(); iter.hasNext();) {
                         final Solution sol = (Solution) iter.next();
                         
                         // ignore bookmarks
                         if (sol.isBookmark()) {
                             continue;
                         }
 
                         final String ls = getStringForLevel(l, levelsToFiles);
                         System.out.print(" " + ls + " " + sol.length()
                                 + " moves");
                         System.out.flush();
 
                         LevelManip lm = new LevelManip(l);
                         lm.optimize();
                         int result = sol.verify(new Level(lm));
 
                         int pad = mls - ls.length() + 10;
                         while (pad-- > 0) {
                             System.out.print(" ");
                         }
 
                         boolean reject = rejectures.contains(md5s.get(l));
 
                         if (reject) {
                             if (result > 0) {
                                 System.out.println("FAILURE");
                                 failure++;
                                 new EscapeFrame(l, sol);
                             } else if (result == -sol.length()) {
                                 System.out.println("REJECTURE at "
                                         + sol.length() + " (end)");
                                 rejecture++;
                             } else {
                                 System.out.println("REJECTURE at " + -result);
                                 rejecture++;
                             }
                         } else {
                             if (result == sol.length()) {
                                 System.out.println("OK");
                                 good++;
                             } else if (result > 0) {
                                 System.out.println("DUBIOUS at " + result);
                                 dubious++;
                                 new EscapeFrame(l, sol);
                             } else if (result == -sol.length()) {
                                 System.out.println("BAD at " + sol.length()
                                         + " (end)");
                                 bad++;
                                 new EscapeFrame(l, sol);
                             } else {
                                 System.out.println("BAD at " + -result);
                                 bad++;
                                 new EscapeFrame(l, sol);
                             }
                         }
                     }
                 }
             }
         } catch (IOException e) {
             e.printStackTrace();
         } finally {
             System.out.println(good + " good, " + bad + " bad, " + dubious
                     + " dubious, " + rejecture + " rejecture, " + failure
                     + " failure, " + unknown + " unknown");
         }
     }
 
     private static String getStringForLevel(Level l, Map<Level, File> levelsToFiles) {
         File f = levelsToFiles.get(l);
         return l.toString() + " [" + f.getName() + "]";
     }
 
     private static void getAllStuff(File f, Map<MD5, Level> levels, Map<Level, MD5> md5s,
             Map<Level, File> levelsToFiles) throws IOException {
 //    	System.out.println("checking " + f);
     	if (f.isDirectory()) {
             File files[] = f.listFiles(ff);
 
             for (int i = 0; i < files.length; i++) {
                 getAllStuff(files[i], levels, md5s, levelsToFiles);
             }
         } else {
             // level
            FileInputStream fis = new FileInputStream(f);
            byte l[] = Misc.getByteArrayFromInputStream(fis);
            fis.close();
             MessageDigest m = null;
 
             try {
                 m = MessageDigest.getInstance("MD5");
             } catch (NoSuchAlgorithmException e) {
                 e.printStackTrace();
             }
 
             MD5 md5 = new MD5(m.digest(l));
             Level ll = new EquateableLevel(
                     new BitInputStream(new ByteArrayInputStream(l)));
             levels.put(md5, ll);
             md5s.put(ll, md5);
             levelsToFiles.put(ll, f);
         }
     }
 }
