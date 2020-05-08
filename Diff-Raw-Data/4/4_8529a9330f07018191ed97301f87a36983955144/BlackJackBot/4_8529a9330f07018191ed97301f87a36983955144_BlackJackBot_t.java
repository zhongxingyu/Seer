 /*
  * BlackJackBot.java
  *
  * Copyright (C) 2012 Matthew Khouzam
  * 
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU General Public License
  * as published by the Free Software Foundation; either version 2
  * of the License, or (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
  */
 
 package com.matthew.hookersandblackjack;
 
 import java.util.HashMap;
 import java.util.Random;
 
 import org.jibble.pircbot.PircBot;
 
 import com.matthew.hookersandblackjack.bankUtil.Currency;
 import com.matthew.hookersandblackjack.blackjackutil.Card;
 import com.matthew.hookersandblackjack.blackjackutil.Deck;
 import com.matthew.hookersandblackjack.blackjackutil.Hand;
 import com.matthew.hookersandblackjack.blackjackutil.Player;
 import com.matthew.hookersandblackjack.blackjackutil.Player.status;
 
 public class BlackJackBot extends PircBot {
 
 	Deck d = new Deck();
 
 	HMDB playerDB;
 
 	HashMap<String, Player> playerMap = new HashMap<String, Player>();
 
 	Random rnd = new Random(System.nanoTime());
 
 	public BlackJackBot() {
 		this.setName("BlackJackBot");
 	}
 
 	private void mainGame(String sender, String message) {
 		Player player = playerMap.get(sender);
 		if (player == null) {
 			player = new Player();
 			playerMap.put(sender, player);
 		}
 
 		String msg = message.toLowerCase();
 		if (msg.startsWith("!play"))
 			playStart(sender, player);
 		else if (msg.startsWith("!bet"))
 			bet(sender, player, msg);
 		else if (msg.startsWith("!deal"))
 			deal(sender, player);
 		else if (msg.startsWith("!hit"))
 			hit(sender, player);
 		else if (msg.startsWith("!stand"))
 			stand(sender, player);
 		else if (msg.startsWith("!balance"))
 			balance(sender);
 		else if (msg.startsWith("!double"))
 			doubleDown(sender, player);
 		else if (msg.startsWith("!help"))
 			help(sender);
 	}
 
 	private void balance(String sender) {
 		{
 			Currency money = new Currency(playerDB.get(sender));
 			sendMessage(sender, "You have " + money.toString());
 		}
 	}
 
 	private void bet(String sender, Player player, String msg) {
 		{
 			Long money = playerDB.get(sender);
 			Long bet = Long.decode(msg.split(" ")[1]);
 
 			if (player.stat.equals(status.notStarted)
 					|| player.stat.equals(status.started)) {
 				if (money <= 0)
 					reply(sender, "please bet a positive integer");
 				else if (bet <= money) {
 					playerDB.put(sender, money - bet);
 					player.bet = new Currency(bet);
 					player.stat = status.bet;
 					reply(sender, "You've just bet "+player.bet.toString());
 				} else {
 					reply(sender, "You don't have that kind of money");
 				}
 			}
 		}
 	}
 
 	private void deal(String sender, Player player) {
 		if (!player.stat.equals(status.bet)) {
 			reply(sender, "You are either playing or didn't bet yet.");
 		} else {
 
 			player.dealerHand = new Hand();
 			player.playerHand = new Hand();
 			Hand dealerHand = player.dealerHand;
 			Hand playerHand = player.playerHand;
 			dealerHand.hit(d.Deal());
 			dealerHand.hit(d.Deal());
 			playerHand.hit(d.Deal());
 			playerHand.hit(d.Deal());
 			if (dealerHand.getValue() == 21) {
 				if (playerHand.getValue() == 21) {
 					reply(sender, "It's a blackjack push!");
 					playerDB.put(sender,
 							player.bet.getValue() + playerDB.get(sender));
 				} else {
 					reply(sender, "Dealer has blackjack!");
 					player.reset();
 				}
 			} else if (playerHand.getValue() == 21) {
 				reply(sender, "Player has blackjack!");
 				playerDB.put(
 						sender,
 						(playerDB.get(sender) + (long) (player.bet.getValue() * 2.5)));
 				player.reset();
 
 			} else {
 				reply(sender, "Dealer has " + dealerHand.peek().toString());
 				reply(sender, "You have " + playerHand.toString());
 				player.stat = status.dealt;
 			}
 		}
 	}
 
 	private void hit(String sender, Player player) {
 		{
			if (!canHitOrStand(player)) {
				sendAction(sender, "hits " + sender);
 			} else {
 				Card c = d.Deal();
 				player.playerHand.hit(c);
 				reply(sender,
 						"Given a " + c + " total: "
 								+ player.playerHand.getValue());
 
 				if (player.playerHand.getValue() > 21) {
 					reply(sender, "Busted");
 					player.reset();
 				}
 			}
 		}
 	}
 
 	@Override
 	protected void onMessage(String channel, String sender, String login,
 			String hostname, String message) {
 		super.onMessage(channel, sender, login, hostname, message);
 		mainGame(sender, message);
 	}
 
 	@Override
 	protected void onPrivateMessage(String sender, String login,
 			String hostname, String message) {
 		super.onPrivateMessage(sender, login, hostname, message);
 		mainGame(sender, message);
 	}
 
 	private void doubleDown(String sender, final Player player) {
 		if (player.stat.equals(Player.status.dealt)) {
 			long val = player.bet.getValue();
 			player.bet = new Currency(val * 2);
 			Long cash = playerDB.get(sender);
 			cash -= val;
 			playerDB.put(sender, cash - val);
 			this.hit(sender, player);
 			this.stand(sender, player);
 		}
 	}
 
 	private void help(String sender) {
 		sendMessage(sender, "Type " + 
 				(char) 2 + "!help" + (char) 2 + " to display this message, " + 
 				(char) 2 + "!play" + (char) 2 + " to play, " + 
 				(char) 2 + "!bet <amount>" + (char) 2 + " to bet, " + 
 				(char) 2 + "!deal" + (char) 2 + " to deal, " +
 				(char) 2 + "!double" + (char) 2 + " to double down, " +
 				(char) 2 + "!stand" + (char) 2 + " to stand and " + 
 				(char) 2 + "!balance" + (char) 2 + " to know how much cash you have");
 	}
 
 	private void playStart(String sender, final Player player) {
 		{
 			if (player.stat.equals(status.notStarted)) {
 				this.reply(sender,
 						"Blackjack will start, place a bet (!bet <Amount>)");
 				Long money = playerDB.get(sender);
 				if (money == null) {
 					money = 50L;
 					playerDB.put(sender, money);
 				}
 				player.stat = status.started;
 				balance(sender);
 			} else {
 				this.reply(sender, "You're already playing a game.");
 			}
 		}
 	}
 
 	public void setPlayerDB(HMDB playerDB2) {
 		this.playerDB = playerDB2;
 	}
 
 	private void stand(String sender, Player player) {
 		{
 			if (canHitOrStand(player)) {
 				int dealerValue = player.dealerHand.getValue();
 				reply(sender, "Dealer has " + player.dealerHand);
 				while (dealerValue <= 17) {
 					Card c = d.Deal();
 					reply(sender, "Dealer hits " + c);
 					player.dealerHand.hit(c);
 					dealerValue = player.dealerHand.getValue();
 				}
 				Long money = playerDB.get(sender);
 				int playerValue = player.playerHand.getValue();
 				if (dealerValue > 21) {
 					money += 2 * player.bet.getValue();
 					reply(sender, "Dealer is bust! " + sender + " has won "
 							+ player.bet.toString());
 					playerDB.put(sender, money);
 					player.reset();
 				} else if (dealerValue < playerValue) {
 					money += 2 * player.bet.getValue();
 					reply(sender, "Dealer is bust! " + sender + " has won "
 							+ player.bet.toString());
 					playerDB.put(sender, money);
 					player.reset();
 				} else if (dealerValue == playerValue) {
 					money += player.bet.getValue();
 					reply(sender, "It's a push");
 					playerDB.put(sender, money);
 					player.reset();
 				} else {
 					reply(sender, "Dealer wins");
 					player.reset();
 				}
 			}
 		}
 	}
 
 	private boolean canHitOrStand(Player player) {
 		return player.stat.equals(status.dealt)
 				|| player.stat.equals(status.hit);
 	}
 
 	public void reply(String sender, String message) {
 		sendMessage(this.getChannels()[0], sender + ": " + message);
 	}
 
 }
