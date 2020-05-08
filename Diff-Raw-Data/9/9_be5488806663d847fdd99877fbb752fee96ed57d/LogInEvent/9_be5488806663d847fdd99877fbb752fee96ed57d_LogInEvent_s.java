 package webServiceClient;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.Map;
 import java.util.Random;
 
 import data.Service;
 import data.UserEntry;
 import data.UserEntry.Offer;
 import interfaces.Command;
 import interfaces.MediatorWeb;
 
 /**
  * Ofertele noi adaugate in urma aparitie unui nou utilizator, sau a activarii
  * de catre un cumparator a unor noi oferte.
  *
  * @author pvlase
  *
  */
 public class LogInEvent implements Command {
 	private MediatorWeb med;
 	private ArrayList<String> usersOnline;
 	
 	private static Date date = new Date();
 	private static Random random = new Random();
 
 	public LogInEvent(MediatorWeb med, ArrayList<String> usersOnline) {
 		this.med = med;
 		this.usersOnline = usersOnline;
 	}
 
 	public void execute() {
 		String username = Util.getRandomString(5 + random.nextInt(15));
 
 		for (Map.Entry<String, Service> offer : med.getOffers().entrySet()) {
 			Service service = offer.getValue();
 
 			Integer n = random.nextInt(1000);
 
 			if (n % 2 == 0) {
 				Integer delay = 1000;
 				Integer timeLimit = 1000000;
 				Integer priceLimit = 100000;
 
 				Long time = date.getTime() + delay + random.nextInt(timeLimit);
 				Double price = random.nextInt(priceLimit) / 100.0;
 
				UserEntry user = new UserEntry(username, Offer.NO_OFFER, time, price);
 
 				service.addUserEntry(user);
 				med.changeServiceNotify(service);
 			}
 		}
 		
 		usersOnline.add(username);
 	}
 }
