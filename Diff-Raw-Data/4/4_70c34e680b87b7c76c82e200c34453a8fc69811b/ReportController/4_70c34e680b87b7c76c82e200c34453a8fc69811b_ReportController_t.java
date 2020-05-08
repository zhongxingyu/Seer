 package com.freeroom.projectci.beans;
 
 import com.freeroom.di.annotations.Bean;
 import com.freeroom.di.annotations.Inject;
 import com.freeroom.web.Model;
 
 import java.util.Date;
 
 import static com.freeroom.web.Model.render;
 
 @Bean
 public class ReportController {
 
     @Inject
     private ReportService reportService;
 
     @Inject
     private HomeController homeController;
 
     public Model create(final String httpMethod, final TimeReport report)
     {
         if ("POST".equals(httpMethod)) {
             reportService.addReport(report);
         }
         return homeController.index();
     }
 
     public Model utilityData()
     {
         return render("text").put("content", reportService.utilityData());
     }
 
     public Model timestamp()
     {
        return render("text").put("content", String.valueOf(new Date().getTime()));
     }
 }
