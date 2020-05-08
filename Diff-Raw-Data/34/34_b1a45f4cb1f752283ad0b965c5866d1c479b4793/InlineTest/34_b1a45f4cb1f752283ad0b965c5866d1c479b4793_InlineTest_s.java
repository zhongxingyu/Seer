 package org.sigwinch.xacml.output.sat;
 
 import junit.framework.Test;
 import junit.framework.TestSuite;
 
 import org.sigwinch.xacml.tree.ConstantValuePredicate;
 import org.sigwinch.xacml.tree.EnvironmentalPredicate;
 import org.sigwinch.xacml.tree.FunctionCallPredicate;
 import org.sigwinch.xacml.tree.Predicate;
 import org.sigwinch.xacml.tree.SimplePredicate;
 import org.sigwinch.xacml.tree.SolePredicate;
 import org.sigwinch.xacml.tree.Triple;
 import org.sigwinch.xacml.tree.VariableReference;
 
 /**
  * InlineTest.java Created: Sat Dec 20 01:34:28 2003
  * 
  * @author Graham Hughes
  * @version
  */
 
 public class InlineTest extends XACMLTestCase {
     public static Test suite() {
         return new TestSuite(InlineTest.class);
     }
 
     public void testSimple() {
         Triple t = new Triple(a, b, c);
         t.walk(out);
         assertTriple(a, b, c, t);
     }
 
     public void testAnd() {
         Predicate p = a.andWith(b);
         Predicate d = b.andWith(c);
         Predicate e = p.andWith(c);
         Triple t = new Triple(p, d, e);
         t.walk(out);
         assertTriple(new And(a, b), new And(b, c), new And(new And(a, b), c), t);
     }
 
     public void testOr() {
         Predicate p = a.orWith(b);
         Predicate d = b.orWith(c);
         Predicate e = p.orWith(c);
         Triple t = new Triple(p, d, e);
         t.walk(out);
         assertTriple(new Or(a, b), new Or(b, c), new Or(new Or(a, b), c), t);
     }
 
     public void testTrueFalse() {
         Triple t = new Triple(SimplePredicate.TRUE, SimplePredicate.FALSE, c);
         t.walk(out);
         assertTriple(PrimitiveBoolean.TRUE, PrimitiveBoolean.FALSE, c, t,
                 new BooleanFormula[] { new Not(PrimitiveBoolean.FALSE),
                         PrimitiveBoolean.TRUE });
     }
 
     public void testAndOrFunctions() {
         FunctionCallPredicate and = buildFunction("and", new Predicate[] { a,
                 b, c });
         FunctionCallPredicate or = buildFunction("or", new Predicate[] { a, b,
                 c });
         FunctionCallPredicate not = buildFunction("not", new Predicate[] { c });
         Triple t = new Triple(and, or, not);
         t.walk(out);
         assertTriple(new And(new BooleanFormula[] { a, b, c }), new Or(
                 new BooleanFormula[] { a, b, c }), new Not(c), t);
     }
 
     public void testEquality() {
         out.setMultiplicity(4); // to avoid frame conditions
         EnvironmentalPredicate foo = new EnvironmentalPredicate("string", "foo");
         EnvironmentalPredicate bar = new EnvironmentalPredicate("string", "bar");
         ConstantValuePredicate first = new ConstantValuePredicate("string",
                 "first");
         ConstantValuePredicate second = new ConstantValuePredicate("string",
                 "second");
         FunctionCallPredicate scalar = buildFunction("string-equal",
                 new Predicate[] { first, second });
         FunctionCallPredicate set = buildFunction("string-set-equals",
                 new Predicate[] { foo, bar });
         Triple t = new Triple(scalar, set, c);
         t.walk(out);
         assertTriple(new And(buildEquivalence(out.getNamesFor(first), out
                 .getNamesFor(second))), new And(buildEquivalence(out
                 .getNamesFor(foo), out.getNamesFor(bar))), c, t);
     }
 
     public void testSubset() {
         EnvironmentalPredicate foo = new EnvironmentalPredicate("string", "foo");
         EnvironmentalPredicate bar = new EnvironmentalPredicate("string", "bar");
         FunctionCallPredicate subset = buildFunction("string-subset",
                 new Predicate[] { foo, bar });
         Triple t = new Triple(subset, b, c);
         t.walk(out);
         VariableReference[] foos = out.getNamesFor(foo);
         VariableReference[] bars = out.getNamesFor(bar);
         BooleanFormula[] subsetbf = new BooleanFormula[3];
         for (int i = 0; i < 3; i++) {
             subsetbf[i] = new Implication(foos[i], bars[i]);
         }
         assertTriple(new And(subsetbf), b, c, t);
     }
 
     public void testAtLeastOne() {
         EnvironmentalPredicate foo = new EnvironmentalPredicate("string", "foo");
         EnvironmentalPredicate bar = new EnvironmentalPredicate("string", "bar");
         FunctionCallPredicate atleast = buildFunction(
                 "string-at-least-one-member-of", new Predicate[] { foo, bar });
         Triple t = new Triple(atleast, b, c);
         t.walk(out);
         VariableReference[] foos = out.getNamesFor(foo);
         VariableReference[] bars = out.getNamesFor(bar);
         BooleanFormula[] atbf = new BooleanFormula[3];
         for (int i = 0; i < 3; i++) {
             atbf[i] = new And(foos[i], bars[i]);
         }
         assertTriple(new Or(atbf), b, c, t);
     }
 
     public void testConstants() {
         out.setMultiplicity(6);
         ConstantValuePredicate foo = new ConstantValuePredicate("string", "foo");
         foo.walk(out);
         VariableEncoding foos = out.getEncodingFor(foo);
         VariableReference[] names = out.getNamesFor(foo);
         assertEquals(new And(new BooleanFormula[] { names[0].negate(),
                 names[1].negate(), names[2].negate() }), foos.address(0));
         assertEquals(new And(new BooleanFormula[] { names[0],
                 names[1].negate(), names[2] }), foos.address(5));
         BooleanFormula[] conditions = out.getFrameConditions();
         assertEquals(2, conditions.length);
         assertEquals(new Not(new And(new BooleanFormula[] { names[0].negate(),
                 names[1], names[2] })), conditions[0]);
         assertEquals(new Not(new And(new BooleanFormula[] { names[0], names[1],
                 names[2] })), conditions[1]);
     }
 
     public void testInclusion() {
         out.setMultiplicity(2);
         ConstantValuePredicate foo = new ConstantValuePredicate("string", "foo");
         EnvironmentalPredicate bar = new EnvironmentalPredicate("string", "bar");
         FunctionCallPredicate isin = buildFunction("string-is-in",
                 new Predicate[] { foo, bar });
         Triple t = new Triple(isin, b, c);
         t.walk(out);
         VariableEncoding foos = out.getEncodingFor(foo);
         VariableReference[] bars = out.getNamesFor(bar);
         BooleanFormula[] inclusion = new BooleanFormula[2];
         for (int i = 0; i < 2; i++) {
             inclusion[i] = new And(foos.address(i), bars[i]);
         }
         assertTriple(new Or(inclusion), b, c, t);
     }
 
     public void testUnionIntersection() {
         EnvironmentalPredicate foo = new EnvironmentalPredicate("string", "foo");
         EnvironmentalPredicate bar = new EnvironmentalPredicate("string", "bar");
         FunctionCallPredicate union = buildFunction("string-union",
                 new Predicate[] { foo, bar });
         FunctionCallPredicate intersection = buildFunction(
                 "string-intersection", new Predicate[] { foo, bar });
         FunctionCallPredicate temp = buildFunction("string-set-equals",
                 new Predicate[] { union, intersection });
         Triple t = new Triple(temp, b, c);
         t.walk(out);
         VariableReference[] foos = out.getNamesFor(foo);
         VariableReference[] bars = out.getNamesFor(bar);
         VariableReference[] unions = out.getNamesFor(union);
         VariableReference[] intersections = out.getNamesFor(intersection);
         BooleanFormula[] iconstraints = new BooleanFormula[3];
         BooleanFormula[] uconstraints = new BooleanFormula[3];
         for (int i = 0; i < 3; i++) {
             iconstraints[i] = new Equivalence(intersections[i], new And(
                     foos[i], bars[i]));
             uconstraints[i] = new Equivalence(unions[i], new Or(foos[i],
                     bars[i]));
         }
         BooleanFormula[] temps = buildEquivalence(unions, intersections);
         assertTriple(new And(temps), b, c, t, new And[] {
                 new And(uconstraints), new And(iconstraints) });
     }
 
     public void testSole() {
         EnvironmentalPredicate ep = new EnvironmentalPredicate("string", "foo");
         Predicate p = new SolePredicate(ep);
         Triple t = new Triple(p, b, c);
         t.walk(out);
         VariableReference[] vars = out.getNamesFor(ep);
         BooleanFormula[] formula = new BooleanFormula[3];
         for (int i = 0; i < 3; i++) {
             BooleanFormula[] or = new BooleanFormula[3];
             for (int j = 0; j < 3; j++) {
                 if (i == j)
                     or[j] = vars[j];
                 else
                     or[j] = new Not(vars[j]);
             }
             formula[i] = new And(or);
         }
         assertTriple(new Or(formula), b, c, t);
     }
 
     public void testSetCreation() {
         ConstantValuePredicate foo = new ConstantValuePredicate("string", "foo");
         FunctionCallPredicate temp = buildFunction("string-bag",
                 new Predicate[] { foo });
         temp.walk(out);
         VariableReference[] vars = out.getNamesFor(temp);
         VariableEncoding enc = out.getEncodingFor(foo);
         BooleanFormula[] formula = new BooleanFormula[3];
         for (int i = 0; i < formula.length; i++) {
             BooleanFormula[] subformula = new BooleanFormula[3];
             for (int j = 0; j < subformula.length; j++) {
                 if (i == j)
                     subformula[j] = vars[j];
                 else
                     subformula[j] = vars[j].negate();
             }
             formula[i] = new Equivalence(enc.address(i), new And(subformula));
         }
         BooleanFormula[] frameConditions = out.getFrameConditions();
         assertEquals(2, frameConditions.length);
         assertEquals(new And(formula), frameConditions[0]);
         assertEquals(makeInvalidCondition(foo), frameConditions[1]);
     }
 
     private BooleanFormula makeInvalidCondition(ConstantValuePredicate predicate) {
         return new And(out.getNamesFor(predicate)).negate();
     }
 
     public void testSetSize() {
         EnvironmentalPredicate foo = new EnvironmentalPredicate("string", "foo");
         FunctionCallPredicate size = buildFunction("string-bag-size",
                 new Predicate[] { foo });
         size.walk(out);
         VariableReference[] foos = out.getNamesFor(foo);
         VariableReference[] sizes = out.getNamesFor(size);
         for (int i = 0; i < foos.length; i++) {
             for (int j = 0; j < sizes.length; j++) {
                 assertFalse(foos[i].equals(sizes[j]));
             }
         }
         BooleanFormula[] frameConditions = out.getFrameConditions();
         assertEquals(1, frameConditions.length);
         assertEquals(new And(sizes).negate(), frameConditions[0]);
     }
 
     public void testXPath() {
         // Takes an xpath expression as first argument and a string as second,
         // returning a Boolean. We don't handle this at all, just returning an
         // unconstrained Boolean.
         ConstantValuePredicate xpath = new ConstantValuePredicate("string",
                 "an xpath expression");
         ConstantValuePredicate str = new ConstantValuePredicate("string",
                 "a string");
         FunctionCallPredicate match = buildFunction("xpath-node-match",
                 new Predicate[] { xpath, str });
         match.walk(out);
         BooleanFormula result = out.getFormulaFor(match);
         VariableReference[] xpaths = out.getNamesFor(xpath);
         VariableReference[] strs = out.getNamesFor(str);
         for (int i = 0; i < xpaths.length; i++) {
             assertFalse(result.equals(xpaths[i]));
         }
         for (int i = 0; i < strs.length; i++) {
             assertFalse(result.equals(strs[i]));
         }
         BooleanFormula[] frameConditions = out.getFrameConditions();
         assertEquals(2, frameConditions.length);
         assertEquals(makeInvalidCondition(str), frameConditions[0]);
         assertEquals(makeInvalidCondition(xpath), frameConditions[1]);
     }
 
     public void testBooleanConstants() {
         ConstantValuePredicate bool = new ConstantValuePredicate(
                 "http://www.w3.org/2001/XMLSchema#boolean", "foop");
         bool.walk(out);
         BooleanFormula formula = out.getFormulaFor(bool);
         assertTrue(formula instanceof VariableReference);
         VariableReference[] bools = out.getNamesFor(bool);
         assertEquals(1, bools.length);
         assertSame(formula, bools[0]);
         VariableEncoding enc = out.getEncodingFor(bool);
         assertEquals(1, enc.getSize());
         assertSame(formula, enc.address(0));
         assertEquals(0, out.getFrameConditions().length);
     }
 
     public void testIndependentVariables() {
         checkEquivalences(new EnvironmentalPredicate("string",
                 "bar  baz   quux"), new EnvironmentalPredicate("string",
                 "bar  baz   quux"), new EnvironmentalPredicate("string",
                 "bar: baz * quux"));
     }
 
     public void testIndependentConstants() {
         checkEquivalences(new ConstantValuePredicate("string",
                 "bar  baz   quux"), new ConstantValuePredicate("string",
                 "bar  baz   quux"), new ConstantValuePredicate("string",
                 "bar: baz * quux"));
     }
 
     public void testExistential() {
         out.setMultiplicity(4); // to avoid frame conditions
         // in the -equal case, this is akin to asserting that the given string
         // *will* be in the set
         // in the xpath-node-match case, this is a noop.
         Predicate[] formulas = new Predicate[3];
         Predicate attribute = new ConstantValuePredicate("string", "attribute");
         Predicate bag = new EnvironmentalPredicate("string", "bag");
         formulas[0] = buildExistential("string-equal", attribute, bag);
         Predicate second = buildExistential("string-equal", attribute, bag);
         formulas[1] = buildFunction("not", new Predicate[] { second });
         formulas[2] = buildExistential("xpath-node-match", attribute, bag);
         Predicate and = formulas[0].andWith(formulas[1]).orWith(formulas[2]);
         and.walk(out);
         BooleanFormula result = out.getFormulaFor(and);
 
         VariableEncoding attr = out.getEncodingFor(attribute);
         VariableReference[] bagNames = out.getNamesFor(bag);
 
         BooleanFormula[] subclauses = new BooleanFormula[bagNames.length];
         for (int i = 0; i < subclauses.length; i++) {
             subclauses[i] = new Implication(attr.address(i), bagNames[i]);
         }
         BooleanFormula strEq = new And(subclauses);
         BooleanFormula expected = new Or(new And(strEq, new Not(strEq)),
                PrimitiveBoolean.TRUE);
         assertEquals(expected, result);
         assertEquals(0, out.getFrameConditions().length);
     }
 
     private void checkEquivalences(Predicate first, Predicate firstCopy,
             Predicate second) {
         first.walk(out);
         firstCopy.walk(out);
         second.walk(out);
         checkEquivalences(out.getNamesFor(first), out.getNamesFor(firstCopy),
                 out.getNamesFor(second));
     }
 
     private void checkEquivalences(VariableReference[] first,
             VariableReference[] firstCopy, VariableReference[] second) {
         assertEquals(first.length, firstCopy.length);
         assertEquals(first.length, second.length);
         for (int i = 0; i < second.length; i++) {
             assertEquals(first[i], firstCopy[i]);
             assertFalse(first[i] + " is the same as bars[i]", first[i]
                     .equals(second[i]));
             assertFalse(first[i].toString().matches(".*[^_A-Za-z0-9]+.*"));
             assertFalse(second[i].toString().matches(".*[^_A-Za-z0-9]+.*"));
         }
     }
 }
 // arch-tag: BB83F7DE-32CF-11D8-8724-000A957284DA
