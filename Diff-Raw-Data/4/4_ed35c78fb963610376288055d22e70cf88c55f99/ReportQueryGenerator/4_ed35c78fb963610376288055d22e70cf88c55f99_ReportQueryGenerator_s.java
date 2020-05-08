 package com.mpower.util;
 
 import java.text.DateFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 
 import org.springframework.context.ApplicationContext;
 
 import com.mpower.domain.ReportCrossTabColumn;
 import com.mpower.domain.ReportCrossTabFields;
 import com.mpower.domain.ReportCrossTabMeasure;
 import com.mpower.domain.ReportCrossTabRow;
 import com.mpower.domain.ReportCustomFilter;
 import com.mpower.domain.ReportCustomFilterDefinition;
 import com.mpower.domain.ReportDatabaseType;
 import com.mpower.domain.ReportField;
 import com.mpower.domain.ReportFieldType;
 import com.mpower.domain.ReportFilter;
 import com.mpower.domain.ReportSelectedField;
 import com.mpower.domain.ReportStandardFilter;
 import com.mpower.domain.ReportWizard;
 import com.mpower.domain.TheGuruView;
 import com.mpower.domain.TheGuruViewJoin;
 import com.mpower.service.ReportCustomFilterDefinitionService;
 import com.mpower.service.ReportFieldService;
 import com.mpower.service.TheGuruViewJoinService;
 import com.mpower.service.TheGuruViewService;
 import com.mpower.service.customfiltercriterialookup.ReportCustomFilterCriteriaLookupService;
 
 /**
  * <p>
  * Object to create queries for either mySql or SQL Server.
  */
 public class ReportQueryGenerator {
 	private ReportWizard reportWizard;
 	private ReportFieldService reportFieldService;
 	private ReportCustomFilterDefinitionService reportCustomFilterDefinitionService;
 	private ApplicationContext applicationContext;
 	private TheGuruViewService theGuruViewService;
 	private TheGuruViewJoinService theGuruViewJoinService;
 
 	private class DynamicReportQuery {
 		public String crlf = System.getProperty("line.separator");
 		public String selectClause = "SELECT DISTINCT";
 		public String fromClause = "";
 		public String joinWhereClause = "WHERE (" + crlf;
 		public String whereClause = "";
 		public String orderByClause = "";
 		
 		public Boolean addWhereAnd = false;
 		
 		public List<String> primaryKeys = new ArrayList<String>();
 		public HashMap<String, String> tableAliases = new HashMap<String, String>();
 		public HashMap<Long, String> fieldTableKeys = new HashMap<Long, String>();
 		public HashMap<Long, String> joinTableKeys = new HashMap<Long, String>();
 		public HashMap<Long, String> viewTableKeys = new HashMap<Long, String>();
 		public HashMap<String, TheGuruViewJoin> allJoins = new HashMap<String, TheGuruViewJoin>();
 		public HashMap<String, TheGuruView> allViews = new HashMap<String, TheGuruView>();
 		public List<String> allJoinTableKeysInOrder = new ArrayList<String>();
 		public List<String> tableKeysFromFields = new ArrayList<String>();
 		public HashMap<String, TheGuruViewJoin> requiredJoins = new HashMap<String, TheGuruViewJoin>(); 
 	}
 	
 	/**
      * Constructor for the <tt>QueryGenerator</tt>.
      * @param reportWizard ReportWizard that contains the various report options.
      * @param reportFieldService ReportFieldService the QueryGenerator will use to retrieve field information from the database.
      */
     public ReportQueryGenerator(ReportWizard reportWizard, ReportFieldService reportFieldService,
     		ReportCustomFilterDefinitionService reportCustomFilterDefinitionService, ApplicationContext applicationContext,
     		TheGuruViewService theGuruViewService, TheGuruViewJoinService theGuruViewJoinService) {
     	this.setReportWizard(reportWizard);
     	this.setReportFieldService(reportFieldService);
     	this.setReportCustomFilterDefinitionService(reportCustomFilterDefinitionService);
     	this.setApplicationContext(applicationContext);
     	this.setTheGuruViewService(theGuruViewService);
     	this.setTheGuruViewJoinService(theGuruViewJoinService);
     }
 
     public enum DatePart {
         YEAR ("YEAR", "yy"),
         QUARTER   ("QUARTER", "qq"),
         MONTH   ("MONTH", "mm"),
         WEEK    ("WEEK", "wk");
 
         private final String mySQL;
         private final String SQL;
         DatePart(String mySQL, String SQL) {
             this.mySQL = mySQL;
             this.SQL = SQL;
         }
         public String mySQL()   { return mySQL; }
         public String SQL() { return SQL; }
 
     }
 
    /*
     public class DatePart{
 
     	   public class SQLConstants {
 
     	    	static final String YEAR = "yy";
     	    	static final String QUARTER = "qq";
     	    	static final String MONTH = "mm";
     	    	static final String WEEK = "wk";
 
     	    }
 
     	    public class mySQLConstants {
 
     	    	static final String YEAR = "YEAR";
     	    	static final String QUARTER = "QUARTER";
     	    	static final String MONTH = "MONTH";
     	    	static final String WEEK = "WEEK";
 
     	    }
     	String YEAR;
   }
 
 
     public class SQLConstants {
 
     	static final String YEAR = "yy";
     	static final String QUARTER = "qq";
     	static final String MONTH = "mm";
     	static final String WEEK = "wk";
 
     }
 
     public class mySQLConstants {
 
     	static final String YEAR = "YEAR";
     	static final String QUARTER = "QUARTER";
     	static final String MONTH = "MONTH";
     	static final String WEEK = "WEEK";
 
     }
 */
 	/**
 	 * Sets the ReportWizard that contains the various report options.
 	 * @param reportWizard
 	 */
 	public void setReportWizard(ReportWizard reportWizard) {
 		this.reportWizard = reportWizard;
 	}
 
 	/**
 	 * Returns the ReportWizard that contains the various report options.
 	 * @return
 	 */
 	public ReportWizard getReportWizard() {
 		return reportWizard;
 	}
 
 	/**
 	 * Sets the ReportFieldService the QueryGenerator uses to retrieve field information.
 	 * @param reportFieldService
 	 */
 	public void setReportFieldService(ReportFieldService reportFieldService) {
 		this.reportFieldService = reportFieldService;
 	}
 
 	/**
 	 * Returns the ReportFieldService the QueryGenerator uses to retrieve field information.
 	 * @return ReportFieldService
 	 */
 	public ReportFieldService getReportFieldService() {
 		return reportFieldService;
 	}
 
 	/**
 	 * Sets the ReportCustomFilterDefinitionService the QueryGenerator uses to retrieve custom filter definition information.
 	 * @param getReportCustomFilterDefinitionService
 	 */
 	public ReportCustomFilterDefinitionService getReportCustomFilterDefinitionService() {
 		return reportCustomFilterDefinitionService;
 	}
 
 	/**
 	 * Returns the ReportCustomFilterDefinitionService the QueryGenerator uses to retrieve custom filter definition information.
 	 * @return ReportFieldService
 	 */
 	public void setReportCustomFilterDefinitionService(
 			ReportCustomFilterDefinitionService reportCustomFilterDefinitionService) {
 		this.reportCustomFilterDefinitionService = reportCustomFilterDefinitionService;
 	}
 
     public ApplicationContext getApplicationContext() {
 		return applicationContext;
 	}
 
 	public void setApplicationContext(ApplicationContext applicationContext) {
 		this.applicationContext = applicationContext;
 	}
 
 	/**
 	 * Builds and returns a mySql or SQL Server query.
 	 * <P>
 	 * {@code} String query = getQueryString();
 	 * @return String
 	 * @throws ParseException
 	 */
 	public String getQueryString() throws ParseException {
 		String query = "";
 		if (getReportWizard().getDataSubSource().getDatabaseType().equals(ReportDatabaseType.MYSQL) && getReportWizard().getUseDynamicSQLGeneration()) {
 			query = buildDynamicMySQLStatment(false);
 		} else {
 			query = buildSelectClause(false);
 			query += buildWhereClause(false);
 			query += buildOrderByClause();
 			if (getReportWizard().getDataSubSource().getDatabaseType() == ReportDatabaseType.MYSQL
 				&& getReportWizard().getRowCount() > 0)
 				query += " LIMIT 0," + getReportWizard().getRowCount();
 			query += ";";
 		}
 		return query;
 	}
 
 	/**
 	 * Builds and returns a mySql or SQL Server query used for report previews (no sorting, only top 100 results).
 	 * <P>
 	 * {@code} String query = getQueryString();
 	 * @return String
 	 * @throws ParseException
 	 */
 	public String getPreviewQueryString() throws ParseException {
 		String query = "";
 		if (getReportWizard().getDataSubSource().getDatabaseType().equals(ReportDatabaseType.MYSQL) && getReportWizard().getUseDynamicSQLGeneration()) {
 			query = buildDynamicMySQLStatment(true);
 		} else {
 			query = buildSelectClause(true);
 			query += buildWhereClause(false);
 			// If queries take too long to run for the preview, the order by clause could be left off of the query
 			// by setting theguru.preview.includeorderbyclause to false.  This would prevent the complete result from 
 			// having to be compiled in order for the top rows to be returned.
 			String includeOrderBy = System.getProperty("theguru.preview.includeorderbyclause");
 			if (includeOrderBy == null || !includeOrderBy.equalsIgnoreCase("false"))
 				query += buildOrderByClause();
 			if (getReportWizard().getDataSubSource().getDatabaseType() == ReportDatabaseType.MYSQL) {
 				if (getReportWizard().getRowCount() == -1 || getReportWizard().getRowCount() > 2000)
 					query += " LIMIT 0, 2000";
 				else
 					query += " LIMIT 0," + getReportWizard().getRowCount();
 			}	
 			query += ";";
 		}
 		return query;
 	}
 	
 	/**
 	 * Builds and returns a mySql or SQL Server query.
 	 * <P>
 	 * {@code} String query = getQueryString(reportSegmentationTypeService.find(wiz.getReportSegmentationTypeId()).getColumnName());
      * @param columnName The name of the ID column that is to be selected for the segmentation results.
 	 * @return String
 	 * @throws ParseException
 	 */
 	public String getSegmentationQueryString(String columnName) throws ParseException {
 		String query = buildSegmentationSelectClause(reportWizard.getId(), columnName);
 		query += buildSegmentationWhereClause(columnName);
 		query += System.getProperty("line.separator") + "ORDER BY " + columnName;
 		if (getReportWizard().getDataSubSource().getDatabaseType() == ReportDatabaseType.MYSQL
 				&& getReportWizard().getRowCount() > 0)
 			query += " LIMIT 0," + getReportWizard().getRowCount();
 		query += ";";
 		return query;
 	}
 
 	/**
 	 * Builds and returns a select clause based on the selected report fields.
 	 * <P>
 	 * {@code} String selectClause = buildSelectClause(false);
 	 * @param preview A value that indicates whether a preview SQL statment should be created.
 	 * @return String
 	 */
 	private String buildSelectClause(Boolean preview) {
 		String selectClause = "SELECT";
 
 		selectClause += " DISTINCT";
 
 		if (getReportWizard().getDataSubSource().getDatabaseType() == ReportDatabaseType.SQLSERVER
 				&& (getReportWizard().getRowCount() > 0	|| preview))
 			if (preview && getReportWizard().getRowCount() > 2000)
 				selectClause += " TOP 100";
 			else
 				selectClause += " TOP " + getReportWizard().getRowCount();
 
 		if (getReportWizard().getReportType().compareToIgnoreCase("matrix") == 0)
 		{
 			selectClause += buildSelectFieldsForMatrix(null);
 		} else {
 			selectClause += buildSelectFieldsForNonMatrix(null);
 		}
 
 		selectClause = selectClause + System.getProperty("line.separator") + "FROM " + getReportWizard().getDataSubSource().getViewName();
 
 		return selectClause;
 	}
 
 	/**
 	 * Builds and returns a segmentation select clause based on the report ID and column name.
 	 * {@code} String selectClause = buildSegmentationSelectClause(wiz.getId(), columnName);
 	 * @param reportId The ID of the report
 	 * @param columnName The column name to select and save to the THEGURU_SEGMENTATION_RESULT table
 	 * @return
 	 */
 	private String buildSegmentationSelectClause(Long reportId, String columnName) {
 		String selectClause = "SELECT DISTINCT ";
 
 		if (getReportWizard().getDataSubSource().getDatabaseType() == ReportDatabaseType.SQLSERVER
 				&& getReportWizard().getRowCount() > 0)
 			selectClause += "TOP " + getReportWizard().getRowCount() + " ";
 
 
 		selectClause += reportId.toString() + " AS REPORT_ID, " + columnName + " AS ENTITY_ID ";
 
 		selectClause = selectClause + System.getProperty("line.separator") + "FROM " + getReportWizard().getDataSubSource().getViewName();
 
 		return selectClause;
 	}
 
 	/**
 	 * Builds and returns a segmentation where clause based on the column name.
 	 * {@code} String whereClause = buildSegmentationWhereClause(columnName);
 	 * @param columnName The column name to select and save to the THEGURU_SEGMENTATION_RESULT table.  The where clause will require that the field is not null.
 	 * @return String
 	 * @throws ParseException
 	 */
 	private String buildSegmentationWhereClause(String columnName) throws ParseException {
 		String whereClause = buildWhereClause(true);
 		if (whereClause.length() > 0)
 			whereClause += System.getProperty("line.separator") + "AND ";
 		else
 			whereClause += System.getProperty("line.separator") + "WHERE ";
 		whereClause += "(" + columnName + " IS NOT NULL)";
 		return whereClause;
 	}
 
 	/**
 	 * Builds and returns a the fields for tabular or summary reports for the select clause.
 	 * <P>
 	 * {@code} String selectClause += buildSelectFieldsForNonMatrix();
 	 * @return String
 	 */
 	private String buildSelectFieldsForNonMatrix(DynamicReportQuery dynamicReportQuery) {
 		String selectClause = System.getProperty("line.separator");
 		Iterator<ReportSelectedField> itReportSelectedFields = getReportWizard().getReportSelectedFields().iterator();
 		boolean addComma = false;
 		Integer columnIndex = 0;
 		String columnName = null;
 		HashMap<String, String> primaryKeys = new HashMap<String, String>();
 		while (itReportSelectedFields.hasNext()) {
 			ReportSelectedField selectedField = (ReportSelectedField) itReportSelectedFields.next();
 			ReportField reportField = getReportFieldService().find(selectedField.getFieldId());
 			if (dynamicReportQuery == null) {
 				primaryKeys.put(reportField.getPrimaryKeys(), reportField.getPrimaryKeys());
 			} else if (reportField.getDynamicPrimaryKeys() != null && !reportField.getDynamicPrimaryKeys().isEmpty()) {
 				String primaryKey = dynamicReportQuery.tableAliases.get(dynamicReportQuery.fieldTableKeys.get(reportField.getId())) + "." + reportField.getDynamicPrimaryKeys();
 				if (!dynamicReportQuery.primaryKeys.contains(primaryKey))
 					dynamicReportQuery.primaryKeys.add(primaryKey);
 			}
 			if (reportField.getAliasName() == null || reportField.getAliasName().length() == 0)
 				columnName = reportField.getColumnName() + "_" + columnIndex;
 			else
 				columnName = getFieldNameTextQualifier() + reportField.getAliasName() + "_" + columnIndex + getFieldNameTextQualifier();
 			if (reportField == null || reportField.getId() == -1) continue;
 
 			if (addComma)
 				selectClause += "," + System.getProperty("line.separator");
 			else
 				addComma = true;
 				
 			if (dynamicReportQuery == null) 
 				selectClause += getFieldNameForSelectOrWhereClause(reportField, null) + " as " + columnName;
 			else
 				selectClause += getSourceColumnNameFromReportField(reportField, dynamicReportQuery) + " as " + columnName;
 				
 			columnIndex++;
 		}
 		
 		//Add all the primary_key fields so that the records are not accidentally eliminated
 		//by the DISTINCT in the select clause
 		if (!reportWizard.getUniqueRecords()){
 			if (dynamicReportQuery == null) {
 				if (primaryKeys.size() > 0){
 					Iterator<String> itPrimaryKeys = primaryKeys.values().iterator();
 					while (itPrimaryKeys.hasNext()){
 						String pk = (String) itPrimaryKeys.next();
 						if (pk != null)
 							selectClause += " ," + pk;
 					}
 				}
 			} else {
 				for (String primaryKey : dynamicReportQuery.primaryKeys) {
 					selectClause += "," + dynamicReportQuery.crlf + primaryKey;
 				}				
 			}
 		}
 		return selectClause;
 	}
 
 
 	/**
 	 * Builds and returns a the fields for matrix reports for the select clause.
 	 * <P>
 	 * {@code} String selectClause += buildSelectFieldsForMatrix();
 	 * @return String
 	 */
 	private String buildSelectFieldsForMatrix(DynamicReportQuery dynamicReportQuery) {
 		String selectClause = "";
 		boolean addComma = false;
 		Integer columnIndex = 0;
 		String columnName = null;
 
 		//These must be added in the same order as the ReportGenerator.CreateCrosstab() method
 		//Add the Measure
 		List<ReportCrossTabMeasure> colMeasure = getReportWizard().getReportCrossTabFields().getReportCrossTabMeasure();
 		Iterator<ReportCrossTabMeasure> itMeasure = colMeasure.iterator();
 		while (itMeasure.hasNext()){
 			ReportCrossTabMeasure fGroupBy = (ReportCrossTabMeasure) itMeasure.next();
 			if (fGroupBy != null && fGroupBy.getFieldId() != -1){
 				ReportField reportField = reportFieldService.find(fGroupBy.getFieldId());
 				if (reportField.getAliasName() == null || reportField.getAliasName().length() == 0)
 					columnName = reportField.getColumnName() + "_" + columnIndex;
 				else
 					columnName = getFieldNameTextQualifier() + reportField.getAliasName() + "_" + columnIndex + getFieldNameTextQualifier();
 					if (addComma)
 						selectClause = selectClause + "," + System.getProperty("line.separator");
 					else
 						addComma = true;
 					
 					if (dynamicReportQuery == null) {
 						selectClause = selectClause + " " + getFieldNameForSelectOrWhereClause(reportField, null) + " as " + columnName;	
 					} else {
 						selectClause = selectClause + " " + getSourceColumnNameFromReportField(reportField, dynamicReportQuery) + " as " + columnName;
 					}
 					
 					//Add the primary_keys column to the select so that the DISTINCT in the select clause
 					//does not remove data that should be there.
 					if (dynamicReportQuery == null) {
 						if (reportField.getPrimaryKeys() != null){
 							selectClause = selectClause + "," + System.getProperty("line.separator") + reportField.getPrimaryKeys();
 						}
 					} else {
 						String primaryKey = dynamicReportQuery.tableAliases.get(dynamicReportQuery.fieldTableKeys.get(reportField.getId())) + "." + reportField.getDynamicPrimaryKeys();
 						if (!dynamicReportQuery.primaryKeys.contains(primaryKey)) {
 							dynamicReportQuery.primaryKeys.add(primaryKey);
 							selectClause += "," + dynamicReportQuery.crlf + primaryKey;
 						}
 						selectClause += "," + dynamicReportQuery.crlf + primaryKey;
 						if (!dynamicReportQuery.primaryKeys.contains(primaryKey))
 							dynamicReportQuery.primaryKeys.add(primaryKey);						
 					}
 					columnIndex++;
 			}
 		}
 		
 		//Add the rows
 		List<ReportCrossTabRow> rowFields = getReportWizard().getReportCrossTabFields().getReportCrossTabRows();
 		Iterator<ReportCrossTabRow> itRow = rowFields.iterator();
 		while (itRow.hasNext()){
 			ReportCrossTabRow fGroupBy = (ReportCrossTabRow) itRow.next();
 			if (fGroupBy != null && fGroupBy.getFieldId() != -1 ){
 				ReportField reportField = reportFieldService.find(fGroupBy.getFieldId());
 				if (reportField.getAliasName() == null || reportField.getAliasName().length() == 0)
 					columnName = reportField.getColumnName() + "_" + columnIndex;
 				else
 					columnName = getFieldNameTextQualifier() + reportField.getAliasName() + "_" + columnIndex + getFieldNameTextQualifier();
 					if (addComma)
 						selectClause = selectClause + "," + System.getProperty("line.separator");
 					else
 						addComma = true;
 					
 					if (dynamicReportQuery == null) {
 						selectClause = selectClause + reportField.getColumnName() + " as " + columnName;
 					} else {
 						selectClause = selectClause + getSourceColumnNameFromReportField(reportField, dynamicReportQuery) + " as " + columnName;	
 					}
 					columnIndex++;
 				}
 		}
 		
 		//Add Column Fields
 		List<ReportCrossTabColumn> colFields = getReportWizard().getReportCrossTabFields().getReportCrossTabColumns();
 		Iterator<ReportCrossTabColumn> itCol = colFields.iterator();
 		while (itCol.hasNext()){
 			ReportCrossTabColumn fGroupBy = (ReportCrossTabColumn) itCol.next();
 			if (fGroupBy != null && fGroupBy.getFieldId() != -1){
 				ReportField reportField = reportFieldService.find(fGroupBy.getFieldId());
 				if (reportField.getAliasName() == null || reportField.getAliasName().length() == 0)
 					columnName = reportField.getColumnName() + "_" + columnIndex;
 				else
 					columnName = getFieldNameTextQualifier() + reportField.getAliasName() + "_" + columnIndex + getFieldNameTextQualifier();
 					if (addComma)
 						selectClause = selectClause + "," + System.getProperty("line.separator");
 					else
 						addComma = true;
 					
 					if (dynamicReportQuery == null) { 
 						selectClause = selectClause + reportField.getColumnName() + " as " + columnName;
 					} else {
 						selectClause = selectClause + getSourceColumnNameFromReportField(reportField, dynamicReportQuery) + " as " + columnName;
 					}
 					columnIndex++;
 				}
 		}
 
 		return selectClause;
 	}
 
 	/**
 	 * Builds and returns a where clause based on the filters.
 	 * <P>
 	 * {@code} String whereClause = buildWhereClause();
 	 * @return String
 	 * @throws ParseException
 	 */
 	private String buildWhereClause(Boolean segmentation) throws ParseException {
 		String whereClause = "";
 		// very first criteria doesn't need an and
 		boolean afterGroup = true;
 		int index = 0;
 		Iterator<ReportFilter> itFilters = getReportWizard().getReportFilters().iterator();
 		boolean addWhere = true;
 		// tracks whether the filters contain actual criteria or not
 		boolean hasCriteria = false;
 		while (itFilters.hasNext()) {
 			index++;
 			ReportFilter filter = (ReportFilter) itFilters.next();
 			if (filter == null) continue; // this is an empty filter
 			if (filter.getFilterType() == 1 && filter.getReportStandardFilter().getFieldId() == -1) continue; // this is an empty filter
 			if (filter.getFilterType() == 2 && filter.getReportCustomFilter().getCustomFilterDefinitionId() <= 0) continue; // this is an empty filter
 
 			if (addWhere) {
 				whereClause += System.getProperty("line.separator") + "WHERE";
 				if (segmentation)
 					whereClause += " (";
 				addWhere = false;
 			} else if (!afterGroup && filter.getFilterType() != 4) {
 				// Do not add And or Or the first record after a group, or on end groups
 				if (filter.getOperator() == 0)
 					whereClause += " AND";
 				else if (filter.getOperator() == 1)
 					whereClause += " OR";
 			}
 
 			if (filter.getOperatorNot() == 1)
 				whereClause += " NOT";
 			if (filter.getFilterType() == 3) {
 				afterGroup = true;
 				whereClause += " (";
 			} else if (filter.getFilterType() == 4) {
 				afterGroup = false;
 				whereClause += " )";
 			} else if (filter.getFilterType() == 1) {
 				hasCriteria = true;
 				afterGroup = false;
 				whereClause += buildStandardFilterWhereClause(filter.getReportStandardFilter(), index, null);
 			} else if (filter.getFilterType() == 2) {
 				hasCriteria = true;
 				afterGroup = false;
 				whereClause += buildCustomFilterWhereClause(filter.getReportCustomFilter());
 			}
 		}
 		if (!hasCriteria)
 			return "";
 		else
 			if (segmentation)
 				return whereClause + ")";
 			else
 				return whereClause;
 	}
 
 	/**
 	 * Builds and returns a portion of a where clause for the standard filters.
 	 * <P>
 	 * {@code} whereClause += buildStandardFilterWhereClause(false);
 	 * @param includeWhere Specifies whether the returned string should begin with a WHERE if true, or with an AND if false.
 	 * @return String
 	 * @throws ParseException
 	 */
 	private String buildStandardFilterWhereClause(ReportStandardFilter reportStandardFilter, int index, DynamicReportQuery dynamicReportQuery) throws ParseException {
 		String whereClause = " (";
 		ReportField rf = reportFieldService.find(reportStandardFilter.getFieldId());
 		String controlName = rf.getAliasName() + Integer.toString(index);
 
 		String columnName;
 		if (getReportWizard().getUseDynamicSQLGeneration()) {
 			columnName = getSourceColumnNameFromReportField(rf, dynamicReportQuery);
 		} else {
 			columnName = rf.getColumnName();
 		}
 
 		String sqlDateType = "";
 		if (getReportWizard().getDataSubSource().getDatabaseType() == ReportDatabaseType.MYSQL)
 			sqlDateType += "DATE";
 		else if (getReportWizard().getDataSubSource().getDatabaseType() == ReportDatabaseType.SQLSERVER)
 			sqlDateType += "DATETIME";
 
 		switch(reportStandardFilter.getComparison()) {
 			case 1:
 				whereClause += getFieldNameForSelectOrWhereClause(rf, dynamicReportQuery) + " =";
 				whereClause += buildPromptForCritiera(reportStandardFilter, controlName, rf);
 				break;
 			case 2:
 				whereClause += getFieldNameForSelectOrWhereClause(rf, dynamicReportQuery) + " !=";
 				whereClause += buildPromptForCritiera(reportStandardFilter, controlName, rf);
 				break;
 			case 3:
 				whereClause += getFieldNameForSelectOrWhereClause(rf, dynamicReportQuery) + " <";
 				whereClause += buildPromptForCritiera(reportStandardFilter, controlName, rf);
 				break;
 			case 4:
 				whereClause += getFieldNameForSelectOrWhereClause(rf, dynamicReportQuery) + " >";
 				whereClause += buildPromptForCritiera(reportStandardFilter, controlName, rf);
 				break;
 			case 5:
 				whereClause += getFieldNameForSelectOrWhereClause(rf, dynamicReportQuery) + " <=";
 				whereClause += buildPromptForCritiera(reportStandardFilter, controlName, rf);
 				break;
 			case 6:
 				whereClause += getFieldNameForSelectOrWhereClause(rf, dynamicReportQuery) + " >=";
 				whereClause += buildPromptForCritiera(reportStandardFilter, controlName, rf);
 				break;
 			case 7:
 				whereClause += getFieldNameForSelectOrWhereClause(rf, dynamicReportQuery) + " LIKE";
 				if (getReportWizard().getDataSubSource().getDatabaseType() == ReportDatabaseType.MYSQL)
 					whereClause += " CONCAT(";
 				else if (getReportWizard().getDataSubSource().getDatabaseType() == ReportDatabaseType.SQLSERVER)
 					whereClause += "";
 				whereClause += buildPromptForCritiera(reportStandardFilter, controlName, rf);
 				if (getReportWizard().getDataSubSource().getDatabaseType() == ReportDatabaseType.MYSQL)
 					whereClause += " , '%')";
 				else if (getReportWizard().getDataSubSource().getDatabaseType() == ReportDatabaseType.SQLSERVER)
 					whereClause += " + '%'";
 				break; // starts with
 			case 8:
 				whereClause += getFieldNameForSelectOrWhereClause(rf, dynamicReportQuery) + " LIKE";
 				if (getReportWizard().getDataSubSource().getDatabaseType() == ReportDatabaseType.MYSQL)
 					whereClause += " CONCAT( '%',";
 				else if (getReportWizard().getDataSubSource().getDatabaseType() == ReportDatabaseType.SQLSERVER)
 					whereClause += " '%' +";
 				whereClause += buildPromptForCritiera(reportStandardFilter, controlName, rf);
 				if (getReportWizard().getDataSubSource().getDatabaseType() == ReportDatabaseType.MYSQL)
 					whereClause += " )";
 				else if (getReportWizard().getDataSubSource().getDatabaseType() == ReportDatabaseType.SQLSERVER)
 					whereClause += "";
 				break; // ends with
 			case 9:
 				whereClause += getFieldNameForSelectOrWhereClause(rf, dynamicReportQuery) + " LIKE";
 				if (getReportWizard().getDataSubSource().getDatabaseType() == ReportDatabaseType.MYSQL)
 					whereClause += " CONCAT( '%',";
 				else if (getReportWizard().getDataSubSource().getDatabaseType() == ReportDatabaseType.SQLSERVER)
 					whereClause += " '%' +";
 				whereClause += buildPromptForCritiera(reportStandardFilter, controlName, rf);
 				if (getReportWizard().getDataSubSource().getDatabaseType() == ReportDatabaseType.MYSQL)
 					whereClause += "  , '%') ";
 				else if (getReportWizard().getDataSubSource().getDatabaseType() == ReportDatabaseType.SQLSERVER)
 					whereClause += " + '%'";
 				break; // contains
 			case 10:
 				whereClause += getFieldNameForSelectOrWhereClause(rf, dynamicReportQuery) + " NOT LIKE";
 				if (getReportWizard().getDataSubSource().getDatabaseType() == ReportDatabaseType.MYSQL)
 					whereClause += " CONCAT( '%',";
 				else if (getReportWizard().getDataSubSource().getDatabaseType() == ReportDatabaseType.SQLSERVER)
 					whereClause += " '%' + ";
 				whereClause += buildPromptForCritiera(reportStandardFilter, controlName, rf);
 				if (getReportWizard().getDataSubSource().getDatabaseType() == ReportDatabaseType.MYSQL)
 					whereClause += "  , '%')";
 				else if (getReportWizard().getDataSubSource().getDatabaseType() == ReportDatabaseType.SQLSERVER)
 					whereClause += " + '%'";
 				break; // does not contain
 			case 11:
 				whereClause += getFieldNameForSelectOrWhereClause(rf, dynamicReportQuery) + " IS NOT NULL";
 				break;
 			case 12:
 				if (reportStandardFilter.getPromptForCriteria() && reportStandardFilter.getComparison() == 12) {
 					//
 					// we are doing a one of and requesting a prompt so we need something like $X(In,fieldName,paramaterName)
 					whereClause += "$X(In," + getFieldNameForSelectOrWhereClause(rf, dynamicReportQuery) + "," + controlName +")";
 				} else {
 					whereClause += getFieldNameForSelectOrWhereClause(rf, dynamicReportQuery) + " IN (";
 					whereClause += buildPromptForCritiera(reportStandardFilter, controlName, rf);
 					whereClause += ") ";
 				}
 				break;
 			// Duration filters
 			case 20: // Current FY
 				break;
 			case 21: // Previous FY
 				break;
 			case 22: // Current FY
 				break;
 			case 23: // Current FY
 				break;
 			case 24: // Current FY
 				break;
 			case 25: // Current FY
 				break;
 			case 26: // Current FY
 				break;
 			case 27: // Current FY
 				break;
 			case 28: // Current FY
 				break;
 			case 29: // Current FY
 				break;
 			case 30: // Current Calendar Year
 				whereClause += getSqlCalendarDurationCriteriaFromCurrentDate(DatePart.YEAR, columnName, 0);
 				break;
 			case 31: // Previous Calendar Year
 				whereClause += getSqlCalendarDurationCriteriaFromCurrentDate(DatePart.YEAR, columnName, -1);
 				break;
 			case 32: // Current and Previous Calendar Year
 				whereClause += "( " + getSqlCalendarDurationCriteriaFromCurrentDate(DatePart.YEAR, columnName, -1) +
 							   " OR " +
 							   getSqlCalendarDurationCriteriaFromCurrentDate(DatePart.YEAR, columnName, 0) + " )";
 				break;
 			case 33: // Current Calendar Month
 				whereClause += getSqlCalendarDurationCriteriaFromCurrentDate(DatePart.MONTH, columnName, 0);
 				break;
 			case 34: // Previous Calendar Month
 				whereClause += getSqlCalendarDurationCriteriaFromCurrentDate(DatePart.MONTH, columnName, -1);
 				break;
 			case 35: // Current and Previous Calendar Month
 				whereClause += "( " + getSqlCalendarDurationCriteriaFromCurrentDate(DatePart.MONTH, columnName, -1) +
 				   			   " OR " +
 				   			   getSqlCalendarDurationCriteriaFromCurrentDate(DatePart.MONTH, columnName, 0) + " )";
 				break;
 			case 36: // Current Calendar Week
 				whereClause += getSqlCalendarDurationCriteriaFromCurrentDate(DatePart.WEEK, columnName, 0);
 				break;
 			case 37: // Previous Calendar Week
 				whereClause += getSqlCalendarDurationCriteriaFromCurrentDate(DatePart.WEEK, columnName, -1);
 				break;
 			case 38: // Current and Previous Calendar Week
 				whereClause += "( " + getSqlCalendarDurationCriteriaFromCurrentDate(DatePart.WEEK, columnName, -1) +
 				   			   " OR " +
 				   			   getSqlCalendarDurationCriteriaFromCurrentDate(DatePart.WEEK, columnName, 0) + " )";
 				break;
 			case 39: // Today
 				whereClause += "CAST(" + columnName +" AS " + sqlDateType + ") " + " = " + getSqlCriteriaDaysFromCurrentDate(0);
 				break;
 			case 40: // Yesterday
 				whereClause += "CAST(" + columnName +" AS " + sqlDateType + ") " + " = " + getSqlCriteriaDaysFromCurrentDate(-1);
 				break;
 			case 41: // Last 7
 				whereClause += "CAST(" + columnName +" AS " + sqlDateType + ") " +  " > " + getSqlCriteriaDaysFromCurrentDate(-7);
 				break;
 			case 42: // Last 30
 				whereClause += "CAST(" + columnName +" AS " + sqlDateType + ") " +  " > " + getSqlCriteriaDaysFromCurrentDate(-30);
 				break;
 			case 43: // Last 60
 				whereClause += "CAST(" + columnName +" AS " + sqlDateType + ") " +  " > " + getSqlCriteriaDaysFromCurrentDate(-60);
 				break;
 			case 44: // Last 90
 				whereClause += "CAST(" + columnName +" AS " + sqlDateType + ") " +  " > " + getSqlCriteriaDaysFromCurrentDate(-90);
 				break;
 			case 45: // Last 120
 				whereClause += "CAST(" + columnName +" AS " + sqlDateType + ") " +  " > " + getSqlCriteriaDaysFromCurrentDate(-120);
 				break;
 		}
 		whereClause += ") ";
 		return whereClause;
 	}
 
 	/**
 	 * Builds and returns a portion of a standard filter where clause based on the field type and whether PromptForCriteria was selected.
 	 * <P>
 	 * whereClause += buildCustomFilterWhereClause(filter.getReportCustomFilter());
 	 * @return String
 	 * @throws ParseException
 	 */
 	private String buildPromptForCritiera(ReportStandardFilter filter, String controlName, ReportField rf) throws ParseException {
 		String whereClause = "";
 		if ( rf.getFieldType() == ReportFieldType.DATE) {
 			if (filter.getPromptForCriteria()) {
 				whereClause += " $P{" + controlName + "} ";
 			} else {
 				whereClause += getFormattedDateString(filter.getCriteria());
 			}
 		} else	if(rf.getFieldType() == ReportFieldType.STRING) {
 			if (filter.getPromptForCriteria()) {
 				whereClause += " $P{" + controlName + "} ";
 			} else {
 				whereClause += " '" + filter.getCriteria() + "'";
 				if (filter.getComparison() == 12) {
 					//  We are doing a 'is one of' so we need to strip spaces and replace , with ','
 					whereClause = whereClause.replaceAll(" ", "");
 					whereClause = whereClause.replaceAll(",", "','"); // quote strings
 				}
 			}
 		} else if(rf.getFieldType() == ReportFieldType.DOUBLE) {
 			if (filter.getPromptForCriteria()) {
 				whereClause += " $P{" + controlName + "} ";
 			} else {
 				whereClause += " " + filter.getCriteria();
 			}
 		} else if(rf.getFieldType() == ReportFieldType.INTEGER) {
 			if (filter.getPromptForCriteria()) {
 				whereClause += " $P{" + controlName + "} ";
 			} else {
 				whereClause += " " + filter.getCriteria();
 			}
 		} else if(rf.getFieldType() == ReportFieldType.MONEY) {
 			if (filter.getPromptForCriteria()) {
 				whereClause += " $P{" + controlName + "} ";
 			} else {
 				whereClause += " " + filter.getCriteria();
 			}
 		} else if(rf.getFieldType() == ReportFieldType.BOOLEAN) {
 			if (filter.getPromptForCriteria()) {
 				whereClause += " $P{" + controlName + "} ";
 			} else {
 				whereClause += " " + filter.getCriteria();
 			}
 		}
 		return whereClause;
 	}
 
 	/**
 	 * Builds and returns a portion of a where clause for the custom filter.
 	 * <P>
 	 * whereClause += buildCustomFilterWhereClause(filter.getReportCustomFilter());
 	 * @return String
 	 * @throws ParseException
 	 */
 	private String buildCustomFilterWhereClause(ReportCustomFilter filter) throws ParseException {
 		String whereClause = " ";
 		ReportCustomFilterDefinition reportCustomFilterDefinition = getReportCustomFilterDefinitionService().find(filter.getCustomFilterDefinitionId());
 		if (reportCustomFilterDefinition != null) {
 			String filterString = reportCustomFilterDefinition.getSqlText();
 			if (filterString.length() != 0) {
 				filterString = filterString.replace("[VIEWNAME]", getReportWizard().getDataSubSource().getViewName());
 				int criteriaSize = filter.getReportCustomFilterCriteria().size();
 				for (int index = 0; index < criteriaSize; index++) {
 					filterString = filterString.replace("{" + Integer.toString(index) + "}", filter.getReportCustomFilterCriteria().get(index).getCriteria());
 				}
 				
 				String lookupReferenceBeanString = "{lookupReferenceBean:";
 				int stringIndex = filterString.indexOf("{", 0);
 				while (stringIndex > 0)
 				{
 					int beginIndex = filterString.indexOf("{", stringIndex);
 					int endIndex = filterString.indexOf("}", beginIndex) + 1;
 					String field = filterString.substring(beginIndex, endIndex);
 					// If it is a lookup reference, get the bean name and call the lookup.
 					if (field.startsWith(lookupReferenceBeanString)) {
 						String beanName = field.replace("{lookupReferenceBean:", "").replace("}", "");
 						ReportCustomFilterCriteriaLookupService reportCustomFilterCriteriaLookupService = (ReportCustomFilterCriteriaLookupService)applicationContext.getBean(beanName);
 
 						filterString = filterString.substring(0, beginIndex)
 							+ reportCustomFilterCriteriaLookupService.getLookupSql(filter)
 							+ filterString.substring(endIndex);
 					}
 					stringIndex = filterString.indexOf("{", stringIndex + 1);
 				}
 			}
 			whereClause += filterString;
 		}
 		return whereClause;
 	}
 
 	/**
 	 * Attempts to parse the incoming date string using the default locale first, and then various
 	 * other date formats.  If it is able to parse the date, it will then return a string with the
 	 * date formatted for mySql and SQL Server.
 	 * <P>
 	 * {@code} whereClause += getFormattedDateString(filter.getCriteria());
 	 * @param dateString The string containing the date to be parsed and formatted.
 	 * @return String Date string formatted for mySql and SQL Server
 	 * @throws ParseException The exception to be thrown if the dateString is unable to be parsed.
 	 */
 	private String getFormattedDateString(String dateString) throws ParseException {
 		String result = "";
 		ParseException lastException = null;
 		Date whereDate = null;
 		SimpleDateFormat resultDateFormat = new SimpleDateFormat("yyyy-MM-dd");
 
 		// First attempt to use the default locale to attempt to parse the date
 		try {
 			whereDate = DateFormat.getDateInstance(DateFormat.SHORT).parse(dateString);
 			result = " '" +  resultDateFormat.format(whereDate) + "'";
 			return result;
 		}
 		catch (ParseException exception) {
 			lastException = exception;
 		}
 
 		ArrayList<String> dateFormatStrings = new ArrayList<String>();
 		// Add various date formats
 		dateFormatStrings.add("yyyy-MM-dd");
 		dateFormatStrings.add("yyyy/MM/dd");
 		dateFormatStrings.add("yyyy MM dd");
 		dateFormatStrings.add("yyyy.MM.dd");
 
 		dateFormatStrings.add("yyyy-MMM-dd");
 		dateFormatStrings.add("yyyy/MMM/dd");
 		dateFormatStrings.add("yyyy MMM dd");
 		dateFormatStrings.add("yyyy.MMM.dd");
 
 		dateFormatStrings.add("MM-dd-yyyy");
 		dateFormatStrings.add("MM/dd/yyyy");
 		dateFormatStrings.add("MM dd yyyy");
 		dateFormatStrings.add("MM.dd.yyyy");
 
 		dateFormatStrings.add("dd-MM-yyyy");
 		dateFormatStrings.add("dd/MM/yyyy");
 		dateFormatStrings.add("dd MM yyyy");
 		dateFormatStrings.add("dd.MM.yyyy");
 
 		dateFormatStrings.add("dd-MMM-yy");
 		dateFormatStrings.add("dd MMM yy");
 		dateFormatStrings.add("dd.MMM.yy");
 		dateFormatStrings.add("dd/MMM/yy");
 
 		dateFormatStrings.add("yyyy-MM-dd hh:mm:ss");
 		dateFormatStrings.add("yyyy MM dd hh:mm:ss");
 		dateFormatStrings.add("yyyy.MM.dd hh:mm:ss");
 		dateFormatStrings.add("yyyy/MM/dd hh:mm:ss");
 
 		dateFormatStrings.add("yyyy-MMM-dd hh:mm:ss");
 		dateFormatStrings.add("yyyy MMM dd hh:mm:ss");
 		dateFormatStrings.add("yyyy.MMM.dd hh:mm:ss");
 		dateFormatStrings.add("yyyy/MMM/dd hh:mm:ss");
 
 		dateFormatStrings.add("dd-MM-yyyy hh:mm:ss");
 		dateFormatStrings.add("dd MM yyyy hh:mm:ss");
 		dateFormatStrings.add("dd.MM.yyyy hh:mm:ss");
 		dateFormatStrings.add("dd/MM/yyyy hh:mm:ss");
 
 		dateFormatStrings.add("dd-MMM-yyyy hh:mm:ss");
 		dateFormatStrings.add("dd/MMM/yyyy hh:mm:ss");
 		dateFormatStrings.add("dd MMM yyyy hh:mm:ss");
 		dateFormatStrings.add("dd.MMM.yyyy hh:mm:ss");
 
 		for (String dateFormatString : dateFormatStrings) {
 	    	try {
 	    		SimpleDateFormat dateFormat = new SimpleDateFormat(dateFormatString);
 	    		dateFormat.setLenient(false);
 	    		whereDate = dateFormat.parse(dateString);
 				result = " '" +  resultDateFormat.format(whereDate) + "'";
 				return result;
 	    	}
 	  		catch (ParseException exception) {
 	  			lastException = exception;
 	  		}
 		}
 
 		// If no date was parsed, throw the last parse exception
 		if (result.length() == 0 && lastException != null)
 			throw lastException;
 
 		return result;
 	}
 
 	/**
 	 * Returns a value for the field that will be used in the select or where clause.  For DateTime fields
 	 * this will return a string that will cause the time to be removed from the DateTime.  Other
 	 * field types may be returned with just the column name.
 	 * <P>
 	 * {@code} whereClause += " " + getFieldNameForSelectOrWhereClause(reportField);
 	 * @param reportField The report field for which the function will generate a string to be used in the where clause.
 	 * @return String
 	 */
 	private String getFieldNameForSelectOrWhereClause(ReportField reportField, DynamicReportQuery dynamicReportQuery) {
 		String result = "";
 		String columnName;
 		
 		if (getReportWizard().getUseDynamicSQLGeneration()) {
 			columnName = getSourceColumnNameFromReportField(reportField, dynamicReportQuery);
 		} else {
 			columnName = reportField.getColumnName();
 		}
 		
 		if (reportField.getFieldType() == ReportFieldType.DATE) {
 			if (getReportWizard().getDataSubSource().getDatabaseType() == ReportDatabaseType.MYSQL) {
 				result = "CAST(" + columnName + " AS DATE)";
 			}
 			else if (getReportWizard().getDataSubSource().getDatabaseType() == ReportDatabaseType.SQLSERVER) {
 				result = "DATEADD(DAY, DATEDIFF(DAY, 0, " + columnName + "), 0)";
 			}
 		}
 		else {
 			result = columnName;
 		}
 		return result;
 	}
 
 	/**
 	 * Builds and returns a string for either mySql or SQL Server that subtracts the specified number of days from the current date.
 	 * <P>
 	 * {@code} whereClause += rf.getColumnName() + " = " + getSqlCriteriaDaysFromCurrentDate(-1);
 	 * @param days Number of days from the current date
 	 * @return String
 	 */
 	private String getSqlCriteriaDaysFromCurrentDate(int days) {
 		String result = "";
 		if (getReportWizard().getDataSubSource().getDatabaseType() == ReportDatabaseType.MYSQL) {
 			result = "DATE_ADD(CURDATE(),INTERVAL " + Integer.toString(days) + " DAY)";
 		}
 		else if (getReportWizard().getDataSubSource().getDatabaseType() == ReportDatabaseType.SQLSERVER) {
 			result = "DATEADD(DAY, DATEDIFF(DAY, 0, GETDATE()), " + Integer.toString(days) + ")";
 		}
 		return result;
 	}
 
 	/**
 	 * Builds and returns a string for either mySql or SQL Server that adds/subtracts the specified number of weeks, months, etc. from the current date.
 	 * <P>
 	 * {@code} whereClause += getSqlCalendarDurationCriteriaFromCurrentDate(DatePart.YEAR, rf.getColumnName(), 0);
 	 * @param DatePart datePart (YEAR, MONTH, QUARTER, WEEK)
 	 * @param String columnName Name of column
 	 * @param int duration Number of weeks, months, etc. from the current date.
 	 * @return String
 	 */
 	private String getSqlCalendarDurationCriteriaFromCurrentDate(DatePart datePart, String columnName, int duration) {
 		String result = "( ";
 		
 		if (getReportWizard().getDataSubSource().getDatabaseType() == ReportDatabaseType.MYSQL) {
 			result += datePart.mySQL() + "(" + columnName + ") = " + datePart.mySQL() + "(DATE_ADD(CURDATE(), INTERVAL " + Integer.toString(duration) + " " + datePart.mySQL() + "))";
 			if (datePart != DatePart.YEAR){
 				result += " AND YEAR(" + columnName + ") = YEAR(DATE_ADD(CURDATE(), INTERVAL " + Integer.toString(duration) + " " + datePart.mySQL() + "))";
 			}
 		}
 		else if (getReportWizard().getDataSubSource().getDatabaseType() == ReportDatabaseType.SQLSERVER) {
 			result += "DATEPART(" + datePart.SQL() + ", " + columnName + ") = (DATEPART(" + datePart.SQL() + ", DATEADD(" + datePart.SQL() + ", " + Integer.toString(duration) + ", GETDATE())))";
 			if (datePart != DatePart.YEAR){
 				result += " AND YEAR(" + columnName + ") = YEAR( DATEADD(" + datePart.SQL() + ", " + Integer.toString(duration) +  ", GETDATE()))" ;
 			}
 		}
 		result += " )";			
 		return result;
 	}
 
 	/**
 	 * Builds and returns the order by clause for a summary or matrix report.
 	 * <P>
 	 * {@code}String orderBy = buildOrderByClause();
 	 * @return String orderBy
 	 */
 	private String buildOrderByClause() {
 		// Add the order by clause
 		String orderBy = "";
 		if (getReportWizard().getReportType().compareTo("matrix") == 0) {
 			// Matrix Reports
 			orderBy = getOrderByClauseForMatrix();
 		} else {
 			// Tabular / Summary Reports
 			orderBy = getOrderByClauseForSummary();
 		}
 		return orderBy;
 	}
 
 	/**
 	 * Builds and returns the order by clause for a matrix report.
 	 * <P>
 	 * {@code} String orderBy = getOrderByClauseForMatrix();
 	 * @return String
 	 */
 	private String getOrderByClauseForMatrix() {
 		String orderBy = "";
 		Boolean addComma = false;
 		ReportCrossTabFields rptCTList = getReportWizard().getReportCrossTabFields();
 		List<ReportCrossTabRow> ctRows = rptCTList.getReportCrossTabRows();
 		addComma = false;
 
 		long columnIndex = 0;
 		List<ReportCrossTabMeasure> colMeasure = getReportWizard().getReportCrossTabFields().getReportCrossTabMeasure();
 		Iterator<ReportCrossTabMeasure> itMeasure = colMeasure.iterator();
 		while (itMeasure.hasNext()){
 			ReportCrossTabMeasure fGroupBy = (ReportCrossTabMeasure) itMeasure.next();
 			if (fGroupBy != null && fGroupBy.getFieldId() != -1){
 					columnIndex++;
 			}
 		}
 
 		List<Long> addedFields = new ArrayList<Long>();
 		//order by rows first
 		Iterator<ReportCrossTabRow> itCtRows = ctRows.iterator();
 		while (itCtRows.hasNext()){
 			ReportCrossTabRow rowField = (ReportCrossTabRow) itCtRows.next();
 			if (rowField != null) {
 				if (rowField.getFieldId() != -1){
 					ReportField rg = reportFieldService.find(rowField.getFieldId());
 					if (addedFields.indexOf(rg.getId()) == -1) {
 						if (!addComma) {
 							orderBy += System.getProperty("line.separator") + "ORDER BY";
 							addComma = true;
 						}
 						else
 							orderBy += ",";
 
 						if (rg.getAliasName() == null || rg.getAliasName().length() == 0)
 							orderBy += " " + rg.getColumnName() + "_" + columnIndex;
 						else
 							orderBy += " " + getFieldNameTextQualifier() + rg.getAliasName() + "_" + columnIndex + getFieldNameTextQualifier();
 
 						addedFields.add(rg.getId());
 						if (rowField.getSortOrder().compareTo("") != 0)
 							orderBy += " " + rowField.getSortOrder();
 						else
 							orderBy += " ASC";
 
 						columnIndex++;
 					}
 				}
 			}
 		}
 
 		//order by Columns last
 		List<ReportCrossTabColumn> ctCols = rptCTList.getReportCrossTabColumns();
 		Iterator<ReportCrossTabColumn> itCtCols = ctCols.iterator();
 		while (itCtCols.hasNext()){
 			ReportCrossTabColumn colField = (ReportCrossTabColumn) itCtCols.next();
 			if (colField != null) {
 				if (colField.getFieldId() != -1){
 					ReportField rg = reportFieldService.find(colField.getFieldId());
 					if (addedFields.indexOf(rg.getId()) == -1) {
 						if (!addComma) {
 							orderBy += System.getProperty("line.separator") + "ORDER BY";
 							addComma = true;
 						}
 						else
 							orderBy += ",";
 
 						if (rg.getAliasName() == null || rg.getAliasName().length() == 0)
 							orderBy += " " + rg.getColumnName() + "_" + columnIndex;
 						else
 							orderBy += " " + getFieldNameTextQualifier() + rg.getAliasName() + "_" + columnIndex + getFieldNameTextQualifier();
 
 						addedFields.add(rg.getId());
 						if (colField.getSortOrder().compareTo("") != 0)
 							orderBy += " " + colField.getSortOrder();
 						else
 							orderBy += " ASC";
 
 						columnIndex++;
 					}
 				}
 			}
 		}
 		return orderBy;
 	}
 
 	/**
 	 * Builds and returns the order by clause for a summary report.
 	 * <P>
 	 * {@code} String orderBy = getOrderByClauseForSummary();
 	 * @return String
 	 */
 	private String getOrderByClauseForSummary() {
 		String orderBy = "";
 		Boolean addComma = false;
 		List<ReportSelectedField> reportSelectedFields = getReportWizard().getReportSelectedFields();
 		Iterator<ReportSelectedField> itReportSelectedFields = reportSelectedFields.iterator();
 		List<Long> addedFields = new ArrayList<Long>();
 		long columnIndex = 0;
 		if (itReportSelectedFields != null){
 			addComma = false;
 			while (itReportSelectedFields.hasNext()) {
 				ReportSelectedField reportSelectedField = (ReportSelectedField) itReportSelectedFields.next();
 				if (reportSelectedField != null
 					&& reportSelectedField.getFieldId() != -1) {
 					ReportField rg = reportFieldService.find(reportSelectedField.getFieldId());
 					if (addedFields.indexOf(rg.getId()) == -1
 							&& (reportSelectedField.getSortOrder().compareTo("") != 0
 									|| reportSelectedField.getGroupBy())) {
 						if (!addComma) {
 							orderBy += System.getProperty("line.separator") + "ORDER BY";
 							addComma = true;
 						}
 						else
 							orderBy += ",";
 
 						if (rg.getAliasName() == null || rg.getAliasName().length() == 0)
 							orderBy += " " + rg.getColumnName() + "_" + columnIndex;
 						else
 							orderBy += " " + getFieldNameTextQualifier() + rg.getAliasName() + "_" + columnIndex + getFieldNameTextQualifier();
 
 						addedFields.add(rg.getId());
 						if (reportSelectedField.getSortOrder().compareTo("") != 0)
 							orderBy += " " + reportSelectedField.getSortOrder();
 						else
 							orderBy += " ASC";
 					}
 				}
 				columnIndex++;
 			}
 		}
 		return orderBy;
 	}
 	
 	private String getFieldNameTextQualifier(){
 		if (getReportWizard().getDataSubSource().getDatabaseType() == ReportDatabaseType.MYSQL) {
 			return "`";
 		} else if (getReportWizard().getDataSubSource().getDatabaseType() == ReportDatabaseType.SQLSERVER) {
 			return "\"";
 		} else {
 			return "";
 		}
 	}
 
 	private String buildDynamicMySQLStatment(boolean preview) throws ParseException {
 		DynamicReportQuery dynamicReportQuery = new DynamicReportQuery();
 
 		String viewName = getReportWizard().getDataSubSource().getViewName();
 		TheGuruView theGuruView = getTheGuruViewService().readTheGuruViewByViewName(viewName);
 
 		// Build table keys for all selected fields
 		if (getReportWizard().getReportType().compareToIgnoreCase("matrix") == 0) {
 			for (ReportCrossTabMeasure reportCrossTabMeasure : getReportWizard().getReportCrossTabFields().getReportCrossTabMeasure()) {
 				if (reportCrossTabMeasure != null && reportCrossTabMeasure.getFieldId() != -1){
 					populateTableKeyForReportFieldId(dynamicReportQuery, theGuruView, reportCrossTabMeasure.getFieldId());
 				}			
 			}
 			
 			for (ReportCrossTabRow reportCrossTabRow : getReportWizard().getReportCrossTabFields().getReportCrossTabRows()) {
 				if (reportCrossTabRow != null && reportCrossTabRow.getFieldId() != -1 ){
 					populateTableKeyForReportFieldId(dynamicReportQuery, theGuruView, reportCrossTabRow.getFieldId());
 				}
 			}
 			
 			for (ReportCrossTabColumn reportCrossTabColumn : getReportWizard().getReportCrossTabFields().getReportCrossTabColumns()) {
 				if (reportCrossTabColumn != null && reportCrossTabColumn.getFieldId() != -1 ){
 					populateTableKeyForReportFieldId(dynamicReportQuery, theGuruView, reportCrossTabColumn.getFieldId());
 				}
 			}
 		} else {
 			for (ReportSelectedField reportSelectedField : getReportWizard().getReportSelectedFields()) {
 				populateTableKeyForReportFieldId(dynamicReportQuery, theGuruView, reportSelectedField.getFieldId());			
 			}
 		}
 		
 		// Build table keys for all criteria fields
 		for (ReportFilter reportFilter : getReportWizard().getReportFilters()) {
 			if (reportFilter == null) continue; // this is an empty filter
 			if (reportFilter.getFilterType() == 1 && reportFilter.getReportStandardFilter().getFieldId() == -1) continue; // this is an empty filter
 			if (reportFilter.getFilterType() == 2 && reportFilter.getReportCustomFilter().getCustomFilterDefinitionId() <= 0) continue; // this is an empty filter
 			
 			if (reportFilter.getFilterType() == 1) {
 				ReportField reportField = getReportFieldService().find(reportFilter.getReportStandardFilter().getFieldId());
 				String tableKey = getTableKeyForReportField(reportField, theGuruView);
 				if (!dynamicReportQuery.fieldTableKeys.containsKey(reportField.getId()))
 					dynamicReportQuery.fieldTableKeys.put(reportField.getId(), tableKey);
 				if (!dynamicReportQuery.tableKeysFromFields.contains(tableKey))
 					dynamicReportQuery.tableKeysFromFields.add(tableKey);
 			} else if (reportFilter.getFilterType() == 2) {
 				//TODO handle customfilter fields
 			}
 		}
 		
 		//HashMap<String, TheGuruViewJoin> requiredJoins = buildRequiredJoins(tableKeysFromFields, allViews, allJoins, allJoinTableKeysInOrder, joinTableKeys, viewTableKeys);
 		buildRequiredJoins(dynamicReportQuery);
 		
 		// Assign all aliases first
 		for (String tableKey : dynamicReportQuery.allJoinTableKeysInOrder) {
 			if (dynamicReportQuery.requiredJoins.containsKey(tableKey)) {
 				String tableAlias = "`T" + Integer.toString(dynamicReportQuery.tableAliases.size()) + "`";
 				dynamicReportQuery.tableAliases.put(tableKey, tableAlias);
 			}
 		}
 
 		for (String tableKey : dynamicReportQuery.allJoinTableKeysInOrder) {
 			if (dynamicReportQuery.requiredJoins.containsKey(tableKey)) {		
 				TheGuruViewJoin theGuruViewJoin = dynamicReportQuery.requiredJoins.get(tableKey);
 				if (theGuruViewJoin.getJoinType().isEmpty()) {
 					if (dynamicReportQuery.fromClause.isEmpty())
 						dynamicReportQuery.fromClause = "FROM " + theGuruViewJoin.getJoinTable() + " AS " + dynamicReportQuery.tableAliases.get(tableKey);
 					else
 						dynamicReportQuery.fromClause += dynamicReportQuery.crlf +  ", " + theGuruViewJoin.getJoinTable() + " AS " 
 							+ dynamicReportQuery.tableAliases.get(tableKey);
 					// Add to where clause
 					if (theGuruViewJoin.getWhereClause() != null && !theGuruViewJoin.getWhereClause().isEmpty()) {
 						if (dynamicReportQuery.addWhereAnd)
 							dynamicReportQuery.joinWhereClause += dynamicReportQuery.crlf + "AND ";
 						else
 							dynamicReportQuery.addWhereAnd = true;
 						dynamicReportQuery.joinWhereClause += "(";
 						dynamicReportQuery.joinWhereClause += theGuruViewJoin.getWhereClause().replace(theGuruViewJoin.getJoinTableAlias() + ".", 
 								dynamicReportQuery.tableAliases.get(tableKey) + ".");
 						dynamicReportQuery.joinWhereClause += ")";
 					}					
 				} else {
 					String joinCriteria = theGuruViewJoin.getJoinCriteria();
 					//joinCriteria = processJoinCriteria(tableKey, joinCriteria, theGuruViewJoin, allViews, allJoins, allJoinTableKeysInOrder, joinTableKeys, viewTableKeys, tableAliases);
 					joinCriteria = processJoinCriteria(tableKey, joinCriteria, theGuruViewJoin, dynamicReportQuery);
 					dynamicReportQuery.fromClause += dynamicReportQuery.crlf + theGuruViewJoin.getJoinType() + " JOIN " + theGuruViewJoin.getJoinTable() 
 						+ " AS " + dynamicReportQuery.tableAliases.get(tableKey) + " ON " + joinCriteria;
 					// Add additional criteria to join clause
 					if (theGuruViewJoin.getWhereClause() != null && !theGuruViewJoin.getWhereClause().isEmpty()) {
 					
 						dynamicReportQuery.fromClause += " AND (";
 						dynamicReportQuery.fromClause += theGuruViewJoin.getWhereClause().replace(theGuruViewJoin.getJoinTableAlias() + ".", 
 								dynamicReportQuery.tableAliases.get(tableKey) + ".");							
 						dynamicReportQuery.fromClause += ")";
 					}
 				}
 			}
 		}
 		
 		// Add Top for SQL Server queries
 		if (getReportWizard().getDataSubSource().getDatabaseType() == ReportDatabaseType.SQLSERVER) {
 			if (preview) {
 				if (getReportWizard().getRowCount() == -1 || getReportWizard().getRowCount() > 2000)
 					dynamicReportQuery.selectClause += " TOP 2000";
 				else
 					dynamicReportQuery.selectClause += " TOP " + getReportWizard().getRowCount();
 			} else if (getReportWizard().getRowCount() > 0)
 				dynamicReportQuery.selectClause += " TOP " + getReportWizard().getRowCount();
 		}
 		
 		// Add fields
 		if (getReportWizard().getReportType().compareToIgnoreCase("matrix") == 0)
 		{
 			dynamicReportQuery.selectClause += buildSelectFieldsForMatrix(dynamicReportQuery) + dynamicReportQuery.crlf;
 		} else {
 			dynamicReportQuery.selectClause += buildSelectFieldsForNonMatrix(dynamicReportQuery) + dynamicReportQuery.crlf;
 		}		
 
 		// Add where clause
 		//String whereClause = "";
 		if (dynamicReportQuery.joinWhereClause.equals("WHERE (" + dynamicReportQuery.crlf)) {
 			dynamicReportQuery.joinWhereClause = "";
 			dynamicReportQuery.whereClause = "WHERE (" + dynamicReportQuery.crlf;
 		} else {
 			dynamicReportQuery.joinWhereClause += dynamicReportQuery.crlf;
 			dynamicReportQuery.whereClause = "AND (" + dynamicReportQuery.crlf;
 		}
 		
 		// very first criteria doesn't need an and
 		boolean afterGroup = true;
 		int index = 0;
 		
 		// tracks whether the filters contain actual criteria or not
 		boolean hasCriteria = false;
 		
 		for (ReportFilter reportFilter : getReportWizard().getReportFilters()) {
 			index++;
 			if (reportFilter == null) continue; // this is an empty filter
 			if (reportFilter.getFilterType() == 1 && reportFilter.getReportStandardFilter().getFieldId() == -1) continue; // this is an empty filter
 			if (reportFilter.getFilterType() == 2 && reportFilter.getReportCustomFilter().getCustomFilterDefinitionId() <= 0) continue; // this is an empty filter
 			
 			if (dynamicReportQuery.addWhereAnd) {
 				if (!afterGroup && reportFilter.getFilterType() != 4) {
 					// Do not add And or Or the first record after a group, or on end groups
 					if (reportFilter.getOperator() == 0)
 						dynamicReportQuery.whereClause += "AND";
 					else if (reportFilter.getOperator() == 1)
 						dynamicReportQuery.whereClause += "OR";
 				}
 			} else {
 				dynamicReportQuery.addWhereAnd = true;
 			}
 			
 			if (reportFilter.getOperatorNot() == 1)
 				dynamicReportQuery.whereClause += " NOT";
 			if (reportFilter.getFilterType() == 3) {
 				afterGroup = true;
 				dynamicReportQuery.whereClause += "(" + dynamicReportQuery.crlf;
 			} else if (reportFilter.getFilterType() == 4) {
 				afterGroup = false;
 				dynamicReportQuery.whereClause += ")" + dynamicReportQuery.crlf;
 			} else if (reportFilter.getFilterType() == 1) {
 				hasCriteria = true;
 				afterGroup = false;
 				dynamicReportQuery.whereClause += buildStandardFilterWhereClause(reportFilter.getReportStandardFilter(), index, dynamicReportQuery) + dynamicReportQuery.crlf;
 			} else if (reportFilter.getFilterType() == 2) {
 				hasCriteria = true;
 				afterGroup = false;
 				dynamicReportQuery.whereClause += buildCustomFilterWhereClause(reportFilter.getReportCustomFilter()) + dynamicReportQuery.crlf;
 			}	
 		}
 				
 		dynamicReportQuery.whereClause += ")" + dynamicReportQuery.crlf + ")" + dynamicReportQuery.crlf;
 		if (!hasCriteria)
 			if (!dynamicReportQuery.joinWhereClause.isEmpty())
 				dynamicReportQuery.whereClause = ")" + dynamicReportQuery.crlf;
 			else
 				dynamicReportQuery.whereClause = "";
 
 		// Build Order By clause
 		String includeOrderBy = System.getProperty("theguru.preview.includeorderbyclause");
 		if (includeOrderBy == null || !includeOrderBy.equalsIgnoreCase("false"))
 		{
 			//dynamicReportQuery.orderByClause = "ORDER BY" + dynamicReportQuery.crlf;
 			if (getReportWizard().getReportType().compareTo("matrix") == 0) {
 				// Matrix Reports
 				dynamicReportQuery.orderByClause = getOrderByClauseForMatrix().replace(dynamicReportQuery.crlf, "") + dynamicReportQuery.crlf;
 			} else {
 				// Tabular / Summary Reports
 				dynamicReportQuery.orderByClause = getOrderByClauseForSummary().replace(dynamicReportQuery.crlf, "") + dynamicReportQuery.crlf;
 			}
 			if (dynamicReportQuery.orderByClause.equals(dynamicReportQuery.crlf))
 				dynamicReportQuery.orderByClause = "";
 		}
 		
 		String query = dynamicReportQuery.selectClause
 			+ dynamicReportQuery.fromClause + (dynamicReportQuery.fromClause.endsWith(dynamicReportQuery.crlf) ? "" : dynamicReportQuery.crlf)
 			+ dynamicReportQuery.joinWhereClause
 			+ dynamicReportQuery.whereClause 
 			+ dynamicReportQuery.orderByClause;
 
 		// Add Limit for MySQL queries
 		if (getReportWizard().getDataSubSource().getDatabaseType() == ReportDatabaseType.MYSQL) {
 			if (preview) {
 				if (getReportWizard().getRowCount() == -1 || getReportWizard().getRowCount() > 2000)
 					query += "LIMIT 0, 2000";
 				else
 					query += "LIMIT 0," + getReportWizard().getRowCount();
 			} else if (getReportWizard().getRowCount() > 0)
 				query += "LIMIT 0," + getReportWizard().getRowCount();
 		}
 
 		query += ";";
 		
 		return query;
 	}
 
 	private void populateTableKeyForReportFieldId(
 			DynamicReportQuery dynamicReportQuery, TheGuruView theGuruView,
 			Long reportFieldId) {
 		ReportField reportField = getReportFieldService().find(reportFieldId);
 		String tableKey = getTableKeyForReportField(reportField, theGuruView);
 		if (!dynamicReportQuery.fieldTableKeys.containsKey(reportField.getId()))
 			dynamicReportQuery.fieldTableKeys.put(reportField.getId(), tableKey);
 		if (!dynamicReportQuery.tableKeysFromFields.contains(tableKey))
 			dynamicReportQuery.tableKeysFromFields.add(tableKey);
 	}
 
 	private String getSourceColumnNameFromReportField(ReportField reportField, DynamicReportQuery dynamicReportQuery) {
 		String customTableTableKey = "";
 		String customTableTableAlias = "";
 		String constituentTableTableKey = "";
 		String constituentTableTableAlias = "";
 		String picklistTableTableKey = "";
 		String picklistTableTableAlias = "";
 		String picklistItemTableTableKey = "";
 		String picklistItemTableTableAlias = "";
 		
 		if (reportField.getCustomFieldEntityType() != null && !reportField.getCustomFieldEntityType().isEmpty()
 				&& (reportField.getPicklistCustomField() == null || !reportField.getPicklistCustomField())) {
 			customTableTableKey = reportField.getCustomFieldEntityType() + "_" + reportField.getCustomFieldFieldName() + "_" + reportField.getCustomFieldEntityId()
 				+ "_" + dynamicReportQuery.tableAliases.get(dynamicReportQuery.fieldTableKeys.get(reportField.getId()));
 			customTableTableAlias = dynamicReportQuery.tableAliases.get(customTableTableKey);
 			if (customTableTableAlias == null) {
 				if (!dynamicReportQuery.fromClause.endsWith(dynamicReportQuery.crlf))
 					dynamicReportQuery.fromClause += dynamicReportQuery.crlf; 
 				customTableTableAlias = "`T" + Integer.toString(dynamicReportQuery.tableAliases.size()) + "`"; 
 				dynamicReportQuery.tableAliases.put(customTableTableKey, customTableTableAlias);
 				
 				String customFieldName = reportField.getCustomFieldFieldName();
 				
 				if (customFieldName.startsWith("CASE")) {
 					while (customFieldName.contains("${") && customFieldName.contains("}.")) {
 						String tableName = customFieldName.substring(customFieldName.indexOf("${"), customFieldName.indexOf("}.") + 2);
 						customFieldName = customFieldName.replace(tableName, dynamicReportQuery.tableAliases.get(dynamicReportQuery.fieldTableKeys.get(reportField.getId())) + ".");
 					}
 				} else if (customFieldName != null && !customFieldName.isEmpty() && !customFieldName.contains("${OR}")) {
 					customFieldName = "'" + customFieldName + "'";
 				}
 				
 				dynamicReportQuery.fromClause += "LEFT JOIN CUSTOM_FIELD AS " + customTableTableAlias + " ON " 
 					+ customTableTableAlias + ".ENTITY_TYPE = '" + reportField.getCustomFieldEntityType() + "'"
 					+ " AND " + customTableTableAlias + ".ENTITY_ID = " + dynamicReportQuery.tableAliases.get(dynamicReportQuery.fieldTableKeys.get(reportField.getId())) 
 					+ "." + reportField.getCustomFieldEntityId();
 				if (reportField.getCustomFieldFieldName() != null && !reportField.getCustomFieldFieldName().isEmpty())
 					if (customFieldName.contains("${OR}")) {
 						dynamicReportQuery.fromClause += " AND (";
 						String[] customFieldNames = customFieldName.split(" \\$\\{OR\\} ");
 						int index = 0;
 						for (String customFieldNameSplit : customFieldNames) {
 							if (index > 0)
 								dynamicReportQuery.fromClause += " OR ";
 							dynamicReportQuery.fromClause += customTableTableAlias + ".FIELD_NAME = '" + customFieldNameSplit + "'";
 							index++;
 						}
 						dynamicReportQuery.fromClause += ")";
 					} else					
 						dynamicReportQuery.fromClause += " AND " + customTableTableAlias + ".FIELD_NAME = " + customFieldName;
 				dynamicReportQuery.fromClause += dynamicReportQuery.crlf;
 			}
 		}
 
 		if (reportField.getConstituentJoinField() != null && !reportField.getConstituentJoinField().isEmpty()) {
 			constituentTableTableKey = customTableTableKey + reportField.getConstituentJoinField() + "_" 
 				+ dynamicReportQuery.tableAliases.get(dynamicReportQuery.fieldTableKeys.get(reportField.getId()));
 			constituentTableTableAlias = dynamicReportQuery.tableAliases.get(constituentTableTableKey);
 			if (constituentTableTableAlias == null) {
 				if (!dynamicReportQuery.fromClause.endsWith(dynamicReportQuery.crlf))
 					dynamicReportQuery.fromClause += dynamicReportQuery.crlf;
 				constituentTableTableAlias = "`T" + Integer.toString(dynamicReportQuery.tableAliases.size()) + "`"; 
 				dynamicReportQuery.tableAliases.put(constituentTableTableKey, constituentTableTableAlias);
 				if (!customTableTableKey.isEmpty()) {
 					// constituent is linked based on a custom field
 					dynamicReportQuery.fromClause += "LEFT JOIN CONSTITUENT AS " + constituentTableTableAlias + " ON " 
 						+ constituentTableTableAlias + ".CONSTITUENT_ID = "
 						+ reportField.getConstituentJoinField().replace("${CUSTOM_FIELD}", customTableTableAlias)
 						+ dynamicReportQuery.crlf;
 				} else {
 					// constituent is linked by some other field 
 					dynamicReportQuery.fromClause += "LEFT JOIN CONSTITUENT AS " + constituentTableTableAlias + " ON " + constituentTableTableAlias + ".CONSTITUENT_ID = "
 					+ dynamicReportQuery.tableAliases.get(dynamicReportQuery.fieldTableKeys.get(reportField.getId())) + "." + reportField.getConstituentJoinField()
 					+ dynamicReportQuery.crlf;
 				}					
 			}
 		}
 		
 		if (reportField.getPicklistNameId() != null && !reportField.getPicklistNameId().isEmpty()) {
 			picklistTableTableKey = reportField.getPicklistNameId() + "_" + dynamicReportQuery.tableAliases.get(dynamicReportQuery.fieldTableKeys.get(reportField.getId()));
 			picklistTableTableAlias = dynamicReportQuery.tableAliases.get(picklistTableTableKey);
 			picklistItemTableTableKey = reportField.getPicklistNameId() + "_" + reportField.getPicklistItemName() + "_" 
 				+ dynamicReportQuery.tableAliases.get(dynamicReportQuery.fieldTableKeys.get(reportField.getId()));
 			picklistItemTableTableAlias = dynamicReportQuery.tableAliases.get(picklistItemTableTableKey);
 			if (picklistTableTableAlias == null) {
 				if (!dynamicReportQuery.fromClause.endsWith(dynamicReportQuery.crlf))
 					dynamicReportQuery.fromClause += dynamicReportQuery.crlf;
 				picklistTableTableAlias = "`T" + Integer.toString(dynamicReportQuery.tableAliases.size()) + "`"; 
 				dynamicReportQuery.tableAliases.put(picklistTableTableKey, picklistTableTableAlias);
 				picklistItemTableTableAlias = "`T" + Integer.toString(dynamicReportQuery.tableAliases.size()) + "`";
 				dynamicReportQuery.tableAliases.put(picklistItemTableTableKey, picklistItemTableTableAlias);
 
 				String picklistNameId = reportField.getPicklistNameId();
 				if (picklistNameId.startsWith("CASE")) {
 					while (picklistNameId.contains("${") && picklistNameId.contains("}.")) {
 						String tableName = picklistNameId.substring(picklistNameId.indexOf("${"), picklistNameId.indexOf("}.") + 2);
 						picklistNameId = picklistNameId.replace(tableName, dynamicReportQuery.tableAliases.get(dynamicReportQuery.fieldTableKeys.get(reportField.getId())) + ".");
 					}
 				} else {
 					picklistNameId = "'" + picklistNameId + "'";
 				}
 
 				String picklistItemName = reportField.getPicklistItemName();
 				if (customTableTableAlias != null && !customTableTableAlias.isEmpty())
 					picklistItemName = picklistItemName.replace("${CUSTOM_FIELD}", customTableTableAlias);				
 				if (picklistItemName.startsWith("CASE")) {
 					while (picklistItemName.contains("${") && picklistItemName.contains("}.")) {
 						String tableName = picklistItemName.substring(picklistItemName.indexOf("${"), picklistItemName.indexOf("}.") + 2);
 						picklistItemName = picklistItemName.replace(tableName, dynamicReportQuery.tableAliases.get(dynamicReportQuery.fieldTableKeys.get(reportField.getId())) + ".");
 					}
 				} else if (picklistItemName.equals(reportField.getPicklistItemName()))
 					picklistItemName = dynamicReportQuery.tableAliases.get(dynamicReportQuery.fieldTableKeys.get(reportField.getId())) + "." + picklistItemName; 
 				
 				dynamicReportQuery.fromClause += "LEFT JOIN PICKLIST AS " + picklistTableTableAlias + " ON "
					+ picklistTableTableAlias + ".PICKLIST_NAME_ID = " + picklistNameId
					+ dynamicReportQuery.crlf;						
 				dynamicReportQuery.fromClause += "LEFT JOIN PICKLIST_ITEM AS " + picklistItemTableTableAlias + " ON "
 					+ picklistItemTableTableAlias + ".PICKLIST_ID = "
 					+ picklistTableTableAlias + ".PICKLIST_ID AND " + picklistItemTableTableAlias + ".ITEM_NAME = "
 					+ picklistItemName
 					+ dynamicReportQuery.crlf;
 			}
 			
 			if (reportField.getPicklistCustomField() != null && reportField.getPicklistCustomField()) {
 				if (reportField.getCustomFieldEntityType() != null && !reportField.getCustomFieldEntityType().isEmpty()
 						&& reportField.getPicklistCustomField()) {
 					customTableTableKey = reportField.getCustomFieldEntityType() + "_" + reportField.getCustomFieldFieldName() + "_" + reportField.getCustomFieldEntityId()
 						+ "_" + dynamicReportQuery.tableAliases.get(dynamicReportQuery.fieldTableKeys.get(reportField.getId()));
 					customTableTableAlias = dynamicReportQuery.tableAliases.get(customTableTableKey);
 					if (customTableTableAlias == null) {
 						if (!dynamicReportQuery.fromClause.endsWith(dynamicReportQuery.crlf))
 							dynamicReportQuery.fromClause += dynamicReportQuery.crlf; 
 						customTableTableAlias = "`T" + Integer.toString(dynamicReportQuery.tableAliases.size()) + "`"; 
 						dynamicReportQuery.tableAliases.put(customTableTableKey, customTableTableAlias);
 						
 						String customFieldName = reportField.getCustomFieldFieldName();
 						
 						if (customFieldName.startsWith("CASE")) {
 							while (customFieldName.contains("${") && customFieldName.contains("}.")) {
 								String tableName = customFieldName.substring(customFieldName.indexOf("${"), customFieldName.indexOf("}.") + 2);
 								customFieldName = customFieldName.replace(tableName, dynamicReportQuery.tableAliases.get(dynamicReportQuery.fieldTableKeys.get(reportField.getId())) + ".");
 							}
 						} else if (customFieldName != null && !customFieldName.isEmpty() && !customFieldName.contains("${OR}")) {
 							customFieldName = "'" + customFieldName + "'";
 						}
 						
 						dynamicReportQuery.fromClause += "LEFT JOIN CUSTOM_FIELD AS " + customTableTableAlias + " ON " 
 							+ customTableTableAlias + ".ENTITY_TYPE = '" + reportField.getCustomFieldEntityType() + "'"
 							+ " AND " + customTableTableAlias + ".ENTITY_ID = " + picklistItemTableTableAlias
 							+ "." + reportField.getCustomFieldEntityId();
 						if (reportField.getCustomFieldFieldName() != null && !reportField.getCustomFieldFieldName().isEmpty())
 							if (customFieldName.contains("${OR}")) {
 								dynamicReportQuery.fromClause += " AND (";
 								String[] customFieldNames = customFieldName.split(" \\$\\{OR\\} ");
 								int index = 0;
 								for (String customFieldNameSplit : customFieldNames) {
 									if (index > 0)
 										dynamicReportQuery.fromClause += " OR ";
 									dynamicReportQuery.fromClause += customTableTableAlias + ".FIELD_NAME = '" + customFieldNameSplit + "'";
 									index++;
 								}
 								dynamicReportQuery.fromClause += ")";
 							} else					
 								dynamicReportQuery.fromClause += " AND " + customTableTableAlias + ".FIELD_NAME = " + customFieldName;
 						dynamicReportQuery.fromClause += dynamicReportQuery.crlf;
 					}
 				}
 			}
 		}
 		
 		String sourceColumnName = reportField.getSourceColumnName();
 		if (sourceColumnName.contains("${CUSTOM_FIELD}")) {
 			sourceColumnName = sourceColumnName.replace("${CUSTOM_FIELD}", customTableTableAlias);
 		}
 		
 		if (sourceColumnName.contains("${CONSTITUENT_TABLE}")) {
 			sourceColumnName = sourceColumnName.replace("${CONSTITUENT_TABLE}", constituentTableTableAlias);
 		}
 		
 		if (reportField.getPicklistNameId() != null && !reportField.getPicklistNameId().isEmpty() && sourceColumnName.contains("${PICKLIST_ITEM}")) {
 			sourceColumnName = sourceColumnName.replace("${PICKLIST_ITEM}", picklistItemTableTableAlias);
 		}
 		
 		if (sourceColumnName.contains("${") && sourceColumnName.contains("}.")) {
 			String tableName = sourceColumnName.substring(sourceColumnName.indexOf("${"), sourceColumnName.indexOf("}.") + 2);
 			sourceColumnName = sourceColumnName.replace(tableName, dynamicReportQuery.tableAliases.get(dynamicReportQuery.fieldTableKeys.get(reportField.getId())) + ".");
 		}
 		return sourceColumnName;
 	}
 
 	private String customReplace(String value, String valueToReplace, String replacementValue) {
 		String result = value;
 		if (result.startsWith(valueToReplace))
 			result = replacementValue + result.substring(valueToReplace.length());
 		result = result.replace(" " + valueToReplace, " " + replacementValue);		
 		result = result.replace("(" + valueToReplace, "(" + replacementValue);
 		return result;
 	}
 
 	private String processJoinCriteria(String tableKey, String joinCriteria, TheGuruViewJoin theGuruViewJoin, DynamicReportQuery dynamicReportQuery) {
 		joinCriteria = customReplace(joinCriteria, theGuruViewJoin.getJoinTableAlias() + ".", dynamicReportQuery.tableAliases.get(tableKey) + ".");
 		// it's from a view - replace the column prefixes
 		if (theGuruViewJoin.getId() < 0)
 			joinCriteria = joinCriteria.replace("." + theGuruViewJoin.getJoinTableColumnPrefix(), ".");
 		TheGuruView theGuruView;
 		TheGuruViewJoin secondaryTheGuruViewJoin;
 		
 		String[] viewIds = tableKey.split("_");
 		for (String viewIdString : viewIds) {
 			Long viewId;
 			try {				
 				viewId = Long.parseLong(viewIdString);
 			} catch (NumberFormatException exception) {
 				viewId = null;
 			}
 				
 			if (viewId == null)
 				break;
 			else if (viewId.equals(0l)) {
 				// Primary view
 				theGuruView = getTheGuruViewService().readTheGuruViewByViewName(getReportWizard().getDataSubSource().getViewName());
 				List<String> primaryTableAliasesToReplace = new ArrayList<String>();
 				String primaryTableAlias = theGuruView.getPrimaryTableAlias();
 				String newTableAlias = dynamicReportQuery.tableAliases.get(dynamicReportQuery.joinTableKeys.get(theGuruView.getId() * -1));
 				if (newTableAlias != null)
 					joinCriteria = customReplace(joinCriteria, primaryTableAlias + ".", newTableAlias + ".");
 				else
 					primaryTableAliasesToReplace.add(primaryTableAlias);
 				while (theGuruView.isPrimaryTableIsView()) {
 					theGuruView = getTheGuruViewService().readTheGuruViewByViewName(theGuruView.getPrimaryTable());
 					primaryTableAlias = theGuruView.getPrimaryTableAlias();
 					newTableAlias = dynamicReportQuery.tableAliases.get(dynamicReportQuery.joinTableKeys.get(theGuruView.getId() * -1));
 					if (newTableAlias != null) {
 						joinCriteria = customReplace(joinCriteria, primaryTableAlias + ".", newTableAlias + ".");
 						for (String primaryTableAliasToReplace : primaryTableAliasesToReplace) {
 							joinCriteria = customReplace(joinCriteria, primaryTableAliasToReplace + "." + theGuruView.getPrimaryTableColumnPrefix(), newTableAlias + ".");
 						}
 						primaryTableAliasesToReplace.clear();
 					} else
 						primaryTableAliasesToReplace.add(primaryTableAlias);					
 				}
 			} else if (viewId > 0l) {
 				secondaryTheGuruViewJoin = theGuruViewJoinService.find(viewId);
 				joinCriteria = customReplace(joinCriteria, secondaryTheGuruViewJoin.getJoinTableAlias() + ".", dynamicReportQuery.tableAliases.get(tableKey) + ".");
 
 				List<String> primaryTableAliasesToReplace = new ArrayList<String>();
 				if (secondaryTheGuruViewJoin.isJoinTableIsView()) {
 					theGuruView = getTheGuruViewService().readTheGuruViewByViewName(secondaryTheGuruViewJoin.getJoinTable());
 					String primaryTableAlias = theGuruView.getPrimaryTableAlias();
 					String newTableAlias = dynamicReportQuery.tableAliases.get(dynamicReportQuery.joinTableKeys.get(theGuruView.getId() * -1));
 					if (newTableAlias != null)
 						joinCriteria = customReplace(joinCriteria, primaryTableAlias + ".", newTableAlias + ".");
 					else
 						primaryTableAliasesToReplace.add(primaryTableAlias);					
 					while (theGuruView.isPrimaryTableIsView()) {
 						theGuruView = getTheGuruViewService().readTheGuruViewByViewName(theGuruView.getPrimaryTable());						
 						primaryTableAlias = theGuruView.getPrimaryTableAlias();
 						newTableAlias = dynamicReportQuery.tableAliases.get(dynamicReportQuery.joinTableKeys.get(theGuruView.getId() * -1));
 						if (newTableAlias != null) {
 							boolean replaceColumnPrefix = true;
 
 							if (theGuruViewJoin.getRequiresJoinId() == 0) {
 								if (!theGuruViewService.find(theGuruViewJoin.getViewId()).isPrimaryTableIsView())
 									replaceColumnPrefix = false;
 							} else if (theGuruViewJoin.getRequiresJoinId() > 0) {
 								if (!theGuruViewJoinService.find(theGuruViewJoin.getRequiresJoinId()).isJoinTableIsView())
 									replaceColumnPrefix = false;
 							} else if (theGuruViewJoin.getRequiresJoinId() < 0) {
 								if (!theGuruViewService.find(theGuruViewJoin.getRequiresJoinId() * -1).isPrimaryTableIsView())
 									replaceColumnPrefix = false;									
 							}
 								
 							if (replaceColumnPrefix)
 								joinCriteria = customReplace(joinCriteria, primaryTableAlias + "." + theGuruView.getPrimaryTableColumnPrefix(), newTableAlias + ".");
 							else
 								joinCriteria = customReplace(joinCriteria, primaryTableAlias + ".", newTableAlias + ".");
 							for (String primaryTableAliasToReplace : primaryTableAliasesToReplace) {
 								if (replaceColumnPrefix)
 									joinCriteria = customReplace(joinCriteria, primaryTableAliasToReplace + "." + theGuruView.getPrimaryTableColumnPrefix(), newTableAlias + ".");
 								else
 									joinCriteria = customReplace(joinCriteria, primaryTableAliasToReplace + ".", newTableAlias + ".");
 							}
 							primaryTableAliasesToReplace.clear();
 						} else {
 							primaryTableAliasesToReplace.add(primaryTableAlias);
 						}
 					}
 				}
 			} else if (viewId < 0l) {
 				theGuruView = getTheGuruViewService().find(viewId);
 				List<String> primaryTableAliasesToReplace = new ArrayList<String>();
 				String primaryTableAlias = theGuruView.getPrimaryTableAlias();
 				String newTableAlias = dynamicReportQuery.tableAliases.get(dynamicReportQuery.joinTableKeys.get(theGuruView.getId() * -1));
 				if (newTableAlias != null)
 					joinCriteria = customReplace(joinCriteria, primaryTableAlias + ".", newTableAlias + ".");
 				else
 					primaryTableAliasesToReplace.add(primaryTableAlias);
 				while (theGuruView.isPrimaryTableIsView()) {
 					theGuruView = getTheGuruViewService().readTheGuruViewByViewName(theGuruView.getPrimaryTable());
 					primaryTableAlias = theGuruView.getPrimaryTableAlias();
 					newTableAlias = dynamicReportQuery.tableAliases.get(dynamicReportQuery.joinTableKeys.get(theGuruView.getId() * -1));
 					if (newTableAlias != null) {
 						joinCriteria = customReplace(joinCriteria, primaryTableAlias + ".", newTableAlias + ".");
 						for (String primaryTableAliasToReplace : primaryTableAliasesToReplace) {
 							joinCriteria = customReplace(joinCriteria, primaryTableAliasToReplace + "." + theGuruView.getPrimaryTableColumnPrefix(), newTableAlias + ".");
 						}
 						primaryTableAliasesToReplace.clear();
 					} else
 						primaryTableAliasesToReplace.add(primaryTableAlias);					
 				}
 			}			
 		}
 
 		// process required joins that may not have been handled by the table key order
 		if (theGuruViewJoin.getRequiresJoinId() == 0l && !tableKey.startsWith("0")) {
 			theGuruView = getTheGuruViewService().readTheGuruViewByViewName(getReportWizard().getDataSubSource().getViewName());
 			String primaryTableAlias = theGuruView.getPrimaryTableAlias();
 			String primaryTableColumnPrefix = "";
 			while (theGuruView.isPrimaryTableIsView()) {
 				theGuruView = getTheGuruViewService().readTheGuruViewByViewName(theGuruView.getPrimaryTable());
 				primaryTableColumnPrefix += theGuruView.getPrimaryTableColumnPrefix();
 			}
 			if (theGuruViewJoin.isJoinTableIsView())
 				joinCriteria = customReplace(joinCriteria, primaryTableAlias + "." + theGuruView.getPrimaryTableColumnPrefix(),
 					dynamicReportQuery.tableAliases.get(dynamicReportQuery.joinTableKeys.get(theGuruView.getId() * -1)) + ".");
 			else
 				joinCriteria = customReplace(joinCriteria, primaryTableAlias + "." + primaryTableColumnPrefix,
 						dynamicReportQuery.tableAliases.get(dynamicReportQuery.joinTableKeys.get(theGuruView.getId() * -1)) + ".");
 		} else if (theGuruViewJoin.getRequiresJoinId() > 0l) {
 			secondaryTheGuruViewJoin = theGuruViewJoinService.find(theGuruViewJoin.getRequiresJoinId());
 			if (secondaryTheGuruViewJoin.isJoinTableIsView()) {
 				theGuruView = theGuruViewService.readTheGuruViewByViewName(secondaryTheGuruViewJoin.getJoinTable());
 				while (theGuruView.isPrimaryTableIsView())
 					theGuruView = theGuruViewService.readTheGuruViewByViewName(theGuruView.getPrimaryTable());
 				joinCriteria = customReplace(joinCriteria, secondaryTheGuruViewJoin.getJoinTableAlias() + "." + theGuruView.getPrimaryTableColumnPrefix(),
 						dynamicReportQuery.tableAliases.get(dynamicReportQuery.joinTableKeys.get(theGuruView.getId() * -1)) + ".");
 			} else {
 				joinCriteria = customReplace(joinCriteria, secondaryTheGuruViewJoin.getJoinTableAlias() + ".",
 						dynamicReportQuery.tableAliases.get(dynamicReportQuery.joinTableKeys.get(secondaryTheGuruViewJoin.getId())) + ".");
 			}
 		} else if (theGuruViewJoin.getRequiresJoinId() < 0l) {
 			theGuruView = getTheGuruViewService().find(theGuruViewJoin.getRequiresJoinId() * -1);
 			String primaryTableAlias = theGuruView.getPrimaryTableAlias();
 			String primaryTableColumnPrefix = "";
 			while (theGuruView.isPrimaryTableIsView()) {
 				theGuruView = getTheGuruViewService().readTheGuruViewByViewName(theGuruView.getPrimaryTable());
 				primaryTableColumnPrefix += theGuruView.getPrimaryTableColumnPrefix();
 			}
 			if (theGuruViewJoin.isJoinTableIsView())
 				joinCriteria = customReplace(joinCriteria, primaryTableAlias + "." + theGuruView.getPrimaryTableColumnPrefix(),
 					dynamicReportQuery.tableAliases.get(dynamicReportQuery.joinTableKeys.get(theGuruView.getId() * -1)) + ".");
 			else
 				joinCriteria = customReplace(joinCriteria, primaryTableAlias + "." + primaryTableColumnPrefix,
 						dynamicReportQuery.tableAliases.get(dynamicReportQuery.joinTableKeys.get(theGuruView.getId() * -1)) + ".");
 		}
 		return joinCriteria;
 	}
 
 	private void buildRequiredJoins(DynamicReportQuery dynamicReportQuery) { 
 		// iterate through the view (going through the joins for the secondary view if it's a view), then through each of the joins and any tables they link to
 		// generate a table key for each table possibly used in the view, and if the table key is referenced in the tableKeys, add the join info and any parent join info
 		String viewName = getReportWizard().getDataSubSource().getViewName();
 		TheGuruView theGuruView = getTheGuruViewService().readTheGuruViewByViewName(viewName);
 		buildAllJoinsForTheGuruView(theGuruView, dynamicReportQuery, "", "", "", 0l, "");
 		
 		long baseTheGuruViewId = theGuruView.getId();
 		String baseTheGuruViewTableKey = "";
 		while (theGuruView.isPrimaryTableIsView()) {
 			theGuruView = getTheGuruViewService().readTheGuruViewByViewName(theGuruView.getPrimaryTable());
 			baseTheGuruViewId = theGuruView.getId();
 		}
 		baseTheGuruViewTableKey = dynamicReportQuery.viewTableKeys.get(baseTheGuruViewId);
 		
 		for (String tableKey : dynamicReportQuery.allJoins.keySet()) {
 			if (dynamicReportQuery.tableKeysFromFields.contains(tableKey) || baseTheGuruViewTableKey.equals(tableKey)) {
 				TheGuruViewJoin theGuruViewJoin = dynamicReportQuery.allJoins.get(tableKey);
 				dynamicReportQuery.requiredJoins.put(tableKey, theGuruViewJoin);				
 				while (theGuruViewJoin.getRequiresJoinId() > 0) {
 					Long requiredId = theGuruViewJoin.getRequiresJoinId();
 					TheGuruViewJoin requiredJoin = theGuruViewJoinService.find(requiredId);
 					if (requiredJoin.isJoinTableIsView()) {
 						TheGuruView requiredView = theGuruViewService.readTheGuruViewByViewName(requiredJoin.getJoinTable());
 						while (requiredView.isPrimaryTableIsView())
 							requiredView = theGuruViewService.readTheGuruViewByViewName(requiredView.getPrimaryTable());
 						requiredId = requiredView.getId() * -1;
 					}
 					
 					String requiredTableKey = dynamicReportQuery.joinTableKeys.get(requiredId);
 					theGuruViewJoin = dynamicReportQuery.allJoins.get(requiredTableKey);
 					if (!dynamicReportQuery.requiredJoins.containsKey(requiredTableKey))
 						dynamicReportQuery.requiredJoins.put(requiredTableKey, theGuruViewJoin);
 				}
 				while (theGuruViewJoin.getRequiresJoinId() == 0 && (theGuruViewJoin.getId() * -1) != theGuruViewJoin.getViewId()) {
 					Long requiredId = theGuruViewJoin.getViewId();				
 					TheGuruView requiredView = theGuruViewService.find(requiredId);
 					if (requiredView.isPrimaryTableIsView()) {
 						requiredView = theGuruViewService.readTheGuruViewByViewName(requiredView.getPrimaryTable());
 						while (requiredView.isPrimaryTableIsView())
 							requiredView = theGuruViewService.readTheGuruViewByViewName(requiredView.getPrimaryTable());
 						requiredId = requiredView.getId();
 					}
 					
 					String requiredTableKey = dynamicReportQuery.joinTableKeys.get(requiredId * -1);
 					theGuruViewJoin = dynamicReportQuery.allJoins.get(requiredTableKey);
 					if (!dynamicReportQuery.requiredJoins.containsKey(requiredTableKey))
 						dynamicReportQuery.requiredJoins.put(requiredTableKey, theGuruViewJoin);
 					
 					while (theGuruViewJoin.getRequiresJoinId() != 0) {
 						if (theGuruViewJoin.getRequiresJoinId() > 0) {
 							requiredId = theGuruViewJoin.getRequiresJoinId();				
 							TheGuruViewJoin requiredJoin = theGuruViewJoinService.find(requiredId);
 							if (requiredJoin.isJoinTableIsView()) {
 								requiredView = theGuruViewService.readTheGuruViewByViewName(requiredJoin.getJoinTable());
 								while (requiredView.isPrimaryTableIsView())
 									requiredView = theGuruViewService.readTheGuruViewByViewName(requiredView.getPrimaryTable());
 								requiredId = requiredView.getId() * -1;
 							}
 							
 							requiredTableKey = dynamicReportQuery.joinTableKeys.get(requiredId);
 							theGuruViewJoin = dynamicReportQuery.allJoins.get(requiredTableKey);
 							if (!dynamicReportQuery.requiredJoins.containsKey(requiredTableKey))
 								dynamicReportQuery.requiredJoins.put(requiredTableKey, theGuruViewJoin);
 						} else if (theGuruViewJoin.getRequiresJoinId() < 0) {
 							requiredId = theGuruViewJoin.getRequiresJoinId() * -1;	
 							requiredView = theGuruViewService.find(requiredId);
 							if (requiredView.isPrimaryTableIsView()) {
 								requiredView = theGuruViewService.readTheGuruViewByViewName(requiredView.getPrimaryTable());
 								while (requiredView.isPrimaryTableIsView())
 									requiredView = theGuruViewService.readTheGuruViewByViewName(requiredView.getPrimaryTable());
 								requiredId = requiredView.getId();
 							}
 							
 							requiredTableKey = dynamicReportQuery.joinTableKeys.get(requiredId * -1);
 							theGuruViewJoin = dynamicReportQuery.allJoins.get(requiredTableKey);
 							if (!dynamicReportQuery.requiredJoins.containsKey(requiredTableKey))
 								dynamicReportQuery.requiredJoins.put(requiredTableKey, theGuruViewJoin);						
 						}
 					}
 					
 				}
 				while (theGuruViewJoin.getRequiresJoinId() < 0) {
 					Long requiredId = theGuruViewJoin.getRequiresJoinId() * -1;	
 					TheGuruView requiredView = theGuruViewService.find(requiredId);
 					if (requiredView.isPrimaryTableIsView()) {
 						requiredView = theGuruViewService.readTheGuruViewByViewName(requiredView.getPrimaryTable());
 						while (requiredView.isPrimaryTableIsView())
 							requiredView = theGuruViewService.readTheGuruViewByViewName(requiredView.getPrimaryTable());
 						requiredId = requiredView.getId();
 					}
 					
 					String requiredTableKey = dynamicReportQuery.joinTableKeys.get(requiredId * -1);
 					theGuruViewJoin = dynamicReportQuery.allJoins.get(requiredTableKey);
 					if (!dynamicReportQuery.requiredJoins.containsKey(requiredTableKey))
 						dynamicReportQuery.requiredJoins.put(requiredTableKey, theGuruViewJoin);
 				}				
 			}
 		}
 	}
 
 	private void buildAllJoinsForTheGuruView(TheGuruView theGuruView, DynamicReportQuery dynamicReportQuery,
 			String ids, String joinType, String joinCriteria, Long requiresJoinId, String whereClause) {
 		if (theGuruView.isPrimaryTableIsView()) {
 			TheGuruView secondaryTheGuruView = getTheGuruViewService().readTheGuruViewByViewName(theGuruView.getPrimaryTable());
 			buildAllJoinsForTheGuruView(secondaryTheGuruView, dynamicReportQuery, ids + "0_", joinType, joinCriteria, requiresJoinId, whereClause);
 
 			buildAllSecondaryJoinsForTheGuruView(theGuruView, dynamicReportQuery, ids, joinType, joinCriteria, requiresJoinId, whereClause);
 		} else {
 			String tableKey = ids + "0_" + theGuruView.getPrimaryTable();
 			dynamicReportQuery.viewTableKeys.put(theGuruView.getId(), tableKey);
 			dynamicReportQuery.allViews.put(tableKey, theGuruView);
 			
 			String tempWhereClause = "";
 			if (whereClause != null && !whereClause.isEmpty()) {
 				tempWhereClause = "(" + whereClause + ")";
 				if (theGuruView.getWhereClause() != null && !theGuruView.getWhereClause().isEmpty()) {
 					tempWhereClause += System.getProperty("line.separator") + "AND (" + theGuruView.getWhereClause() + ")"; 
 				}
 			} else if (theGuruView.getWhereClause() != null && !theGuruView.getWhereClause().isEmpty()) {
 				tempWhereClause = theGuruView.getWhereClause();
 			}
 			
 			TheGuruViewJoin joinFromView = new TheGuruViewJoin(theGuruView.getId() * -1, theGuruView.getId(), joinType, theGuruView.getPrimaryTable(),
 					theGuruView.isPrimaryTableIsView(), theGuruView.getPrimaryTableAlias(), theGuruView.getPrimaryTableColumnPrefix(), joinCriteria,
 					theGuruView.getFieldGroupPrefix(), theGuruView.getFieldGroupOverride(), theGuruView.isIncludeAllFields(), 1, theGuruView.getParentEntityType(),
 					theGuruView.getSubFieldName(), theGuruView.getDefaultPageType(), false, requiresJoinId, 
 					tempWhereClause);
 			
 			dynamicReportQuery.allJoins.put(tableKey, joinFromView);
 			dynamicReportQuery.joinTableKeys.put(joinFromView.getId(), tableKey);
 			dynamicReportQuery.allJoinTableKeysInOrder.add(tableKey);
 			
 			buildAllSecondaryJoinsForTheGuruView(theGuruView, dynamicReportQuery, ids, joinType, joinCriteria, requiresJoinId, whereClause);					
 		}		
 	}
 
 	private void buildAllSecondaryJoinsForTheGuruView(TheGuruView theGuruView, DynamicReportQuery dynamicReportQuery,
 			String ids, String joinType, String joinCriteria, Long requiresJoinId, String whereClause) {
 		List<TheGuruViewJoin> theGuruViewJoins = theGuruViewJoinService.readTheGuruViewJoinsByViewId(theGuruView.getId());
 		for (TheGuruViewJoin theGuruViewJoin : theGuruViewJoins) {
 			if (theGuruViewJoin.isJoinTableIsView()) {
 				TheGuruView secondaryTheGuruView = getTheGuruViewService().readTheGuruViewByViewName(theGuruViewJoin.getJoinTable());
 
 				buildAllJoinsForTheGuruView(secondaryTheGuruView, dynamicReportQuery, ids + theGuruViewJoin.getId() + "_",
 					theGuruViewJoin.getJoinType(), theGuruViewJoin.getJoinCriteria(), 
 					(theGuruViewJoin.getRequiresJoinId() == 0 ? theGuruView.getId() * -1 : theGuruViewJoin.getRequiresJoinId()),
 					theGuruViewJoin.getWhereClause());
 			} else {
 				String tableKey = ids + theGuruViewJoin.getId() + "_" + theGuruViewJoin.getJoinTable();
 				dynamicReportQuery.allJoins.put(tableKey, theGuruViewJoin);
 				dynamicReportQuery.joinTableKeys.put(theGuruViewJoin.getId(), tableKey);
 				dynamicReportQuery.allJoinTableKeysInOrder.add(tableKey);
 			}
 		}
 	}
 
 	private String getTableKeyForReportField(ReportField reportField, TheGuruView theGuruView) {
 		String tableKey = "";
 		String ids = "";
 		if (reportField.getJoinId() == 0) {
 			if (theGuruView.isPrimaryTableIsView()) {
 				TheGuruView secondaryView = getTheGuruViewService().readTheGuruViewByViewName(theGuruView.getPrimaryTable());
 				while (secondaryView.isPrimaryTableIsView()) {
 					ids += "0_";
 					secondaryView = getTheGuruViewService().readTheGuruViewByViewName(secondaryView.getPrimaryTable());
 				}
 				tableKey = getTableKeyForReportFieldFromViewJoins(reportField, secondaryView, "0_" + ids);
 			} else {
 				tableKey = "0_" + theGuruView.getPrimaryTable();
 			}
 		} else {
 			TheGuruViewJoin theGuruViewJoin = getTheGuruViewJoinService().find(reportField.getJoinId());
 			String tableKeyPrefix = getTableKeyPrefixForJoinId(theGuruView, theGuruViewJoin.getId(), "");
 			if (theGuruViewJoin.isJoinTableIsView()) {
 				TheGuruView secondaryView = getTheGuruViewService().readTheGuruViewByViewName(theGuruViewJoin.getJoinTable());
 				while (secondaryView.isPrimaryTableIsView()) {
 					ids += "0_";
 					secondaryView = getTheGuruViewService().readTheGuruViewByViewName(secondaryView.getPrimaryTable());
 				}
 				tableKey = getTableKeyForReportFieldFromViewJoins(reportField, secondaryView, theGuruViewJoin.getId().toString() + "_" + ids);
 				if (tableKey.isEmpty()) {
 					tableKey = tableKeyPrefix + theGuruViewJoin.getId().toString() + "_" + ids + "0_" + secondaryView.getPrimaryTable();
 				} else {
 					tableKey = tableKeyPrefix + tableKey;
 				}
 			} else {
 				tableKey = tableKeyPrefix + ids + theGuruViewJoin.getId().toString() + "_" + theGuruViewJoin.getJoinTable();
 			}
 		}
 		
 		return tableKey;
 	}
 
 	private String getTableKeyPrefixForJoinId(TheGuruView theGuruView, Long theGuruViewJoinId, String tableKeyPrefix) {
 		String result = "";
 		boolean found = false;
 		if (theGuruView.isPrimaryTableIsView()) {
 			TheGuruView secondaryTheGuruView = theGuruViewService.readTheGuruViewByViewName(theGuruView.getPrimaryTable());
 			result = getTableKeyPrefixForJoinId(secondaryTheGuruView, theGuruViewJoinId, tableKeyPrefix + "0_");
 		}
 		if (result.isEmpty()) {
 			List<TheGuruViewJoin> theGuruViewJoins = theGuruViewJoinService.readTheGuruViewJoinsByViewId(theGuruView.getId());
 			for (TheGuruViewJoin theGuruViewJoin : theGuruViewJoins) {
 				if (theGuruViewJoin.getId().equals(theGuruViewJoinId)) {
 					result = tableKeyPrefix;
 					found = true;
 					break;
 				} else if (theGuruViewJoin.isJoinTableIsView()) {
 					TheGuruView secondaryTheGuruView = theGuruViewService.readTheGuruViewByViewName(theGuruViewJoin.getJoinTable());
 					result = getTableKeyPrefixForJoinId(secondaryTheGuruView, theGuruViewJoinId, tableKeyPrefix + theGuruViewJoin.getId().toString() + "_");
 					if (!result.isEmpty()) {
 						found = true;
 						break;
 					}
 				}
 			}
 		} else {
 			found = true;
 		}
 		if (!found)
 			result = "";
 		return result;
 	}
 
 	private String getTableKeyForReportFieldFromViewJoins(ReportField reportField, TheGuruView theGuruView, String ids) {
 		String tableKey = "";
 		String tableNameFromField = "";
 		if (reportField.getSourceColumnName().contains("${")) {
 			tableNameFromField = reportField.getSourceColumnName();
 			tableNameFromField = tableNameFromField.substring(tableNameFromField.indexOf("${") + 2, tableNameFromField.indexOf("}"));
 			if (tableNameFromField.equals(theGuruView.getPrimaryTable())
 					|| (tableNameFromField.equals("CONSTITUENT_TABLE"))
 					|| (reportField.getPicklistItemName() != null && !reportField.getPicklistItemName().isEmpty() && tableNameFromField.equals("PICKLIST_ITEM"))
 					|| (reportField.getCustomFieldFieldName() != null && !reportField.getCustomFieldFieldName().isEmpty() && tableNameFromField.equals("CUSTOM_FIELD"))) {
 				tableKey = ids + "0_" + theGuruView.getPrimaryTable();
 			} else {
 				List<TheGuruViewJoin> theGuruViewJoins = getTheGuruViewJoinService().readTheGuruViewJoinsByViewId(theGuruView.getId());
 				for (TheGuruViewJoin theGuruViewJoin : theGuruViewJoins) {
 					if (theGuruViewJoin.isJoinTableIsView()) {
 						TheGuruView secondaryView = getTheGuruViewService().readTheGuruViewByViewName(theGuruViewJoin.getJoinTable());
 						tableKey = getTableKeyForReportFieldFromViewJoins(reportField, secondaryView, ids);
 						if (tableKey.length() > 0) {
 							// make sure table is included in the joins
 							break;
 						}
 					} else {
 						if (theGuruViewJoin.getJoinTable().equalsIgnoreCase(tableNameFromField)) {
 							tableKey = ids + theGuruViewJoin.getId().toString() + "_" + theGuruViewJoin.getJoinTable();
 							break;
 						}
 					}
 				}
 			}
 		} else {
 			tableKey = ids + "0_" + theGuruView.getPrimaryTable();
 		}
 		return tableKey;
 	}
 
 	public void setTheGuruViewService(TheGuruViewService theGuruViewService) {
 		this.theGuruViewService = theGuruViewService;
 	}
 
 	public TheGuruViewService getTheGuruViewService() {
 		return theGuruViewService;
 	}
 
 	public void setTheGuruViewJoinService(TheGuruViewJoinService theGuruViewJoinService) {
 		this.theGuruViewJoinService = theGuruViewJoinService;
 	}
 
 	public TheGuruViewJoinService getTheGuruViewJoinService() {
 		return theGuruViewJoinService;
 	}
 }
