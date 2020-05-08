 package app;
 
 import java.applet.Applet;
 import java.awt.Canvas;
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.Frame;
 import java.awt.Image;
 import java.awt.Insets;
 import java.awt.Panel;
 import java.awt.ScrollPane;
 import java.awt.Toolkit;
 import java.awt.event.ComponentEvent;
 import java.awt.event.ComponentListener;
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.RandomAccessFile;
 import java.net.URL;
 import java.util.Hashtable;
 import nativeadvert.browsercontrol;
 
 public final class appletviewer
 		implements ComponentListener
 {
 	private static Panel var_1f08;
 	private static Component var_1f10;
 	static boolean debug = false;
 	private static Applet _appletLoader;
 	static boolean inWindows;
 	private static boolean _in64Bits;
 	static Frame MainFrame;
 	private static ScrollPane var_1f50;
 	private static Canvas var_1f58;
 	private static float var_1f70;
 	private static float var_1f78 = 0.0F;
 	private static int[] _languageIds;
 	static String[] languageNames;
 	public static int var_1fa0;
 	public static int var_1fa8;
 	public static boolean var_1fb0;
 
 	private static File _configFile;
 	private static String _configUrl;
 	static Hashtable<String, String> configOur = new Hashtable<String, String>();
 	static Hashtable<String, String> configInner = new Hashtable<String, String>();
 
 	public final void componentMoved(ComponentEvent paramComponentEvent) {
 	}
 
 	public final void componentResized(ComponentEvent paramComponentEvent) {
 		sub_3809(2);
 	}
 
 	private static final void loadConfigValues() {
 		// reset configuration values
 		LanguageStrings.Load();
 		configInner.clear();
 		configOur.clear();
 
 		BufferedReader localBufferedReader = null;
 		try {
 			localBufferedReader = getConfigReader();
 			String configLine = null;
 			while ((configLine = localBufferedReader.readLine()) != null) {
 				configLine = configLine.trim();
 
 				if (configLine.startsWith("//") || configLine.startsWith("#")) {
 					// ignore line comments
 					continue;
 				} else if (configLine.startsWith("msg=")) {
 					// language string
 					configLine = configLine.substring(4);
 
 					int k = configLine.indexOf('=');
 					if (k != -1) {
 						String name = configLine.substring(0, k).trim().toLowerCase();
 						String value = configLine.substring(k + 1).trim();
 						LanguageStrings.Set(name, value);
 						if (debug) {
 							System.out.println("Message - name=" + name + " text=" + value);
 						}
 					}
 				} else if (configLine.startsWith("param=")) {
 					// config parameter
 					configLine = configLine.substring(6);
 					int k = configLine.indexOf('=');
 					if (k != -1) {
 						String name = configLine.substring(0, k).trim().toLowerCase();
 						String value = configLine.substring(k + 1).trim();
 						configInner.put(name, value);
 						if (debug) {
 							System.out.println("Innerconfig - variable=" + name + " value=" + value);
 						}
 					}
 				} else {
 					// other config variables
 					int k = configLine.indexOf('=');
 					if (k == -1) {
 						// ignore invalid lines
 						continue;
 					}
 
 					String name = configLine.substring(0, k).trim().toLowerCase();
 					String value = configLine.substring(k + 1).trim();
 					configOur.put(name, value);
 					if (debug) {
 						System.out.println("Ourconfig - variable=" + name + " value=" + value);
 					}
 				}
 			}
 		} catch (IOException ex) {
 			if (debug) {
 				ex.printStackTrace();
 			}
 			DialogFactory.ShowError(LanguageStrings.Get("err_load_config"));
 		} catch (Exception ex) {
 			if (debug) {
 				ex.printStackTrace();
 			}
 			DialogFactory.ShowError(LanguageStrings.Get("err_decode_config"));
 		} finally {
 			if (localBufferedReader != null) {
 				try {
 					localBufferedReader.close();
 				} catch (IOException ex) {
 				}
 			}
 		}
 	}
 
 	public final void componentHidden(ComponentEvent paramComponentEvent) {
 	}
 
 	private static final BufferedReader getConfigReader() throws IOException {
 		if (_configUrl != null) {
 			return new BufferedReader(new InputStreamReader(new URL(_configUrl).openStream()));
 		}
 		return new BufferedReader(new FileReader(_configFile));
 	}
 
 	static final int sub_260c(boolean paramBoolean) {
 		int i = DialogLanguage.GetChoiceIndex();
 		if (i < 0) {
 			return -1;
 		}
 		Preferences.Set("Language", Integer.toString(_languageIds[i]));
 		if (paramBoolean != true) {
 			removeadvert();
 		}
 		Preferences.Save();
 		return i;
 	}
 
 	public final void componentShown(ComponentEvent paramComponentEvent) {
 	}
 
 	private static final File getLocationForFile(String fileName, String cacheSubdir, int mode, String homeDir) {
 		String[] dirs = { "c:/rscache/", "/rscache/", "c:/windows/", "c:/winnt/", "c:/", homeDir, "/tmp/", "" };
 		String[] subDirs = { ".jagex_cache_" + mode, ".file_store_" + mode };
 
 		for (int i = 0; i < 2; i++) {
 			for (int j = 0; j < subDirs.length; j++) {
 				for (int k = 0; k < dirs.length; k++) {
 					String filePath = dirs[k] + subDirs[j] + "/" + (cacheSubdir == null ? "" : cacheSubdir + "/") + fileName;
 					RandomAccessFile dummyFile = null;
 					try {
 						File file = new File(filePath);
 						if ((i == 1) || file.exists()) {
 							String dir = dirs[k];
 							if ((i == 0) || (dir.length() == 0) || (new File(dir).exists())) {
 								new File(dirs[k] + subDirs[j]).mkdir();
 								if (cacheSubdir != null) {
 									new File(dirs[k] + subDirs[j] + "/" + cacheSubdir).mkdir();
 								}
 
 								// check if this file is read/write-able
 								dummyFile = new RandomAccessFile(file, "rw");
 								int l = dummyFile.read();
 								dummyFile.seek(0L);
 								dummyFile.write(l);
 								dummyFile.seek(0L);
 								dummyFile.close();
 
 								// looks like it is, return the path
 								return file;
 							}
 						}
 					} catch (Exception ex) {
 						if (debug) {
 							System.out.println("Unable to open/write: " + filePath);
 						}
 						try {
 							if (dummyFile != null) {
 								dummyFile.close();
 								dummyFile = null;
 							}
 						} catch (IOException ioEx) {
 						}
 					}
 				}
 			}
 		}
 
 		if (!debug) {
 			throw new RuntimeException();
 		}
 		throw new RuntimeException("Fatal - could not find ANY location for file: " + fileName);
 	}
 
 	public static final void Load(String resourcesName) {
 		boolean bool = Preferences.dummy;
 		debug = Boolean.getBoolean("com.jagex.debug");
 		if (debug) {
 			System.setErr(DialogDebug.GetInstance("Jagex host console"));
 			System.setOut(DialogDebug.GetInstance("Jagex host console"));
 			System.out.println("release #7");
 			System.out.println("java.version = " + System.getProperty("java.version"));
 			System.out.println("os.name = " + System.getProperty("os.name"));
 			System.out.println("os.arch = " + System.getProperty("os.arch"));
 		}
 
 		Preferences.Load();
 		LanguageStrings.Load();
 
 		MainFrame = new Frame();
 
 		// load window icon
 		File resourcesPath = new File(new File(System.getProperty("user.dir")).getParentFile(), resourcesName);
 		File iconPath = new File(resourcesPath, "jagexappletviewer.png");
 		System.out.println("Trying to load icon file: " + iconPath.getAbsolutePath());
 		if (iconPath.exists()) {
 			Image icon = Toolkit.getDefaultToolkit().getImage(iconPath.getAbsolutePath());
 			if (icon != null) {
 				MainFrame.setIconImage(icon);
 			}
 		}
 
 		// load 'loading' window
 		LoaderBox.Create();
 
 		// load config file
 		LoaderBox.SetProgressText(LanguageStrings.Get("loading_config"));
 
 		String configUrl = System.getProperty("com.jagex.config");
 		String configFile = System.getProperty("com.jagex.configfile");
 
 		if (configUrl == null) {
 			if (configFile == null) {
 				DialogFactory.ShowError(LanguageStrings.Get("err_missing_config"));
 			}
 			_configFile = new File(resourcesPath, configFile);
 			System.out.println("Config File is " + _configFile.getAbsolutePath());
 		} else {
 			_configUrl = configUrl;
 			System.out.println("Config URL is " + _configUrl);
 		}
 
 		loadConfigValues();
 
 		String newVersion = configOur.get("viewerversion");
 		if (newVersion != null) {
 			try {
 				if (Integer.parseInt(newVersion) > 100) {
 					DialogFactory.ShowOk(LanguageStrings.Get("new_version"));
 				}
 			} catch (NumberFormatException ex) {
 			}
 		}
 
 		int l = Integer.parseInt(configInner.get("modewhat")) + 32;
 
 		String cacheSubdir = configOur.get("cachesubdir");
 		String codeBase = configOur.get("codebase");
 
 		String osName = System.getProperty("os.name").toLowerCase();
 		String osArch = System.getProperty("os.arch").toLowerCase();
 		inWindows = osName.startsWith("win");
 		_in64Bits = (osArch.startsWith("amd64") || osArch.startsWith("x86_64"));
 
 		String homePath = null;
 		try {
 			homePath = System.getProperty("user.home");
 			if ((homePath != null) && !homePath.endsWith("/")) {
 				homePath += "/";
 			}
 		} catch (Exception ex) {
 		}
 		if (homePath == null) {
 			homePath = "~/";
 		}
 
 		// load browser control
 		LoaderBox.SetProgressText(LanguageStrings.Get("loading_app_resources"));
 		File browserControlFile = null;
 		try {
 			byte[] browserControlJar;
 			if (!_in64Bits) {
 				browserControlJar = downloadBinary(configOur.get("browsercontrol_win_x86_jar"), codeBase);
 				browserControlFile = getLocationForFile("browsercontrol.dll", cacheSubdir, l, homePath);
 
 				byte[] browserControlBinary = new ZippedFile(browserControlJar).Extract("browsercontrol.dll");
 				if (browserControlBinary == null) {
 					browserControlFile = null;
 					DialogFactory.ShowError(LanguageStrings.Get("err_verify_bc"));
 				}
 				saveFile(browserControlBinary, browserControlFile);
 			} else {
 				browserControlJar = downloadBinary(configOur.get("browsercontrol_win_amd64_jar"), codeBase);
 				browserControlFile = getLocationForFile("browsercontrol64.dll", cacheSubdir, l, homePath);
 
 				byte[] browserControlBinary = new ZippedFile(browserControlJar).Extract("browsercontrol64.dll");
 				if (browserControlBinary == null) {
 					browserControlFile = null;
 					DialogFactory.ShowError(LanguageStrings.Get("err_verify_bc64"));
 				}
 				saveFile(browserControlBinary, browserControlFile);
 			}
 			if (debug) {
 				System.out.println("dlldata : " + browserControlJar.length);
 			}
 		} catch (Exception ex) {
 			if (debug) {
 				ex.printStackTrace();
 			}
 			DialogFactory.ShowError(LanguageStrings.Get("err_load_bc"));
 		}
 
 		// load rs client
 		LoaderBox.SetProgressText(LanguageStrings.Get("loading_app"));
 		if (inWindows) {
 			Class_e.sub_ae5();
 		}
 
 		try {
 			byte[] loaderBinary = downloadBinary(configOur.get("loader_jar"), codeBase);
 			ClassLoaderZipFile loaderPackage = new ClassLoaderZipFile(loaderBinary);
 			_appletLoader = (Applet)loaderPackage.loadClass("loader").newInstance();
 			if (debug) {
 				System.out.println("loader_jar : " + loaderBinary.length);
 			}
 		} catch (Exception localException3) {
 			if (debug) {
 				localException3.printStackTrace();
 			}
 			DialogFactory.ShowError(LanguageStrings.Get("err_target_applet"));
 		}
 		LoaderBox.Hide();
 		Class_i.sub_7d4(-12660);
 
 		MainFrame.setTitle(configOur.get("title"));
 		int i2 = (inWindows ? Integer.parseInt(configOur.get("advert_height")) : 0);
 
 		int i3 = Integer.parseInt(configOur.get("window_preferredwidth"));
 
 		int i4 = Integer.parseInt(configOur.get("window_preferredheight"));
 		int i5 = 40;
 
 		Insets localInsets = MainFrame.getInsets();
 		MainFrame.setSize(i3 + (localInsets.left - -localInsets.right), i5 + localInsets.top + (i2 + i4) - -localInsets.bottom);
 		MainFrame.setLocationRelativeTo(null);
 		MainFrame.setVisible(true);
 		var_1f50 = new ScrollPane();
 		MainFrame.add(var_1f50);
 		var_1f08 = new Panel();
 		var_1f08.setBackground(Color.black);
 		var_1f08.setLayout(null);
 		var_1f50.add(var_1f08);
 
 		int i6 = (!"yes".equals(Preferences.Get("Member"))) ? 1 : 0;
 		i6 = 1;
 		if (inWindows && (i6 != 0)) {
 			var_1f58 = new Canvas();
 			var_1f08.add(var_1f58);
 		}
 
 		var_1f08.add(_appletLoader);
 		var_1f10 = new Class_a(LanguageStrings.Get("tandc"));
 		var_1f08.add(var_1f10);
 		MainFrame.doLayout();
 		sub_3809(-1);
 		var_1f50.doLayout();
 		if (inWindows) if (i6 != 0) {
 			while (true) {
 				if ((var_1f58.isDisplayable()) && (var_1f58.isShowing())) {
 					break; //break label1817;
 				}
 				try {
 					Thread.sleep(100L);
 				} catch (Exception localException4) {
 				}
 			}
 			try {
 				label1817:
 				System.load(browserControlFile.toString());
 				browsercontrol.create(var_1f58, configOur.get("adverturl"));
 				browsercontrol.resize(var_1f58.getSize().width, var_1f58.getSize().height);
 			} catch (Throwable localThrowable) {
 				if (debug) {
 					localThrowable.printStackTrace();
 				}
				//DialogFactory.ShowError(LanguageStrings.Get("err_create_advertising"));
				//return;
 			}
 		}
 
 		MainFrame.addWindowListener(MainWindowAdapter.GetInstance());
 		var_1f50.addComponentListener(new appletviewer());
 		_appletLoader.setStub(new Class_g());
 		_appletLoader.init();
 		_appletLoader.start();
 	}
 
   public static void removeadvert() {
     if (var_1f58 == null)
       return;
     if (browsercontrol.iscreated()) {
       browsercontrol.destroy();
     }
     var_1f08.remove(var_1f58);
     var_1f58 = null;
     sub_3809(2);
   }
 
   public static void readdadvert() {
     if (!inWindows || (var_1f58 != null))
       return;
     var_1f58 = new Canvas();
     var_1f08.add(var_1f58);
     sub_3809(2);
 
     while ((!var_1f58.isDisplayable()) || (!var_1f58.isShowing()))
     {
       try
       {
         Thread.sleep(100L);
       }
       catch (Exception localException) {
       }
     }
     try {
       browsercontrol.create(var_1f58, configOur.get("adverturl"));
       browsercontrol.resize(var_1f58.getSize().width, var_1f58.getSize().height);
     } catch (Throwable localThrowable) {
       if (debug) {
         localThrowable.printStackTrace();
       }
       DialogFactory.ShowError(LanguageStrings.Get("err_create_advertising"));
       return;
     }
   }
 
 	static final void Terminate() {
 		if (browsercontrol.iscreated()) {
 			browsercontrol.destroy();
 		}
 		System.exit(0);
 	}
 
 	private static final boolean saveFile(byte[] binaryData, File filePath) {
 		try {
 			FileOutputStream writer = new FileOutputStream(filePath);
 			writer.write(binaryData, 0, binaryData.length);
 			writer.close();
 			return true;
 		} catch (IOException localIOException) {
 			if (debug) {
 				localIOException.printStackTrace();
 			}
 			DialogFactory.ShowError(LanguageStrings.Get("err_save_file"));
 		}
 		return false;
 	}
 
   private static final void sub_3809(int paramInt)
   {
     int i = (var_1f58 == null) ? 0 : Integer.parseInt(configOur.get("advert_height"));
 
     int j = 40;
 
     int k = Integer.parseInt(configOur.get("applet_minwidth"));
 
     int l = Integer.parseInt(configOur.get("applet_minheight"));
 
     int i1 = Integer.parseInt(configOur.get("applet_maxwidth"));
     int i2 = Integer.parseInt(configOur.get("applet_maxheight"));
 
     Dimension localDimension = var_1f50.getSize();
 
     Insets localInsets = var_1f50.getInsets();
 
     int i3 = -localInsets.right + (localDimension.width + -localInsets.left);
 
     int i4 = -localInsets.bottom + -localInsets.top + localDimension.height;
 
     int i5 = i3;
     if (i5 < k)
     {
       i5 = k;
     }
     int i6 = -i + i4 - j;
     if (i6 < l)
     {
       i6 = l;
     }
     if ((i5 ^ 0xFFFFFFFF) < (i1 ^ 0xFFFFFFFF))
     {
       i5 = i1;
     }
     if (i6 > i2) {
       i6 = i2;
     }
 
     int i7 = i3;
 
     int i8 = i4;
     if ((k ^ 0xFFFFFFFF) < (i7 ^ 0xFFFFFFFF)) {
       i7 = k;
     }
     if (i8 < j + l + i)
     {
       i8 = j + (l + i);
     }
     var_1f08.setSize(i7, i8);
     if (var_1f58 != null)
     {
       var_1f58.setBounds((i7 + -i5) / 2, 0, i5, i);
     }
     _appletLoader.setBounds((-i5 + i7) / 2, i, i5, i6);
     var_1f10.setBounds((i7 - i5) / paramInt, i + i6, i5, j);
     if ((var_1f58 == null) || (!browsercontrol.iscreated()))
       return;
     browsercontrol.resize(var_1f58.getSize().width, var_1f58.getSize().height);
   }
 
 	private static final byte[] downloadBinary(String fileName, String baseUrl) {
 		byte[] buffer = new byte[300000];
 		int bufferLength = 0;
 
 		InputStream reader = null;
 		try {
 			reader = new URL(baseUrl + fileName).openStream();
 			int bytesRead;
 			while (
 				(buffer.length > bufferLength) &&
 				((bytesRead = reader.read(buffer, bufferLength, buffer.length - bufferLength)) > 0)
 			) {
 				var_1f78 += bytesRead;
 				bufferLength += bytesRead;
 				LoaderBox.SetProgressPercent((int)(var_1f78 / var_1f70 * 100.0F));
 			}
 		} catch (Exception ex) {
 			if (debug) {
 				ex.printStackTrace();
 			}
 			DialogFactory.ShowError(LanguageStrings.Get("err_downloading") + ": " + fileName);
 		} finally {
 			if (reader != null) {
 				try {
 					reader.close();
 				} catch (IOException ex) {
 				}
 			}
 		}
 
 		byte[] binary = new byte[bufferLength];
 		System.arraycopy(buffer, 0, binary, 0, bufferLength);
 		return binary;
 	}
 
 	static {
 		var_1f70 = 58988.0F;
 		_configFile = null;
 		_configUrl = null;
 	}
 
 } //class appletviewer
