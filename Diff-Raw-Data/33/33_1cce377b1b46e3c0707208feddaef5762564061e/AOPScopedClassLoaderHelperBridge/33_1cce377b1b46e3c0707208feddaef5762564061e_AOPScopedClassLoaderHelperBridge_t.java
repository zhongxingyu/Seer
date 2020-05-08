 /*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, JBoss Inc., and individual contributors as indicated
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
 package org.jboss.aop.classpool;
 
 import java.util.Map;
 import java.util.WeakHashMap;
 
 import org.jboss.aop.AspectManager;
 import org.jboss.aop.Domain;
 import org.jboss.logging.Logger;
 
 /**
  * AOPScopedClassLoaderHelperBridge.<p>
  * 
  * A bridged from the old scoped classloader helper to the temporary
  * AOPClassLoaderScopingPolicy
  * 
  * @author <a href="adrian@jboss.com">Adrian Brock</a>
  * @version $Revision: 1.1 $
  */
 public class AOPScopedClassLoaderHelperBridge implements AOPClassLoaderScopingPolicy
 {
    /** The log */
    private static final Logger log = Logger.getLogger(AOPScopedClassLoaderHelper.class);
 
    /** The delegate */
    private AOPScopedClassLoaderHelper delegate;
    
    /** A map of domains by loader repository, maintaned by the top level AspectManager */
    private Map<Object, Domain> scopedClassLoaderDomains = new WeakHashMap<Object, Domain>();
 
    /**
     * Create a new AOPScopedClassLoaderHelperBridge.
     * 
     * @param delegate the delegate helper
     * @throws IllegalArgumentException for a null delegate
     */
    public AOPScopedClassLoaderHelperBridge(AOPScopedClassLoaderHelper delegate)
    {
       if (delegate == null)
          throw new IllegalArgumentException("Null delegate");
       this.delegate = delegate;
    }
    
    public boolean isScoped(ClassLoader classLoader)
    {
       return delegate.ifScopedDeploymentGetScopedParentUclForCL(classLoader) != null;
    }
 
    public synchronized Domain getDomain(ClassLoader classLoader, AspectManager parent)
    {
       ClassLoader scopedClassLoader = delegate.ifScopedDeploymentGetScopedParentUclForCL(classLoader);
       if (scopedClassLoader != null)
       {
          Domain scopedManager = null;
          synchronized (AOPClassPoolRepository.getInstance().getRegisteredCLs())
          {
             Object loaderRepository = delegate.getLoaderRepository(classLoader);
             scopedManager = scopedClassLoaderDomains.get(loaderRepository);
            
            // FIXME: JBAOP-107 REMOVE THIS HACK
            if (scopedManager != null && scopedManager.isValid() == false)
            {
               scopedClassLoaderDomains.remove(loaderRepository);
               scopedManager = null;
            }
            
             if (scopedManager == null)
             {
                scopedManager = delegate.getScopedClassLoaderDomain(scopedClassLoader, parent);
                log.debug("Created domain " + scopedManager + " for scoped deployment on: " +
                         classLoader + "; identifying scoped ucl: " + scopedClassLoader);
                scopedManager.setInheritsBindings(true);
                scopedManager.setInheritsDeclarations(true);
                
                scopedClassLoaderDomains.put(loaderRepository, scopedManager);
             }
             return scopedManager;
          }
       }
       return null;
    }
 
    public Domain getTopLevelDomain(AspectManager parent)
    {
       ClassLoader classLoader = delegate.getTopLevelJBossClassLoader();
       return getDomain(classLoader, parent);
    }
 }
