 package web;
 
 import java.sql.SQLException;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Calendar;
 import java.util.Collections;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.logging.Logger;
 
 import org.jsoup.Jsoup;
 import org.jsoup.nodes.Document;
 import org.jsoup.nodes.Element;
 import org.jsoup.select.Elements;
 
 import src.DBManager;
 import src.Util;
 
 
 public class CommunityInfoReader extends HTMLReader {
 	private final static Logger logger = Logger.getLogger(CommunityInfoReader.class .getName()); 
 	private StatisticReader statsReader = null;
 	
 	public CommunityInfoReader() {
 		super();
 		prepareUserAgent();
 		statsReader = new StatisticReader();
 	}
 	
 	public HashMap<String,Object> readCommunityStats(String url, String name) {
 		Util.printMessage("Getting stats of the community: " + name,"info",logger);
 		HashMap<String,Object> comStatistics = statsReader.getCommunityStatistic(url);
 		comStatistics.put("name", name.replace("\"",""));
 		comStatistics.put("url", url);
 		
 		return comStatistics;
 	}
 	
 	public HashMap<String,Object> syncCommunityStats(String communityId, String communityURL, 
 													 DBManager db) 
 	throws SQLException {
 		Util.printMessage("Getting community's stats","info",logger);
 		//Get community statistics
 		HashMap<String,Object> comStatistics = statsReader.getCommunityStatistic(communityURL);
 		//Update community statistics
 		db.updateCommunityStats(comStatistics, communityId);
 		
 		return comStatistics;
 	}
 	
 	public ArrayList<HashMap<String,Object>> resumeSyncProcess(HashMap<String,Object> process, DBManager db) 
 	throws Exception {
 		Util.printMessage("Resuming a previous unfinished process","info",logger);
 		
 		String communityURL = (String) process.get("community_url");
 		Integer communityId = (Integer) process.get("community_id");
 		Integer currentPage = (Integer) process.get("current_page");
 		Integer observation = (Integer) process.get("observation");
 		String content = getUrlContent(Util.toURI(communityURL));
 		Document doc = Jsoup.parse(content);
 		String currentTab = (String) process.get("current_tab");
 		
 		ArrayList<HashMap<String,String>> tabs = statsReader.getTabsURL(doc);
 		for (HashMap<String,String> tab : tabs) {
 			if (tab.get("url").equals(currentTab))
 				break;
 			else
 				tabs.remove(tab);
 		}
 		
 		ArrayList<HashMap<String,Object>> info = syncIdeas(communityURL,
 														   communityId,tabs,
 														   currentPage, db,
 														   observation);
 		
 		return info;
 	}
 	
 	public ArrayList<HashMap<String,Object>> syncIdeas(String communityURL, Integer communityId,
 						  							    ArrayList<HashMap<String,String>> tabs, 
 						  							    Integer currentPageNum, DBManager db,
 						  							    Integer observation) 
 	throws Exception 
 	{
 		Integer pageNum = currentPageNum;
 		String SUFFIX = "&pageOffset=";
 		String IDEA_CONTENT_CLASS = "content";
 		String IDEA_TITLE_CLASS = "title";
 		String IDEA_AUTHOR_ATTR_KEY = "rel";
 		String IDEA_AUTHOR_ATTR_VAL = "user";
 		String IDEA_CREATION_TIME = "published";
 		Document doc;
 		Calendar cal = Calendar.getInstance();
 		Date today = cal.getTime();
 		ArrayList<HashMap<String,Object>> info = new ArrayList<HashMap<String,Object>>();
 		boolean lastPage;
 		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
 		Integer idIdeaDB = -1;
 		
 		Util.printMessage("Getting community's ideas","info",logger);
 		for (HashMap<String,String> tab : tabs) {
 			String currentPage = communityURL+tab.get("url")+SUFFIX+pageNum.toString();
 			String page = getUrlContent(Util.toURI(currentPage));
 			lastPage = false;
 			
 			Util.printMessage("Getting ideas of the page " + currentPage,"info",logger);
 			
 			doc = Jsoup.parse(page);
 			
 			db.insertSyncProcess(communityURL, tab.get("url"), pageNum, communityId, observation);
 			while (!lastPage) {
 				Element ideasList = doc.getElementById("ideas");
 				for (Element ideaList : ideasList.children()) {
 					String ideaId = ideaList.attr("id").split("-")[1];
 					if (isNewIdea(ideaId,info)) {
 						Elements ideaContentElems = ideaList.getElementsByClass(IDEA_CONTENT_CLASS);
 						if (!ideaContentElems.isEmpty()) {
 							Element ideaContent = ideaContentElems.first();
 							Elements ideaTitleElems = ideaContent.getElementsByClass(IDEA_TITLE_CLASS);
 							if (!ideaTitleElems.isEmpty()) {
 								Element ideaTitle = ideaTitleElems.first();
 								String ideaLink = ideaTitle.child(0).attr("href");
 								HashMap<String,Object> ideaStats = statsReader.getIdeaStatistics(communityURL,ideaLink);
 								ideaStats.put("title", ideaTitle.text());
 								ideaStats.put("url", communityURL+ideaLink);
 								ideaStats.put("id", ideaId);
 								Elements ideaAuthorElems = ideaList.getElementsByAttributeValue(IDEA_AUTHOR_ATTR_KEY, IDEA_AUTHOR_ATTR_VAL);
 								if (!ideaAuthorElems.isEmpty()) {
 									ideaStats.put("author-name", ideaAuthorElems.text());
 									String authorId = ideaAuthorElems.attr("href");
 									authorId = authorId.substring(authorId.lastIndexOf("/")+1,authorId.length());
 									authorId = authorId.split("-")[0];
 									ideaStats.put("author-id", authorId);
 								}
 								else {
 									ideaStats.put("author-name", "Unsuscribed User");
 									ideaStats.put("author-id", "-1");
 								}
 								Elements ideaDTElems = ideaList.getElementsByClass(IDEA_CREATION_TIME);
 								if (!ideaDTElems.isEmpty()) {
 									String[] ideaDT = ideaDTElems.first().attr("title").split("T");
 									Date ideaDateTime = formatter.parse(ideaDT[0]+" "+ideaDT[1].split("-")[0]);
 									ideaStats.put("datetime", ideaDateTime);
 								}
 								else {
 									ideaStats.put("datetime", null);
 								}
 								Element statusElement = ideaList.getElementById("idea-"+ideaId+"-status");
 								if (statusElement != null) {
 									String statusStr = statusElement.attr("class");
 									ideaStats.put("status", statusStr.split(" ")[1]);
 								}
 								else {
 									ideaStats.put("status", null);
 								}
 								//Update/Insert Idea
 								HashMap<String,String> existingIdea = db.ideaAlreadyInserted(Integer.parseInt(ideaId));
 								if (existingIdea.isEmpty()) {
 									db.insertCommunityIdea(ideaStats,communityId.toString());
 								}
 								else {
 									db.updateCommunityIdea(ideaStats, Integer.parseInt(existingIdea.get("id")));
 									checkIncrementSNCounters(existingIdea,ideaStats,observation,db);
 								}
 								//Save Comments and Votes
 								if (existingIdea.isEmpty()) {
 									idIdeaDB = db.getIdeaId(Integer.parseInt(ideaId));
 								}
 								else {
 									idIdeaDB = Integer.parseInt(existingIdea.get("id"));
 								}
 								if (idIdeaDB != -1) {
 									//Comments
 									if (ideaStats.containsKey("comments-meta")) {
 										ArrayList<HashMap<String,String>> commentsMeta = 
 										(ArrayList<HashMap<String, String>>) ideaStats.get("comments-meta");
 										for (HashMap<String,String> comment : commentsMeta) {
 											Integer commentId = Integer.parseInt(comment.get("id"));
											if (!db.commentAlreadyExisting(commentId, Integer.parseInt(ideaId)))
 												db.insertComment(comment, idIdeaDB, today);
 										}
 									}
 									//Votes
 									if (ideaStats.containsKey("votes-meta")) {
 										ArrayList<HashMap<String,String>> votesMeta = 
 										(ArrayList<HashMap<String, String>>) ideaStats.get("votes-meta");
 										for (HashMap<String,String> vote : votesMeta)
 											db.insertVote(vote, idIdeaDB, today);
 									}
 								}
 								else {
 									throw new Exception("Could not find idea with id: " + ideaId);
 								}
 								info.add(ideaStats);
 							}
 							else {
 								throw new Exception("Couldn't find idea's title");
 							}
 						}
 						else {
 							throw new Exception("Couldn't find idea's content");
 						}
 					}
 				}
 				lastPage = isLastPage(doc);
 				if (!lastPage) {
 					pageNum += 1;
 					currentPage = communityURL+tab.get("url")+SUFFIX+pageNum.toString();
 					Util.printMessage("Getting ideas of the page " + currentPage,"info",logger);
 					page = getUrlContent(Util.toURI(currentPage));
 					doc = Jsoup.parse(page);
 					db.updateSyncProcess(pageNum);
 				}
 			}
 			
 			//Wait for a moment to avoid being banned
 			double rand = Math.random() * 5;		        			
 			Thread.sleep((long) (rand * 1000)); 
 			pageNum = 0;
 			db.cleanSyncProgressTable();
 		}
 		
 		return info;
 	}
 	
 	private static void checkIncrementSNCounters(HashMap<String,String> old,
 												 HashMap<String,Object> current,
 												 Integer observation,
 												 DBManager db) 
 	throws SQLException {
 		
 		if (current.get("facebook") != null && current.get("twitter") != null &&
 			old.get("facebook") != null && old.get("twitter") != null) 
 		{
 			Integer currentFBCounter = Integer.parseInt((String) current.get("facebook"));
 			Integer currentTWCounter = Integer.parseInt((String) current.get("twitter"));
 			Integer oldFBCounter = Integer.parseInt((String) old.get("facebook"));
 			Integer oldTWCounter = Integer.parseInt((String) old.get("twitter"));
 			if (currentFBCounter > oldFBCounter || currentTWCounter > oldTWCounter) {
 				if (current.get("comments") != null && old.get("comments") != null) {
 					if ((Integer) current.get("comments") < Integer.parseInt(old.get("comments")))
 					Util.printMessage("There are less comments than before. " +
 									  "Idea: " + old.get("url") + ". " +
 									  "Before: " + old.get("comments") +
 									  " - Now: " + current.get("comments"), 
 									  "severe", logger);
 					}
 				db.saveLogIdea(observation, old, current);
 			}
 		}
 		else {
 			Util.printMessage("Some of the SN counters of the idea: " + 
 							  old.get("name") + " are null", "info", logger);
 		}
 	}
 	
 	private boolean isNewIdea(String idIdea, ArrayList<HashMap<String,Object>> elems) {
 		for (HashMap<String,Object> elem : elems) {
 			if (elem.get("id") == idIdea)
 				return false;
 		}
 		return true;
 	}
 	
 	public HashMap<String,Object> getCommunityLifeSpan(String urlCommunity, 
 													   ArrayList<HashMap<String,String>> tabs) 
 	throws Exception 
 	{
 		final String SUFFIX = "&pageOffset=";
 		final Integer MAXIDEAS = 3; 
 		
 		HashMap<String,Object> communityInfo = new HashMap<String,Object>();
 		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
 		Date ideaCreationDate = null;
 		Date dateFirstIdea = null;
 		ArrayList<Date> dateLastIdeas = new ArrayList<Date>();
 		Integer pageNum = 0;
 		Integer ideasProcessed = 0;
 		
 		Util.printMessage("Calculating the lifespan of the community: " + 
 					  urlCommunity,"info",logger);
 		
 		int counterLastIdeas = 0;
 		for (HashMap<String,String> tab : tabs) {
 			String currentPage = urlCommunity+tab.get("url")+SUFFIX+pageNum.toString();
 			String page = getUrlContent(Util.toURI(currentPage));
 			Integer tabIdeas = Integer.parseInt(tab.get("ideas"));
 			ideasProcessed = 0;
 			
 			Util.printMessage("Getting idea's creation time of the community page: " + 
 						  currentPage,"info",logger);
 			
 			Document doc = Jsoup.parse(page);
 
 			while (ideasProcessed < tabIdeas || !emptyList(doc)) {
 				Element ideaList = doc.getElementById("ideas");
 				for (Element idea : ideaList.children()) {
 					ideasProcessed += 1;
 					ideaCreationDate = format.parse(getIdeaCreationDate(idea));
 					if (dateFirstIdea == null ||
 						isOlderIdea(ideaCreationDate,dateFirstIdea))
 						dateFirstIdea = ideaCreationDate;
 					if (dateLastIdeas.isEmpty() ||
 						counterLastIdeas < MAXIDEAS) {
 						dateLastIdeas.add(ideaCreationDate);
 						counterLastIdeas += 1;
 					}
 					else {
 						if (isNewerIdea(ideaCreationDate,dateLastIdeas)) {
 							dateLastIdeas.remove(getIndexOldestIdea(dateLastIdeas));  /* Remove the oldest idea */
 							dateLastIdeas.add(ideaCreationDate); /* Add the newest one */
 						}
 					} 
 				}
 				pageNum += 1;
 				currentPage = urlCommunity+tab.get("url")+SUFFIX+pageNum.toString();
 				page = getUrlContent(Util.toURI(currentPage));
 				doc = Jsoup.parse(page);
 			}
 			
 			//Wait for a moment to avoid being banned
 			double rand = Math.random() * 5;		        			
 			Thread.sleep((long) (rand * 1000));
 			
 			pageNum = 0;
 		}
 		
 		/* If it is not a closed community */
 		if (!dateLastIdeas.isEmpty() && dateFirstIdea != null) {
 			Collections.sort(dateLastIdeas);
 			int lenghtArrayLastIdeas = dateLastIdeas.size();
 			Date dateLastIdea = dateLastIdeas.get(lenghtArrayLastIdeas-1);
 			/* Save the information */
 			communityInfo.put("dateLastIdea", dateLastIdea);
 			communityInfo.put("dateFirstIdea", dateFirstIdea);
 			communityInfo.put("lifespan", calculateLifeSpan(dateFirstIdea, 
 															dateLastIdea));
 			communityInfo.put("status", getCommunityStatus(dateLastIdeas));
 		}
 		else {
 			communityInfo.put("dateLastIdea", null);
 			communityInfo.put("dateFirstIdea", null);
 			communityInfo.put("lifespan", null);
 			communityInfo.put("status", "closed");
 		}
 		
 		return communityInfo;
 	}
 	
 	/**
 	 * The heuristic is simple: for being considered open, the last 5-ideas
 	 * of a community have to be newer than the reference date (01-01-13)
 	 * @param lastIdeas Array containing the date of the latest ideas
 	 * @return the status
 	 */
 	private String getCommunityStatus(ArrayList<Date> lastIdeas) {
 		/* Reference Date 01-01-2013 */
 		Calendar cal = Calendar.getInstance();
 		cal.set(Calendar.YEAR,2013);
 		cal.set(Calendar.MONTH, Calendar.JANUARY);
 		cal.set(Calendar.DAY_OF_MONTH,1);
 		Date refDate = cal.getTime();
 		Date lastIdea = lastIdeas.get(lastIdeas.size()-1);
 		if (lastIdea.before(refDate)) {
 			return "inactive";
 		}
 		else {
 			for (Date idea : lastIdeas)
 				if (idea.before(refDate))
 					return "inactive";
 			return "active";
 		}
 	}
 	
 	private int getIndexOldestIdea(ArrayList<Date> lastIdeas) {
 		int indexOI = 0;
 		Date oldestIdea = lastIdeas.get(0);
 		for (int i = 0; i < lastIdeas.size(); i++) {
 			if (oldestIdea.after(lastIdeas.get(i))) {
 				oldestIdea = lastIdeas.get(i);
 				indexOI = i;
 			}
 		}
 		return indexOI;
 	}
 	
 	private boolean isNewerIdea(Date currentIdea, ArrayList<Date> lastIdeas) 
 	throws ParseException 
 	{
 		Collections.sort(lastIdeas);
 		if (currentIdea.after(lastIdeas.get(0))) {
 			for (Date idea : lastIdeas) {
 				if (currentIdea.equals(idea))
 					return false;
 			}
 			return true;
 		}
 		else {
 			return false;
 		}
 	}
 	
 	private boolean isOlderIdea(Date currentIdea, Date firstIdea) 
 	throws ParseException 
 	{
 		if (currentIdea.before(firstIdea)) return true;
 		else return false;
 	}
 	
 	private String getIdeaCreationDate(Element idea) {
 		Element ideaCreationDate = idea.getElementsByClass("published").first();
 		return ideaCreationDate.attr("title").split("T")[0];
 	}
 	
 	private int calculateLifeSpan(Date firstIdea, Date lastIdea) {
 		long diff = lastIdea.getTime()-firstIdea.getTime();
 		long diffDays = diff / (24 * 60 * 60 * 1000);
 		int lifeSpanInDays = (int) diffDays + 1; /* Considering the kickoff date */
 		return lifeSpanInDays; 
 	}
 	
 	public Boolean inBlackList(String link) {
 		List<String> blackList = new ArrayList<String>(
 								 Arrays.asList("support.ideascale",
 								 			   "blog.ideascale",
 								 			   "twitter.com/ideascale",
 								 			   "facebook.com/ideascale",
 								 			   "www.surveyanalytics.com/?utm_source=IdeaScale",
 								 			   "www.questionpro.com/?utm_source=IdeaScale",
 								 			   "www.micropoll.com/?utm_source=IdeaScale",
 								 			   "www.researchaccess.com/?utm_source=IdeaScale"));
 		for (String item : blackList)
 			if (link.contains(item))
 				return true;
 		return false;
 	}
 	
 	private boolean emptyList(Document doc) {
 		Elements e = doc.getElementsByClass("no-ideas");
 		if (e.size() > 0)
 			return true;
 		else
 			return false;
 	}
 	
 	private boolean isLastPage(Document doc) {
 		Elements pag = doc.getElementsByClass("pagination");
 		
 		//If the pagination elements doesn't exist we are in the last page
 		if (pag.isEmpty()) {  
 			return true;
 		}
 		else {
 			int numElements = pag.first().children().size();
 			Element lastPag = pag.first().children().get(numElements-1);
 			
 			//If the last pagination element doesn't have child we are in the
 			//last page
 			if (lastPag.children().size() > 0) {
 				lastPag = lastPag.child(0);
 				//The 'next' button is identified by the absent of the attribute 'title'
 				//and by the presence of the attribute 'href'
 				//So if the 'next' button is within the pagination elements set 
 				//we still have pages ahead and we are not in the last page.
 				if (lastPag.hasAttr("href") && !lastPag.hasAttr("title")) 
 					return false;
 				else
 					return true;
 			}
 			else {
 				return true;
 			}
 		}
 	}
 	
 	/*public List<HashMap<String,Object>> getCommunitiesInfo() {
 		List<HashMap<String,Object>> comunities = new ArrayList<HashMap<String,Object>>();
 		List<String> letterDir = new ArrayList<String>(
 					Arrays.asList("a","b","c","d","e","f","g","h","i","j","k",
 							      "l","m","n","o","p","q","r","s","t","u","v",
 							      "w","x","y","z"));
 		try {
 			for (String letter : letterDir) {
 				System.out.println("Starting with communities of cat: " + letter.toUpperCase() + ".");
 				String content = getUrlContent(urlDir+letter+".html");
 		        Document doc = Jsoup.parse(content);	        
 		        Elements liElements = doc.getElementsByTag("li");
 		        for (Element li : liElements)
 		        	if (li.children().size() > 0) {
 		        		String link = li.child(0).attr("href");
 		        		String name = li.text();
 		        		if (link.contains("http://") && !inBlackList(link)) {
 		        			HashMap<String,Object> comunity = readCommunityStats(link, name);		        			
 		        			comunities.add(comunity);
 		        		}
 		        	}
 		        System.out.println("Communities cat: " + letter.toUpperCase() + " finished.");
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		return comunities;
 	}
 	
 	public void existsCommunity(String urlCommunity) throws Exception {
 		getUrlContent(urlCommunity);
 	}*/
 }
