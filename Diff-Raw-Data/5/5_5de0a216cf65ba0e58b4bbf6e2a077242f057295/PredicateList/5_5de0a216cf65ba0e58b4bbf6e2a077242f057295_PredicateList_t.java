 package net.cscott.sdr.calls;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 import java.util.Set;
 
 import org.junit.runner.RunWith;
 
 import net.cscott.jdoctest.JDoctestRunner;
 import net.cscott.sdr.calls.TaggedFormation.Tag;
 import net.cscott.sdr.calls.ast.Apply;
 import net.cscott.sdr.calls.ast.Condition;
 import net.cscott.sdr.calls.ast.ParCall;
 import net.cscott.sdr.util.Fraction;
 
 /** This class contains all the predicates known to the system. */
 @RunWith(value=JDoctestRunner.class)
 public abstract class PredicateList {
     
     // zero-arg operators
     /**
      * Always true.
      * @doc.test
      *  js> ds = new DanceState(new DanceProgram(Program.C4), Formation.SQUARED_SET); undefined;
      *  js> c = net.cscott.sdr.calls.ast.AstNode.valueOf('(Condition true)');
      *  (Condition true)
      *  js> c.getPredicate().evaluate(ds.dance, ds.currentFormation(), c)
      *  true
      */
     public final static Predicate TRUE = new _Predicate("true") {
         @Override
         public boolean evaluate(DanceProgram ds, Formation f, Condition c) {
             assert c.args.isEmpty();
             return true;
         }        
     };
     /**
      * Always false.
      * @doc.test
      *  js> ds = new DanceState(new DanceProgram(Program.C4), Formation.SQUARED_SET); undefined;
      *  js> c = net.cscott.sdr.calls.ast.AstNode.valueOf('(Condition false)');
      *  (Condition false)
      *  js> c.getPredicate().evaluate(ds.dance, ds.currentFormation(), c)
      *  false
      */
     public final static Predicate FALSE = new _Predicate("false") {
         @Override
         public boolean evaluate(DanceProgram ds, Formation f, Condition c) {
             assert c.args.isEmpty();
             return false;
         }
     };
     // one-arg operators
     /**
      * Boolean negation.
      * @doc.test
      *  js> ds = new DanceState(new DanceProgram(Program.C4), Formation.SQUARED_SET); undefined;
      *  js> c = net.cscott.sdr.calls.ast.AstNode.valueOf('(Condition not (Condition false))');
      *  (Condition not (Condition false))
      *  js> c.getPredicate().evaluate(ds.dance, ds.currentFormation(), c)
      *  true
      *  js> c = net.cscott.sdr.calls.ast.AstNode.valueOf('(Condition not (Condition true))');
      *  (Condition not (Condition true))
      *  js> c.getPredicate().evaluate(ds.dance, ds.currentFormation(), c)
      *  false
      */
     public final static Predicate NOT = new _Predicate("not") {
         @Override
         public boolean evaluate(DanceProgram ds, Formation f, Condition c) {
             assert c.args.size()==1;
             Condition arg = c.getArg(0);
             return !arg.getPredicate().evaluate(ds,f,arg);
         }
     };
     // string/numeric literals
     public final static Predicate LITERAL = new _Predicate("literal") {
         @Override
         public boolean evaluate(DanceProgram ds, Formation f, Condition c) {
             // a little bit of magic for our two zero-arg predicates
             if (c.args.size()==1 && c.getArg(0).args.isEmpty()) {
                 if (c.getArg(0).predicate.equalsIgnoreCase("false"))
                     return FALSE.evaluate(ds, f, c.getArg(0));
                 if (c.getArg(0).predicate.equalsIgnoreCase("true"))
                     return TRUE.evaluate(ds, f, c.getArg(0));
             }
             throw new IllegalArgumentException("type mismatch");
         }
         @Override
         public String evaluateAsString(DanceProgram ds, Formation f, Condition c) {
             assert c.args.size()==1;
             Condition arg = c.getArg(0);
             assert arg.args.size()==0;
             return arg.predicate;
         }
     };
     // thunk to allow computation in Apply nodes
     public final static Predicate CALL = new _Predicate("call") {
         @Override
         public boolean evaluate(DanceProgram ds, Formation f, Condition c) {
             throw new IllegalArgumentException("type mismatch");
         }
         @Override
         public String evaluateAsString(DanceProgram ds, Formation f, Condition c) {
             throw new IllegalArgumentException("type mismatch");
         }
         @Override
         public Fraction evaluateAsNumber(DanceProgram ds, Formation f, Condition c) {
             assert c.args.size()==1;
             return cond2apply(c).getNumberArg(0);
         }
         private Apply cond2apply(Condition c) {
             List<Apply> args = new ArrayList<Apply>(c.args.size());
             for (Condition arg : c.args)
                 args.add(cond2apply(arg));
             return new Apply(c.predicate, args);
         }
     };
     // binary numerical operators
     /**
      * Numerical equality.
      * @doc.test
      *  js> ds = new DanceState(new DanceProgram(Program.C4), Formation.SQUARED_SET); undefined;
      *  js> c = net.cscott.sdr.calls.ast.AstNode.valueOf('(Condition equal (Condition literal (Condition "1 1/2")) (Condition literal (Condition "1 1/2")))');
      *  (Condition equal (Condition literal (Condition 1 1/2)) (Condition literal (Condition 1 1/2)))
      *  js> c.getPredicate().evaluate(ds.dance, ds.currentFormation(), c)
      *  true
      *  js> c = net.cscott.sdr.calls.ast.AstNode.valueOf('(Condition equal (Condition literal (Condition "1 1/2")) (Condition literal (Condition 2)))');
      *  (Condition equal (Condition literal (Condition 1 1/2)) (Condition literal (Condition 2)))
      *  js> c.getPredicate().evaluate(ds.dance, ds.currentFormation(), c)
      *  false
      */
     public final static Predicate EQUAL = new _Predicate("equal") {
         @Override
         public boolean evaluate(DanceProgram ds, Formation f, Condition c) {
             assert c.args.size()==2;
             Fraction f1 = c.getNumberArg(0, ds, f);
             Fraction f2 = c.getNumberArg(1, ds, f);
             return f1.equals(f2);
         }
     };
     /**
      * Numerical comparison.
      * @doc.test
      *  js> ds = new DanceState(new DanceProgram(Program.C4), Formation.SQUARED_SET); undefined;
      *  js> c = net.cscott.sdr.calls.ast.AstNode.valueOf('(Condition greater (Condition literal (Condition "1 1/2")) (Condition literal (Condition "1 1/2")))');
      *  (Condition greater (Condition literal (Condition 1 1/2)) (Condition literal (Condition 1 1/2)))
      *  js> c.getPredicate().evaluate(ds.dance, ds.currentFormation(), c)
      *  false
      *  js> c = net.cscott.sdr.calls.ast.AstNode.valueOf('(Condition greater (Condition literal (Condition 2)) (Condition literal (Condition "1 1/2")))');
      *  (Condition greater (Condition literal (Condition 2)) (Condition literal (Condition 1 1/2)))
      *  js> c.getPredicate().evaluate(ds.dance, ds.currentFormation(), c)
      *  true
      */
     public final static Predicate GREATER = new _Predicate("greater") {
         @Override
         public boolean evaluate(DanceProgram ds, Formation f, Condition c) {
             assert c.args.size()==2;
             Fraction f1 = c.getNumberArg(0, ds, f);
             Fraction f2 = c.getNumberArg(1, ds, f);
             return f1.compareTo(f2) > 0;
         }
     };
     // n-ary operators.
     /**
      * Short-circuit boolean conjunction.
      * @doc.test
      *  js> ds = new DanceState(new DanceProgram(Program.C4), Formation.SQUARED_SET); undefined;
      *  js> c = net.cscott.sdr.calls.ast.AstNode.valueOf('(Condition and (Condition true) (Condition true) (Condition false))');
      *  (Condition and (Condition true) (Condition true) (Condition false))
      *  js> c.getPredicate().evaluate(ds.dance, ds.currentFormation(), c)
      *  false
      *  js> c = net.cscott.sdr.calls.ast.AstNode.valueOf('(Condition and (Condition true) (Condition true) (Condition true))');
      *  (Condition and (Condition true) (Condition true) (Condition true))
      *  js> c.getPredicate().evaluate(ds.dance, ds.currentFormation(), c)
      *  true
      *  js> // short-circuits
      *  js> c = net.cscott.sdr.calls.ast.AstNode.valueOf('(Condition and (Condition false) (Condition bogus))');
      *  (Condition and (Condition false) (Condition bogus))
      *  js> c.getPredicate().evaluate(ds.dance, ds.currentFormation(), c)
      *  false
      */
     public final static Predicate AND = new _Predicate("and") {
         @Override
         public boolean evaluate(DanceProgram ds, Formation f, Condition c) {
             assert c.args.size()>0;
             boolean result = true;
             for (Condition cc : c.args) {
                 result = cc.getPredicate().evaluate(ds, f, cc);
                 if (!result) break; // short-circuit operator.
             }
             return result;
         }
     };
     /**
      * Short-circuit boolean disjunction.
      * @doc.test
      *  js> ds = new DanceState(new DanceProgram(Program.C4), Formation.SQUARED_SET); undefined;
      *  js> c = net.cscott.sdr.calls.ast.AstNode.valueOf('(Condition or (Condition false) (Condition false) (Condition false))');
      *  (Condition or (Condition false) (Condition false) (Condition false))
      *  js> c.getPredicate().evaluate(ds.dance, ds.currentFormation(), c)
      *  false
      *  js> c = net.cscott.sdr.calls.ast.AstNode.valueOf('(Condition or (Condition false) (Condition false) (Condition true))');
      *  (Condition or (Condition false) (Condition false) (Condition true))
      *  js> c.getPredicate().evaluate(ds.dance, ds.currentFormation(), c)
      *  true
      *  js> // short-circuits
      *  js> c = net.cscott.sdr.calls.ast.AstNode.valueOf('(Condition or (Condition true) (Condition bogus))');
      *  (Condition or (Condition true) (Condition bogus))
      *  js> c.getPredicate().evaluate(ds.dance, ds.currentFormation(), c)
      *  true
      */
     public final static Predicate OR = new _Predicate("or") {
         @Override
         public boolean evaluate(DanceProgram ds, Formation f, Condition c) {
             assert c.args.size()>0;
             boolean result = false;
             for (Condition cc : c.args) {
                 result = cc.getPredicate().evaluate(ds, f, cc);
                 if (result) break; // short-circuit operator.
             }
             return result;
         }
     };
     // okay, square-dance-specific operators.
     /**
      * Check the current dance program level.
      * @doc.test
      *  js> ds = new DanceState(new DanceProgram(Program.PLUS), Formation.SQUARED_SET); undefined;
      *  js> c = net.cscott.sdr.calls.ast.AstNode.valueOf('(Condition program at least (Condition literal (Condition BASIC)))');
      *  (Condition program at least (Condition literal (Condition BASIC)))
      *  js> c.getPredicate().evaluate(ds.dance, ds.currentFormation(), c)
      *  true
      *  js> c = net.cscott.sdr.calls.ast.AstNode.valueOf('(Condition program at least (Condition literal (Condition A2)))');
      *  (Condition program at least (Condition literal (Condition A2)))
      *  js> c.getPredicate().evaluate(ds.dance, ds.currentFormation(), c)
      *  false
      */
     public final static Predicate PROGRAM_AT_LEAST = new _Predicate("program at least") {
         @Override
         public boolean evaluate(DanceProgram ds, Formation f, Condition c) {
             assert c.args.size()==1;
             Program p = Program.valueOf(c.getStringArg(0, ds, f).toUpperCase());
             return ds.getProgram().includes(p);
         }
     };
     /**
      * Check the order of the selected dancers within the given formation.
      * @doc.test
      *  js> FormationList = FormationListJS.initJS(this); undefined;
      *  js> SD = StandardDancer; undefined
      *  js> // rotate the formation 1/2 just to get rid of the original tags
      *  js> f = FormationList.RH_OCEAN_WAVE; f.toStringDiagram()
      *  ^    v    ^    v
      *  js> // label those dancers
      *  js> f= f.mapStd([SD.COUPLE_1_BOY, SD.COUPLE_1_GIRL,
      *    >              SD.COUPLE_3_BOY, SD.COUPLE_3_GIRL]); f.toStringDiagram()
      *  1B^  1Gv  3B^  3Gv
      *  js> ds = new DanceState(new DanceProgram(Program.PLUS), f); undefined;
      *  js> function test(sel, pat) {
      *    >   let c = net.cscott.sdr.calls.ast.AstNode.valueOf(
      *    >           '(Condition SELECTION PATTERN '+
      *    >           '(Condition literal (Condition '+sel+')) '+
      *    >           '(Condition literal (Condition '+pat+')))');
      *    >    return c.getPredicate().evaluate(ds.dance, ds.currentFormation(), c);
      *    > }
      *  js> test('BOY', '____')
      *  false
      *  js> test('BOY', 'x_x_')
      *  true
      *  js> test('BOY', '_x_x')
      *  false
      *  js> test('CENTER', '_xx_')
      *  true
      *  js> test('HEAD', 'xxxx')
      *  true
      *  js> test('SIDE', '____')
      *  true
      *  js> test('COUPLE 1', 'xx__')
      *  true
      *  js> test('SIDE', '_xx_')
      *  false
      */
     public final static Predicate SELECTION_PATTERN = new _Predicate("selection pattern") {
         @Override
         public boolean evaluate(DanceProgram ds, Formation f, Condition c) {
             List<String> args = new ArrayList<String>(c.args.size());
             for (int i=0; i<c.args.size()-1; i++)
                 args.add(c.getStringArg(i, ds, f));
             Set<Tag> tags = ParCall.parseTags(args);
             String pattern = c.getStringArg(c.args.size()-1, ds, f);
             if (pattern.length() != f.dancers().size())
                 return false;
             // check each dancer against the corresponding character in the
             // pattern.
             TaggedFormation tf = TaggedFormation.coerce(f);
             int i=0;
             for (Dancer d : f.sortedDancers()) {
                 boolean t1 = tf.isTagged(d, tags);
                 boolean t2 = pattern.charAt(i++) != '_';
                 if (t1!=t2) return false;
             }
             return true;
         }
     };
     /** Check whether the tagged dancers are t-boned.
      * @doc.test
      *  js> importPackage(net.cscott.sdr.util); // for Fraction
      *  js> FormationList = FormationListJS.initJS(this); undefined;
      *  js> SD = StandardDancer; undefined
      *  js> // rotate the formation 1/2 just to get rid of the original tags
      *  js> f = FormationList.RH_OCEAN_WAVE; f.toStringDiagram()
      *  ^    v    ^    v
      *  js> d = [d for (d in Iterator(f.sortedDancers()))]; undefined
      *  js> f = f.move(d[1], f.location(d[1]).turn
      *    >                 (Fraction.ONE_QUARTER, false)) ; f.toStringDiagram()
      *  ^    <    ^    v
      *  js> f = f.move(d[3], f.location(d[3]).turn
      *    >                 (Fraction.ONE_QUARTER, false)) ; f.toStringDiagram()
      *  ^    <    ^    <
      *  js> // label those dancers
      *  js> f= f.mapStd([SD.COUPLE_1_BOY, SD.COUPLE_1_GIRL,
      *    >              SD.COUPLE_3_BOY, SD.COUPLE_3_GIRL]); f.toStringDiagram()
      *  1B^  1G<  3B^  3G<
      *  js> ds = new DanceState(new DanceProgram(Program.PLUS), f); undefined;
      *  js> function test(sel) {
      *    >   let c = net.cscott.sdr.calls.ast.AstNode.valueOf(
      *    >           '(Condition TBONED '+
      *    >           '(Condition literal (Condition '+sel+')))');
      *    >    return c.getPredicate().evaluate(ds.dance, ds.currentFormation(), c);
      *    > }
      *  js> test('BOY')
      *  false
      *  js> test('GIRL')
      *  false
      *  js> test('CENTER')
      *  true
      *  js> test('END')
      *  true
      *  js> test('HEAD')
      *  true
      */
     public final static Predicate TBONED = new _Predicate("tboned") {
         @Override
         public boolean evaluate(DanceProgram ds, Formation f, Condition c) {
             List<String> args = new ArrayList<String>(c.args.size());
             for (int i=0; i<c.args.size(); i++)
                 args.add(c.getStringArg(i, ds, f));
             Set<Tag> tags = ParCall.parseTags(args);
             // ok, look at rotation directions for the selected dancers.
             Rotation r = null;
             // each selected dancer must have all of these tags
             TaggedFormation tf = TaggedFormation.coerce(f);
             for (Dancer d: f.selectedDancers()) {
                 if (!tf.isTagged(d, tags))
                     continue;
                 if (r==null) {
                     r = tf.location(d).facing;
                     r = r.union(r.add(Fraction.ONE_HALF)); // fuzz
                 } else {
                     if (!r.includes(tf.location(d).facing))
                         return true; // yes, it's t-boned
                 }
             }
             return false; // nope, not t-boned
         }
     };
     /** Check that the tagged dancers also have some other tag. */
     public final static Predicate ARE = new _Predicate("are") {
         @Override
         public boolean evaluate(DanceProgram ds, Formation f, Condition c) {
             assert c.args.size()==2;
             String left = c.getStringArg(0, ds, f);
             String right = c.getStringArg(1, ds, f);
 
             Set<Tag> leftTags = ParCall.parseTags
                 (Arrays.asList(left.split(",\\s*")));
             Set<Tag> rightTags = ParCall.parseTags
                 (Arrays.asList(right.split(",\\s*")));
 
             TaggedFormation tf = TaggedFormation.coerce(f);
             Set<Dancer> leftDancers = tf.tagged(leftTags);
             Set<Dancer> rightDancers = tf.tagged(rightTags);
 
             return rightDancers.containsAll(leftDancers);
         }
     };
     /** Check that all dancers have the specified tag. */
     public final static Predicate ALL = new _Predicate("all") {
         @Override
         public boolean evaluate(DanceProgram ds, Formation f, Condition c) {
             assert c.args.size()==1;
             return ARE.evaluate(ds, f, Condition.makeCondition
                      ("are",
                       Condition.makeCondition("literal",
                                               Condition.makeCondition("ALL")),
                       c.getArg(0)));
         }
     };
 
     /** Check the identify of a call provided as an argument.
      *  Used in a hack to implement "boys trade".
      */
     public final static Predicate CALL_IS = new _Predicate("call is") {
         /** This is just a case-insensitive string comparison, really. */
         @Override
         public boolean evaluate(DanceProgram ds, Formation f, Condition c) {
             assert c.args.size() == 2;
             assert c.getArg(0).predicate.equals("literal") ||
                    c.getArg(0).predicate.equals("call");
             assert c.getArg(1).predicate.equals("literal") ||
                    c.getArg(1).predicate.equals("call");
             return condEquals(c.getArg(0).getArg(0), c.getArg(1).getArg(0));
         }
         private boolean condEquals(Condition c1, Condition c2) {
             if (!c1.predicate.equalsIgnoreCase(c2.predicate))
                 return false;
             if (c1.args.size() != c2.args.size())
                 return false;
             for (int i=0; i<c1.args.size(); i++)
                 if (!condEquals(c1.args.get(i), c2.args.get(i)))
                     return false;
             return true;
         }
     };
 
     // helper class ////////////////////////////////////
     private static abstract class _Predicate extends Predicate {
         private final String name;
         _Predicate(String name) { this.name = name; }
         public String getName() { return name; }
     }
 }
