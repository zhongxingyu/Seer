 package mrz.lab1;
 
 import java.util.List;
 
 import mrz.lab1.processor.ProcessingItem;
 import mrz.lab1.processor.Processor;
 
 /**
  * @author Q-YAA
  */
 public class Main {
 
    private static String DATA_FILE = "data/data.txt";

     public static void main(String[] args) {
        Processor processor = new Processor(DATA_FILE);
         List<ProcessingItem> result = processor.processData();
 
         System.out.println("\n------------------------- Result -------------------------\n");
         for (ProcessingItem processingItem : result) {
             System.out.println(processingItem.getFactor() + "\n" + processingItem.getMultiplicand() + "\n" +
                 processingItem.getPartialSum() + "\n");
         }
     }
 }
