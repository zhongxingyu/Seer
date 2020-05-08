 package dk.itu.kf04.g4tw.model;
 
 import dk.itu.kf04.g4tw.util.DynamicArray;
 
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 public class AddressParser {
 
     /**
      * Named roads, all road segments with the same name is grouped
      */
     private static HashMap<String, DynamicArray<Road>> namedRoads = null;
 
     private static String sal = "(s|S)(a|A)(l|L)";
     private static String bs = "[a-zæøåüöéA-ZÆØÅÜÖÉ]";
 
     static String[] patterns = new String[] {
             /* Street Name */				"((?<=\\s|^)(?!"+sal+")"+bs+"{3,}\\s?)+",
             /* House Number */				"(?<=\\W|"+bs+"|^)\\d{1,3}(?!\\d|\\.)",
             /* House Letter */				"(?<=\\W\\d{1,3}\\s?)\\s?"+bs+"{1}(\\b?|$)",
             /* Floor */						"(?<=(\\d|"+bs+"|\\W)\\s?)(\\d{1,2}\\s?("+sal+"|\\.\\s?"+sal+")(\\s[fFhHmMtTvV\\.]{2,3})?)",
             /* Postal Code */				"\\d{4}\\s?",
             /* City */						"(?<=\\s|^)(?!"+sal+")("+bs+"{3,}\\s?)+\\s"+bs+"+"};
 
     /**
      * Return a dynamicarray of roads, that matches a given address
      * @param address The address to search for
      * @return return an DynamicArray of Road with all the hits
      * @throws IllegalArgumentException If the address is null or "".
      */
     public static DynamicArray<Road> getRoad(String address) throws IllegalArgumentException
     {
         // Throw exception if null or empty
         if (address == null || address.equals("")) {
             throw new IllegalArgumentException("Unable to parse empty address.");
         }
 
         /**
          * The index order og the array
          * index 0 = street name
          * index 1 = street number
          * index 2 = house letter
          * index 3 = house floor
          * index 4 = postal code
         * index 5 = city
          */
 
         // Split the address up into smaller parts
         String[] addressInfo = parseAddress(address);
 
         DynamicArray<Road> hits = namedRoads.get(addressInfo[0]);
         if(hits == null)
             hits = new DynamicArray<Road>();
 
         // If a postal code is given, use that to sort with
         if(!addressInfo[4].equals("") && hits.length() > 1)
         {
             hits = sortByPostal(hits, Integer.parseInt(addressInfo[4]));
         }
 
         // If a street number is given, use that to sort with
         if(!addressInfo[2].equals("") && hits.length() > 1) {
             hits = sortByLetter(hits, addressInfo[2]);
         }
 
         // Is a house letter is given, use that to sort with
         if(!addressInfo[1].equals("")  && hits.length() > 1) {
             hits = sortByNumber(hits, Integer.parseInt(addressInfo[1]));
         }
 
         return hits;
     }
 
     private static DynamicArray<Road> sortByPostal(DynamicArray<Road> arr, int postal)
     {
         DynamicArray<Road> returnArr = new DynamicArray<Road>();
 
         // Magic
         for(int i = 0; i < arr.length(); i++){
             Road r = arr.get(i);
             if(r.leftPostalCode == postal || r.rightPostalCode == postal)
                 returnArr.add(r);
         }
 
         return returnArr;
     }
 
     protected static DynamicArray<Road> sortByLetter(DynamicArray<Road> arr, String letter)
     {
         DynamicArray<Road> returnArr = new DynamicArray<Road>();
 
         for(int i = 0; i < arr.length(); i++){
             Road r = arr.get(i);
             if(r.startLetter != null) {
                 if(r.endLetter != null) {
                     if(letter.compareTo(r.startLetter) >= 0 && letter.compareTo(r.endLetter) <= 0) {
                         returnArr.add(r);
                     }
                 } else if(letter.compareTo(r.startLetter) >= 0) {
                     returnArr.add(r);
                 }
             } else if(r.endLetter != null && letter.compareTo(r.endLetter) <= 0) {
                 returnArr.add(r);
             }
         }
         return returnArr;
     }
 
     protected static DynamicArray<Road> sortByNumber(DynamicArray<Road> arr, int num)
     {
         DynamicArray<Road> returnArr = new DynamicArray<Road>();
 
         for(int i = 0; i < arr.length(); i++){
             Road r = arr.get(i);
             if(r.startNumber > 0) {
                 if(r.endNumber > 0) {
                     if(num >= r.startNumber && num <= r.endNumber)
                         returnArr.add(arr.get(i));
                 } else if(num >= r.startNumber)
                     returnArr.add(arr.get(i));
             } else if(r.endNumber > 0 && num <= r.endNumber)
                 returnArr.add(arr.get(i));
         }
         return returnArr;
     }
 
     protected static String[] parseAddress(String s) throws IllegalArgumentException {
         String[] address = new String[6];
         Arrays.fill(address, "");
 
         s = s.replaceAll(",", " ");
         s = s.replaceAll("#", " ");
 
         for(int i = 0; i < patterns.length; i++)
         {
             Pattern p = Pattern.compile(patterns[i]);
             Matcher m = p.matcher(s);
             if(m.find())
             {
                 address[i] = m.group().trim();
                 if(i == 0) s = s.replaceAll(m.group(), " ");
             }
         }
 
         if(address[0].equals("")) {
             throw new IllegalArgumentException();
         }
 
         return address;
     }
 
     public static void setNamedRoads(HashMap<String, DynamicArray<Road>> map) {
         namedRoads = map;
     }
 }
