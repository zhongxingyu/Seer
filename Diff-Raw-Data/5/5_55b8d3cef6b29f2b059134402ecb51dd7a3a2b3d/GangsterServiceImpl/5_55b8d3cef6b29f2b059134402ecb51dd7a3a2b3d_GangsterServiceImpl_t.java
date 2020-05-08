 package com.github.kpacha.mafia.service.impl;
 
 import java.util.Collection;
 import java.util.Date;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Set;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.data.domain.Page;
 import org.springframework.data.domain.Pageable;
 import org.springframework.data.neo4j.template.Neo4jOperations;
 import org.springframework.stereotype.Service;
 import org.springframework.transaction.annotation.Transactional;
 
 import com.github.kpacha.mafia.model.Gangster;
 import com.github.kpacha.mafia.model.Manager;
 import com.github.kpacha.mafia.repository.GangsterRepository;
 import com.github.kpacha.mafia.service.GangsterService;
 
 @Service
 @Transactional
 public class GangsterServiceImpl implements GangsterService {
 
     private static final Logger log = LoggerFactory
 	    .getLogger(GangsterServiceImpl.class);
 
     @Autowired
     private GangsterRepository repo;
 
     @Autowired
     private Neo4jOperations template;
 
     @Transactional(readOnly = true)
     public Gangster find(Long gangsterId) {
 	return repo.findOne(gangsterId);
     }
 
     @Transactional(readOnly = true)
     public Page<Gangster> findAll(Pageable pageable) {
 	return repo.findAll(pageable);
     }
 
     @Transactional(readOnly = true)
     public Page<Gangster> findByNameLike(String query, Pageable pageable) {
 	return repo.findByNameLike(query, pageable);
     }
 
     @Override
     public Gangster save(Gangster gangster) {
 	return repo.save(gangster);
     }
 
     @Override
     public void deleteAll() {
 	repo.deleteAll();
     }
 
     public Gangster enroleSubordinate(final Gangster boss,
 	    final Gangster subordinate) {
 	log.debug("Enrolling " + boss.getNodeId() + " -> "
 		+ subordinate.getNodeId());
 	log.debug("Initial managed size: " + boss.getManaged().size());
 	Set<Manager> managed = boss.getManaged();
 	managed.add(buildManager(boss, subordinate, true));
 	boss.setManaged(managed);
 	// boss.addKnown(subordinate);
 	log.debug("Final managed size: " + boss.getManaged().size());
 	return boss;
     }
 
     public Gangster getUpdatedInstance(Gangster gangster) {
 	Gangster storedGangster = null;
 	if (gangster != null) {
 	    if (gangster.getNodeId() == null) {
 		log.debug("Saving new instance " + gangster);
 		storedGangster = repo.save(gangster);
 	    } else {
 		log.debug("Looking for instance " + gangster.getNodeId());
 		storedGangster = repo.findOne(gangster.getNodeId());
 		if (storedGangster == null) {
 		    log.debug("Saving new instance " + gangster);
 		    storedGangster = repo.save(gangster);
 		}
 	    }
 	}
 	log.debug("Returning instance " + storedGangster);
 	return storedGangster;
     }
 
     public void release(final Gangster convicted) {
 	// update the convicted properties
 	convicted.setOnDuty(true);
 	Gangster gangster = repo.save(convicted);
 	log.debug("Releasing [" + convicted.getNodeId() + "] from jail!");
 
 	for (Manager manager : gangster.getManaged()) {
 	    log.debug("The management relationship [" + manager.getId()
 		    + "] has onDuty=[" + manager.isOnDuty() + "], createdAd=["
 		    + manager.getCreatedAt() + "], updatedAd=["
 		    + manager.getUpdatedAt() + "]");
 	    revertEndosement(manager);
 	}
     }
 
     private void revertEndosement(final Manager formerManager) {
 	Gangster gangster = getUpdatedInstance(formerManager.getSubordinate());
 	for (Manager manager : gangster.getManagers()) {
 	    if (manager.getId().equals(formerManager.getId())) {
 		manager.setOnDuty(true);
 		manager.setUpdatedAt(new Date());
 		saveManager(manager);
 	    } else if (manager.getCreatedAt().getTime() >= formerManager
 		    .getCreatedAt().getTime()) {
 		deleteManager(manager);
 	    }
 	}
     }
 
     private void deleteManager(Manager manager) {
 	template.delete(manager);
     }
 
     private void saveManager(Manager manager) {
 	template.save(manager);
     }
 
     public Gangster sendToJail(final Gangster convicted) {
 	log.debug("Sending [" + convicted.getNodeId() + "] to jail!");
 	Gangster boss = getBoss(convicted);
 	log.debug("Convicted's boss: " + boss.getNodeId());
 	Gangster candidate = null;
 	Set<Gangster> subordinates = repo.getActiveSubordinates(convicted);
 	log.debug("Subordinates size: " + subordinates.size());
 
 	// look for a candidate for the substitution
 	if (boss != null) {
 	    candidate = getCandidateFromSet(repo.getActiveCollegues(convicted));
 	    log.debug("Is the collegue [" + candidate + "] the best candidate?");
 	}
 	if (candidate == null) {
 	    candidate = getCandidateFromSet(subordinates);
 	    log.debug("The subordinate [" + candidate
 		    + "] is the best candidate");
 	}
 
 	// release the subordinates and enrole them with the candidate
 	if (candidate != null) {
 	    candidate = prepareEndorsement(convicted, boss, candidate,
 		    subordinates);
 	    log.debug("Substitutor [" + candidate + "] with "
 		    + candidate.getManaged().size() + " subordinates saved");
 	}
 
 	// update the convicted properties;
 	updateConvicted(convicted);
 
 	return candidate;
     }
 
     private Gangster prepareEndorsement(final Gangster convicted,
 	    Gangster boss, final Gangster candidate, Set<Gangster> subordinates) {
 	log.debug("Before the endorsement, the substitutor [" + candidate
 		+ "] has " + candidate.getManaged().size() + " subordinates");
 	Gangster substitutor = repo.save(endoseSubordinates(convicted,
 		candidate, subordinates));
 	log.debug("After the endorsement, the substitutor [" + candidate
 		+ "] has " + substitutor.getManaged().size() + " subordinates");
 	// if the candidate was a subordinate, promote him
	Gangster previousBoss = getBoss(substitutor);
	if (previousBoss != null
		&& convicted.getNodeId().equals(previousBoss.getNodeId())
		&& boss != null) {
 	    log.debug("The substitutor [" + substitutor
 		    + "] was a subordinate. Promoting him!");
 	    repo.save(enroleSubordinate(boss, substitutor));
 	}
 	return substitutor;
     }
 
     private Gangster updateConvicted(final Gangster convicted) {
 	convicted.setOnDuty(false);
 	Collection<Manager> managed = new HashSet<Manager>();
 	for (Manager manager : convicted.getManaged()) {
 	    manager.setOnDuty(false);
 	    manager.setUpdatedAt(new Date());
 	    saveManager(manager);
 	    log.debug("The management relationship [" + manager.getId()
 		    + "] has onDuty=[" + manager.isOnDuty() + "], updateAd=["
 		    + manager.getUpdatedAt() + "]");
 	    managed.add(manager);
 	}
 	convicted.setManaged(managed);
 	return repo.save(convicted);
     }
 
     private Gangster endoseSubordinates(Gangster convicted, Gangster candidate,
 	    Set<Gangster> subordinates) {
 	for (Gangster subordinate : subordinates) {
 	    if (!subordinate.getNodeId().equals(candidate.getNodeId())) {
 		candidate = enroleSubordinate(candidate, subordinate);
 	    }
 	}
 	return candidate;
     }
 
     private Gangster getCandidateFromSet(Set<Gangster> gangsters) {
 	Gangster candidate = null;
 	Iterator<Gangster> gangstersIterator = gangsters.iterator();
 	if (gangstersIterator.hasNext()) {
 	    candidate = gangstersIterator.next();
 	}
 	return candidate;
     }
 
     @Transactional(readOnly = true)
     public Gangster getBoss(Gangster gangster) {
 	Gangster boss = null;
 	Set<Gangster> bosses = repo.getCurrentBoss(gangster);
 	if (!bosses.isEmpty()) {
 	    boss = (Gangster) bosses.iterator().next();
 	}
 	return boss;
     }
 
     @Transactional(readOnly = true)
     public Integer getLevel(Gangster gangster) {
 	Integer level = null;
 	Set<Integer> result = repo.getLevel(gangster);
 	if (!result.isEmpty()) {
 	    level = (Integer) result.iterator().next();
 	}
 	return level;
     }
 
     @Transactional(readOnly = true)
     public Integer countAllSubordinates(Gangster gangster) {
 	Integer subordinates = null;
 	Set<Integer> result = repo.countAllSubordinates(gangster);
 	if (!result.isEmpty()) {
 	    subordinates = (Integer) result.iterator().next();
 	}
 	return subordinates;
     }
 
     private Manager buildManager(Gangster boss, Gangster subordinate,
 	    boolean onDuty) {
 	Manager manager = new Manager();
 	manager.setBoss(boss);
 	manager.setSubordinate(subordinate);
 	manager.setCreatedAt(new Date());
 	manager.setOnDuty(onDuty);
 	return manager;
     }
 }
