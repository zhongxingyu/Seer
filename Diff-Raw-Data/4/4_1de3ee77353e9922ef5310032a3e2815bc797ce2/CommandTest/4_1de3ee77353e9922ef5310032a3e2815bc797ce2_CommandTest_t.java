import mavenp2versionmatch.main.Command;
 import junit.framework.TestCase;
 

 public class CommandTest extends TestCase{
 	public CommandTest(String name){
 		super(name);
 	}
 	
 	public void testCommands() throws Exception {
 		assertEquals(Command.ADD, Command.findByStr("add"));
 		assertEquals(Command.FIND, Command.findByStr("find"));
 		assertEquals(Command.UPDATE, Command.findByStr("update"));
 		assertEquals(null, Command.findByStr("notacommand"));
 		assertEquals(null, Command.findByStr(""));
 	}
 
 }
