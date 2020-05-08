 package se.raek.ahsa;
 
 import static org.junit.Assert.*;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import org.junit.Test;
 
 import se.raek.ahsa.Interpreter.CastException;
 import se.raek.ahsa.ast.Expression;
 import se.raek.ahsa.ast.LoopLabel;
 import se.raek.ahsa.ast.Statement;
 import se.raek.ahsa.ast.ValueLocation;
 import se.raek.ahsa.ast.VariableLocation;
 import se.raek.ahsa.runtime.BuiltInFunctions;
 import se.raek.ahsa.runtime.CompoundFunction;
 import se.raek.ahsa.runtime.ControlAction;
 import se.raek.ahsa.runtime.Store;
 import se.raek.ahsa.runtime.Value;
 import se.raek.ahsa.runtime.Function;
 
 import static se.raek.ahsa.ast.Expression.*;
 import static se.raek.ahsa.ast.ArithmeticOperator.*;
 import static se.raek.ahsa.ast.EqualityOperator.*;
 import static se.raek.ahsa.ast.RelationalOperator.*;
 import static se.raek.ahsa.ast.Statement.*;
 import static se.raek.ahsa.runtime.Value.*;
 
 public class InterpreterTest {
 
 	private final static Value v0 = makeNumber(0.0);
 	private final static Value v1 = makeNumber(1.0);
 	private final static Value v2 = makeNumber(2.0);
 	private final static Value v3 = makeNumber(3.0);
 	private final static Expression c0 = makeConstant(v0);
 	private final static Expression c1 = makeConstant(v1);
 	private final static Expression c2 = makeConstant(v2);
 	private final static Expression c3 = makeConstant(v3);
 	private final static Expression c6 = makeConstant(makeNumber(6.0));
 	private final static Expression cNull = makeConstant(makeNull());
 	private final static ValueLocation valX = new ValueLocation("x");
 	private final static VariableLocation varY = new VariableLocation("y");
 
 	@Test
 	public void evalConstant() {
 		assertEquals(v1, Interpreter.eval(makeConstant(v1), null));
 	}
 
 	@Test
 	public void evalValueLookup() {
 		Store sto = new Store(null);
 		sto.defineValue(valX, v1);
 		assertEquals(v1, Interpreter.eval(makeValueLookup(valX), sto));
 	}
 
 	@Test
 	public void evalVariableLookup() {
 		Store sto = new Store(null);
 		sto.assignVariable(varY, v1);
 		assertEquals(v1, Interpreter.eval(makeVariableLookup(varY), sto));
 	}
 
 	@Test
 	public void evalAddition() {
 		assertEquals(makeNumber(5), Interpreter.eval(
 				makeArithmeticOperation(ADDITION, c2, c3), null));
 	}
 
 	@Test
 	public void evalSubtraction() {
 		assertEquals(makeNumber(-1), Interpreter.eval(
 				makeArithmeticOperation(SUBTRACTION, c2, c3), null));
 	}
 
 	@Test
 	public void evalMultiplication() {
 		assertEquals(
 				makeNumber(6),
 				Interpreter.eval(
 						makeArithmeticOperation(MULTIPLICATION, c2, c3), null));
 	}
 
 	@Test
 	public void evalDivision() {
 		assertEquals(makeNumber(2), Interpreter.eval(
 				makeArithmeticOperation(DIVISION, c6, c3), null));
 	}
 
 	@Test(expected = Interpreter.CastException.class)
 	public void evalArithOpWithNonNumberArgument() {
 		Interpreter.eval(makeArithmeticOperation(ADDITION, c1, cNull), null);
 	}
 
 	@Test
 	public void evalEqualSameNumbers() {
 		assertEquals(makeBoolean(true),
 				Interpreter.eval(makeEqualityOperation(EQUAL, c1, c1), null));
 	}
 
 	@Test
 	public void evalEqualDifferentNumbers() {
 		assertEquals(makeBoolean(false),
 				Interpreter.eval(makeEqualityOperation(EQUAL, c1, c2), null));
 	}
 
 	@Test
 	public void evalEqualDifferentTypes() {
 		assertEquals(makeBoolean(false), Interpreter.eval(
 				makeEqualityOperation(EQUAL, cNull, c2), null));
 	}
 
 	@Test
 	public void evalUnequalSameNumbers() {
 		assertEquals(makeBoolean(false),
 				Interpreter.eval(makeEqualityOperation(UNEQUAL, c1, c1), null));
 	}
 
 	@Test
 	public void evalUnequalDifferentNumbers() {
 		assertEquals(makeBoolean(true),
 				Interpreter.eval(makeEqualityOperation(UNEQUAL, c1, c2), null));
 	}
 
 	@Test
 	public void evalUnequalDifferentTypes() {
 		assertEquals(makeBoolean(true), Interpreter.eval(
 				makeEqualityOperation(UNEQUAL, cNull, c2), null));
 	}
 
 	@Test
 	public void evalGreaterTrue() {
 		assertEquals(makeBoolean(true), Interpreter.eval(
 				makeRelationalOperation(GREATER, c3, c2), null));
 	}
 
 	@Test
 	public void evalGreaterFalse() {
 		assertEquals(makeBoolean(false), Interpreter.eval(
 				makeRelationalOperation(GREATER, c2, c3), null));
 	}
 
 	@Test
 	public void evalLessTrue() {
 		assertEquals(makeBoolean(true),
 				Interpreter.eval(makeRelationalOperation(LESS, c2, c3), null));
 	}
 
 	@Test
 	public void evalLessFalse() {
 		assertEquals(makeBoolean(false),
 				Interpreter.eval(makeRelationalOperation(LESS, c3, c2), null));
 	}
 
 	@Test
 	public void evalGreaterEqualTrue() {
 		assertEquals(
 				makeBoolean(true),
 				Interpreter.eval(
 						makeRelationalOperation(GREATER_EQUAL, c3, c2), null));
 	}
 
 	@Test
 	public void evalGreaterEqualFalse() {
 		assertEquals(
 				makeBoolean(false),
 				Interpreter.eval(
 						makeRelationalOperation(GREATER_EQUAL, c2, c3), null));
 	}
 
 	@Test
 	public void evalLessEqualTrue() {
 		assertEquals(makeBoolean(true), Interpreter.eval(
 				makeRelationalOperation(LESS_EQUAL, c2, c3), null));
 	}
 
 	@Test
 	public void evalLessEqualFalse() {
 		assertEquals(makeBoolean(false), Interpreter.eval(
 				makeRelationalOperation(LESS_EQUAL, c3, c2), null));
 	}
 
 	@Test(expected = Interpreter.CastException.class)
 	public void evalRelOpWithNonNumberArgument() {
 		Interpreter.eval(makeRelationalOperation(GREATER, c1, cNull), null);
 	}
 
 	@Test
 	public void executeThrowawayExpression() {
 		Store sto = new Store(null);
 		Interpreter.execute(makeThrowawayExpression(cNull), sto);
 	}
 
 	@Test
 	public void executeValueDefinition() {
 		Store sto = new Store(null);
 		Interpreter.execute(makeValueDefinition(valX, c1), sto);
 		assertEquals(v1, sto.lookupValue(valX));
 	}
 
 	@Test
 	public void executeVariableAssignment() {
 		Store sto = new Store(null);
 		Interpreter.execute(makeVariableAssignment(varY, c1), sto);
 		assertEquals(v1, sto.lookupVariable(varY));
 	}
 
 	@Test
 	public void executeConditionalTrue() {
 		Expression c0 = makeConstant(makeNumber(0.0));
 		Expression lookupX = makeValueLookup(valX);
 		Store sto = new Store(null);
 		List<Statement> stmts = new ArrayList<Statement>();
 		stmts.add(makeValueDefinition(valX, makeConstant(makeNumber(2.0))));
 		Expression cond = makeRelationalOperation(GREATER_EQUAL, lookupX, c0);
 		Statement thenStmt = makeVariableAssignment(varY, lookupX);
 		Statement elseStmt = makeVariableAssignment(varY,
 				makeArithmeticOperation(SUBTRACTION, c0, lookupX));
 		stmts.add(makeConditional(cond, Collections.singletonList(thenStmt),
 				Collections.singletonList(elseStmt)));
 		Interpreter.execute(stmts, sto);
 		assertEquals(makeNumber(2.0), sto.lookupVariable(varY));
 	}
 
 	@Test
 	public void executeConditionalFalse() {
 		Expression c0 = makeConstant(makeNumber(0.0));
 		Expression lookupX = makeValueLookup(valX);
 		Store sto = new Store(null);
 		List<Statement> stmts = new ArrayList<Statement>();
 		stmts.add(makeValueDefinition(valX, makeConstant(makeNumber(-2.0))));
 		Expression cond = makeRelationalOperation(GREATER_EQUAL, lookupX, c0);
 		Statement thenStmt = makeVariableAssignment(varY, lookupX);
 		Statement elseStmt = makeVariableAssignment(varY,
 				makeArithmeticOperation(SUBTRACTION, c0, lookupX));
 		stmts.add(makeConditional(cond, Collections.singletonList(thenStmt),
 				Collections.singletonList(elseStmt)));
 		Interpreter.execute(stmts, sto);
 		assertEquals(makeNumber(2.0), sto.lookupVariable(varY));
 	}
 	
 	@Test
 	public void executeBreak() {
 		final LoopLabel loop1 = new LoopLabel("l1");
 		final LoopLabel loop2 = new LoopLabel("l2");
 		List<Statement> stmts = new ArrayList<Statement>();
 		stmts.add(makeBreak(loop1));
 		stmts.add(makeBreak(loop2));;
 		assertTrue(Interpreter.execute(stmts, new Store()).matchControlAction(new ControlAction.AbstractMatcher<java.lang.Boolean>() {
 			@Override
 			public java.lang.Boolean caseBreak(LoopLabel loop) {
 				return loop.equals(loop1);
 			}
 			@Override
 			public Boolean otherwise() {
 				return false;
 			}
 		}));
 	}
 	
 	@Test
 	public void breakThisLoop() {
 		final LoopLabel loop0 = new LoopLabel("l");
 		List<Statement> body = Collections.singletonList(makeBreak(loop0));
 		List<Statement> stmts = Collections.singletonList(makeLoop(loop0, body));
 		assertTrue(Interpreter.execute(stmts, new Store()).matchControlAction(new ControlAction.AbstractMatcher<java.lang.Boolean>() {
 			@Override
 			public java.lang.Boolean caseNext() {
 				return true;
 			}
 			@Override
 			public Boolean otherwise() {
 				return false;
 			}
 		}));
 	}
 	
 	@Test
 	public void breakOtherLoop() {
 		final LoopLabel loop1 = new LoopLabel("l1");
 		final LoopLabel loop2 = new LoopLabel("l2");
 		List<Statement> body = Collections.singletonList(makeBreak(loop1));
 		List<Statement> stmts = Collections.singletonList(makeLoop(loop2, body));
 		assertTrue(Interpreter.execute(stmts, new Store()).matchControlAction(new ControlAction.AbstractMatcher<java.lang.Boolean>() {
 			@Override
 			public java.lang.Boolean caseBreak(LoopLabel loop) {
 				return loop.equals(loop1);
 			}
 			@Override
 			public Boolean otherwise() {
 				return false;
 			}
 		}));
 	}
 	
 	@Test
 	public void returnFromLoop() {
 		final LoopLabel loop0 = new LoopLabel("l");
 		List<Statement> body = Collections.singletonList(makeReturn(c0));
 		List<Statement> stmts = Collections.singletonList(makeLoop(loop0, body));
 		assertTrue(Interpreter.execute(stmts, new Store()).matchControlAction(new ControlAction.AbstractMatcher<java.lang.Boolean>() {
 			@Override
 			public java.lang.Boolean caseReturn(Value v) {
 				return v.equals(v0);
 			}
 			@Override
 			public Boolean otherwise() {
 				return false;
 			}
 		}));
 	}
 	
 	@Test
 	public void executeReturn() {
 		List<Statement> stmts = new ArrayList<Statement>();
 		stmts.add(makeReturn(c1));
 		stmts.add(makeReturn(c2));;
 		assertTrue(Interpreter.execute(stmts, new Store()).matchControlAction(new ControlAction.AbstractMatcher<java.lang.Boolean>() {
 			@Override
 			public java.lang.Boolean caseReturn(Value v) {
 				return v.equals(v1);
 			}
 			@Override
 			public Boolean otherwise() {
 				return false;
 			}
 		}));
 	}
 	
 	@Test
 	public void returnFromFunction() {
 		List<Statement> body = Collections.singletonList(makeReturn(c0));
 		Function fn = new CompoundFunction(Collections.<ValueLocation>emptyList(), body, new Store());
 		assertEquals(v0, fn.apply(Collections.<Value>emptyList()));
 	}
 	
 	@Test
 	public void reachEndOfFunction() {
 		List<Statement> body = Collections.emptyList();
 		Function fn = new CompoundFunction(Collections.<ValueLocation>emptyList(), body, new Store());
 		assertEquals(makeNull(), fn.apply(Collections.<Value>emptyList()));
 	}
 	
 	@Test(expected=AssertionError.class)
 	public void breakFromFunction() {
 		final LoopLabel loop0 = new LoopLabel("l");
 		List<Statement> body = Collections.singletonList(makeBreak(loop0));
 		Function fn = new CompoundFunction(Collections.<ValueLocation>emptyList(), body, new Store());
 		fn.apply(Collections.<Value>emptyList());
 	}
 	
 	@Test
 	public void makeAndApplyFunction() {
 		ValueLocation f = new ValueLocation("f");
 		ValueLocation x = new ValueLocation("x");
 		ValueLocation y = new ValueLocation("y");
 		List<Statement> stmts = new ArrayList<Statement>();
 		Expression bodyExpr = makeArithmeticOperation(ADDITION, makeValueLookup(x), c1);
 		List<Statement> bodyStmts = Collections.singletonList((Statement) makeReturn(bodyExpr));
 		stmts.add(makeValueDefinition(f, makeFunctionAbstraction(Collections.singletonList(x), bodyStmts)));
 		Expression fExpr = makeValueLookup(f);
 		List<Expression> params = Collections.<Expression>singletonList(c2);
 		stmts.add(makeValueDefinition(y, makeFunctionApplication(fExpr, params)));
 		Store sto = new Store();
 		Interpreter.execute(stmts, sto);
 		assertEquals(v3, sto.lookupValue(y));
 	}
 	
 	@Test(expected=CastException.class)
 	public void applyNonFunction() {
 		Expression function = cNull;
 		List<Expression> params = Collections.emptyList();
 		List<Statement> stmts = new ArrayList<Statement>();
 		stmts.add(makeThrowawayExpression(makeFunctionApplication(function, params)));
 		Interpreter.execute(stmts, new Store());
 	}
 	
 	@Test
 	public void executeTopLevelNormal() {
 		List<Statement> stmts = Collections.singletonList(makeThrowawayExpression(cNull));
 		Interpreter.executeTopLevel(stmts, new Store());
 	}
 	
 	@Test(expected=AssertionError.class)
 	public void executeTopLevelBreak() {
 		LoopLabel loop = new LoopLabel("l");
 		List<Statement> stmts = Collections.singletonList(makeBreak(loop));
 		Interpreter.executeTopLevel(stmts, new Store());
 	}
 	
 	@Test(expected=AssertionError.class)
 	public void executeTopLevelReturn() {
 		List<Statement> stmts = Collections.singletonList(makeReturn(cNull));
 		Interpreter.executeTopLevel(stmts, new Store());
 	}
 	
 	@Test
 	public void loop() {
 		// var a = 3;
 		// var b = 0;
 		// loop l {
 		//   if a == 0 {
 		//     break l;
 		//   }
 		//   a = a - 1;
 		//   b = b + 1;
 		// }
 		VariableLocation varA = new VariableLocation("a");
 		VariableLocation varB = new VariableLocation("b");
 		LoopLabel loop = new LoopLabel("l");
 		List<Statement> stmts = new ArrayList<Statement>();
 		stmts.add(makeVariableAssignment(varA, c3));
 		stmts.add(makeVariableAssignment(varB, c0));
 		List<Statement> body = new ArrayList<Statement>();
 		body.add(makeConditional(makeEqualityOperation(EQUAL, makeVariableLookup(varA), c0), Collections.singletonList(makeBreak(loop)), Collections.<Statement>emptyList()));
 		body.add(makeVariableAssignment(varA, makeArithmeticOperation(SUBTRACTION, makeVariableLookup(varA), c1)));
 		body.add(makeVariableAssignment(varB, makeArithmeticOperation(ADDITION, makeVariableLookup(varB), c1)));
 		stmts.add(makeLoop(loop, body));
 		Store sto = new Store();
 		Interpreter.executeTopLevel(stmts, sto);
 		assertEquals(v0, sto.lookupVariable(varA));
 		assertEquals(v3, sto.lookupVariable(varB));
 	}
 
 	@Test
 	public void typeNameNull() {
 		assertEquals("null", Interpreter.typeName(makeNull()));
 	}
 
 	@Test
 	public void typeNameBoolean() {
 		assertEquals("boolean", Interpreter.typeName(makeBoolean(true)));
 	}
 
 	@Test
 	public void typeNameNumber() {
 		assertEquals("number", Interpreter.typeName(makeNumber(1.0)));
 	}
 
 	@Test
 	public void typeNameFunction() {
 		assertEquals("function", Interpreter.typeName(makeFunction(BuiltInFunctions.print)));
 	}
 	
 	@Test
 	public void isTruthyNull() {
 		assertFalse(Interpreter.isTruthy(makeNull()));
 	}
 	
 	@Test
 	public void isTruthyBooleanTrue() {
 		assertTrue(Interpreter.isTruthy(makeBoolean(true)));
 	}
 	
 	@Test
 	public void isTruthyBooleanFalse() {
 		assertFalse(Interpreter.isTruthy(makeBoolean(false)));
 	}
 	
 	@Test
 	public void isTruthyNumberPositive() {
 		assertTrue(Interpreter.isTruthy(makeNumber(1.0)));
 	}
 	
 	@Test
 	public void isTruthyNumberZero() {
 		assertTrue(Interpreter.isTruthy(makeNumber(0.0)));
 	}
 	
 	@Test
 	public void isTruthyNumberNegative() {
 		assertTrue(Interpreter.isTruthy(makeNumber(-1.0)));
 	}
 	
 	@Test
 	public void isTruthyFunction() {
 		assertTrue(Interpreter.isTruthy(makeFunction(BuiltInFunctions.print)));
 	}
 
 }
