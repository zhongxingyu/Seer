 package com.brotherlogic.booser.servlets;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.text.DateFormat;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.TreeMap;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.Cookie;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import com.brotherlogic.booser.Config;
 import com.brotherlogic.booser.atom.Atom;
 import com.brotherlogic.booser.atom.Beer;
 import com.brotherlogic.booser.atom.Drink;
 import com.brotherlogic.booser.atom.FoursquareVenue;
 import com.brotherlogic.booser.atom.User;
 import com.brotherlogic.booser.atom.Venue;
 import com.brotherlogic.booser.storage.AssetManager;
 import com.brotherlogic.booser.storage.db.Database;
 import com.brotherlogic.booser.storage.db.DatabaseFactory;
 import com.brotherlogic.booser.storage.web.Downloader;
 import com.brotherlogic.booser.storage.web.WebLayer;
 import com.google.gson.Gson;
 import com.google.gson.JsonArray;
 import com.google.gson.JsonElement;
 import com.google.gson.JsonObject;
 import com.google.gson.JsonParser;
 import com.google.gson.JsonPrimitive;
 
 public class APIEndpoint extends UntappdBaseServlet
 {
    //private static final String baseURL = "http://localhost:8080/";
   private static final String baseURL = "http://booser-beautiful.rhcloud.com/";
    private static final int COOKIE_AGE = 60 * 60 * 24 * 365;
    private static final String COOKIE_NAME = "untappdpicker_cookie";
 
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException,
          IOException
    {
       if (req.getParameter("action") != null && req.getParameter("action").equals("version"))
       {
          write(resp, "0.4");
          return;
       }
 
       String token = null;
 
       if (req.getParameter("notoken") == null)
       {
          token = req.getParameter("access_token");
          if (token == null)
             token = getUserToken(req, resp);
       }
 
       // If we're not logged in, server will redirect
       if (token != "not_logged_in")
       {
          AssetManager manager = AssetManager.getManager(token);
 
          String action = req.getParameter("action");
 
          if (action == null)
          {
             resp.sendRedirect("/");
             return;
          }
 
          if (action.equals("getVenue"))
          {
             double lat = Double.parseDouble(req.getParameter("lat"));
             double lon = Double.parseDouble(req.getParameter("lon"));
             List<Atom> venues = getFoursquareVenues(manager, lat, lon);
             processJson(resp, venues);
          }
          else if (action.equals("venueDetails"))
          {
             Database db = DatabaseFactory.getInstance().getDatabase();
             db.reset();
 
             Venue v = manager.getVenue(req.getParameter("vid"));
             List<JsonObject> retList = new LinkedList<JsonObject>();
             for (Entry<Beer, Integer> count : v.getBeerCounts().entrySet())
             {
                JsonObject nObj = new JsonObject();
                nObj.add("beer_name", new JsonPrimitive(count.getKey().getBeerName()));
                nObj.add("beer_style", new JsonPrimitive(count.getKey().getBeerStyle()));
                nObj.add("brewery_name", new JsonPrimitive(count.getKey().getBrewery()
                      .getBreweryName()));
                nObj.add("count", new JsonPrimitive(count.getValue()));
                nObj.add("beer_score", new JsonPrimitive(count.getKey().resolveScore(token)));
                retList.add(nObj);
             }
 
             // Simple sorting for now
             Collections.sort(retList, new Comparator<JsonObject>()
             {
                @Override
                public int compare(JsonObject arg0, JsonObject arg1)
                {
                   double diff = (arg1.get("beer_score").getAsDouble() - arg0.get("beer_score")
                         .getAsDouble()) * 1000;
                   System.out.println("DIFF = " + ((int) diff) + " from " + diff);
                   return (int) diff;
                }
             });
             System.out.println("TOP = " + retList.get(0).get("beer_score").getAsDouble());
 
             JsonArray retArr = new JsonArray();
             for (JsonObject obj : retList)
                retArr.add(obj);
             write(resp, retArr);
          }
          else if (action.equals("getNumberOfDrinks"))
          {
             Database db = DatabaseFactory.getInstance().getDatabase();
             db.reset();
 
             User u = manager.getUser();
 
             List<Drink> beers = u.getDrinks();
             JsonObject obj = new JsonObject();
             obj.add("drinks", new JsonPrimitive(beers.size()));
             obj.add("username", new JsonPrimitive(u.getId()));
             write(resp, obj);
 
          }
          else if (action.equals("getDrinks"))
          {
             Database db = DatabaseFactory.getInstance().getDatabase();
             db.reset();
 
             User u = manager.getUser();
             List<Drink> drinks = u.getDrinks();
 
             Map<Beer, Integer> counts = new TreeMap<Beer, Integer>();
             Map<Beer, Double> beers = new TreeMap<Beer, Double>();
             for (Drink d : drinks)
             {
                if (!counts.containsKey(d.getBeer()))
                   counts.put(d.getBeer(), 1);
                else
                   counts.put(d.getBeer(), counts.get(d.getBeer()) + 1);
 
                beers.put(d.getBeer(), d.getRating_score());
             }
             Gson gson = new Gson();
             JsonArray arr = new JsonArray();
             for (Beer b : beers.keySet())
             {
                JsonObject obj = gson.toJsonTree(b).getAsJsonObject();
                obj.add("rating", new JsonPrimitive(beers.get(b)));
                obj.add("drunkcount", new JsonPrimitive(counts.get(b)));
                arr.add(obj);
             }
 
             write(resp, arr);
 
          }
          else if (action.equals("getStyle"))
          {
             String style = req.getParameter("style");
 
             if (style.equals("null"))
                style = "";
 
             Database db = DatabaseFactory.getInstance().getDatabase();
             db.reset();
 
             User u = manager.getUser();
             List<Drink> drinks = u.getDrinks();
 
             Map<Beer, Integer> counts = new TreeMap<Beer, Integer>();
             Map<Beer, Double> rating = new TreeMap<Beer, Double>();
             for (Drink d : drinks)
                if (style.length() == 0 || d.getBeer().getBeerStyle().equals(style))
                {
                   if (!counts.containsKey(d.getBeer()))
                      counts.put(d.getBeer(), 1);
                   else
                      counts.put(d.getBeer(), counts.get(d.getBeer()) + 1);
 
                   rating.put(d.getBeer(), d.getRating_score());
                }
 
             Gson gson = new Gson();
             JsonArray arr = new JsonArray();
             for (Beer b : counts.keySet())
             {
                JsonObject obj = gson.toJsonTree(b).getAsJsonObject();
                obj.add("drunkcount", new JsonPrimitive(counts.get(b)));
                obj.add("rating", new JsonPrimitive(rating.get(b)));
                arr.add(obj);
             }
 
             write(resp, arr);
 
          }
          else if (action.equals("getBeer"))
          {
             String beerId = req.getParameter("beer");
             DateFormat df = DateFormat.getInstance();
 
             Database db = DatabaseFactory.getInstance().getDatabase();
             db.reset();
 
             User u = manager.getUser();
             List<Drink> drinks = u.getDrinks();
             List<Drink> show = new LinkedList<Drink>();
 
             for (Drink d : drinks)
                if (d.getBeer().getId().equals(beerId))
                   show.add(d);
 
             Gson gson = new Gson();
             JsonArray arr = new JsonArray();
             for (Drink d : show)
             {
                JsonObject obj = gson.toJsonTree(d.getBeer()).getAsJsonObject();
                obj.add("date", new JsonPrimitive(df.format(d.getCreated_at().getTime())));
                if (d.getPhoto_url() != null)
                   obj.add("photo", new JsonPrimitive(d.getPhoto_url()));
                obj.add("rating", new JsonPrimitive(d.getRating_score()));
                arr.add(obj);
             }
 
             write(resp, arr);
 
          }
          else if (action.equals("users"))
          {
             Database db = DatabaseFactory.getInstance().getDatabase();
             db.reset();
 
             List<Atom> users = manager.getUsers();
             JsonArray arr = new JsonArray();
             for (Atom user : users)
                arr.add(new JsonPrimitive(user.getId()));
             write(resp, arr);
 
          }
       }
    }
 
    public List<Atom> getFoursquareVenues(AssetManager manager, double lat, double lon)
    {
       try
       {
          WebLayer layer = new WebLayer(manager);
          List<Atom> atoms = layer.getLocal(FoursquareVenue.class,
                new URL("https://api.foursquare.com/v2/venues/search?ll=" + lat + "," + lon
                      + "&client_id=" + Config.getFoursquareClientId() + "&" + "client_secret="
                      + Config.getFoursquareSecret() + "&v=20130118"), false);
 
          return atoms;
       }
       catch (MalformedURLException e)
       {
          e.printStackTrace();
       }
 
       return new LinkedList<Atom>();
    }
 
    public String getUserDetails(String userToken) throws IOException
    {
       User u = AssetManager.getManager(userToken).getUser();
       return u.getJson().toString();
    }
 
    /**
     * Gets the user token from the request
     * 
     * @param req
     *           The servlet request object
     * @return The User Token
     */
    private String getUserToken(final HttpServletRequest req, final HttpServletResponse resp)
    {
       if (req.getCookies() != null)
          for (Cookie cookie : req.getCookies())
             if (cookie.getName().equals(COOKIE_NAME))
                return cookie.getValue();
 
       // Forward the thingy on to the login point
       try
       {
          // Check we're not in the login process
          if (req.getParameter("code") != null)
          {
             String response = Downloader.getInstance().download(
                   new URL("https://untappd.com/oauth/authorize/?client_id="
                         + Config.getUntappdClient() + "&client_secret=" + Config.getUntappdSecret()
                         + "&response_type=code&redirect_url=" + baseURL + "API&code="
                         + req.getParameter("code")));
 
             // Get the access token
             JsonParser parser = new JsonParser();
             String nToken = parser.parse(response).getAsJsonObject().get("response")
                   .getAsJsonObject().get("access_token").getAsString();
 
             // Set the cookie
             Cookie cookie = new Cookie(COOKIE_NAME, nToken);
             cookie.setMaxAge(COOKIE_AGE);
             cookie.setPath("/");
             resp.addCookie(cookie);
 
             return nToken;
          }
          else
             resp.sendRedirect("http://untappd.com/oauth/authenticate/?client_id="
                   + Config.getUntappdClient() + "&client_secret=" + Config.getUntappdSecret()
                   + "&response_type=code&redirect_url=" + baseURL + "API");
       }
       catch (IOException e)
       {
          e.printStackTrace();
       }
 
       return "not_logged_in";
    }
 
    private void processJson(HttpServletResponse resp, List<Atom> atoms) throws IOException
    {
       JsonArray arr = new JsonArray();
       for (Atom atom : atoms)
          arr.add(atom.getJson());
       write(resp, arr.toString());
    }
 
    private void write(HttpServletResponse resp, JsonElement obj) throws IOException
    {
       write(resp, obj.toString());
    }
 
    private void write(HttpServletResponse resp, String jsonString) throws IOException
    {
       resp.setContentType("application/json");
       PrintWriter out = resp.getWriter();
       String convString = new String(jsonString.getBytes("UTF-8"), "US-ASCII");
       out.print(convString);
       out.close();
    }
 }
