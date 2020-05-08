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
 import com.redhat.contentspec.processor.ContentSpecParser;
 import com.redhat.contentspec.processor.ContentSpecProcessor;
 import com.redhat.contentspec.processor.structures.ProcessingOptions;
 import com.redhat.contentspec.rest.RESTManager;
 import com.redhat.contentspec.rest.RESTReader;
 import com.redhat.contentspec.utils.logging.ErrorLoggerManager;
 import com.redhat.ecs.commonutils.DocBookUtilities;
 import com.redhat.ecs.commonutils.FileUtilities;
 import com.redhat.topicindex.rest.entities.interfaces.RESTUserV1;
 import com.redhat.topicindex.rest.entities.interfaces.RESTTopicV1;
 
 @Parameters(commandDescription = "Push an updated Content Specification to the server")
 public class PushCommand extends BaseCommandImpl {
 
 	@Parameter(converter = FileConverter.class, metaVar = "[FILE]")
 	private List<File> files = new ArrayList<File>();
 	
 	@Parameter(names = {Constants.PERMISSIVE_LONG_PARAM, Constants.PERMISSIVE_SHORT_PARAM}, description = "Turn on permissive processing.")
 	private Boolean permissive = false;
 	
 	@Parameter(names = Constants.EXEC_TIME_LONG_PARAM, description = "Show the execution time of the command.", hidden = true)
 	private Boolean executionTime = false;
 	
 	private ContentSpecProcessor csp = null;
 	
 	public PushCommand(final JCommander parser, final ContentSpecConfiguration cspConfig) {
 		super(parser, cspConfig);
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
 
 	@Override
 	public void printError(String errorMsg, boolean displayHelp) {
 		printError(errorMsg, displayHelp, Constants.PUSH_COMMAND_NAME);
 	}
 
 	@Override
 	public void printHelp() {
 		printHelp(Constants.PUSH_COMMAND_NAME);
 	}
 	
 	@Override
 	public RESTUserV1 authenticate(RESTReader reader) {
 		return authenticate(getUsername(), reader);
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
 	public void process(final RESTManager restManager, final ErrorLoggerManager elm, final RESTUserV1 user)
 	{
 		boolean pushingFromConfig = false;
 		// If files is empty then we must be using a csprocessor.cfg file
 		if (loadFromCSProcessorCfg()) {
 			final RESTTopicV1 contentSpec = restManager.getReader().getContentSpecById(cspConfig.getContentSpecId(), null);
 			String fileName = DocBookUtilities.escapeTitle(contentSpec.getTitle()) + "-post." + Constants.FILENAME_EXTENSION;
 			File file = new File(fileName);
 			if (!file.exists()) {
 				// Backwards compatibility check for files ending with .txt
 				file = new File(DocBookUtilities.escapeTitle(contentSpec.getTitle()) + "-post.txt");
 				if (!file.exists()) {
 					printError(String.format(Constants.NO_FILE_FOUND_FOR_CONFIG, fileName), false);
 					shutdown(Constants.EXIT_FAILURE);
 				}
 			}
 			files.add(file);
 			pushingFromConfig = true;
 		}
 		
 		// Check that the parameters are valid
 		if (!isValid()) {
 			printError(Constants.ERROR_NO_FILE_MSG, true);
 			shutdown(Constants.EXIT_FAILURE);
 		}
 		
 		// Good point to check for a shutdown (before starting)
 		if (isAppShuttingDown()) {
 			shutdown.set(true);
 			return;
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
 		
 		// Setup the processing options
 		final ProcessingOptions processingOptions = new ProcessingOptions();
 		processingOptions.setPermissiveMode(permissive);
 		processingOptions.setAllowEmptyLevels(true);
 		
 		csp = new ContentSpecProcessor(restManager, elm, processingOptions);
 		Integer revision = null;
 		try {
 			success = csp.processContentSpec(contentSpec, user, ContentSpecParser.ParsingMode.EDITED);
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
 		// Doesn't matter if the latest copy doesn't get downloaded just so long as the push goes through
 		if (isAppShuttingDown()) {
 			shutdown.set(true);
 			return;
 		}
 		
 		if (success && pushingFromConfig) {
 			// Save the post spec to file if the push was successful
 			final String escapedTitle = DocBookUtilities.escapeTitle(csp.getContentSpec().getTitle());
 			final RESTTopicV1 contentSpecTopic = restManager.getReader().getContentSpecById(csp.getContentSpec().getId(), null);
			File outputSpec = new File((cspConfig.getRootOutputDirectory() == null || cspConfig.getRootOutputDirectory().equals("") ? "" : (cspConfig.getRootOutputDirectory() + escapedTitle + File.separator)) + escapedTitle + "-post." + Constants.FILENAME_EXTENSION);
 			
 			// Create the directory
 			if (outputSpec.getParentFile() != null)
 				outputSpec.getParentFile().mkdirs();
 			
 			// Save the Post Processed spec
 			try {
				final FileOutputStream fos = new FileOutputStream(outputSpec);
 				fos.write(contentSpecTopic.getXml().getBytes());
 				fos.flush();
 				fos.close();
 			} catch (IOException e) {
 				printError(String.format(Constants.ERROR_FAILED_SAVING_FILE, outputSpec.getAbsolutePath()), false);
 				shutdown(Constants.EXIT_FAILURE);
 			}
 		}
 	}
 	
 	@Override
 	public void shutdown() {
 		super.shutdown();
 		if (csp != null) {
 			csp.shutdown();
 		}
 	}
 
 	@Override
 	public boolean loadFromCSProcessorCfg() {
 		return files.size() == 0 && cspConfig != null && cspConfig.getContentSpecId() != null;
 	}
 }
