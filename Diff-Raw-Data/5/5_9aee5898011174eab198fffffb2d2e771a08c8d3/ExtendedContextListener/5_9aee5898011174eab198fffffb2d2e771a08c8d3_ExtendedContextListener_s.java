 /*
  * LICENSE
  *
  * "THE BEER-WARE LICENSE" (Revision 43):
  * "Sven Strittmatter" <weltraumschaf(at)googlemail(dot)com> wrote this file.
  * As long as you retain this notice you can do whatever you want with
  * this stuff. If we meet some day, and you think this stuff is worth it,
  * you can buy me a non alcohol-free beer in return.
  *
  * Copyright (C) 2012 "Sven Strittmatter" <weltraumschaf(at)googlemail(dot)com>
  */
 package org.lafayette.server.web.servlet;
 
 import com.google.inject.Guice;
 import com.google.inject.Injector;
 import com.google.inject.servlet.GuiceServletContextListener;
 import java.io.File;
 import org.lafayette.server.core.log.Log;
 import java.security.NoSuchAlgorithmException;
 import javax.naming.InitialContext;
 import javax.naming.NamingException;
 import javax.servlet.ServletContext;
 import javax.servlet.ServletContextEvent;
 import javax.servlet.ServletContextListener;
 import javax.sql.DataSource;
 import org.apache.commons.lang3.Validate;
 import org.lafayette.server.domain.db.NullDataSource;
 import org.lafayette.server.core.log.Logger;
 import org.lafayette.server.domain.Finders;
 import org.lafayette.server.domain.mapper.Mappers;
 import org.lafayette.server.web.InitialServletParameters;
 import org.lafayette.server.web.ServerModule;
 import org.lafayette.server.web.Services;
 import org.mapdb.DB;
 import org.mapdb.DBMaker;
 
 /**
  * Implements a servlet context listener.
  *
  * @author Sven Strittmatter <weltraumschaf@googlemail.com>
  */
 public final class ExtendedContextListener extends GuiceServletContextListener implements ServletContextListener {
 
     /**
      * Logging facility.
      */
     private static final Logger LOG = Log.getLogger(ExtendedContextListener.class);
     /**
      * JNDI name for main data base data source.
      */
     private static final String JNDI_NAME_DATA_SOURCE = "java:/comp/env/jdbc/mysql";
     /**
      * TODO Make file configurable.
      */
     private static final String MAPDB_LOCATION = "/var/lafayette/mapdb/database";
     /**
      * Extended listener.
      */
     private final BaseContextListener base = new BaseContextListener();
 
     /**
      * Dedicated constructor.
      *
     * @throws NoSuchAlgorithmException if registry can't initialize {@link BaseRegistry#nonceGenerator}
      */
     public ExtendedContextListener() throws NoSuchAlgorithmException {
         super();
     }
 
     @Override
     protected Injector getInjector() {
         // http://code.google.com/p/google-guice/wiki/ServletModule
         LOG.debug("Create Guice injector.");
         final Injector injector = Guice.createInjector(new ServerModule());
         base.getRegistry().setDependnecyInjector(injector);
         return injector;
     }
 
     @Override
     public void contextInitialized(final ServletContextEvent sce) {
         base.contextInitialized(sce);
         LOG.info("Context initialized. Execute extended listener...");
         final DataSource dataSource = createDataSource();
         final Mappers mappers = createMappersFactory(dataSource);
         createInitialServletParameters(sce);
         final DB mapDb = createMapDatabase();
         createServices(mappers, mapDb);
         LOG.debug("Extended listener executed.");
     }
 
     @Override
     public void contextDestroyed(final ServletContextEvent sce) {
         base.contextDestroyed(sce);
         LOG.info("Context destroyed. Execute extended listener...");
         closeMapDatabase();
         LOG.debug("Extended listener executed.");
     }
 
     /**
      * Create {@link Mappers mapper factory} and stores to registry.
      *
      * @param dataSource open database connection
      * @return never {@code null}
      */
     private Mappers createMappersFactory(final DataSource dataSource) {
         final Mappers mappers = new Mappers(dataSource);
         LOG.debug("Create data mappers.");
         base.getRegistry().setMappers(mappers);
         return mappers;
     }
 
     /**
     * Creates data source provided by container via JNDI and sets to {@link BaseRegistry}.
      *
      * @return created data source
      */
     private DataSource createDataSource() {
         try {
             LOG.debug("Create data source.");
             final InitialContext initialContext = new InitialContext();
             final DataSource dataSource = (DataSource) initialContext.lookup(JNDI_NAME_DATA_SOURCE);
 
             if (null == dataSource) {
                 LOG.error("Can't lookup data source via JNDI!");
             } else {
                 base.getRegistry().setDataSource(dataSource);
                 return dataSource;
             }
         } catch (NamingException ex) {
             LOG.fatal(ex.getMessage());
         }
 
         return new NullDataSource();
     }
 
     /**
      * Create and set initial servelt parameters.
      *
      * @param sce must not be {@code null}
      */
     private void createInitialServletParameters(final ServletContextEvent sce) {
         Validate.notNull(sce, "Servlet context event must not be null!");
         LOG.debug("Create initial servlet parameter.");
         final ServletContext servletContext = sce.getServletContext();
         base.getRegistry().setInitParameters(new InitialServletParameters(servletContext));
     }
 
     /**
      * Create and set map database.
      *
      * @return created map database, never {@code null}
      */
     private DB createMapDatabase() {
         LOG.debug("Create map database.");
         final DB db = DBMaker.newFileDB(new File(MAPDB_LOCATION))
                 .closeOnJvmShutdown()
                 .make();
         base.getRegistry().setMapDatabase(db);
         return db;
     }
 
     /**
      * Create and set service provider.
      *
      * @param mappers must not be {@code null}
      * @param mapDatabase must not be {@code null}
      */
     private void createServices(final Mappers mappers, final DB mapDatabase) {
         Validate.notNull(mappers, "Mappers must not be null!");
         Validate.notNull(mapDatabase, "Mapp database must not be null!");
         LOG.debug("Create service provider.");
         base.getRegistry().setServices(new Services(new Finders(mappers), mapDatabase));
     }
 
     /**
      * Close map database.
      */
     private void closeMapDatabase() {
         final DB mapdb = base.getRegistry().getMapDatabase();
         LOG.debug("Commit map database.");
         mapdb.commit();
 
         if (shouldCompactMapDb()) {
             LOG.debug("Compact map database.");
             mapdb.compact();
         }
 
         LOG.debug("Close map database.");
         mapdb.close();
     }
 
     /**
      * Determines if map database should be compacted.
      *
      * @return {@code true} if compact should be performed, else {@code false}
      */
     private boolean shouldCompactMapDb() {
         // TODO Return true from time to time.
         return false;
     }
 
 }
