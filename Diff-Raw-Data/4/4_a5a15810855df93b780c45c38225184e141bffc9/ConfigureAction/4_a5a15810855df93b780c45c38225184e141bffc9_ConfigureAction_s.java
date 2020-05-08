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
 package org.jboss.kernel.plugins.dependency;
 
 import java.util.Set;
 
 import org.jboss.beans.info.spi.BeanInfo;
 import org.jboss.beans.metadata.spi.BeanMetaData;
 import org.jboss.beans.metadata.spi.PropertyMetaData;
 import org.jboss.kernel.plugins.config.Configurator;
 import org.jboss.kernel.spi.dependency.KernelControllerContext;
 
 /**
  * New ConfigureAction.
  * @see org.jboss.kernel.plugins.dependency.OldConfigureAction
  *
  * @author <a href="ales.justin@jboss.com">Ales Justin</a>
  */
 public class ConfigureAction extends AbstractConfigureAction
 {
    protected void installActionInternal(KernelControllerContext context) throws Throwable
    {
       Object object = context.getTarget();
       BeanInfo info = context.getBeanInfo();
       BeanMetaData metaData = context.getBeanMetaData();
       setAttributes(context, object, info, metaData, false);
 
       installKernelControllerContextAware(context);
    }
 
    protected void uninstallActionInternal(KernelControllerContext context)
    {
       uninstallKernelControllerContextAware(context);
 
       Object object = context.getTarget();
       BeanInfo info = context.getBeanInfo();
       BeanMetaData metaData = context.getBeanMetaData();
       try
       {
          setAttributes(context, object, info, metaData, true);
       }
       catch (Throwable t)
       {
          log.warn("Error unconfiguring bean " + context, t);
       }
    }
 
    /**
     * Set attributes/properties.
     *
     * @param context the context
     * @param target the target
     * @param info the bean info
     * @param metaData the bean metadata
     * @param nullify should we nullify attributes/properties
     * @throws Throwable for any error
     */
    protected void setAttributes(KernelControllerContext context, Object target, BeanInfo info, BeanMetaData metaData, boolean nullify) throws Throwable
    {
       Set<PropertyMetaData> propertys = metaData.getProperties();
       if (propertys != null && propertys.isEmpty() == false)
       {
          ClassLoader cl = null;
          if (nullify == false)
             cl = Configurator.getClassLoader(metaData);
 
          for(PropertyMetaData property : propertys)
          {
             dispatchSetProperty(context, property, nullify, info, target, cl);
          }
       }
    }
 
    /**
     * Dispatch property set
     *
     * @param context the context
     * @param property the property
     * @param nullify should we nullify
     * @param info the bean info
     * @param target the target
     * @param cl classloader
     * @throws Throwable for any error
     */
    protected void dispatchSetProperty(KernelControllerContext context, PropertyMetaData property, boolean nullify, BeanInfo info, Object target, ClassLoader cl)
          throws Throwable
    {
       ExecutionWrapper wrapper = new PropertyDispatchWrapper(property, nullify, info, target, cl);
      // FIXME - http://www.jboss.org/index.html?module=bb&op=viewtopic&p=4112356
//      dispatchExecutionWrapper(context, wrapper);
      wrapper.execute(null);      
    }
 }
