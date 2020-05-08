 package com.brotherlogic.untappdpicker.db;
 
 import java.util.Date;
 
 import org.junit.Assert;
 import org.junit.Test;
 
 import com.brotherlogic.booser.atom.Beer;
 import com.brotherlogic.booser.atom.Drink;
 import com.brotherlogic.booser.atom.User;
 import com.brotherlogic.booser.atom.Venue;
 import com.brotherlogic.booser.storage.db.DatabaseFactory;
 import com.brotherlogic.booser.storage.db.DatabaseLayer;
 
public class DatabaseTest
 {
    @Test
    public void storeBeer()
    {
       Beer b = new Beer(123, "testbeer", 12.5);
       DatabaseLayer layer = new DatabaseLayer(DatabaseFactory.getInstance().getDatabase());
       layer.store(b);
 
       Beer b2 = (Beer) layer.get(new Beer(b.getId()));
 
       Assert.assertEquals("User ids do not match", b.getId(), b2.getId());
       Assert.assertEquals("Beer names do not match", b.getBeerName(), b2.getBeerName());
       Assert.assertEquals("ABVs do not match", b.getAbv(), b2.getAbv());
    }
 
    @Test
    public void storeUser()
    {
       User u = new User("testuser");
       DatabaseLayer layer = new DatabaseLayer(DatabaseFactory.getInstance().getDatabase());
       layer.store(u);
 
       User u2 = (User) layer.get(new User(u.getId()));
 
       Assert.assertEquals("User ids do not match", u.getId(), u2.getId());
    }
 
    @Test
    public void storeVenue()
    {
       Venue v = new Venue("Blah", "TestName");
       v.setVenueType(789);
 
       // Add a drink to this venue
       Drink d = new Drink("did-test", new Beer(123, "Vlah", 12.5), new User("123"), v, new Date(
             123L));
       v.addDrink(d);
 
       DatabaseLayer layer = new DatabaseLayer(DatabaseFactory.getInstance().getDatabase());
       layer.store(v);
 
       Venue v2 = (Venue) layer.get(new Venue(v.getId()));
 
       Assert.assertEquals("Venues do not match", v, v2);
    }
 }
