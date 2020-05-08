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
 
 package org.orcas.ioclengine.util;
 
 public enum Operation {
 
 	ABS(1, "abs"),
 
 	AND(2, "and"),
 
 	ANY(3, "any"),
 
 	APPEND(4, "append"),
 
 	AS_BAG(6, "asBag"),
 
 	AS_ORDERED_SET(7, "asOrderedSet"),
 
 	AS_SEQUENCE(8, "asSequence"),
 
 	AS_SET(9, "asSet"),
 
 	AT(5, "at"),
 
 	CLOSURE(10, "closure"),
 
 	COLLECT(11, "collect"),
 
 	COLLECT_NESTED(12, "collectNested"),
 
 	CONCAT(13, "concat"),
 
 	COUNT(14, "count"),
 
 	CUSTOM(15, "custom"),
 
 	DIV(16, "div"),
 
 	DIVIDE(17, "/"),
 
 	EQUAL(18, "="),
 
 	EXCLUDES(19, "excludes"),
 
 	EXCLUDING(20, "excluding"),
 
 	EXISTS(21, "exists"),
 
 	FIRST(22, "first"),
 
 	FLATTEN(23, "flatten"),
 
 	FLOOR(24, "floor"),
 
 	FOR_ALL(25, "forAll"),
 
 	GT(26, ">"),
 
 	GTE(27, ">="),
 
 	INCLUDES(28, "includes"),
 
 	INCLUDES_ALL(29, "includesAll"),
 
 	INCLUDING(30, "including"),
 
 	INDEX_OF(31, "indexOf"),
 
 	INSERT_AT(32, "insertAt"),
 
 	INTERSECTION(33, "intersection"),
 
 	IS_EMPTY(34, "isEmpty"),
 
 	IS_UNIQUE(35, "isUnique"),
 
 	LAST(36, "last"),
 
 	LT(37, "<"),
 
 	LTE(38, "<="),
 
 	MAX(39, "max"),
 
 	MIN(40, "min"),
 
 	MINUS(41, "-"),
 
 	MOD(42, "mod"),
 
 	MULT(43, "*"),
 
 	NOT(44, "not"),
 
 	NOT_EMPTY(45, "not"),
 
 	NOT_EQUAL(46, "<>"),
 
 	OCL_AS_TYPE(47, "oclAsType"),
 
 	OCL_IS_KIND_OF(48, "oclIsKindOf"),
 
 	OCL_IS_TYPE_OF(49, "oclIsTypeOf"),
 
 	ONE(50, "one"),
 
 	OR(51, "or"),
 
 	PLUS(52, "+"),
 
 	PREPEND(53, "prepend"),
 
 	PRODUCT(54, "product"),
 
 	REJECT(55, "reject"),
 
 	ROUND(56, "round"),
 
 	SELECT(57, "select"),
 
 	SIZE(58, "size"),
 
 	SORTED_BY(59, "sortedBy"),
 
 	SUBSTRING(60, "substring"),
 
 	SUB_ORDERED_SET(61, "subOrderedSet"),
 
 	SUB_SEQUENCE(62, "subSequence"),
 
 	SYMMETRIC_DIFFERENCE(63, "symmetricDifference"),
 
 	SUM(64, "sum"),
 
 	TO_INTEGER(65, "toInteger"),
 
 	TO_LOWER(66, "toLower"),
 
 	TO_REAL(67, "toReal"),
 
 	TO_UPPER(68, "toUpper"),
 
 	UNION(69, "union"),
 
 	XOR(70, "xor");
 
 	public static Operation[] OPERATIONS = {
 		ABS, AND, ANY, APPEND, AT, AS_BAG, AS_ORDERED_SET, AS_SEQUENCE, AS_SET,
 		CLOSURE, COLLECT, COLLECT_NESTED, CONCAT, COUNT, CUSTOM, DIV, DIVIDE,
 		EQUAL, EXCLUDES, EXCLUDING, EXISTS, FIRST, FLATTEN, FLOOR, FOR_ALL, GT,
 		GTE, INCLUDES, INCLUDES_ALL, INCLUDING, INDEX_OF, INSERT_AT,
 		INTERSECTION, IS_EMPTY, IS_UNIQUE, LAST, LT, LTE, MAX, MIN, MINUS, MOD,
 		MULT, NOT, NOT_EMPTY, NOT_EQUAL, OCL_AS_TYPE, OCL_IS_KIND_OF,
		OCL_IS_TYPE_OF, ONE, OR, PLUS, PREPEND, PRODUCT, REJECT, ROUND, SELECT,
		SIZE, SORTED_BY, SUBSTRING, SUB_ORDERED_SET, SUB_SEQUENCE,
		SYMMETRIC_DIFFERENCE, SUM, TO_INTEGER, TO_LOWER, TO_REAL, TO_UPPER,
		UNION, XOR};
 
 	public static String fromOperationCode(int operationCode) {
 		for (int i = 0; i < OPERATIONS.length; i++) {
 			if (OPERATIONS[i].getOperationCode() == operationCode) {
 				return OPERATIONS[i].getOperationName();
 			}
 		}
 
 		return null;
 	}
 
 	public static Operation getByOperationCode(int operationCode) {
 		for (int i = 0; i < OPERATIONS.length; i++) {
 			if (OPERATIONS[i].getOperationCode() == operationCode) {
 				return OPERATIONS[i];
 			}
 		}
 
 		return null;
 	}
 
 	public static int toOperationCode(String operationName) {
 		for (int i = 0; i < OPERATIONS.length; i++) {
 			if (Validator.equals(
 					OPERATIONS[i].getOperationName(), operationName)) {
 
 				return (Integer) OPERATIONS[i].getOperationCode();
 			}
 		}
 
 		return CUSTOM.getOperationCode();
 	}
 
 	public int getOperationCode() {
 		return _code;
 	}
 
 	public String getOperationName() {
 		return _name;
 	}
 
 
 	private Operation(int code, String name) {
 		_code = code;
 		_name = name;
 	}
 
 	private int _code;
 	private String _name;
 
 }
