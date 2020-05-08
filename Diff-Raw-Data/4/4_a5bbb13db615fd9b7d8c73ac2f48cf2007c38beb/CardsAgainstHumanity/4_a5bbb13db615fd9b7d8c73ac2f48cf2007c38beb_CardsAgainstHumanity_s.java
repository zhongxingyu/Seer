 import java.io.BufferedReader;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.Random;
 import java.util.Timer;
 import java.util.TimerTask;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.apache.commons.lang3.StringUtils;
 import org.jibble.pircbot.IrcException;
 import org.jibble.pircbot.NickAlreadyInUseException;
 import org.jibble.pircbot.PircBot;
 
 public class CardsAgainstHumanity extends PircBot {
 	
 	//TODO remember score after leaving
 	//TODO don't let people spam join/leave
 	//TODO Shortcuts
 	
 	//TODO Inactivity timeout
 	//TODO Check scores in game
 	//TODO some sort of bug with !cah drop and causing another pick
 	//TODO Hold drop's until after card is picked, should fix above bug
 	//TODO Opportunity to dump hand after 10 rounds
 	//TODO HOF
 	
 	// \x03#,# \u0003 Colors
 	// \x02    \u0002 Bold
 	
 	enum GameStatus{
 		Idle, //No game is playing
 		WaitingForPlayers, //30 second period where players should join
 		WaitingForCards, //Waiting for all players to play cards
 		ChoosingWinner //Waiting for the czar to pick a winner
 	}
 	
 	final String channel = "#joe.to";
 	private ArrayList<Player> players = new ArrayList<Player>();
 	private HashSet<Player> allPlayers = new HashSet<Player>();
 	//private ArrayList<Player> blacklist = new ArrayList<Player>();
 	private ArrayList<Player> randomPlayers;
 	private ArrayList<String> originalBlackCards = new ArrayList<String>();
 	private ArrayList<String> blackCards = new ArrayList<String>();
 	private ArrayList<String> originalWhiteCards = new ArrayList<String>();
 	private ArrayList<String> whiteCards = new ArrayList<String>();
 	private String blackCard;
 	private Timer timer = new Timer();
 	private GameStatus status = GameStatus.Idle;
 	private Player czar;
 	public int requiredAnswers = 1;
 	
 	public CardsAgainstHumanity() throws Exception {
 		this.setName("CAHbot");
 		
 		FileReader f = new FileReader("/home/bender/cahbot/black.txt");
 		BufferedReader br = new BufferedReader(f);
 		String s;
 		while ((s = br.readLine()) != null) {
 			originalBlackCards.add(s);
 		}
 		f.close();
 		br.close();
 		
 		f = new FileReader("/home/bender/cahbot/white.txt");
 		br = new BufferedReader(f);
 		while ((s = br.readLine()) != null) {
 			originalWhiteCards.add(s);
 		}
 		f.close();
 		br.close();
 	}
 	
 	private Player getPlayer(String name) {
 		for (Player p : players) {
 			if (p.getName().equals(name))
 				return p;
 		}
 		return null;
 	}
 	
 	public String getOrdinal(int value) {
 		int hundredRemainder = value % 100;
 		if(hundredRemainder >= 10 && hundredRemainder <= 20) {
 			return "th";
 		}
 		int tenRemainder = value % 10;
 		switch (tenRemainder) {
 		case 1:
 			return "st";
 		case 2:
 			return "nd";
 		case 3:
 			return "rd";
 		default:
 			return "th";
 		}
 	}
 
 	public void onMessage(String channel, String sender, String login, String hostname, String message) {
 		Pattern p = Pattern.compile("play ((?:[0-9]+ ?){" + requiredAnswers +"})");
 		Matcher m = p.matcher(message);
 		
 		Pattern p1 = Pattern.compile("pick ([0-9]+)");
 		Matcher m1 = p1.matcher(message);
 		
 		Pattern p2 = Pattern.compile("!cah boot ([a-zA-Z0-9]+)");
 		Matcher m2 = p2.matcher(message);
 		
 		if (!channel.equalsIgnoreCase(this.channel))
 			return;
 		else if (message.equalsIgnoreCase("!cah join"))
 			join(sender);
 		else if (message.equalsIgnoreCase("!cah drop"))
 			drop(sender);
 		else if (message.equalsIgnoreCase("!cah start") && status == GameStatus.Idle)
 			start();
 		else if (message.equalsIgnoreCase("!cah stop"))
 			stop();
 		else if (message.equalsIgnoreCase("cards"))
 			getPlayer(sender).showCardsToPlayer();
 		else if (m.matches() && status == GameStatus.WaitingForCards && !czar.getName().equals(sender))
 			getPlayer(sender).playCard(m.group(1));
 		else if (m1.matches() && status == GameStatus.ChoosingWinner && czar.getName().equals(sender))
 			pickWinner(m1.group(1));
 		else if (message.equalsIgnoreCase("turn"))
 			nag(sender);
 		else if (message.equalsIgnoreCase("check"))
 			checkForPlayedCards();
 		else if (m2.matches()) {
 			drop(m2.group(1));
 		}
 	}
 	
 	public void onNickChange(String oldNick, String login, String hostname, String newNick) {
 	    if(getPlayer(oldNick) != null){
 	        getPlayer(oldNick).setName(newNick);
 	    }
 	}
 	
 	public void onPart(String channel, String sender, String login, String hostname) {
 		drop(sender);
 	}
 	
 	public void onKick(String channel, String kickerNick, String kickerLogin, String kickerHostname, String recipientNick, String reason) {
 		drop(recipientNick);
 	}
 	
 	public void onQuit(String sourceNick, String sourceLogin, String sourceHostname, String reason) {
 		if (!sourceNick.equals(this.getNick()))
 			drop(sourceNick);
 		else {
 			try {
 				this.connect("irc.gamesurge.net");
 			} catch (NickAlreadyInUseException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (IrcException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 			this.joinChannel(channel);
 		}
 	}
 	
 	private void join(String name) {
 		if (status == GameStatus.Idle) {
 			this.sendMessage(channel, "There is no game currently playing. Try starting one with !cah start");
 			return;
 		}
 		for (Player p : players) {
 			if (p.getName().equals(name)) {
 				this.sendMessage(channel, name + ": you can't join this game twice!");
 				return;
 			}
 		}
 		/*for (Player p : blacklist) {
 			if (p.getName().equals(name)) {
 				this.sendMessage(channel, name + ": You can't join a game after leaving one");
 				return;
 			}
 		}*/
 		Player p = new Player(name, this);
 		players.add(p);
 		allPlayers.add(p);
 		this.sendMessage(channel, name + " joins this game of Cards Against Humanity!");
 	}
 	
 	private void drop(String name) {
 		/*if (status == GameStatus.Idle) {
 			this.sendMessage(channel, "There is no game currently playing to drop yourself from");
 			return;
 		}*/
 	    if(getPlayer(name) == null){
 	        return;
 	    }
 		Player p;
 		p = getPlayer(name);
 		this.sendMessage(channel, p.getName() + " has left this game of Cards Against Humanity with " + p.getScore() + " points!");
 		players.remove(p);
 		//blacklist.add(p);
 		if (czar.equals(p))
 			newCzar();
 		if (players.size() < 3)
 			stop();
 		else
 			checkForPlayedCards();
 	}
 	
 	private void start() {
 		blackCards = new ArrayList<String>(originalBlackCards);
 		whiteCards = new ArrayList<String>(originalWhiteCards);
 				
 		Collections.shuffle(blackCards);
 		Collections.shuffle(whiteCards);
 		
 		this.sendMessage(channel, "Game begins in 45 seconds. Type !cah join to join the game.");
 		
 		status = GameStatus.WaitingForPlayers;
 		
 		timer.schedule(new TimerTask() {
 			public void run() {
 				sendMessage(channel, "Game starts in 30 seconds");
 			}
 		}, 15000);
 		
 		timer.schedule(new TimerTask() {
 			public void run() {
 				sendMessage(channel, "Game starts in 15 seconds");
 			}
 		}, 30000);
 		timer.schedule(new TimerTask() {
 			public void run() {
 				if (players.size() < 3) {
 					sendMessage(channel, "Not enough players to start a game");
 					players.clear();
 					status = GameStatus.Idle;
 					return;
 				}
 				//Everything here is pre game stuff
 				sendMessage(channel, "Game starting now!");
 				czar = players.get(0);
 				sendMessage(channel, czar.getName() + " is the first czar");
 				blackCard = "\u00030,1" + blackCards.remove(0) + "\u0003";
 				requiredAnswers = StringUtils.countMatches(blackCard, "_");
 				blackCard.replaceAll("_", "<BLANK>");
 				sendMessage(channel, "The first black card is " + blackCard);
 				if (requiredAnswers > 1)
 					sendMessage(channel, "Be sure to play " + requiredAnswers + " white cards this round");
 				status = GameStatus.WaitingForCards;
 			}
 		}, 45000); //45 seconds
 	}
 	
 	private void stop() {
 		status = GameStatus.Idle;
 		czar = null;
 		this.sendMessage(channel, "The game is over!");
 		this.sendMessage(channel, "Scores for this game were:");
 		int winningScore = 0;
 		for (Player p : allPlayers) {
 			this.sendMessage(channel, p.getName() + " " + p.getScore());
 			if (p.getScore() > winningScore)
 				winningScore = p.getScore();
 		}
 		this.sendMessage(channel, "Winners this game are:");
		for (Player p : players) {
 			if (p.getScore() == winningScore)
 				this.sendMessage(channel, p.getName());
 		}
 		allPlayers.clear();
 		players.clear();
 	}
 	
 	private void newCzar() {
 		Player oldCzar = czar;
 		Random playerPicker = new Random();
 		ArrayList<Player> contestants = new ArrayList<Player>(players);
 		contestants.remove(oldCzar);
 		czar = contestants.get(playerPicker.nextInt(contestants.size()));
 		this.sendMessage(channel, czar.getName() + " is the next czar");
 	}
 	
 	private void nextTurn() {
 		newCzar();
 		if (blackCards.size() < 1) {
 			blackCards = new ArrayList<String>(originalBlackCards);
 			Collections.shuffle(blackCards);
 		}
 		blackCard = "\u00030,1" + blackCards.remove(0) + "\u0003";
 		requiredAnswers = StringUtils.countMatches(blackCard, "_");
 		blackCard.replaceAll("_", "<BLANK>");
 		this.sendMessage(channel, "The next black card is " + blackCard);
 		if (requiredAnswers > 1)
 			sendMessage(channel, "Be sure to play " + requiredAnswers + " white cards this round");
 		status = GameStatus.WaitingForCards;
 		for (Player p : players) {
 			p.playedCard = null;
 			p.drawTo10();
 			if (!p.equals(czar))
 				p.showCardsToPlayer();
 		}
 	}
 	
 	public String nextWhiteCard() {
 		if (whiteCards.size() < 1) {
 			whiteCards = new ArrayList<String>(originalWhiteCards);
 			Collections.shuffle(whiteCards);
 		}
 		return whiteCards.remove(0);
 	}
 	
 	public void checkForPlayedCards() {
 		int playedCards = 0;
 		for (Player p : players) {
 			if (p.playedCard != null)
 				playedCards++;
 		}
 		if (playedCards + 1 == players.size()) {
 			this.sendMessage(channel, "All players have played their white cards");
 			this.sendMessage(channel, "The black card is " + blackCard);
 			this.sendMessage(channel, "The white cards are:");
 			playedCards = 0;
 			randomPlayers = new ArrayList<Player>(players);
 			randomPlayers.remove(czar);
 			Collections.shuffle(randomPlayers);
 			for (Player p : randomPlayers) {
 				if (p.equals(czar))
 					continue;
 				playedCards++;
 				this.sendMessage(channel, playedCards + ") " + p.playedCard);
 			}
 			this.sendMessage(channel, czar.getName() + ": Pick the best white card");
 			status = GameStatus.ChoosingWinner;
 		}
 	}
 	
 	private void pickWinner(String winningNumber) {
 		int cardNumber = Integer.parseInt(winningNumber);
 		cardNumber--;
 		Player winningPlayer;
 		try {
 			winningPlayer = randomPlayers.get(cardNumber);
 		} catch (IndexOutOfBoundsException e) {
 			this.sendMessage(channel, czar.getName() + ": You have picked an invalid card, pick again");
 			return;
 		}
 		String winningCard = winningPlayer.playedCard;
 		this.sendMessage(channel, "The winning card is " + winningCard + "played by " + winningPlayer.getName());
 		this.sendMessage(channel, winningPlayer.getName() + " is awarded one point");
 		winningPlayer.addPoint();
 		nextTurn();
 	}
 	
 	private void nag(String sender) {
 		if (status == GameStatus.WaitingForCards) {
 			String missingPlayers = "";
 			for (Player p : players) {
 				if (!p.equals(czar) && p.playedCard == null) {
 					System.out.println(p.getName());
 					missingPlayers += p.getName() + " ";
 				}
 			}
 			this.sendMessage(channel, "Waiting for " + missingPlayers + "to submit cards");
 		} else if (status == GameStatus.ChoosingWinner) {
 			this.sendMessage(channel, "Waiting for " + czar.getName() + " to pick the winning card");
 		}
 	}
 }
