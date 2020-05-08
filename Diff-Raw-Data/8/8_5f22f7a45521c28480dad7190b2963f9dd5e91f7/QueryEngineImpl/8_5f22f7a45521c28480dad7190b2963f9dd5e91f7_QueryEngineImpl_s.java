 /*
  * Copyright 2013 the original author or authors.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package com.evinceframework.data.warehouse.query.impl;
 
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.hibernate.dialect.Dialect;
 import org.hibernate.dialect.pagination.LimitHandler;
 import org.hibernate.engine.spi.RowSelection;
 import org.hibernate.internal.util.StringHelper;
 import org.hibernate.sql.JoinFragment;
 import org.hibernate.sql.JoinType;
 import org.hibernate.sql.Select;
 import org.hibernate.sql.SelectFragment;
 import org.springframework.context.support.MessageSourceAccessor;
 import org.springframework.dao.DataAccessException;
 import org.springframework.jdbc.core.JdbcTemplate;
 import org.springframework.jdbc.core.PreparedStatementCreator;
 import org.springframework.jdbc.core.ResultSetExtractor;
 
 import com.evinceframework.data.warehouse.Dimension;
 import com.evinceframework.data.warehouse.DimensionalAttribute;
 import com.evinceframework.data.warehouse.FactTable;
 import com.evinceframework.data.warehouse.query.DimensionCriterion;
 import com.evinceframework.data.warehouse.query.FactSelection;
 import com.evinceframework.data.warehouse.query.Query;
 import com.evinceframework.data.warehouse.query.QueryException;
 import com.evinceframework.data.warehouse.query.QueryResult;
 import com.evinceframework.data.warehouse.query.SummarizationAttribute;
 
 public class QueryEngineImpl {
 
 	public static final int DEFAULT_ROW_LIMIT = 10000;
 	
 	private static final Log logger = LogFactory.getLog(QueryEngineImpl.class);
 	
 	private Dialect dialect;
 	
 	private JdbcTemplate jdbcTemplate;
 	
 	private MessageSourceAccessor messageSourceAccessor = new MessageSourceAccessor(new QueryEngineMessageSource());
 	
 	private Integer rowLimit = null;
 	
 	public QueryEngineImpl(Dialect dialect) {
 		this(dialect, DEFAULT_ROW_LIMIT);
 	}
 	
 	public QueryEngineImpl(Dialect dialect, Integer rowLimit) {
 		this.dialect = dialect;
 		this.rowLimit = rowLimit;
 	}
 
 	public Dialect getDialect() {
 		return dialect;
 	}
 	
 	public QueryResult execute(final Query query) throws QueryException {
 		
 		final QueryResultImpl result = new QueryResultImpl(query);
 		
 		PreparedStatementCreator psc = new PreparedStatementCreator() {
 			@Override
 			public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
 				
 				SqlGenerationResult sqlResult = null;
 				
 				try {
 					sqlResult = generateSql(query, result);
 					
 				} catch (QueryException e) {
 					throw new SQLException(e);
 				}
 				
 				PreparedStatement stmt = con.prepareStatement(sqlResult.sql);
 				
 				int paramIdx = 0;
 				
 				if(sqlResult.limitHandler != null)
					paramIdx =+ sqlResult.limitHandler.bindLimitParametersAtStartOfQuery(stmt, paramIdx);
 				
 				// set parameters
 				for(DimensionCriterion dc : query.getDimensionCriterion()) {
					paramIdx =+ dc.setParameterValue(stmt, paramIdx);
 				}
 				
 				if(sqlResult.limitHandler != null)
					paramIdx =+ sqlResult.limitHandler.bindLimitParametersAtEndOfQuery(stmt, paramIdx);
 				
 				return stmt;
 			}
 		};
 		
 		ResultSetExtractor<QueryResult> rse = new ResultSetExtractor<QueryResult>() {
 			@Override
 			public QueryResult extractData(ResultSet rs) throws SQLException, DataAccessException {
 				
 				
 				return result;
 			}
 		};
 		
 		return (QueryResult) jdbcTemplate.query(psc, rse);
 	}
 	
 	protected SqlGenerationResult generateSql(Query query, QueryResultImpl result) throws QueryException {
 		
 		/*
 		 A pivot table usually consists of row, column and data (or fact) fields. In this case, the column is 
 		 Ship Date, the row is Region and the datum we would like to see is (sum of) Units. These fields allow 
 		 several kinds of aggregations including: sum, average, standard deviation, count etc. In this case, the 
 		 total number of units shipped is displayed here using a sum aggregation.
 		 */
 		
 		if(query.getFactSelections() == null || query.getFactSelections().length == 0)
 			throw new QueryException(messageSourceAccessor.getMessage(
 					QueryEngineMessageSource.MISSING_FACT_SELECTION, query.getLocale()));
 		
 		SqlGenerationResult sqlResult = new SqlGenerationResult();
 		
 		FactTable fact = query.getFactTable();
 		String factTableAlias = "fact";
 		
 		Select select = new Select(getDialect());
 		select.setFromClause(fact.getTableName(), factTableAlias);
 		SelectFragment selectFrag = new SelectFragment();
 		
 		JoinFragment joinFrag = getDialect().createOuterJoinFragment();
 		List<String> groupBy = new LinkedList<String>();
 		List<String> where = new LinkedList<String>();
 		
 		DimensionJoinAliasLookup dimensionJoinLookup = new DimensionJoinAliasLookup();
 		
 		// Summarization columns
 		SummarizationAttribute[] summaryAttributes = query.getSummarizations();
 		if(summaryAttributes != null && summaryAttributes.length > 0) {
 			
 			for(SummarizationAttribute attr : summaryAttributes) {
 				
 				String alias = joinDimension(dimensionJoinLookup, attr.getDimension(), joinFrag);
 				
 				for(DimensionalAttribute<? extends Object> dimAttr : attr.getDimension().getTable().getBusinessKey()) {
 					selectFrag.addColumn(alias, dimAttr.getColumnName());
 					groupBy.add(StringHelper.qualify(alias, dimAttr.getColumnName()));
 				}
 				
 				// TODO support grouping by a dimensional attribute and not just a dimension
 			}
 		}
 		
 		// Datum columns
 		for(FactSelection fs : query.getFactSelections()) {
 			
 			if(fs.getFunction() == null) {
 				selectFrag.addColumn(factTableAlias, fs.getFact().getColumnName());
 				
 			} else {
 				String qualifiedName = StringHelper.qualify(factTableAlias, fs.getFact().getColumnName());
 				String formula = String.format("%s(%s)", fs.getFunction().getSyntax(), qualifiedName);
 				String formulaAlias = String.format("%s_%s", fs.getFunction().getSyntax(), fs.getFact().getColumnName());
 				
 				selectFrag.addFormula(factTableAlias, formula, formulaAlias);
 			}
 		}
 		
 		// Dimension filtering/slicing
 		for(DimensionCriterion dc : query.getDimensionCriterion()) {
 			String dimensionTableAlias = null;
 			if(dc.requiresJoinOnDimensionTable()) {
 				dimensionTableAlias = joinDimension(dimensionJoinLookup, dc.getDimension(), joinFrag);
 			}
 			where.add(dc.createWhereFragment(factTableAlias, dimensionTableAlias));
 		}
 		
 		// TODO Fact filtering
 		
 		
 		// Update select
 		select.setSelectClause(selectFrag);
 		select.setOuterJoins(joinFrag.toFromFragmentString(), joinFrag.toWhereFragmentString());
 		
 		if (groupBy.size() > 0)
 			select.setGroupByClause(StringHelper.join(",", groupBy.toArray(new String[]{})));
 		
 		if(where.size() > 0)
 			select.setWhereClause(StringHelper.join(" AND ", where.toArray(new String[]{})));
 		
 		sqlResult.sql = select.toStatementString();
 		
 		if(query.getMaximumRowCount() != null || this.rowLimit != null) {
 			
 			Integer limit = query.getMaximumRowCount();
 			
 			if(limit == null) {
 				limit = this.rowLimit;
 				
 			} else if(this.rowLimit != null && limit > this.rowLimit){
 				result.getMessages().add(messageSourceAccessor.getMessage(
 						QueryEngineMessageSource.ROW_LIMIT_EXCEEDED, new Object[] { this.rowLimit }, query.getLocale()));
 				
 				limit = this.rowLimit;
 			}
 			
 			RowSelection rowSelection = new RowSelection();
 			rowSelection.setMaxRows(limit);
 			
 			sqlResult.limitHandler = dialect.buildLimitHandler(sqlResult.sql, rowSelection);
 			if(sqlResult.limitHandler.supportsLimit()) {
 				sqlResult.sql = sqlResult.limitHandler.getProcessedSql();
 				
 			} else { 
 				if(this.rowLimit != null) {
 					logger.warn(String.format(
 							"The query engine is configured to limit the number of rows but the underlying database dialect [%s] does not support this.",
 							dialect.toString()));
 				}
 				
 				if(query.getMaximumRowCount() != null) {
 					result.getMessages().add(messageSourceAccessor.getMessage(
 							QueryEngineMessageSource.ROW_LIMIT_NOT_SUPPORTED, query.getLocale()));
 				}
 			}
 		}
 
 		return sqlResult;
 	}
 	
 	protected String joinDimension(DimensionJoinAliasLookup lookup, Dimension dimension, JoinFragment join) {
 		
 		String alias = lookup.byDimension(dimension);
 		if(alias != null)
 			return alias;
 		
 		int i = 0;
 		do {
 			alias = String.format("dim_%s", i++);
 		} 
 		while(lookup.byAlias(alias) != null);
 		
 		join.addJoin(dimension.getTable().getTableName(), alias, 
 				new String[] { dimension.getForeignKeyColumn() }, 
 				new String[] { dimension.getTable().getPrimaryKeyColumn() }, 
 				JoinType.INNER_JOIN);
 		
 		lookup.register(alias, dimension);
 		
 		return alias;
 	}
 	
 	private class DimensionJoinAliasLookup {
 		
 		private Map<String, Dimension> aliasDimensionMap = new HashMap<String, Dimension>();
 		
 		private Map<Dimension, String> dimensionAliasMap = new HashMap<Dimension, String>();
 		
 		public void register(String alias, Dimension dimension) {
 			aliasDimensionMap.put(alias, dimension);
 			dimensionAliasMap.put(dimension, alias);
 		}
 		
 		public String byDimension(Dimension dimension) {
 			return dimensionAliasMap.get(dimension);
 		}
 		
 		public Dimension byAlias(String alias) {
 			return aliasDimensionMap.get(alias);
 		}
 	}
 	
 	protected class SqlGenerationResult {
 		
 		public String sql;
 		
 		public LimitHandler limitHandler;
 	}
 }
