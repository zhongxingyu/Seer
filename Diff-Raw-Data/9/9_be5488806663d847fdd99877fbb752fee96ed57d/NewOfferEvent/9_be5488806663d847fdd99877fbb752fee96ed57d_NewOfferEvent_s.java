 package webServiceClient;
 
 import interfaces.Command;
 import interfaces.MediatorWeb;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.Map;
 import java.util.Random;
 
 import data.Service;
 import data.UserEntry;
 import data.UserEntry.Offer;
 
 public class NewOfferEvent implements Command {
 	private MediatorWeb			med;
 	private ArrayList<String>	usersOnline;
 
 	private static Date			date	= new Date();
 	private static Random		random	= new Random();
 
 	public NewOfferEvent(MediatorWeb med, ArrayList<String> usersOnline) {
 		this.med = med;
 		this.usersOnline = usersOnline;
 	}
 
 	public void execute() {
 		if (usersOnline.size() == 0) {
 			return;
 		}
 
 		Integer index = random.nextInt(usersOnline.size());
 		String username = usersOnline.get(index);
 
 		for (Map.Entry<String, Service> offer : med.getOffers().entrySet()) {
 			Service service = offer.getValue();
 			ArrayList<UserEntry> users = service.getUsers();
 
 			if (users == null) {
 				continue;
 			}
 
 			Boolean skip = false;
 			for (UserEntry userEntry : users) {
 				if (userEntry.getName() == username) {
 					skip = true;
 					break;
 				}
 			}
 
 			if (!skip) {
 				Integer n = random.nextInt(1000);
 
 				if (n % 2 == 0) {
 					Integer delay = 1000;
 					Integer timeLimit = 1000000;
 					Integer priceLimit = 100000;
 
 					Long time = date.getTime() + delay
 							+ random.nextInt(timeLimit);
 					Double price = random.nextInt(priceLimit) / 100.0;
 
					UserEntry userEntry = new UserEntry(username,
 							Offer.NO_OFFER, time, price);
 
 					service.addUserEntry(userEntry);
 					med.changeServiceNotify(service);
 				}
 			}
 		}
 	}
 }
