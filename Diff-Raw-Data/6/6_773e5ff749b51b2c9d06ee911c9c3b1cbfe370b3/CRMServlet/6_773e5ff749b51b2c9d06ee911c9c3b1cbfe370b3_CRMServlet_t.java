 package servlet;
 
 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 import CRMS.entity.MemberEntity;
 import CRMS.session.FeedbackSessionBean;
 import CRMS.session.MemberManagementSessionBean;
 import FBMS.entity.RestaurantEntity;
 import FBMS.session.IndReservationSessionBeanRemote;
 import CRMS.session.MemberSessionBean;
 import java.io.IOException;
 import java.text.DateFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.Set;
 import javax.ejb.EJB;
 import javax.servlet.RequestDispatcher;
 import javax.servlet.ServletContext;
 import javax.servlet.ServletException;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 /**
  *
  * @author lionetdd
  */
 @WebServlet(urlPatterns = {"/CRMServlet", "/CRMServlet/*"})
 public class CRMServlet extends HttpServlet {
 
     @EJB
     private FeedbackSessionBean feedbackSessionBean;
     @EJB
     private MemberManagementSessionBean memberManagementSessionBean;
     @EJB
     private MemberSessionBean memberSession;
     @EJB
     private IndReservationSessionBeanRemote indReservationSessionBean;
     //private Set<RestaurantEntity> data = null;
     private String message = null;
     private MemberEntity member;
     private MemberEntity data;
     private String data2;
 
     //private String keyword=null;
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
     @Override
     public void init() {
         System.out.println("CRMSERVLET: init()");
     }
 
     protected void processRequest(HttpServletRequest request, HttpServletResponse response)
             throws ServletException, IOException {
         System.out.println("CRMSERVLET: processRequest()");
         HttpSession session = request.getSession(true);
 
         /* response.setContentType("text/html;charset=UTF-8");
          PrintWriter out = response.getWriter();*/
         try {
             RequestDispatcher dispatcher;
             ServletContext servletContext = getServletContext();
 
             String temp = request.getServletPath();
 
             String page = request.getPathInfo();
             page = page.substring(1);
             System.out.println("CRMSServlet page: " + page);
 
 
             if ("member".equals(page)) {
                 System.out.println("***member page***");
 
                 request.getRequestDispatcher("/member.jsp").forward(request, response);
 
             } else if ("memberFeedback".equals(page)) {
                 System.out.println("***member feedback page***");
                 request.getRequestDispatcher("/memberFeedback.jsp").forward(request, response);
 
             } else if ("memberFeedbackResult".equals(page)) {
                 System.out.println("***member Feedback Result page***");
                 String memberEmail = (String) session.getAttribute("memberEmail");
                 addFeedback(request, memberEmail);
                 System.out.println("CRMServlet:Going to next page");
                 request.getRequestDispatcher("/memberFeedbackResult.jsp").forward(request, response);
             } else if ("memberPromotion".equals(page)) {
                 System.out.println("***member promotion page***");
 
                 request.getRequestDispatcher("/memberPromotion.jsp").forward(request, response);
 
             } else if ("memberInfo".equals(page)) {
 
 
                 System.out.println(request.getParameter("email"));
                 System.out.println(request.getParameter("password"));
 
                 String email = request.getParameter("email");
 
                 if (email == null) {
                     request.getRequestDispatcher("/accessDenied.jsp").forward(request, response);
                 } else {
                     System.out.println("email is not null");
                     String loginStatus = request.getParameter("loginStatus");
 
                     if (loginStatus.equals("true")) {
                         System.out.println("has logged in before");
                         member = memberSession.getMemberByEmail(email);
                         System.out.println(member.getMemberName());
                         request.setAttribute("data", member);
                         request.getRequestDispatcher("/memberInfo.jsp").forward(request, response);
                     } else {
                         String password = request.getParameter("password");
                         System.out.println(email);
                         System.out.println(password);
 
                         boolean isLogin = memberManagementSessionBean.login(email, password);
                         if (isLogin) {
                             System.out.println(isLogin);
                             member = memberSession.getMemberByEmail(email);
                             System.out.println(member.getMemberName());
                             session.setAttribute("member", member);
                             session.setAttribute("memberEmail", email);
                             request.setAttribute("data", member);
                             request.setAttribute("memberEmail", member.getMemberEmail());
                             request.setAttribute("loginStatus", "true");
                             request.getRequestDispatcher("/memberInfo.jsp").forward(request, response);
                         } else {
                             message = "Wrong password or username entered";
                             request.setAttribute("message", message);
                             request.getRequestDispatcher("/member.jsp").forward(request, response);
 
                         }
                     }
                 }
 
                 /*member=memberSession.getMemberByEmail(email);
              
                  System.out.println("CRMSServlet: member has been returned");
                  request.setAttribute("data", member);
 
 
                  request.getRequestDispatcher("/memberInfo.jsp").forward(request, response);*/
 
 
             } else if ("resetMemberPassword".equals(page)) {
                 System.out.println("***resetMemberPassword page***");
 
                 String email = request.getParameter("email");
                 System.out.println("email get from request: " + email);
 
                 if (email == null) {
                     request.getRequestDispatcher("/accessDenied.jsp").forward(request, response);
                 } else {
                     member = memberSession.getMemberByEmail(email);
                     request.setAttribute("data", member);
 
                     request.getRequestDispatcher("/memberInfo.jsp").forward(request, response);
                 }
 
             } else if ("resetMemberPasswordConfirmation".equals(page)) {
                 System.out.println("***resetMemberPasswordConfirmation page***");
 
 
 
                 String email = request.getParameter("email");
 
                 if (email == null) {
                     request.getRequestDispatcher("/accessDenied.jsp").forward(request, response);
                 } else {
                     String oldPassword = request.getParameter("oldPwd");
                     String newPassword1 = request.getParameter("newPwd1");
                     String newPassword2 = request.getParameter("newPwd2");
 
                     Boolean correctOldPwd = memberManagementSessionBean.checkPassword(email, oldPassword);
                     if (!correctOldPwd) {
                         message = "wrong password";
                         request.setAttribute("message", message);
                         request.getRequestDispatcher("/memberInfo.jsp").forward(request, response);
                     } else {
                         memberManagementSessionBean.resetPasswordWithNewPassword(email, newPassword1);
                         System.out.println("password saved.");
 
                         request.getRequestDispatcher("/resetMemberPasswordConfirmation.jsp").forward(request, response);
                     }
                 }
 
 
             } else if ("memberInfoEditionConfirmation".equals(page)) {
                 System.out.println("***memberInfoEdictionConfirmation page***");
 
                 String email = request.getParameter("email");
                 System.out.println(email);
 
                 if (email == null) {
                     request.getRequestDispatcher("/accessDenied.jsp").forward(request, response);
                 } else {
                     String userName = request.getParameter("userName");
                     System.out.println(userName);
 
                     String password = request.getParameter("password");
                     System.out.println("captured password: " + password);
                     String mobile = request.getParameter("mobile");
                     System.out.println(mobile);
                     String nationality = request.getParameter("nationality");
                     System.out.println(nationality);
                     Integer day = Integer.parseInt(request.getParameter("date"));
                     System.out.println(day);
                     Integer month = Integer.parseInt(request.getParameter("month"));
                     System.out.println(month);
                     Integer year = Integer.parseInt(request.getParameter("year"));
                     System.out.println(year);
                     String maritalStatus = request.getParameter("maritalStatus");
                     System.out.println(maritalStatus);
                     String gender = request.getParameter("gender");
                     System.out.println(gender);
                     Boolean subscribe = Boolean.valueOf(request.getParameter("subscribe"));
                     System.out.println(Boolean.valueOf(subscribe));
                     String securityQuestion = request.getParameter("securityQuestion");
                     String answer = request.getParameter("answer");
 
                     //    String this_date = day + "-" + month + "-" + year;
                     //    System.out.println(this_date);
                     Date date;
                     date = new Date(year - 1900, month - 1, day);
                     //    Date date = new SimpleDateFormat("dd-MMM-yyyy").parse("01-July-2013");
                     //    System.out.println(date);
 
                     /*    if (subscribe) {
                      System.out.println("the member has subscribed");
                      message="You have subscribed";
                      request.setAttribute("message",message);
                      } else {
                      message="You have not subscribed.";
                      request.setAttribute("message",message);                
                      }
                 
                      System.out.println("message, "+message);*/
 
                     member = memberSession.updateMember(email, userName, mobile, date, maritalStatus, gender);
                     System.out.println("member email before setAttribute" + member.getMemberEmail());
                     System.out.println("member subscriber? :" + Boolean.toString(member.isSubscriber()));
                     request.setAttribute("data", member);
                     request.setAttribute("data2", "true");
 
                     request.getRequestDispatcher("/memberInfoEditionConfirmation.jsp").forward(request, response);
                 }
 
             } else if ("memberRegister".equals(page)) {
                 System.out.println("***memberRegister page***");
                 request.getRequestDispatcher("/memberRegister.jsp").forward(request, response);
 
             } else if ("memberRegisterResult".equals(page)) {
 
                 System.out.println("***memberRegisterResult page***");
 
                 String email = request.getParameter("e-mail");
                 if (email == null) {
                     request.getRequestDispatcher("/accessDenied.jsp").forward(request, response);
                 } else {
 
                     String userName = request.getParameter("username");
 
                     String password1 = request.getParameter("password");
                     String password2 = request.getParameter("password2");
                     String mobile = request.getParameter("mobile");
                     String nationality = request.getParameter("nationality");
                     Integer day = Integer.parseInt(request.getParameter("dateDay"));
                     Integer month = Integer.parseInt(request.getParameter("dateMonth"));
                     Integer year = Integer.parseInt(request.getParameter("dateYear"));
                     String maritalStatus = request.getParameter("marital");
                     String gender = request.getParameter("gender");
                     Boolean subscribe = Boolean.valueOf(request.getParameter("subscribe"));
 
                     //     String this_date = day + "-" + month + "-" + year;
                     //     System.out.println(this_date);
                     Date date;
                     date = new Date(year - 1900, month - 1, day);
                     //             Date date = new SimpleDateFormat("dd-MMM-yyyy").parse("01-July-2013");
                     //             System.out.println(date);
                     String securityQuestion = request.getParameter("securityQuestion");
                     String answer = request.getParameter("answer");
                     System.out.println("userName: " + userName);
 
                     member = memberSession.addMember(email, userName, password1, password2, mobile, gender, nationality, date, maritalStatus, subscribe,
                             securityQuestion, answer);
                     request.setAttribute("data", member);
 
                     request.getRequestDispatcher("/memberRegisterResult.jsp").forward(request, response);
                 }
 
             } else if ("memberForgetPassword".equals(page)) {
                 System.out.println("***memberForgetPassword page***");
 
                 request.getRequestDispatcher("/memberForgetPassword.jsp").forward(request, response);
 
             } else if ("memberForgetPasswordResult".equals(page)) {
                 System.out.println("***memberForgetPasswordResult***");
 
                 String email = request.getParameter("email");
                 System.out.println("email: " + email);
 
 
                 if (email == null) {
                     request.getRequestDispatcher("/accessDenied.jsp").forward(request, response);
                 } else if (memberSession.getMemberByEmail(email) == null) {
                     System.out.println("invalid email: " + email);
                     message = "This email is not registered yet.";
                     request.setAttribute("message", message);
                     request.getRequestDispatcher("/memberForgetPassword.jsp").forward(request, response);
                 } else {
                     String question = request.getParameter("question");
                     String answer = request.getParameter("answer");
                     boolean correctAnswer = memberManagementSessionBean.checkAnswer(email, answer);
                     if (correctAnswer) {
                         memberManagementSessionBean.ResetPassword(email);
                         request.getRequestDispatcher("/memberForgetPasswordResult.jsp").forward(request, response);
                     } else {
                         System.out.println("wrong answer");
                         message = "Your question or answer is not correct";
                         request.setAttribute("message", message);
                         request.getRequestDispatcher("/memberForgetPassword.jsp").forward(request, response);
                     }
                 }
 
                 //       memberManagementSessionBean.ResetPassword("leijq369@gmail.com");
 
             } else if ("accessDenied".equals(page)) {
                 System.out.println("***accessDenied***");
                 request.getRequestDispatcher("/accessDenied.jsp").forward(request, response);
             } else if ("logOut".equals(page)) {
                 System.out.println("***logOut***");
                 request.getRequestDispatcher("/logOut.jsp").forward(request, response);
             } else {
                 System.out.println("other page");
             }
 //          
 //             
 //            dispatcher=servletContext.getNamedDispatcher(page);
 //            System.out.println("dispatcher set up");
 //            System.out.println(dispatcher);
 //            if(dispatcher==null){
 //                dispatcher=servletContext.getNamedDispatcher("Error");
 //            }
 //            System.out.println("Before push content");
 //            dispatcher.forward(request, response);
 //            System.out.println("After push content");
 
 
 
         } catch (Exception e) {
             System.out.println(e);
             log("Exception in CRMServlet.processRequest()");
             //System.out.println(e);
         }
     }
 
     /* TODO output your page here. You may use following sample code. */
     /* TODO output your page here. You may use following sample code. */
 
     /*
      out.println("<!DOCTYPE html>");
      out.println("<html>");
      out.println("<head>");
      out.println("<title>Servlet irmsServlet</title>");            
      out.println("</head>");
      out.println("<body>");
      out.println("<h1>Servlet irmsServlet at " + request.getContextPath() + "</h1>");
      out.println("</body>");
      out.println("</html>");
      * */
     /*} finally {            
      out.close();
      }*/
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
     /**
      * Handles the HTTP
      * <code>POST</code> method.
      *
      * @param request servlet request
      * @param response servlet response
      * @throws ServletException if a servlet-specific error occurs
      * @throws IOException if an I/O error occurs
      */
     /**
      * Returns a short description of the servlet.
      *
      * @return a String containing servlet description
      */
     @Override
     public String getServletInfo() {
         return "Short description";
     }// </editor-fold>
 
     @Override
     protected void doGet(HttpServletRequest request, HttpServletResponse response)
             throws ServletException, IOException {
         processRequest(request, response);
     }
 
     @Override
     protected void doPost(HttpServletRequest request, HttpServletResponse response)
             throws ServletException, IOException {
         processRequest(request, response);
     }
 
     @Override
     public void destroy() {
         System.out.println("CRMServlet: destroy()");
     }
 
     private void addFeedback(HttpServletRequest request, String memberEmail) {
        System.out.println(memberEmail);
         String content = request.getParameter("content");
         String title = request.getParameter("title");
         Integer rating = Integer.parseInt(request.getParameter("rating"));
 
         String department = request.getParameter("department");
         Date currentDate = new Date();
        System.out.println(memberEmail);
         feedbackSessionBean.createFeedback(content, title, memberEmail, department, currentDate, rating);
         System.out.println("CRMServlet:addFeedback:feedback has been added!");
     }
 }
