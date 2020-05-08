 package states;
 
 import interfaces.NetworkService;
 import interfaces.WebService;
 
 import java.util.ArrayList;
 
 import org.apache.log4j.Logger;
 
 import webClient.Util;
 import webServer.messages.LaunchOfferRequest;
 import webServer.messages.LaunchOfferResponse;
 
 import data.Message;
 import data.Service;
 import data.Service.Status;
 import data.UserEntry;
 import data.UserEntry.Offer;
 import data.UserProfile;
 
 public class LaunchOfferState extends AbstractState {
 	private static final long	serialVersionUID	= 1L;
 	private static Logger		logger				= Logger.getLogger(LaunchOfferState.class);
 
 	public LaunchOfferState(Service service) {

 	}
 
 	public LaunchOfferState(LaunchOfferState state) {
 		service = state.service;
 	}
 
 	public void executeNet(NetworkService net) {
 		System.out.println("[LaunchOfferState: executeNet] Begin");
 
 		System.out.println("[LaunchOfferState: executeNet] End");
 	}
 
 	public void executeWeb(WebService web) {
 		System.out.println("[LaunchOfferState: executeWeb] Begin");
 		System.out.println("[LaunchOfferState: executeWeb] service.getName(): " + service.getName());
 
 		// TODO
 		LaunchOfferRequest requestObj = new LaunchOfferRequest(web.getUsername(), web.getUserRole(), service);
 		LaunchOfferResponse responseObj = (LaunchOfferResponse) Util.askWebServer(requestObj);
 
 		responseObj.getService().setStatus(Status.ACTIVE);
 
 		System.out.println("[LaunchOfferState: executeWeb] service.getStatus(): "
 				+ responseObj.getService().getStatus());
 
 		web.notifyNetwork(responseObj.getService());
 
 		System.out.println("[LaunchOfferState: executeWeb] End");
 	}
 
 	public void setState(Service service) {
 		this.service = service;
 	}
 
 	public String getName() {
 		return "LaunchOfferState";
 	}
 
 	@Override
 	public ArrayList<Message> asMessages(NetworkService net) {
 		UserProfile profile = net.getUserProfile();
 
 		System.out.println("[LaunchOfferState: asMessages] ");
 		ArrayList<Message> list = new ArrayList<Message>();
 
 		/**
 		 * There is no one who offer this service
 		 */
 		if (service.getUsers() == null) {
 			return new ArrayList<Message>();
 		}
 
 		for (UserEntry user : service.getUsers()) {
 			Message message = new Message();
 			message.setType(data.Message.MessageType.LAUNCH);
 			message.setServiceName(service.getName());
 			message.setUsername(user.getUsername());
 			message.setDestination(user.getUsername());
 			message.setPayload(new UserEntry(profile.getUsername(), profile.getFirstName() + " "
 					+ profile.getLastName(), Offer.NO_OFFER, service.getTime(), service.getPrice()));
 			System.out.println("[LaunchOfferState: asMessages] user.getUsername(): " + user.getUsername());
 			list.add(message);
 		}
 
 		return list;
 	}
 
 	public LaunchOfferState clone() {
 		return new LaunchOfferState(this);
 	}
 }
