 /**
  * Copyright (C) 2010  Marcellus C. Tavares
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
 
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package org.orcas.iocl.util;
 
 public enum Template {
 
 	ABS("abs"),
 
 	ALT("alt"),
 
 	AND("and"),
 
 	ANY("any"),
 
 	APPEND("append"),
 
 	ARITHMETIC_EXPRESSION("arithmeticExp"),
 
 	ASSIGN("assign"),
 
 	AS_BAG("asBag"),
 
 	AS_ORDERED_SET("asOrderedSet"),
 
 	AS_SEQUENCE("asSequence"),
 
 	AS_SET("asSet"),
 
 	AT("at"),
 
 	BLOCK("block"),
 
 	BREAK("break"),
 
 	CATCH("catch"),
 
 	CLOSURE("closure"),
 
 	COLLECT("collect"),
 
 	COLLECTION("collection"),
 
 	COLLECT_NESTED("collectNested"),
 
 	COMPUTE("compute"),
 
 	CONCAT("concat"),
 
 	CONTINUE("continue"),
 
 	COUNT("count"),
 
 	CUSTOM("custom"),
 
 	DIV("div"),
 
 	EQUAL("="),
 
 	EXCLUDES("excludes"),
 
 	EXCLUDING("excluding"),
 
 	EXISTS("exists"),
 
 	FIRST("first"),
 
 	FLATTEN("flatten"),
 
 	FLOOR("floor"),
 
 	FOR("for"),
 
 	FOR_ALL("forAll"),
 
 	GT(">"),
 
 	GTE(">="),
 
 	INCLUDES("includes"),
 
 	INCLUDES_ALL("includesAll"),
 
 	INCLUDING("including"),
 
 	INDEX_OF("indexOf"),
 
 	INSERT_AT("insertAt"),
 
 	INTERSECTION("intersection"),
 
 	IS_EMPTY("isEmpty"),
 
 	IS_UNIQUE("isUnique"),
 
 	LAST("last"),
 
 	LT("<"),
 
 	LTE("<="),
 
 	MAX("max"),
 
 	MIN("min"),
 
	MINUS("-"),

 	MOD("mod"),
 
 	NOT("not"),
 
 	NOT_EMPTY("not"),
 
 	NOT_EQUAL("<>"),
 
 	OCL_AS_TYPE("oclAsType"),
 
 	OCL_IS_KIND_OF("oclIsKindOf"),
 
 	OCL_IS_TYPE_OF("oclIsTypeOf"),
 
 	ONE("one"),
 
 	OR("or"),
 
 	PREPEND("prepend"),
 
 	PRODUCT("product"),
 
 	PROPERTY_CALL("property_call"),
 
 	RAISE("raise"),
 
 	REJECT("reject"),
 
 	RETURN("return"),
 
 	ROUND("round"),
 
 	SELECT("select"),
 
 	SIZE("size"),
 
 	SORTED_BY("sortedBy"),
 
 	SUBSTRING("substring"),
 
 	SUB_ORDERED_SET("subOrderedSet"),
 
 	SUB_SEQUENCE("subSequence"),
 
 	SUM("sum"),
 
 	SWITCH("switch"),
 
 	SYMMETRIC_DIFFERENCE("symmetricDifference"),
 
 	TO_INTEGER("toInteger"),
 
 	TO_LOWER("toLower"),
 
 	TO_REAL("toReal"),
 
 	TO_UPPER("toUpper"),
 
 	TRY("try"),
 
 	UNION("union"),
 
 	VARIABLE("variable"),
 
 	WHILE("while"),
 
 	XOR("xor");
 
 	public static Template[] TEMPLATES = {
 		ABS, ALT, AND, ANY, APPEND, ARITHMETIC_EXPRESSION, ASSIGN, AT, AS_BAG,
 		AS_ORDERED_SET, AS_SEQUENCE, AS_SET, BLOCK, BREAK, CATCH, CLOSURE,
 		COLLECT, COLLECTION, COLLECT_NESTED, COMPUTE, CONCAT, CONTINUE, COUNT,
 		CUSTOM, DIV, EQUAL, EXCLUDES, EXCLUDING, EXISTS, FIRST, FLATTEN, FLOOR,
 		FOR, FOR_ALL, GT, GTE, INCLUDES, INCLUDES_ALL, INCLUDING, INDEX_OF,
 		INSERT_AT, INTERSECTION, IS_EMPTY, IS_UNIQUE, LAST, LT, LTE, MAX, MIN,
		MINUS, MOD, NOT, NOT_EMPTY, NOT_EQUAL, OCL_AS_TYPE, OCL_IS_KIND_OF,
 		OCL_IS_TYPE_OF, ONE, OR, PREPEND, PRODUCT, PROPERTY_CALL, RAISE, REJECT,
 		RETURN, ROUND, SELECT, SIZE, SORTED_BY, SUBSTRING, SUB_ORDERED_SET,
 		SUB_SEQUENCE, SUM, SWITCH, SYMMETRIC_DIFFERENCE, TRY, TO_INTEGER,
 		TO_LOWER, TO_REAL, TO_UPPER, UNION, VARIABLE, WHILE, XOR};
 
 	public static Template getByName(String templateName) {
 		for (int i = 0; i < TEMPLATES.length; i++) {
 			if (Validator.equals(
 					TEMPLATES[i].getTemplateName(), templateName)) {
 
 				return TEMPLATES[i];
 			}
 		}
 
 		return null;
 	}
 
 	public String getTemplateName() {
 		return _templateName;
 	}
 
 	private Template(String templateName) {
 		_templateName = templateName;
 	}
 
 	private String _templateName;
 
 }
