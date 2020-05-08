 package pl.edu.agh.two.mud.server.command.executor;
 
 import java.util.Random;
 
 import javax.mail.MessagingException;
 
 import pl.edu.agh.two.mud.common.Player;
 import pl.edu.agh.two.mud.common.command.dispatcher.Dispatcher;
 import pl.edu.agh.two.mud.common.command.executor.CommandExecutor;
 import pl.edu.agh.two.mud.common.message.MessageType;
 import pl.edu.agh.two.mud.server.command.RegisterUICommand;
 import pl.edu.agh.two.mud.server.command.SendMessageToUserCommand;
 import pl.edu.agh.two.mud.server.command.exception.ClientAwareException;
 import pl.edu.agh.two.mud.server.mail.Mailer;
 import pl.edu.agh.two.mud.server.world.exception.NoPlayerWithSuchNameException;
 import pl.edu.agh.two.mud.server.world.model.Board;
 
 public class RegisterUICommandExecutor implements
 		CommandExecutor<RegisterUICommand> {
 
 	private static final String MESSAGE_ON_WRONG_ADDRESS = "Bledny adres e-mail";
 
 	private static final String MESSAGE_ON_EXISTING_LOGIN = "Podany login juz istnieje";
 
	private static final String MESSAGE_ON_SUCCESS = "Konto zostalo utworzone. Dane dostepowe zostaly wyslane na podany adres.";
 
 	private Board board;
 
 	private Mailer mailer;
 
 	private Dispatcher dispatcher;
 
 	@Override
 	public void execute(RegisterUICommand command) throws ClientAwareException {
 		try {
 			board.getPlayerByName(command.getLogin());
 			throw new ClientAwareException(MESSAGE_ON_EXISTING_LOGIN);
 		} catch (NoPlayerWithSuchNameException e) {
 			try {
 				Player player = new Player();
 				String password = generatePassword();
 				mailer.sendRegistrationMail(command.getEmail(),
 						command.getLogin(), password);
 				player.setName(command.getLogin());
 				player.setPassword(password);
 				board.addPlayer(player);
 				dispatcher.dispatch(new SendMessageToUserCommand(
						MESSAGE_ON_SUCCESS, MessageType.INFO));
 			} catch (MessagingException e1) {
				throw new ClientAwareException(MESSAGE_ON_WRONG_ADDRESS);
 			}
 		}
 
 	}
 
 	private String generatePassword() {
 		Long password = new Random(System.nanoTime()).nextLong();
 		password += 100000;
 		password = Math.abs(password);
 		return password.toString();
 	}
 
 	public void setBoard(Board board) {
 		this.board = board;
 	}
 
 	public void setMailer(Mailer mailer) {
 		this.mailer = mailer;
 	}
 
 	public Dispatcher getDispatcher() {
 		return dispatcher;
 	}
 
 	public void setDispatcher(Dispatcher dispatcher) {
 		this.dispatcher = dispatcher;
 	}
 }
