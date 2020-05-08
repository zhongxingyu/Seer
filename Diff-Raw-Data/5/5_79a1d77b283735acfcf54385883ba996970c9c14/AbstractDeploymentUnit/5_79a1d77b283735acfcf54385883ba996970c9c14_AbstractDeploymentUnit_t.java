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
 package org.jboss.deployers.plugins.deployer;
 
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.jboss.deployers.plugins.attachments.AbstractAttachments;
 import org.jboss.deployers.plugins.structure.ComponentDeploymentContext;
 import org.jboss.deployers.spi.DeploymentException;
 import org.jboss.deployers.spi.attachments.Attachments;
 import org.jboss.deployers.spi.classloader.ClassLoaderFactory;
 import org.jboss.deployers.spi.deployer.DeploymentUnit;
 import org.jboss.deployers.spi.structure.DeploymentContext;
 import org.jboss.virtual.VirtualFile;
 
 /**
  * AbstractDeploymentUnit.<p>
  * 
  * This is just a wrapper to the deployment context that
  * restricts people from "poking" behind the scenes.
  * 
  * @author <a href="adrian@jboss.com">Adrian Brock</a>
  * @version $Revision: 1.1 $
  */
 public class AbstractDeploymentUnit extends AbstractAttachments implements DeploymentUnit
 {
    /** The deployment context */
    private DeploymentContext deploymentContext;
    
    /**
     * Create a new AbstractDeploymentUnit.
     * 
     * @param deploymentContext the deployment context
     */
    public AbstractDeploymentUnit(DeploymentContext deploymentContext)
    {
       if (deploymentContext == null)
          throw new IllegalArgumentException("Null deployment context");
       this.deploymentContext = deploymentContext;
    }
    
    public String getName()
    {
       return deploymentContext.getName();
    }
    
    public ClassLoader getClassLoader()
    {
       ClassLoader cl = deploymentContext.getClassLoader();
       if (cl == null)
          throw new IllegalStateException("ClassLoader has not been set");
       return cl;
    }
 
    public boolean createClassLoader(ClassLoaderFactory factory) throws DeploymentException
    {
       return deploymentContext.createClassLoader(factory);
    }
    
    public VirtualFile getMetaDataFile(String name)
    {
       return deploymentContext.getMetaDataFile(name);
    }
 
    public List<VirtualFile> getMetaDataFiles(String name, String suffix)
    {
       return deploymentContext.getMetaDataFiles(name, suffix);
    }
 
    public DeploymentUnit addComponent(String name)
    {
       ComponentDeploymentContext component = new ComponentDeploymentContext(name, deploymentContext);
       AbstractDeploymentUnit unit = new AbstractDeploymentUnit(component);
       component.setDeploymentUnit(unit);
       deploymentContext.addComponent(component);
       return unit;
    }
 
    public boolean removeComponent(String name)
    {
       if (name == null)
          throw new IllegalArgumentException("Null name");
       
       for (DeploymentContext component : deploymentContext.getComponents())
       {
          if (name.equals(component.getName()))
             return deploymentContext.removeComponent(component);
       }
       return false;
    }
 
    public Map<String, Object> getAttachments()
    {
       DeploymentContext parent = deploymentContext.getParent();
       if (deploymentContext.isComponent() == false)
          parent = null;
       HashMap<String, Object> result = new HashMap<String, Object>();
       if (parent != null)
          result.putAll(parent.getTransientAttachments().getAttachments());
       result.putAll(deploymentContext.getTransientAttachments().getAttachments());
       if (parent != null)
          result.putAll(parent.getTransientManagedObjects().getAttachments());
       result.putAll(deploymentContext.getTransientManagedObjects().getAttachments());
       if (parent != null)
          result.putAll(parent.getPredeterminedManagedObjects().getAttachments());
       result.putAll(deploymentContext.getPredeterminedManagedObjects().getAttachments());
       return Collections.unmodifiableMap(result);
    }
 
    public Object addAttachment(String name, Object attachment)
    {
       return deploymentContext.getTransientAttachments().addAttachment(name, attachment);
    }
 
    public Object getAttachment(String name)
    {
       DeploymentContext parent = deploymentContext.getParent();
      if (deploymentContext.isComponent() == false)
         parent = null;
       Object result = deploymentContext.getPredeterminedManagedObjects().getAttachment(name);
       if (result != null)
          return result;
       if (parent != null)
       {
          result = parent.getPredeterminedManagedObjects().getAttachment(name);
          if (result != null)
             return result;
       }
       result = deploymentContext.getTransientManagedObjects().getAttachment(name);
       if (result != null)
          return result;
       if (parent != null)
       {
          result = parent.getTransientManagedObjects().getAttachment(name);
          if (result != null)
             return result;
       }
       result = deploymentContext.getTransientAttachments().getAttachment(name);
       if (result != null)
          return result;
       if (parent != null)
       {
          result = parent.getTransientAttachments().getAttachment(name);
          if (result != null)
             return result;
       }
       return null;
    }
 
    public boolean isAttachmentPresent(String name)
    {
       DeploymentContext parent = deploymentContext.getParent();
      if (deploymentContext.isComponent() == false)
         parent = null;
       if (deploymentContext.getPredeterminedManagedObjects().isAttachmentPresent(name))
          return true;
       if (parent != null && parent.getPredeterminedManagedObjects().isAttachmentPresent(name))
          return true;
       if (deploymentContext.getTransientManagedObjects().isAttachmentPresent(name))
          return true;
       if (parent != null && parent.getTransientAttachments().isAttachmentPresent(name))
          return true;
       if (deploymentContext.getTransientAttachments().isAttachmentPresent(name))
          return true;
       if (parent != null && parent.getTransientAttachments().isAttachmentPresent(name))
          return true;
       return false;
    }
 
    public Object removeAttachment(String name)
    {
       return deploymentContext.getTransientAttachments().removeAttachment(name);
    }
 
    public Attachments getTransientManagedObjects()
    {
       return deploymentContext.getTransientManagedObjects();
    }
 
    @SuppressWarnings("unchecked")
    // TODO optimize
    public <T> Set<? extends T> getAllMetaData(Class<T> type)
    {
       if (type == null)
          throw new IllegalArgumentException("Null type");
       
       Set<T> result = new HashSet<T>();
       Map<String, Object> attachments = getAttachments();
       for (Object object : attachments.values())
       {
          if (type.isInstance(object))
             result.add((T) object);
       }
       return result;
    }
 
    @Deprecated
    public DeploymentContext getDeploymentContext()
    {
       return deploymentContext;
    }
 }
