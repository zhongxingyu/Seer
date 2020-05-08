 package com.yahoo.dtf.deploy;
 
 import java.io.BufferedReader;
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.OutputStream;
 import java.util.Properties;
 
 import com.jcraft.jsch.Channel;
 import com.jcraft.jsch.ChannelExec;
 import com.jcraft.jsch.ChannelSftp;
 import com.jcraft.jsch.JSch;
 import com.jcraft.jsch.JSchException;
 import com.jcraft.jsch.Session;
 import com.jcraft.jsch.SftpException;
 import com.yahoo.dtf.actions.protocol.deploy.DTFNode;
 import com.yahoo.dtf.exception.DTFException;
 import com.yahoo.dtf.logger.DTFLogger;
 import com.yahoo.dtf.util.ThreadUtil;
 
 public class SSHUtil {
     
     private static DTFLogger _logger = DTFLogger.getLogger(SSHUtil.class);
 
     public static Session connectToHost(String host,
                                           String user,
                                           String privkey,
                                           String passphrase) throws JSchException { 
         JSch jsch = new JSch();
         DeployUI ui = new DeployUI(host,user,passphrase);
     
         String home = System.getProperty("user.home");
         String known_hosts = home + "/.dtf/known_hosts";
         String id_rsa = null;
         
         if ( privkey != null ) {
             id_rsa = privkey;
             _logger.info("Using specified RSA key [" + id_rsa + "]");
         } else { 
             id_rsa = home + "/.dtf/id_rsa";
             _logger.info("Using default RSA key [" + id_rsa + "]");
         }
         
         if (new File(known_hosts).exists())
             jsch.setKnownHosts(known_hosts);
     
         if (new File(id_rsa).exists())
             jsch.addIdentity(id_rsa);
 
         Session session = jsch.getSession(user, host);
   
 // XXX: this seems to break others builds and I will have to revist this setting
 //        Properties config = new Properties();
 //        config.put("compression.s2c", "zlib,none");
 //        config.put("compression.c2s", "zlib,none");
 //        session.setConfig(config);
         session.setUserInfo(ui);
         session.connect();
     
         return session;
     }
     
     public static int execute(Session session,
                                String command,
                                boolean showoutput)
                     throws JSchException, IOException { 
         ChannelExec exec = (ChannelExec) session.openChannel("exec");
         exec.setCommand(command);
         InputStream is = exec.getInputStream();
         exec.connect();
    
         BufferedReader br = null;
         if ( _logger.isDebugEnabled()) 
             _logger.debug("executing [" + command + "]");
         
         try { 
             InputStreamReader esr = new InputStreamReader(exec.getErrStream());
 	        InputStreamReader isr = new InputStreamReader(is);
 	        br = new BufferedReader(isr);
 	    
 	        String line = null;
 	        while ( (line = br.readLine()) != null ) {
 	            if ( showoutput) 
 	                System.out.println(line);
 	        }
 	        
             br = new BufferedReader(esr);
 	        while ( (line = br.readLine()) != null ) {
 	            if ( showoutput) 
 	                System.err.println(line);
 	        }
 	        
 	        return exec.getExitStatus();
         } finally { 
             if ( br != null ) br.close();
             exec.disconnect();
         }
     }
     
     
     public static int execute(Session session,
                               String command,
                               InputStream in,
                               OutputStream out,
                               OutputStream err)
            throws JSchException, IOException {
         ChannelExec exec = (ChannelExec) session.openChannel("exec");
 
         if (_logger.isDebugEnabled())
             _logger.debug("executing [" + command + "]");
         
         exec.setCommand(command);
         exec.connect();
        
         OutputStream os = exec.getOutputStream();
         byte[] buffer = new byte[1024];
         int read = 0;
         while ( (read = in.read(buffer)) != -1 )
             os.write(buffer,0,read);
         
         os.close();
 
         BufferedReader br = null;
 
         try {
            InputStream ise = null;
             String line = null;
            
             try { 
                 ise = exec.getErrStream();
             } catch (NullPointerException e) { }
             
             if ( ise != null ) { 
                 InputStreamReader esr = new InputStreamReader(ise);
                 br = new BufferedReader(esr);
                 while ((line = br.readLine()) != null) {
                     err.write((line + "\n").getBytes());
                 }
             }
             
             InputStream is = null;
             try { 
                 is = exec.getInputStream();
             } catch (NullPointerException e) { }
            
             if ( is != null ) { 
 	            InputStreamReader isr = new InputStreamReader(is);
 	            br = new BufferedReader(isr);
 	
 	            while ((line = br.readLine()) != null) {
 	                out.write((line + "\n").getBytes());
 	            }
             }
 


             return exec.getExitStatus();
         } finally {
             if (br != null)
                 br.close();
             exec.disconnect();
         }
     }
     
     public static String getHomeDir(Session session, DTFNode node)
            throws JSchException, IOException, DTFException {
         String cmd = DeployDTF.wrap(node.getWrapcmd(), "pwd");
         
         ByteArrayOutputStream out = new ByteArrayOutputStream();
         ByteArrayOutputStream err = new ByteArrayOutputStream();
         int rc = SSHUtil.execute(session, cmd, new ByteArrayInputStream(
                 new byte[0]), out, err);
         if (rc != 0) {
             throw new DTFException("Unable to determine home, got rc " + rc);
         }
 
         return new String(out.toByteArray()).replaceAll("\n", "");
     }
 }
