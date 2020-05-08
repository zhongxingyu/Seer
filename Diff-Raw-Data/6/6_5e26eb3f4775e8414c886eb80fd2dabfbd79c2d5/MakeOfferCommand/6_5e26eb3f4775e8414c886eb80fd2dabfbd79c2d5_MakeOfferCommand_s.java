 package AuctionHouse.Commands;
 
 import java.util.Vector;
 
 import javax.swing.JTable;
 
 import AuctionHouse.DataContext.Service;
 import AuctionHouse.DataContext.ServiceEntry;
 import AuctionHouse.GUI.AHTableModel;
 import AuctionHouse.GUI.ControllerMediator;
 import AuctionHouse.DataContext.DataManager;
 import AuctionHouse.Network.NetworkCommunicator;
 import AuctionHouse.NetworkMessages.MakeOfferNetworkMessage;
 import AuctionHouse.NetworkMessages.NetworkMessage;
 
 /*
  * Class that incapsulates de Make Offer command from the GUI
  * It is also used to notify the GUI when an offer is received through the network 
  * module
  */
 public class MakeOfferCommand implements Command {
 	private DataManager dataManager;
 	private String service;
 	private String buyer;
 	private String offer;
 	private ControllerMediator controllerMediator;
 	private NetworkCommunicator communicator;
 	private boolean sendToNetwork;
 	
 	public MakeOfferCommand(String service, String buyer, String offer,
 			ControllerMediator controllerMediator, DataManager dataManager,
 			NetworkCommunicator communicator) {
 		this.service = service;
 		this.buyer = buyer;
 		this.offer = offer;
 		this.controllerMediator = controllerMediator;
 		this.dataManager = dataManager;
 		this.communicator = communicator;
 		this.sendToNetwork = true;
 	}
 	
 	public MakeOfferCommand(String service, String buyer, String offer,
 			ControllerMediator controllerMediator, DataManager dataManager,
 			NetworkCommunicator communicator, boolean sendToNetwork) {
 		this.service = service;
 		this.buyer = buyer;
 		this.offer = offer;
 		this.controllerMediator = controllerMediator;
 		this.dataManager = dataManager;
 		this.communicator = communicator;
 		this.sendToNetwork = sendToNetwork;
 	}
 	
 	@Override
 	public Object run() {
 		AHTableModel model = controllerMediator.getModel();
 		Service s = dataManager.getService(service);
 		ServiceEntry se = s.getEntry(buyer);
 		
 		if (se.getState() != ServiceEntry.State.NO_OFFER
				&& se.getState() != ServiceEntry.State.OFFER_EXCEED){
 			System.out.println("ServiceEntry [" + se.getPerson() + "] isn't NO_OFFER, nor OFFER_EXCEED: " + se.getState() + ", " + se.getStatus());
 			return false;
 		}
 		
 		AHTableModel embeddedModel = model.getInnerTableModel(se.getService().getName());
 		int offerRow = embeddedModel.getInnerPersonRowNr(se.getPerson());
 		
 		embeddedModel.setValueAt("Offer Made", offerRow, 1);
		embeddedModel.setValueAt(se.getOffer(), offerRow, 2);
 		
 		se.setOffer(offer);
 		se.setState(ServiceEntry.State.OFFER_MADE);
 		
 		/*
 		 * Use the network module to send the offer
 		 */
 		if (sendToNetwork){
 			NetworkMessage netMsg = new MakeOfferNetworkMessage(service, buyer, offer);
 			netMsg.setDestinationPerson(buyer);
 			communicator.sendMessage(netMsg);
 		}
 		
 		return true;
 	}
 
 }
