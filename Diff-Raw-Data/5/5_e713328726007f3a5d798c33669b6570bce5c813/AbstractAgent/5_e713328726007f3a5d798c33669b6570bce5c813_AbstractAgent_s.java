 package agents;
 
 import se.sics.tasim.aw.Agent;
 import se.sics.tasim.aw.Message;
 import se.sics.tasim.props.StartInfo;
 import se.sics.tasim.props.SimulationStatus;
 import se.sics.isl.transport.Transportable;
 import edu.umich.eecs.tac.props.*;
 
 import java.util.*;
 
 /**
  * This class is a skeletal implementation of a TAC/AA agent.  Based on the Example Agent
  *
  * @author Patrick Jordan
  * @see <a href="http://aa.tradingagents.org/documentation">TAC AA Documentation</a>
  */
 public abstract class AbstractAgent extends Agent {
 	/**
 	 * Basic simulation information. {@link StartInfo} contains
 	 * <ul>
 	 * <li>simulation ID</li>
 	 * <li>simulation start time</li>
 	 * <li>simulation length in simulation days</li>
 	 * <li>actual seconds per simulation day</li>
 	 * </ul>
 	 * An agent should receive the {@link StartInfo} at the beginning of the game or during recovery.
 	 */
 	protected StartInfo _startInfo; 
 
 	/**
 	 * Basic auction slot information. {@link SlotInfo} contains
 	 * <ul>
 	 * <li>the number of regular slots</li>
 	 * <li>the number of promoted slots</li>
 	 * <li>promoted slot bonus</li>
 	 * </ul>
 	 * An agent should receive the {@link SlotInfo} at the beginning of the game or during recovery.
 	 * This information is identical for all auctions over all query classes.
 	 */
 	protected SlotInfo _slotInfo;
 
 	/**
 	 * The retail catalog. {@link RetailCatalog} contains
 	 * <ul>
 	 * <li>the product set</li>
 	 * <li>the sales profit per product</li>
 	 * <li>the manufacturer set</li>
 	 * <li>the component set</li>
 	 * </ul>
 	 * An agent should receive the {@link RetailCatalog} at the beginning of the game or during recovery.
 	 */
 	protected RetailCatalog _retailCatalog;
 
 	/**
 	 * The basic advertiser specific information. {@link AdvertiserInfo} contains
 	 * <ul>
 	 * <li>the manufacturer specialty</li>
 	 * <li>the component specialty</li>
 	 * <li>the manufacturer bonus</li>
 	 * <li>the component bonus</li>
 	 * <li>the distribution capacity discounter</li>
 	 * <li>the address of the publisher agent</li>
 	 * <li>the distribution capacity</li>
 	 * <li>the address of the advertiser agent</li>
 	 * <li>the distribution window</li>
 	 * <li>the target effect</li>
 	 * <li>the focus effects</li>
 	 * </ul>
 	 * An agent should receive the {@link AdvertiserInfo} at the beginning of the game or during recovery.
 	 */
 	protected AdvertiserInfo _advertiserInfo;
 
 	/**
 	 * The basic publisher information. {@link PublisherInfo} contains
 	 * <ul>
 	 * <li>the squashing parameter</li>
 	 * </ul>
 	 * An agent should receive the {@link PublisherInfo} at the beginning of the game or during recovery.
 	 */
 	protected PublisherInfo _publisherInfo;
 
 	/**
 	 * The list contains all of the {@link SalesReport sales report} delivered to the agent.  Each
 	 * {@link SalesReport sales report} contains the conversions and sales revenue accrued by the agent for each query
 	 * class during the period.
 	 */
 	protected Queue<SalesReport> _salesReports;
 
 	/**
 	 * The list contains all of the {@link QueryReport query reports} delivered to the agent.  Each
 	 * {@link QueryReport query report} contains the impressions, clicks, cost, average position, and ad displayed
 	 * by the agent for each query class during the period as well as the positions and displayed ads of all advertisers
 	 * during the period for each query class.
 	 */
 	protected Queue<QueryReport> _queryReports;
 
 	/**
 	 * List of all the possible queries made available in the {@link RetailCatalog retail catalog}.
 	 */
 	protected Set<Query> _querySpace;
 	
 	protected Hashtable<QueryType, Set<Query>> _queryFocus;
 	protected Hashtable<String, Set<Query>> _queryManufacturer;
 	protected Hashtable<String, Set<Query>> _queryComponent;
 	protected Hashtable<String, Hashtable<String, Set<Query>>> _querySingleton; //*sigh* only if java had tuples
 	protected boolean _firstDay;
 	
 	public AbstractAgent() {
 		_salesReports = new LinkedList<SalesReport>();
 		_queryReports = new LinkedList<QueryReport>();
 		_querySpace = new LinkedHashSet<Query>();
 		
 		_queryFocus = new Hashtable<QueryType, Set<Query>>();
 		_queryManufacturer = new Hashtable<String, Set<Query>>();
 		_queryComponent = new Hashtable<String, Set<Query>>();
 		_querySingleton = new Hashtable<String, Hashtable<String, Set<Query>>>();
 		
 		_firstDay = true;
 	}
 
 	/**
 	 * Processes the messages received the by agent from the server.
 	 *
 	 * @param message the message
 	 */
 	protected void messageReceived(Message message) {
 		Transportable content = message.getContent();
 
 		if (content instanceof QueryReport) {
 			handleQueryReport((QueryReport) content);
 		} else if (content instanceof SalesReport) {
 			handleSalesReport((SalesReport) content);
 		} else if (content instanceof SimulationStatus) {
 			handleSimulationStatus((SimulationStatus) content);
 		} else if (content instanceof PublisherInfo) {
 			handlePublisherInfo((PublisherInfo) content);
 		} else if (content instanceof SlotInfo) {
 			handleSlotInfo((SlotInfo) content);
 		} else if (content instanceof RetailCatalog) {
 			handleRetailCatalog((RetailCatalog) content);
 		} else if (content instanceof AdvertiserInfo) {
 			handleAdvertiserInfo((AdvertiserInfo) content);
 		} else if (content instanceof StartInfo) {
 			handleStartInfo((StartInfo) content);
 		}
 	}
 
 	/**
 	 * Sends a constructed {@link BidBundle} from any updated bids, ads, or spend limits.
 	 */
 	protected void sendBidAndAds() {
 		updateBidStratagy();
		BidBundle bidBundle = buildBidBudle();
 
 		String publisherAddress = _advertiserInfo.getPublisherId();
 
 		// Send the bid bundle to the publisher
 		if (publisherAddress != null) {
 			sendMessage(publisherAddress, bidBundle);
 		}
 	}
 	
 	protected abstract void updateBidStratagy();
	protected abstract BidBundle buildBidBudle();
 	
 	/**
 	 * Processes an incoming query report.
 	 *
 	 * @param queryReport the daily query report.
 	 */
 	protected void handleQueryReport(QueryReport queryReport) {
 		_queryReports.add(queryReport);
 	}
 
 	/**
 	 * Processes an incoming sales report.
 	 *
 	 * @param salesReport the daily sales report.
 	 */
 	protected void handleSalesReport(SalesReport salesReport) {
 		_salesReports.add(salesReport);
 	}
 
 	/**
 	 * Processes a simulation status notification.  Each simulation day the {@link SimulationStatus simulation status }
 	 * notification is sent after the other daily messages ({@link QueryReport} {@link SalesReport} have been sent.
 	 *
 	 * @param simulationStatus the daily simulation status.
 	 */
 	protected void handleSimulationStatus(SimulationStatus simulationStatus) {
 		if(_firstDay){
 			_firstDay = false;
 			initBidder();
 		}
 		sendBidAndAds();
 	}
 	
 	/**
 	 * This method will be called at the start of the first day only;
 	 * After all other reports have been received, but before sendBidAndAds is called.
 	 * It can be used like a constructor for the agent's state
 	 */
 	protected abstract void initBidder();
 
 	/**
 	 * Processes the publisher information.
 	 * @param publisherInfo the publisher information.
 	 */
 	protected void handlePublisherInfo(PublisherInfo publisherInfo) {
 		_publisherInfo = publisherInfo;
 	}
 
 	/**
 	 * Processrs the slot information.
 	 * @param slotInfo the slot information.
 	 */
 	protected void handleSlotInfo(SlotInfo slotInfo) {
 		_slotInfo = slotInfo;
 	}
 
 	/**
 	 * Processes the retail catalog.
 	 * @param retailCatalog the retail catalog.
 	 */
 	protected void handleRetailCatalog(RetailCatalog retailCatalog) {
 		_retailCatalog = retailCatalog;
 		Query tempquery;
 		
 		Set<Query> F0 = new LinkedHashSet<Query>();
 		Set<Query> F1 = new LinkedHashSet<Query>();
 		Set<Query> F2 = new LinkedHashSet<Query>();
 		_queryFocus.put(QueryType.FOCUS_LEVEL_ZERO, F0);
 		_queryFocus.put(QueryType.FOCUS_LEVEL_ONE, F1);
 		_queryFocus.put(QueryType.FOCUS_LEVEL_TWO, F2);
 		
 		// The query space is all the F0, F1, and F2 queries for each product
 		// The F0 query class
 		if(retailCatalog.size() > 0) {
 			tempquery = new Query(null, null);
 			_querySpace.add(tempquery);
 			F0.add(tempquery);
 		}
 
 		Set<Query> manufacturerSet;
 		Set<Query> componentSet;
 		
 		for(Product product : retailCatalog) {
 			if(_queryManufacturer.containsKey(product.getManufacturer())){
 				manufacturerSet = _queryManufacturer.get(product.getManufacturer());
 			}
 			else {
 				manufacturerSet = new LinkedHashSet<Query>();
 				_queryManufacturer.put(product.getManufacturer(), manufacturerSet);
 			}
 			
 			if(_queryComponent.containsKey(product.getComponent())){
 				componentSet = _queryComponent.get(product.getComponent());
 			}
 			else {
 				componentSet = new LinkedHashSet<Query>();
 				_queryComponent.put(product.getComponent(), componentSet);
 			}
 			// The F1 query classes
 			// F1 Manufacturer only
 			tempquery = new Query(product.getManufacturer(), null);
 			_querySpace.add(tempquery);
 			manufacturerSet.add(tempquery);
 			F1.add(tempquery);
 			
 			// F1 Component only
 			tempquery = new Query(null, product.getComponent());
 			_querySpace.add(tempquery);
 			componentSet.add(tempquery);
 			F1.add(tempquery);
 			
 			// The F2 query class
 			tempquery = new Query(product.getManufacturer(), product.getComponent());
 			_querySpace.add(tempquery);
 			F2.add(tempquery);
 			componentSet.add(tempquery);
 			manufacturerSet.add(tempquery);
 			
 			Set<Query> singleton = new LinkedHashSet<Query>();
 			singleton.add(tempquery);
 			
 			Hashtable<String, Set<Query>> tempTable= new Hashtable<String, Set<Query>>();
 			tempTable.put(product.getComponent(), singleton);
 			_querySingleton.put(product.getManufacturer(), tempTable);
 		}
 	}
 
 	/**
 	 * Processes the advertiser information.
 	 * @param advertiserInfo the advertiser information.
 	 */
 	protected void handleAdvertiserInfo(AdvertiserInfo advertiserInfo) {
 		_advertiserInfo = advertiserInfo;
 	}
 
 	/**
 	 * Processes the start information.
 	 * @param startInfo the start information.
 	 */
 	protected void handleStartInfo(StartInfo startInfo) {
 		_startInfo = startInfo;
 	}
 
 	/**
 	 * Prepares the agent for a new simulation.
 	 */
 	protected void simulationSetup() {
 	}
 
 	/**
 	 * Runs any post-processes required for the agent after a simulation ends.
 	 */
 	protected void simulationFinished() {
 		_salesReports.clear();
 		_queryReports.clear();
 		_querySpace.clear();
 		_firstDay = true;
 	}
 	
 	
 	/**
 	 * Helper methods for generating more interesting sets of queries
 	 * 
 	 * @param s1
 	 * @param s2
 	 * @return
 	 */
 	protected Set<Query> intersect(Set<Query> s1, Set<Query> s2){
 		Set<Query> inter = new LinkedHashSet<Query>();
 		inter.addAll(s1);
 		inter.retainAll(s2);
 		return inter;
 	}
 	
 	protected void printAdvertiserInfo(){
 		System.out.println("Agent Info");
 		System.out.println("Distribution Capacity: "+_advertiserInfo.getDistributionCapacity());
 		System.out.println("Distribution Window: "+_advertiserInfo.getDistributionWindow());
 		
 		System.out.println("Component Specialty: "+_advertiserInfo.getComponentSpecialty());
 		System.out.println("Component Bonus: "+_advertiserInfo.getComponentBonus());
 		
 		System.out.println("Manufacturer Specialty: "+_advertiserInfo.getManufacturerSpecialty());
 		System.out.println("Manufacturer Bonus: "+_advertiserInfo.getManufacturerBonus());
 	}
 	
 	protected void printQueryReport(QueryReport report){
 		for(Query q : report){
 			System.out.println(q);
 			for(String a : report.advertisers(q)){
 				System.out.println("\t"+a+" "+report.getPosition(q,a));
 			}		
 		}
 	}
 }
