 package com.gofetch.entities;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 //import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import javax.persistence.EntityManager;
 import javax.persistence.EntityManagerFactory;
 import javax.persistence.Persistence;
 import javax.persistence.PersistenceUnit;
 import javax.persistence.Query;
 //import javax.persistence.Query;
 import javax.persistence.TemporalType;
 
 import com.gofetch.utils.DateUtil;
 //import com.google.appengine.api.rdbms.AppEngineDriver;
 
 /**
  * Service class for CRUD operations on URL entity
  * @author alandonohoe
  *
  */
 
 public class URLDBService{
 
 	@PersistenceUnit(unitName="GoFetch")
 	EntityManagerFactory emf;
 
 	private static Logger log = Logger.getLogger(URLDBService.class.getName());
 
 	//TODO: dont know if this works - JQPL Selecting single field.. rather than whole entity...
 	public List<String> getURLAddresses(){
 
 		log.info("Entering getURLAddresses");
 		List<String> result = null;
 
 		emf = Persistence.createEntityManagerFactory("GoFetch");
 		EntityManager mgr = emf.createEntityManager();
 
 		try { 
 			result= (List<String>) mgr.createQuery("SELECT u.url_address FROM url u").getResultList();
 		}catch(Exception e){
 			String msg = "Exception thrown. URLService: getURLAddresses";
 
 			log.severe(msg + e.getMessage());
 
 		} finally {
 			mgr.close();
 		}
 		if (result == null) {
 			log.warning("No URLs returned");
 		}
 		log.info("Exiting getURLAddresses");
 		return result;
 	}
 
 	//TODO: writing URLs to DB is v v slow - takes about 2 secs per record... why????
 	//	maybe create a method: void createURLs(List<URL> listURLs) method and have a loop within that to include only: if(!urlInDB(url)){...persist.... }
 	//	use batch processing of URLs to DB.
 	public boolean createURL(URL url) throws Exception{
 		log.info("Entering url[" + url.getUrl_address() + "]");
 		
 		boolean urlInDB = false; // flag, set to true if URL already exists in DB.
 
 		emf = Persistence.createEntityManagerFactory("GoFetch");
 		EntityManager mgr = emf.createEntityManager();
 
 		try {
 
 			///////
 			// check to see if url already in table
 			// TODO: make an efficient look up service here..... see:
 			// http://stackoverflow.com/questions/558978/most-efficient-way-to-see-if-an-arraylist-contains-an-object-in-java
 
 
 			if(!urlInDB(url)){
 
 				mgr.getTransaction().begin();
 				mgr.persist(url);
 				mgr.getTransaction().commit();
 
 			}else{
 				//TODO: send error to the user with the offending url address....
 
 				log.info("url already exists in DB: "
 						+ url.getUrl_address());
 				
 				urlInDB = true;
 			}
 
 
 		}catch(Exception e){
 			String msg = "Exception thrown. URLService: createURL";
 			log.severe(msg + e.getMessage());
 			throw(e);
 
 		}
 		finally {
 			mgr.close();
 		}
 		log.info("Exiting createURL");
 		return urlInDB;
 	}
 
 	/**
 	 * Gets a URL given an ID
 	 * @param id
 	 * @return URL
 	 */
 	public URL getURL(int id) {
 		log.info("Entering getURL[" + id + "]");
 		URL result = null;
 
 		emf = Persistence.createEntityManagerFactory("GoFetch");
 		EntityManager mgr = emf.createEntityManager();
 
 		try {
 			result = mgr.find(URL.class, id);
 		}catch(Exception e){
 			String msg = "Exception thrown. URLService: getURL";
 			
 			log.severe(msg + e.getMessage());
 
 		} finally {
 			mgr.close();
 		}
 		if (result == null) {
 			log.warning("URLDBService: getURL(int id) - No URLs returned, for id:" + id);
 		}
 		log.info("Exiting getURL");
 		return result;
 	}
 
 
 	/**
 	 * Gets a URL given an ID
 	 * @param id
 	 * @return URL
 	 */
 	public URL getURL(String address) {
 		log.info("Entering getURL[" + address + "]");
 
 		List<URL> url = null;
 
 		emf = Persistence.createEntityManagerFactory("GoFetch");
 		EntityManager mgr = emf.createEntityManager();
 
 		try {
 			url = (List<URL>) mgr.createQuery("SELECT u FROM URL u WHERE u.url_address = :address")
 					.setParameter("address", address)
 					.getResultList();
 		}catch(Exception e){
 			String msg = "Exception thrown getting URL: " + address + "URLService: getURL";
 			//logger.logp(Level.SEVERE, "URLService", "getURL",msg ,e);
 			log.severe(msg + e.getMessage());
 
 		} finally {
 			mgr.close();
 		}
 
 		log.info("Exiting getURL");
 
 		if((null == url) || (url.isEmpty()))
 			return null;
 		else
 			return url.get(0);
 
 	}
 
 	/**
 	 * 
 	 * @return List of ALL URLs in DB
 	 */
 	@SuppressWarnings("unchecked")
 	public List<URL> getURLs() {
 		log.info("Entering getURLs");
 		List<URL> result = null;
 
 		emf = Persistence.createEntityManagerFactory("GoFetch");
 		EntityManager mgr = emf.createEntityManager();
 
 		try {
 			result= mgr.createQuery("SELECT u FROM URL u ORDER BY u.url_address").getResultList();
 		}catch(Exception e){
 			String msg = "Exception thrown. URLService: getURLs";
 			//logger.logp(Level.SEVERE, "URLService", "getURLs",msg ,e);
 			log.severe(msg + e.getMessage());
 
 		} finally {
 			mgr.close();
 		}
 		if (result == null) {
 			log.warning("No URLs returned");
 		}
 		log.info("Exiting getURLs");
 		return result;
 	}
 
 	public List<URL> getURLs(Date date){
 
 		log.info("Entering getURLs");
 		List<URL> result = null;
 
 		emf = Persistence.createEntityManagerFactory("GoFetch");
 		EntityManager mgr = emf.createEntityManager();
 
 		try {
 			result = mgr.createQuery("SELECT u FROM URL u WHERE u.date = :date")
 					.setParameter("date", date, TemporalType.DATE)
 					.getResultList();
 
 		}catch(Exception e){
 			String msg = "Exception thrown. URLService: getURLs";
 
 			log.severe(msg + e.getMessage());
 
 		} finally {
 			mgr.close();
 		}
 		if (result == null) {
 			log.warning("No URLs returned");
 		}
 		log.info("Exiting getURLs");
 		return result;
 
 	}
 
 	/*
 	 * Returns a list of URLs that were added to the database today.
 	 */
 	public List<URL> getTodaysURLs(){
 
 		log.info("Entering getTodaysURLs");
 
 		List<URL> result = null;
 
 		Date todaysDate = DateUtil.getTodaysDate();
 
 		result = getURLs(todaysDate);
 
 		log.info("Exiting getTodaysURLs");
 
 		return result;
 
 	}
 
 
 	/*
 	 * Returns a list of URLs that were added to the database today.
 	 */
 	public List<URL> getTargetURLsFrom(Date date){
 
 		log.info("Entering getTargetURLsFrom");
 		List<URL> result = null;
 
 		emf = Persistence.createEntityManagerFactory("GoFetch");
 		EntityManager mgr = emf.createEntityManager();
 
 		try {
 			result = mgr.createQuery("SELECT u FROM URL u WHERE u.date = :date AND u.get_backlinks = TRUE")
 					.setParameter("date", date, TemporalType.DATE)
 					.getResultList();
 
 		}catch(Exception e){
 			String msg = "Exception thrown. URLService: getTargetURLsFrom";
 			//logger.logp(Level.SEVERE, "URLService", "getTargetURLsFrom",msg ,e);
 			log.severe(msg + e.getMessage());
 
 		} finally {
 			mgr.close();
 		}
 		if (result == null) {
 			log.warning("No URLs returned");
 		}
 		log.info("Exiting getYesterdaysURLs");
 		return result;
 	}
 
 
 	/*
 	 * Returns a list of URLs that were added to the database today.
 	 */
 	public List<URL> getYesterdaysURLs(){
 
 		log.info("Entering getYesterdaysURLs");
 		List<URL> result = null;
 
 		Date yestDate = DateUtil.getYesterDaysDate();
 
 		result = getURLs(yestDate);
 
 		log.info("Exiting getYesterdaysURLs");
 
 		return result;
 
 	}
 
 
 
 	/*
 	 * Returns a list of URLs that were added between the 2 dates
 	 */
 	public List<URL> getURLsBetween(Date startDate, Date endDate){
 
 		log.info("Entering getURLsBetween");
 		List<URL> result = null;
 
 		emf = Persistence.createEntityManagerFactory("GoFetch");
 		EntityManager mgr = emf.createEntityManager();
 
 		try {
 			result = mgr.createQuery("SELECT u FROM URL u WHERE u.date BETWEEN :startDate AND :endDate")
 					.setParameter("startDate", startDate, TemporalType.DATE)
 					.setParameter("endDate", endDate, TemporalType.DATE)
 					.getResultList();
 
 		}catch(Exception e){
 			String msg = "Exception thrown: URLService: getURLsBetween";
 
 			log.severe(msg + e.getMessage());
 
 		} finally {
 			mgr.close();
 		}
 		if (result == null) {
 			log.warning("No URLs returned");
 		}
 		log.info("Exiting getURLsBetween");
 		return result;
 
 	}
 
 	// TODO: make an efficient look up service here..... see:
 	// http://stackoverflow.com/questions/558978/most-efficient-way-to-see-if-an-arraylist-contains-an-object-in-java
 	private boolean urlInDB(URL urlInDB){
 		
 		// new code - 
 		
 		return urlInDB(urlInDB.getUrl_address());
 
 		// 	old code -  v inefficent....
 //		List<URL> urlList = getURLs();
 //
 //		for(URL urlCurrent: urlList){
 //			if(urlCurrent.getUrl_address().equals(urlCheck.getUrl_address()))
 //				return true;
 //		}
 //
 //		return false;
 	}
 
 	// TODO: make an efficient look up service here..... see:
 	// http://stackoverflow.com/questions/558978/most-efficient-way-to-see-if-an-arraylist-contains-an-object-in-java
 	public boolean urlInDB(String urlAddress){
 		
 		//old code doesnt make any sense.... pulls all the urls into RAM - just query DB and return false if its not in DB
 		
 		//new code:
 		if(null == getURL(urlAddress))
 			return false;
 		else
 			return true;
 		
 		//OLD Code:
 //		List<URL> urlList = getURLs();
 //
 //		for(URL urlCurrent: urlList){
 //			if(urlCurrent.getUrl_address().equals(urlAddress))
 //				return true;
 //		}
 //
 //		return false;
 	}
 
 	public Integer getURLIDFromAddress(String urlAddress){
 
 		Integer url_id;
 		List<URL> result = null;
 
 		emf = Persistence.createEntityManagerFactory("GoFetch");
 		EntityManager mgr = emf.createEntityManager();
 
 		try {
 			result = mgr.createQuery("SELECT u FROM URL u WHERE u.url_address = :urlAddress")
 					.setParameter("urlAddress", urlAddress)
 					.getResultList();
 		}catch(Exception e){
 			String msg = "Exception thrown: URLService: getURLIDFromAddress";
 			//logger.logp(Level.SEVERE, "URLService", "getURLIDFromAddress",msg ,e);
 			log.severe(msg + e.getMessage());
 
 		}  finally {
 			mgr.close();
 		}
 		if (result == null || result.isEmpty()) {
 			return 0;
 		}
 
 		url_id = result.get(0).getId();
 
 		return url_id;
 	}
 
 	public String getURLAddressFromID(Integer urlID){
 
 		String address;
 		List<URL> result = null;
 
 		emf = Persistence.createEntityManagerFactory("GoFetch");
 		EntityManager mgr = emf.createEntityManager();
 
 		try {
 			result = mgr.createQuery("SELECT u FROM URL u WHERE u.url_id = :urlID")
 					.setParameter("urlID", urlID)
 					.getResultList();
 		}catch(Exception e){
 			String msg = "Exception thrown: URLService: getURLIDFromAddress";
 			//logger.logp(Level.SEVERE, "URLService", "getURLIDFromAddress",msg ,e);
 			log.severe(msg + e.getMessage());
 
 		}  finally {
 			mgr.close();
 		}
 		if (result == null || result.isEmpty()) {
 			return null;
 		}
 
 		address = result.get(0).getUrl_address();
 
 		return address;
 	}
 
 	/**
 	 * Updates record with url address with new page authority (pa) & domain authority (da) data
 	 * 
 	 * @param urlAddress - target url 
 	 * @param pa - Page Authority
 	 * @param da - Domain Authority
 	 */
 	public void updateURLAuthorityData(String urlAddress, Integer pa, Integer da){
 
 		emf = Persistence.createEntityManagerFactory("GoFetch");
 		EntityManager mgr = emf.createEntityManager();
 		int url_id;
 
 		try {
 
 			url_id = getURLIDFromAddress(urlAddress);
 
 			mgr.getTransaction().begin();
 
 			URL url = mgr.find(URL.class, url_id);
 			url.setPage_authority(pa);
 			url.setDomain_authority(da);
 
 			mgr.merge(url);
 			mgr.getTransaction().commit();
 
 		}catch(Exception e){
 			String msg = "Exception thrown: URLService: updateURLAuthorityData";
 			//logger.logp(Level.SEVERE, "URLService", "updateURLAuthorityData",msg ,e);
 			log.severe(msg + e.getMessage());
 
 		}  finally {
 			mgr.close();
 		}
 
 
 	}
 
 	/**
 	 * Updates record with url address with new page authority (pa) & domain authority (da) data
 	 * 
 	 * @param urlAddress - target url 
 	 * @param pa - Page Authority
 	 * @param da - Domain Authority
 	 * @param domainName - Domain name of url.
 	 */
 	public void updateURLData(String urlAddress, Integer pa, Integer da, String docTitle){
 
 		emf = Persistence.createEntityManagerFactory("GoFetch");
 		EntityManager mgr = emf.createEntityManager();
 		int url_id;
 
 		try {
 
 			url_id = getURLIDFromAddress(urlAddress);
 
 			mgr.getTransaction().begin();
 
 			URL url = mgr.find(URL.class, url_id);
 			url.setPage_authority(pa);
 			url.setDomain_authority(da);
 			url.setDoc_title(docTitle);
 
 			mgr.merge(url);
 			mgr.getTransaction().commit();
 
 		}catch(Exception e){
 			String msg = "Exception thrown. URLService: updateURLData";
 			//logger.logp(Level.SEVERE, "URLService", "updateURLData",msg ,e);
 			log.severe(msg + e.getMessage());
 
 		}  finally {
 			mgr.close();
 		}
 
 	}
 
 	public void updateSocialFrequencyData(URL url){
 
 		emf = Persistence.createEntityManagerFactory("GoFetch");
 		EntityManager mgr = emf.createEntityManager();
 
 		try {
 
 			mgr.getTransaction().begin();
 
 			URL urlDB = mgr.find(URL.class, url.getId());
 			urlDB.setSocial_data_freq(url.getSocial_data_freq());
 			urlDB.setSocial_data_date(url.getSocial_data_date());
 
 			mgr.merge(urlDB);
 			//mgr.flush();
 			mgr.getTransaction().commit();
 			
 
 
 
 		}catch(Exception e){
 			String msg = "Exception thrown. URLService: updateSocialFrequencyData";
 			//logger.logp(Level.SEVERE, "URLService", "updateDomainNames",msg ,e);
 			log.severe(msg + e.getMessage());
 
 		} finally {
 			mgr.close();
 		}
 
 
 	}
 
 	public void updateDomainNames(List<URL> urls){
 
 		emf = Persistence.createEntityManagerFactory("GoFetch");
 		EntityManager mgr = emf.createEntityManager();
 
 		try {
 
 			for(URL currentURL : urls){
 
 				mgr.getTransaction().begin();
 
 				URL url = mgr.find(URL.class, currentURL.getId());
 				url.setDomain(currentURL.getDomain());
 
 				mgr.merge(url);
 				mgr.getTransaction().commit();
 
 			}
 
 		}catch(Exception e){
 			String msg = "Exception thrown. URLService: updateDomainNames";
 			//logger.logp(Level.SEVERE, "URLService", "updateDomainNames",msg ,e);
 			log.severe(msg + e.getMessage());
 
 		} finally {
 			mgr.close();
 		}
 
 	}
 
 
 	/**
 	 * Updates record with url address with new page authority (pa) & domain authority (da) data
 	 * 
 	 * @param urlAddress - target url 
 	 * @param pa - Page Authority
 	 * @param da - Domain Authority
 	 * @param domainName - Domain name of url.
 	 */
 	public void updateBackLinksGot(URL url, boolean backLinksGot ){
 
 		emf = Persistence.createEntityManagerFactory("GoFetch");
 		EntityManager mgr = emf.createEntityManager();
 		int url_id;
 
 		try {
 
 			url_id = url.getId(); 
 
 			mgr.getTransaction().begin();
 
 			URL urlDB = mgr.find(URL.class, url_id);
 			urlDB.setBacklinks_got(backLinksGot);
 
 			mgr.merge(urlDB);
 			mgr.getTransaction().commit();
 
 		}catch(Exception e){
 			String msg = "Exception thrown. URLService: updateURLData";
 
 			log.severe(msg + e.getMessage());
 
 		}  finally {
 			mgr.close();
 		}
 
 	}
 
 
 
 
 	/*
 	 * Returns a list of URLs that are to be monitored for social data...
 	 */
 	public List<URL> getSociallyTrackedURLs(){
 
 		log.info("Entering getSociallyTrackedURLs");
 		List<URL> result = null;
 
 		emf = Persistence.createEntityManagerFactory("GoFetch");
 		EntityManager mgr = emf.createEntityManager();
 
 		try {
 			result = mgr.createQuery("SELECT u FROM URL u WHERE u.get_social_data = TRUE")
 					.getResultList();
 
 		}catch(Exception e){
 			String msg = "Exception thrown. URLService: getSociallyTrackedURLs";
 			//logger.logp(Level.SEVERE, "URLService", "getSociallyTrackedURLs",msg ,e);
 			log.severe(msg + e.getMessage());
 
 		} finally {
 			mgr.close();
 		}
 		if (result == null) {
 			log.warning("No SociallyTracked URLs returned");
 		}
 		log.info("Exiting getSociallyTrackedURLs");
 		return result;
 
 	}
 
 
 	public List<URL> getURLsFromIDs(List<Integer> idList){
 
 		log.info("Entering getURLsFromIDs");
 		// List<URL> result = null; // used if we use option 1
 		List<URL> urls = new ArrayList<URL>(); //used if we use option 2.
 
 		emf = Persistence.createEntityManagerFactory("GoFetch");
 		EntityManager mgr = emf.createEntityManager();
 
 		//TODO: decide: 2 options: - need to check in optimization....
 
 		//1: one call to DB and search list for idList
 		//		get list of all urls and then use java selection process to retrieve urls from this list that are in idList
 
 		//2: multiple calls to DB:
 		//	loop through all idList and make repeat calls to DB retrieving URL for each.
 
 
 		try {
 
 			//2: 
 			for(int i = 0; i < idList.size(); i++){
 
 				urls.add(mgr.find(URL.class, idList.get(i)));
 
 			}
 
 
 		}catch(Exception e){
 			String msg = "Exception thrown. URLService: getURLsFromIDs";
 			//logger.logp(Level.SEVERE, "URLService", "getSociallyTrackedURLs",msg ,e);
 			log.severe(msg + e.getMessage());
 
 		} finally {
 			mgr.close();
 		}
 
 		log.info("Exiting getURLsFromIDs");
 		return urls;
 
 	}
 
 	//	/**
 	//	 * Method checks that urlAddress is in DB, and if it is, deletes that URL from the DB.
 	//	 * @param urlAddress string address of url to be deleted
 	//	 */
 	//	public void deleteURL(String urlAddress){
 	//
 	//		log.info("Entering deleteURL. Deleting " + urlAddress);
 	//
 	//		URL url;
 	//
 	//		emf = Persistence.createEntityManagerFactory("GoFetch");
 	//		EntityManager mgr = emf.createEntityManager();
 	//
 	//		url = getURL(urlAddress);
 	//
 	//		if(url != null){
 	//
 	//			try{
 	//				mgr.getTransaction().begin();
 	//
 	//				mgr.createQuery("DELETE FROM URL u WHERE u.url_id = :url_id")
 	//				.setParameter("url_id", url_id)
 	//				.executeUpdate();
 	//
 	//				mgr.getTransaction().commit();
 	//
 	//			}catch(Exception e){
 	//				String msg = "Exception thrown deleting: " + urlAddress;
 	//				logger.logp(Level.SEVERE, "URLService", "deleteURL",msg ,e);
 	//
 	//			} finally {
 	//				mgr.close();
 	//			}
 	//
 	//		}
 	//		else{
 	//			log.info(urlAddress + " Not in DB.");
 	//		}
 	//
 	//		log.info("Exiting deleteURL.");
 	//
 	//	}
 	//
 
 	public void deleteURL(int url_id){
 
 		log.info("Entering deleteURL. Deleting " + url_id);
 
 
 		emf = Persistence.createEntityManagerFactory("GoFetch");
 		EntityManager mgr = emf.createEntityManager();
 
 		try{
 			mgr.getTransaction().begin();
 
 			mgr.createQuery("DELETE FROM URL u WHERE u.url_id = :url_id")
 			.setParameter("url_id", url_id)
 			.executeUpdate();
 
 			mgr.getTransaction().commit();
 
 		}catch(Exception e){
 			String msg = "Exception thrown deleting: " + url_id + ". URLService: deleteURL";
 			//logger.logp(Level.SEVERE, "URLService", "deleteURL",msg ,e);
 			log.severe(msg + e.getMessage());
 
 		} finally {
 			mgr.close();
 		}
 
 
 		log.info("Exiting deleteURL.");
 
 	}
 
 	/*
 	 * Returns list of urls that have had their get_backlinks flag checked, but have not yet got their backlink data eg: from SEOMoz
 	 */
 	public List<URL> getUnproccessedTargetURLs(){
 
 		log.info("Entering getUnproccessedTargetURLs");
 
 		List<URL> urls = null; 
 
 		emf = Persistence.createEntityManagerFactory("GoFetch");
 		EntityManager mgr = emf.createEntityManager();
 
 		try {
 
 			urls = mgr.createQuery("SELECT u FROM URL u WHERE u.get_backlinks = TRUE and u.backlinks_got = false")
 					.getResultList();
 
 		}catch(Exception e){
 			String msg = "Exception thrown. URLDBService: getUnproccessedTargetURLs";
 			log.severe(msg + e.getMessage());
 
 		} finally {
 			mgr.close();
 		}
 
 		log.info("Exiting getUnproccessedTargetURLs");
 		return urls;
 
 	}
 
 	/*
 	 * returns all URLs that have a social_data_date less than or equal to todays date and get_social_data = true
 	 */
 	public List<URL> getTodaysSocialCrawlURLs() {
 
 		log.info("Entering getTodaysSocialCrawlURLs");
 		List<URL> result = null;
 		Date date = DateUtil.getTodaysDate();
 
 		emf = Persistence.createEntityManagerFactory("GoFetch");
 		EntityManager mgr = emf.createEntityManager();
 		
 
 		try {
 			result =  mgr.createQuery("SELECT u FROM URL u WHERE u.get_social_data = true AND u.social_data_date <= :date")
 					.setParameter("date", date, TemporalType.DATE)
 					.getResultList();
 
 		}catch(Exception e){
 			String msg = "Exception thrown. URLService: getURLs";
 
 			log.severe(msg + e.getMessage());
 
 		} finally {
 			mgr.close();
 		}
 		if (result == null) {
 			log.warning("No URLs returned");
 		}
 		log.info("Exiting getURLs");
 		return result;
 
 	}
 	
	public List<URL> getURLsOfClientCategory(Integer clientCategoryID, boolean targetURLsOnly){
 		
 		log.info("Entering getURLsOfClientCategory. Client Category = " + clientCategoryID);
 		List<URL> result = null;
 
 		emf = Persistence.createEntityManagerFactory("GoFetch");
 		EntityManager mgr = emf.createEntityManager();
 
		try { 
			if(targetURLsOnly){
				result = mgr.createQuery("SELECT u FROM URL u WHERE u.client_category_id = :client_category_id AND u.client_target_url = true")
						.setParameter("client_category_id", clientCategoryID)
						.getResultList();
			}else{
				result = mgr.createQuery("SELECT u FROM URL u WHERE u.client_category_id = :client_category_id")
						.setParameter("client_category_id", clientCategoryID)
						.getResultList();
				
			}
			
 
 		}catch(Exception e){
 			String msg = "Exception thrown: URLService: getURLsOfClientCategory";
 
 			log.severe(msg + e.getMessage());
 
 		} finally {
 			mgr.close();
 		}
 		if (result == null) {
 			log.info("No URLs returned");
 		}
 		log.info("Exiting getURLsOfClientCategory");
 		return result;
 	}
 	
	public List<URL> getClientsTargetURLs(Integer clientID,  boolean targetURLsOnly){
 		
 		log.info("Entering getClientsURLs. Client ID = " + clientID);
 		List<URL> result = null;
 
 		emf = Persistence.createEntityManagerFactory("GoFetch");
 		EntityManager mgr = emf.createEntityManager();
 
 		try {
			
			if(targetURLsOnly){
			result = mgr.createQuery("SELECT u FROM URL u WHERE u.client_category_users_user_id = :client_category_users_user_id AND u.client_target_url = true")
 					.setParameter("client_category_users_user_id", clientID)
 					.getResultList();
			}else{
				result = mgr.createQuery("SELECT u FROM URL u WHERE u.client_category_users_user_id = :client_category_users_user_id")
						.setParameter("client_category_users_user_id", clientID)
						.getResultList();
				
			}
			
 
 		}catch(Exception e){
 			String msg = "Exception thrown: URLService: getURLsOfClientCategory";
 
 			log.severe(msg + e.getMessage());
 
 		} finally {
 			mgr.close();
 		}
 		if (result == null) {
 			log.info("No URLs returned");
 		}
 		log.info("Exiting getURLsOfClientCategory");
 		return result;
 		
 		
 	}
 }
