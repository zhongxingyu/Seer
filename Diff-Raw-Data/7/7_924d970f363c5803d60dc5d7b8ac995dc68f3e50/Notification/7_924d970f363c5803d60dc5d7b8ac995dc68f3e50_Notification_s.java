 package com.censoredsoftware.demigods.player;
 
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Set;
 import java.util.UUID;
 import java.util.concurrent.ConcurrentHashMap;
 
 import org.bukkit.configuration.ConfigurationSection;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.configuration.serialization.ConfigurationSerializable;
 import org.bukkit.entity.Player;
 
 import com.censoredsoftware.core.bukkit.ConfigFile;
 import com.censoredsoftware.demigods.Demigods;
 import com.censoredsoftware.demigods.data.DataManager;
 import com.censoredsoftware.demigods.language.Translation;
 import com.google.common.collect.Sets;
 
 public class Notification implements ConfigurationSerializable
 {
 	private UUID id;
 	private long expiration;
 	private UUID receiver;
 	private UUID sender;
 	private String senderType;
 	private String danger;
 	private String name;
 	private String message;
 
 	public Notification()
 	{}
 
 	public Notification(UUID id, ConfigurationSection conf)
 	{
 		this.id = id;
 		expiration = conf.getLong("expiration");
 		receiver = UUID.fromString(conf.getString("receiver"));
 		sender = UUID.fromString(conf.getString("sender"));
 		senderType = conf.getString("senderType");
 		danger = conf.getString("danger");
 		name = conf.getString("name");
 		message = conf.getString("message");
 	}
 
 	@Override
 	public Map<String, Object> serialize()
 	{
 		return new HashMap<String, Object>()
 		{
 			{
 				put("expiration", expiration);
 				put("receiver", receiver.toString());
 				put("sender", sender.toString());
 				put("senderType", senderType);
 				put("danger", danger);
 				put("name", name);
 				put("message", message);
 			}
 		};
 	}
 
 	public void generateId()
 	{
 		id = UUID.randomUUID();
 	}
 
 	void setExpiration(int minutes)
 	{
 		this.expiration = System.currentTimeMillis() + (minutes * 60000);
 	}
 
 	void setDanger(Danger danger)
 	{
 		this.danger = danger.name();
 	}
 
 	void setSenderType(Sender senderType)
 	{
 		this.senderType = senderType.name();
 	}
 
 	void setSender(DCharacter sender)
 	{
 		this.sender = sender.getId();
 	}
 
 	void setReceiver(DCharacter receiver)
 	{
 		this.receiver = receiver.getId();
 	}
 
 	void setName(String name)
 	{
 		this.name = name;
 	}
 
 	void setMessage(String message)
 	{
 		this.message = message;
 	}
 
 	public long getExpiration()
 	{
 		return this.expiration;
 	}
 
 	public Sender getSenderType()
 	{
 		return Sender.valueOf(this.senderType);
 	}
 
 	public Danger getDanger()
 	{
 		return Danger.valueOf(this.danger);
 	}
 
 	public DCharacter getReceiver()
 	{
 		return DCharacter.Util.load(this.receiver);
 	}
 
 	public DCharacter getSender()
 	{
 		return DCharacter.Util.load(this.sender);
 	}
 
 	public String getName()
 	{
 		return this.name;
 	}
 
 	public String getMessage()
 	{
 		return this.message;
 	}
 
 	public UUID getId()
 	{
 		return this.id;
 	}
 
 	public boolean hasExpiration()
 	{
 		return this.expiration != 0L;
 	}
 
 	public static void remove(Notification notification)
 	{
 		DataManager.notifications.remove(notification.getId());
 	}
 
 	public static Set<Notification> loadAll()
 	{
 		return Sets.newHashSet(DataManager.notifications.values());
 	}
 
 	public static class File extends ConfigFile
 	{
 		private static String SAVE_PATH;
 		private static final String SAVE_FILE = "notifications.yml";
 
 		public File()
 		{
 			super(Demigods.plugin);
 			SAVE_PATH = Demigods.plugin.getDataFolder() + "/data/";
 		}
 
 		@Override
 		public ConcurrentHashMap<UUID, Notification> loadFromFile()
 		{
 			final FileConfiguration data = getData(SAVE_PATH, SAVE_FILE);
 			return new ConcurrentHashMap<UUID, Notification>()
 			{
 				{
 					for(String stringId : data.getKeys(false))
 						put(UUID.fromString(stringId), new Notification(UUID.fromString(stringId), data.getConfigurationSection(stringId)));
 				}
 			};
 		}
 
 		@Override
 		public boolean saveToFile()
 		{
 			FileConfiguration saveFile = getData(SAVE_PATH, SAVE_FILE);
 			Map<UUID, Notification> currentFile = loadFromFile();
 
			for(UUID id : DataManager.devotion.keySet())
				if(!currentFile.keySet().contains(id) || !currentFile.get(id).equals(DataManager.devotion.get(id))) saveFile.createSection(id.toString(), Util.load(id).serialize());
 
 			for(UUID id : currentFile.keySet())
				if(!DataManager.devotion.keySet().contains(id)) saveFile.set(id.toString(), null);
 
 			return saveFile(SAVE_PATH, SAVE_FILE, saveFile);
 		}
 	}
 
 	public static class Util
 	{
 		public static void save(Notification notification)
 		{
 			DataManager.notifications.put(notification.getId(), notification);
 		}
 
 		public static Notification load(UUID id)
 		{
 			return DataManager.notifications.get(id);
 		}
 
 		public static Notification create(Sender sender, DCharacter receiver, Danger danger, String name, String message)
 		{
 			Notification notification = new Notification();
 			notification.generateId();
 			notification.setReceiver(receiver);
 			notification.setDanger(danger);
 			notification.setSenderType(sender);
 			notification.setName(name);
 			notification.setMessage(message);
 			save(notification);
 			return notification;
 		}
 
 		public static Notification create(Sender sender, DCharacter receiver, Danger danger, int minutes, String name, String message)
 		{
 			Notification notification = create(sender, receiver, danger, name, message);
 			notification.generateId();
 			notification.setExpiration(minutes);
 			save(notification);
 			return notification;
 		}
 
 		public static Notification create(DCharacter sender, DCharacter receiver, Danger danger, String name, String message)
 		{
 			Notification notification = create(Sender.CHARACTER, receiver, danger, name, message);
 			notification.generateId();
 			notification.setSender(sender);
 			save(notification);
 			return notification;
 		}
 
 		public static Notification create(DCharacter sender, DCharacter receiver, Danger danger, int minutes, String name, String message)
 		{
 			Notification notification = create(sender, receiver, danger, name, message);
 			notification.generateId();
 			notification.setExpiration(minutes);
 			save(notification);
 			return notification;
 		}
 
 		public static void updateNotifications()
 		{
 			for(Notification notification : loadAll())
 			{
 				if(notification.getExpiration() <= System.currentTimeMillis())
 				{
 					Notification.remove(notification);
 					notification.getReceiver().getMeta().removeNotification(notification);
 				}
 			}
 		}
 
 		public static void sendNotification(DCharacter character, Notification notification)
 		{
 			// Add the notification
 			character.getMeta().addNotification(notification);
 
 			// Message them if possible
 			if(character.getOfflinePlayer().isOnline())
 			{
 				Player player = character.getOfflinePlayer().getPlayer();
 				for(String message : Demigods.language.getTextBlock(Translation.Text.NOTIFICATION_RECEIVED))
 				{
 					player.sendMessage(message);
 				}
 			}
 		}
 	}
 
 	public enum Sender
 	{
 		PLUGIN, QUEST, CHARACTER
 	}
 
 	public enum Danger
 	{
 		GOOD, NEUTRAL, BAD
 	}
 }
