import java.util.List;
 import java.util.Arrays;
 
 public class Histogram extends Chart {
 
    private List<Integer> values;
     private int[][] ranges;
     
     /**
      * Create a histogram instance.
      * @param ranges 2D array.  Each row should be of type {@code int[]} with a 
      * length of 2.  The first value is the min of that range, the second is the
      * max of the range.
      * @param values Array of numbers to use for the histogram. 
      */
     public Histogram(int[][] ranges, Integer... values) {
         // initialize super class with empty contents
         super(new String[][]{});
         
         this.ranges = ranges;
        this.values = Arrays.asList(values);
         generateChartRows();
     }
     
     /**
      * Add a number to the histogram.
      */
     public void addValue(int value) {
         values.add(value);
         generateChartRows();
     }
     
     public void setRanges(int[][] ranges) { this.ranges = ranges; generateChartRows(); }
     public int[][] getRanges() { return this.ranges;}
     
     /**
      * Convert {@code ranges} into rows for chart.
      */
     protected void generateChartRows() {
         String[][] rows = new String[ranges.length][2];
         int i = 0;
         for (int[] range: ranges) {
             String data = "";
             for (int val: values) {
                 if (val <= range[1] && val >= range[0]) {
                     data += "*";
                 }
             }
             rows[i] = new String[]{labelForRange(range), data};
             i++;
         }
         orderRows(rows);
         this.setRows(rows);
     }
     
     protected String labelForRange(int[] range) {
         return "" + range[0] + " - " + range[1];
     }
     
     protected int[] rangeForLabel(String label) {
         return new int[]{new Integer(label.split("-")[0].trim()), new Integer(label.split("-")[1].trim())};
     }
     
     /**
      * Apply sorting to rows.  Sort order by first item of label then second.
      */
     protected void orderRows(String[][] rows) {
         // build ranges in appropriate order
         int[][] ranges = new int[rows.length][2];
         int i = 0;
         
         // build ranges
         for (String[] row: rows) {
             ranges[i] = rangeForLabel(row[0]);
             i++;
         }
         
         // insertion sort on ranges.  apply same sorting operations to the rows
         // array as well (parallel arrays)
         for (i = 1; i < ranges.length; i++) {
             int[] item = ranges[i];
             String[] row = rows[i];
             int index = i;
             while (index >= 1 && (item[0] < ranges[index-1][0] || item[1] < ranges[index-1][1] && item[0] == ranges[index-1][0])) {
                 // shift stuff to the right
                 ranges[index] = ranges[index-1];
                 rows[index] = rows[index-1];
                 index--;               
             }
             ranges[index] = item;
             rows[index] = row;
         }
     }
     
     // testing/usage example.
     public static void main(String[] args) {
         Histogram hist = new Histogram(new int[][]{{0, 10}, {11, 20}, {11, 15}}, 1, 1, 2, 3, 12, 16, 13, 1, 200);
         System.out.println(hist);
     }
 }
