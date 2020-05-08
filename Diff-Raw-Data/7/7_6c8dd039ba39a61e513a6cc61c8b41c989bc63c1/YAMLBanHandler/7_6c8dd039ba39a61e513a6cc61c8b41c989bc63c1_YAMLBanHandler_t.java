 package com.cole2sworld.ColeBans.handlers;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.Map;
 import java.util.Set;
 import java.util.Vector;
 
 import org.bukkit.configuration.ConfigurationSection;
 import org.bukkit.configuration.InvalidConfigurationException;
 import org.bukkit.configuration.file.YamlConfiguration;
 
 import com.cole2sworld.ColeBans.GlobalConf;
 import com.cole2sworld.ColeBans.Main;
 import com.cole2sworld.ColeBans.framework.PlayerAlreadyBannedException;
 import com.cole2sworld.ColeBans.framework.PlayerNotBannedException;
 
 public class YAMLBanHandler extends BanHandler {
 	private File file;
 	private YamlConfiguration conf;
 	public static BanHandler onEnable() {
 		Map<String, String> data = Main.getBanHandlerInitArgs();
 		File tFile = new File("./plugins/ColeBans/"+data.get("yaml"));
 		try {
 			tFile.createNewFile();
 		} catch (IOException e1) {
 			e1.printStackTrace();
 			Main.LOG.severe(GlobalConf.logPrefix+"[YAMLBanHandler] IOException when creating file. Plugin will fail to operate correctly.");
 			return null;
 		}
 		YamlConfiguration tConf = new YamlConfiguration();
 		try {
 			tConf.load(tFile);
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 			Main.LOG.severe(GlobalConf.logPrefix+"[YAMLBanHandler] FileNotFoundException when loading. Plugin will fail to operate correctly.");
 			return null;
 		} catch (IOException e) {
 			e.printStackTrace();
 			Main.LOG.severe(GlobalConf.logPrefix+"[YAMLBanHandler] IOException when loading. Plugin will fail to operate correctly.");
 			return null;
 		} catch (InvalidConfigurationException e) {
 			e.printStackTrace();
 			Main.LOG.severe(GlobalConf.logPrefix+"[YAMLBanHandler] InvalidConfigurationException when loading. Plugin will fail to operate correctly.");
 			return null;
 		}
 		
 		return new YAMLBanHandler(tFile, tConf);
 	}
 
 	public YAMLBanHandler(File file, YamlConfiguration conf) {
 		this.file = file;
 		this.conf = conf;
 		System.out.println(GlobalConf.logPrefix+"[YAMLBanHandler] Done loading.");
 	}
 
 	@Override
 	public void banPlayer(String player, String reason, String admin) throws PlayerAlreadyBannedException {
 		if (isPlayerBanned(player, admin)) throw new PlayerAlreadyBannedException(player+" is already banned!");
 		conf.set("permBans."+player, reason);
 		save();
 	}
 
 	@Override
 	public void tempBanPlayer(String player, long primTime, String admin) throws PlayerAlreadyBannedException, UnsupportedOperationException {
 		if (!GlobalConf.allowTempBans) throw new UnsupportedOperationException("Temp bans are disabled!");
 		if (isPlayerBanned(player, admin)) throw new PlayerAlreadyBannedException(player+" is already banned!");
 		long time = System.currentTimeMillis()+((primTime*60)*1000);
 		conf.set("tempBans."+player, time);
 		save();
 	}
 
 	@Override
 	public void unbanPlayer(String player, String admin) throws PlayerNotBannedException {
 		if (!isPlayerBanned(player, admin)) throw new PlayerNotBannedException(player+" is not banned!");
 		conf.set("permBans."+player, null);
 		conf.set("tempBans."+player, null); //lazy removal, don't check what is actually there
 		save();
 	}
 
 	@Override
 	public boolean isPlayerBanned(String player, String admin) {
 		return getBanData(player, admin).getType() != Type.NOT_BANNED;
 	}
 
 	@Override
 	public BanData getBanData(String player, String admin) {
 		String permBanned = conf.getString("permBans."+player);
 		Long tempBanned = conf.getLong("tempBans."+player);
 		if (permBanned != null) {
 			return new BanData(player, permBanned);
 		}
 		if (GlobalConf.allowTempBans) {
 			if (tempBanned <= System.currentTimeMillis()) {
				conf.set("permBans."+player, null);
				conf.set("tempBans."+player, null); //lazy removal, don't check what is actually there
				save();
 			}
 			if (tempBanned != null) {
 				return new BanData(player, tempBanned);
 			}
 		}
 		return new BanData(player);
 	}
 
 	@Override
 	public void onDisable() {
 		save();
 		System.out.println(GlobalConf.logPrefix+"[YAMLBanHandler] Saved.");
 	}
 
 	@Override
 	public void convert(BanHandler handler) {
 		Vector<BanData> dump = dump(BanHandler.SYSTEM_ADMIN_NAME);
 		for (BanData data : dump) {
 			if (GlobalConf.allowTempBans && data.getType() == Type.TEMPORARY) {
 				try {
 					handler.tempBanPlayer(data.getVictim(), data.getTime(), BanHandler.SYSTEM_ADMIN_NAME);
 				} catch (UnsupportedOperationException e) {} catch (PlayerAlreadyBannedException e) {} //just skip it
 			} else if (data.getType() == Type.PERMANENT) {
 				try {
 					handler.banPlayer(data.getVictim(), data.getReason(), BanHandler.SYSTEM_ADMIN_NAME);
 				} catch (PlayerAlreadyBannedException e) {} //just skip it
 			}
 		}
 	}
 
 	@Override
 	public Vector<BanData> dump(String admin) {
 		Vector<BanData> data = new Vector<BanData>(50);
 		ConfigurationSection temp = conf.getConfigurationSection("tempBans");
 		ConfigurationSection perm = conf.getConfigurationSection("permBans");
 		if (temp != null) {
 			Map<String, Object> tempBans = temp.getValues(true);
 			Set<String> keySet = tempBans.keySet();
 			for (String key : keySet) {
 				Object time = tempBans.get(key);
 				if (time instanceof Long) data.add(new BanData(key, (Long) time));
 			}
 		}
 		if (perm != null) {
 			Map<String, Object> permBans = perm.getValues(true);
 			Set<String> keySet = permBans.keySet();
 			for (String key : keySet) {
 				Object reason = permBans.get(key);
 				if (reason instanceof String) data.add(new BanData(key, (String) reason));
 			}
 		}
 		return data;
 	}
 
 	@Override
 	public Vector<String> listBannedPlayers(String admin) {
 		Vector<String> list = new Vector<String>(50);
 		Vector<BanData> verbData = dump(admin);
 		for (BanData data : verbData) {
 			list.add(data.getVictim());
 		}
 		return list;
 	}
 	
 	private void save() {
 		try {
 			conf.save(file);
 		} catch (IOException e) {
 			e.printStackTrace();
 			Main.LOG.severe(GlobalConf.logPrefix+"[YAMLBanHandler] IOException when saving config. Plugin will fail to operate correctly.");
 		}
 	}
 
 }
