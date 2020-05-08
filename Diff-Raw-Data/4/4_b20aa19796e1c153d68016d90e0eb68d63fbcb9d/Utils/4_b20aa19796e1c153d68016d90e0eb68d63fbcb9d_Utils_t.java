 package controller.utils;
 
import controller.truthTableTemplates.serialization.IntegerPropertyAdapter;
 import model.AppContext;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.ResourceBundle;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Volodymyr_Kychak
  * Date: 10/11/13
  * Time: 2:09 PM
  * To change this template use File | Settings | File Templates.
  */
 public class Utils {
     public static ResourceBundle getMessages() {
         return getLocalizedResources("messages.MessagesBundle") ;
     }
 
     private static ResourceBundle getLocalizedResources(String name){
         return ResourceBundle.getBundle(name, AppContext.getInstance().getCurrentLocale());
     }
     public static List<Integer> createIntList(int first, int last){
        List<Integer> list = new ArrayList<Integer>();
 
         for (int i = first; i <= last; i++ ){
             list.add(i);
         }
         return list;
     }
 }
