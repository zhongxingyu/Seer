 /**
  * 
  */
 package om.tnavigator.reports.std;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import om.tnavigator.*;
 import om.tnavigator.reports.*;
 
 /**
  * This report exports test scores in the format expected by the Moodle &gt;=1.9 gradebook.
  */
 public class MoodleFormatReport extends TabularReportBase implements OmTestReport, OmReport {
 	NavigatorServlet ns;
 	
 	/**
 	 * Create an instance of this report.
 	 * @param ns the navigator servlet we belong to.
 	 */
 	public MoodleFormatReport(NavigatorServlet ns) {
 		super(ns);
 	}
 
 	/* (non-Javadoc)
 	 * @see om.tnavigator.reports.OmTestReport#getUrlTestReportName()
 	 */
 	public String getUrlTestReportName() {
 		return "moodle";
 	}
 
 	/* (non-Javadoc)
 	 * @see om.tnavigator.reports.OmTestReport#getReadableReportName()
 	 */
 	public String getReadableReportName() {
 		return "Results for import into the Moodle gradebook";
 	}
 	
 	/* (non-Javadoc)
 	 * @see om.tnavigator.reports.OmTestReport#isApplicable(om.tnavigator.TestDeployment)
 	 */
 	public boolean isApplicable(TestDeployment td) {
 		return true;
 	}
 	
 	/* (non-Javadoc)
 	 * @see om.tnavigator.reports.OmTestReport#handleTestReport(om.tnavigator.UserSession, java.lang.String, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
 	 */
 	public void handleTestReport(UserSession us, String suffix,
 			HttpServletRequest request, HttpServletResponse response)
 			throws Exception {
 		handleReport(""/*us.tdDeployment*/, request, response);
 	}
 
 	/* (non-Javadoc)
 	 * @see om.tnavigator.reports.OmReport#getUrlReportName()
 	 */
 	public String getUrlReportName() {
 		return "moodle";
 	}
 
 	/* (non-Javadoc)
 	 * @see om.tnavigator.reports.TabularReportBase#generateReport(om.tnavigator.reports.TabularReportWriter)
 	 */
 	@Override
 	public void generateReport(TabularReportWriter reportWriter) {
		// TODO Actually generate the report.
 		
 	}
 
 	/* (non-Javadoc)
 	 * @see om.tnavigator.reports.TabularReportBase#init(javax.servlet.http.HttpServletRequest)
 	 */
 	@Override
 	public List<ColumnDefinition> init(HttpServletRequest request) {
 		List<ColumnDefinition> columns = new ArrayList<ColumnDefinition>();
 		columns.add(new ColumnDefinition("student", "Student"));
 		columns.add(new ColumnDefinition("assignment", "Assignment"));
 		columns.add(new ColumnDefinition("score", "Score"));
 		return columns;
 	}
 
 }
