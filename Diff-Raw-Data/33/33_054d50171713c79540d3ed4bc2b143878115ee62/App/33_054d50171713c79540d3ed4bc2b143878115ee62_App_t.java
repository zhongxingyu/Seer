 package com.salil.example.springquartz;
 
import org.quartz.Scheduler;
import org.springframework.context.ApplicationContext;
 import org.springframework.context.support.ClassPathXmlApplicationContext;
 
import java.beans.beancontext.BeanContext;


 public class App {
 	public static void main(String[] args) throws Exception {
        ApplicationContext context = new ClassPathXmlApplicationContext("Spring-Quartz.xml");
 
        Scheduler scheduler = (Scheduler) context.getBean("schedulerFactoryBean");
        scheduler.shutdown(true);
 	}
 }
