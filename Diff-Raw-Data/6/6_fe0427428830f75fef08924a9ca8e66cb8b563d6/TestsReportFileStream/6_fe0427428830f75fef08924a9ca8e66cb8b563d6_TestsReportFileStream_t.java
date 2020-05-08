 package framework.testng.report;
 
 import java.io.File;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.FilenameFilter;
 import java.io.IOException;
 
 import com.thoughtworks.xstream.XStream;
 import com.thoughtworks.xstream.io.xml.DomDriver;
 
 import framework.testng.report.xml.TestsReport;
 
 /**
  * @author moran
  */
 public class TestsReportFileStream {
 	
 	public void writeToFile(String outputDirectory, String fileName, TestsReport testsReport) {
 		File dir = new File(outputDirectory);
 		dir.mkdir();
 		
 		File file = new File(outputDirectory, fileName);
 		FileWriter fileWriter = null;
 		try {
 			fileWriter = new FileWriter(file);
 
 			XStream xStream = new XStream();
 			xStream.toXML(testsReport, fileWriter);
 
 		} catch (Exception e) {
 			throw new RuntimeException("could not write to " + file.getAbsolutePath(), e);
 		} finally {
 			if (fileWriter != null) {
 				try {
 					fileWriter.close();
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
 			}
 		}
 		
 	}
 	
 	public TestsReport readFromDirectory(String inputDirectory) {
 		inputDirectory = inputDirectory.replace("\\", "/");
 		File dir = new File(inputDirectory);
 		if (!dir.isDirectory() || !dir.exists()) {
 			throw new IllegalArgumentException("could not read from " + inputDirectory);
 		}
 		TestsReport combinedTestsReport = TestsReport.newEmptyReport();
 		String[] files = dir.list(new FilenameFilter() {
 			@Override
 			public boolean accept(File dir, String name) {
				return name.startsWith("sgtest-result");
 			}
 		});
 		for (String fileName : files) {
			System.out.println("fileName "+fileName);
		}
		for (String fileName : files) {
 			TestsReport testsReport = readFromFile(inputDirectory, fileName);
 			combinedTestsReport.getReports().addAll(testsReport.getReports());
 		}
 		
 		return combinedTestsReport;
 	}
 	
 	public TestsReport readFromFile(String inputDirectory, String fileName) {
 		inputDirectory = inputDirectory.replace("\\", "/");
 		File file = new File(inputDirectory, fileName);
 		FileReader fileReader = null;
 		try {
 			fileReader = new FileReader(file);
 			XStream xStream = new XStream(new DomDriver());
 			Object obj = xStream.fromXML(fileReader);
 			TestsReport testsReport = (TestsReport)obj;
 			return testsReport;
 		} catch(Exception e) {
 			throw new RuntimeException("could not read to " + file.getAbsolutePath(), e);
 		} finally {
 			if (fileReader != null) {
 				try {
 					fileReader.close();
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
 			}
 		}
 	}
 }
