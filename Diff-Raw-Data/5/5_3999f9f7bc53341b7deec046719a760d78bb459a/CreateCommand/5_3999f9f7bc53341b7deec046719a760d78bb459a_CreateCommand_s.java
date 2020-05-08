 package com.redhat.contentspec.client.commands;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import com.beust.jcommander.JCommander;
 import com.beust.jcommander.Parameter;
 import com.beust.jcommander.Parameters;
 import com.redhat.contentspec.client.config.ContentSpecConfiguration;
 import com.redhat.contentspec.client.constants.Constants;
 import com.redhat.contentspec.client.converter.FileConverter;
 import com.redhat.contentspec.client.utils.ClientUtilities;
 import com.redhat.contentspec.processor.ContentSpecParser;
 import com.redhat.contentspec.processor.ContentSpecProcessor;
 import com.redhat.contentspec.rest.RESTManager;
 import com.redhat.contentspec.rest.RESTReader;
 import com.redhat.contentspec.utils.StringUtilities;
 import com.redhat.contentspec.utils.logging.ErrorLoggerManager;
 import com.redhat.ecs.commonutils.FileUtilities;
 import com.redhat.topicindex.rest.entities.TopicV1;
 import com.redhat.topicindex.rest.entities.UserV1;
 
 @Parameters(commandDescription = "Create a new Content Specification on the server")
 public class CreateCommand extends BaseCommandImpl {
 
 	@Parameter(converter = FileConverter.class, metaVar = "[FILE]")
 	private List<File> files = new ArrayList<File>();
 	
 	@Parameter(names = {Constants.PERMISSIVE_LONG_PARAM, Constants.PERMISSIVE_SHORT_PARAM}, description = "Turn on permissive processing.")
 	private Boolean permissive = false;
 	
 	@Parameter(names = Constants.EXEC_TIME_LONG_PARAM, description = "Show the execution time of the command.", hidden = true)
 	private Boolean executionTime = false;
 	
 	@Parameter(names = Constants.NO_CREATE_CSPROCESSOR_CFG_LONG_PARAM, description = "Don't create the csprocessor.cfg and other files.")
 	private Boolean createCsprocessorCfg = true;
 	
 	@Parameter(names = {Constants.FORCE_LONG_PARAM, Constants.FORCE_SHORT_PARAM}, description = "Force the Content Specification directories to be created.")
 	private Boolean force = false;
 	
 	private ContentSpecProcessor csp = null;
 	
 	public CreateCommand(JCommander parser) {
 		super(parser);
 	}
 
 	public List<File> getFiles() {
 		return files;
 	}
 
 	public void setFiles(List<File> files) {
 		this.files = files;
 	}
 	
 	public Boolean getPermissive() {
 		return permissive;
 	}
 
 	public void setPermissive(Boolean permissive) {
 		this.permissive = permissive;
 	}
 
 	public Boolean getExecutionTime() {
 		return executionTime;
 	}
 
 	public void setExecutionTime(Boolean executionTime) {
 		this.executionTime = executionTime;
 	}
 
 	public Boolean getCreateCsprocessorCfg() {
 		return createCsprocessorCfg;
 	}
 
 	public void setCreateCsprocessorCfg(Boolean createCsprocessorCfg) {
 		this.createCsprocessorCfg = createCsprocessorCfg;
 	}
 
 	public Boolean getForce() {
 		return force;
 	}
 
 	public void setForce(Boolean force) {
 		this.force = force;
 	}
 
 	public boolean isValid() {
 		// We should have only one file
 		if (files.size() != 1) return false;
 		
 		// Check that the file exists
 		File file = files.get(0);
 		if (file.isDirectory()) return false;
 		if (!file.exists()) return false;
 		if (!file.isFile()) return false;
 		
 		return true;
 	}
 	
 	@Override
 	public void process(ContentSpecConfiguration cspConfig, RESTManager restManager, ErrorLoggerManager elm, UserV1 user) {
 		if (!isValid()) {
 			printError(Constants.ERROR_NO_FILE_MSG, true);
 			shutdown(Constants.EXIT_FAILURE);
 		}
 
 		long startTime = System.currentTimeMillis();
 		boolean success = false;
 
 		// Read in the file contents
 		String contentSpec = FileUtilities.readFileContents(files.get(0));
 		
 		if (contentSpec == null  || contentSpec.equals("")) {
 			printError(Constants.ERROR_EMPTY_FILE_MSG, false);
 			shutdown(Constants.EXIT_FAILURE);
 		}
 		
 		// Good point to check for a shutdown
 		if (isAppShuttingDown()) {
 			shutdown.set(true);
 			return;
 		}
 		
 		// Parse the spec to get the title
 		ContentSpecParser parser = new ContentSpecParser(elm, restManager);
 		try {
 			parser.parse(contentSpec);
 		} catch (Exception e) {
 			printError(Constants.ERROR_INTERNAL_ERROR, false);
 			shutdown(Constants.EXIT_INTERNAL_SERVER_ERROR);
 		}
 		
 		// Check that the output directory doesn't already exist
 		File directory = new File(cspConfig.getRootOutputDirectory() + StringUtilities.escapeTitle(parser.getContentSpec().getTitle()));
		if (directory.exists() && !force) {
 			printError(String.format(Constants.ERROR_CONTENT_SPEC_EXISTS_MSG, directory.getAbsolutePath()), false);
 			shutdown(Constants.EXIT_FAILURE);
 		// If it exists and force is enabled delete the directory contents
		} else if (directory.exists()) {
 			ClientUtilities.deleteDir(directory);
 		}
 		
 		// Good point to check for a shutdown
 		if (isAppShuttingDown()) {
 			shutdown.set(true);
 			return;
 		}
 		
 		csp = new ContentSpecProcessor(restManager, elm, permissive);
 		Integer revision = null;
 		try {
 			success = csp.processContentSpec(contentSpec, user, ContentSpecParser.ParsingMode.NEW);
 			if (success) {
 				revision = restManager.getReader().getLatestCSRevById(csp.getContentSpec().getId());
 			}
 		} catch (Exception e) {
 			printError(Constants.ERROR_INTERNAL_ERROR, false);
 			shutdown(Constants.EXIT_INTERNAL_SERVER_ERROR);
 		}
 		
 		// Print the logs
 		long elapsedTime = System.currentTimeMillis() - startTime;
 		JCommander.getConsole().println(elm.generateLogs());
 		if (success) {
 			JCommander.getConsole().println(String.format(Constants.SUCCESSFUL_PUSH_MSG, csp.getContentSpec().getId(), revision));
 		}
 		if (executionTime) {
 			JCommander.getConsole().println(String.format(Constants.EXEC_TIME_MSG, elapsedTime));
 		}
 		
 		// Good point to check for a shutdown
 		// It doesn't matter if the directory and files aren't created just so long as the spec finished saving
 		if (isAppShuttingDown()) {
 			shutdown.set(true);
 			return;
 		}
 		
 		if (success && createCsprocessorCfg) {
 			boolean error = false;
 			
 			// Save the csprocessor.cfg and post spec to file if the create was successful
 			String escapedTitle = StringUtilities.escapeTitle(csp.getContentSpec().getTitle());
 			TopicV1 contentSpecTopic = restManager.getReader().getContentSpecById(csp.getContentSpec().getId(), null);
 			File outputSpec = new File(cspConfig.getRootOutputDirectory() + escapedTitle + File.separator + escapedTitle + "-post." + Constants.FILENAME_EXTENSION);
 			File outputConfig = new File(cspConfig.getRootOutputDirectory() + escapedTitle + File.separator + "csprocessor.cfg");
 			String config = ClientUtilities.generateCsprocessorCfg(contentSpecTopic, cspConfig.getServerUrl());
 			
 			// Create the directory
 			if (outputConfig.getParentFile() != null)
 				outputConfig.getParentFile().mkdirs();
 			
 			// Save the csprocessor.cfg
 			try {
 				FileOutputStream fos = new FileOutputStream(outputConfig);
 				fos.write(config.getBytes());
 				fos.flush();
 				fos.close();
 				JCommander.getConsole().println(String.format(Constants.OUTPUT_SAVED_MSG, outputConfig.getAbsolutePath()));
 			} catch (IOException e) {
 				printError(String.format(Constants.ERROR_FAILED_SAVING_FILE, outputConfig.getAbsolutePath()), false);
 				error = true;
 			}
 			
 			// Save the Post Processed spec
 			try {
 				FileOutputStream fos = new FileOutputStream(outputSpec);
 				fos.write(contentSpecTopic.getXml().getBytes());
 				fos.flush();
 				fos.close();
 				JCommander.getConsole().println(String.format(Constants.OUTPUT_SAVED_MSG, outputSpec.getAbsolutePath()));
 			} catch (IOException e) {
 				printError(String.format(Constants.ERROR_FAILED_SAVING_FILE, outputSpec.getAbsolutePath()), false);
 				error = false;
 			}
 			
 			if (error) {
 				shutdown(Constants.EXIT_FAILURE);
 			}
 		}
 	}
 
 	@Override
 	public void printError(String errorMsg, boolean displayHelp) {
 		printError(errorMsg, displayHelp, Constants.CREATE_COMMAND_NAME);
 	}
 
 	@Override
 	public void printHelp() {
 		printHelp(Constants.CREATE_COMMAND_NAME);
 	}
 
 	@Override
 	public UserV1 authenticate(RESTReader reader) {
 		return authenticate(getUsername(), reader);
 	}
 	
 	@Override
 	public void shutdown() {
 		super.shutdown();
 		if (csp != null) {
 			csp.shutdown();
 		}
 	}
 }
