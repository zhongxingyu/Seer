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
 package org.jboss.test.microcontainer.beans.test;
 
 import junit.framework.Test;
 import org.jboss.test.aop.junit.AbstractTypeTest;
 import org.jboss.test.aop.junit.AbstractTypeTestDelegate;
 import org.jboss.test.microcontainer.beans.support.AccessBean;
 
 /**
  * AOP field test case.
  *
  * @author <a href="mailto:ales.justin@jboss.com">Ales Justin</a>
  */
 public class FieldAccessTestCase extends AbstractTypeTest
 {
    public FieldAccessTestCase(String name)
    {
       super(name);
    }
 
    public static Test suite()
    {
       return suite(FieldAccessTestCase.class);
    }
 
    protected void testAccessBean(String name) throws Throwable
    {
       AccessBean bean = getBean(name, AccessBean.class);
       AbstractTypeTestDelegate.Type type = getType(name);
       if (type == AbstractTypeTestDelegate.Type.PROXY)
       {
          assertNull(bean.getPriString());
          assertNull(bean.getProtString());
          assertNull(bean.getPubString());
       }
       else
       {
          assertEquals("foobar", concat(bean));
       }
    }
 
    private String concat(AccessBean bean)
    {
       String string = "";
       if (bean.getPriString() != null)
          string += bean.getPriString();
       if (bean.getProtString() != null)
          string += bean.getProtString();
      if (bean.getPubString() != null)
         string += bean.getPubString();
       return string;
    }
 
    public void testFieldAccess() throws Throwable
    {
       testAccessBean("private");
       testAccessBean("protected");
       testAccessBean("public");
    }
 }
