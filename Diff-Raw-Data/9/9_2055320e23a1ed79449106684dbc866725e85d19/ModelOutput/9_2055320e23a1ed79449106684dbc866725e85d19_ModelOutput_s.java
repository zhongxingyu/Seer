 package org.agmip.translators.apsim;
 
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.StringWriter;
 import java.util.LinkedHashMap;
 import java.util.Map;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 //import java.io.XML;
 import org.agmip.core.types.TranslatorOutput;
 import org.agmip.util.JSONAdapter;
 import org.apache.velocity.VelocityContext;
 import org.apache.velocity.Template;
 import org.apache.velocity.app.Velocity;
 import org.apache.velocity.exception.ResourceNotFoundException;
 import org.apache.velocity.exception.ParseErrorException;
 import org.apache.velocity.exception.MethodInvocationException;
 
 public class ModelOutput implements TranslatorOutput {
     public void writeFile(String filePath, Map input) {
         // Write your file out here.
         
         Velocity.init();
 
         VelocityContext context = new VelocityContext();

        context.put( "WeatherFileName", new String("testlocation") );

         Template template = Velocity.getTemplate("src\\main\\resources\\AgMIPTemplate.apsim");
 
         FileWriter F;
         try {
             F = new FileWriter("Test.apsim");
             template.merge( context, F );
             F.close();
         } catch (IOException ex) {
             Logger.getLogger(ModelOutput.class.getName()).log(Level.SEVERE, null, ex);
         }
         
         
         
        
         
     }
 }
