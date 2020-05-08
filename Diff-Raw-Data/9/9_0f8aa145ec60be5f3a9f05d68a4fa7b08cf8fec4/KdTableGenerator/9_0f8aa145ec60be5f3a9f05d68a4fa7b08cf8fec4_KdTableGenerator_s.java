 package ru.spbau.bioinf.tagfinder;
 
 import java.io.BufferedReader;
 import java.io.File;
 import ru.spbau.bioinf.tagfinder.util.ReaderUtil;
 
 public class KdTableGenerator {
 
     private static String file1 = "full_exp_base_proper_none";
     private static String file2 = "bar_exp_base_proper_none";
    private static String file3 = "full_exp_base_correct_none";
    private static String file4 = "bar_exp_base_correct_none";
 
     public static void main(String[] args) throws Exception {
         for (int gap = 1; gap <= 3; gap++) {
             printTablesProper(gap);
         }
     }
 
     public static void printTablesProper(int gap) throws Exception {
         printKd(file1, "The number of $(k-d,d)$-proper spectra in the $ST$ data set for the case of " + gap + "-aa tags.", gap, "kd-" + gap + "-proper-ST");
         printKd(file2, "The number of $(k-d,d)$-proper spectra in the $\\STbar$ data set for the case of " + gap + "-aa tags.", gap, "kd-" + gap + "-proper-ST-bar");
     }
 
     public static void printTablesCorrect(int gap) throws Exception {
         printKd(file3, "The number of $(k-d,d)$-correct spectra in the $ST$ data set for the case of " + gap + "-aa tags.", gap, "kd-" + gap + "-correct-ST");
         printKd(file4, "The number of $(k-d,d)$-correct spectra in the $\\STbar$ data set for the case of " + gap + "-aa tags.", gap, "kd-" + gap + "-correct-ST-bar");
     }
 
     private static void printKd(String file, String caption, int gap, String label) throws Exception {
         BufferedReader in = ReaderUtil.createInputReader(new File("res", "kd_" + file + "_" + gap + ".txt"));
         int[][] res = new int[1000][1000];
         int rows = 0;
         int maxD = 0;
         int kLessThan3 = 0;
         int kLessThan4 = 0;
         int kTotal = 0;
         do {
             String s = in.readLine();
             if (s == null) {
                 break;
             }
             if (!s.startsWith("(")) {
                 continue;
             }
             String[] data = s.substring(1).split("\\D+");
             int k = Integer.parseInt(data[0]);
             int d = Integer.parseInt(data[1]);
             int n = Integer.parseInt(data[2]);
             res[k][d] += n;
             kTotal += n;
             if (k < 3) {
                 kLessThan3 +=n;
             }
             if (k < 4) {
                 kLessThan4 +=n;
             }
             rows = Math.max(rows, k - d);
             maxD = Math.max(maxD, d);
         } while (true);
         System.out.println("% k <=2 - " + kLessThan3 + " " + (ValidTags.df.format(100d * kLessThan3 / kTotal)) + "%");
         System.out.println("% k <=3 - " + kLessThan4 + " " + (ValidTags.df.format(100d * kLessThan4 / kTotal)) + "%");
 
         System.out.println("\\begin{landscape}\n");
         System.out.println("\\begin{table}[h]\\footnotesize\n" +
                 "%\\vspace{3mm}\n" +
                 "{\\centering\n" +
                 "\\begin{tabular}{|c|c|");
         for (int i = 0; i < maxD; i++) {
             System.out.print("c|");
         }
         System.out.println("}\n  \\hline\n" +
                 "  $k-d$/$d$ ");
 
         for (int i = 0; i <= maxD; i++) {
             System.out.print(" & " + i);
         }
 
         System.out.println("\\\\\n");
         System.out.println("  \\hline\n" +
                 "  \\hline\n");
 
         for (int row = 0; row <= rows; row++) {
             System.out.print(row + " ");
             for (int d = 0; d <= maxD; d++) {
                 System.out.print(" & ");
                 int v = res[row + d][d];
                 if (v > 0) {
                     System.out.print(v);
                 }
             }
             System.out.println("\\\\\n");
         }
         System.out.println("  \\hline\n" +
                 "\\end{tabular}\n" +
                 "\\par}\n" +
                 "\\centering\n" +
                 "\\caption{" + caption + "}\n" +
                 "\\vspace{3mm}\n" +
                 "\\label{table:" + label + "}\n" +
                 "\\end{table}");
         System.out.println("\\end{landscape}");
     }
 }
