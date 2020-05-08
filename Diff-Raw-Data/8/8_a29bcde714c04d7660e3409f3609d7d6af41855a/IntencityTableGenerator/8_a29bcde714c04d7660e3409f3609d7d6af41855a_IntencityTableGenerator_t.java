 package ru.spbau.bioinf.tagfinder;
 
 import java.io.BufferedReader;
 import java.io.File;
 import ru.spbau.bioinf.tagfinder.util.ReaderUtil;
 
 public class IntencityTableGenerator {
 
     public static void main(String[] args) throws Exception {
         String file1 = "bar_exp_annotated_proper_none_intencity";
         String file2 = "bar_exp_annotated_correct_none_intencity";
 
         double[][] res = new double[100][3];
         for (int gap = 1; gap <= 3; gap++) {
            printTable(res, file1, gap, "Percentage of spectra in the $\\STbar$ data set, such that all their top-scoring " + gap + "-aa tags of length $k$ are proper (+) or incorrect (-).");
         }
         TexTableGenerator.createThreeRowsTable(res, "Average percentage of proper top-scoring tags", "k", "");
 
         res = new double[100][3];
         for (int gap = 1; gap <= 3; gap++) {
             printTable(res, file2, gap, "Percentage of spectra in the $\\STbar$ data set, such that all their top-scoring " + gap + "-aa tags of length $k$ are correct (+) or incorrect (-).");
         }
         TexTableGenerator.createThreeRowsTable(res, "Average percentage of correct top-scoring tags", "k", "");
     }
 
     private static void printTable(double[][] res, String file, int gap, String caption) throws Exception {
         BufferedReader in = ReaderUtil.createInputReader(new File("res", "share_" + file + "_" + gap + ".txt"));
         double[] good = new double[100];
         double[] bad = new double[100];
         double[] both = new double[100];
 
         int max = 0;
 
         do {
             String s = in.readLine();
             if (s.contains(",")) {
                 break;
             }
 
             if (s.indexOf(" ") == 0) {
                 break;
             }
             long[] data = CalculateRelation.getData(s);
             int pos = 2;
             int d = 1;
 
             while (pos < data.length) {
                 max = Math.max(max, d);
                 if (data[pos] == 0) {
                     bad[d]++;
                } else if (data[pos + 1] <= data[pos] ) {
                     good[d]++;
                 } else {
                     both[d]++;
                 }
 
                 d++;
                 pos += 2;
             }
         } while (true);
         double[] ans = new double[max];
         for (int i = 1; i<= max; i++) {
             double total = good[i] + bad[i] + both[i];
            ans[i - 1] = (good[i])*100/total;
         }
         res[gap - 1] = ans;
 
         System.out.print("\\begin{table}[ht]\\footnotesize\n" +
                 "\\vspace{3mm}\n" +
                 "{\\centering\n" +
                 "\\begin{center}\n" +
                 "\\begin{tabular}{|c|c|c|");
         for (int i = 1; i<= max; i++) {
             System.out.print("c|");
         }
         System.out.println("}\n" +
                 "  \\hline\n" +
                 "  \\multicolumn{2}{|c|}{ } & \\multicolumn{ " + max + "}{|c|}{$k$} \\\\\n" +
                 "  \\cline{3-" + (max + 2)+ " }\n" +
                 "  \\multicolumn{2}{|c|}{ } ");
         for (int i = 1; i<= max; i++) {
             System.out.print(" & " + i);
         }
 
         System.out.print("\\\\\n" +
                 "  \\hline\n" +
                 "  \\multirow{2}{*}{spectra (\\%)} & + ");
         for (int i = 1; i<= max; i++) {
             System.out.print(" & ");
             double total = good[i] + bad[i] + both[i];
             if (total > 0) {
                 System.out.print(ValidTags.df.format(100 * good[i]/total));
             }
         }
 
         System.out.print(" \\\\\n" +
                 "      & -- ");
         for (int i = 1; i<= max; i++) {
             System.out.print(" & ");
             double total = good[i] + bad[i] + both[i];
             if (total > 0) {
                 System.out.print(ValidTags.df.format(100 * bad[i]/total));
             }
         }
         System.out.println(" \\\\\n" +
                 "  \\hline\n" +
                 "\\end{tabular}\n" +
                 "\\end{center}\n" +
                 "\\par}\n" +
                 "\\centering\n" +
                 "\\caption{" + caption + "}\n" +
                 "\\vspace{3mm}\n" +
                 "\\label{table:all-top-scoring}\n" +
                 "\\end{table}");
     }
 }
