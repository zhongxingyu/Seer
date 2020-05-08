 package com.gofetch.controllers;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Collections;
 import java.util.Date;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.*;
 
 import com.gofetch.GoFetchConstants;
 import com.gofetch.entities.*;
 import com.gofetch.seomoz.*;
 import com.gofetch.socialdata.*;
 import com.gofetch.utils.ConnectionUtil;
 import com.gofetch.utils.DateUtil;
 import com.gofetch.utils.TextUtil;
 
 public class ProcessNewTargets extends HttpServlet {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L; 
 
 	private static Logger log = Logger.getLogger(ProcessNewTargets.class
 			.getName());
 
 	/**
 	 * Pre-conditions:
 	 * 
 	 * Post-conditions: upon exit, all the target urls that were entered by
 	 * user(s) will: 
 	 * 1) have all their backlinks (known by SEOMoz server at least) entered into the url table. And if the call to SEOMoz is successful, then these target url's 
 	 * 		backlink_got field's are set to true.
 	 * 2) all these backlinks will
 	 * have the same get_social_data value as their target url. true/false. 
 	 * 3)
 	 * for every backlink, an entry will be made into the links table, which
 	 * records source and target url for each link 
 	 * 4) for every entry into the
 	 * link table, a final_target_url will be assigned from each target url to
 	 * source url. - this will be the url address of the final target in the
 	 * tree of linked urls. = The root of the tree, which is - the url that was
 	 * originally entered by the user. This connects the cascade of urls which
 	 * simplifies deleting - or modifying the series of links pointing to
 	 * user-entered target url.
 	 * 
 	 */
 	@Override
 	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
 			throws IOException {
 
 		resp.setContentType("text/plain");
 
 		boolean firstRun = true; // only here for the free SEOMoz API limiter.
 
 		String currentTargetAddress;
 		String reportSummary; // used to either email urls' "owner" via their
 		// user_id, or write to log/report after the
 		// completion of each run
 
 		List<URLPlusDataPoints> backLinks = null;
 		URLDBService urlDBUnit = new URLDBService();
 		SEOMoz seoMoz = new SEOMozImplFreeAPI();	
 		
 		/////////
 		// SEOMoz processing of URLs.
 
 
 		//new implementation: returns all the urls that have get_backlinks = true && backlinks_got = false
 		List<URL> unprocessedURLs = urlDBUnit.getUnproccessedTargetURLs();
 
 
 		if (!unprocessedURLs.isEmpty()) {
 			for (URL currentURL : unprocessedURLs) {
 				//if (currentURL.isGet_backlinks()) { - already taken into account in the getUnproccessedTargetURLs(); call
 
 					boolean getLinksSuccessful = true;
 
 					currentTargetAddress = currentURL.getUrl_address();
 
 					if (!firstRun) {
 						try {
 							Thread.sleep(Constants.FREE_API_SEOMOZ_SERVER_DELAY);
 						} catch (InterruptedException e) {
 
 							log.warning( e.getMessage());
 
 						}
 					}
 
 					firstRun = false;
 					try {
 
 						backLinks = seoMoz.getLinks(currentTargetAddress);
 					} catch (Exception e) {
 						String msg = "Exception thrown getting backlink data for: "
 								+ currentTargetAddress + "ProcessNewTargets"
 								+ "- SEOMoz block. Exception - ";
 						log.warning(msg + e.getMessage());
 
 						getLinksSuccessful = false;
 
 					}
 					// this section below ONLY called if we have has a successful call to SEOMoz - even if there's no links for this target.
 					if(getLinksSuccessful){
 						// 2: for each new target - get its page & domain
 						// authority... .. and persist...
 						// : another hit to SEOMoz here.. need to delay....
 						try {
 							Thread.sleep(Constants.FREE_API_SEOMOZ_SERVER_DELAY);
 						} catch (InterruptedException e) {
 
 							log.warning( e.getMessage());
 						}
 						try{
 							getAuthorityAndDomainData(currentURL.getUrl_address(),
 									seoMoz);
 						}catch(Exception e) {
 							String msg = "Exception thrown getting authority data for: "
 									+ currentTargetAddress + "ProcessNewTargets"
 									+ "- SEOMoz block. Exception - ";
 							log.warning(msg + e.getMessage());
 
 						}
 
 						if (!backLinks.isEmpty()) { // if we have successfully
 							// got some data from SEOmoz
 							// TODO: write the new URLs and new Links as batch
 							// writes to DB ASAP...
 							// 3: write each backlink to url table. - with same
 							// data as target, BUT NOT get backlink data always
 							// = false.
 							try{
 								linksToNewSEOMozURLs(backLinks, currentURL);
 
 								// 4: for each new source url - retrieve it's id and
 								// the target's id from DB and
 								// create new entry in link table, with target id
 								// and source id, anchor text, todays date and
 								// domain name extracted from url address.
 								linksToNewLinks(backLinks, currentURL);
 
 							}catch(Exception e){
 								log.warning("Error in creating new links / urls. "  + e.getMessage());
 							}
 
 							// 5: Get PA and DA for single link from each unique
 							// domain....
 							// use getURLsPointingTo(currentURL) and retrieve
 							// first url, get its PA and DA... then loop through
 							// the next urls until currentBackLinkdomainName !=
 							// latestBackLinkDomainName
 							// then get PA and DA for that.....
 							// then queries to only unique domain backlinks can
 							// query those with only PA & DA that != null ????
 
 							// another hit to SEOMoz here.. need to delay....
 							try {
 								Thread.sleep(Constants.FREE_API_SEOMOZ_SERVER_DELAY);
 							} catch (InterruptedException e) {
 								log.warning( e.getMessage());
 							}
 							getDataForUniqueDomains(currentURL, seoMoz);
 
 						} else {
 							log.info("No backlinks from SEOMoz for: "
 									+ currentTargetAddress);
 						}
 
 						// change flag - so this will not be included in further calls for backlinks
 						urlDBUnit.updateBackLinksGot(currentURL, true);
 
 					}
 
 				//} // end of......... if (currentURL.isGet_backlinks()) 
 			}
 
 		} else {
 
 			reportSummary = "No Target URLs entered yesterday.\n";
 
 			log.info("No Target URLs entered yesterday.");
 		}
 		// end of SEOMoz processing URLs
 		// /////////////
 
 		//////////////////////////////////
 		// Social metric crawl / processing of URLs...
 		// Now we have all the backlinks from SEOMoz entered into the DB, now
 		// get all the social data urls in DB that have FB and Twitter data
 		// selected...
 		//TODO: move this to another servlet - and break it into daily, weekly and monthly checks.
 		getURLSocialData();
 
 		try {
 			getServletContext().getRequestDispatcher("/index.jsf").forward(
 					req, resp);
 		} catch (ServletException e) {
 
 			// String msg = "Exception thrown...";
 			// logger.logp(Level.SEVERE, "ProcessNewTargets",
 			// "getRequestDispatcher",msg ,e);
 			String msg = "Exception thrown in ProcessNewTargets\n";
 			log.severe(msg + e.getMessage());
 
 		}
 	}
 
 	@Override
 	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
 			throws ServletException, IOException {
 
 		doGet(req, resp);
 	}
 
 	/**
 	 * Writes backLinks list as new URLs to url table in database. There may be
 	 * a case where a backlink may already exist as a url in the table, - when
 	 * it points to another target already. - In this case, then do not add it
 	 * again to the url table, but just use its url_id later to add just the new
 	 * link to the link table.
 	 * 
 	 * @param backLinks
 	 * @param currentURL
 	 * @param urlDBUnit
 	 */
 	private void linksToNewSEOMozURLs(List<URLPlusDataPoints> backLinks,
 			URL currentURL) {
 
 		URLDBService urlDBUnit = new URLDBService();
 		String domainName;
 		String urlPlusHttp;
 		Date todaysDate = DateUtil.getTodaysDate();
 
 		//////////////////
 		// comment this out for now..... think about moving this functionality to checkboxes in results table
 		//		so you can select specific links rather than general next layer...??
 		//		
 		//		// reduce the number of layers by 1, every round of backlinks, until we
 		//		// are at 0.
 		Integer newNoOfLayers = currentURL.getNo_of_layers();
 		//		if (newNoOfLayers > 0)
 		//			newNoOfLayers--;
 
 		newNoOfLayers = 0;
 		//////////////////////
 
 		for (URLPlusDataPoints currentBackLink : backLinks) {
 
 			domainName = TextUtil.returnDomainName(currentBackLink
 					.getBackLinkURL());
 			// SEOMoz returns urls with no http:// prefix
 			urlPlusHttp = TextUtil.addHTTPToURL(currentBackLink
 					.getBackLinkURL());
 
			// make sure the url has a trailing slash - to standardize all
			// entries in DB,
			// this removes the possibility of bbc.co.uk and bbc.co.uk/ both
			// being entered.
			//urlPlusHttp = TextUtil.addSlashToEndOfString(urlPlusHttp);

 			URL url = new URL();
 			url.setUrl_address(urlPlusHttp);
 			url.setDate(todaysDate);
 			url.setGet_social_data(currentURL.isGet_social_data());
 			
 			url.setSocial_data_freq(GoFetchConstants.DAILY_FREQ);
 
 			url.setDomain(domainName);
 			url.setSeomoz_url(true);
 			url.setBacklinks_got(false);
 
 			//TODO: THIS IS SET TO ZERO for now - up above, as want to move this to the datatable page.
 			url.setNo_of_layers(newNoOfLayers);
 			if (newNoOfLayers > 0)
 				url.setGet_backlinks(true); // this is a source URL, not a
 			// target.
 			else
 				url.setGet_backlinks(false); // this is NOT a target URL, JUST  a source
 
 			urlDBUnit.createURL(url);
 
 		}
 	}
 
 	/**
 	 * Retrieves the id assigned to each link in backLinks list, and the id
 	 * assigned to target current URL and writes both as new link to the links
 	 * table in the DB, with today's date as date detected parameter.
 	 * 
 	 * The link table can have entries with duplicate backlink entries and
 	 * duplicate target entries but NOT in the same link entry. Every link entry
 	 * has to be unique in terms of combined source and target url.
 	 * 
 	 * 
 	 * @param backLinks
 	 * @param currentURL
 	 * @param urlDBUnit
 	 * 
 	 *            Pre-conditions: the target url has not been entered before.
 	 *            the links to the target url have not been entered into the
 	 *            links table already.
 	 * 
 	 *            Post-conditions: any new backlinks not already entered in the
 	 *            links table will be now entered into the table any backlinks
 	 *            already in the table will be unaffected.
 	 */
 	//@SuppressWarnings("unused") 
 	private void linksToNewLinks(List<URLPlusDataPoints> urlBackLinks,
 			URL currentURL) {
 
 		if (urlBackLinks.isEmpty())
 			return;
 
 		LinkDBService linksDBUnit = new LinkDBService();
 		URLDBService urlDBUnit = new URLDBService();
 
 		List<Integer> currentLinkIDs;
 		List<String> currentLinksAddresses = new ArrayList<String>();
 		URL backLinkURL;
 		String httpURL;
 
 		Date today = DateUtil.getTodaysDate();
 
 		// TODO: this seems v inefficent - the two steps....- need to look into
 		// alternatives... using SQL... joins??
 
 		// get all the links pointing to target
 		currentLinkIDs = linksDBUnit.getLinkIDsPointingTo(currentURL);
 
 		// fill list of all url addresses of links pointing at target
 		if (!currentLinkIDs.isEmpty()) {
 			for (Integer currentLinkID : currentLinkIDs) {
 
 				currentLinksAddresses.add(urlDBUnit
 						.getURLAddressFromID(currentLinkID));
 			}
 		}
 
 
 		// run through all the backlinks....
 		for (URLPlusDataPoints currentURLBackLink : urlBackLinks) {
 
 			// check that current backLink does not exist in target's links list
 			if (!currentLinksAddresses.contains(currentURLBackLink
 					.getBackLinkURL())) {
 
 				// then finally... create new link...
 
 				// dealing with SEOMoz supplied urls - so add http://...
 				httpURL = TextUtil.addHTTPToURL(currentURLBackLink
 						.getBackLinkURL());
 
 				Link link = new Link();
 
 				backLinkURL = urlDBUnit.getURL(httpURL);
 
 				//////////////////
 				//TODO: delete: testing.
 				if(null == backLinkURL)
 					log.warning("backLinkURL == null ");
 				////////////
 
 				link.setSource_id(backLinkURL.getId());
 				link.setTarget_id(currentURL.getId());
 				link.setAnchor_text(currentURLBackLink.getBackLinkAnchorText());
 				link.setDate_detected(today);
 				link.setFinal_target_url(currentURL.getUrl_address());
 
 				linksDBUnit.createLink(link);
 			}
 
 		}
 	}
 
 	/**
 	 * retrieves the PA and DA of the target url, and persists them to the DB.
 	 * 
 	 * @param currentURL
 	 *            - url to retrieve and persist the domain and page authority of
 	 */
 	@SuppressWarnings("unused")
 	private void getAuthorityAndDomainData(String url, SEOMoz seoMoz) {
 
 		URLDBService urlDBUnit = new URLDBService();
 		URLPlusDataPoints currentURLDAPA, resultingURLDAPA = null;
 		String docTitle, miniDocTitle; // need to keep this length under the 45
 		// varchar of the DB...
 
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
 		}
 
 		//		if ((null != resultingURLDAPA.getDocTitle())
 		//				|| (null != resultingURLDAPA)) {
 		//			docTitle = resultingURLDAPA.getDocTitle();
 		//			
 		// replaced the above code with:
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
 			// resultingURLDAPA = null
 			String msg = "SEOMoz server not returning Authority data for:  "
 					+ url + "\n";
 			// logger.logp(Level.SEVERE, "ProcessNewTargets",
 			// "getAuthorityAndDomainData",msg);
 			log.warning(msg);
 		}
 
 	}
 
 	private List<URL> getURLsPointingTo(URL targetURL) {
 
 		List<Integer> ids;
 		List<URL> urls = new ArrayList<URL>();
 		LinkDBService linkDBUnit = new LinkDBService();
 		URLDBService urlDBUnit = new URLDBService();
 
 		// ids = linkDBUnit.getLinkIDsPointingTo(targetURL);
 
 		ids = linkDBUnit.getSourceURLsIDsPointingTo(targetURL);
 
 		for (Integer i : ids) {
 			urls.add(urlDBUnit.getURL(i));
 		}
 
 		return urls;
 
 	}
 
 	/**
 	 * Fills the first link from each unique domain pointing to the target url
 	 * // Get PA and DA for single link from each unique domain.... // use
 	 * getURLsPointingTo(currentURL) and retrieve first url, get its PA and
 	 * DA... then loop through the next urls until currentBackLinkdomainName !=
 	 * latestBackLinkDomainName // then get PA and DA for that..... // then
 	 * queries to only unique domain backlinks can query those with only PA & DA
 	 * that != null ????
 	 * 
 	 * 
 	 * @param targetURL
 	 *            - target url
 	 * @param seoMoz
 	 */
 	private void getDataForUniqueDomains(URL targetURL, SEOMoz seoMoz) {
 
 		List<URL> urls = getURLsPointingTo(targetURL);
 		List<String> uniqueDomains = new ArrayList<String>();
 
 		String currentDomain;
 
 		if (null == urls)
 			return;
 
 		if (urls.isEmpty())
 			return;
 
 		// initialise unique domains with first domain and get its data
 		uniqueDomains.add(urls.get(0).getDomain());
 		getAuthorityAndDomainData(urls.get(0).getUrl_address(), seoMoz);
 
 		// loop through rest of the urls and get authority data for first link
 		// from unique domain encountered
 
 		for (int i = 1; i < urls.size(); i++) {
 
 			currentDomain = urls.get(i).getDomain();
 
 			if (!uniqueDomains.contains(currentDomain)) {
 				// add to unique domains
 				uniqueDomains.add(currentDomain);
 
 				// and get data
 				try {
 					Thread.sleep(Constants.FREE_API_SEOMOZ_SERVER_DELAY);
 				} catch (InterruptedException e) {
 					String msg = "Exception thrown: ProcessNewTargets - getDataForUniqueDomains \n";
 					// logger.logp(Level.SEVERE, "ProcessNewTargets",
 					// "getDataForUniqueDomains",msg ,e);
 					log.warning(msg + e.getMessage());
 
 				}
 
 				try {
 					getAuthorityAndDomainData(urls.get(i).getUrl_address(),
 							seoMoz);
 				} catch (Exception e) {
 					String msg = "Exception thrown getting data for "
 							+ urls.get(i).getUrl_address()
 							+ " ProcessNewTargets: getDataForUniqueDomains \n";
 					log.warning(msg + e.getMessage());
 
 				}
 			}
 		}
 
 	}
 
 	/**
 	 * retrieves a list of all urls in DB that have had their get "getSocialData
 	 * checkbox checked then checks each url to see if their current associated
 	 * data (FB & twitter) is different to what is saved previously in the DB.
 	 * If it is different: a new social entry is created with yesterdays date,
 	 * and the url's current data is persisted.
 	 */
 	private void getURLSocialData() {
 
 		// 1: get list of all urls that have "get_social_data" checked...
 
 		String urlAddressNoSlash, urlAddressSlash, urlAddress;
 
 		// URL database communication
 		URLDBService urlDBUnit = new URLDBService();
 
 		// urls back from DB
 		List<URL> urls;
 
 		// used for social data database communication
 		MiscSocialDataDBService socialDataDBUnit = new MiscSocialDataDBService();
 		MiscSocialData socialDataFromDB = null;
 
 		// used to get current social data for urls using external APIs
 		SocialData sharedCountAPI = new SharedCountImpl();
 		// used as the more costly, manual backup social data service	
 		SocialData manualSocialData = new ManualSocialDataImpl();
 
 		MiscSocialData socialDataAPINoSlash = null;
 		MiscSocialData socialDataAPISlash = null;
 		MiscSocialData assimilatedSocialData = null;
 
 		//urls = urlDBUnit.getSociallyTrackedURLs();
 		
 		// replaced the above with:
 		// get urls where get social data = true 
 		//	and social data date <= todays date (not just = today's date, in case there was an error with updating the social date some urls would be left behind & lost)
 		urls = urlDBUnit.getTodaysSocialCrawlURLs();
 		
 		
 		// 2: loop through checking to see if their social data has changed from
 		// previous entry... update if true.
 
 		String noOfSocialURLs = String.valueOf(urls.size());
 		log.info("No of URLs to get social data for: " + noOfSocialURLs );
 		int i = 1;
 
 		for (URL currentURL : urls) {
 
 			// reset on every loop
 			socialDataAPINoSlash = null;
 			socialDataAPISlash = null;
 			assimilatedSocialData = null;
 			boolean manualSocialDataGot = true; 
 			boolean sharedCountFailedSlash = false; 
 			boolean sharedCountFailedNoSlash = false; 
 			double pcDifferenceBtwnSocialData;
 
 			urlAddress = currentURL.getUrl_address();
 
 			log.info(String.valueOf(i) + " of " + noOfSocialURLs + " : " + urlAddress);
 
 			// remove final / if present...
 			if(urlAddress.endsWith("/")){
 				urlAddress = urlAddress.substring(0,(urlAddress.length() -1));
 			}
 
 			urlAddressNoSlash = urlAddress;
 			urlAddressSlash = urlAddress + "/";
 
 			///////////
 			// shared count API
 			// get current social data from the url... first - WITH the trailing slash
 			try {
 
 				socialDataAPISlash = sharedCountAPI.getAllSocialData(urlAddressSlash);
 				//also - have to get StumbleUpon data manually... as sharedcount is failing to get this data
 				socialDataAPISlash.setStumble_upon(manualSocialData.getStumbleUponData(urlAddressSlash));
 
 			} catch (Exception e) {
 				String msg = "Shared Count API failed for: "
 						+ urlAddressSlash
 						+ ". ProcessNewTargets: getURLSocialData. Calling manual implementation \n";
 				log.warning(msg + e.getMessage());
 
 				sharedCountFailedSlash = true; // so now call manual implementation
 			}
 
 			// get current social data from the url... - now without the trailing slash
 			try {
 				socialDataAPINoSlash = sharedCountAPI.getAllSocialData(urlAddressNoSlash);
 
 				//also - have to get StumbleUpon data manually... as sharedcount is failing to get this data
 				socialDataAPINoSlash.setStumble_upon(manualSocialData.getStumbleUponData(urlAddressNoSlash));
 
 			} catch (Exception e) {
 				String msg = "Shared Count API failed for: "
 						+ urlAddressNoSlash
 						+ ". ProcessNewTargets: getURLSocialData. Calling manual implementation \n";
 				log.warning(msg + e.getMessage());
 
 				sharedCountFailedNoSlash = true; // so now call manual implementation
 			}
 			// end shared count API
 			//////////
 
 
 			////////////
 			// manual social data service
 			if (sharedCountFailedSlash) {
 
 				try {
 
 					socialDataAPISlash = manualSocialData.getAllSocialData(urlAddressSlash);
 
 				} catch (Exception e) {
 					String msg = "Manual Implementation Social data for: "
 							+ urlAddressSlash + " failed"
 							+ ". ProcessNewTargets: getURLSocialData.  \n";
 					log.severe(msg + e.getMessage());
 
 					manualSocialDataGot = false;
 				}
 			}
 
 			if (sharedCountFailedNoSlash) {
 
 				try {
 
 					socialDataAPINoSlash = manualSocialData.getAllSocialData(urlAddressNoSlash);
 
 				} catch (Exception e) {
 					String msg = "Manual Implementation Social data for: "
 							+ urlAddressNoSlash + " failed"
 							+ ". ProcessNewTargets: getURLSocialData. \n";
 					log.severe(msg + e.getMessage());
 
 					manualSocialDataGot = false;
 				}
 			}
 			// end manual implementation....
 			///////////////////
 
 
 			if(manualSocialDataGot){ // this is ONLY false, if all calls to the social APIs have failed - v unlikely..
 
 				//////
 				// assimilate the two collection of Social data metrics (from both url + slash and url no slash)
 				//	
 				assimilatedSocialData = assimilateSocialData(socialDataAPINoSlash, socialDataAPISlash);
 
 				// get most recently persisted social data.....
 				socialDataFromDB = socialDataDBUnit.getMostRecentSocialData(currentURL.getId());
 
 				// if social data from DB, is empty or different from most
 				// recent social data from APIs... then persist new social data
 
 				pcDifferenceBtwnSocialData = persistSocialData(socialDataFromDB, assimilatedSocialData, socialDataDBUnit, currentURL.getId());
 				
 				updateSocialCrawlFrequency(currentURL, pcDifferenceBtwnSocialData, urlDBUnit);
 
 				//TODO: here add the code that will check and set the frequency of check_freq: for every url.
 				//	daily = 1, weekly = 2, monthly =3, daily - get all urls that have check_freq < 2, weekly get all urls with check_freq < 3, etc
 				// checkURLsSocialCheckFreq() {update url increase / decrease freq. and email "owner" if there's a spike - or on monthly
 				//	when there's no change from one month to another - email owner - to ask if they want to delete URL}
 
 			} else { // if all calls to the social APIs have failed
 
 				// TODO: record urls and email/ alert user (using their email) /
 				// error screen somehow that of this list.....
 				String msg = "Retrieving social data for: "
 						+ urlAddress + " completely failed. -  ProcessNewTargets: getURLSocialData ";
 
 				log.severe(msg);
 
 
 				// maybe check here for a 404 - and if true - then delete url?
 			}
 		}
 	}
 	
 	private void updateSocialCrawlFrequency(URL currentURL,
 			double pcDifferenceBtwnSocialData, URLDBService urlDBUnit) {
 		
 		int socialFreq = currentURL.getSocial_data_freq();
 		
 		//need to check if it just new URL with no previous social data - so pcDifference would be set to 0, but shouldnt be...
 		if(pcDifferenceBtwnSocialData < 0){
 			currentURL.setSocial_data_date(DateUtil.getTommorrowsDate());
 			urlDBUnit.updateSocialFrequencyData(currentURL);
 			return;
 		}
 		
 		if(pcDifferenceBtwnSocialData < GoFetchConstants.SOCIAL_DATA_RANGE_MIN){
 			
 			currentURL.decreaseSocialCrawlFrequency();
 
 		}
 		
 		if(pcDifferenceBtwnSocialData > GoFetchConstants.SOCIAL_DATA_RANGE_MAX){
 
 			currentURL.setSocial_data_freq(GoFetchConstants.DAILY_FREQ);
 		}
 		
 		// now update the URL's next date to be included in the social data crawl
 		// if may of been increased or decreased:
 		socialFreq = currentURL.getSocial_data_freq();
 			
 		if(GoFetchConstants.DAILY_FREQ==socialFreq){
 			// then make social data date tmw.
 			currentURL.setSocial_data_date(DateUtil.getTommorrowsDate());
 			
 		}
 		
 		if(GoFetchConstants.WEEKLY_FREQ==socialFreq){
 			// then make social data date next week.
 			currentURL.setSocial_data_date(DateUtil.getNextWeeksDate());
 		}
 		
 		if(GoFetchConstants.MONTHLY_FREQ==socialFreq){
 			
 			//TODO: implement this...
 			if(0 ==pcDifferenceBtwnSocialData){
 				//then.. check difference btwn dates?? and if there's x days or weeks.. set getSocialData(false)???
 				// this is typically socially dead backlinks...
 				
 			}
 			
 			// then make social data date next month.
 			currentURL.setSocial_data_date(DateUtil.getNextMonthsDate());
 		}
 		
 		urlDBUnit.updateSocialFrequencyData(currentURL);
 		
 		
 	}
 
 	/* If there is new social data - then persists this...
 	 * returns the percentage difference between the new social data and the previous entry. Returns 0, if no change and -1 if no previous entry in DB for this URL. 
 	 */
 	private double persistSocialData(MiscSocialData socialDataFromDB, MiscSocialData assimilatedSocialData, MiscSocialDataDBService socialDataDBUnit,  Integer currentURLID){
 
 		
 		//used as running total of all social data- so we can check the %age difference below
 		double totalSocialDataDB, totalSocialDataToday; 
 		double pcDifference;
 		Integer deliciousDB, deliciousToday, fbTotalCountDB, fbTotalCountToday, googlePlusDB, googlePlusToday, 
 				linkedInDB, linkedInToday, pinterestDB, pinterestToday, stumbleUponDB, stumbleUponToday, twitterDB, twitterToday;
 		
 		// if there's no social data for this URL in DB yet - just add this as first entry
 		if (null == socialDataFromDB){
 
 			assimilatedSocialData.setDate(DateUtil.getTodaysDate());
 
 			assimilatedSocialData.setUrl_id(currentURLID);
 
 			socialDataDBUnit.createNewSocialData(assimilatedSocialData);
 
 			return -1;
 		}
 
 		// if there's no difference between DB data and today's data: then don't create a new entry in DB:
 		if(socialDataFromDB.equals(assimilatedSocialData)){
 
 			log.info("Todays social data no different from last saved data. url id:  " + String.valueOf(currentURLID));
 			
 			return 0;
 		}
 
 		// check individual properties of the assimilated social data against the socialDataDBUnit
 		//	- if the assimilated data has any entry that is a 0, and if the DBdata is NOT 0, then use the 
 		//	DB data in the new entry - this means the call to that particular social API has failed, 
 		// so rather than let the rest of the data go to waste, just use previous record and update the 
 		//	the social calls that DID work...
 
 		MiscSocialData newSocialData = new MiscSocialData();
 
 		newSocialData.setDate(DateUtil.getTodaysDate());
 		newSocialData.setUrl_id(currentURLID);
 
 		/////////Assimilate individual social metrics//////////////////////////////////////////
 		//TODO: here we can get the new data and old data and look for a spike...
 		// if DB data is Less than most recent API data this means the social data has increased - use API data
 		
 		deliciousDB = socialDataFromDB.getDelicious();
 		deliciousToday = assimilatedSocialData.getDelicious();
 		
 		fbTotalCountDB = socialDataFromDB.getFb_total_Count(); 
 		fbTotalCountToday = assimilatedSocialData.getFb_total_Count();
 		
 		googlePlusDB = socialDataFromDB.getGoogle_plus_one(); 
 		googlePlusToday = assimilatedSocialData.getGoogle_plus_one();
 		
 		linkedInDB = socialDataFromDB.getLinkedin(); 
 		linkedInToday = assimilatedSocialData.getLinkedin();
 		
 		pinterestDB = socialDataFromDB.getPinterest(); 
 		pinterestToday = assimilatedSocialData.getPinterest();
 		
 		stumbleUponDB = socialDataFromDB.getStumble_upon(); 
 		stumbleUponToday = assimilatedSocialData.getStumble_upon();
 		
 		twitterDB = socialDataFromDB.getTwitter(); 
 		twitterToday = assimilatedSocialData.getTwitter();
 		
 		// get summation of social data for both DB and current social data
 		totalSocialDataDB = deliciousDB + fbTotalCountDB + googlePlusDB + linkedInDB + pinterestDB + stumbleUponDB + twitterDB;
 		totalSocialDataToday = deliciousToday + fbTotalCountToday + googlePlusToday + linkedInToday + pinterestToday + stumbleUponToday + twitterToday;
 		
 		// work out %age difference...
 		pcDifference = (double) ((totalSocialDataToday - totalSocialDataDB)/ totalSocialDataDB) * 100;
 		
 			
 		////////////////////////
 		// delicious.....
 		if(deliciousDB < deliciousToday)
 			newSocialData.setDelicious(deliciousToday);
 		else
 			newSocialData.setDelicious(deliciousDB); // else API call has failed or its not changed from DB data
 
 		// Facebook
 		if(fbTotalCountDB < fbTotalCountToday){
 			newSocialData.setFb_click_Count(assimilatedSocialData.getFb_click_Count());
 			newSocialData.setFb_comment_Count(assimilatedSocialData.getFb_comment_Count());
 			newSocialData.setFb_commentsbox_count(assimilatedSocialData.getFb_commentsbox_count());
 			newSocialData.setFb_like_Count(assimilatedSocialData.getFb_like_Count());
 			newSocialData.setFb_share_Count(assimilatedSocialData.getFb_share_Count());
 			newSocialData.setFb_total_Count(fbTotalCountToday);
 		}else{ 
 			newSocialData.setFb_click_Count(socialDataFromDB.getFb_click_Count());
 			newSocialData.setFb_comment_Count(socialDataFromDB.getFb_comment_Count());
 			newSocialData.setFb_commentsbox_count(socialDataFromDB.getFb_commentsbox_count());
 			newSocialData.setFb_like_Count(socialDataFromDB.getFb_like_Count());
 			newSocialData.setFb_share_Count(socialDataFromDB.getFb_share_Count());
 			newSocialData.setFb_total_Count(fbTotalCountDB);
 		}
 
 		//Google+
 		if(googlePlusDB < googlePlusToday)
 			newSocialData.setGoogle_plus_one(googlePlusToday);
 		else
 			newSocialData.setGoogle_plus_one(googlePlusDB);
 
 		//LinkedIn
 		if(linkedInDB < linkedInToday)
 			newSocialData.setLinkedin(linkedInToday);
 		else
 			newSocialData.setLinkedin(linkedInDB);
 
 		// Pinterest
 		if(pinterestDB < pinterestToday)
 			newSocialData.setPinterest(pinterestToday);
 		else
 			newSocialData.setPinterest(pinterestDB);
 
 		//StumbleUpon
 		if(stumbleUponDB < stumbleUponToday)
 			newSocialData.setStumble_upon(stumbleUponToday);
 		else
 			newSocialData.setStumble_upon(stumbleUponDB);
 
 		//Twitter
 		if(twitterDB < twitterToday)
 			newSocialData.setTwitter(twitterToday);
 		else
 			newSocialData.setTwitter(twitterDB);
 
 		///////////////////////////////////////////////
 
 		log.info("New social data entry for url: " + String.valueOf(currentURLID));
 		socialDataDBUnit.createNewSocialData(newSocialData);
 	
 
 		return pcDifference;
 
 	}
 
 	private MiscSocialData assimilateSocialData(MiscSocialData data1, MiscSocialData data2){
 
 		MiscSocialData assimilatedData = new MiscSocialData();	
 
 		/////////////////////////
 
 		// run through every data type... using assimilateHelper method to return one or sum of the values from the sharedCount objects...
 		assimilatedData.setDelicious(			assimilateHelper(data1.getDelicious(),		 data2.getDelicious(),GoFetchConstants.ALLOWED_SOCIAL_DATA_DIFFERENCE));
 		assimilatedData.setGoogle_plus_one(		assimilateHelper(data1.getGoogle_plus_one(), data2.getGoogle_plus_one(),GoFetchConstants.ALLOWED_SOCIAL_DATA_DIFFERENCE));
 		assimilatedData.setLinkedin(			assimilateHelper(data1.getLinkedin(),		 data2.getLinkedin(),GoFetchConstants.ALLOWED_SOCIAL_DATA_DIFFERENCE));
 		assimilatedData.setPinterest(			assimilateHelper(data1.getPinterest(),		 data2.getPinterest(),GoFetchConstants.ALLOWED_SOCIAL_DATA_DIFFERENCE));
 		assimilatedData.setStumble_upon(		assimilateHelper(data1.getStumble_upon(),	 data2.getStumble_upon(),GoFetchConstants.ALLOWED_SOCIAL_DATA_DIFFERENCE));
 		assimilatedData.setTwitter(				assimilateHelper(data1.getTwitter(),		 data2.getTwitter(),GoFetchConstants.ALLOWED_SOCIAL_DATA_DIFFERENCE));	
 		//facebook data:
 		assimilatedData.setFb_click_Count(			assimilateHelper(data1.getFb_click_Count(),		 data2.getFb_click_Count(),GoFetchConstants.ALLOWED_SOCIAL_DATA_DIFFERENCE));
 		assimilatedData.setFb_comment_Count(		assimilateHelper(data1.getFb_comment_Count(), 	 data2.getFb_comment_Count(),GoFetchConstants.ALLOWED_SOCIAL_DATA_DIFFERENCE));
 		assimilatedData.setFb_commentsbox_count(	assimilateHelper(data1.getFb_commentsbox_count(),data2.getFb_commentsbox_count(),GoFetchConstants.ALLOWED_SOCIAL_DATA_DIFFERENCE));
 		assimilatedData.setFb_like_Count(			assimilateHelper(data1.getFb_like_Count(),		 data2.getFb_like_Count(),GoFetchConstants.ALLOWED_SOCIAL_DATA_DIFFERENCE));
 		assimilatedData.setFb_share_Count(			assimilateHelper(data1.getFb_share_Count(),		 data2.getFb_share_Count(),GoFetchConstants.ALLOWED_SOCIAL_DATA_DIFFERENCE));
 		assimilatedData.setFb_total_Count(			assimilateHelper(data1.getFb_total_Count(),		 data2.getFb_total_Count(),GoFetchConstants.ALLOWED_SOCIAL_DATA_DIFFERENCE));
 
 		return assimilatedData;
 
 	}
 
 	/**
 	 * 
 	 * @param data1
 	 * @param data2
 	 * @return - if data1 and data2 are the same, then returns that value,
 	 * if different - returns the sum of the two values
 	 */
 	private int assimilateHelper(int data1, int data2){
 
 		if(data1 != data2 )
 			return (data1 + data2);
 		else
 			return data1;
 	}
 
 	/**
 	 * 
 	 * @param data1 - integer no 1 to compare
 	 * @param data2 - integer no 2 to compare
 	 * @param pcAllowedVariation - maximum allowed %age (inclusively) allowed before the two values passed are returned as a sum. 
 	 *  	to pass 1%, pass '1', not '0.01'.
 	 * @return -
 	 * case 1:  if the difference between data1 and data2 is no more than pcAllowedVariation % then returns the greater of the two values, 
 	 * 				- this represents that the data is the same, it just grew over time btwn the two API calls and the greater value is the most recent data.
 	 * 			Useful in facebook data for popular urls  - like bbc - where a minor % difference btwn facebook data may be due to the difference in 
 	 * 			time between calls to the sharedcount api...
 	 * 
 	 * case 2: if the difference between data1 and data2 is greater than pcAllowedVariation %, then they are summed and this value is returned      
 	 * 
 	 * case 3: if data1 and data2 are the same, then this value is returned...
 	 * 
 	 * /////////
 	 * ex1 : data1 = called first. (facebook totalcount = 1200 )
 	 * 	     data2 = called 2nd: Facebook totalcount = 1212)
 	 * 
 	 * % difference btwn the 2 = ((1212-1200) / 1212) * 100 = 1% - method returns 1212 (taking the maximum allowed pc = 1%)
 	 * /////////
 	 * 
 	 * /////////
 	 * ex2: data1 = fb total count = 100
 	 * 		data2 = fb total count = 1200
 	 * 		% difference btwn the 2 = ((1200-100) / 1200) * 100 = 91.6 (91.6%). - method returns sum = 1300
 	 * 
 	 * 
 	 * /////////
 	 * 
 	 * ex3: data1 = fb total count = 100
 	 * 		data2 = fb total count = 100
 	 * 		% difference btwn the 2 = ((100-100) / 100) * 100 = 0%. - method returns one, (...the largest value) = 100
 	 * 
 	 * 
 	 */
 	//TODO: test this - can use unit testing here - assert return values based on params.
 	private int assimilateHelper(int data1, int data2, double pcAllowedVariation){
 
 		if(data1 != data2 ){
 
 			int largestValue, smallestValue;
 			double difference;
 
 			/////////////
 			//1. assign largest and smallest values
 			if(data1 > data2){
 				largestValue = data1;
 				smallestValue = data2;
 			}
 			else{
 				largestValue = data2;
 				smallestValue = data1;
 			}
 			/////////////////
 
 			//2. calc %age difference
 			difference = (double)(((double)largestValue - (double)smallestValue)/(double)largestValue) *100.0;
 
 			//3.a: return sum - if difference is greater than allowed %age
 			if(difference > pcAllowedVariation){
 				return (data1 + data2);
 
 				//3.b:	its under the allowed %age - return the largest value
 			}else{ 
 				return largestValue;
 			}
 
 		}
 		else // they are the same so just return one of them.
 			return data1;
 
 	}
 }
