 package ro.finsiel.eunis.search.species.names;
 
 
 import ro.finsiel.eunis.search.AbstractSortCriteria;
 
 
 /**
  * Sort criteria used for species->names.
  * @author finsiel
  */
 public class NameSortCriteria extends AbstractSortCriteria {
 
     /**
      * Do not sort.
      */
     public static final Integer SORT_NONE = new Integer(0);
 
     /**
      * Sort by group.
      */
     public static final Integer SORT_GROUP = new Integer(1);
 
     /**
      * Sort by family.
      */
     public static final Integer SORT_FAMILY = new Integer(4);
 
     /**
      * Sort by order.
      */
     public static final Integer SORT_ORDER = new Integer(2);
 
     /**
      * Sort by scientific name.
      */
     public static final Integer SORT_SCIENTIFIC_NAME = new Integer(3);
 
     /**
      * Sort by valid name.
      */
     public static final Integer SORT_VALID_NAME = new Integer(5);
 
     /**
      * Sort by type of result (found by scientific name / common name)
      */
     public static final Integer SORT_S_ORDER = new Integer(6);
 
     /**
      * New sort criteria.
      * @param sortCriteria Criteria used for sorting.
      * @param ascendency Ascendency.
      * @param searchVernacular Search in vernacular names also
      */
     public NameSortCriteria(Integer sortCriteria, Integer ascendency, Boolean searchVernacular) {
         setSortCriteria(sortCriteria);
         setAscendency(ascendency);
         // Initialize the mappings
         possibleSorts.put(SORT_NONE, "none"); // If none, then DO NOT SORT
         possibleSorts.put(SORT_GROUP, "commonName"); // Group
         possibleSorts.put(SORT_ORDER, "taxonomicNameOrder"); // Order
         possibleSorts.put(SORT_FAMILY, "taxonomicNameFamily"); // Family
         if (searchVernacular) {
             possibleSorts.put(SORT_SCIENTIFIC_NAME, "scientificName"); // Scientific name
             possibleSorts.put(SORT_VALID_NAME, "validName"); // Valid name
            possibleSorts.put(SORT_S_ORDER, "S_ORDER");
         } else {
             possibleSorts.put(SORT_SCIENTIFIC_NAME, "A.SCIENTIFIC_NAME"); // Scientific name
             possibleSorts.put(SORT_VALID_NAME, "A.VALID_NAME"); // Valid name
         }

 
     }
 }
