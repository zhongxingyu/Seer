 package main.app;
 
 import java.util.ArrayList;
 
 import main.data.DataStorage;
 import main.data.ExportData;
 import main.data.ImportData;
 import main.data.RuleLogger;
 import main.domain.PlannerSolution;
 
 import org.drools.planner.config.XmlSolverFactory;
 import org.drools.planner.core.Solver;
 import org.drools.planner.core.score.director.drools.DroolsScoreDirector;
 import org.drools.planner.core.solver.DefaultSolver;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class FLMPlannerHelloWorld {
 
 	// GWpK[̍쐬
 	static final Logger logger = LoggerFactory
 			.getLogger(FLMPlannerHelloWorld.class);
 
 	// f[^qɂ̍쐬
 	public static DataStorage storage;
 
 	// \o[̍쐬
 	public static final String SOLVER_CONFIG = "/FLMPlannerSolverConfig.xml";
 	public static final String TEST_CONFIG = "/FLMPlannerRuleCheck.xml";
 
 	// vZ̎s
 	public static void runData(String inFile) {
 
 		// f[^̃C|[g
 		ImportData importer = new ImportData();
 		importer.importFromXLS(inFile);
 		storage = importer.getStorage();
 
 		// \o[̏
 		long startTimeCounter = System.currentTimeMillis();
 		XmlSolverFactory solverFactory = new XmlSolverFactory();
 		solverFactory.configure(SOLVER_CONFIG);
 		DefaultSolver solver = (DefaultSolver) solverFactory.buildSolver();
 
 		// l̐ݒ
 		PlannerSolution initialSolution = new PlannerSolution(
 				storage.scheduleList, storage.classroomList, storage.dayList,
 				storage.blockedClassroomList, storage.courseTotalSizeList);
 		solver.setPlanningProblem(initialSolution);
 
 		// JÓvJn
 		solver.solve();
 
 		// JÓv挋ʂ̎擾
 		PlannerSolution solvedSolution = (PlannerSolution) solver
 				.getBestSolution();
 
 		// ŏIʂ̃XRA̕\
 		logger.info(solvedSolution.getScore().toString());
 
 		// vZԂ̕\
 		long elapsedTimeMillis = System.currentTimeMillis() - startTimeCounter;
 		System.out.println("Elapsed time: "
 				+ (int) (elapsedTimeMillis / (60 * 1000F)) + "min "
 				+ elapsedTimeMillis / 1000F + "sec");
 
 		// XPW[Xgւ̊i[
 		storage.scheduleList = solvedSolution.getScheduleList();
 	}
 
 	// vZʂ̃`FbN
 	public static void checkOutput(String exportMessage) {
 		XmlSolverFactory solverFactory = new XmlSolverFactory();
 		solverFactory.configure(TEST_CONFIG);
 		Solver solver = solverFactory.buildSolver();
 
 		// l̐ݒ
 		PlannerSolution initialSolution = new PlannerSolution(
 				storage.scheduleList, storage.classroomList, storage.dayList,
 				storage.blockedClassroomList, storage.courseTotalSizeList);
 
 		// XRAfBN^[̍쐬
 		DroolsScoreDirector scoreDirector = (DroolsScoreDirector) solver
 				.getScoreDirectorFactory().buildScoreDirector();
 		scoreDirector.setWorkingSolution(initialSolution);
 
 		// [̃K[̍쐬E
 		ArrayList<RuleLogger> ruleLogList = new ArrayList<RuleLogger>();
 		ruleLogList.add(new RuleLogger(exportMessage, ""));
 		scoreDirector.getWorkingMemory().setGlobal("ruleLog", ruleLogList);
 		scoreDirector.calculateScore();
 		storage.ruleLog = (ArrayList<RuleLogger>) scoreDirector.getWorkingMemory().getGlobal("ruleLog");
 
 		// ŏIʂ̃XRA̕\
 		logger.info(initialSolution.getScore().toString());
 	}
 
 	// f[^̃GNX|[g
 	public static void exportResult(String outFile, String logFile) {
 		ExportData exporter = new ExportData(storage.scheduleList,
 				storage.ruleLog);
 		logger.info("Export to XLS: " + exporter.exportToXLS(outFile));
 		if (!logFile.equals("")) {
 			logger.info("Export Log = " + exporter.exportLogText(logFile));
 		}
 	}
 
 	public static void main(String[] args) {
 		runData(args[0]);
		String msg = args[0];
		msg = msg.replace(".xls", "").replace("DroolsMaster", "");
		if (msg.lastIndexOf("\\") > 0) {
			msg = msg.substring(msg.lastIndexOf("\\")+1);
		}
		checkOutput("Planning month: "	+ msg);
 		if (args.length == 2) {
 			exportResult(args[1], "");
 		} else {
 			exportResult(args[1], args[2]);
 		}
 	}
 
 }
