 /**
  * 
  */
 package org.restq.server.router.service.impl;
 
 import static org.mockito.Matchers.any;
 import static org.mockito.Mockito.mock;
 import static org.mockito.Mockito.*;
 
 import java.io.Serializable;
 
 import org.restq.cluster.Cluster;
 import org.restq.cluster.Member;
 import org.restq.cluster.Node;
 import org.restq.cluster.Partition;
 import org.restq.cluster.PartitionStrategy;
 import org.restq.cluster.nio.Connection;
 import org.restq.cluster.nio.ConnectionManager;
 import org.restq.cluster.nio.ResponseFuture;
 import org.restq.core.Request;
 import org.restq.messaging.Destination;
 import org.restq.messaging.EnqueueRequest;
 import org.restq.messaging.ServerMessage;
 import org.restq.messaging.service.MessageService;
 import org.testng.annotations.BeforeMethod;
 import org.testng.annotations.Test;
 
 /**
  * @author ganeshs
  *
  */
 public class RouterServiceImplTest {
 
 	private MessageService messageService;
 	
 	private Node node;
 	
 	private Member member;
 	
 	private Cluster cluster; 
 	
 	private PartitionStrategy partitionStrategy;
 	
 	private ConnectionManager connectionManager;
 	
 	private RouterServiceImpl routerService;
 	
 	@BeforeMethod
 	public void setup() {
 		messageService = mock(MessageService.class);
 		node = mock(Node.class);
 		cluster = mock(Cluster.class);
 		member = mock(Member.class);
 		when(node.getCluster()).thenReturn(cluster);
 		when(node.getMember()).thenReturn(member);
 		partitionStrategy = mock(PartitionStrategy.class);
 		connectionManager = mock(ConnectionManager.class);
 		routerService = new RouterServiceImpl(messageService, node, partitionStrategy, connectionManager);
 	}
 	
 	@Test
 	public void shouldRouteMessageWithinTheNode() {
 		ServerMessage message = mock(ServerMessage.class);
 		Destination destination = mock(Destination.class);
 		Partition partition = new Partition(1);
		when(partitionStrategy.getPartition((Serializable)any())).thenReturn(partition);
 		when(cluster.getMember(partition)).thenReturn(member);
 		routerService.routeMessage(destination, message);
 		verify(messageService).sendMessage(destination, message);
 	}
 	
 	@Test
 	public void shouldRouteMessageToAnotherNode() {
 		routerService = spy(routerService);
 		ServerMessage message = mock(ServerMessage.class);
 		Destination destination = mock(Destination.class);
 		Partition partition = new Partition(1);
		when(partitionStrategy.getPartition((Serializable)any())).thenReturn(partition);
 		Member someOtherMember = mock(Member.class);
 		when(cluster.getMember(partition)).thenReturn(someOtherMember);
 		doNothing().when(routerService).sendMessageToMember(destination, message, someOtherMember);
 		routerService.routeMessage(destination, message);
 		verify(messageService, never()).sendMessage(destination, message);
 		verify(routerService).sendMessageToMember(destination, message, someOtherMember);
 	}
 	
 	@Test
 	public void shouldSendMessageToAnotherMember() {
 		Destination destination = mock(Destination.class);
 		when(destination.getId()).thenReturn("test123");
 		ServerMessage message = mock(ServerMessage.class);
 		Connection connection = mock(Connection.class);
 		Request request = new EnqueueRequest("test123", message);
 		when(connection.send(request)).thenReturn(mock(ResponseFuture.class));
 		when(connectionManager.getConnection(member)).thenReturn(connection);
 		routerService.sendMessageToMember(destination, message, member);
 		verify(connection).send(new EnqueueRequest("test123", message));
 	}
 }
