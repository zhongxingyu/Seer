 package org.zend.sdk.test.sdkcli.commands;
 
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertTrue;
 import static org.mockito.Mockito.doReturn;
 import static org.mockito.Mockito.spy;
 import static org.mockito.Mockito.when;
 
 import java.io.IOException;
 
 import org.junit.Test;
 import org.zend.sdk.test.AbstractWebApiTest;
 import org.zend.sdkcli.CommandFactory;
 import org.zend.sdkcli.ParseError;
 import org.zend.sdkcli.internal.commands.CommandLine;
 import org.zend.sdkcli.internal.commands.ListApplicationsCommand;
 import org.zend.webapi.core.WebApiException;
 import org.zend.webapi.core.connection.data.ApplicationsList;
 import org.zend.webapi.core.connection.data.IResponseData;
 import org.zend.webapi.internal.core.connection.auth.signature.SignatureException;
 
 public class TestListApplicationsCommand extends AbstractWebApiTest {
 
 	private String[] validCommand = new String[] { "list", "applications",
 			"-t", "0" };
 
 	@Test
 	public void testExecute() throws ParseError, WebApiException, IOException {
 		CommandLine cmdLine = new CommandLine(validCommand);
 		ListApplicationsCommand command = getCommand(cmdLine);
 		assertNotNull(command);
 		doReturn(application).when(command).getApplication();
		when(client.applicationGetStatus()).thenReturn(
 				(ApplicationsList) getResponseData("applicationGetStatus",
 						IResponseData.ResponseType.APPLICATIONS_LIST));
 		assertTrue(command.execute(cmdLine));
 	}
 
 	@Test
 	public void testExecuteTargetDisconnected() throws ParseError,
 			WebApiException, IOException {
 		CommandLine cmdLine = new CommandLine(validCommand);
 		ListApplicationsCommand command = getCommand(cmdLine);
 		assertNotNull(command);
 		doReturn(application).when(command).getApplication();
 		when(client.applicationGetStatus()).thenThrow(
 				new SignatureException("testError"));
 		assertFalse(command.execute(cmdLine));
 	}
 
 	private ListApplicationsCommand getCommand(CommandLine cmdLine) throws ParseError {
 		ListApplicationsCommand command = spy((ListApplicationsCommand) CommandFactory
 				.createCommand(cmdLine));
 		return command;
 	}
 
 }
