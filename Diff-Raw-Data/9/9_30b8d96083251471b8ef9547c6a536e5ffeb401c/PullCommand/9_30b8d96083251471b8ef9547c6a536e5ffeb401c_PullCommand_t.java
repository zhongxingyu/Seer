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
 import com.redhat.ecs.commonutils.CollectionUtilities;
 import com.redhat.topicindex.rest.entities.TopicV1;
 import com.redhat.topicindex.rest.entities.UserV1;
 
 @Parameters(commandDescription = "Pull a Content Specification from the server")
 public class PullCommand extends BaseCommandImpl{
 
 	@Parameter(metaVar = "[ID]")
 	private List<Integer> ids = new ArrayList<Integer>();
 	
 	@Parameter(names = {Constants.CONTENT_SPEC_LONG_PARAM, Constants.CONTENT_SPEC_SHORT_PARAM})
 	private Boolean contentSpec = false;
 	
 	@Parameter(names = {Constants.TOPIC_LONG_PARAM, Constants.TOPIC_SHORT_PARAM})
 	private Boolean topic = false;
 	
 	@Parameter(names = {Constants.XML_LONG_PARAM, Constants.XML_SHORT_PARAM})
 	private Boolean useXml = false;
 	
 	@Parameter(names = {Constants.HTML_LONG_PARAM, Constants.HTML_SHORT_PARAM})
 	private Boolean useHtml = false;
 	
 	@Parameter(names = Constants.PRE_LONG_PARAM)
 	private Boolean usePre = false;
 	
 	@Parameter(names = Constants.POST_LONG_PARAM)
 	private Boolean usePost = false;
 	
 	@Parameter(names = {Constants.REVISION_LONG_PARAM, Constants.REVISION_SHORT_PARAM})
 	private Integer revision;
 	
 	@Parameter(names = {Constants.OUTPUT_LONG_PARAM, Constants.OUTPUT_SHORT_PARAM}, description = "Save the output to the specified file/directory.", metaVar = "<FILE>")
 	private String outputPath;
 	
 	public PullCommand(JCommander parser) {
 		super(parser);
 	}
 
 	public Boolean useContentSpec() {
 		return contentSpec;
 	}
 
 	public void setContentSpec(Boolean contentSpec) {
 		this.contentSpec = contentSpec;
 	}
 
 	public Boolean getTopic() {
 		return topic;
 	}
 
 	public void setTopic(Boolean topic) {
 		this.topic = topic;
 	}
 
 	public Boolean isUseXml() {
 		return useXml;
 	}
 
 	public void setUseXml(Boolean useXml) {
 		this.useXml = useXml;
 	}
 
 	public Boolean isUseHtml() {
 		return useHtml;
 	}
 
 	public void setUseHtml(Boolean useHtml) {
 		this.useHtml = useHtml;
 	}
 
 	public Boolean isUsePre() {
 		return usePre;
 	}
 
 	public void setUsePre(Boolean usePre) {
 		this.usePre = usePre;
 	}
 
 	public Boolean isUsePost() {
 		return usePost;
 	}
 
 	public void setUsePost(Boolean usePost) {
 		this.usePost = usePost;
 	}
 
 	public Integer getRevision() {
 		return revision;
 	}
 
 	public void setRevision(Integer revision) {
 		this.revision = revision;
 	}
 
 	public List<Integer> getIds() {
 		return ids;
 	}
 
 	public void setIds(List<Integer> ids) {
 		this.ids = ids;
 	}
 
 	public String getOutputPath() {
 		return outputPath;
 	}
 
 	public void setOutputPath(String outputPath) {
 		this.outputPath = outputPath;
 	}
 	
 	public boolean isValid() {
 		if (topic && !contentSpec) {
 			if (useXml && useHtml) {
 				return false;
 			}
 			if (usePre || usePost) {
 				return false;
 			}
 		} else if (!topic) {
 			if (usePre && usePost) {
 				return false;
 			}
 			if (useXml || useHtml) {
 				return false;
 			}
 		} else {
 			return false;
 		}
 		return true;
 	}
 	
 	@Override
 	public void process(ContentSpecConfiguration cspConfig, RESTManager restManager, ErrorLoggerManager elm, UserV1 user) {
 		RESTReader reader = restManager.getReader();
 		boolean pullForConfig = false;
 		
 		// Load the data from the config data if no ids were specified
 		if (ids.size() == 0 && !topic && cspConfig.getContentSpecId() != null) {
 			setIds(CollectionUtilities.toArrayList(cspConfig.getContentSpecId()));
 			pullForConfig = true;
 			revision = null;
 		}
 		
 		// Check that the options are valid
 		if (!isValid()) {
 			printError(Constants.INVALID_ARG_MSG, true);
 			shutdown(Constants.EXIT_ARGUMENT_ERROR);
 		}
 		
 		// Check that only one ID exists
 		if (ids.size() == 0) {
 			printError(Constants.ERROR_NO_ID_MSG, false);
 			shutdown(Constants.EXIT_ARGUMENT_ERROR);
 		} else if (ids.size() > 1) {
 			printError(Constants.ERROR_MULTIPLE_ID_MSG, false);
 			shutdown(Constants.EXIT_ARGUMENT_ERROR);
 		}
 		
 		// Good point to check for a shutdown
 		if (isAppShuttingDown()) {
 			shutdown.set(true);
 			return;
 		}
 		
 		String data = "";
 		String fileName = "";
 		// Topic
 		if (topic) {
			TopicV1 topic = restManager.getReader().getPostContentSpecById(ids.get(0), null);
 			if (topic == null) {
 				printError(revision == null ? Constants.ERROR_NO_ID_FOUND_MSG : Constants.ERROR_NO_REV_ID_FOUND_MSG, false);
 				shutdown(Constants.EXIT_FAILURE);
 			} else {
 				if (useXml) {
 					data = topic.getXml();
 					fileName = StringUtilities.escapeTitle(topic.getTitle()) + ".xml";
 				} else if (useHtml) {
 					data = topic.getHtml();
 					fileName = StringUtilities.escapeTitle(topic.getTitle()) + ".html";
 				}
 			}
 		// Content Specification
 		} else {
 			if (usePre) {
 				TopicV1 contentSpec = reader.getPreContentSpecById(ids.get(0), revision);
 				if (contentSpec == null) {
 					printError(revision == null ? Constants.ERROR_NO_ID_FOUND_MSG : Constants.ERROR_NO_REV_ID_FOUND_MSG, false);
 					shutdown(Constants.EXIT_FAILURE);
 				} else {
 					data = contentSpec.getXml();
 					fileName = StringUtilities.escapeTitle(contentSpec.getTitle()) + "-pre." + Constants.FILENAME_EXTENSION;
 					if (pullForConfig) {
 						outputPath = (cspConfig.getRootOutputDirectory() == null || cspConfig.getRootOutputDirectory().equals("") ? "" : (cspConfig.getRootOutputDirectory() + StringUtilities.escapeTitle(contentSpec.getTitle() + File.separator)));
 					}
 				}
 			} else {
 				TopicV1 contentSpec = reader.getPostContentSpecById(ids.get(0), revision);
 				if (contentSpec == null) {
 					printError(revision == null ? Constants.ERROR_NO_ID_FOUND_MSG : Constants.ERROR_NO_REV_ID_FOUND_MSG, false);
 					shutdown(Constants.EXIT_FAILURE);
 				} else {
 					data = contentSpec.getXml();
 					fileName = StringUtilities.escapeTitle(contentSpec.getTitle()) + "-post." + Constants.FILENAME_EXTENSION;
 					if (pullForConfig) {
 						outputPath = (cspConfig.getRootOutputDirectory() == null || cspConfig.getRootOutputDirectory().equals("") ? "" : (cspConfig.getRootOutputDirectory() + StringUtilities.escapeTitle(contentSpec.getTitle() + File.separator)));
 					}
 				}
 			}
 		}
 		
 		// Good point to check for a shutdown
 		if (isAppShuttingDown()) {
 			shutdown.set(true);
 			return;
 		}
 		
 		// Save or print the data
 		if (outputPath == null) {
 			JCommander.getConsole().println(data);
 		} else {
 			// Create the output file
 			File output;
 			outputPath = ClientUtilities.validateFilePath(outputPath);
 			if (outputPath != null && outputPath.endsWith(File.separator)) {
 				output = new File(outputPath + fileName);
 			} else if (outputPath == null || outputPath.equals("")) {
 				output = new File(fileName);
 			} else {
 				output = new File(outputPath);
 			}
 			
 			// Make sure the directories exist
 			if (output.isDirectory()) {
 				output.mkdirs();
 				output = new File(output.getAbsolutePath() + File.separator + fileName);
 			} else {
 				if (output.getParentFile() != null)
 					output.getParentFile().mkdirs();
 			}
 			
 			// Good point to check for a shutdown
 			if (isAppShuttingDown()) {
 				shutdown.set(true);
 				return;
 			}
 			
 			// If the file exists then create a backup file
 			if (output.exists()) {
 				output.renameTo(new File(output.getAbsolutePath() + ".backup"));
 			}
 			
 			// Create and write to the file
 			try {
 				FileOutputStream fos = new FileOutputStream(output);
 				fos.write(data.getBytes());
 				fos.flush();
 				fos.close();
 				JCommander.getConsole().println(String.format(Constants.OUTPUT_SAVED_MSG, output.getName()));
 			} catch (IOException e) {
 				printError(Constants.ERROR_FAILED_SAVING, false);
 				shutdown(Constants.EXIT_FAILURE);
 			}
 		}
 	}
 	
 	@Override
 	public UserV1 authenticate(RESTReader reader) {
 		return null;
 	}
 
 	@Override
 	public void printError(String errorMsg, boolean displayHelp) {
 		printError(errorMsg, displayHelp, Constants.PULL_COMMAND_NAME);
 	}
 
 	@Override
 	public void printHelp() {
 		printHelp(Constants.PULL_COMMAND_NAME);
 	}
 }
