 package jobs;
 
 
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.util.Arrays;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Scanner;
 import java.util.concurrent.atomic.AtomicBoolean;
 import java.util.concurrent.atomic.AtomicInteger;
 
 public class MatrixAdditionJob extends Job {
     private Integer row;
     private Integer column;
     private AtomicInteger count;
     private Integer result;
     private AtomicBoolean interrupt;
 
    private int[][] matrix;
 
     /**
      *
      * @param fileName
      * @param row
      * @param column
      * @param count
      * @param matrix
      */
    public MatrixAdditionJob(String fileName, Integer row, Integer column, int count, int[][] matrix) {
         super(fileName);
         this.row = row;
         this.column = column;
        this.matrix = new int[row][column];
         for(int i=0;i<row;++i) {
             System.arraycopy(matrix[i], 0, this.matrix[i], 0, column);
         }
         result = 0;
         this.count = new AtomicInteger(count);
         interrupt = new AtomicBoolean(false);
     }
 
     public MatrixAdditionJob(boolean isRequest) {
         super(isRequest);
     }
 
     @Override
     public void run() {
         while(count.get()>0 && !interrupt.get()) {
             count.decrementAndGet();
             for(int i=0;i<row;++i) {
                 for(int j=0;j<column;++j)
                     result += matrix[i][j];
             }
         }
     }
 
     @Override
     public boolean isFinished() {
         synchronized (this) {
             return count.get() == 0;
         }
     }
 
     @Override
     public void stop() {
         interrupt.set(true);
     }
 
     @Override
     public void resume() {
         interrupt.set(false);
     }
 
     @Override
     public String toString() {
         return jobName;
     }
 
     @Override
     public JobResult getResult() {
         return new MatrixAdditionResult(result);
     }
 
     public static List<Job> splitJobs(String fileName) throws FileNotFoundException {
         Scanner in = new Scanner(new FileInputStream(fileName));
         Integer row,column,count;
         row = in.nextInt();
         column = in.nextInt();
         count = in.nextInt();
        int[][] matrix = new int[row][column];
         readMatrix(in, row, column,  matrix);
         List<Job> list = new LinkedList<Job>();
 
         for(int i=0;i<row;++i) {
            int[][] smallMatrix = new int[1][column];
             smallMatrix[0] = Arrays.copyOf(matrix[i], column);
 //            System.out.println("job " + i + ", " + Arrays.deepToString(smallMatrix));
             MatrixAdditionJob maj = new MatrixAdditionJob(fileName, 1, column, count, smallMatrix);
             list.add(maj);
         }
         in.close();
         return list;
     }
 
    private static void readMatrix(Scanner in, int row, int column, int[][] matrix) {
         for(int i=0;i<row;++i)
             for(int j=0;j<column;++j)
                matrix[i][j] = in.nextInt();
     }
 /*
     public static void main(String[] args) throws FileNotFoundException {
         MatrixAdditionJob.splitJobs("matrix");
     }
 */
 }
