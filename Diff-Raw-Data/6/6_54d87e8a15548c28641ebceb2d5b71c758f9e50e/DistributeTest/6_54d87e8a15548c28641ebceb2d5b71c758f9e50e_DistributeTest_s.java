 import java.util.Iterator;
 
 
 public class DistributeTest {
 
     public static void main(String[] args){
 
         PCList testList = new PCList();
         testList.add(new PCObject(4));
         testList.add(new PCObject(32));
         testList.add(new PCObject(345345));
 
         TestFunction tf = new TestFunction();
 
 
         distributeClient client = new distributeClient();
 
         client.getRegistries(args[0]);
 
         PCList newList = client.distributeFunction(tf , testList);
 
         for(Iterator<PCObject> it = newList.iterator();it.hasNext();){
            System.out.println(it.next().getBase());
         }
 
 
 
 
     }
 }
