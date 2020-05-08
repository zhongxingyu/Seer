 /*
  * JBoss, Home of Professional Open Source
  * Copyright 2008, Red Hat Middleware LLC, and individual contributors
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
 package org.jboss.test.kernel.deployment.support.container;
 
 import java.util.concurrent.ArrayBlockingQueue;
 
 import org.jboss.beans.metadata.spi.factory.BeanFactory;
 
 /**
  * 
  * @param <T> the type
  * @author Scott.Stark@jboss.org
  * @version $Revision$
  */
 public class BeanPool<T>
 {
    /** The pooling policy */
    private ArrayBlockingQueue<T> pool = new ArrayBlockingQueue<T>(2);
    private BeanFactory factory;
    
    public BeanFactory getFactory()
    {
       return factory;
    }
 
    public void setFactory(BeanFactory factory)
    {
       this.factory = factory;
    }
 
    @SuppressWarnings("unchecked")
    public synchronized T createBean()
       throws Throwable
    {
       if(pool.size() == 0)
       {
         int capacity = pool.remainingCapacity();
          // Fill the pool
         for(int n = 0; n < capacity; n ++)
          {
             T bean = (T) factory.createBean();
             pool.put(bean);
          }
       }
       return pool.take();
    }
    public void destroyBean(T bean)
       throws Throwable
    {
       pool.put(bean);
    }
 }
