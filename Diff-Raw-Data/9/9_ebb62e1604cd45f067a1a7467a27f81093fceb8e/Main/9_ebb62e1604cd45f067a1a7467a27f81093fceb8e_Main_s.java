 package core;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.IOException;
 import java.io.PrintStream;
 import java.io.StringReader;
 import java.io.StringWriter;
 import java.lang.reflect.Constructor;
 import java.util.Properties;
 
 import org.apache.velocity.Template;
 import org.apache.velocity.VelocityContext;
 import org.apache.velocity.app.Velocity;
 
 public class Main {
   public static void main(String[] args) {
 
     String prologFileName = null;
     String templateFileName = null;
     String contextGenClassName = null;
 
     if (args.length == 2) {
       prologFileName = args[0];
       templateFileName = args[1];
     } else if (args.length == 4) {
       if (args[0].equals("-cg")) {
         contextGenClassName = args[1];
       } else {
         System.out
             .println("Usage: [-cg ContextGeneratorClass] prolog-file template-file");
         System.exit(0);
       }
       prologFileName = args[2];
       templateFileName = args[3];
     } else {
       System.out
           .println("Usage: [-cg ContextGeneratorClass] prolog-file template-file");
       System.exit(0);
     }
 
     try {
 
       File prologFile = new File(prologFileName);
       File templateFile = new File(templateFileName);
 
       if (!prologFile.exists()) {
         System.out.println("prolog-file does not exist.");
         System.exit(1);
       }
 
       if (!templateFile.exists()) {
         System.out.println("template-file does not exist.");
         System.exit(1);
       }
 
       Properties p = new Properties();
       String templatePath = ".";
       if (templateFile.getParentFile() != null) {
         templatePath = templateFile.getParentFile().getCanonicalPath();
       }
       p.setProperty("file.resource.loader.path", templatePath);
       Velocity.init(p);
 
       Model m = new Model(prologFile);
 
       VelocityContext context;
       if (contextGenClassName != null) {
         @SuppressWarnings("unchecked")
         Class<ContextGenerator> cgClass = (Class<ContextGenerator>) Class
             .forName(contextGenClassName);
         Constructor<ContextGenerator> cgCons = cgClass.getConstructor();
         ContextGenerator cgIns = cgCons.newInstance();
         context = cgIns.generateContext(m);
       } else {
         context = new DefaultContextGenerator().generateContext(m);
       }
 
       Template template = null;
       template = Velocity.getTemplate(templateFile.getName());
       StringWriter writer = new StringWriter();
 
       template.merge(context, writer);
       if (context.get("MARKER") == null) {
         System.out.println(writer.toString());
       } else {
         splitter(writer.toString(), context.get("MARKER").toString());
       }
     } catch (ClassNotFoundException e) {
       System.out.println("The context generator class not found. Please make "
           + "sure it is in the java classpath.");
       System.exit(1);
    } catch (Exception e) {
      e.printStackTrace();
     }
   }
 
   public static void splitter(String content, String marker) {
     BufferedReader br = new BufferedReader(new StringReader(content));
     String line = null;
     try {
       String filename = null;
       PrintStream out = null;
       while ((line = br.readLine()) != null) {
         if (line.startsWith(marker)) {
           if (out != null) {
             out.close();
           }
           filename = line.substring(marker.length());
           File f = new File(filename);
           if (f.getParentFile() != null && !f.getParentFile().exists()) {
             f.getParentFile().mkdirs();
           }
           out = new PrintStream(f);
         } else {
           if (out != null)
             out.println(line);
         }
       }
     } catch (IOException e) {
       e.printStackTrace();
     }
   }
 }
