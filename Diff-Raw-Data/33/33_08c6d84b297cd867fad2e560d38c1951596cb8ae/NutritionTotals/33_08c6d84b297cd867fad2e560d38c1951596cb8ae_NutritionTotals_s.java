 package org.xcube.nfc.domain;
 
 import java.math.BigDecimal;
 
 public class NutritionTotals {
 	
     private BigDecimal calories = new BigDecimal(0);
     private BigDecimal protein = new BigDecimal(0);
     private BigDecimal fat = new BigDecimal(0);
     private BigDecimal carbohydrate = new BigDecimal(0);
     private BigDecimal fibre = new BigDecimal(0);
     private BigDecimal salt = new BigDecimal(0);
     
     public void addItem(Item item) {
     	ItemInfo info = item.getInfo();
    	
     	if (null != info.getPer100g()) {
     		NutritionInfo n = info.getPer100g();
     		
    		if ((null != n.getCalories()) && !"".equals(n.getCalories())) {
    			calories.add(new BigDecimal(n.getCalories()));
     		}
     		
     		if ((null != n.getProtein()) && !"".equals(n.getProtein())) {
    			protein.add(new BigDecimal(n.getCalories()));
     		}
     		
     		if ((null != n.getFat()) && !"".equals(n.getFat())) {
    			fat.add(new BigDecimal(n.getCalories()));
     		}
     		
     		if ((null != n.getCarbohydrate()) && !"".equals(n.getCarbohydrate())) {
    			carbohydrate.add(new BigDecimal(n.getCalories()));
     		}
     		
     		if ((null != n.getFibre()) && !"".equals(n.getFibre())) {
    			fibre.add(new BigDecimal(n.getCalories()));
     		}
     		
     		if ((null != n.getSalt()) && !"".equals(n.getSalt())) {
    			salt.add(new BigDecimal(n.getCalories()));
     		}
     	}
     }
 
     public BigDecimal getCalories() {
         return calories;
     }
 
     public BigDecimal getProtein() {
         return protein;
     }
 
     public BigDecimal getFat() {
         return fat;
     }
 
     public BigDecimal getCarbohydrate() {
         return carbohydrate;
     }
 
     public BigDecimal getFibre() {
         return fibre;
     }
 
     public BigDecimal getSalt() {
         return salt;
     }
     
 }
