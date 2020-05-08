 package org.jboss.pressgang.ccms.contentspec.client.commands;
 
 import static org.hamcrest.Matchers.is;
 import static org.hamcrest.Matchers.not;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertNull;
 import static org.junit.Assert.assertThat;
 import static org.junit.Assert.assertTrue;
 import static org.junit.Assert.fail;
 import static org.junit.matchers.JUnitMatchers.containsString;
 import static org.mockito.BDDMockito.given;
 import static org.mockito.Matchers.any;
 import static org.mockito.Matchers.anyBoolean;
 import static org.mockito.Matchers.anyInt;
 import static org.mockito.Matchers.anyMap;
 import static org.mockito.Matchers.anyString;
 import static org.mockito.Matchers.eq;
 import static org.mockito.Matchers.refEq;
 import static org.mockito.Mockito.doThrow;
 import static org.mockito.Mockito.mock;
 import static org.mockito.Mockito.spy;
 import static org.mockito.Mockito.times;
 import static org.mockito.Mockito.verify;
 import static org.mockito.Mockito.when;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.Map;
 
 import com.beust.jcommander.JCommander;
 import com.redhat.j2koji.exceptions.KojiException;
 import net.sf.ipsedixit.annotation.Arbitrary;
 import net.sf.ipsedixit.annotation.ArbitraryString;
 import net.sf.ipsedixit.core.StringType;
 import org.apache.commons.io.FileUtils;
 import org.jboss.pressgang.ccms.contentspec.ContentSpec;
 import org.jboss.pressgang.ccms.contentspec.builder.BuildType;
 import org.jboss.pressgang.ccms.contentspec.builder.ContentSpecBuilder;
 import org.jboss.pressgang.ccms.contentspec.builder.exception.BuildProcessingException;
 import org.jboss.pressgang.ccms.contentspec.builder.exception.BuilderCreationException;
 import org.jboss.pressgang.ccms.contentspec.builder.structures.CSDocbookBuildingOptions;
 import org.jboss.pressgang.ccms.contentspec.client.BaseUnitTest;
 import org.jboss.pressgang.ccms.contentspec.client.commands.base.BaseCommand;
 import org.jboss.pressgang.ccms.contentspec.client.commands.base.TestUtil;
 import org.jboss.pressgang.ccms.contentspec.client.config.ClientConfiguration;
 import org.jboss.pressgang.ccms.contentspec.client.config.ContentSpecConfiguration;
 import org.jboss.pressgang.ccms.contentspec.client.config.ZanataServerConfiguration;
 import org.jboss.pressgang.ccms.contentspec.client.constants.Constants;
 import org.jboss.pressgang.ccms.contentspec.client.entities.ConfigDefaults;
 import org.jboss.pressgang.ccms.contentspec.client.utils.ClientUtilities;
 import org.jboss.pressgang.ccms.contentspec.processor.ContentSpecParser;
 import org.jboss.pressgang.ccms.contentspec.processor.ContentSpecProcessor;
 import org.jboss.pressgang.ccms.contentspec.utils.CSTransformer;
 import org.jboss.pressgang.ccms.contentspec.utils.EntityUtilities;
 import org.jboss.pressgang.ccms.contentspec.utils.logging.ErrorLoggerManager;
 import org.jboss.pressgang.ccms.provider.BlobConstantProvider;
 import org.jboss.pressgang.ccms.provider.ContentSpecProvider;
 import org.jboss.pressgang.ccms.provider.RESTProviderFactory;
 import org.jboss.pressgang.ccms.provider.TopicProvider;
 import org.jboss.pressgang.ccms.provider.UserProvider;
 import org.jboss.pressgang.ccms.utils.common.DocBookUtilities;
 import org.jboss.pressgang.ccms.utils.common.FileUtilities;
 import org.jboss.pressgang.ccms.wrapper.CSNodeWrapper;
 import org.jboss.pressgang.ccms.wrapper.ContentSpecWrapper;
 import org.jboss.pressgang.ccms.wrapper.TranslatedContentSpecWrapper;
 import org.jboss.pressgang.ccms.wrapper.UserWrapper;
 import org.jboss.pressgang.ccms.wrapper.collection.CollectionWrapper;
 import org.jboss.pressgang.ccms.wrapper.collection.UpdateableCollectionWrapper;
 import org.jboss.pressgang.ccms.zanata.ZanataDetails;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Rule;
 import org.junit.Test;
 import org.junit.contrib.java.lang.system.ExpectedSystemExit;
 import org.junit.contrib.java.lang.system.internal.CheckExitCalled;
 import org.mockito.ArgumentCaptor;
 import org.mockito.Mock;
 import org.powermock.api.mockito.PowerMockito;
 import org.powermock.core.classloader.annotations.PrepareForTest;
 import org.powermock.modules.junit4.rule.PowerMockRule;
 
 @PrepareForTest({RESTProviderFactory.class, ClientUtilities.class, FileUtilities.class, DocBookUtilities.class, CSTransformer.class,
         EntityUtilities.class})
 public class BuildCommandTest extends BaseUnitTest {
     private static final String BOOK_TITLE = "Test";
     private static final String DUMMY_SPEC_FILE = "Test.contentspec";
 
     @Rule public PowerMockRule rule = new PowerMockRule();
     @Rule public final ExpectedSystemExit exit = ExpectedSystemExit.none();
 
     @Arbitrary Integer id;
     @Arbitrary Integer randomNumber;
     @Arbitrary String randomString;
     @ArbitraryString(type = StringType.ALPHANUMERIC) String username;
     @Mock JCommander parser;
     @Mock ContentSpecConfiguration cspConfig;
     @Mock ClientConfiguration clientConfig;
     @Mock RESTProviderFactory providerFactory;
     @Mock ContentSpecProvider contentSpecProvider;
     @Mock ContentSpecWrapper contentSpecWrapper;
     @Mock TranslatedContentSpecWrapper translatedContentSpecWrapper;
     @Mock UpdateableCollectionWrapper<CSNodeWrapper> contentSpecChildren;
     @Mock ContentSpec contentSpec;
     @Mock UserProvider userProvider;
     @Mock CollectionWrapper<UserWrapper> users;
     @Mock UserWrapper user;
     @Mock TopicProvider topicProvider;
     @Mock BlobConstantProvider blobConstantProvider;
     @Mock ConfigDefaults defaults;
 
     BuildCommand command;
     File rootTestDirectory;
     File bookDir;
 
     @Before
     public void setUp() {
         bindStdOut();
         PowerMockito.mockStatic(RESTProviderFactory.class);
         when(RESTProviderFactory.create(anyString())).thenReturn(providerFactory);
         when(providerFactory.getProvider(ContentSpecProvider.class)).thenReturn(contentSpecProvider);
         when(providerFactory.getProvider(TopicProvider.class)).thenReturn(topicProvider);
         when(providerFactory.getProvider(UserProvider.class)).thenReturn(userProvider);
         when(providerFactory.getProvider(BlobConstantProvider.class)).thenReturn(blobConstantProvider);
         when(clientConfig.getDefaults()).thenReturn(defaults);
         command = spy(new BuildCommand(parser, cspConfig, clientConfig));
 
         // Authentication is tested in the base implementation so assume all users are valid
         TestUtil.setUpAuthorisedUser(command, userProvider, users, user, username);
 
         rootTestDirectory = FileUtils.toFile(ClassLoader.getSystemResource(""));
         when(cspConfig.getRootOutputDirectory()).thenReturn(rootTestDirectory.getAbsolutePath() + File.separator);
 
         // Make the book title directory
         bookDir = new File(rootTestDirectory, BOOK_TITLE);
         bookDir.mkdir();
     }
 
     @After
     public void cleanUp() throws IOException {
         FileUtils.deleteDirectory(bookDir);
     }
 
     @Test
     public void shouldFailWithNoIds() {
         // Given a command with no ids
         command.setIds(new ArrayList<String>());
         // and no csprocessor.cfg data
         given(cspConfig.getContentSpecId()).willReturn(null);
 
         // When it is processed
         try {
             command.process();
             // Then an error is printed and the program is shut down
             fail(SYSTEM_EXIT_ERROR);
         } catch (CheckExitCalled e) {
             assertThat(e.getStatus(), is(5));
         }
 
         // Then the command should be shutdown and an error message printed
         assertThat(getStdOutLogs(), containsString("No ID was specified by the command line or a csprocessor.cfg file."));
     }
 
     @Test
     public void shouldFailWithInvalidLanguage() {
         // Given a command with no ids
         command.setIds(new ArrayList<String>());
         // and no csprocessor.cfg data
         given(cspConfig.getContentSpecId()).willReturn(id);
         // and a language is set
         command.setLocale("blah");
         // and the validation will fail
         PowerMockito.mockStatic(ClientUtilities.class);
         PowerMockito.doReturn(false).when(ClientUtilities.class);
         ClientUtilities.validateLanguage(eq(command), eq(providerFactory), anyString());
 
         // When it is processed
         try {
             command.process();
             // Then an error is printed and the program is shut down
             fail(SYSTEM_EXIT_ERROR);
         } catch (CheckExitCalled e) {
             assertThat(e.getStatus(), is(5));
         }
     }
 
     @Test
     public void shouldFailWhenContentSpecFileDoesntExist() {
         final String dummySpecFile = bookDir.getAbsolutePath() + File.separator + DUMMY_SPEC_FILE;
         // Given a command with a file that doesn't exist
         command.setIds(Arrays.asList(dummySpecFile));
 
         // When it is processed
         try {
             command.process();
             // Then an error is printed and the program is shut down
             fail(SYSTEM_EXIT_ERROR);
         } catch (CheckExitCalled e) {
             assertThat(e.getStatus(), is(-1));
         }
 
         // Then the command should be shutdown and an error message printed
         assertThat(getStdOutLogs(), containsString("The specified file was empty!"));
     }
 
     @Test
     public void shouldShutdownWhenContentSpecNotFound() {
         // Given no content spec exists
         given(contentSpecProvider.getContentSpec(anyInt(), anyInt())).willReturn(null);
 
         // When getting the content spec
         try {
             command.getContentSpec(id.toString());
             // Then an error is printed and the program is shut down
             fail(SYSTEM_EXIT_ERROR);
         } catch (CheckExitCalled e) {
             assertThat(e.getStatus(), is(-1));
         }
 
         // Then the command should be shutdown and an error message printed
         assertThat(getStdOutLogs(), containsString("No data was found for the specified ID!"));
     }
 
     @Test
     public void shouldPrintErrorAndShutdownWhenContentSpecHasNoValidVersion() {
         // Given a command called with an ID
         command.setIds(Arrays.asList(id.toString()));
         // And a matching content spec
         given(contentSpecProvider.getContentSpec(eq(id), anyInt())).willReturn(contentSpecWrapper);
         // and the content spec has no children
         final UpdateableCollectionWrapper<CSNodeWrapper> children = mock(UpdateableCollectionWrapper.class);
         given(contentSpecWrapper.getChildren()).willReturn(children);
         given(children.isEmpty()).willReturn(true);
 
         // When it is processed
         try {
             command.process();
             // Then an error is printed and the program is shut down
             fail(SYSTEM_EXIT_ERROR);
         } catch (CheckExitCalled e) {
             assertThat(e.getStatus(), is(-1));
         }
 
         // Then the command should be shutdown and an error message printed
         assertThat(getStdOutLogs(), containsString("No valid version exists on the server, please fix any errors and try again."));
     }
 
     @Test
     public void shouldWarnWhenLatestContentSpecHasErrors() {
         // Given a command called with an ID
         command.setIds(Arrays.asList(id.toString()));
         // And a matching content spec
         given(contentSpecProvider.getContentSpec(eq(id), anyInt())).willReturn(contentSpecWrapper);
         // and the content spec has errors
         given(contentSpecWrapper.getFailed()).willReturn(randomString);
         // and the content spec has no children as a way to finish the test
         given(contentSpecWrapper.getChildren()).willReturn(null);
 
         // When it is processed
         try {
             command.process();
             // Then an error is printed and the program is shut down
             fail(SYSTEM_EXIT_ERROR);
         } catch (CheckExitCalled e) {
             assertThat(e.getStatus(), is(-1));
         }
 
         // Then the command should be shutdown and an error message printed
         assertThat(getStdOutLogs(), containsString("The Content Specification has validation errors, so using the latest valid version."));
     }
 
     @Test
     public void shouldReturnContentSpecWhenUsingIdAndExists() {
         // Given a content spec exists
         given(contentSpecProvider.getContentSpec(anyInt(), anyInt())).willReturn(contentSpecWrapper);
         given(contentSpecWrapper.getChildren()).willReturn(contentSpecChildren);
         given(contentSpecChildren.isEmpty()).willReturn(false);
         // and the transform works
         PowerMockito.mockStatic(CSTransformer.class);
         when(CSTransformer.transform(eq(contentSpecWrapper), eq(providerFactory), anyBoolean())).thenReturn(contentSpec);
 
         // When getting the content spec
         final ContentSpec contentSpec = command.getContentSpec(id.toString());
 
         // Then verify that the content spec isn't null
         assertNotNull(contentSpec);
     }
 
     @Test
     public void shouldReturnContentSpecWhenUsingFileAndParses() {
         final String emptyFile = rootTestDirectory + File.separator + "EmptyFile.txt";
         // Given a File that exists
         PowerMockito.mockStatic(FileUtilities.class);
         when(FileUtilities.readFileContents(any(File.class))).thenReturn(randomString);
         // and the parse works
         PowerMockito.mockStatic(ClientUtilities.class);
         when(ClientUtilities.parseContentSpecString(eq(providerFactory), any(ErrorLoggerManager.class), anyString(),
                 any(ContentSpecParser.ParsingMode.class), anyBoolean())).thenReturn(contentSpec);
         // and call some real methods
         when(ClientUtilities.fixFilePath(anyString())).thenCallRealMethod();
 
         // When getting the content spec
         final ContentSpec contentSpec = command.getContentSpec(emptyFile);
 
         // Then verify that the content spec isn't null and permissive is false
         assertNotNull(contentSpec);
     }
 
     @Test
     public void shouldShutdownWhenContentSpecFailsToParse() {
         final String emptyFile = rootTestDirectory + File.separator + "EmptyFile.txt";
         // Given a File that exists
         PowerMockito.mockStatic(FileUtilities.class);
         when(FileUtilities.readFileContents(any(File.class))).thenReturn(randomString);
         // and the parse works
         PowerMockito.mockStatic(ClientUtilities.class);
         when(ClientUtilities.parseContentSpecString(eq(providerFactory), any(ErrorLoggerManager.class), anyString(),
                 any(ContentSpecParser.ParsingMode.class), anyBoolean())).thenReturn(null);
         // and call some real methods
         when(ClientUtilities.fixFilePath(anyString())).thenCallRealMethod();
 
         // When parsing the content spec
         try {
             command.getContentSpec(emptyFile);
             // Then an error is printed and the program is shut down
             fail(SYSTEM_EXIT_ERROR);
         } catch (CheckExitCalled e) {
             assertThat(e.getStatus(), is(8));
         }
 
         // Then verify that the output contains a message about failing to parse
         assertThat(getStdOutLogs(), containsString("The Content Specification is not valid."));
     }
 
     @Test
     public void shouldShutdownWhenContentSpecInvalid() {
         final ContentSpecProcessor processor = mock(ContentSpecProcessor.class);
         // Given a command with an id
         command.setIds(Arrays.asList(id.toString()));
         // and the content spec exists
         given(contentSpecProvider.getContentSpec(anyInt(), anyInt())).willReturn(contentSpecWrapper);
         given(contentSpecWrapper.getChildren()).willReturn(contentSpecChildren);
         given(contentSpecChildren.isEmpty()).willReturn(false);
         // and the transform works
         PowerMockito.mockStatic(CSTransformer.class);
         when(CSTransformer.transform(eq(contentSpecWrapper), eq(providerFactory), anyBoolean())).thenReturn(contentSpec);
         // and an invalid content spec
         given(processor.processContentSpec(any(ContentSpec.class), anyString(), any(ContentSpecParser.ParsingMode.class),
                 anyString())).willReturn(false);
         given(command.getCsp()).willReturn(processor);
 
         // When validating the content spec in process
         try {
             command.process();
             // Then an error is printed and the program is shut down
             fail(SYSTEM_EXIT_ERROR);
         } catch (CheckExitCalled e) {
             assertThat(e.getStatus(), is(8));
         }
     }
 
     @Test
     public void shouldGetPubsnumberFromKojiWhenSet() throws MalformedURLException, KojiException {
         final ContentSpecProcessor processor = mock(ContentSpecProcessor.class);
         // Given a command with an id
         command.setIds(Arrays.asList(id.toString()));
         // and the content spec exists
         given(contentSpecProvider.getContentSpec(anyInt(), anyInt())).willReturn(contentSpecWrapper);
         given(contentSpecWrapper.getChildren()).willReturn(contentSpecChildren);
         given(contentSpecChildren.isEmpty()).willReturn(false);
         // and the transform works
         PowerMockito.mockStatic(CSTransformer.class);
         final ContentSpec contentSpec = new ContentSpec();
         when(CSTransformer.transform(eq(contentSpecWrapper), eq(providerFactory), anyBoolean())).thenReturn(contentSpec);
         // and a valid content spec
         given(processor.processContentSpec(any(ContentSpec.class), anyString(), any(ContentSpecParser.ParsingMode.class),
                 anyString())).willReturn(true);
         given(command.getCsp()).willReturn(processor);
         // and the fetch pubsnumber is set
         command.setFetchPubsnum(true);
         PowerMockito.mockStatic(ClientUtilities.class);
         when(ClientUtilities.getPubsnumberFromKoji(any(ContentSpec.class), anyString())).thenReturn(randomNumber);
         // and we want to inject a place to stop processing
         command.setZanataUrl("test");
         doThrow(new CheckExitCalled(-2)).when(clientConfig).getZanataServers();
         // and the helper method to get the content spec works
         when(ClientUtilities.getContentSpecEntity(eq(contentSpecProvider), anyInt(), anyInt())).thenCallRealMethod();
         when(ClientUtilities.getContentSpecAsString(eq(contentSpecProvider), anyInt(), anyInt())).thenCallRealMethod();
 
         // When processing the command
         try {
             command.process();
             // Then an error is printed and the program is shut down
             fail(SYSTEM_EXIT_ERROR);
         } catch (CheckExitCalled e) {
             assertThat(e.getStatus(), is(-2));
         }
 
         // Then check that the pubsnumber has been set and the output has a message
         assertThat(getStdOutLogs(), containsString("Fetching the pubsnumber from " + Constants.KOJI_NAME + "..."));
         assertThat(contentSpec.getPubsNumber(), is(randomNumber));
     }
 
     @Test
     public void shouldShutdownWhenGetPubsnumberFromKojiFails() throws MalformedURLException, KojiException {
         final ContentSpecProcessor processor = mock(ContentSpecProcessor.class);
         // Given a command with an id
         command.setIds(Arrays.asList(id.toString()));
         // and the content spec exists
         given(contentSpecProvider.getContentSpec(anyInt(), anyInt())).willReturn(contentSpecWrapper);
         given(contentSpecWrapper.getChildren()).willReturn(contentSpecChildren);
         given(contentSpecChildren.isEmpty()).willReturn(false);
         // and the transform works
         PowerMockito.mockStatic(CSTransformer.class);
         final ContentSpec contentSpec = new ContentSpec();
         when(CSTransformer.transform(eq(contentSpecWrapper), eq(providerFactory), anyBoolean())).thenReturn(contentSpec);
         // and a valid content spec
         given(processor.processContentSpec(any(ContentSpec.class), anyString(), any(ContentSpecParser.ParsingMode.class),
                 anyString())).willReturn(true);
         given(command.getCsp()).willReturn(processor);
         // and the fetch pubsnumber is set
         command.setFetchPubsnum(true);
         PowerMockito.mockStatic(ClientUtilities.class);
         when(ClientUtilities.getPubsnumberFromKoji(any(ContentSpec.class), anyString())).thenThrow(new KojiException(""));
         // and the helper method to get the content spec works
         when(ClientUtilities.getContentSpecEntity(eq(contentSpecProvider), anyInt(), anyInt())).thenCallRealMethod();
         when(ClientUtilities.getContentSpecAsString(eq(contentSpecProvider), anyInt(), anyInt())).thenCallRealMethod();
 
         // When processing the command
         try {
             command.process();
             // Then an error is printed and the program is shut down
             fail(SYSTEM_EXIT_ERROR);
         } catch (CheckExitCalled e) {
             assertThat(e.getStatus(), is(-1));
         }
 
         // Then check that an error was printed
         assertThat(getStdOutLogs(), containsString("Fetching the pubsnumber from " + Constants.KOJI_NAME + "..."));
         assertThat(getStdOutLogs(), containsString("An error occurred when fetching the pubsnumber from " + Constants.KOJI_NAME + "."));
     }
 
     @Test
     public void shouldShutdownWhenGetPubsnumberFromKojiWithInvalidURL() throws MalformedURLException, KojiException {
         final ContentSpecProcessor processor = mock(ContentSpecProcessor.class);
         // Given a command with an id
         command.setIds(Arrays.asList(id.toString()));
         // and the content spec exists
         given(contentSpecProvider.getContentSpec(anyInt(), anyInt())).willReturn(contentSpecWrapper);
         given(contentSpecWrapper.getChildren()).willReturn(contentSpecChildren);
         given(contentSpecChildren.isEmpty()).willReturn(false);
         // and the transform works
         PowerMockito.mockStatic(CSTransformer.class);
         final ContentSpec contentSpec = new ContentSpec();
         when(CSTransformer.transform(eq(contentSpecWrapper), eq(providerFactory), anyBoolean())).thenReturn(contentSpec);
         // and a valid content spec
         given(processor.processContentSpec(any(ContentSpec.class), anyString(), any(ContentSpecParser.ParsingMode.class),
                 anyString())).willReturn(true);
         given(command.getCsp()).willReturn(processor);
         // and the fetch pubsnumber is set
         command.setFetchPubsnum(true);
         PowerMockito.mockStatic(ClientUtilities.class);
         when(ClientUtilities.getPubsnumberFromKoji(any(ContentSpec.class), anyString())).thenThrow(new MalformedURLException());
         // and the helper method to get the content spec works
         when(ClientUtilities.getContentSpecEntity(eq(contentSpecProvider), anyInt(), anyInt())).thenCallRealMethod();
         when(ClientUtilities.getContentSpecAsString(eq(contentSpecProvider), anyInt(), anyInt())).thenCallRealMethod();
 
         // When processing the command
         try {
             command.process();
             // Then an error is printed and the program is shut down
             fail(SYSTEM_EXIT_ERROR);
         } catch (CheckExitCalled e) {
             assertThat(e.getStatus(), is(4));
         }
 
         // Then check that an error was printed
         assertThat(getStdOutLogs(), containsString("Fetching the pubsnumber from " + Constants.KOJI_NAME + "..."));
         assertThat(getStdOutLogs(),
                 containsString("The " + Constants.KOJI_NAME + " Hub URL is invalid or is blank. Please ensure that the URL is valid."));
     }
 
     @Test
     public void shouldSetupZanataOptions() {
         final ContentSpecProcessor processor = mock(ContentSpecProcessor.class);
         final ZanataDetails details = new ZanataDetails();
         // Given a command with an id
         command.setIds(Arrays.asList(id.toString()));
         // and the content spec exists
         given(contentSpecProvider.getContentSpec(anyInt(), anyInt())).willReturn(contentSpecWrapper);
         // and the transform works
         PowerMockito.mockStatic(CSTransformer.class);
         final ContentSpec contentSpec = new ContentSpec();
         when(CSTransformer.transform(eq(contentSpecWrapper), eq(providerFactory), anyBoolean())).thenReturn(contentSpec);
         given(contentSpecWrapper.getChildren()).willReturn(contentSpecChildren);
         given(contentSpecChildren.isEmpty()).willReturn(false);
         // and a valid content spec
         given(processor.processContentSpec(any(ContentSpec.class), anyString(), any(ContentSpecParser.ParsingMode.class),
                 anyString())).willReturn(true);
         given(command.getCsp()).willReturn(processor);
         // and the cspconfig has some zanata details
         given(cspConfig.getZanataDetails()).willReturn(details);
         // and some zanata options are set via the command line
         command.setZanataUrl(randomString);
         command.setZanataProject(randomString);
         command.setZanataVersion(randomNumber.toString());
         // and the fixHostURL will return the input string
         PowerMockito.mockStatic(ClientUtilities.class);
         when(ClientUtilities.fixHostURL(anyString())).thenReturn(randomString);
         // and we make a way to kill the processing after the setup
         doThrow(new CheckExitCalled(-2)).when(command).getBuilder();
         // and the helper method to get the content spec works
         when(ClientUtilities.getContentSpecEntity(eq(contentSpecProvider), anyInt(), anyInt())).thenCallRealMethod();
         when(ClientUtilities.getContentSpecAsString(eq(contentSpecProvider), anyInt(), anyInt())).thenCallRealMethod();
 
         // When the command is processing
         try {
             command.process();
             // Then an error is printed and the program is shut down
             fail(SYSTEM_EXIT_ERROR);
         } catch (CheckExitCalled e) {
             assertThat(e.getStatus(), is(-2));
         }
 
         // Then the details should have been set for the zanata options
         assertThat(details.getServer(), is(randomString));
         assertThat(details.getProject(), is(randomString));
         assertThat(details.getVersion(), is(randomNumber.toString()));
     }
 
     @Test
     public void shouldSetZanataUrlWhenCommandLineOptionIsZanataServerName() {
         final ZanataDetails details = new ZanataDetails();
         final ZanataServerConfiguration zanataConfig = mock(ZanataServerConfiguration.class);
         // Given the cspconfig has some zanata details
         given(cspConfig.getZanataDetails()).willReturn(details);
         // and the client config has some servers
         HashMap<String, ZanataServerConfiguration> zanataServers = new HashMap<String, ZanataServerConfiguration>();
         zanataServers.put(randomString, zanataConfig);
         given(clientConfig.getZanataServers()).willReturn(zanataServers);
         // and the zanata config has a url
         String url = "http://www.example.com/";
         given(zanataConfig.getUrl()).willReturn(url);
         // and some zanata options are set via the command line
         command.setZanataUrl(randomString);
 
 
         // When setting up the zanata options
         command.setupZanataOptions();
 
         // Then the command should have the url set
         assertThat(command.getZanataUrl(), is(url));
     }
 
     @Test
     public void shouldShutdownOnBuildProcessingException() throws BuildProcessingException, BuilderCreationException {
         final ContentSpecProcessor processor = mock(ContentSpecProcessor.class);
         final ContentSpecBuilder builder = mock(ContentSpecBuilder.class);
         // Given a command with an id
         command.setIds(Arrays.asList(id.toString()));
         // and the content spec exists
         given(contentSpecProvider.getContentSpec(anyInt(), anyInt())).willReturn(contentSpecWrapper);
         given(contentSpecWrapper.getChildren()).willReturn(contentSpecChildren);
         given(contentSpecChildren.isEmpty()).willReturn(false);
         // and the transform works
         PowerMockito.mockStatic(CSTransformer.class);
         final ContentSpec contentSpec = new ContentSpec();
         when(CSTransformer.transform(eq(contentSpecWrapper), eq(providerFactory), anyBoolean())).thenReturn(contentSpec);
         // and a valid content spec
         given(processor.processContentSpec(any(ContentSpec.class), anyString(), any(ContentSpecParser.ParsingMode.class),
                 anyString())).willReturn(true);
         given(command.getCsp()).willReturn(processor);
         // and the builder will throw a builder processing exception
         given(builder.buildBook(any(ContentSpec.class), anyString(), any(CSDocbookBuildingOptions.class), anyMap(),
                 any(BuildType.class))).willThrow(new BuildProcessingException(""));
         given(command.getBuilder()).willReturn(builder);
 
         // When the command is processing
         try {
             command.process();
             // Then an error is printed and the program is shut down
             fail(SYSTEM_EXIT_ERROR);
         } catch (CheckExitCalled e) {
             assertThat(e.getStatus(), is(3));
         }
 
         // Then check an error message was printed
         assertThat(getStdOutLogs(), containsString("Starting to build..."));
        assertThat(getStdOutLogs(), containsString("Internal processing error!"));
     }
 
     @Test
     public void shouldShutdownOnBuilderCreationException() throws BuildProcessingException, BuilderCreationException {
         final ContentSpecProcessor processor = mock(ContentSpecProcessor.class);
         final ContentSpecBuilder builder = mock(ContentSpecBuilder.class);
         // Given a command with an id
         command.setIds(Arrays.asList(id.toString()));
         // and the content spec exists
         given(contentSpecProvider.getContentSpec(anyInt(), anyInt())).willReturn(contentSpecWrapper);
         given(contentSpecWrapper.getChildren()).willReturn(contentSpecChildren);
         given(contentSpecChildren.isEmpty()).willReturn(false);
         // and the transform works
         PowerMockito.mockStatic(CSTransformer.class);
         final ContentSpec contentSpec = new ContentSpec();
         when(CSTransformer.transform(eq(contentSpecWrapper), eq(providerFactory), anyBoolean())).thenReturn(contentSpec);
         // and a valid content spec
         given(processor.processContentSpec(any(ContentSpec.class), anyString(), any(ContentSpecParser.ParsingMode.class),
                 anyString())).willReturn(true);
         given(command.getCsp()).willReturn(processor);
         // and the builder will throw a builder processing exception
         given(builder.buildBook(any(ContentSpec.class), anyString(), any(CSDocbookBuildingOptions.class), anyMap(),
                 any(BuildType.class))).willThrow(new BuilderCreationException(""));
         given(command.getBuilder()).willReturn(builder);
 
         // When the command is processing
         try {
             command.process();
             // Then an error is printed and the program is shut down
             fail(SYSTEM_EXIT_ERROR);
         } catch (CheckExitCalled e) {
             assertThat(e.getStatus(), is(3));
         }
 
         // Then check an error message was printed
         assertThat(getStdOutLogs(), containsString("Starting to build..."));
         assertThat(getStdOutLogs(), containsString("Internal processing error!"));
     }
 
     @Test
     public void shouldReturnByteArrayOnSuccessfulBuild() throws BuildProcessingException, BuilderCreationException {
         final ContentSpecProcessor processor = mock(ContentSpecProcessor.class);
         final ContentSpecBuilder builder = mock(ContentSpecBuilder.class);
         final byte[] bookData = new byte[0];
         // Given a command with an id
         command.setIds(Arrays.asList(id.toString()));
         // and the content spec exists
         given(contentSpecProvider.getContentSpec(anyInt(), anyInt())).willReturn(contentSpecWrapper);
         given(contentSpecWrapper.getChildren()).willReturn(contentSpecChildren);
         given(contentSpecChildren.isEmpty()).willReturn(false);
         // and the transform works
         PowerMockito.mockStatic(CSTransformer.class);
         final ContentSpec contentSpec = new ContentSpec();
         when(CSTransformer.transform(eq(contentSpecWrapper), eq(providerFactory), anyBoolean())).thenReturn(contentSpec);
         // and a valid content spec
         given(processor.processContentSpec(any(ContentSpec.class), anyString(), any(ContentSpecParser.ParsingMode.class),
                 anyString())).willReturn(true);
         given(command.getCsp()).willReturn(processor);
         // and the builder will throw a builder processing exception
         given(builder.buildBook(any(ContentSpec.class), anyString(), any(CSDocbookBuildingOptions.class), any(BuildType.class))).willReturn(
                 bookData);
         given(command.getBuilder()).willReturn(builder);
         // and the builder has an error
         given(builder.getNumErrors()).willReturn(randomNumber);
         given(builder.getNumWarnings()).willReturn(0);
         // and we create a way to exit after building
         PowerMockito.mockStatic(DocBookUtilities.class);
         PowerMockito.doThrow(new CheckExitCalled(-2)).when(DocBookUtilities.class);
         DocBookUtilities.escapeTitle(anyString());
 
         // When the command is processing
         try {
             command.process();
             // Then an error is printed and the program is shut down
             fail(SYSTEM_EXIT_ERROR);
         } catch (CheckExitCalled e) {
             assertThat(e.getStatus(), is(-2));
         }
 
         // Then check the build method was called
         verify(builder).buildBook(any(ContentSpec.class), anyString(), any(CSDocbookBuildingOptions.class), anyMap(), any(BuildType.class));
         assertThat(getStdOutLogs(), containsString("Starting to build..."));
         assertThat(getStdOutLogs(),
                 containsString("Content Specification successfully built with " + randomNumber + " Errors and 0 Warnings"));
     }
 
     @Test
     public void shouldGetOverrideFilesFromFileSystemOnBuild() throws BuildProcessingException, BuilderCreationException {
         final ContentSpecProcessor processor = mock(ContentSpecProcessor.class);
         final ContentSpecBuilder builder = mock(ContentSpecBuilder.class);
         final byte[] bookData = new byte[0];
         // Given a command with an id
         command.setIds(Arrays.asList(id.toString()));
         // and an override
         final String emptyFilePath = rootTestDirectory.getAbsolutePath() + File.separator + "EmptyFile.txt";
         command.getOverrides().put("Author_Group.xml", emptyFilePath);
         // and the content spec exists
         given(contentSpecProvider.getContentSpec(anyInt(), anyInt())).willReturn(contentSpecWrapper);
         given(contentSpecWrapper.getChildren()).willReturn(contentSpecChildren);
         given(contentSpecChildren.isEmpty()).willReturn(false);
         // and the transform works
         PowerMockito.mockStatic(CSTransformer.class);
         final ContentSpec contentSpec = new ContentSpec();
         when(CSTransformer.transform(eq(contentSpecWrapper), eq(providerFactory), anyBoolean())).thenReturn(contentSpec);
         // and a valid content spec
         given(processor.processContentSpec(any(ContentSpec.class), anyString(), any(ContentSpecParser.ParsingMode.class),
                 anyString())).willReturn(true);
         given(command.getCsp()).willReturn(processor);
         // and the builder will throw a builder processing exception
         given(builder.buildBook(any(ContentSpec.class), anyString(), any(CSDocbookBuildingOptions.class), any(BuildType.class))).willReturn(
                 bookData);
         given(command.getBuilder()).willReturn(builder);
         // and the builder has an error
         given(builder.getNumErrors()).willReturn(randomNumber);
         given(builder.getNumWarnings()).willReturn(0);
         // and we create a way to exit after building
         PowerMockito.mockStatic(DocBookUtilities.class);
         PowerMockito.doThrow(new CheckExitCalled(-2)).when(DocBookUtilities.class);
         DocBookUtilities.escapeTitle(anyString());
 
         // When the command is processing
         ArgumentCaptor<Map> overrideFileCaptor = ArgumentCaptor.forClass(Map.class);
         try {
             command.process();
             // Then an error is printed and the program is shut down
             fail(SYSTEM_EXIT_ERROR);
         } catch (CheckExitCalled e) {
             assertThat(e.getStatus(), is(-2));
         }
 
         // Then check the build method was called
         verify(builder).buildBook(any(ContentSpec.class), anyString(), any(CSDocbookBuildingOptions.class), overrideFileCaptor.capture(),
                 any(BuildType.class));
         assertThat(getStdOutLogs(), containsString("Starting to build..."));
         assertThat(getStdOutLogs(),
                 containsString("Content Specification successfully built with " + randomNumber + " Errors and 0 Warnings"));
         // and that the override was passed with it's file
         assertThat(overrideFileCaptor.getValue().size(), is(1));
         assertTrue(overrideFileCaptor.getValue().containsKey("Author_Group.xml"));
     }
 
     @Test
     public void shouldReturnByteArrayOnSuccessfulTranslationBuild() throws BuildProcessingException, BuilderCreationException {
         final ContentSpecProcessor processor = mock(ContentSpecProcessor.class);
         final ContentSpecBuilder builder = mock(ContentSpecBuilder.class);
         final byte[] bookData = new byte[0];
         // Given a command with an id
         command.setIds(Arrays.asList(id.toString()));
         // and the content spec exists
         PowerMockito.mockStatic(EntityUtilities.class);
         when(EntityUtilities.getClosestTranslatedContentSpecById(eq(providerFactory), anyInt(), anyInt())).thenReturn(translatedContentSpecWrapper);
         given(translatedContentSpecWrapper.getContentSpec()).willReturn(contentSpecWrapper);
         given(contentSpecWrapper.getChildren()).willReturn(contentSpecChildren);
         given(contentSpecChildren.isEmpty()).willReturn(false);
         // and the transform works
         PowerMockito.mockStatic(CSTransformer.class);
         final ContentSpec contentSpec = new ContentSpec();
         when(CSTransformer.transform(eq(contentSpecWrapper), eq(providerFactory), anyBoolean())).thenReturn(contentSpec);
         // and a valid content spec
         given(processor.processContentSpec(any(ContentSpec.class), anyString(), any(ContentSpecParser.ParsingMode.class),
                 anyString())).willReturn(true);
         given(command.getCsp()).willReturn(processor);
         // and the builder will throw a builder processing exception
         given(builder.buildBook(any(ContentSpec.class), anyString(), any(CSDocbookBuildingOptions.class), any(BuildType.class))).willReturn(
                 bookData);
         given(command.getBuilder()).willReturn(builder);
         // and a locale is set
         command.setLocale("ja");
         // and the builder has no errors
         given(builder.getNumErrors()).willReturn(0);
         given(builder.getNumWarnings()).willReturn(0);
         // and we create a way to exit after building
         PowerMockito.mockStatic(DocBookUtilities.class);
         PowerMockito.doThrow(new CheckExitCalled(-2)).when(DocBookUtilities.class);
         DocBookUtilities.escapeTitle(anyString());
         // and the languages is valid
         PowerMockito.mockStatic(ClientUtilities.class);
         PowerMockito.doReturn(true).when(ClientUtilities.class);
         ClientUtilities.validateLanguage(any(BaseCommand.class), eq(providerFactory), anyString());
         // and the helper method to get the content spec works
         when(ClientUtilities.getContentSpecEntity(eq(contentSpecProvider), anyInt(), anyInt())).thenCallRealMethod();
         when(ClientUtilities.getContentSpecAsString(eq(contentSpecProvider), anyInt(), anyInt())).thenCallRealMethod();
 
         // When the command is processing
         try {
             command.process();
             // Then an error is printed and the program is shut down
             fail(SYSTEM_EXIT_ERROR);
         } catch (CheckExitCalled e) {
             assertThat(e.getStatus(), is(-2));
         }
 
         // Then check that the translated build method was called
         verify(builder).buildTranslatedBook(any(ContentSpec.class), anyString(), any(CSDocbookBuildingOptions.class), anyMap(),
                 any(ZanataDetails.class), any(BuildType.class));
         assertThat(getStdOutLogs(), containsString("Starting to build..."));
         assertThat(getStdOutLogs(),
                 containsString("Content Specification successfully built with 0 Errors and 0 Warnings - Flawless " + "Victory!"));
     }
 
     @Test
     public void shouldReturnByteArrayOnSuccessfulBuildFromCsprocessorCfg() throws BuildProcessingException, BuilderCreationException {
         final ContentSpecProcessor processor = mock(ContentSpecProcessor.class);
         final ContentSpecBuilder builder = mock(ContentSpecBuilder.class);
         final byte[] bookData = new byte[0];
         // Given a command with no id and a valid csprocessor.cfg
         given(cspConfig.getContentSpecId()).willReturn(id);
         // and the content spec exists
         given(contentSpecProvider.getContentSpec(anyInt(), anyInt())).willReturn(contentSpecWrapper);
         given(contentSpecWrapper.getChildren()).willReturn(contentSpecChildren);
         given(contentSpecChildren.isEmpty()).willReturn(false);
         // and the transform works
         PowerMockito.mockStatic(CSTransformer.class);
         final ContentSpec contentSpec = new ContentSpec();
         when(CSTransformer.transform(eq(contentSpecWrapper), eq(providerFactory), anyBoolean())).thenReturn(contentSpec);
         // and a valid content spec
         given(processor.processContentSpec(any(ContentSpec.class), anyString(), any(ContentSpecParser.ParsingMode.class),
                 anyString())).willReturn(true);
         given(command.getCsp()).willReturn(processor);
         // and the builder will throw a builder processing exception
         given(builder.buildBook(any(ContentSpec.class), anyString(), any(CSDocbookBuildingOptions.class), any(BuildType.class))).willReturn(
                 bookData);
         given(command.getBuilder()).willReturn(builder);
         // and the builder has an error
         given(builder.getNumErrors()).willReturn(randomNumber);
         given(builder.getNumWarnings()).willReturn(0);
         // and we create a way to exit after building
         PowerMockito.mockStatic(DocBookUtilities.class);
         PowerMockito.doThrow(new CheckExitCalled(-2)).when(DocBookUtilities.class);
         DocBookUtilities.escapeTitle(anyString());
 
         // When the command is processing
         try {
             command.process();
             // Then an error is printed and the program is shut down
             fail(SYSTEM_EXIT_ERROR);
         } catch (CheckExitCalled e) {
             assertThat(e.getStatus(), is(-2));
         }
 
         // Then check the build method was called
         verify(builder).buildBook(any(ContentSpec.class), anyString(), any(CSDocbookBuildingOptions.class), anyMap(), any(BuildType.class));
         assertThat(getStdOutLogs(), containsString("Starting to build..."));
         assertThat(getStdOutLogs(),
                 containsString("Content Specification successfully built with " + randomNumber + " Errors and 0 Warnings"));
     }
 
     @Test
     public void shouldShutdownWhenSaveBuildFails() throws BuildProcessingException, BuilderCreationException, IOException {
         final ContentSpecProcessor processor = mock(ContentSpecProcessor.class);
         final ContentSpecBuilder builder = mock(ContentSpecBuilder.class);
         final byte[] bookData = new byte[0];
         // Given a command with an id
         command.setIds(Arrays.asList(id.toString()));
         // and the content spec exists
         given(contentSpecProvider.getContentSpec(anyInt(), anyInt())).willReturn(contentSpecWrapper);
         given(contentSpecWrapper.getChildren()).willReturn(contentSpecChildren);
         given(contentSpecChildren.isEmpty()).willReturn(false);
         // and the transform works
         PowerMockito.mockStatic(CSTransformer.class);
         final ContentSpec contentSpec = new ContentSpec();
         when(CSTransformer.transform(eq(contentSpecWrapper), eq(providerFactory), anyBoolean())).thenReturn(contentSpec);
         // and a valid content spec
         given(processor.processContentSpec(any(ContentSpec.class), anyString(), any(ContentSpecParser.ParsingMode.class),
                 anyString())).willReturn(true);
         given(command.getCsp()).willReturn(processor);
         // and the builder will throw a builder processing exception
         given(builder.buildBook(any(ContentSpec.class), anyString(), any(CSDocbookBuildingOptions.class), any(BuildType.class))).willReturn(
                 bookData);
         given(command.getBuilder()).willReturn(builder);
         // and the builder has no errors
         given(builder.getNumErrors()).willReturn(0);
         given(builder.getNumWarnings()).willReturn(0);
         // and the output path is set to the test book dir
         command.setOutputPath(bookDir.getAbsolutePath());
         // and saving should fail
         PowerMockito.mockStatic(FileUtilities.class);
         PowerMockito.doThrow(new IOException()).when(FileUtilities.class);
         FileUtilities.saveFile(any(File.class), any(byte[].class));
 
         // When the command is processing
         try {
             command.process();
             // Then an error is printed and the program is shut down
             fail(SYSTEM_EXIT_ERROR);
         } catch (CheckExitCalled e) {
             assertThat(e.getStatus(), is(-1));
         }
 
         // Then an error should be printed
         assertThat(getStdOutLogs(), containsString("Starting to build..."));
         assertThat(getStdOutLogs(), containsString("An error occurred while trying to save the file."));
     }
 
     @Test
     public void shouldIgnoreOutputDirectoryWhenLoadingFromCsprocessorCfg() throws BuildProcessingException, BuilderCreationException,
             IOException {
         final ContentSpecProcessor processor = mock(ContentSpecProcessor.class);
         final ContentSpecBuilder builder = mock(ContentSpecBuilder.class);
         final byte[] bookData = new byte[0];
         // Given a command with no ids
         command.setIds(new ArrayList<String>());
         // and a csprocessor.cfg that has a content spec id
         given(cspConfig.getContentSpecId()).willReturn(id);
         // and the content spec exists
         given(contentSpecProvider.getContentSpec(anyInt(), anyInt())).willReturn(contentSpecWrapper);
         given(contentSpecWrapper.getChildren()).willReturn(contentSpecChildren);
         given(contentSpecChildren.isEmpty()).willReturn(false);
         // and the transform works
         PowerMockito.mockStatic(CSTransformer.class);
         when(CSTransformer.transform(eq(contentSpecWrapper), eq(providerFactory), anyBoolean())).thenReturn(contentSpec);
         // and the content spec has a title
         given(contentSpec.getTitle()).willReturn(BOOK_TITLE);
         // and a valid content spec
         given(processor.processContentSpec(any(ContentSpec.class), anyString(), any(ContentSpecParser.ParsingMode.class),
                 anyString())).willReturn(true);
         given(command.getCsp()).willReturn(processor);
         // and the builder will throw a builder processing exception
         given(builder.buildBook(any(ContentSpec.class), anyString(), any(CSDocbookBuildingOptions.class), any(BuildType.class))).willReturn(
                 bookData);
         given(command.getBuilder()).willReturn(builder);
         // and the builder has no errors
         given(builder.getNumErrors()).willReturn(0);
         given(builder.getNumWarnings()).willReturn(0);
         // and the output path is set to the test book dir
         command.setOutputPath(bookDir.getAbsolutePath());
         // and we create a way to end the program
         PowerMockito.mockStatic(FileUtilities.class);
         PowerMockito.doThrow(new CheckExitCalled(-2)).when(FileUtilities.class);
         FileUtilities.saveFile(any(File.class), any(byte[].class));
 
         // When the command is processing
         try {
             command.process();
             // Then an error is printed and the program is shut down
             fail(SYSTEM_EXIT_ERROR);
         } catch (CheckExitCalled e) {
             assertThat(e.getStatus(), is(-2));
         }
 
         // Then the output directory should be now be null
         assertNull(command.getOutputPath());
     }
 
     @Test
     public void shouldAskToOverwriteOutputFileIfExistsWhenNotBuildingFromCsprocessorcfg() {
         // Given a mocked file
         File mockFile = mock(File.class);
         // and the file exists
         given(mockFile.exists()).willReturn(true);
         // and we don't want to overwrite the file
         setStdInput("no\n");
         // and we are loading from the csprocessor.cfg
         boolean loadFromCsprocessorCfg = false;
 
         // When saving the output
         try {
             command.saveBuildToFile(new byte[0], mockFile, loadFromCsprocessorCfg);
             // Then an error is printed and the program is shut down
             fail(SYSTEM_EXIT_ERROR);
         } catch (CheckExitCalled e) {
             assertThat(e.getStatus(), is(-1));
         }
 
         // Then the output should have a question printed
         assertThat(getStdOutLogs(), containsString("already exists! Overwrite existing file (y/n)? "));
     }
 
     @Test
     public void shouldKeepAskingToOverwriteOutputFileIfExistsWhenNotBuildingFromCsprocessorcfgAndNotValidAnswer() {
         // Given a mocked file
         File mockFile = mock(File.class);
         // and the file exists
         given(mockFile.exists()).willReturn(true);
         // and the getName method return something
         given(mockFile.getName()).willReturn(randomString);
         // and we don't want to overwrite the file
         setStdInput("blah\nno\n");
         // and we are loading from the csprocessor.cfg
         boolean loadFromCsprocessorCfg = false;
 
         // When saving the output
         try {
             command.saveBuildToFile(new byte[0], mockFile, loadFromCsprocessorCfg);
             // Then an error is printed and the program is shut down
             fail(SYSTEM_EXIT_ERROR);
         } catch (CheckExitCalled e) {
             assertThat(e.getStatus(), is(-1));
         }
 
         // Then the output should have a question printed twice. The first should have the answer
         assertThat(getStdOutLogs(),
                 containsString(randomString + " already exists! Overwrite existing file (y/n)? \n" + randomString + " already exists!" +
                         " Overwrite existing file (y/n)? "));
     }
 
     @Test
     public void shouldOverwriteOutputFileIfExistsWhenNotBuildingFromCsprocessorcfg() throws IOException {
         // Given a mocked file
         File mockFile = mock(File.class);
         // and the file exists
         given(mockFile.exists()).willReturn(true);
         // and we want to overwrite the file
         setStdInput("y\n");
         // and we don't actually want to save anything
         PowerMockito.mockStatic(FileUtilities.class);
         PowerMockito.doNothing().when(FileUtilities.class);
         FileUtilities.saveFile(any(File.class), any(byte[].class));
         // and we are loading from the csprocessor.cfg
         boolean loadFromCsprocessorCfg = false;
 
         // When saving the output
         command.saveBuildToFile(new byte[0], mockFile, loadFromCsprocessorCfg);
 
         // Then the save file method should have been called
         PowerMockito.verifyStatic(times(1));
         FileUtilities.saveFile(eq(mockFile), any(byte[].class));
         assertThat(getStdOutLogs(), containsString("already exists! Overwrite existing file (y/n)? "));
         assertThat(getStdOutLogs(), containsString("Output saved to: "));
     }
 
     @Test
     public void shouldOverwriteOutputFileIfExistsAndNotAskWhenBuildingFromCsprocessorcfg() throws IOException {
         // Given a mocked file
         File mockFile = mock(File.class);
         // and the file exists
         given(mockFile.exists()).willReturn(true);
         // and we don't actually want to save anything
         PowerMockito.mockStatic(FileUtilities.class);
         PowerMockito.doNothing().when(FileUtilities.class);
         FileUtilities.saveFile(any(File.class), any(byte[].class));
         // and we are loading from the csprocessor.cfg
         boolean loadFromCsprocessorCfg = true;
 
         // When saving the output
         command.saveBuildToFile(new byte[0], mockFile, loadFromCsprocessorCfg);
 
         // Then the save file method should have been called and the overwrite question should not be asked
         PowerMockito.verifyStatic(times(1));
         FileUtilities.saveFile(eq(mockFile), any(byte[].class));
         assertThat(getStdOutLogs(), not(containsString("already exists! Overwrite existing file (y/n)? ")));
         assertThat(getStdOutLogs(), containsString("Output saved to: "));
     }
 
     @Test
     public void shouldReturnCorrectOutputFileWithNoOutputPath() {
         // Given a filename
         String filename = "EmptyFile.txt";
         // and no output path
         command.setOutputPath(null);
 
         // When getting the output file
         final File outputFile = command.getOutputFile(bookDir.getAbsolutePath(), filename);
 
         // Then check the file created is correct
         assertThat(outputFile.getAbsolutePath(), is(bookDir + File.separator + filename));
     }
 
     @Test
     public void shouldReturnCorrectOutputFileWithOutputPathAsDirectory() {
         // Given a filename
         String filename = "EmptyFile.txt";
         // and no output path
         command.setOutputPath(rootTestDirectory.getAbsolutePath());
 
         // When getting the output file
         final File outputFile = command.getOutputFile(BOOK_TITLE, filename);
 
         // Then check the file created is correct
         assertThat(outputFile.getAbsolutePath(), is(bookDir + File.separator + filename));
     }
 
     @Test
     public void shouldReturnCorrectOutputFileWithOutputPathAsFile() {
         // Given a filename
         String filename = "EmptyFile.txt";
         // and no output path
         command.setOutputPath(rootTestDirectory.getAbsolutePath() + File.separator + filename);
 
         // When getting the output file
         final File outputFile = command.getOutputFile(BOOK_TITLE, filename);
 
         // Then check the file created is correct
         assertThat(outputFile.getAbsolutePath(), is(rootTestDirectory.getAbsolutePath() + File.separator + filename));
     }
 
     @Test
     public void shouldFixRevHistoryAndAuthorGroupFilePaths() {
         // Given a command with a rev history and author group override
         HashMap<String, String> overrides = new HashMap<String, String>();
         overrides.put("Revision_History.xml", randomString);
         overrides.put("Author_Group.xml", randomNumber.toString());
         command.setOverrides(overrides);
         // and we want to mock the client util calls
         PowerMockito.mockStatic(ClientUtilities.class);
 
         // When fixing the overrides
         command.fixOverrides();
 
         // Then calls should have been made to the fixFilePath method
         PowerMockito.verifyStatic(times(1));
         ClientUtilities.fixFilePath(eq(randomString));
         PowerMockito.verifyStatic(times(1));
         ClientUtilities.fixFilePath(eq(randomNumber.toString()));
     }
 
     @Test
     public void shouldValidateServerUrlWithValidURL() {
         // Given a URL
         String url = "http://www.example.com";
         command.setServerUrl(url);
         // And that the URL will be valid
         PowerMockito.mockStatic(ClientUtilities.class);
         when(ClientUtilities.validateServerExists(anyString())).thenReturn(true);
 
         // When validating the server url
         boolean result = command.validateServerUrl();
 
         // Then the result from the method should be true
         assertThat(getStdOutLogs(), containsString("Connecting to PressGang server: " + url));
         assertTrue(result);
     }
 
     @Test
     public void shouldNotValidateServerUrlWithInvalidURL() {
         // Given a URL
         String url = "http://www.example.com";
         command.setServerUrl(url);
         // And that the URL will not be valid
         PowerMockito.mockStatic(ClientUtilities.class);
         when(ClientUtilities.validateServerExists(anyString())).thenReturn(false);
 
         // When validating the server url
         try {
             command.validateServerUrl();
         } catch (CheckExitCalled e) {
             assertThat(e.getStatus(), is(1));
         }
 
         // Then an error message should have been printed and a System.exit() called
         assertThat(getStdOutLogs(), containsString("Connecting to PressGang server: " + url));
         assertThat(getStdOutLogs(), containsString("Cannot connect to the server, as the server address can't be resolved."));
     }
 
     @Test
     public void shouldValidateKojiUrlWithValidURL() {
         // Given a URL
         String url = "http://www.example.com";
         command.setServerUrl(url);
         // and we are fetching from koji
         command.setFetchPubsnum(true);
         // and the koji url is set
         given(cspConfig.getKojiHubUrl()).willReturn(url);
         // And that the URL will be valid
         PowerMockito.mockStatic(ClientUtilities.class);
         when(ClientUtilities.validateServerExists(anyString())).thenReturn(true);
 
         // When validating the server url
         boolean result = command.validateServerUrl();
 
         // Then the result from the method should be true
         assertThat(getStdOutLogs(), containsString("Connecting to PressGang server: " + url));
         assertThat(getStdOutLogs(), containsString("Connecting to " + Constants.KOJI_NAME + "hub server: " + url));
         assertTrue(result);
     }
 
     @Test
     public void shouldNotValidateKojiUrlWithInvalidURL() {
         // Given a URL
         String url = "http://www.example.com";
         command.setServerUrl(url);
         // And that the URL will not be valid
         PowerMockito.mockStatic(ClientUtilities.class);
         when(ClientUtilities.validateServerExists(refEq(url))).thenReturn(true);
         // and we are fetching from koji
         command.setFetchPubsnum(true);
         // and the koji url is set
         String kojiUrl = "http://koji.example.com";
         given(cspConfig.getKojiHubUrl()).willReturn(kojiUrl);
         // And that the URL will be invalid
         when(ClientUtilities.validateServerExists(refEq(kojiUrl))).thenReturn(false);
 
         // When validating the server url
         try {
             command.validateServerUrl();
         } catch (CheckExitCalled e) {
             assertThat(e.getStatus(), is(1));
         }
 
         // Then an error message should have been printed and a System.exit() called
         assertThat(getStdOutLogs(), containsString("Connecting to PressGang server: " + url));
         assertThat(getStdOutLogs(), containsString("Connecting to " + Constants.KOJI_NAME + "hub server: " + kojiUrl));
         assertThat(getStdOutLogs(), containsString("Cannot connect to the server, as the server address can't be resolved."));
     }
 
     @Test
     public void shouldValidateZanataUrlWithValidURL() {
         final ZanataDetails zanataDetails = mock(ZanataDetails.class);
         // Given a URL
         String url = "http://www.example.com";
         command.setServerUrl(url);
         // and we are inserting links and have a locale
         command.setInsertEditorLinks(true);
         command.setLocale("ja");
         // and the zanata details are set
         given(cspConfig.getZanataDetails()).willReturn(zanataDetails);
         given(zanataDetails.returnUrl()).willReturn(url);
         // And that the URL will be valid
         PowerMockito.mockStatic(ClientUtilities.class);
         when(ClientUtilities.validateServerExists(anyString())).thenReturn(true);
 
         // When validating the server url
         boolean result = command.validateServerUrl();
 
         // Then the result from the method should be true
         assertThat(getStdOutLogs(), containsString("Connecting to PressGang server: " + url));
         assertTrue(result);
     }
 
     @Test
     public void shouldNotValidateZanataUrlWithInvalidURL() {
         final ZanataDetails zanataDetails = mock(ZanataDetails.class);
         // Given a URL
         String url = "http://www.example.com";
         command.setServerUrl(url);
         // And that the URL will not be valid
         PowerMockito.mockStatic(ClientUtilities.class);
         when(ClientUtilities.validateServerExists(refEq(url))).thenReturn(true);
         // and the zanata url is set
         String zanataUrl = "http://translate.zanata.org";
         // and we are inserting links and have a locale
         command.setInsertEditorLinks(true);
         command.setLocale("ja");
         // and the zanata details are set
         given(cspConfig.getZanataDetails()).willReturn(zanataDetails);
         given(zanataDetails.returnUrl()).willReturn(zanataUrl);
         given(zanataDetails.getProject()).willReturn(randomString);
         given(zanataDetails.getServer()).willReturn(zanataUrl);
         given(zanataDetails.getVersion()).willReturn(randomNumber.toString());
         // And that the URL will be invalid
         when(ClientUtilities.validateServerExists(refEq(zanataUrl))).thenReturn(false);
 
         // When validating the server url
         try {
             command.validateServerUrl();
         } catch (CheckExitCalled e) {
             assertThat(e.getStatus(), is(1));
         }
 
         // Then an error message should have been printed and a System.exit() called
         assertThat(getStdOutLogs(), containsString("Connecting to PressGang server: " + url));
         assertThat(getStdOutLogs(), containsString("No Zanata Project exists for the \"" + randomString + "\" project at version \"" +
                 randomNumber + "\" from: " + zanataUrl));
     }
 
     @Test
     public void shouldRequireAnExternalConnection() {
         // Given an already initialised command
 
         // When invoking the method
         boolean result = command.requiresExternalConnection();
 
         // Then the answer should be true
         assertTrue(result);
     }
 
     @Test
     public void shouldNotLoadFromCsprocessorCfgWhenIdsSpecified() {
         // Given a command with ids specified
         command.setIds(Arrays.asList(id.toString()));
 
         // When invoking the method
         boolean result = command.loadFromCSProcessorCfg();
 
         // Then the result should be false
         assertFalse(result);
     }
 
     @Test
     public void shouldReturnRightCommandName() {
         // Given
         // When getting the commands name
         String commandName = command.getCommandName();
 
         // Then the name should be "build"
         assertThat(commandName, is("build"));
     }
 }
