 package edu.umich.lsa.cscs.gridsweeper;
 
 import java.io.*;
 import java.util.*;
 
 import edu.umich.lsa.cscs.gridsweeper.parameters.ParameterMap;
 
 /**
  * A class that encapsulates setup data for a model run. Includes a copy of the
  * settings to be passed on to the running model, file transfer data, run number,
  * random seed, and the adapter class to use.
  * @author Ed Baskerville
  *
  */
 public class RunSetup implements Serializable
 {
	private static final long serialVersionUID = 1L;
 	private Settings settings;
 	private StringMap inputFiles;
 	private String fileTransferSubpath;
 	private ParameterMap parameters;
 	private int runNumber;
 	private int rngSeed;
 	private StringList outputFiles;
 	
 	public RunSetup(Settings settings, StringMap inputFiles, String fileTransferSubpath, ParameterMap parameters, int runNumber, int rngSeed, StringList outputFiles)
 	{
 		this.settings = settings;
 		this.inputFiles = inputFiles;
 		this.fileTransferSubpath = fileTransferSubpath;
 		this.parameters = parameters;
 		this.runNumber = runNumber;
 		this.rngSeed = rngSeed;
 		this.outputFiles = outputFiles;
 	}
 	
 	public Settings getSettings()
 	{
 		return settings;
 	}
 	
 	public int getRngSeed()
 	{
 		return rngSeed;
 	}
 	
 	public int getRunNumber()
 	{
 		return runNumber;
 	}
 	
 	public ParameterMap getParameters()
 	{
 		return parameters;
 	}
 
 	public StringMap getInputFiles()
 	{
 		return inputFiles;
 	}
 	
 	public StringList getOutputFiles()
 	{
 		return outputFiles;
 	}
 	
 	public String getFileTransferSubpath()
 	{
 		return fileTransferSubpath;
 	}
 	
 	public String toString()
 	{
 		HashMap<String, Object> map = new HashMap<String, Object>();
 		map.put("settings", settings);
 		map.put("inputFiles", inputFiles);
 		map.put("fileTransferSubpath", fileTransferSubpath);
 		map.put("runNumber", "" + runNumber);
 		map.put("rngSeed", "" + rngSeed);
 		map.put("outputFiles", outputFiles);
 		
 		return map.toString();
 	}
 }
