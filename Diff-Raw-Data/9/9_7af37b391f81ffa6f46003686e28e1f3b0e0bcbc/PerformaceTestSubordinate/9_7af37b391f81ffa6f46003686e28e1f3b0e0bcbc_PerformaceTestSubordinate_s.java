 package de.uni_koblenz.jgralabtest.dhht;
 
 import java.rmi.RemoteException;
 import java.util.ArrayList;
 
 import de.uni_koblenz.jgralab.Edge;
 import de.uni_koblenz.jgralab.Graph;
 import de.uni_koblenz.jgralab.GraphElement;
 import de.uni_koblenz.jgralab.Vertex;
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
 import de.uni_koblenz.jgralab.impl.mem.GraphElementImpl;
 import de.uni_koblenz.jgralab.impl.mem.SubordinateGraphImpl;
 
 public class PerformaceTestSubordinate {
 	
 	private static int activityCount = 874002;
 	
 	private static int ruleCount = 1541;
 	
 	private static int featureCount = 123074;
 	
 	private static int processCount = 1383;
 	
 	private static int linkCount = 1000000;
 
 	
 	private static int subordinateGraphCount =1;
 
 	
 	ArrayList<Activity> activityList = new ArrayList<Activity>(activityCount*subordinateGraphCount);
 	ArrayList<Feature> featureList = new ArrayList<Feature>(featureCount*subordinateGraphCount);
 	ArrayList<BusinessProcess> processList = new ArrayList<BusinessProcess>(processCount*subordinateGraphCount);
 	ArrayList<TransformationRule> ruleList = new ArrayList<TransformationRule>(ruleCount*subordinateGraphCount);
 	ArrayList<SubordinateGraphImpl> subordinateGraphs = new ArrayList<SubordinateGraphImpl>(subordinateGraphCount*subordinateGraphCount);
 	ArrayList<BusinessProcess> toplevelProcesses = new ArrayList<BusinessProcess>(subordinateGraphCount*subordinateGraphCount);
 	
 	
 	private final void addToSubordinateGraph(GraphElement elem, int i) throws RemoteException {
 		int sgn = i % subordinateGraphCount;
 		//if (sgn==0)  //no subordinate member
 		//	return;
 		if (elem instanceof Edge)
 			toplevelProcesses.get(sgn).addSubordinateElement((Edge) elem);
 		else 
 			toplevelProcesses.get(sgn).addSubordinateElement((Vertex) elem);
 	}
 	
 	private final void setSigma(GraphElement elem, int i) throws RemoteException {
 		int sgn = i % subordinateGraphCount;
 		if (sgn==0)  //no subordinate member
 			return;
 		((GraphElementImpl)elem).setSigma((GraphElementImpl) toplevelProcesses.get(sgn-1));
 	}
 	
 	public static void main(String[] args) {
 		
 		try {
 			PerformaceTestSubordinate pt = new PerformaceTestSubordinate();
 			Graph g = pt.createGraph();
 			pt.search(g);
 		} catch (RemoteException ex) {
 			ex.printStackTrace();
 		}
 	}	
 	
 	public Graph createGraph() throws RemoteException {
 		DHHTTestGraph graph = DHHTTestSchema.instance().createDHHTTestGraphInMem();
 		System.out.println("Creating example DHHTGraph");
 		long startTime = System.currentTimeMillis();
 		for (int j=0;j<subordinateGraphCount;j++) {
 			System.out.println("Creating Elements for subordinate graph " + j);
 			BusinessProcess p = graph.createBusinessProcess();
 			((GraphElementImpl) p).setKappa(1);
 			toplevelProcesses.add(p);
 			//subordinateGraphs.add((SubordinateGraphImpl) p.getSubordinateGraph());
 		
 			for (int i=0; i<activityCount;i++) {
 				Activity a = graph.createActivity();
 				activityList.add(a);
 				((GraphElementImpl) a).setKappa(0);
 				((GraphElementImpl)a).setSigma((GraphElementImpl)p);
 			}
 			for (int i=0; i<featureCount;i++) {
 				Feature f = graph.createFeature();
 				featureList.add(f);
 				((GraphElementImpl) f).setKappa(0);
 				((GraphElementImpl)f).setSigma((GraphElementImpl)p);
 			}
 			for (int i=0; i<processCount;i++) {
 				BusinessProcess b= graph.createBusinessProcess();
 				processList.add(b);
 				((GraphElementImpl) b).setKappa(0);
 				((GraphElementImpl)b).setSigma((GraphElementImpl)p);
 			}
 			for (int i=0; i<ruleCount;i++) {
 				TransformationRule r = graph.createTransformationRule();
 				ruleList.add(r);
 				((GraphElementImpl) r).setKappa(0);
 				((GraphElementImpl)r).setSigma((GraphElementImpl)p);
 			}
 		}
 		
 		System.out.println("Creating edges");
 		for (int i=0; i<linkCount; i++) {
 			FeatureTraceabilityLink l = graph.createFeatureTraceabilityLink();
 			l.connect(FeatureTraceabilityLink_activity.class, activityList.get(i%(activityCount*subordinateGraphCount)));
 			l.connect(FeatureTraceabilityLink_feature.class, featureList.get((i)%(featureCount*subordinateGraphCount)));
 			l.connect(FeatureTraceabilityLink_feature.class, featureList.get((i+1)%(featureCount*subordinateGraphCount)));
 			l.connect(FeatureTraceabilityLink_process.class, processList.get((i+1)%(processCount*subordinateGraphCount)));
 			l.connect(FeatureTraceabilityLink_process.class, processList.get((i+2)%(processCount*subordinateGraphCount)));
 			l.connect(TraceabilityLink_rule.class, ruleList.get(i%(ruleCount*subordinateGraphCount)));
 			((GraphElementImpl) l).setSigma((GraphElementImpl) activityList.get(i%(activityCount*subordinateGraphCount)).getSigma());
 				((GraphElementImpl) l).setKappa(0);
 
 		}
 		System.out.println("Creating subordinate graph objects");
 		for (int i =0; i<subordinateGraphCount;i++) {
 			subordinateGraphs.add((SubordinateGraphImpl) toplevelProcesses.get(i).getSubordinateGraph());
 		}
 		
 
 		long time = System.currentTimeMillis() - startTime;
 		System.out.println("Sucessfully created graph in " + time + " milliseconds");
 		System.out.println("Graph has: " + graph.getVCount() + " vertices and " + graph.getECount() + " edges");
 		
 		return graph;
 	}
 	
 	public void search(Graph graph) throws RemoteException {	
 		System.out.println("Starting search");
 		long startTime = System.currentTimeMillis();
		CountHypergraphSearchAlgorithm algo = new CountHypergraphSearchAlgorithm();
 		subordinateGraphs.get(0).useAsTraversalContext();
 		System.out.println("SubordinateGraph1: "  + subordinateGraphs.get(0));
 		System.out.println(" firstV: "  + subordinateGraphs.get(0).getFirstVertex());
 		System.out.println(" vCount: "  + subordinateGraphs.get(0).getVCount());
 
 		algo.run(subordinateGraphs.get(0).getFirstVertex());
 		long time = System.currentTimeMillis() - startTime;
 		System.out.println("Applied BFS on hypergraph in " + time + " milliseconds");
 		
 		System.out.println("Visited " + algo.getVertexCount() + " of " + graph.getVCount() + " vertices");
 		System.out.println("Visited " + algo.getEdgeCount() + " of " + graph.getECount() + " edges");
 	}
 	
 }
