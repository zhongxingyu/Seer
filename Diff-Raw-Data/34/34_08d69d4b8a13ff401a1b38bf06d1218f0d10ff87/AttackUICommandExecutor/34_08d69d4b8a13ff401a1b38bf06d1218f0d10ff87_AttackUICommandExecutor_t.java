 package pl.edu.agh.two.mud.server.command.executor;
 
 import static pl.edu.agh.two.mud.common.message.MessageType.INFO;
 import pl.edu.agh.two.mud.common.IPlayer;
 import pl.edu.agh.two.mud.common.command.dispatcher.Dispatcher;
 import pl.edu.agh.two.mud.common.command.exception.CommandExecutingException;
 import pl.edu.agh.two.mud.common.command.executor.CommandExecutor;
 import pl.edu.agh.two.mud.common.world.exception.NoPlayerWithSuchNameException;
 import pl.edu.agh.two.mud.common.world.model.Board;
 import pl.edu.agh.two.mud.common.world.model.Field;
 import pl.edu.agh.two.mud.server.IServiceRegistry;
 import pl.edu.agh.two.mud.server.Service;
 import pl.edu.agh.two.mud.server.command.AttackUICommand;
 import pl.edu.agh.two.mud.server.command.SendMessageToUserCommand;
 import pl.edu.agh.two.mud.server.world.fight.Fight;
 
public class AttackUICommandExecutor implements CommandExecutor<AttackUICommand> {
 
 	private IServiceRegistry serviceRegistry;
 	private Board board;
 	private Fight fight;
 	private Dispatcher dispatcher;
 
 	@Override
	public void execute(AttackUICommand command) throws CommandExecutingException {
 
 		Service currentService = serviceRegistry.getCurrentService();
 		IPlayer currentPlayer = serviceRegistry.getPlayer(currentService);
 
 		try {
 			Field field = board.getPlayersPosition(currentPlayer);
 			if (field != null) {
 
 				IPlayer enemy = field.getPlayerByName(command.getPlayer());
 				if (enemy == null) {
					dispatcher.dispatch(new SendMessageToUserCommand(String.format(
							"Przeciwnik %s nie znajduje sie na Twoim polu", command.getPlayer()), INFO));
				} else if (currentPlayer.equals(enemy)) {
 					dispatcher.dispatch(new SendMessageToUserCommand("Nie mozesz atakowac sam siebie", INFO));
				} else if (enemy.isInFight()) {
					dispatcher.dispatch(new SendMessageToUserCommand(String.format("Przeciwnik %s aktualnie walczy",
							command.getPlayer()), INFO));
				} else {
 					currentPlayer.setEnemy(enemy);
 					enemy.setEnemy(currentPlayer);

 					fight.startFight(currentPlayer, enemy);
 				}
 
 			} else {
 				dispatcher.dispatch(new SendMessageToUserCommand("Nieznany blad.", INFO));
 			}
 		} catch (NoPlayerWithSuchNameException e) {
 			dispatcher.dispatch(new SendMessageToUserCommand("Nie ma takiego gracza na tym polu.", INFO));
 		}
 
 	}
 
 	public void setServiceRegistry(IServiceRegistry serviceRegistry) {
 		this.serviceRegistry = serviceRegistry;
 	}
 
 	public void setBoard(Board board) {
 		this.board = board;
 	}
 
 	public void setFight(Fight fight) {
 		this.fight = fight;
 	}

 	public void setDispatcher(Dispatcher dispatcher) {
 		this.dispatcher = dispatcher;
 	}
 
 }
