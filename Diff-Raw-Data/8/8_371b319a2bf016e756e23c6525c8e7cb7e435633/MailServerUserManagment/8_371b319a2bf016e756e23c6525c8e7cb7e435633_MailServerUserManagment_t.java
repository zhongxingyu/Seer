 package common;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map.Entry;
 import java.util.Properties;
 
 import play.db.jpa.Transactional;
 
 import com.google.common.base.Throwables;
 
 /**
  * This class provides basic methods, to interact with the JES-Mail Server.
  * For more details have a loo at http://www.ericdaugherty.com/java/mailserver/
  * 
  * @author Eugen Meissner
  *
  */
 public class MailServerUserManagment {
 	
 	private Properties initData;
 	private Properties users;
 	private Properties mail;
 	
 	private File userFile;
 	private File mailFile;
 	private File initDataFile;
 	private Process mailServerProcess;
 	
 	private static MailServerUserManagment instance;
 	private static Object lock = new Object();
 	
 	/**
 	 * Singleton
 	 * @return instance of {@link MailServerUserManagment}
 	 */
 	@Transactional
 	public static MailServerUserManagment getInstance(){
 		if(instance == null){
 			synchronized (lock) {
 				if(instance == null)
 					instance = new MailServerUserManagment();
 			}
 		}
 		
 		return instance;
 	}
 	
 	/**
 	 * Basic constrcutor which inits the MailServer.
 	 */
 	private MailServerUserManagment(){
 		userFile = new File("./ext/jes/user.conf");
 		mailFile = new File("./ext/jes/mail.conf");
 		initDataFile = new File("./conf/initdata.conf");
 		users = new Properties();
 		mail = new Properties();
 		initData = new Properties();
 		readInitDataFile();
 		readMailFile();
 		readUserFile();
 	}
 	
 	/**
 	 * This method adds a new user to the mail server.
 	 * 
 	 * @param user - user part of an email
 	 * @param host - host part of an email
 	 * @param password - password of the mail account
 	 */
	public synchronized void addUser(String host, String user, String password){
 		String userKey = getUserKey(user, host);
 		users.put(userKey, password);
 		writeUserFile();
 	}
 	
 	/**
 	 * This method adds a new user to the mail server with a forward address.
 	 * 
 	 * @param user - user part of an email
 	 * @param host - host part of an email
 	 * @param password - password of the mail account
 	 * @param forwardAddress - the forward address
 	 */
 	public synchronized void addUserWithForwarding(String user, String host, String password, String forwardAddress){
 		String userKey = getUserKey(user, host);
 		String forwardKey = getUserForwadKey(user, host);
 		
 		users.put(userKey, password);
 		users.put(forwardKey, forwardAddress);
 		writeUserFile();
 	}
 	
 	/**
 	 * This method removes an email from the mail server.
 	 * 
 	 * @param user - user part of an email
 	 * @param host - host part of an email
 	 */
 	public synchronized void removeUser(String user, String host){
 		String userKey = getUserKey(user, host);
 		String forwardKey = getUserForwadKey(user, host);
 		
 		if(users.containsKey(userKey)){
 			users.remove(userKey);
 		}
 		
 		if(users.containsKey(forwardKey)){
 			users.remove(forwardKey);
 		}
 		
 		writeUserFile();
 	}
 	
 	/**
 	 * This method updates the forwarding email address.
 	 * 
 	 * @param oldForwardAddress - old forward address
 	 * @param newForwardAddress - new forward address
 	 */
 	public synchronized void updateUserForwarding(String oldForwardAddress, String newForwardAddress){
 		for( Entry<Object, Object> e : users.entrySet()){
 			if(e.getValue().equals(oldForwardAddress)){
 				users.put(e.getKey(), newForwardAddress);
 			}
 		}
 	}
 	
 	/**
 	 * This method returns the domain of the mail server.
 	 * 
 	 * @return domain of the mail server.
 	 */
 	public String getDomain(){
 		return initData.getProperty("yadm.domain");
 	}
 	
 	/**
 	 * Get the default password.
 	 * 
 	 * @return
 	 */
 	public String getDefaultUsername(){
 		return initData.getProperty("yadm.username");
 	}
 	
 	/**
 	 * Get the default username.
 	 * 
 	 * @return
 	 */
 	public String getDefaultPassword(){
 		return initData.getProperty("yadm.username");
 	}
 		
 	/**
 	 * This method return all domains, but not the main domain, if it is not provided in domains, of the mail server.
 	 * 
 	 * @return - all domains of the mail server
 	 */
 	public List<String> getDomains(){
 		List<String> domains = new ArrayList<>();
 		String sDomains = initData.getProperty("yadm.domains"); 
 		
 		if(sDomains != null){
 			String[] sArrDomains = sDomains.split(",");
 			for(String domain :  sArrDomains)
 				domains.add(domain);
 		}
 		
 		return domains;
 	}
 	
 	/**
 	 * This method adds domains to the mail server.
 	 * 
 	 * @param domains - list of domains
 	 */
 	public void setDomainsOnMailServer(List<String> domains){
 		StringBuilder sb = new StringBuilder();
 		for(String domain : domains)
 			sb.append(domain).append(",");
 		String sDomains = sb.toString();
 		if(sDomains.lastIndexOf(",")==sb.length()-1){
 			sDomains = sDomains.substring(0, sb.length()-1);
 		}
 		mail.put("domains", sDomains);
 		writeMailFile();
 	}
 	
 	/**
 	 * This method starts the mail server.
 	 */
 	@Transactional
 	public void startMailServer(){
 		try {
 			mailServerProcess = Runtime.getRuntime().exec("cmd /c start .\\startmailserver.bat");
 			mailServerProcess.waitFor();
 		} catch (IOException e) {
 			Throwables.propagate(e);
 		} catch (InterruptedException e) {
 			Throwables.propagate(e);
 		}
 	}
 	
 	/**
 	 * This method stops the mail server.
 	 */
 	public void stopMailServer(){
 		mailServerProcess.destroy();
 	}
 	
 	/**
 	 * This method builds the user user key.
 	 * 
 	 * @param user - user part of an email
 	 * @param host - host part of an email
 	 * @return user key
 	 */
 	private String getUserKey(String user, String host){
 		return "user."+user+"@"+host;
 	}
 	
 	/**
 	 * This method builds the user forward key.
 	 * 
 	 * @param user - user part of an email
 	 * @param host - host part of an email
 	 * @return forward key
 	 */
 	private String getUserForwadKey(String user, String host){
 		return "userprop."+user+"@"+host+".forwardAddresses";
 	}
 		
 	/**
 	 * This method writes the current properties into the user.conf file.
 	 */
 	private void writeUserFile(){
 		try {
 			OutputStream out = new FileOutputStream(userFile);
 			users.store(out, "");
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		
 	}
 	
 
 	/**
 	 * This method reads the properties from the user.conf file.
 	 */
 	private void readUserFile(){
 		Properties tmp = (Properties) users.clone();
 		FileInputStream is;
 		try {
 			is = new FileInputStream(userFile);
 			users = new Properties();
 			users.load(is);
 			is.close();
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			users = (Properties)tmp.clone();
 			e.printStackTrace();
 		}
 	}
 	
 	/**
 	 * This method writes the current properties into the mail.conf file.
 	 */
 	private void writeMailFile(){
 		try {
 			OutputStream out = new FileOutputStream(mailFile);
 			mail.store(out, "");
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		
 	}
 	
 	/**
 	 * This method reads the properties from the mail.conf file.
 	 */
 	private void readMailFile(){
 		Properties tmp = (Properties) mail.clone();
 		FileInputStream is;
 		try {
 			is = new FileInputStream(mailFile);
 			mail = new Properties();
 			mail.load(is);
 			is.close();
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			mail = (Properties)tmp.clone();
 			e.printStackTrace();
 		}
 	}
 	
 	/**
 	 * This method reads the properties from the initdata.conf file.
 	 * It can only be performed on the startup.
 	 */
 	private void readInitDataFile(){
 		if(!initData.isEmpty()) return;
 		FileInputStream is;
 		try {
 			is = new FileInputStream(initDataFile);
 			initData.load(is);
 			is.close();
 		} catch (FileNotFoundException e) {
 			Throwables.propagate(e);
 		} catch (IOException e) {
 			Throwables.propagate(e);
 		}
 	}
 
 	/**
 	 * This method removes all users.
 	 */
 	public void clearUsers() {
 		users.clear();
 		writeUserFile();
 	}
 	
 }
