 /*
  * Copyright (c) 2004 UNINETT FAS
  *
  * This program is free software; you can redistribute it and/or modify it
  * under the terms of the GNU General Public License as published by the Free
  * Software Foundation; either version 2 of the License, or (at your option)
  * any later version.
  *
  * This program is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
  * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
  * more details.
  *
  * You should have received a copy of the GNU General Public License along with
  * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
  * Place - Suite 330, Boston, MA 02111-1307, USA.
  *
  */
 
 package no.feide.moria.directory;
 
 import java.lang.reflect.Constructor;
 import java.util.HashMap;
 import java.util.Properties;
 import java.util.Timer;
 
 import no.feide.moria.directory.backend.AuthenticationFailedException;
 import no.feide.moria.directory.backend.BackendException;
 import no.feide.moria.directory.backend.DirectoryManagerBackend;
 import no.feide.moria.directory.backend.DirectoryManagerBackendFactory;
 import no.feide.moria.directory.index.DirectoryManagerIndex;
 import no.feide.moria.directory.index.IndexedReference;
 import no.feide.moria.log.MessageLogger;
 
 /**
  * The Directory Manager (sometimes referred to as DM) component in Moria 2.
  * Responsible for all backend operations, such as user authentication and
  * attribute retrieval from the backend sources.
  */
 public class DirectoryManager {
 
     /** The message logger. */
     private final MessageLogger log = new MessageLogger(DirectoryManager.class);
 
     /** Internal representation of the index. */
     private DirectoryManagerIndex index = null;
 
     /**
      * This timer uses <code>IndexUpdater</code> to periodically call
      * <code>updateIndex()</code>.
      */
     private Timer indexTimer = null;
 
     /** Internal representation of the backend factory. */
     private DirectoryManagerBackendFactory backendFactory = null;
 
     /** The currently used (valid) Directory Manager configuration. */
     private DirectoryManagerConfiguration configuration = null;
 
 
     /**
      * Destructor. Will call <code>stop()</code>.
      * @see DirectoryManager#stop()
      */
     public final void destroy() {
 
         stop();
 
     }
 
 
     /**
      * Sets or updates the Directory Manager's configuration. The first time
      * this
      * method is used, it will force an initial index update by reading the
      * index
      * through <code>IndexUpdater.readIndex()</code>.
      * @param config
      *            The configuration. The actual parsing is done by the
      *            <code>DirectoryManagerConfiguration</code> constructor.
      * @throws DirectoryManagerConfigurationException
      *             If unable create a new configuration object from config
      *             or config is null, and also not able to fall back to a
      *             previous working configuration to
      *             fall back to (in which case a warning will be logged
      *             instead). Also thrown if unable to resolve the backend
      *             factory class (as specified in the configuration file) or if
      *             unable to instantiate this class.
      * @see DirectoryManagerConfiguration#DirectoryManagerConfiguration(Properties)
      * @see IndexUpdater#readIndex()
      */
     public final void setConfig(final Properties config) {
 
         // Update current configuration.
         try {
 
             final DirectoryManagerConfiguration newConfiguration = new DirectoryManagerConfiguration(config);
             configuration = newConfiguration;
 
         } catch (Exception e) {
 
             // Something happened while updating the configuration; can we
             // recover?
             if (configuration == null) {
 
                 // Critical error; we don't have a working configuration.
                 throw new DirectoryManagerConfigurationException("Unable to set initial configuration", e);
 
             } else {
 
                 // Non-critical error; we still have a working configuration.
                 log.logWarn("Unable to update existing configuration", e);
 
             }
 
         }
 
         // Update the index and (re-)start the index update timer.
         IndexUpdater indexUpdater = new IndexUpdater(this, configuration.getIndexFilename());
         if (indexTimer == null) {
 
             // The first time we set the configuration we manually force an
             // index update to ensure we have a working index.
             indexTimer = new Timer(true); // Daemon.
             updateIndex(indexUpdater.readIndex());
 
         }
         indexTimer.scheduleAtFixedRate(indexUpdater, configuration.getIndexUpdateFrequency(), configuration.getIndexUpdateFrequency());
 
         // Set the backend factory class and set its configuration.
         Constructor constructor = null;
         try {
 
             constructor = configuration.getBackendFactoryClass().getConstructor(null);
             backendFactory = (DirectoryManagerBackendFactory) constructor.newInstance(null);
 
         } catch (NoSuchMethodException e) {
             log.logCritical("Cannot find backend factory constructor", e);
             throw new DirectoryManagerConfigurationException("Cannot find backend factory constructor", e);
         } catch (Exception e) {
             log.logCritical("Unable to instantiate backend factory object", e);
             throw new DirectoryManagerConfigurationException("Unable to instantiate backend factory object", e);
         }
 
         // Set backend configuration.
         backendFactory.setConfig(configuration.getBackendElement());
 
     }
 
 
     /**
      * Sets or updates the internal index structure. Used by
      * <code>IndexUpdater.run()</code> to periodically update the index.
      * @param newIndex
      *            The new index object. A <code>null</code> value is taken to
      *            indicate that the index should <em>not</em> be updated.
      * @throws DirectoryManagerConfigurationException
      *             If <code>newIndex</code> is <code>null</code> and the
      *             index has not been previously set.
      * @see IndexUpdater#run()
      */
     protected final synchronized void updateIndex(final DirectoryManagerIndex newIndex) {
 
         // Sanity check.
         if ((newIndex == null) && (index == null))
             throw new DirectoryManagerConfigurationException("Unable to initialize index; aborting");
 
         // Update existing index.
         if (newIndex != null)
             index = newIndex;
 
     }
 
 
     /**
      * Checks that a user actually exists by querying the underlying backend.
      * @param sessionTicket
      *            Passed on to instances of <code>DirectoryManagerBackend</code>,
      *            for logging purposes. May be <code>null</code> or an empty
      *            string.
      * @param username
      *            The username to look up.
      * @return <code>true</code> if the user element corresponding to the
      *         username actually exists, otherwise <code>false</code>.
      * @throws BackendException
      *             A subclass of <code>BackendException</code> is thrown if
      *             there was a problem accessing the backend.
      * @throws IllegalStateException
      *             If attempting to use this method without successfully using
      *             <code>setConfig(Properties)</code> first.
      * @see DirectoryManagerBackend#userExists(String)
      */
     public final boolean userExists(final String sessionTicket, final String username)
     throws BackendException {
 
         // Sanity check.
         if (configuration == null)
             throw new IllegalStateException("Configuration not set");
 
         // Do the call through a temporary backend instance.
         DirectoryManagerBackend backend = backendFactory.createBackend(sessionTicket);
         IndexedReference[] references = index.getReferences(username);
        try {
            if (references != null) {
    
                // Found at least one reference.
                backend.open(references);
    
            } else {
    
                // Could not find the user.
                return false;
    
            }
    
            // Check that the user actually exists.
            return backend.userExists(username);
        } finally {
            
            // Close the backend.
            backend.close();
            
         }
     }
 
 
     /**
      * Forwards an authentication attempt to the underlying backend.
      * @param sessionTicket
      *            Passed on to instances of <code>DirectoryManagerBackend</code>,
      *            for logging purposes. May be <code>null</code> or an empty
      *            string.
      * @param userCredentials
      *            The user credentials passed on for authentication.
      * @param attributeRequest
      *            An array containing the attribute names requested for
      *            retrieval after successful authentication.
      * @return The user attributes matching the attribute request, if those were
      *         available. The keys will be <code>String</code> objects, while
      *         the values will be <code>String</code> arrays containing one or
      *         more attribute values. Note that if any of the requested
      *         attributes could not be retrieved from the backend following a
      *         successful authentication (for example, if they simply do not
      *         exist in the backend in question), the <code>HashMap</code>
      *         will still include those attributes that <em>could</em> be
      *         retrieved. If no attributes were requested, or if no attributes
      *         were retrievable from the backend, an empty <code>HashMap</code>
      *         will be returned. This still indicates a successful
      *         authentication.
      * @throws BackendException
      *             A subclass of <code>BackendException</code> is thrown if
      *             there was a problem accessing the backend.
      * @throws AuthenticationFailedException
      *             If we managed to access the backend, and the authentication
      *             failed. In other words, the user credentials are incorrect.
      *             Also thrown if the user credentials are <code>null</code>.
      * @throws IllegalStateException
      *             If attempting to use this method without successfully using
      *             <code>setConfig(Properties)</code> first.
      * @see #setConfig(Properties)
      * @see DirectoryManagerBackend#authenticate(Credentials, String[])
      */
     public final HashMap authenticate(final String sessionTicket, final Credentials userCredentials, final String[] attributeRequest)
     throws AuthenticationFailedException, BackendException,
     IllegalStateException {
 
         // Sanity checks.
         if (configuration == null)
             throw new IllegalStateException("Configuration not set");
         if (index == null)
             throw new IllegalStateException("Index has not been initialized");
         if (userCredentials == null)
             throw new AuthenticationFailedException("User credentials cannot be NULL");
 
         // Do the call through a temporary backend instance.
         DirectoryManagerBackend backend = backendFactory.createBackend(sessionTicket);
         IndexedReference[] references = index.getReferences(userCredentials.getUsername());
         if (references != null) {
 
             // Found at least one reference.
             backend.open(references);
 
         } else {
 
             // Could not locate the user in the index.
             throw new AuthenticationFailedException("User " + userCredentials.getUsername() + " is unknown");
 
         }
 
         // Authenticate the user.
         HashMap attributes = backend.authenticate(userCredentials, attributeRequest);
 
         // Close the backend and return any attributes.
         backend.close();
         return attributes;
 
     }
 
 
     /**
      * Resolves the realm of a given username. Even for usernames on the form
      * <i>user@realm </i> this method should be used, since it is possible to
      * retain such a username even when changing one's realm or home
      * organization.
      * @param username
      *            The username to check.
      * @return The realm, or <code>null</code> if no such realm can be
      *         resolved.
      * @see DirectoryManagerIndex#getRealm(String)
      */
     public final String getRealm(final String username) {
 
         return index.getRealm(username);
 
     }
 
 
     /**
      * Stops the Directory Manager. <br>
      * <br>
      * Will stop the index updater thread. Note that the Directory Manager may
      * be used after <code>stop()</code>, but this is discouraged.
      */
     public final void stop() {
 
         // Stop the index update timer, if it has been initialized.
         if (indexTimer != null)
             indexTimer.cancel();
 
     }
 
 }
