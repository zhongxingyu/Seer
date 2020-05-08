 package smartpod;
 
 import com.janezfeldin.Math.Vector2D;
 import de.reggi.jade.MessageHelper;
 import de.reggi.jade.MyReceiver;
 import jade.core.AID;
 import jade.core.Agent;
 import jade.core.behaviours.*;
 import jade.lang.acl.*;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 /**
  * Class for creating StationAgent. It extends NodeAgent.
  *
  * @author Janez Feldin
  */
 public class StationNodeAgent extends NodeAgent
 {
 
 	/***************************************************************************
 	 * Variables
 	 **************************************************************************/
 	
 	private int initialPodRequestsForPrediction = 5;
 	private RequestLearner learner = new RequestLearner(this);
 	
 	private int peopleCapacity;
 	private int peopleOnStation;
 	private List<AID> stationList = new ArrayList<AID>();
 	private int neededPods = 3;
 	/**
 	 * The currently pending transport requests at the node.
 	 */
 	public ArrayList<ACLMessage> pendingRequests = new ArrayList<ACLMessage>();
 
 	/***************************************************************************
 	 * Constructor
 	 **************************************************************************/
 	
 	/**
 	 * Constructor method for the StationNodeAgent.
 	 *
 	 * @param position Vector2D containing the position of the station
 	 * @param podsCapacity maximum allowed pods on the station at a given moment
 	 * @param peopleCapacity maximum allowed people waiting on the station at a
 	 * given moment
 	 */
 	public StationNodeAgent(Vector2D position, int podsCapacity, int peopleCapacity)
 	{
 		super(position, podsCapacity);
 	}
 
 	/***************************************************************************
 	 * Public methods
 	 **************************************************************************/
 
 	/**
 	 * Method fulfills the pending request and removes it from the queue.
 	 *
 	 * @param request The request needing fulfillment.
 	 * @param podAID The agent id of the pod fulfilling the request.
 	 */
 	public void fulfilTransportRequest(ACLMessage request, AID podAID)
 	{
 		System.out.printf("%-10s :: fulfilTransportRequest(%s,%s)\n", getLocalName(), podAID.getLocalName(), request.getUserDefinedParameter("destination"));
 
 		pendingRequests.remove(request);
 		departingPods.add(podAID);
 		registeredPods.remove(podAID);
 
 		AID destinationAID = new AID(request.getUserDefinedParameter("destination"), false);
 
 		addBehaviour(new ParhFindingRequest(this, destinationAID, podAID)
 		{
 			@Override
 			public void handleResult(AID roadAID, double cost, AID podAID, AID destinationAID)
 			{
 				communicator.requestPodToRoadDeparture(podAID, roadAID, destinationAID);
 				communicator.informNodeToNodePodArrival(podAID, destinationAID);
 			}
 		});
 	}
 
 	/***************************************************************************
 	 * JADE setup and behaviors
 	 **************************************************************************/
 	
 	/**
 	 * This method gets called when agent is started. It adds the desired
 	 * behaviour to the agent.
 	 */
 	@Override
 	protected void setup()
 	{
 		//setting initial values for prediction
 //		learner.setupWithInitialValue(initialPodRequestsForPrediction);
 
 
 		//adds the desired behaviour to the agent
 		addBehaviour(new StationAgentBehaviour(this));
 		addBehaviour(new CheckWeightPrediction(this, 3600));
 	}
 
 	/**
 	 * Behaviour class for StationAgent. It extends CyclicBehaviour.
 	 */
 	public class StationAgentBehaviour extends CyclicBehaviour
 	{
 
 		/**
 		 * Constructor for station agent behaviour class.
 		 *
 		 * @param a the agent to which behaviour is being applied.
 		 */
 		public StationAgentBehaviour(Agent a)
 		{
 			super(a);
 		}
 
 		/**
 		 * Method that performs actions in StationAgentBehaviour class. It gets
 		 * called each time Jade platform has spare resources.
 		 */
 		@Override
 		public void action()
 		{
 			// check departure message box
 			ArrayList<ACLMessage> departureMessages = communicator.checkPodDepartureMessages();
 			for (ACLMessage msg : departureMessages)
 			{
 				departingPods.remove(msg.getSender());
 			}
 			
 			// check departure message box
 			ArrayList<ACLMessage> finalNodeMessages = communicator.checkPodFromNodeArrivalMessages();
 			for (ACLMessage msg : finalNodeMessages)
 			{
 				AID podAID = new AID(msg.getUserDefinedParameter("pod"),false);
 				arrivingPods.add(podAID);
 			}
 
 			// check arrival message box
 			ACLMessage arrivalMessage = communicator.checkPodArrivalRequests();
 			if (arrivalMessage != null)
 //			ArrayList<ACLMessage> arrivalMessages = communicator.checkPodArrivalRequestMessages();
 //			for (ACLMessage arrivalMessage : arrivalMessages)
 			{
 				AID podAID = arrivalMessage.getSender();
 
 				if (!registeredPods.contains(podAID))
 				{
 					arrivingPods.remove(podAID);
 					registeredPods.add(podAID);
 
 					AID destinationAID = new AID(arrivalMessage.getUserDefinedParameter("destination"), false);
 
 					communicator.confirmPodToNodeArrival(arrivalMessage);
 
 					// check whether final destination has been reached
 					if (destinationAID.equals(getAID()))
 					{
 						// the pod has reached the final destination
 						System.out.println("\u001b[32mSUCCESS    :: " + podAID.getLocalName() + " has reached destination " + getLocalName() + "\u001b[0m");
 
 						if (pendingRequests.size() > 0)
 						{
 							fulfilTransportRequest(pendingRequests.get(0), podAID);
 						}
 					}
 					else
 					{
 						departingPods.add(podAID);
 						registeredPods.remove(podAID);
 
 						myAgent.addBehaviour(new ParhFindingRequest(myAgent, destinationAID, podAID)
 						{
 							@Override
 							public void handleResult(AID roadAID, double cost, AID podAID, AID destinationAID)
 							{
 								communicator.requestPodToRoadDeparture(podAID, roadAID, destinationAID);
 							}
 						});
 					}
 				}
 				else
 				{
 					System.err.println("com-node : pod already registered");
 				}
 			}
 
 			// check transport request message
 			ACLMessage transportRequest = communicator.checkPassengerTransportRequests();
 			if (transportRequest != null)
 			{
 				if (registeredPods.size() > 0)
 				{
 					AID podAID = registeredPods.get(0);
 					fulfilTransportRequest(transportRequest, podAID);
 				}
 				else
 				{
 					pendingRequests.add(transportRequest);
 
 					if (arrivingPods.size() < pendingRequests.size())
 					{
 						myAgent.addBehaviour(new PodBuyerBehaviour(myAgent));
 					}
 				}
 
 				//testing history logging and predicting...
 //				/*podRequested();
 //				 Date temp1 = getCurrentDate();
 //				 Date temp2 = new Date(getCurrentTime()+86400000);
 //				 System.out.println("Current time: "+temp1);
 //				 int[] temp = getNumRequestsInTimeFrame(temp1, temp2);
 //				
 //				
 //				 for(int i=0;i<temp.length;i++)
 //				 {
 //				 System.out.print(temp[i]+", ");
 //				 }
 //				 System.out.println("");
 //				 System.out.println(predictNumRequestsForTimeFrame(temp1,temp2));*/
 			}
 
 			// check pod requests
 			ACLMessage podQuery = myAgent.receive(MessageTemplate.MatchPerformative(ACLMessage.QUERY_REF));
 			if (podQuery != null)
 			{
 				myAgent.addBehaviour(new PodOfferBehaviour(myAgent, podQuery));
 			}
 
 			// fetch needed pods
 //			if (predictedPodCount() < neededPods)
 //			{
 //				myAgent.addBehaviour(new PodBuyerBehaviour(myAgent));
 //			}
 		}
 	}
 
 	public class PodBuyerBehaviour extends SequentialBehaviour
 	{
 
 		ACLMessage msg = new ACLMessage(ACLMessage.QUERY_REF);
		MessageTemplate template = 
				MessageTemplate.and(
					MessageTemplate.MatchPerformative(ACLMessage.INFORM),
					MessageTemplate.MatchOntology("buyer_seller"));
 		double bestPrice = 9999;
 		ACLMessage bestOffer = null;
 		List<ACLMessage> offers = new ArrayList<ACLMessage>(stationList.size() - 1);
 
 		public PodBuyerBehaviour(Agent a)
 		{
 			super(a);
			
			msg.setOntology("buyer_seller");
 
 			// Enquire pod prices from all the stations
 			ParallelBehaviour enquiry = new ParallelBehaviour(ParallelBehaviour.WHEN_ALL);
 
 			for (int i = 0; i < stationList.size(); i++)
 			{
 				if (!myAgent.getAID().equals(stationList.get(i)))
 				{
 					msg.addReceiver(stationList.get(i));
 
 					enquiry.addSubBehaviour(new MyReceiver(myAgent, -1, template)
 					{
 						@Override
 						public void handle(ACLMessage msg)
 						{
 							if (msg != null)
 							{
 								double offer = Double.parseDouble(msg.getUserDefinedParameter("price"));
 
 //								System.out.printf("%-10s :: Got offer from %s price %f\n", myAgent.getLocalName(), msg.getSender().getLocalName(), offer);
 
 								if (offer < bestPrice)
 								{
 									bestPrice = offer;
 									bestOffer = msg;
 								}
 								offers.add(msg);
 							}
 						}
 					});
 				}
 			}
 			addSubBehaviour(enquiry);
 
 			// Request the pod from best offerer.
 			addSubBehaviour(new OneShotBehaviour(myAgent)
 			{
 				@Override
 				public void action()
 				{
 					for (ACLMessage offer : offers)
 					{
 						ACLMessage reply = offer.createReply();
 						if (offer == bestOffer)
 						{
 							reply.setPerformative(ACLMessage.REQUEST);
 							reply.setContent("");
 						}
 						else
 						{
 							reply.setPerformative(ACLMessage.REFUSE);
 						}
 						myAgent.send(reply);
 					}
 				}
 			});
 			myAgent.send(msg);
 		}
 	}
 
 	public class PodOfferBehaviour extends SequentialBehaviour
 	{
 
 		ACLMessage msg, reply;
 		String convID = MessageHelper.genCID();
 		AID firstRoadAID = null;
 		double price = 0.0;
 
 		public PodOfferBehaviour(Agent a, ACLMessage msg)
 		{
 			super(a);
 			this.msg = msg;
 		}
 
 		@Override
 		public void onStart()
 		{
 			addSubBehaviour(new ParhFindingRequest(myAgent, msg.getSender(), null)
 			{
 				@Override
 				public void handleResult(AID roadAID, double cost, AID podAID, AID destinationAID)
 				{
 					firstRoadAID = roadAID;
 					price = cost + (registeredPods.size() > 0 ? neededPods / registeredPods.size() : 1000);
 				}
 			});
 
 			addSubBehaviour(new OneShotBehaviour(myAgent)
 			{
 				@Override
 				public void action()
 				{
 					reply = msg.createReply();
 					reply.setPerformative(ACLMessage.INFORM);
 					reply.setConversationId(convID);
 					reply.addUserDefinedParameter("price", String.valueOf(price));
 					myAgent.send(reply);
 				}
 			});
 
 			addSubBehaviour(new MyReceiver(myAgent, -1, MessageTemplate.MatchConversationId(convID))
 			{
 				@Override
 				public void handle(ACLMessage response)
 				{
 					if (response != null)
 					{
 						if (response.getPerformative() == ACLMessage.REQUEST)
 						{
 //							System.out.printf("\u001b[34m%-10s :: I am king\u001b[0m\n", myAgent.getLocalName());
 
 							if (registeredPods.size() > 0)
 							{
 								AID podAID = registeredPods.get(registeredPods.size() - 1);
 
 								departingPods.add(podAID);
 								registeredPods.remove(podAID);
 
 								AID destinationAID = msg.getSender();
 
 								communicator.requestPodToRoadDeparture(podAID, firstRoadAID, destinationAID);
 								communicator.informNodeToNodePodArrival(podAID, destinationAID);
 							}
 							else
 							{
 								System.err.println("ERROR :: no pods to send =(");
 							}
 						}
 						else if (response.getPerformative() == ACLMessage.REFUSE)
 						{
 //							System.out.printf("\u001b[34m%-10s :: I R looser\u001b[0m\n", myAgent.getLocalName());
 						}
 					}
 				}
 			});
 		}
 	}
 	
 	public class CheckWeightPrediction extends TickerBehaviour
 	{
 		public CheckWeightPrediction(Agent a, long period)
 		{
 			super(a, period);
 		}
 		
 		@Override
 		protected void onTick()
 		{
 			neededPods = (int)Math.ceil(learner.predictNumRequestsForTimeFrame(getCurrentDate(), new Date(getCurrentTime() + 3 * 3600)));
 			int delta = neededPods - registeredPods.size() + arrivingPods.size() + 1;
 			if (delta > 0)
 			{
 				while (delta > 0)
 				{
 					myAgent.addBehaviour(new PodBuyerBehaviour(myAgent));
 					delta--;
 				}
 			}
 		}
 		
 	}
 	
 	/***************************************************************************
 	 * Getters & setters
 	 **************************************************************************/
 	
 	//<editor-fold defaultstate="collapsed" desc="Getters & setters">
 	/**
 	 * Method that returns the maximum number of people, that can be on this
 	 * station at a given moment.
 	 *
 	 * @return Integer value representing the maximum number of people.
 	 */
 	public int getPeopleCapacity()
 	{
 		return peopleCapacity;
 	}
 
 	/**
 	 * Method used to set the maximum number of people, that can be on this
 	 * station at a given moment.
 	 *
 	 * @param peopleCapacity Integer value representing the maximum number of
 	 * people.
 	 */
 	public void setPeopleCapacity(int peopleCapacity)
 	{
 		this.peopleCapacity = peopleCapacity;
 	}
 
 	/**
 	 * Method that returns the current number of people on this station.
 	 *
 	 * @return Current number of people waiting on this station.
 	 */
 	public int getPeopleOnStation()
 	{
 		return peopleOnStation;
 	}
 
 	/**
 	 * Method used to set the current number of people waiting on this station.
 	 *
 	 * @param peopleOnStation Number of people, that are currently waiting on
 	 * the station.
 	 */
 	public void setPeopleOnStation(int peopleOnStation)
 	{
 		this.peopleOnStation = peopleOnStation;
 	}
 
 	/**
 	 * Method used to set the station list of all the stations.
 	 *
 	 * @param stationList The list containing all the stations.
 	 */
 	public void setStationList(List<AID> stationList)
 	{
 		this.stationList = stationList;
 	}
 	//</editor-fold>
 	
 }
