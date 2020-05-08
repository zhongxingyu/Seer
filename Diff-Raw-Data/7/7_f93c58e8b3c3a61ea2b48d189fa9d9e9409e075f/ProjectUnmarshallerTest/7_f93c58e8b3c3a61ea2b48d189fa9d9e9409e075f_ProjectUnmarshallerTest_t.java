 /*
  * Licensed to Marvelution under one or more contributor license 
  * agreements.  See the NOTICE file distributed with this work 
  * for additional information regarding copyright ownership.
  * Marvelution licenses this file to you under the Apache License,
  * Version 2.0 (the "License"); you may not use this file except
  * in compliance with the License.
  * You may obtain a copy of the License at
  *
  *  http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an
  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  * KIND, either express or implied. See the License for the
  * specific language governing permissions and limitations
  * under the License.
  */
 
 package com.marvelution.bamboo.plugins.sonar.wsclient.unmarshallers;
 
 import static org.hamcrest.CoreMatchers.is;
 import static org.junit.Assert.*;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.StringWriter;
 
 import org.apache.commons.io.IOUtils;
 import org.apache.commons.lang.StringUtils;
 import org.junit.Before;
 import org.junit.Test;
 import org.sonar.wsclient.JdkUtils;
 import org.sonar.wsclient.services.Resource;
 import org.sonar.wsclient.services.WSUtils;
 
 import com.marvelution.bamboo.plugins.sonar.wsclient.services.Project;
 import com.marvelution.bamboo.plugins.sonar.wsclient.services.Version;
 import com.marvelution.bamboo.plugins.sonar.wsclient.unmarshallers.ProjectUnmarshaller;
 
 /**
  * TestCase for the {@link ProjectUnmarshaller} implementation
  * 
  * @author <a href="mailto:markrekveld@marvelution.com">Mark Rekveld</a>
  */
 public class ProjectUnmarshallerTest {
 
 	private ProjectUnmarshaller unmarshaller;
 
 	static {
 		WSUtils.setInstance(new JdkUtils());
 	}
 
 	/**
 	 * Setup the tests
 	 * 
 	 * @throws Exception in case of errors
 	 */
 	@Before
 	public void before() throws Exception {
 		unmarshaller = new ProjectUnmarshaller();
 	}
 
 	/**
 	 * Test the {@link ProjectUnmarshaller#toModel(String)} method without Versions
 	 * 
 	 * @throws Exception in case of errors
 	 */
 	@Test
 	public void testToModelWithoutVersions() throws Exception {
 		final String json = getJSON("project-without-versions.json");
 		final Project project = unmarshaller.toModel(json);
 		assertThat(project.getId(), is(1120));
 		assertThat(project.getKey(), is("com.marvelution.bamboo.plugins:bamboo-sonar-integration"));
 		assertThat(project.getName(), is("Bamboo Sonar Integration"));
 		assertThat(project.getScope(), is(Resource.SCOPE_SET));
 		assertThat(project.getQualifier(), is(Resource.QUALIFIER_PROJECT));
 		assertTrue(StringUtils.isNotBlank(project.getDescription()));
 		assertTrue("Expected the versions List to be empty", project.getVersions().isEmpty());
 	}
 
 	/**
 	 * Test the {@link ProjectUnmarshaller#toModel(String)} method with Versions
 	 * 
 	 * @throws Exception in case of errors
 	 */
 	@Test
 	public void testToModelWithVersions() throws Exception {
 		final String json = getJSON("project-with-versions.json");
 		final Project project = unmarshaller.toModel(json);
 		assertThat(project.getId(), is(48569));
 		assertThat(project.getKey(), is("org.codehaus.sonar:sonar"));
 		assertThat(project.getName(), is("Sonar"));
 		assertThat(project.getScope(), is(Resource.SCOPE_SET));
 		assertThat(project.getQualifier(), is(Resource.QUALIFIER_PROJECT));
 		assertTrue(StringUtils.isNotBlank(project.getDescription()));
 		assertFalse("Expected the versions List to have versions", project.getVersions().isEmpty());
 		assertThat("Expected 17 Versions", project.getVersions().size(), is(17));
 		Version last = project.getLastVersion();
 		assertThat(last.getName(), is("2.11-SNAPSHOT"));
 		assertThat(last.getSid(), is(16894689));
 	}
 
 	/**
 	 * Get the test JSON file content for the given json file
 	 * 
 	 * @param jsonFile the json file name to get
 	 * @return the String content
 	 * @throws IOException in case of errors
 	 */
 	private String getJSON(String jsonFile) throws IOException {
 		final InputStream input = getClass().getResourceAsStream(jsonFile);
 		final StringWriter writer = new StringWriter();
 		IOUtils.copy(input, writer);
 		IOUtils.closeQuietly(input);
 		return writer.toString();
 	}
 
 }
