 package org.clafer.choco.constraint.propagator;
 
 import gnu.trove.iterator.TIntIntIterator;
 import gnu.trove.map.hash.TIntIntHashMap;
 import java.util.Arrays;
 import solver.constraints.Propagator;
 import solver.constraints.PropagatorPriority;
 import solver.exception.ContradictionException;
 import solver.variables.EventType;
 import solver.variables.IntVar;
 import solver.variables.SetVar;
 import solver.variables.Variable;
 import util.ESat;
 
 /**
  *
  * @author jimmy
  */
 public class PropJoinFunctionCard extends Propagator<Variable> {
 
     private final SetVar take;
     private final IntVar takeCard;
     private final IntVar[] refs;
     private final IntVar toCard;
     private final Integer globalCardinality;
 
     public PropJoinFunctionCard(SetVar take, IntVar takeCard, IntVar[] refs, IntVar toCard, Integer globalCardinality) {
         super(buildArray(take, takeCard, toCard, refs), PropagatorPriority.LINEAR, false);
         this.take = take;
         this.takeCard = takeCard;
         this.refs = refs;
         this.toCard = toCard;
         this.globalCardinality = globalCardinality;
     }
 
     private static Variable[] buildArray(SetVar take, IntVar takeCard, IntVar toCard, IntVar[] refs) {
         Variable[] array = new Variable[refs.length + 3];
         array[0] = take;
         array[1] = takeCard;
         array[2] = toCard;
         System.arraycopy(refs, 0, array, 3, refs.length);
         return array;
     }
 
     private boolean isTakeVar(int idx) {
         return idx == 0;
     }
 
     private boolean isTakeCardVar(int idx) {
         return idx == 1;
     }
 
     private boolean isToCardVar(int idx) {
         return idx == 2;
     }
 
     private boolean isRefVar(int idx) {
         return idx >= 3;
     }
 
     private int getRefVarIndex(int idx) {
         assert isRefVar(idx);
         return idx - 3;
     }
 
     public boolean hasGlobalCardinality() {
         return globalCardinality != null;
     }
 
     public int getGlobalCardinality() {
         assert hasGlobalCardinality();
         return globalCardinality.intValue();
     }
 
     @Override
     public int getPropagationConditions(int vIdx) {
         if (isTakeVar(vIdx)) {
             return EventType.ADD_TO_KER.mask + EventType.REMOVE_FROM_ENVELOPE.mask;
         }
         if (isRefVar(vIdx)) {
             return EventType.INSTANTIATE.mask;
         }
         assert isTakeCardVar(vIdx) || isToCardVar(vIdx);
         return EventType.BOUND.mask + EventType.INSTANTIATE.mask;
     }
 
     private int countAdditionalSameRefsAllowed(TIntIntHashMap map) {
         assert hasGlobalCardinality();
         int gc = getGlobalCardinality();
 
         int allowed = 0;
 
         if (gc != 1) {
             TIntIntIterator iter = map.iterator();
             for (int i = map.size(); i-- > 0;) {
                 iter.advance();
                 assert iter.value() > 0;
                 assert iter.value() <= gc;
                 allowed += gc - iter.value();
             }
             assert !iter.hasNext();
         }
 
         return allowed;
     }
 
     private TIntIntHashMap countRefs() {
         TIntIntHashMap map = new TIntIntHashMap(take.getKernelSize());
         for (int i = take.getKernelFirst(); i != SetVar.END; i = take.getKernelNext()) {
             IntVar ref = refs[i];
             if (ref.instantiated()) {
                 map.adjustOrPutValue(ref.getValue(), 1, 1);
             }
         }
         return map;
     }
 
     private TIntIntHashMap constrainGlobalCardinality() throws ContradictionException {
         assert hasGlobalCardinality();
         int[] ker = PropUtil.iterateKer(take);
         TIntIntHashMap map = new TIntIntHashMap(ker.length);
         for (int i = 0; i < ker.length; i++) {
             constrainGlobalCardinality(ker, i, i, map);
         }
         return map;
     }
 
     private void constrainGlobalCardinality(int[] ker, int index, int explored, TIntIntHashMap map) throws ContradictionException {
         assert hasGlobalCardinality();
         assert index <= explored;
         assert explored < ker.length;
 
         IntVar a = refs[ker[index]];
         if (a.instantiated()) {
             int value = a.getValue();
             int count = map.adjustOrPutValue(value, 1, 1);
             int gc = getGlobalCardinality();
 
             if (count == gc) {
                 for (int j = 0; j < explored; j++) {
                     IntVar b = refs[ker[j]];
                     if (!b.instantiatedTo(value) && b.removeValue(value, aCause)) {
                         constrainGlobalCardinality(ker, j, explored, map);
                     }
                 }
                 for (int j = explored + 1; j < ker.length; j++) {
                     refs[ker[j]].removeValue(value, aCause);
                 }
             } else if (count > gc) {
                 contradiction(a, "Above global cardinality");
             }
         }
     }
 
     private static int divRoundUp(int a, int b) {
         assert a >= 0;
         assert b > 0;
 
         return (a + b - 1) / b;
     }
 
     @Override
     public void propagate(int evtmask) throws ContradictionException {
         takeCard.updateLowerBound(take.getKernelSize(), aCause);
         takeCard.updateUpperBound(take.getEnvelopeSize(), aCause);
         boolean changed;
         do {
             changed = false;
             TIntIntHashMap map = hasGlobalCardinality() ? constrainGlobalCardinality() : countRefs();
             int instCard = map.size();
             int kerSize = take.getKernelSize();
 
             int minUninstantiated;
             int maxUninstantiated;
             int minCard;
             int maxCard;
             boolean cardChanged;
             do {
                 cardChanged = false;
 //                minUninstantiated = Math.max(0, takeCard.getLB() - kerSize);
 //                maxUninstantiated = Math.max(0, takeCard.getUB() - kerSize);
                 int kerUninstantiated = 0;
                 for (int i = take.getEnvelopeFirst(); i != SetVar.END; i = take.getEnvelopeNext()) {
                     if (!refs[i].instantiated()) {
                         if (take.kernelContains(i)) {
                             kerUninstantiated++;
                         }
                     }
                 }
                 assert takeCard.getLB() >= kerSize;
                 minUninstantiated = takeCard.getLB() - kerSize + kerUninstantiated;
                 maxUninstantiated = takeCard.getUB() - kerSize + kerUninstantiated;
                 minCard = instCard
                         + (hasGlobalCardinality()
                         ? divRoundUp(Math.max(0, minUninstantiated - countAdditionalSameRefsAllowed(map)), getGlobalCardinality())
                         : 0);
                 maxCard = instCard + maxUninstantiated;
 
                 toCard.updateLowerBound(minCard, aCause);
                 toCard.updateUpperBound(maxCard, aCause);
 
                 if (hasGlobalCardinality()) {
                     cardChanged |= takeCard.updateUpperBound(toCard.getUB() * getGlobalCardinality(), aCause);
                 }
                cardChanged |= takeCard.updateLowerBound(toCard.getLB(), aCause);
             } while (cardChanged);
 
 
             if (maxUninstantiated != 0) {
                 if (instCard == toCard.getUB()) {
                     // The rest must be duplicates.
                     for (int i = take.getKernelFirst(); i != SetVar.END; i = take.getKernelNext()) {
                         IntVar ref = refs[i];
                         assert !ref.instantiated() || map.contains(ref.getValue());
                         if (!ref.instantiated()) {
                             changed |= PropUtil.domainSubsetOf(ref, map.keySet(), aCause) && ref.instantiated();
                         }
                     }
                 }
                 if (maxCard == toCard.getLB()) {
                     // No more duplicate values.
                     for (int i = take.getKernelFirst(); i != SetVar.END; i = take.getKernelNext()) {
                         IntVar ref = refs[i];
                         if (!ref.instantiated()) {
                             TIntIntIterator iter = map.iterator();
                             for (int j = map.size(); j-- > 0;) {
                                 iter.advance();
                                 changed |= ref.removeValue(iter.key(), aCause) && ref.instantiated();
                             }
                             assert !iter.hasNext();
                         }
                     }
                 }
             }
         } while (changed);
     }
 
     @Override
     public void propagate(int idxVarInProp, int mask) throws ContradictionException {
         forcePropagate(EventType.FULL_PROPAGATION);
     }
 
     @Override
     public ESat isEntailed() {
         TIntIntHashMap map = new TIntIntHashMap();
         int gc = hasGlobalCardinality() ? getGlobalCardinality() : Integer.MAX_VALUE;
         for (int i = take.getKernelFirst(); i != SetVar.END; i = take.getKernelNext()) {
             IntVar ref = refs[i];
             if (ref.instantiated()) {
                 if (map.adjustOrPutValue(ref.getValue(), 1, 1) > gc) {
                     return ESat.FALSE;
                 }
             }
         }
 
         int instCard = map.size();
 
         int uninstantiated = 0;
         map.clear();
         int minUninstantiated = Math.max(0, takeCard.getLB() - take.getKernelSize());
         int maxUninstantiated = Math.max(0, takeCard.getUB() - take.getKernelSize());
         for (int i = take.getEnvelopeFirst(); i != SetVar.END; i = take.getEnvelopeNext()) {
             if (!refs[i].instantiated()) {
                 if (take.kernelContains(i)) {
                     minUninstantiated++;
                 }
                 maxUninstantiated++;
             }
         }
         int minCard = instCard
                 + (hasGlobalCardinality()
                 ? divRoundUp(Math.max(0, minUninstantiated - countAdditionalSameRefsAllowed(map)), getGlobalCardinality())
                 : 0);
         int maxCard = instCard + maxUninstantiated;
 
         if (toCard.getUB() < minCard) {
             return ESat.FALSE;
         }
         if (toCard.getLB() > maxCard) {
             return ESat.FALSE;
         }
 
         return uninstantiated == 0 ? ESat.TRUE : ESat.UNDEFINED;
     }
 
     @Override
     public String toString() {
         return "joinFunctionCard(" + take + ", " + takeCard + ", " + Arrays.toString(refs) + ", " + toCard + ", " + globalCardinality + ")";
     }
 }
