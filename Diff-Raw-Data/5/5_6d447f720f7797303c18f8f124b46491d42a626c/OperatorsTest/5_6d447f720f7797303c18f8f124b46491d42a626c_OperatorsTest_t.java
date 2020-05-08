 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertTrue;
 import interpreter.envs.Interpreter;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Rule;
 import org.junit.Test;
 import org.junit.rules.ExpectedException;
 
 import result.BagResult;
 import result.BinderResult;
 import result.BooleanResult;
 import result.DoubleResult;
 import result.IntegerResult;
 import result.ReferenceResult;
 import result.StringResult;
 import result.StructResult;
 import ast.Expression;
 import ast.auxname.AsExpression;
 import ast.auxname.GroupAsExpression;
 import ast.binary.AndExpression;
 import ast.binary.CommaExpression;
 import ast.binary.DivideExpression;
 import ast.binary.DotExpression;
 import ast.binary.EqualsExpression;
 import ast.binary.ForAllExpression;
 import ast.binary.ForAnyExpression;
 import ast.binary.GreaterOrEqualThanExpression;
 import ast.binary.GreaterThanExpression;
 import ast.binary.InExpression;
 import ast.binary.IntersectExpression;
 import ast.binary.JoinExpression;
 import ast.binary.LessOrEqualThanExpression;
 import ast.binary.LessThanExpression;
 import ast.binary.MinusExpression;
 import ast.binary.MinusSetExpression;
 import ast.binary.ModuloExpression;
 import ast.binary.MultiplyExpression;
 import ast.binary.OrExpression;
 import ast.binary.PlusExpression;
 import ast.binary.UnionExpression;
 import ast.binary.WhereExpression;
 import ast.binary.XORExpression;
 import ast.terminal.BooleanTerminal;
 import ast.terminal.DoubleTerminal;
 import ast.terminal.IntegerTerminal;
 import ast.terminal.NameTerminal;
 import ast.terminal.StringTerminal;
 import ast.unary.BagExpression;
 import ast.unary.CountExpression;
 import ast.unary.MaxExpression;
 import ast.unary.MinExpression;
 import ast.unary.NotExpression;
 import ast.unary.StructExpression;
 import ast.unary.SumExpression;
 import ast.unary.UniqueExpression;
 import datastore.MyOID;
 import datastore.SBAObject;
 import datastore.SBAStore;
 
 public class OperatorsTest {
 	private static final String OPERATORS_DATA = "operators_data.xml";
 
 	Interpreter i;
 	SBAStore store;
 
 	@Rule
 	public ExpectedException exception = ExpectedException.none();
 
 	@Before
 	public void setUp() throws Exception {
 		MyOID.resetCounterForJUnit();
 		store = new SBAStore();
 		store.loadXML(OPERATORS_DATA);
 		i = new Interpreter(store);
 		// System.out.println(store.allObjectsMap);
 	}
 
 	@After
 	public void teardown() {
 		store = new SBAStore();
 		store.loadXML(OPERATORS_DATA);
 		i = new Interpreter(store);
 	}
 
 	@Test
 	public void test_2() {
 		Expression expr = new ForAllExpression(new IntegerTerminal(1),
 				new BooleanTerminal(true));
 
 		assertTrue(((BooleanResult) i.eval(expr)).getValue());
 	}
 
 	@Test
 	public void test_3() {
 		Expression expr = new ForAllExpression(new AsExpression(
 				new BagExpression(new CommaExpression(new IntegerTerminal(1),
 						new GroupAsExpression(new BagExpression(
 								new CommaExpression(new IntegerTerminal(2),
 										new IntegerTerminal(3))), "wew"))),
 				"num"), new EqualsExpression(new NameTerminal("num"),
 				new IntegerTerminal(2)));
 
 		assertFalse(((BooleanResult) i.eval(expr)).getValue());
 	}
 
 	@Test
 	public void test_4() {
 		Expression expr = new ForAllExpression(new NameTerminal("emp"),
 				new NameTerminal("married"));
 
 		assertTrue(((BooleanResult) i.eval(expr)).getValue());
 	}
 
 	@Test
 	public void test_5() {
 		Expression expr = new AndExpression(new BooleanTerminal(true),
 				new BooleanTerminal(false));
 
 		assertFalse(((BooleanResult) i.eval(expr)).getValue());
 	}
 
 	@Test
 	public void test_6() {
 		Expression expr = new AndExpression(new NameTerminal("booleanValue"),
 				new BooleanTerminal(true));
 
 		assertTrue(((BooleanResult) i.eval(expr)).getValue());
 	}
 
 	@Test
 	public void test_7() {
 		Expression expr = new AndExpression(new AndExpression(
 				new BooleanTerminal(false), new BooleanTerminal(true)),
 				new IntegerTerminal(1));
 
 		assertFalse(((BooleanResult) i.eval(expr)).getValue());
 		// TODO byc moze juz po pierwszym napotkanym false powinno zwrocic
 		// false, a nie isc dalej.
 	}
 
 	@Test
 	public void test_8() {
 		Expression expr = new ForAnyExpression(new IntegerTerminal(1),
 				new BooleanTerminal(true));
 
 		assertTrue(((BooleanResult) i.eval(expr)).getValue());
 	}
 
 	@Test
 	public void test_10() {
 		Expression expr = new ForAnyExpression(new NameTerminal("emp"),
 				new NameTerminal("married"));
 
 		assertTrue(((BooleanResult) i.eval(expr)).getValue());
 	}
 
 	@Test
 	public void test_11() {
 		Expression expr = new AsExpression(new IntegerTerminal(1), "liczba");
 
 		BagResult expected = new BagResult();
 		expected.add(new BinderResult("liczba", new IntegerResult(1)));
 
 		assertTrue(expected.equalsForJUnit((BagResult) i.eval(expr)));
 	}
 
 	@Test
 	public void test_12() {
 		Expression expr = new AsExpression(new BagExpression(
 				new CommaExpression(new IntegerTerminal(1),
 						new IntegerTerminal(2))), "num");
 
 		BagResult expected = new BagResult();
 		expected.add(new BinderResult("num", new IntegerResult(1)));
 		expected.add(new BinderResult("num", new IntegerResult(2)));
 
 		assertTrue(expected.equalsForJUnit((BagResult) i.eval(expr)));
 	}
 
 	@Test
 	public void test_13() {
 		Expression expr = new AsExpression(new CommaExpression(
 				new IntegerTerminal(1), new IntegerTerminal(2)), "num");
 
 		BagResult expected = new BagResult();
 		StructResult struct = new StructResult();
 		struct.add(new IntegerResult(1));
 		struct.add(new IntegerResult(2));
 		expected.add(new BinderResult("num", struct));
 
 		assertTrue(expected.equalsForJUnit((BagResult) i.eval(expr)));
 	}
 
 	@Test
 	public void test_14() {
 		Expression expr = new BagExpression(new IntegerTerminal(1));
 		BagResult expected = new BagResult();
 		expected.add(new IntegerResult(1));
 
 		assertTrue(expected.equalsForJUnit((BagResult) i.eval(expr)));
 	}
 
 	@Test
 	public void test_15() {
 		Expression expr = new BagExpression(new CommaExpression(
 				new CommaExpression(new IntegerTerminal(1),
 						new IntegerTerminal(2)), new IntegerTerminal(3)));
 		BagResult expected = new BagResult();
 		expected.add(new IntegerResult(1));
 		expected.add(new IntegerResult(2));
 		expected.add(new IntegerResult(3));
 		assertTrue(expected.equalsForJUnit((BagResult) i.eval(expr)));
 	}
 
 	@Test
 	public void test_16() {
 		Expression expr = new BagExpression(new CommaExpression(
 				new PlusExpression(new IntegerTerminal(1), new IntegerTerminal(
 						2)), new IntegerTerminal(3)));
 
 		BagResult expected = new BagResult();
 		expected.add(new IntegerResult(1));
 		expected.add(new IntegerResult(2));
 		expected.add(new IntegerResult(3));
 		assertTrue(expected.equalsForJUnit((BagResult) i.eval(expr)));
 	}
 
 	@Test
 	public void test_17() {
 		Expression expr = new BagExpression(new BagExpression(
 				new CommaExpression(new CommaExpression(new IntegerTerminal(1),
 						new IntegerTerminal(2)), new IntegerTerminal(3))));
 
 		BagResult inner = new BagResult();
 		inner.add(new IntegerResult(1));
 		inner.add(new IntegerResult(2));
 		inner.add(new IntegerResult(3));
 		BagResult expected = new BagResult();
 		expected.add(inner);
 		assertTrue(expected.equalsForJUnit((BagResult) i.eval(expr)));
 	}
 
 	@Test
 	public void test_18() {
 		Expression expr = new NameTerminal("integerNumber");
 
 		BagResult expected = new BagResult();
 		ReferenceResult reference = new ReferenceResult(
 				MyOID.createOIDForJUnit(1));
 		expected.add(reference);
 		assertTrue(expected.equalsForJUnit((BagResult) i.eval(expr)));
 	}
 
 	@Test
 	public void test_19() {
 		Expression expr = new NameTerminal("realNumber");
 		BagResult expected = new BagResult();
 		ReferenceResult reference = new ReferenceResult(
 				MyOID.createOIDForJUnit(3));
 		expected.add(reference);
 		assertTrue(expected.equalsForJUnit((BagResult) i.eval(expr)));
 	}
 
 	@Test
 	public void test_20() {
 		Expression expr = new NameTerminal("booleanValue");
 		BagResult expected = new BagResult();
 		ReferenceResult reference = new ReferenceResult(
 				MyOID.createOIDForJUnit(5));
 		expected.add(reference);
 		assertTrue(expected.equalsForJUnit((BagResult) i.eval(expr)));
 	}
 
 	@Test
 	public void test_21() {
 		Expression expr = new NameTerminal("stringValue");
 		BagResult expected = new BagResult();
 		ReferenceResult reference = new ReferenceResult(
 				MyOID.createOIDForJUnit(7));
 		expected.add(reference);
 		assertTrue(expected.equalsForJUnit((BagResult) i.eval(expr)));
 	}
 
 	@Test
 	public void test_22() {
 		Expression expr = new NameTerminal("pomidor");
 
 		BagResult expected = new BagResult();
 		ReferenceResult reference = new ReferenceResult(
 				MyOID.createOIDForJUnit(9));
 		ReferenceResult reference2 = new ReferenceResult(
 				MyOID.createOIDForJUnit(10));
 		ReferenceResult reference3 = new ReferenceResult(
 				MyOID.createOIDForJUnit(11));
 		ReferenceResult reference4 = new ReferenceResult(
 				MyOID.createOIDForJUnit(12));
 		expected.add(reference);
 		expected.add(reference2);
 		expected.add(reference3);
 		expected.add(reference4);
 		assertTrue(expected.equalsForJUnit((BagResult) i.eval(expr)));
 	}
 
 	@Test
 	public void test_23() {
 		Expression expr = new NameTerminal("sampleComplexObj");
 		BagResult expected = new BagResult();
 		ReferenceResult reference = new ReferenceResult(
 				SBAObject.allObjectsForJUnit.get("sampleComplexObj"));
 		expected.add(reference);
 		assertTrue(expected.equalsForJUnit((BagResult) i.eval(expr)));
 	}
 
 	@Test
 	public void test_24() {
 		Expression expr = new CommaExpression(new IntegerTerminal(1),
 				new IntegerTerminal(2));
 		BagResult expected = new BagResult();
 		StructResult struct = new StructResult();
 		struct.add(new IntegerResult(1));
 		struct.add(new IntegerResult(2));
 		expected.add(struct);
 		assertTrue(expected.equalsForJUnit((BagResult) i.eval(expr)));
 	}
 
 	@Test
 	public void test_25() {
 		Expression expr = new CommaExpression(new BagExpression(
 				new CommaExpression(new IntegerTerminal(1),
 						new IntegerTerminal(2))), new IntegerTerminal(3));
 		BagResult expected = new BagResult();
 		StructResult struct1 = new StructResult();
 		struct1.add(new IntegerResult(1));
 		struct1.add(new IntegerResult(3));
 		StructResult struct2 = new StructResult();
 		struct2.add(new IntegerResult(2));
 		struct2.add(new IntegerResult(3));
 		expected.add(struct1);
 		expected.add(struct2);
 		assertTrue(expected.equalsForJUnit((BagResult) i.eval(expr)));
 
 	}
 
 	@Test
 	public void test_26() {
 		Expression expr = new CommaExpression(new BagExpression(
 				new CommaExpression(new IntegerTerminal(1),
 						new IntegerTerminal(2))), new BagExpression(
 				new CommaExpression(new IntegerTerminal(3),
 						new IntegerTerminal(4))));
 		BagResult expected = new BagResult();
 		StructResult struct1 = new StructResult();
 		struct1.add(new IntegerResult(1));
 		struct1.add(new IntegerResult(3));
 		StructResult struct2 = new StructResult();
 		struct2.add(new IntegerResult(1));
 		struct2.add(new IntegerResult(4));
 		StructResult struct3 = new StructResult();
 		struct3.add(new IntegerResult(2));
 		struct3.add(new IntegerResult(3));
 		StructResult struct4 = new StructResult();
 		struct4.add(new IntegerResult(2));
 		struct4.add(new IntegerResult(4));
 		expected.add(struct1);
 		expected.add(struct2);
 		expected.add(struct3);
 		expected.add(struct4);
 		assertTrue(expected.equalsForJUnit((BagResult) i.eval(expr)));
 	}
 
 	// @Test
 	// public void test_27() {
 	// Expression expr = new DivideExpression(new IntegerTerminal(10),
 	// new IntegerTerminal(5));
 	//
 	// assertEquals(2, ((DoubleResult) i.eval(expr)).getValue().doubleValue(),
 	// 0.001);
 	// }
 
 	// @Test
 	// public void test_28() {
 	// Expression expr = new DivideExpression(new IntegerTerminal(5),
 	// new DoubleTerminal(3.50));
 	//
 	// assertEquals(1.4285714285714286, ((DoubleResult) i.eval(expr))
 	// .getValue().doubleValue(), 0.001);
 	// }
 
 	// @Test
 	// public void test_29() {
 	// Expression expr = new DivideExpression(new DoubleTerminal(3.50),
 	// new IntegerTerminal(5));
 	//
 	// assertEquals(0.7, ((DoubleResult) i.eval(expr)).getValue()
 	// .doubleValue(), 0.001);
 	// }
 
 	// @Test
 	// public void test_30() {
 	// Expression expr = new DivideExpression(new DoubleTerminal(3.50),
 	// new DoubleTerminal(5.50));
 	//
 	// assertEquals(0.63636363636364, ((DoubleResult) i.eval(expr)).getValue()
 	// .doubleValue(), 0.001);
 	// }
 
 	@Test
 	public void test_31() {
 		Expression expr = new DotExpression(new AsExpression(
 				new IntegerTerminal(1), "x"), new NameTerminal("x"));
 
		BagResult expected = new BagResult();
		expected.add(new IntegerResult(1));
		assertTrue(expected.equalsForJUnit((BagResult) i.eval(expr)));
 	}
 
 	@Test
 	public void test_32() {
 		Expression expr = new DotExpression(new CommaExpression(
 				new IntegerTerminal(1), new IntegerTerminal(2)),
 				new StringTerminal("Ala"));
 
 		assertEquals("Ala", ((StringResult) i.eval(expr)).getValue());
 	}
 
 	@Test
 	public void test_33() {
 		Expression expr = new DotExpression(new DotExpression(new NameTerminal(
 				"emp"), new NameTerminal("book")), new NameTerminal("author"));
 
 		BagResult expected = new BagResult();
 		ReferenceResult reference = new ReferenceResult(
 				MyOID.createOIDForJUnit(31));
 		ReferenceResult reference2 = new ReferenceResult(
 				MyOID.createOIDForJUnit(43));
 		ReferenceResult reference3 = new ReferenceResult(
 				MyOID.createOIDForJUnit(46));
 		expected.add(reference);
 		expected.add(reference2);
 		expected.add(reference3);
 		assertTrue(expected.equalsForJUnit((BagResult) i.eval(expr)));
 	}
 
 	@Test
 	public void test_34() {
 		Expression expr = new DotExpression(new BagExpression(
 				new CommaExpression(new IntegerTerminal(1),
 						new IntegerTerminal(2))), new StringTerminal("Ala"));
 
 		BagResult expected = new BagResult();
 		expected.add(new StringResult("Ala"));
 		expected.add(new StringResult("Ala"));
 		assertTrue(expected.equalsForJUnit((BagResult) i.eval(expr)));
 	}
 
 	@Test
 	public void test_35() {
 		Expression expr = new EqualsExpression(new IntegerTerminal(1),
 				new IntegerTerminal(2));
 
 		assertFalse(((BooleanResult) i.eval(expr)).getValue());
 	}
 
 	@Test
 	public void test_36() {
 		Expression expr = new EqualsExpression(
 				new NameTerminal("integerNumber"), new IntegerTerminal(10));
 
 		assertTrue(((BooleanResult) i.eval(expr)).getValue());
 	}
 
 	@Test
 	public void test_37() {
 		Expression expr = new EqualsExpression(new BagExpression(
 				new CommaExpression(new IntegerTerminal(1),
 						new IntegerTerminal(2))), new IntegerTerminal(2));
 
 		exception.expect(RuntimeException.class);
 
 		i.eval(expr);
 	}
 
 	@Test
 	public void test_38() {
 		Expression expr = new EqualsExpression(
 				new NameTerminal("booleanValue"), new BooleanTerminal(true));
 
 		assertTrue(((BooleanResult) i.eval(expr)).getValue());
 	}
 
 	@Test
 	public void test_39() {
 		Expression expr = new EqualsExpression(new NameTerminal("stringValue"),
 				new StringTerminal("Ala"));
 
 		assertTrue(((BooleanResult) i.eval(expr)).getValue());
 	}
 
 	@Test
 	public void test_40() {
 		Expression expr = new EqualsExpression(new IntegerTerminal(1),
 				new BooleanTerminal(true));
 
 		assertFalse(((BooleanResult) i.eval(expr)).getValue());
 	}
 
 	@Test
 	public void test_41() {
 		Expression expr = new EqualsExpression(new IntegerTerminal(5),
 				new StringTerminal("5"));
 
 		assertFalse(((BooleanResult) i.eval(expr)).getValue());
 	}
 
 	@Test
 	public void test_42() {
 		Expression expr = new EqualsExpression(new DoubleTerminal(5.50),
 				new IntegerTerminal(5));
 
 		assertFalse(((BooleanResult) i.eval(expr)).getValue());
 	}
 
 	@Test
 	public void test_43() {
 		Expression expr = new GreaterThanExpression(new IntegerTerminal(1),
 				new IntegerTerminal(1));
 
 		assertFalse(((BooleanResult) i.eval(expr)).getValue());
 	}
 
 	@Test
 	public void test_44() {
 		Expression expr = new GreaterThanExpression(new IntegerTerminal(3),
 				new DoubleTerminal(2.99));
 
 		assertTrue(((BooleanResult) i.eval(expr)).getValue());
 	}
 
 	@Test
 	public void test_45() {
 		Expression expr = new GreaterThanExpression(new DoubleTerminal(24.35),
 				new DoubleTerminal(24.34));
 
 		assertTrue(((BooleanResult) i.eval(expr)).getValue());
 	}
 
 	@Test
 	public void test_46() {
 		Expression expr = new GreaterOrEqualThanExpression(new IntegerTerminal(
 				1), new IntegerTerminal(1));
 
 		assertTrue(((BooleanResult) i.eval(expr)).getValue());
 	}
 
 	@Test
 	public void test_47() {
 		Expression expr = new GreaterOrEqualThanExpression(new IntegerTerminal(
 				3), new DoubleTerminal(2.99));
 
 		assertTrue(((BooleanResult) i.eval(expr)).getValue());
 	}
 
 	@Test
 	public void test_48() {
 		Expression expr = new GreaterOrEqualThanExpression(new DoubleTerminal(
 				24.35), new DoubleTerminal(24.34));
 
 		assertTrue(((BooleanResult) i.eval(expr)).getValue());
 	}
 
 	@Test
 	public void test_49() {
 		Expression expr = new GroupAsExpression(new BagExpression(
 				new CommaExpression(new CommaExpression(new IntegerTerminal(1),
 						new IntegerTerminal(2)), new IntegerTerminal(3))),
 				"num");
 		BagResult inner = new BagResult();
 		inner.add(new IntegerResult(1));
 		inner.add(new IntegerResult(2));
 		inner.add(new IntegerResult(3));
 		BinderResult expected = new BinderResult("num", inner);
 		assertTrue(expected.equals((BinderResult) i.eval(expr)));
 	}
 
 	@Test
 	public void test_50() {
 		Expression expr = new GroupAsExpression(new IntegerTerminal(1),
 				"liczba");
 		BinderResult expected = new BinderResult("liczba", new IntegerResult(1));
 		assertTrue(expected.equals((BinderResult) i.eval(expr)));
 	}
 
 	@Test
 	public void test_51() {
 		Expression expr = new IntersectExpression(new BagExpression(
 				new CommaExpression(new CommaExpression(new IntegerTerminal(1),
 						new IntegerTerminal(2)), new IntegerTerminal(3))),
 				new BagExpression(new CommaExpression(new IntegerTerminal(2),
 						new IntegerTerminal(3))));
 
 		BagResult expected = new BagResult();
 		expected.add(new IntegerResult(2));
 		expected.add(new IntegerResult(3));
 		assertTrue(expected.equalsForJUnit((BagResult) i.eval(expr)));
 	}
 
 	@Test
 	public void test_52() {
 		Expression expr = new IntersectExpression(new IntegerTerminal(1),
 				new IntegerTerminal(1));
 
 		BagResult expected = new BagResult();
 		expected.add(new IntegerResult(1));
 		assertTrue(expected.equalsForJUnit((BagResult) i.eval(expr)));
 	}
 
 	@Test
 	public void test_53() {
 		Expression expr = new IntersectExpression(new CommaExpression(
 				new IntegerTerminal(1), new IntegerTerminal(2)),
 				new CommaExpression(new IntegerTerminal(2),
 						new IntegerTerminal(3)));
 
 		BagResult expected = new BagResult();
 		assertTrue(expected.equalsForJUnit((BagResult) i.eval(expr)));
 	}
 
 	@Test
 	public void test_54() {
 		Expression expr = new IntersectExpression(new CommaExpression(
 				new IntegerTerminal(1), new IntegerTerminal(2)),
 				new CommaExpression(new IntegerTerminal(1),
 						new IntegerTerminal(2)));
 		BagResult expected = new BagResult();
 		StructResult struct = new StructResult();
 		struct.add(new IntegerResult(1));
 		struct.add(new IntegerResult(2));
 		expected.add(struct);
 		assertTrue(expected.equalsForJUnit((BagResult) i.eval(expr)));
 	}
 
 	@Test
 	public void test_55() {
 		Expression expr = new IntersectExpression(
 				new BagExpression(new CommaExpression(new CommaExpression(
 						new StringTerminal("ala"), new IntegerTerminal(2)),
 						new IntegerTerminal(3))), new BagExpression(
 						new CommaExpression(new IntegerTerminal(2),
 								new DoubleTerminal(3.40))));
 
 		BagResult expected = new BagResult();
 		expected.add(new IntegerResult(2));
 		assertTrue(expected.equalsForJUnit((BagResult) i.eval(expr)));
 	}
 
 	// @Test
 	// public void test_56() {
 	// Expression expr = new JoinExpression(new IntegerTerminal(1),
 	// new IntegerTerminal(2));
 	//
 	// BagResult expected = new BagResult();
 	// StructResult struct = new StructResult();
 	// struct.add(new IntegerResult(1));
 	// struct.add(new IntegerResult(2));
 	// expected.add(struct);
 	// assertTrue(expected.equalsForJUnit((BagResult) i.eval(expr)));
 	// }
 
 	// @Test
 	// public void test_57() {
 	// Expression expr = new JoinExpression(new AsExpression(
 	// new IntegerTerminal(1), "n"), new NameTerminal("n"));
 	//
 	// BagResult expected = new BagResult();
 	// StructResult struct = new StructResult();
 	// struct.add(new BinderResult("n", new IntegerResult(1)));
 	// struct.add(new IntegerResult(1));
 	// expected.add(struct);
 	// assertTrue(expected.equalsForJUnit((BagResult) i.eval(expr)));
 	// }
 
 	// @Test
 	// public void test_58() {
 	// Expression expr = new JoinExpression(new NameTerminal("emp"),
 	// new NameTerminal("married"));
 	//
 	// BagResult expected = new BagResult();
 	// assertTrue(expected.equalsForJUnit((BagResult) i.eval(expr)));
 	// }
 
 	// @Test
 	// public void test_59() {
 	// Expression expr = new JoinExpression(new AsExpression(new NameTerminal(
 	// "emp"), "e"), new DotExpression(new NameTerminal("e"),
 	// new NameTerminal("married")));
 	//
 	// BagResult expected = new BagResult();
 	// assertTrue(expected.equalsForJUnit((BagResult) i.eval(expr)));
 	// }
 
 	@Test
 	public void test_60() {
 		Expression expr = new LessOrEqualThanExpression(new IntegerTerminal(1),
 				new IntegerTerminal(1));
 
 		assertTrue(((BooleanResult) i.eval(expr)).getValue());
 	}
 
 	@Test
 	public void test_61() {
 		Expression expr = new LessOrEqualThanExpression(new DoubleTerminal(
 				24.34), new DoubleTerminal(24.35));
 
 		assertTrue(((BooleanResult) i.eval(expr)).getValue());
 
 	}
 
 	@Test
 	public void test_62() {
 		Expression expr = new LessOrEqualThanExpression(
 				new DoubleTerminal(2.99), new IntegerTerminal(3));
 		assertTrue(((BooleanResult) i.eval(expr)).getValue());
 
 	}
 
 	@Test
 	public void test_63() {
 		Expression expr = new LessThanExpression(new IntegerTerminal(1),
 				new IntegerTerminal(1));
 
 		assertFalse(((BooleanResult) i.eval(expr)).getValue());
 
 	}
 
 	@Test
 	public void test_64() {
 		Expression expr = new LessThanExpression(new DoubleTerminal(24.34),
 				new DoubleTerminal(24.35));
 
 		assertTrue(((BooleanResult) i.eval(expr)).getValue());
 
 	}
 
 	@Test
 	public void test_65() {
 		Expression expr = new LessThanExpression(new DoubleTerminal(2.99),
 				new IntegerTerminal(3));
 		assertTrue(((BooleanResult) i.eval(expr)).getValue());
 
 	}
 
 	@Test
 	public void test_66() {
 		Expression expr = new MaxExpression(new IntegerTerminal(1));
 
 		assertEquals(1,
 				((IntegerResult) i.eval(expr)).getValue().doubleValue(), 0.001);
 	}
 
 	@Test
 	public void test_67() {
 		Expression expr = new MaxExpression(new BagExpression(
 				new CommaExpression(new CommaExpression(new IntegerTerminal(1),
 						new DoubleTerminal(3.35)), new IntegerTerminal(3))));
 
 		assertEquals(3.35, ((DoubleResult) i.eval(expr)).getValue(), 0.001);
 	}
 
 	@Test
 	public void test_68() {
 		Expression expr = new MinExpression(new IntegerTerminal(1));
 
 		assertEquals(1,
 				((IntegerResult) i.eval(expr)).getValue().doubleValue(), 0.001);
 	}
 
 	@Test
 	public void test_69() {
 		Expression expr = new MinExpression(new BagExpression(
 				new CommaExpression(new CommaExpression(
 						new DoubleTerminal(1.01), new DoubleTerminal(2.35)),
 						new IntegerTerminal(3))));
 
 		assertEquals(1.01, ((DoubleResult) i.eval(expr)).getValue(), 0.001);
 	}
 
 	@Test
 	public void test_70() {
 		Expression expr = new MinusExpression(new IntegerTerminal(10),
 				new IntegerTerminal(5));
 
 		assertEquals(5,
 				((IntegerResult) i.eval(expr)).getValue().doubleValue(), 0.001);
 	}
 
 	@Test
 	public void test_71() {
 		Expression expr = new MinusExpression(new IntegerTerminal(5),
 				new DoubleTerminal(3.50));
 
 		assertEquals(1.50, ((DoubleResult) i.eval(expr)).getValue(), 0.001);
 	}
 
 	@Test
 	public void test_72() {
 		Expression expr = new MinusExpression(new DoubleTerminal(3.50),
 				new IntegerTerminal(5));
 
 		assertEquals(-1.5, ((DoubleResult) i.eval(expr)).getValue(), 0.001);
 	}
 
 	@Test
 	public void test_73() {
 		Expression expr = new MinusExpression(new DoubleTerminal(3.50),
 				new DoubleTerminal(5.50));
 
 		assertEquals(-2, ((IntegerResult) i.eval(expr)).getValue()
 				.doubleValue(), 0.001);
 	}
 
 	// @Test
 	// public void test_74() {
 	// Expression expr = new ModuloExpression(new IntegerTerminal(10),
 	// new IntegerTerminal(5));
 	//
 	// assertEquals(0,
 	// ((IntegerResult) i.eval(expr)).getValue().doubleValue(), 0.001);
 	// }
 
 	// @Test
 	// public void test_75() {
 	// Expression expr = new ModuloExpression(new IntegerTerminal(5),
 	// new DoubleTerminal(3.50));
 	//
 	// assertEquals(1.5, ((DoubleResult) i.eval(expr)).getValue(), 0.001);
 	// }
 
 	// @Test
 	// public void test_76() {
 	// Expression expr = new ModuloExpression(new DoubleTerminal(3.50),
 	// new IntegerTerminal(5));
 	//
 	// assertEquals(3.5, ((DoubleResult) i.eval(expr)).getValue(), 0.001);
 	// }
 
 	// @Test
 	// public void test_77() {
 	// Expression expr = new ModuloExpression(new DoubleTerminal(3.50),
 	// new DoubleTerminal(5.50));
 	//
 	// assertEquals(3.5, ((DoubleResult) i.eval(expr)).getValue(), 0.001);
 	// }
 
 	// @Test
 	// public void test_78() {
 	// Expression expr = new MultiplyExpression(new IntegerTerminal(10),
 	// new IntegerTerminal(5));
 	//
 	// assertEquals(50, ((IntegerResult) i.eval(expr)).getValue()
 	// .doubleValue(), 0.001);
 	// }
 
 	// @Test
 	// public void test_79() {
 	// Expression expr = new MultiplyExpression(new IntegerTerminal(5),
 	// new DoubleTerminal(3.50));
 	//
 	// assertEquals(17.5, ((DoubleResult) i.eval(expr)).getValue(), 0.001);
 	// }
 
 	// @Test
 	// public void test_80() {
 	// Expression expr = new MultiplyExpression(new DoubleTerminal(3.50),
 	// new IntegerTerminal(5));
 	//
 	// assertEquals(17.5, ((DoubleResult) i.eval(expr)).getValue(), 0.001);
 	// }
 
 	// @Test
 	// public void test_81() {
 	// Expression expr = new MultiplyExpression(new DoubleTerminal(3.50),
 	// new DoubleTerminal(5.50));
 	//
 	// assertEquals(19.25, ((DoubleResult) i.eval(expr)).getValue(), 0.001);
 	// }
 
 	// @Test
 	// public void test_82() {
 	// Expression expr = new OrExpression(new BooleanTerminal(true),
 	// new BooleanTerminal(false));
 	//
 	// assertTrue(((BooleanResult) i.eval(expr)).getValue());
 	// }
 
 	// @Test
 	// public void test_83() {
 	// Expression expr = new OrExpression(new NameTerminal("booleanValue"),
 	// new BooleanTerminal(false));
 	//
 	// assertTrue(((BooleanResult) i.eval(expr)).getValue());
 	// }
 
 	// @Test
 	// public void test_84() {
 	// Expression expr = new OrExpression(new OrExpression(
 	// new BooleanTerminal(true), new BooleanTerminal(false)),
 	// new IntegerTerminal(1));
 	//
 	// assertTrue(((BooleanResult) i.eval(expr)).getValue());
 	// }
 
 	@Test
 	public void test_85() {
 		Expression expr = new PlusExpression(new IntegerTerminal(10),
 				new IntegerTerminal(5));
 
 		assertEquals(15, ((IntegerResult) i.eval(expr)).getValue()
 				.doubleValue(), 0.001);
 	}
 
 	@Test
 	public void test_86() {
 		Expression expr = new PlusExpression(new IntegerTerminal(5),
 				new DoubleTerminal(3.50));
 
 		assertEquals(8.50, ((DoubleResult) i.eval(expr)).getValue(), 0.001);
 	}
 
 	@Test
 	public void test_87() {
 		Expression expr = new PlusExpression(new DoubleTerminal(3.50),
 				new IntegerTerminal(5));
 
 		assertEquals(8.50, ((DoubleResult) i.eval(expr)).getValue(), 0.001);
 	}
 
 	@Test
 	public void test_88() {
 		Expression expr = new PlusExpression(new DoubleTerminal(3.50),
 				new DoubleTerminal(5.50));
 
 		assertEquals(9,
 				((IntegerResult) i.eval(expr)).getValue().doubleValue(), 0.001);
 	}
 
 	@Test
 	public void test_89() {
 		Expression expr = new PlusExpression(new IntegerTerminal(3),
 				new StringTerminal("Ala"));
 
 		assertEquals("3Ala", ((StringResult) i.eval(expr)).getValue());
 	}
 
 	@Test
 	public void test_90() {
 		Expression expr = new PlusExpression(new DoubleTerminal(3.5),
 				new StringTerminal("Ala"));
 
 		assertEquals("3.5Ala", ((StringResult) i.eval(expr)).getValue());
 	}
 
 	@Test
 	public void test_91() {
 		Expression expr = new PlusExpression(new StringTerminal("Ala"),
 				new DoubleTerminal(3.7));
 
 		assertEquals("Ala3.7", ((StringResult) i.eval(expr)).getValue());
 	}
 
 	@Test
 	public void test_92() {
 		Expression expr = new PlusExpression(new BooleanTerminal(true),
 				new StringTerminal("Ala"));
 
 		assertEquals("trueAla", ((StringResult) i.eval(expr)).getValue());
 	}
 
 	@Test
 	public void test_93() {
 		Expression expr = new StructExpression(new IntegerTerminal(1));
 
 		BagResult expected = new BagResult();
 		StructResult struct = new StructResult();
 		struct.add(new IntegerResult(1));
 		expected.add(struct);
 		assertTrue(expected.equalsForJUnit((BagResult) i.eval(expr)));
 	}
 
 	@Test
 	public void test_94() {
 		Expression expr = new StructExpression(new CommaExpression(
 				new CommaExpression(new IntegerTerminal(1),
 						new IntegerTerminal(2)), new IntegerTerminal(3)));
 
 		BagResult expected = new BagResult();
 		StructResult struct = new StructResult();
 		struct.add(new IntegerResult(1));
 		struct.add(new IntegerResult(2));
 		struct.add(new IntegerResult(3));
 		expected.add(struct);
 		assertTrue(expected.equalsForJUnit((BagResult) i.eval(expr)));
 	}
 
 	@Test
 	public void test_95() {
 		Expression expr = new StructExpression(new CommaExpression(
 				new PlusExpression(new IntegerTerminal(1), new IntegerTerminal(
 						2)), new IntegerTerminal(3)));
 
 		BagResult expected = new BagResult();
 		StructResult struct = new StructResult();
 		struct.add(new IntegerResult(3));
 		struct.add(new IntegerResult(3));
 		expected.add(struct);
 		assertTrue(expected.equalsForJUnit((BagResult) i.eval(expr)));
 	}
 
 	@Test
 	public void test_96() {
 		Expression expr = new StructExpression(new StructExpression(
 				new CommaExpression(new CommaExpression(new IntegerTerminal(1),
 						new IntegerTerminal(2)), new IntegerTerminal(3))));
 
 		BagResult expected = new BagResult();
 		StructResult struct = new StructResult();
 		struct.add(new IntegerResult(1));
 		struct.add(new IntegerResult(2));
 		struct.add(new IntegerResult(3));
 		expected.add(struct);
 		assertTrue(expected.equalsForJUnit((BagResult) i.eval(expr)));
 	}
 
 	@Test
 	public void test_97() {
 		Expression expr = new UnionExpression(new IntegerTerminal(1),
 				new IntegerTerminal(2));
 		BagResult expected = new BagResult();
 		expected.add(new IntegerResult(1));
 		expected.add(new IntegerResult(2));
 		assertTrue(expected.equalsForJUnit((BagResult) i.eval(expr)));
 	}
 
 	@Test
 	public void test_98() {
 		Expression expr = new UnionExpression(new BagExpression(
 				new CommaExpression(new IntegerTerminal(1),
 						new IntegerTerminal(2))), new BagExpression(
 				new CommaExpression(new IntegerTerminal(3),
 						new IntegerTerminal(4))));
 
 		BagResult expected = new BagResult();
 		expected.add(new IntegerResult(1));
 		expected.add(new IntegerResult(2));
 		expected.add(new IntegerResult(3));
 		expected.add(new IntegerResult(4));
 		assertTrue(expected.equalsForJUnit((BagResult) i.eval(expr)));
 	}
 
 	@Test
 	public void test_99() {
 		Expression expr = new UnionExpression(new CommaExpression(
 				new IntegerTerminal(1), new IntegerTerminal(2)),
 				new CommaExpression(new IntegerTerminal(3),
 						new IntegerTerminal(4)));
 
 		BagResult expected = new BagResult();
 		StructResult struct1 = new StructResult();
 		struct1.add(new IntegerResult(1));
 		struct1.add(new IntegerResult(2));
 		StructResult struct2 = new StructResult();
 		struct2.add(new IntegerResult(3));
 		struct2.add(new IntegerResult(4));
 		expected.add(struct1);
 		expected.add(struct2);
 
 		assertTrue(expected.equalsForJUnit((BagResult) i.eval(expr)));
 	}
 
 	// @Test
 	// public void test_100() {
 	// Expression expr = new UniqueExpression(new BagExpression(
 	// new CommaExpression(new CommaExpression(new IntegerTerminal(1),
 	// new IntegerTerminal(2)), new IntegerTerminal(1))));
 	//
 	// BagResult expected = new BagResult();
 	// expected.add(new IntegerResult(1));
 	// expected.add(new IntegerResult(2));
 	// assertTrue(expected.equalsForJUnit((BagResult) i.eval(expr)));
 	// }
 
 	// @Test
 	// public void test_101() {
 	// Expression expr = new UniqueExpression(new BagExpression(
 	// new CommaExpression(new CommaExpression(new CommaExpression(
 	// new DoubleTerminal(1.01), new IntegerTerminal(2)),
 	// new DoubleTerminal(1.01)), new StringTerminal("ala"))));
 	//
 	// BagResult expected = new BagResult();
 	// expected.add(new DoubleResult(1.01));
 	// expected.add(new IntegerResult(2));
 	// expected.add(new StringResult("ala"));
 	// assertTrue(expected.equalsForJUnit((BagResult) i.eval(expr)));
 	// }
 
 	@Test
 	public void test_102() {
 		Expression expr = new WhereExpression(new BagExpression(
 				new CommaExpression(new IntegerTerminal(1),
 						new IntegerTerminal(2))), new BooleanTerminal(true));
 		BagResult expected = new BagResult();
 		expected.add(new IntegerResult(1));
 		expected.add(new IntegerResult(2));
 		assertTrue(expected.equalsForJUnit((BagResult) i.eval(expr)));
 	}
 
 	// @Test
 	// public void test_103() {
 	// Expression expr = new WhereExpression(new AsExpression(
 	// new BagExpression(new CommaExpression(new CommaExpression(
 	// new IntegerTerminal(1), new IntegerTerminal(2)),
 	// new IntegerTerminal(3))), "n"), new EqualsExpression(
 	// new NameTerminal("n"), new IntegerTerminal(1)));
 	//
 	// BagResult expected = new BagResult();
 	// expected.add(new BinderResult("n", new IntegerResult(1)));
 	// assertTrue(expected.equalsForJUnit((BagResult) i.eval(expr)));
 	// }
 
 	// @Test
 	// public void test_104() {
 	// Expression expr = new WhereExpression(new NameTerminal("emp"),
 	// new NameTerminal("married"));
 	// BagResult expected = new BagResult();
 	// assertTrue(expected.equalsForJUnit((BagResult) i.eval(expr)));
 	// }
 
 	@Test
 	public void test_105() {
 		Expression expr = new SumExpression(new IntegerTerminal(1));
 
 		assertEquals(1,
 				((IntegerResult) i.eval(expr)).getValue().doubleValue(), 0.001);
 
 	}
 
 	@Test
 	public void test_106() {
 		Expression expr = new SumExpression(new BagExpression(
 				new CommaExpression(new CommaExpression(
 						new DoubleTerminal(1.01), new DoubleTerminal(2.35)),
 						new IntegerTerminal(3))));
 
 		assertEquals(6.36, ((DoubleResult) i.eval(expr)).getValue(), 0.001);
 	}
 
 	// @Test
 	// public void test_107() {
 	// Expression expr = new CountExpression(new IntegerTerminal(1));
 	//
 	// assertEquals(1,
 	// ((IntegerResult) i.eval(expr)).getValue().doubleValue(), 0.001);
 	// }
 
 	// @Test
 	// public void test_108() {
 	// Expression expr = new CountExpression(new BagExpression(
 	// new CommaExpression(new CommaExpression(
 	// new DoubleTerminal(1.01), new DoubleTerminal(2.35)),
 	// new IntegerTerminal(3))));
 	// assertEquals(3,
 	// ((IntegerResult) i.eval(expr)).getValue().doubleValue(), 0.001);
 	// }
 
 	// @Test
 	// public void test_109() {
 	// Expression expr = new CountExpression(new CommaExpression(
 	// new CommaExpression(new DoubleTerminal(1.01),
 	// new DoubleTerminal(2.35)), new IntegerTerminal(3)));
 	//
 	// assertEquals(1,
 	// ((IntegerResult) i.eval(expr)).getValue().doubleValue(), 0.001);
 	// }
 
 	// @Test
 	// public void test_110() {
 	// // Expression expr=new EmptyExpression(
 	// // new IntegerTerminal(1)
 	// // );
 	// Expression expr = null;
 	// assertFalse(((BooleanResult) i.eval(expr)).getValue());
 	// }
 
 	// @Test
 	// public void test_111() {
 	// // Expression expr=new EmptyExpression(
 	// // new BagExpression(
 	// // new CommaExpression(
 	// // new CommaExpression(
 	// // new DoubleTerminal(1.01),
 	// // new DoubleTerminal(2.35)
 	// // ),
 	// // new IntegerTerminal(3)
 	// // )
 	// // )
 	// // );
 	// Expression expr = null;
 	// assertFalse(((BooleanResult) i.eval(expr)).getValue());
 	// }
 
 	// @Test
 	// public void test_112() {
 	// // Expression expr = new EmptyExpression(new WhereExpression(
 	// // new IntegerTerminal(1), new BooleanTerminal(false)));
 	//
 	// Expression expr = null;
 	// assertTrue(((BooleanResult) i.eval(expr)).getValue());
 	// }
 
 	// @Test
 	// public void test_113() {
 	// Expression expr = new NotExpression(new BooleanTerminal(true));
 	// assertFalse(((BooleanResult) i.eval(expr)).getValue());
 	// }
 
 	// @Test
 	// public void test_114() {
 	// Expression expr = new NotExpression(new BooleanTerminal(false));
 	//
 	// assertTrue(((BooleanResult) i.eval(expr)).getValue());
 	// }
 
 	// @Test
 	// public void test_115() {
 	// Expression expr = new XORExpression(new BooleanTerminal(true),
 	// new BooleanTerminal(false));
 	//
 	// assertTrue(((BooleanResult) i.eval(expr)).getValue());
 	// }
 
 	// @Test
 	// public void test_116() {
 	// Expression expr = new XORExpression(new BooleanTerminal(true),
 	// new BooleanTerminal(true));
 	//
 	// assertFalse(((BooleanResult) i.eval(expr)).getValue());
 	// }
 	//
 	// @Test
 	// public void test_117() {
 	// Expression expr = new XORExpression(new BooleanTerminal(false),
 	// new BooleanTerminal(true));
 	//
 	// assertTrue(((BooleanResult) i.eval(expr)).getValue());
 	// }
 	//
 	// @Test
 	// public void test_118() {
 	// Expression expr = new XORExpression(new NameTerminal("booleanValue"),
 	// new BooleanTerminal(true));
 	//
 	// assertFalse(((BooleanResult) i.eval(expr)).getValue());
 	// }
 	//
 	// @Test
 	// public void test_119() {
 	// Expression expr = new NotExpression(new AndExpression(
 	// new BooleanTerminal(true), new BooleanTerminal(false)));
 	//
 	// assertTrue(((BooleanResult) i.eval(expr)).getValue());
 	// }
 
 	// @Test
 	// public void test_120() {
 	// Expression expr = new NotExpression(new AndExpression(
 	// new BooleanTerminal(false), new BooleanTerminal(true)));
 	//
 	// assertTrue(((BooleanResult) i.eval(expr)).getValue());
 	// }
 	//
 	// @Test
 	// public void test_121() {
 	// Expression expr = new NotExpression(new AndExpression(
 	// new BooleanTerminal(false), new BooleanTerminal(false)));
 	//
 	// assertTrue(((BooleanResult) i.eval(expr)).getValue());
 	// }
 	//
 	// @Test
 	// public void test_122() {
 	// Expression expr = new NotExpression(new AndExpression(
 	// new BooleanTerminal(true), new BooleanTerminal(true)));
 	//
 	// assertFalse(((BooleanResult) i.eval(expr)).getValue());
 	// }
 	//
 	// @Test
 	// public void test_123() {
 	// Expression expr = new NotExpression(new AndExpression(new NameTerminal(
 	// "booleanValue"), new BooleanTerminal(true)));
 	//
 	// assertFalse(((BooleanResult) i.eval(expr)).getValue());
 	// }
 
 	@Test
 	public void test_124() {
 		Expression expr = new InExpression(new BagExpression(
 				new CommaExpression(new IntegerTerminal(2),
 						new IntegerTerminal(3))), new BagExpression(
 				new CommaExpression(new CommaExpression(new IntegerTerminal(1),
 						new IntegerTerminal(2)), new IntegerTerminal(3))));
 
 		assertTrue(((BooleanResult) i.eval(expr)).getValue());
 	}
 
 	@Test
 	public void test_125() {
 		Expression expr = new InExpression(new IntegerTerminal(1),
 				new IntegerTerminal(1));
 
 		assertTrue(((BooleanResult) i.eval(expr)).getValue());
 	}
 
 	@Test
 	public void test_126() {
 		Expression expr = new InExpression(new CommaExpression(
 				new IntegerTerminal(1), new IntegerTerminal(2)),
 				new CommaExpression(new IntegerTerminal(2),
 						new IntegerTerminal(3)));
 
 		assertFalse(((BooleanResult) i.eval(expr)).getValue());
 	}
 
 	@Test
 	public void test_127() {
 		Expression expr = new InExpression(new CommaExpression(
 				new IntegerTerminal(1), new IntegerTerminal(2)),
 				new CommaExpression(new IntegerTerminal(1),
 						new IntegerTerminal(2)));
 
 		assertTrue(((BooleanResult) i.eval(expr)).getValue());
 	}
 
 	@Test
 	public void test_128() {
 		Expression expr = new MinusSetExpression(new BagExpression(
 				new CommaExpression(new CommaExpression(new IntegerTerminal(1),
 						new IntegerTerminal(2)), new IntegerTerminal(3))),
 				new BagExpression(new CommaExpression(new IntegerTerminal(2),
 						new IntegerTerminal(3))));
 
 		BagResult expected = new BagResult();
 		expected.add(new IntegerResult(1));
 		assertTrue(expected.equalsForJUnit((BagResult) i.eval(expr)));
 	}
 
 	@Test
 	public void test_129() {
 		Expression expr = new MinusSetExpression(new IntegerTerminal(1),
 				new IntegerTerminal(1));
 
 		BagResult expected = new BagResult();
 		assertTrue(expected.equalsForJUnit((BagResult) i.eval(expr)));
 	}
 
 	@Test
 	public void test_130() {
 		Expression expr = new MinusSetExpression(new CommaExpression(
 				new IntegerTerminal(1), new IntegerTerminal(2)),
 				new CommaExpression(new IntegerTerminal(2),
 						new IntegerTerminal(3)));
 
 		BagResult expected = new BagResult();
 		StructResult struct = new StructResult();
 		struct.add(new IntegerResult(1));
 		struct.add(new IntegerResult(2));
 		expected.add(struct);
 		assertTrue(expected.equalsForJUnit((BagResult) i.eval(expr)));
 	}
 
 	@Test
 	public void test_131() {
 		Expression expr = new MinusSetExpression(new CommaExpression(
 				new IntegerTerminal(1), new IntegerTerminal(2)),
 				new CommaExpression(new IntegerTerminal(1),
 						new IntegerTerminal(2)));
 
 		BagResult expected = new BagResult();
 		assertTrue(expected.equalsForJUnit((BagResult) i.eval(expr)));
 	}
 
 	@Test
 	public void test_132() {
 		Expression expr = new MinusSetExpression(
 				new BagExpression(new CommaExpression(new CommaExpression(
 						new StringTerminal("ala"), new IntegerTerminal(2)),
 						new IntegerTerminal(3))), new BagExpression(
 						new CommaExpression(new IntegerTerminal(2),
 								new DoubleTerminal(3.40))));
 
 		BagResult expected = new BagResult();
 		expected.add(new StringResult("Ala"));
 		expected.add(new IntegerResult(3));
 		assertTrue(expected.equalsForJUnit((BagResult) i.eval(expr)));
 	}
 }
