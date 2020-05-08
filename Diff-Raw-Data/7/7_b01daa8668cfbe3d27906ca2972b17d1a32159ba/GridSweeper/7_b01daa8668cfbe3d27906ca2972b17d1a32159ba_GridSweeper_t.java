 package edu.umich.lsa.cscs.gridsweeper;
 
 import org.ggf.drmaa.*;
 
 
 import java.io.*;
 import java.text.DateFormat;
 import java.util.*;
 
 import static edu.umich.lsa.cscs.gridsweeper.StringUtils.*;
 import static edu.umich.lsa.cscs.gridsweeper.DateUtils.*;
 import static edu.umich.lsa.cscs.gridsweeper.DLogger.*;
 
 /**
  * The GridSweeper command-line tool for job submission. Takes a .gsexp
  * XML experiment file and/or a bunch of command line options and submits
  * the resulting experiment to the grid via DRMAA.
  * Warning: begun on a houseboat in Paris. May still contain strange French bugs.
  * 
  * @author Ed Baskerville
  *
  */
 public class GridSweeper
 {
 	enum RunType
 	{
 		RUN,
 		DRY,
 		NORUN
 	}
 	
 	class CaseRun
 	{
 		ExperimentCase expCase;
 		String caseId;
 		int runNum;
 		int rngSeed;
 		
 		public CaseRun(ExperimentCase expCase, String caseId, int runNum, int rngSeed)
 		{
 			this.expCase = expCase;
 			this.caseId = caseId;
 			this.runNum = runNum;
 			this.rngSeed = rngSeed;
 		}
 	}
 	
 	static String className;
 	
 	static
 	{
 		className = GridSweeper.class.toString();
 	}
 	
 	String root;
 	
 	Experiment experiment;
 	RunType runType = RunType.RUN;
 	List<ExperimentCase> cases = null;
 	
 	boolean useFileTransfer = false;
 	
 	Calendar cal;
 	
 	String dateStr;
 	String timeStr;
 	String expDir;
 	
 	String fileTransferSubpath;
 	
 	Session drmaaSession;
 	StringMap caseIdToJobIdMap;
 	Map<String, CaseRun> jobIdToRunMap;
 	
 	public GridSweeper()
 	{
 		cal = Calendar.getInstance(); 
 	}
 	
 	/**
 	 * Generates experiment cases and sets up in preparation for grid submission.
 	 * Experiment results are collated in a master experiment directory,
 	 * specified in the user settings, in a subdirectory tagged with the experiment name
 	 * and date/time ({@code <name>/YYYY-MM-DD/hh-mm-ss}). If a shared filesystem is not
 	 * available, files are first staged to the experiment results directory on the
 	 * file transfer system.
 	 * Finally, a DRMAA session is established, and each case is submitted.
 	 * @throws GridSweeperException
 	 */
 	public void submitExperiment() throws GridSweeperException
 	{
 		if(runType == RunType.NORUN) return;
 		
 		String expName = experiment.getName();
 		if(runType == RunType.DRY)
 		{
 			System.err.println("Performing dry run for experiment \""
 					+ expName + "\"...");
 		}
 		else
 		{
 			System.err.println("Running experiment \""
 					+ experiment.getName() + "\"...");	
 		}
 		
 		Settings settings = experiment.getSettings();
 
 		// Assemble cases
 		try
 		{
 			cases = experiment.generateCases();
 		}
 		catch (ExperimentException e)
 		{
 			// TODO: use ExperimentException to create better error information
 			throw new GridSweeperException("Could not generate experiment cases", e);
 		}
 		
 		// Set up main experiment directory
 		setUpExperimentDirectory(settings);
 
 		// Set up directory & input files on file transfer system if asked for
 		if(runType == RunType.RUN && 
 				settings.getBooleanProperty("UseFileTransfer"))
 		{
 			setUpFileTransfer(settings);
 		}
 		
 		// Create experiment XML in output directory
 		String xmlPath = appendPathComponent(expDir, "experiment.gsexp");
 		try
 		{
 			experiment.writeToFile(xmlPath, true);
 		}
 		catch(Exception e)
 		{
 			throw new GridSweeperException("Could not write experiment XML to"
 					+ xmlPath, e);
 		}
 		
 		// Enumerate and submit cases
 		submitCases();
 		
 		// Finally, 
 		switch(runType)
 		{
 			case DRY:
 				System.err.println("Dry run complete.");
 				break;
 			case RUN:
 				System.err.println("Experiment submitted.");
 				break;
 		}
 		
 	}
 	
 	private void setUpExperimentDirectory(Settings settings)
 		throws GridSweeperException
 	{
 		try
 		{
 			String expsDir = expandTildeInPath(settings.getProperty("ExperimentsDirectory"));
 			
 			// First set up big directory for the whole experiment
 			// Located in <experimentDir>/<experimentName>/<experimentDate>/<experimentTime>
 			String expName = experiment.getName();
 			if(expName == null)
 			{
 				throw new GridSweeperException("Experiment name must be specified.");
 			}
 			
 			dateStr = getDateString(cal);
 			timeStr = getTimeString(cal);
 			String expSubDir = String.format("%s%s%s%s%s", expName, getFileSeparator(), dateStr, getFileSeparator(), timeStr);
 			
 			expDir = appendPathComponent(expsDir, expSubDir);
 			finer("Experiment subdirectory: " + expDir);
 
 			File expDirFile = new File(expDir);
 			expDirFile.mkdirs();
 			
 			System.err.println("Created experiment directory \""
 					+ expDir + "\".");
 		}
 		catch(Exception e)
 		{
 			throw new GridSweeperException("Could not create experiment directory "
 					+ expDir, e);
 		}
 	}
 	
 	private void setUpFileTransfer(Settings settings)
 		throws GridSweeperException
 	{
 		FileTransferSystem fts = null;
 		try
 		{
 			System.err.println("Setting up file transfer system...");
 			
 			String className = settings.getSetting("FileTransferSystemClassName");
 			Settings ftsSettings = settings.getSettingsForClass(className);
 			fts = FileTransferSystemFactory.getFactory().getFileTransferSystem(className, ftsSettings);
 			fts.connect();
 			
 			boolean alreadyExists;
 			do
 			{
 				fileTransferSubpath = UUID.randomUUID().toString();
 				alreadyExists = fts.fileExists(fileTransferSubpath);
 			}
 			while(alreadyExists);
 			
 			System.err.println("Done setting up file transfer.");
 			
 			// If file transfer is on, make the directory
 			// and upload input files
 			StringMap inputFiles = experiment.getInputFiles();
 			
 			if(inputFiles.size() > 0)
 			{
 				System.err.println("Uploading input files...");
 				
 				String inputDir = appendPathComponent(fileTransferSubpath, "input");
 				fts.makeDirectory(inputDir);
 				
 				for(String localPath : inputFiles.keySet())
 				{
 					String remotePath = appendPathComponent(inputDir, inputFiles.get(localPath));
 					System.err.println("Uploading file \"" + localPath
 							+ "\" to \"" + remotePath + "\"");
 					
 					fts.uploadFile(localPath, remotePath);
 				}
 				
 				System.err.println("Done uploading input files.");
 			}
 			
 			fts.disconnect();
 		}
 		catch(Exception e)
 		{
 			throw new GridSweeperException("Could not set up file trasfer system", e);
 		}
 	}
 	
 	public void submitCases() throws GridSweeperException
 	{
 		if(runType == RunType.NORUN) return;
 		
 		try
 		{
 			// Establish DRMAA session, unless this is a dry run
 			if(runType == RunType.RUN)
 			{
 				System.err.println("Establishing grid session");
 				drmaaSession = SessionFactory.getFactory().getSession();
 				drmaaSession.init(null);
 			}
 			
 			// Set up and run each case
 			caseIdToJobIdMap = new StringMap();
 			jobIdToRunMap = new HashMap<String, CaseRun>();
 			
 			if(cases.size() > 1)
 				System.err.println("Submitting cases:");
 			for(ExperimentCase expCase : cases)
 			{
 				runCase(expCase);
 			}
 			if(cases.size() > 1)
 				System.err.println("All cases submitted.");
 		}
 		catch(Exception e)
 		{
 			throw new GridSweeperException("Could not run experiment", e);	
 		}
 	}
 	
 	/**
 	 * Submits a single experiment case. This means running one job for each
 	 * run of the case (one for each random seed).
 	 * @param expCase The experiment case to run.
 	 * @throws FileNotFoundException If the case directory cannot be found/created.
 	 * @throws DrmaaException If a DRMAA error occurs (in {@link #runCaseRun}).
 	 * @throws IOException If the case XML cannot be written out (in {@link #runCaseRun}).
 	 */
 	public void runCase(ExperimentCase expCase) throws FileNotFoundException, DrmaaException, IOException
 	{
 		String caseSubDir = experiment.getDirectoryNameForCase(expCase);
 		String caseDir = appendPathComponent(expDir, caseSubDir);
 		finer("Case subdirectory: " + caseDir);
 		
 		File caseDirFile = new File(caseDir);
 		caseDirFile.mkdirs();
 		
 		String caseName;
 		if(caseSubDir.equals(""))
 		{
 			caseName = experiment.getName()
 			+ " (" + dateStr + ", " + timeStr + ")";
 		}
 		else
 		{
 			caseName = experiment.getName() + " - "
 			+ caseSubDir + " (" + dateStr + ", " + timeStr + ")";
 		}
 
 		// Write XML
 		String xmlPath = appendPathComponent(caseDir, "case.gscase");
 		ExperimentCaseXMLWriter xmlWriter = new ExperimentCaseXMLWriter(
 				xmlPath, expCase, caseName);
 		xmlWriter.writeXML();
 		
 		if(!caseSubDir.equals(""))
 		{
 			System.err.println(caseSubDir);
 		}
 		
 		// Run each individual run on the grid
 		List<Integer> rngSeeds = expCase.getRngSeeds();
 		for(int i = 0; i < rngSeeds.size(); i++)
 		{
 			CaseRun run = new CaseRun(expCase, caseSubDir, i, rngSeeds.get(i));
 			runCaseRun(run);
 		}
 	}
 	
 	/**
 	 * Submits a single run of an experiment case.
 	 * @param expCase The case to run.
 	 * @param caseDir The full path to where files are stored for this case.
 	 * @param caseSubDir The case directory relative to the experiment results directory.
 	 * @param i The run number for this run.
 	 * @param rngSeed The random seed for this run.
 	 * @throws DrmaaException If a DRMAA error occurs during job submission.
 	 * @throws IOException If the case XML cannot be written out.
 	 */
 	public void runCaseRun(CaseRun run) throws DrmaaException, IOException
 	{
 		ExperimentCase expCase = run.expCase;
 		String caseId = run.caseId;
 		int runNum = run.runNum;
 		int rngSeed = run.rngSeed;
 		
 		String caseDir;
 		if(caseId.equals(""))
 			caseDir = expDir;
 		else
 			caseDir = appendPathComponent(expDir, caseId);
 		
 		Settings settings = experiment.getSettings();
 		
 		String caseRunName;
 		if(caseId.equals(""))
 		{
 			caseRunName = experiment.getName() + " - run " + runNum
 			+ " (" + dateStr + ", " + timeStr + ")";
 		}
 		else
 		{
 			caseRunName = experiment.getName() + " - "
 			+ caseId + " - run " + runNum
 			+ " (" + dateStr + ", " + timeStr + ")";
 		}
 		
 		if(runType == RunType.RUN)
 		{
 			// Write setup file
 			String stdinPath = appendPathComponent(caseDir, ".gsweep_in." + runNum);
 			RunSetup setup = new RunSetup(settings,
 					experiment.getInputFiles(), caseId, expCase.getParameterMap(),
 					runNum, rngSeed, experiment.getOutputFiles());
 			ObjectOutputStream stdinStream = new ObjectOutputStream(new FileOutputStream(stdinPath));
 			stdinStream.writeObject(setup);
 			
 			// Generate job template
 			JobTemplate jt = drmaaSession.createJobTemplate();
 			jt.setJobName(caseRunName);
 			jt.setRemoteCommand(appendPathComponent(root, "bin/grunner"));
 			if(!useFileTransfer) jt.setWorkingDirectory(caseDir);
 			jt.setInputPath(":" + stdinPath);
 			jt.setOutputPath(":" + appendPathComponent(caseDir, ".gsweep_out." + runNum));
 			jt.setErrorPath(":" + appendPathComponent(caseDir, ".gsweep_err." + runNum));
 			jt.setBlockEmail(true);
 			
 			try
 			{
 				jt.setTransferFiles(new FileTransferMode(true, true, true));
 			}
 			catch(DrmaaException e)
 			{
 				// If setTransferFiles isn't supported, we'll hope that the system defaults to
 				// transfering them. This works for SGE.
 			}
 			
 			Properties environment = new Properties();
 			environment.setProperty("GRIDSWEEPER_ROOT", root);
 			
 			String classpath = System.getenv("CLASSPATH");
 			if(classpath != null) environment.setProperty("CLASSPATH", classpath);
 			jt.setJobEnvironment(environment);
 			
 			String jobId = drmaaSession.runJob(jt);
 			
 			caseIdToJobIdMap.put(caseId + "." + runNum, jobId);
 			jobIdToRunMap.put(jobId, run);
 			fine("run in runmap: " + jobIdToRunMap.get(jobId));
 			
 			drmaaSession.deleteJobTemplate(jt);
 			
 			System.err.println("  Submitted run " + runNum
 				+ " (DRMAA job ID " + jobId + ")");
 		}
 		else
 		{
 			System.err.println("  Not submitting run " + runNum
 				+ " (dry run)");
 		}
 		fine("run: " + run);
 	}
 	
 	/**
 	 * Cleans up: for now, just closes the DRMAA session.
 	 * @throws GridSweeperException If the DRMAA {@code exit()} call fails.
 	 */
 	public void finish() throws GridSweeperException
 	{
 		// TODO: provide mechanism to detach this session to the background,
 		// in some way that works even if the user logs out
 		// This is a bit like a daemon, so cf:
 		// http://pezra.barelyenough.org/blog/2005/03/java-daemon/
 		// http://wrapper.tanukisoftware.org/doc/english/prop-daemonize.html
 		
 		if(runType != RunType.RUN) return;
 		
 		try
 		{
 			System.err.println("TODO: detach from console...");
 			
 			System.err.println("Waiting for jobs to complete...");
 			
 			int runCount = jobIdToRunMap.size();
 			for(int i = 0; i < runCount; i++)
 			{
 				JobInfo info = drmaaSession.wait(
 					Session.JOB_IDS_SESSION_ANY, Session.TIMEOUT_WAIT_FOREVER);
 				
 				String jobId = info.getJobId();
 				fine("got wait for job ID " + jobId);
 				fine("jobIdToRunMap: " + jobIdToRunMap.toString());
 				CaseRun run = jobIdToRunMap.get(jobId);
 				fine("run: " + run);
 				
 				String caseId = run.caseId;
 				int runNum = run.runNum;
 				
 				if(caseId.equals(""))
 				{
 					System.err.println("Completed run " + runNum
 						+ " (DRMAA job ID " + jobId + ")");
 				}
 				else
 				{
					System.err.println("Completed " + caseId + ", run " + runNum
						+ " (DRMAA job ID " + jobId + ")");
 				}
				System.err.format("%d of %d complete (%.1lf%%).\n",
						i + 1, runCount, (double)(i + 1)/runCount);
 			}
 			
 			System.err.println("All jobs completed.");
 			
 			sendEmail();
 			
 			// Finish it up
 			drmaaSession.exit();
 		}
 		catch(DrmaaException e)
 		{
 			throw new GridSweeperException("Received exception ending DRMAA session", e);
 		}
 	}
 	
 	private void sendEmail() throws GridSweeperException
 	{
 		String email = experiment.getSettings().getSetting("EmailAddress");
 		
 		if(email == null)
 		{
 			System.err.println("Email address not set. Using username.");
 			email = System.getProperty("user.name");
 		}
 		fine("email address: " + email);
 		
 		String expName = experiment.getName();
 		
 		String subject = expName + " complete";
 		
 		// Construct and write out message
 		String messagePath = appendPathComponent(expDir, ".gsweep_email");
 		StringBuffer message = new StringBuffer();
 		
 		message.append("GridSweeper experiment complete.\n\n");
 		
 		message.append("   Experiment name: " + expName + "\n");
 		
 		message.append("      Submitted at: ");
 		DateFormat format = DateFormat.getDateTimeInstance();
 		message.append(format.format(new Date(cal.getTimeInMillis())));
 		message.append("\n");
 		
 		message.append("      Elapsed time: ");
 		long elapsedMilli = (new Date()).getTime() - cal.getTimeInMillis();
 		elapsedMilli /= 1000;
 		long seconds = elapsedMilli % 60;
 		elapsedMilli /= 60;
 		long minutes = elapsedMilli % 60;
 		elapsedMilli /= 60;
 		long hours = elapsedMilli;
 		message.append("" + hours + "h" + minutes + "m" + seconds + "s");
 		message.append("\n");
 		
 		try
 		{
 			FileWriter fw = new FileWriter(messagePath);
 			fw.write(message.toString());
 			fw.close();
 		}
 		catch(IOException e)
 		{
 			throw new GridSweeperException("Could not write email file.");
 		}
 		
 		String command = appendPathComponent(root, "bin/gsmail");
 		
 		String[] commandAndArgs = {command, subject, email, messagePath};
 		
 		try
 		{
 			Runtime.getRuntime().exec(commandAndArgs);
 		}
 		catch (IOException e)
 		{
 			throw new GridSweeperException("Could not send email.", e);
 		}
 	}
 
 	public void setRunType(RunType runType)
 	{
 		this.runType = runType;
 	}
 	
 	public void setRoot(String root)
 	{
 		this.root = root;
 	}
 	
 	public void setExperiment(Experiment experiment)
 	{
 		this.experiment = experiment;
 	}
 }
