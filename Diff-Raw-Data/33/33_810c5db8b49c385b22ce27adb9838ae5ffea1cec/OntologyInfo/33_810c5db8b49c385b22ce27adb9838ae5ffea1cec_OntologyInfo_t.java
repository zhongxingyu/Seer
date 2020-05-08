 package gov.nih.nci.evs.browser.utils;
 
 import java.util.*;
 
 public class OntologyInfo {
     private String _displayName = "";
    private String _codingScheme = "";
     private int _sortCategory = 0;
 
    public OntologyInfo(String displayName, String codingScheme, String sortCategory) {
         try {
            if (sortCategory == null || sortCategory.trim().length() <= 0)
                throw new Exception("Sort Category not defined.");
             int category = Integer.parseInt(sortCategory);
            init(displayName, codingScheme, category);
         } catch (Exception e) {
            init(displayName, codingScheme, 0);
         }
     }
 
    public OntologyInfo(String displayName, String codingScheme, int sortCategory) {
        init(displayName, codingScheme, sortCategory);
     }
 
    private void init(String displayName, String codingScheme, int sortCategory) {
         _displayName = displayName;
        _codingScheme = codingScheme;
         _sortCategory = sortCategory;
     }
 
     public String getDisplayName() {
         return _displayName;
     }
 
    public String getCodingScheme() {
        return _codingScheme;
    }

     public int getSortCategory() {
         return _sortCategory;
     }
 
     public static class ComparatorImpl implements Comparator<OntologyInfo> {
         public int compare(OntologyInfo value1, OntologyInfo value2) {
             int sortCategory1 = value1.getSortCategory();
             int sortCategory2 = value2.getSortCategory();
             if (sortCategory1 != sortCategory2)
                 return sortCategory2 - sortCategory1;
 
             String displayName1 = value1.getDisplayName();
             String displayName2 = value2.getDisplayName();
             return displayName1.compareToIgnoreCase(displayName2);
         }
     }
 }
