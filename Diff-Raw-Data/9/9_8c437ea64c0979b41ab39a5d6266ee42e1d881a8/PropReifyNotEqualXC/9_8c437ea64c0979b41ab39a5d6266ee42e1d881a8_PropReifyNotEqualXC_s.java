 package org.clafer.choco.constraint.propagator;
 
 import solver.constraints.Propagator;
 import solver.constraints.PropagatorPriority;
 import solver.exception.ContradictionException;
 import solver.variables.BoolVar;
 import solver.variables.EventType;
 import solver.variables.IntVar;
 import util.ESat;
 import static util.ESat.FALSE;
 import static util.ESat.TRUE;
 
 /**
  *
  * @author jimmy
  */
 public class PropReifyNotEqualXC  extends Propagator<IntVar> {
 
     private final BoolVar reify;
     private final IntVar x;
     private final int c;
 
     public PropReifyNotEqualXC(BoolVar reify, IntVar x, int c) {
         super(new IntVar[]{reify, x}, PropagatorPriority.UNARY, true);
         this.reify = reify;
         this.x = x;
         this.c = c;
     }
 
     private boolean isReifyVar(int idx) {
         return idx == 0;
     }
 
     private boolean isXVar(int idx) {
         return idx == 1;
     }
 
     @Override
     public int getPropagationConditions(int vIdx) {
         return EventType.INT_ALL_MASK();
     }
 
     private void propagateReifyVar() throws ContradictionException {
         assert reify.instantiated();
         if (reify.getValue() == 0) {
             x.instantiateTo(c, aCause);
         } else {
             x.removeValue(c, aCause);
         }
         setPassive();
     }
 
     private void propagateXVar() throws ContradictionException {
         if (x.contains(c)) {
             if (x.instantiated()) {
                 reify.setToFalse(aCause);
                 setPassive();
             }
         } else {
             reify.setToTrue(aCause);
             setPassive();
         }
     }
 
     @Override
     public void propagate(int evtmask) throws ContradictionException {
         if (reify.instantiated()) {
             propagateReifyVar();
         } else {
             propagateXVar();
         }
     }
 
     @Override
     public void propagate(int idxVarInProp, int mask) throws ContradictionException {
         if (isReifyVar(idxVarInProp)) {
             propagateReifyVar();
         } else {
             assert isXVar(idxVarInProp);
             propagateXVar();
         }
     }
 
     @Override
     public ESat isEntailed() {
         switch (reify.getBooleanValue()) {
             case TRUE:
                 if (!x.contains(c)) {
                     return ESat.TRUE;
                 }
                 if (x.instantiated()) {
                     return ESat.FALSE;
                 }
                 break;
             case FALSE:
                 if (!x.contains(c)) {
                     return ESat.FALSE;
                 }
                 if (x.instantiated()) {
                     return ESat.TRUE;
                 }
                 break;
         }
         return ESat.UNDEFINED;
     }
 
     @Override
     public String toString() {
        return reify + " => (" + x + " != " + c + ")";
     }
 }
