 /*
  * JparsecQlParserTest.java
  * Created on Jul 16, 2013
  *
  * Copyright 2013 Lithium Technologies, Inc. 
  * Emeryville, California, U.S.A.  All Rights Reserved.
  *
  * This software is the  confidential and proprietary information
  * of  Lithium  Technologies,  Inc.  ("Confidential Information")
  * You shall not disclose such Confidential Information and shall 
  * use  it  only in  accordance  with  the terms of  the  license 
  * agreement you entered into with Lithium.
  */
 
 package com.lithium.ldn.starql.parsers;
 
 import static com.lithium.ldn.starql.models.QlConstraintOperator.EQUALS;
 import static com.lithium.ldn.starql.models.QlConstraintOperator.GREATER_THAN;
 import static com.lithium.ldn.starql.models.QlConstraintOperator.GREATER_THAN_EQUAL;
 import static com.lithium.ldn.starql.models.QlConstraintOperator.IN;
 import static com.lithium.ldn.starql.models.QlConstraintOperator.LESS_THAN;
 import static com.lithium.ldn.starql.models.QlConstraintOperator.LESS_THAN_EQUAL;
 import static com.lithium.ldn.starql.models.QlConstraintOperator.NOT_EQUALS;
 import static com.lithium.ldn.starql.models.QlConstraintOperator.MATCHES;
 import static com.lithium.ldn.starql.models.QlConstraintOperator.LIKE;
 import static com.lithium.ldn.starql.models.QlConstraintPairOperator.AND;
 import static com.lithium.ldn.starql.models.QlConstraintPairOperator.OR;
 import static com.lithium.ldn.starql.models.QlSortOrderType.ASC;
 import static com.lithium.ldn.starql.models.QlSortOrderType.DESC;
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertTrue;
 
 import java.util.Date;
 import java.util.List;
 
 import org.codehaus.jparsec.error.ParserException;
 import org.junit.Assert;
 import org.junit.Test;
 
 import com.fasterxml.jackson.databind.util.ISO8601Utils;
 import com.google.common.collect.Lists;
 import com.lithium.ldn.starql.exceptions.InvalidQueryException;
 import com.lithium.ldn.starql.exceptions.QueryValidationException;
 import com.lithium.ldn.starql.models.QlBooleanConstraintNode;
 import com.lithium.ldn.starql.models.QlConstraint;
 import com.lithium.ldn.starql.models.QlConstraintOperator;
 import com.lithium.ldn.starql.models.QlConstraintPair;
 import com.lithium.ldn.starql.models.QlConstraintValue;
 import com.lithium.ldn.starql.models.QlConstraintValueCollection;
 import com.lithium.ldn.starql.models.QlConstraintValueDate;
 import com.lithium.ldn.starql.models.QlConstraintValueNumber;
 import com.lithium.ldn.starql.models.QlConstraintValueString;
 import com.lithium.ldn.starql.models.QlField;
 import com.lithium.ldn.starql.models.QlPageConstraints;
 import com.lithium.ldn.starql.models.QlSelectStatement;
 import com.lithium.ldn.starql.models.QlSortClause;
 import com.lithium.ldn.starql.models.QlSortOrderType;
 
 /**
  * @author David Esposito
  */
 
 public class JparsecQlParserTest {
 
 	private static final JparsecQueryMarkupManager inst = new JparsecQueryMarkupManager();
 
 	private final QlPageConstraints defaultLimit = new QlPageConstraints(10, -1);
 	private final QlPageConstraints defaultOffset = new QlPageConstraints(-1, 100);
 	private final QlPageConstraints defaultPage = new QlPageConstraints(10, 100);
 
 	/*
 	 * ===================================================
 	 * 		WORD
 	 * ===================================================
 	 */
 	@Test(expected = ParserException.class)
 	public final void test_word() {
 		String word = "";
 		inst.alphaNumeric().parse(word);
 		Assert.fail("Words cannot be blank");
 	}
 
 	@Test
 	public final void test_word1() {
 		String word = "messages";
 		assertEquals(word, inst.alphaNumeric().parse(word));
 	}
 
 	@Test
 	public final void test_word2() {
 		String word = "users";
 		assertEquals(word, inst.alphaNumeric().parse(word));
 	}
 
 	@Test
 	public final void test_word3() {
 		String word = "uSeRsAvAtAr";
 		assertEquals(word, inst.alphaNumeric().parse(word));
 	}
 
 	/*
 	 * ===================================================
 	 * 		FIELD
 	 * ===================================================
 	 */
 	@Test(expected = ParserException.class)
 	public final void test_field() {
 			String word = "";
 			inst.fieldOrFunctionParser().parse(word);
 			Assert.fail("Field cannot be blank");
 	}
 
 	@Test
 	public final void test_field1() {
 		String word = "messages";
 		runFieldTest(word, null, word, word, false);
 	}
 	
 	@Test
 	public final void test_field1_distinct() {
 		String name = "messages";
 		runFieldTest("distinct messages", "distinct", name, name, false);
 	}
 
 	@Test
 	public final void test_field2() {
 		String word = "users";
 		runFieldTest(word, null, word, word, false);
 	}
 	
 	@Test
 	public final void test_field2_distinct() {
 		String name = "users";
 		runFieldTest("distinct users", "distinct", name, name, false);
 	}
 
 	@Test
 	public final void test_field3() {
 		String word = "board.id";
 		runFieldTest(word, null, "board", word, false);
 	}
 	
 	@Test
 	public final void test_field3_distinct() {
 		runFieldTest("DisTinCT board.id", "DisTinCT", "board", "board.id", false);
 	}
 
 	@Test
 	public final void test_field4() {
 		String word = "message.parent.author.uid";
 		runFieldTest(word, null, "message", word, false);
 	}
 
 	@Test
 	public final void test_field4_distinct() {
 		runFieldTest("DISTINCT message.parent.author.uid", "DISTINCT", "message", 
 				"message.parent.author.uid", false);
 	}
 	
 	@Test
 	public final void test_field5() {
 		String word = "count(*)";
 		runFieldTest(word, null, "count", word, false);
 	}
 	
 	@Test
 	public final void test_field5a() {
 		runFieldTest("count( * )", null, "count", "count(*)", false);
 	}
 	
 	@Test(expected = ParserException.class)
 	public final void test_field5b() {
 		String word = "count (*)";
 		runFieldTest(word, null, "count", word, false);
 		Assert.fail("Parser should not support white space after functions");
 	}
 	
 	@Test
 	public final void test_field6() {
 		String word = "count(kudos)";
 		runFieldTest(word, null, "count", word, false);
 	}
 	
 	@Test
 	public final void test_field7() {
 		String word = "kudos.sum(weight)";
 		runFieldTest(word, null, "kudos", word, false);
 	}
 	
 	@Test
 	public final void test_field8() {
 		String word = "sum(kudos.sum(weight))";
 		runFieldTest(word, null, "sum", word, false);
 	}
 	
 	@Test
 	public final void test_field9() {
 		String word = "crazyFunction(crazyCollection.superFunction(unexpectedInnerField))";
 		runFieldTest(word, null, "crazyFunction", word, false);
 	}
 
 	private final void runFieldTest(String word, String qualifier, String name, String expectedQualifiedName, 
 			boolean isStar) {
 		QlField qlField = inst.qualifiedFieldOrFunctionParser().parse(word);
 		assertNotNull(qlField);
 		assertEquals(expectedQualifiedName, qlField.getQualifiedName());
 		assertEquals(name, qlField.getName());
 		assertEquals(isStar, qlField.isStar());
 		assertEquals(qualifier, qlField.getQualifier());
 	}
 
 	@Test(expected = ParserException.class)
 	public final void test_fieldStar() {
 			String word = "";
 			inst.fieldStarParser().parse(word);
 			Assert.fail("FieldStar cannot be blank");
 	}
 
 	@Test
 	public final void test_fieldStar1() {
 		String word = "*";
 		List<QlField> qlFieldList = inst.fieldStarParser().parse(word);
 		assertEquals(1, qlFieldList.size());
 		QlField qlField = qlFieldList.get(0);
 		assertNotNull(qlField);
 		assertEquals(word, qlField.getName());
 		assertTrue(qlField.isStar());
 	}
 
 	/*
 	 * ===================================================
 	 * 		FIELDS
 	 * ===================================================
 	 */
 	@Test(expected = ParserException.class)
 	public final void test_fields() {
 			String word = "";
 			inst.fieldsParser().parse(word);
 			Assert.fail("Fields cannot be blank");
 	}
 
 	@Test
 	public final void test_fields1() {
 		runFieldsTest("message", 1, false);
 	}
 
 	@Test
 	public final void test_fields2() {
 		runFieldsTest("message, user", 2, false);
 	}
 
 	@Test
 	public final void test_fields3() {
 		runFieldsTest("message,user,id", 3, false);
 	}
 
 	@Test
 	public final void test_fields4() {
 		runFieldsTest("message,user, uid,       kudos", 4, false);
 	}
 
 	@Test
 	public final void test_fields5() {
 		runFieldsTest("*", 1, true);
 	}
 
 	private final void runFieldsTest(String fieldsString, int count, boolean isStar) {
 		List<QlField> fields = inst.fieldsParser().parse(fieldsString);
 		assertNotNull(fields);
 		assertEquals(count, fields.size());
 		assertEquals(isStar, fields.get(0).isStar());
 	}
 	
 	/*
 	 * ===================================================
 	 * 		ORDER BY TYPE
 	 * ===================================================
 	 */
 	@Test(expected = ParserException.class)
 	public final void test_sortOrderType() {
 			String word = "";
 			inst.sortOrderTypeParser().parse(word);
 			Assert.fail("SortOrderType cannot be blank");
 	}
 
 	@Test
 	public final void test_sortOrderType1() {
 		assertEquals(ASC, inst.sortOrderTypeParser().parse("ASC"));
 	}
 
 	@Test
 	public final void test_sortOrderType2() {
 		assertEquals(ASC, inst.sortOrderTypeParser().parse("asc"));
 	}
 
 	@Test
 	public final void test_sortOrderType3() {
 		assertEquals(DESC, inst.sortOrderTypeParser().parse("DESC"));
 	}
 
 	@Test
 	public final void test_sortOrderType4() {
 		assertEquals(DESC, inst.sortOrderTypeParser().parse("desc"));
 	}
 
 	/*
 	 * ===================================================
 	 * 		ORDER BY
 	 * ===================================================
 	 */
 	@Test(expected = ParserException.class)
 	public final void test_sortOrder() {
 			String word = "";
 			inst.sortOrderTypeParser().parse(word);
 			Assert.fail("SortOrder cannot be blank");
 	}
 
 	@Test
 	public final void test_sortOrder1() {
 		runSortOrderTest("ORDER BY date ASC", getSortClause("date", ASC));
 	}
 
 	@Test
 	public final void test_sortOrder2() {
 		runSortOrderTest("ORDER BY email DESC", getSortClause("email", DESC));
 	}
 
 	@Test
 	public final void test_sortOrder3() {
 		runSortOrderTest("order by email desc", getSortClause("email", DESC));
 	}
 	
 	private QlSortClause getSortClause(String field, QlSortOrderType order) {
 		return new QlSortClause(QlField.create(field), order);
 	}
 	
 	private final void runSortOrderTest(String query, QlSortClause expected) {
 		QlSortClause actual = inst.orderByParser().parse(query);
 		assertNotNull(actual);
 		assertEquals(expected, actual);
 	}
 
 	/*
 	 * ===================================================
 	 * 		PAGE CONSTRAINTS
 	 * ===================================================
 	 */
 	@Test
 	public final void test_pageConstraints() {
 		try {
 			String word = "";
 			QlPageConstraints pageConstraint = inst.pageConstraintParser().parse(word);
 			assertEquals(-1, pageConstraint.getLimit());
 			assertEquals(-1, pageConstraint.getOffset());
 		} catch (ParserException e) {
 			Assert.fail("PageConstraints can be blank");
 		}
 	}
 
 	@Test
 	public final void test_pageConstraints1() {
 		assertEquals(defaultLimit,
 				inst.pageConstraintParser().parse("LIMIT 10"));
 	}
 
 	@Test
 	public final void test_pageConstraints2() {
 		assertEquals(defaultOffset,
 				inst.pageConstraintParser().parse("OFFSET 100"));
 	}
 
 	@Test
 	public final void test_pageConstraints3() {
 		assertEquals(defaultPage,
 				inst.pageConstraintParser().parse("LIMIT 10 OFFSET 100"));
 	}
 
 	@Test
 	public final void test_pageConstraints4() {
 		assertEquals(defaultPage,
 				inst.pageConstraintParser().parse("limit 10 offset 100"));
 	}
 
 	/*
 	 * ===================================================
 	 * 		CONSTRAINT OPERATORS
 	 * ===================================================
 	 */
 	@Test(expected = ParserException.class)
 	public final void test_constraintOperator() {
 			String word = "";
 			inst.constraintOperatorParser().parse(word);
 			Assert.fail("ConstraintOperator cannot be blank");
 	}
 
 	@Test
 	public final void test_constraintOperator1() {
 		assertEquals(EQUALS, inst.constraintOperatorParser().parse("="));
 	}
 
 	@Test
 	public final void test_constraintOperator2() {
 		assertEquals(NOT_EQUALS, inst.constraintOperatorParser().parse("!="));
 	}
 
 	@Test
 	public final void test_constraintOperator3() {
 		assertEquals(GREATER_THAN, inst.constraintOperatorParser().parse(">"));
 	}
 
 	@Test
 	public final void test_constraintOperator4() {
 		assertEquals(GREATER_THAN_EQUAL, inst.constraintOperatorParser().parse(">="));
 	}
 
 	@Test
 	public final void test_constraintOperator5() {
 		assertEquals(LESS_THAN, inst.constraintOperatorParser().parse("<"));
 	}
 
 	@Test
 	public final void test_constraintOperator6() {
 		assertEquals(LESS_THAN_EQUAL, inst.constraintOperatorParser().parse("<="));
 	}
 
 	@Test
 	public final void test_constraintOperator7() {
 		assertEquals(IN, inst.constraintOperatorParser().parse("IN"));
 	}
 
 	@Test
 	public final void test_constraintOperator8() {
 		assertEquals(IN, inst.constraintOperatorParser().parse("in"));
 	}
 	
 	@Test
 	public final void test_constraintOperator9() {
 		assertEquals(MATCHES, inst.constraintOperatorParser().parse("MATCHES"));
 	}
 	
 	@Test
 	public final void test_constraintOperator10() {
 		assertEquals(MATCHES, inst.constraintOperatorParser().parse("matches"));
 	}
 	
 	@Test
 	public final void test_constraintOperator11() {
 		assertEquals(LIKE, inst.constraintOperatorParser().parse("LIKE"));
 	}
 	
 	@Test
 	public final void test_constraintOperator12() {
 		assertEquals(LIKE, inst.constraintOperatorParser().parse("like"));
 	}
 
 	/*
 	 * ===================================================
 	 * 		CONSTRAINT PAIR OPERATORS
 	 * ===================================================
 	 */
 	@Test(expected = ParserException.class)
 	public final void test_constraintPairOperator() {
 			String word = "";
 			inst.constraintPairOperatorParser().parse(word);
 			Assert.fail("ConstraintPairOperator cannot be blank");
 	}
 
 	@Test
 	public final void test_constraintPairOperator1() {
 		assertEquals(AND, inst.constraintPairOperatorParser().parse("AND "));
 	}
 
 	@Test
 	public final void test_constraintPairOperator1a() {
 		assertEquals(AND, inst.constraintPairOperatorParser().parse("and "));
 	}
 
 	@Test
 	public final void test_constraintPairOperator2() {
 		assertEquals(OR, inst.constraintPairOperatorParser().parse("OR "));
 	}
 
 	@Test
 	public final void test_constraintPairOperator2a() {
 		assertEquals(OR, inst.constraintPairOperatorParser().parse("or "));
 	}
 
 	/*
 	 * ===================================================
 	 * 		VARIABLE
 	 * ===================================================
 	 */
 	@Test(expected = ParserException.class)
 	public final void test_variable() {
 			String word = "";
 			inst.constraintValueParser().parse(word);
 			Assert.fail("Variable cannot be blank");
 	}
 
 	@Test
 	public final void test_variable1() {
 		String var = "25";
 		assertEquals(25, inst.constraintValueParser().parse(var).asA(QlConstraintValueNumber.class).getValue());
 	}
 
 	@Test
 	public final void test_variable2() {
 		String var = "3.1415";
 		assertEquals(3.1415, inst.constraintValueParser().parse(var).asA(QlConstraintValueNumber.class).getValue());
 	}
 
 	@Test
 	public final void test_variable3() {
 		String var = "'David''s'";
 		String varExpected = "David's";
 		assertEquals(varExpected, inst.constraintValueParser().parse(var).asA(QlConstraintValueString.class).getValue());
 	}
 
 	@Test
 	public final void test_variable4() {
 		String var = "'Hello, World!'";
 		String varExpected = "Hello, World!";
 		assertEquals(varExpected, inst.constraintValueParser().parse(var).asA(QlConstraintValueString.class).getValue());
 	}
 
 	@Test
 	public final void test_variable5() {
 		String var = "'Hello''s, World''!'''";
 		String varExpected = "Hello's, World'!'";
 		assertEquals(varExpected, inst.constraintValueParser().parse(var).asA(QlConstraintValueString.class).getValue());
 	}
 
 	@Test
 	public final void test_variable6() {
 		String var = "()";
 		QlConstraintValueCollection<QlConstraintValue> varExpected = new QlConstraintValueCollection<QlConstraintValue>();
 		assertEquals(varExpected, inst.constraintValueParser().parse(var));
 	}
 
 	@Test
 	public final void test_variable7() {
 		String var = "('a','b','c')";
 		QlConstraintValueCollection<QlConstraintValue> varExpected = getConstraintValueCollection("a","b","c");
 		assertEquals(varExpected, inst.constraintValueParser().parse(var));
 	}
 
 	@Test
 	public final void test_variable8() {
 		String var = "( 1 , 2.5 )";
 		QlConstraintValueCollection<QlConstraintValue> varExpected = getConstraintValueCollectionNumber(1, 2.5);
 		assertEquals(varExpected, inst.constraintValueParser().parse(var));
 	}
 
 	@Test
 	public final void test_variable9() {
 		String var = "('a')";
 		QlConstraintValueCollection<QlConstraintValue> varExpected = getConstraintValueCollection("a");
 		assertEquals(varExpected, inst.constraintValueParser().parse(var));
 	}
 
 	@Test
 	public final void test_variable10() {
 		String var = "\"David's\"";
 		String varExpected = "David's";
 		assertEquals(varExpected, inst.constraintValueParser().parse(var).asA(QlConstraintValueString.class).getValue());
 	}
 
 	@Test
 	public final void test_variable11() {
 		String var = "\"David\\\"s\"";
 		String varExpected = "David\"s";
 		assertEquals(varExpected, inst.constraintValueParser().parse(var).asA(QlConstraintValueString.class).getValue());
 	}
 
 	@Test(expected = ParserException.class)
 	public final void test_variable12() {
 		String var = "\"Da\\vid's\"";
 			inst.constraintValueParser().parse(var);
 	}
 
 	@Test(expected = ParserException.class)
 	public final void test_variable13() {
 		String var = "'David\"s''";
 			inst.constraintValueParser().parse(var);
 	}
 
 	@Test
 	public final void test_variable14() {
 		String var = "'\"Hello, World!\"'";
 		String varExpected = "\"Hello, World!\"";
 		assertEquals(varExpected, inst.constraintValueParser().parse(var).asA(QlConstraintValueString.class).getValue());
 	}
 
 	@Test
 	public final void test_variable15() {
 		String var = "\"'Hello, World!'\"";
 		String varExpected = "'Hello, World!'";
 		assertEquals(varExpected, inst.constraintValueParser().parse(var).asA(QlConstraintValueString.class).getValue());
 	}
 
 	@Test(expected = ParserException.class)
 	public final void test_variable16() {
 		String var = "'\"Hello, World!'\"";
 			inst.constraintValueParser().parse(var);
 	}
 
 	@Test(expected = ParserException.class)
 	public final void test_variable17() {
 		String var = "\"'Hello, World!\"'";
 			inst.constraintValueParser().parse(var);
 	}
 
 	@Test
 	public final void test_variable18() {
 		Date now = new Date();
 		String dateString = ISO8601Utils.format(now);
 		QlConstraintValue var = inst.constraintValueParser().parse(dateString);
 		assertTrue(var.isA(QlConstraintValueDate.class));
 		QlConstraintValueDate dateVar = var.asA(QlConstraintValueDate.class);
 		assertNotNull(dateVar);
 		assertEquals(dateString, dateVar.getValueString());
 	}
 
 	@Test
 	public final void test_variable19() {
 		String var = "0";
 		assertEquals(0, inst.constraintValueParser().parse(var).asA(QlConstraintValueNumber.class).getValue());
 	}
 
 	@Test
 	public final void test_variable20() {
 		String var = "0L";
 		assertEquals(0L, inst.constraintValueParser().parse(var).asA(QlConstraintValueNumber.class).getValue());
 	}
 
 	@Test
 	public final void test_variable21() {
 		String var = "0f";
 		assertEquals(0f, inst.constraintValueParser().parse(var).asA(QlConstraintValueNumber.class).getValue());
 	}
 
 	@Test(expected = ParserException.class)
 	public final void test_variable22() {
 		String var = "00";
 		inst.constraintValueParser().parse(var);
 	}
 
 	@Test(expected = ParserException.class)
 	public final void test_variable23() {
 		String var = "000001";
 		inst.constraintValueParser().parse(var);
 	}
 
 	@Test
 	public final void test_variable24() {
 		String var = "0.0f";
 		assertEquals(0f, inst.constraintValueParser().parse(var).asA(QlConstraintValueNumber.class).getValue());
 	}
 
 	@Test
 	public final void test_variable25() {
 		String var = "0.0";
 		assertEquals(0D, inst.constraintValueParser().parse(var).asA(QlConstraintValueNumber.class).getValue());
 	}
 
 	private final QlConstraintValueCollection<QlConstraintValue> getConstraintValueCollection(String...values) {
 		List<QlConstraintValue> vals = Lists.newArrayList();
 		for (String value : values) {
 			vals.add(new QlConstraintValueString(value));
 		}
 		return new QlConstraintValueCollection<QlConstraintValue>(vals);
 	}
 	
 	private final QlConstraintValueCollection<QlConstraintValue> getConstraintValueCollectionNumber(Number...values) {
 		List<QlConstraintValue> vals = Lists.newArrayList();
 		for (Number value : values) {
 			vals.add(new QlConstraintValueNumber(value));
 		}
 		return new QlConstraintValueCollection<QlConstraintValue>(vals);
 	}
 	
 	/*
 	 * ===================================================
 	 * 		CONSTRAINT
 	 * ===================================================
 	 */
 	@Test(expected = ParserException.class)
 	public final void test_constraint() {
 			String word = "";
 			inst.constraintParser().parse(word);
 			Assert.fail("Constraint cannot be blank");
 	}
 
 	@Test
 	public final void test_constraint1() {
 		runConstraintTest("name = 'david'", getStringConstraint(QlField.create("name"), "david", EQUALS));
 	}
 
 	@Test
 	public final void test_constraint2() {
 		runConstraintTest("email!='david.esposito@lithium.com'", 
 				getStringConstraint(QlField.create("email"), "david.esposito@lithium.com", NOT_EQUALS));
 	}
 
 	@Test
 	public final void test_constraint3() {
 		runConstraintTest("age>25", getNumberConstraint(QlField.create("age"), 25, GREATER_THAN));
 	}
 
 	@Test
 	public final void test_constraint4() {
 		runConstraintTest("average<=2.7182818f", 
 				getNumberConstraint(QlField.create("average"), new Float(2.7182818), LESS_THAN_EQUAL));
 	}
 
 	@Test
 	public final void test_constraint5() {
 		runConstraintTest("board.id IN ('a','b','c')",
 				getStringCollectionConstraint(QlField.create(null, "board", QlField.create("id"), false), IN, "a", "b", "c"));
 	}
 
 	@Test
 	public final void test_constraint6() {
 		runConstraintTest("user.uid IN (1,2,3)",
 				getNumberCollectionConstraint(QlField.create(null, "user", QlField.create("uid"), false), IN, 1, 2, 3));
 	}
 
 	@Test
 	public final void test_constraint7() {
 		runConstraintTest("subject IN ()",
 				getCollectionConstraint(QlField.create("subject"), IN));
 	}
 
 	@Test
 	public final void test_constraint8() {
 		runConstraintTest("age>25000000000000L", getNumberConstraint(QlField.create("age"), Long.parseLong("25000000000000"), GREATER_THAN));
 	}
 
 	@Test
 	public final void test_constraint9() {
 		runConstraintTest("body MATCHES 'asdf'", getStringConstraint(QlField.create("body"), "asdf", MATCHES));
 	}
 	
 	@Test
 	public final void test_constraint10() {
 		runConstraintTest("body LIKE 'fdsa'", getStringConstraint(QlField.create("body"), "fdsa", LIKE));
 	}
 
 	@Test
 	public final void test_constraint11() {
 		runConstraintTest("body LIKE ('asdf', 'fdsa')", getStringCollectionConstraint(QlField.create("body"), LIKE, "asdf", "fdsa"));
 	}
 	
 	//Use this one for recursive ql fields.
 	private final QlConstraint getStringConstraint(QlField field, String value, QlConstraintOperator op) {
 		return new QlConstraint(field, new QlConstraintValueString(value), op);
 	}
 	
 	//Use this one for recursive ql fields.
 	private final QlConstraint getNumberConstraint(QlField field, Number value, QlConstraintOperator op) {
 		return new QlConstraint(field, new QlConstraintValueNumber(value), op);
 	}
 	
 	private final QlConstraint getStringConstraint(String key, String value, QlConstraintOperator op) {
 		return new QlConstraint(QlField.create(key), new QlConstraintValueString(value), op);
 	}
 	
 	private final QlConstraint getNumberConstraint(String key, Number value, QlConstraintOperator op) {
 		return new QlConstraint(QlField.create(key), new QlConstraintValueNumber(value), op);
 	}
 	
 	private final QlConstraint getDateConstraint(String key, Date value, QlConstraintOperator op) {
 		return new QlConstraint(QlField.create(key), new QlConstraintValueDate(value), op);
 	}
 	
 	private final QlConstraint getCollectionConstraint(QlField field, QlConstraintOperator op) {
 		return new QlConstraint(field, new QlConstraintValueCollection<QlConstraintValue>(), op);
 	}
 	
 	private final QlConstraint getStringCollectionConstraint(QlField field, QlConstraintOperator op, String...values) {
 		return new QlConstraint(field, getConstraintValueCollection(values), op);
 	}
 	
 	private final QlConstraint getNumberCollectionConstraint(QlField field, QlConstraintOperator op, Number...values) {
 		return new QlConstraint(field, getConstraintValueCollectionNumber(values), op);
 	}
 	
 	private final void runConstraintTest(String query, QlConstraint expected) {
 		QlBooleanConstraintNode actual = inst.constraintParser().parse(query);
 		assertEquals(expected, actual);
 	}
 	
 	/*
 	 * ===================================================
 	 * 		CONSTRAINTS
 	 * ===================================================
 	 */
 	@Test(expected = ParserException.class)
 	public final void test_constraints() {
 			String word = "";
 			inst.constraintsParser().parse(word);
 			Assert.fail("Constraints cannot be blank");
 	}
 	
 	@Test(expected = InvalidQueryException.class)
 	public final void test_constraints_a() throws InvalidQueryException, QueryValidationException {
 			String word = "";
 			inst.parseQlConstraintsClause(word);
 			Assert.fail("Constraints cannot be blank");
 	}
 	
 	@Test(expected = InvalidQueryException.class)
 	public final void test_constraints_b() throws InvalidQueryException, QueryValidationException {
 			String word = "WHERE id = 5";
 			inst.parseQlConstraintsClause(word);
 			Assert.fail("Constraints clause cannot begin with the \"WHERE\" keyword.");
 	}
 	
 	@Test(expected = InvalidQueryException.class)
 	public final void test_constraints_c() throws InvalidQueryException, QueryValidationException {
 			String word = "distinct id = 5 AND name = 'david'";
 			inst.parseQlConstraintsClause(word);
 			Assert.fail("Qualifiers cannot exist in the WHERE clause");
 	}
 	
 	@Test(expected = InvalidQueryException.class)
 	public final void test_constraints_d() throws InvalidQueryException, QueryValidationException {
 		String word = "id = 5 AND distinct name = 'david'";
 			inst.parseQlConstraintsClause(word);
 			Assert.fail("Qualifiers cannot exist in the WHERE clause");
 	}
 
 	@Test
 	public final void test_constraints1() {
 		QlBooleanConstraintNode expected = getStringConstraint("email", "david.esposito@lithium.com", NOT_EQUALS);
 		runConstraintsTest("email!='david.esposito@lithium.com'", expected);
 	}
 
 	@Test
 	public final void test_constraints2() {
 		QlBooleanConstraintNode expected = getNumberConstraint("age", 25, GREATER_THAN);
 		runConstraintsTest("age>25", expected);
 	}
 
 	@Test
 	public final void test_constraints2a() {
 		Date date = new Date();
 		QlBooleanConstraintNode expected = getDateConstraint("date", date, GREATER_THAN);
 		runConstraintsTest("date>" + ISO8601Utils.format(date), expected);
 	}
 
 	@Test
 	public final void test_constraints2b() {
 		Date date = new Date();
 		QlBooleanConstraintNode expected = getDateConstraint("date", date, LESS_THAN);
 		runConstraintsTest("date<" + ISO8601Utils.format(date), expected);
 	}
 
 	@Test
 	public final void test_constraints3() {
 		QlBooleanConstraintNode expected = new QlConstraintPair(
 				getNumberConstraint("a", 5, EQUALS), 
 				getStringConstraint("b", "c", EQUALS), 
 				AND);
 		runConstraintsTest("a=5 AND b='c'", expected);
 	}
 
 	@Test
 	public final void test_constraints4() {
 		QlBooleanConstraintNode temp = new QlConstraintPair(
 				getNumberConstraint("a", 5, EQUALS), 
 				getStringConstraint("b", "c", EQUALS),
 				AND);
 		QlBooleanConstraintNode expected = new QlConstraintPair(
 				temp, 
 				getNumberConstraint("d", 18, EQUALS), 
 				AND);
 		runConstraintsTest("a=5 AND b='c' AND d=18", expected);
 	}
 
 	@Test
 	public final void test_constraints5() {
 		QlBooleanConstraintNode temp = new QlConstraintPair(
 				getNumberConstraint("a", 5, EQUALS), 
 				getStringConstraint("b", "cdef", EQUALS),
 				AND);
 		QlBooleanConstraintNode expected = new QlConstraintPair(
 				temp, 
 				getNumberConstraint("g", 18, EQUALS), 
 				OR);
 		runConstraintsTest("a=5 AND b='cdef' OR g=18", expected);
 	}
 
 	@Test
 	public final void test_constraints6() {
 		QlBooleanConstraintNode temp1 = new QlConstraintPair(
 				getNumberConstraint("a", 5, EQUALS), 
 				getStringConstraint("b", "c", EQUALS),
 				AND);
 		QlBooleanConstraintNode temp2 = new QlConstraintPair(
 				temp1, 
 				getNumberConstraint("d", 18, EQUALS),
 				OR);
 		QlBooleanConstraintNode temp3 = new QlConstraintPair(
 				temp2, 
 				getNumberConstraint("w", 5, EQUALS),
 				AND);
 		QlBooleanConstraintNode temp4 = new QlConstraintPair(
 				temp3, 
 				getStringConstraint("x", "y", EQUALS),
 				AND);
 		QlBooleanConstraintNode expected = new QlConstraintPair(
 				temp4, 
 				getNumberConstraint("z", 18, EQUALS), 
 				OR);
 		runConstraintsTest("a=5 AND b='c' OR d=18 AND w=5 AND x='y' OR z=18", expected);
 	}
 	
 	private final void runConstraintsTest(String query, QlBooleanConstraintNode expected) {
 		// Test the actual parser
 		QlBooleanConstraintNode actual = inst.constraintsParser().parse(query);
 		assertEquals(expected, actual);
 		try {
 			// Test the public end point in the StarQL API
 			actual = inst.parseQlConstraintsClause(query).getRoot();
 			assertEquals(expected, actual);
 		} catch (InvalidQueryException e) {
 			e.printStackTrace();
 			Assert.fail();
 		} catch (QueryValidationException e) {
 			e.printStackTrace();
 			Assert.fail();
 		}
 	}
 
 	/*
 	 * ===================================================
 	 * 		*QL SELECT STATEMENTS
 	 * ===================================================
 	 */
 	@Test(expected = ParserException.class)
 	public final void test_qlSelectStatement() {
 			String word = "";
 			inst.qlSelectParser().parse(word);
 			Assert.fail("*QL SelectStatement cannot be blank");
 	}
 
 	@Test
 	public final void test_qlSelectStatement_simple() {
 		String query = "SELECT * FROM users";
 		List<QlField> fields = getFields();
 		String table = "users";
 		QlBooleanConstraintNode constraints = null;
 		QlSortClause sortConstraint = null;
 		QlPageConstraints pageConstraints = QlPageConstraints.ALL;
 		QlSelectStatement expected = new QlSelectStatement.Builder()
 				.setFields(fields)
 				.setCollection(table)
 				.setConstraints(constraints)
 				.setSortConstraint(sortConstraint)
 				.setPageConstraints(pageConstraints)
 				.build();
 		verify(query, expected);
 	}
 
 	@Test
 	public final void test_qlSelectStatement_simple1() {
 		String query = "select * from users";
 		List<QlField> fields = getFields();
 		String table = "users";
 		QlBooleanConstraintNode constraints = null;
 		QlSortClause sortConstraint = null;
 		QlPageConstraints pageConstraints = QlPageConstraints.ALL;
 		QlSelectStatement expected = new QlSelectStatement.Builder()
 				.setFields(fields)
 				.setCollection(table)
 				.setConstraints(constraints)
 				.setSortConstraint(sortConstraint)
 				.setPageConstraints(pageConstraints)
 				.build();
 		verify(query, expected);
 	}
 	
 	private List<QlField> getFields(String...fieldNames) {
 		if (fieldNames.length == 0) {
 			return Lists.newArrayList(QlField.create("*"));
 		}
 		List<QlField> fields = Lists.newArrayList();
 		for (String name : fieldNames) {
 			fields.add(QlField.create(name));
 		}
 		return fields;
 	}
 	
 	private List<QlField> getFields(String[] fieldNames, String[] fieldQualifiers) {
 		if (fieldNames.length == 0) {
 			return Lists.newArrayList(QlField.create("*"));
 		}
 		List<QlField> fields = Lists.newArrayList();
 		for (int i=0;i<fieldNames.length;i++) {
 			String name = fieldNames[i];
 			String qualifier = fieldQualifiers[i];
 			fields.add(QlField.create(qualifier, name));
 		}
 		return fields;
 	}
 
 	@Test
 	public final void test_qlSelectStatement_fields() {
 		String query = "SELECT uid,login, email,firstName FROM users";
 		List<QlField> fields = getFields("uid", "login", "email", "firstName");
 		String table = "users";
 		QlBooleanConstraintNode constraints = null;
 		QlSortClause sortConstraint = null;
 		QlPageConstraints pageConstraints = QlPageConstraints.ALL;
 		QlSelectStatement expected = new QlSelectStatement.Builder()
 				.setFields(fields)
 				.setCollection(table)
 				.setConstraints(constraints)
 				.setSortConstraint(sortConstraint)
 				.setPageConstraints(pageConstraints)
 				.build();
 		verify(query, expected);
 	}
 	
 	/*
 	 * ===================================================
 	 * 		SORTED *QL SELECT STATEMENTS
 	 * ===================================================
 	 */
 	@Test(expected = InvalidQueryException.class)
 	public final void test_qlSelectStatementOrderByDistinctFailure() throws InvalidQueryException, 
 			QueryValidationException {
 		String query = "SELECT * FROM users ORDER BY distinct date ASC";
 		inst.parseQlSelect(query);
 		Assert.fail("The distinc keyword cannot be used in the ORDER BY clause.");
 	}
 	
 	@Test
 	public final void test_qlSelectStatement_simpleOrderBy() {
 		String query = "SELECT * FROM users ORDER BY date ASC";
 		List<QlField> fields = getFields();
 		String table = "users";
 		QlBooleanConstraintNode constraints = null;
 		QlSortClause sortConstraint = getSortClause("date", ASC);
 		QlPageConstraints pageConstraints = QlPageConstraints.ALL;
 		QlSelectStatement expected = new QlSelectStatement.Builder()
 				.setFields(fields)
 				.setCollection(table)
 				.setConstraints(constraints)
 				.setSortConstraint(sortConstraint)
 				.setPageConstraints(pageConstraints)
 				.build();
 		verify(query, expected);
 	}
 
 	@Test
 	public final void test_qlSelectStatement_fieldsOrderBy() {
 		String query = "SELECT uid,login, email,firstName FROM users ORDER BY email DESC";
 		List<QlField> fields = getFields("uid", "login", "email", "firstName");
 		String table = "users";
 		QlBooleanConstraintNode constraints = null;
 		QlSortClause sortConstraint = getSortClause("email", DESC);
 		QlPageConstraints pageConstraints = QlPageConstraints.ALL;
 		QlSelectStatement expected = new QlSelectStatement.Builder()
 				.setFields(fields)
 				.setCollection(table)
 				.setConstraints(constraints)
 				.setSortConstraint(sortConstraint)
 				.setPageConstraints(pageConstraints)
 				.build();
 		verify(query, expected);
 	}
 
 	@Test
 	public final void test_qlSelectStatement_fieldsOrderBy1() {
 		String query = "SELECT DISTINCT uid,login, email,firstName FROM users ORDER BY email DESC";
 		List<QlField> fields = getFields(new String[]{"uid", "login", "email", "firstName"},
 				new String[]{"DISTINCT", null, null, null});
 		String table = "users";
 		QlBooleanConstraintNode constraints = null;
 		QlSortClause sortConstraint = getSortClause("email", DESC);
 		QlPageConstraints pageConstraints = QlPageConstraints.ALL;
 		QlSelectStatement expected = new QlSelectStatement.Builder()
 				.setFields(fields)
 				.setCollection(table)
 				.setConstraints(constraints)
 				.setSortConstraint(sortConstraint)
 				.setPageConstraints(pageConstraints)
 				.build();
 		verify(query, expected);
 	}
 
 	@Test
 	public final void test_qlSelectStatement_fieldsOrderBy2() {
 		String query = "SELECT uid,login, email,DISTINCT firstName FROM users ORDER BY email DESC";
 		List<QlField> fields = getFields(new String[]{"uid", "login", "email", "firstName"},
 				new String[]{null, null, null, "DISTINCT"});
 		String table = "users";
 		QlBooleanConstraintNode constraints = null;
 		QlSortClause sortConstraint = getSortClause("email", DESC);
 		QlPageConstraints pageConstraints = QlPageConstraints.ALL;
 		QlSelectStatement expected = new QlSelectStatement.Builder()
 				.setFields(fields)
 				.setCollection(table)
 				.setConstraints(constraints)
 				.setSortConstraint(sortConstraint)
 				.setPageConstraints(pageConstraints)
 				.build();
 		verify(query, expected);
 	}
 
 	/*
 	 * ===================================================
 	 * 		*QL SELECT STATEMENTS WITH LIMIT/OFFSET
 	 * ===================================================
 	 */
 	@Test
 	public final void test_qlSelectStatement_simpleLimit() {
 		String query = "SELECT * FROM users LIMIT 10";
 		List<QlField> fields = getFields();
 		String table = "users";
 		QlBooleanConstraintNode constraints = null;
 		QlSortClause sortConstraint = null;
 		QlPageConstraints pageConstraints = defaultLimit;
 		QlSelectStatement expected = new QlSelectStatement.Builder()
 				.setFields(fields)
 				.setCollection(table)
 				.setConstraints(constraints)
 				.setSortConstraint(sortConstraint)
 				.setPageConstraints(pageConstraints)
 				.build();
 		verify(query, expected);
 	}
 
 	@Test
 	public final void test_qlSelectStatement_fieldsLimitOffset() {
 		String query = "SELECT uid,login, email,firstName FROM users LIMIT 30 OFFSET 10";
 		List<QlField> fields = getFields("uid", "login", "email", "firstName");
 		String table = "users";
 		QlBooleanConstraintNode constraints = null;
 		QlSortClause sortConstraint = null;
 		QlPageConstraints pageConstraints = new QlPageConstraints(30, 10);
 		QlSelectStatement expected = new QlSelectStatement.Builder()
 				.setFields(fields)
 				.setCollection(table)
 				.setConstraints(constraints)
 				.setSortConstraint(sortConstraint)
 				.setPageConstraints(pageConstraints)
 				.build();
 		verify(query, expected);
 	}
 
 	@Test
 	public final void test_qlSelectStatement_simpleSortLimit() {
 		String query = "SELECT * FROM users ORDER BY date ASC LIMIT 100";
 		List<QlField> fields = getFields();
 		String table = "users";
 		QlBooleanConstraintNode constraints = null;
 		QlSortClause sortConstraint = getSortClause("date", ASC);
 		QlPageConstraints pageConstraints = new QlPageConstraints(100, -1);
 		QlSelectStatement expected = new QlSelectStatement.Builder()
 				.setFields(fields)
 				.setCollection(table)
 				.setConstraints(constraints)
 				.setSortConstraint(sortConstraint)
 				.setPageConstraints(pageConstraints)
 				.build();
 		verify(query, expected);
 	}
 
 	@Test
 	public final void test_qlSelectStatement_fieldsSortLimitOffset() {
 		String query = "SELECT uid,login, email,firstName FROM users ORDER BY email DESC LIMIT 100 OFFSET 1000";
 		List<QlField> fields = getFields("uid", "login", "email", "firstName");
 		String table = "users";
 		QlBooleanConstraintNode constraints = null;
 		QlSortClause sortConstraint = getSortClause("email", DESC);
 		QlPageConstraints pageConstraints = new QlPageConstraints(100, 1000);
 		QlSelectStatement expected = new QlSelectStatement.Builder()
 				.setFields(fields)
 				.setCollection(table)
 				.setConstraints(constraints)
 				.setSortConstraint(sortConstraint)
 				.setPageConstraints(pageConstraints)
 				.build();
 		verify(query, expected);
 	}
 	
 	/*
 	 * ===================================================
 	 * 		*QL SELECT STATEMENTS WITH BOOLEAN CONSTRAINTS
 	 * ===================================================
 	 */
 	@Test
 	public final void test_qlSelectStatement_simpleSingleWhere() {
 		String query = "SELECT * FROM users WHERE email='david.esposito@lithium.com'";
 		List<QlField> fields = getFields();
 		String table = "users";
 		QlBooleanConstraintNode constraints = getStringConstraint("email", "david.esposito@lithium.com", EQUALS);
 		QlSortClause sortConstraint = null;
 		QlPageConstraints pageConstraints = QlPageConstraints.ALL;
 		QlSelectStatement expected = new QlSelectStatement.Builder()
 				.setFields(fields)
 				.setCollection(table)
 				.setConstraints(constraints)
 				.setSortConstraint(sortConstraint)
 				.setPageConstraints(pageConstraints)
 				.build();
 		verify(query, expected);
 	}
 
 	@Test
 	public final void test_qlSelectStatement_fieldsTwoWhere() {
 		String query = "SELECT uid,login, email,firstName FROM users WHERE name='david' AND age<50";
 		List<QlField> fields = getFields("uid", "login", "email", "firstName");
 		String table = "users";
 		QlBooleanConstraintNode constraints = new QlConstraintPair(
 				getStringConstraint("name", "david", EQUALS),
 				getNumberConstraint("age", 50, LESS_THAN),
 				AND);
 		QlSortClause sortConstraint = null;
 		QlPageConstraints pageConstraints = QlPageConstraints.ALL;
 		QlSelectStatement expected = new QlSelectStatement.Builder()
 				.setFields(fields)
 				.setCollection(table)
 				.setConstraints(constraints)
 				.setSortConstraint(sortConstraint)
 				.setPageConstraints(pageConstraints)
 				.build();
 		verify(query, expected);
 	}
 
 	@Test
 	public final void test_qlSelectStatement_simpleDateWhere() {
 		Date start = new Date(new Date().getTime() - (60 * 60 * 24));
 		Date end = new Date();
 		String startString = ISO8601Utils.format(start);
 		String endString = ISO8601Utils.format(end);
 		String query = String.format("SELECT * FROM users WHERE date >= %1$s AND date <= %2$s", 
 				startString, endString);
 		List<QlField> fields = getFields();
 		String table = "users";
 		QlBooleanConstraintNode constraints = new QlConstraintPair(
 				getDateConstraint("date", start, GREATER_THAN_EQUAL),
 				getDateConstraint("date", end, LESS_THAN_EQUAL),
 				AND);
 		QlSortClause sortConstraint = null;
 		QlPageConstraints pageConstraints = QlPageConstraints.ALL;
 		QlSelectStatement expected = new QlSelectStatement.Builder()
 				.setFields(fields)
 				.setCollection(table)
 				.setConstraints(constraints)
 				.setSortConstraint(sortConstraint)
 				.setPageConstraints(pageConstraints)
 				.build();
 		try {
 			QlSelectStatement parseQlSelect = inst.parseQlSelect(query);
 			assertEquals(expected, parseQlSelect);
 		} catch (InvalidQueryException e) {
 			Assert.fail(e.getMessage() + "\n" + e.getStackTrace());
 		} catch (QueryValidationException e) {
 			Assert.fail(e.getMessage() + "\n" + e.getStackTrace());
 		}
 	}
 
 	@Test
 	public final void test_qlSelectStatement_fieldsDateWhere() {
 		Date start = new Date(new Date().getTime() - (60 * 60 * 24));
 		Date end = new Date();
 		String startString = ISO8601Utils.format(start);
 		String endString = ISO8601Utils.format(end);
 		String query = String.format("SELECT uid,login, email,firstName FROM users WHERE registerDate >= %1$s AND registerDate <= %2$s", 
 				startString, endString);
 		List<QlField> fields = getFields("uid", "login", "email", "firstName");
 		String table = "users";
 		QlBooleanConstraintNode constraints = new QlConstraintPair(
 				getDateConstraint("registerDate", start, GREATER_THAN_EQUAL),
 				getDateConstraint("registerDate", end, LESS_THAN_EQUAL),
 				AND);
 		QlSortClause sortConstraint = null;
 		QlPageConstraints pageConstraints = QlPageConstraints.ALL;
 		QlSelectStatement expected = new QlSelectStatement.Builder()
 				.setFields(fields)
 				.setCollection(table)
 				.setConstraints(constraints)
 				.setSortConstraint(sortConstraint)
 				.setPageConstraints(pageConstraints)
 				.build();
 		try {
 			QlSelectStatement parseQlSelect = inst.parseQlSelect(query);
 			assertEquals(expected, parseQlSelect);
 		} catch (InvalidQueryException e) {
 			Assert.fail(e.getMessage() + "\n" + e.getStackTrace());
 		} catch (QueryValidationException e) {
 			Assert.fail(e.getMessage() + "\n" + e.getStackTrace());
 		}
 	}
 	
 	@Test
 	public final void test_qlSelectStatement_simpleSingleWhereSort() {
 		String query = "SELECT * FROM users WHERE email='david.esposito@lithium.com' ORDER BY date ASC";
 		List<QlField> fields = getFields();
 		String table = "users";
 		QlBooleanConstraintNode constraints = getStringConstraint("email", "david.esposito@lithium.com", EQUALS);
 		QlSortClause sortConstraint = getSortClause("date", ASC);
 		QlPageConstraints pageConstraints = QlPageConstraints.ALL;
 		QlSelectStatement expected = new QlSelectStatement.Builder()
 				.setFields(fields)
 				.setCollection(table)
 				.setConstraints(constraints)
 				.setSortConstraint(sortConstraint)
 				.setPageConstraints(pageConstraints)
 				.build();
 		verify(query, expected);
 	}
 	
 	@Test
 	public final void test_qlSelectStatement_fieldsTwoWhereSort() {
 		String query = "SELECT uid,login, email,firstName FROM users WHERE name='david' AND age<50 ORDER BY email DESC";
 		List<QlField> fields = getFields("uid", "login", "email", "firstName");
 		String table = "users";
 		QlBooleanConstraintNode constraints = new QlConstraintPair(
 				getStringConstraint("name", "david", EQUALS),
 				getNumberConstraint("age", 50, LESS_THAN),
 				AND);
 		QlSortClause sortConstraint = getSortClause("email", DESC);
 		QlPageConstraints pageConstraints = QlPageConstraints.ALL;
 		QlSelectStatement expected = new QlSelectStatement.Builder()
 				.setFields(fields)
 				.setCollection(table)
 				.setConstraints(constraints)
 				.setSortConstraint(sortConstraint)
 				.setPageConstraints(pageConstraints)
 				.build();
 		verify(query, expected);
 	}
 
 	@Test
 	public final void test_qlSelectStatement_fieldsTwoWhereLimit() {
 		String query = "SELECT uid,login, email,firstName FROM users WHERE name='david' AND age<50 LIMIT 50";
 		List<QlField> fields = getFields("uid", "login", "email", "firstName");
 		String table = "users";
 		QlBooleanConstraintNode constraints = new QlConstraintPair(
 				getStringConstraint("name", "david", EQUALS),
 				getNumberConstraint("age", 50, LESS_THAN),
 				AND);
 		QlSortClause sortConstraint = null;
 		QlPageConstraints pageConstraints = new QlPageConstraints(50, -1);
 		QlSelectStatement expected = new QlSelectStatement.Builder()
 				.setFields(fields)
 				.setCollection(table)
 				.setConstraints(constraints)
 				.setSortConstraint(sortConstraint)
 				.setPageConstraints(pageConstraints)
 				.build();
 		verify(query, expected);
 	}
 
 	@Test
 	public final void test_qlSelectStatement_simpleThreeWhereLimit() {
 		String query = "SELECT * FROM users WHERE name='david' AND age<50 OR login!='davidE' LIMIT 10";
 		List<QlField> fields = getFields();
 		String table = "users";
 		QlBooleanConstraintNode constraints = new QlConstraintPair(
 				new QlConstraintPair(
 						getStringConstraint("name", "david", EQUALS), 
 						getNumberConstraint("age", 50, LESS_THAN), 
 						AND), 
 				getStringConstraint("login", "davidE", NOT_EQUALS), 
 				OR);
 		QlSortClause sortConstraint = null;
 		QlPageConstraints pageConstraints = new QlPageConstraints(10, -1);
 		QlSelectStatement expected = new QlSelectStatement.Builder()
 				.setFields(fields)
 				.setCollection(table)
 				.setConstraints(constraints)
 				.setSortConstraint(sortConstraint)
 				.setPageConstraints(pageConstraints)
 				.build();
 		verify(query, expected);
 	}
 
 	@Test
 	public final void test_qlSelectStatement_fieldsThreeLimitOffset() {
 		String query = "SELECT uid,login, email,firstName FROM users WHERE name='david' AND age<50 OR login!='davidE' LIMIT 30 OFFSET 10";
 		List<QlField> fields = getFields("uid", "login", "email", "firstName");
 		String table = "users";
 		QlBooleanConstraintNode constraints = new QlConstraintPair(
 				new QlConstraintPair(
 						getStringConstraint("name", "david", EQUALS), 
 						getNumberConstraint("age", 50, LESS_THAN), 
 						AND), 
 				getStringConstraint("login", "davidE", NOT_EQUALS), 
 				OR);
 		QlSortClause sortConstraint = null;
 		QlPageConstraints pageConstraints = new QlPageConstraints(30, 10);
 		QlSelectStatement expected = new QlSelectStatement.Builder()
 				.setFields(fields)
 				.setCollection(table)
 				.setConstraints(constraints)
 				.setSortConstraint(sortConstraint)
 				.setPageConstraints(pageConstraints)
 				.build();
 		verify(query, expected);
 	}
 	
 	@Test
 	public final void test_qlSelectStatement_fieldsThreeSort() {
 		String query = "SELECT uid,login, email,firstName FROM users WHERE name='david' AND age<50 OR login!='davidE' ORDER BY age DESC";
 		List<QlField> fields = getFields("uid", "login", "email", "firstName");
 		String table = "users";
 		QlBooleanConstraintNode constraints = new QlConstraintPair(
 				new QlConstraintPair(
 						getStringConstraint("name", "david", EQUALS), 
 						getNumberConstraint("age", 50, LESS_THAN), 
 						AND), 
 				getStringConstraint("login", "davidE", NOT_EQUALS), 
 				OR);
 		QlSortClause sortConstraint = getSortClause("age", DESC);
 		QlPageConstraints pageConstraints = QlPageConstraints.ALL;
 		QlSelectStatement expected = new QlSelectStatement.Builder()
 				.setFields(fields)
 				.setCollection(table)
 				.setConstraints(constraints)
 				.setSortConstraint(sortConstraint)
 				.setPageConstraints(pageConstraints)
 				.build();
 		verify(query, expected);
 	}
 	
 	@Test
 	public final void test_qlSelectStatement_simpleFunctionInFields() {
 		String query = "SELECT count( * ) FROM messages";
 		QlField field = QlField.create(null, "count", QlField.create("*"), true);
 		String table = "messages";
 		QlPageConstraints pageConstraints = QlPageConstraints.ALL;
 		
 		QlSelectStatement expected = new QlSelectStatement.Builder()
 				.setFields(Lists.newArrayList(field)).setCollection(table).setPageConstraints(pageConstraints)
 				.build();
 		verify(query, expected);
 	}
 	
 	@Test
 	public final void test_qlSelectStatement_objectFunctionInFields() {
 		String query = "SELECT kudos.sum(weight) FROM messages";
 		QlField field = QlField.create(null, "kudos", QlField.create(null, "sum", QlField.create("weight"), true), false);
 		String table = "messages";
 		QlPageConstraints pageConstraints = QlPageConstraints.ALL;
 		
 		QlSelectStatement expected = new QlSelectStatement.Builder()
 				.setFields(Lists.newArrayList(field)).setCollection(table)
 				.setPageConstraints(pageConstraints).build();
 		verify(query, expected);
 	}
 	
 	@Test
 	public final void test_qlSelectStatement_functionInFunctionInFields() {
 		String query = "SELECT sum(kudos.count(*)) FROM messages";
 		QlField star = QlField.create("*");
 		QlField count = QlField.create(null, "count", star, true);
 		QlField kudos = QlField.create(null, "kudos", count, false);
 		QlField field = QlField.create(null, "sum", kudos, true);
 		
 		String table = "messages";
 		QlPageConstraints pageConstraints = QlPageConstraints.ALL;
 		
 		QlSelectStatement expected = new QlSelectStatement.Builder()
 				.setFields(Lists.newArrayList(field)).setCollection(table)
 				.setPageConstraints(pageConstraints).build();
 		verify(query, expected);
 	}
 	
 	@Test
 	public final void test_qlSelectStatement_FunctionInWhere() {
 		String query = "SELECT * FROM messages WHERE kudos.count(*) > 0";
 		QlField star = QlField.create("*");
		QlField count = QlField.create(null, "count", star, true);
		QlField kudos = QlField.create(null, "kudos", count, false);
 
 		QlBooleanConstraintNode constraint = new QlConstraint(kudos,
 				new QlConstraintValueNumber(0),
 				QlConstraintOperator.GREATER_THAN);
 		String table = "messages";
 		QlPageConstraints pageConstraints = QlPageConstraints.ALL;
 
 		QlSelectStatement expected = new QlSelectStatement.Builder()
 				.setFields(Lists.newArrayList(star)).setCollection(table)
 				.setPageConstraints(pageConstraints).setConstraints(constraint)
 				.build();
 		verify(query, expected);
 	}
 	
 	@Test
 	public final void test_qlSelectStatement_FunctionInWhereAndOrderBy() {
 		String query = "SELECT * FROM messages WHERE kudos.count(*) > 0 ORDER BY kudos.count(*) DESC";
 		QlField star = QlField.create("*");
		QlField count = QlField.create(null, "count", star, true);
		QlField kudos = QlField.create(null, "kudos", count, false);
 
 		QlBooleanConstraintNode constraint = new QlConstraint(kudos,
 				new QlConstraintValueNumber(0),
 				QlConstraintOperator.GREATER_THAN);
 		QlSortClause sortConstraint = new QlSortClause(kudos,
 				QlSortOrderType.DESC);
 		String table = "messages";
 		QlPageConstraints pageConstraints = QlPageConstraints.ALL;
 
 		QlSelectStatement expected = new QlSelectStatement.Builder()
 				.setFields(Lists.newArrayList(star)).setCollection(table)
 				.setPageConstraints(pageConstraints).setConstraints(constraint)
 				.setSortConstraint(sortConstraint).build();
 		verify(query, expected);
 	}
 
 	private void verify(String query, QlSelectStatement expected) {
 		try {
 			assertEquals(expected, inst.parseQlSelect(query));
 		} catch (InvalidQueryException e) {
 			Assert.fail(e.getMessage() + "\n" + e.getStackTrace());
 		} catch (QueryValidationException e) {
 			Assert.fail(e.getMessage() + "\n" + e.getStackTrace());
 		}
 	}
 }
