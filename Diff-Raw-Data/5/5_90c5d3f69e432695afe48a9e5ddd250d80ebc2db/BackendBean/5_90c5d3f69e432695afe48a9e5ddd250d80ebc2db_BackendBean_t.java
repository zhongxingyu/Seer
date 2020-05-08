 package se.kudomessage.torsken;
 
 import java.io.BufferedReader;
 import java.io.InputStreamReader;
 import java.io.OutputStreamWriter;
 import java.io.PrintWriter;
 import java.net.Socket;
 import javax.faces.bean.ManagedBean;
 import javax.faces.bean.SessionScoped;
 import org.icefaces.application.PushRenderer;
 import org.json.JSONArray;
 import org.json.JSONObject;
 
 @SessionScoped
 @ManagedBean
 public class BackendBean {
     private Socket socket = null;
     private BufferedReader in = null;
     private PrintWriter out = null;
     private boolean loaded = false;
 
     public String init() {
         if (!loaded) {
             loaded = true;
             doInit();
         }
 
         return "";
     }
 
     private void doInit() {
         try {
             socket = new Socket(CONSTANTS.SERVER_ADDRESS, CONSTANTS.SERVER_PORT);
         } catch (Exception e) {
             e.printStackTrace();
         }
 
         if (socket != null) {
             in = null;
             out = null;
 
             try {
                 in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
 
                 OutputStreamWriter outstream = new OutputStreamWriter(socket.getOutputStream(), "UTF-8");
                 out = new PrintWriter(outstream, true);
             } catch (Exception e) {
                 e.printStackTrace();
             }
 
             if (in != null && out != null) {
                 System.out.println("Connected to server " + CONSTANTS.SERVER_ADDRESS + ":" + CONSTANTS.SERVER_PORT);
 
                // TODO: Do all things below in SocketHandler's constructor?
                
                 out.println("CLIENT");
                 out.flush();
 
                 try {
                     JSONObject output = new JSONObject();
                     output.put("action", "init");
                     output.put("token", Globals.accessToken);
 
                     out.println(output.toString());
                     out.flush();
 
                     // First answer from server is the email.
                     Globals.email = in.readLine();
 
                     // Global PushRenderer
                     Globals.pr = PushRenderer.getPortableRenderer();
                     ConversationsHolder.getInstance().addViewToPush();
 
                     // Get contacts
                     output = new JSONObject();
                     output.put("action", "get-contacts");
 
                     out.println(output.toString());
                     out.flush();
 
                     JSONObject c = new JSONObject(in.readLine());
                     JSONArray d = c.getJSONArray("contacts");
 
                     for (int i = 0; i < d.length(); i++) {
                         String name = d.getJSONObject(i).getString("name");
                         String number = d.getJSONObject(i).getString("number");
 
                         Globals.contacts.put(number, name);
                     }
 
                     // Load the first emails
                     output = new JSONObject();
                     output.put("action", "get-messages");
                     output.put("lower", "0");
                     output.put("upper", "5");
 
                     out.println(output.toString());
                     out.flush();
                     
                     JSONObject e = new JSONObject(in.readLine());
                     JSONArray f = e.getJSONArray("messages");
                     
                     System.out.println("###### Got " + f.length());
 
                     for (int i = 0; i < f.length(); i++) {
                         KudoMessage message = new KudoMessage();
                         message.content = f.getJSONObject(i).getString("content");
                        message.origin = f.getJSONObject(i).getString("origin");
                         message.addReceiver(f.getJSONObject(i).getString("receiver"));
                         
                         ConversationsHolder.getInstance().addMessage(message);
                     }
                 } catch (Exception ex) {
                     System.err.println("Something wrong in action init: " + ex.toString());
                 }
 
                 // Read new messages in new thread.
                 (new SocketHandler(socket, in, out)).start();
             }
         }
     }
 }
