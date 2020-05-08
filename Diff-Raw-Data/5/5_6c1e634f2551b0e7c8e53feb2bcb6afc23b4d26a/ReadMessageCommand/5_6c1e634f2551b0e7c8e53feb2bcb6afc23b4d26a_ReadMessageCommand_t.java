 package application.commands;
 
 import java.io.DataOutputStream;
 import java.io.IOException;
 import java.math.BigInteger;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.slf4j.LoggerFactory;
 
 import ch.qos.logback.classic.Logger;
 
 import exceptions.MapperException;
 import exceptions.ParameterException;
 import exceptions.UnrecognizedUserException;
 import domain.message.Message;
 import domain.message.mappers.MessageInputMapper;
 
 public class ReadMessageCommand extends FrontCommand {
 
 	@Override
 	public void execute(HttpServletRequest request, HttpServletResponse response) throws MapperException, ParameterException, IOException, UnrecognizedUserException, SQLException {
 
 		String separatedIDs;
 		
 		// Get parameter of pipe ('|') separated message id values
 		if ((separatedIDs = request.getParameter("messageid")) == null)
 			throw new ParameterException("Missing 'messageid' parameter.");
 		
 		// Split the ids, split takes a regex, so we need to escape the pipe character
 		String[] individualIDs = separatedIDs.split("\\|");
 		
 		BigInteger mid = null;
 		
 		// Max array of length
 		List<Message> messages = new ArrayList<Message>(individualIDs.length);
 		
 		for (String id: individualIDs) {
 			try {
 				mid = new BigInteger(id);
 				Message message = MessageInputMapper.find(mid);
 				messages.add(message);
 			} catch (MapperException e) {
 				// TODO get logger some other way
 				Logger logger = (Logger)LoggerFactory.getLogger("application");
 				logger.debug("Message with id {} was not found.", mid.toString());
 				// ID was not found, ignore it, on to the next one
 				continue;
 			} catch (NumberFormatException e) {
 				throw new ParameterException("Parameter 'messageid' is badly formatted.");
 			}
 		}
 		
 		// TODO write message list to response but not the way we are doing it below
		Message.writeListClient(messages, new DataOutputStream(response.getOutputStream()));
 		response.setStatus(HttpServletResponse.SC_OK);
 	}
 
 }
