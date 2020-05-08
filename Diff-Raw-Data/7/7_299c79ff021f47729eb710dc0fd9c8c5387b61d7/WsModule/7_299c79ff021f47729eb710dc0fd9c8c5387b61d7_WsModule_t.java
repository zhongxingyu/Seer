 package de.behrfriedapp.webshop.server;
 
 import com.google.inject.AbstractModule;
 import com.google.inject.Singleton;
 import de.behrfriedapp.webshop.server.data.DefaultServerDataAccess;
 import de.behrfriedapp.webshop.server.data.ServerDataAccess;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * Server side module for configuring regular services.
  *
  * @author marcus
  *
  */
 public class WsModule extends AbstractModule {
 
 	/**
 	 * logger linked to any logging framework
 	 */
 	private final Logger logger = LoggerFactory.getLogger(WsModule.class);
 
 	/**
 	 * Binds all regular interfaces to a certain implementation.
 	 */
 	@Override
 	protected void configure() {
 		this.logger.info("Configuring dependencies");
 		this.bind(ServerDataAccess.class).to(DefaultServerDataAccess.class).in(Singleton.class);
 		this.logger.info("Cependencies configured");
 	}
 
 }
