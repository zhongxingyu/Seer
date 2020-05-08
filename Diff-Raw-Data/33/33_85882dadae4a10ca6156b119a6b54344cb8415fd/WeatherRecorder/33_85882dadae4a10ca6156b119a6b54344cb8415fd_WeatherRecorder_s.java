 package org.marcus.weather;
 
 import java.io.BufferedReader;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.net.UnknownHostException;
 import java.nio.Buffer;
 import java.sql.SQLException;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Date;
 
 public class WeatherRecorder {
 
 	private static class EarlyTerminationException extends Exception {
 		private static final long serialVersionUID = 7107342102877398736L;
 	}
 
 	private static final String LOG_NAME = "log.txt";
 	private static final String ZIPS_FILE = "zips.txt";
 	private static final String ERROR_NAME = "error.txt";
 	private static boolean debug = false;
 	private static boolean pastOnly = false;
 	private static Date startDate = null;
 
 	public static void main(String[] args) {
 		CheckKeyboard ck = new CheckKeyboard();
 		Thread thread = new Thread(ck);
 		thread.start();
 		boolean useDB = true;
 		if (args.length > 0) {
 			for (int i = 0; i < args.length; i++) {
 				if (args[i].equals("-csv")) {
 					useDB = false;
 				} else if (args[i].equals("help")) {
 					System.out.println("Arguments are:"
 							+ System.lineSeparator()
 							+ "-csv to write to csv files"
							+ System.lineSeparator() + "-d to debug");
 					System.exit(0);
 				} else if (args[i].equals("-d")) {
 					debug = true;
 				} else if (args[i].startsWith("-past")) {
 					pastOnly = true;
 					SimpleDateFormat format = getYMDFormatter();
 					try {
 						startDate = format.parse(args[i].substring(5));
 					} catch (ParseException e) {
						// TODO Auto-generated catch block
 						System.out
 								.println("Date must be in format 'yyyy-mm-dd'");
 						System.exit(0);
 					}
 				} else {
 					System.out.println("Arguments are:"
 							+ System.lineSeparator()
 							+ " -csv to write to csv files"
 							+ System.lineSeparator() + "-d to debug"
							+ System.lineSeparator() + "-pastYYYY-MM-DD");
 					System.exit(0);
 				}
 			}
 		}
 		if (debug)
 			System.out.println(1);
 		try {
 			String[] zips = null;
 			try {
 				if (pastOnly) {
 					zips = getZips();
 				} else {
					if (hasRunToday()) {
 						System.out.println("Already run today");
 						System.exit(0);
 					}
 					zips = getZips();
 				}
 			} catch (IOException e) {
 				System.exit(1);
 			}
 			int i = 0;
 
 			if (debug)
 				System.out.println(2);
 
 			DBStore db = null;
 			if (useDB)
 				db = new DBStore();
 			CSVStore csv = null;
 			if (!useDB)
 				csv = new CSVStore();
 
 			if (debug)
 				System.out.println(3);
 
 			try {
 				if (useDB)
 					db.open();
 			} catch (Exception e) {
 				FileWriter fileWriter;
 				fileWriter = new FileWriter(LOG_NAME, false);
 				PrintWriter out = new PrintWriter(fileWriter, true);
 				SimpleDateFormat format = getYMDFormatter();
 				out.print("error " + format.format(new Date()));
 
 				out.close();
 				printError(e, zips[i]);
 				System.exit(2);
 			}
 
 			if (debug)
 				System.out.println(4);
 
 			try {
 				DataFetcher df = new DataFetcher(debug);
 				for (i = 0; i < zips.length; i++) {
 					System.out.println(zips[i]);
 					if (!pastOnly) {
 						df.load(zips[i]);
 						if (df.valid && useDB) {
 							if (!pastOnly) {
 								db.storeForecast(df.forecast1);
 								db.storeForecast(df.forecast3);
 								db.storePast(df.past);
 							}
 							db.commit();
 						} else if (df.valid && !useDB) {
 							csv.storeForecast(df.forecast1);
 							csv.storeForecast(df.forecast3);
 							csv.storePast(df.past);
 						}
 						if (ck.isStopProgram()) {
 							i++;
 							throw new EarlyTerminationException();
 						}
 					} else {
 						// if pastOnly cycle through the dates up till yesterday
 						Calendar cal = Calendar.getInstance();
 						Date today = cal.getTime();
 						cal.setTime(startDate);
 						while (cal.getTime().before(today)) {
 							Date currentDate = cal.getTime();
 							if (debug)
 								System.out
 										.println(getYMDFormatter().format(
 												currentDate)
 												+ " "
 												+ getYMDFormatter().format(
 														today));
 							df.loadPast(zips[i], currentDate);
 							db.storePast(df.past);
 							cal.setTimeInMillis(cal.getTimeInMillis() + 3600 * 24 * 1000);
 						}
 					}
 				}
 
 				if (debug)
 					System.out.println(5);
 
 				if (useDB)
 					db.close();
 				FileWriter fileWriter = new FileWriter(LOG_NAME, false);
 				PrintWriter out = new PrintWriter(fileWriter, true);
 				out.print("ok " + getYMDFormatter().format(new Date()));
 				out.close();
 				fileWriter.close();
 				System.exit(0);
 			} catch (Exception e) {
 				FileWriter fileWriter;
 				try {
 					if (useDB) {
 						db.commit();
 						db.close();
 					}
 					fileWriter = new FileWriter(LOG_NAME, false);
 					PrintWriter out = new PrintWriter(fileWriter, true);
 
 					SimpleDateFormat format = getYMDFormatter();
 
 					out.print("error " + format.format(new Date()) + " "
 							+ zips[i]);
 
 					out.close();
 					fileWriter.close();
 					printError(e, zips[i]);
 				} catch (IOException e1) {
 					e1.printStackTrace();
 				} catch (SQLException e1) {
 					e1.printStackTrace();
 				}
 				System.exit(3);
 			}
 		} catch (IOException e) {
 			e.printStackTrace();
 			System.exit(4);
 		}
 
 	}
 
 	private static boolean hasRunToday() throws IOException {
 		BufferedReader br = new BufferedReader(new FileReader(LOG_NAME));
 		String line = br.readLine();
 		if (line == null) {
 			return false;
 		}
 		int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
 		String[] pieces = line.split(" ");
 		if (pieces.length == 2 && pieces[0].equals("ok")) {
 			String now = getYMDFormatter().format(new Date());
 			if (pieces[1].equals(now)) {
 				return true;
 			} else if (hour < 4) {
 				return true;
 			}
 		} else if (pieces[0].equals("stop")) {
 			return true;
 		}
 		return false;
 	}
 
 	private static String[] getZips() throws IOException {
 		String lastZip = "";
 		BufferedReader br = new BufferedReader(new FileReader(LOG_NAME));
 		String line = br.readLine();
 		if (line == null) {
 			lastZip = "";
 		} else {
 			String[] splitted = line.split(" ");
 
 			if (splitted.length > 2) {
 				lastZip = splitted[2];
 			} else if (splitted[1].equals(getYMDFormatter().format(new Date()))) {
 				lastZip = "";
 			}
 		}
 
 		boolean pastLastZip = false;
 		br = new BufferedReader(new FileReader(ZIPS_FILE));
 		line = "";
 		String read = br.readLine();
 		while (read != null) {
 			if (lastZip.isEmpty()) {
 				if (line.isEmpty()) {
 					line = read;
 				} else {
 					line = line + " " + read;
 				}
 			} else if (!lastZip.isEmpty() && pastLastZip) {
 				if (line.isEmpty()) {
 					line = read;
 				} else {
 					line = line + " " + read;
 				}
 			} else if (!lastZip.isEmpty() && !pastLastZip) {
 				if (read.equals(lastZip)) {
 					pastLastZip = true;
 					line = read;
 				}
 			}
 			read = br.readLine();
 		}
 		return line.split(" ");
 	}
 
 	private static SimpleDateFormat getYMDFormatter() {
 		return new SimpleDateFormat("yyyy-MM-dd");
 	}
 
 	private static SimpleDateFormat getDateFormatter() {
 		return new SimpleDateFormat("yyyy-MM-dd'T'kk:mm:ss.SSS");
 	}
 
 	private static void printError(Exception e, String zip) throws IOException {
 		FileWriter fileWriter;
 		PrintWriter out;
 		fileWriter = new FileWriter(ERROR_NAME, true);
 		out = new PrintWriter(fileWriter, true);
 
 		out.print("error " + getDateFormatter().format(new Date()) + " " + zip);
 		out.println();
 		e.printStackTrace(out);
 		out.print(e.getMessage());
 		out.println();
 		out.close();
 		fileWriter.close();
 	}
 
 }
