 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package uk.tripbrush.service;
 
 import java.io.BufferedInputStream;
 import java.io.FileInputStream;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Iterator;
 import org.apache.poi.xssf.usermodel.XSSFCell;
 import org.apache.poi.xssf.usermodel.XSSFRow;
 import org.apache.poi.xssf.usermodel.XSSFSheet;
 import org.apache.poi.xssf.usermodel.XSSFWorkbook;
 import org.hibernate.classic.Session;
 import uk.tripbrush.model.travel.Attraction;
 import uk.tripbrush.model.travel.AttractionSeason;
 import uk.tripbrush.model.travel.AttractionTime;
 import uk.tripbrush.util.DateUtil;
 import uk.tripbrush.util.PojoConstant;
 
 /**
  *
  * @author sseetal
  */
 public class UploadService {
 
 
     public static String getCellValue(Iterator cells) {
         XSSFCell cell = (XSSFCell) cells.next();
         cell.setCellType(XSSFCell.CELL_TYPE_STRING);
         String result = cell.getStringCellValue();
         return result;
     }
     
     public static String[] getRow(XSSFRow row) {
         ArrayList<String> result = new ArrayList<String>();
         Iterator cells = row.cellIterator();
 
         while (cells.hasNext()) {
             result.add(getCellValue(cells));
         }
         return result.toArray(new String[0]);
     }
     
     public static void addTime(Session session,Attraction attraction,int dow,String time) {
         AttractionTime result = new AttractionTime();
         result.setAttraction(attraction);
         result.setDow(dow);
         String[] seasonsplit = time.split(";");
         String[] timesplit = seasonsplit[0].split("-");
         if (timesplit.length==2) {
             String[] from = timesplit[0].trim().split(":");
             if (from.length==2) {
                 String[] to = timesplit[1].trim().split(":");
                 result.setStarthour(Integer.parseInt(from[0].trim()));
                 result.setStartminute(Integer.parseInt(from[1].trim()));
                 result.setEndhour(Integer.parseInt(to[0].trim()));
                 result.setEndminute(Integer.parseInt(to[1].trim())); 
                 if (result!=null) {
                     session.saveOrUpdate(result);
                 }
             } 
         }
         if (seasonsplit.length>1) {
             for (int counter=1;counter<seasonsplit.length;counter++) {
                 timesplit = seasonsplit[counter].split("-");
                 AttractionSeason season = new AttractionSeason();
                 season.setAttraction(attraction);
                 season.setDow(dow);
                 if (timesplit.length==4) {
                     String fromdate = timesplit[0].trim();
                     String todate = timesplit[1].trim();
                     season.setFromday(Calendar.getInstance());
                     season.setToday(Calendar.getInstance());
                     season.getFromday().setTime(DateUtil.formatDate(fromdate));
                     season.getToday().setTime(DateUtil.formatDate(todate));
                     String[] from = timesplit[2].trim().split(":");
                     if (from.length==2) {
                         String[] to = timesplit[3].trim().split(":");
                         season.setStarthour(Integer.parseInt(from[0].trim()));
                         season.setStartminute(Integer.parseInt(from[1].trim()));
                         season.setEndhour(Integer.parseInt(to[0].trim()));
                         season.setEndminute(Integer.parseInt(to[1].trim()));
                         if (season!=null) {
                             session.saveOrUpdate(season);
                         }
                     }
                 }
             }
         }
     }
     
     public static String getRowValue(String[] row, int id) {
         if (row.length>id) {
             return row[id];
         }
         return "";
     }
     
     public static void process(InputStream is) throws Exception {
         Session session = Database.getSession();
 
         Database.beginTransaction();
 
         for (Object atr: session.createCriteria(PojoConstant.ATTRACTIONTIME_MODEL).list()) {
             session.delete(atr);
         }
         for (Object atr: session.createCriteria(PojoConstant.ATTRACTIONEVENT_MODEL).list()) {
             session.delete(atr);
         }        
         for (Object atr: session.createCriteria(PojoConstant.ATTRACTIONSEASON_MODEL).list()) {
             session.delete(atr);
         }
         for (Object atr: session.createCriteria(PojoConstant.ATTRACTION_MODEL).list()) {
             session.delete(atr);
         }
         
         XSSFWorkbook wb = new XSSFWorkbook(is);  
         
         XSSFSheet sheet = wb.getSheetAt(0);
         Iterator rows = sheet.rowIterator();
 
 
 
 
 
         String[] row = getRow((XSSFRow) rows.next());
         while (rows.hasNext()) {
             row = getRow((XSSFRow) rows.next());
             Attraction attraction = new Attraction();
             attraction.setUniqueId(Integer.parseInt(getRowValue(row,0)));
             attraction.setCategory(CalendarService.getCategory(getRowValue(row,1)));
             if (attraction.getCategory()==null) {
                 continue;
             }
             attraction.setLocation(CommonService.getLocation(getRowValue(row,3)));
             attraction.setName(getRowValue(row,2));
             attraction.setPostcode(getRowValue(row,5));
             attraction.setDescription_short(getRowValue(row,14));
             attraction.setDescription(getRowValue(row,15));
             attraction.setImageFileName(getRowValue(row,17));
             attraction.setImageFileName_small(getRowValue(row,16));
             attraction.setWikiurl(getRowValue(row,18));
             attraction.setOtherlinks(getRowValue(row,19));
             session.save(attraction);
             
             addTime(session,attraction,Calendar.MONDAY,getRowValue(row,7));
             addTime(session,attraction,Calendar.TUESDAY,getRowValue(row,8));
             addTime(session,attraction,Calendar.WEDNESDAY,getRowValue(row,9));
             addTime(session,attraction,Calendar.THURSDAY,getRowValue(row,10));
             addTime(session,attraction,Calendar.FRIDAY,getRowValue(row,11));
             addTime(session,attraction,Calendar.SATURDAY,getRowValue(row,12));
             addTime(session,attraction,Calendar.SUNDAY,getRowValue(row,13));
             
             
         }
 
         Database.commitTransaction();
     }
     
     public static String convertArray(String[] input) {
         StringBuffer result = new StringBuffer();
         for (String r : input) {
             result.append(r);
             result.append("\t");
         }
         return result.toString();
     }    
     
     public static void main(String[] args) throws Exception {
        String file = "C://Users//Manu//Documents//Attraction Data Capture v13.xlsx";
        //String file = "/Users/sseetal/Dropbox/Life Made Easy Ltd/Attraction Data Capture v13.xlsx";
         InputStream input = new BufferedInputStream(new FileInputStream(file));
         process(input);      
     }
 }
