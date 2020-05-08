 package org.apache.archiva.redback.components.cache.ehcache;
 /*
  * Licensed to the Apache Software Foundation (ASF) under one
  * or more contributor license agreements.  See the NOTICE file
  * distributed with this work for additional information
  * regarding copyright ownership.  The ASF licenses this file
  * to you under the Apache License, Version 2.0 (the
  * "License"); you may not use this file except in compliance
  * with the License.  You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an
  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  * KIND, either express or implied.  See the License for the
  * specific language governing permissions and limitations
  * under the License.
  */
 
 import junit.framework.TestCase;
 import net.sf.ehcache.CacheManager;
 import org.apache.archiva.redback.components.cache.Cache;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 
 import javax.inject.Inject;
 import javax.inject.Named;
 import java.io.File;
 
 /**
  * @author Olivier Lamy
  */
 @RunWith( SpringJUnit4ClassRunner.class )
 @ContextConfiguration( locations = { "classpath*:/spring-context-disk.xml" } )
 public class EhCacheDiskPathTest
     extends TestCase
 {
     @Inject
     @Named( value = "cache#test-cache-int-disk" )
     Cache<String, Integer> cache;
 
     @Test
     public void testDisk()
         throws Exception
     {
         for ( int i = 0; i < 1000; i++ )
         {
             cache.put( Integer.toString( i ), Integer.valueOf( i ) );
         }
         File cacheDiskFile = new File( System.getProperty( "basedir" ), "target/ehcache-test-store-disk-int" );
         assertTrue( cacheDiskFile.exists() );

        assertTrue( cacheDiskFile.length() > 0 );
     }
 }
