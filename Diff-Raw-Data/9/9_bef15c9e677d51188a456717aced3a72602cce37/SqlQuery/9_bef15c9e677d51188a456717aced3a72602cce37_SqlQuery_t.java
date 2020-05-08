 package com.psddev.dari.db;
 
 import com.psddev.dari.util.ObjectUtils;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Locale;
 import java.util.Map;
 import java.util.Set;
 import java.util.UUID;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 /** Internal representation of an SQL query based on a Dari one. */
 class SqlQuery {
 
     private static final Pattern QUERY_KEY_PATTERN = Pattern.compile("\\$\\{([^}]+)\\}");
 
     private final SqlDatabase database;
     private final Query<?> query;
     private final String aliasPrefix;
 
     private final SqlVendor vendor;
     private final String recordIdField;
     private final String recordTypeIdField;
     private final String recordInRowIndexField;
     private final Map<String, Query.MappedKey> mappedKeys;
     private final Map<String, ObjectIndex> selectedIndexes;
 
     private String fromClause;
     private String whereClause;
     private String havingClause;
     private String orderByClause;
     private List<String> orderBySelectColumns = new ArrayList<String>();
     private final List<Join> joins = new ArrayList<Join>();
     private final Map<Query<?>, String> subQueries = new LinkedHashMap<Query<?>, String>();
     private final Map<Query<?>, SqlQuery> subSqlQueries = new HashMap<Query<?>, SqlQuery>();
 
     private boolean needsDistinct;
     private Join mysqlIndexHint;
 
     /**
      * Creates an instance that can translate the given {@code query}
      * with the given {@code database}.
      */
     public SqlQuery(
             SqlDatabase initialDatabase,
             Query<?> initialQuery,
             String initialAliasPrefix) {
 
         database = initialDatabase;
         query = initialQuery;
         aliasPrefix = initialAliasPrefix;
 
         vendor = database.getVendor();
         recordIdField = aliasedField("r", SqlDatabase.ID_COLUMN);
         recordTypeIdField = aliasedField("r", SqlDatabase.TYPE_ID_COLUMN);
         recordInRowIndexField = aliasedField("r", SqlDatabase.IN_ROW_INDEX_COLUMN);
         mappedKeys = query.mapEmbeddedKeys(database.getEnvironment());
         selectedIndexes = new HashMap<String, ObjectIndex>();
 
         for (Map.Entry<String, Query.MappedKey> entry : mappedKeys.entrySet()) {
             selectIndex(entry.getKey(), entry.getValue());
         }
     }
 
     private void selectIndex(String queryKey, Query.MappedKey mappedKey) {
         ObjectIndex selectedIndex = null;
         int maxMatchCount = 0;
 
         for (ObjectIndex index : mappedKey.getIndexes()) {
             List<String> indexFields = index.getFields();
             int matchCount = 0;
 
             for (Query.MappedKey mk : mappedKeys.values()) {
                 ObjectField mkf = mk.getField();
                 if (mkf != null && indexFields.contains(mkf.getInternalName())) {
                     ++ matchCount;
                 }
             }
 
             if (matchCount > maxMatchCount) {
                 selectedIndex = index;
                 maxMatchCount = matchCount;
             }
         }
 
         if (selectedIndex != null) {
             if (maxMatchCount == 1) {
                 for (ObjectIndex index : mappedKey.getIndexes()) {
                     if (index.getFields().size() == 1) {
                         selectedIndex = index;
                         break;
                     }
                 }
             }
 
             selectedIndexes.put(queryKey, selectedIndex);
         }
     }
 
     public SqlQuery(SqlDatabase initialDatabase, Query<?> initialQuery) {
         this(initialDatabase, initialQuery, "");
     }
 
     private String aliasedField(String alias, String field) {
         StringBuilder fieldBuilder = new StringBuilder();
         fieldBuilder.append(aliasPrefix);
         fieldBuilder.append(alias);
         fieldBuilder.append(".");
         vendor.appendIdentifier(fieldBuilder, field);
         return fieldBuilder.toString();
     }
 
     private SqlQuery getOrCreateSubSqlQuery(Query<?> subQuery) {
         SqlQuery subSqlQuery = subSqlQueries.get(subQuery);
         if (subSqlQuery == null) {
             subSqlQuery = new SqlQuery(database, subQuery, aliasPrefix + "s" + subSqlQueries.size());
             subSqlQuery.initializeClauses();
             subSqlQueries.put(subQuery, subSqlQuery);
         }
         return subSqlQuery;
     }
 
     /** Initializes FROM, WHERE, and ORDER BY clauses. */
     private void initializeClauses() {
         String extraJoins = ObjectUtils.to(String.class, query.getOptions().get(SqlDatabase.EXTRA_JOINS_QUERY_OPTION));
 
         if (extraJoins != null) {
             Matcher queryKeyMatcher = QUERY_KEY_PATTERN.matcher(extraJoins);
             int lastEnd = 0;
             StringBuilder newExtraJoinsBuilder = new StringBuilder();
 
             while (queryKeyMatcher.find()) {
                 newExtraJoinsBuilder.append(extraJoins.substring(lastEnd, queryKeyMatcher.start()));
                 lastEnd = queryKeyMatcher.end();
 
                 String queryKey = queryKeyMatcher.group(1);
                 Query.MappedKey mappedKey = query.mapEmbeddedKey(database.getEnvironment(), queryKey);
                 mappedKeys.put(queryKey, mappedKey);
                 selectIndex(queryKey, mappedKey);
                 Join join = getJoin(queryKey);
                 join.type = JoinType.LEFT_OUTER;
                 newExtraJoinsBuilder.append(join.getValueField(queryKey, null));
             }
 
             newExtraJoinsBuilder.append(extraJoins.substring(lastEnd));
             extraJoins = newExtraJoinsBuilder.toString();
         }
 
         // Builds the WHERE clause.
         StringBuilder whereBuilder = new StringBuilder();
         whereBuilder.append("\nWHERE ");
 
         String extraWhere = ObjectUtils.to(String.class, query.getOptions().get(SqlDatabase.EXTRA_WHERE_QUERY_OPTION));
         if (!ObjectUtils.isBlank(extraWhere)) {
             whereBuilder.append("(");
         }
 
         whereBuilder.append("1 = 1");
 
         if (!query.isFromAll()) {
             Set<ObjectType> types = query.getConcreteTypes(database.getEnvironment());
             whereBuilder.append("\nAND ");
 
             if (types.isEmpty()) {
                 whereBuilder.append("0 = 1");
 
             } else {
                 whereBuilder.append(recordTypeIdField);
                 whereBuilder.append(" IN (");
                 for (ObjectType type : types) {
                     vendor.appendValue(whereBuilder, type.getId());
                     whereBuilder.append(", ");
                 }
                 whereBuilder.setLength(whereBuilder.length() - 2);
                 whereBuilder.append(")");
             }
         }
 
         Predicate predicate = query.getPredicate();
         if (predicate != null) {
             whereBuilder.append("\nAND (");
             addWherePredicate(whereBuilder, predicate, null, false);
             whereBuilder.append(')');
         }
 
         if (!ObjectUtils.isBlank(extraWhere)) {
             whereBuilder.append(") ");
             whereBuilder.append(extraWhere);
         }
 
         // Builds the ORDER BY clause.
         StringBuilder orderByBuilder = new StringBuilder();
 
         for (Sorter sorter : query.getSorters()) {
             String operator = sorter.getOperator();
             boolean ascending = Sorter.ASCENDING_OPERATOR.equals(operator);
             boolean descending = Sorter.DESCENDING_OPERATOR.equals(operator);
             boolean closest = Sorter.CLOSEST_OPERATOR.equals(operator);
             boolean farthest = Sorter.CLOSEST_OPERATOR.equals(operator);
 
             if (ascending || descending || closest || farthest) {
                 String queryKey = (String) sorter.getOptions().get(0);
                 String joinValueField = getSortFieldJoin(queryKey).getValueField(queryKey, null);
                 Query<?> subQuery = mappedKeys.get(queryKey).getSubQueryWithSorter(sorter, 0);
 
                 if (subQuery != null) {
                     SqlQuery subSqlQuery = getOrCreateSubSqlQuery(subQuery);
                     subQueries.put(subQuery, joinValueField + " = ");
                     orderByBuilder.append(subSqlQuery.orderByClause.substring(9));
                     orderByBuilder.append(", ");
                     continue;
                 }
 
                 if (ascending || descending) {
                     orderByBuilder.append(joinValueField);
                     orderBySelectColumns.add(joinValueField);
 
                 } else if (closest || farthest) {
                     Location location = (Location) sorter.getOptions().get(1);
 
                     StringBuilder selectBuilder = new StringBuilder();
                     try {
                         vendor.appendNearestLocation(orderByBuilder, selectBuilder, whereBuilder, location, joinValueField);
                         orderBySelectColumns.add(selectBuilder.toString());
                     } catch(UnsupportedIndexException uie) {
                         throw new UnsupportedIndexException(vendor, queryKey);
                     }
                 }
 
                 orderByBuilder.append(' ');
                 orderByBuilder.append(ascending || closest ? "ASC" : "DESC");
                 orderByBuilder.append(", ");
                 continue;
             }
 
             throw new UnsupportedSorterException(database, sorter);
         }
 
         if (orderByBuilder.length() > 0) {
             orderByBuilder.setLength(orderByBuilder.length() - 2);
             orderByBuilder.insert(0, "\nORDER BY ");
         }
 
         // Builds the FROM clause.
         StringBuilder fromBuilder = new StringBuilder();
 
         for (Join join : joins) {
             if (join.indexKeys.isEmpty()) {
                 continue;
             }
 
             // e.g. JOIN RecordIndex AS i#
             fromBuilder.append("\n");
             fromBuilder.append(join.type.token);
             fromBuilder.append(" ");
             fromBuilder.append(join.table);
 
             if (join.type == JoinType.INNER && join.equals(mysqlIndexHint)) {
                 fromBuilder.append(" /*! USE INDEX (k_name_value) */");
             }
 
             // e.g. ON i#.recordId = r.id AND i#.name = ...
             fromBuilder.append(" ON ");
             fromBuilder.append(join.idField);
             fromBuilder.append(" = ");
             fromBuilder.append(aliasPrefix);
             fromBuilder.append("r.");
             vendor.appendIdentifier(fromBuilder, "id");
             fromBuilder.append(" AND ");
             fromBuilder.append(join.keyField);
             fromBuilder.append(" IN (");
 
             for (String indexKey : join.indexKeys) {
                 fromBuilder.append(join.quoteIndexKey(indexKey));
                 fromBuilder.append(", ");
             }
 
             fromBuilder.setLength(fromBuilder.length() - 2);
             fromBuilder.append(")");
         }
 
         for (Map.Entry<Query<?>, String> entry : subQueries.entrySet()) {
             Query<?> subQuery = entry.getKey();
             SqlQuery subSqlQuery = getOrCreateSubSqlQuery(subQuery);
 
             if (subSqlQuery.needsDistinct) {
                 needsDistinct = true;
             }
 
             fromBuilder.append("\nINNER JOIN ");
             vendor.appendIdentifier(fromBuilder, "Record");
             fromBuilder.append(" ");
             fromBuilder.append(subSqlQuery.aliasPrefix);
             fromBuilder.append("r ON ");
             fromBuilder.append(entry.getValue());
             fromBuilder.append(subSqlQuery.aliasPrefix);
             fromBuilder.append("r.");
             vendor.appendIdentifier(fromBuilder, "id");
             fromBuilder.append(subSqlQuery.fromClause);
         }
 
         if (extraJoins != null) {
             fromBuilder.append(' ');
             fromBuilder.append(extraJoins);
         }
 
         this.whereClause = whereBuilder.toString();
 
         String extraHaving = ObjectUtils.to(String.class, query.getOptions().get(SqlDatabase.EXTRA_HAVING_QUERY_OPTION));
         this.havingClause = ObjectUtils.isBlank(extraHaving) ? "" : "\nHAVING " + extraHaving;
 
         this.orderByClause = orderByBuilder.toString();
         this.fromClause = fromBuilder.toString();
     }
 
     /** Adds the given {@code predicate} to the {@code WHERE} clause. */
     private void addWherePredicate(
             StringBuilder whereBuilder,
             Predicate predicate,
             Predicate parentPredicate,
             boolean usesLeftJoin) {
 
         if (predicate instanceof CompoundPredicate) {
             CompoundPredicate compoundPredicate = (CompoundPredicate) predicate;
             String operator = compoundPredicate.getOperator();
             boolean isNot = PredicateParser.NOT_OPERATOR.equals(operator);
 
             // e.g. (child1) OR (child2) OR ... (child#)
             if (isNot || PredicateParser.OR_OPERATOR.equals(operator)) {
                 StringBuilder compoundBuilder = new StringBuilder();
                 List<Predicate> children = compoundPredicate.getChildren();
 
                 boolean usesLeftJoinChildren;
                 if (children.size() > 1) {
                     usesLeftJoinChildren = true;
                     needsDistinct = true;
                 } else {
                     usesLeftJoinChildren = isNot;
                 }
 
                 for (Predicate child : children) {
                     compoundBuilder.append("(");
                     addWherePredicate(compoundBuilder, child, predicate, usesLeftJoinChildren);
                     compoundBuilder.append(")\nOR ");
                 }
 
                 if (compoundBuilder.length() > 0) {
                     compoundBuilder.setLength(compoundBuilder.length() - 4);
 
                     // e.g. NOT ((child1) OR (child2) OR ... (child#))
                     if (isNot) {
                         whereBuilder.append("NOT (");
                         whereBuilder.append(compoundBuilder);
                         whereBuilder.append(")");
 
                     } else {
                         whereBuilder.append(compoundBuilder);
                     }
                 }
 
                 return;
 
             // e.g. (child1) AND (child2) AND .... (child#)
             } else if (PredicateParser.AND_OPERATOR.equals(operator)) {
                 StringBuilder compoundBuilder = new StringBuilder();
 
                 for (Predicate child : compoundPredicate.getChildren()) {
                     compoundBuilder.append("(");
                     addWherePredicate(compoundBuilder, child, predicate, usesLeftJoin);
                     compoundBuilder.append(")\nAND ");
                 }
 
                 if (compoundBuilder.length() > 0) {
                     compoundBuilder.setLength(compoundBuilder.length() - 5);
                     whereBuilder.append(compoundBuilder);
                 }
 
                 return;
             }
 
         } else if (predicate instanceof ComparisonPredicate) {
             ComparisonPredicate comparisonPredicate = (ComparisonPredicate) predicate;
             String queryKey = comparisonPredicate.getKey();
             Query.MappedKey mappedKey = mappedKeys.get(queryKey);
             boolean isFieldCollection = mappedKey.isInternalCollectionType();
 
             Join join = null;
             if (mappedKey.getField() != null &&
                     parentPredicate instanceof CompoundPredicate &&
                     PredicateParser.OR_OPERATOR.equals(((CompoundPredicate) parentPredicate).getOperator())) {
                 for (Join j : joins) {
                     if (j.parent == parentPredicate &&
                             j.sqlIndex.equals(SqlIndex.Static.getByType(mappedKeys.get(queryKey).getInternalType()))) {
                         join = j;
                         join.addIndexKey(queryKey);
                         needsDistinct = true;
                         break;
                     }
                 }
                 if (join == null) {
                     join = getJoin(queryKey);
                     join.parent = parentPredicate;
                 }
 
             } else if (isFieldCollection) {
                 join = createJoin(queryKey);
 
             } else {
                 join = getJoin(queryKey);
             }
 
             if (usesLeftJoin) {
                 join.type = JoinType.LEFT_OUTER;
             }
 
             if (isFieldCollection &&
                     (join.sqlIndexTable == null ||
                     join.sqlIndexTable.getVersion() < 2)) {
                 needsDistinct = true;
             }
 
             String joinValueField = join.getValueField(queryKey, comparisonPredicate);
             String operator = comparisonPredicate.getOperator();
             StringBuilder comparisonBuilder = new StringBuilder();
             boolean hasMissing = false;
             int subClauseCount = 0;
             boolean isNotEqualsAll = PredicateParser.NOT_EQUALS_ALL_OPERATOR.equals(operator);
 
             if (isNotEqualsAll || PredicateParser.EQUALS_ANY_OPERATOR.equals(operator)) {
                 Query<?> valueQuery = mappedKey.getSubQueryWithComparison(comparisonPredicate);
 
                 // e.g. field IN (SELECT ...)
                 if (valueQuery != null) {
                     if (isNotEqualsAll || isFieldCollection) {
                         needsDistinct = true;
                     }
 
                     if (findSimilarComparison(mappedKey.getField(), query.getPredicate())) {
                         whereBuilder.append(joinValueField);
                         if (isNotEqualsAll) {
                             whereBuilder.append(" NOT");
                         }
                         whereBuilder.append(" IN (");
                         whereBuilder.append(new SqlQuery(database, valueQuery).subQueryStatement());
                         whereBuilder.append(")");
 
                     } else {
                        SqlQuery subSqlQuery = getOrCreateSubSqlQuery(valueQuery);
                         subQueries.put(valueQuery, joinValueField + (isNotEqualsAll ? " != " : " = "));
                        whereBuilder.append(subSqlQuery.whereClause.substring(7));
                     }
 
                     return;
                 }
 
                 for (Object value : comparisonPredicate.resolveValues(database)) {
                     if (value == null) {
                         ++ subClauseCount;
                         comparisonBuilder.append("0 = 1");
 
                     } else if (value == Query.MISSING_VALUE) {
                         ++ subClauseCount;
                         hasMissing = true;
 
                         join.type = JoinType.LEFT_OUTER;
                         comparisonBuilder.append(joinValueField);
 
                         if (isNotEqualsAll) {
                             if (isFieldCollection) {
                                 needsDistinct = true;
                             }
                             comparisonBuilder.append(" IS NOT NULL");
                         } else {
                             comparisonBuilder.append(" IS NULL");
                         }
 
                     } else if (value instanceof Region) {
                         List<Location> locations = ((Region) value).getLocations();
                         if (!locations.isEmpty()) {
                             ++ subClauseCount;
 
                             if (isNotEqualsAll) {
                                 comparisonBuilder.append("NOT ");
                             }
 
                             try {
                                 vendor.appendWhereRegion(comparisonBuilder, (Region) value, joinValueField);
                             } catch (UnsupportedIndexException uie) {
                                 throw new UnsupportedIndexException(vendor, queryKey);
                             }
                         }
 
                     } else {
                         ++ subClauseCount;
 
                         if (isNotEqualsAll) {
                             join.type = JoinType.LEFT_OUTER;
                             needsDistinct = true;
                             hasMissing = true;
 
                             comparisonBuilder.append("(");
                             comparisonBuilder.append(joinValueField);
                             comparisonBuilder.append(" IS NULL OR ");
                             comparisonBuilder.append(joinValueField);
                             if (join.likeValuePrefix != null) {
                                 comparisonBuilder.append(" NOT LIKE ");
                                 join.appendValue(comparisonBuilder, comparisonPredicate, join.likeValuePrefix + database.getSymbolId(value.toString()) + ";%");
                             } else {
                                 comparisonBuilder.append(" != ");
                                 join.appendValue(comparisonBuilder, comparisonPredicate, value);
                             }
                             comparisonBuilder.append(")");
 
                         } else {
                             comparisonBuilder.append(joinValueField);
                             if (join.likeValuePrefix != null) {
                                 comparisonBuilder.append(" LIKE ");
                                 join.appendValue(comparisonBuilder, comparisonPredicate, join.likeValuePrefix + database.getSymbolId(value.toString()) + ";%");
                             } else {
                                 comparisonBuilder.append(" = ");
                                 join.appendValue(comparisonBuilder, comparisonPredicate, value);
                             }
                         }
                     }
 
                     comparisonBuilder.append(isNotEqualsAll ? " AND " : " OR  ");
                 }
 
                 if (comparisonBuilder.length() == 0) {
                     whereBuilder.append(isNotEqualsAll ? "1 = 1" : "0 = 1");
                     return;
                 }
 
             } else {
                 boolean isStartsWith = PredicateParser.STARTS_WITH_OPERATOR.equals(operator);
                 String sqlOperator =
                         isStartsWith ? "LIKE" :
                         PredicateParser.LESS_THAN_OPERATOR.equals(operator) ? "<" :
                         PredicateParser.LESS_THAN_OR_EQUALS_OPERATOR.equals(operator) ? "<=" :
                         PredicateParser.GREATER_THAN_OPERATOR.equals(operator) ? ">" :
                         PredicateParser.GREATER_THAN_OR_EQUALS_OPERATOR.equals(operator) ? ">=" :
                         null;
 
                 // e.g. field OP value1 OR field OP value2 OR ... field OP value#
                 if (sqlOperator != null) {
                     for (Object value : comparisonPredicate.resolveValues(database)) {
                         ++ subClauseCount;
 
                         if (value == null) {
                             comparisonBuilder.append("0 = 1");
 
                         } else if (value == Query.MISSING_VALUE) {
                             hasMissing = true;
 
                             join.type = JoinType.LEFT_OUTER;
                             comparisonBuilder.append(joinValueField);
                             comparisonBuilder.append(" IS NULL");
 
                         } else {
                             comparisonBuilder.append(joinValueField);
                             comparisonBuilder.append(" ");
                             comparisonBuilder.append(sqlOperator);
                             comparisonBuilder.append(" ");
                             if (isStartsWith) {
                                 value = value.toString() + "%";
                             }
                             join.appendValue(comparisonBuilder, comparisonPredicate, value);
                         }
 
                         comparisonBuilder.append(" OR  ");
                     }
 
                     if (comparisonBuilder.length() == 0) {
                         whereBuilder.append("0 = 1");
                         return;
                     }
                 }
             }
 
             if (comparisonBuilder.length() > 0) {
                 comparisonBuilder.setLength(comparisonBuilder.length() - 5);
 
                 if (!hasMissing) {
                     if (join.needsIndexTable) {
                         String indexKey = mappedKeys.get(queryKey).getIndexKey(selectedIndexes.get(queryKey));
                         if (indexKey != null) {
                             whereBuilder.append(join.keyField);
                             whereBuilder.append(" = ");
                             whereBuilder.append(join.quoteIndexKey(indexKey));
                             whereBuilder.append(" AND ");
                         }
                     }
 
                     whereBuilder.append(joinValueField);
                     whereBuilder.append(" IS NOT NULL AND ");
 
                     if (subClauseCount > 1) {
                         needsDistinct = true;
                         whereBuilder.append("(");
                         comparisonBuilder.append(")");
                     }
                 }
 
                 whereBuilder.append(comparisonBuilder);
                 return;
             }
         }
 
         throw new UnsupportedPredicateException(this, predicate);
     }
 
     private boolean findSimilarComparison(ObjectField field, Predicate predicate) {
         if (field != null) {
             if (predicate instanceof CompoundPredicate) {
                 for (Predicate child : ((CompoundPredicate) predicate).getChildren()) {
                     if (findSimilarComparison(field, child)) {
                         return true;
                     }
                 }
 
             } else if (predicate instanceof ComparisonPredicate) {
                 ComparisonPredicate comparison = (ComparisonPredicate) predicate;
                 Query.MappedKey mappedKey = mappedKeys.get(comparison.getKey());
 
                 if (field.equals(mappedKey.getField()) &&
                         mappedKey.getSubQueryWithComparison(comparison) == null) {
                     return true;
                 }
             }
         }
 
         return false;
     }
 
     /**
      * Returns an SQL statement that can be used to get a count
      * of all rows matching the query.
      */
     public String countStatement() {
         StringBuilder statementBuilder = new StringBuilder();
         initializeClauses();
 
         statementBuilder.append("SELECT COUNT(");
         if (needsDistinct) {
             statementBuilder.append("DISTINCT ");
         }
 
         statementBuilder.append(recordIdField);
         statementBuilder.append(")\nFROM ");
         vendor.appendIdentifier(statementBuilder, "Record");
         statementBuilder.append(" ");
         statementBuilder.append(aliasPrefix);
         statementBuilder.append("r");
         statementBuilder.append(fromClause.replace(" /*! USE INDEX (k_name_value) */", ""));
         statementBuilder.append(whereClause);
 
         return statementBuilder.toString();
     }
 
     /**
      * Returns an SQL statement that can be used to delete all rows
      * matching the query.
      */
     public String deleteStatement() {
         StringBuilder statementBuilder = new StringBuilder();
         initializeClauses();
 
         statementBuilder.append("DELETE r\nFROM ");
         vendor.appendIdentifier(statementBuilder, "Record");
         statementBuilder.append(" ");
         statementBuilder.append(aliasPrefix);
         statementBuilder.append("r");
         statementBuilder.append(fromClause);
         statementBuilder.append(whereClause);
         statementBuilder.append(havingClause);
         statementBuilder.append(orderByClause);
 
         return statementBuilder.toString();
     }
 
     /**
      * Returns an SQL statement that can be used to get all objects
      * grouped by the values of the given {@code groupFields}.
      */
     public String groupStatement(String[] groupFields) {
         Map<String, Join> groupJoins = new LinkedHashMap<String, Join>();
         if (groupFields != null) {
             for (String groupField : groupFields) {
                 Query.MappedKey mappedKey = query.mapEmbeddedKey(database.getEnvironment(), groupField);
                 mappedKeys.put(groupField, mappedKey);
                 Iterator<ObjectIndex> indexesIterator = mappedKey.getIndexes().iterator();
                 if (indexesIterator.hasNext()) {
                     ObjectIndex selectedIndex = indexesIterator.next();
                     while (indexesIterator.hasNext()) {
                         ObjectIndex index = indexesIterator.next();
                         if (selectedIndex.getFields().size() < index.getFields().size()) {
                             selectedIndex = index;
                         }
                     }
                     selectedIndexes.put(groupField, selectedIndex);
                 }
                 groupJoins.put(groupField, getJoin(groupField));
             }
         }
 
         StringBuilder statementBuilder = new StringBuilder();
         initializeClauses();
 
         statementBuilder.append("SELECT COUNT(");
         if (needsDistinct) {
             statementBuilder.append("DISTINCT ");
         }
         statementBuilder.append(recordIdField);
         statementBuilder.append(")");
         for (Map.Entry<String, Join> entry : groupJoins.entrySet()) {
             statementBuilder.append(", ");
             statementBuilder.append(entry.getValue().getValueField(entry.getKey(), null));
         }
 
         for (String field : orderBySelectColumns) {
             statementBuilder.append(", ");
             statementBuilder.append(field);
         }
 
         statementBuilder.append("\nFROM ");
         vendor.appendIdentifier(statementBuilder, "Record");
         statementBuilder.append(" ");
         statementBuilder.append(aliasPrefix);
         statementBuilder.append("r");
         statementBuilder.append(fromClause.replace(" /*! USE INDEX (k_name_value) */", ""));
         statementBuilder.append(whereClause);
 
         StringBuilder groupBy = new StringBuilder();
         for (Map.Entry<String, Join> entry : groupJoins.entrySet()) {
             groupBy.append(entry.getValue().getValueField(entry.getKey(), null));
             groupBy.append(", ");
         }
 
         if (groupBy.length() > 0) {
             groupBy.setLength(groupBy.length() - 2);
             statementBuilder.append(" GROUP BY ");
             statementBuilder.append(groupBy);
         }
 
         for (String field : orderBySelectColumns) {
             statementBuilder.append(", ");
             statementBuilder.append(field);
         }
 
         if (orderBySelectColumns.size() > 0) {
             statementBuilder.append(" ORDER BY ");
 
             int i = 0;
             for (Map.Entry<String, Join> entry : groupJoins.entrySet()) {
                 if (i++ == 1) {
                     statementBuilder.append(", ");
                 }
                 statementBuilder.append(entry.getValue().getValueField(entry.getKey(), null));
             }
 
             for (String field : orderBySelectColumns) {
                 if (i++ == 1) {
                     statementBuilder.append(", ");
                 }
                 statementBuilder.append(field);
             }
         }
 
         statementBuilder.append(havingClause);
 
         return statementBuilder.toString();
     }
 
     /**
      * Returns an SQL statement that can be used to get when the rows
      * matching the query were last updated.
      */
     public String lastUpdateStatement() {
         StringBuilder statementBuilder = new StringBuilder();
         initializeClauses();
 
         statementBuilder.append("SELECT MAX(r.");
         vendor.appendIdentifier(statementBuilder, "updateDate");
         statementBuilder.append(")\nFROM ");
         vendor.appendIdentifier(statementBuilder, "RecordUpdate");
         statementBuilder.append(" ");
         statementBuilder.append(aliasPrefix);
         statementBuilder.append("r");
         statementBuilder.append(fromClause);
         statementBuilder.append(whereClause);
 
         return statementBuilder.toString();
     }
 
     /**
      * Returns an SQL statement that can be used to list all rows
      * matching the query.
      */
     public String selectStatement() {
         StringBuilder statementBuilder = new StringBuilder();
         initializeClauses();
 
         statementBuilder.append("SELECT");
         if (needsDistinct && vendor.supportsDistinctBlob()) {
             statementBuilder.append(" DISTINCT");
         }
 
         statementBuilder.append(" r.");
         vendor.appendIdentifier(statementBuilder, "id");
         statementBuilder.append(", r.");
         vendor.appendIdentifier(statementBuilder, "typeId");
 
         List<String> fields = query.getFields();
         if (fields == null) {
             if (!needsDistinct || vendor.supportsDistinctBlob()) {
                 statementBuilder.append(", r.");
                 vendor.appendIdentifier(statementBuilder, "data");
             }
         } else if (!fields.isEmpty()) {
             statementBuilder.append(", ");
             vendor.appendSelectFields(statementBuilder, fields);
         }
 
         if (!orderBySelectColumns.isEmpty()) {
             for (String joinValueField : orderBySelectColumns) {
                 statementBuilder.append(", ");
                 statementBuilder.append(joinValueField);
             }
         }
 
         String extraColumns = ObjectUtils.to(String.class, query.getOptions().get(SqlDatabase.EXTRA_COLUMNS_QUERY_OPTION));
         if (extraColumns != null) {
             statementBuilder.append(", ");
             statementBuilder.append(extraColumns);
         }
 
         statementBuilder.append("\nFROM ");
         vendor.appendIdentifier(statementBuilder, "Record");
         statementBuilder.append(" ");
         statementBuilder.append(aliasPrefix);
         statementBuilder.append("r");
         statementBuilder.append(fromClause);
         statementBuilder.append(whereClause);
         statementBuilder.append(havingClause);
         statementBuilder.append(orderByClause);
 
         if (needsDistinct && !vendor.supportsDistinctBlob()) {
             StringBuilder distinctBuilder = new StringBuilder();
 
             distinctBuilder.append("SELECT");
             distinctBuilder.append(" r.");
             vendor.appendIdentifier(distinctBuilder, "id");
             distinctBuilder.append(", r.");
             vendor.appendIdentifier(distinctBuilder, "typeId");
 
             if (fields == null) {
                 distinctBuilder.append(", r.");
                 vendor.appendIdentifier(distinctBuilder, "data");
             } else if (!fields.isEmpty()) {
                 distinctBuilder.append(", ");
                 vendor.appendSelectFields(distinctBuilder, fields);
             }
 
             distinctBuilder.append(" FROM ");
             vendor.appendIdentifier(distinctBuilder, SqlDatabase.RECORD_TABLE);
             distinctBuilder.append(" r INNER JOIN (");
             distinctBuilder.append(statementBuilder.toString());
             distinctBuilder.append(") d0 ON (r.id = d0.id)");
 
             return distinctBuilder.toString();
         }
 
         return statementBuilder.toString();
     }
 
     /** Returns an SQL statement that can be used as a sub-query. */
     public String subQueryStatement() {
         StringBuilder statementBuilder = new StringBuilder();
         initializeClauses();
 
         statementBuilder.append("SELECT");
         if (needsDistinct) {
             statementBuilder.append(" DISTINCT");
         }
         statementBuilder.append(" r.");
         vendor.appendIdentifier(statementBuilder, "id");
         statementBuilder.append("\nFROM ");
         vendor.appendIdentifier(statementBuilder, "Record");
         statementBuilder.append(" ");
         statementBuilder.append(aliasPrefix);
         statementBuilder.append("r");
 
         statementBuilder.append(fromClause);
         statementBuilder.append(whereClause);
         statementBuilder.append(havingClause);
         statementBuilder.append(orderByClause);
 
         return statementBuilder.toString();
     }
 
     private enum JoinType {
 
         INNER("INNER JOIN"),
         LEFT_OUTER("LEFT OUTER JOIN");
 
         public final String token;
 
         private JoinType(String token) {
             this.token = token;
         }
     }
 
     private Join createJoin(String queryKey) {
         Join join = new Join("i" + joins.size(), queryKey);
         joins.add(join);
         if (queryKey.equals(query.getOptions().get(SqlDatabase.MYSQL_INDEX_HINT_QUERY_OPTION))) {
             mysqlIndexHint = join;
         }
         return join;
     }
 
     /** Returns the column alias for the given {@code queryKey}. */
     private Join getJoin(String queryKey) {
         ObjectIndex index = selectedIndexes.get(queryKey);
         for (Join join : joins) {
             if (queryKey.equals(join.queryKey)) {
                 return join;
             } else {
                 String indexKey = mappedKeys.get(queryKey).getIndexKey(index);
                 if (indexKey != null &&
                         indexKey.equals(mappedKeys.get(join.queryKey).getIndexKey(join.index))) {
                     return join;
                 }
             }
         }
         return createJoin(queryKey);
     }
 
     /** Returns the column alias for the given field-based {@code sorter}. */
     private Join getSortFieldJoin(String queryKey) {
         ObjectIndex index = selectedIndexes.get(queryKey);
         for (Join join : joins) {
             if (queryKey.equals(join.queryKey)) {
                 return join;
             } else {
                 String indexKey = mappedKeys.get(queryKey).getIndexKey(index);
                 if (indexKey != null &&
                         indexKey.equals(mappedKeys.get(join.queryKey).getIndexKey(join.index))) {
                     return join;
                 }
             }
         }
 
         Join join = createJoin(queryKey);
         join.type = JoinType.LEFT_OUTER;
         return join;
     }
 
     private class Join {
 
         public Predicate parent;
         public JoinType type = JoinType.INNER;
 
         public final boolean needsIndexTable;
         public final String likeValuePrefix;
         public final String queryKey;
         public final String indexType;
         public final String table;
         public final String idField;
         public final String keyField;
         public final List<String> indexKeys = new ArrayList<String>();
 
         private final String alias;
         private final ObjectIndex index;
         private final SqlIndex sqlIndex;
         private final SqlIndex.Table sqlIndexTable;
         private final String valueField;
 
         public Join(String alias, String queryKey) {
             this.alias = alias;
             this.queryKey = queryKey;
 
             Query.MappedKey mappedKey = mappedKeys.get(queryKey);
             this.index = selectedIndexes.get(queryKey);
 
             this.indexType = mappedKey.getInternalType();
             this.sqlIndex = this.index != null ?
                     SqlIndex.Static.getByIndex(this.index) :
                     SqlIndex.Static.getByType(this.indexType);
 
             if (Query.ID_KEY.equals(queryKey)) {
                 needsIndexTable = false;
                 likeValuePrefix = null;
                 valueField = recordIdField;
                 sqlIndexTable = null;
                 table = null;
                 idField = null;
                 keyField = null;
 
             } else if (Query.TYPE_KEY.equals(queryKey)) {
                 needsIndexTable = false;
                 likeValuePrefix = null;
                 valueField = recordTypeIdField;
                 sqlIndexTable = null;
                 table = null;
                 idField = null;
                 keyField = null;
 
             } else if (Query.ANY_KEY.equals(queryKey)) {
                 throw new UnsupportedIndexException(database, queryKey);
 
             } else if (database.hasInRowIndex() && index.isShortConstant()) {
                 needsIndexTable = false;
                 likeValuePrefix = "%;" + database.getSymbolId(mappedKeys.get(queryKey).getIndexKey(selectedIndexes.get(queryKey))) + "=";
                 valueField = recordInRowIndexField;
                 sqlIndexTable = this.sqlIndex.getReadTable(database, index);
 
                 table = null;
                 idField = null;
                 keyField = null;
 
             } else {
                 needsIndexTable = true;
                 likeValuePrefix = null;
                 addIndexKey(queryKey);
                 valueField = null;
                 sqlIndexTable = this.sqlIndex.getReadTable(database, index);
 
                 StringBuilder tableBuilder = new StringBuilder();
                 vendor.appendIdentifier(tableBuilder, sqlIndexTable.getName(database, index));
                 tableBuilder.append(" ");
                 tableBuilder.append(aliasPrefix);
                 tableBuilder.append(alias);
                 table = tableBuilder.toString();
 
                 idField = aliasedField(alias, sqlIndexTable.getIdField(database, index));
                 keyField = aliasedField(alias, sqlIndexTable.getKeyField(database, index));
             }
         }
 
         public void addIndexKey(String queryKey) {
             String indexKey = mappedKeys.get(queryKey).getIndexKey(selectedIndexes.get(queryKey));
             if (ObjectUtils.isBlank(indexKey)) {
                 throw new UnsupportedIndexException(database, indexKey);
             }
             if (needsIndexTable) {
                 indexKeys.add(indexKey);
             }
         }
 
         public Object quoteIndexKey(String indexKey) {
             return SqlDatabase.quoteValue(sqlIndexTable.convertKey(database, index, indexKey));
         }
 
         public void appendValue(StringBuilder builder, ComparisonPredicate comparison, Object value) {
             ObjectField field = mappedKeys.get(comparison.getKey()).getField();
             SqlIndex fieldSqlIndex = field != null ?
                     SqlIndex.Static.getByType(field.getInternalItemType()) :
                     sqlIndex;
 
             if (fieldSqlIndex == SqlIndex.UUID) {
                 value = ObjectUtils.to(UUID.class, value);
 
             } else if (fieldSqlIndex == SqlIndex.NUMBER
                     && !PredicateParser.STARTS_WITH_OPERATOR.equals(comparison.getOperator())) {
                 if (value != null) {
                     Long valueLong = ObjectUtils.to(Long.class, value);
                     if (valueLong != null) {
                         value = valueLong;
                     } else {
                         value = ObjectUtils.to(Double.class, value);
                     }
                 }
 
             } else if (fieldSqlIndex == SqlIndex.STRING) {
                 if (comparison.isIgnoreCase()) {
                     value = value.toString().toLowerCase(Locale.ENGLISH);
                 } else if (database.comparesIgnoreCase()) {
                     String valueString = value.toString().trim();
                     if (!index.isCaseSensitive()) {
                         valueString = valueString.toLowerCase(Locale.ENGLISH);
                     }
                     value = valueString;
                 }
             }
 
             vendor.appendValue(builder, value);
         }
 
         public String getValueField(String queryKey, ComparisonPredicate comparison) {
             String field;
 
             if (valueField != null) {
                 field = valueField;
 
             } else if (sqlIndex != SqlIndex.CUSTOM) {
                 field = aliasedField(alias, sqlIndexTable.getValueField(database, index, 0));
 
             } else {
                 String valueFieldName = mappedKeys.get(queryKey).getField().getInternalName();
                 List<String> fieldNames = index.getFields();
                 int fieldIndex = 0;
                 for (int size = fieldNames.size(); fieldIndex < size; ++ fieldIndex) {
                     if (valueFieldName.equals(fieldNames.get(fieldIndex))) {
                         break;
                     }
                 }
                 field = aliasedField(alias, sqlIndexTable.getValueField(database, index, fieldIndex));
             }
 
             if (comparison != null && comparison.isIgnoreCase()) {
                 field = "LOWER(CONVERT(" + field + " USING utf8))";
             }
 
             return field;
         }
     }
 }
