 package controllers.client;
 
import controllers.ConnectionFactory;
 import controllers.Workflow;
 import org.junit.Test;
 import view.ClientDetailsView;
 
 import static org.mockito.Mockito.*;
 
 public class ClientDetailsControllerTest {
     @Test
     public void connect_to_server_on_localhost_is_successful() {
         Workflow workflow = mock(Workflow.class);
         ClientDetailsView view = mock(ClientDetailsView.class);
        ConnectionFactory connectionFactory = mock(ConnectionFactory.class);
         ClientDetailsController controller = new ClientDetailsController(workflow,connectionFactory);
         controller.bind(view);
         when(view.getServerName()).thenReturn("localhost");
         controller.connectToServer();
         verify(connectionFactory).connectToServer("localhost",1254,controller);
     }
 }
