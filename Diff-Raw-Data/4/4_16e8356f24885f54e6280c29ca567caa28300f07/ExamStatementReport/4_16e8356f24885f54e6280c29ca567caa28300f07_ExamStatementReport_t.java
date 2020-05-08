 package by.vsu.emdsproject.report.aspose.impl;
 
 import by.vsu.emdsproject.common.ReportUtil;
 import by.vsu.emdsproject.model.Group;
 import by.vsu.emdsproject.model.Student;
 import by.vsu.emdsproject.model.Teacher;
 import by.vsu.emdsproject.model.comparator.StudentComparator;
 import by.vsu.emdsproject.report.aspose.AsposeReport;
 import com.aspose.words.Document;
 import com.aspose.words.NodeType;
 import com.aspose.words.Row;
 import com.aspose.words.Table;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import java.util.regex.Pattern;
 
 public class ExamStatementReport extends AsposeReport {
 
     private Group group;
     private Teacher chief;
     private List<Teacher> teacherList;
 
     public static final String TEMPLATE_NAME = "ExamStatement.docx";
 
     public static Pattern GROUP_NAME = Pattern.compile("<groupName>");
     public static Pattern NUMBER = Pattern.compile("<n>");
     public static Pattern FIO = Pattern.compile("<fio>");
    public static Pattern TEACHER_FIO = Pattern.compile("<teacherFio>");
     public static Pattern CHIEF = Pattern.compile("<chief>");
 
     public ExamStatementReport(Group group, Teacher chief, List<Teacher> teacherList) {
         this.group = group;
         this.chief = chief;
         this.teacherList = teacherList;
     }
 
     @Override
     public Document generate() throws Exception {
 
         Document document = new Document(getTemplateFilePath(TEMPLATE_NAME));
 
         document.getRange().replace(GROUP_NAME, group.getTitle());
         document.getRange().replace(CHIEF, ReportUtil.getFullFIO(chief));
 
         String teachers = "";
         for (Teacher t : teacherList) {
             teachers += ReportUtil.getFullFIO(t) + ", ";
         }
         teachers.substring(0, teachers.length() - 2);
        document.getRange().replace(TEACHER_FIO, teachers);
         Table table = (Table) document.getChild(NodeType.TABLE, 0, true);
         Row lastRow = table.getLastRow();
         Integer number = 1;
         ArrayList<Student> students = new ArrayList<Student>(group.getStudents());
         Collections.sort(students, new StudentComparator());
         for (Student student : students) {
             table.appendChild(lastRow.deepClone(true));
             lastRow.getRange().replace(NUMBER, number.toString());
             lastRow.getRange().replace(FIO, ReportUtil.getShortFIO(student));
             number++;
             lastRow = table.getLastRow();
         }
         table.removeChild(lastRow);
 
         return document;
     }
 
 }
