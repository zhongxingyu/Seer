 package com.kevinchard.phonebooth;
 
 import static org.junit.Assert.assertNotNull;
 
 import org.junit.Test;
 import org.neo4j.test.ImpermanentGraphDatabase;
 
 import com.kevinchard.phonebooth.EntityManagerFactory;
 import com.kevinchard.phonebooth.Persistence;
 
 public class PersistenceTest {
 
 	
 	@Test(expected = RuntimeException.class)
 	public void testCreateEntityManagerFactoryWithNoGraphDatabaseService() {
		Persistence.create("com.kevinchard.neo.impl.NeoEntityManagerFactory").build();
 	}
 	
 	@Test(expected = RuntimeException.class)
 	public void testCreateEntityManagerFactoryWithNoEntityManagerFactoryClass() {
 		Persistence.create(null).withGraphDatabaseService(new ImpermanentGraphDatabase()).build();
 	}
 	
 	@Test
 	public void testDefaultCreateEntityManagerFactory() {
 		
		EntityManagerFactory emf = Persistence.create("com.kevinchard.neo.impl.NeoEntityManagerFactory")
 											  .withGraphDatabaseService(new ImpermanentGraphDatabase())
 											  .build();
 		
 		assertNotNull(emf);
 	}
 }
