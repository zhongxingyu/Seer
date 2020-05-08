 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package netcracker.service;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import netcracker.dao.DAOFactory;
 import org.apache.poi.hssf.usermodel.*;
 import org.apache.poi.hssf.util.HSSFColor;
 
 /**
  *
  * @author lastride
  */
 public class ExcelExport {
     public static boolean exportToExcel() {
         PreparedStatement stmt = null;
         ResultSet result = null;
         Connection conn = DAOFactory.createConnection();
         FileOutputStream fos = null;
         HSSFWorkbook workbook = new HSSFWorkbook();
         try {
             HSSFSheet advertsSheet = workbook.createSheet("adverts");
             HSSFSheet advertsForStudentsSheet = workbook.createSheet("advertsForStudents");
             HSSFSheet employeesSheet = workbook.createSheet("employees");
             HSSFSheet facultiesSheet = workbook.createSheet("faculties");
             HSSFSheet interestsSheet = workbook.createSheet("interests");
             HSSFSheet interestsForStudentsSheet = workbook.createSheet("interestsForStudents");
             HSSFSheet intervalsSheet = workbook.createSheet("intervals");
             HSSFSheet intervalsForStudentsSheet = workbook.createSheet("intervalsForStudents");
             HSSFSheet intervalStatusesSheet = workbook.createSheet("intervalStatuses");
             HSSFSheet messagesSheet = workbook.createSheet("messages");
             HSSFSheet resultsSheet = workbook.createSheet("results");
             HSSFSheet rolesSheet = workbook.createSheet("roles");
             HSSFSheet skillsSheet = workbook.createSheet("skills");
             HSSFSheet skillsForStudentsSheet = workbook.createSheet("skillsForStudents");
             HSSFSheet skillsTypesSheet = workbook.createSheet("skillsTypes");
             HSSFSheet studentsSheet = workbook.createSheet("students");
             HSSFSheet universitiesSheet = workbook.createSheet("universities");
                        
             
             ArrayList<HSSFSheet> sheets = new ArrayList<HSSFSheet>();
             sheets.add(advertsSheet); sheets.add(advertsForStudentsSheet);
             sheets.add(employeesSheet); sheets.add(facultiesSheet);
             sheets.add(interestsSheet); sheets.add(interestsForStudentsSheet);
             sheets.add(intervalsSheet); sheets.add(intervalsForStudentsSheet);
             sheets.add(intervalStatusesSheet); sheets.add(messagesSheet);
             sheets.add(resultsSheet); sheets.add(rolesSheet);
             sheets.add(skillsSheet); sheets.add(skillsForStudentsSheet);
             sheets.add(skillsTypesSheet); sheets.add(studentsSheet);
             sheets.add(universitiesSheet);
            
             String[] advertsFields = {"id_advert", "advert_name"};
             String[] advertsForStudentsFields = {"id_student", "id_advert", "notes"};
             String[] employeesFields = {"id_employee", "login", "password",
                                         "first_name", "last_name", "email", "id_role"};
             String[] facultiesFields = {"id_faculty", "id_university", "faculty_name"};
             String[] interestsFields = {"id_interest", "interst_name"};
             String[] interestsForStudentsFields = {"id_interest", "id_student", "mark", "notes"};
             String[] intervalsFields = {"id_interval", "start_time", "end_time", "interviewers_count", "id_interval_status"};
             String[] intervalsForStudentsFields = {"id_interval", "id_student"};
             String[] intervalStatusesFields = {"id_interval_status", "interval_status_name"};
             String[] messagesFields = {"id_student", "edit_values", "status"};
             String[] resultsFields = {"id_student", "id_employee", "comment"};
             String[] rolesFields = {"id_role", "role_name"};
             String[] skillsFields = {"id_skill", "skill_name", "id_skill_type"};
             String[] skillsForStudentsFields = {"id_skill", "id_student", "mark", "notes"};
             String[] skillsTypesFields = {"id_skill_type", "skill_type_name"};
             String[] studentsFields = {"id_student", "first_name", "last_name", "middle_name", "course", "study_end_year",
                                     "id_faculty", "email1", "email2", "phone1", "extra_contacts", "why", "experience",
                                     "extra", "photo"};
             String[] universitiesFields = {"id_university", "university_name"};
             
             ArrayList<String[]> strings = new ArrayList<String[]>();
             strings.add(advertsFields); strings.add(advertsForStudentsFields);
             strings.add(employeesFields); strings.add(facultiesFields);
             strings.add(interestsFields); strings.add(interestsForStudentsFields);
             strings.add(intervalsFields); strings.add(intervalsForStudentsFields);
             strings.add(intervalStatusesFields); strings.add(messagesFields);
             strings.add(resultsFields); strings.add(rolesFields);
             strings.add(skillsFields); strings.add(skillsForStudentsFields);
             strings.add(skillsTypesFields); strings.add(studentsFields);
             strings.add(universitiesFields);
             
             
             HSSFCellStyle style = workbook.createCellStyle();
             style.setBorderTop((short) 6); // double lines border
             style.setBorderBottom((short) 1); // single line border
             style.setFillBackgroundColor(HSSFColor.GREY_25_PERCENT.index);
             
             HSSFFont font = workbook.createFont();
             font.setFontName(HSSFFont.FONT_ARIAL);
             font.setFontHeightInPoints((short) 12);
             font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
             font.setColor(HSSFColor.BLUE.index);
             
             style.setFont(font);
             
             HSSFRow row = null;
             for (int i = 0; i < sheets.size(); i++) {
                 row = sheets.get(i).createRow(0);
                   for (int j = 0; j < strings.get(i).length; j++) {
                         HSSFCell cell = row.createCell(j);
                         cell.setCellValue(strings.get(i)[j]);
                         cell.setCellStyle(style);
                         sheets.get(i).autoSizeColumn((short)j);
                   }
             }
             
             
             String[] tableNames = {"adverts", "advertsForStudents", "employees", "faculties",
                                     "interests", "interestsForStudents", "intervals",
                                     "intervalsForStudents", "intervalStatuses",
                                     "messages", "results", "roles", "skills",
                                     "skillsForStudents", "skillsTypes", "students",
                                     "universities"};
             
             StringBuffer sbEmployees = null;
             HSSFRow rowT = null;
             for (int i = 0; i < tableNames.length; i++) {
                 sbEmployees = new StringBuffer();
                 sbEmployees.append("SELECT * FROM ");
                 sbEmployees.append(tableNames[i]);
                 stmt = conn.prepareStatement(sbEmployees.toString());
                 result = stmt.executeQuery();
                 int c = 0;
                 while (result.next()) {
                     c++;
                     rowT = sheets.get(i).createRow(c);
                     for (int j = 0; j < strings.get(i).length; j++) {
                         HSSFCell cellT = rowT.createCell(j);
                         cellT.setCellValue(result.getString(strings.get(i)[j]));
                        sheets.get(i).autoSizeColumn((short)j);

                     }
                 }
             }
         }
         catch (SQLException ex) {
             System.out.print("\nSQL exception in exportToExcel");
         }
         finally {
             DAOFactory.closeConnection(conn);
             DAOFactory.closeStatement(stmt);
         }
         /**
          * Write xls file
          */
         try {
            fos = new FileOutputStream(new File("/home/lastride/DataBase.xls"));
             workbook.write(fos);
         } catch (IOException e) {
             e.printStackTrace();
         } finally {
             if (fos != null) {
                 try {
                     fos.flush();
                     fos.close();
                 } catch (IOException e) {
                     e.printStackTrace();
                 }
             }
         }
         
         return true;
     }
     public static void main(String[] args) {
         ExcelExport.exportToExcel();
     }
 }
