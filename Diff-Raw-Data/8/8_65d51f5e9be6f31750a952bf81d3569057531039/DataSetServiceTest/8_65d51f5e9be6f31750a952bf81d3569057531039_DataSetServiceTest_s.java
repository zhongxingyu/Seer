 package pals.service;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.testng.Assert;
 import org.testng.annotations.Test;
 
 import pals.BaseTest;
 import pals.Globals;
 import pals.dao.DAO;
 import pals.entity.Analysable;
 import pals.entity.DataSet;
 import pals.entity.DataSetVersion;
 import pals.entity.Experiment;
 import pals.entity.Model;
 import pals.entity.ModelOutput;
 import pals.entity.TestEntityFactory;
 
 public class DataSetServiceTest extends BaseTest{
 	
 	DataSetVersion entity;
 	
     @Autowired
     DAO dao;
     
     @Autowired
     DataSetService dataSetService;
     
     @Autowired
     ModelOutputService modelOutputService;
     
     @Autowired 
     ExperimentService experimentService;
 	
     public void setUp() throws IOException
     {    	
     	entity = TestEntityFactory.dataSetVersion();
     	dao.persist(entity.getDataSet().getCountry());
     	dao.persist(entity.getDataSet().getVegType());
     	dao.persist(entity.getDataSet().getOwner());
     	dao.persist(entity.getDataSet());
     	entity.getDataSet().setLatestVersion(null);
     	entity.setDataSetId(entity.getDataSet().getId());
     	entity.setOwner(entity.getDataSet().getOwner());
     	entity.setIsPublic(false);
     	dao.persist(entity);
     }
     
 	public void tearDown() throws IOException
 	{
 		dao.deleteAll("Analysis");
 		dao.deleteAll("DataSetVersion");
 		dao.deleteAll("DataSet");
 		dao.deleteAll("Country");
 		dao.deleteAll("VegetationType");
 		dao.deleteAll("User");
 		dao.deleteAll("AnalysisType");
 	}
 	
 	public void tearDown(Experiment experiment) throws IOException
 	{
 		dao.deleteAll("Analysis");
 		dao.deleteAll("DataSetVersion");
 		experimentService.removeCurrentExperimentFromExperimentables(experiment);
 		experimentService.remove(experiment);
 		dao.deleteAll("DataSet");
 		dao.deleteAll("Country");
 		dao.deleteAll("VegetationType");
 		dao.deleteAll("User");
 		dao.deleteAll("AnalysisType");
 	}
 	
 	@Test
 	public void testSetLatestVersion() throws IOException
 	{
 		setUp();
 		dataSetService.setLatestVersion(entity.getId(), entity.getDataSet().getId());
 		DataSet result = dataSetService.get(entity.getDataSet().getId());
 		Assert.assertNotNull(result.getLatestVersion());
 		Assert.assertEquals(result.getLatestVersion().getId(),entity.getId());
 		Assert.assertTrue(result.getLatestVersion().getIsPublic());
 		result.setLatestVersion(null);
 		dataSetService.update(result);
 		tearDown();
 	}
 	
 	@Test
 	public void testExistsVersionNameExists() throws IOException
 	{
         setUp();
         Assert.assertTrue(dataSetService.existsVersionName(entity.getName(), entity.getDataSet()));
         tearDown();
 	}
 	
 	@Test
 	public void testExistsVersionNameDoesntExist() throws IOException
 	{
         setUp();
         Assert.assertFalse(dataSetService.existsVersionName("MADEUPNAMETHATDOESNTEXIST", entity.getDataSet()));
         tearDown();
 	}
 	
 	@Test
 	public void testGetMyDataSets() throws IOException
 	{
 		setUp();
 		List<DataSet> myDataSets = dataSetService.getMyDataSets(entity.getDataSet().getOwner());
 		Assert.assertEquals(myDataSets.size(),1);
 		Assert.assertEquals(myDataSets.get(0).getId(),entity.getDataSet().getId());
 		tearDown();
 	}
 	
 	@Test
 	public void testGetMyDataSetsExistingExperiment() throws IOException
 	{
 		setUp();
 		Experiment experiment = TestEntityFactory.experiment();
 		experiment.setOwner(entity.getDataSet().getOwner());
 		dao.persist(experiment);
 		entity.getDataSet().setExperiment(experiment);
 		dao.update(entity.getDataSet());
 		
 		entity.getDataSet().getOwner().setCurrentExperiment(experiment);
 		List<DataSet> myDataSets = dataSetService.getMyDataSets(entity.getDataSet().getOwner());
 		Assert.assertEquals(myDataSets.size(),1);
 		Assert.assertEquals(myDataSets.get(0).getId(),entity.getDataSet().getId());
 		
 		tearDown(experiment);
 	}
 	
 	@Test
 	public void testGetMyDataSetsExistingExperimentWrongExperiment() throws IOException
 	{
 		setUp();
 		Experiment experiment = TestEntityFactory.experiment();
 		experiment.setOwner(entity.getDataSet().getOwner());
 		dao.persist(experiment);
 		entity.getDataSet().setExperiment(experiment);
 		dao.update(entity.getDataSet());
 		
 		Experiment experimentWrongId = new Experiment();
 		experimentWrongId.setId(-111);
 		entity.getDataSet().getOwner().setCurrentExperiment(experimentWrongId);
 		List<DataSet> myDataSets = dataSetService.getMyDataSets(entity.getDataSet().getOwner());
 		Assert.assertEquals(myDataSets.size(),0);
 		
 		tearDown(experiment);
 	}
 	
 	public void testGetMyDataSetsNotNullExperimentNotReturned() throws IOException
 	{
 		setUp();
 		Experiment experiment = TestEntityFactory.experiment();
 		experiment.setOwner(entity.getDataSet().getOwner());
 		dao.persist(experiment);
 		entity.getDataSet().setExperiment(null);
 		dao.update(entity.getDataSet());
 		
 		entity.getDataSet().getOwner().setCurrentExperiment(experiment);
 		List<DataSet> myDataSets = dataSetService.getMyDataSets(entity.getDataSet().getOwner());
 		Assert.assertEquals(myDataSets.size(),0);
 		
 		tearDown(experiment);
 	}
 	
 	@Test
 	public void testGetDataSetVersionsExistingExperiment() throws IOException
 	{
 		setUp();
 		Experiment experiment = TestEntityFactory.experiment();
 		experiment.setOwner(entity.getDataSet().getOwner());
 		dao.persist(experiment);
 		entity.getDataSet().setExperiment(experiment);
 		entity.getDataSet().setLatestVersion(entity);
 		dao.update(entity.getDataSet());
 		
 		entity.getDataSet().getOwner().setCurrentExperiment(experiment);
 		List<DataSetVersion> myDSV = dataSetService.getDataSetVersions(entity.getDataSet().getOwner());
 		Assert.assertEquals(myDSV.size(),1);
 		Assert.assertEquals(myDSV.get(0).getId(),entity.getId());
 		
 		entity.getDataSet().setLatestVersion(null);
 		dao.update(entity.getDataSet());
 		tearDown(experiment);
 	}
 	
 	@Test
 	public void testGetDataSetVersionsWrongExperiment() throws IOException
 	{
 		setUp();
 		Experiment experiment = TestEntityFactory.experiment();
 		experiment.setOwner(entity.getDataSet().getOwner());
 		dao.persist(experiment);
 		entity.getDataSet().setExperiment(experiment);
 		entity.getDataSet().setLatestVersion(entity);
 		dao.update(entity.getDataSet());
 		
 		Experiment wrongExperiment = new Experiment();
 		wrongExperiment.setId(-111);
 		entity.getDataSet().getOwner().setCurrentExperiment(wrongExperiment);
 		List<DataSetVersion> myDSV = dataSetService.getDataSetVersions(entity.getDataSet().getOwner());
 		Assert.assertEquals(myDSV.size(),0);
 		
 		entity.getDataSet().setLatestVersion(null);
 		dao.update(entity.getDataSet());
 		tearDown(experiment);
 	}
 	
 	@Test
 	public void testGetDataSetVersionsNoNullExperiment() throws IOException
 	{
 		setUp();
 		Experiment experiment = TestEntityFactory.experiment();
 		experiment.setOwner(entity.getDataSet().getOwner());
 		dao.persist(experiment);
 		entity.getDataSet().setExperiment(experiment);
 		entity.getDataSet().setLatestVersion(entity);
 		dao.update(entity.getDataSet());
 		
 		entity.getDataSet().getOwner().setCurrentExperiment(null);
 		List<DataSetVersion> myDSV = dataSetService.getDataSetVersions(entity.getDataSet().getOwner());
 		Assert.assertEquals(myDSV.size(),0);
 		
 		entity.getDataSet().setLatestVersion(null);
 		dao.update(entity.getDataSet());
 		tearDown(experiment);
 	}
 	
 	@Test
 	public void testGetDataSetVersionsExistingExperimentSharedWithAll() throws IOException
 	{
 		setUp();
 		Experiment experiment = TestEntityFactory.experiment();
 		experiment.setOwner(entity.getDataSet().getOwner());
 		experiment.setShareWithAll(true);
 		dao.persist(experiment);
 		entity.getDataSet().setExperiment(experiment);
 		entity.getDataSet().setLatestVersion(entity);
 		dao.update(entity.getDataSet());
 		
 		entity.getDataSet().getOwner().setCurrentExperiment(null);
 		List<DataSetVersion> myDSV = dataSetService.getDataSetVersions(entity.getDataSet().getOwner());
 		Assert.assertEquals(myDSV.size(),1);
 		Assert.assertEquals(myDSV.get(0).getId(),entity.getId());
 		
 		entity.getDataSet().setLatestVersion(null);
 		dao.update(entity.getDataSet());
 		tearDown(experiment);
 	}
 	
 	@Test
 	public void testGetDataSetVersionsNoExperiment() throws IOException
 	{
 		setUp();
 		entity.getDataSet().setExperiment(null);
 		entity.getDataSet().setLatestVersion(entity);
 		dao.update(entity.getDataSet());
 		
 		entity.getDataSet().getOwner().setCurrentExperiment(null);
 		List<DataSetVersion> myDSV = dataSetService.getDataSetVersions(entity.getDataSet().getOwner());
 		Assert.assertEquals(myDSV.size(),1);
 		Assert.assertEquals(myDSV.get(0).getId(),entity.getId());
 		
 		entity.getDataSet().setLatestVersion(null);
 		dao.update(entity.getDataSet());
 		tearDown();
 	}
 	
 	@Test
 	public void testDelete() throws IOException, InvalidInputException
 	{
 		setUp();
 		entity.getDataSet().setLatestVersion(entity);
 		dao.update(entity.getDataSet());
 		
     	Model model = TestEntityFactory.model();
     	dao.persist(model);
     	
     	File uploadedFile = new File("testdata/CABLE_Tumbarumba.nc");
 		
 		ModelOutput modelOutput = 
 			modelOutputService.newModelOutput(
 			    entity.getOwner(), uploadedFile, "Model Output Name",
 			    model.getId(), entity, "SS", "PS", "UC");
 		Assert.assertNotNull(modelOutput.getId());
 		
 		dataSetService.delete(entity.getDataSet());
 		
 	    testEmptyTable("DataSet");
 	    testEmptyTable("DataSetVersion");
 	    testEmptyTable("ModelOutput");
 	    
 	    dao.deleteAll("Model");
 		
 		tearDown();
 	}
 	
 	public void testEmptyTable(String className)
 	{
 		@SuppressWarnings("unchecked")
 		List<Object> list = dao.getAll(className);
 		Assert.assertEquals(list.size(), 0);
 	}
 	
 	@Test
 	public void testCopy() throws IOException
 	{
 		setUp();
 		Experiment experiment = TestEntityFactory.experiment();
 		experiment.setOwner(entity.getDataSet().getOwner());
 		dao.persist(experiment);
 		DataSet ds = entity.getDataSet();
 		ds.setLatestVersion(entity);
 		DataSet copy = dataSetService.copy(ds,experiment);
 		Assert.assertEquals(copy.getComments(),ds.getComments());
 		Assert.assertEquals(copy.getDataSetType(),ds.getDataSetType());
 		Assert.assertEquals(copy.getFullUrl(),ds.getFullUrl());
 		Assert.assertEquals(copy.getMeasurementAggregation(),ds.getMeasurementAggregation());
 		Assert.assertEquals(copy.getMetadata(),ds.getMetadata());
 		Assert.assertEquals(copy.getName(),ds.getName());
 		Assert.assertEquals(copy.getRefs(),ds.getRefs());
 		Assert.assertEquals(copy.getStatus(),Analysable.STATUS_NEW);
 		Assert.assertEquals(copy.getUrl(),ds.getUrl());
 		Assert.assertEquals(copy.getUserName(),ds.getUserName());
 		Assert.assertEquals(copy.getCountry().getId(),ds.getCountry().getId());
 		Assert.assertEquals(copy.getDownloadCount(),new Integer(0));
 		Assert.assertEquals(copy.getElevation(),ds.getElevation());
 		Assert.assertNotSame(copy.getId(),ds.getId());
 		Assert.assertNotNull(copy.getLatestVersion().getId());
 		Assert.assertEquals(copy.getLatitude(),ds.getLatitude());
 		Assert.assertEquals(copy.getLongitude(),ds.getLongitude());
 		Assert.assertEquals(copy.getMaxVegHeight(),ds.getMaxVegHeight());
 		Assert.assertEquals(copy.getOwner().getUsername(),ds.getOwner().getUsername());
 		Assert.assertEquals(copy.getTimeZoneOffsetHours(),ds.getTimeZoneOffsetHours());
 		Assert.assertEquals(copy.getTowerHeight(),ds.getTowerHeight());
 		Assert.assertEquals(copy.getVegType().getVegetationType(),ds.getVegType().getVegetationType());
 		Assert.assertEquals(copy.getCopiedFrom().getId(),ds.getId());
 		Assert.assertEquals(ds.getCopiedTo().get(0).getId(),experiment.getId());
 		
 		ds.setLatestVersion(null);
 		ds.setCopiedTo(null);
 		dao.update(ds);
 		copy.setLatestVersion(null);
 		dao.update(copy);
 		tearDown(experiment);
 	}
 	
 	@Test
 	public void copyDataSetVersions() throws IOException
 	{
 		setUp();
 		Experiment experiment = TestEntityFactory.experiment();
 		experiment.setOwner(entity.getDataSet().getOwner());
 		dao.persist(experiment);
 		DataSet ds = entity.getDataSet();
 		ds.setLatestVersion(entity);
 		DataSet copy = dataSetService.copy(ds,experiment);
 		
 		dataSetService.copyDataSetVersions(ds, copy);
 		
 		List<DataSetVersion> dsvList = dao.getAll("DataSetVersion");
 		boolean have = false;
 		for( DataSetVersion dsv : dsvList )
 		{
 			if( dsv.getDataSetId().equals(copy.getId()) )
 			{
 				have = true;
 			}
 		}
 		Assert.assertTrue(have);
 		
 		ds.setLatestVersion(null);
 		ds.setCopiedTo(null);
 		dao.update(ds);
 		copy.setLatestVersion(null);
 		dao.update(copy);
 		tearDown(experiment);		
 	}
 	
 	@Test
 	public void testCopyLatestVersion() throws IOException
 	{
 		setUp();
 		Experiment experiment = TestEntityFactory.experiment();
 		experiment.setOwner(entity.getDataSet().getOwner());
 		dao.persist(experiment);
 		DataSet ds = entity.getDataSet();
 		
     	DataSetVersion latestVersion = TestEntityFactory.dataSetVersion();
     	String distinctiveName = "DISTINCTIVE_NAME";
     	latestVersion.setName(distinctiveName);
     	latestVersion.setDataSet(ds);
     	latestVersion.setDataSetId(entity.getDataSet().getId());
     	latestVersion.setOwner(entity.getDataSet().getOwner());
     	latestVersion.setIsPublic(false);
     	dao.persist(latestVersion);
 		ds.setLatestVersion(latestVersion);
 		
 		DataSet copy = dataSetService.copy(ds,experiment);
 		Assert.assertNotSame(copy.getLatestVersion().getId(),ds.getLatestVersion().getId());
 		Assert.assertEquals(copy.getLatestVersion().getName(),distinctiveName);
 		
 		ds.setLatestVersion(null);
 		ds.setCopiedTo(null);
 		dao.update(ds);
 		copy.setLatestVersion(null);
 		dao.update(copy);
 		tearDown(experiment);
 	}
 	
 	@Test
 	public void testSetExperiment() throws IOException
 	{
 		setUp();
 		Experiment experiment = TestEntityFactory.experiment();
 		experiment.setOwner(entity.getDataSet().getOwner());
 		dao.persist(experiment);
 		
 		dataSetService.setExperiment(entity.getDataSet(), experiment);
 		
 		Assert.assertEquals(entity.getDataSet().getExperiment().getId(),experiment.getId());
 		List<DataSetVersion> dsvList = dataSetService.getDataSetVersions(entity.getDataSet().getId(),false);
 		for( DataSetVersion dsv : dsvList )
 		{
 			Assert.assertEquals(dsv.getExperiment().getId(),experiment.getId());
 			Assert.assertEquals(dsv.getDataSet().getExperiment().getId(),experiment.getId());
 		}
 		
 		tearDown(experiment);
 	}
 	
 	@Test
 	public void testGetMyDataSetsPagination() throws IOException
 	{
 		DataSetVersion dsv = null;
 		DataSetVersion lastDSV = null;
 		
 		for( int i=0; i < 10; ++i )
 		{
 	    	dsv = TestEntityFactory.dataSetVersion();
 	    	if( i == 0 )
 	    	{
 		    	dao.persist(dsv.getDataSet().getCountry());
 		    	dao.persist(dsv.getDataSet().getVegType());
 		    	dao.persist(dsv.getDataSet().getOwner());
 	    	}
 	    	else
 	    	{
 	    		dsv.getDataSet().setCountry(lastDSV.getDataSet().getCountry());
 	    		dsv.getDataSet().setVegType(lastDSV.getDataSet().getVegType());
 	    		dsv.getDataSet().setOwner(lastDSV.getDataSet().getOwner());
 	    	}
 	    	dao.persist(dsv.getDataSet());
 	    	dsv.getDataSet().setLatestVersion(null);
 	    	dsv.setDataSetId(dsv.getDataSet().getId());
 	    	dsv.setOwner(dsv.getDataSet().getOwner());
 	    	dsv.setIsPublic(false);
 	    	dao.persist(dsv);
 	    	lastDSV = dsv;
 		}
 		
 		List<DataSet> myDataSets = dataSetService.getMyDataSets(dsv.getDataSet().getOwner(),2,5,null,true);
 		Assert.assertEquals(myDataSets.size(),2);
 		tearDown();
 	}
 	
 	@Test
 	public void testGetMyDataSetsCount() throws IOException
 	{
 		DataSetVersion dsv = null;
 		DataSetVersion lastDSV = null;
 		
 		for( int i=0; i < 10; ++i )
 		{
 	    	dsv = TestEntityFactory.dataSetVersion();
 	    	if( i == 0 )
 	    	{
 		    	dao.persist(dsv.getDataSet().getCountry());
 		    	dao.persist(dsv.getDataSet().getVegType());
 		    	dao.persist(dsv.getDataSet().getOwner());
 	    	}
 	    	else
 	    	{
 	    		dsv.getDataSet().setCountry(lastDSV.getDataSet().getCountry());
 	    		dsv.getDataSet().setVegType(lastDSV.getDataSet().getVegType());
 	    		dsv.getDataSet().setOwner(lastDSV.getDataSet().getOwner());
 	    	}
 	    	dao.persist(dsv.getDataSet());
 	    	dsv.getDataSet().setLatestVersion(null);
 	    	dsv.setDataSetId(dsv.getDataSet().getId());
 	    	dsv.setOwner(dsv.getDataSet().getOwner());
 	    	dsv.setIsPublic(false);
 	    	dao.persist(dsv);
 	    	lastDSV = dsv;
 		}
 		
 		long result = dataSetService.getMyDataSetsCount(dsv.getDataSet().getOwner());
 		Assert.assertEquals(result,10);
 		tearDown();
 	}
 	
 	@Test
 	public void testGetMyDataSetsColumnDataSetNameAsc() throws IOException
 	{
 		DataSetVersion dsv = null;
 		DataSetVersion lastDSV = null;
 		
 		for( int i=0; i < 10; ++i )
 		{
 	    	dsv = TestEntityFactory.dataSetVersion();
 	    	if( i == 0 )
 	    	{
 		    	dao.persist(dsv.getDataSet().getCountry());
 		    	dao.persist(dsv.getDataSet().getVegType());
 		    	dao.persist(dsv.getDataSet().getOwner());
 	    	}
 	    	else
 	    	{
 	    		dsv.getDataSet().setCountry(lastDSV.getDataSet().getCountry());
 	    		dsv.getDataSet().setVegType(lastDSV.getDataSet().getVegType());
 	    		dsv.getDataSet().setOwner(lastDSV.getDataSet().getOwner());
 	    	}
 	    	dsv.getDataSet().setName(new Integer(10-i).toString());
 	    	dao.persist(dsv.getDataSet());
 	    	dsv.getDataSet().setLatestVersion(null);
 	    	dsv.setDataSetId(dsv.getDataSet().getId());
 	    	dsv.setOwner(dsv.getDataSet().getOwner());
 	    	dsv.setIsPublic(false);
 	    	dao.persist(dsv);
 	    	lastDSV = dsv;
 		}
 		
 		List<DataSet> myDataSets = dataSetService.getMyDataSets(dsv.getDataSet().getOwner(),
 		    10,0,DataSetService.COLUMN_NAME,true);
 		Assert.assertEquals(myDataSets.size(),10);
 		
 		Integer last = null;
 		for(DataSet dataSet : myDataSets )
 		{
 			Integer intName = new Integer(dataSet.getName());
 			if( last != null && last != 10)
 			{
 				Assert.assertTrue(intName.compareTo(last) > 0);
 			}
 			last = intName;
 		}
 		
 		tearDown();
 	}
 	
 	@Test
 	public void testGetMyDataSetsColumnDataSetNameAscOtherWay() throws IOException
 	{
 		DataSetVersion dsv = null;
 		DataSetVersion lastDSV = null;
 		
 		for( int i=0; i < 10; ++i )
 		{
 	    	dsv = TestEntityFactory.dataSetVersion();
 	    	if( i == 0 )
 	    	{
 		    	dao.persist(dsv.getDataSet().getCountry());
 		    	dao.persist(dsv.getDataSet().getVegType());
 		    	dao.persist(dsv.getDataSet().getOwner());
 	    	}
 	    	else
 	    	{
 	    		dsv.getDataSet().setCountry(lastDSV.getDataSet().getCountry());
 	    		dsv.getDataSet().setVegType(lastDSV.getDataSet().getVegType());
 	    		dsv.getDataSet().setOwner(lastDSV.getDataSet().getOwner());
 	    	}
 	    	dsv.getDataSet().setName(new Integer(i).toString());
 	    	dao.persist(dsv.getDataSet());
 	    	dsv.getDataSet().setLatestVersion(null);
 	    	dsv.setDataSetId(dsv.getDataSet().getId());
 	    	dsv.setOwner(dsv.getDataSet().getOwner());
 	    	dsv.setIsPublic(false);
 	    	dao.persist(dsv);
 	    	lastDSV = dsv;
 		}
 		
 		List<DataSet> myDataSets = dataSetService.getMyDataSets(dsv.getDataSet().getOwner(),
 		    10,0,DataSetService.COLUMN_NAME,true);
 		Assert.assertEquals(myDataSets.size(),10);
 		
 		Integer last = null;
 		for(DataSet dataSet : myDataSets )
 		{
 			Integer intName = new Integer(dataSet.getName());
 			if( last != null && last != 10)
 			{
 				Assert.assertTrue(intName.compareTo(last) > 0);
 			}
 			last = intName;
 		}
 		
 		tearDown();
 	}
 	
 	@Test
 	public void testGetMyDataSetsColumnDataSetNameDesc() throws IOException
 	{
 		DataSetVersion dsv = null;
 		DataSetVersion lastDSV = null;
 		
 		for( int i=0; i < 10; ++i )
 		{
 	    	dsv = TestEntityFactory.dataSetVersion();
 	    	if( i == 0 )
 	    	{
 		    	dao.persist(dsv.getDataSet().getCountry());
 		    	dao.persist(dsv.getDataSet().getVegType());
 		    	dao.persist(dsv.getDataSet().getOwner());
 	    	}
 	    	else
 	    	{
 	    		dsv.getDataSet().setCountry(lastDSV.getDataSet().getCountry());
 	    		dsv.getDataSet().setVegType(lastDSV.getDataSet().getVegType());
 	    		dsv.getDataSet().setOwner(lastDSV.getDataSet().getOwner());
 	    	}
 	    	dsv.getDataSet().setName(new Integer(i).toString());
 	    	dao.persist(dsv.getDataSet());
 	    	dsv.getDataSet().setLatestVersion(null);
 	    	dsv.setDataSetId(dsv.getDataSet().getId());
 	    	dsv.setOwner(dsv.getDataSet().getOwner());
 	    	dsv.setIsPublic(false);
 	    	dao.persist(dsv);
 	    	lastDSV = dsv;
 		}
 		
 		List<DataSet> myDataSets = dataSetService.getMyDataSets(dsv.getDataSet().getOwner(),
 		    10,0,DataSetService.COLUMN_NAME,false);
 		Assert.assertEquals(myDataSets.size(),10);
 		
 		Integer last = null;
 		for(DataSet dataSet : myDataSets )
 		{
 			Integer intName = new Integer(dataSet.getName());
 			if( last != null )
 			{
 				Assert.assertTrue(intName.compareTo(last) < 0);
 			}
 			last = intName;
 		}
 		
 		tearDown();
 	}
 	
 	@Test
 	public void testGetMyDataSetsColumnCountryAsc() throws IOException
 	{
 		DataSetVersion dsv = null;
 		DataSetVersion lastDSV = null;
 		
 		for( int i=0; i < 10; ++i )
 		{
 	    	dsv = TestEntityFactory.dataSetVersion();
 	    	if( i == 0 )
 	    	{
 		    	dao.persist(dsv.getDataSet().getVegType());
 		    	dao.persist(dsv.getDataSet().getOwner());
 	    	}
 	    	else
 	    	{
 	    		dsv.getDataSet().setVegType(lastDSV.getDataSet().getVegType());
 	    		dsv.getDataSet().setOwner(lastDSV.getDataSet().getOwner());
 	    	}
 	    	dsv.getDataSet().getCountry().setName(new Integer(9-i).toString());
 	    	dao.persist(dsv.getDataSet().getCountry());
 	    	dao.persist(dsv.getDataSet());
 	    	dsv.getDataSet().setLatestVersion(null);
 	    	dsv.setDataSetId(dsv.getDataSet().getId());
 	    	dsv.setOwner(dsv.getDataSet().getOwner());
 	    	dsv.setIsPublic(false);
 	    	dao.persist(dsv);
 	    	lastDSV = dsv;
 		}
 		
 		List<DataSet> myDataSets = dataSetService.getMyDataSets(dsv.getDataSet().getOwner(),
 		    10,0,DataSetService.COLUMN_COUNTRY,true);
 		Assert.assertEquals(myDataSets.size(),10);
 		
 		Integer last = null;
 		for(DataSet dataSet : myDataSets )
 		{
 			Integer intName = new Integer(dataSet.getCountry().getName());
 			if( last != null )
 			{
 				Assert.assertTrue(intName.compareTo(last) > 0);
 			}
 			last = intName;
 		}
 		
 		tearDown();
 	}
 	
 	@Test
 	public void testGetMyDataSetsColumnCountryAscOtherWay() throws IOException
 	{
 		DataSetVersion dsv = null;
 		DataSetVersion lastDSV = null;
 		
 		for( int i=0; i < 10; ++i )
 		{
 	    	dsv = TestEntityFactory.dataSetVersion();
 	    	if( i == 0 )
 	    	{
 		    	dao.persist(dsv.getDataSet().getVegType());
 		    	dao.persist(dsv.getDataSet().getOwner());
 	    	}
 	    	else
 	    	{
 	    		dsv.getDataSet().setVegType(lastDSV.getDataSet().getVegType());
 	    		dsv.getDataSet().setOwner(lastDSV.getDataSet().getOwner());
 	    	}
 	    	dsv.getDataSet().getCountry().setName(new Integer(i).toString());
 	    	dao.persist(dsv.getDataSet().getCountry());
 	    	dsv.getDataSet().setName(new Integer(i).toString());
 	    	dao.persist(dsv.getDataSet());
 	    	dsv.getDataSet().setLatestVersion(null);
 	    	dsv.setDataSetId(dsv.getDataSet().getId());
 	    	dsv.setOwner(dsv.getDataSet().getOwner());
 	    	dsv.setIsPublic(false);
 	    	dao.persist(dsv);
 	    	lastDSV = dsv;
 		}
 		
 		List<DataSet> myDataSets = dataSetService.getMyDataSets(dsv.getDataSet().getOwner(),
 		    10,0,DataSetService.COLUMN_COUNTRY,true);
 		Assert.assertEquals(myDataSets.size(),10);
 		
 		Integer last = null;
 		for(DataSet dataSet : myDataSets )
 		{
 			Integer intName = new Integer(dataSet.getCountry().getName());
 			if( last != null )
 			{
 				Assert.assertTrue(intName.compareTo(last) > 0);
 			}
 			last = intName;
 		}
 		
 		tearDown();
 	}
 	
 	@Test
 	public void testGetMyDataSetsColumnCountryDesc() throws IOException
 	{
 		DataSetVersion dsv = null;
 		DataSetVersion lastDSV = null;
 		
 		for( int i=0; i < 10; ++i )
 		{
 	    	dsv = TestEntityFactory.dataSetVersion();
 	    	if( i == 0 )
 	    	{
 		    	dao.persist(dsv.getDataSet().getVegType());
 		    	dao.persist(dsv.getDataSet().getOwner());
 	    	}
 	    	else
 	    	{
 	    		dsv.getDataSet().setVegType(lastDSV.getDataSet().getVegType());
 	    		dsv.getDataSet().setOwner(lastDSV.getDataSet().getOwner());
 	    	}
 	    	dsv.getDataSet().getCountry().setName(new Integer(i).toString());
 	    	dao.persist(dsv.getDataSet().getCountry());
 	    	dsv.getDataSet().setName(new Integer(i).toString());
 	    	dao.persist(dsv.getDataSet());
 	    	dsv.getDataSet().setLatestVersion(null);
 	    	dsv.setDataSetId(dsv.getDataSet().getId());
 	    	dsv.setOwner(dsv.getDataSet().getOwner());
 	    	dsv.setIsPublic(false);
 	    	dao.persist(dsv);
 	    	lastDSV = dsv;
 		}
 		
 		List<DataSet> myDataSets = dataSetService.getMyDataSets(dsv.getDataSet().getOwner(),
 		    10,0,DataSetService.COLUMN_COUNTRY,false);
 		Assert.assertEquals(myDataSets.size(),10);
 		
 		Integer last = null;
 		for(DataSet dataSet : myDataSets )
 		{
 			Integer intName = new Integer(dataSet.getCountry().getName());
 			if( last != null )
 			{
 				Assert.assertTrue(intName.compareTo(last) < 0);
 			}
 			last = intName;
 		}
 		
 		tearDown();
 	}
 	
 	@Test
 	public void testGetMyDataSetsColumnVegTypeAsc() throws IOException
 	{
 		DataSetVersion dsv = null;
 		DataSetVersion lastDSV = null;
 		
 		for( int i=0; i < 10; ++i )
 		{
 	    	dsv = TestEntityFactory.dataSetVersion();
 	    	if( i == 0 )
 	    	{
 		    	dao.persist(dsv.getDataSet().getCountry());
 		    	dao.persist(dsv.getDataSet().getOwner());
 	    	}
 	    	else
 	    	{
 	    		dsv.getDataSet().setCountry(lastDSV.getDataSet().getCountry());
 	    		dsv.getDataSet().setOwner(lastDSV.getDataSet().getOwner());
 	    	}
 	    	dsv.getDataSet().getVegType().setVegetationType(new Integer(9-i).toString());
 	    	dao.persist(dsv.getDataSet().getVegType());
 	    	dao.persist(dsv.getDataSet());
 	    	dsv.getDataSet().setLatestVersion(null);
 	    	dsv.setDataSetId(dsv.getDataSet().getId());
 	    	dsv.setOwner(dsv.getDataSet().getOwner());
 	    	dsv.setIsPublic(false);
 	    	dao.persist(dsv);
 	    	lastDSV = dsv;
 		}
 		
 		List<DataSet> myDataSets = dataSetService.getMyDataSets(dsv.getDataSet().getOwner(),
 		    10,0,DataSetService.COLUMN_VEG_TYPE,true);
 		Assert.assertEquals(myDataSets.size(),10);
 		
 		Integer last = null;
 		for(DataSet dataSet : myDataSets )
 		{
 			Integer intName = new Integer(dataSet.getVegType().getVegetationType());
 			if( last != null)
 			{
 				Assert.assertTrue(intName.compareTo(last) > 0);
 			}
 			last = intName;
 		}
 		
 		tearDown();
 	}
 	@Test
 	public void testAddSortItemAllNull()
 	{
 		Assert.assertNull(dataSetService.addSortItem(null,null,null,null,null));
 	}
 	
 	@Test
 	public void testAddSortItemNameSortNone()
 	{
 		String sortString = "";
 		String result = dataSetService.addSortItem(sortString,DataSetService.COLUMN_NAME,
 		    Globals.SORT_NONE,null,null);
 		Assert.assertEquals(result.length(),0);
 	}
 	
 	@Test
 	public void testAddSortItemNameSortASC()
 	{
 		String sortString = "";
 		String result = dataSetService.addSortItem(sortString,DataSetService.COLUMN_NAME,
 		    Globals.SORT_ASC,null,null);
		Assert.assertEquals(result," ds.dataSetType ASC,");
 	}
 	
 	@Test
 	public void testAddSortAllNull()
 	{
 		Assert.assertNull(dataSetService.addSort(null, null, null, null, null, null));
 	}
 	
 	@Test
 	public void testAddSortDefault()
 	{
 		String queryString = "";
 		Assert.assertEquals(dataSetService.addSort(queryString, null, null, null, null, null),
 		    " ORDER BY ds.name ASC");
 	}
 	
 	@Test
 	public void testAddSortAllASCNoPref()
 	{
 		String queryString = "";
 		Assert.assertEquals(dataSetService.addSort(queryString,Globals.SORT_ASC,
 				Globals.SORT_ASC,Globals.SORT_ASC,null,null),
		    " order by  ds.name ASC, ds.country.name ASC, ds.vegetationType.vegetationType ASC");
 	}
 	
 	@Test
 	public void testAddSortAllASCVegTypePref1CountryPref2()
 	{
 		String queryString = "";
 		Assert.assertEquals(dataSetService.addSort(queryString,Globals.SORT_ASC,
 				Globals.SORT_ASC,Globals.SORT_ASC,DataSetService.COLUMN_VEG_TYPE,DataSetService.COLUMN_COUNTRY),
		    " order by  ds.vegetationType.vegetationType ASC, ds.country.name ASC, ds.name ASC");
 	}
 	
 	@Test
 	public void testAddSortAllNone()
 	{
 		String queryString = "";
 		Assert.assertEquals(dataSetService.addSort(queryString,Globals.SORT_NONE,
 				Globals.SORT_NONE,Globals.SORT_NONE,null,null),
 				" ORDER BY ds.name ASC");
 	}
 	
 	@Test
 	public void testGetMyDataSetsAdvancedSorting() throws IOException
 	{
 		setUp();
 		List<DataSet> myDataSets = dataSetService.getMyDataSetsAdvancedSorting(entity.getDataSet().getOwner(),
 		    null,null,null,null,null,null,null,null,null);
 		Assert.assertEquals(myDataSets.size(),1);
 		Assert.assertEquals(myDataSets.get(0).getId(),entity.getDataSet().getId());
 		tearDown();
 	}
 	
 	@Test
 	public void testGetMyDataSetsPaginationAdvancedSorting() throws IOException
 	{
 		DataSetVersion dsv = null;
 		DataSetVersion lastDSV = null;
 		
 		for( int i=0; i < 10; ++i )
 		{
 	    	dsv = TestEntityFactory.dataSetVersion();
 	    	if( i == 0 )
 	    	{
 		    	dao.persist(dsv.getDataSet().getCountry());
 		    	dao.persist(dsv.getDataSet().getVegType());
 		    	dao.persist(dsv.getDataSet().getOwner());
 	    	}
 	    	else
 	    	{
 	    		dsv.getDataSet().setCountry(lastDSV.getDataSet().getCountry());
 	    		dsv.getDataSet().setVegType(lastDSV.getDataSet().getVegType());
 	    		dsv.getDataSet().setOwner(lastDSV.getDataSet().getOwner());
 	    	}
 	    	dao.persist(dsv.getDataSet());
 	    	dsv.getDataSet().setLatestVersion(null);
 	    	dsv.setDataSetId(dsv.getDataSet().getId());
 	    	dsv.setOwner(dsv.getDataSet().getOwner());
 	    	dsv.setIsPublic(false);
 	    	dao.persist(dsv);
 	    	lastDSV = dsv;
 		}
 		
 		List<DataSet> myDataSets = dataSetService.getMyDataSetsAdvancedSorting(dsv.getDataSet().getOwner(),2,5,null,null,
 		    null,null,null,null,null);
 		Assert.assertEquals(myDataSets.size(),2);
 		tearDown();
 	}
 	
 	@Test
 	public void testGetMyDataSetsColumnDataSetNameAscAdvancedSorting() throws IOException
 	{
 		DataSetVersion dsv = null;
 		DataSetVersion lastDSV = null;
 		
 		for( int i=0; i < 10; ++i )
 		{
 	    	dsv = TestEntityFactory.dataSetVersion();
 	    	if( i == 0 )
 	    	{
 		    	dao.persist(dsv.getDataSet().getCountry());
 		    	dao.persist(dsv.getDataSet().getVegType());
 		    	dao.persist(dsv.getDataSet().getOwner());
 	    	}
 	    	else
 	    	{
 	    		dsv.getDataSet().setCountry(lastDSV.getDataSet().getCountry());
 	    		dsv.getDataSet().setVegType(lastDSV.getDataSet().getVegType());
 	    		dsv.getDataSet().setOwner(lastDSV.getDataSet().getOwner());
 	    	}
 	    	dsv.getDataSet().setName(new Integer(10-i).toString());
 	    	dao.persist(dsv.getDataSet());
 	    	dsv.getDataSet().setLatestVersion(null);
 	    	dsv.setDataSetId(dsv.getDataSet().getId());
 	    	dsv.setOwner(dsv.getDataSet().getOwner());
 	    	dsv.setIsPublic(false);
 	    	dao.persist(dsv);
 	    	lastDSV = dsv;
 		}
 		
 		List<DataSet> myDataSets = dataSetService.getMyDataSetsAdvancedSorting(dsv.getDataSet().getOwner(),
 		    10,0,null,null,Globals.SORT_ASC,null,null,null,null);
 		Assert.assertEquals(myDataSets.size(),10);
 		
 		Integer last = null;
 		for(DataSet dataSet : myDataSets )
 		{
 			Integer intName = new Integer(dataSet.getName());
 			if( last != null && last != 10)
 			{
 				Assert.assertTrue(intName.compareTo(last) > 0);
 			}
 			last = intName;
 		}
 		
 		tearDown();
 	}
 	
 	@Test
 	public void testGetMyDataSetsColumnDataSetNameAscOtherWayAdvancedSorting() throws IOException
 	{
 		DataSetVersion dsv = null;
 		DataSetVersion lastDSV = null;
 		
 		for( int i=0; i < 10; ++i )
 		{
 	    	dsv = TestEntityFactory.dataSetVersion();
 	    	if( i == 0 )
 	    	{
 		    	dao.persist(dsv.getDataSet().getCountry());
 		    	dao.persist(dsv.getDataSet().getVegType());
 		    	dao.persist(dsv.getDataSet().getOwner());
 	    	}
 	    	else
 	    	{
 	    		dsv.getDataSet().setCountry(lastDSV.getDataSet().getCountry());
 	    		dsv.getDataSet().setVegType(lastDSV.getDataSet().getVegType());
 	    		dsv.getDataSet().setOwner(lastDSV.getDataSet().getOwner());
 	    	}
 	    	dsv.getDataSet().setName(new Integer(i).toString());
 	    	dao.persist(dsv.getDataSet());
 	    	dsv.getDataSet().setLatestVersion(null);
 	    	dsv.setDataSetId(dsv.getDataSet().getId());
 	    	dsv.setOwner(dsv.getDataSet().getOwner());
 	    	dsv.setIsPublic(false);
 	    	dao.persist(dsv);
 	    	lastDSV = dsv;
 		}
 		
 		List<DataSet> myDataSets = dataSetService.getMyDataSetsAdvancedSorting(dsv.getDataSet().getOwner(),
 			    10,0,null,null,Globals.SORT_ASC,null,null,null,null);
 		Assert.assertEquals(myDataSets.size(),10);
 		
 		Integer last = null;
 		for(DataSet dataSet : myDataSets )
 		{
 			Integer intName = new Integer(dataSet.getName());
 			if( last != null && last != 10)
 			{
 				Assert.assertTrue(intName.compareTo(last) > 0);
 			}
 			last = intName;
 		}
 		
 		tearDown();
 	}
 	
 	@Test
 	public void testGetMyDataSetsColumnDataSetNameDescAdvancedSorting() throws IOException
 	{
 		DataSetVersion dsv = null;
 		DataSetVersion lastDSV = null;
 		
 		for( int i=0; i < 10; ++i )
 		{
 	    	dsv = TestEntityFactory.dataSetVersion();
 	    	if( i == 0 )
 	    	{
 		    	dao.persist(dsv.getDataSet().getCountry());
 		    	dao.persist(dsv.getDataSet().getVegType());
 		    	dao.persist(dsv.getDataSet().getOwner());
 	    	}
 	    	else
 	    	{
 	    		dsv.getDataSet().setCountry(lastDSV.getDataSet().getCountry());
 	    		dsv.getDataSet().setVegType(lastDSV.getDataSet().getVegType());
 	    		dsv.getDataSet().setOwner(lastDSV.getDataSet().getOwner());
 	    	}
 	    	dsv.getDataSet().setName(new Integer(i).toString());
 	    	dao.persist(dsv.getDataSet());
 	    	dsv.getDataSet().setLatestVersion(null);
 	    	dsv.setDataSetId(dsv.getDataSet().getId());
 	    	dsv.setOwner(dsv.getDataSet().getOwner());
 	    	dsv.setIsPublic(false);
 	    	dao.persist(dsv);
 	    	lastDSV = dsv;
 		}
 		
 		List<DataSet> myDataSets = dataSetService.getMyDataSetsAdvancedSorting(dsv.getDataSet().getOwner(),
 			    10,0,null,null,Globals.SORT_DESC,null,null,null,null);
 		Assert.assertEquals(myDataSets.size(),10);
 		
 		Integer last = null;
 		for(DataSet dataSet : myDataSets )
 		{
 			Integer intName = new Integer(dataSet.getName());
 			if( last != null )
 			{
 				Assert.assertTrue(intName.compareTo(last) < 0);
 			}
 			last = intName;
 		}
 		
 		tearDown();
 	}
 	
 	@Test
 	public void testGetMyDataSetsColumnCountryAscAdvancedSorting() throws IOException
 	{
 		DataSetVersion dsv = null;
 		DataSetVersion lastDSV = null;
 		
 		for( int i=0; i < 10; ++i )
 		{
 	    	dsv = TestEntityFactory.dataSetVersion();
 	    	if( i == 0 )
 	    	{
 		    	dao.persist(dsv.getDataSet().getVegType());
 		    	dao.persist(dsv.getDataSet().getOwner());
 	    	}
 	    	else
 	    	{
 	    		dsv.getDataSet().setVegType(lastDSV.getDataSet().getVegType());
 	    		dsv.getDataSet().setOwner(lastDSV.getDataSet().getOwner());
 	    	}
 	    	dsv.getDataSet().getCountry().setName(new Integer(9-i).toString());
 	    	dao.persist(dsv.getDataSet().getCountry());
 	    	dao.persist(dsv.getDataSet());
 	    	dsv.getDataSet().setLatestVersion(null);
 	    	dsv.setDataSetId(dsv.getDataSet().getId());
 	    	dsv.setOwner(dsv.getDataSet().getOwner());
 	    	dsv.setIsPublic(false);
 	    	dao.persist(dsv);
 	    	lastDSV = dsv;
 		}
 		
 		List<DataSet> myDataSets = dataSetService.getMyDataSetsAdvancedSorting(dsv.getDataSet().getOwner(),
 			    10,0,null,null,null,Globals.SORT_ASC,null,null,null);
 		Assert.assertEquals(myDataSets.size(),10);
 		
 		Integer last = null;
 		for(DataSet dataSet : myDataSets )
 		{
 			Integer intName = new Integer(dataSet.getCountry().getName());
 			if( last != null )
 			{
 				Assert.assertTrue(intName.compareTo(last) > 0);
 			}
 			last = intName;
 		}
 		
 		tearDown();
 	}
 	
 	@Test
 	public void testGetMyDataSetsColumnCountryAscOtherWayAdvancedSorting() throws IOException
 	{
 		DataSetVersion dsv = null;
 		DataSetVersion lastDSV = null;
 		
 		for( int i=0; i < 10; ++i )
 		{
 	    	dsv = TestEntityFactory.dataSetVersion();
 	    	if( i == 0 )
 	    	{
 		    	dao.persist(dsv.getDataSet().getVegType());
 		    	dao.persist(dsv.getDataSet().getOwner());
 	    	}
 	    	else
 	    	{
 	    		dsv.getDataSet().setVegType(lastDSV.getDataSet().getVegType());
 	    		dsv.getDataSet().setOwner(lastDSV.getDataSet().getOwner());
 	    	}
 	    	dsv.getDataSet().getCountry().setName(new Integer(i).toString());
 	    	dao.persist(dsv.getDataSet().getCountry());
 	    	dsv.getDataSet().setName(new Integer(i).toString());
 	    	dao.persist(dsv.getDataSet());
 	    	dsv.getDataSet().setLatestVersion(null);
 	    	dsv.setDataSetId(dsv.getDataSet().getId());
 	    	dsv.setOwner(dsv.getDataSet().getOwner());
 	    	dsv.setIsPublic(false);
 	    	dao.persist(dsv);
 	    	lastDSV = dsv;
 		}
 		
 		List<DataSet> myDataSets = dataSetService.getMyDataSetsAdvancedSorting(dsv.getDataSet().getOwner(),
 			    10,0,null,null,null,Globals.SORT_ASC,null,null,null);
 		Assert.assertEquals(myDataSets.size(),10);
 		
 		Integer last = null;
 		for(DataSet dataSet : myDataSets )
 		{
 			Integer intName = new Integer(dataSet.getCountry().getName());
 			if( last != null )
 			{
 				Assert.assertTrue(intName.compareTo(last) > 0);
 			}
 			last = intName;
 		}
 		
 		tearDown();
 	}
 	
 	@Test
 	public void testGetMyDataSetsColumnCountryDescAdvancedSorting() throws IOException
 	{
 		DataSetVersion dsv = null;
 		DataSetVersion lastDSV = null;
 		
 		for( int i=0; i < 10; ++i )
 		{
 	    	dsv = TestEntityFactory.dataSetVersion();
 	    	if( i == 0 )
 	    	{
 		    	dao.persist(dsv.getDataSet().getVegType());
 		    	dao.persist(dsv.getDataSet().getOwner());
 	    	}
 	    	else
 	    	{
 	    		dsv.getDataSet().setVegType(lastDSV.getDataSet().getVegType());
 	    		dsv.getDataSet().setOwner(lastDSV.getDataSet().getOwner());
 	    	}
 	    	dsv.getDataSet().getCountry().setName(new Integer(i).toString());
 	    	dao.persist(dsv.getDataSet().getCountry());
 	    	dsv.getDataSet().setName(new Integer(i).toString());
 	    	dao.persist(dsv.getDataSet());
 	    	dsv.getDataSet().setLatestVersion(null);
 	    	dsv.setDataSetId(dsv.getDataSet().getId());
 	    	dsv.setOwner(dsv.getDataSet().getOwner());
 	    	dsv.setIsPublic(false);
 	    	dao.persist(dsv);
 	    	lastDSV = dsv;
 		}
 		
 		List<DataSet> myDataSets = dataSetService.getMyDataSetsAdvancedSorting(dsv.getDataSet().getOwner(),
 			    10,0,null,null,null,Globals.SORT_DESC,null,null,null);
 		Assert.assertEquals(myDataSets.size(),10);
 		
 		Integer last = null;
 		for(DataSet dataSet : myDataSets )
 		{
 			Integer intName = new Integer(dataSet.getCountry().getName());
 			if( last != null )
 			{
 				Assert.assertTrue(intName.compareTo(last) < 0);
 			}
 			last = intName;
 		}
 		
 		tearDown();
 	}
 	
 	@Test
 	public void testGetMyDataSetsFirstAndSecondSortPreferences() throws IOException
 	{
 		DataSetVersion dsv = null;
 		DataSetVersion lastDSV = null;
 		
 		for( int i=0; i < 10; ++i )
 		{
 	    	dsv = TestEntityFactory.dataSetVersion();
 	    	if( i == 0 )
 	    	{
 		    	dao.persist(dsv.getDataSet().getVegType());
 		    	dao.persist(dsv.getDataSet().getOwner());
 	    	}
 	    	else
 	    	{
 	    		dsv.getDataSet().setVegType(lastDSV.getDataSet().getVegType());
 	    		dsv.getDataSet().setOwner(lastDSV.getDataSet().getOwner());
 	    	}
 	    	dsv.getDataSet().getCountry().setName(new Integer(10-i-1).toString());
 	    	dao.persist(dsv.getDataSet().getCountry());
 	    	dsv.getDataSet().setName(new Integer(i).toString());
 	    	dao.persist(dsv.getDataSet());
 	    	dsv.getDataSet().setLatestVersion(null);
 	    	dsv.setDataSetId(dsv.getDataSet().getId());
 	    	dsv.setOwner(dsv.getDataSet().getOwner());
 	    	dsv.setIsPublic(false);
 	    	dao.persist(dsv);
 	    	lastDSV = dsv;
 		}
 		
 		List<DataSet> myDataSets = dataSetService.getMyDataSetsAdvancedSorting(dsv.getDataSet().getOwner(),
 			    10,0,null,null,Globals.SORT_ASC,Globals.SORT_ASC,null,DataSetService.COLUMN_COUNTRY,DataSetService.COLUMN_NAME);
 		Assert.assertEquals(myDataSets.size(),10);
 		
 		Integer last = null;
 		for(DataSet dataSet : myDataSets )
 		{
 			Integer intName = new Integer(dataSet.getCountry().getName());
 			if( last != null )
 			{
 				Assert.assertTrue(intName.compareTo(last) > 0);
 			}
 			last = intName;
 		}
 		
 		tearDown();
 	}
 	
 	@Test
 	public void testGetMyDataSetsColumnVegTypeAscAdvancedSorting() throws IOException
 	{
 		DataSetVersion dsv = null;
 		DataSetVersion lastDSV = null;
 		
 		for( int i=0; i < 10; ++i )
 		{
 	    	dsv = TestEntityFactory.dataSetVersion();
 	    	if( i == 0 )
 	    	{
 		    	dao.persist(dsv.getDataSet().getCountry());
 		    	dao.persist(dsv.getDataSet().getOwner());
 	    	}
 	    	else
 	    	{
 	    		dsv.getDataSet().setCountry(lastDSV.getDataSet().getCountry());
 	    		dsv.getDataSet().setOwner(lastDSV.getDataSet().getOwner());
 	    	}
 	    	dsv.getDataSet().getVegType().setVegetationType(new Integer(9-i).toString());
 	    	dao.persist(dsv.getDataSet().getVegType());
 	    	dao.persist(dsv.getDataSet());
 	    	dsv.getDataSet().setLatestVersion(null);
 	    	dsv.setDataSetId(dsv.getDataSet().getId());
 	    	dsv.setOwner(dsv.getDataSet().getOwner());
 	    	dsv.setIsPublic(false);
 	    	dao.persist(dsv);
 	    	lastDSV = dsv;
 		}
 		
 		List<DataSet> myDataSets = dataSetService.getMyDataSetsAdvancedSorting(dsv.getDataSet().getOwner(),
 			    10,0,null,null,null,null,Globals.SORT_ASC,null,null);
 		Assert.assertEquals(myDataSets.size(),10);
 		
 		Integer last = null;
 		for(DataSet dataSet : myDataSets )
 		{
 			Integer intName = new Integer(dataSet.getVegType().getVegetationType());
 			if( last != null)
 			{
 				Assert.assertTrue(intName.compareTo(last) > 0);
 			}
 			last = intName;
 		}
 		
 		tearDown();
 	}
 }
