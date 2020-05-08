 package modules;
 
 import static org.jibble.pircbot.Colors.*;
 
 import java.util.*;
 import java.io.DataInputStream;
 import java.io.IOException;
 import java.net.URL;
 import java.net.URLConnection;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
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
 	private static final String WEATHER_URL = "http://weather.yahooapis.com/forecastjson?w=";
 	private static final String COLOR_INFO = PURPLE;
 	private static final String COLOR_LOC = CYAN;
 	private static final String COLOR_TEXT = YELLOW;
 	private static final String COLOR_TEMP = MAGENTA;
 	private static final String COLOR_ERROR = RED + REVERSE;
 
 	private static final String[][] cities = {
 		{ "12778384", "Terre Haute", "IN"},
 		{ "2357536",  "Austin", "TX"},
 		{ "2374418",  "Canoga Park", "CA"},
 		{ "2401279",  "Fairbanks", "AK"},
 		{ "2402488",  "Farmington Hills", "MI"},
 		{ "2470874",  "Petaluma", "CA"},
 		{ "2490383",  "Seattle", "WA"},
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
 
 	private static JSONObject getJSON(String url) throws IOException, JSONException {
 		final URLConnection c = new URL(url).openConnection();
 		final DataInputStream s = new DataInputStream(c.getInputStream());
 		final StringBuffer b = new StringBuffer();
 		
 		byte[] buffer = new byte[1024];
 		int size;
 		while((size = s.read(buffer)) >= 0)
 			b.append(new String(buffer, 0, size));
 
 		return new JSONObject(b.toString());
 	}
 
 	public List<Map<String,String>> getWeather()
 	{
 		ArrayList list = new ArrayList();
 		try {
 			for (int i = 0; i < cities.length; i++) {
 				final String uri = WEATHER_URL + cities[i][0];
 				final JSONObject cond = getJSON(uri).getJSONObject("condition");;
 
 				String icon = cond.getString("text");
 				for (int j = 0; j < icons.length; j++)
 					icon = icon.replace(icons[j][0], icons[j][1]);
 				String txt = cond.getString("text");
 				for (int j = 0; j < names.length; j++)
 					txt = txt.replace(names[j][0], names[j][1]);
 
 				HashMap map = new HashMap();
 				map.put("city",  cities[i][1]);
 				map.put("state", cities[i][2]);
 				map.put("temp",  "" + cond.getInt("temperature"));
 				map.put("text",  cond.getString("text"));
 				map.put("txt",   txt.toLowerCase());
 				map.put("icon",  icon);
 				list.add(map);
 			}
 		} catch (Exception e) {
 			this.bot.sendMessage(COLOR_ERROR + "Problem parsing Weather data");
 		}
 		return list;
 	}
 
 
 	@Command(".weather")
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
 		this.bot.sendMessage(implode(list.toArray(new String[0]), " "));
 	}
 
 	@Command(".wx")
 	public void wx(Message message)
 	{
 		List<String> list = new ArrayList();
		for (Map<String,String> cond : getWeather())
 			list.add(
				cond.get("city") + " " +
				cond.get("temp") + " " +
				cond.get("txt"));
 		this.bot.sendMessage(implode(list.toArray(new String[0]), "  |  "));
 	}
 
 	@Override public String getFriendlyName() { return "Weather"; }
 	@Override public String getDescription() { return "Outputs the current weather conditions in cities occupied by #rhnoise"; }
 	@Override public String[] getExamples() { return new String[] { ".weather" }; }
 }
