 package au.edu.uq.cmm.paul.grabber;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.nio.channels.FileLock;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.BlockingDeque;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.LinkedBlockingDeque;
 import java.util.concurrent.TimeUnit;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.apache.log4j.Logger;
 import org.codehaus.jackson.JsonGenerationException;
 
 import au.edu.uq.cmm.paul.Paul;
 import au.edu.uq.cmm.paul.PaulException;
 import au.edu.uq.cmm.paul.queue.QueueManager;
 import au.edu.uq.cmm.paul.status.DatafileTemplate;
 import au.edu.uq.cmm.paul.status.Facility;
 import au.edu.uq.cmm.paul.status.FacilitySession;
 import au.edu.uq.cmm.paul.watcher.FileWatcherEvent;
 
 /**
  * This class represents a unit of work for the {@link FileGrabber} executor;
  * i.e. a dataset to be "grabbed".  This class does most of the work of grabbing
  * and creation of the queue entries.
  * 
  * @author scrawley
  */
 class WorkEntry implements Runnable {
     private static final Logger LOG = Logger.getLogger(WorkEntry.class);
 
     private final FileGrabber fileGrabber;
     private final QueueManager queueManager;
     private final BlockingDeque<FileWatcherEvent> events;
     private final File baseFile;
     private final Map<File, GrabbedFile> files;
     private final Facility facility;
     private final Date timestamp;
     
     public WorkEntry(Paul services, FileWatcherEvent event, File baseFile) {
         this.facility = (Facility) event.getFacility();
         this.timestamp = new Date(event.getTimestamp());
         this.fileGrabber = services.getFileGrabber();
         this.queueManager = services.getQueueManager();
         this.baseFile = baseFile;
         this.files = new ConcurrentHashMap<File, GrabbedFile>();
         this.events = new LinkedBlockingDeque<FileWatcherEvent>();
         addEvent(event);
     }
 
     public void addEvent(FileWatcherEvent event) {
         events.add(event);
         // FIXME - events that arrive too late possibly won't be grabbed
         // because they aren't guaranteed to be in the set that the
         // grabFiles method is iterating.
         File file = event.getFile();
         LOG.debug("Processing event for file " + file);
         boolean matched = false;
         List<DatafileTemplate> templates = facility.getDatafileTemplates();
         if (templates.isEmpty()) {
             if (!files.containsKey(file)) {
                 files.put(file, new GrabbedFile(file, file, null));
                 LOG.debug("Added file " + file + " to map for grabbing");
             } else {
                 LOG.debug("File " + file + " already in map for grabbing");
             }
         } else {
             for (DatafileTemplate template : templates) {
                 Pattern pattern = template.getCompiledFilePattern(
                         facility.isCaseInsensitive());
                 Matcher matcher = pattern.matcher(file.getAbsolutePath());
                 if (matcher.matches()) {
                     if (!files.containsKey(file)) {
                         files.put(file, new GrabbedFile(
                                 new File(matcher.group(1)), file, template));
                         LOG.debug("Added file " + file + " to map for grabbing");
                     } else {
                         LOG.debug("File " + file + " already in map for grabbing");
                     }
                     matched = true;
                     break;
                 }
             }
             if (!matched) {
                 LOG.debug("File " + file + " didn't match any template - ignoring");
             }
         }
     }
 
     @Override
     public void run() {
         FileGrabber.LOG.debug("Processing a workEntry");
         try {
             grabFiles();
         } catch (InterruptedException ex) {
             FileGrabber.LOG.debug("interrupted");
         }
         synchronized (fileGrabber) {
             fileGrabber.remove(this.baseFile);
         }
         FileGrabber.LOG.debug("Finished processing workEntry");
     }
 
     private void grabFiles() throws InterruptedException {
         int settling = facility.getFileSettlingTime();
         if (settling <= 0) {
             settling = FileGrabber.DEFAULT_FILE_SETTLING_TIME;
         }
         // Wait until the file modification events stop arriving ... plus
         // the settling time.
         while (events.poll(settling, TimeUnit.MILLISECONDS) != null) {
             LOG.debug("poll");
         }
        // Optionally lock the files, then grab them.
         Date now = new Date();
         FacilitySession session = fileGrabber.getStatusManager().
                 getLoginDetails(facility.getFacilityName(), timestamp.getTime());
         if (session == null) {
             session = FacilitySession.makeDummySession(facility, now);
         }
         // FIXME - note that we may not see all of the files ... see above.
         for (GrabbedFile file : files.values()) {
             try (FileInputStream is = new FileInputStream(file.getFile())) {
                 if (facility.isUseFileLocks()) {
                     LOG.debug("acquiring lock on " + file);
                     try (FileLock lock = is.getChannel().lock(0, Long.MAX_VALUE, true)) {
                         LOG.debug("locked " + file);
                         doGrabFile(file, is);
                     }
                     LOG.debug("unlocked " + file);
                 } else {
                     doGrabFile(file, is);
                 }
             } catch (IOException ex) {
                 LOG.error("Unexpected IO Error", ex);
             }
         }
         try {
             saveMetadata(now, session);
         } catch (JsonGenerationException ex) {
             LOG.error("Unexpected JSON Error", ex);
         } catch (IOException ex) {
             LOG.error("Unexpected IO Error", ex);
         }
     }
 
     private void doGrabFile(GrabbedFile file, FileInputStream is) 
             throws InterruptedException, IOException {
         LOG.debug("Start file grabbing");
         Date now = new Date();
         Date fileTimestamp = new Date(file.getFile().lastModified());
         String suffix = (file.getTemplate() == null) ?
                 ".data" : file.getTemplate().getSuffix();
         File copiedFile = copyFile(is, file.getFile(), suffix);
         file.setCopiedFile(copiedFile);
         file.setFileTimestamp(fileTimestamp);
         file.setCopyTimestamp(now);
         LOG.debug("Done grabbing");
     }
 
     private void saveMetadata(Date now,FacilitySession session)
             throws IOException, JsonGenerationException {
         // FIXME - support multiple files properly ...
         File metadataFile = generateUniqueFile(".admin");
         List<DatafileMetadata> list = new ArrayList<DatafileMetadata>(files.size());
         for (GrabbedFile g : files.values()) {
             String mimeType = (g.getTemplate() == null) ? 
                     "application/octet-stream" : g.getTemplate().getMimeType();
             DatafileMetadata d = new DatafileMetadata(
                     g.getFile().getAbsolutePath(), g.getCopiedFile().getAbsolutePath(), 
                     g.getFileTimestamp(), g.getCopyTimestamp(), mimeType);
             list.add(d);
         }
         DatasetMetadata metadata = new DatasetMetadata(
                 baseFile.getAbsolutePath(), metadataFile.getAbsolutePath(), 
                 session.getUserName(), facility.getFacilityName(), 
                 session.getAccount(), session.getEmailAddress(), 
                 now, session.getSessionUuid(), session.getLoginTime(), list);
         queueManager.addEntry(metadata, metadataFile);
     }
 
     private File copyFile(FileInputStream is, File source, String suffix) 
             throws IOException {
         // TODO - if the time taken to copy files is a problem, we could 
         // potentially improve this by using NIO or memory mapped files.
         File target = generateUniqueFile(suffix);
         long size = source.length();
         try (FileOutputStream os = new FileOutputStream(target)) {
             byte[] buffer = new byte[(int) Math.min(size, 8192)];
             int nosRead;
             long totalRead = 0;
             while ((nosRead = is.read(buffer, 0, buffer.length)) > 0) {
                 os.write(buffer, 0, nosRead);
                 totalRead += nosRead;
             }
             if (totalRead != size) {
                 // If this happen's there is something wrong with our locking
                 // and / or file settling heuristics.
                 LOG.error("Copied file size discrepancy - initial file size was " + size +
                         "bytes but we copied " + totalRead + " bytes");
             }
             LOG.info("Copied " + totalRead + " bytes from " + source + " to " + target);
         }
         return target;
     }
 
     private File generateUniqueFile(String suffix) {
         long now = System.currentTimeMillis();
         for (int i = 0; i < 10; i++) {
             String name = String.format("file-%d-%d-%d%s", 
                     now, Thread.currentThread().getId(), i, suffix);
             File file = new File(fileGrabber.getSafeDirectory(), name);
             if (!file.exists()) {
                 return file;
             }
         }
         throw new PaulException("Can't generate a unique filename!");
     }
 }
