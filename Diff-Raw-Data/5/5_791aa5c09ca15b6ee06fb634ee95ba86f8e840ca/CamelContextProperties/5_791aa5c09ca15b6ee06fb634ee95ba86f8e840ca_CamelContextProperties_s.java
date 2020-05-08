 package de.ctrlaltdel.cci;
 
 import javax.enterprise.inject.Produces;
 
 import org.apache.camel.CamelContext;
 import org.apache.camel.management.DefaultManagementAgent;
 import org.apache.camel.management.DefaultManagementLifecycleStrategy;
 import org.apache.camel.management.DefaultManagementNamingStrategy;
 import org.apache.camel.spi.CamelContextNameStrategy;
 import org.apache.camel.spi.ManagementNamingStrategy;
 import org.apache.camel.spi.ManagementStrategy;
 
 /**
  * CamelContextProperties
  * @author ds
  */
 public class CamelContextProperties  {
 
 	@Produces
 	public CamelContextNameStrategy produceCamelContextNameStrategy() {
 		return new CamelContextNameStrategy() {
 			@Override
 			public String getName() {
 				return TestCamelContext.CONTEX_NAME;
 			}
 			
 			@Override
 			public String getNextName() {
 				throw new UnsupportedOperationException();
 			}
 			
 			@Override
 			public boolean isFixedName() {
 				return true;
 			}
 		};
 	};
 	
 	
 	/*
 	 *  there is no simple way to change the object name of the camel context.
	 *  normally, the objectname contains the hostname - but if you manage a lot of host eg. with nagios is easier to have the same objectname on each host.
 	 *  
	 *  unfortunatly, there is some logic in the camelContext.getManagementStrategy method
 	 *  this is done in ManagementStrategyFactory.create and isn't called if we use 'setManagementStrategy' 
 	 *  therefore this ugly hack is required :-(
 	 */
 	@Produces
 	public ManagementStrategy produceManagementStrategy() {
 		return new org.apache.camel.management.ManagedManagementStrategy() {
 			 
 			@Override
 			public void setCamelContext(CamelContext camelContext) {
 				super.setCamelContext(camelContext);
 				camelContext.getLifecycleStrategies().add(0, new DefaultManagementLifecycleStrategy(camelContext));
 				setManagementAgent(new DefaultManagementAgent(camelContext));
 			}
 			
 			@Override
 			public ManagementNamingStrategy getManagementNamingStrategy() {
 				return new DefaultManagementNamingStrategy() {
 					@Override
 					protected String getContextId(String name) {
 						return name;
 					}
 				};
 			};
 		};
 	}
 		
 }
