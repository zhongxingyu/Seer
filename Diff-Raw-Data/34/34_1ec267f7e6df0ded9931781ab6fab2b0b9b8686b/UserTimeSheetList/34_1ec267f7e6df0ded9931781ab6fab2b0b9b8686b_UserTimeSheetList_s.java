 package com.roosterpark.rptime.selenium.control.complex.list.timesheet;
 
 import com.roosterpark.rptime.selenium.control.complex.list.generator.UserTimeSheetRowGenerator;
 import org.openqa.selenium.WebDriver;
 
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 /**
  * User: John
  * Date: 1/7/14
  * Time: 2:08 PM
  */
 public class UserTimeSheetList {
 
     private WebDriver driver;
     private UserTimeSheetRowGenerator generator;
     private List<UserTimeSheetRow> rows;
     private Map<Integer, Map<Integer, UserTimeSheetRow>> rowsByYearAndWeek;
     private Map<String, UserTimeSheetRow> rowsByDate;
 
     public UserTimeSheetList(WebDriver driver) {
         this.driver = driver;
         generator = new UserTimeSheetRowGenerator(driver);
     }
 
     public WebDriver getDriver() {
         return driver;
     }
 
     public void init() {
         rows = generator.generate();
         initializeRowsByYearAndWeek();
         rowsByDate = new HashMap<>();
         for (UserTimeSheetRow row: rows) {
             rowsByDate.put(row.getDate(), row);
         }
     }
 
     private void initializeRowsByYearAndWeek() {
         rowsByYearAndWeek = new HashMap<>();
         for (UserTimeSheetRow row: rows) {
             if (rowsByYearAndWeek.containsKey(row.getYear())) {
                 Map<Integer, UserTimeSheetRow> rowsByWeek = rowsByYearAndWeek.get(row.getYear());
                 rowsByWeek.put(row.getWeek(), row);
                 rowsByYearAndWeek.put(row.getYear(), rowsByWeek);
             } else {
                 rowsByYearAndWeek.put(row.getYear(), new HashMap<Integer, UserTimeSheetRow>());
                 Map<Integer, UserTimeSheetRow> rowsByWeek = rowsByYearAndWeek.get(row.getYear());
                 rowsByWeek.put(row.getWeek(), row);
                 rowsByYearAndWeek.put(row.getYear(), rowsByWeek);
             }
         }
     }
 
     public UserTimeSheetRow getRowByDate(String date) {
         return rowsByDate.get(date);
     }
 
     public UserTimeSheetRow getRowByYearAndWeek(Integer year, Integer week) {
         Map<Integer, UserTimeSheetRow> rowsByWeek = rowsByYearAndWeek.get(year);
         return rowsByWeek.get(week);
     }
 
 }
