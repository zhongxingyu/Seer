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
     		
    		if ((null != n.getCalories()) && !n.getCalories().isEmpty()) {
    		    BigDecimal c = new BigDecimal(n.getCalories());
    			calories = calories.add(c);
     		}
     		
     		if ((null != n.getProtein()) && !"".equals(n.getProtein())) {
    			protein = protein.add(new BigDecimal(n.getProtein()));
     		}
     		
     		if ((null != n.getFat()) && !"".equals(n.getFat())) {
    			fat =fat.add(new BigDecimal(n.getFat()));
     		}
     		
     		if ((null != n.getCarbohydrate()) && !"".equals(n.getCarbohydrate())) {
    			carbohydrate = carbohydrate.add(new BigDecimal(n.getCarbohydrate()));
     		}
     		
     		if ((null != n.getFibre()) && !"".equals(n.getFibre())) {
    			fibre = fibre.add(new BigDecimal(n.getFibre()));
     		}
     		
     		if ((null != n.getSalt()) && !"".equals(n.getSalt())) {
    			salt = salt.add(new BigDecimal(n.getSalt()));
     		}
     	}
     }
 
     public BigDecimal getCalories() {
        System.out.println("--------- getting calories " + calories.toString());
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
