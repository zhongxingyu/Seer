 package ru.ifmo.patterns;
 
 import org.junit.Assert;
 import org.junit.Test;
 
 import java.net.MalformedURLException;
 import java.rmi.Naming;
 import java.rmi.NotBoundException;
 import java.rmi.RemoteException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import static org.junit.Assert.assertEquals;
 
 public class MainTest {
 	public static final String RMI_HOST = "//localhost/";
 	public static final String ADD_QUEUE = RMI_HOST + "addQueue";
 	public static final String SUB_QUEUE = RMI_HOST + "subQueue";
 	public static final String MUL_QUEUE = RMI_HOST + "mulQueue";
 	public static final String DIV_QUEUE = RMI_HOST + "divQueue";
 	public static final String MAIN_QUEUE = RMI_HOST + "mainQueue";
 	public static final String CLIENT = RMI_HOST + "client";
 
 	@Test
 	public void test1() throws RemoteException, MalformedURLException, NotBoundException {
 		MessageQueueIml<AddOperation> addQueue = new MessageQueueIml<>();
 		MessageQueueIml<SubOperation> subQueue = new MessageQueueIml<>();
 		MessageQueueIml<MulOperation> mulQueue = new MessageQueueIml<>();
 		MessageQueueIml<DivOperation> divQueue = new MessageQueueIml<>();
 		addQueue.share(ADD_QUEUE);
 		subQueue.share(SUB_QUEUE);
 		mulQueue.share(MUL_QUEUE);
 		divQueue.share(DIV_QUEUE);
 
 		MessageQueueIml<BinaryOperation> mainQueue = new MessageQueueIml<>();
 		mainQueue.share(MAIN_QUEUE);
 
 		final RoutingWorkerFactory routingFactory = new RoutingWorkerFactory(
 				(MessageQueue<AddOperation>)Naming.lookup(ADD_QUEUE),
 				(MessageQueue<SubOperation>)Naming.lookup(SUB_QUEUE),
 				(MessageQueue<MulOperation>)Naming.lookup(MUL_QUEUE),
 				(MessageQueue<DivOperation>)Naming.lookup(DIV_QUEUE)
 		);
 
 		final RunnableWorkerFactory<AddOperation> addWorkerFactory = new RunnableWorkerFactory();
 		final RunnableWorkerFactory<SubOperation> subWorkerFactory = new RunnableWorkerFactory();
 		final RunnableWorkerFactory<MulOperation> mulWorkerFactory = new RunnableWorkerFactory();
 		final RunnableWorkerFactory<DivOperation> divWorkerFactory = new RunnableWorkerFactory();
 
 		List<WorkerPool> pools = new ArrayList<WorkerPool>() {{
 			add(createWorkerPool(MAIN_QUEUE, routingFactory, 10));
 			add(createWorkerPool(ADD_QUEUE, addWorkerFactory, 10));
 			add(createWorkerPool(SUB_QUEUE, subWorkerFactory, 10));
 			add(createWorkerPool(MUL_QUEUE, mulWorkerFactory, 10));
 			add(createWorkerPool(DIV_QUEUE, divWorkerFactory, 10));
 		}};
 
 		List<String> input = Arrays.asList("1 2 +", "5 1 + 2 / * 3 - 4");
 		List<Double> result = ClientImpl.runClient(input, CLIENT, MAIN_QUEUE);
 		List<Double> expectedResult = Arrays.asList(3., 5.);
 
 		for (int i = 0; i < result.size(); i++) {
 			assertEquals(expectedResult.get(i), result.get(i), 0.0001);
 		}
 	}
 
 	private static <T> WorkerPool createWorkerPool(String queuePath, WorkerFactory<T> factory, int threadCount)
 			throws RemoteException, NotBoundException, MalformedURLException {
 		return new WorkerPool((MessageQueue<T>)Naming.lookup(queuePath), factory, threadCount);
 	}
 }
