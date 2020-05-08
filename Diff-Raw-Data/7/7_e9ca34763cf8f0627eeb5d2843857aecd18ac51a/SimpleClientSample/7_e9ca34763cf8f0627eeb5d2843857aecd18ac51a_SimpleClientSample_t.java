 package xdi2.example.client;
 
 import org.apache.log4j.Level;
 import org.apache.log4j.LogManager;
 
 import xdi2.client.XDIClient;
 import xdi2.client.http.XDIHttpClient;
 import xdi2.core.io.XDIWriter;
 import xdi2.core.io.XDIWriterRegistry;
 import xdi2.core.xri3.XDI3Segment;
 import xdi2.core.xri3.XDI3Statement;
 import xdi2.messaging.Message;
 import xdi2.messaging.MessageEnvelope;
 import xdi2.messaging.MessageResult;
 
 public class SimpleClientSample {
 
     static XDIWriter writer = XDIWriterRegistry.forFormat("XDI/JSON", null);
 
     static XDIClient client = new XDIHttpClient("http://localhost:8080/xdi/mem-graph");
 
     static void doSet() throws Exception {
 
         MessageEnvelope messageEnvelope = new MessageEnvelope();
         Message message = messageEnvelope.createMessage(XDI3Segment.create("=sender"));
         message.createSetOperation(XDI3Statement.create("=markus<+name>&/&/\"Markus\""));
        message.createSetOperation(XDI3Statement.create("=markus/+friend/=animesh"));
 
         client.send(messageEnvelope, null);
     }
 
     static void doGet() throws Exception {
 
         MessageEnvelope messageEnvelope = new MessageEnvelope();
         Message message = messageEnvelope.createMessage(XDI3Segment.create("=sender"));
        message.createGetOperation(XDI3Segment.create(""));
 
         MessageResult messageResult = new MessageResult();
         client.send(messageEnvelope, messageResult);
         writer.write(messageResult.getGraph(), System.out);
     }
 
     static void doDel() throws Exception {
 
         MessageEnvelope messageEnvelope = new MessageEnvelope();
         Message message = messageEnvelope.createMessage(XDI3Segment.create("=sender"));
        message.createDelOperation(XDI3Segment.create("=markus"));
 
         client.send(messageEnvelope, null);
     }
 
     public static void main(String[] args) throws Exception {
 
         LogManager.getLogger("xdi2").setLevel(Level.OFF);
 
         // run a $set message
 
         System.out.println("Running $set");
         doSet();
         System.out.println();
 
         // run a $get message
 
         System.out.println("Running $get");
         doGet();
         System.out.println();
 
         // run a $del message
 
         System.out.println("Running $del");
         doDel();
         System.out.println();
 
         // run a $get message
 
         System.out.println("Running $get");
         doGet();
         System.out.println();
    }
 }
