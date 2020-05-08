 package fitgoodies.selenium;
 
 import com.thoughtworks.selenium.CommandProcessor;
 import com.thoughtworks.selenium.SeleniumException;
 
 import fit.Parse;
 import fitgoodies.ActionFixture;
 
 /**
  * Run the selenium-IDE and record your test-case. 
  * Save the result as html and copy the table part into a new Html-File.
  * Adjust the first row and add the reference to this class. 
 * <table>
  * 		<tr><td>fitgoodies.selenium.SeleniumFixture</td><td></td><td></td></tr>
 * 		<tr><td>open</td><td>/application/mainMenu.html</td><td></td></tr>
 * 		<tr><td>type</td><td>j_username</td><td>username</td></tr>
 * 		<tr><td>type</td><td>j_password</td><td>secret</td></tr>
  * 		<tr><td>clickAndWait</td><td>//input[@name='login']</td><td></td></tr>
  * 		<tr><td>clickAndWait</td><td>link=Products</td><td></td></tr>
  * </table>
  * 
  * @author kmussawisade
  *
  */
 public class SeleniumFixture extends ActionFixture {
 
 	private CommandProcessor commandProcessor = SetupHelper.instance().getCommandProcessor();	
 
 	@Override
 	public void doCells(Parse cells) {	
 		String command = cells.text();
 		String[] args = new String[] {cells.more.text(), cells.more.more.text()};
 		String returnValue = null;
 		try {
 			returnValue  = commandProcessor.doCommand(command, args);			
 			checkResult(cells, returnValue);
 		} catch (SeleniumException e) {
 			wrong(cells.more.more, e.getMessage());
 		} catch (Exception e) {
 			exception(cells.more.more, e);
 		}				
 	}
 
 	private void checkResult(Parse cells, String returnValue) {
 		if("OK".equals(returnValue)) {
 			right(cells.more.more);
 		} else {
 			wrong(cells.more.more);
 		}
 	}
 
 }
