 package at.ac.tuwien.complang.sbc11.factory;
 
 import java.io.Serializable;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.logging.Logger;
 
 import javax.jms.Connection;
 import javax.jms.DeliveryMode;
 import javax.jms.Destination;
 import javax.jms.ExceptionListener;
 import javax.jms.JMSException;
 import javax.jms.Message;
 import javax.jms.MessageConsumer;
 import javax.jms.MessageListener;
 import javax.jms.MessageProducer;
 import javax.jms.ObjectMessage;
 import javax.jms.Session;
 
 import org.apache.activemq.ActiveMQConnection;
 import org.apache.activemq.ActiveMQConnectionFactory;
 import org.apache.activemq.broker.BrokerService;
 import org.mozartspaces.core.AsyncCapi;
 import org.mozartspaces.core.Capi;
 import org.mozartspaces.core.DefaultMzsCore;
 import org.mozartspaces.core.MzsCoreException;
 
 import at.ac.tuwien.complang.sbc11.factory.exception.SharedWorkspaceException;
 import at.ac.tuwien.complang.sbc11.parts.CPU;
 import at.ac.tuwien.complang.sbc11.parts.CPU.CPUType;
 import at.ac.tuwien.complang.sbc11.parts.Computer;
 import at.ac.tuwien.complang.sbc11.parts.Order;
 import at.ac.tuwien.complang.sbc11.parts.Part;
 import at.ac.tuwien.complang.sbc11.ui.Factory;
 import at.ac.tuwien.complang.sbc11.workers.AsyncAssembler;
 import at.ac.tuwien.complang.sbc11.workers.Tester.TestType;
 
 /* Implements the shared workspace with an alternative technology.
  * The technology used is JMS. OpenJMS is the implementation that will be used here.
  * There are other implementations that are not completely conform to a standard.
  * OpenJMS provides a clean implementation of handling Objects.
  */
 
 
 public class SharedWorkspaceJMSImpl extends SharedWorkspace 
 {
 	// General
 	private Logger logger;
 	private Factory factory;
 	
 	//ActiveMQ
 	private ActiveMQConnectionFactory connectionFactory;
 	private Connection connection;
 	private Session session;
 	private Destination destination;
 	
 	public SharedWorkspaceJMSImpl() throws SharedWorkspaceException 
 	{
 		super(null);
 		
 		this.factory = null;
 		logger = Logger.getLogger("at.ac.tuwien.complang.sbc11.factory.SharedWorkspaceJMSImpl");
 		
 		// ActiveMQ init		
 		BrokerService broker = new BrokerService();
 		broker.setUseJmx(true);
 		try 
 		{
 			broker.addConnector("tcp://localhost:61616");
 			broker.start();
 			
 			connectionFactory = new ActiveMQConnectionFactory(
 					                  ActiveMQConnection.DEFAULT_USER,
 					                  ActiveMQConnection.DEFAULT_PASSWORD,
 					                  ActiveMQConnection.DEFAULT_BROKER_URL);
 			
 			connection = (Connection) connectionFactory.createConnection();
 	
 			connection.start();
 			
 			
 			//destination = session.createQueue("mmy first active mq queue");
 			
 			
 		} catch (JMSException e) 
 		{
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (Exception e) 
 		{
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		logger.info("Initialization done.");
 	}
 	public SharedWorkspaceJMSImpl(int serverPort) throws SharedWorkspaceException {
 		super(null);
 		
 		this.factory = null;
 		logger = Logger.getLogger("at.ac.tuwien.complang.sbc11.factory.SharedWorkspaceJMSImpl");
 		
 		// ActiveMQ init		
 		BrokerService broker = new BrokerService();
 		broker.setUseJmx(true);
 		
 		try 
 		{
 			broker.addConnector("tcp://localhost:" + String.valueOf(serverPort));
 			broker.start();
 			
 			connectionFactory = new ActiveMQConnectionFactory(
 					                  ActiveMQConnection.DEFAULT_USER,
 					                  ActiveMQConnection.DEFAULT_PASSWORD,
 					                  ActiveMQConnection.DEFAULT_BROKER_URL);
 			
 			connection = (Connection) connectionFactory.createConnection();
 	
 			connection.start();
 			
 			//destination = session.createQueue("mmy first active mq queue");
 			
 			
 		} catch (JMSException e) 
 		{
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (Exception e) 
 		{
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		logger.info("Initialization done.");
 	}
 
 	public SharedWorkspaceJMSImpl(Factory factory) throws SharedWorkspaceException 
 	{
 		super(factory);
 		
 		logger = Logger.getLogger("at.ac.tuwien.complang.sbc11.factory.SharedWorkspaceJMSImpl");
 		this.factory = factory;	
 		
 		// ActiveMQ init		
 		BrokerService broker = new BrokerService();
 		broker.setUseJmx(true);
 		try 
 		{
 			broker.addConnector("tcp://localhost:61616");
 			broker.start();
 			
 			connectionFactory = new ActiveMQConnectionFactory(
 					                ActiveMQConnection.DEFAULT_USER,
 					                ActiveMQConnection.DEFAULT_PASSWORD,
 					                ActiveMQConnection.DEFAULT_BROKER_URL);
 			
 			connection = (Connection) connectionFactory.createConnection();
 	
 			connection.start();
 			session = connection.createSession(true, Session.AUTO_ACKNOWLEDGE);
 			
 			initDestinations();
 			
 			//destination = session.createQueue("mmy first active mq queue");
 			
 			
 		} catch (JMSException e) 
 		{
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (Exception e) 
 		{
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		logger.info("Initialization done.");
 	}
 	
 	private void initDestinations()
 	{
 		try 
 		{
 			/* part properties: 
 			 *  - type: cpu, mainboard,graphicboard, ram
 			 *  - cputype: CPUType
 			 * 
 			 */
 			session.createQueue("parts");
 			/* computer properties:
 			 *  - CompletenessTested: bool
 			 *  - CorrectnessTested: bool
 			 *  - Deconstructed: bool
 			 * 
 			 */
 			session.createQueue("computers");
 			session.createQueue("trashed");
 			session.createQueue("shipped");
 			/* oder properties:
 			 *  - completelytested: bool
 			 *  - finished : bool
 			 * 
 			 */
 			session.createQueue("orders");
			session.createQueue("finished");
 			session.createQueue("balanceContainer");
 		} catch (JMSException e) 
 		{
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 	
 	private long getNextId()
 	{
 		Date theDate = new Date();
 		return theDate.getTime();
 	}
 	
 	@Override
 	public long getNextComputerId() 
 	{
 		return this.getNextId();
 	}
 
 	@Override
 	public long getNextPartId() throws SharedWorkspaceException 
 	{
 		return this.getNextId();
 	}
 	
 	@Override
 	public void secureShutdown() throws SharedWorkspaceException 
 	{
 		logger.info("Shutting down...");
 		try 
 		{
 			session.rollback();
 			session.close();
 			connection.close();
 		} catch (JMSException e) 
 		{
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 	}
 	
 	@Override
 	public void startTransaction() throws SharedWorkspaceException 
 	{
 		try 
 		{
 			session = connection.createSession(true, Session.AUTO_ACKNOWLEDGE);
 		} catch (JMSException e) 
 		{
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 	}
 
 	@Override
 	public void commitTransaction() throws SharedWorkspaceException 
 	{
 		try 
 		{
 			session.commit();
 		} catch (JMSException e) 
 		{
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	@Override
 	public void rollbackTransaction() throws SharedWorkspaceException 
 	{
 		try 
 		{
 			session.rollback();
 		} catch (JMSException e) 
 		{
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 	
 	private void sendMessage(ObjectMessage objMessage)
 	{
 		MessageProducer producer;
 		
 		try 
 		{
 			producer = session.createProducer(destination);
 
 			producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
 		
 			logger.info("Sending message.");
 			producer.send(objMessage);
 		} catch (JMSException e) 
 		{
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	private ObjectMessage receiveMessage(String filter)
 	{
 		ObjectMessage message = null;
 		try 
 		{
 			MessageConsumer consumer = session.createConsumer(destination, filter);
 			message = (ObjectMessage) consumer.receive();
 			message.clearBody();
 		} catch (JMSException e) 
 		{
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return message;
 	}
 
 	@Override
 	public void addPart(Part part) throws SharedWorkspaceException 
 	{
 		// TODO Auto-generated method stub
 		ObjectMessage message;
 		try 
 		{
 			message = session.createObjectMessage(part);
 			message.setJMSType(part.getClass().toString()); //set standard type
 			
 			if(part.getClass().equals(CPU.class)) // set custom attribute
 			{
 				message.setStringProperty("CPUType", ((CPU)part).getCpuType().toString());
 			}
 			
 			destination = session.createQueue("parts");
 			
 			sendMessage(message);
 			
 		} catch (JMSException e) 
 		{
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 	
 	@Override
 	public List<Part> takeParts(Class<?> partType, boolean blocking, int partCount) throws SharedWorkspaceException 
 	{
 		ArrayList<Part> result = new ArrayList<Part>();
 		String filter;
 		Part p;
 
 		filter = "JMSType='"+partType.getClass()+"'";
 		
 //		if(partType.getClass().equals(CPU.class))
 //		{
 //			filter += " AND CPUType='"+((CPU)part).getCpuType().toString()+"'"; 
 //		}
 		//"JMSType = 'car' AND color = 'blue' AND weight > 2500"
 		
 		for(int i = 0; i < partCount; i++)
 		{
 			p = (Part) receiveMessage(filter);
 			if(p!=null)
 				result.add(p);
 		}
 		
 		return result;
 	}
 
 	@Override
 	public List<CPU> takeCPU(CPUType cpuType, boolean blocking, int partCount) throws SharedWorkspaceException 
 	{
 		ArrayList<CPU> result = new ArrayList<CPU>();
 		String filter;
		CPU p;
 
 		filter = "JMSType='"+CPU.class.toString()+"'";
 		
 		filter += " AND CPUType='"+cpuType.toString()+"'"; 
 		
 		//"JMSType = 'car' AND color = 'blue' AND weight > 2500"
		
 		for(int i = 0; i < partCount; i++)
 		{
 			p = (CPU) receiveMessage(filter);
 			if(p!=null)
 				result.add(p);
 		}
 		
 		return result;
 	}
 	
 
 	@Override
 	public void addParts(List<Part> parts) throws SharedWorkspaceException 
 	{
 		for(Part part:parts)
 		{
 			addPart(part);
 		}
 	}
 	
 	/**************** NOT DONE YET ****************/
 	
 	@Override
 	public String getWorkspaceID() 
 	{
 		// ????
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public List<Part> getAvailableParts() throws SharedWorkspaceException 
 	{
 		ArrayList<Part> result = new ArrayList<Part>();
 		
 		return result;
 	}
 
 	@Override
 	public List<Computer> getIncompleteComputers() throws SharedWorkspaceException 
 	{
 		ArrayList<Computer> result = new ArrayList<Computer>();
 		
 		return result;
 	}
 
 	@Override
 	public List<Computer> getShippedComputers() throws SharedWorkspaceException 
 	{
 		ArrayList<Computer> result = new ArrayList<Computer>();
 		
 		return result;
 	}
 
 	@Override
 	public List<Computer> getTrashedComputers() throws SharedWorkspaceException 
 	{
 		ArrayList<Computer> result = new ArrayList<Computer>();
 		
 		return result;
 	}
 
 	@Override
 	public List<Computer> getDeconstructedComputers() throws SharedWorkspaceException 
 	{
 		ArrayList<Computer> result = new ArrayList<Computer>();
 		
 		return result;
 	}
 
 	@Override
 	public List<Order> getUnfinishedOrders() throws SharedWorkspaceException 
 	{
 		ArrayList<Order> result = new ArrayList<Order>();
 		
 		return result;
 	}
 
 	@Override
 	public List<Order> getFinishedOrders() throws SharedWorkspaceException 
 	{
 		ArrayList<Order> result = new ArrayList<Order>();
 		
 		return result;
 	}
 
 	@Override
 	public void addComputer(Computer computer) throws SharedWorkspaceException 
 	{
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public Computer takeUntestedComputer(TestType untestedFor) throws SharedWorkspaceException 
 	{
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public Computer takeNormalCompletelyTestedComputer() throws SharedWorkspaceException 
 	{
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public boolean testOrderCountMet(Order order) throws SharedWorkspaceException 
 	{
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	@Override
 	public List<Computer> takeAllOrderedComputers(Order order) throws SharedWorkspaceException 
 	{
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public void shipComputer(Computer computer) throws SharedWorkspaceException 
 	{
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void addComputerToTrash(Computer computer) throws SharedWorkspaceException 
 	{
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void addOrder(Order order) throws SharedWorkspaceException 
 	{
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void finishOrder(Order order) throws SharedWorkspaceException 
 	{
 		// TODO Auto-generated method stub
 		
 	}
 	
 	/**************** NOT CLEAR HOW ****************/
 
 	@Override
 	public void startBalancing() throws SharedWorkspaceException 
 	{
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void stopBalancing() throws SharedWorkspaceException 
 	{
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void waitForBalancing() throws SharedWorkspaceException 
 	{
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public boolean isCurrentlyBalancing() throws SharedWorkspaceException 
 	{
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	@Override
 	public void takePartsAsync(Class<?> partType, boolean blocking, int partCount, AsyncAssembler callback) throws SharedWorkspaceException 
 	{
 		// TODO Auto-generated method stub
 		
 	}
 
 }
