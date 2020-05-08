 package com.nclodger.additional;
 
 import org.apache.poi.hssf.usermodel.HSSFRow;
 import org.apache.poi.hssf.usermodel.HSSFSheet;
 import org.apache.poi.hssf.usermodel.HSSFWorkbook;
 
 import javax.servlet.http.HttpServletResponse;
 import java.io.FileOutputStream;
 import java.util.ArrayList;
 
 
 /**
  * Created with IntelliJ IDEA.
  * User: pasha
  * Date: 11/24/13
  * Time: 7:56 PM
  * To change this template use File | Settings | File Templates.
  */
 public class ReportInExcel {
 
     public ReportInExcel(){
 
     }
 
     public void createMostPopularHotel(ArrayList<HotelTotalOrder> hList,HttpServletResponse response){
         try{
             String filename="hotelList.xls" ;
             HSSFWorkbook hwb=new HSSFWorkbook();
             HSSFSheet sheet =  hwb.createSheet("new sheet");
 
             HSSFRow rowhead=   sheet.createRow((short)0);
 
             rowhead.createCell((short) 0).setCellValue("Hotel Name");
             rowhead.createCell((short) 1).setCellValue("City");
             rowhead.createCell((short) 2).setCellValue("Country");
             rowhead.createCell((short) 3).setCellValue("Total Order");
 
             for(int i=0;i<hList.size();i++){
                 HSSFRow row = sheet.createRow(i + 1);
 
                 row.createCell((short) 0).setCellValue(hList.get(i).getHotelname());
                 row.createCell((short) 1).setCellValue(hList.get(i).getCity());
                 row.createCell((short) 2).setCellValue(hList.get(i).getCountry());
                 row.createCell((short) 3).setCellValue(hList.get(i).getTotalOrder());
 
             }
 
             response.setContentType("application/vnd.ms-excel");
             response.setHeader("Content-Disposition", "attachment; filename=hotelList.xls");
             hwb.write(response.getOutputStream());
             response.getOutputStream().close();
             //System.out.println("Your excel file has been generated!");
 
         } catch ( Exception ex ) {
             System.out.println(ex);
         }
     }
 
     public void createMostValuableAccomodation(ArrayList<AccommodationTotalValue> aList,HttpServletResponse response){
         try{
             String filename="AccList.xls" ;
             HSSFWorkbook hwb=new HSSFWorkbook();
             HSSFSheet sheet =  hwb.createSheet("new sheet");
 
             HSSFRow rowhead=   sheet.createRow((short)0);
 
             rowhead.createCell((short) 0).setCellValue("Type");
             rowhead.createCell((short) 1).setCellValue("Hotel name");
             rowhead.createCell((short) 2).setCellValue("City");
             rowhead.createCell((short) 3).setCellValue("Country");
             rowhead.createCell((short) 4).setCellValue("Total Value");
 
             for(int i=0;i<aList.size();i++){
                 HSSFRow row = sheet.createRow(i + 1);
 
                 row.createCell((short) 0).setCellValue(aList.get(i).getType());
                 row.createCell((short) 1).setCellValue(aList.get(i).getHotel_name());
                 row.createCell((short) 2).setCellValue(aList.get(i).getCity());
                 row.createCell((short) 3).setCellValue(aList.get(i).getCountry());
                 row.createCell((short) 4).setCellValue(aList.get(i).getTotalValue());
 
             }
 
 
             response.setContentType("application/vnd.ms-excel");
            response.setHeader("Content-Disposition", "attachment; filename=hotelList.xls");
             hwb.write(response.getOutputStream());
             response.getOutputStream().close();
            // System.out.println("Your excel file has been generated!");
 
         } catch ( Exception ex ) {
             System.out.println(ex);
         }
     }
 
 }
