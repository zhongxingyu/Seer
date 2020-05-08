 /*
  * Copyright (c) 2008-2010, Intel Corporation.
  * Copyright (c) 2006-2007, The Trustees of Stanford University.
  * All rights reserved.
  */
 package chord.project;
 
 import java.io.File;
 import java.io.FilenameFilter;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.io.FileWriter;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.Collection;
 import java.util.Stack;
 
 import org.scannotation.AnnotationDB;
 
 import chord.program.Program;
 import chord.project.analyses.DlogAnalysis;
 import chord.project.analyses.ITask;
 import chord.project.analyses.ProgramDom;
 import chord.project.analyses.ProgramRel;
 import chord.util.ArraySet;
 import chord.util.ArrayUtils;
 import chord.util.ClassUtils;
 import chord.util.StringUtils;
 import chord.util.Timer;
 import chord.util.tuple.object.Pair;
 import chord.util.ChordRuntimeException;
 import chord.bddbddb.RelSign;
 import chord.bddbddb.Dom;
 
 /**
  * A Chord project comprising a set of tasks and a set of targets
  * produced/consumed by those tasks.
  * 
  * @author Mayur Naik (mhn@cs.stanford.edu)
  */
 public class Project {
 	private static boolean isInited;
 	private static boolean hasNoErrors = true;
 	private static Map<String, Set<TrgtInfo>> nameToTrgtsDebugMap;
 	private static Map<ITask, Set<String>> taskToConsumedNamesMap;
 	private static Map<ITask, Set<String>> taskToProducedNamesMap;
 	private static Map<String, ITask> nameToTaskMap;
 	private static Map<String, Object> nameToTrgtMap;
 	private static Map<ITask, Set<Object>> taskToProducedTrgtsMap;
 	private static Map<ITask, Set<Object>> taskToConsumedTrgtsMap;
 	private static Map<Object, Set<ITask>> trgtToProducerTasksMap;
 	private static Map<Object, Set<ITask>> trgtToConsumerTasksMap;
 	private static Set<ITask> doneTasks = new HashSet<ITask>();
 	private static Set<Object> doneTrgts = new HashSet<Object>();
 	private static Stack<Timer> timers = new Stack<Timer>();
 	private static Timer currTimer;
 	private static boolean verbose = Properties.verbose;
 
 	private Project() { }
 
 	public static void run() {
 		System.out.println("ENTER: chord");
 		currTimer = new Timer("chord");
 		currTimer.init();
 
 		Program program = Program.getProgram();
 
 		if (Properties.buildScope) {
 			program.getMethods();
 			// program.getReflectInfo();
 		}
 
 		if (Properties.printAllClasses) {
 			program.printAllClasses();
 
 		}
 		String[] printClasses = Properties.toArray(Properties.printClasses);
 		if (printClasses.length > 0) {
 			for (String className : printClasses)
 				program.printClass(className);
 		}
 		String[] printMethods = Properties.toArray(Properties.printMethods);
 		if (printMethods.length > 0) {
 			for (String methodSign : printMethods)
 				program.printMethod(methodSign);
 		}
 
         String[] runAnalyses = Properties.toArray(Properties.runAnalyses);
         if (runAnalyses.length > 0) {
         	Project.init();
 			for (String name : runAnalyses)
 				runTask(name);
         }
 
 		String[] printRels = Properties.toArray(Properties.printRels);
 		if (printRels.length > 0) {
 			Project.init();
 			for (String relName : printRels) {
 				ProgramRel rel = (ProgramRel) nameToTrgtMap.get(relName);
 				assert rel != null : "failed to load relation " + relName;
 				rel.load();
 				rel.print();
 			}
 		}
 
 		if (Properties.publishTargets) {
 			Project.init();
 			PrintWriter out;
 			try {
 				File file = new File(Properties.outDirName, "targets.xml");
 				out = new PrintWriter(new FileWriter(file));
 			} catch (IOException ex) {
 				throw new ChordRuntimeException(ex);
 			}
 			out.println("<targets " +
 				"java_analysis_path=\"" + Properties.javaAnalysisPathName + "\" " +
 				"dlog_analysis_path=\"" + Properties.dlogAnalysisPathName + "\">");
 			for (String name : nameToTrgtMap.keySet()) {
 				Object trgt = nameToTrgtMap.get(name);
 				String kind;
 				if (trgt instanceof ProgramDom)
 					kind = "domain";
 				else if (trgt instanceof ProgramRel)
 					kind = "relation";
 				else
 					kind = "other";
 				Set<ITask> tasks = trgtToProducerTasksMap.get(trgt);
 				Iterator<ITask> it = tasks.iterator();
 				String producerStr;
 				String otherProducersStr = "";
 				if (it.hasNext()) {
 					ITask fstTask = it.next();
 					producerStr = getNameAndURL(fstTask);
 					while (it.hasNext()) {
 						ITask task = it.next();
 						otherProducersStr += "<producer " + getNameAndURL(task) + "/>";
 					}
 				} else
 					producerStr = "producer_name=\"-\" producer_url=\"-\"";
 				out.println("\t<target name=\"" + name + "\" kind=\"" + kind +
 					"\" " + producerStr  + ">" +
 					otherProducersStr + "</target>");
 			}
 			out.println("</targets>");
 			out.close();
 			OutDirUtils.copyFileFromMainDir("src/web/style.css");
 			OutDirUtils.copyFileFromMainDir("src/web/targets.xsl");
 			OutDirUtils.copyFileFromMainDir("src/web/targets.dtd");
 			OutDirUtils.runSaxon("targets.xml", "targets.xsl");
 		}
 
 		System.out.println("LEAVE: chord");
 		currTimer.done();
 		printCurrTimer();
 	}
 
 	public static Object getTrgt(String name) {
 		Object trgt = nameToTrgtMap.get(name);
 		if (trgt == null) {
 			throw new ChordRuntimeException("Trgt '" + name +
 				"' not found.");
 		}
 		return trgt;
 	}
 	public static ITask getTask(String name) {
 		ITask task = nameToTaskMap.get(name);
 		if (task == null) {
 			throw new ChordRuntimeException("Task '" + name +
 				"' not found.");
 		}
 		return task;
 	}
 
 	private static void printCurrTimer() {
 		System.out.println("Exclusive time: " + currTimer.getExclusiveTimeStr());
 		System.out.println("Inclusive time: " + currTimer.getInclusiveTimeStr());
 	}
 
 	public static ITask getTaskProducingTrgt(Object trgt) {
 		Set<ITask> tasks = trgtToProducerTasksMap.get(trgt);
 		if (tasks.size() != 1) {
 			throw new ChordRuntimeException("Task producing trgt '" +
 				trgt + "' not found.");
 		}
 		return tasks.iterator().next();
 	}
 
 	public static void runTask(ITask task) {
 		System.out.println("ENTER: " + task);
 		if (isTaskDone(task)) {
 			System.out.println("ALREADY DONE.");
 			return;
 		}
 		currTimer.pause();
 		timers.push(currTimer);
 		currTimer = new Timer(task.getName());
 		currTimer.init();
 		Set<Object> consumedTrgts = taskToConsumedTrgtsMap.get(task);
 		for (Object trgt : consumedTrgts) {
 			if (isTrgtDone(trgt))
 				continue;
 			ITask task2 = getTaskProducingTrgt(trgt);
 			runTask(task2);
 		}
 		task.run();
         System.out.println("LEAVE: " + task);
         currTimer.done();
 		printCurrTimer();
 		currTimer = timers.pop();
 		currTimer.resume();
 		setTaskDone(task);
 		Set<Object> producedTrgts = taskToProducedTrgtsMap.get(task);
 		assert(producedTrgts != null);
 		for (Object trgt : producedTrgts) {
 			setTrgtDone(trgt);
 		}
 	}
 
 	public static ITask runTask(String name) {
 		ITask task = getTask(name);
 		runTask(task);
 		return task;
 	}
 
 	public static boolean isTrgtDone(Object trgt) {
 		return doneTrgts.contains(trgt);
 	}
 
 	public static boolean isTrgtDone(String name) {
 		return isTrgtDone(getTrgt(name));
 	}
 
 	public static void setTrgtDone(Object trgt) {
 		doneTrgts.add(trgt);
 	}
 
 	public static void setTrgtDone(String name) {
 		setTrgtDone(getTrgt(name));
 	}
 
 	public static void resetTrgtDone(Object trgt) {
 		if (doneTrgts.remove(trgt)) {
 			for (ITask task : trgtToConsumerTasksMap.get(trgt)) {
 				resetTaskDone(task);
 			}
 		}
 	}
 
 	public static void runTasksWithVisitors(Collection<ITask> tasks) {
 		// TODO
 	}
 
 	public static void runTaskWithVisitors(ITask task) {
 		// TODO
 	}
 
 	public static void resetAll() {
 		doneTrgts.clear();
 		doneTasks.clear();
 	}
 
 	public static void resetTrgtDone(String name) {
 		resetTrgtDone(getTrgt(name));
 	}
 
 	public static boolean isTaskDone(ITask task) {
 		return doneTasks.contains(task);
 	}
 
 	public static boolean isTaskDone(String name) {
 		return isTaskDone(getTask(name));
 	}
 
 	public static void setTaskDone(ITask task) {
 		doneTasks.add(task);
 	}
 
 	public static void setTaskDone(String name) {
 		setTaskDone(getTask(name));
 	}
 
 	public static void resetTaskDone(ITask task) {
 		if (doneTasks.remove(task)) {
 			for (Object trgt : taskToProducedTrgtsMap.get(task)) {
 				resetTrgtDone(trgt);
 			}
 		}
 	}
 
 	public static void resetTaskDone(String name) {
 		resetTaskDone(getTask(name));
 	}
 
 	public static void init() {
 		if (isInited)
 			return;
 
 		// create and populate the following maps:
 		// nameToTaskMap, nameToTrgtsDebugMap
 		// taskToConsumedNamesMap, taskToProducedNamesMap
 		nameToTaskMap = new HashMap<String, ITask>();
 		nameToTrgtsDebugMap = new HashMap<String, Set<TrgtInfo>>();
 		taskToConsumedNamesMap = new HashMap<ITask, Set<String>>();
 		taskToProducedNamesMap = new HashMap<ITask, Set<String>>();
 		buildDlogAnalysisMap();
 		buildJavaAnalysisMap();
 
 		nameToTrgtMap = new HashMap<String, Object>();
 		List<Pair<ProgramRel, RelSign>> todo =
 			new ArrayList<Pair<ProgramRel, RelSign>>();
 		for (Map.Entry<String, Set<TrgtInfo>> e :
 				nameToTrgtsDebugMap.entrySet()) {
 			String name = e.getKey();
 			Set<TrgtInfo> infos = e.getValue();
 			Iterator<TrgtInfo> it = infos.iterator();
 			TrgtInfo fstInfo = it.next();
 			Class resType = fstInfo.type;
 			String resTypeLoc = fstInfo.location;
 			String[] resDomNames;
 			String resDomOrder;
 			if (fstInfo.sign != null) {
 				resDomNames = fstInfo.sign.val0;
 				resDomOrder = fstInfo.sign.val1;
 			} else {
 				resDomNames = null;
 				resDomOrder = null;
 			}
 			String resDomNamesLoc = fstInfo.location;
 			String resDomOrderLoc = fstInfo.location;
 			boolean corrupt = false;
 			while (it.hasNext()) {
 				TrgtInfo info = it.next();
 				Class curType = info.type;
 				if (curType != null) {
 					if (resType == null) {
 						resType = curType;
 						resTypeLoc = info.location;
 					} else if (ClassUtils.isSubclass(curType, resType)) {
 						resType = curType;
 						resTypeLoc = info.location;
 					} else if (!ClassUtils.isSubclass(resType, curType)) {
 						inconsistentTypes(name,
 							resType.toString(), curType.toString(),
 							resTypeLoc, info.location);
 						corrupt = true;
 						break;
 					}
 				}
 				RelSign curSign = info.sign;
 				if (curSign != null) {
 					String[] curDomNames = curSign.val0;
 					if (resDomNames == null) {
 						resDomNames = curDomNames;
 						resDomNamesLoc = info.location;
 					} else if (!Arrays.equals(resDomNames, curDomNames)) {
 						inconsistentDomNames(name,
 							ArrayUtils.toString(resDomNames),
 							ArrayUtils.toString(curDomNames),
 							resDomNamesLoc, info.location);
 						corrupt = true;
 						break;
 					}
 					String curDomOrder = curSign.val1;
 					if (curDomOrder != null) {
 						if (resDomOrder == null) {
 							resDomOrder = curDomOrder;
 							resDomOrderLoc = info.location;
 						} else if (!resDomOrder.equals(curDomOrder)) {
 							inconsistentDomOrders(name,
 								resDomOrder, curDomOrder,
 								resDomOrderLoc, info.location);
 						}
 					}
 				}
 			}
 			if (corrupt)
 				continue;
 			if (resType == null) {
 				unknownType(name);
 				continue;
 			}
 			RelSign sign = null;
 			if (ClassUtils.isSubclass(resType, ProgramRel.class)) {
 				if (resDomNames == null) {
 					unknownSign(name);
 					continue;
 				}
 				if (resDomOrder == null) {
 					unknownOrder(name);
 					continue;
 				}
 				sign = new RelSign(resDomNames, resDomOrder);
 			}
 			Object trgt = nameToTaskMap.get(name);
 			if (trgt == null) {
 				trgt = instantiate(resType.getName(), resType);
 				if (trgt instanceof ITask) {
 					ITask analysis = (ITask) trgt;
 					analysis.setName(name);
 				}
 			}
 			nameToTrgtMap.put(name, trgt);
 			if (sign != null) {
 				ProgramRel rel = (ProgramRel) trgt;
 				todo.add(new Pair<ProgramRel, RelSign>(rel, sign));
 			}
 		}
 
 		checkErrors();
 
 		for (Pair<ProgramRel, RelSign> tuple : todo) {
 			ProgramRel rel = tuple.val0;
 			RelSign sign = tuple.val1;
 			String[] domNames = sign.getDomNames();
 			ProgramDom[] doms = new ProgramDom[domNames.length];
 			for (int i = 0; i < domNames.length; i++) {
 				String domName = StringUtils.trimNumSuffix(domNames[i]);
 				Object trgt = nameToTrgtMap.get(domName);
 				assert (trgt != null);
 				assert (trgt instanceof ProgramDom);
 				doms[i] = (ProgramDom) trgt;
 			}
 			rel.setSign(sign);
 			rel.setDoms(doms);
 		}
 
 		trgtToConsumerTasksMap = new HashMap<Object, Set<ITask>>();
 		trgtToProducerTasksMap = new HashMap<Object, Set<ITask>>();
 		for (Object trgt : nameToTrgtMap.values()) {
 			Set<ITask> consumerTasks = new HashSet<ITask>();
 			trgtToConsumerTasksMap.put(trgt, consumerTasks);
 			Set<ITask> producerTasks = new HashSet<ITask>();
 			trgtToProducerTasksMap.put(trgt, producerTasks);
 		}
 		taskToConsumedTrgtsMap = new HashMap<ITask, Set<Object>>();
 		taskToProducedTrgtsMap = new HashMap<ITask, Set<Object>>();
 		for (ITask task : nameToTaskMap.values()) {
 			Set<String> consumedNames = taskToConsumedNamesMap.get(task);
 			Set<Object> consumedTrgts =
 				new HashSet<Object>(consumedNames.size());
 			for (String name : consumedNames) {
 				Object trgt = nameToTrgtMap.get(name);
 				assert (trgt != null);
 				consumedTrgts.add(trgt);
 				Set<ITask> consumerTasks =
 					trgtToConsumerTasksMap.get(trgt);
 				consumerTasks.add(task);
 			}
 			taskToConsumedTrgtsMap.put(task, consumedTrgts);
 			Set<String> producedNames =
 				taskToProducedNamesMap.get(task);
 			Set<Object> producedTrgts =
 				new HashSet<Object>(producedNames.size());
 			for (String name : producedNames) {
 				Object trgt = nameToTrgtMap.get(name);
 				assert (trgt != null);
 				producedTrgts.add(trgt);
 				Set<ITask> producerTasks =
 					trgtToProducerTasksMap.get(trgt);
 				producerTasks.add(task);
 			}
 			taskToProducedTrgtsMap.put(task, producedTrgts);
 		}
 		for (String trgtName : nameToTrgtMap.keySet()) {
 			Object trgt = nameToTrgtMap.get(trgtName);
 			Set<ITask> producerTasks = trgtToProducerTasksMap.get(trgt);
 			int pSize = producerTasks.size();
 			if (pSize == 0) {
 				Set<ITask> consumerTasks =
 					trgtToConsumerTasksMap.get(trgt);
 				int cSize = consumerTasks.size();
 				List<String> consumerTaskNames =
 					new ArraySet<String>(cSize);
 				for (ITask task : consumerTasks)
 					consumerTaskNames.add(getSourceName(task));
 				undefinedTarget(trgtName, consumerTaskNames);
 			} else if (pSize > 1) {
 				List<String> producerTaskNames =
 					new ArraySet<String>(pSize);
 				for (ITask task : producerTasks) {
 					producerTaskNames.add(getSourceName(task));
 				}
 				redefinedTarget(trgtName, producerTaskNames);
 			}
 		}
 
 		if (Properties.reuseRels) {
 			File file = new File(Properties.bddbddbWorkDirName);
 			File[] subFiles = file.listFiles(filter);
 			for (File subFile : subFiles) {
 				String fileName = subFile.getName();
 				if (fileName.endsWith(".bdd")) {
 					int n = fileName.length();
 					String relName = fileName.substring(0, n - 4);
 					ProgramRel rel = (ProgramRel) getTrgt(relName);
 					for (Dom dom : rel.getDoms()) {
 						ITask task2 = getTaskProducingTrgt(dom);
 						runTask(task2);
 					}
 					rel.load();
 					setTrgtDone(relName);
 				}
 			}
 		}
 
 		isInited = true;
 	}
 
 	private static String getNameAndURL(ITask task) {
 		Class clazz = task.getClass();
 		String loc;
 		if (clazz == DlogAnalysis.class) {
 			loc = ((DlogAnalysis) task).getFileName();
 			loc = (new File(loc)).getName();
 		} else
 			loc = clazz.getName().replace(".", "/") + ".html";
 		loc = Properties.javadocURL + loc;
 		return "producer_name=\"" + task.getName() +
 			"\" producer_url=\"" + loc + "\"";
 	}
 
 	private static void checkErrors() {
 		if (!hasNoErrors) {
 			System.err.println("Found errors (see above). Exiting ...");
 			System.exit(1);
 		}
 	}
 
 	private static void createTrgt(String name, Class type, String location) {
 		TrgtInfo info = new TrgtInfo(type, location, null);
 		createTrgt(name, info);
 	}
 	
 	private static void createTrgt(String name, Class type, String location,
 			RelSign relSign) {
 		for (String name2 : relSign.getDomKinds()) {
 			createTrgt(name2, ProgramDom.class, location); 
 		}
 		TrgtInfo info = new TrgtInfo(type, location, relSign);
 		createTrgt(name, info);
 	}
 
 	private static void createTrgt(String name, TrgtInfo info) {
 		Set<TrgtInfo> infos = nameToTrgtsDebugMap.get(name);
 		if (infos == null) {
 			infos = new HashSet<TrgtInfo>();
 			nameToTrgtsDebugMap.put(name, infos);
 		}
 		infos.add(info);
 	}
 
 	private static class TrgtInfo {
 		public Class type;
 		public final String location;
 		public RelSign sign;
 		public TrgtInfo(Class type, String location, RelSign sign) {
 			this.type = type;
 			this.location = location;
 			this.sign = sign;
 		}
 	};
 
 	private static void buildDlogAnalysisMap() {
 		String dlogAnalysisPathName = Properties.dlogAnalysisPathName;
 		if (dlogAnalysisPathName.equals(""))
 			return;
 		String[] fileNames = dlogAnalysisPathName.split(File.pathSeparator);
 		for (String fileName : fileNames) {
 			File file = new File(fileName);
 			if (!file.exists()) {
 				nonexistentPathElem(fileName, "chord.dlog.analysis.path");
 				continue;
 			}
 			processDlogAnalysis(file);
 		}
 	}
 
 	private static void buildJavaAnalysisMap() {
 		String javaAnalysisPathName = Properties.javaAnalysisPathName;
 		if (javaAnalysisPathName.equals(""))
 			return;
 		ArrayList<URL> list = new ArrayList<URL>();
 		String[] fileNames = javaAnalysisPathName.split(File.pathSeparator);
 		for (String fileName : fileNames) {
 			File file = new File(fileName);
 			if (!file.exists()) {
 				nonexistentPathElem(fileName, "chord.java.analysis.path");
 				continue;
 			}
 			try {
                list.add(file.toURL());
             } catch (MalformedURLException ex) {
             	malformedPathElem(fileName, "chord.java.analysis.path",
             		ex.getMessage());
 				continue;
            }
         }
 		URL[] urls = new URL[list.size()];
 		list.toArray(urls);
 		AnnotationDB db = new AnnotationDB();
 		try {
 			db.scanArchives(urls);
 		} catch (IOException ex) {
 			throw new ChordRuntimeException(ex);
 		}
 		Map<String, Set<String>> index = db.getAnnotationIndex();
 		if (index == null)
 			return;
 		Set<String> classNames = index.get(Chord.class.getName());
 		if (classNames == null)
 			return;
 		for (String className : classNames) {
 			processJavaAnalysis(className);
 		}
 	}
 
     private static final FilenameFilter filter = new FilenameFilter() {
 		public boolean accept(File dir, String name) {
 			if (name.startsWith("."))
 				return false;
 			return true;
 		}
 	};
 
 	// 1. Creates a DlogAnalysis task object corresponding to this Datalog analysis
 	// 2. Maps the name of the analysis to that object in nameToTaskMAp
 	// 3. Creates a ProgramDom trgt object for each domain in the .bddvarorder of
 	//    the Datalog analysis and for each domain of each input/output relation of
 	//    the Datalog analysis, and adds it to nameToTrgtsDebugMap
 	// 4. Creates a ProgramRel trgt object for each input/output relation of 
 	//    the Datalog analysis, and adds it to nameToTrgtsDebugMap
 	// 5. Adds each input relation as a consumed trgt of the created DlogAnalysis
 	//    task object, in taskToConsumedNamesMap
 	// 6. Adds each output relation as a produced trgt of the created DlogAnalysis
 	//    task object, in taskToProducedNamesMap
 	private static void processDlogAnalysis(File file) {
 		if (file.isDirectory()) {
 			File[] subFiles = file.listFiles(filter);
 			for (File subFile : subFiles)
 				processDlogAnalysis(subFile);
 			return;
 		}
 		String fileName = file.getAbsolutePath();
 		if (!fileName.endsWith(".dlog") && !fileName.endsWith(".datalog"))
 			return;
 		DlogAnalysis task = new DlogAnalysis();
 		boolean ret = task.parse(fileName);
 		if (!ret) {
 			ignoreDlogAnalysis(fileName);
 			return;
 		}
 		String name = task.getDlogName();
 		if (name == null) {
 			anonDlogAnalysis(fileName);
 			name = fileName;
 		}
 		ITask task2 = nameToTaskMap.get(name);
 		if (task2 != null) {
 			redefinedDlogTask(fileName, name, getSourceName(task2));
 			return;
 		}
 		for (String domName : task.getDomNames()) {
 			createTrgt(domName, ProgramDom.class, fileName);
 		}
 		Map<String, RelSign> consumedRelsMap =
 			task.getConsumedRels();
 		for (Map.Entry<String, RelSign> e : consumedRelsMap.entrySet()) {
 			String relName = e.getKey();
 			RelSign relSign = e.getValue();
 			createTrgt(relName, ProgramRel.class, fileName, relSign);
 		}
 		Map<String, RelSign> producedRelsMap =
 			task.getProducedRels();
 		for (Map.Entry<String, RelSign> e : producedRelsMap.entrySet()) {
 			String relName = e.getKey();
 			RelSign relSign = e.getValue();
 			createTrgt(relName, ProgramRel.class, fileName, relSign);
 		}
 		taskToConsumedNamesMap.put(task,
 			consumedRelsMap.keySet());
 		taskToProducedNamesMap.put(task,
 			producedRelsMap.keySet());
 		task.setName(name);
 		nameToTaskMap.put(name, task);
 	}
 	
 	private static void processJavaAnalysis(String className) {
 		Class type;
 		try {
 			type = Class.forName(className);
 		} catch (ClassNotFoundException ex) {
 			throw new ChordRuntimeException(ex);
 		}
 		ChordAnnotParser info = new ChordAnnotParser(type);
 		boolean ret = info.parse();
 		if (!ret) {
 			ignoreJavaAnalysis(className);
 			return;
 		}
 		String name = info.getName();
 		if (name.equals("")) {
 			anonJavaAnalysis(className);
 			name = className;
 		}
 		ITask task = nameToTaskMap.get(name);
 		if (task != null) {
 			redefinedJavaTask(className, name, getSourceName(task));
 			return;
 		}
 		try {
 			task = instantiate(className, ITask.class);
 		} catch (RuntimeException ex) {
 			nonInstantiableJavaAnalysis(className, ex.getMessage());
 			return;
 		}
 		Map<String, Class  > nameToTypeMap = info.getNameToTypeMap();
 		Map<String, RelSign> nameToSignMap = info.getNameToSignMap();
 		for (Map.Entry<String, Class> e : nameToTypeMap.entrySet()) {
 			String name2 = e.getKey();
 			Class type2 = e.getValue();
 			RelSign sign2 = nameToSignMap.get(name2);
 			if (sign2 != null)
 				createTrgt(name2, type2, className, sign2);
 			else
 				createTrgt(name2, type2, className);
 		}
 		for (Map.Entry<String, RelSign> e :
 				nameToSignMap.entrySet()) {
 			String name2 = e.getKey();
 			if (nameToTypeMap.containsKey(name2))
 				continue;
 			RelSign sign2 = e.getValue();
 			createTrgt(name2, ProgramRel.class, className, sign2);
 		}
 		Set<String> consumedNames = info.getConsumedNames();
 		Set<String> producedNames = info.getProducedNames();
 		taskToConsumedNamesMap.put(task, consumedNames);
 		taskToProducedNamesMap.put(task, producedNames);
 		for (String consumedName : consumedNames) {
 			if (!nameToTypeMap.containsKey(consumedName) &&
 				!nameToSignMap.containsKey(consumedName)) {
 				createTrgt(consumedName, null, className);
 			}
 		}
 		for (String producedName : producedNames) {
 			if (!nameToTypeMap.containsKey(producedName) &&
 				!nameToSignMap.containsKey(producedName)) {
 				createTrgt(producedName, null, className);
 			}
 		}
 		task.setName(name);
 		nameToTaskMap.put(name, task);
 	}
 
 	// create an instance of the class named className and cast
 	// it to the class named clazz
 	private static <T> T instantiate(String className, Class<T> clazz) {
 		try {
 			Object obj = Class.forName(className).newInstance();
 			return clazz.cast(obj);
 		} catch (InstantiationException e) {
 			throw new ChordRuntimeException(
 				"Class '" + className + "' cannot be instantiated.");
 		} catch (IllegalAccessException e) {
 			throw new ChordRuntimeException(
 				"Class '" + className + "' cannot be accessed.");
 		} catch (ClassNotFoundException e) {
 			throw new ChordRuntimeException(
 				"Class '" + className + "' not found.");
 		} catch (ClassCastException e) {
 			throw new ChordRuntimeException(
 				"Class '" + className + "' must be a subclass of " +
 				clazz.getName() + ".");
 		}
 	}
 	
 	private static String getSourceName(ITask analysis) {
 		Class clazz = analysis.getClass();
 		if (clazz == DlogAnalysis.class)
 			return ((DlogAnalysis) analysis).getFileName();
 		return clazz.getName();
 	}
 
 	private static void anonJavaAnalysis(String name) {
 		if (verbose) Messages.log("PROJECT.ANON_JAVA_ANALYSIS", name);
 	}
 	
 	private static void anonDlogAnalysis(String name) {
 		if (verbose) Messages.log("PROJECT.ANON_DLOG_ANALYSIS", name);
 	}
 
 	private static void ignoreDlogAnalysis(String name) {
 		Messages.log("PROJECT.IGNORING_DLOG_ANALYSIS", name);
 		hasNoErrors = false;
 	}
 	
 	private static void ignoreJavaAnalysis(String name) {
 		Messages.log("PROJECT.IGNORING_JAVA_ANALYSIS", name);
 		hasNoErrors = false;
 	}
 	
 	private static void undefinedTarget(String name, List<String> consumerTaskNames) {
 		if (verbose) {
 			String msg = "WARNING: '" + name + "' not declared as produced name of any task";
 			if (consumerTaskNames.isEmpty())
 				msg += "\n";
 			else {
 				msg += "; declared as consumed name of following tasks:\n";
 				for (String taskName : consumerTaskNames)
 					msg += "\t'" + taskName + "'\n";
 			}
 			Messages.logAnon(msg);
 		}
 	}
 	
 	private static void redefinedTarget(String name, List<String> producerTaskNames) {
 		if (verbose) {
 			String msg = "WARNING: '" + name + "' declared as produced name of multiple tasks:\n";
 			for (String taskName : producerTaskNames) 
 				msg += "\t'" + taskName + "'\n";
 			Messages.logAnon(msg);
 		}
 	}
 	
 	private static void inconsistentDomNames(String relName, String names1, String names2, String loc1, String loc2) {
 		Messages.log("PROJECT.DOM_NAMES_INCONSISTENT", relName, names1, names2, loc1, loc2);
 		hasNoErrors = false;
 	}
 	
 	private static void inconsistentDomOrders(String relName, String order1, String order2, String loc1, String loc2) {
 		if (verbose) Messages.log("PROJECT.DOM_ORDERS_INCONSISTENT", relName, order1, order2, loc1, loc2);
 	}
 	
 	private static void inconsistentTypes(String name, String type1, String type2, String loc1, String loc2) {
 		Messages.log("PROJECT.TARGET_TYPE_INCONSISTENT", name, type1, type2, loc1, loc2);
 		hasNoErrors = false;
 	}
 	
 	private static void unknownSign(String name) {
 		Messages.log("PROJECT.RELATION_SIGN_UNKNOWN", name);
 		hasNoErrors = false;
 	}
 	
 	private static void unknownOrder(String name) {
 		Messages.log("PROJECT.RELATION_ORDER_UNKNOWN", name);
 		hasNoErrors = false;
 	}
 	
 	private static void unknownType(String name) {
 		Messages.log("PROJECT.TARGET_TYPE_UNKNOWN", name);
 		hasNoErrors = false;
 	}
 	
 	private static void redefinedJavaTask(String newTaskName, String name, String oldTaskName) {
 		Messages.log("PROJECT.JAVA_TASK_REDEFINED", name, oldTaskName, newTaskName);
 		hasNoErrors = false;
 	}
 	private static void redefinedDlogTask(String newTaskName, String name, String oldTaskName) {
 		Messages.log("PROJECT.DLOG_TASK_REDEFINED", newTaskName, name, oldTaskName);
 		hasNoErrors = false;
 	}
 	
 	private static void malformedPathElem(String elem, String path, String msg) {
 		if (verbose) Messages.log("PROJECT.MALFORMED_PATH_ELEM", elem, path);
 	}
 	
 	private static void nonexistentPathElem(String elem, String path) {
 		if (verbose) Messages.log("PROJECT.NON_EXISTENT_PATH_ELEM", elem, path);
 	}
 	
 	private static void nonInstantiableJavaAnalysis(String name, String msg) {
 		Messages.log("PROJECT.JAVA_TASK_UNINSTANTIABLE", name, msg);
 		hasNoErrors = false;
 	}
 }
