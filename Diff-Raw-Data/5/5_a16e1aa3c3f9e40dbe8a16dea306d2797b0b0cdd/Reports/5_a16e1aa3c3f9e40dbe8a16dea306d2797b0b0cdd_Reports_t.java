 package org.alt60m.ministry.servlet;
 
 import java.util.*;
 
 import org.alt60m.ministry.model.dbio.*;
 
 import java.sql.Connection;
 import java.sql.Statement;
 import java.sql.ResultSet;
 import org.alt60m.util.*;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 public class Reports {
 	private static Log log = LogFactory.getLog(InfoBaseController.class);
 	
 	//the following strings are for getSuccessCriteriaReport() method. 
 	private static String  conditionsPortion(String type, String region, String periodEnd, String periodBegin, String strategyList, String localLevelId, String targetAreaID){
 		String queryPortion="";
 		queryPortion="WHERE ("+
 			" ministry_statistic.periodEnd < "+periodEnd+
 			" AND ministry_statistic.periodEnd >="+periodBegin+
 			" and ministry_activity.Strategy in ("+strategyList+") ";
 			if (type.equals("locallevel")){
 				queryPortion+=" and ministry_locallevel.teamID = '" + localLevelId + "' ";
 			}
 			else if (type.equals("regional")){
 				queryPortion+=" and ministry_targetarea.region = '" + region+ "' ";
 			}
 			else if (type.equals("national"))
 			{
 				queryPortion+=" and (ministry_targetarea.region is not null) AND (ministry_targetarea.region <> '') ";
 			}
 			else if (type.equals("targetarea"))
 			{
 				queryPortion+=" and (ministry_targetarea.targetAreaID = '" + targetAreaID+ "') ";
 			}
 			queryPortion+=") "; 
 			return queryPortion;
 	}
 	private static String groupPortion(String type){ 
 		if(type.equals("national"))
 		{
 			return " GROUP BY ministry_targetarea.region ORDER BY ministry_targetarea.region";
 		}
 		else if(type.equals("targetarea"))
 		{
 			return " GROUP BY ministry_targetarea.name, ministry_activity.strategy, ministry_statistic.periodEnd, ministry_statistic.peopleGroup ORDER BY ministry_targetarea.name, ministry_activity.strategy, ministry_statistic.periodEnd, ministry_statistic.peopleGroup";
 		}
 		else
 		{
 		return " GROUP BY ministry_targetarea.name, ministry_activity.strategy, ministry_statistic.peopleGroup"+
 				" ORDER BY ministry_targetarea.name, ministry_activity.strategy, ministry_statistic.peopleGroup ";
 		}
 	}
 	private static String summingFieldsPortion(String type){
 		String queryPortion="";
 		queryPortion="SELECT "+
 		
 		" ministry_activity.ActivityID as activityID,"+
 		" CAST(ministry_activity.periodBegin AS DATE) as activityPeriodBegin,"+
 		" CAST(ministry_statistic.periodEnd AS DATE) as statPeriodEnd,"+
 		" CAST(ministry_statistic.periodBegin AS DATE) as statPeriodBegin,"+
 		" ministry_locallevel.teamID as localLevelId,"+
 		" ministry_targetarea.region, "+
 		" SUM(ministry_statistic.exposuresViaMedia) as exposuresViaMedia,"+
 		" SUM(ministry_statistic.evangelisticOneOnOne) as evangelisticOneOnOne, "+
 		" SUM(ministry_statistic.evangelisticGroup) as evangelisticGroup,"+
 		" SUM(ministry_statistic.decisions) as decisionSum,"+
 		" SUM(ministry_statistic.decisionsHelpedByOneOnOne) as decisionsPersonalEvangelismExposures, "+
 		" SUM(ministry_statistic.decisionsHelpedByGroup) as decisionsGroupEvangelismExposures, "+
 		" SUM(ministry_statistic.decisionsHelpedByMedia) as decisionsMediaExposures, "+
 		" SUM(ministry_statistic.holySpiritConversations) as holySpirit, "+
 		" SUM(ministry_statistic.laborersSent) as laborersSent, " +
 		" ministry_statistic.peopleGroup, ";
 		
 		if (type.equals("targetarea")){					
 			queryPortion+=" ministry_activity.status as status, "+
 				
 				
 				" MAX(ministry_targetarea.TargetAreaID) targetAreaID,"+
 				" MAX(ministry_targetarea.name) campusName,"+
 				" ministry_activity.strategy, "+
				" concat(ministry_activity.ActivityID,'_',ministry_statistic.periodEnd) as rowid, "+
 				" MAX(ministry_targetarea.enrollment) enrollment ";
 		}
 		
 		else if (!type.equals("national")){					
 			queryPortion+=" ministry_activity.status as status, "+
 				
 				
 				" MAX(ministry_targetarea.TargetAreaID) targetAreaID,"+
 				" MAX(ministry_targetarea.name) campusName,"+
 				" ministry_activity.strategy, "+
 				" ministry_activity.ActivityID as rowid,"+
 				" MAX(ministry_targetarea.enrollment) enrollment ";
 		}
 		else
 		{
 			queryPortion+=" '' as status, "+
 				
 				
 				" '' as targetAreaID, "+
 				" '' as campusName,"+
 				" 'CA' as strategy, "+
 				" ministry_targetarea.region as rowid,"+
 				" ''  as enrollment ";
 		}
 		
 		return queryPortion;		
 	}
 	private static String summingTablesPortion=" from ministry_statistic left join "+
 							" ministry_activity on (ministry_statistic.fk_Activity = ministry_activity.ActivityID) left join "+
 							" ministry_locallevel on (ministry_locallevel.TeamID = ministry_activity.fk_TeamID) LEFT JOIN "+
 							" ministry_targetarea on ministry_targetarea.targetAreaID = ministry_activity.fk_targetAreaID ";
 
 	private static String demographicFieldsPortion(String type) { 
 		String queryPortion="";
 		if(type.equals("targetarea")){
 			queryPortion="SELECT ministry_statistic.peopleGroup,"+
							" concat(ministry_activity.ActivityID,'_',ministry_statistic.periodEnd) as rowid, "+
 							
 							" ministry_targetarea.region,"+
 							" ministry_targetarea.targetAreaID, "+
 							" ministry_targetarea.name, "+
 							" ministry_statistic.multipliers, "+
 							" ministry_statistic.invldStudents, "+
 							" ministry_statistic.ongoingEvangReln as Seekers,"+
 							" ministry_statistic.studentLeaders, "+
 							" ministry_statistic.periodEnd, "+
 							" ministry_locallevel.region ";
 		}
 		else if(!type.equals("national")){
 			queryPortion="SELECT ministry_statistic.peopleGroup,"+
 							" ministry_activity.ActivityID as rowid, "+
 							
 							" ministry_targetarea.region,"+
 							" ministry_targetarea.targetAreaID, "+
 							" ministry_targetarea.name, "+
 							" ministry_statistic.multipliers, "+
 							" ministry_statistic.invldStudents, "+
 							" ministry_statistic.ongoingEvangReln as Seekers,"+
 							" ministry_statistic.studentLeaders, "+
 							
 							" ministry_locallevel.region ";
 		}
 		else
 		{
 			queryPortion="SELECT ministry_statistic.peopleGroup, "+
 							" ministry_targetarea.region as rowid, "+
 							" ministry_targetarea.region,"+
 							" ministry_targetarea.targetAreaID, "+
 							" ministry_targetarea.name, "+
 							" SUM(ministry_statistic.multipliers) multipliers, "+
 							" SUM(ministry_statistic.invldStudents) invldStudents, "+
 							" SUM(ministry_statistic.ongoingEvangReln) as Seekers, "+
 							" SUM(ministry_statistic.studentLeaders) studentLeaders,"+
 							" ministry_locallevel.region ";
 							
 		}
 		
 				return queryPortion;
 		}
 	private static String demographicTablesPortion(String type, String periodEnd, String periodBegin) {
 		return "FROM ministry_statistic INNER JOIN "+
 					(!type.equals("targetarea")? //we skip this step for targetarea, since all weeks are shown
 							" (SELECT ministry_statistic.peopleGroup as peopleGroup, "+
 						" ministry_statistic.fk_Activity AS fk_Activity, "+
 						" MAX(ministry_statistic.periodEnd) AS lastDate "+
 						" FROM ministry_statistic WHERE ministry_statistic.periodEnd < " + periodEnd + 
 						" AND ministry_statistic.periodEnd >= " + periodBegin + 
 						" GROUP BY fk_Activity, ministry_statistic.peopleGroup) LastActivities"+
 					" ON ministry_statistic.fk_Activity = LastActivities.fk_Activity "+
 						" AND ministry_statistic.periodEnd = LastActivities.lastDate "+
 						" and (((ministry_statistic.peopleGroup is null) and (LastActivities.peopleGroup is null)) "+
 							" or (ministry_statistic.peopleGroup=LastActivities.peopleGroup)) LEFT JOIN "
 						:
 							"")+
 			" ministry_activity on ministry_statistic.fk_Activity = ministry_activity.ActivityID LEFT JOIN "+
 			" ministry_locallevel on ministry_locallevel.teamID = ministry_activity.fk_teamID LEFT JOIN "+
 			" ministry_targetarea on ministry_targetarea.targetAreaID = ministry_activity.fk_targetAreaID ";
 	}
 
 	private static ReportRow resultSet2ReportRow(ResultSet sums, ResultSet demos)throws Exception 
 	{
 			try{
 		ReportRow row=new ReportRow();
 		row.setRowid(sums.getString("rowid"));
 		row.setActivityID(sums.getString("activityID"));
 		row.setCampusName(sums.getString("campusName"));
 		row.setStrategy(sums.getString("strategy"));
 		row.setRegion(sums.getString("region"));
 		row.setPeopleGroup(sums.getString("peopleGroup"));
 		row.setStatus(sums.getString("status"));
 		row.setTargetAreaID(sums.getString("targetAreaID"));
 		row.setLocalLevelId(sums.getString("localLevelId"));
 		row.setActivityPeriodBegin(sums.getString("activityPeriodBegin"));
 		row.setStatPeriodEnd(sums.getString("statPeriodEnd"));
 		row.setStatPeriodBegin(sums.getString("statPeriodBegin"));
 		row.setPeopleGroup(sums.getString("peopleGroup"));
 		row.setEnrollment(sums.getString("enrollment"));
 		
 		
 		
 		row.setEvangelisticOneOnOne(sums.getInt("evangelisticOneOnOne"));
 		row.setEvangelisticGroup(sums.getInt("evangelisticGroup"));
 		
 		row.setExposuresViaMedia(sums.getInt("exposuresViaMedia"));
 		row.setHolySpirit(sums.getInt("holySpirit"));
 		
 		row.setDecisions(sums.getInt("decisionSum"));
 		row.setDecisionsPersonalEvangelismExposures(sums.getInt("decisionsPersonalEvangelismExposures"));
 		row.setDecisionsGroupEvangelismExposures(sums.getInt("decisionsGroupEvangelismExposures"));
 		
 		row.setDecisionsMediaExposures(sums.getInt("decisionsMediaExposures"));
 		row.setLaborersSent(sums.getInt("laborersSent"));
 		
 		row.setMultipliers(demos.getInt("multipliers"));
 		row.setInvldStudents(demos.getInt("invldStudents"));
 		row.setSeekers(demos.getInt("seekers"));
 		row.setStudentLeaders(demos.getInt("studentLeaders"));
 		return row;
 	}catch (Exception e) {
 		log.error("Failed to perform Reports.resultSet2ReportRow().", e);
         throw new Exception(e);
     }
 	}
 	// Returns stats for various Success Criteria Reports
 	public static Vector<ReportRow> getSuccessCriteriaReport(String type, String region, String strategyList, String periodEnd, String periodBegin, String localLevelId, String targetAreaId) throws Exception{
 		try{		
 		String sumsQuery=summingFieldsPortion(type)+summingTablesPortion+
 						conditionsPortion(type, region,  periodEnd, periodBegin,  strategyList, localLevelId, targetAreaId)+
 						groupPortion(type);
 		String demographicQuery=demographicFieldsPortion(type)+demographicTablesPortion(type, periodEnd, periodBegin)+
 						conditionsPortion(type, region, periodEnd, periodBegin, strategyList, localLevelId, targetAreaId)+groupPortion(type);
 				
 		Vector<ReportRow> report=new Vector<ReportRow>();
 		Connection conn = DBConnectionFactory.getDatabaseConn();
 		Statement stmt1 = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
 		Statement stmt2 = conn.createStatement();
 		log.debug(sumsQuery);
 		
 		ResultSet sums = stmt1.executeQuery(sumsQuery);
 		
 		log.debug(demographicQuery);
 		ResultSet demos = stmt2.executeQuery(demographicQuery);
 		ReportRow lastRow=new ReportRow();
 		ReportRow row=new ReportRow();
 		ReportRow summingRow=new ReportRow(); //for within Bridges movements
 		ReportRow runningTotal=new ReportRow(); //for totals at report bottoms
 		String lastRowId="";
 		String lastStrategy="";
 		if (sums.isBeforeFirst()){
 			while (sums.next()){
 			
 			demos.next();
 			row=new ReportRow();
 			if (sums.getString("rowid").equals(demos.getString("rowid"))){
 				row=resultSet2ReportRow(sums,demos);
 				
 				if (type.equals("national")){
 					row.setLabel(org.alt60m.ministry.Regions.expandRegion(row.getRegion()));
 					}
 					else if (type.equals("targetarea"))
 					{
 					row.setLabel((row.getStatPeriodBegin().replace("-","/")+" - "+row.getStatPeriodEnd().replace("-","/")));
 					}
 					else 
 					{
 					row.setLabel(row.getCampusName()+" - "+org.alt60m.ministry.Strategy.expandStrategy(row.getStrategy())+" ("+row.getEnrollment()+" enrolled)");
 					}
 			
 				if((!lastRow.getRowid().equals(row.getRowid()))&&(lastRow.getStrategy().equals("BR"))){ //new activity or week after a run of Bridges; the order is important for these functional rows
 					
 					//put end row on if after Bridges rows, since we are now in new activity
 						ReportRow endRow=summingRow; //we have been totaling the previous Bridges rows, now we dump them into final row
 						endRow.setFunction("end"); 
 						endRow.setLabel(summingRow.getLabel());
 						report.add(endRow);
 						
 						}
 				
 				if ((!lastRow.getStrategy().equals(row.getStrategy()))&&(type.equals("targetarea"))&&(!(lastRow.getStrategy().equals("")||(lastRow.getStrategy()==null)))){ // before the top row of each strategy  we also insert a totals row for the bottom of each strategy
 						ReportRow bottom=new ReportRow(runningTotal);
 						bottom.setFunction("bottom");
 						if (type.equals("targetarea")){ //we actually don't want a running total of these for targetarea, just the last record.
 							bottom.setInvldStudents(lastRow.getInvldStudents());
 							bottom.setMultipliers(lastRow.getMultipliers());
 							bottom.setStudentLeaders(lastRow.getStudentLeaders());
 						}
 						report.add(bottom);
 						runningTotal=new ReportRow();
 						
 					}
 					
 				if (((!lastRow.getStrategy().equals(row.getStrategy()))&&(type.equals("targetarea")))||((lastRow.getStrategy().equals("")||(lastRow.getStrategy()==null)))){	//always at top and between strategies for targetarea
 					ReportRow top=new ReportRow(row);
 					top.setFunction("top");
 					report.add(top);
 					
 					}
 				
 				
 				
 				
 			if ((!lastRow.getRowid().equals(row.getRowid()))&&(row.getStrategy().equals("BR"))){// is new activity and also Bridges; start toggling rows
 				row.setFunction("detail");
 				ReportRow startRow=new ReportRow();
 				startRow.setFunction("start"); 
 				startRow.setRowGroup(row.getRowid());
 				startRow.setStrategy(row.getStrategy());
 				startRow.setCampusName(row.getCampusName());
 				startRow.setEnrollment(row.getEnrollment());
 				startRow.setLabel(row.getLabel());
 				
 				report.add(startRow);
 			}
 			
 			else if (row.getStrategy().equals("BR"))//row within split activity
 			{
 				row.setFunction("detail"); 
 				
 			}
 			
 			if((!lastRow.getRowid().equals(row.getRowid()))&&(row.getStrategy().equals("BR"))){//new activity and it's Bridges
 				summingRow=new ReportRow();	//we have been totaling the previous Bridges rows, now we clear them before starting a new summing session
 				}
 			
 			if (row.getStrategy().equals("BR")){ // we sum for Bridges rows only
 				summingRow.addToTotal(row);
 			}
 			
 			runningTotal.addToTotal(row);
 			
 			report.add(row);
 			}
 			else
 			{
 				throw new Exception("Rows do not match in Reports.getSuccessCriteriaReport()") ;
 			}
 			lastRow=new ReportRow(row);
 		}
 		if (lastRow.getStrategy().equals("BR")){//put end row on if last activity was Bridges
 			ReportRow endRow=summingRow; //we have been totaling the previous Bridges rows, now we dump them
 			endRow.setFunction("end"); 
 			report.add(endRow);
 		}
 		ReportRow bottom=new ReportRow(runningTotal);
 		bottom.setFunction("bottom");
 		if (type.equals("targetarea")){ //we actually don't want a running total of these for targetarea, just the last record.
 			bottom.setInvldStudents(row.getInvldStudents());
 			bottom.setMultipliers(row.getMultipliers());
 			bottom.setStudentLeaders(row.getStudentLeaders());
 		}
 		report.add(bottom);
 		runningTotal=new ReportRow();
 		}//resultset had data, otherwise return no rows in Vector<ReportRow> 'report'
 		
 		return report;
 		}
 		catch (Exception e) {
 			log.error("Failed to perform getSuccessCriteriaReport().", e);
             throw new Exception(e);
         }
 	}
 }
