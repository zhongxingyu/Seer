 package org.clafer.choco.constraint.propagator;
 
 import solver.constraints.Propagator;
 import solver.constraints.PropagatorPriority;
 import solver.exception.ContradictionException;
 import solver.variables.EventType;
 import solver.variables.IntVar;
 import solver.variables.SetVar;
 import solver.variables.Variable;
 import solver.variables.delta.IIntDeltaMonitor;
 import solver.variables.delta.monitor.SetDeltaMonitor;
 import util.ESat;
 import util.procedure.IntProcedure;
 
 /**
  *
  * @author jimmy
  */
 public class PropSingleton extends Propagator<Variable> {
 
     private final IntVar i;
     private final IIntDeltaMonitor iD;
     private final SetVar s;
     private final SetDeltaMonitor sD;
 
     public PropSingleton(IntVar ivar, SetVar svar) {
         super(new Variable[]{ivar, svar}, PropagatorPriority.UNARY, true);
         this.i = ivar;
         this.iD = i.monitorDelta(aCause);
         this.s = svar;
         this.sD = s.monitorDelta(aCause);
     }
 
     private boolean isIVar(int idx) {
         return idx == 0;
     }
 
     private boolean isSVar(int idx) {
         return idx == 1;
     }
 
     @Override
     public int getPropagationConditions(int vIdx) {
         if (isIVar(vIdx)) {
             return EventType.INT_ALL_MASK();
         }
         assert isSVar(vIdx);
         return EventType.ADD_TO_KER.mask + EventType.REMOVE_FROM_ENVELOPE.mask;
     }
 
     @Override
     public void propagate(int evtmask) throws ContradictionException {
         if (s.getKernelSize() > 1) {
             contradiction(s, "Singleton cannot have more than 1 element");
         } else if (s.getKernelSize() == 1) {
             int val = s.getKernelFirst();
             i.instantiateTo(val, aCause);
             s.instantiateTo(new int[]{val}, aCause);
         }
         PropUtil.intSubsetEnv(i, s, aCause);
         PropUtil.envSubsetInt(s, i, aCause);
         if (i.instantiated()) {
             s.instantiateTo(new int[]{i.getValue()}, aCause);
         } else if (s.getEnvelopeSize() == 1) {
             int val = s.getEnvelopeFirst();
             i.instantiateTo(val, aCause);
             s.instantiateTo(new int[]{val}, aCause);
         }
     }
 
     @Override
     public void propagate(int idxVarInProp, int mask) throws ContradictionException {
         if (isIVar(idxVarInProp)) {
             if (i.instantiated()) {
                 s.instantiateTo(new int[]{i.getValue()}, aCause);
             } else {
                 iD.freeze();
                 iD.forEach(pruneSOnIRem, EventType.REMOVE);
                 iD.unfreeze();
             }
         } else {
             assert isSVar(idxVarInProp);
             int sKerSize = s.getKernelSize();
             if (sKerSize > 1) {
                 contradiction(s, "Singleton cannot have more than 1 element");
             } else if (sKerSize == 1) {
                 int val = s.getKernelFirst();
                 i.instantiateTo(val, aCause);
                 s.instantiateTo(new int[]{val}, aCause);
             } else {
                 sD.freeze();
                 sD.forEach(pruneIOnSEnv, EventType.REMOVE_FROM_ENVELOPE);
                 sD.unfreeze();
                 if (i.instantiated()) {
                     s.instantiateTo(new int[]{i.getValue()}, aCause);
                 }
             }
         }
     }
     private final IntProcedure pruneSOnIRem = new IntProcedure() {
         @Override
         public void execute(int i) throws ContradictionException {
             s.removeFromEnvelope(i, aCause);
         }
     };
     private final IntProcedure pruneIOnSEnv = new IntProcedure() {
         @Override
         public void execute(int sEnv) throws ContradictionException {
             i.removeValue(sEnv, aCause);
         }
     };
 
     @Override
     public ESat isEntailed() {
         if (s.getKernelSize() > 1) {
             return ESat.FALSE;
         }
         if (s.getEnvelopeSize() < 1) {
             return ESat.FALSE;
         }
         if (PropUtil.domainIntersectEnv(i, s)) {
             return i.instantiated() && s.instantiated() ? ESat.TRUE : ESat.UNDEFINED;
         }
         return ESat.FALSE;
     }
 
     @Override
     public String toString() {
        return "singleton({" + i + "} = " + s + ")";
     }
 }
