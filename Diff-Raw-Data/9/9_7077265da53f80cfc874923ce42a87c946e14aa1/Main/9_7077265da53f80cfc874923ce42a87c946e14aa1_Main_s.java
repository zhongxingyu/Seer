 import com.documentum.com.DfClientX;
 import com.documentum.com.IDfClientX;
 import com.documentum.fc.client.IDfClient;
 import com.documentum.fc.client.IDfSession;
 import com.documentum.fc.client.IDfSessionManager;
 import com.documentum.fc.common.DfCriticalException;
 import com.documentum.fc.common.DfException;
 import com.documentum.fc.common.DfLogger;
 import com.documentum.fc.common.IDfLoginInfo;
 
 public class Main {
 
     public static void main(String[] args) throws DfException {
         if (args.length < 4) {
             String message = "Wrong parameter's number. Must be at least 4 parameters: "
                     + "<user_name> <password> <repository> <fullpath_filename>";
 //            DfLogger.error(UpdateDpkFromFile.class, message, null, null);
             throw new DfCriticalException(message);
         }
 
         String docbaseName = args[2];
         IDfSessionManager dfSessionManager = connect2Dctm(args);
 
         IDfSession dfSession = null;
         try {
             dfSession = dfSessionManager.newSession(docbaseName);
 //            DfLogger.info(UpdateDpkFromFile.class, "Successfully connect to the repository ".concat(docbaseName), null, null);
             System.out.println("Successfully connect to the repository ".concat(docbaseName));
 
 /*            CustomProxyFactoriesLocator.registerAll();
 
             updateDpk(args, dfSession);*/
 
             System.out.println("UpdateDpkFromFile Successfully complete");
         } catch (Throwable t) {
             DfLogger.fatal(dfSessionManager, t.getMessage(), null, t);
         } finally {
             if (dfSessionManager != null) {
                 dfSessionManager.release(dfSession);
             }
         }
 
         System.exit(0);
 	// write your code here
     }
 
     private static IDfSessionManager connect2Dctm(String[] args) throws DfException {
         IDfClientX clientx = new DfClientX();
         IDfClient client = clientx.getLocalClient();
        IDfLoginInfo oLogin = clientx.getLoginInfo();
        oLogin.setUser(args[0]);
        oLogin.setPassword(args[1]);
         IDfSessionManager dfSessionManager = client.newSessionManager();
        dfSessionManager.setIdentity(args[2], oLogin);
         return dfSessionManager;
     }
 }
