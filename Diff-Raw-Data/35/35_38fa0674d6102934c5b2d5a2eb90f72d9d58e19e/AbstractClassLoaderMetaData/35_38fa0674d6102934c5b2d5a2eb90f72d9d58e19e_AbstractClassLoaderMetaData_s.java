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
 package org.jboss.beans.metadata.plugins;
 
 import java.util.Collections;
 import java.util.Iterator;
 import java.util.List;
 import java.util.ArrayList;
 
 import org.jboss.beans.metadata.spi.*;
 import org.jboss.dependency.spi.ControllerState;
 import org.jboss.util.JBossObject;
 import org.jboss.util.JBossStringBuilder;
 
 /**
  * A classloader.
  * 
  * @author <a href="adrian@jboss.com">Adrian Brock</a>
  * @version $Revision$
  */
 public class AbstractClassLoaderMetaData extends JBossObject implements ClassLoaderMetaData, BeanMetaDataFactory
 {
    /** The classloader */
    protected ValueMetaData classloader;
 
    /**
     * Create a new classloader
     */
    public AbstractClassLoaderMetaData()
    {
    }
 
    /**
     * Create a new classloader
     * 
     * @param classloader the classloader value
     */
    public AbstractClassLoaderMetaData(ValueMetaData classloader)
    {
       this.classloader = classloader;
    }
 
    /**
     * Set the classloader
     * 
     * @param classloader the classloader value
     */
    public void setClassLoader(ValueMetaData classloader)
    {
       this.classloader = classloader;
       flushJBossObjectCache();
    }
 
    public ValueMetaData getClassLoader()
    {
       return classloader;
    }
 
    public List<BeanMetaData> getBeans()
    {
      if (classloader instanceof BeanMetaData)
       {
          return Collections.singletonList((BeanMetaData)classloader);
       }
       return new ArrayList<BeanMetaData>();
    }
 
    public void initialVisit(MetaDataVisitor visitor)
    {
       visitor.setContextState(ControllerState.DESCRIBED);
       visitor.initialVisit(this);
    }
 
    public void describeVisit(MetaDataVisitor vistor)
    {
       // todo - any context setting?
       vistor.describeVisit(this);
    }
 
    public Iterator<? extends MetaDataVisitorNode> getChildren()
    {
       if (classloader != null)
          return Collections.singletonList(classloader).iterator();
       return null;
    }
 
    public void toString(JBossStringBuilder buffer)
    {
       buffer.append("classloader=").append(classloader);
    }
 
    public void toShortString(JBossStringBuilder buffer)
    {
       buffer.append(classloader);
    }
 }
