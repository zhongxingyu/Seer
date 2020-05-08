 /*
 * JBoss, Home of Professional Open Source.
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors. 
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
 package org.jboss.aop.microcontainer.beans;
 
 import org.jboss.aop.AspectManager;
 import org.jboss.aop.Domain;
 import org.jboss.metadata.plugins.scope.ApplicationScope;
 import org.jboss.metadata.plugins.scope.DeploymentScope;
 import org.jboss.metadata.spi.MetaData;
 import org.jboss.metadata.spi.stack.MetaDataStack;
 import org.jboss.util.JBossStringBuilder;
 
 /**
  * 
  * @author <a href="kabir.khan@jboss.com">Kabir Khan</a>
  * @version $Revision: 1.1 $
  */
 public class AspectManagerFactory
 {
    public static AspectManager getAspectManager()
    {
       return getAspectManager(MetaDataStack.peek());
    }
    
    public static AspectManager getAspectManager(MetaData md)
    {
       AspectManager manager = AspectManager.instance();
       if (md != null)
       {
          ApplicationScope app = md.getMetaData(ApplicationScope.class);
          DeploymentScope dep = md.getMetaData(DeploymentScope.class);
          if (app != null && dep != null)
          {
             JBossStringBuilder fqn = new JBossStringBuilder("/");
             AspectManager sub = null;
             if (app != null)
             {
                String name="APPLICATION=" + app.value();
                fqn.append(name);
                fqn.append("/");
                sub = manager.findManagerByName(fqn.toString());
                if (sub == null)
                {
                   sub = createNewDomain(manager, name);
                }
             }
             
             if (dep != null)
             {
                String name="DEPLOYMENT=" + dep.value();
                fqn.append(name);
                fqn.append("/");
                AspectManager parent = sub;
                sub = manager.findManagerByName(fqn.toString());
                if (sub == null)
                {
                   sub = createNewDomain(parent, name);
                }
             }
             return sub;
          }
       }
       return manager;
    }
    
    private static AspectManager createNewDomain(AspectManager parent, String name)
    {
       Domain domain = new Domain(parent, name, true);
       domain.setInheritsBindings(true);
       domain.setInheritsDeclarations(true);
       return domain;
    }
 }
 
