package com.ronnie.qa.tools;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.regex.Pattern;
 import java.util.regex.Matcher;
 
 /*
  * Read incl file and process every line ignoring all jobs that are --> ejRunOpt='n'
  * Group disabled jobs in case we ever need them
  * Group jobs with dependencies -->ejJobDepend='3401 3017'; and recursively process until you hit independent jobs
  * Group jobs without dependencies
  * Group daily jobs and group hourly jobs which will be used when printing the jobs out
  *
  */
 public class JobProcessor {
 
 	/*
 	 * store jobs with dependencies using job id as key and value is array of
 	 * jobs. There are NO independent jobs in jobdep. There are NO disabled jobs in jobdep
 	 */
 	Map<String, List<String>> jobdep = new HashMap<String, List<String>>();
 
 	// store list of jobs without dependencies
 	List<String> independentjobslists = new LinkedList<String>();
 
 	// store list of jobs with dependencies. 
 	List<String> nonindependentjobslistslists = new LinkedList<String>();
 
 	// temporary storage of job dependencies
 	List<String> fulljoblist = new LinkedList<String>();
 	
 	// store disabled jobs
 	List<String> disabledjobslists = new LinkedList<String>();	
 	
 	// store lists of hourly jobs
 	List<String> hourlyjoblists = new LinkedList<String>();	
 	
 	// store lists of daily jobs
 	List<String> dailyjoblists = new LinkedList<String>();	
 
 	DateFormat dailyjobsdate = new SimpleDateFormat("yyyyMMdd");
 	DateFormat hourlyjobsdate = new SimpleDateFormat("yyyyMMddhh");
 	Date date = new Date();
 	
 	public static void main(String[] args) {
 
 		if (args.length < 1) {
			System.out.printf("usage: java com.ronnie.qa.tools.JobProcessor <completeInputFileName> [jobid|all]\n");
 			return;
 		} 
 		
 		String inputFile = args[0];
 		JobProcessor gdjr = new JobProcessor();
 		gdjr.readFromFileIdentifyJobs(inputFile);
 		
 		if (args[1].equalsIgnoreCase("all")) {
 			gdjr.listAllAssociatedJobs();
 			
 		} else if (args[1].matches("\\d\\d\\d\\d")) {
 			gdjr.fulljoblist = gdjr.listAllAssociatedJobs(args[1]);
 			//System.out.println(gdjr.fulljoblist.toString());
 			gdjr.printGivenJob(gdjr.fulljoblist);
 			
 		} else {
			System.out.printf("usage: java com.ronnie.qa.tools.JobProcessor <completeInputFileName> [jobid|all]\n");
 			return;
 		}
 
 
 
 	}
 
 	/*
 	 * a.) Read from file
 	 */
 	public void readFromFileIdentifyJobs(String inputFile) {
 		try {
 
 			FileReader fr = new FileReader(inputFile);
 			BufferedReader br = new BufferedReader(fr);
 			String devstr;
 			String outerjob = null;
 			String innerjob = null;
 			// looking for lines that matches "[0-9][0-9][0-9][0-9])" which is
 			// the line that has the job definition but you want to avoid
 			// comments like # MMEUS FACT_TRAFFIC_MON : 3280 - 3285 (MMEUS)
 			Pattern jobpattern = Pattern.compile("^\\W.+\\d\\d\\d\\d\\)");
 			// ejJobDepend="4138 4210";
 			Pattern innerjobpattern = Pattern.compile("\\d\\d\\d\\d");
 			// ejRunOpt='d'
 			Pattern disabledjobs = Pattern.compile("ejRunOpt='n'");
 			Pattern hourlyjob = Pattern.compile("ejRunOpt='h'");
 			Pattern dailyjob = Pattern.compile("ejRunOpt='d'");
 
 			while ((devstr = br.readLine()) != null) {
 				Matcher jobmatch = jobpattern.matcher(devstr);
 				Matcher innerjobmatch = innerjobpattern.matcher(devstr);
 				Matcher disabledjobmatch = disabledjobs.matcher(devstr);
 				Matcher hourlyjobmatch = hourlyjob.matcher(devstr);
 				Matcher dailyjobmatch = dailyjob.matcher(devstr);
 				
 
 				List<String> jobarray = new LinkedList<String>();
 				int i = 0; // counter for jobs with dependencies. resets every
 							// line.
 
 				if (devstr.isEmpty()) {
 					// empty line, do nothing
 				} else if (jobmatch.lookingAt()) {
 					// System.out.println(devstr);
 
 					// check for disabled jobs. we don't want it.
 					if (!disabledjobmatch.find()) {
 
 						while (innerjobmatch.find()) {
 							// System.out.println(innerjobmatch.group().trim());
 							if (i == 0) {
 								// use job xxxx) as the key
 								outerjob = innerjobmatch.group().trim();
 								//keep a list of jobs that run hourly or daily.
 								if (hourlyjobmatch.find() && !hourlyjoblists.contains(outerjob)){
 									hourlyjoblists.add(outerjob);
 								} else if (dailyjobmatch.find() && !dailyjoblists.contains(outerjob)){
 									dailyjoblists.add(outerjob);
 								} else {
 									
 								}
 								
 								
 							} else {
 								innerjob = innerjobmatch.group().trim();
 								// store the inner job dependencies in to
 								// jobarray
 								jobarray.add(innerjob);
 							}
 							i++;
 						}
 
 						// if i is only 1 or 0 then there is no dependent jobs
 						if (i < 2) {
 							// add independent jobs found while looking at
 							// innerjobs and only add if one doesn't exist
 							// already
 							if (innerjob != null
 									&& !independentjobslists.contains(innerjob)) {
 								independentjobslists.add(innerjob);
 							} else {
 								// if we don't find innerjobmatch then this must
 								// be an independent job
 								if (!independentjobslists.contains(jobmatch
 										.group(0)
 										.substring(0,
 												jobmatch.group(0).length() - 1)
 										.trim())) {
 									independentjobslists
 											.add(jobmatch
 													.group(0)
 													.substring(
 															0,
 															jobmatch.group(0)
 																	.length() - 1)
 													.trim());
 								}
 							}
 						} else {
 							if (outerjob != null) {
 								jobdep.put(outerjob, jobarray);
 								nonindependentjobslistslists.add(outerjob);
 							}
 						}
 						// reset
 						innerjob = null;
 						outerjob = null;
 
 					} else {
 						// job is disabled, do nothing or in future assign to
 						// disabled job arraylist
 						//System.out.println("Disabled: " + jobmatch.group(0).substring(0,jobmatch.group(0).length() - 1).trim());
 						disabledjobslists.add(jobmatch.group(0).substring(0,jobmatch.group(0).length() - 1).trim());
 					}
 
 				} else {
 					// ignore non related lines
 				}
 			}
 			fr.close();
 			br.close();
 
 		} catch (IOException ex) {
 			System.out.println(ex);
 		}
 		return;
 	}
 
 
 
 	/*
 	 * Check if the jobs in an independent job.
 	 */
 	public boolean checkindependentjobs(String cj) {
 		boolean result = false;
 		if (independentjobslists.contains(cj)) {
 			result = true;
 		}
 		return result;
 	}
 
 	/*
 	 * for every non dependent job, identify its dependencies and sub
 	 * dependencies and drill down.
 	 */
 	public void listAllAssociatedJobs() {
 		List<String> thejoblist = new LinkedList<String>();
 		for (Map.Entry<String, List<String>> entry : jobdep.entrySet()) {
 			String key = entry.getKey();
 			if (key != null) {
 				thejoblist = listAllAssociatedJobs(key);
 				//System.out.println(key + ": " + thejoblist.toString());
 				printGivenJob(thejoblist);
 				// empty the linkedlist
 				fulljoblist.clear();
 
 			}
 		}
 	}
 
 	/*
 	 * for a given jobid, lists its dependencies and their
 	 * dependencies and drill down.
 	 */
 	public List<String> listAllAssociatedJobs(String jobid) {
 
 		if (jobdep.containsKey(jobid)) {
 			List<String> value = jobdep.get(jobid);
 			for (String m : value) {
 				//if not and independent job, drill down until you hit independent job
 				if (m != null && !checkindependentjobs(m)) {
 					if (!fulljoblist.contains(m)){ // check for duplicates. no reason to run job twice. but if u r debugging, remove this.
 						fulljoblist.add(m);
 					}
 					listAllAssociatedJobs(m);
 					
 				//if independent job then its the base case, add to list and be done	
 				} else if (m != null && checkindependentjobs(m)) {
 					if (!fulljoblist.contains(m)){// check for duplicates. no reason to run job twice. but if u r debugging, remove this.
 						fulljoblist.add(m);
 					}	
 				} else {
 					//do nothing
 				}
 			}
 		}
 		return fulljoblist;
 	}
 	
 	
 	public void printGivenJob(List<String> printlist){
 		String printthis = null;
 		
 		for (String m : printlist) {
 			if (dailyjoblists.contains(m)) {
 				printthis = "./cfi_etl.sh -j " + m + "-t" + dailyjobsdate.format(date) + " -rf -fn";
 				System.out.println(printthis);
 				
 				
 			} else if (hourlyjoblists.contains(m)) {
 				printthis = "./cfi_etl.sh -j " + m + "-t" + hourlyjobsdate.format(date) + " -rf -fn";
 				System.out.println(printthis);
 			} else {
 				//System.out.print("Problems in method printGivenJob with input value of " + m + "\n");
 			}
 			
 			
 		}
 		
 	}
 
 }
