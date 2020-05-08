 import se.sics.tasim.aw.Agent;
 import se.sics.tasim.aw.Message;
 import se.sics.tasim.props.StartInfo;
 import se.sics.tasim.props.SimulationStatus;
 import se.sics.isl.transport.Transportable;
 import edu.umich.eecs.tac.props.*;
 
 import java.util.*;
 import java.io.*;
 import java.net.*;
 
 
 public class AgentOrange extends Agent{
     
     private enum R {MISS, MISSNEUTRAL, MISSHIT, NEUTRAL, HITNEUTRAL, HIT}
     private EnumMap Result = new EnumMap<R,Integer>(R.class);
 
     private double[] clicks;
     private double[] values;
     //private double moneyMade = 0;
     //private double moneySpent = 0;
     
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
     private StartInfo startInfo;
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
     protected SlotInfo slotInfo;
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
     protected RetailCatalog retailCatalog;
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
     protected AdvertiserInfo advertiserInfo;
 
     /**
      * The basic publisher information. {@link PublisherInfo} contains
      * <ul>
      * <li>the squashing parameter</li>
      * </ul>
      * An agent should receive the {@link PublisherInfo} at the beginning of the game or during recovery.
      */
     protected PublisherInfo publisherInfo;
 
     /**
      * The list contains all of the {@link SalesReport sales report} delivered to the agent.  Each
      * {@link SalesReport sales report} contains the conversions and sales revenue accrued by the agent for each query
      * class during the period.
      */
     protected Queue<SalesReport> salesReports;
 
     /**
      * The list contains all of the {@link QueryReport query reports} delivered to the agent.  Each
      * {@link QueryReport query report} contains the impressions, clicks, cost, average position, and ad displayed
      * by the agent for each query class during the period as well as the positions and displayed ads of all advertisers
      * during the period for each query class.
      */
     protected Queue<QueryReport> queryReports;
 
     /**
      * List of all the possible queries made available in the {@link RetailCatalog retail catalog}.
      */
     protected Set<Query> querySpace;
 	
 	/**
 	 * List of all bid bundles	 
 	 */
 	protected Queue<BidBundle> bidBundles;
 	
 	private double bankBalance;
 	
 	private double cap; // if we have a bank balance below this value, we can only spend this value per day till we get out
     
     protected Socket comms;
     
    public AgentOrange() {
         System.out.println("Agent Orange Reset");
         salesReports = new LinkedList<SalesReport>();
         queryReports = new LinkedList<QueryReport>();
 		bidBundles = new LinkedList<BidBundle>();
         querySpace = new LinkedHashSet<Query>();
         generateEnumMap();
         clicks = new double[Result.size()];
         values = new double[Result.size()];
         bankBalance = 0;
         cap = 100;
     }
 
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
 	        } else if (content instanceof BankStatus) {
             bankBalance = ((BankStatus)(content)).getAccountBalance();
           }
 	        
 	        System.out.println("(AgentOrange): Message Received!");
 	        
 	    }
       
       private double change(double x, double deltaX)
         {
           x = x + deltaX;
           if (x < 0.05)
             {
               x = 0.05;
             }
           else if (x > 0.95) 
             {
               x = 0.95;
             }
           return x;  
         }
 
       private double getCost(Query query)
         {
           return (((QueryReport)(((LinkedList)queryReports).getLast())).getCost(query));
         }
       
       
       
       private int getDay(Query query) 
       	{
       	  return ((LinkedList)queryReports).size();
       	}
 
       private double getClicks(Query query)
         {
           return ( ((QueryReport) ( (LinkedList)queryReports ).getLast()).getClicks(query) );
         }
         
       private double getValue(Query query)
         {
 		  return ( ((SalesReport) ( (LinkedList)salesReports ).getLast()).getRevenue(query) );
         }
       
 	    /**
 	     * Sends a constructed {@link BidBundle} from any updated bids, ads, or spend limits.
 	     */
 
 
 	    protected void sendBidAndAds() {
 	        
 		//***Croc Initial Bidding Algorithm***
 		//Initial bidding algorithm for croc uses fixed cpc due to delay in report
 
 	   		BidBundle bidBundle = new BidBundle();
 			String publisherAddress = advertiserInfo.getPublisherId();
 				
 
         System.out.println("AgentOrangeNEW: bankBalance = "+bankBalance);
 				// Spend whatever money we have plus an initial float
 				double spendLimit = cap + bankBalance;
 				if (spendLimit < cap) {
 					spendLimit = cap;
           System.out.println("AgentOrange: Capping spendlimit as in debt");
 				}
 				System.out.println("AgentOrangeNEW: Spendlimit = "+spendLimit);
 
 				
 				for(Query query : querySpace) {
 					// The publisher will interpret a NaN bid as
 					// a request to persist the prior day's bid
 					double bid = 1;
 					// bid = [ calculated optimal bid ]
 					double bidMultiplier = 1;
 					
 					// Calculate Matching Category
 					if (isManufacturerSpeciality(query) && isComponentSpeciality(query))
 					{
 						// HIT (zeta)
 						addClicksAndVals(R.HIT, query);
 						bid = generateBid(R.HIT, query);
 					}            
 					else if ( (isManufacturerSpeciality(query) && !isComponentSpeciality(query)) )
 					{
 						if (query.getComponent() != null) {
 							// MISS-HIT (gamma)
 							addClicksAndVals(R.MISSHIT, query);
 							bid = generateBid(R.MISSHIT, query);
 						} else {
 							// NEUTRAL-HIT (epsilon)
 							addClicksAndVals(R.HITNEUTRAL, query);
 							bid = generateBid(R.HITNEUTRAL, query);
 						}
 					}
 					else if ( (!isManufacturerSpeciality(query) && isComponentSpeciality(query)) )
 					{
 						if (query.getManufacturer() != null) {
 							// MISS-HIT (gamma)
 							addClicksAndVals(R.MISSHIT, query);
 							bid = generateBid(R.HITNEUTRAL, query);
 						} else {
 							// NEUTRAL-HIT (epsilon)
 							addClicksAndVals(R.HITNEUTRAL, query);
 							bid = generateBid(R.HITNEUTRAL, query);
 						}
 					} 
 					else if ((query.getManufacturer() == null) && (query.getComponent() == null))
 					{	
 						// NEUTRAL (delta)
 						addClicksAndVals(R.NEUTRAL, query);
 						bid = generateBid(R.NEUTRAL, query);
 					} 
 					else if (query.getManufacturer() == null || query.getComponent() == null)
 					{
 						// MISS-NEUTRAL (beta)
 						// One is null, not the other (therefore one neutral)
 						// and the speciality case has already been triggered (therefore one miss)
 						addClicksAndVals(R.MISSNEUTRAL, query);
 						bid = generateBid(R.MISSNEUTRAL, query);
 					}
 					else {
 						// MISS (alpha)
 						addClicksAndVals(R.MISS, query);
 						bid = generateBid(R.MISS, query);
 					}
 					
 					// Target the Advert Appropriately
 					Ad ad = new Ad();
 					switch (query.getType()) {
 						case FOCUS_LEVEL_ONE:
 							if (query.getManufacturer() == null)
 							{
 							  Product p = new Product(advertiserInfo.getManufacturerSpecialty(),query.getComponent());
 							  ad = new Ad(p);
 							}
 							else
 							{
 							  Product p = new Product(query.getManufacturer(),advertiserInfo.getComponentSpecialty());
 							  ad = new Ad(p);
 							}
 						case FOCUS_LEVEL_TWO:
 							Product p = new Product(query.getManufacturer(),query.getComponent());
 							ad = new Ad(p);
 					}
 					
 					// Set the daily updates to the ad campaigns for this query class
 					bidBundle.addQuery(query,  bid, ad);
 					
 				}
     
 
 	        // Set the daily updates to the campaign spend limit
 	        bidBundle.setCampaignDailySpendLimit(spendLimit);
 
 	        // Send the bid bundle to the publisher
 	        if (publisherAddress != null) {
 	            sendMessage(publisherAddress, bidBundle);
 				
 				//save tthis bid bundle to the list 
 				bidBundles.add(bidBundle);
 	        }
 	    }
 		
 		private double generateBid(R r, Query q) {
 			//start with a ridiculous bid
 			double bid = 1.0;
 			double reserve = 0.0; //TODO	
 
 			double focusParameter = 0.8;
 			double specialityParameter = 1.3;
 			double minimumBidZero = 0.3;
 			double hitParameter = 1.3;
 			double parameterZero = 1.05;
 			double specialityParameterZero = 1.2;
 		
 			if(get(r, clicks) == 0 || get(r, values) == 0) {
 				// INITIALISATION VARIANT
 				// bid = sales_profit * mu
 				
 				bid = initialBids(r);
 			} else {
 				// bid = VPC * omega
 				
 				double revenue = ((SalesReport)((LinkedList)salesReports).getLast()).getRevenue(q); 
 				double lastBid = ((BidBundle)((LinkedList)bidBundles).getLast()).getBid(q);
 				
 				if (revenue != 0) {
 				
 					//cr = click/conversion
 					double clicks = ((QueryReport)((LinkedList)queryReports).getLast()).getClicks(q);
 					double conversions = ((SalesReport)((LinkedList)salesReports).getLast()).getConversions(q);
 					double conversionRate = clicks/conversions;
 
 					//get the new bid value 
 					bid = defineBid(conversionRate, q);
 					
 					//if focus level is F0 or F1
 					if ((r == R.NEUTRAL) || (r == R.HITNEUTRAL) || (r == R.MISSNEUTRAL)) {
 						bid = focusParameter * lastBid;
 					}
 					
 					//if the item was our speciality, increase bid accordingly
 					if (r == R.HIT) {
 						bid = specialityParameter * lastBid;
 					}
 				} else {
 					//if focus level is F2
 					if ((r == R.MISS) || (r == R.HIT)) {
 						bid = Math.min(minimumBidZero, hitParameter * lastBid);
 						//if this is our speciality
 						if (r == R.HIT) {
 							bid = specialityParameterZero * lastBid; 
 						}
 					} else {
 						bid = parameterZero * lastBid;
 					}				
 				}
 			}
 			return bid;
 		}
 	
 	/**
 	*	Returns a bid value 
 	**/
 	private double defineBid(double conversionRate, Query q) {
 		double bid = 0.0;
 		double lowerBound = 10;
 		double middleBound = 0.65;
 		
 		double minBid = 0.1;
 		
 		double decreaseFactor = 0.97;
 		double increaseFactor = 1.15;
 		double maxIncreaseFactor = 1.2;
 		
 		double lastBid = ((BidBundle)((LinkedList)bidBundles).getLast()).getBid(q);
 		
 		//get last bid on keyword
 		if (conversionRate < lowerBound) {
 			bid = Math.max(minBid, decreaseFactor * lastBid);
 		} else if ((lowerBound <= conversionRate) && (conversionRate <= middleBound)) {
 			bid = Math.max(minBid, increaseFactor * lastBid);
 		} else {
 			bid = Math.max(minBid, maxIncreaseFactor * lastBid);
 		}
 		
 		return bid;
 	}
 		
 	private double initialBids(R r) {  
 
 		double bid = 0.0;
 		
 		for(Query query : querySpace) {
 		
 			switch(r) {
 				case MISS:          bid = retailCatalog.getSalesProfit(new Product(query.getManufacturer(), query.getComponent())) * 1.15;
 					break;
 				case MISSNEUTRAL:   bid = retailCatalog.getSalesProfit(new Product(query.getManufacturer(), query.getComponent())) * 0.65;
 					break;
 				case MISSHIT:       bid = retailCatalog.getSalesProfit(new Product(query.getManufacturer(), query.getComponent())) * 1.05;
 					break;
 				case NEUTRAL:       bid = retailCatalog.getSalesProfit(new Product(query.getManufacturer(), query.getComponent())) * 1.15;
 					break;
 				case HITNEUTRAL:    bid = retailCatalog.getSalesProfit(new Product(query.getManufacturer(), query.getComponent())) * 1.15;
 					break;
 				case HIT:           bid = retailCatalog.getSalesProfit(new Product(query.getManufacturer(), query.getComponent())) * 1.25;
 					break;
 				default:            bid = 0;
 			}
 		}
 		
		return bid;
 	}
 		
 		private boolean isManufacturerSpeciality(Query query) {
 			return (query.getManufacturer() == null ? false : query.getManufacturer().equals(advertiserInfo.getManufacturerSpecialty()));
 		}
 		
 		private boolean isComponentSpeciality(Query query) {
 			return (query.getComponent() == null ? false : query.getComponent().equals(advertiserInfo.getComponentSpecialty()));
 		}
 		
 		private void addClicksAndVals(R r, Query query) {
 			set(r, get(r, clicks) + getClicks(query), clicks);
 			set(r, get(r, values) + getValue(query), values);
 			//moneyMade += getValue(query);
 			//moneySpent += getCost(query);
 		}
 		
 		/**
 	     * Processes an incoming query report.
 	     *
 	     * @param queryReport the daily query report.
 	     */
 	    protected void handleQueryReport(QueryReport queryReport) {
 	        queryReports.add(queryReport);
 	    }
 
 	    /**
 	     * Processes an incoming sales report.
 	     *
 	     * @param salesReport the daily sales report.
 	     */
 	    protected void handleSalesReport(SalesReport salesReport) {
 	        salesReports.add(salesReport);
 	    }
 
 	    /**
 	     * Processes a simulation status notification.  Each simulation day the {@link SimulationStatus simulation status }
 	     * notification is sent after the other daily messages ({@link QueryReport} {@link SalesReport} have been sent.
 	     *
 	     * @param simulationStatus the daily simulation status.
 	     */
 	    protected void handleSimulationStatus(SimulationStatus simulationStatus) {
 	        sendBidAndAds();
 	    }
 
 	    /**
 	     * Processes the publisher information.
 	     * @param publisherInfo the publisher information.
 	     */
 	    protected void handlePublisherInfo(PublisherInfo publisherInfo) {
 	        this.publisherInfo = publisherInfo;
 	    }
 
 	    /**
 	     * Processrs the slot information.
 	     * @param slotInfo the slot information.
 	     */
 	    protected void handleSlotInfo(SlotInfo slotInfo) {
 	        this.slotInfo = slotInfo;
 	    }
 
 	    /**
 	     * Processes the retail catalog.
 	     * @param retailCatalog the retail catalog.
 	     */
 	    protected void handleRetailCatalog(RetailCatalog retailCatalog) {
 	        this.retailCatalog = retailCatalog;
 
 	        // The query space is all the F0, F1, and F2 queries for each product
 	        // The F0 query class
 	        if(retailCatalog.size() > 0) {
 	            querySpace.add(new Query(null, null));
 	        }
 
 	        for(Product product : retailCatalog) {
 	            // The F1 query classes
 	            // F1 Manufacturer only
 	            querySpace.add(new Query(product.getManufacturer(), null));
 	            // F1 Component only
 	            querySpace.add(new Query(null, product.getComponent()));
 
 	            // The F2 query class
 	            querySpace.add(new Query(product.getManufacturer(), product.getComponent()));
 	        }
 	    }
 
 	    /**
 	     * Processes the advertiser information.
 	     * @param advertiserInfo the advertiser information.
 	     */
 	    protected void handleAdvertiserInfo(AdvertiserInfo advertiserInfo) {
 	        this.advertiserInfo = advertiserInfo;
 	    }
 
 	    /**
 	     * Processes the start information.
 	     * @param startInfo the start information.
 	     */
 	    protected void handleStartInfo(StartInfo startInfo) {
 	        this.startInfo = startInfo;
 	    }
 
 	    /**
 	     * Prepares the agent for a new simulation.
 	     */
 	    protected void simulationSetup() {
 	    	
 	    	try {
 	    	
 	    		comms = new Socket("localhost", 6502);
 	    	
 	    	} catch (IOException e) {
 	    		
 	    		
 	    	}
 	    }
 
 	    /**
 	     * Runs any post-processes required for the agent after a simulation ends.
 	     */
 	    protected void simulationFinished() {
 	        salesReports.clear();
 	        queryReports.clear();
 	        querySpace.clear();
 	    }
 		
 		private void set(R r, double f, double[] fA)
 		  {
 			fA[index(r)] = f;
 		  }
 		
 		private double get(R r, double[] fA)
 		  {
 			 return fA[index(r)];
 		  }
 		  
 		private void generateEnumMap()
 		  {
 			  Result.put(R.MISS, new Integer(0));
 			  Result.put(R.MISSNEUTRAL, new Integer(1));
 			  Result.put(R.MISSHIT, new Integer(2));
 			  Result.put(R.NEUTRAL, new Integer(3));
 			  Result.put(R.HITNEUTRAL, new Integer(4));
 			  Result.put(R.HIT, new Integer(5));
 		  }
 		  
 		private int index(R r)
 		  {
			return ((int) Result.get(r));
 		  }
 	}
