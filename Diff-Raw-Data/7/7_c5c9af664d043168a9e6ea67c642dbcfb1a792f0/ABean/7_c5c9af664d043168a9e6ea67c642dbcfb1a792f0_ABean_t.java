 /*
  * ABean.java
  *
  * Created on November 29, 2006, 3:46 PM
  *
  * To change this template, choose Tools | Template Manager
  * and open the template in the editor.
  */
 
 package test.source;
 
 import java.util.Map;
 import java.util.Set;
 
 /**
  *
  * @author cooper
  */
 public class ABean {
     
     private String stringProperty;
     private int intProperty;
     private String[] arrayProperty;
     private BBean[][] beanArray;
     private Map<String, BBean> beanMap;
     public char charProperty;
     public Set<String> stringSet;
    public long propertyNotOnDestination = Long.MAX_VALUE;
     /** Creates a new instance of ABean */
     public ABean() {
     }
 
     public String getStringProperty() {
         return stringProperty;
     }
 
     public void setStringProperty(String stringProperty) {
         this.stringProperty = stringProperty;
     }
 
     public int getIntProperty() {
         return intProperty;
     }
 
     public void setIntProperty(int intProperty) {
         this.intProperty = intProperty;
     }
 
     public String[] getArrayProperty() {
         return arrayProperty;
     }
 
     public void setArrayProperty(String[] arrayProperty) {
         this.arrayProperty = arrayProperty;
     }
 
     public BBean[][] getBeanArray() {
         return beanArray;
     }
 
     public void setBeanArray(BBean[][] beanArray) {
         this.beanArray = beanArray;
     }
 
     public Map<String, BBean> getBeanMap() {
         return beanMap;
     }
 
     public void setBeanMap(Map<String, BBean> beanMap) {
         this.beanMap = beanMap;
     }
     
 }
