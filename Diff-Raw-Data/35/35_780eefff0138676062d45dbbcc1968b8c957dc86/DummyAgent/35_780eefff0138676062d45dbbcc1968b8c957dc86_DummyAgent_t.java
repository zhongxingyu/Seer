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
 import java.util.List;
 import java.util.logging.*;
 
 public class DummyAgent extends AgentImpl {
 
 	private static final Logger log =
 		Logger.getLogger(DummyAgent.class.getName());
 
 	private static final boolean DEBUG = false;
 
 	private float[] prices;
 	
 	/**
 	 * Contains the maximum number of entertainment tickets we'll ever need each day
 	 */
 	private int[] maxEntPerDay;
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
 	
 	//These booleans control what testing logs should be displayed
 	/**
 	 * Should the log for the entertainment functions be displayed
 	 */
 	private boolean LOG_ENTERTAINMENT = true;
 
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
 			/*
 			int alloc = agent.getAllocation(auction) - agent.getOwn(auction);
 			if (alloc != 0) {
 				Bid bid = new Bid(auction);
 				if (alloc < 0)
 					prices[auction] = 200f - (agent.getGameTime() * 120f) / 720000;
 				else
 					prices[auction] = 50f + (agent.getGameTime() * 100f) / 720000;
 				bid.addBidPoint(alloc, prices[auction]);
 				if (DEBUG) {
 					log.finest("submitting bid with alloc="
 							+ agent.getAllocation(auction)
 							+ " own=" + agent.getOwn(auction));
 				}
 				agent.submitBid(bid);
 			}
 			*/
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
 
 	public void gameStarted() {
 		log.fine("Game " + agent.getGameID() + " started!");
 
 		//Functions dealing with entertainment auctions
 		getEntAuctionIds();			//Create an array containing all of the auction ID's
 		maximumEntDay();			//Calculate the maximum tickets required each day
 		entTicketPriority();		//Create a list of the order entertainment tickets should be allocated in
 		createClientEntArray(); 	//Create a blank array with client details
 		allocateStartingTickets();
 		
 		calculateAllocation();
 		sendBids();
 	}
 
 	public void gameStopped() {
 		log.fine("Game Stopped!");
 	}
 
 	public void auctionClosed(int auction) {
 		log.fine("*** Auction " + auction + " closed!");
 	}
 
 	private void sendBids() {
 		for (int i = 0, n = agent.getAuctionNo(); i < n; i++) {
 			int alloc = agent.getAllocation(i) - agent.getOwn(i);
 			float price = -1f;
 			switch (agent.getAuctionCategory(i)) {
 			case TACAgent.CAT_FLIGHT:
 				if (alloc > 0) {
 					price = 1000;
 				}
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
 			if (price > 0) {
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
 		for (int i = 0; i < 8; i++) {
 			int inFlight = agent.getClientPreference(i, TACAgent.ARRIVAL);
 			int outFlight = agent.getClientPreference(i, TACAgent.DEPARTURE);
 			int hotel = agent.getClientPreference(i, TACAgent.HOTEL_VALUE);
 			int type;
 
 			// Get the flight preferences auction and remember that we are
 			// going to buy tickets for these days. (inflight=1, outflight=0)
 			int auction = agent.getAuctionFor(TACAgent.CAT_FLIGHT,
 					TACAgent.TYPE_INFLIGHT, inFlight);
 			agent.setAllocation(auction, agent.getAllocation(auction) + 1);
 			auction = agent.getAuctionFor(TACAgent.CAT_FLIGHT,
 					TACAgent.TYPE_OUTFLIGHT, outFlight);
 			agent.setAllocation(auction, agent.getAllocation(auction) + 1);
 
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
 	 * Calculates the maximum number of tickets we need each day to entertain all our clients
 	 * @return int[] Array containing the maximum number of tickets we need each day
 	 */
 	private void maximumEntDay(){
 		int[] entPerDay = {0,0,0,0};
 		//Get the days that each client is here
 		for (int i = 0; i < 8; i++) {
 			int inFlight = agent.getClientPreference(i, TACAgent.ARRIVAL);
 			int outFlight = agent.getClientPreference(i, TACAgent.DEPARTURE);
 			//Adjust the inflight and outflight days to match up to the array
 			inFlight = inFlight-1;
 			outFlight = outFlight-1;
 			
 			while(inFlight < outFlight){
 				//Update the array entry with a new holiday day
 				entPerDay[inFlight]= entPerDay[inFlight]+1;
 				inFlight++;
 			}
 		}
 		
 		//For Testing, this will print the number of possible tickets we need each day
 		if(LOG_ENTERTAINMENT){
 			for (int i=0; i< entPerDay.length; i++){
 				int j = i+1;
 				log.finer("We have " + entPerDay[i] + " possible entertainment slots on day " + j);
 			}
 		}
 		
 		//Update global variable
 		maxEntPerDay = entPerDay;
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
 					agent.getClientPreference(client, TACAgent.DEPARTURE)-agent.getClientPreference(client, TACAgent.ARRIVAL));
 			clientArray.add(clientAllocation);
 		}
 		clientEntAvail = clientArray;
 	}
 	
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
						if(allocateTicket(d, e+1, 0, true)){
 							log.finest("Just allocated eType: " +(e+1)+ " on day " + (d+1));
 							alloc++;
 						}else{
 							//TODO Need to sell the remainder of tickets
 							sellLeftoverTickets(e,d,own-alloc);
 							alloc=own+1;
 						}
 					}
 				}
 			}
 		}
 	}
 	
 	
 	private boolean allocateTicket(int day, int eType, int value, boolean process){
 		for (int i=0; i<24; i++){
			if (LOG_ENTERTAINMENT) {
				log.finest("priority " + i + " day assigned: " + entTicketPriorityList.get(i).getDayAssigned() + " eType: "+ entTicketPriorityList.get(i).geteType() + "normal eType:" + eType);
			}
 			//TODO add statement to process when some clients have already been assigned to
 			if(entTicketPriorityList.get(i).geteType()==eType && entTicketPriorityList.get(i).getDayAssigned()<1){
 				//Check whether the utility gained is greater than the value
 				//This should be the most we'll get, so if utility is less
 				if (value<entTicketPriorityList.get(i).getFunBonus()){
 					//If day during agent's visit
					if (LOG_ENTERTAINMENT) {
						log.finest("raw day " + i + " day arrival: " + (agent.getClientPreference(entTicketPriorityList.get(i).getClient(), TACAgent.ARRIVAL)-1) + " dept day: "+ (agent.getClientPreference(entTicketPriorityList.get(i).getClient(), TACAgent.DEPARTURE)-1));
					}
					if(agent.getClientPreference(entTicketPriorityList.get(i).getClient(), TACAgent.ARRIVAL)-1 < day+1 && 
							day+1 < agent.getClientPreference(entTicketPriorityList.get(i).getClient(), TACAgent.DEPARTURE)){
 						//Check the client still has days left to visit entertainment
 						if(clientEntAvail.get(entTicketPriorityList.get(i).getClient()).getDaysAssigned()<clientEntAvail.get(entTicketPriorityList.get(i).getClient()).getDaysPossible()){
 							//If the entertainment type still hasn't been allocated to the client
							if(clientEntAvail.get(entTicketPriorityList.get(i).getClient()).getEntertainmentAllocation(eType).getAssignedDay()<0){
 								//If the entertainment day hasn't been allocated to any other entertainment types
 								if(clientEntAvail.get(entTicketPriorityList.get(i).getClient()).dayAvailable(day)){
 									//If this call was for information only, we don't want to update our records
 									if(process){
 										updateRecords(day, eType,entTicketPriorityList.get(i).getClient() ,i);
 										return true;
 									}else{
 										return true;
 									}
 								}else{
 									//TODO handle re-allocating entertainment to get best possible fun bonus - recursive
									if (LOG_ENTERTAINMENT) {
										log.finest("assigned to another entertainment");
									}
 								}
 							}
 						}
 					}
 				}else{
 					return false;
 				}
 			}
 		}
 		return false;
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
 	
 	private void sellLeftoverTickets(int eType, int day, int quantity){
 		//TODO this just sets up a basic sale at a solid price for now
 		int auctionId = entAuctionIds[eType][day];
 		
 		Bid bid = new Bid(auctionId);
 		bid.addBidPoint(quantity*-1, 80);
 		if (LOG_ENTERTAINMENT) {
 			log.finest("submitting bid to sell" + quantity + " of eType: " +(eType+1)
 					+ " owning=" + agent.getOwn(auctionId));
 		}
 		agent.submitBid(bid);
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
 
 
 
 	// -------------------------------------------------------------------
 	// Only for backward compability
 	// -------------------------------------------------------------------
 
 	public static void main (String[] args) {
 		TACAgent.main(args);
 	}
 
 } // DummyAgent
