 /**
  * 
  */
 package om.tnavigator.reports.std;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.*;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.w3c.dom.Element;
 
 import om.OmUnexpectedException;
 import om.tnavigator.NavigatorServlet;
 import om.tnavigator.db.DatabaseAccess;
 import om.tnavigator.reports.*;
 
 /**
  * This report lists the questions that have been deployed on this server, with the
  * available versions in reverse date order.
  */
 public class MonthlyUsageReport implements OmReport {
 	private final static DateFormat dateFormat = new SimpleDateFormat("yyyy-MM");
 
 	protected NavigatorServlet ns;
 
 	/**
 	 * Create an instance of this report.
 	 * @param ns the navigator servlet we belong to.
 	 */
 	public MonthlyUsageReport(NavigatorServlet ns) {
 		this.ns = ns;
 	}
 
 	/* (non-Javadoc)
 	 * @see om.tnavigator.reports.OmReport#getUrlReportName()
 	 */
 	@Override
 	public String getUrlReportName() {
 		return "monthlyusage";
 	}
 
 	/* (non-Javadoc)
 	 * @see om.tnavigator.reports.OmReport#handleReport(java.lang.String, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
 	 */
 	@Override
 	public void handleReport(String suffix, HttpServletRequest request,
 			HttpServletResponse response) throws Exception {
 		TestUsageTabularReport report = new TestUsageTabularReport(ns);
 		report.handleReport(request, response);
 	}
 
 	private class TestUsageTabularReport extends TabularReportBase {
 		/**
 		 * Constructor.
 		 * @param ns
 		 */
 		public TestUsageTabularReport(NavigatorServlet ns) {
 			batchid = null;
 			title = "Number of attempts each month";
 			this.ns = ns;
 		}
 
 		@Override
 		public String getReportTagName() {
 			return "months";
 		}
 		@Override
 		public String getRowTagName() {
 			return "month";
 		}
 
 		/* (non-Javadoc)
 		 * @see om.tnavigator.reports.TabularReportBase#init(javax.servlet.http.HttpServletRequest)
 		 */
 		@Override
 		public List<ColumnDefinition> init(HttpServletRequest request) {
 			List<ColumnDefinition> columns = new ArrayList<ColumnDefinition>();
 			columns.add(new ColumnDefinition("date", "Date"));
 			columns.add(new ColumnDefinition("attemptscompleted", "Attempts Completed"));
 			columns.add(new ColumnDefinition("attemptsstarted", "Attempts Started"));
 			return columns;
 		}
 
 		
 
 		/* (non-Javadoc)
 		 * @see om.tnavigator.reports.TabularReportBase#generateReport(om.tnavigator.reports.TabularReportWriter)
 		 */
 		@Override
 		public void generateReport(TabularReportWriter reportWriter) {
 			DatabaseAccess.Transaction dat;
 			try {
 				dat = ns.getDatabaseAccess().newTransaction();
 			} catch (SQLException e) {
 				throw new OmUnexpectedException("Error connecting to the database.", e);
 			}
 			try {
 				ResultSet rs = ns.getOmQueries().queryMonthlyTestFinishes(dat);	
 				Map<String, String> testFinishes = new HashMap<String, String>();
 				while(rs.next()) {
 					int year = rs.getInt(1);
 					int month = rs.getInt(2);
					String date = dateFormat.format((new GregorianCalendar(year, month, 1).getTime()));
 					testFinishes.put(date, rs.getInt(3) + "");
 				}
 
 				rs = ns.getOmQueries().queryMonthlyTestStarts(dat);
 				while(rs.next()) {
 					Map<String, String> row = new HashMap<String, String>();
 					int year = rs.getInt(1);
 					int month = rs.getInt(2);
					String date = dateFormat.format((new GregorianCalendar(year, month, 1).getTime()));
 					row.put("date", date);
 					if (testFinishes.containsKey(date)) {
 						row.put("attemptscompleted", testFinishes.get(date));
 					} else if (year > 2006 || (year == 2006 && month > 9)) {
 						row.put("attemptscompleted", "0");
 					} else {
 						// The finishedclock column was only added to the database in September 2006,
 						// so this data is not available before then.
 						row.put("attemptscompleted", "n/a");
 					}
 					row.put("attemptsstarted", rs.getInt(3) + "");
 					reportWriter.printRow(row);
 				}
 
 			} catch (SQLException e) {
 				throw new OmUnexpectedException("Error generating report.", e);
 			} finally {
 				dat.finish();
 			}
 		}
 
 		@Override
 		public void extraHtmlContent(Element mainElement) {
 			super.extraHtmlContent(mainElement);
 			printMessage("This report only counts non-admin attempts.", mainElement);
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see om.tnavigator.reports.OmReport#isSecurityRestricted()
 	 */
 	@Override
 	public boolean isSecurityRestricted() {
 		return false;
 	}
 }
