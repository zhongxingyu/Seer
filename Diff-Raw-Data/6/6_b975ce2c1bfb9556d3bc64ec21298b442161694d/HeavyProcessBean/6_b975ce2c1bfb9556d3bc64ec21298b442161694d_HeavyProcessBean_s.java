 package edu.sjsu.carbonated.server;
 
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 
 import javax.ejb.Schedule;
 import javax.ejb.Stateless;
 import javax.ejb.Timer;
 
 /**
  * @author Session Bean implementation class HeavyProcessBean
  */
 @Stateless
 public class HeavyProcessBean implements HeavyProcessBeanRemote {
 
 	/**
 	 * Default constructor.
 	 */
 	public HeavyProcessBean() {
 	}
 
 	public String remoteJSONConvert(String filename) {
 
 		return null;
 
 	}
 
 	// The main
 	@Schedule(minute = "*/2", hour = "*")
 	public void TimeHandler(Timer timer) {
 		// this is ran for as long as the ejb is deployed
 		System.out.println("QPB 12345 is pulling jobs from database...");
 
 		// go pull jobs from database
 		pullJobs();
 
 		String jobId = "5";
 		String stockName = "AA";
 		String stockRequest = "AA|Alcoa Inc. Common Stock|N|AA|N|100|N|AA";
 
 		// make a directory for each unique jobId
 		makeClientDirectory(jobId);
 
 		// call Perlexecute
 		getCSV(jobId, stockRequest);
 
 		// generate JSON
 		CSVtoJSON csvToJson = new CSVtoJSON();
 
 		String jsonResult = csvToJson.convertCSVFile(jobId + "/historic/"
 				+ stockName + ".dat");
 
 		System.out.println(jsonResult);
 
 		// File writer for json to a file
 
 		try {
 
			FileWriter file = new FileWriter(
					"/jboss.server.temp.dir/jobId.json");
 			file.write(jsonResult.toString());
			file.flush();
 			file.close();
 
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 
 		System.out.print(jsonResult);
 
 	}
 
 	// put back into the database
 	private void setClientJobStatusToDone() {
 
 	}
 
 	/**
 	 * Will pull x amount of jobs from the database
 	 */
 	private void pullJobs() {
 
 		// Will take all jobs that are in the Pending state and are for QPB
 		// Will change those jobs to In Progress state
 
 	}
 
 	/**
 	 * Will make a temporary directory in jboss' tmp directory for each job
 	 * 
 	 * @param takes
 	 *            the jobId as the directory name to make
 	 * @return True on success, false on failure to create directory
 	 */
 	private boolean makeClientDirectory(String jobId) {
 
 		return new File(System.getProperty("jboss.server.temp.dir") + "/"
 				+ jobId).mkdir();
 
 	}
 
 	/**
 	 * Will use perl script to get dat files from yahoo finance
 	 */
 	private void getCSV(String jobId, String stockName) {
 
 		String dummyStr = "AADR|WCM BNY Mellon Focused Growth ADR ETF|P|AADR|Y|100|N|AADR\n";
 
 		// write the company_symbols.dat file that the perl script needs
 		try {
 			FileWriter fout = new FileWriter(
 					System.getProperty("jboss.server.temp.dir") + "/" + jobId
 							+ "/company_symbols.dat");
 			fout.write(dummyStr); // to fix perl bug
 			fout.write(stockName + "\n");
 			fout.close();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		PerlExecute.runperl("perl "
 				+ System.getProperty("jboss.server.data.dir")
 				+ "/stock_data.pl -h "
 				+ System.getProperty("jboss.server.temp.dir") + "/" + jobId);
 
 	}
 
 	/*
 	 * private void setClientJobStatusToDone(){
 	 * 
 	 * }
 	 */
 
 }
