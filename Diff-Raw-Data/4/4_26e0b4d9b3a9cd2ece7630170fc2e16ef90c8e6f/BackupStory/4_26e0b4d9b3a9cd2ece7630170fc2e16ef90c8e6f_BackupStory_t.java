 package info.dbackup.backup;
 
 import java.io.File;
 
 import org.jmock.integration.junit4.JUnitRuleMockery;
 import org.junit.Assert;
 import org.junit.Rule;
 
 import cucumber.annotation.en.Given;
 import cucumber.annotation.en.Then;
 import cucumber.annotation.en.When;
 
 //import cucumber.junit.Cucumber;
 
 public class BackupStory {
 
 	private Backup backup;
 
 	@Rule
 	public final JUnitRuleMockery context = new JUnitRuleMockery();
 
 	@Given("^a backup scheduler with user with name (.*)$")
 	public void given(String username) {
 		// User user = new User(username);
 		// backup = new Backup(user);
 	}
 
 	@When("^I add file (.*) for scheduled backup$")
 	public void when(String filename) {
 		backup.addFolderToBackup(new File(filename));
 	}
 
	@When("^user will backup$")
	public void I_will_backup() {
 		backup.backupFiles();
 	}
 
 	@Then("^all files will be backuped$")
 	public void then(int x, int y, String orientation) {
 		Assert.assertTrue(backup.allFilesAreBackuped());
 	}
 
 }
