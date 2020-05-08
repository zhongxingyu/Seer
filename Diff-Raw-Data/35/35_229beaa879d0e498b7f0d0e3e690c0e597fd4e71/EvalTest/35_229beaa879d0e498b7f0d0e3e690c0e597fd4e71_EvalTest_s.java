 package pl.edu.mimuw.cloudatlas.query;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import pl.edu.mimuw.cloudatlas.attributes.BooleanValue;
 import pl.edu.mimuw.cloudatlas.attributes.DoubleValue;
 import pl.edu.mimuw.cloudatlas.attributes.DurationValue;
 import pl.edu.mimuw.cloudatlas.attributes.IntegerValue;
 import pl.edu.mimuw.cloudatlas.attributes.ListType;
 import pl.edu.mimuw.cloudatlas.attributes.ListValue;
 import pl.edu.mimuw.cloudatlas.attributes.SetType;
 import pl.edu.mimuw.cloudatlas.attributes.SetValue;
 import pl.edu.mimuw.cloudatlas.attributes.SimpleType;
 import pl.edu.mimuw.cloudatlas.attributes.SimpleValue;
 import pl.edu.mimuw.cloudatlas.attributes.StringValue;
 import pl.edu.mimuw.cloudatlas.attributes.TimeValue;
 import pl.edu.mimuw.cloudatlas.attributes.Value;
 import pl.edu.mimuw.cloudatlas.zones.ZMI;
 import junit.framework.TestCase;
 
 public class EvalTest extends TestCase {
 	
 	public static void testOrderBy() throws Exception {
 		ZMI zmi1 = new ZMI();
 		zmi1.addAttribute("id", new IntegerValue(1));
 		zmi1.addAttribute("str", new StringValue("xx"));
 		zmi1.addAttribute("w1", new DoubleValue(1.0));
 		zmi1.addAttribute("intList", ListType.of(SimpleType.INTEGER));
 		
 		ZMI zmi2 = new ZMI();
 		zmi2.addAttribute("id", new IntegerValue(2));
 		zmi2.addAttribute("w1", new DoubleValue(2.0));
 		
 		ZMI zmi3 = new ZMI();
 		zmi3.addAttribute("id", new IntegerValue(3));
 		zmi3.addAttribute("str", new StringValue("x"));
 		zmi3.addAttribute("w1", new DoubleValue(2.0));
 		
 		ZMI zmi4 = new ZMI();
 		zmi4.addAttribute("id", new IntegerValue(4));
 		zmi4.addAttribute("str", new StringValue("yx"));
 		zmi4.addAttribute("w1", new DoubleValue(1.0));
 		
 		List<ZMI> zmis = new ArrayList<ZMI>();
 		zmis.add(zmi1); zmis.add(zmi2); zmis.add(zmi3); zmis.add(zmi4);
 		
 		assertSelectThrows("SELECT 1 ORDER BY intList", zmis);
 		
 		assertSelectReturns("SELECT first(10, id)", zmis,
 				listWith(new IntegerValue(1), new IntegerValue(2), new IntegerValue(3), new IntegerValue(4)));
 		assertSelectReturns("SELECT first(10, id) ORDER BY id ASC", zmis,
 				listWith(new IntegerValue(1), new IntegerValue(2), new IntegerValue(3), new IntegerValue(4)));
 		assertSelectReturns("SELECT first(10, id) ORDER BY id ASC NULLS FIRST", zmis,
 				listWith(new IntegerValue(1), new IntegerValue(2), new IntegerValue(3), new IntegerValue(4)));
 		assertSelectReturns("SELECT first(10, id) ORDER BY id ASC NULLS LAST", zmis,
 				listWith(new IntegerValue(1), new IntegerValue(2), new IntegerValue(3), new IntegerValue(4)));
 		assertSelectReturns("SELECT first(10, id) ORDER BY id DESC", zmis,
 				listWith(new IntegerValue(4), new IntegerValue(3), new IntegerValue(2), new IntegerValue(1)));
 		
 		assertSelectReturns("SELECT first(10, id) ORDER BY str", zmis,
 				listWith(new IntegerValue(3), new IntegerValue(1), new IntegerValue(4), new IntegerValue(2)));
 		assertSelectReturns("SELECT first(10, id) ORDER BY str ASC", zmis,
 				listWith(new IntegerValue(3), new IntegerValue(1), new IntegerValue(4), new IntegerValue(2)));
 		assertSelectReturns("SELECT first(10, id) ORDER BY str DESC", zmis,
 				listWith(new IntegerValue(2), new IntegerValue(4), new IntegerValue(1), new IntegerValue(3)));
 		assertSelectReturns("SELECT first(10, id) ORDER BY str ASC NULLS FIRST", zmis,
 				listWith(new IntegerValue(2), new IntegerValue(3), new IntegerValue(1), new IntegerValue(4)));
 		assertSelectReturns("SELECT first(10, id) ORDER BY str ASC", zmis,
 				listWith(new IntegerValue(3), new IntegerValue(1), new IntegerValue(4), new IntegerValue(2)));
 		assertSelectReturns("SELECT first(10, id) ORDER BY str DESC NULLS FIRST", zmis,
 				listWith(new IntegerValue(2), new IntegerValue(4), new IntegerValue(1), new IntegerValue(3)));
 		assertSelectReturns("SELECT first(10, id) ORDER BY str DESC NULLS LAST", zmis,
 				listWith(new IntegerValue(4), new IntegerValue(1), new IntegerValue(3), new IntegerValue(2)));
 		
		assertSelectReturns("SELECT first(10, id) ORDER BY w1 ASC, id ASC", zmis,
 				listWith(new IntegerValue(1), new IntegerValue(4), new IntegerValue(2), new IntegerValue(3)));
		assertSelectReturns("SELECT first(10, id) ORDER BY w1 ASC, id DESC", zmis,
 				listWith(new IntegerValue(4), new IntegerValue(1), new IntegerValue(3), new IntegerValue(2)));
		assertSelectReturns("SELECT first(10, id) ORDER BY w1 DESC, id ASC", zmis,
 				listWith(new IntegerValue(2), new IntegerValue(3), new IntegerValue(1), new IntegerValue(4)));
		assertSelectReturns("SELECT first(10, id) ORDER BY w1 DESC, id DESC", zmis,
 				listWith(new IntegerValue(3), new IntegerValue(2), new IntegerValue(4), new IntegerValue(1)));
 		
 	}
 	
 	public static void testAggregates() throws Exception {
 		ZMI zmi1 = new ZMI();
 		zmi1.addAttribute("int", new IntegerValue(1));
 		zmi1.addAttribute("nullInt", SimpleType.INTEGER, null);
 		zmi1.addAttribute("intList", listWith(new IntegerValue(2), new IntegerValue(3)));
 		zmi1.addAttribute("str", new StringValue("a"));
 		zmi1.addAttribute("float", new DoubleValue(3.5));
 		zmi1.addAttribute("dur", new DurationValue(100));
 		zmi1.addAttribute("boolTF", new BooleanValue(false));
 		zmi1.addAttribute("boolTT", new BooleanValue(true));
 		zmi1.addAttribute("boolFF", new BooleanValue(false));
 		
 		ZMI zmi2 = new ZMI();
 		zmi2.addAttribute("intList", listWith(new IntegerValue(2), new IntegerValue(3), new IntegerValue(5)));
 		zmi2.addAttribute("str", new StringValue("b"));
 		zmi2.addAttribute("strSet", setWith(new StringValue("aa"), new StringValue("bb")));
 		zmi2.addAttribute("float", new DoubleValue(2));
 		zmi2.addAttribute("dur", new DurationValue(500));
 		zmi2.addAttribute("boolTF", new BooleanValue(true));
 
 		ZMI zmi3 = new ZMI();
 		zmi3.addAttribute("int", new IntegerValue(5));
 		zmi3.addAttribute("str", new StringValue("c"));
 		zmi3.addAttribute("strSet", setWith(new StringValue("dd"), new StringValue("aa")));
 		zmi3.addAttribute("boolTT", new BooleanValue(true));
 		zmi3.addAttribute("boolFF", new BooleanValue(false));
 		
 		List<ZMI> zmis = new ArrayList<ZMI>();
 		zmis.add(zmi1); zmis.add(zmi2); zmis.add(zmi3);
 		
 		assertSelectThrows("SELECT count(1)", zmis);
 		assertSelectReturns("SELECT count(int)", zmis, new IntegerValue(2));
 		assertSelectReturns("SELECT count(is_null(int))", zmis, new IntegerValue(3));
 		assertSelectReturns("SELECT count(str)", zmis, new IntegerValue(3));
 		
 		assertSelectThrows("SELECT unfold(1)", zmis);
 		assertSelectThrows("SELECT unfold(str)", zmis);
 		assertSelectReturns("SELECT count(unfold(strSet))", zmis, new IntegerValue(4));
 		assertSelectReturns("SELECT count(is_null(unfold(intList)))", zmis, new IntegerValue(5));
 		assertSelectReturns("SELECT count(is_null(unfold(strSet)))", zmis, new IntegerValue(4));
 		
 		assertSelectThrows("SELECT distinct(1)", zmis);
 		assertSelectReturns("SELECT count(distinct(str))", zmis, new IntegerValue(3));
 		assertSelectReturns("SELECT count(distinct(int))", zmis, new IntegerValue(2));
 		assertSelectReturns("SELECT count(is_null(distinct(int)))", zmis, new IntegerValue(2));
 		assertSelectReturns("SELECT count(distinct(unfold(intList)))", zmis, new IntegerValue(3));
 		assertSelectReturns("SELECT count(is_null(distinct(unfold(intList))))", zmis, new IntegerValue(3));
 		
 		assertSelectThrows("SELECT avg(0)", zmis);
 		assertSelectReturns("SELECT avg(int)", zmis, new DoubleValue(3.0));
 		assertSelectReturns("SELECT avg(int) WHERE false", zmis, null);
 		assertSelectThrows("SELECT avg(str)", zmis);
 		assertSelectThrows("SELECT avg(intList)", zmis);
 		assertSelectReturns("SELECT avg(float)", zmis, new DoubleValue(5.5 / 2.0));
 		assertSelectReturns("SELECT avg(float) WHERE float > 666.0", zmis, null);
 		assertSelectReturns("SELECT avg(dur)", zmis, new DurationValue(300));
 		assertSelectReturns("SELECT avg(dur) WHERE false", zmis, null);
 		
 		assertSelectThrows("SELECT sum(0)", zmis);
 		assertSelectReturns("SELECT sum(int)", zmis, new IntegerValue(6));
 		assertSelectReturns("SELECT sum(float)", zmis, new DoubleValue(5.5));
 		assertSelectReturns("SELECT sum(dur)", zmis, new DurationValue(600));
 
 		assertSelectReturns("SELECT sum(int) WHERE false", zmis, null);
 		assertSelectReturns("SELECT sum(float) WHERE false", zmis, null);
 		assertSelectReturns("SELECT sum(dur) WHERE false", zmis, null);
 		assertSelectThrows("SELECT sum(str)", zmis);
 		
 		assertSelectThrows("SELECT land(false)", zmis);
 		assertSelectThrows("SELECT land(int)", zmis);
 		assertSelectReturns("SELECT land(boolTF)", zmis, new BooleanValue(false));
 		assertSelectReturns("SELECT land(boolTF) WHERE boolTF = true", zmis, new BooleanValue(true));
 		assertSelectReturns("SELECT land(boolTF) WHERE boolTF = false", zmis, new BooleanValue(false));
 		assertSelectReturns("SELECT land(boolTT)", zmis, new BooleanValue(true));
 		assertSelectReturns("SELECT land(boolFF)", zmis, new BooleanValue(false));
 		assertSelectReturns("SELECT land(boolTF) WHERE false", zmis, null);
 		
 		assertSelectThrows("SELECT lor(false)", zmis);
 		assertSelectThrows("SELECT lor(int)", zmis);
 		assertSelectReturns("SELECT lor(boolTF)", zmis, new BooleanValue(true));
 		assertSelectReturns("SELECT lor(boolTF) WHERE boolTF = true", zmis, new BooleanValue(true));
 		assertSelectReturns("SELECT lor(boolTF) WHERE boolTF = false", zmis, new BooleanValue(false));
 		assertSelectReturns("SELECT lor(boolTT)", zmis, new BooleanValue(true));
 		assertSelectReturns("SELECT lor(boolFF)", zmis, new BooleanValue(false));
 		assertSelectReturns("SELECT lor(boolTF) WHERE false", zmis, null);
 		
 		assertSelectThrows("SELECT min(1)", zmis);
 		assertSelectThrows("SELECT min(intList)", zmis);
 		assertSelectReturns("SELECT min(int)", zmis, new IntegerValue(1));
 		assertSelectReturns("SELECT min(int) WHERE int > 1", zmis, new IntegerValue(5));
 		assertSelectReturns("SELECT min(nullInt)", zmis, null);
 		
 		assertSelectThrows("SELECT max(1)", zmis);
 		assertSelectThrows("SELECT max(intList)", zmis);
 		assertSelectReturns("SELECT max(int)", zmis, new IntegerValue(5));
 		assertSelectReturns("SELECT max(int) WHERE int < 5", zmis, new IntegerValue(1));
 		assertSelectReturns("SELECT max(nullInt)", zmis, null);
 		
 		assertSelectThrows("SELECT first(1, 1)", zmis);
 		assertSelectThrows("SELECT first(false, 1)", zmis);
 		assertSelectThrows("SELECT first(1, false)", zmis);
 		assertSelectThrows("SELECT first(false, int)", zmis);
 		assertSelectThrows("SELECT first(1, intList)", zmis);
 		assertSelectReturns("SELECT first(1, int)", zmis, listWith(new IntegerValue(1)));
 		assertSelectThrows("SELECT first(min(nullInt), int)", zmis);
 		assertSelectReturns("SELECT first(2, int)", zmis, listWith(new IntegerValue(1), new IntegerValue(5)));
 		assertSelectReturns("SELECT first(3, int)", zmis, listWith(new IntegerValue(1), new IntegerValue(5)));
 		assertSelectReturns("SELECT first(0, int)", zmis, ListValue.of(SimpleType.INTEGER));
 		assertSelectReturns("SELECT first(-2147483649, int)", zmis, ListValue.of(SimpleType.INTEGER));
 		assertSelectReturns("SELECT first(2147483648, int)", zmis, listWith(new IntegerValue(1), new IntegerValue(5)));
 		assertSelectReturns("SELECT first(3, unfold(intList))", zmis,
 				listWith(new IntegerValue(2), new IntegerValue(3), new IntegerValue(2)));
 		
 		assertSelectThrows("SELECT last(1, 1)", zmis);
 		assertSelectThrows("SELECT last(false, 1)", zmis);
 		assertSelectThrows("SELECT last(1, false)", zmis);
 		assertSelectThrows("SELECT last(false, int)", zmis);
 		assertSelectThrows("SELECT last(1, intList)", zmis);
 		assertSelectReturns("SELECT last(1, int)", zmis, listWith(new IntegerValue(5)));
 		assertSelectThrows("SELECT last(min(nullInt), int)", zmis);
 		assertSelectReturns("SELECT last(2, int)", zmis, listWith(new IntegerValue(1), new IntegerValue(5)));
 		assertSelectReturns("SELECT last(3, int)", zmis, listWith(new IntegerValue(1), new IntegerValue(5)));
 		assertSelectReturns("SELECT last(0, int)", zmis, ListValue.of(SimpleType.INTEGER));
 		assertSelectReturns("SELECT last(-2147483649, int)", zmis, ListValue.of(SimpleType.INTEGER));
 		assertSelectReturns("SELECT last(2147483648, int)", zmis, listWith(new IntegerValue(1), new IntegerValue(5)));
 		assertSelectReturns("SELECT last(3, unfold(intList))", zmis,
 				listWith(new IntegerValue(2), new IntegerValue(3), new IntegerValue(5)));
 		assertSelectReturns("SELECT last(3, unfold(intList)) ORDER BY dur DESC NULLS FIRST", zmis,
 				listWith(new IntegerValue(5), new IntegerValue(2), new IntegerValue(3)));
 		
 		assertSelectThrows("SELECT random(1, 1)", zmis);
 		assertSelectThrows("SELECT random(false, 1)", zmis);
 		assertSelectThrows("SELECT random(1, false)", zmis);
 		assertSelectThrows("SELECT random(false, int)", zmis);
 		assertSelectThrows("SELECT random(1, intList)", zmis);
 		{
 			boolean found1 = false, found5 = false;
 			for (int i = 0; i < 100 && !(found1 && found5); i++) {
 				Value value = evaluateOneValueSelect("SELECT random(1, int)", zmis);
 				if (value.equals(listWith(new IntegerValue(5)))) {
 					found5 = true;
 				} else if (value.equals(listWith(new IntegerValue(1)))) {
 					found1 = true;
 				} else {
 					fail("Unexpected value: " + value);
 				}
 			}
 			assertTrue(found1);
 			assertTrue(found5);
 			
 		}
 		assertSelectThrows("SELECT random(min(nullInt), int)", zmis);
 		assertSelectReturns("SELECT size(random(2, int))", zmis, new IntegerValue(2));
 		assertSelectReturns("SELECT size(random(3, int))", zmis, new IntegerValue(2));
 		assertSelectReturns("SELECT random(0, int)", zmis, ListValue.of(SimpleType.INTEGER));
 		assertSelectReturns("SELECT random(-2147483649, int)", zmis, ListValue.of(SimpleType.INTEGER));
 		assertSelectReturns("SELECT size(random(2147483648, int))", zmis, new IntegerValue(2));
 	}
 	
 	public static void testEqualityAndComparison() throws Exception {
 		ZMI zmi = new ZMI();
 		zmi.addAttribute("id", new IntegerValue(1));
 		zmi.addAttribute("intList", listWith(new IntegerValue(1), new IntegerValue(2)));
 		zmi.addAttribute("nullInt", SimpleType.INTEGER);
 		
 		assertSelectTrue("SELECT 1 = 1");
 		assertSelectFalse("SELECT 1 = 2");
 		assertSelectTrue("SELECT 1 <= 1");
 		assertSelectTrue("SELECT 1 <= 2");
 		assertSelectFalse("SELECT 1 < 1");
 		assertSelectTrue("SELECT 1 < 2");
 		assertSelectTrue("SELECT 1 >= 1");
 		assertSelectFalse("SELECT 1 > 1");
 		assertSelectTrue("SELECT 1 > 0");
 		assertSelectFalse("SELECT 1 >= 2");
 		assertSelectFalse("SELECT 1 <> 1");
 		assertSelectTrue("SELECT 1 <> 2");
 		
 		assertSelectTrue("SELECT true > false");
 		assertSelectFalse("SELECT true < false");
 		
 		assertSelectTrue("SELECT 1.0 > 0.0");
 		assertSelectTrue("SELECT 1.0 = 1.0");
 		assertSelectFalse("SELECT 1.0 < 0.0");
 		
 		assertSelectTrue("SELECT to_duration(100) = to_duration(100)");
 		assertSelectFalse("SELECT to_duration(100) = to_duration(200)");
 		assertSelectFalse("SELECT to_duration(100) < to_duration(100)");
 		assertSelectFalse("SELECT to_duration(100) > to_duration(100)");
 		assertSelectTrue("SELECT to_duration(200) > to_duration(100)");
 		
 		assertSelectTrue("SELECT \"aa\" = \"aa\"");
 		assertSelectTrue("SELECT \"a	a\" = \"a\ta\"");
 		assertSelectFalse("SELECT \"a	a\" <> \"a\ta\"");
 		assertSelectTrue("SELECT \"a\" < \"aa\"");
 		assertSelectFalse("SELECT \"aa\" < \"aa\"");
 		
 		assertSelectTrue("SELECT to_time(\"1999/01/01 02:12:34.123 CET\") < to_time(\"1999/02/01 02:12:34.123 CET\")");
 		assertSelectTrue("SELECT to_time(\"1999/01/01 02:12:34.123 CET\") < to_time(\"1999/02/01 02:12:34.123 CET\")");
 		assertSelectFalse("SELECT to_time(\"1999/01/01 02:12:34.123 CET\") > to_time(\"1999/02/01 02:12:34.123 CET\")");
 		assertSelectTrue("SELECT to_time(\"1999/02/01 02:12:34.123 CET\") = to_time(\"1999/02/01 02:12:34.123 CET\")");
 		
 		assertSelectThrows("SELECT 1 = false");
 		assertSelectThrows("SELECT 1 = false");
 		
 		assertSelectTrue("SELECT count(id) = 1 WHERE is_null(nullInt)", zmi);
 		assertSelectTrue("SELECT count(id) = 1 WHERE is_null(1 = nullInt)", zmi);
 		assertSelectTrue("SELECT count(id) = 1 WHERE is_null(1 <> nullInt)", zmi);
 		assertSelectTrue("SELECT count(id) = 1 WHERE is_null(1 > nullInt)", zmi);
 		assertSelectTrue("SELECT count(id) = 1 WHERE is_null(1 < nullInt)", zmi);
 		assertSelectTrue("SELECT count(id) = 1 WHERE is_null(1 <= nullInt)", zmi);
 		assertSelectTrue("SELECT count(id) = 1 WHERE is_null(1 >= nullInt)", zmi);
 		assertSelectThrows("SELECT true = nullInt", zmi);
 	}
 	
 	public static void testLogicalOperators() throws Exception {
 		ZMI zmi = new ZMI();
 		zmi.addAttribute("id", new IntegerValue(1));
 		zmi.addAttribute("nullBool", SimpleType.BOOLEAN);
 		
 		assertSelectTrue("SELECT count(id) = 0 WHERE false OR false", zmi);
 		assertSelectTrue("SELECT count(id) = 1 WHERE true OR false", zmi);
 		assertSelectTrue("SELECT count(id) = 1 WHERE false OR true", zmi);
 		assertSelectTrue("SELECT count(id) = 1 WHERE true OR true", zmi);
 		assertSelectTrue("SELECT count(id) = 0 WHERE nullBool OR false", zmi);
 		assertSelectTrue("SELECT count(id) = 0 WHERE nullBool OR true", zmi);
 		assertSelectTrue("SELECT count(id) = 0 WHERE false OR nullBool", zmi);
 		assertSelectTrue("SELECT count(id) = 0 WHERE true OR nullBool", zmi);
 		assertSelectTrue("SELECT count(id) = 0 WHERE nullBool OR nullBool", zmi);
 
 		assertSelectTrue("SELECT count(id) = 0 WHERE false AND false", zmi);
 		assertSelectTrue("SELECT count(id) = 0 WHERE true AND false", zmi);
 		assertSelectTrue("SELECT count(id) = 0 WHERE false AND true", zmi);
 		assertSelectTrue("SELECT count(id) = 1 WHERE true AND true", zmi);
 		assertSelectTrue("SELECT count(id) = 0 WHERE nullBool AND false", zmi);
 		assertSelectTrue("SELECT count(id) = 0 WHERE nullBool AND true", zmi);
 		assertSelectTrue("SELECT count(id) = 0 WHERE false AND nullBool", zmi);
 		assertSelectTrue("SELECT count(id) = 0 WHERE true AND nullBool", zmi);
 		assertSelectTrue("SELECT count(id) = 0 WHERE nullBool AND nullBool", zmi);
 		
 		assertSelectTrue("SELECT count(id) = 1 WHERE NOT false", zmi);
 		assertSelectTrue("SELECT count(id) = 0 WHERE NOT true", zmi);
 		assertSelectTrue("SELECT count(id) = 0 WHERE NOT nullBool", zmi);
 		
 		assertSelectThrows("SELECT 1 AND 2");
 		assertSelectThrows("SELECT 1 OR 2");
 		assertSelectThrows("SELECT NOT 2");
 	}
 	
 	public static void testArithmeticOperators() throws Exception {
 		ZMI zmi = new ZMI();
 		zmi.addAttribute("id", new IntegerValue(1));
 		zmi.addAttribute("nullBool", SimpleType.BOOLEAN);
 		zmi.addAttribute("nullInt", SimpleType.INTEGER);
 		zmi.addAttribute("nullFloat", SimpleType.DOUBLE);
 		zmi.addAttribute("nullDur", SimpleType.DURATION);
 		zmi.addAttribute("nullTime", SimpleType.TIME);
 		zmi.addAttribute("nullStr", SimpleType.STRING);
 		
 		// ADD
 		
 		assertSelectReturns("SELECT 1 + 1", new IntegerValue(2));
 		assertSelectThrows("SELECT 1 + false");
 		assertSelectTrue("SELECT count(id) = 1 WHERE is_null(1 + nullInt)", zmi);
 		assertSelectTrue("SELECT count(id) = 1 WHERE is_null(nullInt + 1)", zmi);
 		assertSelectThrows("SELECT count(id) = 1 WHERE is_null(1 + nullBool)", zmi);
 		
 		assertSelectTrue("SELECT count(id) = 1 WHERE 2.0 + 3.5 = 5.5", zmi);
 		assertSelectTrue("SELECT count(id) = 1 WHERE is_null(2.0 + nullFloat)", zmi);
 		assertSelectTrue("SELECT count(id) = 1 WHERE is_null(nullFloat + 2.0)", zmi);
 		
 		assertSelectTrue("SELECT count(id) = 1 WHERE to_duration(100) + to_duration(200) = to_duration(300)", zmi);
 		assertSelectTrue("SELECT count(id) = 1 WHERE is_null(to_duration(100) + nullDur)", zmi);
 		assertSelectTrue("SELECT count(id) = 1 WHERE is_null(nullDur + to_duration(100))", zmi);
 		
 		assertSelectTrue("SELECT count(id) = 1 WHERE to_duration(100) + to_time(\"1999/01/01 02:12:34.123 CET\") = to_time(\"1999/01/01 02:12:34.223 CET\")", zmi);
 		assertSelectTrue("SELECT count(id) = 1 WHERE is_null(to_duration(100) + nullTime)", zmi);
 		assertSelectTrue("SELECT count(id) = 1 WHERE is_null(nullDur + to_time(\"1999/01/01 02:12:34.123 CET\"))", zmi);
 		assertSelectTrue("SELECT count(id) = 1 WHERE to_time(\"1999/01/01 02:12:34.123 CET\") + to_duration(100) = to_time(\"1999/01/01 02:12:34.223 CET\")", zmi);
 		assertSelectTrue("SELECT count(id) = 1 WHERE is_null(nullTime + to_duration(100))", zmi);
 		assertSelectTrue("SELECT count(id) = 1 WHERE is_null(to_time(\"1999/01/01 02:12:34.123 CET\") + nullDur)", zmi);
 		
 		assertSelectTrue("SELECT \"f\" + \"oo\" = \"foo\"", zmi);
 		assertSelectTrue("SELECT count(id) = 1 WHERE is_null(\"f\" + nullStr)", zmi);
 		assertSelectTrue("SELECT count(id) = 1 WHERE is_null(nullStr + \"f\")", zmi);
 		
 		assertSelectThrows("SELECT false + true");
 		
 		// SUB
 
 		assertSelectReturns("SELECT 1 - 5", new IntegerValue(-4));
 		assertSelectThrows("SELECT 1 - false");
 		assertSelectTrue("SELECT count(id) = 1 WHERE is_null(1 - nullInt)", zmi);
 		assertSelectTrue("SELECT count(id) = 1 WHERE is_null(nullInt - 1)", zmi);
 		assertSelectThrows("SELECT count(id) = 1 WHERE is_null(1 - nullBool)", zmi);
 		
 		assertSelectTrue("SELECT count(id) = 1 WHERE 2.0 - 3.5 = -1.5", zmi);
 		assertSelectTrue("SELECT count(id) = 1 WHERE is_null(2.0 - nullFloat)", zmi);
 		assertSelectTrue("SELECT count(id) = 1 WHERE is_null(nullFloat - 2.0)", zmi);
 		
 		assertSelectTrue("SELECT count(id) = 1 WHERE to_duration(100) - to_duration(200) = to_duration(-100)", zmi);
 		assertSelectTrue("SELECT count(id) = 1 WHERE is_null(to_duration(100) - nullDur)", zmi);
 		assertSelectTrue("SELECT count(id) = 1 WHERE is_null(nullDur - to_duration(100))", zmi);
 		
 		assertSelectThrows("SELECT count(id) = 1 WHERE is_null(to_duration(100) - nullTime)", zmi);
 		assertSelectTrue("SELECT count(id) = 1 WHERE to_time(\"1999/01/01 02:12:34.123 CET\") - to_duration(100) = to_time(\"1999/01/01 02:12:34.023 CET\")", zmi);
 		assertSelectTrue("SELECT count(id) = 1 WHERE is_null(nullTime - to_duration(100))", zmi);
 		assertSelectTrue("SELECT count(id) = 1 WHERE is_null(to_time(\"1999/01/01 02:12:34.123 CET\") - nullDur)", zmi);
 		
 		assertSelectTrue("SELECT count(id) = 1 WHERE to_time(\"1999/01/01 02:12:34.123 CET\") - to_time(\"1999/01/01 02:12:38.123 CET\") = to_duration(-4000)", zmi);
 		assertSelectTrue("SELECT count(id) = 1 WHERE is_null(to_time(\"1999/01/01 02:12:34.123 CET\") - nullTime)", zmi);
 		assertSelectTrue("SELECT count(id) = 1 WHERE is_null(nullTime - to_time(\"1999/01/01 02:12:34.123 CET\"))", zmi);
 		
 		assertSelectThrows("SELECT false - true");
 		
 		// MUL
 		
 		assertSelectReturns("SELECT 7 * 2", new IntegerValue(14));
 		assertSelectThrows("SELECT 7 * false");
 		assertSelectTrue("SELECT count(id) = 1 WHERE is_null(7 * nullInt)", zmi);
 		assertSelectTrue("SELECT count(id) = 1 WHERE is_null(nullInt * 7)", zmi);
 		assertSelectThrows("SELECT count(id) = 1 WHERE is_null(7 * nullBool)", zmi);
 
 		assertSelectTrue("SELECT count(id) = 1 WHERE 2.0 * 4.5 = 9.0", zmi);
 		assertSelectTrue("SELECT count(id) = 1 WHERE is_null(2.0 * nullFloat)", zmi);
 		assertSelectTrue("SELECT count(id) = 1 WHERE is_null(nullFloat * 2.0)", zmi);
 		
 		assertSelectTrue("SELECT count(id) = 1 WHERE to_duration(100) * 3 = to_duration(300)", zmi);
 		assertSelectTrue("SELECT count(id) = 1 WHERE is_null(to_duration(100) * nullInt)", zmi);
 		assertSelectTrue("SELECT count(id) = 1 WHERE is_null(nullDur * 3)", zmi);
 		assertSelectTrue("SELECT count(id) = 1 WHERE 3 * to_duration(100) = to_duration(300)", zmi);
 		assertSelectTrue("SELECT count(id) = 1 WHERE is_null(nullInt * to_duration(100))", zmi);
 		assertSelectTrue("SELECT count(id) = 1 WHERE is_null(77 * nullDur)", zmi);
 		
 		assertSelectThrows("SELECT false * false");
 		
 		// DIV
 		
 		assertSelectReturns("SELECT 7 / 2", new DoubleValue(3.5));
 		assertSelectThrows("SELECT 7 / false");
 		assertSelectThrows("SELECT 7 / 0");
 		assertSelectTrue("SELECT 0 / 2 = 0.0");
 		assertSelectTrue("SELECT count(id) = 1 WHERE is_null(7 / nullInt)", zmi);
 		assertSelectTrue("SELECT count(id) = 1 WHERE is_null(nullInt / 0)", zmi);
 		
 		assertSelectTrue("SELECT count(id) = 1 WHERE 4.0 / 8.0 = 0.5", zmi);
 		assertSelectThrows("SELECT 7.0 / 0.0");
 		assertSelectTrue("SELECT count(id) = 1 WHERE is_null(0.0 / nullFloat)", zmi);
 		assertSelectTrue("SELECT count(id) = 1 WHERE is_null(nullFloat / 0.0)", zmi);
 		
 		assertSelectTrue("SELECT count(id) = 1 WHERE to_duration(200) / to_duration(10) = 20.0", zmi);
 		assertSelectTrue("SELECT count(id) = 1 WHERE to_duration(200) / to_duration(400) = 0.5", zmi);
 		assertSelectThrows("SELECT count(id) = 1 WHERE to_duration(200) / to_duration(0) = 20.0", zmi);
 		assertSelectTrue("SELECT count(id) = 1 WHERE is_null(nullDur / to_duration(0))", zmi);
 		assertSelectTrue("SELECT count(id) = 1 WHERE is_null(to_duration(0) / nullDur)", zmi);
 		
 		assertSelectTrue("SELECT count(id) = 1 WHERE to_duration(200) / 10 = to_duration(20)", zmi);
 		assertSelectThrows("SELECT count(id) = 1 WHERE to_duration(200) / 0 = to_duration(20)", zmi);
 		assertSelectTrue("SELECT count(id) = 1 WHERE is_null(to_duration(200) / nullInt)", zmi);
 		assertSelectTrue("SELECT count(id) = 1 WHERE is_null(nullDur / 0)", zmi);
 		
 		// MOD
 		
 		assertSelectReturns("SELECT 7 % 3", new IntegerValue(7 % 3));
 		assertSelectThrows("SELECT 7 % false");
 		assertSelectThrows("SELECT 7 % 0");
 		assertSelectTrue("SELECT 0 % 2 = 0");
 		assertSelectTrue("SELECT count(id) = 1 WHERE is_null(7 % nullInt)", zmi);
 		assertSelectTrue("SELECT count(id) = 1 WHERE is_null(nullInt % 0)", zmi);
 
 		assertSelectTrue("SELECT count(id) = 1 WHERE 7.5 % 3.5 = 0.5", zmi);
 		assertSelectThrows("SELECT 7.0 % 0.0");
 		assertSelectTrue("SELECT count(id) = 1 WHERE is_null(0.0 % nullFloat)", zmi);
 		assertSelectTrue("SELECT count(id) = 1 WHERE is_null(nullFloat % 0.0)", zmi);
 
 		assertSelectTrue("SELECT to_duration(7) % to_duration(3) = to_duration(1)");
 		assertSelectThrows("SELECT to_duration(7) % to_duration(0)");
 		assertSelectTrue("SELECT to_duration(0) % to_duration(666) = to_duration(0)");
 		assertSelectTrue("SELECT count(id) = 1 WHERE is_null(to_duration(0) % nullDur)", zmi);
 		assertSelectTrue("SELECT count(id) = 1 WHERE is_null(nullDur % to_duration(0))", zmi);
 		
 		// NEG
 		assertSelectReturns("SELECT -(7)", new IntegerValue(-7));
 		assertSelectReturns("SELECT ----------(7)", new IntegerValue(7));
 		assertSelectTrue("SELECT count(id) = 1 WHERE is_null(-nullInt)", zmi);
 		
 		assertSelectReturns("SELECT -(7.5)", new DoubleValue(-7.5));
 		assertSelectTrue("SELECT count(id) = 1 WHERE is_null(-nullFloat)", zmi);
 		
 		assertSelectReturns("SELECT -(to_duration(6789))", new DurationValue(-6789));
 		assertSelectTrue("SELECT count(id) = 1 WHERE is_null(-nullDur)", zmi);
 	}
 	
 	public static void testFunctions() throws Exception {
 		ZMI zmi = new ZMI();
 		zmi.addAttribute("id", new IntegerValue(1));
 		zmi.addAttribute("intList", listWith(new IntegerValue(1), new IntegerValue(2)));
 		zmi.addAttribute("intListAsSet", setWith(new IntegerValue(1), new IntegerValue(2)));
 		zmi.addAttribute("nullIntList", ListType.of(SimpleType.INTEGER));
 		zmi.addAttribute("intSet", setWith(new IntegerValue(6)));
 		zmi.addAttribute("intSetAsList", listWith(new IntegerValue(6)));
 		zmi.addAttribute("nullIntSet", SetType.of(SimpleType.INTEGER));
 		zmi.addAttribute("nullInt", SimpleType.INTEGER);
 		zmi.addAttribute("nullFloat", SimpleType.DOUBLE);
 		zmi.addAttribute("nullStr", SimpleType.STRING);
 		zmi.addAttribute("nullDur", SimpleType.DURATION);
 		zmi.addAttribute("curTime", new TimeValue(new Date().getTime()));
 		
 		// to_string
 		
 		assertSelectReturns("SELECT to_string(true)", zmi, new StringValue("true"));
 		assertSelectReturns("SELECT to_string(false)", zmi, new StringValue("false"));
 		assertSelectReturns("SELECT to_string(2.0)", zmi, new StringValue("2.0"));
 		assertSelectReturns("SELECT to_string(15)", zmi, new StringValue("15"));
 		assertSelectReturns("SELECT to_string(to_duration(15))", zmi, new StringValue("+0 00:00:00.015"));
 		assertSelectTrue("SELECT count(id) = 1 WHERE to_string(nullInt) = \"NULL\"", zmi);
 		assertSelectTrue("SELECT count(id) = 1 WHERE to_string(nullInt) <> \"null\"", zmi);
 		assertSelectTrue("SELECT count(id) = 1 WHERE to_string(intList) = \"[ 1, 2 ]\"", zmi);
 		assertSelectTrue("SELECT count(id) = 1 WHERE to_string(intSet) = \"{ 6 }\"", zmi);
 		
 		// is_null
 		
 		assertSelectTrue("SELECT count(id) = 1 WHERE is_null(nullInt)", zmi);
 		assertSelectTrue("SELECT count(id) = 1 WHERE is_null(id) = false", zmi);
 		
 		// to_integer (from double, string, duration)
 		
 		assertSelectTrue("SELECT count(id) = 1 WHERE to_integer(\"123\") = 123", zmi);
 		assertSelectTrue("SELECT count(id) = 1 WHERE to_integer(\"-123\") = -123", zmi);
 		assertSelectThrows("SELECT count(id) = 1 WHERE to_integer(\"abc\") = -123", zmi);
 		assertSelectTrue("SELECT count(id) = 1 WHERE is_null(to_integer(nullStr))", zmi);
 		
 		assertSelectTrue("SELECT count(id) = 1 WHERE to_integer(123.1) = 123", zmi);
 		assertSelectTrue("SELECT count(id) = 1 WHERE to_integer(123.5) = 124", zmi);
 		assertSelectTrue("SELECT count(id) = 1 WHERE is_null(to_integer(nullFloat))", zmi);
 		
 		assertSelectTrue("SELECT count(id) = 1 WHERE to_integer(to_duration(123)) = 123", zmi);
 		assertSelectTrue("SELECT count(id) = 1 WHERE to_integer(to_duration(-123)) = -123", zmi);
 		assertSelectTrue("SELECT count(id) = 1 WHERE is_null(to_integer(nullDur))", zmi);
 		
 		assertSelectThrows("SELECT is_null(to_integer(false))");
 		
 		// to_double (from integer, string)
 		
 		assertSelectTrue("SELECT count(id) = 1 WHERE to_double(\"123.25\") = 123.25", zmi);
 		assertSelectTrue("SELECT count(id) = 1 WHERE to_double(\"-123.25\") = -123.25", zmi);
 		assertSelectThrows("SELECT count(id) = 1 WHERE to_double(\"abc\") = -123.25", zmi);
 		assertSelectTrue("SELECT count(id) = 1 WHERE is_null(to_double(nullStr))", zmi);
 		
 		assertSelectTrue("SELECT count(id) = 1 WHERE to_double(123) = 123.0", zmi);
 		assertSelectTrue("SELECT count(id) = 1 WHERE to_double(-123) = -123.0", zmi);
 		assertSelectTrue("SELECT count(id) = 1 WHERE is_null(to_double(nullInt))", zmi);
 
 		assertSelectThrows("SELECT is_null(to_double(false))");
 		
 		// to_boolean (from string)
 		
 		assertSelectTrue("SELECT count(id) = 1 WHERE to_boolean(\"true\") = true", zmi);
 		assertSelectTrue("SELECT count(id) = 1 WHERE to_boolean(\"false\") = false", zmi);
 		assertSelectThrows("SELECT count(id) = 1 WHERE to_boolean(\"abc\") = true", zmi);
 		assertSelectTrue("SELECT count(id) = 1 WHERE is_null(to_boolean(nullStr))", zmi);
 
 		assertSelectThrows("SELECT is_null(to_boolean(12))");
 		
 		// to_duration (from integer, string)
 		
 		assertSelectReturns("SELECT to_duration(1234)", new DurationValue(1234));
 		assertSelectReturns("SELECT to_duration(-1234)", new DurationValue(-1234));
 		assertSelectTrue("SELECT count(id) = 1 WHERE is_null(to_duration(nullInt))", zmi);
 		
 		assertSelectReturns("SELECT to_duration(\"+1 23:45:12.345\")", zmi,
 				new DurationValue((((1 * 24 + 23) * 60 + 45) * 60 + 12) * 1000 + 345));
 		assertSelectReturns("SELECT to_duration(\"-1 23:45:12.345\")", zmi,
 				new DurationValue(-((((1 * 24 + 23) * 60 + 45) * 60 + 12) * 1000 + 345)));
 		assertSelectThrows("SELECT to_duration(\"abc\")", zmi);
 		assertSelectThrows("SELECT to_duration(\"+1 23:45:92.345\")", zmi);
 		assertSelectThrows("SELECT to_duration(\"+1 24:45:12.345\")", zmi);
 		assertSelectThrows("SELECT to_duration(\"+1 24:60:12.345\")", zmi);
 		assertSelectTrue("SELECT count(id) = 1 WHERE is_null(to_duration(nullStr))", zmi);
 		
 		assertSelectThrows("SELECT is_null(to_duration(false))");
 		
 		// to_time (from string)
 		
 		assertSelectTrue("SELECT count(id) = 1 WHERE to_time(to_string(curTime)) = curTime", zmi);
 		assertSelectTrue("SELECT count(id) = 1 WHERE is_null(to_time(nullStr))", zmi);
 		assertSelectThrows("SELECT count(id) = 1 WHERE to_time(\"yesterday\")", zmi);
 		
 		assertSelectThrows("SELECT is_null(to_time(false))");
 		
 		// to_set
 		
 		{
 			ZMI zmi2 = new ZMI();
 			zmi2.addAttribute("id", new IntegerValue(1));
 			zmi2.addAttribute("intList", listWith(new IntegerValue(1), new IntegerValue(2), new IntegerValue(1),
 					new IntegerValue(3)));
 			zmi2.addAttribute("intListAsSet", setWith(new IntegerValue(1), new IntegerValue(2), new IntegerValue(3)));
 			
 			assertSelectTrue("SELECT count(id) = 1 WHERE to_set(intList) = intListAsSet", zmi2);
 			assertSelectTrue("SELECT count(id) = 1 WHERE is_null(to_set(nullIntList))", zmi);
 			assertSelectThrows("SELECT count(id) = 1 WHERE is_null(to_set(12))", zmi);
 		}
 		
 		// to_list
 		
 		assertSelectTrue("SELECT count(id) = 1 WHERE to_list(intSet) = intSetAsList", zmi);
 		assertSelectTrue("SELECT count(id) = 1 WHERE is_null(to_list(nullIntSet))", zmi);
 		assertSelectThrows("SELECT count(id) = 1 WHERE is_null(to_list(12))", zmi);
 		
 		// now
 		
 		assertSelectTrue("SELECT count(id) = 1 WHERE now() - to_duration(100) <= curTime AND curTime <= now()", zmi);
 		
 		// epoch
 		
 		assertSelectTrue("SELECT epoch() = to_time(\"2000/01/01 00:00:00.000 CET\")");
 		
 		// size
 		
 		assertSelectTrue("SELECT count(id) = 1 WHERE size(intList) = 2", zmi);
 		assertSelectTrue("SELECT count(id) = 1 WHERE size(intSet) = 1", zmi);
 		assertSelectTrue("SELECT count(id) = 1 WHERE size(\"two\") = 3", zmi);
 		assertSelectTrue("SELECT count(id) = 1 WHERE is_null(size(nullIntSet))", zmi);
 		assertSelectTrue("SELECT count(id) = 1 WHERE is_null(size(nullIntList))", zmi);
 		assertSelectTrue("SELECT count(id) = 1 WHERE is_null(size(nullStr))", zmi);
 		
 		// round
 		
 		assertSelectTrue("SELECT count(id) = 1 WHERE round(1.0) = 1.0", zmi);
 		assertSelectTrue("SELECT count(id) = 1 WHERE round(1.1) = 1.0", zmi);
 		assertSelectTrue("SELECT count(id) = 1 WHERE round(1.5) = 2.0", zmi);
 		assertSelectTrue("SELECT count(id) = 1 WHERE round(1.7) = 2.0", zmi);
 		assertSelectTrue("SELECT count(id) = 1 WHERE is_null(round(nullFloat))", zmi);
 		
 		// floor
 		
 		assertSelectTrue("SELECT count(id) = 1 WHERE floor(1.0) = 1.0", zmi);
 		assertSelectTrue("SELECT count(id) = 1 WHERE floor(1.1) = 1.0", zmi);
 		assertSelectTrue("SELECT count(id) = 1 WHERE floor(1.5) = 1.0", zmi);
 		assertSelectTrue("SELECT count(id) = 1 WHERE floor(1.7) = 1.0", zmi);
 		assertSelectTrue("SELECT count(id) = 1 WHERE is_null(floor(nullFloat))", zmi);
 		
 		// ceil
 		
 		assertSelectTrue("SELECT count(id) = 1 WHERE ceil(1.0) = 1.0", zmi);
 		assertSelectTrue("SELECT count(id) = 1 WHERE ceil(1.1) = 2.0", zmi);
 		assertSelectTrue("SELECT count(id) = 1 WHERE ceil(1.5) = 2.0", zmi);
 		assertSelectTrue("SELECT count(id) = 1 WHERE ceil(1.7) = 2.0", zmi);
 		assertSelectTrue("SELECT count(id) = 1 WHERE is_null(ceil(nullFloat))", zmi);
 	}
 	
 	public static void testMisc() throws Exception {
 		ZMI zmi1 = new ZMI();
 		zmi1.addAttribute("id", new IntegerValue(1));
 		zmi1.addAttribute("nullStr", SimpleType.STRING);
 		zmi1.addAttribute("intList", listWith(new IntegerValue(1), new IntegerValue(2)));
 		zmi1.addAttribute("int1", new IntegerValue(5));
 		zmi1.addAttribute("int2", new IntegerValue(7));
 		zmi1.addAttribute("float", new DoubleValue(1.3));
 		
 		ZMI zmi2 = new ZMI();
 		zmi2.addAttribute("id", new IntegerValue(2));
 		zmi2.addAttribute("intList", listWith(new IntegerValue(3), new IntegerValue(4), new IntegerValue(5)));
 		zmi2.addAttribute("int1", new IntegerValue(10));
 		zmi2.addAttribute("int2", new IntegerValue(8));
 		zmi2.addAttribute("float", new DoubleValue(2.5));
 		
 		List<ZMI> zmis = new ArrayList<ZMI>();
 		zmis.add(zmi1); zmis.add(zmi2);
 		
 		// RegexpExpr
 		
 		assertSelectTrue("SELECT \"aaa\" REGEXP \"aaa\"");
 		assertSelectTrue("SELECT \"aaaaa\" REGEXP \"aaa\"");
 		assertSelectTrue("SELECT NOT \"aaa\" REGEXP \"b+\"");
 		assertSelectTrue("SELECT count(id) = 1 WHERE is_null(nullStr REGEXP \"aa\")", zmi1);
 		
 		// OneResult, ColumnResult, ListResult mixing
 		
 		assertSelectTrue("SELECT sum(int1 - int2) = 0", zmis);
 		assertSelectTrue("SELECT sum(to_double(int1) - to_double(int2)) = 0.0", zmis);
 		assertSelectThrows("SELECT sum(int1 - distinct(int2)) = 0", zmis);
 		assertSelectTrue("SELECT min(int1 - int2) = -2", zmis);
 		assertSelectTrue("SELECT max(int1 - int2) = 2", zmis);
 		assertSelectTrue("SELECT sum(int1 * 2) = 30", zmis);
 		assertSelectTrue("SELECT sum(2 * int1) = 30", zmis);
 		assertSelectThrows("SELECT sum(int1 - unfold(intList))", zmis);
 		assertSelectThrows("SELECT sum(unfold(intList) - int1)", zmis);
 		assertSelectTrue("SELECT sum(unfold(intList) * 2) = 30", zmis);
 		assertSelectTrue("SELECT min(unfold(intList) - 5) = -4", zmis);
 		assertSelectTrue("SELECT min(5 - unfold(intList)) = 0", zmis);
 		
 		assertSelectTrue("SELECT avg(floor(float)) = 1.5", zmis);
 		assertSelectTrue("SELECT avg(round(float)) = 2.0", zmis);
 		
 		assertSelectThrows("SELECT not_found");
 		assertSelectThrows("SELECT min(1, 2, 3)");
 		assertSelectThrows("SELECT float(1, 2, 3)");
 		assertSelectThrows("SELECT not_found(1)");
 		assertSelectThrows("SELECT 1 <> false");
 		assertSelectThrows("SELECT 1 WHERE 1", zmi1);
 		assertSelectThrows("SELECT (SELECT 1, 2)", zmi1);
 	}
 	
 	public static void testSelect() throws Exception {
 		ZMI zmi = new ZMI();
 		zmi.addAttribute("id", new IntegerValue(1));
 		List<ZMI> zmis = new ArrayList<ZMI>();
 		
 		assertSelectTrue("SELECT (SELECT true)");
 		assertSelectTrue("SELECT (SELECT true AS xyz)");
 		assertSelectThrows("SELECT (SELECT id AS xyz)", zmi);
 		assertSelectThrows("SELECT (SELECT distinct(id) AS xyz)", zmi);
 		
 		List<SelectionResult> result = evaluateSelect("SELECT 1 AS a, 2, 3 AS b", zmis);
 		assertEquals(3, result.size());
 		assertEquals(result.get(0).getName(), "a");
 		assertEquals(result.get(0).getValue(), new IntegerValue(1));
 		assertEquals(result.get(1).getName(), null);
 		assertEquals(result.get(1).getValue(), new IntegerValue(2));
 		assertEquals(result.get(2).getName(), "b");
 		assertEquals(result.get(2).getValue(), new IntegerValue(3));
 	}
 	
 	// Helpers
 	
 	private static <V extends SimpleValue> ListValue<V> listWith(V... items) {
 		@SuppressWarnings("unchecked")
 		ListValue<V> retVal = ListValue.of((SimpleType<V>) items[0].getType());
 		for (V item : items) {
 			retVal.addItem(item);
 		}
 		return retVal;
 	}
 	
 	private static <V extends SimpleValue> SetValue<V> setWith(V... items) {
 		@SuppressWarnings("unchecked")
 		SetValue<V> retVal = SetValue.of((SimpleType<V>) items[0].getType());
 		for (V item : items) {
 			retVal.addItem(item);
 		}
 		return retVal;
 	}
 	
 	private static void assertSelectThrows(String source) throws ParseException {
 		List<ZMI> zmis = new ArrayList<ZMI>();
 		assertSelectThrows(source, zmis);
 	}
 	
 	private static void assertSelectThrows(String source, ZMI zmi) throws ParseException {
 		List<ZMI> zmis = new ArrayList<ZMI>();
 		zmis.add(zmi);
 		assertSelectThrows(source, zmis);
 	}
 	
 	private static void assertSelectThrows(String source, List<ZMI> zmis) throws ParseException {
 		try {
 			Value value = evaluateOneValueSelect(source, zmis);
 			fail("EvaluationException not thrown, returned " + value);
 		}
 		catch (EvaluationException ex) {
 			// Uncomment to see error messages
 			// System.out.println(ex.getMessage());
 		}
 	}
 	
 	private static void assertSelectReturns(String source, Value expected) throws EvaluationException, ParseException {
 		List<ZMI> zmis = new ArrayList<ZMI>();
 		assertSelectReturns(source, zmis, expected);
 	}
 
 	private static void assertSelectReturns(String source, ZMI zmi, Value expected) throws EvaluationException, ParseException {
 		List<ZMI> zmis = new ArrayList<ZMI>();
 		zmis.add(zmi);
 		assertSelectReturns(source, zmis, expected);
 	}
 
 	private static void assertSelectReturns(String source, List<ZMI> zmis, Value expected) throws EvaluationException, ParseException {
 		Value actual = evaluateOneValueSelect(source, zmis);
 		assertEquals(expected, actual);
 	}
 	
 	private static void assertSelectTrue(String source, List<ZMI> zmis) throws EvaluationException, ParseException {
 		assertSelectReturns(source, zmis, new BooleanValue(true));
 	}
 	
 	private static void assertSelectTrue(String source, ZMI zmi) throws EvaluationException, ParseException {
 		assertSelectReturns(source, zmi, new BooleanValue(true));
 	}
 	
 	private static void assertSelectFalse(String source) throws EvaluationException, ParseException {
 		assertSelectReturns(source, new BooleanValue(false));
 	}
 	
 	private static void assertSelectTrue(String source) throws EvaluationException, ParseException {
 		assertSelectReturns(source, new BooleanValue(true));
 	}
 
 	private static Value evaluateOneValueSelect(String source, List<ZMI> zmis) throws EvaluationException, ParseException {
 		return evaluateSelect(source, zmis).get(0).getValue();
 	}
 	
 	private static List<SelectionResult> evaluateSelect(String source, List<ZMI> zmis) throws EvaluationException, ParseException {
 		SelectStmt select = (SelectStmt) Parsers.parseQuery(source).get(0);
 		Env env = Env.createFromZMIs(zmis);
 		return select.evaluate(env);
 	}
 }
