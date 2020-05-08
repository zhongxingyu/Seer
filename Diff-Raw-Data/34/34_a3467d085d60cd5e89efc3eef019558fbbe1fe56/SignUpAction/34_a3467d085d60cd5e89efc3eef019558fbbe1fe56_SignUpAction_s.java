 package com.nclodger.control.action.access;
 
 import com.nclodger.additional.MD5Value;
 import com.nclodger.control.action.Action;
 import com.nclodger.domain.ConfirmationEmail;
 import com.nclodger.dao.UserDao;
 import com.nclodger.domain.User;
 import com.nclodger.dao.ConfirmationEmailDAO;
 import com.nclodger.mail.EmailNotification;
 import com.nclodger.mail.MailConfirmation;
 import com.nclodger.myexception.MyException;
 import org.springframework.context.ApplicationContext;
 import org.springframework.context.support.ClassPathXmlApplicationContext;
 //import org.springframework.context.annotation.Bean;
 
 //import javax.inject.Inject;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 /**
  * created with intellij idea.
  * user: iaroslav
  * date: 21.10.13
  * time: 21:29
  */
 
 public class SignUpAction extends Action {
     UserDao users;
     @Override
     public String execute(HttpServletRequest request, HttpServletResponse response) throws MyException {
         // If user is logged in then he can't sign up
        if(request.getSession().getAttribute("email") != null){
             return "home";
         }
 
         //ApplicationContext ctx = new ClassPathXmlApplicationContext("bean-config.xml");
         UserDao users = (UserDao) ctx.getBean("userDAO");
 
         if(users.isExistEmail(request.getParameter("email"))){
             request.setAttribute("isExist",true);
             return "registration";
         }
         boolean bool;
         User user;
         try {
             user = new User(request.getParameter("email"),request.getParameter("password"),request.getParameter("username"),0);
             bool = users.insert(user);
             User user_stored = users.getUserObj(user.getEmail(),user.getPswd());
             ConfirmationEmailDAO ConEmail =(ConfirmationEmailDAO) ctx.getBean("conemailDAO");
 
             //get hash
             MD5Value MD = (MD5Value) ctx.getBean("md5value");
             String hash = MD.getmd5value(user.getEmail()+"."+user.getPswd());
             ConEmail.insert(new ConfirmationEmail(user_stored.getId(),hash));
             //send mail
             EmailNotification mailconfirm =new EmailNotification();
             mailconfirm.sendConfirmation(user.getEmail(),"http://"+request.getLocalAddr()+":8080/NCLodger/confirmation?param="+hash);
         } catch (MyException ex) {
             request.setAttribute("error_message",ex.getMessage());
             return "exception";
         }
         request.setAttribute("email",request.getParameter("email"));
         request.setAttribute("username",request.getParameter("username"));
         return "signup_succeed";
     }
 
 }
