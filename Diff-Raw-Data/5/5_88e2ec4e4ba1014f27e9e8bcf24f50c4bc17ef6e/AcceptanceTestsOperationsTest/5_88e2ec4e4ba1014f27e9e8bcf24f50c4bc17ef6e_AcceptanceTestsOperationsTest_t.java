 //  Copyright 2009 viadee Unternehmensberatung GmbH / Andreas Simon
 //	
 //  Licensed under the Apache License, Version 2.0 (the "License");
 //  you may not use this file except in compliance with the License.
 //  You may obtain a copy of the License at
 //	
 //    http://www.apache.org/licenses/LICENSE-2.0
 //	
 //  Unless required by applicable law or agreed to in writing, software
 //  distributed under the License is distributed on an "AS IS" BASIS,
 //  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 //  See the License for the specific language governing permissions and
 //  limitations under the License.
 package com.viadee.acceptancetests.roo.addon;
 
 import static org.junit.Assert.assertEquals;
 import static org.mockito.Matchers.any;
 import static org.mockito.Matchers.argThat;
 import static org.mockito.Mockito.mock;
 import static org.mockito.Mockito.never;
 import static org.mockito.Mockito.verify;
 import static org.mockito.Mockito.when;
 
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 
 import org.hamcrest.BaseMatcher;
 import org.hamcrest.Description;
 import org.junit.Before;
 import org.junit.Test;
 import org.mockito.Mockito;
 import org.springframework.roo.classpath.MutablePhysicalTypeMetadataProvider;
 import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetails;
 import org.springframework.roo.classpath.details.MethodMetadata;
 import org.springframework.roo.classpath.operations.ClasspathOperations;
 import org.springframework.roo.process.manager.FileManager;
 import org.springframework.roo.process.manager.MutableFile;
 
 public class AcceptanceTestsOperationsTest {
 
 	static final String POM_IDENTIFIER = "ROOT/pom.xml";
 
 	private AcceptanceTestsOperations _acceptanceTestsOperations;
 	private MockProjectOperations _projectOperations;
 	private FileManager _fileManager;
 	private MockProjectMetadataProvider _projectMetadataProvider;
 	private ClasspathOperations _classpathOperations;
 	private MockMetadataService _metadataService;
 	private MutablePhysicalTypeMetadataProvider _physicalTypeMetadataProvider;
 
 	@Before
 	public void setUp() {
 		_metadataService = new MockMetadataService();
 		_projectMetadataProvider = new MockProjectMetadataProvider();
 		_projectOperations = new MockProjectOperations(_metadataService,
 				_projectMetadataProvider);
 		_physicalTypeMetadataProvider = new MockPhysicalTypeMetadataProvider();
 		MockPathResolver pathResolver = new MockPathResolver();
 		_acceptanceTestsOperations = new AcceptanceTestsOperations(
 				mockFileManager(), pathResolver, _metadataService,
 				_projectOperations, mockClasspathOperations(),
 				acceptanceTestsUtils());
 	}
 
 	private AcceptanceTestsUtils acceptanceTestsUtils() {
 		return new AcceptanceTestsUtils(_metadataService);
 	}
 
 	private ClasspathOperations mockClasspathOperations() {
 		if (_classpathOperations == null) {
 			_classpathOperations = new MockClasspathOperations(
 					_projectOperations, _physicalTypeMetadataProvider,
 					_metadataService, mockFileManager());
 			_classpathOperations = Mockito.spy(_classpathOperations);
 		}
 		return _classpathOperations;
 	}
 
 	private FileManager mockFileManager() {
 		if (_fileManager == null) {
 			_fileManager = mock(FileManager.class);
 
 			when(_fileManager.exists(POM_IDENTIFIER)).thenReturn(true);
 			MutableFile mutablePomFile = mutablePomFile();
 			when(_fileManager.updateFile(POM_IDENTIFIER)).thenReturn(
 					mutablePomFile);
 
 			MutableFile mutableFile = mutableEmptyFile();
 			when(
 					_fileManager
 							.updateFile("SRC_TEST_JAVA/de/viadee/bibliothek1/acceptancetests/GeneralStories.java"))
 					.thenReturn(mutableFile);
 			when(
 					_fileManager
 							.updateFile("SRC_TEST_JAVA/de/viadee/bibliothek1/acceptancetests/SpecifiedStories.java"))
 					.thenReturn(mutableFile);
 		}
 		return _fileManager;
 	}
 
 	private MutableFile mutableEmptyFile() {
 		MutableFile mutableEmptyFile = mock(MutableFile.class);
 
 		InputStream pomInputStream = emptyInputStream();
 		when(mutableEmptyFile.getInputStream()).thenReturn(pomInputStream);
 
 		OutputStream pomOutputStream = new ByteArrayOutputStream();
 		when(mutableEmptyFile.getOutputStream()).thenReturn(pomOutputStream);
 
 		return mutableEmptyFile;
 	}
 
 	private InputStream emptyInputStream() {
 		return mock(InputStream.class);
 	}
 
 	private MutableFile mutablePomFile() {
 		return new MutablePomFile();
 	}
 
 	@Test
 	public void loadDependenciesShouldLoadDependencies() {
		assertEquals(2, _acceptanceTestsOperations.loadDependencies().size());
 	}
 
 	@Test
 	public void newAcceptanceTestShouldUpdateDependencies() throws IOException {
 		newShowHomepageTest();
		assertEquals(2, _projectOperations.getCallsToDependencyUpdate());
 	}
 
 	@Test
 	public void newAcceptanceTestShouldNotBlowUpWhenAcceptanceTestDirectoryAlreadyExists()
 			throws IOException {
 		when(_fileManager.createDirectory((String) any())).thenThrow(
 				new IllegalArgumentException());
 		newShowHomepageTest();
 	}
 
 	@Test
 	public void newAcceptanceTestShouldBindJettyStartToPreIntegrationPhase()
 			throws IOException {
 		newShowHomepageTest();
 	}
 
 	@Test
 	public void newAcceptanceTestShouldCreateClassForGeneralGroup()
 			throws IOException {
 		newShowHomepageTest();
 		verify(mockClasspathOperations()).generateClassFile(
 				argThat(generatedClassesMatcher()));
 	}
 
 	private BaseMatcher<ClassOrInterfaceTypeDetails> generatedClassesMatcher() {
 		return new BaseMatcher<ClassOrInterfaceTypeDetails>() {
 
 			public boolean matches(Object param) {
 				if (param instanceof ClassOrInterfaceTypeDetails) {
 					ClassOrInterfaceTypeDetails typeDetails = (ClassOrInterfaceTypeDetails) param;
 					String qualifiedTypeName = typeDetails.getName().toString();
 					return qualifiedTypeName
 							.equals("de.viadee.bibliothek1.acceptancetests.GeneralStories")
 							|| qualifiedTypeName
 									.equals("de.viadee.bibliothek1.acceptancetests.AbstractAcceptanceTest");
 				}
 
 				return false;
 			}
 
 			public void describeTo(Description arg0) {
 			}
 		};
 	}
 
 	@Test
 	public void newAcceptanceTestShouldCreateClassForSpecifiedGroup()
 			throws IOException {
 		_acceptanceTestsOperations.newAcceptanceTest("Should show homepage",
 				"Specified");
 		verify(mockClasspathOperations()).generateClassFile(
 				argThat(new BaseMatcher<ClassOrInterfaceTypeDetails>() {
 
 					public boolean matches(Object param) {
 						if (param instanceof ClassOrInterfaceTypeDetails) {
 							ClassOrInterfaceTypeDetails typeDetails = (ClassOrInterfaceTypeDetails) param;
 							return typeDetails
 									.getName()
 									.toString()
 									.equals(
 											"de.viadee.bibliothek1.acceptancetests.SpecifiedStories");
 						}
 
 						return false;
 					}
 
 					public void describeTo(Description arg0) {
 					}
 				}));
 	}
 
 	@Test
 	public void newAcceptanceTestShouldInsertNewTestCaseForGivenStory()
 			throws IOException {
 		newShowHomepageTest();
 		verify(mockClasspathOperations()).generateClassFile(
 				argThat(new BaseMatcher<ClassOrInterfaceTypeDetails>() {
 
 					public boolean matches(Object param) {
 						if (param instanceof ClassOrInterfaceTypeDetails) {
 							ClassOrInterfaceTypeDetails typeDetails = (ClassOrInterfaceTypeDetails) param;
 							if (typeDetails.getDeclaredMethods().size() == 1) {
 								if ("shouldShowHomepage".equals(typeDetails
 										.getDeclaredMethods().get(0)
 										.getMethodName().toString())) {
 									return true;
 								}
 							}
 						}
 						return false;
 					}
 
 					public void describeTo(Description arg0) {
 					}
 				}));
 	}
 
 	@Test
 	public void newAcceptanceTestShouldInsertNewTestIntoExistingGroup()
 			throws IOException {
 		_metadataService.setGeneralStoriesExists(true);
 
 		_acceptanceTestsOperations.newAcceptanceTest(
 				"Should show profile page", "General");
 
 		verify(_metadataService.generalStoriesPhysicalTypeDetails()).addMethod(
 				argThat(new BaseMatcher<MethodMetadata>() {
 
 					public boolean matches(Object param) {
 						if (param instanceof MethodMetadata) {
 							MethodMetadata methodMetadata = (MethodMetadata) param;
 							if ("shouldShowProfilePage".equals(methodMetadata
 									.getMethodName().toString())) {
 								return true;
 							}
 						}
 						return false;
 					}
 
 					public void describeTo(Description arg0) {
 					}
 				}));
 	}
 
 	@Test(expected = IllegalStateException.class)
 	public void newAcceptanceTestShouldNotInsertNewTestForExistingStory()
 			throws IOException {
 		_metadataService.setGeneralStoriesExists(true);
 
 		_acceptanceTestsOperations.newAcceptanceTest(
 				"This is an existing story", "General");
 
 		verify(_metadataService.generalStoriesPhysicalTypeDetails(), never())
 				.addMethod((MethodMetadata) any());
 
 	}
 
 	private void newShowHomepageTest() throws IOException {
 		_acceptanceTestsOperations.newAcceptanceTest("Should show homepage",
 				"General");
 	}
 }
