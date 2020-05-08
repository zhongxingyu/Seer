 package com.oshmidt;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.FileReader;
 import java.io.IOException;
 import java.net.URL;
 import java.nio.channels.Channels;
 import java.nio.channels.ReadableByteChannel;
 import java.util.List;
 import java.util.Map;
 import java.util.Scanner;
 import java.util.Set;
 
 import org.apache.commons.cli.CommandLine;
 import org.apache.commons.cli.CommandLineParser;
 import org.apache.commons.cli.HelpFormatter;
 import org.apache.commons.cli.Options;
 import org.apache.commons.cli.ParseException;
 import org.apache.commons.cli.PosixParser;
 import org.eclipse.egit.github.core.Gist;
 import org.eclipse.egit.github.core.GistFile;
 import org.eclipse.egit.github.core.service.GistService;
 
 public class GistManager {
 
 	private List<Gist> gists;
 
 	private GistFetcher gistFetcher = new GistFetcher();
 
 	private GistRepository glfm = new GistLocalFileManager();
 
 	private User user = new User();
 
 	private static final String TYPE_GIST_ID = "com.oshmidt.gistManager.typeGistID";
 
 	private static Scanner scanner = new Scanner(System.in);
 
 	public GistManager(String[] args) {
 		Options options = new Options();
 
 		options.addOption("l", true, "login");
 		options.addOption("p", true, "password");
 		options.addOption("d", false, "download gists from github");
 		options.addOption("show", true, "show loaded gist list");
 		options.addOption("download", true,
 				"download files by gistId or download all gists files if run with \"all\" parameter");
 		options.addOption("h", "help", false, "print help to console");
 
 		
 
 		CommandLineParser parser = new PosixParser();
 		CommandLine cmd;
 		try {
 			cmd = parser.parse(options, args);
 			if (cmd.hasOption("l") && cmd.hasOption("p")) {
 				user.setLogin(cmd.getOptionValue("l"));
 				user.setPassword(cmd.getOptionValue("p"));
				gists = gistFetcher.loadGists(user);
				glfm.writeGists(gists);
 			} else if (cmd.hasOption("d")) {
 				user.importUser();
				gists = gistFetcher.loadGists(user);
				glfm.writeGists(gists);
 			} else {
 				gists = glfm.readGists();
 			}
 
 			if (cmd.hasOption("download")) {
 				if (cmd.getOptionValue("download").equals("all")) {
 					for (Gist gist : gists) {
 						glfm.writeFiles(gist);
 					}
 				} else {
 					glfm.writeFiles(findGist(cmd.getOptionValue("download")));
 				}
 			}
 			
 			
 			if (cmd.hasOption("h")) {
 				  HelpFormatter formatter = new HelpFormatter();
 				  formatter.printHelp("github gist client", "Read following instructions for tuning chat work", options,
 				  "Developed by oshmidt");
 			}
 			
 			
 			if (cmd.hasOption("show")) {
 				if (cmd.getOptionValue("show").equals("all")) {
 					showGists();
 				}
 			}
 
 			/*
 			 * if (cmd.hasOption("m")) { String count = cmd.getOptionValue("m");
 			 * System.out.println("m " + count); }
 			 */
 
 		} catch (ParseException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	public Gist findGist(String s) {
 		for (Gist gist : gists) {
 			if (gist.getId().equals(s)) {
 				return gist;
 			}
 		}
 		return null;
 	}
 
 	public void loadFiles(String login) throws IOException {
 
 		System.out.print(Messages.getString(TYPE_GIST_ID));
 		String gid = scanner.nextLine();
 
 		for (Gist gist : gists) {
 			if (gist.getId().equalsIgnoreCase(gid)) {
 				Map<String, GistFile> gistFiles = gist.getFiles();
 				Set<String> set = gistFiles.keySet();
 				for (String s : set) {
 					GistFile gf = gistFiles.get(s);
 					System.out.println(gf.getRawUrl());
 					URL website = new URL(gf.getRawUrl());
 					ReadableByteChannel rbc = Channels.newChannel(website
 							.openStream());
 					createUserFolder(login);
 					createUserFolder(login + "\\" + gist.getId());
 					String filename = login + "\\" + gist.getId() + "\\"
 							+ gf.getFilename();
 					FileOutputStream fos = new FileOutputStream(filename);
 					fos.getChannel().transferFrom(rbc, 0, 1 << 24);
 					fos.close();
 				}
 			}
 		}
 	}
 
 	public Boolean createUserFolder(String dir) {
 		File theDir = new File(dir);
 		if (!theDir.exists()) {
 			return theDir.mkdir();
 		}
 		return true;
 	}
 
 	public void uploadFiles(User user) throws IOException {
 
 		System.out.print(Messages.getString(TYPE_GIST_ID));
 		String gistId = scanner.nextLine();
 
 		for (Gist gist : gists) {
 			if (gist.getId().equalsIgnoreCase(gistId)) {
 				Map<String, GistFile> gistFiles = gist.getFiles();
 				Set<String> set = gistFiles.keySet();
 				for (String s : set) {
 					GistFile gf = gistFiles.get(s);
 					String filename = user.getLogin() + "\\" + gist.getId()
 							+ "\\" + gf.getFilename();
 					String con = readFileAsString(filename);
 					if (con != null) {
 						gf.setContent(con);
 						GistService service = new GistService();
 						service.getClient().setCredentials(user.getLogin(),
 								user.getPassword());
 						service.updateGist(gist);
 						System.out.println(Messages.getString("fileWasUpdated",
 								filename));
 					} else {
 						System.out.println(Messages.getString(
 								"com.oshmidt.gistManager.localFileNotExist", filename));
 					}
 
 				}
 			}
 		}
 	}
 
 	public void showGists() throws IOException {
 		if (gists != null) {
 			Messages.getString("com.oshmidt.gistManager.lineSeparator");
 			for (Gist gist : gists) {
 				showGist(gist);
 			}
 		} else {
 			System.out.println(Messages
 					.getString("com.oshmidt.gistManager.noLoadedGists"));
 		}
 	}
 	
 	
 	public void showGist(Gist gist){
 		System.out.println(Messages.getString("com.oshmidt.gistManager.lineSeparator"));
 		System.out.println(Messages.getString("com.oshmidt.gistManager.gistID") + gist.getId());
 		
 		Set<String> sett = gist.getFiles().keySet();
 		int i=0;
 		for (String s : sett) {
 			GistFile gf = gist.getFiles().get(s);
 			i++;
 			System.out.println(i + ": " + gf.getFilename());
 		}
 		
 	}
 	
 
 	public String[] readContent() throws IOException {
 		System.out.print(Messages
 				.getString("com.oshmidt.gistManager.typeFilepath"));
 		String filepath = scanner.nextLine();
 		String[] a = { filepath, readFileAsString(filepath) };
 		return a;
 	}
 
 	private String readFileAsString(String filePath) throws java.io.IOException {
 		if (new File(filePath).exists()) {
 			StringBuffer fileData = new StringBuffer(1000);
 			FileReader fr = new FileReader(filePath);
 			BufferedReader reader = new BufferedReader(fr);
 			char[] buf = new char[1024];
 			int numRead = 0;
 			while ((numRead = reader.read(buf)) != -1) {
 				String readData = String.valueOf(buf, 0, numRead);
 				fileData.append(readData);
 				buf = new char[1024];
 			}
 			reader.close();
 			return fileData.toString();
 		}
 		return null;
 
 	}
 
 	public String readString(String message) {
 		System.out.print(" " + message);
 		return scanner.nextLine();
 	}
 
 
 	public void addNewGist(Gist gist) {
 		try {
 			gistFetcher.addNewGist(user, gist);
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	public void loadGists() {
 		try {
 			gists = gistFetcher.loadGists(user);
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	public Gist loadGist(String gistId) {
 		try {
 			return gistFetcher.loadGist(gistId, user);
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			return null;
 		}
 	}
 
 	public void updateGist(Gist gist) {
 		try {
 			gistFetcher.updateGist(user, gist);
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	public void deleteGist(String gistId) {
 		try {
 			gistFetcher.deleteGist(user, gistId);
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 }
