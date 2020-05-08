 package pl.edu.agh.two.mud.server.command.executor;
 
 import java.util.Arrays;
 
 import pl.edu.agh.two.mud.common.IPlayer;
 import pl.edu.agh.two.mud.common.command.dispatcher.Dispatcher;
 import pl.edu.agh.two.mud.common.command.exception.CommandExecutingException;
 import pl.edu.agh.two.mud.common.command.executor.CommandExecutor;
 import pl.edu.agh.two.mud.server.IServiceRegistry;
 import pl.edu.agh.two.mud.server.Service;
 import pl.edu.agh.two.mud.server.command.AttackUICommand;
 import pl.edu.agh.two.mud.server.command.HitCommand;
 import pl.edu.agh.two.mud.server.command.RunCommand;
 import pl.edu.agh.two.mud.server.command.SendAvailableCommands;
 import pl.edu.agh.two.mud.server.command.exception.ClientAwareException;
 import pl.edu.agh.two.mud.server.world.exception.NoPlayerWithSuchNameException;
 import pl.edu.agh.two.mud.server.world.fight.Fight;
 import pl.edu.agh.two.mud.server.world.model.Board;
 import pl.edu.agh.two.mud.server.world.model.Field;
 
 public class AttackUICommandExecutor implements
 		CommandExecutor<AttackUICommand> {
 
 	private IServiceRegistry serviceRegistry;
 
 	private Board board;
 
 	private Dispatcher dispatcher;
 
 	private Fight fight;
 
 	@SuppressWarnings("unchecked")
 	@Override
 	public void execute(AttackUICommand command)
 			throws CommandExecutingException {
 
 		Service currentService = serviceRegistry.getCurrentService();
 		IPlayer currentPlayer = serviceRegistry.getPlayer(currentService);
 
 		try {
 			Field field = board.getPlayersPosition(currentPlayer);
 			if (field != null) {
 
 				IPlayer enemy = field.getPlayerByName(command.getPlayer());
 				if (enemy == null) {
 					throw new ClientAwareException(String.format(
 							"Przeciwnik %s nie znajduje si na Twoim polu",
 							command.getPlayer()));
 				}
				
				if (currentPlayer.equals(enemy)) {
					throw new ClientAwareException("Nie mozesz atakowa sam siebie");
				}
 
 				if (enemy.isInFight()) {
 					throw new ClientAwareException(String.format(
 							"Przeciwnik %s aktualnie walczy",
 							command.getPlayer()));
 				}
 
 				currentPlayer.setEnemy(enemy);
 				enemy.setEnemy(currentPlayer);
 
 				dispatcher.dispatch(new SendAvailableCommands(currentPlayer,
 						Arrays.asList(HitCommand.class, RunCommand.class)));
 				dispatcher.dispatch(new SendAvailableCommands(enemy, Arrays
 						.asList(HitCommand.class, RunCommand.class)));
 
 				fight.startFight(currentPlayer, enemy);
 
 			} else {
 				throw new ClientAwareException("Nieznany blad.");
 			}
 		} catch (NoPlayerWithSuchNameException e) {
 			throw new ClientAwareException(e,
 					"Nie ma takiego gracza na tym polu.");
 		}
 
 	}
 
 	public void setServiceRegistry(IServiceRegistry serviceRegistry) {
 		this.serviceRegistry = serviceRegistry;
 	}
 
 	public void setBoard(Board board) {
 		this.board = board;
 	}
 
 	public void setDispatcher(Dispatcher dispatcher) {
 		this.dispatcher = dispatcher;
 	}
 
 	public void setFight(Fight fight) {
 		this.fight = fight;
 	}
 
 }
