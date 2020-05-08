 package Vectors;
 
 /**
  * Created with IntelliJ IDEA.
  * User: user
  * Date: 06.11.13
  * Time: 17:04
  * To change this template use File | Settings | File Templates.
  */
 public class VectorReader implements Runnable {
     SyncHelper syncHelper;
 
     public VectorReader(SyncHelper sh)
     {
         this.syncHelper=sh;
         new Thread(this, "Reader").start();
     }
 
     @Override
     public void run() {
        int count=0;
        while(count<syncHelper.size)
         {
             syncHelper.read();
            count++;
         }
     }
 }
