 package ch.zhaw.mppce.cpu;
 
 import java.util.ArrayList;
 import java.util.TreeMap;
 
 /**
  * Created with IntelliJ IDEA.
  * User: bbu
  * Date: 15.10.12
  * Time: 21:17
  */
 public class CommandRegister extends Register {
    private TreeMap<String, String> commandRegister;
    private String pointer;
 
     public CommandRegister() {
         commandRegister = new TreeMap<String, String>();
     }
 
     public String getCommandRegister(int pointer) {
         return commandRegister.get(pointer);
     }
 
     public void addCommand(String addr, String command) {
         commandRegister.put(addr, command);
     }
 
     public TreeMap<String, String> getCommandRegisterAsTree() {
         return commandRegister;
     }

    public String getPointer() {
        return pointer;
    }
 }
