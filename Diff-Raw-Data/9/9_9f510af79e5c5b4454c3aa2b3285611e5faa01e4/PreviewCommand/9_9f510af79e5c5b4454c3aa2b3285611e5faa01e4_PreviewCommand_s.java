 package com.redhat.contentspec.client.commands;
 
 import java.io.File;
 
 import com.beust.jcommander.JCommander;
 import com.beust.jcommander.Parameter;
 import com.beust.jcommander.Parameters;
 import com.redhat.contentspec.client.config.ContentSpecConfiguration;
 import com.redhat.contentspec.client.constants.Constants;
 import com.redhat.contentspec.client.utils.ClientUtilities;
 import com.redhat.contentspec.processor.ContentSpecParser;
 import com.redhat.contentspec.rest.RESTManager;
 import com.redhat.contentspec.rest.RESTReader;
 import com.redhat.contentspec.utils.logging.ErrorLoggerManager;
 import com.redhat.ecs.commonutils.DocBookUtilities;
 import com.redhat.topicindex.rest.entities.TopicV1;
 import com.redhat.topicindex.rest.entities.UserV1;
 
 @Parameters(commandDescription = "Build, Assemble and then open the preview of the Content Specification")
 public class PreviewCommand extends AssembleCommand {
 	
 	@Parameter(names = Constants.NO_ASSEMBLE_LONG_PARAM, description = "Don't assemble the Content Specification.")
 	private Boolean noAssemble = false;
 	
 	protected String previewFormat = Constants.DEFAULT_PUBLICAN_FORMAT;
 	
 	public PreviewCommand(final JCommander parser, final ContentSpecConfiguration cspConfig) {
 		super(parser, cspConfig);
 	}
 	
 	public Boolean getNoAssemble() {
 		return noAssemble;
 	}
 
 	public void setNoAssemble(Boolean noAssemble) {
 		this.noAssemble = noAssemble;
 	}
 	
 	public String getPreviewFormat() {
 		return previewFormat;
 	}
 	
 	public void setPreviewFormat(String previewFormat) {
 		this.previewFormat = previewFormat;
 	}
 	
 	private boolean validateFormat() {
 		if (previewFormat.equals("html") || previewFormat.equals("html-single") || previewFormat.equals("pdf"))
 			return true;
 		else
 			return false;
 	}
 
 	@Override
 	public void process(final RESTManager restManager, final ErrorLoggerManager elm, final UserV1 user)
 	{
 		final RESTReader reader = restManager.getReader();
 		final boolean previewFromConfig = loadFromCSProcessorCfg();
 		
 		// Check that the format can be previewed
 		if (!validateFormat()) {
 			printError(String.format(Constants.ERROR_UNSUPPORTED_FORMAT, previewFormat), false);
 			shutdown(Constants.EXIT_FAILURE);
 		}
 		
 		// Good point to check for a shutdown
 		if (isAppShuttingDown()) {
 			shutdown.set(true);
 			return;
 		}
 		
 		if (!noAssemble) {
 			// Assemble the content specification
 			super.process(restManager, elm, user);
 			if (isShutdown()) return;
 		}
 		
 		// Create the file object that will be opened
 		String previewFileName = null;
 		if (previewFromConfig) {
 			final TopicV1 contentSpec = restManager.getReader().getContentSpecById(cspConfig.getContentSpecId(), null);
 			
 			// Check that that content specification was found
 			if (contentSpec == null || contentSpec.getXml() == null) {
 				printError(Constants.ERROR_NO_ID_FOUND_MSG, false);
 				shutdown(Constants.EXIT_FAILURE);
 			}
 			
 			String rootDir = (cspConfig.getRootOutputDirectory() == null || cspConfig.getRootOutputDirectory().equals("") ? "" : (cspConfig.getRootOutputDirectory() + DocBookUtilities.escapeTitle(contentSpec.getTitle()) + File.separator));
 			
 			if (previewFormat.equals("pdf")) {				
 				// Parse the content specification to get the product and versions
 				ContentSpecParser csp = new ContentSpecParser(elm, restManager);
 				try {
 					csp.parse(contentSpec.getXml());
 				} catch (Exception e) {
 					printError(Constants.ERROR_INTERNAL_ERROR, false);
 					shutdown(Constants.EXIT_ARGUMENT_ERROR);
 				}
 				
 				// Create the file
 				previewFileName = rootDir + Constants.DEFAULT_CONFIG_PUBLICAN_LOCATION + "tmp/en-US/" + previewFormat + "/" + DocBookUtilities.escapeTitle(csp.getContentSpec().getProduct()) + "-" + csp.getContentSpec().getVersion() + "-" + DocBookUtilities.escapeTitle(contentSpec.getTitle()) + "-en-US.pdf";
 			} else {
 				previewFileName = rootDir + Constants.DEFAULT_CONFIG_PUBLICAN_LOCATION + "tmp/en-US/" + previewFormat + "/index.html";
 			}
 		} else if (getIds() != null && getIds().size() == 1) {
 			// Create the file based on an ID passed from the command line
 			final String contentSpec = this.getContentSpecString(reader, getIds().get(0));
 			
 			final ContentSpecParser csp = new ContentSpecParser(elm, restManager);
 			try {
 				csp.parse(contentSpec);
 			} catch (Exception e) {
 				printError(Constants.ERROR_INTERNAL_ERROR, false);
 				shutdown(Constants.EXIT_INTERNAL_SERVER_ERROR);
 			}
 			
 			if (previewFormat.equals("pdf")) {
 				previewFileName = DocBookUtilities.escapeTitle(csp.getContentSpec().getTitle()) + "/tmp/en-US/" + previewFormat + "/" + DocBookUtilities.escapeTitle(csp.getContentSpec().getProduct()) + "-" + csp.getContentSpec().getVersion() + "-" + DocBookUtilities.escapeTitle(csp.getContentSpec().getTitle()) + "-en-US.pdf";
 			} else {
 				previewFileName = DocBookUtilities.escapeTitle(csp.getContentSpec().getTitle()) + "/tmp/en-US/" + previewFormat + "/index.html";
 			}
 		} else if (getIds().size() == 0) {
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
 		
 		File previewFile = new File(previewFileName);
 		
 		// Check that the file exists
 		if (!previewFile.exists()) {
 			printError(String.format(Constants.ERROR_UNABLE_TO_FIND_HTML_SINGLE_MSG, previewFile.getAbsolutePath()), false);
 			shutdown(Constants.EXIT_FAILURE);
 		}
 		
 		// Good point to check for a shutdown
 		if (isAppShuttingDown()) {
 			shutdown.set(true);
 			return;
 		}
 		
 		// Open the file
 		try {
 			ClientUtilities.openFile(previewFile);
 		} catch (Exception e) {
 			printError(String.format(Constants.ERROR_UNABLE_TO_OPEN_FILE_MSG, previewFile.getAbsolutePath()), false);
 			shutdown(Constants.EXIT_FAILURE);
 		}
 	}
 	
 	@Override
 	public void printError(String errorMsg, boolean displayHelp) {
 		printError(errorMsg, displayHelp, Constants.PREVIEW_COMMAND_NAME);
 	}
 	
 	@Override
 	public void printHelp() {
 		printHelp(Constants.PREVIEW_COMMAND_NAME);
 	}
 	
 	@Override
 	public UserV1 authenticate(RESTReader reader) {
		return authenticate(getUsername(), reader);
 	}
 }
