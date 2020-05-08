 package br.com.tuxknife.main;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import java.io.IOException;
 import java.io.InputStream;
 
 import com.jcraft.jsch.Channel;
 import com.jcraft.jsch.ChannelExec;
 import com.jcraft.jsch.JSch;
 import com.jcraft.jsch.JSchException;
 import com.jcraft.jsch.Session;
 import org.eclipse.jetty.server.Request;
 import org.eclipse.jetty.server.Server;
 import org.eclipse.jetty.server.handler.AbstractHandler;
 
 public class Main {
 
     public static void main(String[] args) throws Exception {
         Server server = new Server(8083);
         server.setHandler( new MainHandler() );
         server.start();
         server.join();
     }
 
     private static class MainHandler extends AbstractHandler {
 
         public static final String REGEX_SERVER = "^/servers/(.*)$";
         public static final int TIMEOUT_IN_SECONDS = 5000;
 
         @Override
         public void handle(String url, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
 
             StringBuilder result = new StringBuilder();
 
             if (url.matches(REGEX_SERVER)) {
 
                 String server = url.replaceAll(REGEX_SERVER, "$1");
                 System.out.println("Request to server " + server);
 
                 String username = request.getParameter("username");
                 String password = request.getParameter("password");
 
                 response.setStatus(HttpServletResponse.SC_OK);
                 response.setHeader("Access-Control-Allow-Origin", "*");
                 response.setContentType("application/json;charset=utf-8");
                 baseRequest.setHandled(true);
 
                String commandResponse = "";
                String commandError = "";
                 try {
                     commandResponse = executeCommand(server, username, password, "ls -ls");
                 } catch (JSchException e) {
                    commandError = "Err: " + e.getMessage();
                 }
 
                 result.append("{")
                         .append("\"host\": \"").append(server).append("\", ")
                         .append("\"username\":\"").append(username).append("\", ")
                         .append("\"password\":\"").append(password).append("\",")
                        .append("\"commandError\":\"").append(commandError).append("\",")
                         .append("\"commandResponse\":\"").append(commandResponse).append("\"")
                         .append("}");
                 System.out.println(result);
             }
             response.getWriter().print(result);
         }
 
         private String executeCommand(String server, String username, String password, String command) throws JSchException, IOException {
 
             JSch jSch = new JSch();
 
             Session session = jSch.getSession(username, server);
 
             session.setPassword(password);
             session.setConfig("StrictHostKeyChecking", "no");
 
             session.connect(TIMEOUT_IN_SECONDS);
 
             Channel channel=  session.openChannel("exec");
             ((ChannelExec)channel).setCommand(command);
 
             // X Forwarding
             // channel.setXForwarding(true);
 
             //channel.setInputStream(System.in);
             channel.setInputStream(null);
 
             //channel.setOutputStream(System.out);
 
             //FileOutputStream fos=new FileOutputStream("/tmp/stderr");
             //((ChannelExec)channel).setErrStream(fos);
             ((ChannelExec)channel).setErrStream(System.err);
 
             InputStream in=channel.getInputStream();
 
             channel.connect();
 
             StringBuilder result = new StringBuilder();
 
             byte[] tmp=new byte[1024];
             while(true){
                 while(in.available()>0){
                     int i=in.read(tmp, 0, 1024);
                     if(i<0)break;
                     result.append(new String(tmp, 0, i));
                 }
                 if(channel.isClosed()){
                     System.out.println("exit-status: "+channel.getExitStatus());
                     break;
                 }
             }
             channel.disconnect();
             session.disconnect();
 
             return result.toString().replaceAll("\\n","");
         }
     }
 }
