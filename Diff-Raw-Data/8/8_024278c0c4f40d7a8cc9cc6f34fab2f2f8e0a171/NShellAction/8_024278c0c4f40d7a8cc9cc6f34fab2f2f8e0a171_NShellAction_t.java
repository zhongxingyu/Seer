 package n3phele.service.actions;
 /**
  *
  * (C) Copyright 2010-2013. Nigel Cook. All rights reserved.
  * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
  * 
  * Licensed under the terms described in LICENSE file that accompanied this code, (the "License"); you may not use this file
  * except in compliance with the License. 
  * 
  *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on 
  *  an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the 
  *  specific language governing permissions and limitations under the License.
  *
  */
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import javax.xml.bind.annotation.XmlRootElement;
 import javax.xml.bind.annotation.XmlType;
 
 import n3phele.service.core.NotFoundException;
 import n3phele.service.core.UnprocessableEntityException;
 import n3phele.service.lifecycle.ProcessLifecycle;
 import n3phele.service.lifecycle.ProcessLifecycle.WaitForSignalRequest;
 import n3phele.service.model.Action;
 import n3phele.service.model.CloudProcess;
 import n3phele.service.model.Command;
 import n3phele.service.model.CommandImplementationDefinition;
 import n3phele.service.model.Context;
 import n3phele.service.model.FileSpecification;
 import n3phele.service.model.FileTracker;
 import n3phele.service.model.ShellFragment;
 import n3phele.service.model.ShellFragmentKind;
 import n3phele.service.model.SignalKind;
 import n3phele.service.model.Variable;
 import n3phele.service.model.VariableType;
 import n3phele.service.model.core.Helpers;
 import n3phele.service.model.core.ParameterType;
 import n3phele.service.model.core.TypedParameter;
 import n3phele.service.model.core.User;
 import n3phele.service.model.repository.Repository;
 import n3phele.service.nShell.ExpressionEngine;
 import n3phele.service.nShell.UnexpectedTypeException;
 import n3phele.service.rest.impl.ActionResource;
 import n3phele.service.rest.impl.CloudProcessResource;
 import n3phele.service.rest.impl.CommandResource;
 import n3phele.service.rest.impl.RepositoryResource;
 import n3phele.service.rest.impl.UserResource;
 
 import com.googlecode.objectify.VoidWork;
 import com.googlecode.objectify.annotation.Cache;
 import com.googlecode.objectify.annotation.EntitySubclass;
 import com.googlecode.objectify.annotation.Serialize;
 import com.googlecode.objectify.annotation.Unindex;
 
 @EntitySubclass
 @XmlRootElement(name = "NShellAction")
 @XmlType(name = "NShellAction", propOrder = { "executableName", "pc", "watchFor", "abnormalTermination", "command", "cloud", "start", "active", "adopted", "executable" })
 @Unindex
 @Cache
 public class NShellAction extends Action {
 	private final static Logger log = Logger.getLogger(NShellAction.class.getName()); 
 	
 	private String executableName;
 	private int pc = 0;
 	private String watchFor = null;
 	private String abnormalTermination = null;
 	private String command;
 	private String cloud;
 	private int start;
 	private List<String> adopted = new ArrayList<String>();
 	private List<String> active = new ArrayList<String>();
 	@Serialize private List<ShellFragment> executable;
 	private ActionLogger logger;
 
 	
 	public NShellAction() {}
 	
 	public NShellAction(User owner, String name, Context context, Command command, int commandImplementation) {
 		super(owner.getUri(), name, context);
 		this.command = command.getUri().toString();
 		this.executable = command.getImplementations().get(commandImplementation).getCompiled();
 		this.executableName = command.getName()+" "+command.getVersion()+(command.isPreferred()?"":"*")+" on "+command.getName();
 	}
 	
 	@Override
 	public String getDescription() {
 		return this.executableName;
 	}
 
 	
 	/* (non-Javadoc)
 	 * @see n3phele.service.model.Action#getPrototype()
 	 */
 	@Override
 	public Command getPrototype() {
 		Command command = CommandResource.dao.load(this.getCommand());
 		for(TypedParameter param : Helpers.safeIterator(command.getExecutionParameters())) {
 			param.setDefaultValue(this.context.getValue(param.getName()));
 		}
 		User user = UserResource.dao.load(this.getOwner());
 		for(FileSpecification file : Helpers.safeIterator(command.getInputFiles())) {
 			URI target = this.context.getFileValue(file.getName());
 			if(target != null) {
 				try {
 					Repository repo = RepositoryResource.dao.load(target.getScheme(), user);
 					file.setRepository(repo.getUri());
 					file.setFilename(target.getPath().substring(1));
 				} catch (NotFoundException e) {
 					log.warning("No repo "+target.getScheme()+" for "+user.getName());
 				}
 			}
 		}
 		for(FileSpecification file : Helpers.safeIterator(command.getOutputFiles())) {
 			URI target = this.context.getFileValue(file.getName());
 			if(target != null) {
 				try {
 					Repository repo = RepositoryResource.dao.load(target.getScheme(), user);
 					file.setRepository(repo.getUri());
 					file.setFilename(target.getPath().substring(1));
 				} catch (NotFoundException e) {
 					log.warning("No repo "+target.getScheme()+" for "+user.getName());
 				}
 			}
 		}
 		command.getExecutionParameters().add(new TypedParameter("$account", "account", ParameterType.String, "", this.context.getValue("account")));
 		return command;
 	}
 
 	@Override
 	public void init() throws Exception {
 		logger = new ActionLogger(this);
 
 		if(executable == null || executable.isEmpty()) {
 			String arg = this.getContext().getValue("arg");
 			String[] argv;
 			if(Helpers.isBlankOrNull(arg)) {
 				throw new IllegalArgumentException("executable not specified");
 			} else {
 				argv =	arg.split("[\\s]+");	// FIXME - find a better regex for shell split
 			}
 			
 			URI command = URI.create(argv[0]);
 	
 			/*
 			 * NB: initalizeExecutableFromCommandImplementation sets up this.command and this.executableName
 			 */
 			initalizeExecutableFromCommandImplementationDefinition(command);
 		}
 		
 		this.pc = 0;
 	}
 	
 	@Override
 	public boolean call() throws IllegalArgumentException, UnexpectedTypeException, NotFoundException, ClassNotFoundException, WaitForSignalRequest { 
 		if(this.abnormalTermination != null){
 			log.info("Aborting due to abnormal termination of "+this.abnormalTermination);
 			throw new UnprocessableEntityException("Abnormal termination of "+this.abnormalTermination);
 		}
 		ShellFragment script = this.executable.get(this.executable.size()-1);
 		for(int i = this.pc; i < script.children.length; i++) {
 			this.pc = i;
 			if(this.watchFor != null) {;
 			log.info("watchFor "+watchFor);
 				throw new ProcessLifecycle.WaitForSignalRequest();
 			}
 
 			ShellFragment fragment = this.executable.get(script.children[i]);
 			if(assertDependenciesAvailable(buildDependencySetFor(fragment))) {
 				log.info("execute "+i+":"+this.executable.get(script.children[i]).kind);
 				this.execute(script.children[i], null);
 			} else {
 				log.info("has dependencies");
 				return false;
 			}
 		}
 		this.pc = script.children.length;
 		int myChildren = this.active.size();
 		log.info("waiting for "+myChildren+" children");
 		if(myChildren != 0) {
 			throw new ProcessLifecycle.WaitForSignalRequest();
 		}
 		return true;
 	}
 	
 	
 	@Override
 	public void cancel() {
 		log.info("Cancelling "+(active.size()+adopted.size())+" children");
 		for(String vm : active) {
 			try {
 				processLifecycle().cancel(URI.create(vm));
 			} catch (NotFoundException e) {
 				log.severe("Not found: "+e.getMessage());
 			}
 		}
 		for(String vm : adopted) {
 			try {
 				processLifecycle().cancel(URI.create(vm));
 			} catch (NotFoundException e) {
 				log.severe("Not found: "+e.getMessage());
 			}
 		}
 	}
 
 	@Override
 	public void dump() {
 		log.info("Dump of "+(active.size()+adopted.size())+" children");
 		for(String vm : active) {
 			try {
 				processLifecycle().dump(URI.create(vm));
 			} catch (NotFoundException e) {
 				log.severe("Not found: "+e.getMessage());
 			}
 		}
 		for(String vm : adopted) {
 			try {
 				processLifecycle().dump(URI.create(vm));
 			} catch (NotFoundException e) {
 				log.severe("Not found: "+e.getMessage());
 			}
 		}
 		
 	}
 	
 	
 
 	@Override
 	public void signal(SignalKind kind, String assertion) {
 		log.info("Signal "+kind+":"+assertion);
 		boolean isWatchChild = this.watchFor != null && this.watchFor.equals(assertion);
 		boolean isChild = isWatchChild || this.active.contains(assertion);
 		boolean isAdopted = this.adopted.contains(assertion);
 		switch(kind) {
 		case Adoption:
 			URI processURI = URI.create(assertion);
 			try {
 				CloudProcess child = CloudProcessResource.dao.load(processURI);
 				log.info("Adopting child "+child.getName()+" "+child.getClass().getSimpleName());
 				this.adopted.add(assertion);
 			} catch (Exception e) {
 				log.info("Assertion is not a cloudProcess");
 			}
 			return;
 		case Cancel:
 			log.info((isChild?"Child ":isAdopted?"Adopted ":"Unknown ")+assertion+" cancelled");
 			if(isChild) {
 				this.active.remove(assertion);
 				this.abnormalTermination = assertion;
 				if(isWatchChild)
 					this.watchFor = null;
 			} else if(isAdopted) {
 				this.adopted.remove(assertion);
 			}
 			break;
 		case Event:
 			log.warning("Ignoring event "+assertion);
 			return;
 		case Failed:
 			log.info((isChild?"Child ":isAdopted?"Adopted ":"Unknown ")+assertion+" failed");
 			if(isChild) {
 				this.active.remove(assertion);
 				this.abnormalTermination = assertion;
 				if(isWatchChild)
 					this.watchFor = null;
 			} else if(isAdopted) {
 				this.adopted.remove(assertion);
 			}
 			break;
 		case Ok:
 			log.info((isChild?"Child ":isAdopted?"Adopted ":"Unknown ")+assertion+" ok");
 			if(isChild) {
 				this.active.remove(assertion);
 				if(isWatchChild)
 					this.watchFor = null;
 			} else if(isAdopted) {
 				this.adopted.remove(assertion);
 			}
 			break;
 		default:
 			return;		
 		}
 		
 	}
 
 	/** Processes the executable at the pc, creating a series of executable subshell processes
 	 *  responsible for the top level command objects in the syntax tree.
 	 *  script:: (
 	 *  	  createvm()
 	 *		| on()
 	 *		| log()
 	 * 		| destroy()
 	 *		| forCommand
 	 *		| variableAssign()
   	 *	) *
   	 *  
 	 * @param pc
 	 * @return
 	 * @throws UnexpectedTypeException
 	 * @throws IllegalArgumentException
 	 * @throws ClassNotFoundException 
 	 * @throws NotFoundException 
 	 * @throws WaitForSignalRequest 
 	 */
 	Variable execute(int pc, String variableName) throws UnexpectedTypeException, IllegalArgumentException, NotFoundException, ClassNotFoundException, WaitForSignalRequest {
 		ShellFragment s = executable.get(pc);
 		switch (s.kind){
 		case createvm:
 			return createVMCommand(s, variableName);
 		case destroy:
 			return destroyCommand(s);
 		case on:
 			return onCommand(s, variableName);
 		case onexit:
 			return onExitCommand(s, variableName);
 		case log:
 			return logCommand(s, variableName);
 		case forCommand:
 			return forCommand(s, variableName);
 		case variableAssign:
 			return variableAssignCommand(s);
 		case assimilatevm:
 			return assimilateVMCommand(s, variableName);
 		case export:
 			return exportCommand(s);
 		default:
 			throw new IllegalArgumentException("Unexpected node "+s.kind);
 		}		
 	}
 	
 	/** createVM shell command
 	 * 
 	 * createvm:: < CREATEVM > ( option() )+ 
 	 * option:: (
 	 *				(< OPTION > arg() )
 	 *			  |  < NO_ARG_OPTION >
   	 *			)
 	 * @param createVMFragment createVM parse tree fragment
 	 * @param specifiedName	name of context variable object will be assigned to
 	 * @return
 	 * @throws UnexpectedTypeException 
 	 * @throws IllegalArgumentException 
 	 * @throws ClassNotFoundException 
 	 * @throws NotFoundException 
 	 */
 	private Variable createVMCommand(ShellFragment createVMFragment, String specifiedName) throws IllegalArgumentException, UnexpectedTypeException, NotFoundException, ClassNotFoundException {
 		Context childContext = new Context();
 		childContext.putAll(this.context);
 		childContext.remove("name");
 		for(int i : createVMFragment.children){
 			ShellFragment option = this.executable.get(i);
 			String optionName = option.value;
 			if(option.children != null && option.children.length != 0) {
 				Variable v = optionParse(option);
 				childContext.put(optionName, v);
 			} else {
 				childContext.putValue(optionName, true);
 			}
 		}
 		boolean isAsync = childContext.getBooleanValue("async");
 		return makeChildProcess("CreateVM", childContext, specifiedName, isAsync, null);
 
 	}
 	
 	/** assimilateVM shell command
 	 * 
 	 * assimilateVM:: < ASSIMILATE > ( option() )+ 
 	 * option:: (
 	 *				(< OPTION > arg() )
 	 *			  |  < NO_ARG_OPTION >
   	 *			)
 	 * @param assimiateVMFragment assimilate parse tree fragment
 	 * @param specifiedName	name of context variable object will be assigned to
 	 * @return
 	 * @throws UnexpectedTypeException 
 	 * @throws IllegalArgumentException 
 	 * @throws ClassNotFoundException 
 	 * @throws NotFoundException 
 	 */
 	private Variable assimilateVMCommand(ShellFragment assimilateVMFragment, String specifiedName) throws IllegalArgumentException, UnexpectedTypeException, NotFoundException, ClassNotFoundException {
 		Context childContext = new Context();
 		childContext.putAll(this.context);
 		childContext.remove("name");
 		for(int i : assimilateVMFragment.children){
 			ShellFragment option = this.executable.get(i);
 			String optionName = option.value;
 			if(option.children != null && option.children.length != 0) {
 				Variable v = optionParse(option);
 				childContext.put(optionName, v);
 			} else {
 				childContext.putValue(optionName, true);
 			}
 		}
 		boolean isAsync = childContext.getBooleanValue("async");
 		return makeChildProcess("Assimilate", childContext, specifiedName, isAsync, null);
 
 	}
 
 	/** ON shell command
 	 * 
 	 * ON:: < ON > expression() ( option() )*  pieces()
 	 * PIECES:: ( expression | passThru )+
 	 * option:: (
 	 *				(< OPTION > arg() )
 	 *			  |  < NO_ARG_OPTION >
   	 *			)
 	 * 
 	 * @param onFragment
 	 * @param specifiedName
 	 * @return
 	 * @throws IllegalArgumentException
 	 * @throws UnexpectedTypeException
 	 * @throws NotFoundException
 	 * @throws ClassNotFoundException
 	 */
 	
 	private Variable onCommand(ShellFragment onFragment, String specifiedName) throws IllegalArgumentException, UnexpectedTypeException, NotFoundException, ClassNotFoundException {
 		return onCommandProcessing(false, onFragment, specifiedName);
 	}
 	
 	
 	/** ONEXIT shell command
 	 * 
 	 * ONEXIT:: < ONEXIT > expression() ( option() )*  pieces()
 	 * PIECES:: ( expression | passThru )+
 	 * option:: (
 	 *				(< OPTION > arg() )
 	 *			  |  < NO_ARG_OPTION >
   	 *			)
 	 * 
 	 * @param onFragment	
 	 * @param specifiedName 
 	 * @return
 	 * @throws IllegalArgumentException
 	 * @throws UnexpectedTypeException
 	 * @throws NotFoundException
 	 * @throws ClassNotFoundException
 	 */
 	private Variable onExitCommand(ShellFragment onFragment, String specifiedName) throws IllegalArgumentException, UnexpectedTypeException, NotFoundException, ClassNotFoundException {
 		return onCommandProcessing(true, onFragment, specifiedName);
 	}
 
 	/** ON and ONEXIT command processing
 	 * @param isOnExit true if performing OnExit command processing
 	 * @param onOrOnExitFragment shell command fragment for On or OnExit command
 	 * @param specifiedName specified command name or null if none
 	 * @return
 	 * @throws IllegalArgumentException
 	 * @throws UnexpectedTypeException
 	 * @throws NotFoundException
 	 * @throws ClassNotFoundException
 	 */
 	private Variable onCommandProcessing(boolean isOnExit, ShellFragment onOrOnExitFragment, String specifiedName) throws IllegalArgumentException, UnexpectedTypeException, NotFoundException, ClassNotFoundException {
 		List<CloudProcess> needsInit = new ArrayList<CloudProcess>();
 		Context childContext = new Context();
 		childContext.putAll(this.context);
 		childContext.remove("name");
 		
 		ShellFragment expression = this.executable.get(onOrOnExitFragment.children[0]);
 		ExpressionEngine ee = new ExpressionEngine(this.executable, this.context, this.getOwner());
 		URI uri = URI.create(ee.expression(expression).toString());
 		Action action = ActionResource.dao.load(uri);
 		if(action instanceof CreateVMAction) {
 			CreateVMAction head = (CreateVMAction) action;
 			List<String> list = head.getContext().getListValue("cloudVM");
 			uri = URI.create(list.get(0));
 			action = ActionResource.dao.load(uri);
 		}
		
		if(action instanceof AssimilateAction) {
			AssimilateAction head = (AssimilateAction) action;
			Entry<String, String> e = head.getChildMap().entrySet().iterator().next();
			URI process =   Helpers.stringToURI(e.getValue());
			CloudProcess cp = CloudProcessResource.dao.load(process);
			action = ActionResource.dao.load(cp.getAction());
		}		
 
 		VMAction target =  (VMAction) action;
 		
 		ShellFragment pieces = this.executable.get(onOrOnExitFragment.children[onOrOnExitFragment.children.length-1]);
 		for(int i=1; i < onOrOnExitFragment.children.length-1; i++){
 			ShellFragment option = this.executable.get(onOrOnExitFragment.children[i]);
 			String optionName = option.value;
 			if(option.children != null && option.children.length != 0) {
 				Variable v = optionParse(option);
 				childContext.put(optionName, v);
 			} else {
 				childContext.putValue(optionName, true);
 			}
 		}
 		
 		/*
 		 * Input File Processing
 		 * ---------------------
 		 * 
 		 * Examine the context, command and target VM to determine
 		 * 1. neededInputFiles - the list of files on the target VM for the command to execute
 		 * 2. newEntries - those neededInputFiles not already on the target VM
 		 * 3. dependentOn - start to build the list of producer processors already placing the input files on the target VM
 		 * 
 		 */
 		
 		Command cmd = loadCommandDefinition(this.getCommand());
 		
 		List<FileTracker> neededInputFiles = resolveNeeds(childContext, cmd);
 		List<FileTracker> newEntries = new ArrayList<FileTracker>();
 		Map<String, FileTracker> current = target.getFileTable();
 		Set<URI> dependentOn = new HashSet<URI>();
 		for(FileTracker i : neededInputFiles) {
 			if(current.containsKey(i.getName())) {
 				FileTracker onVM = current.get(i.getName());
 				URI producer = onVM.getProcess();
 				dependentOn.add(producer);
 			} else {
 				newEntries.add(i);
 			}
 		}
 		
 		/*
 		 * Output File Processing
 		 * ----------------------
 		 * Examine the context, command and target VM to determine
 		 * 1. producedOutputFiles - the list of files produced on the VM by the process
 		 * 2. needTransfers - those producedOutputFiles that have a repo transfer target specified in the command innovation
 		 */
 		
 		List<FileTracker> needTransfers = new ArrayList<FileTracker>();
 		List<FileTracker> producedOutputFiles = resolveProduces(childContext, cmd, needTransfers);
 		
 		
 		/*
 		 * Input File Processing
 		 * ---------------------
 		 * 
 		 * For each of the new files that need to be placed on the targetVM (newEntries)
 		 * create a fileCopy process to put them there. Generate a new FileTracker object
 		 * containing details of the file transfer and the process responsible for it.
 		 * 
 		 */
 		
 		
 		Map<String, CloudProcess> inputXferProcess = new HashMap<String, CloudProcess>();
 		Context fileCopyContext = new Context();
 		for(FileTracker newEntry : newEntries) {
 			fileCopyContext.putValue("target", target.getUri());
 			fileCopyContext.putValue("name", newEntry.getRepo().toString());
 			fileCopyContext.putValue("source", newEntry.getRepo());
 			fileCopyContext.putValue("destination", URI.create("file:///"+newEntry.getLocalName()));
 			fileCopyContext.putValue("fileTableId", newEntry.getName());
 			CloudProcess fileCopy;
 			if(isOnExit) {
 				fileCopy = forkOnExitChildProcess("FileTransfer", fileCopyContext, generateUniqueName("FileTransfer"), null);
 			} else {
 				fileCopy = forkChildProcess("FileTransfer", fileCopyContext, generateUniqueName("FileTransfer"), null);
 			}
 			newEntry.setProcess(fileCopy.getUri());
 			inputXferProcess.put(newEntry.getName(), fileCopy);
 		}
 		
 		/*
 		 * Input File Processing
 		 * ---------------------
 		 * 
 		 * In a transaction, insert the FileTracker objects into the VM fileTable.
 		 * If any fileTracker object is already in the table due to some concurrent operation
 		 * add the filetable identifier of that process to the abortList. Outside of the
 		 * transaction, abort all processes on the abort list, and then init (start) the
 		 * remaining processes. 
 		 * 
 		 * The transaction updates the dependentOn list, and which at the end of this section
 		 * now contains the complete list of processes responsible for file placement on the
 		 * vm.
 		 * 
 		 */
 		
 		List<String> abortList = new ArrayList<String>();
 		putInputFilesIntoFileTable(target.getUri(), newEntries, abortList, dependentOn);
 
 		if(!abortList.isEmpty()) {
 			for(String fileTableId : abortList) {
 				CloudProcess p = inputXferProcess.remove(fileTableId);
 				abort(p);
 			}
 		}
 		
 		needsInit.addAll(inputXferProcess.values());	
 		
 		/*
 		 * Create the On command with execution dependencies of the file copy processes
 		 * that are transferring the needed files to the target VM.
 		 */
 		
 		
 		boolean isAsync = childContext.getBooleanValue("async");
 		
 		
 		childContext.putValue("command", assemblePieces(pieces));
 		childContext.putObjectValue("target", target.getUri());
 		
 		Variable on;
 		if(isOnExit) {
 			CloudProcess child = forkOnExitChildProcess("OnExit", childContext, specifiedName, dependentOn);	
 			needsInit.add(child);
 			on = this.context.get(child.getName());
 			
 		} else {
 			CloudProcess child = forkChildProcess("On", childContext, specifiedName, dependentOn);
 			if(!isAsync)
 				this.setWatchFor(child.getUri());	
 			needsInit.add(child);
 			on = this.context.get(child.getName());
 		}
 		
 		OnAction onAction = (OnAction) ActionResource.dao.load(URI.create(on.value()));
 		
 		
 		/*
 		 * Output File Processing
 		 * ----------------------
 		 * 
 		 * In a transaction, add the FileTracker elements into the VM file table, with the
 		 * production process set to the ON command.
 		 * 
 		 */
 		
 		putOutputFilesIntoFileTable(target.getUri(), onAction.getProcess(), producedOutputFiles);
 		
 		/*
 		 * Output File Processing
 		 * ----------------------
 		 * 
 		 * Create the set of output file processes with execution dependency on the ON process. The
 		 * output file process will transfer the generated output files to the cloud repo.
 		 */
 		if(!needTransfers.isEmpty()) {
 			for(FileTracker outputXfer : needTransfers) {
 				fileCopyContext.putValue("target", target.getUri());
 				fileCopyContext.putValue("name", outputXfer.getRepo().toString());
 				fileCopyContext.putValue("destination", outputXfer.getRepo());
 				fileCopyContext.putValue("source", URI.create("file:///"+outputXfer.getLocalName()));
 				fileCopyContext.putValue("fileTableId", outputXfer.getName());
 				CloudProcess fileCopy;
 				if(isOnExit) {
 					fileCopy = forkOnExitChildProcess("FileTransfer", fileCopyContext, generateUniqueName("FileTransfer"), Arrays.asList(outputXfer.getProcess()));					
 				} else {
 					fileCopy = forkChildProcess("FileTransfer", fileCopyContext, generateUniqueName("FileTransfer"), Arrays.asList(outputXfer.getProcess()));
 				}
 				needsInit.add(fileCopy);
 			}
 		}
 		
 		//
 		// Init processes
 		//
 		if(!isOnExit)
 			for(CloudProcess p : needsInit) {
 				processLifecycle().init(p);
 			}
 		else
 			processLifecycle().addOnExitProcesses(this.getProcess(), needsInit); 
 		
 		return on;
 		
 	}
 	
 	private void putOutputFilesIntoFileTable(final URI vmTarget, final URI onProcess,
 			final List<FileTracker> newEntries) {
 		ActionResource.dao.transact(new VoidWork() {
 
 			@Override
 			public void vrun() {
 				VMAction target = (VMAction) ActionResource.dao.load(vmTarget);
 				Map<String, FileTracker> workingFileTable = target.getFileTable();
 				for(FileTracker newEntry : newEntries) {
 					if(workingFileTable.containsKey(newEntry.getName())) {
 						log.severe("filetable shows multiple producers for "+newEntry.getName());
 						logger.error("multiple producers for "+newEntry.getName());
 						throw new IllegalArgumentException("multiple producers for "+newEntry.getName());
 					} else {
 						newEntry.setProcess(onProcess);
 						workingFileTable.put(newEntry.getName(), newEntry);
 					}
 				}
 				ActionResource.dao.update(target);
 			}});
 		
 	}
 
 	private void putInputFilesIntoFileTable(final URI vmTarget,  
 			final List<FileTracker> newEntries, final List<String> abortList, final Set<URI> dependentOn) {
 		ActionResource.dao.transact(new VoidWork() {
 
 			@Override
 			public void vrun() {
 				VMAction target = (VMAction) ActionResource.dao.load(vmTarget);
 				Map<String, FileTracker> workingFileTable = target.getFileTable();
 				abortList.clear();
 				for(FileTracker newEntry : newEntries) {
 					if(workingFileTable.containsKey(newEntry.getName())) {
 						abortList.add(newEntry.getName());
 						dependentOn.add(workingFileTable.get(newEntry.getName()).getProcess());
 					} else {
 						workingFileTable.put(newEntry.getName(), newEntry);
 						dependentOn.add(newEntry.getProcess());
 					}
 				}
 				ActionResource.dao.update(target);
 			}});
 	}
 	
 	/** Generate the list of input file needs for the command execution
 	 * @param context execution context including input files, and command parameters
 	 * @param cmd specification of the command including command input files
 	 * @return list of input files needed for command execution.
 	 * <br>The context has <i>needs</i> variable created for all command input files if <i>needsNone</i>=true not in the context
 	 */
 	protected List<FileTracker> resolveNeeds(Context context, Command cmd) {
 		boolean needsNone = context.getBooleanValue("needsNone");
 		Variable needs = context.get("needs");
 		List<FileTracker> inputList = new ArrayList<FileTracker>();
 		String missing = null;
 		if(needs == null) {
 			if(!needsNone) {
 				// default is NeedsAll
 				Map<String,String> inputs = new HashMap<String,String>();
 				for(FileSpecification i : Helpers.safeIterator(cmd.getInputFiles())) {
 					URI source = context.getFileValue(i.getName());
 					if(source == null) {
 						if(!i.isOptional()) {
 							log.warning("Missing file "+i.getName());
 							logger.error("Missing file "+i.getName());
 							if(missing == null)
 								missing = i.getName();
 							else
 								missing = missing+" "+i.getName();
 						}
 					} else {
 						inputs.put(i.getName(), i.getName());
 						FileTracker inputFile = new FileTracker();
 						inputFile.setName(i.getName());
 						inputFile.setLocalName(i.getName());
 						inputFile.setRepo(source);
 						inputList.add(inputFile);
 					}
 				}
 				if(missing != null) 
 					throw new NotFoundException("Missing file(s) "+missing);
 				context.putValue("needs", inputs);
 			}
 		} else {
 			@SuppressWarnings("unchecked")
 			Map<String, String> inputs = (Map<String, String>) context.getObjectValue("needs");
 			Set<String> optional = new HashSet<String>();
 			for(FileSpecification i : cmd.getInputFiles()) {
 				if(i.isOptional())
 					optional.add(i.getName());
 			}
 			for(Entry<String, String> i : inputs.entrySet()) {
 				URI source = context.getFileValue(i.getKey());
 				if(source == null && !optional.contains(i.getKey())) {
 					
 					log.warning("Missing file "+i.getKey());
 					logger.error("Missing file "+i.getKey());
 					if(missing == null)
 						missing = i.getKey();
 					else
 						missing = missing+" "+i.getKey();
 				} else {
 					FileTracker inputFile = new FileTracker();
 					inputFile.setName(i.getKey());
 					inputFile.setLocalName(i.getValue());
 					inputFile.setRepo(source);
 					inputList.add(inputFile);
 				}
 			}
 		}
 		return inputList;
 	}
 	
 	/** Generates list of output files produced by the command
 	 * @param context context containing the command invocation parameters and the produces specification
 	 * @param cmd the command definition describing the output files
 	 * @param needTransfers list of output file transfers to the cloud repo
 	 * @return list of output files produced by the command
 	 * <br> context variable <i>produces</i> is generated if absent an the context variable <i>producesAll</i>==true
 	 */
 	protected List<FileTracker> resolveProduces(Context context, Command cmd, List<FileTracker> needTransfers) {
 		boolean producesall = context.getBooleanValue("producesAll");
 		Variable produces = context.get("produces");
 		List<FileTracker> production = new ArrayList<FileTracker>();
 		if(produces == null) {
 			// producesNone is the default
 			if(producesall) {
 				Map<String,String> outputs = new HashMap<String,String>();
 				for(FileSpecification i : Helpers.safeIterator(cmd.getOutputFiles())) {
 					URI destination = context.getFileValue(i.getName());
 					outputs.put(i.getName(), i.getName());
 					FileTracker outputFile = new FileTracker();
 					outputFile.setName(i.getName());
 					outputFile.setLocalName(i.getName());
 					outputFile.setRepo(destination);
 					production.add(outputFile);
 					if(destination != null ) {
 						outputFile.setRepo(destination);
 						needTransfers.add(outputFile);
 					}
 				}
 				context.putValue("produces", outputs);
 				
 			}
 		} else {
 			@SuppressWarnings("unchecked")
 			Map<String, String> outputs = (Map<String, String>) context.getObjectValue("produces");
 			for(Entry<String, String> i : outputs.entrySet()) {
 				URI destination = context.getFileValue(i.getKey());
 				FileTracker outputFile = new FileTracker();
 				outputFile.setName(i.getKey());
 				outputFile.setLocalName(i.getValue());
 				outputFile.setRepo(destination);
 				production.add(outputFile);
 				if(destination != null ) {
 					needTransfers.add(outputFile);
 				}
 			}
 		}
 		
 		return production;
 		
 	}
 
 	private Variable optionParse(ShellFragment option) throws IllegalArgumentException, UnexpectedTypeException, NotFoundException {
 		Variable result = null;
 		String optionName = option.value;
 		if(option.children != null && option.children.length == 1) {
 			ShellFragment arg = this.executable.get(option.children[0]);
 			switch(arg.kind) {
 			case literalArg:
 				result = new Variable(optionName, arg.value);
 				break;
 			case fileList:
 				Map<String,String> fileList = new HashMap<String,String>();
 				if(arg.children != null) {
 					for(int i : arg.children){
 						String combo = this.executable.get(i).value;
 						String[] parts = combo.split(":");
 						fileList.put(parts[0], parts[1]);
 					}
 				}
 				result = new Variable(optionName, fileList);
 				break;
 			case expression:
 				result = new Variable(optionName, new ExpressionEngine(this.executable, this.context, this.getOwner()).expression(arg));
 				break;
 			default:
 				throw new IllegalArgumentException("Found "+arg.kind);
 			}
 		}
 		
 		return result;
 	}
 	
 	private String assemblePieces(ShellFragment pieces) throws IllegalArgumentException, UnexpectedTypeException, NotFoundException {
 		StringBuffer result = new StringBuffer();
 		ExpressionEngine ee = new ExpressionEngine(this.executable, this.context, this.getOwner());
 		boolean first = true;
 		for(int i : pieces.children) {
 			ShellFragment piece = this.executable.get(i);
 			if(piece.kind == ShellFragmentKind.passThru) {
 				result.append(piece.value);
 			} else {
 				try {
 					result.append((!first && piece.value!=null)?piece.value+ee.expression(piece).toString():ee.expression(piece).toString());
 				} catch (IllegalArgumentException e) {
 					log.info("Illegal argument: "+e.getMessage());
 					logger.error("Illegal argument: "+e.getMessage());
 					throw e;
 				} catch (UnexpectedTypeException e) {
 					log.info("Unexpected type: "+e.getMessage());
 					logger.error("Unexpected type: "+e.getMessage());
 					throw e;
 				} catch (NotFoundException e) {
 					log.info("Not found: "+e.getMessage());
 					logger.error("Not found: "+e.getMessage());
 					
 					throw e;
 				}
 			}
 			first = false;
 		}
 		return result.toString();
 	}
 	
 	
 	
 	
 	/** FOR shell command
 	 * < FORLOOP > variable() < COLON > expression() [ <COLON > expression() ] block
 	 * block:: ( command() )*
 	 * @param shellFragment
 	 * @return
 	 * @throws NotFoundException
 	 * @throws IllegalArgumentException
 	 * @throws ClassNotFoundException
 	 * @throws UnexpectedTypeException 
 	 */
 	private Variable forCommand(ShellFragment shellFragment, String specifiedName) throws NotFoundException, IllegalArgumentException, ClassNotFoundException, UnexpectedTypeException {
 		int children = (shellFragment.children == null) ? 0 : shellFragment.children.length ;
 		if(!(children == 3 || children == 4))
 			throw new IllegalArgumentException("For command has "+children+" children");
 		
 		ShellFragment expression = this.executable.get(shellFragment.children[1]);
 		ExpressionEngine ee = new ExpressionEngine(this.executable, this.context, this.getOwner());
 		Object countObject = ee.expression(expression);
 		int count;
 		if(countObject instanceof Long) {
 			count = ((Long)countObject).intValue();
 		} else if(countObject instanceof Double) {
 			count = ((Double)countObject).intValue();
 		} else {
 			throw new UnexpectedTypeException(countObject, "number");
 		}
 		int chunk = count;
 		if(children == 4) {
 			expression = this.executable.get(shellFragment.children[2]);
 			countObject = ee.expression(expression);
 			if(countObject instanceof Long) {
 				chunk = ((Long)countObject).intValue();
 			} else if(countObject instanceof Double) {
 				chunk = ((Double)countObject).intValue();
 			} else {
 				throw new UnexpectedTypeException(countObject, "number");
 			}
 		}
 		String iterator = this.executable.get(shellFragment.children[0]).value.substring(2);
 		
 		Context childContext = new Context();
 		childContext.putAll(this.context);
 		childContext.remove("name");
 		childContext.putValue("iterator", iterator);
 		childContext.putValue("n", count);
 		childContext.putValue("chunkSize", chunk);
 		
 		CloudProcess child = forkChildProcess("For", childContext, specifiedName, null);
 		ForAction forAction = (ForAction) ActionResource.dao.load(child.getAction());
 		forAction.setExecutable(new ArrayList<ShellFragment>(this.executable.subList(0, shellFragment.children[children-1]+1)));
 		forAction.setCloud(this.getCloud());
 		forAction.setCommand(this.getCommand());
 		forAction.setExecutableName(this.getExecutableName());
 		ActionResource.dao.update(forAction);
 		processLifecycle().init(child);
 		return this.context.get(child.getName());
 
 	}
 	
 
 	/** variableAssign shell command
 	 * 
 	 * < VARIABLE > "=" ( simpleCommand() | expression() )
 	 * 
 	 * @param varAssign
 	 * @return
 	 * @throws NotFoundException
 	 * @throws IllegalArgumentException
 	 * @throws ClassNotFoundException
 	 * @throws WaitForSignalRequest 
 	 * @throws UnexpectedTypeException 
 	 */
 	private Variable variableAssignCommand(ShellFragment varAssign) throws NotFoundException, IllegalArgumentException, ClassNotFoundException, UnexpectedTypeException, WaitForSignalRequest {
 
 		String variableName = varAssign.value;
 		ShellFragment fragment = this.executable.get(varAssign.children[0]);
 		if(fragment.kind == ShellFragmentKind.expression) {
 			ExpressionEngine ee = expressionEngineFactory(fragment.children[0]);
 			this.context.putObjectValue(variableName, ee.run());
 			return this.context.get(variableName);
 		} else {
 			return this.execute(varAssign.children[0], variableName);
 		}
 	}
 	
 	/** export shell command
 	 * 
 	 * < EXPORT > < VARIABLE >
 	 * 
 	 * @param export
 	 * @return
 	 * @throws NotFoundException
 	 * @throws UnprocessableEntityException 
 	 */
 	private Variable exportCommand(ShellFragment export) throws NotFoundException {
 
 		String variableName = export.value.substring(2); // strip leading $$
 		Variable v = this.context.get(variableName);
 		if(v == null) {
 			log.warning(variableName+" not found");
 			logger.warning(variableName+" not found");
 			throw new NotFoundException(variableName);
 		}
 		
 		URI root = processLifecycle().getProcessRoot(this.getProcess());
 		processLifecycle().insertIntoContext(root, v);
 		
 		return v;
 	}
 	
 	
 	/** LOG shell command
 	 * 
 	 * LOG:: < LOG > pieces()
 	 * PIECES:: ( expression | passThru )+
 	 * 
 	 * @param logFragment
 	 * @return
 	 * @throws NotFoundException
 	 * @throws IllegalArgumentException
 	 * @throws ClassNotFoundException
 	 * @throws UnexpectedTypeException 
 	 */
 	private Variable logCommand(ShellFragment logFragment, String specifiedName) throws NotFoundException, IllegalArgumentException, ClassNotFoundException, UnexpectedTypeException {
 		
 		ShellFragment pieces = this.executable.get(logFragment.children[0]);
 		String arg = assemblePieces(pieces);
 		if(Helpers.isBlankOrNull(specifiedName)) {
 			LogAction.generateLog(logger, arg);
 			return null;
 		}
 		Context childContext = new Context();
 		childContext.putAll(this.context);
 		// Dont childContext.remove("name"); -- use the parents name
 		childContext.putValue("arg", arg);
 		
 		return makeChildProcess("Log", childContext, specifiedName, false, null);
 	}
 	
 	
 	/** DESTROY shell command
 	 * 
 	 * DESTROY:: < DESTROY >	 expression()
 	 * 
 	 * @param destroy
 	 * @return
 	 * @throws NotFoundException
 	 * @throws IllegalArgumentException
 	 * @throws ClassNotFoundException
 	 * @throws UnexpectedTypeException 
 	 */
 	private Variable destroyCommand(ShellFragment destroy) throws NotFoundException, IllegalArgumentException, ClassNotFoundException, UnexpectedTypeException {
 		Variable result = null;
 		ShellFragment fragment = this.executable.get(destroy.children[0]);
 		if(fragment.kind == ShellFragmentKind.expression) {
 			ExpressionEngine ee = expressionEngineFactory(fragment.children[0]);
 			Object o = ee.run();
 			URI[] targets = new URI[0];
 			String name = null;
 			URI first = null;
 			if(o instanceof URI) {
 				targets = new URI[] { (URI)o };
 			} else if(o instanceof String) {
 				targets = new URI[] { URI.create((String)o) };
 			} else if(o instanceof List<?>) {
 				@SuppressWarnings("unchecked")
 				List<String> vms = (List<String>)o;
 				if(vms.isEmpty())
 					throw new IllegalArgumentException("Null list");
 				targets = new URI[vms.size()];
 				for(int i=0; i < targets.length; i++) {
 					targets[i] = URI.create(vms.get(i));
 				}		
 			} else {
 				logger.error("URI target expected, got"+(o==null?"":" "+o.getClass().getSimpleName())+"  "+o);
 				log.info("URI target expected, got"+(o==null?"":" "+o.getClass().getSimpleName())+"  "+o);
 				throw new IllegalArgumentException("URI target expected, got"+(o==null?"":" "+o.getClass().getSimpleName())+"  "+o);
 			}
 			
 			for(URI target : targets) {
 				if(target.toString().contains("process")) {
 					CloudProcess process = CloudProcessResource.dao.load(target);
 					target = process.getAction();
 				}
 				
 				Action action;
 				if(target.toString().contains("action")) {
 					action = ActionResource.dao.load(target);
 					if(name == null) {
 						name = action.getName();
 						first = target;
 					}
 					if(action instanceof CreateVMAction) {
 						CreateVMAction createVMAction = (CreateVMAction) action;
 						createVMAction.killVM();
 					} else  {
 						processLifecycle().dump(action.getProcess());
 					} 
 				} else {
 					log.warning(target+" is not a valid termination target");
 					logger.error(target+" is not a valid termination target");
 					throw new IllegalArgumentException(target+" is not a valid termination target");
 				}
 			}
 
 			result = new Variable("destroy_"+name, first);
 		}
 		return result;
 	}
 	
 
 	/*
 	 * Helpers
 	 * =======
 	 */
 	
 	private Variable lookupAction(String name) {
 		int fieldIdx = name.indexOf(".");
 		Variable result;
 		String primaryName = name;
 		if(fieldIdx > 0) {
 			primaryName = name.substring(0, fieldIdx);
 		}
 		result = this.context.get(primaryName);
 		return result;
 	}
 	
 	private Set<URI> buildDependencySetFor(ShellFragment s)   {
 		Set<String> references = new HashSet<String>();
 		Set<URI> dependencies = new HashSet<URI>();
 		
 		scanExpressionTreeForDependencies(s, references);
 		log.info("Found references "+references);
 		if(!references.isEmpty()) {
 			for(String identifier : references) {
 				Variable v = lookupAction(identifier);
 				if(v != null) {
 					if(v.getType() == VariableType.Action) {
 						URI actionURI = URI.create(v.value());
 						dependencies.add(actionURI);
 					}
 				} 
 			}
 		}
 		return dependencies;
 	}
 	
 	
 	private void scanExpressionTreeForDependencies(ShellFragment s, Set<String> dependencies) {
 		if(s.kind == ShellFragmentKind.identifier) {
 			dependencies.add(s.value);
 		} if(s.kind == ShellFragmentKind.block) {
 			return; // dont scan blocks
 		}
 		if(s.children != null) {
 			for(int i : s.children) {
 				ShellFragment f = this.executable.get(i);
 				scanExpressionTreeForDependencies(f, dependencies);
 			}
 		} 
 	}
 	
 	/** sets process to block until a set of dependencies is available
 	 * @param dependencies set of dependencies
 	 * @return false if process must block, true if dependences are available
 	 */
 	private boolean assertDependenciesAvailable(Set<URI> dependencies) {
 		List<URI> dependency = null;
 		if(!dependencies.isEmpty()) {
 			dependency = new ArrayList<URI>(dependencies.size());
 			for(Action action: ActionResource.dao.load(dependencies)) {
 				dependency.add(action.getProcess());
 			}
 			return !processLifecycle().setDependentOn(this.getProcess(), dependency);
 		}
 		return true;
 	}
 
 	/** Fetches the command definition associated with a data store URI.
 	 * @param uri reference to the command definition in the form: 
 	 * <BR> <i>datastore-uri</i>#<i>name of CommandImplementationDefinition</i>?start=<i>start</i>
 	 * <BR>
 	 * For example: http://foo.com/resources/command/5#EC2?start=5
 	 * If start is not specified, then the default starting point is assumed.
 	 *
 	 * @return
 	 */
 	protected CommandImplementationDefinition initalizeExecutableFromCommandImplementationDefinition(URI uri) throws NotFoundException {
 		URI baseURI;
 		try {
 			baseURI = new URI(uri.getScheme(), uri.getAuthority(), uri.getPath(), null, null);
 		} catch (URISyntaxException e) {
 			log.log(Level.SEVERE, "Parse of "+uri, e);
 			throw new NotFoundException(uri.toString());
 		}
 		log.info("Uri ="+uri.toString());
 		String name = uri.getFragment(); 
 		String start = uri.getQuery();
 		Command cmd = loadCommandDefinition(baseURI);
 		for(CommandImplementationDefinition cid : Helpers.safeIterator(cmd.getImplementations())) {
 			log.info("looking for "+name+" against "+cid.getName());
 			if(cid.getName().equals(name)) {
 				if(start != null) {
 					int postStart = start.indexOf("start=");
 					int index = Integer.valueOf(start.substring(postStart+1));
 					this.start = index;
 					this.executable = cid.getCompiled().subList(0, index+1);
 				} else {
 					this.start = -1;
 					this.executable = cid.getCompiled();
 				}
 				this.executableName = cmd.getName()+(this.start==-1?" ":" sub-shell of ")+cmd.getVersion()+(cmd.isPreferred()?"":"*")+" on "+cid.getName();
 				this.command = baseURI.toString();
 				this.cloud = cid.getName();
 				return cid;
 			}
 		}
 		throw new NotFoundException(uri.toString());
 	}
 	
 	private Variable makeChildProcess(String processType, Context childContext, String variableName, boolean isAsync, Set<URI> dependency) throws NotFoundException, IllegalArgumentException, ClassNotFoundException {
 		
 		CloudProcess child = forkChildProcess(processType, childContext, variableName, dependency);
 		if(!isAsync)
 			this.setWatchFor(child.getUri());	
 		processLifecycle().init(child);
 		return this.context.get(child.getName());
 	}
 	
 	
 	private CloudProcess forkChildProcess(String processType, Context childContext, String variableName, List<URI> dependency) throws NotFoundException, IllegalArgumentException, ClassNotFoundException {
 		CloudProcess child = forkChildProcessNoTracking(processType, childContext, variableName, dependency, this.getProcess());
 		this.active.add(child.getUri().toString());
 		return child;
 	}
 	
 	private CloudProcess forkChildProcessNoTracking(String processType, Context childContext, String variableName, List<URI> dependency, URI parent) throws NotFoundException, IllegalArgumentException, ClassNotFoundException {
 		
 		String optionName = childContext.getValue("name");
 		if(Helpers.isBlankOrNull(variableName)) {
 			variableName = optionName;
 			if(Helpers.isBlankOrNull(variableName)) {
 				variableName = generateUniqueName(processType);
 			}
 		}
 		CloudProcess child = processLifecycle().spawn(this.getOwner(), variableName, childContext, dependency, parent, processType);	
 		this.context.putActionValue(variableName, child.getAction());
 		trackCreation(processType);
 		return child;
 	}
 	
 	private CloudProcess forkChildProcess(String processType, Context childContext, String variableName, Collection<URI> dependency) throws NotFoundException, IllegalArgumentException, ClassNotFoundException {
 		List<URI>dependsOn = null;
 		if(dependency != null) {
 			dependsOn = new ArrayList<URI>();
 			dependsOn.addAll(dependency);
 		}
 		
 		return forkChildProcess(processType, childContext, variableName, dependsOn);
 		
 	}
 	
 	private CloudProcess forkOnExitChildProcess(String processType, Context childContext, String variableName, Collection<URI> dependency) throws NotFoundException, IllegalArgumentException, ClassNotFoundException {
 		List<URI>dependsOn = null;
 		if(dependency != null) {
 			dependsOn = new ArrayList<URI>();
 			dependsOn.addAll(dependency);
 		}
 
 		return forkChildProcessNoTracking(processType, childContext, variableName, dependsOn, processLifecycle().getProcessRoot(this.getProcess()));
 	}
 	
 	private void abort(CloudProcess p) {
 		this.context.remove(p.getName());
 		this.active.remove(p.getUri().toString());
 		ActionResource.dao.delete(ActionResource.dao.load(p.getAction()));
 		CloudProcessResource.dao.delete(p);
 		
 	}
 	
 	private void trackCreation(String processType) {
 		int created = this.context.getIntegerValue(processType+"Created");
 		this.context.putValue(processType+"Created", created+1);
 	}
 	private String generateUniqueName(String processType) {
 		String nameSeed = context.getValue(processType+"NameSeed");
 		if(nameSeed == null)
 			nameSeed = processType;
 		return nameSeed +"_"+this.context.getIntegerValue(processType+"Created");
 	}
 
 	/*
 	 * Getters and Setters
 	 * ===================
 	 * 
 	 */
 	
 	/**
 	 * @return the executable
 	 */
 	public List<ShellFragment> getExecutable() {
 		return executable;
 	}
 
 	/**
 	 * @return the executableName
 	 */
 	public String getExecutableName() {
 		return executableName;
 	}
 
 	/**
 	 * @param executableName the executableName to set
 	 */
 	public void setExecutableName(String executableName) {
 		this.executableName = executableName;
 	}
 
 	/**
 	 * @param executable the executable to set
 	 */
 	public void setExecutable(List<ShellFragment> executable) {
 		this.executable = executable;
 	}
 
 	/**
 	 * @return the pc
 	 */
 	public int getPc() {
 		return pc;
 	}
 
 	/**
 	 * @param pc the pc to set
 	 */
 	public void setPc(int pc) {
 		this.pc = pc;
 	}
 
 	/**
 	 * @return the watchFor
 	 */
 	public URI getWatchFor() {
 		return Helpers.stringToURI(watchFor);
 	}
 
 	/**
 	 * @param watchFor the watchFor to set
 	 */
 	public void setWatchFor(URI watchFor) {
 		this.watchFor = Helpers.URItoString(watchFor);
 	}
 	
 	/**
 	 * @return the abnormally terminating child URI
 	 */
 	public URI getAbnormalTermination() {
 		return Helpers.stringToURI(abnormalTermination);
 	}
 
 	/**
 	 * @param child set the abnormally terminating child
 	 */
 	public void setAbnormalTermination(URI child) {
 		this.abnormalTermination = Helpers.URItoString(child);
 	}
 	
 	/**
 	 * @return the command
 	 */
 	public URI getCommand() {
 		return Helpers.stringToURI(command);
 	}
 
 	/**
 	 * @param command the command to set
 	 */
 	public void setCommand(URI command) {
 		this.command = Helpers.URItoString(command);
 	}
 
 	/**
 	 * @return the cloud
 	 */
 	public String getCloud() {
 		return cloud;
 	}
 
 	/**
 	 * @param cloud the cloud to set
 	 */
 	public void setCloud(String cloud) {
 		this.cloud = cloud;
 	}
 
 	/**
 	 * @return the start
 	 */
 	public int getStart() {
 		return start;
 	}
 
 	/**
 	 * @param start the start to set
 	 */
 	public void setStart(int start) {
 		this.start = start;
 	}
 	
 	
 
 	/**
 	 * @return the adopted
 	 */
 	public List<String> getAdopted() {
 		return adopted;
 	}
 
 	/**
 	 * @param adopted the adopted to set
 	 */
 	public void setAdopted(List<String> adopted) {
 		this.adopted = adopted;
 	}
 
 	/**
 	 * @return the active
 	 */
 	public List<String> getActive() {
 		return active;
 	}
 
 	/**
 	 * @param active the active to set
 	 */
 	public void setActive(List<String> active) {
 		this.active = active;
 	}
 
 	
 	
 	/*
 	 * Object housekeeping
 	 * ===================
 	 */
 	
 
 	/* (non-Javadoc)
 	 * @see java.lang.Object#toString()
 	 */
 	@Override
 	public String toString() {
 		return String
 				.format("NShellAction %s [executableName=%s, pc=%s, watchFor=%s, abnormalTermination=%s, command=%s, cloud=%s, start=%s, adopted=%s, active=%s, executable=%s, logger=%s]",
 						super.toString(),
 						executableName, pc, watchFor, abnormalTermination,
 						command, cloud, start, adopted, active, executable,
 						logger);
 	}
 
 	/* (non-Javadoc)
 	 * @see java.lang.Object#hashCode()
 	 */
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = super.hashCode();
 		result = prime
 				* result
 				+ ((abnormalTermination == null) ? 0 : abnormalTermination
 						.hashCode());
 		result = prime * result + ((active == null) ? 0 : active.hashCode());
 		result = prime * result + ((adopted == null) ? 0 : adopted.hashCode());
 		result = prime * result + ((cloud == null) ? 0 : cloud.hashCode());
 		result = prime * result + ((command == null) ? 0 : command.hashCode());
 		result = prime * result
 				+ ((executable == null) ? 0 : executable.hashCode());
 		result = prime * result
 				+ ((executableName == null) ? 0 : executableName.hashCode());
 		result = prime * result + ((logger == null) ? 0 : logger.hashCode());
 		result = prime * result + pc;
 		result = prime * result + start;
 		result = prime * result
 				+ ((watchFor == null) ? 0 : watchFor.hashCode());
 		return result;
 	}
 
 	/* (non-Javadoc)
 	 * @see java.lang.Object#equals(java.lang.Object)
 	 */
 	@Override
 	public boolean equals(Object obj) {
 		if (this == obj)
 			return true;
 		if (!super.equals(obj))
 			return false;
 		if (getClass() != obj.getClass())
 			return false;
 		NShellAction other = (NShellAction) obj;
 		if (abnormalTermination == null) {
 			if (other.abnormalTermination != null)
 				return false;
 		} else if (!abnormalTermination.equals(other.abnormalTermination))
 			return false;
 		if (active == null) {
 			if (other.active != null)
 				return false;
 		} else if (!active.equals(other.active))
 			return false;
 		if (adopted == null) {
 			if (other.adopted != null)
 				return false;
 		} else if (!adopted.equals(other.adopted))
 			return false;
 		if (cloud == null) {
 			if (other.cloud != null)
 				return false;
 		} else if (!cloud.equals(other.cloud))
 			return false;
 		if (command == null) {
 			if (other.command != null)
 				return false;
 		} else if (!command.equals(other.command))
 			return false;
 		if (executable == null) {
 			if (other.executable != null)
 				return false;
 		} else if (!executable.equals(other.executable))
 			return false;
 		if (executableName == null) {
 			if (other.executableName != null)
 				return false;
 		} else if (!executableName.equals(other.executableName))
 			return false;
 		if (logger == null) {
 			if (other.logger != null)
 				return false;
 		} else if (!logger.equals(other.logger))
 			return false;
 		if (pc != other.pc)
 			return false;
 		if (start != other.start)
 			return false;
 		if (watchFor == null) {
 			if (other.watchFor != null)
 				return false;
 		} else if (!watchFor.equals(other.watchFor))
 			return false;
 		return true;
 	}
 	
 	
 	/*
 	 * Testing
 	 * =======
 	 */		
 
 	/** Fetches the command definition associated with a data store URI.
 	 * Override method for unit testing
 	 * @param uri
 	 * @return
 	 * @throws NotFoundException
 	 */
 	protected Command loadCommandDefinition(URI uri) throws NotFoundException {
 		Command cmd = CommandResource.dao.load(uri);
 		return cmd;
 	}
 
 	protected ExpressionEngine expressionEngineFactory(int index) {
 		return new ExpressionEngine(this.executable.subList(0, index+1), this.context, this.getOwner());
 	}
 	
 	protected ProcessLifecycle processLifecycle() {
 		return ProcessLifecycle.mgr();
 	}
 }
