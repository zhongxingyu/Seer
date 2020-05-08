 package com.ibm.opensocial.landos.email;
 
 import java.text.DateFormat;
 import java.util.Date;
 import java.util.Scanner;
 
 import javax.el.ExpressionFactory;
 
 import de.odysseus.el.util.SimpleContext;
 
 public class EmailRenderer {
   
   private final ExpressionFactory factory;
   private final SimpleContext context;
   
  public EmailRenderer(Integer id, long start, long end, boolean isTest) {
     factory = ExpressionFactory.newInstance();
     context = new SimpleContext();
     
    context.setVariable("id", factory.createValueExpression(id, Integer.class));
     context.setVariable("start", factory.createValueExpression(new Date(start), Date.class));
     context.setVariable("end", factory.createValueExpression(new Date(end), Date.class));
     context.setVariable("isTest", factory.createValueExpression(isTest, boolean.class));
     context.setVariable("dFormat", factory.createValueExpression(DateFormat.getDateInstance(DateFormat.FULL), DateFormat.class));
     context.setVariable("tFormat", factory.createValueExpression(DateFormat.getTimeInstance(), DateFormat.class));
     context.setVariable("dtFormat", factory.createValueExpression(DateFormat.getDateTimeInstance(), DateFormat.class));
     context.setVariable("subject", factory.createValueExpression(getEmailSubject(), String.class));
   }
   
   public String renderHtmlEmail() {
     return render(getTemplate("email.html"));
   }
   
   public String renderTextEmail() {
     return render(getTemplate("email.txt"));
   }
   
   public String getEmailSubject() {
     return render(getTemplate("subject.txt"));
   }
  
   private String getTemplate(String template) {
     return new Scanner(EmailRenderer.class.getResourceAsStream(template), "UTF-8")
         .useDelimiter("\\z").next(); 
   }
   
   private String render(String template) {
     return (String) factory.createValueExpression(context, template, String.class).getValue(context);
   }
  
 }
 
