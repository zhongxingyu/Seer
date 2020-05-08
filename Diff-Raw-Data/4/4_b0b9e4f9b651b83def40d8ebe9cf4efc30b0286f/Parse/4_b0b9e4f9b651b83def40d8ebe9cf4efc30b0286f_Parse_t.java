 import java.net.*;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.*;
 import java.io.*;
 
 import org.jsoup.Jsoup;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 import com.alchemyapi.api.AlchemyAPI;
 import com.alchemyapi.api.AlchemyAPI_KeywordParams;
 import com.alchemyapi.api.AlchemyAPI_NamedEntityParams;
 
 public class Parse implements Runnable {
 
 	private Document doc; // Stores the response from Alchemy
 	private Document sentdoc;
 	private LinkedList<Topic> topics = new LinkedList<Topic>();
 	private CompanyList cList = new CompanyList();
 	private String sector;
 	private String urlCache;
 	private int uid;
 	private String APIkey = "fbde73712800960605177cdcf8cc5ade6ebd15a5";
 
 
 	Parse(String sector) {
 		this.sector = sector;
 		urlCache = null;
 	}
 
 	public void run() {
 		uid = 0;
 		while (true) {
 			try {
 				System.out.println("Starting search");
 				// URL newsURL = new URL("http://www.google.com/search?q=" + sector + "&num=100&tbm=nws&tbs=sbd:1,nsd:1");
 				URL newsURL = new URL("http://www.google.com/search?q=" + sector + "&num=30&tbm=nws&tbs=sbd:1,nsd:1");
 				URLConnection uc = newsURL.openConnection();
 				// Need to pretend we are a browser so that google responds
 				uc.setRequestProperty
 				( "User-Agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.0)" );
 				BufferedReader in = new BufferedReader(
 						new InputStreamReader(
 								uc.getInputStream()));
 				String inputLine;
 
 				// Grabs all the links and stuffs them into theURLS
 				// Grabs all the titles and stuffs them into theTitles
 				// Title appears between "> and </a></h3>
 				// URL appears between <a href="/url?q= and &amp;
 				LinkedList<String> theURLS = new LinkedList<String>();
 				LinkedList<String> theTitles = new LinkedList<String>();
 				LinkedList<String> theSources = new LinkedList<String>();
 				LinkedList<String> theDescrips = new LinkedList<String>();
 				// while we have a line to read
 				while ((inputLine = in.readLine()) != null) {
 
 					int startIndex = inputLine.indexOf("<h3 class=\"r\"><a href=\"/url?q=");
 					while(startIndex != -1)
 					{
 						try {
 						inputLine = inputLine.substring(startIndex + 30);
 						String urlTemp = inputLine.substring(0,inputLine.indexOf("&amp;sa=U&amp;"));
 						urlTemp = URLDecoder.decode(urlTemp, "UTF-8");
 						inputLine = inputLine.substring(inputLine.indexOf("\">")+2);
 						String titleTemp = inputLine.substring(0,inputLine.indexOf("</a></h3>"));
 						titleTemp = Jsoup.parse(titleTemp).text();
 						inputLine = inputLine.substring(inputLine.indexOf("<span class=\"f\">")+16);
 						String sourceTemp = inputLine.substring(0,inputLine.indexOf("-")-1);
 						inputLine = inputLine.substring(inputLine.indexOf("<div>")+5);
 //						System.out.println(inputLine);
 //						String descripTemp = "";
 //						if(inputLine.indexOf("<b>...</b>") != -1)
 //							descripTemp = inputLine.substring(0,inputLine.indexOf("<b>...</b>")-1);
 //						else
 							String descripTemp =  inputLine.substring(0,inputLine.indexOf("</div>")-1);
 						theURLS.add(urlTemp);
 						theTitles.add(titleTemp);
 						theSources.add(sourceTemp);
 						theDescrips.add(descripTemp);
 						startIndex = inputLine.indexOf("<h3 class=\"r\"><a href=\"/url?q=");
 						}
 						catch(Exception e)
 						{
 							startIndex = inputLine.indexOf("<h3 class=\"r\"><a href=\"/url?q=");
 						}
 					}
 				}
 
 				// close input stream
 				in.close();
 
 				// Create a list of articles and titles
 				// Is there a guarantee that these match up?
 				LinkedList<Article> articles = new LinkedList<Article>();
 				Date d = new Date();
 				for (int i = 0; i < theURLS.size(); i++) {
 					System.out.println(theTitles.get(i));
 					articles.add(new Article(theURLS.get(i),theTitles.get(i),d,theSources.get(i),theDescrips.get(i)));
 				}
 
 				LinkedList<Article> newArticles = new LinkedList<Article>();
 				if (articles.size() > 0) {
 					for (Article art : articles) {
 						if (art.getURL().equals(urlCache))
 							break;
 						newArticles.add(art);
 					}
 				}
 
 				for (Article art : newArticles) {
 					//System.out.println(art.getURL());
 					art.printMe();
 					AlchemyAPI alchemyObj = AlchemyAPI.GetInstanceFromString(APIkey);
 					AlchemyAPI_KeywordParams params = new AlchemyAPI_KeywordParams();
 					params.setKeywordExtractMode("strict");
 					params.setMaxRetrieve(10);
 					params.setSentiment(true);
 					try {
 						doc = alchemyObj.URLGetRankedKeywords(art.getURL(), params);
 						sentdoc = alchemyObj.URLGetTextSentiment(art.getURL());
 
 						// Convert output to String
 						String alchemyOutput = NewsAnalyst.getStringFromDocument(doc);
 						String alchemyOutputSent = NewsAnalyst.getStringFromDocument(sentdoc);
 						alchemyOutput = NewsAnalyst.removeKeywordXML(alchemyOutput);
 						alchemyOutput = Jsoup.parse(alchemyOutput).text();
 						String[] result = alchemyOutput.split(";");
 						double sentiment = 0;
 						if(alchemyOutputSent.indexOf("<score>") != -1) {
 							alchemyOutputSent = alchemyOutputSent.substring(alchemyOutputSent.indexOf("<score>"));
 							alchemyOutputSent = alchemyOutputSent.substring(7,alchemyOutputSent.lastIndexOf("</score>"));
 							sentiment = Double.parseDouble(alchemyOutputSent);
 						}
 
 						// Add words to list
 						LinkedList<String> newWords = new LinkedList<String>();
 						LinkedList<Double> newRels = new LinkedList<Double>();
 
 						if (result.length < 6)
 							continue;
 
 						for (int i = 0; i < result.length; i += 3) {
 							newWords.addLast(result[i]);
 							newRels.addLast(Double.parseDouble(result[i+1]));
 						}
 
 						ArrayList<CompanyLink> companies = financeParse(art.getURL());
 
 						boolean isNewTopic = true;
 						double overlap;
 
 						// Check for overlap in existing topics
 						// Current check is if at least 3 words 
 						topicloop:
 						for (Topic t : topics) {
 							// check for unique sources
 							for (Article a : t.getArticles()) {
 								if (a.getSource().equals(art.getSource())) {
 									break topicloop;
 								}
 							}
 							
 							overlap = 0;
 							for (int i = 0; i < newWords.size(); i += 1) {
 								if (t.containsWord(newWords.get(i))) {
 									overlap = overlap + (newRels.get(i) * t.getRel(newWords.get(i)));
 								}
 							}
 							// overlap is sum of (topic relevance * art relevance)
 							if (overlap >= Math.round((t.totalRel()/3))) {
 								// if overlap is >  numwords / 3
 								isNewTopic = false;
 								t.addArticle(art, sentiment, companies);
 								// initial word merging, adds together
 								for (int i = 0; i < result.length; i += 3)
 									t.addWord(result[i], Double.parseDouble(result[i+1]), Double.parseDouble(result[i+2]));
 								t.addSentiment(sentiment);
 								System.out.println("Topic overlap found");
 							}
 						}
 
 						if (isNewTopic) {
 							Topic nextTopic = new Topic(art,uid,sentiment,companies);
 							uid++;
 							for (int i = 0; i < result.length; i += 3)
 								nextTopic.addWord(result[i], Double.parseDouble(result[i+1]), Double.parseDouble(result[i+2]));
 							nextTopic.printWordData();
 							topics.add(nextTopic);
 						}
 						
 					} catch (Exception e) {
 //						e.printStackTrace();
 						System.err.println("URL parsed incorrectly");
 					}
 				}
 				for (Company c : cList) {
 					c.updatePrice();
 				}
 				
 				ArrayList<Integer> removeThese = new ArrayList<Integer>();
 				for (int i = 0; i < topics.size(); i++) {
					for (int j = (i + 1); j < topics.size(); j++) {
						if (!removeThese.contains(j) && !removeThese.contains(i)) {
 							int overlaps = 0;
 							Iterator<Map.Entry<String, WordInfo>> it = topics.get(i).getWords().entrySet().iterator();
 							String s;
 							Map.Entry<String, WordInfo> swi;
 							while (it.hasNext()) {
 								swi = it.next();
 								s = swi.getKey();
 								if (topics.get(j).containsWord(s)) {
 									overlaps += swi.getValue().getRel() * topics.get(i).getWords().get(s).getRel();
 								}
 							}
 							// overlap is sum of (topic relevance * art relevance)
 							if (overlaps >= Math.round((topics.get(i).totalRel()/3))) {
 								topics.get(i).mergeTopic(topics.get(j));
 								removeThese.add(j);
 							}
 						}
 					}
 				}
 				ArrayList<Topic> at = new ArrayList<Topic>();
 				for (Integer i : removeThese) {
 					at.add(topics.get(i));
 				}
 				for (Topic t : at) {
 					topics.remove(t);
 				}
 				
 				// Output information on topics
 				for (Topic t : topics) {
 					System.out.println("Topic has " + (t.getArticles().size()) + " articles");
 					System.out.println("Topic has " + t.artsLastHour() + " articles in the last hour");
 					System.out.println(t.getRecentTitle());
 					Iterator<Article> iterator = t.getArticles().iterator();  
 					while (iterator.hasNext()) {
 						Article nextart = iterator.next();
 						DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
 						String date = dateFormat.format(nextart.getDate());
 						System.out.println(nextart.getURL() + " @ " + date);
 					} 
 					// Comment this out to test printTopWords
 					// t.printWordData();
 					t.printTopWords();
 					if(t.companies.size() != 0)
 						System.out.println("Companies Mentioned: ");
 					for(CompanyLink cl : t.companies)
 					{
 						Company current = cList.findCompany(cl.getCompany());
 						System.out.println("Company: " + current.getName());
 						if(current.isTraded())
 							System.out.println("Stock: " + current.getStockPrice() + " : " + current.getStockChange());
 					}
 				}
 				if (articles.size() != 0)
 					urlCache = articles.get(0).getURL();
 				System.out.println("Waiting before rerunning");
 				Thread.sleep(16000);
 			} catch (Exception e) {
 				e.printStackTrace();
 				System.err.println(e);
 			}
 		}
 	}
 
 	public ArrayList<CompanyLink> financeParse(String URL) throws Exception {
 		
 		Set<String> blackList = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER); 
 		blackList.add("facebook");
 		blackList.add("google");
 		blackList.add("twitter");
 		blackList.add("youtube");
 		blackList.add("reuters");
 		blackList.add("asset management company");
 		blackList.add("charset-error");
 		
 		AlchemyAPI alchemyObj = AlchemyAPI.GetInstanceFromString(APIkey);
 		AlchemyAPI_NamedEntityParams entityParams = new AlchemyAPI_NamedEntityParams();
 		entityParams.setSentiment(true);
 		entityParams.setDisambiguate(true);
 		entityParams.setMaxRetrieve(10);
 		Document doc = alchemyObj.URLGetRankedNamedEntities(URL, entityParams);
 
 		NodeList nList = doc.getElementsByTagName("entity");
 
 		ArrayList<CompanyLink> retList = new ArrayList<CompanyLink>();
 
 		for (int i = 0; i < nList.getLength(); i++) {
 			Element e = (Element) nList.item(i);
 
 			if (getTagValue("type", e).equals("Company")) {
 
 				String[] s = new String[3];
 				NodeList neList = e.getElementsByTagName("sentiment");
 				Element e2 = (Element) neList.item(0);
 				String type = getTagValue("type", e2);
 				if (type.equals("neutral"))
 					s[1] = "0.0";
 				else
 					s[1] = getTagValue("score", e2);
 
 				NodeList neList2 = e.getElementsByTagName("disambiguated");
 				if (neList2.getLength() == 0)
 					continue;
 				Element e3 = (Element) neList2.item(0);
 
 				s[0] = getTagValue("name", e3);
 				s[2] = getTagValue("relevance", e);
 
 				boolean found = false;
 				for (int k = 0; k < cList.size(); k++) {
 					if (cList.get(k).getName().equals(s[0])) {
 						cList.get(k).update(s[1], s[2]);
 						found = true;
 						System.out.println("Updated old company: " + s[0]);
 					}
 				}
 				if (!found && !blackList.contains(s[0])) {
 					Company c = new Company(s[0], s[1], s[2]);
 					c.updatePrice();
 					cList.add(c);
 					System.out.println("Found new company: " + s[0]);
 				}
 
 
 				retList.add(new CompanyLink(s[0],Double.parseDouble(s[1]),Double.parseDouble(s[2])));
 			}			
 		}
 		return new ArrayList<CompanyLink>(new HashSet<CompanyLink>(retList));
 	}
 
 	private static String getTagValue(String sTag, Element eElement) {
 		NodeList nlList = eElement.getElementsByTagName(sTag).item(0).getChildNodes();
 
 		Node nValue = (Node) nlList.item(0);
 
 		return nValue.getNodeValue();
 	}
 
 	public LinkedList<Topic> getTopics() {
 		return topics;
 	}
 
 	public String getSector() {
 		return sector;
 	}
 
 	public CompanyList getCompanies() {
 		return cList;
 	}
 	
 }
 
 class CompanyList extends LinkedList<Company>  {
 
 	private static final long serialVersionUID = -2879891466110522574L;
 
 	CompanyList ()
 	{
 		super();
 	}
 	
 	Company findCompany(String name) {
 		Company found = null;
 		for(Company comp : this)
 		{
 			if(comp.getName().equals(name))
 				return comp;
 		}
 		return found;
 	}
 }
