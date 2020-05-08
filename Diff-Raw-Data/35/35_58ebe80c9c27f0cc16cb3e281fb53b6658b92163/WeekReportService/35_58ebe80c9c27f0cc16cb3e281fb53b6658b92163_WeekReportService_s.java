 package com.pengjun.ka.db.service;
 
 import java.util.List;
 
 import com.pengjun.ka.db.model.AccountRecord;
 import com.pengjun.ka.db.model.WeekOrMonthReport;
 import com.pengjun.ka.utils.TimeUtils;
 
 public class WeekReportService extends BaseReportService {
 
 	public static WeekOrMonthReport computeReportData(List<AccountRecord> arList) {
 
 		WeekOrMonthReport weekReport = new WeekOrMonthReport();
 
 		creatBaseReport(weekReport, arList);
 
 		int workDay = 0;
 		int weekend = 0;
 		for (AccountRecord ar : arList) {
 
 			int day = TimeUtils.getDayOfWeek(TimeUtils.string2Date(ar.getCreateDate()).getTime());
 			// Sunday is 1, Monday is 2
			if (day >= 2 && day <= 5) {
 				workDay++;
 			} else if (day == 1 || day == 7) {
 				weekend++;
 			}
 		}
 
 		int max = Math.max(workDay, weekend);
 		if (max == workDay) {
 			weekReport.setMaxCostInterval("工作日");
 		} else if (max == weekend) {
 			weekReport.setMaxCostInterval("周末");
 		}
 		return weekReport;
 	}
 }
