 package worker;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.net.Socket;
 import java.net.UnknownHostException;
 import java.security.KeyStore;
 import java.security.NoSuchAlgorithmException;
 import java.util.Arrays;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import javax.net.ssl.KeyManager;
 import javax.net.ssl.KeyManagerFactory;
 import javax.net.ssl.SSLContext;
 import javax.net.ssl.SSLSocketFactory;
 import javax.net.ssl.TrustManager;
 import javax.net.ssl.TrustManagerFactory;
 
 import model.BSMap;
 import networking.Controller;
 import networking.Network;
 import networking.Packet;
 import networking.PacketCmd;
 
 import common.Config;
 import common.Dependencies;
 import common.NetworkMatch;
 import common.Util;
 
 /**
  * Connects to master and runs matches
  * @author stevearc
  *
  */
 public class Worker implements Controller, Runnable {
 	private Config config;
 	private Logger _log;
 	private Network network;
 	private MatchRunner[] running;
 	private boolean runWorker = true;
 	private SSLSocketFactory sf;
 
 	public Worker() throws Exception{
 		config = Config.getConfig();
 		_log = config.getLogger();
 		running = new MatchRunner[config.cores];
 
 		// Connect to the master using the certificate in the keystore
 		KeyStore keystore = KeyStore.getInstance("JKS");
 		keystore.load(new FileInputStream(config.keystore), config.keystore_pass.toCharArray());
 
 		TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
 		tmf.init(keystore);
 
 		SSLContext context = SSLContext.getInstance("TLS");
 		TrustManager[] trustManagers = tmf.getTrustManagers();
 
 		KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
 		kmf.init(keystore, config.keystore_pass.toCharArray());
 		KeyManager[] keyManagers = kmf.getKeyManagers();
 
 		context.init(keyManagers, trustManagers, null);
 		sf = context.getSocketFactory();
 	}
 
 	/**
 	 * Send match data to master
 	 * @param mr
 	 * @param match
 	 * @param status
 	 * @param winner
 	 * @param win_condition
 	 * @param a_points
 	 * @param b_points
 	 * @param data
 	 */
 	public synchronized void matchFinish(int core, NetworkMatch match, String status, int winner, 
 			int win_condition, double a_points, double b_points, byte[] data) {
 		Packet p = new Packet(PacketCmd.RUN_REPLY, new Object[] {match, status, winner, win_condition, a_points, b_points, data});
 		network.send(p);
 		running[core].stop();
 		running[core] = null;
 	}
 
 	@Override
 	public void run() {
 		while (runWorker) {
 			try {
 				if (network == null || !network.isConnected()) {
 					try {
 						Socket socket = sf.createSocket(config.server, config.port);
 						network = new Network(this, socket);
 						new Thread(network).start();
 						_log.info("Connecting to master");
 						network.send(new Packet(PacketCmd.INIT, new Object[]{config.cores}));
 					} catch (UnknownHostException e) {
 					} catch (IOException e) {
 					}
 				}
 				Thread.sleep(5000);
 			} catch (InterruptedException e) {
 				_log.log(Level.WARNING, "Interrupt exception", e);
 			}
 		}
 	}
 
 	private boolean battlecodeUpToDate(NetworkMatch nm) {
 		try {
 			if (!nm.battlecodeServerHash.equals(Util.convertToHex(Util.SHA1Checksum(config.install_dir + "/battlecode/lib/battlecode-server.jar")))) {
 				return false;
 			}
 			if (!nm.idataHash.equals(Util.convertToHex(Util.SHA1Checksum(config.install_dir + "/battlecode/idata")))) {
 				return false;
 			}
 		} catch (NoSuchAlgorithmException e) {
 			_log.log(Level.SEVERE, "Could not find SHA1 algorithm", e);
 			return false;
 		} catch (IOException e) {
 			_log.log(Level.SEVERE, "Error hashing battlecode install files", e);
 			return false;
 		}
 		return true;
 	}
 
 	private boolean haveMap(BSMap map) {
 		File mapFile = new File(config.install_dir + "/battlecode/maps/" + map.getMapName() + ".xml");
 		if (mapFile.exists()) {
 			try {
 				return map.getHash().equals(Util.convertToHex(Util.SHA1Checksum(mapFile.getAbsolutePath())));
 			} catch (NoSuchAlgorithmException e) {
 				_log.log(Level.SEVERE, "Can't find SHA1 hash!", e);
 			} catch (IOException e) {
 				_log.log(Level.SEVERE, "Can't read map file!", e);
 			}
 		}
 		return false;
 	}
 
 	private boolean havePlayer(String player) {
 		File playerFile = new File(config.install_dir + "/battlecode/teams/" + player + ".jar");
 		return playerFile.exists();
 	}
 
 	/**
 	 * Checks to make sure we have the right files.  Requests them if not.
 	 * @param match
 	 * @return
 	 */
 	private boolean resolveDependencies(NetworkMatch match) {
 		boolean allClear = true;
 		boolean needUpdate = false;
 		boolean needMap = false;
 		boolean needTeamA = false;
 		boolean needTeamB = false;
 		if (!battlecodeUpToDate(match)) {
 			allClear = false;
 			needUpdate = true;
 		}
 		if (!haveMap(match.map)) {
 			allClear = false;
 			needMap = true;
 		}
 		if (!havePlayer(match.team_a)) {
 			allClear = false;
 			needTeamA = true;
 		}
 		if (!havePlayer(match.team_b)) {
 			allClear = false;
 			needTeamB = true;
 		}
 		if (!allClear) {
 			_log.info("Requesting " + (needUpdate ? "battlecode-server.jar & idata, " : "") + 
 					(needMap ? "map: " + match.map + ", " : "") + 
 					(needTeamA ? "player: " + match.team_a + ", " : "") + 
 					(needTeamB ? "player: " + match.team_b : ""));
 			Packet requestPacket = new Packet(PacketCmd.REQUEST, new Object[]{match, needUpdate, needMap, needTeamA, needTeamB});
 			network.send(requestPacket);
 		}
 		return allClear;
 	}
 
 	private boolean fileEqualsData(File file, byte[] data) throws IOException {
 		if (file.length() != data.length) {
 			return false;
 		}
 		byte[] fileData = new byte[data.length];
 		FileInputStream fis = new FileInputStream(file);
 		fis.read(fileData);
 		return Arrays.equals(fileData, data);
 	}
 	
 	private void writeDataToFile(byte[] data, String fileName) throws IOException {
 		_log.info("Writing file: " + fileName);
 		File outputFile = new File(fileName);
 		if (outputFile.exists()) {
 			if (!fileEqualsData(outputFile, data)){
 				outputFile.delete();
 			}
 		}
 		FileOutputStream ostream = new FileOutputStream(fileName);
 		ostream.write(data);
 		ostream.close();
 	}
 
 	private void writeDependencies(Dependencies dep) {
 		if (dep == null) {
 			return;
 		}
 		try {
 			if (dep.battlecodeServer != null) {
 				writeDataToFile(dep.battlecodeServer, config.install_dir + "/battlecode/lib/battlecode-server.jar");
 			}
 			if (dep.idata != null) {
 				writeDataToFile(dep.idata, config.install_dir + "/battlecode/idata");
 			}
 			if (dep.map != null) {
 				writeDataToFile(dep.map, config.install_dir + "/battlecode/maps/" + dep.mapName + ".xml");
 			}
 			if (dep.teamA != null) {
 				writeDataToFile(dep.teamA, config.install_dir + "/battlecode/teams/" + dep.teamAName + ".jar");
 			}
 			if (dep.teamB != null) {
 				writeDataToFile(dep.teamB, config.install_dir + "/battlecode/teams/" + dep.teamBName + ".jar");
 			}
 		} catch (IOException e) {
 			_log.log(Level.SEVERE, "Could not create player or map file", e);
 		}
 	}
 
 	@Override
 	public synchronized void addPacket(Packet p) {
 		switch (p.getCmd()) {
 		case RUN:
 			try {
 				// Find a free core
 				int core;
 				for (core = 0; core < config.cores; core++) {
 					if (running[core] == null)
 						break;
 				}
 				// Could not find free core
 				if (core == config.cores)
 					break;
 
 				NetworkMatch match = (NetworkMatch) p.get(0);
 				Dependencies dep = (Dependencies) p.get(1);
 				writeDependencies(dep);
 				if (resolveDependencies(match)) {
 					MatchRunner m = new MatchRunner(this, (NetworkMatch) p.get(0), core);
 					new Thread(m).start();
 					running[core] = m;
 				}
 			} catch (Exception e) {
 				_log.warning("Error running match");
 			}
 			break;
 		case STOP:
 			_log.info("Received stop command");
 			for (int i = 0; i < config.cores; i++) {
 				if (running[i] != null) {
 					running[i].stop();
 					running[i] = null;
 				}
 			}
 			break;
 		default:
 			_log.warning("Unrecognized packet command: " + p.getCmd());
 		}
 	}
 
 	@Override
 	public synchronized void onDisconnect() {
 		for (int i = 0; i < config.cores; i++) {
 			if (running[i] != null) {
 				running[i].stop();
 				running[i] = null;
 			}
 		}
 		_log.info("Disconnected from master");
 	}
 }
