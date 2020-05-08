 package pl.edu.agh.two.mud.server;
 
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.net.Socket;
 import java.net.SocketAddress;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.log4j.Logger;
 
 import pl.edu.agh.two.mud.common.command.IParsedCommand;
 import pl.edu.agh.two.mud.common.command.UICommand;
 import pl.edu.agh.two.mud.common.command.converter.UICommandToDefinitionConverter;
 import pl.edu.agh.two.mud.common.command.definition.ICommandDefinition;
 import pl.edu.agh.two.mud.common.command.dispatcher.Dispatcher;
 import pl.edu.agh.two.mud.common.message.AvailableCommandsMessage;
 import pl.edu.agh.two.mud.server.command.LogOutCommand;
 import pl.edu.agh.two.mud.server.command.util.AvailableCommands;
 
 public class Service extends Thread {
 
 	private Socket clientSocket;
 	private SocketAddress clientAddress;
 	private Logger logger = Logger.getLogger(Service.class);
 	private ObjectOutputStream out;
 	private ObjectInputStream in;
 	private Dispatcher dispatcher;
 	private UICommandToDefinitionConverter converter;
 
 	public void bindSocket(Socket socket) throws IOException {
 		clientSocket = socket;
 		out = new ObjectOutputStream(socket.getOutputStream());
 		in = new ObjectInputStream(socket.getInputStream());
 		clientAddress = clientSocket.getRemoteSocketAddress();
 		logger.info("New client connected: " + clientAddress);
 	}
 
 	@Override
 	public void run() {
 		try {
 			// Send commands defined by server to clients
 			List<ICommandDefinition> commandsToSend = new ArrayList<ICommandDefinition>();
 			for (UICommand command : AvailableCommands.getInstance()
 					.getUnloggedCommands()) {
 				commandsToSend.add(converter
 						.convertToCommandDefinition(command));
 			}
 
 			writeObject(new AvailableCommandsMessage(commandsToSend));
 
 			while (true) {
 				Object userCommand = in.readObject();
 				dispatcher.dispatch((IParsedCommand) userCommand);
 			}
 		} catch (Exception e) {
			logger.error(clientAddress + " - " + e.getMessage());
 		}
 
 		try {
 			dispatcher.dispatch(new LogOutCommand());
 			logger.info(clientAddress + " - shutting down client connection");
 			clientSocket.close();
 		} catch (IOException e) {
 			logger.error(clientAddress + " - closing client socket error: "
					+ e.getMessage());
 		}
 	}
 
 	public synchronized void writeObject(Object o) throws IOException {
 		out.writeObject(o);
 	}
 
 	public void setDispatcher(Dispatcher dispatcher) {
 		this.dispatcher = dispatcher;
 	}
 
 	public void setConverter(UICommandToDefinitionConverter converter) {
 		this.converter = converter;
 	}
 
 }
