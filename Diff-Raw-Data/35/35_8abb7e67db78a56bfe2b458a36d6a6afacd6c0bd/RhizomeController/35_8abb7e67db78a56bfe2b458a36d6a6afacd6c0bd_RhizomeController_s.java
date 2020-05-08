 package com.technosophos.rhizome.controller;
 
 import com.technosophos.rhizome.repository.RepositoryManager;
 import com.technosophos.rhizome.repository.RepositoryContext;
 import java.util.LinkedList;
 import java.util.Queue;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Iterator;
 import java.util.List;
 
 /**
  * Controller for handling Rhizome requests.
  * <p>The main job of this class is to act as a controller (front controller pattern) for
  * the Rhizome backend. It works by first initializing a rhizome RepositoryManager, which 
  * will handle access to data storage and searching facilities, and then handling requests
  * from "clients" (any caller from outside).
  * The controller takes requests, unpacks them into queues of commands, 
  * and then processes the commands in order.
  * </p>
  * <p>
  * The request-to-command queue mapping is performed during 
  * initialization. Then, at each doRequest() method, the given information is unpacked
  * into a command queue, and each command is executed in sequence.
  * </p>
  * <hr/>
  * <p>About the request-to-command HashMap:</p>
  * <p>The RhizomeController maps requests (identified by String names) with a queue of 
  * commands. Each request should result (assuming no fatal error) in the processing of 
  * each command in sequence. So each entry in the request-to-command HashMap should have 
  * the string name of the request, followed by a Queue of CommandConfiguration objects. One
  * way of retrieving such mappings is with the XMLRequstConfigurationReader, in
  * which case an XML file containing mappings is read, and converted into an appropriately
  * formatted HashMap.</p>
  * @see XMLRequestConfigurationReader
  * @see CommandConfiguration
  * @author mbutcher
  * @since 0.1
  *
  */
 public class RhizomeController {
 
	private HashMap<String, Queue<CommandConfiguration>> cqMap = null;
 	private RepositoryManager repoman = null;
 	private RepositoryContext repocxt = null;
 	
 	/**
 	 * This constructor should only be used when necessary.
 	 * It must be followed by a call to init(), otherwise the repository may not get 
 	 * correctly initialized, and no commands will exist.
 	 */
 	public RhizomeController() {
 		this.cqMap = new HashMap<String, Queue<CommandConfiguration>>();
 		this.repocxt = new RepositoryContext();
 		this.repoman = new RepositoryManager(this.repocxt);
 	}
 	
 	/**
 	 * This is the preferred constructor.
 	 * If you use this constructor, you do not need to initialize with {@code init()}.
 	 * @param cqMap a command queue map of request names to a queue of command names.
 	 * @param cxt A RepositoryContext with settings for operating the respository.
 	 * @see RepositoryContext()
 	 */
	public RhizomeController(HashMap<String, Queue<CommandConfiguration>> cqMap, RepositoryContext cxt) {
 		this.cqMap = cqMap;
 		this.repocxt = cxt;
 		this.repoman = new RepositoryManager(cxt);
 	}
 	
 	/**
 	 * Initialize this controller.
 	 * You <b>MUST</b> use init() if you used the empty constructor.
 	 * @see isInitialized()
 	 * @see RepositoryContext()
 	 * @param cqMap a command queue map of request names to a queue of command names.
 	 * @param cxt A RepositoryContext with settings for operating the repository.
 	 */
	public void init(HashMap<String, Queue<CommandConfiguration>> cqMap, RepositoryContext cxt) {
 		this.cqMap = cqMap;
 		this.repocxt = cxt;
 		this.repoman = new RepositoryManager(cxt);
 	}
 	
 	/**
 	 * Returns true if this object has been initialized.
 	 * An uninitialized RhizomeController is one that has no requests in the command queue
 	 * map.
 	 * 
 	 * @return true if it has at least one register request-to-command mapping.
 	 */
 	public boolean isInitialized() {
 		if(this.cqMap == null || this.cqMap.isEmpty()) return false;
 		return true;
 	}
 	
 	/**
 	 * Handle a request.
 	 * This handles a request, which is typically made up of one or more commands, and
 	 * returns the results in a List. Exceptions thrown by commands are caught and stored
 	 * in the CommandResult objects.
 	 * @param requestName
 	 * @param data
 	 * @return
 	 * @throws RequestNotFoundException
 	 */
 	public LinkedList<CommandResult> doRequest(String requestName, Map<String, Object> data) 
 			throws RequestNotFoundException {
 		
 		LinkedList<CommandResult> results = new LinkedList<CommandResult>();
 		
 		if(!this.cqMap.containsKey(requestName))
 			throw new RequestNotFoundException("No match for request \"" 
 					+ requestName + "\" ");
 		Iterator<CommandConfiguration> commands = this.cqMap.get(requestName).iterator();
 		
 		
 		try {
 			while (commands.hasNext()) {
 				CommandConfiguration cconf = commands.next();
 				this.doCommand(cconf, data, results);
 			}
 		} catch (FatalCommandException fce) {
 			results.clear();
 			CommandResult res = new CommandResult();
 			String errMsg = "Fatal Error in " + requestName + ".";
 			String ferrMsg = "The server experienced a severe error, and cannot complete this task.";
 			res = new CommandResult();
 			res.setError(errMsg, ferrMsg, fce);
 			results.add(res);
 		}		
 		return results;
 	}
 	
 	/**
 	 * Run a command.
 	 * <p>This method is used internally. As a request is processed, each command is called 
 	 * in turn.</p>
 	 * <p>Any errors are caught and wrapped in a CommandResult.</p>
 	 * @param cconf
 	 * @param data
 	 * @return
 	 */
 	protected void doCommand(CommandConfiguration cconf, Map<String, Object> data, List<CommandResult> results) 
 			throws FatalCommandException {
 		try {
 			RhizomeCommand command = RhizomeCommandFactory.getCommand(cconf, this.repoman);
 			command.doCommand(data, results);
 		} catch (CommandNotFoundException cnfe) {
 			if (cconf.failOnError()) {
 				String err = "Fatal error in " +cconf.getName() + ".";
 				throw new FatalCommandException(err, cnfe);
 			} else {
 				String errMsg = "Command " + cconf.getName() + " not found.";
 				String ferrMsg = "The server could not find the tools required to handle this request.";
 				CommandResult res = new CommandResult();
 				res.setError(errMsg, ferrMsg, cnfe);
 			}			
 		}
 	}
 	
 }
