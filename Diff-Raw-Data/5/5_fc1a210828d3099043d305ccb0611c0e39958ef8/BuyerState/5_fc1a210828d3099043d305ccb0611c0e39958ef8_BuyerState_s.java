 package app.states;
 
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Vector;
 import java.util.Map.Entry;
 
 import org.apache.log4j.Logger;
 
 import app.Command;
import app.DropOfferReqComm;
 import app.RefuseOfferComm;
 import app.AcceptOfferComm;
 import app.LaunchOfferReqComm;
 import app.Mediator;
 import app.ProductListModel;
 import app.model.Buyer;
 import app.model.Seller;
 import app.model.User;
 
 public class BuyerState extends State {
 	private Logger logger = null;
 
 	public BuyerState(Mediator med) {
 		super(med);
 
 		actions = new Vector<Integer>();
 
 		actions.add(RequestTypes.REQUEST_LAUNCH_OFFER);
 		actions.add(RequestTypes.REQUEST_DROP_OFFER);
 
 		actions.add(RequestTypes.REQUEST_ACCEPT_OFFER);
 		actions.add(RequestTypes.REQUEST_REFUSE_OFFER);
 	}
 
 	@Override
 	public String getListName() {
 		return User.sellerType;
 	}
 
 	@Override
 	public void login(String username, Vector<String> products) {
 		logger = Logger.getLogger(BuyerState.class.getName());
 		user = new Buyer(username, products);
 		logger.debug("Created seller " + username);
 	}
 
 	@Override
 	public boolean logout() {
 		med.sendNotifications(RequestTypes.REQUEST_LOGOUT, getUserName(), "");
 
 		return true;
 	}
 
 	/**
 	 * A Buyer may either launch or drop an offer request.
 	 */
 	@Override
 	public Map<String, Command> getServiceMenuItems(String status) {
 		Map<String, Command> items = null;
 		System.out.println(status);
 		if (status.equals(STATE_INACTIVE)) {
 			items = new HashMap<String, Command>();
 			items.put("Launch offer request", new LaunchOfferReqComm(med));
 		} else if (!status.equals(STATE_OFFERACC)
 				&& !status.equals(STATE_TRANSFERS)
 				&& !status.equals(STATE_TRANSFERP)
 				&& !status.equals(STATE_TRANSFERC)) {
 			items = new HashMap<String, Command>();
			items.put("Drop offer request", new DropOfferReqComm(med));
 		}
 
 		return items;
 	}
 
 	/**
 	 * A Buyer may either accept or refuse an offer.
 	 */
 	@Override
 	public Map<String, Command> getContextualMenuItems(String status) {
 		Map<String, Command> items = null;
 		if (status.equals(STATE_OFFERMADE)) {
 			items = new HashMap<String, Command>();
 			items.put("Accept offer", new AcceptOfferComm(med));
 			items.put("Refuse offer", new RefuseOfferComm(med));
 		}
 
 		return items;
 	}
 
 	/* Update the GUI with received info from network _on the EDT_ */
 	public boolean receiveStatusUpdate(int action, String name, String product,
 			int price) {
 
 		switch (action) {
 		case RequestTypes.REQUEST_LAUNCH_OFFER:
 			med.addUserToList(name, product);
 			break;
 		case RequestTypes.REQUEST_MAKE_OFFER:
 			int minOffer = price;
 			Integer[] offers = med.getOffersList(product);
 			if (offers != null) {
 				for (Integer offer : offers)
 					if (offer != null)
 						minOffer = (minOffer > offer) ? offer : minOffer;
 				if (minOffer > -1 && price < minOffer)
 					/* Notify everyone the best offer. */
 					med.sendNotifications(RequestTypes.REQUEST_MAKE_OFFER,
 							name, product, price);
 			}
 			med.updateStatusList(name, product, price, State.STATE_OFFERMADE);
 			break;
 		case RequestTypes.REQUEST_DROP_AUCTION:
 			med.removeUserFromList(name, product);
 			break;
 		case RequestTypes.REQUEST_INITIAL_TRANSFER:
 			med.initTransfer(name, product, price);
 			break;
 		case RequestTypes.REQUEST_TRANSFER:
 			med.transfer(name, product, price);
 			break;
 		case RequestTypes.REQUEST_LOGOUT:
 			med.removeUserFromList(name);
 			med.forgetRelevantUser(name);
 			break;
 		default:
 			System.out.println("Invalid action: " + action);
 			return false;
 		}
 
 		return true;
 	}
 	
 	public User createUser(String name, String ip, int port, Vector<String> products) {
 		return new Seller(name, ip, port, products);
 	}
 	
 	public Vector<User> computeDestinations(int action, String userName, String product, int price) {
 		Vector<User> destinations = new Vector<User>();
 
 		for (Entry<String, User> entry : med.getRelevantUsers().entrySet()) {
 			User seller = entry.getValue();
 
 			if (!seller.getName().equals(userName)) {
 				destinations.add(seller);
 			}
 		}
 
 		return destinations;
 	}
 
 	public int getColumnCount() {
 		return ProductListModel.OFFER_COL + 1;
 	}
 	public void updateColumns(ProductListModel model) {
 		/* Buyer doesn't have specific columns */
 	}
 
 	/**
 	 * When a Buyer, B, logs out, B drops all active auction, implicitly
 	 * refusing all the offers. If no offer has been made to B for neither of
 	 * the products B made an offer request for, drop all the offer requests. If
 	 * B logs out during a transfer of services, this would fail and would be
 	 * marked correspondingly.
 	 */
 	public boolean allowedLogout() {
 		return true;
 	}
 }
