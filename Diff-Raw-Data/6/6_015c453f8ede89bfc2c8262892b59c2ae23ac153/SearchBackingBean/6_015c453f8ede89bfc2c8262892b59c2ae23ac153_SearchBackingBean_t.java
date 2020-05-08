 package backingbeans;
 
 /*
 * The sorting algorithm for sorting results is inspired by http://www.mkyong.com/jsf2/jsf-2-datatable-sorting-example/
  */
 
 /**
  *
 * @author marschu & thituson
  */
 import core.Product;
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.enterprise.context.*;
 import javax.faces.event.ActionEvent;
 import javax.faces.event.ActionListener;
 import javax.faces.event.ValueChangeEvent;
 import javax.inject.Inject;
 import javax.inject.Named;
 import javax.validation.constraints.Max;
 import javax.validation.constraints.Min;
 import javax.validation.constraints.NotNull;
 import javax.validation.constraints.Pattern;
 import javax.validation.constraints.Size;
 
 @Named
 @SessionScoped
 public class SearchBackingBean implements Serializable{
     
 
     private String searchName;
     private List<Product> resultProducts = new ArrayList<Product>();
     private boolean sortNameUp = true;
     private boolean sortPriceUp = true;
     private boolean sortSkillUp = true;
     private boolean sortCategoryUp = true;
 
     public SearchBackingBean(){}
 
     public String getSearchName() {
         return searchName;
     }
 
     public void setSearchName(String searchString) {
         this.searchName = searchString;
     }
 
 
     public void setResultProducts(List<Product> resultProducts) {
         this.resultProducts = resultProducts;
     }
 
     public List<Product> getResultProducts() {
         return resultProducts;
     }
     
     
     public String sortByName() 
     {
  
         if(sortNameUp)
         {
         Collections.sort(resultProducts, new Comparator<Product>() {
              @Override
              public int compare(Product p1, Product p2) 
              {
                  return p1.getName().compareTo(p2.getName());
              }
             });	
             sortNameUp = false;
         }
         else
         {
             Collections.sort(resultProducts, new Comparator<Product>() {
              @Override
              public int compare(Product p1, Product p2) 
              {
                  return p2.getName().compareTo(p1.getName());
              }
             });	
             sortNameUp = true;
         }
         
         return null;
     }
     
     public String sortByPrice()
     {
         if(sortPriceUp)
         {
         Collections.sort(resultProducts, new Comparator<Product>() {
              @Override
              public int compare(Product p1, Product p2) 
              {
                  return ((Double)p1.getPrice()).compareTo((Double)p2.getPrice());
              }
             });	
             sortPriceUp = false;
         }
         else
         {
             Collections.sort(resultProducts, new Comparator<Product>() {
              @Override
              public int compare(Product p1, Product p2) 
              {
                  return ((Double)p2.getPrice()).compareTo((Double)p1.getPrice());
              }
             });	
             sortPriceUp = true;
         }
         
         return null;
     }
     
     public String sortBySkill()
     {
         if(sortSkillUp)
         {
         Collections.sort(resultProducts, new Comparator<Product>() {
              @Override
              public int compare(Product p1, Product p2) 
              {
                  return p1.getRequiredSkill().compareTo(p2.getRequiredSkill());
              }
             });	
             sortSkillUp = false;
         }
         else
         {
             Collections.sort(resultProducts, new Comparator<Product>() {
              @Override
              public int compare(Product p1, Product p2) 
              {
                  return p2.getRequiredSkill().compareTo(p1.getRequiredSkill());
              }
             });	
             sortSkillUp = true;
         }
         
         return null;
     }
     
     public String sortByCategory()
     {
         if(sortCategoryUp)
         {
         Collections.sort(resultProducts, new Comparator<Product>() {
              @Override
              public int compare(Product p1, Product p2) 
              {
                  return p1.getCategory().compareTo(p2.getCategory());
              }
             });	
             sortCategoryUp = false;
         }
         else
         {
             Collections.sort(resultProducts, new Comparator<Product>() {
              @Override
              public int compare(Product p1, Product p2) 
              {
                  return p2.getCategory().compareTo(p1.getCategory());
              }
             });	
             sortCategoryUp = true;
         }
         
         return null;
     }
 
 
 
 }
