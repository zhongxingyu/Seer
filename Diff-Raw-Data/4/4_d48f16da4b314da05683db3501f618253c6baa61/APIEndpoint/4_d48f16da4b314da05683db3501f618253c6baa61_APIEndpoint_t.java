 package com.brotherlogic.booser.servlets;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
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
   // private static final String baseURL = "http://localhost:8080/";
   private static final String baseURL = "http://booser-beautiful.rhcloud.com/";
    private static final int COOKIE_AGE = 60 * 60 * 24 * 365;
    private static final String COOKIE_NAME = "untappdpicker_cookie";
 
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException,
          IOException
    {
       String token = getUserToken(req, resp);
 
       System.out.println("Got the user token");
 
       // If we're not logged in, server will redirect
       if (token != "not_logged_in")
       {
          AssetManager manager = AssetManager.getManager(token);
 
          String action = req.getParameter("action");
 
          if (action == null)
             resp.sendRedirect("/");
 
          if (action.equals("getVenue"))
          {
             double lat = Double.parseDouble(req.getParameter("lat"));
             double lon = Double.parseDouble(req.getParameter("lon"));
             List<Atom> venues = getFoursquareVenues(lat, lon);
             processJson(resp, venues);
          }
          else if (action.equals("getNumberOfDrinks"))
          {
             User u = manager.getUser();
 
             List<Drink> beers = u.getDrinks();
             JsonObject obj = new JsonObject();
             obj.add("drinks", new JsonPrimitive(beers.size()));
             obj.add("username", new JsonPrimitive(u.getId()));
             write(resp, obj);
          }
          else if (action.equals("getDrinks"))
          {
             User u = manager.getUser();
             List<Drink> drinks = u.getDrinks();
 
             Map<Beer, Double> beers = new TreeMap<Beer, Double>();
             for (Drink d : drinks)
                beers.put(d.getBeer(), d.getRating_score());
 
             Gson gson = new Gson();
             JsonArray arr = new JsonArray();
             for (Beer b : beers.keySet())
             {
                JsonObject obj = gson.toJsonTree(b).getAsJsonObject();
                obj.add("rating", new JsonPrimitive(beers.get(b)));
                arr.add(obj);
             }
 
             write(resp, arr);
          }
       }
    }
 
    public List<Atom> getFoursquareVenues(double lat, double lon)
    {
       try
       {
          WebLayer layer = new WebLayer();
          List<Atom> atoms = layer.getLocal(FoursquareVenue.class,
                new URL("https://api.foursquare.com/v2/venues/search?ll=" + lat + "," + lon
                      + "&client_id=" + Config.getFoursquareClientId() + "&" + "client_secret="
                      + Config.getFoursquareSecret() + "&v=20130118"));
 
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
       return u.getJson();
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
             Thread.dumpStack();
 
             String response = Downloader.getInstance().download(
                   new URL("https://untappd.com/oauth/authorize/?client_id="
                         + Config.getUntappdClient() + "&client_secret=" + Config.getUntappdSecret()
                         + "&response_type=code&redirect_url=" + baseURL + "API&code="
                         + req.getParameter("code")));
 
             System.out.println("RESP = " + response);
 
             // Get the access token
             JsonParser parser = new JsonParser();
             String nToken = parser.parse(response).getAsJsonObject().get("response")
                   .getAsJsonObject().get("access_token").getAsString();
 
             // Set the cookie
             Cookie cookie = new Cookie(COOKIE_NAME, nToken);
             cookie.setMaxAge(COOKIE_AGE);
             cookie.setPath("/");
             resp.addCookie(cookie);
 
             System.out.println("RETURNING");
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
 
    public List<Venue> getVenues(String userToken, double latitude, double longitude)
    {
       List<Atom> fsVenues = getFoursquareVenues(latitude, longitude);
       List<Venue> venues = new LinkedList<Venue>();
       for (Atom fsVenue : fsVenues)
          venues.add(AssetManager.getManager(userToken).getVenue(fsVenue.getId()));
       return venues;
    }
 
    private void processJson(HttpServletResponse resp, List<Atom> atoms) throws IOException
    {
       StringBuffer ret = new StringBuffer("[");
       ret.append(atoms.get(0).toString());
       for (Atom atom : atoms)
          ret.append("," + atom.getJson());
       ret.append("]");
 
       write(resp, ret.toString());
    }
 
    private void write(HttpServletResponse resp, JsonElement obj) throws IOException
    {
       write(resp, obj.toString());
    }
 
    private void write(HttpServletResponse resp, String jsonString) throws IOException
    {
       resp.setContentType("application/json");
       PrintWriter out = resp.getWriter();
       out.print(jsonString);
       out.close();
    }
 }
