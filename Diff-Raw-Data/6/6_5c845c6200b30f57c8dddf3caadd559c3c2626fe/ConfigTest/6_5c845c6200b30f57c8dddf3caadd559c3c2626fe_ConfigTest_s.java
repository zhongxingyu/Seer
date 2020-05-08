 package com.brotherlogic.beer;
 
 import org.junit.Assert;
 import org.junit.Test;
 
 import com.brotherlogic.beer.actions.untappd.DrinkFinder;
 
 /**
  * Tests that the config system is working properly
  * 
  * @author simon
  * 
  */
 public class ConfigTest extends TestBase
 {
    /**
     * Tests that we can retrieve the config
     */
    @Test
    public void testConfigRetrieve()
    {
       DrinkFinder df = new DrinkFinder("blah");
<<<<<<< HEAD
       System.out.println(System.getenv());
      Assert.assertNotNull("Config is null", df.getConfig("UT_SECRET"));
=======
       Assert.assertNotNull("Config is null", df.getConfig("FoursquareClient"));
>>>>>>> 845b9b3dd9ded6a4ef78e1f0cb280e4c0bc6add7
    }
 
 }
