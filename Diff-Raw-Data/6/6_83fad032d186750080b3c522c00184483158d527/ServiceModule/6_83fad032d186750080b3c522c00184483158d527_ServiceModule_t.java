 package ch.hslu.appe.fs1301.business;
 
 import ch.hslu.appe.fs1301.business.shared.iPersonAPI;
 import ch.hslu.appe.fs1301.business.shared.iProductAPI;
 import ch.hslu.appe.fs1301.business.shared.iSessionAPI;
 
 import com.google.inject.AbstractModule;
 import com.google.inject.Singleton;
 /**
  * @author Thomas Bomatter
  * Guice binding module for injections
  */
 public class ServiceModule extends AbstractModule {
 
 	@Override
 	protected void configure() {
		bind(SessionAPI.class).in(Singleton.class);
		bind(iSessionAPI.class).to(SessionAPI.class);
		bind(iInternalSessionAPI.class).to(SessionAPI.class);
		
 		bind(iPersonAPI.class).to(PersonAPI.class).in(Singleton.class);
 		bind(iProductAPI.class).to(ProductAPI.class).in(Singleton.class);
 	}
 }
