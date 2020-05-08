 package pl.edu.agh.two.mud.client.command.factory;
 
 import static org.junit.Assert.assertEquals;
 import static org.mockito.Mockito.mock;
 import static org.mockito.Mockito.when;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import pl.edu.agh.two.mud.client.command.DelegateToServerCommand;
 import pl.edu.agh.two.mud.common.command.Command;
 import pl.edu.agh.two.mud.common.command.IParsedCommand;
import pl.edu.agh.two.mud.common.command.factory.ReflexiveCommandFactoryTest;
 import pl.edu.agh.two.mud.common.command.provider.CommandProvider;
 
public class ClientCommandFactoryTest extends ReflexiveCommandFactoryTest {
 
 	private ClientCommandFactory factory;
 
 	private IParsedCommand parsedCommand;
 
 	private CommandProvider commandProvider;
 
 	@Before
 	public void prepareTest() {
 		factory = new ClientCommandFactory();
 		parsedCommand = mock(IParsedCommand.class);
 		commandProvider = mock(CommandProvider.class);
 		factory.setCommandProvider(commandProvider);
 	}
 
 	@Test
 	public void testExternalCommand() {
 		String commandId = "command";
 		when(parsedCommand.getCommandId()).thenReturn(commandId);
 		when(commandProvider.isCommandAvailable(commandId)).thenReturn(false);
 
 		Command command = factory.create(parsedCommand);
 		assertEquals(DelegateToServerCommand.class, command.getClass());
 		assertEquals(parsedCommand,
 				((DelegateToServerCommand) command).getParsedCommand());
 	}
 
 	@Test
 	public void testInternalCommand() {
 		class InnerCommand extends Command {
 		}
 
 		InnerCommand innerCommand = new InnerCommand();
 		String commandId = innerCommand.getClass().getName();
 		when(parsedCommand.getCommandId()).thenReturn(commandId);
 		when(commandProvider.isCommandAvailable(commandId)).thenReturn(true);
 		when(commandProvider.getCommandById(commandId))
 				.thenReturn(innerCommand);
 
 		Command command = factory.create(parsedCommand);
 		assertEquals(innerCommand, command);
 	}
 
 }
