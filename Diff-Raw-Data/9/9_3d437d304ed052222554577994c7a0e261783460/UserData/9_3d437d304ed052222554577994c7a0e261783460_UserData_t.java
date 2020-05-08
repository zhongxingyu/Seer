 package ro.btanase.chordlearning.services;
 
 public interface UserData {
   /**
    * constant used for media folder subdirectory.<br/> 
    * <font color="red">To get the current path for the media folder use getMediaFolder() method instead</font>
    */
   static final String MEDIA_FOLDER = "media"; 
 
   /**
    * constant used for config folder subdirectory.<br/> 
    * <font color="red">To get the current path for the config folder use getConfigFolder method instead</font>
    */
   public final String CONFIG_FOLDER = "config";
 
   
   
   /**
    * retrieves the userFolder. The folder defined by the user to contain configuration & audio files
    * @return
    */
   public String getUserFolder();
 
   /**
    * set "userFolder" as the current active directory where the application will save its data. This method
    * will also write the %user.home%/.gced/application.properties file with the location of user selected folder.
    * @param userFolder
    */
   public void setUserFolder(String userFolder);
 
   /**
    * retrieves the full path to the media folder (where audio files are stored)
    * @return
    */
   public String getMediaFolder();
   
   /**
    * check if the user folder (not the user.home directory) is defined and valid
    * This will also set the userFolder property in case it's defined in %user.home%/.gcet/application.properties
    * @return
    */
   public boolean isUserDirectoryDefined();
   
   /**
    * retrieves the full path to the config folder (where the config database is stored)
    * @return
    */
   public String getConfigFolder();
   
   
   /**
    * Examines the DB Schema and upgrades it if necessary to the latest version
    */
   public void upgradeIfNecessary();
}
