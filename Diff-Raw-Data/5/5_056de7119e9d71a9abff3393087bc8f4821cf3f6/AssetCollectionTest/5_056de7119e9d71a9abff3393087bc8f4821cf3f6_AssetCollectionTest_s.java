 package com.modernnerds.javelin.test;
 
 import com.modernnerds.javelin.JavelinException;
 import com.modernnerds.javelin.AssetCollection;
 import com.modernnerds.javelin.asset.Asset;
 import com.modernnerds.javelin.asset.FileAsset;
 import com.modernnerds.javelin.asset.GlobalAsset;
 import com.modernnerds.javelin.filter.Filter;
 import com.modernnerds.javelin.filter.GoogleClosureFilter;
 import com.modernnerds.javelin.filter.YuiCompressorFilter;
 
 import java.util.List;
 import java.util.ArrayList;
 import java.io.FileNotFoundException;
 
 import junit.framework.TestCase;
 
 public class AssetCollectionTest extends TestCase {
   
   public void testConstructor() {
     List<Asset> assets = new ArrayList<Asset>();
     assets.add(new FileAsset("src/test/resources/fixture1.test"));
 
    AssetCollection assetCollection = new AssetCollection();
     AssetCollection assetCollection2 = new AssetCollection(assets);
 
     assertEquals(1, assetCollection2.getSize());
   }
 
   public void testAdd() {
     AssetCollection assetCollection = new AssetCollection();
     boolean added = assetCollection.add(new FileAsset("src/test/resources/fixture1.test"));
 
     assertTrue(added);
   }
 
   public void testAddAssets() {
     AssetCollection assetCollection = new AssetCollection();
     List<Asset> assets = new ArrayList<Asset>();
 
     assetCollection.addAssets(assets);
     assertEquals(0, assetCollection.getSize());
 
     assets.add(new FileAsset("src/test/resources/fixture1.test"));
 
     assertEquals(1, assetCollection.getSize());
   }
 
   public void testDumpForLocalFiles() throws JavelinException, FileNotFoundException {
     List<Asset> assets = new ArrayList<Asset>();
     assets.add(new FileAsset("src/test/resources/fixture1.test"));
     assets.add(new FileAsset("src/test/resources/fixture2.test"));
 
     AssetCollection assetCollection = new AssetCollection(assets);
 
     assertEquals(mergedFilesMock(), assetCollection.dump());
 
     assets.clear();
     assets.add(new GlobalAsset("src/test/resources/"));
 
     assetCollection = new AssetCollection(assets);
 
     String expected = jsFileMock() + mergedGlobalFilesMock();
 
     assertEquals(expected, assetCollection.dump());
 
     assets.clear();
     assets.add(new FileAsset("src/test/resources/fixture1.js"));
 
     List<Filter> filters = new ArrayList<Filter>();
     filters.add(new YuiCompressorFilter("src/test/resources/lib/yuicompressor-2.4.6.jar"));
 
     assetCollection = new AssetCollection(assets, filters);
 
     assertEquals(compressedJsFileMock(), assetCollection.dump());
 
     assets.clear();
     filters.clear();
 
     assets.add(new FileAsset("src/test/resources/fixture1.js"));
     filters.add(new GoogleClosureFilter("src/test/resources/lib/googleclosure.jar"));
 
     assetCollection = new AssetCollection(assets, filters);
 
     assertEquals(compressedJsFileMock(), assetCollection.dump());
   }
 
   public void testDumpExpectingException() {
     List<Asset> assets = new ArrayList<Asset>();
     assets.add(new FileAsset("src/test/resources/fixture_does_not_exist"));
 
     AssetCollection assetCollection = new AssetCollection(assets);
 
     try {
      String dump = assetCollection.dump();
 
       fail("FileNotFoundException was not thrown");
     } catch(Exception e) { }
   }
 
   private String mergedFilesMock() {
     return "test1\ntest2\ntest3\ntest4\n";
   }
 
   private String mergedGlobalFilesMock() {
     return "test1\ntest2\ntest3\ntest4\ntest5\ntest6\ntest7\ntest8\n";
   }
 
   private String jsFileMock() {
     return "function foo() {\n  return 'bar';\n}\n\nfunction bar() {\n  return 'foo';\n}\n";
   }
 
   private String compressedJsFileMock() {
     return "function foo(){return\"bar\"}function bar(){return\"foo\"};";
   }
 }
