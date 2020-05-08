 package safeguard;
 
 public class AccessController {
 
     private static AccessController accessControlInstance = null;
     private Crypto crypto;
     private DatabaseHelper databaseHelper;
 
     public static enum ClassLevel {
 
         NOT_CONFIDENTIAL(0),
         CONFIDENTIAL(1),
         STRICTLY_CONFIDENTIAL(2);
 
         ClassLevel(int level) {
             this.level = level;
         }
 
         public int value() {
             return level;
         }
 
         public static ClassLevel map(int val) {
             switch (val) {
                 case 0:
                     assert NOT_CONFIDENTIAL.value() == 0;
                     return NOT_CONFIDENTIAL;
                 case 1:
                    assert CONFIDENTIAL.value() == 1;
                     return CONFIDENTIAL;
                 case 2:
                    assert STRICTLY_CONFIDENTIAL.value() == 2;
                     return STRICTLY_CONFIDENTIAL;
                 default:
                     return null;
             }
         }
 
         private int level;
     }
 
     private AccessController() {
         crypto = Crypto.getInstance();
         databaseHelper = DatabaseHelper.getInstance();
     }
 
     public static AccessController getInstance() {
         if (accessControlInstance != null) {
             return accessControlInstance;
         } else {
             return accessControlInstance = new AccessController();
         }
     }
 
     private String getPathName(String filePath) {
         if (filePath.charAt(filePath.lastIndexOf('\\') - 1) == '\\') {
             return filePath.substring(0, filePath.indexOf(':') + 1);
         } else {
             return filePath.substring(0, filePath.lastIndexOf('\\') + 1);
         }
     }
 
     /**
      * Checks if the user can create file with given filename.
      *
      * @param uid user identifier
      * @param filePath full path with filename
      * @return true if user can create file
      */
     public boolean checkIfHaveAccess(int uid, String filePath) {
         return databaseHelper.accessTypeDirVolExe(uid, getPathName(filePath)) == Engine.AccessMode.FULL_ACCESS;
     }
 
     /**
      * Checks if the user can execute a program
      *
      * @param uid user identifier
      * @param filePath full path
      * @return true if user can execute program
      */
     public boolean checkIfcanExecuteFile(int uid, String filePath) {
         return databaseHelper.accessTypeDirVolExe(uid, filePath) == Engine.AccessMode.FULL_ACCESS;
     }
 
     /**
      * Gets access type for given file and user
      *
      * @param uid user identifier
      * @param filePath full path
      * @return access mode for given file and user
      */
     public Engine.AccessMode checkHowCanOpenFile(int uid, String filePath) {
         return databaseHelper.accessTypeSecretFile(uid, filePath);
     }
 
     private boolean isExecutable(String filePath) {
         return filePath.endsWith(".exe");
     }
 
     private boolean isDirectory(String filePath) {
         return filePath.endsWith("\\");
     }
 
     /**
      * Checks if user can delete the directory
      *
      * @param uid user identifier
      * @param filePath full path
      * @return true if user can delete directory
      */
     public boolean checkIfCanDeleteDir(int uid, String filePath) {
         for (String inFilePath : databaseHelper.getControlledDirsAndFilesIn(filePath)) {
             if (isExecutable(inFilePath)) {
                 return false;
             } else {
                 if (databaseHelper.accessTypeDirVolExe(uid, filePath) != Engine.AccessMode.FULL_ACCESS) {
                     return false;
                 }
                 if (isDirectory(inFilePath) && !checkIfCanDeleteDir(uid, inFilePath)) {
                     return false;
                 }
             }
         }
         return true;
     }
 }
