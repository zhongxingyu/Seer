 package org.mosaic.server.transaction.impl;
 
 import com.google.common.io.CharStreams;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.UnsupportedEncodingException;
 import java.lang.reflect.Constructor;
 import java.lang.reflect.InvocationTargetException;
 import java.net.URL;
 import java.sql.Connection;
 import java.sql.Driver;
 import java.sql.DriverManager;
 import java.sql.SQLException;
 import java.util.Collection;
 import java.util.LinkedHashSet;
 import java.util.Map;
 import java.util.Properties;
 import java.util.concurrent.ConcurrentHashMap;
 import javax.annotation.PostConstruct;
 import javax.annotation.PreDestroy;
 import org.mosaic.lifecycle.ContextRef;
 import org.mosaic.logging.Logger;
 import org.mosaic.logging.LoggerFactory;
 import org.mosaic.osgi.util.BundleUtils;
 import org.osgi.framework.Bundle;
 import org.osgi.framework.BundleContext;
 import org.osgi.framework.BundleEvent;
 import org.osgi.framework.BundleListener;
 import org.springframework.stereotype.Component;
 
 import static java.lang.reflect.Modifier.isAbstract;
 
 /**
  * @author arik
  */
 @Component
 public class JdbcDriverRegistrar implements BundleListener {
 
     private static final Logger LOG = LoggerFactory.getLogger( JdbcDriverRegistrar.class );
 
     private BundleContext bundleContext;
 
     private final Map<Bundle, Collection<Driver>> driverClasses = new ConcurrentHashMap<>();
 
     @ContextRef
     public void setBundleContext( BundleContext bundleContext ) {
         this.bundleContext = bundleContext;
     }
 
     public Connection getConnection( String url,
                                      String user,
                                      String password,
                                      Properties info ) throws SQLException {
 
         if( info == null ) {
             info = new Properties();
         }
         if( user != null ) {
             info.put( "user", user );
         }
         if( password != null ) {
             info.put( "password", password );
         }
 
         // iterate through the drivers, searching for an appropriate one
         for( Collection<Driver> drivers : this.driverClasses.values() ) {
             for( Driver driver : drivers ) {
                 if( driver.acceptsURL( url ) ) {
                     LOG.trace( "Trying to obtain JDBC connection '{}' from driver '{}'", url, driver );
                     Connection connection = driver.connect( url, info );
                     if( connection != null ) {
                         LOG.trace( "Driver '{}' successfully connected using URL: {}", driver, url );
                         return connection;
                     }
                 }
             }
         }
 
         // none succeeded - fail
         throw new SQLException( "No JDBC driver accepts the URL '" + url + "'" );
     }
 
     @Override
     public synchronized void bundleChanged( BundleEvent event ) {
         if( event.getType() == BundleEvent.RESOLVED ) {
             scanForJdbcDrivers( event.getBundle() );
         } else if( event.getType() == BundleEvent.UNRESOLVED ) {
             removeJdbcDrivers( event.getBundle() );
         }
     }
 
     @PostConstruct
     public synchronized void init() {
         this.bundleContext.addBundleListener( this );
         Bundle[] bundles = this.bundleContext.getBundles();
         if( bundles != null ) {
             for( Bundle bundle : bundles ) {
                if( bundle.getState() == Bundle.ACTIVE || bundle.getState() == Bundle.RESOLVED ) {
                    scanForJdbcDrivers( bundle );
                }
             }
         }
     }
 
     @PreDestroy
     public void destroy() {
         this.bundleContext.removeBundleListener( this );
     }
 
     private void scanForJdbcDrivers( Bundle bundle ) {
         Collection<Driver> jdbcDriverClasses = new LinkedHashSet<>();
 
         URL driverServiceUrl = bundle.getEntry( "/META-INF/services/java.sql.Driver" );
         if( driverServiceUrl != null ) {
             try( InputStreamReader in = new InputStreamReader( driverServiceUrl.openStream(), "UTF-8" ) ) {
                 for( String className : CharStreams.readLines( in ) ) {
                     try {
 
                         Class<?> cls = bundle.loadClass( className );
                         if( !isAbstract( cls.getModifiers() ) && !cls.isInterface() ) {
 
                             if( Driver.class.isAssignableFrom( cls ) ) {
                                 Class<? extends Driver> driverClass = cls.asSubclass( Driver.class );
                                 Constructor<? extends Driver> defaultConstructor = driverClass.getConstructor();
                                 Driver driver = defaultConstructor.newInstance();
 
                                 DriverManager.registerDriver( driver );
                                 jdbcDriverClasses.add( driver );
                                 LOG.info( "Registered JDBC driver '{}' from bundle '{}'", driver, BundleUtils.toString( bundle ) );
                             }
 
                         }
 
                     } catch( ClassNotFoundException | InvocationTargetException | InstantiationException | IllegalAccessException | SQLException e ) {
                         LOG.warn( "Could not scan JDBC drivers in bundle '{}': {}", BundleUtils.toString( bundle ), e.getMessage(), e );
 
                     } catch( NoSuchMethodException e ) {
                         // ignore classes without a default constructor
                     }
                 }
 
             } catch( UnsupportedEncodingException e ) {
                 LOG.warn( "UTF-8 is not supported in this JVM: {}", e.getMessage(), e );
 
             } catch( IOException e ) {
                 LOG.warn( "Could not scan JDBC drivers in bundle '{}': {}", BundleUtils.toString( bundle ), e.getMessage(), e );
             }
         }
 
         if( !jdbcDriverClasses.isEmpty() ) {
             this.driverClasses.put( bundle, jdbcDriverClasses );
         }
     }
 
     private void removeJdbcDrivers( Bundle bundle ) {
         Collection<Driver> drivers = this.driverClasses.remove( bundle );
         if( drivers != null ) {
             for( Driver driver : drivers ) {
                 try {
                     DriverManager.deregisterDriver( driver );
                 } catch( SQLException e ) {
                     LOG.warn( "Could not unregister JDBC driver '{}': {}", driver, e.getMessage(), e );
                 }
             }
         }
     }
 }
