 package de.soulworks.dam.webservice;
 
 import de.soulworks.dam.webservice.service.*;
 import org.junit.runner.RunWith;
 import org.junit.runners.Suite;
 
 /**
 * @author Christian Schmitz <csc@soulworks.de
  */
 @RunWith(Suite.class)
 @Suite.SuiteClasses({
 	AccountServiceTest.class,
 	AssetServiceTest.class,
 	BucketServiceTest.class,
 	CustomerServiceTest.class,
 	ListServiceTest.class,
 	UploadServiceTest.class
 })
 public class ServiceTestSuite {
 }
