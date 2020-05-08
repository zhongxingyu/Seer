 package deployer.report;
 
 import deployer.report.xml.SummaryReport;
 import deployer.report.xml.TestsReport;
 
 public class TestsReportMerger {
 
 	/**
 	 * args[0] - suite name
 	 * args[1] - input directory path
 	 * args[2] - output directory path
 	 */
 	public static void main(String[] args) {
		String suiteName = args[0];
 		String inputDirectory = args[1];
 		String outputDirectory = args[2];
         System.out.println("reading reports from: " + inputDirectory);
 
 		TestsReportFileStream fileStream = new TestsReportFileStream();
         TestsReport testsReport = fileStream.readFromDirectory(inputDirectory);
 		testsReport.setSuiteName(suiteName);
 		
 		String fileName = "sgtest-results.xml";
 		fileStream.writeToFile(outputDirectory, fileName, testsReport);
         System.out.println("generating combined report to: " + outputDirectory+"/"+fileName);
        System.out.println(new SummaryReport(testsReport).toString());
 	}
 
 }
