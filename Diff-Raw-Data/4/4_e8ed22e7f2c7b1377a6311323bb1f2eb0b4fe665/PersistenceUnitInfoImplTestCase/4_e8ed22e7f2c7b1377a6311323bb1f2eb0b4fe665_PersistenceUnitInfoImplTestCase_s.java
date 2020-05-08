 /*
  * JBoss, Home of Professional Open Source.
  * Copyright 2009, Red Hat Middleware LLC, and individual contributors
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
 package org.jboss.jpa.impl.test.deployment;
 
 import static junit.framework.Assert.assertNull;
 
 import java.net.URL;
 import java.util.List;
 import java.util.Properties;
 
 import javax.naming.Context;
 import javax.naming.NamingException;
 
 import org.jboss.jpa.impl.deployment.PersistenceUnitInfoImpl;
 import org.jboss.jpa.impl.test.common.BrainlessContext;
 import org.jboss.metadata.jpa.spec.PersistenceUnitMetaData;
 import org.jboss.metadata.jpa.spec.ValidationMode;
 import org.junit.Test;
 
 /**
  * Generic tests against PersistenceUnitInfoImpl that provide coverage
  * and test general conditions.
  * 
  * @author <a href="mailto:cdewolf@redhat.com">Carlo de Wolf</a>
  * @version $Revision: $
  */
 public class PersistenceUnitInfoImplTestCase
 {
    /**
     * If I don't specify cache mode I don't want a NPE.
     */
    @Test
    public void testUnspecifiedCacheMode() throws Exception
    {
       PersistenceUnitMetaData metaData = new PersistenceUnitMetaData();
       metaData.setJtaDataSource("dummy-datasource");
       metaData.setName("dummy-name");
       metaData.setValidationMode(ValidationMode.AUTO);
       Properties props = new Properties();
       ClassLoader classLoader = null;
       URL persistenceUnitRootUrl = null;
      List<URL> jarFiles = null;
       Context ctx = new BrainlessContext() {
          @Override
          public Object lookup(String name) throws NamingException
          {
             return null;
          }
       };
       PersistenceUnitInfoImpl puii = new PersistenceUnitInfoImpl(metaData, props, classLoader, persistenceUnitRootUrl, jarFiles, ctx);
       assertNull(puii.getCaching());
    }
 }
