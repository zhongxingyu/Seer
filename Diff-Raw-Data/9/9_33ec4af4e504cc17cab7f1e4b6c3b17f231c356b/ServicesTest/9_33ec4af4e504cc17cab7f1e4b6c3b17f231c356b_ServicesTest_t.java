 package org.acme;
 
 import static java.util.Collections.*;
 import static org.acme.TestMocks.*;
 import static org.acme.TestUtils.*;
 import static org.junit.Assert.*;
 
 import org.junit.Test;
 import org.virtualrepository.RepositoryService;
 import org.virtualrepository.impl.Services;
 
 public class ServicesTest {
 	
 
 	@Test
 	public void addRepository() {
 		
 
 		RepositoryService service = aService().get();
 		
 		Services services = new Services(service);
 		
 		assertTrue(services.contains(service.name()));
 		
 		assertEqualElements(asList(services),singleton(service));
 	}
 	
 	@Test
 	public void loadRepository() {
 		
 		Services repos = new Services();
 		
 		repos.load();
 		
		assertTrue(repos.size()==0);
 	}
 	
 	
 	@Test
 	public void repositoriesMustBeUniquelyNamed() {
 		
 		Services services = new Services(aService().name("test").get());
 		
 		assertEquals(1,asList(services).size());
 		
 		services.add(aService().name("test").get());
 		
 		assertEquals(1,asList(services).size());
 	}
 	
 }
