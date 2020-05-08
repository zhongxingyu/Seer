 package com.github.seqware.queryengine.model.test;
 
 import java.io.File;
 import java.io.IOException;
 import java.math.BigInteger;
 import java.security.SecureRandom;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import java.util.UUID;
 
 import org.apache.hadoop.conf.Configuration;
 import org.apache.hadoop.hbase.HBaseConfiguration;
 import org.apache.hadoop.hbase.KeyValue;
 import org.apache.hadoop.hbase.MasterNotRunningException;
 import org.apache.hadoop.hbase.ZooKeeperConnectionException;
 import org.apache.hadoop.hbase.client.Get;
 import org.apache.hadoop.hbase.client.HBaseAdmin;
 import org.apache.hadoop.hbase.client.HTable;
 import org.apache.hadoop.hbase.client.HTableInterface;
 import org.apache.hadoop.hbase.client.Result;
 import org.apache.hadoop.hbase.client.Row;
 import org.apache.hadoop.hbase.util.Bytes;
 import org.junit.After;
 import org.junit.AfterClass;
 import org.junit.Assert;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 import com.github.seqware.queryengine.backInterfaces.StorageInterface;
 import com.github.seqware.queryengine.factory.CreateUpdateManager;
 import com.github.seqware.queryengine.factory.SWQEFactory;
 import com.github.seqware.queryengine.model.Atom;
 import com.github.seqware.queryengine.model.Feature;
 import com.github.seqware.queryengine.model.FeatureSet;
 import com.github.seqware.queryengine.plugins.PluginInterface;
 import com.github.seqware.queryengine.plugins.contribs.OverlappingMutationsAggregationPlugin;
 import com.github.seqware.queryengine.plugins.plugins.FeatureSetCountPlugin;
 import com.github.seqware.queryengine.plugins.plugins.FeaturesByAttributesPlugin;
 import com.github.seqware.queryengine.system.importers.FeatureImporter;
 import com.github.seqware.queryengine.util.FSGID;
 import com.github.seqware.queryengine.util.SGID;
 import com.github.seqware.queryengine.util.SeqWareIterable;
 import com.github.seqware.queryengine.model.QueryFuture;
 import com.github.seqware.queryengine.model.impl.AtomImpl;
 import com.github.seqware.queryengine.model.impl.FeatureList;
 import com.github.seqware.queryengine.impl.MRHBaseModelManager;
 import com.github.seqware.queryengine.impl.SimplePersistentBackEnd;
 import com.github.seqware.queryengine.impl.protobufIO.FeatureListIO;
 import com.github.seqware.queryengine.kernel.RPNStack;
 import com.github.seqware.queryengine.kernel.RPNStack.Constant;
 import com.github.seqware.queryengine.kernel.RPNStack.FeatureAttribute;
 import com.github.seqware.queryengine.kernel.RPNStack.Operation;
 
 public class TableSetupTest {
 	static FeatureSet aSet, bSet, cSet;
 	static Feature a1,a2,a3;
 	static File testVCFFile = null;
 	static File testSecondVCFFile = null;
 	static String refName = null;
 	static String refName2 = null;
     
 //	public static void main(String[] args){
 //		try {
 //			setUpTest();
 //			testVCFImport();
 //			featureRetrieval();
 //		} catch (IOException e) {
 //			// TODO Auto-generated catch block
 //			e.printStackTrace();
 //		}
 //	}
 	
 //	@BeforeClass
 	//this will reset all the tables and load the vcf file paths for testiing
 	public static void setUpTest() throws IOException{
 		Configuration config = HBaseConfiguration.create();
 		try {
 			HBaseAdmin hba = new HBaseAdmin(config);
 			hba.disableTables("b.*");
 			hba.deleteTables("b.*");
 		} catch (MasterNotRunningException e) {
 			e.printStackTrace();
 		} catch (ZooKeeperConnectionException e) {
 			e.printStackTrace();
 		}
 		
 		String curDir = System.getProperty("user.dir");
         SecureRandom random = new SecureRandom();
         refName = "Random_ref_" + new BigInteger(20, random).toString(32);
 //        refName2 = "Random_ref_" + new BigInteger(20, random).toString(32);
         testVCFFile = new File(curDir + "/src/test/resources/com/github/seqware/queryengine/system/FeatureImporter/smallTest.vcf");
 //        testSecondVCFFile = new File(curDir + "/src/test/resources/com/github/seqware/queryengine/system/FeatureImporter/consequences_annotated.vcf");
 
 	}
 	
 //	@Test
 	public void setupTest(){
 		SWQEFactory.getStorage().clearStorage();
 		CreateUpdateManager manager = SWQEFactory.getModelManager();
 		aSet = manager.buildFeatureSet().setReference(manager.buildReference().setName("DummyReference").build()).build();
 		a1 = manager.buildFeature().setSeqid("chr1").setStart(100).setType("type1").setStop(101).setScore(100.0).setStrand(Feature.Strand.NEGATIVE).setSource("human").setPhase(".").build();
 		a2 = manager.buildFeature().setSeqid("chr1").setStart(101).setType("type2").setStop(102).setScore(100.0).setStrand(Feature.Strand.NEGATIVE).setSource("human").setPhase(".").build();
 		aSet.add(a1);
 		aSet.add(a2);
 		manager.flush();
 	}
 	
 //	@Test
 	//Test some implemented plugin that is working
     public void testInstallAndRunArbitraryPlugin() {
         Class<? extends PluginInterface> arbitraryPlugin;
         // only use the M/R plugin for this test if using MR
         if (SWQEFactory.getModelManager() instanceof MRHBaseModelManager) {
             // pretend that the included com.github.seqware.queryengine.plugins.hbasemr.MRFeaturesByAttributesPlugin is an external plug-in
             arbitraryPlugin = FeaturesByAttributesPlugin.class;
         } else {
             // pretend the equivalent for a non-HBase back-end
             arbitraryPlugin = FeaturesByAttributesPlugin.class;
         }
         // get a FeatureSet from the back-end
         QueryFuture<FeatureSet> future = SWQEFactory.getQueryInterface().getFeaturesByPlugin(0, arbitraryPlugin, null, aSet, new RPNStack(
                 new Constant("1"), new FeatureAttribute("seqid"), Operation.EQUAL));
         // check that Features are present match
         FeatureSet result = future.get();
         System.out.println("This is the plugin result for FeatureSet " + result.getReference().getDisplayName() + " : ");
         System.out.println("This is the length of the feature set " + result.getReference().getDisplayName() + " : " + result.getCount());
         for (Feature f : result) {
 			System.out.println(f.getDisplayName() + 
 					", Seqid: " + f.getSeqid() + 
 					", Source: " + f.getSource() + 
 					", Start: " + f.getStart() + 
 					", Stop: " + f.getStop() + 
 					", Strand: " + f.getStrand());
         }
 //        Assert.assertTrue("Query results wrong, expected 1 and found " + count, count == 1);
     }
 
 //	@Test
 	//This imports the features from a vcf file into HBase
 	public void testVCFImport(){
 		SGID main;
 		FeatureSet fSet;
 		CreateUpdateManager manager;
 		Iterator<Feature> fIter;
 		
         main = FeatureImporter.naiveRun(new String[]{"VCFVariantImportWorker", "1", "false", refName, testVCFFile.getAbsolutePath()});        
         fSet = SWQEFactory.getQueryInterface().getLatestAtomBySGID(main, FeatureSet.class);
         
         manager = SWQEFactory.getModelManager();
         fIter = fSet.getFeatures();
         aSet = manager.buildFeatureSet().setReference(fSet.getReference()).build();
         while(fIter.hasNext()){
         	Feature f = fIter.next();
         	aSet.add(f);
         	System.out.println("Stop: " + f.getStop());
         }
 		
 //        main= FeatureImporter.naiveRun(new String[]{"VCFVariantImportWorker", "1", "false", refName2, testSecondVCFFile.getAbsolutePath()});        
 //        fSet = SWQEFactory.getQueryInterface().getLatestAtomBySGID(main, FeatureSet.class);
 //        
 //        manager = SWQEFactory.getModelManager();
 //        fIter = fSet.getFeatures();
 //        
 //        bSet = manager.buildFeatureSet().setReference(fSet.getReference()).build();
 //        while(fIter.hasNext()){
 //        	bSet.add(fIter.next());
 //        }
         manager.flush();
 	}
 	
 //	@Test
 	public void complexQueryTest(){
 		SimplePersistentBackEnd backend = new SimplePersistentBackEnd(SWQEFactory.getStorage());
 		CreateUpdateManager manager = SWQEFactory.getModelManager();
 		QueryFuture<FeatureSet> queryFuture = backend.getFeaturesByAttributes(1, aSet, new RPNStack(
 				new Constant("chr1"),
                 Operation.EQUAL));
 		System.out.println(queryFuture.get().getCount());
 		cSet = manager.buildFeatureSet().setReference(queryFuture.get().getReference()).build();
 		System.out.println("Plugin has run.");
 		manager.close();
 	}
 	
 //	@AfterClass
 //	@Test
 	//	loop through hbase table to retrieve features in feature sets
 	public void featureRetrieval(){		
 		Atom a,b,c,d,e;
 		for (FeatureSet fSet : SWQEFactory.getQueryInterface().getFeatureSets()){
 //			System.out.println(fSet.getReference().getDisplayName());
 //			a = SWQEFactory.getQueryInterface().;
 //			b = SWQEFactory.getQueryInterface().getLatestAtomByRowKey("hg_19.1:000000000000013", Feature.class);
 //			c = SWQEFactory.getQueryInterface().getLatestAtomByRowKey("hg_19.1:000000000000014", Feature.class);
 //			d = SWQEFactory.getQueryInterface().getLatestAtomByRowKey("hg_19.1:000000000000015", Feature.class);
 //			System.out.println(a.getDisplayName());
 //			System.out.println(b.getDisplayName());
 //			System.out.println(c.getDisplayName());
 //			System.out.println(d.getDisplayName());
 			
 			for (Feature f : fSet){
 //				System.out.println(f.getDisplayName() + 
 //						", Seqid: " + f.getSeqid() + 
 //						", Source: " + f.getSource() + 
 //						", Start: " + f.getStart() + 
 //						", Stop: " + f.getStop() + 
 //						", Strand: " + f.getStrand());
 			}
 		}
 	}
 	
 	@Test
 	public void lowLevelRetrieval(){
 		try {
 			Configuration config = HBaseConfiguration.create();
 			HTableInterface hg19Table = new HTable(config, "batman.hbaseTestTable_v2.Feature.hg_19");
 			
 			List<Get> getList = new ArrayList<Get>();
 			getList.add(new Get(Bytes.toBytes("hg_19.1:000000000000012")));
 			getList.add(new Get(Bytes.toBytes("hg_19.1:000000000000013")));
 			getList.add(new Get(Bytes.toBytes("hg_19.1:000000000000014")));
 			getList.add(new Get(Bytes.toBytes("hg_19.1:000000000000015")));
 
 			for (Get g : getList){
 				System.out.println(hg19Table.exists(g));
 				Result r = hg19Table.get(g);
 //				System.out.println(r.getColumnLatest(Bytes.toBytes("d"), Bytes.toBytes("2682ee4b-5d7b-4ad8-b632-a897b5043715")));
 				FeatureListIO fLio = new FeatureListIO();
 				KeyValue columnLatest = r.getColumnLatest(Bytes.toBytes("d"), Bytes.toBytes("2682ee4b-5d7b-4ad8-b632-a897b5043715"));
 				byte[] value = columnLatest.getValue();
				FeatureList fL = fLio.byteArr2m(value);
 				System.out.println(fL);
 //				for (Feature f : fL.getFeatures()){
 //					System.out.println("Row: " + g.getId());
 //					System.out.println(f.getStart() + " " + f.getStop());
 //				}
 			}
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 //  @Test
     //Write a Test the OverlapMutationsAggregationPlugin plugin
 	public void testOLapPlugin(){
 		Class<? extends PluginInterface> arbPlugin;
 		arbPlugin = OverlappingMutationsAggregationPlugin.class;
 		SWQEFactory.getQueryInterface().getFeaturesByPlugin(0, arbPlugin, null, null);
 	}
 	
 }
