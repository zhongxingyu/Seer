 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package org.udhc.gen;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.List;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.commons.fileupload.FileItem;
 import org.apache.commons.fileupload.disk.DiskFileItemFactory;
 import org.apache.commons.fileupload.servlet.ServletFileUpload;
 //ServletFileUpload;
 
 
 
 /**
  *
  * @author root
  */
 public class CatchHealthIssue2 extends HttpServlet {
 
     /**
      * Processes requests for both HTTP
      * <code>GET</code> and
      * <code>POST</code> methods.
      *
      * @param request servlet request
      * @param response servlet response
      * @throws ServletException if a servlet-specific error occurs
      * @throws IOException if an I/O error occurs
      */
     
    
     protected void processRequest(HttpServletRequest request, HttpServletResponse response)
             throws ServletException, IOException {
         
         System.out.println("--------------IN SERVLET-----");
         
         String topic = "";//request.getParameter("topic");
         String problem_details = "";//request.getParameter("problem_details");
         String swid= User.getLoggedInUserEmail(request);
         String patient_name="";
         String isNew="";  // this denotes whether a new patient is to be added.
         
     try {
         List<FileItem> items = new ServletFileUpload(new DiskFileItemFactory()).parseRequest(request);
         
         String fieldname;
         String fieldvalue=""; ;
         InputStream filecontent=null ;
         for (FileItem item : items) {
             if (item.isFormField()) {
                 // Process regular form field (input type="text|radio|checkbox|etc", select, etc).
                 fieldname = item.getFieldName();
                 fieldvalue = item.getString();
                 
                 if( fieldname.equals("isNew"))
                 {
                     isNew=fieldvalue;
                 }
                 
                 if(fieldname.equals("topic"))
                 {
                     topic=fieldvalue;
                 }
                 else if(fieldname.equals("problem_details"))
                 {
                     problem_details=fieldvalue;
                 }
                 
                 else if(fieldname.equals("patient_name"))
                 {
                     patient_name=fieldvalue;                    
                 }
                 
                 System.out.println(fieldname+"....formfield...."+fieldvalue);
                 // ... (do your job here)
             } else {
                 // Process form file field (input type="file").
                //ouur String fieldname = item.getFieldName();
             	
             	/*
             	 *  Currently there's just one file - the patient consent form.
             	 *  TO BE DONE: Support for upload of multiple files/scans
             	 *  during case upload
             	 * 
             	 */
             	
             	
                 String filename = (item.getName());
                 System.out.println("....non-form-field...."+filename);
                 
                 // filecontent is the consent form.
                 
                 filecontent = item.getInputStream();
                 
               
                 
             }
         }
        /*
         *  String sc_name=org.udhc.gen.User.getScientificName();
         *  
         *  The above code is commented out because the patient sci name is recieved as a form parameter now
         *  
         */
         
         
           HealthRecord hr=new HealthRecord(topic,swid,"0",problem_details,filecontent);
           
           String name=org.udhc.gen.User.getLoggedInUserName(request);
           
           // This is an object created to help invoke functions
           
           User u=new User(patient_name,swid);
          
           /*
            The first condition checks whether its a new patient. 
            The second condition checks whether the logged in user is *not* a care-seeker because the rest can add new 
           patients to the database
           * 
           * 
           */
           
           if(isNew.equals("1"))// && !User.getLoggedInUserRole(request).equals("0"))
              
           {
             u.insertSocialWorkerPatient();
           }
           
           String topic_id=hr.insertHealthRecord3(patient_name);
           System.out.println(topic_id);
           //HealthRecord.insertImage(filecontent,Integer.parseInt(fieldvalue));          
           
 
           /*
            * 
            * Sending the email notification.
            * The below process will be put into a utlity class to make sure
            * for sending ouut all notifications.
            * 
            * 
            */
           
            String to[]=User.getModeratorEmails();
           String subject="[NEW HEALTH ISSUE] Patient name : "+ patient_name + " uploaded by " + org.udhc.gen.User.getLoggedInUserName(request);
            String content=" Hello care-givers!, <br>A new health issue has been posted on UDHC : "+topic;
           content+=" <strong> NARRATIVE  </strong>: <br> <br> <br>"+problem_details+"<br> <br> <br>";
             
            topic="[ UDHC ] - "+ patient_name + "uploaded by " + org.udhc.gen.User.getLoggedInUserName(request);
            org.udhc.gen.EmailUtil.sendMail("sbose78", to, subject, content);
             
            response.sendRedirect(request.getContextPath()+"/UPLOADER/care-seeker-input-edit.jsp?topic_id="+topic_id);
            
     }
          
           
          
         
      catch (Exception e) {
         throw new ServletException("Cannot parse multipart request.", e);
     }
 
     // ...
  }
     
   
         
         
         
         
         
         
         
         
         
         
 
     
     
 
     // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
     /**
      * Handles the HTTP
      * <code>GET</code> method.
      *
      * @param request servlet request
      * @param response servlet response
      * @throws ServletException if a servlet-specific error occurs
      * @throws IOException if an I/O error occurs
      */
     @Override
     protected void doGet(HttpServletRequest request, HttpServletResponse response)
             throws ServletException, IOException{
         
         processRequest(request, response);
     }
 
     /**
      * Handles the HTTP
      * <code>POST</code> method.
      *
      * @param request servlet request
      * @param response servlet response
      * @throws ServletException if a servlet-specific error occurs
      * @throws IOException if an I/O error occurs
      */
     @Override
     protected void doPost(HttpServletRequest request, HttpServletResponse response)
             throws  ServletException, IOException{
         processRequest(request, response);
     }
 
     /**
      * Returns a short description of the servlet.
      *
      * @return a String containing servlet description
      */
     @Override
     public String getServletInfo() {
         return "Short description";
     }// </editor-fold>
 }
 
