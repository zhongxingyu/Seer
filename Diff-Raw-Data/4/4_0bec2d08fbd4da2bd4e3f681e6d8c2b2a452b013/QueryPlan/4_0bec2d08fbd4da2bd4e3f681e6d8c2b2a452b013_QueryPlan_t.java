 /*****************************************************************************
  * Copyright (C) 2008 EnterpriseDB Corporation.
  * Copyright (C) 2011 Stado Global Development Group.
  *
  * This file is part of Stado.
  *
  * Stado is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * Stado is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with Stado.  If not, see <http://www.gnu.org/licenses/>.
  *
  * You can find Stado at http://www.stado.us
  *
  ****************************************************************************/
 /*
  * QueryPlan.java
  *
  */
 package org.postgresql.stado.planner;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Types;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.atomic.AtomicInteger;
 
 import org.postgresql.stado.common.ColumnMetaData;
 import org.postgresql.stado.common.util.ParseCmdLine;
 import org.postgresql.stado.common.util.Props;
 import org.postgresql.stado.common.util.XLogger;
 import org.postgresql.stado.engine.Engine;
 import org.postgresql.stado.engine.XDBSessionContext;
 import org.postgresql.stado.exception.ErrorMessageRepository;
 import org.postgresql.stado.exception.XDBServerException;
 import org.postgresql.stado.metadata.DBNode;
 import org.postgresql.stado.metadata.SyncCreateTable;
 import org.postgresql.stado.metadata.SysColumn;
 import org.postgresql.stado.metadata.SysDatabase;
 import org.postgresql.stado.metadata.SysTable;
 import org.postgresql.stado.misc.SortedVector;
 import org.postgresql.stado.misc.combinedresultset.SortCriteria;
 import org.postgresql.stado.optimizer.AttributeColumn;
 import org.postgresql.stado.optimizer.FromRelation;
 import org.postgresql.stado.optimizer.OrderByElement;
 import org.postgresql.stado.optimizer.QueryCondition;
 import org.postgresql.stado.optimizer.QueryNode;
 import org.postgresql.stado.optimizer.QueryTree;
 import org.postgresql.stado.optimizer.RelationNode;
 import org.postgresql.stado.optimizer.SqlExpression;
 import org.postgresql.stado.parser.ExpressionType;
 import org.postgresql.stado.parser.handler.IFunctionID;
 import org.postgresql.stado.parser.handler.IdentifierHandler;
 
 
 /**
  * QueryPlan takes a QueryTree and converts it into a sequence of steps
  * that make up the plan. It is later refined into an ExecutionPlan
  */
 public class QueryPlan
 {
     private static final XLogger logger = XLogger.getLogger(QueryPlan.class);
 
     public static final XLogger CATEGORY_QUERYFLOW = XLogger.getLogger("queryflow");
 
     /** session info */
     private final XDBSessionContext client;
 
     /** Contains the steps to execute in the plan; leaves is a bit of a misnomer. */
     private final List<Leaf> leaves;
 
     /** used for iterating through leaves */
     private int iLeafNumber;
 
     /** Regular Plan */
     public static final int NORMAL = 0;
 
     /** Relation Plan */
     public static final int RELATION = 1;
 
     /** Scalar plan */
     public static final int SCALAR = 2;
 
     /** Uncorrelated plan */
     public static final int NONCORRELATED = 3;
 
     /** Correlated plan */
     public static final int CORRELATED = 4;
 
     /** Whether or not this is a UNION plan */
     protected boolean isUnion = false;
 
     /**
      * The type of QueryPlan this represents: NORMAL, RELATION, SCALAR,
      * NONCORRELATED, CORRELATED.
      */
     public int planType = NORMAL;
 
     // These are used if this is a subplan part of a union
     public static final int UNIONTYPE_NONE = 0;
 
     public static final int UNIONTYPE_UNION = 1;
 
     public static final int UNIONTYPE_UNIONALL = 2;
 
     /**
      * Describes the type of union: UNIONTYPE_NONE, UNIONTYPE_UNION,
      * UNIONTYPE_UNIONALL
      */
     public int unionType = UNIONTYPE_NONE;
 
     /**
      * Determines whether or not DISTINCT should be used for the query.
      */
     public boolean isDistinct = false;
 
     private static final AtomicInteger QUERY_ID = new AtomicInteger();
 
     /**
      * Specifies the id for the query. This is used to generate unique temp
      * table names.
      */
     private final int queryId;
 
     /** for generating temp table names */
     public int tempIdCounter = 0;
 
     /**
      * Tracks the current step number in the plan.
      */
     public int currentLeafStepNo = 0;
 
     /** A counter used for generating distinct column names  */
     private int colGenCount = 0;
 
     /** Used for aggregate expression substitution */
     private final Map<SqlExpression,String> colMappings;
 
     /** Final resulting table from last step. Needed for subqueries.  */
     public String finalTableName;
 
     /**
      * Final order by clause. We have to do this at the very end when getting
      * the results to ensure sorting (the ResultSet combiner will also
      * merge sort).
      */
     public String orderByClause = "";
 
     /** Indicaates which corresponding position of an order by element is in the
      * the projection list
      */
     private List<Integer> orderByProjPos;
 
     /**
      * Used for merge sorting results from the nodes
      */
     public List<SortCriteria> sortInfo;
 
     /** For counting place holders in scalar subqueries. */
     public int placeHolderNo = 0;
 
     /**
      * If this Plan represents a scalar subquery, then scalarLeaf will point to
      * the Leaf in the parent plan that depends on this value.
      */
     public Leaf scalarLeaf;
 
     /**
      * List of subquery plans for this plan.
      */
     public List<QueryPlan> subplanList;
 
     /**
      * List of plans that are to be UNIONed
      */
     public List<QueryPlan> unionSubplanList;
 
     /**
      * Tracks the last temp table we joined with
      */
     private String lastJoinTableName = "";
 
     /**
      * Flags when we are starting to process the correlated subtable that we
      * just passed in
      */
     private boolean correlatedStart = false;
 
     /**
      * Assigned to the "placeholder" node in the correlated subquery
      */
     private QueryNode correlatedNode = null;
 
     /**
      * What the current QueryNode level status is 1 = same outer level as other
      * join node 2 = different out level 3 = both-- due to multiple join
      * conditions
      */
     private int levelStatus;
 
     /**
      * Used for OUTER joins when we join when we have multiple join conditions
      * on multiple levels with a QueryNode. Not very elegant putting it here,
      * but avoided passing it all around.
      */
     private Leaf aLeaf2;
 
     /**
      * Keeps track of where a specified temp table was last used. This is
      * helpful for tracking when to drop temp tables
      */
     private Map<String, Leaf> tempTableLastUsedAt;
 
     /**   */
     private SysDatabase database;
 
     /**
      * This contains all the querynodes used- across all steps
      */
     public Map<Integer,DBNode> queryNodeTable;
 
     /**
      * Final projection string
      */
     public String finalProjString = "";
 
     /**
      * Due to parallelization, we create an extra step for aggregation. We use
      * this to build up the extra step's group by
      */
     private final List<Leaf.Projection> extraStepGroupByList;
 
     /**
      * Determines whether or not this plan is the top level plan.
      */
     public boolean isTopLevelPlan = false;
 
     /**
      * Indicates whether this plan is a main union part whose parent is the top
      * level plan. This is used to stream results properly.
      */
     public boolean isFinalUnionPart = false;
 
     /**
      * Select clause added to final projection string for support of order by
      * with hidden values (those not appearing in select).
      */
     public String addedFinalProjections = "";
 
     /**
      * if we have nested plans due to subqueries, this will allow us to access
      * top one.
      */
     protected QueryPlan topQueryPlan;
 
     /**
      * Due to projections, we may need to calculate things in a slower manner.
      * This is not used anymore, but keeping just in case we add it back.
      */
     private boolean useSlowAggregation = false;
 
     //private List<String> correlatedFinalTables = new ArrayList<String>();
 
     /**
      * Handle cases when we have expressions that must be made into a final
      * extra step group by
      */
     private List<Leaf.Projection> projectedGroupBy = new ArrayList<Leaf.Projection>();
 
     /**
      * We normally will later try and only use the nodes required for
      * MultinodeExecutor. This indicates we have some special processing that
      * will require all available nodes.
      */
     private boolean useAllNodes = false;
 
     /**
      * Top-level tree's projection list descriptions to build up
      * ResultSetMetaData object
      */
     private ColumnMetaData[] columnMetaData = null;
 
     /** support for LIMIT clause */
     private long limit = -1;
 
     /** support for OFFSET clause */
     private long offset = -1;
 
     /** support for INTO clause */
     private SysTable intoTable = null;
     private SyncCreateTable syncCreateTable = null;
     private String intoTableRefName = null;
 
     // column counter for distinguishing outer node id columns
     private int outerCounter = 0;
 
     private boolean isExistingInto = false;
 
     /**
      * Constructor for QueryPlan.
      *
      * @param client
      */
     public QueryPlan(XDBSessionContext client) {
         this.client = client;
         database = client.getSysDatabase();
 
         queryId = QUERY_ID.incrementAndGet();
 
         leaves = new ArrayList<Leaf>();
         colMappings = new HashMap<SqlExpression,String>();
         subplanList = new ArrayList<QueryPlan>();
         unionSubplanList = new ArrayList<QueryPlan>();
         tempTableLastUsedAt = new HashMap<String,Leaf>(10);
         queryNodeTable = new HashMap<Integer,DBNode>(10);
 
         extraStepGroupByList = new ArrayList<Leaf.Projection>();
 
         topQueryPlan = this;
     }
 
     /**
      * Adds leaf to the list of steps that the plan should execute
      *
      * @param aLeaf
      *            The Leaf to add to the QueryPlan
      */
     private void addLeaf(Leaf aLeaf) {
         final String method = "addLeaf";
         logger.entering(method);
 
         try {
             leaves.add(aLeaf);
         } finally {
             logger.exiting(method);
         }
     }
 
     /**
      * Outputs debugging info
      *
      * @return String
      */
     @Override
     public String toString() {
         // String sSQL;
         Leaf aLeaf;
         StringBuffer sbPlan = new StringBuffer(512);
 
         for (QueryPlan aQueryPlan : unionSubplanList) {
             sbPlan.append("\n UNION subplan\n");
             sbPlan.append(" -------\n");
 
             sbPlan.append(aQueryPlan);
             sbPlan.append(" end UNION subplan\n");
             sbPlan.append(" -----------\n");
         }
 
         for (QueryPlan aQueryPlan : subplanList) {
             sbPlan.append("\n subplan\n");
             sbPlan.append(" -------\n");
 
             sbPlan.append(aQueryPlan);
             sbPlan.append(" end subplan\n");
             sbPlan.append(" -----------\n");
         }
 
 
         for (int i = 0; i < leaves.size(); i++) {
             aLeaf = leaves.get(i);
 
             if (aLeaf.subplan != null) {
                 sbPlan.append('\n');
                 sbPlan.append(" Correlated subplan\n");
                 sbPlan.append(" -------\n");
                 sbPlan.append(aLeaf.subplan);
                 sbPlan.append('\n');
             }
 
             for (QueryPlan uncorPlan : aLeaf.uncorrelatedSubplanList) {
 
                 sbPlan.append('\n');
                 sbPlan.append(" Uncorrelated subplan\n");
                 sbPlan.append(" -------\n");
                 sbPlan.append(uncorPlan);
                 sbPlan.append('\n');
             }
 
             if (aLeaf.outerSubplan != null) {
                 sbPlan.append('\n');
                 sbPlan.append(" Extra Outer Step\n");
                 sbPlan.append(" ----------------\n");
                 sbPlan.append(aLeaf.outerSubplan);
                 sbPlan.append('\n');
             }
 
             sbPlan.append("\n Step: " + i + "\n");
             sbPlan.append(" -------\n");
             if (isTopLevelPlan && i < leaves.size() - 1) {
             	// Do not print target for final step - temp table is not created
             	sbPlan.append(" Target: " + aLeaf.getTempTargetCreateStmt() + "\n");
             }
             sbPlan.append(" Select: " + aLeaf.getSelect() + "\n");
 
             for (int j = 0; j < aLeaf.tempTableDropList.size(); j++) {
                 if (j == 0) {
                     sbPlan.append("  Drop:\n");
                 }
                 sbPlan.append(' ')
                         .append(aLeaf.tempTableDropList.get(j))
                         .append('\n');
             }
             sbPlan.append('\n');
         }
 
         return sbPlan.toString();
     }
 
     /**
      * Initializes iterator of leaves
      */
     public void initLeafIteration() {
         iLeafNumber = 0;
     }
 
     /**
      * Returns next leaf in iterator
      *
      * @return the next leaf
      */
     // -----------------------------------------------------------------
     public Leaf nextLeaf() {
         final String method = "nextLeaf";
         logger.entering(method);
 
         try {
             Leaf currentLeaf;
 
             if (iLeafNumber >= leaves.size()) {
                 return null;
             }
 
             currentLeaf = leaves.get(iLeafNumber);
 
             iLeafNumber++;
 
             return currentLeaf;
         } finally {
             logger.exiting(method);
         }
     }
 
     /**
      * Builds up the SELECT statement that needs to be run at each step.
      *
      * @param aQueryTree
      */
     private void assignSteps(QueryTree aQueryTree) {
         final String method = "assignSteps";
         logger.entering(method);
 
         try {
             for (Leaf currentLeaf : leaves) {
                 if (currentLeaf.getSelectStatement() == null) {
                     if (aQueryTree.isCorrelatedSubtree()) {
                         currentLeaf.setOffset(aQueryTree.getOffset());
                         currentLeaf.setLimit(aQueryTree.getLimit());
                     }
                     currentLeaf.determineSelectStatement(this);
                 }
             }
 
             // On the last step we may need to handle limit and offset
             // in subqueries
             if (leaves != null && !leaves.isEmpty()) {
                 Leaf currentLeaf = leaves.get(leaves.size() - 1);
                 if (currentLeaf.getSelectStatement() == null) {
                     currentLeaf.setOffset(aQueryTree.getOffset());
                     currentLeaf.setLimit(aQueryTree.getLimit());
                     currentLeaf.determineSelectStatement(this);
                 }
             }
         } finally {
             logger.exiting(method);
         }
     }
 
 
     /**
      *
      * @param aQueryTree
      */
     public void determineOrderByClause(QueryTree aQueryTree) {
         final String method = "determineOrderByClause";
         logger.entering(method);
 
         try {
             String orderString;
 
             // track order by not in select clause
             // these will be processed sequentially
             int hiddenCount = 0;
 
             // build up order by
             orderByClause = "";
 
             if (aQueryTree.getOrderByList().size() > 0) {
                 sortInfo = new ArrayList<SortCriteria>();
             }
 
             // For the final order by clause, there is no need to include the
             // table name. Just use the name or alias.
             // Leaving the table in won't work, since we now include the
             // original name.
             for (int i = 0; i < aQueryTree.getOrderByList().size(); i++) {
                 OrderByElement anOBElement = aQueryTree.getOrderByList().get(i);
 
                 SqlExpression aSqlExpression = anOBElement.orderExpression;
 
                 if (aSqlExpression.getAlias().length() > 0) {
                     orderString = IdentifierHandler.quote(aSqlExpression.getAlias());
                 } else {
                     // For outer handling, we may add a union.
                     // Make sure table name is stripped.
                     if (aQueryTree.getParentQueryTree() != null) {
                         orderString = aSqlExpression.getExprString().replaceAll(
                                 "\".*\"\\.", "");
                     } else {
                         orderString = aSqlExpression.getExprString();
                     }
                 }
                 String baseOrderString = new String(orderString);
                 orderString += " " + anOBElement.getDirectionString();
 
                 if (orderByClause.length() > 0) {
                     orderByClause += ", ";
                 }
 
                 this.orderByClause += orderString;
 
                 boolean includeInResult = true;
                 int direction;
 
                 if (anOBElement.orderDirection == OrderByElement.ASC) {
                     direction = SortCriteria.ASCENDING;
                 } else {
                     direction = SortCriteria.DESCENDING;
                 }
 
                   if (anOBElement.orderExpression.isAdded()) {
                     includeInResult = false;
                     addedFinalProjections += ", "
                             + anOBElement.orderExpression.rebuildString();
 
                     if (anOBElement.orderExpression.getAlias().length() > 0) {
                         addedFinalProjections += " as "
                                 + IdentifierHandler.quote(anOBElement.orderExpression.getAlias());
                     }
                 }
 
                 int colPos = 0;
 
                 // We take advantage of the fact that the parser
                 // should try and reuse the same SqlExpression that appears in
                 // the select clause in the order by clause, as well as how we
                 // try and match them up as well
                 int pos = 1;
 
                 for (SqlExpression aSE : aQueryTree.getProjectionList()) {
                     if (aSE == anOBElement.orderExpression) {
                         colPos = pos;
                     }
                     pos++;
                 }
 
                 // If we could not match them up
                 // try and find expression in final proj list
                 if (colPos == 0) {
 
                     ArrayList projList = new ArrayList();
                     int parenDepth = 0;
                     boolean openQuote = false;
                     StringBuffer singleProj = new StringBuffer();
 
                     for (int j = 0; j < finalProjString.length(); j++) {
                         char currChar = finalProjString.charAt(j);
 
                         if (currChar == ',' && parenDepth == 0 && !openQuote) {
                             projList.add(singleProj.toString());
                             singleProj.setLength(0);
                             continue;
                         }
 
                         if (currChar == '(') {
                             parenDepth++;
                         }
                         if (currChar == ')') {
                             parenDepth--;
                         }
 
                         if (currChar == '\'') {
                             openQuote = !openQuote;
                         }
 
                         singleProj.append(currChar);
                     }
 
                     if (singleProj.length() > 0) {
                         projList.add(singleProj.toString());
                     }
 
                     String[] proj = (String[]) projList.toArray(new String[projList.size()]);
                     String oeString = baseOrderString; //anOBElement.orderExpression.getExprString();
 
                     int startPos;
 
                     // check temp table prefix
                     if (Props.XDB_TEMPTABLEPREFIX.length() > 0
                             && oeString.indexOf(Props.XDB_TEMPTABLEPREFIX) == 0) {
                         startPos = Props.XDB_TEMPTABLEPREFIX.length() + 1;
                         oeString = oeString.substring(startPos);
                     }
 
                     startPos = oeString.indexOf('.') + 1;
 
                     if (startPos > 0) {
                         oeString = oeString.substring(startPos);
                     }
 
                     for (int j = 0; j < proj.length; j++) {
                         int aliasPos = proj[j].indexOf(" as ");
                         String projAlias;
                         if (aliasPos > 0) {
                             aliasPos += 4;
                             projAlias = proj[j].substring(aliasPos);
                         } else {
                             projAlias = proj[j];
                         }
 
                         if (projAlias.compareToIgnoreCase(anOBElement.orderExpression.getAlias()) == 0) {
                             colPos = j + 1;
                             break;
                         } else {
                             if (projAlias.compareToIgnoreCase(oeString) == 0)
 
                             {
                                 colPos = j + 1;
                                 break;
                             }
                         }
                     }
 
                     // See if we did not find it. It must be a hidden column
                     // then.
                     // We add it here, but note that we need to note it is an
                     // extra select expression.
                     //
                     // Note that addedFinalProjections are needed because
                     // we may combine results from multiple nodes and we need
                     // to be able to merge and sort them. When the final
                     // results are returned, this information is left out.
                     if (colPos == 0) {
                         colPos = ++hiddenCount + proj.length;
                         includeInResult = false;
 
                         addedFinalProjections += ", "
                                 + anOBElement.orderExpression.getExprString();
 
                         if (anOBElement.orderExpression.getAlias().length() > 0) {
                             addedFinalProjections += " as "
                                     + IdentifierHandler.quote(anOBElement.orderExpression.getAlias());
                         }
                     }
                 }
 
                 SortCriteria aSortCriteria = new SortCriteria(colPos,
                         includeInResult, direction,
                         anOBElement.orderExpression.getExprDataType().type);
 
                 sortInfo.add(aSortCriteria);
 
                 logger.debug("orderby = " + orderByClause);
             }
 
             logger.debug("final orderby = " + orderByClause);
         } finally {
             logger.exiting(method);
         }
     }
 
     /**
      * Create a QueryPlan from list of QueryTrees.
      *
      * @param aQueryTree
      * @throws org.postgresql.stado.exception.XDBServerException
      */
     public void createMainPlanFromTree(QueryTree aQueryTree)
             throws XDBServerException {
         final String method = "createPlanFromTreeList";
         logger.entering(method);
 
         try {
             int idx = 0;
 
             createPlan(aQueryTree, false);
 
             List<ColumnMetaData> metadata = new ArrayList<ColumnMetaData>();
 
             for(SqlExpression aSE : aQueryTree.getProjectionList()) {
                 if (!aSE.isAdded()) {
                     metadata.add(getColumnMetaData(aSE));
                 }
             }
             columnMetaData = new ColumnMetaData[metadata.size()];
             for (Object obj : metadata) {
                 columnMetaData[idx++] = (ColumnMetaData)obj;
             }
 
             // don't redo this if already did because of union
             if (orderByClause.length() == 0) {
                 determineOrderByClause(aQueryTree);
             }
 
             isTopLevelPlan = true;
 
             // if we have a correlated query, a relation query, or a union
             // we will always use all nodes
             if (useAllNodes) {
                 Collection<DBNode> dbNodeList = database.getDBNodeList();
 
                 // We need to add each of these to the main plan, if they are
                 // already not there
                 for (DBNode dbNode : dbNodeList) {
                     queryNodeTable.put(dbNode.getNodeId(), dbNode);
                 }
             }
 
             if (aQueryTree.getIntoTableName() != null) {
                 // Check if it already exists.
                 // If it does, we already created it, such as in SqlInsertTable
                 try {
                     intoTable = database.getSysTable(aQueryTree.getIntoTableName());
                     isExistingInto = true;
                 } catch (Exception e) {
                     isExistingInto = false;
                 }
                 if (intoTable == null) {
                     try {
                         intoTable = aQueryTree.createIntoTable(client);
                         if (intoTable.isTemporary()) {
                             intoTableRefName = aQueryTree.getIntoTableRefName();
                         } else {
                             syncCreateTable = new SyncCreateTable(intoTable,
                                     aQueryTree.getColumnDefinitions());
                         }
                         for (DBNode dbNode : intoTable.getNodeList()) {
                             queryNodeTable.put(dbNode.getNodeId(), dbNode);
                         }
                     } catch (Exception ex) {
                         throw new XDBServerException(
                                 "Can not create target table: " + ex.getMessage());
                     }
                 }
             }
         } finally {
             logger.exiting(method);
         }
     }
 
     /**
      * Creates a QueryPlan from a QueryTree
      *
      * @param aQueryTree
      *            the QueryTree to convert to a plan
      * @param isFromSubQuery
      *            flags this a subquery tree.
      */
     private void createPlan(QueryTree aQueryTree, boolean isFromSubQuery) {
         final String method = "createPlan";
         logger.entering(method);
 
         try {
             // To make alias handling easier, generate final alias names here
             // if they do not exist.
             generateProjectionAliases(aQueryTree.getProjectionList());
 
             // have Projections refer to the same AttributeColumn instance.
             equateProjectionAttributeColumns(aQueryTree);
 
             // The parser should equate order by expressions with those in
             // projection list, but does not always do so.
             // We fix them here
             equateOrderByWithProjections(aQueryTree);
 
             // To make resolving ambiguous columns easier, search for any
             // AttributeColumns used in conditions, and reassign instance to
             // that in the projection list, if it exists.
             // Before this change, they were different instances and sometimes
             // aliasing got confused. This makes things easier.
             equateConditionsWithProjections(aQueryTree);
 
             // Handle cases where a projection is aliased and the alias is
             // referred to in the group by. We want that to have priority
             // compared to non-projected group by expressions
             equateGroupByWithProjections(aQueryTree);
 
             // If distinct is used, we use order by to ensure that when we
             // combine results from individual nodes distinctness can be
             // determined by merging sorted results.
             if (aQueryTree.isDistinct()) {
                 updateOrderByForDistinct(aQueryTree);
             }
 
             createPlanSegmentFromTree(aQueryTree, isFromSubQuery, "", 1);
 
             // Now, update the temp table drop list, to make sure temp tables
             // are dropped at the right time.
             for (String tableName : topQueryPlan.tempTableLastUsedAt.keySet()) {
                 Leaf lastLeaf = topQueryPlan.tempTableLastUsedAt.get(tableName);
                 lastLeaf.tempTableDropList.add(tableName);
             }
             logger.debug("QueryPlan = " + this);
         } finally {
             logger.exiting(method);
         }
     }
 
     /**
      * To make resolving ambiguous columns easier, search for any
      * AttributeColumns used in conditions, and reassign instance to that in the
      * projection list, if it exists. Before this change, they were different
      * instances and sometimes aliasing got confused. This makes things easier.
      *
      * @param aQueryTree
      *            the QueryTree
      */
     private void equateConditionsWithProjections(QueryTree aQueryTree) {
         final String method = "equateConditionsWithProjections";
         logger.entering(method);
 
         try {
             List<AttributeColumn> projColumns = new ArrayList<AttributeColumn>();
             // Get all AttributeColumns in all projections
             for (SqlExpression aSE : aQueryTree.getProjectionList()) {
                 for (SqlExpression aSEC : SqlExpression.getNodes(aSE,
                         SqlExpression.SQLEX_COLUMN)) {
                     projColumns.add(aSEC.getColumn());
                 }
             }
             QueryCondition.equateConditionsWithColumns(
                     aQueryTree.getConditionList(), projColumns);
         } finally {
             logger.exiting(method);
         }
     }
 
     /**
      * To handle final projections better, have projection expressions reference
      * the same AttributeColumn instance. Note, this would be cleaner if the
      * Parser did this, maintaining a list of used AttributeColumns and
      * referencing the same instance as it goes along.
      *
      * @param aQueryTree
      *            the QueryTree
      */
     private void equateProjectionAttributeColumns(QueryTree aQueryTree) {
         final String method = "equateConditionsWithProjections";
         logger.entering(method);
 
         try {
             // Get all AttributeColumns in all projections
             List<SqlExpression> projSEColumns = new ArrayList<SqlExpression>();
             HashMap<String,AttributeColumn> columnTable = new HashMap<String,AttributeColumn>();
             for (SqlExpression aSE : aQueryTree.getProjectionList()) {
                 projSEColumns.addAll(SqlExpression.getNodes(aSE,
                         SqlExpression.SQLEX_COLUMN));
             }
 
             for (SqlExpression colExpr : projSEColumns) {
                 // There is a problem with WITH subqueries because the underlying
                 // column looks the same even though it is different,
                 // so we exclude those.
                 // As a result, we also compare that the relation node is the same
                 String columnString = colExpr.getColumn().rebuildString();
                 if (columnTable.containsKey(columnString)
                         && columnTable.get(columnString).relationNode == colExpr.getColumn().relationNode) {
                     colExpr.setColumn(columnTable.get(columnString));
                 } else {
                     columnTable.put(columnString, colExpr.getColumn());
                 }
             }
 
             List<AttributeColumn> projColumns = new ArrayList<AttributeColumn>();
 
             for (SqlExpression colExpr : projSEColumns) {
                 projColumns.add(colExpr.getColumn());
             }
 
             QueryCondition.equateConditionsWithColumns(
                     aQueryTree.getConditionList(), projColumns);
         } finally {
             logger.exiting(method);
         }
     }
 
     /**
      * Creates a QueryPlan from a QueryTree. It is also called for subqueries.
      *
      * @param unionDepth
      * @param aQueryTree the QueryTree to convert to a plan
      * @param isFromSubQuery flags this a subquery tree.
      * @param joinTable for subqueries, what parent temp table to join with
      * @throws org.postgresql.stado.exception.XDBServerException
      */
     private void createPlanSegmentFromTree(QueryTree aQueryTree,
             boolean isFromSubQuery, String joinTable, int unionDepth)
             throws XDBServerException {
 
         if (aQueryTree.getUnionQueryTreeList().size() > 0) {
             createPlanSegmentFromTreeUnion(aQueryTree, isFromSubQuery,
                     joinTable, unionDepth);
         } else {
             createPlanSegmentFromTreeDetail(aQueryTree,
                     joinTable, unionDepth);
         }
     }
 
     /**
      * Creates a union subplan QueryPlan from a QueryTree.
      *
      * @param unionDepth
      * @param aQueryTree the QueryTree to convert to a plan
      * @param isFromSubQuery flags this a subquery tree.
      * @param joinTable for subqueries, what parent temp table to join with
      * @throws org.postgresql.stado.exception.XDBServerException
      */
     private void createPlanSegmentFromTreeUnion(QueryTree aQueryTree,
             boolean isFromSubQuery, String joinTable, int unionDepth)
             throws XDBServerException {
 
         int lastStepNo = 1;
 
         this.topQueryPlan.useAllNodes = true;
 
         changeUnionAllToUnion (aQueryTree);
 
         // Handle the first plan in the query.
         StringBuilder unionCombineSB = new StringBuilder(256);
 
         unionCombineSB.append(handleFirstUnionPlan (
                 aQueryTree, joinTable, unionDepth));
 
         // Now, process the rest
         unionCombineSB.append(handleOtherUnionPlans (aQueryTree, isFromSubQuery,
                 joinTable, unionDepth));
 
         // Handle case if we are right below parent tree, which is a
         // union, and we ourselves are a union subquery.
         // In that case, we need to apply order by clause as well, but
         // it is enough to add it at the end of the query.
         if (unionDepth == 2
                 && !topQueryPlan.orderByProjPos.isEmpty()) {
 
             unionCombineSB.append(" ORDER BY ");
 
             QueryTree firstQueryTree = aQueryTree;
 
             for (int j = 0; j < this.topQueryPlan.orderByProjPos.size(); j++) {
                 SqlExpression aSqlExpression = firstQueryTree
                         .getProjectionList().get(
                         topQueryPlan.orderByProjPos.get(j).intValue());
 
                 int orderDirection = firstQueryTree.getOrderByList().get(j).orderDirection;
 
                 if (j > 0) {
                     unionCombineSB.append(",");
                 }
 
                 if (aSqlExpression.getAlias() != null
                         && aSqlExpression.getAlias().length() > 0) {
                     unionCombineSB.append(aSqlExpression.getAlias());
                 } else {
                     unionCombineSB.append(aSqlExpression.getExprString());
                 }
 
                 if (orderDirection == OrderByElement.DESC) {
                     unionCombineSB.append(" DESC");
                 }
             }
         }
 
         String unionCombineStr = unionCombineSB.toString().replaceAll(
                 "[A-Za-z]+[A-Za-z0-9_]*\\.", "");
 
         this.tempIdCounter = lastStepNo;
         this.isUnion = true;
 
         // set a target table for the UNION, to be used in the
         // QueryProcessor
         this.finalTableName = generateTempTableName();
 
         // finalProjString = orderByPlan.finalProjString;
         finalProjString = "*";
 
         if (this != this.topQueryPlan) {
             // Not a top level plan.
             // Note that this will execute on the nodes, not the
             // coordinator.
             // Create a single leaf for the union
             Leaf aLeaf = new Leaf();
 
             aLeaf.setSelectStatement(unionCombineStr);
             aLeaf.setTargetTableName(finalTableName);
 
             // update leaf's nodelist
             // We need to add each of these to the main plan
             aLeaf.queryNodeList = new ArrayList<DBNode>(database.getDBNodeList());
 
             List<SqlExpression> projList;
 
             if (aQueryTree.getFinalProjList() != null
                     && aQueryTree.getFinalProjList().size() > 0) {
                 // aggregate
                 projList = aQueryTree.getFinalProjList();
             } else {
                 // non aggreagate
                 // it = aQueryTree.rootNode.projectionList.iterator();
                 projList = aQueryTree.getProjectionList();
             }
 
             // Projections are already added to the first Union Subplan,
             // and resulting column names of union combine query will be
             // the same as first query's expression aliases.
             // We are sure aSqlExpression.getAlias() is valid because
             // Leaf.appendProjectionFromExpr have updated when the leaf
             // of the Union Subplan has been generated
             for (SqlExpression aSqlExpression : projList) {
                 String columnName = aSqlExpression.getAlias();
                 if (columnName == null || columnName.length() == 0) {
                     if (aSqlExpression.getColumn() != null) {
                         if (aSqlExpression.getColumn().columnAlias == null
                                 || aSqlExpression.getColumn().columnAlias
                                         .length() == 0) {
                             columnName = aSqlExpression.getColumn().columnName;
                         } else {
                             columnName = aSqlExpression.getColumn().columnAlias;
                         }
                     }
                 }
                 aLeaf.appendProjection(columnName, columnName,
                 		aSqlExpression.getExprDataType(),
                 		false);
             }
 
             // add this single leaf to plan
             leaves.add(aLeaf);
         }
     }
 
     /**
      *  Check the union types and readjust, if possible.
      *
      * If we have S1 UNION S2 UNION ALL S3 UNION S4,
      * it is the equivalent of S1 U S2 U S3 U S4- there
      * is no need for UNION ALL in the middle.
      * This makes combining union results easier later.
      *
      * @param aQueryTree the union plan to try and adjust.
      */
     private void changeUnionAllToUnion (QueryTree aQueryTree) {
 
         int unionMarker = 0;
 
         for (int i = 0; i < aQueryTree.getUnionQueryTreeList().size(); i++) {
             QueryTree aUnionQueryTree = aQueryTree.getUnionQueryTreeList().get(i);
 
             if (aUnionQueryTree.getUnionType() == QueryTree.UNIONTYPE_UNION) {
                 unionMarker = i;
             }
         }
 
         // Flip any UNION ALL's to UNIONs if we can
         for (int i = 0; i < unionMarker; i++) {
             QueryTree aUnionQueryTree = aQueryTree.getUnionQueryTreeList().get(i);
 
             if (aUnionQueryTree.getUnionType() == QueryTree.UNIONTYPE_UNIONALL) {
                 aUnionQueryTree.setUnionType(QueryTree.UNIONTYPE_UNION);
             }
         }
     }
 
     /**
      * Create a plan for the first UNION subquery
      *
      * @param aQueryTree the QueryTree to convert to a plan
      * @param isFromSubQuery flags this a subquery tree.
      * @param joinTable for subqueries, what parent temp table to join with
      *
      * @return - a String for combining with other unioned queries
      */
     private String handleFirstUnionPlan (QueryTree aQueryTree,
                         String joinTable, int unionDepth) throws XDBServerException {
 
         QueryPlan firstQueryPlan = new QueryPlan(client);
         firstQueryPlan.topQueryPlan = this.topQueryPlan;
         firstQueryPlan.unionType = aQueryTree.getUnionType();
 
         // Set first "union type" based on first subtree, not top tree
         QueryTree aUQueryTree = aQueryTree.getUnionQueryTreeList().get(0);
 
         if (aUQueryTree.getUnionType() != QueryTree.UNIONTYPE_UNIONALL) {
             // apply distinct, to reduce row count.
             aQueryTree.setDistinct(true);
 
             // We need to add an order by column if none exists,
             // for sorting. See if we need to add any.
 
             int projCount = 0;
 
             if (orderByProjPos != null) {
                 int orderCount = orderByProjPos.size();
 
                 for (SqlExpression projExpr : aQueryTree.getProjectionList()) {
                     boolean found = false;
 
                     for (int j = 0; j < orderCount; j++) {
                         if (projCount == orderByProjPos.get(j).intValue()) {
                             found = true;
                             break;
                         }
                     }
 
                     if (!found) {
                         // To ensure uniqueness in the UNION, we need to add
                         // this
                         OrderByElement anOBE = new OrderByElement();
                         anOBE.orderExpression = projExpr;
                         anOBE.orderDirection = OrderByElement.ASC;
                         aQueryTree.getOrderByList().add(anOBE);
                         orderByProjPos.add(Integer.valueOf(projCount));
                     }
                     projCount++;
                 }
             }
         }
 
         firstQueryPlan.createPlanSegmentFromTreeDetail(aQueryTree,
                 joinTable, unionDepth + 1);
 
         // If the parent of this union is the top plan, indicate that
         if (firstQueryPlan.topQueryPlan == this) {
             firstQueryPlan.isFinalUnionPart = true;
             limit = aQueryTree.getLimit();
             offset = aQueryTree.getOffset();
         }
 
         this.unionSubplanList.add(firstQueryPlan);
 
         String unionCombineStr;
 
         // we build up our union string here
         // Use * if we are a subquery, otherwise use the finalProjString
         if (this == this.topQueryPlan) {
             unionCombineStr = "SELECT " + firstQueryPlan.finalProjString
                     + " FROM "
                     + IdentifierHandler.quote(firstQueryPlan.finalTableName);
         } else {
             unionCombineStr = "SELECT * FROM "
                     + IdentifierHandler.quote(firstQueryPlan.finalTableName);
         }
 
         updateQueryNodeTempTableName(aQueryTree.getRootNode(),
                 firstQueryPlan.finalTableName);
 
         return unionCombineStr;
     }
 
     /**
      * Create a plan for the second and further UNION subqueries
      *
      * @param aQueryTree the QueryTree to convert to a plan
      * @param isFromSubQuery flags this a subquery tree.
      * @param joinTable for subqueries, what parent temp table to join with
      *
      * @return - a String for combining with other unioned queries
      */
     private String handleOtherUnionPlans (QueryTree aQueryTree,
                         boolean isFromSubQuery, String joinTable, int unionDepth) {
 
         StringBuilder unionCombineSB = new StringBuilder(256);
 
         int lastStepNo = 1;
 
         for (int i = 0; i < aQueryTree.getUnionQueryTreeList().size(); i++) {
             QueryTree aUnionQueryTree = aQueryTree.getUnionQueryTreeList().get(i);
 
             QueryPlan aQueryPlan = new QueryPlan(client);
             aQueryPlan.topQueryPlan = this.topQueryPlan;
 
             // Carry forward step no to avoid duplicate temp table names
             if (lastStepNo > -1) {
                 aQueryPlan.tempIdCounter = lastStepNo;
             }
 
             if (aUnionQueryTree.getUnionType() != QueryTree.UNIONTYPE_UNIONALL) {
                 aUnionQueryTree.setDistinct(true);
             }
 
             // Create order by list in union subquery
             // that matches first one specified so that we can merge.
             if (aQueryTree.getOrderByList() != null) {
                 // We need to add an order by column if none exists,
                 // for sorting.
                 // Use order by in parent
                 if (orderByProjPos != null) {
                     for (int j = 0; j < orderByProjPos.size(); j++) {
                         // create corresponding order by element in child
                         OrderByElement anOBE = new OrderByElement();
                         anOBE.orderExpression = aUnionQueryTree.getProjectionList().get(orderByProjPos.get(j).intValue());
                         anOBE.orderDirection = aQueryTree.getOrderByList().get(j).orderDirection;
                         aUnionQueryTree.getOrderByList().add(anOBE);
                     }
                 }
             }
 
             aQueryPlan.createPlanSegmentFromTree(aUnionQueryTree,
                     isFromSubQuery, joinTable, unionDepth + 1);
 
             // If the parent of this union is the top plan, indicate that
             if (aQueryPlan.topQueryPlan == this) {
                 aQueryPlan.isFinalUnionPart = true;
             }
 
             aQueryPlan.unionType = aUnionQueryTree.getUnionType();
 
             this.unionSubplanList.add(aQueryPlan);
             lastStepNo = aQueryPlan.tempIdCounter;
 
             // Build up final statement to combine results
             unionCombineSB.append(" UNION ");
 
             if (aQueryPlan.unionType == QueryPlan.UNIONTYPE_UNIONALL) {
                 unionCombineSB.append("ALL ");
             }
 
             if (this == this.topQueryPlan) {
                 unionCombineSB.append("SELECT ")
                         .append(aQueryPlan.finalProjString)
                         .append(" FROM ")
                         .append(IdentifierHandler.quote(aQueryPlan.finalTableName));
             } else {
                 unionCombineSB.append("SELECT * FROM ")
                         .append(IdentifierHandler.quote(aQueryPlan.finalTableName));
             }
 
             updateQueryNodeTempTableName(aQueryTree.getRootNode(),
                     aQueryPlan.finalTableName);
         }
 
         return unionCombineSB.toString();
     }
 
     /**
      * Creates a QueryPlan from a QueryTree. It is also called for subqueries.
      *
      * @param unionDepth
      * @param aQueryTree the QueryTree to convert to a plan
      * @param joinTable for subqueries, what parent temp table to join with
      * @throws org.postgresql.stado.exception.XDBServerException
      */
     private void createPlanSegmentFromTreeDetail(QueryTree aQueryTree,
             String joinTable, int unionDepth)
             throws XDBServerException {
         final String method = "createPlanSegmentFromTreeDetail";
         logger.entering(method);
 
         try {
             // if beginning correlated subquery, allow previous results to flow
             // in
             lastJoinTableName = joinTable;
 
             // Create WITH subplans, if any
             createWithSubplans (aQueryTree, unionDepth);
             
             // Create relation subplans, if any
             createRelationSubplans (aQueryTree, unionDepth);
 
             /**
              * see if we have any scalar subqueries, where it is part of a
              * condition and exactly one value is expected.
              */
             createScalarSubplans (aQueryTree, unionDepth);
 
             this.isDistinct = aQueryTree.isDistinct();
             this.limit = aQueryTree.getLimit();
             this.offset = aQueryTree.getOffset();
 
             logger.debug("containsAggregates = "
                     + aQueryTree.isContainsAggregates());
 
             // Process each node left-depth first recursively,
             // working our way up
             createPlanStep(aQueryTree, aQueryTree.getRootNode());
 
             // If we have aggregates, create one more step.
             // Do not do it if we are are dealing with a Correlated
             // subquery, though
             if (aQueryTree.isContainsAggregates()
                     && !aQueryTree.isPartitionedGroupBy()
                     && !aQueryTree.usesSingleDBNode()
                     && this.planType != QueryPlan.CORRELATED) {
 
                 // Note that this also calls adjustFinalProjections
                 createFinalPlanStep(aQueryTree);
             }
 
             // We call this to determine the final projection
             // string that should be used when joining with the
             // correlated subquery.
             // The "final" projection of the subquery itself will have
             // already been done based on the root query node.
             if (this.planType == QueryPlan.CORRELATED) {
                 determineFinalCorrelatedProjections(aQueryTree.getProjectionList());
             }
 
             // we need to do this in case we are using UNIONs
             determineOrderByClause(aQueryTree);
 
             // now assign the SELECT statements to each step
             assignSteps(aQueryTree);
         } finally {
             logger.exiting(method);
         }
     }
 
     /**
      * Handle queries that make up new relations in WITH clause
      *
      * @param aQueryTree the QueryTree to convert to a plan
      * @param unionDepth
      *
      * @throws org.postgresql.stado.exception.XDBServerException
      */
     private void createWithSubplans(QueryTree aQueryTree, int unionDepth)
             throws XDBServerException {
 
         for (RelationNode subTreeRelationNode : aQueryTree.getTopWithSubqueryList()) {
             this.subplanList.add(createSubplan(subTreeRelationNode, unionDepth));
         }
     }
     
     /**
      * Handle queries that make up new relations in FROM clause
      *
      * @param aQueryTree the QueryTree to convert to a plan
      * @param unionDepth
      *
      * @throws org.postgresql.stado.exception.XDBServerException
      */
     private void createRelationSubplans(QueryTree aQueryTree, int unionDepth)
             throws XDBServerException {
 
         for (RelationNode subTreeRelationNode : aQueryTree.getRelationSubqueryList()) {
             if (subTreeRelationNode.getSubqueryTree() != null) {
                 subplanList.add(createSubplan(subTreeRelationNode, unionDepth));
             }
         }
     }
     
     /**
      * 
      * @param subTreeNode
      * @param unionDepth
      * @return
      * @throws XDBServerException 
      */
     private QueryPlan createSubplan(RelationNode subTreeNode, int unionDepth) 
             throws XDBServerException {
         
         logger.debug(" - createSubplan - ");
         topQueryPlan.useAllNodes = true;
 
         QueryTree subTree = subTreeNode.getSubqueryTree();
 
         QueryPlan aQueryPlan = new QueryPlan(client);
         aQueryPlan.topQueryPlan = this.topQueryPlan;
 
         aQueryPlan.tempIdCounter = this.tempIdCounter;
         aQueryPlan.planType = RELATION;
 
         // Handle DISTINCT
         if (subTree.isDistinct()
                 && (subTree.getGroupByList() == null
                 || subTree.getGroupByList().size() == 0)) {
 
             //subTree.getRootNode().setGroupByList(subTree.getProjectionList());
             subTree.setGroupByList(subTree.getProjectionList());
             subTree.setContainsAggregates(true);
         }
 
         aQueryPlan.createPlanSegmentFromTree(subTree, true, "",
                 unionDepth);
 
         this.tempIdCounter = aQueryPlan.tempIdCounter;
 
         subTreeNode.setTableName(aQueryPlan.finalTableName);
         subTreeNode.setAlias(""); // ignore any alias that was set
 
         // Make sure the temp table is set properly
         subTreeNode.setCurrentTempTableName(aQueryPlan.finalTableName);
 
         return aQueryPlan;
     }
 
 
     /**
      *
      * Creates a QueryPlan from a QueryTree. It is also called for subqueries.
      *
      * @param aQueryTree the QueryTree to convert to a plan
      * @param unionDepth
      *
      * @throws org.postgresql.stado.exception.XDBServerException
      */
     private void createScalarSubplans(QueryTree aQueryTree, int unionDepth)
             throws XDBServerException {
 
         for (int i = 0; i < aQueryTree.getScalarSubqueryList().size(); i++) {
             logger.debug(" - scalarSubqueryList - ");
 
             SqlExpression aSqlExpression = aQueryTree.getScalarSubqueryList().get(i);
 
             QueryTree subTree = aSqlExpression.getSubqueryTree();
 
             QueryPlan aQueryPlan = new QueryPlan(client);
             aQueryPlan.topQueryPlan = this.topQueryPlan;
 
             aQueryPlan.tempIdCounter = this.tempIdCounter;
             aQueryPlan.planType = SCALAR;
 
             // Handle DISTINCT
             if (subTree.isDistinct()
                     && (subTree.getGroupByList() == null
                     || subTree.getGroupByList().size() == 0)) {
                 subTree.setGroupByList(subTree.getProjectionList());
                 subTree.setContainsAggregates(true);
             }
 
             aQueryPlan.createPlanSegmentFromTree(subTree, true, "",
                     unionDepth);
 
             this.tempIdCounter = aQueryPlan.tempIdCounter;
 
             // Now, modify SqlExpression, setting a constant placeholder
             // that will be substituted later
             aSqlExpression.setExprType(SqlExpression.SQLEX_SUBQUERY);
             placeHolderNo++;
             aSqlExpression.setConstantValue("&x" + placeHolderNo + "x&");
             aSqlExpression.setExprString(aSqlExpression.getConstantValue());
 
             aSqlExpression.setTempExpr(true); // don't rebuild this expr!
             logger.debug("   - new expr: " + aSqlExpression.getExprString());
 
             // tell subplan what placeholder it is for in parent
             aQueryPlan.placeHolderNo = this.placeHolderNo;
 
             // Add to plan
             this.subplanList.add(aQueryPlan);
         }
     }
 
     /**
      * See if any projections don't have aliases defined at the top level
      * expressions if not a column, and create them. This makes working with the
      * final projections easier.
      *
      * Try and detect duplicate column names and generate aliases for
      * them as well.
      *
      * @param projList
      *            the projection list of query
      */
     private void generateProjectionAliases(List<SqlExpression> projList) {
         final String method = "generateProjectionAliases";
         logger.entering(method);
 
         try {
             HashMap aliasMap = new HashMap();
             int genCount = 0;
             for (int i = 0; i < projList.size(); i++) {
                 SqlExpression aSqlExpr = projList.get(i);
 
                 if (aSqlExpr.getAlias() == null || aSqlExpr.getAlias().length() == 0) {
                     if (aSqlExpr.getExprType() != SqlExpression.SQLEX_COLUMN) {
                         aSqlExpr.setAlias("EXPRESSION" + ++genCount);
 
                         // Label projection PostgreSQL-style, taking function
                         // name if possible
                         if (aSqlExpr.getExprType() == SqlExpression.SQLEX_FUNCTION) {
                             aSqlExpr.setProjectionLabel(aSqlExpr.getFunctionName());
                         } else if (aSqlExpr.getExprType() == SqlExpression.SQLEX_CASE) {
                             aSqlExpr.setProjectionLabel("case");
                         }
                     } else {
                         // check if duplicate column name, then create alias.
                         if (aSqlExpr.getColumn().columnAlias != "") {
                             if (aliasMap.containsKey(aSqlExpr.getColumn().columnAlias)) {
                                 aSqlExpr.setAlias("EXPRESSION" + ++genCount);
                             } else {
                                 aSqlExpr.setAlias(aSqlExpr.getColumn().columnAlias);
                             }
                         } else if (aliasMap.containsKey(aSqlExpr.getColumn().columnName)) {
                             aSqlExpr.setAlias("EXPRESSION" + ++genCount);
                         } else {
                             aSqlExpr.setAlias(aSqlExpr.getColumn().columnName);
                         }
                     }
                 } else {
                     if (aliasMap.containsKey(aSqlExpr.getAlias())) {
                         aSqlExpr.setAlias("EXPRESSION" + ++genCount);
                     }
                 }
 
                 aliasMap.put(aSqlExpr.getAlias(), null);
             }
         } finally {
             logger.exiting(method);
         }
     }
 
     /**
      * Try and substitute order by expressions with those from projection list.
      *
      * @param aQueryTree
      *            the QueryTree to evaluate
      */
     private void equateOrderByWithProjections(QueryTree aQueryTree) {
         final String method = "equateOrderByWithProjections";
         logger.entering(method);
 
         try {
             int projPos = 0;
 
             orderByProjPos = new ArrayList<Integer>();
 
             for (OrderByElement anOE : aQueryTree.getOrderByList()) {
                 anOE.orderExpression.rebuildExpression();
                 String orderString = anOE.orderExpression.getExprString();
                 projPos = -1;
                 // look for it in proj list
                 for (SqlExpression projExpr : aQueryTree.getProjectionList()) {
                     projExpr.rebuildExpression();
 
                     projPos++;
                     if (orderString.equalsIgnoreCase(projExpr.getExprString())) {
                         // found a match; substitute
                         anOE.orderExpression = projExpr;
                         orderByProjPos.add(Integer.valueOf(projPos));
                         break;
                     }
                 }
             }
         } finally {
             logger.exiting(method);
         }
     }
 
     /**
      * Try and substitute group by expressions with those from projection list.
      *
      * @param aQueryTree
      *            the QueryTree to evaluate
      */
     private void equateGroupByWithProjections(QueryTree aQueryTree) {
         final String method = "equateGroupByWithProjections";
         logger.entering(method);
         ArrayList<SqlExpression> newGroupByList = new ArrayList<SqlExpression>();
         try {
 
             for (SqlExpression aSE : aQueryTree.getGroupByList()) {
                 aSE.rebuildExpression();
 
                 if (aSE.getMappedExpression() != null
                         && aSE.getExprType() == SqlExpression.SQLEX_CONSTANT) {
                     aSE.setConstantValue(aSE.getAlias());
                 }
                 newGroupByList.add(aSE);
             }
             aQueryTree.setGroupByList(newGroupByList);
         } finally {
             logger.exiting(method);
         }
     }
 
     /**
      * Handle the case when we use distinct. We may combine results from the
      * nodes, in which case we should ORDER BY all elements in the projection
      * list.
      *
      * @param aQueryTree
      *            the QueryTree to evaluate
      */
     private void updateOrderByForDistinct(QueryTree aQueryTree) {
         final String method = "updateOrderByForDistinct";
         logger.entering(method);
 
         /* TODO: We could make this smarter and only add unique key columns, as well
          * as try to use types like int, long first, before CHAR and such.
          */
         try {
             // look for it in proj list
             for (SqlExpression projExpr : aQueryTree.getProjectionList()) {
 
                 if (projExpr.getExprType() == SqlExpression.SQLEX_CONSTANT) {
                     continue;
                 }
 
                 projExpr.rebuildExpression();
 
                 // see if it is in order by clause
                 boolean found = false;
 
                 for (OrderByElement anOE : aQueryTree.getOrderByList()) {
                     anOE.orderExpression.rebuildExpression();
                     String orderString = anOE.orderExpression.getExprString();
 
                     if (orderString.equalsIgnoreCase(projExpr.getExprString())) {
                         // found a match; substitute
                         found = true;
                         break;
                     }
                 }
 
                 if (!found) {
                     OrderByElement anOBE = new OrderByElement();
                     anOBE.orderExpression = projExpr;
                     aQueryTree.getOrderByList().add(anOBE);
                     anOBE.orderDirection = OrderByElement.ASC;
                 }
             }
         } finally {
             logger.exiting(method);
         }
     }
 
     /**
      * Generates a temp table name to use for intermediate results.
      *
      * @return String generated temp table name
      */
     private String generateTempTableName() {
         final String method = "generateTempTableName";
         logger.entering(method);
 
         try {
             tempIdCounter++;
 
             return Props.XDB_TEMPTABLEPREFIX + "T" + queryId + "_" + tempIdCounter;
         } finally {
             logger.exiting(method);
         }
     }
 
     /**
      * Determines if a correlated query is "simple", that is, that it can be
      * executed in one step, avoiding data-down and rejoin steps.
      *
      * @return the table to base intermediate results on, or null if it is not a
      *         simple correlated query.
      * @param aRelationNode
      */
     private String getSingleCorrelatedBaseTable(RelationNode aRelationNode) {
 
         String baseTable = null;
         boolean foundNonLookupBase = false;
 
         for (RelationNode checkNode :
                 aRelationNode.getSubqueryTree().getRelationNodeList()) {
 
             switch (checkNode.getNodeType()) {
             case RelationNode.SUBQUERY_CORRELATED_PH:
                 continue;
 
             case RelationNode.TABLE:
 
                 String checkTableName = checkNode.getTableName();
                 SysTable sysTable = database.getSysTable(checkTableName);
 
                 if (sysTable.getPartitionScheme() == SysTable.PTYPE_LOOKUP) {
                     // If it is a lookup, it is ok, keep looking
                     if (baseTable == null) {
                         // No other ones, just assign it.
                         baseTable = checkTableName;
                     }
                 } else {
                     if (!foundNonLookupBase) {
                         // No other ones, just assign it.
                         baseTable = checkTableName;
                         foundNonLookupBase = true;
                     } else {
                         // We have more than non-lookup table,
                         // this case is not simple, so just
                         // return null;
                         return null;
                     }
                 }
                 break;
 
             // For other types, assume it is not simple case
             default:
                 return null;
             }
 
         }
         return baseTable;
     }
 
     /**
      * Determines simple correlated join expression to use in sending
      * intermediate rows.
      *
      * @return the table to base intermediate results on, or null if it is not a
      *         simple correlated query.
      * @param aRelationNode
      * @param correlatedBaseTable
      */
     private String getSingleCorrelatedHashColumn(RelationNode aRelationNode,
             String correlatedBaseTable) {
         SysTable sysTable = database.getSysTable(correlatedBaseTable);
 
         if (sysTable.getPartitionScheme() != SysTable.PTYPE_HASH) {
             return null;
         }
 
         String partColumn = sysTable.getPartitionColumn();
 
         for (QueryCondition aQC :
                 aRelationNode.getSubqueryTree().getConditionList()) {
 
             if (aQC.getCondType() != QueryCondition.QC_RELOP) {
                 continue;
             }
 
             if (aQC.getLeftCond() != null
                     && aQC.getLeftCond().getCondType() == QueryCondition.QC_SQLEXPR
                     && aQC.getLeftCond().getExpr().getExprType() == SqlExpression.SQLEX_COLUMN
                     && aQC.getRightCond() != null
                     && aQC.getRightCond().getCondType() == QueryCondition.QC_SQLEXPR
                     && aQC.getRightCond().getExpr().getExprType() == SqlExpression.SQLEX_COLUMN) {
 
                 String checkStr = checkCorrelatedHashString (
                         aQC.getLeftCond().getExpr().getColumn(),
                         correlatedBaseTable, partColumn,
                         aQC.getRightCond().getExpr());
 
                 if (checkStr != null) {
                     return IdentifierHandler.stripQuotes(checkStr);
                 }
 
                 checkStr = checkCorrelatedHashString (
                         aQC.getRightCond().getExpr().getColumn(),
                         correlatedBaseTable, partColumn,
                         aQC.getLeftCond().getExpr());
 
                 if (checkStr != null) {
                     return IdentifierHandler.stripQuotes(checkStr);
                 }
             }
         }
         return null;
     }
 
     /**
      * see if the AttributeColumn matches the partitioning column of
      * the specified table.
      *
      * @param anAttributeColumn candidate column to compare
      * @param correlatedBaseTable base table to look for
      * @param partColumn partition column to look for
      * @param aQueryCondition the condition to comare
      */
     public String checkCorrelatedHashString (AttributeColumn anAttributeColumn,
             String correlatedBaseTable, String partColumn,
             SqlExpression aSqlExpression) {
 
         String hashStr = null;
 
         if (anAttributeColumn.getTableName().compareToIgnoreCase(
                 correlatedBaseTable) == 0
                 && anAttributeColumn.columnName.compareToIgnoreCase(partColumn) == 0) {
 
             // we found a comparison to the partition column
             aSqlExpression.rebuildExpression();
             hashStr = aSqlExpression.getExprString();
 
             if (hashStr.lastIndexOf('.') > 0) {
                 hashStr = hashStr.substring(hashStr.lastIndexOf('.') + 1);
             }
         }
 
         return hashStr;
     }
 
     /**
      * If we are based on a WITH, update our temp table name
      * We use this mechanism to have the wrapped relation node
      * refer to the WITH subquery
      * 
      * @param aQueryNode 
      */
     private void checkRightNodeWith(QueryNode aQueryNode) {
 
         if (aQueryNode.getNodeType() == QueryNode.RELATION) {
             
             if (aQueryNode.isWithDerived()) {
 
                 aQueryNode.getRelationNode().setCurrentTempTableName(
                         aQueryNode.getRelationNode().getBaseWithRelation().getCurrentTempTableName());
             }
         } else if (aQueryNode.getNodeType() == QueryNode.JOIN
                 && aQueryNode.getRightNode() != null) {
             checkRightNodeWith(aQueryNode.getRightNode());
         }
     }
     
     
     /**
      * Creates a step (Leaf) based on aQueryNode. We traverse left-depth,
      * processing the left-most node and all subtrees on the right
      *
      * @param aQueryTree the QueryTree the QueryNode belongs to
      * @param aQueryNode the QueryNode to convert
      * @throws org.postgresql.stado.exception.XDBServerException
      */
     private void createPlanStep(QueryTree aQueryTree, QueryNode aQueryNode)
             throws XDBServerException {
         final String method = "createPlanStep";
         logger.entering(method);
 
         // All right subtrees are to be processed "together",
         // whether that is an individual table or group of tables.
         try {
             List<QueryCondition> joinCondGroup = null;
             List<QueryCondition> joinCondGroup2 = null;
 
             logger.debug("nodeType = " + aQueryNode.getNodeType());
 
             // recurse left if have not hit a TABLE or preserved subtree
             if (aQueryNode.getNodeType() == QueryNode.JOIN) {
 
                 // While we traverse left, the right node may contain a WTIH
                 // statement that did not get its starting temp table initialized
                 // so do it here.
                 if (aQueryNode.getRightNode() != null) {
                     checkRightNodeWith(aQueryNode.getRightNode());
                 }
             
                 // Traverse down to the left and create steps
                 if (aQueryNode.getLeftNode() != null
                         && !aQueryNode.isPreserveSubtree()) {
 
                     createPlanStep(aQueryTree, aQueryNode.getLeftNode());
                 }
             }
 
 
             
             // Detect if we are trying to do a down-tree join.
             // We want to have that execute as its own step first, then
             // fold that in with the current step later
             if (aQueryNode.getParent() != null && aQueryNode.getParent().isTreeDownJoin()
                     && aQueryNode.getParent().getRightNode() != aQueryNode) // not self!
             {
                 createPlanStep(aQueryTree, aQueryNode.getParent().getRightNode());
             }
 
             // don't create a step for the correlated "placeholder"
             // Mark the node, then process it with the next
             // correlated subquery step
             // We do this to avoid creating an extra step, and to
             // grab its conditions.
             if (aQueryNode.getNodeType() == QueryNode.RELATION
                     && aQueryNode.getRelationNode().getNodeType()
                             == RelationNode.SUBQUERY_CORRELATED_PH) {
                 correlatedStart = true;
                 correlatedNode = aQueryNode;
                 return;
             }
 
             // If we are based on a WITH, update our temp table name
             // We use this mechanism to have the wrapped relation node
             // refer to the WITH subquery
             if (aQueryNode.getNodeType() == QueryNode.RELATION
                     && aQueryNode.isWithDerived()) {
                 //if (aQueryNode.getRelationNode().getCurrentTempTableName().isEmpty()) {
                     aQueryNode.getRelationNode().setCurrentTempTableName(
                             aQueryNode.getRelationNode().getBaseWithRelation().getCurrentTempTableName());
                 //}
             }
             
             // Create a new Step
             Leaf aLeaf = getStepToProcess (aQueryNode, isDistinct);
 
             // See if we have the leftmost preserved subtree or node,
             // and process, otherwise, process the right side
             // but, don't do for correlated subtrees
 
             // Note "subtree" does not mean "subquery" here...
             // just a subtree in query tree
             if (aQueryTree.isContainsAggregates() && aQueryNode.getParent() == null
                     && !aQueryTree.isCorrelatedSubtree()) {
 
                 if (aQueryNode.isTreeDownJoin()) {
                     aLeaf.resetProjections();
                 }
                 updateAggProjections(aQueryTree, aLeaf);
                 if (aQueryTree.isPartitionedGroupBy()
                         || aQueryTree.usesSingleDBNode()) {
                     /* make sure having expression is ok */
                     addHavingConditions(aQueryTree, aLeaf);
                 }
             }
 
             if (aQueryNode.getNodeType() == QueryNode.RELATION
                     && (aQueryNode.getRelationNode().isSubquery() || aQueryNode.getRelationNode().getNodeType() == RelationNode.TABLE)
                     || aQueryNode.getNodeType() == QueryNode.JOIN
                     && aQueryNode.isPreserveSubtree()) {
                 // TODO - we should be able to optimize this and avoid
                 // in some cases of WITH. We essentially are materializing
                 // and then reusing. In some cases, we should be able to fold
                 // into one step... future optimization
                 createPlanStepSubtree(aQueryNode, aLeaf);
             } else if (aQueryNode.getNodeType() == QueryNode.RELATION
                     && aQueryNode.getRelationNode().getNodeType() == RelationNode.FAKE) {
                 createPlanStepFake(aLeaf);
             } else {
                 String saveTempName = lastJoinTableName;
 
                 // Process correlated subquery tree if we find one
                 // Note that this is processed before we process the
                 // corresponding node in the parent tree
                 createPlanStepCheckCorrelated (aQueryNode, aLeaf);
 
                 // do this, whether it is a subtree or just individual table
                 if (!aQueryNode.isTreeDownJoin()) {
                     createPlanStepSubtree(aQueryNode.getRightNode(), aLeaf);
                 }
 
                 lastJoinTableName = saveTempName;
             }
 
             // specify what temp table to join with for next step
             determineJoinTable(aQueryNode, aLeaf);
 
             // Normal case for join conditions
             levelStatus = 0;
             aLeaf2 = null;
 
             if (aQueryNode.getConditionList().size() > 0) {
                 createPlanStepDoConditions(aQueryNode, aLeaf, joinCondGroup,
                         joinCondGroup2);
             }
 
             // If we had found a correlated node last time, add its conditions
             if (correlatedNode != null) {
                 updateCorrelatedConditions(aLeaf);
             }
 
             // Determine SELECT list
             createPlanStepDoProjections(aQueryTree, aQueryNode, aLeaf);
 
             createPlanStepDoGroupByList(aQueryTree, aQueryNode, aLeaf);
 
             /**
              * Note: we do order by on the very last step
              */
             this.finalTableName = aLeaf.getTargetTableName();
 
             // Make sure we have at least one column
             // due to cartesian products
             checkForCartesianProduct(aQueryNode, aLeaf);
 
             // update column aliases we generated.
             aLeaf.updateTempAliases();
 
             // Check for special outer case
             checkOuterCase (aLeaf, joinCondGroup2);
 
             // We also determine which nodes to execute the step on.
             determineExecutionNodes (aLeaf, aLeaf2);
 
             // Moved this in from CreatePlanFromTree,
             // to address aliasing issues.
             // Check to see if we are the final step when NOT
             // doing aggregates, to go ahead and update the final
             // projections and order by
             if (aQueryNode.getParent() == null
                     && (!aQueryTree.isContainsAggregates()
                             || aQueryTree.isPartitionedGroupBy() || aQueryTree.usesSingleDBNode())) {
                 Leaf lastLeaf = aLeaf;
 
                 if (levelStatus == 3) {
                     lastLeaf = aLeaf2;
                 }
 
                 // adjustFinalProjections (lastLeaf, aQueryTree.projectionList);
 
                 adjustNonAggOrderBy(lastLeaf, aQueryTree.getOrderByList());
             }
 
             // Now that we have created the step, we tell all used QueryNodes
             // what their current temp table is.
             // (except for if we are the joining node right below a tree-down
             // node)
 
             
             if (!(aQueryNode.getParent() != null 
                         && aQueryNode.getParent().isTreeDownJoin() 
                         && aQueryNode.getParent().getLeftNode() == aQueryNode)) {
                 updateQueryNodeTempTableName(aQueryNode, aLeaf.getTargetTableName());
             }
 
             if (levelStatus == 3) {
                 updateQueryNodeTempTableName(aQueryNode, aLeaf2.getTargetTableName());
             }
 
             /*
              * If we have an outer join and we cannot join on the inner table
              * via the partitioned key, we need to add special handling.
              */
             if (levelStatus == 2 && aLeaf.getHashColumn() == null) {
                 // We need to do some additional work for outer join handling.
                 handleOuter(aLeaf, aQueryNode.getLeftNode().getOuterLevel(), aQueryTree);
             }
 
         } finally {
             logger.exiting(method);
         }
     }
 
     /**
      * Create a new step for the plan, and initialize.
      * If doing a tree down join, use previous Leaf
      *
      * @param aQueryNode - the current QueryNode to create a step for
      * @param isDistinct - whether or not this query is distinct
      */
     private Leaf getStepToProcess (QueryNode aQueryNode, boolean isDistinct) {
 
         Leaf aLeaf;
 
         // detect if we are trying to do a down-tree join.
         // In that case, instead of creating a new Leaf (step),
         // we reuse the last one
         if (aQueryNode.isTreeDownJoin()) {
             aLeaf = leaves.get(leaves.size() - 1);
 
             Leaf prevLeaf = leaves.get(leaves.size() - 2);
 
             // Use table from parent rightNode
             aLeaf.setTableName(aLeaf.getTableName() + "," + prevLeaf.getTargetTableName());
             aLeaf.addUsedTable(prevLeaf.getTargetTableName());
             aLeaf.fromRelationList.add(
                     new FromRelation(prevLeaf.getTargetTableName(), 
                     prevLeaf.getTargetTableName(),
                     false,
                     aQueryNode.getRightNode().getOuterLevel(),
                     aQueryNode.getRightNode().getRelationNode() != null ? aQueryNode.getRightNode().getRelationNode().isOnly() : false));         
 
             // we also need the temp table name 2 steps back
             prevLeaf = leaves.get(leaves.size() - 3);
             aLeaf.setJoinTableName(prevLeaf.getTargetTableName());
         } else {
             aLeaf = new Leaf();
             addLeaf(aLeaf);
 
             aLeaf.setLeafStepNo(++this.currentLeafStepNo);
             if (aQueryNode.getLeftNode() != null) {
                 aLeaf.lastOuterLevel = aQueryNode.getLeftNode().getOuterLevel();
             }
 
             if (isDistinct) {
                 aLeaf.setDistinct(true);
             }
         }
         return aLeaf;
     }
 
     /**
      * Check if we are dealing with a correlated subquery, and handle it.
      *
      * @param aQueryNode the current QueryNode
      */
     private void createPlanStepCheckCorrelated (QueryNode aQueryNode,
             Leaf aLeaf) {
 
         if (aQueryNode.getRightNode().getNodeType() == QueryNode.RELATION
                 && aQueryNode.getRightNode().getRelationNode().getNodeType() == RelationNode.SUBQUERY_CORRELATED) {
 
             // Note that we first check if it is a multi-step correlated
             // subquery. Only then do we add extra data-down and
             // rejoin steps, otherwise, we just want to treat this as
             // a single step.
             String correlatedBaseTable = getSingleCorrelatedBaseTable(aQueryNode.getRightNode().getRelationNode());
 
             if (correlatedBaseTable != null) {
                 aLeaf.setSingleCorrelatedHash(getSingleCorrelatedHashColumn(
                         aQueryNode.getRightNode().getRelationNode(),
                         correlatedBaseTable));
             }
 
             // If null, we cannot do the correlated query in a single
             // step
             if (aLeaf.getSingleCorrelatedHash() == null) {
                 aLeaf.correlatedJoinTableName = lastJoinTableName + "A";
                 topQueryPlan.useAllNodes = true;
 
                 createPlanStepCorrelated(aQueryNode, aLeaf);
 
                 lastJoinTableName = aLeaf.correlatedJoinTableName;
             } else {
                 aLeaf.setSingleStepCorrelated(true);
 
                 // Note, we set tableName to the base table to use.
                 // This will help StepDetail.convertFromLeaf
                 // to pick the proper node destination.
                 aLeaf.setTableName(correlatedBaseTable);
             }
         }
     }
 
     /**
      * determine what temp table to join with for next step
      *
      * @param aQueryNode the current QueryNode
      * @param aLeaf the current step
      */
     private void determineJoinTable (QueryNode aQueryNode, Leaf aLeaf) {
 
         boolean isSubQ = false;
 
         if (aQueryNode.getNodeType() == QueryNode.RELATION
                 && aQueryNode.getRelationNode().isSubquery()) {
                 isSubQ = true;
         }
 
         logger.debug("stepNo = " + tempIdCounter);
         if (isSubQ) {
             logger.debug("isSubquery() = "
                     + aQueryNode.getRelationNode().isSubquery());
         }
         logger.debug("leftNode: " + (aQueryNode.getLeftNode() != null));
 
         // Don't do substitution here for subquery
         // Make sure we did not just do relation subquery
         // nor scalar
         if (!aQueryNode.isTreeDownJoin()) {
             if (tempIdCounter > 0
                     && !isSubQ
                     && aQueryNode.getLeftNode() != null
                     && !(aQueryNode.getParent() != null && aQueryNode.getParent().isTreeDownJoin())
                     && lastJoinTableName != null && lastJoinTableName.length() > 0
                     || correlatedStart) {
 
                 aLeaf.setJoinTableName(lastJoinTableName);
                 logger.debug("joinTable: " + aLeaf.getJoinTableName());
                 logger.debug("nodeType : " + aQueryNode.getNodeType());
             }
             aLeaf.setTargetTableName(generateTempTableName());
             lastJoinTableName = aLeaf.getTargetTableName();
         }
 
         logger.debug("target : " + aLeaf.getTargetTableName());
     }
 
 
     /**
      * Update this step with correlated conditions
      *
      * @param Leaf the current step
      */
     private void updateCorrelatedConditions (Leaf aLeaf) {
 
         if (correlatedNode != null) {
             logger.debug("searching for correlated");
 
             for (int i = 0; i < correlatedNode.getConditionList().size(); i++) {
                 QueryCondition aQueryCondition = correlatedNode.getConditionList().get(i);
 
                 logger.debug(" - merging cond.");
 
                 if (aQueryCondition.isJoin() && !aQueryCondition.isInPlan()) {
                     // rebuild, we may have made changes for subqueries
                     aQueryCondition.rebuildCondString();
 
                     // join is a bit of a misnomer here.
                     aLeaf.addJoin(aQueryCondition, aQueryCondition.getCondString());
                     aQueryCondition.setInPlan(true);
                     logger.debug(" - joincond 3a: "
                             + aQueryCondition.getCondString());
                 }
             }
             // Tell Processor first step in correlated subquery
             logger.debug("SUBQUERY_DATA_DOWN");
 
             aLeaf.setLeafType(Leaf.SUBQUERY_DATA_DOWN);
 
             // reset
             correlatedStart = false;
             correlatedNode = null;
         }
     }
 
     /**
      * Checks and adjusts if we have a cartesian product on this step,
      * and adds a dummy column if necessary
      *
      * @param aQueryNode the current node
      * @param aLeaf the current step
      */
     private void checkForCartesianProduct (QueryNode aQueryNode, Leaf aLeaf) {
 
         // Make sure we have at least one column
         if (!aLeaf.hasProjections()
                 && aQueryNode.getParent() != null) {
 
             SqlExpression tempExpr = new SqlExpression();
 
             ExpressionType anET = new ExpressionType();
             anET.type = org.postgresql.stado.parser.ExpressionType.CHAR_TYPE;
             anET.length = 1;
 
             tempExpr.setExprType(SqlExpression.SQLEX_CONSTANT);
             tempExpr.setConstantValue("'1'");
             tempExpr.setExprString("'1'");
             tempExpr.setExprDataType(anET); // java.sql.Types.CHAR;
             tempExpr.setAlias("XDUMMY");
             tempExpr.setTempExpr(true);
 
             aLeaf.appendProjectionFromExpr(tempExpr, false);
 
             logger.debug("Col: " + tempExpr.getExprString());
         }
     }
 
     /**
      * Adjusts the current outer step if necessary
      *
      * @param aLeaf the current step
      * @param joinCondGroup2 list of outer join conditions
      */
     private void checkOuterCase (Leaf aLeaf,
             List<QueryCondition> joinCondGroup2) {
         // See if we need to create an extra step due to OUTERs
         /*
          * If we have more than one join condition, we need to look at all
          * of the conditions and compare OUTER levels. If they are
          * different, we must create an extra step and rejoin. For example:
          * SELECT .... FROM a, (OUTER b, c) WHERE a.col1 = b.col1 AND b.col2 =
          * c.col2 AND a.col3 = c.col3
          *
          * If we have already done the a-b join, we will have a QueryNode
          * that needs to join with c, using the two conditions of tmp1.col2 =
          * c.col2 tmp1.col3 = c.col3
          *
          * We want the col2 join to NOT be OUTER'ed, but we do want the col3
          * join to be OUTER'ed. So, we need to this as two separate steps,
          * and create aLeaf2
          */
         if (levelStatus == 3) {
             for (QueryCondition aQueryCondition : joinCondGroup2) {
                 // Also, make sure we add extra join columns to aLeaf2,
                 // if necessary.
                 // We check to make sure all query condition columns
                 // are there.
                 for (int j = 0; j < aQueryCondition.getColumnList().size(); j++) {
                     AttributeColumn anAC = aQueryCondition.getColumnList().get(j);
 
                     SqlExpression aSE;
 
                     // First, build up candidate column to create
                     aSE = new SqlExpression();
 
                     aSE.setExprType(SqlExpression.SQLEX_COLUMN);
                     aSE.setColumn(anAC);
                     aSE.setExprDataType(anAC.columnType);
 
                     if (anAC.columnAlias != null) {
                         aSE.setAlias(anAC.columnAlias);
                     } else {
                         aSE.setAlias(anAC.columnName);
                     }
 
                     aSE.rebuildExpression();
 
                     // See if it already is in aLeaf.
                     if (!aLeaf.isProjection(aSE.getExprString())) {
                         aLeaf.appendProjectionFromExpr(aSE, false);
                     }
                 }
             }
 
             addLeaf(aLeaf2); // Add second leaf, too.
         }
     }
 
     /**
      * Determine which nodes to execute the current step on.
      *
      * @param aLeaf current step
      * @param extraLeaf extra outer Leaf
      */
     private void determineExecutionNodes (Leaf aLeaf, Leaf extraLeaf) {
 
         if (!aLeaf.isCombineOnMain()) {
             // Determine which table to base partitioning decsions on
 
             try {
                 // see if we already set this when checking for partitioned
                 // condition. If not, get nodes.
                 if (aLeaf.queryNodeList.size() == 0) {
                     SysTable sysTab = SysTable.getPartitionTable(
                             aLeaf.getTableName(), database);
                     if (sysTab.isLookup()) {
                         aLeaf.setLookupStep(true);
                     }
 
                     aLeaf.queryNodeList = new ArrayList(
                             sysTab.getJoinNodeList());
                 }
             } catch (XDBServerException xe) {
                 // We must be dealing with a relation subquery,
                 // with the extra step. Just use all nodes
                 aLeaf.queryNodeList = new ArrayList(
                         this.database.getDBNodeList());
             }
 
             if (levelStatus == 3) {
                 extraLeaf.queryNodeList = aLeaf.queryNodeList;
             }
 
             // We need to add each of these to the main plan, if they are
             // already not there
             for (int i = 0; i < aLeaf.queryNodeList.size(); i++) {
                 DBNode dbNode = aLeaf.queryNodeList.get(i);
 
                 topQueryPlan.queryNodeTable.put(dbNode.getNodeId(), dbNode);
             }
         }
     }
 
     /**
      * This should only be called for an outer join, for the first step in the
      * query.
      *
      * @param prevLeaf
      *            the previous step
      *
      * @return whether or not we need to generate a "serial".
      */
     private boolean needToGenerateSerialForOuter(Leaf prevLeaf) {
         // Check to see if we have a join on the first step due to
         // similarly partitioned tables (or a lookup).
         // Also check if we don't have a unique key for this table.
         // If so, we need to create a new step to make sure we can
         // uniquely identify the rows.
         if (prevLeaf.fromRelationList.size() <= 1) {
             try {
                 SysTable aSysTable = database.getSysTable(prevLeaf.getTableName());
 
                 // Just ensure isTrueRowID is set.
                 aSysTable.getRowID();
 
                 if (aSysTable.isTrueRowID()) {
                     // We have a single base table here, with a reliable key.
                     // We do not need to create an extra step here.
                     return false;
                 }
             } catch (Exception e) {
                 // must be a temp table, need to generate.
                 return true;
             }
         }
 
         return true;
     }
 
     /**
      * Check the case if there is an outer join as part of an aggregate query,
      * with the inner table not appearing in any of the group by columns, just
      * part of an aggregate expression. Example:
      *
      * SELECT a.col1, SUM(b.col2) FROM a LEFT OUTER JOIN b ON a.col1 = b.col1
      * GROUP BY a.col1
      *
      * For the above case, there is no need to create extra steps.
      * @param aQueryTree
      * @return
      */
     private boolean isSpecialOuterAggregateCase(QueryTree aQueryTree) {
 
         boolean specialCase = false;
 
         // Make sure there are only 3 nodes (left, right and join)
         if (aQueryTree.getQueryNodeTable().size() == 3
                 && aQueryTree.isContainsAggregates()) {
             specialCase = true;
 
             for (int i = 0; i < aQueryTree.getProjectionList().size(); i++) {
                 SqlExpression aSqlExpression = aQueryTree.getProjectionList().get(i);
 
                 if (!aSqlExpression.contains(aQueryTree.getRootNode().getRightNode().getRelationNode())) {
                     continue;
                 }
 
                 // At this point, we have a projection in an aggregate outer
                 // query that contains the inner relation.
 
                 // See if it is not an aggregate expresion
                 if (!aSqlExpression.containsAggregates()) {
                     // We need to handle this with extra steps
                     specialCase = false;
                     break;
                 }
 
                 // Make sure expression does not involve other node
                 if (aSqlExpression.contains(aQueryTree.getRootNode().getLeftNode().getRelationNode())) {
                     specialCase = false;
                     break;
                 }
             }
 
         }
         return specialCase;
     }
 
     /**
      * Does additional work for outer join. We add a serial id and nodeid to the
      * temp table we created in the previous step.
      * 
      * TODO: Replace this with function that rebuilds the statements, 
      * putting in NULL as needed. It could possibly require a new setNull 
      * parameter to  SqlExpression.rebuildExpression
      *
      * @param aLeaf
      * @param prevOuterLevel
      * @param aQueryTree
      */
     private void handleOuter(Leaf aLeaf, int prevOuterLevel,
             QueryTree aQueryTree) {
         if (leaves.size() < 2) {
             // tables may be on same step because they are both single
             // node tables.
             return;
         }
 
         /*
          * We also check the case if there is an outer join as part of an
          * aggregate query, with the inner table not appearing in any of the
          * group by columns, just part of an aggregate expression. Example:
          *
          * SELECT a.col1, SUM(b.col2) FROM a LEFT OUTER JOIN b ON a.col1 =
          * b.col1 GROUP BY a.col1
          *
          * For the above case, there is no need to create extra steps
          */
         if (isSpecialOuterAggregateCase(aQueryTree)) {
             return;
         }
 
         Leaf prevLeaf = leaves.get(leaves.size() - 2);
 
         // Update the select statement for outer handling
         // (partial rebuild)
         if (prevLeaf == leaves.get(0)
                 && !needToGenerateSerialForOuter(prevLeaf)) {
             prevLeaf.updateSelectForOuter(this, true);
         } else {
             // We need to get a unique key in the temporary table from
             // a couple of steps ago, so create a serial there.
             prevLeaf.addOuterIdSerial(this);
             prevLeaf.setSerialColumnPosition((short) prevLeaf.selectColumns.size());
             prevLeaf.updateSelectForOuter(this, false);
         }
 
         Leaf outerLeaf1 = aLeaf.createDerivedOuterLeaf();
         outerLeaf1.lastOuterLevel = prevOuterLevel;
 
         // Add required projections.
 
         // We need to add the columns that make up the unique key
         for (SqlExpression outerIdSqlExpr :
                 prevLeaf.createSqlExprFromOuterIdColumns(aLeaf.getJoinTableName())) {
 
             outerLeaf1.appendProjectionFromExpr(outerIdSqlExpr, false);
         }
 
         // Add XONODEID last
         ExpressionType exprType = new ExpressionType();
         exprType.setExpressionType(ExpressionType.INT_TYPE, 0, 0, 0);
 
         AttributeColumn anAC = new AttributeColumn();
         anAC.setColumnName(prevLeaf.getOuterNodeIdColumn());
         anAC.setTableAlias(aLeaf.getJoinTableName());
         anAC.setTableName(aLeaf.getJoinTableName());
         anAC.columnType = exprType;
 
         SqlExpression aSqlExpr = new SqlExpression();
         aSqlExpr.setExprType(SqlExpression.SQLEX_COLUMN);
         aSqlExpr.setColumn(anAC);
         aSqlExpr.setExprDataType(exprType);
 
         outerLeaf1.appendProjectionFromExpr(aSqlExpr, false);
 
         aLeaf.outerSubplan = new QueryPlan(this.client);
         aLeaf.outerSubplan.topQueryPlan = this.topQueryPlan;
 
         outerLeaf1.determineSelectStatement(aLeaf.outerSubplan);
 
         // modify select statement to just get outered rows
         if (outerLeaf1.getSelectStatement().indexOf(" WHERE ") > 0) {
             outerLeaf1.setSelectStatement(outerLeaf1.getSelectStatement() + " AND " + outerLeaf1.getOuterTestString());
         } else {
             outerLeaf1.setSelectStatement(outerLeaf1.getSelectStatement() + " WHERE " + outerLeaf1.getOuterTestString());
         }
 
         // We have a problem in that it is already using new table names.
         // It should use the old one
         outerLeaf1.setSelectStatement(outerLeaf1.getSelectStatement()
                 .replaceAll(aLeaf.getTargetTableName(),
                         aLeaf.getJoinTableName()));
 
         aLeaf.outerSubplan.leaves.add(outerLeaf1);
 
         logger.debug("Outer Leaf 1");
         logger.debug(outerLeaf1.toString());
 
         // ------------------------------------
         // Now handle the second step
         // ------------------------------------
 
         // Get column names
         String subqColumnString = new String();
 
         for (Leaf.Projection aColumn : prevLeaf.selectColumns) {
 
             String colAlias = aColumn.getCreateColumnName();
 
             if (aColumn.projectString.startsWith("XONODEID")) {
                 continue;
             }
             if (subqColumnString.length() > 0) {
                 subqColumnString += ", ";
             }
 
             if (colAlias != null && colAlias.length() > 0) {
                 subqColumnString += colAlias;
             } else {
                 subqColumnString += aColumn.projectString;
             }
         }
 
         // we need to do some substitutions.
         // We want to replace base table values with NULL,
         // since we did not find any results.
 
         // we need to determine select statement prematurely
         aLeaf.determineSelectStatement(this);
 
         // We will replace &xnodecount& in ExecutionStep
         StringBuilder sbSubSelect = new StringBuilder(128);
 
         sbSubSelect.append("(SELECT " + prevLeaf.getOuterColumnSelectString())
                 .append(", ").append(IdentifierHandler.quote(prevLeaf.getOuterNodeIdColumn()))
                 .append(", COUNT(*)")
                 .append(" FROM ")
                 .append(IdentifierHandler.quote(outerLeaf1.getTargetTableName()))
                 .append(" GROUP BY ")
                 .append(prevLeaf.getOuterColumnSelectString())
                 .append(", ")
                 .append(IdentifierHandler.quote(prevLeaf.getOuterNodeIdColumn()))
                 .append(" HAVING COUNT(*) = &xnodecount&) as xsubselect ");
 
         String subSelect = sbSubSelect.toString();
 
         int pos = aLeaf.getSelectStatement().toUpperCase().indexOf(" FROM ");
 
         String projString = aLeaf.getSelectStatement().substring(0, pos);
 
         int groupPos = aLeaf.getSelectStatement().toUpperCase().indexOf(" GROUP BY ");
         String groupString = null;
 
         if (groupPos > 0) {
             groupString = aLeaf.getSelectStatement().substring(groupPos + 10);
         }
 
         // For the second statement (in the union), we need to also add
         // the WHERE clause of the first, but substitute NULLs as
         // appropriate.
         String otherWhereClause = aLeaf.getWhereClause();
 
         // TODO: improve code here, this will work in many but not all situations
         // Do some substituions, replacing NULL for the inner table.
         // We also look for sum and avg- these need to be nulled, too
         // It is ok to do count(null)
         for (FromRelation subRelation : aLeaf.fromRelationList) {
             String baseTable = subRelation.getAlias();
 
             //if (baseTable.toUpperCase().startsWith(Props.XDB_TEMPTABLEPREFIX)) {
             //    continue;
             //}
 
             projString = replaceColumnsBasedOnTable(projString,
                     IdentifierHandler.quote(baseTable),
                     "NULL");
             otherWhereClause = replaceColumnsBasedOnTable(otherWhereClause,
                     IdentifierHandler.quote(baseTable), "NULL");
 
             projString = projString.replaceAll("sum\\(NULL\\)", "NULL");
             projString = projString.replaceAll("avg\\(NULL\\)", "NULL");
             projString = projString.replaceAll("sum\\( NULL\\)", "NULL");
             projString = projString.replaceAll("avg\\( NULL\\)", "NULL");
 
             // Do the same for group by
             if (groupPos > 0) {
                 groupString = replaceColumnsBasedOnTable(groupString,
                         IdentifierHandler.quote(baseTable)
                         + ",", "");
                 groupString = replaceColumnsBasedOnTable(groupString,
                         IdentifierHandler.quote(baseTable), "__deleteme__");
 
                 groupString = groupString.replaceAll("__deleteme__,", "");
                 //again, in case last item, without comma
                 //groupString = groupString.replaceAll(", __deleteme__", "");
                 //any remaining ones, just make NULL
                 groupString = groupString.replaceAll("__deleteme__", "NULL");
                 
                 if (groupString.toUpperCase().indexOf(" HAVING") >= 0) {
                     groupString = groupString.replaceAll("sum\\(NULL\\)",
                             "NULL");
                     groupString = groupString.replaceAll("avg\\(NULL\\)",
                             "NULL");
                     groupString = groupString.replaceAll("sum\\( NULL\\)",
                             "NULL");
                     groupString = groupString.replaceAll("avg\\( NULL\\)",
                             "NULL");
                 }
             }
         }
 
         String otherSelectStatement = projString + " FROM "
                 + IdentifierHandler.quote(prevLeaf.getTargetTableName())
                 + " INNER JOIN " + subSelect
                 + " ON " + IdentifierHandler.quote(prevLeaf.getTargetTableName()) + "."
                 + IdentifierHandler.quote(prevLeaf.getOuterNodeIdColumn()) + " = xsubselect."
                 + IdentifierHandler.quote(prevLeaf.getOuterNodeIdColumn());
 
         for (String element : prevLeaf.getOuterColumnSelectString().split(",")) {
             otherSelectStatement += " AND " + IdentifierHandler.quote(prevLeaf.getTargetTableName()) + "."
                     + element + " = xsubselect." + element;
         }
 
         // For the second statement (in the union), we need to also add
         // the WHERE clause of the first, but substitute NULLs as
         // appropriate.
         otherSelectStatement += otherWhereClause;
 
         if (groupPos > 0 && groupString.trim().length() > 0) {
             otherSelectStatement += " GROUP BY " + groupString;
         }
 
         // We now want the original first step to now be inner,
         // since we handle the outer rows separately.
         aLeaf.setSelectStatement(aLeaf.getSelectStatement().replaceFirst(
                 "LEFT OUTER", "INNER"));
         aLeaf.setNonProjectionSelectPart(aLeaf.getNonProjectionSelectPart().replaceFirst(
                 "LEFT OUTER", "INNER"));
 
         if (aLeaf.isDistinct()) {
             aLeaf.setSelectStatement(aLeaf.getSelectStatement() + " UNION " + otherSelectStatement);
             aLeaf.setNonProjectionSelectPart(aLeaf.getNonProjectionSelectPart()
                     + " UNION " + otherSelectStatement);
         } else {
             aLeaf.setSelectStatement(aLeaf.getSelectStatement() + " UNION ALL " + otherSelectStatement);
             aLeaf.setNonProjectionSelectPart(aLeaf.getNonProjectionSelectPart()
                     + " UNION ALL " + otherSelectStatement);
         }
     }
 
     /**
      * This is used for OUTER joins. We want to replace table.column with NULL
      * @param inputString
      * @param replaceTable
      * @param replaceString
      * @return
      */
     public String replaceColumnsBasedOnTable(String inputString,
             String replaceTable, String replaceString) {
 
         if (inputString == null || inputString.length() == 0
                 || replaceTable == null || replaceTable.length() == 0) {
             return inputString;
         }
 
         String targetString = new String(inputString);
 
         for (int pos = targetString.toUpperCase().indexOf(replaceTable.toUpperCase());
                 pos >= 0;
                 pos = targetString.toUpperCase().indexOf(replaceTable.toUpperCase())) {
             if (pos > 0) {
                 // check that it really is the start
                 char prevChar = targetString.charAt(pos - 1);
 
                 if (prevChar != ' ' && prevChar != '(' && prevChar != ',') {
                     continue;
                 }
             }
 
             int endPos = targetString.length();
 
             int endPos1 = targetString.indexOf(" ", pos + 1);
             if (endPos1 > -1 && endPos1 < endPos) {
                 endPos = endPos1;
             }
 
             endPos1 = targetString.indexOf(")", pos + 1);
             if (endPos1 > -1 && endPos1 < endPos) {
                 endPos = endPos1;
             }
 
             endPos1 = targetString.indexOf(",", pos + 1);
             if (endPos1 > -1 && endPos1 < endPos) {
                 endPos = endPos1;
             }
 
             if (endPos < targetString.length()) {
                 targetString = targetString.substring(0, pos) + replaceString
                         + targetString.substring(endPos);
             } else {
                 targetString = targetString.substring(0, pos) + replaceString;
             }
         }
         return targetString;
     }
 
     /**
      * Handles when we encounter a correlated subquery in the tree. It in turn
      * will also create a subplan for it.
      *
      * @param aQueryNode the QueryNode represnting the correlated subquery link in the
      *            main tree.
      * @param aLeaf the target Leaf
      * @throws org.postgresql.stado.exception.XDBServerException
      */
     private void createPlanStepCorrelated(QueryNode aQueryNode, Leaf aLeaf)
             throws XDBServerException {
         final String method = "createPlanStepCorrelated";
         logger.entering(method);
 
         try {
             QueryTree subTree;
             QueryNode subTreeNode;
             String selectString = "";
             String tableColumnString = "";
 
             //  first we build up strings for handling
             // correlated table
             aLeaf.setCorrelatedSelectString("");
 
             // track names to avoid dupes
             HashMap colMap = new HashMap();
 
             for (AttributeColumn anAC : aQueryNode.getRightNode().getRelationNode().getCorrelatedColumnList()) {
                 String columnName = anAC.columnName;
 
                 if (anAC.tempColumnAlias != null
                         && anAC.tempColumnAlias.length() > 0) {
                     columnName = anAC.tempColumnAlias;
                 }
 
                 // don't add column name twice
                 if (colMap.containsKey(columnName)) {
                     continue;
                 } else {
                     colMap.put(columnName, null);
                 }
 
                 if (tableColumnString.length() > 0) {
                     tableColumnString += ", ";
                     selectString += ", ";
                 }
 
                 try {
                     tableColumnString += IdentifierHandler.quote(columnName) + " "
                             + anAC.columnType.getTypeString();
                 } catch (Exception e) {
                     throw new XDBServerException(
                             ErrorMessageRepository.ILLEGAL_COLUMN_TYPE
                                     + anAC.columnName, e,
                             XDBServerException.SEVERITY_HIGH);
                 }
 
                 selectString += IdentifierHandler.quote(columnName);
             }
 
             // We just want to pass down distinct values
             aLeaf.setCorrelatedSelectString("SELECT DISTINCT " + selectString
                     + " FROM " + IdentifierHandler.quote(lastJoinTableName));
 
             // We use the same table name, with "A" appended to it
             // down in the subquery.
             // Note that it might have a different structure in the subquery,
             // since it only contains required columns
 
             if (aQueryNode.getLeftNode() != null) {
                 updateQueryNodeTempTableName(aQueryNode.getLeftNode(),
                         aLeaf.correlatedJoinTableName);
             }
 
             if (Props.XDB_USE_LOAD_FOR_STEP) {
                 aLeaf.setCreateCorrelatedTableString(Props.XDB_SQLCOMMAND_CREATEGLOBALTEMPTABLE_START
                         + " "
                         + IdentifierHandler.quote(aLeaf.correlatedJoinTableName)
                         + " ("
                         + tableColumnString
                         + ") "
                         + Props.XDB_SQLCOMMAND_CREATEGLOBALTEMPTABLE_SUFFIX);
             } else {
                 aLeaf.setCreateCorrelatedTableString(Props.XDB_SQLCOMMAND_CREATETEMPTABLE_START
                         + " "
                         + IdentifierHandler.quote(aLeaf.correlatedJoinTableName)
                         + " ("
                         + tableColumnString
                         + ") "
                         + Props.XDB_SQLCOMMAND_CREATETEMPTABLE_SUFFIX);
             }
             // ---------------------------------------------------------
             logger.debug(" plan for correlated");
 
             subTreeNode = aQueryNode.getRightNode();
             subTree = subTreeNode.getRelationNode().getSubqueryTree();
 
             QueryPlan aQueryPlan = new QueryPlan(client);
             aQueryPlan.topQueryPlan = this.topQueryPlan;
 
             aQueryPlan.tempIdCounter = this.tempIdCounter; // init stepNo
             aQueryPlan.planType = CORRELATED;
 
             // Create a new subplan off of Leaf
             logger.debug("+++ Creating correlated subplan.");
 
             // unionDepth is unimportant here- just set to 0
             aQueryPlan.createPlanSegmentFromTree(subTree, true,
                     aLeaf.correlatedJoinTableName, 0);
 
             trackTable(aQueryPlan.finalTableName, aLeaf);
 
             // Set back correct relation name
             if (aQueryNode.getLeftNode() != null) {
                 updateQueryNodeTempTableName(aQueryNode.getLeftNode(),
                         lastJoinTableName);
             }
 
             logger.debug("+++ Finished correlated subplan.");
 
             subTreeNode.getRelationNode().setTableName(aQueryPlan.finalTableName);
 
             aLeaf.subplan = aQueryPlan;
 
             createPlanStepSubtree(subTreeNode, aLeaf);
 
             this.tempIdCounter = aQueryPlan.tempIdCounter; // save stepNo
 
             logger.debug("final = " + aQueryPlan.finalTableName);
             logger.debug("right table: "
                     + aQueryNode.getRightNode().getRelationNode().getTableName());
         } finally {
             logger.exiting(method);
         }
     }
 
     /**
      * Processes QueryConditions for the step. In particular, it handles joins.
      * Note that it includes special handling for OUTER joins as well.
      *
      * @param aQueryNode
      *            QueryNode to process
      * @param aLeaf
      *            the (step) Leaf that corrsponds to the QueryNode
      * @param joinCondGroup
      *            for handling outers, first step
      * @param joinCondGroup2
      *            for handling outers, second step
      */
     private void createPlanStepDoConditions(QueryNode aQueryNode, Leaf aLeaf,
             List<QueryCondition> joinCondGroup, List<QueryCondition> joinCondGroup2) {
         final String method = "createPlanStepDoConditions";
         logger.entering(method);
 
         try {
             QueryCondition aQueryCondition;
             int lastLevel;
             SortedVector sortedList = new SortedVector();
 
             // group those that can be processed together
             joinCondGroup = new ArrayList<QueryCondition>();
             joinCondGroup2 = new ArrayList<QueryCondition>();
 
             // We have an issue where for queries like this:
             // select count(*) from customer left outer join orders
             // on o_custkey = c_custkey and o_orderkey = c_custkey;
             // we are getting one single QueryCondition.
             // We should really have 2 separate conditions.
             // So, we loop through once and try and fix these.
             List<QueryCondition> newConds = new ArrayList<QueryCondition>();
 
             for (int i = 0; i < aQueryNode.getConditionList().size(); i++) {
                 aQueryCondition = aQueryNode.getConditionList().get(i);
 
                 newConds.addAll(aQueryCondition.getAndedConditions());
             }
 
             /*
              * RelationNodeList is not set on ANDed conditions. Populate them
              * now, in case if they have been exposed.
              */
             for (QueryCondition newCond : newConds) {
                 for (QueryCondition aQC : QueryCondition.getNodes(newCond,
                         QueryCondition.QC_SQLEXPR)) {
                     for (SqlExpression aSqlExpression : SqlExpression.getNodes(
                             aQC.getExpr(), SqlExpression.SQLEX_COLUMN)) {
                         AttributeColumn anAC = aSqlExpression.getColumn();
 
                         if (!newCond.getRelationNodeList().contains(
                                 anAC.relationNode)) {
                             newCond.getRelationNodeList()
                                     .add(anAC.relationNode);
                         }
                     }
                 }
             }
 
             aQueryNode.setConditionList(newConds);
 
             for (int i = 0; i < aQueryNode.getConditionList().size(); i++) {
                 aQueryCondition = aQueryNode.getConditionList().get(i);
                 if (aQueryCondition.isJoin()
                         && aQueryCondition.getRelationNodeList().size() > 0) {
                     int nodeLevel = 0;
                     RelationNode node1, node2;
 
                     aQueryCondition.rebuildCondString();
 
                     try {
                         node1 = aQueryCondition.getRelationNodeList().get(0);
                         nodeLevel = node1.getOuterLevel();
 
                         // only get into outer checking if more than one
                         // relation node
                         // is involved (may just be one for subqueries)
                         if (aQueryCondition.getRelationNodeList().size() != 2
                                 || aQueryNode.getRightNode() == null) {
                             // sortedList.addElement (0, aQueryCondition);
                             joinCondGroup.add(aQueryCondition);
                         } else if (aQueryCondition.getRelationNodeList().size() == 2) {
                             node2 = aQueryCondition.getRelationNodeList().get(1);
 
                             if (aQueryNode.getRightNode().subtreeContains(node1)) {
                                 // make node1 our folded node
                                 RelationNode swapNode;
 
                                 swapNode = node1;
                                 node1 = node2;
                                 node2 = swapNode;
 
                                 nodeLevel = node1.getOuterLevel();
                             } else {
                                 if (aQueryNode.getRightNode().subtreeContains(node2)) {
                                     nodeLevel = node1.getOuterLevel();
                                 }
                             }
 
                             // Assume we work our way from out to in?
                             logger.debug(" nodeLevel = " + nodeLevel);
                             sortedList.addElement(nodeLevel, aQueryCondition);
 
                             if (aQueryCondition.isSimpleTableJoin()
                                     && !aQueryNode.isTreeDownJoin()) {
                                 // Now check if left side is partitioned column
                                 AttributeColumn anAC = aQueryCondition.getExpr().getLeftExpr().getColumn();
 
                                 if (aQueryNode.getRightNode().subtreeContains(anAC.relationNode)
                                         && anAC.isPartitionColumn()) {
                                     // hash based on right side.
                                     aLeaf.setHashInfo(
                                             anAC.getTableName(),
                                             aQueryCondition.getExpr().getRightExpr().getExprString());
                                 } else {
                                     // Now check if right side is partitioned
                                     // column
                                     anAC = aQueryCondition.getExpr().getRightExpr().getColumn();
 
                                     if (aQueryNode.getRightNode().subtreeContains(anAC.relationNode)
                                             && anAC.isPartitionColumn()) {
                                         // hash based on left side.
                                         aLeaf.setHashInfo(
                                             anAC.getTableName(),
                                             aQueryCondition.getExpr().getLeftExpr().getExprString());
                                     }
                                 }
                             }
 
                             // if we have outer case and not joining on hashed
                             // column,
                             // we need to do extra work.
                             // We get outer expressions
                             if (node1.getOuterLevel() != node2.getOuterLevel()
                                     && aLeaf.getHashColumn() == null) {
                                 // we want to save expression
                                 boolean savedOk = false;
 
                                 if (aQueryCondition.getExpr().getLeftExpr() != null
                                         && aQueryCondition.getExpr().getRightExpr() != null) {
                                     if (aQueryCondition.getExpr().getLeftExpr().contains(node1)
                                             && aQueryCondition.getExpr().getRightExpr().contains(node2)) {
                                         if (aLeaf.outerExprList == null) {
                                             aLeaf.outerExprList = new ArrayList<SqlExpression>();
                                             aLeaf.innerExprList = new ArrayList<SqlExpression>();
                                         }
                                         aLeaf.outerExprList.add(aQueryCondition.getExpr().getLeftExpr());
                                         aLeaf.innerExprList.add(aQueryCondition.getExpr().getRightExpr());
                                         savedOk = true;
 
                                     } else if (aQueryCondition.getExpr().getLeftExpr().contains(node2)
                                             && aQueryCondition.getExpr().getRightExpr().contains(node1)) {
                                         if (aLeaf.outerExprList == null) {
                                             aLeaf.outerExprList = new ArrayList<SqlExpression>();
                                             aLeaf.innerExprList = new ArrayList<SqlExpression>();
                                         }
                                         aLeaf.outerExprList.add(aQueryCondition.getExpr().getRightExpr());
                                         aLeaf.innerExprList.add(aQueryCondition.getExpr().getLeftExpr());
                                         savedOk = true;
                                     }
                                 }
 
                                 if (!savedOk) {
                                     throw new XDBServerException(
                                             "This outer join condition too complex and is currently not supported. Try simplifying the outer or using temp tables over multiple steps.");
                                 }
                             }
                         }
                     } catch (Exception e) {
                         throw new XDBServerException(
                                 ErrorMessageRepository.INVALID_JOIN, e,
                                 ErrorMessageRepository.INVALID_JOIN_CODE);
                     }
                 } else {
                     // See if the condition is a constant compared to
                     // a partitioned column, then add to queryNodeList
                     Collection<DBNode> aDBNodes = aQueryCondition.getPartitionedNode(client);
 
                     if (aDBNodes != null) {
                         aLeaf.queryNodeList = new ArrayList<DBNode>(aDBNodes);
                     } else {
                         // See if we are dealing with PreparedStatement
                         // parameters
                         aLeaf.setPartitionParameterExpression(aQueryCondition.getPartitionParameterExpression(database));
                     }
 
                 }
             }
 
             /*
              * Now that we have our list of join conditions sorted by outer
              * level, we examine them
              */
 
             lastLevel = -1;
 
             for (int i = 0; i < sortedList.size(); i++) {
                 QueryCondition aQC = (QueryCondition) sortedList.get(i);
 
                 logger.debug("outerLevel = " + aQueryNode.getRightNode().getOuterLevel());
 
                 if (lastLevel == -1) // || lastLevel ==
                 // sortedList.getKeyAt(i))
                 {
                     // see if we are on the same level.
 
                     if (sortedList.getKeyAt(i) == aQueryNode.getRightNode().getOuterLevel()) {
 
                         levelStatus = 1; // all same level
                     } else {
                         levelStatus = 2; // all different levels
                     }
                     joinCondGroup.add(aQC);
                 } else {
                     if (levelStatus == 1) {
                         if (sortedList.getKeyAt(i) == aQueryNode.getRightNode().getOuterLevel()) {
                             // same level
                             joinCondGroup.add(aQC);
                         } else {
                             levelStatus = 2;
                             joinCondGroup.add(aQC);
                         }
                     } else {
                         joinCondGroup.add(aQC);
                     }
                 }
                 logger.debug("levelStatus = " + levelStatus);
 
                 // Swap these.
                 if (levelStatus == 3) {
                     List<QueryCondition> tempJCG = joinCondGroup;
 
                     joinCondGroup = joinCondGroup2;
                     joinCondGroup2 = tempJCG;
                 }
                 // save for comparison
                 lastLevel = sortedList.getKeyAt(i);
             }
 
             // See if we had the case of either all conditions joining on
             // the same level, or across multiple.
             // If so, just add the conditions as we normally would.
             // (otherwise, create new step).
 
             // Don't re-add these if we already did as part of a
             // preserved subtree
             if (!aQueryNode.isPreserveSubtree()) {
                 for (int i = 0; i < joinCondGroup.size(); i++) {
                     aQueryCondition = joinCondGroup.get(i);
 
                     if (aQueryCondition.isJoin() && !aQueryCondition.isInPlan()) {
                         // rebuild, we may have made changes for subqueries
                         aQueryCondition.rebuildCondString();
 
                         String joinString = aQueryCondition.getCondString();
 
                         // join is a bit of a misnomer here.
                         aLeaf.addJoin(aQueryCondition, joinString);
                         aQueryCondition.setInPlan(true);
                         logger.debug(" - joincond 3: " + joinString);
                     }
                 }
             }
 
             // Handle complex outer (currently not used)
             if (levelStatus == 3) {
                 // We need to create a second leaf
                 aLeaf2 = new Leaf();
                 aLeaf2.setLeafStepNo(++this.currentLeafStepNo);
 
                 // We go ahead and update the temp table names so they will
                 // be correct in the next step
                 // updateQueryNodeTempTableName (aQueryNode,
                 // aLeaf.targetTableName);
 
                 // aLeaf2.depth = aLeaf.depth;
                 aLeaf2.setDistinct(aLeaf.isDistinct());
                 aLeaf2.setJoinTableName(aLeaf.getJoinTableName()); // yes, reuse!
                 aLeaf2.setTableName(aLeaf.getTargetTableName()); //
 
                 trackTable(aLeaf2.getJoinTableName(), aLeaf2);
                 trackTable(aLeaf2.getTableName(), aLeaf2);
 
                 logger.debug("ext add B, table: " + aLeaf.getTargetTableName());
                 // Given new outer scheme, force this to be a
                 // higher outerlevel, add +1
                 aLeaf2.fromRelationList.add (new FromRelation(
                         aLeaf.getTargetTableName(),
                         aLeaf.getTargetTableName(),
                         true,
                         aQueryNode.getRightNode().getOuterLevel() + 1,
                         aQueryNode.getRightNode().getRelationNode() != null ? aQueryNode.getRightNode().getRelationNode().isOnly() : false));                      
                 aLeaf2.setTargetTableName(generateTempTableName());
                 aLeaf2.setExtraStep(true);
 
                 for (int i = 0; i < joinCondGroup2.size(); i++) {
                     aQueryCondition = joinCondGroup2.get(i);
 
                     if (aQueryCondition.isJoin() && !aQueryCondition.isInPlan()) {
                         // rebuild, we may have made changes for subqueries
                         aQueryCondition.rebuildCondString();
                         String sExpr = aQueryCondition.getCondString();
 
                         if (sExpr.indexOf(aLeaf.getTableName() + ".") >= 0) {
                             sExpr = ParseCmdLine.replace(sExpr, aLeaf.getTableName(), aLeaf.getTargetTableName());
                         }
 
                         // join is a bit of a misnomer here.
                         aLeaf2.addJoin(aQueryCondition, sExpr);
                         aQueryCondition.setInPlan(true);
                         logger.debug(" - joincond 4: " + sExpr);
                     }
                 }
             }
 
             // Need to add more complex conditions like OR
             for (int i = 0; i < aQueryNode.getConditionList().size(); i++) {
                 aQueryCondition = aQueryNode.getConditionList().get(i);
 
                 // See if we have not yet added it
                 if (!aQueryCondition.isJoin() && !aQueryCondition.isInPlan()) {
                     if (Props.XDB_CONSTANT_EXPRESSION_THRESHOLD >= 0
                             && aQueryNode.getBaseNumRows() > Props.XDB_CONSTANT_EXPRESSION_THRESHOLD) {
                         evaluateConstantExpressions(aQueryCondition);
                     }
 
                     // rebuild, we may have made changes for subqueries
                     aQueryCondition.rebuildCondString();
                     logger.debug(" - joincond 5: " + aQueryCondition.getCondString());
 
                     aLeaf.addCondition(aQueryCondition.getCondString());
                     aQueryCondition.setInPlan(true);
                 }
             }
         } finally {
             logger.exiting(method);
         }
     }
 
     /**
      * For the current step being processed, determines which expressions and
      * columns to include in the SELECT clause
      *
      * @param aQueryTree
      *            the current QueryTree
      * @param aQueryNode
      *            the QueryNode being processed
      * @param aLeaf
      *            the target Leaf info
      */
     private void createPlanStepDoProjections(QueryTree aQueryTree,
             QueryNode aQueryNode, Leaf aLeaf) {
         final String method = "createPlanStepDoProjections";
         logger.entering(method);
 
         try {
             logger.debug("createPlanStepDoProjections()");
 
             if (aQueryNode.getParent() == null
                     && (!aQueryTree.isContainsAggregates()
                             || aQueryTree.isPartitionedGroupBy() || aQueryTree.usesSingleDBNode())
                     && !aQueryTree.isCorrelatedSubtree()) {
                 adjustFinalProjections(
                         aLeaf,
                         aQueryTree.getProjectionList(),
                         (aQueryTree.getQueryType() & QueryTree.NONCORRELATED) > 0);
 
                 // See if in final step, we created an extra Leaf and need
                 // to adjust projections in it, too.
                 if (levelStatus == 3) {
                     // Now, update these by hand
                     aLeaf2.setCombineOnMain(true);
 
                     for (SqlExpression aSqlExpression : aQueryNode.getProjectionList()) {
 
                         aSqlExpression.rebuildExpression();
 
                         String sExpr = aSqlExpression.getExprString();
 
                         if (sExpr.indexOf(aLeaf.getJoinTableName() + ".") < 0) {
                             // We have some special handling here, which also
                             // helps if we are the final step and our
                             // projections get
                             // replaced by the ones in the main QueryTree.
                             // We want to not just update the expression here,
                             // but also do it for the underlying SqlExpression
                             int p = sExpr.indexOf(".");
 
                             aSqlExpression.setExprString(aLeaf.getTargetTableName()
                                     + "." + sExpr.substring(p + 1));
 
                             // We don't want to rebuild this expr anymore
                             aSqlExpression.setTempExpr(true);
 
                             // Set alias for appendSelectColumnFromExpr
                             if (aSqlExpression.getAlias().length() == 0) {
                                 aSqlExpression.setAlias(sExpr.substring(p + 1));
                             }
                         }
 
                         aLeaf2.appendProjectionFromExpr(aSqlExpression, false);
                     }
                 }
             } else {
                 for (SqlExpression aSqlExpression : aQueryNode.getProjectionList()) {
                     // Don't add constants here- wait until final step
                     if (aSqlExpression.isConstantExpr()
                             && aQueryNode.getParent() != null) {
                         continue;
                     }
 
                     aSqlExpression.rebuildExpression();
 
                     if (!aLeaf.isProjection(aSqlExpression.getExprString())
                             || aSqlExpression.containsAggregates()
                             || aSqlExpression.isTempExpr()) {
                         aLeaf.appendProjectionFromExpr(aSqlExpression, false);
                     }
 
                     // need to check for underlying table and substitute
                     if (levelStatus == 3) // We need to add it for second
                     // query
                     {
                         // see if we need to substitute in second join,
                         // taking the tablename and replacing it with the 2nd
                         // target.
                         // ex:
                         // select a.col1, b.col2, c.col3
                         // from a, outer (b,c)
                         // where a.x = b.x
                         // and b.y = c.y
                         // and a.z = c.z
                         //
                         // we end up doing something like:
                         //
                         // Into t1:
                         // select a.col1, b.col2, b.y
                         // from a outer b
                         // where a.x = b.x
                         //
                         // Into t2:
                         // select t1.col, t1.col2, c.col3, c.z
                         // from t1, c
                         // where t1.y = c.y
                         //
                         // Into t3 (extra step- Leaf2!)
                         // select t1.col1, t1.col2, t2.col3
                         // from t1 outer t2
                         // where t1.z = t2.z
                         //
                         // Table substitution is normally done at the end,
                         // but we do it here for this special case
 
                         String sExpr = aSqlExpression.getExprString();
 
                         // If we don't find orig join table name, it must be our
                         // added
                         if (sExpr.indexOf(aLeaf.getJoinTableName() + ".") < 0) {
                             int p = sExpr.indexOf(".");
 
                             aSqlExpression.setExprString(aLeaf.getTargetTableName()
                                     + "." + sExpr.substring(p + 1));
 
                             sExpr = aSqlExpression.getExprString();
                         }
 
                         aLeaf2.appendProjectionFromExpr(aSqlExpression, false);
 
                         aLeaf2.setCombineOnMain(true);
                     }
 
                     logger.debug(" col: " + aSqlExpression.getExprString());
                 }
             }
         } finally {
             logger.exiting(method);
         }
     }
 
     /**
      * Handles processing of the group by list for the current step
      *
      * @param aQueryTree
      *            the QueryTree being processed
      * @param aQueryNode
      *            the QueryNode being processed
      * @param aLeaf
      *            the target Leaf info
      */
     private void createPlanStepDoGroupByList(QueryTree aQueryTree,
             QueryNode aQueryNode, Leaf aLeaf) {
         final String method = "createPlanStepDoGroupByList";
         logger.entering(method);
 
         try {
             SqlExpression aSqlExpression;
             Leaf.Projection aColumn;
             String stepColName;
 
             for (int i = 0; i < aQueryNode.getGroupByList().size(); i++) {
                 aSqlExpression = aQueryNode.getGroupByList().get(i);
 
                 aSqlExpression.rebuildExpression();
 
                 if (!subplanList.isEmpty()) {
                     checkScalarExpression(aSqlExpression.getExprString(), aLeaf);
                 }
 
                 // Make sure that we do not double add
                 if (aLeaf.isGroupByColumn(aSqlExpression.getExprString())) {
                     continue;
                 } else {
                     aColumn = aLeaf.new Projection(aSqlExpression.getExprString());
 
                     // Don't add if aggregating by "slow" method
                     if (useSlowAggregation
                             && !aQueryTree.isPartitionedGroupBy()) {
                         aLeaf.setSuppressedGroupBy(true);
                     }
 
                     // We want to make sure this column gets quoted later
                     if (aSqlExpression.isTempExpr() && aSqlExpression.getExprType() == SqlExpression.SQLEX_COLUMN) {
                         aColumn.forceGroupQuote = true;
                     }
 
                     aLeaf.groupByColumns.add(aColumn);
                 }
 
                 // Also add to projection list if it is not yet there
                 if (!aLeaf.isProjection(aSqlExpression.getExprString())) {
                     stepColName = aLeaf.appendProjectionFromExpr(
                             aSqlExpression, false);
 
                     aColumn.setCreateColumnName(stepColName);
 
                     aColumn.groupByPosition = aLeaf.selectColumns.size();
                 } else {
                     stepColName = "";
 
                     int j = 0;
                     for (Leaf.Projection compProjection : aLeaf.selectColumns) {
                         j++;
                         if (compProjection.projectString.compareToIgnoreCase(aSqlExpression.getExprString()) == 0) {
                             stepColName = compProjection.getCreateColumnName();
 
                             aColumn.setCreateColumnName(stepColName);
                             aColumn.groupByPosition = j;
                             break;
                         }
                     }
 
                     if (stepColName.length() == 0) {
                         throw new XDBServerException(
                                 ErrorMessageRepository.LOOKUP_ERROR
                                         + "( GroupByList "
                                         + aSqlExpression.getExprString() + " )",
                                 XDBServerException.SEVERITY_HIGH,
                                 ErrorMessageRepository.LOOKUP_ERROR_CODE);
                     }
 
                 }
 
                 // Go ahead and set up the "Columns" for the additional
                 // step's group by, based on the column name here
 
                 // Don't add to extra step's group
                 // if special handling for cases like SELECT COUNT (DISTINCT x)
                 if (!aSqlExpression.isDistinctExtraGroup()) {
                     logger.debug("Extra Group By: " + stepColName);
 
                     aColumn = aLeaf.new Projection(stepColName);
 
                     // We want to make sure this column gets quoted later
                     aColumn.forceGroupQuote = true;
 
                     extraStepGroupByList.add(aColumn);
                 } else {
                     // track thse added columns so that StepDetail.java can
                     // figure out if it is just a special case
                     aLeaf.incrementAddedGroupCount();
                 }
             }
         } finally {
             logger.exiting(method);
         }
     }
 
     /**
      * Update the QueryNodes temp table name (used for table name substitution).
      * As we complete processing each node, we update what temp table the
      * results currently reside in. This provides a simple mechanism for
      * tracking when we can drop temp tables.
      *
      * @param aQueryNode
      *            the QueryNode being processed
      * @param tempTableName
      *            the temp table that now contains the node's info
      */
     private void updateQueryNodeTempTableName(QueryNode aQueryNode,
             String tempTableName) {
         final String method = "updateQueryNodeTempTableName";
         logger.entering(method);
 
         try {
 
             logger.debug(" -- " + tempTableName);
 
             if (aQueryNode.getNodeType() == QueryNode.JOIN) {
                 updateQueryNodeTempTableName(aQueryNode.getLeftNode(), tempTableName);
                 updateQueryNodeTempTableName(aQueryNode.getRightNode(),
                         tempTableName);
             } else {
                 if (aQueryNode.getRelationNode().getNodeType() == RelationNode.TABLE) {
                     aQueryNode.getRelationNode().setCurrentTempTableName(tempTableName);
                 } else if (aQueryNode.getRelationNode().getNodeType() == RelationNode.SUBQUERY_RELATION) {
                 //} else if (aQueryNode.getRelationNode().getNodeType() == RelationNode.SUBQUERY_RELATION 
                 //        && (!aQueryNode.isWith() || aQueryNode.getParent() != null)) {
                     aQueryNode.getRelationNode().setCurrentTempTableName(tempTableName);
                 }
             }
         } finally {
             logger.exiting(method);
         }
     }
 
     /**
      * Handles uncorrelated subqueries
      *
      * @param subTreeNode
      *            the node in parent tree that contains subquery
      * @param aLeaf
      *            the target Leaf
      * @param isInClause
      *            whether or not IN clause being used
      * @param inClausePartTable
      *            if IN clause, the partition table to be used
      * @param inClausePartColumn
      *            if IN clause, the partition column to use
      */
     private void createPlanStepUncorrelated(RelationNode subTreeNode,
             Leaf aLeaf, boolean isInClause, String inClausePartTable,
             String inClausePartColumn) {
         final String method = "createPlanStepUncorrelated";
         logger.entering(method);
 
         try {
             QueryTree subTree;
 
             subTree = subTreeNode.getSubqueryTree();
 
             QueryPlan aQueryPlan = new QueryPlan(client);
             aQueryPlan.topQueryPlan = this.topQueryPlan;
             aQueryPlan.tempIdCounter = this.tempIdCounter; // init stepNo
             aQueryPlan.planType = NONCORRELATED;
 
             // unionDepth is unimportant here, just set to 0
             aQueryPlan.createPlanSegmentFromTree(subTree, true, "", 0);
 
             this.tempIdCounter = aQueryPlan.tempIdCounter; // save stepNo
             subTreeNode.setTableName(aQueryPlan.finalTableName);
 
             logger.debug("final = " + aQueryPlan.finalTableName);
 
             // Set any extra partitioning we can take advantage of
             // due to IN Clause
 
             Leaf lastLeaf;
 
             if (aQueryPlan.unionSubplanList.isEmpty()) {
                 lastLeaf = aQueryPlan.leaves.get(aQueryPlan.leaves.size() - 1);
             } else {
                 QueryPlan unionPlan = aQueryPlan.unionSubplanList.get(0);
                 lastLeaf = unionPlan.leaves.get(unionPlan.leaves.size() - 1);
             }
 
             aQueryPlan.determineSubqueryProjections(lastLeaf,
                     subTree.getProjectionList());
 
             if (isInClause) {
                 if (!lastLeaf.isDistinct()) {
                     // Cut down on network traffic and writes; use SELECT
                     // DISTINCT for IN here
                     aQueryPlan.isDistinct = true;
                     lastLeaf.setDistinct(true);
 
                     lastLeaf.setSelectStatement("select distinct" + lastLeaf.getSelectStatement().substring(6));
                 }
                 lastLeaf.finalInClausePartitioningTable = inClausePartTable;
             }
 
             // We now add the subplan to the Leaf, not the parent Plan
             aLeaf.uncorrelatedSubplanList.add(aQueryPlan);
 
             /*
              * I took a stab at unrolling the IN clause to treat it as a join.
              * It gives us incorrect results because of duplicate values. We
              * need to either add a step to eliminate dupes, or just check if
              * distinct values will be returned (either implicitly or explicitly),
              * then allow it. Even with the
              * extra step, I feel that it will probably be faster in most cases
              * than using IN.
              *
              * Note that our performance with IN may actually decrease as we add
              * nodes because each node may return the same non-distinct value.
              * The number of results in the final subquery temp table could
              * increase for each additional node used.
              *
              * if (isInClause) { // try and rewrite as join String tempColString =
              * aQueryPlan.finalProjString.substring(0,aQueryPlan.finalProjString.indexOf("
              * "));
              *
              * Leaf.Join aJoin = aLeaf.new Join ( inClausePartTable + "." +
              * inClausePartColumn + " = " + aQueryPlan.finalTableName + "." +
              * tempColString );
              *
              * aLeaf.joinConditions.add (aJoin);
              *
              * aLeaf.extendedTableList.add (aQueryPlan.finalTableName);
              * aLeaf.extendedAliasList.add (aQueryPlan.finalTableName);
              * aLeaf.extendedOuterLevel.add (new
              * Integer(subTreeNode.outerLevel)); aLeaf.extendedOuter.addElement
              * (new Boolean(false)); }
              */
 
             // If unrolling, comment out the next 2 statements
             // set expression in parent tree
             // aLeaf.leafType = Leaf.SUBQUERY_JOIN;
             // aLeaf.combineOnMain = true;
 
             subTreeNode.getParentNoncorExpr().setExprString(" (SELECT "
                     + aQueryPlan.finalProjString + " FROM "
                     + IdentifierHandler.quote(subTreeNode.getTableName()) + ")");
 
             subTreeNode.getParentNoncorExpr().setTempExpr(true);
 
             trackTable(aQueryPlan.finalTableName, aLeaf);
         } finally {
             logger.exiting(method);
         }
     }
 
     /**
      * Processes the join with a correlated subquery.
      *
      * @param aQueryNode
      *            the QueryNode being processed
      * @param aLeaf
      *            the target Leaf info*
      */
     private void createPlanStepSubtreeCorrelated(QueryNode aQueryNode,
             Leaf aLeaf) {
 
         String condString = "";
         String subqueryFinalTempName;
 
         aLeaf.setLeafType(Leaf.SUBQUERY_JOIN);
 
         // We use the correlated join conditions twice-
         // once when creating the initial result set,
         // then a second time when we applying the condition
         // against it.
 
         // first, find placeholder node in query.
         if (aLeaf.isSingleStepCorrelated()) {
             aQueryNode.getRelationNode().getParentCorrelatedExpr().rebuildExpression();
         } else {
             for (QueryNode testNode : aQueryNode.getRelationNode()
                     .getSubqueryTree().getQueryNodeTable().values()) {
                 if (testNode.getNodeType() == QueryNode.RELATION
                         && testNode.getRelationNode().getNodeType() == RelationNode.SUBQUERY_CORRELATED_PH) {
                     correlatedNode = testNode;
                     break;
                 }
             }
 
             if (correlatedNode == null) {
                 throw new XDBServerException(
                         ErrorMessageRepository.CORRELATED_NODE_LOST,
                         XDBServerException.SEVERITY_HIGH,
                         ErrorMessageRepository.CORRELATED_NODE_LOST_CODE);
             }
 
             // Look for conditions that join the parent and child query.
             for (QueryCondition aQueryCondition : aQueryNode.getRelationNode().getSubqueryTree().getConditionList()) {
                 for (RelationNode relCheckNode : aQueryCondition.getRelationNodeList()) {
                     // see if condition involves our node.
                     if (relCheckNode == correlatedNode.getRelationNode()) {
                         SqlExpression parentExpr;
                         SqlExpression childExpr;
 
                         if (condString.length() > 0) {
                             condString += " AND ";
                         }
 
                         aQueryCondition.rebuildCondString();
 
                         condString += aQueryCondition.getCondString();
 
                         // For supporting greater parallelism, we
                         // now also try and determine 2 SqlExpressions
                         // that we can hash by for the final join, so
                         // we can execute it down at the nodes.
                         // We want 1 in the parent, and 1 in the child
                         subqueryFinalTempName = aLeaf.correlatedJoinTableName.substring(
                                 0, aLeaf.correlatedJoinTableName.length() - 1);
 
                         parentExpr = aQueryCondition.getExpr().getLeftExpr();
                         childExpr = aQueryCondition.getExpr().getRightExpr();
                         if (!parentExpr.containsColumnsExclusiveFromTable(subqueryFinalTempName)) {
                         	// try swap parent and child
                             SqlExpression tmp = parentExpr;
                         	parentExpr = childExpr;
                             childExpr = tmp;
                         }
 
                         if (parentExpr.containsColumnsExclusiveFromTable(subqueryFinalTempName) &&
                                 childExpr.containsColumnsExclusiveFromTable(aQueryNode.getRelationNode().getTableName())) {
                             aLeaf.addCorrelatedColumn(childExpr);
 
                             // only set if we have not yet set
                             if (!aLeaf.isCorrelatedHashable()) {
                                 aLeaf.setCorrelatedHashable(true);
                                 aLeaf.correlatedChildHashableExpression = childExpr;
                                 aLeaf.correlatedParentHashableExpression = parentExpr;
                             }
                         }
                     }
                 }
             }
 
             // Build join string for joining the subquery results
             // back with the parent.
             // In effect, we do an extra join at the end
             aQueryNode.getRelationNode().getParentCorrelatedExpr().setExprString(" (SELECT "
                     + aLeaf.subplan.finalProjString
                     + " FROM "
                     + IdentifierHandler.quote(aQueryNode.getRelationNode().getTableName()));
 
             if (condString.length() > 0) {
                 aQueryNode.getRelationNode().getParentCorrelatedExpr().setExprString(aQueryNode.getRelationNode().getParentCorrelatedExpr().getExprString()
                         + " WHERE "
                                 + condString);
             }
 
             aQueryNode.getRelationNode().getParentCorrelatedExpr().setExprString(aQueryNode.getRelationNode().getParentCorrelatedExpr().getExprString() + ")");
         }
 
         // We don't want to rebuild this expression, set temp expr to true!
         aQueryNode.getRelationNode().getParentCorrelatedExpr().setTempExpr(true);
 
         logger.debug("Correlated track: "
                 + aQueryNode.getRelationNode().getTableName() + ", "
                 + aLeaf.getSelect());
 
         trackTable(aQueryNode.getRelationNode().getTableName(), aLeaf);
     }
 
     /**
      *
      * @param aLeaf
      */
     private void createPlanStepFake(Leaf aLeaf) {
         aLeaf.setCombineOnMain(true);
     }
 
     /**
      * Processes a QueryNode that is either a relation of some kind or a subtree
      * of the tree that is "preserved", indicating that below this node contains
      * tables that are to be processed together, like in the case of lookup
      * joins.
      *
      * @param aQueryNode
      *            the QueryNode being processed
      * @param aLeaf
      *            the target Leaf info*
      */
     private void createPlanStepSubtree(QueryNode aQueryNode, Leaf aLeaf) {
         final String method = "createPlanStepSubtree";
         logger.entering(method);
 
         try {
             QueryCondition aQueryCondition;
             String inClausePartTable;
             boolean isInClause;
             AttributeColumn anAC;
 
             // Check for any uncorrelated subqueries that are part of conditions
             // at this node
             for (RelationNode uncorRelationNode : aQueryNode.getUncorrelatedCondTreeList()) {
                 inClausePartTable = null;
                 isInClause = false;
 
                 // We add special IN clause handling here.
                 // First, check for IN clause.
                 // There may be multiple IN clauses off of the QueryNode,
                 // so we determine which condition to use for this
                 // uncorRationNode
                 anAC = null;
 
                 for (QueryCondition aQC : aQueryNode.getConditionList()) {
                     if (aQC.getCondType() == QueryCondition.QC_COMPOSITE
                             || aQC.getCondType() == QueryCondition.QC_COND_COMPOSITE) {
                         if (aQC.getOperator() != null
                                 && aQC.getOperator().compareToIgnoreCase("IN") == 0) {
                             isInClause = true;
 
                             // we have an IN clause, now check if we have right
                             // node
                             if (aQC.getRightCond().getCondType() == QueryCondition.QC_SQLEXPR
                                     && aQC.getRightCond().getExpr().getExprType() == SqlExpression.SQLEX_SUBQUERY) {
                                 // Now check for node
                                 if (aQC.getRightCond().getCondString().equalsIgnoreCase(uncorRelationNode.getParentNoncorExpr().getExprString())) {
 
                                     // got one!
 
                                     // check if left side is a column
                                     if (aQC.getLeftCond().getCondType() == QueryCondition.QC_SQLEXPR
                                             && aQC.getLeftCond().getExpr().getExprType() == SqlExpression.SQLEX_COLUMN) {
                                         // Now check if left side is partitioned
                                         anAC = aQC.getLeftCond().getExpr().getColumn();
 
                                         if (anAC.isPartitionColumn()) {
                                             // ok, we can take advantage of
                                             // partitioning
                                             inClausePartTable = anAC.getTableName();
                                         }
                                     }
                                 }
                             }
                         }
                     }
                 }
                 createPlanStepUncorrelated(uncorRelationNode, aLeaf,
                         isInClause, inClausePartTable, null);
 
                 // Undid work for unrolling IN clause
                 // if (isInClause) aQueryNode.conditionList.remove(aQC);
             }
 
             // Note that a join here means that a subtree contains a join.
             // The subtree contains either a parent-child pair or lookups
             if (aQueryNode.getNodeType() == QueryNode.JOIN) {
                 createPlanStepSubtree(aQueryNode.getLeftNode(), aLeaf);
                 createPlanStepSubtree(aQueryNode.getRightNode(), aLeaf);
 
                 /*
                  * ok, now add our join conditions Remember, these join
                  * conditions are for lookups and parent-child joins.                 */
 
                 for (int i = 0; i < aQueryNode.getConditionList().size(); i++) {
                     aQueryCondition = aQueryNode.getConditionList().get(i);
                     logger.debug(" pre-rebuild: " + aQueryCondition.getCondString());
                     aQueryCondition.rebuildCondString();
 
                     aLeaf.addJoin(aQueryCondition, aQueryCondition.getCondString());
                     aQueryCondition.setInPlan(true);
                     logger.debug(" - joincond: " + aQueryCondition.getCondString());
                 }
 
                 // For type 2 right subtree outer join
                 if (aQueryNode.isSubtreeOuter()) {
                     // reset first one to indicate outer
                     aLeaf.fromRelationList.get(0).setIsOuter(true);
                 }
             } else {
                 // Handle correlated subquery
                 if (aQueryNode.getNodeType() == QueryNode.RELATION
                         && aQueryNode.getRelationNode().getNodeType() == RelationNode.SUBQUERY_CORRELATED) {
                     createPlanStepSubtreeCorrelated(aQueryNode, aLeaf);
                 }
 
                 // Need to update pseudotable placeholder
                 if (aQueryNode.getRelationNode().getNodeType() == RelationNode.SUBQUERY_CORRELATED_PH) {
                     aQueryNode.getRelationNode().setTableName(this.lastJoinTableName);
 
                     trackTable(aQueryNode.getRelationNode().getTableName(), aLeaf);
                 }
 
                 // add tables
                 // hack- comma separate 'em
                 logger.debug(" - adding table: "
                         + aQueryNode.getRelationNode().getTableName());
 
                 // don't set the table name if we are doing a subquery join
                 if (aQueryNode.getRelationNode().getNodeType() != RelationNode.SUBQUERY_NONCORRELATED
                         && aQueryNode.getRelationNode().getNodeType() != RelationNode.SUBQUERY_CORRELATED) {
 
                     if (aLeaf.getTableName().length() > 0) {
                         aLeaf.setTableName(aLeaf.getTableName() + ","); // " INNER JOIN ";
                     }
 
                     logger.debug(" check levelStatus = " + levelStatus);
 
                     // Outers for non-right subtrees (NOT parent-child and
                     // lookup joins)
                     // are handled above, but these need to be handled here
                     logger.debug(" outercheck table: "
                             + aQueryNode.getRelationNode().getTableName()
                             + " outer: " + aQueryNode.isSubtreeOuter());
 
                     FromRelation fromRelation = new FromRelation();                   
                     fromRelation.setOuterLevel(aQueryNode.getOuterLevel());
                     
                     if (aQueryNode.isSubtreeOuter()) {
                         fromRelation.setIsOuter(true);
                     } else {
                         fromRelation.setIsOuter(false);
                     }
 
                     //aLeaf.setTableName(aLeaf.getTableName() + aQueryNode.getRelationNode().getTableName());
                     
                     // handle differently if based on WITH
                     if (aQueryNode.isWithDerived()) {
                         // We are materializing, so make the temp table name 
                         // the current table name
                         String currentTableName = aQueryNode.getRelationNode().getBaseWithRelation().getCurrentTempTableName();
                         aLeaf.setTableName(currentTableName);
                         fromRelation.setTableName(currentTableName);
                         aQueryNode.getRelationNode().setCurrentTempTableName(currentTableName);
                         fromRelation.setAlias(currentTableName);
                         
                     } else {
                         aLeaf.setTableName(aLeaf.getTableName() + aQueryNode.getRelationNode().getTableName());
                         fromRelation.setTableName(aQueryNode.getRelationNode().getTableName());
                         
                         if (aQueryNode.getRelationNode().getAlias() != null
                                 && aQueryNode.getRelationNode().getAlias().length() > 0) {
                             fromRelation.setAlias(aQueryNode.getRelationNode().getAlias());
                         } else {
                             fromRelation.setAlias(aQueryNode.getRelationNode().getTableName());
                         }
                         
                     }
 
                     fromRelation.setIsOnly(aQueryNode.getRelationNode().isOnly());
                     
                     /* Add relation */
                     aLeaf.fromRelationList.add(fromRelation);
                     aLeaf.addUsedTable(aQueryNode.getRelationNode().getTableName());
 
                     if (aQueryNode.getRelationNode().getNodeType() != RelationNode.TABLE) {
                         trackTable(aQueryNode.getRelationNode().getTableName(),
                                 aLeaf);
                     }
                     logger.debug("ext add C, table: " + aLeaf.getTableName());
                     logger.debug("isSubtreeOuter() =  "
                             + aQueryNode.isSubtreeOuter());
                 }
 
                 // add atomic conditions
                 logger.debug(" - conditionList.size(): "
                         + aQueryNode.getConditionList().size());
                 logger.debug(" - subplanList.size(): " + subplanList.size());
 
                 for (int i = 0; i < aQueryNode.getConditionList().size(); i++) {
                     logger.debug(" - adding condition.");
 
                     aQueryCondition = aQueryNode.getConditionList().get(i);
 
                     // We look for expressions containing only constants and
                     // evaluate them now, for PostgresQL constraint exclusion partitioning.
                     // It makes it more likely to eliminate subtables to scan
                     if (Props.XDB_CONSTANT_EXPRESSION_THRESHOLD >= 0
                             && aQueryNode.getBaseNumRows() > Props.XDB_CONSTANT_EXPRESSION_THRESHOLD) {
                         evaluateConstantExpressions(aQueryCondition);
                     }
 
                     aQueryCondition.rebuildCondString();
 
                     logger.debug(" - cond: " + aQueryCondition.getCondString());
 
                     // see if we have to do any substitutions
                     if (!subplanList.isEmpty()) {
                         checkScalarExpression(aQueryCondition.getCondString(), aLeaf);
                     }
 
                     aLeaf.addCondition(aQueryCondition.getCondString());
                     aQueryCondition.setInPlan(true);
                     logger.debug(" - add cond1: " + aQueryCondition.getCondString());
                 }
             }
         } finally {
             logger.exiting(method);
         }
     }
 
     /**
      * Looks for expressions made up of only constants to evaluate. This is
      * important for constraint exclusion partitioning, and makes a big
      * difference for TPC-H query time.
      * @param aQueryCondition
      */
     private void evaluateConstantExpressions(QueryCondition aQueryCondition) {
 
         List<QueryCondition> sqlExpressionList = QueryCondition.getNodes(aQueryCondition,
                 QueryCondition.QC_SQLEXPR);
 
         for (QueryCondition aSqlExprCond : sqlExpressionList) {
             SqlExpression aSqlExpression = aSqlExprCond.getExpr();
 
             // Only process it if it is a constant expression,
             // BUT, not a simple one. That is, it is a more complicated one
             // we evaluate for constraint exclusion partitioning
             if (aSqlExpression.isConstantExpr()
                     && aSqlExpression.getExprType() != SqlExpression.SQLEX_CONSTANT) {
                 Engine anEngine = Engine.getInstance();
                 List nodeList = new ArrayList();
                 nodeList.add(database.getDBNode(database.getCoordinatorNodeID()));
                 Map results = anEngine.executeQueryOnMultipleNodes("select "
                         + aSqlExpression.getExprString(), nodeList, client);
 
                 Iterator it = results.values().iterator();
                 ResultSet rs = (ResultSet) it.next();
 
                 try {
                     if (rs.next()) {
                         // We replace it with the new value
                         if (aSqlExpression.getExprDataType().isNumeric()) {
                             aSqlExpression.setConstantValue(rs.getString(1));
                         } else {
                             aSqlExpression.setConstantValue("'"
                                     + rs.getString(1) + "'");
                         }
                         aSqlExpression.setExprType(SqlExpression.SQLEX_CONSTANT);
                         aSqlExpression.setLeftExpr(null);
                         aSqlExpression.setRightExpr(null);
                     }
                 } catch (SQLException se) {
 
                 } finally {
                     try {
                         rs.close();
                     } catch (SQLException se) {
 
                     }
                 }
             }
         }
     }
 
     // Generalized this to allow HAVING clause to use it, too.
 
     /**
      * An expression string is examined to determine if it is expecting a
      * constant value to be substituted as a result of the execution of a scalar
      * subquery
      *
      * @param exprString
      *            string containing the expression
      * @param aLeaf
      *            the Leaf that contains the scalar subquery
      */
     private void checkScalarExpression(String exprString, Leaf aLeaf) {
         final String method = "checkScalarExpression";
         logger.entering(method);
 
         try {
             int placePos, endPlacePos, placeNo;
 
             placePos = exprString.indexOf("&x");
 
             while (placePos >= 0) {
                 endPlacePos = exprString.indexOf("x&", placePos + 1);
                 // Skip if this is not a scalar placeholder (param?)
                 if (endPlacePos < 0) {
                     // see if we have any more
                     placePos = exprString.indexOf("&x", placePos + 1);
                 	continue;
                 }
 
                 placeNo = Integer.parseInt(exprString.substring(placePos + 2, endPlacePos));
 
                 // we have our place
                 // Now find orig subplan and tell it where to do
                 // substitution at execute time
                 for (QueryPlan aQueryPlan : subplanList) {
                     if (aQueryPlan.placeHolderNo == placeNo) {
                         logger.debug(" - assigned scalarLeaf.");
                         aQueryPlan.scalarLeaf = aLeaf;
                         break;
                     }
                 }
 
                 // see if we have any more
                 placePos = exprString.indexOf("&x", endPlacePos + 1);
             }
         } finally {
             logger.exiting(method);
         }
     }
 
     /**
      * The last final step's projection list is examined here and replaced with
      * the "final" ones required by the orginial QueryTree.
      *
      * @param isSubQuery
      * @param aLeaf the step being processed
      * @param projectionList List of SqlExpressions representing the values to SELECT
      */
     private void adjustFinalProjections(Leaf aLeaf, List<SqlExpression> projectionList,
             boolean isSubQuery) {
         final String method = "adjustFinalProjections";
         logger.entering(method);
 
         try {
             SqlExpression aSqlExpression;
             String sExpr;
             String currColumnName; // the name of the column that will appear
             // in final CREATE TABLE
             StringBuffer sbFinalProj = new StringBuffer();
 
             // replace projections, if they already exist
             aLeaf.resetProjections();
 
             for (int i = 0; i < projectionList.size(); i++) {
                 aSqlExpression = projectionList.get(i);
 
                 logger.debug(" expr: " + aSqlExpression.getExprString());
                 logger.debug(" alias: " + aSqlExpression.getAlias());
 
                 aSqlExpression.rebuildExpression();
 
                 // if we are dealing with a constant, put in finalProjString,
                 // but not in steps.
                 currColumnName = "";
 
                 // see if need to handle scalars in the SELECT clause
                 if (!subplanList.isEmpty()) {
                     checkScalarExpression(aSqlExpression.getExprString(), aLeaf);
                 }
 
                 sExpr = aSqlExpression.getExprString();
 
                 // See if we have already added it to CREATE TABLE
                 // BUT, always add it if it is an extra generated column
                 // or an aggregate function (as a workaround for now)
                // or a constant
                boolean isSelColumn = aLeaf.isProjection(aSqlExpression.getExprString())
                                   && !aSqlExpression.isConstantExpr();
 
                 if (!isSelColumn || aSqlExpression.isAdded()
                         || aSqlExpression.containsAggregates()
                         || aSqlExpression.isTempExpr()) {
                     if( aSqlExpression.isAdded() && !isSubQuery) {
                         continue;
                     }
                     currColumnName = aLeaf.appendProjectionFromExpr(
                             aSqlExpression, true);
 
                     if (!ExecutionPlan.TRANSFORM_PROJECTIONS) {
                         currColumnName = sExpr;
                     }
                 } else if (isSelColumn) {
                     // It has already been selected, reference previously
                     // mapped expression
                     currColumnName = aSqlExpression.getExprString();
                 }
 
                 // ignore expressions that were created extra
                 // (ones missing from SELECT clause, but in ORDER BY)
                 if (aSqlExpression.isAdded()) {
                     continue;
                 }
 
                 if (sbFinalProj.length() > 0) {
                     sbFinalProj.append(", ");
                 }
 
                 if (aSqlExpression.isConstantExpr()) {
                     sbFinalProj.append(aSqlExpression.getExprString());
                 } else {
                     if (currColumnName.length() > 0) {
                         sbFinalProj.append(currColumnName);
                     } else {
                         sbFinalProj.append(aSqlExpression.getExprString().substring(aSqlExpression.getExprString().indexOf(".") + 1));
                     }
                 }
 
                 if (aSqlExpression.getAlias().length() > 0) {
                     // add alias
                     sbFinalProj.append(" as ").append(IdentifierHandler.quote(aSqlExpression.getAlias()));
                 }
 
                 logger.debug("finalProjString = " + sbFinalProj.toString());
             }
 
             finalProjString = sbFinalProj.toString();
         } finally {
             logger.exiting(method);
         }
     }
 
     /**
      * For an uncorrelated subquery, determines the final projection string
      *
      * @param aLeaf
      *            the step being processed
      * @param projectionList
      *            List of SqlExpressions representing the values to SELECT
      */
     private void determineSubqueryProjections(Leaf aLeaf, List<SqlExpression> projectionList) {
         final String method = "determineSubqueryProjections";
         logger.entering(method);
 
         try {
             SqlExpression aSqlExpression;
             // the name of the column that will appear in final CREATE TABLE
             String currColumnName;
 
             // replace projections, if they already exist.
             aLeaf.resetProjections();
 
             this.finalProjString = "";
 
             for (int i = 0; i < projectionList.size(); i++) {
                 aSqlExpression = projectionList.get(i);
 
                 logger.debug(" expr: " + aSqlExpression.getExprString());
                 logger.debug(" alias: " + aSqlExpression.getAlias());
 
                 aSqlExpression.rebuildExpression();
 
                 // if we are dealing with a constant, put in finalProjString,
                 // but not in steps.
                 currColumnName = "";
 
                 // see if need to handle scalars in the SELECT clause
                 if (!subplanList.isEmpty()) {
                     checkScalarExpression(aSqlExpression.getExprString(), aLeaf);
                 }
 
                 currColumnName = aLeaf.appendProjectionFromExpr(
                         aSqlExpression, true);
 
                 if (finalProjString.length() > 0) {
                     finalProjString += ", ";
                 }
 
                 finalProjString += IdentifierHandler.quote(currColumnName);
 
                 logger.debug("finalProjString = " + finalProjString);
             }
         } finally {
             logger.exiting(method);
         }
     }
 
     /**
      * Determines the final projection string for correlated subqueries
      *
      * @param projectionList
      *            List of SqlExpressions to SELECT
      */
     private void determineFinalCorrelatedProjections(List<SqlExpression> projectionList) {
         final String method = "determineFinalCorrelatedProjections";
         logger.entering(method);
 
         try {
             SqlExpression aSqlExpression;
 
             finalProjString = "";
 
             for (int i = 0; i < projectionList.size(); i++) {
                 aSqlExpression = projectionList.get(i);
 
                 // ignore expressions that were created extra
                 // (ones missing from SELECT clause, but in ORDER BY)
                 if (aSqlExpression.isAdded()) {
                     continue;
                 }
 
                 aSqlExpression.rebuildExpression();
 
                 if (finalProjString.length() > 0) {
                     finalProjString += ", ";
                 }
 
                 finalProjString += aSqlExpression.getExprString().replaceAll(
                         "[A-Za-z]+[A-Za-z0-9_]*\\.", "");
 
                 logger.debug("finalProjString = " + finalProjString);
 
             }
         } finally {
             logger.exiting(method);
         }
     }
 
     /**
      * Handles cases when an expression appears in the order by list, but not in
      * the projection list. We need to make sure we add it to the final temp
      * table.
      *
      * @param aLeaf
      *            the step being processed
      * @param orderByList
      *            the order by list
      */
     // ------------------------------------------------------------------------
     private void adjustNonAggOrderBy(Leaf aLeaf, List<OrderByElement> orderByList) {
         final String method = "adjustNonAggOrderBy";
         logger.entering(method);
 
         try {
             String currColumnName;
             OrderByElement anOBE;
             SqlExpression aSqlExpression;
 
             for (int i = 0; i < orderByList.size(); i++) {
                 anOBE = orderByList.get(i);
                 aSqlExpression = anOBE.orderExpression;
 
                 aSqlExpression.rebuildExpression();
 
                 logger.debug(" order by expr: " + aSqlExpression.getExprString());
                 logger.debug(" order by alias: " + aSqlExpression.getAlias());
 
                 currColumnName = "";
 
                 // See if we have already added it to CREATE TABLE
                 // Also check alias
                 if (!aLeaf.isProjection(aSqlExpression.getExprString())) {
                     boolean found = false;
 
                     String crStr = aLeaf.getCreateTableColumns();
 
                     if (aSqlExpression.getAlias().length() > 0
                             && crStr.indexOf(" " + aSqlExpression.getAlias() + " ") >= 0) {
                         found = true;
                     }
                     if (aSqlExpression.getColumn() != null) {
                         if (crStr.indexOf(" "
                                 + aSqlExpression.getColumn().columnName) >= 0) {
                             found = true;
                         }
                     } else {
                         // Need to handle cases like "order by count(*)"
                         // String sExpr = aSqlExpression.exprString;
                         this.replaceColumnExpression(aSqlExpression);
 
                         if (crStr.indexOf(" " + aSqlExpression.getExprString() + " ") >= 0) {
                             found = true;
                         }
                     }
 
                     if (!found) {
                         currColumnName = aLeaf.appendProjectionFromExpr(
                                 aSqlExpression, false);
 
                         // note for substituting later
                         aSqlExpression.setAlias(currColumnName);
                     }
                 }
             }
         } finally {
             logger.exiting(method);
         }
     }
 
     /**
      * This is for a "final" step for handling aggregates. It creates an extra
      * step for combining aggregate results
      *
      * @param aQueryTree
      *            the QueryTree of the final step to determine
      */
     private void createFinalPlanStep(QueryTree aQueryTree) {
         final String method = "createFinalPlanStep";
         logger.entering(method);
 
         try {
             QueryCondition aQueryCondition;
             Leaf aLeaf;
 
             // We populate a "final" node for running on the coordinating node
             // and combining the results.
 
             aLeaf = new Leaf();
             aLeaf.setLeafStepNo(++this.currentLeafStepNo);
 
             aLeaf.setCombinerStep(true);
             aLeaf.setCombineOnMain(true);
 
             // Use last temp table name
             aLeaf.setTableName(Props.XDB_TEMPTABLEPREFIX + "T" + queryId + "_"
                     + tempIdCounter);
             logger.debug("ext add D, table: " + aLeaf.getTableName());
             
             aLeaf.fromRelationList.add(new FromRelation(
                     aLeaf.getTableName(), aLeaf.getTableName(),
                     false, 1, false));           
 
             aLeaf.setTargetTableName(generateTempTableName());
 
             logger.debug("final target: " + aLeaf.getTargetTableName());
 
             adjustFinalProjections(aLeaf, aQueryTree.getFinalProjList(),
                     (aQueryTree.getQueryType() & QueryTree.NONCORRELATED) > 0);
 
             // Make sure we have at least one column
             if (!aLeaf.hasProjections()) {
                 SqlExpression tempExpr = new SqlExpression();
 
                 ExpressionType anET = new ExpressionType();
                 anET.type = org.postgresql.stado.parser.ExpressionType.CHAR_TYPE;
                 anET.length = 1;
 
                 tempExpr.setExprType(SqlExpression.SQLEX_CONSTANT);
                 tempExpr.setConstantValue("'1'");
                 tempExpr.setExprString("'1'");
                 tempExpr.setExprDataType(anET); // java.sql.Types.CHAR;
                 tempExpr.setAlias("XDUMMY");
                 tempExpr.setTempExpr(true);
 
                 // Need to adjust table name
                 aLeaf.appendProjectionFromExpr(tempExpr, false);
 
                 // aColumn = aLeaf.new Column (tempExpr.exprString);
 
                 // aLeaf.selectColumns.addElement (aColumn);
                 logger.debug("Col: " + tempExpr.getExprString());
             }
 
             // Just set the group by list to the one we built up earlier
             aLeaf.groupByColumns = extraStepGroupByList;
 
             // Add other additional group by columns if necessary
             for (Leaf.Projection candidateColumn : projectedGroupBy) {
                 boolean found = false;
 
                 for (Leaf.Projection groupColumn : aLeaf.groupByColumns) {
                     if (groupColumn.projectString.equalsIgnoreCase(candidateColumn.projectString)) {
                         found = true;
                         break;
                     }
                 }
 
                 if (!found) {
                     aLeaf.groupByColumns.add(candidateColumn);
                 }
             }
 
             // if doing group by, we use all nodes
             if (aLeaf.groupByColumns.size() > 0) {
                 Collection<DBNode> dbNodeList = database.getDBNodeList();
                 aLeaf.queryNodeList = new ArrayList<DBNode>(dbNodeList.size());
 
                 // We need to add each of these to the main plan, if they are
                 // already not there
                 for (DBNode dbNode : dbNodeList) {
                     aLeaf.queryNodeList.add(dbNode);
                     topQueryPlan.queryNodeTable.put(dbNode.getNodeId(), dbNode);
                 }
             }
 
             for (int i = 0; i < aQueryTree.getHavingList().size(); i++) {
                 logger.debug("Having clause");
 
                 aQueryCondition = aQueryTree.getHavingList().get(i);
 
                 replaceHavingColumns(aQueryCondition);
 
                 // For scalars in HAVING,
                 // We need to mark all expressions in the query as temp,
                 // then rebuild so that the scalar placeholder will
                 // appear properly.
                 markConditionExpressionsAsTemp(aQueryCondition);
 
                 aQueryCondition.rebuildCondString();
 
                 logger.debug(" - having cond: " + aQueryCondition.getCondString());
 
                 // see if we have to do any scalar substitutions
                 if (!subplanList.isEmpty()) {
                     logger.debug(" - found having subplan");
 
                     checkScalarExpression(aQueryCondition.getCondString(), aLeaf);
                 }
 
                 // perform substitution
                 // aSqlExpression.exprString = (String) colMappings.get (
                 // replace (aSqlExpression.exprString.toUpperCase(), " ", ""));
 
                 aLeaf.addHavingCondition(aQueryCondition.getCondString());
 
             }
 
             // Make sure any correlated tables aren't dropped until the end
             /*
             if (aQueryTree.getCorrelatedSubqueryList().size() > 0) {
                 for (String tabName : correlatedFinalTables) {
                     trackTable(tabName, aLeaf);
                 }
             }
              */
 
             this.finalTableName = aLeaf.getTargetTableName();
 
             trackTable(aLeaf.getTableName(), aLeaf);
 
             // We want to drop the WITHs at the end
             for (RelationNode aRelationNode : aQueryTree.getTopWithSubqueryList()) {
                 trackTable(aRelationNode.getCurrentTempTableName(), aLeaf);
             }
             addLeaf(aLeaf);
         } finally {
             logger.exiting(method);
         }
     }
 
     /**
      * Mark SqlExpressions in QueryCondition as temp We need to do this so that
      * the HAVING handling will work ok with scalars.
      *
      * @param aQueryCondition
      *            the condition to mark as "temporary" ie, don't rebuild.
      */
     private void markConditionExpressionsAsTemp(QueryCondition aQueryCondition) {
         final String method = "markConditionExpressionsAsTemp";
         logger.entering(method);
 
         try {
             if (aQueryCondition.getLeftCond() != null) {
                 markConditionExpressionsAsTemp(aQueryCondition.getLeftCond());
             }
 
             if (aQueryCondition.getRightCond() != null) {
                 markConditionExpressionsAsTemp(aQueryCondition.getRightCond());
             }
 
             if (aQueryCondition.getCondType() == QueryCondition.QC_SQLEXPR) {
                 aQueryCondition.getExpr().setTempExpr(true);
             }
         } finally {
             logger.exiting(method);
         }
     }
 
     /**
      * Replaces mapped columns in the HAVING clause, due to the creation of the
      * extra aggregate step
      *
      * @param aQueryCondition
      *            the HAVING condition to process
      */
     // -------------------------------------------------------------------
     private void replaceHavingColumns(QueryCondition aQueryCondition) {
         final String method = "replaceHavingColumns";
         logger.entering(method);
 
         try {
             logger.debug("Having cond: " + aQueryCondition.getCondString());
 
             if (aQueryCondition.getCondType() == QueryCondition.QC_CONDITION
                     || aQueryCondition.getCondType() == QueryCondition.QC_RELOP) {
                 if (aQueryCondition.getLeftCond() != null) {
                     replaceHavingColumns(aQueryCondition.getLeftCond());
                 }
 
                 if (aQueryCondition.getRightCond() != null) {
                     replaceHavingColumns(aQueryCondition.getRightCond());
                 }
             } else {
                 logger.debug("Having expr: " + aQueryCondition.getExpr().getExprString());
 
                 replaceColumnExpression(aQueryCondition.getExpr());
 
                 aQueryCondition.getExpr().rebuildExpression();
             }
 
             aQueryCondition.rebuildCondString();
         } finally {
             logger.exiting(method);
         }
     }
 
     /**
      * Replaces mapped columns we renamed for SELECT, ORDER BY, GROUP BY
      *
      * @param aSqlExpression
      *            the column expression to process
      */
     private void replaceColumnExpression(SqlExpression aSqlExpression) {
         final String method = "replaceColumnExpression";
         logger.entering(method);
 
         try {
             String newExprString;
 
             if (aSqlExpression.getLeftExpr() != null) {
                 replaceColumnExpression(aSqlExpression.getLeftExpr());
             }
 
             if (aSqlExpression.getRightExpr() != null) {
                 replaceColumnExpression(aSqlExpression.getRightExpr());
             }
 
             if (aSqlExpression.getExprType() == SqlExpression.SQLEX_COLUMN
                     || aSqlExpression.getExprType() == SqlExpression.SQLEX_FUNCTION) {
                 // perform substitution
                 logger.debug("Replace: " + aSqlExpression.getExprString());
 
                 newExprString = getMappedExpression(aSqlExpression);
 
                 logger.debug("newExprString: " + newExprString);
 
                 if (newExprString != null) {
                     aSqlExpression.setTempExpr(true);
                     aSqlExpression.setExprString(newExprString);
                 }
             }
         } finally {
             logger.exiting(method);
         }
     }
 
     // -------------------------------------------------------------------
     // -------------------------------------------------------------------
     // The following methods are for handling aggregates
     // -------------------------------------------------------------------
     // -------------------------------------------------------------------
 
     /**
      * Generates a new column name for the extra step in handling aggregates
      * @param currentExpr
      * @return
      */
     private String generateColumnName(String currentExpr) {
         final String method = "generateColumnName";
         logger.entering(method);
 
         try {
 
             String newColumnName;
 
             newColumnName = "XCOL" + ++colGenCount;
 
             // Save mapping for later
             // colMappings.put (
             // replace (currentExpr.toUpperCase(), " ", ""),
             // newColumnName);
 
             return newColumnName;
         } finally {
             logger.exiting(method);
         }
     }
 
     /**
      * This is called in the case where we have a partitioned group by with a
      * having clause
      *
      * @param aQueryTree
      *            the QueryTree being processed
      * @param aLeaf
      *            the current step
      */
     private void addHavingConditions(QueryTree aQueryTree, Leaf aLeaf) {
         final String method = "addHavingConditions";
         logger.entering(method);
 
         try {
             for (int i = 0; i < aQueryTree.getHavingList().size(); i++) {
                 logger.debug("Having clause");
 
                 QueryCondition aQueryCondition = aQueryTree.getHavingList().get(i);
 
                 aQueryCondition.rebuildCondString();
 
                 logger.debug(" - having cond: " + aQueryCondition.getCondString());
 
                 // see if we have to do any scalar substitutions
                 if (!subplanList.isEmpty()) {
                     logger.debug(" - found having subplan");
 
                     checkScalarExpression(aQueryCondition.getCondString(), aLeaf);
                 }
 
                 // ---------------------------------------------------------------
                 replaceHavingColumns(aQueryCondition);
 
                 aLeaf.addHavingCondition(aQueryCondition.getCondString());
             }
         } finally {
             logger.exiting(method);
         }
     }
 
     /**
      * Checks to see if specified alias is already in projection list,
      * to help handle adding extra elements in the order by clause
      * when necessary.
      *
      * @param aQueryTree the QueryTree being processed
      * @param SqlExpression checkExpression the expression to compare
      *
      * @return the equivalent projected expression, or null if not found
      */
     SqlExpression getInProjectionList(QueryTree aQueryTree,
             SqlExpression checkExpression) {
 
         for (SqlExpression aSqlExpression : aQueryTree.getProjectionList()) {
             // Changed the exprString comparision to alias comparision
             if (checkExpression.getAlias().compareTo(aSqlExpression.getAlias()) == 0) {
                 return aSqlExpression;
             }
         }
         return null;
     }
 
     /**
      * We may have expressions and aggregates. We update the last query that
      * runs at all the nodes, as well as the real final query that runs at the
      * coordinating node that combines all of the results from the nodes.
      *
      * @param aQueryTree
      *            the QueryTree being processed
      * @param aLeaf
      *            current step
      */
     // -------------------------------------------------------------------
     private void updateAggProjections(QueryTree aQueryTree, Leaf aLeaf) {
         final String method = "updateAggProjections";
         logger.entering(method);
 
         try {
 
             SqlExpression aSqlExpression;
             SqlExpression newSqlExpression;
             SqlExpression aNewSE;
             SqlExpression finalSE;
             List<SqlExpression> newNodeProjList;
             String newColumnName;
             String checkExpression;
             String finalExprString;
 
             // Also, get group by list for last node query
             aQueryTree.getRootNode().setGroupByList(aQueryTree.getGroupByList());
 
             // First, check to see if we are dealing with the special
             // case where the first item of the group by clause is
             // a partition column of a table, which also appears in
             // the right subtree of the node.
             aQueryTree.determinePartitionedGroupBy(database);
 
             if (aQueryTree.isPartitionedGroupBy()
                     || aQueryTree.usesSingleDBNode()) {
                 return;
             }
 
             // Now process projections
             newNodeProjList = new ArrayList<SqlExpression>();
 
             for (int i = 0; i < aQueryTree.getProjectionList().size(); i++) {
                 aSqlExpression = aQueryTree.getProjectionList().get(i);
 
                 if (!subplanList.isEmpty()) {
                     aSqlExpression.rebuildExpression();
                     checkScalarExpression(aSqlExpression.getExprString(), aLeaf);
                 }
 
                 // just add to final list, not intermediate
                 if (aSqlExpression.isConstantExpr()) {
                     aQueryTree.getFinalProjList().add(aSqlExpression);
                     continue;
                 }
 
                 aSqlExpression.rebuildExpression();
 
                 if (!aSqlExpression.containsAggregates()) {
                     // Add to the Node Projection List
 
                     /*
                      * Add special handling for cases like: select n_nationkey,
                      * n_name, n_nationkey as nn, count(*) from nation group by
                      * n_nationkey, n_name;
                      *
                      * We do not want to generate a new column for the second
                      * n_nationkey; we want to reuse the first one. So, first
                      * check if we already have "mapped" that expression.
                      */
                     checkExpression = getMappedExpression(aSqlExpression);
 
                     // See if we have already handled this base expression
                     if (checkExpression != null) {
                         // ok, create a new expression, but use other alias,
                         // not our own
                         finalExprString = checkExpression;
                         newColumnName = checkExpression;
                     } else {
                         newColumnName = generateColumnName(aSqlExpression.getExprString());
 
                         // Save mapping for later
                         colMappings.put(aSqlExpression, newColumnName);
 
                         finalExprString = newColumnName;
 
                         // It is new, generate a column name and add it
                         aNewSE = aSqlExpression.copy();
                         aNewSE.setAlias(newColumnName);
 
                         aNewSE.setTempExpr(true);
                         newNodeProjList.add(aNewSE);
                     }
 
                     // Now add to the final list for the coordinating node
                     finalSE = new SqlExpression();
                     finalSE.setExprType(SqlExpression.SQLEX_COLUMN);
                     finalSE.setExprString(finalExprString);
                     finalSE.setAlias(aSqlExpression.getAlias());
                     finalSE.setExprDataType(aSqlExpression.getExprDataType());
 
                     if (aSqlExpression.getAlias().length() == 0) {
                         if (aSqlExpression.getExprType() == SqlExpression.SQLEX_COLUMN) {
                             aSqlExpression.setAlias(aSqlExpression.getColumn().columnName);
                         } else {
                             aSqlExpression.setAlias(newColumnName);
                         }
                         finalSE.setAlias(aSqlExpression.getAlias());
                     }
 
                     // Don't bother with .column info
                     finalSE.setTempExpr(true);
 
                     aQueryTree.getFinalProjList().add(finalSE);
 
                     // add as a group by element on final step
                     Leaf.Projection aColumn = aLeaf.new Projection(
                             finalSE.getExprString());
                     aColumn.forceGroupQuote = true;
                     projectedGroupBy.add(aColumn);
                 } else {
                     // We are dealing with some aggregates
                     newSqlExpression = aSqlExpression.copy();
 
                     handleProjections(aSqlExpression, newSqlExpression,
                             newNodeProjList);
 
                     // We need to rebuild the top level expression
 
                     logger.debug("old Expr: " + newSqlExpression.getExprString());
                     newSqlExpression.rebuildExpression();
                     logger.debug("new Expr: " + newSqlExpression.getExprString());
 
                     if (newSqlExpression.getAlias().length() == 0) {
                         newSqlExpression.setAlias(generateColumnName(""));
                     }
 
                     // note the new column name in final table
                     aSqlExpression.setAggAlias(newSqlExpression.getAlias());
 
                     aQueryTree.getFinalProjList().add(newSqlExpression);
 
                     // For distinct agg functions, we need to also add it to the
                     // group by list.
                     // Look for nested distinct aggregate, and handle it here.
                     if (!useSlowAggregation) {
                         for (SqlExpression deferredExpr : newSqlExpression.getDeferredExpressions()) {
                             // grab the last one we added to the new proj list
                             // gets "x" from SELECT COUNT(DISTINCT x)
                             SqlExpression aSE = deferredExpr.getFunctionParams().get(0).copy();
                             aSE.setDistinctExtraGroup(true);
                             aQueryTree.getRootNode().getGroupByList().add(aSE);
                         }
                     }
                 }
             }
 
             // Check to make sure all order by elements are being selected here.
             // Handles "ORDER BY COUNT(*)"
             for (int i = 0; i < aQueryTree.getOrderByList().size(); i++) {
                 OrderByElement anOBE = aQueryTree.getOrderByList().get(i);
 
                 anOBE.orderExpression.rebuildExpression();
                 aSqlExpression = null;
 
                 // Look for it in projection list
                 aSqlExpression = getInProjectionList(aQueryTree,
                         anOBE.orderExpression);
 
                 if (aSqlExpression != null) {
                     if (aSqlExpression == anOBE.orderExpression) {
                         continue;
                     }
                     // pick up the alias
                     // need to use the one in the second step
                     String mappedExprString = getMappedExpression(aSqlExpression);
                     if (mappedExprString != null) {
                         anOBE.orderExpression.setAlias(mappedExprString);
                     } else {
                         anOBE.orderExpression.setAlias(aSqlExpression.getAggAlias());
                     }
                 } else {
                     // We need to add this to the projection list
                     aQueryTree.getProjectionList().add(anOBE.orderExpression);
 
                     aSqlExpression = anOBE.orderExpression;
                     newSqlExpression = aSqlExpression.copy();
 
                     handleProjections(aSqlExpression, newSqlExpression,
                             newNodeProjList);
 
                     // We need to rebuild the top level expression
                     logger.debug("old Expr: " + newSqlExpression.getExprString());
                     newSqlExpression.rebuildExpression();
                     logger.debug("new Expr: " + newSqlExpression.getExprString());
 
                     if (newSqlExpression.getAlias().length() == 0) {
                         newSqlExpression.setAlias(generateColumnName(""));
                     }
 
                     // note the new column name in final table
                     anOBE.orderExpression.setAlias(newSqlExpression.getAlias());
 
                     // We need it in the final proj list to create the table
                     // properly, but we set a flag for special handling
                     anOBE.orderExpression.setAdded(true);
                     newSqlExpression.setAdded(true);
                     aQueryTree.getFinalProjList().add(newSqlExpression);
                 }
             }
 
             // We need to handle the special situation when a having expression
             // does not appear in the SELECT clause
             for (int i = 0; i < aQueryTree.getHavingList().size(); i++) {
                 QueryCondition aQC = aQueryTree.getHavingList().get(i);
 
                 handleHavingCond(aQC, newNodeProjList);
             }
 
             // We need to replace the top QueryNode's projection list
             // with our new one.
             aQueryTree.getRootNode().setProjectionList(newNodeProjList);
 
         } finally {
             logger.exiting(method);
         }
     }
 
 
     /**
      * We need to handle the special situation when a having expression does not
      * appear in the SELECT clause
      *
      * @param aQueryCondition
      *            the having condition
      * @param newNodeProjList
      *            the current new projection list
      */
     private void handleHavingCond(QueryCondition aQueryCondition,
             List<SqlExpression> newNodeProjList) {
         final String method = "handleHavingCond";
         logger.entering(method);
 
         try {
             if (aQueryCondition.getLeftCond() != null) {
                 handleHavingCond(aQueryCondition.getLeftCond(), newNodeProjList);
             }
 
             if (aQueryCondition.getRightCond() != null) {
                 handleHavingCond(aQueryCondition.getRightCond(), newNodeProjList);
             }
 
             if (aQueryCondition.getCondType() == QueryCondition.QC_SQLEXPR) {
                 handleHavingProj(aQueryCondition.getExpr(), newNodeProjList);
             }
         } finally {
             logger.exiting(method);
         }
     }
 
     /**
      * This method was created to handle the case when an expression appears in
      * the HAVING clause, but not the SELECT clause
      *
      * @param aSqlExpression
      *            an expression in part of a having condition
      * @param newNodeProjList
      *            the current new projection list
      */
     private void handleHavingProj(SqlExpression aSqlExpression,
             List<SqlExpression> newNodeProjList) {
         final String method = "handleHavingProj";
         logger.entering(method);
 
         try {
             SqlExpression newSqlExpression;
 
             if (aSqlExpression.getLeftExpr() != null) {
                 handleHavingProj(aSqlExpression.getLeftExpr(), newNodeProjList);
             }
 
             if (aSqlExpression.getRightExpr() != null) {
                 handleHavingProj(aSqlExpression.getRightExpr(), newNodeProjList);
             }
 
             if (aSqlExpression.getExprType() == SqlExpression.SQLEX_FUNCTION
                     && aSqlExpression.getFunctionId() > 0) {
 
                 // Added having for cartesian product referring to wrong
                 // table
                 aSqlExpression.rebuildExpression();
 
                 newSqlExpression = aSqlExpression.copy();
 
                 handleProjections(aSqlExpression, newSqlExpression,
                         newNodeProjList);
 
                 // We don't need to add newSqlExpression to the final proj
                 // list here. We don't want to display it.
             }
 
         } finally {
             logger.exiting(method);
         }
     }
 
     /**
      * Given an expression that takes part in an aggregate expression, this
      * creates a new expression to be included in the extra step created for
      * aggregate handling.
      *
      * @param aSqlExpression
      *            an expression involved in an aggregate SELECT
      * @param newSqlExpression
      *            the new expression for the extra step
      * @param newNodeProjList
      *            the current projection list
      */
     private void handleProjections(SqlExpression aSqlExpression,
             SqlExpression newSqlExpression, List<SqlExpression> newNodeProjList) {
         final String method = "handleProjections";
         logger.entering(method);
 
         try {
 
             if (aSqlExpression.getExprType() == SqlExpression.SQLEX_OPERATOR_EXPRESSION
             		|| aSqlExpression.getExprType() == SqlExpression.SQLEX_UNARY_EXPRESSION) {
 	            if (aSqlExpression.getLeftExpr() != null) {
 	                SqlExpression childSqlExpression = aSqlExpression.getLeftExpr().copy();
 	                newSqlExpression.setLeftExpr(childSqlExpression);
 
 	                childSqlExpression.rebuildExpression();
 
 	                handleProjections(aSqlExpression.getLeftExpr(), childSqlExpression,
 	                        newNodeProjList);
 	            }
 
 	            if (aSqlExpression.getRightExpr() != null) {
 	                SqlExpression childSqlExpression = aSqlExpression.getRightExpr().copy();
 	                newSqlExpression.setRightExpr(childSqlExpression);
 
 	                childSqlExpression.rebuildExpression();
 
 	                handleProjections(aSqlExpression.getRightExpr(), childSqlExpression,
 	                        newNodeProjList);
 	            }
             } else if (aSqlExpression.getExprType() == SqlExpression.SQLEX_CONDITION) {
         		for (QueryCondition qcExpr : QueryCondition.getNodes(aSqlExpression.getQueryCondition(), QueryCondition.QC_SQLEXPR)) {
         			SqlExpression childSqlExpression = qcExpr.getExpr().copy();
         			qcExpr.setExpr(childSqlExpression);
 
         			childSqlExpression.rebuildExpression();
 
         			handleProjections(qcExpr.getExpr(), childSqlExpression, newNodeProjList);
         		}
         		newSqlExpression.setQueryCondition(aSqlExpression.getQueryCondition());
             } else if (aSqlExpression.getExprType() == SqlExpression.SQLEX_CASE) {
             	SqlExpression.SCase newCase = newSqlExpression.new SCase();
             	for (Map.Entry<QueryCondition, SqlExpression> entry : aSqlExpression.getCaseConstruct().getCases().entrySet()) {
             		for (QueryCondition qcExpr : QueryCondition.getNodes(entry.getKey(), QueryCondition.QC_SQLEXPR)) {
             			SqlExpression childSqlExpression = qcExpr.getExpr().copy();
 
             			childSqlExpression.rebuildExpression();
 
             			handleProjections(qcExpr.getExpr(), childSqlExpression, newNodeProjList);
             			qcExpr.setExpr(childSqlExpression);
             		}
         			SqlExpression childSqlExpression = entry.getValue().copy();
 
         			childSqlExpression.rebuildExpression();
 
         			handleProjections(entry.getValue(), childSqlExpression, newNodeProjList);
         			newCase.addCase(entry.getKey(), childSqlExpression);
             	}
             	SqlExpression defExpr = aSqlExpression.getCaseConstruct().getDefaultexpr();
             	if (defExpr != null) {
         			SqlExpression childSqlExpression = defExpr.copy();
         			newCase.setDefaultexpr(childSqlExpression);
 
         			childSqlExpression.rebuildExpression();
 
         			handleProjections(defExpr, childSqlExpression, newNodeProjList);
             	}
             	newSqlExpression.setCaseConstruct(newCase);
             } else if (aSqlExpression.getExprType() == SqlExpression.SQLEX_FUNCTION) {
                 if (aSqlExpression.getFunctionId() == IFunctionID.COUNT_ID
                         || aSqlExpression.getFunctionId() == IFunctionID.COUNT_STAR_ID) {
                     // count becomes SUM (COUNT at nodes)
 
                     // "Deferred" here is a bit of a misnomer,
                     // it means distinct
                     // When the column used in the DISTINCT clause
                     // is a partitioned column, we do not do the aggregation
                     if (aSqlExpression.isDistinctGroupFunction() &&
                              !aSqlExpression.isDistinctGroupFunctionOnPartitionedCol(database)) {
                             // 1. Get distinct values (involves aggregation)
                             // 2. Count them
                         handleProjectionAddStepDeferred(aSqlExpression,
                                 newSqlExpression, newNodeProjList,
                                 IFunctionID.COUNT_STAR_ID, "COUNT", true);
                     } else {
                         // 1. Get counts
                         // 2. Sum them
                         handleProjectionAddStep(aSqlExpression,
                                 newSqlExpression, newNodeProjList,
                                 IFunctionID.SUM_ID, "SUM");
                     }
                 }
                 // sum becomes SUM (SUM at nodes)
                 else if (aSqlExpression.getFunctionId() == IFunctionID.SUM_ID) {
                     // When the column used in the DISTINCT clause
                     // is a partitioned column, we do not do the aggregation
                     if (aSqlExpression.isDistinctGroupFunction() &&
                             !aSqlExpression.isDistinctGroupFunctionOnPartitionedCol(database)) {
                         // 1. Get distinct values
                         // 2. Sum them
                         handleProjectionAddStepDeferred(aSqlExpression,
                                 newSqlExpression, newNodeProjList,
                                 IFunctionID.SUM_ID, "SUM", true);
                     } else {
                         // 1. Get sums
                         // 2. Sum them
                         handleProjectionAddStep(aSqlExpression,
                                 newSqlExpression, newNodeProjList,
                                 IFunctionID.SUM_ID, "SUM");
                     }
                 }
                 // avg becomes SUM (SUM at nodes) / SUM (COUNT at nodes)
                 else if (aSqlExpression.getFunctionId() == IFunctionID.AVG_ID) {
                     // for average, we need to add a sum column and a count
                     // column
 
                     // When the column used in the DISTINCT clause
                     // is a partitioned column, we do not do the aggregation
                     if (aSqlExpression.isDistinctGroupFunction() &&
                              !aSqlExpression.isDistinctGroupFunctionOnPartitionedCol(database)) {
                         // 1. Get distinct values
                         // 2. Average them
                         handleProjectionAddStepDeferred(aSqlExpression,
                                 newSqlExpression, newNodeProjList,
                                 IFunctionID.AVG_ID, "AVG", true);
                     } else {
                         makeAvgExpression(aSqlExpression, newNodeProjList,
                                 newSqlExpression);
                     }
                     colMappings.put(aSqlExpression, newSqlExpression.getExprString());
                 } else if (aSqlExpression.getFunctionId() == IFunctionID.STDEV_ID) {
                     if (aSqlExpression.isDistinctGroupFunction()) {
                         handleProjectionAddStepDeferred(aSqlExpression,
                                 newSqlExpression, newNodeProjList,
                                 IFunctionID.AVG_ID, "STDDEV", true);
                         colMappings.put(aSqlExpression,
                                 newSqlExpression.getExprString());
                     } else {
                         makeStddevExpression(aSqlExpression, newNodeProjList,
                                 newSqlExpression,
                                 aSqlExpression.getFunctionParams().get(0).getExprString());
                     }
                 } else if (aSqlExpression.getFunctionId() == IFunctionID.VARIANCE_ID) {
                     if (aSqlExpression.isDistinctGroupFunction()) {
                         handleProjectionAddStepDeferred(aSqlExpression,
                                 newSqlExpression, newNodeProjList,
                                 IFunctionID.AVG_ID, "VARIANCE", true);
                         colMappings.put(aSqlExpression,
                                 newSqlExpression.getExprString());
                     } else {
                         makeVarianceExpression(aSqlExpression, newNodeProjList,
                                 newSqlExpression,
                                 aSqlExpression.getFunctionParams().get(0).getExprString());
                     }
                 } else if (aSqlExpression.getFunctionId() == IFunctionID.REGRCOUNT_ID) {
                     makeRegrCountExpression(aSqlExpression, newNodeProjList,
                             newSqlExpression,
                             aSqlExpression.getFunctionParams().get(0).getExprString(),
                             aSqlExpression.getFunctionParams().get(1).getExprString());
                 } else if (aSqlExpression.getFunctionId() == IFunctionID.REGRAVX_ID) {
                     makeRegrAvgxExpression(aSqlExpression, newNodeProjList,
                             newSqlExpression,
                             aSqlExpression.getFunctionParams().get(0).getExprString(),
                             aSqlExpression.getFunctionParams().get(1).getExprString());
                 } else if (aSqlExpression.getFunctionId() == IFunctionID.REGRAVY_ID) {
                     // swap the parameters and pass to regr_avgx
                     makeRegrAvgxExpression(aSqlExpression, newNodeProjList,
                             newSqlExpression,
                             aSqlExpression.getFunctionParams().get(1).getExprString(),
                             aSqlExpression.getFunctionParams().get(0).getExprString());
                 } else if (aSqlExpression.getFunctionId() == IFunctionID.REGRSXX_ID) {
                     makeRegrSxxExpression(aSqlExpression, newNodeProjList,
                             newSqlExpression,
                             aSqlExpression.getFunctionParams().get(0).getExprString(),
                             aSqlExpression.getFunctionParams().get(1).getExprString());
                 } else if (aSqlExpression.getFunctionId() == IFunctionID.REGRSYY_ID) {
                     // swap the parameters and pass it to regr_sxx
                     makeRegrSxxExpression(aSqlExpression, newNodeProjList,
                             newSqlExpression,
                             aSqlExpression.getFunctionParams().get(1).getExprString(),
                             aSqlExpression.getFunctionParams().get(0).getExprString());
                 } else if (aSqlExpression.getFunctionId() == IFunctionID.REGRSXY_ID) {
                     makeRegrSxyExpression(aSqlExpression, newNodeProjList,
                             newSqlExpression,
                             aSqlExpression.getFunctionParams().get(0).getExprString(),
                             aSqlExpression.getFunctionParams().get(1).getExprString());
                 } else if (aSqlExpression.getFunctionId() == IFunctionID.REGRR2_ID) {
                     makeRegrR2Expression(aSqlExpression, newNodeProjList,
                             newSqlExpression,
                             aSqlExpression.getFunctionParams().get(0).getExprString(),
                             aSqlExpression.getFunctionParams().get(1).getExprString());
                 } else if (aSqlExpression.getFunctionId() == IFunctionID.REGRSLOPE_ID) {
                     makeRegrSlopeExpression(aSqlExpression, newNodeProjList,
                             newSqlExpression,
                             aSqlExpression.getFunctionParams().get(0).getExprString(),
                             aSqlExpression.getFunctionParams().get(1).getExprString());
                 } else if (aSqlExpression.getFunctionId() == IFunctionID.REGRINTERCEPT_ID) {
                     makeRegrInterceptExpression(aSqlExpression,
                             newNodeProjList, newSqlExpression,
                             aSqlExpression.getFunctionParams().get(0).getExprString(),
                             aSqlExpression.getFunctionParams().get(1).getExprString());
                 } else if (aSqlExpression.getFunctionId() == IFunctionID.VARIANCEPOP_ID) {
                     makeVarPopExpression(aSqlExpression, newNodeProjList,
                             newSqlExpression,
                             aSqlExpression.getFunctionParams().get(0).getExprString());
                 } else if (aSqlExpression.getFunctionId() == IFunctionID.COVARPOP_ID) {
                     makeCovarPopExpression(aSqlExpression, newNodeProjList,
                             newSqlExpression,
                             aSqlExpression.getFunctionParams().get(0).getExprString(),
                             aSqlExpression.getFunctionParams().get(1).getExprString());
                 } else if (aSqlExpression.getFunctionId() == IFunctionID.COVARSAMP_ID) {
                     makeCovarSampExpression(aSqlExpression, newNodeProjList,
                             newSqlExpression,
                             aSqlExpression.getFunctionParams().get(0).getExprString(),
                             aSqlExpression.getFunctionParams().get(1).getExprString());
                 } else if (aSqlExpression.getFunctionId() == IFunctionID.CORR_ID) {
                     makeCorrExpression(aSqlExpression, newNodeProjList,
                             newSqlExpression,
                             aSqlExpression.getFunctionParams().get(0).getExprString(),
                             aSqlExpression.getFunctionParams().get(1).getExprString());
                 } else if (aSqlExpression.getFunctionId() == IFunctionID.STDEVPOP_ID) {
                     makeStddevPopExpression(aSqlExpression, newNodeProjList,
                             newSqlExpression,
                             aSqlExpression.getFunctionParams().get(0).getExprString());
                 } else if (aSqlExpression.getFunctionId() == IFunctionID.VARIANCESAMP_ID) {
                     makeVarSampExpression(aSqlExpression, newNodeProjList,
                             newSqlExpression,
                             aSqlExpression.getFunctionParams().get(0).getExprString());
                 } else if (aSqlExpression.getFunctionId() == IFunctionID.STDEVSAMP_ID) {
                     makeStddevSampExpression(aSqlExpression, newNodeProjList,
                             newSqlExpression,
                             aSqlExpression.getFunctionParams().get(0).getExprString());
                 } else if (aSqlExpression.getFunctionId() == IFunctionID.MAX_ID) {
                     handleProjectionAddStep(aSqlExpression, newSqlExpression,
                             newNodeProjList, IFunctionID.MAX_ID, "MAX");
                 } else if (aSqlExpression.getFunctionId() == IFunctionID.MIN_ID) {
                     handleProjectionAddStep(aSqlExpression, newSqlExpression,
                             newNodeProjList, IFunctionID.MIN_ID, "MIN");
                 } else if (aSqlExpression.getFunctionId() == IFunctionID.BITOR_ID) {
                     // Note we do not care if it is DISTINCT, the result will
                     // be the same. So, there is no need to defer when
                     // distinct is present
 
                     // 1. Apply bit_or
                     // 2. Apply bit_or on top of those results
                     handleProjectionAddStep(aSqlExpression, newSqlExpression,
                             newNodeProjList, IFunctionID.BITOR_ID, "BIT_OR");
                 } else if (aSqlExpression.getFunctionId() == IFunctionID.BITAND_ID) {
                     // When the column used in the DISTINCT clause
                     // is a partitioned column, we do not do the aggregation
                     if (aSqlExpression.isDistinctGroupFunction() &&
                             !aSqlExpression.isDistinctGroupFunctionOnPartitionedCol(database)) {
                         // 1. Get distinct values
                         // 2. Bit_and them
                         handleProjectionAddStepDeferred(aSqlExpression,
                                 newSqlExpression, newNodeProjList,
                                 IFunctionID.BITAND_ID, "BIT_AND", true);
                     } else {
                         // 1. Apply bit_and
                         // 2. Apply bit_and on top of those results
                         handleProjectionAddStep(aSqlExpression,
                                 newSqlExpression, newNodeProjList,
                                 IFunctionID.BITAND_ID, "BIT_AND");
                     }
                 } else if (aSqlExpression.getFunctionId() == IFunctionID.BOOLOR_ID) {
                     // Note we do not care if it is DISTINCT, the result will
                     // be the same. So, there is no need to defer when
                     // distinct is present
 
                     // 1. Apply bool_or
                     // 2. Apply bool_or on top of those results
                     handleProjectionAddStep(aSqlExpression, newSqlExpression,
                             newNodeProjList, IFunctionID.BOOLOR_ID, "BOOL_OR");
                 } else if (aSqlExpression.getFunctionId() == IFunctionID.BOOLAND_ID) {
                     if (aSqlExpression.isDistinctGroupFunction()) {
                         // 1. Get distinct values
                         // 2. Bool_and them
                         handleProjectionAddStepDeferred(aSqlExpression,
                                 newSqlExpression, newNodeProjList,
                                 IFunctionID.BOOLAND_ID, "BOOL_AND", true);
                     } else {
                         // 1. Apply bool_and
                         // 2. Apply bool_and on top of those results
                         handleProjectionAddStep(aSqlExpression,
                                 newSqlExpression, newNodeProjList,
                                 IFunctionID.BOOLAND_ID, "BOOL_AND");
                     }
                 } else if (aSqlExpression.getFunctionId() == IFunctionID.EVERY_ID) {
                     if (aSqlExpression.isDistinctGroupFunction()) {
                         // 1. Get distinct values
                         // 2. Apply every()
                         handleProjectionAddStepDeferred(aSqlExpression,
                                 newSqlExpression, newNodeProjList,
                                 IFunctionID.EVERY_ID, "EVERY", true);
                     } else {
                         // 1. Apply every()
                         // 2. Apply every() on top of those results
                         handleProjectionAddStep(aSqlExpression,
                                 newSqlExpression, newNodeProjList,
                                 IFunctionID.EVERY_ID, "EVERY");
                     }
                 } else {
                     // handle cases like CAST (count(*) as real)
                     for (int i = 0; i < aSqlExpression.getFunctionParams().size(); i++) {
                         SqlExpression paramExpression = aSqlExpression.getFunctionParams().get(i);
 
                         SqlExpression childSqlExpression = paramExpression.copy();
 
                         newSqlExpression.getFunctionParams().set(i, childSqlExpression);
                         childSqlExpression.rebuildExpression();
 
                         handleProjections(paramExpression, childSqlExpression,
                                 newNodeProjList);
                     }
                 }
 
             } else {
                 // Just add it
                 if (aSqlExpression.getExprType() != SqlExpression.SQLEX_CONSTANT) {
                     newNodeProjList.add(aSqlExpression);
                 }
             }
         } finally {
             logger.exiting(method);
         }
     }
 
     /**
      * Handles the basic case for adding an extra step due to aggregates
      * @param aSqlExpression
      * @param newSqlExpression
      * @param newNodeProjList
      * @param functionId
      * @param functionName
      */
     private void handleProjectionAddStep(SqlExpression aSqlExpression,
             SqlExpression newSqlExpression, List<SqlExpression> newNodeProjList,
             int functionId, String functionName) {
         SqlExpression aNewSE = aSqlExpression.copy();
 
         String newColumnName = generateColumnName(aNewSE.getExprString());
 
         aNewSE.setExprString(aSqlExpression.getExprString());
         aNewSE.setAlias(newColumnName);
         aNewSE.setTempExpr(true);
 
         newNodeProjList.add(aNewSE);
 
         newSqlExpression.setFunctionId(functionId);
         newSqlExpression.setFunctionName(functionName);
         newSqlExpression.setExprString(functionName + "(" + IdentifierHandler.quote(newColumnName) + ")");
         newSqlExpression.setTempExpr(true);
 
         colMappings.put(aSqlExpression, newSqlExpression.getExprString());
     }
 
     /**
      * Handles the basic case for adding an extra step due to aggregates for
      * operations where we just get the column in the first step and then apply
      * aggregate function in the second.
      *
      *
      * @param aSqlExpression
      * @param newSqlExpression
      * @param newNodeProjList
      * @param functionId
      * @param functionName
      * @param distinct
      */
     private void handleProjectionAddStepDeferred(SqlExpression aSqlExpression,
             SqlExpression newSqlExpression, List<SqlExpression> newNodeProjList,
             int functionId, String functionName, boolean isDistinct) {
 
         SqlExpression aNewSE = aSqlExpression.getFunctionParams().get(0);
 
         String newColumnName = generateColumnName(aNewSE.getExprString());
         aNewSE.setAlias(newColumnName);
         aNewSE.setTempExpr(true);
         newNodeProjList.add(aNewSE);
 
         newSqlExpression.setFunctionId(functionId);
         newSqlExpression.setFunctionName(functionName);
         newSqlExpression.setExprString(functionName + "("
                 + (isDistinct ? "DISTINCT " : "") + IdentifierHandler.quote(newColumnName) + ")");
 
         newSqlExpression.setTempExpr(true);
         newSqlExpression.setDeferredGroup(true);
         colMappings.put(aSqlExpression, newSqlExpression.getExprString());
 
         logger.debug("end ExprString = " + newSqlExpression.getExprString());
     }
 
     /**
      * Tracks temp table usage for dropping later
      *
      * @param tableName
      *            the current temp table name to use
      * @param aLeaf
      *            the current step
      */
     protected void trackTable(String tableName, Leaf aLeaf) {
         final String method = "trackTable";
         logger.entering(method);
 
         try {
             if (tableName != null
                     && tableName.indexOf(Props.XDB_TEMPTABLEPREFIX) == 0) {
                 topQueryPlan.tempTableLastUsedAt.put(tableName, aLeaf);
             }
         } finally {
             logger.exiting(method);
         }
     }
 
     /**
      * Get mapped expression.
      *
      * This used to use strings and do a string comparison. Now, the keys are
      * expressions so that they can be rebuilt and compared, to solve a having
      * clause bug.
      * @param aSqlExpression
      * @return
      */
     private String getMappedExpression(SqlExpression aSqlExpression) {
         aSqlExpression.rebuildExpression();
         String targetStr = ParseCmdLine.replace(aSqlExpression.getExprString(),
                 " ", "");
 
         for (Map.Entry entry : colMappings.entrySet()) {
 
             SqlExpression keyExpression = (SqlExpression) entry.getKey();
 
             keyExpression.rebuildExpression();
             String keyStr = ParseCmdLine.replace(keyExpression.getExprString(),
                     " ", "");
 
             if (targetStr.compareTo(keyStr) == 0) {
                 return (String) entry.getValue();
             }
         }
 
         return null;
     }
 
     /**
      * sets up metadata information for the final result set
      *
      * @param expression
      * @return
      */
     private ColumnMetaData getColumnMetaData(SqlExpression expression) {
 
         if (expression.getMappedExpression() != null) {
             return getColumnMetaData(expression.getMappedExpression());
         }
 
         // Column name - only defined if expression is Column
         String name = null;
         // Column alias (a bit of black magic)
 
         String alias = expression.getAggAlias() == null
                 || expression.getAggAlias().length() == 0 ? expression.getAlias()
                 : expression.getAggAlias();
 
         if (expression.getProjectionLabel() != null) {
             alias = expression.getProjectionLabel();
         }
 
         // Column DataType
         int xdbType = expression.getExprDataType().type;
         int length = expression.getExprDataType().length;
         int precision = expression.getExprDataType().precision;
         int scale = expression.getExprDataType().scale;
         // Table name - only defined if expression is Column
         String tableName = null;
         // IS_NULLABLE - false if expression is Column and does not allow nulls
         // IS_WRITABLE - always false since ResultSets are read-only
         // IS_CURRENCY - always false since we do not distinguish CURRENCY data
         // type
         // IS_SIGNED_NUM - true for numeric data types
         // IS_CASE_SENSITIVE - always false (??? true)
         // IS_AUTO_INCREMENT - true if expression is Column and is Serial
         // IS_READ_ONLY - always true since ResultSets are read-only
         // IS_SEARCHABLE - always true since only BLOBs are not searchable but
         // we do not support them
         // IS_PRIMARY_KEY - not supported
         // IS_FOREIGN_KEY - not supported
         short flags = 0;
 
         // "always true" and "true by default" values
         flags = ColumnMetaData.IS_READ_ONLY | ColumnMetaData.IS_SEARCHABLE
                 | ColumnMetaData.IS_NULLABLE;
         // depends on datatype only
         if (xdbType == Types.BIGINT || xdbType == Types.DECIMAL
                 || xdbType == Types.DOUBLE || xdbType == Types.FLOAT
                 || xdbType == Types.INTEGER || xdbType == Types.NUMERIC
                 || xdbType == Types.REAL || xdbType == Types.SMALLINT
                 || xdbType == Types.TINYINT) {
             flags |= ColumnMetaData.IS_SIGNED_NUM;
         }
         // A bit more info if we have column
         if (expression.getExprType() == SqlExpression.SQLEX_COLUMN) {
             try {
                 SysColumn column = expression.getColumn().getSysColumn(database);
 
                 if (!column.isNullable()) {
                     // switch off default
                     flags &= ~ColumnMetaData.IS_NULLABLE;
                 }
 
                 if (column.isSerial()) {
                     flags |= ColumnMetaData.IS_AUTO_INCREMENT;
                 }
             } catch (Exception noluck) {
             }
         }
 
         return new ColumnMetaData(name, alias, length, xdbType, precision,
                 scale, tableName, flags, true);
     }
 
     /**
      *
      * @return
      */
     public ColumnMetaData[] getMetaData() {
         return columnMetaData;
     }
 
     /**
      * For lookups, allows us to change where to execute query
      * @param nodeId
      */
     public void setSingleQueryNode(int nodeId) {
         DBNode dbNode = database.getDBNode(nodeId);
 
         queryNodeTable = new HashMap<Integer,DBNode>();
         queryNodeTable.put(nodeId, dbNode);
     }
 
     /**
      * getLimit
      * @return
      */
     public long getLimit() {
         return limit;
     }
 
     /**
      * getOffset
      * @return
      */
     public long getOffset() {
         return offset;
     }
 
     /**
      *
      * @return
      */
     public SysTable getIntoTable() {
     	return intoTable;
     }
 
     /**
      *
      * @return
      */
     public SyncCreateTable getSyncCreateTable() {
     	return syncCreateTable;
     }
 
     /**
      *
      * @return
      */
     public String getIntoTableReferenceName() {
     	return intoTableRefName;
     }
 
     /**
      *
      * @return
      */
     public int getCurrentOuterCounter() {
         return topQueryPlan.outerCounter;
     }
 
     /**
      *
      * @return
      */
     public int incrementOuterCounter() {
         return topQueryPlan.outerCounter++;
     }
 
     /**
      *
      * @return
      */
     protected SysDatabase getSysDatabase() {
         return database;
     }
 
     /**
      * Creates an expression that has the steps to compute var_pop
      * of the given expression
      *
      * @param   aSqlExpression
      * @param   newNodeProjList
      * @param   newSqlExpression    [out]   Output expression
      * @param   expr                [in]    Input expression
      *
      * @return
      */
     private void makeVarPopExpression(SqlExpression aSqlExpression,
             List<SqlExpression> newNodeProjList, SqlExpression newSqlExpression, String expr) {
         // Do not worry about the distinct case.
 
         /**
          * We need to do the following:
          * var_pop(expr) =
          *              SUM(expr^2) - SUM(expr)^2
          *                            ------------
          *                            COUNT(expr)
          *              ---------------------------
          *                      COUNT(expr)
          **/
 
         // Step 1: Calculate the following
         //      a: SUM(expr^2) for non null expr values
         //      b: SUM(expr)   for non null expr values
         //      c: COUNT(expr) for non null expr values
         SqlExpression aNewSE = aSqlExpression.copy();
         aNewSE.setFunctionId(IFunctionID.SUM_ID);
         aNewSE.setFunctionName("SUM");
         aNewSE.setExprString("SUM(" + "CASE WHEN " + expr + " IS NULL THEN 0 "
                 + "ELSE (" + expr + " * " + expr + ") END)");
         aNewSE.setTempExpr(true);
         String newColumnName1 = generateColumnName(aSqlExpression.getExprString());
         aNewSE.setAlias(newColumnName1);
 
         newNodeProjList.add(aNewSE);
 
         aNewSE = aSqlExpression.copy();
         aNewSE.setFunctionId(IFunctionID.SUM_ID);
         aNewSE.setFunctionName("SUM");
         aNewSE.setExprString("SUM(" + "CASE WHEN " + expr + " IS NULL THEN 0 "
                 + "ELSE " + expr + " END)");
         aNewSE.setTempExpr(true);
         String newColumnName2 = generateColumnName(aSqlExpression.getExprString());
         aNewSE.setAlias(newColumnName2);
 
         newNodeProjList.add(aNewSE);
 
         aNewSE = aSqlExpression.copy();
         aNewSE.setFunctionId(IFunctionID.SUM_ID);
         aNewSE.setFunctionName("SUM");
         aNewSE.setExprString("SUM(" + "CASE WHEN " + expr + " IS NULL THEN 0 "
                 + "ELSE " + 1 + " END)");
         aNewSE.setTempExpr(true);
         String newColumnName3 = generateColumnName(aSqlExpression.getExprString());
         aNewSE.setAlias(newColumnName3);
 
         newNodeProjList.add(aNewSE);
 
         /**
          * Step 2: Put the results of Step 1 in the formula
          * var_pop(expr) =
          *              SUM(xcol1) -  POWER(SUM(xcol2), 2)
          *                            --------------------
          *                                  SUM(xcol3)
          *              -------------------------------------
          *                         SUM(xcol3)
          *
          **/
         newSqlExpression.setExprType(SqlExpression.SQLEX_OPERATOR_EXPRESSION);
 
         SqlExpression aNewSE_SumXCOL1 = SqlExpression.createNewTempFunction(
                 "SUM", IFunctionID.SUM_ID);
         aNewSE_SumXCOL1.setExprString("SUM("
                 + IdentifierHandler.quote(newColumnName1) + ")");
 
         SqlExpression aNewSE_SumXCOL2 = SqlExpression.createNewTempFunction(
                 "SUM", IFunctionID.SUM_ID);
         aNewSE_SumXCOL2.setExprString("SUM("
                 + IdentifierHandler.quote(newColumnName2) + ")");
 
         // have to worry about division by zero error, therefore the CASE step
         SqlExpression aNewSE_SumXCOL3 = SqlExpression.createNewTempFunction(
                 "CASE", IFunctionID.CASE_ID);
         aNewSE_SumXCOL3.setExprString("CASE WHEN SUM("
                 + IdentifierHandler.quote(newColumnName3)
                 + ") = 0 THEN NULL " + "ELSE SUM("
                 + IdentifierHandler.quote(newColumnName3) + ") END");
 
         SqlExpression aNewSE_Power = SqlExpression.createNewTempFunction(
                 "POWER", IFunctionID.CUSTOM_ID);
         aNewSE_Power.setExprString("POWER(SUM("
                 + IdentifierHandler.quote(newColumnName2) + "), 2)");
 
         aNewSE = SqlExpression.createNewTempOpExpression("/", aNewSE_Power,
                 aNewSE_SumXCOL3);
         aNewSE.setExprString("(" + aNewSE_Power.getExprString() + " / "
                 + aNewSE_SumXCOL3.getExprString() + ")");
 
         SqlExpression aNewSE_Nr = SqlExpression.createNewTempOpExpression("-",
                 aNewSE_SumXCOL1, aNewSE);
         aNewSE_Nr.setExprString("(" + aNewSE_SumXCOL1.getExprString() + " - "
                 + aNewSE.getExprString() + ")");
 
         newSqlExpression.setLeftExpr(aNewSE_Nr);
         newSqlExpression.setRightExpr(aNewSE_SumXCOL3);
         newSqlExpression.setExprString("(" + aNewSE_Nr.getExprString() + " / "
                 + aNewSE_SumXCOL3.getExprString() + ")");
         newSqlExpression.setTempExpr(true);
 
         colMappings.put(aSqlExpression, newSqlExpression.getExprString());
     }
 
     /**
      * Creates an expression that has the steps to compute var_pop
      * of the given expression
      *
      * @param   aSqlExpression
      * @param   newNodeProjList
      * @param   newSqlExpression    [out]   Output expression
      * @param   expr1               [in]    Input expression1
      * @param   expr2               [in]    Input expression2
      *
      * Overloaded function for var_pop that takes 2 parameters. This will be used
      * when var_pop is used in some other function involving 2 parameters
      * e.g. regr_r2
      *
      * @return
      */
     private void makeVarPopExpression(SqlExpression aSqlExpression,
             List<SqlExpression> newNodeProjList, SqlExpression newSqlExpression,
             String expr1, String expr2) {
         /**
          *
          * Do not worry about the distinct case.
          * Oracle does not support var_pop(distinct(col)), so our
          * results with distinct or without distinct does not matter
          * as we have no results to compare
          *
          * We need to do the following:
          * var_pop(expr) =
          *              SUM(expr^2) - SUM(expr)^2
          *                            ------------
          *                            COUNT(expr)
          *              ---------------------------
          *                      COUNT(expr)
          *
          * Step 1: Calculate the following
          *      a: SUM(expr^2) for non null expr values
          *      b: SUM(expr)   for non null expr values
          *      c: COUNT(expr) for non null expr values
          *
          **/
 
         SqlExpression aNewSE = aSqlExpression.copy();
         aNewSE.setFunctionId(IFunctionID.SUM_ID);
         aNewSE.setFunctionName("SUM");
         aNewSE.setExprString("SUM(" + "CASE WHEN " + expr1 + " IS NULL OR "
                 + expr2 + " IS NULL THEN 0 " + "ELSE (" + expr1 + " * " + expr1
                 + ") END)");
         aNewSE.setTempExpr(true);
         String newColumnName1 = generateColumnName(aSqlExpression.getExprString());
         aNewSE.setAlias(newColumnName1);
 
         newNodeProjList.add(aNewSE);
 
         aNewSE = aSqlExpression.copy();
         aNewSE.setFunctionId(IFunctionID.SUM_ID);
         aNewSE.setFunctionName("SUM");
         aNewSE.setExprString("SUM(" + "CASE WHEN " + expr1 + " IS NULL OR "
                 + expr2 + " IS NULL THEN 0 " + "ELSE " + expr1 + " END)");
         aNewSE.setTempExpr(true);
         String newColumnName2 = generateColumnName(aSqlExpression.getExprString());
         aNewSE.setAlias(newColumnName2);
 
         newNodeProjList.add(aNewSE);
 
         aNewSE = aSqlExpression.copy();
         aNewSE.setFunctionId(IFunctionID.SUM_ID);
         aNewSE.setFunctionName("SUM");
         aNewSE.setExprString("SUM(" + "CASE WHEN " + expr1 + " IS NULL OR "
                 + expr2 + " IS NULL THEN 0 " + "ELSE " + 1 + " END)");
         aNewSE.setTempExpr(true);
         String newColumnName3 = generateColumnName(aSqlExpression.getExprString());
         aNewSE.setAlias(newColumnName3);
 
         newNodeProjList.add(aNewSE);
 
         /**
          *
          * -----------------------------------------------------
          * Step 2: Put the results of Step 1 in the formula
          * var_pop(expr) =
          *              SUM(xcol1) -  POWER(SUM(xcol2), 2)
          *                            --------------------
          *                                  SUM(xcol3)
          *              -------------------------------------
          *                         SUM(xcol3)
          *
          **/
 
         newSqlExpression.setExprType(SqlExpression.SQLEX_OPERATOR_EXPRESSION);
 
         SqlExpression aNewSE_SumXCOL1 = SqlExpression.createNewTempFunction(
                 "SUM", IFunctionID.SUM_ID);
         aNewSE_SumXCOL1.setExprString("SUM("
                 + IdentifierHandler.quote(newColumnName1) + ")");
 
         SqlExpression aNewSE_SumXCOL2 = SqlExpression.createNewTempFunction(
                 "SUM", IFunctionID.SUM_ID);
         aNewSE_SumXCOL2.setExprString("SUM("
                 + IdentifierHandler.quote(newColumnName2) + ")");
 
         // have to worry about division by zero error, therefore the CASE step
         SqlExpression aNewSE_SumXCOL3 = SqlExpression.createNewTempFunction(
                 "CASE", IFunctionID.CASE_ID);
         aNewSE_SumXCOL3.setExprString("CASE WHEN SUM("
                 + IdentifierHandler.quote(newColumnName3)
                 + ") = 0 THEN NULL " + "ELSE SUM("
                 + IdentifierHandler.quote(newColumnName3) + ") END");
 
         SqlExpression aNewSE_Power = SqlExpression.createNewTempFunction(
                 "POWER", IFunctionID.CUSTOM_ID);
         aNewSE_Power.setExprString("POWER(SUM("
                 + IdentifierHandler.quote(newColumnName2) + "), 2)");
 
         aNewSE = SqlExpression.createNewTempOpExpression("/", aNewSE_Power,
                 aNewSE_SumXCOL3);
         aNewSE.setExprString("(" + aNewSE_Power.getExprString() + " / "
                 + aNewSE_SumXCOL3.getExprString() + ")");
 
         SqlExpression aNewSE_Nr = SqlExpression.createNewTempOpExpression("-",
                 aNewSE_SumXCOL1, aNewSE);
         aNewSE_Nr.setExprString("(" + aNewSE_SumXCOL1.getExprString() + " - "
                 + aNewSE.getExprString() + ")");
 
         newSqlExpression.setLeftExpr(aNewSE_Nr);
         newSqlExpression.setRightExpr(aNewSE_SumXCOL3);
         newSqlExpression.setExprString("(" + aNewSE_Nr.getExprString() + " / "
                 + aNewSE_SumXCOL3.getExprString() + ")");
         newSqlExpression.setTempExpr(true);
 
         colMappings.put(aSqlExpression, newSqlExpression.getExprString());
     }
 
     /**
      * Creates an expression that has the steps to compute covar_pop
      * of the given expressions
      *
      * @param   aSqlExpression
      * @param   newNodeProjList
      * @param   newSqlExpression    [out]   Output expression
      * @param   expr1               [in]    Input expression
      * @param   expr2               [in]    Input expression
      *
      * @return
      */
     private void makeCovarPopExpression(SqlExpression aSqlExpression,
             List<SqlExpression> newNodeProjList, SqlExpression newSqlExpression,
             String expr1, String expr2) {
 
         /**
          *
          * We need to do:
          *  SUM(expr1 * expr2)    -     SUM(expr2) * SUM(expr1)
          *                              -------------------------
          *                                        n
          *  -----------------------------------------------------
          *                         n
          *  where n is the number of (expr1, expr2) pairs where neither
          *  expr1 nor expr2 is null.
          *
          **/
 
         // Step 0
         // SUM(expr1 * expr2)
         String newColumnName1 = generateColumnName(aSqlExpression.getExprString());
         SqlExpression aNewSE = aSqlExpression.copy();
         aNewSE.setExprType(SqlExpression.SQLEX_FUNCTION);
         aNewSE.setFunctionId(IFunctionID.SUM_ID);
         aNewSE.setFunctionName("SUM");
         aNewSE.setExprString("SUM(" + "CASE WHEN " + expr1 + " IS NULL OR "
                 + expr2 + " IS NULL THEN 0 " + "ELSE (" + expr1 + " * " + expr2
                 + " ) END)");
         aNewSE.setTempExpr(true);
         aNewSE.setAlias(newColumnName1);
         newNodeProjList.add(aNewSE);
 
         // SUM(expr2)
         aNewSE = aSqlExpression.copy();
         String newColumnName2 = generateColumnName(aSqlExpression.getExprString());
         logger.debug("new column name=" + newColumnName2);
         aNewSE.setExprType(SqlExpression.SQLEX_FUNCTION);
         aNewSE.setFunctionId(IFunctionID.SUM_ID);
         aNewSE.setFunctionName("SUM");
         aNewSE.setExprString("SUM(" + "CASE WHEN " + expr1 + " IS NULL OR "
                 + expr2 + " IS NULL THEN 0 " + "ELSE (" + expr2 + " ) END)");
         aNewSE.setTempExpr(true);
         aNewSE.setAlias(newColumnName2);
         newNodeProjList.add(aNewSE);
 
         // SUM(expr1)
         aNewSE = aSqlExpression.copy();
         String newColumnName3 = generateColumnName(aSqlExpression.getExprString());
         aNewSE.setExprType(SqlExpression.SQLEX_FUNCTION);
         aNewSE.setFunctionId(IFunctionID.SUM_ID);
         aNewSE.setFunctionName("SUM");
         aNewSE.setExprString("SUM(" + "CASE WHEN " + expr1 + " IS NULL OR "
                 + expr2 + " IS NULL THEN 0 " + "ELSE (" + expr1 + " ) END)");
         aNewSE.setTempExpr(true);
         aNewSE.setAlias(newColumnName3);
         newNodeProjList.add(aNewSE);
 
         // Count non-null pairs of (expr1, expr2)
         aNewSE = aSqlExpression.copy();
         String newColumnName4 = generateColumnName(aSqlExpression.getExprString());
         logger.debug("new column name=" + newColumnName4);
         aNewSE.setExprType(SqlExpression.SQLEX_FUNCTION);
         aNewSE.setFunctionId(IFunctionID.SUM_ID);
         aNewSE.setFunctionName("SUM");
         aNewSE.setExprString("SUM(" + "CASE WHEN " + expr1 + " IS NULL OR "
                 + expr2 + " IS NULL THEN 0 " + "ELSE 1 END)");
         aNewSE.setTempExpr(true);
         aNewSE.setAlias(newColumnName4);
         newNodeProjList.add(aNewSE);
 
         /**
          *
          * Step - 1 Final agg step
          * Put the results of previous step in the following formula
          *  SUM(xcol1)    -     SUM(xcol2) * SUM(xcol3)
          *                      -----------------------
          *                            SUM(xcol4)
          *  --------------------------------------------
          *                   SUM(xcol4)
          *
          **/
         SqlExpression aNewSE_SumXCOL1 = SqlExpression.createNewTempFunction(
                 "SUM", IFunctionID.SUM_ID);
         aNewSE_SumXCOL1.setExprString("SUM("
                 + IdentifierHandler.quote(newColumnName1) + ")");
 
         SqlExpression aNewSE_SumXCOL2 = SqlExpression.createNewTempFunction(
                 "SUM", IFunctionID.SUM_ID);
         aNewSE_SumXCOL2.setExprString("SUM("
                 + IdentifierHandler.quote(newColumnName2) + ")");
 
         SqlExpression aNewSE_SumXCOL3 = SqlExpression.createNewTempFunction(
                 "SUM", IFunctionID.SUM_ID);
         aNewSE_SumXCOL3.setExprString("SUM("
                 + IdentifierHandler.quote(newColumnName3) + ")");
 
         /*
          * Special handling for the case when there is no
          * non-null (expr1, expr2) pairs.
          * In that case we return the count as NULL instead of 0, eliminating the
          * possibility of a divide by zero error in the subsequent step
          */
         SqlExpression aNewSE_SumXCOL4 = SqlExpression.createNewTempFunction(
                 "CASE", IFunctionID.CASE_ID);
         aNewSE_SumXCOL4.setExprString("(CASE WHEN SUM("
                 + IdentifierHandler.quote(newColumnName4)
                 + ") = 0 THEN NULL ELSE SUM("
                 + IdentifierHandler.quote(newColumnName4) + ") END)");
 
         // Compute SUM(expr2) * SUM(expr1)
         SqlExpression tempSqlExpression = SqlExpression.createNewTempOpExpression(
                 "*", aNewSE_SumXCOL2, aNewSE_SumXCOL3);
         tempSqlExpression.setExprString("(" + aNewSE_SumXCOL2.getExprString() + " * "
                 + aNewSE_SumXCOL3.getExprString() + ")");
 
         // Compute tempSqlExpression/(SUM(xcol4))
         SqlExpression tempSqlExpression2 = SqlExpression.createNewTempOpExpression(
                 "/", tempSqlExpression, aNewSE_SumXCOL4);
         tempSqlExpression2.setExprString("(" + tempSqlExpression.getExprString()
                 + " / " + aNewSE_SumXCOL4.getExprString() + ")");
 
         // Compute SUM(expr1 * expr2) - tempSqlExpression2
         SqlExpression tempSqlExpression3_nr = SqlExpression.createNewTempOpExpression(
                 "-", aNewSE_SumXCOL3, tempSqlExpression2);
         tempSqlExpression3_nr.setExprString("(" + aNewSE_SumXCOL1.getExprString()
                 + " - (" + tempSqlExpression2.getExprString() + "))");
 
         // Compute tempSqlExpression3_nr/N
         newSqlExpression.setExprType(SqlExpression.SQLEX_OPERATOR_EXPRESSION);
         newSqlExpression.setOperator("/");
         newSqlExpression.setLeftExpr(tempSqlExpression3_nr);
         newSqlExpression.setRightExpr(aNewSE_SumXCOL4);
         newSqlExpression.setExprString("((" + tempSqlExpression3_nr.getExprString()
                 + ") / " + aNewSE_SumXCOL4.getExprString() + ")");
         newSqlExpression.setTempExpr(true);
 
         colMappings.put(aSqlExpression, newSqlExpression.getExprString());
     }
 
     /**
      * Creates an expression that has the steps to compute corr
      * of the given expressions
      *
      * @param   aSqlExpression
      * @param   newNodeProjList
      * @param   newSqlExpression    [out]   Output expression
      * @param   expr1               [in]    Input expression1
      * @param   expr2               [in]    Input expression2
      *
      * @return
      */
     private void makeCorrExpression(SqlExpression aSqlExpression,
             List<SqlExpression> newNodeProjList, SqlExpression newSqlExpression,
             String expr1, String expr2) {
         /**
          *
          * We need to do:
          *  corr(expr1, expr2) =
          *          covar_pop(expr1, expr2)
          *     -------------------------------------
          *      stddev_pop(expr1) * stddev_pop(expr2)
          *
          * i.e.
          *      SUM(expr1 * expr2) - SUM(expr2) * SUM(expr1)
          *                           -------------------------
          *                                     n
          *      ------------------------------------------------
          *                          n
          * ---------------------------------------------------------------------------
          *       / SUM(expr1^2) - SUM(expr1)^2 \        / SUM(expr2^2) - SUM(expr2)^2 \
          *      |                -------------  |      |                -------------  |
          *  SQRT|                     n         | *SQRT|                     n         |
          *      | ----------------------------- |      | ----------------------------- |
          *       \             n               /        \               n             /
          *
          *
          * where n is the number of (expr1, expr2) pairs where neither
          *  expr1 nor expr2 is null.
          *
          **/
 
         // Step 0
         // SUM(expr1 * expr2)
         String newColumnName1 = generateColumnName(aSqlExpression.getExprString());
         SqlExpression aNewSE = SqlExpression.createNewTempFunction("SUM",
                 IFunctionID.SUM_ID);
         aNewSE.setExprString("SUM(" + "CASE WHEN " + expr1 + " IS NULL OR "
                 + expr2 + " IS NULL THEN 0 " + "ELSE (" + expr1 + " * " + expr2
                 + " ) END)");
         aNewSE.setAlias(newColumnName1);
         aNewSE.setExprDataType(aSqlExpression.getExprDataType());
         newNodeProjList.add(aNewSE);
 
         // SUM(expr2)
         aNewSE = SqlExpression.createNewTempFunction("SUM", IFunctionID.SUM_ID);
         String newColumnName2 = generateColumnName(aSqlExpression.getExprString());
         aNewSE.setExprString("SUM(" + "CASE WHEN " + expr1 + " IS NULL OR "
                 + expr2 + " IS NULL THEN 0 " + "ELSE (" + expr2 + " ) END)");
         aNewSE.setAlias(newColumnName2);
         aNewSE.setExprDataType(aSqlExpression.getExprDataType());
         newNodeProjList.add(aNewSE);
 
         // SUM(expr1)
         aNewSE = SqlExpression.createNewTempFunction("SUM", IFunctionID.SUM_ID);
         String newColumnName3 = generateColumnName(aSqlExpression.getExprString());
         aNewSE.setExprString("SUM(" + "CASE WHEN " + expr1 + " IS NULL OR "
                 + expr2 + " IS NULL THEN 0 " + "ELSE (" + expr1 + " ) END)");
         aNewSE.setAlias(newColumnName3);
         aNewSE.setExprDataType(aSqlExpression.getExprDataType());
         newNodeProjList.add(aNewSE);
 
         // Count non-null pairs of (expr1, expr2)
         aNewSE = SqlExpression.createNewTempFunction("SUM", IFunctionID.SUM_ID);
         String newColumnName4 = generateColumnName(aSqlExpression.getExprString());
         aNewSE.setExprString("SUM(" + "CASE WHEN " + expr1 + " IS NULL OR "
                 + expr2 + " IS NULL THEN 0 " + "ELSE 1 END)");
         aNewSE.setAlias(newColumnName4);
         aNewSE.setExprDataType(aSqlExpression.getExprDataType());
         newNodeProjList.add(aNewSE);
 
         // SUM(expr1 * expr1)
         String newColumnName5 = generateColumnName(aSqlExpression.getExprString());
         aNewSE = SqlExpression.createNewTempFunction("SUM", IFunctionID.SUM_ID);
         aNewSE.setExprString("SUM(" + "CASE WHEN " + expr1 + " IS NULL OR "
                 + expr2 + " IS NULL THEN 0 " + "ELSE (" + expr1 + " * " + expr1
                 + " ) END)");
         aNewSE.setAlias(newColumnName5);
         aNewSE.setExprDataType(aSqlExpression.getExprDataType());
         newNodeProjList.add(aNewSE);
 
         // SUM(expr2 * expr2)
         String newColumnName6 = generateColumnName(aSqlExpression.getExprString());
         aNewSE = SqlExpression.createNewTempFunction("SUM", IFunctionID.SUM_ID);
         aNewSE.setExprString("SUM(" + "CASE WHEN " + expr1 + " IS NULL OR "
                 + expr2 + " IS NULL THEN 0 " + "ELSE (" + expr2 + " * " + expr2
                 + " ) END)");
         aNewSE.setAlias(newColumnName6);
         aNewSE.setExprDataType(aSqlExpression.getExprDataType());
         newNodeProjList.add(aNewSE);
 
         /**
          *
          * Step - 2
          * Put the results of previous step in the following formula
          *      SUM(xcol1) - SUM(xcol2) * SUM(xcol3)
          *                   -------------------------
          *                             SUM(xcol4)
          *      ------------------------------------------------
          *                          SUM(xcol4)
          * ---------------------------------------------------------------------------
          *       / SUM(xcol5) - POWER(SUM(xcol3), 2) \        / SUM(xcol6) -  POWER(SUM(xcol2), 2) \
          *      |                -------------------  |      |                --------------------  |
          *  SQRT|                   SUM(xcol4)        | *SQRT|                     SUM(xcol4)       |
          *      | ----------------------------------  |      | -----------------------------------  |
          *       \             SUM(xcol4)            /        \               SUM(xcol4)           /
          *
          *
          **/
         SqlExpression aNewSE_SumXCOL1 = SqlExpression.createNewTempFunction(
                 "SUM", IFunctionID.SUM_ID);
         aNewSE_SumXCOL1.setExprString("SUM("
                 + IdentifierHandler.quote(newColumnName1) + ")");
 
         SqlExpression aNewSE_SumXCOL2 = SqlExpression.createNewTempFunction(
                 "SUM", IFunctionID.SUM_ID);
         aNewSE_SumXCOL2.setExprString("SUM("
                 + IdentifierHandler.quote(newColumnName2) + ")");
 
         SqlExpression aNewSE_SumXCOL3 = SqlExpression.createNewTempFunction(
                 "SUM", IFunctionID.SUM_ID);
         aNewSE_SumXCOL3.setExprString("SUM("
                 + IdentifierHandler.quote(newColumnName3) + ")");
 
         /*
          * Special handling for the case when there is no
          * non-null (expr1, expr2) pairs.
          * In that case we return the count as NULL instead of 0, eliminating the
          * possibility of a divide by zero error in the subsequent step
          */
         SqlExpression aNewSE_SumXCOL4 = SqlExpression.createNewTempFunction(
                 "CASE", IFunctionID.CASE_ID);
         aNewSE_SumXCOL4.setExprString("(CASE WHEN SUM("
                 + IdentifierHandler.quote(newColumnName4)
                 + ") = 0 THEN NULL ELSE SUM("
                 + IdentifierHandler.quote(newColumnName4) + ") END)");
 
         // Compute SUM(expr2) * SUM(expr1)
         SqlExpression tempSqlExpression = SqlExpression.createNewTempOpExpression(
                 "*", aNewSE_SumXCOL2, aNewSE_SumXCOL3);
         tempSqlExpression.setExprString("(" + aNewSE_SumXCOL2.getExprString() + " * "
                 + aNewSE_SumXCOL3.getExprString() + ")");
 
         // Compute tempSqlExpression/(SUM(xcol4))
         SqlExpression tempSqlExpression2 = SqlExpression.createNewTempOpExpression(
                 "/", tempSqlExpression, aNewSE_SumXCOL4);
         tempSqlExpression2.setExprString("(" + tempSqlExpression.getExprString()
                 + " / " + aNewSE_SumXCOL4.getExprString() + ")");
 
         // Compute SUM(expr1 * expr2) - tempSqlExpression2
         SqlExpression tempSqlExpression3_nr = SqlExpression.createNewTempOpExpression(
                 "-", tempSqlExpression2, aNewSE_SumXCOL3);
         tempSqlExpression3_nr.setExprString("(" + aNewSE_SumXCOL1.getExprString()
                 + " - (" + tempSqlExpression2.getExprString() + "))");
 
         // Compute tempSqlExpression3_nr/N
         SqlExpression aNewSE_covar_pop = SqlExpression.createNewTempOpExpression(
                 "/", tempSqlExpression3_nr, aNewSE_SumXCOL4);
         aNewSE_covar_pop.setExprString("(" + tempSqlExpression3_nr.getExprString()
                 + ") / " + aNewSE_SumXCOL4.getExprString());
 
         SqlExpression aNewSE_SumXCOL5 = SqlExpression.createNewTempFunction(
                 "SUM", IFunctionID.SUM_ID);
         aNewSE_SumXCOL5.setExprString("SUM("
                 + IdentifierHandler.quote(newColumnName5) + ")");
 
         SqlExpression aNewSE_Power = SqlExpression.createNewTempFunction(
                 "POWER", IFunctionID.CUSTOM_ID);
         aNewSE_Power.setExprString("POWER(SUM("
                 + IdentifierHandler.quote(newColumnName3) + "), 2)");
 
         aNewSE = SqlExpression.createNewTempOpExpression("/", aNewSE_Power,
                 aNewSE_SumXCOL4);
         aNewSE.setExprString("(" + aNewSE_Power.getExprString() + " / "
                 + aNewSE_SumXCOL4.getExprString() + ")");
 
         SqlExpression aNewSE_Nr = SqlExpression.createNewTempOpExpression("-",
                 aNewSE_SumXCOL5, aNewSE);
         aNewSE_Nr.setExprString("(" + aNewSE_SumXCOL5.getExprString() + " - "
                 + aNewSE.getExprString() + ")");
 
         SqlExpression aNewSE_var_pop_e1 = SqlExpression.createNewTempOpExpression(
                 "/", aNewSE_Nr, aNewSE_SumXCOL4);
         aNewSE_var_pop_e1.setExprString("(" + aNewSE_Nr.getExprString() + " / "
                 + aNewSE_SumXCOL4.getExprString() + ")");
 
         SqlExpression aNewSE_stddev_pop_e1 = SqlExpression.createNewTempFunction(
                 "SQRT", IFunctionID.CUSTOM_ID);
         aNewSE_stddev_pop_e1.setExprString("SQRT("
                 + aNewSE_var_pop_e1.getExprString() + ")");
 
         SqlExpression aNewSE_SumXCOL6 = SqlExpression.createNewTempFunction(
                 "SUM", IFunctionID.SUM_ID);
         aNewSE_SumXCOL6.setExprString("SUM("
                 + IdentifierHandler.quote(newColumnName6) + ")");
 
         aNewSE_Power = SqlExpression.createNewTempFunction("POWER",
                 IFunctionID.CUSTOM_ID);
         aNewSE_Power.setExprString("POWER(SUM("
                 + IdentifierHandler.quote(newColumnName2) + "), 2)");
 
         aNewSE = SqlExpression.createNewTempOpExpression("/", aNewSE_Power,
                 aNewSE_SumXCOL4);
         aNewSE.setExprString("(" + aNewSE_Power.getExprString() + " / "
                 + aNewSE_SumXCOL4.getExprString() + ")");
 
         aNewSE_Nr = SqlExpression.createNewTempOpExpression("-",
                 aNewSE_SumXCOL6, aNewSE);
         aNewSE_Nr.setExprString("(" + aNewSE_SumXCOL6.getExprString() + " - "
                 + aNewSE.getExprString() + ")");
 
         SqlExpression aNewSE_var_pop_e2 = SqlExpression.createNewTempOpExpression(
                 "/", aNewSE_Nr, aNewSE_SumXCOL4);
         aNewSE_var_pop_e2.setExprString("(" + aNewSE_Nr.getExprString() + " / "
                 + aNewSE_SumXCOL4.getExprString() + ")");
 
         SqlExpression aNewSE_stddev_pop_e2 = SqlExpression.createNewTempFunction(
                 "SQRT", IFunctionID.CUSTOM_ID);
         aNewSE_stddev_pop_e2.setExprString("SQRT("
                 + aNewSE_var_pop_e2.getExprString() + ")");
 
         // Denominator can be 0 giving divide by zero error, handle that
         SqlExpression aNewSE_dr = SqlExpression.createNewTempFunction("CASE",
                 IFunctionID.CASE_ID);
         aNewSE_dr.setExprString("CASE WHEN (" + aNewSE_stddev_pop_e1.getExprString()
                 + " * " + aNewSE_stddev_pop_e2.getExprString()
                 + ") = 0 THEN NULL ELSE " + "("
                 + aNewSE_stddev_pop_e1.getExprString() + " * "
                 + aNewSE_stddev_pop_e2.getExprString() + ") END");
 
         newSqlExpression.setExprType(SqlExpression.SQLEX_OPERATOR_EXPRESSION);
         newSqlExpression.setOperator("/");
         newSqlExpression.setLeftExpr(aNewSE_covar_pop);
         newSqlExpression.setRightExpr(aNewSE_dr);
         newSqlExpression.setExprString("(" + aNewSE_covar_pop.getExprString() + "/"
                 + aNewSE_dr.getExprString() + ")");
         newSqlExpression.setTempExpr(true);
 
         colMappings.put(aSqlExpression, newSqlExpression.getExprString());
     }
 
     /**
      * Creates an expression that has the steps to compute stddev_pop
      * of the given expression
      *
      * @param   aSqlExpression
      * @param   newNodeProjList
      * @param   newSqlExpression    [out]   Output expression
      * @param   expr                [in]    Input expression
      *
      * @return
      */
     private void makeStddevPopExpression(SqlExpression aSqlExpression,
             List<SqlExpression> newNodeProjList, SqlExpression newSqlExpression, String expr) {
         /**
          *
          * stddev_pop(expr) = SQRT(var_pop(expr))
          *
          **/
         SqlExpression aNewSE_var_pop = new SqlExpression();
         makeVarPopExpression(aSqlExpression, newNodeProjList, aNewSE_var_pop,
                 expr);
         newSqlExpression.setExprType(SqlExpression.SQLEX_FUNCTION);
         newSqlExpression.setExprString("SQRT(" + aNewSE_var_pop.getExprString() + ")");
         newSqlExpression.getFunctionParams().add(aNewSE_var_pop);
         newSqlExpression.setTempExpr(true);
 
         colMappings.put(aSqlExpression, newSqlExpression.getExprString());
     }
 
     /**
      * Creates an expression that has the steps to compute regr_count
      * of the given expressions
      *
      * @param   aSqlExpression
      * @param   newNodeProjList
      * @param   newSqlExpression    [out]   Output expression
      * @param   expr1               [in]    Input expression1
      * @param   expr2               [in]    Input expression2
      *
      * @return
      */
     private void makeRegrCountExpression(SqlExpression aSqlExpression,
             List<SqlExpression> newNodeProjList, SqlExpression newSqlExpression,
             String expr1, String expr2) {
         // Note we do not care if it is DISTINCT, the result will
         // be the same. So, there is no need to defer when
         // distinct is present
 
         /**
          *
          * We need to do:
          * regr_count(expr1, expr2) =
          *              sum(count non-null pairs of expr1, expr2)
          *
          **/
 
         // Step 1: Calculate the count of non null pairs of expr1, expr2
         String newColumnName = generateColumnName(aSqlExpression.getExprString());
 
         SqlExpression aNewSE = aSqlExpression.copy();
         aNewSE.setFunctionId(IFunctionID.SUM_ID);
         aNewSE.setFunctionName("SUM");
         aNewSE.setExprString("SUM(" + "CASE WHEN " + expr1 + " IS NULL OR "
                 + expr2 + " IS NULL THEN 0 " + "ELSE 1 END)");
 
         aNewSE.setTempExpr(true);
         aNewSE.setAlias(newColumnName);
 
         newNodeProjList.add(aNewSE);
 
         // -----------------------------------------------------
         // Step 2: Add the results of Step 1
 
         newSqlExpression.setExprType(SqlExpression.SQLEX_FUNCTION);
         newSqlExpression.setFunctionId(SqlExpression.SQLEX_CASE);
         newSqlExpression.setFunctionName("CASE");
         // When there are no rows returned, return 0 as the result
         newSqlExpression.setExprString("(CASE WHEN SUM("
                 + IdentifierHandler.quote(newColumnName)
                 + ") IS NULL THEN 0 " + "ELSE SUM("
                 + IdentifierHandler.quote(newColumnName) + ") END)");
         newSqlExpression.setTempExpr(true);
 
         colMappings.put(aSqlExpression, newSqlExpression.getExprString());
     }
 
     /**
      * Creates an expression that has the steps to compute regr_sxx
      * of the given expressions
      *
      * @param   aSqlExpression
      * @param   newNodeProjList
      * @param   newSqlExpression    [out]   Output expression
      * @param   expr1               [in]    Input expression1
      * @param   expr2               [in]    Input expression2
      *
      * @return
      */
     private void makeRegrSxxExpression(SqlExpression aSqlExpression,
             List<SqlExpression> newNodeProjList, SqlExpression newSqlExpression,
             String expr1, String expr2) {
         /**
          *
          * Note we do not care if it is DISTINCT, the result will
          * be the same. So, there is no need to defer when
          * distinct is present
          *
          * We need to do:
          * regr_sxx(expr1, expr2) =
          *              regr_count(expr1, expr2) * var_pop(expr2)
          *
          *
          **/
 
         SqlExpression aNewSE_regrcount = new SqlExpression();
         makeRegrCountExpression(aSqlExpression, newNodeProjList,
                 aNewSE_regrcount, expr1, expr2);
 
         SqlExpression aNewSE_varpop = new SqlExpression();
         makeVarPopExpression(aSqlExpression, newNodeProjList, aNewSE_varpop,
                 expr2, expr1);
 
         newSqlExpression.setExprType(SqlExpression.SQLEX_OPERATOR_EXPRESSION);
         newSqlExpression.setOperator("*");
         newSqlExpression.setLeftExpr(aNewSE_regrcount);
         newSqlExpression.setRightExpr(aNewSE_varpop);
         newSqlExpression.setExprString("(" + aNewSE_regrcount.getExprString() + " * "
                 + aNewSE_varpop.getExprString() + ")");
         newSqlExpression.setTempExpr(true);
 
         colMappings.put(aSqlExpression, newSqlExpression.getExprString());
     }
 
     /**
      * Creates an expression that has the steps to compute regr_sxy
      * of the given expressions
      *
      * @param   aSqlExpression
      * @param   newNodeProjList
      * @param   newSqlExpression    [out]   Output expression
      * @param   expr1               [in]    Input expression1
      * @param   expr2               [in]    Input expression1
      *
      * @return
      */
     private void makeRegrSxyExpression(SqlExpression aSqlExpression,
             List<SqlExpression> newNodeProjList, SqlExpression newSqlExpression,
             String expr1, String expr2) {
         /**
          *
          * Note we do not care if it is DISTINCT, the result will
          * be the same. So, there is no need to defer when
          * distinct is present
          *
          * We need to do:
          * regr_sxy(expr1, expr2) =
          *              regr_count(expr1, expr2) * covar_pop(expr1, expr2)
          *
          **/
 
         // Step 0:
         //      Combining step 0 of regr_count and var_pop
         // Step 0 of regr_count
         String newColumnName1 = generateColumnName(aSqlExpression.getExprString());
 
         SqlExpression aNewSE = aSqlExpression.copy();
         aNewSE.setFunctionId(IFunctionID.SUM_ID);
         aNewSE.setFunctionName("SUM");
         aNewSE.setExprString("SUM(" + "CASE WHEN " + expr1 + " IS NULL OR "
                 + expr2 + " IS NULL THEN 0 " + "ELSE 1 END)");
         aNewSE.setTempExpr(true);
         aNewSE.setAlias(newColumnName1);
 
         newNodeProjList.add(aNewSE);
 
         /**
          *
          * Step 0 of covar_pop
          * We need to do:
          *  SUM(expr1 * expr2)    -     SUM(expr2) * SUM(expr1)
          *                              -------------------------
          *                                        n
          *  -----------------------------------------------------
          *                         n
          *  where n is the number of (expr1, expr2) pairs where neither
          *  expr1 nor expr2 is null.
          *
          **/
 
         // Step 0
         // SUM(expr1 * expr2)
         String newColumnName2 = generateColumnName(aSqlExpression.getExprString());
         aNewSE = aSqlExpression.copy();
         aNewSE.setExprType(SqlExpression.SQLEX_FUNCTION);
         aNewSE.setFunctionId(IFunctionID.SUM_ID);
         aNewSE.setFunctionName("SUM");
         aNewSE.setExprString("SUM(" + "CASE WHEN " + expr1 + " IS NULL OR "
                 + expr2 + " IS NULL THEN 0 " + "ELSE (" + expr1 + " * " + expr2
                 + " ) END)");
         aNewSE.setTempExpr(true);
         aNewSE.setAlias(newColumnName2);
         newNodeProjList.add(aNewSE);
 
         // SUM(expr2)
         aNewSE = aSqlExpression.copy();
         String newColumnName3 = generateColumnName(aSqlExpression.getExprString());
         aNewSE.setExprType(SqlExpression.SQLEX_FUNCTION);
         aNewSE.setFunctionId(IFunctionID.SUM_ID);
         aNewSE.setFunctionName("SUM");
         aNewSE.setExprString("SUM(" + "CASE WHEN " + expr1 + " IS NULL OR "
                 + expr2 + " IS NULL THEN 0 " + "ELSE (" + expr2 + " ) END)");
         aNewSE.setTempExpr(true);
         aNewSE.setAlias(newColumnName3);
         newNodeProjList.add(aNewSE);
 
         // SUM(expr1)
         aNewSE = aSqlExpression.copy();
         String newColumnName4 = generateColumnName(aSqlExpression.getExprString());
         aNewSE.setExprType(SqlExpression.SQLEX_FUNCTION);
         aNewSE.setFunctionId(IFunctionID.SUM_ID);
         aNewSE.setFunctionName("SUM");
         aNewSE.setExprString("SUM(" + "CASE WHEN " + expr1 + " IS NULL OR "
                 + expr2 + " IS NULL THEN 0 " + "ELSE (" + expr1 + " ) END)");
         aNewSE.setTempExpr(true);
         aNewSE.setAlias(newColumnName4);
         newNodeProjList.add(aNewSE);
 
         /**
          *
          * Step - 1 Combining the results of regr_count and var_pop
          * Put the results of previous step in the following formula
          *                 SUM(xcol2)    -     SUM(xcol3) * SUM(xcol4)
          *                                     -----------------------
          * SUM(xcol1) *                              SUM(xcol1)
          *                 --------------------------------------------
          *                              SUM(xcol1)
          *
          **/
 
         /*
          * Special handling for the case when there is no
          * non-null (expr1, expr2) pairs.
          * In that case we return the count as NULL instead of 0, eliminating the
          * possibility of a divide by zero error in the subsequent step
          */
         SqlExpression aNewSE_SumXCOL1 = SqlExpression.createNewTempFunction(
                 "CASE", IFunctionID.CASE_ID);
         aNewSE_SumXCOL1.setExprString("(CASE WHEN SUM("
                 + IdentifierHandler.quote(newColumnName1)
                 + ") = 0 THEN NULL ELSE SUM("
                 + IdentifierHandler.quote(newColumnName1) + ") END)");
 
         SqlExpression aNewSE_SumXCOL2 = SqlExpression.createNewTempFunction(
                 "SUM", IFunctionID.SUM_ID);
         aNewSE_SumXCOL2.setExprString("SUM("
                 + IdentifierHandler.quote(newColumnName2) + ")");
 
         SqlExpression aNewSE_SumXCOL3 = SqlExpression.createNewTempFunction(
                 "SUM", IFunctionID.SUM_ID);
         aNewSE_SumXCOL3.setExprString("SUM("
                 + IdentifierHandler.quote(newColumnName3) + ")");
 
         SqlExpression aNewSE_SumXCOL4 = SqlExpression.createNewTempFunction(
                 "SUM", IFunctionID.SUM_ID);
         aNewSE_SumXCOL4.setExprString("SUM("
                 + IdentifierHandler.quote(newColumnName4) + ")");
 
         // Compute SUM(expr2) * SUM(expr1)
         SqlExpression tempSqlExpression = SqlExpression.createNewTempOpExpression(
                 "*", aNewSE_SumXCOL3, aNewSE_SumXCOL4);
         tempSqlExpression.setExprString("(" + aNewSE_SumXCOL3.getExprString() + " * "
                 + aNewSE_SumXCOL4.getExprString() + ")");
 
         // Compute tempSqlExpression/(SUM(xcol1))
         SqlExpression tempSqlExpression2 = SqlExpression.createNewTempOpExpression(
                 "/", tempSqlExpression, aNewSE_SumXCOL1);
         tempSqlExpression2.setExprString("(" + tempSqlExpression.getExprString()
                 + " / " + aNewSE_SumXCOL1.getExprString() + ")");
 
         // Compute SUM(expr1 * expr2) - tempSqlExpression2
         SqlExpression tempSqlExpression3_nr = SqlExpression.createNewTempOpExpression(
                 "-", aNewSE_SumXCOL2, tempSqlExpression2);
         tempSqlExpression3_nr.setExprString("(" + aNewSE_SumXCOL2.getExprString()
                 + " - (" + tempSqlExpression2.getExprString() + "))");
 
         // Compute tempSqlExpression3_nr/N
         SqlExpression aNewSE_covarpop = SqlExpression.createNewTempOpExpression(
                 "/", tempSqlExpression3_nr, aNewSE_SumXCOL1);
         aNewSE_covarpop.setExprString("(" + tempSqlExpression3_nr.getExprString()
                 + ") / " + aNewSE_SumXCOL1.getExprString());
 
         newSqlExpression.setExprType(SqlExpression.SQLEX_OPERATOR_EXPRESSION);
         newSqlExpression.setOperator("*");
         newSqlExpression.setLeftExpr(aNewSE_SumXCOL1);
         newSqlExpression.setRightExpr(aNewSE_covarpop);
         newSqlExpression.setExprString("(" + aNewSE_SumXCOL1.getExprString() + " * "
                 + aNewSE_covarpop.getExprString() + ")");
         newSqlExpression.setTempExpr(true);
 
         colMappings.put(aSqlExpression, newSqlExpression.getExprString());
     }
 
     /**
      * Creates an expression that has the steps to compute regr_r2
      * of the given expressions
      *
      * @param   aSqlExpression
      * @param   newNodeProjList
      * @param   newSqlExpression    [out]   Output expression
      * @param   expr1               [in]    Input expression1
      * @param   expr2               [in]    Input expression1
      *
      * @return
      */
     private void makeRegrR2Expression(SqlExpression aSqlExpression,
             List<SqlExpression> newNodeProjList, SqlExpression newSqlExpression,
             String expr1, String expr2) {
         /**
          *
          * regr_r2 = NULL if VAR_POP(expr2) = 0
          *           1    if VAR_POP(expr1) = 0 and VAR_POP(expr2) != 0
          *           POWER(CORR(expr1,expr),2) otherwise
          *
          **/
 
         SqlExpression aNewSE_varpop_e2 = new SqlExpression();
         makeVarPopExpression(aSqlExpression, newNodeProjList, aNewSE_varpop_e2,
                 expr2, expr1);
 
         SqlExpression aNewSE_varpop_e1 = new SqlExpression();
         makeVarPopExpression(aSqlExpression, newNodeProjList, aNewSE_varpop_e1,
                 expr1, expr2);
 
         SqlExpression aNewSE_corr = new SqlExpression();
         makeCorrExpression(aSqlExpression, newNodeProjList, aNewSE_corr, expr1,
                 expr2);
 
         SqlExpression aNewSE_power = SqlExpression.createNewTempFunction(
                 "POWER", IFunctionID.CUSTOM_ID);
         aNewSE_power.setExprString("POWER(" + aNewSE_corr.getExprString() + ", 2)");
 
         newSqlExpression.setExprType(SqlExpression.SQLEX_CASE);
         newSqlExpression.setFunctionName("CASE");
         newSqlExpression.setFunctionId(IFunctionID.CASE_ID);
         newSqlExpression.setExprString("(CASE " + "WHEN "
                 + aNewSE_varpop_e2.getExprString() + " = 0 THEN NULL " + "WHEN "
                 + aNewSE_varpop_e2.getExprString() + " != 0 AND "
                 + aNewSE_varpop_e1.getExprString() + " = 0 THEN 1 " + "ELSE "
                 + aNewSE_power.getExprString() + " END)");
         newSqlExpression.setTempExpr(true);
 
         colMappings.put(aSqlExpression, newSqlExpression.getExprString());
     }
 
     /**
      * Creates an expression that has the steps to compute regr_slope
      * of the given expressions
      *
      * @param   aSqlExpression
      * @param   newNodeProjList
      * @param   newSqlExpression    [out]   Output expression
      * @param   expr1               [in]    Input expression1
      * @param   expr2               [in]    Input expression1
      *
      * @return
      */
     private void makeRegrSlopeExpression(SqlExpression aSqlExpression,
             List<SqlExpression> newNodeProjList, SqlExpression newSqlExpression,
             String expr1, String expr2) {
         /**
          *
          * We need to do:
          *  regr_slope(expr1, expr2) =
          *          covar_pop(expr1, expr2)
          *     -------------------------------------
          *              var_pop(expr2)
          *
          * i.e.
          *      SUM(expr1 * expr2) - SUM(expr2) * SUM(expr1)
          *                           -------------------------
          *                                     n
          *      ------------------------------------------------
          *                          n
          * ---------------------------------------------------------
          *                  SUM(expr2^2) - SUM(expr2)^2
          *                                --------------
          *                                 COUNT(expr2)
          *                  ---------------------------
          *                          COUNT(expr2)
          *
          * where n is the number of (expr1, expr2) pairs where neither
          *  expr1 nor expr2 is null.
          *
          **/
 
         // Step 0
         // SUM(expr1 * expr2)
         String newColumnName1 = generateColumnName(aSqlExpression.getExprString());
         SqlExpression aNewSE = SqlExpression.createNewTempFunction("SUM",
                 IFunctionID.SUM_ID);
         aNewSE.setExprString("SUM(" + "CASE WHEN " + expr1 + " IS NULL OR "
                 + expr2 + " IS NULL THEN 0 " + "ELSE (" + expr1 + " * " + expr2
                 + " ) END)");
         aNewSE.setAlias(newColumnName1);
         aNewSE.setExprDataType(aSqlExpression.getExprDataType());
         newNodeProjList.add(aNewSE);
 
         // SUM(expr2) when expr1 and expr2 are not null
         aNewSE = SqlExpression.createNewTempFunction("SUM", IFunctionID.SUM_ID);
         String newColumnName2 = generateColumnName(aSqlExpression.getExprString());
         aNewSE.setExprString("SUM(" + "CASE WHEN " + expr1 + " IS NULL OR "
                 + expr2 + " IS NULL THEN 0 " + "ELSE (" + expr2 + " ) END)");
         aNewSE.setAlias(newColumnName2);
         aNewSE.setExprDataType(aSqlExpression.getExprDataType());
         newNodeProjList.add(aNewSE);
 
         // SUM(expr1) when expr1 and expr2 are non-null
         aNewSE = SqlExpression.createNewTempFunction("SUM", IFunctionID.SUM_ID);
         String newColumnName3 = generateColumnName(aSqlExpression.getExprString());
         aNewSE.setExprString("SUM(" + "CASE WHEN " + expr1 + " IS NULL OR "
                 + expr2 + " IS NULL THEN 0 " + "ELSE (" + expr1 + " ) END)");
         aNewSE.setAlias(newColumnName3);
         aNewSE.setExprDataType(aSqlExpression.getExprDataType());
         newNodeProjList.add(aNewSE);
 
         // Count non-null pairs of (expr1, expr2)
         aNewSE = SqlExpression.createNewTempFunction("SUM", IFunctionID.SUM_ID);
         String newColumnName4 = generateColumnName(aSqlExpression.getExprString());
         aNewSE.setExprString("SUM(" + "CASE WHEN " + expr1 + " IS NULL OR "
                 + expr2 + " IS NULL THEN 0 " + "ELSE 1 END)");
         aNewSE.setAlias(newColumnName4);
         aNewSE.setExprDataType(aSqlExpression.getExprDataType());
         newNodeProjList.add(aNewSE);
 
         // SUM(expr2 * expr2)
         String newColumnName5 = generateColumnName(aSqlExpression.getExprString());
         aNewSE = SqlExpression.createNewTempFunction("SUM", IFunctionID.SUM_ID);
         aNewSE.setExprString("SUM(" + "CASE WHEN " + expr1 + " IS NULL OR "
                 + expr2 + " IS NULL THEN 0 " + "ELSE (" + expr2 + " * " + expr2
                 + " ) END)");
         aNewSE.setAlias(newColumnName5);
         aNewSE.setExprDataType(aSqlExpression.getExprDataType());
         newNodeProjList.add(aNewSE);
 
         /*
          * Step - 2
          * Put the results of previous step in the following formula
          *      SUM(xcol1) - SUM(xcol2) * SUM(xcol3)
          *                   -------------------------
          *                             SUM(xcol4)
          *      ------------------------------------------------
          *                          SUM(xcol4)
          * ----------------------------------------------------------
          *                      SUM(xcol5) - SUM(xcol2)^2
          *                                   ------------
          *                                    SUM(xcol4)
          *                        ---------------------------
          *                                  SUM(xcol4)
          */
 
         SqlExpression aNewSE_SumXCOL1 = SqlExpression.createNewTempFunction(
                 "SUM", IFunctionID.SUM_ID);
         aNewSE_SumXCOL1.setExprString("SUM("
                 + IdentifierHandler.quote(newColumnName1) + ")");
 
         SqlExpression aNewSE_SumXCOL2 = SqlExpression.createNewTempFunction(
                 "SUM", IFunctionID.SUM_ID);
         aNewSE_SumXCOL2.setExprString("SUM("
                 + IdentifierHandler.quote(newColumnName2) + ")");
 
         SqlExpression aNewSE_SumXCOL3 = SqlExpression.createNewTempFunction(
                 "SUM", IFunctionID.SUM_ID);
         aNewSE_SumXCOL3.setExprString("SUM("
                 + IdentifierHandler.quote(newColumnName3) + ")");
 
         /*
          * Special handling for the case when there is no
          * non-null (expr1, expr2) pairs.
          * In that case we return the count as NULL instead of 0 eliminating the
          * possibility of a divide by zero error in the subsequent step
          */
         SqlExpression aNewSE_SumXCOL4 = SqlExpression.createNewTempFunction(
                 "CASE", IFunctionID.CASE_ID);
         aNewSE_SumXCOL4.setExprString("(CASE WHEN SUM("
                 + IdentifierHandler.quote(newColumnName4)
                 + ") = 0 THEN NULL ELSE SUM("
                 + IdentifierHandler.quote(newColumnName4) + ") END)");
 
         // Compute SUM(expr2) * SUM(expr1)
         SqlExpression tempSqlExpression = SqlExpression.createNewTempOpExpression(
                 "*", aNewSE_SumXCOL2, aNewSE_SumXCOL3);
         tempSqlExpression.setExprString("(" + aNewSE_SumXCOL2.getExprString() + " * "
                 + aNewSE_SumXCOL3.getExprString() + ")");
 
         // Compute tempSqlExpression/(SUM(xcol4))
         SqlExpression tempSqlExpression2 = SqlExpression.createNewTempOpExpression(
                 "/", tempSqlExpression, aNewSE_SumXCOL4);
         tempSqlExpression2.setExprString("(" + tempSqlExpression.getExprString()
                 + " / " + aNewSE_SumXCOL4.getExprString() + ")");
 
         // Compute SUM(expr1 * expr2) - tempSqlExpression2
         SqlExpression tempSqlExpression3_nr = SqlExpression.createNewTempOpExpression(
                 "-", aNewSE_SumXCOL3, tempSqlExpression2);
         tempSqlExpression3_nr.setExprString("(" + aNewSE_SumXCOL1.getExprString()
                 + " - (" + tempSqlExpression2.getExprString() + "))");
 
         // Compute tempSqlExpression3_nr/N
         SqlExpression aNewSE_covar_pop = SqlExpression.createNewTempOpExpression(
                 "/", tempSqlExpression3_nr, aNewSE_SumXCOL4);
         aNewSE_covar_pop.setExprString("(" + tempSqlExpression3_nr.getExprString()
                 + ") / " + aNewSE_SumXCOL4.getExprString());
 
         SqlExpression aNewSE_SumXCOL5 = SqlExpression.createNewTempFunction(
                 "SUM", IFunctionID.SUM_ID);
         aNewSE_SumXCOL5.setExprString("SUM("
                 + IdentifierHandler.quote(newColumnName5) + ")");
 
         SqlExpression aNewSE_Power = SqlExpression.createNewTempFunction(
                 "POWER", IFunctionID.CUSTOM_ID);
         aNewSE_Power.setExprString("POWER(SUM("
                 + IdentifierHandler.quote(newColumnName2) + "), 2)");
 
         aNewSE = SqlExpression.createNewTempOpExpression("/", aNewSE_Power,
                 aNewSE_SumXCOL4);
         aNewSE.setExprString("(" + aNewSE_Power.getExprString() + " / "
                 + aNewSE_SumXCOL4.getExprString() + ")");
 
         SqlExpression aNewSE_Nr = SqlExpression.createNewTempOpExpression("-",
                 aNewSE_SumXCOL5, aNewSE);
         aNewSE_Nr.setExprString("(" + aNewSE_SumXCOL5.getExprString() + " - "
                 + aNewSE.getExprString() + ")");
 
         SqlExpression aNewSE_var_pop = SqlExpression.createNewTempOpExpression(
                 "/", aNewSE_Nr, aNewSE_SumXCOL4);
         aNewSE_var_pop.setExprString("(" + aNewSE_Nr.getExprString() + " / "
                 + aNewSE_SumXCOL4.getExprString() + ")");
 
         newSqlExpression.setExprType(SqlExpression.SQLEX_OPERATOR_EXPRESSION);
         newSqlExpression.setOperator("/");
         newSqlExpression.setLeftExpr(aNewSE_covar_pop);
         newSqlExpression.setRightExpr(aNewSE_var_pop);
 
         newSqlExpression.setExprString("(" + aNewSE_covar_pop.getExprString() + "/"
                 + "(CASE WHEN SUM("
                 + IdentifierHandler.quote(newColumnName5)
                 + ") = " + "((SUM("
                 + IdentifierHandler.quote(newColumnName2)
                 + ") * SUM("
                 + IdentifierHandler.quote(newColumnName2)
                 + "))/(CASE WHEN SUM("
                 + IdentifierHandler.quote(newColumnName4)
                 + ") = 0 THEN NULL "
                 + "ELSE SUM("
                 + IdentifierHandler.quote(newColumnName4)
                 + ") END)) THEN NULL ELSE "
                 + aNewSE_var_pop.getExprString() + " END ))");
         newSqlExpression.setTempExpr(true);
 
         colMappings.put(aSqlExpression, newSqlExpression.getExprString());
     }
 
     /**
      *
      * Computes the average of the given aSqlExpression
      * aSqlExpression
      * newNodeProjList
      * newSqlExpression
      *
      * @return
      */
     private void makeAvgExpression(SqlExpression aSqlExpression,
             List<SqlExpression> newNodeProjList, SqlExpression newSqlExpression) {
         // We need to do
         // sum(sum(x)) / sum(count(x))
         SqlExpression aNewSE = aSqlExpression.copy();
 
         String newColumnName = generateColumnName(aSqlExpression.getExprString());
 
         aNewSE.setFunctionId(IFunctionID.SUM_ID);
         aNewSE.setFunctionName("SUM");
 
         aNewSE.setExprString(ParseCmdLine.replace(aNewSE.getExprString(), "AVG", "SUM"));
         aNewSE.setAlias(newColumnName);
         aNewSE.setTempExpr(true);
 
         newNodeProjList.add(aNewSE);
 
         aNewSE = aSqlExpression.copy();
 
         String newColumnName2 = generateColumnName(aNewSE.getExprString());
 
         aNewSE.setFunctionId(IFunctionID.COUNT_ID);
         aNewSE.setFunctionName("COUNT");
         aNewSE.setExprString(ParseCmdLine.replace(aNewSE.getExprString(), "AVG", "COUNT"));
         aNewSE.setAlias(newColumnName2);
         aNewSE.setTempExpr(true);
 
         newNodeProjList.add(aNewSE);
 
         // -----------------------------------------------------
         // Final agg step
 
         newSqlExpression.setExprType(SqlExpression.SQLEX_OPERATOR_EXPRESSION);
 
         newSqlExpression.setOperator("/");
 
         newSqlExpression.setExprString("(SUM("
                 + IdentifierHandler.quote(newColumnName)
                 + ") / SUM("
                 + IdentifierHandler.quote(newColumnName2) + "))");
 
         aNewSE = SqlExpression.createNewTempFunction("SUM", IFunctionID.SUM_ID);
         aNewSE.setExprString("SUM(" + IdentifierHandler.quote(newColumnName) + ")");
 
         newSqlExpression.setLeftExpr(aNewSE);
 
         aNewSE = SqlExpression.createNewTempFunction("SUM", IFunctionID.SUM_ID);
         aNewSE.setExprString("SUM(" + IdentifierHandler.quote(newColumnName2) + ")");
 
         newSqlExpression.setRightExpr(aNewSE);
         newSqlExpression.setTempExpr(true);
     }
 
     /**
      *
      * Computes the average of the given aSqlExpression
      *
      * @param   aSqlExpression
      * @param   newNodeProjList
      * @param   newSqlExpression    [out]   Output expression
      * @param   expr1               [in]    Input expression1
      * @param   expr2               [in]    Input expression1
      *
      * Overloaded function for avg that takes 2 parameters. This will be used
      * when avg is used in some other function involving 2 parameters. In these
      * cases we have to filter out rows that have either of expr1 or expr2 as null
      * e.g. regr_intercept
      *
      * @return
      */
     private void makeAvgExpression(SqlExpression aSqlExpression,
             List<SqlExpression> newNodeProjList, SqlExpression newSqlExpression,
             String expr1, String expr2) {
         /**
          *
          * We need to do
          * sum(sum(expr1)) / sum(count(expr1))
          * for only the non-null pairs of (expr1, expr2)
          *
          **/
 
         // Sum of expr1 for non-null (expr1, expr2) pairs on each site
         String newColumnName = generateColumnName(aSqlExpression.getExprString());
         SqlExpression aNewSE = SqlExpression.createNewTempFunction("SUM",
                 IFunctionID.SUM_ID);
         aNewSE.setExprString("SUM(" + "CASE WHEN " + expr1 + " IS NULL OR "
                 + expr2 + " IS NULL THEN 0 " + "ELSE (" + expr1 + " ) END)");
         aNewSE.setAlias(newColumnName);
         aNewSE.setExprDataType(aSqlExpression.getExprDataType());
         newNodeProjList.add(aNewSE);
 
         // Count of non-null (expr1, expr2) pairs on each site
         aNewSE = SqlExpression.createNewTempFunction("SUM", IFunctionID.SUM_ID);
         String newColumnName2 = generateColumnName(aNewSE.getExprString());
         aNewSE.setExprString("SUM(" + "CASE WHEN " + expr1 + " IS NULL OR "
                 + expr2 + " IS NULL THEN 0 " + "ELSE 1 END)");
         aNewSE.setAlias(newColumnName2);
         aNewSE.setExprDataType(aSqlExpression.getExprDataType());
         newNodeProjList.add(aNewSE);
 
         // -----------------------------------------------------
         // Final agg step
         SqlExpression aNewSE_nr = SqlExpression.createNewTempFunction("SUM",
                 IFunctionID.SUM_ID);
         aNewSE_nr.setExprString("SUM("
                 + IdentifierHandler.quote(newColumnName) + ")");
 
         SqlExpression aNewSE_dr = SqlExpression.createNewTempFunction("CASE",
                 IFunctionID.CASE_ID);
         aNewSE_dr.setExprString("CASE WHEN SUM("
                 + IdentifierHandler.quote(newColumnName2)
                 + ") = 0 THEN NULL " + "ELSE SUM("
                 + IdentifierHandler.quote(newColumnName2) + ") END");
 
         newSqlExpression.setExprType(SqlExpression.SQLEX_OPERATOR_EXPRESSION);
         newSqlExpression.setOperator("/");
         newSqlExpression.setLeftExpr(aNewSE_nr);
         newSqlExpression.setRightExpr(aNewSE_dr);
         newSqlExpression.setExprString("(" + aNewSE_nr.getExprString() + "/"
                 + aNewSE_dr.getExprString() + ")");
         newSqlExpression.setTempExpr(true);
 
         colMappings.put(aSqlExpression, newSqlExpression.getExprString());
     }
 
     /**
      * Creates an expression that has the steps to compute regr_intercept
      * of the given expressions
      *
      * @param   aSqlExpression
      * @param   newNodeProjList
      * @param   newSqlExpression    [out]   Output expression
      * @param   expr1               [in]    Input expression1
      * @param   expr2               [in]    Input expression1
      *
      * @return
      */
     private void makeRegrInterceptExpression(SqlExpression aSqlExpression,
             List<SqlExpression> newNodeProjList, SqlExpression newSqlExpression,
             String expr1, String expr2) {
 
         /*
          * regr_intercept(expr1, expr2) =
          *          AVG(expr1) - REGR_SLOPE(expr1, expr2) * AVG(expr2)
          * after elimination on null (expr1, expr2) pairs
          */
 
         SqlExpression aNewSE_avg_e1 = new SqlExpression();
         makeAvgExpression(aSqlExpression, newNodeProjList, aNewSE_avg_e1,
                 expr1, expr2);
 
         SqlExpression aNewSE_avg_e2 = new SqlExpression();
         makeAvgExpression(aSqlExpression, newNodeProjList, aNewSE_avg_e2,
                 expr2, expr1);
 
         SqlExpression aNewSE_regrslope = new SqlExpression();
         makeRegrSlopeExpression(aSqlExpression, newNodeProjList,
                 aNewSE_regrslope, expr1, expr2);
 
         SqlExpression aNewSE_prod = SqlExpression.createNewTempOpExpression(
                 "*", aNewSE_regrslope, aNewSE_avg_e2);
         aNewSE_prod.setExprString(aNewSE_regrslope.getExprString() + " * "
                 + aNewSE_avg_e2.getExprString());
 
         newSqlExpression.setExprType(SqlExpression.SQLEX_OPERATOR_EXPRESSION);
         newSqlExpression.setOperator("-");
         newSqlExpression.setLeftExpr(aNewSE_avg_e1);
         newSqlExpression.setRightExpr(aNewSE_prod);
         newSqlExpression.setExprString("(" + aNewSE_avg_e1.getExprString() + "-" +
                 aNewSE_prod.getExprString() + ")");
         newSqlExpression.setTempExpr(true);
 
         colMappings.put(aSqlExpression, newSqlExpression.getExprString());
     }
 
     /**
      * Creates an expression that has the steps to compute var_samp
      * of the given expression
      *
      * @param   aSqlExpression
      * @param   newNodeProjList
      * @param   newSqlExpression    [out]   Output expression
      * @param   expr                [in]    Input expression
      *
      * @return
      */
     private void makeVarSampExpression(SqlExpression aSqlExpression,
             List<SqlExpression> newNodeProjList, SqlExpression newSqlExpression, String expr) {
         // Do not worry about the distinct case.
 
         /**
          * We need to do the following:
          * VAR_SAMP(expr) =
          * SUM(expr ^ 2) - (SUM(expr)) ^ 2
          *                  ----------------
          *                         N
          *------------------------------------------------
          *                  N - 1
          * Where N is the number of (expr1, expr2) pairs where neither
          *  expr1 nor expr2 is null.
          **/
 
         /**
          * We need to do the following:
          * Step 1: Calculate the following
          *      a: SUM(expr^2) for non null expr values
          *      b: SUM(expr)   for non null expr values
          *      c: COUNT(expr) for non null expr values
          **/
 
         // SUM(expr^2)
         SqlExpression aNewSE = SqlExpression.createNewTempFunction("SUM",
                 IFunctionID.SUM_ID);
         aNewSE.setExprString("SUM(" + "CASE WHEN " + expr + " IS NULL THEN 0 "
                 + "ELSE (" + expr + " * " + expr + ") END)");
         String newColumnName1 = generateColumnName(aSqlExpression.getExprString());
         aNewSE.setAlias(newColumnName1);
         aNewSE.setExprDataType(aSqlExpression.getExprDataType());
         newNodeProjList.add(aNewSE);
 
         // SUM(expr)
         aNewSE = SqlExpression.createNewTempFunction("SUM", IFunctionID.SUM_ID);
         aNewSE.setExprString("SUM(" + "CASE WHEN " + expr + " IS NULL THEN 0 "
                 + "ELSE " + expr + " END)");
         String newColumnName2 = generateColumnName(aSqlExpression.getExprString());
         aNewSE.setAlias(newColumnName2);
         aNewSE.setExprDataType(aSqlExpression.getExprDataType());
         newNodeProjList.add(aNewSE);
 
         // COUNT(expr)
         aNewSE = SqlExpression.createNewTempFunction("SUM", IFunctionID.SUM_ID);
         aNewSE.setExprString("SUM(" + "CASE WHEN " + expr + " IS NULL THEN 0 "
                 + "ELSE " + 1 + " END)");
         String newColumnName3 = generateColumnName(aSqlExpression.getExprString());
         aNewSE.setAlias(newColumnName3);
         aNewSE.setExprDataType(aSqlExpression.getExprDataType());
         newNodeProjList.add(aNewSE);
 
         /**
          * -----------------------------------------------------
          * Step 2: Put the results of Step 1 in the formula
          * var_samp(expr) =
          *              SUM(xcol1) -  POWER(SUM(xcol2), 2)
          *                            --------------------
          *                                  SUM(xcol3)
          *              -------------------------------------
          *                         SUM(xcol3) - 1
          **/
 
         newSqlExpression.setExprType(SqlExpression.SQLEX_OPERATOR_EXPRESSION);
 
         SqlExpression aNewSE_SumXCOL1 = SqlExpression.createNewTempFunction(
                 "SUM", IFunctionID.SUM_ID);
         aNewSE_SumXCOL1.setExprString("SUM("
                 + IdentifierHandler.quote(newColumnName1) + ")");
 
         SqlExpression aNewSE_SumXCOL2 = SqlExpression.createNewTempFunction(
                 "SUM", IFunctionID.SUM_ID);
         aNewSE_SumXCOL2.setExprString("SUM("
                 + IdentifierHandler.quote(newColumnName2) + ")");
 
         SqlExpression aNewSE_SumXCOL3 = SqlExpression.createNewTempFunction(
                 "CASE", IFunctionID.CASE_ID);
         aNewSE_SumXCOL3.setExprString("(CASE WHEN SUM("
                 + IdentifierHandler.quote(newColumnName3)
                 + ") = 0 OR (SUM("
                 + IdentifierHandler.quote(newColumnName3)
                 + ") - 1) = 0 THEN NULL ELSE SUM("
                 + IdentifierHandler.quote(newColumnName3) + ") END)");
 
         SqlExpression aNewSE_Power = SqlExpression.createNewTempFunction(
                 "POWER", IFunctionID.CUSTOM_ID);
         aNewSE_Power.setExprString("POWER(SUM("
                 + IdentifierHandler.quote(newColumnName2) + "), 2)");
 
         aNewSE = SqlExpression.createNewTempOpExpression("/", aNewSE_Power,
                 aNewSE_SumXCOL3);
         aNewSE.setExprString("(" + aNewSE_Power.getExprString() + " / "
                 + aNewSE_SumXCOL3.getExprString() + ")");
 
         SqlExpression aNewSE_Nr = SqlExpression.createNewTempOpExpression("-",
                 aNewSE_SumXCOL1, aNewSE);
         aNewSE_Nr.setExprString("(" + aNewSE_SumXCOL1.getExprString() + " - "
                 + aNewSE.getExprString() + ")");
 
         SqlExpression aNewSE_Const1 = new SqlExpression();
         aNewSE_Const1.setExprType(SqlExpression.SQLEX_CONSTANT);
         aNewSE_Const1.setConstantValue("1");
 
         SqlExpression aNewSE_Denr = SqlExpression.createNewTempOpExpression(
                 "-", aNewSE_SumXCOL3, aNewSE_Const1);
         aNewSE_Denr.setExprString("(" + aNewSE_SumXCOL3.getExprString() + " - "
                 + aNewSE_Const1.getConstantValue() + ")");
 
         newSqlExpression.setLeftExpr(aNewSE_Nr);
         newSqlExpression.setRightExpr(aNewSE_Denr);
         newSqlExpression.setExprString("(" + aNewSE_Nr.getExprString() + " / "
                 + aNewSE_Denr.getExprString() + ")");
         newSqlExpression.setTempExpr(true);
 
         colMappings.put(aSqlExpression, newSqlExpression.getExprString());
     }
 
     /**
      * Creates an expression that has the steps to compute variance
      * of the given expression
      *
      * @param   aSqlExpression
      * @param   newNodeProjList
      * @param   newSqlExpression    [out]   Output expression
      * @param   expr                [in]    Input expression
      *
      * @return
      */
     private void makeVarianceExpression(SqlExpression aSqlExpression,
             List<SqlExpression> newNodeProjList, SqlExpression newSqlExpression, String expr) {
 
         /* VARIANCE(expr)
          *              = 0 if the number of rows in expr = 1
          *              = VAR_SAMP(expr) if the number of rows in expr = 1
          */
 
         // Step 1: Count of non-null expr values
         SqlExpression aNewSE = SqlExpression.createNewTempFunction("SUM",
                 IFunctionID.SUM_ID);
         aNewSE.setExprString("SUM(" + "CASE WHEN " + expr + " IS NULL THEN 0 "
                 + "ELSE " + 1 + " END)");
         String newColumnName = generateColumnName(aSqlExpression.getExprString());
         aNewSE.setAlias(newColumnName);
         aNewSE.setExprDataType(aSqlExpression.getExprDataType());
         newNodeProjList.add(aNewSE);
 
         // Step 2: Put the results of Step 1 in the formula
         SqlExpression aNewSE_SumXCOL = SqlExpression.createNewTempFunction(
                 "SUM", IFunctionID.SUM_ID);
         aNewSE_SumXCOL.setExprString("SUM("
                 + IdentifierHandler.quote(newColumnName) + ")");
 
         SqlExpression aNewSE_varsamp = new SqlExpression();
         makeVarSampExpression(aSqlExpression, newNodeProjList, aNewSE_varsamp,
                 expr);
         newSqlExpression.setFunctionId(IFunctionID.CASE_ID);
         newSqlExpression.setFunctionName("CASE");
         newSqlExpression.setExprString("(CASE WHEN " + aNewSE_SumXCOL.getExprString()
                 + " = 1 " + "THEN 0 ELSE " + aNewSE_varsamp.getExprString() + " END)");
         newSqlExpression.setTempExpr(true);
 
         colMappings.put(aSqlExpression, newSqlExpression.getExprString());
     }
 
     /**
      * Creates an expression that has the steps to compute stddev_samp
      * of the given expression
      *
      * @param   aSqlExpression
      * @param   newNodeProjList
      * @param   newSqlExpression    [out]   Output expression
      * @param   expr                [in]    Input expression
      *
      * @return
      */
     private void makeStddevSampExpression(SqlExpression aSqlExpression,
             List<SqlExpression> newNodeProjList, SqlExpression newSqlExpression, String expr) {
         /*
          * stddev_samp(expr) =
          *                  SQRT(var_samp(expr))
          */
         SqlExpression aNewSE_var_samp = new SqlExpression();
         makeVarSampExpression(aSqlExpression, newNodeProjList, aNewSE_var_samp,
                 expr);
         newSqlExpression.setExprString("SQRT(" + aNewSE_var_samp.getExprString()
                 + ")");
         newSqlExpression.getFunctionParams().add(aNewSE_var_samp);
         newSqlExpression.setTempExpr(true);
 
         colMappings.put(aSqlExpression, newSqlExpression.getExprString());
     }
 
     /**
      * Creates an expression that has the steps to compute stddev of the given expression
      * @param   aSqlExpression
      * @param   newNodeProjList
      * @param   newSqlExpression    [out]   Output expression
      * @param   expr                [in]    Input expression
      *
      * @return
      */
     private void makeStddevExpression(SqlExpression aSqlExpression,
             List<SqlExpression> newNodeProjList, SqlExpression newSqlExpression, String expr) {
         // STDEV(expr) = SQRT(Variance)
 
         /* In this case we do not call directly makeVarianceExpression()
          * function and then take underroot of that because in case of N = 1
          * variance returns 0 and underroot of 0 returns 0E-15 because
          * precision of sqrt is different. To avoid this we first check
          * if N = 1 then return 0 else call makeVarianceExpression() function.
          */
 
         // Step 1: Get N; count of non-null expr values
         SqlExpression aNewSE_variance = new SqlExpression();
         SqlExpression aNewSE = SqlExpression.createNewTempFunction("SUM",
                 IFunctionID.SUM_ID);
         aNewSE.setExprString("SUM(" + "CASE WHEN " + expr + " IS NULL THEN 0 "
                 + "ELSE " + 1 + " END)");
         aNewSE.setExprDataType(aSqlExpression.getExprDataType());
 
         String newColumnName = generateColumnName(aSqlExpression.getExprString());
         aNewSE.setAlias(newColumnName);
         newNodeProjList.add(aNewSE);
 
         // Step 2:
         // Combine the count of non-null expr values from all nodes
         SqlExpression aNewSE_SumXCOL = SqlExpression.createNewTempFunction(
                 "SUM", IFunctionID.SUM_ID);
         aNewSE_SumXCOL.setExprString("SUM("
                 + IdentifierHandler.quote(newColumnName) + ")");
 
         // Compute the SQRT(variance). Use the existing makeVarianceExpression
         // for the computation
         makeVarianceExpression(aSqlExpression, newNodeProjList,
                 aNewSE_variance, expr);
         SqlExpression aNewSE_sqrtvariance = SqlExpression.createNewTempFunction(
                 "SQRT", IFunctionID.CUSTOM_ID);
         aNewSE_sqrtvariance.setExprString("SQRT(" + aNewSE_variance.getExprString()
                 + ")");
 
         newSqlExpression.setFunctionId(IFunctionID.CASE_ID);
         newSqlExpression.setFunctionName("CASE");
         newSqlExpression.setExprString("(CASE WHEN SUM("
                 + IdentifierHandler.quote(newColumnName)
                 + ") = 1 " + "THEN 0 ELSE " + aNewSE_sqrtvariance.getExprString()
                 + " END)");
         newSqlExpression.setTempExpr(true);
 
         colMappings.put(aSqlExpression, newSqlExpression.getExprString());
     }
 
     /**
      * Creates an expression that has the steps to compute covar_samp
      * of the given expressions
      *
      * @param   aSqlExpression
      * @param   newNodeProjList
      * @param   newSqlExpression    [out]   Output expression
      * @param   expr1               [in]    Input expression
      * @param   expr2               [in]    Input expression
      *
      * @return
      */
     private void makeCovarSampExpression(SqlExpression aSqlExpression,
             List<SqlExpression> newNodeProjList, SqlExpression newSqlExpression,
             String expr1, String expr2) {
         /**
          * COVAR_SAMP(expr1, expr2) =
          * (SUM(expr1 * expr2) - SUM(expr2) * SUM(expr1)
          *                                     --------
          *                                         N
          *------------------------------------------------
          *                  N - 1
          * Where N is the number of (expr1, expr2) pairs where neither
          *  expr1 nor expr2 is null.
          **/
 
         // Step 1
         // SUM(X*Y)
         String newColumnName = generateColumnName(aSqlExpression.getExprString());
         SqlExpression aNewSE = SqlExpression.createNewTempFunction("SUM",
                 IFunctionID.SUM_ID);
         aNewSE.setExprString("SUM(" + "CASE WHEN " + expr1 + " IS NULL OR "
                 + expr2 + " IS NULL THEN 0 " + "ELSE (" + expr1 + " * " + expr2
                 + " ) END)");
         aNewSE.setAlias(newColumnName);
         aNewSE.setExprDataType(aSqlExpression.getExprDataType());
         newNodeProjList.add(aNewSE);
 
         // SUM(Y)
         aNewSE = SqlExpression.createNewTempFunction("SUM", IFunctionID.SUM_ID);
         String newColumnName2 = generateColumnName(aSqlExpression.getExprString());
         aNewSE.setExprString("SUM(" + "CASE WHEN " + expr1 + " IS NULL OR "
                 + expr2 + " IS NULL THEN 0 " + "ELSE (" + expr2 + " ) END)");
         aNewSE.setAlias(newColumnName2);
         aNewSE.setExprDataType(aSqlExpression.getExprDataType());
         newNodeProjList.add(aNewSE);
 
         // SUM(X)
         aNewSE = SqlExpression.createNewTempFunction("SUM", IFunctionID.SUM_ID);
         String newColumnName3 = generateColumnName(aSqlExpression.getExprString());
         aNewSE.setExprString("SUM(" + "CASE WHEN " + expr1 + " IS NULL OR "
                 + expr2 + " IS NULL THEN 0 " + "ELSE (" + expr1 + " ) END)");
         aNewSE.setAlias(newColumnName3);
         aNewSE.setExprDataType(aSqlExpression.getExprDataType());
         newNodeProjList.add(aNewSE);
 
         // SUM(n)
         aNewSE = SqlExpression.createNewTempFunction("SUM", IFunctionID.SUM_ID);
         String newColumnName4 = generateColumnName(aSqlExpression.getExprString());
         aNewSE.setExprString("SUM(" + "CASE WHEN " + expr1 + " IS NULL OR "
                 + expr2 + " IS NULL THEN 0 " + "ELSE 1 END)");
         aNewSE.setAlias(newColumnName4);
         aNewSE.setExprDataType(aSqlExpression.getExprDataType());
         newNodeProjList.add(aNewSE);
 
         // Step - 2 Final agg step
         SqlExpression aNewSE_SumXCOL1 = SqlExpression.createNewTempFunction(
                 "SUM", IFunctionID.SUM_ID);
         aNewSE_SumXCOL1.setExprString("SUM("
                 + IdentifierHandler.quote(newColumnName) + ")");
 
         SqlExpression aNewSE_SumXCOL2 = SqlExpression.createNewTempFunction(
                 "SUM", IFunctionID.SUM_ID);
         aNewSE_SumXCOL2.setExprString("SUM("
                 + IdentifierHandler.quote(newColumnName2) + ")");
 
         SqlExpression aNewSE_SumXCOL3 = SqlExpression.createNewTempFunction(
                 "SUM", IFunctionID.SUM_ID);
         aNewSE_SumXCOL3.setExprString("SUM("
                 + IdentifierHandler.quote(newColumnName3) + ")");
 
         SqlExpression aNewSE_SumXCOL4 = SqlExpression.createNewTempFunction(
                 "CASE", IFunctionID.CASE_ID);
         aNewSE_SumXCOL4.setExprString("(CASE WHEN SUM("
                 + IdentifierHandler.quote(newColumnName4)
                 + ") = 0 OR (SUM("
                 + IdentifierHandler.quote(newColumnName4)
                 + ") - 1) = 0 THEN NULL ELSE SUM("
                 + IdentifierHandler.quote(newColumnName4) + ") END)");
 
         // Get [(sum(y) * sum(x)]
         SqlExpression tempSqlExpression = SqlExpression.createNewTempOpExpression(
                 "*", aNewSE_SumXCOL2, aNewSE_SumXCOL3);
         tempSqlExpression.setExprString(aNewSE_SumXCOL2.getExprString() + " * "
                 + aNewSE_SumXCOL3.getExprString());
 
         // Get tempSqlExpression/N
         SqlExpression tempSqlExpression2 = SqlExpression.createNewTempOpExpression(
                 "/", tempSqlExpression, aNewSE_SumXCOL4);
         tempSqlExpression2.setExprString("(" + tempSqlExpression.getExprString()
                 + " / " + aNewSE_SumXCOL4.getExprString() + ")");
 
         // Get SUM(x*y) - tempSqlExpression2
         SqlExpression tempSqlExpression3_nr = SqlExpression.createNewTempOpExpression(
                 "-", aNewSE_SumXCOL1, tempSqlExpression2);
         tempSqlExpression3_nr.setExprString("(" + aNewSE_SumXCOL1.getExprString()
                 + " - " + tempSqlExpression2.getExprString() + ")");
 
         // Get tempSqlExpression3_nr/N - 1
         SqlExpression aNewSE_Denr = new SqlExpression();
         aNewSE_Denr.setExprType(SqlExpression.SQLEX_OPERATOR_EXPRESSION);
         aNewSE_Denr.setOperator("-");
         aNewSE_Denr.setExprString("(" + aNewSE_SumXCOL4.getExprString() + " - 1)");
         aNewSE_Denr.setTempExpr(true);
 
         newSqlExpression.setExprType(SqlExpression.SQLEX_OPERATOR_EXPRESSION);
         newSqlExpression.setOperator("/");
         newSqlExpression.setLeftExpr(tempSqlExpression3_nr);
         newSqlExpression.setRightExpr(aNewSE_Denr);
         newSqlExpression.setExprString("(" + tempSqlExpression3_nr.getExprString()
                 + " / " + aNewSE_Denr.getExprString() + ")");
         newSqlExpression.setTempExpr(true);
 
         colMappings.put(aSqlExpression, newSqlExpression.getExprString());
     }
 
     /**
      * Creates an expression that has the steps to compute regr_avgx
      * of the given expression
      *
      * @param   aSqlExpression
      * @param   newNodeProjList
      * @param   newSqlExpression    [out]   Output expression
      * @param   expr1               [in]    Input expression1
      * @param   expr2               [in]    Input expression2
      *
      * @return
      */
     private void makeRegrAvgxExpression(SqlExpression aSqlExpression,
             List<SqlExpression> newNodeProjList, SqlExpression newSqlExpression,
             String expr1, String expr2) {
         /**
          *
          * Note we do not care if it is DISTINCT, the result will
          * be the same. So, there is no need to defer when
          * distinct is present
          *
          * We need to do:
          * regr_count(expr1, expr2) =
          *              AVG(expr2) of non-null pairs of (expr1, expr2)
          *
          * Step 1:
          *      a: SUM(epxr2) for non-null pairs of expr1, expr2
          *      b: Calculate the count of non-null pairs of expr1, expr2
          *
          **/
         String newColumnName = generateColumnName(aSqlExpression.getExprString());
 
         SqlExpression aNewSE = SqlExpression.createNewTempFunction("SUM",
                 IFunctionID.SUM_ID);
         aNewSE.setExprString("SUM(" + "CASE WHEN " + expr1 + " IS NULL OR "
                 + expr2 + " IS NULL THEN 0 " + "ELSE " + expr2 + " END)");
         aNewSE.setAlias(newColumnName);
         aNewSE.setExprDataType(aSqlExpression.getExprDataType());
 
         newNodeProjList.add(aNewSE);
 
         String newColumnName2 = generateColumnName(aSqlExpression.getExprString());
 
         aNewSE = SqlExpression.createNewTempFunction("SUM", IFunctionID.SUM_ID);
         aNewSE.setExprString("SUM(" + "CASE WHEN " + expr1 + " IS NULL OR "
                 + expr2 + " IS NULL THEN 0 " + "ELSE 1 END)");
         aNewSE.setAlias(newColumnName2);
         aNewSE.setExprDataType(aSqlExpression.getExprDataType());
 
         newNodeProjList.add(aNewSE);
         // -----------------------------------------------------
         // Step 2: Compute the average
         //          SUM(xcol1) / SUM(xcol2)
 
         SqlExpression aNewSE_nr = SqlExpression.createNewTempFunction("SUM",
                 IFunctionID.SUM_ID);
         aNewSE_nr.setExprString("SUM("
                     + IdentifierHandler.quote(newColumnName) + ")");
 
         SqlExpression aNewSE_dr = SqlExpression.createNewTempFunction("CASE",
                 IFunctionID.CASE_ID);
         aNewSE_dr.setExprString("CASE WHEN SUM("
                 + IdentifierHandler.quote(newColumnName2)
                 + ") = 0 THEN NULL " + "ELSE SUM("
                 + IdentifierHandler.quote(newColumnName2) + ") END");
 
         newSqlExpression.setExprType(SqlExpression.SQLEX_OPERATOR_EXPRESSION);
         newSqlExpression.setOperator("/");
         newSqlExpression.setLeftExpr(aNewSE_nr);
         newSqlExpression.setRightExpr(aNewSE_dr);
         newSqlExpression.setExprString("(" + aNewSE_nr.getExprString() + "/"
                 + aNewSE_dr.getExprString() + ")");
         newSqlExpression.setTempExpr(true);
 
         colMappings.put(aSqlExpression, newSqlExpression.getExprString());
     }
 
     /**
      * Return the the last Leaf (step) in this plan
      *
      * @return the last Leaf (step) in this plan
      */
     public Leaf getLastLeaf () {
         if (leaves == null) {
             return null;
         }
         return leaves.get(leaves.size()-1);
     }
 
 
     /**
      * Return the the first Leaf (step) in this plan
      *
      * @return the first Leaf (step) in this plan
      */
     public Leaf getFirstLeaf () {
         if (leaves == null) {
             return null;
         }
         return leaves.get(0);
     }
 
     /**
      * Return whether or not this is for a DML statement
      * where we have already created the temporary destination.
      *
      * @return whether or not the temporary desintation for
      * the DML statement was already created
      */
     public boolean isExistingInto() {
         return isExistingInto;
     }
 }
