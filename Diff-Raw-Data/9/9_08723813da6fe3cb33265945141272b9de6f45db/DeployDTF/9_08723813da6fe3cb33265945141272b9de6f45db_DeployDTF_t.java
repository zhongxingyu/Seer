 package com.yahoo.dtf.deploy;
 
 import java.io.BufferedReader;
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.OutputStream;
 import java.net.SocketException;
 import java.net.URLEncoder;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Properties;
 import java.util.Map.Entry;
 
 import org.apache.commons.net.telnet.TelnetClient;
 
 import com.jcraft.jsch.Channel;
 import com.jcraft.jsch.ChannelExec;
 import com.jcraft.jsch.ChannelSftp;
 import com.jcraft.jsch.JSchException;
 import com.jcraft.jsch.Session;
 import com.jcraft.jsch.SftpException;
 import com.yahoo.dtf.DTFProperties;
 import com.yahoo.dtf.actions.Action;
 import com.yahoo.dtf.actions.ActionFactory;
 import com.yahoo.dtf.actions.properties.Property;
 import com.yahoo.dtf.actions.protocol.deploy.DTFNode;
 import com.yahoo.dtf.actions.protocol.deploy.Dtfa;
 import com.yahoo.dtf.actions.protocol.deploy.Dtfc;
 import com.yahoo.dtf.actions.protocol.deploy.Dtfx;
 import com.yahoo.dtf.actions.protocol.deploy.Setup;
 import com.yahoo.dtf.config.Config;
 import com.yahoo.dtf.exception.DTFException;
 import com.yahoo.dtf.exception.DebugServerException;
 import com.yahoo.dtf.exception.ParseException;
 import com.yahoo.dtf.logger.DTFLogger;
 import com.yahoo.dtf.util.JarUtil;
 import com.yahoo.dtf.util.ThreadUtil;
 
 /**
  * XXX: this class needs to be cleaned up quite a bit.
  * 
  * @author rlgomes
  *
  */
 public class DeployDTF {
     
     static DTFLogger _logger = DTFLogger.getLogger(DeployDTF.class);
 
     public static void main(String[] args) {
         try { 
 	        com.yahoo.dtf.DTFNode.init();
 	        
 	        String command = "start";
 	        if ( args.length > 0 ) command = args[0];
 	
 	        Config conf = Action.getConfig();
 	        String config = conf.getProperty("deploy.config");
 	        String user = conf.getProperty("deploy.user");
	        String script = conf.getProperty("deploy.script");
 	        
 	        
 	        _logger.info("Reading [" + config + "] config file");
 	
 	        if ( command.equals("setup-ssh") ) { 
 	            String host = conf.getProperty("setup.host");
 	            user = conf.getProperty("setup.user");
 	            
 	            if ( host == null ) 
 	                throw new DTFException("Set the setup.host property.");
 	            
 	            if ( user == null ) { 
 	                user = System.getProperty("user.name");
 	                _logger.info("Defaulting to user [" + user + "]");
 	            }
 	            
 	            setupSSH(host, user, _logger);
 	            return;
 	        }
 	      
 	        FileInputStream fis;
 	        try {
 	            fis = new FileInputStream(config);
 	        } catch (FileNotFoundException e) {
 	            throw new DTFException("Error reading [" + config +"]", e);
 	        }
 	
 	        /*
 	         * Load any default properties identified by the deploy.defaults 
 	         * property
 	         */
 	        String defaults = Action.getConfig().getProperty("deploy.defaults");
 	        if ( defaults != null && new File(defaults).exists() ) { 
 	            try { 
 	                FileInputStream pfis = null;
 		            try {
 			            pfis = new FileInputStream(defaults);
 			            Action.getConfig().loadProperties(pfis, true);
 		            } finally { 
 		                if ( pfis != null ) pfis.close();
 		            }
 	            } catch (IOException e) { 
 	                throw new DTFException("Error reading properties file [" 
 	                                       + defaults + "]",e);
 	            }
 	        }
 	
 	        Action root = ActionFactory.parseAction(fis, config);
 	     
 	        if ( !(root instanceof Setup) )
 	            throw new DTFException("Root element of document is not <setup>.");
 	        
 	        Setup setup = (Setup) root;
 	        
 	        initJar();
 	
 	        if ( command.equals("start") ) { 
 	            setupStart(setup);
 	        } else if ( command.equals("status")) { 
 	            setupStatus(setup);
 	        } else if ( command.equals("stop") ) { 
 	            setupStop(setup);
 	        } else if ( command.equals("wait") ) { 
 	            waitForCompletion(setup);
 	        } else if ( command.equals("watch") ) { 
 	            setupWatch(setup);
 	        } else if ( command.equals("savelogs") ) { 
 	            setupSaveLogs(setup);
 	        } else if ( command.equals("script") ) { 
 	            user = ( user.equals("N/A") ? null : user );
 	            setupInit(setup, user, script);
 	        }
         } catch (DTFException e) { 
             _logger.error("Error deploying.",e);
             System.exit(-1);
         }
     }
     
     public static void initJar() throws DTFException { 
         File dtf_jar = new File("dtf.jar");
         if ( dtf_jar.exists() )  {
              if ( !dtf_jar.delete() ) 
                  throw new DTFException("Unable to delete [" + dtf_jar + "]");
         }
       
         String dtfhome = Action.getConfig().getProperty(DTFProperties.DTF_HOME);
         JarUtil.jarUp(new File(dtfhome), dtf_jar); 
     }
     
     private static void setupSSH(String host, String user, DTFLogger logger) 
             throws DTFException {
         try {
             Session session = DTFSSHSetup.setupSSH(host, user, logger);
             session.disconnect();
         } catch (FileNotFoundException e) {
             throw new DTFException("Unable to setup passwordless ssh.",e);
         } catch (JSchException e) {
             throw new DTFException("Unable to setup passwordless ssh.",e);
         } catch (IOException e) {
             throw new DTFException("Unable to setup passwordless ssh.",e);
         } catch (SftpException e) {
             throw new DTFException("Unable to setup passwordless ssh.",e);
         }
     }
     
     private static void waitForCompletion(Setup setup) throws DTFException {
         ArrayList<Dtfc> dtfcs = setup.findAllActions(Dtfc.class);
        
         Config conf = Action.getConfig();
        
         int check_interval = conf.getPropertyAsInt("deploy.wait.interval",60000);
         
         for (int i = 0; i < dtfcs.size(); i++) { 
             Dtfc dtfc = dtfcs.get(i);
             
             // lets startup this DTFC on the machine specified
             String host = dtfc.getHost();
             String user = dtfc.getUser();
 
             String hostkey = user + "@" + host;
             try { 
                 _logger.info("Status of dtfc on " + host);
                     
                 TelnetClient telnet = new TelnetClient();
                 telnet.connect(host, 40000);
                 OutputStream os = telnet.getOutputStream();
                 InputStream is = telnet.getInputStream();
                     
                 InputStreamReader isr = new InputStreamReader(is);
                 BufferedReader br = new BufferedReader(isr);
                     
                 try { 
                     os.write("status\nquit\n".getBytes());
                     os.flush();
         
                     String line = null;
                     while ( ( line = br.readLine() ) != null ) {
                         ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                
                         // read the props for each component
                         while ( line != null && !line.trim().equals("") && 
                                 !line.equals("Bye")) {
                             line = line.replaceFirst("^#","");
                             baos.write((line + "\n").getBytes());
                             line = br.readLine();
                         }
                                 
                         ByteArrayInputStream bais = 
                                    new ByteArrayInputStream(baos.toByteArray());
                         Properties props = new Properties();
                         props.load(bais);
                                 
                         if ( props.getProperty("Bye") != null ) 
                             break;
                                 
                         String type =
                                  props.getProperty(DTFProperties.DTF_NODE_TYPE);
                         String hostname = props.getProperty("dtf.node.host");
                                 
                         if ( type.equals("dtfc") ) { 
                             _logger.info("DTFC on " + hostname);
                         }
                     }
                 } finally { 
                     br.close();
                     os.close();
                     is.close();
                 }
             } catch (IOException e) { 
                 _logger.info("DTFC not running at " + hostkey);
                 return;
             }
                 
             boolean done = false;
             while ( !done ) { 
                 ThreadUtil.pause(check_interval);
                 Dtfx dtfx = (Dtfx) dtfc.findFirstAction(Dtfx.class);
                 if ( dtfx != null ) { 
                     String dtfx_host = dtfx.getHost();
                     String dtfx_user = dtfx.getUser();
                     String dtfx_path = dtfx.getPath();
                     String dtfx_rsakey = dtfx.getRsakey();
                    
                     try { 
                         Properties state = getState("dtfx",
                                                     dtfx_host,
                                                     dtfx_user,
                                                     dtfx_path,
                                                     dtfx_rsakey);
                         
                         _logger.info("DTFX running at [" + dtfx_host + "]");
                         String exited = state.getProperty("dtf.node.exited");
                         if ( exited != null && exited.equals("true") ) {
 
                             String succeeded = state.getProperty("dtf.node.succeeded");
                             
                             if ( succeeded.equals("true") ) { 
                                 _logger.info("DTFX has completed successfully at [" + dtfx_host + "]");
                             } else { 
                                 throw new DTFException("DTFX has completed with an error at [" + dtfx_host + "]");
                             }
                             done = true;
                         } 
                     } catch (IOException e) { 
                         _logger.info("DTFX on " + dtfx_host + " not running.",e);
                     } catch (JSchException e) {
                         _logger.info("DTFX on " + dtfx_host + " not running.",e);
                     } catch (SftpException e) {
                         _logger.info("DTFX on " + dtfx_host + " not running.",e);
                     } 
                 }
             }
         }
     }
     
     private static void setupStatus(Setup setup) throws DTFException {
         ArrayList<Dtfc> dtfcs = setup.findAllActions(Dtfc.class);
         
         for (int i = 0; i < dtfcs.size(); i++) { 
             Dtfc dtfc = dtfcs.get(i);
             
             ArrayList<Dtfa> dtfas = dtfc.findAllActions(Dtfa.class);
             ArrayList<Dtfa> dtfas_connected = new ArrayList<Dtfa>();
             
             // lets startup this DTFC on the machine specified
             String host = dtfc.getHost();
             String user = dtfc.getUser();
 
             String hostkey = user + "@" + host;
             try { 
                 _logger.info("Status of dtfc on " + host);
                 
                 TelnetClient telnet = new TelnetClient();
                 telnet.connect(host, 40000);
                 OutputStream os = telnet.getOutputStream();
                 InputStream is = telnet.getInputStream();
                 
                 try { 
                     InputStreamReader isr = new InputStreamReader(is);
                     BufferedReader br = new BufferedReader(isr);
                   
                     try { 
 	                    os.write("status\nquit\n".getBytes());
 	                    os.flush();
 	
 	                    String line = null;
 	                    while ( ( line = br.readLine() ) != null ) {
 	                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
 	                       
 	                        // read the props for each component
 	                        while ( line != null && !line.trim().equals("") && !line.equals("Bye")) {
 	                            line = line.replaceFirst("^#","");
 	                            baos.write((line + "\n").getBytes());
 	                            line = br.readLine();
 	                        }
 	                        
 	                        ByteArrayInputStream bais = 
 	                                           new ByteArrayInputStream(baos.toByteArray());
 	                        Properties props = new Properties();
 	                        props.load(bais);
 	                        
 	                        if ( props.getProperty("Bye") != null ) 
 	                            break;
 	                        
 	                        String type = props.getProperty(DTFProperties.DTF_NODE_TYPE);
 	                        String id = props.getProperty("dtf.node.id");
 	                        String hostname = props.getProperty("dtf.node.host");
 	                        String node_user = props.getProperty("dtf.node.user");
 	                        String path = props.getProperty("dtf.node.home");
 	                        boolean locked = Boolean.valueOf(props.getProperty("dtf.node.locked"));
 	                        
 	                        if ( type.equals("dtfc") ) { 
 	                            _logger.info("DTFC on " + hostname);
 	                        } else if ( type.equals("dtfa") ) { 
 	                            Dtfa aux = new Dtfa();
 	                            aux.setHost(hostname);
 	                            aux.setUser(node_user);
 	                            aux.setPath(path);
 	                            dtfas_connected.add(aux);
 	                            if ( locked ) { 
 	                                _logger.info("DTFA [" + id + "] on " + hostname + 
 	                                             " is locked");
 	                            } else { 
 	                                _logger.info("DTFA [" + id + "] on " + hostname +
 	                                             " is free");
 	                            }
 	                        }
 	                    }
                     } finally { 
                         br.close();
                     }
                
                     /*
                      * Validate that there are no agents not accounted for
                      */
                     
                 } finally { 
                     os.close();
                     is.close();
                 }
                 
                 for (int a = 0; a < dtfas.size(); a++) { 
                     Dtfa dtfa = dtfas.get(a);
                    
                     boolean found = false;
                     for (int x = 0; x < dtfas_connected.size(); x++) { 
                         if ( dtfa.equals(dtfas_connected.get(x)) ) { 
                             dtfas_connected.remove(x);
                             found = true;
                             break;
                         }
                     }
                     
                     if ( !found ) { 
                         _logger.warn("DTFA [dtfa-" + a + "] NOT running on " + 
                                      dtfa.getUser() + "@" +  dtfa.getHost() + 
                                      ":" + dtfa.getPath());
                     }
                 }
 
             } catch (IOException e) { 
                 _logger.info("DTFC not running at " + hostkey);
                 return;
             } catch (DTFException e) { 
                 _logger.info("DTFC not running at " + hostkey);
                 return;
             }
             
             Dtfx dtfx = (Dtfx) dtfc.findFirstAction(Dtfx.class);
             if ( dtfx != null ) { 
                 String dtfx_host = dtfx.getHost();
                 String dtfx_user = dtfx.getUser();
                 String dtfx_path = dtfx.getPath();
                 String dtfx_rsakey = dtfx.getRsakey();
                
                 try { 
                     Properties state = getState("dtfx", dtfx_host, dtfx_user, dtfx_path, dtfx_rsakey);
                     int dport = Integer.valueOf(state.getProperty("dtf.debug.port"));
                    
                     String exited = state.getProperty("dtf.node.exited");
                     if ( exited != null && exited.equals("true") ) { 
                         String msg = state.getProperty("dtf.node.succeeded");
                         msg = ( msg.equals("true") ? "succeeded" : "failed");
                         _logger.info("DTFX on " + dtfx_host + 
                                      " not running, last run has " + msg + ".");
                     } else { 
                         try {
                             Properties status = getStatus(dtfx_host, dport);
                             String xml     = status.getProperty("dtf.xml.filename");
                             String xmlcol  = status.getProperty("dtf.xml.column");
                             String xmlline = status.getProperty("dtf.xml.line");
                             
                             _logger.info("DTFX on " + dtfx_host + " running " + 
                                          xml + ":" + xmlline + "," + xmlcol);
                         } catch (IOException e) { 
                             _logger.info("DTFX on " + dtfx_host + " not running.");
                         } 
                     }
                 } catch (IOException e) { 
                     _logger.info("DTFX on " + dtfx_host + " not running.",e);
                 } catch (JSchException e) {
                     _logger.info("DTFX on " + dtfx_host + " not running.",e);
                 } catch (SftpException e) {
                     _logger.info("DTFX on " + dtfx_host + " not running.",e);
                 } 
             }
         }
     }
     
     private static void setupWatch(Setup setup) throws DTFException {
         ArrayList<Dtfc> dtfcs = setup.findAllActions(Dtfc.class);
         
         for (int i = 0; i < dtfcs.size(); i++) { 
             Dtfc dtfc = dtfcs.get(i);
             
             Dtfx dtfx = (Dtfx) dtfc.findFirstAction(Dtfx.class);
             if ( dtfx != null ) { 
                 String dtfx_host = dtfx.getHost();
                 String dtfx_user = dtfx.getUser();
                 String dtfx_path = dtfx.getPath();
                 String dtfx_rsakey = dtfx.getRsakey();
                 
                 if ( dtfx_path == null ) 
                     dtfx_path = "dtf";
 
 	            try { 
 	                Session session = SSHUtil.connectToHost(dtfx_host, dtfx_user, dtfx_rsakey);
 	                try { 
 		                SSHUtil.execute(session,
 		                                "tail -f " + dtfx_path + "/dtfx.log",
 		                                true);
 	                } finally { 
 	                    session.disconnect();
 	                }
                 } catch (JSchException e) { 
                     throw new DTFException("Unable to connect to dtfx.",e);
                 } catch (IOException e) { 
                     throw new DTFException("Unable to connect to dtfx.",e);
                 }
             }
         }
     }
     
     public static Properties getState(String component,
 	                                    String host,
 	                                    String user,
 	                                    String path,
 	                                    String rsakey) 
                   throws JSchException, SftpException, IOException { 
         Properties props = new Properties();
         
         Session session = SSHUtil.connectToHost(host, user, rsakey);
         Channel sChannel = session.openChannel("sftp");
         sChannel.connect();
         ChannelSftp csftp = (ChannelSftp)sChannel;
        
         if ( path == null ) 
             path = csftp.getHome() + "/dtf";
       
         File tmp = new File(component + ".state");
         tmp.deleteOnExit();
         FileOutputStream fos = null;
         FileInputStream fis = null;
         try { 
 	        fos = new FileOutputStream(tmp);
 	        csftp.get(path + "/state/" + component + ".state", fos);
 	        fos.close();
 	       
 	        fis = new FileInputStream(tmp);
 	        props.load(fis);
         } finally { 
             if ( fos != null ) fos.close();
             if ( fis != null ) fis.close();
         }
         
         return props;
     }
     
     public static Properties getStatus(String host, 
                                        int port) throws SocketException, IOException {
         TelnetClient telnet = new TelnetClient();
         OutputStream os = null;
         InputStream is = null;
         
         try { 
             telnet.connect(host, port);
             os = telnet.getOutputStream();
             is = telnet.getInputStream();
             
             InputStreamReader isr = new InputStreamReader(is);
             BufferedReader br = new BufferedReader(isr);
           
             try { 
 	            os.write("status\nquit\n".getBytes());
 	            os.flush();
 	
 	            ByteArrayOutputStream baos = new ByteArrayOutputStream();
 	               
 	            String line =  br.readLine();
 	            while ( line != null && !line.trim().equals("") && !line.equals("Bye")) {
 	                line = line.replaceFirst("^#","");
 	                baos.write((line + "\n").getBytes());
 	                line = br.readLine();
 	            }
 	            ByteArrayInputStream bais = 
 	                               new ByteArrayInputStream(baos.toByteArray());
 	            Properties props = new Properties();
 	            props.load(bais);
 
 	            return props;
             } finally { 
                 br.close();
             }
         } finally { 
             if ( os != null ) os.close();
             if ( is != null ) is.close();
         }
     }
    
     public static void status(String component,
                               String host,
                               String user,
                               int port)
            throws JSchException, IOException, SftpException, DTFException {
 
     }
     
     public static boolean isUp(String component,
                                String host,
                                int port)
                   throws IOException { 
         TelnetClient telnet = new TelnetClient();
         OutputStream os = null;
         InputStream is = null;
         
         try {
             try { 
                 telnet.connect(host, port);
             } catch (IOException e ) { 
                 return false;
             }
 	        os = telnet.getOutputStream();
 	        is = telnet.getInputStream();
 
             InputStreamReader isr = new InputStreamReader(is);
             BufferedReader br = new BufferedReader(isr);
 
             try { 
 	            os.write("status\nquit\n".getBytes());
 	            os.flush();
 	
 	            String line = null;
 	            while ((line = br.readLine()) != null && !line.startsWith("#"));
 		        while ( (line = br.readLine()) != null && !line.equals("# Bye"));
 		        return true;
             } finally { 
                 br.close();
             }
         } finally { 
             if ( os != null) os.close();
 	        if ( is != null ) is.close();
 	        if ( telnet.isConnected() ) telnet.disconnect();
         }
     }
     
     public static void waitForComponentToStart(String component,
                                                String host,
                                                int port)
                   throws JSchException, IOException, SftpException {
         TelnetClient telnet = new TelnetClient();
         OutputStream os = null;
         BufferedReader br = null;
         
         long start = System.currentTimeMillis();
         try {
 	        while ( (System.currentTimeMillis() - start) < 10000 ) {
 	            try { 
 	                telnet.connect(host, port);
 	            } catch (IOException e ) { 
 	                _logger.info("Waiting for [" + component + "] on " + host);
 	                ThreadUtil.pause(1000);
 	                continue;
 	            }
 		        os = telnet.getOutputStream();
 		        InputStream is = telnet.getInputStream();
 	
 	            InputStreamReader isr = new InputStreamReader(is);
 	            br = new BufferedReader(isr);
 	
 	            os.write("status\nquit\n".getBytes());
 	            os.flush();
 	
 	            String line = null;
 	            while ((line = br.readLine()) != null && !line.startsWith("#"));
 		        while ( (line = br.readLine()) != null && !line.equals("# Bye") ) 
 		        break;
 	        }
         } finally { 
 	        if ( os != null ) os.close();
 	        if ( br != null ) br.close();
 	        if ( telnet.isConnected() ) telnet.disconnect();
         }
     }
     
     public static void waitForComponentToStop(String component,
                                               String host,
                                               int debug_port)
                   throws JSchException, IOException, SftpException {
         TelnetClient telnet = new TelnetClient();
         OutputStream os = null;
         InputStream is = null;
 
         long start = System.currentTimeMillis();
         try {
             boolean shutdown = false;
             while ((System.currentTimeMillis() - start) < 10000) {
                 try {
                     telnet.connect(host, debug_port);
                 } catch (IOException e) {
                     // debug server is down so the node should be down
                     shutdown = true;
                     break;
                 }
                 _logger.info("Waiting for [" + component + 
                              "] to stop on " + host);
                 ThreadUtil.pause(500);
                 os = telnet.getOutputStream();
                 is = telnet.getInputStream();
 
                 InputStreamReader isr = new InputStreamReader(is);
                 BufferedReader br = new BufferedReader(isr);
 
                 try { 
 	                os.write("shutdown\nquit\n".getBytes());
 	                os.flush();
 	
 	                String line = null;
 	                while ((line = br.readLine()) != null && !line.startsWith("#"));
 	
 	                while ((line = br.readLine()) != null && !line.equals("# Bye"))
 	                ThreadUtil.pause(500);
                 } finally { 
                     br.close();
                 }
             }
             
             if ( !shutdown )
                 _logger.info(component + " refuses to shutdown.");
         } finally {
             if ( os != null ) os.close();
             if ( is != null ) is.close();
             if ( telnet.isConnected() ) telnet.disconnect();
         }
     }
 
     private static void setupStart(Setup setup) throws DTFException {
         ArrayList<Dtfc> dtfcs = setup.findAllActions(Dtfc.class);
         
         for (int i = 0; i < dtfcs.size(); i++) { 
             Dtfc dtfc = dtfcs.get(i);
             String dtfc_host = dtfc.getHost();
             String dtfc_user = dtfc.getUser();
 
             String hostkey = dtfc_user + "@" + dtfc_host;
             
             try { 
                 HashMap<String, String> properties = new HashMap<String, String>();
                 ArrayList<Property> props = dtfc.findActions(Property.class);
                 for (int c = 0; c < props.size(); c++) { 
                     Property prop = props.get(c);
                     properties.put(prop.getName(),prop.getValue());
                 }
                 
                 startup("dtfc", dtfc, properties, "dtfc.log");
                 waitForComponentToStart("dtfc", dtfc_host, 40000);
             } catch (JSchException e ) { 
                 _logger.error("Unable to deploy DTF to [" + hostkey + "]",e);
                 System.exit(-1);
             } catch (IOException e) {
                 _logger.error("Unable to deploy DTF to [" + hostkey + "]",e);
                 System.exit(-1);
             } catch (SftpException e) {
                 _logger.error("Unable to deploy DTF to [" + hostkey + "]",e);
                 System.exit(-1);
             }
             
             /*
              * Startup the DTFA's
              */
             ArrayList<Dtfa> dtfas = dtfc.findAllActions(Dtfa.class);
             AdminClient dsc = null;
             try { 
 	            dsc = new AdminClient(dtfc_host, 40000, _logger);
             } catch (DebugServerException e) { 
                 _logger.error("Unable to connect to debugserver on [" + 
                               dtfc_host + "], check the logs at " + hostkey + 
                               "/dtfc.log for more information.");
                 System.exit(-1);
             }
             
             try { 
                 for (int a = 0; a < dtfas.size(); a++) { 
                     Dtfa dtfa = dtfas.get(a);
 
                     StringBuffer properties = new StringBuffer();
                     ArrayList<Property> props = dtfa.findActions(Property.class);
                     for (int c = 0; c < props.size(); c++) { 
                         Property prop = props.get(c);
                         properties.append(prop.getName() + "=" + prop.getValue());
                         if ( c+1 < props.size() ) properties.append(",");
                     }
                     
                     String dtfa_host = dtfa.getHost();
                     String dtfa_user = dtfa.getUser();
                     String dtfa_path = dtfa.getPath();
                     String dtfa_rsakey = dtfa.getRsakey();
                     
                     String wrapcmd = dtfa.getWrapcmd();
                     if ( wrapcmd != null ) { 
                         wrapcmd = URLEncoder.encode(wrapcmd,"UTF8");
                     }
                     
                     String propstring = URLEncoder.encode(properties.toString(),
                                                           "UTF8");
 
                     String start = "start dtfa " + dtfa_host + " " + 
                                                    dtfa_user + 
                                                    " dtfa-" + a + ".log " + 
                                                    dtfa_path + " " +  
                                                    dtfa_rsakey + " " +
                                                    wrapcmd + " " +
                                                    propstring;
                   
                     if ( _logger.isDebugEnabled() )
                         _logger.info("Executing [" + start + "]");
                     
                     dsc.execute(start);
                 }
 
 	            /*
 	             * Startup the DTFX if it was defined
 	             */
                
                 Dtfx dtfx = (Dtfx) dtfc.findFirstAction(Dtfx.class);
                 if ( dtfx != null ) { 
                     ArrayList<Property> props = dtfx.findActions(Property.class);
                     HashMap<String, String> properties = new HashMap<String, String>();
                     for (int c = 0; c < props.size(); c++) { 
                         Property prop = props.get(c);
                         properties.put(prop.getName(), prop.getValue());
                     }
 
                     properties.put("dtf.xml.filename",dtfx.getTest());
                     properties.put("dtf.listen.addr",dtfx.getHost());
                     
                     startup("dtfx", dtfx, properties, "dtfx.log");
                 }
             } catch (JSchException e ) { 
                 _logger.error("Unable to deploy DTF to [" + hostkey + "]",e);
                 System.exit(-1);
             } catch (IOException e) {
                 _logger.error("Unable to deploy DTF to [" + hostkey + "]",e);
                 System.exit(-1);
             } catch (SftpException e) {
                 _logger.error("Unable to deploy DTF to [" + hostkey + "]",e);
                 System.exit(-1);
             } finally { 
                 dsc.close();
             }
         }
     }
     
     private static void setupInit(Setup setup,
                                   String guser,
                                   String script) throws DTFException {
         ArrayList<Dtfc> dtfcs = setup.findAllActions(Dtfc.class);
         
         for (int i = 0; i < dtfcs.size(); i++) { 
             Dtfc dtfc = dtfcs.get(i);
             String dtfc_host = dtfc.getHost();
             String dtfc_user = dtfc.getUser();
             String dtfc_path = dtfc.getPath();
             String hostkey = dtfc_user + "@" + dtfc_host;
             
             try { 
                 String user = (guser == null ? dtfc_user : guser);
                 init(dtfc_host, user, dtfc_path, script, dtfc.getWrapcmd());
             } catch (JSchException e ) { 
                 _logger.error("Unable to deploy DTF to [" + hostkey + "]",e);
                 System.exit(-1);
             } catch (IOException e) {
                 _logger.error("Unable to deploy DTF to [" + hostkey + "]",e);
                 System.exit(-1);
             } catch (SftpException e) {
                 _logger.error("Unable to deploy DTF to [" + hostkey + "]",e);
                 System.exit(-1);
             }
            
             /*
              * init the DTFA's
              */
             ArrayList<Dtfa> dtfas = dtfc.findAllActions(Dtfa.class);
             
             try { 
                 for (int a = 0; a < dtfas.size(); a++) { 
                     Dtfa dtfa = dtfas.get(a);
                     String dtfa_host = dtfa.getHost();
                     String dtfa_user = dtfa.getUser();
                     String dtfa_path = dtfa.getPath();
                  
                     String user = (guser == null ? dtfa_user : guser);
 
                     hostkey = user + "@" + dtfa_host;
                     init(dtfa_host, user, dtfa_path, script, dtfa.getWrapcmd());
                 }
 
                 /*
                  * init the DTFX if it was defined
                  */
                
                 Dtfx dtfx = (Dtfx) dtfc.findFirstAction(Dtfx.class);
                 
                 if ( dtfx != null ) { 
                     String dtfx_host = dtfx.getHost();
                     String dtfx_user = dtfx.getUser();
                     String dtfx_path = dtfx.getPath();
                    
                     String user = (guser == null ? dtfx_user : guser);
                     hostkey = user + "@" + dtfx_host;
                     init(dtfx_host, user, dtfx_path, script, dtfx.getWrapcmd());
                 }
             } catch (JSchException e ) { 
                 _logger.error("Unable to deploy DTF to [" + hostkey + "]",e);
                 System.exit(-1);
             } catch (IOException e) {
                 _logger.error("Unable to deploy DTF to [" + hostkey + "]",e);
                 System.exit(-1);
             } catch (SftpException e) {
                 _logger.error("Unable to deploy DTF to [" + hostkey + "]",e);
                 System.exit(-1);
             } 
         }
     }
 
     private static void setupStop(Setup setup) throws ParseException {
         ArrayList<Dtfc> dtfcs = setup.findAllActions(Dtfc.class);
         
         for (int i = 0; i < dtfcs.size(); i++) { 
             Dtfc dtfc = dtfcs.get(i);
             String host = dtfc.getHost();
             String user = dtfc.getUser();
             String path = dtfc.getPath();
             String rsakey = dtfc.getRsakey();
             String hostkey = user + "@" + host;
             
             try { 
                 if ( isUp("dtfc", host, 40000) ) {
                     stop("dtfc", host, user, path, rsakey);
                 } else { 
                     _logger.info("DTFC not running on " + hostkey);
                 }
             } catch (JSchException e ) { 
                 _logger.error("Unable to stop dtf setup on [" + hostkey + "]",e);
                 System.exit(-1);
             } catch (IOException e) {
                 _logger.error("Unable to stop dtf setup on [" + hostkey + "]",e);
                 System.exit(-1);
             } catch (SftpException e) {
                 _logger.error("Unable to stop dtf setup on [" + hostkey + "]",e);
                 System.exit(-1);
             }
         }
     }
     
     private static void setupSaveLogs(Setup setup) throws DTFException {
         ArrayList<Dtfc> dtfcs = setup.findAllActions(Dtfc.class);
         
         for (int i = 0; i < dtfcs.size(); i++) { 
             Dtfc dtfc = dtfcs.get(i);
             String host = dtfc.getHost();
             String user = dtfc.getUser();
             String path = dtfc.getPath();
             String rsakey = dtfc.getRsakey();
             String hostkey = user + "@" + host;
             
             File file = new File("dtf_logs");
             while ( file.exists() ) { 
                 File bklogs = new File("dtf_logs_bk");
                 if ( !bklogs.exists() && !bklogs.mkdirs() ) { 
 	                _logger.error("Unable to create [" + bklogs + "]");
 	                System.exit(-1);
                 }
                 File ren = new File("dtf_logs_bk/dtf_logs-" + System.currentTimeMillis());
                 if ( !file.renameTo(ren) ) {
                     _logger.error("Unable to rename [" + file + "] to [" +ren + "]");
                     System.exit(-1);
                 }
                 _logger.info("Old logs at dtf_logs_bk/" + ren.getName());
             }
             
             if ( !file.exists() && !file.mkdir() )  {
                 _logger.error("Unable to create [" + file + "]");
                 System.exit(-1);
             }
             
             try { 
                 collect("dtfc",
                         host,
                         user,
                         path,
                         rsakey,
                         "dtfc.log",
                         file.getAbsolutePath());
             } catch (JSchException e ) { 
                 _logger.error("Unable to deploy DTF to [" + hostkey + "]",e);
                 System.exit(-1);
             } catch (IOException e) {
                 _logger.error("Unable to deploy DTF to [" + hostkey + "]",e);
                 System.exit(-1);
             } catch (SftpException e) {
                 _logger.error("Unable to deploy DTF to [" + hostkey + "]",e);
                 System.exit(-1);
             }
             
             try { 
                 ArrayList<Dtfa> dtfas = dtfc.findAllActions(Dtfa.class);
                 
                 for (int a = 0; a < dtfas.size(); a++) { 
                     Dtfa dtfa = dtfas.get(a);
                     String dtfa_host = dtfa.getHost();
                     String dtfa_user = dtfa.getUser();
                     String dtfa_path = dtfa.getPath();
                     String dtfa_rsakey = dtfa.getRsakey();
                   
                     hostkey = user + "@" + host;
                     collect("dtfa",
                              dtfa_host,
                              dtfa_user,
                              dtfa_path,
                              dtfa_rsakey,
                              "dtfa-" + a + ".log",
                              file.getAbsolutePath());
                 }
                
                 Dtfx dtfx = (Dtfx) dtfc.findFirstAction(Dtfx.class);
                 
                 if ( dtfx != null ) { 
                     String dtfx_host = dtfx.getHost();
                     String dtfx_user = dtfx.getUser();
                     String dtfx_path = dtfx.getPath();
                     String dtfx_rsakey = dtfx.getRsakey();
                     String dtfx_logs = dtfx.getLogs();
                   
                     hostkey = user + "@" + host;
                     collect("dtfx",
                              dtfx_host,
                              dtfx_user,
                              dtfx_path,
                              dtfx_rsakey,
                              "dtfx.log",
                              file.getAbsolutePath());
                    
                     if ( dtfx_logs != null ) { 
                         String[] logs = dtfx_logs.split(",");
                         
                         for (int l = 0; l < logs.length; l++) { 
 	                        collect("dtfx",
 	                                 dtfx_host,
 	                                 dtfx_user,
 	                                 dtfx_path,
 	                                 dtfx_rsakey,
 	                                 logs[l],
 	                                 file.getAbsolutePath());
                         }
                     }
                 }
             } catch (JSchException e ) { 
                 _logger.error("Unable to deploy DTF to [" + hostkey + "]",e);
                 System.exit(-1);
             } catch (IOException e) {
                 _logger.error("Unable to deploy DTF to [" + hostkey + "]",e);
                 System.exit(-1);
             } catch (SftpException e) {
                 _logger.error("Unable to deploy DTF to [" + hostkey + "]",e);
             } 
             
             _logger.info("Logs at " + file);
         }
     }
 
     public static void startup(String component,
                                DTFNode node,
                                HashMap<String, String > properties,
                                String logname) 
                  throws JSchException, IOException, SftpException, DTFException { 
         startup(component, node, properties, logname, _logger);
     }
     
     public static void startup(String component,
                                   DTFNode node,
                                   HashMap<String, String> properties,
                                   String logname,
                                   DTFLogger logger) 
                  throws JSchException, IOException, SftpException, DTFException { 
         String host = node.getHost();
         String user = node.getUser();
         String path = node.getPath();
         String wrapcmd = node.getWrapcmd();
         
         Session session = DTFSSHSetup.setupHost(node, component, logger);
         int rc = 0;
         
         Channel sChannel = session.openChannel("sftp");
         sChannel.connect();
       
 
         if ( path == null ) {
             String home = SSHUtil.getHomeDir(session, node);
             path = home + "/dtf";
         }
 
         String hostkey = user + "@" + host + "/" + path;
        
         // don't setup the same machine with the same build if its exactly the
         // same machine + user + path
         logger.info("");
         logger.info("Starting [" + component + "] on " + host);
         logger.info("Logs at " + hostkey  + "/" + logname);
 
         properties.put("dtf.listen.addr", host);
         
         StringBuffer props = new StringBuffer();
         props.append("# properties file create by DTF deploy task\n");
         for (Entry<String, String> entry : properties.entrySet())
             props.append(entry.getKey() + "=" + entry.getValue() + "\n");
         
         File tmp = new File("deploy.props");
         tmp.deleteOnExit();
         FileOutputStream fos = null;
         FileInputStream fis = null;
        
         try { 
 	        fos = new FileOutputStream(tmp);
 	        fos.write(props.toString().getBytes());
 	       
 	        fis = new FileInputStream(tmp);
 	        
 	        String cmd = "cat > " + path + "/" + logname + ".props";
 	        cmd = wrap(wrapcmd,cmd);
 	        rc = SSHUtil.execute(session, 
 	                             cmd,
 	                             fis,
 	                             new ByteArrayOutputStream(),
 	                             new ByteArrayOutputStream());
 	        
 	        if ( rc != 0 ) { 
 	            throw new DTFException("Unable to copy props file on " + 
 	                                   hostkey + ", got rc " + rc);
 	        }
         } finally { 
             if ( fos != null ) fos.close();
             if ( fis != null ) fis.close();
         }
 	        
         String cmd = "cd " + path + " ; nohup ./ant.sh run_" + component + 
                      " -Ddtf.defaults=" + logname + ".props > " + 
                      logname + " 2>&1 &";
    
         cmd = wrap(wrapcmd,cmd);
                    
         if ( logger.isDebugEnabled() )
             logger.debug("cmd [" + cmd + "]");
         
         rc = SSHUtil.execute(session, cmd, logger.isDebugEnabled());
         
         if ( rc != 0 ) { 
             throw new DTFException("Unable to start " + 
                                    component + " on " + hostkey + " rc " + rc);
         }
         
         session.disconnect();
     }
     
     public static String wrap(String wrapcmd, String cmd) {  
         if ( wrapcmd != null ) { 
             return wrapcmd.replace("%u", "\"" + cmd + "\"");
         } else { 
             return cmd;
         }
     }
     
     private static ArrayList<String> alreadyInit = new ArrayList<String>();
     
     public static void init(String host,
                             String user,
                             String path,
                             String script,
                             String wrapcmd)
            throws JSchException, IOException, SftpException, DTFException { 
         Session session = DTFSSHSetup.setupSSH(host, user, _logger);
        String escript = wrap(wrapcmd, "cd " + path + "; ./" + script);
         
         String hostkey = user + "@" + host + "{" + script + "} in [" + wrapcmd + "]" ;
         
         if ( alreadyInit.contains(hostkey ) ) { 
 	        _logger.info("Already executed [" + escript + "] on [" + user + 
 	                     "@" + host + "]");
         } else { 
             alreadyInit.add(hostkey);
 	        
 	        FileInputStream fis = null;
 	        ChannelExec exec = null;
 	        int rc = 0;
 	        
 	        try { 
 	            String cmd = "mkdir -p " + path;
 	            cmd = wrap(wrapcmd,cmd);
 	            rc = SSHUtil.execute(session, cmd, _logger.isDebugEnabled());
 	            
 		        if ( rc != 0 ) { 
		            throw new DTFException("Unable to create [" + path + 
		                                   "] rc " + rc);
 		        }
 		        
 		        fis = new FileInputStream(script);
 		        String scriptname = new File(script).getName();
 		       
 		        cmd = "cat > " + path + "/" + scriptname;
 		        cmd = wrap(wrapcmd,cmd);
 		        rc = SSHUtil.execute(session,
 		                             cmd,
 		                             fis,
 		                             new ByteArrayOutputStream(),
 		                             new ByteArrayOutputStream());
 		        
 		        if ( rc != 0 ) { 
 		            throw new DTFException("Unable to copy script [" + 
 		                                   scriptname + "], got rc " + rc);
 		        }
 		      
 		        // fix permissions
 		        cmd = "chmod +x " + path + "/" + scriptname; 
 		        cmd = wrap(wrapcmd,cmd);
 	            rc = SSHUtil.execute(session, cmd, _logger.isDebugEnabled());
 		        if ( rc != 0 ) { 
 		            throw new DTFException("Unable to fix script permissions ["
 		                                   + scriptname + "], got rc " + rc);
 		        }
 		        
 		        exec = (ChannelExec) session.openChannel("exec");
 		        exec.setCommand(escript);
 		        _logger.info("executing [" + escript + "]");
 		        
 		        final ChannelExec fexec = exec;
 		        OutputStream aux = new OutputStream() { 
 		            StringBuffer line = new StringBuffer();
 		            
 		            @Override
 		            public void write(int b) throws IOException {
 		                System.out.write(b);
 		                line.append((char)b);
 		               
                         if ( line.toString().matches(".*Password:.*") ) { 
                             System.out.flush();
                             OutputStream os = fexec.getOutputStream();
                             InputStreamReader isr = new InputStreamReader(System.in);
                             BufferedReader br = new BufferedReader(isr);
                             String password = br.readLine();
                             os.write((password + "\n").getBytes());
                             os.flush();
                             line = new StringBuffer();
                         }
                         
                         if ( b == '\n') {
                             line = new StringBuffer();
                         }
 		            }
 		            
 		            @Override
 		            public void flush() throws IOException {
 		                System.out.flush();
 		            }
 		            
 		        };
 		        exec.setErrStream(aux);
 		        exec.setExtOutputStream(aux);
 		        exec.setOutputStream(aux);
 
 		        exec.connect();
 
 		        // wait for completion
                 while ( exec.getExitStatus() == -1 )
                     ThreadUtil.pause(1000);
                 
                 if ( exec.getExitStatus() != 0 ) { 
                     throw new DTFException("Error executing script [" +  
                                            exec.getExitStatus() + "]");
                 }
 	        } finally { 
 	            if ( fis != null ) fis.close();
 	            if ( exec != null ) exec.disconnect();
 		        session.disconnect();
 	        }
         }
     }
     
     public static void stop(String component,
 	                           String host,
 	                           String user,
 			                   String path,
 			                   String rsakey)
         throws JSchException, IOException, SftpException { 
         Session session = SSHUtil.connectToHost(host, user, rsakey);
         
         Channel sChannel = session.openChannel("sftp");
         sChannel.connect();
         ChannelSftp csftp = (ChannelSftp)sChannel;
 
         if ( path == null ) 
             path = csftp.getHome() + "/dtf";
         
         try { 
 	        _logger.info("");
 	        waitForComponentToStop(component, host, 40000);
         } finally { 
 	        csftp.disconnect();
 	        session.disconnect();
         }
     }
     
     public static void collect(String component,
 			                      String host,
 				                  String user,
 				                  String path,
 				                  String rsakey,
 				                  String logpath,
 				                  String savepath) 
         throws JSchException,
             IOException, SftpException, DTFException {
         Session session = SSHUtil.connectToHost(host, user, rsakey);
         int rc = 0;
 
         Channel sChannel = session.openChannel("sftp");
         sChannel.connect();
         ChannelSftp csftp = (ChannelSftp) sChannel;
 
         if (path == null)
             path = csftp.getHome() + "/dtf";
 
         String hostkey = user + "@" + host + ":" + path;
         String outputname = new File(logpath).getName();
 
         try {
             _logger.info("");
             if ( csftp.lstat(path + "/" + logpath).isDir() ) { 
 	            _logger.info("Compressing " + logpath + " to " + outputname + ".tgz");
 	            String cmd = "cd dtf ; tar cvfz " + outputname + ".tgz " + logpath;
 	            rc = SSHUtil.execute(session, cmd, _logger.isDebugEnabled());
                 if ( rc != 0 ) { 
                     throw new DTFException("Unable to compress log directory [" 
                                            + logpath + "]");
                 }
 	
 	            _logger.info("Saving " + hostkey + "/" + outputname
 	                          + ".tgz to " + savepath);
 	            csftp.get(path + "/" + outputname + ".tgz",
 	                      savepath + "/" + outputname + ".tgz");
 	            csftp.rm(path + "/" + outputname + ".tgz");
             } else { 
 	            _logger.info("Saving " + hostkey + "/" + logpath
 	                          + " to " + savepath);
 	            csftp.get(path + "/" + logpath, savepath + "/" + outputname);
             }
         } catch (SftpException e) { 
             _logger.warn("Unable to save [" + logpath + "] from [" + hostkey + "]");
         } finally {
             csftp.disconnect();
             session.disconnect();
         }
     }
 }
