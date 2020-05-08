 package uk.co.stfo.adriver.integration.form;
 
 public class DynamicFormActionIT extends AbstractFormActionIT {
 
     @Override
     protected String filename() {
         return "form.html";
     }
 
 
     @Override
     protected String folder() {
         return "dynamic";
     }
 
 
     @Override
     protected long timeout() {
         return 1200;
     }
 
 
     @Override
     protected long pollFreq() {
         return 100;
     }
 }
