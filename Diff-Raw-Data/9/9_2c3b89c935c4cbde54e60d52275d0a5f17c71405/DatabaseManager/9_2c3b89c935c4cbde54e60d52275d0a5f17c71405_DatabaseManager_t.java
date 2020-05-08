 
 package se.slide.timy.db;
 
 import android.content.Context;
 
 import com.j256.ormlite.stmt.QueryBuilder;
 
 import se.slide.timy.model.Category;
 import se.slide.timy.model.Project;
 import se.slide.timy.model.Report;
 
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.List;
 
 public class DatabaseManager {
     static private DatabaseManager instance;
     private DatabaseHelper helper;
 
     static public void init(Context ctx) {
         if (instance == null) {
             instance = new DatabaseManager(ctx);
         }
     }
 
     static public DatabaseManager getInstance() {
         return instance;
     }
 
     private DatabaseManager(Context ctx) {
         helper = new DatabaseHelper(ctx);
     }
 
     private DatabaseHelper getHelper() {
         return helper;
     }
 
     public List<Project> getAllProjects() {
         List<Project> projectLists = null;
         try {
             projectLists = getHelper().getProjectDao().queryForAll();
         } catch (SQLException e) {
             e.printStackTrace();
         }
         return projectLists;
     }
     
     public List<Project> getAllProjects(int categoryId) {
         List<Project> projectLists = null;
         try {
             projectLists = getHelper().getProjectDao().query(getHelper().getProjectDao().queryBuilder().where().eq("belongsToCategoryId", categoryId).and().eq("active", true).prepare());
             //alertLists = getHelper().getAlertDao().query(getHelper().getAlertDao().queryBuilder().where().like("level", "warning").and().ge("timeStamp", timestamp).and().eq("clearedTimesStamp", -1).prepare());
         } catch (SQLException e) {
             e.printStackTrace();
         }
         return projectLists;
     }
     
     public List<Project> getProject(String name) {
         List<Project> projectLists = null;
         try {
             projectLists = getHelper().getProjectDao().query(getHelper().getProjectDao().queryBuilder().where().eq("name", name).and().eq("active", false).prepare());
         } catch (SQLException e) {
             e.printStackTrace();
         }
         return projectLists;
     }
 
     public void addProject(Project f) {
         try {
             getHelper().getProjectDao().createOrUpdate(f);
         } catch (SQLException e) {
             e.printStackTrace();
         }
     }
     
     public void updateProject(Project f) {
         try {
             getHelper().getProjectDao().update(f);
         } catch (SQLException e) {
             e.printStackTrace();
         }
     }
     
     public void deleteProject(Project f) {
         try {
             getHelper().getProjectDao().delete(f);
         } catch (SQLException e) {
             e.printStackTrace();
         }
     }
     
     public List<Category> getAllCategories() {
         List<Category> categoryLists = null;
         try {
             categoryLists = getHelper().getCategoryDao().queryForAll();
         } catch (SQLException e) {
             e.printStackTrace();
         }
         return categoryLists;
     }
     
     public List<Category> getAllActiveCategories() {
         List<Category> categoryLists = null;
         try {
             categoryLists = getHelper().getCategoryDao().query(getHelper().getCategoryDao().queryBuilder().where().eq("active", true).prepare());
         } catch (SQLException e) {
             e.printStackTrace();
         }
         return categoryLists;
     }
 
     public void addCategory(Category f) {
         try {
             getHelper().getCategoryDao().create(f);
         } catch (SQLException e) {
             e.printStackTrace();
         }
     }
     
     public void updateCategory(Category f) {
         try {
             getHelper().getCategoryDao().update(f);
         } catch (SQLException e) {
             e.printStackTrace();
         }
     }
     
     public void deleteCategory(Category f) {
         try {
             getHelper().getCategoryDao().delete(f);
         } catch (SQLException e) {
             e.printStackTrace();
         }
     }
     
     public List<Report> getAllReports() {
         List<Report> reportLists = null;
         try {
             reportLists = getHelper().getReportDao().queryForAll();
         } catch (SQLException e) {
             e.printStackTrace();
         }
         return reportLists;
     }
     
     public List<Project> getProjectsWithUnsyncedReports() {
         List<Project> projects = null;
         try {
             List<Report> reports = getHelper().getReportDao().queryBuilder().where().eq("googleCalendarSync", false).query();
             
             List<Integer> reportIds = new ArrayList<Integer>();
             for (int i = 0; i < reports.size(); i++) {
                reportIds.add(reports.get(i).getProjectId());
             }
             
             QueryBuilder<Project, Integer> builderProject = getHelper().getProjectDao().queryBuilder();
             builderProject.where().in("id", reportIds);
             
             projects = builderProject.query();
             
             for (int a = 0; a < projects.size(); a++) {
                 Project project = projects.get(a);
                 
                 for (int b = 0; b < reports.size(); b++) {
                     Report report = reports.get(b);
                     
                     if (report.getProjectId() == project.getId())
                         project.addReport(report);
                 }
             }
             
         } catch (SQLException e) {
             e.printStackTrace();
         }
         return projects;
     }
     
     public List<Report> getAllReports(int projectId) {
         List<Report> reportLists = null;
         try {
             reportLists = getHelper().getReportDao().query(getHelper().getReportDao().queryBuilder().where().eq("projectId", projectId).prepare());
         } catch (SQLException e) {
             e.printStackTrace();
         }
         return reportLists;
     }
     
     public boolean haveUnsyncedReports() {
         List<Report> reports = null;
         try {
             reports = getHelper().getReportDao().query(getHelper().getReportDao().queryBuilder().where().eq("googleCalendarSync", false).prepare());
         } catch (SQLException e) {
             e.printStackTrace();
         }
         
         if (reports == null || reports.size() < 1)
             return false;
         else
             return true;
         
     }
     
     public List<Report> getReport(int projectId, Date date) {
         List<Report> reports = null;
         try {
             Calendar cal = Calendar.getInstance();
             cal.setTime(date);
             cal.set(Calendar.HOUR_OF_DAY, 0);
             cal.set(Calendar.MINUTE, 0);
             cal.set(Calendar.MILLISECOND, 0);
             
             Date startDay = cal.getTime();
             
             cal.set(Calendar.HOUR_OF_DAY, 23);
             cal.set(Calendar.MINUTE, 59);
             cal.set(Calendar.MILLISECOND, 999);
             
             Date endDate = cal.getTime();
             
             reports = getHelper().getReportDao().query(getHelper().getReportDao().queryBuilder().where().ge("date", startDay).and().le("date", endDate).and().eq("projectId", projectId) .prepare());
         } catch (SQLException e) {
             e.printStackTrace();
         }
         return reports;
     }
 
     public void addReport(Report f) {
         try {
             getHelper().getReportDao().create(f);
         } catch (SQLException e) {
             e.printStackTrace();
         }
     }
     
     public void addOrUpdateReport(Report f) {
         try {
             getHelper().getReportDao().createOrUpdate(f);
         } catch (SQLException e) {
             e.printStackTrace();
         }
     }
     
     public void updateReport(Report f) {
         try {
             getHelper().getReportDao().update(f);
         } catch (SQLException e) {
             e.printStackTrace();
         }
     }
     
     public void deleteReport(Report f) {
         try {
             getHelper().getReportDao().delete(f);
         } catch (SQLException e) {
             e.printStackTrace();
         }
     }
 
 }
