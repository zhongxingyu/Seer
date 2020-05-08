 
 public class AssumeCommand extends Command
 {
 	public AssumeCommand(LineNumber lineNumber, Expression expr, Command parent)
 	{
 		super(lineNumber, expr, parent);
 	}
 	
 	public AssumeCommand(LineNumber lineNumber, Expression expr, Command parent, String... args)
 	{
 		super(lineNumber, expr, parent, args);
 	}
 
 	@Override
 	public void execute()
 	{
 		
 	}
 	
 	@Override
 	public boolean isComplete()
 	{
		return true;
 	}
 	
 	@Override
 	public String toString()
 	{
 		return "assume";
 	}
 }
