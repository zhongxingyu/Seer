 package spaceshooters.bootstrap;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.net.URL;
 import java.net.URLConnection;
 
 public class Bootstrap {
 	
 	private URL versionURL;
 	private URL launcherURL;
 	
 	public Bootstrap() {
 		try {
 			versionURL = this.getRealURL(new URL("https://raw.github.com/Matterross/Spaceshooters-2-Launcher/master/stable_version.txt"));
 			launcherURL = this.getRealURL(new URL("http://bit.ly/YV3m4v"));
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	public URL getRealURL(URL url) throws IOException {
 		URLConnection con = url.openConnection();
 		con.connect();
 		String s = con.getHeaderField("Location");
 		URL real = (s != null ? new URL(s) : url);
 		return real;
 	}
 	
 	public boolean updatesAvailble() {
 		File versionFile = new File(getSpaceshootersPath() + "bin/launcher_version");
		File launcherJar = new File(getSpaceshootersPath() + "bin/launcher.jar");
		if (versionFile.exists() && launcherJar.exists()) {
 			try (BufferedReader v = new BufferedReader(new FileReader(versionFile)); BufferedReader r = new BufferedReader(new InputStreamReader(versionURL.openStream()))) {
 				String newestVersion = r.readLine();
 				String currentVersion = v.readLine();
 				if (newestVersion.equals(currentVersion)) {
 					return false;
 				} else {
 					return true;
 				}
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
		} else {
			return true;
 		}
		
 		return true;
 	}
 	
 	public void download() {
 		File file = new File(getSpaceshootersPath() + "bin/launcher.jar");
 		file.getParentFile().mkdirs();
 		try (InputStream in = launcherURL.openStream(); FileOutputStream out = new FileOutputStream(file)) {
 			byte[] data = new byte[1024];
 			int length = -1;
 			while ((length = in.read(data, 0, 1024)) != -1) {
 				out.write(data, 0, length);
 			}
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	public void runLauncher() {
 		try {
 			Runtime.getRuntime().exec("java -jar \"" + getSpaceshootersPath() + "bin/launcher.jar");
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	public static String getSpaceshootersPath() {
 		String os = System.getProperty("os.name").toLowerCase();
 		if (os.indexOf("win") >= 0) {
 			return System.getenv("APPDATA") + File.separator + "Spaceshooters 2" + File.separator;
 		} else if (os.indexOf("mac") >= 0) {
 			return System.getProperty("user.home") + File.separator + "Library" + File.separator + "Application Support" + File.separator + "Spaceshooters 2" + File.separator;
 		} else if (os.indexOf("nix") >= 0 || os.indexOf("nux") >= 0 || os.indexOf("aix") > 0) {
 			return System.getProperty("user.home") + File.separator + "Spaceshooters 2" + File.separator;
 		} else {
 			throw new RuntimeException("Sorry, but " + os + " is not supported!");
 		}
 	}
 	
 	public static void main(String[] args) {
 		Bootstrap boot = new Bootstrap();
 		if (boot.updatesAvailble()) {
 			boot.download();
 		}
 		boot.runLauncher();
 	}
 }
