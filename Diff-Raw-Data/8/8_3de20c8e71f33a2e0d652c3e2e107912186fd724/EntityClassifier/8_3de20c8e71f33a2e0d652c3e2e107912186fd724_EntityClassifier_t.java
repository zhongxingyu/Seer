 package base.classification;
 
import base.util.Detail;
 import base.util.EntityID;
 
 import java.util.HashMap;
 import java.util.Map;
 
 /**
  * @author Mahdi
  */
 public class EntityClassifier {
 
     private Map<EntityID, Category> idCategoryMap = new HashMap<>();
     private Map<EntityID, Detail> idDetailMap = new HashMap<>();
     private Category root;
     private Category car;
     private Category currency;
     private Category metal;
 
     public void register(Category category) {
         if (category != null && category.getId() != null) {
             if (idCategoryMap.containsKey(category.getId())) {
                 System.out.println("Warning:This category id is exist....");
             }
             idCategoryMap.put(category.getId(), category);
 //            String name = category.getName().get(Language.ENGLISH);
 //            if (name.equalsIgnoreCase("car")) {
 //                car = category;
 //            } else if (name.equalsIgnoreCase("currency")) {
 //                currency = category;
 //            } else if (name.equalsIgnoreCase("metal")) {
 //                metal= category;
 //            }
         }
     }
 
 //    public void register(Detail detail) {
 //        if (detail != null && detail.getId() != null) {
 //            if (idDetailMap.containsKey(detail.getId())) {
 //                System.out.println("Warning:This detail id is exist....");
 //            }
 //            idDetailMap.put(detail.getId(), detail);
 //            //String name = detail.getName().getEnglish();
 //        }
 //    }
 
     public Category getCar() {
         return car;
     }
 
     public Category getCurrency() {
         return currency;
     }
 
     public Category getMetal() {
         return metal;
     }
     
     
 
     public Category getCategory(EntityID id) {
         return idCategoryMap.get(id);
     }
 }
