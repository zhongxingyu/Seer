 /**
  * TAC AgentWare
  * http://www.sics.se/tac        tac-dev@sics.se
  *
  * Copyright (c) 2001-2005 SICS AB. All rights reserved.
  *
  * SICS grants you the right to use, modify, and redistribute this
  * software for noncommercial purposes, on the conditions that you:
  * (1) retain the original headers, including the copyright notice and
  * this text, (2) clearly document the difference between any derived
  * software and the original, and (3) acknowledge your use of this
  * software in pertaining publications and reports.  SICS provides
  * this software "as is", without any warranty of any kind.  IN NO
  * EVENT SHALL SICS BE LIABLE FOR ANY DIRECT, SPECIAL OR INDIRECT,
  * PUNITIVE, INCIDENTAL OR CONSEQUENTIAL LOSSES OR DAMAGES ARISING OUT
  * OF THE USE OF THE SOFTWARE.
  *
  * -----------------------------------------------------------------
  *
  * Author  : Joakim Eriksson, Niclas Finne, Sverker Janson
  * Created : 23 April, 2002
  * Updated : $Date: 2005/06/07 19:06:16 $
  *	     $Revision: 1.1 $
  * ---------------------------------------------------------
  * DummyAgent is a simplest possible agent for TAC. It uses
  * the TACAgent agent ware to interact with the TAC server.
  *
  * Important methods in TACAgent:
  *
  * Retrieving information about the current Game
  * ---------------------------------------------
  * int getGameID()
  *  - returns the id of current game or -1 if no game is currently plaing
  *
  * getServerTime()
  *  - returns the current server time in milliseconds
  *
  * getGameTime()
  *  - returns the time from start of game in milliseconds
  *
  * getGameTimeLeft()
  *  - returns the time left in the game in milliseconds
  *
  * getGameLength()
  *  - returns the game length in milliseconds
  *
  * int getAuctionNo()
  *  - returns the number of auctions in TAC
  *
  * int getClientPreference(int client, int type)
  *  - returns the clients preference for the specified type
  *   (types are TACAgent.{ARRIVAL, DEPARTURE, HOTEL_VALUE, E1, E2, E3}
  *
  * int getAuctionFor(int category, int type, int day)
  *  - returns the auction-id for the requested resource
  *   (categories are TACAgent.{CAT_FLIGHT, CAT_HOTEL, CAT_ENTERTAINMENT
  *    and types are TACAgent.TYPE_INFLIGHT, TACAgent.TYPE_OUTFLIGHT, etc)
  *
  * int getAuctionCategory(int auction)
  *  - returns the category for this auction (CAT_FLIGHT, CAT_HOTEL,
  *    CAT_ENTERTAINMENT)
  *
  * int getAuctionDay(int auction)
  *  - returns the day for this auction.
  *
  * int getAuctionType(int auction)
  *  - returns the type for this auction (TYPE_INFLIGHT, TYPE_OUTFLIGHT, etc).
  *
  * int getOwn(int auction)
  *  - returns the number of items that the agent own for this
  *    auction
  *
  * Submitting Bids
  * ---------------------------------------------
  * void submitBid(Bid)
  *  - submits a bid to the tac server
  *
  * void replaceBid(OldBid, Bid)
  *  - replaces the old bid (the current active bid) in the tac server
  *
  *   Bids have the following important methods:
  *    - create a bid with new Bid(AuctionID)
  *
  *   void addBidPoint(int quantity, float price)
  *    - adds a bid point in the bid
  *
  * Help methods for remembering what to buy for each auction:
  * ----------------------------------------------------------
  * int getAllocation(int auctionID)
  *   - returns the allocation set for this auction
  * void setAllocation(int auctionID, int quantity)
  *   - set the allocation for this auction
  *
  *
  * Callbacks from the TACAgent (caused via interaction with server)
  *
  * bidUpdated(Bid bid)
  *  - there are TACAgent have received an answer on a bid query/submission
  *   (new information about the bid is available)
  * bidRejected(Bid bid)
  *  - the bid has been rejected (reason is bid.getRejectReason())
  * bidError(Bid bid, int error)
  *  - the bid contained errors (error represent error status - commandStatus)
  *
  * quoteUpdated(Quote quote)
  *  - new information about the quotes on the auction (quote.getAuction())
  *    has arrived
  * quoteUpdated(int category)
  *  - new information about the quotes on all auctions for the auction
  *    category has arrived (quotes for a specific type of auctions are
  *    often requested at once).
 
  * auctionClosed(int auction)
  *  - the auction with id "auction" has closed
  *
  * transaction(Transaction transaction)
  *  - there has been a transaction
  *
  * gameStarted()
  *  - a TAC game has started, and all information about the
  *    game is available (preferences etc).
  *
  * gameStopped()
  *  - the current game has ended
  *
  */
 
 package se.sics.tac.aw;
 import se.sics.tac.util.ArgEnumerator;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.logging.*;
 
 import java.io.File;
 import java.util.HashMap;
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.transform.*;
 import javax.xml.transform.dom.DOMSource;
 import javax.xml.transform.stream.StreamResult;
 import org.w3c.dom.*;
 
 public class DummyAgent extends AgentImpl {
 
 	private static final Logger log =
 		Logger.getLogger(DummyAgent.class.getName());
 
 	private static final boolean DEBUG = false;
 
 	private float[] prices;
 	
 	/**
 	 * ID's for the relevant entertainment auctions format [eType][day]
 	 */
 	private int[][] entAuctionIds;
 	/**
 	 * Ordered array of the entertainment tickets we need in order of the bonus we'll receive for them.
 	 */
 	ArrayList<TicketPriorityEntry> entTicketPriorityList;
 	
 	/**
 	 * Lists each client's availability for entertainment.
 	 */
 	ArrayList<ClientEntertainmentAlloc> clientEntAvail;
 	
 	/**
 	 * Holds all of the tickets put up for sale.
 	 * When the game first starts, this list will also be used to generate the initial bids.
 	 */
 	ArrayList<TicketSale> ticketSales;
 	
 	/**
 	 * Holds the entries of all the tickets we want to buy
 	 */
 	ArrayList<TicketPurchase> ticketPurchases;
 	
 	/**
 	 * Contains records of whether flight costs have been logged
 	 */
 	private FlightsLogged loggedFlights;
 	
 	/**
 	 * Holds the flight costs logged for this game
 	 */
 	private LoggedCosts loggedCosts;
 	
 	/**
 	 * Holds the average cost for flights loaded from the XML file
 	 */
 	private LoggedCosts averageCosts;
 	
 	/**
 	 * XML containing the flight costs so far
 	 */
 	private Document flightCostXml;
 	
 	/**
 	 * Contains the previous flight costs as read from the XML file
 	 */
 	private ReadCosts previousFlightCosts;
 	
 	/**
 	 * When true, this will indicate that we should base our flight costs on the generated averages
 	 */
 	private boolean useAverage = false;
 	
 	/**
 	 * last time we made a flight purchase
 	 */
 	private long flightPurchase;
 	
 	/**
 	 * last time we made an entertainment ticket purchase
 	 */
 	private long entPurchase;
 	
 	private static final long PURCHASE_DELAY = 3000;
 	
 	//These booleans control what testing logs should be displayed
 	/**
 	 * Should the log for the entertainment functions be displayed
 	 */
 	private static final boolean LOG_ENTERTAINMENT = true;
 	
 	/**
 	 * Should the log for the XML functions be displayed
 	 */
 	private static final boolean LOG_XML = false;
 
 	protected void init(ArgEnumerator args) {
 		prices = new float[agent.getAuctionNo()];
 	}
 
 	public void quoteUpdated(Quote quote) {
 		int auction = quote.getAuction();
 		int auctionCategory = agent.getAuctionCategory(auction);
 		if (auctionCategory == TACAgent.CAT_HOTEL) {
 			int alloc = agent.getAllocation(auction);
 			if (alloc > 0 && quote.hasHQW(agent.getBid(auction)) &&
 					quote.getHQW() < alloc) {
 				Bid bid = new Bid(auction);
 				// Can not own anything in hotel auctions...
 				prices[auction] = quote.getAskPrice() + 50;
 				bid.addBidPoint(alloc, prices[auction]);
 				if (DEBUG) {
 					log.finest("submitting bid with alloc="
 							+ agent.getAllocation(auction)
 							+ " own=" + agent.getOwn(auction));
 				}
 				agent.submitBid(bid);
 			}
 		} else if (auctionCategory == TACAgent.CAT_ENTERTAINMENT) {
 			//Update the selling prices of tickets - this largely exists for when we don't buy for a while
 			int auctionId = quote.getAuction();
 			
 			//Worth sacrificing a minor saving for not double buying
 			Bid bid = agent.getBid(auctionId);
 			if(bid!=null && bid.getNoBidPoints()>0){
 				if(!bid.isAwaitingTransactions()){
 					//Check if the current bids mean we've already made the purchase - don't want to buy again
 					if(!(quote.getAskPrice()<bid.getPrice(0))){		
 						log.finest("bidding points:" + bid.getNoBidPoints() +" current asking price: " + quote.getAskPrice() + " current bid price: " + bid.getPrice(0));
 						if(agent.getGameTime() > (entPurchase + PURCHASE_DELAY) && agent.getGameTime() > 5000){
 							log.finest("Updated Quotes updating flights for auction:" + auctionId);
 							//Update bidding costs
 							updateEntertainmentSellingPrices();
 						}
 					}
 				}
 			}
 		} else if (auctionCategory == TACAgent.CAT_FLIGHT) {
 			int auctionId = quote.getAuction();
 			
 			//Worth sacrificing a minor saving for not double buying
 			Bid bid = agent.getBid(auctionId);
 			if(bid!=null && bid.getNoBidPoints()>0){
 				if(!bid.isAwaitingTransactions()){
 					//Check if the current bids mean we've already made the purchase - don't want to buy again
 					if(!(quote.getAskPrice()<bid.getPrice(0))){		
 						log.finest("bidding points:" + bid.getNoBidPoints() +" current asking price: " + quote.getAskPrice() + " current bid price: " + bid.getPrice(0));
 						if(agent.getGameTime() > (flightPurchase + PURCHASE_DELAY) && agent.getGameTime() > 5000){
 							log.finest("Updated Quotes updating flights for auction:" + auctionId);
 							//Update bidding costs
 							submitFlightBids(auctionId);
 						}
 					}
 				}
 			}
 			
 			//Update Costs Logs
 			int interval = (int) Math.floor((agent.getGameTime()/15000))/2;
 			FlightDirection flightDirection=null;
 			if(agent.getAuctionType(auctionId) == TACAgent.TYPE_INFLIGHT){
 				flightDirection = FlightDirection.In;
 			}else{
 				flightDirection = FlightDirection.Out;
 			}
 			
 			log.finest("interval: " + interval);
 			
 			if(!loggedFlights.checkIfLogged(flightDirection, agent.getAuctionDay(auctionId), interval)){
 				loggedCosts.setLoggedCost(flightDirection, agent.getAuctionDay(auctionId), interval, quote.getAskPrice());
 				loggedFlights.loggedFlight(flightDirection, agent.getAuctionDay(auctionId), interval);
 			}
 		}
 	}
 
 	public void quoteUpdated(int auctionCategory) {
 		log.fine("All quotes for "
 				+ agent.auctionCategoryToString(auctionCategory)
 				+ " has been updated");
 	}
 
 	public void bidUpdated(Bid bid) {
 		log.fine("Bid Updated: id=" + bid.getID() + " auction="
 				+ bid.getAuction() + " state="
 				+ bid.getProcessingStateAsString());
 		log.fine("       Hash: " + bid.getBidHash());
 	}
 
 	public void bidRejected(Bid bid) {
 		log.warning("Bid Rejected: " + bid.getID());
 		log.warning("      Reason: " + bid.getRejectReason()
 				+ " (" + bid.getRejectReasonAsString() + ')');
 	}
 
 	public void bidError(Bid bid, int status) {
 		log.warning("Bid Error in auction " + bid.getAuction() + ": " + status
 				+ " (" + agent.commandStatusToString(status) + ')');
 	}
 	
 	public void transaction(Transaction transaction) {
 		int auction = transaction.getAuction();
 		int auctionCategory = agent.getAuctionCategory(auction);
 		if (auctionCategory == TACAgent.CAT_ENTERTAINMENT) {
 			entPurchase=agent.getGameTime();
 			allocateEntertainment();
 			
 		}else if (auctionCategory == TACAgent.CAT_FLIGHT) {
 			flightPurchase=agent.getGameTime();
 			log.finest("Updating flight bids from transaction for auction:" + auction);
 		}
 	}
 
 	public void gameStarted() {
 		log.fine("Game " + agent.getGameID() + " started!");
 		
 		//Log orientated functions
 		loggedFlights = new FlightsLogged();
 		loggedCosts = new LoggedCosts();
 		averageCosts = new LoggedCosts();
 		flightCostXml=null;
 		flightPurchase=0;
 		readFlightLog();
 
 		//Functions dealing with entertainment auctions
 		getEntAuctionIds();			//Create an array containing all of the auction ID's
 		entPurchase =0;
 		allocateEntertainment();
 		
 		//Old Dummy Methods
 		calculateAllocation();
 		sendBids();
 	}
 
 	public void gameStopped() {
 		log.fine("Game Stopped!");
 		
 		loggedCosts.printToLog(log);
 		writeFlightLog();
 	}
 
 	public void auctionClosed(int auction) {
 		log.fine("*** Auction " + auction + " closed!");
 	}
 	
 	private void allocateEntertainment(){
 		//Make sure arrays are null
 		entTicketPriorityList = null;
 		clientEntAvail = null;
 		ticketSales = null;
 		ticketPurchases = null;
 		
 		
 		entTicketPriority();		//Create a list of the order entertainment tickets should be allocated in
 		createClientEntArray(); 	//Create a blank array with client details
 		ticketSales = new ArrayList<TicketSale>();	//Creates a blank array for generating ticketSales
 		allocateStartingTickets();	//Allocates the tickets we're assigned and sells the un-needed tickets
 		sellTickets();				//Puts all tickets we've allocated up for sale
 		ticketPurchases = new ArrayList<TicketPurchase>();	//Creates a blank array for generating ticketSales
 		buyTickets();				//Puts bids in for tickets to get additional fun bonuses
 		
 		processBids();				//Generates the initial bid strings as determined by the previous functions
 	}
 
 	private void sendBids() {
 		for (int i = 0, n = agent.getAuctionNo(); i < n; i++) {
 			int alloc = agent.getAllocation(i) - agent.getOwn(i);
 			float price = -1f;
 			switch (agent.getAuctionCategory(i)) {
 			case TACAgent.CAT_FLIGHT:
 				log.finest("sendBids updating flights for auction:" + i);
 				submitFlightBids(i);
 				flightPurchase=agent.getGameTime();
 				break;
 			case TACAgent.CAT_HOTEL:
 				if (alloc > 0) {
 					price = 200;
 					prices[i] = 200f;
 				}
 				break;
 				
 			case TACAgent.CAT_ENTERTAINMENT:
 				/*
 				if (alloc < 0) {
 					price = 200;
 					prices[i] = 200f;
 				} else if (alloc > 0) {
 					price = 50;
 					prices[i] = 50f;
 				}
 				*/
 				break;
 				
 			default:
 				break;
 			}
 			//We don't want to submit flight bids - have done that already!
 			if (price > 0 && agent.getAuctionCategory(i)!=TACAgent.CAT_FLIGHT) {
 				Bid bid = new Bid(i);
 				bid.addBidPoint(alloc, price);
 				if (DEBUG) {
 					log.finest("submitting bid with alloc=" + agent.getAllocation(i)
 							+ " own=" + agent.getOwn(i));
 				}
 				agent.submitBid(bid);
 			}
 		}
 	}
 
 	private void calculateAllocation() {
 		//Add a quick check in here - have we done this before? Help stops flights receiving the allocation twice
 		boolean runPreviously = false;
 		int inAlloc=0;
 		int outAlloc=0;
 		for (int i = 0; i < 8; i++) {
 			int inFlight = agent.getClientPreference(i, TACAgent.ARRIVAL);
 			int outFlight = agent.getClientPreference(i, TACAgent.DEPARTURE);
 			int auction = agent.getAuctionFor(TACAgent.CAT_FLIGHT,
 					TACAgent.TYPE_INFLIGHT, inFlight);
 			inAlloc = inAlloc + agent.getAllocation(auction);
 			auction = agent.getAuctionFor(TACAgent.CAT_FLIGHT,
 					TACAgent.TYPE_OUTFLIGHT, outFlight);
 			outAlloc = outAlloc + agent.getAllocation(auction);
 		}
 		if (inAlloc>0 || outAlloc>0){
 			runPreviously=true;
 		}
 		
 		for (int i = 0; i < 8; i++) {
 			int inFlight = agent.getClientPreference(i, TACAgent.ARRIVAL);
 			int outFlight = agent.getClientPreference(i, TACAgent.DEPARTURE);
 			int hotel = agent.getClientPreference(i, TACAgent.HOTEL_VALUE);
 			int type;
 
 			int auction;
 			if(!runPreviously){
 				// Get the flight preferences auction and remember that we are
 				// going to buy tickets for these days. (inflight=1, outflight=0)
 				auction = agent.getAuctionFor(TACAgent.CAT_FLIGHT,
 						TACAgent.TYPE_INFLIGHT, inFlight);
 				agent.setAllocation(auction, agent.getAllocation(auction) + 1);
 				auction = agent.getAuctionFor(TACAgent.CAT_FLIGHT,
 						TACAgent.TYPE_OUTFLIGHT, outFlight);
 				agent.setAllocation(auction, agent.getAllocation(auction) + 1);
 			}
 
 			// if the hotel value is greater than 70 we will select the
 			// expensive hotel (type = 1)
 			if (hotel > 70) {
 				type = TACAgent.TYPE_GOOD_HOTEL;
 			} else {
 				type = TACAgent.TYPE_CHEAP_HOTEL;
 			}
 			// allocate a hotel night for each day that the agent stays
 			for (int d = inFlight; d < outFlight; d++) {
 				auction = agent.getAuctionFor(TACAgent.CAT_HOTEL, type, d);
 				log.finer("Adding hotel for day: " + d + " on " + auction);
 				agent.setAllocation(auction, agent.getAllocation(auction) + 1);
 			}
 			
 			
 			//TODO Remove Existing entertainment functions
 			/*
 			int eType = -1;
 			while((eType = nextEntType(i, eType)) > 0) {
 				auction = bestEntDay(inFlight, outFlight, eType);
 				log.finer("Adding entertainment " + eType + " on " + auction);
 				agent.setAllocation(auction, agent.getAllocation(auction) + 1);
 			}
 			*/
 		}
 	}
 	
 	/**
 	 * Gets all of the entertainment auction ID's
 	 */
 	private void getEntAuctionIds(){
 		entAuctionIds = new int[3][4];
 		for (int ent = 1; ent < 4; ent++){
 			for (int day =1; day < 5; day++){
 				entAuctionIds[ent-1][day-1]=agent.getAuctionFor(TACAgent.CAT_ENTERTAINMENT,ent, day);
 			}
 		}
 	}
 	
 	/**
 	 * Create an ordered array of the entertainment tickets we require
 	 */
 	private void entTicketPriority(){
 		ArrayList<TicketPriorityEntry> entPriority = new ArrayList<TicketPriorityEntry>();
 		for (int client=0; client<8; client++){
 			entPriority.add(new TicketPriorityEntry(client, 1,agent.getClientPreference(client, TACAgent.E1),-1));
 			entPriority.add(new TicketPriorityEntry(client, 2,agent.getClientPreference(client, TACAgent.E2),-1));
 			entPriority.add(new TicketPriorityEntry(client, 3,agent.getClientPreference(client, TACAgent.E3),-1));
 		}
 		
 		//Sort the list into the correct priority order
 		Collections.sort(entPriority);
 		Collections.reverse(entPriority);
 		entTicketPriorityList = entPriority;
 		
 		//For Testing, we will print this array
 		if(LOG_ENTERTAINMENT){
 			for (int i=0; i< entPriority.size(); i++){
 				log.finer("Position: " + i + " Client: "+ entPriority.get(i).getClient() + " eType: " + entPriority.get(i).geteType() + " Value: " + entPriority.get(i).getFunBonus());
 			}
 		}
 	}
 	
 	/**
 	 * 
 	 */
 	private void createClientEntArray(){
 		
 		ArrayList<ClientEntertainmentAlloc> clientArray = new ArrayList<ClientEntertainmentAlloc>();
 		for (int client=0; client<8; client++){
 			ClientEntertainmentAlloc clientAllocation = new ClientEntertainmentAlloc(
 					new EntertainmentAllocation(agent.getClientPreference(client, TACAgent.E1),-1), 
 					new EntertainmentAllocation(agent.getClientPreference(client, TACAgent.E2),-1), 
 					new EntertainmentAllocation(agent.getClientPreference(client, TACAgent.E3),-1), 
 					agent.getClientPreference(client, TACAgent.DEPARTURE)-agent.getClientPreference(client, TACAgent.ARRIVAL),
 					client);
 			clientArray.add(clientAllocation);
 		}
 		clientEntAvail = clientArray;
 	}
 	
 	/**
 	 * This function allocates all of our starting entertainment tickets to a client.
 	 * If a ticket cannot be assigned, it gets added to the sale string as we will
 	 * only benefit from it if it is sold.
 	 */
 	private void allocateStartingTickets(){
 		for (int e = 0; e<3; e++){
 			for (int d = 0; d<4; d++){
 				int own = agent.getOwn(agent.getAuctionFor(TACAgent.CAT_ENTERTAINMENT, e+1, d+1));
 				if (LOG_ENTERTAINMENT) {
 					log.finest("We own " + own + "tickets of eType: " +(e+1)
 							+ " on day " + (d+1));
 				}
 				if (own>0){
 					int alloc =0;
 					while(alloc<own){
 						//For our starting tickets, we give them a value of 0
 						if(allocateTicket(d, e+1, 0, true, new ArrayList<AllocationsChecked>()).isSuccess()){
 							log.finest("Just allocated eType: " +(e+1)+ " on day " + (d+1));
 							alloc++;
 						}else{
 							//Any tickets leftover are sold
 							sellLeftoverTickets(e,d,own-alloc,100);
 							alloc=own+1;
 						}
 					}
 				}
 			}
 		}
 	}
 	
 	
 	private AllocateTicketResult allocateTicket(int day, int eType, int value, boolean process, ArrayList<AllocationsChecked> allocChecked){
 		for (int i=0; i<24; i++){
 			if(entTicketPriorityList.get(i).geteType()==eType && entTicketPriorityList.get(i).getDayAssigned()<1){
 				//Check whether the utility gained is greater than the value
 				//This should be the most we'll get, so if utility is less
 				if (value<entTicketPriorityList.get(i).getFunBonus()){
 					//If day during agent's visit
 					if(agent.getClientPreference(entTicketPriorityList.get(i).getClient(), TACAgent.ARRIVAL)-1 < day+1 && 
 							day+1 < agent.getClientPreference(entTicketPriorityList.get(i).getClient(), TACAgent.DEPARTURE)){
 						//Check the client still has days left to visit entertainment
 						if(clientEntAvail.get(entTicketPriorityList.get(i).getClient()).getDaysAssigned()<
 								clientEntAvail.get(entTicketPriorityList.get(i).getClient()).getDaysPossible()){
 							//If the entertainment type still hasn't been allocated to the client
 							if(clientEntAvail.get(entTicketPriorityList.get(i).getClient()).getEntertainmentAllocation(eType).getAssignedDay()<0){
 								//If the entertainment day hasn't been allocated to any other entertainment types
 								if(clientEntAvail.get(entTicketPriorityList.get(i).getClient()).dayAvailable(day)){
 									if (LOG_ENTERTAINMENT) {
 										log.finest("priority " + i + " day assigned: " + day + " eType:" + eType);
 									}
 									//If this call was for information only, we don't want to update our records
 									if(process){
 										updateRecords(day, eType,entTicketPriorityList.get(i).getClient() ,i);
 									}
 									return new AllocateTicketResult(true, entTicketPriorityList.get(i).getClient(), day, eType, value, i);
 								}else{
 									//Handle re-allocating entertainment to get best possible fun bonus - recursive
 									if (LOG_ENTERTAINMENT) {
 										log.finest("Starting to re-assign");
 									}
 									if(checkedAllocation(allocChecked, entTicketPriorityList.get(i).getClient(), day)){
 										//We've checked this one previously, so fail
 										return new AllocateTicketResult(false);
 									}else{
 										allocChecked.add(new AllocationsChecked(entTicketPriorityList.get(i).getClient(), day));
 										//Not checked before, so keep on trying!
 										//Check if we can allocate the eType currently allocated here elsewhere
 										if(allocateTicket(day, clientEntAvail.get(entTicketPriorityList.get(i).getClient()).getEType(day), 
 												value, process,allocChecked).isSuccess()){
 											//If success, it means we have been able to do some reallocation. If applicable, update records
 											if(process){
 												updateRecords(day, eType,entTicketPriorityList.get(i).getClient() ,i);
 											}
 											return new AllocateTicketResult(true, entTicketPriorityList.get(i).getClient(), day, eType, value, i);
 										}else{
 											//This failed, continued passing this result back
 											return new AllocateTicketResult(false);
 										}
 									}
 								}
 							}
 						}
 					}
 				}else{
 					return new AllocateTicketResult(false);
 				}
 			}
 		}
 		return new AllocateTicketResult(false);
 	}
 	
 	private void updateRecords(int day, int eType, int client, int priorityPosn){
 		//Update the priority list to mark as assigned
 		TicketPriorityEntry tpe = entTicketPriorityList.get(priorityPosn);
 		tpe.setDayAssigned(day);
 		entTicketPriorityList.set(priorityPosn, tpe);
 		
 		//Update the client records
 		ClientEntertainmentAlloc cea = clientEntAvail.get(client);
 		cea.setDaysAssigned(cea.getDaysAssigned()+1);
 		cea.updateEntertainmentAllocation(eType, day);
 		
 	}
 	
 	/**
 	 * Tells us if we have previously checked this entertainment allocation
 	 * @param allocChecked - the list of previously checked allocation pairs
 	 * @param client - the client we're currently checking
 	 * @param day - the day we're currently checking
 	 * @return boolean declaring whether it has been checked previously or not
 	 */
 	private boolean checkedAllocation(ArrayList<AllocationsChecked> allocChecked, int client, int day){
 		for(AllocationsChecked ac: allocChecked){
 			if(ac.getClient()== client && ac.getDay()==day){
 				return true;
 			}
 		}
 		return false;
 	}
 	
 	/**
 	 * For each ticket left over, this creates an entry in the ticketSales array
 	 * to be added to the initial set of bids submitted
 	 * @param eType
 	 * @param day
 	 * @param quantity
 	 * @param 
 	 */
 	private void sellLeftoverTickets(int eType, int day, int quantity, int initialPrice){
 		for(int i=0; i<quantity; i++){
 			TicketSale ticketSale = new TicketSale(entAuctionIds[eType][day],
 					-1,			//a client of -1 indicates surplus
					eType,
 					calculateCurrentSellingPrice(initialPrice,1),
 					initialPrice,
 					1,			//This ticket strictly has no value
 					SalePurpose.surplus);
 			ticketSales.add(ticketSale);
 		}
 	}
 	
 	
 	/**
 	 * From the list of assigned entertainment tickets, this function generates
 	 * a sale price for them and adds them to the array list of bids to be submitted.<br/>
 	 * The sale price is greater than the fun bonus - this means our agent will always be better
 	 * of by selling a ticket, not worse.
 	 */
 	private void sellTickets(){
 		for (TicketPriorityEntry tpe: entTicketPriorityList){
 			//We want to sell tickets which have been assigned to a user using this method
 			if (tpe.getDayAssigned()>-1){
 				generateTicketSale(tpe,tpe.getDayAssigned());
 			}
 		}
 	}
 	
 	/**
 	 * Goes through all of the possible entertainment slots and tries to buy a tickets if the slot is available
 	 */
 	private void buyTickets(){
 		for(ClientEntertainmentAlloc cea: clientEntAvail){
 			for(int eType=1; eType<4; eType++){
 				if(cea.getEntertainmentAllocation(eType).getAssignedDay()<0){
 					log.finest("client: " +cea.getClient());
 					log.finest("eType: " + eType);
 					//If it isn't assigned, we want to purchase for any available day
 					if(cea.getDaysPossible()>0){
 						generateTicketPurchases(cea, eType);
 					}
 				}
 			}
 		}
 	}
 	
 	/**
 	 * Using the ticketSales and ticketPurchases array list, this function creates
 	 * the initial bids for entertainment at the start of the game
 	 */
 	private void processBids(){
 		for(int e=0; e<3; e++){
 			for (int day=0; day<4; day++){
 				int auctionId = entAuctionIds[e][day];			
 				ArrayList<TicketSale> auctionSales = getSalesByAuction(auctionId);
 				ArrayList<TicketPurchase> auctionPurchases = getPurchasesByAuction(auctionId);
 				if(auctionSales.size()>0 | auctionPurchases.size()>0){
 					Bid bid = new Bid(auctionId);
 					if(auctionSales.size()>0){
 						for(TicketSale ts: auctionSales){	
 							bid.addBidPoint(-1, ts.getCurrentSalePrice());
 							
 						}
 					}
 					if(auctionPurchases.size()>0){
 						for(TicketPurchase tp: auctionPurchases){	
 							bid.addBidPoint(1, tp.getCurrentSalePrice());
 							
 						}
 					}
 					agent.submitBid(bid);
 				}
 			}
 		}
 	}
 	
 	/**
 	 * Returns all of the auctions in the ticketSales array which are for the specified auctionId
 	 * @param auctionId - The auction for which the corresponding sale entries are returned
 	 * @return
 	 */
 	private ArrayList<TicketSale> getSalesByAuction(int auctionId){
 		ArrayList<TicketSale> auctionSale = new ArrayList<TicketSale>();
 		//TODO Is there a better way than iterating through every entry?
 		for(TicketSale ticketSale: ticketSales){
 			if(ticketSale.getAuctionId() == auctionId){
 				auctionSale.add(ticketSale);
 			}
 		}
 		return auctionSale;
 	}
 	
 	/**
 	 * Returns all of the auctions in the ticketPurchases array which are for the specified auctionId
 	 * @param auctionId
 	 * @return
 	 */
 	private ArrayList<TicketPurchase> getPurchasesByAuction(int auctionId){
 		ArrayList<TicketPurchase> auctionPurchase = new ArrayList<TicketPurchase>();
 		//TODO Is there a better way than iterating through every entry?
 		for(TicketPurchase ticketPurchase: ticketPurchases){
 			if(ticketPurchase.getAuctionId() == auctionId){
 				auctionPurchase.add(ticketPurchase);
 			}
 		}
 		return auctionPurchase;
 	}
 	
 	
 	/**
 	 * Generates all of the possible ticket purchases for a defined client and entertainment type
 	 * @param cea - The client, contained within a ClientEntertainmentAlloc Item
 	 * @param eType - The entertainment type to buy tickets for
 	 */
 	private void generateTicketPurchases(ClientEntertainmentAlloc cea, int eType){
 		for(int day = agent.getClientPreference(cea.getClient(), TACAgent.ARRIVAL)-1; 
 				day< agent.getClientPreference(cea.getClient(), TACAgent.DEPARTURE)-1; day++){
 			if(cea.dayAvailable(day)){
 				//We want to generate a buy price which is likely, but also worth the profit.
 				int buyPrice;
 				if(cea.getEntertainmentAllocation(eType).getFunBonus()<20){
 					buyPrice = cea.getEntertainmentAllocation(eType).getFunBonus()-2;
 				}else if(cea.getEntertainmentAllocation(eType).getFunBonus()<30){
 					buyPrice = cea.getEntertainmentAllocation(eType).getFunBonus()-10;
 				}else{
 					buyPrice = cea.getEntertainmentAllocation(eType).getFunBonus()-20;
 				}
 				if(buyPrice<0){
 					buyPrice=0;
 				}
 				TicketPurchase ticketPurchase = new TicketPurchase(entAuctionIds[eType-1][day],
 						cea.getClient(),
 						eType,
 						buyPrice,	
 						buyPrice,	//TODO handle current
 						cea.getEntertainmentAllocation(eType).getFunBonus());
 				log.finest("Just added TicketPurchase. eType: " + ticketPurchase.geteType()
 						+ " Client: " + ticketPurchase.getClientId()
 						+ " Sale Price: " + ticketPurchase.getCurrentSalePrice()
 						+ " Value: " + ticketPurchase.getValue());
 				ticketPurchases.add(ticketPurchase);
 			}
 		}
 	}
 	
 	/**
 	 * Adds an entry to the list of ticket sales
 	 * @param tpe
 	 */
 	private void generateTicketSale(TicketPriorityEntry tpe, int day){
 		TicketSale ticketSale = new TicketSale(entAuctionIds[tpe.geteType()-1][day],
 				tpe.getClient(),
 				tpe.geteType(),
 				calculateCurrentSellingPrice(tpe.getFunBonus()+50,tpe.getFunBonus()),	//Calculate the current sales price
 				tpe.getFunBonus()+50,	//The initial starting selling price is the fun bonus + 50
 				tpe.getFunBonus(),
 				SalePurpose.standard);
 		log.finest("Just created ticketSale. eType: " + ticketSale.geteType()
 				+ " Client: " + ticketSale.getClientId()
 				+ " Sale Price: " + ticketSale.getCurrentSalePrice()
 				+ " Value: " + ticketSale.getValue());
 		ticketSales.add(ticketSale);
 	}
 	
 	/**
 	 * Used to calculate how much the current selling price should be set to
 	 * @param startSelling
 	 * @param stopSelling
 	 * @return
 	 */
 	private int calculateCurrentSellingPrice(int startSelling, int stopSelling){
 		double timeSpan = 360000;
 		double timeToGo = timeSpan- ((Long) (agent.getGameTimeLeft())).doubleValue();
 		log.finest("timeToGo: " + timeToGo);
 		if(timeToGo>0){
 			double timePercent = timeToGo/timeSpan;
 			double variablePrice = (double)(startSelling-stopSelling);
 			int priceDecrease = (int) (timePercent * variablePrice);
 			log.finest("calculation: " + timePercent + "*" + variablePrice);
 			log.finest("New selling price: " + (startSelling-priceDecrease));
 			return startSelling-priceDecrease;
 		}else{
 			return startSelling;
 		}
 	}
 	
 	private void updateEntertainmentSellingPrices(){
 		log.finest("Updating Entertainment Ticket Sale Prices");
 		for(TicketSale ts : ticketSales){
 			ts.setCurrentSalePrice(calculateCurrentSellingPrice(ts.getStartingSalePrice(),ts.getValue()));
 		}
 		processBids();
 	}
 	
 	private int bestEntDay(int inFlight, int outFlight, int type) {
 		for (int i = inFlight; i < outFlight; i++) {
 			int auction = agent.getAuctionFor(TACAgent.CAT_ENTERTAINMENT,
 					type, i);
 			if (agent.getAllocation(auction) < agent.getOwn(auction)) {
 				return auction;
 			}
 		}
 		// If no left, just take the first...
 		return agent.getAuctionFor(TACAgent.CAT_ENTERTAINMENT,
 				type, inFlight);
 	}
 
 	private int nextEntType(int client, int lastType) {
 		int e1 = agent.getClientPreference(client, TACAgent.E1);
 		int e2 = agent.getClientPreference(client, TACAgent.E2);
 		int e3 = agent.getClientPreference(client, TACAgent.E3);
 
 		// At least buy what each agent wants the most!!!
 		if ((e1 > e2) && (e1 > e3) && lastType == -1)
 			return TACAgent.TYPE_ALLIGATOR_WRESTLING;
 		if ((e2 > e1) && (e2 > e3) && lastType == -1)
 			return TACAgent.TYPE_AMUSEMENT;
 		if ((e3 > e1) && (e3 > e2) && lastType == -1)
 			return TACAgent.TYPE_MUSEUM;
 		return -1;
 	}
 	
 	/**
 	 * Creates (or adds) to the xml file containing the flight costs recorded over intervals
 	 */
 	private void writeFlightLog(){
 		try {
 			//Create DOM (with top-level node)
 			
 			DocumentBuilder xmlBuilder =
 				DocumentBuilderFactory.newInstance().newDocumentBuilder();
 			Document xmlDoc = xmlBuilder.newDocument();
 			Node node;
 			if(flightCostXml==null){
 				node = xmlDoc.createElement("games");
 				xmlDoc.appendChild(node);  //top-level node
 			}else{
 				xmlDoc=flightCostXml;
 				node= xmlDoc.getFirstChild();
 			}
 
 			//Add Data to DOM
 			Element gameElem = xmlDoc.createElement("game");
 			node.appendChild(gameElem);
 
 			Element inFlightElem = xmlDoc.createElement("inflight");
 			gameElem.appendChild(inFlightElem);
 
 			for(int day=1; day<5; day++){
 				Element dayElem = xmlDoc.createElement("day-"+day);
 				inFlightElem.appendChild(dayElem);
 				
 				for(int i=0; i<18; i++){
 					Element intervalElem = xmlDoc.createElement("interval-"+Integer.toString(i));
 					intervalElem.appendChild(xmlDoc.createTextNode(Float.toString(loggedCosts.getLoggedCost(FlightDirection.In, day, i))));
 					dayElem.appendChild(intervalElem);
 				}
 			}
 			
 			Element outFlightElem = xmlDoc.createElement("outflight");
 			gameElem.appendChild(outFlightElem);
 
 			for(int day=2; day<6; day++){
 				Element dayElem = xmlDoc.createElement("day-"+day);
 				outFlightElem.appendChild(dayElem);
 				
 				for(int i=0; i<18; i++){
 					Element intervalElem = xmlDoc.createElement("interval-"+Integer.toString(i));
 					intervalElem.appendChild(xmlDoc.createTextNode(Float.toString(loggedCosts.getLoggedCost(FlightDirection.Out, day, i))));
 					dayElem.appendChild(intervalElem);
 				}
 			}
 
 			//Write DOM to XML file
 			Source source = new DOMSource(xmlDoc);
 			Result result = new StreamResult(new File("flightcosts.xml"));
 			Transformer xformer =
 				TransformerFactory.newInstance().newTransformer();
 			xformer.setOutputProperty(OutputKeys.INDENT, "yes");
 			xformer.transform(source, result);
 		}
 		catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 	
 	/**
 	 * Reads in the XML file containing all of the previous costs
 	 */
 	private void readFlightLog(){
 		log.finest("Reading XML File");
 		previousFlightCosts = new ReadCosts();
 		try {
 			File xmlFile = new File("flightcosts.xml");
 			if(xmlFile.exists()){
 				DocumentBuilder xmlBuilder =
 					DocumentBuilderFactory.newInstance().newDocumentBuilder();
 				Document xmlDoc = xmlBuilder.parse(xmlFile);
 				flightCostXml=xmlDoc;
 
 				Node node = xmlDoc.getDocumentElement();
 				log.finest("Converting XML to object");
 				for (Node subNode = node.getFirstChild(); subNode != null;
 						subNode = subNode.getNextSibling()){
 					if(LOG_XML){
 						log.finest("Got SubNode: " + subNode.getNodeName());
 					}
 					//At this level we are going through each of the game records
 					if (subNode.getNodeType() == Node.ELEMENT_NODE){
 						if(LOG_XML){
 							log.finest("Inflight");
 						}
 						processFlightXml(FlightDirection.In, ((Element) subNode).getElementsByTagName("inflight"));
 						
 						if(LOG_XML){
 							log.finest("Outflight");
 						}
 						processFlightXml(FlightDirection.Out, ((Element) subNode).getElementsByTagName("outflight"));
 					}
 				}
 				log.finest("Conversion Complete");
 				//Check if we should use this average yet
 				if(previousFlightCosts.getRecordNumbers()>4){
 					useAverage=true;
 					log.finest("We have at least 5 previous game records, use an average");
 					generateAverageValues();
 				}else{
 					log.finest("Need more records. Use default flight purchasing method");
 				}
 			}
 
 		}
 		catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 	
 	/**
 	 * Processes all of the read-in XML for each of the flight entries
 	 * @param flightDirection
 	 * @param flightNode
 	 */
 	private void processFlightXml(FlightDirection flightDirection, NodeList flightNode){
 		try{
 			for (int i=0; i<flightNode.getLength(); i++){
 				Node arrayNode = flightNode.item(i);
 				if (arrayNode.getNodeType() == Node.ELEMENT_NODE){
 					
 					if(flightDirection == FlightDirection.In){
 						for (int day=1; day<5; day++){
 							processDayXml(flightDirection, day, ((Element) arrayNode).getElementsByTagName("day-"+day));
 						}
 						
 					}else if(flightDirection == FlightDirection.Out){
 						for (int day=2; day<6; day++){
 							processDayXml(flightDirection, day, ((Element) arrayNode).getElementsByTagName("day-"+day));
 						}
 					}
 				}
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 	
 	/**
 	 * Processes the XML read-in for each of the day entries
 	 * @param flightDirection
 	 * @param day
 	 * @param dayNode
 	 */
 	private void processDayXml(FlightDirection flightDirection, int day, NodeList dayNode){
 		try{
 			for (int i=0; i<dayNode.getLength(); i++){
 				Node arrayNode = dayNode.item(i);
 				if (arrayNode.getNodeType() == Node.ELEMENT_NODE){
 					
 					for (int interval=0; interval<18; interval++){
 						NodeList intervalNodeList = ((Element) arrayNode).getElementsByTagName("interval-"+interval);
 						Element intervalElement = (Element) intervalNodeList.item(0);
 						NodeList costList = intervalElement.getChildNodes();
 						if(LOG_XML){
 							log.finest("day: "+ day + " interval: " + interval + " cost: " + ((Node) costList.item(0)).getNodeValue());
 						}
 						previousFlightCosts.addCost(flightDirection, 
 								day, 
 								interval, 
 								Float.parseFloat(((Node) costList.item(0)).getNodeValue()));
 							
 					}
 				}
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 	
 	/**
 	 * Generates the average cost of flights based on non-zero entries
 	 */
 	private void generateAverageValues(){
 		ArrayList<IntervalCosts> allCosts = previousFlightCosts.getAllReadCosts();
 		averageCosts = new LoggedCosts();
 		
 		int entry =0;
 		while(entry < allCosts.size()){
 			IntervalCosts costs = allCosts.get(entry);
 			
 			//We need to know the direction and day details later
 			FlightDirection dir;
 			int day;
 			if(entry<4){
 				dir = FlightDirection.In;
 				day = entry +1;
 			}else{
 				dir = FlightDirection.Out;
 				day = entry-2;
 			}
 			
 			for(int i=0; i<18; i++){
 				ArrayList<Float> costArray = costs.getIntervalEntries(i);
 				int records =0;
 				Float sum= new Float(0);
 				for(Float price: costArray){
 					if (price != 0){
 						records++;
 						sum=sum+price;
 					}
 				}
 				sum = sum/records;
 				averageCosts.setLoggedCost(dir, day, i, sum);
 			}	
 			entry++;
 		}		
 		log.finest("Average Costs:");
 		averageCosts.printToLog(log);
 	}
 
 	/**
 	 * Calculates the recommended price which the specified flight should be purchased at
 	 * @param type - Whether it's an inflight or an outflight
 	 * @param day
 	 * @param askingPrice
 	 * @return
 	 */
 	private float calculateFlightPrice(int type, int day, float askingPrice){
 		FlightDirection flightDirection=null;
 		if(type == TACAgent.TYPE_INFLIGHT){
 			flightDirection = FlightDirection.In;
 		}else{
 			flightDirection = FlightDirection.Out;
 		}
 		int interval = (int) Math.floor((agent.getGameTime()/15000))/2;		
 		float avgPrice = averageCosts.getLoggedCost(flightDirection, day, interval);
 		
 		//Less than 20 seconds left, so we know just want to purchase the flights to make sure we have them
 		if(agent.getGameTimeLeft()<20000){
 			return askingPrice;
 		}
 		
 		//If we have an asking price of 0
 		if(askingPrice == 0){
 			log.finest("Asking Price == 0");
 			return (float) (avgPrice*0.8);
 		}
 		
 		if(askingPrice <= (float) (avgPrice*0.8)){
 			log.finest("less than 80, submit asking");
 			return askingPrice;
 		}else if (askingPrice <= avgPrice){
 			if(getPercentCheaperFlights(avgPrice, flightDirection, day, interval)>25){
 				//This suggests that the trend is that the price will go down, so bid at the cheapest average left
 				log.finest("less than avg return low");
 				return ((getCheapestFlightCost(avgPrice, flightDirection, day, interval)+avgPrice)/2);
 			}else{
 				//Although the price may go down, it's not as likely, so get it now - still cheaper than average
 				log.finest("less than avg return asking");
 				return askingPrice;
 			}
 		}else{
 			//This is more expensive than average
 			if(getPercentCheaperFlights(avgPrice, flightDirection, day, interval)>8){
 				//This suggests that the trend is that the price will go down, so bid at 80% and wait
 				log.finest("higher than avg, wait for decrease");
 				return (float) (avgPrice*0.8);
 			}else{
 				//Although the price may go down, it's really not very likely - get it now to save cash
 				log.finest("higher than avg buy now");
 				return askingPrice;
 			}
 		}
 	}
 	
 	/**
 	 * Calculates the percentage of remaining intervals which are cheaper than the specified cost
 	 * @param cost
 	 * @param dir
 	 * @param day
 	 * @param interval
 	 * @return
 	 */
 	private double getPercentCheaperFlights(float cost, FlightDirection dir, int day, int interval){
 		double cheaperIntervals=0;
 		for(int i=interval; i<18; i++){
 			if(averageCosts.getLoggedCost(dir, day, i) < cost){
 				cheaperIntervals++;
 			}
 		}
 		log.finest("Percent cheaper:" + (cheaperIntervals/(18-interval))*100);
 		return (cheaperIntervals/(18-interval))*100;
 	}
 	
 	/**
 	 * Gets the cheapest remaining average cost
 	 * @param cost
 	 * @param dir
 	 * @param day
 	 * @param interval
 	 * @return
 	 */
 	private float getCheapestFlightCost(float cost, FlightDirection dir, int day, int interval){
 		float cheapestCost = cost;
 		for(int i=interval; i<18; i++){
 			if(averageCosts.getLoggedCost(dir, day, i) < cheapestCost){
 				cheapestCost = averageCosts.getLoggedCost(dir, day, i);
 			}
 		}
 		return cheapestCost;
 	}
 	/**
 	 * Creates the bid points for flights
 	 * @param auctionId
 	 */
 	private void submitFlightBids(int auctionId){
 		int alloc = agent.getAllocation(auctionId) - agent.getOwn(auctionId);
 		float price = -1f;
 		if (alloc > 0) {
 			//Check whether to use the default purchase or use averages
 			if (useAverage){
 				price = calculateFlightPrice(agent.getAuctionType(auctionId), agent.getAuctionDay(auctionId), agent.getQuote(auctionId).getAskPrice());
 			}else{
 				price = 1000;
 			}
 		}
 		
 		if (price > 0) {
 			Bid oldBid = agent.getBid(auctionId);
 			Bid bid = new Bid(auctionId);
 			bid.addBidPoint(alloc, price);
 			if (DEBUG) {
 				log.finest("submitting bid with alloc=" + agent.getAllocation(auctionId)
 						+ " own=" + agent.getOwn(auctionId));
 			}
 			
 			if(oldBid == null){
 				agent.submitBid(bid);
 			}else{
 				agent.replaceBid(oldBid, bid);
 			}
 		}
 	}
 
 
 	// -------------------------------------------------------------------
 	// Only for backward compability
 	// -------------------------------------------------------------------
 
 	public static void main (String[] args) {
 		TACAgent.main(args);
 	}
 
 } // DummyAgent
