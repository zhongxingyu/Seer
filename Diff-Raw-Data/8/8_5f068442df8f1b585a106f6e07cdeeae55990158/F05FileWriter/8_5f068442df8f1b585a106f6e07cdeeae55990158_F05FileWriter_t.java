 package tw.com.citi.cdic.batch.item.file;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.springframework.batch.item.ItemWriter;
 
 import tw.com.citi.cdic.batch.model.A24;
 
 /**
  * @author Chih-Liang Chang
  * @since 2010/12/30
  */
 public class F05FileWriter implements ItemWriter<A24> {
 
     private ItemWriter<A24> a24ItemWriter;
 
     private ItemWriter<A24> b24ItemWriter;
 
     private ItemWriter<A24> c24ItemWriter;
 
     @Override
     public void write(List<? extends A24> items) throws Exception {
         List<A24> a24Items = new ArrayList<A24>();
         List<A24> b24Items = new ArrayList<A24>();
         List<A24> c24Items = new ArrayList<A24>();
 
         for (A24 a26 : items) {
             switch (a26.getType()) {
             case A:
                 a24Items.add(a26);
                 break;
             case B:
                 b24Items.add(a26);
                 break;
             case C:
                 c24Items.add(a26);
                 break;
             }
         }
 
         a24ItemWriter.write(a24Items);
         b24ItemWriter.write(b24Items);
         c24ItemWriter.write(c24Items);
     }
 
    public void setA24ItemWriter(ItemWriter<A24> a24ItemWriter) {
         this.a24ItemWriter = a24ItemWriter;
     }
 
    public void setB24ItemWriter(ItemWriter<A24> b24ItemWriter) {
         this.b24ItemWriter = b24ItemWriter;
     }
 
    public void setC24ItemWriter(ItemWriter<A24> c24ItemWriter) {
         this.c24ItemWriter = c24ItemWriter;
     }
 
 }
