 package com.gofetch.controllers;
 
 import java.io.IOException;
 import java.util.List;
 import java.util.logging.Logger;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import com.gofetch.entities.URL;
 import com.gofetch.entities.URLDBService;
 import com.gofetch.seomoz.Constants;
 import com.gofetch.seomoz.SEOMoz;
 import com.gofetch.seomoz.SEOMozImplFreeAPI;
 import com.gofetch.seomoz.URLPlusDataPoints;
 import com.gofetch.utils.TextUtil;
 /**
  * 
  * @author alandonohoe
  * Pulls all urls, that have not yet got Domain and Page authority data
  * and gets this from SEOMoz service, updating the URL data in the database
  */
 public class DomainPageAuthorityCrawl extends HttpServlet{
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 
 	private static Logger log =  Logger.getLogger(DomainPageAuthorityCrawl.class.getName());
 
 	@Override
 	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
 			throws ServletException, IOException {

 		log.info("Entering DomainPageAuthorityCrawl()");
 
 		List<URL> unprocessedURLs = null;
 		URLDBService urlDBUnit = new URLDBService();
 		SEOMoz seoMoz = new SEOMozImplFreeAPI();
 		boolean firstRun = true; // only here for the free SEOMoz API limiter.
 		String domain;
 		Integer noOfURLs; 
 		Integer counter = 1;

 		unprocessedURLs = urlDBUnit.getUnProccessedAuthorityData(500);
 		noOfURLs = unprocessedURLs.size();

 		log.info("No of Unprocessed URLs: " + noOfURLs);
 
 		for (URL currentURL : unprocessedURLs) {
 
 			Integer da = null;
 			//////
 			// first hit local DB - to see if domain already exists with a score..
 
 			domain = currentURL.getDomain();
 			log.info(counter + " of " + noOfURLs + " Current Domain: " + domain);

 			if(null == domain){
 				//TODO: update/persist the new domain name?
 				currentURL.setDomain(TextUtil.returnDomainName(currentURL.getUrl_address())); 
 			}
 
			// first get DA for this domain from GoFetch system (before hitting SeoMoz)
 			da = urlDBUnit.getDomainAuthorityForThisDomain(currentURL.getDomain()); 
 
 			if(null!= da){
				try {
					urlDBUnit.updateURLDomainAuthorityData(currentURL.getUrl_address(), da);
				} catch (Exception e) {
					log.severe(e.getMessage());
				}
 
 				log.info("Got DA (" + da + ") from DB");
 			}else{
 
 				log.info("Getting DA from SEOMoz");
 				///////
 				// 2nd - hit SEOMoz API for score....
 				// Wait for nec SEOMoz API delay....
 				if (!firstRun) {
 					try {
 						Thread.sleep(Constants.FREE_API_SEOMOZ_SERVER_DELAY);
 					} catch (InterruptedException e) {
 						log.warning( e.getMessage());
 					}
 				}
 				firstRun = false;
 
 				try{
 					getAuthorityAndDomainData(currentURL.getUrl_address());
 				}catch(Exception e) {
 					String msg = "Exception thrown getting authority data for: "
 							+ currentURL.getUrl_address() + "DomainPageAuthorityCrawl - getAuthorityAndDomainData(...)";
 					log.warning(msg);
 					log.warning(e.getMessage());
 
 				}
 				//
 				////////
 
 
 
 			}
 			counter++;
 		}
 
 
 	}
 
 
 	/**
 	 * retrieves the PA and DA of the target url from the SEOMoz API, and persists them to the DB.
 	 * 
 	 * @param currentURL
 	 *            - url to retrieve and persist the domain and page authority of
 	 */
 	@SuppressWarnings("unused")
 	private void getAuthorityAndDomainData(String url) {
 
 		log.info("Entering getAuthorityAndDomainData");

 		URLDBService urlDBUnit = new URLDBService();
 		URLPlusDataPoints currentURLDAPA, resultingURLDAPA = null;
 		String docTitle, miniDocTitle; // need to keep this length under the 45
 		// varchar of the DB...
 		SEOMoz seoMoz = new SEOMozImplFreeAPI();
 
 		// need to create a URLPlusDataPoints object to communicate with SEOMoz
 		// interface
 		currentURLDAPA = new URLPlusDataPoints();
 
 		currentURLDAPA.setBackLinkURL(url);
 
 		try {
 
 			resultingURLDAPA = seoMoz.getURLMetricsData(currentURLDAPA);
 		} catch (Exception e) {
 			// deal with SEOMoz server time out
 			String msg = "Exception thrown getting getAuthorityAndDomainData for "
 					+ url + "\n";
 
 			log.warning(msg + e.getMessage());
 
 			return;
 		}
 
 		if(null != resultingURLDAPA){
 
 			docTitle = resultingURLDAPA.getDocTitle();
 
 			if(null!=docTitle){
 
 				if (docTitle.length() > 44)
 					miniDocTitle = resultingURLDAPA.getDocTitle().substring(0, 44);
 				else
 					miniDocTitle = docTitle;
 			}else{
 				miniDocTitle = "";
 			}
 
 			// now persist the domain and page authority to the correct url in
 			// the DB.
 			urlDBUnit.updateURLData(url, resultingURLDAPA.getBackLinkPAInt(),
 					resultingURLDAPA.getBackLinkDAInt(), miniDocTitle);
 		} else {
 
 			String msg = "SEOMoz server not returning Authority data for:  "
 					+ url + "\n";
 
 			log.warning(msg);
 		}
 
 	}
 
 
 
 }
