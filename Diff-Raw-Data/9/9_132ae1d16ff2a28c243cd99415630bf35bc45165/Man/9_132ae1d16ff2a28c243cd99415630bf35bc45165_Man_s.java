 package pi.interpreter.commands;
 
 import pi.interpreter.Environment;
 import pi.interpreter.Interpreter;
 
 public class Man implements Command
 {
 
   private static final String LABEL = "man";
   private Interpreter _interpreter;
 
   public Man(Interpreter interpreter)
   {
     _interpreter = interpreter;
   }
 
   public String getLabel()
   {
     return LABEL;
   }
 
   public int exec(String[] args, Environment env)
   {
     Command cmd = _interpreter.getCmd(args[1]);
     if (cmd == null)
       {
         env.out.println(args[1] + " : command not found");
         return Command.EXIT_FAILURE;
       }
 
     env.out.println(cmd.manual());
     return Command.EXIT_SUCCESS;
   }
 
   public String manual()
   {
    return "";
   }
 }
