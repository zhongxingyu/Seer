 package jp.ac.osaka_u.ist.sdl.ectec.analyzer.sourceanalyzer;
 
 import java.util.Collection;
 import java.util.Map;
 import java.util.Set;
 import java.util.TreeMap;
 import java.util.TreeSet;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.ConcurrentMap;
 import java.util.concurrent.atomic.AtomicInteger;
 
 import jp.ac.osaka_u.ist.sdl.ectec.analyzer.sourceanalyzer.hash.IHashCalculator;
 import jp.ac.osaka_u.ist.sdl.ectec.analyzer.sourceanalyzer.normalizer.NormalizerCreator;
 import jp.ac.osaka_u.ist.sdl.ectec.analyzer.vcs.IRepositoryManager;
 import jp.ac.osaka_u.ist.sdl.ectec.data.CRD;
 import jp.ac.osaka_u.ist.sdl.ectec.data.CodeFragmentInfo;
 import jp.ac.osaka_u.ist.sdl.ectec.data.FileInfo;
 import jp.ac.osaka_u.ist.sdl.ectec.data.RevisionInfo;
 import jp.ac.osaka_u.ist.sdl.ectec.data.registerer.CRDRegisterer;
 import jp.ac.osaka_u.ist.sdl.ectec.data.registerer.CodeFragmentRegisterer;
 import jp.ac.osaka_u.ist.sdl.ectec.settings.AnalyzeGranularity;
 import jp.ac.osaka_u.ist.sdl.ectec.settings.MessagePrinter;
 
 import org.eclipse.jdt.core.dom.CompilationUnit;
 
 /**
  * A class for managing threads that detects code fragments
  * 
  * @author k-hotta
  * 
  */
 public class CodeFragmentIdentifier {
 
 	/**
 	 * target files
 	 */
 	private final Collection<FileInfo> targetFiles;
 
 	/**
 	 * target revisions
 	 */
 	private final Collection<RevisionInfo> revisions;
 
 	/**
 	 * the number of threads
 	 */
 	private final int threadsCount;
 
 	/**
 	 * the registerer for crds
 	 */
 	private final CRDRegisterer crdRegisterer;
 
 	/**
 	 * the registerer for fragments
 	 */
 	private final CodeFragmentRegisterer fragmentRegisterer;
 
 	/**
 	 * the threshold for storing elements
 	 */
 	private final int maxElementsCount;
 
 	/**
 	 * the repository manager
 	 */
 	private final IRepositoryManager repositoryManager;
 
 	/**
 	 * the granularity of the analysis
 	 */
 	private final AnalyzeGranularity granularity;
 
 	/**
 	 * a factory for block analyzers
 	 */
 	private final NormalizerCreator blockAnalyzerCreator;
 
 	/**
 	 * the hash calculator
 	 */
 	private final IHashCalculator hashCalculator;
 
 	public CodeFragmentIdentifier(final Collection<FileInfo> targetFiles,
 			final Collection<RevisionInfo> revisions, final int threadsCount,
 			final CRDRegisterer crdRegisterer,
 			final CodeFragmentRegisterer fragmentRegisterer,
 			final int maxElementsCount,
 			final IRepositoryManager repositoryManager,
 			final AnalyzeGranularity granularity,
 			final NormalizerCreator blockAnalyzerCreator,
 			final IHashCalculator hashCalculator) {
 		this.targetFiles = targetFiles;
 		this.revisions = revisions;
 		this.threadsCount = threadsCount;
 		this.crdRegisterer = crdRegisterer;
 		this.fragmentRegisterer = fragmentRegisterer;
 		this.maxElementsCount = maxElementsCount;
 		this.repositoryManager = repositoryManager;
 		this.granularity = granularity;
 		this.blockAnalyzerCreator = blockAnalyzerCreator;
 		this.hashCalculator = hashCalculator;
 	}
 
 	public void run() throws Exception {
 		// creating a map between revision id and revision identifier
 		final ConcurrentMap<Long, String> revisionIdentifiers = new ConcurrentHashMap<Long, String>();
 		for (final RevisionInfo revision : revisions) {
 			revisionIdentifiers.put(revision.getId(), revision.getIdentifier());
 		}
 
 		final FileInfo[] filesArray = targetFiles.toArray(new FileInfo[0]);
 
 		if (threadsCount == 1) {
 			runWithSingleThread(revisionIdentifiers, filesArray);
 		} else {
 			runWithMultipleThreads(revisionIdentifiers, filesArray);
 		}
 	}
 
 	private final void runWithMultipleThreads(
 			final ConcurrentMap<Long, String> revisionIdentifiers,
 			final FileInfo[] filesArray) throws Exception {
 		assert threadsCount > 1;
 
 		final Thread[] threads = new Thread[threadsCount - 1];
 
 		final ConcurrentMap<Long, CRD> detectedCrds = new ConcurrentHashMap<Long, CRD>();
 		final ConcurrentMap<Long, CodeFragmentInfo> detectedFragments = new ConcurrentHashMap<Long, CodeFragmentInfo>();
 
 		final AtomicInteger index = new AtomicInteger(0);
 
 		for (int i = 0; i < threadsCount - 1; i++) {
 			threads[i] = new Thread(new CodeFragmentDetectingThread(
 					detectedCrds, detectedFragments, filesArray, index,
 					repositoryManager, revisionIdentifiers, granularity,
 					blockAnalyzerCreator, hashCalculator));
 			threads[i].start();
 		}
 
 		final CodeFragmentDetectingThreadMonitor monitor = new CodeFragmentDetectingThreadMonitor(
 				detectedCrds, detectedFragments, maxElementsCount,
 				crdRegisterer, fragmentRegisterer);
 		monitor.monitor();
 	}
 
 	private final void runWithSingleThread(
 			final ConcurrentMap<Long, String> revisionIdentifiers,
 			final FileInfo[] filesArray) throws Exception {
 		final Map<Long, CRD> detectedCrds = new TreeMap<Long, CRD>();
 		final Map<Long, CodeFragmentInfo> detectedFragments = new TreeMap<Long, CodeFragmentInfo>();
 
 		long numberOfCrds = 0;
 		long numberOfFragments = 0;
 
 		for (int i = 0; i < filesArray.length; i++) {
 			final FileInfo file = filesArray[i];
 			MessagePrinter.println("\t[" + (i + 1) + "/" + filesArray.length
 					+ "] processing " + file.getPath());
 
 			final String startRevision = revisionIdentifiers.get(file
 					.getStartRevisionId());
 
 			final String src = repositoryManager.getFileContents(startRevision,
 					file.getPath());
 			final CompilationUnit root = ASTCreator.createAST(src);
 
 			final CodeFragmentDetector detector = new CodeFragmentDetector(
 					file.getId(), file.getStartRevisionId(),
 					file.getEndRevisionId(), hashCalculator, root, granularity,
 					blockAnalyzerCreator);
 
 			root.accept(detector);
 
 			detectedCrds.putAll(detector.getDetectedCrds());
 			detectedFragments.putAll(detector.getDetectedFragments());
 
 			if (detectedCrds.size() >= maxElementsCount) {
 				final Set<CRD> currentElements = new TreeSet<CRD>();
 				currentElements.addAll(detectedCrds.values());
 				crdRegisterer.register(currentElements);
 				MessagePrinter.println("\t" + currentElements.size()
 						+ " CRDs have been registered into db");
 				numberOfCrds += currentElements.size();
 
 				for (final CRD crd : currentElements) {
 					detectedCrds.remove(crd.getId());
 				}
 			}
 
 			if (detectedFragments.size() >= maxElementsCount) {
				final Collection<CodeFragmentInfo> currentElements = detectedFragments
						.values();
 				fragmentRegisterer.register(currentElements);
 				MessagePrinter.println("\t" + currentElements.size()
 						+ " fragments have been registered into db");
 				numberOfFragments += currentElements.size();
 
 				for (final CodeFragmentInfo fragment : currentElements) {
 					detectedFragments.remove(fragment.getId());
 				}
 			}
 		}
 
 		MessagePrinter.println();
 
 		MessagePrinter
 				.println("\tregistering all the remaining elements into db ");
 		crdRegisterer.register(detectedCrds.values());
 		fragmentRegisterer.register(detectedFragments.values());
 
 		numberOfCrds += detectedCrds.size();
 		numberOfFragments += detectedFragments.size();
 
 		MessagePrinter.println("\t\tOK");
 
 		MessagePrinter.println();
 
 		MessagePrinter.println("the numbers of detected elements are ... ");
 		MessagePrinter.println("\tCRD: " + numberOfCrds);
 		MessagePrinter.println("\tFragment: " + numberOfFragments);
 	}
 
 }
