 package to.joe.strangeweapons.datastorage;
 
 import java.util.LinkedHashMap;
 import java.util.List;
 
 import org.bukkit.configuration.ConfigurationSection;
 
 import to.joe.strangeweapons.Part;
 import to.joe.strangeweapons.Quality;
 
 public class WeaponData implements Cloneable {
 
     private boolean isUpdated = true;
     private int weaponId;
     private Quality quality;
     private String customName = null;
     private String description = null;
     private LinkedHashMap<Part, Integer> parts = null;
 
     public int getWeaponId() {
         return weaponId;
     }
 
     public void setWeaponId(int weaponId) {
         this.weaponId = weaponId;
         isUpdated = false;
     }
 
     public Quality getQuality() {
         return quality;
     }
 
     public void setQuality(Quality quality) {
         this.quality = quality;
         isUpdated = false;
     }
 
     public String getCustomName() {
         return customName;
     }
 
     public void setCustomName(String customName) {
         this.customName = customName;
         isUpdated = false;
     }
 
     public String getDescription() {
         return description;
     }
 
     public void setDescription(String description) {
         this.description = description;
         isUpdated = false;
     }
 
     public LinkedHashMap<Part, Integer> getParts() {
         isUpdated = false;
         return parts;
     }
 
     public void setParts(LinkedHashMap<Part, Integer> parts) {
         this.parts = parts;
         isUpdated = false;
     }
 
     public boolean isUpdated() {
         return isUpdated;
     }
 
     public void setUpdated(boolean updated) {
         isUpdated = updated;
     }
 
     @SuppressWarnings("unchecked")
     @Override
     public WeaponData clone() {
         WeaponData data = new WeaponData();
         data.setCustomName(customName);
         data.setDescription(description);
        if (parts != null) {
            data.setParts((LinkedHashMap<Part, Integer>) parts.clone());
        }
         data.setQuality(quality);
         data.setWeaponId(weaponId);
         return data;
     }
 
     public static WeaponData fromConfigurationSection(int id, ConfigurationSection section) {
         WeaponData data = new WeaponData();
         data.weaponId = id;
         data.quality = Quality.valueOf(section.getString("quality"));
         LinkedHashMap<Part, Integer> parts = new LinkedHashMap<Part, Integer>();
         List<String> rawParts = section.getStringList("parts");
         for (String part : rawParts) {
             String[] split = part.split(",");
             parts.put(Part.valueOf(split[0]), Integer.parseInt(split[1]));
         }
         data.parts = parts;
         if (section.contains("customname")) {
             data.customName = section.getString("customname");
         }
         if (section.contains("description")) {
             data.description = section.getString("description");
         }
         return data;
     }
 
 }
