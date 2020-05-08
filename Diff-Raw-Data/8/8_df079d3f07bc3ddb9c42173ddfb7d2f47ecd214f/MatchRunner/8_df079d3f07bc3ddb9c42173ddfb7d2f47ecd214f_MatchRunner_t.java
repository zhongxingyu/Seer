 package worker;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.OutputStreamWriter;
 import java.util.Collection;
 import java.util.Enumeration;
 import java.util.List;
 import java.util.jar.JarEntry;
 import java.util.jar.JarFile;
 
 import main.Main;
 import model.BSScrimmageSet;
 import model.MatchResultImpl;
 import model.STATUS;
 import model.ScrimmageMatchResult;
 import model.TEAM;
 
 import org.apache.commons.io.FileUtils;
 import org.apache.log4j.Logger;
 
 import battlecode.server.Server;
 import battlecode.server.controller.Controller;
 import battlecode.server.controller.ControllerFactory;
 import battlecode.server.proxy.Proxy;
 import battlecode.server.proxy.ProxyFactory;
 
 import common.BSUtil;
 import common.Config;
 import common.NetworkMatch;
 
 /**
  * This class handles the running of battlecode matches and returning the results
  * @author stevearc
  *
  */
 
 public class MatchRunner implements Runnable {
 	private static Logger _log = Logger.getLogger(MatchRunner.class);
 	private Worker worker;
 	private boolean running;
 	private NetworkMatch match;
 	private BSScrimmageSet scrim;
 	private byte[] scrimData;
 	private int core;
 	private Process currentProcess;
 
 	public MatchRunner(Worker worker, NetworkMatch match, int core) {
 		this.match = match;
 		this.worker = worker;
 		this.core = core;
 	}
 	
 	public MatchRunner(Worker worker, BSScrimmageSet scrim, byte[] scrimData, int core) {
 		this.worker = worker;
 		this.scrim = scrim;
 		this.scrimData = scrimData;
 		this.core = core;
 	}
 	
 	public NetworkMatch getMatch() {
 		return match;
 	}
 	
 	public int getCore() {
 		return core;
 	}
 
 	/**
 	 * Safely halts the execution of the MatchRunner
 	 */
 	public void stop() {
 		running = false;
 		if (currentProcess != null) {
 			currentProcess.destroy();
 		}
 	}
 	
 	public boolean isRunning() {
 		return running;
 	}
 
 	private static void extractAndRenameTeam(String jarfile, String team) throws IOException {
 		JarFile jar = new JarFile(jarfile);
 		Enumeration<JarEntry> entries = jar.entries();
 		while (entries.hasMoreElements()) {
 			JarEntry file = entries.nextElement();
 			File f = new File(Config.teamsDir + "tmp" + File.separator + file.getName());
 			if (file.isDirectory()) {
 				f.mkdir();
 				continue;
 			}
 			BufferedReader br = new BufferedReader(new InputStreamReader(jar.getInputStream(file)));
 			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(f)));
 			String line;
 			while ((line = br.readLine()) != null) {
 				bw.write(line.replaceAll("package team[0-9]{3}", "package " + team).replaceAll("import team[0-9]{3}", "import " + team));
 				bw.newLine();
 			}
 			br.close();
 			bw.close();
 		}
 	}
 
 	public static void compilePlayer(String teamName, String jarFile) throws IOException {
 		File aPlayer = new File(Config.teamsDir + teamName);
 		if (!aPlayer.exists()) {
 			_log.info("Compiling player " + teamName);
 			aPlayer.mkdir();
 			File workingDir = new File(Config.teamsDir + "tmp");
 			workingDir.mkdir();
 			extractAndRenameTeam(jarFile, teamName);
 			Collection<File> srcFiles = FileUtils.listFiles(workingDir, new String[] {"java"}, true);
 			String[] srcFileNames = new String[srcFiles.size()];
 			int index = 0;
 			for (File f: srcFiles) {
 				srcFileNames[index++] = f.getAbsolutePath();
 			}
 			String[] javacArgs = new String[4 + srcFiles.size()];
 			javacArgs[0] = "-classpath";
 			javacArgs[1] = Config.battlecodeServerFile;
 			javacArgs[2] = "-d";
 			javacArgs[3] = Config.teamsDir;
 			System.arraycopy(srcFileNames, 0, javacArgs, 4, srcFiles.size());
 			com.sun.tools.javac.Main.compile(javacArgs);
 			FileUtils.deleteDirectory(workingDir);
 		}
 	}
 	
 	/**
 	 * Runs the battlecode match
 	 */
 	public void run() {
 		running = true;
 		if (match != null) {
 			runMatch();
 		} else if (scrim != null) {
 			analyzeMatch();
 		} else {
 			_log.error("MatchRunner is dazed and confused");
 		}
 		running = false;
 	}
 	
 	private void analyzeMatch() {
 		_log.info("Analyzing: " + scrim.getFileName());
 		// Read in the replay file
 		try {
 			GameAnalyzer ga = new GameAnalyzer(scrimData);
 			List<ScrimmageMatchResult> results = ga.analyzeScrimmageMatches();
 			if (results.isEmpty()) {
 				_log.error("Analysis turned up no results!");
 				worker.matchAnalyzed(this, core, null, STATUS.CANCELED);
 			}
 			scrim.setScrimmageMatches(results);
 			int[] wins = new int[TEAM.values().length];
 			for (ScrimmageMatchResult smr: results) {
 				wins[smr.getWinner().ordinal()]++;
 			}
 			if (wins[TEAM.A.ordinal()] > wins[TEAM.B.ordinal()]) {
 				scrim.setWinner(TEAM.A);
 			} else {
 				scrim.setWinner(TEAM.B);
 			}
 			scrim.setPlayerA(ga.getTeamA());
 			scrim.setPlayerB(ga.getTeamB());
 			scrim.setStatus(STATUS.COMPLETE);
 			
 			if (running) {
 				_log.info("Finished analyzing: " + scrim.getFileName());
 				worker.matchAnalyzed(this, core, scrim, STATUS.COMPLETE);
 			}
 			ga.close();
 		} catch (IOException e) {
 			_log.error("Error parsing scrimmage match data", e);
 			worker.matchAnalyzed(this, core, scrim, STATUS.CANCELED);
 		} catch (ClassNotFoundException e) {
 			_log.error("Error parsing scrimmage match data", e);
 			worker.matchAnalyzed(this, core, scrim, STATUS.CANCELED);
 		}
 	}
 	
 	private void runMatch() {
 		_log.info("Running: " + match);
 		if (Config.MOCK_WORKER) {
 			try {
 				Thread.sleep(1000 * Config.MOCK_WORKER_SLEEP);
 			} catch (InterruptedException e) {
 			}
 			if (running) {
 				worker.matchFinish(this, core, match, STATUS.COMPLETE, MatchResultImpl.constructMockMatchResult(), new byte[0]);
 			}
 			return;
 		}
 
		String team_a = match.team_a.replaceAll("\\W", "_");
		String team_b = match.team_b.replaceAll("\\W", "_");
 
 		try {
 			// Run the match inside this JVM if we're the first core.  Otherwise we need to start a new process
 			if (core == 0) {
				runMatch(match.seed, match.map.getMapName(), team_a, team_b);
 			} else {
 				_log.debug("Forking process");
 				Runtime run = Runtime.getRuntime();
				Process p = run.exec(new String[] {"/bin/bash", "run.sh", "-" + Main.runMatchArg, match.seed + "", match.map.getMapName(), team_a, team_b});
 				p.waitFor();
 			}
 			
 			String matchFile = match.seed + match.map.getMapName() + ".rms";
 			
 			if (!running) {
 				if (!new File(matchFile).delete()) {
 					_log.warn("Error deleting file: " + matchFile);
 				}
 				return;
 			}
 
 			// Read in the replay file
 			GameAnalyzer ga = new GameAnalyzer(matchFile);
 			List<MatchResultImpl> results = ga.analyzeMatches();
 			if (results.size() != 1) {
 				_log.error("Number of MatchResults is incorrect");
 				worker.matchFailed(this, core, match);
 			}
 			ga.close();
 			
 			byte[] data;
 			try {
 				data = BSUtil.getFileData(matchFile);
 			} catch (IOException e) {
 				if (running) {
 					_log.error("Failed to read " + matchFile, e);
 					worker.matchFailed(this, core, match);
 				}
 				return;
 			}
 			
 			File mf = new File(matchFile);
 			if (!mf.delete()) {
 				_log.warn("Error deleting file: " + matchFile);
 			}
 
 			if (running) {
 				_log.info("Finished: " + match);
 				worker.matchFinish(this, core, match, STATUS.COMPLETE, results.get(0), data);
 			}
 		} catch (IOException e) {
 			if (running) {
 				_log.error("Failed to run match", e);
 				worker.matchFailed(this, core, match);
 			}
 			return;
 		} catch (ClassNotFoundException e) {
 			if (running) {
 				_log.error("Failed to read match file", e);
 				worker.matchFailed(this, core, match);
 			}
 		} catch (InterruptedException e) {
 			if (running) {
 				_log.error("Interrupted while running match", e);
 				worker.matchFailed(this, core, match);
 			}
 		}
 	}
 	
 	public static void runMatch(long seed, String mapName, String team_a, String team_b) throws IOException {
 		// Construct the map file with the appropriate seeeeeed
 		File seededMap = new File(Config.mapsDir + seed + mapName + ".xml");
 		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(new File(Config.mapsDir + mapName + ".xml"))));
 		BufferedWriter fos = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(seededMap)));
 		String line;
 		while ((line = br.readLine()) != null) {
 			fos.write(line.replaceAll("seed=[^ ]*", "seed=\"" + seed + "\""));
 			fos.newLine();
 		}
 		br.close();
 		fos.close();
 		
 		battlecode.server.Config bcConfig = battlecode.server.Config.getGlobalConfig();
 		bcConfig.set("bc.engine.debug-methods", "false");
 		bcConfig.set("bc.game.maps", seed + mapName);
 		bcConfig.set("bc.game.team-a", "A" + team_a);
 		bcConfig.set("bc.game.team-b", "B" + team_b);
 		bcConfig.set("bc.server.mode", "headless");
         bcConfig.set("bc.engine.silence-a", "true");
         bcConfig.set("bc.engine.silence-b", "true");
 		Controller controller = ControllerFactory
 				.createHeadlessController(bcConfig);
 		Proxy[] proxies = new Proxy[] { 
 				ProxyFactory.createProxyFromFile(seed + mapName + ".rms"),
 		};
 		Server bcServer = new Server(bcConfig, Server.Mode.HEADLESS, controller, proxies);
 		controller.addObserver(bcServer);
 		bcServer.run();
 		
 		if (!seededMap.delete()) {
 			_log.warn("Error deleting file: " + seededMap.getPath());
 		}
 	}
 
 	@Override
 	public String toString() {
 		return match.toString();
 	}
 
 }
