 /*   
  * Copyright 2008-2010 Oleg Sukhoroslov
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *     http://www.apache.org/licenses/LICENSE-2.0
  *     
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package jlite.cli;
 
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Locale;
 
 import jlite.GridSession;
 import jlite.GridSessionConfig;
 import jlite.GridSessionFactory;
 
 import org.apache.commons.cli.CommandLine;
 import org.apache.commons.cli.CommandLineParser;
 import org.apache.commons.cli.GnuParser;
 import org.apache.commons.cli.HelpFormatter;
 import org.apache.commons.cli.OptionBuilder;
 import org.apache.commons.cli.Options;
 import org.apache.commons.cli.ParseException;
 import org.glite.jdl.JobAd;
 import org.glite.wsdl.types.lb.StatName;
 import org.glite.wsdl.types.lb.StateEnterTimesItem;
 
 public class JobStatus {
 
 	private static final String COMMAND = "job-status [options] <jobId> ...";
 	
 	public static void main(String[] args) {
 		System.out.println(); // extra line
 	    CommandLineParser parser = new GnuParser();
 	    Options options = setupOptions();
 	    HelpFormatter helpFormatter = new HelpFormatter();
 	    helpFormatter.setSyntaxPrefix("Usage: ");
 	    CommandLine line = null;
 		try {
 			line = parser.parse(options, args);
             if (line.hasOption("help")) {                
             	helpFormatter.printHelp(100, COMMAND, "\noptions:", options, "\n"+CLI.FOOTER, false);
             	System.out.println(); // extra line
                 System.exit(0);
             } else {
             	if (line.hasOption("xml")) {
 					System.out.println("<output>");
 				}
             	run(line.getArgs(), line);
             }
 		} catch (ParseException e) {
 			System.err.println(e.getMessage() + "\n");
             helpFormatter.printHelp(100, COMMAND, "\noptions:", options, "\n"+CLI.FOOTER, false);
             System.out.println(); // extra line
             System.exit(-1);
 		} catch (Exception e) {
 			if (line.hasOption("xml")) {
 				System.out.println("<error>" + e.getMessage() + "</error>");
 			} else {
 				System.err.println(e.getMessage());
 			}
 		} finally {
			if (line.hasOption("xml")) {
				System.out.println("</output>");
			}
 		}
 		System.out.println(); // extra line
 	}
 
 	private static Options setupOptions() {
         Options options = new Options();
                 
         options.addOption(OptionBuilder
                 .withDescription("displays usage")
                 .create("help"));
 
         options.addOption(OptionBuilder
         		.withArgName("file_path")
                 .withDescription("select JobId(s) from the specified file")
                 .hasArg()
                 .create("i"));
 
         options.addOption(OptionBuilder
                 .withArgName("proxyfile")
                 .withDescription("non-standard location of proxy cert")
                 .hasArg()
                 .create("proxypath"));
 
         options.addOption(OptionBuilder
         		.withArgName("xml")
                 .withDescription("output as xml")
                 .create("xml"));
 
 //        options.addOption(OptionBuilder
 //        		.withArgName("level")
 //                .withDescription("sets verbosity level of displayed information")
 //                .hasArg()
 //                .create("v"));
         
         return options;
 	}
 
 	private static void run(String[] jobIdArgs, CommandLine line) throws Exception {
 		GridSessionConfig conf = new GridSessionConfig();
 
 		if (line.hasOption("proxypath")) {
             conf.setProxyPath(line.getOptionValue("proxypath"));
         }
 		
 		GridSession grid = GridSessionFactory.create(conf);
 
 		List<String> jobIds = new ArrayList<String>();;
 		if (jobIdArgs.length > 0) {
 			for (String jobId : jobIdArgs) {
 				jobIds.add(jobId);
 			}
 		}
 		
 		if (line.hasOption("i")) {
 			for (String jobId : JobsSelector.select(line.getOptionValue("i"))) {
 				jobIds.add(jobId);
 			}
 		}
 		
 		if (jobIds.size() == 0){
 			throw new Exception("JobId(s) not found");
 		}
 		
 		for (String jobId : jobIds) {
 		
 			org.glite.wsdl.types.lb.JobStatus status = grid.getJobStatus(jobId);
 	        if (line.hasOption("xml")) {
 				System.out.println("<jobId>" + jobId + "</jobId>");
 			} else {
 				System.out.println("Status info for the job\n" + jobId + "\n");
 			}
 
 			if (line.hasOption("xml")) {
 				System.out.println("<status>" + status.getState().getValue() + "</status>");
 			} else {
 				System.out.print("Current status: " + status.getState().getValue());
 			}
 			if (status.getState().equals(StatName.DONE)) {
 				if (line.hasOption("xml")) {
 					System.out.println("<statusDoneCode>" + status.getDoneCode().getValue() + "</statusDoneCode>");
 				} else {
 					System.out.println(" (" + status.getDoneCode().getValue() + ")");
 				}
 			} else {
 				System.out.print("\n");
 			}
 
 			if (line.hasOption("xml")) {
 					System.out.println("<statusReason>" + status.getReason() + "</statusReason>");
 			} else {
 				System.out.println("Status reason: " + status.getReason());
 			}
 			
 			if (line.hasOption("xml")) {
 				System.out.println("<jobStatusHistory>");
 			} else {
 	        	System.out.println("\nJob state history:");
 	        }        
 	        StateEnterTimesItem[] states = status.getStateEnterTimes();
 	        SimpleDateFormat df = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z", Locale.ENGLISH);
 	        for (StateEnterTimesItem state : states) {
 	        	if (state.getTime().getTimeInMillis() != 0) {
 	        		if (line.hasOption("xml")) {
 						System.out.println("<historyEntry>");
 						System.out.println("<status>" + status.getState().getValue() + "</status>");
 						System.out.println("<time>" + df.format(state.getTime().getTime()) + "</time>");
 						System.out.println("</historyEntry>");
 					} else {
 	        			System.out.println("\t" + df.format(state.getTime().getTime()) + "\t" + state.getState().getValue());
 	        		}
 	        	}
 	        }
 	        if (line.hasOption("xml")) {
 				System.out.println("</jobStatusHistory>");
 			}
 
 	        if (line.hasOption("xml")) {
 	        	System.out.println("<destination>" + status.getDestination() + "</destination>");
 		        System.out.println("<cpuTime>" + status.getCpuTime() + "</cpuTime>");
 		        System.out.println("<exitCode>" + status.getExitCode() + "</exitCode>");
 	        } else {
 				System.out.println("\nDestination: " + status.getDestination());
 		        System.out.println("CPU Time: " + status.getCpuTime());
 		        System.out.println("Exit code: " + status.getExitCode());
 	        }
 	        
 	        org.glite.wsdl.types.lb.JobStatus[] children = status.getChildrenStates();
 	        if (children != null && children.length > 0) {
 	        	
 	        	 if (line.hasOption("xml")) {
 	        	 	System.out.println("<childrenStatus>");
 	        	 } else {
 	        		System.out.println("\nJob has " + children.length + " children **************************************** "); 	
 	        	 }
 		        for (org.glite.wsdl.types.lb.JobStatus child : children) {
 		        	if (line.hasOption("xml")) {
 	        	 		System.out.println("<child>");
 	        	 		System.out.println("<jobId>" + child.getJobId() + "</jobId>");
 	        	 	} else {
 		    			System.out.println("\nStatus info for the job\n" + child.getJobId() + "\n");
 		    		}
 		    		JobAd jdl = new JobAd(child.getJdl());
 		    		if (jdl.hasAttribute("NodeName")) {
 		    			if (line.hasOption("xml")) {
 	        	 			System.out.println("<nodeName>" + jdl.getString("NodeName") + "</nodeName>");
 	        	 		} else {
 		    				System.out.println("Node name: " + jdl.getString("NodeName"));
 		    			}
 		    		}
 
 		    		if (line.hasOption("xml")) {
 	        	 		System.out.println("<status>" + child.getState().getValue() + "</status>");
 	        	 	} else {
 		    			System.out.print("Current status: " + child.getState().getValue());
 		    		}
 
 		    		if (child.getState().equals(StatName.DONE)) {
 		    			if (line.hasOption("xml")) {
 	        	 			System.out.println("<statusDoneCode>" + child.getDoneCode().getValue() + "</statusDoneCode>");
 	        	 		} else {
 		    				System.out.println(" (" + child.getDoneCode().getValue() + ")");
 		    			}
 		    		} else {
 		    			System.out.print("\n");
 		    		}
 
 		    		if (line.hasOption("xml")) {
 	        	 		System.out.println("<statusReason>" + child.getReason() + "</statusReason>");
 	        	 	} else {
 		    			System.out.println("Status reason: " + child.getReason());
 		    		}
 		    		
 		    		if (line.hasOption("xml")) {
 		            	System.out.println("<jobStatusHistory>");
 		            } else {
 		            	System.out.println("\nJob state history:");
 		            }	        
 		            states = child.getStateEnterTimes();            
 		            for (StateEnterTimesItem state : states) {
 		            	if (state.getTime().getTimeInMillis() != 0) {
 							if (line.hasOption("xml")) {
 								System.out.println("<historyEntry>");
 								System.out.println("<status>" + status.getState().getValue() + "</status>");
 								System.out.println("<time>" + df.format(state.getTime().getTime()) + "</time>");
 								System.out.println("</historyEntry>");
 							} else {
 			        			System.out.println("\t" + df.format(state.getTime().getTime()) + "\t" + state.getState().getValue());
 			        		}		            	
 			        	}
 		            }
 
 		            if (line.hasOption("xml")) {
 		            	System.out.println("</jobStatusHistory>");
 		            }
 		            
 		            if (line.hasOption("xml")) {
 		            	System.out.println("<destination>" + child.getDestination() + "</destination>");
 		        		System.out.println("<cpuTime>" + child.getCpuTime() + "</cpuTime>");
 		        		System.out.println("<exitCode>" + child.getExitCode() + "</exitCode>");
 		            } else {
 		            	System.out.println("\nDestination: " + child.getDestination());
 		            	System.out.println("CPU Time: " + child.getCpuTime());
 		            	System.out.println("Exit code: " + child.getExitCode());
 		            }
 		    		
 		            if (line.hasOption("xml")) {
 	        	 		System.out.println("</child>");
 	        	 	} else {
 			        	System.out.println("\n-----------------------------------------------------------");
 			        }
 		        }
 		        
 	        }
 	        
 	        if (jobIds.size() > 1) {
 	        	if (line.hasOption("xml")) {
 	        		System.out.println("</childrenStatus>");
 		        } else {
 	        		System.out.println("\n******************************************************************\n");
 	        	}
 	        }
 		}
 
 	}
 	
 }
