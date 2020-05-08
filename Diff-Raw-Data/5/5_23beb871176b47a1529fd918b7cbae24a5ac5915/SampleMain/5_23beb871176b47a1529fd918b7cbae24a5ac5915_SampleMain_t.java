 package it.unipi.rupos.processmining;
 
 import org.processmining.contexts.cli.ProMFactory;
 import org.processmining.contexts.cli.ProMManager;
 import org.deckfour.xes.model.XLog;
 import org.deckfour.xes.model.XTrace;
 import org.processmining.plugins.petrinet.replay.ReplayAction;
 import org.processmining.plugins.petrinet.replayfitness.ReplayFitnessSetting;
 import org.processmining.plugins.petrinet.replayfitness.TotalFitnessResult;
 import org.processmining.plugins.petrinet.replayfitness.TotalPerformanceResult;
 import org.processmining.plugins.petrinet.replayfitness.PerformanceVisualJS;
 import org.processmining.plugins.petrinet.replayfitness.PNVisualizzeJS;
 
 /**
  * @author Dipartimento di Informatica - Rupos
  *
  */
 public class SampleMain {
     public static void main(String [] args) throws Exception {
 	
     	//String logFile = "../prom5_log_files/TracceRupos.mxml";
     	//String netFile = "../prom5_log_files/TracceRuposAlpha.pnml";
     	//String logFile = "../prom5_log_files/InviaFlusso.mxml";
     	//String netFile = "../prom5_log_files/InviaFlussoWoped.pnml";
     	//String logFile = "../prom5_log_files/InviaFlusso.mxml";
     	//String netFile = "../prom5_log_files/InviaFlussoWoped.pnml";
     	
 	//String logFile = "../prom5_log_files/provepar.mxml";
     	//String netFile = "../prom5_log_files/provepar3xProm6.pnml";
	String logFile = "../prom5_log_files/recursionprove3.mxml";
     	String netFile = "../prom5_log_files/ReteAdHocRicorsionePerformacexProm6.pnml";
	//String netFile = "../prom5_log_files/ReteAdHocRicorsionePerformace3xProm6.pnml";
         //String logFile = "../prom5_log_files/sequence.mxml";
     	//String netFile = "../prom5_log_files/seqAlphahiddenx6.pnml";
     	//String logFile = "../prom5_log_files/sequence.mxml";
     	//String netFile = "../prom5_log_files/sequence_prom6.pnml";
     	
 	ProMManager manager = new ProMFactory().createManager();
 	PetriNetEngine engine = manager.createPetriNetEngine(netFile);
 	System.out.println(engine);
 
 	engine = manager.createPetriNetEngine(netFile);
 	System.out.println(engine);
 
 	XLog log = manager.openLog(logFile);
 	System.out.println("Log size: " + log.size());
 
 	ReplayFitnessSetting settings = engine.suggestSettings(log);
 	System.out.println("Settings: " + settings);
 	settings.setAction(ReplayAction.INSERT_ENABLED_MATCH, true);
 	settings.setAction(ReplayAction.INSERT_ENABLED_INVISIBLE, true);
 	settings.setAction(ReplayAction.REMOVE_HEAD, false);
 	settings.setAction(ReplayAction.INSERT_ENABLED_MISMATCH, false);
 	settings.setAction(ReplayAction.INSERT_DISABLED_MATCH, false);
 	settings.setAction(ReplayAction.INSERT_DISABLED_MISMATCH, false);
 	
 	
 	long startFitness = System.currentTimeMillis();
 	 TotalFitnessResult fitness = engine.getFitness(log, settings);
 	// System.out.println("Fitness: " + fitness);
 	long endFitness = System.currentTimeMillis();
 
 	//visualizza i dati di conformance con nella pagina html 
 	PNVisualizzeJS js = new PNVisualizzeJS();
 	js.generateJS("../javascrips/conformance.html", engine.net, fitness);
 	
 
 
 	System.out.println("Fitness for a single TRACE");
 
 	long startFitness2 = System.currentTimeMillis();
 	fitness = engine.getFitness(log.get(0), settings);
 	System.out.println("Fitness: " + fitness);
 	long endFitness2 = System.currentTimeMillis();
 	
 	
 	System.out.println("Time fitness single call " + (endFitness - startFitness));
 	System.out.println("Time fitness multiple calls " + (endFitness2 - startFitness2));
 	
 	
 	long startPerformance= System.currentTimeMillis();
 	// TotalPerformanceResult performance = engine.getPerformance(log, settings);
 	// System.out.println(performance);
 	long endPerformance = System.currentTimeMillis();
 
 	long startPerformance2 = System.currentTimeMillis();
 	TotalPerformanceResult performance = engine.getPerformance(log.get(4), settings);
 	System.out.println("Fitness: " + performance);
 	long endPerformance2 = System.currentTimeMillis();
 
 	PerformanceVisualJS js2 = new PerformanceVisualJS();
 	js2.generateJS("../javascrips/Performance.html", engine.net, performance.getList().get(0));
 	
 	
 	System.out.println("Time Performance single call " + (endPerformance - startPerformance));
 	System.out.println("Time Performance multiple calls " + (endPerformance2 - startPerformance2));
 	
 	manager.closeContext();
     }
 }
