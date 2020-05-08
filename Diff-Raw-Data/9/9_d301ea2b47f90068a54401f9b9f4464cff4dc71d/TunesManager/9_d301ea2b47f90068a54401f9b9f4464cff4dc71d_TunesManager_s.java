 package net.q1cc.cfs.tusync;
 
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.nio.file.FileVisitResult;
 import java.nio.file.FileVisitor;
 import java.nio.file.Files;
 import java.nio.file.InvalidPathException;
 import java.nio.file.Path;
 import java.nio.file.StandardCopyOption;
 import java.nio.file.attribute.BasicFileAttributes;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Map.Entry;
 import javax.swing.JOptionPane;
 import javax.swing.ListModel;
 import javax.swing.event.ListDataEvent;
 import javax.swing.event.ListDataListener;
 import javax.swing.event.TreeModelEvent;
 import javax.swing.event.TreeModelListener;
 import javax.swing.tree.TreeModel;
 import javax.swing.tree.TreePath;
 import net.q1cc.cfs.tusync.struct.*;
 import xmlwise.Plist;
 import xmlwise.XmlParseException;
 
 /**
  *
  * @author cfstras
  */
 public class TunesManager {
     
     public final static String[] tunesTitleFolders = {
     "Audiobooks", "Automatically Add to iTunes", "Automatisch zu iTunes hinzufügen",
     "iPod Games", "iTunes U", "Mobile Applications", "Movies", "Music", "Podcasts",
     "Ringtones", "Tones", "TV Shows"};
     
     private Main main;
     public ArrayList<Playlist> playlists;
     public HashMap<Integer, Title> titles;
     private ReCheckThread reCheckThread;
     private HashMap<String, Title> titlesToSync;
     public String baseFolder;
     TunesModel libModel;
     public boolean recheck;
     boolean checkingSize;
     boolean loadingLib;
     boolean syncingLib;
     long targetSize = 64 * 1000 * 1000 * 1000L;
 
     public TunesManager() {
         main = Main.instance();
         initLib();
         libModel = new TunesModel();
         reCheckThread = new ReCheckThread();
         reCheckThread.setPriority(Thread.MIN_PRIORITY);
         reCheckThread.start();
 
         if(Main.instance().props.getBoolean("lib.lastLoadWasSuccessful", false)) {
             loadLibrary();
         }
     }
 
     private void initLib() {
         playlists = new ArrayList<Playlist>(32);
         titles = new HashMap<Integer, Title>(256);
     }
 
     public void loadLibrary() {
         Thread libt = new Thread() {
             @Override
             public void run() {
                 try {
                     loadingLib = true;
                     main.gui.setSyncButton(checkingSize, syncingLib, loadingLib);
                     doLoadLibrary();
                 } catch (TunesParseException e) {
                     System.out.println("Parse Error: " + e);
                 } catch (XmlParseException e) {
                     // TODO Auto-generated catch block
                     e.printStackTrace();
                 } catch (IOException e) {
                     // TODO Auto-generated catch block
                     e.printStackTrace();
                 } finally {
                     main.gui.progressBar.setIndeterminate(false);
                     main.gui.progressBar.setString("");
                     main.gui.progressBar.setValue(0);
                     loadingLib = false;
                     main.gui.setSyncButton(checkingSize, syncingLib, loadingLib);
                 }
             }
         };
         libt.setName("LoadLibThread");
         libt.setPriority((Thread.MIN_PRIORITY+Thread.MAX_PRIORITY)/2);
         libt.start();
     }
 
     public void syncLibrary() {
         Thread synt = new Thread() {
             @Override
             public void run() {
                 syncingLib=true;
                 main.gui.setSyncButton(checkingSize, syncingLib, loadingLib);
                 main.gui.setListEnabled(false);
                 doSyncLibrary();
                 syncingLib=false;
                 main.gui.setSyncButton(checkingSize, syncingLib, loadingLib);
                 main.gui.setListEnabled(true);
             }
         };
         synt.setName("SyncThread");
         synt.setPriority((Thread.MIN_PRIORITY+Thread.MAX_PRIORITY)/2);
         synt.start();
     }
 
     private void doLoadLibrary() throws XmlParseException, IOException, TunesParseException {
         initLib();
         main.gui.progressBar.setString("Loading Library...");
         main.gui.progressBar.setIndeterminate(true);
 
         Main.instance().props.putBoolean("lib.lastLoadWasSuccessful",false);
 
         String path = main.props.get("lib.xmlfile", null);
         if (path == null) {
             JOptionPane.showMessageDialog(main.gui, "Please select the path to your iTunes library first.");
             return;
         }
         File xmlFile = new File(path);
         if (!xmlFile.exists()) {
             JOptionPane.showMessageDialog(main.gui, "Sorry, but i couldn't find your Library XML file.");
             //TODO find reason why xml was not found
             return;
         }
         Map<String, Object> lib = Plist.load(xmlFile);
 
         //for (Map.Entry<String, Object> e : lib.entrySet()) {
         //    System.out.println(e.getKey() + ": " + e.getValue().getClass());
         //}
 
         baseFolder = new File(Title.decodeLocation(lib.get("Music Folder").toString())).getAbsolutePath().concat(File.separator);
         Title.baseFolder = baseFolder;
         
         loadTracks(lib);
         loadPlaylists(lib);
         main.gui.list.setModel(libModel);
         Main.instance().props.putBoolean("lib.lastLoadWasSuccessful",true);
         libModel.fireUpdate();
         
         System.gc();
         //TODO serialize
     }
 
     void loadTracks(Map<String, Object> lib) throws TunesParseException {
         Object tr = lib.remove("Tracks");
         HashMap<String, HashMap> tracks = null;
         if (tr instanceof HashMap) {
             tracks = (HashMap<String, HashMap>) tr;
         }
 
         if (tracks == null) {
             throw new TunesParseException("no tracks");
         }
 
         int numTracks = tracks.size();
         int tracksPerProgress = numTracks / 100;
         int num = 0;
         int progress = 0;
         main.gui.progressBar.setMinimum(0);
         main.gui.progressBar.setMaximum(200);
         main.gui.progressBar.setValue(0);
         main.gui.progressBar.setString("reading tracks");
         main.gui.progressBar.setStringPainted(true);
         main.gui.progressBar.setIndeterminate(false);
 
         Iterator<HashMap> it = tracks.values().iterator();
         while (it.hasNext()) {
             HashMap<String, Object> obj = it.next();
             it.remove();
             if (!obj.containsKey("Track ID")) {
                 continue;
             }
             int id = (Integer) obj.get("Track ID");
             Title title = new Title(id);
 
             for (Entry<String, Object> e : obj.entrySet()) {
                 int ind = Title.getAttInd(e.getKey());
                 if (ind == -1) {
                     //error was already printed, ignore attribute
                     continue;
                 }
                 Object val = e.getValue();
                 if (val instanceof String) {
                     val = ((String) val).intern();
                 }
                 title.attribs[ind] = val;
             }
             titles.put(id, title);
             num++;
             if (num % tracksPerProgress == 0 || num == numTracks) {
                 main.gui.progressBar.setValue(++progress);
                 main.gui.progressBar.setString("reading tracks: " + num + " / " + numTracks);
             }
 
         }
         //tracks loaded.
 
     }
 
     void loadPlaylists(Map<String, Object> lib) throws TunesParseException {
         //playlists!
         Object pl = lib.remove("Playlists");
         ArrayList<HashMap> lists = null;
         if (pl instanceof ArrayList) {
             lists = (ArrayList<HashMap>) pl;
         }
 
         if (lists == null) {
             throw new TunesParseException("no playlists");
         }
 
         int numLists = lists.size();
         int listsPerProgress = Math.max(1,numLists/100);
         int num = 0;
         int progress = 100;
         main.gui.progressBar.setMinimum(0);
         main.gui.progressBar.setMaximum(200);
         main.gui.progressBar.setValue(100);
         main.gui.progressBar.setString("reading playlists");
         main.gui.progressBar.setStringPainted(true);
         main.gui.progressBar.setIndeterminate(false);
 
         Iterator<HashMap> it = lists.iterator();
         while (it.hasNext()) {
             HashMap list = it.next();
             it.remove();
             Playlist playlist = new Playlist(list.get("Name").toString(), (Integer) list.get("Playlist ID"));
             playlist.persID = list.get("Playlist Persistent ID").toString();
             Object ppid = list.get("Parent Persistent ID");
             if(ppid!=null) {
                 playlist.parentPersID = ppid.toString();
             }
             ArrayList<HashMap> entries = (ArrayList) list.get("Playlist Items");
             if (entries == null) {
                 //no entries.
                 continue;
             }
             Iterator<HashMap> listIt = entries.iterator();
             while (listIt.hasNext()) {
                 HashMap<String, Integer> entry = listIt.next();
                 playlist.addTitle(titles.get((Integer)entry.get("Track ID")));
             }
             playlists.add(playlist);
             //System.out.println("Playlist "+playlist.title+": "+playlist.tracks.size()+" tracks.");
             num++;
             if (num % listsPerProgress == 0 || num == numLists) {
                 main.gui.progressBar.setValue(++progress);
                 main.gui.progressBar.setString("reading playlists: " + num + " / " + numLists);
             }
         }
         //TODO sort playlists
         main.gui.progressBar.setValue(0);
         main.gui.progressBar.setString("");
         //playlists read. yay.
     }
 
     private void doSyncLibrary() {
         syncingLib=true;
         main.gui.setSyncButton(checkingSize, syncingLib, loadingLib);
         String targetPath = main.props.get("lib.targetpath", null);
         if(targetPath == null) {
             JOptionPane.showMessageDialog(main.gui, "No target path selected! Please select one.", "No target!", JOptionPane.ERROR_MESSAGE);
             return;
         }
         File targetPathFile = new File(targetPath);
         targetPathFile.mkdirs();
         if(!targetPathFile.canRead()) {
             JOptionPane.showMessageDialog(main.gui, "Can't read target path", "Read error", JOptionPane.ERROR_MESSAGE);
             return;
         }
         if(!targetPathFile.canWrite()) {
             JOptionPane.showMessageDialog(main.gui, "Can't write target path", "Write error", JOptionPane.ERROR_MESSAGE);
             return;
         }
 
         titlesToSync = new HashMap<String, Title>(256);
         syncPlaylists(targetPathFile);
         syncTitles(targetPathFile);
         titlesToSync = null;
         
         syncingLib=false;
         main.gui.setSyncButton(checkingSize, syncingLib, loadingLib);
         
         System.gc();
     }
     
     private void syncPlaylists(File targetPathFile) {
         main.gui.progressBar.setString("writing playlists...");
         main.gui.progressBar.setStringPainted(true);
         main.gui.progressBar.setMaximum(1100);
         main.gui.progressBar.setValue(0);
         main.gui.progressBar.setIndeterminate(false);
         int playlistsPerStep = Math.max(1,playlists.size()/100);
         int i=0, value=0;
         Iterator<Playlist> plIt = playlists.iterator();
         
         if(main.props.getBoolean("sync.deleteotherplaylists", false)) {
             main.gui.progressBar.setIndeterminate(true);
             main.gui.progressBar.setString("deleting old playlists...");
             for( File f: targetPathFile.listFiles()) {
                 if(f.getName().endsWith(".m3u")) {
                     f.delete();
                 }
             }
             main.gui.progressBar.setString("writing playlists...");
             main.gui.progressBar.setIndeterminate(false);
         }
         
         while(plIt.hasNext()) {
             PrintWriter out = null;
             try {
                 if(i++ % playlistsPerStep == 0) {
                     main.gui.progressBar.setValue(++value);
                 }
                 Playlist playlist = plIt.next();
                 if(!playlist.selected) {
                     continue;
                 }
                 String filename = playlist.title.replaceAll("[^\\w äöü\\-]", "-").concat(".m3u");
                 File playlistFile = new File(targetPathFile.getAbsolutePath()+File.separator+filename);
                 System.out.println("Playlist "+playlist.title+" --> "+filename);
                 playlistFile.createNewFile();
                 out = new PrintWriter(new FileWriter(playlistFile, false));
                 
                 out.println("#EXTM3U");
                 Iterator<Title> titleIt = playlist.tracks.values().iterator();
                 while(titleIt.hasNext()) {
                     Title t = titleIt.next();
                     if(!t.selected) {
                         continue;
                     }
                     String pathRel = t.getPathRelative();
                     if(pathRel == null || !t.selected)
                     {
                         continue;
                     }
                     out.print("#EXTINF:");
                     out.print(t.getLength()/100);
                     out.print(", ");
                     out.print(t.attribs[Title.getAttInd("Artist")]);
                     out.print(" - ");
                     out.println(t.attribs[Title.getAttInd("Name")]);
                     out.println(pathRel);
                     if(!Main.fileSystemCaseSensitive) {
                         pathRel = pathRel.toLowerCase();
                     }
                     titlesToSync.put(pathRel, t);
                 }
                 
                 out.println();
                 out.flush();
                 out.close();
             } catch (IOException ex) {
                 ex.printStackTrace();
             } finally {
                 if(out!=null) {
                     out.flush();
                     out.close();
                 }
             }
             main.gui.progressBar.setValue(100);
             
         }
         
     }
     
     private void syncTitles(File targetPathFile) {
         main.gui.progressBar.setString("syncing titles...");
        
        int titlesPerValue = Math.max(1,titlesToSync.size() / 1000);
        int value = 100, i = 0, iMax = titlesToSync.size();
 
         HashSet<Path> filesInTarget = new HashSet<Path>(256);
         if(main.props.getBoolean("sync.deleteothertitles", false)) {
             //walk dir and delete any extra files.
             main.gui.progressBar.setIndeterminate(true);
             main.gui.progressBar.setString("deleting other files...");
             try {
                 for (String s : tunesTitleFolders){
                     Files.walkFileTree(new File(targetPathFile+File.separator+s).toPath(),
                         new FileDeleter(titlesToSync, targetPathFile.getAbsolutePath()));
                 }
             } catch (IOException ex) {
                 ex.printStackTrace();
             }
             main.gui.progressBar.setString("syncing titles...");
             main.gui.progressBar.setIndeterminate(false);
         }
        
         Iterator<Entry<String, Title>> titleIt = titlesToSync.entrySet().iterator();
         while(titleIt.hasNext()) {
             Entry<String, Title> t = null;
             try {
                 main.gui.progressBar.setString("syncing titles: "+i+" / "+iMax);
                 if(i++ % titlesPerValue == 0) {
                     main.gui.progressBar.setValue(++value);
                 }
                 t = titleIt.next();
                 Path targetfile = new File(targetPathFile +File.separator+ t.getKey()).toPath();
 
                 Path source = new File(t.getValue().getFile()).toPath();
                 filesInTarget.remove(targetfile);
                 Files.createDirectories(targetfile.getParent());
                 System.out.println("copying "+source +" to "+ targetfile);
                 Files.copy(source, targetfile, StandardCopyOption.REPLACE_EXISTING);
             } catch (IOException ex) {
                 ex.printStackTrace();
             } catch (InvalidPathException ex) {
                 if(t!=null){
                     System.out.println("Error at "+t.toString()+": "+ex);
                 } else {
                     System.out.println("Error at "+ex);
                 }
             }
         }
         main.gui.progressBar.setValue(1100);
         main.gui.progressBar.setString("Titles successfully synced!");
         System.out.println("success.");
     }
     
     long lastSize = 0;
 
     public long getPlaylistSize(Collection<Title> tracks, HashSet<Title> ignore, long sizeBefore) {
         int attribID = Title.getAttInd("Size");
         long size = 0;
         for (Title t : tracks) {
             if (ignore.contains(t)) {
                 continue;
             }
             t.selected = true;
             size += t.getSizeOnDisk();
             ignore.add(t);
             setProgressSize(targetSize, size + sizeBefore, false);
         }
         return size;
     }
     
     public void reCheck() {
         recheck = true;
         main.gui.setSyncButton(true, syncingLib, loadingLib);
         reCheckThread.interrupt();
     }
 
     protected void doReCheck() {
         recheck = false;
         checkingSize = true;
         main.gui.setSyncButton(checkingSize, syncingLib, loadingLib);
         lastSize = 0;
         long size = 0;
         HashSet<Title> ignore = new HashSet<Title>(256);
         for (Playlist p : playlists) {
             if (p.selected) {
                 size += getPlaylistSize(p.tracks.values(), ignore, size);
             }
         }
         setProgressSize(targetSize, size, true);
         checkingSize = false;
         main.gui.setSyncButton(checkingSize, syncingLib, loadingLib);
         System.gc();
     }
     static final String progressIndicator = "|/-\\";
 
     private void setProgressSize(long targetSize, long size, boolean finished) {
         if (!finished && size - lastSize < 1024) {
             return;
         }
         long it = System.currentTimeMillis() / 200;
         it %= progressIndicator.length();
 
         long sizeDiv = targetSize / 500;
         main.gui.progressBar.setMaximum((int) (targetSize / sizeDiv));
         main.gui.progressBar.setValue((int) (size / sizeDiv));
         main.gui.progressBar.setString(humanize(size) + " / " + humanize(targetSize)
                 + ((!finished) ? " " + progressIndicator.charAt((int) it) : " occupied."));
         main.gui.progressBar.setStringPainted(true);
         lastSize = size;
     }
     private static final String siPrefixes = " KMGTPE";
 
     private static String humanize(long bytes) {
         int prefixID = 0;
         double divided = bytes;
         while (divided >= 1000 && prefixID < siPrefixes.length()) {
             divided /= 1024;
             prefixID++;
         }
         return (Math.floor(divided * 100) / 100) + " " + siPrefixes.charAt(prefixID) + 'B';
     }
 
     void toggleSelected(Playlist playlist) {
         if (syncingLib) {
             return;
         }
         playlist.setSelected(!playlist.selected);
         reCheck();
     }
 
     private class ReCheckThread extends Thread {
 
         boolean live = true;
 
         @Override
         public void run() {
             setName("ReCheckThread");
             while (live) {
                 if (TunesManager.this.recheck) {
                     TunesManager.this.doReCheck();
                 } else {
                     synchronized (this) {
                         try {
                             wait(5000);
                         } catch (InterruptedException ex) {
                         }
                     }
                 }
             }
         }
     }
 
     private class TunesModel implements TreeModel, ListModel {
 
         public String root = "Music";
         String playlistsNode = "Playlists";
         public ArrayList<TreeModelListener> treeeners = new ArrayList<TreeModelListener>(2);
         public ArrayList<ListDataListener> listeners = new ArrayList<ListDataListener>(2);
 
         public TunesModel() {
         }
 
         @Override
         public Object getRoot() {
             return root;
         }
 
         @Override
         public Object getChild(Object parent, int index) {
             if (parent == root) {
                 switch (index) {
                     case 0:
                         return "Playlists";
                     case 1:
                         return ""; //TODO tracks
                     default:
                         return null;
                 }
             } else if (parent == playlistsNode) {
                 return TunesManager.this.playlists.get(index);
             } else if (playlists.contains(parent)) {
                 //return one title
                 return null;
             }
             return null;
         }
 
         @Override
         public int getChildCount(Object parent) {
             if (parent == root) {
                 return 1;
             } else if (parent == playlistsNode) {
                 return TunesManager.this.playlists.size();
             } else if (TunesManager.this.playlists.contains(parent)) {
                 return 0;
                 //return ((Playlist)parent).tracks.size();
             }
             return 0;
         }
 
         @Override
         public boolean isLeaf(Object node) {
             if (node == root || node == playlistsNode) {
                 return false;
             } else {
                 return true;
             }
         }
 
         @Override
         public void valueForPathChanged(TreePath path, Object newValue) {
             throw new UnsupportedOperationException("Not supported yet.");
         }
 
         @Override
         public int getIndexOfChild(Object parent, Object child) {
             //TODO
             return 0;
         }
 
         @Override
         public void addTreeModelListener(TreeModelListener l) {
             treeeners.add(l);
         }
 
         @Override
         public void removeTreeModelListener(TreeModelListener l) {
             treeeners.remove(l);
         }
 
         public void fireUpdate() {
             TreeModelEvent t = new TreeModelEvent(root, new TreePath(root));
             for (TreeModelListener l : treeeners) {
                 l.treeStructureChanged(t);
             }
             ListDataEvent e = new ListDataEvent(TunesManager.this, ListDataEvent.INTERVAL_ADDED, 0, getSize());
             for (ListDataListener l : listeners) {
                 l.contentsChanged(e);
             }
         }
 
         @Override
         public int getSize() {
             return TunesManager.this.playlists.size();
         }
 
         @Override
         public Object getElementAt(int index) {
             return TunesManager.this.playlists.get(index);
         }
 
         @Override
         public void addListDataListener(ListDataListener l) {
             listeners.add(l);
         }
 
         @Override
         public void removeListDataListener(ListDataListener l) {
             listeners.remove(l);
         }
     }
 
     private static class FileDeleter implements FileVisitor {
 
         HashMap<String, Title> notThese;
         String basePath;
         boolean empty = false;
 
         public FileDeleter(HashMap<String, Title> notThese, String basePath) {
             this.notThese = notThese;
             this.basePath = basePath;
         }
 
         @Override
         public FileVisitResult preVisitDirectory(Object dir, BasicFileAttributes attrs) throws IOException {
             empty = true;
             return FileVisitResult.CONTINUE;
         }
 
         @Override
         public FileVisitResult visitFile(Object file, BasicFileAttributes attrs) throws IOException {
             String relpath = ((Path)file).toAbsolutePath().toString().replace(basePath+File.separator, "");
             if(!Main.fileSystemCaseSensitive) {
                 relpath = relpath.toLowerCase();
             }
             Title t = notThese.remove(relpath);
             if(t == null) {
                 Files.delete((Path) file);
                 System.out.println("deleting "+file);
             } else {
                 empty = false;
                 Path source = new File(t.getFile()).toPath();
                 if(Files.size(source) == Files.size((Path)file)
                 && Files.getLastModifiedTime(source).compareTo(Files.getLastModifiedTime((Path)file)) <= 0)  {
 
                     /*System.out.println("Skipping: srcTime:"+(Files.getLastModifiedTime(source).toString())
                     +" dstTime: "+Files.getLastModifiedTime((Path)file).toString()+ " size:"+Files.size(source)
                     +" src: "+source+" dest: "+(Path)file);*/
                 } else {
                     //put it back in
                     System.out.println("replacing: srcTime:"+(Files.getLastModifiedTime(source).toString())
                     +" dstTime: "+Files.getLastModifiedTime((Path)file).toString()+ " size:"+Files.size(source)
                     +" src: "+source+" dest: "+(Path)file);
                     notThese.put(relpath, t);
                 }
                 
             }
             return FileVisitResult.CONTINUE;
         }
 
         @Override
         public FileVisitResult visitFileFailed(Object file, IOException exc) throws IOException {
             return FileVisitResult.CONTINUE;
         }
 
         @Override
         public FileVisitResult postVisitDirectory(Object dir, IOException exc) throws IOException {
             if(empty) {
                 boolean empty = true;
                 Path p = (Path)dir;
                 Iterator<Path> it = p.iterator();
                 while(it.hasNext()) { // double-check whether empty
                     if(!it.next().toFile().isDirectory()) {
                         empty = false;
                         break;
                     }
                 }
                 if(empty) {
                     Files.delete(p);
                     return FileVisitResult.CONTINUE;
                 }
             }
             return FileVisitResult.CONTINUE;
         }
     }
 
     private static class FileLister implements FileVisitor {
         HashSet<Path> files;
         boolean dirEmpty = false;
         public FileLister(HashSet<Path> files) {
             this.files = files;
         }
 
         @Override
         public FileVisitResult preVisitDirectory(Object dir, BasicFileAttributes attrs) throws IOException {
             dirEmpty = true;
             return FileVisitResult.CONTINUE;
         }
 
         @Override
         public FileVisitResult visitFile(Object file, BasicFileAttributes attrs) throws IOException {
             if(attrs.isRegularFile()){
                 dirEmpty = false;
                 files.add((Path)file);
             }
             return FileVisitResult.CONTINUE;
         }
 
         @Override
         public FileVisitResult visitFileFailed(Object file, IOException exc) throws IOException {
             return FileVisitResult.CONTINUE;
         }
 
         @Override
         public FileVisitResult postVisitDirectory(Object dir, IOException exc) throws IOException {
             if(dirEmpty) { // delete directory if empty
                 boolean empty = true;
                 Path p = (Path)dir;
                 Iterator<Path> it = p.iterator();
                 while(it.hasNext()) { // double-check whether empty
                     if(!it.next().toFile().isDirectory()) {
                         empty = false;
                         break;
                     }
                 }
                 if(empty) {
                     Files.delete(p);
                     return FileVisitResult.CONTINUE;
                 }
             }
             files.add((Path)dir);
             return FileVisitResult.CONTINUE;
         }
     }
 }
