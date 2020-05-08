 package couk.Adamki11s.Regios.Mutable;
 
 import java.util.Map;
 import java.util.Map.Entry;
 
 import org.bukkit.Material;
 import org.bukkit.util.config.Configuration;
 
 import couk.Adamki11s.Regios.Regions.Region;
 
 public class MutableSpout {
 	
 	public boolean checkMusicUrl(String url, Region r){
 		for(String URL : r.getMusicUrls()){
 			if(URL.equalsIgnoreCase(url)){
 				return true;
 			}
 		}
 		return false;
 	}
 	
 	public void editWelcomeMessage(Region r, String message){
 		Configuration c = r.getConfigFile();
 		c.load();
 		Map<String, Object> all = c.getAll();
 		all.remove("Region.Spout.Welcome.Message");
 		for(Entry<String, Object> entry : all.entrySet()){
 			c.setProperty(entry.getKey(), entry.getValue());
 		}
 		c.setProperty("Region.Spout.Welcome.Message", message);
 		r.setSpoutEntryMessage(message);
 		c.save();
 	}
 	
 	public void editWelcomeEnabled(Region r, boolean val){
 		Configuration c = r.getConfigFile();
 		c.load();
 		Map<String, Object> all = c.getAll();
 		all.remove("Region.Spout.Welcome.Enabled");
 		for(Entry<String, Object> entry : all.entrySet()){
 			c.setProperty(entry.getKey(), entry.getValue());
 		}
 		c.setProperty("Region.Spout.Welcome.Enabled", val);
 		r.setSpoutWelcomeEnabled(val);
 		c.save();
 	}
 	
 	public void editLeaveEnabled(Region r, boolean val){
 		Configuration c = r.getConfigFile();
 		c.load();
 		Map<String, Object> all = c.getAll();
 		all.remove("Region.Spout.Leave.Enabled");
 		for(Entry<String, Object> entry : all.entrySet()){
 			c.setProperty(entry.getKey(), entry.getValue());
 		}
 		c.setProperty("Region.Spout.Leave.Enabled", val);
 		r.setSpoutLeaveEnabled(val);
 		c.save();
 	}
 	
 	public void editLeaveMessage(Region r, String message){
 		Configuration c = r.getConfigFile();
 		c.load();
 		Map<String, Object> all = c.getAll();
 		all.remove("Region.Spout.Leave.Message");
 		for(Entry<String, Object> entry : all.entrySet()){
 			c.setProperty(entry.getKey(), entry.getValue());
 		}
 		c.setProperty("Region.Spout.Leave.Message", message);
 		r.setSpoutExitMessage(message);
 		c.save();
 	}
 	
 	public void editWelcomeMaterial(Region r, int mat){
 		Configuration c = r.getConfigFile();
 		c.load();
 		Map<String, Object> all = c.getAll();
 		all.remove("Region.Spout.Welcome.IconID");
 		for(Entry<String, Object> entry : all.entrySet()){
 			c.setProperty(entry.getKey(), entry.getValue());
 		}
 		c.setProperty("Region.Spout.Welcome.IconID", mat);
 		r.setSpoutEntryMaterial(Material.getMaterial(mat));
 		c.save();
 	}
 	
 	public void editLeaveMaterial(Region r, int mat){
 		Configuration c = r.getConfigFile();
 		c.load();
 		Map<String, Object> all = c.getAll();
 		all.remove("Region.Spout.Leave.IconID");
 		for(Entry<String, Object> entry : all.entrySet()){
 			c.setProperty(entry.getKey(), entry.getValue());
 		}
 		c.setProperty("Region.Spout.Leave.IconID", mat);
 		r.setSpoutExitMaterial(Material.getMaterial(mat));
 		c.save();
 	}
 	
 	public void editTexturePackURL(Region r, String message){
 		Configuration c = r.getConfigFile();
 		c.load();
 		Map<String, Object> all = c.getAll();
 		all.remove("Region.Spout.Texture.TexturePackURL");
 		for(Entry<String, Object> entry : all.entrySet()){
 			c.setProperty(entry.getKey(), entry.getValue());
 		}
 		c.setProperty("Region.Spout.Texture.TexturePackURL", message);
 		r.setSpoutExitMessage(message);
 		c.save();
 	}
 	
 	public void editUseTexturePack(Region r, boolean val){
 		Configuration c = r.getConfigFile();
 		c.load();
 		Map<String, Object> all = c.getAll();
 		all.remove("Region.Spout.Texture.UseTexture");
 		for(Entry<String, Object> entry : all.entrySet()){
 			c.setProperty(entry.getKey(), entry.getValue());
 		}
 		c.setProperty("Region.Spout.Texture.Region.Spout.Texture.UseTexture", val);
 		r.setUseSpoutTexturePack(val);
 		c.save();
 	}
 	
 	public void editUseMusic(Region r, boolean val){
 		Configuration c = r.getConfigFile();
 		c.load();
 		Map<String, Object> all = c.getAll();
 		all.remove("Region.Spout.Sound.PlayCustomMusic");
 		for(Entry<String, Object> entry : all.entrySet()){
 			c.setProperty(entry.getKey(), entry.getValue());
 		}
 		c.setProperty("Region.Spout.Sound.PlayCustomMusic", val);
 		r.setUseSpoutTexturePack(val);
 		c.save();
 	}
 	
 	public void editAddToMusicList(Region r, String message){
 		Configuration c = r.getConfigFile();
 		c.load();
 		Map<String, Object> all = c.getAll();
 		String current = (String)all.get("Region.Spout.Sound.CustomMusicURL");
 		all.remove("Region.Spout.Sound.CustomMusicURL");
 		for(Entry<String, Object> entry : all.entrySet()){
 			c.setProperty(entry.getKey(), entry.getValue());
 		}
 		c.setProperty("Region.Spout.Sound.CustomMusicURL", current.trim() + message.trim() + ",");
		r.setPermanentNodesCacheRemove((current.trim() + "," + message.trim()).split(","));
 		c.save();
 	}
 	
 	public void editRemoveFromMusicList(Region r, String message){
 		Configuration c = r.getConfigFile();
 		c.load();
 		Map<String, Object> all = c.getAll();
 		String current = (String)all.get("Region.Spout.Sound.CustomMusicURL");
 		current = current.replaceAll(" ", "");
 		current = current.replaceAll(message + ",", "");
 		current = current.replaceAll(",,", ",");
 		all.remove("Region.Spout.Sound.CustomMusicURL");
 		for(Entry<String, Object> entry : all.entrySet()){
 			c.setProperty(entry.getKey(), entry.getValue());
 		}
 		c.setProperty("Region.Spout.Sound.CustomMusicURL", current.trim());
		r.setPermanentNodesCacheRemove((current.trim()).split(","));
 		c.save();
 	}
 	
 	public void editResetMusicList(Region r){
 		Configuration c = r.getConfigFile();
 		c.load();
 		Map<String, Object> all = c.getAll();
 		all.remove("Region.Spout.Sound.CustomMusicURL");
 		for(Entry<String, Object> entry : all.entrySet()){
 			c.setProperty(entry.getKey(), entry.getValue());
 		}
 		c.setProperty("Region.Spout.Sound.CustomMusicURL", "");
		r.setPermanentNodesCacheRemove(("").split(","));
 		c.save();
 	}
 
 }
