 package com.pengjun.ka.android.service;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import android.app.PendingIntent;
 import android.app.Service;
 import android.content.Context;
 import android.content.Intent;
 import android.os.Binder;
 import android.os.Bundle;
 import android.os.IBinder;
 
 import com.pengjun.ka.R;
 import com.pengjun.ka.android.activity.report.MonthReportActivity;
 import com.pengjun.ka.android.activity.report.WeekReportActivity;
 import com.pengjun.ka.db.dao.ArDao;
 import com.pengjun.ka.db.model.AccountRecord;
 import com.pengjun.ka.db.model.ArSearchCondition;
 import com.pengjun.ka.utils.ComponentUtils;
 import com.pengjun.ka.utils.KaConstants;
 import com.pengjun.ka.utils.MyDebug;
 import com.pengjun.ka.utils.ResourceUtils;
 import com.pengjun.ka.utils.StringUtils;
 import com.pengjun.ka.utils.TimeUtils;
 
 public class ReportNotificationService extends Service {
 	private ReportServiceBinder binder = new ReportServiceBinder();
 
 	private static final long HOUR_PERIOD = 60 * 60 * 1000;
 	private Timer timer = new Timer(true);
 	private final String MORNING_TEN_COLOCK = "10";
 	private final String LIGHT_TEN_COLOCK = "22";
 
 	private enum Report {
 		Week(0), Month(1), Year(2);
 
 		private int value = 0;
 
 		private Report(int value) { // 必须是private的，否则编译错误
 			this.value = value;
 		}
 
 		public int getValue() {
 			return value;
 		}
 	}
 
 	public static List<AccountRecord> arList = new ArrayList<AccountRecord>();

 	private TimerTask task = new TimerTask() {
 		public void run() {
 
 			String curMonthYearStr = TimeUtils.getCurMonthYearStr();
 			boolean isCurMonthReportExist = !ResourceUtils.getSharedPreferencesString(
 					ReportNotificationService.this, curMonthYearStr).equals(StringUtils.STRING_NULL_VALUE);
 
 			String curWeekYearStr = TimeUtils.getCurWeekYearStr();
 			boolean isCurWeekReportExist = !ResourceUtils.getSharedPreferencesString(
 					ReportNotificationService.this, curWeekYearStr).equals(StringUtils.STRING_NULL_VALUE);
 
 			String curHour = TimeUtils.getCurHour();
 			boolean isProperTimeToPushReport = curHour.compareTo(MORNING_TEN_COLOCK) >= 0
 					&& curHour.compareTo(LIGHT_TEN_COLOCK) <= 0;
 
 			if (isProperTimeToPushReport) {
 				if (!isCurMonthReportExist) {
 					startMonthReportNotification(ReportNotificationService.this);
 					ResourceUtils.putSharedPreferencesString(ReportNotificationService.this, curMonthYearStr,
 							curMonthYearStr);
 				}
 				if (!isCurWeekReportExist) {
 					startWeekReportNotification(ReportNotificationService.this);
 					ResourceUtils.putSharedPreferencesString(ReportNotificationService.this, curWeekYearStr,
 							curWeekYearStr);
 				}
 			}
 
 		}
 	};
 
 	public static void startMonthReportNotification(Context context) {
 
 		// the frist day of month to create the last month report
 		String lastMonthOfTodayStr = TimeUtils.getLastMonthOfTodayStr();
 
 		ArSearchCondition arSC = new ArSearchCondition();
 		arSC.setStartDate(lastMonthOfTodayStr);
 		arSC.setEndDate(TimeUtils.getLastDayStr());
 
 		arList = ArDao.queryAr(arSC, 0, -1);
 
 		if (arList.size() == 0) {
 			return;
 		}
 
 		String lastMonthYearStr = TimeUtils.String2MonthYearStr(lastMonthOfTodayStr);
 		Intent intent = new Intent(context, MonthReportActivity.class);
 		ComponentUtils.putIntentStringData(intent, new Bundle(), KaConstants.INTENT_TOP_TITLE,
 				lastMonthYearStr + " 月报表");
 		PendingIntent pendingIntent = PendingIntent.getActivity(context, Report.Month.getValue(), intent,
 				PendingIntent.FLAG_UPDATE_CURRENT);
 		ComponentUtils.createNotification(context, context.getResources().getString(R.string.app_name),
				lastMonthYearStr + " 月报表", pendingIntent, Report.Month.getValue());
 	}
 
 	public static void startWeekReportNotification(Context context) {
 
 		// the frist day of week to create the last week report
 		String lastWeekOfTodayStr = TimeUtils.getLastWeekOfTodayStr();
 		String lastDayStr = TimeUtils.getLastDayStr();
 
 		ArSearchCondition arSC = new ArSearchCondition();
 		arSC.setStartDate(lastWeekOfTodayStr);
 		arSC.setEndDate(lastDayStr);
 
 		arList = ArDao.queryAr(arSC, 0, -1);
 
 		if (arList.size() == 0) {
 			return;
 		}
 
 		String lastWeekFirstDayStr = TimeUtils.String2DayMonthStr(lastWeekOfTodayStr);
 		String lastWeekLastDayStr = TimeUtils.String2DayMonthStr(lastDayStr);
 
 		Intent intent = new Intent(context, WeekReportActivity.class);
 		ComponentUtils.putIntentStringData(intent, new Bundle(), KaConstants.INTENT_TOP_TITLE,
 				lastWeekFirstDayStr + "至" + lastWeekLastDayStr + " 周报表");
 		PendingIntent pendingIntent = PendingIntent.getActivity(context, Report.Week.getValue(), intent,
 				PendingIntent.FLAG_UPDATE_CURRENT);
 
 		ComponentUtils.createNotification(context, context.getResources().getString(R.string.app_name),
				lastWeekFirstDayStr + "至" + lastWeekLastDayStr + " 周报表", pendingIntent,
				Report.Week.getValue());
 	}
 
 	@Override
 	public IBinder onBind(Intent intent) {
 		return binder;
 	}
 
 	@Override
 	public void onCreate() {
 		super.onCreate();
 		timer.schedule(task, TimeUtils.getCurDate(), HOUR_PERIOD);
 		MyDebug.printFromPJ("ReportService onCreate");
 	}
 
 	@Override
 	public void onStart(Intent intent, int startId) {
 		super.onStart(intent, startId);
 		MyDebug.printFromPJ("ReportService onStart");
 	}
 
 	@Override
 	public void onDestroy() {
 		MyDebug.printFromPJ("ReportService onDestroy");
 		super.onDestroy();
 	}
 
 	@Override
 	public boolean onUnbind(Intent intent) {
 		MyDebug.printFromPJ("ReportService onUnbind");
 		return super.onUnbind(intent);
 	}
 
 	public class ReportServiceBinder extends Binder {
 		public ReportNotificationService getService() {
 			return ReportNotificationService.this;
 		}
 	}
 
 }
