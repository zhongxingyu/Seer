 package iTests.framework.utils;
 
 import com.gigaspaces.internal.utils.StringUtils;
 import com.jcraft.jsch.*;
 import org.apache.tools.ant.taskdefs.optional.ssh.SSHExec;
 import org.openspaces.admin.Admin;
 import org.openspaces.grid.gsm.machines.plugins.exceptions.ElasticMachineProvisioningException;
 import org.testng.Assert;
 
 import java.io.*;
 import java.util.concurrent.TimeUnit;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 public class SSHUtils {
     
     public static class SSHExecutor implements Runnable {
         
         JSch jsch = null;
         Session session;
         Channel channel;
         InputStream commandInputStream;
         StringBuilder sb = new StringBuilder();
         
         String username;
         String host;
         String password;
         String command;
         
         public SSHExecutor(String username, String host, String password) {
             this.username = username;
             this.host = host;
             this.password = password;
             jsch = new JSch();
         }
         
         public void open() {
             try {
                 session = jsch.getSession(username , host, 22);
                 UserInfo ui = new MyUserInfo(password);
                 session.setUserInfo(ui);
                 session.connect();
             } catch (Exception e) {
                 Assert.fail();
             }
         }
     
         public void exec() {
             try {
                 channel = session.openChannel("exec");
                 ((ChannelExec) channel).setCommand(command);
                 channel.setInputStream(null);
                 ((ChannelExec) channel).setErrStream(System.err);
                 commandInputStream = channel.getInputStream();
                 channel.connect();
             } catch (Exception e) {
                 Assert.fail();
             }
         }
         
         public void close() {
             channel.disconnect();
             session.disconnect();
         }
         
         public void openExecuteAndClose() {
             open();
             exec();
             getCommandOutput();
             close();
         }
         
         public String getCommandOutput() {
             try {
                 byte[] tmp = new byte[1024];
                 while (true) {
                     while (commandInputStream.available() > 0) {
                         int i = commandInputStream.read(tmp, 0, 1024);
                         if (i < 0) {
                             break;
                         }
                         sb.append(new String(tmp, 0, i));
                     }
                     if (channel.isClosed()) {
                         break;
                     }
                     Thread.sleep(1000);
                 }
                 return sb.toString();}
             catch (Exception e) {
                Assert.fail("Failed to get ssh command output", e);
             }
             return "";
         }
         
         public void setCommand(String command) {
             this.command = command;
         }
 
         @Override
         public void run() {
             try {
                 open();
                 exec();
                 byte[] tmp = new byte[1024];
                 while (true) {
                     while (commandInputStream.available() > 0) {
                         int i = commandInputStream.read(tmp, 0, 1024);
                         if (i < 0) {
                             break;
                         }
                         sb.append(new String(tmp, 0, i));
                     }
                     if (channel.isClosed()) {
                         break;
                     }
                     Thread.sleep(1000);  
                 }
             } catch (Exception e) {
                 e.printStackTrace();
             }
         }
         
         
         /***
          * 
          * @return PID of running gsc/gsm/... (not a general use method)
          * @throws java.io.IOException
          * @throws InterruptedException
          */
         public int getJavaProcessID() {
             String scriptOutput = sb.toString();
             int pid = -1; 
             
             //This regular expression captures the script's pid
             String regex = "(?:Log file:.+-)([0-9]+)(?:\\.log)";
             Pattern pattern = Pattern.compile(regex);
             Matcher matcher = pattern.matcher(scriptOutput);
             if (matcher.find()) {
                 pid = Integer.parseInt(matcher.group(1));
             }
             return pid;
         }
     }
     
     public static class SSHFileCopy {
         
         private JSch jsch = null;
         private Session session;
         private Channel channel;
         private InputStream channelIn;
         private OutputStream channelOut;
         private FileInputStream fis;
 
         private final String localPath;
         private final String username;
         private final String host;
         private final String password;
         private final String remotePath;
         
         public SSHFileCopy(String username, String host, String password, String localPath, String remotePath) {
             this.username = username;
             this.host = host;
             this.password = password;
             this.localPath = localPath;
             this.remotePath = remotePath;
             jsch = new JSch();
         }
         
         public void open() throws JSchException {
             session = jsch.getSession(username , host, 22);
             UserInfo ui = new MyUserInfo(password);
             session.setUserInfo(ui);
             session.connect();
         }
         
         public void copy() throws Exception {
             // exec 'scp -t rfile' remotely
             String command = "scp -p -t " + remotePath;
             channel = session.openChannel("exec");
             ((ChannelExec) channel).setCommand(command);
 
             // get I/O streams for remote scp
             channelOut = channel.getOutputStream();
             channelIn = channel.getInputStream();
 
             channel.connect();
 
             if (checkAck(channelIn) != 0) {
                 throw new SSHException();
             }
 
             // send "C0644 filesize filename", where filename should not include
             // '/'
             long filesize = (new File(localPath)).length();
             command = "C0644 " + filesize + " ";
             if (localPath.lastIndexOf('/') > 0) {
                 command += localPath.substring(localPath.lastIndexOf('/') + 1);
             } else {
                 command += localPath;
             }
             command += "\n";
             channelOut.write(command.getBytes());
             channelOut.flush();
             if (checkAck(channelIn) != 0) {
                 throw new SSHException();
             }
 
             // send a content of lfile
             fis = new FileInputStream(localPath);
             byte[] buf = new byte[1024];
             while (true) {
                 int len = fis.read(buf, 0, buf.length);
                 if (len <= 0)
                     break;
                 channelOut.write(buf, 0, len); // out.flush();
             }
             fis.close();
             fis = null;
             // send '\0'
             buf[0] = 0;
             channelOut.write(buf, 0, 1);
             channelOut.flush();
             if (checkAck(channelIn) != 0) {
                 throw new SSHException();
             }
             channelOut.close();
         }
         
         public void close() {
             channel.disconnect();
             session.disconnect();
         }
         
         public void openCopyAndClose() throws Exception {
             open();
             copy();
             close();
         }
         
         static int checkAck(InputStream in) throws IOException {
             int b = in.read();
             // b may be 0 for success,
             // 1 for error,
             // 2 for fatal error,
             // -1
             if (b == 0)
                 return b;
             if (b == -1)
                 return b;
 
             if (b == 1 || b == 2) {
                 StringBuffer sb = new StringBuffer();
                 int c;
                 do {
                     c = in.read();
                     sb.append((char) c);
                 } while (c != '\n');
                 if (b == 1) { // error
                     System.out.print(sb.toString());
                 }
                 if (b == 2) { // fatal error
                     System.out.print(sb.toString());
                 }
             }
             return b;
         }
         
     }
     
     private static class MyUserInfo implements UserInfo {
         
         private final String password;
         
         MyUserInfo(String password) {
             this.password = password;
         }
 
         @Override
         public String getPassword() { return password; }
         @Override
         public boolean promptYesNo(String s) { return true; }
         @Override
         public String getPassphrase() { return null; }
         @Override
         public boolean promptPassphrase(String m) { return true; }
         @Override
         public boolean promptPassword(String m) { return true; }
         @Override
         public void showMessage(String m) { }
     }
  
     @SuppressWarnings("serial")
     private static class SSHException extends Exception
     {
     	private static final long serialVersionUID = 1L;
     }
 
 	public static final long DEFAULT_TIMEOUT = 60 * 1000;
 	public static final String SSH_USERNAME = "tgrid";
 	public static final String SSH_PASSWORD = "tgrid";
 
     /* 
      * Kill process on a linux machine (lab) by passing its PID. Will ignore requests on different machines 
      * (i.e: windows)
      */
     public static boolean killProcess(String ipAddress, int pid) {
         
         String labPrefix = SetupUtils.LINUX_HOST_PREFIX;
         if (!ipAddress.startsWith(labPrefix)) {
             return false;
         }
         
         long timeoutMilliseconds = 1000;
         String username = SetupUtils.USERNAME;
         String password = SetupUtils.PASSWORD;
         String command = "kill -9 " + pid;
         
         runCommand(ipAddress, timeoutMilliseconds, command, username, password);
         
         return true;
     }
     
     public static void waitForSSH(String ipAddress,
             String username, String password, long timeout, TimeUnit unit) throws ElasticMachineProvisioningException, InterruptedException{
 
         long timestamp = System.currentTimeMillis() + unit.toMillis(timeout); 
         String result = "";
         while (!result.contains("ping")) {
             try {
                 result = runCommand(ipAddress,5000,"echo ping", username, password);
             } catch (Exception e) {
                 if (System.currentTimeMillis() > timestamp) {
                     Assert.fail("Timeout connecting to SSH server",e);
                 }
             }
             LogUtils.log("SSH server is not responding, retrying");
             Thread.sleep(5000);
         }
     }
 
     public static String runCommand(String ipAddress, long timeoutMilliseconds, String command,
             String username, String password) {
         File output = null;
         try {
             output = File.createTempFile("sshCommand", ".txt");
             try {
                 final SSHExec task = new SSHExec();
                 task.setOutput(output);
                 // ssh related parameters
                task.setFailonerror(true); // throw exception if exit code is not 0
                 task.setCommand(command);
                 task.setHost(ipAddress);
                 task.setTrust(true);
                 task.setUsername(username);
                 task.setPassword(password);
                 task.setTimeout(timeoutMilliseconds);
                 task.execute();
                 String response = readFileAsString(output);
                 LogUtils.log(response);
                 return response;
             } catch(Exception e) {
                 String failResponse = readFileAsString(output);
                 LogUtils.log(failResponse);
                 Assert.fail("Failed running ssh command: '" + command + "' on " + ipAddress +": " + e.getMessage());
             }
         }catch (IOException e){
             Assert.fail("Failed creating temp file.", e);
         } catch(Exception e) {
             Assert.fail("Failed running ssh command: '" + command + "' on " + ipAddress);
         }
         finally {
             if (output != null){
                 output.delete();
             }
         }
         return null;
     }
 
     public static String runCommand(String ipAddress, long timeoutMilliseconds, String command,
                                     String username, File pemFile) {
         File output = null;
         try {
             output = File.createTempFile("sshCommand", ".txt");
             try {
                 final SSHExec task = new SSHExec();
                 task.setOutput(output);
                 // ssh related parameters
                 task.setFailonerror(true); // throw exception if exit code is not 0
                 task.setCommand(command);
                 task.setHost(ipAddress);
                 task.setTrust(true);
                 task.setUsername(username);
                 task.setKeyfile(pemFile.getAbsolutePath());
                 task.setTimeout(timeoutMilliseconds);
                 task.execute();
                 String response = readFileAsString(output);
                 return response;
             } catch(Exception e) {
                 String failResponse = readFileAsString(output);
                 Assert.fail("Failed running ssh command: '" + command + "' on " + ipAddress +": " + e.getMessage() +
                         ". SSH output was: " + failResponse, e);
             }
         }catch (IOException e){
             Assert.fail("Failed creating temp file.", e);
         }
         finally {
             if (output != null){
                 output.delete();
             }
         }
         return null;
     }
 
     private static String readFileAsString(File file) throws IOException {
         StringBuffer fileData = new StringBuffer(1000);
         BufferedReader reader = new BufferedReader(new FileReader(file));
         char[] buf = new char[1024];
         int numRead = 0;
         while ((numRead = reader.read(buf)) != -1) {
             String readData = String.valueOf(buf, 0, numRead);
             fileData.append(readData);
             buf = new char[1024];
         }
         reader.close();
         return fileData.toString();
     }
     
     public static String runGroovyFile(String host, long timeoutMilliseconds, String username, String password, String groovyFilePath,Admin admin){
 		String path = ScriptUtils.getBuildPath() + "/tools/groovy/bin" ;
 		
 		return SSHUtils.runCommand(host, timeoutMilliseconds,
                 "export LOOKUPGROUPS='" + StringUtils.arrayToCommaDelimitedString(admin.getGroups()) + "';" +
                         "export LOOKUPLOCATORS='" + StringUtils.arrayToCommaDelimitedString(admin.getLocators()) + "';" +
                         "cd " + path + ";./groovy " + groovyFilePath, username, password);
 	}
     
     public static void validateSSHUp(String  host, String username, String password) throws JSchException{
     	JSch jsch = new JSch();
     	Session session = jsch.getSession(username , host, 22);
         UserInfo ui = new MyUserInfo(password);
         session.setUserInfo(ui);
         session.connect();
     }
 }
