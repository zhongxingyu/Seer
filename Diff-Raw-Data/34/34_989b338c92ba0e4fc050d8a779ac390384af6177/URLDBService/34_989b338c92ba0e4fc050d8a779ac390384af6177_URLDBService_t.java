 package com.gofetch.entities;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.logging.Logger;
 
 import javax.persistence.EntityManager;
 import javax.persistence.EntityManagerFactory;
 import javax.persistence.Query;
 //import javax.persistence.Persistence;
 //import javax.persistence.PersistenceUnit;
 import javax.persistence.TemporalType;
 
 import com.gofetch.images.ImageConstants;
 import com.gofetch.utils.DateUtil;
 import com.gofetch.utils.TextUtil;
 
 /**
  * Service class for CRUD operations on URL entity
  * @author alandonohoe
  *
  */
 
 public class URLDBService implements Serializable{
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 	private static Logger log = Logger.getLogger(URLDBService.class.getName());
 	
 	/**
 	 * Pulls all URLs that have their get_backlinks = true;
 	 * and sets their backlinks_got = false.
 	 * So that they will pulled by ProcessNewTargets to check for new backlinks every month
 	 * This is called via a cron job, at the beginning of every month.
 	 */
 	public void updateTURLsGetBacklinks(){
 		
 		log.info("Entering updateTURLsGetBacklinks");
 
 		EntityManagerFactory emf = PersistenceManager.getInstance().getEntityManagerFactory();
 		EntityManager mgr = emf.createEntityManager();
 		
 		//sql equiv: update url set backlinks_got = false where get_backlinks = true;
 		try {   
 			mgr.getTransaction().begin();
 			mgr.createQuery("UPDATE URL SET backlinks_got = FALSE WHERE get_backlinks = TRUE").executeUpdate();
 			mgr.getTransaction().commit();
 			
 		}catch(Exception e){
 			String msg = "Exception thrown. URLDBService: updateTURLsGetBacklinks. ";
 
 			log.severe(msg + e.getMessage());
 
 		} finally {
 			mgr.close();
 		}
 		
 	}
 	
 	
 	/**
 	 * 
 	 * @param targetURL 
 	 * @return list of URLs that point to this target
 	 */
 	@SuppressWarnings("unchecked")
 	public List<URL> getBackLinkURLs(Integer targetURLid){
 		
 		log.info("Entering getBackLinkURLs");
 		
 		List<URL> result = null;
 
 		EntityManagerFactory emf = PersistenceManager.getInstance().getEntityManagerFactory();
 		EntityManager mgr = emf.createEntityManager();
 
 		//SQL: this works: select * from url u where u.url_id IN (select l.source_id from links l where l.target_id = id);
 		try {   
 			result= (List<URL>) mgr.createQuery("SELECT u FROM URL u WHERE u.url_id = ANY (SELECT l.source_id FROM Link l WHERE l.target_id = :id)")
 					.setParameter("id", targetURLid)
 					.getResultList();
 			
 		}catch(Exception e){
 			String msg = "Exception thrown. URLService: getBackLinkURLs. ";
 
 			log.severe(msg + e.getMessage());
 
 		} finally {
 			mgr.close();
 		}
 			
 		return result; 
 			
 	}
 	
 	/**
 	 * 
 	 * @param targetURL 
 	 * @return list of URLs that point to this target
 	 * Essentially a wrapper for getBackLinkURLs(Integer id)
 	 */
 	public List<URL> getBackLinkURLs(String targetURLAddress){
 		
 		log.info("Entering getBackLinkURLs - via URL Address wrapper");
 		
 		Integer id = getURLIDFromAddress(targetURLAddress);
 		
 		return(getBackLinkURLs(id));
 			
 	}
 	
 	/**
 	 * 
 	 * @param urlAddress id of the target URL to get all urls and link data for 
 	 * essentially a wrapper for "getURLAndLinkData(Integer targetURLid)"
 	 *  - just gets the id of the url address.
 	 * @return
 	 * @throws Exception 
 	 */
 	public List<URLAndLinkData> getURLAndLinkData(String urlAddress) throws Exception{
 	
 		Integer url_id = getURLIDFromAddress(urlAddress);
 		
 		return getURLAndLinkData(url_id);
 		
 	}
 	
 
 	
 	/**
 	 * 
 	 * @param targetURL_ID id of the target URL to get all urls and link data for 
 	 * @return
 	 * @throws Exception 
 	 */
 	public List<URLAndLinkData> getURLAndLinkData(Integer targetURLid) throws Exception{
 		
 		log.info("Entering getURLAndLinkData");
 		
 		throw(new Exception("Method not implemented"));
 		
 	//	List<URLAndLinkData> result = null;
 //
 //		EntityManagerFactory emf = PersistenceManager.getInstance().getEntityManagerFactory();
 //		EntityManager mgr = emf.createEntityManager();
 //		
 //	
 //		try {   
 ////			result= (List<URLAndLinkData>) mgr.createQuery("SELECT l FROM Link l INNER JOIN l.targetURL" )
 ////					.getResultList(); 
 //			
 //			
 //		}catch(Exception e){
 //			String msg = "Exception thrown. URLService: getURLAndLinkData. ";
 //
 //			log.severe(msg + e.getMessage());
 //
 //		} finally {
 //			mgr.close();
 //		}
 		
 		
 //		log.info("Exiting getURLAndLinkData");
 		
 		//return result;
 	
 
 			
 	}
 
 	@SuppressWarnings("unchecked")
 	public List<String> getURLAddresses(){
 
 		log.info("Entering getURLAddresses");
 		List<String> result = null;
 
 		EntityManagerFactory emf = PersistenceManager.getInstance().getEntityManagerFactory();
 		EntityManager mgr = emf.createEntityManager();
 
 		try { 
 			result= (List<String>) mgr.createQuery("SELECT u.url_address FROM URL u").getResultList();
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
 
 		EntityManagerFactory emf = PersistenceManager.getInstance().getEntityManagerFactory();
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
 
 		EntityManagerFactory emf = PersistenceManager.getInstance().getEntityManagerFactory();
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
 	@SuppressWarnings("unchecked")
 	public URL getURL(String address) {
 		log.info("Entering getURL[" + address + "]");
 
 		List<URL> url = null;
 
 		EntityManagerFactory emf = PersistenceManager.getInstance().getEntityManagerFactory();
 		EntityManager mgr = emf.createEntityManager();
 
 		try {
 			url = (List<URL>) mgr.createQuery("SELECT u FROM URL u WHERE u.url_address = :address")
 					.setParameter("address", address)
 					.getResultList();
 		}catch(Exception e){
 			String msg = "Exception thrown getting URL: " + address + "URLService: getURL";
 			
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
 
 		EntityManagerFactory emf = PersistenceManager.getInstance().getEntityManagerFactory();
 		EntityManager mgr = emf.createEntityManager();
 
 		try {
 			result= mgr.createQuery("SELECT u FROM URL u ORDER BY u.url_address").getResultList();
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
 
 	@SuppressWarnings("unchecked")
 	public List<URL> getURLs(Date date){
 
 		log.info("Entering getURLs");
 		List<URL> result = null;
 
 		EntityManagerFactory emf = PersistenceManager.getInstance().getEntityManagerFactory();
 		EntityManager mgr = emf.createEntityManager();
 		
 		try {
 			result = (List<URL>) mgr.createQuery("SELECT u FROM URL u WHERE u.date = :date")
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
 	@SuppressWarnings("unchecked")
 	public List<URL> getTargetURLsFrom(Date date){
 
 		log.info("Entering getTargetURLsFrom");
 		List<URL> result = null;
 
 		EntityManagerFactory emf = PersistenceManager.getInstance().getEntityManagerFactory();
 		EntityManager mgr = emf.createEntityManager();
 
 		try {
 			result = (List<URL>) mgr.createQuery("SELECT u FROM URL u WHERE u.date = :date AND u.get_backlinks = TRUE")
 					.setParameter("date", date, TemporalType.DATE)
 					.getResultList();
 
 		}catch(Exception e){
 			String msg = "Exception thrown. URLService: getTargetURLsFrom";
 			
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
 	@SuppressWarnings("unchecked")
 	public List<URL> getURLsBetween(Date startDate, Date endDate){
 
 		log.info("Entering getURLsBetween");
 		List<URL> result = null;
 
 		EntityManagerFactory emf = PersistenceManager.getInstance().getEntityManagerFactory();
 		EntityManager mgr = emf.createEntityManager();
 
 		try {
 			result = (List<URL>) mgr.createQuery("SELECT u FROM URL u WHERE u.date BETWEEN :startDate AND :endDate")
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
 			
 		return urlInDB(urlInDB.getUrl_address());
 
 	}
 
 	// TODO: make an efficient look up service here..... see:
 	// http://stackoverflow.com/questions/558978/most-efficient-way-to-see-if-an-arraylist-contains-an-object-in-java
 	public boolean urlInDB(String urlAddress){
 		
 		if(null == getURL(urlAddress))
 			return false;
 		else
 			return true;
 		
 	}
 
 	@SuppressWarnings("unchecked")
 	public Integer getURLIDFromAddress(String urlAddress){
 
 		Integer url_id;
 		List<URL> result = null;
 
 		EntityManagerFactory emf = PersistenceManager.getInstance().getEntityManagerFactory();
 		EntityManager mgr = emf.createEntityManager();
 		
 		try {
 			result = (List<URL>) mgr.createQuery("SELECT u FROM URL u WHERE u.url_address = :urlAddress")
 					.setParameter("urlAddress", urlAddress)
 					.getResultList();
 		}catch(Exception e){
 			String msg = "Exception thrown: URLService: getURLIDFromAddress";
 			
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
 
 	@SuppressWarnings("unchecked")
 	public String getURLAddressFromID(Integer urlID){
 
 		String address;
 		List<URL> result = null;
 
 		EntityManagerFactory emf = PersistenceManager.getInstance().getEntityManagerFactory();
 		EntityManager mgr = emf.createEntityManager();
 
 		try {
 			result = (List<URL>) mgr.createQuery("SELECT u FROM URL u WHERE u.url_id = :urlID")
 					.setParameter("urlID", urlID)
 					.getResultList();
 		}catch(Exception e){
 			String msg = "Exception thrown: URLService: getURLIDFromAddress";
 			
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
 	 * Updates record with url address with new page authority (pa) & domain authority (da) data AND sets "get_authority_data" = false.
 	 * 
 	 * @param urlAddress - target url 
 	 * @param pa - Page Authority
 	 * @param da - Domain Authority
 	 */
 	public void updateURLAuthorityData(String urlAddress, Integer pa, Integer da){
 
 		EntityManagerFactory emf = PersistenceManager.getInstance().getEntityManagerFactory();
 		EntityManager mgr = emf.createEntityManager();
 		
 		int url_id;
 
 		try {
 
 			url_id = getURLIDFromAddress(urlAddress);
 
 			mgr.getTransaction().begin();
 
 			URL url = mgr.find(URL.class, url_id);
 			url.setPage_authority(pa);
 			url.setDomain_authority(da);
 			url.setGet_authority_data(false);
 
 			mgr.merge(url);
 			mgr.getTransaction().commit();
 
 		}catch(Exception e){
 			String msg = "Exception thrown: URLService: updateURLAuthorityData";
 			
 			log.severe(msg + e.getMessage());
 
 		}  finally {
 			mgr.close();
 		}
 
 
 	}
 	
 	/**
 	 * Updates record with url address with new domain authority (da) data AND sets "get_authority_data" = false.
 	 * 
 	 * @param urlAddress - target url 
 	 * @param pa - Page Authority
 	 * @param da - Domain Authority
	 * @throws Exception - if unable to update DA
 	 */
	public void updateURLDomainAuthorityData(String urlAddress, Integer da) throws Exception{
 
 		log.info("updateURLDomainAuthorityData() for: " + urlAddress);
 		
 		EntityManagerFactory emf = PersistenceManager.getInstance().getEntityManagerFactory();
 		EntityManager mgr = emf.createEntityManager();
 		
 		int url_id;
 
 		try {
 
 			url_id = getURLIDFromAddress(urlAddress);
 
 			mgr.getTransaction().begin();
 
 			URL url = mgr.find(URL.class, url_id);
 			url.setDomain_authority(da);
 			url.setGet_authority_data(false);
 
 			mgr.merge(url);
 			mgr.getTransaction().commit();
 
 		}catch(Exception e){
 			String msg = "Exception thrown: URLService: updateURLDomainAuthorityData";
 			
 			log.severe(msg + e.getMessage());
			throw(e);
 
 		}  finally {
 			mgr.close();
 		}
 
 
 	}
 
 	/**
 	 * Updates record with url address with new page authority (pa), domain authority (da), docTitle data
 	 * Then sets URL's get_authority_data to false.
 	 * 
 	 * @param urlAddress - target url 
 	 * @param pa - Page Authority
 	 * @param da - Domain Authority
 	 * @param domainName - Domain name of url.
 	 */
 	public void updateURLData(String urlAddress, Integer pa, Integer da, String docTitle){
 		
 		log.info("Entering updateURLData(...) for: " + urlAddress);
 
 		EntityManagerFactory emf = PersistenceManager.getInstance().getEntityManagerFactory();
 		EntityManager mgr = emf.createEntityManager();
 		
 		int url_id;
 
 		try {
 
 			url_id = getURLIDFromAddress(urlAddress);
 
 			mgr.getTransaction().begin();
 
 			URL url = mgr.find(URL.class, url_id);
 			url.setPage_authority(pa);
 			url.setDomain_authority(da);
 			url.setDoc_title(docTitle);
 			url.setGet_authority_data(false);
 
 			mgr.merge(url);
 			mgr.getTransaction().commit();
 
 		}catch(Exception e){
 			String msg = "Exception thrown. URLService: updateURLData";
 			
 			log.severe(msg + e.getMessage());
 
 		}  finally {
 			mgr.close();
 		}
 
 	}
 
 	public void updateSocialFrequencyData(URL url){
 		
 		log.info("Entering URLService::updateSocialFrequencyData");
 
 		EntityManagerFactory emf = PersistenceManager.getInstance().getEntityManagerFactory();
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
 			log.severe(msg + e.getMessage());
 
 		} finally {
 			mgr.close();
 		}
 
 
 	}
 
 	public void updateDomainNames(List<URL> urls){
 
 	
 		EntityManagerFactory emf = PersistenceManager.getInstance().getEntityManagerFactory();
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
 
 		EntityManagerFactory emf = PersistenceManager.getInstance().getEntityManagerFactory();
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
 	@SuppressWarnings("unchecked")
 	public List<URL> getSociallyTrackedURLs(){
 
 		log.info("Entering getSociallyTrackedURLs");
 		List<URL> result = null;
 
 		EntityManagerFactory emf = PersistenceManager.getInstance().getEntityManagerFactory();
 		EntityManager mgr = emf.createEntityManager();
 		
 		try {
 			result = (List<URL>) mgr.createQuery("SELECT u FROM URL u WHERE u.get_social_data = TRUE")
 					.getResultList();
 
 		}catch(Exception e){
 			String msg = "Exception thrown. URLService: getSociallyTrackedURLs";
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
 		List<URL> urls = new ArrayList<URL>(); //used if we use option 2.
 
 		EntityManagerFactory emf = PersistenceManager.getInstance().getEntityManagerFactory();
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
 
 
 		EntityManagerFactory emf = PersistenceManager.getInstance().getEntityManagerFactory();
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
 	@SuppressWarnings("unchecked")
 	public List<URL> getUnproccessedTargetURLs(Integer limit){
 
 		log.info("Entering getUnproccessedTargetURLs");
 
 		List<URL> urls = null; 
 
 		EntityManagerFactory emf = PersistenceManager.getInstance().getEntityManagerFactory();
 		EntityManager mgr = emf.createEntityManager();
 		
 		try {
 
 			urls = (List<URL>) mgr.createQuery("SELECT u FROM URL u WHERE u.get_backlinks = TRUE and u.backlinks_got = false")
 					.setMaxResults(limit)
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
 	
 	/**
 	 * @param maxResults no of URLs to pull
 	 * @return 	Pulls any URLs that have get_authority_data = TRUE
 	 */
 	@SuppressWarnings("unchecked")
 	public List<URL> getUnProccessedAuthorityData(Integer maxResults){
 		
 		log.info("Entering getUnProccessedAuthorityData()");
 		
 		List<URL> urls = null; 
 		
 		EntityManagerFactory emf = PersistenceManager.getInstance().getEntityManagerFactory();
 		EntityManager mgr = emf.createEntityManager();
 		
 		try {
 
 			urls = (List<URL>) mgr.createQuery("SELECT u FROM URL u WHERE u.get_authority_data = TRUE")
 					.setMaxResults(maxResults)
 					.getResultList();
 
 		}catch(Exception e){
 			String msg = "Exception thrown. URLDBService: getUnProccessedAuthorityData";
 			log.severe(msg + e.getMessage());
 
 		} finally {
 			mgr.close();
 		}
 
 		return urls;
 	}
 	
 	/**
 	 * 
 	 * @param url
 	 * @return the SEOMoz metric: Domain Authority, if there's already a URL in the DB
 	 *  from that domain, - this saves hitting the SEOMoz server unnec.
 	 *  IF there's no url's with domain with the DB, then returns null
 	 */
 	public Integer getDomainAuthorityForThisDomain(String domainName){
 		
 		Integer result = null;
 //		String domain = "http://";
 //		domain += TextUtil.returnDomainName(url);
 		
 
 		EntityManagerFactory emf = PersistenceManager.getInstance().getEntityManagerFactory();
 		EntityManager mgr = emf.createEntityManager();
 
 		try { 
 			result = (Integer) mgr.createQuery("SELECT u.domain_authority FROM URL u WHERE u.domain_authority > 0 AND u.url_address LIKE :query")
 					.setMaxResults(1)
 					.setParameter("query", "%" + domainName + "%")
 					.getSingleResult();
 		
 		}catch(Exception e){
 			String msg = "Exception thrown. URLService: getDomainAuthorityForThisDomain";
 
 			log.info(msg + e.getMessage());
 
 		} finally {
 			mgr.close();
 		}
 
 		return result;
 	}
 
 	/*
 	 * returns all URLs that have a social_data_date less than or equal to todays date and get_social_data = true
 	 */
 	@SuppressWarnings("unchecked")
 	public List<URL> getTodaysSocialCrawlURLs() {
 
 		log.info("Entering getTodaysSocialCrawlURLs");
 		List<URL> result = null;
 		Date date = DateUtil.getTodaysDate();
 
 		EntityManagerFactory emf = PersistenceManager.getInstance().getEntityManagerFactory();
 		EntityManager mgr = emf.createEntityManager();
 		
 
 		try {
 			result = (List<URL>) mgr.createQuery("SELECT u FROM URL u WHERE u.get_social_data = true AND u.social_data_date <= :date ORDER BY u.social_data_date")
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
 	
 	@SuppressWarnings("unchecked")
 	public List<URL> getURLsOfClientCategory(Integer clientCategoryID, boolean targetURLsOnly){
 		
 		log.info("Entering getURLsOfClientCategory. Client Category = " + clientCategoryID);
 		List<URL> result = null;
 
 		EntityManagerFactory emf = PersistenceManager.getInstance().getEntityManagerFactory();
 		EntityManager mgr = emf.createEntityManager();
 		
 		try { 
 			if(targetURLsOnly){
 				result = (List<URL>) mgr.createQuery("SELECT u FROM URL u WHERE u.client_category_id = :client_category_id AND u.client_target_url = true")
 						.setParameter("client_category_id", clientCategoryID)
 						.getResultList();
 			}else{
 				result =(List<URL>) mgr.createQuery("SELECT u FROM URL u WHERE u.client_category_id = :client_category_id")
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
 	
 	@SuppressWarnings("unchecked")
 	public List<URL> getClientsTargetURLs(Integer clientID,  boolean targetURLsOnly){
 		
 		log.info("Entering getClientsURLs. Client ID = " + clientID);
 		List<URL> result = null;
 
 	
 		EntityManagerFactory emf = PersistenceManager.getInstance().getEntityManagerFactory();
 		EntityManager mgr = emf.createEntityManager();
 
 		try {
 			
 			if(targetURLsOnly){
 			result = (List<URL>) mgr.createQuery("SELECT u FROM URL u WHERE u.users_user_id = :users_user_id AND u.client_target_url = true")
 					.setParameter("users_user_id", clientID)
 					.getResultList();
 			}else{
 				result = (List<URL>) mgr.createQuery("SELECT u FROM URL u WHERE u.users_user_id = :users_user_id")
 						.setParameter("users_user_id", clientID)
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
 
 	//updates the corresponding URL in the DB with the client and campaign data passed as a parameter
 	//	and sets client_target_url = true.
 	public void updateURLClientAndCampaign(URL targetURLDB) {
 		
 		EntityManagerFactory emf = PersistenceManager.getInstance().getEntityManagerFactory();
 		EntityManager mgr = emf.createEntityManager();
 
 		try {
 
 			mgr.getTransaction().begin();
 
 			URL url = mgr.find(URL.class, targetURLDB.getId());
 			url.setClient_category_id(targetURLDB.getClient_category_id());
 			url.setUsers_user_id(targetURLDB.getUsers_user_id());
 			url.setClient_target_url(true);
 
 			mgr.merge(url);
 			mgr.getTransaction().commit();
 
 		}catch(Exception e){
 			String msg = "Exception thrown: URLDBService: updateURLClientAndCampaign";
 			log.severe(msg + e.getMessage());
 
 		}  finally {
 			mgr.close();
 		}
 
 		
 	}
 
 	@SuppressWarnings("unchecked")
 	public List<String> getURLAddressesStartingWith(String query, int noOfResults) {
 		
 		//log.info("Entering getURLAddresses");
 		List<String> result = null;
 
 		EntityManagerFactory emf = PersistenceManager.getInstance().getEntityManagerFactory();
 		EntityManager mgr = emf.createEntityManager();
 
 		try { 
 			result= (List<String>) mgr.createQuery("SELECT u.url_address FROM URL u where u.url_address LIKE :query")
 					.setMaxResults(noOfResults)
 					.setParameter("query",query + "%")
 					.getResultList();
 		
 		}catch(Exception e){
 			String msg = "Exception thrown. URLService: getURLAddresses";
 
 			log.severe(msg + e.getMessage());
 
 		} finally {
 			mgr.close();
 		}
 
 		return result;
 		
 	}
 
 	@SuppressWarnings("unchecked")
 	public List<URL> getTodaysSocialCrawlURLs(int noOfResults) {
 		log.info("Entering getTodaysSocialCrawlURLs");
 		List<URL> result = null;
 		Date date = DateUtil.getTodaysDate();
 
 		EntityManagerFactory emf = PersistenceManager.getInstance().getEntityManagerFactory();
 		EntityManager mgr = emf.createEntityManager();
 		
 
 		try {
 			result =  (List<URL>) mgr.createQuery("SELECT u FROM URL u WHERE u.get_social_data = true AND u.social_data_date <= :date ORDER BY u.social_data_date")
 					.setParameter("date", date, TemporalType.DATE)
 					.setMaxResults(noOfResults)
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
 
 	// see: http://en.wikibooks.org/wiki/Java_Persistence/Querying#Pagination.2C_Max.2FFirst_Results
 	// for limiting results... and ordering correclty...
 	@SuppressWarnings("unchecked")
 	public List<URL> getBackLinkURLs(int targetURLId, int limitStart,
 			Integer limitEnd) {
 		log.info("Entering getBackLinkURLs: Start = " + Integer.toString(limitStart) + ". End = " + Integer.toString(limitEnd));
 		List<URL> result = null;
 		
 		EntityManagerFactory emf = PersistenceManager.getInstance().getEntityManagerFactory();
 		EntityManager mgr = emf.createEntityManager();
 		
 		Query query = mgr.createQuery("SELECT u FROM URL u WHERE u.url_id = ANY (SELECT l.source_id FROM Link l WHERE l.target_id = :id) ORDER BY u.url_id");
 		query.setParameter("id", targetURLId);
 		query.setFirstResult(limitStart);
 		query.setMaxResults(limitEnd);
 
 		try {   
 			
 			result= query.getResultList();
 			log.info("getBackLinkURLs: No Of Results: " + Integer.toString(result.size()));
 			
 			
 		}catch(Exception e){
 			String msg = "Exception thrown. URLService: getBackLinkURLs. ";
 
 			log.severe(msg + e.getMessage());
 
 		} finally {
 			mgr.close();
 		}
 			
 		return result; 
 	}
 	
 	/**
 	 * 
 	 * @param links list of links that we want the urls
 	 * @return - list of urls that are the source urls in the list of links passed
 	 */
 	@SuppressWarnings("unchecked")
 	public List<URL> getBackLinkURLs(List<Integer> sourceIDs) {
 		
 		log.info("Entering getBackLinkURLs - list of links version");
 		List<URL> result = null;
 		
 		EntityManagerFactory emf = PersistenceManager.getInstance().getEntityManagerFactory();
 		EntityManager mgr = emf.createEntityManager();
 		
 		Query query = mgr.createQuery("SELECT u FROM URL u WHERE u.url_id IN :sourceIDs ORDER BY u.url_id")
 										.setParameter("sourceIDs", sourceIDs);
 		
 		try{
 			
 			result= query.getResultList();
 
 		}catch(Exception e){
 			String msg = "Exception thrown. URLService: getBackLinkURLs (Link List version) ";
 
 			log.severe(msg + e.getMessage());
 
 		} finally {
 			mgr.close();
 		}
 		
 		
 		
 		return result;
 		
 	}
 		
 	
 	
 	@SuppressWarnings("unchecked")
 	public List<URL> getURLsWithImage() {
 		log.info("Entering getURLsWithImage");
 		List<URL> result = null;
 	
 		EntityManagerFactory emf = PersistenceManager.getInstance().getEntityManagerFactory();
 		EntityManager mgr = emf.createEntityManager();
 		
 
 		try {
 			result =  (List<URL>) mgr.createQuery("SELECT u FROM URL u WHERE u.get_image > 0")
 					.getResultList();
 
 		}catch(Exception e){
 			String msg = "Exception thrown. URLService: getURLsWithImage";
 
 			log.severe(msg + e.getMessage());
 
 		} finally {
 			mgr.close();
 		}
 		if (result == null) {
 			log.info("No URLs with images returned");
 		}
 		log.info("Exiting getURLsWithImage");
 		return result;
 	}
 	
 	public void updateURLImageGot(Integer url_id, Integer getImageType){
 		
 		EntityManagerFactory emf = PersistenceManager.getInstance().getEntityManagerFactory();
 		EntityManager mgr = emf.createEntityManager();
 		
 		try {
 
 			mgr.getTransaction().begin();
 
 			URL url = mgr.find(URL.class, url_id);
 			url.setGet_image(getImageType);
 
 			mgr.merge(url);
 			mgr.getTransaction().commit();
 
 		}catch(Exception e){
 			String msg = "Exception thrown: URLService: getImageType";
 			log.severe(msg + e.getMessage());
 
 		}  finally {
 			mgr.close();
 		}
 
 
 	}
 
 	public void getPremierLeagueURLs() {
 		// TODO  add field to DB to highlight these urls???
 		// as these urls will be different to others currently in there.... - just looking for 1 link per unique domain...
 		
 		// for now - to test return those urls that have get_backinkink set to true...
 		
 		
 		
 	}
 	
 	public Integer noOfClientURLs(Integer clientID){
 		
 		EntityManagerFactory emf = PersistenceManager.getInstance().getEntityManagerFactory();
 		EntityManager mgr = emf.createEntityManager();
 		
 		Integer result = null;
 		
 		try {
 
 			result =  (Integer) mgr.createQuery("SELECT COUNT(u) FROM URL u WHERE u.users_user_id = :clientID")
 			.setParameter("clientID",  clientID)
 			.getSingleResult();
 
 
 
 		}catch(Exception e){
 			String msg = "Exception thrown: URLService: noOfClientURLs";
 			log.severe(msg + e.getMessage());
 
 		}  finally {
 			mgr.close();
 		}
 		
 		return result;
 		
 	}
 
 }
