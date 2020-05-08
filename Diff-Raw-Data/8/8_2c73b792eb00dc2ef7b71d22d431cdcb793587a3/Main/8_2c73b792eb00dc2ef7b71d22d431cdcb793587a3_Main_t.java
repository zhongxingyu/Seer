 package mrz.lab1;
 
 import java.util.List;
 
 import mrz.lab1.processor.ProcessingItem;
 import mrz.lab1.processor.Processor;
 
 /**
  * @author Q-YAA
  */
 public class Main {
 
     public static void main(String[] args) {
        String dataFile = args[0];
//        String dataFile = "data/data.txt";

        Processor processor = new Processor(dataFile);
         List<ProcessingItem> result = processor.processData();
 
         System.out.println("\n------------------------- Result -------------------------\n");
         for (ProcessingItem processingItem : result) {
             System.out.println(processingItem.getFactor() + "\n" + processingItem.getMultiplicand() + "\n" +
                 processingItem.getPartialSum() + "\n");
         }
     }
 }
