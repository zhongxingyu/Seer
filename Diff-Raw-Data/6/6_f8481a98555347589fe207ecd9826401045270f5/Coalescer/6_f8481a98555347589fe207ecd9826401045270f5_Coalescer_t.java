 package org.clafer.ir.analysis;
 
 import gnu.trove.iterator.TIntIterator;
 import gnu.trove.set.TIntSet;
 import gnu.trove.set.hash.TIntHashSet;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Set;
 import org.clafer.collection.DisjointSets;
 import org.clafer.collection.Pair;
 import org.clafer.collection.Triple;
 import org.clafer.ir.IrAcyclic;
 import org.clafer.ir.IrAdd;
 import org.clafer.ir.IrAllDifferent;
 import org.clafer.ir.IrArrayToSet;
 import org.clafer.ir.IrBoolChannel;
 import org.clafer.ir.IrBoolDomain;
 import org.clafer.ir.IrBoolExpr;
 import org.clafer.ir.IrBoolExprVisitorAdapter;
 import org.clafer.ir.IrBoolVar;
 import org.clafer.ir.IrCard;
 import org.clafer.ir.IrCompare;
 import org.clafer.ir.IrDomain;
 import org.clafer.ir.IrElement;
 import org.clafer.ir.IrFilterString;
 import org.clafer.ir.IrIfOnlyIf;
 import org.clafer.ir.IrIntChannel;
 import org.clafer.ir.IrIntExpr;
 import org.clafer.ir.IrIntVar;
 import org.clafer.ir.IrJoinFunction;
 import org.clafer.ir.IrJoinRelation;
 import org.clafer.ir.IrMember;
 import org.clafer.ir.IrMinus;
 import org.clafer.ir.IrModule;
 import org.clafer.ir.IrNot;
 import org.clafer.ir.IrNotImplies;
 import org.clafer.ir.IrNotMember;
 import org.clafer.ir.IrNotWithin;
 import org.clafer.ir.IrOffset;
 import org.clafer.ir.IrRewriter;
 import org.clafer.ir.IrSelectN;
 import org.clafer.ir.IrSetExpr;
 import org.clafer.ir.IrSetTest;
 import org.clafer.ir.IrSetUnion;
 import org.clafer.ir.IrSetVar;
 import org.clafer.ir.IrSingleton;
 import org.clafer.ir.IrSortSets;
 import org.clafer.ir.IrSortStrings;
 import org.clafer.ir.IrSortStringsChannel;
 import org.clafer.ir.IrSubsetEq;
 import org.clafer.ir.IrUtil;
 import org.clafer.ir.IrWithin;
 import org.clafer.ir.Irs;
 import static org.clafer.ir.Irs.*;
 
 /**
  * @author jimmy
  */
 public class Coalescer {
 
     private Coalescer() {
     }
 
     public static Triple<Map<IrIntVar, IrIntVar>, Map<IrSetVar, IrSetVar>, IrModule> coalesce(IrModule module) {
         Pair<DisjointSets<IrIntVar>, DisjointSets<IrSetVar>> graphs = findEquivalences(module.getConstraints());
         DisjointSets<IrIntVar> intGraph = graphs.getFst();
         DisjointSets<IrSetVar> setGraph = graphs.getSnd();
         Map<IrIntVar, IrIntVar> coalescedInts = new HashMap<>();
         Map<IrSetVar, IrSetVar> coalescedSets = new HashMap<>();
 
         for (Set<IrIntVar> component : intGraph.connectedComponents()) {
             if (component.size() > 1) {
                 Iterator<IrIntVar> iter = component.iterator();
                 IrIntVar var = iter.next();
                 StringBuilder name = new StringBuilder().append(var.getName());
                 IrDomain domain = var.getDomain();
                 while (iter.hasNext()) {
                     var = iter.next();
                     name.append(';').append(var.getName());
                     domain = IrUtil.intersection(domain, var.getDomain());
                 }
                 if (domain.isEmpty()) {
                     // Model is unsatisfiable. Compile anyways?
                 } else {
                     IrIntVar coalesced = domainInt(name.toString(), domain);
                     for (IrIntVar coalesce : component) {
                         if (!coalesced.equals(coalesce)) {
                             coalescedInts.put(coalesce, coalesced);
                         }
                     }
                 }
             }
         }
         for (Set<IrSetVar> component : setGraph.connectedComponents()) {
             if (component.size() > 1) {
                 Iterator<IrSetVar> iter = component.iterator();
                 IrSetVar var = iter.next();
                 StringBuilder name = new StringBuilder().append(var.getName());
                 IrDomain env = var.getEnv();
                 IrDomain ker = var.getKer();
                 IrDomain card = var.getCard();
                 while (iter.hasNext()) {
                     var = iter.next();
                     name.append(';').append(var.getName());
                     env = IrUtil.intersection(env, var.getEnv());
                     ker = IrUtil.union(ker, var.getKer());
                     card = IrUtil.intersection(card, var.getCard());
                 }
                 IrSetVar coalesced = newSet(name.toString(), env, ker, card);
                 if (coalesced != null) {
                     for (IrSetVar coalesce : component) {
                         if (!coalesced.equals(coalesce)) {
                             coalescedSets.put(coalesce, coalesced);
                         }
                     }
                 }
             }
         }
         return new Triple<>(
                 coalescedInts,
                 coalescedSets,
                 new CoalesceRewriter(coalescedInts, coalescedSets).rewrite(module, null));
     }
 
     private static IrSetVar newSet(String name, IrDomain env, IrDomain ker, IrDomain card) {
         if (!IrUtil.isSubsetOf(ker, env) || ker.size() > env.size()) {
             // Model is unsatisfiable. Compile anyways?
         } else {
             IrDomain boundCard = IrUtil.intersection(boundDomain(ker.size(), env.size()), card);
             if (boundCard.isEmpty()) {
                 // Model is unsatisfiable. Compile anyways?
             } else {
                 return set(name.toString(), env, ker, boundCard);
             }
         }
         return null;
     }
 
     private static Pair<DisjointSets<IrIntVar>, DisjointSets<IrSetVar>> findEquivalences(Iterable<IrBoolExpr> constraints) {
         DisjointSets<IrIntVar> intGraph = new DisjointSets<>();
         DisjointSets<IrSetVar> setGraph = new DisjointSets<>();
         EquivalenceFinder finder = new EquivalenceFinder(intGraph, setGraph);
         for (IrBoolExpr constraint : constraints) {
             constraint.accept(finder, null);
         }
         return new Pair<>(intGraph, setGraph);
     }
 
     private static class EquivalenceFinder extends IrBoolExprVisitorAdapter<Void, Void> {
 
         private final DisjointSets<IrIntVar> intGraph;
         private final DisjointSets<IrSetVar> setGraph;
         private final Map<IrSetVar, IrIntVar> duplicates = new HashMap<>();
 
         private EquivalenceFinder(DisjointSets<IrIntVar> intGraph, DisjointSets<IrSetVar> setGraph) {
             this.intGraph = intGraph;
             this.setGraph = setGraph;
         }
 
         @Override
         public Void visit(IrBoolVar ir, Void a) {
             if (IrBoolDomain.BoolDomain.equals(ir.getDomain())) {
                 intGraph.union(ir, True);
             }
             return null;
         }
 
         @Override
         public Void visit(IrNot ir, Void a) {
             propagateInt(FalseDomain, ir.getExpr());
             return null;
         }
 
         @Override
         public Void visit(IrNotImplies ir, Void a) {
             propagateEqual(ir.getAntecedent(), One);
             propagateEqual(ir.getConsequent(), Zero);
             return null;
         }
 
         @Override
         public Void visit(IrIfOnlyIf ir, Void a) {
             propagateEqual(ir.getLeft(), ir.getRight());
             return null;
         }
 
         @Override
         public Void visit(IrWithin ir, Void a) {
             propagateInt(ir.getRange(), ir.getValue());
             return null;
         }
 
         @Override
         public Void visit(IrNotWithin ir, Void a) {
             propagateInt(IrUtil.difference(ir.getValue().getDomain(), ir.getRange()), ir.getValue());
             return null;
         }
 
         @Override
         public Void visit(IrCompare ir, Void a) {
             IrIntExpr left = ir.getLeft();
             IrIntExpr right = ir.getRight();
             switch (ir.getOp()) {
                 case Equal:
                     propagateEqual(left, right);
                     break;
                 case NotEqual:
                     propagateNotEqual(left, right);
                     break;
                 case LessThan:
                     propagateLessThan(left, right);
                     break;
                 case LessThanEqual:
                     propagateLessThanEqual(left, right);
                     break;
             }
             return null;
         }
 
         @Override
         public Void visit(IrSetTest ir, Void a) {
             IrSetExpr left = ir.getLeft();
             IrSetExpr right = ir.getRight();
             if (IrSetTest.Op.Equal.equals(ir.getOp())) {
                 if (left instanceof IrSetVar && right instanceof IrSetVar) {
                     setGraph.union((IrSetVar) left, (IrSetVar) right);
                 } else {
                     propagateSet(new PartialSet(left.getEnv(), left.getKer(), left.getCard()), right);
                     propagateSet(new PartialSet(right.getEnv(), right.getKer(), right.getCard()), left);
                 }
             }
             return null;
         }
 
         @Override
         public Void visit(IrMember ir, Void a) {
             IrIntExpr element = ir.getElement();
             IrSetExpr set = ir.getSet();
             propagateInt(set.getEnv(), element);
             IrDomain ker = null;
             Integer constant = IrUtil.getConstant(element);
             if (constant != null && !set.getKer().contains(constant)) {
                 ker = IrUtil.add(set.getKer(), constant);
             }
             IrDomain card = null;
             if (set.getCard().getLowBound() == 0) {
                 card = IrUtil.remove(set.getCard(), 0);
             }
             if (ker != null || card != null) {
                 propagateSet(new PartialSet(null, ker, card), set);
             }
             return null;
         }
 
         @Override
         public Void visit(IrNotMember ir, Void a) {
             IrIntExpr element = ir.getElement();
             IrSetExpr set = ir.getSet();
             IrDomain domain = IrUtil.difference(element.getDomain(), set.getKer());
             propagateInt(domain, element);
             Integer constant = IrUtil.getConstant(element);
             if (constant != null && set.getEnv().contains(constant)) {
                 propagateEnv(IrUtil.remove(set.getEnv(), constant), set);
             }
             return null;
         }
 
         @Override
         public Void visit(IrSubsetEq ir, Void a) {
             IrSetExpr sub = ir.getSubset();
             IrSetExpr sup = ir.getSuperset();
             propagateSet(new PartialSet(sup.getEnv(), null, sup.getCard()), sub);
             propagateSet(new PartialSet(null, sub.getKer(),
                     IrUtil.boundLow(sup.getCard(), sub.getCard().getLowBound())), sub);
             return null;
         }
 
         @Override
         public Void visit(IrBoolChannel ir, Void a) {
             IrBoolExpr[] bools = ir.getBools();
             IrSetExpr set = ir.getSet();
             IrDomain env = set.getEnv();
             IrDomain ker = set.getKer();
             TIntHashSet trues = new TIntHashSet(ker.size());
             TIntHashSet notFalses = new TIntHashSet(env.size());
             env.transferTo(notFalses);
             boolean changed = false;
             for (int i = 0; i < bools.length; i++) {
                 if (bools[i] instanceof IrBoolVar && !IrUtil.isConstant(bools[i])) {
                     if (!env.contains(i)) {
                         intGraph.union((IrBoolVar) bools[i], False);
                     } else if (ker.contains(i)) {
                         intGraph.union((IrBoolVar) bools[i], True);
                     }
                 }
                 if (IrUtil.isTrue(ir)) {
                     changed |= trues.add(i);
                 }
                 if (IrUtil.isFalse(bools[i])) {
                     changed |= notFalses.remove(i);
                 }
             }
             if (changed) {
                 propagateSet(new PartialSet(enumDomain(notFalses), enumDomain(trues), null), set);
             }
             return null;
         }
 
         @Override
         public Void visit(IrIntChannel ir, Void a) {
             IrIntExpr[] ints = ir.getInts();
             IrSetExpr[] sets = ir.getSets();
 
             TIntSet kers = new TIntHashSet();
 
             for (int i = 0; i < ints.length; i++) {
                 TIntSet domain = new TIntHashSet();
                 for (int j = 0; j < sets.length; j++) {
                     if (sets[j].getEnv().contains(i)) {
                         domain.add(j);
                     }
                 }
                 propagateInt(enumDomain(domain), ints[i]);
             }
             int lowCards = 0;
             int highCards = 0;
             for (IrSetExpr set : sets) {
                 set.getKer().transferTo(kers);
                 lowCards += set.getCard().getLowBound();
                 highCards += set.getCard().getHighBound();
             }
             for (int i = 0; i < sets.length; i++) {
                 TIntSet env = new TIntHashSet();
                 TIntSet ker = new TIntHashSet();
                 for (int j = 0; j < ints.length; j++) {
                     if (ints[j].getDomain().contains(i)) {
                         env.add(j);
                         if (ints[j].getDomain().size() == 1) {
                             ker.add(j);
                         }
                     }
                 }
                 env.removeAll(kers);
                 sets[i].getKer().transferTo(env);
                 IrDomain card = boundDomain(
                         ints.length - highCards + sets[i].getCard().getHighBound(),
                         ints.length - lowCards + sets[i].getCard().getLowBound());
                 propagateSet(new PartialSet(enumDomain(env), enumDomain(ker), card), sets[i]);
             }
             return null;
         }
 
         @Override
         public Void visit(IrSortStrings ir, Void a) {
             IrIntExpr[][] strings = ir.getStrings();
             for (int i = 0; i < strings.length - 1; i++) {
                 if (ir.isStrict()) {
                     propagateLessThanString(strings[i], strings[i + 1]);
                 } else {
                     propagateLessThanEqualString(strings[i], strings[i + 1]);
                 }
             }
             return null;
         }
 
         @Override
         public Void visit(IrSortSets ir, Void a) {
             IrSetExpr[] sets = ir.getSets();
             int low = 0;
             int high = 0;
             for (int i = 0; i < sets.length; i++) {
                 IrSetExpr set = sets[i];
                 IrDomain card = set.getCard();
                 int newLow = low + card.getLowBound();
                 int newHigh = high + card.getHighBound();
                 IrDomain env = boundDomain(low, newHigh - 1);
                 IrDomain ker = set.getKer();
                 if (!ker.isEmpty() && !ker.isBounded()) {
                     ker = Irs.boundDomain(ker.getLowBound(), ker.getHighBound());
                 }
                 if (high < newLow) {
                     ker = IrUtil.union(ker, boundDomain(high, newLow - 1));
                 }
                 propagateSet(new PartialSet(env, ker, null), set);
                 low = newLow;
                 high = newHigh;
             }
             return null;
         }
 
         @Override
         public Void visit(IrSortStringsChannel ir, Void a) {
             IrIntExpr[][] strings = ir.getStrings();
             IrIntExpr[] ints = ir.getInts();
             for (int i = 0; i < strings.length; i++) {
                 for (int j = i + 1; j < strings.length; j++) {
                     switch (IrUtil.compareString(strings[i], strings[j])) {
                         case EQ:
                             propagateEqual(ints[i], ints[j]);
                             break;
                         case LT:
                             propagateLessThan(ints[i], ints[j]);
                             break;
                         case LE:
                             propagateLessThanEqual(ints[i], ints[j]);
                             break;
                         case GT:
                             propagateLessThan(ints[j], ints[i]);
                             break;
                         case GE:
                             propagateLessThanEqual(ints[j], ints[i]);
                             break;
                     }
                 }
             }
             IrDomain dom = boundDomain(0, ints.length - 1);
             for (int i = 0; i < ints.length; i++) {
                 propagateInt(dom, ints[i]);
                 for (int j = i + 1; j < ints.length; j++) {
                     switch (IrUtil.compare(ints[i], ints[j])) {
                         case EQ:
                             propagateEqualString(strings[i], strings[j]);
                             break;
                         case LT:
                             propagateLessThanString(strings[i], strings[j]);
                             break;
                         case LE:
                             propagateLessThanEqualString(strings[i], strings[j]);
                             break;
                         case GT:
                             propagateLessThanString(strings[j], strings[i]);
                             break;
                         case GE:
                             propagateLessThanEqualString(strings[j], strings[i]);
                             break;
                     }
                 }
             }
             return null;
         }
 
         @Override
         public Void visit(IrAllDifferent ir, Void a) {
             IrIntExpr[] operands = ir.getOperands();
             for (int i = 0; i < operands.length; i++) {
                 for (int j = i + 1; j > operands.length; j++) {
                     propagateNotEqual(operands[i], operands[j]);
                 }
             }
             return null;
         }
 
         @Override
         public Void visit(IrSelectN ir, Void a) {
             IrBoolExpr[] bools = ir.getBools();
             IrIntExpr n = ir.getN();
             for (int i = 0; i < bools.length; i++) {
                 if (IrUtil.isTrue(bools[i]) && i >= n.getDomain().getLowBound()) {
                     propagateInt(boundDomain(i + 1, bools.length), n);
                 } else if (IrUtil.isFalse(bools[i]) && i < n.getDomain().getHighBound()) {
                     propagateInt(boundDomain(0, i), n);
                 }
             }
             for (int i = 0; i < n.getDomain().getLowBound(); i++) {
                 propagateInt(TrueDomain, bools[i]);
             }
             for (int i = n.getDomain().getHighBound(); i < bools.length; i++) {
                 propagateInt(FalseDomain, bools[i]);
             }
             return null;
         }
 
         @Override
         public Void visit(IrAcyclic ir, Void a) {
             return null;
         }
 
         @Override
         public Void visit(IrFilterString ir, Void a) {
             TIntIterator iter = ir.getSet().getEnv().iterator();
             int i = 0;
             IrDomain values = Irs.EmptyDomain;
             while (iter.hasNext()) {
                 int env = iter.next();
                 if (!ir.getSet().getKer().contains(env)) {
                     i = -1;
                 }
                 if (i >= 0) {
                     IrIntExpr string = ir.getString()[env - ir.getOffset()];
                     IrIntExpr result = ir.getResult()[i];
                     propagateEqual(string, result);
                     i++;
                 }
                 values = IrUtil.union(values, ir.getString()[env - ir.getOffset()].getDomain());
             }
             for (int j = 0; j < ir.getSet().getCard().getLowBound(); j++) {
                 propagateInt(values, ir.getResult()[j]);
             }
             for (int j = ir.getSet().getCard().getHighBound(); j < ir.getResult().length; j++) {
                 propagateInt(constantDomain(-1), ir.getResult()[j]);
             }
             return null;
         }
 
         private void propagateEqual(IrIntExpr left, IrIntExpr right) {
             Pair<IrIntExpr, IrSetVar> cardinality = AnalysisUtil.getAssignCardinality(left, right);
             if (cardinality != null) {
                 IrIntExpr cardExpr = cardinality.getFst();
                 IrSetVar setVar = cardinality.getSnd();
 
                 if (cardExpr instanceof IrIntVar) {
                     IrIntVar cardVar = (IrIntVar) cardExpr;
                     IrIntVar duplicate = duplicates.put(setVar, cardVar);
                     if (duplicate != null) {
                         intGraph.union(cardVar, duplicate);
                         return;
                     }
                 }
             }
             if (left instanceof IrIntVar && right instanceof IrIntVar) {
                 intGraph.union((IrIntVar) left, (IrIntVar) right);
             } else {
                 propagateInt(left.getDomain(), right);
                 propagateInt(right.getDomain(), left);
             }
         }
 
         private void propagateEqualString(IrIntExpr[] a, IrIntExpr[] b) {
             for (int i = 0; i < a.length; i++) {
                 propagateEqual(a[i], b[i]);
             }
         }
 
         private void propagateNotEqual(IrIntExpr left, IrIntExpr right) {
             Integer constant = IrUtil.getConstant(left);
             if (constant != null) {
                 IrDomain minus = IrUtil.remove(right.getDomain(), constant);
                 propagateInt(minus, right);
             }
             constant = IrUtil.getConstant(right);
             if (constant != null) {
                 IrDomain minus = IrUtil.remove(left.getDomain(), constant);
                 propagateInt(minus, left);
             }
         }
 
         private void propagateLessThan(IrIntExpr left, IrIntExpr right) {
             IrDomain leftDomain = left.getDomain();
             IrDomain rightDomain = right.getDomain();
             if (leftDomain.getHighBound() >= rightDomain.getHighBound()) {
                 propagateInt(IrUtil.boundHigh(leftDomain, rightDomain.getHighBound() - 1), left);
             }
             if (rightDomain.getLowBound() <= leftDomain.getLowBound()) {
                 propagateInt(IrUtil.boundLow(rightDomain, leftDomain.getLowBound() + 1), right);
             }
         }
 
         private void propagateLessThanString(IrIntExpr[] a, IrIntExpr[] b) {
             propagateLessThanString(a, b, 0);
         }
 
         private void propagateLessThanString(IrIntExpr[] a, IrIntExpr[] b, int index) {
             assert a.length == b.length;
             if (index == a.length) {
                 // Model is unsatisfiable. Compile anyways?
             }
             switch (IrUtil.compare(a[index], b[index])) {
                 case EQ:
                     propagateLessThanString(a, b, index + 1);
                     return;
                 case LT:
                     return;
                 case GT:
                     // Model is unsatisfiable. Compile anyways?
                     return;
                 case LE:
                 case GE:
                 case UNKNOWN:
                     switch (IrUtil.compareString(a, b, index + 1)) {
                         case EQ:
                         case GT:
                         case GE:
                             propagateLessThan(a[index], b[index]);
                             return;
                         case LT:
                         case LE:
                         case UNKNOWN:
                             propagateLessThanEqual(a[index], b[index]);
                             return;
                         default:
                             throw new IllegalStateException();
                     }
                 default:
                     throw new IllegalStateException();
             }
         }
 
         private void propagateLessThanEqual(IrIntExpr left, IrIntExpr right) {
             IrDomain leftDomain = left.getDomain();
             IrDomain rightDomain = right.getDomain();
             if (leftDomain.getHighBound() > rightDomain.getHighBound()) {
                 propagateInt(IrUtil.boundHigh(left.getDomain(), right.getDomain().getHighBound()), left);
             }
             if (rightDomain.getLowBound() < leftDomain.getLowBound()) {
                 propagateInt(IrUtil.boundLow(right.getDomain(), left.getDomain().getLowBound()), right);
             }
         }
 
         private void propagateLessThanEqualString(IrIntExpr[] a, IrIntExpr[] b) {
             propagateLessThanEqualString(a, b, 0);
         }
 
         private void propagateLessThanEqualString(IrIntExpr[] a, IrIntExpr[] b, int index) {
             assert a.length == b.length;
             if (index == a.length) {
                 return;
             }
             switch (IrUtil.compare(a[index], b[index])) {
                 case EQ:
                     propagateLessThanEqualString(a, b, index + 1);
                     return;
                 case LT:
                     return;
                 case GT:
                     // Model is unsatisfiable. Compile anyways?
                     return;
                 case LE:
                 case GE:
                 case UNKNOWN:
                     switch (IrUtil.compareString(a, b, index + 1)) {
                         case EQ:
                         case LT:
                         case LE:
                         case GE:
                         case UNKNOWN:
                             propagateLessThanEqual(a[index], b[index]);
                             return;
                         case GT:
                             propagateLessThan(a[index], b[index]);
                             return;
                         default:
                             throw new IllegalStateException();
                     }
                 default:
                     throw new IllegalStateException();
             }
         }
 
         private void propagateInt(IrDomain left, IrIntExpr right) {
             if (IrUtil.isSubsetOf(right.getDomain(), left)) {
                 return;
             }
             if (right instanceof IrIntVar) {
                 IrDomain domain = IrUtil.intersection(left, right.getDomain());
                if (domain.isEmpty()) {
                    // Model is unsatisfiable. Compile anyways?
                } else {
                    intGraph.union((IrIntVar) right, domainInt("domain" + domain, domain));
                }
             } else if (right instanceof IrMinus) {
                 propagateInt(IrUtil.minus(left), ((IrMinus) right).getExpr());
             } else if (right instanceof IrCard) {
                 propagateCard(left, ((IrCard) right).getSet());
             } else if (right instanceof IrAdd) {
                 IrAdd add = (IrAdd) right;
                 IrIntExpr[] addends = add.getAddends();
                 if (addends.length == 1) {
                     propagateInt(IrUtil.offset(left, -add.getOffset()), addends[0]);
                 } else {
                     for (IrIntExpr addend : addends) {
                         IrDomain domain = addend.getDomain();
                         IrDomain bound =
                                 IrUtil.intersection(
                                 boundDomain(
                                 left.getLowBound() - right.getDomain().getHighBound() + domain.getHighBound(),
                                 left.getHighBound() - right.getDomain().getLowBound() + domain.getLowBound()),
                                 domain);
                         propagateInt(bound, addend);
                     }
                 }
             } else if (right instanceof IrElement) {
                 IrElement element = (IrElement) right;
                 TIntHashSet domain = new TIntHashSet(element.getIndex().getDomain().size());
                 TIntIterator iter = element.getIndex().getDomain().iterator();
                 while (iter.hasNext()) {
                     int val = iter.next();
                     if (IrUtil.intersects(left, element.getArray()[val].getDomain())) {
                         domain.add(val);
                     }
                 }
                 propagateInt(enumDomain(domain), element.getIndex());
             }
         }
 
         private void propagateSet(PartialSet left, IrSetExpr right) {
             left.updateMask(right.getEnv(), right.getKer(), right.getCard());
             if (left.hasMask()) {
                 if (right instanceof IrSetVar) {
                     propagateSetVar(left, (IrSetVar) right);
                 } else if (right instanceof IrSingleton) {
                     propagateSingleton(left, (IrSingleton) right);
                 } else if (right instanceof IrArrayToSet) {
                     propagateArrayToSet(left, (IrArrayToSet) right);
                 } else if (right instanceof IrJoinRelation) {
                     propagateJoinRelation(left, (IrJoinRelation) right);
                 } else if (right instanceof IrJoinFunction) {
                     propagateJoinFunction(left, (IrJoinFunction) right);
                 } else if (right instanceof IrSetUnion) {
                     propagateSetUnion(left, (IrSetUnion) right);
                 } else if (right instanceof IrOffset) {
                     propagateOffset(left, (IrOffset) right);
                 }
             }
         }
 
         private void propagateSetVar(PartialSet left, IrSetVar right) {
             IrDomain env = right.getEnv();
             IrDomain ker = right.getKer();
             IrDomain card = right.getCard();
             if (left.isEnvMask()) {
                 env = left.getEnv();
             }
             if (left.isKerMask()) {
                 ker = left.getKer();
             }
             if (left.isCardMask()) {
                 card = left.getCard();
             }
             IrSetVar set = newSet(left.toString(), env, ker, card);
             if (set != null) {
                 setGraph.union(right, set);
             }
         }
 
         private void propagateSingleton(PartialSet left, IrSingleton right) {
             if (left.isKerMask()) {
                 IrDomain ker = left.getKer();
                 if (ker.size() == 1) {
                     propagateInt(ker, right.getValue());
                 }
             } else if (left.isEnvMask()) {
                 IrDomain env = left.getEnv();
                 propagateInt(env, right.getValue());
             }
         }
 
         private void propagateArrayToSet(PartialSet left, IrArrayToSet right) {
             if (left.isEnvMask()) {
                 IrDomain env = left.getEnv();
                 for (IrIntExpr child : right.getArray()) {
                     propagateInt(env, child);
                 }
             }
             if (left.isKerMask()) {
                 TIntIterator iter = IrUtil.difference(left.getKer(), right.getKer()).iterator();
                 while (iter.hasNext()) {
                     int val = iter.next();
                     IrIntExpr index = null;
                     for (IrIntExpr operand : right.getArray()) {
                         if (operand.getDomain().contains(val)) {
                             if (index != null) {
                                 index = null;
                                 break;
                             }
                             index = operand;
                         }
                     }
                     if (index != null) {
                         propagateInt(constantDomain(val), index);
                     }
                 }
             }
         }
 
         private void propagateJoinRelation(PartialSet left, IrJoinRelation right) {
             if (right.isInjective()) {
                 if (left.isEnvMask() || left.isCardMask()) {
                     IrDomain env = left.getEnv();
                     IrDomain card = left.isCardMask() ? boundDomain(0, left.getCard().getHighBound()) : null;
                     TIntIterator iter = right.getTake().getKer().iterator();
                     PartialSet set = new PartialSet(env, null, card);
                     while (iter.hasNext()) {
                         propagateSet(set, right.getChildren()[iter.next()]);
                     }
                 }
                 if (left.isKerMask()) {
                     TIntIterator iter = IrUtil.difference(left.getKer(), right.getKer()).iterator();
                     while (iter.hasNext()) {
                         int val = iter.next();
                         TIntIterator env = right.getTake().getEnv().iterator();
                         int index = -1;
                         while (env.hasNext()) {
                             int j = env.next();
                             if (right.getChildren()[j].getEnv().contains(val)) {
                                 if (index != -1) {
                                     index = -1;
                                     break;
                                 }
                                 index = j;
                             }
                         }
                         if (index != -1) {
                             propagateKer(constantDomain(index), right.getTake());
                             propagateKer(constantDomain(val), right.getChildren()[index]);
                         }
                     }
                 }
                 if (left.isCardMask()) {
                     IrSetExpr take = right.getTake();
                     IrSetExpr[] children = right.getChildren();
                     int lb = left.getCard().getLowBound();
                     int ub = left.getCard().getHighBound();
                     int[] envLbs = new int[take.getEnv().size() - take.getKer().size()];
                     int[] envUbs = new int[envLbs.length];
                     int kerMinCard = 0;
                     int kerMaxCard = 0;
                     int env = 0;
                     TIntIterator iter = take.getEnv().iterator();
                     while (iter.hasNext()) {
                         int i = iter.next();
                         if (take.getKer().contains(i)) {
                             kerMinCard += children[i].getCard().getLowBound();
                             kerMaxCard += children[i].getCard().getHighBound();
                         } else {
                             envLbs[env] = children[i].getCard().getLowBound();
                             envUbs[env] = children[i].getCard().getHighBound();
                             env++;
                         }
                     }
                     Arrays.sort(envLbs);
                     Arrays.sort(envUbs);
                     int i;
                     for (i = 0; i < envLbs.length && (kerMinCard < ub || envLbs[i] == 0); i++) {
                         kerMinCard += envLbs[i];
                     }
                     int high = i + take.getKer().size();
                     for (i = envUbs.length - 1; i >= 0 && kerMaxCard < lb; i--) {
                         kerMaxCard += envUbs[i];
                     }
                     int low = envUbs.length - 1 - i + take.getKer().size();
                     if (low > take.getCard().getLowBound() || high < take.getCard().getHighBound()) {
                         propagateCard(boundDomain(low, high), take);
                     }
                 }
             }
         }
 
         private void propagateJoinFunction(PartialSet left, IrJoinFunction right) {
             if (left.isEnvMask()) {
                 IrDomain env = left.getEnv();
                 TIntIterator iter = right.getTake().getKer().iterator();
                 while (iter.hasNext()) {
                     propagateInt(env, right.getRefs()[iter.next()]);
                 }
             }
             if (left.isKerMask()) {
                 TIntIterator iter = IrUtil.difference(left.getKer(), right.getKer()).iterator();
                 while (iter.hasNext()) {
                     int val = iter.next();
                     TIntIterator env = right.getTake().getEnv().iterator();
                     int index = -1;
                     while (env.hasNext()) {
                         int j = env.next();
                         if (right.getRefs()[j].getDomain().contains(val)) {
                             if (index != -1) {
                                 index = -1;
                                 break;
                             }
                             index = j;
                         }
                     }
                     if (index != -1) {
                         propagateKer(constantDomain(index), right.getTake());
                         propagateInt(constantDomain(val), right.getRefs()[index]);
                     }
                 }
             }
             if (left.isCardMask()) {
                 IrDomain card = left.getCard();
                 IrSetExpr take = right.getTake();
                 int low = Math.max(take.getKer().size(), card.getLowBound());
                 int high = Math.min(take.getEnv().size(),
                         right.hasGlobalCardinality()
                         ? card.getHighBound() * right.getGlobalCardinality()
                         : take.getCard().getHighBound());
                 if (low > take.getCard().getLowBound() || high < take.getCard().getHighBound()) {
                     propagateCard(boundDomain(low, high), take);
                 }
             }
         }
 
         private void propagateSetUnion(PartialSet left, IrSetUnion right) {
             if (left.isEnvMask() || left.isCardMask()) {
                 IrSetExpr[] operands = right.getOperands();
                 IrDomain env = left.getEnv();
                 if (right.isDisjoint() && left.isCardMask()) {
                     int lowCards = 0;
                     int highCards = 0;
                     for (IrSetExpr operand : operands) {
                         lowCards += operand.getCard().getLowBound();
                         highCards += operand.getCard().getHighBound();
                     }
                     for (IrSetExpr operand : operands) {
                         IrDomain card = boundDomain(
                                 left.getCard().getLowBound() - highCards + operand.getCard().getHighBound(),
                                 left.getCard().getHighBound() - lowCards + operand.getCard().getLowBound());
                         PartialSet set = new PartialSet(env, null, card);
                         propagateSet(set, operand);
                     }
                 } else {
                     IrDomain card = left.isCardMask() ? boundDomain(0, left.getCard().getHighBound()) : null;
                     PartialSet set = new PartialSet(env, null, card);
                     for (IrSetExpr operand : operands) {
                         propagateSet(set, operand);
                     }
                 }
             }
             if (left.isKerMask()) {
                 TIntIterator iter = IrUtil.difference(left.getKer(), right.getKer()).iterator();
                 while (iter.hasNext()) {
                     int val = iter.next();
                     IrSetExpr index = null;
                     for (IrSetExpr operand : right.getOperands()) {
                         if (operand.getEnv().contains(val)) {
                             if (index != null) {
                                 index = null;
                                 break;
                             }
                             index = operand;
                         }
                     }
                     if (index != null) {
                         propagateKer(constantDomain(val), index);
                     }
                 }
             }
         }
 
         private void propagateOffset(PartialSet left, IrOffset right) {
             int offset = right.getOffset();
             IrDomain env = left.isEnvMask() ? IrUtil.offset(left.getEnv(), offset) : null;
             IrDomain ker = left.isKerMask() ? IrUtil.offset(left.getKer(), offset) : null;
             IrDomain card = left.isCardMask() ? left.getCard() : null;
             propagateSet(new PartialSet(env, ker, card), right.getSet());
         }
 
         private void propagateEnv(IrDomain left, IrSetExpr right) {
             propagateSet(env(left), right);
         }
 
         private void propagateKer(IrDomain left, IrSetExpr right) {
             propagateSet(ker(left), right);
         }
 
         private void propagateCard(IrDomain left, IrSetExpr right) {
             propagateSet(card(left), right);
         }
     }
 
     private static class CoalesceRewriter extends IrRewriter<Void> {
 
         private final Map<IrIntVar, IrIntVar> coalescedInts;
         private final Map<IrSetVar, IrSetVar> coalescedSets;
 
         CoalesceRewriter(Map<IrIntVar, IrIntVar> coalescedInts, Map<IrSetVar, IrSetVar> coalescedSets) {
             this.coalescedInts = coalescedInts;
             this.coalescedSets = coalescedSets;
         }
 
         @Override
         public IrBoolVar visit(IrBoolVar ir, Void a) {
             IrBoolVar var = (IrBoolVar) coalescedInts.get(ir);
             return var == null ? ir : var;
         }
 
         @Override
         public IrIntVar visit(IrIntVar ir, Void a) {
             IrIntVar var = coalescedInts.get(ir);
             return var == null ? ir : var;
         }
 
         @Override
         public IrSetVar visit(IrSetVar ir, Void a) {
             IrSetVar var = coalescedSets.get(ir);
             return var == null ? ir : var;
         }
     }
 
     private static PartialSet env(IrDomain env) {
         return new PartialSet(env, null, null);
     }
 
     private static PartialSet ker(IrDomain ker) {
         return new PartialSet(null, ker, null);
     }
 
     private static PartialSet card(IrDomain card) {
         return new PartialSet(null, null, card);
     }
 
     private static class PartialSet {
 
         private final IrDomain env;
         private final IrDomain ker;
         private final IrDomain card;
         private byte mask;
 
         PartialSet(IrDomain env, IrDomain ker, IrDomain card) {
             assert env != null || ker != null || card != null;
             this.env = env;
             this.ker = ker;
             this.card = card;
         }
 
         IrDomain getEnv() {
             return env;
         }
 
         IrDomain getKer() {
             return ker;
         }
 
         IrDomain getCard() {
             return card;
         }
 
         boolean isEnvMask() {
             return (mask & 1) == 1;
         }
 
         boolean isKerMask() {
             return (mask & 2) == 2;
         }
 
         boolean isCardMask() {
             return (mask & 4) == 4;
         }
 
         boolean hasMask() {
             return mask != 0;
         }
 
         void updateMask(IrDomain env, IrDomain ker, IrDomain card) {
             if (this.env != null && !IrUtil.isSubsetOf(env, this.env)) {
                 mask |= 1;
             }
             if (this.ker != null && !IrUtil.isSubsetOf(this.ker, ker)) {
                 mask |= 2;
             }
             if (this.card != null && !IrUtil.isSubsetOf(card, this.card)) {
                 mask |= 4;
             }
         }
 
         @Override
         public String toString() {
             return (env != null ? "env=" + env : "")
                     + (ker != null ? "ker=" + ker : "")
                     + (card != null ? "card=" + card : "");
         }
     }
 }
