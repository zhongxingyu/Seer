 /***************************************************
 *
 * cismet GmbH, Saarbruecken, Germany
 *
 *              ... and it just works.
 *
 ****************************************************/
 package de.cismet.tools.gui.downloadmanager;
 
 import org.apache.log4j.Logger;
 
 import org.jdom.Element;
 
 import java.io.File;
 
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Observable;
 import java.util.Observer;
 
 import javax.swing.event.EventListenerList;
 
 import de.cismet.tools.configuration.Configurable;
 import de.cismet.tools.configuration.NoWriteError;
 
 /**
  * The download manager manages all current downloads. New downloads are added to a collection, completed downloads are
  * removed. Erraneous downloads remain in the collection. The download manager observes all download objects for state
  * changed and informs the download manager panel via the DownloadListChangedListener interface.
  *
  * @author   jweintraut
  * @version  $Revision$, $Date$
  */
 public class DownloadManager implements Observer, Configurable {
 
     //~ Static fields/initializers ---------------------------------------------
 
     private static final Logger LOG = Logger.getLogger(DownloadManager.class);
     private static final String XML_CONF_ROOT = "downloads";
     private static final String XML_CONF_DIRECTORY = "directory";
     private static final String XML_CONF_PARALLEL_DOWNLOADS = "parallelDownloads";
     private static final String XML_CONF_DIALOG = "dialog";
     private static final String XML_CONF_DIALOG_AKSFORTITLE = "askForTitle";
     private static final String XML_CONF_DIALOG_OPENAUTOMATICALLY = "openAutomatically";
     private static final String XML_CONF_DIALOG_CLOSEAUTOMATICALLY = "closeAutomatically";
     private static final String XML_CONF_DIALOG_USERTITLE = "userTitle";
     private static DownloadManager instance = null;
 
     //~ Instance fields --------------------------------------------------------
 
     private boolean enabled = true;
     private File destinationDirectory = new File(System.getProperty("user.home") + System.getProperty("file.separator")
                     + "cidsDownload");
     private int parallelDownloads = 2;
     private LinkedList<Download> downloads = new LinkedList<Download>();
     private List<SingleDownload> downloadsToStart = new LinkedList<SingleDownload>();
     private EventListenerList listeners = new EventListenerList();
     private int countDownloadsTotal = 0;
     private volatile int countDownloadsRunning = 0;
     private int countDownloadsErraneous = 0;
     private int countDownloadsCompleted = 0;
 
     //~ Constructors -----------------------------------------------------------
 
     /**
      * Creates a new DownloadManager object.
      */
     private DownloadManager() {
     }
 
     //~ Methods ----------------------------------------------------------------
 
     /**
      * It's a Singleton. There can only be one download manager.
      *
      * @return  The download manager.
      */
     public static DownloadManager instance() {
         if (instance == null) {
             instance = new DownloadManager();
         }
 
         return instance;
     }
 
     /**
      * This method is used to add new downloads to the download list.
      *
      * @param  download  wfss A collection of downloads to add.
      */
     public synchronized void add(final Download download) {
         if (download == null) {
             return;
         }
 
         this.downloads.add(download);
         countDownloadsTotal++;
         download.addObserver(this);
 
         if (download instanceof MultipleDownload) {
             final MultipleDownload multipleDownload = (MultipleDownload)download;
 
             for (final SingleDownload singleDownload : multipleDownload.getDownloads()) {
                 singleDownload.addObserver(this);
                 singleDownload.addObserver(multipleDownload);
 
                 downloadsToStart.add(singleDownload);
             }
         } else if (download instanceof SingleDownload) {
             downloadsToStart.add((SingleDownload)download);
         }
 
         notifyDownloadListChanged(new DownloadListChangedEvent(
                 this,
                 download,
                 DownloadListChangedEvent.Action.ADDED));
         notifyDownloadListChanged(new DownloadListChangedEvent(
                 this,
                 download,
                 DownloadListChangedEvent.Action.CHANGED_COUNTERS));
 
         startDownloads();
     }
 
     /**
      * Removes obsolete downloads. Only completed downloads are obsolete.
      */
     public synchronized void removeObsoleteDownloads() {
         final Collection<Download> downloadsRemoved = new LinkedList<Download>();
 
         for (final Download download : downloads) {
             if ((download.getStatus() == Download.State.COMPLETED)
                         || (download.getStatus() == Download.State.COMPLETED_WITH_ERROR)) {
                 downloadsRemoved.add(download);
             }
         }
 
         if (downloadsRemoved.size() <= 0) {
             return;
         }
 
         for (final Download download : downloadsRemoved) {
             downloads.remove(download);
             countDownloadsTotal--;
 
             switch (download.getStatus()) {
                 case COMPLETED_WITH_ERROR: {
                     countDownloadsErraneous--;
                     break;
                 }
                 case COMPLETED: {
                     countDownloadsCompleted--;
                     break;
                 }
             }
 
             download.deleteObserver(this);
             if (download instanceof MultipleDownload) {
                 final MultipleDownload multipleDownload = (MultipleDownload)download;
 
                 for (final SingleDownload singleDownload : multipleDownload.getDownloads()) {
                     singleDownload.deleteObserver(this);
                     singleDownload.deleteObserver(multipleDownload);
                 }
             }
         }
 
         notifyDownloadListChanged(new DownloadListChangedEvent(
                 this,
                 downloadsRemoved,
                 DownloadListChangedEvent.Action.REMOVED));
         notifyDownloadListChanged(new DownloadListChangedEvent(
                 this,
                 downloadsRemoved,
                 DownloadListChangedEvent.Action.CHANGED_COUNTERS));
     }
 
     /**
      * Remove a specified download from the download list.
      *
      * @param  download  The download to remove.
      */
     public synchronized void removeDownload(final Download download) {
         downloads.remove(download);
         download.deleteObserver(this);
 
         if (download instanceof SingleDownload) {
             downloadsToStart.remove((SingleDownload)download);
         } else if (download instanceof MultipleDownload) {
             final MultipleDownload multipleDownload = (MultipleDownload)download;
 
             for (final SingleDownload singleDownload : multipleDownload.getDownloads()) {
                 singleDownload.deleteObserver(this);
                 singleDownload.deleteObserver(multipleDownload);
                 downloadsToStart.remove(singleDownload);
             }
         }
 
         countDownloadsTotal--;
         switch (download.getStatus()) {
             case COMPLETED_WITH_ERROR: {
                 countDownloadsErraneous--;
                 break;
             }
             case COMPLETED: {
                 countDownloadsCompleted--;
                 break;
             }
         }
 
         notifyDownloadListChanged(new DownloadListChangedEvent(
                 this,
                 download,
                 DownloadListChangedEvent.Action.REMOVED));
         notifyDownloadListChanged(new DownloadListChangedEvent(
                 this,
                 download,
                 DownloadListChangedEvent.Action.CHANGED_COUNTERS));
     }
 
     /**
      * Starts pending downloads.
      */
     private synchronized void startDownloads() {
         int downloadsRunning = countDownloadsRunning;
         final Iterator<SingleDownload> downloadToStartIter = downloadsToStart.iterator();
 
         while (downloadToStartIter.hasNext() && (downloadsRunning < parallelDownloads)) {
             final SingleDownload downloadToStart = downloadToStartIter.next();
 
             if (downloadToStart.getStatus().equals(Download.State.WAITING)) {
                downloadToStart.startDownload();
                 downloadsRunning++;
                 downloadToStartIter.remove();
             }
         }
     }
 
     /**
      * Returns the current download list.
      *
      * @return  The current download list.
      */
     public Collection<Download> getDownloads() {
         return downloads;
     }
 
     /**
      * Returns the count of erraneous downloads.
      *
      * @return  The count of erraneous downloads.
      */
     public int getCountDownloadsErraneous() {
         return countDownloadsErraneous;
     }
 
     /**
      * Returns the count of completed downloads.
      *
      * @return  The count of completed downloads.
      */
     public int getCountDownloadsCompleted() {
         return countDownloadsCompleted;
     }
 
     /**
      * Returns the total count of downloads.
      *
      * @return  The total count of downloads.
      */
     public int getCountDownloadsTotal() {
         return countDownloadsTotal;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public int getParallelDownloads() {
         return parallelDownloads;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  parallelDownloads  DOCUMENT ME!
      */
     public void setParallelDownloads(final int parallelDownloads) {
         this.parallelDownloads = parallelDownloads;
     }
 
     /**
      * Returns the destination directory.
      *
      * @return  The destination directory for downloads.
      */
     public File getDestinationDirectory() {
         return destinationDirectory;
     }
 
     /**
      * Sets the destination directory for downloads. Setting this does only affect new downloads. If the given download
      * location is invalid, the DownloadManager will be disabled.
      *
      * @param  destinationDirectory  The new destination directory for downloads.
      */
     public void setDestinationDirectory(final File destinationDirectory) {
         this.destinationDirectory = destinationDirectory;
 
         if (!destinationDirectory.isDirectory() || !destinationDirectory.canWrite()) {
             LOG.error("The download manager can't use the directory '" + destinationDirectory.getAbsolutePath()
                         + "'. The download manager will be disabled.");
             enabled = false;
         } else {
             enabled = true;
         }
     }
 
     /**
      * Returns a flag which tells whether download manager is enabled or not.
      *
      * @return  The flag whether the download manager is enabled or not.
      */
     public boolean isEnabled() {
         return enabled;
     }
 
     @Override
     public synchronized void update(final Observable o, final Object arg) {
         if (!(o instanceof Download)) {
             return;
         }
 
         final Download download = (Download)o;
 
         switch (download.getStatus()) {
             case COMPLETED: {
                 if (download instanceof SingleDownload) {
                     countDownloadsRunning--;
                 }
 
                 if (downloads.contains(download)) {
                     countDownloadsCompleted++;
                 }
 
                 startDownloads();
 
                 break;
             }
             case COMPLETED_WITH_ERROR: {
                 if (download instanceof SingleDownload) {
                     countDownloadsRunning--;
                 }
 
                 if (downloads.contains(download)) {
                     countDownloadsErraneous++;
                 }
 
                 startDownloads();
 
                 break;
             }
             case RUNNING: {
                 if (download instanceof SingleDownload) {
                     countDownloadsRunning++;
                 }
 
                 break;
             }
         }
 
         notifyDownloadListChanged(new DownloadListChangedEvent(
                 this,
                 download,
                 DownloadListChangedEvent.Action.CHANGED_COUNTERS));
     }
 
     /**
      * Adds a new DownloadListChangedListener.
      *
      * @param  listener  The listener to add.
      */
     public void addDownloadListChangedListener(final DownloadListChangedListener listener) {
         listeners.add(DownloadListChangedListener.class, listener);
     }
 
     /**
      * Removes a DownloadListChangedListener.
      *
      * @param  listener  The listener to remove.
      */
     public void removeDownloadListChangedListener(final DownloadListChangedListener listener) {
         listeners.remove(DownloadListChangedListener.class, listener);
     }
 
     /**
      * Notifies all current DownloadListChangedListeners.
      *
      * @param  event  The event to notify about.
      */
     protected synchronized void notifyDownloadListChanged(final DownloadListChangedEvent event) {
         for (final DownloadListChangedListener listener : listeners.getListeners(DownloadListChangedListener.class)) {
             listener.downloadListChanged(event);
         }
     }
 
     @Override
     public void configure(final Element parent) {
         DownloadManagerDialog.setAskForJobname(true);
         DownloadManagerDialog.setJobname("");
         DownloadManagerDialog.setOpenAutomatically(true);
 
         destinationDirectory = new File(System.getProperty("user.home") + System.getProperty("file.separator")
                         + "cidsDownload");
 
         Element downloads = null;
         if (parent == null) {
             LOG.warn("The download manager isn't configured. Using default values.");
         } else {
             downloads = parent.getChild(XML_CONF_ROOT);
         }
 
         if (downloads == null) {
             LOG.warn("The download manager isn't configured. Using default values.");
 
             if (!destinationDirectory.isDirectory() || !destinationDirectory.canWrite()) {
                 LOG.error("The download manager can't use the directory '" + destinationDirectory.getAbsolutePath()
                             + "'. The download manager will be disabled.");
                 enabled = false;
             } else {
                 enabled = true;
             }
 
             return;
         }
 
         final Element directory = downloads.getChild(XML_CONF_DIRECTORY);
         if ((directory == null) || (directory.getTextTrim() == null)) {
             LOG.warn("There is no destination directory configured for downloads. Using default destination directory '"
                         + System.getProperty("user.home") + System.getProperty("file.separator") + "cidsDownload'.");
         } else {
             destinationDirectory = new File(directory.getTextTrim());
         }
         if (!destinationDirectory.isDirectory() || !destinationDirectory.canWrite()) {
             LOG.error("The download manager can't use the directory '" + destinationDirectory.getAbsolutePath()
                         + "'. The download manager will be disabled.");
             enabled = false;
         } else {
             enabled = true;
         }
 
         final Element parallelDownloads = downloads.getChild(XML_CONF_PARALLEL_DOWNLOADS);
         if ((parallelDownloads == null) || (parallelDownloads.getTextTrim() == null)) {
             LOG.warn("There is no limit for parallel downloads configured. Using default limit '2'.");
         } else {
             try {
                 this.parallelDownloads = Integer.parseInt(parallelDownloads.getText());
             } catch (NumberFormatException e) {
                 LOG.warn("Configuration for limit of parallel downloads is invalid. Using default value of '2'", e);
                 this.parallelDownloads = 2;
             }
         }
 
         final Element dialog = downloads.getChild(XML_CONF_DIALOG);
         if (dialog == null) {
             LOG.warn("The download dialog isn't configured. Using default values.");
             return;
         }
 
         final Element askForTitle = dialog.getChild(XML_CONF_DIALOG_AKSFORTITLE);
         if ((askForTitle == null) || (askForTitle.getTextTrim() == null)) {
             LOG.warn(
                 "There is no configuration whether to ask for download titles or not. Using default value 'true'.");
         } else {
             final String value = askForTitle.getTextTrim();
             DownloadManagerDialog.setAskForJobname("1".equals(value) || "true".equalsIgnoreCase(value));
         }
 
         final Element openAutomatically = dialog.getChild(XML_CONF_DIALOG_OPENAUTOMATICALLY);
         if ((openAutomatically == null) || (openAutomatically.getTextTrim() == null)) {
             LOG.warn(
                 "There is no configuration whether to open downloads automatically or not. Using default value 'true'.");
         } else {
             final String value = openAutomatically.getTextTrim();
             DownloadManagerDialog.setOpenAutomatically("1".equals(value) || "true".equalsIgnoreCase(value));
         }
 
         final Element closeAutomatically = dialog.getChild(XML_CONF_DIALOG_CLOSEAUTOMATICALLY);
         if ((closeAutomatically == null) || (closeAutomatically.getTextTrim() == null)) {
             LOG.warn(
                 "There is no configuration whether to close the download manager dialog automatically or not. Using default value 'true'.");
         } else {
             final String value = closeAutomatically.getTextTrim();
             DownloadManagerDialog.setCloseAutomatically("1".equals(value) || "true".equalsIgnoreCase(value));
         }
 
         final Element userTitle = dialog.getChild(XML_CONF_DIALOG_USERTITLE);
         if ((userTitle == null) || (userTitle.getTextTrim() == null)) {
             LOG.warn("There is no user title for downloads configured. Using default value 'cidsDownload'.");
         } else {
             DownloadManagerDialog.setJobname(userTitle.getTextTrim());
         }
     }
 
     @Override
     public void masterConfigure(final Element parent) {
         // NOP
     }
 
     @Override
     public Element getConfiguration() throws NoWriteError {
         final Element root = new Element(XML_CONF_ROOT);
 
         final Element directory = new Element(XML_CONF_DIRECTORY);
         directory.addContent(destinationDirectory.getAbsolutePath());
 
         final Element dialog = new Element(XML_CONF_DIALOG);
 
         final Element parallelDownloads = new Element(XML_CONF_PARALLEL_DOWNLOADS);
         parallelDownloads.addContent(String.valueOf(this.parallelDownloads));
 
         final Element askForTitle = new Element(XML_CONF_DIALOG_AKSFORTITLE);
         askForTitle.addContent(DownloadManagerDialog.isAskForJobname() ? "true" : "false");
 
         final Element openAutomatically = new Element(XML_CONF_DIALOG_OPENAUTOMATICALLY);
         openAutomatically.addContent(DownloadManagerDialog.isOpenAutomatically() ? "true" : "false");
 
         final Element closeAutomatically = new Element(XML_CONF_DIALOG_CLOSEAUTOMATICALLY);
         closeAutomatically.addContent(DownloadManagerDialog.isCloseAutomatically() ? "true" : "false");
 
         final Element userTitle = new Element(XML_CONF_DIALOG_USERTITLE);
         userTitle.addContent(DownloadManagerDialog.getJobname());
 
         dialog.addContent(askForTitle);
         dialog.addContent(openAutomatically);
         dialog.addContent(closeAutomatically);
         dialog.addContent(userTitle);
 
         root.addContent(directory);
         root.addContent(parallelDownloads);
         root.addContent(dialog);
 
         return root;
     }
 }
