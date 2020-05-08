 package de.b2tla;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.PrintStream;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashSet;
 import java.util.Set;
 
 import de.b2tla.tlc.ProcessHelper;
 import de.b2tla.tlc.TLCOutput;
 import de.b2tla.util.BTLCPrintStream;
 
 import util.SimpleFilenameToStream;
 import util.ToolIO;
 import tlc2.TLC;
 
 public class TLCRunner {
 
 	public static void main(String[] args) {
 		// this method will be executed in a separate JVM
		System.out.println("Starting TLC...");
 		String path = args[0];
 		ToolIO.setUserDir(path);
 		String[] parameters = new String[args.length - 1];
 		for (int i = 0; i < parameters.length; i++) {
 			parameters[i] = args[i + 1];
 		}
 		TLC.main(parameters);
 	}
 
 	public static ArrayList<String> runTLCInANewJVM(String machineName,
 			String path) throws IOException {
 		ArrayList<String> list = new ArrayList<String>();
 		list.add(path);
 		list.add(machineName);
 		if (!Globals.deadlockCheck) {
 			list.add("-deadlock");
 		}
 		String[] args = list.toArray(new String[list.size()]);
 		ProcessHelper helper = new ProcessHelper();
		System.out.println("Starting TLC...");
 		Process p = helper.startNewJavaProcess("", TLCRunner.class.getName(),
 				args);
 
 		StreamGobbler stdOut = new StreamGobbler(p.getInputStream());
 		stdOut.start();
 		StreamGobbler errOut = new StreamGobbler(p.getErrorStream());
 		errOut.start();
 		try {
 			p.waitFor();
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		}
 
 		return stdOut.getLog();
 	}
 
 	public static void runTLCOld(String machineName, String path) {
 		ArrayList<String> list = new ArrayList<String>();
 		if (!Globals.deadlockCheck) {
 			list.add("-deadlock");
 		}
 		list.add("-config");
 		list.add(machineName + ".cfg");
 		list.add(machineName);
 		ToolIO.setUserDir(path);
 		String[] args = list.toArray(new String[list.size()]);
 
 		// ByteArrayOutputStream os = new ByteArrayOutputStream();
 		// PrintStream ps = new PrintStream(os);
 		BTLCPrintStream btlcStream = new BTLCPrintStream();
 		PrintStream old = System.out;
 		System.setOut(btlcStream);
 		ToolIO.setMode(ToolIO.SYSTEM);
 
 		TLC tlc = new TLC();
 		// handle parameters
 		if (tlc.handleParameters(args)) {
 			tlc.setResolver(new SimpleFilenameToStream());
 			// call the actual processing method
 			tlc.process();
 		}
 		System.setOut(old);
 
 		String[] messages = btlcStream.getArray();
 		System.out.println(Arrays.asList(messages));
 		TLCOutput tlcOutput = new TLCOutput(machineName, messages);
 		tlcOutput.parseTLCOutput();
 		Globals.tlcOutput = tlcOutput;
 		// TLCOutputEvaluator evaluator = new TLCOutputEvaluator(machineName,
 		// messages);
 		System.out.println("ERROR: " + tlcOutput.getError());
 		StringBuilder trace = tlcOutput.getErrorTrace();
 		if (tlcOutput.hasTrace()) {
 			createfile(path, machineName + ".tla.trace", trace.toString());
 		}
 	}
 
 	public static void createfile(String dir, String fileName, String text) {
 		File d = new File(dir);
 		d.mkdirs();
 		File tempFile = new File(dir + File.separator + fileName);
 		try {
 			tempFile.createNewFile();
 			System.out
 					.println("Tempfile:'" + tempFile.getName() + "' created.");
 		} catch (IOException e1) {
 			e1.printStackTrace();
 		}
 		FileWriter fw;
 		try {
 			fw = new FileWriter(tempFile);
 			fw.write(text);
 			fw.close();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 }
 
 class StreamGobbler extends Thread {
 	private InputStream is;
 	private ArrayList<String> log;
 
 	public ArrayList<String> getLog() {
 		return log;
 	}
 
 	StreamGobbler(InputStream is) {
 		this.is = is;
 		this.log = new ArrayList<String>();
 	}
 
 	public void run() {
 		try {
 			InputStreamReader isr = new InputStreamReader(is);
 			BufferedReader br = new BufferedReader(isr);
 			String line = null;
 			while ((line = br.readLine()) != null) {
 				System.out.println("> " + line);
 				log.add(line);
 			}
 
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 }
