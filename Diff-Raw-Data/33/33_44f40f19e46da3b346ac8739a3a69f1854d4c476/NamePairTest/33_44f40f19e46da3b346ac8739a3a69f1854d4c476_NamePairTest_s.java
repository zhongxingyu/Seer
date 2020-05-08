 package org.thoughtworks.orc.internal.util;
 
 import junit.framework.Assert;
import org.testng.annotations.Test;
 
 public class NamePairTest {
     @Test
     public void test_split_url() throws Exception {
         assertPair("/some/test", "some", "test");
     }
 
     @Test
     public void test_url_not_end() throws Exception {
         assertPair("/some/test/with/more", "some", "test");
     }
 
    @Test(expectedExceptions = RuntimeException.class)
     public void test_url_not_start_with_slash() throws Exception {
         assertPair("some/test", "some", "test");
     }
 
     private void assertPair(String url, String route, String action) {
         final NamePair pair = NamePair.parseUrl(url);
         Assert.assertEquals("route should be some", pair.route, route);
         Assert.assertEquals("action should be test", pair.action, action);
     }
 }
