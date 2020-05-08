 package service.command.impl.mail;
 
 import java.io.IOException;
 
 import model.User;
 import model.validator.LoginValidationException;
 import model.validator.loginvalidator.TimeValidator;
 import service.AbstractSockectService;
 import service.MailSocketService;
 import service.StatusCodes;
 import service.command.ServiceCommand;
 import service.state.impl.mail.ParseMailState;
 
 public class UserCommand extends ServiceCommand {
 
 	public UserCommand(AbstractSockectService owner) {
 		super(owner);
 	}
 
 	@Override
 	public void execute(String[] params) throws Exception {
 		MailSocketService mailServer = (MailSocketService) owner;
 		User user = new User(params[0], null);
		if (!validateAccessToMailByTime(user, mailServer)){
			return;
		}		
 		mailServer.setOriginServer(user.getMailhost());
 		String resp = echoToOriginServerAndReadLine(getOriginalLine());
 		owner.echoLine(resp);
 		if (!"+OK".equals(resp)) {
 			return;
 		}
 		String passwordCmd = owner.read().readLine();
 		resp = echoToOriginServerAndReadLine(passwordCmd);
 		owner.echoLine(resp);
 		if (!resp.toUpperCase().startsWith("+OK")) {
 			return;
 		}
 		user.setPassword(passwordCmd.split(" ")[1]);
 		getBundle().put("AUTH_USER", user);
 		owner.getStateMachine().setState(new ParseMailState(owner));
 	}
 
 	private String echoToOriginServerAndReadLine(String line) throws IOException {
 		MailSocketService service = (MailSocketService) owner;
 		service.echoLineToOriginServer(line);
 		return service.readFromOriginServer().readLine();
 	}
 	
 	private boolean validateAccessToMailByTime(User user, MailSocketService mailServer) throws Exception {
 		String userMail = user.getMail();
 		logger.debug("Checking time access for user: " + userMail);
 		try {
 			new TimeValidator(userMail).validate();
 			return true;
 		} catch (LoginValidationException e) {
 			logger.info("User " + userMail + " is banned. Closing connection.");
			mailServer.echoLine("-ERR User does not have acces during this time.");
 			return false;
 		}
 	}
 }
