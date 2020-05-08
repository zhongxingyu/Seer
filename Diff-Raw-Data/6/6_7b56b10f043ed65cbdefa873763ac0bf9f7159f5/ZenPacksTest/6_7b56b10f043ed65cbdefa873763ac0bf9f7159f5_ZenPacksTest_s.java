 /*****************************************************************************
  * 
  * Copyright (C) Zenoss, Inc. 2011, all rights reserved.
  * 
  * This content is made available according to terms specified in
  * License.zenoss under the directory where your Zenoss product is installed.
  * 
  ****************************************************************************/
 
 
 package org.zenoss.utils;
 
import com.google.common.io.Resources;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import static org.junit.Assert.*;
 
 /**
  * Unit test for {@link ZenPacks}
  */
 public class ZenPacksTest {
 
    private static String fakeZenHome = Resources.getResource(".").getPath();
    private static String ZENPACK = "ZenPacks.zenoss.TestZenPack";
 
     @Before
     public void setUp() {
         System.setProperty("ZENHOME", fakeZenHome);
     }
 
     @After
     public void tearDown() {
         System.getProperties().remove("ZENHOME");
     }
 
     @Test
     public void testGetZenPack() throws Exception {
         ZenPack zp = ZenPacks.getZenPack(ZENPACK);
         assertEquals(ZENPACK, zp.getName());
         assertEquals("1.2.3", zp.getVersion());
 
         zp = ZenPacks.getZenPack(ZENPACK + "2");
         assertEquals(ZENPACK + "2", zp.getName());
         assertEquals("3.2.1", zp.getVersion());
     }
 
     @Test
     public void testGetPackPath() throws Exception {
         ZenPack zp = ZenPacks.getZenPack(ZENPACK);
         String path = zp.packPath("a", "b");
         assertEquals(fakeZenHome + "ZenPacks/ZenPacks.zenoss.TestZenPack/ZenPacks/zenoss/TestZenPack/a/b", path);
     }
 
     @Test
     public void testGetAllPacks() throws Exception {
         List<ZenPack> packs = ZenPacks.getAllZenPacks();
         List<String> names = new ArrayList<String>(packs.size());
         for (ZenPack pack : packs) {
             names.add(pack.getName());
         }
         assertTrue(names.contains("ZenPacks.zenoss.TestZenPack"));
         assertTrue(names.contains("ZenPacks.zenoss.TestZenPack2"));
         assertTrue(names.size() == 2);
     }
 
 }
