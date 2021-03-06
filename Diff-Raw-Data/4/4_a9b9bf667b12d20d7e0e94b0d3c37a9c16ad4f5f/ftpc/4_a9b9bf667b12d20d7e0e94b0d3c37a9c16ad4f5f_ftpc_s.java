 // ftpc.java
 // (C) by Michael Peter Christen; mc@yacy.net
 // first published on http://www.anomic.de
 // Frankfurt, Germany, 2002, 2004, 2006
 // main implementation finished: 28.05.2002
 // last major change: 06.05.2004
 // added html generation for directories: 5.9.2006
 //
 // This is a part of YaCy, a peer-to-peer based web search engine
 //
 // $LastChangedDate: 2006-04-02 22:40:07 +0200 (So, 02 Apr 2006) $
 // $LastChangedRevision: 1986 $
 // $LastChangedBy: orbiter $
 //
 // LICENSE
 // 
 // This program is free software; you can redistribute it and/or modify
 // it under the terms of the GNU General Public License as published by
 // the Free Software Foundation; either version 2 of the License, or
 // (at your option) any later version.
 //
 // This program is distributed in the hope that it will be useful,
 // but WITHOUT ANY WARRANTY; without even the implied warranty of
 // MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 // GNU General Public License for more details.
 //
 // You should have received a copy of the GNU General Public License
 // along with this program; if not, write to the Free Software
 // Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 
 package de.anomic.net;
 
 import java.io.BufferedOutputStream;
 import java.io.BufferedReader;
 import java.io.ByteArrayOutputStream;
 import java.io.DataInputStream;
 import java.io.DataOutputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.OutputStream;
 import java.io.PrintStream;
 import java.io.RandomAccessFile;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.net.InetAddress;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.net.SocketException;
 import java.text.DateFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Locale;
 import java.util.Map;
 import java.util.Properties;
 import java.util.StringTokenizer;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import net.yacy.kelondro.logging.Log;
 import net.yacy.kelondro.util.Domains;
 
 public class ftpc {
 
     private static final String vDATE = "20040506";
     private static final String logPrefix = "FTPC: ";
 
     private final InputStream in;
     private final PrintStream out;
     private final PrintStream err;
 
     private boolean glob = true; // glob = false -> filenames are taken
     // literally for mget, ..
 
     // transfer type
     private static final char transferType = 'i'; // transfer binary
 
     // block size [1K by default]
     private static final int blockSize = 1024;
 
     // client socket for commands
     private Socket ControlSocket = null;
 
     // socket timeout
     private static final int ControlSocketTimeout = 10000;
 
     // data socket timeout
     private int DataSocketTimeout = 0; // in seconds (default infinite)
 
     // minimal data rate (to calculate timeout with max. filesize)
     private static final int DataSocketRate = 100;// Byte/s
 
     // socket for data transactions
     private ServerSocket DataSocketActive = null;
     private Socket DataSocketPassive = null;
     private boolean DataSocketPassiveMode = true;
 
     // output and input streams for client control connection
     private BufferedReader clientInput = null;
     private DataOutputStream clientOutput = null;
 
     // client prompt
     private String prompt = "ftp [local]>";
 
     String[] cmd;
 
     // session parameters
     File currentLocalPath;
     String account, password, host, remotemessage, remotegreeting, remotesystem;
     int port;
 
     // entry info cache
     private final Map<String, entryInfo> infoCache = new HashMap<String, entryInfo>();
 
     // date-format in LIST (english month names)
     private static final SimpleDateFormat lsDateFormat = new SimpleDateFormat("MMM d y H:m", new Locale("en"));
 
     // TODO: implement RFC 2640 Internationalization
 
     public ftpc() {
         this(System.in, System.out, System.err);
     }
 
     public ftpc(final java.io.InputStream ins, final java.io.PrintStream outs, final java.io.PrintStream errs) {
 
         // try {
         // System.setSecurityManager(new sm());
         // } catch (java.security.AccessControlException e) {
         // }
 
         in = ins;
         out = outs;
         err = errs;
 
         currentLocalPath = new File(System.getProperty("user.dir"));
         try {
             currentLocalPath = new File(currentLocalPath.getCanonicalPath());
         } catch (final IOException e) {
         }
 
         account = null;
         password = null;
         host = null;
         port = -1;
         remotemessage = null;
         remotegreeting = null;
         remotesystem = null;
     }
 
     public void shell(final String server) {
         String command;
 
         java.io.PrintWriter pw = null;
         if (out != null) {
             pw = new java.io.PrintWriter(out);
         }
 
         try {
             final java.io.BufferedReader stdin = new java.io.BufferedReader(new java.io.InputStreamReader(in));
             if (server != null) {
                 exec("open " + server, true);
             }
             while (true) {
 
                 // prompt
                 if (pw != null) {
                     pw.print(prompt);
                     pw.flush();
                 }
 
                 // read a line
                 while ((command = stdin.readLine()) == null) {
                     if (pw != null) {
                         pw.print(prompt);
                         pw.flush();
                     }
                 }
 
                 // execute
                 if (!exec(command, false)) {
                     break;
                 }
 
             }
         } catch (final Exception e) {
             errPrintln("Error - ftp exception: " + e);
             Log.logException(e);
         }
     }
 
     private void errPrintln(final String msg) {
         if (err != null) {
             err.println(logPrefix + "---- " + msg);
         }
     }
 
     private void outPrintln(final String msg) {
         if (out != null) {
             out.println(logPrefix + msg);
         }
     }
 
     public boolean exec(String command, final boolean promptIt) {
         if ((command == null) || (command.length() == 0)) {
             return true;
         }
         int pos;
         String com;
         boolean ret = true;
         while (command.length() > 0) {
             pos = command.indexOf(";");
             if (pos < 0) {
                 pos = command.indexOf("\n");
             }
             if (pos < 0) {
                 com = command;
                 command = "";
             } else {
                 com = command.substring(0, pos);
                 command = command.substring(pos + 1);
             }
             if (promptIt) {
                 outPrintln(prompt + com);
             }
             cmd = line2args(com);
             try {
                 ret = (((Boolean) getClass().getMethod(cmd[0].toUpperCase(), new Class[0]).invoke(this, new Object[0]))
                         .booleanValue());
             } catch (final InvocationTargetException e) {
                 if (e.getMessage() != null) {
                     if (notConnected()) {
                         // the error was probably caused because there is no
                         // connection
                         errPrintln("not connected. no effect.");
                         Log.logException(e);
                     } else {
                         errPrintln("ftp internal exception: target exception " + e);
                     }
                     return ret;
                 }
             } catch (final IllegalAccessException e) {
                 errPrintln("ftp internal exception: wrong access " + e);
                 return ret;
             } catch (final NoSuchMethodException e) {
                 // consider first that the user attempted to execute a java
                 // command from
                 // the current path; either local or remote
                 if (notConnected()) {
                     // try a local exec
                     try {
                         javaexec(cmd);
                     } catch (final Exception ee) {
                         errPrintln("Command '" + cmd[0] + "' not supported. Try 'HELP'.");
                     }
                 } else {
                     // try a remote exec
                     exec("java " + com, false);
                 }
                 return ret;
             }
         }
         return ret;
     }
 
     private String[] line2args(final String line) {
         // parse the command line
         if ((line == null) || (line.length() == 0)) {
             return null;
         }
         // pre-parse
         String line1 = "";
         boolean quoted = false;
         for (int i = 0; i < line.length(); i++) {
             if (quoted) {
                 if (line.charAt(i) == '"') {
                     quoted = false;
                 } else {
                     line1 = line1 + line.charAt(i);
                 }
             } else {
                 if (line.charAt(i) == '"') {
                     quoted = true;
                 } else if (line.charAt(i) == ' ') {
                     line1 = line1 + '|';
                 } else {
                     line1 = line1 + line.charAt(i);
                 }
             }
         }
         return line1.split("\\|");
     }
 
     static class cl extends ClassLoader {
 
         public cl() {
             super();
         }
 
         public synchronized Class<?> loadClass(final String classname, final boolean resolve) throws ClassNotFoundException {
             Class<?> c = findLoadedClass(classname);
             if (c == null) {
                 try {
                     // second try: ask the system
                     c = findSystemClass(classname);
                 } catch (final ClassNotFoundException e) {
                     // third try: load myself
                     final File f = new File(System.getProperty("user.dir"), classname + ".class");
                     final int length = (int) f.length();
                     final byte[] classbytes = new byte[length];
                     try {
                         final DataInputStream in = new DataInputStream(new FileInputStream(f));
                         in.readFully(classbytes);
                         in.close();
                         c = defineClass(classname, classbytes, 0, classbytes.length);
                     } catch (final FileNotFoundException ee) {
                         throw new ClassNotFoundException();
                     } catch (final IOException ee) {
                         throw new ClassNotFoundException();
                     }
                 }
             }
             if (resolve) {
                 resolveClass(c);
             }
             return c;
         }
 
     }
 
     private void javaexec(final String[] inArgs) {
         final String obj = inArgs[0];
         final String[] args = new String[inArgs.length - 1];
 
         // remove the object name from the array of arguments
         System.arraycopy(inArgs, 1, args, 0, inArgs.length - 1);
 
         // Build the argument list for invoke() method.
         final Object[] argList = new Object[1];
         argList[0] = args;
 
         final Properties pr = System.getProperties();
         final String origPath = (String) pr.get("java.class.path");
         try {
 
             // set the user.dir to the actual local path
             pr.put("user.dir", currentLocalPath.toString());
 
             // add the current path to the classpath
             // pr.put("java.class.path", "" + pr.get("user.dir") +
             // pr.get("path.separator") + origPath);
 
             // errPrintln(logPrefix + "System Properties: " + pr.toString());
 
             System.setProperties(pr);
 
             // locate object
             final Class<?> c = (new cl()).loadClass(obj);
             // Class c = this.getClass().getClassLoader().loadClass(obj);
 
             // locate public static main(String[]) method
             Class<?>[] parameterType = new Class[1];
             parameterType[0] = Class.forName("[Ljava.lang.String;");
             Method m = c.getMethod("main", parameterType);
 
             // invoke object.main()
             final Object result = m.invoke(null, argList);
             //parameterType = null;
             m = null;
 
             // handle result
             if (result != null) {
                 outPrintln("returns " + result);
             }
 
             // set the local path to the user.dir (which may have changed)
             currentLocalPath = new File((String) pr.get("user.dir"));
 
         } catch (final ClassNotFoundException e) {
             // errPrintln("cannot find class file " + obj +
             // ".class");
             // class file does not exist, go silently over it to not show
             // everybody that the
             // system attempted to load a class file
             errPrintln("Command '" + obj + "' not supported. Try 'HELP'.");
         } catch (final NoSuchMethodException e) {
             errPrintln("no \"public static main(String args[])\" in " + obj);
         } catch (final InvocationTargetException e) {
             final Throwable orig = e.getTargetException();
             if (orig.getMessage() != null) {
                 errPrintln("Exception from " + obj + ": " + orig.getMessage());
                 Log.logException(orig);
             }
         } catch (final IllegalAccessException e) {
             errPrintln("Illegal access for " + obj + ": class is probably not declared as public");
             Log.logException(e);
         } catch (final NullPointerException e) {
             errPrintln("main(String args[]) is not defined as static for " + obj);
             /*
              * } catch (IOException e) { // class file does not exist, go
              * silently over it to not show everybody that the // system
              * attempted to load a class file errPrintln("Command '" + obj + "'
              * not supported. Try 'HELP'.");
              */
         } catch (final Exception e) {
             errPrintln("Exception caught: " + e);
             Log.logException(e);
         }
 
         // set the classpath to its original definition
         pr.put("java.class.path", origPath);
 
     }
 
     // FTP CLIENT COMMANDS ------------------------------------
 
     public boolean ASCII() {
         if (cmd.length != 1) {
             errPrintln("Syntax: ASCII  (no parameter)");
             return true;
         }
         try {
             literal("TYPE A");
         } catch (final IOException e) {
             errPrintln("Error: ASCII transfer type not supported by server.");
         }
         return true;
     }
 
     public boolean BINARY() {
         if (cmd.length != 1) {
             errPrintln("Syntax: BINARY  (no parameter)");
             return true;
         }
         try {
             literal("TYPE I");
         } catch (final IOException e) {
             errPrintln("Error: BINARY transfer type not supported by server.");
         }
         return true;
     }
 
     public boolean BYE() {
         return QUIT();
     }
 
     public boolean CD() {
         if (cmd.length != 2) {
             errPrintln("Syntax: CD <path>");
             return true;
         }
         if (notConnected()) {
             return LCD();
         }
         try {
             // send cwd command
             send("CWD " + cmd[1]);
 
             final String reply = receive();
             if (isNotPositiveCompletion(reply)) {
                 throw new IOException(reply);
             }
         } catch (final IOException e) {
             errPrintln("Error: change of working directory to path " + cmd[1] + " failed.");
         }
         return true;
     }
 
     public boolean CLOSE() {
         return DISCONNECT();
     }
 
     private void rmForced(final String path) throws IOException {
         // first try: send DELE command (to delete a file)
         send("DELE " + path);
         // read reply
         final String reply1 = receive();
         if (isNotPositiveCompletion(reply1)) {
             // second try: send a RMD command (to delete a directory)
             send("RMD " + path);
             // read reply
             final String reply2 = receive();
             if (isNotPositiveCompletion(reply2)) {
                 // third try: test if this thing is a directory or file and send
                 // appropriate error message
                 if (isFolder(path)) {
                     throw new IOException(reply2);
                 } else {
                     throw new IOException(reply1);
                 }
             }
         }
     }
 
     /**
      * @param path
      * @return date of entry on ftp-server or now if date can not be obtained
      */
     public Date entryDate(final String path) {
         final entryInfo info = fileInfo(path);
         Date date = null;
         if (info != null) {
             date = info.date;
         }
         return date;
     }
 
     public boolean DEL() {
         if (cmd.length != 2) {
             errPrintln("Syntax: DEL <file>");
             return true;
         }
         if (notConnected()) {
             return LDEL();
         }
         try {
             rmForced(cmd[1]);
         } catch (final IOException e) {
             errPrintln("Error: deletion of file " + cmd[1] + " failed.");
         }
         return true;
     }
 
     public boolean RM() {
         return DEL();
     }
 
     public boolean DIR() {
         if (cmd.length > 2) {
             errPrintln("Syntax: DIR [<path>|<file>]");
             return true;
         }
         if (notConnected()) {
             return LDIR();
         }
         try {
             List<String> l;
             if (cmd.length == 2) {
                 l = list(cmd[1], false);
             } else {
                 l = list(".", false);
             }
             printElements(l);
         } catch (final IOException e) {
             errPrintln("Error: remote list not available (1): " + e.getMessage());
         }
         return true;
     }
 
     public boolean DISCONNECT() {
         try {
             quit();
             outPrintln("---- Connection closed.");
         } catch (final IOException e) {
            errPrintln("Connection to server lost.");
         }
         try {
             closeConnection();
         } catch (final IOException e) {
             ControlSocket = null;
             DataSocketActive = null;
             DataSocketPassive = null;
             clientInput = null;
             clientOutput = null;
         }
         prompt = "ftp [local]>";
         return true;
     }
 
     private String quit() throws IOException {
 
         send("QUIT");
 
         // read status reply
         final String reply = receive();
         if (isNotPositiveCompletion(reply)) {
             throw new IOException(reply);
         }
 
         closeConnection();
 
         return reply;
     }
 
     public boolean EXIT() {
         return QUIT();
     }
 
     public boolean GET() {
         if ((cmd.length < 2) || (cmd.length > 3)) {
             errPrintln("Syntax: GET <remote-file> [<local-file>]");
             return true;
         }
         final String remote = cmd[1]; // (new File(cmd[1])).getName();
         final boolean withoutLocalFile = cmd.length == 2;
 
         final String localFilename = (withoutLocalFile) ? remote : cmd[2];
         final File local = absoluteLocalFile(localFilename);
 
         if (local.exists()) {
             errPrintln("Error: local file " + local.toString() + " already exists.\n" + "               File " + remote
                     + " not retrieved. Local file unchanged.");
         } else {
             if (withoutLocalFile) {
                 retrieveFilesRecursively(remote, false);
             } else {
                 try {
                     get(local.getAbsolutePath(), remote);
                 } catch (final IOException e) {
                     errPrintln("Error: retrieving file " + remote + " failed. (" + e.getMessage() + ")");
                 }
             }
         }
         return true;
     }
 
     /**
      * @param localFilename
      * @return
      */
     private File absoluteLocalFile(final String localFilename) {
         File local;
         final File l = new File(localFilename);
         if (l.isAbsolute()) {
             local = l;
         } else {
             local = new File(currentLocalPath, localFilename);
         }
         return local;
     }
 
     private void retrieveFilesRecursively(final String remote, final boolean delete) {
         final File local = absoluteLocalFile(remote);
         try {
             get(local.getAbsolutePath(), remote);
             try {
                 if (delete) {
                     rmForced(remote);
                 }
             } catch (final IOException eee) {
                 errPrintln("Warning: remote file or path " + remote + " cannot be removed.");
             }
         } catch (final IOException e) {
             if (e.getMessage().startsWith("550")) {
                 // maybe it's a "not a plain file" error message", then it can
                 // be a folder
                 // test if this exists (then it should be a folder)
                 if (isFolder(remote)) {
                     // copy the whole directory
                     exec("cd \"" + remote + "\";lmkdir \"" + remote + "\";lcd \"" + remote + "\"", true);
                     // exec("mget *",true);
                     try {
                         for (final String element : list(".", false)) {
                             retrieveFilesRecursively(element, delete);
                         }
                     } catch (final IOException ee) {
                     }
                     exec("cd ..;lcd ..", true);
                     try {
                         if (delete) {
                             rmForced(remote);
                         }
                     } catch (final IOException eee) {
                         errPrintln("Warning: remote file or path " + remote + " cannot be removed.");
                     }
                 } else {
                     errPrintln("Error: remote file or path " + remote + " does not exist.");
                 }
             } else {
                 errPrintln("Error: retrieving file " + remote + " failed. (" + e.getMessage() + ")");
             }
         }
     }
 
     /**
      * checks if path is a folder
      * 
      * @param path
      * @return true if ftp-server changes to path
      */
     public boolean isFolder(final String path) {
         try {
             // /// try to parse LIST output (1 command)
             final entryInfo info = fileInfo(path);
             if (info != null) {
                 return info.isDir;
             }
 
             // /// try to change to folder (4 commands)
             // current folder
             final String currentFolder = pwd();
             // check if we can change to folder
             send("CWD " + path);
             String reply = receive();
             if (isNotPositiveCompletion(reply)) {
                 throw new IOException(reply);
             }
             // check if we actually changed into the folder
             final String changedPath = pwd();
             if (!(changedPath.equals(path) || changedPath.equals(currentFolder
                     + (currentFolder.endsWith("/") ? "" : "/") + path))) {
                 throw new IOException("folder is '" + changedPath + "' should be '" + path + "'");
             }
             // return to last folder
             send("CWD " + currentFolder);
             /*reply =*/ receive();
             return true;
         } catch (final IOException e) {
             return false;
         }
     }
 
     public boolean GLOB() {
         if (cmd.length != 1) {
             errPrintln("Syntax: GLOB  (no parameter)");
             return true;
         }
         glob = !glob;
         outPrintln("---- globbing is now turned " + ((glob) ? "ON" : "OFF"));
         return true;
     }
 
     public boolean HASH() {
         errPrintln("no games implemented");
         return true;
     }
 
     /*
      * private static String[] shift(String args[]) { if ((args == null) ||
      * (args.length == 0)) return args; else { String[] newArgs = new
      * String[args.length-1]; System.arraycopy(args, 1, newArgs, 0,
      * args.length-1); return newArgs; } } public boolean JAR() { //Sun
      * proprietary API may be removed in a future Java release
      * sun.tools.jar.Main.main(shift(cmd)); return true; }
      */
 
     public boolean JJENCODE() {
         if (cmd.length != 2) {
             errPrintln("Syntax: JJENCODE <path>");
             return true;
         }
         final String path = cmd[1];
 
         final File dir = new File(path);
         final File newPath = dir.isAbsolute() ? dir : new File(currentLocalPath, path);
         if (newPath.exists()) {
             if (newPath.isDirectory()) {
                 // exec("cd \"" + remote + "\";lmkdir \"" + remote + "\";lcd \""
                 // + remote + "\"",true);
                 /*
                  * if not exist %1\nul goto :error cd %1 c:\jdk1.2.2\bin\jar
                  * -cfM0 ..\%1.jar *.* cd .. c:\jdk1.2.2\bin\jar -cfM %1.jj
                  * %1.jar del %1.jar
                  */
                 String s = "";
                 final String[] l = newPath.list();
                 for (final String element : l) {
                     s = s + " \"" + element + "\"";
                 }
                 exec("cd \"" + path + "\";jar -cfM0 ../\"" + path + ".jar\"" + s, true);
                 exec("cd ..;jar -cfM \"" + path + ".jj\" \"" + path + ".jar\"", true);
                 exec("rm \"" + path + ".jar\"", true);
             } else {
                 errPrintln("Error: local path " + newPath.toString() + " denotes not to a directory.");
             }
         } else {
             errPrintln("Error: local path " + newPath.toString() + " does not exist.");
         }
         return true;
     }
 
     public boolean JJDECODE() {
         if (cmd.length != 2) {
             errPrintln("Syntax: JJENCODE <path>");
             return true;
         }
         final String path = cmd[1];
         final File dir = new File(path);
         final File newPath = dir.isAbsolute() ? dir : new File(currentLocalPath, path);
         final File newFolder = new File(newPath.toString() + ".dir");
         if (newPath.exists()) {
             if (!newPath.isDirectory()) {
                 if (!newFolder.mkdir()) {
                     /*
                      * if not exist %1.jj goto :error mkdir %1.dir copy %1.jj
                      * %1.dir\ > %1.dummy && del %1.dummy cd %1.dir
                      * c:\jdk1.2.2\bin\jar -xf %1.jj del %1.jj
                      * c:\jdk1.2.2\bin\jar -xf %1.jar del %1.jar cd ..
                      */
                     exec("mkdir \"" + path + ".dir\"", true);
 
                 } else {
                     errPrintln("Error: target dir " + newFolder.toString() + " cannot be created");
                 }
             } else {
                 errPrintln("Error: local path " + newPath.toString() + " must denote to jar/jar file");
             }
         } else {
             errPrintln("Error: local path " + newPath.toString() + " does not exist.");
         }
         return true;
     }
 
     private static String[] argList2StringArray(final String argList) {
         return argList.split("\\s");
     }
 
     public boolean JOIN(String[] args) {
 
         // make sure the specified dest file does not exist
         final String dest_name = args[1];
         final File dest_file = new File(dest_name);
         if (dest_file.exists()) {
             errPrintln(logPrefix + "join: destination file " + dest_name + " already exists");
             return true;
         }
 
         // prepare or search file names of the input files to be joined
         String source_name;
         File source_file;
         int pc = -1;
         // create new string array with file names
         // scan first for the files
         pc = 0;
         source_name = dest_name + ".000";
         String argString = "";
         source_file = new File(source_name);
         while ((source_file.exists()) && (source_file.isFile()) && (source_file.canRead())) {
             argString = argString + " " + source_name;
             pc++;
             source_name = dest_name + (pc < 10 ? ".00" + pc : (pc < 100 ? ".0" + pc : "." + pc));
             source_file = new File(source_name);
         }
         args = argList2StringArray(argString.substring(1));
 
         // do the join
         FileOutputStream dest = null;
         FileInputStream source = null;
         byte[] buffer;
         int bytes_read = 0;
 
         try {
             // open output file
             dest = new FileOutputStream(dest_file);
             buffer = new byte[1024];
 
             // append all source files
             for (pc = 0; pc < args.length; pc++) {
                 // open the source file
                 source_name = args[pc];
                 source_file = new File(source_name);
                 source = new FileInputStream(source_file);
 
                 // start with the copy of one source file
                 while (true) {
                     bytes_read = source.read(buffer);
                     if (bytes_read == -1) {
                         break;
                     }
                     dest.write(buffer, 0, bytes_read);
                 }
 
                 // copy finished. close source file
                 try {
                     source.close();
                 } catch (final IOException e) {
                 }
             }
             // close the output file
             try {
                 dest.close();
             } catch (final IOException e) {
             }
 
             // if we come to this point then everything went fine
             // if the user wanted to delete the source it is save to do so now
             for (pc = 0; pc < args.length; pc++) {
                 try {
                     if (!(new File(args[pc])).delete()) {
                         errPrintln(logPrefix + "join: unable to delete file " + args[pc]);
                     }
                 } catch (final SecurityException e) {
                     errPrintln(logPrefix + "join: no permission to delete file " + args[pc]);
                 }
             }
         } catch (final FileNotFoundException e) {
         } catch (final IOException e) {
         }
 
         // clean up
         finally {
             // close any opened streams
             if (dest != null) {
                 try {
                     dest.close();
                 } catch (final IOException e) {
                 }
             }
             if (source != null) {
                 try {
                     source.close();
                 } catch (final IOException e) {
                 }
             }
 
             // print appropriate message
             errPrintln(logPrefix + "join created output from " + args.length + " source files");
         }
         return true;
     }
 
     public boolean COPY(final String[] args) {
         final File dest_file = new File(args[2]);
         if (dest_file.exists()) {
             errPrintln(logPrefix + "copy: destination file " + args[2] + " already exists");
             return true;
         }
         int bytes_read = 0;
         FileOutputStream dest = null;
         FileInputStream source = null;
         try {
             // open output file
             dest = new FileOutputStream(dest_file);
             final byte[] buffer = new byte[1024];
 
             // open the source file
             final File source_file = new File(args[1]);
             source = new FileInputStream(source_file);
 
             // start with the copy of one source file
             while (true) {
                 bytes_read = source.read(buffer);
                 if (bytes_read == -1) {
                     break;
                 }
                 dest.write(buffer, 0, bytes_read);
             }
 
         } catch (final FileNotFoundException e) {
         } catch (final IOException e) {
         } finally {
             // copy finished. close source file
             if (source != null) {
                 try {
                     source.close();
                 } catch (final IOException e) {
                 }
             }
 
             // close the output file
             if (dest != null) {
                 try {
                     dest.close();
                 } catch (final IOException e) {
                 }
             }
         }
         return true;
     }
 
     public boolean JAVA() {
         String s = "JAVA";
         for (int i = 1; i < cmd.length; i++) {
             s = s + " " + cmd[i];
         }
         try {
             send(s);
             /* String reply = */receive();
         } catch (final IOException e) {
         }
         return true;
     }
 
     public boolean LCD() {
         if (cmd.length != 2) {
             errPrintln("Syntax: LCD <path>");
             return true;
         }
         final String path = cmd[1];
         final File dir = new File(path);
         File newPath = dir.isAbsolute() ? dir : new File(currentLocalPath, path);
         try {
             newPath = new File(newPath.getCanonicalPath());
         } catch (final IOException e) {
         }
         if (newPath.exists()) {
             if (newPath.isDirectory()) {
                 currentLocalPath = newPath;
                 outPrintln("---- New local path: " + currentLocalPath.toString());
             } else {
                 errPrintln("Error: local path " + newPath.toString() + " denotes not a directory.");
             }
         } else {
             errPrintln("Error: local path " + newPath.toString() + " does not exist.");
         }
         return true;
     }
 
     public boolean LDEL() {
         return LRM();
     }
 
     public boolean LDIR() {
         if (cmd.length != 1) {
             errPrintln("Syntax: LDIR  (no parameter)");
             return true;
         }
         final String[] name = currentLocalPath.list();
         for (int n = 0; n < name.length; ++n) {
             outPrintln(ls(new File(currentLocalPath, name[n])));
         }
         return true;
     }
 
     /**
      * parse LIST of file
      * 
      * @param path
      *                on ftp-server
      * @return null if info cannot be determined or error occures
      */
     public entryInfo fileInfo(final String path) {
         if (infoCache.containsKey(path)) {
             return infoCache.get(path);
         }
         try {
             /*
              * RFC959 page 33f: If the argument is a pathname, the command is
              * analogous to the "list" command except that data shall be
              * transferred over the control connection.
              */
             send("STAT " +  path);
 
             final String reply = receive();
             if (isNotPositiveCompletion(reply)) {
                 throw new IOException(reply);
             }
 
             // check if reply is correct multi-line reply
             final String[] lines = reply.split("\\r\\n");
             if (lines.length < 3) {
                 throw new IOException(reply);
             }
             final int startCode = getStatusCode(lines[0]);
             final int endCode = getStatusCode(lines[lines.length - 1]);
             if (startCode != endCode) {
                 throw new IOException(reply);
             }
 
             // first line which gives a result is taken (should be only one)
             entryInfo info = null;
             final int endFor = lines.length - 1;
             for (int i = 1; i < endFor; i++) {
                 info = parseListData(lines[i]);
                 if (info != null) {
                     infoCache.put(path, info);
                     break;
                 }
             }
             return info;
         } catch (final IOException e) {
             return null;
         }
     }
 
     /**
      * returns status of reply
      * 
      * 1 Positive Preliminary reply 2 Positive Completion reply 3 Positive
      * Intermediate reply 4 Transient Negative Completion reply 5 Permanent
      * Negative Completion reply
      * 
      * @param reply
      * @return first digit of the reply code
      */
     private int getStatus(final String reply) {
         return Integer.parseInt(reply.substring(0, 1));
     }
 
     /**
      * gives reply code
      * 
      * @param reply
      * @return
      */
     private int getStatusCode(final String reply) {
         return Integer.parseInt(reply.substring(0, 3));
     }
 
     /**
      * checks if status code is in group 2 ("2xx message")
      * 
      * @param reply
      * @return
      */
     private boolean isNotPositiveCompletion(final String reply) {
         return getStatus(reply) != 2;
     }
 
     /**
      * parses output of LIST from ftp-server currently UNIX ls-style only, ie:
      * -rw-r--r-- 1 root other 531 Jan 29 03:26 README dr-xr-xr-x 2 root 512 Apr
      * 8 1994 etc
      * 
      * @param line
      * @return null if not parseable
      */
     private static entryInfo parseListData(final String line) {
         final Pattern lsStyle = Pattern
                 .compile("^([-\\w]{10}).\\s*\\d+\\s+[-\\w]+\\s+[-\\w]+\\s+(\\d+)\\s+(\\w{3})\\s+(\\d+)\\s+(\\d+:?\\d*)\\s+(.*)$");
         // groups: 1: rights, 2: size, 3: month, 4: day, 5: time or year, 6:
         // name
         final Matcher tokens = lsStyle.matcher(line);
         if (tokens.matches()) {
             final boolean isDir = tokens.group(1).startsWith("d");
             int size = -1;
             try {
                 size = Integer.parseInt(tokens.group(2));
             } catch (final NumberFormatException e) {
                 Log.logWarning("FTPC", "Error: not a number in list-entry: " + e.getMessage());
                 return null;
             }
             String time;
             String year;
             if (tokens.group(5).contains(":")) {
                 time = tokens.group(5);
                 year = String.valueOf(Calendar.getInstance().get(Calendar.YEAR)); // current
                 // year
             } else {
                 time = "00:00";
                 year = tokens.group(5);
             }
             // construct date string
             // this has to be done, because the list-entry may have multiple
             // spaces, tabs or so
             Date date;
             final String dateString = tokens.group(3) + " " + tokens.group(4) + " " + year + " " + time;
             try {
             	synchronized(lsDateFormat) {
             		date = lsDateFormat.parse(dateString);
             	}
             } catch (final ParseException e) {
                 Log.logWarning("FTPC", "---- Error: not ls date-format '" + dateString + "': " + e.getMessage());
                 date = new Date();
             }
             return new entryInfo(isDir, size, date, tokens.group(6));
         }
         return null;
     }
 
     /**
      * parameter class
      * 
      * @author danielr
      * @since 2008-03-13 r4558
      */
     private static class entryInfo {
         /**
          * is this a directory?
          */
         public final boolean isDir;
         /**
          * size in bytes
          */
         public final int size;
         /**
          * date of file
          */
         public final Date date;
         /**
          * name of entry
          */
         public final String name;
 
         /**
          * constructor
          * 
          * @param isDir
          * @param size
          *                bytes
          * @param date
          * @param name
          */
         public entryInfo(final boolean isDir, final int size, final Date date, final String name) {
             this.isDir = isDir;
             this.size = size;
             this.date = date;
             this.name = name;
         }
 
         /*
          * (non-Javadoc)
          * 
          * @see java.lang.Object#toString()
          */
         public String toString() {
             final StringBuilder info = new StringBuilder(100);
             info.append(name);
             info.append(" (isDir=");
             info.append(isDir);
             info.append(", size=");
             info.append(size);
             info.append(", ");
             info.append(date);
             info.append(")");
             return info.toString();
         }
     }
 
     private String ls(final File inode) {
         if ((inode == null) || (!inode.exists())) {
             return "";
         }
         String s = "";
         if (inode.isDirectory()) {
             s = s + "d";
         } else if (inode.isFile()) {
             s = s + "-";
         } else {
             s = s + "?";
         }
         if (inode.canRead()) {
             s = s + "r";
         } else {
             s = s + "-";
         }
         if (inode.canWrite()) {
             s = s + "w";
         } else {
             s = s + "-";
         }
         s = s + " " + lenformatted(Long.toString(inode.length()), 9);
         final DateFormat df = DateFormat.getDateTimeInstance();
         s = s + " " + df.format(new Date(inode.lastModified()));
         s = s + " " + inode.getName();
         if (inode.isDirectory()) {
             s = s + "/";
         }
         return s;
     }
 
     private String lenformatted(String s, int l) {
         l = l - s.length();
         while (l > 0) {
             s = " " + s;
             l--;
         }
         return s;
     }
 
     public boolean LITERAL() {
         if (cmd.length == 1) {
             errPrintln("Syntax: LITERAL <ftp-command> [<command-argument>]   (see RFC959)");
             return true;
         }
         String s = "";
         for (int i = 1; i < cmd.length; i++) {
             s = s + " " + cmd[i];
         }
         try {
             literal(s.substring(1));
         } catch (final IOException e) {
             errPrintln("Error: Syntax of FTP-command wrong. See RFC959 for details.");
         }
         return true;
     }
 
     public boolean LLS() {
         return LDIR();
     }
 
     public boolean LMD() {
         return LMKDIR();
     }
 
     public boolean LMKDIR() {
         if (cmd.length != 2) {
             errPrintln("Syntax: LMKDIR <folder-name>");
             return true;
         }
         final File f = new File(currentLocalPath, cmd[1]);
         if (f.exists()) {
             errPrintln("Error: local file/folder " + cmd[1] + " already exists");
         } else {
             if (!f.mkdir()) {
                 errPrintln("Error: creation of local folder " + cmd[1] + " failed");
             }
         }
         return true;
     }
 
     public boolean LMV() {
         if (cmd.length != 3) {
             errPrintln("Syntax: LMV <from> <to>");
             return true;
         }
         final File from = new File(cmd[1]);
         final File to = new File(cmd[2]);
         if (!to.exists()) {
             if (from.renameTo(to)) {
                 outPrintln("---- \"" + from.toString() + "\" renamed to \"" + to.toString() + "\"");
             } else {
                 errPrintln(logPrefix + "rename failed");
             }
         } else {
             errPrintln(logPrefix + "\"" + to.toString() + "\" already exists");
         }
         return true;
     }
 
     public boolean LPWD() {
         if (cmd.length != 1) {
             errPrintln("Syntax: LPWD  (no parameter)");
             return true;
         }
         outPrintln("---- Local path: " + currentLocalPath.toString());
         return true;
     }
 
     public boolean LRD() {
         return LMKDIR();
     }
 
     public boolean LRMDIR() {
         if (cmd.length != 2) {
             errPrintln("Syntax: LRMDIR <folder-name>");
             return true;
         }
         final File f = new File(currentLocalPath, cmd[1]);
         if (!f.exists()) {
             errPrintln("Error: local folder " + cmd[1] + " does not exist");
         } else {
             if (!f.delete()) {
                 errPrintln("Error: deletion of local folder " + cmd[1] + " failed");
             }
         }
         return true;
     }
 
     public boolean LRM() {
         if (cmd.length != 2) {
             errPrintln("Syntax: LRM <file-name>");
             return true;
         }
         final File f = new File(currentLocalPath, cmd[1]);
         if (!f.exists()) {
             errPrintln("Error: local file " + cmd[1] + " does not exist");
         } else {
             if (!f.delete()) {
                 errPrintln("Error: deletion of file " + cmd[1] + " failed");
             }
         }
         return true;
     }
 
     public boolean LS() {
         if (cmd.length > 2) {
             errPrintln("Syntax: LS [<path>|<file>]");
             return true;
         }
         if (notConnected()) {
             return LLS();
         }
         try {
             List<String> l;
             if (cmd.length == 2) {
                 l = list(cmd[1], true);
             } else {
                 l = list(".", true);
             }
             printElements(l);
         } catch (final IOException e) {
             errPrintln("Error: remote list not available (2): " + e.getMessage());
         }
         return true;
     }
 
     /**
      * @param list
      */
     private void printElements(final List<String> list) {
         outPrintln("---- v---v---v---v---v---v---v---v---v---v---v---v---v---v---v---v---v---v---v");
         for (final String element : list) {
             outPrintln(element);
         }
         outPrintln("---- ^---^---^---^---^---^---^---^---^---^---^---^---^---^---^---^---^---^---^");
     }
 
     private List<String> list(final String path, final boolean extended) throws IOException {
         createDataSocket();
 
         // send command to the control port
         if (extended) {
             send("LIST " + path);
         } else {
             send("NLST " + path);
         }
 
         // read status of the command from the control port
         String reply = receive();
 
         // get status code
         final int status = getStatus(reply);
         if (status != 1) {
             throw new IOException(reply);
         }
         
         // starting data transaction
         final Socket data = getDataSocket();
         final BufferedReader ClientStream = new BufferedReader(new InputStreamReader(data.getInputStream()));
 
         // read file system data
         String line;
         final ArrayList<String> files = new ArrayList<String>();
         while ((line = ClientStream.readLine()) != null) {
             if (!line.startsWith("total ")) {
                 files.add(line);
             }
         }
 
         // after stream is empty we should get control completion echo
         /*reply =*/ receive();
 
         // boolean success = !isNotPositiveCompletion(reply);
 
         // shutdown connection
         ClientStream.close(); // Closing the returned InputStream will
         closeDataSocket(); // close the associated socket.
 
         // if (!success) throw new IOException(reply);
 
         files.trimToSize();
         return files;
     }
 
     public boolean MDIR() {
         return MKDIR();
     }
 
     public boolean MKDIR() {
         if (cmd.length != 2) {
             errPrintln("Syntax: MKDIR <folder-name>");
             return true;
         }
         if (notConnected()) {
             return LMKDIR();
         }
         try {
             // send mkdir command
             send("MKD " + cmd[1]);
             // read reply
             final String reply = receive();
             if (isNotPositiveCompletion(reply)) {
                 throw new IOException(reply);
             }
         } catch (final IOException e) {
             errPrintln("Error: creation of folder " + cmd[1] + " failed");
         }
         return true;
     }
 
     public boolean MGET() {
         if (cmd.length != 2) {
             errPrintln("Syntax: MGET <file-pattern>");
             return true;
         }
         try {
             mget(cmd[1], false);
         } catch (final IOException e) {
             errPrintln("Error: mget failed (" + e.getMessage() + ")");
         }
         return true;
     }
 
     private void mget(final String pattern, final boolean remove) throws IOException {
         final List<String> l = list(".", false);
         File local;
         for (final String remote : l) {
             if (matches(remote, pattern)) {
                 local = new File(currentLocalPath, remote);
                 if (local.exists()) {
                     errPrintln("Warning: local file " + local.toString() + " overwritten.");
                     if(!local.delete())
                         errPrintln("Warning: local file " + local.toString() + " could not be deleted.");
                 }
                 retrieveFilesRecursively(remote, remove);
             }
         }
     }
 
     public boolean MOVEDOWN() {
         if (cmd.length != 2) {
             errPrintln("Syntax: MOVEDOWN <file-pattern>");
             return true;
         }
         try {
             mget(cmd[1], true);
         } catch (final IOException e) {
             errPrintln("Error: movedown failed (" + e.getMessage() + ")");
         }
         return true;
     }
 
     /**
      * public boolean MOVEUP() { }
      * 
      * @return
      */
     public boolean MV() {
         if (cmd.length != 3) {
             errPrintln("Syntax: MV <from> <to>");
             return true;
         }
         if (notConnected()) {
             return LMV();
         }
         try {
             // send rename commands
             send("RNFR " + cmd[1]);
             // read reply
             String reply = receive();
             if (isNotPositiveCompletion(reply)) {
                 throw new IOException(reply);
             }
             send("RNTO " + cmd[2]);
             // read reply
             reply = receive();
             if (isNotPositiveCompletion(reply)) {
                 throw new IOException(reply);
             }
         } catch (final IOException e) {
             errPrintln("Error: rename of " + cmd[1] + " to " + cmd[2] + " failed.");
         }
         return true;
     }
 
     public boolean NOOP() {
         if (cmd.length != 1) {
             errPrintln("Syntax: NOOP  (no parameter)");
             return true;
         }
         try {
             literal("NOOP");
         } catch (final IOException e) {
             errPrintln("Error: server does not know how to do nothing");
         }
         return true;
     }
 
     public boolean OPEN() {
         if ((cmd.length < 2) || (cmd.length > 3)) {
             errPrintln("Syntax: OPEN <host> [<port>]");
             return true;
         }
         int port = 21;
         if (cmd.length == 3) {
             try {
                 port = java.lang.Integer.parseInt(cmd[2]);
             } catch (final NumberFormatException e) {
                 port = 21;
             }
         }
         if (cmd[1].indexOf(":") > 0) {
             // port is given
             port = java.lang.Integer.parseInt(cmd[1].substring(cmd[1].indexOf(":") + 1));
             cmd[1] = cmd[1].substring(0, cmd[1].indexOf(":"));
         }
         try {
             open(cmd[1], port);
             outPrintln("---- Connection to " + cmd[1] + " established.");
             prompt = "ftp [" + cmd[1] + "]>";
         } catch (final IOException e) {
             errPrintln("Error: connecting " + cmd[1] + " on port " + port + " failed: " + e.getMessage());
         }
         return true;
     }
 
     private void open(final String host, final int port) throws IOException {
         if (ControlSocket != null) {
             exec("close", false); // close any existing connections first
         }
 
         try {
             ControlSocket = new Socket(host, port);
             ControlSocket.setSoTimeout(getTimeout());
             ControlSocket.setKeepAlive(true);
             ControlSocket.setTcpNoDelay(true); // no accumulation until buffer is full
             ControlSocket.setSoLinger(false, getTimeout()); // !wait for all data being written on close()
             ControlSocket.setSendBufferSize(1440); // read http://www.cisco.com/warp/public/105/38.shtml
             ControlSocket.setReceiveBufferSize(1440); // read http://www.cisco.com/warp/public/105/38.shtml
             clientInput = new BufferedReader(new InputStreamReader(ControlSocket.getInputStream()));
             clientOutput = new DataOutputStream(new BufferedOutputStream(ControlSocket.getOutputStream()));
 
             // read and return server message
             this.host = host;
             this.port = port;
             remotemessage = receive();
             if ((remotemessage != null) && (remotemessage.length() > 3)) {
                 remotemessage = remotemessage.substring(4);
             }
         } catch (final IOException e) {
             // if a connection was opened, it should not be used
             closeConnection();
             throw new IOException(e.getMessage());
         }
     }
 
     /**
      * @return
      */
     public boolean notConnected() {
         return ControlSocket == null;
     }
 
     /**
      * close all sockets
      * 
      * @throws IOException
      */
     private void closeConnection() throws IOException {
         // cleanup
         if (ControlSocket != null) {
             clientOutput.close();
             clientInput.close();
             ControlSocket.close();
             ControlSocket = null;
         }
 
         if (DataSocketActive != null) {
             DataSocketActive.close();
             DataSocketActive = null;
         }
         if (DataSocketPassive != null) {
             DataSocketPassive.close();
             DataSocketPassive = null; // "Once a socket has been closed, it is
             // not available for further networking
             // use"
         }
     }
 
     public boolean PROMPT() {
         errPrintln("prompt is always off");
         return true;
     }
 
     public boolean PUT() {
         if ((cmd.length < 2) || (cmd.length > 3)) {
             errPrintln("Syntax: PUT <local-file> [<remote-file>]");
             return true;
         }
         final File local = new File(currentLocalPath, cmd[1]);
         final String remote = (cmd.length == 2) ? local.getName() : cmd[2];
         if (!local.exists()) {
             errPrintln("Error: local file " + local.toString() + " does not exist.");
             errPrintln(logPrefix + "            Remote file " + remote + " not overwritten.");
         } else {
             try {
                 put(local.getAbsolutePath(), remote);
             } catch (final IOException e) {
                 errPrintln("Error: transmitting file " + local.toString() + " failed.");
             }
         }
         return true;
     }
 
     public boolean PWD() {
         if (cmd.length > 1) {
             errPrintln("Syntax: PWD  (no parameter)");
             return true;
         }
         if (notConnected()) {
             return LPWD();
         }
         try {
             outPrintln("---- Current remote path is: " + pwd());
         } catch (final IOException e) {
             errPrintln("Error: remote path not available");
         }
         return true;
     }
 
     private String pwd() throws IOException {
         // send pwd command
         send("PWD");
 
         // read current directory
         final String reply = receive();
         if (isNotPositiveCompletion(reply)) {
             throw new IOException(reply);
         }
 
         // parse directory name out of the reply
         return reply.substring(5, reply.lastIndexOf('"'));
     }
 
     public boolean REMOTEHELP() {
         if (cmd.length != 1) {
             errPrintln("Syntax: REMOTEHELP  (no parameter)");
             return true;
         }
         try {
             literal("HELP");
         } catch (final IOException e) {
             errPrintln("Error: remote help not supported by server.");
         }
         return true;
     }
 
     public boolean RMDIR() {
         if (cmd.length != 2) {
             errPrintln("Syntax: RMDIR <folder-name>");
             return true;
         }
         if (notConnected()) {
             return LRMDIR();
         }
         try {
             rmForced(cmd[1]);
         } catch (final IOException e) {
             errPrintln("Error: deletion of folder " + cmd[1] + " failed.");
         }
         return true;
     }
 
     public boolean QUIT() {
         if (!notConnected()) {
             exec("close", false);
         }
         return false;
     }
 
     public boolean RECV() {
         return GET();
     }
 
     /**
      * size of file on ftp-server (maybe size of directory-entry is possible)
      * 
      * @param path
      * @return size in bytes or -1 if size cannot be determinied
      */
     public int fileSize(final String path) {
         int size = -1;
         try {
             // extended FTP
             size = size(path);
         } catch (final IOException e) {
             // else with LIST-data
             final entryInfo info = fileInfo(path);
             if (info != null) {
                 size = info.size;
             }
         }
         return size;
     }
 
     public int size(final String path) throws IOException {
         // get the size of a file. If the given path targets to a directory, a
         // -1 is returned
         // this function is not supported by standard rfc 959. The method is
         // descibed in RFC 3659 Extensions to FTP
         // if the method is not supported by the target server, this throws an
         // IOException with the
         // server response as exception message
 
         // send command to the control port
         send("SIZE " + path);
 
         // read status of the command from the control port
         final String reply = receive();
 
         if (getStatusCode(reply) != 213) {
             throw new IOException(reply);
         }
         
         try {
             return Integer.parseInt(reply.substring(4));
         } catch (final NumberFormatException e) {
             throw new IOException(reply);
         }
     }
 
     public boolean USER() {
         if (cmd.length != 3) {
             errPrintln("Syntax: USER <user-name> <password>");
             return true;
         }
         try {
             login(cmd[1], cmd[2]);
             outPrintln("---- Granted access for user " + cmd[1] + ".");
         } catch (final IOException e) {
             errPrintln("Error: authorization of user " + cmd[1] + " failed: " + e.getMessage());
         }
         return true;
     }
 
     public boolean APPEND() {
         errPrintln("not yet supported");
         return true;
     }
 
     public boolean HELP() {
         outPrintln("---- ftp HELP ----");
         outPrintln("");
         outPrintln("This ftp client shell can act as command shell for the local host as well for the");
         outPrintln("remote host. Commands that point to the local host are preceded by 'L'.");
         outPrintln("");
         outPrintln("Supported Commands:");
         outPrintln("ASCII");
         outPrintln("   switch remote server to ASCII transfer mode");
         outPrintln("BINARY");
         outPrintln("   switch remote server to BINARY transfer mode");
         outPrintln("BYE");
         outPrintln("   quit the command shell (same as EXIT)");
         outPrintln("CD <path>");
         outPrintln("   change remote path");
         outPrintln("CLOSE");
         outPrintln("   close connection to remote host (same as DISCONNECT)");
         outPrintln("DEL <file>");
         outPrintln("   delete file on remote server (same as RM)");
         outPrintln("RM <file>");
         outPrintln("   remove file from remote server (same as DEL)");
         outPrintln("DIR [<path>|<file>] ");
         outPrintln("   print file information for remote directory or file");
         outPrintln("DISCONNECT");
         outPrintln("   disconnect from remote server (same as CLOSE)");
         outPrintln("EXIT");
         outPrintln("   quit the command shell (same as BYE)");
         outPrintln("GET <remote-file> [<local-file>]");
         outPrintln("   load <remote-file> from remote server and store it locally,");
         outPrintln("   optionally to <local-file>. if the <remote-file> is a directory,");
         outPrintln("   then all files in that directory are retrieved,");
         outPrintln("   including recursively all subdirectories.");
         outPrintln("GLOB");
         outPrintln("   toggles globbing: matching with wild cards or not");
         outPrintln("COPY");
         outPrintln("   copies local files");
         outPrintln("LCD <path>");
         outPrintln("   local directory change");
         outPrintln("LDEL <file>");
         outPrintln("   local file delete");
         outPrintln("LDIR");
         outPrintln("   shows local directory content");
         outPrintln("LITERAL <ftp-command> [<command-argument>]");
         outPrintln("   Sends FTP commands as documented in RFC959");
         outPrintln("LLS");
         outPrintln("   as LDIR");
         outPrintln("LMD");
         outPrintln("   as LMKDIR");
         outPrintln("LMV <local-from> <local-to>");
         outPrintln("   copies local files");
         outPrintln("LPWD");
         outPrintln("   prints local path");
         outPrintln("LRD");
         outPrintln("   as LMKDIR");
         outPrintln("LRMD <folder-name>");
         outPrintln("   deletes local directory <folder-name>");
         outPrintln("LRM <file-name>");
         outPrintln("   deletes local file <file-name>");
         outPrintln("LS [<path>|<file>]");
         outPrintln("   prints list of remote directory <path> or information of file <file>");
         outPrintln("MDIR");
         outPrintln("   as MKDIR");
         outPrintln("MGET <file-pattern>");
         outPrintln("   copies files from remote server that fits into the");
         outPrintln("   pattern <file-pattern> to the local path.");
         outPrintln("MOVEDOWN <file-pattern>");
         outPrintln("   copies files from remote server as with MGET");
         outPrintln("   and deletes them afterwards on the remote server");
         outPrintln("MV <from> <to>");
         outPrintln("   moves or renames files on the local host");
         outPrintln("NOOP");
         outPrintln("   sends the NOOP command to the remote server (which does nothing)");
         outPrintln("   This command is usually used to measure the speed of the remote server.");
         outPrintln("OPEN <host[':'port]> [<port>]");
         outPrintln("   connects the ftp shell to the remote server <host>. Optionally,");
         outPrintln("   a port number can be given, the default port number is 21.");
         outPrintln("   Example: OPEN localhost:2121 or OPEN 192.168.0.1 2121");
         outPrintln("PROMPT");
         outPrintln("   compatibility command, that usually toggles beween prompting on or off.");
         outPrintln("   ftp has prompting switched off by default and cannot switched on.");
         outPrintln("PUT <local-file> [<remote-file>]");
         outPrintln("   copies the <local-file> to the remote server to the current remote path or");
         outPrintln("   optionally to the given <remote-file> path.");
         outPrintln("PWD");
         outPrintln("   prints current path on the remote server.");
         outPrintln("REMOTEHELP");
         outPrintln("   asks the remote server to print the help text of the remote server");
         outPrintln("RMDIR <folder-name>");
         outPrintln("   removes the directory <folder-name> on the remote server");
         outPrintln("QUIT");
         outPrintln("   exits the ftp application");
         outPrintln("RECV");
         outPrintln("   as GET");
         outPrintln("USER <user-name> <password>");
         outPrintln("   logs into the remote server with the user <user-name>");
         outPrintln("   and the password <password>");
         outPrintln("");
         outPrintln("");
         outPrintln("EXAMPLE:");
         outPrintln("a standard sessions looks like this");
         outPrintln(">open 192.168.0.1:2121");
         outPrintln(">user anonymous bob");
         outPrintln(">pwd");
         outPrintln(">ls");
         outPrintln(">.....");
         outPrintln("");
         outPrintln("");
         return true;
     }
 
     public boolean QUOTE() {
         errPrintln("not yet supported");
         return true;
     }
 
     public boolean BELL() {
         errPrintln("not yet supported");
         return true;
     }
 
     public boolean MDELETE() {
         errPrintln("not yet supported");
         return true;
     }
 
     public boolean SEND() {
         errPrintln("not yet supported");
         return true;
     }
 
     public boolean DEBUG() {
         errPrintln("not yet supported");
         return true;
     }
 
     public boolean MLS() {
         errPrintln("not yet supported");
         return true;
     }
 
     public boolean TRACE() {
         errPrintln("not yet supported");
         return true;
     }
 
     public boolean MPUT() {
         errPrintln("not yet supported");
         return true;
     }
 
     public boolean TYPE() {
         errPrintln("not yet supported");
         return true;
     }
 
     public boolean CREATE() {
         errPrintln("not yet supported");
         return true;
     }
 
     // helper functions
 
     private boolean matches(final String name, final String pattern) {
         // checks whether the string name matches with the pattern
         // the pattern may contain characters '*' as wildcard for several
         // characters (also none) and '?' to match exactly one characters
         // outPrintln("MATCH " + name + " " + pattern);
         if (!glob) {
             return name.equals(pattern);
         }
         if (pattern.equals("*")) {
             return true;
         }
         if (pattern.length() > 0 && pattern.charAt(0) == '*' && pattern.endsWith("*")) {
             return // avoid recursion deadlock
             ((matches(name, pattern.substring(1))) || (matches(name, pattern.substring(0, pattern.length() - 1))));
         }
         try {
             int i = pattern.indexOf("?");
             if (i >= 0) {
                 if (!(matches(name.substring(0, i), pattern.substring(0, i)))) {
                     return false;
                 }
                 return (matches(name.substring(i + 1), pattern.substring(i + 1)));
             }
             i = pattern.indexOf("*");
             if (i >= 0) {
                 if (!(name.substring(0, i).equals(pattern.substring(0, i)))) {
                     return false;
                 }
                 if (pattern.length() == i + 1) {
                     return true; // pattern would be '*'
                 }
                 return (matches(reverse(name.substring(i)), reverse(pattern.substring(i + 1)) + "*"));
             }
             return name.equals(pattern);
         } catch (final java.lang.StringIndexOutOfBoundsException e) {
             // this is normal. it's a lazy implementation
             return false;
         }
     }
 
     private String reverse(final String s) {
         if (s.length() < 2) {
             return s;
         }
         return reverse(s.substring(1)) + s.charAt(0);
     }
 
     // protocoll socket commands
 
     private void send(final String buf) throws IOException {
         clientOutput.writeBytes(buf);
         clientOutput.write('\r');
         clientOutput.write('\n');
         clientOutput.flush();
         if (buf.startsWith("PASS")) {
             outPrintln("> PASS ********");
         } else {
             outPrintln("> " + buf);
         }
     }
 
     private String receive() throws IOException {
         // last reply starts with 3 digit number followed by space
         String reply;
 
         while (true) {
             reply = clientInput.readLine();
 
             // sanity check
             if (reply == null) {
                 throw new IOException("Server has presumably shut down the connection.");
             }
 
             outPrintln("< " + reply);
             // serverResponse.addElement(reply);
 
             if (reply.length() >= 4 && Character.isDigit(reply.charAt(0)) && Character.isDigit(reply.charAt(1))
                     && Character.isDigit(reply.charAt(2)) && (reply.charAt(3) == ' ')) {
                 break; // end of reply
             }
         }
         // return last reply line
         return reply;
     }
 
     private void sendTransferType(final char type) throws IOException {
         send("TYPE " + type);
 
         final String reply = receive();
         if (isNotPositiveCompletion(reply)) {
             throw new IOException(reply);
         }
     }
 
     /**
      * @return
      * @throws IOException
      */
     private Socket getDataSocket() throws IOException {
         Socket data;
         if (isPassive()) {
             if (DataSocketPassive == null) {
                 createDataSocket();
             }
             data = DataSocketPassive;
         } else {
             if (DataSocketActive == null) {
                 createDataSocket();
             }
             data = DataSocketActive.accept();
         }
         return data;
     }
 
     /**
      * create data channel
      * 
      * @throws IOException
      */
     private void createDataSocket() throws IOException {
         if (isPassive()) {
             createPassiveDataPort();
         } else {
             createActiveDataPort();
         }
     }
 
     /**
      * use passive ftp?
      * 
      * @return
      */
     private boolean isPassive() {
         return DataSocketPassiveMode;
     }
 
     private void createActiveDataPort() throws IOException {
         // create data socket and bind it to free port available
         DataSocketActive = new ServerSocket(0);
         DataSocketActive.setSoTimeout(getTimeout());
         DataSocketActive.setReceiveBufferSize(1440); // read http://www.cisco.com/warp/public/105/38.shtml
         applyDataSocketTimeout();
 
         // get port socket has been bound to
         final int DataPort = DataSocketActive.getLocalPort();
 
         // client ip
         // InetAddress LocalIp = serverCore.publicIP();
         // InetAddress LocalIp =
         // DataSocketActive.getInetAddress().getLocalHost();
 
         // save ip address in high byte order
         // byte[] Bytes = LocalIp.getAddress();
         final byte[] Bytes = Domains.myPublicLocalIP().getHostAddress().getBytes();
 
         // bytes greater than 127 should not be printed as negative
         final short[] Shorts = new short[4];
         for (int i = 0; i < 4; i++) {
             Shorts[i] = Bytes[i];
             if (Shorts[i] < 0) {
                 Shorts[i] += 256;
             }
         }
 
         // send port command via control socket:
         // four ip address shorts encoded and two port shorts encoded
         send("PORT "
                 +
                 // "127,0,0,1," +
                 Shorts[0] + "," + Shorts[1] + "," + Shorts[2] + "," + Shorts[3] + "," + ((DataPort & 0xff00) >> 8)
                 + "," + (DataPort & 0x00ff));
 
         // read status of the command from the control port
         final String reply = receive();
 
         // check status code
         if (isNotPositiveCompletion(reply)) {
             throw new IOException(reply);
         }
 
         DataSocketPassiveMode = false;
     }
 
     private void createPassiveDataPort() throws IOException {
         // send port command via control socket:
         // four ip address shorts encoded and two port shorts encoded
         send("PASV");
 
         // read status of the command from the control port
         String reply = receive();
 
         // check status code
         if (getStatusCode(reply) != 227) {
             throw new IOException(reply);
         }
 
         // parse the status return: address should start at the first number
         int pos = 4;
         while ((pos < reply.length()) && ((reply.charAt(pos) < '0') || (reply.charAt(pos) > '9'))) {
             pos++;
         }
         if (pos >= reply.length()) {
             throw new IOException(reply + " [could not parse return code]");
         }
         reply = reply.substring(pos);
         pos = reply.length() - 1;
         while ((pos >= 0) && ((reply.charAt(pos) < '0') || (reply.charAt(pos) > '9'))) {
             pos--;
         }
         if (pos < 0) {
             throw new IOException("[could not parse return code: no numbers]");
         }
         reply = reply.substring(0, pos + 1);
         final StringTokenizer st = new StringTokenizer(reply, ",");
         if (st.countTokens() != 6) {
             throw new IOException("[could not parse return code: wrong number of numbers]");
         }
 
         // set the data host and port
         final int a = Integer.parseInt(st.nextToken());
         final int b = Integer.parseInt(st.nextToken());
         final int c = Integer.parseInt(st.nextToken());
         final int d = Integer.parseInt(st.nextToken());
         final InetAddress datahost = InetAddress.getByName(a + "." + b + "." + c + "." + d);
         final int high = Integer.parseInt(st.nextToken());
         final int low = Integer.parseInt(st.nextToken());
         if (high < 0 || high > 255 || low < 0 || low > 255) {
             throw new IOException("[could not parse return code: syntax error]");
         }
         final int dataport = (high << 8) + low;
 
         DataSocketPassive = new Socket(datahost, dataport);
         applyDataSocketTimeout();
         DataSocketPassiveMode = true;
     }
 
     /**
      * closes data connection
      * 
      * @throws IOException
      */
     private void closeDataSocket() throws IOException {
         if (isPassive()) {
             if (DataSocketPassive != null) {
                 DataSocketPassive.close();
                 DataSocketPassive = null;
             }
         } else {
             if (DataSocketActive != null) {
                 DataSocketActive.close();
                 DataSocketActive = null;
             }
         }
     }
 
     /**
      * sets the timeout for the socket
      * 
      * @throws SocketException
      */
     private void applyDataSocketTimeout() throws SocketException {
         if (isPassive()) {
             if (DataSocketPassive != null) {
                 DataSocketPassive.setSoTimeout(DataSocketTimeout * 1000);
             }
         } else {
             if (DataSocketActive != null) {
                 DataSocketActive.setSoTimeout(DataSocketTimeout * 1000);
             }
         }
     }
 
     private void get(final String fileDest, final String fileName) throws IOException {
         // store time for statistics
         final long start = System.currentTimeMillis();
 
         createDataSocket();
 
         // set type of the transfer
         sendTransferType(transferType);
 
         // send command to the control port
         send("RETR " + fileName);
 
         // read status of the command from the control port
         String reply = receive();
 
         // get status code
         final int status = getStatus(reply);
 
         // starting data transaction
         if (status == 1) {
             Socket data = null;
             InputStream ClientStream = null;
             RandomAccessFile outFile = null;
             int length = 0;
             try {
                 data = getDataSocket();
                 ClientStream = data.getInputStream();
     
                 // create local file
                 if (fileDest == null) {
                     outFile = new RandomAccessFile(fileName, "rw");
                 } else {
                     outFile = new RandomAccessFile(fileDest, "rw");
                 }
     
                 // write remote file to local file
                 final byte[] block = new byte[blockSize];
                 int numRead;
     
                 while ((numRead = ClientStream.read(block)) != -1) {
                     outFile.write(block, 0, numRead);
                     length = length + numRead;
                 }
     
                 // after stream is empty we should get control completion echo
                 /*reply =*/ receive();
                 // boolean success = !isNotPositiveCompletion(reply);
             } finally {
                 // shutdown connection
                 if(outFile != null) {
                     outFile.close();
                 }
                 if(ClientStream != null) {
                     ClientStream.close();
                 }
                 closeDataSocket();
             }
 
             // if (!success) throw new IOException(reply);
 
             // write statistics
             final long stop = System.currentTimeMillis();
             outPrintln(logPrefix
                     + " ---- downloaded "
                     + ((length < 2048) ? length + " bytes" : (length / 1024) + " kbytes")
                     + " in "
                     + (((stop - start) < 2000) ? (stop - start) + " milliseconds"
                             : (((int) ((stop - start) / 100)) / 10) + " seconds"));
             if (start == stop) {
                 errPrintln(logPrefix);
             } else {
                 outPrintln(" (" + (length * 1000 / 1024 / (stop - start)) + " kbytes/second)");
             }
 
         } else {
             throw new IOException(reply);
         }
     }
 
     public byte[] get(final String fileName) throws IOException {
 
         createDataSocket();
 
         // set type of the transfer
         sendTransferType(transferType);
 
         // send command to the control port
         send("RETR " + fileName);
 
         // read status of the command from the control port
         String reply = receive();
 
         // get status code
         final int status = getStatus(reply);
 
         // starting data transaction
         if (status == 1) {
             Socket data = null;
             InputStream ClientStream = null;
             ByteArrayOutputStream os = new ByteArrayOutputStream();
             int length = 0;
             try {
                 data = getDataSocket();
                 ClientStream = data.getInputStream();
     
                 // write remote file to local file
                 final byte[] block = new byte[blockSize];
                 int numRead;
     
                 while ((numRead = ClientStream.read(block)) != -1) {
                     os.write(block, 0, numRead);
                     length = length + numRead;
                 }
     
                 // after stream is empty we should get control completion echo
                 /*reply =*/ receive();
                 // boolean success = !isNotPositiveCompletion(reply);
             } finally {
                 // shutdown connection
                 if(ClientStream != null) {
                     ClientStream.close();
                 }
                 closeDataSocket();
             }
             return os.toByteArray();
         } else {
             throw new IOException(reply);
         }
     }
 
     
     private void put(final String fileName, final String fileDest) throws IOException {
 
         createDataSocket();
 
         // set type of the transfer
         sendTransferType(transferType);
 
         // send command to the control port
         if (fileDest == null) {
             send("STOR " + fileName);
         } else {
             send("STOR " + fileDest);
         }
 
         // read status of the command from the control port
         String reply = receive();
 
         // starting data transaction
         if (getStatus(reply) == 1) {
             final Socket data = getDataSocket();
             final OutputStream ClientStream = data.getOutputStream();
 
             // read from local file
             final RandomAccessFile inFile = new RandomAccessFile(fileName, "r");
 
             // write remote file to local file
             final byte[] block = new byte[blockSize];
             int numRead;
 
             while ((numRead = inFile.read(block)) >= 0) {
                 ClientStream.write(block, 0, numRead);
             }
 
             // shutdown and cleanup
             inFile.close();
             ClientStream.close();
 
             // after stream is empty we should get control completion echo
             reply = receive();
             final boolean success = (getStatus(reply) == 2);
 
             // shutdown remote client connection
             data.close();
 
             if (!success) {
                 throw new IOException(reply);
             }
 
         } else {
             throw new IOException(reply);
         }
     }
 
     /**
      * Login to server
      * 
      * @param account
      * @param password
      * @throws IOException
      */
     private void login(final String account, final String password) throws IOException {
         unsetLoginData();
 
         // send user name
         send("USER " + account);
 
         String reply = receive();
         switch (getStatus(reply)) {
         case 2:
             // User logged in, proceed.
             break;
         case 5:// 530 Not logged in.
         case 4:
         case 1:// in RFC959 an error (page 57, diagram for the Login
             // sequence)
             throw new IOException(reply);
         default:
             // send password
             send("PASS " + password);
 
             reply = receive();
             if (isNotPositiveCompletion(reply)) {
                 throw new IOException(reply);
             }
         }
         setLoginData(account, password, reply);
     }
 
     /**
      * we are authorized to use the server
      * 
      * @return
      */
     public boolean isLoggedIn() {
         return (account != null && password != null && remotegreeting != null);
     }
 
     /**
      * remember username and password which were used to login
      * 
      * @param account
      * @param password
      * @param reply
      *                remoteGreeting
      */
     private void setLoginData(final String account, final String password, final String reply) {
         this.account = account;
         this.password = password;
         remotegreeting = reply;
     }
 
     private void unsetLoginData() {
         account = null;
         password = null;
         remotegreeting = null;
     }
 
     public void sys() throws IOException {
         // send system command
         send("SYST");
 
         // check completion
         final String systemType = receive();
         if (isNotPositiveCompletion(systemType)) {
             throw new IOException(systemType);
         }
 
         // exclude status code from reply
         remotesystem = systemType.substring(4);
     }
 
     private void literal(final String commandLine) throws IOException {
         // send the complete line
         send(commandLine);
 
         // read reply
         final String reply = receive();
 
         if (getStatus(reply) == 5) {
             throw new IOException(reply);
         }
     }
 
     /**
      * control socket timeout
      * 
      * @return
      */
     public int getTimeout() {
         return ControlSocketTimeout;
     }
 
     /**
      * set timeout for data connections calculated for a minimum data rate
      * 
      * @param maxFilesize
      * @return timeout in seconds
      */
     public void setDataTimeoutByMaxFilesize(final int maxFilesize) {
         int timeout = 1;
         if (DataSocketRate > 0) {
             // calculate by minDataRate and MaxFTPFileSize
             timeout = maxFilesize / DataSocketRate;
         }
 
         setDataSocketTimeout(timeout);
     }
 
     /**
      * after this time the data connection is closed
      * 
      * @param timeout
      *                in seconds, 0 = infinite
      */
     public void setDataSocketTimeout(final int timeout) {
         DataSocketTimeout = timeout;
 
         try {
             applyDataSocketTimeout();
         } catch (final SocketException e) {
             errPrintln(logPrefix + " setDataSocketTimeout: " + e.getMessage());
         }
     }
 
     public static List<String> dir(final String host, final String remotePath, final String account,
             final String password, final boolean extended) {
         try {
             final ftpc c = new ftpc();
             c.cmd = new String[] { "open", host };
             c.OPEN();
             c.cmd = new String[] { "user", account, password };
             c.USER();
             c.cmd = new String[] { "ls" };
             final List<String> v = c.list(remotePath, extended);
             c.cmd = new String[] { "close" };
             c.CLOSE();
             c.cmd = new String[] { "exit" };
             c.EXIT();
             return v;
         } catch (final java.security.AccessControlException e) {
             return null;
         } catch (final IOException e) {
             return null;
         }
     }
 
     public static void dir(final String host, final String remotePath, final String account, final String password) {
         try {
             final ftpc c = new ftpc();
             c.exec("open " + host, false);
             c.exec("user " + account + " " + password, false);
             c.exec("cd " + remotePath, false);
             c.exec("ls", true);
             c.exec("close", false);
             c.exec("exit", false);
         } catch (final java.security.AccessControlException e) {
         }
     }
 
     public StringBuilder dirhtml(String remotePath) {
         // returns a directory listing using an existing connection
         try {
             if (isFolder(remotePath) && '/' != remotePath.charAt(remotePath.length()-1)) {
                 remotePath += '/';
             }
             final List<String> list = list(remotePath, true);
             if (remotesystem == null) {
                 sys();
             }
             final String base = "ftp://" + ((account.equals("anonymous")) ? "" : (account + ":" + password + "@"))
                     + host + ((port == 21) ? "" : (":" + port)) + ((remotePath.length() > 0 && remotePath.charAt(0) == '/') ? "" : pwd() + "/")
                     + remotePath;
 
             return dirhtml(base, remotemessage, remotegreeting, remotesystem, list, true);
         } catch (final java.security.AccessControlException e) {
             return null;
         } catch (final IOException e) {
             return null;
         }
     }
 
     public static StringBuilder dirhtml(
             final String host, final int port, final String remotePath,
             final String account, final String password) {
         // opens a new connection and returns a directory listing as html
         try {
             final ftpc c = new ftpc(System.in, null, System.err);
             c.open(host, port);
             c.login(account, password);
             c.sys();
             final StringBuilder page = c.dirhtml(remotePath);
             c.quit();
             return page;
         } catch (final java.security.AccessControlException e) {
             return null;
         } catch (final IOException e) {
             return null;
         }
     }
 
     public static StringBuilder dirhtml(
             final String base, final String servermessage, final String greeting,
             final String system, final List<String> list,
             final boolean metaRobotNoindex) {
         // this creates the html output from collected strings
         final StringBuilder page = new StringBuilder(1024);
         final String title = "Index of " + base;
 
         page.append("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 3.2 Final//EN\">\n");
         page.append("<html><head>\n");
         page.append("  <title>" + title + "</title>\n");
         page.append("  <meta name=\"generator\" content=\"YaCy ftpc dirlisting\">\n");
         if (metaRobotNoindex) {
             page.append("  <meta name=\"robots\" content=\"noindex\">\n");
         }
         page.append("  <base href=\"" + base + "\">\n");
         page.append("</head><body>\n");
         page.append("  <h1>" + title + "</h1>\n");
         if (servermessage != null && greeting != null) {
             page.append("  <p><pre>Server \"" + servermessage + "\" responded:\n");
             page.append("  \n");
             page.append(greeting);
             page.append("\n");
             page.append("  </pre></p>\n");
         }
         page.append("  <hr>\n");
         page.append("  <pre>\n");
         int nameStart, nameEnd;
         entryInfo info;
         for (final String line : list) {
             info = parseListData(line);
             if(info != null) {
                 // with link
                 nameStart = line.indexOf(info.name);
                 page.append(line.substring(0, nameStart));
                 page.append("<a href=\"" + base + info.name + ((info.isDir) ? "/" : "") + "\">" + info.name + "</a>");
                 nameEnd = nameStart + info.name.length();
                 if (line.length() > nameEnd) {
                     page.append(line.substring(nameEnd));
                 }
             } else {
                 // raw
                 page.append(line);
             }
             page.append('\n');
         }
         page.append("  </pre>\n");
         page.append("  <hr>\n");
         if (system != null) page.append("  <pre>System info: \"" + system + "\"</pre>\n");
         page.append("</body></html>\n");
 
         return page;
     }
 
     public static void dirAnonymous(final String host, final String remotePath) {
         dir(host, remotePath, "anonymous", "anomic");
     }
 
     public static void dirAnonymousHtml(final String host, final int port, final String remotePath,
             final String htmloutfile) {
         final StringBuilder page = dirhtml(host, port, remotePath, "anonymous", "anomic");
         final File file = new File(htmloutfile);
         FileOutputStream fos;
         try {
             fos = new FileOutputStream(file);
             fos.write(page.toString().getBytes());
             fos.close();
         } catch (final FileNotFoundException e) {
             Log.logException(e);
         } catch (final IOException e) {
             Log.logException(e);
         }
     }
 
     public static String put(final String host, File localFile, String remotePath, final String remoteName,
             final String account, final String password) throws IOException {
         // returns the log
         try {
             final ByteArrayOutputStream bout = new ByteArrayOutputStream();
             final PrintStream out = new PrintStream(bout);
 
             final ByteArrayOutputStream berr = new ByteArrayOutputStream();
             final PrintStream err = new PrintStream(berr);
 
             final ftpc c = new ftpc(System.in, out, err);
             c.exec("open " + host, false);
             c.exec("user " + account + " " + password, false);
             if (remotePath != null) {
                 remotePath = remotePath.replace('\\', '/');
                 c.exec("cd " + remotePath, false);
             }
             c.exec("binary", false);
             if (localFile.isAbsolute()) {
                 c.exec("lcd \"" + localFile.getParent() + "\"", false);
                 localFile = new File(localFile.getName());
             }
             c.exec("put " + localFile.toString() + ((remoteName.length() == 0) ? "" : (" " + remoteName)), false);
             c.exec("close", false);
             c.exec("exit", false);
 
             out.close();
             err.close();
 
             final String outLog = bout.toString();
             bout.close();
 
             final String errLog = berr.toString();
             berr.close();
 
             if (errLog.length() > 0) {
                 throw new IOException("Ftp put failed:\n" + errLog);
             }
 
             return outLog;
         } catch (final IOException e) {
             throw e;
         }
     }
 
     public static void get(final String host, String remoteFile, final File localPath, final String account,
             final String password) {
         try {
             final ftpc c = new ftpc();
             if (remoteFile.length() == 0) {
                 remoteFile = "/";
             }
             c.exec("open " + host, false);
             c.exec("user " + account + " " + password, false);
             c.exec("lcd " + localPath.getAbsolutePath(), false);
             c.exec("binary", false);
             c.exec("get " + remoteFile + " " + localPath.getAbsoluteFile().toString(), false);
             c.exec("close", false);
             c.exec("exit", false);
         } catch (final java.security.AccessControlException e) {
         }
     }
 
     public static void getAnonymous(final String host, final String remoteFile, final File localPath) {
         get(host, remoteFile, localPath, "anonymous", "anomic");
     }
 
     /**
      * class that puts a file on a ftp-server can be used as a thread
      */
     static class pt implements Runnable {
         String host;
         File localFile;
         String remotePath;
         String remoteName;
         String account;
         String password;
 
         public pt(final String h, final File l, final String rp, final String rn, final String a, final String p) {
             host = h;
             localFile = l;
             remotePath = rp;
             remoteName = rn;
             account = a;
             password = p;
         }
 
         public final void run() {
             try {
                 put(host, localFile, remotePath, remoteName, account, password);
             } catch (final IOException e) {
                 Log.logException(e);
             }
         }
     }
 
     public static Thread putAsync(final String host, final File localFile, final String remotePath,
             final String remoteName, final String account, final String password) {
         final Thread t = new Thread(new pt(host, localFile, remotePath, remoteName, account, password), "ftp to " + host);
         t.start();
         return t; // return value can be used to determine status of transfer
         // with isAlive() or join()
     }
 
     private static void printHelp() {
         System.out.println(logPrefix + "ftp help");
         System.out.println(logPrefix + "----------");
         System.out.println(logPrefix);
         System.out.println(logPrefix + "The following commands are supported");
         System.out.println(logPrefix
                 + "java ftp  -- (without arguments) starts the shell. Thy 'help' then for shell commands.");
         System.out.println(logPrefix + "java ftp <host>[':'<port>]  -- starts shell and connects to specified host");
         System.out.println(logPrefix + "java ftp -h  -- prints this help");
         System.out.println(logPrefix + "java ftp -dir <host>[':'<port>] <path> [<account> <password>]");
         System.out.println(logPrefix
                 + "java ftp -get <host>[':'<port>] <remoteFile> <localPath> [<account> <password>]");
         System.out.println(logPrefix + "java ftp -put <host>[':'<port>] <localFile> <remotePath> <account> <password>");
         System.out.println(logPrefix);
     }
 
     public static void main(final String[] args) {
         System.out.println(logPrefix + "WELCOME TO THE ANOMIC FTP CLIENT v" + vDATE);
         System.out.println(logPrefix + "Visit http://www.anomic.de and support shareware!");
         System.out.println(logPrefix + "try -h for command line options");
         System.out.println(logPrefix);
         if (args.length == 0) {
             (new ftpc()).shell(null);
         } else if (args.length == 1) {
             if (args[0].equals("-h")) {
                 printHelp();
             } else {
                 (new ftpc()).shell(args[0]);
             }
         } else if (args.length == 2) {
             printHelp();
         } else if (args.length == 3) {
             if (args[0].equals("-dir")) {
                 dirAnonymous(args[1], args[2]);
             } else if (args[0].equals("-htmldir")) {
                 dirAnonymousHtml(args[1], 21, args[2], "dirindex.html");
             } else {
                 printHelp();
             }
         } else if (args.length == 4) {
             if (args[0].equals("-get")) {
                 getAnonymous(args[1], args[2], new File(args[3]));
             } else {
                 printHelp();
             }
         } else if (args.length == 5) {
             if (args[0].equals("-dir")) {
                 dir(args[1], args[2], args[3], args[4]);
             } else {
                 printHelp();
             }
         } else if (args.length == 6) {
             if (args[0].equals("-get")) {
                 get(args[1], args[2], new File(args[3]), args[4], args[5]);
             } else if (args[0].equals("-put")) {
                 try {
                     put(args[1], new File(args[2]), args[3], "", args[4], args[5]);
                 } catch (final IOException e) {
                     // TODO Auto-generated catch block
                     Log.logException(e);
                 }
             } else {
                 printHelp();
             }
         } else {
             printHelp();
         }
     }
 
 }
