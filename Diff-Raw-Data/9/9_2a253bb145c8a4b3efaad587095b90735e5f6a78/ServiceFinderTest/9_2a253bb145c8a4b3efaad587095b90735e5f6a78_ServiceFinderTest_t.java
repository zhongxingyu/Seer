 package net.dataforte.doorkeeper;
 
 import static org.junit.Assert.assertEquals;
 
 import java.util.List;
 
 import net.dataforte.commons.resources.ServiceFinder;
 import net.dataforte.doorkeeper.account.provider.AccountProvider;
 import net.dataforte.doorkeeper.authenticator.Authenticator;
 
 import org.junit.Test;
 
 public class ServiceFinderTest {
 
 	@Test
 	public void testFindServices() {
 		List<Class<? extends Authenticator>> authenticators = ServiceFinder.findServices(Authenticator.class);
		assertEquals(9, authenticators.size());
 		
 		List<Class<? extends AccountProvider>> providers = ServiceFinder.findServices(AccountProvider.class);
 		assertEquals(3, providers.size());
 	}
 
 }
