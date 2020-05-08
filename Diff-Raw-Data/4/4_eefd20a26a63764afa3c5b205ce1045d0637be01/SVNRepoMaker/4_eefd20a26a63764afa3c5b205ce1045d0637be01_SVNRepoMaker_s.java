 package org.chaoticbits.collabcloud.vc.svn;
 
 import java.io.BufferedReader;
 import java.io.ByteArrayInputStream;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.util.List;
 import java.util.Scanner;
 
 import org.apache.log4j.PropertyConfigurator;
 import org.chaoticbits.collabcloud.codeprocessor.java.RecurseJavaFiles;
 import org.tmatesoft.svn.core.SVNCommitInfo;
 import org.tmatesoft.svn.core.SVNException;
 import org.tmatesoft.svn.core.SVNURL;
 import org.tmatesoft.svn.core.auth.BasicAuthenticationManager;
 import org.tmatesoft.svn.core.auth.ISVNAuthenticationManager;
 import org.tmatesoft.svn.core.internal.io.fs.FSRepositoryFactory;
 import org.tmatesoft.svn.core.io.ISVNEditor;
 import org.tmatesoft.svn.core.io.SVNRepository;
 import org.tmatesoft.svn.core.io.SVNRepositoryFactory;
 import org.tmatesoft.svn.core.io.diff.SVNDeltaGenerator;
 
 /**
  * This class is for buildling an SVN repo for testing purposes. It's mostly hard-coded for this reason.
  * @author andy
  * 
  */
 public class SVNRepoMaker {
 	private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(SVNRepoMaker.class);
 	private static ISVNAuthenticationManager andyProgrammer = new BasicAuthenticationManager("Andy Programmer <apmeneel@ncsu.edu>", "");
 	private static ISVNAuthenticationManager andyMeneely = new BasicAuthenticationManager("Andy Meneely <andy.meneely@gmail.com>", "");
 	private static ISVNAuthenticationManager kellyDoctor = new BasicAuthenticationManager("Kelly Doctor <andy@se.rit.edu>", "");
 
 	public static void main(String[] args) throws Exception {
 		PropertyConfigurator.configure("log4j.properties");
 
 		log.info("Set up FSRepositoryFactory...");
 		FSRepositoryFactory.setup();
 
 		log.info("Create the local repo...");
 		SVNURL svnurl = SVNRepositoryFactory.createLocalRepository(new File("testsvn/repo"), true, true);
 		SVNRepository repo = SVNRepositoryFactory.create(svnurl);
 
 		addTrunk(repo);
 		importProject(repo);
 		andyPChange(repo);
 		kellyDoctorChange(repo);
 		andyPChangeGreedy(repo);
 		andyMChangeGreedy(repo);
 
 		log.info("Done.");
 	}
 
 	private static void addTrunk(SVNRepository repo) throws SVNException {
 		log.info("Add trunk...");
 		repo.setAuthenticationManager(andyProgrammer);
 		ISVNEditor editor = repo.getCommitEditor("adding trunk", null /* locks */, true /* keepLocks */, null /* mediator */);
 		editor.openRoot(-1);
 		editor.addDir("trunk", null, -1);
 		editor.closeDir();
 		SVNCommitInfo commitInfo = editor.closeEdit();
 		log.info("Finished commit: " + commitInfo.toString());
 	}
 
 	private static void importProject(SVNRepository repo) throws Exception {
 		log.info("Import the project...");
 		repo.setAuthenticationManager(andyMeneely);
 		ISVNEditor editor = repo.getCommitEditor("import project", null /* locks */, true /* keepLocks */, null /* mediator */);
 		editor.openRoot(-1);
 		List<File> files = new RecurseJavaFiles(true).loadRecursive(new File("testsvn/mancala"));
 		for (File file : files) {
 			String svnpath = "";
 			svnpath = "trunk/" + file.getPath().replaceFirst("testsvn\\\\", "").replaceAll("\\\\", "/");
 			log.debug("Adding svnpath: " + svnpath);
 			if (file.isDirectory()) {
 				editor.addDir(svnpath, null, -1);
 				editor.closeDir();
 			} else {
 				editor.addFile(svnpath, null, -1);
 				editor.applyTextDelta(svnpath, null);
 				Scanner scanner = new Scanner(file);
 				StringBuffer sb = new StringBuffer();
				while (scanner.hasNext())
					sb.append(scanner.next());
 				scanner.close();
 				String checksum = new SVNDeltaGenerator().sendDelta(svnpath, new ByteArrayInputStream(sb.toString().getBytes()), editor, true);
 				editor.closeFile(svnpath, checksum);
 			}
 		}
 		SVNCommitInfo commitInfo = editor.closeEdit();
 		log.info("Finished commit: " + commitInfo.toString());
 	}
 
 	private static void andyPChange(SVNRepository repo) throws Exception {
 		log.info("Making Andy Programmer's change...");
 		repo.setAuthenticationManager(andyProgrammer);
 		ISVNEditor editor = repo.getCommitEditor("Small change to a file", null /* locks */, true /* keepLocks */, null /* mediator */);
 		Scanner scanner = new Scanner(new File("testsvn/mancala/player/TimedNegaScoutPlayer.java"));
 		StringBuffer sb = new StringBuffer();
 		while (scanner.hasNextLine()) {
 			String line = scanner.nextLine();
 			if (line.contains("public int getPlay(Board state) {"))
 				line = "public int getPlay(Board state) {//Making a modification for the sake of testing";
 			sb.append(line + "\r\n");
 		}
 		editor.openRoot(-1);
 		editor.openDir("trunk/mancala/player", 2);
 		editor.openFile("trunk/mancala/player/TimedNegaScoutPlayer.java", 2);
 		editor.applyTextDelta("trunk/mancala/player/TimedNegaScoutPlayer.java", null);
 		String checksum = new SVNDeltaGenerator().sendDelta("trunk/mancala/player/TimedNegaScoutPlayer.java", new ByteArrayInputStream(sb
 				.toString().getBytes()), editor, true);
 		editor.closeFile("trunk/mancala/player/TimedNegaScoutPlayer.java", checksum);
 		SVNCommitInfo commitInfo = editor.closeEdit();
 		log.info("Finished commit: " + commitInfo.toString());
 	}
 
 	private static void kellyDoctorChange(SVNRepository repo) throws Exception {
 		log.info("Making Kelly Doctor's change...");
 		repo.setAuthenticationManager(kellyDoctor);
 		ISVNEditor editor = repo.getCommitEditor("Another small change to a file", null /* locks */, true /* keepLocks */, null /* mediator */);
 		Scanner scanner = new Scanner(new File("testsvn/mancala/player/TimedNegaScoutPlayer.java"));
 		StringBuffer sb = new StringBuffer();
 		while (scanner.hasNextLine()) {
 			String line = scanner.nextLine();
 			if (line.contains("public int getPlay(Board state) {"))
 				line = "public int getPlay(Board state) {//Making another modification for the sake of testing";
 			sb.append(line + "\r\n");
 		}
 		editor.openRoot(-1);
 		editor.openDir("trunk/mancala/player", 3);
 		editor.openFile("trunk/mancala/player/TimedNegaScoutPlayer.java", 3);
 		editor.applyTextDelta("trunk/mancala/player/TimedNegaScoutPlayer.java", null);
 		String checksum = new SVNDeltaGenerator().sendDelta("trunk/mancala/player/TimedNegaScoutPlayer.java", new ByteArrayInputStream(sb
 				.toString().getBytes()), editor, true);
 		editor.closeFile("trunk/mancala/player/TimedNegaScoutPlayer.java", checksum);
 		SVNCommitInfo commitInfo = editor.closeEdit();
 		log.info("Finished commit: " + commitInfo.toString());
 	}
 
 	private static void andyPChangeGreedy(SVNRepository repo) throws Exception {
 		log.info("Making Andy Programmer's change to GreedyPlayer...");
 		repo.setAuthenticationManager(andyProgrammer);
 		ISVNEditor editor = repo.getCommitEditor("Another small change to a file", null /* locks */, true /* keepLocks */, null /* mediator */);
 		Scanner scanner = new Scanner(new File("testsvn/mancala/player/GreedyPlayer.java"));
 		StringBuffer sb = new StringBuffer();
 		while (scanner.hasNextLine()) {
 			String line = scanner.nextLine();
 			if (line.contains("for (int play = 0; play < Board.SLOT_WIDTH; play++) {"))
 				line = "for (int play = 0; play < Board.SLOT_WIDTH; play++) {//Making a modification for the sake of testing";
 			sb.append(line + "\r\n");
 		}
 		editor.openRoot(-1);
 		editor.openDir("trunk/mancala/player", -1);
 		editor.openFile("trunk/mancala/player/GreedyPlayer.java", -1);
 		editor.applyTextDelta("trunk/mancala/player/GreedyPlayer.java", null);
 		String checksum = new SVNDeltaGenerator().sendDelta("trunk/mancala/player/GreedyPlayer.java", new ByteArrayInputStream(sb.toString()
 				.getBytes()), editor, true);
 		editor.closeFile("trunk/mancala/player/GreedyPlayer.java", checksum);
 		SVNCommitInfo commitInfo = editor.closeEdit();
 		log.info("Finished commit: " + commitInfo.toString());
 	}
 
 	private static void andyMChangeGreedy(SVNRepository repo) throws Exception {
 		log.info("Making Andy Programmer's change to GreedyPlayer...");
 		repo.setAuthenticationManager(andyMeneely);
 		ISVNEditor editor = repo.getCommitEditor("Yet another small change to a file", null /* locks */, true /* keepLocks */, null /* mediator */);
 		Scanner scanner = new Scanner(new File("testsvn/mancala/player/GreedyPlayer.java"));
 		StringBuffer sb = new StringBuffer();
 		while (scanner.hasNextLine()) {
 			String line = scanner.nextLine();
 			if (line.contains("for (int play = 0; play < Board.SLOT_WIDTH; play++) {"))
 				line = "for (int play = 0; play < Board.SLOT_WIDTH; play++) {//Making another modification for the sake of testing";
 			sb.append(line + "\r\n");
 		}
 		editor.openRoot(-1);
 		editor.openDir("trunk/mancala/player", -1);
 		editor.openFile("trunk/mancala/player/GreedyPlayer.java", -1);
 		editor.applyTextDelta("trunk/mancala/player/GreedyPlayer.java", null);
 		String checksum = new SVNDeltaGenerator().sendDelta("trunk/mancala/player/GreedyPlayer.java", new ByteArrayInputStream(sb.toString()
 				.getBytes()), editor, true);
 		editor.closeFile("trunk/mancala/player/GreedyPlayer.java", checksum);
 		SVNCommitInfo commitInfo = editor.closeEdit();
 		log.info("Finished commit: " + commitInfo.toString());
 	}
 }
