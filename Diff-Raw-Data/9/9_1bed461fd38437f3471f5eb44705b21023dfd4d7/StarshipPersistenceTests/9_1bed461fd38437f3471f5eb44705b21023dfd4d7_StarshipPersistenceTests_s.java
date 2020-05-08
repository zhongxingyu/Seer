 package com.lordofthejars.hibernate;
 
 import static com.lordofthejars.hibernate.model.OfficerBuilder.officer;
 import static com.lordofthejars.hibernate.model.PhysicsBuilder.physics;
 import static com.lordofthejars.hibernate.model.StarshipBuilder.starship;
 
 import java.util.Calendar;
 
 import javax.inject.Inject;
 import javax.persistence.EntityManager;
 import javax.persistence.EntityManagerFactory;
 import javax.persistence.EntityTransaction;
 
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 
 import com.lordofthejars.hibernate.model.AffiliationEnum;
 import com.lordofthejars.hibernate.model.Officer;
 import com.lordofthejars.hibernate.model.Physics;
 import com.lordofthejars.hibernate.model.PlanetEnum;
 import com.lordofthejars.hibernate.model.RankEnum;
 import com.lordofthejars.hibernate.model.SpeciesEnum;
 import com.lordofthejars.hibernate.model.Starship;
 import com.lordofthejars.hibernate.model.StarshipClassEnum;
 
 @ContextConfiguration
 @RunWith(SpringJUnit4ClassRunner.class)
 public class StarshipPersistenceTests {
 
 	@Inject
 	private EntityManagerFactory entityManagerFactory;
 
 	@Test
 	public void testSaveOrderWithItems() throws Exception {
 
 		Starship starship = createData();
 		findStarship(starship);
 
 	}
 
 	private Starship createData() {
 		EntityManager entityManager = entityManagerFactory.createEntityManager();
 		EntityTransaction transaction = entityManager.getTransaction();
 		transaction.begin();
 		
 		Physics physics = physics().height(137.5D).length(642.5D)
 				.power("Wrap reactor").width(467.0D).build();
 
 		Calendar launched = Calendar.getInstance();
 		launched.set(2363, 9, 4);
 
 		Starship starship = starship().registry("NCC-1701-D").physics(physics)
 				.launched(launched.getTime())
 				.starshipClass(StarshipClassEnum.GALAXY)
 				.affiliation(AffiliationEnum.STARFLEET).build();
 
 		Officer jeanLucPicard = officer().name("Jean-Luc Picard")
 				.rank(RankEnum.CAPTAIN).affiliation(AffiliationEnum.STARFLEET)
 				.homePlanet(PlanetEnum.EARTH).speciment(SpeciesEnum.HUMAN)
 				.build();
 		starship.addOfficer(jeanLucPicard);
 
 		Officer williamRiker = officer().name("William Riker")
 				.rank(RankEnum.COMMANDER)
 				.affiliation(AffiliationEnum.STARFLEET)
 				.homePlanet(PlanetEnum.EARTH).speciment(SpeciesEnum.HUMAN)
 				.build();
 		starship.addOfficer(williamRiker);
 
 		Officer data = officer().name("Data")
 				.rank(RankEnum.LIEUTENANT_COMMANDER)
 				.affiliation(AffiliationEnum.STARFLEET)
 				.homePlanet(PlanetEnum.OMICRON_THETA)
 				.speciment(SpeciesEnum.ANDROID).build();
 		starship.addOfficer(data);
 
 		Officer geordiLaForge = officer().name("Geordi La Forge")
 				.rank(RankEnum.LIEUTENANT)
 				.affiliation(AffiliationEnum.STARFLEET)
 				.homePlanet(PlanetEnum.EARTH).speciment(SpeciesEnum.HUMAN)
 				.build();
 		starship.addOfficer(geordiLaForge);
 
 		Officer worf = officer().name("Worf").rank(RankEnum.LIEUTENANT)
 				.affiliation(AffiliationEnum.STARFLEET)
 				.homePlanet(PlanetEnum.QONOS).speciment(SpeciesEnum.KLINGON)
 				.build();
 		starship.addOfficer(worf);
 
 		Officer beverlyCrusher = officer().name("Beverly Crusher")
 				.rank(RankEnum.COMMANDER)
 				.affiliation(AffiliationEnum.STARFLEET)
 				.homePlanet(PlanetEnum.EARTH).speciment(SpeciesEnum.HUMAN)
 				.build();
 		starship.addOfficer(beverlyCrusher);
 
		Officer deannaTroi = officer().name("DeannaTroi")
 				.rank(RankEnum.COMMANDER)
 				.affiliation(AffiliationEnum.STARFLEET)
 				.homePlanet(PlanetEnum.BETAZED).speciment(SpeciesEnum.BETAZOID)
 				.build();
 		starship.addOfficer(deannaTroi);
 
 		entityManager.persist(starship);
 
 		transaction.commit();
 		entityManager.close();
 		return starship;
 	}
 
 	private void findStarship(Starship starship) {
 		
 		EntityManager entityManager = this.entityManagerFactory.createEntityManager();
 		EntityTransaction transaction = entityManager.getTransaction();
 		transaction.begin();
 		System.out.println("Before Find");
 		Starship newStarship = entityManager.find(Starship.class, starship.getId());
 		System.out.println("After Find Before Commit");
 		transaction.commit();
 		System.out.println("After commit");
 		entityManager.close();
 
 	}
 }
