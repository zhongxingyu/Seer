 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package cmput305.group.project;
 
 import java.util.ArrayList;
 import java.util.Comparator;
 import java.util.List;
 
 /**
  *
 * @author Charles Logan  & Jeff Thomas
  * @version 0.2 Set of filter, sorters for the LaurateCollection collection.
  *
  * TODO: ???
  */
 public class NobelCollectionManipulation {
 
     /*
      year;
      prize;
      name;
      longName;
      gender;
      photo;
      country;
      afiliation;
      birthYear;
      deathYear;
      biography;
      lecture;
      */
     /**
      *
      * @param inputList Give it the list of Laureate.
      * @param ListOfTerms Give it the string of terms for which winners to keep.
      * @return The new list of winners which references the terms.
      */
     public static List<Laureate> filterByName(List<Laureate> inputList, List<String> ListOfTerms) {
 
         List<Laureate> outputList = new ArrayList<>();
         // parse input, keep what matchs the terms.
         for (Laureate Awinner : inputList) {
             if (ListOfTerms.contains(Awinner.getName())) {
                 outputList.add(Awinner);
             }
         }
         return outputList;
     }
 
     /**
      *
      * @param inputList
      * @param ListOfTerms
      * @return
      */
     public static List<Laureate> filterByPrize(List<Laureate> inputList, List<String> ListOfTerms) {
 
         List<Laureate> outputList = new ArrayList<Laureate>();
         // parse input, keep what matchs the terms.
         for (Laureate Awinner : inputList) {
             if (ListOfTerms.contains(Awinner.getPrize())) {
                 outputList.add(Awinner);
             }
         }
         return outputList;
     }
 
     /**
      *
      * @param inputList
      * @param ListOfTerms
      * @return
      */
     public static List<Laureate> filterByYear(List<Laureate> inputList, List<String> ListOfTerms) {
 
         List<Laureate> outputList = new ArrayList<Laureate>();
         // parse input, keep what matchs the terms.
         for (Laureate Awinner : inputList) {
             if (ListOfTerms.contains(Awinner.getYear())) {
                 outputList.add(Awinner);
             }
         }
         return outputList;
     }
 
     /**
      *
      * @param inputList
      * @param ListOfTerms
      * @return
      */
     public static List<Laureate> filterByLongName(List<Laureate> inputList, List<String> ListOfTerms) {
 
         List<Laureate> outputList = new ArrayList<Laureate>();
         // parse input, keep what matchs the terms.
         for (Laureate Awinner : inputList) {
             if (ListOfTerms.contains(Awinner.getLongName())) {
                 outputList.add(Awinner);
             }
         }
         return outputList;
     }
 
     /**
      *
      * @param inputList
      * @param ListOfTerms
      * @return
      */
     public static List<Laureate> filterByGender(List<Laureate> inputList, List<String> ListOfTerms) {
 
         List<Laureate> outputList = new ArrayList<Laureate>();
         // parse input, keep what matchs the terms.
         for (Laureate Awinner : inputList) {
             if (ListOfTerms.contains(Awinner.getGender())) {
                 outputList.add(Awinner);
             }
         }
         return outputList;
     }
 
     /**
      *
      * @param inputList
      * @param ListOfTerms
      * @return
      */
     public static List<Laureate> filterByCountry(List<Laureate> inputList, List<String> ListOfTerms) {
 
         List<Laureate> outputList = new ArrayList<Laureate>();
         // parse input, keep what matchs the terms.
         for (Laureate Awinner : inputList) {
             if (ListOfTerms.contains(Awinner.getCountry())) {
                 outputList.add(Awinner);
             }
         }
         return outputList;
     }
 
     /**
      *
      * @param inputList
      * @param ListOfTerms
      * @return
      */
     public static List<Laureate> filterByAfiliation(List<Laureate> inputList, List<String> ListOfTerms) {
 
         List<Laureate> outputList = new ArrayList<Laureate>();
         // parse input, keep what matchs the terms.
         for (Laureate Awinner : inputList) {
             if (ListOfTerms.contains(Awinner.getAfiliation())) {
                 outputList.add(Awinner);
             }
         }
         return outputList;
     }
 
     /**
      *
      * @param inputList
      * @param ListOfTerms
      * @return
      */
     public static List<Laureate> filterByBirthYear(List<Laureate> inputList, List<String> ListOfTerms) {
 
         List<Laureate> outputList = new ArrayList<Laureate>();
         // parse input, keep what matchs the terms.
         for (Laureate Awinner : inputList) {
             if (ListOfTerms.contains(Awinner.getBirth())) {
                 outputList.add(Awinner);
             }
         }
         return outputList;
     }
 
     /**
      *
      * @param inputList
      * @param ListOfTerms
      * @return
      */
     public static List<Laureate> filterByDeathYear(
             List<Laureate> inputList, List<String> ListOfTerms) {
 
         List<Laureate> outputList = new ArrayList<Laureate>();
         // parse input, keep what matchs the terms.
         for (Laureate Awinner : inputList) {
             if (ListOfTerms.contains(Awinner.getDeath())) {
                 outputList.add(Awinner);
             }
         }
         return outputList;
     }
 
     /**
      * Filters collection for Laurates prize winners who lived (startYear,
      * endYear). If you want within a exact year, set start and end years the
      * same. If you don't want to constrain the domain of years, use -1 at start
      * or end year.
      *
      * @param inputList collection
      * @param startYear inclusive start of domain
      * @param endYear inclusive end of domain
      * @return
      */
     public static List<Laureate> filterByLivingYearRange(
             List<Laureate> inputList, String startYear, String endYear) {
 
         List<Laureate> outputList = new ArrayList<Laureate>();
         int sYear = Integer.getInteger(startYear);
         int eYear = Integer.getInteger(endYear);
 
         // parse input, keep what matchs the terms.
         for (Laureate Awinner : inputList) {
             Integer birthYear = Integer.getInteger(Awinner.getBirth()); // if bad string, returns null.
             if (birthYear >= sYear) {
                 Integer deathYear = Integer.getInteger(Awinner.getBirth());
                 if ((deathYear == null) || (deathYear <= ((eYear < 0) ? (deathYear + 1) : eYear))) {
                     outputList.add(Awinner);
                 }
             }
         }
         return outputList;
     }
 
     /**
      * Filters collection for Laurates prize winners within (startYear,
      * endYear). If you want a exact year, set start and end years the same. If
      * you don't want to constrain the domain of years, use -1 at start or end
      * year.
      *
      * @param inputList collection
      * @param startYear inclusive start of domain
      * @param endYear inclusive end of domain
      * @return
      */
     public static List<Laureate> filterByPrizeYearRange(
             List<Laureate> inputList, String startYear, String endYear) {
 
         List<Laureate> outputList = new ArrayList<Laureate>();
         int sYear = Integer.getInteger(startYear);
         int eYear = Integer.getInteger(endYear);
 
 
         // parse input, keep what matchs the terms.
         for (Laureate Awinner : inputList) {
             int year = Integer.getInteger(Awinner.getYear());
             if (year >= sYear) {
                 if (year <= ((eYear < 0) ? (year + 1) : eYear)) {
                     outputList.add(Awinner);
                 }
             }
         }
         return outputList;
     }
 
     // comparators ------------------
     /**
      * Standard comparator. See class title for purpose.
      */
     public class NameComparator implements Comparator<Laureate> {
 
         @Override
         public int compare(Laureate t, Laureate t1) {
             return t.getName().compareTo(t1.getName());
         }
     }
     
         /**
      * Standard comparator. See class title for purpose.
      */
     public class ReverseNameComparator implements Comparator<Laureate> {
 
         @Override
         public int compare(Laureate t, Laureate t1) {
             return -1 * t.getName().compareTo(t1.getName());
         }
     }
 
     /**
      * Standard comparator. See class title for purpose.
      */
     public class YearComparator implements Comparator<Laureate> {
 
         @Override
         public int compare(Laureate t, Laureate t1) {
             return t.getYear().compareTo(t1.getYear());
         }
     }
     
         /**
      * Standard comparator. See class title for purpose.
      */
     public class ReverseYearComparator implements Comparator<Laureate> {
 
         @Override
         public int compare(Laureate t, Laureate t1) {
             return -1 *t.getYear().compareTo(t1.getYear());
         }
     }
 
 
     /**
      * Standard comparator. See class title for purpose.
      */
     public class PrizeComparator implements Comparator<Laureate> {
 
         @Override
         public int compare(Laureate t, Laureate t1) {
             return t.getPrize().compareTo(t1.getPrize());
         }
     }
     
     
     /**
      * Standard comparator. See class title for purpose.
      */
     public class ReversePrizeComparator implements Comparator<Laureate> {
 
         @Override
         public int compare(Laureate t, Laureate t1) {
             return -1 * t.getPrize().compareTo(t1.getPrize());
         }
     }
 
     /**
      * Standard comparator. See class title for purpose.
      */
     public class LongnameComparator implements Comparator<Laureate> {
 
         @Override
         public int compare(Laureate t, Laureate t1) {
             return t.getLongName().compareTo(t1.getLongName());
         }
     }
 
     /**
      * Standard comparator. See class title for purpose.
      */
     public class GenderComparator implements Comparator<Laureate> {
 
         @Override
         public int compare(Laureate t, Laureate t1) {
             return t.getGender().compareTo(t1.getGender());
         }
     }
     
         /**
      * Standard comparator. See class title for purpose.
      */
     public class ReverseGenderComparator implements Comparator<Laureate> {
 
         @Override
         public int compare(Laureate t, Laureate t1) {
             return -1 *t.getGender().compareTo(t1.getGender());
         }
     }
 
 
     /**
      * Standard comparator. See class title for purpose.
      */
     public class AfiliationComparator implements Comparator<Laureate> {
 
         @Override
         public int compare(Laureate t, Laureate t1) {
             return t.getAfiliation().compareTo(t1.getAfiliation());
         }
     }
 
     /**
      * Standard comparator. See class title for purpose.
      */
     public class BirthyearComparator implements Comparator<Laureate> {
 
         @Override
         public int compare(Laureate t, Laureate t1) {
             return t.getBirth().compareTo(t1.getBirth());
         }
     }
 
     /**
      * Standard comparator. See class title for purpose.
      */
     public class DeathyearComparator implements Comparator<Laureate> {
 
         @Override
         public int compare(Laureate t, Laureate t1) {
             return t.getDeath().compareTo(t1.getDeath());
         }
     }
     
         /**
      * Standard comparator. See class title for purpose.
      */
     public class CountryComparator implements Comparator<Laureate> {
 
         @Override
         public int compare(Laureate t, Laureate t1) {
             return t.getCountry().compareTo(t1.getCountry());
         }
     }
     
             /**
      * Standard comparator. See class title for purpose.
      */
     public class ReverseCountryComparator implements Comparator<Laureate> {
 
         @Override
         public int compare(Laureate t, Laureate t1) {
             return -1 *t.getCountry().compareTo(t1.getCountry());
         }
     }
 }
