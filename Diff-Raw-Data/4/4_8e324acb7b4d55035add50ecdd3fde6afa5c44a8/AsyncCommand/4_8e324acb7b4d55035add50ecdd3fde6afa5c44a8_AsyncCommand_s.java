 package ops;
 
 
public interface AsyncCommand
 {
  void exec(CommandContext context, Object[] args) throws Exception;
 }
