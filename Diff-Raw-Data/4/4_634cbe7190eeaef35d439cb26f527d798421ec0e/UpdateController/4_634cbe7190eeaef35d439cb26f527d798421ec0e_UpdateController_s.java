 // %1730913863:de.hattrickorganizer.tools.updater%
 /*
  * Created on 16.05.2004
  *
  */
 package de.hattrickorganizer.tools.updater;
 
 import gui.UserParameter;
 
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Hashtable;
 import java.util.Iterator;
 import java.util.Vector;
 import java.util.zip.ZipFile;
 
 import javax.swing.JOptionPane;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.NodeList;
 
 import plugins.IOfficialPlugin;
 import plugins.IPlugin;
 import de.hattrickorganizer.gui.HOMainFrame;
 import de.hattrickorganizer.gui.login.LoginWaitDialog;
 import de.hattrickorganizer.model.Extension;
 import de.hattrickorganizer.model.HOMiniModel;
 import de.hattrickorganizer.model.HOParameter;
 import de.hattrickorganizer.model.HOVerwaltung;
 import de.hattrickorganizer.model.News;
 import de.hattrickorganizer.net.MyConnector;
 import de.hattrickorganizer.tools.HOLogger;
 import de.hattrickorganizer.tools.HelperWrapper;
 import de.hattrickorganizer.tools.ZipHelper;
 
 /**
  * DOCUMENT ME!
  * 
  * @author Thorsten Dietz
  * 
  * @since 1.35
  */
 public final class UpdateController {
 	// ~ Static fields/initializers
 	// -----------------------------------------------------------------
 	private static File zip = null;
 	private static File tmp = null;
 
 	public static final String PLUGINS_HOMEPAGE = "http://ho1.sourceforge.net/onlinefiles";
 	protected static final String WEB_FLAGSFILE = PLUGINS_HOMEPAGE + "/xml/flags.zip";
 	protected static final String WEB_PLUGINFILE = PLUGINS_HOMEPAGE + "/xml/pluginVersionen.xml";
 
 	// ~ Methods
 	// ------------------------------------------------------------------------------------
 
 	/**
 	 * Show the plugins dialog that allows to remove a plugin.
 	 */
 	public static void showDeletePluginDialog() {
 		try {
 			DeleteDialog dialog = new DeleteDialog();
 			dialog.setVisible(true);
 		} catch (Exception e1) {
 			HOLogger.instance().log(UpdateController.class, e1);
 		}
 	}
 
 	/**
 	 * Show the language file update dialog.
 	 */
 	public static void showLanguageUpdateDialog() {
 		try {
 			File file = createXMLFile(PLUGINS_HOMEPAGE + "/xml/languages.xml",
 					new File(System.getProperty("user.dir") + File.separator + "sprache" + File.separator
 							+ "languages.xml"));
 
 			Document doc = UpdateHelper.instance().getDocument(file);
 
 			Hashtable<String, HPLanguageInfo> list = getWebLanguages(
 					doc.getDocumentElement().getChildNodes(), new Hashtable<String, HPLanguageInfo>());
 
 			LanguagesDialog dialog = new LanguagesDialog(list);
 
 			dialog.setVisible(true);
 		} catch (Exception e1) {
 			HOLogger.instance().log(UpdateController.class, e1);
 		}
 	}
 
 	/**
 	 * Show the library update dialog.
 	 */
 	public static void showPluginUpdaterLibraries() {
 		try {
 			File file = createXMLFile(WEB_PLUGINFILE, getLocalXMLFile());
 
 			if (file == null) {
 				JOptionPane.showMessageDialog(null, "Where is my xml file?", "Error",
 						JOptionPane.ERROR_MESSAGE);
 				return;
 			}
 
 			Document doc = UpdateHelper.instance().getDocument(file);
 			ArrayList<HPPluginInfo> tmp = new ArrayList<HPPluginInfo>();
 			ArrayList<HPPluginInfo> nonVisibles = new ArrayList<HPPluginInfo>();
 			ArrayList<HPPluginInfo> list = UpdateHelper.instance().getWebPlugins(
 					doc.getDocumentElement().getChildNodes(), new ArrayList<HPPluginInfo>(), tmp);
 
 			nonVisibles = list;
 			list = tmp;
 
 			Vector<IPlugin> v = HelperWrapper.instance().getPlugins();
 			int listSize = list.size();
 
 			for (int i = 0; i < listSize; i++) {
 				HPPluginInfo hpp = (HPPluginInfo) list.get(i);
 
 				for (Iterator<IPlugin> iter = v.iterator(); iter.hasNext();) {
 					IPlugin element = iter.next();
 
 					if (element instanceof IOfficialPlugin
 							&& (hpp.getPluginId() == ((IOfficialPlugin) element).getPluginID())) {
 						hpp.setOfficialPlugin((IOfficialPlugin) element);
 					}
 					// if
 				}
 				// for iter
 			}
 			// for list
 
 			RefreshDialog dialog = new RefreshDialog(list);
 			dialog.setOtherPlugins(nonVisibles);
 			dialog.setVisible(true);
 		} catch (Exception e1) {
 			HOLogger.instance().log(UpdateController.class, e1);
 		}
 	}
 
 	/**
 	 * Show the normal plugins update dialog.
 	 */
 	public static void showPluginUpdaterNormal() {
 		try {
 			File file = createXMLFile(WEB_PLUGINFILE, getLocalXMLFile());
 
 			if (file == null) {
 				JOptionPane.showMessageDialog(null, "Where is my xml file?", "Error",
 						JOptionPane.ERROR_MESSAGE);
 				return;
 			}
 
 			Document doc = UpdateHelper.instance().getDocument(file);
 			ArrayList<HPPluginInfo> tmp = new ArrayList<HPPluginInfo>();
 			ArrayList<HPPluginInfo> nonVisibles = new ArrayList<HPPluginInfo>();
 
 			ArrayList<HPPluginInfo> list = UpdateHelper.instance().getWebPlugins(
 					doc.getDocumentElement().getChildNodes(), new ArrayList<HPPluginInfo>(), tmp);
 
 			nonVisibles = tmp;
 
 			Vector<IPlugin> v = HelperWrapper.instance().getPlugins();
 			int listSize = list.size();
 
 			for (int i = 0; i < listSize; i++) {
 				HPPluginInfo hpp = list.get(i);
 
 				for (Iterator<IPlugin> iter = v.iterator(); iter.hasNext();) {
 					IPlugin element = iter.next();
 
 					if (element instanceof IOfficialPlugin
 							&& (hpp.getPluginId() == ((IOfficialPlugin) element).getPluginID())) {
 						hpp.setOfficialPlugin((IOfficialPlugin) element);
 					}
 					// if
 				}
 				// for iter
 			}
 			// for list
 
 			RefreshDialog dialog = new RefreshDialog(list);
 			dialog.setOtherPlugins(nonVisibles);
 			dialog.setVisible(true);
 		} catch (Exception e1) {
 			HOLogger.instance().log(UpdateController.class, e1);
 		}
 	}
 
 	/**
 	 * Download latest flags from the external space.
 	 */
 	public static void updateFlags() {
 		try {
 			UpdateHelper.instance().download(WEB_FLAGSFILE, getLocalZipFile());
 			ZipHelper.unzip(getLocalZipFile(), new File( System.getProperty("user.dir")));
 			JOptionPane.showMessageDialog(null,
 					HOVerwaltung.instance().getLanguageString("NeustartErforderlich"), "HO!",
 					JOptionPane.INFORMATION_MESSAGE);
 		} catch (Exception e1) {
 			HOLogger.instance().log(UpdateController.class, e1);
 		}
 	}
 
 	/**
 	 * Create an zip file in the systems temp folder.
 	 */
 	protected static File getLocalZipFile() throws IOException {
 		if (zip == null) {
 			zip = File.createTempFile("tmp", "zip");
 		}
 		return zip;
 	}
 
 	/**
 	 * Create an xml file in the systems temp folder.
 	 */
 	private static File getLocalXMLFile() throws IOException {
 		if (tmp == null) {
 			tmp = File.createTempFile("tmp", "xml");
 		}
 		return tmp;
 	}
 
 	/**
 	 * analyse the /sprache/languages.xml file and creates a hashtable
 	 */
 	private static Hashtable<String, HPLanguageInfo> getWebLanguages(NodeList elements,
 			Hashtable<String, HPLanguageInfo> list) {
 		HPLanguageInfo tmp = null;
 		Element element = null;
 
 		for (int i = 0; i < elements.getLength(); i++) {
 			if (elements.item(i) instanceof Element) {
 				element = (Element) elements.item(i);
 
 				if (element.getTagName().equals("property")) {
 					tmp = HPLanguageInfo.instance(element.getChildNodes());
 					list.put(tmp.getFilename(), tmp);
 				}
 			}
 		}
 
 		return list;
 	}
 
 	/**
 	 * Download the xml file from Web and save it local
 	 */
 	private static File createXMLFile(String url, File tmp) throws Exception {
 		boolean showDialog = false;
 		String content = "";
 
 		try {
 			content = MyConnector.instance().getUsalWebPage(url, showDialog);
 		} catch (Exception ex) {
 			if (tmp.exists()) {
 				return tmp;
 			}
 			return null;
 		}
 
 		if (tmp.exists()) {
 			tmp.delete();
 		}
 
 		FileWriter writer = new FileWriter(tmp);
 		writer.write(content);
 		writer.flush();
 		writer.close();
 
 		return tmp;
 	}
 
 	/**
 	 * Check the external site for the latest release version.
 	 */
 	public static void check4update() {
 		VersionInfo version = MyConnector.instance().getLatestVersion();
 		if (version != null && 
 				(version.getVersion() > HOMainFrame.VERSION || 
				(version.getVersion() == HOMainFrame.VERSION && version.isBeta()))
 			) {
 			int update = JOptionPane.showConfirmDialog(HOMainFrame.instance(),
 					HOVerwaltung.instance().getLanguageString("updateMSG") + "\n"
 							+ HOVerwaltung.instance().getLanguageString("update") + "?", HOVerwaltung
 							.instance().getLanguageString("update") + "?", JOptionPane.YES_NO_OPTION);
 
 			if (update == JOptionPane.YES_OPTION) {
 				// updateHO(version.getVersion());
 				updateHO(MyConnector.getFinalSite() +"/" + version.getZipFileName());
 			}
 		} else {
 			final int currRev = HOMainFrame.getRevisionNumber();
 			JOptionPane.showMessageDialog(HOMainFrame.instance(),
 					"No update available\n\nYour HO! version is: " + HOMainFrame.VERSION
 							+ (currRev > 1 ? " (r" + currRev + ")" : ""), HOVerwaltung.instance()
 							.getLanguageString("update"), JOptionPane.INFORMATION_MESSAGE);
 
 		}
 	}
 
 	public static void updateHO(double version) {
 		String ver = "" + version;
 		ver = ver.replaceAll("\\.", "");
 		updateHO(MyConnector.getFinalSite() +"/HO_" + ver + ".zip");
 	}
 
 	public static void updateHO(final String urlString) {
 		File tmp = new File("update.zip");
 		LoginWaitDialog wait = new LoginWaitDialog(HOMiniModel.instance().getGUI().getOwner4Dialog());
 		wait.setVisible(true);
 		HOLogger.instance().debug(UpdateController.class, "Try to download: " + urlString);
 		if (!UpdateHelper.instance().download(urlString, tmp)) {
 			wait.setVisible(false);
 			return;
 		}
 		wait.setVisible(false);
 		ZipFile zipFile = null;
 		try {
 			zipFile = new ZipFile("update.zip");
 			String dir = System.getProperty("user.dir");
 			ZipHelper.extractFile(zipFile, "HO.bat", dir);
 			ZipHelper.extractFile(zipFile, "HO.sh", dir);
 			ZipHelper.extractFile(zipFile, "HOLauncher.class", dir);
 		} catch (Exception e) {
 			HOLogger.instance().log(UpdateController.class, e);
 			return;
 		} finally {
 			ZipHelper.close(zipFile);
 		}
 		JOptionPane.showMessageDialog(null,
 				HOVerwaltung.instance().getLanguageString("NeustartErforderlich"), "",
 				JOptionPane.INFORMATION_MESSAGE);
 
 		HOMainFrame.instance().beenden();
 	}
 
 	/**
 	 * Check the external site for the latest beta version. TODO: i18n
 	 */
 	public static void check4latestbeta() {
 		final VersionInfo vi = MyConnector.instance().getLatestBetaVersion();
 		final int currRev = HOMainFrame.getRevisionNumber();
 		if (vi != null && vi.isValid()
 				&& (vi.getVersion() > HOMainFrame.VERSION || (vi.getVersion() == HOMainFrame.VERSION && currRev > 1 && currRev < vi.getBuild()))) {
 			int update = JOptionPane.showConfirmDialog(HOMainFrame.instance(), "Update your HO to this "
 					+ (vi.isBeta() ? "beta " : "") + "version:" + "\n\nVersion: " + vi.getVersionString()
 					+ "\nReleased: " + vi.getReleaseDate() + "\n\n"
 					+ HOVerwaltung.instance().getLanguageString("update") + "?", HOVerwaltung.instance()
 					.getLanguageString("update") + "?", JOptionPane.YES_NO_OPTION);
 
 			if (update == JOptionPane.YES_OPTION) {
 				updateHO(MyConnector.getBetaSite() + "/" + vi.getZipFileName());
 			}
 		} else {
 			JOptionPane.showMessageDialog(HOMainFrame.instance(),
 					"No update available\n\nYour HO! version is: " + HOMainFrame.VERSION
 							+ (currRev > 1 ? " (r" + currRev + ")" : ""), HOVerwaltung.instance()
 							.getLanguageString("update"), JOptionPane.INFORMATION_MESSAGE);
 		}
 	}
 
 	public static void check4EPVUpdate() {
 		Extension data = MyConnector.instance().getEpvVersion();
 		if (HOMainFrame.VERSION >= data.getMinimumHOVersion()
 				&& data.getRelease() > HOParameter.instance().EpvRelease) {
 			// Info anzeigen, dass es ein Update gibt
 			// Show update info
 			int update = JOptionPane.showConfirmDialog(HOMainFrame.instance(), HOVerwaltung.instance()
 					.getLanguageString("updateFile"), HOVerwaltung.instance().getLanguageString("update")
 					+ "?", JOptionPane.YES_NO_OPTION);
 
 			if (update == JOptionPane.YES_OPTION) {
 				updateEPV(data.getRelease());
 			}
 		} else
 			JOptionPane.showMessageDialog(null, HOVerwaltung.instance().getLanguageString("LatestVersion")
 					+ "(" + HOParameter.instance().EpvRelease + ")", HOVerwaltung.instance()
 					.getLanguageString("EPV"), JOptionPane.INFORMATION_MESSAGE);
 
 	}
 
 	public static void updateEPV(float release) {
 		File tmp = new File("tmp.dat");
 		LoginWaitDialog wait = new LoginWaitDialog(HOMiniModel.instance().getGUI().getOwner4Dialog());
 		wait.setVisible(true);
 		if (!UpdateHelper.instance().download(MyConnector.getResourceSite() + "/downloads/epvWeights.mlp",
 				tmp)) {
 			wait.setVisible(false);
 			tmp.delete();
 			return;
 		}
 		File target = new File("prediction/epvWeights.mlp");
 		target.delete();
 		tmp.renameTo(target);
 		HOParameter.instance().EpvRelease = release;
 		wait.setVisible(false);
 		JOptionPane.showMessageDialog(null,
 				HOVerwaltung.instance().getLanguageString("NeustartErforderlich"), "",
 				JOptionPane.INFORMATION_MESSAGE);
 
 	}
 
 	public static void check4RatingsUpdate() {
 		Extension data = MyConnector.instance().getRatingsVersion();
 		HOLogger.instance().log(
 				UpdateController.class,
 				"Check: " + HOMainFrame.VERSION + ">=" + (data != null ? data.getMinimumHOVersion() : -1f)
 						+ " && " + (data != null ? data.getRelease() : -1f) + " > "
 						+ HOParameter.instance().RatingsRelease);
 		if (data != null && HOMainFrame.VERSION >= data.getMinimumHOVersion()
 				&& data.getRelease() > HOParameter.instance().RatingsRelease) {
 			// Infro anzeigen das es ein Update gibt
 			int update = JOptionPane.showConfirmDialog(HOMainFrame.instance(), HOVerwaltung.instance()
 					.getLanguageString("updateFile"), HOVerwaltung.instance().getLanguageString("Ratings")
 					+ "?", JOptionPane.YES_NO_OPTION);
 
 			if (update == JOptionPane.YES_OPTION) {
 				updateRatings(data.getRelease());
 			}
 		} else
 			JOptionPane.showMessageDialog(null, HOVerwaltung.instance().getLanguageString("LatestVersion")
 					+ "(" + HOParameter.instance().RatingsRelease + ")", HOVerwaltung.instance()
 					.getLanguageString("Ratings"), JOptionPane.INFORMATION_MESSAGE);
 	}
 
 	public static void updateRatings(float release) {
 		File tmp = new File("tmp.dat");
 		LoginWaitDialog wait = new LoginWaitDialog(HOMiniModel.instance().getGUI().getOwner4Dialog());
 		wait.setVisible(true);
 		if (!UpdateHelper.instance().download(MyConnector.getResourceSite() + "/downloads/prediction.zip",
 				tmp)) {
 			wait.setVisible(false);
 			tmp.delete();
 			return;
 		}
 		try {
 			File targetDir = new File((System.getProperty("user.dir") + File.separator + "prediction"));
 			HOLogger.instance().log(
 					UpdateController.class,
 					"Unzip " + tmp + " to: " + targetDir.getAbsolutePath());
 			ZipHelper.unzip(tmp, targetDir);
 			tmp.delete();
 			HOParameter.instance().RatingsRelease = release;
 		} catch (Exception e) {
 			HOLogger.instance().log(UpdateController.class, "Rating update unzip: " + e);
 		}
 		wait.setVisible(false);
 		JOptionPane.showMessageDialog(null,
 				HOVerwaltung.instance().getLanguageString("NeustartErforderlich"), "",
 				JOptionPane.INFORMATION_MESSAGE);
 
 	}
 
 	public static void checkNews() {
 		News news = MyConnector.instance().getLatestNews();
 		if (news.getId() > HOParameter.instance().lastNews) {
 			if (HOMainFrame.VERSION >= news.getMinimumHOVersion()) {
 				HOParameter.instance().lastNews = news.getId();
 				switch (news.getType()) {
 				case News.HO: {
 					if (!UserParameter.instance().updateCheck && news.getVersion() > HOMainFrame.VERSION) {
 						int update = JOptionPane.showConfirmDialog(HOMainFrame.instance(), HOVerwaltung
 								.instance().getLanguageString("updateMSG"), HOVerwaltung.instance()
 								.getLanguageString("update") + "?", JOptionPane.YES_NO_OPTION);
 						if (update == JOptionPane.YES_OPTION) {
 							UpdateController.updateHO(news.getVersion());
 						}
 					}
 					break;
 				}
 				case News.EPV: {
 					if (news.getVersion() > HOParameter.instance().EpvRelease) {
 						int update = JOptionPane.showConfirmDialog(HOMainFrame.instance(), news.getMessages()
 								.get(0), HOVerwaltung.instance().getLanguageString("update") + "?",
 								JOptionPane.YES_NO_OPTION);
 						if (update == JOptionPane.YES_OPTION) {
 							UpdateController.updateEPV(news.getVersion());
 						}
 					}
 					break;
 				}
 
 				case News.RATINGS: {
 					if (news.getVersion() > HOParameter.instance().RatingsRelease) {
 
 						int update = JOptionPane.showConfirmDialog(HOMainFrame.instance(), news.getMessages()
 								.get(0), HOVerwaltung.instance().getLanguageString("update") + "?",
 								JOptionPane.YES_NO_OPTION);
 						if (update == JOptionPane.YES_OPTION) {
 
 							UpdateController.updateRatings(news.getVersion());
 						}
 					}
 					break;
 				}
 
 				case News.PLUGIN: {
 					JOptionPane.showMessageDialog(HOMainFrame.instance().getOwner(), new NewsPanel(news),
 							"Plugin News", JOptionPane.INFORMATION_MESSAGE);
 					break;
 				}
 				case News.MESSAGE: {
 					JOptionPane.showMessageDialog(HOMainFrame.instance().getOwner(), new NewsPanel(news),
 							"HO News", JOptionPane.INFORMATION_MESSAGE);
 					break;
 				}
 				default: {
 					// Unsupported Message Type
 				}
 				}
 
 			}
 		}
 	}
 
 }
