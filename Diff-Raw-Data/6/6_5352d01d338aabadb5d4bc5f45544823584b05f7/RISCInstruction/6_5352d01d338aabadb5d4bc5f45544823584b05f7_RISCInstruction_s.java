 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 
 public class RISCInstruction {
 
   private String cmd;
   private String args1;
   private String args2;
   private String args3;
   private String Instruction;
   private boolean isImmediate;
   private int immediate;
   private final String methodName = "exeCmd";
 
   public RISCInstruction() {
   }
 
   public RISCInstruction(String command) {
     cmd = command;
     populateValues();
   }
 
   private void populateValues() {
     getInstruction();
     pullArgs1();
     args2 = pullArgAfter2();
     if (!isImmediate)
       args3 = pullArgAfter2();
     else args3 = "";
   }
 
   private String pullArgAfter2() {
     String result = "";
     if (cmd.length() > 1) {
       if (cmd.charAt(0) == 'r') {
         isImmediate = false;
         result = truncateAt(',');
       } else if (cmd.substring(0, 1).equals("0x")) {
         isImmediate = true;
         cmd = cmd.substring(2, cmd.length());
         immediate = Integer.parseInt(cmd);
       } else {
         isImmediate = true;
         immediate = Integer.parseInt(cmd);
       }
     }
     return result;
   }
 
   private void pullArgs1() {
     args1 = truncateAt(',');
   }
 
   private void getInstruction() {
     cmd = cmd.trim();
    Instruction = truncateAt(' '); 
   }
 
   private String truncateAt(char cha) {
     if (cmd.length() > 1) {
       int index = cmd.indexOf(cha);
       String result = "";
       if (index > 0) {
         result = cmd.substring(0, index);
         result = result.trim();
         cmd = cmd.substring(index + 1, cmd.length());
         cmd = cmd.trim();
       } else {
         result = cmd.trim();
       }
       return result;
     } else return "";
   }
 
   public void exeCmd() {
     try {
      System.out.println(getClassName());
      Class cls = Class.forName(">>>>"+getClassName()+"<<<<");
       Method meth = cls.getMethod(methodName, this.getClass());
       meth.invoke(cls.newInstance(), this);
     } catch (NoSuchMethodException e) {
       Debug.forceQuit("NEVER REACHING:: method not found" + getClassName());
     } catch (IllegalAccessException e) {
       Debug.forceQuit("NEVER REACHING:: class illegally reached " + getClassName());
     } catch (InstantiationException e) {
       Debug.forceQuit("NEVER REACHING:: instantiateion failed " + getClassName());
     } catch (InvocationTargetException e) {
       Debug.forceQuit("NEVER REACHING:: invocation failed " + getClassName());
     } catch (ClassNotFoundException e) {
       Debug.forceQuit("NEVER REACHING:: class is not found " + getClassName());
     }
   }
 
   private String getClassName() {
     return "CMDS_" + Instruction.toUpperCase();
   }
 
   public String getArgs3() {
     return args3;
   }
 
   public String getArg1() {
     return args1;
   }
 
   public boolean isImmediate() {
     return isImmediate;
   }
 
   public int getImmediate() {
     return immediate;
   }
 
   public String getArg2() {
     return args2;
   }
 }
