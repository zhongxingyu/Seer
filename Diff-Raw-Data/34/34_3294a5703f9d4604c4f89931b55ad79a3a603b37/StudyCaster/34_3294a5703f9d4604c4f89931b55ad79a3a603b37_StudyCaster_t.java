 package no.ebakke.studycaster.applications;
 
 import no.ebakke.studycaster.ui.UIUtil;
 import no.ebakke.studycaster.backend.SingleInstanceHandler;
 import no.ebakke.studycaster.backend.EnvironmentHooks;
 import java.awt.AWTException;
 import java.awt.EventQueue;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.awt.event.WindowListener;
 import java.io.File;
 import java.io.IOException;
 import java.util.Arrays;
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.jnlp.SingleInstanceListener;
 import javax.swing.JOptionPane;
 import javax.swing.SwingUtilities;
 import no.ebakke.studycaster.backend.ServerContext;
 import no.ebakke.studycaster.backend.ServerContextUtil;
 import no.ebakke.studycaster.backend.StudyCasterException;
 import no.ebakke.studycaster.configuration.ConcludeConfiguration;
 import no.ebakke.studycaster.configuration.OpenFileConfiguration;
 import no.ebakke.studycaster.configuration.OpenURIConfiguration;
 import no.ebakke.studycaster.configuration.StudyConfiguration;
 import no.ebakke.studycaster.configuration.UIStringKey;
 import no.ebakke.studycaster.configuration.UploadConfiguration;
 import no.ebakke.studycaster.screencasting.ScreenCensor;
 import no.ebakke.studycaster.ui.MainFrame;
 import no.ebakke.studycaster.ui.MainFrame.UserActionListener;
 import no.ebakke.studycaster.screencasting.ScreenRecorder;
 import no.ebakke.studycaster.screencasting.ScreenRecorderConfiguration;
 import no.ebakke.studycaster.ui.ConfirmationCodeDialogPanel;
 import no.ebakke.studycaster.ui.UploadDialogPanel;
 import no.ebakke.studycaster.util.Util;
 import no.ebakke.studycaster.util.Util.CallableExt;
 import no.ebakke.studycaster.util.stream.NonBlockingOutputStream;
 import no.ebakke.studycaster.util.stream.NonBlockingOutputStream.StreamProgressObserver;
 
 /*
   Manual test cases for this class and its dependees:
   * Multiple application launches before and after configuration is loaded.
   * Close window before or after configuration is loaded, including right before the configuration
     is loaded such that the closing confirmation dialog still appears.
   * Failsafe shutdown 10 seconds after window is closed.
   * Regular shutdown after upload.
   * Regular shutdown after window closure.
   * Invoked with missing configuration ID property.
   * Configuration file error.
   * Window closed when error dialog is due to appear.
   * 1, 2, or 3 pages in the study configuration.
   * A study configuration with or without two action buttons on a single page in the study
     configuration.
   * Opening an example document in JRE 1.5 and in JRE 1.6 or above.
   * Opening an example document that already exists in the temporary folder, either modified or not,
     and either as the first time for a particular launch or not. When a file already exists in
     modified form, different messages should appear depending on whether it is the first time that
     file has been opened during the current launch or not.
   * Opening an example document of an association that cannot be resolved.
   * Opening an example document requiring renaming of a locked already existing file.
   * Opening an example document that does not already exist in the temporary folder.
   * Opening an example document while it's already opened exclusively (e.g. by Excel or Acrobat
     Reader).
   * Opening an example Excel spreadsheet, and correctly determining whether it has been modified by
     the user or not. (Excel will modify files upon opening them even before the user has done
     anything.)
   * Canceling rename dialog upon open.
   * Unexpected error while opening an example document (e.g. file is deleted during rename succeeded
     dialog).
   * Uploading downloaded but unmodified default file.
   * Uploading a file remaining from a previous session but not opened in the current session.
   * Uploading with an empty provided path.
   * Uploading with non-existent default file.
   * Uploading locked file.
   * Uploading without default file existing or having been opened in the session before.
   * Verifying that upload dialog path remains the same between errors and gets reset when opened
     manually again.
   * Concluding with or without file upload.
   * Focus on correct button upon startup.
   * Focus on correct button when moving back and forth between pages.
   * Focus retained on previous action button after canceled action or action with error.
   * Focus advances to next logical button after successful action.
   * Confirmation code is automatically copied to clipboard.
   * Screencast upload progress bar.
   * Closing the main frame before the screencast has finished uploading.
 */
 
 /** Except where noted, methods in this class, including private ones, must be called from the
 event-dispatching thread (EDT) only. */
 public final class StudyCaster {
   private static final int RECORDING_BUFFER_SZ = 4 * 1024 * 1024;
   private static final Logger LOG = Logger.getLogger("no.ebakke.studycaster");
   private static final String CONFIGID_PROP_NAME = "studycaster.config.id";
   private final MainFrame mainFrame;
   private final EnvironmentHooks hooks;
   private final WindowListener windowClosingListener;
   /** Some variables are accessed from multiple threads. Rather than try to remember which ones,
   declare them all volatile. */
   private volatile Thread initializerThread, failsafeCloseThread, backendCloseThread;
   private volatile ScreenRecorder recorder;
   private volatile ServerContext serverContext;
   private volatile StudyConfiguration configuration;
   private volatile NonBlockingOutputStream recordingStream;
   private final StreamProgressObserver streamProgressObserver = new StreamProgressObserver() {
       public void updateProgress(final NonBlockingOutputStream nbos) {
         SwingUtilities.invokeLater(new Runnable() {
           public void run() {
             // A workaround to support the longs returned by NonBlockingOutputStream methods.
             double fraction = ((double) nbos.getBytesWritten()) / ((double) nbos.getBytesPosted());
             final int STEPS = 1000000;
             mainFrame.setProgressBarBounds(0, STEPS);
             mainFrame.setProgressBarValue((int) (STEPS * fraction));
           }
         });
       }
     };
 
   private StudyCaster(EnvironmentHooks hooks) {
     this.hooks = hooks;
     mainFrame = new MainFrame(new PrivateUserActionListener());
     windowClosingListener = new WindowAdapter() {
       @Override
       public void windowClosing(WindowEvent e) {
         final boolean doClose;
         if (configuration != null) {
           LOG.info("User tried to close main StudyCaster window");
           doClose = JOptionPane.showConfirmDialog(mainFrame.getPositionDialog(),
               getUIString(UIStringKey.DIALOG_CLOSE_MESSAGE),
               getUIString(UIStringKey.DIALOG_CLOSE_TITLE),
               JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.OK_OPTION;
           if (doClose) {
             LOG.info("User confirmed closing of main StudyCaster window");
           } else {
             LOG.info("User canceled closing of main StudyCaster window");
           }
         } else {
           doClose = true;
           LOG.info("User closed main StudyCaster window before configuration was loaded; exiting");
         }
         if (doClose)
           closeUIandBackend();
       }
     };
     mainFrame.addWindowListener(windowClosingListener);
   }
 
   /** This method may be called from any thread. */
   private String getUIString(UIStringKey key) {
     return configuration.getUIStrings().getString(key);
   }
 
   /** This method may be called from any thread. */
   private String getUIString(UIStringKey key, Object parameters[]) {
     return configuration.getUIStrings().getString(key, parameters);
   }
 
   // TODO: Rename, refactor, or combine closeUI() and closeBackend().
   // Before calling this function, consider whether the user may already have closed the window.
   private void closeUIandBackend() {
     closeUI();
     closeBackend(null);
   }
 
   private void closeUI() {
     LOG.info("Closing UI");
     if (failsafeCloseThread != null)
       throw new IllegalStateException("UI already closed");
     mainFrame.removeWindowListener(windowClosingListener);
     mainFrame.setVisible(false);
     failsafeCloseThread = new Thread(new Runnable() {
       public void run() {
         try {
           Thread.sleep(7000);
           LOG.warning("Forcing exit in three seconds (this may be last log message)");
           Thread.sleep(3000);
           LOG.warning("Forcing exit ten seconds after window closure");
         } catch (InterruptedException e) {
           LOG.warning("Failsafe exit thread interrupted; exiting immediately");
         }
         System.exit(1);
       }
     }, "StudyCaster-failsafeClose");
     // Don't keep the VM running just because of the failsafe thread.
     failsafeCloseThread.setDaemon(true);
     failsafeCloseThread.start();
     /* Attempt to avoid race conditions that might make the window displayable again after the
     call to dispose() by putting the call at the end of the event queue. */
     SwingUtilities.invokeLater(new Runnable() {
       public void run() {
         mainFrame.dispose();
       }
     });
   }
 
   private void closeBackend(final Runnable onEndEDT) {
     if (backendCloseThread != null)
       return;
     LOG.info("Closing backend");
     final Thread initializerThreadF = initializerThread;
     backendCloseThread = new Thread(new Runnable() {
       public void run() {
         if (initializerThreadF != null) {
           Util.ensureInterruptible(new Util.Interruptible() {
             public void run() throws InterruptedException {
               initializerThreadF.join();
             }
           });
         }
         if (recorder != null) {
           try {
             if (onEndEDT != null) {
               recorder.stop();
               recordingStream.addObserver(streamProgressObserver);
             }
             recorder.close();
             if (onEndEDT != null)
               recordingStream.removeObserver(streamProgressObserver);
           } catch (IOException e) {
             LOG.log(Level.SEVERE, "Error while closing screen recorder", e);
           }
         }
         EnvironmentHooks.shutdown();
         // In the case of an error, serverContext may still not be defined.
         if (serverContext != null)
           serverContext.close();
         if (onEndEDT != null)
           SwingUtilities.invokeLater(onEndEDT);
       }
     }, "StudyCaster-backendClose");
     backendCloseThread.start();
   }
 
   private void reportHelperEDT(
         final String message, final String title, final int messageType, final boolean fatal)
   {
     if (failsafeCloseThread != null) {
       LOG.log(Level.INFO, "Suppressing dialog due to earlier close action");
       return;
     }
     mainFrame.stopTask(false);
     JOptionPane.showMessageDialog(mainFrame.getPositionDialog(), message, title, messageType);
     if (fatal)
       closeUIandBackend();
   }
 
   private void reportHelper(
       final String message, final String title, final int messageType, final boolean fatal)
   {
     if (EventQueue.isDispatchThread()) {
       reportHelperEDT(message, title, messageType, fatal);
     } else {
       try {
         Util.checkedSwingInvokeAndWait(new CallableExt<Void,RuntimeException>() {
           public Void call() {
             reportHelperEDT(message, title, messageType, fatal);
             return null;
           }
         });
       } catch (InterruptedException e) {
         LOG.warning("showMessageDialog() interrupted");
         Thread.currentThread().interrupt();
       }
     }
   }
 
   /** Displays a parameterized modal message dialog. The MainFrame taskbar and button states will be
   reset. The method blocks and can be called from any thread, including the EDT. */
   private void reportMessage(final UIStringKey messageKey,
       final Object messageParameters[], final UIStringKey titleKey, final int messageType)
   {
     final String message = (messageParameters != null) ?
         getUIString(messageKey, messageParameters) : getUIString(messageKey);
     LOG.log(Level.INFO, "Showing dialog with message key {0}", messageKey.toString());
     reportHelper(message, getUIString(titleKey), messageType, false);
   }
 
   /** Displays a modal dialog for reporting an exception. The MainFrame taskbar will be reset. The
   method blocks and can be called from any thread, including the EDT. */
   private void reportError(Exception e, boolean fatal) {
     LOG.log(Level.SEVERE, fatal ? "Fatal error " : "Generic non-fatal error ", e);
     reportHelper("There was an unexpected error:\n" + e.getMessage(),
         "Error", JOptionPane.ERROR_MESSAGE, fatal);
   }
 
   private void initUI(StudyCasterException storedException) {
     try {
       if (storedException != null)
         throw storedException;
       if (failsafeCloseThread != null) {
         LOG.info("Window closed before UI was configured");
         /* Explicitly return here as the operations below may make the window displayable again,
         neutralizing an earlier dispose(). */
         return;
       }
       mainFrame.stopTask(false);
       mainFrame.configure(configuration, serverContext);
 
       // Do this after properly setting up the main window, in case there are enqueued messages.
       SingleInstanceHandler sih = hooks.getSingleInstanceHandler();
       if (sih != null) {
         sih.setListener(new SingleInstanceListener() {
           public void newActivation(String[] strings) {
             reportMessage(UIStringKey.DIALOG_ALREADY_RUNNING_MESSAGE, null,
                 UIStringKey.DIALOG_ALREADY_RUNNING_TITLE, JOptionPane.INFORMATION_MESSAGE);
           }
         });
       }
       LOG.info("Showing consent dialog");
       int res = JOptionPane.showOptionDialog(mainFrame.getPositionDialog(),
           getUIString(UIStringKey.DIALOG_CONSENT_QUESTION),
           getUIString(UIStringKey.DIALOG_CONSENT_TITLE), JOptionPane.OK_CANCEL_OPTION,
           JOptionPane.QUESTION_MESSAGE, null, null, null);
       if (res != JOptionPane.OK_OPTION) {
         LOG.info("User declined at consent dialog");
         closeUIandBackend();
      } else {
        /* This is probably overkill, but ScreenRecorder.start() may theoretically block. */
        mainFrame.startTask(null, true);
        new Thread(new Runnable() {
          public void run() {
            recorder.start();
            SwingUtilities.invokeLater(new Runnable() {
              public void run() {
                mainFrame.stopTask(false);
              }
            });
          }
        }, "StudyCaster-startRecording").start();
       }
     } catch (StudyCasterException e) {
       // Note: Due to the exception, configuration and serverContext may not be defined.
       reportError(e, true);
     }
   }
 
   /** This method may only be called once during the lifetime of a StudyUI object. */
   public void runStudy(final String args[]) {
     if (initializerThread != null)
       throw new IllegalStateException("Already started");
     initializerThread = new Thread(new Runnable() {
       public void run() {
         StudyCasterException exception = null;
         try {
           serverContext = new ServerContext();
           hooks.getLogFormatter().setServerTimeSource(serverContext.getServerTimeSource());
           try {
             hooks.getConsoleStream().connect(serverContext.uploadFile("console.txt"));
             final String configurationID = System.getProperty(CONFIGID_PROP_NAME);
             if (configurationID == null)
               throw new StudyCasterException("Unspecified configuration ID");
             configuration = StudyConfiguration.parseConfiguration(
               serverContext.downloadFile("studyconfig.xml"), configurationID);
             LOG.log(Level.INFO, "Loaded configuration with name \"{0}\"", configuration.getName());
 
             // Prepare the screen recorder without starting it yet.
             recordingStream = new NonBlockingOutputStream(RECORDING_BUFFER_SZ);
             recordingStream.connect(serverContext.uploadFile("screencast.ebc"));
             try {
               recorder = new ScreenRecorder(recordingStream, serverContext.getServerTimeSource(),
                   ScreenRecorderConfiguration.DEFAULT);
             } catch (AWTException e) {
               throw new StudyCasterException("Failed to initialize screen recorder", e);
             }
             List<String> screenCastBlacklist = configuration.getScreenCastBlacklist();
             screenCastBlacklist.add(getUIString(UIStringKey.DIALOG_CONCLUDE_TITLE));
             screenCastBlacklist.add(getUIString(UIStringKey.DIALOG_OPEN_FILE_TITLE));
             recorder.setCensor(new ScreenCensor(
                 configuration.getScreenCastWhitelist(), screenCastBlacklist,
                 true, true, true));
           } catch (IOException e) {
             throw new StudyCasterException("Unexpected I/O error", e);
           }
         } catch (StudyCasterException e) {
           exception = e;
         }
         final StudyCasterException exceptionF = exception;
         SwingUtilities.invokeLater(new Runnable() {
           public void run() {
             initUI(exceptionF);
           }
         });
       }
     }, "StudyCaster-runStudy");
 
     /* To avoid a race condition with closeBackend(), initializerThread must be initialized before
     the UI can be displayed, and started after the UI has been displayed. */
     /* TODO: If configuration files ever get parsed on the server side, consider including the
     initial few UI strings as parameters in the JNLP file. */
     mainFrame.startTask(null, true);
     mainFrame.setVisible(true);
     initializerThread.start();
   }
 
   public static void main(final String args[]) {
     final EnvironmentHooks hooks = EnvironmentHooks.create();
 
     // Must be called before any UI components are rendered.
     try {
       UIUtil.setSystemLookAndFeel();
     } catch (StudyCasterException e) {
       LOG.log(Level.INFO, "Couldn't set system L&F", e);
     }
 
     SwingUtilities.invokeLater(new Runnable() {
       public void run() {
         new StudyCaster(hooks).runStudy(args);
       }
     });
   }
 
   private class PrivateUserActionListener implements UserActionListener {
     /** No real concurrency should happen, but these maps are accessed from different threads. */
     private final Map<String,byte[]> hashesBeforeOpen = new ConcurrentHashMap<String,byte[]>();
     private final Map<String,byte[]> hashesAfterOpen  = new ConcurrentHashMap<String,byte[]>();
     private final File openFileDirectory;
 
     PrivateUserActionListener() {
       File rawOpenFileDirectory = new File(System.getProperty("java.io.tmpdir"));
       try {
         /* I/O operations should generally not go on the EDT, but this is a tiny one, and we do it
         in the constructor only once, before the main frame is shown. */
         rawOpenFileDirectory = rawOpenFileDirectory.getCanonicalFile();
       } catch (IOException e) {
         // Canonicalization of the path is only for cosmetic reasons, so take no action.
       }
       openFileDirectory = rawOpenFileDirectory;
     }
 
     private File getOpenFileDirectory() {
       return openFileDirectory;
     }
 
     private File getOpenFilePath(OpenFileConfiguration openFileConfiguration) {
       return new File(getOpenFileDirectory(), openFileConfiguration.getClientName());
     }
 
     private boolean fileIsModified(OpenFileConfiguration openFileConfiguration, File otherFile)
         throws IOException
     {
       final String key = openFileConfiguration.getClientName();
       byte[] otherHash = Util.computeSHA1(otherFile);
       byte[] hashAfterOpen = hashesAfterOpen.get(key);
       if (hashAfterOpen != null && Arrays.equals(otherHash, hashAfterOpen))
         return false;
       if (!hashesBeforeOpen.containsKey(key))
         downloadFile(openFileConfiguration).delete();
       byte[] hashBeforeOpen = hashesBeforeOpen.get(key);
       return !Arrays.equals(otherHash, hashBeforeOpen);
     }
 
     private File downloadFile(OpenFileConfiguration openFileConfiguration) throws IOException {
       final String key = openFileConfiguration.getClientName();
       File ret = File.createTempFile("sc_", ".tmp", getOpenFileDirectory());
       try {
         ServerContextUtil.downloadFile(serverContext, openFileConfiguration.getServerName(), ret);
         if (!hashesBeforeOpen.containsKey(key))
           hashesBeforeOpen.put(key, Util.computeSHA1(ret));
       } catch (IOException e) {
         ret.delete();
         throw e;
       }
       return ret;
     }
 
     /** Should not be called on the EDT. */
     private boolean openFileActionHelper(OpenFileConfiguration openFileConfiguration)
         throws StudyCasterException
     {
       final String key = openFileConfiguration.getClientName();
       final boolean openedInThisSession = hashesBeforeOpen.containsKey(key);
       File renameNewName = null;
       File downloadedFile = null;
       try {
         LOG.log(Level.INFO, "Open action for file \"{0}\"", key);
         final File clientFile = getOpenFilePath(openFileConfiguration);
         if (!Util.fileAvailableExclusive(clientFile)) {
           reportMessage(UIStringKey.DIALOG_OPEN_FILE_ALREADY_MESSAGE,
               new Object[] { Util.getPathString(clientFile) }, UIStringKey.DIALOG_OPEN_FILE_TITLE,
               JOptionPane.INFORMATION_MESSAGE);
           return false;
         }
         final boolean modified, useDownloaded;
         if (!clientFile.exists()) {
           useDownloaded = true;
           modified      = false;
         } else {
           /* Small optimization: call downloadFile() before fileModified() to avoid downloading the
           file both via fileModified() and the other call to downloadFile() below. */
           downloadedFile = downloadFile(openFileConfiguration);
           if (!fileIsModified(openFileConfiguration, clientFile)) {
             LOG.info("File already exists in unmodified form");
             // The existing file is identical to the downloaded one; so use the existing one.
             useDownloaded = false;
             modified      = false;
           } else {
             LOG.info("File already exists in modified form, showing option dialog");
             // The existing file is different from the downloaded one; ask the user what to do.
             int res = Util.checkedSwingInvokeAndWait(new Util.CallableExt<Integer,RuntimeException>() {
               public Integer call() {
                 final String downloadOption = getUIString(UIStringKey.DIALOG_OPEN_FILE_NEW_BUTTON);
                 final String existingOption = getUIString(UIStringKey.DIALOG_OPEN_FILE_KEEP_BUTTON);
                 int ret = JOptionPane.showOptionDialog(mainFrame.getPositionDialog(),
                     getUIString(
                       openedInThisSession ? UIStringKey.DIALOG_OPEN_FILE_MODIFIED_MESSAGE
                                           : UIStringKey.DIALOG_OPEN_FILE_EXISTING_MESSAGE,
                       new Object[] { Util.getPathString(clientFile) }),
                     getUIString(UIStringKey.DIALOG_OPEN_FILE_TITLE),
                     JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE,
                     null, new Object[] {downloadOption, existingOption}, existingOption);
                 return ret;
               }
             });
             if (res == JOptionPane.CLOSED_OPTION) {
               LOG.info("User closed option window");
               return false;
             }
             useDownloaded = (res == JOptionPane.YES_OPTION);
             modified = !useDownloaded;
             if (useDownloaded) {
               // Move the old file out of place without deleting it.
               String path = clientFile.getPath();
               int dot = path.lastIndexOf('.');
               String basename = (dot < 0) ? path : path.substring(0, dot);
               String extension = (dot < 0) ? "" : path.substring(dot);
               int index = 1;
               do {
                 renameNewName = new File(basename + " (" + index + ")" + extension);
                 index++;
               } while (renameNewName.exists());
               if (!clientFile.renameTo(renameNewName)) {
                 reportMessage(UIStringKey.DIALOG_OPEN_FILE_RENAME_FAILED_MESSAGE,
                     new Object[] { Util.getPathString(clientFile) },
                     UIStringKey.DIALOG_OPEN_FILE_TITLE, JOptionPane.WARNING_MESSAGE);
                 // Leave it up to the user to try again.
                 return false;
               }
             }
           }
         }
         if (useDownloaded) {
           if (downloadedFile == null)
             downloadedFile = downloadFile(openFileConfiguration);
           /* An error here is unexpected, as any existing file should already have been moved out of
           the way at this point. */
           if (!downloadedFile.renameTo(clientFile))
             throw new IOException("Failed to rename");
         }
         if (!Util.desktopOpenFile(clientFile)) {
           reportMessage(UIStringKey.DIALOG_OPEN_FILE_ASSOCIATION_FAILED_MESSAGE,
               new Object[] { Util.getPathString(clientFile), openFileConfiguration.getErrorMessage() },
               UIStringKey.DIALOG_OPEN_FILE_TITLE, JOptionPane.WARNING_MESSAGE);
           return false;
         }
         /* Certain applications, such as Microsoft Excel, will modify a file immediately upon
         opening it, possibly in a new way every time. For the purposes of advising the user about
         the status of opened files, consider a file to be unmodified if it matches either the
         originally downloaded file or the file in the state it was a short moment after it was
         opened in an unmodified state (presumably before the user would have had time to change
         it.) */
         if (modified) {
           // Once the file is considered modified, don't use hashAfterOpen for matching anymore.
           hashesAfterOpen.remove(key);
         } else {
           try {
             Thread.sleep(500);
           } catch (InterruptedException e) {
             throw new StudyCasterException(e);
           }
           hashesAfterOpen.put(key, Util.computeSHA1(clientFile));
         }
         if (renameNewName != null) {
           /* Don't show this message until we're done doing everything else, since reportMessage()
           will also re-enable the action buttons. */
           reportMessage(UIStringKey.DIALOG_OPEN_FILE_RENAMED_MESSAGE,
               new Object[] {Util.getPathString(clientFile), Util.getPathString(renameNewName) },
               UIStringKey.DIALOG_OPEN_FILE_TITLE, JOptionPane.INFORMATION_MESSAGE);
         }
       } catch (IOException e) {
         throw new StudyCasterException(e);
       } catch (InterruptedException e) {
         throw new StudyCasterException(e);
       } finally {
         // This file won't exist in all cases, even if downloadedFile != null; that's OK.
         if (downloadedFile != null)
           downloadedFile.delete();
       }
       return true;
     }
 
     /** Callback should return true for a successful task, false for an unsuccessful task, or null
     for an ongoing termination task. */
     private void processAction(final CallableExt<Boolean,StudyCasterException> callable) {
       new Thread(new Runnable() {
         public void run() {
           try {
             final Boolean success = callable.call();
             if (success != null) {
               SwingUtilities.invokeLater(new Runnable() {
                 public void run() {
                   mainFrame.stopTask(success);
                 }
               });
             }
           } catch (final StudyCasterException e) {
             reportError(e, false);
           }
         }
       }, "StudyCaster-action").start();
     }
 
     public void openURIAction(final OpenURIConfiguration openURIConfiguration) {
       mainFrame.startTask(UIStringKey.PROGRESS_OPEN_URI, true);
       processAction(new CallableExt<Boolean,StudyCasterException>() {
         public Boolean call() throws StudyCasterException {
           Util.desktopOpenURI(openURIConfiguration.getURI());
           return true;
         }
       });
     }
 
     public void openFileAction(final OpenFileConfiguration openFileConfiguration) {
       mainFrame.startTask(UIStringKey.PROGRESS_OPEN_FILE, true);
       processAction(new CallableExt<Boolean,StudyCasterException>() {
         public Boolean call() throws StudyCasterException {
           return openFileActionHelper(openFileConfiguration);
         }
       });
     }
 
     /** Should not be called on the EDT. openFileConfiguration may be null. */
     private boolean uploadFileHelper(final OpenFileConfiguration openFileConfiguration,
         final UploadDialogPanel udp)
         throws StudyCasterException, IOException, InterruptedException
     {
       boolean first = true;
       while (true) {
         final boolean firstF = first;
         first = false;
         String selectedFilePath =
             Util.checkedSwingInvokeAndWait(new Util.CallableExt<String,RuntimeException>()
         {
           public String call() {
             if (firstF && openFileConfiguration != null)
               udp.setFilePath(getOpenFilePath(openFileConfiguration).getAbsolutePath());
             mainFrame.stopTask(false);
             /* TODO: Find a workaround for a Swing bug, particularily prevalent in 1.5 (about 1/20th
                      of the time), that causes the dialog contents not to get painted occasionally.
                      See http://bugs.sun.com/view_bug.do?bug_id=6859086 and
                      http://stackoverflow.com/questions/8391554 . */
             LOG.info("Showing upload conclude dialog");
             int res = JOptionPane.showOptionDialog(mainFrame.getPositionDialog(), udp,
                 getUIString(UIStringKey.DIALOG_CONCLUDE_TITLE), JOptionPane.OK_CANCEL_OPTION,
                 JOptionPane.QUESTION_MESSAGE, null, null, null);
             if (res != JOptionPane.OK_OPTION)
               return null;
             mainFrame.startTask(UIStringKey.PROGRESS_UPLOAD_FILE, true);
             return udp.getFilePath();
           }
         });
         if (selectedFilePath == null)
           return false;
         if (selectedFilePath.trim().length() == 0) {
           reportMessage(UIStringKey.DIALOG_CONCLUDE_EMPTY_PATH_MESSAGE, null,
               UIStringKey.DIALOG_CONCLUDE_TITLE, JOptionPane.INFORMATION_MESSAGE);
           continue;
         }
         final File selectedFile = new File(selectedFilePath);
         if (!selectedFile.exists()) {
           reportMessage(UIStringKey.DIALOG_CONCLUDE_FILE_NOT_EXISTS_MESSAGE,
               new Object[] { selectedFile.getAbsolutePath() },
               UIStringKey.DIALOG_CONCLUDE_TITLE, JOptionPane.WARNING_MESSAGE);
           continue;
         }
         if (!Util.fileAvailableExclusive(selectedFile)) {
           reportMessage(UIStringKey.DIALOG_CONCLUDE_FILE_OPEN_MESSAGE,
               new Object[] { selectedFile.getAbsolutePath() },
               UIStringKey.DIALOG_CONCLUDE_TITLE, JOptionPane.INFORMATION_MESSAGE);
           continue;
         }
         if (!fileIsModified(openFileConfiguration, selectedFile)) {
           reportMessage(UIStringKey.DIALOG_CONCLUDE_FILE_NOT_MODIFIED_MESSAGE,
               new Object[] { selectedFile.getAbsolutePath() },
               UIStringKey.DIALOG_CONCLUDE_TITLE, JOptionPane.INFORMATION_MESSAGE);
           continue;
         }
         ServerContextUtil.uploadFile(serverContext, selectedFile,
             "upload_" + Util.sanitizeFileNameComponent(selectedFile.getName()));
         return true;
       }
     }
 
     /** Must be called on the EDT. */
     public void concludeHelperEDT() {
       mainFrame.startTask(UIStringKey.PROGRESS_UPLOAD_SCREENCAST, false);
       closeBackend(new Runnable() {
         public void run() {
           mainFrame.stopTask(false);
           ConfirmationCodeDialogPanel ccdp = mainFrame.getConfirmationCodeDialogPanel();
           ccdp.setConfirmationCode(serverContext.getLaunchTicket());
           LOG.info("Showing confirmation code dialog");
           JOptionPane.showMessageDialog(mainFrame.getPositionDialog(), ccdp,
               getUIString(UIStringKey.DIALOG_CONFIRMATION_TITLE), JOptionPane.PLAIN_MESSAGE);
           closeUI();
         }
       });
     }
 
     public void concludeAction(final ConcludeConfiguration concludeConfiguration) {
       if (concludeConfiguration.getUploadConfiguration() != null) {
         /* To avoid a race condition, briefly disable buttons while waiting for the upload dialog to
         appear. */
         mainFrame.startTask(null, false);
         final UploadDialogPanel udp = mainFrame.getUploadDialogPanel();
         final UploadConfiguration uploadConfiguration = concludeConfiguration.getUploadConfiguration();
         udp.setFileFilter(uploadConfiguration.getFileFilter());
         processAction(new CallableExt<Boolean,StudyCasterException>() {
           public Boolean call() throws StudyCasterException {
             OpenFileConfiguration originalFile = uploadConfiguration.getDefaultFile();
             if (originalFile != null && !getOpenFilePath(originalFile).exists()) {
               reportMessage(UIStringKey.DIALOG_CONCLUDE_FILE_NOT_OPENED_MESSAGE, null,
                   UIStringKey.DIALOG_CONCLUDE_TITLE, JOptionPane.INFORMATION_MESSAGE);
               return false;
             }
             try {
               if (uploadFileHelper(originalFile, udp)) {
                 SwingUtilities.invokeLater(new Runnable() {
                   public void run() {
                     concludeHelperEDT();
                   }
                 });
                 return null;
               } else {
                 return false;
               }
             } catch (InterruptedException e) {
               throw new StudyCasterException(e);
             } catch (IOException e) {
               throw new StudyCasterException(e);
             }
           }
         });
       } else {
         LOG.info("Showing conclude confirmation dialog");
         int res = JOptionPane.showConfirmDialog(mainFrame.getPositionDialog(),
             getUIString(UIStringKey.DIALOG_CONCLUDE_QUESTION),
             getUIString(UIStringKey.DIALOG_CONCLUDE_TITLE), JOptionPane.OK_CANCEL_OPTION,
             JOptionPane.QUESTION_MESSAGE);
         if (res != JOptionPane.OK_OPTION)
           return;
         concludeHelperEDT();
       }
     }
   }
 }
