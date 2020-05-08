 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.nio.MappedByteBuffer;
 import java.nio.channels.FileChannel;
 import java.nio.charset.Charset;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Properties;
 
 /*
 Example of using this JAR in Ant with the local VM:
 
 <!-- Note: You must have the JavaReplaceBuildFiles jar defined as a dependency in ivy.xml in order to automatically retrieve it in /lib/ -->
 <target name="ReplaceBuildFiles">
 	<java	fork="true"
 			failonerror="true"
 			maxmemory="16m"
 			classname="JavaReplaceBuildFiles"
 			classpath="lib/JavaReplaceBuildFiles-1.6.jar">
 		<arg line="${replacetokens.sourcefolders}" />
 		<arg line="${replacetokens.filetypes}" />
 		<arg line="${replacetokens.outputfolder}" />
 		<arg line="../../tokens.LOCAL.properties" />
 		<arg line="${replacetokens.foldernamesreplace}" />
 		<arg line="${debug}" />
 		<arg line="C:/workspace/projectName" />
 		<arg line="true" />
 	</java>
 </target>
 */
 
 public class JavaReplaceBuildFiles
 {
 	private static ArrayList<String[]> keys;
 	private static List<String> extensionList;
 	private static String outputFolder;
 	private static String replaceNames;
 	private static boolean debug = false;
 	private static String projectFolder;
 	private static boolean renameSourceFiles = false;
 
 	// Arg 0: Comma-delimited list of source folders to scan
 	// Arg 1: Comma-delimited list of file types to scan
 	// Arg 2: Target output folder (will be created if not already exists)
 	// Arg 3: Path to properties file that contains the tokens to be found/replaced
 	// Arg 4: Comma-delimited list of strings to be replaced with the outputfolder name instead
 	//        (This helps catch any oversights whereby something is referencing an original (still-tokenized) file,
 	//         instead of the output file that has had the tokens replaced.)
 	// Arg 5: True/False debug flag (controls log output)
 	// Arg 6: Absolute path to the project folder (the expected parent of source folders and outputFolder)
 	// Arg 7: Flag that governs whether source tokenized files get renamed as .replaced
 	public static void main(String[] args)
 			throws Exception
 	{
 		try
 		{
 			if (args.length != 8) {
 				throw new Exception("Invalid number of arguments; expected 8.");
 			}
 
 			// Set variables and read project properties file to get the configured tokens
 			debug = Boolean.parseBoolean(args[5]);	// Need to set debug first; the others can happen in any order
 			String[] folders = args[0].split(",");
 			extensionList = Arrays.asList(args[1].split("\\s*,\\s*"));
 			outputFolder = args[2];
 			keys = getTokensFromPropertiesFile(args[3]);
 			replaceNames = args[4];
 			projectFolder = args[6];
 			renameSourceFiles = Boolean.parseBoolean(args[7]);
 
 			// For each folder that was passed as a comma-delimited list:
 			for (String folderName : folders) {
 				if (!folderName.isEmpty()) {
 					processFolder(folderName);
 				}
 			}
 		} catch (Exception ex) {
 			ex.printStackTrace();
 			throw ex;
 		}
 	}
 
 	private static ArrayList<String[]> getTokensFromPropertiesFile(String fileName) throws FileNotFoundException, IOException
 	{
 		logMessage("Loading properties from " + fileName);
 		ArrayList<String[]> keys = new ArrayList<String[]>();
 
 		Properties prop = new Properties();
 		prop.load(new FileInputStream(fileName));
 
 		for (Iterator localIterator = prop.keySet().iterator(); localIterator.hasNext(); ) {
 			Object propKey = localIterator.next();
 			String key = (String)propKey;
 			if (key.startsWith("token.")) {
 				String[] keyValuePair = new String[2];
 				keyValuePair[0] = key.substring(6);
 				keyValuePair[1] = prop.getProperty(key);
 				keys.add(keyValuePair);
 			}
 		}
 		return keys;
 	}
 
 	private static String readFile(String path) throws IOException
 	{
 		FileInputStream stream = new FileInputStream(new File(path));
 		try {
 			FileChannel fc = stream.getChannel();
 			MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0L, fc.size());
 			return Charset.defaultCharset().decode(bb).toString();
 		}
 		finally {
 			stream.close();
 		}
 	}
 
 	private static void processFolder(String folderName)
 		throws IOException
 	{
 		File folder = new File(projectFolder + "/" + folderName);
 		File[] listOfFiles = folder.listFiles();
 
 		// Examine the list of files in that folder
 		for (File file : listOfFiles) {
 			if (file.isFile() &&
 				extensionList.contains(file.getName().substring(file.getName().lastIndexOf('.')+1))) {
 				processFile(file);
 			}
 		}		
 	}
 
 	private static void processFile(File file)
 		throws IOException
 	{
 		boolean replaced = false;
 		logMessage("Working on: " + file.getPath());
 
 		String content = readFile(file.getPath());
 		for (String[] pair : keys)
 		{
 			logMessage("Searching for token: " + pair[0]);
 			if (content.contains(pair[0])) {
 				logMessage("Token(s) found; replacing.");
 				content = content.replaceAll(pair[0], pair[1]);
 				replaced = true;
 			}
 		}
 
 		// Loop through strings to be replaced with the output folder name instead (if set)
 		// (For instance, a PS that references /business/TokenBS should reference /OutputFolder/TokenBS instead.)
 		if (!replaceNames.isEmpty()) {
 			String[] sourceStrings = replaceNames.split(",");
 			for (String sourceString : sourceStrings) {
 				if (content.contains(sourceString)) {
					logMessage("Replacing " + sourceString + " with /" + outputFolder + "/");
					content = content.replaceAll(sourceString, "/" + outputFolder + "/");
 					replaced = true;
 				}
 			}
 		}
 
 		if (replaced) {
 			// Create output directory if not yet exists
 			File outputDir = new File(projectFolder + "/" + outputFolder);
 			if (!outputDir.exists()) {
 				outputDir.mkdir();
 			}
 
 			BufferedWriter outFile = new BufferedWriter(new FileWriter(projectFolder + "/" + outputFolder + "/" + file.getName()));
 			outFile.write(content);
 			outFile.close();
 			
 			// Rename source file if configured to do so
 			if (renameSourceFiles) {
 				logMessage("Renaming source file to .replaced");
 				file.renameTo(new File(file.getAbsolutePath() + ".replaced"));
 			}
 		}
 	}
 
 	private static void logMessage(String msg)
 	{
 		if (debug) {
 			System.out.println(msg);
 		}		
 	}
 }
