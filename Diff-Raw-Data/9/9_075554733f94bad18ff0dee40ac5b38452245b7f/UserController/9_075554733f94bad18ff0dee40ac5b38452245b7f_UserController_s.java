 package web;
 
 import ejb.UserBean;
 import entity.Admin;
 import entity.Course;
 import entity.Student;
 import entity.Teacher;
 import entity.User;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.Serializable;
 import java.net.SocketException;
 import java.util.List;
 import java.util.UUID;
 import javax.annotation.PostConstruct;
 import javax.ejb.EJB;
 import javax.enterprise.context.SessionScoped;
 import javax.faces.application.ConfigurableNavigationHandler;
 import javax.faces.application.FacesMessage;
 import javax.faces.context.ExternalContext;
 import javax.faces.context.FacesContext;
 import javax.faces.event.ComponentSystemEvent;
 import javax.inject.Named;
 import org.apache.commons.net.ftp.FTP;
 import org.apache.commons.net.ftp.FTPClient;
 import org.apache.commons.net.ftp.FTPReply;
 import org.primefaces.model.UploadedFile;
 import regular.FTPUpload;
 
 @Named
 @SessionScoped
 public class UserController implements Serializable
 {
     private Student tempStudent;
     private Teacher tempTeacher;
     private User tempUser;
     
     private User loggedInUser;
     private FacesContext context;
     
     private UploadedFile file;  
     
     @EJB
     private UserBean bean;
     
     @PostConstruct
     public void init()
     {
         tempUser = new User();
         tempStudent = new Student();
         tempTeacher = new Teacher();
     }
     
     /**Tries to log in the user with the values entered in the fields
      * @return  If succeeds, returns the succes page. If fails, returns the failed page
      */
     public String login()
     {
         try
         {
             loggedInUser = bean.checkLogin(tempUser.getUsername(), tempUser.getPassword());
             System.out.println("Login succes");
             return "welcome";
             
         }
         catch (Exception e)
         {
             context = FacesContext.getCurrentInstance();
             context.addMessage(null, new FacesMessage("Oh-oh!" ,
                     "Er ging iets mis bij het inloggen :("));
             return null;
         }
     }
     
     public String logout()
     {
         if(getLoggedInUser() != null)
         {
             loggedInUser = null;
             return "login";
         }
         else
         
             return "login/failed";
     }
     
     public boolean isLoggedInUserATeacher()
     {
         return loggedInUser instanceof Teacher;
     }
     
     public boolean isLoggedInUserAStudent()
     {
         return loggedInUser instanceof Student;
     }
     
     public boolean isLoggedInUserAnAdmin()
     {
         return loggedInUser instanceof Admin;
     }
     
         
     /**Tries to register the user with the values entered in the fields
      * @return  If succeeds, returns the succes page. If fails, returns the failed page
      */
     public String registerStudent()
     {   
         try
         {
             uploadPhotoFtp("student");
             bean.addOrUpdate(tempStudent);
              
             context = FacesContext.getCurrentInstance();
             context.addMessage(null, new FacesMessage("Proficiat "+tempStudent.getUsername()+"!" ,
                     "U bent succesvol aangemeld bij VELO!"
                     + " Gelieve uw gegevens goed te bewaren."));
             
             return "login";
         }
         catch (Exception e)
         {
             context = FacesContext.getCurrentInstance();
             context.addMessage(null, new FacesMessage("Oh-oh!" ,
                     "Er ging iets mis bij het registreren :("));
             
             return null;
         }
     }
     public String registerTeacher()
     {
         try
         {
             uploadPhotoFtp("teacher");
             bean.addOrUpdate(tempTeacher);
             
             context = FacesContext.getCurrentInstance();
             context.addMessage(null, new FacesMessage("Proficiat "+ tempTeacher.getUsername()+"!", 
                     "U bent succesvol aangemeld bij VELO!"
                     +" Gelieve uw gegevens goed te bewaren.")); 
 
             return "login";
         }
         catch (Exception e)
         {
             context = FacesContext.getCurrentInstance();
             context.addMessage(null, new FacesMessage("Oh-oh!" ,
                     "Er ging iets mis bij het registreren :("));
             
             return null;
         }
     }
     
     /** Subscribes logged in user to a course
         @param course   the course to which the user must be subscribed
         @return the modified course
     */
     public Course subscribeToCourse(Course course)
     {
         return bean.subscribeToCourse(loggedInUser, course);
     }
     
     
     /** redirect page to login page if user is not logged in **/
     /*public void redirectIfNotLoggedIn(ComponentSystemEvent event)          
     {
         if (loggedInUser == null)
         {
             FacesContext context =  FacesContext.getCurrentInstance();
             ConfigurableNavigationHandler handler = (ConfigurableNavigationHandler) context.getApplication().getNavigationHandler();
             handler.performNavigation("login");
         }
     }*/
     
     /** @return the user who is logged in. If no user is logged in, ?????
      */
     public User getLoggedInUser()
     {
         return loggedInUser;
     }
     
     /** returns all users **/
     public List<User> getAllUsers()
     {
         return bean.getAllUsers();
     }
 
     /** used by the registration & login page **/
     public User getTempUser()
     {
         return tempUser;
     }
     /** used by the registration & login page **/
     public Student getTempStudent()
     {
         return tempStudent;
     }
     /** used by the registration & login page **/
     public Teacher getTempTeacher()
     {
         return tempTeacher;
     }
 
     /** used by the registration & login page **/
     public void setTempUser(User user)
     {
         this.tempUser = user;
     }
     /** used by the registration & login page **/
     public void setTempStudent(Student student)
     {
         this.tempStudent = student;
     }
     /** used by the registration & login page **/
     public void setTempTeacher(Teacher teacher)
     {
         this.tempTeacher = teacher;
     }
   
     public UploadedFile getFile() {  
         return file;  
     }  
   
     public void setFile(UploadedFile file) {  
         this.file = file;  
     }  
   
     /*public void uploadPhoto(String whichUser){  
         if(file != null) 
         {  
             ExternalContext extContext = FacesContext.getCurrentInstance().getExternalContext();
             File result = new File(extContext.getRealPath("/upload/" + file.getFileName()));
 
             try
             {
                 FileOutputStream fileOutputStream = new FileOutputStream(result);
 
                 byte[] buffer = new byte[1024];
 
                 int bulk;
                 InputStream inputStream = file.getInputstream();
                 while (true) {
                     bulk = inputStream.read(buffer);
                     if (bulk < 0) {
                         break;
                     }
                     fileOutputStream.write(buffer, 0, bulk);
                     fileOutputStream.flush();
                 }
 
                 fileOutputStream.close();
                 inputStream.close();
             } 
             catch (IOException e) 
             {
                 FacesMessage error = new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                                "The files were not uploaded!", "");
                 FacesContext.getCurrentInstance().addMessage(null, error);
             }
             
             if(whichUser.equals("student"))
             {
                 System.out.println("STUDENT");
                 tempStudent.setPhoto(file.getFileName());
             }
             if(whichUser.equals("teacher"))
             {
                 System.out.println("TEACHER");
                 tempTeacher.setPhoto(file.getFileName());
             }
             
         }  
     }  */
     
     public void uploadPhotoFtp(String whichUser) throws IOException, SocketException
     {
         String newFileName = FTPUpload.uploadPhotoFtp(file);
         if(whichUser.equals("student"))
         {
             tempStudent.setPhoto(newFileName);
         }
         if(whichUser.equals("teacher"))
         {
             tempTeacher.setPhoto(newFileName);
         }
     }
 }
