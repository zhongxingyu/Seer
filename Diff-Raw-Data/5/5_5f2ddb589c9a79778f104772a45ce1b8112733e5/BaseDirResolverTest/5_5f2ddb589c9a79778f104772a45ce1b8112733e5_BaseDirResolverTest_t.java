 /*
  * Copyright 2011 the original author or authors.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package org.brekka.stillingar.spring.resource;
 
 import static org.junit.Assert.*;
 
 import java.io.File;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.brekka.stillingar.spring.resource.BaseDirResolver;
 import org.junit.Before;
 import org.junit.Test;
 import org.springframework.core.io.Resource;
 
 public class BaseDirResolverTest {
 
 	private BaseDirResolver resolverFactoryBean;
 	
 	private File baseDir;
 	
 	@Before
 	public void setUp() throws Exception {
 		File tmpDir = new File(System.getProperty("java.io.tmpdir"));
 		this.baseDir = new File(tmpDir, "BaseDirResolverTest-" + System.currentTimeMillis());
 		File configDir = new File(baseDir, "conf");
 		configDir.mkdirs();
 		configDir.deleteOnExit();
 		baseDir.deleteOnExit();
 		resolverFactoryBean = new BaseDirResolver(Arrays.asList(""));
 	}
 
 	@Test
 	public void testResolveSysProp() throws Exception {
 		System.setProperty("catalina.base", baseDir.getAbsolutePath());
 		Resource resolve = resolverFactoryBean.resolve("file:${catalina.base}/conf");
		assertEquals(new File(baseDir.getAbsolutePath(), "/conf"), resolve.getFile());
 	}
 
 	@Test
 	public void testResolveEnvProp() throws Exception {
 		
 		Map<String, String> env = new HashMap<String, String>();
 		env.put("DOMAIN_HOME", baseDir.getAbsolutePath());
 		resolverFactoryBean.setEnvMap(env);
 		Resource resolve = resolverFactoryBean.resolve("file:${env.DOMAIN_HOME}/conf");
		assertEquals(new File(baseDir.getAbsolutePath(), "/conf"), resolve.getFile());
 	}
 }
