 package org.pit.fetegeo.importer.processors;
 
 import org.pit.fetegeo.importer.objects.Constants;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.net.URL;
 import java.util.HashMap;
 import java.util.Map;
 
 /**
  * Author: Pit Apps
  * Date: 21/11/12
  * Time: 15:11
  */
 public class CountryCodeProcessor {
 
   private final static Map<String, Long> countryCodeMap = new HashMap<String, Long>();
   private final CleverWriter countryCodeWriter;
 
   /*
    Constructor takes a country writer object as parameter and immediately populates the countryCodeMap and country.txt
   */
   public CountryCodeProcessor(CleverWriter countryCodeWriter) {
     this.countryCodeWriter = countryCodeWriter;
 
     try {
       fetchAndSaveCountries();
     } catch (IOException ioe) {
       System.out.print(ioe);
     }
   }
 
   /*
    This method finds country codes from COUNTRY_ISO_CODE_URL, parses them and then adds them to
     the countryCodeMap and country.txt file for subsequent database copy.
   */
   private void fetchAndSaveCountries() throws IOException {
     URL url = new URL(Constants.COUNTRY_ISO_CODE_URL);
     InputStream inputStream = url.openStream();
    BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
 
     long countryId = 0l;
     String line, name, iso3166_2, iso3166_3;
 
     while ((line = br.readLine()) != null) {
       if (line.startsWith("#")) continue;
 
       String[] tokens = line.split(";");
 
       if (tokens.length < 4) continue;
 
       iso3166_2 = tokens[0].trim().toUpperCase();
       iso3166_3 = tokens[1].trim().toUpperCase();
       name = tokens[3].trim();        // English name
 
       countryCodeWriter.writeField(countryId);
 
       if (!iso3166_2.isEmpty()) {
         countryCodeWriter.writeField(iso3166_2);
       } else {
         countryCodeWriter.writeField(Constants.NULL_STRING);
       }
 
       if (!iso3166_3.isEmpty()) {
         countryCodeWriter.writeField(iso3166_3);
       } else {
         countryCodeWriter.writeField(Constants.NULL_STRING);
       }
       countryCodeMap.put(name, countryId);
       countryCodeWriter.writeField(name);
 
       countryCodeWriter.endRecord();
       countryId++;
     }
 
     br.close();
     inputStream.close();
   }
 
   /*
    Returns the database id of a specified ISO country code
   */
   public static Long findCountryId(String country) {
     return countryCodeMap.get(country);
   }
 
 }
