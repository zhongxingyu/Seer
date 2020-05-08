 package com.steamedpears.comp3004.models;
 
 import com.google.gson.JsonElement;
 import com.google.gson.JsonObject;
 
 import java.util.*;
 
 public class AssetMap extends HashMap<String, Integer> {
 
     /**
      * A helper method to calculate the sum of exactly two asset maps
      * @param first an asset map
      * @param last another asset map
      * @return an asset map corresponding to the sum of the two asset maps
      */
     public static AssetMap sum(AssetMap first, AssetMap last){
         Collection<AssetMap> col = new ArrayList<AssetMap>();
         col.add(first);
         col.add(last);
         return sum(col);
     }
 
     /**
      * Calculates the asset map corresponding to the sum of given asset maps
      * @param assets the collection of asset maps to sum
      * @return the sum of the asset maps, as an asset map
      */
     public static AssetMap sum(Collection<AssetMap> assets){
         AssetMap result = new AssetMap();
         for(AssetMap assetMap: assets){
             result.add(assetMap);
         }
 
         return result;
     }
 
     /**
      * A helper method to calculate the result of subtracting one asset map from another
      * @param first the asset map from which to be subtracted
      * @param last the asset map to subtract
      * @return the asset map result of subtracting last from first
      */
     public static AssetMap difference(AssetMap first, AssetMap last){
         AssetMap result = new AssetMap();
         for(String key: first.keySet()){
             int difference = first.get(key) - last.get(key);
             if(difference>0){
                 result.put(key, difference);
             }
         }
         return result;
     }
 
     /**
      * Creates an empty AssetMap
      */
     public AssetMap(){}
 
     /**
      * Creates an AssetMap matching the given JSON object
      * @param obj the JSON object to match
      */
     public AssetMap(JsonObject obj){
         if(obj!=null){
             Set<Map.Entry<String, JsonElement>> entrySet = obj.entrySet();
             for(Map.Entry<String, JsonElement> entry: entrySet){
                 put(entry.getKey(), entry.getValue().getAsInt());
             }
         }
     }
 
     /**
      * Creates an AssetMap matching the child of the given JSON object, or a blank one if no child exists
      * @param obj The JSON object to extract the child from
      * @param key The key of the JSON object to get
      */
     public AssetMap(JsonObject obj, String key){
         this(obj.has(key) ? obj.getAsJsonObject(key) : new JsonObject());
     }
 
     @Override
     public Integer get(Object key){
         if(containsKey(key)){
             return super.get(key);
         }else{
             return 0;
         }
     }
 
     @Override
     public Integer put(String key, Integer value){
         if(value<0){
             value = 0;
         }
         if(value == 0){
             int oldValue = get(key);
             remove(key);
             return oldValue;
         }
         return super.put(key, value);
     }
 
     /**
      * Adds the values of the given assetMap to this one
      * @param assetMap The map to add
      */
     public void add(AssetMap assetMap) {
         for(String key: assetMap.keySet()){
             add(key, assetMap.get(key));
         }
     }
 
     /**
      * Adds the specified amount to the assetMap
      * @param key The key to add the value at
      * @param value The amount to add to the assetMap
      */
     public void add(String key, Integer value) {
         if(containsKey(key)){
             put(key, get(key) + value);
         }else{
             put(key, value);
         }
     }
 
     /**
      * Adds one of the specified type to the assetMap
      * @param key The key to add the value at
      */
     public void add(String key){
         add(key, 1);
     }
 
     /**
      * Subtracts the values of the given assetMap from this one
      * @param assetMap The assetMap to subtract
      */
     public void subtract(AssetMap assetMap){
         for(String key: assetMap.keySet()){
             subtract(key, assetMap.get(key));
         }
     }
 
     /**
      * Subtracts the value from the assetMap
      * @param key The key to subtract the value at
      * @param value The value to subtract
      */
     public void subtract(String key, Integer value) {
         put(key, get(key)-value);
     }
 
     /**
      * Subtracts one from the assetMap
      * @param key The key to subtract from
      */
     public void subtract(String key) {
         subtract(key, 1);
     }
 
     /**
      * Checks if the given choices of assets can be used to pay the given cost
      * @param choices list of sets of assets which can be used to pay the cost
      * @return true if choices can be used to pay cost
      */
     public boolean existsValidChoices(List<AssetSet> choices){
         return recursiveSearchForValidChoices(choices, 0);
     }
 
     private boolean recursiveSearchForValidChoices(List<AssetSet> choices, int index){
        if(choices.isEmpty()){
            return isEmpty();
         }
         Set<String> choice = choices.get(index);
         //loop over every asset in this choice
         boolean everRecursed = false;
         for(String asset: choice){
             //if this asset would help pay for the cost, try it
             if(containsKey(asset)){
                 everRecursed = true;
                 subtract(asset);
                 //see if we've found a solution
                 boolean foundSolution = isEmpty();
 
                 //if that didn't solve it, recurse on the next choice
                 if(!foundSolution && index+1<choices.size()){
                     foundSolution = recursiveSearchForValidChoices(choices, index+1);
                 }
                 //if we found a solution, return true; otherwise put the asset back and try the next asset in the choice
                 add(asset);
                 if(foundSolution){
                     return true;
                 }
             }
         }
         //this asset is totally useless, go around it!
         if(!everRecursed && index+1<choices.size()){
             return recursiveSearchForValidChoices(choices, index+1);
         }
         //if we've gotten to here, no choice in this subtree works
         return false;
     }
 
     /**
      * Get some valid choices for trades that pay for the cost described by this AssetMap, or null if none appears to exist
      * NOTE: this method is not guaranteed to always return a correct or optimal answer
      * @param choices List of AssetMaps representing the definite resources of a target
      * @param optionalChoices List of Lists of AssetSets representing the optional choices each target has
      * @param discounts the discounts are target has
      * @return The valid choices, or null if none is found
      */
     public List<AssetMap> getValidChoices(List<AssetMap> choices, List<List<AssetSet>> optionalChoices, List<AssetSet> discounts){
         List<AssetMap> result = new ArrayList<AssetMap>();
         Map<AssetMap, AssetMap> originalAssociations = new HashMap<AssetMap,AssetMap>();
         for(int i=0; i<choices.size(); ++i){
             result.add(new AssetMap());
             originalAssociations.put(choices.get(i),result.get(i));
         }
 
         AssetMap costLessBase = new AssetMap();
         costLessBase.add(this);
 
         CheapestComparator cheapestComparator = new CheapestComparator(choices, discounts);
 
         for(String asset: keySet()){
             cheapestComparator.setAsset(asset);
             Collections.sort(choices, cheapestComparator);
             for(AssetMap choiceMap: choices){
                 int amount = Math.min(choiceMap.get(asset), costLessBase.get(asset));
                 costLessBase.subtract(asset, amount);
                 originalAssociations.get(choiceMap).add(asset, amount);
             }
         }
 
         //note: this method call changes result
         if(costLessBase.recursiveSearchForValidChoices(result, optionalChoices, 0)){
             return null;
         }
 
         return result;
     }
 
     private boolean recursiveSearchForValidChoices(List<AssetMap> result, List<List<AssetSet>> choicesList, int index){
         int offset = 0;
         int targetNumber = 0;
         int totalSize = 0;
 
         for(List<AssetSet> choices: choicesList){
             totalSize+=choices.size();
         }
 
         List<AssetSet> choices = choicesList.get(0);
 
         while(index-offset>=choices.size()){
             offset+=choices.size();
             targetNumber++;
             if(targetNumber>=choicesList.size()){
                 return isEmpty();
             }
             choices = choicesList.get(targetNumber);
         }
 
         AssetMap currentResult = result.get(targetNumber);
 
         Set<String> choice = choices.get(index);
         //loop over every asset in this choice
         boolean everRecursed = false;
         for(String asset: choice){
             //if this asset would help pay for the cost, try it
             if(containsKey(asset)){
                 everRecursed = true;
                 subtract(asset);
                 currentResult.add(asset);
                 //see if we've found a solution
                 boolean foundSolution = isEmpty();
 
                 //if that didn't solve it, recurse on the next choice
                 if(!foundSolution && index+1<totalSize){
                     foundSolution = recursiveSearchForValidChoices(choices, index+1);
                 }
                 //if we found a solution, return true; otherwise put the asset back and try the next asset in the choice
                 if(foundSolution){
                     return true;
                 }
 
                 //note: this undo only happens if we didn't find a result
                 add(asset);
                 currentResult.remove(asset);
             }
         }
         //this asset is totally useless, go around it!
         if(!everRecursed && index+1<totalSize){
             return recursiveSearchForValidChoices(choices, index+1);
         }
 
         //if we've gotten to here, no choice in this subtree works
         return false;
     }
 
     private class CheapestComparator implements Comparator<AssetMap>{
         private String asset;
         private Map<AssetMap, AssetSet> discounts;
 
         private CheapestComparator(List<AssetMap> choices, List<AssetSet> discounts){
             this.discounts = new HashMap<AssetMap, AssetSet>();
             for(int i=0; i<choices.size(); ++i){
                 this.discounts.put(choices.get(i), discounts.get(i));
             }
         }
 
         @Override
         public int compare(AssetMap o1, AssetMap o2) {
             if(discounts.get(o1).contains(asset)){
                 return 1;
             }else if(discounts.get(o2).contains(asset)){
                 return -1;
             }else{
                 return 0;
             }
         }
 
         private void setAsset(String asset){
             this.asset = asset;
         }
     }
 
 }
