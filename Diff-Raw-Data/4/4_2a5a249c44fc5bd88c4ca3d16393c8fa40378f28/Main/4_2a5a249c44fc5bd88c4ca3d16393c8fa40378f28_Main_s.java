 package wombat.launcher;
 
 import java.awt.BorderLayout;
 import java.awt.Component;
 import java.awt.GridLayout;
 import java.io.*;
 import java.net.*;
 import java.util.*;
 
 import javax.swing.*;
 
 /**
  * Main entry point for Wombat launcher. 
  * 
  * This program is designed to:
  * - install Wombat into the current directory
  * - keep the current installation of Wombat up to date
  * - launch the newest version of Wombat 
  */
 public class Main {
	static final String UpdateSite = "http://www.cs.indiana.edu/cgi-pub/c211/wombat/";
 	static final URL UpdateVersionFile;
 	
 	static final File CurrentDir;
 	static final File VersionFile;
 	static final List<Version> Versions = new ArrayList<Version>();
 	
 	static final JFrame UpdateFrame = new JFrame("Downloading files...");
 	static final JProgressBar CurrentProgress = new JProgressBar();
 	static final JProgressBar OverallProgress = new JProgressBar();
 	static boolean UpdateFrameBuilt = false;
 	
 	static final String MyOS; 
 
 	static {
 		try {
 			
 			CurrentDir = new File(Main.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath()).getParentFile().getAbsoluteFile();
 			VersionFile = new File(CurrentDir, "version.txt");
 			
 			UpdateVersionFile = new URL(UpdateSite + "version.txt");
 			
 		} catch (MalformedURLException e) {
 			throw new Error("Oops. (x1)");
 		} catch (URISyntaxException e) {
 			throw new Error("Oops. (x2)");
 		}
 		
 		String os = System.getProperty("os.name").toLowerCase();
 		if (os.indexOf("win") != -1)
 			MyOS = "win";
 		else if (os.indexOf("win") != -1)
 			MyOS = "osx";
 		else if ((os.indexOf("nux") != -1) || (os.indexOf("nix") != -1))
 			MyOS = "linux";
 		else
 			MyOS = "unknown";
 	}
 	
 	/**
 	 * Run from the command line.
 	 * @param args Ignored.
 	 */
 	public static void main(String[] args) {
 		try {
 			loadVersionFile();
 			verifyFiles();
 			launch();
 			checkForUpdates();
 		} catch(Exception e) {
 			JOptionPane.showMessageDialog(
 					null, 
 					"Unable to launch Wombat, please try again in a few minutes.\n" +
 						"\n" + 
 						(e.getMessage().length() > 0 ? "Reason:\n" + e.getMessage() : ""),
 					"Unable to launch Wombat", JOptionPane.OK_OPTION);
 			System.exit(0);
 		}
 	}
 
 	/**
 	 * Load the version file from the current directory.
 	 * 
 	 * If it doesn't exist, proceed to do a full install.
 	 */
 	private static void loadVersionFile() {
 		try {
 			if (!(VersionFile.exists() && VersionFile.canRead())) {
 				if (JOptionPane.NO_OPTION == 
 						JOptionPane.showConfirmDialog(
 								null, 
 								"Wombat has not been installed.\n\n" + 
 									"Do you want to install it to this directory:\n" + 
 									CurrentDir.getAbsolutePath() + "\n" + 
 									"\n" + 
 									"NOTE: You will not be able to launch Wombat without installing.", 
 								"Install Wombat?", JOptionPane.YES_NO_OPTION))
 					System.exit(0);
 				
 				installAll();
 			}
 				
 			Scanner s = new Scanner(VersionFile);
 			while (s.hasNextLine())
 				Versions.add(new Version(s.nextLine()));
 			
 		} catch(Exception e) {
 			throw new Error("Unable to read version file.");
 		}
 	}
 
 	/**
 	 * Verify that all of the files described in version.txt exist.
 	 * 
 	 * If any file doesn't exist, notify the user then proceed to download them.
 	 * @throws IOException If we can't open a file.
 	 * @throws MalformedURLException If the version URL is bad.
 	 */
 	private static void verifyFiles() throws MalformedURLException, IOException {
 		List<Version> missing = new ArrayList<Version>();
 		
 		for (Version v : Versions) 
 			if (v.forOS(MyOS) && !new File(CurrentDir, v.Filename).exists())
 				missing.add(v);
 				
 		if (missing.size() > 0) {
 			String missingFiles = "";
 			for (Version v : missing)
 				missingFiles += "- " + v.Filename + "\n";
 					
 			if (JOptionPane.NO_OPTION == 
 					JOptionPane.showConfirmDialog(
 							null, 
 							"The following files are missing:\n" + 
 								missingFiles +  
 								"\n" + 
 								"Do you want to download the missing files?\n" +
 								"\n" + 
 								"NOTE: You will not be able to launch Wombat without these files.", 
 							"Missing files", JOptionPane.YES_NO_OPTION))
 				System.exit(0);
 			
 			installThese(missing);
 		}
 	}
 
 	/**
 	 * Launch Wombat.
 	 * @throws MalformedURLException If any of the JAR URLs break (shouldn't happen).
 	 * @throws ClassNotFoundException If the wombat.Wombat class doesn't exist.
 	 * @throws IllegalAccessException If we don't security permissions enough for Wombat.
 	 * @throws InstantiationException If the Wombat constructor doesn't exist.
 	 */
 	private static void launch() throws MalformedURLException, ClassNotFoundException, InstantiationException, IllegalAccessException {
 		// Get all of the JARs that we might need.
 		List<URL> urls = new ArrayList<URL>();
 		urls.add(CurrentDir.toURI().toURL());
 		for (Version v : Versions)
 			urls.add(new File(CurrentDir, v.Filename).toURI().toURL());
 
 		// Build a new class loader.
 		ClassLoader currentThreadClassLoader = Thread.currentThread().getContextClassLoader();
 		URLClassLoader cl = new URLClassLoader(urls.toArray(new URL[] {}), currentThreadClassLoader);
 		Thread.currentThread().setContextClassLoader(cl);
 
 		// Launch Wombat.
 		Class<?> cls = Class.forName("wombat.Wombat", true, cl);
 		try { cls.getField("AllowUpdate").setBoolean(null, true); } catch (Exception e) {}
 		cls.newInstance();
 	}
 
 	/**
 	 * Check for updates in the background.
 	 * @throws IOException If we have an issues getting the new version file.
 	 */
 	private static void checkForUpdates() throws IOException {
 		// Get the new version file.
 		String newVersionFile = download(UpdateVersionFile);
 		Scanner s = new Scanner(newVersionFile);
 		List<Version> newVersions = new ArrayList<Version>();
 		while (s.hasNextLine())
 			newVersions.add(new Version(s.nextLine()));
 		
 		// Check each new version against what we already have.
 		List<Version> toUpdate = new ArrayList<Version>();
 		for (Version newV : newVersions) {
 			boolean haveIt = false;
 			for (Version oldV : Versions) {
 				if (newV.Name.equals(oldV.Name)
 						&& ((newV.OS == null && oldV.OS == null)
 								|| (newV.OS != null && newV.OS.equals(oldV.OS)))
 						&& oldV.compareTo(newV) >= 0) {
 					haveIt = true;
 					break;
 				}
 			}
 			
 			if (!haveIt)
 				toUpdate.add(newV);
 		}
 		
 		// If there are any that we don't have, notify the user.
 		if (!toUpdate.isEmpty()) {
 			String updatedFiles = "";
 			for (Version v : toUpdate)
 				updatedFiles += "- " + v.Filename + "\n";
 					
 			if (JOptionPane.NO_OPTION == 
 					JOptionPane.showConfirmDialog(
 							null, 
 							"An update to Wombat is available.\n\n" +
 							"The following files have been updated:\n" + 
 								updatedFiles +  
 								"\n" + 
 								"Do you want to download the new files?\n",
 							"Update available", 
 							JOptionPane.YES_NO_OPTION))
 				return;
 			
 			installThese(toUpdate);
 			
 			JOptionPane.showMessageDialog(
 					null, 
 					"The updates have been downloaded.\n" + 
 							"They will be automatically installed the next time you restart Wombat.", 
 					"Updates installed", 
 					JOptionPane.OK_OPTION);
 		}
 	}
 	
 	/**
 	 * Build the Update frame.
 	 * 
 	 * Can be called as often as you want, there's a variable that only lets it be built once.
 	 */
 	private static void buildUpdateFrame() {
 		if (UpdateFrameBuilt)
 			return;
 		
 		// Build the frame that will show update progress.
 		UpdateFrame.setSize(400, 200);
 		UpdateFrame.setLocationByPlatform(true);
 		UpdateFrame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
 		UpdateFrame.setLayout(new BorderLayout());
 		
 		UpdateFrame.add(getSpacer(20, 20), BorderLayout.WEST);
 		UpdateFrame.add(getSpacer(20, 20), BorderLayout.EAST);
 		UpdateFrame.add(getSpacer(20, 20), BorderLayout.SOUTH);
 		
 		// Add the status bars.
 		JPanel progressPanel = new JPanel();
 		progressPanel.setLayout(new GridLayout(4, 1));
 		progressPanel.add(new JLabel("Current file:"));
 		progressPanel.add(CurrentProgress);
 		progressPanel.add(new JLabel("Overall:"));
 		progressPanel.add(OverallProgress);
 		UpdateFrame.add(progressPanel, BorderLayout.CENTER);
 
 		CurrentProgress.setStringPainted(true);
 		OverallProgress.setStringPainted(true);
 		
 		UpdateFrameBuilt = true;
 	}
 	
 	/**
 	 * Create an empty panel to use as a spacer.
 	 * @param width The new panel's minimum width.
 	 * @param height The new panel's minimum height.
 	 * @return The new panel.
 	 */
 	private static Component getSpacer(int width, int height) {
 		JPanel spacer = new JPanel();
 		spacer.setSize(width, height);
 		return spacer;
 	}
 
 	/**
 	 * Download the version file and install all of the files described therein.
 	 * @throws IOException If we can't get the version file.
 	 */
 	private static void installAll() throws IOException {
 		// Download the version file.
 		download("version information", UpdateVersionFile, VersionFile);
 		loadVersionFile();
 		
 		// Now get all of the 'missing' files.
 		List<Version> toInstall = new ArrayList<Version>();
 		for (Version v : Versions)
 			if (v.forOS(MyOS))
 				toInstall.add(v);
 		installThese(toInstall);
 	}
 	
 	/**
 	 * Install just these missing files.
 	 * @param toInstall The versions to install.
 	 * @throws IOException If a file couldn't be downloaded.
 	 * @throws MalformedURLException If any of the file URLs are bad.
 	 */
 	private static void installThese(List<Version> toInstall) throws MalformedURLException, IOException {
 		buildUpdateFrame();
 		UpdateFrame.setVisible(true);
 		
 		// Update the files.
 		OverallProgress.setMaximum(toInstall.size());
 		int i = 0;
 		final int maxi = toInstall.size();
 		for (Version v : toInstall) {
 			final int updatei = i;
 			SwingUtilities.invokeLater(new Runnable() {
 				public void run() {
 					OverallProgress.setString(updatei + "/" + maxi);
 					OverallProgress.setValue(updatei);
 				}
 			});
 			i++;
 			
 			// Grab the file (this will update the progress bars as well)
 			download(v.Name, new URL(UpdateSite + v.Filename), new File(CurrentDir, v.Filename));
 			
 			// Update that file's version.
 			boolean hadOldVersion = false;
 			for (Version oldV : Versions) {
 				if (v.Name.equals(oldV.Name)
 					&& ((v.OS == null && oldV.OS == null)
 							|| (v.OS != null && v.OS.equals(oldV.OS)))) {
 					
 					hadOldVersion = true;
 					oldV.Filename = v.Filename;
 					oldV.OS = v.OS;
 					oldV.VersionString = v.VersionString;
 					oldV.Version.clear();
 					oldV.Version.addAll(v.Version);
 					break;
 					
 				}
 			}
 			if (!hadOldVersion)
 				Versions.add(v);
 		}
 		
 		// Update the local version file.
 		StringBuilder sb = new StringBuilder();
 		for (Version v : Versions) {
 			sb.append(v.toString());
 			sb.append("\n");
 		}
 		PrintWriter pw = new PrintWriter(VersionFile);
 		pw.write(sb.toString());
 		pw.close();
 		
 		UpdateFrame.setVisible(false);
 	}
 	
 	/**
 	 * Download a file into a string.
 	 * 
 	 * @param from
 	 *            The source URL.
 	 * @return The contents as a string.
 	 */
 	static String download(final URL from) throws IOException {
 		URLConnection connection = from.openConnection();
 		final int length = connection.getContentLength();
 		
 		SwingUtilities.invokeLater(new Runnable() {
 			public void run() {
 				CurrentProgress.setIndeterminate(false);
 				CurrentProgress.setMinimum(0);
 				CurrentProgress.setValue(0);
 				if (length != -1)
 					CurrentProgress.setMaximum(length);
 				else
 					CurrentProgress.setIndeterminate(true);
 			}
 		});
 		
 		StringBuilder out = new StringBuilder();
 		BufferedInputStream in = new BufferedInputStream(from.openStream());
 
 		int count;
 		byte data[] = new byte[1024];
 		while ((count = in.read(data, 0, 1024)) > 0) {
 			out.append(new String(data, 0, count));
 			
 			
 			if (length != -1) {
 				final int updateCount = count;
 				SwingUtilities.invokeLater(new Runnable() {
 					public void run() {
 						CurrentProgress.setValue(CurrentProgress.getValue() + updateCount);
 					}
 				});
 			}
 		}
 		
 		if (length != -1) {
 			SwingUtilities.invokeLater(new Runnable() {
 				public void run() {
 					CurrentProgress.setValue(CurrentProgress.getMaximum());
 				}
 			});
 		}
 		in.close();
 
 		return out.toString();
 	}
 
 	/**
 	 * Download a file to another file.
 	 * 
 	 * @param from The source URL.
 	 * @param to The destination file.
 	 */
 	static void download(final String name, final URL from, final File to) throws IOException {
 		URLConnection connection = from.openConnection();
 		final int length = connection.getContentLength();
 		
 		final int overallMax = OverallProgress.getMaximum();
 		final int overallNow = OverallProgress.getValue();
 		
 		SwingUtilities.invokeLater(new Runnable() {
 			public void run() {
 				CurrentProgress.setString(name);
 				CurrentProgress.setIndeterminate(false);
 				CurrentProgress.setMinimum(0);
 				CurrentProgress.setValue(0);
 				if (length != -1) {
 					CurrentProgress.setMaximum(length);
 					
 					OverallProgress.setMaximum(length * overallMax);
 					OverallProgress.setValue(length * overallNow);
 				} else {
 					CurrentProgress.setIndeterminate(true);
 				}
 			}
 		});
 		
 		BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(to));
 		BufferedInputStream in = new BufferedInputStream(connection.getInputStream());
 
 		int count;
 		byte data[] = new byte[10240];
 		while ((count = in.read(data, 0, 10240)) > 0) {
 			if (length != -1) {
 				final int updateCount = count;
 				SwingUtilities.invokeLater(new Runnable() {
 					public void run() {
 						CurrentProgress.setValue(CurrentProgress.getValue() + updateCount);
 						OverallProgress.setValue(OverallProgress.getValue() + updateCount);
 					}
 				});
 			}
 			out.write(data, 0, count);
 		}
 		if (length != -1) {
 			SwingUtilities.invokeLater(new Runnable() {
 				public void run() {
 					CurrentProgress.setValue(CurrentProgress.getMaximum());
 					
 					OverallProgress.setMaximum(overallMax);
 					OverallProgress.setValue(overallNow);
 				}
 			});
 		}
 
 		out.close();
 		in.close();
 	}
 }
