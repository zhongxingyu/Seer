 package com.ohsaka;
 
 /*
  * Copyright (C) 2009 Ohsaka
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 import java.io.BufferedInputStream;
 import java.io.BufferedOutputStream;
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Enumeration;
 import java.util.List;
 import java.util.zip.Deflater;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipFile;
 import java.util.zip.ZipOutputStream;
 
 import org.jf.testsign.TestSign;
 
 public class ColorChange {
 
 	/**
 	 * @param args
 	 */
 	public final static String VERSION = "v1.0";
 	public final static char[] validChars = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };
 	public final static String TEMPDIR = System.getProperty("java.io.tmpdir") + "cc-temp";
 	public final static String SLASH = System.getProperty("file.separator");
 	public final static String NL = "\n"; // use unix style even on windows
 	public final static boolean debug = false;
 
 	public static void main(String[] args) {
 		System.out.println("\n************************************\n**  Color Changer " + VERSION + " by Ohsaka  **\n************************************\n");
 
 		if (args.length < 2 || args.length > 10) {
 			System.out.println("Usages: ");
 			System.out.println("java -jar colorChange.jar <theme.zip> [ticker] [clock] [date] [ongoing] [latest] [none] [clear] [network] [roam]\n");
 			System.out.println("< > means required, [ ] means optional");
 			System.out.println("\t<theme.zip> - Android theme update file (not a full ROM update)");
 			System.out.println("\t[ticker] - ARGB color for ticker notifications on the status bar");
 			System.out.println("\t\tDefault: FF000000");
 			System.out.println("\t[clock] - ARGB color for the clock text");
 			System.out.println("\t\tDefault: FF000000");
 			System.out.println("\t[date] - ARGB color for the date");
 			System.out.println("\t\tDefault: FF000000");
 			System.out.println("\t[ongoing] - ARGB color for \"Ongoing\" Label");
 			System.out.println("\t\tDefault: FFFFFFFF");
 			System.out.println("\t[latest] - ARGB color for \"Notifications\" Label");
 			System.out.println("\t\tDefault: FFFFFFFF");
 			System.out.println("\t[none] - ARGB color for \"No Notifications\" Label");
 			System.out.println("\t\tDefault: FFFFFFFF");
 			System.out.println("\t[clear] - ARGB color for text on Clear Notifications button");
 			System.out.println("\t\tDefault: FF000000");
 			System.out.println("\t[network] - ARGB color for network name (T-Mobile usually)");
 			System.out.println("\t\tDefault: FF000000");
 			System.out.println("\t[roam] - Not sure where this color shows up, possibly network name when roaming");
 			System.out.println("\t\tDefault: FF000000");
 			System.out.println("");
 			System.out.println("Example 1) java -jar colorChange.jar themeXYZ.zip ticker=FFFF0000 date=FFFF0000 clock=FFFF0000");
 			System.out.println("\tThis will change ticker, date, and clock to red\n");
 			System.out.println("Example 2) java -jar colorChange.jar themeXYZ.zip clear=FFFFFFFF network=FF0000FF");
 			System.out.println("\tThis will change the clear notifications button text to white and network name (usually T-Mobile) to blue");
 			return;
 		}
 
 		// check RGB value(s)
 		for (int i = 1; i < args.length; i++) {
 			String currentArg = args[i].substring(0, args[i].indexOf("="));
 			String currentValue = args[i].substring(args[i].indexOf("=") + 1);
 			if (debug) {
 				System.out.println("currentArg=" + currentArg);
 				System.out.println("currentValue=" + currentValue);
 			}
 
 			if (currentValue.length() != 8) {
 				System.out.println("Error: " + currentArg + " - AARRGGBB should be 8 characters (dont include # or anything)");
 				return;
 			}
 			for (int j = 0; j < 8; j++) {
 				if (!isValidChar(currentValue.charAt(j))) {
 					System.out.println("Error: " + currentArg + " - " + currentValue.charAt(j) + " is not valid character for AARRGGBB (0-9, A-F; not case sensitive)");
 					return;
 				}
 			}
 		}
 
 		// check file arg
 		File updateFile = new File(args[0]);
 		if (!updateFile.exists()) {
 			System.out.println("Error: file:" + args[0] + " does not exist");
 			return;
 		}
 		if (!updateFile.canRead()) {
 			System.out.println("Error: file:" + args[0] + " can not be read");
 			return;
 		}
 
 		// set default color values, then parse command line arguments to
 		// override them
 		String tickerColor = "FF000000";
 		String clockColor = "FF000000";
 		String dateColor = "FF000000";
 		String ongoingColor = "FFFFFFFF";
 		String latestColor = "FFFFFFFF";
 		String noneColor = "FFFFFFFF";
 		String clearColor = "FF000000";
 		String networkColor = "FF000000";
 		String roamColor = "FF000000";
 
 		String currentArg, currentValue;
 		boolean tickerChanged = false;
 		boolean servicesChanged = false;
 		for (int i = 1; i < args.length; i++) {
 			currentArg = args[i].substring(0, args[i].indexOf("="));
 			currentValue = args[i].substring(args[i].indexOf("=") + 1);
 			if (currentArg.equalsIgnoreCase("ticker")) {
 				tickerColor = currentValue;
 				tickerChanged = true;
 			} else if (currentArg.equalsIgnoreCase("clock")) {
 				clockColor = currentValue;
 				servicesChanged = true;
 			} else if (currentArg.equalsIgnoreCase("date")) {
 				dateColor = currentValue;
 				servicesChanged = true;
 			} else if (currentArg.equalsIgnoreCase("ongoing")) {
 				ongoingColor = currentValue;
 				servicesChanged = true;
 			} else if (currentArg.equalsIgnoreCase("latest")) {
 				latestColor = currentValue;
 				servicesChanged = true;
 			} else if (currentArg.equalsIgnoreCase("none")) {
 				noneColor = currentValue;
 				servicesChanged = true;
 			} else if (currentArg.equalsIgnoreCase("clear")) {
 				clearColor = currentValue;
 				servicesChanged = true;
 			} else if (currentArg.equalsIgnoreCase("network")) {
 				networkColor = currentValue;
 				servicesChanged = true;
 			} else if (currentArg.equalsIgnoreCase("roam")) {
 				roamColor = currentValue;
 				servicesChanged = true;
 			} else {
 				System.out.println("ERROR: Don't recognize argument: " + currentArg + " (check your spelling)");
 				System.exit(1);
 			}
 		}
 
 		// make working temp directory
 		File tempDir = new File(TEMPDIR);
 		if (tempDir.exists()) {
 			// tempDir exists from previous attempt, delete it
 			if (!deleteDir(tempDir)) {
 				System.out.println("Error deleting old temp directory: " + TEMPDIR + "\n\nDelete it yourself first");
 			}
 		}
 		if (!tempDir.mkdir()) {
 			System.out.println("Error creating temp directory: " + TEMPDIR);
 			return;
 		} else {
 			System.out.println("\nMade temp directory: " + TEMPDIR);
 		}
 
 		// unzip update file
 		if (!unzip(updateFile, tempDir)) {
 			System.out.println("Error unzipping file :(");
 			return;
 		}
 
 		// make sure its not a full ROM
 		File bootFile = new File(TEMPDIR + SLASH + "boot.img");
 		if (bootFile.exists()) {
 			System.out.println("Error: this appears to be a full ROM update, not a theme update");
 			return;
 		}
 
 		// don't bother unzipping services unless it needs to be changed
 		if (servicesChanged) {
 			// check for services.jar
 			File servicesFile = new File(TEMPDIR + SLASH + "framework" + SLASH + "services.jar");
 			if (!servicesFile.exists()) {
 				System.out.println("Error: didn't find services.jar");
 				return;
 			}
 
 			// modify services.jar
 			modifyServicesColor(servicesFile, clockColor, dateColor, ongoingColor, latestColor, noneColor, clearColor, networkColor, roamColor);
 		} else {
 			System.out.println("\nNo changes needed to services.jar, skipping it");
 		}
 
 		// don't bother unzipping framework-res.apk unless it needs to be
 		// changed
 		if (tickerChanged) {
 			// check for framework-res.apk
 			File frameworkFile = new File(TEMPDIR + SLASH + "framework" + SLASH + "framework-res.apk");
 			if (!frameworkFile.exists()) {
 				System.out.println("Error: didn't find framework-res.apk");
 				return;
 			}
 
 			// modify framework-res.apk
 			modifyFrameworkColor(frameworkFile, tickerColor.toUpperCase());
 		} else {
 			System.out.println("\nNo change to ticker color, skipping framework-res.apk\n");
 		}
 
 		// zip new update file back up
 		zip(updateFile, tempDir, TEMPDIR, true);
 
 		// Resign the update file
 		TestSign.main(new String[] { getFileNameWithoutExtension(updateFile
 				.getAbsolutePath())
 				+ "-new.zip" });
 		System.out.println("Finished resigning update file\n");
 
 		System.out.println("\nAutomagic complete! Enjoy.  -- Ohsaka");
 	}
 
 	private static void modifyServicesColor(File servicesFile, String clockColor, String dateColor, String ongoingColor, String latestColor, String noNotifsColor, String clearButTextColor, String networkNameColor, String roamingColor) {
 		System.out.println("\n**** BEGIN SERVICES.JAR *******\n");
 		File servicesDir = new File(servicesFile.getParent() + SLASH + "services");
 		unzip(servicesFile, servicesDir);
 		File classesDex = new File(servicesDir + SLASH + "classes.dex");
 		if (classesDex.exists()) {
 			System.out.println("found classes.dex");
 		}
 
 		System.out.println("un-dexing...");
 		// Un-dex services.jar
 		org.jf.baksmali.main.main(new String[] { "-o",
 				servicesDir.getAbsolutePath(), classesDex.getAbsolutePath() });
 		System.out.println("Finished un-dexing classes.dex\n");
 
 		// Edit the StatusBarIcon.smali file
 		BufferedReader bufRead = null;
 		BufferedWriter bufWrite = null;
 		String inputFilename = servicesDir.getAbsolutePath() + SLASH + "com" + SLASH + "android" + SLASH + "server" + SLASH + "status" + SLASH + "StatusBarIcon.smali";
 		if (debug) {
 			System.out.println("inputFilename=" + inputFilename);
 		}
 		String outputFilename = servicesDir.getAbsolutePath() + SLASH + "com" + SLASH + "android" + SLASH + "server" + SLASH + "status" + SLASH + "StatusBarIcon.smali-new";
 		if (debug) {
 			System.out.println(("outputFilename=" + outputFilename));
 		}
 
 		try {
 			bufRead = new BufferedReader(new FileReader(inputFilename));
 			bufWrite = new BufferedWriter(new FileWriter(outputFilename));
 			String line = null;
 			while ((line = bufRead.readLine()) != null) {
 				if (line.contains(".line 46")) {
 					System.out.println("found line: " + line);
 					bufWrite.write(line + NL);
 					line = bufRead.readLine();
 					System.out.println("skipping line: " + line);
 					bufWrite.write("    const v6, 0x" + clockColor + NL);
 					System.out.println("adding line:     const v6, 0x" + clockColor);
 				} else {
 					bufWrite.write(line + NL);
 				}
 			}
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		} finally {
 			try {
 				if (bufRead != null) {
 					bufRead.close();
 				}
 				if (bufWrite != null) {
 					bufWrite.close();
 				}
 			} catch (IOException e) {
 			}
 		}
 		File origFile = new File(inputFilename);
 		if (!origFile.delete()) {
 			System.out.println("ERROR: deleting orig file: " + origFile.getName());
 		}
 		File newFile = new File(outputFilename);
 		if (!newFile.renameTo(origFile)) {
 			System.out.println("ERROR: renaming file: " + origFile.getName() + " to " + newFile.getName());
 		}
 		System.out.println("Finished editing the StatusBarIcon.smali file\n");
 
 		// Edit the StatusBarService.smali file
 		inputFilename = servicesDir.getAbsolutePath() + SLASH + "com" + SLASH + "android" + SLASH + "server" + SLASH + "status" + SLASH + "StatusBarService.smali";
 		outputFilename = servicesDir.getAbsolutePath() + SLASH + "com" + SLASH + "android" + SLASH + "server" + SLASH + "status" + SLASH + "StatusBarService.smali-new";
 		try {
 			bufRead = new BufferedReader(new FileReader(inputFilename));
 			bufWrite = new BufferedWriter(new FileWriter(outputFilename));
 			String line = null;
 			while ((line = bufRead.readLine()) != null) {
 				if (line.contains("check-cast v7, Lcom/android/server/status/DateView;")) {
 					System.out.println("found line: " + line);
 					bufWrite.write(line + NL + NL);
 					System.out.println("skipping line: " + bufRead.readLine());
 					bufWrite.write("    const v8, 0x" + dateColor + NL);
 					System.out.println("adding line:     const v8, 0x" + dateColor);
 					bufWrite.write(NL);
 					System.out.println("adding line: <blank>");
 					bufWrite.write("    invoke-virtual {v7, v8}, Landroid/widget/TextView;->setTextColor(I)V" + NL);
 					System.out.println("adding line:     invoke-virtual {v7, v8}, Landroid/widget/TextView;->setTextColor(I)V");
 					bufWrite.write(NL);
 					System.out.println("adding line: <blank>");
 				} else {
 					bufWrite.write(line + NL);
 				}
 
 				// ongoing color
 				if (line.contains(".line 347")) {
 					System.out.println("found line: " + line);
 					bufWrite.write(line + NL);
 					modifySingleColorSection(bufRead, bufWrite, ongoingColor);
 				}
 				// latest color
 				if (line.contains(".line 349")) {
 					System.out.println("found line: " + line);
 					bufWrite.write(line + NL);
 					modifySingleColorSection(bufRead, bufWrite, latestColor);
 				}
 				// no notifications color
 				if (line.contains(".line 351")) {
 					System.out.println("found line: " + line);
 					bufWrite.write(line + NL);
 					modifySingleColorSection(bufRead, bufWrite, noNotifsColor);
 				}
 				// clear button text color
 				if (line.contains(".line 352")) {
 					System.out.println("found line: " + line);
 					bufWrite.write(line + NL);
 					modifySingleColorSection(bufRead, bufWrite, clearButTextColor);
 				}
 				// roam? text color
 				if (line.contains(".line 354")) {
 					System.out.println("found line: " + line);
 					bufWrite.write(line + NL);
 					modifySingleColorSection(bufRead, bufWrite, roamingColor);
 				}
 				// network text color
 				if (line.contains(".line 355")) {
 					System.out.println("found line: " + line);
 					bufWrite.write(line + NL);
 					modifySingleColorSection(bufRead, bufWrite, networkNameColor);
 				}
 			}
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		} finally {
 			try {
 				if (bufRead != null) {
 					bufRead.close();
 				}
 				if (bufWrite != null) {
 					bufWrite.close();
 				}
 			} catch (IOException e) {
 			}
 		}
 		origFile = new File(inputFilename);
 		if (!origFile.delete()) {
 			System.out.println("ERROR: deleting orig file: " + origFile.getName());
 		}
 		newFile = new File(outputFilename);
 		if (!newFile.renameTo(origFile)) {
 			System.out.println("ERROR: renaming file: " + origFile.getName() + " to " + newFile.getName());
 		}
 		System.out.println("Finished editing the StatusBarService.smali file\n");
 
 		// Re-dex services.jar
 		System.out.println("re-dexing...");
 		org.jf.smali.main.main(new String[] { servicesDir.getAbsolutePath(),
 				"-o", classesDex.getAbsolutePath() });
 		System.out.println("Finished re-dexing classes.dex\n");
 
 		deleteDir(new File(servicesDir.getAbsolutePath() + SLASH + "com"));
 		System.out.println("Deleted dex out directory");
 
 		zip(servicesFile, servicesDir, servicesDir.getAbsolutePath(), false);
 		System.out.println("Rezipped services.jars");
 		deleteDir(servicesDir);
 		System.out.println("\n**** END SERVICES.JAR ******");
 	}
 
 	private static void modifySingleColorSection(BufferedReader bufRead, BufferedWriter bufWrite, String color) throws IOException {
 		String line;
 		line = bufRead.readLine();// const v7, 0x10201dd
 		bufWrite.write(line + NL);
 		line = bufRead.readLine();
 		bufWrite.write(line + NL);
 		line = bufRead.readLine();// invoke-virtual {v1, v7},
 		// Lcom/android/server/status/ExpandedView;->findViewById(I)Landroid/view/View;
 		if (!line.contains("invoke-virtual {v1, v7}, Lcom/android/server/status/ExpandedView;->findViewById(I)Landroid/view/View")) {
 			// sanity check
 			System.out.println("PANIC: skipped line was not what is expected!\nPlease report this to Ohsaka on xda-developers forum.");
 			System.out.println("line=" + line);
 			line = bufRead.readLine();
 			System.out.println("line=" + line);
 			line = bufRead.readLine();
 			System.out.println("line=" + line);
 			line = bufRead.readLine();
 			System.exit(1);
 		}
 		bufWrite.write(line + NL);
 		line = bufRead.readLine();
 		bufWrite.write(line + NL);
 		line = bufRead.readLine();// move-result-object v7
 		bufWrite.write(line + NL);
 		line = bufRead.readLine();
 		bufWrite.write(line + NL);
 		line = bufRead.readLine();// check-cast v7, Landroid/widget/TextView;
 		bufWrite.write(line + NL);
 		line = bufRead.readLine();
 		bufWrite.write(line + NL);
 		bufWrite.write("    const v8, 0x" + color + NL);
 		bufWrite.write("" + NL);
 		bufWrite.write("    invoke-virtual {v7, v8}, Landroid/widget/TextView;->setTextColor(I)V");
 		bufWrite.write("" + NL);
 	}
 
 	private static void modifyFrameworkColor(File frameworkFile, String rgbColor) {
 		System.out.println("\n**** BEGIN framework-res.apk ******\n");
 		File frameworkDir = new File(frameworkFile.getParent() + SLASH + "framework-res-apk");
 		unzip(frameworkFile, frameworkDir);
 
 		// Edit the status_bar.xml file
 		String inputFilename = frameworkDir.getAbsolutePath() + SLASH + "res" + SLASH + "layout" + SLASH + "status_bar.xml";
 		if (debug) {
 			System.out.println("inputFilename=" + inputFilename);
 		}
 		String outputFilename = frameworkDir.getAbsolutePath() + SLASH + "res" + SLASH + "layout" + SLASH + "status_bar.xml-new";
 		if (debug) {
 			System.out.println(("outputFilename=" + outputFilename));
 		}
 
 		FileInputStream fisRead = null;
 		FileOutputStream fisWrite = null;
 		int oneByte;
 		try {
 			File inFile = new File(inputFilename);
 			fisRead = new FileInputStream(inFile);
 			fisWrite = new FileOutputStream(outputFilename);
 
 			String currentByte;
 			int offset = 0;
 			// read whole file one byte at a time
 			while ((oneByte = fisRead.read()) != -1) {
 				fisWrite.write(oneByte);
 				offset++;
 				currentByte = Integer.toHexString(oneByte);
 				// 8 0 0 1c
 				// found start of pattern
 				if (currentByte.equals("8")) {
 					oneByte = fisRead.read();
 					if (oneByte == -1) // make sure we didnt hit end of file
 						break;
 					fisWrite.write(oneByte);
 					offset++;
 					currentByte = Integer.toHexString(oneByte);
 					if (currentByte.equals("0")) {
 						oneByte = fisRead.read();
 						if (oneByte == -1) // make sure we didnt hit end of file
 							break;
 						fisWrite.write(oneByte);
 						offset++;
 						currentByte = Integer.toHexString(oneByte);
 						if (currentByte.equals("0")) {
 							oneByte = fisRead.read();
 							if (oneByte == -1) // make sure we didnt hit end of
 								// file
 								break;
 							fisWrite.write(oneByte);
 							offset++;
 							currentByte = Integer.toHexString(oneByte);
 							if (currentByte.equals("1c")) {
 								System.out.println("textColor pattern found at offset: " + offset);
 								// skip the current color pattern in the file
 								oneByte = fisRead.read();
 								oneByte = fisRead.read();
 								oneByte = fisRead.read();
 								oneByte = fisRead.read();
 								// insert new color pattern, dont forget to
 								// reverse it
 								// BB
 								fisWrite.write(fromHexString(rgbColor.substring(6)));
 								// GG
 								fisWrite.write(fromHexString(rgbColor.substring(4, 6)));
 								// RR
 								fisWrite.write(fromHexString(rgbColor.substring(2, 4)));
 								// AA
 								fisWrite.write(fromHexString(rgbColor.substring(0, 2)));
 							}
 						}
 					}
 				}
 			}
 
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		} finally {
 			try {
 				if (fisRead != null) {
 					fisRead.close();
 				}
 				if (fisWrite != null) {
 					fisWrite.close();
 				}
 			} catch (IOException e) {
 			}
 		}
 		File origFile = new File(inputFilename);
 		if (!origFile.delete()) {
 			System.out.println("ERROR: deleting orig file: " + origFile.getName());
 		}
 		File newFile = new File(outputFilename);
 		if (!newFile.renameTo(origFile)) {
 			System.out.println("ERROR: renaming file: " + origFile.getName() + " to " + newFile.getName());
 		}
 		System.out.println("Finished editing the status_bar.xml file\n");
 
 		zip(frameworkFile, frameworkDir, frameworkDir.getAbsolutePath(), false);
 		System.out.println("Rezipped framework-res.apk");
 
 		deleteDir(frameworkDir);
 		System.out.println("Deleted unzipped framework directory");
 
 		System.out.println("\n**** END framework-res.apk ******\n");
 	}
 
 	// check for valid hexadecimal characters
 	private static boolean isValidChar(char x) {
 		for (int i = 0; i < validChars.length; i++) {
 			if (validChars[i] == Character.toUpperCase(x)) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	// recursively delete a directory tree
 	public static boolean deleteDir(File dir) {
 		if (dir.isDirectory()) {
 			String[] children = dir.list();
 			for (int i = 0; i < children.length; i++) {
 				boolean success = deleteDir(new File(dir, children[i]));
 				if (!success) {
 					return false;
 				}
 			}
 		}
 		// The directory is now empty so delete it
 		return dir.delete();
 	}
 
 	// recursively list all files in a directory
 	static private List<File> getFileListingNoSort(File aStartingDir) throws FileNotFoundException {
 		List<File> result = new ArrayList<File>();
 		File[] filesAndDirs = aStartingDir.listFiles();
 		List<File> filesDirs = Arrays.asList(filesAndDirs);
 
 		for (File file : filesDirs) {
 			if (!file.isDirectory()) {
 				result.add(file);
 			} else {
 				// must be a directory
 				// recursive call!
 				List<File> deeperList = getFileListingNoSort(file);
 				result.addAll(deeperList);
 			}
 		}
 		return result;
 	}
 
 	public static String getFileNameWithoutExtension(String fileName) {
 		File tmpFile = new File(fileName);
 
 		int whereDot = tmpFile.getAbsolutePath().lastIndexOf('.');
 		if (0 < whereDot && whereDot <= tmpFile.getAbsolutePath().length() - 2) {
 			return tmpFile.getAbsolutePath().substring(0, whereDot);
 			// extension = filename.substring(whereDot+1);
 		}
 		return "";
 
 	}
 
 	private static String makeZipEntryName(String filename, String ignorePart) {
 		String newName = filename.substring(ignorePart.length() + 1, filename.length());
 		return newName.replace('\\', '/');
 	}
 
 	private static boolean zip(File updateFile, File zipRootDir, String ignoreDirName, boolean addNewToEndOfFileName) {
 		try {
 			List<File> fileList = getFileListingNoSort(zipRootDir);
 			// Specify files to be zipped
 			String[] filesToZip = new String[fileList.size()];
 			for (int i = 0; i < fileList.size(); i++) {
 				filesToZip[i] = (fileList.get(i)).getAbsolutePath();
 			}
 
 			byte[] buffer = new byte[18024];
 
 			// Specify zip file name
 			String zipFileName = null;
 			if (addNewToEndOfFileName) {
 				zipFileName = getFileNameWithoutExtension(updateFile.getAbsolutePath()) + "-new.zip";
 				System.out.println("updated zipFileName=" + zipFileName);
 			} else {
 				zipFileName = updateFile.getAbsolutePath();
 			}
 
 			ZipOutputStream out = new ZipOutputStream(new FileOutputStream(zipFileName));
 
 			// Set the compression ratio
 			out.setLevel(Deflater.DEFAULT_COMPRESSION);
 
 			// iterate through the array of files, adding each to the zip file
 			for (int i = 0; i < filesToZip.length; i++) {
 				if (debug) {
 					System.out.println("zipping: " + makeZipEntryName(filesToZip[i], ignoreDirName));
 				}
 				// Associate a file input stream for the current file
 				FileInputStream in = new FileInputStream(filesToZip[i]);
 
 				// Add ZIP entry to output stream.
 				out.putNextEntry(new ZipEntry(makeZipEntryName(filesToZip[i], ignoreDirName)));
 
 				// Transfer bytes from the current file to the ZIP file
 				// out.write(buffer, 0, in.read(buffer));
 
 				int len;
 				while ((len = in.read(buffer)) > 0) {
 					out.write(buffer, 0, len);
 				}
 
 				// Close the current entry
 				out.closeEntry();
 
 				// Close the current file input stream
 				in.close();
 
 			}
 			// Close the ZipOutPutStream
 			out.close();
 		} catch (IllegalArgumentException iae) {
 			iae.printStackTrace();
 		} catch (FileNotFoundException fnfe) {
 			fnfe.printStackTrace();
 		} catch (IOException ioe) {
 			ioe.printStackTrace();
 		}
 
 		return true;
 	}
 
 	public static boolean unzip(File sourceZipFile, File unzipDestinationDirectory) {
 		int BUFFER = 2048;
 
 		try {
 			// Open Zip file for reading
 			ZipFile zipFile = new ZipFile(sourceZipFile, ZipFile.OPEN_READ);
 
 			// Create an enumeration of the entries in the zip file
 			Enumeration<? extends ZipEntry> zipFileEntries = zipFile.entries();
 
 			// Process each entry
 			while (zipFileEntries.hasMoreElements()) {
 				// grab a zip file entry
 				ZipEntry entry = (ZipEntry) zipFileEntries.nextElement();
 
 				String currentEntry = entry.getName();
 				if (debug) {
 					System.out.println("Extracting: " + entry);
 				}
 
 				File destFile = new File(unzipDestinationDirectory, currentEntry);
 
 				// grab file's parent directory structure
 				File destinationParent = destFile.getParentFile();
 
 				// create the parent directory structure if needed
 				destinationParent.mkdirs();
 
 				// extract file if not a directory
 				if (!entry.isDirectory()) {
 					BufferedInputStream is = new BufferedInputStream(zipFile.getInputStream(entry));
 					int currentByte;
 					// establish buffer for writing file
 					byte data[] = new byte[BUFFER];
 
 					// write the current file to disk
 					FileOutputStream fos = new FileOutputStream(destFile);
 					BufferedOutputStream dest = new BufferedOutputStream(fos, BUFFER);
 
 					// read and write until last byte is encountered
 					while ((currentByte = is.read(data, 0, BUFFER)) != -1) {
 						dest.write(data, 0, currentByte);
 					}
 					dest.flush();
 					dest.close();
 					is.close();
 				}
 			}
 			zipFile.close();
 			return true;
 		} catch (Exception e) {
 			e.printStackTrace();
 			return false;
 		}
 	}
 
 	// convert a String representing a base b integer into an int
 	static int fromString(String s, int b) {
 		int result = 0;
 		int digit = 0;
 		for (int i = 0; i < s.length(); i++) {
 			char c = s.charAt(i);
 			if (c >= '0' && c <= '9')
 				digit = c - '0';
 			else if (c >= 'A' && c <= 'Z')
 				digit = 10 + c - 'A';
 			if (digit < b)
 				result = b * result + digit;
 		}
 		return result;
 	}
 
 	static int fromHexString(String s) {
 		return fromString(s, 16);
 	}
 }
