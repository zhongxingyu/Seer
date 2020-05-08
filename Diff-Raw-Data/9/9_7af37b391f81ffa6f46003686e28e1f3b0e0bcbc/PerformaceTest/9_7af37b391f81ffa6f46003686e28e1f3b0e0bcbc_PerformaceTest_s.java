 package de.uni_koblenz.jgralabtest.dhht;
 
 import java.rmi.RemoteException;
 import java.util.ArrayList;
 
 import de.uni_koblenz.jgralab.Graph;
 import de.uni_koblenz.jgralab.dhhttest.schema.Activity;
 import de.uni_koblenz.jgralab.dhhttest.schema.BusinessProcess;
 import de.uni_koblenz.jgralab.dhhttest.schema.DHHTTestGraph;
 import de.uni_koblenz.jgralab.dhhttest.schema.DHHTTestSchema;
 import de.uni_koblenz.jgralab.dhhttest.schema.Feature;
 import de.uni_koblenz.jgralab.dhhttest.schema.FeatureTraceabilityLink;
 import de.uni_koblenz.jgralab.dhhttest.schema.FeatureTraceabilityLink_activity;
 import de.uni_koblenz.jgralab.dhhttest.schema.FeatureTraceabilityLink_feature;
 import de.uni_koblenz.jgralab.dhhttest.schema.FeatureTraceabilityLink_process;
 import de.uni_koblenz.jgralab.dhhttest.schema.TraceabilityLink_rule;
 import de.uni_koblenz.jgralab.dhhttest.schema.TransformationRule;
 
 public class PerformaceTest {
 	
 	private static int activityCount = 874002;
 	
 	private static int ruleCount = 1541;
 	
 	private static int featureCount = 123074;
 	
 	private static int processCount = 1383;
 	
 	private static int linkCount = 1000000;
 	
 	ArrayList<Activity> activityList = new ArrayList<Activity>(activityCount);
 	ArrayList<Feature> featureList = new ArrayList<Feature>(featureCount);
 	ArrayList<BusinessProcess> processList = new ArrayList<BusinessProcess>(processCount);
 	ArrayList<TransformationRule> ruleList = new ArrayList<TransformationRule>(ruleCount);
 	
 	private Graph createGraph()  throws RemoteException {
 		System.out.println("Creating graph...");
 		DHHTTestGraph graph = DHHTTestSchema.instance().createDHHTTestGraphInMem();
 		
 		long startTime = System.currentTimeMillis();
 		for (int i=0; i<activityCount;i++) {
 			Activity a = graph.createActivity();
 			activityList.add(a);
 		}
 		for (int i=0; i<featureCount;i++) {
 			Feature f = graph.createFeature();
 			featureList.add(f);
 		}
 		for (int i=0; i<processCount;i++) {
 			BusinessProcess p = graph.createBusinessProcess();
 			processList.add(p);
 		}
 		for (int i=0; i<ruleCount;i++) {
 			TransformationRule r = graph.createTransformationRule();
 			ruleList.add(r);
 		}
 		for (int i=0; i<linkCount; i++) {
 			FeatureTraceabilityLink l = graph.createFeatureTraceabilityLink();
 			l.connect(FeatureTraceabilityLink_activity.class, activityList.get(i%activityCount));
 			l.connect(FeatureTraceabilityLink_activity.class, activityList.get((i+1)%activityCount));
 			l.connect(FeatureTraceabilityLink_feature.class, featureList.get(i%featureCount));
 			l.connect(FeatureTraceabilityLink_process.class, processList.get(i%processCount));
 			l.connect(TraceabilityLink_rule.class, ruleList.get(i%ruleCount));
 		}
 		
 		
 		long time = System.currentTimeMillis() - startTime;
 		System.out.println("Sucessfully created graph in " + time + " milliseconds");
 		System.out.println("Graph has: " + graph.getVCount() + " vertices and " + graph.getECount() + " edges");
 		return graph;
 	}
 	
 	
 	public static void main(String[] args) {
 		try {
 			PerformaceTest test = new PerformaceTest();
 			Graph graph = test.createGraph();
 			
 			System.out.println("Starting search");
 			long startTime = System.currentTimeMillis();
 			
			CountHypergraphSearchAlgorithm algo = new CountHypergraphSearchAlgorithm();
 			algo.run(graph.getFirstVertex());
 			
 			System.out.println("Applied BFS in " + (System.currentTimeMillis() - startTime) + " milliseconds");
 			
 		} catch (RemoteException ex) {
 			ex.printStackTrace();
 		}
 	}
 	
 }
