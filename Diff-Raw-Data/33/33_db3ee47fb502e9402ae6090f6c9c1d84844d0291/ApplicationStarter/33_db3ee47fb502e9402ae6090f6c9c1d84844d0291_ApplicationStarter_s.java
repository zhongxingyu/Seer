 package org.test.bugtracker.starter;
 
 import org.springframework.context.ApplicationContext;
 import org.springframework.context.support.ClassPathXmlApplicationContext;
 import org.test.bugtracker.bo.UserBO;
 import org.test.bugtracker.impl.model.UserImpl;
 import org.test.bugtracker.model.User;
 
 public class ApplicationStarter {
     private ApplicationContext context;
 
     public ApplicationStarter(ApplicationContext context) {
         this.context = context;
     }
 
     public void start() {
         UserBO userBO = context.getBean(UserBO.class);
         User user = new UserImpl();
         user.setLogin("login");
         user.setPass("pass");
         userBO.save(user);
     }
 
     public static void main(String[] args) {
         new ApplicationStarter(new ClassPathXmlApplicationContext(
                 "spring/context.xml")).start();
     }
 }
