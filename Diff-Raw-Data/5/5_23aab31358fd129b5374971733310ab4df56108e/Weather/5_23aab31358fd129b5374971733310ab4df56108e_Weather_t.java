 package modules;
 
 import static org.jibble.pircbot.Colors.*;
 
 import javax.xml.parsers.*;
 import org.w3c.dom.*;
 import org.xml.sax.*;
 
 import java.util.*;
 import java.io.DataInputStream;
 import java.io.IOException;
 import java.net.URL;
 import java.net.URLConnection;
 
 import main.Message;
 import main.NoiseModule;
 
 import static panacea.Panacea.*;
 
 /**
  * Weather
  * Based off the SO module
  *
  * @author Michael Auchter
  *         Created Sep 23, 2011.
  */
 public class Weather extends NoiseModule
 {
 	private static final String WEATHER_URL = "http://weather.yahooapis.com/forecastrss?w=";
 	private static final String COLOR_INFO = PURPLE;
 	private static final String COLOR_LOC = CYAN;
 	private static final String COLOR_TEXT = YELLOW;
 	private static final String COLOR_TEMP = MAGENTA;
 	private static final String COLOR_ERROR = RED + REVERSE;
 	private static final String COLOR_NORMAL = NORMAL;
 
 	private static final String[][] cities = {
 		{ "12778384", "Terre Haute", "IN"},
 		{ "2357536",  "Austin", "TX"},
 		{ "2374418",  "Canoga Park", "CA"},
 		{ "2401279",  "Fairbanks", "AK"},
 		{ "2402488",  "Farmington Hills", "MI"},
 		{ "2470874",  "Petaluma", "CA"},
 		{ "2490383",  "Seattle", "WA"},
 		{ "12794706", "Tucson", "AZ"},
 		{ "2517274",  "West Lafayette", "IN"},
 	};
 
 	private static final String[][] icons = {
 		/* You may need to install "Symbolata" to see the first two symbols */
 		{"Partly Cloudy", "\u00e2\u009b\u0085"}, /* SUN BEHIND CLOUD */
 		{"Thunderstorms", "\u00e2\u009b\u0088"}, /* THUNDER CLOUD AND RAIN */
 		/* The rest are in DejaVu Sans */
 		{"Cloudy", "\u00e2\u0098\u0081"}, /* CLOUD */
 		{"Rain", /*"\u00e2\u0098\u0094"*/ "\u00e2\u009b\u0086"}, /* UMBRELLA WITH RAIN DROPS */
 		{"Snow", "\u00e2\u0098\u0083"}, /* SNOWMAN */
 		{"Sunny", "\u00e2\u0098\u0080"}, /* BLACK SUN WITH RAYS */
 		{"Hail", "\u00e2\u0098\u0084"}, /* COMET */
 	};
 
 	private static final String[][] names = {
 		{"Partly Cloudy", "cloudy-"},
 		{"Mostly Cloudy", "cloudy+"},
 		{"Thunderstorms", "storms"},
 	};
 
 	private static Document getXML(String url) throws Exception {
 		final DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
 		final InputSource src = new InputSource(new URL(url).openStream());
 		return db.parse(src);
 	}
 
 	public List<Map<String,String>> getWeather()
 	{
 		ArrayList list = new ArrayList();
 		try {
 			for (int i = 0; i < cities.length; i++) {
 				final String uri = WEATHER_URL + cities[i][0];
 				final Node cond = getXML(uri).getElementsByTagName("yweather:condition").item(0);
 				final NamedNodeMap attrs = cond.getAttributes();
 
 				String icon = attrs.getNamedItem("text").getNodeValue();
 				for (int j = 0; j < icons.length; j++)
 					icon = icon.replace(icons[j][0], icons[j][1]);
 				String txt = attrs.getNamedItem("text").getNodeValue();
 				for (int j = 0; j < names.length; j++)
 					txt = txt.replace(names[j][0], names[j][1]);
 
 				HashMap map = new HashMap();
 				map.put("city",  cities[i][1]);
 				map.put("state", cities[i][2]);
 				map.put("temp",  "" + attrs.getNamedItem("temp").getNodeValue());
 				map.put("text",  attrs.getNamedItem("text").getNodeValue());
 				map.put("txt",   txt.toLowerCase());
 				map.put("icon",  icon);
 				list.add(map);
 			}
 		} catch (Exception e) {
 			this.bot.sendMessage(COLOR_ERROR + "Problem parsing Weather data");
 		}
 		return list;
 	}
 
 
	@Command("\\.weather")
 	public void weather(Message message)
 	{
 		List<String> list = new ArrayList();
 		for (Map<String,String> wx : getWeather())
 			list.add(
 				COLOR_INFO + "[" +
 				COLOR_LOC  + wx.get("city") + ", " + wx.get("state") +
 				COLOR_INFO + ": " +
 				COLOR_TEXT + wx.get("text") +
 				COLOR_INFO + ", " +
 				COLOR_TEMP + wx.get("temp") + "F" +
 				COLOR_INFO + "]");
 		this.bot.sendMessage(list.toArray(new String[0]), " ");
 	}
 
	@Command("\\.wx")
 	public void wx(Message message)
 	{
 		List<String> list = new ArrayList();
 		for (Map<String,String> wx : getWeather())
 			list.add(
 				COLOR_LOC  + wx.get("city") + " " +
 				COLOR_TEMP + wx.get("temp") + " " +
 				COLOR_TEXT + wx.get("txt")  + COLOR_NORMAL);
 		this.bot.sendMessage(list.toArray(new String[0]), "  |  ");
 	}
 
 	@Override public String getFriendlyName() { return "Weather"; }
 	@Override public String getDescription() { return "Outputs the current weather conditions in cities occupied by #rhnoise"; }
 	@Override public String[] getExamples() { return new String[] { ".weather" }; }
 }
