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
 import com.redhat.contentspec.client.utils.ClientUtilities;
 import com.redhat.contentspec.rest.RESTManager;
 import com.redhat.contentspec.rest.RESTReader;
 import com.redhat.contentspec.utils.StringUtilities;
 import com.redhat.contentspec.utils.logging.ErrorLoggerManager;
 import com.redhat.topicindex.rest.entities.TopicV1;
 import com.redhat.topicindex.rest.entities.UserV1;
 
 @Parameters(commandDescription = "Checkout an existing Content Specification from the server")
 public class CheckoutCommand extends BaseCommandImpl {
 	
 	@Parameter(metaVar = "[ID]")
 	private List<Integer> ids = new ArrayList<Integer>();
 	
 	@Parameter(names = {Constants.FORCE_LONG_PARAM, Constants.FORCE_SHORT_PARAM}, description = "Force the Content Specification directories to be created.")
 	private Boolean force = false;
 
 	public CheckoutCommand(JCommander parser) {
 		super(parser);
 	}
 
 	public List<Integer> getIds() {
 		return ids;
 	}
 
 	public void setIds(List<Integer> ids) {
 		this.ids = ids;
 	}
 	
 	public Boolean getForce() {
 		return force;
 	}
 
 	public void setForce(Boolean force) {
 		this.force = force;
 	}
 
 	@Override
 	public void printHelp() {
 		printHelp(Constants.CHECKOUT_COMMAND_NAME);
 	}
 
 	@Override
 	public void printError(String errorMsg, boolean displayHelp) {
 		printError(errorMsg, displayHelp, Constants.CHECKOUT_COMMAND_NAME);
 	}
 
 	@Override
 	public UserV1 authenticate(RESTReader reader) {
 		return null;
 	}
 
 	@Override
 	public void process(ContentSpecConfiguration cspConfig, RESTManager restManager, ErrorLoggerManager elm, UserV1 user) {
 		// Check that an ID was entered
 		if (ids.size() == 0) {
 			printError(Constants.ERROR_NO_ID_MSG, false);
 			shutdown(Constants.EXIT_ARGUMENT_ERROR);
 		} else if (ids.size() > 1) {
 			printError(Constants.ERROR_MULTIPLE_ID_MSG, false);
 			shutdown(Constants.EXIT_ARGUMENT_ERROR);
 		}
 		
 		// Get the content spec from the server
		TopicV1 contentSpec = restManager.getReader().getContentSpecById(ids.get(0), null);
 		if (contentSpec == null || contentSpec.getXml() == null) {
 			printError(Constants.ERROR_NO_ID_FOUND_MSG, false);
 			shutdown(Constants.EXIT_FAILURE);
 		}
 			
 		// Check that the output directory doesn't already exist
 		File directory = new File(cspConfig.getRootOutputDirectory() + StringUtilities.escapeTitle(contentSpec.getTitle()));
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
 		
 		// Save the csprocessor.cfg and post spec to file if the create was successful
 		String escapedTitle = StringUtilities.escapeTitle(contentSpec.getTitle());
 		TopicV1 contentSpecTopic = restManager.getReader().getContentSpecById(contentSpec.getId(), null);
 		File outputSpec = new File(cspConfig.getRootOutputDirectory() + escapedTitle + File.separator + escapedTitle + "-post." + Constants.FILENAME_EXTENSION);
 		File outputConfig = new File(cspConfig.getRootOutputDirectory() + escapedTitle + File.separator + "csprocessor.cfg");
 		String config = ClientUtilities.generateCsprocessorCfg(contentSpecTopic, cspConfig.getServerUrl());
 		
 		// Create the directory
 		if (outputConfig.getParentFile() != null)
 			outputConfig.getParentFile().mkdirs();
 		
 		boolean error = false;
 		
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
