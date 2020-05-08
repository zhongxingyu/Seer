 /**
  * Razvan's code. Copyright 2008 based on Apache (share alike) see LICENSE.txt for details.
  */
 package com.razie.pub.lightsoa.test;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.URL;
 
 import com.razie.pub.assets.AssetActionToInvoke;
 import com.razie.pub.assets.AssetHandle;
 import com.razie.pub.assets.AssetMgr;
 import com.razie.pub.base.ActionItem;
 import com.razie.pub.base.log.Log;
 import com.razie.pub.comms.ActionToInvoke;
 import com.razie.pub.http.test.TestLightBase;
 import com.razie.pub.lightsoa.HttpAssetSoaBinding;
 
 /**
  * setup a light server with soa assets management and test a few calls to a sample asset
  * 
  * @author razvanc99
  */
 public class TestLightServerSoaAssets extends TestLightBase {
     static String PK = TestLocalSoaAssets.PLAYERKEY.toUrlEncodedString();
 
     public void setUp() {
         super.setUp();
 
         // initialize the main asset manager - is responsible for instantiating assets by key
         AssetMgr.init(new SampleAssetMgr());
 
         // register the test asset
        HttpAssetSoaBinding.register(SampleAsset.class);
 
         // register the asset management service
        HttpAssetSoaBinding soa = new HttpAssetSoaBinding();
         cmdGET.registerSoa(soa);
     }
 
     /**
      * test the SOA simple echo via the proper URL reader (check implementation as a proper http
      * server)
      */
     public void testSoaEchoUrl() throws IOException, InterruptedException {
         // send echo command
         URL url = new URL("http://localhost:" + PORT + "/lightsoa/asset/" + PK + "/play?movie="
                 + TestLocalSoaAssets.MOVIEKEY.toUrlEncodedString());
         BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
         String result = in.readLine();
         in.close();
 
         assertTrue(result.contains(TestLocalSoaAssets.MOVIEKEY.getId()));
     }
 
     /**
      */
     public void testSoaEchoAction() throws IOException, InterruptedException {
         // send echo command
         ActionToInvoke action = new AssetActionToInvoke("http://localhost:" + PORT,
                 TestLocalSoaAssets.PLAYERKEY, new ActionItem("play"), "movie", TestLocalSoaAssets.MOVIEKEY);
         String result = (String) action.act(null);
 
         assertTrue(result.contains(TestLocalSoaAssets.MOVIEKEY.getId()));
     }
 
     /**
      */
     public void testSoaEchoHandle() throws IOException, InterruptedException {
         // send echo command
         AssetHandle handle = new AssetHandle(TestLocalSoaAssets.PLAYERKEY);
         String result = (String) handle.invoke("play", "movie", TestLocalSoaAssets.MOVIEKEY.toString());
 
         assertTrue(result.contains(TestLocalSoaAssets.MOVIEKEY.getId()));
     }
 
     static final Log logger = Log.Factory.create(TestLightServerSoaAssets.class.getName());
 }
