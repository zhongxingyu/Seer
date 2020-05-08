 package at.ac.tuwien.sepm.service.impl;
 
 import at.ac.tuwien.sepm.entity.*;
 import at.ac.tuwien.sepm.service.ServiceException;
 import at.ac.tuwien.sepm.service.LvaFetcherService;
 import at.ac.tuwien.sepm.service.LvaType;
 import at.ac.tuwien.sepm.service.Semester;
 import at.ac.tuwien.sepm.service.TimeFrame;
 import org.apache.log4j.LogManager;
 import org.apache.log4j.Logger;
 import org.joda.time.DateTime;
 import org.joda.time.format.DateTimeFormat;
 import org.jsoup.Jsoup;
 import org.jsoup.nodes.Document;
 import org.jsoup.nodes.Element;
 import org.jsoup.select.Elements;
 import org.springframework.stereotype.Service;
 
 import java.io.IOException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.regex.Pattern;
 
 @Service
 public class LvaFetcherServiceImpl implements LvaFetcherService {
 
     private static Logger log = LogManager.getLogger(LvaFetcherServiceImpl.class);
 
     //@Value("${timeout}")
     private static int timeout = 10000;
 
     //@Value("${baseUrl}")
     private static String BASE_URL = "https://tiss.tuwien.ac.at/";
     //private static final String BASE_URL = "https://tiss.tuwien.ac.at/";
 
     private static final String ACADEMICPROG_URL = "/curriculum/studyCodes.xhtml";
 
     private static final String ACADEMICPROG_TABLE_PATH = "form#studyCodeListForm";
     private static final String ACADEMICPROG_TABLE_HEADING_PATH = "h2";
     private static final String ACADEMICPROG_TABLE_ACADEMICPROG_ROW_PATH = "tbody tr";
 
     private static final String ACADEMICPROG_TABLE_ACADEMICPROG_ROW_NUMBER_PATH = ".studyCodeColumn";
     private static final String ACADEMICPROG_TABLE_ACADEMICPROG_ROW_NUMBER_PATH_FIND_BY_NR = ".studyCodeColumn:contains(%s)";
 
     private static final String ACADEMICPROG_TABLE_ACADEMICPROG_ROW_DESC_PATH = ".studyCodeNameColumn a";
 
 
     @Override
     public List<Curriculum> getAcademicPrograms() throws ServiceException {
         return getAcademicPrograms(false);
     }
 
     @Override
     public List<Curriculum> getAcademicPrograms(boolean recursive) throws ServiceException {
         try {
             URL curriculumUrl = new URL(BASE_URL + ACADEMICPROG_URL);
             Document curriculumDoc = Jsoup.parse(curriculumUrl, timeout);
 
             List<Curriculum> curriculums = new ArrayList<>();
 
             Elements curriculumTable = curriculumDoc.select(ACADEMICPROG_TABLE_PATH);
             for(Element curriculum : curriculumTable.select(ACADEMICPROG_TABLE_HEADING_PATH)) {
                 Elements studyTable = curriculum.nextElementSibling().select(ACADEMICPROG_TABLE_ACADEMICPROG_ROW_PATH); // td to drop header
                 for(Element studies : studyTable) {
                     Curriculum cur = new Curriculum();
                     cur.setStudyNumber(studies.select(ACADEMICPROG_TABLE_ACADEMICPROG_ROW_NUMBER_PATH).text());
                     Element lvaDesc = studies.select(ACADEMICPROG_TABLE_ACADEMICPROG_ROW_DESC_PATH).first();
                     cur.setName(lvaDesc.text());
                     curriculums.add(cur);
                     if(recursive) {
                         if(cur.getStudyNumber() != null && !cur.getStudyNumber().isEmpty()) {
                             List<Module> lm = getModules(cur.getStudyNumber());
                             if(lm != null) {
                                 HashMap<Module, Boolean> moduleBooleanMap = new HashMap<>();
                                 for(Module m : lm)
                                     moduleBooleanMap.put(m, true);
                                 cur.setModules(moduleBooleanMap);
                             }
                         }
                     }
                 }
             }
             return curriculums;
         } catch (IOException e) {
             throw new ServiceException(e);
         }
     }
 
     private String getUrlForCurriculumByStudyNumber(String studyNumber) throws ServiceException {
         try {
             URL curriculumUrl = new URL(BASE_URL + ACADEMICPROG_URL);
             Document curriculumDoc = Jsoup.parse(curriculumUrl, timeout);
             Element getElement = curriculumDoc.select(ACADEMICPROG_TABLE_PATH + " " + ACADEMICPROG_TABLE_ACADEMICPROG_ROW_PATH + " " + String.format(ACADEMICPROG_TABLE_ACADEMICPROG_ROW_NUMBER_PATH_FIND_BY_NR, studyNumber)).first();
             return BASE_URL+getElement.nextElementSibling().select("a").attr("href");
         } catch (NullPointerException | IOException e) {
             throw new ServiceException(e);
         }
     }
 
 
     private static final String CURRICULUM_SEMESTER_LINK = "&semester=CURRENT";
     private static final String CURRICULUM_TABLE = "table#nodeTable tbody tr";
     private static final String CURRICULUM_TABLE_ELEMENTS = ".nodeTable-level-2, .nodeTable-level-3, .nodeTable-level-4";
     private static final String CURRICULUM_TABLE_MODULE_NAME_CLASS = "nodeTable-level-2";
     private static final String CURRICULUM_TABLE_MODULE_LVA_CLASS = "nodeTable-level-4";
     private static final String CURRICULUM_TABLE_MODULE_LVA_CLASS_KEY = ".courseKey";
     private static final String CURRICULUM_TABLE_MODULE_CATALOG_INDICATOR = "Katalog";
 
 
     @Override
     public List<Module> getModules(String studyNumber) throws ServiceException {
         return getModules(studyNumber, false);
     }
 
     private String getCurrentSemester() {
         return String.valueOf(DateTime.now().getYear()) + ((DateTime.now().getMonthOfYear() < 8)? "S":"W");
     }
 
     @Override
     public List<Module> getModules(String studyNumber, boolean recursive) throws ServiceException {
         return getModules(studyNumber, recursive, getCurrentSemester());
     }
 
     @Override
     public List<Module> getModules(String studyNumber, boolean recursive, String semester) throws ServiceException {
         try {
             if(studyNumber == null || studyNumber.isEmpty())
                 throw new ServiceException("invalid study number");
             List<Module> moduleList = new ArrayList<>();
             URL studyUrl = new URL(getUrlForCurriculumByStudyNumber(studyNumber)+CURRICULUM_SEMESTER_LINK);
             Document curriculumDoc = Jsoup.parse(studyUrl, timeout);
 
             Elements lvaTable = curriculumDoc.select(CURRICULUM_TABLE);
             // get all modules, meta lvas and lvas
             Module lastModule = null;
             for(Element e : lvaTable.select(CURRICULUM_TABLE_ELEMENTS)) {
                 if(e.attr("class").trim().equals(CURRICULUM_TABLE_MODULE_NAME_CLASS)) {
                     if(recursive && e.text().startsWith(CURRICULUM_TABLE_MODULE_CATALOG_INDICATOR)) {
                         try {
                             lastModule.setMetaLvas(getCatalogByUrl(e.select("a").attr("href"), semester));
                         } catch (NullPointerException ex) {
                             log.info("Catalog: " + e.text() + " has no link");
                         }
                     } else {
                         lastModule = new Module();
                         lastModule.setName(e.text());
                         log.info("Start new Module: " + lastModule.toString());
                         moduleList.add(lastModule);
                     }
                 } else if(recursive && e.attr("class").trim().startsWith(CURRICULUM_TABLE_MODULE_LVA_CLASS)) {
                     if(lastModule != null) {
                         if(lastModule.getMetaLvas() == null)
                             lastModule.setMetaLvas(new ArrayList<MetaLVA>());
                         String lvaNr = e.select(CURRICULUM_TABLE_MODULE_LVA_CLASS_KEY).text().split(" ", 2)[0];
                         if(lvaNr.isEmpty())
                             continue;
                         MetaLVA lva = getLva(lvaNr, semester);
                         log.info("Add LVA " + lva.toString() + " to Module " + lastModule.toString());
                         lastModule.getMetaLvas().add(lva);
                     } else
                         log.warn("LVA without Module: " + e.text());
                 }
             }
             return moduleList;
         } catch (IOException e) {
             throw new ServiceException(e);
         }
     }
 
     private static final String URL_ADD_CURRENT_SEMESTER  = "&semester=CURRENT";
 
     private List<MetaLVA> getCatalogByUrl(String url, String semester) throws IOException, ServiceException {
         ArrayList<MetaLVA> metaLVAs = new ArrayList<>();
         URL studyUrl = new URL(BASE_URL+url+URL_ADD_CURRENT_SEMESTER);
         Document curriculumDoc = Jsoup.parse(studyUrl, timeout);
 
         Elements lvaKeys = curriculumDoc.select(CURRICULUM_TABLE_MODULE_LVA_CLASS_KEY);
         for(Element lva : lvaKeys) {
             metaLVAs.add(getLva(flattenLvaNr(lva.text().split(" ")[0]), semester));
         }
 
         return metaLVAs;
     }
 
     private static final String LVA_LINK = "/course/courseDetails.xhtml?courseNr=%s&semester=%s";
     private static final String LVA_CONTENT = "div#contentInner";
     private static final String LVA_CONTENT_HEAD = "h1";
     private static final String LVA_CONTENT_SUBHEAD = "div#subHeader";
 
     private static final String REGEX_SINGLE_DATE = "^(\\d)+\\.(\\d)+\\.(\\d)+$";
 
     private String flattenLvaNr(String lvaNr) {
         return lvaNr.replace(".", "").replace(" ", "");
     }
 
     @Override
     public MetaLVA getLva(String lvaNr, String semester) throws ServiceException {
         try {
             if(lvaNr == null)
                 throw new ServiceException("invalid lva nr");
             if(semester == null)
                 throw new ServiceException("invalid semester");
             MetaLVA metaLVA = new MetaLVA();
             URL lvaUrl = new URL(BASE_URL + String.format(LVA_LINK, flattenLvaNr(lvaNr), semester));
             Document lvaDoc = Jsoup.parse(lvaUrl, timeout);
             Elements lvaContent = lvaDoc.select(LVA_CONTENT);
             String[] head = lvaContent.select(LVA_CONTENT_HEAD).text().split(" ", 2);
             metaLVA.setNr(head[0]);
             metaLVA.setName(head[1]);
             String[] subhead = lvaContent.select(LVA_CONTENT_SUBHEAD).text().split(",");
             metaLVA.setType(LvaType.valueOf(subhead[1].trim()));
             metaLVA.setECTS(Float.parseFloat(subhead[3].trim().substring(0, subhead[3].length()-3))); // Kick EC
             LVA lva = new LVA();
             List<LVA> lvaList = new ArrayList<>();
             lvaList.add(lva);
             metaLVA.setLVAs(lvaList);
             lva.setYear(Integer.parseInt(subhead[0].trim().substring(0, subhead[0].trim().length() - 1)));
             lva.setSemester(Semester.valueOf(String.valueOf(subhead[0].charAt(4))));
             try {
                 lva.setGoals(lvaContent.select("h2:contains(Ziele der Lehrveranstaltung)").first().nextElementSibling().text());
             } catch (NullPointerException ex) {
                 log.info("LVA: " + metaLVA.getName() + " no goals found");
             }
             try {
                 lva.setContent(lvaContent.select("h2:contains(Inhalt der Lehrveranstaltung)").first().nextElementSibling().text());
             } catch (NullPointerException ex) {
                 log.info("LVA: " + metaLVA.getName() + " no content found");
             }
             try {
                 lva.setAdditionalInfo1(lvaContent.select("h2:contains(Weitere Informationen)").first().nextElementSibling().text());
             } catch (NullPointerException ex) {
                 log.info("LVA: " + metaLVA.getName() + " no additional infos1 found");
             }
             try {
                 lva.setInstitute(lvaContent.select("h2:contains(Institut)").first().nextElementSibling().text());
             } catch (NullPointerException ex) {
                 log.info("LVA: " + metaLVA.getName() + " no institute found");
             }
             try {
                 lva.setPerformanceRecord(lvaContent.select("h2:contains(Leistungsnachweis)").first().nextElementSibling().text());
             } catch (NullPointerException ex) {
                 log.info("LVA: " + metaLVA.getName() + " no performance record found");
             }
             try {
                 lva.setAdditionalInfo2(lvaContent.select("h2:contains(Weitere Informationen)").get(1).nextElementSibling().text());
             } catch (NullPointerException | IndexOutOfBoundsException ex) {
                 log.info("LVA: " + metaLVA.getName() + " no additional infos2 found");
             }
             /* dropped ?
             try {
                 Elements elements = lvaContent.select("h2:contains(Vortragende)").first().nextElementSibling().select("li");
                 ArrayList<String> lec = new ArrayList<>();
                 for(Element element : elements) {
                     lec.add(element.text());
                 }
                 lva.setLecturer(lec);
             } catch (NullPointerException ex) {
                 log.info("LVA: " + metaLVA.getName() + " no institute found");
             }
             */
             try {
                 Elements lvaDateTable = lvaContent.select("h2:contains(LVA Termine)").first().nextElementSibling().select("tbody tr");
                 ArrayList<LvaDate> lecturesList = new ArrayList<>();
                 for(Element e : lvaDateTable) {
                     Elements elem = e.select("td");
                     String[] time = elem.get(1).text().split(" - ");
                     String[] date = elem.get(2).text().split(" - ");
                    if(Pattern.compile(REGEX_SINGLE_DATE).matcher(date[0]).find()) {
                         LvaDate lvaDate = new LvaDate();
                         lvaDate.setName(metaLVA.getType() +" "+ metaLVA.getName());
                         lvaDate.setType(LvaDateType.LECTURE);
                         DateTime startTime = DateTime.parse(date[0] + " " + time[0],
                                 DateTimeFormat.forPattern("dd.MM.yyyy HH:mm"));
                         DateTime endTime = DateTime.parse(date[0] + " " + time[1],
                                 DateTimeFormat.forPattern("dd.MM.yyyy HH:mm"));
                         lvaDate.setTime(new TimeFrame(startTime, endTime));
                         lvaDate.setRoom(elem.get(3).text());
                         //String roomLink = elem.get(3).select("a").attr("href");
                         lvaDate.setDescription(elem.get(4).text());
                         log.info("Added new Date to LVA " + metaLVA.getName());
                         lecturesList.add(lvaDate);
                     } else {
                         DateTime startTimeDay = DateTime.parse(date[0] + " " + time[0],
                                 DateTimeFormat.forPattern("dd.MM.yyyy HH:mm"));
                         DateTime endTimeDay = DateTime.parse(date[0] + " " + time[1],
                                 DateTimeFormat.forPattern("dd.MM.yyyy HH:mm"));
                         DateTime endTime = DateTime.parse(date[1] + " " + time[1],
                                 DateTimeFormat.forPattern("dd.MM.yyyy HH:mm"));
                         do {
                             LvaDate lvaDate = new LvaDate();
                             lvaDate.setType(LvaDateType.LECTURE);
                             lvaDate.setRoom(elem.get(3).text());
                             lvaDate.setDescription(elem.get(4).text());
                             lvaDate.setTime(new TimeFrame(startTimeDay, endTimeDay));
                             log.info("Added new Date to LVA " + metaLVA.getName());
                             lecturesList.add(lvaDate);
                             startTimeDay = startTimeDay.plusDays(7);
                             endTimeDay = endTimeDay.plusDays(7);
                         } while(startTimeDay.isBefore(endTime));
                     }
                 }
                 lva.setLectures(lecturesList);
             } catch (NullPointerException ex) {
                 log.info("LVA " + metaLVA.getName() + " has no dates");
             }
             try {
                 Elements lvaTestTable = lvaContent.select("h2:contains(Prüfungen)").first().nextElementSibling().select("tbody tr");
                 ArrayList<LvaDate> examList = new ArrayList<>();
                 for(Element e : lvaTestTable) {
                     Elements elem = e.select("td");
                     String[] time = elem.get(1).text().split(" - ");
                     String[] date = elem.get(2).text().split(" - ");
                     //String mode = elem.get(4).text();
                     //String registerDeadline = elem.get(5).text();
                     //String register = elem.get(6).text();
                     LvaDate lvaDate = new LvaDate();
                     lvaDate.setName("Prüfung: " + metaLVA.getType() +" "+ metaLVA.getName());
                     lvaDate.setType(LvaDateType.LECTURE);
                     if(time[0].trim().equals("-"))
                         time = new String[] {"07:00", "20:00"};
                     DateTime startTime = DateTime.parse(date[0] + " " + time[0],
                             DateTimeFormat.forPattern("dd.MM.yyyy HH:mm"));
                     DateTime endTime = DateTime.parse(date[0] + " " + time[1],
                             DateTimeFormat.forPattern("dd.MM.yyyy HH:mm"));
                     lvaDate.setTime(new TimeFrame(startTime, endTime));
                     lvaDate.setRoom(elem.get(3).text());
                     //String roomLink = elem.get(3).select("a").attr("href");
                     lvaDate.setDescription(elem.get(7).text());
 
                     log.info("Added new Date to LVA " + metaLVA.getName());
                     examList.add(lvaDate);
                 }
                 lva.setExams(examList);
             } catch (NullPointerException ex) {
                 log.info("LVA " + metaLVA.getName() + " has no exams");
             }
             log.info("Got LVA: " + lva.toString());
             return metaLVA;
         } catch (IOException e) {
             throw new ServiceException(e);
         }
     }
 }
