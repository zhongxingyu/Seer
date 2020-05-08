 package main.java.me.avastprods.chunkprotection;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.logging.Level;
 
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.configuration.file.YamlConfiguration;
 
 public class MessageData {
 
 	private FileConfiguration messageData = null;
 	private File messageDataFile = null;
 
 	ChunkProtection clazz;
 
 	public MessageData(ChunkProtection instance) {
 		clazz = instance;
 	}
 
 	public void load() {
 		if (messageDataFile == null) {
 			messageDataFile = new File(clazz.getDataFolder(), "messages.yml");
 		}
 
 		messageData = YamlConfiguration.loadConfiguration(messageDataFile);
 		setup();
 	}
 
 	public FileConfiguration getData() {
 		if (messageData == null) {
 			load();
 		}
 
 		return messageData;
 	}
 
 	public void saveData() {
 		if (messageData == null || messageDataFile == null) {
 			return;
 		}
 
 		try {
 			getData().save(messageDataFile);
 		} catch (IOException ex) {
 			clazz.getLogger().log(Level.SEVERE, "Could not save config to " + messageDataFile, ex);
 		}
 	}
 	
 	public void setup() {
 		if(!getData().contains("message.command.prefix")) getData().set("message.command.prefix", "&1[&fChunkProtection&1]&f");		
 		if(!getData().contains("message.command.chunk_claimed")) getData().set("message.command.chunk_claimed", "%prefix% Succesfully claimed chunk!");
 		if(!getData().contains("message.command.chunk_unclaimed")) getData().set("message.command.chunk_unclaimed", "%prefix% Succesfully unclaimed chunk!");
 		if(!getData().contains("message.command.chunk_unclaimed_all")) getData().set("message.command.chunk_unclaimed_all", "%prefix% Succesfully unclaimed %amount% chunks of yours.");
 		if(!getData().contains("message.command.chunk_already_claimed")) getData().set("message.command.chunk_already_claimed", "&4Error: &cThis chunk has already been claimed by &4%claimer%");
 		if(!getData().contains("message.command.chunk_not_claimed")) getData().set("message.command.chunk_not_claimed", "&4Error: &cThis chunk is not claimed.");	
 		if(!getData().contains("message.command.chunk_owned_other")) getData().set("message.command.chunk_owned_other", "&4Error: &cThis chunk is owned by &4%claimer%&c");
 		if(!getData().contains("message.command.chunk_owned_sender")) getData().set("message.command.chunk_owned_sender", "&4Error: &cYou already own this chunk!");
 		if(!getData().contains("message.command.trust")) getData().set("message.command.trust", "%prefix% Player &9%trusted%&f can now build in this chunk.");
 		if(!getData().contains("message.command.untrust")) getData().set("message.command.untrust", "%prefix% Player &9%untrusted%&f can no longer build in this chunk.");
 		if(!getData().contains("message.command.target_not_trusted")) getData().set("message.command.target_not_trusted", "&4Error: &cPlayer &4%target%&c is not trusted in this chunk!");
 		if(!getData().contains("message.command.target_already_trusted")) getData().set("message.command.target_already_trusted", "&4Error: &cPlayer &4%target%&c is already trusted in this chunk!");
		if(!getData().contains("message.command.claim_limit_reached")) getData().set("message.command.claim_limit_reached", "&4Error: &cYou have reached the limit of claimed chunks!");
		if(!getData().contains("message.command.no_chunks_owned")) getData().set("message.command.no_chunks_owned", "&4Error: &cYou do not own any chunks.");	
 		if(!getData().contains("message.command.no_permission")) getData().set("message.command.no_permission", "&4Error: &cInsufficient permissions!");
 		if(!getData().contains("message.event.interact_deny")) getData().set("message.event.interact_deny", "&4Error: &cYou do not have permission to interact with objects in &4%claimer%&c's chunk!");		
 		if(!getData().contains("message.event.place_deny")) getData().set("message.event.place_deny", "&4Error: &cYou do not have permission to place blocks in &4%claimer%&c's chunk!");		
 		if(!getData().contains("message.event.break_deny")) getData().set("message.event.break_deny", "&4Error: &cYou do not have permission to break blocks in &4%claimer%&c's chunk!");		
 				
 		saveData();
 	}
 }
