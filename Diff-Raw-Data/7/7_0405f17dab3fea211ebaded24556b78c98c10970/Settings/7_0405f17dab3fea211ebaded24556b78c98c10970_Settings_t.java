 package net.nexustools.jbitminingstats.util;
 
 import net.nexustools.jbitminingstats.R;
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.content.res.Resources;
 import android.preference.PreferenceManager;
 
 public class Settings {
 	private static Context context;
 	private static SharedPreferences prefs;
 	public static String[] currencyType;
 	public static String[] currencySymbol;
 	public static boolean[] currencyIsPrefix;
 	
 	private boolean showingBlocks;
 	
 	private boolean autoConnect;
 	private int connectionDelay;
 	private boolean showHashrateUnit;
 	private boolean showParseMessage;
 	private boolean canCheckConnectionDelays;
 	
 	private boolean shouldUseBackupHttpUserAgent;
 	private String httpUserAgent;
 	
 	private String slushsAccountDomain;
 	private String slushsBlockDomain;
 	private String slushsAPIKey;
 	
 	private boolean mtGoxFetchEnabled;
 	private int mtGoxFetchDelay;
 	private String mtGoxAPIDomain;
 	private String mtGoxCurrencyType;
 	private boolean mtGoxEffectBlockTable;
 	private boolean mtGoxBTCTOCurrencySymbolPrefix;
 	private String mtGoxBTCToCurrencySymbol;
 	
 	public Settings(Context theContext) {
 		context = theContext;
 		prefs = PreferenceManager.getDefaultSharedPreferences(context);
 		Resources r = context.getResources();
 		
 		currencyType = context.getResources().getStringArray(R.array.supported_currencies_convertable_from_btc);
 		currencySymbol = r.getStringArray(R.array.currency_type_to_symbol);
 		String[] tempArray = r.getStringArray(R.array.currency_type_is_prefix);
 		currencyIsPrefix = new boolean[tempArray.length];
 		for(int i = 0; i < tempArray.length; i++)
 			currencyIsPrefix[i] = Boolean.parseBoolean(tempArray[i]);
 		tempArray = null;
 	}
 	
 	public void load() {
 		showingBlocks = prefs.getBoolean("showing_blocks", true);
 		autoConnect = prefs.getBoolean("settings_auto_connect", true);
 		connectionDelay = Integer.parseInt(prefs.getString("settings_connect_delay", context.getString(R.string.default_option_connection_delay)));
 		showHashrateUnit = prefs.getBoolean("settings_show_hashrates", true);
 		showParseMessage = prefs.getBoolean("settings_show_messages_when_parsed", false);
 		canCheckConnectionDelays = prefs.getBoolean("settings_check_connection_delays", true);
 		
 		shouldUseBackupHttpUserAgent = prefs.getBoolean("settings_force_use_backup_user_agent", false);
 		String userAgent = prefs.getString("settings_backup_user_agent", "");
 		userAgent = userAgent.equals("") ? null : userAgent;
 		httpUserAgent = shouldUseBackupHttpUserAgent ? userAgent : System.getProperty("http.agent", userAgent);
 		
 		slushsAccountDomain = prefs.getString("settings_slushs_account_api_domain", context.getString(R.string.default_option_slushs_miner_domain));
 		slushsBlockDomain = prefs.getString("settings_slushs_block_api_domain", context.getString(R.string.default_option_slushs_miner_domain));
 		slushsAPIKey = prefs.getString("settings_slushs_api_key", "");
 		
 		mtGoxFetchEnabled = prefs.getBoolean("settings_mtgox_enabled", false);
 		mtGoxFetchDelay = Integer.parseInt(prefs.getString("settings_mtgox_fetch_rate", context.getString(R.string.default_option_connection_delay)));
 		mtGoxAPIDomain = prefs.getString("settings_mtgox_api_domain", context.getString(R.string.default_option_mtgox_api_domain));
 		mtGoxCurrencyType = prefs.getString("settings_mtgox_currency_type", "USD");
 		mtGoxEffectBlockTable = prefs.getBoolean("settings_mtgox_effect_block_table", true);
 		
 		mtGoxBTCTOCurrencySymbolPrefix = true; // TODO: Find out what currencies prefix, and what ones affix.
 		mtGoxBTCToCurrencySymbol = "$";
 		for(int i = 0; i < currencyType.length; i++)
 			if(currencyType[i].equals(mtGoxCurrencyType)) {
 				mtGoxBTCToCurrencySymbol = currencySymbol[i];
 				mtGoxBTCTOCurrencySymbolPrefix = currencyIsPrefix[i];
 				break;
 			}
 	}
 	
 	public boolean isShowingBlocks() {
 		return showingBlocks;
 	}
 	
 	public boolean canAutoConnect() {
 		return autoConnect;
 	}
 	
 	public int getConnectionDelay() {
 		return connectionDelay;
 	}
 	
 	public void setConnectionDelay(int connectionDelay) {
 		this.connectionDelay = connectionDelay;
		prefs.edit().putString("settings_connect_delay", Integer.toString(connectionDelay)).commit();
 	}
 	
 	public void setShowingBlocks(boolean showingBlocks) {
 		this.showingBlocks = showingBlocks;
 		prefs.edit().putBoolean("showing_blocks", showingBlocks).commit();
 	}
 	
 	public boolean canShowHashrateUnit() {
 		return showHashrateUnit;
 	}
 	
 	public boolean canShowParseMessage() {
 		return showParseMessage;
 	}
 	
 	public boolean canCheckConnectionDelays() {
 		return canCheckConnectionDelays;
 	}
 	
 	public void setCheckConnectionDelays(boolean canCheckConnectionDelays) {
 		this.canCheckConnectionDelays = canCheckConnectionDelays;
 		prefs.edit().putBoolean("settings_check_connection_delays", canCheckConnectionDelays).commit();
 	}
 	
 	public String getSlushsAccountDomain() {
 		return slushsAccountDomain;
 	}
 	
 	public String getSlushsBlockDomain() {
 		return slushsBlockDomain;
 	}
 	
 	public String getSlushsAPIKey() {
 		return slushsAPIKey;
 	}
 	
 	public boolean shouldUseBackupHttpUserAgent() {
 		return shouldUseBackupHttpUserAgent;
 	}
 	
 	public String getHTTPUserAgent() {
 		return httpUserAgent;
 	}
 	
 	public boolean isMtGoxFetchEnabled() {
 		return mtGoxFetchEnabled;
 	}
 	
 	public int getMtGoxFetchDelay() {
 		return mtGoxFetchDelay;
 	}
 	
 	public void setMtGoxFetchDelay(int mtGoxFetchDelay) {
		prefs.edit().putString("settings_mtgox_fetch_rate", Integer.toString(mtGoxFetchDelay)).commit();
 	}
 	
 	public String getMtGoxAPIDomain() {
 		return mtGoxAPIDomain;
 	}
 	
 	public String getMtGoxCurrencyType() {
 		return mtGoxCurrencyType;
 	}
 	
 	public boolean canMtGoxEffectBlockTable() {
 		return mtGoxEffectBlockTable;
 	}
 	
 	public boolean canTheMtGoxBTCTOCurrencySymbolPrefixOrAffix() {
 		return mtGoxBTCTOCurrencySymbolPrefix;
 	}
 	
 	public String getTheMtGoxBTCToCurrencySymbol() {
 		return mtGoxBTCToCurrencySymbol;
 	}
 }
