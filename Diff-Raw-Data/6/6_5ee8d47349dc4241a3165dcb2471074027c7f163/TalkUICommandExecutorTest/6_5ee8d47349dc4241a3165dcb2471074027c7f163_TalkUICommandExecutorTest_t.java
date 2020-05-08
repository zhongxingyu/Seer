 package pl.edu.agh.two.mud.server.command.executor;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.fail;
 import static org.mockito.Matchers.any;
 import static org.mockito.Mockito.mock;
 import static org.mockito.Mockito.times;
 import static org.mockito.Mockito.verify;
 import static org.mockito.Mockito.when;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import pl.edu.agh.two.mud.common.IPlayer;
 import pl.edu.agh.two.mud.common.command.dispatcher.Dispatcher;
 import pl.edu.agh.two.mud.common.command.exception.CommandExecutingException;
 import pl.edu.agh.two.mud.common.command.type.Text;
 import pl.edu.agh.two.mud.common.message.MessageType;
 import pl.edu.agh.two.mud.server.IServiceRegistry;
 import pl.edu.agh.two.mud.server.Service;
 import pl.edu.agh.two.mud.server.command.SendMessageToUserCommand;
 import pl.edu.agh.two.mud.server.command.TalkUICommand;
 import pl.edu.agh.two.mud.server.command.exception.ClientAwareException;
 import pl.edu.agh.two.mud.server.world.model.Board;
 import pl.edu.agh.two.mud.server.world.model.Field;
 
 public class TalkUICommandExecutorTest {
 	private static final String CURRENT_PLAYER_NAME = "CurrentPlayer";
 	private static final String MESSAGE = "Some message";
 
 	private TalkUICommandExecutor executor;
 	private Board board;
 	private IServiceRegistry serviceRegistry;
 	private IPlayer currentPlayer;
 	private Service currentService;
 	private Dispatcher dispatcher;
 	private TalkUICommand command;
 
 	@Before
 	public void prepareTest() {
 		board = mock(Board.class);
 		currentService = mock(Service.class);
 
 		currentPlayer = mock(IPlayer.class);
 		when(currentPlayer.getName()).thenReturn(CURRENT_PLAYER_NAME);
 
 		dispatcher = mock(Dispatcher.class);
 
 		serviceRegistry = mock(IServiceRegistry.class);
 		when(serviceRegistry.getCurrentService()).thenReturn(currentService);
 		when(serviceRegistry.getPlayer(currentService)).thenReturn(currentPlayer);
 
 		executor = new TalkUICommandExecutor();
 		executor.setBoard(board);
 		executor.setServiceRegistry(serviceRegistry);
 		executor.setDispatcher(dispatcher);
 
 		command = mock(TalkUICommand.class);
 		when(command.getContent()).thenReturn(new Text(MESSAGE));
 	}
 
 	@Test
 	public void userDoesNotExist() {
 		when(serviceRegistry.getPlayer(any(Service.class))).thenReturn(null);
 		try {
 			executor.execute(command);
 			fail("Exception expected");
 		} catch (ClientAwareException e) {
 			assertEquals("Jestes niezalogowany.", e.getErrorMessage());
 
 		} catch (CommandExecutingException e) {
 			fail("Other exception expected");
 		}
 	}
 
 	@Test
 	public void noFieldForUser() {
 		when(board.getPlayersPosition(currentPlayer)).thenReturn(null);
 
 		try {
 			executor.execute(command);
 			fail("Exception expected");
 		} catch (ClientAwareException e) {
 			assertEquals("Nieznany blad.", e.getErrorMessage());
 		} catch (CommandExecutingException e) {
 			fail("Other exception expected");
 		}
 	}
 
 	@Test
 	public void talkNoUser() {
 		Field field = mock(Field.class);
 
 		List<IPlayer> players = new ArrayList<IPlayer>();
 
 		when(board.getPlayersPosition(currentPlayer)).thenReturn(field);
 		when(field.getPlayers()).thenReturn(players);
 
 		try {
 			executor.execute(command);
 		} catch (CommandExecutingException e) {
 			fail("Exception unexpected");
 		}
 	}
 
 	@Test
 	public void talkOneUser() {
 		talkNUsers(1);
 	}
 
 	@Test
 	public void talkManyUser() {
 		talkNUsers(10);
 	}
 
 	private void talkNUsers(int howMany) {
 		List<IPlayer> players = new ArrayList<IPlayer>();
 
 		Field field = mock(Field.class);
 		when(board.getPlayersPosition(currentPlayer)).thenReturn(field);
 
 		players.add(currentPlayer);
 		for (int i = 0; i < howMany; i++) {
 			IPlayer targetPlayer = mock(IPlayer.class);
 			players.add(targetPlayer);
 		}
 
 		when(field.getPlayers()).thenReturn(players);
 
 		try {
 			executor.execute(command);
 			for (IPlayer player : players) {
				if (player.equals(currentPlayer)) {
					continue;
				}

				verify(dispatcher).dispatch(
 						new SendMessageToUserCommand(player, String.format("%s mowi: %s", currentPlayer.getName(),
 								MESSAGE), MessageType.INFO));
 			}
 		} catch (CommandExecutingException e) {
 			fail("Exception unexpected");
 		}
 	}
 }
