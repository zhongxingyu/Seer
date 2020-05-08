 package SmartMarker;
 
 import imagescope.security.ReliableDateException;
 import imagescope.security.SecurityDatabase;
 import imagescope.security.SecurityKey;
 import imagescope.security.SecurityKeyManager;
 
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.util.Date;
 import java.util.logging.Handler;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import javax.swing.JOptionPane;
 
 import SmartMarker.SmartMarkerDlg.ReportType;
 
 /**
  * @author NickP
  * 
  */
 public class SmartMarkerUI {
 
 	private final static String CLASS_NAME = SmartMarkerUI.class.getName();
 	private final static Logger LOGGER = Logger.getLogger(CLASS_NAME);
 
 	/**
 	 * @param args
 	 * @throws ReliableDateException
 	 */
 	public static void main(String[] args) throws ReliableDateException {
 		/*
 		 * Set Handler levels
 		 */
 		Level debugLevel = Level.INFO;
 		for (Handler h : Logger.getLogger("").getHandlers())
 			if (h != null)
 				h.setLevel(debugLevel);
 
 		LOGGER.info("DSUK Smart Marker");
 		
 		SecurityDatabase sdb = SecurityDatabase.load("http://www.dsuk.biz/Downloads/Security.jar",
 				"isolate variable");
 		SecurityKey securityKeys[] = SecurityKeyManager.getSecurityKeys();
 
 		Date expDate = new Date();
 		if (!sdb.isFeatureValid(securityKeys, "SmartMarker", null, expDate)) {
 			String message = "\nSmartMarker is not enabled\n"
 					+ SecurityKeyManager.listSecurityKeys();
 			JOptionPane.showMessageDialog(null, message, "Error", JOptionPane.ERROR_MESSAGE);
 			LOGGER.severe(message);
 			System.exit(-1);
 		} else {
 			String message = String.format("SmartMarker expires %s", expDate);
 			LOGGER.info(message);
 			JOptionPane.showMessageDialog(null, message, "Information",
 					JOptionPane.INFORMATION_MESSAGE);
 		}
 
 		SmartMarkerDlg frame = new SmartMarkerDlg(null, "DSUK Smart Marker");
 		frame.addWindowListener(new WindowAdapter() {
 			@Override
 			public void windowClosing(WindowEvent e) {
 				LOGGER.info("System.exit(0)");
 				System.exit(0);
 			}
 		});
 
 		if (SmartMarkerDlg.DlgResult.PRODUCE_REPORT == frame.showModal()) {
 			String teacherAbsPath = frame.teacherAbsPath;
 			String dirStudent = frame.dirStudent;
 
 			if ((frame.reportType & ReportType.SUMMARY.value) > 0) {
 				IReport ir = null;
 				ir = new SummaryReport();
 				ir.Compare(teacherAbsPath, dirStudent);
 			}
 
 			{
 				Report r = null;
 
 				if ((frame.reportType & ReportType.FULL_HTML_CLASS.value) > 0) {
 					r = new ReportHTML();
 					r.verbosity = frame.verbosity;
 					r.includeImages = frame.includeImages;
 					r.Compare(teacherAbsPath, dirStudent);
 				}
 
 				if ((frame.reportType & ReportType.FULL_XML_CLASS.value) > 0) {
 					ReportXML x = new ReportXML();
 					r = x;
 					r.verbosity = frame.verbosity;
 					r.includeImages = frame.includeImages;
 					r.Compare(teacherAbsPath, dirStudent);
 				}
 
 				if ((frame.reportType & ReportType.FULL_HTML_INDIVIDUAL.value) > 0) {
 					r = new ReportHtmlIndividual();
 					r.verbosity = frame.verbosity;
 					r.includeImages = frame.includeImages;
 					r.Compare(teacherAbsPath, dirStudent);
 				}
 
 				if ((frame.reportType & ReportType.FULL_XML_INDIVIDUAL.value) > 0) {
 					ReportXML x = new ReportXmlIndividual();
 					r = x;
 					r.verbosity = frame.verbosity;
 					r.includeImages = frame.includeImages;
 					r.Compare(teacherAbsPath, dirStudent);
 				}
 			}
 
			System.exit(0);
 		} else {
 			LOGGER.info("No report produced.");
			System.exit(-1);
 		}
 	}
 }
