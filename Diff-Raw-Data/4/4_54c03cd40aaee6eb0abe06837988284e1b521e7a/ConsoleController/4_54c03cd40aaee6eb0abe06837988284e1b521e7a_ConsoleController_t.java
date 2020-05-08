 package pl.edu.agh.two.mud.client.controller;
 
 import org.apache.log4j.Logger;
 
 import pl.edu.agh.two.mud.client.command.exception.InvalidCommandParametersException;
 import pl.edu.agh.two.mud.client.command.exception.UnknownCommandException;
 import pl.edu.agh.two.mud.client.command.parser.ICommandParser;
 import pl.edu.agh.two.mud.client.ui.Console;
 import pl.edu.agh.two.mud.client.ui.Console.ICommandLineListener;
 import pl.edu.agh.two.mud.client.ui.MainWindow;
 import pl.edu.agh.two.mud.common.command.IParsedCommand;
 import pl.edu.agh.two.mud.common.command.dispatcher.Dispatcher;
 
 public class ConsoleController implements ICommandLineListener {
 
 	private static Logger log = Logger.getLogger(ConsoleController.class);
 
 	private Console console;
 	private ICommandParser commandParser;
 	private Dispatcher dispatcher;
 
 	public ConsoleController(MainWindow window, ICommandParser commandParser,
 			Dispatcher dispatcher) {
 		this.console = window.getMainConsole();
 		this.commandParser = commandParser;
 		this.dispatcher = dispatcher;
 
 		console.addCommandLineListener(this);
 	}
 
 	@Override
 	public void commandInvoked(String command) {
 		handleConsoleCommand(command);
 	}
 
 	private void handleConsoleCommand(String command) {
 		try {
 			IParsedCommand parsedCommand = commandParser.parse(command);
 			dispatcher.dispatch(parsedCommand);
 		} catch (UnknownCommandException e) {
 			console.appendTextToConsole(String.format(
 					"Komenda \"%s\" jest nieznana.", e.getCommandName()));
 		} catch (InvalidCommandParametersException e) {
 			console.appendTextToConsole(String.format(
 					"Komenda \"%s\" zostala niepoprawnie uzyta.",
 					e.getCommandName()));
		} catch (RuntimeException e) {
			log.error("Unexpected error during command parsing", e);
 		}
 	}
 }
