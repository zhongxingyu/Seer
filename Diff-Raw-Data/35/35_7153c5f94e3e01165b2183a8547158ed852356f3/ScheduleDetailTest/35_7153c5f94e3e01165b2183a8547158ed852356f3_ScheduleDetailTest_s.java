 package net.kevxu.purdueassist.test;
 
 import java.io.IOException;
 
 import net.kevxu.purdueassist.course.ScheduleDetail;
 import net.kevxu.purdueassist.course.ScheduleDetail.ScheduleDetailEntry;
 import net.kevxu.purdueassist.course.ScheduleDetail.ScheduleDetailListener;
 import net.kevxu.purdueassist.course.elements.Predefined.Term;
 import net.kevxu.purdueassist.course.shared.CourseNotFoundException;
 import net.kevxu.purdueassist.course.shared.HttpParseException;
 import net.kevxu.purdueassist.course.shared.RequestNotFinishedException;
 
 import org.apache.commons.cli.CommandLineParser;
 import org.apache.commons.cli.GnuParser;
 import org.apache.commons.cli.HelpFormatter;
 import org.apache.commons.cli.Options;
 import org.apache.commons.cli.ParseException;
 
 public class ScheduleDetailTest {
 
 	public static final String VERSION = "0.1.2";
 
 	private static final HelpFormatter formatter = new HelpFormatter();
 	private static final Options options = new Options();
 
 	public static void main(String[] args) throws InterruptedException {
 		options.addOption("t", "term", true,
 				"full name (without space) for school term. i.e. fall2012 (optional)");
 		options.addOption("s", "small-silent", false,
 				"Do not print input information.");
 		options.addOption("S", "slient", false, "Do not print anything.");
 		options.addOption("p", "parallel", false,
 				"Process all the search requests parallely.");
 
 		CommandLineParser parser = new GnuParser();
 		org.apache.commons.cli.CommandLine cmd;
 		try {
 			if (args.length <= 0) {
 				printHelp(formatter, options);
 
 				System.exit(10);
 			} else {
 				cmd = parser.parse(options, args);
 
 				String termString;
 				if (!cmd.hasOption("t")) {
 					termString = "CURRENT";
 				} else {
 					termString = cmd.getOptionValue("t");
 				}
 				final Term term = parseTerm(termString);
 				final boolean silent = cmd.hasOption("S");
 				final boolean smallSilent = cmd.hasOption("s");
 				final String[] crns = cmd.getArgs();
 				final boolean parallel = cmd.hasOption("p");
 
 				if (parallel) {
 					// parallel
 					for (final String crnString : crns) {
 						final int crn = Integer.valueOf(crnString);
 						ScheduleDetail detail = new ScheduleDetail(
 								new ScheduleDetailListener() {
 
 									@Override
 									public void onScheduleDetailFinished(
 											CourseNotFoundException e) {
 										if (!silent) {
 											if (!smallSilent)
												System.out.println("INPUT: "
 														+ crnString + " "
 														+ term);
											System.out
 													.println("Course Not Found: "
 															+ e.getMessage());
 										}
 									}
 
 									@Override
 									public void onScheduleDetailFinished(
 											HttpParseException e) {
 										if (!silent) {
 											if (!smallSilent)
												System.out.println("INPUT: "
 														+ crnString + " "
 														+ term);
											System.out.println("Parse Error: "
 													+ e.getMessage());
 										}
 									}
 
 									@Override
 									public void onScheduleDetailFinished(
 											IOException e) {
 										if (!silent) {
 											if (!smallSilent)
												System.out.println("INPUT: "
 														+ crnString + " "
 														+ term);
											System.out.println("IO Error: "
 													+ e.getMessage());
 										}
 									}
 
 									@Override
 									public void onScheduleDetailFinished(
 											ScheduleDetailEntry entry) {
 										if (!silent) {
 											if (!smallSilent)
												System.out.println("INPUT: "
 														+ crnString + " "
 														+ term);
 											System.out.println(entry);
 										}
 									}
 
 									@Override
 									public void onScheduleDetailFinished(
 											Exception e) {
 										if (!silent) {
 											if (!smallSilent)
												System.out.println("INPUT: "
 														+ crnString + " "
 														+ term);
 											e.printStackTrace();
 										}
 									}
 								});
 						detail.getResult(term, crn);
 					}
 				} else {
 					// Not parallel
 					ScheduleDetail detail = new ScheduleDetail(
 							new ScheduleDetailListener() {
 
 								@Override
 								public void onScheduleDetailFinished(
 										CourseNotFoundException e) {
 									if (!silent) {
										System.out.println("Course Not Found: "
 												+ e.getMessage());
 									}
 								}
 
 								@Override
 								public void onScheduleDetailFinished(
 										HttpParseException e) {
 									if (!silent) {
										System.out.println("Parse Error: "
 												+ e.getMessage());
 									}
 								}
 
 								@Override
 								public void onScheduleDetailFinished(
 										IOException e) {
 									if (!silent) {
										System.out.println("IO Error: "
 												+ e.getMessage());
 									}
 								}
 
 								@Override
 								public void onScheduleDetailFinished(
 										ScheduleDetailEntry entry) {
 									if (!silent) {
 										System.out.println(entry);
 									}
 								}
 
 								@Override
 								public void onScheduleDetailFinished(Exception e) {
 									if (!silent) {
 										e.printStackTrace();
 									}
 								}
 							});
 					for (final String crnString : crns) {
 						while (!detail.isRequestFinished()) {
 							Thread.sleep(100);
 						}
 
 						final int crn = Integer.valueOf(crnString);
 
 						if (!smallSilent)
							System.out.println("INPUT: " + crnString + " "
 									+ term);
 
 						detail.getResult(term, crn);
 					}
 				}
 			}
 
 		} catch (ParseException e) {
 			System.err
 					.println("Command line arguments parsing failed. Reason: "
 							+ e.getMessage());
 			printHelp(formatter, options);
 		} catch (IllegalArgumentException e) {
 			System.err.println("No such school term.");
 			printHelp(formatter, options);
 		} catch (RequestNotFinishedException e1) {
 			e1.printStackTrace();
 		}
 	}
 
 	private static void printHelp(HelpFormatter formatter, Options options) {
 		formatter.printHelp(
 				"java -jar RemainSeats.jar [options] [crn1 [crn2 [crn3] ...]]",
 				options);
 	}
 
 	private static Term parseTerm(String termString) {
 		return Term.valueOf(termString.toUpperCase());
 	}
 }
