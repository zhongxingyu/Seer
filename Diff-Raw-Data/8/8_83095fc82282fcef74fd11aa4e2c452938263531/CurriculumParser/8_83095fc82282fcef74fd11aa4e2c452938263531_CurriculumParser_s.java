 package ua.dp.primat.curriculum.planparser;
 
 import java.io.File;
 import java.io.FileInputStream;
import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Locale;
 import java.util.Map;
 import java.util.ResourceBundle;
 
 import org.apache.poi.hssf.usermodel.HSSFWorkbook;
 import org.apache.poi.ss.usermodel.Row;
 import org.apache.poi.ss.usermodel.Sheet;
 import org.apache.poi.ss.usermodel.Workbook;
 
 import ua.dp.primat.curriculum.data.LoadCategory;
 import ua.dp.primat.curriculum.data.StudentGroup;
 import ua.dp.primat.curriculum.data.WorkloadType;
 
 /**
  *
  * @author fdevelop
  *
  */
 public final class CurriculumParser {
 
     /**
      * Load language resources from .properties file
      * @param lang
      *          language identification (like "en", "uk" etc)
      */
     private void languageLoad(String lang) {
         ResourceBundle parserConstants = ResourceBundle.getBundle("parser", new Locale(lang));
         disciplineCategorySelective = parserConstants.getString("category.selective");
         disciplineCategoryAlternativeWar = parserConstants.getString("category.alternativewar");
         diffSetOff = parserConstants.getString("differentialsetoff");
     }
 
     private CurriculumParser(StudentGroup group, int sheetNum,
             int itemStart, int itemEnd, int semCount) {
         this.group = group;
         this.itemStart = itemStart;
         this.itemEnd = itemEnd;
         this.sheetNumber = sheetNum;
         this.semestersCount = semCount;
 
         languageLoad("uk");
     }
 
     /**
      * Constructor, that takes parser options and opens excel workbook at specified sheet.
      * @param group StudentGroup Entity that all items from this plan will be assigned to.
      * @param sheetNum Number of Excel sheet with the curriculum (starts from 0)
      * @param itemStart Number of row, that will be first to parse.
      * Generally, it is the first row after the curriculum header (starts from 0)
      * @param itemEnd Number of row, that will be last to parse (starts from 0)
      * @param semCount Count of semesters in the curriculum
      * @param fileName Excel file name with input curriculum
      */
     public CurriculumParser(StudentGroup group, int sheetNum,
             int itemStart, int itemEnd, int semCount, String fileName) throws IOException {
         //run general constructor
         this(group, sheetNum, itemStart, itemEnd, semCount);
 
         File fileExcel = null;
         fileExcel = new File(fileName);
 
         this.excelBook = new HSSFWorkbook(new FileInputStream(fileExcel));
         currentSheet = excelBook.getSheetAt(sheetNumber);
     }
 
     public CurriculumParser(StudentGroup group, int sheetNum,
             int itemStart, int itemEnd, int semCount, Workbook workBook) {
         //run general constructor
         this(group, sheetNum, itemStart, itemEnd, semCount);
         
         this.excelBook = workBook;
         currentSheet = excelBook.getSheetAt(sheetNumber);
     }
 
     /**
      * This method parses one excel row of curriculum, which contains
      * one discipline item. Cell indexes (start from 0) must follow constants
      * values with prefix "COL_" from this class.
      * @param row POI Row object with data
      * @param workloadType WorkloadType that will be assigned to the item
      * @param loadCategory LoadCategory that will be assigned to the item
      * @return CurriculumXLSRow object which contains parsed values
      */
     private CurriculumXLSRow parseXLSEntry(Row row, WorkloadType workloadType, LoadCategory loadCategory) {
         //parse info
         String subjName = row.getCell(COL_DISCIPLINE).getStringCellValue();
         String subjCath = row.getCell(COL_CATHEDRA).getStringCellValue();
         //entry
         String workCtrlExams = row.getCell(COL_FINALCONTROL).toString();
         String workCtrlMark = row.getCell(COL_FINALCONTROL+1).toString();
         String workCtrlCourse = row.getCell(COL_FINALCONTROL+2).toString();
         //internal ind. control
         String workIndSem = row.getCell(COL_INDIVIDUALTASKS).toString();
         String workIndForm = row.getCell(COL_INDIVIDUALTASKS+1).toString();
         String workIndWeek = row.getCell(COL_INDIVIDUALTASKS+2).toString();
         //hours
         Map<Integer, WorkHours> semesterWorkhours = new HashMap<Integer, WorkHours>();
         for (int sem=0; sem < semestersCount; sem++) {
             WorkHours semesterHoursInfo = new WorkHours();
             semesterHoursInfo.setHoursLec(row.getCell(COL_HOURS_LECTURE+COL_HOUROFFSET*sem).getNumericCellValue());
             semesterHoursInfo.setHoursPract(row.getCell(COL_HOURS_PRACTICE+COL_HOUROFFSET*sem).getNumericCellValue());
             semesterHoursInfo.setHoursLab(row.getCell(COL_HOURS_LAB+COL_HOUROFFSET*sem).getNumericCellValue());
             semesterHoursInfo.setHoursInd(row.getCell(COL_HOURS_INDIVIDUAL+COL_HOUROFFSET*sem).getNumericCellValue());
             semesterHoursInfo.setHoursSam(row.getCell(COL_HOURS_SELFWORK+COL_HOUROFFSET*sem).getNumericCellValue());
             if (semesterHoursInfo.getSum() > 0) {
                 semesterWorkhours.put(sem+1, semesterHoursInfo);
             }
         }
 
         //finally, create object
         return new CurriculumXLSRow(group, subjName, subjCath,
                 workCtrlExams, workCtrlMark, workCtrlCourse,
                 workIndSem, workIndForm, workIndWeek, semesterWorkhours,
                 workloadType, loadCategory, diffSetOff);
     }
 
     /**
      * Set the value of currentLoadCategory or currentWorkloadType, parsed from cell value.
      * @param cellText
      *          cell string value
      */
     private void changeEntriesCategoryOrType(String cellText) {
         if ( cellText.indexOf('.') > -1 ) {
             int dtype = Integer.parseInt( cellText.substring(0, cellText.indexOf('.')) );
             currentLoadCategory = LoadCategory.Normative;
             switch (dtype) {
                 case WTID_HUMANITIES:currentWorkloadType = WorkloadType.wtHumanities;
                     break;
                 case WTID_NATURALSCIENCE:currentWorkloadType = WorkloadType.wtNaturalScience;
                     break;
                 case WTID_PROFPRACT:currentWorkloadType = WorkloadType.wtProfPract;
                     break;
                 case WTID_PROFPRACTSTUDENT:currentWorkloadType = WorkloadType.wtProfPractStudent;
                     break;
                 case WTID_PROFPRACTUNIVER:currentWorkloadType = WorkloadType.wtProfPractUniver;
                     break;
             }
         } else {
             if (cellText.contains(disciplineCategoryAlternativeWar)) {
                 currentLoadCategory = LoadCategory.AlternativeForWar;
             } else if (cellText.contains(disciplineCategorySelective)) {
                 currentLoadCategory = LoadCategory.Selective;
             } else {
                 currentLoadCategory = LoadCategory.Normative;
             }
         }
     }
 
     /**
      * Launch the Parse procedure for configured (by constructor) CurriculumParser.
      * @return list of CurriculumXLSRow, that contain parsed values
      */
     public List<CurriculumXLSRow> parse() {
         if ((itemStart < 0) || (itemEnd > currentSheet.getPhysicalNumberOfRows()-1)) {
             throw new IndexOutOfBoundsException();
         }
 
         //assign default values for type and category of discipline
         currentWorkloadType = WorkloadType.wtProfPract;
         currentLoadCategory = LoadCategory.Normative;
 
         List<CurriculumXLSRow> entries = new ArrayList<CurriculumXLSRow>();
 
         //start reading excel rows
         for (int r = itemStart; r <= itemEnd; r++) {
             Row row = currentSheet.getRow(r);
 
             //could not parse item
             if ((row == null) ||
                 (row.getPhysicalNumberOfCells() == 0) ||
                 (row.getCell(0).toString().length() == 0)) {
                 continue;
             }
 
             //if this item is not a Database entry, it might be the option (WorkloadType, LoadCategory)
             if ((row.getCell(1) == null) || (row.getCell(1).toString().length() == 0)) {
                 String cellText = row.getCell(0).getStringCellValue().trim();
                 changeEntriesCategoryOrType(cellText);
             } else {
                 entries.add(parseXLSEntry(row, currentWorkloadType, currentLoadCategory));
             }
         }
         
         //get the result
         return entries;
     }
 
     /* CONSTANTS */
     /** Excel index for Name of Discipline */
     public static final int COL_DISCIPLINE = 1;
     /** Excel index for Cathedra of Discipline */
     public static final int COL_CATHEDRA = 2;
     /** Excel index for type of final control (exam, mark, differential mark, course) */
     public static final int COL_FINALCONTROL = 6;
     /** Excel index for types of individual tasks */
     public static final int COL_INDIVIDUALTASKS = 9;
     /** Excel indexes for hours for the Discipline */
     public static final int COL_HOURS_LECTURE = 22;
     public static final int COL_HOURS_PRACTICE = 23;
     public static final int COL_HOURS_LAB = 24;
     public static final int COL_HOURS_INDIVIDUAL = 25;
     public static final int COL_HOURS_SELFWORK = 26;
     /** Excel cells count for hours info of one semester */
     public static final int COL_HOUROFFSET = 6;
     //Workload Type Indexes
     public static final int WTID_HUMANITIES = 1;
     public static final int WTID_NATURALSCIENCE = 2;
     public static final int WTID_PROFPRACT = 3;
     public static final int WTID_PROFPRACTSTUDENT = 4;
     public static final int WTID_PROFPRACTUNIVER = 5;
 
     /* VARIABLES */
     private String diffSetOff;
     private String disciplineCategorySelective;
     private String disciplineCategoryAlternativeWar;
 
     private Workbook excelBook;
     private Sheet currentSheet;
 
     private WorkloadType currentWorkloadType;
     private LoadCategory currentLoadCategory;
 
     //pre-params
     private StudentGroup group;
     private int sheetNumber;
     private int itemStart;
     private int itemEnd;
     private int semestersCount;
 
     //getters and setters
     public StudentGroup getGroup() {
         return group;
     }
 
     public void setGroup(StudentGroup group) {
         this.group = group;
     }
 
     public int getItemEnd() {
         return itemEnd;
     }
 
     public void setItemEnd(int itemEnd) {
         this.itemEnd = itemEnd;
     }
 
     public int getItemStart() {
         return itemStart;
     }
 
     public void setItemStart(int itemStart) {
         this.itemStart = itemStart;
     }
 
     public int getSemestersCount() {
         return semestersCount;
     }
 
     public void setSemestersCount(int semestersCount) {
         this.semestersCount = semestersCount;
     }
 
     public int getSheetNumber() {
         return sheetNumber;
     }
 
     public void setSheetNumber(int sheetNumber) {
         currentSheet = excelBook.getSheetAt(sheetNumber);
         if (currentSheet != null) {
             this.sheetNumber = sheetNumber;
         } else {
             currentSheet = excelBook.getSheetAt(this.sheetNumber);
         }
     }
 }
