 package mediator;
 
 import interfaces.Gui;
 import interfaces.MediatorGui;
 import interfaces.MediatorNetwork;
 import interfaces.MediatorWeb;
 import interfaces.NetworkMediator;
 import interfaces.WebClient;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.net.InetSocketAddress;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Collections;
 import java.util.Date;
 import java.util.Hashtable;
 import java.util.StringTokenizer;
 import java.util.concurrent.ConcurrentHashMap;
 
 import org.apache.log4j.FileAppender;
 import org.apache.log4j.Level;
 import org.apache.log4j.Logger;
 
 import app.Main;
 
 import config.FilesConfig;
 import config.GlobalConfig.ServiceType;
 import data.LoginCred;
 import data.Service;
 import data.UserProfile;
 import data.UserProfile.UserRole;
 
 /**
  * Mediator module implementation.
  * 
  * @author Paul Vlase <vlase.paul@gmail.com>
  */
 public class MediatorImpl implements MediatorGui, MediatorNetwork, MediatorWeb {
 	private static Logger						logger	= Logger.getLogger(MediatorImpl.class);
 
 	private Gui									gui;
 	private NetworkMediator						net;
 	private WebClient							web;
 
 	private LoginCred							cred;
 	private UserProfile							profile;
 
 	private ConcurrentHashMap<String, Service>	offers;
 
 	public MediatorImpl() {
 		// TODO: logger.setLevel(Level.OFF);
 		offers = new ConcurrentHashMap<String, Service>();
 	}
 
 	@Override
 	public void registerGui(Gui gui) {
 		logger.debug("Entered");
 		this.gui = gui;
 	}
 
 	@Override
 	public void registerNetwork(NetworkMediator net) {
 		logger.debug("Entered");
 		this.net = net;
 	}
 
 	@Override
 	public void registerWebClient(WebClient web) {
 		logger.debug("Entered");
 		this.web = web;
 	}
 
 	@Override
 	public void start() {
 		gui.start();
 	}
 
 	/* Metode pentru accesul la cache-ul de servicii. */
 	public void putOffer(Service service) {
 		logger.debug("Entered");
 		offers.put(service.getName(), service);
 	}
 
 	public Service getOffer(String serviceName) {
 		logger.debug("Entered");
 		return offers.get(serviceName);
 	}
 
 	public ConcurrentHashMap<String, Service> getOffers() {
 		logger.debug("Entered");
 		return offers;
 	}
 
 	public void removeOffer(String serviceName) {
 		logger.debug("Entered");
 		offers.remove(serviceName);
 	}
 
 	@Override
 	public boolean logIn(LoginCred cred) {
 		logger.debug("Begin");
 		cred.setAddress(net.getAddress());
 		UserProfile profile = web.logIn(cred);
 
 		if (profile != null) {
 			this.cred = cred;
 			this.profile = profile;
 
 			FileAppender fileAppender = (FileAppender) Logger.getRootLogger().getAppender("F");
 			fileAppender.setFile("logs/pvlase.log");
 			fileAppender.activateOptions();
 
 			// TODO: net.init();
 			net.logIn();
 			logger.debug("End (true)");
 			return true;
 		}
 		logger.debug("End (false)");
 		return false;
 	}
 
 	@Override
 	public void logOut() {
 		logger.debug("Begin");
 
 		web.logOut();
 		net.logOut();
 
 		cred = null;
 		profile = null;
 
 		FileAppender fileAppender = (FileAppender) Logger.getRootLogger().getAppender("F");
 		fileAppender.setFile("logs/default.log");
 		fileAppender.activateOptions();
 
 		logger.debug("End");
 	}
 
 	@Override
 	public UserProfile getUserProfile() {
 		logger.debug("Entered");
 		return profile;
 	}
 
 	@Override
 	public boolean setUserProfile(UserProfile profile) {
 		logger.debug("Begin");
 
 		Boolean bRet = web.setUserProfile(profile);
 
 		logger.debug("End");
 		return bRet;
 	}
 
 	@Override
 	public boolean registerUser(UserProfile profile) {
 		logger.debug("Begin");
 
 		Boolean bRet = web.registerUser(profile);
 
 		logger.debug("End");
 		return bRet;
 	}
 
 	@Override
 	public boolean verifyUsername(String username) {
 		logger.debug("Begin");
 
 		Boolean bRet = web.verifyUsername(username);
 
 		logger.debug("End");
 		return bRet;
 	}
 
 	/* MediatorWeb */
 	@Override
 	public ArrayList<Service> loadOffers() {
 		logger.debug("Begin");
 
		if (profile == null)
 			return null;
 
 		ArrayList<Service> services = web.loadOffers(cred);
 
 		for (Service service : services) {
 			if (service.getUsers() != null) {
 				Collections.sort(service.getUsers());
 			}
 		}
 
 		logger.debug("End");
 	}
 
 	public void launchOffers(ArrayList<Service> services) {
 		logger.debug("Begin");
 
 		for (Integer i = 0; i < services.size(); i++) {
 			Service service = services.get(i);
 
 			service.setLaunchOfferState();
 
 			services.set(i, service);
 		}
 
 		publishServices(services);
 		logger.debug("End");
 	}
 
 	@Override
 	public void changeServiceNotify(Service service) {
 		logger.debug("Begin");
 
 		service.setEnabledState();
 		logger.debug("service: " + service);
 
 		putOffer(service);
 		gui.changeServiceNotify(service);
 
 		logger.debug("End");
 	}
 
 	@Override
 	public void changeProfileNotify(UserProfile profile) {
 		logger.debug("Begin");
 		gui.changeProfileNotify(profile);
 		logger.debug("End");
 	}
 
 	@Override
 	public void publishService(Service service) {
 		logger.debug("Begin");
 
 		web.publishService(service);
 
 		logger.debug("End");
 	}
 
 	@Override
 	public void publishServices(ArrayList<Service> services) {
 		logger.debug("Begin");
 
 		web.publishServices(services);
 
 		logger.debug("End");
 	}
 
 	@Override
 	public InetSocketAddress getNetworkAddress() {
 		logger.debug("Begin");
 
 		InetSocketAddress address = net.getAddress();
 
 		logger.debug("End");
 		return address;
 	}
 
 	@Override
 	public void notifyNetwork(Service service) {
 		logger.debug("Begin");
 		logger.debug("service: " + service);
 
 		net.publishService(service);
 		logger.debug("End");
 	}
 
 	@Override
 	public LoginCred getLoginCred() {
 		logger.debug("Entered");
 		return cred;
 	}
 }
