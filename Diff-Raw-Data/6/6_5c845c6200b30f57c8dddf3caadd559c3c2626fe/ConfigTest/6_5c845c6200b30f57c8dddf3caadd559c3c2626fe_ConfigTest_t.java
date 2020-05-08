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
       System.out.println(System.getenv());
       Assert.assertNotNull("Config is null", df.getConfig("FoursquareClient"));
    }
 
 }
