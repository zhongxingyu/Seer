 package webServer;
 
 import java.io.IOException;
 import java.net.InetSocketAddress;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.util.ArrayList;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 
 import webServer.messages.DropOfferRequest;
 import webServer.messages.GetProfileRequest;
 import webServer.messages.GetProfileResponse;
 import webServer.messages.LaunchOfferRequest;
 import webServer.messages.LaunchOfferResponse;
 import webServer.messages.LoginRequest;
 import webServer.messages.LoginResponse;
 import webServer.messages.LogoutRequest;
 import webServer.messages.OkResponse;
 import webServer.messages.SetProfileRequest;
 import config.WebServiceServerConfig;
 import data.LoginCred;
 import data.Service;
 import data.UserEntry;
 import data.UserProfile;
 import data.UserProfile.UserRole;
 
 /**
  * WebServiceServer module implementation.
  * 
  * @author Paul Vlase <vlase.paul@gmail.com>
  */
 public class WebServerMockup implements Runnable {
 	private ServerSocket									serverSocket;
 
 	private ConcurrentHashMap<String, UserProfile>			users;
 	private ConcurrentHashMap<String, InetSocketAddress>	onlineUsers;
 	private ConcurrentHashMap<String, ArrayList<UserEntry>>	sellers;
 	private ConcurrentHashMap<String, ArrayList<UserEntry>>	buyers;
 
 	private static ExecutorService							pool	= Executors.newCachedThreadPool();
 
 	public WebServerMockup() {
 		users = new ConcurrentHashMap<String, UserProfile>();
 		onlineUsers = new ConcurrentHashMap<String, InetSocketAddress>();
 
 		sellers = new ConcurrentHashMap<String, ArrayList<UserEntry>>();
 		buyers = new ConcurrentHashMap<String, ArrayList<UserEntry>>();
 
 		users.put("pvlase", new UserProfile("pvlase", "Paul", "Vlase", UserRole.BUYER, "parola"));
 		users.put("unix140", new UserProfile("unix140", "Ghennadi", "Procopciuc", UserRole.SELLER, "marmota"));
 	}
 
 	public Object login(LoginRequest req) {
 		System.out.println("[WebServerMockup: login()] Begin");
 		LoginCred cred = req.getLoginCred();
 
 		UserProfile profile = users.get(cred.getUsername());
 		if (profile == null) {
 			System.out.println("[WebServerMockup: login()] Username not found");
 			return null;
 		}
 
 		if (!profile.getPassword().equals(cred.getPassword())) {
 			System.out.println("[WebServerMockup: login()] Wrong username or password");
 			return null;
 		}
 
 		profile.setRole(cred.getRole());
 
 		onlineUsers.put(cred.getUsername(), cred.getAddress());
 		
 		System.out.println("[WebServerMockup: login()] Success");
 		return new LoginResponse(profile);
 	}
 
 	public Object logout(LogoutRequest requestMsg) {
 		onlineUsers.remove(requestMsg.getCred().getUsername());
 		return null;
 	}
 
 	public Object launchOffer(LaunchOfferRequest req) {
 		System.out.println("[WebServerMockup: begin()] Begin");
 
 		Service service = req.getService();
 		ArrayList<UserEntry> userEntries;
 
 		UserEntry userEntry = new UserEntry();
 		userEntry.setName(req.getUsername());
 		userEntry.setAddress(onlineUsers.get(req.getUsername()));
 
 		if (req.getUserRole() == UserRole.BUYER) {
 			userEntries = sellers.get(service.getName());
 
			buyers.putIfAbsent(service.getName(), new ArrayList<UserEntry>());
 			ArrayList<UserEntry> buyersUserEntries = buyers.get(service.getName());
 			buyersUserEntries.add(userEntry);
 
 			buyers.put(service.getName(), buyersUserEntries);
 		} else {
 			userEntries = buyers.get(service.getName());
 
			buyers.putIfAbsent(service.getName(), new ArrayList<UserEntry>());
 			ArrayList<UserEntry> sellersUserEntries = sellers.get(service.getName());
 			sellersUserEntries.add(userEntry);
 
 			sellers.put(service.getName(), sellersUserEntries);
 		}
 
 		service.setUsers(userEntries);
 
 		System.out.println("[WebServerMockup: begin()] End");
 		return new LaunchOfferResponse(service);
 	}
 
 	public Object dropOffer(DropOfferRequest req) {
 		System.out.println("[WebServerMockup: drop()] Begin");
 
 		UserEntry userEntry = new UserEntry();
 		userEntry.setName(req.getUsername());
 		userEntry.setAddress(onlineUsers.get(req.getUsername()));
 
 		if (req.getUserRole() == UserRole.BUYER) {
 			ArrayList<UserEntry> buyersUserEntries = buyers.get(req.getServiceName());
 			buyersUserEntries.remove(userEntry);
 
 			buyers.put(req.getServiceName(), buyersUserEntries);
 		} else {
 			ArrayList<UserEntry> sellersUserEntries = sellers.get(req.getServiceName());
 			sellersUserEntries.remove(userEntry);
 
 			sellers.put(req.getServiceName(), sellersUserEntries);
 		}
 
 		System.out.println("[WebServerMockup: drop()] End");
 		return new OkResponse();
 	}
 
 	public Object getProfile(GetProfileRequest req) {
 		UserProfile profile = users.get(req.getUsername());
 		return new GetProfileResponse(profile);
 	}
 
 	public Object setProfile(SetProfileRequest req) {
 		UserProfile profile = req.getUserProfile();
 
 		users.put(profile.getUsername(), profile);
 		return new OkResponse();
 	}
 
 	@Override
 	public void run() {
 		try {
 			this.serverSocket = new ServerSocket(WebServiceServerConfig.PORT);
 		} catch (IOException e) {
 			System.err.println("Nu pot asculta pe portul: " + WebServiceServerConfig.PORT + ".");
 			return;
 		}
 
 		while (true) {
 			try {
 
 				System.out.println("[WebServiceServerMockup:askWebServer()] Before accept");
 				Socket clientSocket = serverSocket.accept();
 				System.out.println("[WebServiceServerMockup:askWebServer()] Connection accepted");
 
 				pool.execute(new WebWorkerMockup(this, clientSocket));
 			} catch (IOException e) {
 				System.err.println("EROARE: Conectare client");
 			}
 		}
 	}
 
 	public static void main(String args[]) {
 		WebServerMockup server = new WebServerMockup();
 
 		(new Thread(server)).start();
 	}
 }
