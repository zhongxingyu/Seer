 package amazon.awis;
 
 import static org.junit.Assert.*;
 import org.junit.Test;
 
 public class AwisTest{
 
   @Test(timeout=10000)
   public void testMakeUrl() throws Exception{
     String url = "http://www.gmail.com";
     String timestamp = "2011-01-05T21%3A36%3A37.930Z";
     String awisUrl = Awis.makeUrl("AAAAAA", "BBBBBB", url, timestamp);
     
     String expected = "http://awis.amazonaws.com?&Action=UrlInfo&ResponseGroup=TrafficData,LinksInCount&AWSAccessKeyId=AAAAAA&Signature=HET%2BlbWGu7gEqKlIhMl%2Bvn3m3zU%3D&Timestamp=2011-01-05T21%253A36%253A37.930Z&Url=http%3A%2F%2Fwww.gmail.com";
     
     assertEquals(expected, awisUrl);
   }
 
  @Test
   public void testGet() throws Exception{
     String accessKey = "";
     String secretKey = "";
     String siteUrl = "http://foursquare.com";
     String output = Awis.get(accessKey,secretKey,siteUrl);
     
    assert(output == null);
   }
 }
