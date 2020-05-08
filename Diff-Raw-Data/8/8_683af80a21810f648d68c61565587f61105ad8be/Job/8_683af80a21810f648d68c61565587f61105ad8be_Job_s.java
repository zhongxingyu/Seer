 package itp.team1.jobseekerserver;
 
 import java.io.Serializable;
 
 /**
  * POJO Entity class/bean POD, representing a "Job" listing.
  * @author Calum
  */
 public class Job implements Serializable
 {
     private int id;
     
     private String description;
     private String url;
     private String source;
     private long   timestamp;  
     private String employer;
     private String title;
     private String city;
     
    private double salary;
     private double latitude;
     private double longitude;
     private String openingDate;
     private String pageName;
     private String closingDate;
     private String hours;
     private String industry;
     private String type;
     
     // -----Getters & Setters------
 
     public Job()
     {
     }
     
     public int getId() {
         return id;
     }
     
     public void setId(int id) {
         this.id = id;
     }
     
     public String getEmployer() {
         return employer;
     }
 
     public void setEmployer(String employer) {
         this.employer = employer;
     }
 
     public String getTitle() {
         return title;
     }
 
     public void setTitle(String title) {
         this.title = title;
     }
 
     public String getCity() {
         return city;
     }
 
     public void setCity(String location) {
         this.city = location;
     }
 
     public String getOpeningDate() {
         return openingDate;
     }
 
     public void setOpeningDate(String openingDate) {
         this.openingDate = openingDate;
     }
 
     public String getDescription() {
         if (description == null || description.equals(""))
             return title + " - " + employer;
         return description;
     }
 
     public void setDescription(String description) {
         this.description = description;
     }
 
     public String getURL() {
         return url;
     }
 
     public void setURL(String externalLink) {
         this.url = externalLink;
     }
 
     public String getSource() {
         return source;
     }
 
     public void setSource(String source) {
         this.source = source;
     }
 
     public long getTimestamp() {
         return timestamp;
     }
     
     public void setTimestamp(long timestamp) {
         this.timestamp = timestamp;
     }
     
     public double getLatitude() {
         return latitude;
     }
 
     public void setLatitude(double latitude) {
         this.latitude = latitude;
     }
 
     public double getLongitude() {
         return longitude;
     }
 
     public void setLongitude(double longitude) {
         this.longitude = longitude;
     }
 
     public String getPageName() {
         return pageName;
     }
 
     public void setPageName(String pageName) {
         this.pageName = pageName;
     }
 
     public String getClosingDate() {
         return closingDate;
     }
 
     public void setClosingDate(String closingDate) {
         this.closingDate = closingDate;
     }
 
     public String getHours() {
         return hours;
     }
 
     public void setHours(String hours) {
         this.hours = hours;
     }
 
     public String getIndustry() {
         return industry;
     }
 
     public void setIndustry(String industry) {
         this.industry = industry;
     }
 
     public String getType() {
         return type;
     }
 
     public void setType(String type) {
         this.type = type;
     }
 
    public double getSalary() {
         return salary;
     }
 
    public void setSalary(double salary) {
         this.salary = salary;
     }
     
     
     
 }
