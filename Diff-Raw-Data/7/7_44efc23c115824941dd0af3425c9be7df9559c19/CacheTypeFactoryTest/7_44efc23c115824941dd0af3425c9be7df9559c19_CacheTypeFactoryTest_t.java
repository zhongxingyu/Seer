 package com.google.code.geobeagle;
 
 import static org.junit.Assert.assertEquals;
 
 import com.google.code.geobeagle.CacheTypeFactory;
 
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.powermock.core.classloader.annotations.PrepareForTest;
 import org.powermock.modules.junit4.PowerMockRunner;
 
 
 @PrepareForTest( {
     CacheTypeFactory.class
 })
 @RunWith(PowerMockRunner.class)
 public class CacheTypeFactoryTest {
 
     @Test
     public void testContainer() {
         CacheTypeFactory cacheTypeFactory = new CacheTypeFactory();
         assertEquals(0, cacheTypeFactory.container("bad string"));
         assertEquals(1, cacheTypeFactory.container("Micro"));
         assertEquals(2, cacheTypeFactory.container("Small"));
         assertEquals(3, cacheTypeFactory.container("Regular"));
         assertEquals(4, cacheTypeFactory.container("Large"));
     }
 
     @Test
     public void testCacheType() {
         CacheTypeFactory cacheTypeFactory = new CacheTypeFactory();
         assertEquals(CacheType.NULL, cacheTypeFactory.fromTag("bad string"));
         assertEquals(CacheType.TRADITIONAL, cacheTypeFactory.fromTag("Traditional Cache"));
        assertEquals(CacheType.TRADITIONAL, cacheTypeFactory.fromTag("traditional"));
         assertEquals(CacheType.MULTI, cacheTypeFactory.fromTag("Multi-cache"));
        assertEquals(CacheType.MULTI, cacheTypeFactory.fromTag("multi"));
        assertEquals(CacheType.UNKNOWN, cacheTypeFactory.fromTag("unknown"));
     }
 
     @Test
     public void testStars() {
         CacheTypeFactory cacheTypeFactory = new CacheTypeFactory();
         assertEquals(0, cacheTypeFactory.stars("0"));
         assertEquals(1, cacheTypeFactory.stars("0.5"));
         assertEquals(2, cacheTypeFactory.stars("1"));
         assertEquals(0, cacheTypeFactory.stars("foo"));
     }
     
 }
