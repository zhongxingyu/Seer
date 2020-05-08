 package com.brotherlogic.beer.db.sqlite;
 
 import java.io.File;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.LinkedList;
 import java.util.List;
 
 import com.brotherlogic.beer.DBObject;
 import com.brotherlogic.beer.actions.foursqure.FourSquareVenue;
 import com.brotherlogic.beer.actions.untappd.Beer;
 import com.brotherlogic.beer.db.BeerOff;
 import com.brotherlogic.beer.db.BeerScore;
 import com.brotherlogic.beer.db.Database;
 import com.brotherlogic.beer.db.Drink;
 import com.brotherlogic.beer.db.User;
 import com.brotherlogic.beer.db.Venue;
 
 public class SQLiteDB extends Database
 {
    private Connection db;
    private String dbName = "pickerv5";
 
    public SQLiteDB(String name)
    {
       dbName += name + ".db";
    }
 
    @Override
    protected void delete() throws SQLException
    {
       doDisconnect();
       if (new File(dbName).exists())
          new File(dbName).delete();
    }
 
    @Override
    public void doConnect() throws SQLException
    {
       boolean tablesNeeded = !(new File(dbName).exists());
 
       try
       {
          Class.forName("org.sqlite.JDBC");
          db = DriverManager.getConnection("jdbc:sqlite:" + dbName);
 
          if (tablesNeeded)
          {
             Statement s = db.createStatement();
             s.execute("CREATE TABLE drunk (id integer, time integer, user text, vid integer, beername text)");
             s.execute("CREATE TABLE score (bid integer, score double, time integer)");
             s.execute("CREATE TABLE fsvenue (fid text, uid integer)");
             s.execute("CREATE TABLE beersoff (vid integer, bid integer, time integer)");
          }
       }
       catch (Exception e)
       {
          throw new SQLException(e);
       }
    }
 
    @Override
    public void doDisconnect() throws SQLException
    {
       db.close();
    }
 
    @Override
    protected BeerScore retrieveBeerScore(Beer b) throws SQLException
    {
       PreparedStatement s = db.prepareStatement("SELECT score,time from score where bid = ?");
       s.setInt(1, b.getId());
       ResultSet rs = s.executeQuery();
       if (rs.next())
       {
         BeerScore bs = new BeerScore(b.getId(), rs.getDouble(1), rs.getLong(2), this);
          return bs;
       }
       else
          return null;
    }
 
    @Override
    protected List<BeerOff> retrieveOffs(Venue venue) throws SQLException
    {
       List<BeerOff> offs = new LinkedList<BeerOff>();
       PreparedStatement s = db.prepareStatement("SELECT bid,time from beersoff where vid = ?");
       s.setInt(1, venue.getId());
       ResultSet rs = s.executeQuery();
       while (rs.next())
       {
          BeerOff off = new BeerOff(venue.getId(), rs.getInt(1), rs.getLong(2), this);
          offs.add(off);
       }
 
       return offs;
    }
 
    @Override
    protected User retrieveUser(String name) throws SQLException
    {
       User u = new User(name);
       u.setDB(this);
 
       PreparedStatement s = db
             .prepareStatement("SELECT id,time,vid,beername from drunk where user = ?");
       s.setString(1, name);
       ResultSet rs = s.executeQuery();
       while (rs.next())
       {
          Drink d = new Drink(new Beer(rs.getInt(1), rs.getString(4)), rs.getLong(2), u,
                rs.getInt(3), this);
          u.addDrink(d);
       }
 
       return u;
    }
 
    @Override
    protected Venue retrieveVenue(int id) throws SQLException
    {
       Venue v = new Venue(id);
       v.setDB(this);
 
       PreparedStatement s = db
             .prepareStatement("SELECT id,time,user,beername from drunk where vid = ?");
       s.setInt(1, id);
       ResultSet rs = s.executeQuery();
       while (rs.next())
       {
          Drink d = new Drink(new Beer(rs.getInt(1), rs.getString(4)), rs.getLong(2), new User(
                rs.getString(3)), v.getId(), this);
          v.addDrink(d);
       }
 
       return v;
    }
 
    @Override
    protected Venue retrieveVenue(String id) throws SQLException
    {
       PreparedStatement s = db.prepareStatement("SELECT uid from fsvenue where fid = ?");
       s.setString(1, id);
       ResultSet rs = s.executeQuery();
       if (rs.next())
       {
          int uid = rs.getInt(1);
          rs.close();
          return getVenue(uid);
       }
 
       return null;
    }
 
    @Override
    protected boolean storeObject(DBObject object) throws SQLException
    {
       if (object instanceof Drink)
       {
          Drink d = (Drink) object;
          PreparedStatement s = db
                .prepareStatement("INSERT into drunk (user,id,time,vid,beername) VALUES (?,?,?,?,?)");
          s.setString(1, d.getUser().getName());
          s.setInt(2, d.getBeer().getId());
          s.setLong(3, d.getDrunkTime());
          s.setInt(4, d.getVenueID());
          s.setString(5, d.getBeer().getName());
          s.execute();
          return true;
       }
       else if (object instanceof BeerScore)
       {
          BeerScore score = (BeerScore) object;
          PreparedStatement s = db
                .prepareStatement("INSERT into score(bid,score,time) VALUES (?,?,?)");
          s.setInt(1, score.getBid());
          s.setDouble(2, score.getScore());
          s.setLong(3, score.getScore_retrieved());
          s.execute();
          return true;
       }
       else if (object instanceof FourSquareVenue)
       {
          FourSquareVenue venue = (FourSquareVenue) object;
          PreparedStatement s = db.prepareStatement("INSERT INTO fsvenue(fid,uid) VALUES (?,?)");
          s.setString(1, venue.getId());
          s.setInt(2, venue.getUTId());
          s.execute();
       }
       else if (object instanceof BeerOff)
       {
          BeerOff off = (BeerOff) object;
          PreparedStatement s = db
                .prepareStatement("INSERT INTO beersoff(bid,vid,time) VALUES (?,?,?)");
          s.setInt(1, off.getBeerId());
          s.setInt(2, off.getVenueId());
          s.setLong(3, off.getTime());
          s.execute();
       }
       else
          System.out.println("Cannot handle: " + object.getClass());
 
       return false;
    }
 }
