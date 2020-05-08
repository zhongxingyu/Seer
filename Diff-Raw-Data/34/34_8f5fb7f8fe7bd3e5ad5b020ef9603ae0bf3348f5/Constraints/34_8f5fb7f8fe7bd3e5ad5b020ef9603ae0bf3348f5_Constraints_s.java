 package org.clafer.choco.constraint;
 
 import java.util.Collection;
 import org.clafer.choco.constraint.propagator.PropAnd;
 import org.clafer.choco.constraint.propagator.PropJoinRelation;
 import org.clafer.choco.constraint.propagator.PropJoinFunction;
 import org.clafer.choco.constraint.propagator.PropSelectN;
 import org.clafer.choco.constraint.propagator.PropSingleton;
 import org.clafer.choco.constraint.propagator.PropArrayToSet;
 import org.clafer.choco.constraint.propagator.PropArrayToSetCard;
 import org.clafer.choco.constraint.propagator.PropFilterString;
 import org.clafer.choco.constraint.propagator.PropIntChannel;
 import org.clafer.choco.constraint.propagator.PropIntNotMemberSet;
 import org.clafer.choco.constraint.propagator.PropJoinFunctionCard;
 import org.clafer.choco.constraint.propagator.PropJoinInjectiveRelationCard;
 import org.clafer.choco.constraint.propagator.PropLexChainChannel;
 import org.clafer.choco.constraint.propagator.PropLone;
 import org.clafer.choco.constraint.propagator.PropOne;
 import org.clafer.choco.constraint.propagator.PropOr;
 import org.clafer.choco.constraint.propagator.PropReifyEqualXC;
 import org.clafer.choco.constraint.propagator.PropReifyNotEqualXC;
 import org.clafer.choco.constraint.propagator.PropSetDifference;
 import org.clafer.choco.constraint.propagator.PropSetEqual;
 import org.clafer.choco.constraint.propagator.PropSetNotEqual;
 import org.clafer.choco.constraint.propagator.PropSetNotEqualC;
 import org.clafer.choco.constraint.propagator.PropSetSum;
 import org.clafer.choco.constraint.propagator.PropSetUnion;
 import org.clafer.choco.constraint.propagator.PropSortedSets;
 import org.clafer.choco.constraint.propagator.PropSortedSetsCard;
 import org.clafer.common.Util;
 import solver.constraints.Constraint;
 import solver.constraints.binary.PropEqualX_Y;
 import solver.constraints.set.PropCardinality;
 import solver.variables.BoolVar;
 import solver.variables.IntVar;
 import solver.variables.SetVar;
 import solver.variables.Variable;
 
 /**
  *
  * @author jimmy
  */
 public class Constraints {
 
     private Constraints() {
     }
 
    public static Constraint singleton(SetVar svar, IntVar ivar) {
        return singleton(ivar, svar);
    }

     public static Constraint singleton(IntVar ivar, SetVar svar) {
         Constraint constraint = new Constraint(new Variable[]{ivar, svar}, ivar.getSolver());
         constraint.setPropagators(new PropSingleton(ivar, svar));
         return constraint;
     }
 
     public static Constraint arrayToSet(IntVar[] ivars, SetVar svar, IntVar svarCard) {
         return arrayToSet(ivars, svar, svarCard, null);
     }
 
     public static Constraint arrayToSet(IntVar[] ivars, SetVar svar, IntVar svarCard, Integer globalCardinality) {
         Variable[] vars = new Variable[ivars.length + 2];
         vars[0] = svar;
         vars[1] = svarCard;
         System.arraycopy(ivars, 0, vars, 2, ivars.length);
         Constraint constraint = new Constraint(vars, svar.getSolver());
         constraint.setPropagators(
                 new PropArrayToSet(ivars, svar),
                 new PropArrayToSetCard(ivars, svarCard, globalCardinality),
                 new PropCardinality(svar, svarCard));
         return constraint;
     }
 
     public static Constraint reifyEqual(BoolVar reify, IntVar i, int c) {
         Constraint constraint = new Constraint(new IntVar[]{reify, i}, reify.getSolver());
         constraint.setPropagators(new PropReifyEqualXC(reify, i, c));
         return constraint;
     }
 
     public static Constraint reifyNotEqual(BoolVar reify, IntVar i, int c) {
         Constraint constraint = new Constraint(new IntVar[]{reify, i}, reify.getSolver());
         constraint.setPropagators(new PropReifyNotEqualXC(reify, i, c));
         return constraint;
     }
 
     public static Constraint equal(SetVar s1, IntVar s1Card, SetVar s2, IntVar s2Card) {
         Constraint constraint = new Constraint(new Variable[]{s1, s1Card, s2, s2Card}, s1.getSolver());
         constraint.setPropagators(new PropSetEqual(s1, s2), new PropEqualX_Y(s1Card, s2Card));
         return constraint;
     }
 
     public static Constraint notEqual(SetVar s1, SetVar s2) {
         Constraint<SetVar, PropSetNotEqual> constraint =
                 new Constraint<SetVar, PropSetNotEqual>(new SetVar[]{s1, s2}, s1.getSolver());
         constraint.setPropagators(new PropSetNotEqual(s1, s2));
         return constraint;
     }
 
     public static Constraint notEqual(SetVar s, int[] c) {
         Constraint<SetVar, PropSetNotEqualC> constraint =
                 new Constraint<SetVar, PropSetNotEqualC>(new SetVar[]{s}, s.getSolver());
         constraint.setPropagators(new PropSetNotEqualC(s, c));
         return constraint;
     }
 
     public static Constraint joinRelation(SetVar take, SetVar[] children, SetVar to) {
         SetVar[] vars = new SetVar[children.length + 2];
         vars[0] = take;
         vars[1] = to;
         System.arraycopy(children, 0, vars, 2, children.length);
         Constraint<SetVar, PropJoinRelation> constraint = new Constraint<SetVar, PropJoinRelation>(vars, take.getSolver());
         constraint.setPropagators(new PropJoinRelation(take, children, to));
         return constraint;
     }
 
     public static Constraint joinInjectiveRelation(SetVar take, IntVar takeCard, SetVar[] children, IntVar[] childrenCards, SetVar to, IntVar toCard) {
         if (children.length != childrenCards.length) {
             throw new IllegalArgumentException();
         }
         Variable[] vars = new Variable[children.length * 2 + 4];
         vars[0] = take;
         vars[1] = takeCard;
         vars[2] = to;
         vars[3] = toCard;
         System.arraycopy(children, 0, vars, 4, children.length);
         System.arraycopy(childrenCards, 0, vars, 4 + children.length, childrenCards.length);
         // Assumes take and children card is already constrained for maximum efficiency.
         Constraint constraint = new Constraint(vars, take.getSolver());
         constraint.setPropagators(
                 new PropJoinRelation(take, children, to),
                 new PropJoinInjectiveRelationCard(take, takeCard, childrenCards, toCard),
                 new PropCardinality(to, toCard));
         return constraint;
     }
 
     public static Constraint joinFunction(SetVar take, IntVar takeCard, IntVar[] refs, SetVar to, IntVar toCard) {
         return joinFunction(take, takeCard, refs, to, toCard, null);
     }
 
     public static Constraint joinFunction(SetVar take, IntVar takeCard, IntVar[] refs, SetVar to, IntVar toCard, Integer globalCardinality) {
         Variable[] vars = new Variable[refs.length + 4];
         vars[0] = take;
         vars[1] = takeCard;
         vars[2] = to;
         vars[3] = toCard;
         // Assumes take card is already constrained for maximum efficiency.
         System.arraycopy(refs, 0, vars, 4, refs.length);
         Constraint constraint = new Constraint(vars, take.getSolver());
         constraint.setPropagators(new PropJoinFunction(take, refs, to),
                 new PropJoinFunctionCard(take, takeCard, refs, toCard, globalCardinality),
                 new PropCardinality(to, toCard));
         return constraint;
     }
 
     public static Constraint selectN(BoolVar[] bools, IntVar n) {
         IntVar[] vars = new IntVar[bools.length + 1];
         System.arraycopy(bools, 0, vars, 0, bools.length);
         vars[bools.length] = n;
         Constraint constraint = new Constraint(vars, bools[0].getSolver());
         constraint.setPropagators(new PropSelectN(bools, n));
         return constraint;
     }
 
     public static Constraint intChannel(SetVar[] sets, IntVar[] ints) {
         Variable[] vars = new Variable[sets.length + ints.length];
         System.arraycopy(sets, 0, vars, 0, sets.length);
         System.arraycopy(ints, 0, vars, sets.length, ints.length);
         Constraint constraint = new Constraint(vars, sets[0].getSolver());
         constraint.setPropagators(new PropIntChannel(sets, ints));
         return constraint;
     }
 
     /**
      * <p>
      * Sum the set with a maximum cardinality. More efficient than the standard
      * operation in the Choco library when n is relatively small.
      * </p>
      * <p>
      * For example:
      * <pre>
      *   Animal 2
      *     Age -> integer
      *   [Animal.Age = 1000]
      * </pre>
      * </p>
      * <p> {@code Animal.Age} is a set with a very large envelope. However, due
      * to static analysis of the model, it is easy to see that the cardinality
      * can be no larger than 2, hence n=2. Once the first integer x is selected
      * for the set, the second integer 1000 - x is already determined due to
      * n=2. Since the Choco library's setSum constraint is not given n, it
      * cannot make this deduction.
      * </p>
      *
      * @param set the set of integers
      * @param sum the sum of the set
      * @param card the cardinality of the set
      * @return a constraint where and sum = Σ set
      */
     public static Constraint setSum(SetVar set, IntVar sum, IntVar card) {
         Constraint constraint = new Constraint(new Variable[]{set, sum, card}, set.getSolver());
         constraint.setPropagators(new PropSetSum(set, sum, card));
         return constraint;
     }
 
     public static Constraint notMember(IntVar element, SetVar set) {
         Constraint constraint = new Constraint(new Variable[]{element, set}, element.getSolver());
         constraint.setPropagators(new PropIntNotMemberSet(element, set));
         return constraint;
     }
 
     public static Constraint and(BoolVar... vars) {
         Constraint<BoolVar, PropAnd> constraint = new Constraint<BoolVar, PropAnd>(vars, vars[0].getSolver());
         constraint.setPropagators(new PropAnd(vars));
         return constraint;
     }
 
     public static Constraint lone(BoolVar... vars) {
         Constraint<BoolVar, PropLone> constraint = new Constraint<BoolVar, PropLone>(vars, vars[0].getSolver());
         constraint.setPropagators(new PropLone(vars));
         return constraint;
     }
 
     public static Constraint one(BoolVar... vars) {
         Constraint<BoolVar, PropOne> constraint = new Constraint<BoolVar, PropOne>(vars, vars[0].getSolver());
         constraint.setPropagators(new PropOne(vars));
         return constraint;
     }
 
     public static Constraint or(BoolVar... vars) {
         Constraint<BoolVar, PropOr> constraint = new Constraint<BoolVar, PropOr>(vars, vars[0].getSolver());
         constraint.setPropagators(new PropOr(vars));
         return constraint;
     }
 
     public static Constraint or(Collection<Constraint> constraints) {
         return or(constraints.toArray(new Constraint[constraints.size()]));
     }
 
     public static Constraint or(Constraint... constraints) {
         return new OrConstraint(constraints);
     }
 
     public static Constraint difference(SetVar minuend, SetVar subtrahend, SetVar difference) {
         Constraint<SetVar, PropSetDifference> constraint = new Constraint<SetVar, PropSetDifference>(new SetVar[]{minuend, subtrahend, difference}, minuend.getSolver());
         constraint.setPropagators(new PropSetDifference(minuend, subtrahend, difference));
         return constraint;
     }
 
     public static Constraint union(SetVar[] sets, SetVar union) {
         Constraint<SetVar, PropSetUnion> constraint = new Constraint<SetVar, PropSetUnion>(Util.cons(union, sets), union.getSolver());
         constraint.setPropagators(new PropSetUnion(sets, union));
         return constraint;
     }
 
     public static Constraint filterString(SetVar set, int offset, IntVar[] string, IntVar[] result) {
         Constraint<Variable, PropFilterString> constraint =
                 new Constraint<Variable, PropFilterString>(PropFilterString.buildArray(set, string, result), set.getSolver());
         constraint.setPropagators(new PropFilterString(set, offset, string, result));
         return constraint;
     }
 
     public static Constraint lexChainChannel(IntVar[][] strings, IntVar[] ints) {
         Constraint<IntVar, PropLexChainChannel> constraint =
                 new Constraint<IntVar, PropLexChainChannel>(PropLexChainChannel.buildArray(strings, ints), strings[0][0].getSolver());
         constraint.setPropagators(new PropLexChainChannel(strings, ints));
         return constraint;
     }
 
     public static Constraint sortedSets(SetVar[] sets, IntVar[] cards) {
         Constraint constraint = new Constraint(PropSortedSetsCard.buildArray(sets, cards), sets[0].getSolver());
         constraint.setPropagators(new PropSortedSets(sets));
         constraint.setPropagators(new PropSortedSetsCard(sets, cards));
         return constraint;
     }
 }
