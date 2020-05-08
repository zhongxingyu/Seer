 package com.sf.checkinactivity;
 
 import com.perforce.p4java.PropertyDefs;
 import com.perforce.p4java.core.IChangelist;
 import com.perforce.p4java.core.IChangelistSummary;
 import com.perforce.p4java.core.file.FileSpecBuilder;
 import com.perforce.p4java.core.file.IFileSpec;
 import com.perforce.p4java.exception.*;
 import com.perforce.p4java.server.IServer;
 import com.perforce.p4java.server.ServerFactory;
 
 import java.io.*;
 import java.net.URISyntaxException;
 import java.util.List;
 import java.util.Properties;
 
 public class P4Checkins {
     private IServer server;
     public static final String PROPERTIES_FILE_KEY = "repoprops";
     public static final String PROPERTIES_FILE = "/com/sf/checkinactivity/repo.properties";
     public static final String P4PASSWD = "P4PASSWD";
 
     public P4Checkins() {
     }
 
     public void connect()
             throws AccessException, ConfigException, RequestException, ConnectionException, NoSuchObjectException, ResourceException, URISyntaxException, IOException {
         InputStream is = null;
         try {
             String propertiesFile = System.getProperty(PROPERTIES_FILE_KEY);
             if (propertiesFile == null) {
                 is = this.getClass().getResourceAsStream(PROPERTIES_FILE);
             } else {
                 File propFile = new File(propertiesFile);
                 if (!propFile.exists()) {
                     throw new FileNotFoundException(String.format("Key: %s File: %s not found.", PROPERTIES_FILE_KEY, propertiesFile));
                 }
                 is = new FileInputStream(propFile);
             }
             if (is != null) {
                 Properties props = new Properties();
                 props.load(is);
                 String sName = props.getProperty("servername");
                 server = ServerFactory.getServer(sName, props);
                 server.connect();
                 // First check the P4PASSWD environment variable
                 String password = System.getenv().get(P4PASSWD);
                 // if not there grab it from -D
                 // if not there, try the properties file for the password
                 if (password == null || password.isEmpty()) {
                     password = System.getProperty(P4PASSWD, props.getProperty(PropertyDefs.PASSWORD_KEY_SHORTFORM));
                 }
                 if (password != null && !password.isEmpty()) {
                     server.login(password);
                 } else {
                     throw new IOException("no p4 password provided");
                 }
             } else {
                 throw new FileNotFoundException("Properties file was not found: " + PROPERTIES_FILE);
             }
 
         } finally {
             if (is != null) { try {is.close();} catch (Exception ignore) {} }
         }
     }
 
    public List<IChangelistSummary> getChangelistsForUser(String userid, String depotPath)
             throws AccessException, RequestException, ConnectionException {
        List<IFileSpec> fileSpecs = FileSpecBuilder.makeFileSpecList(new String[]{depotPath});
 
         return server.getChangelists(-1, fileSpecs, null, userid, false, true, false, false);
     }
 
     public IChangelist describeChangelist(IChangelistSummary iChangelistSummary)
             throws AccessException, RequestException, ConnectionException {
         return server.getChangelist(iChangelistSummary.getId());
     }
 
     @Override
     protected void finalize() throws Throwable {
         super.finalize();
         server.logout();
     }
 }
