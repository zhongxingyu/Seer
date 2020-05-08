 package com.oshmidt;
 
 import java.io.IOException;
 import java.util.List;
 import java.util.Set;
 
 import org.apache.log4j.Logger;
 import org.eclipse.egit.github.core.Gist;
 import org.eclipse.egit.github.core.GistFile;
 
 /**
  * @author oshmidt
  *         <p>
  *         Class gist manager. Created for centralize user operation with gist.
  */
 public class GistManager {
 
     private static final String START_DOWNLOADING_GISTS = Messages
             .getString("com.oshmidt.gistManager.startDownloadingGists");
     private static final String START_DOWNLOADING_GIST = Messages
             .getString("com.oshmidt.gistManager.startDownloadingGist");
     private static final String START_DOWNLOADING_FILES = Messages
             .getString("com.oshmidt.gistManager.startDownloadingFiles");
     private static final String START_UPDATING_GIST = Messages
             .getString("com.oshmidt.gistManager.startUpdatingGist");
     private static final String START_DELETING_GIST = Messages
             .getString("com.oshmidt.gistManager.startDeletingGist");
     private static final String START_SERIALIZE_FILES = Messages
             .getString("com.oshmidt.gistManager.startSerializingGist");
     private static final String START_DESERIALIZE_FILES = Messages
             .getString("com.oshmidt.gistManager.startDeserializingGist");
     private static final String SEPARATOR = Messages
             .getString("com.oshmidt.gistManager.lineSeparator");
     private static final String NO_GISTS = Messages
             .getString("com.oshmidt.gistManager.noLoadedGists");
     private static final String NEW_GIST = Messages
             .getString("com.oshmidt.gistManager.tryAddNewGist");
     private static final String DESERIALIZE_GISTS_FAIL = Messages
             .getString("com.oshmidt.gistManager.desFails");
     private static final String UN_GIST_ID = Messages
             .getString("com.oshmidt.gistManager.wrongGist");
 
     private List<Gist> gists;
 
     private GistFetcher gistFetcher;
 
     private GistRepository glfm;
 
     private User user;
 
     private Logger logger;
 
     /** GistManager constructor. Initialize self component. */
     public GistManager() {
         user = new User();
         logger = Logger.getLogger(GistManager.class);
         gistFetcher = new GistFetcher();
         glfm = new GistLocalFileManager();
     }
 
     /**
      *Method setting user name and password.
      *
      *@param username
      *           - user name for Github
      *@param password
      *           - password for Github
      */
     public final void initUser(final String username, final String password) {
         user.setLogin(username);
         user.setPassword(password);
     }
 
     /**
      *Method tries import user name and password. Used
      *{@link com.oshmidt.User#importUser()}
      */
     public final void importUser() {
         if (!user.importUser()) {
             System.out.println(Messages
                     .getString("com.oshmidt.gistManager.userDataFileProblem"));
         }
     }
 
     /**
      *Method load gists from Github and save them to local file system. Used
      *{@link com.oshmidt.GistManager#loadGists()}
      *{@link com.oshmidt.GistManager#writeLocalGists()}
      */
     public final void loadAndSaveRemoteGists() {
         loadGists();
         writeLocalGists();
     }
 
     /**
      *Method serialize gists to local file system. Used
      *{@link com.oshmidt.GistLocalFileManager#writeGists(List)}
      */
     public final void writeLocalGists() {
         message(START_SERIALIZE_FILES);
         if (!(gists == null)) {
             glfm.writeGists(gists);
         }
     }
 
     /**
      *Method deserialize gists from local file system. Used
      *{@link com.oshmidt.GistLocalFileManager#readGists()}
      */
     public final void readLocalGists() {
         message(START_DESERIALIZE_FILES);
         gists = glfm.readGists();
         if (gists == null) {
             message(DESERIALIZE_GISTS_FAIL);
         }
     }
 
     /**
      *Method download gist files from Github and save them to local file
      *system. Used {@link com.oshmidt.GistLocalFileManager#writeFiles(Gist)}
      *
      *@param key
      *           - gistId
      */
     public final void downloadGists(final String key) {
         message(START_DOWNLOADING_FILES);
         if (key.equals("all")) {
             for (Gist gist : gists) {
                 glfm.writeFiles(gist);
             }
         } else {
             glfm.writeFiles(findGist(key));
         }
     }
 
     /**
      *Method find and return gist by his ID. Method searching in
      *{@link com.oshmidt.GistManager#gists}. Used
      *
      *@param key
      *           - gistId
      *@return gist - Gist item
      */
     public final Gist findGist(final String key) {
         for (Gist gist : gists) {
             if (gist.getId().equals(key)) {
                 return gist;
             }
         }
         message(UN_GIST_ID);
         return null;
     }
 
     /**
      *Method print to console all loaded gists. Used
      *{@link com.oshmidt.GistManager#showGist(Gist)}
      */
 
     public final void showGists() {
         if (gists != null) {
             for (Gist gist : gists) {
                 showGist(gist);
             }
         } else {
             message(NO_GISTS);
         }
     }
 
     /**
      *Method print to console gist info and his list files.
      *
      *@param gist
      *           - Gist object {@link org.eclipse.egit.github.core.Gist}
      */
     public final void showGist(final Gist gist) {
         System.out.println(SEPARATOR);
        message(Messages.getString("com.oshmidt.gistManager.gistID", gist.getId(), gist.getDescription()));
 
         Set<String> sett = gist.getFiles().keySet();
         int i = 0;
        message(Messages.getString("com.oshmidt.gistManager.gistFiles"));
         for (String s : sett) {
             GistFile gf = gist.getFiles().get(s);
             i++;
             message(i + ": " + gf.getFilename());
         }
 
     }
 
     /**
      *Method tries upload new Gist tu Github.
      *
      *@param gist
      *           - Gist object {@link org.eclipse.egit.github.core.Gist}
      */
     @Deprecated
     public final void addNewGist(final Gist gist) {
         try {
             message(NEW_GIST);
             gistFetcher.addNewGist(user, gist);
         } catch (IOException e) {
             logger.error(e);
         }
     }
 
     /**
      *Method tries download gists from Github.
      */
     public final void loadGists() {
         try {
             message(START_DOWNLOADING_GISTS);
             gists = gistFetcher.loadGists(user);
         } catch (IOException e) {
             logger.error(e);
         }
     }
 
     /**
      *Method download Gist from Github by his ID.
      *
      *@param gistId
      *           - {@link org.eclipse.egit.github.core.Gist#getId()}
      *@return gist - Gist object {@link org.eclipse.egit.github.core.Gist}
      */
     public final Gist loadGist(final String gistId) {
         try {
             message(START_DOWNLOADING_GIST);
             return gistFetcher.loadGist(gistId, user);
         } catch (IOException e) {
             logger.error(e);
             return null;
         }
     }
 
     /**
      *Method send updated Gist object to github.
      *
      *@param gist
      *           - Gist object {@link org.eclipse.egit.github.core.Gist}
      */
     @Deprecated
     public final void updateGist(final Gist gist) {
         try {
             message(START_UPDATING_GIST);
             gistFetcher.updateGist(user, gist);
         } catch (IOException e) {
             logger.error(e);
         }
     }
 
     /**
      *Method delete Gist object from Github.
      *
      *@param gistId
      *           - {@link org.eclipse.egit.github.core.Gist#getId()}
      *@return true if deleting was without exception, false if server return
      *        IOException
      */
     @Deprecated
     public final boolean deleteGist(final String gistId) {
         try {
             message(START_DELETING_GIST);
             gistFetcher.deleteGist(user, gistId);
             return true;
         } catch (IOException e) {
             logger.error(e);
             return false;
         }
     }
 
     /**
      *Transmit message to logger and console.
      *
      *@param message
      *           - String message
      */
     private void message(final String message) {
         logger.info(message);
         System.out.println(message);
     }
 
 }
