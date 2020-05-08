 package com.mpower.util;
 
 import java.text.DateFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 
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
 import com.mpower.service.ReportCustomFilterDefinitionService;
 import com.mpower.service.ReportFieldService;
 
 /**
  * <p>
  * Object to create queries for either mySql or SQL Server.
  */
 public class ReportQueryGenerator {
 	private ReportWizard reportWizard;
 	private ReportFieldService reportFieldService;
 	private ReportCustomFilterDefinitionService reportCustomFilterDefinitionService;
 
     /**
      * Constructor for the <tt>QueryGenerator</tt>.
      * @param reportWizard ReportWizard that contains the various report options.
      * @param reportFieldService ReportFieldService the QueryGenerator will use to retrieve field information from the database.
      */
     public ReportQueryGenerator(ReportWizard reportWizard, ReportFieldService reportFieldService,
     		ReportCustomFilterDefinitionService reportCustomFilterDefinitionService) {
     	this.setReportWizard(reportWizard);
     	this.setReportFieldService(reportFieldService);
     	this.setReportCustomFilterDefinitionService(reportCustomFilterDefinitionService);
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
 
 	/**
 	 * Builds and returns a mySql or SQL Server query.
 	 * <P>
 	 * {@code} String query = getQueryString();
 	 * @return String
 	 * @throws ParseException
 	 */
 	public String getQueryString() throws ParseException {
 		String query = buildSelectClause(false);
 		query += buildWhereClause(false);
 		query += buildOrderByClause();
 		if (getReportWizard().getDataSubSource().getDatabaseType() == ReportDatabaseType.MYSQL
 				&& getReportWizard().getRowCount() > 0)
 			query += " LIMIT 0," + getReportWizard().getRowCount();
 		query += ";";
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
 		String query = buildSelectClause(true);
 		query += buildWhereClause(false);
 		// If queries take too long to run for the preview, the order by clause could be removed
 		// and this would prevent the complete result from having to be compiled in order for the 
 		// top rows to be returned.
 		query += buildOrderByClause();
 		if (getReportWizard().getDataSubSource().getDatabaseType() == ReportDatabaseType.MYSQL) {
 			if (getReportWizard().getRowCount() == -1 || getReportWizard().getRowCount() > 2000)
 				query += " LIMIT 0, 2000";
 			else
 				query += " LIMIT 0," + getReportWizard().getRowCount();
 		}	
 		query += ";";
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
 			selectClause += buildSelectFieldsForMatrix();
 		} else {
 			selectClause += buildSelectFieldsForNonMatrix();
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
 	private String buildSelectFieldsForNonMatrix() {
 		String selectClause = "";
 		Iterator<ReportSelectedField> itReportSelectedFields = getReportWizard().getReportSelectedFields().iterator();
 		boolean addComma = false;
 		Integer columnIndex = 0;
 		String columnName = null;
 		HashMap<String, String> primaryKeys = new HashMap<String, String>();
 		while (itReportSelectedFields.hasNext()) {
 			ReportSelectedField selectedField = (ReportSelectedField) itReportSelectedFields.next();
 			ReportField reportField = getReportFieldService().find(selectedField.getFieldId());
 			primaryKeys.put(reportField.getPrimaryKeys(), reportField.getPrimaryKeys());
 			if (reportField.getAliasName() == null || reportField.getAliasName().length() == 0)
 				columnName = reportField.getColumnName() + "_" + columnIndex;
 			else
 				columnName = getFieldNameTextQualifier() + reportField.getAliasName() + "_" + columnIndex + getFieldNameTextQualifier();
 			if (reportField == null || reportField.getId() == -1) continue;
 				if (addComma)
 					selectClause += ",";
 				else
 					addComma = true;
 				selectClause += " " + getFieldNameForSelectOrWhereClause(reportField) + " as " + columnName;
 				columnIndex++;
 
 			}
 		//Add all the primary_key fields so that the records are not accidentally eliminated
 		//by the DISTINCT in the select clause
 		if (!reportWizard.getUniqueRecords()){
 			if (primaryKeys.size() > 0){
 				Iterator<String> itPrimaryKeys = primaryKeys.values().iterator();
 				while (itPrimaryKeys.hasNext()){
 					String pk = (String) itPrimaryKeys.next();
 					if (pk != null)
 						selectClause += " ," + pk;
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
 	private String buildSelectFieldsForMatrix() {
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
 						selectClause = selectClause + ",";
 					else
 						addComma = true;
 					selectClause = selectClause + " " + getFieldNameForSelectOrWhereClause(reportField) + " as " + columnName;
 					//Add the primary_keys column to the select so that the DISTINCT in the select clause
 					//does not remove data that should be there.
 					if (reportField.getPrimaryKeys() != null){
 						selectClause = selectClause + ", " + reportField.getPrimaryKeys();
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
 						selectClause = selectClause + ",";
 					else
 						addComma = true;
 					selectClause = selectClause + " " + reportField.getColumnName() + " as " + columnName;
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
 						selectClause = selectClause + ",";
 					else
 						addComma = true;
 					selectClause = selectClause + " " + reportField.getColumnName() + " as " + columnName;
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
 				whereClause += buildStandardFilterWhereClause(filter.getReportStandardFilter(), index);
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
 	private String buildStandardFilterWhereClause(ReportStandardFilter reportStandardFilter, int index) throws ParseException {
 		String whereClause = " (";
 		ReportField rf = reportFieldService.find(reportStandardFilter.getFieldId());
 		String controlName = rf.getAliasName() + Integer.toString(index);
 
 		String sqlDateType = "";
 		if (getReportWizard().getDataSubSource().getDatabaseType() == ReportDatabaseType.MYSQL)
 			sqlDateType += "DATE";
 		else if (getReportWizard().getDataSubSource().getDatabaseType() == ReportDatabaseType.SQLSERVER)
 			sqlDateType += "DATETIME";
 
 		switch(reportStandardFilter.getComparison()) {
 			case 1:
 				whereClause += getFieldNameForSelectOrWhereClause(rf) + " =";
 				whereClause += buildPromptForCritiera(reportStandardFilter, controlName, rf);
 				break;
 			case 2:
 				whereClause += getFieldNameForSelectOrWhereClause(rf) + " !=";
 				whereClause += buildPromptForCritiera(reportStandardFilter, controlName, rf);
 				break;
 			case 3:
 				whereClause += getFieldNameForSelectOrWhereClause(rf) + " <";
 				whereClause += buildPromptForCritiera(reportStandardFilter, controlName, rf);
 				break;
 			case 4:
 				whereClause += getFieldNameForSelectOrWhereClause(rf) + " >";
 				whereClause += buildPromptForCritiera(reportStandardFilter, controlName, rf);
 				break;
 			case 5:
 				whereClause += getFieldNameForSelectOrWhereClause(rf) + " <=";
 				whereClause += buildPromptForCritiera(reportStandardFilter, controlName, rf);
 				break;
 			case 6:
 				whereClause += getFieldNameForSelectOrWhereClause(rf) + " >=";
 				whereClause += buildPromptForCritiera(reportStandardFilter, controlName, rf);
 				break;
 			case 7:
 				whereClause += getFieldNameForSelectOrWhereClause(rf) + " LIKE";
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
 				whereClause += getFieldNameForSelectOrWhereClause(rf) + " LIKE";
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
 				whereClause += getFieldNameForSelectOrWhereClause(rf) + " LIKE";
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
 				whereClause += getFieldNameForSelectOrWhereClause(rf) + " NOT LIKE";
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
 				whereClause += getFieldNameForSelectOrWhereClause(rf) + " IS NOT NULL";
 				break;
 			case 12:
 				if (reportStandardFilter.getPromptForCriteria() && reportStandardFilter.getComparison() == 12) {
 					//
 					// we are doing a one of and requesting a prompt so we need something like $X(In,fieldName,paramaterName)
 					whereClause += "$X(In," + getFieldNameForSelectOrWhereClause(rf) + "," + controlName +")";
 				} else {
 					whereClause += getFieldNameForSelectOrWhereClause(rf) + " IN (";
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
 				whereClause += getSqlCalendarDurationCriteriaFromCurrentDate(DatePart.YEAR, rf.getColumnName(), 0);
 				break;
 			case 31: // Previous Calendar Year
 				whereClause += getSqlCalendarDurationCriteriaFromCurrentDate(DatePart.YEAR, rf.getColumnName(), -1);
 				break;
 			case 32: // Current and Previous Calendar Year
 				whereClause += "( " + getSqlCalendarDurationCriteriaFromCurrentDate(DatePart.YEAR, rf.getColumnName(), -1) +
 							   " OR " +
 							   getSqlCalendarDurationCriteriaFromCurrentDate(DatePart.YEAR, rf.getColumnName(), 0) + " )";
 				break;
 			case 33: // Current Calendar Month
 				whereClause += getSqlCalendarDurationCriteriaFromCurrentDate(DatePart.MONTH, rf.getColumnName(), 0);
 				break;
 			case 34: // Previous Calendar Month
 				whereClause += getSqlCalendarDurationCriteriaFromCurrentDate(DatePart.MONTH, rf.getColumnName(), -1);
 				break;
 			case 35: // Current and Previous Calendar Month
 				whereClause += "( " + getSqlCalendarDurationCriteriaFromCurrentDate(DatePart.MONTH, rf.getColumnName(), -1) +
 				   			   " OR " +
 				   			   getSqlCalendarDurationCriteriaFromCurrentDate(DatePart.MONTH, rf.getColumnName(), 0) + " )";
 				break;
 			case 36: // Current Calendar Week
 				whereClause += getSqlCalendarDurationCriteriaFromCurrentDate(DatePart.WEEK, rf.getColumnName(), 0);
 				break;
 			case 37: // Previous Calendar Week
 				whereClause += getSqlCalendarDurationCriteriaFromCurrentDate(DatePart.WEEK, rf.getColumnName(), -1);
 				break;
 			case 38: // Current and Previous Calendar Week
 				whereClause += "( " + getSqlCalendarDurationCriteriaFromCurrentDate(DatePart.WEEK, rf.getColumnName(), -1) +
 				   			   " OR " +
 				   			   getSqlCalendarDurationCriteriaFromCurrentDate(DatePart.WEEK, rf.getColumnName(), 0) + " )";
 				break;
 			case 39: // Today
 				whereClause += "CAST(" + rf.getColumnName() +" AS " + sqlDateType + ") " + " = " + getSqlCriteriaDaysFromCurrentDate(0);
 				break;
 			case 40: // Yesterday
 				whereClause += "CAST(" + rf.getColumnName() +" AS " + sqlDateType + ") " + " = " + getSqlCriteriaDaysFromCurrentDate(-1);
 				break;
 			case 41: // Last 7
 				whereClause += "CAST(" + rf.getColumnName() +" AS " + sqlDateType + ") " +  " > " + getSqlCriteriaDaysFromCurrentDate(-7);
 				break;
 			case 42: // Last 30
 				whereClause += "CAST(" + rf.getColumnName() +" AS " + sqlDateType + ") " +  " > " + getSqlCriteriaDaysFromCurrentDate(-30);
 				break;
 			case 43: // Last 60
 				whereClause += "CAST(" + rf.getColumnName() +" AS " + sqlDateType + ") " +  " > " + getSqlCriteriaDaysFromCurrentDate(-60);
 				break;
 			case 44: // Last 90
 				whereClause += "CAST(" + rf.getColumnName() +" AS " + sqlDateType + ") " +  " > " + getSqlCriteriaDaysFromCurrentDate(-90);
 				break;
 			case 45: // Last 120
 				whereClause += "CAST(" + rf.getColumnName() +" AS " + sqlDateType + ") " +  " > " + getSqlCriteriaDaysFromCurrentDate(-120);
 				break;
 		}
 		whereClause += ")";
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
 				whereClause += filterString;
 			}
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
 	private String getFieldNameForSelectOrWhereClause(ReportField reportField) {
 		String result = "";
 		if (reportField.getFieldType() == ReportFieldType.DATE) {
 			if (getReportWizard().getDataSubSource().getDatabaseType() == ReportDatabaseType.MYSQL) {
				result = "CAST(" + reportField.getColumnName() + " AS DATE)";
 			}
 			else if (getReportWizard().getDataSubSource().getDatabaseType() == ReportDatabaseType.SQLSERVER) {
 				result = "DATEADD(DAY, DATEDIFF(DAY, 0, " + reportField.getColumnName() + "), 0)";
 			}
 		}
 		else {
			result = reportField.getColumnName();
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
 			result = "DATEADD(DD, " + Integer.toString(days) + ", GETDATE())";
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
 		String result = "";
 		if (getReportWizard().getDataSubSource().getDatabaseType() == ReportDatabaseType.MYSQL) {
 			result = "( " + datePart.mySQL() + "(" + columnName + ") = " + datePart.mySQL() + "(CURDATE()) + " + Integer.toString(duration);
 			if (datePart == DatePart.YEAR){
 				result += " AND YEAR(" + columnName + ") = (YEAR(CURDATE()) + " + Integer.toString(duration) + " ))";
 			}else{
 				result += " AND YEAR(" + columnName + ") = YEAR(CURDATE()) )";
 			}
 		}
 		else if (getReportWizard().getDataSubSource().getDatabaseType() == ReportDatabaseType.SQLSERVER) {
 			result = "( DATEPART(" + datePart.SQL() + ", " + columnName + ") = (DATEPART(" + datePart.SQL() + ", GETDATE()) + " + Integer.toString(duration) + ")";
 			if (datePart == DatePart.YEAR){
 				result += " AND YEAR(" + columnName + ") = (YEAR(GETDATE()) + " + Integer.toString(duration) + " )) " ;
 			}else{
 				result += " AND YEAR(" + columnName + ") = YEAR(GETDATE()) ) " ;
 			}
 		}
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
 }
