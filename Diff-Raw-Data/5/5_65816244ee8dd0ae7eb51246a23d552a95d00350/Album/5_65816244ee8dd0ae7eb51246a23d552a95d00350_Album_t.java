 package ch.bergturbenthal.image.server;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileFilter;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.OutputStreamWriter;
 import java.io.PrintWriter;
 import java.io.UnsupportedEncodingException;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 import java.text.MessageFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 
 import org.apache.commons.codec.binary.Base32;
 import org.apache.commons.io.FileUtils;
 
 import ch.bergturbenthal.image.data.util.StringUtil;
 
 public class Album {
   private static class ImportEntry {
     static ImportEntry parseLine(final String line) {
       final String[] comps = line.split(";", 2);
       return new ImportEntry(comps[0], comps[1]);
     }
 
     private final String filename;
     private final String hash;
 
     public ImportEntry(final String filename, final String hash) {
       super();
       this.filename = filename;
       this.hash = hash;
     }
 
     public String getFilename() {
       return filename;
     }
 
     public String getHash() {
       return hash;
     }
 
     public String makeString() {
       return filename + ";" + hash;
     }
 
   }
 
   private static String CACHE_DIR = ".servercache";
   private static String CLIENT_FILE = ".clientlist";
   private static String AUTOADD_FILE = ".autoadd";
   private static String INDEX_FILE = ".index";
   private final File baseDir;
   private long cachedImages = 0;
   private Map<String, AlbumImage> images = null;
   private final File cacheDir;
   private final String name;
   private Collection<ImportEntry> importEntries = null;
 
   public Album(final File baseDir, final String name) {
     this.baseDir = baseDir;
     this.name = name;
     prepareGitignore();
     cacheDir = new File(baseDir, CACHE_DIR);
     if (!cacheDir.exists())
       cacheDir.mkdir();
     if (autoaddFile().exists()) {
       loadImportEntries();
     }
   }
 
   public synchronized void addClient(final String client) {
     final Collection<String> clients = listClients();
     clients.add(client);
     saveClientList(clients);
   }
 
   public Date autoAddBeginDate() {
     final File file = autoaddFile();
     if (!file.exists())
       return null;
     try {
       final BufferedReader reader = bufferedReader(file);
       try {
         final String line = reader.readLine();
         if (line == null)
           return null;
         try {
           return new SimpleDateFormat("yyyy-MM-dd").parse(line);
         } catch (final ParseException e) {
           throw new RuntimeException("Cannot parse date " + line + " from File " + file, e);
         }
       } finally {
         reader.close();
       }
     } catch (final IOException e) {
       throw new RuntimeException("Cannot read " + file, e);
     }
   }
 
   public AlbumImage getImage(final String imageId) {
     return loadImages().get(imageId);
   }
 
   public String getName() {
     return name;
   }
 
   public boolean importImage(final File imageFile, final Date createDate) {
     if (!imageFile.exists())
       return false;
     final long length = imageFile.length();
     if (length == 0)
       return false;
     if (imageFile.getParent().equals(baseDir))
       // points to a already imported file
       return true;
     final String sha1OfFile = makeSha1(imageFile);
     synchronized (this) {
       final ImportEntry existingImportEntry = findExistingImportEntry(sha1OfFile);
       if (existingImportEntry != null) {
         final File file = new File(baseDir, existingImportEntry.getFilename());
         if (file.exists() && file.length() == length)
           // already full imported
           return true;
       }
       for (int i = 0; true; i++) {
         final File targetFile = new File(baseDir, makeFilename(imageFile.getName(), i, createDate));
         if (targetFile.exists()) {
           final ImportEntry entry = findOrMakeImportEntryForExisting(targetFile);
           if (entry.getHash().equals(sha1OfFile))
             // File already imported
             return true;
         } else {
           // new Filename found -> import file
           final File tempFile = new File(baseDir, targetFile.getName() + "-temp");
           try {
             FileUtils.copyFile(imageFile, tempFile);
             if (tempFile.renameTo(targetFile)) {
               final ImportEntry loadedEntry = findOrMakeImportEntryForExisting(targetFile);
               return loadedEntry.getHash().equals(sha1OfFile);
             } else
               return false;
           } catch (final IOException ex) {
             throw new RuntimeException("Cannot copy file " + imageFile, ex);
           } finally {
             // clear cache
             cachedImages = 0;
           }
         }
       }
     }
   }
 
   public synchronized Collection<String> listClients() {
     final File file = new File(baseDir, CLIENT_FILE);
     if (file.exists() && file.canRead()) {
       try {
         final Collection<String> clients = new HashSet<String>();
         final BufferedReader reader = bufferedReader(file);
         try {
           while (true) {
             final String line = reader.readLine();
             if (line == null)
               return clients;
             clients.add(line);
           }
         } finally {
           reader.close();
         }
       } catch (final IOException e) {
         throw new RuntimeException("Cannot read client-list: " + file, e);
       }
     } else
       return new HashSet<String>();
   }
 
   public synchronized Map<String, AlbumImage> listImages() {
     return loadImages();
   }
 
   public synchronized void removeClient(final String client) {
     final Collection<String> clients = listClients();
     clients.remove(client);
     saveClientList(clients);
   }
 
   @Override
   public String toString() {
     return "Album [" + getName() + "]";
   }
 
   public synchronized long totalSize() {
     long size = 0;
     for (final AlbumImage image : loadImages().values()) {
       size += image.readSize();
     }
     return size;
   }
 
   private void appendImportEntry(final ImportEntry newEntry) {
     if (importEntries == null)
       loadImportEntries();
     try {
       final PrintWriter writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(indexFile(), true), "utf-8"));
       try {
         writer.println(newEntry.makeString());
       } finally {
         writer.close();
       }
       importEntries.add(newEntry);
     } catch (final IOException e) {
       throw new RuntimeException("Cannot save import index", e);
     }
   }
 
   private File autoaddFile() {
     final File file = new File(baseDir, AUTOADD_FILE);
     return file;
   }
 
   private BufferedReader bufferedReader(final File file) throws UnsupportedEncodingException, FileNotFoundException {
     final BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "utf-8"));
     return reader;
   }
 
   private synchronized ImportEntry findExistingImportEntry(final String sha1OfFile) {
     if (importEntries == null)
       loadImportEntries();
     for (final ImportEntry entry : importEntries) {
       if (entry.getHash().equals(sha1OfFile))
         return entry;
     }
     return null;
   }
 
   private synchronized ImportEntry findOrMakeImportEntryForExisting(final File existingFile) {
     if (importEntries == null)
       loadImportEntries();
     for (final ImportEntry entry : importEntries) {
       if (entry.getFilename().equals(existingFile.getName()))
         return entry;
     }
     final ImportEntry newEntry = new ImportEntry(existingFile.getName(), makeSha1(existingFile));
     appendImportEntry(newEntry);
     return newEntry;
   }
 
   private File indexFile() {
     final File file = new File(cacheDir, INDEX_FILE);
     return file;
   }
 
   private synchronized Map<String, AlbumImage> loadImages() {
     final long lastModifiedBaseDir = baseDir.lastModified();
     if (lastModifiedBaseDir == cachedImages)
       return images;
     final File[] foundFiles = baseDir.listFiles(new FileFilter() {
       @Override
       public boolean accept(final File file) {
         if (!file.isFile() || !file.canRead())
           return false;
         final String lowerFilename = file.getName().toLowerCase();
         return lowerFilename.endsWith(".jpg") || lowerFilename.endsWith(".jpeg") || lowerFilename.endsWith(".nef");
       }
     });
     images = new HashMap<String, AlbumImage>();
     for (final File file : foundFiles) {
       images.put(Util.sha1(file.getAbsolutePath()), new AlbumImage(file, cacheDir));
     }
     cachedImages = lastModifiedBaseDir;
     return images;
   }
 
   private synchronized void loadImportEntries() {
     importEntries = new ArrayList<Album.ImportEntry>();
     final File file = indexFile();
     if (file.exists()) {
       try {
         final BufferedReader reader = bufferedReader(file);
         try {
           while (true) {
             final String line = reader.readLine();
             if (line == null)
               break;
             importEntries.add(ImportEntry.parseLine(line));
           }
         } finally {
           reader.close();
         }
       } catch (final IOException e) {
         throw new RuntimeException("Cannot read " + file, e);
       }
     }
   }
 
   private String makeFilename(final String name, final int i, final Date timestamp) {
     if (i == 0)
      return MessageFormat.format("{1,date,yyyy-MM-dd-HH-mm-ss}-{0}", name, timestamp);
     final int lastPt = name.lastIndexOf(".");
    return MessageFormat.format("{3,date,yyyy-MM-dd-HH-mm-ss}-{0}-{1}{2}", name.substring(0, lastPt), i, name.substring(lastPt), timestamp);
   }
 
   private String makeSha1(final File file) {
     try {
       final MessageDigest md = MessageDigest.getInstance("SHA-1");
       final FileInputStream fileInputStream = new FileInputStream(file);
       try {
         final byte[] buffer = new byte[8192];
         while (true) {
           final int read = fileInputStream.read(buffer);
           if (read < 0)
             break;
           md.update(buffer, 0, read);
         }
         final Base32 base32 = new Base32();
         return base32.encodeToString(md.digest()).toLowerCase();
       } finally {
         fileInputStream.close();
       }
     } catch (final NoSuchAlgorithmException e) {
       throw new RuntimeException("Cannot make sha1 of " + file, e);
     } catch (final IOException e) {
       throw new RuntimeException("Cannot make sha1 of " + file, e);
     }
   }
 
   private void prepareGitignore() {
     try {
       final File gitignore = new File(baseDir, ".gitignore");
       final Set<String> ignoreEntries = new HashSet<String>(Arrays.asList(CACHE_DIR, AUTOADD_FILE));
       if (gitignore.exists()) {
         final BufferedReader reader = bufferedReader(gitignore);
         try {
           while (ignoreEntries.size() > 0) {
             final String line = reader.readLine();
             if (line == null)
               break;
             ignoreEntries.remove(line);
           }
         } finally {
           reader.close();
         }
       }
       if (ignoreEntries.size() == 0)
         return;
       final PrintWriter writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(gitignore, true), "utf-8"));
       try {
         for (final String entry : ignoreEntries) {
           writer.println(entry);
         }
       } finally {
         writer.close();
       }
     } catch (final IOException e) {
       throw new RuntimeException("Cannot prepare .gitignore-file", e);
     }
   }
 
   private void saveClientList(final Collection<String> clients) {
     final File file = new File(baseDir, CLIENT_FILE);
     try {
       final PrintWriter writer = new PrintWriter(file, "utf-8");
       try {
         for (final String clientId : clients) {
           writer.println(StringUtil.filterClientIdString(clientId));
         }
       } finally {
         writer.close();
       }
     } catch (final IOException e) {
       throw new RuntimeException("Cannot write client-list to " + file, e);
     }
   }
 }
