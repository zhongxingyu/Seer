 package com.hexcore.cas.model;
 
 import java.io.BufferedInputStream;
 import java.io.BufferedOutputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.lang.management.ManagementFactory;
 import java.lang.management.MemoryMXBean;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import java.util.StringTokenizer;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipOutputStream;
 
 import com.hexcore.cas.math.Vector2i;
 import com.hexcore.cas.utilities.Log;
 import com.javamex.classmexer.MemoryUtil;
 import com.javamex.classmexer.MemoryUtil.VisibilityFilter;
 
 /**
  * Class WorldStreamer
  * 	The class that streams all world generations to disk as simulation
  * 	runs when hard disk memory is selected.
  * 
  * @authors Megan Duncan
  */
 
 public class WorldStreamer
 {
 	private static final String		TAG = "WorldStreamer";
 	
 	private boolean					reset = false;
 	private volatile boolean		started = false;
 	
 	private int						differenceGenerationCounter = 0;
 	private int						numGenerations = 0;
 	
 	private String					cawFilename = null;
 	private String					tmpDir = null;
 	
 	/**
 	 * WorldStreamer default constructor.
 	 */
 	public WorldStreamer()
 	{
 	}
 	
 	/**
 	 * Returns the CAW file name as given by the file name of the world.
 	 * 
 	 * @return - the CAW file name of the world
 	 */
 	public String getCawFilename()
 	{
 		return cawFilename;
 	}
 	
 	/**
 	 * Looks for the generation with the corresponding given generation number on disk and reads in.
 	 * 
 	 * Returns null during the following conditions:
 	 * 	No configuration file is found;
 	 * 	Configuration file is found but has not grid type symbol; or
 	 * 	No generation with the given number.
 	 * 
 	 * Otherwise returns the generation with all its information read in.
 	 * 
 	 * @param genNum - the generation number that has been requested
 	 * @return - the complete generation
 	 */
 	public Grid getGeneration(int genNum)
 	{
 		Log.debug(TAG, "getGeneration(" + genNum + ") called.");
 		
 		boolean configFound = false;
 		boolean genFound = false;
 		char type = 'N';
 		File currFile = null;
 		File folder = new File(tmpDir);
 		File[] listOfFiles = folder.listFiles();
 		Grid gen = null;
 		InputStream in = null;
 		int properties = 0;
 		int x = -1;
 		int y = -1;
 		String currFilename = null;
 		
 		try
 		{
 			Log.debug(TAG, "Looking for configuration file");
 			//Searching for the configuration file
 			for(int i = 0; i < listOfFiles.length; i++)
 			{
 				if(listOfFiles[i].isFile())
 				{
 					currFilename = listOfFiles[i].getName();
 					currFile = new File(tmpDir + "/" + currFilename);
 					in = new BufferedInputStream(new FileInputStream(currFile));
 					
 					String fileData = "";
 					
 					if(currFilename.endsWith(".cac"))
 					{
 						configFound = true;
 						
 						byte[] data = new byte[1024];
 						@SuppressWarnings("unused")
 						int len = 1024;
 						while((len = in.read(data)) > 0)
 						{
 							fileData += new String(data);
 						}
 
 						StringTokenizer token = new StringTokenizer(fileData);
 						
 						x = Integer.parseInt(token.nextToken());
 						y = Integer.parseInt(token.nextToken());
 						type = token.nextToken().charAt(0);
 						properties = Integer.parseInt(token.nextToken());
 						
 						Vector2i gridSize = new Vector2i(x, y);
 						Cell cell = new Cell(properties);
 						switch(type)
 						{
 							case 'r':
 							case 'R':
 								gen = new RectangleGrid(gridSize, cell);
 								break;
 							case 'h':
 							case 'H':
 								gen = new HexagonGrid(gridSize, cell);
 								break;
 							case 't':
 							case 'T':
 								gen = new TriangleGrid(gridSize, cell);
 								break;
 							case 'v':
 							case 'V':
 								gen = new VonNeumannGrid(gridSize, cell);
 								break;
 							default:
 								Log.error(TAG, "Error retrieving generaion - unable to create a grid with no type");
 								return null;
 						}
 						
 						break;
 					}
 					else
 						continue;
 				}
 			}
 			
 			if(!configFound)
 			{
 				Log.error(TAG, "Error retrieving generaion - configuration file not found");
 				return null;
 			}
 			
 			String theGenerationData = "";
 			
 			currFilename = genNum + ".cag";
 			currFile = new File(tmpDir + "/" + currFilename);
 			in = new BufferedInputStream(new FileInputStream(currFile));
 			if(currFile.exists())
 			{
 				genFound = true;
 				
 				byte[] data = new byte[1024];
 				@SuppressWarnings("unused")
 				int len = 1024;
 				while((len = in.read(data)) > 0)
 				{
 					theGenerationData += new String(data);
 				}
 				
 				theGenerationData = theGenerationData.trim();
 			}
 
 			if(genFound)
 			{
 				Log.debug(TAG, "Found generation " + genNum);
 				
 				StringTokenizer token = new StringTokenizer(theGenerationData);
 				
 				String generationType = token.nextToken();
 				
 				if(generationType.compareTo("E") == 0)
 				{
 					Log.debug(TAG, "Generation " + genNum + " is an entire generation: LOADING");
 					
 					for(int rows = 0; rows < y; rows++)
 					{
 						for(int cols = 0; cols < x; cols++)
 						{
 							double[] vals = new double[properties];
 							
 							for(int j = 0; j < properties; j++)
 								vals[j] = Double.parseDouble(token.nextToken());
 							
 							gen.setCell(cols, rows, vals);
 						}
 					}
 				}
 				else
 				{
 					Log.debug(TAG, "Generation " + genNum + " is a difference generation: Searching for an entire generation to base off of");
 					
 					ArrayList<String> generations = new ArrayList<String>();
 					
 					generations.add(theGenerationData);
 					
 					int prevs = genNum - 1;
 					String prevData;
 					
 					while(true)
 					{
 						prevData = "";
 						
 						Log.debug(TAG, "Looking for generation " + prevs);
 						currFilename = prevs + ".cag";
 						currFile = new File(tmpDir + "/" + currFilename);
 						in = new BufferedInputStream(new FileInputStream(currFile));
 						if(currFile.exists())
 						{
 							byte[] data = new byte[1024];
 							@SuppressWarnings("unused")
 							int len = 1024;
 							while((len = in.read(data)) > 0)
 							{
 								prevData += new String(data);
 							}
 							
 							prevData = prevData.trim();
 							
 							generations.add(0, prevData);
 						}
 						else
 						{
 							Log.error(TAG, "Cannot reconstruct generation - unable to find an entire generation to base off.");
 							return null;
 						}
 						
 						if(prevData.charAt(0) == 'E')
 							break;
 						
 						prevs--;
 					}
 					
 					Log.debug(TAG, "Found all necessary generations: CONSTRUCTING");
 					
 					while(generations.size() > 0)
 					{
 						String theData = generations.get(0);
 						
 						token = new StringTokenizer(theData);
 						generationType = token.nextToken();
 						
 						if(generationType.compareTo("E") == 0)
 						{
 							for(int rows = 0; rows < y; rows++)
 							{
 								for(int cols = 0; cols < x; cols++)
 								{
 									double[] vals = new double[properties];
 									
 									for(int j = 0; j < properties; j++)
 										vals[j] = Double.parseDouble(token.nextToken());
 									
 									gen.setCell(cols, rows, vals);
 								}
 							}
 						}
 						else
 						{
 							while(token.hasMoreTokens())
 							{
 								int col = Integer.parseInt(token.nextToken());
 								int row = Integer.parseInt(token.nextToken());
 								
 								double[] vals = new double[properties];
 								for(int j = 0; j < properties; j++)
 									vals[j] = Double.parseDouble(token.nextToken());
 								
 								gen.setCell(col, row, vals);
 							}
 						}
 						
 						generations.remove(0);
 					}
 				}
 			}
 			else
 			{
 				Log.error(TAG, "Error retrieving generation - generation file " + genNum + " not found");
 				return null;
 			}
 			
 			in.close();
 			in = null;
 			
 			return gen;
 		}
 		catch(IOException ex)
 		{
 			Log.error(TAG, "Error retrieving generation - " + ex.getMessage());
 			ex.printStackTrace();
 			return null;
 		}
 	}
 	
 	/**
 	 * Looks for all generations on disk and reads them in one by one. Only reads in the total number
 	 * of generations that the system can handle with an extra gap of 500 generations.
 	 * 
 	 * Returns null during the following conditions:
 	 * 	No configuration file is found; or
 	 * 	Configuration file is found but has not grid type symbol.
 	 * 
 	 * Otherwise returns the list of generations.
 	 * 
 	 * Any missing generation file will result in a Grid that is null in that spot in the returned list.
 	 * 
 	 * @return - the list of complete generations with 500 gap
 	 */
 	public List<Grid> getGenerations()
 	{
 		boolean configFound = false;
 		char type = 'N';
 		File currFile = null;
 		File folder = new File(tmpDir);
 		File[] listOfFiles = folder.listFiles();
 		Grid gen = null;
 		InputStream in = null;
 		int properties = 0;
 		int x = -1;
 		int y = -1;
 		List<Grid> generations = Collections.synchronizedList(new ArrayList<Grid>());
 		MemoryMXBean bean = ManagementFactory.getMemoryMXBean();
 		String currFilename = null;
 		
 		try
 		{
 			//Searching for the configuration file 
 			for(int i = 0; i < listOfFiles.length; i++)
 			{
 				if(listOfFiles[i].isFile())
 				{
 					currFilename = listOfFiles[i].getName();
 					currFile = new File(tmpDir + "/" + currFilename);
 					in = new BufferedInputStream(new FileInputStream(currFile));
 					
 					String fileData = "";
 					
 					if(currFilename.endsWith(".cac"))
 					{
 						configFound = true;
 						
 						byte[] data = new byte[1024];
 						@SuppressWarnings("unused")
 						int len = 1024;
 						while((len = in.read(data)) > 0)
 						{
 							fileData += new String(data);
 						}
 						
 						StringTokenizer token = new StringTokenizer(fileData);
 						
 						x = Integer.parseInt(token.nextToken());
 						y = Integer.parseInt(token.nextToken());
 						type = token.nextToken().charAt(0);
 						properties = Integer.parseInt(token.nextToken());
 						
 						Vector2i gridSize = new Vector2i(x, y);
 						Cell cell = new Cell(properties);
 						switch(type)
 						{
 							case 'r':
 							case 'R':
 								gen = new RectangleGrid(gridSize, cell);
 								break;
 							case 'h':
 							case 'H':
 								gen = new HexagonGrid(gridSize, cell);
 								break;
 							case 't':
 							case 'T':
 								gen = new TriangleGrid(gridSize, cell);
 								break;
 							case 'v':
 							case 'V':
 								gen = new VonNeumannGrid(gridSize, cell);
 								break;
 							default:
 								Log.error(TAG, "Error retrieving all generations - unable to create a grid with no type");
 								return null;
 						}
 						
 						break;
 					}
 					else
 						continue;
 				}
 			}
 			
 			if(!configFound)
 			{
 				Log.error(TAG, "Error retrieving all generations - configuration file not found");
 				return null;
 			}
 
 			int arrSize = 0;
 			for(int i = 0; i < listOfFiles.length; i++)
 				if(listOfFiles[i].isFile())
 					if(listOfFiles[i].getName().endsWith(".cag"))
 						arrSize++;
 			int finalGen = arrSize - 1;
 			
 			int gensToLoad = arrSize;
 			int scale = 500;
 			long generationSize = MemoryUtil.deepMemoryUsageOf(gen, VisibilityFilter.ALL);
 			long maxHeap = bean.getHeapMemoryUsage().getMax();
 			long toBeUsedHeap = generationSize * gensToLoad;
 			
 			if(toBeUsedHeap >= maxHeap - (scale * 1024 * 1024))
 			{
 				while(true)
 				{
 					gensToLoad--;
 					toBeUsedHeap = generationSize * gensToLoad;
 					if(toBeUsedHeap < maxHeap - (scale * 1024 * 1024))
 						break;
 				}
 			}
 			Log.information(TAG, toBeUsedHeap + " bean heap memory to be used out of a maximum of " + maxHeap + " for " + gensToLoad + " generations.");
 			
 			ArrayList<String> incompleteGens = new ArrayList<String>();
 			for(int i = 0; i < gensToLoad; i++)
 				incompleteGens.add("");
 			
 			//Searching for all generation files and adding them to incompleteGens
 			for(int i = 0; i < listOfFiles.length; i++)
 			{
 				if(listOfFiles[i].isFile())
 				{
 					currFilename = listOfFiles[i].getName();
 					currFile = new File(tmpDir + "/" + currFilename);
 					in = new BufferedInputStream(new FileInputStream(currFile));
 					
 					String fileData = "";
 					
 					if(currFilename.endsWith(".cag"))
 					{
 						byte[] data = new byte[1024];
 						@SuppressWarnings("unused")
 						int len = 1024;
 						while((len = in.read(data)) > 0)
 						{
 							fileData += new String(data);
 						}
 						
 						fileData = fileData.trim();
 						
 						int index = currFilename.lastIndexOf('/');
 						if(index == -1)
 							index = 0;
 						int fileGenIndex = currFilename.indexOf(".cag");
 						int fileGenNum = 0;
 						if(index != 0)
 							fileGenNum = Integer.parseInt(currFilename.substring(index + 1, fileGenIndex));
 						else
 							fileGenNum = Integer.parseInt(currFilename.substring(index, fileGenIndex));
 						
 						int arrIndex = gensToLoad - (finalGen - fileGenNum) - 1;
 						
 						if(arrIndex < 0)
 							continue;
 						
 						incompleteGens.remove(arrIndex);
 						incompleteGens.add(arrIndex, fileData);
 					}
 					else
 						continue;
 				}
 			}
 			
 			//Build up all the generations from incompleteGens and add them to generations
 			for(int i = 0; i < incompleteGens.size(); i++)
 			{
 				StringTokenizer token = new StringTokenizer(incompleteGens.get(i));
 				
 				String generationType = token.nextToken();
 				
 				if(generationType.compareTo("E") == 0)
 				{
 					for(int rows = 0; rows < y; rows++)
 					{
 						for(int cols = 0; cols < x; cols++)
 						{
 							double[] vals = new double[properties];
 							
 							for(int j = 0; j < properties; j++)
 								vals[j] = Double.parseDouble(token.nextToken());
 							
 							gen.setCell(cols, rows, vals);
 						}
 					}
 				}
 				else
 				{
 					while(token.hasMoreTokens())
 					{
 						int col = Integer.parseInt(token.nextToken());
 						int row = Integer.parseInt(token.nextToken());
 						
 						double[] vals = new double[properties];
 						for(int j = 0; j < properties; j++)
 							vals[j] = Double.parseDouble(token.nextToken());
 						
 						gen.setCell(col, row, vals);
 					}
 				}
 				
 				generations.add(gen.clone());
 			}
 			
 			in.close();
 			in = null;
 			
 			return generations;
 		}
 		catch(IOException ex)
 		{
 			Log.error(TAG, "Error retrieving all generations - " + ex.getMessage());
 			ex.printStackTrace();
 			return null;
 		}
 	}
 	
 	/**
 	 * Returns the last generation that was written to disk by using the getGeneration function.
 	 * 
 	 * @return - the last generation written
 	 */
 	public Grid getLastGeneration()
 	{
 		return getGeneration(numGenerations - 1);
 	}
 	
 	/**
 	 * Returns true if the streamer has been started and false if the streamer has not been started.
 	 * 
 	 * @return - the boolean on the started status
 	 */
 	public boolean hasStarted()
 	{
 		return started;
 	}
 	
 	/**
 	 * Resets to the given world.
 	 * 
 	 * If the streamer has already started, then the reset is a soft reset to Generation Zero.
 	 * Otherwise, it is a hard reset to a completely new world.
 	 * 
 	 * @param world - the world that is to be reset to
 	 */
 	public void reset(World world)
 	{
 		if(started)
 		{
 			reset = true;
 			start(world);
 		}
 		else
 		{
 			reset = false;
 			start(world);
 		}
 	}
 	
 	/**
 	 * Sets the CAW file name of the streamer to the given file name.
 	 * 
 	 * @param name - the file name that must be set to
 	 */
 	public void setWorldFilename(String name)
 	{
 		cawFilename = name;
 	}
 	
 	/**
 	 * Starts the streamer up with the given world.
 	 * 
 	 * If it is not a soft reset, a new temporary folder is created and set as the current CAW file name.
 	 * 
 	 * Regardless, the streamer sets the generation counter to 0, re-writes the details from the
 	 * world and starts.
 	 * 
 	 * @param world - the world that the streamer is set to
 	 */
 	public void start(World world)
 	{
 		if(!reset)
 		{
 			File caw = null;
 			if(world.getFilename() == null)
 				world.setFileName("worlds/newWorld.caw");
 			
 			int cawIndex = world.getFilename().indexOf(".caw");
 			
 			String name = (cawIndex != -1 ? world.getFilename() : world.getFilename() + ".caw");
 			
 			if(cawFilename == null)
 				cawFilename = name;
 			
 			caw = new File(name);
 			int worldNum = 1;
 			
 			while(caw.exists())
 			{
				int underIndex = name.lastIndexOf("__");
 				if(underIndex != -1)
					name = name.substring(0, underIndex) + "__" + worldNum + ".caw";
 				else
					name = name.substring(0, name.indexOf(".caw")) + "__" + worldNum + ".caw";
 				
 				caw = new File(name);
 				worldNum++;
 			}
 			
 			cawFilename = name;
 			
 			int backIndex = cawFilename.lastIndexOf("\\");
 			int forwardIndex = cawFilename.lastIndexOf("/");
 			
 			if(backIndex != -1)
 				tmpDir = "worlds/[tmp]" + cawFilename.substring(backIndex + 1, cawFilename.lastIndexOf("."));
 			else if(forwardIndex != -1)
 				tmpDir = "worlds/[tmp]" + cawFilename.substring(forwardIndex + 1, cawFilename.lastIndexOf("."));
 			boolean res = (new File(tmpDir).mkdirs());
 			
 			if(!res)
 			{
 				String[] files = (new File(tmpDir)).list();
 				for(int i = 0; i < files.length; i++)
 				{
 					String currName = tmpDir + "/" + files[i];
 					File currFile = new File(currName);
 					if(!currFile.delete())
 						Log.error(TAG, "Problem clearing out temporary file " + currName);
 				}
 			}
 			
 			Log.debug(TAG, "cawFilename : " + cawFilename);
 			Log.debug(TAG, "tmpDir : " + tmpDir);
 		}
 		
 		numGenerations = 0;
 		
 		writeInitialValues(world);
 		
 		started = true;
 	}
 	
 	/**
 	 * Stops the streamer and streams all information from the temporary folders into a permanent
 	 * zipped CAW file.
 	 */
 	public void stop()
 	{
 		Log.information(TAG, "The program is streaming the world to disk. Please be patient.");
 		
 		File folder = new File(tmpDir);
 		folder.deleteOnExit();
 		
 		File caw = new File(cawFilename);
 		File[] listOfFiles = folder.listFiles();
 		
 		try
 		{
 			ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(caw)));
 		
 			for(int i = 0; i < listOfFiles.length; i++)
 			{
 				if(listOfFiles[i].isFile())
 				{
 					String currFilename = listOfFiles[i].getName();
 					
 					File currFile = new File(tmpDir + "/" + currFilename);
 					currFile.deleteOnExit();
 					
 					FileInputStream in = new FileInputStream(currFile);
 					ZipEntry entry = new ZipEntry(currFilename);
 					
 					out.putNextEntry(entry);
 					byte[] data = new byte[1024];
 					int len = 1024;
 					while((len = in.read(data)) > 0)
 					{
 						out.write(data, 0, len);
 					}
 					out.closeEntry();
 					
 					in.close();
 				}
 			}
 			
 			out.close();
 		}
 		catch(IOException ex)
 		{
 			Log.error(TAG, "Error in stopping - " + ex.getMessage());
 			ex.printStackTrace();
 		}
 		
 		started = false;
 		differenceGenerationCounter = 0;
 	}
 	
 	/**
 	 * Streams the given generation to disk in the temporary folder.
 	 * 
 	 * @param gen - the generation that must be streamed to disk
 	 */
 	public void streamGeneration(Grid prevGen, Grid currGen)
 	{
 		OutputStream out = null;
 		File genFile = null;
 		String str = "";
 		
 		try
 		{
 			int genNum = numGenerations;
 			
 			genFile = new File(tmpDir + "/" + genNum + ".cag");
 			if(!genFile.createNewFile())
 				Log.warning(TAG, "Error streaming generation to world - could not create new temporary generation " + genNum + " file. Overwriting.");
 			out = new BufferedOutputStream(new FileOutputStream(genFile));
 			
 			//Comparing generations
 			List<CoordinatedCell> differentCells = getDifferenceCells(prevGen, currGen, genNum);
 			
 			if(differenceGenerationCounter >= 20)
 			{
 				differentCells.clear();
 				differenceGenerationCounter = 0;
 			}
 			
 			if(differentCells.size() == 0)
 			{
 				str += "E\n";
 				
 				for(int rows = 0; rows < currGen.getHeight(); rows++)
 				{
 					for(int cols = 0; cols < currGen.getWidth(); cols++)
 					{
 						for(int index = 0; index < currGen.getNumProperties() - 1; index++)
 							str += currGen.getCell(cols, rows).getValue(index) + " ";
 						str += currGen.getCell(cols, rows).getValue(currGen.getNumProperties() - 1) + "\n";
 					}
 					str += "\n";
 				}
 				
 				differenceGenerationCounter = 0;
 			}
 			else
 			{
 				str += "D\n";
 				
 				for(int j = 0; j < differentCells.size(); j++)
 				{
 					CoordinatedCell cell = differentCells.get(j);
 					str += cell.x + " " + cell.y + " ";
 					
 					for(int index = 0; index < currGen.getNumProperties() - 1; index++)
 						str += cell.getValue(index) + " ";
 					str += cell.getValue(currGen.getNumProperties() - 1) + "\n";
 				}
 				
 				differenceGenerationCounter++;
 			}
 			
 			out.write(str.getBytes());
 			out.close();
 			
 			numGenerations++;
 		}
 		catch(IOException ex)
 		{
 			Log.error(TAG, "Error streaming generation to the world - " + ex.getMessage());
 			ex.printStackTrace();
 		}
 	}
 	
 	/////////////////////////////////////////////
 	/// Private functions
 	/**
 	 * Gets a list of the cells that are different between the current generation and the previous
 	 * generation.
 	 * 
 	 * If the current generation is Generation Zero, then an empty list is returned.
 	 * 
 	 * If the number of differences between the generations is larger than the threshold,
 	 * an empty list is returned.
 	 * 
 	 * Otherwise returns the list of difference cells.
 	 * 
 	 * @param prevGen - the previous generation
 	 * @param currGen - the current generation
 	 * @param curr - the index of the current generation
 	 * 
 	 * @return - the list of difference cells
 	 */
 	private List<CoordinatedCell> getDifferenceCells(Grid prevGen, Grid currGen, int curr)
 	{
 		if(curr == 0)
 			return new ArrayList<CoordinatedCell>();
 		else
 		{
 			List<CoordinatedCell> differentCells = new ArrayList<CoordinatedCell>();
 			
 			int gridHeight = currGen.getHeight();
 			int gridWidth = currGen.getWidth();
 			int properties = currGen.getNumProperties();
 
 			int totalProperties = gridHeight * gridWidth * properties;
 			int diff = 0;
 			double threshold = (double)properties / (double)(properties + 2);
 			double percent = 1.0;
 			
 			for(int rows = 0; rows < gridHeight; rows++)
 				for(int cols = 0; cols < gridWidth; cols++)
 					for(int props = 0; props < properties; props++)
 						if(prevGen.getCell(cols, rows).getValue(props) != currGen.getCell(cols, rows).getValue(props))
 						{
 							diff++;
 							differentCells.add(new CoordinatedCell(currGen.getCell(cols, rows), cols, rows));
 						}
 			
 			percent = (double)diff / (double)totalProperties;
 			
 			if(percent < threshold)
 				return differentCells;
 			else
 				return new ArrayList<CoordinatedCell>();
 		}
 	}
 	
 	/**
 	 * Requests all the information from the given world and streams everything into the
 	 * temporary folder.
 	 * 
 	 * @param world - the world that the streamer is set to
 	 */
 	private void writeInitialValues(World world)
 	{
 		differenceGenerationCounter = 0;
 		
 		OutputStream out = null;
 		File currFile = null;
 		String data = "";
 		
 		world.setKeepHistory(1);
 		
 		try
 		{
 			Grid genZero = world.getInitialGeneration();
 			char type = genZero.getTypeSymbol();
 			int height = genZero.getHeight();
 			int props = genZero.getNumProperties();
 			int width = genZero.getWidth();
 			
 			currFile = new File(tmpDir + "/config.cac");
 			out = new BufferedOutputStream(new FileOutputStream(currFile));
 			data = width + " " + height + "\n";
 			data += type + "\n";
 			data += props + "\n";
 			out.write(data.getBytes());
 			out.close();
 			
 			currFile = null;
 			data = "";
 			out = null;
 			
 			if(world.getStepAmount() == 1)
 			{
 				currFile = new File(tmpDir + "/rules.cal");
 				out = new BufferedOutputStream(new FileOutputStream(currFile));
 				
 				out.write(world.getRuleCodes().get(0).getBytes());
 				out.close();
 
 				currFile = null;
 				data = "";
 				out = null;
 			}
 			else
 			{
 				ArrayList<String> rulesets = world.getRuleCodes();
 				for(int i = 0; i < rulesets.size(); i++)
 				{
 					currFile = new File(tmpDir + "/rules" + (i + 1) + ".cal");
 					out = new BufferedOutputStream(new FileOutputStream(currFile));
 					out.write(rulesets.get(i).getBytes());
 					out.close();
 					
 					currFile = null;
 					data = "";
 					out = null;
 				}
 			}
 			
 			currFile = new File(tmpDir + "/colours.cacp");
 			out = new BufferedOutputStream(new FileOutputStream(currFile));
 			out.write(world.getColourCode().getBytes());
 			out.close();
 			
 			List<Grid> gens = world.getGenerations();
 			for (int i = 0; i < gens.size(); i++)
 			{
 				Grid grid = gens.get(i);
 				currFile = new File(tmpDir + "/" + i + ".cag");
 				out = new BufferedOutputStream(new FileOutputStream(currFile));
 				
 				List<CoordinatedCell> differentCells;
 				if(i == 0)
 					differentCells = getDifferenceCells(null, grid, i);
 				else
 					differentCells = getDifferenceCells(gens.get(i - 1), grid, i);
 				
 				if(differenceGenerationCounter >= 20)
 				{
 					differentCells.clear();
 					differenceGenerationCounter = 0;
 				}
 				
 				if(differentCells.size() == 0)
 				{
 					data += "E\n";
 					
 					for(int rows = 0; rows < height; rows++)
 					{
 						for(int cols = 0; cols < width; cols++)
 						{
 							for(int index = 0; index < props - 1; index++)
 								data += grid.getCell(cols, rows).getValue(index) + " ";
 							data += grid.getCell(cols, rows).getValue(props - 1) + "\n";
 						}
 						data += "\n";
 					}
 					
 					differenceGenerationCounter = 0;
 				}
 				else
 				{
 					data += "D\n";
 					
 					for(int j = 0; j < differentCells.size(); j++)
 					{
 						CoordinatedCell cell = differentCells.get(j);
 						data += cell.x + " " + cell.y + " ";
 						
 						for(int index = 0; index < props - 1; index++)
 							data += cell.getValue(index) + " ";
 						data += cell.getValue(props - 1) + "\n";
 					}
 					
 					differenceGenerationCounter++;
 				}
 
 				out.write(data.getBytes());
 				out.close();
 				
 				currFile = null;
 				data = "";
 				out = null;
 			}
 			
 			numGenerations = gens.size();
 			
 			world.setGenAmount(numGenerations);
 		}
 		catch(IOException ex)
 		{
 			Log.error(TAG, "Error writing all initial values to the world - " + ex.getMessage());
 			ex.printStackTrace();
 		}
 		
 		world.setKeepHistory(2);
 	}
 	
 	/////////////////////////////////////////////
 	/// Inner classes
 	private class CoordinatedCell extends Cell
 	{
 		public int x;
 		public int y;
 		
 		public CoordinatedCell(Cell cell, int x, int y)
 		{
 			super(cell);
 			this.x = x;
 			this.y = y;
 		}
 	}
 }
