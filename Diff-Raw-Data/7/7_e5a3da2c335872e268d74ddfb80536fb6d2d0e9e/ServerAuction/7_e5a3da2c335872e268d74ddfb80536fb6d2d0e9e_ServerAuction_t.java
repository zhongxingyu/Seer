 /*  Copyright (C) 2010 - 2011  Fabian Neundorf, Philip Caroli,
  *  Maximilian Madlung,	Usman Ghani Ahmed, Jeremias Mechler
  *
  *  This program is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  *  
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *  
  *  You should have received a copy of the GNU General Public License
  *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package org.ojim.logic.state;
 
 import java.util.Timer;
 import java.util.TimerTask;
 
 import org.ojim.client.SimpleClient.AuctionState;
 import org.ojim.logic.ServerLogic;
 import org.ojim.logic.rules.GameRules;
 import org.ojim.logic.state.fields.BuyableField;
 import org.ojim.server.OjimServer;
 
 public class ServerAuction extends Auction {
 
 	private GameState state;
	private static int timeDelay = 1000;
 	private ServerLogic logic;
 	
 	private int playerID;
 	private OjimServer server;
 	
 	private GameRules rules;
 
 	private Timer timer;
 
 	public void setReturnParameters(OjimServer server, int playerID) {
 		this.server = server;
 		this.playerID = playerID;
 	}
 	
	public static void setTimeDelay(int delay) {
		timeDelay = delay;
 	}
 	
 	public ServerAuction(GameState state, ServerLogic logic, GameRules rules,
 			BuyableField objective) {
 		super(objective);
 		this.state = state;
 		this.logic = logic;
 		this.rules = rules;
 		this.timer = new Timer();
 
 		// Start ticking
 		timer.schedule(new AuctionTask(this),
 				timeDelay * 3 * this.rules.getActionStartTime(),
 				timeDelay * this.rules.getActionTickTime());
 	}
 
 	public final void informPlayers() {
 		for (Player player : state.getPlayers()) {
 			((ServerPlayer) player).getClient().informAuction(this.getState().value);
 		}
 	}
 
 	@Override
 	public boolean placeBid(Player bidder, int bid) {
 		if (!super.placeBid(bidder, bid)) {
 			return false;
 		}
 
 		// Restart the Timer
 		// timer.cancel();
 		timer.schedule(new AuctionTask(this),
 				timeDelay * 3 * this.rules.getActionTickTime(),
 				timeDelay * this.rules.getActionTickTime());
 
 		// Tell All Players that a new Highest Bid is there
 		this.informPlayers();
 		
 		return true;
 	}
 
 	protected void tick() {
 		switch (this.getState()) {
 		case WAITING :
 			this.setState(AuctionState.FIRST);
 			break;
 		case FIRST :
 			this.setState(AuctionState.SECOND);
 			break;
 		case SECOND :
 			this.setState(AuctionState.THIRD);
 			break;
 		}
 
 		// Call all Players that the next Step has come
 		this.informPlayers();
 
 		// Auction is now over
 		if (this.getState() == AuctionState.THIRD) {
 
 			// End the Auction, stop the Timer
 			this.timer.cancel();
 
 			// No one wanted the Field, see what to do now
 			if (this.getHighestBidder() == null) {
 				this.logic.auctionWithoutResult(this.objective);
 			} else {
 				this.logic.auctionWithResult(this.objective, this.getHighestBidder(),
 						this.getHighestBid());
 			}
 			
 			//Set endTurn so that the Player doesn't need to call endTurn himself
 			if(server != null) {
 				server.endTurn(playerID);
 			}
 		}
 	}
 }
 
 class AuctionTask extends TimerTask {
 
 	private ServerAuction auction;
 
 	protected AuctionTask(ServerAuction auction) {
 		this.auction = auction;
 		System.out.println("Tick!");
 	}
 
 	@Override
 	public void run() {
 		auction.tick();
 		System.out.println("Tock!");
 	}
 
 }
