 import com.smsgh.ApiHost;
 import com.smsgh.ApiMessage;
 import com.smsgh.ApiException;
 
 public class Demo {
     /**
      * Main
      */
     public static void Main(String[] args) {
         ApiHost apiHost = new ApiHost();
        apiHost.setClientId("user123");
        apiHost.setClientSecret("secret");
         
         try {
             /**
              * Sending a quick message.
              */
             apiHost.getMessagesResource()
                 .send("SMSGH", "+233248183783", "Hello world!");
                 
             /**
              * Sending a message with extended properties.
              */
             ApiMessage apiMessage = new ApiMessage();
             apiMessage.setFrom("SMSGH");
             apiMessage.setTo("+233248183783");
             apiMessage.setContent("Hello world!");
             apiMessage.setRegisteredDelivery(true);
             apiHost.getMessagesResource().send(apiMessage);
             
             /**
              * Scheduling a message.
              */
             // ApiMessage
             apiMessage = new ApiMessage();
             apiMessage.setFrom("SMSGH");
             apiMessage.setTo("+233248183783");
             apiMessage.setContent("Hello world!");
             apiHost.getMessagesResource()
                 .schedule(apiMessage, new java.util.Date());
         } catch (ApiException ex) {
             System.out.println("Exception: " + ex.getMessage());
         }
     }
 }
