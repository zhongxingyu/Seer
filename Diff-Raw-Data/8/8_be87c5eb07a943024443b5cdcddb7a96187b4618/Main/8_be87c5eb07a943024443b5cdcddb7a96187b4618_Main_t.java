 
 package de.weltraumschaf.caythe;
 
 import de.weltraumschaf.caythe.backend.BackendFactory;
 import java.io.PrintWriter;
 import java.io.StringWriter;
 
 /**
  *
  * @author Sven Strittmatter <sst@kwick.de>
  */
 public class Main {
 
     private static final String FLAGS = "[-ixdh]";
     private static final String USAGE = "Usage: java -jar caythe.jar execute|compile " + FLAGS + " <source file path>";
 
 
     private static boolean debug = true;
 
     private static String formatError(Throwable t) {
         return formatError(t, false);
     }
 
     private static String formatError(Throwable t, boolean withMessage) {
         StringBuilder sb = new StringBuilder("DEBUG:\n");
         sb.append("Exception thrown");
 
         if (withMessage && null != t.getMessage()) {
             sb.append("wit message: ")
               .append(t.getMessage());
         }
 
         sb.append("!\n")
           .append("Stack trace:\n");
         StringWriter sw = new StringWriter();
         PrintWriter pw  = new PrintWriter(sw);
         t.printStackTrace(pw);
         sb.append(sw.toString());
         return sb.toString();
     }
 
     public static void main(String[] args) {
         try {
             run(args);
         } catch (Error err) {
             if (null != err.getMessage()) {
                System.out.println(err.getMessage() + "\n");
             }
 
             if (debug) {
                 System.out.println(formatError(err));
             }
 
             System.exit(err.getCode());
         } catch (Exception ex) {
            System.out.println(USAGE + "\n");
 
             if (debug) {
                 System.out.println(formatError(ex, true));
             }
 
             System.exit(-1);
         }
 
         System.exit(0);
     }
 
     public static void run(String[] args) throws Exception, Error {
         if (args.length == 0) {
            throw new Error("Too few arguments!\n" + USAGE, 1);
         }
 
         BackendFactory.Operation operation = null;
 
         // Operation.
         if (args[0].charAt(0) != '-') {
             if (args[0].equalsIgnoreCase("compile")) {
                 operation = BackendFactory.Operation.COMPILE;
             } else if (args[0].equalsIgnoreCase("execute")) {
                 operation = BackendFactory.Operation.EXECUTE;
             }
         }
 
         int i = 0;
         String flags = "";
 
         // Flags.
         while ((i++ < args.length) && (args[i].charAt(0) == '-')) {
             flags += args[i].substring(1);
         }
 
         // @todo fix -h option
         if (flags.indexOf('h') > -1) {
             help();
             return;
         }
 
         if (null == operation) {
             throw new Exception(String.format("Bad operation '%s' given!", args[0]));
         }
 
         // Source path.
         if (i < args.length) {
             String path = args[i];
             App pascal = new App(operation, path, flags);
             pascal.execute();
         } else {
             throw new Exception("No source file given!");
         }
     }
 
     private static void help() {
         StringBuilder sb = new StringBuilder(USAGE);
         sb.append("  -i    intermediate\n");
         sb.append("  -x    crossrevrence\n");
         sb.append("  -d    debug output\n");
         sb.append("  -h    help\n");
         System.out.println(sb);
     }
 }
