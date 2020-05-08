 package net.kerflyn.broceliand.util;
 
 import net.kerflyn.broceliand.controller.model.ShippingChargeStrategyValueObject;
 import net.kerflyn.broceliand.model.Seller;
 import net.kerflyn.broceliand.model.charge.FixedShippingCharge;
 import net.kerflyn.broceliand.model.charge.ProportionalShippingCharge;
 import net.kerflyn.broceliand.model.charge.ShippingChargeStrategy;
 
 import java.math.BigDecimal;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 public final class ShippingCharges {
 
     public static final String POLICY_FIXED = "Fixed";
 
     public static final String POLICY_PROPORTIONAL = "Proportional";
 
     private ShippingCharges() {
         throw new UnsupportedOperationException();
     }
 
     public static Set<ShippingChargeStrategy> instantiateFrom(Map<String, String> form) {
         Map<String, Map<String, String>> strategyData = classifyStrategyData(form);
 
         HashSet<ShippingChargeStrategy> strategies = new HashSet<ShippingChargeStrategy>();
 
         for (Map<String, String> data: strategyData.values()) {
             ShippingChargeStrategy strategy = null;
 
             if (POLICY_FIXED.equals(data.get("policy"))) {
                 strategy = createFixedShippingChargeFrom(data);
             }
             else if (POLICY_PROPORTIONAL.equals(data.get("policy"))) {
                 strategy = createProportionalShippingChargeFrom(data);
             }
 
             strategies.add(strategy);
         }
 
         return strategies;
     }
 
     private static Map<String, Map<String, String>> classifyStrategyData(Map<String, String> form) {
         Map<String, Map<String, String>> strategyData = new HashMap<String, Map<String, String>>();
 
         for (Map.Entry<String, String> entry : form.entrySet()) {
             String key = entry.getKey();
 
             if (!key.startsWith("shipping-charge")) {
                 continue;
             }
 
             String index = getIndexFrom(key);
             String field = getFieldFrom(key);
 
             if (!strategyData.containsKey(index)) {
                 strategyData.put(index, new HashMap<String, String>());
             }
             strategyData.get(index).put(field, entry.getValue());
         }
         return strategyData;
     }
 
     private static String getFieldFrom(String key) {
         int indexSepLoc = key.lastIndexOf("-");
         String keyWithoutIndex = key.substring(0, indexSepLoc);
         return key.substring(keyWithoutIndex.lastIndexOf("-") + 1, indexSepLoc);
     }
 
     private static String getIndexFrom(String key) {
         return key.substring(key.lastIndexOf("-") + 1);
     }
 
     private static FixedShippingCharge createFixedShippingChargeFrom(Map<String, String> data) {
         FixedShippingCharge strategy = new FixedShippingCharge();
         strategy.setCharge(new BigDecimal(data.get("price")));
         strategy.setUpToQuantity(Integer.parseInt(data.get("quantity")));
         return strategy;
     }
 
     private static ProportionalShippingCharge createProportionalShippingChargeFrom(Map<String, String> data) {
         ProportionalShippingCharge strategy = new ProportionalShippingCharge();
        strategy.setPriceRate(new BigDecimal(data.get("rate")));
         strategy.setUpToQuantity(Integer.parseInt(data.get("quantity")));
         return strategy;
     }
 
     public static List<ShippingChargeStrategyValueObject> getShippingChargeStrategyFrom(Seller seller) {
         List<ShippingChargeStrategyValueObject> strategies = new ArrayList<ShippingChargeStrategyValueObject>();
         long index = 0;
 
         for (ShippingChargeStrategy strategy : seller.getShippingChargeStrategies()) {
             ShippingChargeStrategyValueObject valueObject = new ShippingChargeStrategyValueObject();
 
             valueObject.setId(index);
             valueObject.setQuantity(strategy.getUpToQuantity());
 
             if (strategy instanceof FixedShippingCharge) {
                 valueObject.setPolicy(POLICY_FIXED);
                 valueObject.setPrice(((FixedShippingCharge) strategy).getCharge());
             }
             else if (strategy instanceof ProportionalShippingCharge) {
                 valueObject.setPolicy(POLICY_PROPORTIONAL);
                valueObject.setRate(((ProportionalShippingCharge) strategy).getPriceRate());
             }
 
             strategies.add(valueObject);
             index++;
         }
 
         return strategies;
     }
 }
