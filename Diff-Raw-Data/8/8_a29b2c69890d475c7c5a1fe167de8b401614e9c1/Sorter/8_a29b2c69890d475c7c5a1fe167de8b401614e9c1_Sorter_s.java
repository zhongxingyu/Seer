 package rio.sorter;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 
 public class Sorter {
 
     private File input;
     private File output;
     private int outputAmount;
     private Printer printer;
     private long[] longs;
     private Sort sort;
 
     private Sorter(String[] args) {
 
         evaluateArgs(args);
         evaluateInput(args[0]);
         evaluateOutput(args[1], args[2]);
         evaluateSortType(args[3]);
     }
 
     private void evaluateArgs(String[] args) {
 
         if (args.length != 4) {
             System.out.println("Required parameters: [input file] [output file] [output amount] [serial or parallel] (respectively)");
             System.exit(0);
         }
     }
 
     private void evaluateInput(String path) {
 
         input = new File(path);
 
         if (!input.exists()) {
             System.out.println("File \"" + input + "\" not found.");
             System.exit(0);
         }
     }
 
     private void evaluateOutput(String path, String amountParameter) {
 
         try {
             outputAmount = Integer.parseInt(amountParameter);
         } catch (NumberFormatException exception) {
             System.out.println("Third parameter must be a number.");
             System.exit(0);
         }
 
         output = new File(path);
 
         try {
             printer = new Printer(output);
         } catch (FileNotFoundException exception) {
             System.out.println("Captain overboard!");
             exception.printStackTrace();
         }
     }
 
     private void evaluateSortType(String sortType) {
         if (!sortType.equals("serial") && !sortType.equals("parallel")) {
            System.out.println("You must declare which sort to use, serial or parallel");
             System.exit(0);
         }
     }
 
     public void readFile() {
 
         LittleEndianReader reader = null;
 
         try {
             reader = new LittleEndianReader(input);
         } catch (FileNotFoundException exception) {
             System.out.println("Captain overboard!");
             exception.printStackTrace();
         }
 
         System.out.println("Reading file \"" + input + "\"...");
 
         long startTime = System.currentTimeMillis();
         longs = reader.read();
 
         System.out.println("Read file in " + (System.currentTimeMillis() - startTime) + "ms.");
     }
 
     public void sort(String sortType) {
 
         if (sortType.equals("parallel")) {
             sort = new ConcurrentQuickSort(longs);
        }
        if (sortType.equals("serial")) {
             sort = new QuickSort(longs);
         }
 
         System.out.println("Sorting longs...");
 
         long startTime = System.currentTimeMillis();
         sort.sort();
 
         System.out.println("Sorted longs in " + (System.currentTimeMillis() - startTime + "ms."));
     }
 
     public void print() {
 
         System.out.println("Writing output...");
 
         long startTime = System.currentTimeMillis();
         printer.print(longs, outputAmount);
 
         System.out.println("Wrote output in " + (System.currentTimeMillis() - startTime) + "ms to \"" + output + "\"...");
     }
 
     public long[] getLongs() {
         return longs;
     }
 
     public static void run(String[] args) throws FileNotFoundException {
 
         Sorter sorter = new Sorter(args);
         sorter.readFile();
         sorter.sort(args[3]);
         sorter.print();
     }
 }
