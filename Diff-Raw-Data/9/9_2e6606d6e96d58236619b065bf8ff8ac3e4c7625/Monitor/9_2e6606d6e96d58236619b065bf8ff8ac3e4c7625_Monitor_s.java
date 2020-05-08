 package ru.guardz.docmon;
 
 import com.documentum.com.DfClientX;
 import com.documentum.com.IDfClientX;
 import com.documentum.fc.client.*;
 import com.documentum.fc.common.DfException;
 import com.documentum.fc.common.DfLogger;
 import com.documentum.fc.common.IDfLoginInfo;
 
 public class Monitor {
 
     public static void main(String[] args) throws DfException {
         String docbaseName = args[2];
         IDfSessionManager dfSessionManager = initialConnect(args);
 
         IDfSession dfSession = null;
         try {
             dfSession = dfSessionManager.newSession(docbaseName);
//            DfLogger.info(UpdateDpkFromFile.class, "Successfully connect to the repository ".concat(docbaseName), null, null);
             System.out.println("Successfully connect to the repository ".concat(docbaseName));
 
/*            CustomProxyFactoriesLocator.registerAll();

            updateDpk(args, dfSession);*/
            System.out.println("JMS Config".concat(getJMSConfig(dfSession)));
 
 
             System.out.println("JMS Config Successfully complete");
         } catch (Throwable t) {
             DfLogger.fatal(dfSessionManager, t.getMessage(), null, t);
         }
        // write your code here
     }
 
     private static String getJMSConfig(IDfSession dfSession) throws DfException {
         final String s = "SELECT * FROM DM_JMS_CONFIG";
         IDfQuery query = new DfQuery();
         query.setDQL(s);
         String jmsName = null;
         int count = 0;
         IDfCollection collection = query.execute(dfSession,IDfQuery.DF_READ_QUERY);
         try {
             while (collection.next()) {
                 count++;
                 jmsName = collection.getString("object_name");
             }
         } catch (DfException e) {
             e.printStackTrace();
         } finally {
             // ALWAYS! clean up your collections
             if (collection != null) {
                 collection.close();
             }
         }
         return jmsName;
     }
 
     private static IDfSessionManager initialConnect(String[] args) throws DfException {
         IDfClientX clientx = new DfClientX();
         IDfClient client = clientx.getLocalClient();
         IDfLoginInfo iLogin = clientx.getLoginInfo();
         iLogin.setUser(args[0]);
         iLogin.setPassword(args[1]);
         IDfSessionManager dfSessionManager = client.newSessionManager();
         dfSessionManager.setIdentity(args[2], iLogin);
         return dfSessionManager;
     }
 }
