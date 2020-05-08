 /*
 * JBoss, Home of Professional Open Source
 * Copyright 2005, JBoss Inc., and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
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
 package org.jboss.kernel.plugins.bootstrap;
 
 import org.jboss.kernel.Kernel;
 import org.jboss.kernel.KernelFactory;
 import org.jboss.kernel.plugins.AbstractKernelObject;
 import org.jboss.kernel.spi.config.KernelConfig;
 
 /**
  * Abstract Bootstrap of the kernel.
  * 
  * @author <a href="adrian@jboss.com">Adrian Brock</a>
  * @author <a href="mailto:les.hazlewood@jboss.org">Les A. Hazlewood</a>
  * @version $Revision$
  */
 public abstract class AbstractBootstrap extends AbstractKernelObject implements Runnable
 {
    /** The kernel configuration */
    protected KernelConfig config;
 
    /**
     * The kernel created by the bootstrap implementation during the
     * bootstrap process.
     */
    protected Kernel kernel;
 
    /**
     * Create a new abstract bootstrap
     */
    public AbstractBootstrap()
    {
    }
 
    /**
     * Get the kernel configuration
     * 
     * @return the kernel configuration
     */
    public KernelConfig getConfig()
    {
       Kernel.checkAccess();
       return config;
    }
 
    /**
     * Set the kernel configuration
     * 
     * @param config the kernel configuration
     */
    public void setConfig(KernelConfig config)
    {
       Kernel.checkConfigure();
       this.config = config;
    }
 
    /**
     * Returns the Kernel object created during the bootstrap process.
     * @return the kernel instance created during bootstrap.
     */
    public Kernel getKernel()
    {
       return this.kernel;
    }
 
    public void run()
    {
       try
       {
          bootstrap();
       }
       catch (RuntimeException e)
       {
          log.trace("RuntimeException during JBoss Kernel Bootstrap.", e);
          throw e;
       }
       catch (Exception e)
       {
          log.trace("Exception during JBoss Kernel Bootstrap.", e);
         throw new RuntimeException(e);
       }
       catch (Error e)
       {
          log.trace("Error during JBoss Kernel Bootstrap.", e);
          throw e;
       }
       catch (Throwable t)
       {
          log.trace("Error during JBoss Kernel Bootstrap.", t);
         throw new RuntimeException("Error during JBoss Kernel Bootstrap", t);
       }
    }
 
    /**
     * Bootstrap the kernel
     * 
     * @throws Throwable for any error
     */
    protected void bootstrap() throws Throwable
    {
       kernel = KernelFactory.newInstance(config);
    }
 }
