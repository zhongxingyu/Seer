 /**
  * 
  */
 package ch.ethz.e4mooc.server;
 
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.IOException;
 import java.util.Map;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import org.apache.commons.exec.CommandLine;
 import org.apache.commons.exec.DefaultExecuteResultHandler;
 import org.apache.commons.exec.DefaultExecutor;
 import org.apache.commons.exec.ExecuteException;
 import org.apache.commons.exec.ExecuteWatchdog;
 import org.apache.commons.exec.Executor;
 import org.apache.commons.exec.PumpStreamHandler;
 import org.apache.commons.exec.environment.EnvironmentUtils;
 import org.apache.commons.io.FileUtils;
 
 import ch.ethz.e4mooc.client.ExecutionService;
 import ch.ethz.e4mooc.server.util.ProjectModel;
 import ch.ethz.e4mooc.server.util.ServerProperties;
 import ch.ethz.e4mooc.server.util.ServerState;
 import ch.ethz.e4mooc.server.util.ServerSystem;
 import ch.ethz.e4mooc.shared.CompilationResultDTO;
 
 import com.google.gwt.user.server.rpc.RemoteServiceServlet;
 
 /**
  * @author hce
  *
  */
 public class ExecutionServiceImpl extends RemoteServiceServlet implements ExecutionService {
 
 	/** Generated serial id */
 	private static final long serialVersionUID = 1L;
 	
 	// NOTE: all these variables are thread-safe because they are final, thus immutable
 	private final static Logger LOGGER = Logger.getLogger(ch.ethz.e4mooc.server.ExecutionServiceImpl.class.getName());
 	
 	private final String e4mooc = ServerProperties.E4MOOC_PROJECTS_FOLDER.toString();
 	private final String e4moocTmp = ServerProperties.E4MOOC_TMP_FOLDER.toString();
 	private final String SEP = System.getProperty("file.separator");
 	
 	public CompilationResultDTO compile(String projectName, Map<String, String> inputFiles, String timeStampOfLastCompilation) {
 		
 		// get the session id
 		String sessionId = this.getThreadLocalRequest().getSession().getId();
 		
 		
 		LOGGER.log(Level.INFO, "Got request to \"Compile\" Eiffel project: " + projectName + ". Session id: " + sessionId);
 		
 		// first, we check if we got a valid time stamp from a previous compilation
 		// if yes AND the time stamp is not too old, we delete the previously generated tmp-compilation-folder
 		// if it is too old already, then the clean-up thread will take care of it
 		try {
 			Long tsolc = Long.parseLong(timeStampOfLastCompilation);
 			if(System.currentTimeMillis() - tsolc < 1000 * 60 * 10) {
 				// less than 10 minutes have passed since the last compilation
 				// thus the clean-up thread will not have deleted the folder yet
 				
 				// we check if the folder exists and if yes, we delete it
 				File tmpFolderOfLastCompilation = new File(e4moocTmp + SEP + sessionId + "_" + timeStampOfLastCompilation);
 				if(tmpFolderOfLastCompilation.exists()) {
 					FileUtils.deleteQuietly(tmpFolderOfLastCompilation);
 				}
 			}
 		} catch (NumberFormatException e) {
 			
 		}
 		
 		
 		CommandLine cmdInstruction = null;
 		String resultMsg = "Unable to compile the program. Please report this via email to marco.piccioni{at}inf.ethz.ch";
 				
 		String timeStamp = String.valueOf(System.currentTimeMillis());
 		// the path to the temporary folder
 		String pathOfTmpFolder = e4moocTmp + SEP + sessionId + "_" + timeStamp ;
 		// the path to the temporary folder + the folder with the project name
 		String pathOfTmpFolderAndProject = pathOfTmpFolder + SEP + projectName;
 
 		// copy the project folder into the tmp folder
 		try {
 			copyProjectToFolder(pathOfTmpFolder, projectName, inputFiles);
 		} catch (Exception e) {
 			// the user get's a back an error message. Where exactly the error occurred is already logged to console by the method causing the error.
 			String msg = "An error occured while preparing the compilation on the server. Please try again.\nIf the error reoccurrs report this via email to marco.piccioni{at}inf.ethz.ch";
 			return new CompilationResultDTO(msg, timeStamp, false); 
 		}
 		
 		// create the command line instruction: ec -config projectName.ecf -stop -c_compile
 		cmdInstruction = getCommandLine("ec");
 		cmdInstruction.addArgument("-config");
 		cmdInstruction.addArgument(pathOfTmpFolder + SEP + ServerState.getState().getProjectModel(projectName).getEcfFile());
 		cmdInstruction.addArgument("-stop");
 		cmdInstruction.addArgument("-c_compile");
 		//cmdInstruction.addArgument("-freeze");	// we need freezing because only with C compilation can we redirect a programs output into a file
 		cmdInstruction.addArgument("-project_path");
 		cmdInstruction.addArgument(pathOfTmpFolderAndProject);
 		
 		resultMsg = execute(cmdInstruction, 1000 * 180); // should timeout after 3 minutes
 		
 		// the user called this compile method thus we can set/update the time stamp for this project
 		ServerState.getState().setTmpFolderAndTimeStamp(pathOfTmpFolderAndProject);
 	
 		CompilationResultDTO result;
 		if(resultMsg.endsWith("completed" + System.lineSeparator()))
 			result = new CompilationResultDTO(resultMsg, timeStamp, true);
 		else
 			result = new CompilationResultDTO(resultMsg, timeStamp, false);
 		
 		return result;
 	}
 	
 	@Override
 	public String execute(String projectName, String timeStamp) {
 		
 		String sessionId = this.getThreadLocalRequest().getSession().getId();
 		// the path to the temporary folder
 		String pathOfTmpFolder = e4moocTmp + SEP + sessionId + "_" + timeStamp;
 		// the path to the temporary folder + the folder with the project name
 		String pathOfTmpFolderAndProject = pathOfTmpFolder + SEP + projectName;
 		// get the name of the ecf file
 		String ecfFileName = ServerState.getState().getEcfFileNameWithoutAnyPath(projectName);
 		
 		LOGGER.log(Level.INFO, "Got request to \"Run\" Eiffel project: " + projectName + ". Session id: " + sessionId);
 		
 		
 		// first we check how long it's been since the program was compiled
 		Long timeOfLastCompilation = 0L;
 		try {
 			timeOfLastCompilation = Long.parseLong(timeStamp);
 		} catch(NumberFormatException e) {
 			System.err.println("Exception while trying to parse time stamp.");
 			LOGGER.log(Level.SEVERE, "Exception while trying to parse time stamp of value: " + timeStamp);
 			e.printStackTrace();
 		}
 		
 		if(System.currentTimeMillis() - timeOfLastCompilation > 1000 * 60 * 10) // return this message if the last compilation is too old
 			return "Your previously compiled project has been deleted from the server by now.\nPlease recompile first and then run again.";
 		
 		CommandLine cmdInstruction = null;
 		String result = "Unable to run the program. Try to compile it again.\nIf the error remains, please report this via email to marco.piccioni{at}inf.ethz.ch";
 		
 		// the path where the executable is stored (the generated W_code folder)
 		String W_codePath = pathOfTmpFolderAndProject + SEP + "EIFGENs" + SEP + ecfFileName + SEP + "W_code" + SEP;
 		
 		if(ServerProperties.SERVER_SYSTEM == ServerSystem.WIN_SANDBOXED) {
 			// on windows we execute the program inside a sandbox using www.sandboxie.com
 			// 1) we need to create a batch file which start the executable and redirects the output into a file
 			// 2) the output file is stored within the sandbox
 			// 3) we read the file, send it to the user and then delete it from the sandbox
 			
 			// create the content of  batch file: calling the executable and redirecting its output to a file
 			String e4moocTmpOutputFileName = W_codePath + "e4mooc_tmp_output.txt";
 			//String batchFileContent = "mkdir " + W_codePath + System.lineSeparator();
 			//batchFileContent += "copy NUL " + e4moocTmpOutputFileName + System.lineSeparator();
 			String batchFileContent = W_codePath + ecfFileName + ".exe > "  +  e4moocTmpOutputFileName + System.lineSeparator();
 			//batchFileContent += "timeout 1" + System.lineSeparator(); // sleep for a second
 			String batchFileName = W_codePath + "e4mooc_tmp_run.bat";
 			
 			
 			String dummyFileContent = "eJJN8nj>#])Wyk?5mapC" + System.currentTimeMillis();
 			
 			// NOTE: because of the strange behavior of Sandboxie we first create an empty
 			// file in the location where we'll store the output file that's generated by the batch file
 			File emptySandboxedFile = new File(ServerProperties.SANDBOXIE_DRIVE + SEP + e4moocTmpOutputFileName.replaceFirst(":", ""));
 			emptySandboxedFile.getParentFile().mkdirs();
 
 			try {
 				FileUtils.writeStringToFile(emptySandboxedFile, dummyFileContent);
 			} catch (IOException e1) {
 				// TODO Auto-generated catch block
 				e1.printStackTrace();
 			}
 
 			
 			// next we have to create the batch file for running the Eiffel program and redirecting it's output into a file
 			try {
 				// try to write the batch file
 				writeInputTextToFile(batchFileContent, batchFileName);
 			} catch (IOException e) {
 				LOGGER.log(Level.SEVERE, "Exeception thrown while creating batch file: " + batchFileName);
 				System.err.println(e.getMessage());
 				e.printStackTrace();
 			}
 			
 			// now we have the batch file so we can run it within the sandbox			
 			cmdInstruction = getCommandLine(ServerProperties.SANDBOXIE_START);
 			cmdInstruction.addArgument("/hide_window");
 			cmdInstruction.addArgument("/wait");
 			cmdInstruction.addArgument(batchFileName);
 
 			
 			// run the program
			result = execute(cmdInstruction, 1000 * 180); // timeout after 3 minutes (result will here actually be empty)
 			
			// now there should exist and output file containing the the output from the executable
 			// it's stored in the Sandbox drive folder; NOTE: the filepath can't contain "C:\" but only "C", have to clean that up
 			File e4moocTmpOutput = new File(ServerProperties.SANDBOXIE_DRIVE + SEP + e4moocTmpOutputFileName.replaceFirst(":", ""));
 			if(FileUtils.waitFor(e4moocTmpOutput, 5)) {
 				try {
 					
 					try {
 						Thread.sleep(200);
 					} catch (InterruptedException e1) {
 						// TODO Auto-generated catch block
 						e1.printStackTrace();
 					}
 					
 					
 					int waitCounter = 0;
 					while(FileUtils.readFileToString(e4moocTmpOutput).equals(dummyFileContent) && waitCounter < 30) {
 						waitCounter++;
 						try {
 							Thread.sleep(200);
 						} catch (InterruptedException e) {
 							// TODO Auto-generated catch block
 							e.printStackTrace();
 						}
 					}
 					
 					result = FileUtils.readFileToString(e4moocTmpOutput);
 					
 					// if we still have the dummy output in the file then there went something wrong.. so we return an error message
 					if(result.equals(dummyFileContent))
 						result = "There was a problem generating the output of your program. Try to compile and run it again." +
 								"\nIf the error remains, please report this via email to marco.piccioni{at}inf.ethz.ch";
 					
 					// we've read the result file from within the sandbox, now we need to delete it
 					FileUtils.deleteQuietly(new File(ServerProperties.SANDBOXIE_DRIVE + SEP + pathOfTmpFolder.replaceFirst(":", "")));
 				} catch (IOException e) {
 					LOGGER.log(Level.SEVERE, "Exeception when trying to read the content of: " + e4moocTmpOutputFileName);
 					System.err.println(e.getMessage());
 					e.printStackTrace();
 					result = "There was a problem reading the output generated by your program. Try to compile and run it again." +
 							"\nIf the error remains, please report this via email to ymarco.piccioni{at}inf.ethz.ch";
 				}
 			} else {
 				// we couldn't find an output file thus we return that info to the user
 				result = "There was a problem generating the output of your program. Try to compile and run it again." +
 						"\nIf the error remains, please report this via email to marco.piccioni{at}inf.ethz.ch";
 			}
 		}
 		else if (ServerProperties.SERVER_SYSTEM == ServerSystem.LINUX_SANBOXED) {
 			// if we are running a sandboxed Linux, we use "schroot" for the sandboxing
 			cmdInstruction = getCommandLine("schroot");
 			cmdInstruction.addArgument("-c");
 			cmdInstruction.addArgument("e4mooc");
 			// HACK: we have to provide "schroot" a path that acutally exists for it
 			// but at this point, the W_codePath looks like this "/srv/chroot/e4mooc/tmp/..." but we need "/tmp/..."
 			cmdInstruction.addArgument(W_codePath.substring(18) + ecfFileName);
 			LOGGER.log(Level.INFO, "Executeing usr program using: " + cmdInstruction.toString());
 			
			result = execute(cmdInstruction, 1000 * 180); // timeout after 3 minutes
 		}
 		else {
 			cmdInstruction = getCommandLine(W_codePath + ecfFileName);
 			LOGGER.log(Level.WARNING, "NO SANDBOX! Executeing usr program using: " + cmdInstruction.toString());
			result = execute(cmdInstruction, 1000 * 180); // timeout after 3 minutes			
 		}
 		
 		// the user called this compile method thus we can set/update the time stamp for this project
 		ServerState.getState().setTmpFolderAndTimeStamp(pathOfTmpFolderAndProject);
 		
 		return result;
 	}
 	
 	
 	/**
 	 * Copies a project folder into a tmp folder, overwrites project's Eiffel files with the versions of the user.
 	 * @param tmpFolderPathName the full path of the tmp folder
 	 * @param projectName the name of the project that should be copied
 	 * @param inputFiles the input files from the user
 	 * @return true if everything was 
 	 * @throws IOException is thrown if there was a problem with creating the tmp folder or copying the user files.
 	 */
 	private void copyProjectToFolder(String tmpFolderPathName, String projectName, Map<String, String> inputFiles) throws Exception {
 		
 		// the e4mooc folder should a contain the project folder with the name 'projectName'
 		
 		File sourceFolder = new File(e4mooc + SEP + projectName);
 		File targetFolder = new File(tmpFolderPathName + SEP + projectName);
 		// if the target Folder exists, we delete it first
 		if(targetFolder.exists()) // shouldn't exists but we check anyways
 			FileUtils.deleteQuietly(targetFolder);
 		// now we create a fresh target folder that's empty
 		try {
 			targetFolder.mkdirs();
 		} catch (SecurityException e) {
 			String errMsg = "Error when creating a tmp folder for project: " + projectName;
 			LOGGER.log(Level.SEVERE, errMsg);
 			System.err.println(errMsg);
 			e.printStackTrace();
 			throw e; // re-throw exception so the caller can use the exception to provide feedback to the client
 		}
 		
 		// copy the original project files into the tmp folder
 		if(sourceFolder.exists() && targetFolder.exists()) {
 			try {
 				FileUtils.copyDirectory(sourceFolder, targetFolder);
 			} catch (IOException e) {
 				String errMsg = "Error when copying a project to a tmp folder. Project: " + projectName + "; Tmp folder: " + targetFolder;
 				LOGGER.log(Level.SEVERE, errMsg);
 				System.err.println(errMsg);
 				e.printStackTrace();
 				throw e;
 			}
 			
 			// we need to the relative path of each file we got from the user; this info is part of the ProjectModel
 			ProjectModel pm = ServerState.getState().getProjectModel(projectName);
 			// for each file send by the user we have to overwrite the original one in the project
 			for(String fileName: inputFiles.keySet()) {
 				// generate a file and store it in the project's tmp folder
 				writeInputTextToFile(inputFiles.get(fileName), targetFolder.getAbsolutePath() + SEP + pm.getRelativePathAndFileName(fileName));
 			}
 		}
 	}
 	
 	
 	/**
 	 * Writes a string to a text file.
 	 * @param inputText the input string
 	 * @param fileName the name of the file
 	 * @throws IOException is thrown if the file could not be written
 	 */
 	private void writeInputTextToFile(String inputText, String fileName) throws IOException {
 		
 		try {
 			FileUtils.writeStringToFile(new File(fileName), inputText);
 		} catch (IOException e) {
 			System.err.println("Could not create the user Eiffel file '" + fileName +"' at location '" + fileName + "." );
 			e.printStackTrace();
 			throw e;
 		}
 	}
 
 	/**
 	 * Returns the CommandLine object for a given tool. Handles EXE and JAR executables.
 	 * @param toolExecutable the name of the executable of the tool, plus it's ending.
 	 * @return a CommandLine object
 	 */
 	private CommandLine getCommandLine(String toolExecutable) {
 		CommandLine result = null;
 		//the exe file
 		String executable = toolExecutable;
 		
 		result = new CommandLine(executable);	
 		return result;
 	}
 	
 	
 	/**
 	 * Executes a given command-line instruction in a non-blocking manner. The execution is terminated after a certain time.
 	 * @param cmdInstruction the command-line instruction that should be executed.
 	 * @param timeout the time (in milliseconds) after which the command-line process gets terminated.
 	 * @return the output of the command-line instruction.
 	 */
 	private String execute(CommandLine cmdInstruction, long timeout) {
 		// stream and streamhandler for the ouput
 		ByteArrayOutputStream stdout = new ByteArrayOutputStream();
         PumpStreamHandler psh = new PumpStreamHandler(stdout);
 		
         // unblocking execution, thus we use DefaultExecuteResultHandler		
 		//DefaultExecuteResultHandler resultHandler = new DefaultExecuteResultHandler();	
 		// watchdog to terminate the execution after a certain amount of milliseconds
 		ExecuteWatchdog watchdog = new ExecuteWatchdog(timeout);
 		
 		Executor executor = new DefaultExecutor();
 		executor.setWatchdog(watchdog);
 		executor.setStreamHandler(psh);
 		// we want to start the program form the root directory 
 		executor.setWorkingDirectory(new File("/"));
 		
 		try {

			//executor.execute(cmdInstruction, EnvironmentUtils.getProcEnvironment(), resultHandler);
			//resultHandler.waitFor();
			int errorCode = executor.execute(cmdInstruction);
						
 		} catch (ExecuteException e) {
			System.err.println("ExecutionServiceImpl.execute: execution exception while executing " + cmdInstruction);
			e.printStackTrace();
 		} catch (IOException e) {
 			System.err.println("ExecutionServiceImpl.execute: io exception while executing " + cmdInstruction);
 			e.printStackTrace();
 		}
 		
 		// return whatever is in the stream
 		return stdout.toString();	
 	}
 	
 }
