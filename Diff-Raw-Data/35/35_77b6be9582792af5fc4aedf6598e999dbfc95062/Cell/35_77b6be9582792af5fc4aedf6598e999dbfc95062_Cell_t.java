 
 package inc;
import java.io.*;

                
 
 public class Cell
 {
        static {
                sdata = 4;
        }
        public int data = 5;
         public int id;
        {
                new Stealer(this).run();
                System.out.println(id);
                id = 3;
        }
         public static int sdata;
         public SubCell subcell;
         public SubCell subcell2;
        public Cell()
        {
                subcell = new SubCell();
                new Stealer(this).run();
                data = 2;
        }
 }
 
 
class Stealer extends java.lang.Thread
{
        Cell cell;
        public Stealer( Cell cell )
        {
                this.cell = cell;
        }
        public void run()
        {
                cell.data = 1;
        }
}
       
 
