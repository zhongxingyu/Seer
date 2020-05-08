 import java.io.*;
 import java.text.NumberFormat;
 import java.util.*;
 public class RunQuery{
     public static void main(String argv[]) throws Exception{
         NumberFormat nf=NumberFormat.getInstance();
         nf.setMinimumIntegerDigits(3);
         String s;
         Iterator <Query> it=QueryList.iterator();
         BufferedWriter writer=new BufferedWriter(new FileWriter(new File("runquery.sh")));
         while (it.hasNext()){
             Query q=it.next();
             s=q.num;
             BufferedWriter bw=new BufferedWriter(new FileWriter(new File(s+"_query.txt")));
             bw.write("<parameters>");
             bw.newLine();
 
             bw.write("<query>");
             bw.newLine();
 
             bw.write("<number>"+s+"</number>");
             bw.newLine();
 
             bw.write("<text>"+q.words+"</text>");
             bw.newLine();
             
             bw.write("</query>");
             bw.newLine();
 
             bw.write("</parameters>");
             bw.newLine();
 
             bw.close();
 
             writer.write("/bos/usr0/yitongz/indri-5.4/runquery/./IndriRunQuery "
              +s+"_query.txt"
             +" -count=1000 -index=/bos/tmp13/yitongz/data/_indri_inv/"
             +s+" -trecFormat=1 > /bos/tmp13/data/queryresult/"
              +s+".out");
             writer.newLine();
         }
         writer.close();
     }
 }
