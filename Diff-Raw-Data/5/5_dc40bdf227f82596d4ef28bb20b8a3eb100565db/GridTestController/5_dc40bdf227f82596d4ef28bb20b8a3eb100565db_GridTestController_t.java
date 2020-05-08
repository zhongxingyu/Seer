 package org.vpac.grisu.clients.gridTests;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.URL;
 import java.util.Arrays;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.TimeUnit;
 
 import jline.ConsoleReader;
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.log4j.FileAppender;
 import org.apache.log4j.Level;
 import org.apache.log4j.Logger;
 import org.apache.log4j.SimpleLayout;
 import org.vpac.grisu.clients.gridTests.testElements.GridTestElement;
 import org.vpac.grisu.control.JobConstants;
 import org.vpac.grisu.control.ServiceInterface;
 import org.vpac.grisu.control.exceptions.ServiceInterfaceException;
 import org.vpac.grisu.frontend.control.clientexceptions.MdsInformationException;
 import org.vpac.grisu.frontend.control.login.LoginManager;
 import org.vpac.grisu.frontend.control.login.LoginParams;
 import org.vpac.grisu.model.GrisuRegistry;
 import org.vpac.grisu.model.GrisuRegistryManager;
 import org.vpac.grisu.settings.Environment;
 import org.vpac.grisu.utils.GrisuPluginFilenameFilter;
 import org.vpac.security.light.plainProxy.LocalProxy;
 
 import au.org.arcs.jcommons.constants.ArcsEnvironment;
 import au.org.arcs.jcommons.dependencies.ClasspathHacker;
 import au.org.arcs.jcommons.dependencies.Dependency;
 import au.org.arcs.jcommons.dependencies.DependencyManager;
 
 public class GridTestController {
 
 	/**
 	 * @param args
 	 * @throws ServiceInterfaceException
 	 * @throws MdsInformationException
 	 */
 	public static void main(String[] args) throws ServiceInterfaceException,
 			MdsInformationException {
 
 		String name = GridTestController.class.getName();
 		name = name.replace('.', '/') + ".class";
 		URL url = GridTestController.class.getClassLoader().getResource(name);
 		String path = url.getPath();
 		// System.out.println("Executable path: "+path);
 		String baseDir = null;
 		if (url.toString().startsWith("jar:")) {
 			baseDir = path.toString().substring(path.indexOf(":") + 1,
 					path.indexOf(".jar!"));
 			baseDir = baseDir.substring(0, baseDir.lastIndexOf("/"));
 		} else {
 			baseDir = null;
 		}
 
 		System.out.println("Using directory: " + baseDir);
 		
 		GridTestController gtc = new GridTestController(args, baseDir);
 
 		gtc.start();
		
		System.exit(0);
 
 	}
 
 	private final String grisu_base_directory;
 
 	private final File grid_tests_directory;
 	private final ExecutorService submitJobExecutor;
 
 	private final ExecutorService processJobExecutor;
 	private LinkedList<Thread> createAndSubmitJobThreads = new LinkedList<Thread>();
 
 	private Map<String, Thread> checkAndKillJobThreads = new HashMap<String, Thread>();
 	private Map<String, GridTestElement> gridTestElements = new HashMap<String, GridTestElement>();
 
 	private List<GridTestElement> finishedElements = new LinkedList<GridTestElement>();
 
 	private ServiceInterface serviceInterface;
 
 	private final GrisuRegistry registry;
 
 	private String[] gridtestNames;
 	private final String[] fqans;
 	private String output = null;
 	private String[] excludes;
 	private String[] includes;
 
 	private Date timeoutDate;
 	private final int timeout;
 
 	private int sameSubloc = 1;
 
 	private List<OutputModule> outputModules = new LinkedList<OutputModule>();
 
 	public GridTestController(String[] args, String grisu_base_directory_param) {
 
 		if (StringUtils.isBlank(grisu_base_directory_param)) {
 			this.grisu_base_directory = System.getProperty("user.home")
 					+ File.separator + "grisu-grid-tests";
 		} else {
 			this.grisu_base_directory = grisu_base_directory_param;
 		}
 		
 		// logging stuff
 		SimpleLayout layout = new SimpleLayout();
 		try {
 			FileAppender fa = new FileAppender(layout, this.grisu_base_directory+File.separator+"grisu-tests.debug", false);
 			Logger logger = Logger.getRootLogger();
 			logger.addAppender(fa);
 			logger.setLevel((Level) Level.INFO);
 
 			
 		} catch (IOException e) {
 			e.printStackTrace();
 			System.exit(1);
 		}
 
 		
 
 
 		Environment.setGrisuDirectory(this.grisu_base_directory);
 
 		Map<Dependency, String> dependencies = new HashMap<Dependency, String>();
 
 		dependencies.put(Dependency.BOUNCYCASTLE, "jdk15-143");
 
 		DependencyManager.addDependencies(dependencies, ArcsEnvironment
 				.getArcsCommonJavaLibDirectory());
 
 		ClasspathHacker.initFolder(Environment.getGrisuPluginDirectory(),
 				new GrisuPluginFilenameFilter());
 
 //		// try to setup hibernate for local tests if a local Backend is used
 //		try {
 //
 //			// HibernateSessionFactory
 //			// .setCustomHibernateConfigFile(this.grisu_base_directory
 //			// + File.separator + "grid-tests-hibernate-file.cfg.xml");
 //
 //			Class hsfc = Class
 //					.forName("org.vpac.grisu.backend.hibernate.HibernateSessionFactory");
 //			Method method = hsfc.getMethod("setCustomHibernateConfigFile",
 //					String.class);
 //
 //			method.invoke(null, this.grisu_base_directory + File.separator
 //					+ "grid-tests-hibernate-file.cfg.xml");
 //
 //		} catch (Exception e) {
 //			// doesn't really matter
 //		}
 
 		grid_tests_directory = new File(this.grisu_base_directory, "tests");
 
 		output = grid_tests_directory + File.separator + "testResults_"
 				+ new Date().getTime() + ".log";
 
 		GridTestCommandlineOptions options = new GridTestCommandlineOptions(
 				args);
 
 		int threads = options.getSimultaneousThreads();
 		submitJobExecutor = Executors.newFixedThreadPool(threads);
 		processJobExecutor = Executors.newFixedThreadPool(threads);
 
 		if (options.getMyproxyUsername() != null
 				&& options.getMyproxyUsername().length() != 0) {
 			try {
 				ConsoleReader consoleReader = new ConsoleReader();
 				char[] password = consoleReader.readLine(
 						"Please enter your myproxy password: ",
 						new Character('*')).toCharArray();
 
 				LoginParams loginParams = new LoginParams(
 				// "http://localhost:8080/grisu-ws/services/grisu",
 						// "https://ngportaldev.vpac.org/grisu-ws/services/grisu",
 						"Local", options.getMyproxyUsername(), password);
 
 				serviceInterface = LoginManager.login(null, null, null, null,
 						loginParams);
 			} catch (Exception e) {
 				System.out.println("Could not login: "
 						+ e.getLocalizedMessage());
 				System.exit(1);
 			}
 		} else {
 			// trying to get local proxy
 
 			LoginParams loginParams = new LoginParams("Local", null, null,
 					"myproxy2.arcs.org.au", "443");
 			try {
 				serviceInterface = LoginManager.login(LocalProxy
 						.loadGlobusCredential(), null, null, null, loginParams);
 			} catch (Exception e) {
 				System.out.println("Could not login: "
 						+ e.getLocalizedMessage());
 				System.exit(1);
 			}
 		}
 
 		registry = GrisuRegistryManager.getDefault(this.serviceInterface);
 
 		if (options.getFqans().length == 0) {
 			fqans = serviceInterface.getFqans().asArray();
 		} else {
 			fqans = options.getFqans();
 		}
 
 		if (options.getOutput() != null && options.getOutput().length() > 0) {
 			output = options.getOutput();
 		}
 
 		timeout = options.getTimeout();
 
 		sameSubloc = options.getSameSubmissionLocation();
 
 		if (options.listTests()) {
 
 			List<GridTestInfo> infos = new LinkedList<GridTestInfo>();
 
 			List<GridTestInfo> externalinfos = GridExternalTestInfoImpl
 					.generateGridTestInfos(this, new String[] {}, fqans);
 			List<GridTestInfo> internalinfos = GridInternalTestInfoImpl
 					.generateGridTestInfos(this, new String[] {}, fqans);
 
 			infos.addAll(externalinfos);
 			infos.addAll(internalinfos);
 
 			System.out.println("Available tests: ");
 			for (GridTestInfo info : infos) {
 				System.out.println("Testname: " + info.getTestname());
 				System.out.println("\tApplication: "
 						+ info.getApplicationName());
 				System.out.println("\tDescription: " + info.getDescription());
 				System.out.println("\tTest elements:");
 				try {
 					for (GridTestElement el : info
 							.generateAllGridTestElements()) {
 						System.out.println("\t\t" + el.toString());
 					}
 				} catch (MdsInformationException e) {
 					System.err.println("Error while listing test elements: "
 							+ e.getLocalizedMessage());
 					System.err.println("Exiting...");
 					System.exit(1);
 				}
 
 				System.out.println();
 			}
 
 			System.exit(0);
 		}
 
 		gridtestNames = options.getGridTestNames();
 		Arrays.sort(gridtestNames);
 
 		excludes = options.getExcludes();
 		includes = options.getIncludes();
 
 		outputModules.add(new LogFileOutputModule(output));
//		outputModules.add(new XmlRpcOutputModule());
 
 	}
 
 	// public GridTestController(ServiceInterface si, String[] applications,
 	// String fqan) {
 	// this.serviceInterface = si;
 	// registry = GrisuRegistry.getDefault(this.serviceInterface);
 	// this.fqan = fqan;
 	// }
 
 	public void createAndSubmitAllJobs() {
 
 		for (Thread thread : createAndSubmitJobThreads) {
 			submitJobExecutor.execute(thread);
 		}
 
 		submitJobExecutor.shutdown();
 
 		try {
 			submitJobExecutor.awaitTermination(3600, TimeUnit.SECONDS);
 		} catch (InterruptedException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 	}
 
 	private Thread createCheckAndKillJobThread(final GridTestElement gte) {
 
 		Thread thread = new Thread() {
 			public void run() {
 				System.out
 						.println("Checking job success for job submitted to: "
 								+ gte.getSubmissionLocation());
 				gte.checkWhetherJobDidWhatItWasSupposedToDo();
 				if (!gte.failed()) {
 					System.out.println("Job submitted to "
 							+ gte.getSubmissionLocation()
 							+ " completed successfully.");
 				}
 				System.out.println("Killing and cleaning job submitted to: "
 						+ gte.getSubmissionLocation());
 				gte.killAndClean();
 				if (!gte.failed()) {
 					System.out
 							.println("Killing and cleaning of job submitted to "
 									+ gte.getSubmissionLocation()
 									+ " was successful.");
 				}
 
 				gte.finishTest();
 				writeGridTestElementLog(gte);
 
 			}
 		};
 
 		return thread;
 
 	}
 
 	// public String getFqan() {
 	// return this.fqan;
 	// }
 
 	private Thread createCreateAndSubmitJobThread(final GridTestElement gte) {
 
 		Thread thread = new Thread() {
 			public void run() {
 				System.out.println("Creating job for subLoc: "
 						+ gte.getSubmissionLocation());
 				gte.createJob(gte.getFqan());
 				System.out.println("Submitting job for subLoc: "
 						+ gte.getSubmissionLocation());
 				gte.submitJob();
 				if (gte.failed()) {
 					System.out
 							.println("Submission to "
 									+ gte.getSubmissionLocation()
 									+ " finished: Failed");
 				} else {
 					System.out.println("Submission to "
 							+ gte.getSubmissionLocation()
 							+ " finished: Success");
 				}
 			}
 		};
 
 		return thread;
 
 	}
 
 	public void createJobsJobThreads() throws MdsInformationException {
 
 		List<GridTestInfo> externalinfos = GridExternalTestInfoImpl
 				.generateGridTestInfos(this, gridtestNames, fqans);
 		List<GridTestInfo> internalinfos = GridInternalTestInfoImpl
 				.generateGridTestInfos(this, gridtestNames, fqans);
 
 		List<GridTestInfo> infos = new LinkedList<GridTestInfo>();
 		infos.addAll(externalinfos);
 		infos.addAll(internalinfos);
 
 		for (GridTestInfo info : infos) {
 
 			for (GridTestElement el : info.generateAllGridTestElements()) {
 
 				boolean ignoreThisElement = false;
 				if (includes.length == 0) {
 					for (String filter : excludes) {
 						if (el.getSubmissionLocation().indexOf(filter) >= 0) {
 							ignoreThisElement = true;
 						}
 					}
 				} else {
 					for (String filter : includes) {
 						if (el.getSubmissionLocation().indexOf(filter) < 0) {
 							ignoreThisElement = true;
 						}
 					}
 				}
 
 				if (ignoreThisElement) {
 					continue;
 				}
 
 				System.out
 						.println("Adding grid test element: " + el.toString());
 
 				gridTestElements.put(el.getTestId(), el);
 
 				Thread createJobThread = createCreateAndSubmitJobThread(el);
 				createAndSubmitJobThreads.add(createJobThread);
 
 			}
 
 		}
 
 	}
 
 	public File getGridTestDirectory() {
 		return grid_tests_directory;
 	}
 
 	public int getSameSubmissionLocation() {
 		return sameSubloc;
 	}
 
 	public ServiceInterface getServiceInterface() {
 		return serviceInterface;
 	}
 
 	public void start() {
 
 		try {
 			createJobsJobThreads();
 		} catch (MdsInformationException e) {
 
 			System.out.println("Could not create all necessary jobs: "
 					+ e.getLocalizedMessage() + ". Exiting...");
 			System.exit(1);
 
 		}
 
 		StringBuffer setup = OutputModuleHelpers
 				.createTestSetupString(gridTestElements.values());
 
 		for (OutputModule module : outputModules) {
 			module.writeTestsSetup(setup.toString());
 		}
 		System.out.println(setup.toString());
 
 		createAndSubmitAllJobs();
 
 		Calendar cal = Calendar.getInstance();
 		cal.add(Calendar.MINUTE, timeout);
 		timeoutDate = cal.getTime();
 
 		System.out.println("All unfinished jobs will be killed: "
 				+ timeoutDate.toString());
 
 		for (GridTestElement gte : gridTestElements.values()) {
 			//
 			// if (gte.failed()) {
 			// finishedElements.add(gte);
 			// } else {
 			checkAndKillJobThreads.put(gte.getTestId(),
 					createCheckAndKillJobThread(gte));
 			// }
 		}
 
 		// // remove failed gtes from map
 		// for (GridTestElement gte : finishedElements) {
 		// gridTestElements.remove(gte.getId());
 		// }
 
 		waitForJobsToFinishAndCheckAndKillThem();
 
 		writeStatistics();
 
 	}
 
 	public void waitForJobsToFinishAndCheckAndKillThem() {
 
 		while (gridTestElements.size() > 0) {
 
 			if (new Date().after(timeoutDate)) {
 
 				for (GridTestElement gte : gridTestElements.values()) {
 					System.out.println("Interrupting not finished job: "
 							+ gte.toString());
 					if (!gte.failed()
 							&& gte.getJobStatus(true) < JobConstants.FINISHED_EITHER_WAY) {
 						gte.interruptRunningJob();
 					}
 				}
 			}
 
 			List<GridTestElement> batchOfRecentlyFinishedJobs = new LinkedList<GridTestElement>();
 
 			for (GridTestElement gte : gridTestElements.values()) {
 
 				if (gte.getJobStatus(true) >= JobConstants.FINISHED_EITHER_WAY
 						|| gte.getJobStatus(false) <= JobConstants.READY_TO_SUBMIT
 						|| gte.failed()) {
 					batchOfRecentlyFinishedJobs.add(gte);
 				}
 			}
 
 			for (GridTestElement gte : batchOfRecentlyFinishedJobs) {
 				gridTestElements.remove(gte.getTestId());
 				// gte.finishTest();
 				finishedElements.add(gte);
 				processJobExecutor.execute(checkAndKillJobThreads.get(gte
 						.getTestId()));
 			}
 
 			if (gridTestElements.size() == 0) {
 				break;
 			}
 
 			StringBuffer remainingSubLocs = new StringBuffer();
 			for (GridTestElement gte : gridTestElements.values()) {
 				remainingSubLocs.append("\t" + gte.toString() + "\n");
 			}
 			System.out.println("\nStill " + gridTestElements.size()
 					+ " jobs not finished:");
 			System.out.println(remainingSubLocs.toString());
 
 			System.out.println("Sleeping for another 30 seconds...");
 			System.out.println("All remaining jobs will be killed: "
 					+ timeoutDate.toString() + "\n");
 
 			try {
 				Thread.sleep(30000);
 			} catch (InterruptedException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 
 		}
 
 		processJobExecutor.shutdown();
 
 		try {
 			processJobExecutor.awaitTermination(6000, TimeUnit.SECONDS);
 		} catch (InterruptedException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 	}
 
 	public synchronized void writeGridTestElementLog(GridTestElement gte) {
 
 		for (OutputModule module : outputModules) {
 			System.out.println("Writing output using: "
 					+ module.getClass().getName());
 			module.writeTestElement(gte);
 		}
 
 	}
 
 	public void writeStatistics() {
 
 		StringBuffer statistics = OutputModuleHelpers
 				.createStatisticsString(finishedElements);
 
 		for (OutputModule module : outputModules) {
 			module.writeTestsStatistic(statistics.toString());
 		}
 
 		System.out.println(statistics.toString());
 
 	}
 
 }
