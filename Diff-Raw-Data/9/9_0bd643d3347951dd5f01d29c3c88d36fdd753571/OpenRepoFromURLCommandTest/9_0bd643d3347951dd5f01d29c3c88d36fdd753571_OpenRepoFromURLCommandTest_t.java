 package org.codefaces.ui.internal.commands;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertTrue;
 
 import java.util.HashMap;
 
 import org.codefaces.core.connectors.SCMResponseException;
 import org.codefaces.core.models.RepoFolder;
 import org.codefaces.core.models.RepoProject;
 import org.codefaces.core.models.RepoResource;
 import org.codefaces.core.models.RepoWorkspace;
 import org.codefaces.ui.SCMConfigurableElement;
 import org.codefaces.ui.SCMURLConfiguration;
 import org.eclipse.core.commands.ExecutionEvent;
 import org.eclipse.core.commands.ExecutionException;
 import org.eclipse.core.expressions.EvaluationContext;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.Path;
 import org.junit.Before;
 import org.junit.Test;
 
 public class OpenRepoFromURLCommandTest {
 	private static class MockWorkSpace extends RepoWorkspace {
 		public RepoProject getProject() {
 			return getProjects().iterator().next();
 		}
 	}
 
 	private static final String TEST_MOCK_URL = "http://mock_url";
 	private static final String TEST_SCM_KIND = "MOCK_KIND";
 	private static final String TEST_USER = "guest";
 	private static final String TEST_PASSWORD = null;
 
	private static final String TEST_CORRECT_PROJECT_NAME = "correct/path";
 
 	private static final String TEST_CORRECT_BASE_DIRECTORY_WITH_PREFIX_SLASH = "/correct/path";
 	private static final String TEST_INCORRECT_BASE_DIRECTORY = "incorrect/path";
 	private static final String TEST_TOO_LONG_BASE_DIRECTORY = "long/correct/path";
 	private static final String TEST_ROOT_PATH = "/";
 
 	private MockWorkSpace workspace;
 
 	private static final Object[][] NORMAL_HTTP_QUERY_PARAMETERS = {
 			{ SCMConfigurableElement.REPO_URL, TEST_MOCK_URL },
 			{ SCMConfigurableElement.SCM_KIND, TEST_SCM_KIND },
 			{ SCMConfigurableElement.USER, TEST_USER },
 			{ SCMConfigurableElement.BASE_DIRECTORY, TEST_CORRECT_PROJECT_NAME }, };
 
 	private static final Object[][] HTTP_QUERY_PARAMETERS_WITH_PREFIX_SLASH_URL = {
 			{ SCMConfigurableElement.REPO_URL, TEST_MOCK_URL },
 			{ SCMConfigurableElement.SCM_KIND, TEST_SCM_KIND },
 			{ SCMConfigurableElement.BASE_DIRECTORY,
 					TEST_CORRECT_BASE_DIRECTORY_WITH_PREFIX_SLASH }, };
 
 	private static final Object[][] HTTP_QUERY_PARAMETERS_WITH_NO_BASE_DIR = {
 			{ SCMConfigurableElement.REPO_URL, TEST_MOCK_URL },
 			{ SCMConfigurableElement.SCM_KIND, TEST_SCM_KIND }, };
 
 	private static final Object[][] HTTP_QUERY_PARAMETERS_WITH_ROOT_URL = {
 			{ SCMConfigurableElement.REPO_URL, TEST_MOCK_URL },
 			{ SCMConfigurableElement.SCM_KIND, TEST_SCM_KIND },
 			{ SCMConfigurableElement.BASE_DIRECTORY, TEST_ROOT_PATH }, };
 
 	private static final Object[][] HTTP_QUERY_PARAMETERS_WITH_TOO_LONG_URL = {
 			{ SCMConfigurableElement.REPO_URL, TEST_MOCK_URL },
 			{ SCMConfigurableElement.SCM_KIND, TEST_SCM_KIND },
 			{ SCMConfigurableElement.BASE_DIRECTORY,
 					TEST_TOO_LONG_BASE_DIRECTORY }, };
 
 	private static final Object[][] HTTP_QUERY_PARAMETERS_INCORRECT_BASE_DIR = {
 			{ SCMConfigurableElement.REPO_URL, TEST_MOCK_URL },
 			{ SCMConfigurableElement.SCM_KIND, TEST_SCM_KIND },
 			{ SCMConfigurableElement.BASE_DIRECTORY,
 					TEST_INCORRECT_BASE_DIRECTORY }, };
 
 	@Before
 	public void setUp() {
 		workspace = new MockWorkSpace();
 	}
 
 	@Test
 	public void workSpaceShouldBeUpdatedWhenNormalSCMConfigurationIsPassed()
 			throws ExecutionException {
 		SCMURLConfiguration config = createConfiguration(NORMAL_HTTP_QUERY_PARAMETERS);
 		ExecutionEvent event = createMockExecutionEvent(config);
 
 		IPath mockPath = Path.fromOSString(TEST_CORRECT_PROJECT_NAME);
 		RepoFolder mockFolder = (RepoFolder) RepoModelTestingUtils
 				.createMockRepoResourceFromPath(TEST_SCM_KIND, TEST_MOCK_URL,
 						TEST_USER, TEST_PASSWORD, mockPath, false);
 
 		OpenRepoFromURLCommandHandler handler = new OpenRepoFromURLCommandHandler(
 				workspace, mockFolder.getRoot().getRepo());
 		handler.execute(event);
 		RepoProject baseDir = workspace.getProject();
 		assertNotNull(baseDir);
 		assertEquals(TEST_SCM_KIND, baseDir.getRoot().getRepo().getKind());
 		assertEquals(TEST_USER, baseDir.getRoot().getRepo().getCredential()
 				.getUser());
 		assertEquals(TEST_PASSWORD, baseDir.getRoot().getRepo().getCredential()
 				.getPassword());
 		assertEquals(TEST_CORRECT_PROJECT_NAME + "@" + TEST_MOCK_URL,
 				baseDir.getName());
 	}
 
 	@Test
 	public void workSpaceShouldBeUpdatedIfUrlContainsPrefixSlash()
 			throws ExecutionException {
 		SCMURLConfiguration config = createConfiguration(HTTP_QUERY_PARAMETERS_WITH_PREFIX_SLASH_URL);
 		ExecutionEvent event = createMockExecutionEvent(config);
 		IPath mockPath = Path.fromOSString(TEST_CORRECT_PROJECT_NAME);
 		RepoFolder mockFolder = (RepoFolder) RepoModelTestingUtils
 				.createMockRepoResourceFromPath(TEST_SCM_KIND, TEST_MOCK_URL,
 						TEST_USER, TEST_PASSWORD, mockPath, false);
 
 		OpenRepoFromURLCommandHandler handler = new OpenRepoFromURLCommandHandler(
 				workspace, mockFolder.getRoot().getRepo());
 		handler.execute(event);
 		RepoResource baseDir = workspace.getProject();
 		assertEquals(TEST_CORRECT_PROJECT_NAME + "@" + TEST_MOCK_URL,
 				baseDir.getName());
 	}
 
 	@Test
 	public void repoRootShouldBeReturnedIfRootPathIsPassed()
 			throws ExecutionException {
 		SCMURLConfiguration config = createConfiguration(HTTP_QUERY_PARAMETERS_WITH_ROOT_URL);
 		ExecutionEvent event = createMockExecutionEvent(config);
 
 		IPath mockPath = Path.fromOSString(TEST_CORRECT_PROJECT_NAME);
 		RepoFolder mockFolder = (RepoFolder) RepoModelTestingUtils
 				.createMockRepoResourceFromPath(TEST_SCM_KIND, TEST_MOCK_URL,
 						TEST_USER, TEST_PASSWORD, mockPath, false);
 
 		OpenRepoFromURLCommandHandler handler = new OpenRepoFromURLCommandHandler(
 				workspace, mockFolder.getRoot().getRepo());
 		handler.execute(event);
 		RepoResource baseDir = workspace.getProject();
 		assertTrue(baseDir instanceof RepoProject);
 	}
 
 	@Test
 	public void repoRootShouldBeReturnedIfNoPathIsPassed()
 			throws ExecutionException {
 		SCMURLConfiguration config = createConfiguration(HTTP_QUERY_PARAMETERS_WITH_NO_BASE_DIR);
 		ExecutionEvent event = createMockExecutionEvent(config);
 
 		IPath mockPath = Path.fromOSString(TEST_CORRECT_PROJECT_NAME);
 		RepoFolder mockFolder = (RepoFolder) RepoModelTestingUtils
 				.createMockRepoResourceFromPath(TEST_SCM_KIND, TEST_MOCK_URL,
 						TEST_USER, TEST_PASSWORD, mockPath, false);
 
 		OpenRepoFromURLCommandHandler handler = new OpenRepoFromURLCommandHandler(
 				workspace, mockFolder.getRoot().getRepo());
 		handler.execute(event);
 		RepoResource baseDir = workspace.getProject();
 		assertTrue(baseDir instanceof RepoProject);
 	}
 
 	@Test(expected = UnsupportedOperationException.class)
 	public void throwExceptionIfThePathHasDepthLargerThan2()
 			throws ExecutionException {
 		SCMURLConfiguration config = createConfiguration(HTTP_QUERY_PARAMETERS_WITH_TOO_LONG_URL);
 		ExecutionEvent event = createMockExecutionEvent(config);
 
 		IPath mockPath = Path.fromOSString(TEST_CORRECT_PROJECT_NAME);
 		RepoFolder mockFolder = (RepoFolder) RepoModelTestingUtils
 				.createMockRepoResourceFromPath(TEST_SCM_KIND, TEST_MOCK_URL,
 						TEST_USER, TEST_PASSWORD, mockPath, false);
 
 		OpenRepoFromURLCommandHandler handler = new OpenRepoFromURLCommandHandler(
 				workspace, mockFolder.getRoot().getRepo());
 		handler.execute(event);
 	}
 
 	@Test(expected = SCMResponseException.class)
 	public void throwExceptionIfThePathIsIncorrect() throws ExecutionException {
 		SCMURLConfiguration config = createConfiguration(HTTP_QUERY_PARAMETERS_INCORRECT_BASE_DIR);
 		ExecutionEvent event = createMockExecutionEvent(config);
 
 		IPath mockPath = Path.fromOSString(TEST_CORRECT_PROJECT_NAME);
 		RepoFolder mockFolder = (RepoFolder) RepoModelTestingUtils
 				.createMockRepoResourceFromPath(TEST_SCM_KIND, TEST_MOCK_URL,
 						TEST_USER, TEST_PASSWORD, mockPath, false);
 
 		OpenRepoFromURLCommandHandler handler = new OpenRepoFromURLCommandHandler(
 				workspace, mockFolder.getRoot().getRepo());
 		handler.execute(event);
 	}
 
 	private SCMURLConfiguration createConfiguration(Object[][] configs) {
 		SCMURLConfiguration configuration = new SCMURLConfiguration();
 		for (int i = 0; i < configs.length; i++) {
 			configuration.put((SCMConfigurableElement) configs[i][0],
 					(String) configs[i][1]);
 		}
 		return configuration;
 	}
 
 	private ExecutionEvent createMockExecutionEvent(SCMURLConfiguration config) {
 		EvaluationContext context = new EvaluationContext(null, new Object());
 		context.addVariable(
 				OpenRepoFromURLCommandHandler.VARIABLE_SCM_URL_CONFIGUTATION,
 				config);
 		ExecutionEvent event = new ExecutionEvent(null,
 				new HashMap<Object, Object>(), null, context);
 		return event;
 	}
 
 }
