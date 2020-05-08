 /**
  * 
  */
 package chord.analyses.snapshot;
 
 import gnu.trove.TIntObjectHashMap;
 import gnu.trove.TIntObjectProcedure;
 import gnu.trove.TObjectProcedure;
 
 import java.util.HashSet;
 import java.util.Set;
 
 import joeq.Compiler.Quad.Inst;
 import joeq.Compiler.Quad.Quad;
 import chord.bddbddb.Rel.PairIterable;
 import chord.doms.DomE;
 import chord.project.Chord;
 import chord.project.Project;
 import chord.project.analyses.ProgramDom;
 import chord.project.analyses.ProgramRel;
 import chord.util.tuple.integer.IntPair;
 import chord.util.tuple.object.Pair;
 
 /**
  * @author omert
  *
  */
 @Chord(
 	name = "ss-may-alias"
 )
 public class MayAliasAnalysis extends SnapshotAnalysis {
 	
 	private static class MayAliasQuery extends Query {
 		public final int e1;
 		public final int e2;
 
 		public MayAliasQuery(int e1, int e2) {
 			this.e1 = e1;
 			this.e2 = e2;
 		}
 
 		@Override
 		public int hashCode() {
 			final int prime = 31;
 			int result = 1;
 			result = prime * result + e1;
 			result = prime * result + e2;
 			return result;
 		}
 
 		@Override
 		public boolean equals(Object obj) {
 			if (this == obj)
 				return true;
 			if (obj == null)
 				return false;
 			if (getClass() != obj.getClass())
 				return false;
 			MayAliasQuery other = (MayAliasQuery) obj;
 			if (e1 != other.e1)
 				return false;
 			if (e2 != other.e2)
 				return false;
 			return true;
 		}
 	}
 
 	private final TIntObjectHashMap<Set<Object>> loc2abstractions = new TIntObjectHashMap<Set<Object>>();
 	protected final Set<IntPair> aliasingRacePairSet = new HashSet<IntPair>(); 
 	
 	@Override
 	public String propertyName() {
 		return "may-alias";
 	}
 	
 	@Override
 	public void processGetfieldPrimitive(int e, int t, int b, int f) {
 		updatePointsTo(e, abstraction.getValue(b));
 	}
 	
 	@Override
 	public void processPutfieldPrimitive(int e, int t, int b, int f) {
 		updatePointsTo(e, abstraction.getValue(b));
 	}
 	
 	@Override
 	public void processGetfieldReference(int e, int t, int b, int f, int o) {
 		updatePointsTo(e, abstraction.getValue(b));
 	}
 	
 	@Override
 	public void processGetstaticPrimitive(int e, int t, int b, int f) {
 		updatePointsTo(e, abstraction.getValue(b));
 	}
 	
 	@Override
 	public void processAloadPrimitive(int e, int t, int b, int i) {
 		updatePointsTo(e, abstraction.getValue(b));
 	}
 	
 	@Override
 	public void processAstorePrimitive(int e, int t, int b, int i) {
 		updatePointsTo(e, abstraction.getValue(b));
 	}
 	
 	@Override
 	public void processAloadReference(int e, int t, int b, int i, int o) {
 		updatePointsTo(e, abstraction.getValue(b));
 	}
 	
 	@Override
 	public void processAstoreReference(int e, int t, int b, int i, int o) {
 		updatePointsTo(e, abstraction.getValue(b));
 	}
 	
 	@Override
 	public void initPass() {
 		super.initPass();
 		loc2abstractions.clear();
 	}
 	
 	@Override
 	public void donePass() {
		final ProgramDom<Object> domS = new ProgramDom<Object>();
		domS.setName("S");
 		loc2abstractions.forEachValue(new TObjectProcedure<Set<Object>>() {
 			@Override
 			public boolean execute(Set<Object> arg0) {
 				domS.addAll(arg0);
 				return true;
 			}
 		});		
 		domS.save();
 		final ProgramRel absvalRel = (ProgramRel) Project.getTrgt("absval");
 		absvalRel.zero();
 		final DomE domE = instrumentor.getDomE();
 		loc2abstractions.forEachEntry(new TIntObjectProcedure<Set<Object>>() {
 			@Override
 			public boolean execute(int idx0, Set<Object> arg1) {
 				for (Object o : arg1) {
 					int idx1 = domS.indexOf(o);
 					if (idx1 != -1)
 						absvalRel.add(idx0, idx1);
 				}
 				return true;
 			}
 		});
 		absvalRel.save();
 		Project.runTask("aliasing-race-pair-dlog");
 		ProgramRel aliasingRel = (ProgramRel) Project.getTrgt("aliasingRacePair");
 		PairIterable<Inst, Inst> tuples = aliasingRel.getAry2ValTuples();
 		for (Pair<Inst, Inst> p : tuples) {
 			Quad quad0 = (Quad) p.val0;
 			int e1 = domE.indexOf(quad0);
 			Quad quad1 = (Quad) p.val1;
 			int e2 = domE.indexOf(quad1);
 			MayAliasQuery q = new MayAliasQuery(e1, e2);
 			if (shouldAnswerQueryHit(q)) {
 				answerQuery(q, true);
 			}
 		}
 	}
 	
 	@Override
 	protected boolean decideIfSelected() {
 		return true;
 	}
 
 	private void updatePointsTo(int e, Object b) {
 		if (e >= 0 && b != null) {
 			Set<Object> pts = loc2abstractions.get(e);
 			if (pts == null) {
 				pts = new HashSet<Object>();
 				loc2abstractions.put(e, pts);
 			}
 			pts.add(b);
 		}
 	}
 
 	@Override
 	public SnapshotResult takeSnapshot() {
 		return null;
 	}
 }
