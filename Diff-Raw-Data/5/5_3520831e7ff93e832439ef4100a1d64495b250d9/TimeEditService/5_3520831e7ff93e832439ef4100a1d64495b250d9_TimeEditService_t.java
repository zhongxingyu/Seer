 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package no.hials.muldvarpweb.service;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import javax.ejb.Stateless;
 import javax.ws.rs.GET;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
 import javax.ws.rs.core.MediaType;
 import org.jsoup.Jsoup;
 import org.jsoup.nodes.Document;
 import org.jsoup.nodes.Element;
 import org.jsoup.select.Elements;
 
 /**
  *
  * @author kb
  */
 @Stateless
 @Path("timeedit")
 public class TimeEditService {
     /**
      * TimeEdit info:
      * dateformat: yyyymmdd
      * classcodeformat: 183000
      */
     
     static String host = "http://timeedit.hials.no/4DACTION/";
     static String currentActivityPage = "WebShowRoll/1-7?offset=1440&update=0&rows=0&page=0&branch=2&group=-7&day=yes&start=yes&stop=yes&order=ascending&web_cols=1&web_numChars=-";
     
     /**
      * Gets class or course schedule from TimeEdit using the internal TimeEdit id for the class/course
      * @param id
      * @return 
      */
     @GET
     @Path("{id}")
     @Produces({MediaType.APPLICATION_JSON})
    public List<Day> getByClassOrCourseId(@PathParam("id") String id) {
         Document doc = null;
         try {
             doc = Jsoup.connect(getClassScheduleUrlByClasscode(getCurrentDate(), id)).get();
         } catch (IOException ex) {
             System.out.println(ex.getMessage());
         }
         Element content = doc.getElementsByClass("booking").first();
         Elements rows = content.getElementsByTag("tr");
         List<Day> dager = new ArrayList<Day>();
         Day d = null;
         Course t = null;
         for(Element row : rows) {
             for(int i = 0; i < row.getElementsByTag("td").size(); i++) {
                 try {
                     String data = row.getElementsByTag("td").get(i).getElementsByTag("font").first().text();
                     if(data.contains("Uke")) { // Skreller vekk uke radene
                         break;
                     } else if(data.matches(".*\\w.*")) { // Bruker bare kolonnene med innhold
                         switch(i) {
                             case 2:
                                 d = new Day(data);
                                 dager.add(d);
                                 break;
                             case 3:
                                 d.date = data;
                                 break;
                             case 4:
                                 t = new Course(data);
                                 break;
                             case 5:
                                 t.course = data;
                                 break;
                             case 6:
                                 t.type = data;
                                 break;
                             case 7:
                                 t.mClass = data;
                                 break;
                             case 8:
                                 t.teacher = data;
                                 break;
                             case 9:
                                 t.room = data;
                                 d.courses.add(t);
                                 break;
                         }
                     }
                 } catch(NullPointerException ex) {
                 }
             }
         }
         return dager;
     }
     
     public static String getClassScheduleUrlByClasscode(String date, String classcode) {
         String retVal = host + "WebShowSearch/1/1-0?wv_ts=" + date + "&wv_obj1=" + classcode + "&wv_text=Tekstformat";
         System.out.println("Getting url: " + retVal);
         return retVal;
     }
     
     /**
      * Gets current date in yyyymmdd format
      * @return String
      */
     public static String getCurrentDate() {
         Date d = new Date();
         String year = "" + (d.getYear() + 1900);
         String month = "";
        if((d.getMonth()+1) < 10) {
             month += "0";
         }
         month += (d.getMonth() + 1);
         String retVal = "" + year + month + d.getDate();
         System.out.println("Getting date " + retVal);
         return retVal;
     }
     
     public static class Day {
         String day;
         String date;
         List<Course> courses = new ArrayList<Course>();
 
         private Day(String dag) {
             this.day = dag;
         }
 
         public String getDay() {
             return day;
         }
 
         public void setDay(String day) {
             this.day = day;
         }
 
         public String getDate() {
             return date;
         }
 
         public void setDate(String date) {
             this.date = date;
         }
 
         public List<Course> getCourses() {
             return courses;
         }
 
         public void setCourses(List<Course> courses) {
             this.courses = courses;
         }
     }
     
     public static class Course {
         String time;
         String course;
         String type;
         String mClass;
         String teacher;
         String room;
 
         private Course(String tid) {
             this.time = tid;
         }
 
         public String getTime() {
             return time;
         }
 
         public void setTime(String time) {
             this.time = time;
         }
 
         public String getCourse() {
             return course;
         }
 
         public void setCourse(String course) {
             this.course = course;
         }
 
         public String getType() {
             return type;
         }
 
         public void setType(String type) {
             this.type = type;
         }
 
         public String getmClass() {
             return mClass;
         }
 
         public void setmClass(String mClass) {
             this.mClass = mClass;
         }
 
         public String getTeacher() {
             return teacher;
         }
 
         public void setTeacher(String teacher) {
             this.teacher = teacher;
         }
 
         public String getRoom() {
             return room;
         }
 
         public void setRoom(String room) {
             this.room = room;
         }
     }
 }
