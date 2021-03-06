 package net.i2cat.mantychore.commandsets.junos.commands;
 
 import java.util.Vector;
 
 import net.i2cat.mantychore.commandsets.junos.velocity.VelocityEngine;
 import net.i2cat.mantychore.commons.Command;
 import net.i2cat.mantychore.commons.CommandException;
 import net.i2cat.mantychore.commons.Response;
 import net.i2cat.netconf.rpc.Error;
 import net.i2cat.netconf.rpc.Query;
 import net.i2cat.netconf.rpc.Reply;
 
 import org.apache.velocity.exception.ParseErrorException;
 import org.apache.velocity.exception.ResourceNotFoundException;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public abstract class JunosCommand extends Command {
 
 	protected String	template	= "";
 
	private Object		params;
 
 	/** logger **/
 	Logger				log			= LoggerFactory.getLogger(JunosCommand.class);
 	protected Query		query;
 	protected String	netconfXML;
 
 	protected JunosCommand(String commandID, String template) {
 		this.setCommandId(commandID);
 		this.template = template;
 	}
 	
 	protected JunosCommand(String commandID) {
 		this.setCommandId(commandID);
 	}
 	
 	public void initialize() throws CommandException {
 
 		try {
 			//finish initialize if it does not exist template
 			if (template==null || template.equals("")) return;
 			netconfXML = prepareVelocityCommand();
 		} catch (ResourceNotFoundException e) {
 			e.printStackTrace();
 			throw createCommandException(e);
 		} catch (ParseErrorException e) {
 			e.printStackTrace();
 			throw createCommandException(e);
 		} catch (Exception e) {
 			e.printStackTrace();
 			throw createCommandException(e);
 		}
 	}
 
 	private CommandException createCommandException(Exception e) {
 		CommandException commandException = new CommandException();
 		commandException.setStackTrace(e.getStackTrace());
 		return commandException;
 	}
 
 	protected String prepareVelocityCommand() throws ResourceNotFoundException,
 			ParseErrorException, Exception {
 		VelocityEngine velocityEngine = new VelocityEngine(template, params);
 		String command = velocityEngine.mergeTemplate();
 		log.debug("Command from velocity (netconfXML)" + command);
 		return command;
 	}
 
 	public Response checkResponse(Object resp) {
 
 		//Check if is it a wellformed reply message
 		if (!(resp instanceof Reply)) {
 			Vector<String> errors = new Vector<String>();
 			errors.add("The response message is badformed. It is not a reply message");
 			return Response.errorResponse(query.toXML(), errors );			
 		}
 		
 		Reply reply = (Reply) resp;
 
 		// extra control, it checks if is not null the error list
 		if (reply.isOk() || reply.getErrors() == null || reply.getErrors().size() == 0) {
 			// BUILD OK RESPONSE
 
 			return Response.okResponse(query.toXML());
 		} else {
 			// BUILD ERROR MESSAGE
 
 			Vector<String> errors = new Vector<String>();
 			for (Error error : reply.getErrors())
 				errors.add(error.getMessage() + " : " + error.getInfo());
 
 			return Response.errorResponse(query.toXML(), errors);
 		}
 
 	}
 	public abstract Object sendQuery();
 	
 	public Object message () {
 		query = (Query)sendQuery();
 		return query;
 		
 	}
 	
 	
 	
 
 }
