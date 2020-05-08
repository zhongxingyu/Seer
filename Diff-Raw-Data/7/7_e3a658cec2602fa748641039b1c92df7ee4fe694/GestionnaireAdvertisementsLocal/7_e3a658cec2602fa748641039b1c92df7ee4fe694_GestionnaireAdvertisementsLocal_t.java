 package icms_plugin.advertisement;
 
 import java.util.List;
 import javax.ejb.Local;
 
 @Local
 public interface GestionnaireAdvertisementsLocal {
 
     // CRUD
     public void create(String title, String link, String content, String service, String criteria,
                        String criteria_value);
 
     public boolean update(int id, String title, String link, String content, String service,
                           String criteria,
                           String criteria_value);
 
     public void destroy(int id);
 
     // Find one
     public Advertisement find(int id);
 
     public Advertisement findByService(String service);
 
     // Find many
     public List<Advertisement> allAdvertisements();
 }
