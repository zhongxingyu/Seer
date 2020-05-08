 package com.redhat.contentspec.client.commands;
 
 import java.io.File;
 import java.io.IOException;
 
 import com.beust.jcommander.JCommander;
 import com.beust.jcommander.Parameter;
 import com.beust.jcommander.Parameters;
 import com.redhat.contentspec.client.config.ContentSpecConfiguration;
 import com.redhat.contentspec.client.constants.Constants;
 import com.redhat.contentspec.client.utils.ClientUtilities;
 import com.redhat.contentspec.client.utils.ZipUtilities;
 import com.redhat.contentspec.rest.RESTManager;
 import com.redhat.contentspec.rest.RESTReader;
 import com.redhat.contentspec.utils.StringUtilities;
 import com.redhat.contentspec.utils.logging.ErrorLoggerManager;
 import com.redhat.topicindex.rest.entities.TopicV1;
 import com.redhat.topicindex.rest.entities.UserV1;
 
 @Parameters(commandDescription = "Builds and Assembles a Content Specification so that it is ready to be previewed")
 public class AssembleCommand extends BuildCommand {
 	
 	@Parameter(names = Constants.NO_BUILD_LONG_PARAM, description = "Don't build the content specification.")
 	private Boolean noBuild = false;
 
 	@Parameter(names = Constants.HIDE_OUTPUT_LONG_PARAM, description = "Hide the output from assembling the Content Specification.")
 	private Boolean hideOutput = false;
 	
 	protected String publicanBuildOptions;
 	
 	public AssembleCommand(JCommander parser) {
 		super(parser);
 	}
 	
 	public Boolean getNoBuild() {
 		return noBuild;
 	}
 
 	public void setNoBuild(Boolean noBuild) {
 		this.noBuild = noBuild;
 	}
 
 	public Boolean getHideOutput() {
 		return hideOutput;
 	}
 
 	public void setHideOutput(Boolean hideOutput) {
 		this.hideOutput = hideOutput;
 	}
 
 	@Override
 	public void process(ContentSpecConfiguration cspConfig, RESTManager restManager, ErrorLoggerManager elm, UserV1 user) {
 		boolean assembleFromConfig = getIds() != null && getIds().isEmpty() && cspConfig.getContentSpecId() != null;
 		
 		// Good point to check for a shutdown
 		if (isAppShuttingDown()) {
 			shutdown.set(true);
 			return;
 		}
 		
 		if (!noBuild) {
 			super.process(cspConfig, restManager, elm, user);
 			if (isShutdown()) return;
 		}
 		
 		JCommander.getConsole().println(Constants.STARTING_ASSEMBLE_MSG);
 		
 		TopicV1 contentSpec = null;
 		String fileDirectory = "";
 		String outputDirectory = "";
 		String fileName = null;
 		if (assembleFromConfig) {
 			contentSpec = restManager.getReader().getContentSpecById(cspConfig.getContentSpecId(), null);
 			
 			// Check that that content specification was found
 			if (contentSpec == null || contentSpec.getXml() == null) {
 				printError(Constants.ERROR_NO_ID_FOUND_MSG, false);
 				shutdown(Constants.EXIT_FAILURE);
 			}
 			
			String rootDir = (cspConfig.getRootOutputDirectory() == null || cspConfig.getRootOutputDirectory().equals("") ? "" : (cspConfig.getRootOutputDirectory() + StringUtilities.escapeTitle(contentSpec.getTitle() + File.separator)));
 			
 			fileDirectory = rootDir + Constants.DEFAULT_CONFIG_ZIP_LOCATION;
 			outputDirectory = rootDir + Constants.DEFAULT_CONFIG_PUBLICAN_LOCATION;
 			fileName = StringUtilities.escapeTitle(contentSpec.getTitle()) + "-publican.zip";
 		} else if (getIds() != null && getIds().size() == 1) {
 			contentSpec = restManager.getReader().getContentSpecById(getIds().get(0), null);
 			
 			// Check that that content specification was found
 			if (contentSpec == null || contentSpec.getXml() == null) {
 				printError(Constants.ERROR_NO_ID_FOUND_MSG, false);
 				shutdown(Constants.EXIT_FAILURE);
 			}
 			
 			outputDirectory = StringUtilities.escapeTitle(contentSpec.getTitle());
 			fileName = StringUtilities.escapeTitle(contentSpec.getTitle()) + ".zip";
 		} else if (getIds().size() == 0){
 			printError(Constants.ERROR_NO_ID_MSG, false);
 			shutdown(Constants.EXIT_ARGUMENT_ERROR);
 		} else {
 			printError(Constants.ERROR_MULTIPLE_ID_MSG, false);
 			shutdown(Constants.EXIT_ARGUMENT_ERROR);
 		}
 		
 		// Good point to check for a shutdown
 		if (isAppShuttingDown()) {
 			shutdown.set(true);
 			return;
 		}
 		
 		File file = new File(fileDirectory + fileName);
 		if (!file.exists()) {
 			printError(String.format(Constants.ERROR_UNABLE_TO_FIND_ZIP_MSG, fileName), false);
 			shutdown(Constants.EXIT_FAILURE);
 		}
 		
 		// Make sure the output directories exist
 		File outputDir = new File(outputDirectory);
 		outputDir.mkdirs();
 		
 		// Ensure that the directory is empty
 		ClientUtilities.deleteDirContents(outputDir);
 		
 		// Unzip the file
 		if (!ZipUtilities.unzipFileIntoDirectory(file, outputDirectory)) {
 			printError(Constants.ERROR_FAILED_TO_ASSEMBLE_MSG, false);
 			shutdown(Constants.EXIT_FAILURE);
 		} else {
 			JCommander.getConsole().println(String.format(Constants.SUCCESSFUL_UNZIP_MSG, outputDir.getAbsolutePath()));
 		}
 		
 		// Good point to check for a shutdown
 		if (isAppShuttingDown()) {
 			shutdown.set(true);
 			return;
 		}
 		
 		try {
 			JCommander.getConsole().println(Constants.STARTING_PUBLICAN_BUILD_MSG);
 			Integer exitValue = ClientUtilities.runCommand("publican build " + publicanBuildOptions, outputDir, JCommander.getConsole(), !hideOutput);
 			if (exitValue == null || exitValue != 0) {
 				shutdown(Constants.EXIT_FAILURE);
 			}
 		} catch (IOException e) {
 			printError(Constants.ERROR_RUNNING_PUBLICAN_MSG, false);
 			shutdown(Constants.EXIT_FAILURE);
 		}
 		JCommander.getConsole().println(String.format(Constants.SUCCESSFUL_ASSEMBLE_MSG, outputDir.getAbsolutePath()));
 	}
 	
 	@Override
 	public void printError(String errorMsg, boolean displayHelp) {
 		printError(errorMsg, displayHelp, Constants.ASSEMBLE_COMMAND_NAME);
 	}
 	
 	@Override
 	public void printHelp() {
 		printHelp(Constants.ASSEMBLE_COMMAND_NAME);
 	}
 	
 	@Override
 	public UserV1 authenticate(RESTReader reader) {
 		return authenticate(getUsername(), reader);
 	}
 
 	public String getPublicanBuildOptions() {
 		return publicanBuildOptions;
 	}
 
 	public void setPublicanBuildOptions(String publicanBuildOptions) {
 		this.publicanBuildOptions = publicanBuildOptions;
 	}
 }
