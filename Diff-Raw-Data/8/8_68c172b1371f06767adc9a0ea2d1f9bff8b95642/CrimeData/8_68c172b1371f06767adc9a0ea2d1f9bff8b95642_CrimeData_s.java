 import java.util.ArrayList;
 import parser.csvReader;
 
 public class CrimeData implements StatisticData {
     // Stores the tags associated with crime rates
     private static ArrayList<String> tags;
    // Stores one data corresponding to one region
     private ArrayList<String> data;
     private static csvReader reader = new csvReader();
 
     public ArrayList<String> getData() {
         return data;
     }
 
     public ArrayList<String> getTags() {
         return tags;
     }
 
     /**
      * @param line Line in csv file containing tags of the data
      */
     public void setTags(String line) {
         tags = reader.parse(line);
     }
 
     /*
      * Sets the crime data for a region. Region is always 
      * first element of the row.
      * @param line Line in csv file containing a row of data
      */
     public void setRegionData(String line) {
         data = reader.parse(line);
         if (!data.get(1).equals("Total"))
             throw new IllegalArgumentException();
 
         String regionName = data.get(0);
         // throw out first two elements as all data stored in them is
         // mirrored in the region and city classes
         data.remove(0);
         data.remove(1);
        Country.getCountry().getRegion(regionName).setData(this);
     }
 
     public void setCityData(String line) {
         data = reader.parse(line);
         String regionName = data.get(0);
         String cityName = data.get(1);
         // throw out first two elements as all data stored in them is
         // mirrored in the region and city classes
         data.remove(0);
         data.remove(1);
        Country.getCountry().getRegion(regionName).getCity(cityName).setData(this);
     }
 }
