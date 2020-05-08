 package ch.epfl.data.distribdb.parsing;
 
 import gudusoft.gsqlparser.EDbVendor;
 import gudusoft.gsqlparser.EExpressionType;
 import gudusoft.gsqlparser.ESqlStatementType;
 import gudusoft.gsqlparser.TBaseType;
 import gudusoft.gsqlparser.TGSqlParser;
 import gudusoft.gsqlparser.nodes.TExpression;
 import gudusoft.gsqlparser.nodes.TExpressionList;
 import gudusoft.gsqlparser.nodes.TFunctionCall;
 import gudusoft.gsqlparser.nodes.TGroupByItem;
 import gudusoft.gsqlparser.nodes.TGroupByItemList;
 import gudusoft.gsqlparser.nodes.TJoin;
 import gudusoft.gsqlparser.nodes.TOrderByItem;
 import gudusoft.gsqlparser.nodes.TOrderByItemList;
 import gudusoft.gsqlparser.nodes.TResultColumn;
 import gudusoft.gsqlparser.nodes.TTable;
 import gudusoft.gsqlparser.stmt.TSelectSqlStatement;
 
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.ListIterator;
 import java.util.Map;
 
 /**
  * Parser of SQL queries.
  * 
  * @author Artyom Stetsenko
  */
 public class Parser {
 	
 	private Map<Relation, Map<String, Field>> relationFieldMap; // reuse fields such as relation.a
 	private Map<TSelectSqlStatement, Map<String, Field>> statementFieldMap; // reuse aliased (and relationless) fields in GROUP BY, HAVING, and ORDER BY
 	
 	/**
 	 * Parses a query and returns its parse tree.
 	 * 
 	 * @param query
 	 *                query string to parse
 	 * @return query parse tree
 	 */
 	public static QueryRelation parse(String query) {
 		
 		TGSqlParser parser = new TGSqlParser(EDbVendor.dbvansi);
 		parser.setSqltext(query);
 		
 		if (parser.parse() != 0) {
 			throw new IllegalArgumentException(parser.getErrormessage());
 		}
 		if (parser.getSqlstatements().size() > 1) {
 			throw new UnsupportedOperationException("Only one statement per query is supported at this time.");
 		}
 		if (parser.getSqlstatements().get(0).sqlstatementtype != ESqlStatementType.sstselect) {
 			throw new UnsupportedOperationException("Only SELECT queries are supported at this time.");
 		}
 		
 		return new Parser().parse((TSelectSqlStatement)parser.getSqlstatements().get(0));
 	}
 	
 	private QueryRelation parse(TSelectSqlStatement statement) {
 		return this.parse(statement, null);
 	}
 	
 	private QueryRelation parse(TSelectSqlStatement statement, List<Relation> parentRelations) {
 		
 		if (statement.isCombinedQuery()) {
 			throw new UnsupportedOperationException("Combined SELECT queries are not supported at this time.");
 		}
 		
 		// FROM
 		List<Relation> relations = new LinkedList<Relation>();
 		for (int i = 0; i < statement.joins.size(); i++) {
 			TJoin join = statement.joins.getJoin(i);
 			switch (join.getKind()) {
 			case TBaseType.join_source_fake:
 				TTable table = join.getTable();
 				Relation relation = null;
 				if (table.subquery == null) {
 					relation = new NamedRelation(join.getTable().getName());
 				} else {
 					relation = this.parse(table.subquery, parentRelations);
 				}
 				if (table.getAliasClause() != null) {
 					relation.setAlias(table.getAliasClause().toString());
 				}
 				relations.add(relation);
 				break;
 			default:
 				throw new UnsupportedOperationException("JOIN clauses in queries are not supported at this time, please specify your join conditions in the WHERE clause.");
 			}
 		}
 		
 		List<Relation> relationCandidates = new LinkedList<Relation>(relations);
 		/* no correlated queries
 		if (parentRelations != null) {
 			relationCandidates.addAll(parentRelations);
 		}
 		*/
 		
 		// SELECT
 		List<Field> fields = new LinkedList<Field>();
 		for (int i = 0; i < statement.getResultColumnList().size(); i++) {
 			fields.add(this.extractField(
 					statement,
 					statement.getResultColumnList().getResultColumn(i),
 					relations
 					)); // not candidateRelations because no support for outer query fields in SELECT
 		}
 		
 		QueryRelation relation = new QueryRelation(fields, relations);
 		
 		// WHERE
 		if (statement.getWhereClause() != null && statement.getWhereClause().getCondition() != null) {
 			List<Qualifier> qualifiers = new LinkedList<Qualifier>();
 			for (TExpression expression : this.extractExpressions(statement.getWhereClause().getCondition())) {
 				Operator operator;
 				Operand operand1 = null;
 				Operand operand2 = null;
 				TExpression leftOperand = expression.getLeftOperand();
 				TExpression rightOperand = expression.getRightOperand();
 				switch (expression.getExpressionType()) {
 				case simple_comparison_t:
 					if (leftOperand.getExpressionType() != EExpressionType.simple_object_name_t) {
 						throw new UnsupportedOperationException(String.format(
 								"Only field references are supported as left operands of comparison operators at this time: %s",
 								leftOperand
 								));
 					}
 					operator = Operator.forOperatorString(expression.getOperatorToken().toString());
 					if (operator == null) {
 						throw new UnsupportedOperationException(String.format(
 								"Operator %s is not supported at this time.",
 								expression.getOperatorToken()
 								));
 					}
 					switch (rightOperand.getExpressionType()) {
 					case simple_object_name_t:
 						operand2 = this.extractField(rightOperand, relationCandidates);
 						break;
 					case simple_constant_t:
 						operand2 = new LiteralOperand(rightOperand.toString());
 						break;
 					case subquery_t:
 						operand2 = this.parse(rightOperand.getSubQuery(), relationCandidates);
 						break;
 					default:
 						throw new UnsupportedOperationException(String.format(
 								"Only field references, constants, and subqueries are supported as right operands of comparison operators at this time: %s",
 								rightOperand
 								));
 					}
 					qualifiers.add(new Qualifier(
 							operator,
 							new LinkedList<Operand>(Arrays.<Operand>asList(
 									this.extractField(leftOperand, relationCandidates),
 									operand2
 									)
 							)));
 					break;
 				case logical_not_t:
 					if (expression.getRightOperand().getExpressionType() == EExpressionType.exists_t) {
 						qualifiers.add(new Qualifier(
 								Operator.NOT_EXISTS,
 								this.parse(expression.getRightOperand().getSubQuery(), relationCandidates)
 								));
 					} else {
 						throw new IllegalArgumentException(String.format(
 								"Expressions with operator NOT are only supported in combination with IN and EXISTS operators at this time: %s",
 								expression.getRightOperand()
 								));
 					}
 					break;
 				case exists_t:
 					qualifiers.add(new Qualifier(
 							Operator.EXISTS,
 							this.parse(expression.getSubQuery(), relationCandidates)
 							));
 					break;
 				case in_t:
 					if (rightOperand.getExpressionType() != EExpressionType.subquery_t) {
 						throw new UnsupportedOperationException(String.format(
 								"Only subqueries are supported as right operands of IN operators at this time: %s." +
 								rightOperand
 								));
 					}
 					qualifiers.add(new Qualifier(
 							expression.getNotToken() == null ? Operator.IN : Operator.NOT_IN,
 							new LinkedList<Operand>(Arrays.<Operand>asList(
 									this.extractField(expression.getLeftOperand(), relationCandidates),
 									this.parse(expression.getRightOperand().getSubQuery(), relationCandidates)
 									)
 							)));
 					break;
 				case between_t:
 					switch (leftOperand.getExpressionType()) {
 					case simple_object_name_t:
 						operand2 = this.extractField(leftOperand, relationCandidates);
 						break;
 					case simple_constant_t:
 						operand2 = new LiteralOperand(leftOperand.toString());
 						break;
 					case subquery_t:
 						operand2 = this.parse(leftOperand.getSubQuery(), relationCandidates);
 						break;
 					default:
 						throw new UnsupportedOperationException(String.format(
 								"Only field references, constants, and subqueries are supported as middle operands of BETWEEN operator at this time: %s",
 								leftOperand
 								));
 					}
 					Operand operand3 = null;
 					switch (rightOperand.getExpressionType()) {
 					case simple_object_name_t:
 						operand3 = this.extractField(rightOperand, relationCandidates);
 						break;
 					case simple_constant_t:
 						operand3 = new LiteralOperand(rightOperand.toString());
 						break;
 					case subquery_t:
 						operand3 = this.parse(rightOperand.getSubQuery(), relationCandidates);
 						break;
 					default:
 						throw new UnsupportedOperationException(String.format(
 								"Only field references, constants, and subqueries are supported as right operands of BETWEEN operator at this time: %s",
 								rightOperand
 								));
 					}
 					TExpression betweenOperand = expression.getBetweenOperand();
 					if (betweenOperand.getExpressionType() != EExpressionType.simple_object_name_t) {
 						throw new UnsupportedOperationException(String.format(
 								"Only field references are supported as left operands of BETWEEN operator at this time: %s",
 								betweenOperand
 								));
 					}
 					operand1 = this.extractField(betweenOperand, relationCandidates);
 					qualifiers.add(new Qualifier(
 							Operator.BETWEEN,
 							new LinkedList<Operand>(Arrays.<Operand>asList(
 									operand1,
 									operand2,
 									operand3
 									)
 							)));
 					break;
 				default:
 					throw new UnsupportedOperationException(String.format(
 							"Expressions with operator %s are not supported at this time.",
 							expression.getOperatorToken()
 							));
 				}
 			}
 			relation.setQualifiers(qualifiers);
 		}
 		
 		// GROUP BY + HAVING
 		if (statement.getGroupByClause() != null) {
 			List<Field> grouping = new LinkedList<Field>();
 			TGroupByItemList groupByItems = statement.getGroupByClause().getItems();
 			if (groupByItems != null) {
 				for (int i = 0; i < groupByItems.size(); i++) {
 					TGroupByItem groupByItem = groupByItems.getGroupByItem(i);
 					if (groupByItem.getRollupCube() != null || groupByItem.getGroupingSet() != null) {
 						throw new UnsupportedOperationException(String.format(
 								"The following GROUP BY item is unsupported at this time: %s.",
 								groupByItem
 								));
 					}
 					grouping.add(this.extractField(
 							statement, // include statement
 							groupByItem.getExpr(),
 							relations
 							)); // GROUP BY cannot be on fields from outer queries, hence not relationCandidates
 				}
 			}
 			relation.setGrouping(grouping);
 			
 			// HAVING
 			if (statement.getGroupByClause().getHavingClause() != null) {
 				throw new UnsupportedOperationException("HAVING clause is unsupported at this time.");
 			}
 		}
 		
 		// ORDER BY
 		if (statement.getOrderbyClause() != null) {
 			List<OrderingItem> ordering = new LinkedList<OrderingItem>();
 			TOrderByItemList orderByItems = statement.getOrderbyClause().getItems();
 			for (int i = 0; i < orderByItems.size(); i++) {
 				TOrderByItem orderByItem = orderByItems.getOrderByItem(i);
 				String orderingTypeName = null;
 				switch (orderByItem.getSortType()) {
 				case 0:
 					break;
 				case 1:
 					orderingTypeName = "ASC";
 					break;
 				case 2:
 					orderingTypeName = "DESC";
 					break;
 				default: // should never be the case
 					throw new UnsupportedOperationException(String.format(
 							"Ordering type %s is not supported: %s.",
 							orderByItem.getSortType(),
 							orderByItem
 							));
 				}
 				ordering.add(new OrderingItem(
 						this.extractField(statement, orderByItem.getSortKey(), relations), // include statement
 						OrderingType.forTypeName(orderingTypeName)
 						));
 			}
 			relation.setOrdering(ordering);
 		}
 		
 		return relation;
 	}
 	
 	private Field extractField(TSelectSqlStatement statement, TResultColumn column, List<Relation> relationCandidates) {
 		Field field = this.extractField(statement, column.getExpr(), relationCandidates);
 		if (column.getAliasClause() != null) {
 			String alias = column.getAliasClause().toString();
 			field.setAlias(alias);
 			if (this.statementFieldMap == null) {
 				this.statementFieldMap = new HashMap<TSelectSqlStatement, Map<String, Field>>();
 			}
 			if (this.statementFieldMap.get(statement) == null) {
 				this.statementFieldMap.put(statement, new HashMap<String, Field>());
 			}
 			if (this.statementFieldMap.get(statement).get(alias) == null) {
 				this.statementFieldMap.get(statement).put(alias, field);
 			}
 			field = this.statementFieldMap.get(statement).get(alias);
 		}
 		return field;
 	}
 	
 	private Field extractField(TExpression expression, List<Relation> relationCandidates) {
 		return this.extractField(null, expression, relationCandidates);
 	}
 	
 	private Field extractField(TSelectSqlStatement statement, TExpression expression, List<Relation> relationCandidates) {
 		
 		Field field = null;
 		Aggregate aggregate = null;
 		
 		switch (expression.getExpressionType()) {
 		
 		case simple_constant_t: // e.g. 1
 			field = new LiteralField(expression.getConstantOperand().toString());
 			break;
 			
 		case arithmetic_plus_t:
 		case arithmetic_minus_t:
 		case arithmetic_times_t:
 		case arithmetic_divide_t:
 		case arithmetic_modulo_t:
 		case exponentiate_t:
 		case parenthesis_t: // e.g. a * b + (c % d)
 			Field field1 = this.extractField(expression.getLeftOperand(), relationCandidates);
 			Field field2 = null;
 			if (expression.getRightOperand() != null) {
 				field2 = this.extractField(expression.getRightOperand(), relationCandidates);
 			}
 			StringBuilder fieldExpression = new StringBuilder();
 			List<Field> fieldFields = new LinkedList<Field>();
 			int count1 = 1;
 			if (field1 instanceof ExpressionField) {
 				ExpressionField expressionField1 = (ExpressionField)field1;
 				count1 = expressionField1.getFields().size();
 				fieldExpression.append(expressionField1.getExpression());
 				fieldFields.addAll(expressionField1.getFields());
 			} else if (field1 instanceof LiteralField) {
 				fieldExpression.append(field1);
 			} else {
 				fieldExpression.append(ExpressionField.PLACEHOLDER + "1");
 				fieldFields.add(field1);
 			}
 			if (expression.getExpressionType() == EExpressionType.parenthesis_t) {
 				fieldExpression.insert(0, expression.getStartToken()); // "("
 			} else {
 				fieldExpression.append(" ").append(expression.getOperatorToken()).append(" ");
 			}
 			if (field2 != null && field2 instanceof ExpressionField) {
 				ExpressionField expressionField2 = (ExpressionField)field2;
 				String field2Expression = expressionField2.getExpression();
 				for (int i = 1; i <= expressionField2.getFields().size(); i++) {
 					field2Expression = field2Expression.replaceAll(ExpressionField.PLACEHOLDER + i, ExpressionField.PLACEHOLDER + (i + count1));
 				}
 				fieldExpression.append(field2Expression);
 				for (Field field2Field : expressionField2.getFields()) {
 					if (!fieldFields.contains(field2Field)) {
 						fieldFields.add(field2Field);
 					}
 				}
 			} else if (field2 != null && field2 instanceof LiteralField) {
 				fieldExpression.append(field2);
 			} else if (field2 != null) {
 				fieldExpression.append(ExpressionField.PLACEHOLDER + (fieldFields.size() + 1));
 				fieldFields.add(field2);
 			}
 			if (expression.getExpressionType() == EExpressionType.parenthesis_t) {
 				fieldExpression.append(expression.getEndToken()); // ")"
 			}
 			field = new ExpressionField(fieldExpression.toString(), fieldFields);
 			break;
 			
 		case function_t: // e.g. func(a), can be aggregate or not
 			TFunctionCall function = expression.getFunctionCall();
 			String functionName = function.getFunctionName().toString();
 			aggregate = Aggregate.forFunctionName(functionName);
 			TExpressionList functionArguments = function.getArgs();
 			if (aggregate != null) { // known aggregate function
 				if (functionArguments.size() != 1) {
 					throw new UnsupportedOperationException(String.format(
 							"Only 1-argument aggregate functions are supported at this time: %s.",
 							expression
 							));
				} else if (functionArguments.getExpression(0).toString().equals("*")) {
					field = new AggregateField(aggregate, new LiteralField("*"));
 				} else {
 					field = new AggregateField(aggregate, this.extractField(functionArguments.getExpression(0), relationCandidates));
 				}
 			} else { // assuming function call (non-aggregate)
 				switch (function.getFunctionType()) {
 				case extract_t:
 					field = new FunctionField(
 							functionName,
 							new ExpressionField(
 									function.getExtract_time_token() + " FROM " + ExpressionField.PLACEHOLDER + "1",
 									this.extractField(function.getExpr1(), relationCandidates)
 									)
 							);
 					break;
 				default:
 					throw new UnsupportedOperationException(String.format(
 							"Function %s is not supported at this time.",
 							function
 							));
 				}
 			}
 			break;
 			
 		case simple_object_name_t: // e.g. relation.f
 			String[] fieldParts = expression.toString().split("\\.");
 			if (fieldParts.length != 2) { // try to match with aliased fields first
 				if (fieldParts.length == 1 && statement != null && this.statementFieldMap != null && this.statementFieldMap.get(statement) != null && this.statementFieldMap.get(statement).get(fieldParts[0]) != null) {
 					field = this.statementFieldMap.get(statement).get(fieldParts[0]);
 					break;
 				} else {
 					throw new IllegalArgumentException(String.format(
 							"Relation of field %s is unspecified, please specify field names as {relation name or alias}.{field name}.",
 							expression
 							));
 				}
 			}
 			String tableName = fieldParts[0];
 			String fieldName = fieldParts[1];
			if (fieldName.equals("*")) {
				throw new IllegalArgumentException("* fields are not supported at this time.");
			}
 			Relation relation = null;
 			for (ListIterator<Relation> it = relationCandidates.listIterator(relationCandidates.size()); it.hasPrevious(); ) {
 				Relation candidateRelation = it.previous();
 				if ((candidateRelation.getAlias() != null && candidateRelation.getAlias().equals(tableName)) || (candidateRelation.getAlias() == null && (candidateRelation instanceof NamedRelation) && ((NamedRelation)candidateRelation).getName().equals(tableName))) {
 					relation = candidateRelation;
 					break;
 				}
 			}
 			if (relation == null) {
 				throw new IllegalArgumentException(String.format(
 						"Could not find relation %s for field %s.%s. Note that correlated queries are not supported at this time.",
 						tableName,
 						tableName,
 						fieldName
 						));
 			}
 			if (this.relationFieldMap == null) {
 				this.relationFieldMap = new HashMap<Relation, Map<String, Field>>();
 			}
 			if (this.relationFieldMap.get(relation) == null) {
 				this.relationFieldMap.put(relation, new HashMap<String, Field>());
 			}
 			if (this.relationFieldMap.get(relation).get(fieldName) == null) {
 				this.relationFieldMap.get(relation).put(fieldName, new NamedField(relation, fieldName));
 			}
 			field = this.relationFieldMap.get(relation).get(fieldName);
 			break;
 			
 		default:
 			throw new UnsupportedOperationException(String.format(
 					"Expression SELECT fields such as %s are not supported at this time.",
 					expression
 					));
 		}
 		
 		return field;
 	}
 	
 	private List<TExpression> extractExpressions(TExpression expression) {
 		List<TExpression> expressions = new LinkedList<TExpression>();
 		if (expression.getExpressionType() != EExpressionType.logical_and_t) {
 			expressions.add(expression);
 		} else {
 			expressions.addAll(this.extractExpressions(expression.getLeftOperand()));
 			expressions.addAll(this.extractExpressions(expression.getRightOperand()));
 		}
 		return expressions;
 	}
 	
 	public static void main(String[] args) { // tests
 		System.out.println(Parser.parse("select sum(test.a) from test where test.k in (select blah.b from blah where blah.c = test.d) and test.e = 100"));
 		System.out.println(Parser.parse("SELECT S.id FROM S   WHERE NOT EXISTS ( SELECT C.id FROM C WHERE NOT EXISTS (        SELECT T.sid                FROM T          WHERE S.id = T.sid AND              C.id = T.cid                        )              )"));
 		System.out.println(Parser.parse("SELECT myS.id FROM (SELECT S.id FROM S ) myS,(SELECT T.sid FROM T) myT WHERE myS.id = myT.sid"));
 		System.out.println(Parser.parse("SELECT shipping.supp_nation, shipping.cust_nation, shipping.l_year, SUM(shipping.volume) AS revenue FROM (SELECT n1.n_name AS supp_nation, n2.n_name AS cust_nation, lineitem.l_shipdate AS l_year, lineitem.l_extendedprice AS volume FROM supplier, lineitem, orders, customer, nation n1, nation n2 WHERE supplier.s_suppkey = lineitem.l_suppkey AND orders.o_orderkey = lineitem.l_orderkey AND customer.c_custkey = orders.o_custkey AND supplier.s_nationkey = n1.n_nationkey AND customer.c_nationkey = n2.n_nationkey AND n1.n_name = 'GERMANY' AND n2.n_name = 'FRANCE' AND lineitem.l_shipdate BETWEEN '1995-01-01' AND '1996-12-31') shipping GROUP BY shipping.supp_nation, shipping.cust_nation, shipping.l_year"));
 	}
 }
