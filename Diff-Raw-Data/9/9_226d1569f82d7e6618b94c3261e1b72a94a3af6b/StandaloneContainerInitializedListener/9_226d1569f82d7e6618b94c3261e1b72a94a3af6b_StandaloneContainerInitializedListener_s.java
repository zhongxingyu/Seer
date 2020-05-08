 /*
  * Copyright (C) 2009 eXo Platform SAS.
  *
  * This is free software; you can redistribute it and/or modify it
  * under the terms of the GNU Lesser General Public License as
  * published by the Free Software Foundation; either version 2.1 of
  * the License, or (at your option) any later version.
  *
  * This software is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this software; if not, write to the Free
  * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
  * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
  */
 package org.exoplatform.ws.frameworks.servlet;
 
 import org.exoplatform.container.StandaloneContainer;
 import org.exoplatform.services.log.ExoLogger;
 import org.exoplatform.services.log.Log;
 
 import java.net.MalformedURLException;
 
 import javax.servlet.ServletContextEvent;
 import javax.servlet.ServletContextListener;
 
 /**
  * Created by The eXo Platform SAS .<br/> Servlet context initializer that
  * initializes standalone container at the context startup time. To activate
  * this your web.xml have to be configured like:<br/>
  * &lt;listener&gt;<br/>
  * &lt;listener-class&gt;org.exoplatform.frameworks.web.common.StandaloneContainerInitializedListener&lt;/listener-class&gt;<br/>
  * &lt;/listener&gt;<br/>
  * You may also specify an URL to the
  * configuration.xml stored the configuration for StandaloneContainer as
  * servlet's init parameter called 'org.exoplatform.container.standalone.config'
  * 
  * @author <a href="mailto:gennady.azarenkov@exoplatform.com">Gennady
  *         Azarenkov</a>
  * @version $Id: StandaloneContainerInitializedListener.java 6739 2006-07-04
  *          14:34:49Z gavrikvetal $
  */
 
 public class StandaloneContainerInitializedListener implements ServletContextListener
 {
 
    private static final Log LOG = ExoLogger.getLogger("exo.ws.frameworks.servlet.StandaloneContainerInitializedListener");
 
    private static final String CONF_URL_PARAMETER = "org.exoplatform.container.standalone.config";
 
    private static final String PREFIX_WAR = "war:";
 
    /**
     * Container.
     */
    private StandaloneContainer container;
 
    /**
     * {@inheritDoc}
     */
    public void contextInitialized(ServletContextEvent event)
    {
       String configurationURL = event.getServletContext().getInitParameter(CONF_URL_PARAMETER);
 
       try
       {
          if (configurationURL != null && configurationURL.startsWith(PREFIX_WAR))
          {
             configurationURL =
                event.getServletContext().getResource(configurationURL.substring(PREFIX_WAR.length())).toExternalForm();
          }
       }
       catch (MalformedURLException e)
       {
          LOG.error("Error of configurationURL read", e);
       }
 
       //  If no configuration in web.xml check system property.
       if (configurationURL == null)
          configurationURL = System.getProperty(CONF_URL_PARAMETER);
 
       try
       {
          StandaloneContainer.addConfigurationURL(configurationURL);
       }
       catch (MalformedURLException e)
       {
          // Try to use path, we do not need have full path (file:/path/conf) to configuration. Any relative path is OK.
          try
          {
             StandaloneContainer.addConfigurationPath(configurationURL);
          }
          catch (MalformedURLException e2)
          {
             LOG.error("Error of addConfiguration", e2);
          }
       }
 
       try
       {
          container = StandaloneContainer.getInstance(Thread.currentThread().getContextClassLoader());
          event.getServletContext().setAttribute("org.exoplatform.frameworks.web.eXoContainer", container);
       }
       catch (Exception e)
       {
          LOG.error("Error of StandaloneContainer initialization", e);
       }
 
    }
 
    /**
     * {@inheritDoc}
     */
    public void contextDestroyed(ServletContextEvent event)
    {
      container.stop();
    }
 }
