 import flex.messaging.io.amf.client.AMFConnection;
 import flex.messaging.io.amf.client.exceptions.ClientStatusException;
 import flex.messaging.io.amf.client.exceptions.ServerStatusException;
 import flex.messaging.messages.RemotingMessage;
 
 public class RemotingClient
 {
 	public static void main(String[] args)
     {
         AMFConnection amfConnection = new AMFConnection();
         
         String url = "http://demo.pyamf.org/gateway/echo";
        String service = "service.getLanguages";
         try {
             amfConnection.connect(url);
         } catch (ClientStatusException cse) {
             System.out.println(cse);
             return;
         }
         // Make a remoting call and retrieve the result.
         try {
            Object result = amfConnection.call(service);
             System.out.println("results: " + result.toString());
         } catch (ClientStatusException cse) {
             System.out.println(cse);
         } catch (ServerStatusException sse) {
             System.out.println(sse);
         }
         
         // Close the connection.
         amfConnection.close();
 	}
 
 }
