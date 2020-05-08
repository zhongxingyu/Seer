 package jadex;
 
 import jadex.base.fipa.SFipa;
 import jadex.bridge.Argument;
 import jadex.bridge.IArgument;
 import jadex.bridge.IComponentIdentifier;
 import jadex.bridge.IComponentStep;
 import jadex.bridge.IInternalAccess;
 import jadex.bridge.MessageType;
 import jadex.commons.SUtil;
 import jadex.commons.concurrent.DefaultResultListener;
 import jadex.micro.MicroAgent;
 import jadex.micro.MicroAgentMetaInfo;
 
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 
 import data.Board;
 import data.Position;
 
 /**
  *  Agent that pings another and waits for its replies.
  */
 @SuppressWarnings("rawtypes")
 public class ControlAgent extends MicroAgent
 {
 	//-------- attributes --------
 	
 	/** The receiver. */
 	protected IComponentIdentifier receiver;
 	
 	/** The difference between sent messages and received replies. */
 	protected int dif;
 	
 	/** Hashset with conversation ids of sent messages. */
 	protected Set sent;
 	
 	/** Blackboard */
 	protected Board board;
 	
 	//-------- methods --------
 	@Override
 	public void agentCreated()
 	{
 		board = new Board();
 	}
 	/**
 	 *  Execute the body.
 	 */
 	public void executeBody()
 	{
 		receiver = (IComponentIdentifier)getArgument("receiver");
 		final int missed_max = ((Number)getArgument("missed_max")).intValue();
 		final long timeout = ((Number)getArgument("timeout")).longValue();
 //		final Object content = getArgument("content");
 		final Position content = new Position(-1,-1,0);
 		sent = new HashSet();
 		
 		final IComponentStep step = new IComponentStep()
 		{
 			@SuppressWarnings("unchecked")
 			public Object execute(IInternalAccess ia)
 			{
 				if(dif>missed_max)
 				{
 					getLogger().warning("Worker target does not respond: " + receiver);
 					killAgent();
 				}
 				else
 				{
 					String convid = SUtil.createUniqueId(getAgentName());
 					Map msg = new HashMap();
 					
 					msg.put(SFipa.LANGUAGE, SFipa.NUGGETS_XML);
 					msg.put(SFipa.CONTENT, content);
 //					msg.put(SFipa.PERFORMATIVE, SFipa.QUERY_IF);
 					
 					msg.put(SFipa.PERFORMATIVE, SFipa.REQUEST);
 					msg.put(SFipa.CONVERSATION_ID, convid);
 					msg.put(SFipa.RECEIVERS, new IComponentIdentifier[]{receiver});
 //					msg.put(SFipa.SENDER, getComponentIdentifier());
 					dif++;
 					sent.add(convid);
 					sendMessage(msg, SFipa.FIPA_MESSAGE_TYPE);
 					waitFor(timeout, this);
 				}
 				return null;
 			}
 		};
 		
 		if(receiver==null)
 		{
			createComponentIdentifier("Work").addResultListener(
 				createResultListener(new DefaultResultListener()
 			{
 				public void resultAvailable(Object source, Object result)
 				{
 					receiver = (IComponentIdentifier)result;
 					scheduleStep(step);
 				}
 			}));
 		}
 		else
 		{
 			scheduleStep(step);
 		}
 
 	}
 	
 	/**
 	 *  Called when a message arrives.
 	 */
 	public void messageArrived(Map msg, MessageType mt)
 	{
 		System.err.println("Received: " + msg.get(SFipa.CONTENT).toString());
 
 		if(mt.equals(SFipa.FIPA_MESSAGE_TYPE))
 		{
 			String convid = (String)msg.get(SFipa.CONVERSATION_ID);
 			if(sent.remove(convid))
 			{
 //				Position pose = (Position) msg.get(SFipa.CONTENT);
 //				System.err.println("Received: " + pose.toString());
 				dif = 0; // A received ping heals other outstanding pings.
 				sent.clear();
 			}
 		}
 	}
 	@Override
 	public void agentKilled()
 	{
 		board.clear();
 	}
 	/**
 	 *  Get the agent meta info. 
 	 */
 	public static Object getMetaInfo()
 	{
 		return new MicroAgentMetaInfo("A simple agent that sends pings to another agent and waits for replies.", 
 			null, new IArgument[]
 			{
 				new Argument("receiver", "The component receiver of the ping target.", "IComponentIdentifier"),
 				new Argument("missed_max", "Maximum number of allowed missed replies", "int", new Integer(3)),
 				new Argument("timeout", "Timeout for reply", "long", new Long(1000)),
 				new Argument("content", "Ping message content", "String", "ping"),
 				
 			}, null);
 	}
 }
