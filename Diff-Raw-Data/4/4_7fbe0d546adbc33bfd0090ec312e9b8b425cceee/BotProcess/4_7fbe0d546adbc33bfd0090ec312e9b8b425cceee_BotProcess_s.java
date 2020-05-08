 package net.hexid.hexbot.bot;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.Arrays;
 import java.util.List;
 import java.util.Scanner;
 import net.hexid.Utils;
 
 public class BotProcess extends Thread {
 	private Process process;
 	private Scanner in;
 	protected BotTab bot;
 
 	public BotProcess(BotTab bot) throws IOException {
 		super();
 		this.bot = bot;
 		this.process = startProcess();
 		this.in = new Scanner(this.process.getInputStream());
 	}
 
 	protected void processInput(final String in) {
 		javafx.application.Platform.runLater(new Runnable() {
 			@Override public void run() {
 				bot.appendOutput(in);
 			}
 		});
 	}
 
 	protected Process startProcess() throws IOException {
 		List<String> processData;
		String botExecuteData = Utils.join(" ", "casperjs", Bots.getBotFile(bot),
				Utils.join(" ", bot.getBotExecuteData()));
 		String pathName;
 		if(File.pathSeparator.equals(";")) { // if windows
 			pathName = "Path";
 			processData = Arrays.asList(new String[]{"cmd.exe", "/C", botExecuteData});
 		} else { // if unix-based
 			pathName = "PATH";
 			processData = Arrays.asList(new String[]{"/usr/bin/env","bash", "-c", botExecuteData});
 		}
 
 		// create a processbuilder with the bot's commands and combined input and error streams
 		ProcessBuilder pb = new ProcessBuilder(processData).redirectErrorStream(true);
 
 		// append phantomjs and casperjs to the path (runs local installs first)
 		pb.environment().put(pathName, Bots.getBotEnvPath(pb.environment().get(pathName)));
 
 		return pb.start(); // execute the process
 	}
 
 	@Override public void run() {
 		while(in.hasNext())
 			processInput(in.nextLine());
 
 		in.close();
 
 		try {
 			process.waitFor();
 			bot.processExitCode(process.exitValue());
 		} catch(InterruptedException e) {
 			e.printStackTrace();
 		}
 	}
 
 	public void killProcess() {
 		process.destroy();
 	}
 }
