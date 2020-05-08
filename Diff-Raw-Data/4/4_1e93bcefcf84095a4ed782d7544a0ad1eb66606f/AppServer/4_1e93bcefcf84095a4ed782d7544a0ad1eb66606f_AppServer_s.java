 package com.meltmedia.cadmium.jetty;
 
 import com.google.gson.Gson;
 import com.meltmedia.cadmium.core.WarInfo;
 import com.meltmedia.cadmium.core.util.WarUtils;
 import org.apache.commons.dbcp.BasicDataSource;
 import org.apache.commons.io.FileUtils;
 import org.apache.commons.lang3.StringUtils;
 import org.eclipse.jetty.jndi.factories.MailSessionReference;
 import org.eclipse.jetty.plus.jndi.Resource;
 import org.eclipse.jetty.server.Server;
 import org.eclipse.jetty.webapp.WebAppContext;
 
 import java.io.File;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Properties;
 
 /**
  * com.meltmedia.cadmium.jetty.AppServer
  *
  * @author jmcentire
  */
 public class AppServer {
   private File workDir;
   private List<Resource> registeredJndiRes = new ArrayList<Resource>();
 
   public AppServer(File workDirectory) {
     workDir = workDirectory;
   }
 
   public static void log(String msg) {
     System.out.println(msg);
   }
 
   public void runServer(String warLocation) throws Exception {
     URL war = new URL(warLocation);
     warLocation = war.getFile();
     log("Loading war: "+warLocation);
     Integer port = new Integer(System.getProperty("port", "8080"));
 
     WarInfo info = WarUtils.getWarInfo(new File(warLocation));
     String contextPath = info != null && StringUtils.isNotBlank(info.getContext())
         ? info.getContext() : null;
 
     Server server = new Server(port);
 
     setupJNDI(server);
 
    //Enable parsing of jndi-related parts of web.xml and jetty-env.xml
    org.eclipse.jetty.webapp.Configuration.ClassList classlist = org.eclipse.jetty.webapp.Configuration.ClassList.setServerDefault(server);
    classlist.addAfter("org.eclipse.jetty.webapp.FragmentConfiguration", "org.eclipse.jetty.plus.webapp.EnvConfiguration", "org.eclipse.jetty.plus.webapp.PlusConfiguration");

     WebAppContext webapp = new WebAppContext();
     webapp.setContextPath(StringUtils.defaultString(contextPath, "/"));
     webapp.setDescriptor(warLocation + "/WEB-INF/web.xml");
     webapp.setServer(server);
     webapp.setWar(warLocation);
 
     server.setHandler(webapp);
     server.start();
     server.join();
   }
 
   private void setupJNDI(Server server) {
     File configDir = new File(workDir.getParentFile(), "conf");
     log("Looking for configuration file in "+configDir);
     if(configDir.isDirectory()) {
       File configFiles[] = configDir.listFiles();
       for(File configFile: configFiles) {
         if(configFile.getName().equalsIgnoreCase("mail.json")) {
           try {
             log("Found mail config file: "+configFile);
             MailSessionJNDI mail = new Gson().fromJson(FileUtils.readFileToString(configFile), MailSessionJNDI.class);
             configMailJNDI(server, mail);
             log("Configured mail session");
           } catch(Exception e) {
             log("Failed to configure mail session: ");
             e.printStackTrace();
           }
         } else if(configFile.getName().endsWith("-ds.json")) {
           try {
             log("Found datasource config file: "+configFile);
             DataSourceJNDI ds = new Gson().fromJson(FileUtils.readFileToString(configFile), DataSourceJNDI.class);
             configDatasourceJNDI(server, ds);
             log("Configured datasource: "+ds.getJndiName());
           } catch(Exception e) {
             log("Failed to configure datasource from file: "+configFile.getName());
             e.printStackTrace();
           }
         }
       }
     }
   }
 
   private void configDatasourceJNDI(Server server, DataSourceJNDI ds) throws Exception {
     BasicDataSource datasource = new BasicDataSource();
     datasource.setUrl(ds.getConnectionUrl());
     datasource.setDriverClassName(ds.getDriverClass());
     datasource.setUsername(ds.getUsername());
     datasource.setPassword(ds.getPassword());
     if(StringUtils.isNotBlank(ds.getTestSQL())) {
       datasource.setValidationQuery(ds.getTestSQL());
     }
     if(ds.getInitSize() != null && ds.getInitSize() > 0) {
       datasource.setInitialSize(ds.getInitSize());
     }
     if(ds.getMinPoolSize() != null && ds.getMinPoolSize() > 0) {
       datasource.setMinIdle(ds.getMinPoolSize());
     }
     if (ds.getMaxPoolSize() != null && ds.getMaxPoolSize() > 0) {
       datasource.setMaxIdle(ds.getMaxPoolSize());
     }
 
     registeredJndiRes.add(new Resource(ds.getJndiName(), datasource));
   }
 
   private boolean isAnySet(Integer... args) {
     if(args != null) {
       for(Integer arg : args) {
         if(arg != null && arg > 0) {
           return true;
         }
       }
     }
     return false;
   }
 
   private void configMailJNDI(Server server, MailSessionJNDI mail) throws Exception {
     Properties mailProps = new Properties();
     if(mail.getProperties() != null) {
       mailProps.putAll(mail.getProperties());
     }
 
     MailSessionReference mailref = new MailSessionReference();
     if(StringUtils.isNotBlank(mail.getUsername())) {
       mailref.setUser(mail.getUsername());
     }
     if(StringUtils.isNotBlank(mail.getPassword())) {
       mailref.setPassword(mail.getPassword());
     }
     registeredJndiRes.add(new Resource(mail.getJndiName(), mailref));
   }
 }
