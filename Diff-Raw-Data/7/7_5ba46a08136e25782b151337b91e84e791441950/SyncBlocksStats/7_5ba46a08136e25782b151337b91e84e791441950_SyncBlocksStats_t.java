 package tools.syncBlockStats;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.ObjectOutputStream;
 import java.io.RandomAccessFile;
 import java.util.HashSet;
 import java.util.List;
 import java.util.ArrayList;
 import java.util.Set;
 import java.util.Stack;
 import java.util.concurrent.ConcurrentHashMap;
 
 
 import rr.meta.ClassInfo;
 import rr.meta.SourceLocation;
 import acme.util.decorations.Decoration;
 import acme.util.decorations.DecorationFactory;
 import acme.util.decorations.DefaultValue;
 import acme.util.option.CommandLine;
 import acme.util.option.CommandLineOption;
 import rr.tool.Tool;
 import rr.event.AccessEvent;
 import rr.event.AcquireEvent;
 import rr.event.ArrayAccessEvent;
 import rr.event.InterruptEvent;
 import rr.event.JoinEvent;
 import rr.event.MethodEvent;
 import rr.event.ReleaseEvent;
 import rr.event.SleepEvent;
 import rr.event.VolatileAccessEvent;
 import rr.event.WaitEvent;
 import rr.state.ShadowThread;
 import rr.state.ShadowVar;
 import rr.event.FieldAccessEvent;
 
 
 import org.jgrapht.DirectedGraph;
 import org.jgrapht.Graph;
 import org.jgrapht.alg.CycleDetector;
 import org.jgrapht.alg.DijkstraShortestPath;
 import org.jgrapht.alg.StrongConnectivityInspector;
 import org.jgrapht.graph.*;
 
 
 
 
 
 
 public class SyncBlocksStats extends Tool {
 
 /*----------------------------CLASS VARIABLES-----------------------*/
 	private static boolean testOutput = false;
 	
 	//mapping from thread to thread local information (locks, accessed fields)
 	private static DecorationFactory<ShadowThread> fac = new DecorationFactory<ShadowThread>();
 	private static Decoration<ShadowThread, ThreadData> tdata =fac.make("tdata", DecorationFactory.Type.SINGLE, 
 				new DefaultValue<ShadowThread, ThreadData>(){
 					public ThreadData get(ShadowThread t) { return new ThreadData();}
 			}); 
 	
 	//mapping from program location to access counts and depths
 	private static ConcurrentHashMap<SourceLocation, Counter> pcMap = 
 			new ConcurrentHashMap<SourceLocation, Counter>();
 	
 	//state for recording partial order of accesses
 	private static DefaultDirectedGraph<Field, StaticBlock> globalGraph = 
 			new DefaultDirectedGraph<Field, StaticBlock>(StaticBlock.class);
 	
 	static int LOCKS_GRABBED = 1;
 	
 	//commandline options to specify analysis
 	CommandLineOption<Boolean> trackOrder;
 	CommandLineOption<Boolean> trackCounts;
 	CommandLineOption<String> outputName;
 	
 	
 	
 /*-----------------------------INNER CLASSES---------------------*/	
 	
 	private class AccessTracker{
 		Object o;
 		boolean r = false;
 		boolean w = false;
 		SourceLocation loc;
 		int currDepth = 0;
 		int firstRDepth = -1;
 		int firstWDepth = -1;
 		
 		public AccessTracker(AcquireEvent ae){
 			this.o = ae.getLock().getLock();
 			synchronized(ae.getInfo()){
 				SourceLocation origLoc = ae.getInfo().getLoc();
 				synchronized(origLoc){
 					this.loc = new SourceLocation(origLoc.getFile(), origLoc.getLine());
 				}
 			}
 		}
 	}
 	
 	private static class ThreadData{
 		private final Stack<AccessTracker> locks = new Stack<AccessTracker>();
 		private DefaultDirectedGraph<Field, StaticBlock> graph = new DefaultDirectedGraph<Field, StaticBlock>(StaticBlock.class);
 		private Field lastAccessed = null;
 		private HashSet<Field> seen = new HashSet<Field>();
 		public long accesses = 0;
 		public int locksGrabbed;
 		
 		public Stack<AccessTracker> getLocks(){
 			return locks;
 		}
 		
 		public Field getLastAccessed(){
 			return lastAccessed;
 		}
 		
 		public void setLastAccessed(Field f){
 			lastAccessed = f;
 		}
 		
 		public HashSet<Field> getSeen(){
 			return seen;
 		}
 		public void setSeen(HashSet<Field> seen){
 			this.seen = seen; 
 		}
 		
 	}
 	
 	private static class Counter{
 		int  Total = 0;
 		int Read = 0;
 		int Write = 0;
 		int None = 0;
 		List<Integer> depths = new ArrayList<Integer>();
 	}
 /*----------------------------TOOL IMPLEMENTATION----------------------------*/
 	
 	public SyncBlocksStats(String name, Tool next, CommandLine commandLine) {
 		super(name, next, commandLine);
 	  
 		trackOrder = CommandLine.makeBoolean(
 				"noOrder",
 				true,
 				CommandLineOption.Kind.EXPERIMENTAL,
 				"Enable tracking of order of accesses. Default value is true");
 		
 		trackCounts = CommandLine.makeBoolean(
 				"noCounts",
 				true,
 				CommandLineOption.Kind.EXPERIMENTAL,
 				"Enable tracking of read, write, neither counts per static synchronized block." +
 				"Default value is true");
 		
 		outputName = CommandLine.makeString(
 				"output",
 				"",
 				CommandLineOption.Kind.EXPERIMENTAL,
 				"Name modifier for order analysis output. -output=avrora will output the files " +
 				"avrora_graph.ser and avrora_roots.ser if the trackOrder option is set to true."
 				);
 		
 		commandLine.add(trackOrder);
 		commandLine.add(trackCounts);
 		commandLine.add(outputName);
 	}
 	
 	
 	@Override
 	public void stop(ShadowThread st){
 		ThreadData td = tdata.get(st);
 		if(td.graph.vertexSet().size() != 0){
 			unionGraph(td);
 		}
 	}
 	
 	@Override
 	public void preSleep(SleepEvent se){
 		ThreadData td = tdata.get(se.getThread());
 		if(td.graph.vertexSet().size() != 0){
 			unionGraph(td);
 		}	
 	}
 	
 	@Override
 	public void preJoin(JoinEvent je){
 		ThreadData td = tdata.get(je.getThread());
 		if(td.graph.vertexSet().size() != 0){
 			unionGraph(td);
 		}
 	}
 	
 	@Override
 	public void preInterrupt(InterruptEvent ie){
 		ThreadData td = tdata.get(ie.getThread());
 		if(td.graph.vertexSet().size() != 0){
 			unionGraph(td);
 		}
 	}
 	
 	@Override
 	public void preWait(WaitEvent we){
 		ThreadData td = tdata.get(we.getThread());
 		if(td.graph.vertexSet().size() != 0){
 			unionGraph(td);
 		}
 	}
 	
 	@Override
 	public void acquire(AcquireEvent ae){
 		if(testOutput){
 			System.out.println("thread " + ae.getThread().getTid() + " acquired " + ae.getLock().getLock().toString());
 		}
 		
 		Stack<AccessTracker> localLocks = tdata.get(ae.getThread()).getLocks();
 		AccessTracker at = new AccessTracker(ae);
 		
 		if(trackCounts.get()){
 			updateBlockTotal(at);
 		}
 		
 		localLocks.push(at);
 		
 		tdata.get(ae.getThread()).locksGrabbed++;
 	}
 	
 	private void updateBlockTotal(AccessTracker at){
 		Counter newCount = new Counter();
 		newCount.Total++;
 
 		
 		if(pcMap.putIfAbsent(at.loc, newCount) != null){
 			Counter prevCount = pcMap.get(at.loc);
 			synchronized(prevCount){
 				prevCount.Total++;
 			}
 		}
 	}
 	
 	@Override
 	public void release(ReleaseEvent re){
 		if(testOutput){
 			System.out.println("thread " + re.getThread().getTid() + " released " + re.getLock().getLock().toString());
 		}
 		ThreadData td = tdata.get(re.getThread());
 		
 		Stack<AccessTracker> localLocks = td.getLocks();
 		AccessTracker at = localLocks.pop();
 		
 		if(trackCounts.get()){
 			updateBlockCategory(at);
 		}
 		
 		if(trackOrder.get()){
 			if(td.locksGrabbed % LOCKS_GRABBED == 0){
 				unionGraph(td);
 			}
 			//reset variable tracking when finished with sync block
 			if(localLocks.size() == 0){
 				td.setLastAccessed(null);
 				td.setSeen(new HashSet<Field>());
 			}
 		}
 	}
 	
 	
 	private void updateBlockCategory(AccessTracker at){
 		Counter count = pcMap.get(at.loc);
 		
 		synchronized(count){
 			if(at.w){
 				count.Write++;
 				if(!count.depths.contains(at.firstWDepth)) count.depths.add(at.firstWDepth);
 			}
 			else if(at.r){
 				count.Read++;
 				if(!count.depths.contains(at.firstRDepth)) count.depths.add(at.firstRDepth);
 			}
 			else{
 				count.None++;
 			}
 		}
 	}
 	
 	public void unionGraph(ThreadData td){
 		synchronized(globalGraph){
 			globalGraph.union(td.graph);
 		}
 		td.graph = new DefaultDirectedGraph<Field, StaticBlock>(StaticBlock.class);
 		
 	}
 	
 	
 	@Override
 	public void access(AccessEvent ae){
 		ThreadData td = tdata.get(ae.getThread());
 		/*td.accesses++;
 		if((td.accesses % 10000) == 0) {
 			System.out.println("There are " + td.graph.vertexSet().size() + " in the graph.");
 		}*/
 		Stack<AccessTracker> localLocks = td.getLocks();
 		
 		if(trackOrder.get()){
 			//do only when in sync block
 			if(localLocks.size() != 0){
 				//get current field accessed
 				Field curr = null;
 				if(ae.getOriginalShadow() instanceof Field){
 					curr = (Field)ae.getOriginalShadow();
 				}
 				else{
 					curr = (Field)makeShadowVar(ae);
 				}
 				
 				if(!td.getSeen().contains(curr)){
 					curr.loc = localLocks.peek().loc;
 							
 					//get last field accessed in the sync block
 					Field prev = td.getLastAccessed();
 					
 					//if the current field is the first field accessed in sync block
 					if(prev == null){
 						td.setLastAccessed(curr);
 					}
 					else{
 						addAccessToGraph(td, prev, curr, prev.loc);
 						td.setLastAccessed(curr);
 					}
 					td.getSeen().add(curr);
 				}
 			}
 		}
 		
 		if(trackCounts.get()){
 			categorizeAccess(ae, localLocks);
 		}
 	}
 	
 	private void addAccessToGraph(ThreadData td, Field prev, Field curr, SourceLocation block){
 		td.graph.addVertex(prev);
 		td.graph.addVertex(curr);
 		if(DijkstraShortestPath.<Field, StaticBlock>findPathBetween(td.graph, prev, curr) == null){
 			StaticBlock e = td.graph.addEdge(prev, curr);
 			e.file = block.getFile();
 			e.line = block.getLine();
 		}
 	}
 	
 	private void categorizeAccess(AccessEvent ae, Stack<AccessTracker>localLocks){
 		Object target = ae.getTarget();
 		Object self = ae.getAccessed();
 		
 		for(AccessTracker at : localLocks){
 			if (at.o == self){
 				if(ae.isWrite()){
 					setFirstDepth(at, false);
 					at.w = true;
 				}
 				else{
 					setFirstDepth(at, true);
 					at.r = true;
 				}
 			}
 			else if(at.o == target){
 				setFirstDepth(at, true);
 				at.r = true;
 			}
 			else if(at.o instanceof Class){
 				Class<?> heldClass = (Class<?>)at.o;
 				ClassInfo cinfo = ae.getAccessInfo().getEnclosing().getOwner();
 				if(heldClass.getName().equals(cinfo.getName().replace('/', '.'))) { 
 					setFirstDepth(at, true);
 					at.r = true;
 				}
 			}
 		}
 	}
 	
 	private void setFirstDepth(AccessTracker at, boolean isRead){
 		if(isRead){
 			if(!at.r){
 				at.firstRDepth = at.currDepth;
 			}
 		}
 		else{
 			if(!at.w){
 				at.firstWDepth = at.currDepth;
 			}
 		}
 	}
 	
 	@Override
 	public void volatileAccess(VolatileAccessEvent e){
 		access(e);
 	}
 	@Override
 	public ShadowVar makeShadowVar(AccessEvent ae){
 		if(trackOrder.get()){
 			Field f = new Field();
 			if(ae.getKind() == AccessEvent.Kind.FIELD || ae.getKind() == AccessEvent.Kind.VOLATILE){
 				FieldAccessEvent fae = (FieldAccessEvent)ae;
 				f.name = fae.getInfo().getField().getName();
 			}
 			else{
 				ArrayAccessEvent aae = (ArrayAccessEvent)ae;
 				f.name = aae.getTarget().toString() + "[" + aae.getIndex() + "]";
 			}
 			return f;
 		}
 		return new ShadowVar(){};
 	}
 	
 	@Override
 	public void enter(MethodEvent me){
 		if(testOutput){
 			System.out.println(me.toString());
 		}
 		
 		if(trackCounts.get()){
 			Stack<AccessTracker> localLocks = tdata.get(me.getThread()).getLocks();
 			for(AccessTracker at : localLocks){
 				at.currDepth++;
 			}
 		}
 		
 	}
 	@Override
 	public void exit(MethodEvent me){
 		if(testOutput){
 			System.out.println(me.toString());
 		}
 		
 		if(trackCounts.get()){
 			Stack<AccessTracker> localLocks = tdata.get(me.getThread()).getLocks();
 			for(AccessTracker at : localLocks){
 				at.currDepth--;
 			}
 		}
 	}
 	
 	@Override
 	public void fini(){
 		if(trackOrder.get())
 			try {
 				synchronized(globalGraph){
 					saveOrderAnalysis();
 				}
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		if(trackCounts.get()) printCountsAnalysis();
 	}
 	
 	private void saveOrderAnalysis() throws IOException{
 		System.out.println("Number of nodes = " + globalGraph.vertexSet().size());
 		System.out.println("Number of edges = " + globalGraph.edgeSet().size());
 		
 		/*CycleDetector<Field, StaticBlock> cd = new CycleDetector<Field, StaticBlock>(globalGraph);
         Set<Field> cycles = cd.findCycles();
         System.out.println("Number of nodes in a cycle = " + cycles.size());*/
 		
         StrongConnectivityInspector<Field, StaticBlock> inspector =
             new StrongConnectivityInspector<Field, StaticBlock>(globalGraph);
         List<Set<Field>> components = inspector.stronglyConnectedSets();
         final Set<Field> cycles = getCycleSet(components);
         System.out.println("Total number of nodes involved in a loop = " + cycles.size());
        
 
 
         DirectedGraph<Field, StaticBlock> cycleGraph =
         		new DirectedMaskSubgraph<Field, StaticBlock>(
         				globalGraph,
         				new MaskFunctor<Field, StaticBlock>(){
         					public boolean isEdgeMasked(StaticBlock e){
        						return !(cycles.contains(globalGraph.getEdgeSource(e)) &&
        								cycles.contains(globalGraph.getEdgeTarget(e)));
         					}
         					public boolean isVertexMasked(Field v){
        						return !cycles.contains(v);
         					}
         				});
  
         int multipleEs = 0;
         
         for(Field f : cycles){
         	if(cycleGraph.outgoingEdgesOf(f).size() > 1) multipleEs++;
         }
 		
         System.out.println("Numbder of nodes in strongly connected component w/ > 1 out-edge = " + multipleEs);
         
         String output = outputName.get();
 		String graphName;
 		if(!output.isEmpty()){
 			graphName = output + "_graph.ser";
 		}
 		else{
 			graphName = "graph.ser";
 		}
         
 		RandomAccessFile raf = new RandomAccessFile(graphName, "rw");
 		FileOutputStream gout = new FileOutputStream(raf.getFD());
 		ObjectOutputStream graph_oos = new ObjectOutputStream(gout);
 		graph_oos.writeObject(cycleGraph);
 		graph_oos.close();
 	}
 	
 	private Set<Field> getCycleSet(List<Set<Field>> components){
 		 // A vertex participates in a cycle if either of the following is
         // true:  (a) it is in a component whose size is greater than 1
         // or (b) it is a self-loop
 		int nonTrivial = 0;
         Set<Field> set = new HashSet<Field>();
         for (Set<Field> component : components) {
             if (component.size() > 1) {
             	nonTrivial++;
             	System.out.println("Strongly connected component of size " + component.size());
                 // cycle
                 set.addAll(component);
             } else {
                 Field v = component.iterator().next();
                 if (globalGraph.containsEdge(v, v)) {
                     nonTrivial++;
                     System.out.println("Self loop");
                 	// self-loop
                     set.add(v);
                 }
             }
         }
         System.out.println("Number of non-trivial strongly connected components = " + nonTrivial);
         return set;
 	}
 	
 	private void printCountsAnalysis(){
 		synchronized(pcMap){
 			System.out.println("Number of Static Synchronized Blocks = " + pcMap.size());
 			
 			Set<SourceLocation> keys = pcMap.keySet();
 			for(SourceLocation loc : keys){
 				Counter count = pcMap.get(loc);
 				synchronized(count){
 					StringBuilder sb = new StringBuilder();
 					sb.append("[");
 					for(Integer i : count.depths){
 						sb.append(i + ",");
 					}
 					if(sb.length() > 1) sb.replace(sb.length() - 1, sb.length(), "]");
 					else sb.append("]");
 					
 					System.out.printf("result['%s line %d'] = {\"total\" : %d , \"read\" : %d, " +
 							"\"write\" : %d, \"neither\" : %d, \"depths\" : %s}\n", 
 						loc.getFile().replace('/', '.'), loc.getLine(), count.Total, count.Read, count.Write, count.None, sb.toString());
 				}
 			}
 		}
 	}
 }
