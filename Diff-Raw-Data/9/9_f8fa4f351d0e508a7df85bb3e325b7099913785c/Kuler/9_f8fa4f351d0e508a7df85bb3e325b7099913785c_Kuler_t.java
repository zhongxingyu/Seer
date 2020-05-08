 package colorLib.webServices;
 
 import java.util.ArrayList;
 
 import processing.core.PApplet;
 import processing.data.XML;
 
 /**
  * The Kuler object is a container to query the Adobe <a
  * href="http://kuler.adobe.com">Kuler</a> service and get the response as a
  * KulerTheme object. Take a look at the <a
  * href="http://labs.adobe.com/wiki/index.php/Kuler">Kuler API</a> to see the
  * possibilities of the Kuler service.<br/> A Kuler palette contains 5 colors
  * per theme. The default number of themes returned per query is 20, but you can
  * increase the count up 100. To query more than 100 themes, you have to
  * increase the startIndex. A query with maxItems=100 and startIndex=2 will
  * return 100 themes starting at 200.
  * 
  * @author Andreas K&ouml;berle
  * @author Jan Vantomme
  */
 
 public class Kuler extends WebService
 {
 
 
 	protected String key;
	private String serverPage = "https://kuler-api.adobe.com/rss/";
 	private String pageTyp = ".cfm";
 	
 	private boolean debugMode = true;
 
 	/**
 	 * @param parent
 	 *            PApplet: normally you will be using new Kuler(this);"
 	 */
 	public Kuler(final PApplet parent)
 	{
 		p = parent;
 		
 		
 	}
 
 	/**
 	 * Sets your Kuler API Key. You can apply for an API Key at the <a
 	 * href="http://kuler.adobe.com/api/">Adobe Kuler website</a>.
 	 * 
 	 * @param key
 	 */
 	public void setKey(String key)
 	{
 		this.key = key;
 	}
 
 	/**
 	 * Sets the debug mode. Default is true. If you set this to false, nothing will be printed to the Processing console. 
 	 * @param debug
 	 */
 	public void setDebugMode(boolean debug)
 	{
 		this.debugMode = debug;		
 	}
 	
 	/**
 	 * Get the highest rated colors as an array of <a
 	 * href="kulertheme_class_kulertheme.htm">KulerThemes</a>.
 	 * As it isn't necessary to load an xml every time you start your sketch you can save it by using the filename parameter.
 	 * Once a result is saved it will be used every time you use the same filename parameter
 	 * 
 	 * @return KulerTheme[], an array of all themes which match the query
 	 */
 	public KulerTheme[] getHighestRated()
 	{
 		return makePalettes("&listtype=rating", "get", null);
 	}
 	/**
 	 * @param filename  Filename to save the result xml, respectively load the xml if it still exists
 	 */
 	public KulerTheme[] getHighestRated(final String filename)
 	{
 		return makePalettes("&listtype=rating", "get", filename);
 	}
 
 	/**
 	 * Get the most popular colors for the specified number of days (default is
 	 * 30 days) as an array of <a
 	 * href="kulertheme_class_kulertheme.htm">KulerThemes</a>.
 	 * As it isn't necessary to load an xml every time you start your sketch you can save it by using the filename parameter.
 	 * Once a result is saved it will be used every time you use the same filename parameter
 	 * 
 	 * @return KulerTheme[], an array of all themes which match the query
 	 */
 	public KulerTheme[] getPopular(final String filename)
 	{
 		return makePalettes("&listType=popular&timeSpan=30", "get", filename);
 	}
 
 	/**
 	 * @param days
 	 *            int: Days
 	 */
 	public KulerTheme[] getPopular(final int days)
 	{
 		return makePalettes("&listType=popular&timeSpan=" + days, "get", null);
 	}
 	
 	/**
 	 * @param filename  Filename to save the result xml, respectively load the xml if it still exists
 	 */
 	public KulerTheme[] getPopular(final int days, final String filename)
 	{
 		return makePalettes("&listType=popular&timeSpan=" + days, "get", filename);
 	}
 
 	/**
 	 * Get the most recent colors as an array of <a
 	 * href="kulertheme_class_kulertheme.htm">KulerThemes</a>.
 	 * As it isn't necessary to load an xml every time you start your sketch you can save it by using the filename parameter.
 	 * Once a result is saved it will be used every time you use the same filename parameter
 	 * @param filename  Filename to save the result xml, respectively load the xml if it still exists
 	 * @return KulerTheme[], an array of all themes which match the query
 	 */
 
 	public KulerTheme[] getLatest (final String filename)
 	{
 		return makePalettes("&listType=recent", "get", filename);
 	}
 	
 	
 	/**
 	 * @deprecated As of release beta2, replaced by {@link #getLatest()}
 	 * @return -
 	 */
 	public KulerTheme[] getRecent()
 	{
 		return getLatest(null);
 	}
 	
 	/**
 	 * @deprecated As of release beta2, replaced by {@link #getLatest()}
 	 * @param filename  Filename to save the result xml, respectively load the xml if it still exists
 	 * @return -
 	 */
 	public KulerTheme[] getRecent(final String filename)
 	{
 		return getLatest(filename);
 	}
 
 	/**
 	 * Get random colors as an array of <a
 	 * href="kulertheme_class_kulertheme.htm">KulerThemes</a>.
 	 * 
 	 * @return KulerTheme[], an array of all themes which match the query
 	 */
 	
 	public KulerTheme[] getRandom(final String filename)
 	{
 		return makePalettes("&listType=random", "get", filename);
 	}
 
 	/**
 	 * Use your own query string or creates one by a given filter name and a
 	 * query. Possible filters are "themeID", "userID", "email", "tag", "hex"
 	 * and "title". Take a look at the <a
 	 * href="http://labs.adobe.com/wiki/index.php/Kuler#Search_RSS_Feeds">API</a>
 	 * to see the possibilities of the kuler service.<br/> The result is an
 	 * array of <a href="kulertheme_class_kulertheme.htm">KulerThemes</a>.
 	 * As it isn't necessary to load an xml every time you start your sketch you can save it by using the filename parameter.
 	 * Once a result is saved it will be used every time you use the same filename parameter
 	 * 
 	 * @param searchQuery
 	 *            String: your query
 	 * @return KulerTheme[], an array of all themes which match the query
 	 */
 	public KulerTheme[] search(final String searchQuery)
 	{
 		return makePalettes("searchQuery=" + searchQuery, "search", null);
 	}
 
 	/**
 	 * @param searchQuery
 	 *            String: query that is use with a searchFilter
 	 * @param filter
 	 *            String: one of the following filters: "themeID", "userID",
 	 *            "email", "tag", "hex" and "title"
 	 */
 	public KulerTheme[] search(final String searchQuery, final String filter)
 	{
 		return makePalettes("&searchQuery=" + filter + ":" + searchQuery, "search", null);
 	}
 
 	/**
 	 * @param filename Filename to save the result xml, respectively load the xml if it still exists
 	 * @return -
 	 */
 	public KulerTheme[] search(final String searchQuery, final String filter, String filename)
 	{
 		return makePalettes("&searchQuery=" + filter + ":" + searchQuery, "search", filename);
 	}
 	
 	public KulerTheme[] searchForThemes(String tag, String filename)
 	{
 		return makePalettes("&searchQuery=tag:" + tag , "search", filename);
 	}
 	
 	private KulerTheme[] makePalettes(final String querry, final String typ, String filename)
 	{
 //		String url = new StringBuffer(serverPage).append(typ).append(pageTyp).append("?itemsPerPage=").append(numResults).append("&startIndex=").append(resultOffset).append(querry).append(
 //				"&key=" + key).toString();
 		
 		String url = new StringBuffer(serverPage).append(typ).append(pageTyp).append("?").append(querry).append(
 				"&key=" + key).toString();
 		XML xml = getXML(url, filename);
 		ArrayList<KulerTheme> themes = new ArrayList<KulerTheme>();
 
 		if (xml.getChild("success") != null && xml.getChild("success").getContent().equals("false")) {
 
 			if (debugMode == true) {
 				p.println("The following error appears while calling kuler service:");
 				p.println(xml.getChild("error/errorText").getContent());
 			}
 
 		} else {
 			XML[] themeItems = xml.getChildren("channel/item/kuler:themeItem");
 
 			for (int i = 0; i < themeItems.length; i++) {
 
 				XML themeItem = themeItems[i];
 				XML[] themeSwatches = themeItem.getChildren("kuler:themeSwatches/kuler:swatch/kuler:swatchHexColor");
 				int[] colors = new int[themeSwatches.length];
 
 				for (int j = 0; j < themeSwatches.length; j++) {
 					colors[j] = PApplet.unhex("FF" + themeSwatches[j].getContent());
 				}
 
 				KulerTheme kulerTheme = new KulerTheme(p, colors);
 
 				kulerTheme.setThemeID(themeItem.getChildren("kuler:themeID")[0].getContent());
 				kulerTheme.setThemeTitle(themeItem.getChildren("kuler:themeTitle")[0].getContent());
 				kulerTheme.setAuthorID(themeItem.getChildren("kuler:themeAuthor/kuler:authorID")[0].getContent());
 				kulerTheme.setAuthorLabel(themeItem.getChildren("kuler:themeAuthor/kuler:authorLabel")[0].getContent());
 				kulerTheme.setThemeTags(themeItem.getChildren("kuler:themeTags")[0].getContent());
 				kulerTheme.setThemeRating(themeItem.getChildren("kuler:themeRating")[0].getContent());
 				kulerTheme.setThemeDownloadCount(themeItem.getChildren("kuler:themeDownloadCount")[0].getContent());
 				kulerTheme.setThemeCreatedAt(themeItem.getChildren("kuler:themeCreatedAt")[0].getContent());
 				kulerTheme.setThemeEditedAt(themeItem.getChildren("kuler:themeEditedAt")[0].getContent());
 
 				themes.add(kulerTheme);
 			}
 		}
 		
 		return (KulerTheme[]) themes.toArray(new KulerTheme[themes.size()]);
 	}
 
 
 	/**
 	 * As you can't connect to the Kuler service directly if you run your sketch
 	 * as an Applet, you can set a path to a php/cfm/... script. You can query the Kuler
 	 * service from that page and send the response XML to the Applet.
 	 * 
 	 * @param serverPath
 	 * @param pageType
 	 */
 	public void setserverPage(String serverPath, String pageType)
 	{
 		this.serverPage = serverPath;
 		this.pageTyp = pageType;
 	}
 
 }
