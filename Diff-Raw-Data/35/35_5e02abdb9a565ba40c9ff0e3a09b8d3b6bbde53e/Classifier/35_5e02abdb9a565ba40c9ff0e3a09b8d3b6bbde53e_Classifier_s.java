 /**
  * Created with IntelliJ IDEA.
  * User: assaad.moawad
  * Date: 03/07/13
  * Time: 17:11
  * To change this template use File | Settings | File Templates.
  */
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 
 public class Classifier {
 
     private List<String> database;
     private List<String[]> categories;
 
     public Classifier(List<String> database, List<String> categories )
     {
         this.database=database;
         this.categories=convertToLists(categories);
     }
 
 
     private List<String[]> convertToLists(List<String> categoryFile)
     {
         List<String[]> temp = new ArrayList<String[]>();
 
         Iterator iter = categoryFile.iterator();
 
         String s;
         while (iter.hasNext())
         {
             s=(String) iter.next();
             s=s.toLowerCase().trim();

            temp.add(s.split("\\W"));
         }
         return temp;
 
     }
 
 
    private boolean exist(String[] words, String[] category)
     {
        for(int i=0;i<words.length;i++)
        {
            if(words[i].length()<4)
                continue;
             for(int j=0;j<category.length;j++)
             {
                if(words[i].contains(category[j]))
                     return true;

             }

        }
         return false;
     }
 
     public void analyze()
     {
         File statfile=new File("stats.txt");
         File matrixfile=new File("matrix.txt");
         File noncategoryfile=new File("uncategorized.txt");
         try {
             if(!statfile.exists())
                 statfile.createNewFile();
             if(!matrixfile.exists())
                 matrixfile.createNewFile();
             if(!noncategoryfile.exists())
                 noncategoryfile.createNewFile();
             BufferedWriter statout = new BufferedWriter(new FileWriter(statfile,false));
             BufferedWriter matrixout = new BufferedWriter(new FileWriter(matrixfile,false));
             BufferedWriter noncategoryout = new BufferedWriter(new FileWriter(noncategoryfile,false));
 
             int[] results=new int[categories.size()+1];
             String boolR;
             boolean temp;
             boolean tempCat;
             int linecounter=0;
             String line;
             String[] cat;
             int catIndex;
 
             Iterator iterLine = database.iterator();
             Iterator iterCat;
             while(iterLine.hasNext())
             {
                 catIndex=0;
                 line=(String) iterLine.next();
                 String line2=line.toLowerCase().trim();
                String[] words = line2.split("\\W");
                 boolR="" ;
                 temp=false;
                 iterCat=categories.iterator();
                 while(iterCat.hasNext())
                 {
                     cat=(String[])iterCat.next();
                    tempCat=exist(words,cat);
                     temp=temp||tempCat;
                     if(tempCat)
                     {
                         results[catIndex]++;
                         boolR+= "1,";
                     }
                     else
                     {
                         boolR+= "0,";
                     }
                     catIndex++;
                 }
                 matrixout.write(boolR);
                 matrixout.newLine();
                 if(temp==false)
                 {
                     results[categories.size()]++;
                     noncategoryout.write(line);
                     noncategoryout.newLine();
                 }
                 linecounter++;
             }
             System.out.println("Categorized: "+(linecounter- results[categories.size()])+" / " +linecounter);
             System.out.println("-------------------------------------------------------------");
             System.out.println();
 
             String print;
             for(int i=0; i< categories.size();i++)
             {
                 print = "Cat "+i+" - "+ categories.get(i)[0]+ "... : "+ results[i];
                 System.out.println(print);
                 statout.write(print);
                 statout.newLine();
             }
             print = "Uncategorized : "+ results[categories.size()];
             System.out.println(print);
             System.out.println("Press any enter to exit.");
             statout.write(print);
             statout.newLine();
 
             matrixout.close();
             statout.close();
             noncategoryout.close();
 
 
         } catch (Exception ex) {
             Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
         }
 
 
     }
 
 }
