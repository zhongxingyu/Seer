 package com.blogspot.pdrobushevich.screener.screener.data;
 
 import java.util.Comparator;
 
 public class CompanyComparator implements Comparator<Company> {
 
     private final String sortedColumn;
     private final int sortDirection;
 
     public CompanyComparator(final String sortedColumn, final int sortDirection) {
         this.sortedColumn = sortedColumn;
         this.sortDirection = sortDirection;
     }
 
     @Override
     public int compare(final Company company1, final Company company2) {
         if ("Company name".equalsIgnoreCase(sortedColumn)) {
             return company1.getName().compareTo(company2.getName()) * sortDirection;
         } else if ("Symbol".equalsIgnoreCase(sortedColumn)) {
             return company1.getSymbol().compareTo(company2.getSymbol()) * sortDirection;
         } else if ("Market cap.".equalsIgnoreCase(sortedColumn)) {
            return (int) (company1.getMktCap() - company2.getMktCap()) * sortDirection;
         } else if ("Price".equalsIgnoreCase(sortedColumn)) {
            return (int) (company1.getPrice() - company2.getPrice()) * sortDirection;
         }
         return 0;
     }
 
}
