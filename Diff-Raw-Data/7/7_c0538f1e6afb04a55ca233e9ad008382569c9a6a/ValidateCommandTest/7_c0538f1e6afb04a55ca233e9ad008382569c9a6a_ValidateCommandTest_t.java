 package org.jboss.pressgang.ccms.contentspec.client.commands;
 
 import static org.hamcrest.Matchers.containsString;
 import static org.hamcrest.Matchers.is;
 import static org.jboss.pressgang.ccms.contentspec.client.commands.base.TestUtil.createRealFile;
 import static org.jboss.pressgang.ccms.contentspec.client.commands.base.TestUtil.createValidContentSpecString;
 import static org.jboss.pressgang.ccms.contentspec.client.commands.base.TestUtil.setUpAuthorisedUser;
 import static org.jboss.pressgang.ccms.contentspec.client.commands.base.TestUtil.setValidContentSpecMocking;
 import static org.jboss.pressgang.ccms.contentspec.client.commands.base.TestUtil.setValidContentSpecWrapperMocking;
 import static org.jboss.pressgang.ccms.contentspec.client.commands.base.TestUtil.setValidFileProperties;
 import static org.jboss.pressgang.ccms.contentspec.client.commands.base.TestUtil.setValidLevelMocking;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertThat;
 import static org.junit.Assert.assertTrue;
 import static org.junit.Assert.fail;
 import static org.mockito.BDDMockito.given;
 import static org.mockito.Matchers.any;
 import static org.mockito.Matchers.anyInt;
 import static org.mockito.Matchers.anyString;
 import static org.mockito.Matchers.eq;
 import static org.mockito.Mockito.when;
 
 import java.io.File;
 import java.util.Arrays;
 
 import com.beust.jcommander.JCommander;
 import net.sf.ipsedixit.annotation.Arbitrary;
 import net.sf.ipsedixit.annotation.ArbitraryString;
 import net.sf.ipsedixit.core.StringType;
 import org.jboss.pressgang.ccms.contentspec.ContentSpec;
 import org.jboss.pressgang.ccms.contentspec.Level;
 import org.jboss.pressgang.ccms.contentspec.client.BaseUnitTest;
 import org.jboss.pressgang.ccms.contentspec.client.config.ClientConfiguration;
 import org.jboss.pressgang.ccms.contentspec.client.config.ContentSpecConfiguration;
 import org.jboss.pressgang.ccms.contentspec.client.utils.ClientUtilities;
 import org.jboss.pressgang.ccms.contentspec.enums.LevelType;
 import org.jboss.pressgang.ccms.contentspec.utils.logging.ErrorLoggerManager;
 import org.jboss.pressgang.ccms.provider.ContentSpecProvider;
 import org.jboss.pressgang.ccms.provider.DataProviderFactory;
 import org.jboss.pressgang.ccms.provider.RESTProviderFactory;
 import org.jboss.pressgang.ccms.provider.TextContentSpecProvider;
 import org.jboss.pressgang.ccms.provider.TopicProvider;
 import org.jboss.pressgang.ccms.provider.UserProvider;
 import org.jboss.pressgang.ccms.utils.common.DocBookUtilities;
 import org.jboss.pressgang.ccms.utils.common.FileUtilities;
 import org.jboss.pressgang.ccms.wrapper.ContentSpecWrapper;
 import org.jboss.pressgang.ccms.wrapper.TextContentSpecWrapper;
 import org.jboss.pressgang.ccms.wrapper.UserWrapper;
 import org.jboss.pressgang.ccms.wrapper.base.BaseContentSpecWrapper;
 import org.jboss.pressgang.ccms.wrapper.collection.CollectionWrapper;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Rule;
 import org.junit.Test;
 import org.junit.contrib.java.lang.system.ExpectedSystemExit;
 import org.junit.contrib.java.lang.system.internal.CheckExitCalled;
 import org.mockito.Mock;
 import org.powermock.api.mockito.PowerMockito;
 import org.powermock.core.classloader.annotations.PrepareForTest;
 import org.powermock.modules.junit4.rule.PowerMockRule;
 
 @PrepareForTest({RESTProviderFactory.class, FileUtilities.class, ClientUtilities.class, DocBookUtilities.class})
 public class ValidateCommandTest extends BaseUnitTest {
     @Rule public PowerMockRule rule = new PowerMockRule();
     @Rule public final ExpectedSystemExit exit = ExpectedSystemExit.none();
 
     @Arbitrary Integer id;
     @ArbitraryString(type = StringType.ALPHANUMERIC) String username;
     @ArbitraryString(type = StringType.ALPHANUMERIC) String contentSpecString;
     @ArbitraryString(type = StringType.ALPHANUMERIC) String randomAlphanumString;
     @ArbitraryString(type = StringType.ALPHANUMERIC) String filename;
     @Mock JCommander parser;
     @Mock ContentSpecConfiguration cspConfig;
     @Mock ClientConfiguration clientConfig;
     @Mock RESTProviderFactory providerFactory;
     @Mock UserProvider userProvider;
     @Mock CollectionWrapper<UserWrapper> users;
     @Mock UserWrapper user;
     @Mock ContentSpecProvider contentSpecProvider;
     @Mock ContentSpecWrapper contentSpecWrapper;
     @Mock TextContentSpecProvider textContentSpecProvider;
     @Mock TextContentSpecWrapper textContentSpecWrapper;
     @Mock ContentSpec contentSpec;
     @Mock TopicProvider topicProvider;
     @Mock Level level;
     @Mock File file;
     @Mock File file2;
     private File realFile;
 
     private ValidateCommand command;
 
     @Before
     public void setUp() {
         bindStdOut();
         PowerMockito.mockStatic(RESTProviderFactory.class);
         when(RESTProviderFactory.create(anyString())).thenReturn(providerFactory);
         when(providerFactory.getProvider(ContentSpecProvider.class)).thenReturn(contentSpecProvider);
         when(providerFactory.getProvider(TextContentSpecProvider.class)).thenReturn(textContentSpecProvider);
         when(providerFactory.getProvider(UserProvider.class)).thenReturn(userProvider);
         this.command = new ValidateCommand(parser, cspConfig, clientConfig);
     }
 
     @After
     public void cleanUp() {
         if (realFile != null && realFile.exists() && (!realFile.delete())) {
             resetStdStreams();
             System.err.println("WARNING: Test file cleanup for " + realFile.getAbsolutePath() + "was unsuccessful");
         }
     }
 
 //    @Test
 //    public void shouldFailIfUserUnauthorised() {
 //        // Given a user who is unauthorised as they have no name
 //        command.setUsername("");
 //
 //        // When the ValidateCommand is processed
 //        try {
 //            command.process();
 //            // If we get here then the test failed
 //            fail(SYSTEM_EXIT_ERROR);
 //        } catch (CheckExitCalled e) {
 //            assertThat(e.getStatus(), is(2));
 //        }
 //
 //        // Then it should fail and the program should print an error and exit
 //        assertThat(getStdOutLogs(), containsString("No username was specified for the server."));
 //    }
 
     @Test
     public void shouldFailIfFileInvalid() {
         // Given multiple files to validate, which is an invalid case
         command.setFiles(Arrays.asList(file, file2));
         // And an authorised user
         setUpAuthorisedUser(command, userProvider, users, user, username);
 
         // When the ValidateCommand is processed
         try {
             command.process();
             // If we get here then the test failed
             fail(SYSTEM_EXIT_ERROR);
         } catch (CheckExitCalled e) {
             assertThat(e.getStatus(), is(-1));
         }
 
         // Then it should fail with an error message and exit
         assertThat(getStdOutLogs(), containsString("No file was found for the specified file name!"));
     }
 
     @Test
     public void shouldFailIfFileHasNoContent() {
         // Given a valid file to validate that has no content
         command.setFiles(Arrays.asList(file));
         setValidFileProperties(file);
         PowerMockito.mockStatic(FileUtilities.class);
         given(FileUtilities.readFileContents(file)).willReturn("");
         // And an authorised user
         setUpAuthorisedUser(command, userProvider, users, user, username);
 
         // When the ValidateCommand is processed
         try {
             command.process();
             // If we get here then the test failed
             fail(SYSTEM_EXIT_ERROR);
         } catch (CheckExitCalled e) {
             assertThat(e.getStatus(), is(-1));
         }
 
         // Then it should fail with an error message and exit
         assertThat(getStdOutLogs(), containsString("The specified file was empty!"));
     }
 
     @Test
     public void shouldPrintErrorLogsAndExitIfSpecStringParsingFails() {
         // Given a valid file with a content spec string that produces a null result when parsed due to an error
         command.setFiles(Arrays.asList(file));
         setValidFileProperties(file);
         PowerMockito.mockStatic(FileUtilities.class);
         given(FileUtilities.readFileContents(file)).willReturn(contentSpecString);
         // And an authorised user
         setUpAuthorisedUser(command, userProvider, users, user, username);
 
         // When the ValidateCommand is processed
         try {
             command.process();
             // If we get here then the test failed
             fail(SYSTEM_EXIT_ERROR);
         } catch (CheckExitCalled e) {
             assertThat(e.getStatus(), is(-1));
         }
 
         // Then error messages are printed, INVALID is written to the console and the program exits
         assertThat(getStdOutLogs(), containsString("Invalid Content Specification! Incorrect file format"));
     }
 
     @Test
     public void shouldPrintErrorLogsAndFailWhenInvalidSpec() {
         // Given a valid file with content spec that is invalid
         command.setFiles(Arrays.asList(file));
         setValidFileProperties(file);
         PowerMockito.mockStatic(FileUtilities.class);
         given(FileUtilities.readFileContents(file)).willReturn(contentSpecString);
         given(providerFactory.getProvider(TopicProvider.class)).willReturn(topicProvider);
         PowerMockito.mockStatic(ClientUtilities.class);
         given(ClientUtilities.parseContentSpecString(eq(providerFactory), any(ErrorLoggerManager.class), any(String.class))).willReturn(
                 contentSpec);
         given(contentSpec.getBaseLevel()).willReturn(new Level(randomAlphanumString, LevelType.BASE));
        given(contentSpec.getDefaultPublicanCfg()).willCallRealMethod();
         // And an authorised user
         setUpAuthorisedUser(command, userProvider, users, user, username);
 
         // When the ValidateCommand is processed
         try {
             command.process();
             // If we get here then the test failed
             fail(SYSTEM_EXIT_ERROR);
         } catch (CheckExitCalled e) {
             assertThat(e.getStatus(), is(8));
         }
 
         // Then error messages are printed, INVALID is written to the console and the program exits
         assertThat(getStdOutLogs(), containsString("Invalid Content Specification! No Title.")); // Just one of the missing fields
         assertThat(getStdOutLogs(), containsString("The Content Specification is not valid."));
         assertThat(getStdOutLogs(), containsString("INVALID"));
     }
 
     @Test
     public void shouldPrintValidationResult() {
         // Given a valid CSP
         command.setFiles(Arrays.asList(file));
         setValidFileProperties(file);
         PowerMockito.mockStatic(FileUtilities.class);
         given(FileUtilities.readFileContents(file)).willReturn(contentSpecString);
         given(contentSpecProvider.getContentSpec(anyInt(), anyInt())).willReturn(contentSpecWrapper);
         given(textContentSpecProvider.getTextContentSpec(id, null)).willReturn(textContentSpecWrapper);
         given(textContentSpecWrapper.getText()).willReturn(createValidContentSpecString(randomAlphanumString, id));
         given(providerFactory.getProvider(TopicProvider.class)).willReturn(topicProvider);
         PowerMockito.mockStatic(ClientUtilities.class);
         given(ClientUtilities.parseContentSpecString(any(DataProviderFactory.class), any(ErrorLoggerManager.class),
                 any(String.class))).willReturn(contentSpec);
         setValidLevelMocking(level, randomAlphanumString);
         setValidContentSpecMocking(contentSpec, level, randomAlphanumString, id);
         // And the wrapper has the basic data
         setValidContentSpecWrapperMocking(contentSpecWrapper, randomAlphanumString, id);
         // And an authorised user
         setUpAuthorisedUser(command, userProvider, users, user, username);
 
         // When the ValidateCommand is processed
         command.process();
 
         // Then VALID should be returned to the console
         assertThat(getStdOutLogs(), containsString("The Content Specification is valid."));
         assertThat(getStdOutLogs(), containsString("\nVALID"));
     }
 
     @Test
     public void shouldFailIfNoFileOrIdInCspConfig() {
         // Given no files are specified
         // And there is no id specified in CSP config
         given(cspConfig.getContentSpecId()).willReturn(null);
         // And an authorised user
         setUpAuthorisedUser(command, userProvider, users, user, username);
 
         // When the ValidateCommand is processed
         try {
             command.process();
             // If we get here then the test failed
             fail(SYSTEM_EXIT_ERROR);
         } catch (CheckExitCalled e) {
             assertThat(e.getStatus(), is(-1));
         }
 
         // Then it should fail with an error message and exit
         assertThat(getStdOutLogs(), containsString("No file was found for the specified file name!"));
     }
 
     @Test
     public void shouldProcessContentSpecFileFromCspConfig() throws Exception {
         // Given no files are specified as a parameter
         // And there is a valid .contentspec file with its ID specified in the CSP config
         given(cspConfig.getContentSpecId()).willReturn(id);
         given(contentSpecProvider.getContentSpec(id, null)).willReturn(contentSpecWrapper);
         given(textContentSpecProvider.getTextContentSpec(id, null)).willReturn(textContentSpecWrapper);
         given(textContentSpecWrapper.getText()).willReturn(createValidContentSpecString(randomAlphanumString, id));
         PowerMockito.mockStatic(DocBookUtilities.class);
         given(DocBookUtilities.escapeTitle(anyString())).willReturn(filename);
         String testFilename = filename + "-post.contentspec";
         realFile = createRealFile(testFilename, randomAlphanumString);
         // And an authorised user
         setUpAuthorisedUser(command, userProvider, users, user, username);
         // And valid values for validation
         given(providerFactory.getProvider(TopicProvider.class)).willReturn(topicProvider);
         PowerMockito.mockStatic(ClientUtilities.class);
         given(ClientUtilities.parseContentSpecString(any(DataProviderFactory.class), any(ErrorLoggerManager.class),
                 any(String.class))).willReturn(contentSpec);
         setValidLevelMocking(level, randomAlphanumString);
         setValidContentSpecMocking(contentSpec, level, randomAlphanumString, id);
         setValidContentSpecWrapperMocking(contentSpecWrapper, randomAlphanumString, id);
         // and the helper method to get the content spec works
         when(ClientUtilities.getContentSpecEntity(eq(contentSpecProvider), anyInt(), anyInt())).thenCallRealMethod();
         when(ClientUtilities.getContentSpecAsString(eq(contentSpecProvider), anyInt(), anyInt())).thenCallRealMethod();
         when(ClientUtilities.getEscapedContentSpecTitle(eq(providerFactory), any(BaseContentSpecWrapper.class))).thenCallRealMethod();
 
         // When the Validate Command is processed
         command.process();
 
         // Then a file with this name is added and processed
         assertThat(command.getFiles().get(0), is(realFile));
         assertThat(command.getFiles().get(0).getName(), is(testFilename));
     }
 
     @Test
     public void shouldProcessContentSpecFileFromCspConfigWithTxtExtension() throws Exception {
         // Given no files are specified as a parameter
         // And there is a valid .txt file with its ID specified in the CSP config but no .contentspec version
         given(cspConfig.getContentSpecId()).willReturn(id);
         given(contentSpecProvider.getContentSpec(id, null)).willReturn(contentSpecWrapper);
         given(textContentSpecProvider.getTextContentSpec(id, null)).willReturn(textContentSpecWrapper);
         given(textContentSpecWrapper.getText()).willReturn(createValidContentSpecString(randomAlphanumString, id));
         PowerMockito.mockStatic(DocBookUtilities.class);
         given(DocBookUtilities.escapeTitle(anyString())).willReturn(filename);
         String testFilename = filename + "-post.txt";
         realFile = createRealFile(testFilename, randomAlphanumString);
         // And an authorised user
         setUpAuthorisedUser(command, userProvider, users, user, username);
         // And valid values for validation
         given(providerFactory.getProvider(TopicProvider.class)).willReturn(topicProvider);
         PowerMockito.mockStatic(ClientUtilities.class);
         given(ClientUtilities.parseContentSpecString(any(DataProviderFactory.class), any(ErrorLoggerManager.class),
                 any(String.class))).willReturn(contentSpec);
         setValidLevelMocking(level, randomAlphanumString);
         setValidContentSpecMocking(contentSpec, level, randomAlphanumString, id);
         setValidContentSpecWrapperMocking(contentSpecWrapper, randomAlphanumString, id);
         // and the helper method to get the content spec works
         when(ClientUtilities.getContentSpecEntity(eq(contentSpecProvider), anyInt(), anyInt())).thenCallRealMethod();
         when(ClientUtilities.getContentSpecAsString(eq(contentSpecProvider), anyInt(), anyInt())).thenCallRealMethod();
         when(ClientUtilities.getEscapedContentSpecTitle(eq(providerFactory), any(BaseContentSpecWrapper.class))).thenCallRealMethod();
 
         // When the Validate Command is processed
         command.process();
 
         // Then a file with this name is added and processed
         assertThat(command.getFiles().get(0), is(realFile));
         assertThat(command.getFiles().get(0).getName(), is(testFilename));
     }
 
     @Test
     public void shouldFailIfCspConfigSpecIdDoesNotMapToExistingFile() {
         // Given no files are specified as a parameter
         // And there is an ID specifided in the CSP config but the spec file doesn't exist
         given(cspConfig.getContentSpecId()).willReturn(id);
         given(contentSpecProvider.getContentSpec(id, null)).willReturn(contentSpecWrapper);
         PowerMockito.mockStatic(DocBookUtilities.class);
         given(DocBookUtilities.escapeTitle(anyString())).willReturn(filename);
         // And an authorised user
         setUpAuthorisedUser(command, userProvider, users, user, username);
 
         // When the Validate Command is processed
         try {
             command.process();
             // If we get here then the test failed
             fail(SYSTEM_EXIT_ERROR);
         } catch (CheckExitCalled e) {
             assertThat(e.getStatus(), is(-1));
         }
 
         // Then it should fail with an error message and exit
         assertThat(getStdOutLogs(), containsString(filename));
         assertThat(getStdOutLogs(), containsString("was not found in the current directory"));
         // And no files were added for processing
         assertThat(command.getFiles().size(), is(0));
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
     public void shouldNotLoadFromCsprocessorCfgWhenFileSpecified() {
         // Given a command with a file
         command.setFiles(Arrays.asList(file));
 
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
 
         // Then the name should be "validate"
         assertThat(commandName, is("validate"));
     }
 }
