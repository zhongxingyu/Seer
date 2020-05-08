 package jp.ac.osaka_u.ist.sdl.ectec.detector;
 
 import java.util.Collection;
 import java.util.Map;
 import java.util.SortedMap;
 
 import jp.ac.osaka_u.ist.sdl.ectec.db.DBConnectionManager;
 import jp.ac.osaka_u.ist.sdl.ectec.db.DBMaker;
 import jp.ac.osaka_u.ist.sdl.ectec.db.data.DBCommitInfo;
 import jp.ac.osaka_u.ist.sdl.ectec.db.data.DBFileInfo;
 import jp.ac.osaka_u.ist.sdl.ectec.db.data.DBRevisionInfo;
 import jp.ac.osaka_u.ist.sdl.ectec.detector.clonedetector.BlockBasedCloneIdentifier;
 import jp.ac.osaka_u.ist.sdl.ectec.detector.clonelinker.CloneSetLinkIdentifier;
 import jp.ac.osaka_u.ist.sdl.ectec.detector.filedetector.ChangedFilesIdentifier;
 import jp.ac.osaka_u.ist.sdl.ectec.detector.genealogydetector.CloneGenealogyIdentifier;
 import jp.ac.osaka_u.ist.sdl.ectec.detector.genealogydetector.FragmentGenealogyIdentifier;
 import jp.ac.osaka_u.ist.sdl.ectec.detector.linker.CodeFragmentLinkIdentifier;
 import jp.ac.osaka_u.ist.sdl.ectec.detector.sourceanalyzer.CodeFragmentIdentifier;
 import jp.ac.osaka_u.ist.sdl.ectec.detector.sourceanalyzer.hash.DefaultHashCalculator;
 import jp.ac.osaka_u.ist.sdl.ectec.detector.sourceanalyzer.hash.IHashCalculator;
 import jp.ac.osaka_u.ist.sdl.ectec.detector.sourceanalyzer.normalizer.NormalizerCreator;
 import jp.ac.osaka_u.ist.sdl.ectec.detector.vcs.RepositoryManagerManager;
 import jp.ac.osaka_u.ist.sdl.ectec.settings.Constants;
 import jp.ac.osaka_u.ist.sdl.ectec.settings.MessagePrinter;
 
 /**
  * The main class of the analyzer
  * 
  * @author k-hotta
  * 
  */
 public class DetectorMain {
 
 	/**
 	 * the manager of the repository manager
 	 */
 	private static RepositoryManagerManager repositoryManagerManager = null;
 
 	/**
 	 * the manager of the db
 	 */
 	private static DBConnectionManager dbManager = null;
 
 	public static void main(String[] args) {
 		try {
 			final DetectorSettings settings = DetectorSettings.parseArgs(args);
 
 			preprocess(settings);
 
 			main(settings);
 
 			postprocess();
 
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	/*
 	 * preprocessors follow
 	 */
 
 	private static void preprocess(final DetectorSettings settings)
 			throws Exception {
 		// set the level of verbose output
 		MessagePrinter.setLevel(settings.getVerboseLevel());
 
 		// print for starting operations
 		initialPrint(settings);
 
 		// initialize the repository
 		initializeRepository(settings);
 
 		// initialize the db
 		initializeDb(settings);
 
 		// detect start/end revisions if they are not specified
 		resetRevisionIdentifiers(settings);
 	}
 
 	private static void initialPrint(final DetectorSettings settings) {
 		MessagePrinter.stronglyPrint("operations start");
 		MessagePrinter.print(" with the configurations below");
 		MessagePrinter.stronglyPrintln();
 
 		MessagePrinter.println("\ttarget repository: "
 				+ settings.getRepositoryPath());
 		MessagePrinter.println("\toutput database file: "
 				+ settings.getDbPath());
 		MessagePrinter
 				.println("\taddtitional path: "
 						+ ((settings.getAdditionalPath() == null) ? "nothing is specified"
 								: settings.getAdditionalPath()));
 		MessagePrinter.println("\ttarget language: "
 				+ settings.getLanguage().toString());
 		MessagePrinter.println("\tthe number of threads: "
 				+ settings.getThreads());
 		MessagePrinter.println("\tuser name for accessing the repository: "
 				+ ((settings.getUserName() == null) ? "nothing is specified"
 						: settings.getUserName()));
 		MessagePrinter.println("\tpassword for accessing the repository: "
 				+ ((settings.getPasswd() == null) ? "nothing is specified"
 						: "*********"));
 		MessagePrinter
 				.println("\tstart revision: "
						+ ((settings.getStartRevisionIdentifier() == null) ? "nothing is specified, will start at the earliest revision"
 								: settings.getStartRevisionIdentifier()));
 		MessagePrinter
 				.println("\tend revision: "
						+ ((settings.getEndRevisionIdentifier() == null) ? "nothing is specified, will end at the latest revision"
 								: settings.getStartRevisionIdentifier()));
 		MessagePrinter.println("\tversion control system targeted: "
 				+ settings.getVersionControlSystem().toString());
 		MessagePrinter
 				.println("\tthe loaded properties file: "
 						+ ((settings.getPropertiesFilePath() == null) ? "the default one"
 								: settings.getPropertiesFilePath()));
 		MessagePrinter.println("\toverwrite the db if it already exists: "
 				+ ((settings.isOverwriteDb()) ? "yes" : "no"));
 		MessagePrinter.println("\tthe maximum nuber of batched statements: "
 				+ settings.getMaxBatchCount());
 		MessagePrinter
 				.println("\thow to calculate hash values for clone detection: "
 						+ settings.getCloneHashCalculateMode().toString());
 		MessagePrinter.println("\thow to calculate similarity of CRDs: "
 				+ settings.getCrdSimilarityMode().toString());
 		MessagePrinter.println("\thow to link code fragments: "
 				+ settings.getFragmentLinkMode().toString());
 		MessagePrinter.println("\tthe granularity of the analysis: "
 				+ settings.getGranularity().toString());
 		MessagePrinter
 				.println("\tthe threshold for sizes of clones to be detected: "
 						+ settings.getCloneSizeThreshold());
 
 		MessagePrinter.stronglyPrintln();
 	}
 
 	private static void initializeRepository(final DetectorSettings settings)
 			throws Exception {
 		MessagePrinter.stronglyPrintln("initializing the repository ... ");
 		repositoryManagerManager = new RepositoryManagerManager(
 				settings.getVersionControlSystem(),
 				settings.getRepositoryPath(), settings.getUserName(),
 				settings.getPasswd(), settings.getAdditionalPath());
 		MessagePrinter.stronglyPrintln("\tOK");
 		MessagePrinter.stronglyPrintln();
 	}
 
 	private static void initializeDb(final DetectorSettings settings)
 			throws Exception {
 		MessagePrinter.stronglyPrintln("initializing the database ... ");
 		dbManager = new DBConnectionManager(settings.getDbPath(),
 				settings.getMaxBatchCount());
 		final DBMaker dbMaker = new DBMaker(dbManager);
 		dbMaker.makeDb(settings.isOverwriteDb());
 		MessagePrinter.stronglyPrintln("\tOK");
 		MessagePrinter.stronglyPrintln();
 	}
 
 	private static void resetRevisionIdentifiers(final DetectorSettings settings)
 			throws Exception {
 		if (settings.getStartRevisionIdentifier() == null) {
 			settings.setStartRevisionIdentifier(repositoryManagerManager
 					.getRepositoryManager().getFirstRevision());
 		}
 		if (settings.getEndRevisionIdentifier() == null) {
 			settings.setEndRevisionIdentifier(repositoryManagerManager
 					.getRepositoryManager().getLatestRevision());
 		}
 	}
 
 	/*
 	 * preprocessors end
 	 */
 
 	/*
 	 * the main process follows
 	 */
 
 	/**
 	 * the main function
 	 * 
 	 * @param settings
 	 * @throws Exception
 	 */
 	private static void main(final DetectorSettings settings) throws Exception {
 		final long start = System.nanoTime();
 
 		MessagePrinter.stronglyPrintln("start main operations");
 		MessagePrinter.stronglyPrintln("\tfrom revision "
 				+ settings.getStartRevisionIdentifier());
 		MessagePrinter.stronglyPrintln("\tto revision "
 				+ settings.getEndRevisionIdentifier());
 		MessagePrinter.stronglyPrintln();
 
 		detectAndRegisterTargetRevisions(settings);
 
 		final long l1 = System.nanoTime();
 
 		detectAndRegisterFiles(settings);
 
 		final long l2 = System.nanoTime();
 
 		detectAndRegisterFragments(settings);
 
 		final long l3 = System.nanoTime();
 
 		detectAndRegisterFragmentLinks(settings);
 
 		final long l4 = System.nanoTime();
 
 		detectAndRegisterFragmentGenealogies(settings);
 
 		final long l5 = System.nanoTime();
 
 		detectAndRegisterClones(settings);
 
 		final long l6 = System.nanoTime();
 
 		detectAndRegisterCloneLinks(settings);
 
 		final long l7 = System.nanoTime();
 
 		detectAndRegisterCloneGenealogies(settings);
 
 		final long end = System.nanoTime();
 
 		final long detectingRevisions = (l1 - start) / 1000000;
 		final long detectingFiles = (l2 - l1) / 1000000;
 		final long detectingFragments = (l3 - l2) / 1000000;
 		final long detectingFragmentLinks = (l4 - l3) / 1000000;
 		final long detectingFragmentGenealogies = (l5 - l4) / 1000000;
 		final long detectingClones = (l6 - l5) / 1000000;
 		final long detectingCloneLinks = (l7 - l6) / 1000000;
 		final long detectingCloneGenealogies = (end - l7) / 1000000;
 		final long total = (end - start) / 1000000000;
 
 		MessagePrinter
 				.stronglyPrintln("all the main processes have been finished");
 		MessagePrinter.stronglyPrintln("\tthe elapsed time");
 		MessagePrinter.stronglyPrintln("\t\tdetecting revisions : "
 				+ detectingRevisions + " [ms]");
 		MessagePrinter.stronglyPrintln("\t\tdetecting files : "
 				+ detectingFiles + " [ms]");
 		MessagePrinter.stronglyPrintln("\t\tdetecting fragments : "
 				+ detectingFragments + " [ms]");
 		MessagePrinter.stronglyPrintln("\t\tdetecting fragment links : "
 				+ detectingFragmentLinks + " [ms]");
 		MessagePrinter.stronglyPrintln("\t\tdetecting fragment genealogies : "
 				+ detectingFragmentGenealogies + " [ms]");
 		MessagePrinter.stronglyPrintln("\t\tdetecting clones : "
 				+ detectingClones + " [ms]");
 		MessagePrinter.stronglyPrintln("\t\tdetecting clone links : "
 				+ detectingCloneLinks + " [ms]");
 		MessagePrinter.stronglyPrintln("\t\tdetecting clone genealogies : "
 				+ detectingCloneGenealogies + " [ms]");
 		MessagePrinter.stronglyPrintln("\t\tTOTAL : " + total + " [s]");
 	}
 
 	private static void detectAndRegisterTargetRevisions(
 			final DetectorSettings settings) throws Exception {
 		MessagePrinter.stronglyPrintln("detecting target revisions ... ");
 
 		final RevisionIdentifier identifier = new RevisionIdentifier(
 				repositoryManagerManager.getRepositoryManager()
 						.getTargetRevisionDetector(),
 				dbManager.getRevisionRegisterer(),
 				dbManager.getCommitRegisterer());
 		identifier.detectAndRegister(settings.getLanguage(),
 				settings.getStartRevisionIdentifier(),
 				settings.getEndRevisionIdentifier());
 
 		MessagePrinter.stronglyPrintln();
 
 	}
 
 	private static void detectAndRegisterFiles(final DetectorSettings settings)
 			throws Exception {
 		final Map<Long, DBCommitInfo> commits = dbManager.getCommitRetriever()
 				.retrieveAll();
 		final ChangedFilesIdentifier identifier = new ChangedFilesIdentifier(
 				repositoryManagerManager.getRepositoryManager(),
 				dbManager.getFileRegisterer(), settings.getLanguage(),
 				settings.getThreads());
 		identifier.detectAndRegister(commits);
 	}
 
 	private static void detectAndRegisterFragments(
 			final DetectorSettings settings) throws Exception {
 		MessagePrinter
 				.stronglyPrintln("detecting and registering code fragments and their crds ... ");
 
 		final Collection<DBFileInfo> files = dbManager.getFileRetriever()
 				.retrieveAll().values();
 
 		final Collection<DBRevisionInfo> revisions = dbManager
 				.getRevisionRetriever().retrieveAll().values();
 
 		final NormalizerCreator blockAnalyzerCreator = new NormalizerCreator(
 				settings.getCloneHashCalculateMode());
 
 		final IHashCalculator hashCalculator = new DefaultHashCalculator();
 
 		final CodeFragmentIdentifier identifier = new CodeFragmentIdentifier(
 				files, revisions, settings.getThreads(),
 				dbManager.getCrdRegisterer(),
 				dbManager.getFragmentRegisterer(),
 				Constants.MAX_ELEMENTS_COUNT,
 				repositoryManagerManager.getRepositoryManager(),
 				settings.getGranularity(), blockAnalyzerCreator, hashCalculator);
 
 		identifier.run();
 
 		MessagePrinter.stronglyPrintln();
 	}
 
 	private static void detectAndRegisterFragmentLinks(
 			final DetectorSettings settings) throws Exception {
 		MessagePrinter
 				.stronglyPrintln("detecting and registering links of code fragments ... ");
 
 		final Map<Long, DBCommitInfo> commits = dbManager.getCommitRetriever()
 				.retrieveAll();
 
 		final CodeFragmentLinkIdentifier identifier = new CodeFragmentLinkIdentifier(
 				commits, settings.getThreads(),
 				dbManager.getFragmentLinkRegisterer(),
 				dbManager.getFragmentRetriever(), dbManager.getCrdRetriever(),
 				settings.getFragmentLinkMode().getLinker(),
 				settings.getSimilarityThreshold(), settings
 						.getCrdSimilarityMode().getCalculator(),
 				Constants.MAX_ELEMENTS_COUNT);
 		identifier.run();
 
 		MessagePrinter.stronglyPrintln();
 	}
 
 	private static void detectAndRegisterFragmentGenealogies(
 			final DetectorSettings settings) throws Exception {
 		MessagePrinter
 				.stronglyPrintln("detecting and registering genealogies of code fragments ... ");
 
 		final Map<Long, DBRevisionInfo> targetRevisions = dbManager
 				.getRevisionRetriever().retrieveAll();
 
 		final FragmentGenealogyIdentifier identifier = new FragmentGenealogyIdentifier(
 				targetRevisions, settings.getThreads(),
 				dbManager.getFragmentRetriever(),
 				dbManager.getFragmentLinkRetriever(),
 				dbManager.getFragmentGenealogyRegisterer());
 		identifier.detectAndRegister();
 
 		MessagePrinter.stronglyPrintln();
 	}
 
 	private static void detectAndRegisterClones(final DetectorSettings settings)
 			throws Exception {
 		MessagePrinter
 				.stronglyPrintln("detecting and registering clone sets ... ");
 
 		final Map<Long, DBRevisionInfo> targetRevisions = dbManager
 				.getRevisionRetriever().retrieveAll();
 
 		final BlockBasedCloneIdentifier identifier = new BlockBasedCloneIdentifier(
 				targetRevisions, settings.getThreads(),
 				dbManager.getFragmentRetriever(),
 				dbManager.getCloneRegisterer(), Constants.MAX_ELEMENTS_COUNT,
 				settings.getCloneSizeThreshold());
 		identifier.run();
 
 		MessagePrinter.stronglyPrintln();
 	}
 
 	private static void detectAndRegisterCloneLinks(
 			final DetectorSettings settings) throws Exception {
 		MessagePrinter
 				.stronglyPrintln("detecting and registering links of clone sets ... ");
 
 		final Map<Long, DBCommitInfo> commits = dbManager.getCommitRetriever()
 				.retrieveAll();
 
 		final CloneSetLinkIdentifier identifier = new CloneSetLinkIdentifier(
 				commits, settings.getThreads(),
 				dbManager.getFragmentLinkRetriever(),
 				dbManager.getCloneRetriever(),
 				dbManager.getCloneLinkRegisterer(),
 				Constants.MAX_ELEMENTS_COUNT);
 		identifier.run();
 
 		MessagePrinter.stronglyPrintln();
 	}
 
 	private static void detectAndRegisterCloneGenealogies(
 			final DetectorSettings settings) throws Exception {
 		MessagePrinter
 				.stronglyPrintln("detecting and registering genealogies of clones ... ");
 
 		final SortedMap<Long, DBRevisionInfo> targetRevisions = dbManager
 				.getRevisionRetriever().retrieveAll();
 		final long lastRevisionId = targetRevisions.lastKey();
 
 		final CloneGenealogyIdentifier identifier = new CloneGenealogyIdentifier(
 				targetRevisions, settings.getThreads(),
 				dbManager.getCloneRetriever(),
 				dbManager.getCloneLinkRetriever(),
 				dbManager.getCloneGenealogyRegisterer(), lastRevisionId);
 		identifier.detectAndRegister();
 
 		MessagePrinter.stronglyPrintln();
 	}
 
 	/*
 	 * main process ends
 	 */
 
 	/*
 	 * post processors follow
 	 */
 
 	private static void postprocess() {
 		if (dbManager != null) {
 			dbManager.close();
 		}
 	}
 
 }
