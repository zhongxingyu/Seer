 package com.github.kpacha.mafia.test.service;
 
 import junit.framework.Assert;
 
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 import org.springframework.transaction.annotation.Transactional;
 
 import com.github.kpacha.mafia.model.Gangster;
import com.github.kpacha.mafia.service.impl.GangsterServiceImpl;
 import com.github.kpacha.mafia.test.GangsterAbstractTest;
 
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration({ "/gangster-test-context.xml" })
 @Transactional
 public class GangsterServiceTest extends GangsterAbstractTest {
 
     @Autowired
    private GangsterServiceImpl service;
 
     @Test
     @Transactional
     public void enrolingASubordinateShouldAlterTheBossEntity() {
 	Gangster paulie = buildGangster();
 	paulie.setName("Paulie 'Walnuts' Gualtieri");
 	Gangster persitedPaulie = repo.save(paulie);
 
 	Gangster tonySoprano = buildGangster();
 	Gangster persitedTonySoprano = repo.save(tonySoprano);
 
 	service.enroleSubordinate(persitedTonySoprano, persitedPaulie);
 
 	persitedTonySoprano = repo.findOne(persitedTonySoprano.getNodeId());
 	// new entities had been added to the related collections
 	Assert.assertEquals(tonySoprano.getManaged().size() + 1,
 		persitedTonySoprano.getManaged().size());
 	Assert.assertEquals(tonySoprano.getSubordinates().size() + 1,
 		persitedTonySoprano.getSubordinates().size());
 
 	// the subordinate is accessible throught the rellated collections
 	persitedPaulie = repo.findOne(persitedPaulie.getNodeId());
 	assertEqualGangsters(
 		persitedPaulie,
 		repo.findOne(persitedTonySoprano.getManaged().iterator().next()
 			.getSubordinate().getNodeId()));
 	assertEqualGangsters(
 		persitedPaulie,
 		repo.findOne(persitedTonySoprano.getSubordinates().iterator()
 			.next().getNodeId()));
     }
 
     @Test
     @Transactional
     public void enrolingASubordinateShouldAlterTheSubordinateEntity() {
 	Gangster paulie = buildGangster();
 	paulie.setName("Paulie 'Walnuts' Gualtieri");
 	Gangster persitedPaulie = repo.save(paulie);
 
 	Gangster tonySoprano = buildGangster();
 	Gangster persitedTonySoprano = repo.save(tonySoprano);
 
 	service.enroleSubordinate(persitedTonySoprano, persitedPaulie);
 
 	persitedPaulie = repo.findOne(persitedPaulie.getNodeId());
 	// new entities had been added to the related collections
 	Assert.assertEquals(paulie.getManagers().size() + 1, persitedPaulie
 		.getManagers().size());
 	Assert.assertEquals(paulie.getBosses().size() + 1, persitedPaulie
 		.getBosses().size());
 
 	// the boss is accessible throught the rellated collections
 	persitedTonySoprano = repo.findOne(persitedTonySoprano.getNodeId());
 	assertEqualGangsters(
 		persitedTonySoprano,
 		repo.findOne(persitedPaulie.getManagers().iterator().next()
 			.getBoss().getNodeId()));
 	assertEqualGangsters(
 		persitedTonySoprano,
 		repo.findOne(persitedPaulie.getBosses().iterator().next()
 			.getNodeId()));
     }
 
 }
