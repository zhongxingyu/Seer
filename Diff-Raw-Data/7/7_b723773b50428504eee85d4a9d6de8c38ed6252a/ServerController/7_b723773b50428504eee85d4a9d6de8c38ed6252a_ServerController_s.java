 package Server.Application;
 
 
 import java.io.UnsupportedEncodingException;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.TreeMap;
 import java.util.concurrent.Semaphore;
 
 import NetworkCommunication.BroadcastCompanyResultMessage;
 import NetworkCommunication.ChatMessageToClient;
 import NetworkCommunication.ChatMessageToServer;
 import NetworkCommunication.SendAssignedDisposalMessage;
 import NetworkCommunication.SendCompanyResultMessage;
 import NetworkCommunication.SendSupplyMessage;
 import Server.Entities.Disposal;
 import Server.Entities.PeriodBuffer;
 import Server.Network.Server;
 import Server.Network.ClientHandler;
 import common.entities.CompanyResult;
 import common.entities.CompanyResultList;
 import common.entities.Supply;
 
 
 
 public class ServerController {
 	
 	
 	public static Server StartServer(int port, int maxPeriods) {
 		return Server.StartServer(port, maxPeriods);
 	}
 	
 	// Spiel Starten.
 	public static void StartGame() {
 		Server server = Server.getInstance();
 		int countPlayer = server.getClients().size();
 		AppContext.standardDemand = countPlayer * AppContext.STANDARD_DEMAND_PER_PLAYER;
 		server.startGame();
 	}
 	
 	// BEGIN OF Chatnachricht weiterleiten.
 	
 	public static void redirectChatMessage(ClientHandler sender, ChatMessageToServer message) {
 		System.out.println("Chatnachricht gesendet von Client " + sender.get_ID());
 		
 		ChatMessageToClient sendMessage = null;
 		try {
 			sendMessage = new ChatMessageToClient(message.get_Message(), sender.get_Name());
 		} catch (UnsupportedEncodingException e1) {
 			// should never reach this point.
 		}
 		
 		List<ClientHandler> clients = Server.getInstance().getClients();
 		for (ClientHandler client : clients) {
 			try {
 				client.SendMessage(sendMessage);
 			} catch (Exception e) { }
 		}
 	}
 	
 	// END OF Chatnachricht weiterleiten.
 	// -----------------------------------------
 	// BEGIN OF Perdiodenabschluss.
 	
 	private static Semaphore lockSupplies = new Semaphore(1);
 	
 	private static Map<Integer, Supply> supplies = new TreeMap<Integer, Supply>();
 	
 	public static void receiveSupply(ClientHandler Sender, SendSupplyMessage Message) {		
 		lockSupplies.acquireUninterruptibly();
 		try {
 			supplies.put(Sender.get_ID(), Message.getSupply());
 		} catch (Exception e) {
 		} finally {
 			lockSupplies.release();
 		}
 		checkSupplies();
 	}
 	
 	/**
 	 * Prft, ob alle Spieler ihre Angebote abgegeben haben, fhrt die Verteilung aus und bermittelt die zugewiesene Nachfrage an die Spieler.
 	 */
 	public static void checkSupplies() {
 		List<ClientHandler> clients = Server.getInstance().getClients();		
 		if (clients.isEmpty()) return;
 		
 		Map<Integer, Disposal> disposals = null;
 		lockSupplies.acquireUninterruptibly();
 		try {
 			for (ClientHandler client : clients) {
 				if (!supplies.containsKey(client.get_ID())) {
 					return;
 				}
 			}
 			// alle Angebote wurden abgebgeben. Fhre demandFunction aus.
 			disposals = demandFunction(supplies);
 		
 			int quantity;
 			Disposal disposal;
 			for (ClientHandler client : clients) {
 				try {
 					disposal = disposals.get(client.get_ID());
 					quantity = disposal.quantity;
 					PeriodBuffer.Disposals.put(client.get_ID(), disposal);
 					client.SendMessage(new SendAssignedDisposalMessage(quantity));
 				} catch (Exception e) { }
 			}
 			// Abstze wurden an Clients verschickt, Angebote werden zurckgesetzt.
 			supplies.clear();
 			
 		} catch (Exception e) {
 		} finally {
 			lockSupplies.release();
 		}
 	}
 	
 	/**
 	 * determines contingents of total demand for all clients.
 	 * @param Supplies: Map of Supplies for all Client(ID)companyResult.
 	 * @return Contingents: Map of Contingents for all Client(ID)companyResult.
 	 */
 	public static Map<Integer, Disposal> demandFunction(Map<Integer, Supply> Supplies) {
 		
 		// create a new map to keep input list unchanged -> faked call-by-value.
 		Map<Integer, Supply> supplies = new TreeMap<Integer, Supply>();
 		supplies.putAll(Supplies);
 		
 		int leftDemand = AppContext.standardDemand;
 		
 		for (Supply sup : supplies.values()) {
 			double perc = sup.price / AppContext.STANDARD_PRICE_PER_UNIT;
 			int diff = (int) (AppContext.STANDARD_DEMAND_PER_PLAYER * ( 1 - ( 1 / perc ) ));
 			leftDemand -= diff;
 			}
 		
 		// initialize assignedDemand
 		Map<Integer, Disposal> disposals  = new TreeMap<Integer, Disposal>();
 		for (Entry<Integer, Supply> sup : supplies.entrySet()) {
 			disposals.put(sup.getKey(), new Disposal(sup.getKey(), 0, sup.getValue().price)); 
 		}
 		
 		while (leftDemand > 0 && supplies.size() > 0) {
 			// calculate average price
 			double averagePrice = 0d;
 			for (Entry<Integer, Supply> sup : supplies.entrySet()) {
 				averagePrice += sup.getValue().price;							
 			}
 			averagePrice /= supplies.size();
 			
 			Map<Integer, Double> factors = new TreeMap<Integer, Double>();
 			double factorsSum = 0d;
 			// calculate factor: Relation between average price and supplied price, powered by 1,5.
 			for (Entry<Integer, Supply> sup : supplies.entrySet()) {
 				Double factor = Math.pow(averagePrice / sup.getValue().price, 1.5);
 				factors.put(sup.getKey(), factor);
 				factorsSum += factor;
 			}
 			
 			// calculate contingents by factors.
 			Map<Integer, Integer> contingents = new TreeMap<Integer, Integer>();
 			for (Entry<Integer, Double> factor : factors.entrySet()) {
 				//Integer contingent = (int) Math.round( leftDemand * factor.getValue() / factorsSum );
 				Integer contingent = (int) Math.ceil( leftDemand * factor.getValue() / factorsSum );
 				contingents.put(factor.getKey(), contingent);
 			}
 			
 			List<Integer> removedKeys = new LinkedList<Integer>();
 			
 			// split demand to players.
 			for (Integer key : supplies.keySet()) {
 				Integer availableDemand = contingents.get(key);
 				if (supplies.get(key).quantity <= availableDemand) {
 					// player wants to sell less-equal than he is able to.
 					Disposal disposal = (Disposal)disposals.get(key);					
 					disposal.quantity += supplies.get(key).quantity;
 					
 					removedKeys.add(key);
 					
 					leftDemand -= supplies.get(key).quantity;
 				} else {
 					// player wants to sell more than he is able to.
 					Disposal disposal = (Disposal)disposals.get(key);	
 					disposal.quantity += availableDemand;
 					
 					Supply sup = supplies.get(key);
 					sup.quantity -= availableDemand;
 					
 					leftDemand -= availableDemand;
 				}
 			}
 			
 			for (Integer key : removedKeys) {
 				supplies.remove(key);
 			}
 		
 		}
 		
 		return disposals;
 	}
 
 	private static Semaphore profitlocker = new Semaphore(1);
 	private static CompanyResultList profits = new CompanyResultList();
 	public static void collectResults(ClientHandler sender,
 			SendCompanyResultMessage Message) {
 		double singleResult = Message.getProfit();
 		profitlocker.acquireUninterruptibly();
 			try{
 				CompanyResult result = new CompanyResult(singleResult, sender.get_ID());
 				Disposal singleDisposal = PeriodBuffer.Disposals.get(sender.get_ID());
 				result.sales = singleDisposal.price * singleDisposal.quantity;
				result.marketShare = (PeriodBuffer.getTotalDisposal() == 0) ? 0 : singleDisposal.quantity / PeriodBuffer.getTotalDisposal();

 				profits.profitList.put(sender.get_ID(),result);
 			}
 			finally
 			{
 				profitlocker.release();
 			}
 			
 		checkResultsCollected();
 	}
 
 	public static void checkResultsCollected() {
 		List<ClientHandler> clients = Server.getInstance().getClients();		
 		if (clients.isEmpty()) return;
 		
 		profitlocker.acquireUninterruptibly();
 		try {
 			for (ClientHandler client : clients) {
 				if (!profits.profitList.containsKey(client.get_ID())) {
 					return;
 				}
 			}
 			
 			//Dieser Code wird hoffentlich nur erreicht, wenn alle Clients abgegeben haben
 			for (ClientHandler client : clients) {
 				try {
 					client.SendMessage(new BroadcastCompanyResultMessage(profits));
 				} catch (Exception e) { }
 			}
 			//Ergebnisse wurden verschickt. Alte Ergebnisse lschen.
 			profits.profitList.clear();
 			
 		} catch (Exception e) {
 		} finally {
 			profitlocker.release();
 		}
 	}
 	
 	// END OF Pediodenabschluss.
 }
