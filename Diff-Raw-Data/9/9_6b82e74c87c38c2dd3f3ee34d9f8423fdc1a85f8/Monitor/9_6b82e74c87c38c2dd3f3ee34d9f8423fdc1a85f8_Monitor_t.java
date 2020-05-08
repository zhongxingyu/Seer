 package ru.guardz.docmon;
 
 import com.documentum.com.DfClientX;
 import com.documentum.com.IDfClientX;
 import com.documentum.fc.client.*;
 import com.documentum.fc.common.DfException;
 import com.documentum.fc.common.DfLogger;
 import com.documentum.fc.common.IDfId;
 import com.documentum.fc.common.IDfLoginInfo;
 
 import java.io.File;
 import java.io.IOException;
 
 public class Monitor {
 
     private static String OS = System.getProperty("os.name").toLowerCase();
 
     public static void main(String[] args) throws DfException {
         String docbaseName = args[2];
         IDfSessionManager dfSessionManager = initialConnect(args);
 
         IDfSession dfSession = null;
         try {
             dfSession = dfSessionManager.newSession(docbaseName);
             System.out.println("Successfully connect to the repository ".concat(docbaseName));
 /*
             System.out.println("JMS Config ".concat(getJMSConfig(dfSession)));
             List<String> sessions = getActiveSessions(dfSession);
             int size = sessions.size();
             for (int i = 0; i < size; i++) {
                 System.out.println(sessions.get(i));
             }*/
             System.out.println("Total open sessions in docbase: ".concat(getSessionCount(dfSession).toString()));
             System.out.println("Total failed and halted workflows: ".concat(getDeadWorkflows(dfSession).toString()));
             System.out.println("Total workitems not associated with servers: ".concat(getBadWorkitems(dfSession).toString()));
 
             if (fetchContent(dfSession)) System.out.println("Can fetch content!");
 
         } catch (Throwable t) {
             DfLogger.fatal(dfSessionManager, t.getMessage(), null, t);
         } finally {
             assert dfSession != null;
             dfSession.disconnect();
         }
     }
 
     private static Integer getSessionCount(IDfSession dfSession) throws DfException {
         final String s = "EXECUTE show_sessions";
         IDfQuery query = new DfQuery();
         query.setDQL(s);
         int count = 0;
         IDfCollection collection = query.execute(dfSession, IDfQuery.DF_READ_QUERY);
         try {
             while (collection.next()) {
                 count++;
             }
         } catch (DfException e) {
             e.printStackTrace();
         } finally {
             if (collection != null) {
                 collection.close();
             }
         }
         return count;
     }
 
     private static Integer getDeadWorkflows(IDfSession dfSession) throws DfException {
         final String s = "SELECT r_object_id FROM dm_workflow w WHERE any w.r_act_state in (3,4)";
         IDfQuery query = new DfQuery();
         query.setDQL(s);
         int count = 0;
         IDfCollection collection = query.execute(dfSession, IDfQuery.DF_READ_QUERY);
         try {
             while (collection.next()) {
                 count++;
             }
         } catch (DfException e) {
             e.printStackTrace();
         } finally {
             if (collection != null) {
                 collection.close();
             }
         }
         return count;
     }
 
     private static Integer getBadWorkitems(IDfSession dfSession) throws DfException {
         final String s = "select * from dmi_workitem w, dm_workflow wf where  w.r_workflow_id = wf.r_object_id and a_wq_name not in (select r_object_id from dm_server_config)";
         IDfQuery query = new DfQuery();
         query.setDQL(s);
         int count = 0;
         IDfCollection collection = query.execute(dfSession, IDfQuery.DF_READ_QUERY);
         try {
             while (collection.next()) {
                 count++;
             }
         } catch (DfException e) {
             e.printStackTrace();
         } finally {
             if (collection != null) {
                 collection.close();
             }
         }
         return count;
     }
 
     private static Boolean fetchContent(IDfSession dfSession) throws DfException, IOException {
         final String s = "select * from dm_document where folder('/System/Sysadmin/Reports') enable (RETURN_TOP 1)";
         IDfQuery query = new DfQuery();
         query.setDQL(s);
         Boolean ret;
         String filename = null;
         if (isWindows()) {
             filename = "C:\\TEMP\\file.txt";
         } else if (isSolaris()) {
             filename = "/tmp/file.txt";
         } else if (isUnix()) {
             filename = "/tmp/file.txt";
         }
         IDfCollection collection = query.execute(dfSession, IDfQuery.DF_READ_QUERY);
         try {
             collection.next();
             IDfId id = collection.getId("r_object_id");
             IDfSysObject sysObject = (IDfSysObject) dfSession.getObject(id);
             sysObject.getFile(filename);
         } catch (DfException e) {
             e.printStackTrace();
         } finally {
             if (collection != null) {
                 collection.close();
             }
         }
 
         ret = makeFile(filename);
 
         return ret;
     }
 
     private static Boolean makeFile(String filename) throws IOException {
         File file = new File(filename);
         return file.exists();
     }
 
/*    private static String getJMSConfig(IDfSession dfSession) throws DfException {
         final String s = "SELECT * FROM DM_JMS_CONFIG";
         IDfQuery query = new DfQuery();
         query.setDQL(s);
         String jmsName = null;
         IDfCollection collection = query.execute(dfSession, IDfQuery.DF_READ_QUERY);
         try {
             while (collection.next()) {
                 jmsName = collection.getString("object_name");
             }
         } catch (DfException e) {
             e.printStackTrace();
         } finally {
             if (collection != null) {
                 collection.close();
             }
         }
         return jmsName;
     }
 
     private static List<String> getActiveSessions(IDfSession dfSession) throws DfException {
         final String s = "EXECUTE show_sessions";
         IDfQuery query = new DfQuery();
         query.setDQL(s);
         List<String> list = Lists.newArrayList();
         IDfCollection collection = query.execute(dfSession, IDfQuery.DF_READ_QUERY);
         try {
             while (collection.next()) {
                 list.add(collection.getString("user_name"));
                 list.add(collection.getString("db_session_id"));
                 list.add(collection.getString("pid"));
                 list.add(collection.getString("client_host"));
             }
         } catch (DfException e) {
             e.printStackTrace();
         } finally {
             if (collection != null) {
                 collection.close();
             }
         }
         return list;
    }*/
 
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
 
     public static boolean isWindows() {
         return (OS.contains("win"));
     }
 
     public static boolean isUnix() {
         return (OS.contains("nix") || OS.contains("nux") || OS.indexOf("aix") > 0 );
     }
 
     public static boolean isSolaris() {
         return (OS.contains("sunos"));
     }
 }
