 /*******************************************************************************
  * Copyright (c) 2013 GigaSpaces Technologies Ltd. All rights reserved
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *       http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  *******************************************************************************/
 package org.cloudifysource.quality.iTests.test.cli.cloudify.cloud.byon;
 
 import java.io.File;
 import java.io.IOException;
 
 import org.cloudifysource.dsl.internal.DSLException;
 import org.cloudifysource.dsl.internal.packaging.PackagingException;
 import org.cloudifysource.dsl.internal.packaging.ZipUtils;
 import org.cloudifysource.quality.iTests.test.AbstractTestSupport;
 import org.cloudifysource.quality.iTests.test.cli.cloudify.CommandTestUtils;
 import org.cloudifysource.quality.iTests.test.cli.cloudify.NewRestTestUtils;
 import org.cloudifysource.quality.iTests.test.cli.cloudify.cloud.templates.TemplatesCommandsRestAPI;
 import org.junit.Assert;
 import org.testng.annotations.BeforeClass;
 import org.testng.annotations.Test;
 
 public class ValidateApplicationServicesTest extends AbstractByonCloudTest {
 
 	private static final String APP_NAME = "simple";
     private static final String NOT_EXIST_TEMPLATE_APP_FOLDER_PATH = 
     		CommandTestUtils.getPath("src/main/resources/apps/USM/usm/applications/simple-with-template");
     private static final String SIMPLE_APP_FOLDER_PATH = 
     		CommandTestUtils.getPath("src/main/resources/apps/USM/usm/applications/simple");
 	private static final String NOT_EXIST_STORAGE_TEMPLATE_APP_FOLDER_PATH = 
     		CommandTestUtils.getPath("src/main/resources/apps/USM/usm/applications/simple-with-storage-template");
 	private static final String STORAGE_TEMPLATE_FOLDER_PATH = 
     		CommandTestUtils.getPath("src/main/resources/templates/STORAGE_TEMPLATE");
     
 	@Override
 	protected String getCloudName() {
 		return "byon";
 	}
 	
 	@BeforeClass(alwaysRun = true)
 	protected void bootstrap() throws Exception {
 		super.bootstrap();
 	}
 	
 	@Test(timeOut = AbstractTestSupport.DEFAULT_TEST_TIMEOUT * 2, enabled = true)
 	public void testMissingTemplate() throws IOException, DSLException, PackagingException {
 		File appFolder = new File(NOT_EXIST_TEMPLATE_APP_FOLDER_PATH);
 		NewRestTestUtils.installApplicationUsingNewRestApi(
 					getRestUrl(), 
 					APP_NAME, 
 					appFolder, 
 					null /* overrides file */, 
					"template [TEMPLATE_1] does not exist at clouds template list");
 	}
 	
 	@Test(timeOut = AbstractTestSupport.DEFAULT_TEST_TIMEOUT * 2, enabled = true)
 	public void testMissingStorgaeTemplate() throws IOException, DSLException, PackagingException {
 		try {
 			
 			File templatesFolder = new File(STORAGE_TEMPLATE_FOLDER_PATH);
 			File templatesPackedFile = File.createTempFile("packedStorageTempalte", ".zip");
 			ZipUtils.zip(templatesFolder, templatesPackedFile);
 			TemplatesCommandsRestAPI.addTemplates(getRestUrl(), templatesPackedFile);
 		} catch (Exception e) {
 			Assert.fail("failed to add storage template: " + e.getLocalizedMessage());
 		} 
 		File appFolder = new File(NOT_EXIST_STORAGE_TEMPLATE_APP_FOLDER_PATH);
 		NewRestTestUtils.installApplicationUsingNewRestApi(
 					getRestUrl(), 
 					APP_NAME, 
 					appFolder, 
 					null /* overrides file */, 
					"template [STORAGE_TEMPLATE] does not exist at clouds template list");
 	}
 	
 	
 	/**
 	 * Tests that the ValidateApplicationServices can handle service without the compute part.
 	 * @throws IOException
 	 * @throws DSLException
 	 * @throws PackagingException
 	 */
 	@Test(timeOut = AbstractTestSupport.DEFAULT_TEST_TIMEOUT * 2, enabled = true)
 	public void testServiceWithoutCompute() throws IOException, DSLException, PackagingException {
 		File appFolder = new File(SIMPLE_APP_FOLDER_PATH);
 		NewRestTestUtils.installApplicationUsingNewRestApi(
 					getRestUrl(), 
 					APP_NAME, 
 					appFolder);
 	}
 	
 }
