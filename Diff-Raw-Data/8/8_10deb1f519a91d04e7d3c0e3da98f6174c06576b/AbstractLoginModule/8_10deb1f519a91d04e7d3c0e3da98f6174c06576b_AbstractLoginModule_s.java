 /*
  * Copyright (C) 2003-2009 eXo Platform SAS.
  *
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Affero General Public License
  * as published by the Free Software Foundation; either version 3
  * of the License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, see<http://www.gnu.org/licenses/>.
  */
 package org.exoplatform.services.security.jaas;
 
 import org.exoplatform.container.ExoContainer;
 import org.exoplatform.container.ExoContainerContext;
 import org.exoplatform.container.PortalContainer;
 import org.exoplatform.container.RootContainer;
 import org.exoplatform.services.log.Log;
 
 import java.util.Map;
 
 import javax.security.auth.Subject;
 import javax.security.auth.callback.CallbackHandler;
 import javax.security.auth.spi.LoginModule;
 
 /**
  * This class is the root class of all the LoginModules that require an ExoContainer 
  * 
  * Created by The eXo Platform SAS
  * Author : Nicolas Filotto
  *          nicolas.filotto@exoplatform.com
  * 20 ao�t 2009  
  */
 public abstract class AbstractLoginModule implements LoginModule
 {
 
    /**
     * The name of the option to use in order to specify the name of the portal container
     */
    private static final String OPTION_PORTAL_CONTAINER_NAME = "portalContainerName";
 
    /**
     * The name of the option to use in order to specify the name of the realm
     */
    private static final String OPTION_REALM_NAME = "realmName";
 
    /**
     * The name of the portal container.
     */
    private String portalContainerName;
 
    /**
     * The name of the realm.
     */
    protected String realmName;
 
    /**
     * @see {@link Subject} .
     */
    protected Subject subject;
 
    /**
     * @see {@link CallbackHandler}
     */
    protected CallbackHandler callbackHandler;
 
    /**
     * Shared state.
     */
    @SuppressWarnings("unchecked")
    protected Map sharedState;
 
    /**
     * Shared state.
     */
    @SuppressWarnings("unchecked")
    protected Map options;
 
    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    public final void initialize(Subject subject, CallbackHandler callbackHandler, Map sharedState, Map options)
    {
       this.subject = subject;
       this.callbackHandler = callbackHandler;
       this.sharedState = sharedState;
       this.options = options;
       this.portalContainerName = getPortalContainerName(options);
       this.realmName = getRealmName(options);
       afterInitialize();
    }
 
    /**
     * Allows sub-classes to do something after the initialization 
     */
    protected void afterInitialize()
    {
    }
 
    /**
     * @return actual ExoContainer instance.
     */
    protected ExoContainer getContainer() throws Exception
    {
       // TODO set correct current container
       ExoContainer container = ExoContainerContext.getCurrentContainer();
       if (container instanceof RootContainer)
       {
          container = RootContainer.getInstance().getPortalContainer(portalContainerName);
       }
       return container;
    }
 
    @SuppressWarnings("unchecked")
    private String getPortalContainerName(Map options)
    {
       if (options != null)
       {
          String optionValue = (String)options.get(OPTION_PORTAL_CONTAINER_NAME);
          if (optionValue != null && optionValue.length() > 0)
          {
             if (getLogger().isDebugEnabled())
             {
                getLogger().debug("The " + this.getClass() + " will use the portal container " + optionValue);
             }
             return optionValue;
          }
       }
       return PortalContainer.DEFAULT_PORTAL_CONTAINER_NAME;
    }
 
    @SuppressWarnings("unchecked")
    private String getRealmName(Map options)
    {
       if (options != null)
       {
          String optionValue = (String)options.get(OPTION_REALM_NAME);
          if (optionValue != null && optionValue.length() > 0)
          {
             if (getLogger().isDebugEnabled())
             {
                getLogger().debug("The " + this.getClass() + " will use the realm " + optionValue);
             }
             return optionValue;
          }
       }
       return PortalContainer.DEFAULT_REALM_NAME;
    }
 
    /**
     * Returns the Logger corresponding to the Login module
     */
    protected abstract Log getLogger();
 }
