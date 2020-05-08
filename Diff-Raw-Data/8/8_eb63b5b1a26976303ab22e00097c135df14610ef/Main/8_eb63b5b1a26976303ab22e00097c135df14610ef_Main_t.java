 /*
  * Main.java -
  * Copyright (C) 2003 Klaus Rennecke, all rights reserved.
  * 
  * Permission is hereby granted, free of charge, to any person
  * obtaining a copy of this software and associated
  * documentation files (the "Software"), to deal in the
  * Software without restriction, including without limitation
  * the rights to use, copy, modify, merge, publish, distribute,
  * sublicense, and/or sell copies of the Software, and to
  * permit persons to whom the Software is furnished to do so,
  * subject to the following conditions:
  * 
  * The above copyright notice and this permission notice shall
  * be included in all copies or substantial portions of the
  * Software.
  * 
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY
  * KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
  * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
  * PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS
  * OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR
  * OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
  * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
  * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
  * 
  * Created on Mar 14, 2003 by Klaus Rennecke.
  */
 package net.sourceforge.fraglets.zieh;
 
 import java.awt.AWTEvent;
 import java.awt.EventQueue;
 import java.awt.FontMetrics;
 import java.awt.Window;
 import java.awt.event.MouseEvent;
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileFilter;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.OutputStream;
 import java.io.PrintStream;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Comparator;
 import java.util.Date;
 import java.util.Iterator;
 import java.util.StringTokenizer;
 
 import thinlet.FrameLauncher;
 import thinlet.Thinlet;
 
 /**
  * GUI frontend to psftp (PuTTY sftp comment line client).
  * 
  * Command line arguments:
  * <ul>
  * <li>-backend <var>path</var> sets the executable path for the psftp
  * backend.</li>
  * <li>-open <var>[user@]host</var> open specified connection upon
  * startup</li>
  * <li>-buffer <var>size</var> sets the scrollback buffer size in bytes</li>
  * </ul> 
  * 
  * @author Klaus Rennecke
 * @version $Revision: 1.10 $
  */
 public class Main extends Thinlet implements Comparator {
     
     private Process sftp;
     private String sftpCommand; // = "C:\\Programme\\PuTTY\\psftp.exe -v";
     private PrintStream sftpStream;
     private String sftpPrompt = "psftp> ";
     
     private String sftpCd = "Remote directory is now ";
     private String sftpLCd = "New local directory is ";
     private String sftpOpenPwd = "Remote working directory is ";
     private String sftpPwd = "Remote directory is ";
     private String sftpLPwd = "Current local directory is ";
     private String sftpLs = "Listing directory ";
     private int scrollbackSize = 4096;
     
     
     private String sftpListingPath;
     private ArrayList sftpListing;
     
     private Runnable updater;
     private FontMetrics metrics;
     
     private String queue[];
     private int queueHead;
     private int queueSize;
     
     private String stack[];
     private int stackTop;
     
     private int sessionLength;
     private boolean sessionIdle;
     private boolean remoteChanges;
     private boolean localChanges;
     private String multiListing;
     private StringBuffer chatOutput = new StringBuffer();
     
     private int mouseX;
     private int mouseY;
     private int lastX;
     private int lastY;
     
     public Main(String args[]) {
         try {
             add(parse("main.xml"));
         } catch (Exception ex) {
             ex.printStackTrace();
         }
         parseArgs(args);
         
         updater = new Runnable() {
             public void run() {
                 updateChat();
             }
         };
     }
     
     protected void parseArgs(String args[]) {
         String openCommand = null;
         for (int i = 0; i < args.length; i++) {
             String arg = args[i];
             if (arg.equals("-backend")) {
                 if (++i >= args.length) {
                     System.err.println("option requires an argument: -backend");
                 } else {
                     setBackend(args[i]);
                 }
             } else if (arg.equals("-open")) {
                 if (++i >= args.length) {
                     System.err.println("option requires an argument: -open");
                 } else {
                     openCommand = args[i];
                 }
             } else if (arg.equals("-buffer")) {
                 if (++i >= args.length) {
                     System.err.println("option requires an argument: -buffer");
                 } else {
                     try {
                         scrollbackSize = Integer.parseInt(arg);
                     } catch (NumberFormatException e) {
                         System.err.println("argument for -buffer is not a number");
                     }
                 }
             } else {
                 System.err.println("unknown argument or option: "+arg);
             }
         }
         if (openCommand != null) {
             sessionCommand(openCommand);
         }
     }
     
     public void init () {
         ChatStream cs = new ChatStream();
         PrintStream ps = new PrintStream(cs);
         System.setErr(ps);
         System.setOut(ps);
         
         setString(find("localPath"), "text", System.getProperty("user.dir"));
         initLocal(find("localList"));
         
         if (sftpCommand == null) {
             Thread finder = new Thread("Zieh backend finder") {
                 public void run() {
                     findBackend();
                 }
             };
             finder.setDaemon(true);
             finder.start();
         }
         
         requestFocus(find("sessionChat"));
     }
     
     protected void findBackend() {
         File[] roots = File.listRoots();
         String guess[] = new String[] {
             "Program Files\\PuTTY", "Programme\\PuTTY", "PuTTY"
         };
         
         // guessing
         for (int i = 0; i < roots.length; i++) {
             for (int j = 0; j < guess.length; j++) {
                 try {
                     // shortcut
                     if (Thread.currentThread().isInterrupted()) return;
                     if (sftpCommand != null) return;
                     findBackend(new File(roots[i], guess[j]));
                 } catch (Exception ex) {
                     // ignore
                 }
             }
         }
         
         // brute force
         for (int i = 0; i < roots.length; i++) {
             try {
                 if (Thread.currentThread().isInterrupted()) return;
                 if (sftpCommand != null) return;
                 findBackend(roots[i]);
             } catch (Exception ex) {
                 // ignore
             }
         }
     }
     
     public static final FileFilter DIR_FILTER = new FileFilter() {
         public boolean accept(File f) { return f.isDirectory(); }
     };
     
     protected void findBackend(File root) throws IOException {
         String name = root.toString();
         if (name.toLowerCase().startsWith("a:")) {
             return;
         }
         setStatus("searching: "+name);
         File probe =new File(root, "psftp.exe"); 
         if (probe.exists()) {
             setStatus("");
             setBackend(probe.getAbsolutePath());
             return;
         }
         File list[] = root.listFiles(DIR_FILTER);
         for (int i = 0; i < list.length; i++) {
             if (Thread.currentThread().isInterrupted()) return;
             if (sftpCommand != null) return;
             findBackend(list[i]);
         }
     }
     
     protected void setBackend(String name) {
         appendChat("backend: '"+name+"'");
         sftpCommand = name;
     }
 
     /////
     // -- handlers
 
     public void actionExit() {
         actionClose();
         System.exit(0);
     }
 
     public void actionAbout() {
         add("about.xml");
        setString(find("revisionLabel"), "text", "$Revision: 1.10 $");
     }
     
     public void actionHelp() {
         sessionCommand("help");
     }
     
     public void actionClose() {
         try {
             if (sftp != null) {
                 clearQueue();
                 
                 sendCommand("bye", false);
                 sftpStream.close();
                 try {
                     sftp.getOutputStream().close();
                 } catch (IOException ex) {
                     ex.printStackTrace();
                 }
                 sftp.waitFor();
                 sftp = null;
                 setBoolean(find("menuClose"), "enabled", false);
                 
                 // try harder to get rid of extra open descriptors.
                 System.gc();
                 Thread.yield();
                 System.gc();
             }
             setBoolean(find("menuOpen"), "enabled", true);
         } catch (Exception ex) {
             ex.printStackTrace();
         }
     }
     
     public void actionOpen() {
         if (sftp != null) {
             return;
         }
         try {
             sftp = Runtime.getRuntime().exec(sftpCommand);
             Thread t;
             
             t = new Thread
                 (new ChatStream(sftp.getInputStream(), sftpPrompt),
                 "Zieh input shuffler");
             t.setDaemon(true);
             t.start();
             
             t = new Thread
                 (new ChatStream(sftp.getErrorStream()),
                 "Zieh error shuffler");
             t.setDaemon(true);
             t.start();
             
             sftpStream = new PrintStream(sftp.getOutputStream());
             
             Thread.sleep(400); // wait for new window
             ((Window)getParent()).toFront(); // nuisance
             setBoolean(find("menuOpen"), "enabled", false);
             setBoolean(find("menuClose"), "enabled", true);
         } catch (Exception ex) {
             ex.printStackTrace();
         }
     }
     
     public void actionLcd() {
         String value;
         Object path = find("localPath");
         int index = getInteger(path, "selected");
         if (index != -1) {
             value = (String)getProperty(getItem(path, index), "file");
         } else {
             value = getString(path, "text");
         }
         sessionCommand("lcd "+quoteSsh2(value));
     }
     
     public void actionCd() {
         String value;
         Object path = find("remotePath");
         int index = getInteger(path, "selected");
         if (index != -1) {
             value = (String)getProperty(getItem(path, index), "file");
         } else {
             value = getString(path, "text");
         }
         sessionCommand("cd "+quoteSsh2(value));
         sessionCommand("ls");
     }
     
     public void actionLocalRemove() {
         File base = new File(System.getProperty("user.dir"));
         Object items[] = getSelectedItems(find("localList"));
         for (int i = 0; i < items.length; i++) {
             Object row = items[i];
             String name = getString(getItem(row, 0), "text");
             if (name.equals("..")) {
                 continue; // ignore
             }
             localRemove(new File(base, name));
         }
         initLocal(find("localList"));
     }
     
     public void actionRemoteRemove() {
         Object items[] = getSelectedItems(find("remoteList"));
         for (int i = 0; i < items.length; i++) {
             Object row = items[i];
             String name = getString(getItem(row, 0), "text");
             if (name.equals("..")) {
                 continue; // ignore
             }
             boolean dir = "true".equals(getProperty(row, "directory"));
             sessionCommand((dir ? "mrm " : "rm ") + quoteSsh2(name), false);
         }
     }
     
     public void actionPut() {
         Object items[] = getSelectedItems(find("localList"));
         for (int i = 0; i < items.length; i++) {
             Object row = items[i];
             String name = getString(getItem(row, 0), "text");
             if (name.equals("..")) {
                 continue; // ignore
             }
             if ("true".equals(getProperty(row, "directory"))) {
                 sessionCommand("mput " + name, false);
             } else {
                 sessionCommand("put " + quoteSsh2(name), false);
             }
         }
         enable(ALL_CONTROLS, sessionIdle);
     }
     
     public void actionGet() {
         Object items[] = getSelectedItems(find("remoteList"));
         for (int i = 0; i < items.length; i++) {
             Object row = items[i];
             String name = getString(getItem(row, 0), "text");
             if (name.equals("..")) {
                 continue; // ignore
             }
             boolean dir = "true".equals(getProperty(row, "directory"));
             sessionCommand((dir ? "mget " : "get ") + quoteSsh2(name), false);
         }
         enable(ALL_CONTROLS, sessionIdle);
     }
     
     protected void localRemove(File file) {
         if (file.isDirectory()) {
             File list[] = file.listFiles();
             for (int i = 0; i < list.length; i++) {
                 localRemove(list[i]);
             }
         }
         file.delete();
     }
     
     public void localSelect() {
         updateState(find("localList"), find("remoteList"),
             find("localRemove"), find("remoteRemove"),
             find("localPut"), find("remoteGet"));
     }
 
     public void remoteSelect() {
         updateState(find("remoteList"), find("localList"),
             find("remoteRemove"), find("localRemove"),
             find("remoteGet"), find("localPut"));
     }
     
     protected void triggerSelection() {
         {
             Object item = getSelectedItem(find("localList"));
             if (item != null) {
                 triggerSelection("lcd", "lcd", item);
                 return;
             }
         }
         {
             Object item = getSelectedItem(find("remoteList"));
             if (item != null) {
                 triggerSelection("cd", "cd", item);
                 return;
             }
         }
     }
     
     /**
      * Destroy the application window (close clicked).
      * @see thinlet.Thinlet#destroy()
      */
     public boolean destroy() {
         actionClose();
         return super.destroy();
     }
     
     /////
     // state handling
 
     protected void triggerSelection(String cd, String mv, Object row) {
         String name = getString(getItem(row, 0), "text");
         boolean dir = "true".equals(getProperty(row, "directory"));
         sessionCommand((dir ? cd : mv) + " " + quoteSsh2(name));
         return;
     }
 
     public void license(Object old, Object which) {
         String name = getString(which, "name");
         remove(old);
         add("license.xml");
         loadLicense(find("licenseText"), name);
     }
     
     public void loadLicense(Object area, String name) {
         InputStream inputstream = null;
         try {
             inputstream = getClass().getResourceAsStream(name);
         } catch (Exception ex) {
             ex.printStackTrace();
             return;
         }
         BufferedReader reader =
             new BufferedReader(new InputStreamReader(inputstream));
         StringBuffer buffer = new StringBuffer();
         String line;
         try {
             while ((line = reader.readLine()) != null) {
                 line = line.replace('\f', '\n').replace('\t', ' ');
                 buffer.append(line).append('\n');
             }
             reader.close();
         } catch (IOException ex) {
             ex.printStackTrace();
         }
         setString(area, "text", buffer.toString());
     }
     
     public void initLocal(Object c) {
         String dir = System.getProperty("user.dir");
         setString(find("localPath"), "text", dir);
         removeAll(c);
         File current = new File(dir);
         initLocalPath(current);
         File list[] = current.listFiles();
         DateFormat df = SimpleDateFormat
             .getDateTimeInstance(SimpleDateFormat.SHORT, SimpleDateFormat.SHORT);
         if (list == null || list.length == 0) {
             list = new File[] { new File("..") };
         } else {
             Arrays.sort(list, this);
         }
         boolean empty = true;
         int width[] = new int[] {20, 20, 20 };
         for (int i = 0; i < list.length; i++) {
             if (".".equals(list[i].getName())) {
                 // ignore .
                 continue;
             } else if (empty && !"..".equals(list[i].getName())) {
                 // insert ..
                 addLocalEntry(c, new File(".."), df, width);
                 
             }
             addLocalEntry(c, list[i], df, width);
             empty = false;
         }
         setColumns(c, width);
     }
     
     protected void addLocalEntry(Object c, File file, DateFormat df, int width[]) {
         Object row = create("row");
         String text;
         Object cell;
         cell = create("cell");
         setString(cell, "text", text = file.getName());
         width[0] = max(width[0], width(text));
         add(row, cell);
         cell = create("cell");
         setString(cell, "text", text = String.valueOf(file.length()));
         width[1] = max(width[1], width(text));
         add(row, cell);
         cell = create("cell");
         setString(cell, "text", text = df.format(new Date(file.lastModified())));
         width[2] = max(width[2], width(text));
         add(row, cell);
         putProperty(row, "directory", String.valueOf(file.isDirectory()));
         add(c, row);
     }
     
     protected void initLocalPath(File dir) {
         Object path = find("localPath");
         removeAll(path);
         File roots[] = File.listRoots();
         ArrayList dirs = new ArrayList();
         File parent = dir;
         do {
             dirs.add(parent);
             parent = parent.getParentFile();
         } while (parent != null);
         
         parent = (File)dirs.get(dirs.size() - 1);
         int rootIndex = 0;
         while (rootIndex < roots.length) {
             if (parent.equals(roots[rootIndex])) {
                 break; // found root
             } else {
                 rootIndex += 1;
             }
         }
         
         // create indent string
         int scan = dirs.size() - 1;
         StringBuffer buffer = new StringBuffer(scan);
         while (--scan >= 0) {
             buffer.append(' ');
         }
         String indent = buffer.toString();
         
         if (rootIndex == roots.length) {
             // not found in roots, add before
             scan = dirs.size();
             while (--scan >= 0) {
                 Object item = create("choice");
                 File part = (File)dirs.get(scan);
                 setString(item, "text",
                     indent.substring(scan) + describeShort(part));
                 putProperty(item, "file", part.getAbsolutePath());
                 add(path, item);
             }
         }
         for (int i = 0; i < roots.length; i++) {
             if (i == rootIndex) {
                 scan = dirs.size();
                 while (--scan >= 0) {
                     Object item = create("choice");
                     File part = (File)dirs.get(scan);
                     setString(item, "text",
                         indent.substring(scan) + describeShort(part));
                     putProperty(item, "file", part.getAbsolutePath());
                     add(path, item);
                 }
             } else {
                 Object item = create("choice");
                 setString(item, "text", describeShort(roots[i]));
                 putProperty(item, "file", roots[i].getAbsolutePath());
                 add(path, item);
             }
         }
     }
     
     protected void initRemotePath() {
         Object path = find("remotePath");
         removeAll(path);
         String dir = getString(path, "text");
         StringTokenizer tok = new StringTokenizer(dir, "\\/", true);
         StringBuffer indent = new StringBuffer();
         StringBuffer absolute = new StringBuffer();
         if (dir.startsWith("/")) {
             Object choice = create("choice");
             setString(choice, "text", "/");
             putProperty(choice, "file", "/");
             add(path, choice);
             indent.append(' ');
             absolute.append('/');
         }
         while (tok.hasMoreTokens()) {
             dir = tok.nextToken();
             absolute.append(dir);
             if (dir.equals("/") || dir.equals("\\")) {
                 continue;
             }
             Object choice = create("choice");
             setString(choice, "text", indent + dir);
             putProperty(choice, "file", absolute.toString());
             add(path, choice);
             indent.append(' ');
         }
     }
     
     public static String describeShort(File file) {
         String name = file.getName();
         if (name != null && name.length() > 0) {
             return name;
         } else {
             return file.toString();
         }
     }
     
     /////
     // interaction
     
     protected void parseSession(String line, Object source) {
         if (sftp != null) {
             if (source == sftp.getErrorStream()) {
                 appendChat("ERROR: '"+line+"'");
                 return;
             } else if (source == sftp.getInputStream()) {
                 parseSession(line);
                 return;
             }
         }
         appendChat(line);
     }
     
     protected void parsePrompt() {
 //        appendChat("[prompt]");
         parseListResults();
         String command = dequeue();
         if (command != null) {
             sendCommand(command);
         } else {
             sessionIdle = true;
             if (remoteChanges) {
                 remoteChanges = false;
                 sessionCommand("ls");
             } else if (localChanges) {
                 localChanges = false;
                 sessionCommand("lls");
             } else {
                 setStatus("idle");
                 appendChat(sftpPrompt, false);
                 enable(ALL_CONTROLS, true);
             }
         }
     }
     
     protected void parseSession(String line) {
 //        appendChat("["+line+"]");
         if (line.startsWith(sftpOpenPwd)) {
             parseRcd(line.substring(sftpOpenPwd.length()));
         } else if (line.startsWith(sftpCd)) {
             parseRcd(line.substring(sftpCd.length()));
         } else if (line.startsWith(sftpLCd)) {
             String newCd = line.substring(sftpLCd.length());
             System.setProperty("user.dir", newCd);
             Object localList = find("localList");
             initLocal(localList);
             requestFocus(find("sessionChat"));
         } else if (line.startsWith(sftpPwd)) {
             parseRcd(line.substring(sftpPwd.length()));
         } else if (line.startsWith(sftpLs)) {
             sftpListingPath = line.substring(sftpLs.length());
             sftpListing = new ArrayList();
             if (multiListing == null &&
                 !sftpListingPath.equals(getString(find("remotePath"), "text"))) {
                 appendChat(line);
             }
         } else if (sftpListingPath != null) {
             sftpListing.add(line);
             if (multiListing == null &&
                 !sftpListingPath.equals(getString(find("remotePath"), "text"))) {
                 appendChat(line);
             }
         } else if (line.startsWith("local:") && line.indexOf("=> remote:") > 0) {
             remoteChanges = true;
         } else if (line.startsWith("remote:") && line.indexOf("=> local:") > 0) {
             localChanges = true;
         } else {
             multiListing = null;
             sftpListingPath = null;
             appendChat(line);
         }
     }
     
     protected void parseRcd(String dir) {
         Object rp = find("remotePath");
         String old = getString(rp, "text");
         if (!dir.equals(old)) {
             setString(rp, "text", dir);
             initRemotePath();
             if (!isQueued("ls")) {
                 sessionCommand("ls");
             }
         }
     }
     
     protected void parseListResults() {
         if (sftpListingPath != null) {
             if (multiListing != null) {
                 String action = multiListing;
                 multiListing = null;
                 String start = getString(find("remotePath"), "text");
                 if (sftpListingPath.startsWith(start)) {
                     start = sftpListingPath.substring(start.length() + 1);
                     if (action.equals("get")) {
                         new File(System.getProperty("user.dir"), start).mkdirs();
                     }
                     boolean subdirs = false;
                     for (Iterator i = sftpListing.iterator(); i.hasNext();) {
                         String element = (String)i.next();
                         if (element.length() <= 56) {
                             appendChat("OOPS: short listing element: '"+element+"'");
                             continue;
                         }
                         String name = element.substring(56);
                         if (name.equals("..") || name.equals(".")) {
                             continue; // skip
                         }
                         if (element.startsWith("d")) {
                             sessionCommand("m" + action + " "
                                 + quoteSsh2(start + '/' + name));
                             subdirs = true;
                         } else if (action.equals("get")) {
                             sessionCommand(action + " "
                                 + quoteSsh2(start + '/' + name) + " "
                                 + quoteSsh2(start + '/' + name));
                         } else {
                             sessionCommand(action + " "
                                 + quoteSsh2(start + '/' + name));
                         }
                     }
                     if (action.equals("rm") || action.equals("del")) {
                         sessionCommand("rmdir " + quoteSsh2(start));
                     }
                 } else {
                     appendChat("OOPS: "+sftpListingPath+" not in "+start);
                 }
             } else {
                 if (sftpListingPath.equals(getString(find("remotePath"), "text"))) {
                     Object list = find("remoteList");
                     int width[] = new int[] { 20, 20, 20 };
                     removeAll(list);
                     for (Iterator i = sftpListing.iterator(); i.hasNext();) {
                         String element = (String)i.next();
                         parseRemoteList(element, list, width);
                     }
                     setColumns(list, width);
                     setBoolean(find("remotePath"), "enabled", true);
                     setBoolean(find("remoteList"), "enabled", true);
                     requestFocus(find("sessionChat"));
                 }
             }
             sftpListingPath = null;
         }
     }
     
     protected void parseRemoteList(String line, Object list, int width[]) {
         // drwxr-xr-x   19 root     root         4096 Mar 27 13:49 .
         // crw-rw-rw-    1 root     root            0 Mar 16 17:02 tty
         
         // 041777 0 3 657 1049222674 .
         // 040755 0 0 1024 1048099314 ..
         // 040775 0 0 176 1048099412 .X11-pipe
         // 040775 0 0 176 1048099412 .X11-unix
         // 0100600 900 10 1 1049186395 .root_display
         // 0100600 900 10 1 1049186395 .root_xauthority
         
         StringTokenizer tok = new StringTokenizer(line, " ");
         String perms, owner, group, size, date, name;
         perms = tok.nextToken(); // permissions
         if (perms.startsWith("0")) {
             // numeric format
             owner = tok.nextToken();
             group = tok.nextToken();
             size = tok.nextToken();
             date = tok.nextToken();
             name = tok.nextToken("").substring(1); // skip last delimiter
             
            date = new Date(Long.parseLong(date) * 1000).toString();
         } else {
             // string format
             String links = tok.nextToken();
             owner = tok.nextToken(); // owner
             group = tok.nextToken(); // group
             size = tok.nextToken();
             String rest = tok.nextToken("");
             date = rest.substring(0, 13);
             name = rest.substring(14);
         }
         
         if (name.equals(".")) {
             // ignore .
             return;
         }
         
         Object row, cell;
         row = create("row");
         cell = create("cell");
         setString(cell, "text", name);
         width[0] = max(width[0], width(name));
         add(row, cell);
         cell = create("cell");
         setString(cell, "text", size);
         width[1] = max(width[1], width(size));
         add(row, cell);
         cell = create("cell");
         setString(cell, "text", date);
         width[2] = max(width[2], width(date));
         add(row, cell);
         boolean isDir = "dl".indexOf(perms.charAt(0)) >= 0;
         putProperty(row, "directory", String.valueOf(isDir));
         add(list, row);
     }
     
     public void sessionInsert() {
         Object chat = find("sessionChat");
         String text = getString(chat, "text");
         if (text.length() > sessionLength) {
             int end = getInteger(chat, "end");
             if (end >= 0 && text.charAt(end) == '\n') {
                 int start = text.lastIndexOf('\n', end - 1) + 1;
                 String command = text.substring(start, end);
                 while (command.startsWith(sftpPrompt)) {
                     command = command.substring(sftpPrompt.length());
                 }
                 sessionCommand(command, false);
             }
         }
         sessionLength = text.length();
     }
     
     public void sessionCommand(String command) {
         sessionCommand(command, true);
     }
     
     public void sessionCommand(String command, boolean echo) {
         if (sftpStream == null) {
             actionOpen();
         }
         if (sftpStream != null) {
             boolean beenIdle = sessionIdle;
             
             if (sessionIdle) {
                 sendCommand(command, echo);
             } else if (command.startsWith("rmdir ")) {
                 push(command);
             } else {
                 enqueue(command);
             }
             
             if (beenIdle != sessionIdle) {
                 if (command.startsWith("put ") || command.startsWith("get ")
                     || command.startsWith("reput ") || command.startsWith("reget ")) {
                     enable(ALL_CONTROLS, sessionIdle);
                 } else if (command.equals("ls")) {
                     enable(REMOTE_CONTROLS, sessionIdle);
                 }
             }
             if (!remoteChanges) {
                 if (command.startsWith("rm ") || command.startsWith("del ")
                     || command.startsWith("mv ") || command.startsWith("ren ")
                     || command.startsWith("mkdir ") || command.startsWith("rmdir ")) {
                     remoteChanges = true;
                 }
             }
             if (!localChanges) {
                 if (command.startsWith("!")) {
                     localChanges = true;
                 }
             }
         }
     }
     
     protected void sendCommand(String command) {
         sendCommand(command, true);
     }
     
     protected void sendCommand(String command, boolean echo) {
         if (command.equals("lls")) {
             // builtin
             initLocal(find("localList"));
             // synthetic
             parsePrompt();
             return;
         }
         
         if (command.startsWith("mput ")) {
             // builtin
             String start = command.substring(5);
             File base = new File(System.getProperty("user.dir"), start);
             File list[] = base.listFiles();
             sessionCommand("mkdir " + quoteSsh2(start), false);
             for (int i = 0; i < list.length; i++) {
                 File file = list[i];
                 String name = start + '/' + file.getName();
                 if (file.isDirectory()) {
                     sessionCommand("mput " + name, false);
                 } else {
                     sessionCommand("put "
                         + quoteSsh2(name) + ' ' + quoteSsh2(name), false);
                 }
             }
             // synthetic
             parsePrompt();
             return;
         }
         
         if (command.startsWith("mget ")) {
             // builtin
             command = "ls " + command.substring(5);
             multiListing = "get";
         } else if (command.startsWith("mrm ")) {
             // builtin
             command = "ls " + command.substring(4);
             multiListing = "rm";
         } else if (command.startsWith("mdel ")) {
             // builtin
             command = "ls " + command.substring(5);
             multiListing = "rm";
         }
         
         if (sessionIdle && echo) {
             appendChat(command);
         }
         sessionIdle = false;
         setStatus("command: '"+command+"'");
         sftpStream.println(command);
         sftpStream.flush();
         appendHistory(command);
     }
     
     protected void appendChat(String line) {
         appendChat(line, true);
     }
     
     protected void appendChat(String line, boolean newline) {
         if (line != null && line.length() > 0) {
             synchronized(chatOutput) {
                 chatOutput.append(line);
                 if (newline && line.charAt(line.length() - 1) != '\n') {
                     chatOutput.append('\n');
                 }
             }
         }
         if (isShowing()) {
             EventQueue.invokeLater(updater);
         }
     }
     
     protected void updateChat() {
         StringBuffer buffer;
         Object chat;
         synchronized (chatOutput) {
             if (chatOutput.length() == 0) {
                 return;
             }
             chat = find("sessionChat");
             buffer = new StringBuffer(getString(chat, "text"));
             buffer.append(chatOutput);
             chatOutput.setLength(0);
         }
         if (buffer.length() > scrollbackSize) {
             int end = buffer.length();
             int cutoff = buffer.length() - scrollbackSize + scrollbackSize/4;
             while (cutoff < end && buffer.charAt(cutoff) != '\n') {
                 cutoff += 1;
             }
             buffer.delete(0, cutoff);
         }
         setString(chat, "text", buffer.toString());
         sessionLength = buffer.length();
         setInteger(chat, "start", sessionLength);
         setInteger(chat, "end", sessionLength);
     }
 
     protected void appendHistory(String line) {
         Object history = find("sessionHistory");
         StringBuffer buffer = new StringBuffer(getString(history, "text"));
         buffer.append(line);
         if (buffer.charAt(buffer.length() - 1) != '\n') {
             buffer.append('\n');
         }
         if (buffer.length() > scrollbackSize) {
             int end = buffer.length();
             int cutoff = buffer.length() - scrollbackSize + scrollbackSize/4;
             while (cutoff < end && buffer.charAt(cutoff) != '\n') {
                 cutoff += 1;
             }
             buffer.delete(0, cutoff);
         }
         int length = buffer.length();
         setString(history, "text", buffer.toString());
         setInteger(history, "start", length);
         setInteger(history, "end", length);
     }
 
     /////
     // -- handler utilities
 
     protected void add(String name) {
         try {
             super.add(parse(name));
         } catch (IOException ex) {
             ex.printStackTrace();
         }
     }
     
     protected void enable(String names[], boolean state) {
         for (int i = 0; i < names.length; i++) {
             String name = names[i];
             setBoolean(find(name), "enabled", state);
         }
     }
     
     protected static final String[] ALL_CONTROLS = {
         "localPath", "localList", "localPut", "localRemove",
         "remotePath", "remoteList", "remoteGet", "remoteRemove",
     };
     
     protected static final String[] REMOTE_CONTROLS = {
         "remotePath", "remoteList", "remoteGet", "remoteRemove",
     };
     
     protected void clearSelection(Object list) {
         Object items[] = getItems(list);
         for (int i = 0; i < items.length; i++) {
             setBoolean(items[i], "selected", false);
         }
     }
     
     protected void updateState(Object s, Object o,
         Object srm, Object orm, Object smv, Object omv) {
         lastX = mouseX;
         lastY = mouseY;
         clearSelection(o);
         setBoolean(orm, "enabled", false);
         setBoolean(omv, "enabled", false);
         boolean something = getSelectedIndex(s) >= 0;
         setBoolean(srm, "enabled", something);
         setBoolean(smv, "enabled", something);
     }
     
     protected void setStatus(String text) {
         setString(find("status"), "text", text);
     }
     
     public static boolean near(int a, int b) {
         int d = a - b;
         return d <= 2 && d >= -2;
     }
     
     protected int width(String s) {
         if (metrics == null) {
             metrics = getFontMetrics(getFont());
         }
         return metrics.stringWidth(s);
     }
     
     protected void setColumns(Object table, int width[]) {
         for (int i = 0; i < width.length; i++) {
             setInteger(getItem(table, "column", i), "width", width[i] + 10);
         }
     }
     
     public static int max(int a, int b) {
         return a > b ? a : b;
     }
     
     protected synchronized void enqueue(String cmd) {
         if (queue == null) {
             queue = new String[20];
             queueHead = 0;
             queueSize = 1;
         } else if (++queueSize > queue.length) {
             String grow[] = new String[queue.length + queue.length / 2];
             if (queueHead < queue.length) {
                 System.arraycopy(queue, queueHead, grow, 0, queue.length - queueHead);
             }
             if (queueHead > 0) {
                 System.arraycopy(queue, 0, grow, queue.length - queueHead, queueHead);
             }
             queueHead = queue.length;
             queue = grow;
         }
         
         if (queueHead == queue.length) {
             queueHead = 0;
         }
 
         queue[queueHead++] = cmd;
         updateQueue();
     }
     
     protected synchronized void push(String cmd) {
         if (stack == null) {
             stack = new String[20];
         } else if (stackTop >= stack.length) {
             String grow[] = new String[stack.length + stack.length / 2];
             System.arraycopy(stack, 0, grow, 0, stack.length);
             stack = grow;
         }
         stack[stackTop++] = cmd;
         updateQueue();
     }
     
     protected synchronized String dequeue() {
         try {
             if (queue != null && queueSize > 0) {
                 int index = queueHead - queueSize--;
                 if (index < 0) {
                     index += queue.length;
                 }
                 return queue[index];
             } else if (stack != null && stackTop > 0) {
                 return stack[--stackTop];
             } else {
                 return null;
             }
         } finally {
             updateQueue();
         }
     }
     
     public synchronized void clearQueue() {
         queue = null;
         stack = null;
         queueSize = 0;
         stackTop = 0;
         updateQueue();
     }
     
     protected void updateQueue() {
         setString(find("queue"), "text", String.valueOf(queueSize + stackTop));
     }
     
     protected synchronized boolean isQueued(String cmd) {
         if (queue != null) {
             int index = queueHead - queueSize;
             if (index < 0) {
                 index += queue.length;
             }
             while (index != queueHead) {
                 if (cmd.equals(queue[index])) {
                     return true;
                 } else if (++index >= queue.length) {
                     index = 0;
                 }
             }
         }
         return false;
     }
     
     public static String quoteSsh2(String str) {
         if (str.indexOf('"') < 0) {
             return '"'+str+'"';
         } else {
             StringTokenizer tok = new StringTokenizer(str, "\"", true);
             StringBuffer buffer = new StringBuffer(str.length() + 2 * tok.countTokens());
             buffer.append('"');
             while (tok.hasMoreTokens()) {
                 String token = tok.nextToken();
                 if (token.equals("\"")) {
                     buffer.append("\"\"");
                 } else {
                     buffer.append(token);
                 }
             }
             buffer.append('"');
             return buffer.toString();
         }
     }
     
     /////
     // -- launcher
 
     public static void main(String[] args) {
         Main main = new Main(args);
         new FrameLauncher("Zieh", main, 620, 440);
     }
     
     /////
     // -- inner classes
     
     public class ChatStream extends OutputStream implements Runnable {
         
         protected InputStream in;
         
         protected char prompt[];
         
         protected boolean match;
         
         protected StringBuffer buffer = new StringBuffer();
         
         protected ChatEvent promptEvent = new ChatEvent(null, null);
         
         public ChatStream(InputStream in, String prompt) {
             this.in = in;
             if (prompt != null) {
                 this.prompt = prompt.toCharArray();
                 this.match = true;
             }
         }
         
         public ChatStream(InputStream in) {
             this(in, null);
         }
         
         public ChatStream() {
             this(null, null);
         }
         
         /**
          * @see java.io.OutputStream#write(int)
          */
         public void write(int b) throws IOException {
             switch (b) {
                 case '\r':
                 case '\n':
                     if (buffer.length() > 0) {
                         EventQueue.invokeLater
                             (new ChatEvent(buffer.toString(), in));
                         buffer.setLength(0);
                         match = prompt != null;
                     }
                     return;
                 case '\t':
                     buffer.append(' ');
                     break;
                 default:
                     buffer.append((char)b);
                     break;
             }
             if (match) {
                 int index = buffer.length() - 1;
                 if (index < prompt.length) {
                     match = prompt[index] == buffer.charAt(index);
                     if (match && index + 1 == prompt.length) {
                         buffer.setLength(0);
                         try {
                             EventQueue.invokeLater(promptEvent); 
                         } catch (RuntimeException ex) {
                             ex.printStackTrace();
                         }
                     }
                 } else {
                     match = false; // unlikely
                 }
             }
         }
         
         /**
          * Thread runner method.
          * @see java.lang.Runnable#run()
          */
         public void run() {
             try {
                 int n;
                 while (!Thread.interrupted() && (n = in.read()) != -1) {
                     write(n);
                 }
             } catch (Exception ex) {
                 ex.printStackTrace();
             }
             
             if (sftp != null && in == sftp.getInputStream()) {
                 actionClose();
                 appendChat("backend closed");
             }
         }
 
     }
     
     public class ChatEvent implements Runnable {
         private String text;
         private InputStream source;
         public ChatEvent(String text, InputStream source) {
             this.text = text;
             this.source = source;
         }
         public void run() {
             if (text != null) {
                 parseSession(text, source);
             } else {
                 parsePrompt();
             }
         }
     }
     
     /**
      * @see java.awt.Component#processEvent(java.awt.AWTEvent)
      */
     protected void processEvent(AWTEvent e) {
         int id = e.getID();
         if (id == MouseEvent.MOUSE_PRESSED) {
             MouseEvent me = (MouseEvent)e;
             switch (me.getClickCount()) {
                 case 2:
                     if (near(lastX, me.getX()) && near(lastY, me.getY())) {
                         triggerSelection();
                         return; // consumed
                     }
                 default:
                     mouseX = me.getX();
                     mouseY = me.getY();
                     break;
             }
         } else {
             mouseX = -1;
             mouseY = -1;
         }
         super.processEvent(e);
     }
 
     /**
      * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
      */
     public int compare(Object o1, Object o2) {
         return compare((File)o1, (File)o2);
     }
 
     /**
      * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
      */
     public int compare(File f1, File f2) {
         return f1.getName().toLowerCase().compareTo(f2.getName().toLowerCase());
     }
 
     /**
      * @see java.awt.Component#addNotify()
      */
     public void addNotify() {
         super.addNotify();
         init();
         if (chatOutput.length() > 0) {
             EventQueue.invokeLater(updater);
         }
     }
 
 }
