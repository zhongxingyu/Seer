 package herbstJennrichLehmannRitter.ki;
 
 import herbstJennrichLehmannRitter.engine.Globals;
 import herbstJennrichLehmannRitter.engine.model.Card;
 import herbstJennrichLehmannRitter.engine.model.Data;
 import herbstJennrichLehmannRitter.engine.utils.MagicUtils;
 import herbstJennrichLehmannRitter.server.GameServer;
 import herbstJennrichLehmannRitter.ui.UserInterface;
 
 import java.util.Collection;
 import java.util.concurrent.Semaphore;
 
 public class KI implements UserInterface, Runnable {
 
 	private Thread thread = null;
 	private Data data = null;
 	private final String name;
 	private final GameServer gameServer;
 	private Object mutex = new Object();
 	private Semaphore semaphore = new Semaphore(1);
 	
 	static private KI newKiOnServer(final GameServer gameServer, String name) {
 		final KI ki = new KI(name, gameServer);
 		
 		ki.thread = new Thread(ki);
 		ki.thread.start();
 		
 		return ki;
 	}
 	
 	@Override
 	public void run() {
 		try {
 			this.semaphore.acquire();
 			this.gameServer.register(this);
 			System.out.println(getName() + ": I'm ready!");
			
			synchronized (this.mutex) {
				this.semaphore.release();
				this.mutex.wait();
			}
			
 			while (true) {
 				synchronized (this.mutex) {
 					this.mutex.wait();
 				}
 				runKILogic();
 			}
 		} catch (InterruptedException e) {
 			System.out.println("KI " + getName() + " failed!");
 		}
 	}
 	
 	static public void startKIOnLocal(String name) {
 		newKiOnServer(Globals.getLocalGameServer(), name);
 	}
 	
 	public KI(String name, GameServer gameServer) {
 		this.name = name;
 		this.gameServer = gameServer;
 	}
 	
 	private void runKILogic() {
 		System.out.println(getName() + ": my next turn, now you will die!");
 		
 		Card cardToPlay = null;
 		Card lastCard = null;
 		for (Card card : this.data.getOwnPlayer().getDeck().getAllCards()) {
 			lastCard = card;
 			if (MagicUtils.canPlayerEffortCard(this.data.getOwnPlayer(), card)) {
 				cardToPlay = card;
 				break;
 			}
 		}
 		
 		if (cardToPlay != null) {
 			this.gameServer.playCard(cardToPlay);
 		} else {
 			this.gameServer.discardCard(lastCard);
 		}
 	}
 	
 	@Override
 	public void setData(Data data) {
 		this.data = data;
 	}
 
 	@Override
 	public void nextTurn() {
 		playAnotherCard();
 	}
 
 	@Override
 	public void playAnotherCard() {
 		try {
 			this.semaphore.acquire();
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		}
 		synchronized (this.mutex) {
 			this.mutex.notify();
 		}
 		this.semaphore.release();
 	}
 
 	@Override
 	public void enemeyPlayedCard(Card card) {
 		System.out.println(getName() + ": ahh your card (" + card.getName() + ") won't destroy me!");
 	}
 
 	@Override
 	public void youLost() {
 		System.out.println(getName() + ": you a sooo gay you motherfucker, you cheated you sucker!");
 	}
 
 	@Override
 	public void youWon() {
 		System.out.println(getName() + ": HAHA! you are so weak you looser!");
 	}
 
 	@Override
 	public void abort(String reason) {
 		System.out.println(getName() + ": :( the game was aborted! (Reason: " + reason + ')');
 		this.thread.interrupt();
 		this.thread = null;
 	}
 
 	@Override
 	public String getName() {
 		return this.name;
 	}
 
 	@Override
 	public Collection<String> getCards() {
 		return Globals.getGameCardFactory().getAllPossibleCardNames();
 	}
 
 }
