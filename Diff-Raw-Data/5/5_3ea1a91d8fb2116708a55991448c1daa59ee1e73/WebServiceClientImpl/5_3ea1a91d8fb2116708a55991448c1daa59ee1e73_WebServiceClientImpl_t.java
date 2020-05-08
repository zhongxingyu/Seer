 package webserviceclient;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Random;
 import java.util.Set;
 
 import mediator.MediatorWebServiceClient;
 import model.service.Service;
 import model.service.ServiceImpl;
 import model.user.Buyer;
 import model.user.Manufacturer;
 import model.user.User;
 
 /**
  * Implements {@link WebServiceClient}.
  *
  * @author cmihail, radu-tutueanu
  */
 public class WebServiceClientImpl implements WebServiceClient {
 
 	private final MediatorWebServiceClient mediator;
 
 	public WebServiceClientImpl(MediatorWebServiceClient mediator) {
 		this.mediator = mediator;
 	}
 
 	@Override
 	public Map<Service, Set<User>> login(User user, String password) {
 		// TODO login to WebService
 
 		try {
 			Thread.sleep(500); // TODO delete (only for testing)
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 			System.exit(1);
 		}
 
 		// Create mockup data.
 		Map<Service, Set<User>> mapServiceUsers = new HashMap<Service, Set<User>>();
 		Random random = new Random();
 
 		// Randomize users.
 		Set<User> users = new HashSet<User>();
 		int numOfUsers = random.nextInt(user.getServices().size()) + 2; // minimum 2 users
 		for (int i = 0; i < numOfUsers; i++) {
 			int numOfServices = random.nextInt(user.getServices().size() - 1);
 			numOfServices++; // minimum 1 service
 			int order = random.nextInt(2), limit = 0;
 			if (order == 0)
 				limit = user.getServices().size() - 1;
 
 			List<Service> services = new ArrayList<Service>();
 			for (int j = 0; j < numOfServices; j++) {
 				int index = Math.abs(j - limit);
 				services.add(new ServiceImpl(user.getServices().get(index).getName()));
 			}
 
 			switch (user.getType()) {
 			case BUYER:
				users.add(new Manufacturer("Username " + i, services));
 				break;
 			case MANUFACTURER:
				users.add(new Buyer("Username " + i, services));
 				break;
 			}
 		}
 
 		Iterator<Service> it = user.getServices().iterator();
 		while (it.hasNext()) {
 			Service service = it.next();
 			Set<User> serviceUsers = new HashSet<User>();
 
 			Iterator<User> usersIt = users.iterator();
 			while (usersIt.hasNext()) {
 				User u = usersIt.next();
 
 				Iterator<Service> serviceIt = u.getServices().iterator();
 				while (serviceIt.hasNext()) {
 					Service s = serviceIt.next();
 
 					if (service.getName().equals(s.getName())) {
 						serviceUsers.add(u);
 					}
 				}
 			}
 
 			mapServiceUsers.put(service, serviceUsers);
 		}
 
 		return mapServiceUsers;
 	}
 
 	@Override
 	public void logout(User user) {
 		// TODO logout user from WebService
 	}
 }
