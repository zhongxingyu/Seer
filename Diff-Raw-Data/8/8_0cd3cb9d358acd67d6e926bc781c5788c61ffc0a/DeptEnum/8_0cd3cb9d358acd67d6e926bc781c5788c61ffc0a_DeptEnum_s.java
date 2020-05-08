 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package org.puremvc.java.demos.javafx.employeeadmin.model.enums;
 
 import java.util.Map;
 import java.util.TreeMap;
 
 /**
  *
  * @author Jhonghee Park @ Seldon Systems, Inc.
  */
 public enum DeptEnum {
 
     NONE_SELECTED ("--None Selected--", -1),
     ACCT ("Accounting", 0),
    SALES ("Sales", 1),
    PLANT ("Plant", 2),
    SHIPPING ("Shipping", 3),
    QC ("Quality Control", 4);
 
     private static final Map<String, DeptEnum> enumMap = new TreeMap<String, DeptEnum>();
     static {
         for (DeptEnum dept : values()) {
             enumMap.put(dept.getLabel(), dept);
         }
     }
 
     private String label;
     private int value;
 
     private DeptEnum(String label, int value) {
         this.label = label;
         this.value = value;
     }
 
     public String getLabel() {
         return label;
     }
 
     public int getValue() {
         return value;
     }
 
     public static String[] getLabels() {
         return enumMap.keySet().toArray(new String[enumMap.size()]);
     }
 
     public static DeptEnum getEnumByLabel(String label) {
         return enumMap.get(label);
     }
 }
