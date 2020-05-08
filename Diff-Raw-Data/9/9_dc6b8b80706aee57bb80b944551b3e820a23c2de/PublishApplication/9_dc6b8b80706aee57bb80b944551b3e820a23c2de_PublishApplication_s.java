 package org.yellcorp.app.jsfl.publish;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.IOException;
 import java.util.Arrays;
 import java.util.ResourceBundle;
 import java.util.concurrent.TimeoutException;
 
 import org.apache.commons.cli.CommandLine;
 import org.apache.commons.cli.Options;
 import org.apache.commons.io.FileUtils;
 import org.yellcorp.app.cli.CommandLineApplication;
 import org.yellcorp.app.cli.errors.CommandLineException;
 import org.yellcorp.app.cli.errors.RunException;
 import org.yellcorp.app.jsfl.core.Common;
 import org.yellcorp.app.jsfl.core.OptionFactory;
 import org.yellcorp.app.jsfl.core.ScriptBridge;
 import org.yellcorp.app.jsfl.core.errors.ProcessException;
 import org.yellcorp.app.jsfl.core.errors.UnsupportedOSException;
 import org.yellcorp.util.FileSetResolver;
 
 public class PublishApplication implements CommandLineApplication
 {
 	private static final String LOG_MARKER_SUCCESS = "+++";
 	private static final String LOG_MARKER_ERROR = "!!!";
 	
 	private ScriptBridge bridge;
 
 	@Override
 	public String getApplicationName()
 	{
 		return "publish";
 	}
 	
 	@Override
 	public Options getOptions()
 	{
 		Options options = Common.getCommonOptions();
 		
 		OptionFactory optionFactory = new OptionFactory(
 				ResourceBundle.getBundle("PublishUsageStrings"));
 		
 		options.addOption(optionFactory.createOption("move", "m", true, false));
 		
 		return options;
 	}
 	
 	@Override
 	public int run(CommandLine commandLine) throws CommandLineException,
 			RunException
 	{
 		bridge = Common.createBridgeFromOptions(commandLine);
 		int errorCount = 0;
 		
 		File targetFolder = null;
 		if (commandLine.hasOption("move"))
 		{
 			targetFolder = new File(commandLine.getOptionValue("move"));
 		}
 		
 		String[] args = commandLine.getArgs();
 		FileSetResolver resolver = new FileSetResolver();
 		File currentFolder = new File(".");
 		for (String arg : args)
 		{
 			try	{
 				resolver.add(currentFolder, arg);
 			}
 			catch (IOException ioe)
 			{
 				System.err.println("Error resolving " + arg + ": " + 
 						ioe.getLocalizedMessage());
 				errorCount++;
 			}
 		}
 		
 		for (File fla : resolver.getFiles())
 		{
 			try {
 				File swf = publish(fla, targetFolder);
 				System.out.print("OK: " + swf);
 			}
 			catch (RunException re)
 			{
 				System.err.println("Error publishing " + fla + ": " +
 						re.getLocalizedMessage());
 				errorCount++;
 			}
 		}
 		
 		if (errorCount > 0)
 		{
 			System.err.println("" + errorCount + " errors");
 			return 1;
 		}
 		else
 		{
 			return 0;
 		}
 	}
 	
 	private File publish(File inputFile, File outputFile) throws RunException
 	{
 		try {
 			String inputPath = inputFile.getCanonicalPath();
 			String line;
 			String createdPath = null;
 			
			BufferedReader scriptOut = bridge.run(
					getClass().getResourceAsStream("publish.jsfl"),
					"publish.jsfl", 
					Arrays.asList(inputPath));
 			
 			while ((line = scriptOut.readLine()) != null)
 			{
 				if (line.startsWith(LOG_MARKER_SUCCESS))
 				{
 					createdPath = line.substring(LOG_MARKER_SUCCESS.length());
 				}
 				else if (line.startsWith(LOG_MARKER_ERROR))
 				{
 					throw new RunException(line.substring(LOG_MARKER_ERROR.length()));
 				}
 			}
 			if (createdPath == null)
 			{
 				throw new RunException("No markers present in script output");
 			}
 
 			File inputBase;
 			if (inputPath.toLowerCase().endsWith(".xfl"))
 			{
 				// XFLs silently prepend their output path with ..
 				inputBase = inputFile.getParentFile().getParentFile();
 			}
 			else
 			{
 				inputBase = inputFile.getParentFile();
 			}
 			
 			File createdFile = new File(inputBase, createdPath);
 			
 			if (outputFile == null)
 			{
 				return createdFile;
 			}
 			else
 			{
 				return moveFile(createdFile, outputFile);
 			}
 		}
 		catch (IOException e)
 		{
 			throw new RunException(e);
 		}
 		catch (InterruptedException e)
 		{
 			throw new RunException(e);
 		}
 		catch (TimeoutException e)
 		{
 			throw new RunException(e);
 		}
 		catch (UnsupportedOSException e)
 		{
 			throw new RunException(e);
 		}
 		catch (ProcessException e)
 		{
 			throw new RunException(e);
 		}
 	}
 
 	private static File moveFile(File source, File target) throws IOException
 	{
 		if (target.isDirectory())
 		{
 			target = new File(target, source.getName());
 		}
 		if (target.exists())
 		{
 			if (!target.delete())
 			{
 				throw new IOException("Couldn't overwrite " + target);
 			}
 		}
 		FileUtils.moveFile(source, target);
 		return target;
 	}
 }
