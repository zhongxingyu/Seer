 package cz.muni.fi.pv243.et.controller;
 
 import cz.muni.fi.pv243.et.model.*;
 
 import javax.enterprise.context.SessionScoped;
 import javax.enterprise.inject.Produces;
 import javax.inject.Named;
 import java.io.Serializable;
 import java.util.*;
 
 
 @SessionScoped
 public class ExpenseModel implements Serializable {
 
     private ExpenseReport report;
 
     private String backUrl;
 
     public String getBackUrl() {
         return backUrl;
     }
 
     public void setBackUrl(String backUrl) {
         this.backUrl = backUrl;
     }
 
     @Produces
     @Named("expenseReport")
     public ExpenseReport getReport() {
         return report;
     }
 
     public void setReport(ExpenseReport report) {
         this.report = report;
     }
 
 }
