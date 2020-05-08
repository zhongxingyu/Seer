 package com.leafdigital.browserstats.shared;
 
 import java.io.*;
 import java.util.LinkedList;
 
 /** Base class for command-line tools. */
 public abstract class CommandLineTool
 {
 	private boolean failed = false;
 
 	private LinkedList<File> inputFileList = new LinkedList<File>();
 	private boolean showHelp = false;	
 	private boolean stdin = false;
 	
 	private File[] inputFiles = null;
 	
 	/** @return Input files or null if stdin should be used. */
 	protected File[] getInputFiles()
 	{
 		return inputFiles;
 	}
 	
 	/**
 	 * Runs complete tool task.
 	 * @param args Command-line arguments
 	 */
 	protected void run(String[] args)
 	{
 		// Already showed error
 		if(failed)
 		{
 			return;
 		}
 		
 		// Process arguments
 		try
 		{
 			processArgs(args, true);
 		}
 		catch(IllegalArgumentException e)
 		{
 			System.err.println("Error processing command-line arguments:\n\n" +
 				e.getMessage());			
 			return;
 		}
 		// Show help if required
 		if(showHelp)
 		{
 			showHelp();
 			return;
 		}
 		// Do task
 		go();
 	}
 	
 	/**
 	 * Indicates that initialisation (e.g. constructor) has failed and 
 	 * program should quit
 	 */
 	protected void failed()
 	{
 		failed = true;
 	}
 	
 	/**
 	 * Processes an arguments file.
 	 * @param f File
 	 * @throws IOException Error loading file
 	 * @throws IllegalArgumentException Problem with arguments in file
 	 */
 	protected void processArgsFile(File f) throws IOException, IllegalArgumentException
 	{
 		BufferedReader reader = new BufferedReader(new InputStreamReader(			
 			new FileInputStream(f), "UTF-8"));
 		try
 		{
 			LinkedList<String> args = new LinkedList<String>();
 			while(true)
 			{
 				String line = reader.readLine();
 				if(line == null)
 				{
 					// EOF
 					break;
 				}
 				
 				line = line.trim();
 				if(line.equals("") || line.startsWith("#"))
 				{
 					// Skip blank lines and comments
 					continue;
 				}
 				
 				args.add(line);
 			}
 			processArgs(args.toArray(new String[args.size()]), false);
 		}
 		finally
 		{
 			reader.close();
 		}
 	}
 	
 	/**
 	 * Checks that the required number of additional parameters are available.
 	 * @param args Argument array
 	 * @param i Index of first value in parameter
 	 * @param required Number of additional values required
 	 * @throws IllegalArgumentException If there aren't that many
 	 */
 	protected static void checkArgs(String[] args, int i, int required)
 		throws IllegalArgumentException
 	{
 		if(i+required >= args.length)
 		{
 			throw new IllegalArgumentException("Option " + args[i] + " requires " +
 				required + " parameter(s)");
 		}
 	}
 
 	/**
 	 * Processes arguments from command-line or argument file.
 	 * @param args Arguments
 	 * @param commandLine True if this is the top-level (command-line) call
 	 * @throws IllegalArgumentException Any incorrect argument
 	 */
 	private void processArgs(String[] args, boolean commandLine) 
 		throws IllegalArgumentException
 	{
 		if(args.length==0 && commandLine)
 		{
 			showHelp = true;
 			return;			
 		}
 		
 		int i=0;
 		for(; i<args.length; )
 		{
 			if(args[i].startsWith("@"))
 			{
 				String argFileName = args[i].substring(1);
 				if(argFileName.equals(""))
 				{
 					checkArgs(args, i, 1);
 					argFileName = args[i+1];
 					i+=2;
 				}
 				File argFile = new File(argFileName);
 				if(!argFile.exists())
 				{
 					throw new IllegalArgumentException("Arguments file does not exist: " + argFile);
 				}
 				try
 				{
 					processArgsFile(argFile);					
 				}
 				catch(IOException e)
 				{
 					throw new IllegalArgumentException("Error reading args file");
 				}
 				continue;
 			}
 			if(args[i].equals("-help"))
 			{
 				showHelp = true;
 				return;
 			}
 			if(args[i].equals("-stdin"))
 			{
 				stdin = true;
 				i++;
 				continue;
 			}
 			if(args[i].equals("--"))
 			{
 				i++;
 				break;
 			}
 			
 			int result = processArg(args, i);			
 			if(result != 0)
 			{
 				i+=result;
 				continue;				
 			}
 			
 			if(args[i].startsWith("-"))
 			{
 				throw new IllegalArgumentException("Unknown option: " + args[i]);
 			}
 			break;
 		}
 		
 		for(; i<args.length; i++)
 		{
 			File f = new File(args[i]);				
 			if(!f.exists())
 			{
 				throw new IllegalArgumentException("Input file not found: " + f);
 			}
 			if(!f.canRead())
 			{
 				throw new IllegalArgumentException("Input file not readable: " + f);
 			}
 			inputFileList.add(f);
 		}
 		
 		if (commandLine)
 		{
 			inputFiles = inputFileList.toArray(new File[inputFileList.size()]);
 			inputFileList = null;
 			if(inputFiles.length > 0 && stdin)
 			{
 				throw new IllegalArgumentException(
 					"Cannot specify both input files and -stdin");
 			}
 			if(inputFiles.length == 0 && !stdin && requiresInput())
 			{
 				throw new IllegalArgumentException(
 					"Must specify either -stdin or input file(s)");
 			}
 			if(stdin)
 			{
 				inputFiles = null;
 			}
 			
 			validateArgs();
 		}
 	}
 	
 	/**
 	 * Adjust if input files are not required (e.g. for a -test option)
 	 * @return False if files aren't required, default is true
 	 */
 	protected boolean requiresInput()	
 	{
 		return true;
 	}
 	
 	/**
 	 * Shows help from a file called commandline.txt in same folder as class.
 	 */
 	private void showHelp()
 	{
 		try
 		{
 			InputStream stream = getClass().getResourceAsStream("commandline.txt");
 			if(stream==null)
 			{
 				throw new IOException("Helpfile missing");
 			}
 			BufferedReader reader = new BufferedReader(new InputStreamReader(
 				stream, "UTF-8"));
 			while(true)
 			{
 				String line = reader.readLine();
 				if(line==null)
 				{
 					break;
 				}
 				System.out.println(line);
 			}
 		}
 		catch(IOException e)
 		{
 			// Come on
 			System.err.println("Cannot load command-line help.");
 		}
 	}		
 	
 	/**
 	 * Process a single argument.
 	 * @param args Argument array
 	 * @param i Index of argument to process
 	 * @return Number of arguments used (e.g. 1 if there are no parameters to
 	 *   this argument), or 0 if the argument is unknown
 	 * @throws IllegalArgumentException If arguments are not valid
 	 */
 	protected abstract int processArg(String[] args, int i) 
 		throws IllegalArgumentException;
 	
 	/**
 	 * Validates arguments once all have been read. May also carry out tasks
 	 * required to initialise default arguments after reading.
 	 * @throws IllegalArgumentException If arguments are not valid
 	 */
 	protected abstract void validateArgs() throws IllegalArgumentException;
 	
 	/**
 	 * Goes ahead with actual task now that all arguments have been read.
 	 */
 	protected abstract void go();
 }
