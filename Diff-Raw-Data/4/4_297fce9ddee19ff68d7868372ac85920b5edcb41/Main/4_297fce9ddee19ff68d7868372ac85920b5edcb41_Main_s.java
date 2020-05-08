 package core;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.IOException;
 import java.io.PrintStream;
 import java.io.StringReader;
 import java.io.StringWriter;
 import java.lang.reflect.Constructor;
 
 import org.apache.commons.cli.CommandLine;
 import org.apache.commons.cli.CommandLineParser;
 import org.apache.commons.cli.HelpFormatter;
 import org.apache.commons.cli.Option;
 import org.apache.commons.cli.OptionBuilder;
 import org.apache.commons.cli.Options;
 import org.apache.commons.cli.ParseException;
 import org.apache.commons.cli.PosixParser;
 import org.apache.velocity.Template;
 import org.apache.velocity.VelocityContext;
 import org.apache.velocity.app.Velocity;
 
 public class Main {
   public static void main(String[] args) {
     Options options = new Options();
     Option fileOption = OptionBuilder.withArgName("i").hasArg()
         .withDescription("Input Prolog Filename").create("i");
     fileOption.setRequired(true);
     options.addOption(fileOption);
 
     Option templateOption = OptionBuilder.withArgName("t").hasArg()
         .withDescription("Velocity Template").create("t");
     templateOption.setRequired(true);
     options.addOption(templateOption);
 
     Option contextgenOption = OptionBuilder.withArgName("cg").hasArg()
         .withDescription("Context Generator").create("cg");
     contextgenOption.setRequired(false);
     options.addOption(contextgenOption);
 
     HelpFormatter formatter = new HelpFormatter();
     try {
       CommandLineParser parser = new PosixParser();
       CommandLine cmd = parser.parse(options, args);
 
       Velocity.init("velocity.properties");
       Model m = new Model(cmd.getOptionValue("i"));
 
       VelocityContext context;
       if (cmd.hasOption("cg")) {
         Class<ContextGenerator> cgClass = (Class<ContextGenerator>) 
             Main.class.forName(cmd.getOptionValue("cg"));
         Constructor<ContextGenerator> cgCons = cgClass.getConstructor();
         ContextGenerator cgIns = cgCons.newInstance();
         context = cgIns.generateContext(m);
       }
       else {
         context = new DefaultContextGenerator().generateContext(m);
       }
       
       Template template = null;
       template = Velocity.getTemplate(cmd.getOptionValue("t"));
       StringWriter writer = new StringWriter();
 
       template.merge(context, writer);
       if (context.get("MARKER") == null) {
         System.out.println(writer.toString());
       } else {
         splitter(writer.toString(), context.get("MARKER").toString());
       }
     } catch (ParseException e1) {
       formatter.printHelp("FOSD: Model to Text", options);
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
          f.getParentFile().mkdirs();
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
