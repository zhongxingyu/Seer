 package com.redhat.contentspec.client.commands;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.jboss.pressgangccms.contentspec.rest.RESTManager;
 import org.jboss.pressgangccms.contentspec.rest.RESTReader;
 import org.jboss.pressgangccms.contentspec.utils.logging.ErrorLoggerManager;
 import org.jboss.pressgangccms.rest.v1.entities.RESTTopicV1;
 import org.jboss.pressgangccms.rest.v1.entities.RESTUserV1;
 import org.jboss.pressgangccms.utils.common.CollectionUtilities;
 import org.jboss.pressgangccms.utils.common.DocBookUtilities;
 
 import com.beust.jcommander.JCommander;
 import com.beust.jcommander.Parameter;
 import com.beust.jcommander.Parameters;
 import com.redhat.contentspec.client.commands.base.BaseCommandImpl;
 import com.redhat.contentspec.client.config.ClientConfiguration;
 import com.redhat.contentspec.client.config.ContentSpecConfiguration;
 import com.redhat.contentspec.client.constants.Constants;
 import com.redhat.contentspec.client.utils.ClientUtilities;
 
 @Parameters(commandDescription = "Pull a Content Specification from the server")
 public class PullCommand extends BaseCommandImpl
 {
 	@Parameter(metaVar = "[ID]")
 	private List<Integer> ids = new ArrayList<Integer>();
 	
 	@Parameter(names = {Constants.CONTENT_SPEC_LONG_PARAM, Constants.CONTENT_SPEC_SHORT_PARAM})
 	private Boolean pullContentSpec = false;
 	
 	@Parameter(names = {Constants.TOPIC_LONG_PARAM, Constants.TOPIC_SHORT_PARAM})
 	private Boolean pullTopic = false;
 	
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
 	
 	public PullCommand(final JCommander parser, final ContentSpecConfiguration cspConfig, final ClientConfiguration clientConfig)
 	{
 		super(parser, cspConfig, clientConfig);
 	}
 
 	public Boolean useContentSpec()
 	{
 		return pullContentSpec;
 	}
 
 	public void setContentSpec(final Boolean contentSpec)
 	{
 		this.pullContentSpec = contentSpec;
 	}
 
 	public Boolean useTopic()
 	{
 		return pullTopic;
 	}
 
 	public void setTopic(final Boolean topic)
 	{
 		this.pullTopic = topic;
 	}
 
 	public Boolean isUseXml()
 	{
 		return useXml;
 	}
 
 	public void setUseXml(final Boolean useXml)
 	{
 		this.useXml = useXml;
 	}
 
 	public Boolean isUseHtml()
 	{
 		return useHtml;
 	}
 
 	public void setUseHtml(final Boolean useHtml)
 	{
 		this.useHtml = useHtml;
 	}
 
 	public Boolean isUsePre()
 	{
 		return usePre;
 	}
 
 	public void setUsePre(final Boolean usePre)
 	{
 		this.usePre = usePre;
 	}
 
 	public Boolean isUsePost()
 	{
 		return usePost;
 	}
 
 	public void setUsePost(final Boolean usePost)
 	{
 		this.usePost = usePost;
 	}
 
 	public Integer getRevision()
 	{
 		return revision;
 	}
 
 	public void setRevision(final Integer revision)
 	{
 		this.revision = revision;
 	}
 
 	public List<Integer> getIds() {
 		return ids;
 	}
 
 	public void setIds(final List<Integer> ids)
 	{
 		this.ids = ids;
 	}
 
 	public String getOutputPath()
 	{
 		return outputPath;
 	}
 
 	public void setOutputPath(final String outputPath)
 	{
 		this.outputPath = outputPath;
 	}
 	
 	public boolean isValid()
 	{
 		if (pullTopic && !pullContentSpec)
 		{
 			if (useXml && useHtml)
 			{
 				return false;
 			}
 			if (usePre || usePost)
 			{
 				return false;
 			}
 		}
 		else if (!pullTopic)
 		{
 			if (usePre && usePost)
 			{
 				return false;
 			}
 			if (useXml || useHtml)
 			{
 				return false;
 			}
 		}
 		else
 		{
 			return false;
 		}
 		return true;
 	}
 	
 	@Override
 	public void process(final RESTManager restManager, final ErrorLoggerManager elm, final RESTUserV1 user)
 	{
 		final RESTReader reader = restManager.getReader();
 		boolean pullForConfig = false;
 		
 		// Load the data from the config data if no ids were specified
 		if (loadFromCSProcessorCfg())
 		{
 			// Check that the config details are valid
 			if (cspConfig != null && cspConfig.getContentSpecId() != null)
 			{
 				setIds(CollectionUtilities.toArrayList(cspConfig.getContentSpecId()));
 			}
 			pullForConfig = true;
 			revision = null;
 		}
 		
 		// Check that the options are valid
 		if (!isValid())
 		{
 			printError(Constants.INVALID_ARG_MSG, true);
 			shutdown(Constants.EXIT_ARGUMENT_ERROR);
 		}
 		
 		// Check that only one ID exists
 		if (ids.size() == 0)
 		{
 			printError(Constants.ERROR_NO_ID_MSG, false);
 			shutdown(Constants.EXIT_ARGUMENT_ERROR);
 		}
 		else if (ids.size() > 1)
 		{
 			printError(Constants.ERROR_MULTIPLE_ID_MSG, false);
 			shutdown(Constants.EXIT_ARGUMENT_ERROR);
 		}
 		
 		// Good point to check for a shutdown
 		if (isAppShuttingDown())
 		{
 			shutdown.set(true);
 			return;
 		}
 		
 		String data = "";
 		String fileName = "";
 		// Topic
 		if (pullTopic)
 		{
			final RESTTopicV1 topic = restManager.getReader().getTopicById(ids.get(0), null);
 			if (topic == null)
 			{
 				printError(revision == null ? Constants.ERROR_NO_ID_FOUND_MSG : Constants.ERROR_NO_REV_ID_FOUND_MSG, false);
 				shutdown(Constants.EXIT_FAILURE);
 			}
 			else
 			{
 				if (useXml)
 				{
 					data = topic.getXml();
 					fileName = DocBookUtilities.escapeTitle(topic.getTitle()) + ".xml";
 				}
 				else if (useHtml)
 				{
 					data = topic.getHtml();
 					fileName = DocBookUtilities.escapeTitle(topic.getTitle()) + ".html";
 				}
 			}
 		// Content Specification
 		}
 		else
 		{
 			if (usePre)
 			{
 				final RESTTopicV1 contentSpec = reader.getPreContentSpecById(ids.get(0), revision);
 				if (contentSpec == null)
 				{
 					printError(revision == null ? Constants.ERROR_NO_ID_FOUND_MSG : Constants.ERROR_NO_REV_ID_FOUND_MSG, false);
 					shutdown(Constants.EXIT_FAILURE);
 				}
 				else
 				{
 					data = contentSpec.getXml();
 					if (revision == null)
 					{
 						fileName = DocBookUtilities.escapeTitle(contentSpec.getTitle()) + "-pre." + Constants.FILENAME_EXTENSION;
 					}
 					else
 					{
 						fileName = DocBookUtilities.escapeTitle(contentSpec.getTitle()) + "-pre-r" + revision + "." + Constants.FILENAME_EXTENSION;
 					}
 					if (pullForConfig)
 					{
 						outputPath = (cspConfig.getRootOutputDirectory() == null || cspConfig.getRootOutputDirectory().equals("") ? "" : (cspConfig.getRootOutputDirectory() + DocBookUtilities.escapeTitle(contentSpec.getTitle() + File.separator)));
 					}
 				}
 			}
 			else
 			{
 				final RESTTopicV1 contentSpec = reader.getPostContentSpecById(ids.get(0), revision);
 				if (contentSpec == null)
 				{
 					printError(revision == null ? Constants.ERROR_NO_ID_FOUND_MSG : Constants.ERROR_NO_REV_ID_FOUND_MSG, false);
 					shutdown(Constants.EXIT_FAILURE);
 				}
 				else
 				{
 					data = contentSpec.getXml();
 					if (revision == null)
 					{
 						fileName = DocBookUtilities.escapeTitle(contentSpec.getTitle()) + "-post." + Constants.FILENAME_EXTENSION;
 					}
 					else
 					{
 						fileName = DocBookUtilities.escapeTitle(contentSpec.getTitle()) + "-post-r" + revision + "." + Constants.FILENAME_EXTENSION;
 					}
 					if (pullForConfig)
 					{
 						outputPath = (cspConfig.getRootOutputDirectory() == null || cspConfig.getRootOutputDirectory().equals("") ? "" : (cspConfig.getRootOutputDirectory() + DocBookUtilities.escapeTitle(contentSpec.getTitle() + File.separator)));
 					}
 				}
 			}
 		}
 		
 		// Good point to check for a shutdown
 		if (isAppShuttingDown())
 		{
 			shutdown.set(true);
 			return;
 		}
 		
 		// Save or print the data
 		if (outputPath == null)
 		{
 			JCommander.getConsole().println(data);
 		}
 		else
 		{
 			// Create the output file
 			File output;
 			outputPath = ClientUtilities.validateFilePath(outputPath);
 			if (outputPath != null && outputPath.endsWith(File.separator))
 			{
 				output = new File(outputPath + fileName);
 			}
 			else if (outputPath == null || outputPath.equals(""))
 			{
 				output = new File(fileName);
 			}
 			else
 			{
 				output = new File(outputPath);
 			}
 			
 			// Make sure the directories exist
 			if (output.isDirectory())
 			{
 				output.mkdirs();
 				output = new File(output.getAbsolutePath() + File.separator + fileName);
 			}
 			else
 			{
 				if (output.getParentFile() != null)
 					output.getParentFile().mkdirs();
 			}
 			
 			// Good point to check for a shutdown
 			if (isAppShuttingDown())
 			{
 				shutdown.set(true);
 				return;
 			}
 			
 			// If the file exists then create a backup file
 			if (output.exists())
 			{
 				output.renameTo(new File(output.getAbsolutePath() + ".backup"));
 			}
 			
 			// Create and write to the file
 			try
 			{
 				final FileOutputStream fos = new FileOutputStream(output);
 				fos.write(data.getBytes("UTF-8"));
 				fos.flush();
 				fos.close();
 				JCommander.getConsole().println(String.format(Constants.OUTPUT_SAVED_MSG, output.getName()));
 			}
 			catch (IOException e)
 			{
 				printError(Constants.ERROR_FAILED_SAVING, false);
 				shutdown(Constants.EXIT_FAILURE);
 			}
 		}
 	}
 	
 	@Override
 	public RESTUserV1 authenticate(final RESTReader reader)
 	{
 		return null;
 	}
 
 	@Override
 	public void printError(final String errorMsg, final boolean displayHelp)
 	{
 		printError(errorMsg, displayHelp, Constants.PULL_COMMAND_NAME);
 	}
 
 	@Override
 	public void printHelp()
 	{
 		printHelp(Constants.PULL_COMMAND_NAME);
 	}
 
 	@Override
 	public boolean loadFromCSProcessorCfg()
 	{
 		return ids.size() == 0 && !pullTopic;
 	}
 }
