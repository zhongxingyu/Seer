 package worker;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.concurrent.locks.ReentrantLock;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import common.Config;
 import common.Match;
 
 /**
  * This class handles the running of battlecode matches and returning the results
  * @author stevearc
  *
  */
 
 public class MatchRunner implements Runnable {
 	private Logger _log;
 	private Config config;
 	private Worker worker;
 	private Match match;
 	private boolean stop = false;
 	private ReentrantLock repoLock;
 	private Process curProcess;
 
 	public MatchRunner(Worker worker, Match match, ReentrantLock repoLock) {
 		config = Config.getConfig();
 		this.match = match;
 		this.worker = worker;
 		this.repoLock = repoLock;
 		_log = config.getLogger();
 	}
 
 	/**
 	 * Safely halts the execution of the MatchRunner
 	 */
 	public void stop() {
 		stop = true;
 		if (curProcess != null)
 			curProcess.destroy();
 	}
 
 	/**
 	 * Runs the battlecode match
 	 */
 	public void run() {
 		String status = null;
 		int winner = 0;
 		int win_condition = 0;
 		double a_points = 0;
 		double b_points = 0;
 		byte[] data = null;
 		// Clean out old data
 		File f = new File(config.repo + "/" + match.map + ".rms");
 		f.delete();
 
 		try {
 			_log.info("Running: " + match);
 			String output_file = config.repo + "/" + match.map + match.seed + ".out";
 			Runtime run = Runtime.getRuntime();
 			try {
 				repoLock.lock();
 
 				// Update the repository
 				if (stop)
 					return;
 				curProcess = run.exec(config.cmd_update);
 				curProcess.waitFor();
 				if (stop)
 					return;
 				// Get the appropriate teams for the match
 				String[] args = new String[3];
 				args[0] = config.cmd_grabole;
 				args[1] = match.team_a;
 				args[2] = match.team_b;
 				curProcess = run.exec(args);
 				curProcess.waitFor();
 				if (stop)
 					return;
 				// Generate the bc.conf file
 				curProcess = run.exec(new String[] {config.cmd_gen_conf, match.map.map, ""+match.seed});
 				curProcess.waitFor();
 				if (stop)
 					return;
 				// Run the match
 				curProcess = run.exec(new String[] {config.cmd_run_match, config.repo, output_file});
 				Thread.sleep(10000);
 			} finally {
 				repoLock.unlock();
 			}
 			new Thread(new Callback(Thread.currentThread(), curProcess)).start();
 			try {
 				Thread.sleep(config.timeout);
 			} catch (InterruptedException e) {
 			}
 
 			// Read in the output file
 			BufferedReader reader = new BufferedReader(new FileReader(output_file));
 			StringBuilder sb = new StringBuilder();
 			for (String line = null; (line = reader.readLine()) != null; ) {
 				sb.append(line);
 			}
 			String output = sb.toString();
 			File of = new File(config.repo + "/" + output_file);
 			of.delete();
 
 			// Check to see if there were any errors
 			if (stop)
 				return;
 			if (curProcess.exitValue() != 0) {
 				_log.severe("Error running match\n" + output);
 				status = "error";
 				worker.matchFinish(this, match, status, winner, win_condition, a_points, b_points, data);
 				return;
 			}
 
 			// Kind of sloppy win detection, but it works
 			int a_index = output.indexOf("(A) wins");
 			int b_index = output.indexOf("(B) wins");
 			if (a_index == -1) {
 				if (b_index == -1) {
 					status = "error";
 					_log.warning("Unknown error running match\n" + output);
 					_log.warning("^ error: a_index: " + a_index + " b_index: " + b_index);
 					if (!stop) 
 						worker.matchFinish(this, match, status, winner, win_condition, a_points, b_points, data);
 					return;
 				}
 				// If A loses, winner = 0
 				winner = 0;
 			} else {
 				// If A wins, winner = 1
 				winner = 1;
 			}
 
 			// Detect how the match was finished
 			if (output.indexOf("Reason: The losing team was destroyed.") >= 0) {
 				win_condition = 0;
 			} else if (output.indexOf("production and Team") >= 0) {
 				win_condition = 1;
 			} else {
 				win_condition = 2;
 			}
			String a_points_str = getMatch(output, "Team A had [0-9]+\\.[0-9]+ production");
 			if (!"".equals(a_points_str))
 				a_points = Double.parseDouble(a_points_str.substring("Team A had ".length()));
			String b_points_str = getMatch(output, "Team B had [0-9]+\\.[0-9]+ production");
 			if (!"".equals(b_points_str))
 				b_points = Double.parseDouble(b_points_str.substring("Team B had ".length()));
 
 			_log.info("Finished: " + match);
 		} catch (Exception e) {
 			if (!stop) {
 				_log.log(Level.SEVERE, "Failed to run match", e);
 				status = "error";
 				worker.matchFinish(this, match, status, winner, win_condition, a_points, b_points, data);
 			}
 			return;
 		}
 
 		// Read in the replay file
 		String matchFile = config.repo + "/" + match.map + ".rms";
 		try {
 			data = getMatchData(matchFile);
 		} catch (IOException e) {
 			_log.log(Level.SEVERE, "Failed to read " + matchFile, e);
 			status = "error";
 			if (!stop)
 				worker.matchFinish(this, match, status, winner, win_condition, a_points, b_points, data);
 			return;
 		}
 
 		status = "ok";
 		if (!stop)
 			worker.matchFinish(this, match, status, winner, win_condition, a_points, b_points, data);
 	}
 
 	/**
 	 * 
 	 * @param filename
 	 * @return The data from the match.rms file
 	 * @throws IOException
 	 */
 	private byte[] getMatchData(String filename) throws IOException {
 		File file = new File(filename);
 		if (!file.exists())
 			throw new IOException("Match file does not exist");
 		FileInputStream is = new FileInputStream(file);
 
 		long length = file.length();
 
 		// Create the byte array to hold the data
 		byte[] data = new byte[(int)length];
 
 		// Read in the bytes
 		int offset = 0;
 		int numRead = 0;
 		while (offset < data.length
 				&& (numRead=is.read(data, offset, data.length-offset)) >= 0) {
 			offset += numRead;
 		}
 
 		// Ensure all the bytes have been read in
 		if (offset < data.length) {
 			throw new IOException("Could not completely read file "+file.getName());
 		}
 
 		is.close();
 		return data;
 	}
 
 	/**
 	 * Finds the first instance of a pattern in a regex and returns that
 	 * @param contents
 	 * @param regex
 	 * @return
 	 */
 	private static String getMatch(String contents, String regex){
 		return getAllMatches(contents, regex).get(0);
 	}
 
 	/**
 	 * Finds all patterns matching a regex and returns them in an array.
 	 * @param contents
 	 * @param regex
 	 * @return an array of the patterns in the contents String
 	 */
 	private static ArrayList<String> getAllMatches(String contents, String regex){
 		Pattern pattern = Pattern.compile(regex);
 		Matcher matcher = pattern.matcher(contents);
 		ArrayList<String> patterns = new ArrayList<String>();
 		while(matcher.find()){
 			String match = matcher.group();
 			if (!"".equals(match)){
 				patterns.add(matcher.group());
 			}
 		}
 		if (patterns.size() == 0){
 			patterns.add("");
 		}
 		return patterns;
 	}
 
 	@Override
 	public String toString() {
 		return match.toString();
 	}
 
 }
 
 class Callback implements Runnable {
 	private Thread parent;
 	private Process proc;
 
 	public Callback (Thread parent, Process proc) {
 		this.parent = parent;
 		this.proc = proc;
 	}
 
 	@Override
 	public void run() {
 		try {
 			proc.waitFor();
 			parent.interrupt();
 		} catch (InterruptedException e) {
 		}
 	}
 
 }
