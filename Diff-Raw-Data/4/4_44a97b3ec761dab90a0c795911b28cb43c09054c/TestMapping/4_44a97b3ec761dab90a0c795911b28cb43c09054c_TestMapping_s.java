 import junit.framework.TestCase;
 import org.gds.fs.GDSDir;
 import org.gds.fs.GDSFile;
 import org.gds.fs.mapping.FlatMapping;
 import org.junit.Test;
 
 import java.io.*;
 import java.util.Scanner;
 
 
 /**
  * Created by IntelliJ IDEA.
  * User: adrien
  * Date: 3/11/11
  * Time: 11:24 AM
  * To change this template use File | Settings | File Templates.
  */
 public class TestMapping  {

     @Test
     public void testToStringForDir() throws Throwable{
         GDSDir gdsDir = new GDSDir();
         gdsDir.setDocId("testunit");
         gdsDir.setEtag("testunit");
         gdsDir.setParent(null);
         gdsDir.setTitle("testunit");
         FlatMapping fm = new FlatMapping();
         String value = "etag: testunit\n" + "title: testunit\n";
         System.out.println(value+" = " + fm.toString(gdsDir));
         if (value.equals(fm.toString(gdsDir)) == false){
             throw new Exception(" toString(GDSDir) fail");
         }
 
         /*FileWriter fw = new FileWriter(new File(gdsDir.getDocId()), false);
         fw.append(fm.toString(gdsDir));
         fw.flush();
         String s = readFile(new File(gdsDir.getDocId()));
         String[] lines = s.split(System.getProperty("line.separator"));*/
     }
 
      @Test
     public void testToStringForFile() throws Throwable{
          GDSFile gdsFile = new GDSFile();
          gdsFile.setDocId("fileTest");
          gdsFile.setEtag("fileTest");
          gdsFile.setTitle("fileTest");
          FlatMapping fm = new FlatMapping();
          String value = "etag: fileTest\ntitle: fileTest\n";
          if (value.equals(fm.toString(gdsFile)) == false){
              throw new Exception("toString(gdsFile) fail");
 
          }
 
     }
 
     @Test
     public void toObjectTestDir() throws Throwable {
          String value = "etag: testunit"+System.getProperty("line.separator") + "title: testunit"+System.getProperty("line.separator");
          FlatMapping fm = new FlatMapping();
         GDSDir dir = fm.toObject(value, GDSDir.class);
         if (dir.getEtag().equals("testunit") == false){
             throw new Exception ("toObject(gdsdir) fail on etag");
         }
         if (dir.getTitle().equals("testunit") == false){
             throw new Exception ("toObject(gdsdir) fail on  title");
         }
         
 
     }
 
     @Test
     public void toObjectTestFile() throws Throwable {
 
         String value = "etag: fileTest"+System.getProperty("line.separator")+"title: fileTest"+System.getProperty("line.separator");
         FlatMapping fm = new FlatMapping();
         GDSFile dir = fm.toObject(value, GDSFile.class);
         if (dir.getEtag().equals("fileTest") == false){
             throw new Exception ("toObject(gdsfile) fail on etag");
         }
         if (dir.getTitle().equals("fileTest") == false){
             throw new Exception ("toObject(gdsfile) fail on  title");
         }
 
 
     }
 
     @Test
     public void toObjectTest1() throws Throwable {
          String value = "etag: testunit"+System.getProperty("line.separator") + "title: testunit"+System.getProperty("line.separator");
          FlatMapping fm = new FlatMapping();
         GDSDir dir = fm.toObject(value, GDSDir.class);
     }
 
 
 
     private String readFile(File file)
    {
       try
       {
          Scanner sc = new Scanner(new FileReader(file));
          StringBuffer sb = new StringBuffer();
          while (sc.hasNextLine())
          {
             sb.append(sc.nextLine() + System.getProperty("line.separator"));
          }
          return sb.toString();
       }
       catch (FileNotFoundException e)
       {
          e.printStackTrace();
          return null;
       }
    }
 }
