 import java.io.*;
 import java.util.*;
 
 public class sdmrgen {
 	/**
 	 * @param args
 	 */
 	
 	static String[] a = new String[] {"anchor", "title", "url", "body"};
 	//weight for different representation
 	static double[] b = new double[] {0,	0,	0,	1};
 	//weight for different part in sd
	static double w1 = 0.8, w2 = 0.1, w3 = 0.1;
 
     static String fieldSplitter = "+";
 
 
     public static void main(String[] args) throws Exception{
 		// TODO Auto-generated method stub
 		BufferedReader inf = new BufferedReader(new FileReader("queries.txt"));
		FileWriter outf =new FileWriter("sdmr_body_only.txt");
 		String line;
 		while ( (line=inf.readLine()) != null ){
 			String[] tmp = line.split("\\(");
 			String[] words = tmp[1].substring(0, tmp[1].length()-1).split(" ");
 			String id = tmp[0].split(":")[0];
 			outf.write(id + ":#weight( " + w1 + " #and( ");
 			for (String w: words){
 				String ans = "#weight( ";					
 				for (int i=0; i<a.length; i++)
 					ans += b[i] + " " + w + fieldSplitter + a[i] + " ";
 				ans += ") ";
 				outf.write(ans);
 			}
 
             if (words.length > 1){
                 outf.write(") " + w2 + " #and( ");
                 for (int i=1; i<words.length; i++){
                     outf.write(" #weight( ");
                     for (int j=0; j<a.length; j++)
                         outf.write(b[j] + " #near/1(" + words[i-1] + fieldSplitter + a[j] + " " + words[i] + fieldSplitter + a[j] + ") ");
                     outf.write(")");
                 }
 
                 outf.write(") " + w3 + " #and( ");
                 for (int i=1; i<words.length; i++){
                     outf.write(" #weight( ");
                     for (int j=0; j<a.length; j++)
                         outf.write(b[j] + " #uw/8(" + words[i-1] + fieldSplitter + a[j] + " " + words[i] + fieldSplitter + a[j] + ") ");
                     outf.write(")");
                 }
             }
 			outf.write(") )\n");
 		}
 		outf.close();
 		inf.close();
 	}
 }
