 /**
  * 
  */
 package org.mklab.taskit.server;
 
 import org.mklab.taskit.server.domain.EMF;
 import org.mklab.taskit.server.domain.Lecture;
 
 import java.util.ArrayList;
 import java.util.List;
 
 
 /**
  * @author ishikura
  */
 @SuppressWarnings("nls")
 public class ProgramDesign2011 extends DBInitializer {
 
   /**
    * {@inheritDoc}
    */
   @Override
   protected String[] getStudentIds() {
     return new String[] {"08236009", "08237003", "10236001", "10236002", "10236003", "10236004", "10236005", "10236006", "10236007", "10236008", "10236009", "10236010", "10236012", "10236013",
         "10236014", "10236015", "10236016", "10236017", "10236018", "10236019", "10236020", "10236021", "10236022", "10236023", "10236024", "10236025", "10236026", "10236027", "10236028", "10236029",
         "10236030", "10236031", "10236032", "10236033", "10236034", "10236035", "10236036", "10236037", "10236038", "10236039", "10236040", "10236042", "10236043", "10236044", "10236045", "10236046",
         "10236047", "10236048", "10236049", "10236050", "10236051", "10236052", "10236053", "10236054", "10236055", "10236056", "10236057", "10236058", "10236059", "10236060", "10236061", "10236062",
         "10236063", "10236064", "10236065", "10236066", "10236067", "10236068", "10236069", "10236070", "10236071", "10236072", "10236073", "10236074", "10236075", "10236077", "10236078", "10236079",
         "10236080", "10236081", "10236082"};
   }
 
   /**
    * {@inheritDoc}
    */
   @Override
   protected String[] getTeacherIds() {
     return new String[] {"saitoh"}; //$NON-NLS-1$
   }
 
   /**
    * {@inheritDoc}
    */
   @Override
   protected List<Lecture> createLectures() {
     List<Lecture> list = new ArrayList<Lecture>();
     Lecture l1 = createLecture("1", parseDate("2011/10/06 14:40"));
     createReport(l1, "1-1", 1);
     createReport(l1, "1-2", 1);
 
     list.add(l1);
     return list;
   }
 
   /**
    * {@inheritDoc}
    */
   @Override
   protected String[] getTaIds() {
     return new String[] {"ikeda", "kageyama", "esumi", "emoto"};
   }
 
   /**
    * メインメソッドです。
    * 
    * @param args プログラム引数
    */
   public static void main(String[] args) {
     EMF.setPersistenceProperty(EMF.DB_URL_KEY, "jdbc:mysql://kamome.mk.ces.kyutech.ac.jp/PROGRAM_DESIGN2011");
    EMF.setPersistenceProperty(EMF.DB_PASSWORD_KEY, "ta2k3t3");
    EMF.setPersistenceProperty(EMF.DB_USER_KEY, "taskit");
 
     ProgramDesign2011 p = new ProgramDesign2011();
     p.registerAll();
     p.exportAccounts();
   }
 }
