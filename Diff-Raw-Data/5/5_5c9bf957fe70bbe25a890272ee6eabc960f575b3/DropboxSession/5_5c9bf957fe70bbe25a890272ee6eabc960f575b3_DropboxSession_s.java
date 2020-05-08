 package base.sessions;
 
 import base.*;
 import com.dropbox.core.*;
 
 import java.awt.*;
 import java.io.*;
 import java.io.File;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Locale;
 
 /**
  * Dropbox session implementation
  *
  * @author ntpeters
  */
 public class DropboxSession implements UDSession {
     private static final String APP_KEY = "a1boy1ymao44h8q";        // App Key to access Dropbox API
     private static final String APP_SECRET = "yfbvljacppmqx6t";     // App Secret to access Dropbox API
     private static final String sessionType = "Dropbox";            // The session type
 
     private DbxAppInfo appInfo;         // Info for the Dropbox application
     private DbxClient client;           // Client connection to the Dropbox API
     private AccountInfo accountInfo;    // Account info for the currently logged in account
     private UFile directoryTree;        // Root node of the tree representing the directory structure
 
     public DropboxSession() {
         appInfo = new DbxAppInfo( APP_KEY, APP_SECRET);
         accountInfo = new AccountInfo();
     }
 
     /**
      * Authenticates a Dropbox session
      *
      * @param userID        The id of the user, such as username, for the current service
      * @return
      * @throws UDException
      */
     @Override
     public String authenticate(String userID) throws UDException {
 
 
         DbxRequestConfig config = new DbxRequestConfig(
                 "UnityDrive/1.0", Locale.getDefault().toString());
         DbxWebAuthNoRedirect webAuth = new DbxWebAuthNoRedirect(config, appInfo);
 
         String code;
         DbxAuthFinish authFinish;
         try {
             Desktop.getDesktop().browse( new URI( webAuth.start() ) );
 
         } catch( URISyntaxException e ) {
             throw new UDException( "Unable to browse to authentication URL", e );
         } catch( IOException e ) {
             throw new UDException( "Unable to open web browser", e );
         }
 
         try {
             code = new BufferedReader(new InputStreamReader(System.in)).readLine().trim();
         } catch( IOException e ) {
             throw new UDException( "Unable to read input stream", e );
         }
 
         try {
             authFinish = webAuth.finish(code);
         } catch( DbxException e ) {
             throw new UDAuthenticationException( "Authentication failed!", e );
         }
 
         client = new DbxClient(config, authFinish.accessToken);
 
         try {
             accountInfo.setTotalSize( client.getAccountInfo().quota.total );
             accountInfo.setUsedSize( client.getAccountInfo().quota.normal );
            accountInfo.setUsername( client.getAccountInfo().displayName );
         } catch( DbxException e ) {
             throw new UDException( "Unable to get account info!", e );
         }
         accountInfo.setSessionType( this.sessionType );
 
         return authFinish.accessToken;
     }
 
     /**
      * Gets the account info for the current logged in user for Dropbox
      *
      * @return              An AccountInfo object containing all relevant user information
      * @throws UDException
      */
     @Override
     public AccountInfo getAccountInfo() {
         return accountInfo;
     }
 
     /**
      * Builds a tree of the directory structure for the current Dropbox session
      *
      * @return              The root node of the tree representing the directory structure
      * @throws UDException
      */
     @Override
     public UFile getFileList() throws UDException {
         if( directoryTree != null ) {
             return directoryTree;
         }
 
         ArrayList<UFile> returnList = new ArrayList<UFile>();
         DbxEntry.WithChildren files;
 
         UFile root = new UFile();
         root.setParent( null );
         root.setName( "/" );
         root.isFolder( true );
         root.setId( null );
         root.setOrigin( accountInfo.getUsername() + "-" + accountInfo.getSessionType() );
 
         try {
             files = client.getMetadataWithChildren( "/" );
         } catch( DbxException e ) {
             throw new UDException( "Unable to get file list metadata!", e );
         }
 
         for( DbxEntry file : files.children ) {
             UFile tempFile = new UFile();
 
             if( file.isFile() ) {
                 tempFile.isFolder( false );
                 tempFile.setName( file.name );
                 tempFile.setOrigin( accountInfo.getUsername() + "-" + accountInfo.getSessionType() );
                 tempFile.setParent( root );
                 tempFile.setId( file.name );
             } else if( file.isFolder() ) {
                 UFile folder = new UFile();
                 folder.setParent( root );
                 folder.setName(file.name);
                 folder.isFolder(true);
                 folder.setId(file.name);
                 folder.setOrigin(accountInfo.getUsername() + "-" + accountInfo.getSessionType());
 
                 try {
                     tempFile = addChildren( folder, client.getMetadataWithChildren( file.path ) );
                 } catch( DbxException e ) {
                     throw new UDException( "Unable to get file list metadata!", e );
                 }
             }
 
             root.addChild( tempFile );
         }
 
         directoryTree = root;
         return root;
     }
 
     /**
      * Private method for building the directory tree for Dropbox
      * @param root          The root node of the current subtree
      * @param folder        The current folder we are at in the directory hierarchy
      * @return              The last file reached in the subtree
      * @throws UDException
      */
     private UFile addChildren( UFile root, DbxEntry.WithChildren folder ) throws UDException {
         ArrayList<UFile> returnList = new ArrayList<UFile>();
 
         for( DbxEntry file : folder.children ) {
             UFile tempFile= new UFile();
 
             if( file.isFile() ) {
                 tempFile.isFolder( false );
                 tempFile.setName( file.name );
                 tempFile.setOrigin(accountInfo.getUsername() + "-" + accountInfo.getSessionType());
                 tempFile.setParent(root);
                 tempFile.setId(file.name);
             } else if( file.isFolder() ) {
                 UFile nextFolder = new UFile();
                 nextFolder.setParent( root );
                 nextFolder.setName( file.name );
                 nextFolder.isFolder( true );
                 nextFolder.setId( file.name );
                 nextFolder.setOrigin( accountInfo.getUsername() + "-" + accountInfo.getSessionType() );
 
                 try {
                     tempFile =  addChildren( nextFolder, client.getMetadataWithChildren( file.path ) );
                 } catch( DbxException e ) {
                     throw new UDException( "Unable to get file list metadata!", e );
                 }
             }
 
             root.addChild( tempFile );
         }
 
         return root;
     }
 
     /**
      * Searches the current Dropbox session directory for the searchString
      * The list returns all matches, including partial matches containing the searchString
      *
      * @param searchString  String to search for in all file/folder names
      * @return              The list of all matching files
      * @throws UDException
      */
     @Override
     public List<UFile> searchFiles(String searchString) throws UDException {
         ArrayList<UFile> returnList = new ArrayList<UFile>();
         UFile root = getFileList();
         getMatches( root, returnList, searchString );
         return returnList;
     }
 
     /**
      * Private method to recursively find all matching files/folders in current Dropbox session
      *
      * @param root          The root node of the directory tree
      * @param matches       The list containing all matching files
      * @param searchString  The string to search for in all file/folder names
      */
     private void getMatches( UFile root, List<UFile> matches, String searchString ) {
         for(UFile file : root.getChildren() ) {
            if( file.getName().toLowerCase().contains(searchString.toLowerCase())) {
                 matches.add( file );
             }
 
             if( file.isFolder() ) {
                 getMatches(file, matches, searchString);
             }
         }
     }
 
     /**
      * Uploads a file to Dropbox
      *
      * @param filename      The name of the file to upload to Dropbox
      * @return              The file that was just uploaded
      * @throws UDException
      */
     @Override
     public UFile upload(String filename) throws UDException {
         File inputFile = new File(filename);
         FileInputStream inputStream;
         UFile returnFile = new UFile();
 
         try {
             inputStream = new FileInputStream(inputFile);
 
             DbxEntry.File uploadedFile = this.client.uploadFile( "/" + filename, DbxWriteMode.add(), inputFile.length(), inputStream );
 
             returnFile.setName( uploadedFile.name );
             returnFile.setOrigin( accountInfo.getUsername() + "-" + accountInfo.getSessionType() );
             returnFile.setId( uploadedFile.name );
             returnFile.isFolder( false );
 
             inputStream.close();
         } catch( FileNotFoundException e ) {
             throw new UDException( "File '" + filename + "' not found!", e );
         } catch( DbxException e ) {
             throw new UDException( "Upload to Dropbox failed!", e );
         } catch( IOException e ) {
             throw new UDException( "Unable to read file!", e );
         }
 
         return returnFile;
     }
 
     /**
      * Downloads a file from Dropbox
      *
      *
      *
      * @param fileID        The id of the file to download
      * @return              The file just downloaded
      * @throws UDException
      */
     @Override
     public File download(String fileID) throws UDException {
         FileOutputStream outputStream;
 
         DbxEntry.File downloadedFile;
         File ret;
 
         try {
             outputStream = new FileOutputStream(fileID);
 
             downloadedFile = client.getFile( "/" + fileID, null, outputStream);
 
             outputStream.close();
             ret = new File("/" + fileID);
         } catch( FileNotFoundException e ) {
             throw new UDException( "File '" + fileID + "' not found!", e );
         } catch( DbxException e ) {
             throw new UDException( "Upload to Dropbox failed!", e );
         } catch( IOException e ) {
             throw new UDException( "Unable to write file!", e );
         }
 
         return ret;
     }
 
     /**
      * Gets the type of the current session
      *
      * @return      The current session type
      */
     @Override
     public String getSessionType() {
         return sessionType;
     }
 }
