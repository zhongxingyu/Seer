 package net.minecraft.launcher;
 
 import java.io.BufferedInputStream;
 import java.io.BufferedOutputStream;
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileFilter;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.net.InetSocketAddress;
 import java.net.PasswordAuthentication;
 import java.net.Proxy;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.UUID;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipFile;
 
 import javax.swing.JOptionPane;
 import javax.swing.SwingUtilities;
 
 import net.minecraft.bootstrap.EtagDatabase;
 import net.minecraft.launcher.authentication.AuthenticationService;
 import net.minecraft.launcher.process.JavaProcess;
 import net.minecraft.launcher.process.JavaProcessLauncher;
 import net.minecraft.launcher.process.JavaProcessRunnable;
 import net.minecraft.launcher.profile.LauncherVisibilityRule;
 import net.minecraft.launcher.profile.Profile;
 import net.minecraft.launcher.updater.LocalVersionList;
 import net.minecraft.launcher.updater.VersionList;
 import net.minecraft.launcher.updater.VersionSyncInfo;
 import net.minecraft.launcher.updater.download.DownloadJob;
 import net.minecraft.launcher.updater.download.DownloadListener;
 import net.minecraft.launcher.versions.CompleteVersion;
 import net.minecraft.launcher.versions.ExtractRules;
 import net.minecraft.launcher.versions.Library;
 
 import org.apache.commons.io.FileUtils;
 import org.apache.commons.io.filefilter.AgeFileFilter;
 import org.apache.commons.io.filefilter.DirectoryFileFilter;
 import org.apache.commons.io.filefilter.FileFilterUtils;
 import org.apache.commons.io.filefilter.IOFileFilter;
 import org.apache.commons.io.filefilter.PrefixFileFilter;
 import org.apache.commons.lang3.text.StrSubstitutor;
 
 public class GameLauncher implements JavaProcessRunnable, DownloadListener {
 	private final Object lock = new Object();
 	private final Launcher launcher;
 	private final List<DownloadJob> jobs = new ArrayList<DownloadJob>();
 	private CompleteVersion version;
 	private LauncherVisibilityRule visibilityRule;
 	private boolean isWorking;
 	private File nativeDir;
 
 	public GameLauncher(Launcher launcher) {
 		this.launcher = launcher;
 	}
 
 	public void addJob(DownloadJob job) {
 		synchronized (this.lock) {
 			this.jobs.add(job);
 		}
 	}
 
 	private void cleanOldNatives() {
 		File root = new File(this.launcher.getWorkingDirectory(), "versions/");
 		this.launcher.println("Looking for old natives to clean up...");
 		IOFileFilter ageFilter = new AgeFileFilter(
 				System.currentTimeMillis() - 3600L);
 
 		for (File version : root
 				.listFiles((FileFilter) DirectoryFileFilter.DIRECTORY))
 			for (File folder : version.listFiles((FileFilter) FileFilterUtils
 					.and(new IOFileFilter[] {
 							new PrefixFileFilter(version.getName()
 									+ "-natives-"), ageFilter }))) {
 				Launcher.getInstance().println("Deleting " + folder);
 
 				FileUtils.deleteQuietly(folder);
 			}
 	}
 
 	private String constructClassPath(CompleteVersion version) {
 		StringBuilder result = new StringBuilder();
 		Collection<File> classPath = version.getClassPath(
 				OperatingSystem.getCurrentPlatform(),
 				this.launcher.getWorkingDirectory());
 		String separator = System.getProperty("path.separator");
 
 		for (File file : classPath) {
 			if (!file.isFile())
 				throw new RuntimeException("Classpath file not found: " + file);
 			if (result.length() > 0)
 				result.append(separator);
 			result.append(file.getAbsolutePath());
 		}
 
 		return result.toString();
 	}
 
 	private String[] getMinecraftArguments(CompleteVersion version,
 			Profile selectedProfile, File gameDirectory, File assetsDirectory,
 			AuthenticationService authentication) {
 		if (version.getMinecraftArguments() == null) {
 			Launcher.getInstance().println(
 					"Can't run version, missing minecraftArguments");
 			setWorking(false);
 			return null;
 		}
 
 		Map<String, String> map = new HashMap<String, String>();
 		StrSubstitutor substitutor = new StrSubstitutor(map);
 		String[] split = version.getMinecraftArguments().split(" ");
 
 		map.put("auth_username", authentication.getUsername());
 		map.put("auth_session",
 				(authentication.getSessionToken() == null)
 						&& (authentication.canPlayOnline()) ? "-"
 						: authentication.getSessionToken());
 
 		if (authentication.getSelectedProfile() != null) {
 			map.put("auth_player_name", authentication.getSelectedProfile()
 					.getName());
 			map.put("auth_uuid", authentication.getSelectedProfile().getId());
 		} else {
 			map.put("auth_player_name", "Player");
 			map.put("auth_uuid", new UUID(0L, 0L).toString());
 		}
 
 		map.put("profile_name", selectedProfile.getName());
 		map.put("version_name", version.getId());
 
 		map.put("game_directory", gameDirectory.getAbsolutePath());
 		map.put("game_assets", assetsDirectory.getAbsolutePath());
 
 		for (int i = 0; i < split.length; i++) {
 			split[i] = substitutor.replace(split[i]);
 		}
 
 		return split;
 	}
 
 	protected float getProgress() {
 		synchronized (this.lock) {
 			float max = 0.0F;
 			float result = 0.0F;
 
 			for (DownloadJob job : this.jobs) {
 				float progress = job.getProgress();
 
 				if (progress >= 0.0F) {
 					result += progress;
 					max += 1.0F;
 				}
 			}
 
 			return result / max;
 		}
 	}
 
	public boolean hasActiveJobs() {
 		synchronized (this.lock) {
 			for (DownloadJob job : this.jobs) {
 				if (!job.isComplete())
 					return true;
 			}
 		}
 
 		return false;
 	}
 
	public boolean hasRemainingJobs() {
		synchronized (this.lock) {
			for (DownloadJob job : this.jobs) {
				if (!job.isFinished())
					return true;
			}
		}

		return false;
	}

 	public boolean isWorking() {
 		return this.isWorking;
 	}
 
 	protected void launchGame() {
 		this.launcher.println("Launching game");
 		Profile selectedProfile = this.launcher.getProfileManager()
 				.getSelectedProfile();
 
 		if (this.version == null) {
 			Launcher.getInstance().println("Aborting launch; version is null?");
 			return;
 		}
 
 		cleanOldNatives();
 
 		this.nativeDir = new File(this.launcher.getWorkingDirectory(),
 				"versions/" + this.version.getId() + "/" + this.version.getId()
 						+ "-natives-" + System.nanoTime());
 		if (!this.nativeDir.isDirectory())
 			this.nativeDir.mkdirs();
 		this.launcher.println("Unpacking natives to " + this.nativeDir);
 		try {
 			unpackNatives(this.version, this.nativeDir);
 		} catch (IOException e) {
 			Launcher.getInstance().println("Couldn't unpack natives!", e);
 			return;
 		}
 
 		File gameDirectory = selectedProfile.getGameDir() == null ? this.launcher
 				.getWorkingDirectory() : selectedProfile.getGameDir();
 		Launcher.getInstance().println("Launching in " + gameDirectory);
 
 		if (!gameDirectory.exists()) {
 			if (!gameDirectory.mkdirs()) {
 				Launcher.getInstance().println(
 						"Aborting launch; couldn't create game directory");
 			}
 		} else if (!gameDirectory.isDirectory()) {
 			Launcher.getInstance()
 					.println(
 							"Aborting launch; game directory is not actually a directory");
 			return;
 		}
 
 		JavaProcessLauncher processLauncher = new JavaProcessLauncher(
 				selectedProfile.getJavaPath(), new String[0]);
 		processLauncher.directory(gameDirectory);
 
 		File assetsDirectory = new File(this.launcher.getWorkingDirectory(),
 				"assets");
 
 		OperatingSystem os = OperatingSystem.getCurrentPlatform();
 		if (os.equals(OperatingSystem.OSX))
 			processLauncher
 					.addCommands(new String[] {
 							"-Xdock:icon="
 									+ new File(assetsDirectory,
 											"icons/minecraft.icns")
 											.getAbsolutePath(),
 							"-Xdock:name=Minecraft" });
 		else if (os.equals(OperatingSystem.WINDOWS)) {
 			processLauncher
 					.addCommands(new String[] { "-XX:HeapDumpPath=MojangTricksIntelDriversForPerformance_javaw.exe_minecraft.exe.heapdump" });
 		}
 
 		String profileArgs = selectedProfile.getJavaArgs();
 
 		if (profileArgs != null) {
 			processLauncher.addSplitCommands(profileArgs);
 		} else {
 			boolean is32Bit = "32".equals(System
 					.getProperty("sun.arch.data.model"));
 			String defaultArgument = is32Bit ? Profile.DEFAULT_JRE_ARGUMENTS_32BIT
 					: Profile.DEFAULT_JRE_ARGUMENTS_64BIT;
 			processLauncher.addSplitCommands(defaultArgument);
 		}
 
 		processLauncher.addCommands(new String[] { "-Djava.library.path="
 				+ this.nativeDir.getAbsolutePath() });
 		processLauncher.addCommands(new String[] { "-cp",
 				constructClassPath(this.version) });
 		processLauncher
 				.addCommands(new String[] { this.version.getMainClass() });
 
 		AuthenticationService auth = this.launcher.getProfileManager()
 				.getAuthDatabase().getByUUID(selectedProfile.getPlayerUUID());
 
 		String[] args = getMinecraftArguments(this.version, selectedProfile,
 				gameDirectory, assetsDirectory, auth);
 		if (args == null)
 			return;
 		processLauncher.addCommands(args);
 
 		Proxy proxy = this.launcher.getProxy();
 		PasswordAuthentication proxyAuth = this.launcher.getProxyAuth();
 		if (!proxy.equals(Proxy.NO_PROXY)) {
 			InetSocketAddress address = (InetSocketAddress) proxy.address();
 			processLauncher.addCommands(new String[] { "--proxyHost",
 					address.getHostName() });
 			processLauncher.addCommands(new String[] { "--proxyPort",
 					Integer.toString(address.getPort()) });
 			if (proxyAuth != null) {
 				processLauncher.addCommands(new String[] { "--proxyUser",
 						proxyAuth.getUserName() });
 				processLauncher.addCommands(new String[] { "--proxyPass",
 						new String(proxyAuth.getPassword()) });
 			}
 
 		}
 
 		processLauncher.addCommands(this.launcher.getAdditionalArgs());
 
 		if ((auth == null) || (auth.getSelectedProfile() == null)) {
 			processLauncher.addCommands(new String[] { "--demo" });
 		}
 
 		if (selectedProfile.getResolution() != null) {
 			processLauncher
 					.addCommands(new String[] {
 							"--width",
 							String.valueOf(selectedProfile.getResolution()
 									.getWidth()) });
 			processLauncher
 					.addCommands(new String[] {
 							"--height",
 							String.valueOf(selectedProfile.getResolution()
 									.getHeight()) });
 		}
 		try {
 			List<String> parts = processLauncher.getFullCommands();
 			StringBuilder full = new StringBuilder();
 			boolean first = true;
 
 			for (String part : parts) {
 				if (!first)
 					full.append(" ");
 				full.append(part);
 				first = false;
 			}
 
 			Launcher.getInstance().println("Running " + full.toString());
 			JavaProcess process = processLauncher.start();
 			process.safeSetExitRunnable(this);
 
 			if (this.visibilityRule != LauncherVisibilityRule.DO_NOTHING)
 				this.launcher.getFrame().setVisible(false);
 		} catch (IOException e) {
 			Launcher.getInstance().println("Couldn't launch game", e);
 			setWorking(false);
 			return;
 		}
 	}
 
 	@Override
 	public void onDownloadJobFinished(DownloadJob job) {
 		updateProgressBar();
 		EtagDatabase.getInstance().saveDatabase();
 		synchronized (this.lock) {
 			job.setChecked();
 			if (job.getFailures() > 0) {
 				this.launcher.println("Job '" + job.getName()
 						+ "' finished with " + job.getFailures()
 						+ " failure(s)!");
 				setWorking(false);
 			} else {
 				this.launcher.println("Job '" + job.getName()
 						+ "' finished successfully");
 
 				if ((isWorking()) && (!hasRemainingJobs())) {
 					try {
 						launchGame();
 					} catch (Throwable ex) {
 						Launcher.getInstance()
 								.println(
 										"Fatal error launching game. Report this to http://mojang.atlassian.net please!",
 										ex);
 					}
 				}
 			}
 		}
 	}
 
 	@Override
 	public void onDownloadJobProgressChanged(DownloadJob job) {
 		updateProgressBar();
 	}
 
 	@Override
 	public void onJavaProcessEnded(JavaProcess process) {
 		int exitCode = process.getExitCode();
 
 		if (exitCode == 0) {
 			Launcher.getInstance().println(
 					"Game ended with no troubles detected (exit code "
 							+ exitCode + ")");
 
 			if (this.visibilityRule == LauncherVisibilityRule.CLOSE_LAUNCHER)
 				SwingUtilities.invokeLater(new Runnable() {
 					@Override
 					public void run() {
 						GameLauncher.this.launcher
 								.println("Following visibility rule and exiting launcher as the game has ended");
 						GameLauncher.this.launcher.closeLauncher();
 					}
 				});
 			else if (this.visibilityRule == LauncherVisibilityRule.HIDE_LAUNCHER)
 				SwingUtilities.invokeLater(new Runnable() {
 					@Override
 					public void run() {
 						GameLauncher.this.launcher
 								.println("Following visibility rule and showing launcher as the game has ended");
 						GameLauncher.this.launcher.getFrame().setVisible(true);
 					}
 				});
 		} else {
 			Launcher.getInstance().println(
 					"Game ended with bad state (exit code " + exitCode + ")");
 			SwingUtilities.invokeLater(new Runnable() {
 				@Override
 				public void run() {
 					GameLauncher.this.launcher
 							.println("Ignoring visibility rule and showing launcher due to a game crash");
 					GameLauncher.this.launcher.getFrame().setVisible(true);
 				}
 			});
 			String errorText = null;
 			String[] sysOut = process.getSysOutLines().getItems();
 
 			for (int i = sysOut.length - 1; i >= 0; i--) {
 				String line = sysOut[i];
 				String crashIdentifier = "#@!@#";
 				int pos = line.lastIndexOf(crashIdentifier);
 
 				if ((pos >= 0)
 						&& (pos < line.length() - crashIdentifier.length() - 1)) {
 					errorText = line.substring(pos + crashIdentifier.length())
 							.trim();
 					break;
 				}
 			}
 
 			if (errorText != null) {
 				File file = new File(errorText);
 
 				if (file.isFile()) {
 					Launcher.getInstance().println(
 							"Crash report detected, opening: " + errorText);
 					InputStream inputStream = null;
 					try {
 						inputStream = new FileInputStream(file);
 						BufferedReader reader = new BufferedReader(
 								new InputStreamReader(inputStream));
 						StringBuilder result = new StringBuilder();
 						String line;
 						while ((line = reader.readLine()) != null) {
 							if (result.length() > 0)
 								result.append("\n");
 							result.append(line);
 						}
 
 						reader.close();
 					} catch (IOException e) {
 						Launcher.getInstance().println(
 								"Couldn't open crash report", e);
 					} finally {
 						EtagDatabase.closeSilently(inputStream);
 					}
 				} else {
 					Launcher.getInstance().println(
 							"Crash report detected, but unknown format: "
 									+ errorText);
 				}
 			}
 		}
 
 		setWorking(false);
 	}
 
 	public void playGame() {
 		synchronized (this.lock) {
 			if (this.isWorking) {
 				this.launcher
 						.println("Tried to play game but game is already starting!");
 				return;
 			}
 
 			setWorking(true);
 		}
 
 		SwingUtilities.invokeLater(new Runnable() {
 			@Override
 			public void run() {
 				GameLauncher.this.launcher.getLauncherPanel().getTabPanel()
 						.showConsole();
 			}
 		});
 		this.launcher.println("Getting syncinfo for selected version");
 
 		Profile profile = this.launcher.getProfileManager()
 				.getSelectedProfile();
 		String lastVersionId = profile.getLastVersionId();
 		VersionSyncInfo syncInfo = null;
 
 		if (profile.getLauncherVisibilityOnGameClose() == null)
 			this.visibilityRule = Profile.DEFAULT_LAUNCHER_VISIBILITY;
 		else {
 			this.visibilityRule = profile.getLauncherVisibilityOnGameClose();
 		}
 
 		if (lastVersionId != null) {
 			syncInfo = this.launcher.getVersionManager().getVersionSyncInfo(
 					lastVersionId);
 		}
 
 		if ((syncInfo == null) || (syncInfo.getLatestVersion() == null)) {
 			syncInfo = this.launcher.getVersionManager()
 					.getVersions(profile.getVersionFilter()).get(0);
 		}
 
 		if (syncInfo == null) {
 			Launcher.getInstance()
 					.println(
 							"Tried to launch a version without a version being selected...");
 			setWorking(false);
 			return;
 		}
 
 		synchronized (this.lock) {
 			this.launcher.println("Queueing library & version downloads");
 			try {
 				this.version = this.launcher.getVersionManager()
 						.getLatestCompleteVersion(syncInfo);
 			} catch (IOException e) {
 				Launcher.getInstance().println(
 						"Couldn't get complete version info for "
 								+ syncInfo.getLatestVersion(), e);
 				setWorking(false);
 				return;
 			}
 
 			if ((syncInfo.getRemoteVersion() != null)
 					&& (syncInfo.getLatestSource() != VersionSyncInfo.VersionSource.REMOTE)
 					&& (!this.version.isSynced())) {
 				try {
 					CompleteVersion remoteVersion = this.launcher
 							.getVersionManager().getRemoteVersionList()
 							.getCompleteVersion(syncInfo.getRemoteVersion());
 					this.launcher.getVersionManager().getLocalVersionList()
 							.removeVersion(this.version);
 					this.launcher.getVersionManager().getLocalVersionList()
 							.addVersion(remoteVersion);
 					((LocalVersionList) this.launcher.getVersionManager()
 							.getLocalVersionList()).saveVersion(remoteVersion);
 					this.version = remoteVersion;
 				} catch (IOException e) {
 					Launcher.getInstance().println(
 							"Couldn't sync local and remote versions", e);
 				}
 				this.version.setSynced(true);
 			}
 
 			if (!this.version.appliesToCurrentEnvironment()) {
 				String reason = this.version.getIncompatibilityReason();
 				if (reason == null)
 					reason = "This version is incompatible with your computer. Please try another one by going into Edit Profile and selecting one through the dropdown. Sorry!";
 				Launcher.getInstance().println(
 						"Version " + this.version.getId()
 								+ " is incompatible with current environment: "
 								+ reason);
 				JOptionPane.showMessageDialog(this.launcher.getFrame(), reason,
 						"Cannot play game", 0);
 				setWorking(false);
 				return;
 			}
 
 			if (this.version.getMinimumLauncherVersion() > 7) {
 				Launcher.getInstance().println(
 						"An update to your launcher is available and is required to play "
 								+ this.version.getId()
 								+ ". Please restart your launcher.");
 				setWorking(false);
 				return;
 			}
 
 			if (!syncInfo.isInstalled()) {
 				try {
 					VersionList localVersionList = this.launcher
 							.getVersionManager().getLocalVersionList();
 					if ((localVersionList instanceof LocalVersionList)) {
 						((LocalVersionList) localVersionList)
 								.saveVersion(this.version);
 						Launcher.getInstance().println(
 								"Installed " + syncInfo.getLatestVersion());
 					}
 				} catch (IOException e) {
 					Launcher.getInstance().println(
 							"Couldn't save version info to install "
 									+ syncInfo.getLatestVersion(), e);
 					setWorking(false);
 					return;
 				}
 			}
 			try {
 				DownloadJob job = new DownloadJob("Version & Libraries", false,
 						this);
 				addJob(job);
 				this.launcher.getVersionManager()
 						.downloadVersion(syncInfo, job);
 				job.startDownloading(this.launcher.getVersionManager()
 						.getExecutorService());
 			} catch (IOException e) {
 				Launcher.getInstance().println(
 						"Couldn't get version info for "
 								+ syncInfo.getLatestVersion(), e);
 				setWorking(false);
 				return;
 			}
 
 			// =========
 			this.launcher.println("Queueing mods downloads");
 			try {
 				DownloadJob job = new DownloadJob("Mods", false, this);
 				addJob(job);
 				this.launcher.getVersionManager().downloadMods(syncInfo, job);
 				job.startDownloading(this.launcher.getVersionManager()
 						.getExecutorService());
 			} catch (IOException e) {
 				Launcher.getInstance().println(
 						"Couldn't get mods for " + syncInfo.getLatestVersion(),
 						e);
 				setWorking(false);
 				return;
 			}
 		}
 	}
 
 	private void setWorking(boolean working) {
 		synchronized (this.lock) {
 			if (this.nativeDir != null) {
 				Launcher.getInstance().println("Deleting " + this.nativeDir);
 				if ((!this.nativeDir.isDirectory())
 						|| (FileUtils.deleteQuietly(this.nativeDir))) {
 					this.nativeDir = null;
 				} else {
 					Launcher.getInstance().println(
 							"Couldn't delete " + this.nativeDir
 									+ " - scheduling for deletion upon exit");
 					try {
 						FileUtils.forceDeleteOnExit(this.nativeDir);
 					} catch (Throwable localThrowable) {
 					}
 				}
 			}
 			this.isWorking = working;
 			SwingUtilities.invokeLater(new Runnable() {
 				@Override
 				public void run() {
 					GameLauncher.this.launcher.getLauncherPanel()
 							.getBottomBar().getPlayButtonPanel().checkState();
 				}
 			});
 		}
 	}
 
 	private void unpackNatives(CompleteVersion version, File targetDir)
 			throws IOException {
 		OperatingSystem os = OperatingSystem.getCurrentPlatform();
 		Collection<Library> libraries = version.getRelevantLibraries();
 
 		for (Library library : libraries) {
 			Map<OperatingSystem, String> nativesPerOs = library.getNatives();
 
 			if ((nativesPerOs != null) && (nativesPerOs.get(os) != null)) {
 				File file = new File(this.launcher.getWorkingDirectory(),
 						"libraries/"
 								+ library.getArtifactPath(nativesPerOs.get(os)));
 				ZipFile zip = new ZipFile(file);
 				ExtractRules extractRules = library.getExtractRules();
 				try {
 					Enumeration<?> entries = zip.entries();
 
 					while (entries.hasMoreElements()) {
 						ZipEntry entry = (ZipEntry) entries.nextElement();
 
 						if ((extractRules == null)
 								|| (extractRules.shouldExtract(entry.getName()))) {
 							File targetFile = new File(targetDir,
 									entry.getName());
 							if (targetFile.getParentFile() != null)
 								targetFile.getParentFile().mkdirs();
 
 							if (!entry.isDirectory()) {
 								BufferedInputStream inputStream = new BufferedInputStream(
 										zip.getInputStream(entry));
 
 								byte[] buffer = new byte[2048];
 								FileOutputStream outputStream = new FileOutputStream(
 										targetFile);
 								BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(
 										outputStream);
 								try {
 									int length;
 									while ((length = inputStream.read(buffer,
 											0, buffer.length)) != -1)
 										bufferedOutputStream.write(buffer, 0,
 												length);
 								} finally {
 									EtagDatabase
 											.closeSilently(bufferedOutputStream);
 									EtagDatabase.closeSilently(outputStream);
 									EtagDatabase.closeSilently(inputStream);
 								}
 							}
 						}
 					}
 				} finally {
 					zip.close();
 				}
 			}
 		}
 	}
 
 	protected void updateProgressBar() {
 		final float progress = getProgress();
		final boolean hasTasks = hasActiveJobs();
 
 		SwingUtilities.invokeLater(new Runnable() {
 			@Override
 			public void run() {
 				GameLauncher.this.launcher.getLauncherPanel().getProgressBar()
 						.setVisible(hasTasks);
 				GameLauncher.this.launcher.getLauncherPanel().getProgressBar()
 						.setValue((int) (progress * 100.0F));
 			}
 		});
 	}
 }
 
 /*
  * Location: Z:\home\vadim\.minecraft\launcher.jar Qualified Name:
  * net.minecraft.launcher.GameLauncher JD-Core Version: 0.6.2
  */
