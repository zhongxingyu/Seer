 package test.cli.cloudify;
 
 import java.io.IOException;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.testng.annotations.Test;
 /**
  * Test whether the CLI prints runtime/compilation exception details that occur in the groovy script.
  * Cassandra service that throws an exception in the Cassandra-install.groovy file is used to simulate runtime exceptions.
  * simpleCompilation service is used to simulate a compilation error using error.groovy.
  * @author adaml
  *
  */
 public class GroovyRuntimeExceptionInCliTest extends AbstractLocalCloudTest {
 	private static final String SERVICE_WITH_RUNTIME_EXCEPTION = CommandTestUtils.getPath("apps/USM/badUsmServices/Cassandra");
 	private static final String SERVICE_WITH_COMPILATION_EXCEPTION = CommandTestUtils.getPath("apps/USM/badUsmServices/simpleCompilation");
 	private static final String RUNTIME_EXCEPTION_CAUGHT_REGEX = "(Caught:.*\\.groovy:[1-9]{1,}\\))";
 	//This regex assumes that the char before the exception string would either
 	//be a Space char or a newLine Char. to grab the whole line use thhis regex:
 	//"((\\\\|/).+\\.groovy:\\s[1-9]+.*column\\s[1-9]+.*)$";
 	private static final String COMPILATION_EXCEPTION_CAUGHT_REGEX = "(([a-zA-Z]*:\\\\|/).+\\.groovy:\\s[1-9]+.*column\\s[1-9]+)";	
 	/**
 	 * Install a service and assert CLI runtime exception details message. 
 	 * @throws IOException
 	 * @throws InterruptedException
 	 */
 	@Test(timeOut = DEFAULT_TEST_TIMEOUT, groups = "1", enabled = true)
 	public void testGroovyRuntimeExceptionHandling() throws IOException, InterruptedException {
 		String commandOutput = CommandTestUtils.runCommandExpectedFail("connect " + this.restUrl 
															+ ";install-service " + SERVICE_WITH_RUNTIME_EXCEPTION 
 															+ ";exit");
 
 		assertRegexFound(commandOutput, RUNTIME_EXCEPTION_CAUGHT_REGEX, Pattern.MULTILINE+Pattern.DOTALL);
 		
 	}
 	
 	/**
 	 * Install a service and assert CLI compilation exception details message. 
 	 * @throws IOException
 	 * @throws InterruptedException
 	 */
 	@Test(timeOut = DEFAULT_TEST_TIMEOUT, groups = "1", enabled = true)
 	public void testGroovyCompilationExceptionHandling() throws IOException, InterruptedException {
 		String commandOutput = CommandTestUtils.runCommandExpectedFail("connect " + this.restUrl 
															+ ";install-service " + SERVICE_WITH_COMPILATION_EXCEPTION 
 															+ ";exit");
 
 		assertRegexFound(commandOutput, COMPILATION_EXCEPTION_CAUGHT_REGEX, Pattern.MULTILINE);
 		
 	}
 
 	private void assertRegexFound(String commandOutput, String regex, int regexFlags) {
 		//Check whether the exception print pattern exists in the CLI output.
 		Pattern pattern = Pattern.compile(regex, regexFlags);
 		Matcher matcher = pattern.matcher(commandOutput);
 		int beginIndex = 0;
 		int endIndex = 0;
 		if (matcher.find()){
 			beginIndex = matcher.start(0);
 			endIndex = matcher.end(0);
 		}
 		//does the expression exist?
 		assertTrue("Runtime exception was not printed by the CLI.", endIndex != 0 && beginIndex != 0 );
 	}
 }
