 import java.io.IOException;
 import java.util.LinkedList;
 import java.util.List;
 
 import jline.ArgumentCompletor;
 import jline.Completor;
 import jline.ConsoleReader;
 import jline.SimpleCompletor;
 
 public class JLineTest {
     private static final String[] COMMANDS = {"execute", "quit"};
     private static final String[] SUB_COMMANDS = {"listings"};
     private static final String[] FIELDS = {"sedol", "countryIso3", "ric", "symbol"};
 
     public static void main(String[] args) {
         try {
             ConsoleReader reader = new ConsoleReader();
             List<Completor> completors = new LinkedList<Completor>();
             completors.add(new SimpleCompletor(COMMANDS));
             completors.add(new SimpleCompletor(SUB_COMMANDS));
             completors.add(new SimpleCompletor(FIELDS));
             ArgumentCompletor argumentCompletor = new ArgumentCompletor(completors);
             argumentCompletor.setStrict(false);
             reader.addCompletor(argumentCompletor);
             while (true) {
                 String input = reader.readLine("mdcli >>>>");
                 if (input.equalsIgnoreCase("quit")) {
                     break;
                 }
 
                 System.out.println("processing command " + input);
             }
         } catch (IOException e) {
             e.printStackTrace();
         }
     }
 }
