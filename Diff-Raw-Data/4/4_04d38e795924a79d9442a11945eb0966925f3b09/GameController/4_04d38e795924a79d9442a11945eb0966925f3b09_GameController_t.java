 /**
  Copyright (c) 2013 Karl Engstr�m, Sebastian Ivarsson, Jacob Lundberg, Joakim Karlsson, Alexander Persson and Fredrik Westling
  */
 
 /**
  This file is part of TouchDeck.
 
  TouchDeck is free software: you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation, either version 2 of the License, or
  (at your option) any later version.
 
  TouchDeck is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.
 
  You should have received a copy of the GNU General Public License
  along with TouchDeck.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package se.chalmers.touchdeck.gamecontroller;
 
 import java.io.IOException;
 import java.io.ObjectOutputStream;
 import java.net.Socket;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.LinkedList;
 
 import se.chalmers.touchdeck.enums.Rank;
 import se.chalmers.touchdeck.enums.Suit;
 import se.chalmers.touchdeck.models.Card;
 import se.chalmers.touchdeck.models.Pile;
 import se.chalmers.touchdeck.network.GameListener;
 import se.chalmers.touchdeck.network.GameToGuiConnection;
import se.chalmers.touchdeck.network.IpFinder;
 import android.util.Log;
 
 /**
  * Controls the game logic
  * 
  * @author group17
  */
 public class GameController {
 
 	// Public constants
 	public static final int								NUM_ROWS				= 3;
 	public static final int								NUM_COLUMNS				= 8;
 	public static final int								MAX_NUMBER_OF_PILES		= NUM_ROWS * NUM_COLUMNS;
 	public static final int								MID_OF_TABLE			= MAX_NUMBER_OF_PILES / 2 - 1;
 	public static final String							MAIN_DECK_NAME			= "deck";
 
 	private final ArrayList<Pile>						mTable					= new ArrayList<Pile>();
 	private final HashSet<String>						mPileNames				= new HashSet<String>();
 
 	private final GameState								mGameState;
 	private final int									mPort					= 4243;
 	private final HashMap<String, GameToGuiConnection>	mGameToGuiThreads		= new HashMap<String, GameToGuiConnection>();
 	private final LinkedList<Socket>					mAllGameToGuiSockets	= new LinkedList<Socket>();
 	private final GameListener							mGameListener;
 
 	private int											mDefaultPileNameNo		= 1;
 
 	/**
 	 * Creates a new gameController and sets up a deck.
 	 */
 	public GameController() {
 		// Fill the table empty positions.
 		for (int i = 0; i < MAX_NUMBER_OF_PILES; i++) {
 			mTable.add(i, null);
 		}
 		createDeck();
 		mGameState = new GameState(mTable, mPileNames);
 
 		// Start the listener for incoming connections
 		mGameListener = new GameListener(this, 4242);
 		new Thread(mGameListener).start();
 	}
 
 	/**
 	 * @param socket The socket to add as the connection to the guiController
 	 */
 	public void addSocket(Socket socket) {
 		Log.d("network GaC", "socket added to list " + socket.getRemoteSocketAddress().toString());
 		mAllGameToGuiSockets.add(socket);
 	}
 
 	public void removeSocket(Socket socket) {
 		Log.e("In GaC", "Socket removed from list" + socket.getRemoteSocketAddress().toString());
 		mAllGameToGuiSockets.remove(socket);
 	}
 
 	/**
 	 * Sends the gameState to all the guis
 	 */
 	public void sendUpdatedState() {
 		ObjectOutputStream out = null;
 		Log.e("in GaC, sendUpdatedState ", "Sockets left: " + mAllGameToGuiSockets.size());
 
 		for (Socket socket : mAllGameToGuiSockets) {
 			try {
 				out = new ObjectOutputStream(socket.getOutputStream());
 				out.writeObject(mGameState);
 				Log.d("sendUpdated GaC", "State written into socket " + socket.getRemoteSocketAddress().toString() + "host still left: "
 						+ mGameState.getHostStillLeft());
 				out.flush();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 
 	}
 
 	/**
 	 * Creates a standard 52-card deck
 	 * 
 	 * @return A pile containing the deck
 	 */
 	private Pile createDeck() {
 		Pile deck = new Pile(MAIN_DECK_NAME);
 		mPileNames.add(MAIN_DECK_NAME);
 		for (Suit suit : Suit.values()) {
 			for (Rank rank : Rank.values()) {
 				deck.addCard(new Card(suit, rank));
 			}
 		}
 		// Put the deck at the middle of the table
 		mTable.set(MID_OF_TABLE, deck);
 		return deck;
 	}
 
 	/**
 	 * Performs the given operation and sends out the updated state to all guis
 	 * 
 	 * @param op
 	 */
 	public synchronized void performOperation(Operation op) {
 		String ipAddr = op.getIpAddr();
 		Integer pilePosition = op.getPile1();
 		if (pilePosition != null) {
 			Pile p = mTable.get(pilePosition);
 			if (p != null) {
 				if (!p.getOwner().equals("noOwner") && !p.getOwner().equals(ipAddr)) {
 					return; // The user was not allowed to perform the operation
 				}
 			}
 		}
 
 		switch (op.getOp()) {
 
 		case move:
 			Pile srcPile = mTable.get(op.getPile1());
 			Pile destPile = mTable.get(op.getPile2());
 			if (destPile != null && srcPile != null) {
 				Card cardToMove = op.getCard();
 				for (int i = 0; i < srcPile.getSize(); i++) {
 					Card card = srcPile.getCard(i);
 					if (card.equals(cardToMove)) {
 						srcPile.takeCard(i);
 						destPile.addCard(card);
 						sendUpdatedState();
 						return;
 					}
 				}
 			}
 
 			Log.d("handle GaC", "move");
 			break;
 
 		case flip:
 			Card cardToFlip = op.getCard();
 			Pile currentPile = mTable.get(op.getPile1());
 			for (int i = 0; i < currentPile.getSize(); i++) {
 				Card c = currentPile.getCard(i);
 				if (c.equals(cardToFlip)) {
 					c.flipFace();
 					sendUpdatedState();
 					Log.d("handle GaC", "flip");
 					return;
 				}
 			}
 			break;
 
 		case create:
 			int pilePos = op.getPile1();
 			String name = op.getName();
 			if (mTable.get(pilePos) != null) {
 				return; // There was already a pile there
 			}
 			if (mPileNames.contains(name)) {
 				return;
 			} else if (name.equals("Pile " + mDefaultPileNameNo)) {
 				mDefaultPileNameNo++;
 				mGameState.setDefaultPileNo(mDefaultPileNameNo);
 			}
 
 			mTable.set(pilePos, new Pile(name));
 			mPileNames.add(name);
 			sendUpdatedState();
 			Log.d("handle GaC", "create");
 			break;
 
 		case connect:
 			GameToGuiConnection connection = new GameToGuiConnection(op.getIpAddr(), mPort, this);
 			new Thread(connection).start();
 			mGameToGuiThreads.put(op.getIpAddr(), connection);
 			Log.d("handle GaC", "connected : " + op.getIpAddr());
 			break;
 
 		case shuffle:
 			Pile pileToShuffle = mTable.get(op.getPile1());
 			if (pileToShuffle != null) {
 				pileToShuffle.shuffle();
 				sendUpdatedState();
 			}
 			Log.d("handle GaC", "shuffle");
 			break;
 
 		case delete:
 			int pilePosToDelete = op.getPile1();
 			if (mTable.get(pilePosToDelete) != null && mTable.get(pilePosToDelete).getSize() == 0) {
 				mPileNames.remove(mTable.get(pilePosToDelete).getName());
 				mTable.set(pilePosToDelete, null);
 				sendUpdatedState();
 			}
 			break;
 
 		case rename:
 			Pile pileToRename = mTable.get(op.getPile1());
 			if (pileToRename == null) {
 				return;
 			}
 			String oldName = pileToRename.getName();
 			if (mPileNames.contains(op.getName())) {
 				return;
 			} else if (op.getName().equals("Pile " + mDefaultPileNameNo)) {
 				mDefaultPileNameNo++;
 				mGameState.setDefaultPileNo(mDefaultPileNameNo);
 			}
 
 			pileToRename.setName(op.getName());
 			mPileNames.add(op.getName());
 			mPileNames.remove(oldName);
 			sendUpdatedState();
 			break;
 
 		case faceUp:
 			Pile pileToFaceUp = mTable.get(op.getPile1());
 			if (pileToFaceUp != null) {
 				for (Card p : pileToFaceUp.getCards()) {
 					p.setFaceUp();
 				}
 				sendUpdatedState();
 			}
 			break;
 
 		case faceDown:
 			Pile pileToFaceDown = mTable.get(op.getPile1());
 			if (pileToFaceDown != null) {
 				for (Card p : pileToFaceDown.getCards()) {
 					p.setFaceDown();
 				}
 				sendUpdatedState();
 			}
 			break;
 
 		case moveAll:
 			Pile fromPile = mTable.get(op.getPile1());
 			Pile toPile = mTable.get(op.getPile2());
 			if (fromPile != null && toPile != null) {
 				int totalCards = fromPile.getSize();
 				for (int i = 0; i < totalCards; i++) {
 					Card card = fromPile.takeCard(0);
 					toPile.addCard(card);
 				}
 				sendUpdatedState();
 			}
 			break;
 
 		case protect:
 			Pile pileToProtect = mTable.get(op.getPile1());
 			if (pileToProtect != null) {
 				pileToProtect.setOwner(op.getName());
 				sendUpdatedState();
 			}
 
 			break;
 
 		case unprotect:
 			Pile protectedPile = mTable.get(op.getPile1());
 			if (protectedPile != null && protectedPile.getOwner().equals(op.getName())) {
 				protectedPile.setOwner("noOwner");
 				sendUpdatedState();
 			}
 			break;
 
 		case restart:
 			mTable.clear();
 			for (int i = 0; i < MAX_NUMBER_OF_PILES; i++) {
 				mTable.add(i, null);
 			}
 			mPileNames.clear();
 			createDeck();
 			mDefaultPileNameNo = 1;
 			mGameState.setDefaultPileNo(1);
 			sendUpdatedState();
 			break;
 
 		case pileMove:
 			Pile pileToMove = mTable.get(op.getPile1());
 			Pile destination = mTable.get(op.getPile2());
 			if (pileToMove != null && destination == null) {
 				mTable.set(op.getPile2(), pileToMove);
 				mTable.set(op.getPile1(), destination);
 				sendUpdatedState();
 			}
 			break;
 
 		case disconnect:
 			Log.e("in GaC Disconnect", "hello!");
 			String ipDeviceAddr = op.getIpAddr();
 
 			GameToGuiConnection conn = mGameToGuiThreads.get(ipDeviceAddr);
 			conn.end();
 			mGameToGuiThreads.remove(op.getIpAddr());
 			Log.e("in GaC Disconnect", "GameToGui removed, ip : " + ipDeviceAddr);
 
 			mGameListener.end(op.getIpAddr());
			if (ipDeviceAddr.equals(IpFinder.getMyIp())) {
 				Log.e("host", "Leaving");
 				mGameState.setHostStillLeft(false);
 				sendUpdatedState();
 				mAllGameToGuiSockets.clear();
 			}
 			Log.e("in GaC Disconnect", "GameListener ended");
 			break;
 		default:
 		}
 	}
 
 	/**
 	 * @return the gamestate
 	 */
 	public GameState getGameState() {
 		return mGameState;
 	}
 
 }
