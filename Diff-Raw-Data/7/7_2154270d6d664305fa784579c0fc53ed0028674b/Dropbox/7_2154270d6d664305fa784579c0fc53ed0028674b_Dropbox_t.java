 package cloud;
 
 import com.dropbox.client2.DropboxAPI;
 import com.dropbox.client2.exception.DropboxException;
 import com.dropbox.client2.session.*;
 import commons.Tokens;
 
 import java.util.ArrayList;
 
 /**
  * UserEntity: vanstr
  * Date: 13.29.6
  * Time: 20:55
  * Class represent basic cloud.Dropbox API functionality
  * Like: getFileList, getFileLink
  */
 public class Dropbox {
 
     // private static final Logger logger = LoggerFactory.getLogger(Dropbox.class);
 
     // Define application params
     final static private String APP_KEY = "uxw4eysrg39u7jw";
     final static private String APP_SECRET = "77p0nl292u8op2p";
     private static final Session.AccessType ACCESS_TYPE = Session.AccessType.DROPBOX;
     private static AppKeyPair appKeys = new AppKeyPair(APP_KEY, APP_SECRET);
 
     private WebAuthSession session;
     private DropboxAPI<WebAuthSession> api;
     private WebAuthSession.WebAuthInfo authInfo = null;
 
     /**
      * Start session to likn user account with CloudMusic
      */
     public Dropbox() {
         //AppKeyPair appKeys = new AppKeyPair(APP_KEY, APP_SECRET);
         session = new WebAuthSession(appKeys, ACCESS_TYPE);
         try {
             authInfo = session.getAuthInfo();
         } catch (DropboxException e) {
             e.printStackTrace();
         }
 
     }
 
     public Dropbox(String accessTokenKey, String accessTokenSecret) {
         AccessTokenPair accessTokenPair = new AccessTokenPair(accessTokenKey, accessTokenSecret);
         session = new WebAuthSession(appKeys, ACCESS_TYPE);
         session.setAccessTokenPair(accessTokenPair);
         api = new DropboxAPI<WebAuthSession>(session);
 
     }
 
 
     /**
      * @return
      */
     public Tokens getRequestTokens() {
         // Obtaining oAuth request token to be used for the rest of the authentication process.
         RequestTokenPair pair = authInfo.requestTokenPair;
 
         Tokens requestTokens = new Tokens(pair.key, pair.secret);
 
         return requestTokens;
 
     }
 
     /**
      * generate link, where user provides privileges to access his account data
      *
      * @return
      */
     public String getAuthLink() {
         return authInfo.url;
     }
 
     /**
      * Get User access token pair
      *
      * @return
      */
     public Tokens getUserAccessTokens(Tokens requestTokens) {
 
         RequestTokenPair pair = new RequestTokenPair(requestTokens.key, requestTokens.secret);
 
         try {
             session.retrieveWebAccessToken(pair);
         } catch (DropboxException e) {
             System.out.println("ERROR: " + e.getMessage());
             e.printStackTrace();
         }
         AccessTokenPair tokens = session.getAccessTokenPair();
 
         Tokens accessTokens = new Tokens(tokens.key, tokens.secret);
 
         return accessTokens;
     }
 
 
     /**
      * Get file link for downloading
      *
      * @param filePath
      * @return
      */
     public String getFileLink(String filePath) {
 
         String downloadLink = "";
         try {
             DropboxAPI.DropboxLink media = api.media(filePath, false);
             downloadLink = media.url;
             /* Todo: to log
             System.out.println("path:" + filePath);
             System.out.println("Link:" + downloadLink);
             System.out.println("Exp:" + media.expires);
             */
 
         } catch (DropboxException e) {
             System.out.println("getFileLink: " + e);
             e.printStackTrace();
         }
 
         return downloadLink;
     }
 
     /**
      * @param folderPath - in which folder look up
      * @param recursion  - if true also include files from sub folders recusievly
      * @param fileType   - if file type == NULL return all list, ex: folder, files, mp3, txt
      *                   TODO, what if i want to get wav & mp3
      * @return TODO separate structure array{file_path,music metadata album song name, artist}
      *         How to get meta data of file?
      */
     public ArrayList<String> getFileList(String folderPath, boolean recursion, String fileType) {
 
         // Get folder content
         DropboxAPI.Entry dirent = null;
         try {
             dirent = api.metadata(folderPath, 1000, null, true, null);
         } catch (DropboxException e) {
             System.out.println(e);
         }
 
         // TODO create separate music list structure/class, should be similar in all clouds
         ArrayList<String> files = new ArrayList<String>();
 
         for (DropboxAPI.Entry ent : dirent.contents) {
 
             if (ent.isDir ) {
                 if( recursion ){
                     // start recursion through all folders
                     //TODO log System.out.println("Look in: " + ent.path);
                     files.addAll(getFileList(ent.path, false, fileType));
                 }
             } else {
 
                 // filter files by fileType -------------------------------->
                 // TODO separete function, multiple file types MWA,MP3,avi...
                 String fileName = ent.fileName().toLowerCase();
                 int nameLength = fileName.length();
                int extensionLength = fileType.length();

                // file name ".mp3" - not allowed, at least "a.mp3"
                if( nameLength < (extensionLength +2) )  continue;

                String extension = fileName.substring(nameLength - extensionLength, nameLength);
 
                 /*  TODO: to log
                 System.out.println("nameL:" + nameLength );
                 System.out.println("ext" + extension);
                 System.out.println("fileTypes" + fileType);
                 //*/
                 if (extension.equals(fileType.toLowerCase())) {
                     files.add(new String(ent.path));
                 }
                 // --------------------------------------------------------->
             }
         }
 
         return files;
     }
 }
