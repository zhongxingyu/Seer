 package org.nsdl.mptstore.query;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.nsdl.mptstore.core.TableManager;
 
 /** Translates a {@link GraphQuery} into a series of SQL statements
  * <p>
 * Produces ANSI SQL-92 queries by converting each {@link GaphPattern} leaf
  * of the query tree into a series of JOINs.   Each join condition is formed by 
 * matching variables between {@link TriplePatterns} in the appripriate 
  * GraphPatterns.
  * </p>
  * <p>
  * TODO:
  * <ul>
  *  <li> Handle nested subqueries (currently is set to barf if a required or
  * optional compomnent is not a GraphPattern).  Things are set up for doing
  * so, just haven't had the time to do it</li>
  *  <li> Deal with data typing when applying filters </li>
  *  <li> Implement a strategy for dealing with unbound predicates </li>
  *  <li> Testing, especially with escaped/encoded characters and 
  *  manlformed queries </li> 
  *  <li> Clean this up a bit and simplify, if possible </li>
  *  <li> Move the many inner classes to the outside </li>
  * </ul>
  * </p>
  * @author birkland
  *
  */
 public class GraphQuerySQLProvider implements SQLBuilder, SQLProvider {
     private final GraphQuery query;
     
     private final MappingManager manager;
 	private List<String> targets;
     
     private HashMap<String, Set<String>> valueBindings    = new HashMap<String, Set<String>>();
      
 
     public GraphQuerySQLProvider(TableManager adaptor, GraphQuery query) {
         
         this.manager = new MappingManager(adaptor);
         this.query = query;
     }
     
     public void setTargets(List<String> targets) {
         this.targets = new ArrayList<String>(targets);
     }
 
     public List<String> getSQL() throws QueryException {
         
         HashMap<String, String> requiredBindings = new HashMap<String,String>();
         HashMap<String, String> allBindings = new HashMap<String, String>();
         JoinSequence joinSeq = null;
         
         /* Process required elements first */
 		for (QueryElement e : query.getRequired()) {
             
             /* Disallow subqueries for the time being */
             if (e.getType().equals(QueryElement.Type.GraphQuery)) {
                 throw new QueryException("Currently, we do not support subqueries");
             } else if (!e.getType().equals(QueryElement.Type.GraphPattern)) {
                 /* Currently, this will never happen */
                 throw new QueryException("Unknown query element type " + e.getType());
             }
             
             if (joinSeq == null) {
                 joinSeq = new JoinSequence(parseGraphPattern((GraphPattern) e, requiredBindings));
             } else {
                 joinSeq.addJoin(JoinType.innerJoin, parseGraphPattern((GraphPattern) e,requiredBindings), requiredBindings);
             }
         }
         
 		allBindings.putAll(requiredBindings);
         
         for (QueryElement e : query.getOptional()) {
             
             HashMap<String, String> optionalBindings = new HashMap<String, String>(requiredBindings);
             
             /* Disallow subqueries for the time being */
             if (e.getType().equals(QueryElement.Type.GraphQuery)) {
                 throw new QueryException("Currently, we do not support subqueries");
             } else if (!e.getType().equals(QueryElement.Type.GraphPattern)) {
                 /* Currently, this will never happen */
                 throw new QueryException("Unknown query element type " + e.getType());
             }
             
             joinSeq.addJoin(JoinType.leftOuterJoin, 
                     parseGraphPattern((GraphPattern) e, optionalBindings), requiredBindings);
             
             addNewMappings(optionalBindings, allBindings);
             
         }
 	    
         StringBuilder sql = new StringBuilder();
         
         sql.append("SELECT " + generateTargets(allBindings) + " FROM " + joinSeq);
         
         /* 
          * If there are any values or constraints that remain to be added to the query,
          * add them in a WHERE clause.  NB: They better not be from an optional clause:
          * It would probably be wise to either check here, or prove that an exception would
          * have been thrown already
          */
         if (valueBindings.size() > 0) {
             sql.append(" WHERE ");
             ArrayList<String> valueKeys = new ArrayList<String>(valueBindings.keySet());
             for (int i = 0; i < valueKeys.size(); i++) {
                 ArrayList<String> values = new ArrayList<String>(valueBindings.get(valueKeys.get(i)));
                 for (int j = 0; j < values.size(); j++) {
                     if (i > 0 || j > 0) {
                         sql.append(" AND ");
                     }
                     sql.append(values.get(j));
                 }
             }
         }
         
         ArrayList<String> sqlList = new ArrayList<String>();
         sqlList.add(sql.toString());
         return sqlList;
     }
 
     public List<String> getTargets() {
         return new ArrayList<String>(targets);
     }
     
     private Joinable parseGraphPattern(GraphPattern g, HashMap<String, String> variableBindings) throws QueryException {
         
         /* First, organize the filters by variable so that we can map them */
         HashMap<String, Set<TripleFilter>> filters = new HashMap<String, Set<TripleFilter>>();
         for (TripleFilter f : g.getFilters()) {
             if (f.s.isVariable()) {
                 if (!filters.containsKey(f.s.value())) {
                     filters.put(f.s.value(), new HashSet<TripleFilter>());
                 }
                 filters.get(f.s.value()).add(f);
             } 
             if (f.o.isVariable()) {
                if (!filters.containsKey(f.o.value())) {
                    
                     filters.put(f.o.value(), new HashSet<TripleFilter>());
                 }
                filters.get(f.o.value()).add(f);
             } 
             
             if (!(f.s.isVariable() || f.o.isVariable())) {
                 throw new IllegalArgumentException("Triple filters must contain a variable.  Neither " + 
                         f.s.value() + " nor " + f.o.value() + " is a variable!");
             }
         }
         
         
         /* Next, process each triple pattern in this graph pattern */
         LinkedList<TriplePattern> steps = new LinkedList<TriplePattern>(g.getTriplePatterns());
         TriplePattern step = steps.removeFirst();
         
         bindPattern(step, variableBindings);
         JoinSequence joins = new JoinSequence(new JoinTable(step));
         
         Set<TriplePatternNode>joinableVars = joins.joinVars();
         
         while (!steps.isEmpty()) {
             step = getJoinablePattern(steps, variableBindings);
             if (step == null) {
                 throw new QueryException("Cannot bind all query steps!");
             }
             steps.remove(step);
             
             bindPattern(step, variableBindings);
             JoinTable table = new JoinTable(step);
             
             joinableVars.addAll(table.joinVars());
             JoinConditions conditions = new JoinConditions();
             
             for (TriplePatternNode p : step.getNodes()) {
                 if (isBound(p, variableBindings)) {
                     /* Join this variable's column with the corresponding bound column */
                     
                     if (!p.mappedName().equals(getBoundValue(p, variableBindings))) {
                         conditions.addCondition(p.mappedName(), "=", getBoundValue(p, variableBindings));
                     }
                 }
             }
             
             /* Add any filter constraints */
             for (String filterVar : filters.keySet()) {
                 for (TriplePatternNode joinableVar : joinableVars) {
                     if (joinableVar.value().equals(filterVar)) {
                         for (TripleFilter f : filters.get(filterVar)) {
                             String right;
                             String left;
                             
                             if (f.s.isVariable() && f.s.value().equals(filterVar)) {
                                 left = getBoundValue(joinableVar, variableBindings);
                             } else if (f.s.isVariable()) {
                                 left = getBoundValue(f.s, variableBindings);
                             } else {
                                 left = "'" + f.s.value() + "'";
                             }
                             
                             if (f.o.isVariable() && f.o.value().equals(filterVar)) {
                                 right = getBoundValue(joinableVar, variableBindings);
                             } else if (f.o.isVariable()) {
                                 right = getBoundValue(f.o, variableBindings);
                             } else {
                                 right = "'" + f.o.value() + "'";
                             }
                             
                             conditions.addCondition(left, f.operator, right);
                         }
                         
                         removeFromMap(filters.get(filterVar), filters);
                     }
                 }
             }
             
             /* Fold in any remaining constant bindings */
             for (TriplePatternNode var : joinableVars) {
                 if (valueBindings.containsKey(var.boundTable().alias())) {
                     
                     for (String condition : valueBindings.get(var.boundTable().alias())) {
                         conditions.addCondition(condition);
                     }
                     valueBindings.remove(var.boundTable().alias());
                 }
             }
             
             /* Finally, add the join to the sequence */
             joins.addJoin(JoinType.innerJoin, table, conditions);
         }
         
         /* We weren't able to add filters at this stage.. This is legitimate only if 
          * this pattern has a length of 1 AND it contains a variable that matches the filter
          */
         if (filters.values().size() > 0 && g.getTriplePatterns().size() > 1) {
             throw new QueryException("Filter is unbound");
         }
         
         TriplePattern p = g.getTriplePatterns().get(0);
 ;
         for (String varName : filters.keySet()) {
 
             for (TripleFilter f : filters.get(varName)) {
                 String mappedName;
                 if (p.subject.value().equals(varName)) {
                     mappedName = p.subject.mappedName();
                 } else if (p.object.value().equals(varName)) {
                     mappedName = p.object.mappedName();
                 } else {
                     throw new QueryException("Variable " + varName + " in filter Cannot be found in graph query");
                 }
                 
                 if (!valueBindings.containsKey(mappedName)) {
                     valueBindings.put(mappedName, new HashSet<String>());
                 }
                 
                 if (f.s.value().equals(varName)) {
                     valueBindings.get(mappedName).add(mappedName + " " + f.operator + " '" + f.o.value() + "'");
                 } else if (f.o.value().equals(varName) ){
                     valueBindings.get(mappedName).add("'" + f.s.value() + "' " + f.operator + " " + mappedName);
                 } 
             }
         }
         return joins;
     }
     
     private TriplePattern getJoinablePattern(List<TriplePattern> l, HashMap<String, String> variableBinsings) {
         for (TriplePattern p : l) {
             if (isBound(p.subject, variableBinsings) || isBound(p.object, variableBinsings)) {
                 return p;
             }
         }
         return null;
     }
     
     /*
      * Determine if a variable has been apped to a literal or 
      * specific column of a table
      */
     private boolean isBound(TriplePatternNode n, HashMap<String, String> variableBindings) {
         return variableBindings.containsKey(n.value());
     }
     
     private String getBoundValue(TriplePatternNode n, HashMap<String, String> variableBindings) {
         return variableBindings.get(n.value());
     }
     
     /*
      * Bind the variables/values of a triple pattern by:
      * - Placing any new variables into the master bings map,
      * - Placing any literal values into the literals map
      */
     private void bindPattern(TriplePattern t, HashMap<String, String>variableBindings) {
         t.bindTo(manager.mapPredicateTable(t.predicate));
         for (TriplePatternNode p : t.getNodes()) {
             bindNode(p, variableBindings);
         }
     }
     
     private void bindNode(TriplePatternNode p, HashMap<String, String>variableBindings) {
         if (p.isVariable()) {
             if (! variableBindings.containsKey(p.value())) {
                 variableBindings.put(p.value(), p.mappedName());
             }
         } else {
             if (!valueBindings.containsKey(p.boundTable().alias())) {
                 valueBindings.put(p.boundTable().alias(), new HashSet<String>());
             }
             valueBindings.get(p.boundTable().alias()).add(p.mappedName() + " = " + "'" + p.value() + "'");
         }
     }
     
     
     /** Removes a mapped value (and all associated keys) from a map
      *
      * @param value mapped value to remove
      * @param m map to remove the value from
      */
     private <K, V> void removeFromMap(V value, Map<K,V> m) {
         for (Map.Entry<K, V> e : m.entrySet()) {
             if (e.getValue().equals(value)) {
                 m.remove(e.getKey());
             }
         }
     }
     
     private String generateTargets(HashMap<String, String> variableBindings) {
         String selects = "";
         for (int i = 0; i < targets.size(); i++) {
             selects += variableBindings.get(targets.get(i));
             if (i < targets.size() - 1) {selects += ", ";}
         }
         return selects;
     }
     
     private <K, V> void addNewMappings(Map<K,V> from, Map<K,V> to) {
         for (K key : from.keySet()) {
             if (!to.containsKey(key)) {
                 to.put(key, from.get(key));
             }
         }
     }
     
     private interface Joinable {
         public Set<TriplePatternNode> joinVars();
         public String alias();
         public String declaration();
     }
     
     private class JoinTable implements Joinable{
         private final TriplePattern t;
         public JoinTable(TriplePattern t) {
             this.t = t;
         }
         
         public Set<TriplePatternNode> joinVars() {
             HashSet<TriplePatternNode> s = new HashSet<TriplePatternNode>();
             if (t.subject.isVariable()) {
                 s.add(t.subject);
             }
             
             if (t.object.isVariable()) {
                 s.add(t.object);
             }
             
             return s;
         }
         
         public String alias() {
             return t.subject.boundTable().alias();
         }
         
         public String declaration() {
             String alias = t.subject.boundTable().alias();
             String name = t.subject.boundTable().name();
             if (name.equals(alias)) {
                 return name;
             } else {
                 return (name + " AS " + alias);
             }
         }
     }
     
     private class JoinSequence implements Joinable {
         private final StringBuilder join;
         private int joinCount = 0;
         
         private final List<Joinable> joined = new ArrayList<Joinable>();
         public JoinSequence(Joinable start) {
             this.join = new StringBuilder(start.declaration());
             joined.add(start);
             joinCount = 1;
         }
         
         public void addJoin(String joinType, Joinable j, String joinConstraints) {
             join.append(" " + joinType + " " + j.declaration());
             joined.add(j);
             if (joinConstraints != null && joinConstraints != "") {
                 join.append(" ON (" + joinConstraints + ")");
             }
             joinCount++;
         }
         
         public void addJoin(String joinType, Joinable j, JoinConditions conditions) {
             addJoin(joinType, j, conditions.toString());
         }
         
         public void addJoin(String joinType, Joinable j, HashMap<String, String> variableBindings) {
             
             JoinConditions conditions = new JoinConditions();
             
             
             for (TriplePatternNode existingVar : this.joinVars()) {
                 for (TriplePatternNode candidateVar : j.joinVars()) {
                     if (existingVar.value().equals(candidateVar.value()) 
                             && variableBindings.get(candidateVar.value()).equals(existingVar.mappedName())) {
                         conditions.addCondition(existingVar.mappedName(), "=", candidateVar.mappedName());
                     }
                 }
             }
             
             addJoin(joinType, j, conditions);
         }
         
         public Set<TriplePatternNode> joinVars() {
             HashSet<TriplePatternNode> joinVars = new HashSet<TriplePatternNode>();
             
             for (Joinable join : joined) {
                 joinVars.addAll(join.joinVars());
             }
             
             return joinVars;
         }
         
         public String alias() {
             if (joinCount == 1) return join.toString();
             else return "(" + join.toString() + ")";
         }
         
         public String declaration() {
             return alias();
         }
         
         public String toString() {
             return join.toString();
         }
     }
     
     private class JoinConditions {
         private StringBuilder joinClause = new StringBuilder();
         
         public void addCondition(String leftOperand, String operator, String rightOperand ) {
             addCondition(leftOperand + " " + operator + " " + rightOperand);
         }
         
         public void addCondition(String condition) {
             if (joinClause.length() == 0) {
                 joinClause.append(condition);
             } else {
                 joinClause.append(" AND " + condition);
             }
         }
         
         public String toString () {
             return joinClause.toString();
         }
     }
     private class JoinType {
         public static final String leftOuterJoin = "LEFT OUTER JOIN";
         public static final String innerJoin = "JOIN";
     }
     
     private class MappingManager {
         private HashMap<String, List<String>> predicateMap 
                 = new HashMap<String, List<String>>();
         private TableManager adaptor;
         
         public MappingManager(TableManager adaptor) {
             this.adaptor = adaptor;
         }
         public MPTable mapPredicateTable(TriplePatternNode predicate) {
             
             String tableName = adaptor.getTableFor(predicate.value());
             String alias;
             if (predicateMap.containsKey(predicate.value())) {
                 List<String> aliases = predicateMap.get(predicate.value());
                 alias = tableName + "_" + aliases.size();
                 aliases.add(alias);
             } else {
                 ArrayList<String> aliases = new ArrayList<String>();
                 aliases.add(tableName);
                 predicateMap.put(predicate.value(), aliases);
                 alias = tableName;
             }
             
             MPTable table = new MPTable (tableName, alias);
             return table;
         }
     }
 }
