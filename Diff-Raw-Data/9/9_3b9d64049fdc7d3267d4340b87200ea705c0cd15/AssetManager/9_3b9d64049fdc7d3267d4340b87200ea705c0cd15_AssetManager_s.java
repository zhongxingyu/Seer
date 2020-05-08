 package com.brotherlogic.beer.db;
 
 import java.io.IOException;
 import java.sql.SQLException;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.TreeMap;
 import java.util.TreeSet;
 
 import com.brotherlogic.beer.actions.foursqure.FourSquareVenue;
 import com.brotherlogic.beer.actions.untappd.Beer;
 import com.brotherlogic.beer.actions.untappd.BeerScoreFinder;
 import com.brotherlogic.beer.actions.untappd.DrinkFinder;
 import com.brotherlogic.beer.actions.untappd.VenueFeedFinder;
 import com.brotherlogic.beer.actions.untappd.VenueFinder;
 import com.brotherlogic.beer.db.sqlite.SQLiteDB;
 
 public class AssetManager
 {
    static Database db;
    private static String lastProps = "";
    private static Map<String, AssetManager> singletonMap = new TreeMap<String, AssetManager>();
    private static Set<String> tokenSet = new TreeSet<String>();
 
    public synchronized static AssetManager getInstance(String token)
    {
       return getInstance(token, lastProps);
    }
 
    public synchronized static AssetManager getInstance(String token, String props)
    {
       if (singletonMap.get(token) == null)
       {
          AssetManager manager = new AssetManager(props);
          manager.userToken = token;
          singletonMap.put(token, manager);
          lastProps = props;
       }
 
       return singletonMap.get(token);
    }
 
    public static void setToken(String token)
    {
       tokenSet.add(token);
    }
 
    public static void stopAll()
    {
       for (AssetManager manager : singletonMap.values())
          manager.stop();
    }
 
    private String dbName = "";
 
    private String userName;
 
    private String userToken;
 
    private AssetManager(String name)
    {
       try
       {
          connect(name);
       }
       catch (SQLException e)
       {
          e.printStackTrace();
       }
    }
 
    public void addDrink(Drink d)
    {
       d.setDB(db);
    }
 
    public void beerOff(BeerOff off)
    {
       // Mark this to be stored in the database
       off.setDB(db);
    }
 
    private void connect() throws SQLException
    {
       connect(dbName);
    }
 
    private void connect(String name) throws SQLException
    {
       if (db == null)
       {
          dbName = name;
          db = new SQLiteDB(name);
          db.connect();
       }
    }
 
    public BeerScore getScore(Beer b) throws IOException
    {
       try
       {
          connect();
          BeerScore score = db.getBeerScore(b);
          if (score != null)
             return score;
 
          BeerScoreFinder finder = new BeerScoreFinder(userToken);
          BeerScore bs = finder.getScore(b);
 
          // If we can't retrieve the score, use a default score of zero
          if (bs != null)
             bs.setDB(db);
          else
            bs = new BeerScore(b.getId(), 0);
          return bs;
       }
       catch (Exception e)
       {
          e.printStackTrace();
       }
 
       return null;
    }
 
    public User getUser(String name) throws IOException
    {
       // Pull the user from the database
       try
       {
          connect();
          User u = db.getUser(name);
 
          // Update the users checkins
          DrinkFinder df = new DrinkFinder(userToken);
          List<Drink> drinks = df.getRecentDrinks(u);
 
          long lastDrunkTime = u.getLastDrunkTime();
          for (Drink drink : drinks)
             if (drink.getDrunkTime() > lastDrunkTime)
                u.addDrink(drink);
             else
                break;
 
          return u;
       }
       catch (SQLException e)
       {
          throw new IOException(e);
       }
    }
 
    public Venue getVenue(int id) throws IOException
    {
       try
       {
          connect();
          Venue v = db.getVenue(id);
 
          // Update the users checkins
          VenueFeedFinder vff = new VenueFeedFinder(userToken);
          List<Drink> drinks = vff.getRecentDrinks(v);
 
          long lastDrunkTime = v.getLastDrunkTime();
          for (Drink drink : drinks)
             if (drink.getDrunkTime() > lastDrunkTime)
                v.addDrink(drink);
             else
                break;
 
          return v;
       }
       catch (SQLException e)
       {
          e.printStackTrace();
          throw new IOException(e);
       }
    }
 
    public Venue getVenue(String id) throws IOException
    {
       Venue v = null;
 
       try
       {
          connect();
          v = db.getVenue(id);
 
          // Force load the feed
          if (v != null)
             return getVenue(v.getId());
       }
       catch (SQLException e)
       {
          e.printStackTrace();
       }
 
       System.out.println("USER = " + userToken);
       if (userToken == null)
          System.exit(1);
       VenueFinder finder = new VenueFinder(userToken);
       Venue vTemp = finder.getVenue(id);
 
       if (vTemp != null)
       {
          vTemp.setDB(db);
 
          FourSquareVenue fsv = new FourSquareVenue(id, vTemp.getName(), vTemp.getId());
          fsv.setDB(db);
 
          return getVenue(vTemp.getId());
       }
 
       return new Venue(-1);
    }
 
    public void stop()
    {
       try
       {
          db.disconnect();
       }
       catch (SQLException e)
       {
          e.printStackTrace();
       }
    }
 
    public void wipe() throws SQLException
    {
       connect();
       db.wipe();
    }
 }
