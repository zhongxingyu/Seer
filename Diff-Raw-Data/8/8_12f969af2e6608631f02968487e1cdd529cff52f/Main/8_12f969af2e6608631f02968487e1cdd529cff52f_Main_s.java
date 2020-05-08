 package trading.app.interop.fix;
 
 import com.devexperts.fast.client.fast.multicast.MulticastClientInputStream;
 import com.devexperts.fast.client.openfast.OpenFastInput;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.InetAddress;
 import java.net.MulticastSocket;
 import java.net.Socket;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import org.openfast.Context;
 import org.openfast.Message;
 import org.openfast.MessageBlockReader;
 import org.openfast.MessageInputStream;
 import org.openfast.error.FastException;
 import org.openfast.session.Connection;
 import org.openfast.session.FastConnectionException;
 import org.openfast.session.tcp.TcpEndpoint;
 
 import org.openfast.template.MessageTemplate;
 import org.openfast.template.loader.MessageTemplateLoader;
 import org.openfast.template.loader.XMLMessageTemplateLoader;
 
 public class Main {
     public final static String host = "194.247.133.77";
     public final static int port = 5001;
 
     /**
      * @param args See command line options added to "options" above.
      */
     public static void main(String[] args) throws FileNotFoundException, IOException, FastConnectionException {
 
         // Load templates
         InputStream templateSource = new FileInputStream("config//templates.xml");
         MessageTemplateLoader templateLoader = new XMLMessageTemplateLoader();
         //MessageTemplate[] templates = templateLoader.load(templateSource);
         //MessageTemplate tpl = templates[0];
         Context context = new Context();
         context.setTemplateRegistry(templateLoader.getTemplateRegistry());  
         
         // Connect
 
   
        org.openfast.session.tcp.TcpEndpoint endpoint = new TcpEndpoint(host, port);
  
        Connection c  = endpoint.connect();
 
         final Socket socket = new Socket(host, port);      
  
         // Create input stream
         MessageInputStream inputStream = new MessageInputStream(socket.getInputStream(), context);
         inputStream.setBlockReader(new MessageBlockReader(){
 
             @Override
             public boolean readBlock(InputStream in) {
                 return true;
             }
 
             @Override
             public void messageRead(InputStream in, Message message) {
                
             }
         });
    Runtime.getRuntime().addShutdownHook(new Thread() {
             public void run() {
                 try {
                     socket.close();
                 } catch (IOException ex) {
                     Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                 }
             }
         });     
         // Message loop
         while (true) {
             try {
                 Message message = inputStream.readMessage();
                 System.out.println(message.toString());
             }
             catch(final FastException e) {
                 System.err.println(e.getMessage());
             }
         }
 
         //FileOutputStream os = new FileOutputStream(outputFile, true)       
 
     }
 }
