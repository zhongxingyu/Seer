 package com.metal.test;
 
 import javax.ejb.EJB;
 
 import org.jboss.arquillian.container.test.api.Deployment;
 import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.persistence.PersistenceTest;
import org.jboss.arquillian.persistence.TransactionMode;
import org.jboss.arquillian.persistence.Transactional;
 import org.jboss.shrinkwrap.api.Archive;
 import org.jboss.shrinkwrap.api.ShrinkWrap;
 import org.jboss.shrinkwrap.api.asset.EmptyAsset;
 import org.jboss.shrinkwrap.api.spec.WebArchive;
 import org.junit.Assert;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 
 import com.metal.webservice.Statistics;
 
 @RunWith(Arquillian.class)
@PersistenceTest
@Transactional(TransactionMode.ROLLBACK)
 public class ArquillianIT {
 
 	@Deployment
 	public static Archive<?> createDeployment() {
 		return ShrinkWrap.create(WebArchive.class, "test.war").addPackage(Statistics.class.getPackage())
 				.addAsResource("test-persistence.xml", "META-INF/persistence.xml")
 				.addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
 	}
 
 	@EJB
 	Statistics statistics;
 
 	@Test
 	public void findAll() {
 		Assert.assertEquals(0, statistics.getRatingParticipants());
 	}
 }
