 package syam.StopKorean;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.logging.Logger;
 
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class ConfigurationManager {
 	public final static Logger log = StopKorean.log;
 	private static final String logPrefix = StopKorean.logPrefix;
 	private static final String msgPrefix = StopKorean.msgPrefix;
 
 	private JavaPlugin plugin;
 	private FileConfiguration conf;
 
 	private static File pluginDir = new File("plugins", "StopKorean");
 
 	// 設定項目
 	/* Basic Configs */
 	public boolean warnPlayer = new Boolean(false);
 	public String warnMessage = new String("Please use Japanese or English.");
 
 	public boolean kickPlayer = new Boolean(true);
 	public String kickMessage = new String("Please use Japanese or English.");
 
 	public boolean logToConsole = new Boolean(true);
 
 	public boolean cancelEvent = new Boolean(true);
 
 	/* Herochat Configs */
 	public boolean useHerochat = new Boolean(false);
 	public List<String> hcChannels = new ArrayList<String>();
 	// 設定ここまで
 
 	/**
 	 * コンストラクタ
 	 * @param plugin JavaPlugin
 	 */
 	public ConfigurationManager(final JavaPlugin plugin){
 		this.plugin = plugin;
 		pluginDir = this.plugin.getDataFolder();
 	}
 
 	/**
 	 * 必要なディレクトリ群を作成する
 	 */
 	private void createDirs(){
 		createDir(plugin.getDataFolder());
 	}
 
 	/**
 	 * 設定ファイルを読み込む
 	 */
 	public void loadConfig(){
 		// ディレクトリ作成
 		createDirs();
 
 		// 設定ファイルパス取得
 		String filepath = pluginDir + System.getProperty("file.separator") + "config.yml";
 		File file = new File(filepath);
 
 		// 設定ファイルが見つからなければデフォルトのファイルをコピー
 		if (!file.exists()){
 			plugin.saveDefaultConfig();
 			log.info(logPrefix+ "config.yml is not found! Created default config.yml!");
 		}
 
 		plugin.reloadConfig();
 
 		// 項目取得
 
 		/* Basic Configs */
		kickPlayer = plugin.getConfig().getBoolean("WarnPlayer", false);
		kickMessage = plugin.getConfig().getString("WarnMessage", "Please use Japanese or English.");
 
 		kickPlayer = plugin.getConfig().getBoolean("KickPlayer", true);
 		kickMessage = plugin.getConfig().getString("KickMessage", "Please use Japanese or English.");
 
 		logToConsole = plugin.getConfig().getBoolean("LogToConsole", true);
 
 		cancelEvent = plugin.getConfig().getBoolean("CancelEvent", true);
 
 		/* Herochat Configs */
 		useHerochat = plugin.getConfig().getBoolean("UseHerochat", false);
 		hcChannels = plugin.getConfig().getStringList("HerochatChannnels");
 	}
 
 	/**
 	 * 設定ファイルに設定を書き込む
 	 * @throws Exception
 	 */
 	public void save() throws Exception{
 		plugin.saveConfig();
 	}
 
 	/**
 	 * 存在しないディレクトリを作成する
 	 * @param dir File 作成するディレクトリ
 	 */
 	private static void createDir(File dir){
 		// 既に存在すれば作らない
 		if (dir.isDirectory()){
 			return;
 		}
 		if (!dir.mkdir()){
 			log.warning(logPrefix+ "Can't create directory: " + dir.getName());
 		}
 	}
 }
